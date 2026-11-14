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
class PaymentLedgerDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migracje kolejnych wersji należy dopisywać sekwencyjnie; nie usuwamy danych.
        if (oldVersion < 2) {
            // Wersja 1 jest pierwszą wersją schematu; brak destrukcyjnych zmian.
        }
    }

    /**
     * Zapisuje paczkę atomowo. Istniejące identyfikatory są pomijane przez klucz PRIMARY KEY.
     * Dowolny błąd walidacji lub SQL wycofuje całą paczkę.
     */
    fun importBatch(entries: List<BankStatementEntry>): BatchImportSummary {
        val db = writableDatabase
        var inserted = 0
        var duplicates = 0
        db.beginTransaction()
        try {
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
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return BatchImportSummary(inserted, duplicates)
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

    fun readAll(): List<BankStatementEntry> {
        val result = mutableListOf<BankStatementEntry>()
        readableDatabase.query(
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
        const val DATABASE_VERSION = 1
        private const val TABLE_ENTRIES = "bank_statement_entries"
    }
}

data class BatchImportSummary(
    val inserted: Int,
    val duplicates: Int
)
