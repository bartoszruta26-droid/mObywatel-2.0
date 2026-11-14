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
    private const val MAX_DOCUMENT_BYTES = 25L * 1024 * 1024
    private const val MAX_ARCHIVE_BYTES = 50L * 1024 * 1024
    private const val MAX_ARCHIVE_DOCUMENTS = 100
    private const val MAX_ARCHIVE_JSON_CHARS = 70 * 1024 * 1024

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
        val documents = list(context)
        require(documents.size <= MAX_ARCHIVE_DOCUMENTS) {
            "Archiwum może zawierać maksymalnie $MAX_ARCHIVE_DOCUMENTS dokumentów."
        }
        val items = JSONArray()
        var archiveBytes = 0L
        documents.forEach { document ->
            val bytes = context.contentResolver.openInputStream(document.uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var documentBytes = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    documentBytes += count
                    archiveBytes += count
                    require(documentBytes <= MAX_DOCUMENT_BYTES) {
                        "Załącznik przekracza limit 25 MB: ${document.title}"
                    }
                    require(archiveBytes <= MAX_ARCHIVE_BYTES) {
                        "Łączny rozmiar archiwum przekracza limit 50 MB."
                    }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: error("Nie można odczytać załącznika: ${document.title}")
            items.put(JSONObject().put("title", document.title).put("mimeType", document.mimeType)
                .put("bytes", java.util.Base64.getEncoder().encodeToString(bytes)))
        }
        return JSONObject().put("version", 1).put("documents", items).toString()
    }

    /**
     * Importuje archiwum transakcyjnie: najpierw waliduje i zapisuje pliki w katalogu
     * tymczasowym, a dopiero potem przenosi je do magazynu i zatwierdza indeks.
     * W przypadku błędu usuwa wszystkie pliki utworzone przez tę operację.
     */
    fun importArchive(context: Context, raw: String) {
        require(raw.length <= MAX_ARCHIVE_JSON_CHARS) {
            "Archiwum przekracza dopuszczalny rozmiar."
        }
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja załączników." }
        val items = root.optJSONArray("documents") ?: JSONArray()
        require(items.length() <= MAX_ARCHIVE_DOCUMENTS) {
            "Archiwum może zawierać maksymalnie $MAX_ARCHIVE_DOCUMENTS dokumentów."
        }

        val directory = File(context.filesDir, "imported-documents")
        check(directory.exists() || directory.mkdirs()) {
            "Nie można utworzyć magazynu załączników."
        }
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val originalUris = HashSet(preferences.getStringSet(KEY_URIS, emptySet()) ?: emptySet())
        val updatedUris = HashSet(originalUris)
        val stagingDirectory = File(directory, ".import-${java.util.UUID.randomUUID()}")
        check(stagingDirectory.mkdir()) { "Nie można utworzyć katalogu tymczasowego." }
        val committedFiles = mutableListOf<File>()
        var archiveBytes = 0L

        try {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val mime = item.getString("mimeType").trim().lowercase()
                require(isSupportedDocumentMimeType(mime)) {
                    "Nieobsługiwany typ załącznika: $mime"
                }
                val bytes = java.util.Base64.getDecoder().decode(item.getString("bytes"))
                require(bytes.isNotEmpty() && bytes.size <= MAX_DOCUMENT_BYTES) {
                    "Załącznik musi mieć rozmiar od 1 do 25 MB."
                }
                archiveBytes += bytes.size
                require(archiveBytes <= MAX_ARCHIVE_BYTES) {
                    "Łączny rozmiar archiwum przekracza limit 50 MB."
                }

                val extension = when (mime) {
                    "application/vnd.oasis.opendocument.text" -> "odt"
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                    "application/pdf" -> "pdf"
                    else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.take(12) ?: "bin"
                }
                val stagedFile = File(stagingDirectory, "${java.util.UUID.randomUUID()}.$extension")
                stagedFile.writeBytes(bytes)
                val target = File(directory, "${java.util.UUID.randomUUID()}.$extension")
                check(stagedFile.renameTo(target)) { "Nie udało się przenieść załącznika do magazynu." }
                committedFiles.add(target)
                updatedUris.add(Uri.fromFile(target).toString())
            }

            check(preferences.edit().putStringSet(KEY_URIS, updatedUris).commit()) {
                "Nie udało się zapisać listy załączników."
            }
        } catch (failure: Exception) {
            preferences.edit().putStringSet(KEY_URIS, originalUris).commit()
            committedFiles.forEach { it.delete() }
            throw failure
        } finally {
            stagingDirectory.deleteRecursively()
        }
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
            "Obsługiwane są pliki PDF, DOCX, ODT oraz obrazy rastrowe (JPEG, PNG, WebP, GIF, BMP)."
        }
        return ImportedDocument(uri, title, mime)
    }
}
