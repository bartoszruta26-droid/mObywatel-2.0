// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import kotlin.math.abs
import kotlin.math.sqrt

enum class ForecastHorizon(val label: String, val tradingSessions: Int) {
    WEEK("1 tydzień", 5),
    MONTH("1 miesiąc", 21),
    TWO_MONTHS("2 miesiące", 42),
    QUARTER("1 kwartał", 63),
    HALF_YEAR("półrocze", 126),
    YEAR("1 rok", 252),
    TWO_YEARS("2 lata", 504),
    THREE_YEARS("3 lata", 756)
}

enum class ForecastDirection { UP, DOWN, NEUTRAL }

enum class ForecastMagnitude(val label: String) {
    VERY_LARGE("bardzo duży"),
    LARGE("duży"),
    MODERATE("umiarkowany"),
    LIGHT("lekki"),
    NEUTRAL("neutralny")
}

data class HorizonCurrencyForecast(
    val horizon: ForecastHorizon,
    val currentRate: Double,
    val projectedRate: Double,
    val expectedChangePercent: Double,
    val direction: ForecastDirection,
    val magnitude: ForecastMagnitude,
    /** Empirical walk-forward directional hit rate; null when the sample is too small. */
    val directionalProbability: Double?,
    val validationSamples: Int,
    val categoryProbability: Double?,
    val categoryValidationSamples: Int,
    val activeChannels: Int,
    val channelAgreement: Double
)

object CurrencyForecastEngine {
    private const val MIN_VALIDATION_SAMPLES = 20
    private const val NEUTRAL_BAND_PERCENT = ExchangeProfitThreshold.MIN_NET_PROFIT_PERCENT

    fun forecast(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizons: List<ForecastHorizon> = ForecastHorizon.entries
    ): List<HorizonCurrencyForecast> {
        require(values.size >= 2 && values.all { it.isFinite() && it > 0.0 }) {
            "Prognoza wymaga co najmniej dwóch dodatnich, skończonych kursów."
        }
        val current = values.last()
        val channels = LinearRegressionChannels.calculateAll(values, settings).filterNotNull()
        require(channels.isNotEmpty()) { "Za mało danych do obliczenia kanałów regresji." }

        // Shorter windows react faster; inverse-square-root weighting limits their dominance.
        val totalWeight = channels.sumOf { 1.0 / sqrt(it.length.toDouble()) }
        val consensusSlope = channels.sumOf {
            it.slope / sqrt(it.length.toDouble())
        } / totalWeight
        val positiveChannels = channels.count { it.slope > 0.0 }
        val negativeChannels = channels.count { it.slope < 0.0 }
        val agreement = maxOf(positiveChannels, negativeChannels).toDouble() / channels.size

        return horizons.map { horizon ->
            val projected = (current + consensusSlope * horizon.tradingSessions).coerceAtLeast(0.00000001)
            val change = ((projected / current) - 1.0) * 100.0
            val direction = ExchangeProfitThreshold.classify(change)
            val validation = walkForwardValidation(values, settings, horizon.tradingSessions, direction, magnitude(abs(change)))
            HorizonCurrencyForecast(
                horizon = horizon,
                currentRate = current,
                projectedRate = projected,
                expectedChangePercent = change,
                direction = direction,
                magnitude = magnitude(abs(change)),
                directionalProbability = validation.directionProbability,
                validationSamples = validation.directionSamples,
                categoryProbability = validation.categoryProbability,
                categoryValidationSamples = validation.categorySamples,
                activeChannels = channels.size,
                channelAgreement = agreement
            )
        }
    }

    private data class WalkForwardValidation(
        val directionProbability: Double?,
        val directionSamples: Int,
        val categoryProbability: Double?,
        val categorySamples: Int
    )

    /**
     * Krocząca walidacja bez dostępu do przyszłych obserwacji. Osobno mierzy
     * trafność kierunku i trafność pełnej klasy (kierunek + skala ruchu).
     */
    private fun walkForwardValidation(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizon: Int,
        targetDirection: ForecastDirection,
        targetMagnitude: ForecastMagnitude
    ): WalkForwardValidation {
        val activeLengths = settings.lengths.filter { it <= values.size }
        val minimumWindow = activeLengths.maxOrNull()
            ?: return WalkForwardValidation(null, 0, null, 0)
        val lastOrigin = values.lastIndex - horizon
        if (lastOrigin < minimumWindow - 1) {
            return WalkForwardValidation(null, 0, null, 0)
        }

        var directionHits = 0
        var directionTrials = 0
        var categoryHits = 0
        var categoryTrials = 0
        val step = maxOf(1, horizon / 20)
        var origin = minimumWindow - 1
        while (origin <= lastOrigin) {
            val historical = values.subList(0, origin + 1)
            val slopes = activeLengths.mapNotNull { LinearRegressionChannels.calculate(historical, it, settings.deviationMultiplier) }
            if (slopes.size == activeLengths.size) {
                val weightSum = slopes.sumOf { 1.0 / sqrt(it.length.toDouble()) }
                val predictedSlope = slopes.sumOf {
                    it.slope / sqrt(it.length.toDouble())
                } / weightSum
                val predictedRate = historical.last() + predictedSlope * horizon
                val predictedPercent = ((predictedRate.coerceAtLeast(0.00000001) /
                    historical.last()) - 1.0) * 100.0
                val predictedDirection = ExchangeProfitThreshold.classify(predictedPercent)
                val actualPercent = ((values[origin + horizon] / values[origin]) - 1.0) * 100.0
                val actualDirection = ExchangeProfitThreshold.classify(actualPercent)
                if (predictedDirection != ForecastDirection.NEUTRAL) {
                    // Ruch neutralny lub przeciwny jest błędem prognozy kierunku, nie pomijamy go.
                    directionTrials++
                    if (predictedDirection == actualDirection) directionHits++
                }
                if (predictedDirection == targetDirection &&
                    magnitude(abs(predictedPercent)) == targetMagnitude &&
                    predictedDirection != ForecastDirection.NEUTRAL
                ) {
                    categoryTrials++
                    if (actualDirection == predictedDirection &&
                        magnitude(abs(actualPercent)) == targetMagnitude
                    ) categoryHits++
                }
            }
            origin += step
        }
        val directionProbability = if (directionTrials >= MIN_VALIDATION_SAMPLES)
            (directionHits + 1.0) / (directionTrials + 2.0) else null
        val categoryProbability = if (categoryTrials >= MIN_VALIDATION_SAMPLES)
            (categoryHits + 1.0) / (categoryTrials + 2.0) else null
        return WalkForwardValidation(
            directionProbability, directionTrials, categoryProbability, categoryTrials
        )
    }

    private fun magnitude(changePercent: Double): ForecastMagnitude = when {
        changePercent < 0.05 -> ForecastMagnitude.NEUTRAL
        changePercent < 0.5 -> ForecastMagnitude.LIGHT
        changePercent < 2.0 -> ForecastMagnitude.MODERATE
        changePercent < 5.0 -> ForecastMagnitude.LARGE
        else -> ForecastMagnitude.VERY_LARGE
    }
}
