// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import org.junit.Assert.assertThrows
import org.junit.Test

class X509CertificateServiceTest {
    @Test
    fun rejectsEmptyCertificateInput() {
        assertThrows(IllegalArgumentException::class.java) {
            X509CertificateService.parseCertificates(byteArrayOf())
        }
    }

    @Test
    fun rejectsOversizedCertificateBundleBeforeParsing() {
        assertThrows(IllegalArgumentException::class.java) {
            X509CertificateService.parseCertificates(ByteArray(5 * 1024 * 1024 + 1))
        }
    }

    @Test
    fun rejectsNonCertificateBytes() {
        assertThrows(Exception::class.java) {
            X509CertificateService.parseCertificates("not a certificate".toByteArray())
        }
    }
}
