// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class BankAccountsNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun enterDemoMode() {
        composeRule.onNodeWithText("Pomiń wprowadzenie").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Wejdź w trybie demonstracyjnym").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun documentWalletOpensBankAccountsInReadOnlyMode() {
        enterDemoMode()
        composeRule.onNodeWithContentDescription("Dokumenty").performClick()
        composeRule.waitForIdle()
        // Grupy są nagłówkami jednego katalogu; rachunki otwierają się bezpośrednio z listy.
        composeRule.onAllNodesWithText("Konta bankowe", substring = false)[0]
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank-accounts-screen-title").assertIsDisplayed()
        composeRule.onNodeWithText("Rejestr posiadanych rachunków bankowych (PLN i waluty obce)")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        // Ekran szczegółów jest przewijany; przewiń do komunikatu zamiast
        // zakładać, że znajduje się on od razu w widocznym obszarze.
        composeRule.onNodeWithText(
            "Widok tylko do odczytu. Zmiana danych jest dostępna w Więcej → Ustawienia."
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsKeepBankAccountsEditable() {
        enterDemoMode()
        composeRule.onNodeWithContentDescription("Więcej").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Konta bankowe").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank-accounts-screen-title").assertIsDisplayed()
        composeRule.onNodeWithText("Rejestr posiadanych rachunków bankowych (PLN i waluty obce)")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        // Kontrolki formularza leżą niżej w przewijanym ekranie.
        composeRule.onNodeWithText("Dodaj rachunek").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Zapisz").performScrollTo().assertIsDisplayed()
    }
}
