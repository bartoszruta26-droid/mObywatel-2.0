// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NbpCurrencyConverterTest {
    private val rates = mapOf(
        "EUR" to BigDecimal("4.3000"),
        "USD" to BigDecimal("4.0000"),
        "JPY" to BigDecimal("0.0270"),
        "KWD" to BigDecimal("13.0000")
    )

    @Test
    fun convertsForeignCurrencyToPln() {
        assertEquals(
            BigDecimal("430.00"),
            NbpCurrencyConverter.convert(BigDecimal("100"), "EUR", "PLN", rates)
        )
    }

    @Test
    fun convertsPlnToForeignCurrency() {
        assertEquals(
            BigDecimal("23.26"),
            NbpCurrencyConverter.convert(BigDecimal("100"), "PLN", "EUR", rates)
        )
    }

    @Test
    fun convertsBetweenTwoForeignCurrenciesThroughPln() {
        assertEquals(
            BigDecimal("107.50"),
            NbpCurrencyConverter.convert(BigDecimal("100"), "USD", "EUR", rates)
        )
    }

    @Test
    fun appliesIsoCurrencyPrecision() {
        assertEquals(
            BigDecimal("3703"),
            NbpCurrencyConverter.convert(BigDecimal("100"), "EUR", "JPY", rates)
        )
        assertEquals(0, CurrencyPrecision.scale("JPY"))
        assertEquals(3, CurrencyPrecision.scale("KWD"))
        assertEquals(4, CurrencyPrecision.scale("CLF"))
        assertEquals(2, CurrencyPrecision.scale("EUR"))
    }

    @Test
    fun rejectsMissingRateNegativeAmountAndSameCurrency() {
        assertThrows(IllegalArgumentException::class.java) {
            NbpCurrencyConverter.convert(BigDecimal("1"), "ABC", "PLN", rates)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NbpCurrencyConverter.convert(BigDecimal("-1"), "EUR", "PLN", rates)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NbpCurrencyConverter.convert(BigDecimal("1"), "EUR", "EUR", rates)
        }
    }
}
