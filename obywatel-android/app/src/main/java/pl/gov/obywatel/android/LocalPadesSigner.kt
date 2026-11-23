// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier
import pl.gov.obywatel.signature.PadesSigningEngine
import java.security.cert.X509Certificate
import java.util.Base64

/**
 * Executes the two DSS signing phases in this Android process.
 * No PDF, DTBS or signature bytes are sent to a backend.
 *
 * The runtime PDF implementation must pass device-level tests before this
 * class is exposed as a production feature.
 */
internal class LocalPadesSigner(private val keyAlias: String) {
    fun sign(
        originalPdf: ByteArray,
        verifier: CommonCertificateVerifier = CommonCertificateVerifier()
    ): PadesSigningEngine.SignedPdf {
        require(originalPdf.isNotEmpty()) { "Dokument PDF jest pusty." }
        val chain: List<X509Certificate> =
            X509CertificateService.getSigningCertificateChain(keyAlias)
        require(chain.isNotEmpty()) { "Klucz Keystore nie ma certyfikatu podpisującego." }

        val owner = "android-keystore:$keyAlias"
        val engine = PadesSigningEngine()
        val prepared = engine.prepare(owner, originalPdf, chain.first(), chain, verifier)
        val dataToBeSigned = Base64.getDecoder().decode(prepared.dataToBeSignedBase64())
        val signature = X509CertificateService.signPadesData(keyAlias, dataToBeSigned)
        return engine.complete(owner, prepared.sessionId(), signature)
    }
}
