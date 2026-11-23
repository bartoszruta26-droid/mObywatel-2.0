// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates the two DSS signing phases. The same API is called locally by
 * the Android facade or by a separately authenticated HTTP adapter: prepare
 * exact PAdES DTBS, receive an Android Keystore signature, then embed it into
 * the same prepared PDF revision.
 *
 * The session is short-lived, bounded, and bound to the authenticated owner ID.
 * A REST layer MUST derive ownerId from its authenticated principal, never from
 * an untrusted request field. No private key is accepted or stored here.
 */
public final class PadesSigningEngine {
    public static final long MAX_PDF_BYTES = 50L * 1024L * 1024L;
    private static final int MAX_ACTIVE_SESSIONS = 2;
    private static final long SESSION_TTL_MILLIS = 5L * 60L * 1000L;
    private static final int MAX_SIGNATURE_BYTES = 16 * 1024;

    private final ConcurrentHashMap<String, PreparedSession> sessions = new ConcurrentHashMap<>();

    public record SigningRequest(
            String sessionId,
            String dataToBeSignedBase64,
            String signatureAlgorithm,
            String digestAlgorithm,
            String signingCertificateSha256,
            long expiresAtEpochMillis) {}

    public record SignedPdf(byte[] pdfBytes, String signatureAlgorithm, String signerSubject) {
        public SignedPdf {
            pdfBytes = pdfBytes.clone();
        }
        @Override public byte[] pdfBytes() { return pdfBytes.clone(); }
    }

    private static final class PreparedSession {
        private final String ownerId;
        private final long createdAt;
        private final DSSDocument sourceDocument;
        private final PAdESSignatureParameters parameters;
        private final PAdESService service;
        private final ToBeSigned dataToSign;
        private final CertificateToken signingCertificate;

        private PreparedSession(
                String ownerId,
                long createdAt,
                DSSDocument sourceDocument,
                PAdESSignatureParameters parameters,
                PAdESService service,
                ToBeSigned dataToSign,
                CertificateToken signingCertificate) {
            this.ownerId = ownerId;
            this.createdAt = createdAt;
            this.sourceDocument = sourceDocument;
            this.parameters = parameters;
            this.service = service;
            this.dataToSign = dataToSign;
            this.signingCertificate = signingCertificate;
        }
    }

