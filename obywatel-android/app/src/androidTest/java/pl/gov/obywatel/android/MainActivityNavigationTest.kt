// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun documentsServiceOpensAndSystemBackReturnsHome() {
        composeRule.onNodeWithText("Dokumenty").performClick()
        composeRule.onNodeWithText("Moje dokumenty").assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Panel dokumentów i rejestru").assertIsDisplayed()
    }

    @Test
    fun documentsToolbarBackReturnsToHome() {
        composeRule.onNodeWithText("Dokumenty").performClick()
        composeRule.onNodeWithText("Moje dokumenty").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Wróć do ekranu głównego").performClick()
        composeRule.onNodeWithText("Panel dokumentów i rejestru").assertIsDisplayed()
    }

    @Test
    fun registryRouteOpensRegistryScreen() {
        composeRule.onNodeWithText("Rejestr dokumentów").performClick()
        composeRule.onNodeWithText("Wybierz plik i zarejestruj SHA-256").assertIsDisplayed()
    }

    @Test
    fun everyInformationModuleCanBeOpened() {
        val cases = listOf(
            "Moje dane" to "Ustawienia profilu przechowywanego lokalnie na urządzeniu.",
            "Bezpieczeństwo" to "Docelowo: blokada aplikacji",
            "Płatności" to "Moduł informacyjny.",
            "Zdrowie" to "Moduł informacyjny przeznaczony"
        )
        cases.forEach { (tile, expected) ->
            composeRule.onNodeWithText(tile).performClick()
            composeRule.onNodeWithText(expected, substring = true).assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Wróć").performClick()
        }
    }
}
