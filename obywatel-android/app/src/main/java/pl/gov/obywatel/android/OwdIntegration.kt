// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

/**
 * Stable boundary between the civic data model and a future, formally approved
 * government mObywatel integration. This is an architectural contract only:
 * no endpoint, certificate, token or state-system access is implemented here.
 */
internal enum class DataProvenance {
    LOCAL_USER,
    PEER_SHARED,
    ISSUER_ATTESTED,
    GOVERNMENT_SERVICE
}

internal data class ShareableRecord(
    val schema: String,
    val fields: Map<String, String>,
    val provenance: DataProvenance,
    val issuerId: String? = null,
    val evidenceReference: String? = null,
    val issuedAtEpochSeconds: Long? = null,
    val validUntilEpochSeconds: Long? = null
)

internal data class TransferConsent(
    val recipientId: String,
    val purpose: String,
    val selectedFieldNames: Set<String>,
    val channelId: String,
    val expiresAtEpochSeconds: Long
)

internal data class TransferReceipt(
    val transactionId: String,
    val recipientId: String,
    val acceptedFieldNames: Set<String>,
    val receivedAtEpochSeconds: Long,
    val receiptEvidence: String?
)

/**
 * Implement only after written approval, current technical specifications,
 * production certificates, security assessment and data-controller review.
 * The adapter must reject unsupported schemas and preserve provenance.
 */
internal interface GovernmentMobywatelAdapter {
    suspend fun checkReadiness(): GovernmentIntegrationReadiness
    suspend fun submit(
        records: List<ShareableRecord>,
        consent: TransferConsent
    ): TransferReceipt
}

internal data class GovernmentIntegrationReadiness(
    val approved: Boolean,
    val certificateProvisioned: Boolean,
    val backendRegistered: Boolean,
    val supportedSchemas: Set<String>,
    val explanation: String
) {
    val canTransfer: Boolean
        get() = approved && certificateProvisioned && backendRegistered && supportedSchemas.isNotEmpty()
}

/** No implementation is registered in the alpha build. */
