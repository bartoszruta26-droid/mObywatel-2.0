// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Test

class TradingChartModelsTest {
    private fun bar(day: Int, value: String) : TradingPriceBar {
        val price = BigDecimal(value)
        return TradingPriceBar(LocalDate.of(2026, 1, day), price, price, price, price)
    }

    @Test fun tenRegressionWindowsFollowFibonacciSequence() {
        val settings = RegressionChannelSettings()
        assertEquals(listOf(10946, 6765, 4181, 2584, 1597, 987, 610, 377, 233, 144), settings.lengths)
        assertEquals(settings.lengths, LinearRegressionChannels.fibonacciWindowLengths)
        val values = (0 until 1000).map { 50.0 + it * 0.03 + kotlin.math.sin(it / 13.0) }
        val channels = LinearRegressionChannels.calculateAll(values, settings)
        assertEquals(10, channels.size)
        assertEquals(listOf(987, 610, 377, 233, 144), channels.mapNotNull { it?.length })
    }

    @Test fun fibonacciRegressionLengthsCanBeEditedAndWidthsRemainIndependent() {
        val settings = RegressionChannelSettings()
            .withLength(0, 900)
            .withLength(9, 34)
            .withWidth(0, 2.5f)
            .withWidth(9, 3f)
        assertEquals(900, settings.length1)
        assertEquals(34, settings.length10)
        assertEquals(2.5f, settings.width1)
        assertEquals(3f, settings.width10)
        assertEquals(10946, RegressionChannelSettings().length1)
        assertEquals(144, RegressionChannelSettings().length10)
    }

    @Test fun regressionChannelFitsPerfectLinearSeries() {
        val values = (0 until 144).map { 10.0 + it * 0.5 }
        val channel = LinearRegressionChannels.calculate(values, 144, 2.41)!!
        assertEquals(0.5, channel.slope, 1e-10)
        assertEquals(10.0, channel.startMid, 1e-10)
        assertEquals(81.5, channel.endMid, 1e-10)
        assertEquals(0.0, channel.deviation, 1e-10)
        assertEquals(0, channel.brokenDirection)
    }

    @Test fun tripleRegressionUsesIndependentLengthsAndRequiresEnoughBars() {
        val values = (0 until 250).map { 100.0 + it * 0.2 + kotlin.math.sin(it / 8.0) }
        val settings = RegressionChannelSettings()
        val result = LinearRegressionChannels.calculateThree(values, settings)
        assertNull(result[0])
        assertEquals(233, result[1]?.length)
        assertEquals(144, result[2]?.length)
    }

    @Test fun regressionDetectsDownsideBreakAndFibonacciLevels() {
        val values = (0 until 30).map { 100.0 + it.toDouble() } + listOf(1.0)
        val channel = LinearRegressionChannels.calculate(values, 31, 1.0)!!
        assertEquals(-1, channel.brokenDirection)
        assertEquals(channel.lowerAt(channel.endIndex.toDouble(), 1.0) +
            channel.deviation * 2.0 * 0.618,
            channel.fibonacciAt(channel.endIndex.toDouble(), 0.618, 1.0), 1e-9)
    }

    @Test fun regressionSettingsRejectInvalidValues() {
        assertThrows(IllegalArgumentException::class.java) {
            RegressionChannelSettings(length1 = 9)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RegressionChannelSettings(deviationMultiplier = 0.0)
        }
    }

    @Test fun crossRateSupportsCurrencyAndMetalPairs() {
        // USD buys 0.9 EUR and 0.8 CHF; one EUR is therefore 0.8/0.9 CHF.
        assertEquals(BigDecimal("0.8888888889"),
            TradingPairMath.crossRate(BigDecimal("0.9"), BigDecimal("0.8")))
        // USD buys 0.00025 troy ounces of gold and 0.02 troy ounces of silver.
        assertEquals(BigDecimal("80.0000000000"),
            TradingPairMath.crossRate(BigDecimal("0.00025"), BigDecimal("0.02")))
    }

    @Test fun crossRateRejectsNonPositiveInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            TradingPairMath.crossRate(BigDecimal.ZERO, BigDecimal.ONE)
        }
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
        assertEquals(BigDecimal("2.55555556"),
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
        assertEquals(BigDecimal("50.00000000"), values[3])
    }
    @Test fun currencyForecastProducesAllEightHorizonsAndWalkForwardProbability() {
        val values = (0 until 1300).map { index ->
            kotlin.math.exp(index * 0.03 + kotlin.math.sin(index / 17.0) * 0.00002)
        }
        // Keep probability validation on a fully populated short regression window.
                // The default multi-year channels correctly remain neutral for this
                // short-horizon synthetic series.
                val validationSettings = RegressionChannelSettings(
                    length1 = 144, length2 = 144, length3 = 144, length4 = 144, length5 = 144,
                    length6 = 144, length7 = 144, length8 = 144, length9 = 144, length10 = 144
                )
                val forecasts = CurrencyForecastEngine.forecast(values, validationSettings)
        assertEquals(8, forecasts.size)
        assertEquals(ForecastHorizon.WEEK, forecasts.first().horizon)
        assertTrue(forecasts.first().expectedChangePercent > 0.0)
        assertNotNull(forecasts.first().directionalProbability)
        assertTrue(forecasts.first().validationSamples >= 20)
        assertNotNull(forecasts.first().categoryProbability)
        assertTrue(forecasts.first().categoryValidationSamples >= 20)
        assertEquals(ForecastHorizon.THREE_YEARS, forecasts.last().horizon)
        assertNull(forecasts.last().directionalProbability)
        assertTrue(forecasts.last().projectedRate > forecasts.last().currentRate)
    }

    @Test fun currencyForecastRejectsInvalidSeries() {
        assertThrows(IllegalArgumentException::class.java) {
            CurrencyForecastEngine.forecast(listOf(4.0, 0.0), RegressionChannelSettings())
        }
    }

}
