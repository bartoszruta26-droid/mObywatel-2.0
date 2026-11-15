// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.test.core.app.ApplicationProvider
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
}
