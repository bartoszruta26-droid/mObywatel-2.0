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
            assertTrue("No fields defined for $id", definitions.isNotEmpty())
            assertEquals("Duplicate field keys for $id", definitions.size, definitions.map { it.key }.toSet().size)
            assertTrue("Empty field labels for $id", definitions.all { it.label.isNotBlank() })
        }
    }

    @Test
    fun documentEditorFieldKeysAreStableAndUniqueAcrossCatalog() {
        val keys = documentEditorFieldKeys()
        assertTrue(keys.isNotEmpty())
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.all { it.startsWith("doc_") })
    }
}
