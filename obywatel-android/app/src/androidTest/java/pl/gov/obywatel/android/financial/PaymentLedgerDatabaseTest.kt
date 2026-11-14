// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import pl.gov.obywatel.android.Fee
import pl.gov.obywatel.android.FinancialRecordSource
import pl.gov.obywatel.android.FinancialParseStatus
import pl.gov.obywatel.android.FinancialTransaction
import pl.gov.obywatel.android.FinancialTransactionType

@RunWith(AndroidJUnit4::class)
class PaymentLedgerDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "payment-ledger-test.db"

    @After
    fun cleanup() {
        context.deleteDatabase(databaseName)
    }

    private fun openDatabase() = PaymentLedgerDatabase(context, databaseName)

    private fun entry(amount: String = "749.33", reference: String = "102693731") =
        BankStatementEntry(
            accountScopeId = "test-account",
            bookingDate = LocalDate.parse("2026-11-12"),
            currencyCode = "USD",
            signedAmount = BigDecimal(amount),
            operationType = "WYMIANA W KANTORZE - UZNANIE",
            bankOperationReference = reference
        )


    @Test
    fun accountAliasesPersistAndLegacyAliasesAreMigratedWithoutChangingEntries() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE bank_statement_entries (
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
                db.insert(
                    "bank_statement_entries",
                    null,
                    ContentValues().apply {
                        put("accounting_entry_id", "legacy-entry")
                        put("account_scope_id", "legacy-ipko")
                        put("booking_date", "2026-11-12")
                        put("currency_code", "USD")
                        put("signed_amount", "12.50")
                        put("operation_type", "UZNANIE")
                        put("description", "")
                    }
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()

        val upgraded = openDatabase()
        try {
            val aliases = upgraded.getAccountScopes()
            assertEquals(true, aliases.any { it.accountScopeId == "legacy-ipko" })
            assertEquals(true, aliases.any { it.accountScopeId == "ipko-account-1" })
            assertEquals("legacy-ipko", upgraded.readAll().single().accountScopeId)

            upgraded.saveAccountScope("savings-account", "Oszczędności")
            assertEquals(true, upgraded.getAccountScopes().any {
                it.accountScopeId == "savings-account" && it.displayName == "Oszczędności"
            })
        } finally {
            upgraded.close()
        }

        val reopened = openDatabase()
        try {
            assertEquals(true, reopened.getAccountScopes().any {
                it.accountScopeId == "savings-account"
            })
            assertEquals("legacy-ipko", reopened.readAll().single().accountScopeId)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun accountAliasesRejectUnsafeIdentifiersAndDuplicateDisplayNames() {
        val db = openDatabase()
        try {
            db.saveAccountScope("primary", "Główny")
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAccountScope("123 456 789", "Numer rachunku")
            }
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAccountScope("secondary", "Główny")
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun importingSameEntryTwicePersistsItOnlyOnce() {
        val db = openDatabase()
        try {
            assertEquals(BatchImportSummary(1, 0), db.importBatch(listOf(entry())))
            assertEquals(BatchImportSummary(0, 1), db.importBatch(listOf(entry("749.330"))))
            assertEquals(1, db.readAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun invalidEntryRollsBackWholeBatch() {
        val db = openDatabase()
        try {
            val invalid = entry().copy(currencyCode = "US")
            assertThrows(IllegalArgumentException::class.java) {
                db.importBatch(listOf(entry(), invalid))
            }
            assertEquals(0, db.readAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun recordsSurviveDatabaseReopen() {
        val first = openDatabase()
        first.importBatch(listOf(entry()))
        first.close()

        val second = openDatabase()
        try {
            assertEquals(1, second.readAll().size)
            assertEquals(entry().accountingEntryId(), second.readAll().single().accountingEntryId())
        } finally {
            second.close()
        }
    }
    @Test
    fun versionTwoMigrationRebuildsExchangeIdsAndClearsNonExchangeReferences() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 2) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE bank_statement_entries (
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
                    "CREATE TABLE payment_account_scopes (" +
                        "account_scope_id TEXT PRIMARY KEY NOT NULL, display_name TEXT NOT NULL UNIQUE)"
                )
                fun insert(
                    id: String,
                    scope: String,
                    type: String,
                    currency: String,
                    amount: String
                ) {
                    db.insert(
                        "bank_statement_entries",
                        null,
                        ContentValues().apply {
                            put("accounting_entry_id", id)
                            put("exchange_operation_id", "old-reference-only-id")
                            put("account_scope_id", scope)
                            put("booking_date", "2026-03-04")
                            put("currency_code", currency)
                            put("signed_amount", amount)
                            put("operation_type", type)
                            put("bank_operation_reference", "90124777")
                            put("description", "")
                        }
                    )
                }
                insert("legacy-fx", "fx-account", "WYMIANA W KANTORZE - OBCIĄŻENIE", "USD", "-8000.00")
                insert("legacy-transfer", "fx-account", "PRZELEW ZEWNĘTRZNY", "PLN", "100.00")
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()

        val upgraded = openDatabase()
        try {
            val fxEntry = upgraded.readAll().single { it.operationType.contains("WYMIANA") }
            val transferEntry = upgraded.readAll().single { it.operationType.contains("PRZELEW") }
            val storedIds = mutableMapOf<String, String?>()
            upgraded.readableDatabase.query(
                "bank_statement_entries",
                arrayOf("accounting_entry_id", "exchange_operation_id"),
                null, null, null, null, null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    storedIds[cursor.getString(0)] =
                        if (cursor.isNull(1)) null else cursor.getString(1)
                }
            }
            assertEquals(fxEntry.exchangeOperationId(), storedIds["legacy-fx"])
            assertEquals(null, transferEntry.exchangeOperationId())
            assertEquals(null, storedIds["legacy-transfer"])
        } finally {
            upgraded.close()
        }
    }

    @Test
    fun financialTransactionFeeAndBankLinksPersistAtomically() {
        val db = openDatabase()
        try {
            val sourceEntry = entry("-8000.00").copy(
                operationType = "WYMIANA W KANTORZE - OBCIĄŻENIE"
            )
            db.importBatch(listOf(sourceEntry))
            val transaction = FinancialTransaction(
                id = "tx-fx-001",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.EXCHANGE,
                fromAssetCode = "USD",
                fromAmount = BigDecimal("8000.00"),
                toAssetCode = "CHF",
                toAmount = BigDecimal("6183.20"),
                fee = Fee("CHF", BigDecimal("5.25")),
                source = FinancialRecordSource.IPKO_IMPORT,
                externalId = "90124777",
                parseStatus = FinancialParseStatus.VERIFIED
            )
            db.saveFinancialTransaction(transaction, setOf(sourceEntry.accountingEntryId()))

            val stored = db.readFinancialTransactions().single()
            assertEquals(transaction.id, stored.transaction.id)
            assertEquals(transaction.executedAt, stored.transaction.executedAt)
            assertEquals(transaction.type, stored.transaction.type)
            assertEquals(0, transaction.fromAmount!!.compareTo(stored.transaction.fromAmount))
            assertEquals(0, transaction.toAmount!!.compareTo(stored.transaction.toAmount))
            assertEquals(0, BigDecimal("5.25").compareTo(stored.transaction.fee!!.amount))
            assertEquals("CHF", stored.transaction.fee!!.assetCode)
            assertEquals(listOf(sourceEntry.accountingEntryId()), stored.accountingEntryIds)
        } finally {
            db.close()
        }

        val reopened = openDatabase()
        try {
            assertEquals(1, reopened.readFinancialTransactions().size)
            assertEquals(1, reopened.readAll().size)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun financialTransactionRejectsUnknownBankEntryAndRollsBack() {
        val db = openDatabase()
        try {
            val transaction = FinancialTransaction(
                id = "tx-invalid-link",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            assertThrows(IllegalArgumentException::class.java) {
                db.saveFinancialTransaction(transaction, setOf("missing-bank-entry"))
            }
            assertEquals(emptyList<StoredFinancialTransaction>(), db.readFinancialTransactions())
        } finally {
            db.close()
        }
    }

    @Test
    fun versionThreeMigrationCreatesFinancialTablesWithoutChangingBankEntries() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 3) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE bank_statement_entries (
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
                    "CREATE TABLE payment_account_scopes (" +
                        "account_scope_id TEXT PRIMARY KEY NOT NULL, display_name TEXT NOT NULL UNIQUE)"
                )
                db.insert(
                    "bank_statement_entries",
                    null,
                    ContentValues().apply {
                        put("accounting_entry_id", "pre-v4-entry")
                        put("account_scope_id", "legacy-account")
                        put("booking_date", "2026-11-12")
                        put("currency_code", "USD")
                        put("signed_amount", "749.33")
                        put("operation_type", "UZNANIE")
                        put("description", "")
                    }
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()

        val upgraded = openDatabase()
        try {
            assertEquals("pre-v4-entry", upgraded.readAll().single().accountingEntryId())
            assertEquals(emptyList<StoredFinancialTransaction>(), upgraded.readFinancialTransactions())
            val transaction = FinancialTransaction(
                id = "after-migration",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("749.33")
            )
            upgraded.saveFinancialTransaction(transaction, setOf("pre-v4-entry"))
            assertEquals(1, upgraded.readFinancialTransactions().size)
        } finally {
            upgraded.close()
        }
    }

}
