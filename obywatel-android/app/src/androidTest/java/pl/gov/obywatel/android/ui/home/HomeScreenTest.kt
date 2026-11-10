// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test
import pl.gov.obywatel.android.MainActivity

/**
 * Integration checks for the actual activity-hosted home screen.
 * The former tests targeted a removed HomeScreen(onOpenSection) API and
 * therefore no longer represented the application navigation model.
 */
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsIndependentApplicationNoticeAndCurrentDashboard() {
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Inne dokumenty").assertIsDisplayed()
    }

    @Test
    fun dashboardProvidesDocumentsAndServicesEntryPoints() {
        composeRule.onAllNodesWithText("Dokumenty")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Wszystkie usługi").assertIsDisplayed()
        composeRule.onNodeWithText("Inne dokumenty").assertIsDisplayed()
    }
}
