// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
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
 * Klient oficjalnego API NBP. Cache jest procesowy i ograniczony czasowo;
 * dane historyczne są cache'owane dłużej, ponieważ publikacje są niezmienne.
 */
class NbpRatesRepository(
    private val transport: NbpHttpTransport = UrlConnectionNbpTransport(),
    private val now: () -> Long = System::currentTimeMillis,
    private val latestTtl: Duration = Duration.ofMinutes(15),
    private val historicalTtl: Duration = Duration.ofDays(30)
) {
    private data class Cached<T>(val value: T, val storedAt: Long)
    private val cache = ConcurrentHashMap<String, Cached<Any>>()

    fun latestTable(table: NbpTable): NbpRateTable =
        cached("latest-table-${table.apiCode}", latestTtl) {
            parseTable(transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}?format=json"))
        }

    fun tableForDate(table: NbpTable, date: LocalDate): NbpRateTable =
        cached("table-${table.apiCode}-$date", historicalTtl) {
            parseTable(transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}/$date?format=json"))
        }

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
        return cached("range-${table.apiCode}-$start-$end", historicalTtl) {
            parseTables(transport.get("$BASE_URL/exchangerates/tables/${table.apiCode}/$start/$end?format=json"))
        }
    }

    fun latestGoldPrice(): NbpGoldPrice =
        cached("latest-gold", latestTtl) {
            parseGold(JSONArray(transport.get("$BASE_URL/cenyzlota?format=json"))).lastOrNull()
                ?: throw IOException("NBP nie zwróciło ceny złota.")
        }

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
        return cached("gold-$start-$end", historicalTtl) {
            parseGold(JSONArray(transport.get("$BASE_URL/cenyzlota/$start/$end?format=json")))
        }
    }

    fun clearCache() = cache.clear()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> cached(key: String, ttl: Duration, load: () -> T): T {
        val current = cache[key] as? Cached<T>
        if (current != null && now() - current.storedAt < ttl.toMillis()) return current.value
        val loaded = load()
        cache[key] = Cached(loaded, now())
        return loaded
    }

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
