// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.gov.obywatel.android.ui.theme.ObywatelTheme

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsIndependentApplicationNoticeAndAllServiceEntries() {
        composeRule.setContent {
            ObywatelTheme { HomeScreen(onOpenSection = {}) }
        }
        composeRule.onNodeWithText("Niezależna aplikacja").assertIsDisplayed()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
        listOf(
            "Dokumenty", "Rejestr dokumentów", "Weryfikacja QR", "Moje dane",
            "Bezpieczeństwo", "Płatności", "Zdrowie", "Więcej"
        ).forEach { title ->
            composeRule.onNodeWithText(title).performScrollTo().assertIsDisplayed().assertHasClickAction()
        }
    }

    @Test
    fun selectingEachServiceEmitsItsRoute() {
        val opened = mutableListOf<String>()
        composeRule.setContent {
            ObywatelTheme { HomeScreen(onOpenSection = { opened.add(it) }) }
        }
        listOf(
            "Dokumenty" to "documents",
            "Rejestr dokumentów" to "registry",
            "Weryfikacja QR" to "verify",
            "Moje dane" to "profile",
            "Bezpieczeństwo" to "security",
            "Płatności" to "payments",
            "Zdrowie" to "health",
            "Więcej" to "more"
        ).forEach { (label, route) ->
            composeRule.onNodeWithText(label).performScrollTo().performClick()
            composeRule.waitForIdle()
            assertEquals(route, opened.last())
        }
        assertEquals(8, opened.size)
    }
}
