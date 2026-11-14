// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONArray
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class ImportedDocumentStoreTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun listSkipsDocumentsWhoseProviderIsUnavailable() {
        val preferences = context.getSharedPreferences("imported_documents", Context.MODE_PRIVATE)
        try {
            preferences.edit()
                .putStringSet("document_uris", setOf("content://missing.provider/document/1"))
                .commit()

            assertEquals(emptyList<ImportedDocument>(), ImportedDocumentStore.list(context))
        } finally {
            preferences.edit().remove("document_uris").commit()
        }
    }

    @Test
    fun removingUnknownUriLeavesStoredEntriesUnchanged() {
        val preferences = context.getSharedPreferences("imported_documents", Context.MODE_PRIVATE)
        val storedUri = "content://missing.provider/document/kept"
        try {
            preferences.edit().putStringSet("document_uris", setOf(storedUri)).commit()

            ImportedDocumentStore.remove(
                context,
                Uri.parse("content://missing.provider/document/other")
            )

            assertEquals(setOf(storedUri), preferences.getStringSet("document_uris", emptySet<String>()))
        } finally {
            preferences.edit().remove("document_uris").commit()
        }
    }

    @Test
    fun rejectsFileUrisBeforeAccessingDocumentProvider() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportedDocumentStore.add(context, Uri.parse("file:///tmp/document.pdf"))
        }
    }

    @Test
    fun rejectsUrisWithoutProviderAuthority() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportedDocumentStore.add(context, Uri.parse("content:///document/1"))
        }
    }


    @Test
    fun importArchiveRejectsMoreThanOneHundredDocumentsBeforeWritingFiles() {
        val documents = JSONArray()
        repeat(101) {
            documents.put(
                JSONObject()
                    .put("mimeType", "application/pdf")
                    .put("bytes", "YQ==")
            )
        }
        val archive = JSONObject().put("version", 1).put("documents", documents).toString()

        assertThrows(IllegalArgumentException::class.java) {
            ImportedDocumentStore.importArchive(context, archive)
        }
    }

    @Test
    fun importArchiveRollsBackStagedFilesWhenAnyEntryIsInvalid() {
        val preferences = context.getSharedPreferences("imported_documents", Context.MODE_PRIVATE)
        val originalUris = preferences.getStringSet("document_uris", emptySet<String>())?.toSet() ?: emptySet()
        val directory = java.io.File(context.filesDir, "imported-documents")
        val originalFiles = directory.list()?.toSet() ?: emptySet<String>()
        val documents = JSONArray()
            .put(
                JSONObject()
                    .put("mimeType", "application/pdf")
                    .put("bytes", "cGRm")
            )
            .put(
                JSONObject()
                    .put("mimeType", "application/x-unsupported")
                    .put("bytes", "YQ==")
            )
        val archive = JSONObject().put("version", 1).put("documents", documents).toString()

        try {
            assertThrows(IllegalArgumentException::class.java) {
                ImportedDocumentStore.importArchive(context, archive)
            }
            assertEquals(originalUris, preferences.getStringSet("document_uris", emptySet<String>()))
            assertEquals(originalFiles, directory.list()?.toSet() ?: emptySet<String>())
        } finally {
            preferences.edit().putStringSet("document_uris", originalUris).commit()
        }
    }

    @Test
    fun rejectsNetworkUrisBeforeAccessingDocumentProvider() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportedDocumentStore.add(context, Uri.parse("https://example.org/document.pdf"))
        }
    }
}
