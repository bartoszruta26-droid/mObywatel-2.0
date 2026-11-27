// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Thin on-device adapter for image text and barcode recognition.
 *
 * Recognition results are suggestions only. Callers must show them for user
 * confirmation before writing values into an identity or other document.
 * No image or recognized text is uploaded by this adapter.
 */
internal class DocumentImageRecognition {

    suspend fun recognizeText(bitmap: Bitmap): String {
        require(!bitmap.isRecycled) { "Nie można analizować zwolnionego obrazu." }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text
        } finally {
            recognizer.close()
        }
    }

    suspend fun recognizeBarcodes(bitmap: Bitmap): List<RecognizedBarcode> {
        require(!bitmap.isRecycled) { "Nie można analizować zwolnionego obrazu." }
        val scanner = BarcodeScanning.getClient()
        return try {
            scanner.process(InputImage.fromBitmap(bitmap, 0)).await().mapNotNull { barcode ->
                val raw = barcode.rawValue ?: return@mapNotNull null
                if (raw.isBlank()) return@mapNotNull null
                RecognizedBarcode(
                    rawValue = raw,
                    displayValue = barcode.displayValue,
                    format = barcode.format,
                    valueType = barcode.valueType
                )
            }
        } finally {
            scanner.close()
        }
    }
}

/** Platform-neutral result shape for the application layer. */
internal data class RecognizedBarcode(
    val rawValue: String,
    val displayValue: String?,
    val format: Int,
    val valueType: Int
)
