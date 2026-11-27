// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.common.Barcode
import pl.gov.obywatel.android.DocumentImageRecognition
import pl.gov.obywatel.android.RecognizedBarcode
import pl.gov.obywatel.android.RecognizedImageBounds
import pl.gov.obywatel.android.RecognizedText

/**
 * Android boundary object for an already-decoded bitmap. The caller retains
 * ownership of the bitmap; the processing adapter never recycles it.
 */
internal data class BitmapProcessingInput(
    override val source: DocumentSourceDescriptor,
    val bitmap: Bitmap,
    val rotationDegrees: Int = 0
) : ProcessingInput {
    init {
        require(!bitmap.isRecycled) { "Nie można analizować zwolnionego obrazu." }
        require(rotationDegrees in setOf(0, 90, 180, 270)) {
            "Kąt obrotu musi wynosić 0, 90, 180 albo 270 stopni."
        }
    }
}

/** Adapts ML Kit OCR results to the provider-neutral processing contract. */
internal class MlKitTextRecognizerEngine(
    private val recognition: DocumentImageRecognition = DocumentImageRecognition()
) : TextRecognizerEngine {
    override suspend fun recognize(input: ProcessingInput): OcrText {
        val bitmapInput = input.requireBitmapInput()
        return recognition.recognizeTextResult(
            bitmapInput.bitmap,
            bitmapInput.rotationDegrees
        ).toOcrText()
    }
}

/** Adapts ML Kit barcode results to the provider-neutral processing contract. */
internal class MlKitBarcodeReaderEngine(
    private val recognition: DocumentImageRecognition = DocumentImageRecognition()
) : BarcodeReaderEngine {
    override suspend fun read(input: ProcessingInput): List<BarcodeValue> {
        val bitmapInput = input.requireBitmapInput()
        return recognition.recognizeBarcodes(
            bitmapInput.bitmap,
            bitmapInput.rotationDegrees
        ).map(RecognizedBarcode::toBarcodeValue)
    }
}

/** Pure mapping functions are kept separate so their behavior is JVM-testable. */
internal fun RecognizedText.toOcrText(): OcrText = OcrText(
    text = text,
    blocks = blocks.map { block ->
        OcrBlock(
            lines = block.lines.map { line ->
                OcrLine(
                    text = line.text,
                    confidence = line.confidence,
                    bounds = line.bounds?.toTextBounds()
                )
            },
            bounds = block.bounds?.toTextBounds()
        )
    }
)

internal fun RecognizedBarcode.toBarcodeValue(): BarcodeValue = BarcodeValue(
    rawValue = rawValue,
    displayValue = displayValue,
    format = format.toBarcodeFormatName(),
    valueType = valueType.toBarcodeValueTypeName(),
    bounds = bounds?.toTextBounds()
)

private fun ProcessingInput.requireBitmapInput(): BitmapProcessingInput =
    this as? BitmapProcessingInput
        ?: throw IllegalArgumentException("Dostawca ML Kit wymaga wejścia BitmapProcessingInput.")

private fun RecognizedImageBounds.toTextBounds() =
    TextBounds(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

private fun Int.toBarcodeFormatName(): String = when (this) {
    Barcode.FORMAT_CODE_128 -> "CODE_128"
    Barcode.FORMAT_CODE_39 -> "CODE_39"
    Barcode.FORMAT_CODE_93 -> "CODE_93"
    Barcode.FORMAT_CODABAR -> "CODABAR"
    Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    Barcode.FORMAT_EAN_13 -> "EAN_13"
    Barcode.FORMAT_EAN_8 -> "EAN_8"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_QR_CODE -> "QR_CODE"
    Barcode.FORMAT_UPC_A -> "UPC_A"
    Barcode.FORMAT_UPC_E -> "UPC_E"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_AZTEC -> "AZTEC"
    else -> "UNKNOWN_$this"
}

private fun Int.toBarcodeValueTypeName(): String = when (this) {
    Barcode.TYPE_CONTACT_INFO -> "CONTACT_INFO"
    Barcode.TYPE_EMAIL -> "EMAIL"
    Barcode.TYPE_ISBN -> "ISBN"
    Barcode.TYPE_PHONE -> "PHONE"
    Barcode.TYPE_PRODUCT -> "PRODUCT"
    Barcode.TYPE_SMS -> "SMS"
    Barcode.TYPE_TEXT -> "TEXT"
    Barcode.TYPE_URL -> "URL"
    Barcode.TYPE_WIFI -> "WIFI"
    Barcode.TYPE_GEO -> "GEO"
    Barcode.TYPE_CALENDAR_EVENT -> "CALENDAR_EVENT"
    Barcode.TYPE_DRIVER_LICENSE -> "DRIVER_LICENSE"
    else -> "UNKNOWN_$this"
}
