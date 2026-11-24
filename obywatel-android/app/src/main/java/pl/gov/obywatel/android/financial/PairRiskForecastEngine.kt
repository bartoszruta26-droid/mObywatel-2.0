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
    val significance: PairRiskSignificance,
    val activeChannels: Int = 0,
    val lowerQuantilePercent: Double? = null,
    val upperQuantilePercent: Double? = null
)

/**
 * Walidacja krocząca dla wybranej pary. Dla każdego horyzontu dobiera
 * najdłuższe dostępne kanały, które pozostawiają miejsce na próbę historyczną.
 * Wynik jest liczony z niepokrywających się okresów, aby nie zawyżać liczby prób.
 *
 * Sygnał wykorzystuje ważoną medianę prognoz kanałów. Przedział kanałowy
 * powstaje z ważonych kwantyli dolnych i górnych granic regresji (RMS reszt
 * * mnożnik odchylenia). Prawdopodobieństwo jest kalibrowane historycznie
 * dla tego samego kierunku i tej samej klasy położenia przedziału.
 */
object PairRiskForecastEngine {
    private const val MIN_ESTIMATE_SAMPLES = 10
    private const val MIN_SIGNIFICANCE_SAMPLES = 20
    private const val MIN_CHANNELS = 3

    private data class WeightedValue(val value: Double, val weight: Double)
    private data class ForecastBand(
        val medianPercent: Double,
        val lowerQuantilePercent: Double,
        val upperQuantilePercent: Double
    )
    private data class ValidationCounts(
        val profit: Int = 0,
        val loss: Int = 0,
        val neutral: Int = 0
    ) {
        val total: Int get() = profit + loss + neutral
    }

    fun forecast(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizons: List<ForecastHorizon> = ForecastHorizon.entries
    ): List<PairHorizonRisk> {
        require(values.size >= 2 && values.all { it.isFinite() && it > 0.0 }) {
            "Prognoza pary wymaga co najmniej dwóch dodatnich, skończonych obserwacji."
        }
        val logValues = values.map(::ln)
        return horizons.map { horizon ->
            val horizonBars = horizon.tradingSessions
            val availableLengths = settings.lengths.distinct().filter { it <= values.size }.sortedDescending()
            if (availableLengths.size < MIN_CHANNELS) {
                return@map empty(horizon, 0.0, ForecastDirection.NEUTRAL, 0)
            }

            // Dobór okien per horyzont. Dłuższe regresje nie mogą zużyć całej
            // historii i pozostawić zbyt mało punktów do walidacji kroczącej.
            val maxLengthForTenSamples =
                values.size - horizonBars - (MIN_ESTIMATE_SAMPLES - 1) * horizonBars
            val validationLengths = availableLengths
                .filter { it <= maxLengthForTenSamples }
                .ifEmpty { availableLengths.takeLast(MIN_CHANNELS) }

            val currentBand = forecastBand(logValues, validationLengths, settings, horizonBars)
                ?: return@map empty(horizon, 0.0, ForecastDirection.NEUTRAL, 0)
            val currentSignal = ExchangeProfitThreshold.classify(currentBand.medianPercent)
            if (currentSignal == ForecastDirection.NEUTRAL) {
                return@map PairHorizonRisk(
                    horizon, currentBand.medianPercent, currentSignal,
                    null, null, null, 0, PairRiskSignificance.INSUFFICIENT,
                    validationLengths.size, currentBand.lowerQuantilePercent,
                    currentBand.upperQuantilePercent
                )
            }

            val lastOrigin = values.lastIndex - horizonBars
            val firstOrigin = (validationLengths.maxOrNull() ?: values.size) - 1
            var counts = ValidationCounts()
            var origin = firstOrigin
            while (origin <= lastOrigin) {
                val historicalLog = logValues.subList(0, origin + 1)
                val historicalBand = forecastBand(
                    historicalLog, validationLengths, settings, horizonBars
                )
                if (historicalBand != null &&
                    ExchangeProfitThreshold.classify(historicalBand.medianPercent) == currentSignal &&
                    quantileRegime(
                        historicalBand.lowerQuantilePercent,
                        historicalBand.upperQuantilePercent
                    ) == quantileRegime(
                        currentBand.lowerQuantilePercent,
                        currentBand.upperQuantilePercent
                    )
                ) {
                    val actualPercent = (values[origin + horizonBars] / values[origin] - 1.0) * 100.0
                    when (ExchangeProfitThreshold.classifyOutcome(actualPercent, currentSignal)) {
                        currentSignal -> counts = counts.copy(profit = counts.profit + 1)
                        ForecastDirection.NEUTRAL -> counts = counts.copy(neutral = counts.neutral + 1)
                        else -> counts = counts.copy(loss = counts.loss + 1)
                    }
                }
                origin += horizonBars
            }

            if (counts.total < MIN_ESTIMATE_SAMPLES) {
                PairHorizonRisk(
                    horizon, currentBand.medianPercent, currentSignal,
                    null, null, null, counts.total, PairRiskSignificance.INSUFFICIENT,
                    validationLengths.size, currentBand.lowerQuantilePercent,
                    currentBand.upperQuantilePercent
                )
            } else {
                val samples = counts.total
                val pProfit = (counts.profit + 1.0) / (samples + 3.0)
                val pLoss = (counts.loss + 1.0) / (samples + 3.0)
                val pNeutral = (counts.neutral + 1.0) / (samples + 3.0)
                val significance = if (samples < MIN_SIGNIFICANCE_SAMPLES) {
                    PairRiskSignificance.LOW_SAMPLE
                } else {
                    val profitInterval = wilson(counts.profit, samples)
                    val lossInterval = wilson(counts.loss, samples)
                    when {
                        profitInterval.first > lossInterval.second -> PairRiskSignificance.PROFIT_ADVANTAGE
                        lossInterval.first > profitInterval.second -> PairRiskSignificance.LOSS_ADVANTAGE
                        else -> PairRiskSignificance.INCONCLUSIVE
                    }
                }
                PairHorizonRisk(
                    horizon, currentBand.medianPercent, currentSignal,
                    pProfit, pLoss, pNeutral, samples, significance,
                    validationLengths.size, currentBand.lowerQuantilePercent,
                    currentBand.upperQuantilePercent
                )
            }
        }
    }

