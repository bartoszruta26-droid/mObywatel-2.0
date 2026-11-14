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
}
