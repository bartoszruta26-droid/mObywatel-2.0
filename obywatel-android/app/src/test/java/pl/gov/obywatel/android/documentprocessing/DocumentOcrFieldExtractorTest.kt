// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentOcrFieldExtractorTest {

    private val extractor = DocumentOcrFieldExtractor()

    @Test
    fun mapsExistingParserSuggestionsToReviewableEngineFields() {
        val fields = extractor.extract(
            OcrText(
                text = "IMIONA: JAN ADAM\nNAZWISKO: KOWALSKI\nPESEL: 80031212345"
            ),
            "identity_card"
        )

        assertEquals("JAN ADAM", fields.first { it.key == "firstNames" }.value)
        assertEquals("KOWALSKI", fields.first { it.key == "lastName" }.value)
        assertTrue(fields.all { it.requiresReview })
        assertTrue(fields.all { it.confidence == null })
    }

    @Test
    fun returnsNoSuggestionsWithoutDocumentTypeHint() {
        assertTrue(extractor.extract(OcrText("NAZWISKO: KOWALSKI"), null).isEmpty())
        assertTrue(extractor.extract(OcrText("NAZWISKO: KOWALSKI"), " ").isEmpty())
    }

    @Test
    fun returnsNoSuggestionsForBlankOcrText() {
        assertTrue(extractor.extract(OcrText("  "), "identity_card").isEmpty())
    }
}
