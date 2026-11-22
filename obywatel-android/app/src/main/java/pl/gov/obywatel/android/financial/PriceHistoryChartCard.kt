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
            length1 = prefs.getInt("reg_fib_length_1", 987),
            length2 = prefs.getInt("reg_fib_length_2", 610),
            length3 = prefs.getInt("reg_fib_length_3", prefs.getInt("reg_length_1", 377)),
            length4 = prefs.getInt("reg_fib_length_4", prefs.getInt("reg_length_2", 233)),
            length5 = prefs.getInt("reg_fib_length_5", prefs.getInt("reg_length_3", 144)),
            length6 = prefs.getInt("reg_fib_length_6", 89),
            length7 = prefs.getInt("reg_fib_length_7", 55),
            deviationMultiplier = prefs.getFloat("reg_deviation", 2.41f).toDouble(),
            extendLines = prefs.getBoolean("reg_extend", true),
            showFibonacci = prefs.getBoolean("reg_fibonacci", true),
            showBroken = prefs.getBoolean("reg_broken", false),
            width1 = prefs.getFloat("reg_fib_width_1", 1f),
            width2 = prefs.getFloat("reg_fib_width_2", 1f),
            width3 = prefs.getFloat("reg_fib_width_3", 1f),
            width4 = prefs.getFloat("reg_fib_width_4", 1f),
            width5 = prefs.getFloat("reg_fib_width_5", 1f),
            width6 = prefs.getFloat("reg_fib_width_6", 1f),
            width7 = prefs.getFloat("reg_fib_width_7", 1f)
        ))
    }
    val lengthTexts = remember { mutableStateListOf(*regression.lengths.map { it.toString() }.toTypedArray()) }
    val regressionColorIndices = remember {
        mutableStateListOf(*List(7) { index -> (prefs.getInt("reg_fib_color_${index + 1}", index % 7)).mod(7) }.toTypedArray())
    }
    val regressionColors = listOf(
        Color(0xFF2EAD62), Color(0xFFE4B82E), Color(0xFFE05252),
        Color(0xFF4385D8), Color(0xFF9B59B6), Color(0xFF16A6A6), Color(0xFFE67E22)
    )

    fun save() {
        prefs.edit().putString("base", base).putString("quote", quote)
            .putString("period", period.name).putString("style", style.name).apply()
    }

    fun saveRegression() {
        val editor = prefs.edit()
            .putFloat("reg_deviation", regression.deviationMultiplier.toFloat())
            .putBoolean("reg_extend", regression.extendLines)
            .putBoolean("reg_fibonacci", regression.showFibonacci)
            .putBoolean("reg_broken", regression.showBroken)
        regression.lengths.forEachIndexed { index, value ->
            editor.putInt("reg_fib_length_${index + 1}", value)
        }
        regression.widths.forEachIndexed { index, value ->
            editor.putFloat("reg_fib_width_${index + 1}", value)
        }
        regressionColorIndices.forEachIndexed { index, value ->
            editor.putInt("reg_fib_color_${index + 1}", value)
        }
        editor.apply()
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
                    Text("Okna regresji według ciągu Fibonacciego (od najdłuższego do najkrótszego): 987, 610, 377, 233, 144, 89, 55")
                    lengthTexts.indices.toList().chunked(2).forEach { rowIndices ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowIndices.forEach { index ->
                                OutlinedTextField(
                                    value = lengthTexts[index],
                                    onValueChange = { value ->
                                        val filtered = value.filter(Char::isDigit).take(4)
                                        lengthTexts[index] = filtered
                                        filtered.toIntOrNull()?.takeIf { it >= 10 }?.let { length ->
                                            regression = regression.withLength(index, length)
                                            saveRegression()
                                        }
                                    },
                                    label = { Text("Kanał ${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            if (rowIndices.size == 1) Spacer(Modifier.weight(1f))
                        }
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
                    regression.widths.forEachIndexed { index, width ->
                        Text("Kanał ${index + 1}: ${width.toInt()} px", style = MaterialTheme.typography.labelSmall)
                        Slider(width, { value ->
                            regression = regression.withWidth(index, value)
                        }, valueRange = 1f..4f, steps = 2, onValueChangeFinished = ::saveRegression)
                    }
                    Text("Kolory kanałów (dotknij, aby zmienić)")
                    regressionColorIndices.indices.toList().chunked(4).forEach { rowIndices ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowIndices.forEach { index ->
                                OutlinedButton(onClick = {
                                    regressionColorIndices[index] = (regressionColorIndices[index] + 1) % regressionColors.size
                                    saveRegression()
                                }) {
                                    Text("K${index + 1}", color = regressionColors[regressionColorIndices[index]])
                                }
                            }
                        }
                    }
                    Text("Kanał pojawia się po zebraniu pełnej liczby obserwacji. Dla okna 987 potrzeba co najmniej 987 punktów.",
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
                regressionColorIndices.map { regressionColors[it] },
                Modifier.fillMaxWidth().height(300.dp))
            val activeRegressionLengths = LinearRegressionChannels.calculateAll(
                points.map { it.value.toDouble() }, regression
            ).mapIndexedNotNull { index, channel ->
                channel?.let { regression.lengths[index] }
            }
            Text(
                if (activeRegressionLengths.isEmpty()) "Brak aktywnych kanałów: wybierz dłuższy okres lub krótsze długości regresji."
                else "Aktywne kanały regresji: ${activeRegressionLengths.joinToString(" / ")}",
                style = MaterialTheme.typography.bodySmall
            )
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
    regressionColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val marker = MaterialTheme.colorScheme.tertiary
    val vals = points.map { it.value.toDouble() }
    val channels = LinearRegressionChannels.calculateAll(vals, regression)
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
            val color = if (regression.showBroken && channel.brokenDirection != 0) Color(0xFF2878D0)
                else regressionColors[channelIndex]
            val lineWidth = regression.widths[channelIndex].dp.toPx()
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
