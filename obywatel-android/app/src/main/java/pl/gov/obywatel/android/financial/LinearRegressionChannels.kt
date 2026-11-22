// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.sqrt

/** Parametry trzech niezależnych kanałów regresji liniowej. */
data class RegressionChannelSettings(
    val length1: Int = 377,
    val length2: Int = 233,
    val length3: Int = 144,
    val deviationMultiplier: Double = 2.41,
    val extendLines: Boolean = true,
    val showFibonacci: Boolean = true,
    val showBroken: Boolean = false,
    val width1: Float = 1f,
    val width2: Float = 1f,
    val width3: Float = 1f
) {
    init {
        require(length1 >= 10 && length2 >= 10 && length3 >= 10)
        require(deviationMultiplier >= 0.1 && deviationMultiplier.isFinite())
        require(width1 in 1f..4f && width2 in 1f..4f && width3 in 1f..4f)
    }
}

data class RegressionChannelResult(
    val length: Int,
    val startIndex: Int,
    val endIndex: Int,
    val startMid: Double,
    val endMid: Double,
    val deviation: Double,
    val slope: Double,
    val brokenDirection: Int
) {
    fun valueAt(index: Double): Double {
        val fraction = (index - startIndex) / (endIndex - startIndex).coerceAtLeast(1)
        return startMid + (endMid - startMid) * fraction
    }
    fun upperAt(index: Double, multiplier: Double) = valueAt(index) + deviation * multiplier
    fun lowerAt(index: Double, multiplier: Double) = valueAt(index) - deviation * multiplier
    fun fibonacciAt(index: Double, ratio: Double, multiplier: Double) =
        lowerAt(index, multiplier) + deviation * multiplier * 2.0 * ratio
}

object LinearRegressionChannels {
    val fibonacciRatios = listOf(0.236, 0.382, 0.618, 0.786)

    /** OLS na szeregu chronologicznym; odchylenie to RMS reszt regresji. */
    fun calculate(values: List<Double>, length: Int, deviationMultiplier: Double = 2.41): RegressionChannelResult? {
        require(length >= 2)
        require(deviationMultiplier >= 0.0 && deviationMultiplier.isFinite())
        if (values.size < length || values.any { !it.isFinite() }) return null
        val start = values.size - length
        val window = values.subList(start, values.size)
        val n = length.toDouble()
        val meanX = (n - 1.0) / 2.0
        val meanY = window.average()
        var covariance = 0.0
        var varianceX = 0.0
        for (i in window.indices) {
            val dx = i - meanX
            covariance += dx * (window[i] - meanY)
            varianceX += dx * dx
        }
        val slope = if (varianceX == 0.0) 0.0 else covariance / varianceX
        val intercept = meanY - slope * meanX
        val endMid = intercept + slope * (length - 1)
        var squaredResiduals = 0.0
        for (i in window.indices) {
            val residual = window[i] - (intercept + slope * i)
            squaredResiduals += residual * residual
        }
        val deviation = sqrt(squaredResiduals / n)
        val lower = endMid - deviation * deviationMultiplier
        val upper = endMid + deviation * deviationMultiplier
        val last = window.last()
        val broken = when {
            slope > 0.0 && last < lower -> -1
            slope < 0.0 && last > upper -> 1
            else -> 0
        }
        return RegressionChannelResult(length, start, values.lastIndex, intercept, endMid,
            deviation, slope, broken)
    }

    fun calculateThree(values: List<Double>, settings: RegressionChannelSettings) = listOf(
        calculate(values, settings.length1, settings.deviationMultiplier),
        calculate(values, settings.length2, settings.deviationMultiplier),
        calculate(values, settings.length3, settings.deviationMultiplier)
    )
}
