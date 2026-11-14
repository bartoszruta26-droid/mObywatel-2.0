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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.financial.BankStatementEntry
import pl.gov.obywatel.android.financial.BankExchangeOperationProjector
import pl.gov.obywatel.android.financial.ExchangeOperationStatus
import pl.gov.obywatel.android.financial.BankImportPreview
import pl.gov.obywatel.android.financial.PaymentLedgerRepository
import pl.gov.obywatel.android.financial.PaymentAccountScope
import pl.gov.obywatel.android.financial.StoredFinancialTransaction
import pl.gov.obywatel.android.financial.BankStatementXmlParser
import pl.gov.obywatel.android.financial.PaymentLedgerJsonCodec
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Ekran rejestru odczytuje dane z SQLite przez repozytorium pracujące na IO. */
@Composable
fun PaymentLedgerScreen(onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { PaymentLedgerRepository(context) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<BankStatementEntry>>(emptyList()) }
    var financialTransactions by remember { mutableStateOf<List<StoredFinancialTransaction>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var importPreview by remember { mutableStateOf<BankImportPreview?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var accountScopeId by remember { mutableStateOf("ipko-account-1") }
    var accountScopes by remember { mutableStateOf<List<PaymentAccountScope>>(emptyList()) }
    var expandedExchangeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val exchangeOperations = remember(entries) { BankExchangeOperationProjector.project(entries) }
    val groupedEntryIds = remember(exchangeOperations) {
        exchangeOperations.flatMap { it.legs }.map { it.accountingEntryId() }.toSet()
    }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                entries = repository.getEntries()
                financialTransactions = repository.getFinancialTransactions()
                accountScopes = repository.getAccountScopes()
                if (accountScopes.none { it.accountScopeId == accountScopeId }) {
                    accountScopeId = accountScopes.firstOrNull()?.accountScopeId ?: "ipko-account-1"
                }
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
                    val json = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Nie można odczytać wybranego pliku.")
                        input.use { readUtf8Limited(it, MAX_IMPORT_BYTES) }
                    }
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
    val xmlImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val selectedAccountScopeId = accountScopeId.trim()
            scope.launch {
                try {
                    // Każdy zakres użyty do importu musi być zarejestrowany lokalnie,
                    // aby po ponownym uruchomieniu nie powstał osierocony alias.
                    val knownScopes = repository.getAccountScopes()
                    if (knownScopes.none { it.accountScopeId == selectedAccountScopeId }) {
                        repository.saveAccountScope(selectedAccountScopeId, selectedAccountScopeId)
                        accountScopes = repository.getAccountScopes()
                    }
                    val archiveJson = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Nie można odczytać wybranego pliku XML.")
                        val xml = input.use { readUtf8Limited(it, MAX_IMPORT_BYTES) }
                        val parsedEntries = BankStatementXmlParser.parse(xml, selectedAccountScopeId)
                        PaymentLedgerJsonCodec.encode(parsedEntries)
                    }
                    val preview = repository.previewJson(archiveJson)
                    pendingJson = archiveJson
                    importPreview = preview
                    message = "XML iPKO odczytano. Sprawdź podgląd przed zatwierdzeniem."
                    error = null
                } catch (exception: Exception) {
                    pendingJson = null
                    importPreview = null
                    error = exception.message ?: "Nie udało się odczytać pliku XML."
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
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: throw IllegalStateException("Nie można zapisać pliku.")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
                    }
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
                OutlinedTextField(
                    value = accountScopeId,
                    onValueChange = { accountScopeId = it },
                    label = { Text("Pseudonim wybranego rachunku") },
                    supportingText = { Text("Alias lokalny, np. ipko-account-1. Nie wpisuj numeru rachunku.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    repository.saveAccountScope(accountScopeId, accountScopeId)
                                    accountScopes = repository.getAccountScopes()
                                    message = "Zapisano pseudonim rachunku."
                                    error = null
                                } catch (exception: Exception) {
                                    error = exception.message ?: "Nie udało się zapisać aliasu."
                                }
                            }
                        },
                        enabled = accountScopeId.isNotBlank()
                    ) { Text("Zapisz alias") }
                }
                if (accountScopes.isNotEmpty()) {
                    Text("Zapisane pseudonimy — wybierz rachunek do importu XML:")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accountScopes, key = { it.accountScopeId }) { scopeItem ->
                            Button(
                                onClick = {
                                    accountScopeId = scopeItem.accountScopeId
                                    message = "Wybrano: ${scopeItem.displayName}"
                                    error = null
                                }
                            ) {
                                Text(
                                    if (scopeItem.accountScopeId == accountScopeId) {
                                        "✓ ${scopeItem.displayName}"
                                    } else {
                                        scopeItem.displayName
                                    }
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }) {
                        Text("Importuj JSON")
                    }
                    Button(onClick = { xmlImportLauncher.launch(arrayOf("application/xml", "text/xml", "application/octet-stream")) }) {
                        Text("Importuj XML iPKO")
                    }
                }
                Button(onClick = { exportLauncher.launch("rachunki-platnicze.json") }) {
                    Text("Eksportuj JSON")
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
            Text("Rejestr jest pusty. Możesz zaimportować archiwum JSON.")
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (financialTransactions.isNotEmpty()) {
                item {
                    Text("Transakcje finansowe", style = MaterialTheme.typography.titleMedium)
                }
                items(financialTransactions, key = { "financial-${it.transaction.id}" }) { stored ->
                    val transaction = stored.transaction
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${transaction.type} • ${transaction.executedAt}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            transaction.fromAssetCode?.let { code ->
                                Text("Oddano: ${transaction.fromAmount?.toPlainString()} $code")
                            }
                            transaction.toAssetCode?.let { code ->
                                Text("Otrzymano: ${transaction.toAmount?.toPlainString()} $code")
                            }
                            transaction.fee?.let { fee ->
                                Text("Prowizja: ${fee.amount.toPlainString()} ${fee.assetCode}")
                            }
                            Text(
                                "Zapisy bankowe powiązane: ${stored.accountingEntryIds.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            if (exchangeOperations.isNotEmpty()) {
                item {
                    Text("Operacje wymiany walut", style = MaterialTheme.typography.titleMedium)
                }
                items(exchangeOperations, key = { it.operationId }) { operation ->
                    val expanded = operation.operationId in expandedExchangeIds
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                when (operation.status) {
                                    ExchangeOperationStatus.COMPLETE -> "Wymiana kompletna"
                                    ExchangeOperationStatus.SINGLE_LEG -> "Wymiana niepełna — jedna noga"
                                    ExchangeOperationStatus.REVIEW_REQUIRED -> "Wymiana wymaga weryfikacji"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (operation.status == ExchangeOperationStatus.REVIEW_REQUIRED) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                "Zapisy: ${operation.legs.size} • Waluty: ${operation.currencies.sorted().joinToString(", ")}"
                            )
                            Button(
                                onClick = {
                                    expandedExchangeIds = if (expanded) {
                                        expandedExchangeIds - operation.operationId
                                    } else {
                                        expandedExchangeIds + operation.operationId
                                    }
                                }
                            ) {
                                Text(if (expanded) "Ukryj zapisy źródłowe" else "Pokaż zapisy źródłowe")
                            }
                            if (expanded) {
                                operation.legs.forEach { leg ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            "${leg.bookingDate} • ${leg.currencyCode}",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "${if (leg.signedAmount.signum() > 0) "+" else ""}${leg.signedAmount.toPlainString()} ${leg.currencyCode}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(leg.operationType, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Pozostałe zapisy księgowe", style = MaterialTheme.typography.titleMedium)
            }
            items(entries.filterNot { it.accountingEntryId() in groupedEntryIds }, key = { it.accountingEntryId() }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${entry.bookingDate} • ${entry.currencyCode}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${if (entry.signedAmount.signum() > 0) "+" else ""}${entry.signedAmount.toPlainString()} ${entry.currencyCode}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(entry.operationType, style = MaterialTheme.typography.bodySmall)
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


private const val MAX_IMPORT_BYTES = 10 * 1024 * 1024

/** Odczyt z limitem bajtów — chroni UI i pamięć przed nieograniczonym plikiem wejściowym. */
private fun readUtf8Limited(input: InputStream, maxBytes: Int): String {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Plik przekracza limit 10 MB." }
        output.write(buffer, 0, count)
    }
    return output.toString(Charsets.UTF_8.name())
}
