// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BankImportDeduplicatorTest {
    private fun entry(
        currency: String = "USD",
        amount: String = "749.33",
        reference: String = "102693731",
        type: String = "WYMIANA W KANTORZE - UZNANIE",
        account: String = "account-scope-1"
    ) = BankStatementEntry(
        accountScopeId = account,
        bookingDate = LocalDate.parse("2026-11-12"),
        currencyCode = currency,
        signedAmount = BigDecimal(amount),
        operationType = type,
        bankOperationReference = reference
    )

    @Test
    fun sameAccountingEntryFromDifferentFileFormatsHasSameIdentity() {
        val fromXml = entry()
        val fromPdf = entry(amount = "749.330")
        assertEquals(fromXml.accountingEntryId(), fromPdf.accountingEntryId())
    }

    @Test
    fun importerRemovesDuplicatesInsideBatchAndAgainstExistingLedger() {
        val original = entry()
        val another = entry(reference = "102678666", amount = "751.30")
        val result = BankImportDeduplicator.classify(
            listOf(original, entry(amount = "749.330"), another),
            setOf(another.accountingEntryId())
        )
        assertEquals(1, result.newEntries.size)
        assertEquals(2, result.duplicates.size)
        assertEquals(original.accountingEntryId(), result.newEntries.single().accountingEntryId())
    }

    @Test
    fun oppositeLegsOfSameExchangeAreNotDuplicatesButShareOperationId() {
        val usdDebit = entry(
            currency = "USD",
            amount = "-8000.00",
            reference = "90124777",
            type = "WYMIANA W KANTORZE - OBCIĄŻENIE",
            account = "usd-account"
        )
        val chfCredit = entry(
            currency = "CHF",
            amount = "6183.20",
            reference = "90124777",
            type = "WYMIANA W KANTORZE - UZNANIE",
            account = "chf-account"
        )
        assertNotEquals(usdDebit.accountingEntryId(), chfCredit.accountingEntryId())
        assertNotEquals(null, usdDebit.exchangeOperationId())
        assertNotEquals(null, chfCredit.exchangeOperationId())
        assertEquals(usdDebit.exchangeOperationId(), chfCredit.exchangeOperationId())
        assertEquals(
            usdDebit.exchangeOperationId(),
            BankStatementEntry(
                accountScopeId = "usd-account",
                bookingDate = LocalDate.parse("2026-03-04"),
                currencyCode = "USD",
                signedAmount = BigDecimal("-8000"),
                operationType = "WYMIANA W KANTORZE - OBCIĄŻENIE",
                bankOperationReference = "90124777"
            ).exchangeOperationId()
        )
    }

    @Test
    fun fallbackWithoutBankReferenceUsesNormalizedDescription() {
        val a = entry(reference = "").copy(description = " FX operation   123 ")
        val b = entry(reference = "").copy(description = "fx operation 123")
        assertEquals(a.accountingEntryId(), b.accountingEntryId())
        assertNull(a.exchangeOperationId())
    }

    @Test
    fun previewSeparatesDuplicatesFromConflictingReference() {
        val existing = entry()
        val exactDuplicate = entry(amount = "749.330")
        val conflict = entry(amount = "750.00")
        val newRecord = entry(reference = "NEW-REF", amount = "10.00")
        val preview = BankImportDeduplicator.preview(
            incoming = listOf(exactDuplicate, conflict, newRecord),
            existingEntries = listOf(existing)
        )
        assertEquals(1, preview.duplicates.size)
        assertEquals(1, preview.conflicts.size)
        assertEquals(1, preview.newEntries.size)
        assertEquals("750.00", preview.conflicts.single().incoming.signedAmount.toPlainString())
    }

    @Test
    fun previewKeepsOppositeExchangeLegsWhenAccountScopesDiffer() {
        val debit = entry(currency = "USD", amount = "-8000", reference = "FX-1", account = "usd")
        val credit = entry(currency = "CHF", amount = "6183.20", reference = "FX-1", account = "chf")
        val preview = BankImportDeduplicator.preview(listOf(debit, credit), emptyList())
        assertEquals(2, preview.newEntries.size)
        assertEquals(0, preview.conflicts.size)
    }


    @Test
    fun previewFlagsSameReferenceWithChangedDateCurrencyOrDirection() {
        val existing = entry()
        val changedEntries = listOf(
            existing.copy(bookingDate = existing.bookingDate.plusDays(1)),
            existing.copy(currencyCode = "EUR"),
            existing.copy(signedAmount = BigDecimal("-749.33")),
            existing.copy(operationType = "INNY TYP OPERACJI")
        )
        changedEntries.forEach { changed ->
            val preview = BankImportDeduplicator.preview(
                incoming = listOf(changed),
                existingEntries = listOf(existing)
            )
            assertEquals(1, preview.conflicts.size)
            assertEquals(0, preview.newEntries.size)
            assertEquals(0, preview.duplicates.size)
        }
    }

    @Test
    fun previewDetectsConflictsInsideTheIncomingBatch() {
        val first = entry()
        val changed = first.copy(signedAmount = BigDecimal("750.00"))
        val preview = BankImportDeduplicator.preview(
            incoming = listOf(first, changed),
            existingEntries = emptyList()
        )
        assertEquals(1, preview.newEntries.size)
        assertEquals(1, preview.conflicts.size)
        assertEquals(0, preview.duplicates.size)
    }

}
