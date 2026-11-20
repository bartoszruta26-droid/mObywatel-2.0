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
            val route = requireNotNull(DocumentRouteRegistry.find(id))
            assertEquals("Dokument $id musi mieć trasę DOCUMENT", DocumentRouteType.DOCUMENT, route.type)
            assertEquals("Trasa musi zachować identyfikator $id", id, route.documentId)
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
        assertEquals(14, existingGroups.size)
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
            assertEquals("Nieprawidłowa grupa definicji dla $id", group, paymentAccountDocumentDefinition(id)?.group)
            val route = requireNotNull(DocumentRouteRegistry.find(id))
            assertEquals("Dokument $id musi mieć trasę DOCUMENT", DocumentRouteType.DOCUMENT, route.type)
            assertEquals("Trasa musi zachować identyfikator $id", id, route.documentId)
        }
    }


    @Test
    fun paymentAccountDonationDocumentsHaveIndependentGroupsRoutesAndSchemas() {
        val expected = mapOf(
            "payment_account_foundation" to "Służba fundacyjna",
            "payment_account_association" to "Służba stowarzyszeniowa",
            "payment_account_church" to "Służba kościelna",
            "payment_account_business" to "Służba gospodarcza",
            "payment_account_person" to "Służba osobista"
        )
        expected.forEach { (id, group) ->
            assertTrue("Brak dokumentu $id", documentDisplayOptions.any { it.first == id })
            assertEquals("Nieprawidłowa grupa dla $id", group, documentVisibilityGroup(id))
            assertTrue("Brak trasy dla $id", DocumentRouteRegistry.find(id) != null)
            val fields = documentEditorFieldDefinitions(id)
            assertTrue("Brak formularza dla $id", fields.isNotEmpty())
            assertEquals("Powtórzone pola dla $id", fields.size, fields.map { it.key }.toSet().size)
            assertTrue("Brak danych rachunku dla $id", fields.map { it.key }.containsAll(
                setOf("accountHolderName", "accountBankName", "accountIban", "accountCurrency", "accountStatus")
            ))
            assertTrue("Brak danych darowizny dla $id", fields.map { it.key }.containsAll(
                setOf("donationPurpose", "donationDate", "donationAmount", "donationCurrency",
                    "donationMethod", "donationTransactionReference", "donationSettlementStatus")
            ))
        }
    }

    @Test
    fun fivePaymentDocumentsShareCashLedgerFieldsButKeepSeparateDocumentNamespaces() {
        val documentIds = setOf(
            "payment_account_foundation", "payment_account_association",
            "payment_account_church", "payment_account_business", "payment_account_person"
        )
        val requiredCashKeys = setOf(
            "cashRegisterId", "cashEntryId", "cashEventType", "cashEventDate", "cashEventTime",
            "cashAmount", "cashCurrency", "cashReceivedBy", "cashReceiverAuthorization",
            "cashReceiptNumber", "cashRegisterBalanceBefore", "cashRegisterBalanceAfter",
            "cashReportNumber", "cashHandoverDate", "cashHandoverAmount", "cashHandoverBy",
            "cashHandoverTo", "cashBankDepositDate", "cashBankDepositAmount",
            "cashBankDepositAccount", "cashBankDepositReference", "cashAccountingDate",
            "cashAccountingReference", "cashAccountingStatus", "cashReconciliationStatus",
            "cashDifferenceAmount"
        )
        documentIds.forEach { id ->
            val keys = documentEditorFieldDefinitions(id).map { it.key }.toSet()
            assertTrue("Brak wspólnych pól kasowych dla $id", keys.containsAll(requiredCashKeys))
            assertTrue("Brak identyfikatora własnego rejestru dla $id", "cashRegisterId" in keys)
        }
        val storageKeys = documentIds.flatMap { id ->
            requiredCashKeys.map { key -> "doc_${id}_${key}" }
        }
        assertEquals("Każdy dokument musi mieć własną przestrzeń rejestru.", storageKeys.size, storageKeys.toSet().size)
        assertTrue("KP, wpłata bankowa i księgowanie muszą mieć odrębne referencje.",
            requiredCashKeys.containsAll(setOf("cashReceiptNumber", "cashBankDepositReference", "cashAccountingReference")))
    }

}
