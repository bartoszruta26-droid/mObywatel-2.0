// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

/**
 * Shared streaming download entry point. Requests are distributed across the embedded
 * Tor daemon's isolated SOCKS listeners; every listener belongs to the same Tor client.
 * The caller classifies each resource and closes the returned response with use { }.
 */
class TorDownloadService(
    private val runtime: TorRuntime,
    policy: TorRequestPolicy = TorRequestPolicy(),
    poolPolicy: () -> TorPoolPolicy = { TorPoolPolicy.ROUND_ROBIN },
    routeRules: () -> Map<TorDownloadKind, String> = { emptyMap() }
) {
    private val transport = TorHttpTransport(runtime, policy)
    private val router = TorDownloadRouter(
        profiles = {
            runtime.proxyEndpoints().mapIndexed { index, endpoint ->
                TorClientProfile(
                    id = "embedded-tor-socks-${endpoint.port}",
                    name = "Wbudowany Tor — obwód ${index + 1}",
                    host = endpoint.host,
                    port = endpoint.port
                )
            }
        },
        isReady = { profile ->
            runtime.state == TorState.READY &&
                runtime.proxyEndpoints().any { it.host == profile.host && it.port == profile.port }
        },
        policy = poolPolicy,
        rules = routeRules
    )

    fun open(url: String, kind: TorDownloadKind): TorHttpResponse {
        if (runtime.state != TorState.READY) throw java.io.IOException("Tor runtime is not ready.")
        val selected = router.select(kind)
        return transport.open(url, selected.endpoint())
    }
}
