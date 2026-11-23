// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SelectedPairForecastCard(
    base: String,
    quote: String,
    values: List<Double>,
    settings: RegressionChannelSettings,
    modifier: Modifier = Modifier
) {
    var forecasts by remember { mutableStateOf<List<PairHorizonRisk>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(base, quote, values, settings) {
        if (values.size < 2) {
            forecasts = emptyList()
            error = "Brak danych do walidacji wybranej pary."
        } else {
            loading = true
            error = null
            try {
                forecasts = withContext(Dispatchers.Default) {
                    PairRiskForecastEngine.forecast(
                        values,
                        settings,
                        listOf(
                            ForecastHorizon.MONTH,
                            ForecastHorizon.TWO_MONTHS,
                            ForecastHorizon.QUARTER,
                            ForecastHorizon.HALF_YEAR,
                            ForecastHorizon.YEAR
                        )
                    )
                }
            } catch (exception: Exception) {
                forecasts = emptyList()
                error = exception.message ?: "Nie udało się obliczyć ryzyka wybranej pary."
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
            Text("Ryzyko wybranej pary: $base/$quote", style = MaterialTheme.typography.titleMedium)
            Text(
                "Prawdopodobieństwa dotyczą wyłącznie pary widocznej na wykresie. Zysk oznacza ruch zgodny z sygnałem modelu " +
                    "(pozycja długa przy prognozie wzrostu lub krótka przy prognozie spadku); strata oznacza ruch przeciwny. " +
                    "Nie uwzględniają spreadu, prowizji ani podatków.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Walidacja wyłącznie dla tej pary: ${values.size} obserwacji historycznych. " +
                    "Wykres może pokazywać krótszy zakres; szanse liczone są z dłuższej historii tej samej pary.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            if (loading) CircularProgressIndicator()
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            forecasts.forEach { item ->
                val profitColor = when (item.significance) {
                    PairRiskSignificance.PROFIT_ADVANTAGE -> Color(0xFF168447)
                    PairRiskSignificance.LOSS_ADVANTAGE -> Color(0xFFB3261E)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val lossColor = when (item.significance) {
                    PairRiskSignificance.PROFIT_ADVANTAGE -> Color(0xFFB56A20)
                    PairRiskSignificance.LOSS_ADVANTAGE -> Color(0xFFB3261E)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val status = when (item.significance) {
                    PairRiskSignificance.PROFIT_ADVANTAGE -> "Istotna statystycznie przewaga zysku"
                    PairRiskSignificance.LOSS_ADVANTAGE -> "Ostrzeżenie: istotna przewaga straty"
                    PairRiskSignificance.INCONCLUSIVE -> "Brak istotnej przewagi zysku lub straty"
                    PairRiskSignificance.LOW_SAMPLE -> "Wstępny szacunek — mała próba; bez wniosku o przewadze"
                    PairRiskSignificance.INSUFFICIENT -> if (item.direction == ForecastDirection.NEUTRAL)
                        "Model nie wskazuje kierunku — brak sygnału transakcyjnego"
                    else "Za mało niezależnych prób do oceny statystycznej"
                }
                val statusColor = when (item.significance) {
                    PairRiskSignificance.PROFIT_ADVANTAGE -> Color(0xFF168447)
                    PairRiskSignificance.LOSS_ADVANTAGE -> Color(0xFFB3261E)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${item.horizon.label} • prognozowana zmiana: ${if (item.expectedChangePercent > 0) "+" else ""}${"%.2f".format(item.expectedChangePercent)}%",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Prawdopodobieństwo zysku", style = MaterialTheme.typography.labelSmall)
                            Text(
                                item.profitProbability?.let { "${"%.1f".format(it * 100)}%" } ?: "—",
                                color = profitColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Prawdopodobieństwo straty", style = MaterialTheme.typography.labelSmall)
                            Text(
                                item.lossProbability?.let { "${"%.1f".format(it * 100)}%" } ?: "—",
                                color = lossColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(Modifier.weight(0.8f)) {
                            Text("Neutralnie", style = MaterialTheme.typography.labelSmall)
                            Text(
                                item.neutralProbability?.let { "${"%.1f".format(it * 100)}%" } ?: "—",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Text(status, color = statusColor, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Próby walidacyjne: ${item.samples} • sygnał: ${when (item.direction) {
                            ForecastDirection.UP -> "wzrost"
                            ForecastDirection.DOWN -> "spadek"
                            ForecastDirection.NEUTRAL -> "neutralny"
                        }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Text(
                "Walidacja wykorzystuje niepokrywające się okresy wyników. Szacunki od 10 do 19 prób są oznaczane jako wstępne; " +
                    "kolor przewagi pojawia się dopiero od 20 prób i po rozdzieleniu 95-procentowych przedziałów Wilsona. " +
                    "Wyniki nie uwzględniają spreadu, prowizji ani podatków.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

