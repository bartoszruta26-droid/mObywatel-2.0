// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import java.io.IOException

enum class TorDownloadKind { API, SVG, IMAGE, DOCUMENT, ARCHIVE, OTHER }
enum class TorPoolPolicy { ROUND_ROBIN, PINNED_BY_KIND }

/**
 * Selects a configured local Tor client for each download.
 *
 * Readiness must be supplied by the concrete runtime adapter. A listening SOCKS port alone
 * is not proof that the endpoint is a Tor client; production adapters must verify bootstrap
 * and Tor egress before returning true.
 */
class TorDownloadRouter(
    private val profiles: () -> List<TorClientProfile>,
    private val isReady: (TorClientProfile) -> Boolean,
    private val policy: () -> TorPoolPolicy = { TorPoolPolicy.ROUND_ROBIN },
    private val rules: () -> Map<TorDownloadKind, String> = { emptyMap() }
) {
    private var cursor = 0

    @Synchronized
    @Throws(IOException::class)
    fun select(kind: TorDownloadKind): TorClientProfile {
        val eligible = profiles().asSequence()
            .filter { it.enabled }
            .distinctBy { "${it.host}:${it.port}" }
            .filter { runCatching { isReady(it) }.getOrDefault(false) }
            .toList()

        if (eligible.isEmpty()) throw IOException("No ready Tor client is available.")

        if (policy() == TorPoolPolicy.PINNED_BY_KIND) {
            val selectedId = rules()[kind]
                ?: throw IOException("No Tor client is assigned to download type $kind.")
            return eligible.firstOrNull { it.id == selectedId }
                ?: throw IOException("Assigned Tor client '$selectedId' is disabled or unavailable.")
        }

        val selected = eligible[cursor.mod(eligible.size)]
        cursor = (cursor + 1).mod(eligible.size)
        return selected
    }
}
