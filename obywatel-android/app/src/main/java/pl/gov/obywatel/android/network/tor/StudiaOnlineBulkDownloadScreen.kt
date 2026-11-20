// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun StudiaOnlineBulkDownloadScreen(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val runtime = remember(context) { GuardianTorRuntime(context) }
    val downloadService = remember(context) { TorDownloadService(context, runtime) }
    val downloader = remember(context) { StudiaOnlineBulkDownloader(downloadService) }
    val store = remember(context) { StudiaOnlineSafStore(context) }
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
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            treeUri = uri
            statusText = "Wybrano folder docelowy."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack).padding(bottom = 4.dp))
        Text("Archiwum materiałów", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Pobieranie Twoich materiałów z serwisu studia-online.pl przez skonfigurowaną pulę lokalnych klientów Tor. " +
                "Zadanie pobiera po jednym pliku naraz i zachowuje przerwę między żądaniami.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Zakres numerów materiałów", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(firstId, { firstId = it.filter(Char::isDigit).take(9) },
                        modifier = Modifier.weight(1f), label = { Text("Od numeru") }, singleLine = true)
                    OutlinedTextField(lastId, { lastId = it.filter(Char::isDigit).take(9) },
                        modifier = Modifier.weight(1f), label = { Text("Do numeru") }, singleLine = true)
                }
                Text("Liczba sekund przerwy między plikami: ${intervalSeconds.toInt()}")
                Slider(
                    value = intervalSeconds, onValueChange = { intervalSeconds = it },
                    valueRange = 2f..120f, steps = 58
                )
                OutlinedButton(onClick = { folderPicker.launch(null) }, modifier = Modifier.fillMaxWidth()) {
                    IconLabel("Folder docelowy: ${if (treeUri == null) "nie wybrano" else "wybrany"}", Icons.Filled.Archive)
                }
                Button(
                    enabled = !running && treeUri != null &&
                        firstId.toIntOrNull()?.let { it > 0 } == true &&
                        lastId.toIntOrNull()?.let { it >= (firstId.toIntOrNull() ?: Int.MAX_VALUE) } == true &&
                        ((lastId.toIntOrNull() ?: 0).toLong() - (firstId.toIntOrNull() ?: 0).toLong() < StudiaOnlineDownloadCatalog.MAX_RANGE_SIZE),
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
                                if (runtime.state == TorState.STOPPED || runtime.state == TorState.FAILED) {
                                    withContext(Dispatchers.IO) { runtime.start() }
                                }
                                val result = withContext(Dispatchers.IO) {
                                    downloader.run(items, intervalSeconds.toLong() * 1_000L, save = { item, bytes ->
                                        store.save(destination, item, bytes)
                                    }, onProgress = { update ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            progress = update
                                            paused = update.paused
                                        }
                                    })
                                }
                                progress = result
                                statusText = if (result.finished) "Pobieranie zakończone." else "Zadanie zatrzymane."
                            } catch (failure: Exception) {
                                errorText = failure.message ?: "Błąd pobierania."
                            } finally {
                                running = false
                                paused = false
                                activePause = null
                                activeResume = null
                                activeStop = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { IconLabel("Rozpocznij pobieranie", Icons.Filled.PlayArrow) }
            }
        }
        if (running) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    paused = !paused
                    if (paused) activePause?.invoke() else activeResume?.invoke()
                }, modifier = Modifier.weight(1f)) {
                    IconLabel(if (paused) "Wznów" else "Pauza", if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause)
                }
                OutlinedButton(onClick = {
                    activeStop?.invoke()
                    paused = false
                    statusText = "Zatrzymywanie po bezpiecznym zamknięciu bieżącego żądania…"
                }, modifier = Modifier.weight(1f)) {
                    IconLabel("Zatrzymaj", Icons.Filled.Stop)
                }
            }
        }
        progress?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Postęp: ${it.completed} / ${it.total}", style = MaterialTheme.typography.titleMedium)
                    Text("Pozostało: ${it.remaining}")
                    Text("Pobrano poprawnie: ${it.succeeded.size}")
                    Text("Błędy: ${it.failed.size}")
                    it.currentId?.let { id -> Text("Aktualny materiał: $id") }
                    if (it.failed.isNotEmpty()) {
                        Text("Nieudane numery: ${it.failed.keys.joinToString()}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        Text(statusText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (errorText.isNotBlank()) Text(errorText, color = MaterialTheme.colorScheme.error)
        Text(
            "Uwaga: serwis nie udostępnił w aplikacji automatycznego katalogu materiałów. " +
                "Zakres numerów jest jawnie wybierany, a niezgłoszone numery zostaną zapisane jako błędy. " +
                "Pliki są zapisywane pod nazwą materiału z jego numerem.",
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
