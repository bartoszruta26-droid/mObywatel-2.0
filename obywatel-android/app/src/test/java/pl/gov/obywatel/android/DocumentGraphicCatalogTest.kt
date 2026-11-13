// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentGraphicCatalogTest {
    @Test
    fun graphicAssetLibraryHasDedicatedVectorTileAndHologramGroups() {
        val kinds = DocumentBrandStore.assetKinds()
        assertTrue("Vector tile backgrounds need their own asset group",
            "Grafika wektorowa tła kafelków" in kinds)
        assertTrue("Vector tile icons need their own asset group",
            "Ikona wektorowa kafelka" in kinds)
        assertTrue("Animated holograms need their own asset group",
            "Animowany hologram" in kinds)
    }

    @Test
    fun allAuditedDocumentsHaveUniqueIndependentDefinitions() {
        val expected = setOf(
            "registry", "birth_extract", "marriage_extract", "death_extract", "large_family",
            "blood_donor", "disability_card", "vehicle_registration", "insurance_vehicle",
            "insurance_professional", "insurance_private", "insurance_business", "criminal_record",
            "mission_travel", "ehic", "eu_education", "church_bishop", "church_archbishop",
            "church_cardinal", "church_abbot", "church_minister", "church_missionary",
            "professional_medical", "professional_education", "professional_therapeutic",
            "professional_other", "organization_foundation", "organization_church",
            "organization_company", "organization_association", "official", "loyalty_cards", "military_book", "martial_arts", "shooting_sport", "archery",
            "tactical_training", "survival_training", "security_guard", "defense_training",
            "historical_reenactment", "reenactment_qualification", "reenactment_event"
        )
        val definitions = GraphicDocumentCatalog.definitions
        assertTrue(definitions.map { it.id }.toSet().containsAll(expected))
        assertTrue(documentBrandTargets.all { target -> GraphicDocumentCatalog.find(target.first) != null })
        assertEquals(definitions.size, definitions.map { it.id }.toSet().size)
        definitions.forEach { definition ->
            assertNotNull(definition.title.takeIf { it.isNotBlank() })
            assertNotNull(credentialFieldOptions(definition.id).takeIf { it.isNotEmpty() })
        }
    }

    @Test
    fun previouslyMissingDocumentAndSubdocumentTypesAreAvailable() {
        val expected = setOf(
            "identity", "passport", "parliament", "training", "student", "family",
            "professional", "professional_medical", "professional_education",
            "professional_therapeutic", "professional_other", "church",
            "church_bishop", "church_archbishop", "church_cardinal", "church_abbot",
            "church_minister", "church_missionary", "organization_foundation",
            "organization_church", "organization_company", "organization_association",
            "insurance_vehicle", "insurance_professional", "insurance_private",
            "insurance_business", "birth_extract", "marriage_extract", "death_extract"
        )
        expected.forEach { id -> assertNotNull(GraphicDocumentCatalog.find(id)) }
    }

    @Test
    fun everyNestedChurchFamilyEntityAndOfficialDocumentHasItsOwnGraphicEditor() {
        val nestedTargets = churchGraphicDocumentTargets() +
            familyEntityGraphicDocumentTargets() + officialGraphicDocumentTargets()
        assertTrue("Nested documents must be included in the graphic catalog",
            nestedTargets.isNotEmpty() && nestedTargets.all { GraphicDocumentCatalog.find(it.first) != null })
        assertEquals("Nested document identifiers must be unique",
            nestedTargets.size, nestedTargets.map { it.first }.toSet().size)
        nestedTargets.forEach { (id, _) ->
            assertTrue("No document-specific fields for $id", credentialFieldOptions(id).isNotEmpty())
        }
    }

    @Test
    fun visibilityCatalogContainsEverySettingsAndNestedDocumentTarget() {
        val visibilityIds = documentDisplayOptions.map { it.first }.toSet()
        assertEquals("Visibility catalog must not contain duplicate document identifiers",
            documentDisplayOptions.size, visibilityIds.size)
        assertTrue("Every graphic-branding target must have a visibility setting",
            documentBrandTargets.all { it.first in visibilityIds })
        val nestedTargets = churchGraphicDocumentTargets() +
            familyEntityGraphicDocumentTargets() + officialGraphicDocumentTargets()
        assertTrue("Every nested document must be manageable in visibility settings",
            nestedTargets.all { it.first in visibilityIds })
        assertTrue("Both family and entity bank registers must have independent visibility",
            setOf("bank_accounts", "rod_bank_accounts", "entity_bank_accounts").all { it in visibilityIds })
        assertEquals("Służba kościelna", documentVisibilityGroup("church_minutes"))
        assertEquals("Służba rodowa", documentVisibilityGroup("rod_birth_record"))
        assertEquals("Służba gospodarcza", documentVisibilityGroup("entity_foundation_statute"))
        assertEquals("Służba cywilna", documentVisibilityGroup("birth_extract"))
    }

    @Test
    fun nestedDocumentKindsDistinguishCredentialsFromRecords() {
        assertEquals(DocumentPreviewKind.RECORD, GraphicDocumentCatalog.find("church_bishop_appointment")?.kind)
        assertEquals(DocumentPreviewKind.CREDENTIAL, GraphicDocumentCatalog.find("church_minister_card")?.kind)
        val familyCredential = familyEntityGraphicDocumentTargets().first { it.second.contains("Legitymacja") }
        assertEquals(DocumentPreviewKind.CREDENTIAL, GraphicDocumentCatalog.find(familyCredential.first)?.kind)
        assertTrue(familyEntityGraphicDocumentTargets().filterNot { it.second.contains("Legitymacja") }
            .all { GraphicDocumentCatalog.find(it.first) != null })
    }

    @Test
    fun sourceDocumentsKeepRecordPreviewInsteadOfCredentialMockup() {
        val recordIds = setOf(
            "registry", "birth_extract", "marriage_extract", "death_extract",
            "vehicle_registration", "insurance_vehicle", "insurance_professional",
            "insurance_private", "insurance_business", "criminal_record", "mission_travel",
            "eu_education", "organization_foundation", "organization_church", "tactical_training", "survival_training", "defense_training", "reenactment_qualification",
            "organization_company", "organization_association", "official"
        )
        recordIds.forEach { id ->
            assertEquals(DocumentPreviewKind.RECORD, GraphicDocumentCatalog.find(id)?.kind)
        }
    }

    @Test
    fun credentialDocumentsUseCredentialPreview() {
        val credentialIds = setOf(
            "large_family", "blood_donor", "disability_card", "ehic", "church_bishop",
            "church_archbishop", "church_cardinal", "church_abbot", "church_minister",
            "church_missionary", "professional_medical", "professional_education", "military_book", "martial_arts", "shooting_sport", "archery", "security_guard", "historical_reenactment", "reenactment_event",
            "professional_therapeutic", "professional_other", "loyalty_cards"
        )
        credentialIds.forEach { id ->
            assertEquals(DocumentPreviewKind.CREDENTIAL, GraphicDocumentCatalog.find(id)?.kind)
        }
        assertTrue(GraphicDocumentCatalog.definitions.all { it.supportsPhoto })
    }
}
