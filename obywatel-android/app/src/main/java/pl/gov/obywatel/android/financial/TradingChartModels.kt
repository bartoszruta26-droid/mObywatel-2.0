// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

enum class PriceBarKind { REFERENCE, EXECUTED, BID_ASK, AGGREGATED }

data class TradingPriceBar(
    val date: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: BigDecimal? = null,
    val kind: PriceBarKind = PriceBarKind.REFERENCE
) {
    init {
        require(low <= minOf(open, close)) { "Minimum świecy nie może przekraczać otwarcia ani zamknięcia." }
        require(high >= maxOf(open, close)) { "Maksimum świecy nie może być niższe od otwarcia ani zamknięcia." }
        require(volume == null || volume.signum() >= 0) { "Wolumen nie może być ujemny." }
    }

    companion object {
        fun reference(date: LocalDate, value: BigDecimal) =
            TradingPriceBar(date, value, value, value, value, kind = PriceBarKind.REFERENCE)
    }
}

enum class TradingChartPeriod(val label: String, val calendarDays: Long) {
    DAY_7("1T", 7), MONTH_1("1M", 31), MONTH_3("3M", 93),
    MONTH_6("6M", 183), YEAR_1("1R", 365), YEAR_5("5R", 1825)
}

enum class TradingChartStyle { LINE, CANDLE, AREA }

data class TradingChartStatistics(
    val first: BigDecimal?, val last: BigDecimal?, val minimum: BigDecimal?,
    val maximum: BigDecimal?, val absoluteChange: BigDecimal?, val percentChange: BigDecimal?
)

object TradingChartAnalytics {
    fun statistics(bars: List<TradingPriceBar>): TradingChartStatistics {
        if (bars.isEmpty()) return TradingChartStatistics(null, null, null, null, null, null)
        val ordered = bars.sortedBy { it.date }
        val first = ordered.first().close
        val last = ordered.last().close
        val change = last.subtract(first)
        val percent = if (first.compareTo(BigDecimal.ZERO) == 0) null else
            change.multiply(BigDecimal("100")).divide(first, 4, RoundingMode.HALF_EVEN)
        return TradingChartStatistics(first, last, ordered.minOf { it.low }, ordered.maxOf { it.high }, change, percent)
    }

    fun simpleMovingAverage(bars: List<TradingPriceBar>, length: Int): List<BigDecimal?> {
        require(length > 0) { "Długość średniej musi być dodatnia." }
        val ordered = bars.sortedBy { it.date }
        return ordered.indices.map { index ->
            if (index + 1 < length) null else {
                val sum = (index - length + 1..index).map { ordered[it].close }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                sum.divide(BigDecimal(length), 8, RoundingMode.HALF_EVEN)
            }
        }
    }

    fun exponentialMovingAverage(bars: List<TradingPriceBar>, length: Int): List<BigDecimal?> {
        require(length > 0) { "Długość średniej musi być dodatnia." }
        val ordered = bars.sortedBy { it.date }
        if (ordered.isEmpty()) return emptyList()
        val alpha = BigDecimal("2").divide(BigDecimal(length + 1), 12, RoundingMode.HALF_EVEN)
        var ema: BigDecimal? = null
        return ordered.map { bar ->
            ema = if (ema == null) bar.close else
                bar.close.multiply(alpha).add(ema!!.multiply(BigDecimal.ONE.subtract(alpha)))
                    .setScale(8, RoundingMode.HALF_EVEN)
            ema
        }
    }

    fun relativeStrengthIndex(bars: List<TradingPriceBar>, length: Int = 14): List<BigDecimal?> {
        require(length > 0) { "Długość RSI musi być dodatnia." }
        val ordered = bars.sortedBy { it.date }
        val result = MutableList<BigDecimal?>(ordered.size) { null }
        if (ordered.size <= length) return result
        var averageGain = BigDecimal.ZERO
        var averageLoss = BigDecimal.ZERO
        for (i in 1..length) {
            val delta = ordered[i].close.subtract(ordered[i - 1].close)
            if (delta.signum() > 0) averageGain = averageGain.add(delta)
            else averageLoss = averageLoss.add(delta.abs())
        }
        averageGain = averageGain.divide(BigDecimal(length), 10, RoundingMode.HALF_EVEN)
        averageLoss = averageLoss.divide(BigDecimal(length), 10, RoundingMode.HALF_EVEN)
        fun rsi(gain: BigDecimal, loss: BigDecimal): BigDecimal = when {
            loss.signum() == 0 && gain.signum() == 0 -> BigDecimal("50")
            loss.signum() == 0 -> BigDecimal("100")
            gain.signum() == 0 -> BigDecimal.ZERO
            else -> BigDecimal("100").subtract(
                BigDecimal("100").divide(BigDecimal.ONE.add(gain.divide(loss, 10, RoundingMode.HALF_EVEN)),
                    8, RoundingMode.HALF_EVEN)
            )
        }
        result[length] = rsi(averageGain, averageLoss)
        for (i in length + 1 until ordered.size) {
            val delta = ordered[i].close.subtract(ordered[i - 1].close)
            val gain = if (delta.signum() > 0) delta else BigDecimal.ZERO
            val loss = if (delta.signum() < 0) delta.abs() else BigDecimal.ZERO
            averageGain = averageGain.multiply(BigDecimal(length - 1)).add(gain)
                .divide(BigDecimal(length), 10, RoundingMode.HALF_EVEN)
            averageLoss = averageLoss.multiply(BigDecimal(length - 1)).add(loss)
                .divide(BigDecimal(length), 10, RoundingMode.HALF_EVEN)
            result[i] = rsi(averageGain, averageLoss)
        }
        return result
    }
}


/** Przelicza parę A/B z dwóch kursów wyrażonych w jednostkach na 1 USD. */
object TradingPairMath {
    fun crossRate(baseUnitsPerUsd: BigDecimal, quoteUnitsPerUsd: BigDecimal): BigDecimal {
        require(baseUnitsPerUsd.signum() > 0) { "Kurs instrumentu bazowego musi być dodatni." }
        require(quoteUnitsPerUsd.signum() > 0) { "Kurs instrumentu kwotowanego musi być dodatni." }
        return quoteUnitsPerUsd.divide(baseUnitsPerUsd, 10, RoundingMode.HALF_EVEN)
    }
}
