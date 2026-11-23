// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.signature;

import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertThrows;

public class PadesSigningEngineTest {

    @Test
    public void rejectsMissingAuthenticatedOwner() {
        PadesSigningEngine engine = new PadesSigningEngine();
        assertThrows(IllegalArgumentException.class, () ->
                engine.prepare(" ", new byte[0], null, null, null));
    }

    @Test
    public void rejectsNonPdfBeforeCertificateProcessing() {
        PadesSigningEngine engine = new PadesSigningEngine();
        assertThrows(IOException.class, () ->
                engine.prepare("authenticated-user", "not a pdf".getBytes(),
                        null, null, new CommonCertificateVerifier()));
    }

    @Test
    public void rejectsPdfWithInvalidHeader() {
        PadesSigningEngine engine = new PadesSigningEngine();
        assertThrows(IOException.class, () ->
                engine.prepare("authenticated-user", "XXXXX123".getBytes(),
                        null, null, new CommonCertificateVerifier()));
    }
}
