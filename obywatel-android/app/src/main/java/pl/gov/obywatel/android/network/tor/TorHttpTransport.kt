// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import java.io.Closeable
import java.io.FilterInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

/** Lifecycle/state contract. A Tor runtime implementation may be embedded or externally managed. */
interface TorRuntime {
    val state: TorState
    fun start()
    fun stop()
    fun proxyEndpoint(): TorProxyEndpoint?
}

enum class TorState { STOPPED, STARTING, BOOTSTRAPPING, READY, STOPPING, FAILED }

/** A loopback-only SOCKS5 endpoint. Remote DNS is requested by using an unresolved target address. */
data class TorProxyEndpoint(
    val host: String = "127.0.0.1",
    val port: Int = 9050
) {
    init {
        require(host == "127.0.0.1" || host == "::1" || host == "localhost") {
            "Tor SOCKS endpoint must be local."
        }
        require(port in 1..65535) { "Invalid SOCKS port." }
    }

    internal fun asJavaProxy(): Proxy =
        Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port))
}

data class TorRequestPolicy(
    val connectTimeoutMs: Int = 15_000,
    val readTimeoutMs: Int = 30_000,
    val maxRedirects: Int = 5,
    val maxBytes: Long = 64L * 1024L * 1024L,
    val userAgent: String = "mObywatel-2.0"
) {
    init {
        require(connectTimeoutMs > 0)
        require(readTimeoutMs > 0)
        require(maxRedirects in 0..10)
        require(maxBytes > 0)
        require(userAgent.isNotBlank())
    }
}

/** Owns the connection and response stream. Closing it always disconnects the HTTP connection. */
class TorHttpResponse internal constructor(
    val statusCode: Int,
    val contentLength: Long?,
    val contentType: String?,
    val finalUrl: URL,
    private val connection: HttpURLConnection,
    input: java.io.InputStream,
    private val maxBytes: Long
) : Closeable {
    val body: java.io.InputStream = object : FilterInputStream(input) {
        private var consumed = 0L
        override fun read(): Int {
            val value = super.read()
            if (value >= 0) account(1)
            return value
        }
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            val count = super.read(buffer, offset, length)
            if (count > 0) account(count.toLong())
            return count
        }
        private fun account(count: Long) {
            consumed += count
            if (consumed > maxBytes) {
                close()
                throw IOException("Response exceeds configured byte limit.")
            }
        }
    }

    override fun close() {
        runCatching { body.close() }
        connection.disconnect()
    }
}

/**
 * Reusable HTTPS-over-Tor transport. It does not persist response bytes and does not own storage.
 * The Tor runtime must already be READY and expose a loopback SOCKS5 endpoint.
 */
class TorHttpTransport(
    private val runtime: TorRuntime,
    private val policy: TorRequestPolicy = TorRequestPolicy()
) {
    @Throws(IOException::class)
    fun open(rawUrl: String): TorHttpResponse {
        if (runtime.state != TorState.READY) throw IOException("Tor runtime is not ready.")
        val endpoint = runtime.proxyEndpoint() ?: throw IOException("Tor SOCKS endpoint is unavailable.")
        var url = validateHttpsUrl(rawUrl)
        var redirects = 0

        while (true) {
            val connection = (url.openConnection(endpoint.asJavaProxy()) as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = policy.connectTimeoutMs
                readTimeout = policy.readTimeoutMs
                requestMethod = "GET"
                setRequestProperty("User-Agent", policy.userAgent)
                setRequestProperty("Accept-Encoding", "identity")
                useCaches = false
            }
            try {
                val code = connection.responseCode
                if (code in REDIRECT_CODES) {
                    val location = connection.getHeaderField("Location")
                        ?: throw IOException("Redirect response has no Location header.")
                    if (redirects >= policy.maxRedirects) throw IOException("Redirect limit exceeded.")
                    val next = validateHttpsUrl(URL(url, location).toExternalForm())
                    connection.disconnect()
                    url = next
                    redirects++
                    continue
                }
                if (code !in 200..299) {
                    val message = "Tor HTTP request failed: HTTP $code"
                    connection.disconnect()
                    throw IOException(message)
                }
                val length = connection.getHeaderFieldLong("Content-Length", -1L)
                    .takeIf { it >= 0L }
                if (length != null && length > policy.maxBytes) {
                    connection.disconnect()
                    throw IOException("Response exceeds configured byte limit.")
                }
                return TorHttpResponse(
                    code, length, connection.contentType, url, connection,
                    connection.inputStream, policy.maxBytes
                )
            } catch (failure: Exception) {
                connection.disconnect()
                if (failure is IOException) throw failure
                throw IOException("Unable to open HTTPS response through Tor.", failure)
            }
        }
    }

    private fun validateHttpsUrl(value: String): URL {
        val parsed = try { java.net.URI(value) } catch (failure: Exception) {
            throw IOException("Invalid download URL.", failure)
        }
        if (!parsed.scheme.equals("https", ignoreCase = true) ||
            parsed.host.isNullOrBlank() || parsed.rawUserInfo != null ||
            parsed.port !in -1..65535
        ) throw IOException("Only absolute HTTPS URLs without embedded credentials are allowed.")
        return try { parsed.toURL() } catch (failure: Exception) {
            throw IOException("Invalid HTTPS URL.", failure)
        }
    }

    private companion object {
        val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    }
}
