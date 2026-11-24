// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PairChartInteractionTest {
    @Test fun horizontalAndVerticalPinchScaleIndependentAxes() {
        val initial = PairChartViewport.fit(-10.0, 10.0)
        val zoomed = initial.transform(
            zoomX = 2.0,
            zoomY = 4.0,
            anchorX = 0.5,
            anchorY = 0.5
        )
        assertEquals((initial.endFraction - initial.startFraction) / 2.0,
            zoomed.endFraction - zoomed.startFraction, 1e-9)
        assertEquals((initial.maxPercent - initial.minPercent) / 4.0,
            zoomed.maxPercent - zoomed.minPercent, 1e-9)
    }

    @Test fun viewportPanKeepsDateWindowInsideAvailableHistory() {
        val initial = PairChartViewport.fit(-5.0, 5.0).transform(zoomX = 2.0)
        val shiftedToStart = initial.transform(panX = 10.0)
        assertEquals(0.0, shiftedToStart.startFraction, 1e-9)
        assertTrue(shiftedToStart.endFraction > 0.0)
        assertTrue(shiftedToStart.endFraction <= 1.0)
    }


    @Test fun viewportCanReserveFutureSpaceAndPanIntoIt() {
        val initial = PairChartViewport.fit(-4.0, 8.0, endFraction = 1.25)
        assertEquals(1.25, initial.endFraction, 1e-9)
        val movedIntoFuture = initial.transform(panX = -0.4)
        assertTrue(movedIntoFuture.startFraction > 0.0)
        assertTrue(movedIntoFuture.endFraction > 1.0)
        val movedBack = movedIntoFuture.transform(panX = 0.4)
        assertTrue(movedBack.startFraction < movedIntoFuture.startFraction)
    }

    @Test fun regressionForecastSegmentContinuesPastLastObservation() {
        val values = (0 until 100).map { 5.0 + it * 0.03 + kotlin.math.sin(it / 9.0) * 0.1 }
        val channel = LinearRegressionChannels.calculate(values, 55)!!
        val forecast = RegressionPlotGeometry.forecastSegment(
            channel,
            projectionEndIndex = 130.0,
            visibleStart = 99.0,
            visibleEnd = 130.0
        )!!
        assertEquals(99.0, forecast.startIndex, 1e-9)
        assertEquals(130.0, forecast.endIndex, 1e-9)
        assertEquals(channel.valueAt(99.0), forecast.startValue, 1e-9)
        assertEquals(channel.valueAt(130.0), forecast.endValue, 1e-9)
        assertNull(RegressionPlotGeometry.forecastSegment(channel, 130.0, 0.0, 98.0))
    }

    @Test fun regressionFitRangeIncludesFutureProjection() {
        val values = (0 until 100).map { it * 0.1 }
        val settings = RegressionChannelSettings(
            length1 = 55, length2 = 55, length3 = 55, length4 = 55,
            length5 = 55, length6 = 55, length7 = 55
        )
        val current = RegressionPlotGeometry.fitRange(values, settings)
        val projected = RegressionPlotGeometry.fitRange(values, settings, 150.0)
        assertTrue(projected.second > current.second)
    }

    @Test fun regressionSegmentsUseMatchingVisibleXAndYCoordinates() {
        val values = (0 until 100).map { 10.0 + it * 0.2 + kotlin.math.sin(it / 7.0) }
        val channel = LinearRegressionChannels.calculate(values, 55)!!
        val visible = RegressionPlotGeometry.visibleSegment(channel, 30.0, 70.0)!!
        assertEquals(45.0, visible.startIndex, 1e-9)
        assertEquals(70.0, visible.endIndex, 1e-9)
        assertEquals(channel.valueAt(45.0), visible.startValue, 1e-9)
        assertEquals(channel.valueAt(70.0), visible.endValue, 1e-9)
        assertNull(RegressionPlotGeometry.visibleSegment(channel, 0.0, 44.0))
    }

    @Test fun regressionFitRangeIncludesPriceSeriesAndChannelBands() {
        val values = (0 until 100).map { 3.0 + it * 0.02 + kotlin.math.sin(it / 5.0) * 0.1 }
        val settings = RegressionChannelSettings(
            length1 = 55, length2 = 55, length3 = 55, length4 = 55,
            length5 = 55, length6 = 55, length7 = 55, deviationMultiplier = 2.41
        )
        val channel = LinearRegressionChannels.calculate(values, 55, settings.deviationMultiplier)!!
        val bounds = RegressionPlotGeometry.fitRange(values, settings)
        val band = channel.deviation * settings.deviationMultiplier
        assertTrue(bounds.first <= channel.valueAt(channel.startIndex.toDouble()) - band)
        assertTrue(bounds.first <= values.minOrNull()!!)
        assertTrue(bounds.second >= channel.valueAt(channel.endIndex.toDouble()) + band)
        assertTrue(bounds.second >= values.maxOrNull()!!)
    }

    @Test fun emptyChartHistoryUsesSafeDefaultPercentBounds() {
        assertEquals(-1.0, RegressionPlotGeometry.fitRange(emptyList(), RegressionChannelSettings()).first, 0.0)
        assertEquals(1.0, RegressionPlotGeometry.fitRange(emptyList(), RegressionChannelSettings()).second, 0.0)
    }

    @Test fun shortChartPeriodUsesAdaptiveRegressionWhenConfiguredWindowsDoNotFit() {
        val values = (0 until 21).map { 100.0 + it * 0.1 + kotlin.math.sin(it / 4.0) }
        val channels = RegressionPlotGeometry.channelsForPlot(values, RegressionChannelSettings())
        assertEquals(1, channels.size)
        assertEquals(21, channels.single()?.length)
        val bounds = RegressionPlotGeometry.fitRange(values, RegressionChannelSettings())
        assertTrue(bounds.first <= values.minOrNull()!!)
        assertTrue(bounds.second >= values.maxOrNull()!!)
    }

    @Test fun selectedPairReportsMonthlyThroughAnnualRiskFromLongHistory() {
        // 20 lat danych dziennych: roczny wynik ma mniej niż 20 prób niezależnych,
        // ale można pokazać ostrożny szacunek z liczbą prób i bez etykiety przewagi.
        val values = (0 until 5000).map { index -> kotlin.math.exp(index * 0.005) }
        val horizons = listOf(
            ForecastHorizon.MONTH,
            ForecastHorizon.TWO_MONTHS,
            ForecastHorizon.QUARTER,
            ForecastHorizon.HALF_YEAR,
            ForecastHorizon.YEAR
        )
        val risks = PairRiskForecastEngine.forecast(values, RegressionChannelSettings(), horizons)
        assertEquals(horizons, risks.map { it.horizon })
        risks.forEach { risk ->
            assertNotNull(risk.profitProbability)
            assertNotNull(risk.lossProbability)
            assertNotNull(risk.neutralProbability)
            assertTrue(risk.samples >= 10)
        }
        val annual = risks.last()
        assertEquals(PairRiskSignificance.LOW_SAMPLE, annual.significance)
        assertTrue(annual.samples in 10 until 20)
        assertTrue(risks.first().expectedChangePercent > 2.0)
    }

    @Test fun pairRiskReportsProfitAndLossForOneSelectedSeries() {
        val values = (0 until 1500).map { index ->
            kotlin.math.exp(index * 0.01 + kotlin.math.sin(index / 19.0) * 0.00002)
        }
        val risks = PairRiskForecastEngine.forecast(values, RegressionChannelSettings())
        assertEquals(8, risks.size)
        val weekly = risks.first()
        assertNotNull(weekly.profitProbability)
        assertNotNull(weekly.lossProbability)
        assertTrue(weekly.samples >= 20)
        assertEquals(PairRiskSignificance.PROFIT_ADVANTAGE, weekly.significance)
        assertNull(risks.last().profitProbability)
        assertEquals(PairRiskSignificance.INSUFFICIENT, risks.last().significance)
    }
    @Test fun selectedPricePointBecomesZeroPercentReference() {
        val dates = listOf(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-02"),
            LocalDate.parse("2026-01-03")
        )
        val points = listOf("100", "110", "90").mapIndexed { index, value ->
            PairPricePoint(dates[index], BigDecimal(value))
        }
        val relative = pairRelativePerformance(points, anchorIndex = 1)
        assertEquals(-100.0 / 11.0, relative[0], 1e-9)
        assertEquals(0.0, relative[1], 1e-9)
        assertEquals(-200.0 / 11.0, relative[2], 1e-9)
    }

    @Test fun referenceIndexIsClampedAndDefaultUsesFirstPrice() {
        val points = listOf(
            PairPricePoint(LocalDate.parse("2026-01-01"), BigDecimal("200")),
            PairPricePoint(LocalDate.parse("2026-01-02"), BigDecimal("250"))
        )
        assertEquals(0.0, pairRelativePerformance(points)[0], 1e-9)
        assertEquals(25.0, pairRelativePerformance(points, -1)[1], 1e-9)
    }

}
