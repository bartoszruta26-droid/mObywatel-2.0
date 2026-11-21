// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import java.io.IOException

enum class DownloadConnectionMode(val label: String, val description: String) {
    GUARDIAN_TOR("Guardian Project Tor", "Wbudowany klient Tor, uruchamiany na żądanie."),
    ORBOT("Orbot", "Lokalny SOCKS5 Orbot; aplikacja wymaga potwierdzenia wyjścia przez Tor."),
    DIRECT("Bezpośrednio — bez Tor", "HTTPS bez lokalnego proxy Tor. Adres IP urządzenia jest widoczny dla serwisu; systemowy VPN (np. Orbot w trybie VPN) może nadal kierować ruch przez tunel.")
}

/**
 * Preferred geographic scope for the Tor exit node.
 *
 * This is a best-effort Tor exit preference, not a guarantee: Tor may be unable to
 * satisfy a country or regional preference when the requested relays are unavailable.
 * DIRECT mode ignores this setting because it does not use Tor.
 */
enum class TorExitRegion(val label: String, val torCountryCodes: List<String>) {
    POLAND("Polska", listOf("pl")),
    EUROPE("Europa", listOf(
        "ad", "al", "at", "ba", "be", "bg", "by", "ch", "cy", "cz", "de", "dk",
        "ee", "es", "fi", "fr", "gb", "gr", "hr", "hu", "ie", "is", "it", "li",
        "lt", "lu", "lv", "mc", "md", "me", "mk", "mt", "nl", "no", "pl", "pt",
        "ro", "rs", "se", "si", "sk", "sm", "tr", "ua", "va"
    )),
    WORLD("Świat", emptyList())
}

object TorExitRegionSettings {
    private const val PREFS = "download_connection_settings"
    private const val KEY_REGION = "exit_region"

    fun get(context: Context): TorExitRegion {
        val stored = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_REGION, TorExitRegion.POLAND.name)
        return runCatching { TorExitRegion.valueOf(stored.orEmpty()) }
            .getOrDefault(TorExitRegion.POLAND)
    }

    fun set(context: Context, region: TorExitRegion) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_REGION, region.name).apply()
    }
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
        if (mode != DownloadConnectionMode.GUARDIAN_TOR) {
            TorDownloadServiceProvider.stopGuardian()
        }
    }
}

/** Shared download service with an explicit, user-selected network route. */
class TorDownloadService(
    private val context: Context,
    private val runtime: TorRuntime,
    policy: TorRequestPolicy = TorRequestPolicy()
) {
    private val transport = TorHttpTransport(runtime, policy)
    private val guardianRouter = TorDownloadRouter(
        profiles = {
            val isolated = (runtime as? GuardianTorRuntime)?.isolatedProxyEndpoints().orEmpty()
            val endpoints = isolated.ifEmpty { listOfNotNull(runtime.proxyEndpoint()) }
            endpoints.mapIndexed { index, endpoint ->
                TorClientProfile("guardian-embedded-${endpoint.port}", "Guardian Tor — listener ${index + 1}",
                    endpoint.host, endpoint.port)
            }
        },
        isReady = { profile ->
            val endpoint = runCatching { profile.endpoint() }.getOrNull()
            endpoint != null && runtime.state == TorState.READY &&
                runtime.proxyEndpoints().contains(endpoint) && TorEndpointProbe.isTor(endpoint)
        }
    )
    val selectedMode: DownloadConnectionMode get() = DownloadConnectionSettings.get(context)

    /**
     * Route is explicit and fail-closed: Guardian and Orbot never silently switch
     * to each other, and neither silently falls back to an unproxied connection.
     */
    @Throws(IOException::class)
    fun open(url: String, kind: TorDownloadKind, rangeStart: Long = 0L, ifRange: String? = null): TorHttpResponse =
        when (selectedMode) {
            DownloadConnectionMode.GUARDIAN_TOR -> {
                val endpoint = ensureGuardianEndpoint(kind)
                applyExitRegionPreference(endpoint)
                transport.open(url, endpoint, rangeStart, ifRange)
            }
            DownloadConnectionMode.ORBOT -> {
                val endpoint = ensureOrbotEndpoint()
                applyExitRegionPreference(endpoint)
                transport.open(url, endpoint, rangeStart, ifRange)
            }
            DownloadConnectionMode.DIRECT ->
                transport.openDirect(url, rangeStart, ifRange)
        }

    /**
     * Apply a best-effort geographic preference to the selected Tor process.
     * This must never silently change the transport or report a guaranteed country.
     * The runtime's Tor configuration is changed only while the configured exit
     * restrictions remain compatible with a healthy Tor circuit.
     */
    private fun applyExitRegionPreference(endpoint: TorProxyEndpoint) {
        val region = TorExitRegionSettings.get(context)
        val countries = region.torCountryCodes
        if (countries.isEmpty()) return
        if (selectedMode == DownloadConnectionMode.GUARDIAN_TOR) {
            (runtime as? GuardianTorRuntime)?.setExitCountryPreference(countries)
        } else {
            // For Orbot, the application cannot safely rewrite another app's torrc.
            // A profile-level instruction is retained for diagnostics; Orbot's own
            // exit-node configuration remains authoritative.
            TorClientRegistry.setExitCountryPreference(context, "orbot-local", countries)
        }
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
    private fun ensureGuardianEndpoint(kind: TorDownloadKind): TorProxyEndpoint {
        if (runtime.state == TorState.STOPPED || runtime.state == TorState.FAILED) {
            try {
                runtime.start()
            } catch (failure: Exception) {
                throw IOException("Nie udało się uruchomić wybranego klienta Guardian Tor.", failure)
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
        runtime.proxyEndpoint()
            ?: throw IOException("Guardian Tor zgłosił READY, ale nie udostępnia działającego SOCKS5.")
        return runCatching { guardianRouter.select(kind).endpoint() }
            .getOrElse { throw IOException("Guardian nie udostępnia żadnego zweryfikowanego listenera SOCKS dla pobierania.", it) }
    }
}
