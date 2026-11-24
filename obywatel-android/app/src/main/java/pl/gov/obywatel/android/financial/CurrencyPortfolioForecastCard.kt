// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PortfolioCurrency(val code: String, val name: String)
private data class CurrencyHorizonRow(
    val currency: String,
    val forecast: HorizonCurrencyForecast,
    val balance: Double?,
    val currentBankBid: Double?,
    val currentBankAsk: Double?
)

@Composable
fun CurrencyPortfolioForecastCard(
    settings: RegressionChannelSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("currency_portfolio_forecast", 0) }
    val scope = rememberCoroutineScope()
    val currencies = remember {
        listOf(
            PortfolioCurrency("USD", "dolar amerykański"),
            PortfolioCurrency("EUR", "euro"),
            PortfolioCurrency("CHF", "frank szwajcarski"),
            PortfolioCurrency("CZK", "korona czeska")
        )
    }
    val fields = remember {
        mutableStateMapOf<String, String>().apply {
            put("PLN", FinancialDataCipher.read(context, prefs, "balance_PLN", ""))
            currencies.forEach { currency ->
                put("balance_${currency.code}", prefs.getString("balance_${currency.code}", "") ?: "")
                put("bid_${currency.code}", prefs.getString("bid_${currency.code}", "") ?: "")
                put("ask_${currency.code}", prefs.getString("ask_${currency.code}", "") ?: "")
            }
        }
    }
    var rows by remember { mutableStateOf<List<CurrencyHorizonRow>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dataDate by remember { mutableStateOf<String?>(null) }
    val repository = remember(context) {
        NbpRatesRepository(cacheDirectory = File(context.filesDir, "nbp-rates-cache"))
    }

    fun saveFields() {
        runCatching {
            val editor = prefs.edit()
            fields.forEach { (key, value) ->
                editor.putString("secure_" + key, FinancialDataCipher.encrypt(context, key, value))
                editor.remove(key)
            }
            editor.apply()
        }.onFailure {
            error = "Nie udało się bezpiecznie zapisać sald i kursów. Sprawdź blokadę urządzenia i spróbuj ponownie."
        }
    }

    fun decimal(key: String): Double? = fields[key]?.trim()
        ?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }

    fun calculate() {
        scope.launch {
            loading = true
            error = null
            rows = emptyList()
            try {
                val end = LocalDate.now()
                val start = end.minusYears(5)
                val calculated = mutableListOf<CurrencyHorizonRow>()
                for (currency in currencies) {
                    val history = withContext(Dispatchers.IO) {
                        repository.currencyRateHistory(NbpTable.A, currency.code, start, end)
                    }
                    val requiredObservations = settings.lengths.minOrNull() ?: 144
                    require(history.size >= requiredObservations) {
                        "Za mało danych NBP dla ${currency.code}: ${history.size} obserwacji; wymagane jest okno $requiredObservations."
                    }
                    val forecasts = withContext(Dispatchers.Default) {
                        CurrencyForecastEngine.forecast(
                            history.map { it.second.toDouble() }, settings
                        )
                    }
                    val balance = decimal("balance_${currency.code}")
                    val bid = decimal("bid_${currency.code}")?.takeIf { it > 0.0 }
                    val ask = decimal("ask_${currency.code}")?.takeIf { it > 0.0 }
                    forecasts.forEach { forecast ->
                        calculated += CurrencyHorizonRow(currency.code, forecast, balance, bid, ask)
                    }
                    dataDate = history.lastOrNull()?.first?.toString()
                }
                rows = calculated
                saveFields()
            } catch (e: Exception) {
                error = e.message ?: "Nie udało się obliczyć prognozy walutowej."
            } finally {
                loading = false
            }
        }
    }

    Card(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Prognoza portfela walutowego", style = MaterialTheme.typography.titleMedium)
            Text(
                "Prognoza USD, EUR, CHF i CZK względem PLN. Wpisz salda oraz aktualne kursy iPKO. " +
                    "Saldo PLN jest budżetem do przyszłych zakupów walut.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = fields["PLN"].orEmpty(),
                onValueChange = { fields["PLN"] = it; saveFields() },
                label = { Text("Saldo PLN • budżet wymiany") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            currencies.forEach { currency ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("${currency.code} • ${currency.name}", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = fields["balance_${currency.code}"].orEmpty(),
                            onValueChange = { fields["balance_${currency.code}"] = it; saveFields() },
                            label = { Text("Saldo ${currency.code}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = fields["bid_${currency.code}"].orEmpty(),
                                onValueChange = { fields["bid_${currency.code}"] = it; saveFields() },
                                label = { Text("iPKO kupuje") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fields["ask_${currency.code}"].orEmpty(),
                                onValueChange = { fields["ask_${currency.code}"] = it; saveFields() },
                                label = { Text("iPKO sprzedaje") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }
            Button(
                onClick = ::calculate,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Obliczanie…" else "Oblicz prognozę 5 walut • 8 horyzontów") }
            if (loading) CircularProgressIndicator()
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (rows.isNotEmpty()) {
                Text("Dane historyczne NBP do: ${dataDate ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Wynik portfela porównuje wartość sprzedaży waluty dziś z hipotetyczną wartością w przyszłości. " +
                        "Kurs iPKO jest wprowadzany ręcznie; jeśli go nie podasz, wynik kwotowy użyje kursu referencyjnego NBP.",
                    style = MaterialTheme.typography.bodySmall
                )
                currencies.forEach { currency ->
                    val currencyRows = rows.filter { it.currency == currency.code }
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("${currency.code}/PLN", style = MaterialTheme.typography.titleSmall)
                            currencyRows.forEach { item ->
                                val f = item.forecast
                                val change = f.expectedChangePercent
                                val sign = if (change > 0) "+" else ""
                                val magnitude = when (f.magnitude) {
                                    ForecastMagnitude.VERY_LARGE -> "bardzo duży"
                                    ForecastMagnitude.LARGE -> "duży"
                                    ForecastMagnitude.MODERATE -> "umiarkowany"
                                    ForecastMagnitude.LIGHT -> "lekki"
                                    ForecastMagnitude.NEUTRAL -> "neutralny"
                                }
                                val movement = when (f.direction) {
                                    ForecastDirection.UP -> "wzrost"
                                    ForecastDirection.DOWN -> "spadek"
                                    ForecastDirection.NEUTRAL -> "ruch neutralny"
                                }
                                val currentSell = item.currentBankBid ?: f.currentRate
                                val futureSell = if (f.currentRate > 0.0 && currentSell > 0.0)
                                    f.projectedRate * (currentSell / f.currentRate) else f.projectedRate
                                val pnl = item.balance?.let { it * (futureSell - currentSell) }
                                val currentAsk = item.currentBankAsk ?: f.currentRate
                                val futureAsk = if (f.currentRate > 0.0 && currentAsk > 0.0)
                                    f.projectedRate * (currentAsk / f.currentRate) else f.projectedRate
                                val plnBudget = decimal("PLN")
                                val currentBuyingPower = plnBudget?.takeIf { currentAsk > 0.0 }?.div(currentAsk)
                                val futureBuyingPower = plnBudget?.takeIf { futureAsk > 0.0 }?.div(futureAsk)
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        "${f.horizon.label}: ${magnitude} ${movement} • ${sign}${"%.2f".format(change)}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Kurs modelowy: ${"%.4f".format(f.projectedRate)} PLN",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        pnl?.let {
                                            "Zmiana wartości salda: ${if (it > 0) "+" else ""}${"%.2f".format(it)} PLN"
                                        } ?: "Wpisz saldo, aby wyliczyć zmianę wartości portfela.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        if (currentBuyingPower != null && futureBuyingPower != null)
                                            "Siła nabywcza budżetu PLN: ${"%.2f".format(currentBuyingPower)} ${currency.code} dziś → ${"%.2f".format(futureBuyingPower)} ${currency.code} w prognozie"
                                        else "Wpisz saldo PLN oraz kurs sprzedaży iPKO, aby porównać siłę nabywczą.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        f.directionalProbability?.let {
                                            "Historyczne prawdopodobieństwo zysku netto (>2%): ${"%.0f".format(it * 100)}% " +
                                                "(${f.validationSamples} prób)"
                                        } ?: "Brak wiarygodnej próby dla tego horyzontu (${f.validationSamples} prób).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        f.categoryProbability?.let {
                                            "Prawdopodobieństwo historyczne tej klasy (${magnitude} ${movement}): " +
                                                "${"%.0f".format(it * 100)}% (${f.categoryValidationSamples} prób)"
                                        } ?: "Brak co najmniej 20 prób dla klasy ${magnitude} ${movement} " +
                                            "(${f.categoryValidationSamples} prób).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        "Zgodność kanałów: ${"%.0f".format(f.channelAgreement * 100)}% • aktywne: ${f.activeChannels}/10",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Text(
                "Zyskowna wymiana oznacza ruch zgodny z prognozą powyżej 2%; mniejszy ruch zgodny z prognozą jest neutralny, " +
                    "a ruch przeciwny oznacza stratę. Próg uwzględnia koszt wymiany walut. Prawdopodobieństwo pochodzi z kroczącej " +
                    "walidacji historycznej (minimum 20 prób). Projekcja nie stanowi gwarancji wyniku.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
