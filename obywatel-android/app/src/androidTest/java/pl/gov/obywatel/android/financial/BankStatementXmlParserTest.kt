// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class BankStatementXmlParserTest {
    private val fixture = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account-history>
          <search>
            <account>00000000000000000000000000</account>
            <date since="2025-11-14" to="2026-11-14"/>
            <get-hold-list>YES</get-hold-list>
            <filtering>Wszystkie</filtering>
          </search>
          <operations>
            <operation>
              <order-date>2026-11-12</order-date>
              <exec-date>2026-11-12</exec-date>
              <type>WYMIANA W KANTORZE - UZNANIE</type>
              <description>Rachunek nadawcy : 11111111111111111111111111 Nazwa nadawcy : Jan Kowalski Adres nadawcy : UL. TESTOWA 1 Tytuł : FX102693731 USD/PLN 4.0036 749,33 USD -3 000,00 PLN Referencje własne zleceniodawcy : 102693731</description>
              <amount curr="USD">+749.33</amount>
            </operation>
            <operation>
              <order-date>2026-11-12</order-date>
              <exec-date>2026-11-12</exec-date>
              <type>WYMIANA W KANTORZE - UZNANIE</type>
              <description>Tytuł : FX102678666 USD/PLN 3.9931 751,30 USD Referencje własne zleceniodawcy : 102678666</description>
              <amount curr="USD">+751.30</amount>
            </operation>
            <operation>
              <order-date>2026-03-04</order-date>
              <exec-date>2026-03-04</exec-date>
              <type>WYMIANA W KANTORZE - OBCIĄŻENIE</type>
              <description>Tytuł : FX90124777 USD/CHF 0.7729 6 183,20 CHF -8 000,00 USD Referencje własne zleceniodawcy : 90124777</description>
              <amount curr="USD">-8000.00</amount>
            </operation>
            <operation>
              <order-date>2025-12-20</order-date>
              <exec-date>2025-12-20</exec-date>
              <type>WYMIANA W KANTORZE - UZNANIE</type>
              <description>Tytuł : FX86421366 USD/PLN 3.6326 825,85 USD Referencje własne zleceniodawcy : 86421366</description>
              <amount curr="USD">+825.85</amount>
            </operation>
            <operation>
              <order-date>2025-12-17</order-date>
              <exec-date>2025-12-17</exec-date>
              <type>WYMIANA W KANTORZE - UZNANIE</type>
              <description>Tytuł : FX86236675 USD/PLN 3.6187 829,03 USD Referencje własne zleceniodawcy : 86236675</description>
              <amount curr="USD">+829.03</amount>
            </operation>
          </operations>
        </account-history>
    """.trimIndent()

    @Test
    fun parsesAllFiveOperationsAndPreservesDecimalValues() {
        val entries = BankStatementXmlParser.parse(fixture, "pkobp-main-account")

        assertEquals(5, entries.size)
        assertEquals(LocalDate.parse("2026-11-12"), entries[0].bookingDate)
        assertEquals("USD", entries[0].currencyCode)
        assertEquals(BigDecimal("749.33"), entries[0].signedAmount)
        assertEquals("102693731", entries[0].bankOperationReference)
        assertEquals(BigDecimal("-8000.00"), entries[2].signedAmount)
        assertEquals("90124777", entries[2].bankOperationReference)
        assertEquals(LocalDate.parse("2025-12-17"), entries[4].bookingDate)
        assertEquals("pkobp-main-account", entries[0].accountScopeId)
    }

    @Test
    fun doesNotPersistCounterpartyAccountNameOrAddress() {
        val entries = BankStatementXmlParser.parse(fixture, "local-account-alias")
        val serialized = entries.joinToString(" ") { it.description + " " + it.accountScopeId }

        assertFalse(serialized.contains("11111111111111111111111111"))
        assertFalse(serialized.contains("Jan Kowalski"))
        assertFalse(serialized.contains("UL. TESTOWA"))
        assertFalse(serialized.contains("00000000000000000000000000"))
        assertEquals("", entries.first().description)
    }

    @Test
    fun rejectsMissingLocalAccountScopeAndMalformedOperation() {
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementXmlParser.parse(fixture, " ")
        }
        val malformedOperation = """
            <account-history><operations><operation>
              <type>WYMIANA</type><amount curr="USD">0</amount>
            </operation></operations></account-history>
        """.trimIndent()
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementXmlParser.parse(malformedOperation, "local-account-alias")
        }
    }
}
