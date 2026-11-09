// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.util.Base64
import org.json.JSONObject
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Building blocks for OWD (Obywatelska Wymiana Danych).
 *
 * This class deliberately does not claim that a network or Bluetooth transport exists.
 * The QR contains only a short-lived session offer and an ephemeral public key.
 */
internal object OwdSecurity {
    private const val VERSION = 1
    private const val SESSION_TTL_SECONDS = 180L
    private const val CLOCK_SKEW_SECONDS = 15L
    private const val CURVE = "secp256r1"
    private val random = SecureRandom()

    data class SessionOffer(
        val sessionId: String,
        val createdAtEpochSeconds: Long,
        val expiresAtEpochSeconds: Long,
        val publicKeyBase64: String,
        val nonceBase64: String,
        val encodedQrPayload: String
    )

    data class EncryptedTransfer(
        val version: Int,
        val sessionId: String,
        val category: String,
        val createdAtEpochSeconds: Long,
        val ivBase64: String,
        val ciphertextBase64: String
    )

    /** Generates a QR-safe offer. No personal data or government identifier is embedded. */
    fun createOffer(nowEpochSeconds: Long = System.currentTimeMillis() / 1000): Pair<SessionOffer, KeyPair> {
        val pair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec(CURVE), random)
        }.generateKeyPair()
        val id = randomBytes(16)
        val nonce = randomBytes(32)
        val expires = nowEpochSeconds + SESSION_TTL_SECONDS
        val idText = b64(id)
        val publicText = b64(pair.public.encoded)
        val nonceText = b64(nonce)
        val json = JSONObject()
            .put("type", "OWD_SESSION")
            .put("version", VERSION)
            .put("sessionId", idText)
            .put("createdAt", nowEpochSeconds)
            .put("expiresAt", expires)
            .put("curve", CURVE)
            .put("publicKey", publicText)
            .put("nonce", nonceText)
        val offer = SessionOffer(idText, nowEpochSeconds, expires, publicText, nonceText, json.toString())
        return offer to pair
    }

    /**
     * Derives a session key using ECDH. The caller must validate the offer, obtain explicit
     * user consent, and bind this key to the selected transport/session before sending data.
     */
    fun deriveSessionKey(localPrivateKey: java.security.PrivateKey, remotePublicKey: java.security.PublicKey,
                         sessionId: String, nonceBase64: String): ByteArray {
        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(localPrivateKey)
        agreement.doPhase(remotePublicKey, true)
        val shared = agreement.generateSecret()
        val salt = MessageDigest.getInstance("SHA-256").digest(unb64(nonceBase64))
        val info = ("OWD-v$VERSION|" + sessionId).toByteArray(Charsets.UTF_8)
        return hkdfSha256(shared, salt, info, 32)
    }

    /** AES-256-GCM authenticated encryption. AAD binds ciphertext to session and data category. */
    fun encrypt(key: ByteArray, sessionId: String, category: String, plaintext: ByteArray,
                nowEpochSeconds: Long = System.currentTimeMillis() / 1000): EncryptedTransfer {
        require(key.size == 32) { "Klucz sesji musi mieć 256 bitów." }
        require(category.matches(Regex("[a-z0-9_.-]{1,64}"))) { "Nieprawidłowa kategoria danych." }
        val iv = randomBytes(12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        cipher.updateAAD(aad(sessionId, category, nowEpochSeconds))
        val encrypted = cipher.doFinal(plaintext)
        return EncryptedTransfer(VERSION, sessionId, category, nowEpochSeconds, b64(iv), b64(encrypted))
    }

    fun decrypt(key: ByteArray, transfer: EncryptedTransfer): ByteArray {
        require(transfer.version == VERSION) { "Nieobsługiwana wersja protokołu." }
        require(key.size == 32) { "Nieprawidłowy klucz sesji." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"),
            GCMParameterSpec(128, unb64(transfer.ivBase64)))
        cipher.updateAAD(aad(transfer.sessionId, transfer.category, transfer.createdAtEpochSeconds))
        return cipher.doFinal(unb64(transfer.ciphertextBase64))
    }

    /** Strict validation for scanned QR offers; expiry and type are checked before use. */
    fun validateOffer(payload: String, nowEpochSeconds: Long = System.currentTimeMillis() / 1000): JSONObject {
        require(payload.length <= 4096) { "Kod sesji jest zbyt duży." }
        val json = JSONObject(payload)
        require(json.optString("type") == "OWD_SESSION") { "To nie jest kod sesji OWD." }
        require(json.optInt("version") == VERSION) { "Nieobsługiwana wersja kodu." }
        require(json.optString("curve") == CURVE) { "Nieobsługiwana krzywa kryptograficzna." }
        val created = json.getLong("createdAt")
        val expires = json.getLong("expiresAt")
        require(expires > nowEpochSeconds) { "Kod sesji wygasł." }
        require(expires - created in 1..SESSION_TTL_SECONDS) { "Nieprawidłowy czas ważności sesji." }
        require(created <= nowEpochSeconds + CLOCK_SKEW_SECONDS) { "Kod sesji pochodzi z przyszłości." }
        require(json.getString("sessionId").let { unb64(it).size == 16 }) { "Nieprawidłowy identyfikator sesji." }
        require(json.getString("nonce").let { unb64(it).size == 32 }) { "Nieprawidłowy nonce sesji." }
        require(unb64(json.getString("publicKey")).size in 80..256) { "Nieprawidłowy klucz publiczny." }
        return json
    }

    /** Process-local replay guard. Production must persist atomic consumption across process restarts. */
    class OneTimeSessionRegistry {
        private val consumed = HashSet<String>()
        @Synchronized fun consume(sessionId: String, expiresAt: Long,
                                  nowEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
            if (expiresAt <= nowEpochSeconds || sessionId.isBlank()) return false
            return consumed.add(sessionId)
        }
        @Synchronized fun clearExpired(expiredIds: Collection<String>) { consumed.removeAll(expiredIds.toSet()) }
    }

    private fun aad(sessionId: String, category: String, timestamp: Long) =
        "OWD|$VERSION|$sessionId|$category|$timestamp".toByteArray(Charsets.UTF_8)

    private fun randomBytes(size: Int) = ByteArray(size).also(random::nextBytes)
    private fun b64(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    private fun unb64(value: String) = Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    private fun hkdfSha256(ikm: ByteArray, salt: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(ikm)
        val output = ByteArray(length)
        var previous = ByteArray(0)
        var offset = 0
        var counter = 1
        while (offset < length) {
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            mac.update(previous)
            mac.update(info)
            mac.update(counter.toByte())
            previous = mac.doFinal()
            val count = minOf(previous.size, length - offset)
            System.arraycopy(previous, 0, output, offset, count)
            offset += count
            counter++
        }
        prk.fill(0)
        previous.fill(0)
        return output
    }
}
