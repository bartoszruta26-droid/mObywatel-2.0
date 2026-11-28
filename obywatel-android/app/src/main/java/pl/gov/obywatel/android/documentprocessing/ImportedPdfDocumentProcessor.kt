// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import pl.gov.obywatel.android.DocumentImageRecognition
import kotlin.math.min

/** Result for one PDF page; a failed page does not discard successful pages. */
internal data class PdfPageProcessingResult(
    val pageNumber: Int,
    val result: ProcessingResult? = null,
    val error: String? = null
)

/**
 * Renders and recognizes PDF pages sequentially. Only one page bitmap exists at
 * a time; the caller receives text/metadata, never a bitmap or persisted data.
 */
internal class ImportedPdfDocumentProcessor(
    private val contentResolver: ContentResolver,
    recognition: DocumentImageRecognition = DocumentImageRecognition(),
    private val maxDimension: Int = DEFAULT_MAX_DIMENSION
) {
    private val engine = DocumentProcessingEngine(
        textRecognizer = MlKitTextRecognizerEngine(recognition),
        barcodeReader = MlKitBarcodeReaderEngine(recognition),
        fieldExtractor = DocumentOcrFieldExtractor(),
        providerName = "ml-kit-on-device",
        onDevice = true
    )

    init {
        require(maxDimension > 0) { "Maksymalny wymiar strony musi być dodatni." }
    }

    suspend fun process(
        uri: Uri,
        displayName: String?,
        pageNumbers: IntRange,
        onPageCompleted: suspend (completed: Int, total: Int, result: PdfPageProcessingResult) -> Unit
    ) {
        require(uri.scheme == "content") { "PDF musi pochodzić z istniejącego importu SAF." }
        require(!pageNumbers.isEmpty()) { "Nie wybrano stron do rozpoznania." }

        val descriptor = contentResolver.openFileDescriptor(uri, "r")
            ?: error("Nie można otworzyć pliku PDF. Sprawdź dostęp do zaimportowanego dokumentu.")
        descriptor.use { pfd ->
            val renderer = try {
                PdfRenderer(pfd)
            } catch (failure: Exception) {
                throw IllegalArgumentException(
                    "Nie można otworzyć PDF. Plik może być uszkodzony, zaszyfrowany hasłem lub niedostępny.",
                    failure
                )
            }
            renderer.use { pdf ->
                require(pdf.pageCount > 0) { "Plik PDF nie zawiera stron." }
                require(pageNumbers.first >= 0 && pageNumbers.last < pdf.pageCount) {
                    "Wybrany zakres stron wykracza poza dokument."
                }
                val total = pageNumbers.count()
                var completed = 0
                for (pageIndex in pageNumbers) {
                    currentCoroutineContext().ensureActive()
                    val pageResult = try {
                        pdf.openPage(pageIndex).use { page ->
                            val dimensions = boundedDimensions(page.width, page.height, maxDimension)
                            val bitmap = Bitmap.createBitmap(
                                dimensions.first, dimensions.second, Bitmap.Config.ARGB_8888
                            )
                            try {
                                bitmap.eraseColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                val source = DocumentSourceDescriptor(
                                    id = "${uri}#page=${pageIndex + 1}",
                                    mimeType = "application/pdf",
                                    displayName = displayName,
                                    pageCount = pdf.pageCount
                                )
                                val input = BitmapProcessingInput(source, bitmap)
                                val processed = engine.process(
                                    ProcessingRequest(
                                        source = source,
                                        operations = setOf(
                                            ProcessingOperation.OCR,
                                            ProcessingOperation.BARCODE_RECOGNITION,
                                            ProcessingOperation.FIELD_EXTRACTION
                                        )
                                    ),
                                    input
                                )
                                PdfPageProcessingResult(pageIndex + 1, processed)
                            } finally {
                                if (!bitmap.isRecycled) bitmap.recycle()
                            }
                        }
                    } catch (cancelled: kotlinx.coroutines.CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        PdfPageProcessingResult(
                            pageNumber = pageIndex + 1,
                            error = failure.message ?: "Nie udało się przetworzyć strony."
                        )
                    }
                    completed++
                    onPageCompleted(completed, total, pageResult)
                }
            }
        }
    }

    private fun boundedDimensions(width: Int, height: Int, limit: Int): Pair<Int, Int> {
        require(width > 0 && height > 0) { "Strona PDF ma nieprawidłowe wymiary." }
        val scale = min(1f, limit.toFloat() / maxOf(width, height).toFloat())
        return (width * scale).toInt().coerceAtLeast(1) to
            (height * scale).toInt().coerceAtLeast(1)
    }

    private companion object {
        const val DEFAULT_MAX_DIMENSION = 1800
    }
}
