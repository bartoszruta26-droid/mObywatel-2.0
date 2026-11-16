// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForeignPrescriptionTest {
    @Test
    fun medicineNameMayBeInternationalNameOrTradeName() {
        val complete = foreignPrescriptionRequiredFieldKeys.associate { "fpr_$it" to "value" } +
            mapOf("fpr_issueDate" to "2026-11-16", "fpr_medicineBrand" to "Example brand", "fpr_prescriberContact" to "+55 11 3812-1157")
        assertTrue(missingForeignPrescriptionFields(complete).isEmpty())

        val withoutEitherName = complete - "fpr_medicineBrand"
        assertTrue(missingForeignPrescriptionFields(withoutEitherName).contains("medicineInn"))
    }

    @Test
    fun requiredPrescriptionFieldsAreReportedWhenBlankOrMissing() {
        val missing = missingForeignPrescriptionFields(
            mapOf("fpr_patientName" to " ", "fpr_medicineInn" to "Example INN")
        )
        assertTrue(missing.contains("patientName"))
        assertTrue(missing.contains("patientAddress"))
        assertTrue(missing.contains("prescriberName"))
        assertFalse(missing.contains("medicineInn"))
        assertFalse(missing.contains("prescriberAddress")) // § 15 does not make practice address a mandatory item.
    }

    @Test
    fun prescriberContactIsRequiredButPracticeAddressIsOptional() {
        val complete = foreignPrescriptionRequiredFieldKeys.associate { "fpr_$it" to "value" } +
            mapOf("fpr_issueDate" to "2026-11-16", "fpr_medicineInn" to "Example INN", "fpr_prescriberContact" to "doctor@example.org")
        assertTrue(missingForeignPrescriptionFields(complete).isEmpty())

        val withoutContact = complete - "fpr_prescriberContact"
        assertTrue(missingForeignPrescriptionFields(withoutContact).contains("prescriberContact"))
        assertFalse(missingForeignPrescriptionFields(complete - "fpr_prescriberAddress").contains("prescriberAddress"))
    }

    @Test
    fun prescriberContactMustBeAnEmailOrInternationalPhoneNumber() {
        assertTrue(isValidForeignPrescriptionContact("doctor@example.org"))
        assertTrue(isValidForeignPrescriptionContact("+55 11 3812-1157"))
        assertFalse(isValidForeignPrescriptionContact("3812-1157"))
        assertFalse(isValidForeignPrescriptionContact("doctor@"))
        assertFalse(isValidForeignPrescriptionContact(""))
    }

    @Test
    fun issueDateMustBeARealIsoCalendarDate() {
        assertTrue(isValidForeignPrescriptionIssueDate("2024-02-29"))
        assertTrue(isValidForeignPrescriptionIssueDate("2026-11-16"))
        assertFalse(isValidForeignPrescriptionIssueDate("2023-02-29"))
        assertFalse(isValidForeignPrescriptionIssueDate("2026-13-01"))
        assertFalse(isValidForeignPrescriptionIssueDate("16-11-2026"))
        assertFalse(isValidForeignPrescriptionIssueDate("2026-11-16-extra"))
        assertFalse(isValidForeignPrescriptionIssueDate(""))
    }

    @Test
    fun machineTranslationMustContainTheTranslatedFieldValue() {
        assertTrue(isValidMedicalCertificateValueTranslation("500 mg", "500 mg", "Strength"))
        assertFalse(isValidMedicalCertificateValueTranslation("500 mg", "Strength", "Strength"))
        assertFalse(isValidMedicalCertificateValueTranslation("500 mg", " ", "Strength"))
        assertFalse(isValidMedicalCertificateValueTranslation(" ", "moc", "Strength"))
    }

    @Test
    fun foreignPrescriptionHasItsOwnSpecializedDocumentRoute() {
        val route = DocumentRouteRegistry.find(FOREIGN_PRESCRIPTION_ID)
        assertEquals(DocumentRouteType.SPECIALIZED, route?.type)
        assertEquals("foreign_prescription", route?.destination)
        assertEquals("Służba zdrowotna", documentVisibilityGroup(FOREIGN_PRESCRIPTION_ID))
        assertTrue(documentDisplayOptions.any { it.first == FOREIGN_PRESCRIPTION_ID })
        assertEquals("dashboard", defaultDocumentPlacements[FOREIGN_PRESCRIPTION_ID])
    }

    @Test
    fun defaultTranslationLanguagesAreSupported() {
        val codes = MedicalCertificateLanguages.supported.map { it.code }.toSet()
        assertTrue(codes.containsAll(setOf("pt", "pl")))
    }
}
