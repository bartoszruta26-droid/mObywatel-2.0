// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PfcProjectDocumentAssetTest {
    @Test
    fun bundledPfcDocumentContainsTheRequiredLegalSections() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val text = loadPfcProjectDocumentText(context)

        assertTrue(text.length > 140_000)
        assertTrue(text.contains("Poseł z Powołania"))
        assertTrue(text.contains("jednostek sektora finansów publicznych"))
        assertTrue(text.contains("art. 9 pkt 1"))
        assertTrue(text.contains("art. 29 ust. 3"))
        assertTrue(text.contains("art. 43"))
        assertTrue(text.contains("art. 47"))
        assertTrue(text.contains("Wzór dokumentu powierzenia środków"))
    }
}
