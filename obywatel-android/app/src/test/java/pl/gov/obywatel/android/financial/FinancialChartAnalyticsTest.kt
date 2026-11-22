// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialChartAnalyticsTest {
    private fun values(count: Int) = (1..count).map { BigDecimal(it.toString()) }

    @Test
    fun movingAveragesWaitForTheirFullLookbackWindow() {
        val input = values(50)
        val sma20 = FinancialChartAnalytics.sma(input, 20)
        val sma50 = FinancialChartAnalytics.sma(input, 50)
        assertNull(sma20[18])
        assertEquals("10.50000000", sma20[19]?.toPlainString())
        assertNull(sma50[48])
        assertEquals("25.50000000", sma50[49]?.toPlainString())
    }

    @Test
    fun rsiReturnsHundredForStrictlyRisingSeriesAndFiftyForFlatSeries() {
        val rising = FinancialChartAnalytics.rsi(values(20), 14)
        assertEquals(0, rising.last()!!.compareTo(BigDecimal("100")))
        val flat = FinancialChartAnalytics.rsi(List(20) { BigDecimal("4.0") }, 14)
        assertEquals("50", flat.last()?.stripTrailingZeros()?.toPlainString())
    }

    @Test
    fun macdAndSignalRespectWarmupPeriods() {
        val input = values(60)
        val line = FinancialChartAnalytics.macd(input, 12, 26)
        val signal = FinancialChartAnalytics.macdSignal(input, 12, 26, 9)
        assertNull(line[24])
        assertNotNull(line[25])
        assertNull(signal[32])
        assertNotNull(signal[33])
    }

    @Test
    fun bollingerBandsContainTheMovingAverageForNonConstantSeries() {
        val input = values(30)
        val (upper, lower) = FinancialChartAnalytics.bollinger(input, 20, 2)
        assertNull(upper[18])
        assertTrue(upper[19]!! > lower[19]!!)
    }

    @Test
    fun pineScriptTemplateDeclaresVersionSixAndExpectedIndicators() {
        val script = FinancialChartAnalytics.pineScriptTemplate()
        assertTrue(script.contains("//@version=6"))
        assertTrue(script.contains("ta.rsi"))
        assertTrue(script.contains("ta.macd"))
        assertTrue(script.contains("ta.stdev"))
    }
}
