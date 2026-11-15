// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import pl.gov.obywatel.android.Asset
import pl.gov.obywatel.android.CashDonation
import pl.gov.obywatel.android.DonationCustodyStatus
import pl.gov.obywatel.android.FinancialTransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Okres raportu: początek włącznie, koniec wyłącznie. */
data class FinancialReportPeriod(
    val fromInclusive: Instant,
    val toExclusive: Instant
) {
    init {
        require(fromInclusive < toExclusive) { "Początek okresu musi poprzedzać jego koniec." }
    }

    fun contains(value: Instant): Boolean =
        !value.isBefore(fromInclusive) && value.isBefore(toExclusive)

    companion object {
        /** Buduje okres z dat lokalnych; obie daty są włączne dla użytkownika. */
        fun fromInclusiveDates(
            startDate: LocalDate,
            endDateInclusive: LocalDate,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): FinancialReportPeriod {
            require(!endDateInclusive.isBefore(startDate)) {
                "Data końcowa musi przypadać tego samego dnia lub po dacie początkowej."
            }
            return FinancialReportPeriod(
                startDate.atStartOfDay(zoneId).toInstant(),
                endDateInclusive.plusDays(1).atStartOfDay(zoneId).toInstant()
            )
        }
    }
}

data class FinancialAssetTotals(
    val assetCode: String,
    val unit: String,
    val incoming: BigDecimal,
    val outgoing: BigDecimal,
    val fees: BigDecimal
) {
    val balanceChange: BigDecimal
        get() = incoming.subtract(outgoing).subtract(fees)
}

data class DonationCurrencyTotals(
    val currencyCode: String,
    val accepted: BigDecimal,
    val transferred: BigDecimal,
    val held: BigDecimal,
    val deposited: BigDecimal,
    val stored: BigDecimal,
    val unsettledReview: BigDecimal
)

data class FinancialReportRow(
    val recordType: String,
    val recordId: String,
    val occurredAt: Instant,
    val event: String,
    val assetCode: String,
    val amount: BigDecimal,
    val unit: String,
    val status: String,
    val description: String
)

data class FinancialReport(
    val period: FinancialReportPeriod,
    val assetTotals: List<FinancialAssetTotals>,
    val donationTotals: List<DonationCurrencyTotals>,
    val rows: List<FinancialReportRow>
)

/**
 * Raport bazuje na transakcjach finansowych i osobnym rejestrze darowizn.
 * Wpisy wyciągu bankowego są celowo pominięte: mogą być powiązane z transakcją
 * i ich ponowne sumowanie powodowałoby podwójne liczenie tej samej operacji.
 */
object FinancialReportBuilder {
    private val ZERO = BigDecimal.ZERO

    fun build(
        period: FinancialReportPeriod,
        transactions: List<StoredFinancialTransaction>,
        donations: List<CashDonation>,
        assets: List<Asset>
    ): FinancialReport {
        val units = assets.associate { it.code to it.unit }
        val totals = linkedMapOf<String, MutableTotals>()
        val rows = mutableListOf<FinancialReportRow>()

        fun bucket(code: String): MutableTotals =
            totals.getOrPut(code) { MutableTotals(units[code] ?: code) }

        transactions.asSequence()
            .map { it.transaction }
            .filter { period.contains(it.executedAt) }
            .sortedWith(compareBy({ it.executedAt }, { it.id }))
            .forEach { tx ->
                tx.validate()
                tx.fromAssetCode?.let { code ->
                    val amount = requireNotNull(tx.fromAmount)
                    bucket(code).outgoing = bucket(code).outgoing.add(amount)
                    rows += FinancialReportRow(
                        "TRANSACTION", tx.id, tx.executedAt, "ODDANO",
                        code, amount, units[code] ?: code, tx.parseStatus.name,
                        tx.rawDescription.orEmpty()
                    )
                }
                tx.toAssetCode?.let { code ->
                    val amount = requireNotNull(tx.toAmount)
                    bucket(code).incoming = bucket(code).incoming.add(amount)
                    rows += FinancialReportRow(
                        "TRANSACTION", tx.id, tx.executedAt, "OTRZYMANO",
                        code, amount, units[code] ?: code, tx.parseStatus.name,
                        tx.rawDescription.orEmpty()
                    )
                }
                tx.fee?.let { fee ->
                    bucket(fee.assetCode).fees = bucket(fee.assetCode).fees.add(fee.amount)
                    rows += FinancialReportRow(
                        "PROWIZJA", tx.id, tx.executedAt, "PROWIZJA",
                        fee.assetCode, fee.amount, units[fee.assetCode] ?: fee.assetCode,
                        tx.parseStatus.name, tx.rawDescription.orEmpty()
                    )
                }
            }

        val donationGroups = linkedMapOf<String, DonationMutableTotals>()
        donations.sortedWith(compareBy({ it.receivedAt }, { it.id })).forEach { donation ->
            donation.validate()
            val receivedInPeriod = period.contains(donation.receivedAt)
            val transferInPeriod = donation.transferredAt?.let(period::contains) == true
            if (receivedInPeriod || transferInPeriod) {
                val group = donationGroups.getOrPut(donation.currencyCode) { DonationMutableTotals() }
                if (receivedInPeriod) {
                    group.accepted = group.accepted.add(donation.amount)
                    val remaining = donation.amount.subtract(donation.transferredAmount ?: ZERO)
                    when (donation.custodyStatus) {
                        DonationCustodyStatus.HELD -> {
                            group.held = group.held.add(remaining)
                            group.stored = group.stored.add(remaining)
                        }
                        DonationCustodyStatus.DEPOSITED -> {
                            group.deposited = group.deposited.add(remaining)
                            group.stored = group.stored.add(remaining)
                        }
                        DonationCustodyStatus.PARTIALLY_TRANSFERRED ->
                            group.stored = group.stored.add(remaining)
                        DonationCustodyStatus.TRANSFERRED -> Unit
                        DonationCustodyStatus.REQUIRES_REVIEW ->
                            group.unsettledReview = group.unsettledReview.add(remaining)
                    }
                    rows += FinancialReportRow(
                        "DAROWIZNA", donation.id, donation.receivedAt, "PRZYJĘTO",
                        donation.currencyCode, donation.amount, donation.currencyCode,
                        donation.custodyStatus.name,
                        listOf(donation.beneficiaryOrganizationName, donation.purpose).joinToString(" — ")
                    )
                }
                if (transferInPeriod) {
                    val transferred = requireNotNull(donation.transferredAmount)
                    group.transferred = group.transferred.add(transferred)
                    rows += FinancialReportRow(
                        "DAROWIZNA", donation.id, requireNotNull(donation.transferredAt),
                        "PRZEKAZANO", donation.currencyCode, transferred,
                        donation.currencyCode, donation.custodyStatus.name,
                        donation.beneficiaryOrganizationName
                    )
                }
            }
        }

        val assetTotals = totals.toSortedMap().map { (code, value) ->
            FinancialAssetTotals(code, value.unit, value.incoming, value.outgoing, value.fees)
        }
        val donationTotals = donationGroups.toSortedMap().map { (code, value) ->
            DonationCurrencyTotals(
                code, value.accepted, value.transferred, value.held,
                value.deposited, value.stored, value.unsettledReview
            )
        }
        return FinancialReport(
            period,
            assetTotals,
            donationTotals,
            rows.sortedWith(compareBy({ it.occurredAt }, { it.recordId }, { it.event }))
        )
    }

