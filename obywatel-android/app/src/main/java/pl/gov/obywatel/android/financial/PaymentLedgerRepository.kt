// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Warstwa dostępu do rejestru. Wszystkie operacje SQLite wykonuje na Dispatchers.IO. */
class PaymentLedgerRepository(context: Context) : AutoCloseable {
    private val database = PaymentLedgerDatabase(context.applicationContext)

    suspend fun getEntries(): List<BankStatementEntry> = withContext(Dispatchers.IO) {
        database.readAll()
    }

    suspend fun previewJson(json: String): BankImportPreview = withContext(Dispatchers.IO) {
        val incoming = PaymentLedgerJsonCodec.decode(json)
        BankImportDeduplicator.preview(incoming, database.readAll())
    }

    /**
     * Zatwierdza import tylko wtedy, gdy podgląd nie zawiera konfliktów.
     * Duplikaty są pomijane, a cała nowa paczka zapisywana transakcyjnie.
     */
    suspend fun importJson(json: String): BatchImportSummary = withContext(Dispatchers.IO) {
        val incoming = PaymentLedgerJsonCodec.decode(json)
        val preview = BankImportDeduplicator.preview(incoming, database.readAll())
        require(preview.conflicts.isEmpty()) {
            "Import zawiera ${preview.conflicts.size} konfliktów referencji. Wymagana weryfikacja."
        }
        val result = database.importBatch(preview.newEntries)
        BatchImportSummary(
            inserted = result.inserted,
            duplicates = result.duplicates + preview.duplicates.size
        )
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        PaymentLedgerJsonCodec.encode(database.readAll())
    }

    override fun close() {
        database.close()
    }
}
