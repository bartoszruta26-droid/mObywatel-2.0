// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TradingChartModelsTest {
    private fun bar(day: Int, value: String) : TradingPriceBar {
        val price = BigDecimal(value)
        return TradingPriceBar(LocalDate.of(2026, 1, day), price, price, price, price)
    }

    @Test fun referenceBarHasNoInventedRangeOrVolume() {
        val price = BigDecimal("4.3210")
        val candle = TradingPriceBar.reference(LocalDate.of(2026, 1, 1), price)
        assertEquals(price, candle.open)
        assertEquals(price, candle.high)
        assertEquals(price, candle.low)
        assertEquals(price, candle.close)
        assertEquals(PriceBarKind.REFERENCE, candle.kind)
        assertNull(candle.volume)
    }

    @Test fun rejectsInconsistentOhlcAndNegativeVolume() {
        assertThrows(IllegalArgumentException::class.java) {
            TradingPriceBar(LocalDate.now(), BigDecimal("4"), BigDecimal("3"), BigDecimal("2"), BigDecimal("4"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            TradingPriceBar(LocalDate.now(), BigDecimal("4"), BigDecimal("5"), BigDecimal("3"), BigDecimal("4"), BigDecimal("-1"))
        }
    }

    @Test fun calculatesPriceChangeAndRange() {
        val bars = listOf(
            TradingPriceBar(LocalDate.of(2026, 1, 1), BigDecimal("4.0"), BigDecimal("4.3"), BigDecimal("3.9"), BigDecimal("4.1")),
            TradingPriceBar(LocalDate.of(2026, 1, 2), BigDecimal("4.1"), BigDecimal("4.5"), BigDecimal("4.0"), BigDecimal("4.3"))
        )
        val stats = TradingChartAnalytics.statistics(bars)
        assertEquals(BigDecimal("4.1"), stats.first)
        assertEquals(BigDecimal("4.3"), stats.last)
        assertEquals(BigDecimal("3.9"), stats.minimum)
        assertEquals(BigDecimal("4.5"), stats.maximum)
        assertEquals(BigDecimal("0.2"), stats.absoluteChange)
        assertEquals(BigDecimal("4.8780"), stats.percentChange)
    }

    @Test fun computesSmaWithWarmupAndEma() {
        val bars = listOf(bar(1, "1"), bar(2, "2"), bar(3, "3"))
        assertEquals(listOf(null, BigDecimal("1.50000000"), BigDecimal("2.50000000")),
            TradingChartAnalytics.simpleMovingAverage(bars, 2))
        assertEquals(BigDecimal("2.33333333"),
            TradingChartAnalytics.exponentialMovingAverage(bars, 2)[2])
    }

    @Test fun calculatesWilderRsiAndWarmup() {
        val bars = listOf("1", "2", "3", "2", "3", "4").mapIndexed { index, value ->
            val price = BigDecimal(value)
            TradingPriceBar(LocalDate.of(2026, 1, index + 1), price, price, price, price)
        }
        val values = TradingChartAnalytics.relativeStrengthIndex(bars, 2)
        assertNull(values[0])
        assertNull(values[1])
        assertEquals(BigDecimal("100"), values[2])
        assertEquals(BigDecimal("50"), values[3])
    }
}
