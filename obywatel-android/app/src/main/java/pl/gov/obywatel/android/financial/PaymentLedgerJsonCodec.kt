// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Wersjonowany format wymiany historii wyciągów.
 * JSON jest formatem transportowym/archiwalnym; SQLite pozostaje magazynem źródłowym.
 */
object PaymentLedgerJsonCodec {
    const val SCHEMA_VERSION = 1
    private const val FORMAT = "mobywatel-payment-ledger"

    fun encode(entries: List<BankStatementEntry>): String {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("schemaVersion", SCHEMA_VERSION)
        val array = JSONArray()
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
            array.put(item)
        }
        root.put("entries", array)
        return root.toString()
    }

    fun decode(json: String): List<BankStatementEntry> {
        require(json.isNotBlank()) { "Plik JSON jest pusty." }
        val root = try { JSONObject(json) } catch (error: Exception) {
            throw IllegalArgumentException("Nieprawidłowy dokument JSON.", error)
        }
        require(root.optString("format") == FORMAT) { "Nieobsługiwany format archiwum." }
        require(root.optInt("schemaVersion", -1) == SCHEMA_VERSION) {
            "Nieobsługiwana wersja schematu JSON."
        }
        val array = root.optJSONArray("entries")
            ?: throw IllegalArgumentException("Brak tablicy entries.")
        val result = ArrayList<BankStatementEntry>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index)
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
            result += entry
        }
        return result
    }
}
