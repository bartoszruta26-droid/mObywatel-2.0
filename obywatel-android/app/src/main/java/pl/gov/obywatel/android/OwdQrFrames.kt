// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Offline multi-QR framing for OWD payloads.
 *
 * This is an OWD-specific transport format. It is NOT the mObywatel/mWeryfikator
 * certificate format and must never be advertised as accepted by the government app.
 * Callers must encrypt and authenticate the payload before framing it.
 */
internal object OwdQrFrames {
    private const val TYPE = "OWD_QR_FRAME"
    private const val VERSION = 1
    private const val MAX_FRAME_CHARS = 700
    private const val MAX_FRAMES = 128
    private const val MAX_PAYLOAD_BYTES = 64 * 1024

    data class FrameSet(val transferId: String, val frames: List<String>, val sha256: String)
    data class Frame(val transferId: String, val index: Int, val total: Int,
                     val digest: String, val chunk: String)

    /** Splits already-encrypted bytes into QR-sized JSON frames with a whole-payload digest. */
    fun encode(encryptedPayload: ByteArray, maxFrameChars: Int = MAX_FRAME_CHARS): FrameSet {
        require(encryptedPayload.isNotEmpty()) { "Ładunek OWD jest pusty." }
        require(encryptedPayload.size <= MAX_PAYLOAD_BYTES) { "Ładunek OWD przekracza limit 64 KiB." }
        require(maxFrameChars in 300..MAX_FRAME_CHARS) { "Nieprawidłowy rozmiar ramki QR." }

        val digest = hex(MessageDigest.getInstance("SHA-256").digest(encryptedPayload))
        val transferId = digest.take(24)
        val encoded = Base64.encodeToString(encryptedPayload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        // Determine chunk size conservatively; JSON overhead is bounded and checked below.
        var chunkSize = maxFrameChars - 220
        var chunks: List<String>
        while (true) {
            chunks = encoded.chunked(chunkSize)
            require(chunks.size <= MAX_FRAMES) { "Transfer wymaga zbyt wielu kodów QR." }
            val total = chunks.size
            val frames = chunks.mapIndexed { index, chunk ->
                JSONObject().put("type", TYPE).put("version", VERSION)
                    .put("transferId", transferId).put("index", index)
                    .put("total", total).put("sha256", digest).put("chunk", chunk).toString()
            }
            if (frames.all { it.length <= maxFrameChars }) {
                return FrameSet(transferId, frames, digest)
            }
            chunkSize -= 16
            require(chunkSize >= 64) { "Nie można zmieścić ramki w limicie QR." }
        }
    }

    /** Parses one OWD frame and rejects malformed, oversized or inconsistent metadata. */
    fun decodeFrame(raw: String): Frame {
        require(raw.length <= MAX_FRAME_CHARS) { "Ramka QR jest zbyt duża." }
        val json = JSONObject(raw)
        require(json.optString("type") == TYPE) { "To nie jest ramka OWD." }
        require(json.optInt("version") == VERSION) { "Nieobsługiwana wersja ramki OWD." }
        val id = json.getString("transferId")
        val index = json.getInt("index")
        val total = json.getInt("total")
        val digest = json.getString("sha256")
        val chunk = json.getString("chunk")
        require(id.matches(Regex("[0-9a-f]{24}"))) { "Nieprawidłowy identyfikator transferu." }
        require(digest.matches(Regex("[0-9a-f]{64}")) && digest.startsWith(id)) { "Nieprawidłowy skrót transferu." }
        require(total in 1..MAX_FRAMES && index in 0 until total) { "Nieprawidłowy numer ramki." }
        require(chunk.isNotEmpty() && chunk.matches(Regex("[A-Za-z0-9_-]+"))) { "Nieprawidłowa zawartość ramki." }
        return Frame(id, index, total, digest, chunk)
    }

    /**
     * Reassembles a complete set. Missing, duplicated-conflicting, mixed-transfer or
     * tampered frames are rejected. The caller must then decrypt/authenticate the payload.
     */
    fun reassemble(rawFrames: Collection<String>): ByteArray {
        require(rawFrames.isNotEmpty() && rawFrames.size <= MAX_FRAMES) { "Nieprawidłowa liczba ramek." }
        val parsed = rawFrames.map(::decodeFrame)
        val first = parsed.first()
        require(parsed.all {
            it.transferId == first.transferId && it.total == first.total && it.digest == first.digest
        }) { "Ramki pochodzą z różnych transferów." }

        require(parsed.map { it.index }.toSet().size == parsed.size) {
            "Transfer zawiera powtórzone numery ramek QR."
        }
        val byIndex = parsed.associateBy { it.index }
        require(byIndex.size == first.total && (0 until first.total).all(byIndex::containsKey)) {
            "Brakuje ramek transferu OWD."
        }
        val joined = (0 until first.total).joinToString("") { byIndex.getValue(it).chunk }
        val bytes = try {
            Base64.decode(joined, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Nieprawidłowe kodowanie ładunku OWD.")
        }
        require(bytes.isNotEmpty() && bytes.size <= MAX_PAYLOAD_BYTES) { "Nieprawidłowy rozmiar ładunku OWD." }
        val actual = hex(MessageDigest.getInstance("SHA-256").digest(bytes))
        require(actual == first.digest && actual.take(24) == first.transferId) {
            "Niezgodność integralności transferu OWD."
        }
        return bytes
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }
}
