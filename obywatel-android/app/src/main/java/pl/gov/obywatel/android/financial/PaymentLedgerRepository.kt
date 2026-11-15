// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Warstwa dostępu do rejestru. Wszystkie operacje SQLite wykonuje na Dispatchers.IO. */
class PaymentLedgerRepository(
    context: Context,
    databaseName: String = PaymentLedgerDatabase.DATABASE_NAME
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val fileProviderAuthority = "${appContext.packageName}.ocrfiles"
    private val database = PaymentLedgerDatabase(appContext, databaseName)
    private val attachmentStorage = DonationAttachmentStorage(appContext)

    suspend fun getEntries(): List<BankStatementEntry> = withContext(Dispatchers.IO) {
        database.readAll()
    }

    suspend fun getAssets(): List<pl.gov.obywatel.android.Asset> = withContext(Dispatchers.IO) {
        database.readAssets()
    }

    suspend fun saveAsset(asset: pl.gov.obywatel.android.Asset) = withContext(Dispatchers.IO) {
        database.saveAsset(asset)
    }

    suspend fun getAccountScopes(): List<PaymentAccountScope> = withContext(Dispatchers.IO) {
        database.getAccountScopes()
    }

    suspend fun saveAccountScope(accountScopeId: String, displayName: String) = withContext(Dispatchers.IO) {
        database.saveAccountScope(accountScopeId, displayName)
    }

    suspend fun saveFinancialTransaction(
        transaction: pl.gov.obywatel.android.FinancialTransaction,
        accountingEntryIds: Set<String> = emptySet()
    ) = withContext(Dispatchers.IO) {
        database.saveFinancialTransaction(transaction, accountingEntryIds)
    }

    suspend fun saveCashDonation(donation: pl.gov.obywatel.android.CashDonation) =
        withContext(Dispatchers.IO) { database.saveCashDonation(donation) }

    suspend fun updateCashDonation(donation: pl.gov.obywatel.android.CashDonation) =
        withContext(Dispatchers.IO) { database.updateCashDonation(donation) }

    suspend fun getCashDonations(): List<pl.gov.obywatel.android.CashDonation> =
        withContext(Dispatchers.IO) { database.readCashDonations() }

    /**
     * Jedyna publiczna ścieżka usuwania darowizny. Usuwa także prywatne pliki
     * załączników; bezpośrednie usunięcie rekordu z SQLite pozostawiałoby pliki osierocone.
     */
    suspend fun deleteCashDonation(id: String) = deleteCashDonationWithAttachments(id)


    suspend fun getDonationAttachments(): List<pl.gov.obywatel.android.DonationAttachment> =
        withContext(Dispatchers.IO) { database.readDonationAttachments() }

    /** Tworzy ograniczone URI content:// po ponownej weryfikacji pliku i jego SHA-256. */
    suspend fun getDonationAttachmentUri(id: String): Uri = withContext(Dispatchers.IO) {
        require(id.isNotBlank()) { "Identyfikator załącznika jest wymagany." }
        val attachment = database.readDonationAttachments().firstOrNull { it.id == id }
            ?: throw IllegalStateException("Nie znaleziono załącznika.")
        attachmentStorage.contentUri(attachment, fileProviderAuthority)
    }

    suspend fun attachDonationDocument(
        donationId: String,
        uri: Uri
    ): pl.gov.obywatel.android.DonationAttachment = withContext(Dispatchers.IO) {
        val attachment = attachmentStorage.import(donationId, uri)
        try {
            database.saveDonationAttachment(attachment)
            attachment
        } catch (error: Exception) {
            attachmentStorage.delete(attachment.storedFileName)
            throw error
        }
    }

    suspend fun deleteDonationAttachment(id: String) = withContext(Dispatchers.IO) {
        val attachment = database.deleteDonationAttachment(id)
        attachmentStorage.delete(attachment.storedFileName)
    }

    suspend fun deleteCashDonationWithAttachments(id: String) = withContext(Dispatchers.IO) {
        val attachments = database.readDonationAttachments().filter { it.donationId == id }
        database.deleteCashDonation(id)
        attachments.forEach { attachmentStorage.delete(it.storedFileName) }
    }

    suspend fun getFinancialTransactions(): List<StoredFinancialTransaction> =
        withContext(Dispatchers.IO) {
            database.readFinancialTransactions()
        }

    suspend fun previewJson(json: String): BankImportPreview = withContext(Dispatchers.IO) {
        val incoming = PaymentLedgerJsonCodec.decode(json)
        BankImportDeduplicator.preview(incoming, database.readAll())
    }

    /**
     * Zatwierdza import tylko wtedy, gdy podgląd nie zawiera konfliktów.
     * Duplikaty są pomijane, a cała nowa paczka zapisywana transakcyjnie.
     */
    /** Podgląd pełnego archiwum; liczy nowe wpisy, duplikaty i konflikty księgowe. */
    suspend fun previewArchive(json: String): ArchiveImportPreview = withContext(Dispatchers.IO) {
        val archive = PaymentLedgerJsonCodec.decodeArchive(json)
        val existingTransactions = database.readFinancialTransactions().associateBy { it.transaction.id }
        val existingDonations = database.readCashDonations().associateBy { it.id }
        val bankPreview = database.previewArchiveEntries(archive.entries)
        val transactionConflicts = archive.transactions.count { incoming ->
            val existing = existingTransactions[incoming.transaction.id]
            existing != null && existing != incoming
        }
        val donationConflicts = archive.donations.count { incoming ->
            val existing = existingDonations[incoming.id]
            existing != null && !sameDonationIdentity(existing, incoming)
        }
        ArchiveImportPreview(
            newBankEntries = bankPreview.newEntries.size,
            duplicateBankEntries = bankPreview.duplicates.size,
            bankConflicts = bankPreview.conflicts.size,
            newTransactions = archive.transactions.count { existingTransactions[it.transaction.id] == null },
            transactionConflicts = transactionConflicts,
            newDonations = archive.donations.count { existingDonations[it.id] == null },
            donationConflicts = donationConflicts
        )
    }

    private fun sameDonationIdentity(
        existing: pl.gov.obywatel.android.CashDonation,
        incoming: pl.gov.obywatel.android.CashDonation
    ): Boolean =
        existing.receivedAt == incoming.receivedAt &&
            existing.amount.compareTo(incoming.amount) == 0 &&
            existing.currencyCode == incoming.currencyCode &&
            existing.beneficiaryOrganizationName.trim() == incoming.beneficiaryOrganizationName.trim() &&
            existing.purpose.trim() == incoming.purpose.trim()

    suspend fun importJson(json: String): BatchImportSummary = withContext(Dispatchers.IO) {
        val archive = PaymentLedgerJsonCodec.decodeArchive(json)
        database.importArchive(archive)
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        PaymentLedgerJsonCodec.encode(
            database.readAll(),
            database.readFinancialTransactions(),
            database.readCashDonations()
        )
    }

    override fun close() {
        database.close()
    }
}

data class ArchiveImportPreview(
    val newBankEntries: Int,
    val duplicateBankEntries: Int,
    val bankConflicts: Int,
    val newTransactions: Int,
    val transactionConflicts: Int,
    val newDonations: Int,
    val donationConflicts: Int
) {
    val hasConflicts: Boolean get() =
        bankConflicts > 0 || transactionConflicts > 0 || donationConflicts > 0
}
