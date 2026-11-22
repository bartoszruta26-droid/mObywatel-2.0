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
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException
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
    val pairRepository = remember { FrankfurterChartRepository() }
    var instruments by remember { mutableStateOf(listOf(
        ChartInstrument("PLN", "złoty polski"), ChartInstrument("EUR", "euro"),
        ChartInstrument("USD", "dolar amerykański"), ChartInstrument("CHF", "frank szwajcarski"),
        ChartInstrument("GBP", "funt brytyjski"), ChartInstrument("XAU", "złoto"),
        ChartInstrument("XAG", "srebro"), ChartInstrument("XPT", "platyna"),
        ChartInstrument("XPD", "pallad")
    )) }
    LaunchedEffect(pairRepository) {
        runCatching { withContext(Dispatchers.IO) { pairRepository.fetchInstruments() } }
            .onSuccess { discovered -> if (discovered.isNotEmpty()) instruments = discovered }
    }
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
                    pairRepository.history(base, quote, start, end)
                }
                require(data.isNotEmpty()) {
                    "Brak wspólnych notowań dla $${base}/$${quote} w wybranym okresie."
                }
                points = data
                source = "Źródło: Frankfurter • dane referencyjne banków centralnych i innych oficjalnych dostawców"
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
        Text("Dane Frankfurter są kursami referencyjnymi, nie notowaniami transakcyjnymi. Interwał zależy od częstotliwości publikacji dostawcy; nie jest generowany sztuczny wolumen.",
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


/**
 * Frankfurter v2: publiczny, otwarty interfejs bez klucza API.
 * Dostarcza waluty oraz wybrane aktywa, w tym metale szlachetne.
 */
private class FrankfurterChartRepository {
    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 20000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "mObywatelska-2.0")
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IOException("Frankfurter API HTTP $status: $body")
            return body
        } finally { connection.disconnect() }
    }

    fun fetchInstruments(): List<ChartInstrument> {
        val array = JSONArray(get("https://api.frankfurter.dev/v2/currencies"))
        val result = (0 until array.length()).mapNotNull { index ->
            val item = array.getJSONObject(index)
            val code = sequenceOf("iso_code", "code", "symbol")
                .map { item.optString(it).trim().uppercase() }
                .firstOrNull { it.matches(Regex("[A-Z0-9]{3,5}")) } ?: return@mapNotNull null
            val name = sequenceOf("name", "description", "title")
                .map { item.optString(it).trim() }
                .firstOrNull { it.isNotBlank() } ?: code
            ChartInstrument(code, name, code in setOf("XAU", "XAG", "XPT", "XPD"))
        }
        return (result + listOf(
            ChartInstrument("PLN", "złoty polski"),
            ChartInstrument("XAU", "złoto", true),
            ChartInstrument("XAG", "srebro", true),
            ChartInstrument("XPT", "platyna", true),
            ChartInstrument("XPD", "pallad", true)
        )).distinctBy { it.code }.sortedBy { it.code }
    }

    fun history(base: String, quote: String, start: LocalDate, end: LocalDate): List<PairPricePoint> {
        require(base != quote) { "Para musi zawierać dwa różne instrumenty." }
        val url = "https://api.frankfurter.dev/v2/rates?from=$start&to=$end&base=$base&quotes=$quote"
        val rows = runCatching { JSONArray(get(url)) }.getOrElse {
            // Some providers publish a metal only as a quote. Try the inverse pair.
            val inverseUrl = "https://api.frankfurter.dev/v2/rates?from=$start&to=$end&base=$quote&quotes=$base"
            val inverse = JSONArray(get(inverseUrl))
            return (0 until inverse.length()).mapNotNull { index ->
                val row = inverse.getJSONObject(index)
                val rate = row.optString("rate").toBigDecimalOrNull() ?: return@mapNotNull null
                if (rate.signum() == 0) null else PairPricePoint(
                    LocalDate.parse(row.getString("date")),
                    BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_EVEN)
                )
            }.sortedBy { it.date }
        }
        return (0 until rows.length()).mapNotNull { index ->
            val row = rows.getJSONObject(index)
            val rate = row.optString("rate").toBigDecimalOrNull() ?: return@mapNotNull null
            PairPricePoint(LocalDate.parse(row.getString("date")), rate)
        }.sortedBy { it.date }
    }
}
