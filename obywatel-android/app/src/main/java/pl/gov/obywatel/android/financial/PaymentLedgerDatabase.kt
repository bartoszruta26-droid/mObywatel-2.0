// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Lokalny magazyn zapisów wyciągów. Kwoty są przechowywane jako tekst dziesiętny,
 * aby zachować dokładność BigDecimal. Unikalny klucz zabezpiecza przed ponownym importem.
 */
class PaymentLedgerDatabase(
    context: Context,
    databaseName: String = DATABASE_NAME
) : SQLiteOpenHelper(
    context.applicationContext,
    databaseName,
    null,
    DATABASE_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ENTRIES (
                accounting_entry_id TEXT PRIMARY KEY NOT NULL,
                exchange_operation_id TEXT,
                account_scope_id TEXT NOT NULL,
                booking_date TEXT NOT NULL,
                currency_code TEXT NOT NULL,
                signed_amount TEXT NOT NULL,
                operation_type TEXT NOT NULL,
                bank_operation_reference TEXT,
                description TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX idx_payment_exchange_operation ON $TABLE_ENTRIES(exchange_operation_id)"
        )
        db.execSQL(
            "CREATE INDEX idx_payment_booking_date ON $TABLE_ENTRIES(booking_date)"
        )
        createAccountScopesTable(db)
        createFinancialTransactionTables(db)
        seedDefaultAccountScope(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migracje są sekwencyjne i zachowują istniejące zapisy księgowe.
        if (oldVersion < 2) {
            createAccountScopesTable(db)
            // Dotychczas używane aliasy zostają zarejestrowane bez zmiany identyfikatorów wpisów.
            db.execSQL(
                "INSERT OR IGNORE INTO $TABLE_ACCOUNT_SCOPES(account_scope_id, display_name) " +
                    "SELECT DISTINCT account_scope_id, account_scope_id FROM $TABLE_ENTRIES"
            )
            seedDefaultAccountScope(db)
        }
        if (oldVersion < 3) {
            // Odświeżamy wyliczany identyfikator operacji: od teraz dotyczy wyłącznie
            // operacji kantorowych i uwzględnia datę, lecz nie pseudonim rachunku.
            val updates = mutableListOf<Pair<String, String?>>()
            db.query(
                TABLE_ENTRIES,
                arrayOf(
                    "accounting_entry_id", "account_scope_id", "booking_date",
                    "currency_code", "signed_amount", "operation_type",
                    "bank_operation_reference", "description"
                ),
                null, null, null, null, null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val entry = BankStatementEntry(
                        accountScopeId = cursor.getString(1),
                        bookingDate = java.time.LocalDate.parse(cursor.getString(2)),
                        currencyCode = cursor.getString(3),
                        signedAmount = java.math.BigDecimal(cursor.getString(4)),
                        operationType = cursor.getString(5),
                        bankOperationReference = cursor.getString(6),
                        description = cursor.getString(7)
                    )
                    updates += cursor.getString(0) to entry.exchangeOperationId()
                }
            }
            updates.forEach { (accountingEntryId, operationId) ->
                val values = ContentValues().apply {
                    if (operationId == null) putNull("exchange_operation_id")
                    else put("exchange_operation_id", operationId)
                }
                db.update(
                    TABLE_ENTRIES,
                    values,
                    "accounting_entry_id = ?",
                    arrayOf(accountingEntryId)
                )
            }
        }
        if (oldVersion < 4) {
            createFinancialTransactionTables(db)
        }
    }

    private fun createFinancialTransactionTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS financial_transactions (
                transaction_id TEXT PRIMARY KEY NOT NULL,
                executed_at TEXT NOT NULL,
                transaction_type TEXT NOT NULL,
                from_asset_code TEXT,
                from_amount TEXT,
                to_asset_code TEXT,
                to_amount TEXT,
                source TEXT NOT NULL,
                external_id TEXT,
                raw_description TEXT,
                parse_status TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS financial_transaction_fees (
                transaction_id TEXT PRIMARY KEY NOT NULL
                    REFERENCES financial_transactions(transaction_id) ON DELETE CASCADE,
                asset_code TEXT NOT NULL,
                amount TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS financial_transaction_bank_entries (
                transaction_id TEXT NOT NULL
                    REFERENCES financial_transactions(transaction_id) ON DELETE CASCADE,
                accounting_entry_id TEXT NOT NULL UNIQUE
                    REFERENCES $TABLE_ENTRIES(accounting_entry_id) ON DELETE RESTRICT,
                PRIMARY KEY(transaction_id, accounting_entry_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_financial_transactions_executed_at " +
                "ON financial_transactions(executed_at)"
        )
    }

    private fun createAccountScopesTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNT_SCOPES (
                account_scope_id TEXT PRIMARY KEY NOT NULL,
                display_name TEXT NOT NULL UNIQUE
            )
            """.trimIndent()
        )
    }

    private fun seedDefaultAccountScope(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put("account_scope_id", DEFAULT_ACCOUNT_SCOPE_ID)
            put("display_name", DEFAULT_ACCOUNT_SCOPE_NAME)
        }
        db.insertWithOnConflict(TABLE_ACCOUNT_SCOPES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    /** Zwraca wyłącznie lokalne pseudonimy i ich etykiety — bez numerów rachunków. */
    fun getAccountScopes(): List<PaymentAccountScope> {
        val result = mutableListOf<PaymentAccountScope>()
        readableDatabase.query(
            TABLE_ACCOUNT_SCOPES,
            arrayOf("account_scope_id", "display_name"),
            null, null, null, null, "display_name COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += PaymentAccountScope(cursor.getString(0), cursor.getString(1))
            }
        }
        return result
    }

    /** Dodaje alias lub aktualizuje jego etykietę; nie zmienia identyfikatorów zapisów. */
    fun saveAccountScope(accountScopeId: String, displayName: String) {
        val normalizedId = accountScopeId.trim()
        val normalizedName = displayName.trim()
        require(normalizedId.matches(Regex("[A-Za-z0-9._-]{1,64}"))) {
            "Pseudonim rachunku musi mieć 1–64 znaki ASCII, cyfry, kropkę, myślnik lub podkreślenie."
        }
        require(normalizedName.length in 1..64) { "Nazwa aliasu musi mieć 1–64 znaki." }

        val db = writableDatabase
        db.beginTransaction()
        try {
            db.query(
                TABLE_ACCOUNT_SCOPES,
                arrayOf("account_scope_id"),
                "display_name = ? AND account_scope_id <> ?",
                arrayOf(normalizedName, normalizedId),
                null, null, null
            ).use { cursor ->
                require(!cursor.moveToFirst()) { "Taka nazwa aliasu jest już przypisana do innego pseudonimu." }
            }
            val values = ContentValues().apply {
                put("account_scope_id", normalizedId)
                put("display_name", normalizedName)
            }
            db.insertWithOnConflict(
                TABLE_ACCOUNT_SCOPES, null, values, SQLiteDatabase.CONFLICT_REPLACE
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Zapisuje paczkę atomowo. Istniejące identyfikatory są pomijane przez klucz PRIMARY KEY.
     * Dowolny błąd walidacji lub SQL wycofuje całą paczkę.
     */
    fun importBatch(entries: List<BankStatementEntry>): BatchImportSummary {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val result = insertEntries(db, entries)
            db.setTransactionSuccessful()
            return result
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Weryfikuje referencje i zapisuje paczkę w tej samej transakcji SQLite.
     * Zapobiega wyścigowi między podglądem a zatwierdzeniem importu.
     */
    fun importBatchRejectingReferenceConflicts(
        entries: List<BankStatementEntry>
    ): BatchImportSummary {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val preview = BankImportDeduplicator.preview(entries, readAll(db))
            require(preview.conflicts.isEmpty()) {
                "Import zawiera ${preview.conflicts.size} konfliktów referencji. Wymagana weryfikacja."
            }
            val inserted = insertEntries(db, preview.newEntries)
            db.setTransactionSuccessful()
            return BatchImportSummary(
                inserted = inserted.inserted,
                duplicates = inserted.duplicates + preview.duplicates.size
            )
        } finally {
            db.endTransaction()
        }
    }

    private fun insertEntries(
        db: SQLiteDatabase,
        entries: List<BankStatementEntry>
    ): BatchImportSummary {
        var inserted = 0
        var duplicates = 0
        entries.forEach { entry ->
            entry.validate()
            val values = ContentValues().apply {
                put("accounting_entry_id", entry.accountingEntryId())
                put("exchange_operation_id", entry.exchangeOperationId())
                put("account_scope_id", entry.accountScopeId.trim())
                put("booking_date", entry.bookingDate.toString())
                put("currency_code", entry.currencyCode)
                put("signed_amount", entry.signedAmount.stripTrailingZeros().toPlainString())
                put("operation_type", entry.operationType.trim())
                put("bank_operation_reference", entry.bankOperationReference?.trim())
                put("description", entry.description.trim())
            }
            val result = db.insertWithOnConflict(
                TABLE_ENTRIES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            if (result == -1L) duplicates++ else inserted++
        }
        return BatchImportSummary(inserted, duplicates)
    }

    /**
     * Zapisuje transakcję i jej prowizję oraz relacje do źródłowych zapisów bankowych
     * w jednej transakcji SQLite. Każdy zapis bankowy może być przypisany najwyżej
     * do jednej transakcji finansowej.
     */
    fun saveFinancialTransaction(
        transaction: pl.gov.obywatel.android.FinancialTransaction,
        accountingEntryIds: Set<String> = emptySet()
    ) {
        transaction.validate()
        require(accountingEntryIds.size == accountingEntryIds.toSet().size) {
            "Powtórzone identyfikatory zapisów bankowych."
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            accountingEntryIds.forEach { entryId ->
                db.query(
                    TABLE_ENTRIES,
                    arrayOf("accounting_entry_id"),
                    "accounting_entry_id = ?",
                    arrayOf(entryId),
                    null, null, null
                ).use { cursor ->
                    require(cursor.moveToFirst()) {
                        "Nie znaleziono powiązanego zapisu bankowego."
                    }
                }
            }
            db.delete(
                TABLE_TRANSACTION_BANK_ENTRIES,
                "transaction_id = ?",
                arrayOf(transaction.id)
            )
            db.delete(TABLE_TRANSACTION_FEES, "transaction_id = ?", arrayOf(transaction.id))
            val values = ContentValues().apply {
                put("transaction_id", transaction.id)
                put("executed_at", transaction.executedAt.toString())
                put("transaction_type", transaction.type.name)
                put("from_asset_code", transaction.fromAssetCode)
                put("from_amount", transaction.fromAmount?.stripTrailingZeros()?.toPlainString())
                put("to_asset_code", transaction.toAssetCode)
                put("to_amount", transaction.toAmount?.stripTrailingZeros()?.toPlainString())
                put("source", transaction.source.name)
                put("external_id", transaction.externalId)
                put("raw_description", transaction.rawDescription)
                put("parse_status", transaction.parseStatus.name)
            }
            db.insertWithOnConflict(
                TABLE_FINANCIAL_TRANSACTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE
            )
            transaction.fee?.let { fee ->
                val feeValues = ContentValues().apply {
                    put("transaction_id", transaction.id)
                    put("asset_code", fee.assetCode)
                    put("amount", fee.amount.stripTrailingZeros().toPlainString())
                }
                db.insertOrThrow(TABLE_TRANSACTION_FEES, null, feeValues)
            }
            accountingEntryIds.forEach { entryId ->
                val linkValues = ContentValues().apply {
                    put("transaction_id", transaction.id)
                    put("accounting_entry_id", entryId)
                }
                db.insertOrThrow(TABLE_TRANSACTION_BANK_ENTRIES, null, linkValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Odtwarza transakcje wraz z prowizjami i identyfikatorami źródłowych zapisów. */
    fun readFinancialTransactions(): List<StoredFinancialTransaction> {
        val db = readableDatabase
        val result = mutableListOf<StoredFinancialTransaction>()
        db.query(
            TABLE_FINANCIAL_TRANSACTIONS,
            arrayOf(
                "transaction_id", "executed_at", "transaction_type",
                "from_asset_code", "from_amount", "to_asset_code", "to_amount",
                "source", "external_id", "raw_description", "parse_status"
            ),
            null, null, null, null, "executed_at ASC, transaction_id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val fee = db.query(
                    TABLE_TRANSACTION_FEES,
                    arrayOf("asset_code", "amount"),
                    "transaction_id = ?",
                    arrayOf(id), null, null, null
                ).use { feeCursor ->
                    if (feeCursor.moveToFirst()) {
                        pl.gov.obywatel.android.Fee(
                            feeCursor.getString(0),
                            java.math.BigDecimal(feeCursor.getString(1))
                        )
                    } else null
                }
                val linkedIds = mutableListOf<String>()
                db.query(
                    TABLE_TRANSACTION_BANK_ENTRIES,
                    arrayOf("accounting_entry_id"),
                    "transaction_id = ?",
                    arrayOf(id), null, null, null
                ).use { links ->
                    while (links.moveToNext()) linkedIds += links.getString(0)
                }
                val transaction = pl.gov.obywatel.android.FinancialTransaction(
                    id = id,
                    executedAt = java.time.Instant.parse(cursor.getString(1)),
                    type = pl.gov.obywatel.android.FinancialTransactionType.valueOf(cursor.getString(2)),
                    fromAssetCode = cursor.getString(3),
                    fromAmount = cursor.getString(4)?.let(::java.math.BigDecimal),
                    toAssetCode = cursor.getString(5),
                    toAmount = cursor.getString(6)?.let(::java.math.BigDecimal),
                    fee = fee,
                    source = pl.gov.obywatel.android.FinancialRecordSource.valueOf(cursor.getString(7)),
                    externalId = cursor.getString(8),
                    rawDescription = cursor.getString(9),
                    parseStatus = pl.gov.obywatel.android.FinancialParseStatus.valueOf(cursor.getString(10))
                )
                result += StoredFinancialTransaction(transaction, linkedIds)
            }
        }
        return result
    }

    fun accountingEntryIds(): Set<String> {
        val ids = linkedSetOf<String>()
        readableDatabase.query(
            TABLE_ENTRIES,
            arrayOf("accounting_entry_id"),
            null,
            null,
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) ids += cursor.getString(0)
        }
        return ids
    }

    fun readAll(): List<BankStatementEntry> = readAll(readableDatabase)

    private fun readAll(db: SQLiteDatabase): List<BankStatementEntry> {
        val result = mutableListOf<BankStatementEntry>()
        db.query(
            TABLE_ENTRIES,
            arrayOf(
                "account_scope_id", "booking_date", "currency_code", "signed_amount",
                "operation_type", "bank_operation_reference", "description"
            ),
            null, null, null, null, "booking_date ASC, accounting_entry_id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += BankStatementEntry(
                    accountScopeId = cursor.getString(0),
                    bookingDate = java.time.LocalDate.parse(cursor.getString(1)),
                    currencyCode = cursor.getString(2),
                    signedAmount = java.math.BigDecimal(cursor.getString(3)),
                    operationType = cursor.getString(4),
                    bankOperationReference = cursor.getString(5),
                    description = cursor.getString(6)
                )
            }
        }
        return result
    }

    companion object {
        const val DATABASE_NAME = "payment_ledger.db"
        const val DATABASE_VERSION = 4
        const val DEFAULT_ACCOUNT_SCOPE_ID = "ipko-account-1"
        const val DEFAULT_ACCOUNT_SCOPE_NAME = "Rachunek iPKO 1"
        private const val TABLE_ENTRIES = "bank_statement_entries"
        private const val TABLE_ACCOUNT_SCOPES = "payment_account_scopes"
        private const val TABLE_FINANCIAL_TRANSACTIONS = "financial_transactions"
        private const val TABLE_TRANSACTION_FEES = "financial_transaction_fees"
        private const val TABLE_TRANSACTION_BANK_ENTRIES = "financial_transaction_bank_entries"
    }
}

data class PaymentAccountScope(
    val accountScopeId: String,
    val displayName: String
)

data class BatchImportSummary(
    val inserted: Int,
    val duplicates: Int
)


data class StoredFinancialTransaction(
    val transaction: pl.gov.obywatel.android.FinancialTransaction,
    val accountingEntryIds: List<String>
)
