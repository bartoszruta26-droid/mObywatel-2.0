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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.documentprocessing.ImportedImageDocumentProcessor
import pl.gov.obywatel.android.documentprocessing.ProcessingOperation
import pl.gov.obywatel.android.documentprocessing.ProcessingResult

/**
 * Presents OCR and barcode suggestions for an imported raster image.
 * This component deliberately has no document-write action: users can inspect
 * and copy the suggestions, while persistence remains a separate explicit flow.
 */
@Composable
internal fun DocumentOcrReviewPanel(
    uri: android.net.Uri,
    mimeType: String,
    displayName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialogVisible by remember(uri) { mutableStateOf(false) }
    var isProcessing by remember(uri) { mutableStateOf(false) }
    var result by remember(uri) { mutableStateOf<ProcessingResult?>(null) }
    var error by remember(uri) { mutableStateOf<String?>(null) }

    OutlinedButton(
        onClick = { dialogVisible = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Rozpoznaj tekst i kody (OCR)")
    }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { if (!isProcessing) dialogVisible = false },
            title = { Text("Rozpoznawanie dokumentu") },
            text = {
                Column(
                    Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Rozpoznane dane są jedynie propozycjami. Sprawdź je z oryginałem; aplikacja nie zapisuje ich automatycznie.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isProcessing) {
                        CircularProgressIndicator(Modifier.padding(top = 16.dp))
                    }
                    error?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    result?.let { processed ->
                        Text(
                            "Rozpoznany tekst",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            processed.ocr?.text?.takeIf(String::isNotBlank)
                                ?: "Nie rozpoznano tekstu.",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            "Kody kreskowe i QR: ${processed.barcodes.size}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        processed.barcodes.forEachIndexed { index, barcode ->
                            Text(
                                "${index + 1}. ${barcode.format}: ${barcode.rawValue}",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            error = null
                            result = null
                            scope.launch {
                                try {
                                    result = withContext(Dispatchers.IO) {
                                        ImportedImageDocumentProcessor(context.contentResolver)
                                            .process(
                                                uri = uri,
                                                mimeType = mimeType,
                                                displayName = displayName,
                                                operations = setOf(
                                                    ProcessingOperation.OCR,
                                                    ProcessingOperation.BARCODE_RECOGNITION
                                                )
                                            )
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (failure: Exception) {
                                    error = failure.message
                                        ?: "Nie udało się rozpoznać obrazu."
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = !isProcessing
                ) {
                    Text(if (result == null) "Rozpocznij OCR" else "Ponów OCR")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogVisible = false },
                    enabled = !isProcessing
                ) { Text("Zamknij") }
            }
        )
    }
}
