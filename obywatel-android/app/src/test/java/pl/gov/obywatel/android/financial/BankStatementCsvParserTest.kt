// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class BankStatementCsvParserTest {
    @Test
    fun parsesSemicolonCsvWithBomPolishHeadersAndQuotedMultilineDescription() {
        val csv = "\uFEFFData księgowania;Waluta;Kwota;Typ operacji;Referencja;Opis\r\n" +
            "15.11.2026;PLN;1 234,56;UZNANIE;REF-1;\"Wpłata; opis\"\r\n" +
            "16.11.2026;EUR;-20,50;OBCIĄŻENIE;REF-2;\"linia 1\nlinia 2\"\r\n"
        val entries = BankStatementCsvParser.parse(csv, "konto-lokalne")
        assertEquals(2, entries.size)
        assertEquals(LocalDate.parse("2026-11-15"), entries[0].bookingDate)
        assertEquals(0, BigDecimal("1234.56").compareTo(entries[0].signedAmount))
        assertEquals("REF-1", entries[0].bankOperationReference)
        assertEquals(0, BigDecimal("-20.50").compareTo(entries[1].signedAmount))
    }

    @Test
    fun detectsWindows1250WhenUtf8DecodingFails() {
        val text = "data;waluta;kwota;typ;opis\n15.11.2026;PLN;10,00;PRZELEW;Wpłata za książki\n"
        val bytes = text.toByteArray(java.nio.charset.Charset.forName("windows-1250"))
        val entry = BankStatementCsvParser.parse(bytes, "konto-1").single()
        assertEquals("PLN", entry.currencyCode)
        assertEquals(true, entry.description.startsWith("CSV-OP-"))
    }

    @Test
    fun parsesCommaDelimitedEnglishHeadersAndGroupedDecimal() {
        val csv = "booking_date,currency,amount,operation_type,reference,description\n" +
            "2026-11-15,USD,\"1,234.56\",CREDIT,REF-3,\"transfer, incoming\"\n"
        val entry = BankStatementCsvParser.parse(csv, "account-1").single()
        assertEquals("USD", entry.currencyCode)
        assertEquals(0, BigDecimal("1234.56").compareTo(entry.signedAmount))
    }

    @Test
    fun hashesDescriptionWhenNoReferenceAndDoesNotPersistRawPersonalText() {
        val csv = "data;waluta;kwota;typ;opis\n15.11.2026;PLN;10,00;PRZELEW;Jan Kowalski rachunek 123456789\n"
        val entry = BankStatementCsvParser.parse(csv, "konto-1").single()
        assertFalse(entry.description.contains("Jan Kowalski"))
        assertFalse(entry.description.contains("123456789"))
        assertEquals(true, entry.description.startsWith("CSV-OP-"))
    }

    @Test
    fun rejectsMissingRequiredColumnsMalformedQuotesAndWrongColumnCount() {
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementCsvParser.parse("data;waluta;kwota;typ\n15.11.2026;PLN;10;PRZE\"LEW\n", "konto-1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementCsvParser.parse("data;waluta;kwota;typ\n15.11.2026;PLN;10;\"PRZELEW\n", "konto-1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementCsvParser.parse("data;waluta;kwota;typ\n15.11.2026;PLN;10;PRZELEW;extra\n", "konto-1")
        }

    }

    @Test
    fun rejectsInvalidDateCurrencyZeroAmountAndInvalidScope() {
        val invalidRows = listOf(
            "31.02.2026;PLN;10;PRZELEW",
            "15.11.2026;EURO;10;PRZELEW",
            "15.11.2026;PLN;0;PRZELEW"
        )
        invalidRows.forEach { row ->
            assertThrows(IllegalArgumentException::class.java) {
                BankStatementCsvParser.parse("data;waluta;kwota;typ\n$row\n", "konto-1")
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            BankStatementCsvParser.parse("data;waluta;kwota;typ\n15.11.2026;PLN;10;PRZELEW\n", "123 456")
        }
    }

    @Test
    fun byteParserAcceptsUtf8BomAndProducesEntriesForSharedDeduplication() {
        val csv = "\uFEFFdata;waluta;kwota;typ;referencja\n" +
            "15.11.2026;PLN;10,00;UZNANIE;CSV-REF-1\n"
        val bytes = csv.toByteArray(Charsets.UTF_8)
        val parsed = BankStatementCsvParser.parse(bytes, "konto-1")
        val preview = BankImportDeduplicator.preview(parsed + parsed, emptyList())
        assertEquals(1, preview.newEntries.size)
        assertEquals(1, preview.duplicates.size)
        assertEquals(0, preview.conflicts.size)
    }


    @Test
    fun parsesIpkoCsvWithTrailingUnnamedDescriptionColumnsWithoutPersistingPersonalData() {
        val csv = "\"Data operacji\",\"Data waluty\",\"Typ transakcji\",\"Kwota\",\"Waluta\",\"Saldo po transakcji\",\"Opis transakcji\",\"\",\"\",\"\",\"\"\r\n" +
            "\"2026-11-08\",\"2026-11-08\",\"WYMIANA W KANTORZE - UZNANIE\",\"+617.42\",\"CHF\",\"\",\"Rachunek nadawcy: 00000000000000000000000000\",\"Nazwa nadawcy: Osoba Testowa\",\"Adres nadawcy: ULICA TESTOWA 1\",\"Tytuł: FX102498834 CHF/PLN 4.8589 617,42 CHF\",\"Referencje własne zleceniodawcy: 102498834\"\r\n"
        val entry = BankStatementCsvParser.parse(
            csv.toByteArray(Charsets.UTF_8),
            "ipko-test-account"
        ).single()

        assertEquals(LocalDate.parse("2026-11-08"), entry.bookingDate)
        assertEquals("CHF", entry.currencyCode)
        assertEquals(0, BigDecimal("617.42").compareTo(entry.signedAmount))
        assertEquals("WYMIANA W KANTORZE - UZNANIE", entry.operationType)
        assertEquals("102498834", entry.bankOperationReference)
        assertEquals("", entry.description)
        assertFalse(entry.description.contains("Osoba Testowa"))
        assertFalse(entry.description.contains("00000000000000000000000000"))
    }

}
