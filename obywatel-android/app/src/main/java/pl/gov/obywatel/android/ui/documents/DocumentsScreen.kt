// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
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
import pl.gov.obywatel.android.ui.theme.Navy
import pl.gov.obywatel.android.ui.theme.White

private const val MAX_IMAGE_PREVIEW_DIMENSION = 2048
private const val MAX_PDF_PREVIEW_DIMENSION = 1800

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
                        context.contentResolver.takePersistableUriPermission(
                            uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
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
                    color = Navy,
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
                    onClick = { importLauncher.launch(arrayOf("application/pdf", "image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDocuments
                ) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null)
                    Text("  Dodaj PDF lub obraz")
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
                                colors = CardDefaults.cardColors(containerColor = White)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (document.mimeType == "application/pdf") Icons.Filled.PictureAsPdf
                                        else Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = Navy,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(document.title, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            if (document.mimeType == "application/pdf") "PDF" else document.mimeType,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            val updated = withContext(Dispatchers.IO) {
                                                ImportedDocumentStore.remove(context, document.uri)
                                                ImportedDocumentStore.list(context)
                                            }
                                            documents = updated
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
                DocumentPreviewPane(document = selected!!, modifier = Modifier.weight(1f).fillMaxWidth())
            }
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
                                val longestSide = maxOf(pdfPage.width, pdfPage.height).toFloat()
                                val scale = minOf(2f, MAX_PDF_PREVIEW_DIMENSION / longestSide)
                                val bitmap = Bitmap.createBitmap(
                                    (pdfPage.width * scale).toInt().coerceAtLeast(1),
                                    (pdfPage.height * scale).toInt().coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                bitmap.eraseColor(Color.WHITE)
                                pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                DocumentPreview(bitmap, renderer.pageCount)
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

    var scale by remember(document.uri) { mutableFloatStateOf(1f) }
    var offsetX by remember(document.uri) { mutableFloatStateOf(0f) }
    var offsetY by remember(document.uri) { mutableFloatStateOf(0f) }
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
                    contentDescription = "Podgląd dokumentu",
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
                Text(
                    "Uszczypnij, aby powiększyć; przeciągnij, aby przesunąć.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
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
    resolver.openInputStream(document.uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    } ?: error("Nie można odczytać obrazu.")
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Nieprawidłowy plik obrazu." }

    var sampleSize = 1
    while (
        maxOf(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) >
        MAX_IMAGE_PREVIEW_DIMENSION
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return resolver.openInputStream(document.uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    } ?: error("Nie można zdekodować obrazu.")
}
