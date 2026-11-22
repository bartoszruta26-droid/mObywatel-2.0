// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BankStatementNbpPoint(val date: LocalDate, val netPln: BigDecimal)

data class BankStatementNbpValuation(
    val points: List<BankStatementNbpPoint>,
    val unpricedCurrencies: Set<String>
)

/** Wycena zapisów bankowych w PLN według ostatniego dostępnego kursu średniego NBP A. */
object BankStatementNbpValuator {
    fun value(
        entries: List<BankStatementEntry>,
        histories: Map<String, List<Pair<LocalDate, BigDecimal>>>
    ): BankStatementNbpValuation {
        if (entries.isEmpty()) return BankStatementNbpValuation(emptyList(), emptySet())
        val missing = linkedSetOf<String>()
        val totals = sortedMapOf<LocalDate, BigDecimal>()
        entries.forEach { entry ->
            val rate = if (entry.currencyCode == "PLN") BigDecimal.ONE else {
                histories[entry.currencyCode]
                    .orEmpty()
                    .filter { it.first <= entry.bookingDate }
                    .maxByOrNull { it.first }
                    ?.second
            }
            if (rate == null) {
                missing += entry.currencyCode
            } else {
                val value = entry.signedAmount.multiply(rate)
                    .setScale(2, RoundingMode.HALF_EVEN)
                totals[entry.bookingDate] = (totals[entry.bookingDate] ?: BigDecimal.ZERO).add(value)
            }
        }
        return BankStatementNbpValuation(
            points = totals.map { BankStatementNbpPoint(it.key, it.value) },
            unpricedCurrencies = missing
        )
    }
}

@Composable
fun BankStatementNbpChartCard(
    entries: List<BankStatementEntry>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember(context) {
        NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache"))
    }
    val scope = rememberCoroutineScope()
    var valuation by remember(entries) { mutableStateOf<BankStatementNbpValuation?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(entries) {
        if (entries.isEmpty()) {
            valuation = null
        } else {
            loading = true
            error = null
            try {
                val result = withContext(Dispatchers.IO) {
                    val currencies = entries.map { it.currencyCode }.filter { it != "PLN" }.distinct()
                    val start = entries.minOf { it.bookingDate }.minusDays(7)
                    val end = entries.maxOf { it.bookingDate }
                    val histories = currencies.associateWith { code ->
                        repository.currencyRateHistory(NbpTable.A, code, start, end)
                    }
                    BankStatementNbpValuator.value(entries, histories)
                }
                valuation = result
                if (result.points.isEmpty()) error = "Nie udało się wycenić żadnego zapisu według kursów NBP."
            } catch (exception: Exception) {
                valuation = null
                error = exception.message ?: "Nie udało się przeliczyć wyciągu według NBP."
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wykres rachunku — wycena zapisów w PLN", style = MaterialTheme.typography.titleMedium)
        Text("Przeliczenie według historycznych kursów średnich NBP z tabeli A, z uwzględnieniem daty księgowania. Oryginalne kwoty i waluty pozostają bez zmian.")
        Button(
            enabled = entries.isNotEmpty() && !loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        val result = withContext(Dispatchers.IO) {
                            val currencies = entries.map { it.currencyCode }.filter { it != "PLN" }.distinct()
                            val start = entries.minOf { it.bookingDate }.minusDays(7)
                            val end = entries.maxOf { it.bookingDate }
                            val histories = currencies.associateWith { code ->
                                repository.currencyRateHistory(NbpTable.A, code, start, end)
                            }
                            BankStatementNbpValuator.value(entries, histories)
                        }
                        valuation = result
                        if (result.points.isEmpty()) error = "Nie udało się wycenić żadnego zapisu według kursów NBP."
                    } catch (exception: Exception) {
                        valuation = null
                        error = exception.message ?: "Nie udało się przeliczyć wyciągu według NBP."
                    } finally {
                        loading = false
                    }
                }
            }
        ) { Text(if (valuation == null) "Przelicz wyciąg i pokaż wykres" else "Odśwież wycenę i wykres") }
        if (loading) CircularProgressIndicator()
        valuation?.let { result ->
            val total = result.points.fold(BigDecimal.ZERO) { sum, point -> sum.add(point.netPln) }
            Text("Suma salda przepływów z zaimportowanych zapisów: ${total.setScale(2, RoundingMode.HALF_EVEN).toPlainString()} PLN")
            Text("Punkty wykresu: ${result.points.size} • ${result.points.firstOrNull()?.date ?: "—"} – ${result.points.lastOrNull()?.date ?: "—"}")
            if (result.points.isNotEmpty()) {
                BankStatementNbpLineChart(result.points, Modifier.fillMaxWidth().height(190.dp))
            }
            if (result.unpricedCurrencies.isNotEmpty()) {
                Text("Brak kursu NBP dla walut: ${result.unpricedCurrencies.sorted().joinToString()}. Te zapisy pominięto w sumie.", color = MaterialTheme.colorScheme.error)
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun BankStatementNbpLineChart(points: List<BankStatementNbpPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val values = points.map { it.netPln.toFloat() }
    val minValue = values.minOrNull() ?: 0f
    val maxValue = values.maxOrNull() ?: 0f
    val span = (maxValue - minValue).takeIf { it > 0f } ?: 1f
    Canvas(modifier = modifier) {
        val left = 8.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = size.height - 10.dp.toPx()
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
