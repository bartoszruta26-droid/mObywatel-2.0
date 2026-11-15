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
    fun imagingAttachmentReferencesMustExactlyMatchEncryptedScopeMetadata() {
        assertTrue(medicalImagingAttachmentReferencesMatch(listOf("report-1", "scan-2"), listOf("scan-2", "report-1")))
        assertTrue(medicalImagingAttachmentReferencesMatch(emptyList(), emptyList()))
        assertFalse(medicalImagingAttachmentReferencesMatch(listOf("report-1"), emptyList()))
        assertFalse(medicalImagingAttachmentReferencesMatch(emptyList(), listOf("orphan-1")))
        assertFalse(medicalImagingAttachmentReferencesMatch(listOf("same", "same"), listOf("same")))
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
    fun importFailureKeepsInstalledFilesOnceMetadataCommitWasAttempted() {
        assertTrue(shouldDeleteInstalledAttachmentsAfterImportFailure(false))
        assertFalse(shouldDeleteInstalledAttachmentsAfterImportFailure(true))
    }

    @Test
    fun addFailureDeletesOnlyBeforeMetadataCommitAttempt() {
        assertTrue(shouldDeleteAddedAttachmentAfterFailure(false))
        assertFalse(shouldDeleteAddedAttachmentAfterFailure(true))
    }

    @Test
    fun attachmentRecoveryPlanFindsOnlyUnreferencedEncryptedFiles() {
        val referenced = setOf("keep-1", "keep-2")
        val stored = setOf("keep-1", "keep-2", "orphan-1", "orphan-2")
        assertEquals(setOf("orphan-1", "orphan-2"), findOrphanedOriginalAttachmentIds(referenced, stored))
        assertTrue(findOrphanedOriginalAttachmentIds(referenced, referenced).isEmpty())
    }

    @Test
    fun attachmentMetadataValidatorRejectsDuplicateOrInvalidRecords() {
        val valid = """[{"id":"attachment-1","name":"report.pdf","mime":"application/pdf"}]"""
        validateOriginalAttachmentMetadata(valid)
        val invalidRecords = listOf(
            """[{"id":"../escape","name":"report.pdf","mime":"application/pdf"}]""",
            """[{"id":"same","name":"a.pdf","mime":"application/pdf"},{"id":"same","name":"b.pdf","mime":"application/pdf"}]""",
            """[{"id":"valid","name":"report.html","mime":"text/html"}]""",
            """[{"id":"valid","name":"","mime":"application/pdf"}]"""
        )
        invalidRecords.forEach { raw ->
            assertTrue("Expected invalid metadata rejection: $raw", runCatching {
                validateOriginalAttachmentMetadata(raw)
            }.isFailure)
        }
    }

    @Test
    fun attachmentArchiveBase64DecoderAcceptsCanonicalPayload() {
        val original = "Clinical report".toByteArray(Charsets.UTF_8)
        val encoded = java.util.Base64.getEncoder().encodeToString(original)
        assertTrue(decodeOriginalAttachmentBase64(encoded, 1024).contentEquals(original))
    }

    @Test
    fun attachmentArchiveBase64DecoderRejectsMalformedNonCanonicalAndOversizedPayloads() {
        listOf("%%%=", "YQ", "YQ==\\n", "YR==").forEach { invalid ->
            assertTrue("Expected invalid Base64 rejection: $invalid", runCatching {
                decodeOriginalAttachmentBase64(invalid, 1024)
            }.isFailure)
        }
        val tooLarge = java.util.Base64.getEncoder().encodeToString(ByteArray(5))
        assertTrue(runCatching {
            decodeOriginalAttachmentBase64(tooLarge, 4)
        }.isFailure)
        assertTrue(runCatching {
            decodeOriginalAttachmentBase64("", 1024)
        }.isFailure)
    }

    @Test
    fun attachmentPayloadValidatorAcceptsSupportedNonImagePayloads() {
        validateOriginalAttachmentPayload("application/pdf", "%PDF-1.7".toByteArray(Charsets.US_ASCII))
        validateOriginalAttachmentPayload("text/plain", "Clinical report".toByteArray(Charsets.UTF_8))
        validateOriginalAttachmentPayload("text/csv", "date,result".toByteArray(Charsets.UTF_8))
        validateOriginalAttachmentPayload("application/xml", "<report/>".toByteArray(Charsets.UTF_8))
    }

    @Test
    fun attachmentPayloadValidatorRejectsImageSignaturesWithoutDecodableImageData() {
        val invalidImages = listOf(
            "image/jpeg" to byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0x00),
            "image/png" to byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a),
            "image/webp" to "RIFF0000WEBP".toByteArray(Charsets.US_ASCII)
        )
        invalidImages.forEach { (mime, bytes) ->
            assertTrue("Expected invalid image data rejection for $mime", runCatching {
                validateOriginalAttachmentPayload(mime, bytes)
            }.isFailure)
        }
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
        val lateDeclaration = " ".repeat(4096) +
            "<!DOCTYPE report [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><report>&x;</report>"
        assertTrue(runCatching {
            validateOriginalAttachmentPayload("application/xml", lateDeclaration.toByteArray())
        }.isFailure)
    }

    @Test
    fun attachmentRemovalRequiresExclusiveOwnershipByRequestedDocument() {
        validateOriginalAttachmentIdsOwnedByDocument(
            listOf("attachment-1", "attachment-2"),
            "document-a",
            mapOf("attachment-1" to "document-a", "attachment-2" to "document-a")
        )
        assertTrue(runCatching {
            validateOriginalAttachmentIdsOwnedByDocument(
                listOf("attachment-1"),
                "document-b",
                mapOf("attachment-1" to "document-a")
            )
        }.isFailure)
        assertTrue(runCatching {
            validateOriginalAttachmentIdsOwnedByDocument(
                listOf("missing"),
                "document-a",
                emptyMap()
            )
        }.isFailure)
        assertTrue(runCatching {
            validateOriginalAttachmentIdsOwnedByDocument(
                listOf("attachment-1", "foreign"),
                "document-a",
                mapOf("attachment-1" to "document-a", "foreign" to "document-b")
            )
        }.isFailure)
    }

    @Test
    fun attachmentOwnershipValidationRejectsCrossDocumentReuseAndAllowsSameDocumentRetry() {
        val owners = mapOf("attachment-1" to "medical-certificate-1")
        validateOriginalAttachmentIdOwner("attachment-1", "medical-certificate-1", owners)
        validateOriginalAttachmentIdOwner("attachment-2", "medical-certificate-2", owners)
        assertTrue(runCatching {
            validateOriginalAttachmentIdOwner("attachment-1", "medical-certificate-2", owners)
        }.isFailure)
    }


    @Test
    fun certificateValidationRejectsUnsupportedLanguagesAndDuplicateImagingIds() {
        val document = InternationalMedicalCertificate(
            sourceLanguage = "xx",
            targetLanguages = setOf("pl", "unknown"),
            imagingStudies = listOf(
                MedicalImagingStudy(id = "study-1", modality = MedicalImagingModality.MRI),
                MedicalImagingStudy(id = "study-1", modality = MedicalImagingModality.CT)
            )
        )
        val errors = document.validate()
        assertTrue(errors.any { it.contains("Original language is not supported") })
        assertTrue(errors.any { it.contains("target language") })
        assertTrue(errors.any { it.contains("identifiers must be unique") })
    }

    @Test
    fun certificateValidationRejectsInvalidImagingDatesAndAttachmentIds() {
        val document = InternationalMedicalCertificate(
            imagingStudies = listOf(
                MedicalImagingStudy(
                    id = "study-1",
                    modality = MedicalImagingModality.MRI,
                    studyDate = "2026-02-30",
                    attachmentIds = listOf("valid-id", "../escape", "valid-id")
                )
            )
        )
        val errors = document.validate()
        assertTrue(errors.any { it.contains("invalid examination date") })
        assertTrue(errors.any { it.contains("attachment identifiers") })
    }

    @Test
    fun certificateValidationAcceptsSupportedLanguagesAndValidImagingMetadata() {
        val document = InternationalMedicalCertificate(
            sourceLanguage = "he",
            targetLanguages = setOf("pl", "en"),
            imagingStudies = listOf(
                MedicalImagingStudy(
                    id = "study-2026-01",
                    modality = MedicalImagingModality.MRI,
                    studyDate = "2024-02-29",
                    attachmentIds = listOf("report-1", "image-2")
                )
            )
        )
        assertTrue(document.validate().isEmpty())
    }


    @Test
    fun missingOrBlankImagingStudyPayloadLoadsAsAnEmptyList() {
        assertTrue(loadMedicalImagingStudies(null).isEmpty())
        assertTrue(loadMedicalImagingStudies("").isEmpty())
        assertTrue(loadMedicalImagingStudies("   ").isEmpty())
    }

    @Test
    fun malformedNonBlankImagingStudyPayloadIsRejected() {
        assertTrue(runCatching { loadMedicalImagingStudies("[") }.isFailure)
        assertTrue(runCatching { loadMedicalImagingStudies("not-json") }.isFailure)
    }

    @Test
    fun imagingStudyJsonRoundTripPreservesAllFieldsAndAttachmentReferences() {
        val original = listOf(
            MedicalImagingStudy(
                id = "study-2026-02",
                modality = MedicalImagingModality.MRI,
                bodyRegion = "Lumbar spine",
                studyDate = "2026-02-14",
                facility = "Regional Imaging Centre",
                indication = "Persistent pain",
                technique = "T1/T2 sequences",
                contrast = "Gadolinium",
                findings = "No acute lesion",
                impression = "Degenerative changes",
                radiologistName = "A. Example",
                reportNumber = "MR-2026/014",
                attachmentIds = listOf("report-001", "scan-002")
            ),
            MedicalImagingStudy(
                id = "study-2026-03",
                modality = MedicalImagingModality.CT,
                bodyRegion = "Chest",
                studyDate = "2026-03-01"
            )
        )

        val decoded = decodeMedicalImagingStudies(encodeMedicalImagingStudies(original))

        assertEquals(original, decoded)
    }

    @Test
    fun imagingStudyDecoderKeepsLegacyRecordsWithoutAttachmentIdsReadable() {
        val legacy = """[
            {"id":"legacy-study-1","modality":"X_RAY","bodyRegion":"Chest","studyDate":"2024-05-20"},
            {"id":"legacy-study-2","modality":"MRI","attachmentIds":[]}
        ]"""

        val decoded = decodeMedicalImagingStudies(legacy)

        assertEquals(2, decoded.size)
        assertTrue(decoded.all { it.attachmentIds.isEmpty() })
        assertEquals("Chest", decoded.first().bodyRegion)
    }

    @Test
    fun removingImagingStudyIdsReturnsOnlyDeletedStudies() {
        val previous = listOf(
            MedicalImagingStudy("study-1", MedicalImagingModality.MRI),
            MedicalImagingStudy("study-2", MedicalImagingModality.CT),
            MedicalImagingStudy("study-3", MedicalImagingModality.X_RAY)
        )
        val current = listOf(
            MedicalImagingStudy("study-1", MedicalImagingModality.MRI),
            MedicalImagingStudy("study-3", MedicalImagingModality.X_RAY)
        )

        assertEquals(listOf("study-2"), removedMedicalImagingStudyIds(previous, current))
    }

    @Test
    fun imagingStudyDecoderRejectsDuplicateIdsAndInvalidDates() {
        val duplicateIds = """[
            {"id":"same","modality":"MRI"},
            {"id":"same","modality":"CT"}
        ]"""
        assertTrue(runCatching { decodeMedicalImagingStudies(duplicateIds) }.isFailure)

        val invalidDate = """[
            {"id":"study-1","modality":"MRI","studyDate":"2026-02-30"}
        ]"""
        assertTrue(runCatching { decodeMedicalImagingStudies(invalidDate) }.isFailure)
    }

    @Test
    fun imagingStudyDecoderRejectsUnsafeAttachmentIdentifiers() {
        val invalidAttachments = """[
            {"id":"study-1","modality":"MRI","attachmentIds":["../outside"]}
        ]"""
        assertTrue(runCatching { decodeMedicalImagingStudies(invalidAttachments) }.isFailure)
    }

    @Test
    fun stagedAttachmentWriterPersistsCompleteBytesBeforeReturning() {
        val directory = java.nio.file.Files.createTempDirectory("attachment-stage-test").toFile()
        try {
            val staging = java.io.File(directory, ".attachment-1.import")
            val encryptedBytes = byteArrayOf(1, 2, 3, 4, 5, 6)

            writeOriginalAttachmentStagingFile(staging, encryptedBytes)

            assertTrue(staging.isFile)
            assertTrue(staging.readBytes().contentEquals(encryptedBytes))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun stagedAttachmentWriterRejectsEmptyPayloadWithoutCreatingFile() {
        val directory = java.nio.file.Files.createTempDirectory("attachment-stage-empty-test").toFile()
        try {
            val staging = java.io.File(directory, ".attachment-empty.import")

            assertTrue(runCatching {
                writeOriginalAttachmentStagingFile(staging, byteArrayOf())
            }.isFailure)
            assertFalse(staging.exists())
        } finally {
            directory.deleteRecursively()
        }
    }

}
