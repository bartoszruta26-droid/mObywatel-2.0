// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.Closeable
import java.io.FilterInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.TimeUnit

/** Lifecycle/state contract. A Tor runtime implementation may be embedded or externally managed. */
interface TorRuntime {
    val state: TorState
    fun start()
    fun stop()
    fun proxyEndpoint(): TorProxyEndpoint?
    fun proxyEndpoints(): List<TorProxyEndpoint> = listOfNotNull(proxyEndpoint())
    fun failureMessage(): String? = null
}

enum class TorState { STOPPED, STARTING, BOOTSTRAPPING, READY, STOPPING, FAILED }

class TorHttpException(
    val httpCode: Int,
    val retryAfter: String?,
    message: String
) : IOException(message)

/** A loopback-only SOCKS5 endpoint. */
data class TorProxyEndpoint(val host: String = "127.0.0.1", val port: Int = 9050) {
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

/** Owns the OkHttp response and stream. Closing it releases the response and socket. */
class TorHttpResponse internal constructor(
    val statusCode: Int,
    val contentLength: Long?,
    val contentType: String?,
    val finalUrl: URL,
    private val response: Response,
    input: java.io.InputStream,
    private val maxBytes: Long
) : Closeable {
    fun header(name: String): String? = response.header(name)
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
        response.close()
    }
}

/**
 * Reusable HTTPS transport for Tor-routed and explicitly direct downloads.
 * SOCKS routing uses unresolved destination addresses in OkHttp, so destination DNS is
 * resolved by the SOCKS proxy rather than the Android device. Direct requests explicitly
 * disable proxy selection and are only used when the user selects the unprotected route.
 */
class TorHttpTransport(
    private val runtime: TorRuntime,
    private val policy: TorRequestPolicy = TorRequestPolicy()
) {
    private val clientCache = HashMap<TorProxyEndpoint, OkHttpClient>()
    private val directClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .connectTimeout(policy.connectTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(policy.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .cache(null)
            .build()
    }

    @Synchronized
    private fun client(endpoint: TorProxyEndpoint): OkHttpClient =
        clientCache.getOrPut(endpoint) {
            OkHttpClient.Builder()
                .proxy(endpoint.asJavaProxy())
                .connectTimeout(policy.connectTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(policy.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null)
                .build()
        }

    @Throws(IOException::class)
    fun open(rawUrl: String): TorHttpResponse {
        if (runtime.state != TorState.READY) throw IOException("Tor runtime is not ready.")
        val endpoint = runtime.proxyEndpoint() ?: throw IOException("Tor SOCKS endpoint is unavailable.")
        return open(rawUrl, endpoint)
    }

    /**
     * Opens a request through a locally managed client selected by TorDownloadRouter.
     * The caller is responsible for selecting only a runtime-confirmed READY Tor endpoint.
     */
    fun open(
        rawUrl: String,
        endpoint: TorProxyEndpoint,
        rangeStart: Long = 0L,
        ifRange: String? = null
    ): TorHttpResponse = openUsing(rawUrl, rangeStart, ifRange) { client(endpoint) }

    /** Explicit direct HTTPS transport. No SOCKS proxy or Tor fallback is used. */
    fun openDirect(
        rawUrl: String,
        rangeStart: Long = 0L,
        ifRange: String? = null
    ): TorHttpResponse = openUsing(rawUrl, rangeStart, ifRange) { directClient }

    private fun openUsing(
        rawUrl: String,
        rangeStart: Long,
        ifRange: String?,
        clientForRequest: () -> OkHttpClient
    ): TorHttpResponse {
        require(rangeStart >= 0L) { "Range offset cannot be negative." }
        var url = validateHttpsUrl(rawUrl)
        var redirects = 0

        while (true) {
            val request = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", policy.userAgent)
                .header("Accept-Encoding", "identity")
                .apply {
                    if (rangeStart > 0L) header("Range", "bytes=$rangeStart-")
                    if (rangeStart > 0L && !ifRange.isNullOrBlank()) header("If-Range", ifRange)
                }
                .build()
            val response = try {
                clientForRequest().newCall(request).execute()
            } catch (failure: Exception) {
                if (failure is IOException) throw failure
                throw IOException("Unable to open HTTPS response.", failure)
            }

            val code = response.code
            if (code in REDIRECT_CODES) {
                val location = response.header("Location")
                response.close()
                if (location.isNullOrBlank()) throw IOException("Redirect response has no Location header.")
                if (redirects >= policy.maxRedirects) throw IOException("Redirect limit exceeded.")
                url = validateHttpsUrl(url.toURI().resolve(location).toString())
                redirects++
                continue
            }
            if (!response.isSuccessful) {
                val retryAfter = response.header("Retry-After")
                response.close()
                throw TorHttpException(code, retryAfter, "HTTPS request failed: HTTP $code")
            }

            val body = response.body
            if (body == null) {
                response.close()
                throw IOException("Tor HTTP response has no body.")
            }
            val length = body.contentLength().takeIf { it >= 0L }
            if (length != null && length > policy.maxBytes) {
                response.close()
                throw IOException("Response exceeds configured byte limit.")
            }
            return TorHttpResponse(
                code, length, body.contentType()?.toString(), url,
                response, body.byteStream(), policy.maxBytes
            )
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
