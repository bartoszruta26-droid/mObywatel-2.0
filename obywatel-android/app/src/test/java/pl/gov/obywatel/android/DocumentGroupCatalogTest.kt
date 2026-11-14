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
    }

    @Test
    fun armedServiceDocumentsAreAvailableInCatalogButHiddenFromDashboardByDefault() {
        val armedIds = setOf(
            "military_book", "martial_arts", "shooting_sport", "archery",
            "tactical_training", "survival_training", "security_guard", "defense_training",
            "historical_reenactment", "reenactment_qualification", "reenactment_event"
        )
        assertTrue(armedIds.all { defaultDocumentPlacements[it] == "hidden" })
        assertEquals("dashboard", defaultDocumentPlacements["identity"])
        assertEquals("Służba zbrojna", documentVisibilityGroup("military_book"))
        assertTrue("Służba zbrojna" in documentVisibilityGroupOrder)
    }

    @Test
    fun foundationBusinessAndAssociationServicesEachHaveThreeIndependentCompleteDocumentSets() {
        val expected = mapOf(
            "foundation" to "Służba fundacyjna",
            "business" to "Służba gospodarcza",
            "association" to "Służba stowarzyszeniowa"
        )
        val subjectPrefixes = listOf("legal_person", "legal_person_unit", "natural_person_unit")
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
        assertEquals("Służba fundacyjna", documentVisibilityGroup("organization_foundation"))
        assertEquals("Służba gospodarcza", documentVisibilityGroup("organization_company"))
        assertEquals("Służba stowarzyszeniowa", documentVisibilityGroup("organization_association"))
        assertTrue("Służba fundacyjna" in documentVisibilityGroupOrder)
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
