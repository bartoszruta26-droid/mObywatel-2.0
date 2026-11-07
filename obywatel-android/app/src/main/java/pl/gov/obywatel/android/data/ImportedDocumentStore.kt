// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Minimalny magazyn lokalnych odwołań do plików wybranych przez użytkownika.
 * Pliki pozostają w prywatnych lokalizacjach dostawców; aplikacja przechowuje
 * wyłącznie URI z trwałym uprawnieniem SAF, a nie kopiuje dokumentów.
 */
data class ImportedDocument(
    val uri: Uri,
    val title: String,
    val mimeType: String
)

object ImportedDocumentStore {
    private const val PREFS = "imported_documents"
    private const val KEY_URIS = "document_uris"

    fun list(context: Context): List<ImportedDocument> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (prefs.getStringSet(KEY_URIS, emptySet()) ?: emptySet())
            .mapNotNull { raw -> runCatching { fromUri(context, Uri.parse(raw)) }.getOrNull() }
            .sortedBy { it.title.lowercase() }
    }

    /**
     * Waliduje typ pliku przed utrwaleniem uprawnienia URI. W razie błędu zapisu
     * zwalnia wyłącznie uprawnienie uzyskane przez to wywołanie.
     */
    fun add(context: Context, uri: Uri): ImportedDocument {
        val document = fromUri(context, uri)
        val resolver = context.contentResolver
        val hadReadPermission = resolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }

        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val updated = HashSet(prefs.getStringSet(KEY_URIS, emptySet()) ?: emptySet())
            updated.add(uri.toString())
            check(prefs.edit().putStringSet(KEY_URIS, updated).commit()) {
                "Nie udało się zapisać listy dokumentów."
            }
            return document
        } catch (failure: Exception) {
            if (!hadReadPermission) {
                runCatching {
                    resolver.releasePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            throw failure
        }
    }

    fun remove(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = HashSet(prefs.getStringSet(KEY_URIS, emptySet()) ?: emptySet())
        updated.remove(uri.toString())
        prefs.edit().putStringSet(KEY_URIS, updated).apply()
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun fromUri(context: Context, uri: Uri): ImportedDocument {
        val title = context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uri.lastPathSegment ?: "Dokument"
        val mime = context.contentResolver.getType(uri).orEmpty()
        require(
            mime == "application/pdf" ||
                mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                mime.startsWith("image/")
        ) {
            "Obsługiwane są pliki PDF, DOCX oraz obrazy."
        }
        return ImportedDocument(uri, title, mime)
    }
}
