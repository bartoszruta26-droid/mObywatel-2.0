// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: EUPL-1.2

package pl.gov.obywatel.android.ui.documents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.documentprocessing.ImportedPdfDocumentProcessor
import pl.gov.obywatel.android.documentprocessing.PdfPageProcessingResult

@Composable
internal fun PdfOcrReviewPanel(
    uri: android.net.Uri,
    displayName: String,
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val results = remember(uri) { mutableStateListOf<PdfPageProcessingResult>() }
    var dialogVisible by remember(uri) { mutableStateOf(false) }
    var isProcessing by remember(uri) { mutableStateOf(false) }
    var completed by remember(uri) { mutableStateOf(0) }
    var total by remember(uri) { mutableStateOf(0) }
    var error by remember(uri) { mutableStateOf<String?>(null) }
    var processingJob by remember(uri) { mutableStateOf<Job?>(null) }

    OutlinedButton(
        onClick = { dialogVisible = true },
        modifier = modifier.fillMaxWidth()
    ) { Text("Rozpoznaj tekst PDF (OCR)") }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { if (!isProcessing) dialogVisible = false },
            title = { Text("OCR dokumentu PDF") },
            text = {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(
                        "Strony są renderowane i rozpoznawane kolejno. Wyniki pozostają propozycjami — aplikacja nie zapisuje ich automatycznie.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isProcessing) {
                        CircularProgressIndicator(Modifier.padding(top = 12.dp))
                        Text(
                            "Przetworzono $completed z $total stron",
                            modifier = Modifier.padding(top = 8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite }
                        )
                        TextButton(onClick = { processingJob?.cancel() }) {
                            Text("Przerwij")
                        }
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    results.forEach { pageResult ->
                        Text(
                            "Strona ${pageResult.pageNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                        if (pageResult.error != null) {
                            Text(pageResult.error, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(pageResult.result?.ocr?.text?.ifBlank { "Nie rozpoznano tekstu." }
                                ?: "Nie rozpoznano tekstu.")
                            pageResult.result?.barcodes?.forEach { barcode ->
                                Text("Kod ${barcode.format}: ${barcode.rawValue}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column {
                    Button(
                        enabled = !isProcessing,
                        onClick = {
                            results.clear()
                            error = null
                            completed = 0
                            total = 1
                            isProcessing = true
                            processingJob = scope.launch {
                                try {
                                    ImportedPdfDocumentProcessor(context.contentResolver).process(
                                        uri = uri,
                                        displayName = displayName,
                                        pageNumbers = currentPage..currentPage
                                    ) { done, count, result ->
                                        withContext(Dispatchers.Main.immediate) {
                                            completed = done
                                            total = count
                                            results.add(result)
                                        }
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (failure: Exception) {
                                    error = failure.message ?: "Nie udało się rozpoznać PDF."
                                } finally {
                                    isProcessing = false
                                    processingJob = null
                                }
                            }
                        }
                    ) { Text("Bieżąca strona (${currentPage + 1})") }
                    Button(
                        enabled = !isProcessing,
                        onClick = {
                            results.clear()
                            error = null
                            completed = 0
                            total = pageCount
                            isProcessing = true
                            processingJob = scope.launch {
                                try {
                                    ImportedPdfDocumentProcessor(context.contentResolver).process(
                                        uri = uri,
                                        displayName = displayName,
                                        pageNumbers = 0 until pageCount
                                    ) { done, count, result ->
                                        withContext(Dispatchers.Main.immediate) {
                                            completed = done
                                            total = count
                                            results.add(result)
                                        }
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (failure: Exception) {
                                    error = failure.message ?: "Nie udało się rozpoznać PDF."
                                } finally {
                                    isProcessing = false
                                    processingJob = null
                                }
                            }
                        }
                    ) { Text("Cały dokument (${pageCount} stron)") }
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }, enabled = !isProcessing) {
                    Text("Zamknij")
                }
            }
        )
    }
}
