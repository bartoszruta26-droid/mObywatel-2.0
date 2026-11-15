package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InternationalMedicalCertificateTest {
    @Test
    fun supportedLanguagesIncludeRequestedLanguagesAndBelgianVariants() {
        val codes = MedicalCertificateLanguages.supported.map { it.code }.toSet()
        assertTrue(codes.containsAll(setOf(
            "en", "he", "de", "es", "fr", "it", "pl", "bg", "ro",
            "nl", "nl-BE", "fr-BE", "de-BE", "pt", "sv"
        )))
        assertNull(MedicalCertificateLanguages.find("Belgian"))
        assertEquals("nl-BE", MedicalCertificateLanguages.find("nl-be")?.code)
    }

    @Test
    fun hebrewIsMarkedAsRightToLeft() {
        assertTrue(MedicalCertificateLanguages.isRightToLeft("he"))
        assertTrue(MedicalCertificateLanguages.isRightToLeft("he-IL"))
        assertFalse(MedicalCertificateLanguages.isRightToLeft("en"))
    }

    @Test
    fun changingSourceTextMarksExistingTranslationAsStale() {
        val document = InternationalMedicalCertificate(
            sourceFields = mapOf("diagnosis" to "Source diagnosis"),
            translations = listOf(
                MedicalFieldTranslation(
                    fieldKey = "diagnosis",
                    sourceLanguage = "en",
                    targetLanguage = "pl",
                    sourceText = "Source diagnosis",
                    translatedText = "Rozpoznanie",
                    status = MedicalTranslationStatus.MACHINE_TRANSLATED
                )
            )
        )
        assertTrue(document.hasCurrentTranslation("diagnosis", "pl"))
        val changed = document.withUpdatedSourceField("diagnosis", "Updated diagnosis")
        assertEquals(MedicalTranslationStatus.STALE, changed.translations.single().status)
        assertFalse(changed.hasCurrentTranslation("diagnosis", "pl"))
        assertEquals("Source diagnosis", document.sourceFields["diagnosis"])
    }

    @Test
    fun imagingModelSupportsAllRequiredModalitiesAndKeepsReportData() {
        assertEquals(setOf("X-ray", "MRI", "CT", "Ultrasound", "Other"),
            MedicalImagingModality.entries.map { it.label }.toSet())
        val study = MedicalImagingStudy(
            id = "study-1",
            modality = MedicalImagingModality.MRI,
            bodyRegion = "Lumbar spine",
            findings = "Disc protrusion",
            impression = "Clinical correlation recommended",
            attachmentIds = listOf("report-1")
        )
        assertEquals("Lumbar spine", study.bodyRegion)
        assertEquals("report-1", study.attachmentIds.single())
        val completedReport = study.copy(
            technique = "T1/T2 sagittal sequences",
            contrast = "Gadolinium",
            radiologistName = "Dr Example",
            reportNumber = "MRI-2026-001"
        )
        assertEquals("T1/T2 sagittal sequences", completedReport.technique)
        assertEquals("Gadolinium", completedReport.contrast)
        assertEquals("Dr Example", completedReport.radiologistName)
        assertEquals("MRI-2026-001", completedReport.reportNumber)
    }

    @Test
    fun invalidTranslationFieldIsReportedWithoutDiscardingOriginalValues() {
        val document = InternationalMedicalCertificate(
            sourceFields = mapOf("patientName" to "Example Patient"),
            translations = listOf(
                MedicalFieldTranslation(
                    fieldKey = "unknown",
                    sourceLanguage = "en",
                    targetLanguage = "pl",
                    sourceText = "x",
                    translatedText = "y",
                    status = MedicalTranslationStatus.MACHINE_TRANSLATED
                )
            )
        )
        assertTrue(document.validate().any { it.contains("absent from the source") })
        assertEquals("Example Patient", document.sourceFields["patientName"])
    }
    @Test
    fun imagingStudyDateAcceptsValidIsoDatesAndRejectsInvalidDates() {
        assertTrue(isValidMedicalStudyDate("2024-02-29"))
        assertTrue(isValidMedicalStudyDate("2026-11-15"))
        assertTrue(isValidMedicalStudyDate(""))
        assertFalse(isValidMedicalStudyDate("2026-02-29"))
        assertFalse(isValidMedicalStudyDate("2026-04-31"))
        assertFalse(isValidMedicalStudyDate("2026-13-01"))
        assertFalse(isValidMedicalStudyDate("15-11-2026"))
        assertFalse(isValidMedicalStudyDate("2026-11-15extra"))
    }

    @Test
    fun imagingStudyAttachmentsUseStableIsolatedDocumentScopes() {
        assertEquals("imaging_study_study-001", medicalImagingAttachmentDocumentId("study-001"))
        assertTrue(runCatching { medicalImagingAttachmentDocumentId("../outside") }.isFailure)
        assertTrue(medicalImagingAttachmentDocumentId("study-001") != medicalImagingAttachmentDocumentId("study-002"))
    }

    @Test
    fun imagingStudiesPreserveEveryFieldAcrossJsonRoundTrip() {
        val original = listOf(
            MedicalImagingStudy(
                id = "study-mri-001",
                modality = MedicalImagingModality.MRI,
                bodyRegion = "Lumbar spine",
                studyDate = "2026-11-15",
                facility = "Regional Imaging Centre",
                indication = "Persistent lower back pain",
                technique = "T1/T2 sagittal and axial sequences",
                contrast = "Gadolinium",
                findings = "L4-L5 disc protrusion",
                impression = "Degenerative changes",
                radiologistName = "Dr Example",
                reportNumber = "MRI-2026-001",
                attachmentIds = listOf("report-001", "image-002")
            ),
            MedicalImagingStudy(
                id = "study-xray-002",
                modality = MedicalImagingModality.X_RAY,
                bodyRegion = "Chest",
                studyDate = "",
                facility = "Clinic",
                indication = "Baseline examination",
                findings = "No focal opacity",
                impression = "No acute findings"
            )
        )

        val restored = decodeMedicalImagingStudies(encodeMedicalImagingStudies(original))
        assertEquals(original, restored)
    }


    @Test
    fun removingImagingStudiesSelectsOnlyPreviouslySavedStudyIds() {
        val previous = listOf(
            MedicalImagingStudy(id = "study-keep", modality = MedicalImagingModality.MRI),
            MedicalImagingStudy(id = "study-remove", modality = MedicalImagingModality.CT)
        )
        val current = listOf(
            MedicalImagingStudy(id = "study-keep", modality = MedicalImagingModality.MRI),
            MedicalImagingStudy(id = "study-new", modality = MedicalImagingModality.X_RAY)
        )
        assertEquals(listOf("study-remove"), removedMedicalImagingStudyIds(previous, current))
        assertTrue(removedMedicalImagingStudyIds(previous, previous).isEmpty())
    }

    @Test
    fun attachmentPayloadValidatorAcceptsSupportedFileSignatures() {
        validateOriginalAttachmentPayload("application/pdf", "%PDF-1.7".toByteArray(Charsets.US_ASCII))
        validateOriginalAttachmentPayload("image/jpeg", byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0x00))
        validateOriginalAttachmentPayload("image/png", byteArrayOf(
            0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
        ))
        validateOriginalAttachmentPayload("image/webp", "RIFF0000WEBP".toByteArray(Charsets.US_ASCII))
        validateOriginalAttachmentPayload("text/plain", "Clinical report".toByteArray(Charsets.UTF_8))
        validateOriginalAttachmentPayload("text/csv", "date,result".toByteArray(Charsets.UTF_8))
        validateOriginalAttachmentPayload("application/xml", "<report/>".toByteArray(Charsets.UTF_8))
    }

    @Test
    fun attachmentPayloadValidatorRejectsMismatchedAndUnsupportedMimeTypes() {
        val invalidCases = listOf(
            "image/jpeg" to "not an image".toByteArray(),
            "image/png" to byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8),
            "image/webp" to "RIFF0000JPEG".toByteArray(Charsets.US_ASCII),
            "application/pdf" to "not a pdf".toByteArray(),
            "image/gif" to byteArrayOf(1, 2, 3),
            "text/html" to "<html/>".toByteArray()
        )
        invalidCases.forEach { (mime, bytes) ->
            assertTrue("Expected rejection for $mime", runCatching {
                validateOriginalAttachmentPayload(mime, bytes)
            }.isFailure)
        }
    }

    @Test
    fun attachmentPayloadValidatorRejectsEmptyOversizedAndExternalEntityText() {
        assertTrue(runCatching {
            validateOriginalAttachmentPayload("text/plain", byteArrayOf())
        }.isFailure)
        assertTrue(runCatching {
            validateOriginalAttachmentPayload("text/plain", ByteArray(25 * 1024 * 1024 + 1))
        }.isFailure)
        assertTrue(runCatching {
            validateOriginalAttachmentPayload(
                "application/xml",
                "<!DOCTYPE report [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><report>&x;</report>".toByteArray()
            )
        }.isFailure)
    }

}
