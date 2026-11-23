// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import java.security.Signature
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class X509KeystoreSigningInstrumentedTest {

    @Test
    fun nonExportableEcKeySignsDataThatVerifiesWithItsPublicKey() {
        val alias = "pades-test-${UUID.randomUUID()}"
        val dataToSign = "DSS-PAdES-DTBS-test".toByteArray(Charsets.UTF_8)
        try {
            val generated = X509CertificateService.generateSigningKey(
                alias, requireUserAuthentication = false
            )
            val signatureBytes = X509CertificateService.signPadesData(alias, dataToSign)
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(generated.public)
            verifier.update(dataToSign)
            assertTrue("Android Keystore ECDSA signature must verify", verifier.verify(signatureBytes))
            assertTrue("Private key must not be exportable", generated.private.encoded == null)
        } finally {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                deleteEntry(alias)
            }
        }
    }
}
