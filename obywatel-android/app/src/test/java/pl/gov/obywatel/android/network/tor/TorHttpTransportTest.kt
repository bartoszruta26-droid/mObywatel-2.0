// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TorHttpTransportTest {
    private class FakeRuntime(
        override var state: TorState = TorState.READY,
        private val endpoint: TorProxyEndpoint? = TorProxyEndpoint()
    ) : TorRuntime {
        override fun start() { state = TorState.READY }
        override fun stop() { state = TorState.STOPPED }
        override fun proxyEndpoint(): TorProxyEndpoint? = endpoint
    }

    @Test fun endpointIsRestrictedToLoopback() {
        assertEquals(9050, TorProxyEndpoint().port)
        assertThrows(IllegalArgumentException::class.java) {
            TorProxyEndpoint(host = "192.0.2.10", port = 9050)
        }
        assertThrows(IllegalArgumentException::class.java) {
            TorProxyEndpoint(port = 70000)
        }
    }

    @Test fun loopbackAliasesAreAccepted() {
        assertEquals("127.0.0.1", TorProxyEndpoint().host)
        assertEquals("localhost", TorProxyEndpoint(host = "localhost").host)
        assertEquals("::1", TorProxyEndpoint(host = "::1").host)
    }

    @Test fun commonsRequestsUseAnHonestDescriptiveApplicationUserAgent() {
        val userAgent = TorRequestPolicy().userAgent
        assertTrue(userAgent.startsWith("mObywatel-2.0/"))
        assertTrue(userAgent.contains("Android"))
        assertTrue(userAgent.contains("github.com/bartoszruta26-droid/mObywatel-2.0"))
        assertTrue(!userAgent.contains("Chrome", ignoreCase = true))
        assertTrue(!userAgent.contains("Brave", ignoreCase = true))
    }

    @Test fun policyRejectsUnsafeLimits() {
        assertThrows(IllegalArgumentException::class.java) { TorRequestPolicy(maxRedirects = 11) }
        assertThrows(IllegalArgumentException::class.java) { TorRequestPolicy(maxBytes = 0) }
    }

    @Test fun transportRequiresReadyRuntimeBeforeNetworkAccess() {
        val transport = TorHttpTransport(FakeRuntime(state = TorState.STARTING))
        val failure = assertThrows(java.io.IOException::class.java) {
            transport.open("https://example.org/file.bin")
        }
        assertTrue(failure.message!!.contains("not ready"))
    }

    @Test fun transportRequiresConfiguredProxyEndpoint() {
        val transport = TorHttpTransport(FakeRuntime(endpoint = null))
        val failure = assertThrows(java.io.IOException::class.java) {
            transport.open("https://example.org/file.bin")
        }
        assertTrue(failure.message!!.contains("unavailable"))
    }

    @Test fun invalidSchemesAreRejectedBeforeConnecting() {
        val transport = TorHttpTransport(FakeRuntime())
        for (url in listOf("http://example.org/a", "file:///etc/passwd", "javascript:alert(1)")) {
            assertThrows(java.io.IOException::class.java) { transport.open(url) }
        }
    }

    @Test fun urlCredentialsAreRejected() {
        val transport = TorHttpTransport(FakeRuntime())
        assertThrows(java.io.IOException::class.java) {
            transport.open("https://user:password@example.org/file")
        }
    }
}
