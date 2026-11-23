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
        settings: RegressionChannelSettings
    ): Pair<Double, Double> {
        require(values.isNotEmpty() && values.all { it.isFinite() })
        val candidates = values.toMutableList()
        LinearRegressionChannels.calculateAll(values, settings).filterNotNull().forEach { channel ->
            val band = channel.deviation * settings.deviationMultiplier
            val start = channel.valueAt(channel.startIndex.toDouble())
            val end = channel.valueAt(channel.endIndex.toDouble())
            candidates += listOf(start - band, start + band, end - band, end + band)
        }
        return candidates.minOrNull()!! to candidates.maxOrNull()!!
    }
}
