package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentCatalogTest {
    @Test
    fun catalogContainsUniqueDocumentIds() {
        val ids = documentDisplayOptions.map { it.first }
        assertEquals("Identyfikatory dokumentów muszą być unikalne.", ids.size, ids.toSet().size)
    }

    @Test
    fun everyDocumentBelongsToOneDeclaredGroup() {
        val declaredGroups = documentVisibilityGroupOrder.toSet()
        documentDisplayOptions.forEach { (id, _, _) ->
            assertTrue(
                "Dokument $id ma grupę spoza wspólnego katalogu: ${documentVisibilityGroup(id)}",
                documentVisibilityGroup(id) in declaredGroups
            )
        }
    }

    @Test
    fun stateServiceDocumentsAreAssignedToStateServiceGroup() {
        val stateIds = setOf("mayor", "village_head", "county_starosta", "citizen_record", "ombudsman", "children_ombudsman", "patient_ombudsman", "pretrial_mediator", "milicja_officer", "foreign_service_member", "diplomat", "consul", "ambassador", "foreign_mission_head", "consular_employee", "inspector")
        assertTrue(stateIds.all { id -> documentVisibilityGroup(id) == "Służba państwowa" })
        assertTrue(stateIds.all { id -> documentDisplayOptions.any { it.first == id } })
    }

    @Test
    fun groupOrderDoesNotContainDuplicates() {
        assertEquals(documentVisibilityGroupOrder.size, documentVisibilityGroupOrder.toSet().size)
    }
}
