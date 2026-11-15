// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.util.Currency
import java.util.Locale

/** Pozycja katalogowa ISO 4217 dostarczana przez bazę walut środowiska Java/Android. */
data class Iso4217Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val minorUnits: Int,
    val active: Boolean
)

/**
 * Katalog walut ISO 4217 niezależny od tabel publikowanych przez NBP.
 *
 * Źródłem kodów i liczby miejsc dziesiętnych jest java.util.Currency,
 * której baza jest dostarczana z platformą. Nazwy i symbole są lokalizowane
 * na język polski. Nie dopisujemy sztucznie walut rozliczeniowych ani kodów
 * specjalnych bez jednostki ISO.
 */
object Iso4217CurrencyCatalog {
    private val polish = Locale.forLanguageTag("pl-PL")

    val all: List<Iso4217Currency> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Currency.getAvailableCurrencies()
            .mapNotNull { currency ->
                val digits = currency.defaultFractionDigits
                if (digits < 0 || !currency.currencyCode.matches(Regex("[A-Z]{3}"))) {
                    null
                } else {
                    Iso4217Currency(
                        code = currency.currencyCode,
                        name = currency.getDisplayName(polish).ifBlank { currency.currencyCode },
                        symbol = currency.getSymbol(polish).ifBlank { currency.currencyCode },
                        minorUnits = digits,
                        active = true
                    )
                }
            }
            .distinctBy { it.code }
            .sortedBy { it.code }
    }

    fun find(code: String): Iso4217Currency? =
        all.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

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
