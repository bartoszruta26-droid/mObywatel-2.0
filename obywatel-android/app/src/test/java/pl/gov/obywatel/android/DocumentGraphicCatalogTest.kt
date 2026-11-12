// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentGraphicCatalogTest {
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
            "organization_company", "organization_association", "official", "loyalty_cards"
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
    fun sourceDocumentsKeepRecordPreviewInsteadOfCredentialMockup() {
        val recordIds = setOf(
            "registry", "birth_extract", "marriage_extract", "death_extract",
            "vehicle_registration", "insurance_vehicle", "insurance_professional",
            "insurance_private", "insurance_business", "criminal_record", "mission_travel",
            "eu_education", "organization_foundation", "organization_church",
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
            "church_missionary", "professional_medical", "professional_education",
            "professional_therapeutic", "professional_other", "loyalty_cards"
        )
        credentialIds.forEach { id ->
            assertEquals(DocumentPreviewKind.CREDENTIAL, GraphicDocumentCatalog.find(id)?.kind)
        }
        assertTrue(GraphicDocumentCatalog.definitions.all { it.supportsPhoto })
    }
}
