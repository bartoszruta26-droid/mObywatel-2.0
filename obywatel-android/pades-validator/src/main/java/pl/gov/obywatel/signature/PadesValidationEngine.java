// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.model.FileDocument;
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
            return new ValidationReport(
                    reports.getXmlSimpleReport(),
                    reports.getXmlDetailedReport(),
                    reports.getXmlValidationReport());
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
