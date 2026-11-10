// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
        if (!Files.isRegularFile(pdf) || !Files.isReadable(pdf)) {
            throw new IOException("PDF file is missing or unreadable");
        }
        long size = Files.size(pdf);
        if (size < 8 || size > MAX_PDF_BYTES) {
            throw new IOException("PDF size is outside the accepted range");
        }
        byte[] header = new byte[5];
        try (var input = Files.newInputStream(pdf)) {
            if (input.read(header) != header.length ||
                    !new String(header, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new IOException("Input is not a PDF document");
            }
        }

        SignedDocumentValidator validator =
                SignedDocumentValidator.fromDocument(new FileDocument(pdf.toFile()));
        validator.setCertificateVerifier(verifier);
        Reports reports = validator.validateDocument();
        return new ValidationReport(
                reports.getXmlSimpleReport(),
                reports.getXmlDetailedReport(),
                reports.getXmlValidationReport());
    }

    public record ValidationReport(
            String simpleReportXml,
            String detailedReportXml,
            String etsiValidationReportXml) {
        public ValidationReport {
            Objects.requireNonNull(simpleReportXml);
            Objects.requireNonNull(detailedReportXml);
            Objects.requireNonNull(etsiValidationReportXml);
        }
    }
}
