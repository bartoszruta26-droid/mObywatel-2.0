// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        composeRule.onNodeWithTag("bottom-nav-0").assertIsDisplayed()
        composeRule.onNodeWithText("Portfel dokumentów").assertIsDisplayed()
        composeRule.onNodeWithText("Dokumenty").assertIsDisplayed()
    }

    @Test
    fun dashboardProvidesServicesEntryPoint() {
        composeRule.onNodeWithText("Wszystkie usługi").performScrollTo().assertIsDisplayed()
    }
}
