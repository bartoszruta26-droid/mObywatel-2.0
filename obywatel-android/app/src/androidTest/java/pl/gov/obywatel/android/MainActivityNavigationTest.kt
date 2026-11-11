// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

/**
 * Navigation checks against the current MainActivity route model.
 * Bottom-navigation items use stable test tags rather than ambiguous repeated labels.
 */
class MainActivityNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun selectBottomTab(index: Int) {
        val label = listOf("Pulpit", "Dokumenty", "Usługi", "Kod QR", "Więcej")[index]
        composeRule.onAllNodesWithText(label, substring = false)[0].performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun dashboardShowsCurrentDocumentWalletAndServicesEntry() {
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Wszystkie usługi").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun bankAccountsDocumentEntryOpensDedicatedReadOnlyCatalog() {
        selectBottomTab(1)
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
    fun bankAccountsSettingsEntryRemainsEditable() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Konta bankowe").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank-accounts-screen-title").assertIsDisplayed()
        composeRule.onNodeWithText("Dodaj rachunek").assertIsDisplayed()
        composeRule.onNodeWithText("Zapisz").assertIsDisplayed()
    }

    @Test
    fun documentsWalletShowsIdentityPassportAndLegitimacyRecords() {
        selectBottomTab(1)
        composeRule.onAllNodesWithText("Dane osobowe profilu")[0].performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Dowód tożsamości")[0].performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Dane paszportowe")[0].performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Legitymacja kursanta")[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun documentsWalletBackReturnsToDashboard() {
        selectBottomTab(1)
        composeRule.onNodeWithText("‹  Pulpit").performClick()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
    }

    @Test
    fun registryExtractOpensFromServicesCatalog() {
        selectBottomTab(2)
        composeRule.onNodeWithText("Odpis z rejestru podmiotów")
            .performScrollTo().performClick()
        composeRule.onNodeWithText("Wybierz rodzaj podmiotu").assertIsDisplayed()
        composeRule.onNodeWithText("Spółka z ograniczoną odpowiedzialnością — KRS")
            .performScrollTo().performClick()
        composeRule.onNodeWithText("Numer KRS").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun moreContainsSettingsDesignerAndImportedFiles() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Projektant dokumentów").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Pliki i załączniki").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ustawienia").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun legalInformationOpensFromMoreAndShowsReadmeContent() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Informacje prawne").performScrollTo().performClick()
        composeRule.onNodeWithText("STATUS I NIEZALEŻNOŚĆ APLIKACJI")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("OBYWATELSKA IDEA APLIKACJI I GODNOŚĆ CZŁOWIEKA")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun designerRouteRetainsPdfLayoutAndDecorationTools() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Projektant dokumentów").performScrollTo().performClick()
        composeRule.onNodeWithText("Zarządzanie").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Projektant układu dokumentu").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dodawanie elementów dekoracyjnych").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsExposeIdentityPassportAndLegitimacyDataEntry() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Dane osobowe", substring = false).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości", substring = false).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dane paszportowe", substring = false).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Legitymacja ucznia/studenta")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsExposeDocumentPlacementChoices() {
        selectBottomTab(4)
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Widoczność dokumentów").performScrollTo().performClick()
        composeRule.onNodeWithText(
            "Dla każdego dokumentu wybierz miejsce wyświetlania. Ukrycie dokumentu nie usuwa jego danych ani załączników."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dokument rodowy").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Ukryj")[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun servicesExposePublicPortalAndSearch() {
        selectBottomTab(2)
        composeRule.onNodeWithText("Katalog usług według kategorii").assertIsDisplayed()
        composeRule.onNodeWithText("Przeglądarka usług publicznych").performScrollTo().performClick()
        composeRule.onNodeWithText("Usługi publiczne").assertIsDisplayed()
        composeRule.onNodeWithText("Znajdź sprawę urzędową").assertIsDisplayed()
    }

    @Test
    fun qrNavigationRemainsAvailable() {
        selectBottomTab(3)
        composeRule.onNodeWithText("Obywatelska Wymiana Danych").assertIsDisplayed()
    }
}
