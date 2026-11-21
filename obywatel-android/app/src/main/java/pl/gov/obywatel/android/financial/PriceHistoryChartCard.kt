// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PriceChartPoint(val date: LocalDate, val value: BigDecimal)
private enum class PriceChartInstrument(val label: String) { GOLD("Złoto"), CURRENCY("Waluta") }
private enum class PriceChartPeriod(val label: String, val days: Long) {
    WEEK("7 dni", 7), MONTH("1 mies.", 31), QUARTER("3 mies.", 93),
    HALF_YEAR("6 mies.", 183), YEAR("1 rok", 365), FIVE_YEARS("5 lat", 1825)
}

@Composable
fun PriceHistoryChartCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) {
        NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache"))
    }
    val scope = rememberCoroutineScope()
    var instrument by remember { mutableStateOf(PriceChartInstrument.GOLD) }
    var period by remember { mutableStateOf(PriceChartPeriod.YEAR) }
    var currencyCode by remember { mutableStateOf("EUR") }
    var points by remember { mutableStateOf<List<PriceChartPoint>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loadedLabel by remember { mutableStateOf("") }

    fun loadHistory() {
        scope.launch {
            loading = true
            error = null
            try {
                val end = LocalDate.now()
                val start = end.minusDays(period.days - 1)
                val loaded = withContext(Dispatchers.IO) {
                    when (instrument) {
                        PriceChartInstrument.GOLD -> repository.goldPriceHistory(start, end).map {
                            PriceChartPoint(it.date, it.pricePerGramPln)
                        }
                        PriceChartInstrument.CURRENCY -> repository.currencyRateHistory(
                            NbpTable.A, currencyCode.trim().uppercase(), start, end
                        ).map { PriceChartPoint(it.first, it.second) }
                    }
                }
                require(loaded.isNotEmpty()) { "NBP nie udostępnił notowań dla wybranego instrumentu i okresu." }
                points = loaded
                loadedLabel = if (instrument == PriceChartInstrument.GOLD) {
                    "Cena złota • PLN/g"
                } else {
                    "Kurs ${currencyCode.uppercase()} • PLN za 1 jednostkę"
                }
            } catch (exception: Exception) {
                points = emptyList()
                error = exception.message ?: "Nie udało się pobrać historii notowań."
            } finally {
                loading = false
            }
        }
    }

    val values = points.map { it.value }
    val minimum = values.minOrNull()
    val maximum = values.maxOrNull()
    val first = values.firstOrNull()
    val last = values.lastOrNull()
    val change = if (first != null && last != null && first.compareTo(BigDecimal.ZERO) != 0) {
        last.subtract(first).multiply(BigDecimal("100")).divide(first, 4, java.math.RoundingMode.HALF_EVEN)
    } else null

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wykres cen i notowań", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceChartInstrument.entries.forEach { option ->
                Button(onClick = { instrument = option; points = emptyList(); error = null }) {
                    Text(if (instrument == option) "✓ ${option.label}" else option.label)
                }
            }
        }
        if (instrument == PriceChartInstrument.CURRENCY) {
            OutlinedTextField(
                value = currencyCode,
                onValueChange = { currencyCode = it.filter(Char::isLetter).uppercase().take(3); points = emptyList() },
                label = { Text("Kod waluty ISO 4217") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Kurs średni tabeli A NBP, wyrażony w PLN za jednostkę waluty.")
        } else {
            Text("Cena referencyjna NBP za gram złota, wyrażona w PLN.")
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PriceChartPeriod.entries.toList()) { option ->
                Button(onClick = { period = option; points = emptyList(); error = null }) {
                    Text(if (period == option) "✓ ${option.label}" else option.label)
                }
            }
        }
        Button(
            onClick = ::loadHistory,
            enabled = !loading && (instrument != PriceChartInstrument.CURRENCY || currencyCode.length == 3)
        ) { Text(if (points.isEmpty()) "Wyświetl wykres" else "Odśwież wykres") }
        if (loading) CircularProgressIndicator()
        if (points.isNotEmpty()) {
            Text(loadedLabel, style = MaterialTheme.typography.titleSmall)
            PriceHistoryLineChart(points = points, modifier = Modifier.fillMaxWidth().height(210.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Minimum", style = MaterialTheme.typography.labelSmall)
                    Text(minimum?.toPlainString().orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text("Maksimum", style = MaterialTheme.typography.labelSmall)
                    Text(maximum?.toPlainString().orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text("Zmiana", style = MaterialTheme.typography.labelSmall)
                    Text(change?.let { "${if (it.signum() > 0) "+" else ""}${it.toPlainString()}%" }.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Punkty: ${points.size} • ${points.first().date} – ${points.last().date}")
            Text("Ostatnia cena: ${last?.toPlainString()} • ${points.last().date}")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(
            "Źródło: Narodowy Bank Polski. Notowanie referencyjne; wykres pomija dni bez publikacji (weekendy i święta).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PriceHistoryLineChart(points: List<PriceChartPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val values = points.map { it.value.toFloat() }
    val minValue = values.minOrNull() ?: 0f
    val maxValue = values.maxOrNull() ?: 0f
    val span = (maxValue - minValue).takeIf { it > 0f } ?: 1f
    Canvas(modifier = modifier) {
        val left = 8.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = size.height - 12.dp.toPx()
        repeat(4) { index ->
            val y = top + (bottom - top) * index / 3f
            drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
        }
        if (points.size == 1) {
            val y = bottom - ((values.first() - minValue) / span) * (bottom - top)
            drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset((left + right) / 2f, y))
        } else if (points.size > 1) {
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = left + (right - left) * index / values.lastIndex.toFloat()
                val y = bottom - ((value - minValue) / span) * (bottom - top)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx()))
        }
    }
}
