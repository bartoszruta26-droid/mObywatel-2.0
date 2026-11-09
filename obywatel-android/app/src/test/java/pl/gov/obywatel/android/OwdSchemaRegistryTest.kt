// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwdSchemaRegistryTest {
    @Test
    fun disabilityRecordRoundTripsWithoutChangingProvenance() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.DISABILITY_STATUS,
            fields = mapOf("status" to "aktywny", "degree" to "umiarkowany"),
            provenance = DataProvenance.LOCAL_USER
        )

        val encoded = OwdSchemaRegistry.serialize(record)
        val decoded = OwdSchemaRegistry.deserialize(encoded)

        assertEquals(record, decoded)
        assertEquals(DataProvenance.LOCAL_USER, decoded.provenance)
    }

    @Test
    fun rejectsUnknownFields() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.DISABILITY_STATUS,
            fields = mapOf("status" to "aktywny", "pesel" to "00000000000"),
            provenance = DataProvenance.LOCAL_USER
        )

        assertTrue(OwdSchemaRegistry.validate(record).any { it.contains("Nieznane pola") })
    }

    @Test
    fun rejectsMissingRequiredFields() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.CIVIC_MANDATE_STATUS,
            fields = mapOf("office" to "radny"),
            provenance = DataProvenance.LOCAL_USER
        )

        val errors = OwdSchemaRegistry.validate(record)
        assertTrue(errors.any { it.contains("jurisdiction") })
        assertTrue(errors.any { it.contains("status") })
    }

    @Test
    fun issuerAttestedRecordRequiresIssuerEvidenceAndIssueTime() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.DISABILITY_STATUS,
            fields = mapOf("status" to "aktywny"),
            provenance = DataProvenance.ISSUER_ATTESTED
        )

        val errors = OwdSchemaRegistry.validate(record)
        assertTrue(errors.any { it.contains("identyfikatora wystawcy") })
        assertTrue(errors.any { it.contains("referencji dowodu") })
        assertTrue(errors.any { it.contains("czasu wystawienia") })
    }

    @Test
    fun catalogueContainsOnlyApplicationOwnedSchemaIds() {
        val catalogue = OwdSchemaRegistry.catalogueJson()
        assertTrue(catalogue.contains("pl.owd.person.disability-status"))
        assertTrue(catalogue.contains("pl.owd.civic.mandate-status"))
        assertFalse(catalogue.contains("mobywatel.gov.pl"))
    }
}
