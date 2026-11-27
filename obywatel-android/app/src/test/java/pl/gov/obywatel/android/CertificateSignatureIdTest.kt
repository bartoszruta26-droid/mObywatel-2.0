// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificateSignatureIdTest {
    @Test
    fun acceptsOnlyGeneratedUuidStyleIdentifiers() {
        assertTrue(isValidCertificateSignatureId("550e8400-e29b-41d4-a716-446655440000"))
        assertTrue(isValidCertificateSignatureId("a1B2-c3D4"))
    }

    @Test
    fun rejectsPathTraversalAndMalformedIdentifiers() {
        listOf(
            "", ".", "..", "../outside", "../../shared_prefs/prefs",
            "/absolute/path", "name.enc", "id\\name", "id with spaces",
            "id?query", "x".repeat(81)
        ).forEach { id ->
            assertFalse("Identifier should be rejected: $id", isValidCertificateSignatureId(id))
        }
    }
}
