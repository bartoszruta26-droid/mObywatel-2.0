// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import java.io.IOException

enum class DownloadConnectionMode(val label: String, val description: String) {
    GUARDIAN_TOR("Guardian Project Tor", "Wbudowany klient Tor, uruchamiany na żądanie."),
    ORBOT("Orbot", "Lokalny SOCKS5 Orbot; aplikacja wymaga potwierdzenia wyjścia przez Tor."),
    DIRECT("Bezpośrednio — bez Tor", "Zwykłe połączenie HTTPS. Adres IP urządzenia jest widoczny dla serwisu i operatora sieci.")
}

object DownloadConnectionSettings {
    private const val PREFS = "download_connection_settings"
    private const val KEY_MODE = "mode"

    fun get(context: Context): DownloadConnectionMode {
        val stored = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODE, DownloadConnectionMode.GUARDIAN_TOR.name)
        return runCatching { DownloadConnectionMode.valueOf(stored.orEmpty()) }
            .getOrDefault(DownloadConnectionMode.GUARDIAN_TOR)
    }

    fun set(context: Context, mode: DownloadConnectionMode) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_MODE, mode.name).apply()
    }
}

/** Shared download service with an explicit, user-selected network route. */
class TorDownloadService(
    private val context: Context,
    private val runtime: TorRuntime,
    policy: TorRequestPolicy = TorRequestPolicy(),
    poolPolicy: () -> TorPoolPolicy = { TorPoolPolicy.ROUND_ROBIN },
    routeRules: () -> Map<TorDownloadKind, String> = { emptyMap() }
) {
    private val transport = TorHttpTransport(runtime, policy)
    private val router = TorDownloadRouter(
        profiles = {
            val isolatedEndpoints = (runtime as? GuardianTorRuntime)?.isolatedProxyEndpoints().orEmpty()
            val embeddedEndpoints = isolatedEndpoints.ifEmpty { listOfNotNull(runtime.proxyEndpoint()) }
            val embedded = embeddedEndpoints.mapIndexed { index, endpoint ->
                TorClientProfile("guardian-embedded-${endpoint.port}", "Guardian Tor — listener ${index + 1}", endpoint.host, endpoint.port)
            }
            val external = TorClientRegistry.load(context).filter { profile ->
                profile.enabled && profile.id !in setOf(
                    "guardian-embedded", "guardian-isolated-1", "guardian-isolated-2",
                    "guardian-isolated-3", "guardian-isolated-4"
                )
            }
            val all = embedded + external
            val preferredId = TorClientRegistry.activeId(context)
            val preferred = all.firstOrNull { profile ->
                profile.id == preferredId ||
                    (preferredId == "guardian-embedded" && profile.id.startsWith("guardian-embedded-"))
            }
            if (preferred == null) all else listOf(preferred) + all.filter { it.id != preferred.id }
        },
        isReady = { profile ->
            val endpoint = runCatching { profile.endpoint() }.getOrNull()
            if (endpoint == null) false
            else if (profile.id.startsWith("guardian-embedded-")) {
                val primaryEndpoint = runtime.proxyEndpoint()
                runtime.state == TorState.READY &&
                    runtime.proxyEndpoints().contains(endpoint) &&
                    primaryEndpoint != null &&
                    TorEndpointProbe.isTor(primaryEndpoint)
            } else {
                val orbotProfile = profile.id.startsWith("orbot-")
                !runtime.proxyEndpoints().contains(endpoint) &&
                    (!orbotProfile || TorEndpointProbe.isOrbotInstalled(context)) &&
                    TorEndpointProbe.isTor(endpoint)
            }
        },
        policy = poolPolicy,
        rules = routeRules
    )

    /**
     * Route is explicit and fail-closed: Guardian and Orbot never silently switch
     * to each other, and neither silently falls back to an unproxied connection.
     */
    @Throws(IOException::class)
    fun open(url: String, kind: TorDownloadKind, rangeStart: Long = 0L, ifRange: String? = null): TorHttpResponse =
        when (DownloadConnectionSettings.get(context)) {
            DownloadConnectionMode.GUARDIAN_TOR ->
                transport.open(url, ensureGuardianEndpoint(), rangeStart, ifRange)
            DownloadConnectionMode.ORBOT ->
                transport.open(url, ensureOrbotEndpoint(), rangeStart, ifRange)
            DownloadConnectionMode.DIRECT ->
                transport.openDirect(url, rangeStart, ifRange)
        }

    @Throws(IOException::class)
    private fun ensureOrbotEndpoint(): TorProxyEndpoint {
        if (!TorEndpointProbe.isOrbotInstalled(context)) {
            throw IOException("Wybrano Orbot, ale aplikacja Orbot nie jest zainstalowana.")
        }
        val profile = TorClientRegistry.load(context).firstOrNull { it.id == "orbot-local" && it.enabled }
            ?: throw IOException("Profil Orbot jest wyłączony lub nie istnieje. Włącz go w ustawieniach klientów Tor.")
        val endpoint = runCatching { profile.endpoint() }
            .getOrElse { throw IOException("Nieprawidłowy lokalny port SOCKS5 Orbot.", it) }
        if (runtime.proxyEndpoints().contains(endpoint)) {
            throw IOException("Port Orbot pokrywa się z listenerem Guardian. Zmień port Orbot lub zatrzymaj Guardian.")
        }
        if (!TorEndpointProbe.isTor(endpoint)) {
            throw IOException("Orbot nie potwierdził wyjścia przez sieć Tor. Uruchom Orbot i sprawdź lokalny port SOCKS5.")
        }
        return endpoint
    }

    @Throws(IOException::class)
    private fun ensureGuardianEndpoint(): TorProxyEndpoint {
        if (runtime.state == TorState.STOPPED || runtime.state == TorState.FAILED) {
            try {
                runtime.start()
            } catch (failure: Exception) {
                throw IOException("Nie udało się uruchomić zapasowego klienta Guardian Tor.", failure)
            }
        }
        val deadline = System.currentTimeMillis() + 120_000L
        while (runtime.state != TorState.READY && runtime.state != TorState.FAILED &&
            System.currentTimeMillis() < deadline
        ) {
            try {
                Thread.sleep(250L)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Oczekiwanie na Guardian Tor zostało przerwane.", interrupted)
            }
        }
        if (runtime.state != TorState.READY) {
            throw IOException("Guardian Tor nie osiągnął stanu READY; pobieranie pozostaje zablokowane." +
                (runtime.failureMessage()?.let { " Przyczyna zgłoszona przez usługę: $it" } ?: " Brak komunikatu ACTION_ERROR z usługi Tor."))
        }
        val endpoint = runtime.proxyEndpoint()
            ?: throw IOException("Guardian Tor zgłosił READY, ale nie udostępnia działającego SOCKS5.")
        if (!TorEndpointProbe.isTor(endpoint)) {
            throw IOException("Guardian udostępnia SOCKS5, ale nie potwierdzono wyjścia przez sieć Tor.")
        }
        return endpoint
    }
}
