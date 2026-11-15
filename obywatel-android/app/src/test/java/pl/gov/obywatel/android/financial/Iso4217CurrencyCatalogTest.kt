// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Iso4217CurrencyCatalogTest {
    @Test fun catalogContainsCurrenciesBeyondNbpCoverage() {
        assertNotNull(Iso4217CurrencyCatalog.find("USD"))
        assertNotNull(Iso4217CurrencyCatalog.find("EUR"))
        assertNotNull(Iso4217CurrencyCatalog.find("JPY"))
        assertTrue(Iso4217CurrencyCatalog.all.size > 100)
        assertEquals("2026-09-17", Iso4217CurrencyCatalog.version)
    }

    @Test fun currencyMetadataUsesVersionedIsoMinorUnitsAndNumericCodes() {
        assertEquals(0, Iso4217CurrencyCatalog.find("JPY")?.minorUnits)
        assertEquals(2, Iso4217CurrencyCatalog.find("EUR")?.minorUnits)
        assertEquals(3, Iso4217CurrencyCatalog.find("KWD")?.minorUnits)
        assertEquals("392", Iso4217CurrencyCatalog.find("JPY")?.numericCode)
        assertEquals("978", Iso4217CurrencyCatalog.find("EUR")?.numericCode)
        assertEquals("985", Iso4217CurrencyCatalog.find("PLN")?.numericCode)
    }

    @Test fun fundsAndNonCurrencyCodesAreNotOfferedAsPaymentCurrencies() {
        assertNull(Iso4217CurrencyCatalog.find("BOV"))
        assertNull(Iso4217CurrencyCatalog.find("XAU"))
        assertNull(Iso4217CurrencyCatalog.find("XXX"))
    }

    @Test fun historicalCodesExposeWithdrawalDateAndDoNotOverrideActiveCurrency() {
        assertEquals("2026-01", Iso4217CurrencyCatalog.findHistorical("BGN")?.withdrawnAt)
        assertNull(Iso4217CurrencyCatalog.find("BGN"))
        assertNotNull(Iso4217CurrencyCatalog.find("EUR"))
        assertNull(Iso4217CurrencyCatalog.findHistorical("EUR"))
        assertTrue(Iso4217CurrencyCatalog.historical.size > 100)
    }

    @Test fun searchMatchesCodePolishNameAndSymbol() {
        assertTrue(Iso4217CurrencyCatalog.search("USD").any { it.code == "USD" })
        assertTrue(Iso4217CurrencyCatalog.search("dolar", limit = 100).any { it.code == "USD" })
        assertFalse(Iso4217CurrencyCatalog.search("EUR").isEmpty())
    }

    @Test fun catalogIsStableAndCodesAreUnique() {
        val codes = Iso4217CurrencyCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
        assertEquals(codes.sorted(), codes)
        val historicalCodes = Iso4217CurrencyCatalog.historical.map { it.code }
        assertEquals(historicalCodes.size, historicalCodes.toSet().size)
    }
}
