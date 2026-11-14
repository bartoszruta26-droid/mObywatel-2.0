// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.gov.obywatel.android.financial.BankStatementEntry
import pl.gov.obywatel.android.financial.BankImportPreview
import pl.gov.obywatel.android.financial.PaymentLedgerRepository

/** Ekran rejestru odczytuje dane z SQLite przez repozytorium pracujące na IO. */
@Composable
fun PaymentLedgerScreen(onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { PaymentLedgerRepository(context) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<BankStatementEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var importPreview by remember { mutableStateOf<BankImportPreview?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                entries = repository.getEntries()
            } catch (exception: Exception) {
                error = exception.message ?: "Nie udało się odczytać rejestru."
            } finally {
                loading = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: error("Nie można odczytać wybranego pliku.")
                    val preview = repository.previewJson(json)
                    pendingJson = json
                    importPreview = preview
                    message = "Wczytano plik do podglądu."
                    error = null
                } catch (exception: Exception) {
                    pendingJson = null
                    importPreview = null
                    error = exception.message ?: "Nie udało się przygotować importu."
                }
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = repository.exportJson()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                        it.write(json)
                    } ?: error("Nie można zapisać pliku.")
                    message = "Eksport JSON został zapisany."
                    error = null
                } catch (exception: Exception) {
                    error = exception.message ?: "Nie udało się zapisać eksportu."
                }
            }
        }
    }

    LaunchedEffect(repository) { reload() }
    DisposableEffect(repository) {
        onDispose { repository.close() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Rachunki płatnicze", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) { Text("Wróć") }
        }
        Text(
            "Lokalny rejestr transakcji walutowych, kruszcowych i darowizn gotówkowych.",
            style = MaterialTheme.typography.bodyLarge
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Import i eksport danych", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }) {
                        Text("Importuj JSON")
                    }
                    Button(onClick = { exportLauncher.launch("rachunki-platnicze.json") }) {
                        Text("Eksportuj JSON")
                    }
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                importPreview?.let { preview ->
                    Text("Podgląd: nowe ${preview.newEntries.size}, duplikaty ${preview.duplicates.size}, konflikty ${preview.conflicts.size}")
                    preview.conflicts.forEach { conflict ->
                        Text(
                            "Konflikt: ${conflict.incoming.bankOperationReference} — ${conflict.existing.currencyCode} ${conflict.existing.signedAmount} / ${conflict.incoming.currencyCode} ${conflict.incoming.signedAmount}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            val json = pendingJson
                            if (json != null && preview.conflicts.isEmpty()) {
                                scope.launch {
                                    try {
                                        val summary = repository.importJson(json)
                                        message = "Dodano: ${summary.inserted}; duplikaty: ${summary.duplicates}."
                                        pendingJson = null
                                        importPreview = null
                                        reload()
                                    } catch (exception: Exception) {
                                        error = exception.message ?: "Import nie powiódł się."
                                    }
                                }
                            }
                        },
                        enabled = pendingJson != null && preview.conflicts.isEmpty() && !loading
                    ) { Text("Zatwierdź import") }
                    if (preview.conflicts.isNotEmpty()) {
                        Text("Import zablokowany do czasu wyjaśnienia konfliktów.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Historia zapisów bankowych", style = MaterialTheme.typography.titleMedium)
                Text("Liczba zapisów: ${entries.size}")
                Button(onClick = { reload() }, enabled = !loading) { Text("Odśwież rejestr") }
                if (loading) CircularProgressIndicator()
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        if (!loading && entries.isEmpty() && error == null) {
            Text("Rejestr jest pusty. Import wyciągów zostanie udostępniony w kolejnym etapie.")
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { it.accountingEntryId() }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${entry.bookingDate} • ${entry.currencyCode}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${if (entry.signedAmount.signum() > 0) "+" else ""}${entry.signedAmount.toPlainString()} ${entry.currencyCode}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(entry.operationType, style = MaterialTheme.typography.bodySmall)
                        if (entry.description.isNotBlank()) {
                            Text(entry.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Darowizny gotówkowe", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Oddzielna ewidencja z obowiązkową nazwą organizacji-beneficjenta i celem darowizny.")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Notowania i wycena", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Kursy NBP i referencyjna wycena aktywów zostaną dodane w kolejnych etapach.")
            }
        }
    }
}
