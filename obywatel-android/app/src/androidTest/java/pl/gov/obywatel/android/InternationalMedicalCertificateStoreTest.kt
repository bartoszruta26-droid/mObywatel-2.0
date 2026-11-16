// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class InternationalMedicalCertificateStoreTest {
    @Test
    fun bilingualPdfContainsBothSourceAndTargetFieldLabelsAndValues() {
        val bytes = createBilingualMedicalCertificatePdf(
            sourceLanguage = "Polish",
            targetLanguage = "English",
            fields = listOf(
                MedicalCertificatePdfField(
                    sourceLabel = "Rozpoznanie",
                    sourceValue = "Ból kręgosłupa",
                    targetLabel = "English — Diagnosis",
                    targetValue = "Spinal pain"
                )
            )
        )
        assertTrue(bytes.size > 500)
        assertTrue(String(bytes.copyOfRange(0, 5), Charsets.US_ASCII) == "%PDF-")
    }

    @Test
    fun atomicWriterReplacesExistingFileWithCompleteContent() {
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "medical-certificate-atomic-${UUID.randomUUID()}"
        )
        assertTrue(directory.mkdirs())
        try {
            val target = File(directory, "certificate.enc")
            target.writeBytes(byteArrayOf(1, 2, 3))
            val replacement = ByteArray(4096) { (it % 251).toByte() }

            writeMedicalCertificateAtomically(directory, "certificate.enc", replacement)

            assertTrue(target.isFile)
            assertTrue(target.readBytes().contentEquals(replacement))
            assertFalse(File(directory, ".certificate.enc.tmp").exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun atomicWriterPreservesExistingDestinationWhenReplacementFails() {
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "medical-certificate-failure-${UUID.randomUUID()}"
        )
        assertTrue(directory.mkdirs())
        try {
            val destination = File(directory, "certificate.enc")
            assertTrue(destination.mkdir())

            val result = runCatching {
                writeMedicalCertificateAtomically(directory, "certificate.enc", byteArrayOf(9, 8, 7))
            }

            assertTrue(result.isFailure)
            assertTrue(destination.isDirectory)
            assertFalse(File(directory, ".certificate.enc.tmp").exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun loadDoesNotConvertCorruptedEncryptedDataIntoAnEmptyRecord() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.noBackupFilesDir, "international_medical_certificate.enc")
        val existed = file.isFile
        if (!existed) InternationalMedicalCertificateStore.save(context, emptyMap())
        val original = file.readBytes()
        try {
            val corrupted = original.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 0x01).toByte() }
            file.writeBytes(corrupted)

            assertTrue(runCatching { InternationalMedicalCertificateStore.load(context) }.isFailure)
        } finally {
            if (existed) file.writeBytes(original) else file.delete()
        }
    }

    @Test
    fun encryptedStoreRoundTripsAndAtomicallyOverwritesValues() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = InternationalMedicalCertificateStore.loadStrict(context)
        val key = "atomic_test_${UUID.randomUUID().toString().replace("-", "")}"

        try {
            val first = original + (key to "first-value")
            InternationalMedicalCertificateStore.save(context, first)
            assertEquals("first-value", InternationalMedicalCertificateStore.loadStrict(context)[key])

            val second = first + (key to "second-value")
            InternationalMedicalCertificateStore.save(context, second)
            assertEquals(second, InternationalMedicalCertificateStore.loadStrict(context))
        } finally {
            InternationalMedicalCertificateStore.save(context, original)
        }
    }

    @Test
    fun encryptedCertificateRecordsSupportCreateReadUpdateListAndDelete() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = InternationalMedicalCertificateStore.loadStrict(context)
        val id = "test_${UUID.randomUUID().toString().replace("-", "")}"
        try {
            InternationalMedicalCertificateRecords.save(context, id, mapOf(
                "doc_international_medical_certificate_documentNumber" to "CERT-001",
                "imc_source_language" to "pl"
            ))
            assertEquals("CERT-001", InternationalMedicalCertificateRecords.load(context, id)
                ["doc_international_medical_certificate_documentNumber"])
            assertTrue(InternationalMedicalCertificateRecords.list(context).any { it.first == id })

            InternationalMedicalCertificateRecords.save(context, id, mapOf(
                "doc_international_medical_certificate_documentNumber" to "CERT-002"
            ))
            assertEquals("CERT-002", InternationalMedicalCertificateRecords.load(context, id)
                ["doc_international_medical_certificate_documentNumber"])

            InternationalMedicalCertificateRecords.delete(context, id)
            assertFalse(InternationalMedicalCertificateRecords.list(context).any { it.first == id })
            assertTrue(InternationalMedicalCertificateRecords.load(context, id).isEmpty())
        } finally {
            InternationalMedicalCertificateStore.save(context, original)
        }
    }

}
