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
import androidx.compose.foundation.layout.height
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val runtime = remember(context) { GuardianTorRuntime(context) }
    val downloadService = remember(context) { TorDownloadService(context, runtime) }
    val downloader = remember(context) {
        StudiaOnlineBulkDownloader(downloadService, cookieHeaderProvider = {
            CookieManager.getInstance().getCookie("https://studia-online.pl//pobierz-material/")
        })
    }
    val store = remember(context) { StudiaOnlineSafStore(context) }

    DisposableEffect(runtime, downloader) {
        onDispose {
            downloader.stop()
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
    var showLogin by remember { mutableStateOf(false) }
    var sessionCookieCount by remember { mutableStateOf(0) }
    var selectedConnectionMode by remember(context) {
        mutableStateOf(DownloadConnectionSettings.get(context))
    }
    fun refreshSessionStatus() {
        CookieManager.getInstance().flush()
        val cookieHeader = CookieManager.getInstance().getCookie("https://studia-online.pl//pobierz-material/")
        sessionCookieCount = cookieHeader?.split(';')?.count { it.contains('=') } ?: 0
    }
    LaunchedEffect(Unit) { refreshSessionStatus() }

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
        Text(
            "Pobieranie materiałów z serwisu studia-online.pl przez pulę lokalnych klientów Tor. " +
                "Pobieranie jest sekwencyjne: w danym momencie wykonywane jest tylko jedno żądanie.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Konto studia-online.pl", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (sessionCookieCount > 0) "Wykryto $sessionCookieCount ciasteczek dla domeny serwisu. To potwierdza dane sesyjne, ale nie gwarantuje aktywnego logowania."
                    else "Brak ciasteczek sesyjnych. Zaloguj się na stronie, aby pobierać materiały wymagające konta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showLogin = !showLogin }, modifier = Modifier.weight(1f)) {
                        Text(if (showLogin) "Zamknij logowanie" else "Zaloguj do WSKZ")
                    }
                    OutlinedButton(onClick = ::refreshSessionStatus) {
                        Text("Sprawdź sesję")
                    }
                }
                OutlinedButton(
                    onClick = {
                        // Android's CookieManager is shared by WebViews in this app.
                        // Make the scope explicit rather than pretending to clear only WSKZ cookies.
                        CookieManager.getInstance().removeAllCookies {
                            CookieManager.getInstance().flush()
                            sessionCookieCount = 0
                            statusText = "Wyczyszczono ciasteczka WebView aplikacji. Zaloguj się ponownie."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usuń sesję i ciasteczka WebView")
                }
                Text(
                    "Usunięcie czyści ciasteczka wszystkich witryn używanych przez WebView aplikacji, nie tylko WSKZ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showLogin) {
                    Text(
                        "Zaloguj się na oficjalnej stronie WSKZ. Hasło wpisujesz bezpośrednio w formularzu serwisu; aplikacja nie zapisuje go w swoich ustawieniach. Logowanie w oknie WebView korzysta z systemowej trasy sieciowej Androida, natomiast samo pobieranie poniżej używa wybranego trybu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(520.dp),
                        factory = { viewContext ->
                            WebView(viewContext).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                CookieManager.getInstance().setAcceptCookie(true)
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        refreshSessionStatus()
                                    }
                                }
                                loadUrl("https://studia-online.pl/")
                            }
                        },
                        update = { }
                    )
                }
                Text("Trasa pobierania", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DownloadConnectionMode.values().forEach { mode ->
                        OutlinedButton(
                            onClick = {
                                selectedConnectionMode = mode
                                DownloadConnectionSettings.set(context, mode)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                when (mode) {
                                    DownloadConnectionMode.GUARDIAN_TOR -> if (selectedConnectionMode == mode) "✓ Tor" else "Tor"
                                    DownloadConnectionMode.ORBOT -> if (selectedConnectionMode == mode) "✓ Orbot" else "Orbot"
                                    DownloadConnectionMode.DIRECT -> if (selectedConnectionMode == mode) "✓ Bezpośrednio" else "Bezpośrednio"
                                }
                            )
                        }
                    }
                }
                Text(
                    when (selectedConnectionMode) {
                        DownloadConnectionMode.GUARDIAN_TOR -> "Pobieranie przez wbudowany klient Guardian Tor."
                        DownloadConnectionMode.ORBOT -> "Pobieranie przez skonfigurowany lokalny port SOCKS5 Orbot."
                        DownloadConnectionMode.DIRECT -> "Pobieranie bez lokalnego proxy Tor; połączenie może nadal podlegać systemowemu VPN."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider()
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
