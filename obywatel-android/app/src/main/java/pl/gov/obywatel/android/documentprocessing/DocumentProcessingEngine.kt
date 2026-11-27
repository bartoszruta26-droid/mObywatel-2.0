// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

/**
 * Coordinates provider-neutral processing steps. Providers are injected so the
 * pipeline can be tested without Android, ML Kit, ZXing or a camera.
 *
 * The engine never persists data. Its caller owns source lifetime and must ask
 * the user to review extracted values before any document is updated.
 */
internal class DocumentProcessingEngine(
    private val textRecognizer: TextRecognizerEngine? = null,
    private val barcodeReader: BarcodeReaderEngine? = null,
    private val fieldExtractor: DocumentFieldExtractor? = null,
    private val fieldValidator: FieldValidator? = null,
    private val translator: TextTranslator? = null,
    private val providerName: String = "document-processing-engine",
    private val onDevice: Boolean = true,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun process(
        request: ProcessingRequest,
        input: ProcessingInput
    ): ProcessingResult {
        require(input.source == request.source) {
            "Wejście nie odpowiada źródłu wskazanemu w żądaniu."
        }

        val needsText = request.operations.any {
            it == ProcessingOperation.OCR ||
                it == ProcessingOperation.FIELD_EXTRACTION ||
                it == ProcessingOperation.TRANSLATION
        }
        val ocr = if (needsText) {
            requireNotNull(textRecognizer) { "Brak dostawcy OCR." }.recognize(input)
        } else null

        val barcodes = if (ProcessingOperation.BARCODE_RECOGNITION in request.operations) {
            requireNotNull(barcodeReader) { "Brak dostawcy odczytu kodów." }.read(input)
        } else emptyList()

        val fields = if (ProcessingOperation.FIELD_EXTRACTION in request.operations) {
            requireNotNull(fieldExtractor) { "Brak ekstraktora pól." }
                .extract(requireNotNull(ocr), request.documentTypeHint)
        } else emptyList()

        val validations = if (ProcessingOperation.FIELD_VALIDATION in request.operations) {
            requireNotNull(fieldValidator) { "Brak walidatora pól." }
                .validate(fields, request.documentTypeHint)
        } else emptyList()

        val translation = if (ProcessingOperation.TRANSLATION in request.operations) {
            requireNotNull(translator) { "Brak dostawcy tłumaczenia." }.translate(
                text = requireNotNull(ocr).text,
                sourceLanguage = request.sourceLanguage,
                targetLanguage = requireNotNull(request.targetLanguage)
            )
        } else null

        return ProcessingResult(
            ocr = if (ProcessingOperation.OCR in request.operations) ocr else null,
            barcodes = barcodes,
            fields = fields,
            validations = validations,
            translation = translation,
            provenance = ProcessingProvenance(
                sourceId = request.source.id,
                provider = providerName,
                processedAtEpochMillis = clock(),
                onDevice = onDevice
            )
        )
    }
}
