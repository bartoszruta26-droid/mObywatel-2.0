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

    @Test
    fun attachmentMetadataAndEncryptedBytesAreReadableThroughFreshPackageContext() {
        val document = documentId()
        val attachmentId = "fresh-context-${UUID.randomUUID().toString().take(8)}"
        val original = "persistent medical report".toByteArray(Charsets.UTF_8)
        try {
            importOriginalAttachmentArchive(context, archive(document, attachmentId, original))

            val freshContext = context.createPackageContext(context.packageName, 0)
            assertEquals(listOf(attachmentId), listOriginalAttachmentIds(freshContext, document))

            val exported = JSONObject(exportOriginalAttachmentArchive(freshContext))
                .getJSONArray("attachments")
            val item = (0 until exported.length())
                .map { exported.getJSONObject(it) }
                .first { it.getString("documentId") == document && it.getString("id") == attachmentId }
            assertArrayEquals(original, Base64.decode(item.getString("dataBase64"), Base64.DEFAULT))
        } finally {
            runCatching { removeOriginalAttachmentDocumentScope(context, document) }
        }
    }



    private class ControlledStagingOperations(
        private val failWriteAfterPartialBytes: Boolean = false,
        private val failInstallAt: Int? = null
    ) : OriginalAttachmentStagingOperations {
        var installCalls = 0

        override fun write(staging: File, encryptedBytes: ByteArray) {
            if (failWriteAfterPartialBytes) {
                staging.writeBytes(encryptedBytes.copyOfRange(0, minOf(4, encryptedBytes.size)))
                throw IllegalStateException("simulated staging write failure")
            }
            writeOriginalAttachmentStagingFile(staging, encryptedBytes)
        }

        override fun install(staging: File, target: File): Boolean {
            installCalls += 1
            if (installCalls == failInstallAt) return false
            return staging.renameTo(target)
        }
    }

    @Test
    fun addPartialStagingWriteFailureLeavesNoTargetMetadataOrTemporaryFile() {
        val document = documentId()
        val attachmentId = "write-fail-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend()
        val stagingOperations = ControlledStagingOperations(failWriteAfterPartialBytes = true)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.add(
                    context, document, "partial.txt", "text/plain", "partial write".toByteArray(),
                    attachmentId, backend, stagingOperations
                )
            }.isFailure)
            assertTrue(OriginalAttachmentStore.list(context, document, backend).isEmpty())
            assertFalse(File(directory, "$attachmentId.enc").exists())
            assertTrue(directory.listFiles().orEmpty().none {
                it.name.startsWith(".$attachmentId.") && it.name.endsWith(".import")
            })
        } finally {
            File(directory, "$attachmentId.enc").delete()
            directory.listFiles().orEmpty().filter {
                it.name.startsWith(".$attachmentId.") && it.name.endsWith(".import")
            }.forEach { it.delete() }
        }
    }

    @Test
    fun addInstallRenameFailureCleansStagingAndDoesNotCommitMetadata() {
        val document = documentId()
        val attachmentId = "rename-fail-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend()
        val stagingOperations = ControlledStagingOperations(failInstallAt = 1)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.add(
                    context, document, "rename.txt", "text/plain", "rename failure".toByteArray(),
                    attachmentId, backend, stagingOperations
                )
            }.isFailure)
            assertEquals(1, stagingOperations.installCalls)
            assertTrue(OriginalAttachmentStore.list(context, document, backend).isEmpty())
            assertFalse(File(directory, "$attachmentId.enc").exists())
            assertTrue(directory.listFiles().orEmpty().none {
                it.name.startsWith(".$attachmentId.") && it.name.endsWith(".import")
            })
        } finally {
            File(directory, "$attachmentId.enc").delete()
            directory.listFiles().orEmpty().filter {
                it.name.startsWith(".$attachmentId.") && it.name.endsWith(".import")
            }.forEach { it.delete() }
        }
    }

    @Test
    fun importFailureOnSecondInstallRollsBackFirstInstalledFileAndAllStagingFiles() {
        val document = documentId()
        val firstId = "install-a-" + UUID.randomUUID().toString().take(8)
        val secondId = "install-b-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val entries = JSONArray()
            .put(JSONObject().put("documentId", document).put("id", firstId).put("name", "a.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("first".toByteArray(), Base64.NO_WRAP)))
            .put(JSONObject().put("documentId", document).put("id", secondId).put("name", "b.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("second".toByteArray(), Base64.NO_WRAP)))
        val raw = JSONObject().put("version", 1).put("attachments", entries).toString()
        val backend = FailingMetadataBackend()
        val stagingOperations = ControlledStagingOperations(failInstallAt = 2)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.importArchive(context, raw, backend, stagingOperations)
            }.isFailure)
            assertEquals(2, stagingOperations.installCalls)
            assertTrue(OriginalAttachmentStore.list(context, document, backend).isEmpty())
            assertFalse(File(directory, "$firstId.enc").exists())
            assertFalse(File(directory, "$secondId.enc").exists())
            assertTrue(directory.listFiles().orEmpty().none {
                (it.name.startsWith(".$firstId.") || it.name.startsWith(".$secondId.")) && it.name.endsWith(".import")
            })
        } finally {
            File(directory, "$firstId.enc").delete()
            File(directory, "$secondId.enc").delete()
            directory.listFiles().orEmpty().filter {
                (it.name.startsWith(".$firstId.") || it.name.startsWith(".$secondId.")) && it.name.endsWith(".import")
            }.forEach { it.delete() }
        }
    }

    private class FailingMetadataBackend(
        initial: Map<String, String> = emptyMap(),
        var commitResult: Boolean = true,
        var applyChangesWhenReturningFalse: Boolean = false
    ) : OriginalAttachmentMetadataBackend {
        private val values = initial.toMutableMap()
        override val all: Map<String, *> get() = values.toMap()
        override fun getString(key: String, defaultValue: String?): String? = values[key] ?: defaultValue
        override fun commit(changes: Map<String, String?>): Boolean {
            if (commitResult || applyChangesWhenReturningFalse) {
                changes.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
            }
            return commitResult
        }
    }

    @Test
    fun addCommitFalsePreservesInstalledFileAndPartialMetadataUpdateRemainsConsistent() {
        val document = documentId()
        val attachmentId = "add-false-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend(commitResult = false, applyChangesWhenReturningFalse = true)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.add(
                    context, document, "report.txt", "text/plain",
                    "add transaction".toByteArray(Charsets.UTF_8), attachmentId, backend
                )
            }.isFailure)
            assertTrue(File(directory, attachmentId + ".enc").isFile)
            assertEquals(listOf(attachmentId), OriginalAttachmentStore.list(context, document, backend).map { it.id })
        } finally {
            File(directory, attachmentId + ".enc").delete()
        }
    }

    @Test
    fun removeCommitFalsePreservesMetadataAndEncryptedFile() {
        val document = documentId()
        val attachmentId = "remove-false-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend()
        try {
            OriginalAttachmentStore.add(
                context, document, "report.txt", "text/plain",
                "remove transaction".toByteArray(Charsets.UTF_8), attachmentId, backend
            )
            backend.commitResult = false
            assertTrue(runCatching { OriginalAttachmentStore.remove(context, document, attachmentId, backend) }.isFailure)
            assertEquals(listOf(attachmentId), OriginalAttachmentStore.list(context, document, backend).map { it.id })
            assertTrue(File(directory, attachmentId + ".enc").isFile)
        } finally {
            File(directory, attachmentId + ".enc").delete()
        }
    }

    @Test
    fun removeDocumentCommitFalsePreservesEveryEncryptedFile() {
        val document = documentId()
        val firstId = "scope-a-" + UUID.randomUUID().toString().take(8)
        val secondId = "scope-b-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend()
        try {
            OriginalAttachmentStore.add(context, document, "a.txt", "text/plain", "first".toByteArray(), firstId, backend)
            OriginalAttachmentStore.add(context, document, "b.txt", "text/plain", "second".toByteArray(), secondId, backend)
            backend.commitResult = false
            assertTrue(runCatching { OriginalAttachmentStore.removeDocument(context, document, backend) }.isFailure)
            assertEquals(setOf(firstId, secondId), OriginalAttachmentStore.list(context, document, backend).map { it.id }.toSet())
            assertTrue(File(directory, firstId + ".enc").isFile)
            assertTrue(File(directory, secondId + ".enc").isFile)
        } finally {
            File(directory, firstId + ".enc").delete()
            File(directory, secondId + ".enc").delete()
        }
    }

    @Test
    fun multiEntryImportCommitFalsePreservesAllInstalledFiles() {
        val document = documentId()
        val firstId = "import-a-" + UUID.randomUUID().toString().take(8)
        val secondId = "import-b-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val entries = JSONArray()
            .put(JSONObject().put("documentId", document).put("id", firstId).put("name", "a.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("first".toByteArray(), Base64.NO_WRAP)))
            .put(JSONObject().put("documentId", document).put("id", secondId).put("name", "b.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("second".toByteArray(), Base64.NO_WRAP)))
        val raw = JSONObject().put("version", 1).put("attachments", entries).toString()
        val backend = FailingMetadataBackend(commitResult = false)
        try {
            assertTrue(runCatching { OriginalAttachmentStore.importArchive(context, raw, backend) }.isFailure)
            assertTrue(File(directory, firstId + ".enc").isFile)
            assertTrue(File(directory, secondId + ".enc").isFile)
            assertTrue(OriginalAttachmentStore.list(context, document, backend).isEmpty())
            assertTrue(directory.listFiles().orEmpty().none {
                (it.name.startsWith("." + firstId + ".") || it.name.startsWith("." + secondId + ".")) && it.name.endsWith(".import")
            })
        } finally {
            File(directory, firstId + ".enc").delete()
            File(directory, secondId + ".enc").delete()
        }
    }


    @Test
    fun productionSharedPreferencesBackendPersistsAcrossFreshAdapterInstances() {
        val preferencesName = "attachment_backend_test_${UUID.randomUUID()}"
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val firstBackend = SharedPreferencesOriginalAttachmentMetadataBackend(preferences)
        val document = documentId()
        try {
            assertTrue(firstBackend.commit(mapOf(document to "[]")))
            val freshPreferences = context.createPackageContext(context.packageName, 0)
                .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            val freshBackend = SharedPreferencesOriginalAttachmentMetadataBackend(freshPreferences)
            assertEquals("[]", freshBackend.getString(document, null))
            assertTrue(freshBackend.commit(mapOf(document to null)))
            assertEquals(null, firstBackend.getString(document, null))
        } finally {
            preferences.edit().clear().commit()
        }
    }

    @Test
    fun reconciliationRemovesFilesLeftByImportWhenMetadataCommitDidNotApply() {
        val document = documentId()
        val firstId = "reconcile-a-" + UUID.randomUUID().toString().take(8)
        val secondId = "reconcile-b-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val entries = JSONArray()
            .put(JSONObject().put("documentId", document).put("id", firstId).put("name", "a.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("first".toByteArray(), Base64.NO_WRAP)))
            .put(JSONObject().put("documentId", document).put("id", secondId).put("name", "b.txt")
                .put("mime", "text/plain").put("dataBase64", Base64.encodeToString("second".toByteArray(), Base64.NO_WRAP)))
        val backend = FailingMetadataBackend(commitResult = false)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.importArchive(
                    context, JSONObject().put("version", 1).put("attachments", entries).toString(), backend
                )
            }.isFailure)
            assertTrue(File(directory, "$firstId.enc").isFile)
            assertTrue(File(directory, "$secondId.enc").isFile)

            OriginalAttachmentStore.reconcile(context, backend)

            assertFalse(File(directory, "$firstId.enc").exists())
            assertFalse(File(directory, "$secondId.enc").exists())
            assertTrue(OriginalAttachmentStore.list(context, document, backend).isEmpty())
        } finally {
            File(directory, "$firstId.enc").delete()
            File(directory, "$secondId.enc").delete()
        }
    }

    @Test
    fun reconciliationPreservesFilesWhenFailedCommitPartiallyAppliedMetadata() {
        val document = documentId()
        val attachmentId = "reconcile-partial-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend(commitResult = false, applyChangesWhenReturningFalse = true)
        try {
            assertTrue(runCatching {
                OriginalAttachmentStore.add(
                    context, document, "report.txt", "text/plain",
                    "partial commit".toByteArray(Charsets.UTF_8), attachmentId, backend
                )
            }.isFailure)
            assertTrue(File(directory, "$attachmentId.enc").isFile)

            OriginalAttachmentStore.reconcile(context, backend)

            assertTrue(File(directory, "$attachmentId.enc").isFile)
            assertEquals(listOf(attachmentId), OriginalAttachmentStore.list(context, document, backend).map { it.id })
        } finally {
            File(directory, "$attachmentId.enc").delete()
        }
    }

    @Test
    fun addRejectsAttachmentIdAlreadyOwnedByAnotherDocumentWithoutChangingFiles() {
        val firstDocument = documentId()
        val secondDocument = documentId()
        val attachmentId = "cross-owner-" + UUID.randomUUID().toString().take(8)
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val backend = FailingMetadataBackend()
        try {
            OriginalAttachmentStore.add(
                context, firstDocument, "first.txt", "text/plain", "first".toByteArray(),
                attachmentId, backend
            )
            assertTrue(runCatching {
                OriginalAttachmentStore.add(
                    context, secondDocument, "second.txt", "text/plain", "second".toByteArray(),
                    attachmentId, backend
                )
            }.isFailure)
            assertEquals(listOf(attachmentId), OriginalAttachmentStore.list(context, firstDocument, backend).map { it.id })
            assertTrue(OriginalAttachmentStore.list(context, secondDocument, backend).isEmpty())
            assertTrue(File(directory, "$attachmentId.enc").isFile)
        } finally {
            File(directory, "$attachmentId.enc").delete()
        }
    }

}
