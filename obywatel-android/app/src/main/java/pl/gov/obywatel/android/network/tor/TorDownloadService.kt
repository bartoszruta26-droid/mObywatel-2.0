// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import java.io.IOException

/** Shared Tor-only streaming download service for all app resources. */
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
                runtime.state == TorState.READY && runtime.proxyEndpoints().contains(endpoint)
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
     * The selected external client (Orbot) is used independently of the embedded
     * Guardian runtime. Guardian starts lazily only when no selected client is ready
     * or an external SOCKS connection fails. No direct-network fallback exists.
     */
    @Throws(IOException::class)
    fun open(url: String, kind: TorDownloadKind, rangeStart: Long = 0L, ifRange: String? = null): TorHttpResponse {
        val selected = try {
            router.select(kind)
        } catch (selectionFailure: IOException) {
            // An unavailable Orbot must not strand the request: start Guardian as
            // the verified fallback, then ask the router to select again.
            ensureGuardianEndpoint()
            try {
                router.select(kind)
            } catch (retryFailure: IOException) {
                retryFailure.addSuppressed(selectionFailure)
                throw retryFailure
            }
        }

        try {
            return transport.open(url, selected.endpoint(), rangeStart, ifRange)
        } catch (failure: IOException) {
            if (failure is TorHttpException || selected.id.startsWith("guardian-embedded-")) throw failure
            TorEndpointProbe.markUnavailable(selected.endpoint())
            val guardian = ensureGuardianEndpoint()
            if (guardian == selected.endpoint()) throw failure
            return try {
                transport.open(url, guardian, rangeStart, ifRange)
            } catch (fallbackFailure: IOException) {
                fallbackFailure.addSuppressed(failure)
                throw fallbackFailure
            }
        }
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
            throw IOException("Guardian Tor nie osiągnął stanu READY; pobieranie pozostaje zablokowane.")
        }
        return runtime.proxyEndpoint()
            ?: throw IOException("Guardian Tor zgłosił READY, ale nie udostępnia działającego SOCKS5.")
    }
}
