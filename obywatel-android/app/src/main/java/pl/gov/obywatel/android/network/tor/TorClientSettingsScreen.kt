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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TorClientSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clients = remember {
        mutableStateListOf<TorClientProfile>().apply { addAll(TorClientRegistry.load(context)) }
    }
    var activeId by remember { mutableStateOf(TorClientRegistry.activeId(context)) }
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("9050") }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    fun persist() {
        TorClientRegistry.save(context, clients.toList())
        if (clients.none { it.id == activeId && it.enabled }) {
            activeId = clients.firstOrNull { it.enabled }?.id ?: "guardian-embedded"
        }
        if (clients.any { it.id == activeId && it.enabled }) TorClientRegistry.setActive(context, activeId)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Klienci sieci Tor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Wybierz klienta Tor używanego przez usługę pobierania. Możesz zarejestrować kilka lokalnych klientów SOCKS5, np. wbudowany Tor i Orbot.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ważne — bezpieczeństwo połączenia", fontWeight = FontWeight.Bold)
                Text("Lista przyjmuje wyłącznie lokalne adresy pętli zwrotnej (127.0.0.1, localhost, ::1). Nie dodawaj przypadkowych publicznych proxy SOCKS: sam adres proxy nie potwierdza, że operator korzysta z Tor ani że nie rejestruje ruchu. Wbudowany klient Guardian Project jest otwartym klientem i łączy się z publiczną siecią Tor. Profile Orbot i własne profile są obecnie zapisywane jako konfiguracja do kolejnego etapu integracji.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Zarejestrowani klienci", fontWeight = FontWeight.Bold)
        clients.forEach { client ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = activeId == client.id,
                            onClick = {
                                if (client.enabled) {
                                    activeId = client.id
                                    TorClientRegistry.setActive(context, client.id)
                                    notice = "Wybrano: ${client.name}"
                                }
                            },
                            enabled = client.enabled
                        )
                        Column(Modifier.weight(1f)) {
                            Text(client.name, fontWeight = FontWeight.SemiBold)
                            Text("SOCKS5 · ${client.host}:${client.port}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = client.enabled, onCheckedChange = { enabled ->
                            val index = clients.indexOfFirst { it.id == client.id }
                            if (index >= 0) clients[index] = client.copy(enabled = enabled)
                            persist()
                        })
                    }
                    if (client.id !in setOf("guardian-embedded", "orbot-local")) {
                        OutlinedButton(onClick = {
                            clients.removeAll { it.id == client.id }
                            persist()
                        }, modifier = Modifier.align(Alignment.End)) { Text("Usuń klienta") }
                    }
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
                notice = "Dodano profil. Jego wybór zapisano; podłączenie go do transportu pobierania nastąpi w kolejnym etapie."
            }.onFailure { error = it.message ?: "Nie udało się dodać klienta." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj do listy") }
        Text("Orbot: po instalacji włącz tryb zaawansowany (Power User Mode), uruchom Tor i pozostaw lokalny port SOCKS5 9050. Aplikacja nie uruchamia ani nie konfiguruje Orbot automatycznie.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
