// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExchangeRateCalculatorTest {
    private val eurPln = ExchangeRate("EUR", "PLN", BigDecimal("4.3000"))

    @Test
    fun convertsBaseToQuoteWithTargetCurrencyScale() {
        assertEquals(
            BigDecimal("430.00"),
            ExchangeRateCalculator.convert(
                BigDecimal("100"), "EUR", "PLN", eurPln, 2
            )
        )
    }

    @Test
    fun convertsQuoteToBaseUsingReciprocalRate() {
        assertEquals(
            BigDecimal("23.26"),
            ExchangeRateCalculator.convert(
                BigDecimal("100"), "PLN", "EUR", eurPln, 2
            )
        )
    }

    @Test
    fun appliesExplicitRoundingMode() {
        assertEquals(
            BigDecimal("23.2558"),
            ExchangeRateCalculator.convert(
                BigDecimal("100"), "PLN", "EUR", eurPln, 4, RoundingMode.HALF_UP
            )
        )
    }

    @Test
    fun rejectsZeroRateUnknownPairAndNegativeAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRate("EUR", "PLN", BigDecimal.ZERO).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRateCalculator.convert(BigDecimal("10"), "USD", "PLN", eurPln, 2)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRateCalculator.convert(BigDecimal("-1"), "EUR", "PLN", eurPln, 2)
        }
    }

    @Test
    fun rejectsSameCurrencyAndInvalidPrecision() {
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRate("EUR", "EUR", BigDecimal.ONE).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRateCalculator.convert(BigDecimal.ONE, "EUR", "PLN", eurPln, 13)
        }
    }
}
