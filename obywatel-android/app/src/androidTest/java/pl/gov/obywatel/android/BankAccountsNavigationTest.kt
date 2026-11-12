// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import org.junit.Assert.assertTrue
import org.junit.Before

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

    @Before
    fun resetDocumentDisplayPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("document_display_preferences", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    private fun openSettingsMenu() {
        enterDemoMode()
        composeRule.onNodeWithContentDescription("Więcej").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    private fun enterDemoMode() {
        // Plansza z godłem jest wyświetlana przez 1,5 s przed ekranem powitalnym.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Zaloguj się", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        // Przejście do lokalnego trybu demonstracyjnego prowadzi przez dwa ekrany logowania.
        composeRule.onNodeWithText("Zaloguj się", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Zaloguj się", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun documentWalletOpensBankAccountsInReadOnlyMode() {
        // Włącz widoczność rachunków przez rzeczywisty ekran ustawień.
        openSettingsMenu()
        composeRule.onNodeWithText("Widoczność dokumentów").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("document-placement-bank_accounts-dashboard")
            .performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("‹  Powrót do ustawień").performScrollTo().performClick()
        composeRule.waitForIdle()
        // Wróć do pulpitu przez widoczny element nawigacji zamiast polegać
        // na globalnym BackHandler, który zależy od aktywnej podstrony.
        composeRule.onNodeWithText("‹  Powrót do ekranu głównego")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Dokumenty").performClick()
        composeRule.waitForIdle()
        // Karta nawigacyjna „Dokumenty” otwiera pulpit; portfel dokumentów
        // znajduje się pod nagłówkiem „Zarządzaj” na tym pulpicie.
        composeRule.onNodeWithText("Zarządzaj").performScrollTo().performClick()
        composeRule.waitForIdle()
        // Na pulpicie występuje kilka kart „Konta bankowe” (różne miejsca
        // prezentacji tego samego dokumentu). Wybierz pierwszą kartę w sekcji
        // „Zarządzaj”, zamiast wymagać globalnej unikalności tekstu.
        composeRule.onAllNodesWithText("Konta bankowe", substring = false)
            .get(0)
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank-accounts-screen-title").assertIsDisplayed()
        composeRule.onNodeWithText("Rejestr posiadanych rachunków bankowych (PLN i waluty obce)")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(
            "Podgląd wyłącznie do odczytu. Zmiana danych jest dostępna w Więcej → Ustawienia → konkretny dokument."
        ).performScrollTo().assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Dodaj rachunek").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithText("Zapisz dane").fetchSemanticsNodes().isEmpty())
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
        composeRule.onNodeWithText("Zapisz dane").performScrollTo().assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Podgląd wyłącznie do odczytu.", substring = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun androidBackReturnsFromApplicationAppearanceToSettingsMenu() {
        openSettingsMenu()
        composeRule.onNodeWithText("Ustawienia aplikacji").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Motyw i kolory").assertIsDisplayed()

        Espresso.pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Wspólny wygląd legitymacji")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun documentIconsCanBeSelectedGloballyAndPerDocument() {
        openSettingsMenu()
        composeRule.onNodeWithText("Widoczność dokumentów").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Domyślna ikona dokumentów").assertIsDisplayed()
        composeRule.onNodeWithText("Automatycznie — ikona przypisana do dokumentu").performClick()
        composeRule.onNodeWithText("Karta").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Dowód tożsamości").performScrollTo()
        // Każdy wiersz dokumentu ma własny selektor; drugi odpowiada „Dowód tożsamości”.
        composeRule.onNodeWithTag("document-icon-selector-identity").performScrollTo().performClick()
        composeRule.onNodeWithText("Paszport").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Paszport").assertIsDisplayed()
    }

    @Test
    fun dashboardShowsDocumentsMarkedAsVisibleInBothPlacements() {
        openSettingsMenu()
        composeRule.onNodeWithText("Widoczność dokumentów").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("document-placement-passport-dashboard").performScrollTo().performClick()
        composeRule.onNodeWithTag("document-placement-training-other").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("‹  Powrót do ustawień").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("‹  Powrót do ekranu głównego").performScrollTo().performClick()
        composeRule.waitForIdle()
        // Menu „Więcej” pozostaje wybraną zakładką po zamknięciu ustawień.
        composeRule.onNodeWithContentDescription("Dokumenty").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("dashboard-document-passport").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-document-training").assertIsDisplayed()
    }

    @Test
    fun applicationTileSizeSettingAlsoResizesIdentityDashboardCard() {
        openSettingsMenu()
        composeRule.onNodeWithText("Ustawienia aplikacji").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Małe kafelki").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Powrót do ustawień").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("‹  Powrót do ekranu głównego").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Dokumenty").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("identity-dashboard-card-small").assertIsDisplayed()
    }

    @Test
    fun sharedCredentialAppearanceExposesSeparateCrestAndFlagTransforms() {
        openSettingsMenu()
        composeRule.onNodeWithText("Wspólny wygląd legitymacji").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Skala godła", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Obrót godła", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Skala flagi", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Obrót flagi", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun individualCredentialEditorExposesBackgroundSelectors() {
        openSettingsMenu()
        composeRule.onNodeWithText("Legitymacja kursanta").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Tło legitymacji").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Wybierz tło z katalogu").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Wczytaj własne tło").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Wektorowy motyw tła kafelka").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Miniikona kafelka").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Kolor gradientowego tła kafelka — paleta heraldyczna").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Lazur").performScrollTo().assertIsDisplayed()
    }
}
