// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.math.RoundingMode

data class FinancialChartIndicators(
    val sma20: List<BigDecimal?>,
    val sma50: List<BigDecimal?>,
    val rsi14: List<BigDecimal?>,
    val macd: List<BigDecimal?>,
    val macdSignal: List<BigDecimal?>,
    val bollingerUpper: List<BigDecimal?>,
    val bollingerLower: List<BigDecimal?>
)

/**
 * Deterministic indicator calculations using close/benchmark observations.
 * These are analytical indicators, not executable trading advice.
 */
object FinancialChartAnalytics {
    private val scale = 8
    private val hundred = BigDecimal("100")

    fun calculate(values: List<BigDecimal>): FinancialChartIndicators {
        require(values.all { it.signum() >= 0 }) { "Notowania nie mogą być ujemne." }
        return FinancialChartIndicators(
            sma20 = sma(values, 20),
            sma50 = sma(values, 50),
            rsi14 = rsi(values, 14),
            macd = macd(values, 12, 26),
            macdSignal = macdSignal(values, 12, 26, 9),
            bollingerUpper = bollinger(values, 20, 2).first,
            bollingerLower = bollinger(values, 20, 2).second
        )
    }

    fun sma(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        require(period > 0)
        return values.indices.map { index ->
            if (index + 1 < period) null else
                values.subList(index + 1 - period, index + 1)
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal(period), scale, RoundingMode.HALF_EVEN)
        }
    }

    fun ema(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        require(period > 0)
        if (values.isEmpty()) return emptyList()
        val multiplier = BigDecimal("2").divide(BigDecimal(period + 1), 12, RoundingMode.HALF_EVEN)
        var current: BigDecimal? = null
        return values.map { value ->
            current = if (current == null) value else
                value.subtract(current!!).multiply(multiplier).add(current!!)
            current!!.setScale(scale, RoundingMode.HALF_EVEN)
        }
    }

    fun rsi(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        require(period > 0)
        if (values.isEmpty()) return emptyList()
        val result = MutableList<BigDecimal?>(values.size) { null }
        if (values.size <= period) return result
        var gains = BigDecimal.ZERO
        var losses = BigDecimal.ZERO
        for (i in 1..period) {
            val delta = values[i].subtract(values[i - 1])
            if (delta.signum() >= 0) gains = gains.add(delta) else losses = losses.add(delta.abs())
        }
        var avgGain = gains.divide(BigDecimal(period), scale, RoundingMode.HALF_EVEN)
        var avgLoss = losses.divide(BigDecimal(period), scale, RoundingMode.HALF_EVEN)
        fun index(): BigDecimal = when {
            avgLoss.compareTo(BigDecimal.ZERO) == 0 && avgGain.compareTo(BigDecimal.ZERO) == 0 -> BigDecimal("50")
            avgLoss.compareTo(BigDecimal.ZERO) == 0 -> hundred
            else -> hundred.subtract(hundred.divide(
                BigDecimal.ONE.add(avgGain.divide(avgLoss, 16, RoundingMode.HALF_EVEN)),
                scale, RoundingMode.HALF_EVEN
            ))
        }
        result[period] = index()
        for (i in period + 1 until values.size) {
            val delta = values[i].subtract(values[i - 1])
            val gain = if (delta.signum() > 0) delta else BigDecimal.ZERO
            val loss = if (delta.signum() < 0) delta.abs() else BigDecimal.ZERO
            avgGain = avgGain.multiply(BigDecimal(period - 1)).add(gain)
                .divide(BigDecimal(period), scale, RoundingMode.HALF_EVEN)
            avgLoss = avgLoss.multiply(BigDecimal(period - 1)).add(loss)
                .divide(BigDecimal(period), scale, RoundingMode.HALF_EVEN)
            result[i] = index()
        }
        return result
    }

    fun macdSignal(values: List<BigDecimal>, fast: Int, slow: Int, signalPeriod: Int): List<BigDecimal?> {
        require(signalPeriod > 0)
        val line = macd(values, fast, slow)
        val firstValid = 0
        if (line.size <= firstValid) return List(line.size) { null }
        val signal = ema(line.drop(firstValid).mapNotNull { it }, signalPeriod)
        return List(line.size) { index ->
            val signalIndex = index - firstValid
            if (signalIndex < signalPeriod - 1 || signalIndex !in signal.indices) null else signal[signalIndex]
        }
    }

    fun macd(values: List<BigDecimal>, fast: Int, slow: Int): List<BigDecimal?> {
        require(fast > 0 && slow > fast)
        val fastEma = ema(values, fast)
        val slowEma = ema(values, slow)
        return values.indices.map { i ->
            if (fastEma[i] == null || slowEma[i] == null) null
            else fastEma[i]!!.subtract(slowEma[i]!!).setScale(scale, RoundingMode.HALF_EVEN)
        }
    }

    fun bollinger(values: List<BigDecimal>, period: Int, deviations: Int): Pair<List<BigDecimal?>, List<BigDecimal?>> {
        require(period > 0 && deviations > 0)
        val upper = MutableList<BigDecimal?>(values.size) { null }
        val lower = MutableList<BigDecimal?>(values.size) { null }
        for (i in period - 1 until values.size) {
            val window = values.subList(i + 1 - period, i + 1)
            val mean = window.fold(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal(period), 16, RoundingMode.HALF_EVEN)
            val variance = window.fold(BigDecimal.ZERO) { sum, value ->
                val delta = value.subtract(mean)
                sum.add(delta.multiply(delta))
            }.divide(BigDecimal(period), 16, RoundingMode.HALF_EVEN)
            val stdDev = kotlin.math.sqrt(variance.toDouble())
            val width = BigDecimal.valueOf(stdDev).multiply(BigDecimal(deviations))
            upper[i] = mean.add(width).setScale(scale, RoundingMode.HALF_EVEN)
            lower[i] = mean.subtract(width).setScale(scale, RoundingMode.HALF_EVEN)
        }
        return upper to lower
    }

    /** Pine Script v6 reference template for charting the same indicator set. */
    fun pineScriptTemplate(): String = """
//@version=6
indicator("mObywatelska — analiza notowań", overlay=true)
src = input.source(close, "Cena referencyjna")
showBands = input.bool(true, "Wstęgi Bollingera")
sma20 = ta.sma(src, 20)
sma50 = ta.sma(src, 50)
rsi14 = ta.rsi(src, 14)
[macdLine, signalLine, histogram] = ta.macd(src, 12, 26, 9)
basis = ta.sma(src, 20)
dev = 2.0 * ta.stdev(src, 20)
upper = basis + dev
lower = basis - dev
plot(sma20, "SMA 20", color=color.orange)
plot(sma50, "SMA 50", color=color.blue)
plot(showBands ? upper : na, "BB upper", color=color.new(color.gray, 25))
plot(showBands ? lower : na, "BB lower", color=color.new(color.gray, 25))
plot(rsi14, "RSI 14", color=color.purple, display=display.pane)
plot(macdLine, "MACD", color=color.teal, display=display.pane)
plot(signalLine, "MACD signal", color=color.orange, display=display.pane)
plot(histogram, "MACD histogram", style=plot.style_columns, color=color.gray, display=display.pane)
hline(70, "RSI 70", color=color.new(color.red, 60), display=display.pane)
hline(30, "RSI 30", color=color.new(color.green, 60), display=display.pane)
""".trimIndent()
}
