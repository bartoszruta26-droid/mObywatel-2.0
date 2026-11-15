// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Liczba miejsc dziesiętnych zgodna z metadanymi katalogu ISO 4217. */
object CurrencyPrecision {
    fun scale(code: String): Int = Iso4217CurrencyCatalog.find(code)?.minorUnits ?: 2
}

/** Przelicza dowolną parę przez PLN na podstawie kursów średnich NBP (PLN za 1 jednostkę). */
object NbpCurrencyConverter {
    fun convert(
        amount: BigDecimal,
        fromCode: String,
        toCode: String,
        plnPerCurrency: Map<String, BigDecimal>
    ): BigDecimal {
        require(amount >= BigDecimal.ZERO) { "Kwota nie może być ujemna." }
        val source = fromCode.trim().uppercase()
        val target = toCode.trim().uppercase()
        require(source.matches(Regex("[A-Z]{3}")) && target.matches(Regex("[A-Z]{3}"))) {
            "Wprowadź trzyliterowe kody walut ISO 4217."
        }
        require(source != target) { "Wybierz dwie różne waluty." }
        val sourcePln = if (source == "PLN") BigDecimal.ONE else
            plnPerCurrency[source] ?: throw IllegalArgumentException("Brak kursu NBP dla waluty $source.")
        val targetPln = if (target == "PLN") BigDecimal.ONE else
            plnPerCurrency[target] ?: throw IllegalArgumentException("Brak kursu NBP dla waluty $target.")
        require(sourcePln > BigDecimal.ZERO && targetPln > BigDecimal.ZERO) {
            "Kursy NBP muszą być większe od zera."
        }
        return amount.multiply(sourcePln)
            .divide(targetPln, MathContext(34, RoundingMode.HALF_EVEN))
            .setScale(CurrencyPrecision.scale(target), RoundingMode.HALF_EVEN)
    }
}

@Composable
fun NbpCurrencyConverterCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) {
        NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache"))
    }
    val scope = rememberCoroutineScope()
    var amountText by remember { mutableStateOf("100") }
    var fromCode by remember { mutableStateOf("EUR") }
    var toCode by remember { mutableStateOf("PLN") }
    var rates by remember { mutableStateOf<Map<String, BigDecimal>>(emptyMap()) }
    var rateTableLabel by remember { mutableStateOf("") }
    var rateDateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var goldPriceLabel by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<String?>(null) }

    fun loadRates(dateText: String? = null) {
        scope.launch {
            loading = true
            error = null
            try {
                val loaded = withContext(Dispatchers.IO) {
                    val requestedDate = dateText?.let { LocalDate.parse(it) }
                    val tableA = if (requestedDate == null) repository.latestTable(NbpTable.A)
                        else repository.tableOnOrBefore(NbpTable.A, requestedDate)
                    val tableB = runCatching {
                        if (requestedDate == null) repository.latestTable(NbpTable.B)
                        else repository.tableOnOrBefore(NbpTable.B, requestedDate)
                    }.getOrNull()
                    val gold = runCatching {
                        if (requestedDate == null) repository.latestGoldPrice()
                        else repository.goldPriceOnOrBefore(requestedDate)
                    }.getOrNull()
                    val allTables = listOfNotNull(tableA, tableB)
                    val rateMap = allTables.flatMap { table ->
                        table.rates.mapNotNull { rate -> rate.mid?.let { rate.code to it } }
                    }.toMap()
                    Triple(rateMap, allTables.joinToString(" • ") {
                        "Tabela ${it.table.apiCode}: ${it.effectiveDate}"
                    }, gold?.let { "${it.pricePerGramPln.toPlainString()} PLN/g • ${it.date}" })
                }
                rates = loaded.first
                rateTableLabel = loaded.second
                goldPriceLabel = loaded.third
                if (rates.isEmpty()) error = "NBP nie zwrócił dostępnych kursów średnich."
                result = null
            } catch (exception: Exception) {
                rates = emptyMap()
                goldPriceLabel = null
                result = null
                error = exception.message ?: "Nie udało się pobrać kursów NBP."
            } finally {
                loading = false
            }
        }
    }

    fun calculate() {
        try {
            val amount = BigDecimal(amountText.trim().replace(',', '.'))
            val source = fromCode.trim().uppercase()
            val target = toCode.trim().uppercase()
            require(rates.isNotEmpty()) { "Najpierw pobierz aktualne kursy NBP." }
            val converted = NbpCurrencyConverter.convert(amount, source, target, rates)
            result = "${amount.toPlainString()} $source = ${converted.toPlainString()} $target"
            error = null
        } catch (exception: Exception) {
            result = null
            error = exception.message ?: "Nie udało się wykonać przeliczenia."
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Przelicznik walut NBP", style = MaterialTheme.typography.titleMedium)
        Text("Kursy średnie z tabel A i B Narodowego Banku Polskiego.")
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it; result = null },
            label = { Text("Kwota") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fromCode,
                onValueChange = { fromCode = it.uppercase(); result = null },
                label = { Text("Z waluty (ISO)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = toCode,
                onValueChange = { toCode = it.uppercase(); result = null },
                label = { Text("Na walutę (ISO)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Text("Katalog ISO 4217 — wpisz kod, nazwę lub symbol waluty.")
        Text("Podpowiedzi waluty źródłowej")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(Iso4217CurrencyCatalog.search(fromCode), key = { it.code }) { currency ->
                Button(onClick = { fromCode = currency.code; result = null }) {
                    Text("${currency.code} · ${currency.name}")
                }
            }
        }
        Text("Podpowiedzi waluty docelowej")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(Iso4217CurrencyCatalog.search(toCode), key = { it.code }) { currency ->
                Button(onClick = { toCode = currency.code; result = null }) {
                    Text("${currency.code} · ${currency.name}")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val previous = fromCode
                fromCode = toCode
                toCode = previous
                result = null
            }) { Text("Zamień waluty") }
            Button(onClick = { loadRates() }, enabled = !loading) {
                Text(if (rates.isEmpty()) "Pobierz aktualne" else "Odśwież")
            }
            Button(onClick = { loadRates(rateDateText) }, enabled = !loading) {
                Text("Pobierz dla daty")
            }
            Button(onClick = ::calculate, enabled = rates.isNotEmpty()) { Text("Przelicz") }
        }
        OutlinedTextField(
            value = rateDateText,
            onValueChange = { rateDateText = it },
            label = { Text("Data kursu (RRRR-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (loading) CircularProgressIndicator()
        if (rates.isNotEmpty()) {
            Text("Źródło: NBP • $rateTableLabel")
            goldPriceLabel?.let { Text("Cena złota: $it") }
            Text("Dostępność kursu zależy od tabel NBP A/B, a nie od pełnego katalogu ISO 4217.")
        }
        result?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text(
            "Wycena referencyjna według kursu średniego NBP. Nie jest to oferta ani kurs wykonania transakcji bankowej.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
