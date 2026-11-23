// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.max
import kotlin.math.min

/** Odcinek regresji przycięty do widocznego zakresu indeksów danych. */
data class RegressionPlotSegment(
    val startIndex: Double,
    val endIndex: Double,
    val startValue: Double,
    val endValue: Double
)

/**
 * Geometria regresji w tym samym układzie współrzędnych co seria procentowa.
 * Każdy punkt X/Y pochodzi z tego samego indeksu; nie mieszamy ekstrapolowanej
 * wartości przyszłej z pozycją ostatniej historycznej obserwacji.
 */
object RegressionPlotGeometry {
    /**
     * Gdy wybrany zakres ma mniej obserwacji niż najkrótsze skonfigurowane okno,
     * użyj jednej regresji adaptacyjnej obejmującej dostępne dane (minimum 10 punktów).
     */
    fun channelsForPlot(
        values: List<Double>,
        settings: RegressionChannelSettings
    ): List<RegressionChannelResult?> {
        val configured = LinearRegressionChannels.calculateAll(values, settings)
        if (configured.any { it != null } || values.size < 10) return configured
        return listOf(LinearRegressionChannels.calculate(values, values.size, settings.deviationMultiplier))
    }

    /** Odcinek prognozy liniowej wychodzący poza ostatnie rzeczywiste notowanie. */
    fun forecastSegment(
        channel: RegressionChannelResult,
        projectionEndIndex: Double,
        visibleStart: Double,
        visibleEnd: Double,
        offset: Double = 0.0
    ): RegressionPlotSegment? {
        require(projectionEndIndex.isFinite() && projectionEndIndex >= channel.endIndex)
        require(visibleStart.isFinite() && visibleEnd.isFinite() && visibleStart <= visibleEnd)
        require(offset.isFinite())
        val start = max(channel.endIndex.toDouble(), visibleStart)
        val end = min(projectionEndIndex, visibleEnd)
        if (start > end || end <= channel.endIndex.toDouble()) return null
        return RegressionPlotSegment(
            startIndex = start,
            endIndex = end,
            startValue = channel.valueAt(start) + offset,
            endValue = channel.valueAt(end) + offset
        )
    }

    fun visibleSegment(
        channel: RegressionChannelResult,
        visibleStart: Double,
        visibleEnd: Double,
        offset: Double = 0.0
    ): RegressionPlotSegment? {
        require(visibleStart.isFinite() && visibleEnd.isFinite() && visibleStart <= visibleEnd)
        require(offset.isFinite())
        val start = max(channel.startIndex.toDouble(), visibleStart)
        val end = min(channel.endIndex.toDouble(), visibleEnd)
        if (start > end) return null
        return RegressionPlotSegment(
            startIndex = start,
            endIndex = end,
            startValue = channel.valueAt(start) + offset,
            endValue = channel.valueAt(end) + offset
        )
    }

    /** Zakres Y obejmujący dane oraz górne i dolne granice aktywnych kanałów. */
    fun fitRange(
        values: List<Double>,
        settings: RegressionChannelSettings,
        projectionEndIndex: Double? = null
    ): Pair<Double, Double> {
        if (values.isEmpty()) return -1.0 to 1.0
        require(values.all { it.isFinite() })
        val candidates = values.toMutableList()
        channelsForPlot(values, settings).filterNotNull().forEach { channel ->
            val band = channel.deviation * settings.deviationMultiplier
            val start = channel.valueAt(channel.startIndex.toDouble())
            val end = channel.valueAt(channel.endIndex.toDouble())
            candidates += listOf(start - band, start + band, end - band, end + band)
            projectionEndIndex?.takeIf { it > channel.endIndex }?.let { projection ->
                val projected = channel.valueAt(projection)
                candidates += listOf(projected - band, projected + band)
            }
        }
        return candidates.minOrNull()!! to candidates.maxOrNull()!!
    }
}
