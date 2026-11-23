// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.sqrt

enum class PairRiskSignificance { PROFIT_ADVANTAGE, LOSS_ADVANTAGE, INCONCLUSIVE, INSUFFICIENT }

data class PairHorizonRisk(
    val horizon: ForecastHorizon,
    val expectedChangePercent: Double,
    val direction: ForecastDirection,
    val profitProbability: Double?,
    val lossProbability: Double?,
    val neutralProbability: Double?,
    val samples: Int,
    val significance: PairRiskSignificance
)

/**
 * Walidacja krocząca dla dokładnie jednego szeregu pary wybranej na wykresie.
 * Prawdopodobieństwa są warunkowe względem sygnału kierunkowego modelu.
 */
object PairRiskForecastEngine {
    private const val NEUTRAL_BAND_PERCENT = 0.05
    private const val MIN_SAMPLES = 20
    private const val MAX_VALIDATION_POINTS = 250

    fun forecast(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizons: List<ForecastHorizon> = ForecastHorizon.entries
    ): List<PairHorizonRisk> {
        require(values.size >= 2 && values.all { it.isFinite() && it > 0.0 }) {
            "Prognoza pary wymaga co najmniej dwóch dodatnich, skończonych obserwacji."
        }
        val currentForecasts = CurrencyForecastEngine.forecast(values, settings, horizons)
        val minimumWindow = settings.lengths.maxOrNull()
            ?: return currentForecasts.map { empty(it) }

        return currentForecasts.map { current ->
            val currentSignal = current.direction
            val lastOrigin = values.lastIndex - current.horizon.tradingSessions
            if (lastOrigin < minimumWindow - 1) return@map empty(current)

            val possibleOrigins = lastOrigin - minimumWindow + 2
            val step = maxOf(1, possibleOrigins / MAX_VALIDATION_POINTS)
            var profit = 0
            var loss = 0
            var neutral = 0
            var samples = 0
            var origin = minimumWindow - 1

            while (origin <= lastOrigin) {
                val history = values.subList(0, origin + 1)
                val channels = LinearRegressionChannels.calculateAll(history, settings).filterNotNull()
                if (channels.size == settings.lengths.size) {
                    val weightSum = channels.sumOf { 1.0 / sqrt(it.length.toDouble()) }
                    val slope = channels.sumOf {
                        it.slope / sqrt(it.length.toDouble())
                    } / weightSum
                    val projected = (history.last() + slope * current.horizon.tradingSessions)
                        .coerceAtLeast(1e-12)
                    val predictedPercent = (projected / history.last() - 1.0) * 100.0
                    val predictedDirection = direction(predictedPercent)
                    val matchesSignal = if (currentSignal == ForecastDirection.NEUTRAL) {
                        predictedDirection != ForecastDirection.NEUTRAL
                    } else predictedDirection == currentSignal

                    if (matchesSignal) {
                        val actualPercent = (values[origin + current.horizon.tradingSessions] /
                            values[origin] - 1.0) * 100.0
                        when {
                            actualPercent > NEUTRAL_BAND_PERCENT -> profit++
                            actualPercent < -NEUTRAL_BAND_PERCENT -> loss++
                            else -> neutral++
                        }
                        samples++
                    }
                }
                origin += step
            }

            if (samples < MIN_SAMPLES) {
                PairHorizonRisk(
                    current.horizon, current.expectedChangePercent, current.direction,
                    null, null, null, samples, PairRiskSignificance.INSUFFICIENT
                )
            } else {
                val pProfit = (profit + 1.0) / (samples + 3.0)
                val pLoss = (loss + 1.0) / (samples + 3.0)
                val pNeutral = (neutral + 1.0) / (samples + 3.0)
                val profitInterval = wilson(profit, samples)
                val lossInterval = wilson(loss, samples)
                val significance = when {
                    profitInterval.first > lossInterval.second -> PairRiskSignificance.PROFIT_ADVANTAGE
                    lossInterval.first > profitInterval.second -> PairRiskSignificance.LOSS_ADVANTAGE
                    else -> PairRiskSignificance.INCONCLUSIVE
                }
                PairHorizonRisk(
                    current.horizon, current.expectedChangePercent, current.direction,
                    pProfit, pLoss, pNeutral, samples, significance
                )
            }
        }
    }

    private fun empty(forecast: HorizonCurrencyForecast) = PairHorizonRisk(
        forecast.horizon, forecast.expectedChangePercent, forecast.direction,
        null, null, null, 0, PairRiskSignificance.INSUFFICIENT
    )

    private fun direction(change: Double) = when {
        change > NEUTRAL_BAND_PERCENT -> ForecastDirection.UP
        change < -NEUTRAL_BAND_PERCENT -> ForecastDirection.DOWN
        else -> ForecastDirection.NEUTRAL
    }

    /** 95% Wilson score interval; conservative overlap rule for color warnings. */
    private fun wilson(successes: Int, trials: Int): Pair<Double, Double> {
        val z = 1.96
        val n = trials.toDouble()
        val p = successes / n
        val z2 = z * z
        val denominator = 1.0 + z2 / n
        val center = (p + z2 / (2.0 * n)) / denominator
        val margin = z * sqrt((p * (1.0 - p) / n) + z2 / (4.0 * n * n)) / denominator
        return (center - margin).coerceAtLeast(0.0) to (center + margin).coerceAtMost(1.0)
    }
}
