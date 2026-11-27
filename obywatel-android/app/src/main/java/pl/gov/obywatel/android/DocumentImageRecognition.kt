// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
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

    /**
     * Compatibility helper for callers that need only the concatenated text.
     * rotationDegrees is the clockwise rotation required to make the bitmap upright.
     */
    suspend fun recognizeText(bitmap: Bitmap, rotationDegrees: Int = 0): String =
        recognizeTextResult(bitmap, rotationDegrees).text

    /**
     * Returns the ML Kit hierarchy instead of discarding geometry and confidence.
     * Coordinates are in the input bitmap coordinate space.
     */
    suspend fun recognizeTextResult(
        bitmap: Bitmap,
        rotationDegrees: Int = 0
    ): RecognizedText {
        require(!bitmap.isRecycled) { "Nie można analizować zwolnionego obrazu." }
        require(rotationDegrees in setOf(0, 90, 180, 270)) {
            "Kąt obrotu musi wynosić 0, 90, 180 albo 270 stopni."
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val result = recognizer.process(InputImage.fromBitmap(bitmap, rotationDegrees)).await()
            RecognizedText(
                text = result.text,
                blocks = result.textBlocks.map { block ->
                    RecognizedTextBlock(
                        text = block.text,
                        bounds = block.boundingBox.toPlainBounds(),
                        lines = block.lines.map { line ->
                            RecognizedTextLine(
                                text = line.text,
                                confidence = line.confidence?.takeIf { it in 0f..1f },
                                bounds = line.boundingBox.toPlainBounds()
                            )
                        }
                    )
                }
            )
        } finally {
            recognizer.close()
        }
    }

    suspend fun recognizeBarcodes(
        bitmap: Bitmap,
        rotationDegrees: Int = 0
    ): List<RecognizedBarcode> {
        require(!bitmap.isRecycled) { "Nie można analizować zwolnionego obrazu." }
        require(rotationDegrees in setOf(0, 90, 180, 270)) {
            "Kąt obrotu musi wynosić 0, 90, 180 albo 270 stopni."
        }
        val scanner = BarcodeScanning.getClient()
        return try {
            scanner.process(InputImage.fromBitmap(bitmap, rotationDegrees)).await().mapNotNull { barcode ->
                val raw = barcode.rawValue ?: return@mapNotNull null
                if (raw.isBlank()) return@mapNotNull null
                RecognizedBarcode(
                    rawValue = raw,
                    displayValue = barcode.displayValue,
                    format = barcode.format,
                    valueType = barcode.valueType,
                    bounds = barcode.boundingBox.toPlainBounds()
                )
            }
        } finally {
            scanner.close()
        }
    }
}

private fun Rect?.toPlainBounds(): RecognizedImageBounds? = this?.let {
    RecognizedImageBounds(left = it.left, top = it.top, right = it.right, bottom = it.bottom)
}

internal data class RecognizedText(
    val text: String,
    val blocks: List<RecognizedTextBlock>
)

internal data class RecognizedTextBlock(
    val text: String,
    val bounds: RecognizedImageBounds?,
    val lines: List<RecognizedTextLine>
)

internal data class RecognizedTextLine(
    val text: String,
    val confidence: Float?,
    val bounds: RecognizedImageBounds?
)

internal data class RecognizedImageBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

/** Platform-neutral result shape for the application layer. */
internal data class RecognizedBarcode(
    val rawValue: String,
    val displayValue: String?,
    val format: Int,
    val valueType: Int,
    val bounds: RecognizedImageBounds?
)
