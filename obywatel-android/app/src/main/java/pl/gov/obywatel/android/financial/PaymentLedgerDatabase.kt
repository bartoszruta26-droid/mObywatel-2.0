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
        createAssetTable(db)
        createCashDonationTable(db)
        createDonationAttachmentTable(db)
        createCurrencyConversionHistoryTable(db)
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
        if (oldVersion < 5) {
            createAssetTable(db)
        }
        if (oldVersion < 6) {
            createCashDonationTable(db)
        }
        if (oldVersion < 7) {
            createDonationAttachmentTable(db)
        }
        if (oldVersion < 8) {
            createCurrencyConversionHistoryTable(db)
        }
    }

    private fun createCashDonationTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cash_donations (
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
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cash_donations_received_at ON cash_donations(received_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cash_donations_beneficiary ON cash_donations(beneficiary_organization_name)")
    }


    private fun createDonationAttachmentTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS donation_attachments (
                attachment_id TEXT PRIMARY KEY NOT NULL,
                donation_id TEXT NOT NULL REFERENCES $TABLE_CASH_DONATIONS(donation_id) ON DELETE CASCADE,
                file_name TEXT NOT NULL,
                mime_type TEXT NOT NULL,
                size_bytes INTEGER NOT NULL,
                sha256 TEXT NOT NULL,
                stored_file_name TEXT NOT NULL UNIQUE,
                added_at TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_donation_attachments_donation ON donation_attachments(donation_id)")
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

    private fun createAssetTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_ASSETS (
                asset_code TEXT PRIMARY KEY NOT NULL,
                asset_name TEXT NOT NULL,
                asset_kind TEXT NOT NULL,
                unit TEXT NOT NULL,
                decimal_places INTEGER NOT NULL,
                purity INTEGER,
                updated_at TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_payment_assets_kind ON $TABLE_ASSETS(asset_kind)")
    }

    /**
     * Zapisuje definicję aktywa. Po użyciu aktywa w transakcji można zmienić jego nazwę,
     * ale nie jednostkę, precyzję, rodzaj ani próbę — te pola zmieniałyby interpretację historii.
     */
    fun saveAsset(asset: pl.gov.obywatel.android.Asset) {
        asset.validate()
        val db = writableDatabase
        db.beginTransaction()
        try {
            var existing: pl.gov.obywatel.android.Asset? = null
            db.query(
                TABLE_ASSETS,
                arrayOf("asset_code", "asset_name", "asset_kind", "unit", "decimal_places", "purity"),
                "asset_code = ?",
                arrayOf(asset.code),
                null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    existing = pl.gov.obywatel.android.Asset(
                        code = cursor.getString(0),
                        name = cursor.getString(1),
                        kind = pl.gov.obywatel.android.AssetKind.valueOf(cursor.getString(2)),
                        unit = cursor.getString(3),
                        decimalPlaces = cursor.getInt(4),
                        purity = if (cursor.isNull(5)) null else cursor.getInt(5)
                    )
                }
            }
            val previous = existing
            if (previous != null && isAssetReferenced(db, asset.code)) {
                require(
                    previous.kind == asset.kind &&
                        previous.unit == asset.unit &&
                        previous.decimalPlaces == asset.decimalPlaces &&
                        previous.purity == asset.purity
                ) {
                    "Nie można zmienić rodzaju, jednostki, precyzji ani próby aktywa użytego w historii. Utwórz osobny kod aktywa."
                }
            }
            val values = ContentValues().apply {
                put("asset_code", asset.code)
                put("asset_name", asset.name)
                put("asset_kind", asset.kind.name)
                put("unit", asset.unit)
                put("decimal_places", asset.decimalPlaces)
                if (asset.purity == null) putNull("purity") else put("purity", asset.purity)
                put("updated_at", java.time.Instant.now().toString())
            }
            if (previous == null) {
                db.insertOrThrow(TABLE_ASSETS, null, values)
            } else {
                db.update(TABLE_ASSETS, values, "asset_code = ?", arrayOf(asset.code))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun isAssetReferenced(db: SQLiteDatabase, assetCode: String): Boolean {
        db.query(
            TABLE_FINANCIAL_TRANSACTIONS,
            arrayOf("transaction_id"),
            "from_asset_code = ? OR to_asset_code = ?",
            arrayOf(assetCode, assetCode),
            null, null, null, "1"
        ).use { if (it.moveToFirst()) return true }
        db.query(
            TABLE_TRANSACTION_FEES,
            arrayOf("transaction_id"),
            "asset_code = ?",
            arrayOf(assetCode),
            null, null, null, "1"
        ).use { if (it.moveToFirst()) return true }
        return false
    }

    /** Zwraca słownik aktywów w stabilnej kolejności, bez konwersji wartości liczbowych. */
    fun readAssets(): List<pl.gov.obywatel.android.Asset> {
        val result = mutableListOf<pl.gov.obywatel.android.Asset>()
        readableDatabase.query(
            TABLE_ASSETS,
            arrayOf("asset_code", "asset_name", "asset_kind", "unit", "decimal_places", "purity"),
            null, null, null, null,
            "asset_kind ASC, asset_code ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += pl.gov.obywatel.android.Asset(
                    code = cursor.getString(0),
                    name = cursor.getString(1),
                    kind = pl.gov.obywatel.android.AssetKind.valueOf(cursor.getString(2)),
                    unit = cursor.getString(3),
                    decimalPlaces = cursor.getInt(4),
                    purity = if (cursor.isNull(5)) null else cursor.getInt(5)
                )
            }
        }
        return result
    }

    private fun createCurrencyConversionHistoryTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS currency_conversion_history (
                conversion_id TEXT PRIMARY KEY NOT NULL,
                converted_at TEXT NOT NULL,
                source_amount TEXT NOT NULL,
                source_currency_code TEXT NOT NULL,
                target_amount TEXT NOT NULL,
                target_currency_code TEXT NOT NULL,
                applied_rate TEXT NOT NULL,
                rate_date TEXT NOT NULL,
                rate_source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_currency_conversion_date " +
                "ON currency_conversion_history(converted_at DESC)"
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
                    arrayOf("currency_code", "signed_amount"),
                    "accounting_entry_id = ?",
                    arrayOf(entryId),
                    null, null, null
                ).use { cursor ->
                    require(cursor.moveToFirst()) {
                        "Nie znaleziono powiązanego zapisu bankowego."
                    }
                    val currency = cursor.getString(0)
                    val signedAmount = java.math.BigDecimal(cursor.getString(1))
                    val absoluteAmount = signedAmount.abs()
                    val matchesFrom = transaction.fromAssetCode == currency &&
                        signedAmount.signum() < 0 &&
                        transaction.fromAmount?.compareTo(absoluteAmount) == 0
                    val matchesTo = transaction.toAssetCode == currency &&
                        signedAmount.signum() > 0 &&
                        transaction.toAmount?.compareTo(absoluteAmount) == 0
                    val matchesFee = transaction.fee?.let { fee ->
                        fee.assetCode == currency &&
                            signedAmount.signum() < 0 &&
                            fee.amount.compareTo(absoluteAmount) == 0
                    } == true
                    require(matchesFrom || matchesTo || matchesFee) {
                        "Powiązany zapis bankowy nie odpowiada kwocie i walucie transakcji ani prowizji."
                    }
                }
            }
            // Aktualizacja istniejącego rekordu nie może używać CONFLICT_REPLACE:
            // SQLite REPLACE wykonuje usunięcie i ponowny INSERT, co uruchamia akcje
            // ON DELETE i może naruszyć semantykę zachowania relacji.
            // Najpierw aktualizujemy rekord nadrzędny, a następnie wymieniamy relacje.
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
            val updated = db.update(
                TABLE_FINANCIAL_TRANSACTIONS,
                values,
                "transaction_id = ?",
                arrayOf(transaction.id)
            )
            if (updated == 0) {
                val insertedTransaction = db.insert(
                    TABLE_FINANCIAL_TRANSACTIONS,
                    null,
                    values
                )
                check(insertedTransaction != -1L) {
                    "Nie udało się zapisać transakcji finansowej."
                }
            }
            db.delete(
                TABLE_TRANSACTION_BANK_ENTRIES,
                "transaction_id = ?",
                arrayOf(transaction.id)
            )
            db.delete(
                TABLE_TRANSACTION_FEES,
                "transaction_id = ?",
                arrayOf(transaction.id)
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

    /**
     * Importuje pełne archiwum atomowo: wpisy bankowe oraz transakcje i ich powiązania.
     * Każde powiązanie jest sprawdzane po zapisaniu (w tej samej transakcji) wpisów źródłowych.
     */
    /** Importuje wszystkie typy danych archiwum w jednej transakcji SQLite. */
    fun importArchive(archive: PaymentLedgerJsonCodec.Archive): BatchImportSummary {
        // Identyczne rekordy w jednej paczce są deduplikowane; ten sam identyfikator
        // z odmienną treścią pozostaje konfliktem i wycofuje całą transakcję.
        val uniqueTransactions = linkedMapOf<String, StoredFinancialTransaction>()
        archive.transactions.forEach { incoming ->
            val previous = uniqueTransactions[incoming.transaction.id]
            if (previous == null) uniqueTransactions[incoming.transaction.id] = incoming
            else require(sameStoredTransaction(previous, incoming)) {
                "Archiwum zawiera sprzeczne dane transakcji " + incoming.transaction.id + "."
            }
        }
        val uniqueDonations = linkedMapOf<String, pl.gov.obywatel.android.CashDonation>()
        archive.donations.forEach { incoming ->
            val previous = uniqueDonations[incoming.id]
            if (previous == null) uniqueDonations[incoming.id] = incoming
            else require(sameDonationIdentity(previous, incoming)) {
                "Archiwum zawiera sprzeczne dane darowizny " + incoming.id + "."
            }
        }
        val uniqueConversions = linkedMapOf<String, CurrencyConversionRecord>()
        archive.currencyConversions.forEach { incoming ->
            incoming.validate()
            val previous = uniqueConversions[incoming.id]
            if (previous == null) uniqueConversions[incoming.id] = incoming
            else require(sameCurrencyConversion(previous, incoming)) {
                "Archiwum zawiera sprzeczne dane przeliczenia " + incoming.id + "."
            }
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val preview = BankImportDeduplicator.preview(archive.entries, readAll(db))
            require(preview.conflicts.isEmpty()) {
                "Import zawiera ${preview.conflicts.size} konfliktów referencji. Wymagana weryfikacja."
            }
            val inserted = insertEntries(db, preview.newEntries)
            val existingTransactions = readFinancialTransactions().associateBy { it.transaction.id }
            uniqueTransactions.values.forEach { stored ->
                val existing = existingTransactions[stored.transaction.id]
                if (existing != null) {
                    require(sameStoredTransaction(existing, stored)) {
                        "Konflikt identyfikatora transakcji ${stored.transaction.id}; dane wymagają weryfikacji."
                    }
                } else {
                    saveFinancialTransaction(stored.transaction, stored.accountingEntryIds.toSet())
                }
            }
            var importedDonations = 0
            uniqueDonations.values.forEach { donation ->
                val exists = db.query(
                    TABLE_CASH_DONATIONS,
                    arrayOf("donation_id"),
                    "donation_id = ?",
                    arrayOf(donation.id),
                    null, null, null
                ).use { it.moveToFirst() }
                if (!exists) {
                    saveCashDonation(donation)
                    importedDonations++
                } else {
                    val previous = db.query(
                        TABLE_CASH_DONATIONS,
                        arrayOf(
                            "received_at", "amount", "currency_code", "beneficiary_organization_name",
                            "purpose", "donor_name", "is_anonymous", "campaign_name", "collected_by",
                            "receipt_number", "custody_status", "transferred_at", "transferred_amount",
                            "note", "source"
                        ),
                        "donation_id = ?",
                        arrayOf(donation.id),
                        null, null, null
                    ).use { cursor ->
                        require(cursor.moveToFirst())
                        pl.gov.obywatel.android.CashDonation(
                            id = donation.id,
                            receivedAt = java.time.Instant.parse(cursor.getString(0)),
                            amount = java.math.BigDecimal(cursor.getString(1)),
                            currencyCode = cursor.getString(2),
                            beneficiaryOrganizationName = cursor.getString(3),
                            purpose = cursor.getString(4),
                            donorName = cursor.getString(5),
                            isAnonymous = cursor.getInt(6) == 1,
                            campaignName = cursor.getString(7),
                            collectedBy = cursor.getString(8),
                            receiptNumber = cursor.getString(9),
                            custodyStatus = pl.gov.obywatel.android.DonationCustodyStatus.valueOf(cursor.getString(10)),
                            transferredAt = cursor.getString(11)?.let { java.time.Instant.parse(it) },
                            transferredAmount = cursor.getString(12)?.let { java.math.BigDecimal(it) },
                            note = cursor.getString(13),
                            source = pl.gov.obywatel.android.FinancialRecordSource.valueOf(cursor.getString(14))
                        )
                    }
                    require(sameDonationIdentity(previous, donation)) {
                        "Konflikt identyfikatora darowizny ${donation.id}; dane wymagają weryfikacji."
                    }
                }
            }
            val existingConversions = readAllCurrencyConversions().associateBy { it.id }
            var importedConversions = 0
            uniqueConversions.values.forEach { incoming ->
                val existing = existingConversions[incoming.id]
                if (existing == null) {
                    saveCurrencyConversion(incoming)
                    importedConversions++
                } else {
                    require(sameCurrencyConversion(existing, incoming)) {
                        "Konflikt identyfikatora przeliczenia " + incoming.id + "; dane wymagają weryfikacji."
                    }
                }
            }
            db.setTransactionSuccessful()
            return BatchImportSummary(
                inserted = inserted.inserted + importedDonations + importedConversions,
                duplicates = inserted.duplicates + preview.duplicates.size +
                    (archive.donations.size - importedDonations) +
                    (archive.currencyConversions.size - importedConversions)
            )
        } finally {
            db.endTransaction()
        }
    }

    private fun sameCurrencyConversion(first: CurrencyConversionRecord, second: CurrencyConversionRecord): Boolean =
        first.id == second.id && first.convertedAt == second.convertedAt &&
            first.sourceAmount == second.sourceAmount &&
            first.sourceCurrencyCode == second.sourceCurrencyCode &&
            first.targetAmount == second.targetAmount &&
            first.targetCurrencyCode == second.targetCurrencyCode &&
            first.appliedRate == second.appliedRate &&
            first.rateDate == second.rateDate && first.rateSource == second.rateSource

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
            sameAmount(existing.transferredAmount, incoming.transferredAmount) &&
            existing.note == incoming.note &&
            existing.source == incoming.source

    private fun sameAmount(first: java.math.BigDecimal?, second: java.math.BigDecimal?): Boolean =
        if (first == null || second == null) first == second else first.compareTo(second) == 0

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

    /** Odtwarza transakcje wraz z prowizjami i identyfikatorami źródłowych zapisów. */
    fun previewArchiveEntries(entries: List<BankStatementEntry>): BankImportPreview =
        BankImportDeduplicator.preview(entries, readAll())

    fun saveCashDonation(donation: pl.gov.obywatel.android.CashDonation) {
        donation.validate()
        val values = donation.toContentValues()
        writableDatabase.insertOrThrow(TABLE_CASH_DONATIONS, null, values)
    }

    fun updateCashDonation(donation: pl.gov.obywatel.android.CashDonation) {
        donation.validate()
        val values = donation.toContentValues().apply { remove("donation_id") }
        val count = writableDatabase.update(TABLE_CASH_DONATIONS, values, "donation_id = ?", arrayOf(donation.id))
        check(count == 1) { "Nie znaleziono darowizny do aktualizacji." }
    }

    private fun pl.gov.obywatel.android.CashDonation.toContentValues(): ContentValues =
        ContentValues().apply {
            put("donation_id", id)
            put("received_at", receivedAt.toString())
            put("amount", amount.stripTrailingZeros().toPlainString())
            put("currency_code", currencyCode)
            put("beneficiary_organization_name", beneficiaryOrganizationName.trim())
            put("purpose", purpose.trim())
            put("donor_name", donorName)
            put("is_anonymous", if (isAnonymous) 1 else 0)
            put("campaign_name", campaignName)
            put("collected_by", collectedBy)
            put("receipt_number", receiptNumber)
            put("custody_status", custodyStatus.name)
            put("transferred_at", transferredAt?.toString())
            put("transferred_amount", transferredAmount?.stripTrailingZeros()?.toPlainString())
            put("note", note)
            put("source", source.name)
        }

    fun readCashDonations(): List<pl.gov.obywatel.android.CashDonation> {
        val result = mutableListOf<pl.gov.obywatel.android.CashDonation>()
        readableDatabase.query(
            TABLE_CASH_DONATIONS,
            arrayOf(
                "donation_id", "received_at", "amount", "currency_code",
                "beneficiary_organization_name", "purpose", "donor_name",
                "is_anonymous", "campaign_name", "collected_by", "receipt_number",
                "custody_status", "transferred_at", "transferred_amount", "note", "source"
            ),
            null, null, null, null, "received_at ASC, donation_id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += pl.gov.obywatel.android.CashDonation(
                    id = cursor.getString(0),
                    receivedAt = java.time.Instant.parse(cursor.getString(1)),
                    amount = java.math.BigDecimal(cursor.getString(2)),
                    currencyCode = cursor.getString(3),
                    beneficiaryOrganizationName = cursor.getString(4),
                    purpose = cursor.getString(5),
                    donorName = cursor.getString(6),
                    isAnonymous = cursor.getInt(7) == 1,
                    campaignName = cursor.getString(8),
                    collectedBy = cursor.getString(9),
                    receiptNumber = cursor.getString(10),
                    custodyStatus = pl.gov.obywatel.android.DonationCustodyStatus.valueOf(cursor.getString(11)),
                    transferredAt = cursor.getString(12)?.let { java.time.Instant.parse(it) },
                    transferredAmount = cursor.getString(13)?.let { java.math.BigDecimal(it) },
                    note = cursor.getString(14),
                    source = pl.gov.obywatel.android.FinancialRecordSource.valueOf(cursor.getString(15))
                ).also { it.validate() }
            }
        }
        return result
    }

    fun deleteCashDonation(id: String) {
        require(id.isNotBlank()) { "Identyfikator darowizny jest wymagany." }
        val count = writableDatabase.delete(TABLE_CASH_DONATIONS, "donation_id = ?", arrayOf(id))
        check(count == 1) { "Nie znaleziono darowizny do usunięcia." }
    }


    fun saveDonationAttachment(attachment: pl.gov.obywatel.android.DonationAttachment) {
        attachment.validate()
        val values = ContentValues().apply {
            put("attachment_id", attachment.id)
            put("donation_id", attachment.donationId)
            put("file_name", attachment.fileName)
            put("mime_type", attachment.mimeType)
            put("size_bytes", attachment.sizeBytes)
            put("sha256", attachment.sha256)
            put("stored_file_name", attachment.storedFileName)
            put("added_at", attachment.addedAt.toString())
        }
        writableDatabase.insertOrThrow(TABLE_DONATION_ATTACHMENTS, null, values)
    }

    fun readDonationAttachments(): List<pl.gov.obywatel.android.DonationAttachment> {
        val result = mutableListOf<pl.gov.obywatel.android.DonationAttachment>()
        readableDatabase.query(
            TABLE_DONATION_ATTACHMENTS,
            arrayOf("attachment_id", "donation_id", "file_name", "mime_type", "size_bytes", "sha256", "stored_file_name", "added_at"),
            null, null, null, null, "added_at ASC, attachment_id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += pl.gov.obywatel.android.DonationAttachment(
                    id = cursor.getString(0),
                    donationId = cursor.getString(1),
                    fileName = cursor.getString(2),
                    mimeType = cursor.getString(3),
                    sizeBytes = cursor.getLong(4),
                    sha256 = cursor.getString(5),
                    storedFileName = cursor.getString(6),
                    addedAt = java.time.Instant.parse(cursor.getString(7))
                ).also { it.validate() }
            }
        }
        return result
    }

    fun deleteDonationAttachment(id: String): pl.gov.obywatel.android.DonationAttachment {
        require(id.isNotBlank()) { "Identyfikator załącznika jest wymagany." }
        val db = writableDatabase
        val attachment = readDonationAttachments().firstOrNull { it.id == id }
            ?: throw IllegalStateException("Nie znaleziono załącznika.")
        val count = db.delete(TABLE_DONATION_ATTACHMENTS, "attachment_id = ?", arrayOf(id))
        check(count == 1) { "Nie udało się usunąć załącznika." }
        return attachment
    }

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
                    fromAmount = cursor.getString(4)?.let { java.math.BigDecimal(it) },
                    toAssetCode = cursor.getString(5),
                    toAmount = cursor.getString(6)?.let { java.math.BigDecimal(it) },
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

    /** Zapisuje niezmienny ślad przeliczenia wraz z kursem i datą notowania. */
    fun saveCurrencyConversion(record: CurrencyConversionRecord) {
        record.validate()
        val values = ContentValues().apply {
            put("conversion_id", record.id)
            put("converted_at", record.convertedAt.toString())
            put("source_amount", record.sourceAmount.toPlainString())
            put("source_currency_code", record.sourceCurrencyCode)
            put("target_amount", record.targetAmount.toPlainString())
            put("target_currency_code", record.targetCurrencyCode)
            put("applied_rate", record.appliedRate.toPlainString())
            put("rate_date", record.rateDate.toString())
            put("rate_source", record.rateSource)
        }
        writableDatabase.insertOrThrow("currency_conversion_history", null, values)
    }

    /** Historia dla widoku użytkownika; limit chroni interfejs przed nieograniczonym odczytem. */
    fun readCurrencyConversions(limit: Int = 100): List<CurrencyConversionRecord> {
        require(limit in 1..1000) { "Limit historii musi mieścić się w zakresie 1–1000." }
        return readCurrencyConversionsInternal(limit.toString())
    }

    /**
     * Pełna historia do archiwizacji, deduplikacji i kontroli integralności.
     * Ta metoda celowo nie stosuje limitu prezentacyjnego.
     */
    fun readAllCurrencyConversions(): List<CurrencyConversionRecord> =
        readCurrencyConversionsInternal(null)

    private fun readCurrencyConversionsInternal(limit: String?): List<CurrencyConversionRecord> {
        val result = mutableListOf<CurrencyConversionRecord>()
        readableDatabase.query(
            "currency_conversion_history",
            arrayOf(
                "conversion_id", "converted_at", "source_amount", "source_currency_code",
                "target_amount", "target_currency_code", "applied_rate", "rate_date", "rate_source"
            ),
            null, null, null, null, "converted_at DESC, conversion_id DESC", limit
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += CurrencyConversionRecord(
                    id = cursor.getString(0),
                    convertedAt = java.time.Instant.parse(cursor.getString(1)),
                    sourceAmount = java.math.BigDecimal(cursor.getString(2)),
                    sourceCurrencyCode = cursor.getString(3),
                    targetAmount = java.math.BigDecimal(cursor.getString(4)),
                    targetCurrencyCode = cursor.getString(5),
                    appliedRate = java.math.BigDecimal(cursor.getString(6)),
                    rateDate = java.time.LocalDate.parse(cursor.getString(7)),
                    rateSource = cursor.getString(8)
                ).also { it.validate() }
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
        const val DATABASE_VERSION = 8
        const val DEFAULT_ACCOUNT_SCOPE_ID = "ipko-account-1"
        const val DEFAULT_ACCOUNT_SCOPE_NAME = "Rachunek iPKO 1"
        private const val TABLE_ASSETS = "financial_assets"
        private const val TABLE_ENTRIES = "bank_statement_entries"
        private const val TABLE_ACCOUNT_SCOPES = "payment_account_scopes"
        private const val TABLE_FINANCIAL_TRANSACTIONS = "financial_transactions"
        private const val TABLE_TRANSACTION_FEES = "financial_transaction_fees"
        private const val TABLE_TRANSACTION_BANK_ENTRIES = "financial_transaction_bank_entries"
        private const val TABLE_CASH_DONATIONS = "cash_donations"
        private const val TABLE_DONATION_ATTACHMENTS = "donation_attachments"
    }
}

