// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TorDownloadRouterTest {
    @Test fun torExitRegionDefaultsAreWellFormed() {
        assertEquals(listOf("pl"), TorExitRegion.POLAND.torCountryCodes)
        assertTrue(TorExitRegion.EUROPE.torCountryCodes.isNotEmpty())
        assertTrue(TorExitRegion.EUROPE.torCountryCodes.all { it.matches(Regex("[a-z]{2}")) })
        assertTrue(TorExitRegion.WORLD.torCountryCodes.isEmpty())
    }

    private val clients = listOf(
        TorClientProfile("a", "Client A", "127.0.0.1", 9050),
        TorClientProfile("b", "Client B", "127.0.0.1", 9150),
        TorClientProfile("disabled", "Disabled", "127.0.0.1", 9250, false)
    )

    @Test fun selectedInstalledOrbotDefersGuardianStartup() {
        assertEquals(false, shouldStartGuardianForSelectedClient("orbot-local", true))
    }

    @Test fun GuardianStartsWhenItIsSelectedOrOrbotIsMissing() {
        assertEquals(true, shouldStartGuardianForSelectedClient("guardian-embedded", true))
        assertEquals(true, shouldStartGuardianForSelectedClient("orbot-local", false))
    }

    @Test fun roundRobinDistributesAcrossReadyDistinctEndpoints() {
        val router = TorDownloadRouter({ clients }, { it.id != "disabled" })
        assertEquals("a", router.select(TorDownloadKind.SVG).id)
        assertEquals("b", router.select(TorDownloadKind.IMAGE).id)
        assertEquals("a", router.select(TorDownloadKind.DOCUMENT).id)
    }

    @Test fun unavailableClientsAreExcludedFromPool() {
        val router = TorDownloadRouter({ clients }, { it.id == "b" })
        assertEquals("b", router.select(TorDownloadKind.SVG).id)
        assertEquals("b", router.select(TorDownloadKind.IMAGE).id)
    }

    @Test fun noReadyClientFailsClosed() {
        val router = TorDownloadRouter({ clients }, { false })
        assertThrows(IOException::class.java) { router.select(TorDownloadKind.SVG) }
    }

    @Test fun pinnedPolicyUsesExplicitKindRule() {
        val router = TorDownloadRouter(
            profiles = { clients },
            isReady = { it.id != "disabled" },
            policy = { TorPoolPolicy.PINNED_BY_KIND },
            rules = { mapOf(TorDownloadKind.SVG to "b") }
        )
        assertEquals("b", router.select(TorDownloadKind.SVG).id)
        assertThrows(IOException::class.java) { router.select(TorDownloadKind.IMAGE) }
    }

    @Test fun pinnedPolicyDoesNotSilentlyFallbackWhenAssignedClientIsDown() {
        val router = TorDownloadRouter(
            profiles = { clients },
            isReady = { it.id == "a" },
            policy = { TorPoolPolicy.PINNED_BY_KIND },
            rules = { mapOf(TorDownloadKind.SVG to "b") }
        )
        assertThrows(IOException::class.java) { router.select(TorDownloadKind.SVG) }
    }

    @Test fun duplicateEndpointsDoNotPretendToBeIndependentClients() {
        val duplicated = clients + TorClientProfile("same-port", "Alias", "localhost", 9050)
        val router = TorDownloadRouter({ duplicated }, { true })
        val selected = List(10) { router.select(TorDownloadKind.OTHER).id }.toSet()
        assertEquals(setOf("a", "b"), selected)
    }
}
