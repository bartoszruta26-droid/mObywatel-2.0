// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NbpRatesRepositoryTest {
    @Test
    fun parsesTableAAndPreservesEffectiveDateAndMidRate() {
        val json = """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
        val table = NbpRatesRepository.parseTable(json)
        assertEquals(NbpTable.A, table.table)
        assertEquals("220/A/NBP/2026", table.number)
        assertEquals(LocalDate.parse("2026-11-13"), table.effectiveDate)
        assertEquals("4.31", table.rate("EUR")?.mid.toString())
        assertEquals(ExchangeRate("EUR", "PLN", java.math.BigDecimal("4.31")), table.rate("EUR")?.toExchangeRate())
    }

    @Test
    fun parsesTableCWithBidAndAskWithoutInventingMidRate() {
        val json = """[{"table":"C","no":"220/C/NBP/2026","tradingDate":"2026-11-12","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","bid":4.29,"ask":4.37}]}]"""
        val rate = NbpRatesRepository.parseTable(json).rate("EUR")
        assertEquals("4.29", rate?.bid.toString())
        assertEquals("4.37", rate?.ask.toString())
        assertEquals(null, rate?.mid)
        assertThrows(IllegalArgumentException::class.java) { rate?.toExchangeRate() }
    }

    @Test
    fun parsesGoldPriceAsDecimalAndDate() {
        val prices = NbpRatesRepository.parseGold(
            org.json.JSONArray("""[{"data":"2026-11-13","cena":520.25}]""")
        )
        assertEquals(1, prices.size)
        assertEquals(LocalDate.parse("2026-11-13"), prices.single().date)
        assertEquals("520.25", prices.single().pricePerGramPln.toString())
    }

    @Test
    fun cachesLatestResponseAndCanExplicitlyClearCache() {
        var calls = 0
        val transport = object : NbpHttpTransport {
            override fun get(url: String): String {
                calls++
                return """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
            }
        }
        val repository = NbpRatesRepository(transport)
        repository.latestTable(NbpTable.A)
        repository.latestTable(NbpTable.A)
        assertEquals(1, calls)
        repository.clearCache()
        repository.latestTable(NbpTable.A)
        assertEquals(2, calls)
    }

    @Test
    fun rejectsReversedOrTooLongDateRangesBeforeNetworkRequest() {
        var calls = 0
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String {
                calls++
                return "[]"
            }
        })
        assertThrows(IllegalArgumentException::class.java) {
            repository.tableRange(NbpTable.A, LocalDate.parse("2026-11-15"), LocalDate.parse("2026-11-14"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            repository.goldPriceRange(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-10"))
        }
        assertEquals(0, calls)
    }

    @Test
    fun resolvesLastPublishedTableForWeekendDate() {
        var requestedUrl = ""
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String {
                requestedUrl = url
                return """[
                    {"table":"A","no":"219/A/NBP/2026","effectiveDate":"2026-11-12","rates":[{"currency":"euro","code":"EUR","mid":4.30}]},
                    {"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}
                ]"""
            }
        })
        val table = repository.tableOnOrBefore(NbpTable.A, LocalDate.parse("2026-11-15"))
        assertEquals(LocalDate.parse("2026-11-13"), table.effectiveDate)
        assertEquals(true, requestedUrl.contains("/2026-11-08/2026-11-15"))
    }

    @Test
    fun resolvesLastPublishedGoldPriceForWeekendDate() {
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String =
                """[
                    {"data":"2026-11-12","cena":519.10},
                    {"data":"2026-11-13","cena":520.25}
                ]"""
        })
        val price = repository.goldPriceOnOrBefore(LocalDate.parse("2026-11-15"))
        assertEquals(LocalDate.parse("2026-11-13"), price.date)
        assertEquals("520.25", price.pricePerGramPln.toString())
    }
}
