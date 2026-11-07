// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.data.ImportedDocument
import pl.gov.obywatel.android.data.ImportedDocumentStore

private const val MAX_IMAGE_PREVIEW_DIMENSION = 2048
private const val MAX_PDF_PREVIEW_DIMENSION = 1800
private const val DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

private data class DocumentPreview(
    val bitmap: Bitmap,
    val pageCount: Int,
    val error: String? = null
)

@Composable
fun DocumentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var documents by remember { mutableStateOf<List<ImportedDocument>>(emptyList()) }
    var selected by remember { mutableStateOf<ImportedDocument?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingDocuments by remember { mutableStateOf(true) }

    // Wstecz najpierw zamyka podgląd dokumentu; dopiero kolejny powrót
    // opuszcza ekran dokumentów (obsługiwany przez MainActivity).
    BackHandler(enabled = selected != null) { selected = null }

    LaunchedEffect(Unit) {
        documents = withContext(Dispatchers.IO) { ImportedDocumentStore.list(context) }
        isLoadingDocuments = false
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        // Magazyn najpierw waliduje MIME, a następnie atomowo
                        // utrwala uprawnienie SAF razem z wpisem na liście.
                        val document = ImportedDocumentStore.add(context, uri)
                        document to ImportedDocumentStore.list(context)
                    }
                }
                result.onSuccess { (document, updatedDocuments) ->
                    documents = updatedDocuments
                    selected = document
                    errorMessage = null
                }.onFailure { error ->
                    errorMessage = error.message ?: "Nie udało się dodać pliku."
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (selected != null) selected = null else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    selected?.title ?: "Moje dokumenty",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (selected == null) {
                Text(
                    "Pliki pozostają na urządzeniu lub u wybranego dostawcy. " +
                        "To lokalny prototyp — dokumenty nie są weryfikowane przez administrację.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { importLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        DOCX_MIME_TYPE,
                        "image/jpeg",
                        "image/png",
                        "image/webp",
                        "image/gif",
                        "image/bmp"
                    )
                ) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDocuments
                ) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null)
                    Text("  Dodaj PDF, DOCX lub obraz")
                }
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.height(12.dp))
                when {
                    isLoadingDocuments -> Box(
                        Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    documents.isEmpty() -> Box(
                        Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Brak dokumentów. Dodaj plik, aby rozpocząć.")
                    }

                    else -> LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(documents, key = { it.uri.toString() }) { document ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { selected = document },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (document.mimeType) {
                                            "application/pdf" -> Icons.Filled.PictureAsPdf
                                            DOCX_MIME_TYPE -> Icons.Filled.Description
                                            else -> Icons.Filled.Image
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(document.title, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            when (document.mimeType) {
                                                "application/pdf" -> "PDF"
                                                DOCX_MIME_TYPE -> "Dokument Word"
                                                else -> document.mimeType
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                runCatching {
                                                    ImportedDocumentStore.remove(context, document.uri)
                                                    ImportedDocumentStore.list(context)
                                                }
                                            }
                                            result.onSuccess { updated ->
                                                documents = updated
                                                errorMessage = null
                                            }.onFailure { failure ->
                                                errorMessage = failure.message
                                                    ?: "Nie udało się usunąć dokumentu."
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Usuń z listy")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (selected!!.mimeType == DOCX_MIME_TYPE) {
                    DocxPreviewPane(document = selected!!, modifier = Modifier.weight(1f).fillMaxWidth())
                } else {
                    DocumentPreviewPane(document = selected!!, modifier = Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

private data class DocxPreview(val text: String = "", val error: String? = null)

@Composable
private fun DocxPreviewPane(document: ImportedDocument, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preview by produceState<DocxPreview?>(initialValue = null, document.uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val input = checkNotNull(context.contentResolver.openInputStream(document.uri)) {
                    "Nie można otworzyć dokumentu DOCX."
                }
                input.use { DocxTextExtractor.extract(it) }
            }.fold(
                onSuccess = { DocxPreview(text = it) },
                onFailure = { DocxPreview(error = it.message ?: "Nie udało się odczytać dokumentu DOCX.") }
            )
        }
    }

    when {
        preview == null -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        preview?.error != null -> Box(modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(preview?.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        else -> Column(
            modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Podgląd tekstu DOCX", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Układ, tabele, obrazy i formatowanie mogą różnić się od oryginału.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Text(preview?.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun DocumentPreviewPane(document: ImportedDocument, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var page by remember(document.uri) { mutableIntStateOf(0) }
    val preview by produceState<DocumentPreview?>(initialValue = null, document.uri, page) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (document.mimeType == "application/pdf") {
                    val descriptor = checkNotNull(context.contentResolver.openFileDescriptor(document.uri, "r")) {
                        "Nie można otworzyć pliku PDF."
                    }
                    descriptor.use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            require(renderer.pageCount > 0) { "Plik PDF nie zawiera stron." }
                            val safePage = page.coerceIn(0, renderer.pageCount - 1)
                            renderer.openPage(safePage).use { pdfPage ->
                                val dimensions = calculatePreviewDimensions(
                                    pdfPage.width,
                                    pdfPage.height,
                                    MAX_PDF_PREVIEW_DIMENSION
                                )
                                val bitmap = Bitmap.createBitmap(
                                    dimensions.width,
                                    dimensions.height,
                                    Bitmap.Config.ARGB_8888
                                )
                                try {
                                    bitmap.eraseColor(Color.WHITE)
                                    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    DocumentPreview(bitmap, renderer.pageCount)
                                } catch (failure: Exception) {
                                    bitmap.recycle()
                                    throw failure
                                }
                            }
                        }
                    }
                } else {
                    DocumentPreview(decodeSampledImage(context, document), 1)
                }
            }.getOrElse {
                DocumentPreview(
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                    1,
                    it.message ?: "Błąd podglądu."
                )
            }
        }
    }

    // Bitmapa należy do bieżącego podglądu; zwolnij ją po zmianie strony
    // lub dokumentu oraz po opuszczeniu ekranu.
    DisposableEffect(preview?.bitmap) {
        val bitmap = preview?.bitmap
        onDispose {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    // Każda strona zaczyna się z neutralnym powiększeniem i położeniem.
    var scale by remember(document.uri, page) { mutableFloatStateOf(1f) }
    var offsetX by remember(document.uri, page) { mutableFloatStateOf(0f) }
    var offsetY by remember(document.uri, page) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offsetX += pan.x
        offsetY += pan.y
    }

    Column(modifier) {
        when {
            preview == null -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            preview?.error != null -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(preview?.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            else -> {
                Image(
                    bitmap = preview!!.bitmap.asImageBitmap(),
                    contentDescription = "Podgląd dokumentu ${document.title}, strona ${page + 1}",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .transformable(transformState)
                )
                if (preview!!.pageCount > 1) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { page = (page - 1).coerceAtLeast(0) }, enabled = page > 0) {
                            Text("Poprzednia")
                        }
                        Text("Strona ${page + 1} / ${preview!!.pageCount}")
                        OutlinedButton(
                            onClick = { page = (page + 1).coerceAtMost(preview!!.pageCount - 1) },
                            enabled = page < preview!!.pageCount - 1
                        ) { Text("Następna") }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        enabled = scale != 1f || offsetX != 0f || offsetY != 0f
                    ) {
                        Text("Przywróć widok")
                    }
                }
                Text(
                    "Uszczypnij, aby powiększyć; przeciągnij, aby przesunąć.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun decodeSampledImage(
    context: android.content.Context,
    document: ImportedDocument
): Bitmap {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(document.uri)
        ?: error("Nie można odczytać obrazu.")
    boundsStream.use { input -> BitmapFactory.decodeStream(input, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Nieprawidłowy plik obrazu." }

    val sampleSize = calculateInSampleSize(
        bounds.outWidth,
        bounds.outHeight,
        MAX_IMAGE_PREVIEW_DIMENSION
    )

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return resolver.openInputStream(document.uri)?.use { input ->
        checkNotNull(BitmapFactory.decodeStream(input, null, options)) {
            "Nie można zdekodować obrazu."
        }
    } ?: error("Nie można ponownie otworzyć obrazu.")
}
