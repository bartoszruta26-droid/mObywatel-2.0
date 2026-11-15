// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject

enum class NbpTable(val apiCode: String) { A("A"), B("B"), C("C") }

data class NbpRate(
    val code: String,
    val currency: String,
    val mid: BigDecimal?,
    val bid: BigDecimal?,
    val ask: BigDecimal?
) {
    fun toExchangeRate(): ExchangeRate {
        require(mid != null) { "Tabela NBP nie udostępnia kursu średniego dla tej pozycji." }
        return ExchangeRate(code, "PLN", mid)
    }
}

data class NbpRateTable(
    val table: NbpTable,
    val number: String,
    val effectiveDate: LocalDate,
    val rates: List<NbpRate>
) {
    fun rate(code: String): NbpRate? = rates.firstOrNull { it.code == code }
}

data class NbpGoldPrice(val date: LocalDate, val pricePerGramPln: BigDecimal)

interface NbpHttpTransport {
    @Throws(IOException::class)
    fun get(url: String): String
}

class UrlConnectionNbpTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : NbpHttpTransport {
    override fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "mObywatelska-2.0")
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IOException("NBP API zwróciło HTTP $status.")
            return body
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Klient API NBP z cache'em na dysku aplikacji.
 * Dane historyczne są niezmienne i pozostają zapisane bezterminowo; klucze bieżące
 * są odświeżane po TTL, bez nadpisywania archiwalnych notowań.
 */
