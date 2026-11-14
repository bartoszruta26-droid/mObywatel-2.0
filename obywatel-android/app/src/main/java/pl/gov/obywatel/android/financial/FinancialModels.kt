// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant

/** Klasyfikacja aktywa przechowywanego w rejestrze finansowym. */
enum class AssetKind { CURRENCY, PRECIOUS_METAL }

/** Jednostka aktywa; dla metali kod określa masę, a próba pozostaje osobnym polem. */
data class Asset(
    val code: String,
    val name: String,
    val kind: AssetKind,
    val unit: String,
    val decimalPlaces: Int,
    val purity: Int? = null
) {
    fun validate() {
        require(code.isNotBlank()) { "Kod aktywa jest wymagany." }
        require(name.isNotBlank()) { "Nazwa aktywa jest wymagana." }
        require(unit.isNotBlank()) { "Jednostka aktywa jest wymagana." }
        require(decimalPlaces in 0..12) { "Nieprawidłowa precyzja aktywa." }
        if (kind == AssetKind.CURRENCY) {
            require(code.matches(Regex("[A-Z]{3}"))) { "Waluta musi mieć trzyliterowy kod ISO 4217." }
            require(purity == null) { "Waluta nie może mieć próby kruszcu." }
        } else {
            require(purity == null || purity in 1..1000) { "Próba kruszcu musi mieścić się w zakresie 1–1000." }
        }
    }
}

enum class FinancialTransactionType { EXCHANGE, METAL_BUY, METAL_SELL, DEPOSIT, WITHDRAWAL, ADJUSTMENT }
enum class FinancialRecordSource { MANUAL, IPKO_IMPORT, JSON_IMPORT }
enum class FinancialParseStatus { MANUAL, PARSED, NEEDS_REVIEW, VERIFIED, REJECTED }

/** Prowizja jest wyrażona w jednostkach wskazanego aktywa. */
data class Fee(
    val assetCode: String,
    val amount: BigDecimal
) {
    fun validate() {
        require(assetCode.isNotBlank()) { "Kod aktywa prowizji jest wymagany." }
        require(amount >= BigDecimal.ZERO) { "Prowizja nie może być ujemna." }
    }
}

/**
 * Zapisuje obie strony wymiany bez utraty pierwotnych ilości.
 * Brak jednej ze stron jest dopuszczalny dla wpłaty, wypłaty lub korekty.
 */
data class FinancialTransaction(
    val id: String,
    val executedAt: Instant,
    val type: FinancialTransactionType,
    val fromAssetCode: String?,
    val fromAmount: BigDecimal?,
    val toAssetCode: String?,
    val toAmount: BigDecimal?,
    val fee: Fee? = null,
    val source: FinancialRecordSource = FinancialRecordSource.MANUAL,
    val externalId: String? = null,
    val rawDescription: String? = null,
    val parseStatus: FinancialParseStatus = FinancialParseStatus.MANUAL
) {
    fun validate() {
        require(id.isNotBlank()) { "Identyfikator transakcji jest wymagany." }
        require((fromAssetCode == null) == (fromAmount == null)) { "Aktywo i ilość strony oddanej muszą występować razem." }
        require((toAssetCode == null) == (toAmount == null)) { "Aktywo i ilość strony otrzymanej muszą występować razem." }
        require(fromAssetCode != null || toAssetCode != null) { "Transakcja musi obejmować co najmniej jedną stronę." }
        require(fromAmount == null || fromAmount > BigDecimal.ZERO) { "Ilość strony oddanej musi być dodatnia." }
        require(toAmount == null || toAmount > BigDecimal.ZERO) { "Ilość strony otrzymanej musi być dodatnia." }
        require(fromAssetCode == null || fromAssetCode.isNotBlank()) { "Kod aktywa oddanego jest pusty." }
        require(toAssetCode == null || toAssetCode.isNotBlank()) { "Kod aktywa otrzymanego jest pusty." }
        when (type) {
            FinancialTransactionType.EXCHANGE,
            FinancialTransactionType.METAL_BUY,
            FinancialTransactionType.METAL_SELL -> {
                require(fromAssetCode != null && toAssetCode != null) {
                    "Wymiana i kupno/sprzedaż kruszcu wymagają obu stron transakcji."
                }
                require(fromAssetCode != toAssetCode) {
                    "Aktywo oddane i otrzymane muszą być różne."
                }
            }
            FinancialTransactionType.DEPOSIT -> require(fromAssetCode == null && toAssetCode != null) {
                "Wpłata musi wskazywać wyłącznie aktywo otrzymane."
            }
            FinancialTransactionType.WITHDRAWAL -> require(fromAssetCode != null && toAssetCode == null) {
                "Wypłata musi wskazywać wyłącznie aktywo oddane."
            }
            FinancialTransactionType.ADJUSTMENT -> Unit
        }
        fee?.let {
            it.validate()
            require(it.assetCode == fromAssetCode || it.assetCode == toAssetCode) {
                "Prowizja musi być wyrażona w aktywie jednej ze stron transakcji."
            }
        }
    }
}

