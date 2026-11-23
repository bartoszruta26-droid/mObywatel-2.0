// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.pades;

import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;

/**
 * Local DSS runtime. It deliberately has no HTTP client, remote endpoint,
 * credential, or implicit network dependency. A caller must explicitly attach
 * cached trust material and revocation evidence before claiming a trusted result.
 *
 * This class is an Android packaging/compatibility probe until the PDF backend
 * has passed device-level tests. Do not present it as a production signer yet.
 */
public final class OfflineDssRuntime {
    private OfflineDssRuntime() {}

    public static CommonCertificateVerifier newLocalVerifier() {
        return new CommonCertificateVerifier();
    }

    public static PAdESService newLocalPadesService(CommonCertificateVerifier verifier) {
        if (verifier == null) {
            throw new IllegalArgumentException("A local certificate verifier is required");
        }
        return new PAdESService(verifier);
    }

    public static String runtimeMode() {
        return "LOCAL_ONLY_NO_NETWORK_SOURCES_CONFIGURED";
    }
}
