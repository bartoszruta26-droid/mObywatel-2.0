// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.security.CertPathBuilder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertStore
import java.security.cert.CollectionCertStoreParameters
import java.security.cert.PKIXBuilderParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.Date
import javax.security.auth.x500.X500Principal

/** Dane prezentowane użytkownikowi po odczycie certyfikatu X.509. */
data class X509CertificateDetails(
    val subject: String,
    val issuer: String,
    val serialNumberHex: String,
    val notBefore: Date,
    val notAfter: Date,
    val sha256Fingerprint: String,
    val signatureAlgorithm: String,
    val publicKeyAlgorithm: String,
    val keyUsage: List<String>,
    val subjectAlternativeNames: List<String>,
    val currentlyValid: Boolean
)

data class X509PathValidation(
    val trusted: Boolean,
    val validationTime: Date,
    val certificatePathSubjects: List<String>,
    val trustAnchorSubject: String?,
    val revocationChecked: Boolean,
    val message: String
)

/**
 * Android-side X.509 primitives.
 *
 * Chain validation is PKIX path building against caller-supplied trust anchors.
 * Revocation is deliberately disabled here: production validation must use the
 * backend DSS verifier configured with trusted lists, OCSP and CRL.
 */
object X509CertificateService {
    private const val MAX_CERTIFICATE_BUNDLE_BYTES = 5 * 1024 * 1024
    private const val MAX_CERTIFICATES = 20
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val keyUsageNames = listOf(
        "podpis cyfrowy", "zaprzeczalność", "szyfrowanie klucza",
        "szyfrowanie danych", "uzgadnianie klucza", "podpis certyfikatu CA",
        "podpis CRL", "ograniczenie encipherOnly", "ograniczenie decipherOnly"
    )

    fun parseCertificates(encoded: ByteArray): List<X509Certificate> {
        require(encoded.isNotEmpty() && encoded.size <= MAX_CERTIFICATE_BUNDLE_BYTES) {
            "Plik certyfikatu jest pusty lub przekracza limit 5 MiB."
        }
        val factory = java.security.cert.CertificateFactory.getInstance("X.509")
        val parsed = factory.generateCertificates(ByteArrayInputStream(encoded))
            .filterIsInstance<X509Certificate>()
        require(parsed.isNotEmpty()) { "Nie znaleziono certyfikatu X.509 (PEM lub DER)." }
        require(parsed.size <= MAX_CERTIFICATES) { "Pakiet zawiera zbyt wiele certyfikatów." }
        return parsed
    }

    fun inspect(certificate: X509Certificate, now: Date = Date()): X509CertificateDetails {
        val usage = certificate.keyUsage?.mapIndexedNotNull { index, enabled ->
            if (enabled) keyUsageNames.getOrNull(index) else null
        }.orEmpty()
        val sans = runCatching {
            certificate.subjectAlternativeNames.orEmpty().mapNotNull { entry ->
                if (entry.size >= 2) "${entry[0]}: ${entry[1]}" else null
            }
        }.getOrDefault(emptyList())
        val fingerprint = java.security.MessageDigest.getInstance("SHA-256")
            .digest(certificate.encoded).joinToString(":") { "%02X".format(it) }
        val valid = now >= certificate.notBefore && now <= certificate.notAfter
        return X509CertificateDetails(
            subject = certificate.subjectX500Principal.name,
            issuer = certificate.issuerX500Principal.name,
            serialNumberHex = certificate.serialNumber.toString(16).uppercase(),
            notBefore = certificate.notBefore,
            notAfter = certificate.notAfter,
            sha256Fingerprint = fingerprint,
            signatureAlgorithm = certificate.sigAlgName,
            publicKeyAlgorithm = certificate.publicKey.algorithm,
            keyUsage = usage,
            subjectAlternativeNames = sans,
            currentlyValid = valid
        )
    }

