// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gov.obywatel.android.ui.theme.ObywatelTheme

class DocumentsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun clearImportedDocuments() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("imported_documents", Context.MODE_PRIVATE)
            .edit().remove("document_uris").commit()
    }

    @Test
    fun showsLocalPrototypeNoticeAndEmptyState() {
        composeRule.setContent {
            ObywatelTheme {
                DocumentsScreen(onBack = {}, readOnly = true)
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Brak dokumentów do wyświetlenia.")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Moje dokumenty").assertIsDisplayed()
        composeRule.onNodeWithText("Brak dokumentów do wyświetlenia.").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Dodaj PDF, DOCX, ODT lub obraz").fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithText("Dodawanie i usuwanie plików jest dostępne w Więcej → Ustawienia → Dokumenty własne — pliki.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesHostNavigationWhenNoDocumentIsSelected() {
        var backCount = 0
        composeRule.setContent {
            ObywatelTheme {
                DocumentsScreen(onBack = { backCount += 1 })
            }
        }

        composeRule.onNodeWithContentDescription("Wróć do poprzedniego ekranu").performClick()
        org.junit.Assert.assertEquals(1, backCount)
    }
}