    private fun forecastBand(
        logValues: List<Double>,
        lengths: List<Int>,
        settings: RegressionChannelSettings,
        horizon: Int
    ): ForecastBand? {
        val channels = lengths.mapNotNull {
            LinearRegressionChannels.calculate(logValues, it, settings.deviationMultiplier)
        }
        if (channels.size != lengths.size || channels.size < MIN_CHANNELS) return null
        val currentLog = logValues.last()
        val midValues = channels.map { channel ->
            val weight = 1.0 / sqrt(channel.length.toDouble())
            WeightedValue(
                compoundedPercent(channel.endMid + channel.slope * horizon - currentLog),
                weight
            )
        }
        val lowerValues = channels.map { channel ->
            val weight = 1.0 / sqrt(channel.length.toDouble())
            WeightedValue(
                compoundedPercent(
                    channel.endMid + channel.slope * horizon -
                        channel.deviation * settings.deviationMultiplier - currentLog
                ),
                weight
            )
        }
        val upperValues = channels.map { channel ->
            val weight = 1.0 / sqrt(channel.length.toDouble())
            WeightedValue(
                compoundedPercent(
                    channel.endMid + channel.slope * horizon +
                        channel.deviation * settings.deviationMultiplier - currentLog
                ),
                weight
            )
        }
        return ForecastBand(
            weightedQuantile(midValues, 0.50),
            weightedQuantile(lowerValues, 0.25),
            weightedQuantile(upperValues, 0.75)
        )
    }

    private fun weightedQuantile(values: List<WeightedValue>, quantile: Double): Double {
        require(values.isNotEmpty())
        val ordered = values.sortedBy { it.value }
        val totalWeight = ordered.sumOf { it.weight }
        val target = totalWeight * quantile.coerceIn(0.0, 1.0)
        var cumulative = 0.0
        for (item in ordered) {
            cumulative += item.weight
            if (cumulative >= target) return item.value
        }
        return ordered.last().value
    }

    /** 0 = przedział niepotwierdzony, +1/-1 = przedział po jednej stronie progu. */
    private fun quantileRegime(lower: Double, upper: Double): Int = when {
        lower > ExchangeProfitThreshold.MIN_NET_PROFIT_PERCENT -> 1
        upper < -ExchangeProfitThreshold.MIN_NET_PROFIT_PERCENT -> -1
        else -> 0
    }

    private fun compoundedPercent(logChange: Double): Double {
        val exponent = logChange.coerceIn(-50.0, 50.0)
        return (exp(exponent) - 1.0) * 100.0
    }

    private fun empty(
        horizon: ForecastHorizon,
        expectedChange: Double,
        direction: ForecastDirection,
        activeChannels: Int
    ) = PairHorizonRisk(
        horizon, expectedChange, direction,
        null, null, null, 0, PairRiskSignificance.INSUFFICIENT, activeChannels
    )

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
