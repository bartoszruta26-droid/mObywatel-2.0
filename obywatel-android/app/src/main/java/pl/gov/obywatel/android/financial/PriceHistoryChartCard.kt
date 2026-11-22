// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ChartInstrument(val code: String, val label: String, val gold: Boolean = false) {
    val title: String get() = "$${code} • $${label}"
}
private enum class PriceChartPeriod(val label: String, val days: Long) {
    WEEK("1T", 7), MONTH("1M", 31), QUARTER("3M", 93),
    HALF_YEAR("6M", 183), YEAR("1R", 365), FIVE_YEARS("5R", 1825)
}
private enum class PriceChartStyle(val label: String) { LINE("Linia"), AREA("Obszar"), CANDLE("Świece") }
private data class PairPricePoint(val date: LocalDate, val value: BigDecimal)

@Composable
fun PriceHistoryChartCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("financial_chart_config", 0) }
    val repository = remember(context) {
        NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache"))
    }
    val scope = rememberCoroutineScope()
    var base by remember { mutableStateOf(prefs.getString("base", "EUR") ?: "EUR") }
    var quote by remember { mutableStateOf(prefs.getString("quote", "PLN") ?: "PLN") }
    var period by remember { mutableStateOf(PriceChartPeriod.entries.firstOrNull {
        it.name == prefs.getString("period", "YEAR")
    } ?: PriceChartPeriod.YEAR) }
    var style by remember { mutableStateOf(PriceChartStyle.entries.firstOrNull {
        it.name == prefs.getString("style", "LINE")
    } ?: PriceChartStyle.LINE) }
    var instruments by remember { mutableStateOf(listOf(
        ChartInstrument("PLN", "złoty polski"), ChartInstrument("EUR", "euro"),
        ChartInstrument("USD", "dolar amerykański"), ChartInstrument("CHF", "frank szwajcarski"),
        ChartInstrument("GBP", "funt brytyjski"), ChartInstrument("XAU", "złoto • gram", true)
    )) }
    var points by remember { mutableStateOf<List<PairPricePoint>>(emptyList()) }
    var selected by remember { mutableStateOf<PairPricePoint?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var baseMenu by remember { mutableStateOf(false) }
    var quoteMenu by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("Źródło: NBP • kursy referencyjne") }

    fun save() {
        prefs.edit().putString("base", base).putString("quote", quote)
            .putString("period", period.name).putString("style", style.name).apply()
    }

    fun load() {
        if (base == quote) { error = "Wybierz dwa różne instrumenty."; points = emptyList(); return }
        scope.launch {
            loading = true; error = null; selected = null
            try {
                val end = LocalDate.now()
                val start = end.minusDays(period.days - 1)
                val data = withContext(Dispatchers.IO) {
                    val a = instruments.firstOrNull { it.code == base }
                        ?: throw IllegalArgumentException("Nieznany instrument bazowy.")
                    val b = instruments.firstOrNull { it.code == quote }
                        ?: throw IllegalArgumentException("Nieznany instrument kwotowany.")
                    fun plnSeries(item: ChartInstrument): Map<LocalDate, BigDecimal> = when {
                        item.code == "PLN" -> (0 until period.days.toInt() + 1)
                            .associate { start.plusDays(it.toLong()) to BigDecimal.ONE }
                        item.gold -> repository.goldPriceHistory(start, end)
                            .associate { it.date to it.pricePerGramPln }
                        else -> repository.currencyRateHistory(NbpTable.A, item.code, start, end)
                            .associate { it.first to it.second }
                    }
                    val av = plnSeries(a); val bv = plnSeries(b)
                    av.keys.intersect(bv.keys).sorted().mapNotNull { date ->
                        val ap = av[date] ?: return@mapNotNull null
                        val bp = bv[date] ?: return@mapNotNull null
                        if (bp.signum() == 0) null else PairPricePoint(
                            date, ap.divide(bp, 10, RoundingMode.HALF_EVEN)
                        )
                    }
                }
                require(data.isNotEmpty()) {
                    "Brak wspólnych notowań dla $${base}/$${quote} w wybranym okresie."
                }
                points = data
                source = if (base == "XAU" || quote == "XAU")
                    "Źródło: NBP • złoto PLN/g i waluta PLN • kurs krzyżowy"
                else "Źródło: NBP, tabela A • kursy średnie • kurs krzyżowy"
                save()
            } catch (e: Exception) {
                points = emptyList()
                error = e.message ?: "Nie udało się pobrać notowań."
            } finally { loading = false }
        }
    }

    val min = points.minOfOrNull { it.value }
    val max = points.maxOfOrNull { it.value }
    val first = points.firstOrNull()?.value
    val last = points.lastOrNull()?.value
    val delta = if (first != null && last != null) last.subtract(first) else null
    val pct = if (first != null && last != null && first.signum() != 0)
        last.subtract(first).multiply(BigDecimal("100")).divide(first, 4, RoundingMode.HALF_EVEN)
    else null

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Analiza pary instrumentów", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box {
                OutlinedButton(onClick = { baseMenu = true }) { Text(base) }
                DropdownMenu(baseMenu, onDismissRequest = { baseMenu = false }) {
                    instruments.filter { it.code != quote }.forEach { item ->
                        DropdownMenuItem(text = { Text(item.title) }, onClick = {
                            base = item.code; baseMenu = false; points = emptyList(); save()
                        })
                    }
                }
            }
            Text("/", style = MaterialTheme.typography.titleLarge)
            Box {
                OutlinedButton(onClick = { quoteMenu = true }) { Text(quote) }
                DropdownMenu(quoteMenu, onDismissRequest = { quoteMenu = false }) {
                    instruments.filter { it.code != base }.forEach { item ->
                        DropdownMenuItem(text = { Text(item.title) }, onClick = {
                            quote = item.code; quoteMenu = false; points = emptyList(); save()
                        })
                    }
                }
            }
            Button(onClick = ::load, enabled = !loading) { Text("Pokaż") }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            items(PriceChartPeriod.entries.toList()) { option ->
                if (period == option) Button(onClick = { period = option; save(); load() }) {
                    Text(option.label)
                } else OutlinedButton(onClick = { period = option; save(); load() }) {
                    Text(option.label)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            PriceChartStyle.entries.forEach { option ->
                if (style == option) Button(onClick = { style = option; save() }) {
                    Text(option.label)
                } else OutlinedButton(onClick = { style = option; save() }) {
                    Text(option.label)
                }
            }
        }
        if (loading) CircularProgressIndicator()
        if (points.isNotEmpty()) {
            Text("$${base}/$${quote}", style = MaterialTheme.typography.titleLarge)
            Text(last?.stripTrailingZeros()?.toPlainString() ?: "—",
                style = MaterialTheme.typography.headlineSmall)
            Text(pct?.let { (if (it.signum() > 0) "+" else "") + it.toPlainString() + "%" } ?: "—")
            PairHistoryChart(points, style, selected?.date, { selected = it },
                Modifier.fillMaxWidth().height(235.dp))
            selected?.let { Text("${it.date} • ${it.value.stripTrailingZeros().toPlainString()} $${quote} za 1 $${base}") }
                ?: Text("Dotknij wykresu, aby sprawdzić datę i wartość.")
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Minimum", style = MaterialTheme.typography.labelSmall)
                    Text(min?.stripTrailingZeros()?.toPlainString() ?: "—")
                }
                Column(Modifier.weight(1f)) {
                    Text("Maksimum", style = MaterialTheme.typography.labelSmall)
                    Text(max?.stripTrailingZeros()?.toPlainString() ?: "—")
                }
                Column(Modifier.weight(1f)) {
                    Text("Zmiana", style = MaterialTheme.typography.labelSmall)
                    Text(delta?.let { (if (it.signum() > 0) "+" else "") + it.stripTrailingZeros().toPlainString() } ?: "—")
                }
            }
            Text("$${points.size} punktów • $${points.first().date} – $${points.last().date}",
                style = MaterialTheme.typography.bodySmall)
        } else if (!loading) Text("Wybierz dwa różne instrumenty i naciśnij „Pokaż”.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(source, style = MaterialTheme.typography.bodySmall)
        Text("Kurs pary jest obliczany jako A/PLN ÷ B/PLN. Dane NBP są dziennymi kursami referencyjnymi, nie notowaniami transakcyjnymi ani wolumenem giełdowym.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PairHistoryChart(
    points: List<PairPricePoint>, style: PriceChartStyle, selectedDate: LocalDate?,
    onSelect: (PairPricePoint) -> Unit, modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val marker = MaterialTheme.colorScheme.tertiary
    val vals = points.map { it.value.toFloat() }
    val low = vals.minOrNull() ?: 0f
    val span = ((vals.maxOrNull() ?: low) - low).takeIf { it > 0f } ?: 1f
    Canvas(modifier.pointerInput(points) {
        detectTapGestures { tap ->
            if (points.isNotEmpty()) {
                val l = 8.dp.toPx(); val r = size.width - 8.dp.toPx()
                val ratio = ((tap.x - l) / (r - l)).coerceIn(0f, 1f)
                onSelect(points[(ratio * points.lastIndex).toInt().coerceIn(0, points.lastIndex)])
            }
        }
    }) {
        val l = 8.dp.toPx(); val r = size.width - 8.dp.toPx()
        val t = 12.dp.toPx(); val b = size.height - 12.dp.toPx()
        repeat(5) { i ->
            val y = t + (b - t) * i / 4f
            drawLine(grid, Offset(l, y), Offset(r, y), 1.dp.toPx())
        }
        val coords = vals.mapIndexed { i, v ->
            Offset(l + (r - l) * i / maxOf(1, vals.lastIndex), b - ((v - low) / span) * (b - t))
        }
        if (coords.size > 1) {
            when (style) {
                PriceChartStyle.LINE -> {
                    val p = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(p, line, style = Stroke(2.5.dp.toPx()))
                }
                PriceChartStyle.AREA -> {
                    val area = Path().apply {
                        moveTo(coords.first().x, b); lineTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, b); close()
                    }
                    drawPath(area, line.copy(alpha = 0.18f))
                    val p = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(p, line, style = Stroke(2.5.dp.toPx()))
                }
                PriceChartStyle.CANDLE -> coords.forEach {
                    // NBP supplies one reference price, not OHLC. Show neutral ticks;
                    // do not invent candle ranges or trading volume.
                    drawLine(line, Offset(it.x, it.y - 3.dp.toPx()),
                        Offset(it.x, it.y + 3.dp.toPx()), 1.5.dp.toPx())
                }
            }
        }
        val idx = selectedDate?.let { d -> points.indexOfFirst { it.date == d } } ?: -1
        if (idx >= 0) {
            val p = coords[idx]
            drawLine(marker, Offset(p.x, t), Offset(p.x, b), 1.dp.toPx())
            drawCircle(marker, 5.dp.toPx(), p)
        }
    }
}