    /**
     * Builds a PKIX path to one of the explicitly supplied trust anchors.
     * The caller must obtain anchors from a trusted source; importing a root
     * certificate from the same untrusted file must never silently establish trust.
     */
    fun validatePath(
        leaf: X509Certificate,
        intermediates: Collection<X509Certificate>,
        trustAnchors: Set<TrustAnchor>,
        validationTime: Date = Date()
    ): X509PathValidation {
        require(trustAnchors.isNotEmpty()) { "Brak zaufanych kotwic certyfikacji." }
        val certificates = (listOf(leaf) + intermediates)
            .distinctBy { it.serialNumber.toString() + it.issuerX500Principal.name }
        val selector = X509CertSelector().apply { certificate = leaf }
        val parameters = PKIXBuilderParameters(trustAnchors, selector).apply {
            date = validationTime
            isRevocationEnabled = false
            addCertStore(CertStore.getInstance(
                "Collection", CollectionCertStoreParameters(certificates)
            ))
        }
        return try {
            val result = CertPathBuilder.getInstance("PKIX")
                .build(parameters) as java.security.cert.PKIXCertPathBuilderResult
            val pathSubjects = result.certPath.certificates
                .filterIsInstance<X509Certificate>()
                .map { it.subjectX500Principal.name }
            X509PathValidation(
                trusted = true,
                validationTime = validationTime,
                certificatePathSubjects = pathSubjects,
                trustAnchorSubject = result.trustAnchor.trustedCert?.subjectX500Principal?.name
                    ?: result.trustAnchor.caName,
                revocationChecked = false,
                message = "Ścieżka PKIX prowadzi do wskazanej kotwicy zaufania. Unieważnienie OCSP/CRL nie było sprawdzane."
            )
        } catch (error: Exception) {
            X509PathValidation(
                trusted = false,
                validationTime = validationTime,
                certificatePathSubjects = emptyList(),
                trustAnchorSubject = null,
                revocationChecked = false,
                message = "Nie udało się zbudować zaufanej ścieżki PKIX: ${error.message ?: error.javaClass.simpleName}"
            )
        }
    }

    /** Generates a non-exportable P-256 signing key in Android Keystore. */
    fun generateSigningKey(alias: String, requireUserAuthentication: Boolean = true): KeyPair {
        require(alias.matches(Regex("[A-Za-z0-9._-]{1,80}"))) { "Nieprawidłowy alias klucza." }
        val store = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        require(!store.containsAlias(alias)) { "Klucz o tym aliasie już istnieje." }
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(requireUserAuthentication)
            .build()
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    fun getSigningKeyPair(alias: String): KeyPair {
        require(alias.matches(Regex("[A-Za-z0-9._-]{1,80}"))) { "Nieprawidłowy alias klucza." }
        val store = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val privateKey = store.getKey(alias, null) as? java.security.PrivateKey
            ?: error("Nie znaleziono klucza prywatnego w Android Keystore.")
        val certificate = store.getCertificate(alias)
            ?: error("Brak publicznej części klucza w Android Keystore.")
        return KeyPair(certificate.publicKey, privateKey)
    }

    /**
     * Creates a PKCS#10 CSR signed by the non-exportable Android Keystore key.
     * P-256 / SHA256withECDSA is used. The private key is never serialized.
     */
    fun createCsr(alias: String, subject: String): ByteArray {
        val pair = getSigningKeyPair(alias)
        require(pair.public.algorithm.equals("EC", ignoreCase = true)) {
            "CSR obsługuje obecnie klucze EC P-256."
        }
        val subjectDer = X500Principal(subject).encoded
        val certificationRequestInfo = Der.sequence(
            Der.integerZero(),
            subjectDer,
            pair.public.encoded,
            Der.tlv(0xA0, byteArrayOf())
        )
        val algorithmIdentifier = Der.sequence(
            Der.oid(byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x04, 0x03, 0x02))
        )
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(pair.private)
        signer.update(certificationRequestInfo)
        val signature = signer.sign()
        return Der.sequence(
            certificationRequestInfo,
            algorithmIdentifier,
            Der.tlv(0x03, byteArrayOf(0) + signature)
        )
    }

    fun csrPem(csrDer: ByteArray): String {
        require(csrDer.isNotEmpty() && csrDer.size <= 64 * 1024) { "Nieprawidłowy rozmiar CSR." }
        val encoded = android.util.Base64.encodeToString(
            csrDer, android.util.Base64.NO_WRAP
        ).chunked(64).joinToString("\n")
        return "-----BEGIN CERTIFICATE REQUEST-----\n$encoded\n-----END CERTIFICATE REQUEST-----\n"
    }

    /** Confirms that a CA-issued certificate contains this Keystore key's public key. */
    fun requireIssuedCertificateMatchesKey(alias: String, certificate: X509Certificate) {
        val pair = getSigningKeyPair(alias)
        require(pair.public.encoded.contentEquals(certificate.publicKey.encoded)) {
            "Certyfikat wystawiony przez CA nie odpowiada kluczowi z Android Keystore."
        }
    }

    private object Der {
        fun integerZero(): ByteArray = tlv(0x02, byteArrayOf(0))
        fun sequence(vararg values: ByteArray): ByteArray = tlv(0x30, values.fold(ByteArray(0)) { a, b -> a + b })
        fun oid(value: ByteArray): ByteArray = tlv(0x06, value)
        fun tlv(tag: Int, value: ByteArray): ByteArray = byteArrayOf(tag.toByte()) + length(value.size) + value
        private fun length(size: Int): ByteArray {
            require(size >= 0)
            if (size < 128) return byteArrayOf(size.toByte())
            var n = size
            val bytes = ArrayList<Byte>()
            while (n > 0) {
                bytes.add(0, (n and 0xff).toByte())
                n = n ushr 8
            }
            return byteArrayOf((0x80 or bytes.size).toByte()) + bytes.toByteArray()
        }
    }
}
