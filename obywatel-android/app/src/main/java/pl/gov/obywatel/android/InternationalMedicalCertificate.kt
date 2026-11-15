// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

/**
 * Domain model for a multilingual international medical certificate.
 *
 * This model deliberately keeps source text and machine translations separate.
 * Persistence, remote translation and PDF generation are implemented by later
 * stages; no patient data is sent to a network provider from this model.
 */
internal const val INTERNATIONAL_MEDICAL_CERTIFICATE_ID = "international_medical_certificate"
internal const val INTERNATIONAL_MEDICAL_CERTIFICATE_SCHEMA_VERSION = 1

internal data class MedicalCertificateLanguage(
    val code: String,
    val englishName: String,
    val selectableAsSource: Boolean = true,
    val selectableAsTarget: Boolean = true
)

internal object MedicalCertificateLanguages {
    val supported: List<MedicalCertificateLanguage> = listOf(
        MedicalCertificateLanguage("en", "English"),
        MedicalCertificateLanguage("he", "Hebrew"),
        MedicalCertificateLanguage("de", "German"),
        MedicalCertificateLanguage("es", "Spanish"),
        MedicalCertificateLanguage("fr", "French"),
        MedicalCertificateLanguage("it", "Italian"),
        MedicalCertificateLanguage("pl", "Polish"),
        MedicalCertificateLanguage("bg", "Bulgarian"),
        MedicalCertificateLanguage("ro", "Romanian"),
        MedicalCertificateLanguage("nl", "Dutch"),
        MedicalCertificateLanguage("nl-BE", "Dutch (Belgium)"),
        MedicalCertificateLanguage("fr-BE", "French (Belgium)"),
        MedicalCertificateLanguage("de-BE", "German (Belgium)"),
        MedicalCertificateLanguage("pt", "Portuguese"),
        MedicalCertificateLanguage("sv", "Swedish")
    )

    fun find(code: String): MedicalCertificateLanguage? =
        supported.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun isRightToLeft(code: String): Boolean =
        code.substringBefore('-').equals("he", ignoreCase = true)

    fun normalizeOtherLanguage(codeOrName: String): String =
        codeOrName.trim().take(35)
}

internal enum class MedicalTranslationStatus {
    PENDING,
    MACHINE_TRANSLATED,
    REVIEWED,
    APPROVED,
    FAILED,
    STALE
}

internal data class MedicalFieldTranslation(
    val fieldKey: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val sourceText: String,
    val translatedText: String,
    val status: MedicalTranslationStatus,
    val providerId: String? = null,
    val translatedAtEpochMillis: Long? = null,
    val reviewer: String? = null
)

internal enum class MedicalImagingModality(val label: String) {
    X_RAY("X-ray"),
    MRI("MRI"),
    CT("CT"),
    ULTRASOUND("Ultrasound"),
    OTHER("Other")
}

internal data class MedicalImagingStudy(
    val id: String,
    val modality: MedicalImagingModality,
    val bodyRegion: String = "",
    val studyDate: String = "",
    val facility: String = "",
    val indication: String = "",
    val technique: String = "",
    val contrast: String = "",
    val findings: String = "",
    val impression: String = "",
    val radiologistName: String = "",
    val reportNumber: String = "",
    val attachmentIds: List<String> = emptyList()
)

internal data class MedicalCertificateSignatureReferences(
    val patientHandwrittenScanId: String? = null,
    val clinicianHandwrittenScanId: String? = null,
    val medicalSealScanId: String? = null
)

internal data class InternationalMedicalCertificate(
    val schemaVersion: Int = INTERNATIONAL_MEDICAL_CERTIFICATE_SCHEMA_VERSION,
    val documentNumber: String = "",
    val sourceLanguage: String = "en",
    val otherSourceLanguage: String = "",
    val targetLanguages: Set<String> = emptySet(),
    val sourceFields: Map<String, String> = emptyMap(),
    val translations: List<MedicalFieldTranslation> = emptyList(),
    val imagingStudies: List<MedicalImagingStudy> = emptyList(),
    val signatures: MedicalCertificateSignatureReferences = MedicalCertificateSignatureReferences()
) {
    fun translationsFor(targetLanguage: String): List<MedicalFieldTranslation> =
        translations.filter { it.targetLanguage.equals(targetLanguage, ignoreCase = true) }

    fun hasCurrentTranslation(fieldKey: String, targetLanguage: String): Boolean =
        translations.any {
            it.fieldKey == fieldKey &&
                it.targetLanguage.equals(targetLanguage, ignoreCase = true) &&
                it.sourceText == sourceFields[fieldKey] &&
                it.status in setOf(MedicalTranslationStatus.MACHINE_TRANSLATED,
                    MedicalTranslationStatus.REVIEWED, MedicalTranslationStatus.APPROVED)
        }

    fun withUpdatedSourceField(fieldKey: String, value: String): InternationalMedicalCertificate {
        require(fieldKey.matches(Regex("[A-Za-z][A-Za-z0-9_]{0,63}"))) {
            "Invalid medical certificate field key."
        }
        val updated = sourceFields + (fieldKey to value)
        val invalidated = translations.map { translation ->
            if (translation.fieldKey == fieldKey && translation.sourceText != value) {
                translation.copy(status = MedicalTranslationStatus.STALE)
            } else translation
        }
        return copy(sourceFields = updated, translations = invalidated)
    }

    fun validate(): List<String> = buildList {
        if (schemaVersion != INTERNATIONAL_MEDICAL_CERTIFICATE_SCHEMA_VERSION) {
            add("Unsupported medical certificate schema version.")
        }
        if (sourceLanguage.isBlank()) add("Original language is required.")
        if (sourceLanguage == "other" && otherSourceLanguage.isBlank()) {
            add("Specify the original language.")
        }
        if (targetLanguages.any { it.isBlank() }) add("Target language codes must not be blank.")
        if (translations.any { it.fieldKey !in sourceFields }) {
            add("A translation references a field absent from the source document.")
        }
        if (imagingStudies.any { it.id.isBlank() }) add("Every imaging study requires a stable identifier.")
    }
}

/** Implementations must request explicit consent before transmitting clinical data externally. */
internal interface MedicalTranslationProvider {
    val providerId: String
    suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String
}
