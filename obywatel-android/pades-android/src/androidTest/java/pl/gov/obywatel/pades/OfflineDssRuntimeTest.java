// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.pades;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineDssRuntimeTest {

    @Test
    public void localVerifierAndPadesServiceCanBeCreatedWithoutNetworkConfiguration() {
        CommonCertificateVerifier verifier = OfflineDssRuntime.newLocalVerifier();
        assertNotNull(verifier);
        PAdESService service = OfflineDssRuntime.newLocalPadesService(verifier);
        assertNotNull(service);
        assertEquals("LOCAL_ONLY_NO_NETWORK_SOURCES_CONFIGURED", OfflineDssRuntime.runtimeMode());
    }
}
