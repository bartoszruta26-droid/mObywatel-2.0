// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.test.core.app.ApplicationProvider
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DonationAttachmentStorageTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val directory = File(context.filesDir, "donation-attachments")

    @After
    fun cleanup() {
        directory.listFiles()?.forEach { it.delete() }
        directory.delete()
    }

    private fun attachment(fileName: String, bytes: ByteArray): pl.gov.obywatel.android.DonationAttachment {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(bytes).joinToString("") { "%02x".format(it) }
        return pl.gov.obywatel.android.DonationAttachment(
            id = "attachment-open-test",
            donationId = "donation-open-test",
            fileName = "pokwitowanie.pdf",
            mimeType = "application/pdf",
            sizeBytes = bytes.size.toLong(),
            sha256 = hash,
            storedFileName = fileName,
            addedAt = Instant.parse("2026-11-15T10:00:00Z")
        )
    }

    @Test
    fun contentUriUsesPrivateFileProviderAndCanBeReadByGrantedContentResolver() {
        assertTrue(directory.exists() || directory.mkdirs())
        val bytes = "%PDF-1.7".toByteArray()
        val name = "11111111-1111-4111-8111-111111111111.pdf"
        File(directory, name).writeBytes(bytes)

        val uri = DonationAttachmentStorage(context).contentUri(
            attachment(name, bytes),
            "${context.packageName}.ocrfiles"
        )

        assertEquals("content", uri.scheme)
        assertTrue(uri.toString().contains(".ocrfiles/"))
        val received = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        assertTrue(bytes.contentEquals(received))
    }

    @Test
    fun contentUriRejectsFileChangedAfterImport() {
        assertTrue(directory.exists() || directory.mkdirs())
        val name = "22222222-2222-4222-8222-222222222222.pdf"
        File(directory, name).writeBytes("%PDF-1.7".toByteArray())
        val metadata = attachment(name, "%PDF-1.7".toByteArray())
        File(directory, name).writeBytes("%PDF-1.7-tampered".toByteArray())

        assertThrows(IllegalArgumentException::class.java) {
            DonationAttachmentStorage(context).contentUri(metadata, "${context.packageName}.ocrfiles")
        }
    }

    private fun sourceUri(fileName: String, bytes: ByteArray): Uri {
        assertTrue(directory.exists() || directory.mkdirs())
        val source = File(directory, fileName)
        source.writeBytes(bytes)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.ocrfiles",
            source
        )
    }

    @Test
    fun importCopiesValidPdfIntoPrivateStorageAndCalculatesSha256() {
        val bytes = "%PDF-1.7\nvalid-test-document".toByteArray()
        val uri = sourceUri("saf-source-valid.pdf", bytes)

        val imported = DonationAttachmentStorage(context).import("donation-saf-test", uri)

        assertEquals("application/pdf", imported.mimeType)
        assertEquals(bytes.size.toLong(), imported.sizeBytes)
        assertEquals(
            MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) },
            imported.sha256
        )
        val stored = File(directory, imported.storedFileName)
        assertTrue(stored.isFile)
        assertTrue(bytes.contentEquals(stored.readBytes()))
    }

    @Test
    fun importRejectsEmptyDocumentAndRemovesPartialTarget() {
        val uri = sourceUri("saf-source-empty.pdf", byteArrayOf())

        assertThrows(IllegalArgumentException::class.java) {
            DonationAttachmentStorage(context).import("donation-saf-test", uri)
        }

        assertEquals(1, directory.listFiles()?.size)
    }

    @Test
    fun importRejectsDocumentLargerThanTenMiBAndRemovesPartialTarget() {
        val bytes = ByteArray(pl.gov.obywatel.android.DonationAttachment.MAX_BYTES.toInt() + 1)
        val uri = sourceUri("saf-source-oversize.pdf", bytes)

        assertThrows(IllegalArgumentException::class.java) {
            DonationAttachmentStorage(context).import("donation-saf-test", uri)
        }

        assertEquals(1, directory.listFiles()?.size)
    }

    @Test
    fun importRejectsMimeAndBinarySignatureMismatchAndRemovesPartialTarget() {
        val pngHeader = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        )
        val uri = sourceUri("saf-source-mismatch.pdf", pngHeader)

        assertThrows(IllegalArgumentException::class.java) {
            DonationAttachmentStorage(context).import("donation-saf-test", uri)
        }

        assertEquals(1, directory.listFiles()?.size)
    }

    @Test
    fun contentUriRejectsMissingPrivateFile() {
        val name = "33333333-3333-4333-8333-333333333333.pdf"
        assertThrows(IllegalArgumentException::class.java) {
            DonationAttachmentStorage(context).contentUri(
                attachment(name, "%PDF-1.7".toByteArray()),
                "${context.packageName}.ocrfiles"
            )
        }
    }

    @Test
    fun importAcceptsJpegWithMatchingMimeAndSignature() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0x00)
        val uri = sourceUri("saf-source-valid.jpg", bytes)

        val imported = DonationAttachmentStorage(context).import("donation-saf-test", uri)

        assertEquals("image/jpeg", imported.mimeType)
        assertEquals(bytes.size.toLong(), imported.sizeBytes)
        assertTrue(bytes.contentEquals(File(directory, imported.storedFileName).readBytes()))
    }

    @Test
    fun importAcceptsPngWithMatchingMimeAndSignature() {
        val bytes = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        )
        val uri = sourceUri("saf-source-valid.png", bytes)

        val imported = DonationAttachmentStorage(context).import("donation-saf-test", uri)

        assertEquals("image/png", imported.mimeType)
        assertEquals(bytes.size.toLong(), imported.sizeBytes)
        assertTrue(bytes.contentEquals(File(directory, imported.storedFileName).readBytes()))
    }

    @Test
    fun importRemovesPartialTargetWhenProviderFailsDuringRead() {
        val uri = sourceUri("saf-source-read-error.pdf", "%PDF-1.7".toByteArray())
        File(directory, "saf-source-read-error.pdf").delete()

        assertThrows(Exception::class.java) {
            DonationAttachmentStorage(context).import("donation-saf-test", uri)
        }

        assertTrue(directory.listFiles().isNullOrEmpty())
    }

    @Test
    fun importDeletesPartialFileWhenProviderFailsAfterWritingBytes() {
        // Use the target application's context for storage. The test APK provider is
        // exported and is reached through the target application's ContentResolver.
        val uri = Uri.parse(
            "content://pl.gov.obywatel.android.prototype.test.failingstream/partial.pdf"
        )

        val error = assertThrows(java.io.IOException::class.java) {
            DonationAttachmentStorage(context)
                .import("donation-saf-midstream-error", uri)
        }

        assertTrue(error.message.orEmpty().contains("controlled read failure"))
        assertTrue(directory.listFiles().isNullOrEmpty())
    }

}
