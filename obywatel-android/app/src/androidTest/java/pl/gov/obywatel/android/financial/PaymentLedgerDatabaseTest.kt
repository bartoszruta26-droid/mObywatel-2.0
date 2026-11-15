// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteConstraintException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import pl.gov.obywatel.android.Asset
import pl.gov.obywatel.android.AssetKind
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
            val preservedEntry = upgraded.readAll().single()
            assertEquals("USD", preservedEntry.currencyCode)
            assertEquals(0, BigDecimal("749.33").compareTo(preservedEntry.signedAmount))
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

    @Test
    fun financialTransactionRejectsBankEntryWithMismatchedAmountOrCurrency() {
        val db = openDatabase()
        try {
            val unrelatedEntry = entry("749.33")
            db.importBatch(listOf(unrelatedEntry))
            val transaction = FinancialTransaction(
                id = "tx-mismatch",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.EXCHANGE,
                fromAssetCode = "USD",
                fromAmount = BigDecimal("8000.00"),
                toAssetCode = "CHF",
                toAmount = BigDecimal("6183.20")
            )
            assertThrows(IllegalArgumentException::class.java) {
                db.saveFinancialTransaction(transaction, setOf(unrelatedEntry.accountingEntryId()))
            }
            assertEquals(emptyList<StoredFinancialTransaction>(), db.readFinancialTransactions())
        } finally {
            db.close()
        }
    }

    @Test
    fun updatingFinancialTransactionPreservesRecordAndReplacesOnlyItsExplicitLinks() {
        val db = openDatabase()
        try {
            val fromEntry = entry("-8000.00", "fx-out").copy(
                operationType = "WYMIANA W KANTORZE - OBCIĄŻENIE"
            )
            val toEntry = entry("6183.20", "fx-in").copy(
                currencyCode = "CHF",
                operationType = "WYMIANA W KANTORZE - UZNANIE"
            )
            db.importBatch(listOf(fromEntry, toEntry))
            val original = FinancialTransaction(
                id = "tx-update",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.EXCHANGE,
                fromAssetCode = "USD",
                fromAmount = BigDecimal("8000.00"),
                toAssetCode = "CHF",
                toAmount = BigDecimal("6183.20"),
                fee = Fee("CHF", BigDecimal("5.25"))
            )
            db.saveFinancialTransaction(
                original,
                setOf(fromEntry.accountingEntryId(), toEntry.accountingEntryId())
            )

            val edited = original.copy(
                fee = null,
                parseStatus = FinancialParseStatus.VERIFIED
            )
            db.saveFinancialTransaction(
                edited,
                setOf(fromEntry.accountingEntryId(), toEntry.accountingEntryId())
            )

            val stored = db.readFinancialTransactions().single()
            val persisted = stored.transaction
            assertEquals(edited.id, persisted.id)
            assertEquals(edited.executedAt, persisted.executedAt)
            assertEquals(edited.type, persisted.type)
            assertEquals(edited.fromAssetCode, persisted.fromAssetCode)
            assertEquals(0, edited.fromAmount!!.compareTo(persisted.fromAmount))
            assertEquals(edited.toAssetCode, persisted.toAssetCode)
            assertEquals(0, edited.toAmount!!.compareTo(persisted.toAmount))
            assertEquals(edited.fee, persisted.fee)
            assertEquals(edited.source, persisted.source)
            assertEquals(edited.externalId, persisted.externalId)
            assertEquals(edited.rawDescription, persisted.rawDescription)
            assertEquals(edited.parseStatus, persisted.parseStatus)
            assertEquals(
                setOf(fromEntry.accountingEntryId(), toEntry.accountingEntryId()),
                stored.accountingEntryIds.toSet()
            )
            assertEquals(2, db.readAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun assetRegistryPersistsAndUpdatesCurrencyAndMetalDefinitions() {
        val db = openDatabase()
        try {
            val usd = Asset("USD", "Dolar amerykański", AssetKind.CURRENCY, "USD", 2)
            val gold = Asset("XAU_G_999", "Złoto próby 999", AssetKind.PRECIOUS_METAL, "g", 4, 999)
            db.saveAsset(usd)
            db.saveAsset(gold)

            assertEquals(listOf(usd, gold), db.readAssets())

            val updatedUsd = usd.copy(name = "Dolar USA", decimalPlaces = 3)
            db.saveAsset(updatedUsd)
            val stored = db.readAssets().associateBy { it.code }
            assertEquals(2, stored.size)
            assertEquals(updatedUsd, stored.getValue("USD"))
            assertEquals(gold, stored.getValue("XAU_G_999"))
        } finally {
            db.close()
        }

        val reopened = openDatabase()
        try {
            assertEquals(2, reopened.readAssets().size)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun usedAssetKeepsHistoricalMeasurementDefinitionImmutableButAllowsRename() {
        val db = openDatabase()
        try {
            val usd = Asset("USD", "Dolar amerykański", AssetKind.CURRENCY, "USD", 2)
            db.saveAsset(usd)
            val transaction = FinancialTransaction(
                id = "asset-history-usd",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "USD",
                toAmount = BigDecimal("100.00")
            )
            db.saveFinancialTransaction(transaction)

            db.saveAsset(usd.copy(name = "Dolar USA"))
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(usd.copy(decimalPlaces = 3))
            }
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(usd.copy(unit = "cent"))
            }
            assertEquals("Dolar USA", db.readAssets().single().name)
            assertEquals(2, db.readAssets().single().decimalPlaces)
            assertEquals(1, db.readFinancialTransactions().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun usedMetalCannotChangePurityOrUnit() {
        val db = openDatabase()
        try {
            val gold = Asset("XAU_G_999", "Złoto próby 999", AssetKind.PRECIOUS_METAL, "g", 4, 999)
            db.saveAsset(gold)
            val transaction = FinancialTransaction(
                id = "asset-history-gold",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.METAL_BUY,
                fromAssetCode = "PLN",
                fromAmount = BigDecimal("500.00"),
                toAssetCode = gold.code,
                toAmount = BigDecimal("1.0000")
            )
            db.saveFinancialTransaction(transaction)

            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(gold.copy(purity = 750))
            }
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(gold.copy(unit = "ozt"))
            }
            assertEquals(gold, db.readAssets().single())
        } finally {
            db.close()
        }
    }

    @Test
    fun assetUsedOnlyAsTransactionFeeKeepsItsHistoricalDefinitionImmutable() {
        val db = openDatabase()
        try {
            val feeAsset = Asset("XAU_FEE_999", "Złoto prowizyjne próby 999", AssetKind.PRECIOUS_METAL, "g", 4, 999)
            db.saveAsset(feeAsset)
            val transaction = FinancialTransaction(
                id = "asset-history-fee-only",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.DEPOSIT,
                fromAssetCode = null,
                fromAmount = null,
                toAssetCode = "PLN",
                toAmount = BigDecimal("100.00"),
                fee = Fee(feeAsset.code, BigDecimal("0.0010"))
            )
            db.saveFinancialTransaction(transaction)

            db.saveAsset(feeAsset.copy(name = "Złoto prowizyjne"))
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(feeAsset.copy(purity = 750))
            }
            assertThrows(IllegalArgumentException::class.java) {
                db.saveAsset(feeAsset.copy(unit = "ozt"))
            }
            assertEquals(999, db.readAssets().single().purity)
            assertEquals(feeAsset.code, db.readFinancialTransactions().single().transaction.fee?.assetCode)
        } finally {
            db.close()
        }
    }

    @Test
    fun versionFourMigrationCreatesAssetRegistryWithoutDroppingLegacyTables() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 4) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE legacy_marker (marker TEXT PRIMARY KEY NOT NULL)")
                db.execSQL("INSERT INTO legacy_marker(marker) VALUES ('preserved')")
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()

        val upgraded = openDatabase()
        try {
            upgraded.saveAsset(Asset("PLN", "Polski złoty", AssetKind.CURRENCY, "PLN", 2))
            assertEquals("PLN", upgraded.readAssets().single().code)
            upgraded.readableDatabase.rawQuery(
                "SELECT marker FROM legacy_marker", null
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("preserved", cursor.getString(0))
            }
        } finally {
            upgraded.close()
        }
    }

    @Test
    fun oneBankEntryCannotBeLinkedToTwoFinancialTransactions() {
        val db = openDatabase()
        try {
            val sourceEntry = entry("-8000.00").copy(
                operationType = "WYMIANA W KANTORZE - OBCIĄŻENIE"
            )
            db.importBatch(listOf(sourceEntry))
            val first = FinancialTransaction(
                id = "tx-first",
                executedAt = Instant.parse("2026-11-12T10:15:30Z"),
                type = FinancialTransactionType.EXCHANGE,
                fromAssetCode = "USD",
                fromAmount = BigDecimal("8000.00"),
                toAssetCode = "CHF",
                toAmount = BigDecimal("6183.20")
            )
            val second = first.copy(
                id = "tx-second",
                toAssetCode = "EUR",
                toAmount = BigDecimal("7500.00")
            )
            db.saveFinancialTransaction(first, setOf(sourceEntry.accountingEntryId()))

            assertThrows(SQLiteConstraintException::class.java) {
                db.saveFinancialTransaction(second, setOf(sourceEntry.accountingEntryId()))
            }
            val stored = db.readFinancialTransactions()
            assertEquals(1, stored.size)
            assertEquals("tx-first", stored.single().transaction.id)
        } finally {
            db.close()
        }
    }

    @Test
    fun cashDonationPersistsAcrossReopenWithOrganizationAndPurpose() {
        val db = openDatabase()
        try {
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "donation-persist-1",
                receivedAt = Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("250.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Zakup materiałów edukacyjnych",
                isAnonymous = true
            )
            db.saveCashDonation(donation)
            val stored = db.readCashDonations().single()
            assertEquals(donation.id, stored.id)
            assertEquals(donation.beneficiaryOrganizationName, stored.beneficiaryOrganizationName)
            assertEquals(donation.purpose, stored.purpose)
            assertEquals(true, stored.isAnonymous)
            assertEquals(0, donation.amount.compareTo(stored.amount))
        } finally {
            db.close()
        }
        val reopened = openDatabase()
        try {
            assertEquals("Fundacja Aria", reopened.readCashDonations().single().beneficiaryOrganizationName)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun cashDonationRejectsDuplicateIdAndCanBeUpdatedOrDeleted() {
        val db = openDatabase()
        try {
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "donation-crud-1",
                receivedAt = Instant.parse("2026-11-14T12:00:00Z"),
                amount = BigDecimal("100.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Cel testowy"
            )
            db.saveCashDonation(donation)
            assertThrows(SQLiteConstraintException::class.java) { db.saveCashDonation(donation) }
            db.updateCashDonation(donation.copy(purpose = "Nowy cel"))
            assertEquals("Nowy cel", db.readCashDonations().single().purpose)
            db.deleteCashDonation(donation.id)
            assertEquals(emptyList<pl.gov.obywatel.android.CashDonation>(), db.readCashDonations())
        } finally {
            db.close()
        }
    }

    @Test
    fun cashDonationSchemaMigratesFromVersionFiveWithoutDroppingLegacyData() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 5) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE legacy_donation_marker (value TEXT NOT NULL)")
                db.execSQL("INSERT INTO legacy_donation_marker(value) VALUES ('preserve-me')")
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()
        val upgraded = openDatabase()
        try {
            upgraded.saveCashDonation(
                pl.gov.obywatel.android.CashDonation(
                    id = "donation-after-migration",
                    receivedAt = Instant.parse("2026-11-14T12:00:00Z"),
                    amount = BigDecimal("10.00"),
                    currencyCode = "PLN",
                    beneficiaryOrganizationName = "Fundacja Aria",
                    purpose = "Cel po migracji"
                )
            )
            upgraded.readableDatabase.rawQuery("SELECT value FROM legacy_donation_marker", null).use {
                assertEquals(true, it.moveToFirst())
                assertEquals("preserve-me", it.getString(0))
            }
            assertEquals(1, upgraded.readCashDonations().size)
        } finally {
            upgraded.close()
        }
    }


    @Test
    fun donationAttachmentsPersistAndCascadeWhenDonationIsDeleted() {
        val db = openDatabase()
        try {
            val donation = pl.gov.obywatel.android.CashDonation(
                id = "donation-attachment-owner",
                receivedAt = Instant.parse("2026-11-15T10:00:00Z"),
                amount = BigDecimal("75.00"),
                currencyCode = "PLN",
                beneficiaryOrganizationName = "Fundacja Aria",
                purpose = "Test załącznika"
            )
            db.saveCashDonation(donation)
            val attachment = pl.gov.obywatel.android.DonationAttachment(
                id = "00000000-0000-0000-0000-000000000001",
                donationId = donation.id,
                fileName = "pokwitowanie.pdf",
                mimeType = "application/pdf",
                sizeBytes = 1024,
                sha256 = "a".repeat(64),
                storedFileName = "00000000-0000-0000-0000-000000000002.pdf",
                addedAt = Instant.parse("2026-11-15T10:01:00Z")
            )
            db.saveDonationAttachment(attachment)
            assertEquals(attachment, db.readDonationAttachments().single())
            db.deleteCashDonation(donation.id)
            assertEquals(emptyList<pl.gov.obywatel.android.DonationAttachment>(), db.readDonationAttachments())
        } finally {
            db.close()
        }
    }

    @Test
    fun donationAttachmentRejectsUnsupportedMimeAndOversizedMetadata() {
        val base = pl.gov.obywatel.android.DonationAttachment(
            id = "00000000-0000-0000-0000-000000000001",
            donationId = "donation-x",
            fileName = "pokwitowanie.pdf",
            mimeType = "application/pdf",
            sizeBytes = 1024,
            sha256 = "b".repeat(64),
            storedFileName = "00000000-0000-0000-0000-000000000002.pdf",
            addedAt = Instant.parse("2026-11-15T10:01:00Z")
        )
        base.validate()
        assertThrows(IllegalArgumentException::class.java) {
            base.copy(mimeType = "application/octet-stream").validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            base.copy(sizeBytes = pl.gov.obywatel.android.DonationAttachment.MAX_BYTES + 1).validate()
        }
    }



    @Test
    fun versionSixMigrationCreatesDonationAttachmentTableAndPreservesDonation() {
        val legacy = object : SQLiteOpenHelper(context, databaseName, null, 6) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE cash_donations (
                        donation_id TEXT PRIMARY KEY NOT NULL,
                        received_at TEXT NOT NULL,
                        amount TEXT NOT NULL,
                        currency_code TEXT NOT NULL,
                        beneficiary_organization_name TEXT NOT NULL,
                        purpose TEXT NOT NULL,
                        donor_name TEXT,
                        is_anonymous INTEGER NOT NULL,
                        campaign_name TEXT,
                        collected_by TEXT,
                        receipt_number TEXT,
                        custody_status TEXT NOT NULL,
                        transferred_at TEXT,
                        transferred_amount TEXT,
                        note TEXT,
                        source TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO cash_donations(donation_id, received_at, amount, currency_code, beneficiary_organization_name, purpose, is_anonymous, custody_status, source) " +
                        "VALUES ('legacy-donation', '2026-11-14T10:00:00Z', '12.00', 'PLN', 'Fundacja Aria', 'Cel', 0, 'HELD', 'MANUAL')"
                )
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        legacy.writableDatabase
        legacy.close()
        val upgraded = openDatabase()
        try {
            assertEquals("Fundacja Aria", upgraded.readCashDonations().single().beneficiaryOrganizationName)
            upgraded.readableDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='donation_attachments'",
                null
            ).use { assertEquals(true, it.moveToFirst()) }
        } finally {
            upgraded.close()
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
            assertEquals(original, target.getFinancialTransactions().single().transaction)

            val changed = original.copy(toAmount = BigDecimal("999.00"))
            val changedArchive = PaymentLedgerJsonCodec.encode(
                emptyList(),
                listOf(StoredFinancialTransaction(changed, emptyList()))
            )
            val result = runCatching { target.importJson(changedArchive) }

            assertTrue("Zmiana danych pod istniejącym identyfikatorem musi być konfliktem.", result.isFailure)
            assertEquals(original, target.getFinancialTransactions().single().transaction)
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

}
