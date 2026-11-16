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
    fun internationalMedicalCertificateIsCataloguedInHealthService() {
        val item = documentDisplayOptions.single { it.first == INTERNATIONAL_MEDICAL_CERTIFICATE_ID }
        assertEquals("International Medical Certificate", item.second)
        assertEquals("Służba zdrowotna", documentVisibilityGroup(item.first))
        assertEquals("hidden", defaultDocumentPlacements[item.first])
        assertTrue(DocumentRouteRegistry.find(item.first) != null)
        assertTrue(documentEditorFieldDefinitions(item.first).all { it.label.all { ch -> ch.code < 128 } })
    }


    @Test
    fun internationalDocumentCatalogHasDedicatedGroupsRoutesAndSchemas() {
        val expectedGroups = mapOf(
            "icvp" to "Dokumenty międzynarodowe — szczepienia",
            "medical_summary" to "Dokumenty międzynarodowe — medyczne",
            "allergy_card" to "Dokumenty międzynarodowe — farmaceutyczne",
            "medication_card" to "Dokumenty międzynarodowe — farmaceutyczne",
            "fit_to_travel" to "Dokumenty międzynarodowe — medyczne",
            "fit_to_fly" to "Dokumenty międzynarodowe — medyczne",
            "rehabilitation_report" to "Dokumenty międzynarodowe — medyczne",
            "functional_report" to "Dokumenty międzynarodowe — medyczne",
            "disability_opinion" to "Dokumenty międzynarodowe — medyczne",
            "eu_parking_card" to "Dokumenty międzynarodowe — niepełnosprawność",
            "prc" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "s1" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "s2" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "s3" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "da1" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "cross_border_prescription" to "Dokumenty międzynarodowe — farmaceutyczne",
            "refund_claim" to "Dokumenty międzynarodowe — ubezpieczenie zdrowotne",
            "a1" to "Dokumenty międzynarodowe — ubezpieczenia i socjalne",
            "u1" to "Dokumenty międzynarodowe — ubezpieczenia i socjalne",
            "u2" to "Dokumenty międzynarodowe — ubezpieczenia i socjalne",
            "epc" to "Dokumenty międzynarodowe — zawodowe",
            "good_standing" to "Dokumenty międzynarodowe — zawodowe",
            "professional_status" to "Dokumenty międzynarodowe — zawodowe",
            "qualification_recognition" to "Dokumenty międzynarodowe — zawodowe",
            "diploma" to "Dokumenty międzynarodowe — edukacja",
            "education_certificate" to "Dokumenty międzynarodowe — edukacja",
            "diploma_supplement" to "Dokumenty międzynarodowe — edukacja",
            "birth_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "marriage_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "death_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "marital_status" to "Dokumenty międzynarodowe — stan cywilny",
            "capacity_to_marry" to "Dokumenty międzynarodowe — stan cywilny",
            "residence_certificate" to "Dokumenty międzynarodowe — urzędowe",
            "citizenship_certificate" to "Dokumenty międzynarodowe — urzędowe",
            "life_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "criminal_record_foreign" to "Dokumenty międzynarodowe — urzędowe",
            "divorce_decree" to "Dokumenty międzynarodowe — urzędowe",
            "adoption_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "parentage_certificate" to "Dokumenty międzynarodowe — stan cywilny",
            "international_driving_permit" to "Dokumenty międzynarodowe — podróż i tożsamość",
            "travel_document" to "Dokumenty międzynarodowe — podróż i tożsamość",
            "residence_card" to "Dokumenty międzynarodowe — podróż i tożsamość",
            "passport_record" to "Dokumenty międzynarodowe — podróż i tożsamość"
        )
        assertEquals(43, expectedGroups.size)
        expectedGroups.forEach { (id, group) ->
            assertTrue("Brak kafelka $id", documentDisplayOptions.any { it.first == id })
            assertEquals("Nieprawidłowa grupa dla $id", group, documentVisibilityGroup(id))
            assertTrue("Brak trasy dla $id", DocumentRouteRegistry.find(id) != null)
            val schema = documentEditorFieldDefinitions(id)
            assertTrue("Brak pól ewidencyjnych dla $id", schema.isNotEmpty())
            assertEquals("Powtórzone klucze pól dla $id", schema.size, schema.map { it.key }.toSet().size)
        }
        assertTrue(expectedGroups.values.all { it in documentVisibilityGroupOrder })
    }

}
