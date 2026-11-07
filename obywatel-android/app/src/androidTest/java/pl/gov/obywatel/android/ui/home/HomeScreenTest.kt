// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.gov.obywatel.android.ui.theme.ObywatelTheme

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsPrototypeNoticeAndAvailableDocumentsService() {
        composeRule.setContent {
            ObywatelTheme {
                HomeScreen(onOpenDocuments = {})
            }
        }

        composeRule.onNodeWithText("To prototyp interfejsu. Brak prawdziwych dokumentów i usług.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Dokumenty").assertIsDisplayed()
        composeRule.onNodeWithText("Podgląd dokumentów (demo)").assertIsDisplayed()
    }

    @Test
    fun servicesUnderPreparationAreDisabled() {
        composeRule.setContent {
            ObywatelTheme {
                HomeScreen(onOpenDocuments = {})
            }
        }

        composeRule.onNodeWithText("Moje dane").assertIsNotEnabled()
        composeRule.onNodeWithText("Płatności").assertIsNotEnabled()
    }

    @Test
    fun selectingDocumentsInvokesNavigation() {
        var openCount = 0
        composeRule.setContent {
            ObywatelTheme {
                HomeScreen(onOpenDocuments = { openCount += 1 })
            }
        }

        composeRule.onNodeWithText("Dokumenty").performClick()
        assertEquals(1, openCount)
    }
}
