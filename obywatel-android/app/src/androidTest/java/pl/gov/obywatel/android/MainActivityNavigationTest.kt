// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun selectBottomTab(label: String, matchingIndex: Int = 0) {
        composeRule.onAllNodesWithText(label)[matchingIndex].performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun dashboardShowsCurrentBrandAndDocumentWallet() {
        composeRule.onNodeWithText("Obywatel").assertIsDisplayed()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Inne dokumenty").assertIsDisplayed()
    }

    @Test
    fun documentsWalletShowsIdentityPassportAndLegitimacyRecords() {
        selectBottomTab("Dokumenty", 1)
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Twoje dokumenty i oświadczenia").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dane osobowe profilu").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dane paszportowe").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Legitymacja ucznia/studenta")[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun documentsWalletBackReturnsToDashboard() {
        selectBottomTab("Dokumenty", 1)
        composeRule.onNodeWithText("‹  Pulpit").performClick()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
    }

    @Test
    fun registryExtractOpensFromServicesCatalog() {
        selectBottomTab("Usługi", 1)
        composeRule.onNodeWithText("Rejestry").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Odpis z rejestru podmiotów").performClick()
        composeRule.onNodeWithText("Wybierz rodzaj podmiotu").assertIsDisplayed()
        composeRule.onNodeWithText("Spółka z ograniczoną odpowiedzialnością — KRS").performClick()
        composeRule.onNodeWithText("Numer KRS").assertIsDisplayed()
        composeRule.onNodeWithText("‹  Wszystkie rodzaje podmiotów").assertIsDisplayed()
    }

    @Test
    fun moreContainsSettingsDesignerAndImportedFiles() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Projektant dokumentów").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Pliki i załączniki").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ustawienia").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun legalInformationOpensFromMoreAndShowsReadmeContent() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Informacje prawne").performScrollTo().performClick()
        composeRule.onNodeWithText("STATUS I NIEZALEŻNOŚĆ APLIKACJI")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("OBYWATELSKA IDEA APLIKACJI I GODNOŚĆ CZŁOWIEKA")
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Wróć").assertIsDisplayed()
    }

    @Test
    fun designerRouteRetainsPdfLayoutAndDecorationTools() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Projektant dokumentów").performScrollTo().performClick()
        composeRule.onNodeWithText("Zarządzanie").assertIsDisplayed()
        composeRule.onNodeWithText("Projektant układu dokumentu").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dodawanie elementów dekoracyjnych").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsExposeIdentityPassportAndLegitimacyDataEntry() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Dane osobowe").assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").assertIsDisplayed()
        composeRule.onNodeWithText("Dane paszportowe").assertIsDisplayed()
        composeRule.onNodeWithText("Legitymacja ucznia/studenta").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsExposeDocumentPlacementChoices() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Widoczność dokumentów").performScrollTo().performClick()
        composeRule.onNodeWithText("Dla każdego dokumentu wybierz miejsce wyświetlania. Ukrycie dokumentu nie usuwa jego danych ani załączników.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dokument rodowy").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Inne dokumenty")[0].performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Ukryj")[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun servicesExposePublicPortalAndQrNavigationRemainsAvailable() {
        selectBottomTab("Usługi", 1)
        composeRule.onNodeWithText("Katalog usług według kategorii").assertIsDisplayed()
        composeRule.onNodeWithText("Przeglądarka usług publicznych").assertIsDisplayed()
        composeRule.onNodeWithText("Przeglądarka usług publicznych").performClick()
        composeRule.onNodeWithText("Usługi publiczne").assertIsDisplayed()
        composeRule.onNodeWithText("Szukaj usługi").assertIsDisplayed()
        composeRule.onNodeWithText("mObywatel — portal").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Wróć").performClick()
        composeRule.onNodeWithText("Katalog usług według kategorii").assertIsDisplayed()
        selectBottomTab("Kod QR")
        composeRule.onNodeWithText("Obywatelska Wymiana Danych").assertIsDisplayed()
    }
}
