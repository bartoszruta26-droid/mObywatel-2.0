// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudiaOnlineBulkDownloadScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val runtime = remember(context) { TorDownloadServiceProvider.getRuntime(context) }
    val downloadService = remember(context) { TorDownloadServiceProvider.get(context) }
    val downloader = remember(context) { StudiaOnlineBulkDownloader(downloadService) }
    val store = remember(context) { StudiaOnlineSafStore(context) }

    DisposableEffect(runtime, downloader) {
        onDispose {
            downloader.stop()
            TorDownloadServiceProvider.stopGuardian()
        }
    }

    var firstId by remember { mutableStateOf("1") }
    var lastId by remember { mutableStateOf("100") }
    var intervalSeconds by remember { mutableStateOf(15f) }
    var treeUri by remember { mutableStateOf<Uri?>(null) }
    var running by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Wybierz zakres materiałów i folder docelowy.") }
    var activePause by remember { mutableStateOf<(() -> Unit)?>(null) }
    var activeResume by remember { mutableStateOf<(() -> Unit)?>(null) }
    var activeStop by remember { mutableStateOf<(() -> Unit)?>(null) }
    var progress by remember { mutableStateOf<StudiaOnlineDownloadProgress?>(null) }
    var errorText by remember { mutableStateOf("") }
    var torDiagnostic by remember { mutableStateOf("Guardian Project Tor nie został jeszcze sprawdzony.") }
    var checkingTor by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
            treeUri = uri
            statusText = "Wybrano folder docelowy."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "‹  Powrót",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack).padding(bottom = 4.dp)
        )
        Text("Archiwum materiałów", style = MaterialTheme.typography.headlineSmall)
        val selectedMode = DownloadConnectionSettings.get(context)
        Text("Tryb połączenia: ${selectedMode.label}", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Text(
            "Pobieranie materiałów z serwisu studia-online.pl zgodnie z wybranym trybem połączenia. " +
                "Pobieranie jest sekwencyjne: w danym momencie wykonywane jest tylko jedno żądanie.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Diagnostyka Guardian Project Tor", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    enabled = !running && !checkingTor && selectedMode == DownloadConnectionMode.GUARDIAN_TOR,
                    onClick = {
                        checkingTor = true
                        torDiagnostic = "Uruchamianie usługi Guardian i oczekiwanie na obwód Tor…"
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    if (runtime.state == TorState.STOPPED || runtime.state == TorState.FAILED) runtime.start()
                                    val deadline = System.currentTimeMillis() + 120_000L
                                    while (runtime.state != TorState.READY &&
                                        runtime.state != TorState.FAILED &&
                                        System.currentTimeMillis() < deadline
                                    ) {
                                        Thread.sleep(250L)
                                    }
                                    check(runtime.state == TorState.READY) {
                                        "Guardian nie osiągnął READY: ${runtime.failureMessage() ?: runtime.state.name}"
                                    }
                                    val endpoints = runtime.proxyEndpoints()
                                    check(endpoints.isNotEmpty()) { "Guardian zgłosił READY, ale nie udostępnił działającego portu SOCKS." }
                                    val primary = runtime.proxyEndpoint()
                                        ?: error("Guardian nie udostępnił podstawowego portu SOCKS.")
                                    check(TorEndpointProbe.isTor(primary, force = true)) {
                                        "Port SOCKS odpowiada, ale test check.torproject.org nie potwierdził wyjścia przez sieć Tor."
                                    }
                                    "Guardian Project Tor działa. Obwód i wyjście Tor potwierdzone na porcie ${primary.port}; aktywne listenery SOCKS: ${endpoints.joinToString { it.port.toString() }}."
                                }.getOrElse { "Test Guardian Project Tor nie powiódł się: ${it.message ?: it.javaClass.simpleName}" }
                            }
                            torDiagnostic = result
                            checkingTor = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (checkingTor) "Sprawdzanie Tor…" else "Sprawdź Guardian Project Tor")
                }
                if (selectedMode != DownloadConnectionMode.GUARDIAN_TOR) {
                    Text("Test Guardian jest dostępny po wybraniu trybu Guardian Project Tor. Tryb Orbot i połączenie bezpośrednie nie uruchamiają Guardian.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (checkingTor) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(torDiagnostic, color = if (torDiagnostic.startsWith("Guardian Project Tor działa")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Zakres numerów materiałów", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstId,
                        onValueChange = { firstId = it.filter(Char::isDigit).take(9) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Od numeru") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lastId,
                        onValueChange = { lastId = it.filter(Char::isDigit).take(9) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Do numeru") },
                        singleLine = true
                    )
                }
                Text("Przerwa między plikami: ${intervalSeconds.toInt()} s")
                Slider(
                    value = intervalSeconds,
                    onValueChange = { intervalSeconds = it },
                    valueRange = 2f..120f,
                    steps = 58
                )
                OutlinedButton(
                    onClick = { folderPicker.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconLabel(
                        "Folder docelowy: ${if (treeUri == null) "nie wybrano" else "wybrany"}",
                        Icons.Filled.Archive
                    )
                }
                val first = firstId.toIntOrNull()
                val last = lastId.toIntOrNull()
                val validRange = first != null && last != null && first > 0 && last >= first &&
                    last.toLong() - first.toLong() + 1L <= StudiaOnlineDownloadCatalog.MAX_RANGE_SIZE
                Button(
                    enabled = !running && treeUri != null && validRange,
                    onClick = {
                        val items = runCatching {
                            StudiaOnlineDownloadCatalog.fromRange(firstId.toInt(), lastId.toInt())
                        }.getOrElse {
                            errorText = it.message ?: "Nieprawidłowy zakres."
                            return@Button
                        }
                        val destination = treeUri ?: return@Button
                        running = true
                        paused = false
                        errorText = ""
                        progress = null
                        scope.launch {
                            activePause = { downloader.pause() }
                            activeResume = { downloader.resume() }
                            activeStop = { downloader.stop() }
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    downloader.run(
                                        items = items,
                                        cooldownMs = intervalSeconds.toLong() * 1_000L,
                                        save = { item, bytes -> store.save(destination, item, bytes) },
                                        onProgress = { update ->
                                            scope.launch(Dispatchers.Main.immediate) {
                                                progress = update
                                                paused = update.paused
                                            }
                                        }
                                    )
                                }
                                progress = result
                                statusText = if (result.finished) "Pobieranie zakończone." else "Zadanie zatrzymane."
                            } catch (failure: Exception) {
                                errorText = failure.message ?: "Błąd pobierania."
                            } finally {
                                withContext(Dispatchers.IO) { runCatching { runtime.stop() } }
                                running = false
                                paused = false
                                activePause = null
                                activeResume = null
                                activeStop = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconLabel("Rozpocznij pobieranie", Icons.Filled.PlayArrow)
                }
            }
        }

        if (running) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val nextPaused = !paused
                        if (nextPaused) activePause?.invoke() else activeResume?.invoke()
                        paused = nextPaused
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    IconLabel(
                        if (paused) "Wznów" else "Pauza",
                        if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause
                    )
                }
                OutlinedButton(
                    onClick = {
                        activeStop?.invoke()
                        paused = false
                        statusText = "Zatrzymywanie po bezpiecznym zamknięciu bieżącego żądania…"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    IconLabel("Zatrzymaj", Icons.Filled.Stop)
                }
            }
        }

        progress?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Postęp: ${it.completed} / ${it.total}", style = MaterialTheme.typography.titleMedium)
                    Text("Etap: ${it.phase}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (it.expectedBytes != null && it.expectedBytes > 0L) {
                        LinearProgressIndicator(
                            progress = (it.downloadedBytes.toFloat() / it.expectedBytes.toFloat()).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (it.currentId != null && it.phase.contains("Pobieranie")) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (it.currentId != null && it.phase.contains("Pobieranie")) {
                        Text("Pobrano: ${android.text.format.Formatter.formatFileSize(context, it.downloadedBytes)}" +
                            (it.expectedBytes?.let { size -> " / ${android.text.format.Formatter.formatFileSize(context, size)}" } ?: ""))
                    }
                    Text("Pozostało: ${it.remaining}")
                    Text("Pobrano poprawnie: ${it.succeeded.size}")
                    Text("Błędy: ${it.failed.size}")
                    Text("Sumy SHA-256 zapisanych plików: ${it.sha256ById.size}")
                    it.sha256ById.forEach { (id, checksum) ->
                        Text("Materiał $id · SHA-256: $checksum", style = MaterialTheme.typography.bodySmall)
                    }
                    it.currentId?.let { id -> Text("Aktualny materiał: $id") }
                    if (it.failed.isNotEmpty()) {
                        Text("Szczegóły nieudanych pobrań:", color = MaterialTheme.colorScheme.error)
                        it.failed.forEach { (id, reason) ->
                            Text("Materiał $id: $reason", color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Text(statusText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (errorText.isNotBlank()) Text(errorText, color = MaterialTheme.colorScheme.error)
        Text(
            "W repozytorium nie ma katalogu identyfikatorów materiałów. " +
                "Zakres wybierasz jawnie; numery, których serwer nie udostępni, będą pokazane jako błędy. " +
                "Pliki są zapisywane w wybranym folderze z numerem materiału w nazwie.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconLabel(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Icon(icon, contentDescription = null)
        Text(label)
    }
}
