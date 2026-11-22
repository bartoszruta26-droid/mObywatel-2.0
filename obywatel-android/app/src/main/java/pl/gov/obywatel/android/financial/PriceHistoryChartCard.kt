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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ChartInstrument(val code: String, val label: String, val gold: Boolean = false) {
    val title: String get() = "${code} • ${label}"
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
    var source by remember { mutableStateOf("Źródło: Frankfurter • otwarte dane referencyjne") }
    var regressionPanel by remember { mutableStateOf(false) }
    var regression by remember {
        mutableStateOf(RegressionChannelSettings(
            length1 = prefs.getInt("reg_length_1", 377),
            length2 = prefs.getInt("reg_length_2", 233),
            length3 = prefs.getInt("reg_length_3", 144),
            deviationMultiplier = prefs.getFloat("reg_deviation", 2.41f).toDouble(),
            extendLines = prefs.getBoolean("reg_extend", true),
            showFibonacci = prefs.getBoolean("reg_fibonacci", true),
            showBroken = prefs.getBoolean("reg_broken", false),
            width1 = prefs.getFloat("reg_width_1", 1f),
            width2 = prefs.getFloat("reg_width_2", 1f),
            width3 = prefs.getFloat("reg_width_3", 1f)
        ))
    }
    var length1Text by remember { mutableStateOf(regression.length1.toString()) }
    var length2Text by remember { mutableStateOf(regression.length2.toString()) }
    var length3Text by remember { mutableStateOf(regression.length3.toString()) }
    var color1 by remember { mutableIntStateOf(prefs.getInt("reg_color_1", 0)) }
    var color2 by remember { mutableIntStateOf(prefs.getInt("reg_color_2", 1)) }
    var color3 by remember { mutableIntStateOf(prefs.getInt("reg_color_3", 2)) }
    val regressionColors = listOf(Color(0xFF2EAD62), Color(0xFFE4B82E), Color(0xFFE05252),
        Color(0xFF4385D8), Color(0xFF9B59B6), Color(0xFF16A6A6))

    fun save() {
        prefs.edit().putString("base", base).putString("quote", quote)
            .putString("period", period.name).putString("style", style.name).apply()
    }

    fun saveRegression() {
        prefs.edit().putInt("reg_length_1", regression.length1)
            .putInt("reg_length_2", regression.length2).putInt("reg_length_3", regression.length3)
            .putFloat("reg_deviation", regression.deviationMultiplier.toFloat())
            .putBoolean("reg_extend", regression.extendLines)
            .putBoolean("reg_fibonacci", regression.showFibonacci)
            .putBoolean("reg_broken", regression.showBroken)
            .putFloat("reg_width_1", regression.width1).putFloat("reg_width_2", regression.width2)
            .putFloat("reg_width_3", regression.width3)
            .putInt("reg_color_1", color1).putInt("reg_color_2", color2)
            .putInt("reg_color_3", color3).apply()
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
                    "Brak wspólnych notowań dla ${base}/${quote} w wybranym okresie."
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
        OutlinedButton(onClick = { regressionPanel = !regressionPanel }) {
            Text(if (regressionPanel) "Ukryj ustawienia regresji" else "Ustawienia regresji • 3 kanały")
        }
        if (regressionPanel) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kanały regresji liniowej", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        OutlinedTextField(length1Text, { value ->
                            length1Text = value.filter(Char::isDigit).take(4)
                            length1Text.toIntOrNull()?.takeIf { it >= 10 }?.let {
                                regression = regression.copy(length1 = it); saveRegression()
                            }
                        }, label = { Text("Długość 1") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(length2Text, { value ->
                            length2Text = value.filter(Char::isDigit).take(4)
                            length2Text.toIntOrNull()?.takeIf { it >= 10 }?.let {
                                regression = regression.copy(length2 = it); saveRegression()
                            }
                        }, label = { Text("Długość 2") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(length3Text, { value ->
                            length3Text = value.filter(Char::isDigit).take(4)
                            length3Text.toIntOrNull()?.takeIf { it >= 10 }?.let {
                                regression = regression.copy(length3 = it); saveRegression()
                            }
                        }, label = { Text("Długość 3") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Text("Odchylenie: ${"%.2f".format(regression.deviationMultiplier)} × σ")
                    Slider(regression.deviationMultiplier.toFloat(), { regression = regression.copy(deviationMultiplier = it.toDouble()) },
                        valueRange = 0.1f..5f, steps = 48, onValueChangeFinished = ::saveRegression)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(regression.extendLines, { regression = regression.copy(extendLines = it); saveRegression() })
                        Text("Przedłuż kanały w prawo")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(regression.showFibonacci, { regression = regression.copy(showFibonacci = it); saveRegression() })
                        Text("Poziomy Fibonacciego")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(regression.showBroken, { regression = regression.copy(showBroken = it); saveRegression() })
                        Text("Wyróżniaj wybite kanały")
                    }
                    Text("Grubość linii (1–4)")
                    listOf(regression.width1, regression.width2, regression.width3).forEachIndexed { index, width ->
                        Slider(width, { value ->
                            regression = when (index) {
                                0 -> regression.copy(width1 = value)
                                1 -> regression.copy(width2 = value)
                                else -> regression.copy(width3 = value)
                            }
                        }, valueRange = 1f..4f, steps = 2, onValueChangeFinished = ::saveRegression)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(color1, color2, color3).forEachIndexed { index, selectedColor ->
                            OutlinedButton(onClick = {
                                val next = (selectedColor + 1) % regressionColors.size
                                when (index) { 0 -> color1 = next; 1 -> color2 = next; else -> color3 = next }
                                saveRegression()
                            }) { Text("Kolor ${index + 1}", color = regressionColors[selectedColor]) }
                        }
                    }
                    Text("Kanał wymaga pełnej liczby obserwacji. Przy domyślnych długościach potrzeba odpowiednio 377, 233 i 144 punktów.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (loading) CircularProgressIndicator()
        if (points.isNotEmpty()) {
            Text("${base}/${quote}", style = MaterialTheme.typography.titleLarge)
            Text(last?.stripTrailingZeros()?.toPlainString() ?: "—",
                style = MaterialTheme.typography.headlineSmall)
            Text(pct?.let { (if (it.signum() > 0) "+" else "") + it.toPlainString() + "%" } ?: "—")
            if (style == PriceChartStyle.CANDLE) {
                Text("Źródło nie udostępnia rzeczywistych danych OHLC. Pokazujemy kurs referencyjny jako linię, bez sztucznych świec i wolumenu.",
                    color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
            PairHistoryChart(points, if (style == PriceChartStyle.CANDLE) PriceChartStyle.LINE else style,
                selected?.date, { selected = it }, regression,
                regressionColors[color1], regressionColors[color2], regressionColors[color3],
                Modifier.fillMaxWidth().height(275.dp))
            selected?.let { Text("${it.date} • ${it.value.stripTrailingZeros().toPlainString()} ${quote} za 1 ${base}") }
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
            Text("${points.size} punktów • ${points.first().date} – ${points.last().date}",
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
    onSelect: (PairPricePoint) -> Unit, regression: RegressionChannelSettings,
    regressionColor1: Color, regressionColor2: Color, regressionColor3: Color,
    modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val marker = MaterialTheme.colorScheme.tertiary
    val vals = points.map { it.value.toDouble() }
    val channels = LinearRegressionChannels.calculateThree(vals, regression)
    val drawableChannels = channels.filterNotNull()
    val projectedValues = drawableChannels.flatMap { ch ->
        val projectedEnd = if (regression.extendLines)
            ch.endIndex + points.lastIndex * 0.19
        else ch.endIndex.toDouble()
        listOf(ch.lowerAt(ch.startIndex.toDouble(), regression.deviationMultiplier),
            ch.upperAt(ch.startIndex.toDouble(), regression.deviationMultiplier),
            ch.lowerAt(projectedEnd, regression.deviationMultiplier),
            ch.upperAt(projectedEnd, regression.deviationMultiplier))
    }
    val low = (vals + projectedValues).minOrNull() ?: 0.0
    val high = (vals + projectedValues).maxOrNull() ?: low
    val span = (high - low).takeIf { it > 0.0 } ?: 1.0
    Canvas(modifier.pointerInput(points) {
        detectTapGestures { tap ->
            if (points.isNotEmpty()) {
                val l = 8.dp.toPx()
                val dataRight = if (regression.extendLines) size.width * 0.84f else size.width - 8.dp.toPx()
                val ratio = ((tap.x - l) / (dataRight - l)).coerceIn(0f, 1f)
                onSelect(points[(ratio * points.lastIndex).toInt().coerceIn(0, points.lastIndex)])
            }
        }
    }) {
        val l = 8.dp.toPx()
        val dataRight = if (regression.extendLines) size.width * 0.84f else size.width - 8.dp.toPx()
        val r = size.width - 8.dp.toPx()
        val t = 12.dp.toPx(); val b = size.height - 12.dp.toPx()
        fun xFor(index: Double): Float =
            l + (dataRight - l) * (index / maxOf(1, points.lastIndex).toDouble()).toFloat()
        fun yFor(value: Double): Float = b - ((value - low) / span).toFloat() * (b - t)
        repeat(5) { i ->
            val y = t + (b - t) * i / 4f
            drawLine(grid, Offset(l, y), Offset(r, y), 1.dp.toPx())
        }
        val coords = vals.mapIndexed { i, v -> Offset(xFor(i.toDouble()), yFor(v)) }
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
        channels.forEachIndexed { channelIndex, channel ->
            if (channel != null) {
            val colors = listOf(regressionColor1, regressionColor2, regressionColor3)
            val color = if (regression.showBroken && channel.brokenDirection != 0) Color(0xFF2878D0)
                else colors[channelIndex]
            val lineWidth = listOf(regression.width1, regression.width2, regression.width3)[channelIndex].dp.toPx()
            val x1 = xFor(channel.startIndex.toDouble())
            val x2 = if (regression.extendLines) r else xFor(channel.endIndex.toDouble())
            val endIndex = if (regression.extendLines) {
                channel.endIndex + points.lastIndex * 0.19
            } else channel.endIndex.toDouble()
            fun drawRegressionLine(offset: Double, width: Float, pattern: Int) {
                val y1 = yFor(channel.valueAt(channel.startIndex.toDouble()) + offset)
                val y2 = yFor(channel.valueAt(endIndex) + offset)
                if (pattern == 0) drawLine(color, Offset(x1, y1), Offset(x2, y2), width)
                else {
                    val segments = if (pattern == 1) 28 else 14
                    for (segment in 0 until segments step 2) {
                        val a = segment.toFloat() / segments
                        val z = (segment + 1).toFloat() / segments
                        drawLine(color.copy(alpha = 0.82f),
                            Offset(x1 + (x2 - x1) * a, y1 + (y2 - y1) * a),
                            Offset(x1 + (x2 - x1) * z, y1 + (y2 - y1) * z), width)
                    }
                }
            }
            val band = channel.deviation * regression.deviationMultiplier
            drawRegressionLine(0.0, lineWidth, 0)
            drawRegressionLine(band, lineWidth, 1)
            drawRegressionLine(-band, lineWidth, 1)
            if (regression.showFibonacci) {
                LinearRegressionChannels.fibonacciRatios.forEach { ratio ->
                    drawRegressionLine(-band + 2.0 * band * ratio, 1.dp.toPx(), 2)
                }
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
        // Use USD as a common numeraire so currencies and metals can be paired
        // even when a provider does not expose a particular metal as the base.
        val requested = listOf(base, quote).filter { it != "USD" }.distinct()
        val url = buildString {
            append("https://api.frankfurter.dev/v2/rates?from=$start&to=$end&base=USD")
            if (requested.isNotEmpty()) append("&quotes=").append(requested.joinToString(","))
        }
        val rows = JSONArray(get(url))
        val byDate = mutableMapOf<LocalDate, MutableMap<String, BigDecimal>>()
        for (index in 0 until rows.length()) {
            val row = rows.getJSONObject(index)
            val date = LocalDate.parse(row.getString("date"))
            val code = row.optString("quote").uppercase()
            val rate = row.optString("rate").toBigDecimalOrNull() ?: continue
            byDate.getOrPut(date) { mutableMapOf("USD" to BigDecimal.ONE) }[code] = rate
        }
        return byDate.entries.mapNotNull { (date, rates) ->
            val basePerUsd = rates[base] ?: if (base == "USD") BigDecimal.ONE else null
            val quotePerUsd = rates[quote] ?: if (quote == "USD") BigDecimal.ONE else null
            if (basePerUsd == null || quotePerUsd == null || basePerUsd.signum() == 0) null
            else PairPricePoint(date, TradingPairMath.crossRate(basePerUsd, quotePerUsd))
        }.sortedBy { it.date }
    }
}
