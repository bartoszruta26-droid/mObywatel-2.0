// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.nio.file.Files
import java.time.Duration
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun historicalRatesPersistAcrossRepositoryInstancesAndWorkOffline() {
        val directory = Files.createTempDirectory("nbp-history").toFile()
        var networkCalls = 0
        val json = """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
        val first = NbpRatesRepository(
            transport = object : NbpHttpTransport {
                override fun get(url: String): String { networkCalls++; return json }
            },
            cacheDirectory = directory
        )
        val expected = first.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13"))
        val second = NbpRatesRepository(
            transport = object : NbpHttpTransport {
                override fun get(url: String): String = throw java.io.IOException("offline")
            },
            cacheDirectory = directory
        )

        val restored = second.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13"))
        assertEquals(expected, restored)
        assertEquals(1, networkCalls)
        assertEquals(1, directory.listFiles()?.count { it.extension == "json" })
        directory.deleteRecursively()
    }

    @Test
    fun corruptedDiskEnvelopeIsIgnoredAndReplacedFromNetwork() {
        val directory = Files.createTempDirectory("nbp-corrupt").toFile()
        val key = "table-A-2026-11-13"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        java.io.File(directory, "$digest.json").writeText("not-a-valid-cache-envelope")
        var calls = 0
        val validJson = """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
        val repository = NbpRatesRepository(
            transport = object : NbpHttpTransport {
                override fun get(url: String): String { calls++; return validJson }
            },
            cacheDirectory = directory
        )

        val table = repository.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13"))
        assertEquals("4.31", table.rate("EUR")?.mid.toString())
        assertEquals(1, calls)
        assertTrue(java.io.File(directory, "$digest.json").readText().contains(validJson))
        directory.deleteRecursively()
    }

    @Test
    fun interruptedNetworkRequestDoesNotPoisonCacheAndCanBeRetried() {
        val directory = Files.createTempDirectory("nbp-interrupted").toFile()
        var calls = 0
        val json = """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
        val repository = NbpRatesRepository(
            transport = object : NbpHttpTransport {
                override fun get(url: String): String {
                    calls++
                    if (calls == 1) throw java.io.IOException("connection interrupted")
                    return json
                }
            },
            cacheDirectory = directory
        )
        assertThrows(java.io.IOException::class.java) {
            repository.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13"))
        }
        assertEquals("4.31", repository.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13")).rate("EUR")?.mid.toString())
        assertEquals(2, calls)
        directory.deleteRecursively()
    }

    @Test
    fun cacheWriteFailureDoesNotBreakSuccessfulNetworkResponse() {
        val parent = Files.createTempFile("nbp-cache-parent", ".tmp").toFile()
        var calls = 0
        val repository = NbpRatesRepository(
            transport = object : NbpHttpTransport {
                override fun get(url: String): String {
                    calls++
                    return """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
                }
            },
            cacheDirectory = parent
        )
        try {
            assertEquals("4.31", repository.tableForDate(NbpTable.A, LocalDate.parse("2026-11-13")).rate("EUR")?.mid.toString())
            assertEquals(1, calls)
        } finally {
            parent.delete()
        }
    }

    @Test
    fun latestRatesRefreshAfterTtlWhileHistoricalArchiveRemainsSeparate() {
        val directory = Files.createTempDirectory("nbp-current").toFile()
        var clock = 1_000_000L
        var currentCalls = 0
        val firstJson = """[{"table":"A","no":"220/A/NBP/2026","effectiveDate":"2026-11-13","rates":[{"currency":"euro","code":"EUR","mid":4.31}]}]"""
        val updatedJson = """[{"table":"A","no":"221/A/NBP/2026","effectiveDate":"2026-11-16","rates":[{"currency":"euro","code":"EUR","mid":4.32}]}]"""
        val transport = object : NbpHttpTransport {
            override fun get(url: String): String {
                currentCalls++
                return if (currentCalls == 1) firstJson else updatedJson
            }
        }
        val repository = NbpRatesRepository(
            transport = transport,
            now = { clock },
            latestTtl = Duration.ofMinutes(15),
            cacheDirectory = directory
        )
        assertEquals("4.31", repository.latestTable(NbpTable.A).rate("EUR")?.mid.toString())
        assertEquals("4.31", repository.latestTable(NbpTable.A).rate("EUR")?.mid.toString())
        assertEquals(1, currentCalls)

        clock += Duration.ofMinutes(16).toMillis()
        assertEquals("4.32", repository.latestTable(NbpTable.A).rate("EUR")?.mid.toString())
        assertEquals(2, currentCalls)
        directory.deleteRecursively()
    }


    @Test
    fun currencyRateHistorySplitsLongRangesIntoNbpCompatibleRequests() {
        val requested = mutableListOf<String>()
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String {
                requested += url
                return """[{"table":"A","no":"1/A/NBP/2026","effectiveDate":"2026-01-02","rates":[{"currency":"euro","code":"EUR","mid":4.25}]}]"""
            }
        })
        val history = repository.currencyRateHistory(
            NbpTable.A, "EUR", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-04")
        )
        assertEquals(2, requested.size)
        assertTrue(requested[0].contains("/2026-01-01/2026-04-03"))
        assertTrue(requested[1].contains("/2026-04-04/2026-04-04"))
        assertEquals(1, history.size)
        assertEquals("4.25", history.single().second.toPlainString())
    }

    @Test
    fun currencyBidAskHistoryReadsTableCAndSplitsLongRanges() {
        val requested = mutableListOf<String>()
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String {
                requested += url
                return """[{"table":"C","no":"1/C/NBP/2026","tradingDate":"2026-01-01","effectiveDate":"2026-01-02","rates":[{"currency":"euro","code":"EUR","bid":4.20,"ask":4.30}]}]"""
            }
        })
        val history = repository.currencyBidAskHistory(
            "EUR", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-04")
        )
        assertEquals(2, requested.size)
        assertTrue(requested.all { it.contains("/exchangerates/tables/C/") })
        assertEquals(1, history.size)
        assertEquals("4.20", history.single().bidPln.toPlainString())
        assertEquals("4.30", history.single().askPln.toPlainString())
    }

    @Test
    fun goldPriceHistorySplitsLongRangesAndSortsPublishedPrices() {
        val requested = mutableListOf<String>()
        val repository = NbpRatesRepository(object : NbpHttpTransport {
            override fun get(url: String): String {
                requested += url
                return """[{"data":"2026-01-02","cena":510.25},{"data":"2026-01-05","cena":512.50}]"""
            }
        })
        val history = repository.goldPriceHistory(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-04")
        )
        assertEquals(2, requested.size)
        assertTrue(requested[0].contains("/2026-01-01/2026-04-03"))
        assertTrue(requested[1].contains("/2026-04-04/2026-04-04"))
        assertEquals(2, history.size)
        assertEquals(LocalDate.parse("2026-01-02"), history.first().date)
        assertEquals("512.50", history.last().pricePerGramPln.toPlainString())
    }

}
