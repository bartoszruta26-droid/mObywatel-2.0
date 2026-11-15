// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FinancialReportDateRangePickerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun opensStartCalendarAndAppliesSelectedDate() {
        val dates = mutableStateOf("2026-11-01" to "2026-11-15")
        composeRule.setContent {
            FinancialReportDateRangePicker(
                startDate = dates.value.first,
                endDate = dates.value.second,
                onDatesChanged = { start, end -> dates.value = start to end }
            )
        }

        composeRule.onNodeWithText("Data początkowa: 2026-11-01").performClick()
        composeRule.onNodeWithText("Zastosuj datę").assertIsDisplayed().performClick()

        assertEquals("2026-11-01", dates.value.first)
        assertEquals("2026-11-15", dates.value.second)
    }

    @Test
    fun dismissingCalendarDoesNotChangeEitherDate() {
        val dates = mutableStateOf("2026-11-01" to "2026-11-15")
        composeRule.setContent {
            FinancialReportDateRangePicker(
                startDate = dates.value.first,
                endDate = dates.value.second,
                onDatesChanged = { start, end -> dates.value = start to end }
            )
        }

        composeRule.onNodeWithText("Data końcowa: 2026-11-15").performClick()
        composeRule.onNodeWithText("Anuluj").assertIsDisplayed().performClick()

        assertEquals("2026-11-01" to "2026-11-15", dates.value)
        composeRule.onNodeWithText("Data końcowa: 2026-11-15").assertIsDisplayed()
    }

    @Test
    fun reversedDateRangeShowsValidationMessage() {
        composeRule.setContent {
            FinancialReportDateRangePicker(
                startDate = "2026-11-16",
                endDate = "2026-11-15",
                onDatesChanged = { _, _ -> }
            )
        }

        composeRule.onNodeWithText(
            "Data końcowa musi przypadać tego samego dnia lub po dacie początkowej."
        ).assertIsDisplayed()
    }

    @Test
    fun selectingDifferentStartDateUpdatesOnlyStartBoundary() {
        val dates = mutableStateOf("2026-11-01" to "2026-11-20")
        composeRule.setContent {
            FinancialReportDateRangePicker(
                startDate = dates.value.first,
                endDate = dates.value.second,
                onDatesChanged = { start, end -> dates.value = start to end }
            )
        }

        composeRule.onNodeWithText("Data początkowa: 2026-11-01").performClick()
        composeRule.onNode(hasContentDescription("November 10, 2026", substring = true)).assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Zastosuj datę").performClick()

        assertEquals("2026-11-10" to "2026-11-20", dates.value)
    }

    @Test
    fun selectingDifferentEndDateUpdatesOnlyEndBoundary() {
        val dates = mutableStateOf("2026-11-01" to "2026-11-15")
        composeRule.setContent {
            FinancialReportDateRangePicker(
                startDate = dates.value.first,
                endDate = dates.value.second,
                onDatesChanged = { start, end -> dates.value = start to end }
            )
        }

        composeRule.onNodeWithText("Data końcowa: 2026-11-15").performClick()
        composeRule.onNode(hasContentDescription("November 25, 2026", substring = true)).assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Zastosuj datę").performClick()

        assertEquals("2026-11-01" to "2026-11-25", dates.value)
    }
}
