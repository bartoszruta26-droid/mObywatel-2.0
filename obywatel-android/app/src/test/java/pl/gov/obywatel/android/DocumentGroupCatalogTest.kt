package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentGroupCatalogTest {
    @Test
    fun everyDocumentHasExactlyOneFixedGroup() {
        val documents = documentDisplayOptions.distinctBy { it.first }
        assertTrue(documents.isNotEmpty())
        documents.forEach { (id, _, _) ->
            assertTrue("Missing group for $id", documentVisibilityGroup(id) in documentVisibilityGroupOrder)
        }
    }

    @Test
    fun fixedGroupCatalogContainsNoEmptyGroupsOrDuplicateDocuments() {
        val ids = documentDisplayOptions.map { it.first }
        assertEquals(ids.size, ids.toSet().size)
        val groupsWithDocuments = documentVisibilityGroupOrder.filter { group ->
            documentDisplayOptions.any { documentVisibilityGroup(it.first) == group }
        }
        assertFalse(groupsWithDocuments.isEmpty())
        assertEquals(groupsWithDocuments.size, groupsWithDocuments.toSet().size)
    }

    @Test
    fun everyCatalogDocumentHasItsOwnDocumentSpecificFieldSchema() {
        documentDisplayOptions.distinctBy { it.first }.forEach { (id, _, _) ->
            val definitions = documentEditorFieldDefinitions(id)
            assertTrue("No dedicated editor or schema for $id",
                definitions.isNotEmpty() || documentUsesDedicatedEditor(id))
            assertEquals("Duplicate field keys for $id", definitions.size, definitions.map { it.key }.toSet().size)
            assertTrue("Empty field labels for $id", definitions.all { it.label.isNotBlank() })
        }
    }

    @Test
    fun everyCatalogEditorFieldIsAvailableUnderItsExactLabelInCredentialPreview() {
        documentDisplayOptions.distinctBy { it.first }.forEach { (id, _, _) ->
            val definitions = documentEditorFieldDefinitions(id)
            val options = credentialFieldOptions(id).toMap()
            definitions.forEach { definition ->
                val key = "doc_${id}_${definition.key}"
                assertEquals("Missing preview field label for $id/$key", definition.label, options[key])
            }
            val graphicFields = churchGraphicDocumentFields(id)
                .ifEmpty { familyEntityGraphicDocumentFields(id) }
                .ifEmpty { officialGraphicDocumentFields(id) }
            graphicFields.forEach { (key, label) ->
                assertEquals("Missing graphic-document field label for $id/$key", label, options[key])
            }
        }
    }

    @Test
    fun groupedOcrArchiveKeysUseOnlyTheSupportedSafeFormat() {
        assertTrue(isGroupedOcrArchiveField("groupedOcr_7f3a21"))
        assertFalse(isGroupedOcrArchiveField("groupedOcr:church:field_0"))
        assertFalse(isGroupedOcrArchiveField("groupedOcr_../../file"))
        assertTrue(isSupportedDynamicDocumentField("loyalty12Number"))
        assertTrue(isSupportedDynamicDocumentField("registry_foundation_notes"))
        assertFalse(isSupportedDynamicDocumentField("unknownField_123"))
    }

    @Test
    fun archiveImportAllowlistIncludesDedicatedAndCatalogEditorFields() {
        val keys = documentEditorImportableFieldKeys()
        assertTrue("passportNumber" in keys)
        assertTrue("disabilityDecisionNumber" in keys)
        assertTrue("passportOcrText" in keys)
        assertTrue(documentArchiveImportKeys().containsAll(setOf(
            "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
            "professionalQualificationArchive", "documentCatalogManifest"
        )))
        assertTrue("insurance_vehicle" in officialGraphicDocumentTargets().map { it.first })
    }

    @Test
    fun documentEditorFieldKeysAreStableAndUniqueAcrossCatalog() {
        val keys = documentEditorFieldKeys()
        assertTrue(keys.isNotEmpty())
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.all { it.startsWith("doc_") })
        assertTrue(keys.all { it.length <= 64 && it.matches(Regex("[A-Za-z0-9_-]+")) })
    }
}
