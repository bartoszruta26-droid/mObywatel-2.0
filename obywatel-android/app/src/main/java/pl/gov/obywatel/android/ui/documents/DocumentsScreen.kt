// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.data.ImportedDocument
import pl.gov.obywatel.android.data.ImportedDocumentStore

private const val MAX_IMAGE_PREVIEW_DIMENSION = 2048
private const val MAX_PDF_PREVIEW_DIMENSION = 1800
private const val DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
private const val ODT_MIME_TYPE = "application/vnd.oasis.opendocument.text"

private data class DocumentPreview(
    val bitmap: Bitmap,
    val pageCount: Int,
    val error: String? = null
)

@Composable
fun DocumentsScreen(onBack: () -> Unit, readOnly: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var documents by remember { mutableStateOf<List<ImportedDocument>>(emptyList()) }
    var selected by remember { mutableStateOf<ImportedDocument?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingDocuments by remember { mutableStateOf(true) }

    BackHandler(enabled = readOnly && selected != null) { selected = null }

    LaunchedEffect(Unit) {
        try {
            documents = withContext(Dispatchers.IO) { ImportedDocumentStore.list(context) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            errorMessage = failure.message ?: "Nie udało się wczytać listy dokumentów."
        } finally {
            isLoadingDocuments = false
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val document = ImportedDocumentStore.add(context, uri)
                        Result.success(document to ImportedDocumentStore.list(context))
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        Result.failure(failure)
                    }
                }
                result.onSuccess { (document, updatedDocuments) ->
                    documents = updatedDocuments
                    selected = null
                    errorMessage = null
                    statusMessage = "Dodano plik: ${document.title}. Podgląd jest dostępny w portfelu dokumentów."
                }.onFailure { error ->
                    errorMessage = error.message ?: "Nie udało się dodać pliku."
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().semantics {
            paneTitle = if (readOnly && selected != null) "Podgląd dokumentu" else if (readOnly) "Dokumenty" else "Zarządzanie dokumentami"
        },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (readOnly && selected != null) selected = null else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (readOnly && selected != null) "Wróć do listy dokumentów" else "Wróć do poprzedniego ekranu")
                }
                Text(
                    if (readOnly && selected != null) selected!!.title else if (readOnly) "Moje dokumenty" else "Zarządzanie plikami dokumentów",
                    style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).semantics { heading() }
                )
            }

            if (readOnly && selected != null) {
                val document = selected!!
                when (document.mimeType) {
                    DOCX_MIME_TYPE -> DocxPreviewPane(document = document, modifier = Modifier.weight(1f).fillMaxWidth())
                    ODT_MIME_TYPE -> OdtPreviewPane(document = document, modifier = Modifier.weight(1f).fillMaxWidth())
                    else -> DocumentPreviewPane(document = document, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                Text("Podgląd wyłącznie do odczytu. Dodawanie i usuwanie plików jest dostępne w Więcej → Ustawienia → Dokumenty własne — pliki.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    if (readOnly) "Wybierz dokument, aby otworzyć jego podgląd. Ten widok nie umożliwia modyfikacji plików."
                    else "Dodawaj i usuwaj pliki. Ich podgląd jest dostępny osobno w portfelu dokumentów.",
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!readOnly) Button(onClick = {
                    importLauncher.launch(arrayOf("application/pdf", DOCX_MIME_TYPE, ODT_MIME_TYPE,
                        "image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp"))
                }, modifier = Modifier.fillMaxWidth(), enabled = !isLoadingDocuments) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Dodaj PDF, DOCX, ODT lub obraz")
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp).semantics { liveRegion = LiveRegionMode.Polite }) }
                statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(12.dp))
                when {
                    isLoadingDocuments -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    documents.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            Image(painter = painterResource(id = pl.gov.obywatel.android.R.drawable.empty_documents_illustration),
                                contentDescription = null, modifier = Modifier.size(148.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(if (readOnly) "Brak dokumentów do wyświetlenia." else "Brak dokumentów. Dodaj plik, aby rozpocząć.",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    else -> LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(documents, key = { it.uri.toString() }) { document ->
                            Card(
                                modifier = Modifier.fillMaxWidth().then(
                                    if (readOnly) Modifier.clickable(onClickLabel = "Otwórz dokument ${document.title}", role = Role.Button) { selected = document }
                                    else Modifier
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(when (document.mimeType) {
                                        "application/pdf" -> Icons.Filled.PictureAsPdf
                                        DOCX_MIME_TYPE, ODT_MIME_TYPE -> Icons.Filled.Description
                                        else -> Icons.Filled.Image
                                    }, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(document.title, fontWeight = FontWeight.SemiBold)
                                        Text(when (document.mimeType) {
                                            "application/pdf" -> "PDF"
                                            DOCX_MIME_TYPE -> "Dokument Word (DOCX)"
                                            ODT_MIME_TYPE -> "Dokument OpenDocument (ODT)"
                                            else -> document.mimeType
                                        }, style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (readOnly) Text("Podgląd", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                    else IconButton(onClick = {
                                        scope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                try {
                                                    ImportedDocumentStore.remove(context, document.uri)
                                                    Result.success(ImportedDocumentStore.list(context))
                                                } catch (cancelled: CancellationException) {
                                                    throw cancelled
                                                } catch (failure: Exception) {
                                                    Result.failure(failure)
                                                }
                                            }
                                            result.onSuccess { updated ->
                                                documents = updated
                                                errorMessage = null
                                                statusMessage = "Usunięto plik: ${document.title}."
                                            }.onFailure { failure ->
                                                errorMessage = failure.message ?: "Nie udało się usunąć dokumentu."
                                            }
                                        }
                                    }) { Icon(Icons.Filled.DeleteOutline, contentDescription = "Usuń dokument: ${document.title}") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
