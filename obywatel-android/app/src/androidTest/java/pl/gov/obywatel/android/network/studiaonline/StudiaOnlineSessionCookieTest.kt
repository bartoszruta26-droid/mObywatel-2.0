// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import android.webkit.CookieManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StudiaOnlineSessionCookieTest {
    @Test
    fun cookieForDownloadPathIsAvailableToTheNativeDownloaderProvider() {
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        manager.removeAllCookies(null)
        manager.flush()

        val latch = CountDownLatch(1)
        manager.setCookie(
            StudiaOnlineSessionCookies.DOWNLOAD_COOKIE_SCOPE,
            "session_test=instrumentation; Path=/pobierz-material/; Secure; HttpOnly"
        ) { latch.countDown() }

        try {
            assertTrue("CookieManager did not finish setting the test cookie",
                latch.await(5, TimeUnit.SECONDS))
            manager.flush()
            val header = StudiaOnlineSessionCookies.currentHeader().orEmpty()
            assertTrue("The shared cookie provider did not return the path-scoped cookie",
                header.contains("session_test=instrumentation"))
        } finally {
            manager.removeAllCookies(null)
            manager.flush()
        }
    }
}
