// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.util.Locale

/**
 * Projekcja operacji wymiany z zachowaniem źródłowych zapisów księgowych.
 * Nie zastępuje rekordów BankStatementEntry i nie wykonuje przewalutowania.
 */
data class BankExchangeOperation(
    val operationId: String,
    val legs: List<BankStatementEntry>,
    val status: ExchangeOperationStatus
) {
    val currencies: Set<String> get() = legs.map { it.currencyCode }.toSet()
}

enum class ExchangeOperationStatus {
    COMPLETE,
    SINGLE_LEG,
    REVIEW_REQUIRED
}

/**
 * Grupuje nogi wyciągu według identyfikatora operacji FX.
 *
 * Para COMPLETE musi zawierać dokładnie dwa zapisy kantorowe, w różnych walutach,
 * z przeciwnymi kierunkami księgowymi. Nie usuwa ani nie scala zapisów źródłowych.
 * Grupy niepełne i wieloelementowe pozostają widoczne jako wymagające kontroli.
 */
object BankExchangeOperationProjector {
    fun project(entries: List<BankStatementEntry>): List<BankExchangeOperation> {
        return entries
            .mapNotNull { entry ->
                val operationId = entry.exchangeOperationId() ?: return@mapNotNull null
                operationId to entry
            }
            .groupBy({ it.first }, { it.second })
            .map { (operationId, legs) ->
                val status = when {
                    legs.size == 1 -> ExchangeOperationStatus.SINGLE_LEG
                    legs.size == 2 && isValidPair(legs[0], legs[1]) ->
                        ExchangeOperationStatus.COMPLETE
                    else -> ExchangeOperationStatus.REVIEW_REQUIRED
                }
                BankExchangeOperation(
                    operationId = operationId,
                    legs = legs.sortedWith(
                        compareBy<BankStatementEntry> { it.bookingDate }
                            .thenBy { it.currencyCode }
                            .thenBy { it.signedAmount }
                    ),
                    status = status
                )
            }
            .sortedBy { it.operationId }
    }

    private fun isValidPair(first: BankStatementEntry, second: BankStatementEntry): Boolean {
        val firstType = first.operationType.uppercase(Locale.ROOT)
        val secondType = second.operationType.uppercase(Locale.ROOT)
        val bothExchange = firstType.contains("WYMIANA W KANTORZE") &&
            secondType.contains("WYMIANA W KANTORZE")
        val oppositeDirections = first.signedAmount.signum() == -second.signedAmount.signum()
        val distinctCurrencies = first.currencyCode != second.currencyCode
        val oneDebitOneCredit = listOf(first, second).count {
            it.signedAmount.compareTo(BigDecimal.ZERO) < 0
        } == 1
        return bothExchange && oppositeDirections && distinctCurrencies && oneDebitOneCredit
    }
}
