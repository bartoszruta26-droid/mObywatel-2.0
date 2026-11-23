# SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
# SPDX-License-Identifier: EUPL-1.2

# OWD PAdES validation module

Experimental JVM-side validation component based on European Commission Digital Signature Services (DSS) **6.5.1**. It is intentionally separate from the Android APK: DSS validation, trust-list and certificate-validation dependencies belong in a controlled backend process.

## Implemented

- Bounded streaming of a regular PDF input into a private temporary file (maximum 50 MiB).
- PDF header validation on the exact staged bytes passed to DSS, avoiding a size-check / later-read race on the original path.
- DSS PAdES validation through `SignedDocumentValidator`.
- Two-phase PAdES Baseline-B signing coordinator:
  - `prepare(...)` validates the PDF input and prepares DSS `ToBeSigned` bytes with the supplied signing certificate and chain;
  - the short-lived signing session is bound to the authenticated owner ID and retains the exact PDF revision and DSS parameters;
  - Android signs the returned DTBS bytes using the non-exportable EC P-256 Android Keystore key with `SHA256withECDSA`;
  - `complete(...)` checks the returned ECDSA signature against the prepared DTBS and signing certificate, then asks DSS to embed the CMS signature in the prepared PDF revision;
  - session lifetime is five minutes, with at most two active sessions and a 50 MiB input bound.
- The original PDF is never overwritten by the signing coordinator; it returns a separate signed PDF byte array.
- Preservation of DSS Simple, Detailed and ETSI Validation Reports.
- Caller-supplied `CommonCertificateVerifier`, making trust anchors, EU LOTL/TL, OCSP/CRL sources and validation policy explicit.

## Required backend wiring before production

The backend must configure the verifier with:
1. Current European Commission LOTL and Member State Trusted Lists, including TLv6 (ETSI TS 119 612 v2.4.1).
2. Trusted-list signature validation and updated LOTL trust anchors.
3. Online OCSP and CRL sources with bounded timeouts, safe URL handling, caching and audit logging.
4. A documented validation policy and validation time, including historical validation where trusted timestamps are available.
5. A mapping from DSS indication/sub-indication and diagnostic data to OWD UI statuses. A cryptographically intact signature is not automatically qualified or trusted.

The signing coordinator is a JVM library API, not an HTTP server. A backend adapter still needs authenticated transport, CSRF/replay protection, TLS, request-size enforcement, owner binding from the authenticated principal, rate limiting, durable audit events, and controlled trust-list/OCSP/CRL/TSA configuration. Session state is in-memory and is intentionally lost on process restart.

The engine returns DSS reports without flattening evidence. It does **not** itself create an HTTP endpoint, provision trust anchors, schedule trusted-list refreshes, or establish a production trust policy. Do not expose it directly to untrusted network clients; wrap it in an authenticated, size-limited backend API and retain the original signed bytes and report digest for audit.

**Status:** isolated backend library; not production-ready and not connected to the Android UI. The tests cover input rejection and size bounds for validation and signing-session setup. A controlled integration test with valid and invalid PAdES fixtures, trusted-list refresh, revocation and timestamp scenarios remains required.

## Build and test

From `obywatel-android/`:

```sh
gradle --no-daemon :pades-validator:test
gradle --no-daemon :pades-validator:build
```

European Commission DSS documentation:
https://ec.europa.eu/digital-building-blocks/sites/spaces/DIGITAL/pages/467109107/Digital+Signature+Service+-+DSS
