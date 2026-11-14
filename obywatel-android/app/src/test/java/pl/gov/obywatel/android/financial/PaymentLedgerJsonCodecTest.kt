// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PaymentLedgerJsonCodecTest {
    private val sample = BankStatementEntry(
        accountScopeId = "konto-testowe",
        bookingDate = LocalDate.parse("2026-11-08"),
        currencyCode = "CHF",
        signedAmount = BigDecimal("617.4200"),
        operationType = "UZNANIE",
        bankOperationReference = "REF-123",
        description = "Przelew"
    )

    @Test fun roundTripPreservesExactDecimalAndIdentity() {
        val decoded = PaymentLedgerJsonCodec.decode(PaymentLedgerJsonCodec.encode(listOf(sample)))
        assertEquals(1, decoded.size)
        assertEquals(0, sample.signedAmount.compareTo(decoded.single().signedAmount))
        assertEquals(sample.accountingEntryId(), decoded.single().accountingEntryId())
    }

    @Test fun roundTripPreservesNullReferenceAndNegativeAmount() {
        val entry = sample.copy(
            signedAmount = BigDecimal("-8000.00"),
            bankOperationReference = null
        )
        val decoded = PaymentLedgerJsonCodec.decode(PaymentLedgerJsonCodec.encode(listOf(entry))).single()
        assertEquals(-1, decoded.signedAmount.signum())
        assertNull(decoded.bankOperationReference)
        assertEquals(entry.accountingEntryId(), decoded.accountingEntryId())
    }

    @Test fun rejectsUnknownSchemaVersion() {
        val json = PaymentLedgerJsonCodec.encode(listOf(sample))
            .replace("\"schemaVersion\":1", "\"schemaVersion\":99")
        assertThrows(IllegalArgumentException::class.java) {
            PaymentLedgerJsonCodec.decode(json)
        }
    }

    @Test fun rejectsInvalidCurrencyAndMalformedJson() {
        val invalid = sample.copy(currencyCode = "PL")
        assertThrows(IllegalArgumentException::class.java) {
            PaymentLedgerJsonCodec.encode(listOf(invalid))
        }
        assertThrows(IllegalArgumentException::class.java) {
            PaymentLedgerJsonCodec.decode("{")
        }
    }
    @Test fun schemaTwoRoundTripIncludesFinancialTransactionAndFee() {
        val transaction = pl.gov.obywatel.android.FinancialTransaction(
            id = "tx-json-1",
            executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
            type = pl.gov.obywatel.android.FinancialTransactionType.EXCHANGE,
            fromAssetCode = "USD", fromAmount = BigDecimal("8000.00"),
            toAssetCode = "CHF", toAmount = BigDecimal("6183.20"),
            fee = pl.gov.obywatel.android.Fee("CHF", BigDecimal("5.25"))
        )
        val stored = StoredFinancialTransaction(transaction, listOf(sample.accountingEntryId()))
        val archive = PaymentLedgerJsonCodec.decodeArchive(
            PaymentLedgerJsonCodec.encode(listOf(sample), listOf(stored))
        )
        assertEquals(listOf(sample.accountingEntryId()), archive.transactions.single().accountingEntryIds)
        assertEquals(0, BigDecimal("8000.00").compareTo(archive.transactions.single().transaction.fromAmount))
        assertEquals(0, BigDecimal("5.25").compareTo(archive.transactions.single().transaction.fee!!.amount))
    }

    @Test fun schemaOneArchiveRemainsReadable() {
        val legacy = PaymentLedgerJsonCodec.encode(listOf(sample))
            .replace("\\"schemaVersion\\":2", "\\"schemaVersion\\":1")
            .replace(",\\"transactions\\":[]", "")
        val archive = PaymentLedgerJsonCodec.decodeArchive(legacy)
        assertEquals(1, archive.entries.size)
        assertEquals(0, archive.transactions.size)
    }

    @Test fun rejectsInvalidTransactionInArchive() {
        val transaction = pl.gov.obywatel.android.FinancialTransaction(
            id = "tx-invalid",
            executedAt = java.time.Instant.parse("2026-11-12T10:15:30Z"),
            type = pl.gov.obywatel.android.FinancialTransactionType.EXCHANGE,
            fromAssetCode = "USD", fromAmount = BigDecimal("10"),
            toAssetCode = "USD", toAmount = BigDecimal("10")
        )
        assertThrows(IllegalArgumentException::class.java) {
            PaymentLedgerJsonCodec.encode(listOf(sample), listOf(StoredFinancialTransaction(transaction, emptyList())))
        }
    }
}
