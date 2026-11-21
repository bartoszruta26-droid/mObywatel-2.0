// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import pl.gov.obywatel.android.Asset
import pl.gov.obywatel.android.AssetKind
import pl.gov.obywatel.android.CashDonation
import pl.gov.obywatel.android.DonationCustodyStatus
import pl.gov.obywatel.android.Fee
import pl.gov.obywatel.android.FinancialTransaction
import pl.gov.obywatel.android.FinancialTransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FinancialReportTest {
    private val period = FinancialReportPeriod(
        Instant.parse("2026-11-01T00:00:00Z"),
        Instant.parse("2026-12-01T00:00:00Z")
    )

    private fun transaction(
        id: String,
        at: String,
        fromCode: String? = null,
        fromAmount: String? = null,
        toCode: String? = null,
        toAmount: String? = null,
        feeCode: String? = null,
        feeAmount: String? = null,
        type: FinancialTransactionType = FinancialTransactionType.ADJUSTMENT,
        description: String? = null
    ) = StoredFinancialTransaction(
        FinancialTransaction(
            id = id,
            executedAt = Instant.parse(at),
            type = type,
            fromAssetCode = fromCode,
            fromAmount = fromAmount?.let(::BigDecimal),
            toAssetCode = toCode,
            toAmount = toAmount?.let(::BigDecimal),
            fee = if (feeCode == null) null else Fee(feeCode, BigDecimal(requireNotNull(feeAmount))),
            rawDescription = description
        ),
        emptyList()
    )

    private fun donation(
        id: String,
        amount: String,
        status: DonationCustodyStatus,
        transferred: String? = null,
        receivedAt: String = "2026-11-10T12:00:00Z",
        transferredAt: String? = transferred?.let { "2026-11-20T12:00:00Z" },
        purpose: String = "Wsparcie rodzin"
    ) = CashDonation(
        id = id,
        receivedAt = Instant.parse(receivedAt),
        amount = BigDecimal(amount),
        currencyCode = "PLN",
        beneficiaryOrganizationName = "Fundacja Aria",
        purpose = purpose,
        custodyStatus = status,
        transferredAmount = transferred?.let(::BigDecimal),
        transferredAt = transferredAt?.let(Instant::parse)
    )

    @Test
    fun totalsKeepCurrenciesAndMetalUnitsSeparateAndFeesAreNotDoubleCounted() {
        val transactions = listOf(
            transaction("fx-1", "2026-11-12T10:00:00Z", "USD", "100.123456789", "CHF", "88.50", "CHF", "0.25", FinancialTransactionType.EXCHANGE),
            transaction("metal-1", "2026-11-13T10:00:00Z", "PLN", "250.00", "XAU-G", "1.2345", type = FinancialTransactionType.METAL_BUY)
        )
        val report = FinancialReportBuilder.build(
            period, transactions, emptyList(),
            listOf(
                Asset("USD", "Dolar", AssetKind.CURRENCY, "USD", 2),
                Asset("CHF", "Frank", AssetKind.CURRENCY, "CHF", 2),
                Asset("PLN", "Złoty", AssetKind.CURRENCY, "PLN", 2),
                Asset("XAU-G", "Złoto", AssetKind.PRECIOUS_METAL, "g", 4, 999)
            )
        )
        val usd = report.assetTotals.single { it.assetCode == "USD" }
        val chf = report.assetTotals.single { it.assetCode == "CHF" }
        val gold = report.assetTotals.single { it.assetCode == "XAU-G" }
        assertEquals(0, BigDecimal("100.123456789").compareTo(usd.outgoing))
        assertEquals(0, BigDecimal("88.50").compareTo(chf.incoming))
        assertEquals(0, BigDecimal("0.25").compareTo(chf.fees))
        assertEquals(0, BigDecimal("88.25").compareTo(chf.balanceChange))
        assertEquals("g", gold.unit)
        assertEquals(0, BigDecimal("1.2345").compareTo(gold.incoming))
        assertEquals(4, report.assetTotals.size)
        assertEquals(5, report.rows.count { it.recordType == "TRANSACTION" || it.recordType == "PROWIZJA" })
    }

    @Test
    fun donationTotalsSeparateAcceptedHeldDepositedTransferredAndReviewAmounts() {
        val donations = listOf(
            donation("held", "100.00", DonationCustodyStatus.HELD),
            donation("deposited", "200.00", DonationCustodyStatus.DEPOSITED),
            donation("partial", "300.00", DonationCustodyStatus.PARTIALLY_TRANSFERRED, "125.00"),
            donation("sent", "50.00", DonationCustodyStatus.TRANSFERRED, "50.00"),
            donation("review", "75.00", DonationCustodyStatus.REQUIRES_REVIEW)
        )
        val totals = FinancialReportBuilder.build(period, emptyList(), donations, emptyList()).donationTotals.single()
        assertEquals(0, BigDecimal("725.00").compareTo(totals.accepted))
        assertEquals(0, BigDecimal("175.00").compareTo(totals.transferred))
        assertEquals(0, BigDecimal("100.00").compareTo(totals.held))
        assertEquals(0, BigDecimal("200.00").compareTo(totals.deposited))
        assertEquals(0, BigDecimal("475.00").compareTo(totals.stored))
        assertEquals(0, BigDecimal("75.00").compareTo(totals.unsettledReview))
        val csv = FinancialReportCsv.encode(FinancialReportBuilder.build(period, emptyList(), donations, emptyList()))
        assertTrue(csv.contains("PRZYJĘTE"))
        assertTrue(csv.contains("PRZECHOWYWANE"))
        assertTrue(csv.contains("WPŁACONE_NA_RACHUNEK"))
        assertTrue(csv.contains("NIEROZLICZONE_WERYFIKACJA"))
    }

    @Test
    fun donationTransferIsReportedByTransferDateEvenWhenReceiptWasEarlier() {
        val donation = donation(
            "old-donation", "500.00", DonationCustodyStatus.PARTIALLY_TRANSFERRED,
            "100.00", receivedAt = "2026-10-20T12:00:00Z",
            transferredAt = "2026-11-15T12:00:00Z"
        )
        val report = FinancialReportBuilder.build(period, emptyList(), listOf(donation), emptyList())
        assertEquals(0, report.donationTotals.single().accepted.compareTo(BigDecimal.ZERO))
        assertEquals(0, BigDecimal("100.00").compareTo(report.donationTotals.single().transferred))
        assertEquals(1, report.rows.size)
        assertEquals("PRZEKAZANO", report.rows.single().event)
    }

    @Test
    fun periodUsesInclusiveStartAndExclusiveEnd() {
        val entries = listOf(
            transaction("at-start", "2026-11-01T00:00:00Z", toCode = "PLN", toAmount = "1"),
            transaction("at-end", "2026-12-01T00:00:00Z", toCode = "PLN", toAmount = "2")
        )
        val report = FinancialReportBuilder.build(period, entries, emptyList(), emptyList())
        assertEquals(1, report.rows.size)
        assertEquals("at-start", report.rows.single().recordId)
    }

    @Test
    fun csvHasUtf8BomFullQuotingAndEscapesFormulaLikeTextAndQuotes() {
        val unsafe = transaction(
            "csv-1", "2026-11-12T10:00:00Z", toCode = "PLN", toAmount = "10.0000000001",
            description = "  =HYPERLINK(\"https://bad\",\"klik\"); opis"
        )
        val report = FinancialReportBuilder.build(period, listOf(unsafe), emptyList(), emptyList())
        val csv = FinancialReportCsv.encode(report)
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("'  =HYPERLINK("))
        assertTrue(csv.contains("10.0000000001"))
        assertTrue(csv.contains(";"))
        assertTrue(csv.endsWith("\r\n"))
    }

    @Test
    fun localDateRangeIncludesBothCalendarDaysAcrossDaylightSavingTransition() {
        val period = FinancialReportPeriod.fromInclusiveDates(
            LocalDate.parse("2026-03-28"),
            LocalDate.parse("2026-03-29"),
            ZoneId.of("Europe/Warsaw")
        )
        assertTrue(period.contains(period.fromInclusive))
        assertTrue(period.contains(period.toExclusive.minusNanos(1)))
        assertFalse(period.contains(period.toExclusive))
        assertEquals(
            Instant.parse("2026-03-27T23:00:00Z"),
            period.fromInclusive
        )
        assertEquals(
            Instant.parse("2026-03-29T22:00:00Z"),
            period.toExclusive
        )
    }

    @Test
    fun localDateRangeRejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException::class.java) {
            FinancialReportPeriod.fromInclusiveDates(
                LocalDate.parse("2026-11-16"),
                LocalDate.parse("2026-11-15"),
                ZoneId.of("Europe/Warsaw")
            )
        }
    }

    @Test
    fun invalidPeriodIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FinancialReportPeriod(
                Instant.parse("2026-12-01T00:00:00Z"),
                Instant.parse("2026-11-01T00:00:00Z")
            )
        }
    }

    @Test
    fun reportIncludesBothLocalCalendarDaysAcrossDstAndExcludesFollowingMidnight() {
        val dstPeriod = FinancialReportPeriod.fromInclusiveDates(
            LocalDate.parse("2026-03-28"),
            LocalDate.parse("2026-03-29"),
            ZoneId.of("Europe/Warsaw")
        )
        val entries = listOf(
            transaction("at-local-start", "2026-03-27T23:00:00Z", toCode = "PLN", toAmount = "1"),
            transaction("last-nanosecond-of-end-day", "2026-03-29T21:59:59.999999999Z", toCode = "PLN", toAmount = "2"),
            transaction("at-following-midnight", "2026-03-29T22:00:00Z", toCode = "PLN", toAmount = "4")
        )

        val report = FinancialReportBuilder.build(dstPeriod, entries, emptyList(), emptyList())

        assertEquals(
            listOf("at-local-start", "last-nanosecond-of-end-day"),
            report.rows.map { it.recordId }
        )
        assertEquals(0, BigDecimal("3").compareTo(report.assetTotals.single().incoming))
    }
}
