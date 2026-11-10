// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.fail;

public class PadesValidationEngineTest {

    @Test
    public void rejectsMissingFile() throws Exception {
        Path missing = Files.createTempDirectory("pades-test").resolve("missing.pdf");
        try {
            expectIOException(() ->
                    PadesValidationEngine.validate(missing, new CommonCertificateVerifier()));
        } finally {
            Files.deleteIfExists(missing.getParent());
        }
    }

    @Test
    public void rejectsEmptyFile() throws Exception {
        Path pdf = Files.createTempFile("pades-empty", ".pdf");
        try {
            expectIOException(() ->
                    PadesValidationEngine.validate(pdf, new CommonCertificateVerifier()));
        } finally {
            Files.deleteIfExists(pdf);
        }
    }

    @Test
    public void rejectsNonPdfHeader() throws Exception {
        Path pdf = Files.createTempFile("pades-invalid", ".pdf");
        try {
            Files.writeString(pdf, "NOT A PDF DOCUMENT");
            expectIOException(() ->
                    PadesValidationEngine.validate(pdf, new CommonCertificateVerifier()));
        } finally {
            Files.deleteIfExists(pdf);
        }
    }

    private static void expectIOException(IoAction action) throws Exception {
        try {
            action.run();
            fail("Expected IOException");
        } catch (IOException expected) {
            // Expected input validation failure.
        }
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws Exception;
    }
}