/** Osobny model darowizny — nie jest transakcją wymiany ani sprzedaży aktywa. */
data class CashDonation(
    val id: String,
    val receivedAt: Instant,
    val amount: BigDecimal,
    val currencyCode: String,
    val beneficiaryOrganizationName: String,
    val purpose: String,
    val donorName: String? = null,
    val isAnonymous: Boolean = false,
    val campaignName: String? = null,
    val collectedBy: String? = null,
    val receiptNumber: String? = null,
    val custodyStatus: DonationCustodyStatus = DonationCustodyStatus.HELD,
    val transferredAt: Instant? = null,
    val transferredAmount: BigDecimal? = null,
    val note: String? = null,
    val source: FinancialRecordSource = FinancialRecordSource.MANUAL
) {
    fun validate() {
        require(id.isNotBlank()) { "Identyfikator darowizny jest wymagany." }
        require(amount > BigDecimal.ZERO) { "Kwota darowizny musi być dodatnia." }
        require(currencyCode.matches(Regex("[A-Z]{3}"))) { "Waluta musi mieć trzyliterowy kod ISO 4217." }
        require(beneficiaryOrganizationName.isNotBlank()) { "Nazwa organizacji-beneficjenta jest obowiązkowa." }
        require(purpose.isNotBlank()) { "Cel darowizny jest obowiązkowy." }
        require(transferredAmount == null || transferredAmount >= BigDecimal.ZERO) { "Kwota przekazania nie może być ujemna." }
        require(transferredAmount == null || transferredAmount <= amount) { "Kwota przekazania przekracza przyjętą darowiznę." }
        require((transferredAt == null) == (transferredAmount == null)) { "Data i kwota przekazania muszą występować razem." }
    }
}

enum class DonationCustodyStatus { HELD, DEPOSITED, PARTIALLY_TRANSFERRED, TRANSFERRED, REQUIRES_REVIEW }

/** Obliczenia finansowe bez użycia Float/Double. */
object FinancialCalculator {
    private val divisionContext = MathContext(16, RoundingMode.HALF_EVEN)

    fun unitRate(amountPaid: BigDecimal, amountReceived: BigDecimal): BigDecimal {
        require(amountPaid > BigDecimal.ZERO) { "Kwota oddana musi być dodatnia." }
        require(amountReceived > BigDecimal.ZERO) { "Kwota otrzymana musi być dodatnia." }
        return amountPaid.divide(amountReceived, divisionContext)
    }

    fun percentageChange(currentValue: BigDecimal, acquisitionCost: BigDecimal): BigDecimal {
        require(acquisitionCost > BigDecimal.ZERO) { "Koszt nabycia musi być dodatni." }
        return currentValue.subtract(acquisitionCost)
            .multiply(BigDecimal("100"))
            .divide(acquisitionCost, divisionContext)
    }
}
