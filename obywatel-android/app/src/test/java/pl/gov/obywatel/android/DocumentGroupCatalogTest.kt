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
    fun groupedOcrArchiveKeysUseOnlyTheSupportedSafeFormat() {
        assertTrue(isGroupedOcrArchiveField("groupedOcr_7f3a21"))
        assertFalse(isGroupedOcrArchiveField("groupedOcr:church:field_0"))
        assertFalse(isGroupedOcrArchiveField("groupedOcr_../../file"))
    }

    @Test
    fun archiveImportAllowlistIncludesDedicatedAndCatalogEditorFields() {
        val keys = documentEditorImportableFieldKeys()
        assertTrue("passportNumber" in keys)
        assertTrue("disabilityDecisionNumber" in keys)
        assertTrue("doc_insurance_vehicle_policyNumber" in keys)
        assertTrue("passportOcrText" in keys)
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
