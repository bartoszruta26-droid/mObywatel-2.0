// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

/**
 * Platform-neutral contracts for document processing.
 *
 * This file deliberately has no Android, ML Kit, ZXing or Compose imports.
 * Provider adapters and Android URI/Bitmap handling belong in separate files.
 */

enum class ProcessingOperation {
    OCR,
    BARCODE_RECOGNITION,
    FIELD_EXTRACTION,
    FIELD_VALIDATION,
    TRANSLATION
}

data class DocumentSourceDescriptor(
    val id: String,
    val mimeType: String,
    val displayName: String? = null,
    val pageCount: Int? = null
) {
    init {
        require(id.isNotBlank()) { "Source id must not be blank." }
        require(mimeType.isNotBlank()) { "MIME type must not be blank." }
        require(pageCount == null || pageCount > 0) { "Page count must be positive." }
    }
}

data class ProcessingRequest(
    val source: DocumentSourceDescriptor,
    val operations: Set<ProcessingOperation>,
    val documentTypeHint: String? = null,
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null
) {
    init {
        require(operations.isNotEmpty()) { "At least one processing operation is required." }
        require(
            ProcessingOperation.TRANSLATION !in operations ||
                (!targetLanguage.isNullOrBlank())
        ) { "Target language is required for translation." }
        require(
            ProcessingOperation.FIELD_VALIDATION !in operations ||
                ProcessingOperation.FIELD_EXTRACTION in operations
        ) { "Field validation requires field extraction in the same request." }
    }
}

data class TextBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    init {
        require(left <= right && top <= bottom) { "Text bounds are invalid." }
    }
}

data class OcrLine(
    val text: String,
    val confidence: Float? = null,
    val bounds: TextBounds? = null
) {
    init {
        require(text.isNotBlank()) { "OCR line must not be blank." }
        require(confidence == null || confidence in 0f..1f) {
            "OCR confidence must be between 0 and 1."
        }
    }
}

data class OcrBlock(
    val lines: List<OcrLine>,
    val bounds: TextBounds? = null
) {
    init {
        require(lines.isNotEmpty()) { "OCR block must contain at least one line." }
    }
}

data class OcrText(
    val text: String,
    val blocks: List<OcrBlock> = emptyList(),
    val languageTag: String? = null
)

data class BarcodeValue(
    val rawValue: String,
    val displayValue: String? = null,
    val format: String,
    val valueType: String? = null,
    val bounds: TextBounds? = null
) {
    init {
        require(rawValue.isNotBlank()) { "Barcode raw value must not be blank." }
        require(format.isNotBlank()) { "Barcode format must not be blank." }
    }
}

data class ExtractedField(
    val key: String,
    val value: String,
    val confidence: Float? = null,
    val sourceBounds: TextBounds? = null,
    val requiresReview: Boolean = true
) {
    init {
        require(key.isNotBlank()) { "Field key must not be blank." }
        require(confidence == null || confidence in 0f..1f) {
            "Field confidence must be between 0 and 1."
        }
    }
}

enum class ValidationStatus { VALID, INVALID, NEEDS_REVIEW, NOT_CHECKED }

data class FieldValidation(
    val fieldKey: String,
    val status: ValidationStatus,
    val message: String? = null
) {
    init {
        require(fieldKey.isNotBlank()) { "Validation field key must not be blank." }
    }
}

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String? = null,
    val targetLanguage: String
) {
    init {
        require(targetLanguage.isNotBlank()) { "Target language must not be blank." }
    }
}

data class ProcessingProvenance(
    val sourceId: String,
    val provider: String,
    val processedAtEpochMillis: Long,
    val onDevice: Boolean
) {
    init {
        require(sourceId.isNotBlank()) { "Provenance source id must not be blank." }
        require(provider.isNotBlank()) { "Provider must not be blank." }
        require(processedAtEpochMillis >= 0L) { "Processing timestamp must not be negative." }
    }
}

data class ProcessingResult(
    val ocr: OcrText? = null,
    val barcodes: List<BarcodeValue> = emptyList(),
    val fields: List<ExtractedField> = emptyList(),
    val validations: List<FieldValidation> = emptyList(),
    val translation: TranslationResult? = null,
    val provenance: ProcessingProvenance
)

/** Provider-neutral interfaces. Implementations own and close their native resources. */
fun interface TextRecognizerEngine {
    suspend fun recognize(input: ProcessingInput): OcrText
}

fun interface BarcodeReaderEngine {
    suspend fun read(input: ProcessingInput): List<BarcodeValue>
}

fun interface TextTranslator {
    suspend fun translate(text: String, sourceLanguage: String?, targetLanguage: String): TranslationResult
}

fun interface DocumentFieldExtractor {
    fun extract(text: OcrText, documentTypeHint: String?): List<ExtractedField>
}

fun interface FieldValidator {
    fun validate(fields: List<ExtractedField>, documentTypeHint: String?): List<FieldValidation>
}

/**
 * Opaque input supplied by the platform layer. The core never opens Android
 * URIs or owns caller-managed files; adapters must not retain it after return.
 */
interface ProcessingInput {
    val source: DocumentSourceDescriptor
}
