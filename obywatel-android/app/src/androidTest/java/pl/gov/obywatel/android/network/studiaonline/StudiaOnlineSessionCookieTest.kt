// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import android.webkit.CookieManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StudiaOnlineSessionCookieTest {
    @Test
    fun cookieForDownloadPathIsAvailableToTheNativeDownloaderProvider() {
        assertTrue(StudiaOnlineSessionCookies.DOWNLOAD_COOKIE_SCOPE.endsWith("//pobierz-material/"))
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val cleared = CountDownLatch(1)
        instrumentation.runOnMainSync {
            manager.removeAllCookies { cleared.countDown() }
        }
        assertTrue("CookieManager did not clear the previous test state",
            cleared.await(5, TimeUnit.SECONDS))
        manager.flush()

        val latch = CountDownLatch(2)
        instrumentation.runOnMainSync {
            manager.setCookie(
                StudiaOnlineSessionCookies.DOWNLOAD_COOKIE_SCOPE,
                "session_root=instrumentation; Path=/; Secure; HttpOnly"
            ) { latch.countDown() }
            manager.setCookie(
                StudiaOnlineSessionCookies.DOWNLOAD_COOKIE_SCOPE,
                "session_route=instrumentation; Path=//pobierz-material/; Secure; HttpOnly"
            ) { latch.countDown() }
        }

        try {
            assertTrue("CookieManager did not finish setting the test cookies",
                latch.await(5, TimeUnit.SECONDS))
            manager.flush()
            val header = StudiaOnlineSessionCookies.currentHeader().orEmpty()
            assertTrue("The shared cookie provider did not return the root-scoped cookie",
                header.contains("session_root=instrumentation"))
            assertTrue("The shared cookie provider did not return the exact-route cookie",
                header.contains("session_route=instrumentation"))
        } finally {
            val removed = CountDownLatch(1)
            instrumentation.runOnMainSync {
                manager.removeAllCookies { removed.countDown() }
            }
            assertTrue("CookieManager did not clear the test cookies",
                removed.await(5, TimeUnit.SECONDS))
            manager.flush()
        }
    }
}
