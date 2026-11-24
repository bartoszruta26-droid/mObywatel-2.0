// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExchangeProfitThresholdAndRegressionTest {
    @Test
    fun exchangeMovementMustExceedTwoPercentToCountAsProfitOrLoss() {
        assertEquals(ForecastDirection.NEUTRAL, ExchangeProfitThreshold.classify(2.0))
        assertEquals(ForecastDirection.UP, ExchangeProfitThreshold.classify(2.000001))
        assertEquals(ForecastDirection.NEUTRAL, ExchangeProfitThreshold.classify(-2.0))
        assertEquals(ForecastDirection.DOWN, ExchangeProfitThreshold.classify(-2.000001))
        assertEquals(ForecastDirection.NEUTRAL, ExchangeProfitThreshold.classify(0.75))
    }

    @Test
    fun tenRegressionWindowsReplaceTheTwoShortestAndAddFiveLongerOnes() {
        val settings = RegressionChannelSettings()
        assertEquals(
            listOf(10946, 6765, 4181, 2584, 1597, 987, 610, 377, 233, 144),
            settings.lengths
        )
        assertFalse(settings.lengths.contains(89))
        assertFalse(settings.lengths.contains(55))
        assertEquals(10, settings.lengths.size)
    }

    @Test
    fun longRegressionWindowsActivateOnlyWhenEnoughObservationsExist() {
        val settings = RegressionChannelSettings()
        val values = (0 until 2000).map { it.toDouble() + kotlin.math.sin(it / 10.0) }
        val channels = LinearRegressionChannels.calculateAll(values, settings)
        assertEquals(10, channels.size)
        assertNull(channels[0]) // 10946 observations are not present
        assertNull(channels[1]) // 6765 observations are not present
        assertNull(channels[2]) // 4181 observations are not present
        assertNull(channels[3]) // 2584 observations are not present
        assertNotNull(channels[4]) // 1597 observations are present
        assertNotNull(channels[5]) // 987 observations are present
        assertTrue(channels.drop(4).all { it != null })
    }

    @Test
    fun regressionGeometryRetainsAllTenSlotsWithoutInventingShortFallbackLines() {
        val settings = RegressionChannelSettings()
        val values = (0 until 2000).map { it.toDouble() + kotlin.math.sin(it / 10.0) }
        val channels = RegressionPlotGeometry.channelsForPlot(values, settings)
        assertEquals(10, channels.size)
        assertEquals(listOf(1597, 987, 610, 377, 233, 144), channels.mapNotNull { it?.length })
    }
}
