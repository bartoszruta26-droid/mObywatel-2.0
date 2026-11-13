// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentRouteRegistryTest {
    @Test
    fun everyDocumentVisibilityTileHasAnExplicitRoute() {
        val visibleTileIds = setOf(
            "profile", "identity", "passport", "registry", "family", "parliament", "training",
            "student", "disability", "church", "civil", "business", "bank_accounts", "vaccines",
            "signed_pdf", "imported", "children", "branding", "loyalty", "birth_extract",
            "marriage_extract", "death_extract", "large_family", "blood_donor", "disability_card",
            "vehicle_registration", "insurance_vehicle", "insurance_professional", "insurance_private",
            "insurance_business", "criminal_record", "mission_travel", "ehic", "eu_education", "military_book", "martial_arts",
            "shooting_sport", "archery", "tactical_training", "survival_training", "security_guard", "defense_training", "historical_reenactment", "reenactment_qualification", "reenactment_event",
            "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister",
            "church_missionary", "professional", "professional_medical", "professional_education",
            "professional_therapeutic", "professional_other", "organization_foundation",
            "organization_church", "organization_company", "organization_association", "entity", "official"
        )

        assertEquals(visibleTileIds, DocumentRouteRegistry.routes.keys - setOf("identity_card", "loyalty_cards"))
        visibleTileIds.forEach { id -> assertNotNull("Missing route for $id", DocumentRouteRegistry.find(id)) }
    }

    @Test
    fun concreteDocumentTilesKeepTheirOwnIdsForDirectFormEditing() {
        val concreteDocumentIds = setOf(
            "birth_extract", "marriage_extract", "death_extract", "insurance_vehicle",
            "insurance_professional", "insurance_private", "insurance_business",
            "vehicle_registration", "criminal_record", "mission_travel", "ehic", "eu_education",
            "military_book", "martial_arts", "shooting_sport", "archery", "tactical_training",
            "survival_training", "security_guard", "defense_training",
            "large_family", "blood_donor", "disability_card", "church_bishop", "church_archbishop",
            "church_cardinal", "church_abbot", "church_minister", "church_missionary",
            "professional_medical", "professional_education", "professional_therapeutic",
            "professional_other"
        )

        concreteDocumentIds.forEach { id ->
            val route = requireNotNull(DocumentRouteRegistry.find(id))
            assertEquals("$id must open its own document", DocumentRouteType.DOCUMENT, route.type)
            assertEquals("$id must preserve its identifier", id, route.documentId)
            assertNull("$id must not point to a group/specialized destination", route.destination)
        }
    }

    @Test
    fun armedServiceDocumentsBelongToTheirOwnGroupAndHaveDedicatedFields() {
        val ids = listOf(
            "military_book", "martial_arts", "shooting_sport", "archery",
            "tactical_training", "survival_training", "security_guard", "defense_training"
        )
        assertTrue("Służba zbrojna" in documentVisibilityGroupOrder)
        ids.forEach { id ->
            assertEquals("Służba zbrojna", documentVisibilityGroup(id))
            assertTrue("$id must have a tailored schema", documentEditorFieldDefinitions(id).isNotEmpty())
            assertEquals("$id must open its own preview", DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find(id)?.type)
        }
    }

    @Test
    fun genericChurchTitleTilesHaveDedicatedSchemaBackedFormFields() {
        listOf("church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister", "church_missionary")
            .forEach { id ->
                val definitions = documentEditorFieldDefinitions(id)
                assertNotNull("Missing schema for $id", definitions)
                org.junit.Assert.assertTrue("$id must expose form fields", definitions.isNotEmpty())
            }
    }

    @Test
    fun collectionTilesAreExplicitlyGroups() {
        val expectedGroups = mapOf(
            "church" to "Dokumenty Kościelne",
            "civil" to "Formularze dokumentów",
            "business" to "Dokumenty rejestru podmiotów",
            "professional" to "Dokumenty kwalifikacji zawodowych",
            "organization_foundation" to "Dokumenty rejestru podmiotów",
            "organization_church" to "Dokumenty Kościelne",
            "organization_company" to "Dokumenty rejestru podmiotów",
            "organization_association" to "Dokumenty rejestru podmiotów",
            "entity" to "Dokumenty rejestru podmiotów",
            "official" to "Formularze dokumentów"
        )

        expectedGroups.forEach { (id, destination) ->
            val route = requireNotNull(DocumentRouteRegistry.find(id))
            assertEquals(id, DocumentRouteType.GROUP, route.type)
            assertEquals(id, destination, route.destination)
        }
    }

    @Test
    fun featureScreensAreExplicitlySpecializedAndLegacyAliasesRemainCompatible() {
        listOf("registry", "bank_accounts", "vaccines", "signed_pdf", "imported", "branding", "children")
            .forEach { id ->
                assertEquals(id, DocumentRouteType.SPECIALIZED, DocumentRouteRegistry.find(id)?.type)
            }

        assertEquals("identity", DocumentRouteRegistry.find("identity_card")?.documentId)
        assertEquals("loyalty", DocumentRouteRegistry.find("loyalty_cards")?.documentId)
        assertEquals(DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find("identity_card")?.type)
        assertEquals(DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find("loyalty_cards")?.type)
    }

    @Test
    fun unknownIdsDoNotSilentlyBecomeGroupRoutes() {
        assertNull(DocumentRouteRegistry.find("unknown_document_id"))
    }
}
