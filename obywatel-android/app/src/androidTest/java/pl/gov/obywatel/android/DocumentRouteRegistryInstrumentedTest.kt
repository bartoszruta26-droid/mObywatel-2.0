// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on the Android emulator so the document-route contract is checked in
 * the same Android runtime used by the application.
 *
 * Both home sections call MainActivity.openDocumentCard, therefore the
 * registry contract below is shared by "Dokumenty" and "Inne dokumenty".
 */
@RunWith(AndroidJUnit4::class)
class DocumentRouteRegistryInstrumentedTest {
    private val individualDocumentIds = listOf(
        "birth_extract",
        "marriage_extract",
        "death_extract",
        "insurance_vehicle",
        "insurance_professional",
        "insurance_private",
        "insurance_business",
        "vehicle_registration",
        "criminal_record",
        "mission_travel",
        "ehic",
        "eu_education",
        "large_family",
        "blood_donor",
        "disability_card",
        "church_bishop",
        "church_archbishop",
        "church_cardinal",
        "church_abbot",
        "church_minister",
        "church_missionary",
        "professional_medical",
        "professional_education",
        "professional_therapeutic",
        "professional_other"
    )

    @Test
    fun allTwentyFiveIndividualTilesResolveToTheirOwnDocumentPreview() {
        individualDocumentIds.forEach { id ->
            val route = DocumentRouteRegistry.find(id)
            assertNotNull("Missing route for $id", route)
            assertEquals("Wrong route type for $id", DocumentRouteType.DOCUMENT, route!!.type)
            assertEquals("Tile ID was not preserved for $id", id, route.documentId)
            assertNull("Individual document $id must not redirect to a group", route.destination)
        }
        assertEquals(25, individualDocumentIds.size)
    }

    @Test
    fun sampleTilesFromBothHomeSectionsUseTheSameExplicitRegistry() {
        // Representative IDs assigned to the two home sections by visibility settings.
        val dokumentySection = listOf("church_bishop", "birth_extract", "professional_medical")
        val inneDokumentySection = listOf("church_missionary", "vehicle_registration", "professional_other")

        (dokumentySection + inneDokumentySection).forEach { id ->
            assertNotNull("No shared route for home tile $id", DocumentRouteRegistry.find(id))
            assertSame(
                "Both home sections must resolve a tile through the same registry",
                DocumentRouteRegistry.find(id),
                DocumentRouteRegistry.routes[id]
            )
        }
        assertTrue(dokumentySection.all { DocumentRouteRegistry.find(it)?.type == DocumentRouteType.DOCUMENT })
        assertTrue(inneDokumentySection.all { DocumentRouteRegistry.find(it)?.type == DocumentRouteType.DOCUMENT })
    }
}
