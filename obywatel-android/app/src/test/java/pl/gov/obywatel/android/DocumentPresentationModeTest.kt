// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentPresentationModeTest {
    @Test
    fun missingOrUnknownPreferenceKeepsLegitimacyAsDefault() {
        assertEquals(DocumentPresentationMode.LEGITIMACY, DocumentPresentationMode.fromStorage(null))
        assertEquals(DocumentPresentationMode.LEGITIMACY, DocumentPresentationMode.fromStorage(""))
        assertEquals(DocumentPresentationMode.LEGITIMACY, DocumentPresentationMode.fromStorage("unknown"))
    }

    @Test
    fun everySupportedPresentationModeHasStableStorageValue() {
        assertEquals(DocumentPresentationMode.LEGITIMACY, DocumentPresentationMode.fromStorage("legitimacy"))
        assertEquals(DocumentPresentationMode.CERTIFICATE, DocumentPresentationMode.fromStorage("certificate"))
        assertEquals(DocumentPresentationMode.CSV, DocumentPresentationMode.fromStorage("csv"))
    }
}
