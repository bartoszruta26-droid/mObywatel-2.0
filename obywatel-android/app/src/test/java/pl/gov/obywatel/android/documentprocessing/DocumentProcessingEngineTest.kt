// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentProcessingEngineTest {
    private val source = DocumentSourceDescriptor("fixture:image", "image/png")

    private class Input(override val source: DocumentSourceDescriptor) : ProcessingInput

    @Test
    fun extractsAndValidatesFieldsAfterOcr() = runBlocking {
        val engine = DocumentProcessingEngine(
            textRecognizer = TextRecognizerEngine { OcrText("NAZWISKO: KOWALSKI") },
            fieldExtractor = DocumentFieldExtractor { _, _ ->
                listOf(ExtractedField("lastName", "KOWALSKI"))
            },
            fieldValidator = FieldValidator { fields, _ ->
                fields.map { FieldValidation(it.key, ValidationStatus.NEEDS_REVIEW) }
            },
            clock = { 123L }
        )
        val result = engine.process(
            ProcessingRequest(
                source,
                setOf(
                    ProcessingOperation.FIELD_EXTRACTION,
                    ProcessingOperation.FIELD_VALIDATION
                ),
                documentTypeHint = "identity_card"
            ),
            Input(source)
        )
        assertEquals("KOWALSKI", result.fields.single().value)
        assertTrue(result.fields.single().requiresReview)
        assertEquals(ValidationStatus.NEEDS_REVIEW, result.validations.single().status)
        assertNull(result.ocr)
        assertEquals(123L, result.provenance.processedAtEpochMillis)
    }

    @Test
    fun barcodeOnlyRequestDoesNotInvokeOcr() = runBlocking {
        var ocrCalled = false
        val engine = DocumentProcessingEngine(
            textRecognizer = TextRecognizerEngine {
                ocrCalled = true
                OcrText("unexpected")
            },
            barcodeReader = BarcodeReaderEngine {
                listOf(BarcodeValue("123", format = "QR_CODE"))
            }
        )
        val result = engine.process(
            ProcessingRequest(source, setOf(ProcessingOperation.BARCODE_RECOGNITION)),
            Input(source)
        )
        assertTrue(!ocrCalled)
        assertEquals("123", result.barcodes.single().rawValue)
        assertNull(result.ocr)
    }

    @Test
    fun translationUsesOcrTextAndRequestedTargetLanguage() = runBlocking {
        var translatedInput = ""
        var target = ""
        val engine = DocumentProcessingEngine(
            textRecognizer = TextRecognizerEngine { OcrText("tekst źródłowy") },
            translator = TextTranslator { text, _, targetLanguage ->
                translatedInput = text
                target = targetLanguage
                TranslationResult(text, "translated", "pl", targetLanguage)
            }
        )
        val result = engine.process(
            ProcessingRequest(
                source,
                setOf(ProcessingOperation.TRANSLATION),
                sourceLanguage = "pl",
                targetLanguage = "en"
            ),
            Input(source)
        )
        assertEquals("tekst źródłowy", translatedInput)
        assertEquals("en", target)
        assertEquals("translated", result.translation?.translatedText)
    }

    @Test
    fun rejectsMismatchedInputSource() = runBlocking {
        val engine = DocumentProcessingEngine()
        val error = runCatching {
            engine.process(
                ProcessingRequest(source, setOf(ProcessingOperation.OCR)),
                Input(DocumentSourceDescriptor("other", "image/png"))
            )
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
