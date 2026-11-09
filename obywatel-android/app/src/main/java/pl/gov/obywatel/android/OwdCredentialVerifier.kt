// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.util.Base64
import java.security.PublicKey
import java.security.Signature

/**
 * Verifies issuer-signed attestations against keys provisioned through a
 * separately trusted issuer directory. A key supplied by the credential itself
 * is never treated as a trust anchor.
 */
internal object OwdCredentialVerifier {
    data class SignedCredential(
        val issuerId: String,
        val schema: String,
        val issuedAtEpochSeconds: Long,
        val validUntilEpochSeconds: Long,
        val fields: Map<String, String>,
        val signatureBase64: String
    )

    data class Result(
        val valid: Boolean,
        val provenance: DataProvenance,
        val issuerId: String?,
        val reason: String
    )

    fun verify(
        credential: SignedCredential,
        trustedIssuerKeys: Map<String, PublicKey>,
        nowEpochSeconds: Long = System.currentTimeMillis() / 1000
    ): Result {
        val key = trustedIssuerKeys[credential.issuerId]
            ?: return Result(false, DataProvenance.LOCAL_USER, null, "Wystawca nie znajduje się na liście zaufanych.")
        if (credential.issuerId.isBlank() || credential.schema.isBlank() ||
            credential.issuedAtEpochSeconds > nowEpochSeconds + 60 ||
            credential.validUntilEpochSeconds <= nowEpochSeconds ||
            credential.validUntilEpochSeconds <= credential.issuedAtEpochSeconds
        ) return Result(false, DataProvenance.LOCAL_USER, null, "Poświadczenie jest nieaktualne lub ma nieprawidłowe daty.")

        return try {
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(key)
            verifier.update(canonicalBytes(credential))
            val signature = Base64.decode(
                credential.signatureBase64,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            if (verifier.verify(signature)) {
                Result(true, DataProvenance.ISSUER_ATTESTED, credential.issuerId, "Podpis wystawcy jest prawidłowy.")
            } else {
                Result(false, DataProvenance.LOCAL_USER, null, "Podpis wystawcy jest nieprawidłowy.")
            }
        } catch (error: Exception) {
            Result(false, DataProvenance.LOCAL_USER, null, "Nie można zweryfikować poświadczenia: ${error.javaClass.simpleName}.")
        }
    }

    /** Canonical, length-prefixed encoding prevents field-order and delimiter ambiguity. */
    fun canonicalBytes(credential: SignedCredential): ByteArray {
        val parts = listOf(
            "OWD-CREDENTIAL-v1",
            credential.issuerId,
            credential.schema,
            credential.issuedAtEpochSeconds.toString(),
            credential.validUntilEpochSeconds.toString()
        ) + credential.fields.toSortedMap().flatMap { listOf(it.key, it.value) }
        val output = java.io.ByteArrayOutputStream()
        val data = java.io.DataOutputStream(output)
        parts.forEach { value ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            data.writeInt(bytes.size)
            data.write(bytes)
        }
        data.flush()
        return output.toByteArray()
    }
}
