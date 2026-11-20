// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InternationalDocumentWorkflowTest {
    @Test
    fun allFiftyFourInternationalDocumentsHaveIssuerAndWorkflowMetadata() {
        assertEquals(57, InternationalDocumentWorkflowCatalog.all.size)
        InternationalDocumentWorkflowCatalog.all.forEach { (id, workflow) ->
            assertEquals(id, workflow.documentId)
            assertTrue(workflow.authorityLabel.isNotBlank())
            assertTrue(workflow.statusValues.contains(InternationalWorkflowStatus.DRAFT))
            assertTrue(workflow.statusValues.contains(InternationalWorkflowStatus.VERIFIED))
        }
    }

    @Test
    fun authorityIssuedDocumentsAreNotMarkedAsAppIssued() {
        val authorityIds = setOf(
            "prc", "s1", "s2", "s3", "da1", "a1", "u1", "u2",
            "birth_certificate", "marriage_certificate", "death_certificate",
            "divorce_decree", "adoption_certificate", "parentage_certificate",
            "vatican_passport_ordinary", "holy_see_passport_diplomatic", "holy_see_passport_service",
            "holy_see_passport_temporary_service", "vatican_citizenship_certificate",
            "ecclesiastical_baptism_certificate", "ecclesiastical_marriage_certificate",
            "ecclesiastical_death_certificate", "clerical_ordination_certificate"
        )
        authorityIds.forEach { id ->
            val workflow = InternationalDocumentWorkflowCatalog.find(id)
                ?: throw AssertionError("Missing workflow for $id")
            assertTrue(workflow.authorityIssuesOriginal)
            assertFalse(workflow.mode == InternationalDocumentMode.PROFESSIONAL_CERTIFICATE)
        }
    }

    @Test
    fun professionalCertificatesRequireIssuerSignature() {
        listOf("fit_to_travel", "fit_to_fly", "rehabilitation_report", "functional_report", "disability_opinion", "cross_border_prescription")
            .forEach { id ->
                assertTrue("$id should require issuer signature",
                    InternationalDocumentWorkflowCatalog.find(id)?.requiresIssuerSignature == true)
            }
    }

    @Test
    fun incompleteOrExpiredInstitutionalAssetAuthorizationIsRejected() {
        val valid = InstitutionalAssetAuthorization(
            owner = "Example institution",
            sourceUrl = "https://example.org/brand-guidelines",
            rightsBasis = "Written permission",
            authorizationReference = "PERMISSION-2026-01",
            verifiedBy = "Compliance reviewer",
            version = "2.1",
            effectiveFrom = LocalDate.of(2026, 1, 1),
            effectiveUntil = LocalDate.of(2026, 12, 31),
            permittedKinds = setOf("Logo", "Pieczęć", "Znak wodny", "Herb", "Godło", "Flaga", "Tło dokumentu")
        )
        assertTrue(valid.isCompleteFor(LocalDate.of(2026, 11, 16)))
        assertFalse(valid.copy(sourceUrl = "http://example.org/brand").isCompleteFor(LocalDate.of(2026, 11, 16)))
        assertFalse(valid.copy(authorizationReference = "").isCompleteFor(LocalDate.of(2026, 11, 16)))
        assertFalse(valid.isCompleteFor(LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun sealsHandwrittenSignaturesAndSecurityHologramsAreNeverRenderedAsImages() {
        val valid = InstitutionalAssetAuthorization(
            owner = "Example institution",
            sourceUrl = "https://example.org/brand-guidelines",
            rightsBasis = "Written permission",
            authorizationReference = "PERMISSION-2026-01",
            verifiedBy = "Compliance reviewer",
            version = "2.1",
            effectiveFrom = LocalDate.of(2026, 1, 1),
            permittedKinds = setOf("Logo", "Pieczęć", "Znak wodny")
        )
        assertTrue(InstitutionalAssetPolicy.mayRender("Pieczęć", valid))
        assertFalse(InstitutionalAssetPolicy.mayRender("Wzór podpisu odręcznego", valid))
        assertFalse(InstitutionalAssetPolicy.mayRender("Grafika hologramu / zabezpieczenia", valid))
        assertTrue(InstitutionalAssetPolicy.mayRender("Logo", valid))
        assertFalse(InstitutionalAssetPolicy.mayRender("Godło", valid))
        assertFalse(InstitutionalAssetPolicy.mayRender("Logo", null))
    }

    @Test
    fun everyInternationalCatalogueIdResolvesToOneWorkflowEntry() {
        assertEquals(57, InternationalDocumentWorkflowCatalog.all.keys.toSet().size)
        assertNotNull(InternationalDocumentWorkflowCatalog.find("icvp"))
        assertNotNull(InternationalDocumentWorkflowCatalog.find("passport_record"))
        assertNotNull(InternationalDocumentWorkflowCatalog.find("vatican_passport_ordinary"))
        assertNotNull(InternationalDocumentWorkflowCatalog.find("holy_see_passport_diplomatic"))
        assertNotNull(InternationalDocumentWorkflowCatalog.find("ecclesiastical_baptism_certificate"))
    }

    @Test
    fun authorityIssuedDocumentsPrintAsDataOrApplicationSheetsNotAsIssuedOriginals() {
        assertEquals("Dane / wniosek dotyczący: Dokument S1", pdfDocumentDisplayTitle("s1", "Dokument S1"))
        assertEquals("Zaświadczenie Fit to Fly", pdfDocumentDisplayTitle("fit_to_fly", "Zaświadczenie Fit to Fly"))
    }

    @Test
    fun officialTemplateMustHaveCurrentJurisdictionalSourceAndPermissionMetadata() {
        val approved = InstitutionalTemplateApproval(
            documentId = "s1",
            jurisdiction = "PL",
            issuingAuthority = "Właściwa instytucja ubezpieczenia zdrowotnego",
            sourceUrl = "https://example.org/forms/s1",
            sourceTitle = "S1 official form",
            sourceVersion = "2026-01",
            rightsBasis = "Official publication terms",
            approvalReference = "PUBLICATION-TERMS-2026",
            checkedBy = "Compliance reviewer",
            effectiveFrom = LocalDate.of(2026, 1, 1),
            effectiveUntil = LocalDate.of(2026, 12, 31)
        )
        assertTrue(approved.isUsableFor(LocalDate.of(2026, 11, 16)))
        assertFalse(approved.copy(sourceUrl = "http://example.org/s1").isUsableFor(LocalDate.of(2026, 11, 16)))
        assertFalse(approved.copy(approvalReference = "").isUsableFor(LocalDate.of(2026, 11, 16)))
        assertFalse(approved.copy(documentId = "unknown_document").isUsableFor(LocalDate.of(2026, 11, 16)))
        assertFalse(approved.isUsableFor(LocalDate.of(2027, 1, 1)))
        assertTrue(InstitutionalTemplateCatalog.approved.isEmpty())
        assertEquals(null, InstitutionalTemplateCatalog.findUsable("s1"))
    }
}
