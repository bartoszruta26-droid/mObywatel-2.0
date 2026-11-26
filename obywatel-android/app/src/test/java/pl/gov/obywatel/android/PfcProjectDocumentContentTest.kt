// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertTrue
import org.junit.Test

class PfcProjectDocumentContentTest {
    @Test
    fun consolidatedDocumentContainsTheRequiredLegalSections() {
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.length > 140_000)
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("Poseł z Powołania"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("jednostek sektora finansów publicznych"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("art. 9 pkt 1"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("art. 29 ust. 3"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("art. 43"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("art. 47"))
        assertTrue(PFC_PROJECT_DOCUMENT_TEXT.contains("Wzór dokumentu powierzenia środków"))
    }
}
