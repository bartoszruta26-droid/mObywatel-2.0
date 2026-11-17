// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import android.content.pm.PackageManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.Proxy
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** Verifies that a local SOCKS endpoint actually reaches the Tor network. */
object TorEndpointProbe {
    private data class Result(val isTor: Boolean, val checkedAt: Long)
    private val cache = ConcurrentHashMap<String, Result>()

    fun isOrbotInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo("org.torproject.android", PackageManager.PackageInfoFlags.of(0))
    }.isSuccess

    fun isTor(endpoint: TorProxyEndpoint, force: Boolean = false): Boolean {
        val key = "${endpoint.host}:${endpoint.port}"
        val now = System.currentTimeMillis()
        val cached = cache[key]
        if (!force && cached != null && now - cached.checkedAt < if (cached.isTor) 60_000L else 15_000L) {
            return cached.isTor
        }
        val result = runCatching {
            val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(endpoint.host, endpoint.port))
            val client = OkHttpClient.Builder().proxy(proxy)
                .connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS)
                .callTimeout(12, TimeUnit.SECONDS).followRedirects(false).cache(null).build()
            val request = Request.Builder().url("https://check.torproject.org/api/ip")
                .header("Accept", "application/json").header("User-Agent", "mObywatel-2.0-Tor-Probe").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) false
                else JSONObject(response.body?.string().orEmpty()).optBoolean("IsTor", false)
            }
        }.getOrDefault(false)
        cache[key] = Result(result, now)
        return result
    }
}

/** Process-wide runtime. The embedded Guardian client remains the guaranteed fallback. */
object TorDownloadServiceProvider {
    @Volatile private var runtime: GuardianTorRuntime? = null
    @Volatile private var service: TorDownloadService? = null

    @Synchronized
    fun get(context: Context): TorDownloadService {
        val app = context.applicationContext
        val activeRuntime = runtime ?: GuardianTorRuntime(app).also { runtime = it }
        if (activeRuntime.state == TorState.STOPPED || activeRuntime.state == TorState.FAILED) {
            activeRuntime.start()
        }
        val deadline = System.currentTimeMillis() + 120_000L
        while (activeRuntime.state != TorState.READY && activeRuntime.state != TorState.FAILED &&
            System.currentTimeMillis() < deadline) {
            Thread.sleep(250L)
        }
        if (activeRuntime.state != TorState.READY) {
            throw java.io.IOException("Wbudowany klient Guardian Tor nie osiągnął stanu READY; pobieranie zatrzymano.")
        }
        return service ?: TorDownloadService(app, activeRuntime).also { service = it }
    }
}
