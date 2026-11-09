// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

/** Stable boundary to a future formally approved mObywatel integration. */
internal enum class DataProvenance { LOCAL_USER, PEER_SHARED, ISSUER_ATTESTED, GOVERNMENT_SERVICE }

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
 * Mapping is separate from the OWD registry. Entries may be installed only
 * after the operator approves the target schema and the organization.
 */
internal data class ApprovedSchemaMapping(
    val owdSchemaId: String,
    val owdSchemaVersion: Int,
    val governmentSchemaId: String,
    val approvalReference: String
)

internal data class GovernmentIntegrationReadiness(
    val approved: Boolean,
    val certificateProvisioned: Boolean,
    val backendRegistered: Boolean,
    /** Exact target schema identifiers supplied by the approved operator. */
    val supportedSchemas: Set<String>,
    val explanation: String
) {
    val canTransfer: Boolean
        get() = approved && certificateProvisioned && backendRegistered && supportedSchemas.isNotEmpty()
}

internal object GovernmentSchemaMapper {
    fun map(
        record: ShareableRecord,
        mapping: ApprovedSchemaMapping,
        readiness: GovernmentIntegrationReadiness
    ): Pair<String, Map<String, String>> {
        OwdSchemaRegistry.requireValid(record)
        require(readiness.canTransfer) { "Integracja rządowa nie jest gotowa: ${readiness.explanation}" }
        require(mapping.owdSchemaId == record.schema) { "Mapowanie dotyczy innego schematu OWD." }
        require(mapping.owdSchemaVersion == OwdSchemaRegistry.SCHEMA_VERSION) {
            "Brak mapowania dla tej wersji schematu OWD."
        }
        require(mapping.approvalReference.isNotBlank()) { "Mapowanie nie ma podstawy zatwierdzenia." }
        require(mapping.governmentSchemaId in readiness.supportedSchemas) {
            "Docelowy schemat nie znajduje się na liście schematów dopuszczonych przez operatora."
        }
        // Field names are never guessed or silently translated. A reviewed,
        // explicit field-level transformation is required when the target schema is known.
        return mapping.governmentSchemaId to record.fields.toMap()
    }
}

/**
 * Implement only after written approval, current technical specifications,
 * production certificates, security assessment and data-controller review.
 */
internal interface GovernmentMobywatelAdapter {
    suspend fun checkReadiness(): GovernmentIntegrationReadiness
    suspend fun submit(records: List<ShareableRecord>, consent: TransferConsent): TransferReceipt
}

/** No implementation or government schema mapping is registered in the alpha build. */