class NbpRatesRepository(
    private val transport: NbpHttpTransport = UrlConnectionNbpTransport(),
    private val now: () -> Long = System::currentTimeMillis,
    private val latestTtl: Duration = Duration.ofMinutes(15),
    private val historicalTtl: Duration = Duration.ofDays(30),
    private val cacheDirectory: File? = null
) {
    private data class CachedResponse(val body: String, val storedAt: Long)
    private val cache = ConcurrentHashMap<String, CachedResponse>()

    fun latestTable(table: NbpTable): NbpRateTable =
        parseTable(cachedResponse("latest-table-${table.apiCode}", latestTtl, historical = false) {
            transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}?format=json")
        })

    fun tableForDate(table: NbpTable, date: LocalDate): NbpRateTable =
        parseTable(cachedResponse("table-${table.apiCode}-$date", historicalTtl, historical = true) {
            transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}/$date?format=json")
        })

    /** Zwraca ostatnią opublikowaną tabelę nie późniejszą niż wskazany dzień. */
    fun tableOnOrBefore(table: NbpTable, requestedDate: LocalDate, lookbackDays: Long = 7): NbpRateTable {
        require(lookbackDays in 1..92) { "Zakres wyszukiwania musi wynosić od 1 do 92 dni." }
        return tableRange(table, requestedDate.minusDays(lookbackDays), requestedDate).lastOrNull()
            ?: throw IOException("NBP nie opublikował tabeli w podanym przedziale dat.")
    }

    fun tableRange(table: NbpTable, start: LocalDate, end: LocalDate): List<NbpRateTable> {
        require(!end.isBefore(start)) { "Data końcowa musi przypadać po dacie początkowej." }
        require(ChronoUnit.DAYS.between(start, end) + 1 <= 93) {
            "Jedno zapytanie NBP może obejmować maksymalnie 93 dni."
        }
        val key = "range-${table.apiCode}-$start-$end"
        return parseTables(cachedResponse(key, historicalTtl, historical = true) {
            transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}/$start/$end?format=json")
        })
    }

    /**
     * Historyczny szereg kursu waluty względem PLN. Zakresy są dzielone na
     * odcinki po maksymalnie 93 dni zgodnie z limitem API NBP.
     */
    fun currencyRateHistory(
        table: NbpTable,
        currencyCode: String,
        start: LocalDate,
        end: LocalDate
    ): List<Pair<LocalDate, BigDecimal>> {
        require(currencyCode.matches(Regex("[A-Z]{3}"))) { "Kod waluty musi mieć trzy litery ISO." }
        require(!end.isBefore(start)) { "Data końcowa musi przypadać po dacie początkowej." }
        val result = mutableListOf<Pair<LocalDate, BigDecimal>>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            val chunkEnd = minOf(cursor.plusDays(92), end)
            tableRange(table, cursor, chunkEnd).forEach { rateTable ->
                rateTable.rate(currencyCode)?.mid?.let { result += rateTable.effectiveDate to it }
            }
            cursor = chunkEnd.plusDays(1)
        }
        return result.distinctBy { it.first }.sortedBy { it.first }
    }

    /** Historyczny szereg cen złota w PLN/g z automatycznym dzieleniem zakresu. */
    fun goldPriceHistory(start: LocalDate, end: LocalDate): List<NbpGoldPrice> {
        require(!end.isBefore(start)) { "Data końcowa musi przypadać po dacie początkowej." }
        val result = mutableListOf<NbpGoldPrice>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            val chunkEnd = minOf(cursor.plusDays(92), end)
            result += goldPriceRange(cursor, chunkEnd)
            cursor = chunkEnd.plusDays(1)
        }
        return result.distinctBy { it.date }.sortedBy { it.date }
    }

    fun latestGoldPrice(): NbpGoldPrice =
        parseGold(JSONArray(cachedResponse("latest-gold", latestTtl, historical = false) {
            transport.get("$BASE_URL/cenyzlota?format=json")
        })).lastOrNull() ?: throw IOException("NBP nie zwróciło ceny złota.")

    /** Cena z ostatniego dnia publikacji, gdy wskazany dzień przypada np. w weekend. */
    fun goldPriceOnOrBefore(requestedDate: LocalDate, lookbackDays: Long = 7): NbpGoldPrice {
        require(lookbackDays in 1..92) { "Zakres wyszukiwania musi wynosić od 1 do 92 dni." }
        return goldPriceRange(requestedDate.minusDays(lookbackDays), requestedDate).lastOrNull()
            ?: throw IOException("NBP nie opublikował ceny złota w podanym przedziale dat.")
    }

    fun goldPriceRange(start: LocalDate, end: LocalDate): List<NbpGoldPrice> {
        require(!end.isBefore(start)) { "Data końcowa musi przypadać po dacie początkowej." }
        require(ChronoUnit.DAYS.between(start, end) + 1 <= 93) {
            "Jedno zapytanie NBP może obejmować maksymalnie 93 dni."
        }
        val key = "gold-$start-$end"
        return parseGold(JSONArray(cachedResponse(key, historicalTtl, historical = true) {
            transport.get("$BASE_URL/cenyzlota/$start/$end?format=json")
        }))
    }

    fun clearCache() {
        cache.clear()
        cacheDirectory?.listFiles()?.filter { it.isFile && it.extension == "json" }?.forEach(File::delete)
    }

    private fun cachedResponse(
        key: String,
        ttl: Duration,
        historical: Boolean,
        load: () -> String
    ): String {
        val memory = cache[key]
        if (memory != null && (historical || now() - memory.storedAt < ttl.toMillis())) {
            return memory.body
        }

        val file = cacheFile(key)
        val disk = readDisk(file)
        if (disk != null && (historical || now() - disk.storedAt < ttl.toMillis())) {
            cache[key] = disk
            return disk.body
        }

        val body = load()
        require(body.isNotBlank()) { "NBP zwróciło pustą odpowiedź." }
        val saved = CachedResponse(body, now())
        cache[key] = saved
        writeDisk(file, saved)
        return body
    }

    private fun cacheFile(key: String): File? {
        val directory = cacheDirectory ?: return null
        return File(directory, "${sha256(key)}.json")
    }

    private fun readDisk(file: File?): CachedResponse? {
        if (file == null || !file.isFile) return null
        return runCatching {
            val content = file.readText(Charsets.UTF_8)
            val separator = content.indexOf('\n')
            require(separator > 0)
            CachedResponse(content.substring(separator + 1), content.substring(0, separator).toLong())
        }.getOrNull()
    }

    private fun writeDisk(file: File?, response: CachedResponse) {
        if (file == null) return
        runCatching {
            file.parentFile?.mkdirs()
            val temporary = File(file.parentFile, file.name + ".tmp")
            temporary.writeText("${response.storedAt}\n${response.body}", Charsets.UTF_8)
            if (!temporary.renameTo(file)) {
                file.writeText("${response.storedAt}\n${response.body}", Charsets.UTF_8)
                temporary.delete()
            }
        }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val BASE_URL = "https://api.nbp.pl/api"

        fun parseTable(json: String): NbpRateTable {
            val array = JSONArray(json)
            require(array.length() > 0) { "NBP zwróciło pustą tabelę." }
            val obj = array.getJSONObject(0)
            val table = NbpTable.valueOf(obj.getString("table"))
            val rates = obj.getJSONArray("rates")
            return NbpRateTable(
                table = table,
                number = obj.getString("no"),
                effectiveDate = LocalDate.parse(obj.getString("effectiveDate")),
                rates = (0 until rates.length()).map { index ->
                    val item = rates.getJSONObject(index)
                    NbpRate(
                        code = item.getString("code"),
                        currency = item.getString("currency"),
                        mid = item.optString("mid").takeIf { it.isNotBlank() && it != "null" }?.let(::BigDecimal),
                        bid = item.optString("bid").takeIf { it.isNotBlank() && it != "null" }?.let(::BigDecimal),
                        ask = item.optString("ask").takeIf { it.isNotBlank() && it != "null" }?.let(::BigDecimal)
                    )
                }
            )
        }

        fun parseTables(json: String): List<NbpRateTable> {
            val array = JSONArray(json)
            return (0 until array.length()).map { index ->
                parseTable(JSONArray().put(array.getJSONObject(index)).toString())
            }
        }

        fun parseGold(json: JSONArray): List<NbpGoldPrice> =
            (0 until json.length()).map { index ->
                val item: JSONObject = json.getJSONObject(index)
                NbpGoldPrice(
                    date = LocalDate.parse(item.getString("data")),
                    pricePerGramPln = BigDecimal(item.get("cena").toString()).also {
                        require(it > BigDecimal.ZERO) { "Cena złota musi być większa od zera." }
                    }
                )
            }
    }
}
