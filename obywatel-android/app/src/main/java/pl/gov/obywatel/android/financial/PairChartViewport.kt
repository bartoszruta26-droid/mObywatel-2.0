// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.max
import kotlin.math.min

/** Widoczny zakres indeksów danych i zakresu procentowego wykresu. */
data class PairChartViewport(
    val startFraction: Double = 0.0,
    val endFraction: Double = 1.0,
    val minPercent: Double,
    val maxPercent: Double
) {
    init {
        require(startFraction.isFinite() && endFraction.isFinite())
        require(startFraction >= 0.0 && endFraction <= 3.0 && startFraction < endFraction)
        require(minPercent.isFinite() && maxPercent.isFinite() && minPercent < maxPercent)
    }

    /** Niezależne skalowanie osi X (daty) i Y (procenty), z kotwicą pod palcami. */
    fun transform(
        zoomX: Double = 1.0,
        zoomY: Double = 1.0,
        anchorX: Double = 0.5,
        anchorY: Double = 0.5,
        panX: Double = 0.0,
        panY: Double = 0.0
    ): PairChartViewport {
        val ax = anchorX.coerceIn(0.0, 1.0)
        val ay = anchorY.coerceIn(0.0, 1.0)
        val oldXSpan = endFraction - startFraction
        val newXSpan = (oldXSpan / zoomX.coerceIn(0.05, 20.0)).coerceIn(0.01, 3.0)
        val xAnchor = startFraction + ax * oldXSpan
        val rawStart = xAnchor - ax * newXSpan - panX * newXSpan
        val newStart = rawStart.coerceIn(0.0, (3.0 - newXSpan).coerceAtLeast(0.0))

        val oldYSpan = maxPercent - minPercent
        val newYSpan = (oldYSpan / zoomY.coerceIn(0.05, 20.0)).coerceIn(0.01, 100_000.0)
        val yAnchor = maxPercent - ay * oldYSpan
        val rawMin = yAnchor - (1.0 - ay) * newYSpan + panY * newYSpan
        val newMin = rawMin.coerceIn(-100_000.0, 100_000.0 - newYSpan)

        return copy(
            startFraction = newStart,
            endFraction = newStart + newXSpan,
            minPercent = newMin,
            maxPercent = newMin + newYSpan
        )
    }

    fun reset(
        fullMinPercent: Double,
        fullMaxPercent: Double,
        endFraction: Double = 1.0
    ): PairChartViewport = fit(fullMinPercent, fullMaxPercent, endFraction)

    companion object {
        fun fit(
            minPercent: Double,
            maxPercent: Double,
            endFraction: Double = 1.0
        ): PairChartViewport {
            val low = min(minPercent, maxPercent)
            val high = max(minPercent, maxPercent)
            val rawSpan = (high - low).takeIf { it > 0.0 } ?: 1.0
            val padding = max(rawSpan * 0.08, 0.1)
            return PairChartViewport(
                endFraction = endFraction.coerceIn(0.01, 3.0),
                minPercent = low - padding,
                maxPercent = high + padding
            )
        }
    }
}
