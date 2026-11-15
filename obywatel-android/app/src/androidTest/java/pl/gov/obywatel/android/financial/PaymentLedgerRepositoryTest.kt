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
    fun currencyConversionHistoryIsAvailableThroughRepositoryAndPersistsAfterReopen() = runBlocking {
        val record = CurrencyConversionRecord(
            id = "repository-conversion-1",
            convertedAt = java.time.Instant.parse("2026-11-15T10:00:00Z"),
            sourceAmount = BigDecimal("125.50"),
            sourceCurrencyCode = "USD",
            targetAmount = BigDecimal("501.25"),
            targetCurrencyCode = "PLN",
            appliedRate = BigDecimal("3.99402390438247"),
            rateDate = LocalDate.parse("2026-11-13"),
            rateSource = "NBP_TABLE_A"
        )
        val repository = PaymentLedgerRepository(context, sourceDatabase)
        try {
            repository.saveCurrencyConversion(record)
            assertEquals(listOf(record), repository.getCurrencyConversionHistory())
        } finally {
            repository.close()
        }

        val reopened = PaymentLedgerRepository(context, sourceDatabase)
        try {
            assertEquals(listOf(record), reopened.getCurrencyConversionHistory())
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

    @Test
    fun archivePreviewUsesNumericTransactionIdentityAndDetectsChangedAmounts() = runBlocking {
        val repository = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val original = pl.gov.obywatel.android.FinancialTransaction(
                id = "preview-transaction-id",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            repository.importJson(PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(original, emptyList()))
            ))

            val sameValueDifferentScale = original.copy(toAmount = BigDecimal("100"))
            val samePreview = repository.previewArchive(PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(sameValueDifferentScale, emptyList()))
            ))
            assertEquals(0, samePreview.transactionConflicts)

            val changed = original.copy(toAmount = BigDecimal("101.00"))
            val changedPreview = repository.previewArchive(PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(changed, emptyList()))
            ))
            assertEquals(1, changedPreview.transactionConflicts)
        } finally {
            repository.close()
        }
    }

    @Test
    fun sameDonationIdWithChangedMetadataIsReportedAsConflictAndPreservesRecord() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val original = pl.gov.obywatel.android.CashDonation(
                id = "donation-metadata-conflict",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("250.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Zakup książek",
                donorName = "Darczyńca A",
                isAnonymous = false,
                campaignName = "Jesienna zbiórka",
                collectedBy = "Osoba A",
                receiptNumber = "KW-001",
                note = "Oryginalna notatka"
            )
            val changes: List<(pl.gov.obywatel.android.CashDonation) -> pl.gov.obywatel.android.CashDonation> =
                listOf(
                    { it.copy(donorName = "Darczyńca B") },
                    { it.copy(isAnonymous = true) },
                    { it.copy(campaignName = "Inna kampania") },
                    { it.copy(collectedBy = "Osoba B") },
                    { it.copy(receiptNumber = "KW-002") },
                    { it.copy(custodyStatus = pl.gov.obywatel.android.DonationCustodyStatus.DEPOSITED) },
                    {
                        it.copy(
                            custodyStatus = pl.gov.obywatel.android.DonationCustodyStatus.TRANSFERRED,
                            transferredAt = java.time.Instant.parse("2026-11-15T12:00:00Z"),
                            transferredAmount = BigDecimal("250.00")
                        )
                    },
                    { it.copy(note = "Zmieniona notatka") },
                    { it.copy(source = pl.gov.obywatel.android.FinancialRecordSource.JSON_IMPORT) }
                )

            changes.forEachIndexed { index, change ->
                val saved = original.copy(id = "donation-metadata-conflict-$index")
                target.saveCashDonation(saved)
                val changed = change(saved)
                val archive = PaymentLedgerJsonCodec.encode(emptyList(), emptyList(), listOf(changed))

                val preview = target.previewArchive(archive)
                assertEquals("Podgląd musi wykryć zmianę metadanych nr $index.", 1, preview.donationConflicts)
                assertTrue(preview.hasConflicts)
                val result = runCatching { target.importJson(archive) }

                assertTrue("Zmiana metadanych darowizny nr $index musi być konfliktem.", result.isFailure)
                val preserved = target.getCashDonations().single { it.id == saved.id }
                assertEquals(saved.copy(
                    amount = saved.amount.stripTrailingZeros().let { BigDecimal(it.toPlainString()) },
                    transferredAmount = saved.transferredAmount?.stripTrailingZeros()
                        ?.let { BigDecimal(it.toPlainString()) }
                ), preserved)
            }
        } finally {
            target.close()
        }
    }

    @Test
    fun donationImportTreatsEquivalentBigDecimalScaleAsDuplicate() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val original = pl.gov.obywatel.android.CashDonation(
                id = "donation-scale-idempotent",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("250.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Zakup książek",
                transferredAt = java.time.Instant.parse("2026-11-15T12:00:00Z"),
                transferredAmount = BigDecimal("50.00"),
                custodyStatus = pl.gov.obywatel.android.DonationCustodyStatus.PARTIALLY_TRANSFERRED
            )
            target.saveCashDonation(original)
            val equivalent = original.copy(
                amount = BigDecimal("250.000"),
                transferredAmount = BigDecimal("50.000")
            )
            val archive = PaymentLedgerJsonCodec.encode(emptyList(), emptyList(), listOf(equivalent))

            val preview = target.previewArchive(archive)
            assertEquals(0, preview.donationConflicts)
            assertEquals(0, preview.newDonations)
            assertEquals(1, target.importJson(archive).duplicates)

            val preserved = target.getCashDonations().single()
            assertEquals(original.id, preserved.id)
            assertEquals(0, original.amount.compareTo(preserved.amount))
            assertEquals(0, original.transferredAmount!!.compareTo(preserved.transferredAmount))
        } finally {
            target.close()
        }
    }


    @Test
    fun sameTransactionIdWithChangedDataIsRejectedAndPreservesExistingRecord() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val original = pl.gov.obywatel.android.FinancialTransaction(
                id = "stable-transaction-id",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00"),
                parseStatus = pl.gov.obywatel.android.FinancialParseStatus.VERIFIED
            )
            val originalArchive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(original, emptyList()))
            )
            target.importJson(originalArchive)
            assertEquals(original.id, target.getFinancialTransactions().single().transaction.id)
            assertEquals(0, original.toAmount!!.compareTo(target.getFinancialTransactions().single().transaction.toAmount))

            val changed = original.copy(toAmount = BigDecimal("999.00"))
            val changedArchive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(changed, emptyList()))
            )
            val result = runCatching { target.importJson(changedArchive) }

            assertTrue("Zmiana danych pod istniejącym identyfikatorem musi być konfliktem.", result.isFailure)
            val preserved = target.getFinancialTransactions().single().transaction
            assertEquals(original.id, preserved.id)
            assertEquals(0, original.toAmount!!.compareTo(preserved.toAmount))
        } finally {
            target.close()
        }
    }

    @Test
    fun identicalTransactionArchiveCanBeImportedRepeatedlyWithoutChangingRecord() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val transaction = pl.gov.obywatel.android.FinancialTransaction(
                id = "idempotent-transaction-id",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.000"),
                parseStatus = pl.gov.obywatel.android.FinancialParseStatus.VERIFIED
            )
            val archive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(transaction, emptyList()))
            )

            target.importJson(archive)
            target.importJson(archive)

            assertEquals(1, target.getFinancialTransactions().size)
            assertEquals(0, BigDecimal("100").compareTo(
                target.getFinancialTransactions().single().transaction.toAmount
            ))
        } finally {
            target.close()
        }
    }



    @Test
    fun archivePreviewAndImportDeduplicateIdenticalRepeatedTransactionAndDonationIds() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val transaction = pl.gov.obywatel.android.FinancialTransaction(
                id = "repeated-archive-transaction",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "repeated-archive-donation",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("25.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Wsparcie"
            )
            val archive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(
                    StoredFinancialTransaction(transaction, emptyList()),
                    StoredFinancialTransaction(transaction.copy(toAmount = BigDecimal("100.000")), emptyList())
                ),
                listOf(donation, donation.copy(amount = BigDecimal("25.000")))
            )

            val preview = target.previewArchive(archive)
            assertEquals(1, preview.newTransactions)
            assertEquals(1, preview.duplicateTransactions)
            assertEquals(0, preview.transactionConflicts)
            assertEquals(1, preview.newDonations)
            assertEquals(1, preview.duplicateDonations)
            assertEquals(0, preview.donationConflicts)
            assertTrue(!preview.hasConflicts)

            assertEquals(BatchImportSummary(1, 1), target.importJson(archive))
            assertEquals(1, target.getFinancialTransactions().size)
            assertEquals(1, target.getCashDonations().size)
        } finally {
            target.close()
        }
    }

    @Test
    fun archivePreviewDetectsConflictingRepeatedIdsAndImportLeavesDatabaseUnchanged() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val transaction = pl.gov.obywatel.android.FinancialTransaction(
                id = "conflicting-repeated-transaction",
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "conflicting-repeated-donation",
                receivedAt = java.time.Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("25.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Cel A"
            )
            val archive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(
                    StoredFinancialTransaction(transaction, emptyList()),
                    StoredFinancialTransaction(transaction.copy(toAmount = BigDecimal("101.00")), emptyList())
                ),
                listOf(donation, donation.copy(purpose = "Cel B"))
            )

            val preview = target.previewArchive(archive)
            assertEquals(1, preview.transactionConflicts)
            assertEquals(1, preview.donationConflicts)
            assertTrue(preview.hasConflicts)
            assertTrue(runCatching { target.importJson(archive) }.isFailure)
            assertTrue(target.getFinancialTransactions().isEmpty())
            assertTrue(target.getCashDonations().isEmpty())
        } finally {
            target.close()
        }
    }

    @Test
    fun archivePreviewDetectsOneBankEntryLinkedToTwoDifferentTransactions() = runBlocking {
        val target = PaymentLedgerRepository(context, conflictDatabase)
        try {
            val bankEntry = entry("100.00", "SHARED-ENTRY").copy(
                currencyCode = "USD",
                operationType = "UZNANIE"
            )
            fun deposit(id: String) = pl.gov.obywatel.android.FinancialTransaction(
                id = id,
                executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
                type = pl.gov.obywatel.android.FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            val archive = PaymentLedgerJsonCodec.encode(
                listOf(bankEntry),
                listOf(
                    StoredFinancialTransaction(deposit("link-owner-a"), listOf(bankEntry.accountingEntryId())),
                    StoredFinancialTransaction(deposit("link-owner-b"), listOf(bankEntry.accountingEntryId()))
                )
            )

            val preview = target.previewArchive(archive)
            assertEquals(1, preview.transactionLinkConflicts)
            assertTrue(preview.hasConflicts)
            assertTrue(runCatching { target.importJson(archive) }.isFailure)
            assertTrue(target.getEntries().isEmpty())
            assertTrue(target.getFinancialTransactions().isEmpty())
        } finally {
            target.close()
        }
    }

}