    public synchronized SigningRequest prepare(
            String authenticatedOwnerId,
            byte[] originalPdf,
            X509Certificate signingCertificate,
            List<X509Certificate> certificateChain,
            CommonCertificateVerifier certificateVerifier) throws IOException, GeneralSecurityException {
        requireOwner(authenticatedOwnerId);
        Objects.requireNonNull(originalPdf, "originalPdf");
        expireOldSessions();

        if (originalPdf.length < 8 || originalPdf.length > MAX_PDF_BYTES
                || !new String(originalPdf, 0, Math.min(originalPdf.length, 5), StandardCharsets.US_ASCII).equals("%PDF-")) {
            throw new IOException("Input must be a PDF between 8 bytes and 50 MiB");
        }
        Objects.requireNonNull(signingCertificate, "signingCertificate");
        Objects.requireNonNull(certificateChain, "certificateChain");
        Objects.requireNonNull(certificateVerifier, "certificateVerifier");
        if (sessions.size() >= MAX_ACTIVE_SESSIONS) {
            throw new IOException("Signing capacity is busy; retry after an active signing session expires");
        }
        signingCertificate.checkValidity();
        if (!"EC".equalsIgnoreCase(signingCertificate.getPublicKey().getAlgorithm())) {
            throw new IllegalArgumentException("This signing flow currently accepts EC certificates only");
        }
        boolean[] keyUsage = signingCertificate.getKeyUsage();
        if (keyUsage != null && (keyUsage.length == 0 || !keyUsage[0])) {
            throw new IllegalArgumentException("Signing certificate does not allow digitalSignature usage");
        }

        CertificateToken signingToken = new CertificateToken(signingCertificate);
        List<CertificateToken> dssChain = new ArrayList<>();
        dssChain.add(signingToken);
        for (X509Certificate certificate : certificateChain) {
            if (!MessageDigest.isEqual(certificate.getEncoded(), signingCertificate.getEncoded())) {
                dssChain.add(new CertificateToken(certificate));
            }
        }

        InMemoryDocument source = new InMemoryDocument(originalPdf.clone());
        source.setName("document-to-sign.pdf");
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(signingToken);
        parameters.setCertificateChain(dssChain);
        parameters.bLevel().setSigningDate(new Date());

        PAdESService service = new PAdESService(certificateVerifier);
        ToBeSigned dataToSign = service.getDataToSign(source, parameters);
        if (dataToSign == null || dataToSign.getBytes() == null || dataToSign.getBytes().length == 0) {
            throw new IOException("DSS returned empty data-to-be-signed");
        }

        String sessionId = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + SESSION_TTL_MILLIS;
        sessions.put(sessionId, new PreparedSession(
                authenticatedOwnerId, System.currentTimeMillis(), source,
                parameters, service, dataToSign, signingToken));

        String fingerprint = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(signingCertificate.getEncoded()));
        return new SigningRequest(
                sessionId,
                Base64.getEncoder().encodeToString(dataToSign.getBytes()),
                "ECDSA_SHA256",
                "SHA256",
                fingerprint,
                expiresAt);
    }

    public SignedPdf complete(
            String authenticatedOwnerId,
            String sessionId,
            byte[] signatureDer) throws IOException {
        requireOwner(authenticatedOwnerId);
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(signatureDer, "signatureDer");
        PreparedSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IOException("Signing session does not exist or has expired");
        }
        if (!session.ownerId.equals(authenticatedOwnerId)) {
            throw new SecurityException("Signing session belongs to another authenticated user");
        }
        if (!sessions.remove(sessionId, session)) {
            throw new IOException("Signing session has already been consumed");
        }
        if (System.currentTimeMillis() - session.createdAt > SESSION_TTL_MILLIS) {
            sessions.remove(sessionId, session);
            throw new IOException("Signing session has expired");
        }
        if (signatureDer.length == 0 || signatureDer.length > MAX_SIGNATURE_BYTES) {
            throw new IOException("Invalid ECDSA signature length");
        }

        SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.ECDSA_SHA256, signatureDer.clone());
        if (!session.service.isValidSignatureValue(
                session.dataToSign, signatureValue, session.signingCertificate)) {
            sessions.remove(sessionId, session);
            throw new SecurityException("Android Keystore signature does not match the prepared PAdES data");
        }

        DSSDocument signedDocument;
        try {
            signedDocument = session.service.signDocument(
                    session.sourceDocument, session.parameters, signatureValue);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            signedDocument.writeTo(output);
            byte[] signedBytes = output.toByteArray();
            if (signedBytes.length < 8
                    || !new String(signedBytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new IOException("DSS did not produce a valid PDF output");
            }
            return new SignedPdf(
                    signedBytes,
                    SignatureAlgorithm.ECDSA_SHA256.name(),
                    session.signingCertificate.getCertificate().getSubjectX500Principal().getName());
        } finally {
            sessions.remove(sessionId, session);
        }
    }

    public synchronized int activeSessionCount() {
        expireOldSessions();
        return sessions.size();
    }

    private synchronized void expireOldSessions() {
        long cutoff = System.currentTimeMillis() - SESSION_TTL_MILLIS;
        sessions.entrySet().removeIf(entry -> entry.getValue().createdAt < cutoff);
    }

    private static void requireOwner(String ownerId) {
        if (ownerId == null || ownerId.isBlank() || ownerId.length() > 256) {
            throw new IllegalArgumentException("Authenticated owner ID is required");
        }
    }
}
