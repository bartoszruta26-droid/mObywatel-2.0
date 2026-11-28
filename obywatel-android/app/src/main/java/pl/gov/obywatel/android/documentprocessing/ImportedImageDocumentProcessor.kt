// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.content.ContentResolver
import android.net.Uri
import pl.gov.obywatel.android.DocumentImageRecognition

/**
 * Connects the existing SAF content URI to the shared document-processing engine.
 *
 * The processor owns the sampled bitmap created by [DocumentUriImageDecoder] and
 * releases it after the complete pipeline finishes, including cancellation or failure.
 * It never persists OCR output; extracted fields remain suggestions for user review.
 */
internal class ImportedImageDocumentProcessor(
    contentResolver: ContentResolver,
    recognition: DocumentImageRecognition = DocumentImageRecognition(),
    private val engine: DocumentProcessingEngine = DocumentProcessingEngine(
        textRecognizer = MlKitTextRecognizerEngine(recognition),
        barcodeReader = MlKitBarcodeReaderEngine(recognition),
        fieldExtractor = DocumentOcrFieldExtractor(),
        providerName = "ml-kit-on-device",
        onDevice = true
    )
) {
    private val decoder = DocumentUriImageDecoder(contentResolver)

    suspend fun process(
        uri: Uri,
        mimeType: String,
        displayName: String? = null,
        operations: Set<ProcessingOperation> = setOf(
            ProcessingOperation.OCR,
            ProcessingOperation.BARCODE_RECOGNITION
        ),
        documentTypeHint: String? = null
    ): ProcessingResult {
        require(operations.isNotEmpty()) { "Wybierz co najmniej jedną operację." }
        require(operations.all { it in SUPPORTED_OPERATIONS }) {
            "Wybrana operacja nie jest obsługiwana dla obrazu."
        }

        val source = DocumentSourceDescriptor(
            id = uri.toString(),
            mimeType = mimeType,
            displayName = displayName
        )
        val input = decoder.decode(source, uri)
        return try {
            engine.process(
                ProcessingRequest(
                    source = source,
                    operations = operations,
                    documentTypeHint = documentTypeHint
                ),
                input
            )
        } finally {
            if (!input.bitmap.isRecycled) input.bitmap.recycle()
        }
    }

    private companion object {
        val SUPPORTED_OPERATIONS = setOf(
            ProcessingOperation.OCR,
            ProcessingOperation.BARCODE_RECOGNITION,
            ProcessingOperation.FIELD_EXTRACTION
        )
    }
}
