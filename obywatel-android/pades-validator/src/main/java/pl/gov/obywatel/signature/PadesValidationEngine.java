// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * JVM-side PAdES validation engine. Host it behind an authenticated backend API;
 * do not package the DSS validation stack into the Android APK.
 */
public final class PadesValidationEngine {
    public static final long MAX_PDF_BYTES = 50L * 1024L * 1024L;

    private PadesValidationEngine() {}

    public static ValidationReport validate(Path pdf, CommonCertificateVerifier verifier)
            throws IOException {
        Objects.requireNonNull(pdf, "pdf");
        Objects.requireNonNull(verifier, "verifier");

        Path stagedPdf = stageAndValidatePdf(pdf);
        try {
            SignedDocumentValidator validator =
                    SignedDocumentValidator.fromDocument(new FileDocument(stagedPdf.toFile()));
            validator.setCertificateVerifier(verifier);
            Reports reports = validator.validateDocument();
            SimpleReport simple = reports.getSimpleReport();
            List<SignatureSummary> signatures = new ArrayList<>();
            for (String signatureId : simple.getSignatureIdList()) {
                var chain = simple.getCertificateChain(signatureId);
                List<String> certificateNames = chain == null
                        ? List.of()
                        : chain.getCertificate().stream()
                            .map(certificate -> certificate.getQualifiedName()
                                    + (certificate.isTrusted() ? " [trusted]" : " [not trusted]"))
                            .toList();
                List<CertificateSummary> certificateDetails = reports.getDiagnosticData()
                        .getSignatureCertificateChain(signatureId).stream()
                        .map(certificate -> new CertificateSummary(
                                certificate.getId(),
                                certificate.getCertificateDN(),
                                certificate.getCertificateIssuerDN(),
                                certificate.getSerialNumber(),
                                toIso(certificate.getNotBefore()),
                                toIso(certificate.getNotAfter()),
                                certificate.isTrusted(),
                                Objects.toString(reports.getDiagnosticData()
                                        .getCertificateRevocationStatus(certificate.getId()), "UNKNOWN"),
                                Objects.toString(reports.getDiagnosticData()
                                        .getCertificateRevocationSource(certificate.getId()), "UNKNOWN"),
                                reports.getDiagnosticData().isValidCertificate(certificate.getId())))
                        .toList();
                signatures.add(new SignatureSummary(
                        signatureId,
                        reports.getDiagnosticData().isBLevelTechnicallyValid(signatureId),
                        simple.isValid(signatureId),
                        reports.getDiagnosticData().getSignatureById(signatureId) != null
                                && reports.getDiagnosticData().getSignatureById(signatureId).isTrustedChain(),
                        Objects.toString(simple.getIndication(signatureId), "UNKNOWN"),
                        Objects.toString(simple.getSubIndication(signatureId), "UNKNOWN"),
                        Objects.toString(simple.getSignatureQualification(signatureId), "UNKNOWN"),
                        Objects.toString(simple.getSignatureFormat(signatureId), "UNKNOWN"),
                        simple.getSignedBy(signatureId),
                        toIso(simple.getSigningTime(signatureId)),
                        toIso(simple.getBestSignatureTime(signatureId)),
                        certificateNames,
                        certificateDetails));
            }
            List<TimestampSummary> timestamps = new ArrayList<>();
            for (String timestampId : simple.getTimestampIdList()) {
                timestamps.add(new TimestampSummary(
                        timestampId,
                        simple.getProducedBy(timestampId),
                        toIso(simple.getProductionTime(timestampId)),
                        Objects.toString(simple.getTimestampQualification(timestampId), "UNKNOWN"),
                        simple.isValid(timestampId)));
            }
            return new ValidationReport(
                    reports.getXmlSimpleReport(),
                    reports.getXmlDetailedReport(),
                    reports.getXmlDiagnosticData(),
                    reports.getXmlValidationReport(),
                    toIso(simple.getValidationTime()),
                    signatures,
                    timestamps);
        } finally {
            Files.deleteIfExists(stagedPdf);
        }
    }

    /**
     * Copy once, with a streaming bound, and validate the staged bytes that DSS
     * will actually consume. NOFOLLOW_LINKS prevents accepting a symlink as input.
     */
    private static Path stageAndValidatePdf(Path source) throws IOException {
        if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
                || !Files.isReadable(source)) {
            throw new IOException("PDF file is missing, not regular, or unreadable");
        }

        Path staged = Files.createTempFile("owd-pades-validation-", ".pdf");
        boolean complete = false;
        try (InputStream input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS);
             OutputStream output = Files.newOutputStream(
                     staged, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] header = input.readNBytes(5);
            if (header.length != 5
                    || !new String(header, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new IOException("Input is not a PDF document");
            }

            long total = header.length;
            output.write(header);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_PDF_BYTES) {
                    throw new IOException("PDF exceeds the 50 MiB limit");
                }
                output.write(buffer, 0, read);
            }
            if (total < 8) {
                throw new IOException("PDF size is outside the accepted range");
            }
            complete = true;
            return staged;
        } finally {
            if (!complete) {
                Files.deleteIfExists(staged);
            }
        }
    }

    private static String toIso(Date value) {
        return value == null ? null : value.toInstant().toString();
    }

    public record SignatureSummary(
            String signatureId,
            boolean cryptographicSignatureValid,
            boolean dssValid,
            boolean certificateChainTrusted,
            String indication,
            String subIndication,
            String qualification,
            String padesLevel,
            String signerIdentity,
            String signingTime,
            String bestSignatureTime,
            List<String> certificateChainNames,
            List<CertificateSummary> certificates) {
        public SignatureSummary {
            certificateChainNames = List.copyOf(certificateChainNames);
            certificates = List.copyOf(certificates);
        }
    }

    public record CertificateSummary(
            String certificateId,
            String subject,
            String issuer,
            String serialNumber,
            String notBefore,
            String notAfter,
            boolean trusted,
            String revocationStatus,
            String revocationSource,
            boolean certificateAndRevocationValid) {}

    public record TimestampSummary(
            String timestampId,
            String authority,
            String producedAt,
            String qualification,
            boolean dssValid) {}

    public record ValidationReport(
            String simpleReportXml,
            String detailedReportXml,
            String diagnosticDataXml,
            String etsiValidationReportXml,
            String validationTime,
            List<SignatureSummary> signatures,
            List<TimestampSummary> timestamps) {
        public ValidationReport {
            Objects.requireNonNull(simpleReportXml);
            Objects.requireNonNull(detailedReportXml);
            Objects.requireNonNull(diagnosticDataXml);
            Objects.requireNonNull(etsiValidationReportXml);
            signatures = List.copyOf(signatures);
            timestamps = List.copyOf(timestamps);
        }
    }
}
