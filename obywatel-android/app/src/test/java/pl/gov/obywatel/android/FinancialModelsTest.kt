// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FinancialModelsTest {
    @Test
    fun assetValidationAcceptsIsoCurrencyAndPreciousMetalPurity() {
        Asset("EUR", "Euro", AssetKind.CURRENCY, "unit", 2).validate()
        Asset("XAU", "Złoto", AssetKind.PRECIOUS_METAL, "g", 3, 999).validate()
    }

    @Test
    fun currencyMustUseIsoAlphaCode() {
        assertThrows(IllegalArgumentException::class.java) {
            Asset("EURO", "Euro", AssetKind.CURRENCY, "unit", 2).validate()
        }
    }

    @Test
    fun transactionRequiresPairedAssetAndAmountAndPositiveQuantities() {
        val valid = FinancialTransaction(
            id = "tx-1",
            executedAt = Instant.parse("2026-11-14T10:00:00Z"),
            type = FinancialTransactionType.EXCHANGE,
            fromAssetCode = "PLN",
            fromAmount = BigDecimal("1000.00"),
            toAssetCode = "EUR",
            toAmount = BigDecimal("230.00")
        )
        valid.validate()

        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(fromAmount = null).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(toAmount = BigDecimal.ZERO).validate()
        }
    }

    @Test
    fun feeCannotBeNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            Fee("PLN", BigDecimal("-0.01")).validate()
        }
    }

    @Test
    fun cashDonationRequiresOrganizationAndPurposeButAllowsAnonymousDonor() {
        val donation = CashDonation(
            id = "donation-1",
            receivedAt = Instant.parse("2026-11-14T10:00:00Z"),
            amount = BigDecimal("250.00"),
            currencyCode = "PLN",
            beneficiaryOrganizationName = "Fundacja Przykładowa",
            purpose = "Zakup materiałów edukacyjnych",
            isAnonymous = true
        )
        donation.validate()

        assertThrows(IllegalArgumentException::class.java) {
            donation.copy(beneficiaryOrganizationName = "  ").validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            donation.copy(purpose = "").validate()
        }
    }

    @Test
    fun donationTransferCannotExceedReceivedAmountOrHavePartialTimestamp() {
        val donation = CashDonation(
            id = "donation-2",
            receivedAt = Instant.parse("2026-11-14T10:00:00Z"),
            amount = BigDecimal("250.00"),
            currencyCode = "PLN",
            beneficiaryOrganizationName = "Fundacja Przykładowa",
            purpose = "Cel",
            transferredAt = Instant.parse("2026-11-15T10:00:00Z"),
            transferredAmount = BigDecimal("200.00")
        )
        donation.validate()
        assertThrows(IllegalArgumentException::class.java) {
            donation.copy(transferredAmount = BigDecimal("251.00")).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            donation.copy(transferredAt = null).validate()
        }
    }

    @Test
    fun financialCalculationsUseExactDecimalInputs() {
        assertEquals(
            BigDecimal("4.347826086956522"),
            FinancialCalculator.unitRate(BigDecimal("1000"), BigDecimal("230"))
        )
        assertEquals(
            BigDecimal("15.00"),
            FinancialCalculator.percentageChange(BigDecimal("115"), BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_EVEN)
        )
    }
}
