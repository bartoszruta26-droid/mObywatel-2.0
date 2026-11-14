// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDate
import java.util.Locale

/**
 * Znormalizowany pojedynczy zapis wyciągu bankowego.
 *
 * accountScopeId powinien być lokalnym, nieodwracalnym identyfikatorem zakresu rachunku,
 * a nie pełnym numerem rachunku. amount jest podpisana: uznanie dodatnie, obciążenie ujemne.
 */
data class BankStatementEntry(
    val accountScopeId: String,
    val bookingDate: LocalDate,
    val currencyCode: String,
    val signedAmount: BigDecimal,
    val operationType: String,
    val bankOperationReference: String? = null,
    val description: String = ""
) {
    fun validate() {
        require(accountScopeId.isNotBlank()) { "Zakres rachunku jest wymagany." }
        require(currencyCode.matches(Regex("[A-Z]{3}"))) { "Waluta musi mieć kod ISO 4217." }
        require(signedAmount.compareTo(BigDecimal.ZERO) != 0) { "Kwota zapisu nie może wynosić zero." }
        require(operationType.isNotBlank()) { "Typ operacji jest wymagany." }
    }

    /** Identyfikator pojedynczego zapisu księgowego — niezależny od formatu pliku. */
    fun accountingEntryId(): String {
        validate()
        val reference = normalize(bankOperationReference.orEmpty())
        val identity = if (reference.isNotEmpty()) {
            listOf(
                accountScopeId.trim(), bookingDate.toString(), currencyCode,
                signedAmount.stripTrailingZeros().toPlainString(),
                if (signedAmount.signum() > 0) "CREDIT" else "DEBIT",
                normalize(operationType), reference
            ).joinToString("|")
        } else {
            listOf(
                accountScopeId.trim(), bookingDate.toString(), currencyCode,
                signedAmount.stripTrailingZeros().toPlainString(),
                if (signedAmount.signum() > 0) "CREDIT" else "DEBIT",
                normalize(operationType), normalize(description)
            ).joinToString("|")
        }
        return "bank-entry-" + sha256(identity)
    }

    /**
     * Łączy różne zapisy (np. obciążenie USD i uznanie CHF) należące do jednej wymiany.
     * Nie służy do deduplikacji zapisów — dwa zapisy z tym samym numerem FX pozostają osobne.
     */
    fun exchangeOperationId(): String? {
        val reference = normalize(bankOperationReference.orEmpty())
        if (reference.isEmpty()) return null
        return "bank-operation-" + sha256("PKO|" + reference)
    }

    private fun normalize(value: String): String =
        value.trim().uppercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/** Wynik planowania importu. Nie modyfikuje magazynu i nie usuwa żadnych zapisów. */
data class ImportDeduplicationResult(
    val newEntries: List<BankStatementEntry>,
    val duplicates: List<BankStatementEntry>
)

/**
 * Deduplikacja zarówno wewnątrz paczki (CSV+XLS+XML+PDF), jak i względem istniejącego rejestru.
 * Klucz obejmuje konto, datę, walutę, kwotę, kierunek, typ i referencję bankową.
 */
object BankImportDeduplicator {
    fun classify(
        incoming: List<BankStatementEntry>,
        existingAccountingEntryIds: Set<String>
    ): ImportDeduplicationResult {
        val seen = existingAccountingEntryIds.toMutableSet()
        val newEntries = mutableListOf<BankStatementEntry>()
        val duplicates = mutableListOf<BankStatementEntry>()
        incoming.forEach { entry ->
            val id = entry.accountingEntryId()
            if (seen.add(id)) newEntries += entry else duplicates += entry
        }
        return ImportDeduplicationResult(newEntries, duplicates)
    }
}
