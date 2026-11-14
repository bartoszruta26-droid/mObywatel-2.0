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

    /**
     * Importuje wersjonowany JSON atomowo. Duplikaty są pomijane, a liczba nowych
     * zapisów i duplikatów jest zwracana do warstwy prezentacji.
     */
    suspend fun importJson(json: String): BatchImportSummary = withContext(Dispatchers.IO) {
        val incoming = PaymentLedgerJsonCodec.decode(json)
        val classified = BankImportDeduplicator.classify(
            incoming = incoming,
            existingAccountingEntryIds = database.accountingEntryIds()
        )
        val result = database.importBatch(classified.newEntries)
        BatchImportSummary(
            inserted = result.inserted,
            duplicates = result.duplicates + classified.duplicates.size
        )
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        PaymentLedgerJsonCodec.encode(database.readAll())
    }

    override fun close() {
        database.close()
    }
}
