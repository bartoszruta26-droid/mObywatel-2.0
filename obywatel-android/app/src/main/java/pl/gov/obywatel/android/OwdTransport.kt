// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * OWD transport primitives. The QR endpoint is an address for the sender's
 * temporary listener; it contains no personal data or session key.
 */
internal data class OwdEndpoint(val host: String, val port: Int) {
    init {
        require(host.isNotBlank() && host.length <= 255)
        require(port in 1..65535)
    }

    fun toJson(): JSONObject = JSONObject().put("host", host).put("port", port)

    companion object {
        fun fromJson(json: JSONObject) = OwdEndpoint(json.getString("host"), json.getInt("port"))
    }
}

internal interface OwdTransport {
    val channelName: String
    fun send(endpoint: OwdEndpoint, transfer: OwdSecurity.EncryptedTransfer, sessionKey: ByteArray): OwdTransportReceipt
}

internal data class OwdTransportReceipt(
    val sessionId: String,
    val accepted: Boolean,
    val receivedAtEpochSeconds: Long,
    val channelName: String,
    val macBase64: String
)

/** Direct local-network transport. Payload remains AES-GCM encrypted end-to-end. */
internal class OwdDirectTcpTransport(
    private val connectTimeoutMillis: Int = 4_000,
    private val readTimeoutMillis: Int = 8_000,
    private val maxFrameBytes: Int = 2 * 1024 * 1024
) : OwdTransport {
    override val channelName = "direct-tcp"

    override fun send(
        endpoint: OwdEndpoint,
        transfer: OwdSecurity.EncryptedTransfer,
        sessionKey: ByteArray
    ): OwdTransportReceipt {
        require(sessionKey.size == 32)
        val socket = Socket()
        socket.connect(InetSocketAddress(endpoint.host, endpoint.port), connectTimeoutMillis)
        socket.soTimeout = readTimeoutMillis
        socket.use {
            val output = DataOutputStream(it.getOutputStream())
            val input = DataInputStream(it.getInputStream())
            val frame = OwdTransferCodec.encode(transfer).toByteArray(Charsets.UTF_8)
            require(frame.size <= maxFrameBytes) { "Pakiet OWD przekracza dozwolony rozmiar." }
            output.writeInt(frame.size)
            output.write(frame)
            output.flush()

            val ackSize = input.readInt()
            require(ackSize in 1..4096) { "Nieprawidłowe potwierdzenie odbioru." }
            val ack = ByteArray(ackSize)
            input.readFully(ack)
            return OwdReceiptCodec.decode(String(ack, Charsets.UTF_8), sessionKey, transfer.sessionId)
        }
    }

    /**
     * One-shot receiver. Caller must run this on a worker thread, validate the
     * session/consent before calling it, and store accepted plaintext securely.
     */
    fun receiveOnce(
        server: ServerSocket,
        expectedSessionId: String,
        sessionKey: ByteArray,
        onVerifiedPayload: (OwdSecurity.EncryptedTransfer, ByteArray) -> Unit
    ): OwdTransportReceipt {
        require(sessionKey.size == 32)
        server.soTimeout = readTimeoutMillis
        server.accept().use { socket ->
            socket.soTimeout = readTimeoutMillis
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())
            val frameSize = input.readInt()
            require(frameSize in 1..maxFrameBytes) { "Nieprawidłowy rozmiar pakietu." }
            val bytes = ByteArray(frameSize)
            input.readFully(bytes)
            val transfer = OwdTransferCodec.decode(JSONObject(String(bytes, Charsets.UTF_8)))
            require(transfer.sessionId == expectedSessionId) { "Pakiet należy do innej sesji." }
            val plaintext = OwdSecurity.decrypt(sessionKey, transfer)
            onVerifiedPayload(transfer, plaintext)
            val receipt = OwdReceiptCodec.create(expectedSessionId, System.currentTimeMillis() / 1000, sessionKey)
            val ack = OwdReceiptCodec.encode(receipt).toByteArray(Charsets.UTF_8)
            output.writeInt(ack.size)
            output.write(ack)
            output.flush()
            plaintext.fill(0)
            return receipt
        }
    }
}

