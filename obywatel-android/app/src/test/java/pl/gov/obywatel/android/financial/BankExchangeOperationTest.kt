// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BankExchangeOperationTest {
    private fun leg(
        currency: String,
        amount: String,
        type: String,
        reference: String = "90124777"
    ) = BankStatementEntry(
        accountScopeId = "ipko-account-1",
        bookingDate = LocalDate.parse("2026-03-04"),
        currencyCode = currency,
        signedAmount = BigDecimal(amount),
        operationType = type,
        bankOperationReference = reference
    )

    @Test
    fun projectsUsdDebitAndChfCreditAsOneCompleteOperationWithoutMergingEntries() {
        val debit = leg("USD", "-8000.00", "WYMIANA W KANTORZE - OBCIĄŻENIE")
        val credit = leg("CHF", "6183.20", "WYMIANA W KANTORZE - UZNANIE")

        val result = BankExchangeOperationProjector.project(listOf(debit, credit))

        assertEquals(1, result.size)
        assertEquals(ExchangeOperationStatus.COMPLETE, result.single().status)
        assertEquals(2, result.single().legs.size)
        assertEquals(setOf("USD", "CHF"), result.single().currencies)
        assertEquals(
            setOf(debit.accountingEntryId(), credit.accountingEntryId()),
            result.single().legs.map { it.accountingEntryId() }.toSet()
        )
        assertNotEquals(debit.accountingEntryId(), credit.accountingEntryId())
    }

    @Test
    fun keepsSingleLegVisibleAsIncomplete() {
        val debit = leg("USD", "-8000.00", "WYMIANA W KANTORZE - OBCIĄŻENIE")
        val result = BankExchangeOperationProjector.project(listOf(debit))

        assertEquals(1, result.size)
        assertEquals(ExchangeOperationStatus.SINGLE_LEG, result.single().status)
        assertEquals(1, result.single().legs.size)
    }

    @Test
    fun marksAmbiguousOrInvalidGroupsForReview() {
        val debit = leg("USD", "-8000.00", "WYMIANA W KANTORZE - OBCIĄŻENIE")
        val wrongDirection = leg("CHF", "-6183.20", "WYMIANA W KANTORZE - OBCIĄŻENIE")
        val extra = leg("EUR", "7000.00", "WYMIANA W KANTORZE - UZNANIE")

        val result = BankExchangeOperationProjector.project(listOf(debit, wrongDirection, extra))

        assertEquals(1, result.size)
        assertEquals(ExchangeOperationStatus.REVIEW_REQUIRED, result.single().status)
        assertEquals(3, result.single().legs.size)
    }

    @Test
    fun ignoresEntriesWithoutExchangeReference() {
        val ordinary = leg("PLN", "100.00", "PRZELEW", reference = "")
        assertEquals(emptyList<BankExchangeOperation>(), BankExchangeOperationProjector.project(listOf(ordinary)))
    }
}
