// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

enum class PairRiskSignificance {
    PROFIT_ADVANTAGE, LOSS_ADVANTAGE, INCONCLUSIVE, LOW_SAMPLE, INSUFFICIENT
}

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
 *
 * Trend i prognoza procentowa są liczone w logarytmach cen (stopy składane),
 * a wynik historyczny jest mierzony na rzeczywistych ilorazach kursów.
 * Próby wyników nie nakładają się na siebie.
 */
object PairRiskForecastEngine {
    private const val NEUTRAL_BAND_PERCENT = ExchangeProfitThreshold.MIN_NET_PROFIT_PERCENT
    // 10–19 prób daje wyłącznie wstępny szacunek bez etykiety przewagi.
    // Do oceny przewagi statystycznej nadal wymagamy co najmniej 20 prób.
    private const val MIN_ESTIMATE_SAMPLES = 10
    private const val MIN_SIGNIFICANCE_SAMPLES = 20

    fun forecast(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizons: List<ForecastHorizon> = ForecastHorizon.entries
    ): List<PairHorizonRisk> {
        require(values.size >= 2 && values.all { it.isFinite() && it > 0.0 }) {
            "Prognoza pary wymaga co najmniej dwóch dodatnich, skończonych obserwacji."
        }
        val logValues = values.map(::ln)
        val current = values.last()
        val activeLengths = settings.lengths.filter { it <= values.size }
        val minimumWindow = activeLengths.maxOrNull() ?: return horizons.map {
            empty(it, 0.0, ForecastDirection.NEUTRAL)
        }
        val currentChannels = LinearRegressionChannels.calculateAll(logValues, settings).filterNotNull()
        if (currentChannels.isEmpty()) return horizons.map {
            empty(it, 0.0, ForecastDirection.NEUTRAL)
        }
        val consensusSlope = weightedSlope(currentChannels)

        return horizons.map { horizon ->
            val expectedChange = compoundedPercent(consensusSlope, horizon.tradingSessions)
            val currentSignal = direction(expectedChange)
            if (currentSignal == ForecastDirection.NEUTRAL) {
                return@map empty(horizon, expectedChange, currentSignal)
            }

            val lastOrigin = values.lastIndex - horizon.tradingSessions
            if (lastOrigin < minimumWindow - 1) {
                return@map empty(horizon, expectedChange, currentSignal)
            }

            // Każdy wynik obejmuje osobny, niepokrywający się horyzont czasu.
            var profit = 0
            var loss = 0
            var neutral = 0
            var samples = 0
            var origin = minimumWindow - 1
            val step = horizon.tradingSessions

            while (origin <= lastOrigin) {
                val historicalLog = logValues.subList(0, origin + 1)
                val channels = LinearRegressionChannels.calculateAll(historicalLog, settings).filterNotNull()
                if (channels.size == activeLengths.size) {
                    val historicalSlope = weightedSlope(channels)
                    val predictedPercent = compoundedPercent(historicalSlope, horizon.tradingSessions)
                    val predictedDirection = direction(predictedPercent)

                    // Kalibrujemy prawdopodobieństwo dla tego samego sygnału,
                    // który model wskazuje obecnie dla wybranej pary.
                    if (predictedDirection == currentSignal) {
                        val actualPercent = (values[origin + horizon.tradingSessions] /
                            values[origin] - 1.0) * 100.0
                        when (direction(actualPercent)) {
                            ForecastDirection.NEUTRAL -> neutral++
                            predictedDirection -> profit++
                            else -> loss++
                        }
                        samples++
                    }
                }
                origin += step
            }

            if (samples < MIN_ESTIMATE_SAMPLES) {
                PairHorizonRisk(
                    horizon, expectedChange, currentSignal,
                    null, null, null, samples, PairRiskSignificance.INSUFFICIENT
                )
            } else {
                // Smoothing zapobiega raportowaniu dokładnego 0% lub 100% przy małej próbie.
                val pProfit = (profit + 1.0) / (samples + 3.0)
                val pLoss = (loss + 1.0) / (samples + 3.0)
                val pNeutral = (neutral + 1.0) / (samples + 3.0)
                val significance = if (samples < MIN_SIGNIFICANCE_SAMPLES) {
                    PairRiskSignificance.LOW_SAMPLE
                } else {
                    val profitInterval = wilson(profit, samples)
                    val lossInterval = wilson(loss, samples)
                    when {
                        profitInterval.first > lossInterval.second -> PairRiskSignificance.PROFIT_ADVANTAGE
                        lossInterval.first > profitInterval.second -> PairRiskSignificance.LOSS_ADVANTAGE
                        else -> PairRiskSignificance.INCONCLUSIVE
                    }
                }
                PairHorizonRisk(
                    horizon, expectedChange, currentSignal,
                    pProfit, pLoss, pNeutral, samples, significance
                )
            }
        }
    }

    private fun weightedSlope(channels: List<RegressionChannelResult>): Double {
        val weights = channels.sumOf { 1.0 / sqrt(it.length.toDouble()) }
        require(weights > 0.0 && weights.isFinite())
        return channels.sumOf { it.slope / sqrt(it.length.toDouble()) } / weights
    }

    /** Logarytmiczny trend daje składany, a nie liniowo dodawany procent zmiany. */
    private fun compoundedPercent(logSlope: Double, sessions: Int): Double {
        val exponent = (logSlope * sessions).coerceIn(-50.0, 50.0)
        return (exp(exponent) - 1.0) * 100.0
    }

    private fun empty(
        horizon: ForecastHorizon,
        expectedChange: Double,
        direction: ForecastDirection
    ) = PairHorizonRisk(
        horizon, expectedChange, direction,
        null, null, null, 0, PairRiskSignificance.INSUFFICIENT
    )

    private fun direction(change: Double) = ExchangeProfitThreshold.classify(change)

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
