// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.graphics.Paint as AndroidPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ChartInstrument(val code: String, val label: String, val gold: Boolean = false) {
    val title: String get() = "${code} • ${label}"
}
private enum class PriceChartPeriod(val label: String, val days: Long) {
    WEEK("1T", 7), MONTH("1M", 31), QUARTER("3M", 93),
    HALF_YEAR("6M", 183), YEAR("1R", 365), FIVE_YEARS("5R", 1825),
    TEN_YEARS("10R", 3650), TWENTY_YEARS("20R", 7300), THIRTY_YEARS("30R", 10950)
}
private enum class PriceChartStyle(val label: String) { LINE("Linia"), AREA("Obszar"), CANDLE("Świece") }
internal data class PairPricePoint(val date: LocalDate, val value: BigDecimal)

internal fun pairRelativePerformance(
    points: List<PairPricePoint>,
    anchorIndex: Int = 0
): List<Double> {
    if (points.isEmpty()) return emptyList()
    val anchor = points[anchorIndex.coerceIn(points.indices)].value.toDouble()
    if (!anchor.isFinite() || anchor == 0.0) return emptyList()
    return points.map { point -> (point.value.toDouble() / anchor - 1.0) * 100.0 }
}

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
    // Ryzyko liczymy z długiej historii tej samej pary, niezależnie od zakresu widocznego wykresu.
    var forecastValues by remember { mutableStateOf<List<Double>>(emptyList()) }
    var selected by remember { mutableStateOf<PairPricePoint?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var baseMenu by remember { mutableStateOf(false) }
    var quoteMenu by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("Źródło: Frankfurter • otwarte dane referencyjne") }
    var regressionPanel by remember { mutableStateOf(false) }
    var regression by remember {
        mutableStateOf(RegressionChannelSettings(
            length1 = prefs.getInt("reg_v2_length_1", 10946),
            length2 = prefs.getInt("reg_v2_length_2", 6765),
            length3 = prefs.getInt("reg_v2_length_3", 4181),
            length4 = prefs.getInt("reg_v2_length_4", 2584),
            length5 = prefs.getInt("reg_v2_length_5", 1597),
            length6 = prefs.getInt("reg_v2_length_6", prefs.getInt("reg_fib_length_1", 987)),
            length7 = prefs.getInt("reg_v2_length_7", prefs.getInt("reg_fib_length_2", 610)),
            length8 = prefs.getInt("reg_v2_length_8", prefs.getInt("reg_fib_length_3", 377)),
            length9 = prefs.getInt("reg_v2_length_9", prefs.getInt("reg_fib_length_4", 233)),
            length10 = prefs.getInt("reg_v2_length_10", prefs.getInt("reg_fib_length_5", 144)),
            deviationMultiplier = prefs.getFloat("reg_deviation", 2.41f).toDouble(),
            showFibonacci = prefs.getBoolean("reg_fibonacci", true),
            showBroken = prefs.getBoolean("reg_broken", false),
            width1 = prefs.getFloat("reg_v2_width_1", 1f),
            width2 = prefs.getFloat("reg_v2_width_2", 1f),
            width3 = prefs.getFloat("reg_v2_width_3", 1f),
            width4 = prefs.getFloat("reg_v2_width_4", 1f),
            width5 = prefs.getFloat("reg_v2_width_5", 1f),
            width6 = prefs.getFloat("reg_v2_width_6", prefs.getFloat("reg_fib_width_1", 1f)),
            width7 = prefs.getFloat("reg_v2_width_7", prefs.getFloat("reg_fib_width_2", 1f)),
            width8 = prefs.getFloat("reg_v2_width_8", prefs.getFloat("reg_fib_width_3", 1f)),
            width9 = prefs.getFloat("reg_v2_width_9", prefs.getFloat("reg_fib_width_4", 1f)),
            width10 = prefs.getFloat("reg_v2_width_10", prefs.getFloat("reg_fib_width_5", 1f))
        ))
    }
    val lengthTexts = remember { mutableStateListOf(*regression.lengths.map { it.toString() }.toTypedArray()) }
    val regressionColorIndices = remember {
        mutableStateListOf(*List(10) { index ->
            val legacyIndex = index - 4
            val fallback = if (index < 5) index % 10 else (legacyIndex - 1).mod(7)
            val stored = if (prefs.contains("reg_v2_color_${index + 1}"))
                prefs.getInt("reg_v2_color_${index + 1}", fallback)
            else if (index >= 5) prefs.getInt("reg_fib_color_${legacyIndex}", fallback)
            else fallback
            stored.mod(10)
        }.toTypedArray())
    }
    val regressionColors = listOf(
        Color(0xFF2EAD62), Color(0xFFE4B82E), Color(0xFFE05252),
        Color(0xFF4385D8), Color(0xFF9B59B6), Color(0xFF16A6A6), Color(0xFFE67E22),
        Color(0xFF00A8E8), Color(0xFFD35400), Color(0xFF7F8C8D)
    )

    fun save() {
        prefs.edit().putString("base", base).putString("quote", quote)
            .putString("period", period.name).putString("style", style.name).apply()
    }

    fun saveRegression() {
        val editor = prefs.edit()
            .putFloat("reg_deviation", regression.deviationMultiplier.toFloat())
            .putBoolean("reg_fibonacci", regression.showFibonacci)
            .putBoolean("reg_broken", regression.showBroken)
        regression.lengths.forEachIndexed { index, value ->
            editor.putInt("reg_v2_length_${index + 1}", value)
        }
        regression.widths.forEachIndexed { index, value ->
            editor.putFloat("reg_v2_width_${index + 1}", value)
        }
        regressionColorIndices.forEachIndexed { index, value ->
            editor.putInt("reg_v2_color_${index + 1}", value)
        }
        editor.apply()
    }

    fun load() {
        if (base == quote) { error = "Wybierz dwa różne instrumenty."; points = emptyList(); forecastValues = emptyList(); return }
        scope.launch {
            loading = true; error = null; selected = null
            try {
                val end = LocalDate.now()
                val start = end.minusDays(period.days - 1)
                // Maksymalny dostępny zakres (dostawca może udostępniać krótszą historię)
                // pozwala aktywować długie kanały regresji aż do 10946 obserwacji.
                val fullHistory = withContext(Dispatchers.IO) {
                    try {
                        pairRepository.history(base, quote, end.minusYears(30), end)
                    } catch (_: Exception) {
                        pairRepository.history(base, quote, end.minusYears(20), end)
                    }.ifEmpty {
                        pairRepository.history(base, quote, start, end)
                    }
                }
                val data = fullHistory.filter { !it.date.isBefore(start) }
                require(data.isNotEmpty()) {
                    "Brak wspólnych notowań dla ${base}/${quote} w wybranym okresie."
                }
                points = data
                // Prawdopodobieństwo przeliczamy na dokładnie tym samym zakresie, który wybrał użytkownik.
                forecastValues = data.map { it.value.toDouble() }
                source = "Źródło: Frankfurter • dane referencyjne banków centralnych i innych oficjalnych dostawców"
                save()
            } catch (e: Exception) {
                points = emptyList()
                forecastValues = emptyList()
                error = e.message ?: "Nie udało się pobrać notowań."
            } finally { loading = false }
        }
    }

    val first = points.firstOrNull()?.value
    val last = points.lastOrNull()?.value
    val delta = if (first != null && last != null) last.subtract(first) else null
    // Wybrany punkt staje się kotwicą 0%; domyślnie kotwicą jest pierwszy punkt.
    val referenceIndex = selected?.date?.let { date ->
        points.indexOfFirst { it.date == date }.takeIf { it >= 0 }
    } ?: 0
    val relativeValues = remember(points, referenceIndex) {
        pairRelativePerformance(points, referenceIndex)
    }
    val pct = relativeValues.lastOrNull()?.let {
        BigDecimal.valueOf(it).setScale(4, RoundingMode.HALF_EVEN)
    }
    val projectionBars = if (points.size > 1)
        minOf(maxOf(3, kotlin.math.ceil(points.size * 0.20).toInt()), points.lastIndex * 2) else 0
    val projectionEndIndex = (points.lastIndex + projectionBars).coerceAtLeast(0)
    val projectionEndFraction = if (points.size > 1)
        projectionEndIndex.toDouble() / points.lastIndex.toDouble() else 1.0
    val minPercent = relativeValues.minOrNull() ?: -1.0
    val maxPercent = relativeValues.maxOrNull() ?: 1.0
    val regressionBounds = remember(relativeValues, regression, projectionEndIndex) {
        RegressionPlotGeometry.fitRange(
            relativeValues, regression,
            projectionEndIndex.takeIf { it > points.lastIndex }?.toDouble()
        )
    }
    var viewport by remember(points, regression, referenceIndex) {
        mutableStateOf(
            PairChartViewport.fit(
                regressionBounds.first, regressionBounds.second, projectionEndFraction
            )
        )
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Analiza pary instrumentów", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box {
                OutlinedButton(onClick = { baseMenu = true }) { Text(base) }
                DropdownMenu(baseMenu, onDismissRequest = { baseMenu = false }) {
                    instruments.filter { it.code != quote }.forEach { item ->
                        DropdownMenuItem(text = { Text(item.title) }, onClick = {
                            base = item.code; baseMenu = false; points = emptyList(); forecastValues = emptyList(); save()
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
                            quote = item.code; quoteMenu = false; points = emptyList(); forecastValues = emptyList(); save()
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
            Text(if (regressionPanel) "Ukryj ustawienia regresji" else "Ustawienia regresji • 10 kanałów")
        }
        if (regressionPanel) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kanały regresji liniowej", style = MaterialTheme.typography.titleSmall)
                    Text("Okna regresji od najdłuższego do najkrótszego: 10946, 6765, 4181, 2584, 1597, 987, 610, 377, 233, 144. Najkrótsze 89 i 55 zostały usunięte.")
                    lengthTexts.indices.toList().chunked(2).forEach { rowIndices ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowIndices.forEach { index ->
                                OutlinedTextField(
                                    value = lengthTexts[index],
                                    onValueChange = { value ->
                                        val filtered = value.filter(Char::isDigit).take(5)
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
                    Text("Kanał pojawia się po zebraniu pełnej liczby obserwacji. Najdłuższe okno 10946 wymaga 10946 punktów; rzeczywista dostępność zależy od historii dostawcy.",
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
            Text("Oś X: daty • oś Y: zmiana procentowa względem wybranego punktu (0%). Dotknij innego notowania, aby przenieść punkt odniesienia. Linia ciągła pokazuje regresję historyczną, a przerywana — jej projekcję poza ostatnie notowanie.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("●", color = MaterialTheme.colorScheme.primary)
                Text("$base/$quote • zmiana relacji (%)", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    viewport = viewport.reset(
                        regressionBounds.first, regressionBounds.second, projectionEndFraction
                    )
                }) {
                    Text("Resetuj widok")
                }
            }
            PairHistoryChart(
                points = points,
                relativeValues = relativeValues,
                style = if (style == PriceChartStyle.CANDLE) PriceChartStyle.LINE else style,
                selectedDate = selected?.date,
                onSelect = { selected = it },
                regression = regression,
                regressionColors = regressionColorIndices.map { regressionColors[it] },
                viewport = viewport,
                projectionEndIndex = projectionEndIndex,
                onViewportChange = { viewport = it },
                modifier = Modifier.fillMaxWidth().height(340.dp)
            )
            Text("Dotknięcie punktu ustawia go jako 0%. Przeciągnij poza punktami, aby przesunąć wykres; dwa palce służą do niezależnego skalowania osi dat i procentów.",
                style = MaterialTheme.typography.bodySmall)
            val activeRegressionLengths = RegressionPlotGeometry.channelsForPlot(
                relativeValues, regression
            ).mapNotNull { it?.length }
            Text(
                if (activeRegressionLengths.isEmpty()) "Brak aktywnych kanałów: wybierz dłuższy okres lub krótsze długości regresji."
                else "Aktywne kanały regresji: ${activeRegressionLengths.joinToString(" / ")}",
                style = MaterialTheme.typography.bodySmall
            )
            selected?.let {
                Text("Punkt odniesienia 0%: ${it.date} • ${it.value.stripTrailingZeros().toPlainString()} ${quote} za 1 ${base}")
            } ?: Text("Dotknij wykresu, aby ustawić punkt odniesienia 0%.")
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Minimum", style = MaterialTheme.typography.labelSmall)
                    Text(minPercent.let { "%.2f%%".format(it) })
                }
                Column(Modifier.weight(1f)) {
                    Text("Maksimum", style = MaterialTheme.typography.labelSmall)
                    Text(maxPercent.let { "%.2f%%".format(it) })
                }
                Column(Modifier.weight(1f)) {
                    Text("Zmiana", style = MaterialTheme.typography.labelSmall)
                    Text(pct?.let { (if (it.signum() > 0) "+" else "") + it.toPlainString() + "%" } ?: "—")
                }
            }
            Text("${points.size} punktów • ${points.first().date} – ${points.last().date}",
                style = MaterialTheme.typography.bodySmall)
            SelectedPairForecastCard(base, quote, forecastValues, regression, Modifier.fillMaxWidth())
        } else if (!loading) Text("Wybierz dwa różne instrumenty i naciśnij „Pokaż”.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(source, style = MaterialTheme.typography.bodySmall)
        Text("Dane Frankfurter są kursami referencyjnymi, nie notowaniami transakcyjnymi. Interwał zależy od częstotliwości publikacji dostawcy; nie jest generowany sztuczny wolumen.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PairHistoryChart(
    points: List<PairPricePoint>,
    relativeValues: List<Double>,
    style: PriceChartStyle,
    selectedDate: LocalDate?,
    onSelect: (PairPricePoint) -> Unit,
    regression: RegressionChannelSettings,
    regressionColors: List<Color>,
    viewport: PairChartViewport,
    projectionEndIndex: Int,
    onViewportChange: (PairChartViewport) -> Unit,
    modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val marker = MaterialTheme.colorScheme.tertiary
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val channels = RegressionPlotGeometry.channelsForPlot(relativeValues, regression)
    val plotDateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yy", Locale.getDefault()) }
    val latestViewport by rememberUpdatedState(viewport)
    val latestViewportCallback by rememberUpdatedState(onViewportChange)
    val latestSelectionCallback by rememberUpdatedState(onSelect)

    Canvas(
        modifier
            .pointerInput(points) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragging = false
                    var accumulated = Offset.Zero
                    val plotLeft = 48.dp.toPx()
                    val plotRight = size.width - 10.dp.toPx()
                    val plotTop = 10.dp.toPx()
                    val plotBottom = size.height - 30.dp.toPx()
                    val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
                    val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
                    do {
                        val event = awaitPointerEvent()
                        val active = event.changes.filter { it.pressed }
                        when {
                            active.size >= 2 -> {
                                dragging = true
                                val first = active[0]
                                val second = active[1]
                                val oldX = kotlin.math.abs(first.previousPosition.x - second.previousPosition.x)
                                val newX = kotlin.math.abs(first.position.x - second.position.x)
                                val oldY = kotlin.math.abs(first.previousPosition.y - second.previousPosition.y)
                                val newY = kotlin.math.abs(first.position.y - second.position.y)
                                val oldCenterX = (first.previousPosition.x + second.previousPosition.x) / 2f
                                val oldCenterY = (first.previousPosition.y + second.previousPosition.y) / 2f
                                val centerX = (first.position.x + second.position.x) / 2f
                                val centerY = (first.position.y + second.position.y) / 2f
                                latestViewportCallback(
                                    latestViewport.transform(
                                        zoomX = if (oldX > 2f) (newX / oldX).toDouble() else 1.0,
                                        zoomY = if (oldY > 2f) (newY / oldY).toDouble() else 1.0,
                                        anchorX = ((centerX - plotLeft) / plotWidth).toDouble(),
                                        anchorY = ((centerY - plotTop) / plotHeight).toDouble(),
                                        panX = ((centerX - oldCenterX) / plotWidth).toDouble(),
                                        panY = ((centerY - oldCenterY) / plotHeight).toDouble()
                                    )
                                )
                                active.forEach { it.consume() }
                            }
                            active.size == 1 -> {
                                val change = active.first()
                                val delta = change.position - change.previousPosition
                                accumulated += delta
                                if (kotlin.math.hypot(
                                        accumulated.x.toDouble(), accumulated.y.toDouble()
                                    ) > viewConfiguration.touchSlop
                                ) dragging = true
                                if (dragging && (delta.x != 0f || delta.y != 0f)) {
                                    latestViewportCallback(
                                        latestViewport.transform(
                                            panX = delta.x.toDouble() / plotWidth,
                                            panY = delta.y.toDouble() / plotHeight
                                        )
                                    )
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (!dragging && points.isNotEmpty()) {
                        val ratio = ((down.position.x - plotLeft) / plotWidth).coerceIn(0f, 1f)
                        val indexFraction = latestViewport.startFraction +
                            ratio * (latestViewport.endFraction - latestViewport.startFraction)
                        val index = (indexFraction * points.lastIndex).toInt()
                            .coerceIn(0, points.lastIndex)
                        latestSelectionCallback(points[index])
                    }
                }
            }
    ) {
        if (points.isEmpty() || relativeValues.size != points.size) return@Canvas
        val left = 48.dp.toPx()
        val right = size.width - 10.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = size.height - 30.dp.toPx()
        val xSpan = (viewport.endFraction - viewport.startFraction).coerceAtLeast(0.0001)
        val firstIndex = viewport.startFraction * points.lastIndex
        val lastIndex = viewport.endFraction * points.lastIndex
        val ySpan = (viewport.maxPercent - viewport.minPercent).coerceAtLeast(0.0001)
        fun xFor(index: Double): Float =
            left + ((index - firstIndex) / (lastIndex - firstIndex)).toFloat() * (right - left)
        fun yFor(value: Double): Float =
            bottom - ((value - viewport.minPercent) / ySpan).toFloat() * (bottom - top)

        val native = drawContext.canvas.nativeCanvas
        val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
            textSize = 10.dp.toPx()
            textAlign = AndroidPaint.Align.RIGHT
        }
        val datePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
            textSize = 9.dp.toPx()
            textAlign = AndroidPaint.Align.CENTER
        }

        repeat(5) { tick ->
            val fraction = tick / 4.0
            val y = top + (bottom - top) * fraction.toFloat()
            val value = viewport.maxPercent - ySpan * fraction
            drawLine(grid, Offset(left, y), Offset(right, y), 1.dp.toPx())
            native.drawText(String.format(Locale.getDefault(), "%.1f%%", value),
                left - 5.dp.toPx(), y + 3.dp.toPx(), textPaint)
        }
        repeat(5) { tick ->
            val fraction = tick / 4.0
            val index = firstIndex + (lastIndex - firstIndex) * fraction
            val x = xFor(index)
            drawLine(grid.copy(alpha = 0.45f), Offset(x, top), Offset(x, bottom), 1.dp.toPx())
            val date = if (index <= points.lastIndex) {
                points[index.toInt().coerceIn(0, points.lastIndex)].date
            } else {
                val lastDate = points.last().date
                val intervalDays = if (points.size > 1)
                    ChronoUnit.DAYS.between(points[points.lastIndex - 1].date, lastDate).coerceAtLeast(1)
                else 1L
                lastDate.plusDays(kotlin.math.round((index - points.lastIndex) * intervalDays).toLong())
            }
            native.drawText(date.format(plotDateFormatter), x, size.height - 7.dp.toPx(), datePaint)
        }

        val visibleStart = firstIndex.toInt().coerceIn(0, points.lastIndex)
        val visibleEnd = kotlin.math.ceil(lastIndex).toInt().coerceIn(visibleStart, points.lastIndex)
        val coords = (visibleStart..visibleEnd).map { index ->
            Offset(xFor(index.toDouble()), yFor(relativeValues[index]))
        }
        if (coords.size > 1) {
            when (style) {
                PriceChartStyle.LINE -> {
                    val path = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, line, style = Stroke(2.5.dp.toPx()))
                }
                PriceChartStyle.AREA -> {
                    val area = Path().apply {
                        moveTo(coords.first().x, bottom)
                        lineTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, bottom)
                        close()
                    }
                    drawPath(area, line.copy(alpha = 0.18f))
                    val path = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, line, style = Stroke(2.5.dp.toPx()))
                }
                PriceChartStyle.CANDLE -> coords.forEach { point ->
                    drawLine(line, Offset(point.x, point.y - 3.dp.toPx()),
                        Offset(point.x, point.y + 3.dp.toPx()), 1.5.dp.toPx())
                }
            }
        }

        channels.forEachIndexed { channelIndex, channel ->
            if (channel != null) {
                val color = if (regression.showBroken && channel.brokenDirection != 0)
                    Color(0xFF2878D0) else regressionColors[channelIndex]
                val width = regression.widths[channelIndex].dp.toPx()
                fun drawDashedSegment(segment: RegressionPlotSegment, strokeWidth: Float, alpha: Float) {
                    val x1 = xFor(segment.startIndex)
                    val x2 = xFor(segment.endIndex)
                    val y1 = yFor(segment.startValue)
                    val y2 = yFor(segment.endValue)
                    val dashCount = 32
                    for (dash in 0 until dashCount step 2) {
                        val a = dash.toFloat() / dashCount
                        val z = (dash + 1).toFloat() / dashCount
                        drawLine(
                            color.copy(alpha = alpha),
                            Offset(x1 + (x2 - x1) * a, y1 + (y2 - y1) * a),
                            Offset(x1 + (x2 - x1) * z, y1 + (y2 - y1) * z),
                            strokeWidth
                        )
                    }
                }
                fun regressionLine(offset: Double, strokeWidth: Float, dashed: Boolean) {
                    val segment = RegressionPlotGeometry.visibleSegment(
                        channel, firstIndex, lastIndex, offset
                    ) ?: return
                    if (dashed) drawDashedSegment(segment, strokeWidth, 0.72f)
                    else drawLine(
                        color,
                        Offset(xFor(segment.startIndex), yFor(segment.startValue)),
                        Offset(xFor(segment.endIndex), yFor(segment.endValue)),
                        strokeWidth
                    )
                }
                fun forecastLine(offset: Double, strokeWidth: Float, alpha: Float) {
                    val segment = RegressionPlotGeometry.forecastSegment(
                        channel, projectionEndIndex.toDouble(), firstIndex, lastIndex, offset
                    ) ?: return
                    drawDashedSegment(segment, strokeWidth, alpha)
                }
                val band = channel.deviation * regression.deviationMultiplier
                regressionLine(0.0, width, false)
                regressionLine(band, width, true)
                regressionLine(-band, width, true)
                forecastLine(0.0, (width * 1.35f).coerceAtLeast(2.dp.toPx()), 1.0f)
                forecastLine(band, width, 0.58f)
                forecastLine(-band, width, 0.58f)
                if (regression.showFibonacci) {
                    LinearRegressionChannels.fibonacciRatios.forEach { ratio ->
                        val offset = -band + 2.0 * band * ratio
                        regressionLine(offset, 1.dp.toPx(), true)
                        forecastLine(offset, 1.dp.toPx(), 0.42f)
                    }
                }
            }
        }

        // Pozioma linia 0% wskazuje poziom ceny z wybranego punktu odniesienia.
        if (0.0 in viewport.minPercent..viewport.maxPercent) {
            val zeroY = yFor(0.0)
            drawLine(marker.copy(alpha = 0.72f), Offset(left, zeroY), Offset(right, zeroY), 1.5.dp.toPx())
        }

        val selectedIndex = selectedDate?.let { date -> points.indexOfFirst { it.date == date } } ?: -1
        if (selectedIndex >= 0 && selectedIndex.toDouble() in firstIndex..lastIndex) {
            val point = Offset(xFor(selectedIndex.toDouble()), yFor(relativeValues[selectedIndex]))
            drawLine(marker, Offset(point.x, top), Offset(point.x, bottom), 1.dp.toPx())
            drawCircle(marker, 5.dp.toPx(), point)
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
