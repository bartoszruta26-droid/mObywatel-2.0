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

    @Test
    fun civilAndSocialFunctionDocumentsArePresentInExistingGroups() {
        val expectedGroups = mapOf(
            "org_president" to "Służba gospodarcza",
            "org_general_director" to "Służba gospodarcza",
            "org_regional_director" to "Służba gospodarcza",
            "org_county_director" to "Służba gospodarcza",
            "org_local_representative" to "Służba gospodarcza",
            "org_general_secretary" to "Służba gospodarcza",
            "org_treasurer" to "Służba gospodarcza",
            "org_members_rights_ombudsman" to "Służba cywilna",
            "org_patient_ombudsman" to "Służba cywilna",
            "org_family_ombudsman" to "Służba cywilna",
            "civil_mediator" to "Służba cywilna",
            "civil_arbitrator" to "Służba cywilna",
            "civil_expert" to "Służba zawodowa",
            "civil_appraiser" to "Służba zawodowa",
            "civil_auditor" to "Służba zawodowa",
            "civil_internal_inspector" to "Służba zawodowa",
            "civil_controller" to "Służba zawodowa",
            "civil_consultant" to "Służba zawodowa",
            "civil_social_caretaker" to "Służba cywilna",
            "civil_org_curator" to "Służba cywilna",
            "civil_rescuer" to "Służba zdrowotna",
            "org_ambassador" to "Służba gospodarcza",
            "org_foreign_representative" to "Służba gospodarcza",
            "org_proxy" to "Służba gospodarcza",
            "org_delegate" to "Służba gospodarcza"
        )
        expectedGroups.forEach { (id, group) ->
            assertTrue("Brak dokumentu $id w katalogu.", documentDisplayOptions.any { it.first == id })
            assertEquals("Nieprawidłowa grupa dokumentu $id.", group, documentVisibilityGroup(id))
            assertTrue("Brak formularza danych dla $id.", officialGraphicDocumentFields(id).isNotEmpty())
        }
    }


    @Test
    fun commonServiceRecordDocumentsArePresentInExistingStateServiceGroup() {
        val expected = setOf(
            "service_record_acceptance_decision",
            "service_record_acceptance_order",
            "service_record_personnel_appointment_order",
            "service_record_position_appointment",
            "service_record_rank_appointment",
            "service_record_contract",
            "service_record_public_appointment",
            "service_record_public_election",
            "service_record_oath",
            "service_record_official_id",
            "service_record_service_certificate",
            "service_record_discharge_certificate",
            "service_record_personnel_history",
            "service_record_transfer_order",
            "service_record_promotion_order",
            "service_record_discharge_order",
            "service_record_opinion",
            "service_record_periods_certificate"
        )
        val targets = officialGraphicDocumentTargets().filter { it.first in expected }
        assertEquals("Katalog powinien zawierać wszystkie 18 wspólnych dokumentów służbowych.", expected, targets.map { it.first }.toSet())
        targets.forEach { (id, title) ->
            assertTrue("Pusty tytuł dokumentu $id.", title.isNotBlank())
            assertEquals("Dokument $id powinien należeć do istniejącej grupy Służba państwowa.", "Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Brak pól formularza dla $id.", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Brak kafelka dla $id.", documentDisplayOptions.any { it.first == id })
        }
        assertTrue(officialGraphicDocumentFields("service_record_oath").any { it.second.contains("rot", ignoreCase = true) })
        assertTrue(officialGraphicDocumentFields("service_record_personnel_history").any { it.second.contains("Chronologia", ignoreCase = true) })
    }

    @Test
    fun publicAndUniformedServiceCatalogContainsEveryNewRoleInAnExistingGroup() {
        val expectedPrefixes = mapOf(
            "military_" to "Służba zbrojna", "police_" to "Służba państwowa",
            "fire_" to "Służba państwowa", "border_" to "Służba państwowa",
            "prison_" to "Służba państwowa", "customs_" to "Służba państwowa",
            "special_" to "Służba państwowa", "protective_" to "Służba państwowa",
            "civil_service_" to "Służba cywilna", "civil_tax_" to "Służba cywilna",
            "civil_government_" to "Służba cywilna", "civil_voivodeship_" to "Służba cywilna",
            "local_" to "Służba państwowa", "constitutional_" to "Służba państwowa",
            "justice_" to "Służba cywilna", "rescue_" to "Służba zdrowotna",
"education_" to "Służba zawodowa",
            "inspection_" to "Służba cywilna"
        )
        val additions = officialGraphicDocumentTargets().filter { (id, _) ->
            id == "military" || expectedPrefixes.keys.any(id::startsWith)
        }
        assertTrue("Nowy katalog służb jest pusty.", additions.isNotEmpty())
        expectedPrefixes.forEach { (prefix, group) ->
            val subset = additions.filter { it.first.startsWith(prefix) }
            assertTrue("Brak dokumentów dla kategorii $prefix", subset.isNotEmpty())
            subset.forEach { (id, title) ->
                assertTrue("Pusty tytuł $id", title.isNotBlank())
                assertEquals("Nieprawidłowa grupa dla $id", group, documentVisibilityGroup(id))
                assertTrue("Brak formularza dla $id", officialGraphicDocumentFields(id).isNotEmpty())
                assertTrue("Brak kafelka dla $id", documentDisplayOptions.any { it.first == id })
            }
        }
        assertEquals("Żołnierz zawodowy powinien należeć do służby zbrojnej.", "Służba zbrojna", documentVisibilityGroup("military"))
        assertEquals(additions.size, additions.map { it.first }.toSet().size)
        assertTrue("Formularz wojskowy musi mieć pola właściwe dla służby wojskowej.",
            officialGraphicDocumentFields("military").map { it.second }.contains("Stopień wojskowy"))
        assertTrue("Formularz policyjny musi mieć pola właściwe dla formacji mundurowej.",
            officialGraphicDocumentFields("police_traffic").map { it.second }.contains("Numer legitymacji służbowej"))
        assertTrue("Formularz sądowy musi mieć pola właściwe dla wymiaru sprawiedliwości.",
            officialGraphicDocumentFields("justice_common_judge").map { it.second }.contains("Sąd / prokuratura / jednostka"))
        assertTrue("Formularz nauczycielski musi mieć pola właściwe dla oświaty.",
            officialGraphicDocumentFields("education_nominated_teacher").map { it.second }.contains("Placówka / uczelnia / instytut"))
    }

}
