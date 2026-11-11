// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

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
        val wasStored = updated.remove(uri.toString())
        if (!wasStored) return

        check(prefs.edit().putStringSet(KEY_URIS, updated).commit()) {
            "Nie udało się usunąć dokumentu z listy."
        }
        if (uri.scheme == "file") {
            val base = File(context.filesDir, "imported-documents").canonicalFile
            val file = File(uri.path.orEmpty()).canonicalFile
            if (file.path.startsWith(base.path + File.separator)) file.delete()
        } else {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    }


    fun exportArchive(context: Context): String {
        val items = JSONArray()
        list(context).forEach { document ->
            val bytes = context.contentResolver.openInputStream(document.uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 25 * 1024 * 1024) { "Załącznik przekracza limit 25 MB: ${document.title}" }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: error("Nie można odczytać załącznika: ${document.title}")
            items.put(JSONObject().put("title", document.title).put("mimeType", document.mimeType)
                .put("bytes", java.util.Base64.getEncoder().encodeToString(bytes)))
        }
        return JSONObject().put("version", 1).put("documents", items).toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja załączników." }
        val directory = File(context.filesDir, "imported-documents").apply { mkdirs() }
        val uris = HashSet(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_URIS, emptySet()) ?: emptySet())
        val items = root.optJSONArray("documents") ?: JSONArray()
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val mime = item.getString("mimeType").lowercase()
            require(isSupportedDocumentMimeType(mime)) { "Nieobsługiwany typ załącznika: $mime" }
            val bytes = java.util.Base64.getDecoder().decode(item.getString("bytes"))
            require(bytes.isNotEmpty() && bytes.size <= 25 * 1024 * 1024) { "Załącznik przekracza limit 25 MB." }
            val extension = when (mime) {\n                "application/vnd.oasis.opendocument.text" -> "odt"\n                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"\n                "application/pdf" -> "pdf"\n                else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.take(12) ?: "bin"\n            }
            val target = File(directory, "${java.util.UUID.randomUUID()}.$extension")
            target.writeBytes(bytes)
            uris.add(Uri.fromFile(target).toString())
        }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_URIS, uris).commit()) { "Nie udało się zapisać listy załączników." }
    }

    private fun fromUri(context: Context, uri: Uri): ImportedDocument {
        val isPrivateFile = uri.scheme == "file"
        if (isPrivateFile) {
            val base = File(context.filesDir, "imported-documents").canonicalFile
            val file = File(uri.path.orEmpty()).canonicalFile
            require(file.path.startsWith(base.path + File.separator) && file.isFile) {
                "Odwołanie do załącznika lokalnego jest nieprawidłowe."
            }
        } else {
            require(uri.scheme == "content" && !uri.authority.isNullOrBlank()) {
                "Dokument musi pochodzić z dostawcy plików systemu Android."
            }
        }

        val queriedTitle = if (isPrivateFile) null else context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        val title = queriedTitle?.trim()?.takeIf { it.isNotEmpty() }
            ?: uri.lastPathSegment?.substringAfterLast('/')?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Dokument"
        val inferredMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            title.substringAfterLast('.', "").lowercase()
        )
        val mime = (if (isPrivateFile) inferredMime else context.contentResolver.getType(uri))
            .orEmpty().trim().lowercase()
        require(isSupportedDocumentMimeType(mime)) {
            "Obsługiwane są pliki PDF, DOCX oraz obrazy rastrowe (JPEG, PNG, WebP, GIF, BMP)."
        }
        return ImportedDocument(uri, title, mime)
    }
}
