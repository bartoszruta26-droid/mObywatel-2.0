// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentRouteRegistryTest {
    @Test
    fun everyDocumentVisibilityTileHasAnExplicitRoute() {
        val visibleTileIds = setOf(
            "profile", "identity", "passport", "registry", "family", "parliament", "training",
            "student", "disability", "church", "civil", "business", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts", "vaccines",
            "signed_pdf", "imported", "children", "branding", "loyalty", "payment_transactions", "birth_extract",
            "marriage_extract", "death_extract", "large_family", "blood_donor", "disability_card",
            "vehicle_registration", "insurance_vehicle", "insurance_professional", "insurance_private",
            "insurance_business", "criminal_record", "mission_travel", "ehic", "eu_education", "military_book", "martial_arts",
            "shooting_sport", "archery", "tactical_training", "survival_training", "security_guard", "defense_training", "historical_reenactment", "reenactment_qualification", "reenactment_event",
            "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister",
            "church_missionary", "professional", "professional_medical", "professional_education",
            "professional_therapeutic", "professional_other", "organization_foundation",
            "organization_church", "organization_company", "organization_association", "entity", "official"
        )

        val catalogTileIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue(
            "Every live visibility tile must have an explicit route.",
            DocumentRouteRegistry.routes.keys.containsAll(catalogTileIds)
        )
        assertTrue(
            "The established document tiles must remain in the live catalog.",
            catalogTileIds.containsAll(visibleTileIds)
        )
        visibleTileIds.forEach { id -> assertNotNull("Missing route for $id", DocumentRouteRegistry.find(id)) }
    }

    @Test
    fun concreteDocumentTilesKeepTheirOwnIdsForDirectFormEditing() {
        val concreteDocumentIds = setOf(
            "birth_extract", "marriage_extract", "death_extract", "insurance_vehicle",
            "insurance_professional", "insurance_private", "insurance_business",
            "vehicle_registration", "criminal_record", "mission_travel", "ehic", "eu_education",
            "military_book", "martial_arts", "shooting_sport", "archery", "tactical_training",
            "survival_training", "security_guard", "defense_training", "historical_reenactment",
            "reenactment_qualification", "reenactment_event",
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
            "business" to "Służba gospodarcza",
            "professional" to "Dokumenty kwalifikacji zawodowych",
            "organization_foundation" to "Służba fundacyjna",
            "organization_church" to "Dokumenty Kościelne",
            "organization_company" to "Służba gospodarcza",
            "organization_association" to "Służba stowarzyszeniowa",
            "entity" to "Służba gospodarcza",
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
        listOf(
            "registry", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts",
            "vaccines", "signed_pdf", "imported", "branding", "children", "payment_transactions"
        ).forEach { id ->
            val route = requireNotNull(DocumentRouteRegistry.find(id))
            assertEquals(id, DocumentRouteType.SPECIALIZED, route.type)
            if (id == "rod_bank_accounts" || id == "entity_bank_accounts") {
                assertEquals("The specialized route must preserve the selected bank register.", id, route.destination)
            }
        }

        assertEquals("identity", DocumentRouteRegistry.find("identity_card")?.documentId)
        assertEquals("loyalty", DocumentRouteRegistry.find("loyalty_cards")?.documentId)
        assertEquals(DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find("identity_card")?.type)
        assertEquals(DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find("loyalty_cards")?.type)
    }

    @Test
    fun paymentLedgerIsASpecializedFinancialServiceAndIsVisibleByDefault() {
        val route = requireNotNull(DocumentRouteRegistry.find("payment_transactions"))
        assertEquals(DocumentRouteType.SPECIALIZED, route.type)
        assertEquals("payment_transactions", route.destination)
        assertEquals("Służba finansowa", documentVisibilityGroup("payment_transactions"))
        assertEquals("dashboard", defaultDocumentPlacements["payment_transactions"])
    }

    @Test
    fun unknownIdsDoNotSilentlyBecomeGroupRoutes() {
        assertNull(DocumentRouteRegistry.find("unknown_document_id"))
    }
}
