package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentGroupCatalogTest {
    private fun defaultConfiguration() = DocumentGroupConfiguration(
        groups = documentVisibilityGroupOrder,
        assignments = documentDisplayOptions.distinctBy { it.first }
            .associate { (id, _, _) -> id to documentVisibilityGroup(id) }
    )

    @Test
    fun everyCatalogDocumentHasExactlyOneDeclaredGroup() {
        val config = defaultConfiguration()
        val ids = documentDisplayOptions.distinctBy { it.first }.map { it.first }
        assertEquals(ids.toSet(), config.assignments.keys)
        assertTrue(config.assignments.values.all { it in config.groups })
    }

    @Test
    fun addingGroupDoesNotChangeExistingAssignments() {
        val config = defaultConfiguration()
        val updated = addDocumentGroup(config, "Dokumenty dodatkowe")
        assertTrue("Dokumenty dodatkowe" in updated.groups)
        assertEquals(config.assignments, updated.assignments)
    }

    @Test
    fun documentCanBeMovedToNewGroup() {
        val config = addDocumentGroup(defaultConfiguration(), "Dokumenty dodatkowe")
        val updated = assignDocumentToGroup(config, "passport", "Dokumenty dodatkowe")
        assertEquals("Dokumenty dodatkowe", updated.assignments["passport"])
        assertEquals(config.assignments.size, updated.assignments.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateGroupNamesAreRejectedIgnoringCase() {
        addDocumentGroup(addDocumentGroup(defaultConfiguration(), "Archiwum"), "archiwum")
    }
}
