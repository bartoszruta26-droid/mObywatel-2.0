// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PriceChartPoint(val date: LocalDate, val value: BigDecimal, val bid: BigDecimal? = null, val ask: BigDecimal? = null)
private enum class PriceChartInstrument(val label: String) { GOLD("Złoto"), CURRENCY("Waluta") }
private enum class PriceChartPeriod(val label: String, val days: Long) {
    WEEK("7 dni", 7), MONTH("1 mies.", 31), QUARTER("3 mies.", 93),
    HALF_YEAR("6 mies.", 183), YEAR("1 rok", 365), FIVE_YEARS("5 lat", 1825)
}

@Composable
fun PriceHistoryChartCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) { NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache")) }
    val scope = rememberCoroutineScope()
    var instrument by remember { mutableStateOf(PriceChartInstrument.CURRENCY) }
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
                        PriceChartInstrument.GOLD -> repository.goldPriceHistory(start, end).map { PriceChartPoint(it.date, it.pricePerGramPln) }
                        PriceChartInstrument.CURRENCY -> repository.currencyBidAskHistory(currencyCode.trim().uppercase(), start, end).map {
                            val midpoint = it.bidPln.add(it.askPln).divide(BigDecimal("2"), 8, RoundingMode.HALF_EVEN)
                            PriceChartPoint(it.date, midpoint, it.bidPln, it.askPln)
                        }
                    }
                }
                require(loaded.isNotEmpty()) {
                    if (instrument == PriceChartInstrument.CURRENCY) "NBP nie udostępnił historycznych kursów kupna i sprzedaży dla tej waluty w tabeli C."
                    else "NBP nie udostępnił cen złota dla wybranego okresu."
                }
                points = loaded
                loadedLabel = if (instrument == PriceChartInstrument.GOLD) "Cena referencyjna złota • PLN/g"
                    else "Kurs kupna/sprzedaży ${currencyCode.uppercase()} • PLN za 1 jednostkę"
            } catch (exception: Exception) {
                points = emptyList()
                error = exception.message ?: "Nie udało się pobrać historii notowań."
            } finally { loading = false }
        }
    }

    val values = points.map { it.value }
    val indicators = remember(points) { if (values.isEmpty()) null else FinancialChartAnalytics.calculate(values) }
    val minimum = values.minOrNull()
    val maximum = values.maxOrNull()
    val first = values.firstOrNull()
    val last = values.lastOrNull()
    val change = if (first != null && last != null && first.compareTo(BigDecimal.ZERO) != 0)
        last.subtract(first).multiply(BigDecimal("100")).divide(first, 4, RoundingMode.HALF_EVEN) else null
    val latest = points.lastOrNull()
    val spreadReturn = if (latest?.bid != null && latest.ask != null && latest.ask.compareTo(BigDecimal.ZERO) > 0)
        latest.bid.subtract(latest.ask).multiply(BigDecimal("100")).divide(latest.ask, 4, RoundingMode.HALF_EVEN) else null

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wykres analityczny walut i kruszców", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceChartInstrument.entries.forEach { option ->
                Button(onClick = { instrument = option; points = emptyList(); error = null }) {
                    Text(if (instrument == option) "✓ ${option.label}" else option.label)
                }
            }
        }
        if (instrument == PriceChartInstrument.CURRENCY) {
            OutlinedTextField(value = currencyCode, onValueChange = {
                currencyCode = it.filter(Char::isLetter).uppercase().take(3); points = emptyList()
            }, label = { Text("Kod waluty ISO 4217") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Tabela C NBP: kurs kupna (bid) i sprzedaży (ask). To wartości referencyjne NBP, nie kurs konkretnego banku ani kantoru.")
        } else {
            Text("Złoto: cena referencyjna NBP w PLN/g. To źródło nie publikuje osobnych cen kupna/sprzedaży ani notowań srebra.")
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PriceChartPeriod.entries.toList()) { option ->
                Button(onClick = { period = option; points = emptyList(); error = null }) {
                    Text(if (period == option) "✓ ${option.label}" else option.label)
                }
            }
        }
        Button(onClick = ::loadHistory, enabled = !loading && (instrument != PriceChartInstrument.CURRENCY || currencyCode.length == 3)) {
            Text(if (points.isEmpty()) "Pobierz dane i pokaż wykres" else "Odśwież notowania")
        }
        if (loading) CircularProgressIndicator()
        if (points.isNotEmpty() && indicators != null) {
            Text(loadedLabel, style = MaterialTheme.typography.titleSmall)
            PriceHistoryAnalyticalChart(points, indicators, Modifier.fillMaxWidth().height(240.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { Text("Minimum", style = MaterialTheme.typography.labelSmall); Text(minimum?.toPlainString().orEmpty()) }
                Column(Modifier.weight(1f)) { Text("Maksimum", style = MaterialTheme.typography.labelSmall); Text(maximum?.toPlainString().orEmpty()) }
                Column(Modifier.weight(1f)) {
                    Text("Zmiana", style = MaterialTheme.typography.labelSmall)
                    Text(change?.let { "${if (it.signum() > 0) "+" else ""}${it.toPlainString()}%" }.orEmpty())
                }
            }
            if (latest?.bid != null && latest.ask != null) {
                Text("Ostatni kurs kupna: ${latest.bid.toPlainString()} PLN • sprzedaży: ${latest.ask.toPlainString()} PLN")
                Text("Spread: ${latest.ask.subtract(latest.bid).toPlainString()} PLN • relacja bid/ask: ${spreadReturn?.toPlainString()}%")
            } else {
                Text("Zmiana ceny referencyjnej w okresie: ${change?.toPlainString() ?: "—"}%. To zmiana notowania, nie zrealizowany zysk z transakcji.")
            }
            Text("Wskaźniki techniczne", style = MaterialTheme.typography.titleSmall)
            Text("SMA 20: ${indicators.sma20.lastOrNull()?.toPlainString() ?: "za mało danych"} • SMA 50: ${indicators.sma50.lastOrNull()?.toPlainString() ?: "za mało danych"}")
            Text("RSI 14: ${indicators.rsi14.lastOrNull()?.toPlainString() ?: "za mało danych"} • MACD: ${indicators.macd.lastOrNull()?.toPlainString() ?: "za mało danych"} • sygnał: ${indicators.macdSignal.lastOrNull()?.toPlainString() ?: "za mało danych"}")
            Text("Bollinger 20/2: dolne ${indicators.bollingerLower.lastOrNull()?.toPlainString() ?: "—"} • górne ${indicators.bollingerUpper.lastOrNull()?.toPlainString() ?: "—"}")
            Text("Punkty: ${points.size} • ${points.first().date} – ${points.last().date} • ostatnie notowanie: ${latest?.date}")
            Button(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Pine Script v6", FinancialChartAnalytics.pineScriptTemplate()))
            }) { Text("Kopiuj szablon Pine Script v6") }
            Text("Szablon obejmuje SMA, RSI, MACD i wstęgi Bollingera. Dane NBP trzeba wprowadzić do TradingView osobno; aplikacja nie publikuje automatycznie notowań jako symbolu giełdowego.")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Źródło: Narodowy Bank Polski. Weekendy i święta nie są interpolowane. Wskaźniki opisują dane historyczne i nie stanowią rekomendacji inwestycyjnej.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PriceHistoryAnalyticalChart(points: List<PriceChartPoint>, indicators: FinancialChartIndicators, modifier: Modifier = Modifier) {
    val mainColor = MaterialTheme.colorScheme.primary
    val bidColor = MaterialTheme.colorScheme.tertiary
    val askColor = MaterialTheme.colorScheme.error
    val sma20Color = MaterialTheme.colorScheme.secondary
    val sma50Color = MaterialTheme.colorScheme.inversePrimary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val main = points.map { it.value.toFloat() }
    val series = listOf(main, points.map { it.bid?.toFloat() }, points.map { it.ask?.toFloat() },
        indicators.sma20.map { it?.toFloat() }, indicators.sma50.map { it?.toFloat() })
    val all = series.flatten().filterNotNull()
    val minValue = all.minOrNull() ?: 0f
    val maxValue = all.maxOrNull() ?: 1f
    val span = (maxValue - minValue).takeIf { it > 0f } ?: 1f
    Canvas(modifier = modifier) {
        val left = 8.dp.toPx(); val right = size.width - 8.dp.toPx()
        val top = 12.dp.toPx(); val bottom = size.height - 12.dp.toPx()
        repeat(5) { index ->
            val y = top + (bottom - top) * index / 4f
            drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
        }
        fun drawSeries(values: List<Float?>, color: androidx.compose.ui.graphics.Color, width: Float) {
            val path = Path()
            var started = false
            var count = 0
            values.forEachIndexed { index, nullableValue ->
                val value = nullableValue ?: return@forEachIndexed
                val x = left + (right - left) * index / values.lastIndex.coerceAtLeast(1).toFloat()
                val y = bottom - ((value - minValue) / span) * (bottom - top)
                if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
                count++
            }
            if (count == 1) {
                val index = values.indexOfFirst { it != null }
                val value = values[index]!!
                val x = left + (right - left) * index / values.lastIndex.coerceAtLeast(1).toFloat()
                val y = bottom - ((value - minValue) / span) * (bottom - top)
                drawCircle(color, 4.dp.toPx(), Offset(x, y))
            } else if (count > 1) drawPath(path, color, style = Stroke(width))
        }
        drawSeries(points.map { it.value.toFloat() }, mainColor, 2.8.dp.toPx())
        if (points.all { it.bid != null && it.ask != null }) {
            drawSeries(points.map { it.bid?.toFloat() }, bidColor, 1.6.dp.toPx())
            drawSeries(points.map { it.ask?.toFloat() }, askColor, 1.6.dp.toPx())
        }
        drawSeries(indicators.sma20.map { it?.toFloat() }, sma20Color, 1.7.dp.toPx())
        drawSeries(indicators.sma50.map { it?.toFloat() }, sma50Color, 1.7.dp.toPx())
    }
}
