// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Kurs jawnie określający liczbę jednostek waluty kwotowanej za jedną
 * jednostkę waluty bazowej (np. EUR/PLN = 4.3000 oznacza 1 EUR = 4.30 PLN).
 *
 * To wyłącznie mechanizm kalkulacyjny: nie pobiera notowań i nie oznacza
 * rzeczywistej ceny wykonania transakcji.
 */
data class ExchangeRate(
    val baseCurrency: String,
    val quoteCurrency: String,
    val quotePerBase: BigDecimal
) {
    fun validate() {
        require(baseCurrency.matches(Regex("[A-Z]{3}"))) { "Nieprawidłowy kod waluty bazowej." }
        require(quoteCurrency.matches(Regex("[A-Z]{3}"))) { "Nieprawidłowy kod waluty kwotowanej." }
        require(baseCurrency != quoteCurrency) { "Waluty kursu muszą być różne." }
        require(quotePerBase > BigDecimal.ZERO) { "Kurs musi być większy od zera." }
    }
}

object ExchangeRateCalculator {
    private val reciprocalContext = MathContext(34, RoundingMode.HALF_EVEN)

    /**
     * Przelicza kwotę według kursu bezpośredniego albo odwrotnego.
     * targetScale musi pochodzić ze słownika walut ISO 4217.
     */
    fun convert(
        amount: BigDecimal,
        fromCurrency: String,
        toCurrency: String,
        rate: ExchangeRate,
        targetScale: Int,
        roundingMode: RoundingMode = RoundingMode.HALF_EVEN
    ): BigDecimal {
        rate.validate()
        require(amount >= BigDecimal.ZERO) { "Kwota do przeliczenia nie może być ujemna." }
        require(targetScale in 0..12) { "Nieprawidłowa precyzja waluty docelowej." }
        require(fromCurrency.matches(Regex("[A-Z]{3}")) && toCurrency.matches(Regex("[A-Z]{3}"))) {
            "Waluty muszą mieć trzyliterowe kody ISO 4217."
        }
        require(fromCurrency != toCurrency) { "Waluta źródłowa i docelowa muszą być różne." }

        val unrounded = when {
            fromCurrency == rate.baseCurrency && toCurrency == rate.quoteCurrency ->
                amount.multiply(rate.quotePerBase)
            fromCurrency == rate.quoteCurrency && toCurrency == rate.baseCurrency ->
                amount.divide(rate.quotePerBase, reciprocalContext)
            else -> throw IllegalArgumentException("Kurs nie obejmuje wskazanej pary walut.")
        }
        return unrounded.setScale(targetScale, roundingMode)
    }
}
