// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TorClientSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clients = remember {
        mutableStateListOf<TorClientProfile>().apply { addAll(TorClientRegistry.load(context)) }
    }
    var activeId by remember { mutableStateOf(TorClientRegistry.activeId(context)) }
    var connectionMode by remember { mutableStateOf(DownloadConnectionSettings.get(context)) }
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("9050") }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    val probeStatus = remember { mutableStateMapOf<String, String>() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    fun persist() {
        TorClientRegistry.save(context, clients.toList())
        val routeProfileId = when (connectionMode) {
            DownloadConnectionMode.GUARDIAN_TOR -> "guardian-embedded"
            DownloadConnectionMode.ORBOT -> "orbot-local"
            DownloadConnectionMode.DIRECT -> null
        }
        if (routeProfileId != null && clients.any { it.id == routeProfileId && it.enabled }) {
            activeId = routeProfileId
            TorClientRegistry.setActive(context, routeProfileId)
        }
    }

    fun chooseMode(mode: DownloadConnectionMode) {
        connectionMode = mode
        DownloadConnectionSettings.set(context, mode)
        val profileId = when (mode) {
            DownloadConnectionMode.GUARDIAN_TOR -> "guardian-embedded"
            DownloadConnectionMode.ORBOT -> "orbot-local"
            DownloadConnectionMode.DIRECT -> null
        }
        if (profileId != null && clients.any { it.id == profileId && it.enabled }) {
            activeId = profileId
            TorClientRegistry.setActive(context, profileId)
        }
        notice = if (mode == DownloadConnectionMode.ORBOT && clients.none { it.id == "orbot-local" && it.enabled }) {
            "Wybrano Orbot, ale jego profil jest wyłączony. Włącz profil Orbot poniżej."
        } else {
            "Zapisano tryb pobierania: ${mode.label}"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Klienci sieci Tor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Wybierz jawnie sposób pobierania plików. Tryb Tor korzysta wyłącznie z wybranego klienta i nie przełącza się automatycznie na połączenie bezpośrednie.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ważne — bezpieczeństwo połączenia", fontWeight = FontWeight.Bold)
                Text("Wybrany tryb jest stosowany do pobierania materiałów, symboli SVG i pozostałych zasobów. Guardian i Orbot są sprawdzane pod kątem rzeczywistego wyjścia przez Tor. Gdy wybrany klient Tor nie działa, pobieranie kończy się czytelnym błędem — aplikacja nie przełącza po cichu na inny klient ani na połączenie bezpośrednie. Tryb bezpośredni omija lokalne proxy Tor i ujawnia adres IP urządzenia serwisowi docelowemu; systemowy VPN, np. Orbot w trybie VPN całego urządzenia, nadal może tunelować ruch.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Sposób pobierania", fontWeight = FontWeight.Bold)
        DownloadConnectionMode.values().forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    chooseMode(mode)
                }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = connectionMode == mode,
                    onClick = {
                        chooseMode(mode)
                    }
                )
                Column(Modifier.weight(1f)) {
                    Text(mode.label, fontWeight = FontWeight.SemiBold)
                    Text(mode.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text("Profile klientów", fontWeight = FontWeight.Bold)
        Text("Tryb pobierania wybierasz powyżej. Profile Guardian i Orbot są powiązane z odpowiednimi trybami; dodatkowe profile lokalne służą obecnie do diagnostyki.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        clients.forEach { client ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = when (client.id) {
                                "guardian-embedded" -> connectionMode == DownloadConnectionMode.GUARDIAN_TOR
                                "orbot-local" -> connectionMode == DownloadConnectionMode.ORBOT
                                else -> false
                            },
                            onClick = {
                                when (client.id) {
                                    "guardian-embedded" -> chooseMode(DownloadConnectionMode.GUARDIAN_TOR)
                                    "orbot-local" -> chooseMode(DownloadConnectionMode.ORBOT)
                                }
                            },
                            enabled = client.enabled && client.id in setOf("guardian-embedded", "orbot-local")
                        )
                        Column(Modifier.weight(1f)) {
                            Text(client.name, fontWeight = FontWeight.SemiBold)
                            Text(if (client.id == "guardian-embedded") "Guardian Project · do 4 izolowanych listenerów" else "SOCKS5 · ${client.host}:${client.port}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = client.enabled, enabled = client.id != "guardian-embedded", onCheckedChange = { enabled ->
                            val index = clients.indexOfFirst { it.id == client.id }
                            if (index >= 0) clients[index] = client.copy(enabled = enabled)
                            persist()
                        })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (client.id != "guardian-embedded") OutlinedButton(onClick = {
                            probeStatus[client.id] = "Sprawdzanie SOCKS i wyjścia Tor…"
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    if (client.id.startsWith("orbot-") && (!TorEndpointProbe.isOrbotInstalled(context) || TorDownloadServiceProvider.isEmbeddedEndpoint(client.endpoint()))) false
                                    else TorEndpointProbe.isTor(client.endpoint(), force = true)
                                }
                                probeStatus[client.id] = if (result) "Połączenie działa — potwierdzono wyjście przez Tor."
                                    else "Brak potwierdzonego połączenia Tor. Ten profil nie zostanie użyty."
                            }
                        }) { Text("Testuj") }
                        if (client.id !in setOf("guardian-embedded", "orbot-local")) {
                            OutlinedButton(onClick = {
                                clients.removeAll { it.id == client.id }
                                persist()
                            }) { Text("Usuń") }
                        }
                    }
                    probeStatus[client.id]?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                        color = if (it.startsWith("Połączenie działa")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        Text("Dodaj klienta lokalnego", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it; error = "" },
            label = { Text("Nazwa klienta") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = host, onValueChange = { host = it; error = "" },
            label = { Text("Adres lokalny") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = port, onValueChange = { port = it.filter(Char::isDigit); error = "" },
            label = { Text("Port SOCKS5") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.primary)
        Button(onClick = {
            runCatching {
                val profile = TorClientRegistry.newProfile(name, host, port.toInt())
                require(clients.none { it.host == profile.host && it.port == profile.port }) {
                    "Klient o takim adresie i porcie już istnieje."
                }
                clients.add(profile)
                name = ""
                persist()
                notice = "Dodano profil lokalny do diagnostyki. Aktualne tryby pobierania korzystają z Guardian, Orbot lub jawnego połączenia bezpośredniego."
            }.onFailure { error = it.message ?: "Nie udało się dodać klienta." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj do listy") }
        Text("Orbot: zainstaluj oficjalną aplikację i uruchom Tor. Profil aplikacji korzysta z domyślnego lokalnego SOCKS5 127.0.0.1:9050. Pozostaw ten port w Orbot; inny port wymagałby aktualizacji profilu aplikacji. Następnie użyj przycisku Testuj. Aplikacja nie steruje procesem Orbot — sprawdza dostępność proxy i rzeczywiste wyjście przez sieć Tor. W aktualnych trybach pobierania używany jest profil Orbot; dodatkowe profile lokalne pozostają zapisane do konfiguracji i diagnostyki, ale nie są automatycznie wybierane przez tryb Orbot.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
