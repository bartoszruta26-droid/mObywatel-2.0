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
            // Prefer listeners explicitly configured for stream isolation. Keep the
            // primary Guardian SOCKS endpoint as a fallback if no extra listener started.
            val isolatedEndpoints = (runtime as? GuardianTorRuntime)?.isolatedProxyEndpoints().orEmpty()
            val embeddedEndpoints = isolatedEndpoints.ifEmpty { listOfNotNull(runtime.proxyEndpoint()) }
            val embedded = embeddedEndpoints.mapIndexed { index, endpoint ->
                TorClientProfile("guardian-embedded-${endpoint.port}", "Guardian Tor — isolated listener ${index + 1}", endpoint.host, endpoint.port)
            }
            // All external profiles are loopback-only and must pass a live Tor egress probe.
            // Orbot additionally must be installed; unavailable or non-Tor SOCKS is excluded.
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
                !runtime.proxyEndpoints().contains(endpoint) && (!orbotProfile || TorEndpointProbe.isOrbotInstalled(context)) && TorEndpointProbe.isTor(endpoint)
            }
        },
        policy = poolPolicy,
        rules = routeRules
    )

    /**
     * Opens one resource through a verified Tor client. If a selected external SOCKS
     * endpoint has stopped accepting Tor traffic, this request is retried once through Guardian.
     * No direct-network fallback is implemented.
     */
    @Throws(IOException::class)
    fun open(url: String, kind: TorDownloadKind, rangeStart: Long = 0L, ifRange: String? = null): TorHttpResponse {
        if (runtime.state != TorState.READY) throw IOException("Guardian Tor runtime is not ready.")
        val selected = router.select(kind)
        try {
            return transport.open(url, selected.endpoint(), rangeStart, ifRange)
        } catch (failure: IOException) {
            // HTTP errors come from the destination and must not be hidden by switching clients.
            if (failure is TorHttpException || selected.id.startsWith("guardian-embedded-")) throw failure
            TorEndpointProbe.markUnavailable(selected.endpoint())
            // If Orbot or another external local Tor client drops, retry exactly once via Guardian.
            val guardian = runtime.proxyEndpoint()
                ?: throw IOException("External Tor client failed and Guardian SOCKS endpoint is unavailable.", failure)
            if (guardian == selected.endpoint()) throw failure
            return try {
                transport.open(url, guardian, rangeStart, ifRange)
            } catch (fallbackFailure: IOException) {
                fallbackFailure.addSuppressed(failure)
                throw fallbackFailure
            }
        }
    }
}
