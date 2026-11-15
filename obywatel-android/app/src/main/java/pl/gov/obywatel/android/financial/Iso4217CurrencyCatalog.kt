// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.util.Currency
import java.util.Locale

/** Aktywna pozycja katalogowa ISO 4217. */
data class Iso4217Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val minorUnits: Int,
    val active: Boolean,
    val numericCode: String = "",
    val isFund: Boolean = false
)

/** Kod wycofany według List Three; data pozostaje w formacie publikacji SIX (YYYY-MM lub zakres). */
data class Iso4217HistoricalCurrency(
    val code: String,
    val numericCode: String,
    val englishName: String,
    val withdrawnAt: String
)

/**
 * Wersjonowany katalog oparty na migawkach list utrzymywanych przez SIX, oficjalną
 * agencję utrzymania ISO 4217. Symbole i lokalne nazwy są prezentacją Androida,
 * ponieważ nie stanowią pól normatywnego rejestru ISO 4217.
 */
object Iso4217CurrencyCatalog {
    private val polish = Locale.forLanguageTag("pl-PL")

    const val version: String = Iso4217CurrencySnapshot.CURRENT_VERSION
    const val historicalVersion: String = Iso4217CurrencySnapshot.HISTORICAL_VERSION
    const val source: String = Iso4217CurrencySnapshot.CURRENT_SOURCE
    const val historicalSource: String = Iso4217CurrencySnapshot.HISTORICAL_SOURCE

    val all: List<Iso4217Currency> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Iso4217CurrencySnapshot.current
            .asSequence()
            .filter { !it.fund && it.minorUnits != null }
            .map { entry ->
                val currency = runCatching { Currency.getInstance(entry.code) }.getOrNull()
                Iso4217Currency(
                    code = entry.code,
                    name = currency?.getDisplayName(polish)?.takeIf { it.isNotBlank() }
                        ?: entry.englishName,
                    symbol = currency?.getSymbol(polish)?.takeIf { it.isNotBlank() }
                        ?: entry.code,
                    minorUnits = requireNotNull(entry.minorUnits),
                    active = true,
                    numericCode = entry.numericCode,
                    isFund = entry.fund
                )
            }
            .distinctBy { it.code }
            .sortedBy { it.code }
            .toList()
    }

    /** Waluty historyczne, których kod nie figuruje już w bieżącej Liście One. */
    val historical: List<Iso4217HistoricalCurrency> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val activeCodes = Iso4217CurrencySnapshot.current.mapTo(hashSetOf()) { it.code }
        Iso4217CurrencySnapshot.historical
            .asSequence()
            .filter { it.code !in activeCodes && it.withdrawnAt != null }
            .map {
                Iso4217HistoricalCurrency(
                    code = it.code,
                    numericCode = it.numericCode,
                    englishName = it.englishName,
                    withdrawnAt = requireNotNull(it.withdrawnAt)
                )
            }
            .distinctBy { it.code }
            .sortedBy { it.code }
            .toList()
    }

    fun find(code: String): Iso4217Currency? =
        all.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun findHistorical(code: String): Iso4217HistoricalCurrency? =
        historical.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    /** Wyszukiwanie po kodzie, polskiej nazwie lub symbolu. */
    fun search(query: String, limit: Int = 12): List<Iso4217Currency> {
        require(limit in 1..100) { "Limit wyników musi mieścić się w zakresie 1–100." }
        val normalized = query.trim().lowercase(polish)
        if (normalized.isEmpty()) return all.take(limit)
        return all.asSequence()
            .filter {
                it.code.lowercase(polish).contains(normalized) ||
                    it.name.lowercase(polish).contains(normalized) ||
                    it.symbol.lowercase(polish).contains(normalized)
            }
            .take(limit)
            .toList()
    }
}
