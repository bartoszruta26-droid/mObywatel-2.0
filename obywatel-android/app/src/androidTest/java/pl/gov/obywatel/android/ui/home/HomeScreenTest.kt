// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import pl.gov.obywatel.android.MainActivity

/** Integration checks for the current activity-hosted dashboard. */
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun dashboardShowsDocumentWalletAndNavigation() {
        composeRule.onAllNodesWithText("Pulpit", substring = false)[0].assertIsDisplayed()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
    }

    @Test
    fun dashboardProvidesServicesEntryPoint() {
        composeRule.onNodeWithText("Wszystkie usługi").performScrollTo().assertIsDisplayed()
    }
}
