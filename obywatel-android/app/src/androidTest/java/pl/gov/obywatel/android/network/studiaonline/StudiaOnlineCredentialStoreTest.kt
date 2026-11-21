// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudiaOnlineCredentialStoreTest {
    @Test
    fun credentialsRoundTripAndClear() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = StudiaOnlineCredentialStore(context)
        store.clear()

        val expectedPassword = "test-password-123".toCharArray()
        store.save("test-login", expectedPassword)

        val restored = store.load()
        try {
            assertEquals("test-login", restored?.username)
            assertArrayEquals(expectedPassword, restored?.password)
        } finally {
            expectedPassword.fill('\u0000')
            restored?.password?.fill('\u0000')
        }

        store.clear()
        assertNull(store.load())
    }

    @Test
    fun savingNewCredentialsReplacesPreviousCiphertext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = StudiaOnlineCredentialStore(context)
        store.clear()

        store.save("first-user", "first-secret".toCharArray())
        store.save("second-user", "second-secret".toCharArray())

        val restored = store.load()
        try {
            assertEquals("second-user", restored?.username)
            assertArrayEquals("second-secret".toCharArray(), restored?.password)
        } finally {
            restored?.password?.fill('\u0000')
            store.clear()
        }
    }
}
