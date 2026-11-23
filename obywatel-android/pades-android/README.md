# SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
# SPDX-License-Identifier: EUPL-1.2

# Offline PAdES Android module

This module is the Android packaging and runtime boundary for the local-first PAdES path.

## Design

- The PDF and signing key stay on the device during local signing.
- DSS prepares PAdES DTBS and finalizes the signed PDF.
- Android Keystore signs DTBS bytes with the non-exportable EC key.
- `:pades-validator` is reused as the shared DSS implementation.
- `OfflineEvidenceStore` caches validation material encrypted with AES-256-GCM under an Android Keystore key.
- No HTTP client or network source is configured by `OfflineDssRuntime`.

## Offline evidence

Supported cache types: EU LOTL, Member State Trusted Lists, CRL, OCSP responses, TSA tokens, validation policies and trust-anchor bundles.

The cache retains expired material for historical diagnostics, but marks it `fresh=false`. A caller must never interpret stale or missing revocation evidence as GOOD. Evidence must be cryptographically validated before being trusted or used to build a trust path.

## Current status

The module is an integration probe, not yet a production-ready Android DSS runtime. DSS 6.5.1's PAdES backend uses PDFBox; successful Gradle/D8 packaging alone does not prove that all PDFBox runtime paths are compatible with Android. Device-level tests must exercise actual PDF preparation, PAdES finalization, signature validation, ServiceLoader provider discovery and offline report semantics.

If the stock PDFBox backend fails on Android, do not silently downgrade to an old DSS fork. Implement and test a maintained Android PDF backend/adapter while keeping DSS core and validation semantics current.
