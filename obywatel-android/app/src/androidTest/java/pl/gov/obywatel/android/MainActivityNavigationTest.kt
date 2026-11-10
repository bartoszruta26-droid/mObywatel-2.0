// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
    fun restoredDashboardShowsStateEmblemBrandAndDocumentShortcuts() {
        composeRule.onNodeWithText("mObywatel 2.0").assertIsDisplayed()
        composeRule.onNodeWithText("Pulpit").assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").assertIsDisplayed()
        composeRule.onNodeWithText("Dane paszportowe").assertIsDisplayed()
        composeRule.onNodeWithText("Dane rejestru podmiotów").assertIsDisplayed()
    }

    @Test
    fun documentsWalletShowsIdentityPassportAndLegitimacyRecords() {
        selectBottomTab("Dokumenty", 1)
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").assertIsDisplayed()
        composeRule.onNodeWithText("Dane paszportowe").assertIsDisplayed()
        composeRule.onAllNodesWithText("Legitymacja ucznia/studenta")[0].assertIsDisplayed()
    }

    @Test
    fun documentsWalletBackReturnsToDashboard() {
        selectBottomTab("Dokumenty", 1)
        composeRule.onNodeWithText("‹  Pulpit").performClick()
        composeRule.onNodeWithText("mObywatel 2.0").assertIsDisplayed()
    }

    @Test
    fun entityRegistryShortcutOpensRegistryExtract() {
        composeRule.onNodeWithText("Dane rejestru podmiotów").performClick()
        composeRule.onNodeWithText("Odpis z rejestru podmiotów").assertIsDisplayed()
    }

    @Test
    fun moreContainsSettingsDesignerAndImportedFiles() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Projektant dokumentów").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Pliki i załączniki").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ustawienia").performScrollTo().assertIsDisplayed()
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
        composeRule.onNodeWithText("Legitymacja ucznia/studenta").assertIsDisplayed()
    }

    @Test
    fun settingsExposePerDocumentPlacementChoicesAndDashboardOtherDocumentsSection() {
        selectBottomTab("Więcej")
        composeRule.onNodeWithText("Ustawienia").performScrollTo().performClick()
        composeRule.onNodeWithText("Widoczność dokumentów na ekranie głównym").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dowód tożsamości").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dokument rodowy").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Inne dokumenty").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ukryj").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun servicesRetainModulesAndExposeEmbeddedPublicPortalBrowser() {
        selectBottomTab("Usługi", 1)
        composeRule.onNodeWithText("Katalog usług według kategorii").assertIsDisplayed()
        composeRule.onNodeWithText("Przeglądarka usług publicznych").assertIsDisplayed()
        composeRule.onNodeWithText("Przeglądarka usług publicznych").performClick()
        composeRule.onNodeWithText("Usługi publiczne").assertIsDisplayed()
        composeRule.onNodeWithText("Szukaj usługi").assertIsDisplayed()
        composeRule.onNodeWithText("mObywatel — portal").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun servicesBrowserBackReturnsToServiceCatalogAndQrStillWorks() {
        selectBottomTab("Usługi", 1)
        composeRule.onNodeWithText("Przeglądarka usług publicznych").performClick()
        composeRule.onNodeWithText("Wróć").performClick()
        composeRule.onNodeWithText("Katalog usług według kategorii").assertIsDisplayed()
        selectBottomTab("Kod QR")
        composeRule.onNodeWithText("Obywatelska Wymiana Danych").assertIsDisplayed()
    }
}
