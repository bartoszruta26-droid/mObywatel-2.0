// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaticanHeraldicAssetsTest {
    @Test
    fun vaticanAndPapalHeraldicReferencesArePackagedAndIndexed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expected = mapOf(
            "builtin_vatican_state_coat_of_arms_2023" to "institutions/vatican-city-state-coat-of-arms-2023.svg",
            "builtin_holy_see_emblem_reference" to "institutions/holy-see-emblem-heraldic-reference.svg",
            "builtin_pope_leo_xiv_coat_of_arms_reference" to "institutions/pope-leo-xiv-coat-of-arms-reference.svg",
            "builtin_holy_roman_church_historical_banner" to "institutions/banner-holy-roman-church-historical.svg"
        )
        val assets = DocumentBrandStore.list(context).associateBy { it.id }
        expected.forEach { (id, file) ->
            val asset = assets[id]
            assertNotNull("Brak zasobu $id w bibliotece", asset)
            assertTrue("Niepoprawny typ pliku $id", asset!!.file == "builtin:$file")
            val bytes = DocumentBrandStore.fileBytes(context, id)
            assertNotNull("Brak danych pliku $file", bytes)
            assertTrue("Pusty plik $file", bytes!!.isNotEmpty())
            assertTrue("Nie jest to SVG: $file", String(bytes, Charsets.UTF_8).contains("<svg"))
        }
    }

    @Test
    fun referenceOnlyHeraldicAssetsAreNotRenderedAsInstitutionalMarks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assets = DocumentBrandStore.list(context).associateBy { it.id }
        val ids = listOf(
            "builtin_vatican_state_coat_of_arms_2023",
            "builtin_holy_see_emblem_reference",
            "builtin_pope_leo_xiv_coat_of_arms_reference",
            "builtin_holy_roman_church_historical_banner"
        )
        ids.forEach { id ->
            val asset = assets[id]
            assertNotNull(asset)
            assertTrue(asset!!.rights.isNotBlank())
            assertFalse(asset.rights.contains("\"permittedKinds\":[\"Herb\"]"))
            assertFalse(asset.rights.contains("\"permittedKinds\":[\"Godło\"]"))
            assertFalse(asset.rights.contains("\"permittedKinds\":[\"Flaga\"]"))
        }
        assertNull(DocumentBrandStore.authorizedImageForDocument(context, "church", "Państwo Watykańskie", "Herb"))
        assertNull(DocumentBrandStore.authorizedImageForDocument(context, "church", "Stolica Apostolska", "Godło"))
        assertNull(DocumentBrandStore.authorizedImageForDocument(context, "church", "Papież Leon XIV", "Herb"))
    }
}
