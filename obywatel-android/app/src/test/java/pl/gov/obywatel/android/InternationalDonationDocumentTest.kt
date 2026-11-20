// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InternationalDonationDocumentTest {
    @Test
    fun allThreeInternationalDonationDocumentsAreCataloguedInTheirOwnCategories() {
        assertEquals("Służba kościelna",
            InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_WORSHIP_ID)?.category)
        assertEquals("Służba stowarzyszeniowa",
            InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_NGO_ID)?.category)
        assertEquals("Służba cywilna",
            InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_PERSON_ID)?.category)
        assertEquals(3, InternationalDonationDocumentCatalog.all.size)
    }

    @Test
    fun donationFormsRequireThePartiesAndEssentialDonationTerms() {
        val worship = requireNotNull(InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_WORSHIP_ID))
        val ngo = requireNotNull(InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_NGO_ID))
        val person = requireNotNull(InternationalDonationDocumentCatalog.find(INTERNATIONAL_DONATION_PERSON_ID))

        assertTrue(worship.requiredFieldKeys.containsAll(setOf(
            "donor_name", "recipient_name", "recipient_legal_status", "religious_purpose",
            "donation_object", "donation_value", "donation_currency", "donation_date",
            "donation_method", "recipient_acceptance"
        )))
        assertTrue(ngo.requiredFieldKeys.containsAll(setOf(
            "donor_name", "recipient_name", "recipient_legal_form", "recipient_registry",
            "statutory_purpose", "donation_object", "donation_value", "donation_currency"
        )))
        assertTrue(person.requiredFieldKeys.containsAll(setOf(
            "donor_name", "recipient_name", "recipient_address", "donation_object",
            "donation_value", "donation_currency", "recipient_acceptance"
        )))
    }

    @Test
    fun missingRequiredFieldsAreReportedByFieldKey() {
        val missing = InternationalDonationDocumentCatalog.missingRequiredFields(
            INTERNATIONAL_DONATION_PERSON_ID,
            mapOf("donor_name" to "Jan Kowalski")
        )
        assertTrue("recipient_name" in missing)
        assertTrue("donation_currency" in missing)
        assertTrue("donation_value" in missing)
    }

    @Test
    fun eachDonationDocumentHasARegisteredDocumentRoute() {
        listOf(
            INTERNATIONAL_DONATION_WORSHIP_ID,
            INTERNATIONAL_DONATION_NGO_ID,
            INTERNATIONAL_DONATION_PERSON_ID
        ).forEach { id ->
            assertEquals(DocumentRouteType.DOCUMENT, DocumentRouteRegistry.find(id)?.type)
        }
    }
}
