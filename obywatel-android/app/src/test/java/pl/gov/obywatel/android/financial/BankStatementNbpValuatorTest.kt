// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankStatementNbpValuatorTest {
    @Test
    fun valuesBankEntriesUsingLatestNbpRateNotLaterThanBookingDate() {
        val friday = LocalDate.parse("2026-11-20")
        val saturday = LocalDate.parse("2026-11-21")
        val entries = listOf(
            BankStatementEntry("account-1", friday, "EUR", BigDecimal("100.00"), "UZnanie"),
            BankStatementEntry("account-1", saturday, "EUR", BigDecimal("-20.00"), "Obciążenie"),
            BankStatementEntry("account-1", saturday, "PLN", BigDecimal("50.00"), "Uznanie")
        )
        val result = BankStatementNbpValuator.value(
            entries,
            mapOf("EUR" to listOf(friday to BigDecimal("4.30")))
        )
        assertEquals(2, result.points.size)
        assertEquals(BigDecimal("430.00"), result.points[0].netPln)
        assertEquals(BigDecimal("-36.00"), result.points[1].netPln)
        assertTrue(result.unpricedCurrencies.isEmpty())
    }

    @Test
    fun reportsCurrenciesWithoutHistoricalNbpRatesInsteadOfInventingValues() {
        val entry = BankStatementEntry(
            "account-1", LocalDate.parse("2026-11-20"), "XYZ",
            BigDecimal("10"), "Uznanie"
        )
        val result = BankStatementNbpValuator.value(listOf(entry), emptyMap())
        assertTrue(result.points.isEmpty())
        assertEquals(setOf("XYZ"), result.unpricedCurrencies)
    }
}
