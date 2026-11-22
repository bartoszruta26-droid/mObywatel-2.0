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
    val activeChannels: Int,
    val channelAgreement: Double
)

object CurrencyForecastEngine {
    private const val MIN_VALIDATION_SAMPLES = 20
    private const val NEUTRAL_BAND_PERCENT = 0.05

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
            val direction = when {
                change > NEUTRAL_BAND_PERCENT -> ForecastDirection.UP
                change < -NEUTRAL_BAND_PERCENT -> ForecastDirection.DOWN
                else -> ForecastDirection.NEUTRAL
            }
            val validation = walkForwardHitRate(values, settings, horizon.tradingSessions)
            HorizonCurrencyForecast(
                horizon = horizon,
                currentRate = current,
                projectedRate = projected,
                expectedChangePercent = change,
                direction = direction,
                magnitude = magnitude(abs(change)),
                directionalProbability = validation.first,
                validationSamples = validation.second,
                activeChannels = channels.size,
                channelAgreement = agreement
            )
        }
    }

    /**
     * Walk-forward validation: each historical forecast uses only observations available
     * at its origin. We require every configured channel to be available, preventing
     * a shorter-window backtest from overstating confidence in a seven-channel forecast.
     */
    private fun walkForwardHitRate(
        values: List<Double>,
        settings: RegressionChannelSettings,
        horizon: Int
    ): Pair<Double?, Int> {
        val minimumWindow = settings.lengths.maxOrNull() ?: return null to 0
        val lastOrigin = values.lastIndex - horizon
        if (lastOrigin < minimumWindow - 1) return null to 0

        var hits = 0
        var trials = 0
        val step = maxOf(1, horizon / 20)
        var origin = minimumWindow - 1
        while (origin <= lastOrigin) {
            val historical = values.subList(0, origin + 1)
            val slopes = LinearRegressionChannels.calculateAll(historical, settings).filterNotNull()
            if (slopes.size == settings.lengths.size) {
                val weightSum = slopes.sumOf { 1.0 / sqrt(it.length.toDouble()) }
                val predictedSlope = slopes.sumOf {
                    it.slope / sqrt(it.length.toDouble())
                } / weightSum
                val actualReturn = values[origin + horizon] / values[origin] - 1.0
                if (abs(predictedSlope) > 1e-12 && abs(actualReturn) > 1e-12) {
                    trials++
                    if ((predictedSlope > 0.0) == (actualReturn > 0.0)) hits++
                }
            }
            origin += step
        }
        return if (trials >= MIN_VALIDATION_SAMPLES) (hits + 1.0) / (trials + 2.0) else null to trials
    }

    private fun magnitude(changePercent: Double): ForecastMagnitude = when {
        changePercent < 0.05 -> ForecastMagnitude.NEUTRAL
        changePercent < 0.5 -> ForecastMagnitude.LIGHT
        changePercent < 2.0 -> ForecastMagnitude.MODERATE
        changePercent < 5.0 -> ForecastMagnitude.LARGE
        else -> ForecastMagnitude.VERY_LARGE
    }
}