data class CurrencyConversionRecord(
    val id: String,
    val convertedAt: java.time.Instant,
    val sourceAmount: java.math.BigDecimal,
    val sourceCurrencyCode: String,
    val targetAmount: java.math.BigDecimal,
    val targetCurrencyCode: String,
    val appliedRate: java.math.BigDecimal,
    val rateDate: java.time.LocalDate,
    val rateSource: String
) {
    fun validate() {
        require(id.isNotBlank() && id.length <= 128) { "Identyfikator przeliczenia jest wymagany." }
        require(sourceAmount >= java.math.BigDecimal.ZERO && targetAmount >= java.math.BigDecimal.ZERO) {
            "Kwoty przeliczenia muszą być nieujemne."
        }
        require(sourceCurrencyCode.matches(Regex("[A-Z]{3}")) &&
            targetCurrencyCode.matches(Regex("[A-Z]{3}")) &&
            sourceCurrencyCode != targetCurrencyCode) {
            "Przeliczenie wymaga dwóch różnych kodów walut ISO 4217."
        }
        require(appliedRate > java.math.BigDecimal.ZERO) { "Zastosowany kurs musi być większy od zera." }
        require(rateSource.isNotBlank() && rateSource.length <= 128) { "Źródło kursu jest wymagane." }
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
