// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.json.JSONArray
import org.json.JSONObject
import pl.gov.obywatel.android.Fee
import pl.gov.obywatel.android.FinancialParseStatus
import pl.gov.obywatel.android.FinancialRecordSource
import pl.gov.obywatel.android.FinancialTransaction
import pl.gov.obywatel.android.FinancialTransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Wersjonowany format kompletnego rejestru. SQLite pozostaje magazynem źródłowym.
 * Wersja 1 (same wpisy bankowe) pozostaje czytelna; wersja 2 dodaje transakcje.
 */
object PaymentLedgerJsonCodec {
    const val SCHEMA_VERSION = 2
    private const val LEGACY_SCHEMA_VERSION = 1
    private const val FORMAT = "mobywatel-payment-ledger"

    data class Archive(
        val entries: List<BankStatementEntry>,
        val transactions: List<StoredFinancialTransaction>
    )

    fun encode(entries: List<BankStatementEntry>): String = encode(entries, emptyList())

    fun encode(
        entries: List<BankStatementEntry>,
        transactions: List<StoredFinancialTransaction>
    ): String {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
        val entryArray = JSONArray()
        entries.forEach { entry ->
            entry.validate()
            val item = JSONObject()
                .put("accountScopeId", entry.accountScopeId.trim())
                .put("bookingDate", entry.bookingDate.toString())
                .put("currencyCode", entry.currencyCode)
                .put("signedAmount", entry.signedAmount.stripTrailingZeros().toPlainString())
                .put("operationType", entry.operationType.trim())
                .put("description", entry.description)
            if (entry.bankOperationReference == null) item.put("bankOperationReference", JSONObject.NULL)
            else item.put("bankOperationReference", entry.bankOperationReference)
            entryArray.put(item)
        }
        root.put("entries", entryArray)

        val transactionArray = JSONArray()
        transactions.forEach { stored ->
            val tx = stored.transaction
            tx.validate()
            val item = JSONObject()
                .put("id", tx.id)
                .put("executedAt", tx.executedAt.toString())
                .put("type", tx.type.name)
                .put("source", tx.source.name)
                .put("parseStatus", tx.parseStatus.name)
                .put("accountingEntryIds", JSONArray(stored.accountingEntryIds))
            putNullable(item, "fromAssetCode", tx.fromAssetCode)
            putNullable(item, "fromAmount", tx.fromAmount?.stripTrailingZeros()?.toPlainString())
            putNullable(item, "toAssetCode", tx.toAssetCode)
            putNullable(item, "toAmount", tx.toAmount?.stripTrailingZeros()?.toPlainString())
            putNullable(item, "externalId", tx.externalId)
            putNullable(item, "rawDescription", tx.rawDescription)
            tx.fee?.let { item.put("fee", JSONObject()
                .put("assetCode", it.assetCode)
                .put("amount", it.amount.stripTrailingZeros().toPlainString())) }
            transactionArray.put(item)
        }
        root.put("transactions", transactionArray)
        return root.toString()
    }

    /** Zachowuje dotychczasowe API dla parserów i podglądu importu bankowego. */
    fun decode(json: String): List<BankStatementEntry> = decodeArchive(json).entries

    fun decodeArchive(json: String): Archive {
        require(json.isNotBlank()) { "Plik JSON jest pusty." }
        val root = try { JSONObject(json) } catch (error: Exception) {
            throw IllegalArgumentException("Nieprawidłowy dokument JSON.", error)
        }
        require(root.optString("format") == FORMAT) { "Nieobsługiwany format archiwum." }
        val version = root.optInt("schemaVersion", -1)
        require(version == LEGACY_SCHEMA_VERSION || version == SCHEMA_VERSION) {
            "Nieobsługiwana wersja schematu JSON."
        }
        val entriesArray = root.optJSONArray("entries")
            ?: throw IllegalArgumentException("Brak tablicy entries.")
        val entries = ArrayList<BankStatementEntry>(entriesArray.length())
        for (index in 0 until entriesArray.length()) {
            val item = entriesArray.optJSONObject(index)
                ?: throw IllegalArgumentException("Nieprawidłowy wpis JSON pod indeksem $index.")
            val entry = try {
                BankStatementEntry(
                    accountScopeId = item.getString("accountScopeId"),
                    bookingDate = LocalDate.parse(item.getString("bookingDate")),
                    currencyCode = item.getString("currencyCode"),
                    signedAmount = BigDecimal(item.getString("signedAmount")),
                    operationType = item.getString("operationType"),
                    bankOperationReference = if (item.isNull("bankOperationReference")) null
                        else item.getString("bankOperationReference"),
                    description = item.optString("description", "")
                )
            } catch (error: Exception) {
                throw IllegalArgumentException("Nieprawidłowy wpis JSON pod indeksem $index.", error)
            }
            entry.validate()
            entries += entry
        }

        val transactions = mutableListOf<StoredFinancialTransaction>()
        val txArray = root.optJSONArray("transactions")
        if (version == SCHEMA_VERSION && txArray == null) {
            throw IllegalArgumentException("Brak tablicy transactions w schemacie 2.")
        }
        if (txArray != null) for (index in 0 until txArray.length()) {
            val item = txArray.optJSONObject(index)
                ?: throw IllegalArgumentException("Nieprawidłowa transakcja JSON pod indeksem $index.")
            try {
                val feeObject = item.optJSONObject("fee")
                val tx = FinancialTransaction(
                    id = item.getString("id"),
                    executedAt = Instant.parse(item.getString("executedAt")),
                    type = FinancialTransactionType.valueOf(item.getString("type")),
                    fromAssetCode = nullableString(item, "fromAssetCode"),
                    fromAmount = nullableDecimal(item, "fromAmount"),
                    toAssetCode = nullableString(item, "toAssetCode"),
                    toAmount = nullableDecimal(item, "toAmount"),
                    fee = feeObject?.let { Fee(it.getString("assetCode"), BigDecimal(it.getString("amount"))) },
                    source = FinancialRecordSource.valueOf(item.getString("source")),
                    externalId = nullableString(item, "externalId"),
                    rawDescription = nullableString(item, "rawDescription"),
                    parseStatus = FinancialParseStatus.valueOf(item.getString("parseStatus"))
                )
                tx.validate()
                val ids = item.optJSONArray("accountingEntryIds")
                    ?: throw IllegalArgumentException("Brak accountingEntryIds.")
                val linkedIds = (0 until ids.length()).map { ids.getString(it) }
                require(linkedIds.distinct().size == linkedIds.size) { "Powtórzone powiązania zapisów bankowych." }
                transactions += StoredFinancialTransaction(tx, linkedIds)
            } catch (error: Exception) {
                throw IllegalArgumentException("Nieprawidłowa transakcja JSON pod indeksem $index.", error)
            }
        }
        return Archive(entries, transactions)
    }

    private fun putNullable(item: JSONObject, key: String, value: String?) {
        if (value == null) item.put(key, JSONObject.NULL) else item.put(key, value)
    }

    private fun nullableString(item: JSONObject, key: String): String? =
        if (!item.has(key) || item.isNull(key)) null else item.getString(key)

    private fun nullableDecimal(item: JSONObject, key: String): BigDecimal? =
        nullableString(item, key)?.let(::BigDecimal)
}
