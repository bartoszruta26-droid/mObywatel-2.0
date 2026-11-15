// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Kalendarzowy wybór obu granic raportu. Daty z DatePicker są interpretowane
 * jako dni kalendarzowe UTC, a dopiero model okresu mapuje je na strefę urządzenia.
 */
@Composable
fun FinancialReportDateRangePicker(
    startDate: String,
    endDate: String,
    onDatesChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startMillis = remember(startDate) { dateToUtcMillis(startDate) }
    val endMillis = remember(endDate) { dateToUtcMillis(endDate) }
    val invalidRange = runCatching {
        LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))
    }.getOrDefault(true)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { showStartPicker = true }) {
            Text("Data początkowa: $startDate")
        }
        Button(onClick = { showEndPicker = true }) {
            Text("Data końcowa: $endDate")
        }
        if (invalidRange) {
            Text("Data końcowa musi przypadać tego samego dnia lub po dacie początkowej.")
        }
    }

    if (showStartPicker) {
        key(startDate) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = startMillis)
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            onDatesChanged(millisToDate(selected), endDate)
                        }
                        showStartPicker = false
                    }) { Text("Zastosuj datę") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartPicker = false }) { Text("Anuluj") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }

    if (showEndPicker) {
        key(endDate) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = endMillis)
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            onDatesChanged(startDate, millisToDate(selected))
                        }
                        showEndPicker = false
                    }) { Text("Zastosuj datę") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndPicker = false }) { Text("Anuluj") }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}

private fun dateToUtcMillis(value: String): Long =
    LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun millisToDate(value: Long): String =
    Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate().toString()
