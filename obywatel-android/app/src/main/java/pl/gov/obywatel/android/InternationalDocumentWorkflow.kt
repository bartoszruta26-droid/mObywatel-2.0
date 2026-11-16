// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import java.time.LocalDate

/**
 * Workflow metadata for the 43 international catalogue entries.
 *
 * This metadata distinguishes an authority-issued original from a professional
 * certificate, a user-maintained record and an application. It deliberately
 * does not confer issuing authority on the app or its operator.
 */
internal enum class InternationalIssuerKind {
    HEALTH_PROFESSIONAL,
    HEALTH_INSURANCE_INSTITUTION,
    SOCIAL_SECURITY_INSTITUTION,
    PROFESSIONAL_AUTHORITY,
    EDUCATION_INSTITUTION,
    CIVIL_REGISTRY,
    COURT_OR_AUTHORITY,
    TRANSPORT_AUTHORITY,
    IMMIGRATION_AUTHORITY,
    DOCUMENT_HOLDER
}

internal enum class InternationalDocumentMode {
    PROFESSIONAL_CERTIFICATE,
    PERSONAL_INFORMATION,
    AUTHORITY_APPLICATION,
    AUTHORITY_ISSUED_RECORD,
    EDUCATION_RECORD,
    HOLDER_MAINTAINED_RECORD
}

internal enum class InternationalWorkflowStatus {
    DRAFT,
    READY_FOR_REVIEW,
    SIGNED_BY_ISSUER,
    SUBMITTED_TO_AUTHORITY,
    ISSUED_BY_AUTHORITY,
    VERIFIED,
    EXPIRED,
    REJECTED
}

internal data class InternationalDocumentWorkflow(
    val documentId: String,
    val issuerKind: InternationalIssuerKind,
    val authorityLabel: String,
    val mode: InternationalDocumentMode,
    val requiresIssuerSignature: Boolean,
    val authorityIssuesOriginal: Boolean,
    val statusValues: Set<InternationalWorkflowStatus>
)

internal object InternationalDocumentWorkflowCatalog {
    private val commonStatuses = setOf(
        InternationalWorkflowStatus.DRAFT,
        InternationalWorkflowStatus.READY_FOR_REVIEW,
        InternationalWorkflowStatus.SIGNED_BY_ISSUER,
        InternationalWorkflowStatus.SUBMITTED_TO_AUTHORITY,
        InternationalWorkflowStatus.ISSUED_BY_AUTHORITY,
        InternationalWorkflowStatus.VERIFIED,
        InternationalWorkflowStatus.EXPIRED,
        InternationalWorkflowStatus.REJECTED
    )

    private fun spec(
        id: String,
        issuer: InternationalIssuerKind,
        authority: String,
        mode: InternationalDocumentMode,
        signature: Boolean = false,
        authorityOriginal: Boolean = mode == InternationalDocumentMode.AUTHORITY_ISSUED_RECORD
    ) = InternationalDocumentWorkflow(id, issuer, authority, mode, signature, authorityOriginal, commonStatuses)

