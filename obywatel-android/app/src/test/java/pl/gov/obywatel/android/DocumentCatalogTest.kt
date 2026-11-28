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
    fun internationalMedicalCertificateIsVisibleOnDashboardByDefault() {
        assertEquals("dashboard", defaultDocumentPlacements[INTERNATIONAL_MEDICAL_CERTIFICATE_ID])
    }

    @Test
    fun internationalMedicalCertificateIsCataloguedInHealthService() {
        val item = documentDisplayOptions.single { it.first == INTERNATIONAL_MEDICAL_CERTIFICATE_ID }
        assertEquals("International Medical Certificate", item.second)
        assertEquals("Służba zdrowotna", documentVisibilityGroup(item.first))
        assertEquals("dashboard", defaultDocumentPlacements[item.first])
        assertTrue(DocumentRouteRegistry.find(item.first) != null)
        assertTrue(documentEditorFieldDefinitions(item.first).all { it.label.all { ch -> ch.code < 128 } })
    }


    @Test
    fun internationalDocumentsUseExistingGroupsAndHaveRoutesAndSchemas() {
        val expectedGroups = mapOf(
            "icvp" to "Służba zdrowotna",
            "medical_summary" to "Służba zdrowotna",
            "allergy_card" to "Służba zdrowotna",
            "medication_card" to "Służba zdrowotna",
            "fit_to_travel" to "Służba zdrowotna",
            "fit_to_fly" to "Służba zdrowotna",
            "rehabilitation_report" to "Służba zdrowotna",
            "functional_report" to "Służba zdrowotna",
            "disability_opinion" to "Służba zdrowotna",
            "eu_parking_card" to "Służba zdrowotna",
            "prc" to "Służba zdrowotna",
            "s1" to "Służba zdrowotna",
            "s2" to "Służba zdrowotna",
            "s3" to "Służba zdrowotna",
            "da1" to "Służba zdrowotna",
            "cross_border_prescription" to "Służba zdrowotna",
            "refund_claim" to "Służba zdrowotna",
            "a1" to "Służba finansowa",
            "u1" to "Służba finansowa",
            "u2" to "Służba finansowa",
            "epc" to "Służba zawodowa",
            "good_standing" to "Służba zawodowa",
            "professional_status" to "Służba zawodowa",
            "qualification_recognition" to "Służba zawodowa",
            "diploma" to "Służba zawodowa",
            "education_certificate" to "Służba zawodowa",
            "diploma_supplement" to "Służba zawodowa",
            "birth_certificate" to "Służba cywilna",
            "marriage_certificate" to "Służba cywilna",
            "death_certificate" to "Służba cywilna",
            "marital_status" to "Służba cywilna",
            "capacity_to_marry" to "Służba cywilna",
            "residence_certificate" to "Służba cywilna",
            "citizenship_certificate" to "Służba cywilna",
            "life_certificate" to "Służba cywilna",
            "criminal_record_foreign" to "Służba cywilna",
            "divorce_decree" to "Służba cywilna",
            "adoption_certificate" to "Służba cywilna",
            "parentage_certificate" to "Służba cywilna",
            "international_driving_permit" to "Służba osobista",
            "travel_document" to "Służba osobista",
            "residence_card" to "Służba osobista",
            "passport_record" to "Służba osobista",
            "vatican_passport_ordinary" to "Służba osobista",
            "holy_see_passport_diplomatic" to "Służba osobista",
            "holy_see_passport_service" to "Służba osobista",
            "holy_see_passport_temporary_service" to "Służba osobista",
            "vatican_citizenship_certificate" to "Służba cywilna",
            "ecclesiastical_baptism_certificate" to "Służba kościelna",
            "ecclesiastical_marriage_certificate" to "Służba kościelna",
            "ecclesiastical_death_certificate" to "Służba kościelna",
            "canonical_status_certificate" to "Służba kościelna",
            "clerical_ordination_certificate" to "Służba kościelna",
            "apostolic_nunciature_credential" to "Służba kościelna",
            INTERNATIONAL_DONATION_WORSHIP_ID to "Służba kościelna",
            INTERNATIONAL_DONATION_NGO_ID to "Służba społeczna",
            INTERNATIONAL_DONATION_PERSON_ID to "Służba cywilna"
        )
        assertEquals(57, expectedGroups.size)
        val existingGroups = documentVisibilityGroupOrder.toSet()
        expectedGroups.forEach { (id, group) ->
            assertTrue("Brak kafelka $id", documentDisplayOptions.any { it.first == id })
            assertTrue("Dodano nową grupę dla $id", group in existingGroups)
            assertEquals("Nieprawidłowa grupa dla $id", group, documentVisibilityGroup(id))
            assertTrue("Brak trasy dla $id", DocumentRouteRegistry.find(id) != null)
            val schema = documentEditorFieldDefinitions(id)
            assertTrue("Brak pól ewidencyjnych dla $id", schema.isNotEmpty())
            assertEquals("Powtórzone klucze pól dla $id", schema.size, schema.map { it.key }.toSet().size)
            assertTrue("Metadane wewnętrzne trafiły do wydruku dla $id", schema.none { it.key in setOf("verificationStatus", "originalFileReference", "notes") })
            val workflowKeys = setOf(
                "issuerName", "issuerPosition", "issuingAuthority", "verificationAuthority",
                "procedureName", "jurisdiction", "sourceLanguage", "translationLanguage",
                "templateSourceReference", "templateVersion"
            )
            assertTrue("Brak pól wystawcy, organu, procedury lub wzoru dla $id",
                schema.map { it.key }.containsAll(workflowKeys))
        }
        assertEquals(15, existingGroups.size)
        val passportFields = documentEditorFieldDefinitions("holy_see_passport_service").map { it.key }.toSet()
        assertTrue(passportFields.containsAll(setOf("passportTypeCode", "passportTypeDescription", "issuingEntity")))
        val passportCodes = documentEditorFieldDefinitions("vatican_passport_ordinary").associate { it.key to it.placeholder }
        assertTrue(passportCodes["passportTypeCode"].orEmpty().contains("PP"))
        assertTrue(passportCodes["passportTypeCode"].orEmpty().contains("PV"))
        val citizenshipFields = documentEditorFieldDefinitions("vatican_citizenship_certificate").map { it.key }.toSet()
        assertTrue(citizenshipFields.containsAll(setOf("citizenshipStatus", "citizenshipAcquisitionBasis", "citizenshipEffectiveDate", "competentVaticanOffice")))
        val canonicalFields = documentEditorFieldDefinitions("canonical_status_certificate").map { it.key }.toSet()
        assertTrue(canonicalFields.containsAll(setOf("canonicalStatus", "competentOrdinary", "dioceseOrInstitute", "verificationDate")))
        val nunciatureFields = documentEditorFieldDefinitions("apostolic_nunciature_credential").map { it.key }.toSet()
        assertTrue(nunciatureFields.containsAll(setOf("nunciatureCountry", "credentialHolder", "diplomaticCapacity", "accreditingAuthority")))
        val sacramentalFields = documentEditorFieldDefinitions("ecclesiastical_baptism_certificate").map { it.key }.toSet()
        assertTrue(sacramentalFields.containsAll(setOf("parishName", "dioceseName", "registerEntryNumber", "sacramentalEventDate")))
        val ordinationFields = documentEditorFieldDefinitions("clerical_ordination_certificate").map { it.key }.toSet()
        assertTrue(ordinationFields.containsAll(setOf("ordinationRank", "ordinationDate", "ordainingBishop")))
    }


    @Test
    fun preexistingInternationalDocumentsRemainInHealthGroup() {
        val ids = setOf(
            "ehic",
            "vaccines",
            "foreign_prescription",
            INTERNATIONAL_MEDICAL_CERTIFICATE_ID
        )
        ids.forEach { id ->
            assertTrue("Brak istniejącego dokumentu $id", documentDisplayOptions.any { it.first == id })
            assertEquals("Służba zdrowotna", documentVisibilityGroup(id))
            assertTrue("Brak trasy dla $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun recoveredPublicServiceCatalogContainsRolesInExistingGroups() {
        val expectedPrefixes = mapOf(
            "military_" to "Służba zbrojna",
            "police_" to "Służba państwowa",
            "fire_" to "Służba państwowa",
            "border_" to "Służba państwowa",
            "prison_" to "Służba państwowa",
            "customs_" to "Służba państwowa",
            "special_" to "Służba państwowa",
            "protective_" to "Służba państwowa",
            "civil_service_" to "Służba cywilna",
            "civil_tax_" to "Służba cywilna",
            "civil_government_" to "Służba cywilna",
            "civil_voivodeship_" to "Służba cywilna",
            "local_" to "Służba państwowa",
            "constitutional_" to "Służba państwowa",
            "justice_" to "Służba cywilna",
            "rescue_" to "Służba zdrowotna",
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
        assertTrue(officialGraphicDocumentFields("military").any { it.second == "Stopień wojskowy" })
        assertTrue(officialGraphicDocumentFields("police_traffic").any { it.second == "Numer legitymacji służbowej" })
        assertTrue(officialGraphicDocumentFields("justice_common_judge").any { it.second == "Sąd / prokuratura / jednostka" })
        assertTrue(officialGraphicDocumentFields("education_nominated_teacher").any { it.second == "Placówka / uczelnia / instytut" })
    }

    @Test
    fun recoveredCommonServiceRecordDocumentsArePresent() {
        val expected = setOf(
            "service_record_acceptance_decision", "service_record_acceptance_order",
            "service_record_personnel_appointment_order", "service_record_position_appointment",
            "service_record_rank_appointment", "service_record_contract", "service_record_public_appointment",
            "service_record_public_election", "service_record_oath", "service_record_official_id",
            "service_record_service_certificate", "service_record_discharge_certificate",
            "service_record_personnel_history", "service_record_transfer_order",
            "service_record_promotion_order", "service_record_discharge_order",
            "service_record_opinion", "service_record_periods_certificate"
        )
        val targets = officialGraphicDocumentTargets().filter { it.first in expected }
        assertEquals(expected, targets.map { it.first }.toSet())
        targets.forEach { (id, title) ->
            assertTrue(title.isNotBlank())
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue(officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue(documentDisplayOptions.any { it.first == id })
        }
    }


    @Test
    fun selfDeterminationDocumentsHaveSafetyBoundariesAndEditablePreviewFields() {
        val expected = setOf("common_rights_self_determination_declaration", "common_rights_self_limitation", "common_rights_third_party_safeguards", "common_rights_accessibility_needs", "common_rights_faith_conscience_declaration", "common_rights_declaration_of_will", "common_rights_nonparliamentary_mandate", "common_rights_civil_service_legitimacy", "common_rights_personal_integrity_certificate")
        expected.forEach { id ->
            assertEquals("Bez szczególnej formy prawnej", documentVisibilityGroup(id))
            assertTrue("Missing tile: $id", documentDisplayOptions.any { it.first == id })
            val fields = officialGraphicDocumentFields(id)
            assertTrue("Missing form fields: $id", fields.isNotEmpty())
            val previewFields = credentialFieldOptions(id).toMap()
            fields.forEach { (key, label) -> assertEquals(label, previewFields[key]) }
        }
        val statement = officialDocTypes.first { it.id == "common_rights_declaration_of_will" }
        assertTrue(statement.note.contains("art. 233"))
        assertTrue(statement.note.contains("przepis szczególny"))
        assertTrue(officialDocTypes.first { it.id == "common_rights_third_party_safeguards" }.fields.any { it.contains("osoby trzecie") })
        assertTrue(officialDocTypes.first { it.id == "common_rights_accessibility_needs" }.fields.any { it.contains("znaczny") })
    }


}
