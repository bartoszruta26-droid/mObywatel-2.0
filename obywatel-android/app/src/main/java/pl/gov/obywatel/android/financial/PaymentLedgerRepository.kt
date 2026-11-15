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
        val existingTransactionList = database.readFinancialTransactions()
        val existingTransactions = existingTransactionList.associateBy { it.transaction.id }
        val existingDonations = database.readCashDonations().associateBy { it.id }
        val existingEntries = database.readAll()
        val bankPreview = BankImportDeduplicator.preview(archive.entries, existingEntries)

        val transactionCandidates = existingTransactions.toMutableMap()
        var newTransactions = 0
        var duplicateTransactions = 0
        var transactionConflicts = 0
        archive.transactions.forEach { incoming ->
            val id = incoming.transaction.id
            val previous = transactionCandidates[id]
            when {
                previous == null -> {
                    transactionCandidates[id] = incoming
                    newTransactions++
                }
                sameStoredTransaction(previous, incoming) -> duplicateTransactions++
                else -> transactionConflicts++
            }
        }

        val donationCandidates = existingDonations.toMutableMap()
        var newDonations = 0
        var duplicateDonations = 0
        var donationConflicts = 0
        archive.donations.forEach { incoming ->
            val previous = donationCandidates[incoming.id]
            when {
                previous == null -> {
                    donationCandidates[incoming.id] = incoming
                    newDonations++
                }
                sameDonationIdentity(previous, incoming) -> duplicateDonations++
                else -> donationConflicts++
            }
        }

        // Podgląd sprawdza te same warunki powiązań, które wymusza SQLite przy zatwierdzaniu.
        val knownEntries = (existingEntries + archive.entries).associateBy { it.accountingEntryId() }
        val linkOwners = mutableMapOf<String, String>()
        existingTransactionList.forEach { stored ->
            stored.accountingEntryIds.forEach { entryId -> linkOwners[entryId] = stored.transaction.id }
        }
        var transactionLinkConflicts = 0
        archive.transactions.distinctBy { it.transaction.id }.forEach { stored ->
            val tx = stored.transaction
            stored.accountingEntryIds.forEach { entryId ->
                val entry = knownEntries[entryId]
                val compatible = entry != null && (
                    (tx.fromAssetCode == entry.currencyCode && entry.signedAmount.signum() < 0 &&
                        tx.fromAmount?.compareTo(entry.signedAmount.abs()) == 0) ||
                    (tx.toAssetCode == entry.currencyCode && entry.signedAmount.signum() > 0 &&
                        tx.toAmount?.compareTo(entry.signedAmount.abs()) == 0) ||
                    (tx.fee?.assetCode == entry.currencyCode && entry.signedAmount.signum() < 0 &&
                        tx.fee.amount.compareTo(entry.signedAmount.abs()) == 0)
                )
                if (!compatible) transactionLinkConflicts++
                val owner = linkOwners[entryId]
                if (owner != null && owner != tx.id) transactionLinkConflicts++
                else linkOwners[entryId] = tx.id
            }
        }

        ArchiveImportPreview(
            newBankEntries = bankPreview.newEntries.size,
            duplicateBankEntries = bankPreview.duplicates.size,
            bankConflicts = bankPreview.conflicts.size,
            newTransactions = newTransactions,
            duplicateTransactions = duplicateTransactions,
            transactionConflicts = transactionConflicts,
            newDonations = newDonations,
            duplicateDonations = duplicateDonations,
            donationConflicts = donationConflicts,
            transactionLinkConflicts = transactionLinkConflicts
        )
    }

    private fun sameStoredTransaction(
        existing: StoredFinancialTransaction,
        incoming: StoredFinancialTransaction
    ): Boolean {
        val first = existing.transaction
        val second = incoming.transaction
        fun sameAmount(a: java.math.BigDecimal?, b: java.math.BigDecimal?): Boolean =
            if (a == null || b == null) a == b else a.compareTo(b) == 0
        val sameFee = when {
            first.fee == null || second.fee == null -> first.fee == second.fee
            else -> first.fee.assetCode == second.fee.assetCode &&
                first.fee.amount.compareTo(second.fee.amount) == 0
        }
        return first.id == second.id &&
            first.executedAt == second.executedAt &&
            first.type == second.type &&
            first.fromAssetCode == second.fromAssetCode &&
            sameAmount(first.fromAmount, second.fromAmount) &&
            first.toAssetCode == second.toAssetCode &&
            sameAmount(first.toAmount, second.toAmount) &&
            sameFee &&
            first.source == second.source &&
            first.externalId == second.externalId &&
            first.rawDescription == second.rawDescription &&
            first.parseStatus == second.parseStatus &&
            existing.accountingEntryIds.toSet() == incoming.accountingEntryIds.toSet()
    }

    private fun sameDonationIdentity(
        existing: pl.gov.obywatel.android.CashDonation,
        incoming: pl.gov.obywatel.android.CashDonation
    ): Boolean =
        existing.receivedAt == incoming.receivedAt &&
            existing.amount.compareTo(incoming.amount) == 0 &&
            existing.currencyCode == incoming.currencyCode &&
            existing.beneficiaryOrganizationName.trim() == incoming.beneficiaryOrganizationName.trim() &&
            existing.purpose.trim() == incoming.purpose.trim() &&
            existing.donorName == incoming.donorName &&
            existing.isAnonymous == incoming.isAnonymous &&
            existing.campaignName == incoming.campaignName &&
            existing.collectedBy == incoming.collectedBy &&
            existing.receiptNumber == incoming.receiptNumber &&
            existing.custodyStatus == incoming.custodyStatus &&
            existing.transferredAt == incoming.transferredAt &&
            sameNullableAmount(existing.transferredAmount, incoming.transferredAmount) &&
            existing.note == incoming.note &&
            existing.source == incoming.source

    private fun sameNullableAmount(first: java.math.BigDecimal?, second: java.math.BigDecimal?): Boolean =
        if (first == null || second == null) first == second else first.compareTo(second) == 0

    suspend fun importJson(json: String): BatchImportSummary = withContext(Dispatchers.IO) {
        val archive = PaymentLedgerJsonCodec.decodeArchive(json)
        database.importArchive(archive)
    }

    /** Eksportuje raport CSV z transakcji i darowizn bez ponownego sumowania wpisów bankowych. */
    suspend fun exportFinancialReportCsv(period: FinancialReportPeriod): String = withContext(Dispatchers.IO) {
        val report = FinancialReportBuilder.build(
            period = period,
            transactions = database.readFinancialTransactions(),
            donations = database.readCashDonations(),
            assets = database.readAssets()
        )
        FinancialReportCsv.encode(report)
    }

    /** Generuje wielostronicowy PDF poza głównym wątkiem. */
    suspend fun exportFinancialReportPdf(period: FinancialReportPeriod): ByteArray = withContext(Dispatchers.IO) {
        val report = FinancialReportBuilder.build(
            period = period,
            transactions = database.readFinancialTransactions(),
            donations = database.readCashDonations(),
            assets = database.readAssets()
        )
        FinancialReportPdf.encode(report)
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
    val duplicateTransactions: Int,
    val transactionConflicts: Int,
    val newDonations: Int,
    val duplicateDonations: Int,
    val donationConflicts: Int,
    val transactionLinkConflicts: Int
) {
    val hasConflicts: Boolean get() =
        bankConflicts > 0 || transactionConflicts > 0 ||
            donationConflicts > 0 || transactionLinkConflicts > 0
}
