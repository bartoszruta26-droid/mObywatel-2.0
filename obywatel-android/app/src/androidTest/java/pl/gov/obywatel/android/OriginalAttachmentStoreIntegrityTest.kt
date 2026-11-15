// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class OriginalAttachmentStoreIntegrityTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun documentId() = "imaging_test_${UUID.randomUUID().toString().replace("-", "")}"

    private fun archive(documentId: String, id: String, bytes: ByteArray): String =
        JSONObject()
            .put("version", 1)
            .put("attachments", JSONArray().put(
                JSONObject()
                    .put("documentId", documentId)
                    .put("id", id)
                    .put("name", "report.txt")
                    .put("mime", "text/plain")
                    .put("dataBase64", Base64.encodeToString(bytes, Base64.NO_WRAP))
            )).toString()

    @Test
    fun importedAttachmentSurvivesReconciliationAndArchiveRoundTrip() {
        val document = documentId()
        val attachmentId = "report-${UUID.randomUUID().toString().take(8)}"
        val original = "medical imaging report".toByteArray(Charsets.UTF_8)
        try {
            importOriginalAttachmentArchive(context, archive(document, attachmentId, original))
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(context, document))

            reconcileOriginalAttachments(context)
            val exported = JSONObject(exportOriginalAttachmentArchive(context))
                .getJSONArray("attachments")
            val matching = (0 until exported.length())
                .map { exported.getJSONObject(it) }
                .first { it.getString("documentId") == document && it.getString("id") == attachmentId }
            assertArrayEquals(original, Base64.decode(matching.getString("dataBase64"), Base64.DEFAULT))

            // A second import of the same bytes and metadata must be idempotent.
            importOriginalAttachmentArchive(context, archive(document, attachmentId, original))
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(context, document))
            reconcileOriginalAttachments(context)
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
        }
    }

    @Test
    fun reconciliationRemovesOnlyUnreferencedEncryptedFiles() {
        val document = documentId()
        val attachmentId = "keep-${UUID.randomUUID().toString().take(8)}"
        val orphanId = "orphan-${UUID.randomUUID().toString().take(8)}"
        val bytes = "kept report".toByteArray(Charsets.UTF_8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        try {
            importOriginalAttachmentArchive(context, archive(document, attachmentId, bytes))
            val orphan = File(directory, "$orphanId.enc")
            orphan.writeBytes(byteArrayOf(1, 2, 3, 4))

            reconcileOriginalAttachments(context)

            assertTrue(File(directory, "$attachmentId.enc").isFile)
            assertFalse(orphan.exists())
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(context, document))
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
            File(directory, "$orphanId.enc").delete()
        }
    }

    @Test
    fun reconciliationReportsMissingReferencedEncryptedFileWithoutDeletingMetadata() {
        val document = documentId()
        val attachmentId = "missing-${UUID.randomUUID().toString().take(8)}"
        val preferences = context.getSharedPreferences("original_document_attachments", Context.MODE_PRIVATE)
        val metadata = JSONArray().put(
            JSONObject()
                .put("id", attachmentId)
                .put("name", "missing.txt")
                .put("mime", "text/plain")
        ).toString()
        try {
            assertTrue(preferences.edit().putString(document, metadata).commit())

            assertTrue(runCatching { reconcileOriginalAttachments(context) }.isFailure)
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(context, document))
        } finally {
            preferences.edit().remove(document).commit()
        }
    }

    @Test
    fun removingImagingStudyScopeRemovesItsMetadataAndEncryptedFiles() {
        val document = documentId()
        val attachmentId = "remove-${UUID.randomUUID().toString().take(8)}"
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        try {
            importOriginalAttachmentArchive(
                context,
                archive(document, attachmentId, "remove me".toByteArray(Charsets.UTF_8))
            )
            assertTrue(File(directory, "$attachmentId.enc").isFile)

            removeOriginalAttachmentDocumentScope(context, document)

            assertTrue(listOriginalAttachmentIds(context, document).isEmpty())
            assertFalse(File(directory, "$attachmentId.enc").exists())
            reconcileOriginalAttachments(context)
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
        }
    }

    @Test
    fun reconciliationCleansInterruptedImportStagingBeforeReportingMissingSource() {
        val document = documentId()
        val attachmentId = "missing-${UUID.randomUUID().toString().take(8)}"
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        val staging = File(directory.apply { mkdirs() }, ".interrupted-${UUID.randomUUID()}.import")
        val preferences = context.getSharedPreferences("original_document_attachments", Context.MODE_PRIVATE)
        val metadata = JSONArray().put(
            JSONObject().put("id", attachmentId).put("name", "missing.txt").put("mime", "text/plain")
        ).toString()
        try {
            staging.writeBytes(byteArrayOf(1, 2, 3))
            assertTrue(preferences.edit().putString(document, metadata).commit())

            assertTrue(runCatching { reconcileOriginalAttachments(context) }.isFailure)

            assertFalse(staging.exists())
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(context, document))
        } finally {
            preferences.edit().remove(document).commit()
            staging.delete()
        }
    }

    @Test
    fun invalidLaterArchiveEntryDoesNotPartiallyInstallEarlierValidEntry() {
        val document = documentId()
        val validId = "valid-${UUID.randomUUID().toString().take(8)}"
        val invalidId = "invalid-${UUID.randomUUID().toString().take(8)}"
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        val entries = JSONArray()
            .put(JSONObject().put("documentId", document).put("id", validId)
                .put("name", "valid.txt").put("mime", "text/plain")
                .put("dataBase64", Base64.encodeToString("valid".toByteArray(), Base64.NO_WRAP)))
            .put(JSONObject().put("documentId", document).put("id", invalidId)
                .put("name", "invalid.bin").put("mime", "application/octet-stream")
                .put("dataBase64", Base64.encodeToString("invalid".toByteArray(), Base64.NO_WRAP)))
        try {
            assertTrue(runCatching {
                importOriginalAttachmentArchive(context, JSONObject().put("version", 1)
                    .put("attachments", entries).toString())
            }.isFailure)

            assertTrue(listOriginalAttachmentIds(context, document).isEmpty())
            assertFalse(File(directory, "$validId.enc").exists())
            assertFalse(File(directory, "$invalidId.enc").exists())
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
            File(directory, "$validId.enc").delete()
            File(directory, "$invalidId.enc").delete()
        }
    }

    @Test
    fun importFailureAfterStagingCleansTemporaryFilesWithoutCommittingMetadata() {
        val document = documentId()
        val stagedId = "staged-${UUID.randomUUID().toString().take(8)}"
        val collisionId = "collision-${UUID.randomUUID().toString().take(8)}"
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val collision = File(directory, "$collisionId.enc")
        collision.writeBytes(ByteArray(32) { it.toByte() })
        val entries = JSONArray()
            .put(JSONObject().put("documentId", document).put("id", stagedId)
                .put("name", "staged.txt").put("mime", "text/plain")
                .put("dataBase64", Base64.encodeToString("staged".toByteArray(), Base64.NO_WRAP)))
            .put(JSONObject().put("documentId", document).put("id", collisionId)
                .put("name", "collision.txt").put("mime", "text/plain")
                .put("dataBase64", Base64.encodeToString("collision".toByteArray(), Base64.NO_WRAP)))
        try {
            assertTrue(runCatching {
                importOriginalAttachmentArchive(context, JSONObject().put("version", 1)
                    .put("attachments", entries).toString())
            }.isFailure)

            assertTrue(listOriginalAttachmentIds(context, document).isEmpty())
            assertFalse(File(directory, "$stagedId.enc").exists())
            assertTrue(directory.listFiles().orEmpty().none {
                it.name.startsWith(".$stagedId.") && it.name.endsWith(".import")
            })
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
            collision.delete()
            directory.listFiles().orEmpty().filter {
                it.name.startsWith(".$stagedId.") && it.name.endsWith(".import")
            }.forEach { it.delete() }
        }
    }

    @Test
    fun concurrentImportsForSameDocumentPreserveBothCommittedAttachments() {
        val document = documentId()
        val firstId = "parallel-a-${UUID.randomUUID().toString().take(8)}"
        val secondId = "parallel-b-${UUID.randomUUID().toString().take(8)}"
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        val firstArchive = archive(document, firstId, "first".toByteArray(Charsets.UTF_8))
        val secondArchive = archive(document, secondId, "second".toByteArray(Charsets.UTF_8))
        try {
            val first = executor.submit { start.await(); importOriginalAttachmentArchive(context, firstArchive) }
            val second = executor.submit { start.await(); importOriginalAttachmentArchive(context, secondArchive) }
            start.countDown()
            first.get()
            second.get()

            assertEquals(listOf(firstId, secondId).sorted(), listOriginalAttachmentIds(context, document).sorted())
            reconcileOriginalAttachments(context)
        } finally {
            executor.shutdownNow()
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
        }
    }
}
