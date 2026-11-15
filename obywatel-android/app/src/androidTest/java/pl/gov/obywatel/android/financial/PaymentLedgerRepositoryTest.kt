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
    fun accountAliasesAreAvailableThroughRepositoryAndPersistAcrossReopen() = runBlocking {
        val repository = PaymentLedgerRepository(context, sourceDatabase)
        try {
            repository.saveAccountScope("daily-account", "Rachunek bieżący")
            repository.saveAccountScope("savings-account", "Rachunek oszczędnościowy")
            val scopes = repository.getAccountScopes()
            assertTrue(scopes.any {
                it.accountScopeId == "daily-account" && it.displayName == "Rachunek bieżący"
            })
            assertTrue(scopes.any {
                it.accountScopeId == "savings-account" && it.displayName == "Rachunek oszczędnościowy"
            })
        } finally {
            repository.close()
        }

        val reopened = PaymentLedgerRepository(context, sourceDatabase)
        try {
            assertEquals(
                true,
                reopened.getAccountScopes().any { it.accountScopeId == "savings-account" }
            )
        } finally {
            reopened.close()
        }
    }

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
            val conflictingArchive = PaymentLedgerJsonCodec.encode(
                listOf(entry("10.00", "PKO-REF-NEW"), entry("750.00"))
            )

            val preview = repository.previewJson(conflictingArchive)
            assertEquals(1, preview.conflicts.size)
            assertEquals(1, preview.newEntries.size)

            val result = runCatching { repository.importJson(conflictingArchive) }
            assertTrue("Import z konfliktem musi zostać odrzucony.", result.isFailure)
            assertEquals(before, repository.getEntries())
        } finally {
            repository.close()
        }
    }
    @Test
    fun completeArchiveRoundTripsFinancialTransactionsAndFeesAtomically() = runBlocking {
        val source = PaymentLedgerRepository(context, sourceDatabase)
        val target = PaymentLedgerRepository(context, targetDatabase)
        try {
            val bankEntry = entry("-8000.00").copy(
                operationType = "WYMIANA W KANTORZE - OBCIĄŻENIE"
            )
            source.importJson(PaymentLedgerJsonCodec.encode(listOf(bankEntry)))
            val tx = pl.gov.obywatel.android.FinancialTransaction(
                id = "archive-fx-1",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.EXCHANGE,
                fromAssetCode = "USD", fromAmount = BigDecimal("8000.00"),
                toAssetCode = "CHF", toAmount = BigDecimal("6183.20"),
                fee = pl.gov.obywatel.android.Fee("CHF", BigDecimal("5.25"))
            )
            source.saveFinancialTransaction(tx, setOf(bankEntry.accountingEntryId()))
            val archive = source.exportJson()
            assertTrue(archive.contains("transactions"))
            assertTrue(archive.contains("transactions"))
            assertEquals(BatchImportSummary(1, 0), target.importJson(archive))
            val imported = target.getFinancialTransactions().single()
            assertEquals(tx.id, imported.transaction.id)
            assertEquals(0, BigDecimal("5.25").compareTo(imported.transaction.fee!!.amount))
            assertEquals(listOf(bankEntry.accountingEntryId()), imported.accountingEntryIds)
            assertEquals(BatchImportSummary(0, 1), target.importJson(archive))
            assertEquals(1, target.getFinancialTransactions().size)
        } finally {
            source.close()
            target.close()
        }
    }

    @Test
    fun invalidFinancialLinkRollsBackEntireArchiveImport() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val bankEntry = entry()
            val tx = pl.gov.obywatel.android.FinancialTransaction(
                id = "archive-invalid-link",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null, fromAmount = null,
                toAssetCode = "USD", toAmount = BigDecimal("749.33")
            )
            val archive = PaymentLedgerJsonCodec.encode(
                listOf(bankEntry),
                listOf(StoredFinancialTransaction(tx, listOf("missing-entry-id")))
            )
            val result = runCatching { target.importJson(archive) }
            assertTrue(result.isFailure)
            assertEquals(emptyList<BankStatementEntry>(), target.getEntries())
            assertEquals(emptyList<StoredFinancialTransaction>(), target.getFinancialTransactions())
        } finally {
            target.close()
        }
    }

    @Test
    fun donationsAreExportedImportedAndDeduplicatedIdempotently() = runBlocking {
        val source = PaymentLedgerRepository(context, sourceDatabase)
        val target = PaymentLedgerRepository(context, targetDatabase)
        try {
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "donation-json-1",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("250.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Zakup książek",
                isAnonymous = true
            )
            source.saveCashDonation(donation)
            val archive = source.exportJson()
            assertTrue(archive.contains("donations"))
            assertEquals(BatchImportSummary(1, 0), target.importJson(archive))
            assertEquals(donation.id, target.getCashDonations().single().id)
            assertEquals(BatchImportSummary(0, 1), target.importJson(archive))
            assertEquals(1, target.getCashDonations().size)
        } finally {
            source.close()
            target.close()
        }
    }

    @Test
    fun sameDonationIdWithDifferentPurposeIsRejectedAndPreservesExistingRecord() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val original = pl.gov.obywatel.android.CashDonation(
                id = "donation-conflict",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("250.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Cel pierwotny"
            )
            target.saveCashDonation(original)
            val changed = original.copy(purpose = "Inny cel")
            val archive = PaymentLedgerJsonCodec.encode(emptyList(), emptyList(), listOf(changed))
            val result = runCatching { target.importJson(archive) }
            assertTrue(result.isFailure)
            assertEquals("Cel pierwotny", target.getCashDonations().single().purpose)
        } finally {
            target.close()
        }
    }

    @Test
    fun deletingDonationThroughPrimaryRepositoryMethodAlsoRemovesAttachmentMetadataAndPrivateFile() = runBlocking {
        val donation = pl.gov.obywatel.android.CashDonation(
            id = "donation-delete-with-attachment",
            receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
            amount = BigDecimal("250.00"),
            currencyCode = "PLN",
            beneficiaryOrganizationName = "Fundacja Aria",
            purpose = "Test usuwania załącznika"
        )
        val storedName = "11111111-1111-4111-8111-111111111111.pdf"
        val attachment = pl.gov.obywatel.android.DonationAttachment(
            id = "attachment-delete-test",
            donationId = donation.id,
            fileName = "pokwitowanie.pdf",
            mimeType = "application/pdf",
            sizeBytes = 5L,
            sha256 = "a".repeat(64),
            storedFileName = storedName,
            addedAt = java.time.Instant.parse("2026-11-14T12:01:00Z")
        )
        val database = PaymentLedgerDatabase(context, conflictDatabase)
        database.saveCashDonation(donation)
        database.saveDonationAttachment(attachment)
        database.close()

        val attachmentDirectory = java.io.File(context.filesDir, "donation-attachments")
        assertTrue(attachmentDirectory.exists() || attachmentDirectory.mkdirs())
        val attachmentFile = java.io.File(attachmentDirectory, storedName)
        attachmentFile.writeBytes("%PDF-".toByteArray())

        val repository = PaymentLedgerRepository(context, conflictDatabase)
        try {
            repository.deleteCashDonation(donation.id)

            assertTrue(repository.getCashDonations().isEmpty())
            assertTrue(repository.getDonationAttachments().isEmpty())
            assertTrue("Plik prywatny powinien zostać usunięty.", !attachmentFile.exists())
        } finally {
            repository.close()
            attachmentFile.delete()
            attachmentDirectory.delete()
        }
    }

}
