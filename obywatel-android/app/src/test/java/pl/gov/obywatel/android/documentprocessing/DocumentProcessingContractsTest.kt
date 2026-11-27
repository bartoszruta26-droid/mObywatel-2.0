// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentProcessingContractsTest {

    private val source = DocumentSourceDescriptor(
        id = "fixture:identity-card-front",
        mimeType = "image/png",
        displayName = "identity-front.png"
    )

    @Test
    fun requestRequiresAtLeastOneOperation() {
        val error = runCatching { ProcessingRequest(source, emptySet()) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun translationRequestRequiresTargetLanguage() {
        val error = runCatching {
            ProcessingRequest(source, setOf(ProcessingOperation.TRANSLATION))
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun fieldValidationRequiresFieldExtraction() {
        val error = runCatching {
            ProcessingRequest(source, setOf(ProcessingOperation.FIELD_VALIDATION))
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun translationRequestAcceptsTargetLanguage() {
        val request = ProcessingRequest(
            source = source,
            operations = setOf(ProcessingOperation.TRANSLATION),
            sourceLanguage = "pl",
            targetLanguage = "en"
        )
        assertEquals("en", request.targetLanguage)
    }

    @Test
    fun confidenceMustBeWithinZeroAndOne() {
        assertTrue(runCatching { OcrLine("text", confidence = -0.1f) }.isFailure)
        assertTrue(runCatching { OcrLine("text", confidence = 1.1f) }.isFailure)
        assertEquals(0.85f, OcrLine("text", confidence = 0.85f).confidence)
    }

    @Test
    fun boundsMustNotBeInverted() {
        assertTrue(runCatching { TextBounds(10f, 0f, 5f, 10f) }.isFailure)
        assertEquals(5f, TextBounds(0f, 0f, 5f, 10f).right)
    }

    @Test
    fun extractedFieldsRequireReviewByDefault() {
        val field = ExtractedField(key = "lastName", value = "KOWALSKI")
        assertTrue(field.requiresReview)
        assertEquals("lastName", field.key)
    }

    @Test
    fun validationStatusIsExplicitAndDoesNotImplyAuthenticity() {
        val result = FieldValidation("pesel", ValidationStatus.NEEDS_REVIEW, "Sprawdź dane")
        assertEquals(ValidationStatus.NEEDS_REVIEW, result.status)
        assertFalse(result.status == ValidationStatus.VALID)
    }

    @Test
    fun provenanceRequiresNamedProviderAndSource() {
        assertTrue(
            runCatching {
                ProcessingProvenance(sourceId = "", provider = "ML Kit", processedAtEpochMillis = 1L, onDevice = true)
            }.isFailure
        )
        val provenance = ProcessingProvenance(
            sourceId = source.id,
            provider = "test",
            processedAtEpochMillis = 1L,
            onDevice = true
        )
        assertTrue(provenance.onDevice)
    }
}