    val all: Map<String, InternationalDocumentWorkflow> = listOf(
        spec("icvp", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Uprawniony punkt szczepień / właściwy organ zdrowia", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, true),
        spec("medical_summary", InternationalIssuerKind.DOCUMENT_HOLDER, "Pacjent lub placówka medyczna — zależnie od autora", InternationalDocumentMode.PERSONAL_INFORMATION),
        spec("allergy_card", InternationalIssuerKind.DOCUMENT_HOLDER, "Pacjent; rozpoznanie może potwierdzić lekarz", InternationalDocumentMode.PERSONAL_INFORMATION),
        spec("medication_card", InternationalIssuerKind.DOCUMENT_HOLDER, "Pacjent; zestawienie może zweryfikować lekarz lub farmaceuta", InternationalDocumentMode.PERSONAL_INFORMATION),
        spec("fit_to_travel", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Lekarz uprawniony według prawa właściwego", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("fit_to_fly", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Lekarz uprawniony; przewoźnik może wymagać własnego formularza", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("rehabilitation_report", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Fizjoterapeuta lub lekarz w granicach kompetencji", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("functional_report", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Właściwy specjalista w granicach kompetencji", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("disability_opinion", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Lekarz lub terapeuta; formalne orzeczenie wydaje właściwy organ", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("eu_parking_card", InternationalIssuerKind.TRANSPORT_AUTHORITY, "Właściwy organ państwa wydającego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("prc", InternationalIssuerKind.HEALTH_INSURANCE_INSTITUTION, "Właściwa instytucja ubezpieczenia zdrowotnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("s1", InternationalIssuerKind.HEALTH_INSURANCE_INSTITUTION, "Właściwa instytucja ubezpieczenia zdrowotnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("s2", InternationalIssuerKind.HEALTH_INSURANCE_INSTITUTION, "Właściwa instytucja ubezpieczenia zdrowotnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("s3", InternationalIssuerKind.HEALTH_INSURANCE_INSTITUTION, "Właściwa instytucja ubezpieczenia zdrowotnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("da1", InternationalIssuerKind.SOCIAL_SECURITY_INSTITUTION, "Właściwa instytucja zabezpieczenia społecznego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("cross_border_prescription", InternationalIssuerKind.HEALTH_PROFESSIONAL, "Osoba uprawniona według prawa państwa wystawienia", InternationalDocumentMode.PROFESSIONAL_CERTIFICATE, true, false),
        spec("refund_claim", InternationalIssuerKind.HEALTH_INSURANCE_INSTITUTION, "Właściwa instytucja ubezpieczenia zdrowotnego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, false),
        spec("a1", InternationalIssuerKind.SOCIAL_SECURITY_INSTITUTION, "Właściwa instytucja zabezpieczenia społecznego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("u1", InternationalIssuerKind.SOCIAL_SECURITY_INSTITUTION, "Właściwa instytucja zabezpieczenia społecznego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("u2", InternationalIssuerKind.SOCIAL_SECURITY_INSTITUTION, "Właściwa instytucja zabezpieczenia społecznego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("epc", InternationalIssuerKind.PROFESSIONAL_AUTHORITY, "Właściwy organ państwa członkowskiego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("good_standing", InternationalIssuerKind.PROFESSIONAL_AUTHORITY, "Właściwy samorząd lub organ zawodowy", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("professional_status", InternationalIssuerKind.PROFESSIONAL_AUTHORITY, "Właściwy organ rejestrowy lub samorząd zawodowy", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("qualification_recognition", InternationalIssuerKind.PROFESSIONAL_AUTHORITY, "Organ właściwy do uznania kwalifikacji", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("diploma", InternationalIssuerKind.EDUCATION_INSTITUTION, "Uczelnia lub instytucja nadająca kwalifikację", InternationalDocumentMode.EDUCATION_RECORD, true, true),
        spec("education_certificate", InternationalIssuerKind.EDUCATION_INSTITUTION, "Szkoła lub instytucja edukacyjna", InternationalDocumentMode.EDUCATION_RECORD, true, true),
        spec("diploma_supplement", InternationalIssuerKind.EDUCATION_INSTITUTION, "Uczelnia wydająca dyplom", InternationalDocumentMode.EDUCATION_RECORD, true, true),
        spec("birth_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy zagraniczny urząd stanu cywilnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("marriage_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy zagraniczny urząd stanu cywilnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("death_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy zagraniczny urząd stanu cywilnego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("marital_status", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy organ stanu cywilnego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("capacity_to_marry", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy organ państwa wydającego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("residence_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy organ administracji lokalnej", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("citizenship_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy organ ds. obywatelstwa", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("life_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Organ lub osoba uprawniona w danej procedurze", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("criminal_record_foreign", InternationalIssuerKind.COURT_OR_AUTHORITY, "Właściwy organ rejestru karnego państwa wydającego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("divorce_decree", InternationalIssuerKind.COURT_OR_AUTHORITY, "Właściwy sąd lub organ państwa wydającego", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("adoption_certificate", InternationalIssuerKind.COURT_OR_AUTHORITY, "Właściwy sąd lub organ adopcyjny", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("parentage_certificate", InternationalIssuerKind.CIVIL_REGISTRY, "Właściwy organ stanu cywilnego lub sąd", InternationalDocumentMode.AUTHORITY_ISSUED_RECORD),
        spec("international_driving_permit", InternationalIssuerKind.TRANSPORT_AUTHORITY, "Organ lub upoważniona organizacja państwa wydającego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("travel_document", InternationalIssuerKind.IMMIGRATION_AUTHORITY, "Właściwy organ państwa wydającego", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("residence_card", InternationalIssuerKind.IMMIGRATION_AUTHORITY, "Właściwy organ ds. cudzoziemców", InternationalDocumentMode.AUTHORITY_APPLICATION, false, true),
        spec("passport_record", InternationalIssuerKind.DOCUMENT_HOLDER, "Organ wydający wskazany na oryginalnym dokumencie", InternationalDocumentMode.HOLDER_MAINTAINED_RECORD)
    ).associateBy { it.documentId }

    fun find(documentId: String): InternationalDocumentWorkflow? = all[documentId]
}

/**
 * Evidence record for an institution's graphic asset. This is a provenance
 * gate, not a legal determination: approval must be checked against the
 * institution's actual permission or the official template's terms.
 */
internal data class InstitutionalAssetAuthorization(
    val owner: String,
    val sourceUrl: String,
    val rightsBasis: String,
    val authorizationReference: String,
    val verifiedBy: String,
    val version: String,
    val effectiveFrom: LocalDate,
    val effectiveUntil: LocalDate? = null
) {
    fun isCompleteFor(date: LocalDate = LocalDate.now()): Boolean =
        owner.isNotBlank() &&
            sourceUrl.startsWith("https://", ignoreCase = true) &&
            rightsBasis.isNotBlank() &&
            authorizationReference.isNotBlank() &&
            verifiedBy.isNotBlank() &&
            version.isNotBlank() &&
            !date.isBefore(effectiveFrom) &&
            (effectiveUntil == null || !date.isAfter(effectiveUntil))
}

internal object InstitutionalAssetPolicy {
    private val neverRenderAsImage = setOf("Pieczęć", "Wzór podpisu odręcznego", "Grafika hologramu / zabezpieczenia")

    fun mayRender(kind: String, authorization: InstitutionalAssetAuthorization?): Boolean {
        if (kind in neverRenderAsImage) return false
        return authorization?.isCompleteFor() == true
    }
}
