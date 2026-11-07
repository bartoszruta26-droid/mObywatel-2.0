// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

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
            preferences.edit().clear().commit()
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
    fun rejectsNetworkUrisBeforeAccessingDocumentProvider() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportedDocumentStore.add(context, Uri.parse("https://example.org/document.pdf"))
        }
    }
}
