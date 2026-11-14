// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentLedgerRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val sourceDatabase = "payment-ledger-repository-source-test.db"
    private val targetDatabase = "payment-ledger-repository-target-test.db"
    private val conflictDatabase = "payment-ledger-repository-conflict-test.db"

    @After
    fun cleanup() {
        context.deleteDatabase(sourceDatabase)
        context.deleteDatabase(targetDatabase)
        context.deleteDatabase(conflictDatabase)
    }

    private fun entry(amount: String = "749.33", reference: String = "PKO-REF-1") =
        BankStatementEntry(
            accountScopeId = "test-account",
            bookingDate = LocalDate.parse("2026-11-12"),
            currencyCode = "USD",
            signedAmount = BigDecimal(amount),
            operationType = "WYMIANA W KANTORZE - UZNANIE",
            bankOperationReference = reference,
            description = "Test importu"
        )

    @Test
    fun exportImportRoundTripIsIdempotent() = runBlocking {
        val source = PaymentLedgerRepository(context, sourceDatabase)
        val target = PaymentLedgerRepository(context, targetDatabase)
        try {
            source.importJson(PaymentLedgerJsonCodec.encode(listOf(entry(), entry("751.30", "PKO-REF-2"))))
            val archive = source.exportJson()

            val firstImport = target.importJson(archive)
            assertEquals(BatchImportSummary(inserted = 2, duplicates = 0), firstImport)
            assertEquals(source.getEntries(), target.getEntries())

            val secondImport = target.importJson(archive)
            assertEquals(BatchImportSummary(inserted = 0, duplicates = 2), secondImport)
            assertEquals(2, target.getEntries().size)
        } finally {
            source.close()
            target.close()
        }
    }

    @Test
    fun conflictingReferenceIsReportedAndDoesNotChangeDatabase() = runBlocking {
        val repository = PaymentLedgerRepository(context, conflictDatabase)
        try {
            repository.importJson(PaymentLedgerJsonCodec.encode(listOf(entry())))
            val before = repository.getEntries()
            val conflictingArchive = PaymentLedgerJsonCodec.encode(listOf(entry("750.00")))

            val preview = repository.previewJson(conflictingArchive)
            assertEquals(1, preview.conflicts.size)
            assertEquals(0, preview.newEntries.size)

            val result = runCatching { repository.importJson(conflictingArchive) }
            assertTrue("Import z konfliktem musi zostać odrzucony.", result.isFailure)
            assertEquals(before, repository.getEntries())
        } finally {
            repository.close()
        }
    }
}
