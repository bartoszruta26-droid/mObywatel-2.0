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
        // Portfel najpierw pokazuje kafelki grup; rachunki są w grupie „Państwowe”.
        composeRule.onNodeWithText("Państwowe", substring = false).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Konta bankowe", substring = false)[0]
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank-accounts-screen-title").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Widok tylko do odczytu. Zmiana danych jest dostępna w Więcej → Ustawienia."
        ).assertIsDisplayed()
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
        composeRule.onNodeWithText("Dodaj rachunek").assertIsDisplayed()
        composeRule.onNodeWithText("Zapisz").assertIsDisplayed()
    }
}
