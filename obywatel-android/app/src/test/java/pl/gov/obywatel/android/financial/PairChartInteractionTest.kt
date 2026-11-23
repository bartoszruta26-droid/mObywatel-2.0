// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

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

    @Test fun pairRiskReportsProfitAndLossForOneSelectedSeries() {
        val values = (0 until 1500).map { index ->
            2.0 + index * 0.001 + kotlin.math.sin(index / 19.0) * 0.0002
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
}
