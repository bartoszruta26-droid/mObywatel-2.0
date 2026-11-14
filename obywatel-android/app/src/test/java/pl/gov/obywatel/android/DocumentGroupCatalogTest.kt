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
    fun serviceGroupNamesAreUsedAndLegacyNamesMigrateToTheirNewCounterparts() {
        assertEquals("Służba rodowa", documentVisibilityGroup("family"))
        assertEquals("Służba osobista", documentVisibilityGroup("identity"))
        assertEquals("Służba cywilna", documentVisibilityGroup("birth_extract"))
        assertEquals("Służba państwowa", documentVisibilityGroup("parliament"))
        assertEquals("Służba kościelna", documentVisibilityGroup("church_bishop"))
        assertEquals("Służba zbrojna", documentVisibilityGroup("military_book"))
        assertEquals("Służba zbrojna", documentVisibilityGroup("historical_reenactment"))
        assertEquals("Służba zawodowa", documentVisibilityGroup("professional_medical"))
        assertEquals("Służba finansowa", documentVisibilityGroup("bank_accounts"))
        assertEquals("Służba zdrowotna", documentVisibilityGroup("disability_card"))
        assertEquals("Służba zbrojna", migrateDocumentGroupName("Służba zbrojna"))
        assertEquals("Służba rodowa", migrateDocumentGroupName("Dokumenty rodowe i rodzinne"))
        assertEquals("Moja własna grupa", migrateDocumentGroupName("Moja własna grupa"))
        assertEquals("Służba społeczna", migrateDocumentGroupAssignment("organization_foundation", "Służba gospodarcza"))
        assertEquals("Służba stowarzyszeniowa", migrateDocumentGroupAssignment("organization_association", "Służba gospodarcza"))
        assertEquals("Moja własna grupa", migrateDocumentGroupAssignment("organization_foundation", "Moja własna grupa"))
    }

    @Test
    fun armedServiceDocumentsAreAvailableInCatalogButHiddenFromDashboardByDefault() {
        val armedIds = setOf(
            "military_book", "martial_arts", "shooting_sport", "archery",
            "tactical_training", "survival_training", "security_guard", "defense_training",
            "historical_reenactment", "reenactment_qualification", "reenactment_event",
            "military_service_record", "military_personnel_order", "military_professional_contract",
            "military_voluntary_basic_service", "military_territorial_service", "military_active_reserve",
            "military_passive_reserve", "military_exercise_callup", "military_candidate_service",
            "military_gendarmerie_service", "military_foreign_service"
        )
        assertTrue(armedIds.all { defaultDocumentPlacements[it] == "hidden" })
        assertEquals("dashboard", defaultDocumentPlacements["identity"])
        assertEquals("Służba zbrojna", documentVisibilityGroup("military_book"))
        assertTrue("Służba zbrojna" in documentVisibilityGroupOrder)
    }

    @Test
    fun specialServicesHaveDedicatedCredentialPersonnelCertificateAndAssignmentDocuments() {
        val expected = setOf(
            "abw_legitimacy",
            "abw_personnel_order",
            "abw_service_certificate",
            "abw_assignment",
            "aw_legitimacy",
            "aw_personnel_order",
            "aw_service_certificate",
            "aw_assignment",
            "skw_legitimacy",
            "skw_personnel_order",
            "skw_service_certificate",
            "skw_assignment",
            "sww_legitimacy",
            "sww_personnel_order",
            "sww_service_certificate",
            "sww_assignment",
            "cba_legitimacy",
            "cba_personnel_order",
            "cba_service_certificate",
            "cba_assignment",
            "sop_legitimacy",
            "sop_personnel_order",
            "sop_service_certificate",
            "sop_assignment"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing special-service document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun customsTaxServiceDocumentsHaveDedicatedFieldsRoutesAndStateServiceGroup() {
        val expected = setOf(
            "customs_tax_legitimacy", "customs_tax_personnel_order",
            "customs_tax_service_certificate", "customs_tax_control",
            "customs_tax_economic_crime", "customs_tax_enforcement",
            "customs_tax_border_control"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing Customs and Tax Service document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun prisonServiceDocumentsHaveDedicatedFieldsRoutesAndStateServiceGroup() {
        val expected = setOf(
            "prison_service_legitimacy", "prison_service_personnel_order",
            "prison_service_certificate", "prison_service_protection",
            "prison_service_penitentiary", "prison_service_records",
            "prison_service_healthcare", "prison_service_escort"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing Prison Service document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun borderGuardServiceDocumentsHaveDedicatedFieldsRoutesAndStateServiceGroup() {
        val expected = setOf(
            "border_guard_legitimacy", "border_guard_personnel_order",
            "border_guard_service_certificate", "border_guard_border_service",
            "border_guard_operational_service", "border_guard_aviation_service",
            "border_guard_unit_service"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing Border Guard document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun stateFireServiceDocumentsHaveDedicatedFieldsRoutesAndGroup() {
        val expected = setOf(
            "fire_service_legitimacy", "fire_service_personnel_order", "fire_service_preparatory",
            "fire_service_permanent", "fire_service_station_commander", "fire_service_shift_commander",
            "fire_service_officer", "fire_service_rescue_unit", "fire_service_headquarters"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing PSP document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun policeServiceDocumentsHaveDedicatedFieldsRoutesAndPublicServiceGroup() {
        val expected = setOf(
            "police_service_legitimacy", "police_personnel_order", "police_service_certificate",
            "police_preventive_service", "police_criminal_service", "police_investigative_service",
            "police_counterterrorist_service", "police_supporting_service", "police_traffic_service",
            "police_water_service", "police_riot_unit_service", "police_cybercrime_service"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing police document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba państwowa", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty())
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun militaryServiceDocumentFormsCoverEveryListedServiceTypeAndUseArmedServiceGroup() {
        val expected = setOf(
            "military_book", "military_service_record", "military_personnel_order",
            "military_professional_contract", "military_voluntary_basic_service",
            "military_territorial_service", "military_active_reserve", "military_passive_reserve",
            "military_exercise_callup", "military_candidate_service",
            "military_gendarmerie_service", "military_foreign_service"
        )
        val catalogIds = documentDisplayOptions.map { it.first }.toSet()
        assertTrue("Missing military document forms: ${expected - catalogIds}", catalogIds.containsAll(expected))
        expected.forEach { id ->
            assertEquals("Służba zbrojna", documentVisibilityGroup(id))
            assertTrue("Missing dedicated fields for $id", officialGraphicDocumentFields(id).isNotEmpty() || id == "military_book")
            assertTrue("Missing route for $id", DocumentRouteRegistry.find(id) != null)
        }
    }

    @Test
    fun foundationBusinessAndAssociationServicesEachHaveThreeIndependentCompleteDocumentSets() {
        val expected = mapOf(
            "foundation" to "Służba społeczna",
            "business" to "Służba gospodarcza",
            "association" to "Służba stowarzyszeniowa"
        )
        val subjectPrefixes = listOf("legal_person_unit", "natural_person_unit", "legal_person")
        expected.forEach { (service, group) ->
            val documents = familyEntityDocumentsForService(service)
            assertTrue("$group must contain documents", documents.isNotEmpty())
            assertEquals("$group must have unique document IDs", documents.size, documents.map { it.first }.toSet().size)
            subjectPrefixes.forEach { subject ->
                val subset = documents.filter { it.first.startsWith("service_${service}_${subject}_") }
                assertTrue("$group is missing the $subject document set", subset.isNotEmpty())
                subset.forEach { (id, _) ->
                    assertEquals("$id must be assigned to $group", group, documentVisibilityGroup(id))
                    assertTrue("$id must have dedicated preview fields", familyEntityGraphicDocumentFields(id).isNotEmpty())
                }
            }
            assertEquals("Every service must expose exactly the three required subject sets", 3,
                documents.mapNotNull { id -> subjectPrefixes.firstOrNull { id.first.startsWith("service_${service}_${it}_") } }.toSet().size)
        }
        assertEquals("Służba społeczna", documentVisibilityGroup("organization_foundation"))
        assertEquals("Służba gospodarcza", documentVisibilityGroup("organization_company"))
        assertEquals("Służba stowarzyszeniowa", documentVisibilityGroup("organization_association"))
        assertTrue("Służba społeczna" in documentVisibilityGroupOrder)
        assertTrue("Służba gospodarcza" in documentVisibilityGroupOrder)
        assertTrue("Służba stowarzyszeniowa" in documentVisibilityGroupOrder)
    }

    @Test
    fun iconAssetKindsAreTintedWhiteAcrossLegacyAndExtendedCategories() {
        assertTrue(isWhiteTintedIconAssetKind("Ikona wektorowa"))
        assertTrue(isWhiteTintedIconAssetKind("Ikona wektorowa kafelka"))
        assertTrue(isWhiteTintedIconAssetKind("Ikona"))
        assertTrue(isWhiteTintedIconAssetKind("  Ikona dodatkowa  "))
        assertTrue(isWhiteTintedIconAssetKind("Piktogram kafelka"))
        assertFalse(isWhiteTintedIconAssetKind("Tło kafelka"))
        assertFalse(isWhiteTintedIconAssetKind("Fotografia"))
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