/** Relay transport is deliberately injectable; no unapproved relay endpoint is hard-coded. */
internal interface OwdRelayTransport : OwdTransport

internal class OwdAutomaticTransport(
    private val direct: OwdTransport = OwdDirectTcpTransport(),
    private val relay: OwdRelayTransport? = null
) {
    data class Result(val receipt: OwdTransportReceipt, val attemptedChannels: List<String>)

    fun send(
        endpoint: OwdEndpoint,
        transfer: OwdSecurity.EncryptedTransfer,
        sessionKey: ByteArray,
        allowRelay: Boolean
    ): Result {
        val attempted = mutableListOf<String>()
        attempted += direct.channelName
        try {
            return Result(direct.send(endpoint, transfer, sessionKey), attempted)
        } catch (directFailure: Exception) {
            if (!allowRelay) throw directFailure
            val fallback = relay ?: throw IllegalStateException(
                "Kanał bezpośredni jest niedostępny, a przekaźnik OWD nie został skonfigurowany.",
                directFailure
            )
            attempted += fallback.channelName
            return Result(fallback.send(endpoint, transfer, sessionKey), attempted)
        }
    }
}

internal object OwdReceiptCodec {
    fun encode(receipt: OwdTransportReceipt): String = receipt.toJson().toString()

    fun create(sessionId: String, timestamp: Long, key: ByteArray): OwdTransportReceipt {
        val mac = hmac(key, message(sessionId, timestamp))
        return OwdTransportReceipt(sessionId, true, timestamp, "receiver", android.util.Base64.encodeToString(
            mac, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        ))
    }

    fun decode(json: String, key: ByteArray, expectedSessionId: String): OwdTransportReceipt {
        val obj = JSONObject(json)
        val sessionId = obj.getString("sessionId")
        val accepted = obj.getBoolean("accepted")
        val timestamp = obj.getLong("receivedAt")
        require(sessionId == expectedSessionId && accepted) { "Odbiorca odrzucił transfer lub sesja jest niezgodna." }
        val supplied = android.util.Base64.decode(
            obj.getString("mac"), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        )
        val expected = hmac(key, message(sessionId, timestamp))
        require(java.security.MessageDigest.isEqual(supplied, expected)) { "Nieprawidłowy podpis potwierdzenia odbioru." }
        return OwdTransportReceipt(sessionId, true, timestamp, obj.optString("channel", "receiver"), obj.getString("mac"))
    }

    private fun message(sessionId: String, timestamp: Long) =
        "OWD-RECEIPT|v1|$sessionId|$timestamp|accepted".toByteArray(Charsets.UTF_8)

    private fun hmac(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key, "HmacSHA256"))
            doFinal(data)
        }

    private fun OwdTransportReceipt.toJson() = JSONObject()
        .put("sessionId", sessionId)
        .put("accepted", accepted)
        .put("receivedAt", receivedAtEpochSeconds)
        .put("channel", channelName)
        .put("mac", macBase64)
}

internal object OwdTransferCodec {
    fun encode(transfer: OwdSecurity.EncryptedTransfer): String = JSONObject()
        .put("version", transfer.version)
        .put("sessionId", transfer.sessionId)
        .put("category", transfer.category)
        .put("createdAt", transfer.createdAtEpochSeconds)
        .put("iv", transfer.ivBase64)
        .put("ciphertext", transfer.ciphertextBase64)
        .toString()

    fun decode(json: JSONObject) = OwdSecurity.EncryptedTransfer(
        version = json.getInt("version"),
        sessionId = json.getString("sessionId"),
        category = json.getString("category"),
        createdAtEpochSeconds = json.getLong("createdAt"),
        ivBase64 = json.getString("iv"),
        ciphertextBase64 = json.getString("ciphertext")
    )
}
