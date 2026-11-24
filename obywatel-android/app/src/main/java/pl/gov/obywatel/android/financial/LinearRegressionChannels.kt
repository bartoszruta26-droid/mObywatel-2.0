// SPDX-FileCopyrightText: LonesomeTheBlue
// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: MPL-2.0
// Kotlin adaptation of the "Linear Regression Channel" Pine Script by LonesomeTheBlue.
// Original source license: https://mozilla.org/MPL/2.0/
package pl.gov.obywatel.android.financial

import kotlin.math.sqrt

/**
 * Dziesięć niezależnych kanałów regresji, w tym pięć okien dłuższych niż 987 obserwacji.
 */
/**
 * Dziesięć okien regresji: pięć dłuższych od poprzedniego maksimum 987
 * oraz pięć dotychczasowych długich okien. Okno jest aktywne dopiero po
 * zgromadzeniu odpowiedniej liczby obserwacji.
 */
data class RegressionChannelSettings(
    val length1: Int = 10946,
    val length2: Int = 6765,
    val length3: Int = 4181,
    val length4: Int = 2584,
    val length5: Int = 1597,
    val length6: Int = 987,
    val length7: Int = 610,
    val length8: Int = 377,
    val length9: Int = 233,
    val length10: Int = 144,
    val deviationMultiplier: Double = 2.41,
    val showFibonacci: Boolean = true,
    val showBroken: Boolean = false,
    val width1: Float = 1f,
    val width2: Float = 1f,
    val width3: Float = 1f,
    val width4: Float = 1f,
    val width5: Float = 1f,
    val width6: Float = 1f,
    val width7: Float = 1f,
    val width8: Float = 1f,
    val width9: Float = 1f,
    val width10: Float = 1f
) {
    init {
        require(lengths.all { it >= 10 })
        require(deviationMultiplier >= 0.1 && deviationMultiplier.isFinite())
        require(widths.all { it in 1f..4f })
    }

    val lengths: List<Int> get() = listOf(
        length1, length2, length3, length4, length5,
        length6, length7, length8, length9, length10
    )
    val widths: List<Float> get() = listOf(
        width1, width2, width3, width4, width5,
        width6, width7, width8, width9, width10
    )

    fun withLength(index: Int, value: Int): RegressionChannelSettings = when (index) {
        0 -> copy(length1 = value)
        1 -> copy(length2 = value)
        2 -> copy(length3 = value)
        3 -> copy(length4 = value)
        4 -> copy(length5 = value)
        5 -> copy(length6 = value)
        6 -> copy(length7 = value)
        7 -> copy(length8 = value)
        8 -> copy(length9 = value)
        9 -> copy(length10 = value)
        else -> throw IndexOutOfBoundsException("Kanał regresji: $index")
    }

    fun withWidth(index: Int, value: Float): RegressionChannelSettings = when (index) {
        0 -> copy(width1 = value)
        1 -> copy(width2 = value)
        2 -> copy(width3 = value)
        3 -> copy(width4 = value)
        4 -> copy(width5 = value)
        5 -> copy(width6 = value)
        6 -> copy(width7 = value)
        7 -> copy(width8 = value)
        8 -> copy(width9 = value)
        9 -> copy(width10 = value)
        else -> throw IndexOutOfBoundsException("Kanał regresji: $index")
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
    val fibonacciWindowLengths = listOf(10946, 6765, 4181, 2584, 1597, 987, 610, 377, 233, 144)

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

    fun calculateAll(values: List<Double>, settings: RegressionChannelSettings): List<RegressionChannelResult?> =
        settings.lengths.map { calculate(values, it, settings.deviationMultiplier) }

    /** Zachowane dla zgodności ze starszymi wywołaniami i testami. */
    fun calculateThree(values: List<Double>, settings: RegressionChannelSettings) =
        listOf(settings.length3, settings.length4, settings.length5).map {
            calculate(values, it, settings.deviationMultiplier)
        }
}


/**
 * Próg wymiany walut w rachunku płatniczym: ruch musi przekroczyć 2%,
 * aby był zaliczony jako zyskowny; analogiczny ruch przeciwny jest stratą.
 * Przedział [-2%, +2%] pozostaje neutralny i nie podbija prawdopodobieństwa zysku.
 */
object ExchangeProfitThreshold {
    const val MIN_NET_PROFIT_PERCENT = 2.0

    fun classify(changePercent: Double): ForecastDirection {
        require(changePercent.isFinite()) { "Zmiana procentowa musi być skończona." }
        return when {
            changePercent > MIN_NET_PROFIT_PERCENT -> ForecastDirection.UP
            changePercent < -MIN_NET_PROFIT_PERCENT -> ForecastDirection.DOWN
            else -> ForecastDirection.NEUTRAL
        }
    }
}