    private data class MutableTotals(
        val unit: String,
        var incoming: BigDecimal = ZERO,
        var outgoing: BigDecimal = ZERO,
        var fees: BigDecimal = ZERO
    )

    private data class DonationMutableTotals(
        var accepted: BigDecimal = ZERO,
        var transferred: BigDecimal = ZERO,
        var held: BigDecimal = ZERO,
        var deposited: BigDecimal = ZERO,
        var stored: BigDecimal = ZERO,
        var unsettledReview: BigDecimal = ZERO
    )
}

/** CSV UTF-8 z BOM, separatorem średnikowym, pełnym quotingiem i ochroną pól tekstowych. */
object FinancialReportCsv {
    private const val BOM = "\uFEFF"
    private val HEADER = listOf(
        "typ_rekordu", "identyfikator", "data_utc", "zdarzenie",
        "kod_aktywa", "kwota", "jednostka", "status", "opis"
    )

    fun encode(report: FinancialReport): String = buildString {
        append(BOM)
        append(HEADER.joinToString(";") { quote(it, false) })
        append("\r\n")

        fun appendRow(fields: List<String>, numericAmountIndex: Int = 5) {
            append(fields.mapIndexed { index, value -> quote(value, index != numericAmountIndex) }.joinToString(";"))
            append("\r\n")
        }

        // Podsumowania aktywów: oddzielne wiersze dla każdej jednostki, bez przeliczania walut.
        report.assetTotals.forEach { total ->
            val common = listOf("PODSUMOWANIE_AKTYWA", "", report.period.fromInclusive.toString())
            listOf(
                "PRZYCHODY" to total.incoming,
                "ROZCHODY" to total.outgoing,
                "PROWIZJE" to total.fees,
                "ZMIANA_NETTO" to total.balanceChange
            ).forEach { (event, amount) ->
                appendRow(common + listOf(event, total.assetCode, amount.toPlainString(), total.unit, "OKRES", ""))
            }
        }

        // Kwoty przyjęte/przekazane są przepływami okresu; przechowywanie i kontrola
        // nierozliczonych darowizn opisują ich bieżący stan w chwili wygenerowania raportu.
        report.donationTotals.forEach { total ->
            listOf(
                "PRZYJĘTE" to total.accepted,
                "PRZEKAZANE" to total.transferred,
                "PRZECHOWYWANE" to total.stored,
                "PRZECHOWYWANE_GOTÓWKA" to total.held,
                "WPŁACONE_NA_RACHUNEK" to total.deposited,
                "NIEROZLICZONE_WERYFIKACJA" to total.unsettledReview
            ).forEach { (event, amount) ->
                appendRow(
                    listOf("PODSUMOWANIE_DAROWIZN", "", report.period.fromInclusive.toString(),
                        event, total.currencyCode, amount.toPlainString(), total.currencyCode,
                        if (event == "PRZYJĘTE" || event == "PRZEKAZANE") "OKRES" else "STAN_BIEŻĄCY", "")
                )
            }
        }

        report.rows.forEach { row ->
            appendRow(
                listOf(
                    row.recordType, row.recordId, row.occurredAt.toString(), row.event,
                    row.assetCode, row.amount.toPlainString(), row.unit, row.status, row.description
                )
            )
        }
    }

    private fun quote(value: String, protectFormula: Boolean): String {
        val safe = if (protectFormula && (value.trimStart().firstOrNull()?.let { it in setOf('=', '+', '-', '@', '\t', '\r') } == true)) {
            "'$value"
        } else value
        return "${safe.replace("\"", "\"\"")}".let { "\"$it\"" }
    }
}
