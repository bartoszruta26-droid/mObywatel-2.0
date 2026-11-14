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
     * Łączy różne zapisy kantorowe tej samej wymiany, także z różnych zakresów rachunków.
     * Nie jest kluczem deduplikacji. Data ogranicza ryzyko ponownego użycia referencji.
     */
    fun exchangeOperationId(): String? {
        if (!operationType.uppercase(Locale.ROOT).contains("WYMIANA W KANTORZE")) return null
        val reference = normalize(bankOperationReference.orEmpty())
        if (reference.isEmpty()) return null
        val identity = listOf(bookingDate.toString(), reference).joinToString("|")
        return "bank-operation-" + sha256("PKO|" + identity)
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
    /**
     * Podgląd importu rozróżnia identyczne zapisy od sprzecznych danych z tą samą
     * referencją bankową w obrębie tego samego rachunku.
     */
    fun preview(
        incoming: List<BankStatementEntry>,
        existingEntries: List<BankStatementEntry>
    ): BankImportPreview {
        val seenIds = existingEntries.map { it.accountingEntryId() }.toMutableSet()
        val seenReferences = existingEntries
            .filter { !it.bankOperationReference.isNullOrBlank() }
            .groupBy { referenceKey(it) }
            .mapValues { it.value.toMutableList() }
            .toMutableMap()
        val newEntries = mutableListOf<BankStatementEntry>()
        val duplicates = mutableListOf<BankStatementEntry>()
        val conflicts = mutableListOf<BankImportConflict>()

        incoming.forEach { entry ->
            val id = entry.accountingEntryId()
            if (id in seenIds) {
                duplicates += entry
                return@forEach
            }
            val key = if (entry.bankOperationReference.isNullOrBlank()) null else referenceKey(entry)
            val previousEntries = key?.let { seenReferences[it].orEmpty() }.orEmpty()
            if (previousEntries.isNotEmpty() && !previousEntries.all { isOppositeFxLeg(it, entry) }) {
                conflicts += BankImportConflict(entry, previousEntries.first())
                return@forEach
            }
            newEntries += entry
            seenIds += id
            if (key != null) seenReferences.getOrPut(key) { mutableListOf() }.add(entry)
        }
        return BankImportPreview(newEntries, duplicates, conflicts)
    }

    private fun referenceKey(entry: BankStatementEntry): String =
        entry.accountScopeId.trim() + "|" +
            entry.bankOperationReference.orEmpty().trim().uppercase(Locale.ROOT)

    /**
     * Jedna referencja iPKO może opisywać dwie nogi tej samej wymiany w obrębie
     * jednego rachunku. Dopuszczamy wyłącznie parę uznanie/obciążenie w różnych
     * walutach; inne rozbieżności nadal wymagają ręcznej weryfikacji.
     */
    private fun isOppositeFxLeg(first: BankStatementEntry, second: BankStatementEntry): Boolean {
        val firstType = first.operationType.uppercase(Locale.ROOT)
        val secondType = second.operationType.uppercase(Locale.ROOT)
        val isFx = firstType.contains("WYMIANA W KANTORZE") &&
            secondType.contains("WYMIANA W KANTORZE")
        val oppositeLabels =
            (firstType.contains("UZNANIE") && secondType.contains("OBCIĄŻENIE")) ||
            (firstType.contains("OBCIĄŻENIE") && secondType.contains("UZNANIE"))
        return isFx &&
            oppositeLabels &&
            first.currencyCode != second.currencyCode &&
            first.signedAmount.signum() == -second.signedAmount.signum()
    }

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

/** Rekord wymagający ręcznej weryfikacji: ta sama referencja w tym samym zakresie rachunku,
 * ale inna tożsamość księgowa (np. inna kwota, data, waluta lub kierunek). */
data class BankImportConflict(
    val incoming: BankStatementEntry,
    val existing: BankStatementEntry
)

data class BankImportPreview(
    val newEntries: List<BankStatementEntry>,
    val duplicates: List<BankStatementEntry>,
    val conflicts: List<BankImportConflict>
)

