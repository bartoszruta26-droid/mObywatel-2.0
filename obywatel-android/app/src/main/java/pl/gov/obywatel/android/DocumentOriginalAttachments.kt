// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal fun decodeOriginalAttachmentBase64(encoded: String, maxBytes: Int): ByteArray {
    require(maxBytes > 0) { "Nieprawidłowy limit dekodowania załącznika." }
    require(encoded.isNotEmpty() && encoded.length <= (maxBytes * 4L / 3L + 4L).toInt()) {
        "Zakodowany załącznik przekracza dopuszczalny rozmiar."
    }
    val decoded = try {
        java.util.Base64.getDecoder().decode(encoded)
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("Nieprawidłowe kodowanie Base64 załącznika.", failure)
    }
    require(decoded.size <= maxBytes) { "Załącznik przekracza dopuszczalny rozmiar." }
    require(java.util.Base64.getEncoder().encodeToString(decoded) == encoded) {
        "Kodowanie Base64 załącznika nie jest kanoniczne."
    }
    return decoded
}

internal fun validateOriginalAttachmentImage(bytes: ByteArray) {
    require(bytes.isNotEmpty() && bytes.size <= 25 * 1024 * 1024) {
        "Obraz przekracza dopuszczalny rozmiar."
    }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val width = bounds.outWidth
    val height = bounds.outHeight
    require(width > 0 && height > 0) { "Nie można odczytać wymiarów obrazu." }
    require(width <= 12000 && height <= 12000 && width.toLong() * height.toLong() <= 40000000L) {
        "Wymiary obrazu przekraczają dopuszczalny limit."
    }
    var sampleSize = 1
    while (width / sampleSize > 2048 || height / sampleSize > 2048) sampleSize *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        ?: throw IllegalArgumentException("Plik obrazu jest uszkodzony lub ma nieobsługiwany format.")
    decoded.recycle()
}

internal fun validateOriginalAttachmentPayload(mime: String, bytes: ByteArray) {
    require(bytes.isNotEmpty() && bytes.size <= 25 * 1024 * 1024) {
        "Plik musi mieć od 1 bajta do 25 MB."
    }
    when (mime) {
        "application/pdf" -> require(bytes.size >= 5 &&
            String(bytes, 0, 5, Charsets.US_ASCII) == "%PDF-") {
            "Nieprawidłowy nagłówek PDF."
        }
        "image/jpeg" -> {
            require(bytes.size >= 3 && bytes[0] == 0xff.toByte() &&
                bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte()) {
                "Nieprawidłowa sygnatura JPEG."
            }
            validateOriginalAttachmentImage(bytes)
        }
        "image/png" -> {
            require(bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(
                0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
            ))) { "Nieprawidłowa sygnatura PNG." }
            validateOriginalAttachmentImage(bytes)
        }
        "image/webp" -> {
            require(bytes.size >= 12 && String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP") {
                "Nieprawidłowa sygnatura WebP."
            }
            validateOriginalAttachmentImage(bytes)
        }
        "text/plain", "text/csv", "application/xml" -> {
            // Scan the complete payload: declarations beyond a short prefix must
            // not bypass the DTD/ENTITY rejection rule.
            val content = String(bytes, Charsets.UTF_8)
            require(!content.contains("<!DOCTYPE", true) && !content.contains("<!ENTITY", true)) {
                "Plik zawiera niedozwoloną deklarację DTD/ENTITY."
            }
        }
        else -> throw IllegalArgumentException("Nieobsługiwany typ MIME załącznika: $mime")
    }
}


internal fun validateOriginalAttachmentMetadata(raw: String) {
    val json = JSONArray(raw)
    val seen = mutableSetOf<String>()
    for (index in 0 until json.length()) {
        val item = json.getJSONObject(index)
        val id = item.getString("id")
        require(id.matches(Regex("[A-Za-z0-9-]{1,64}")) && seen.add(id)) {
            "Nieprawidłowy lub powtórzony identyfikator załącznika."
        }
        val name = item.getString("name")
        val mime = item.getString("mime").substringBefore(';').trim().lowercase()
        require(name.isNotBlank() && name.length <= 180) { "Nieprawidłowa nazwa załącznika." }
        require(mime in setOf(
            "application/pdf", "image/jpeg", "image/png", "image/webp",
            "text/plain", "text/csv", "application/xml"
        )) { "Nieobsługiwany typ MIME w rejestrze załączników." }
    }
}

internal fun validateOriginalAttachmentIdOwner(
    attachmentId: String,
    requestedDocumentId: String,
    existingOwners: Map<String, String>
) {
    val existingOwner = existingOwners[attachmentId]
    require(existingOwner == null || existingOwner == requestedDocumentId) {
        "Identyfikator załącznika jest już przypisany do innego dokumentu: $attachmentId"
    }
}

internal fun validateOriginalAttachmentIdsOwnedByDocument(
    attachmentIds: Collection<String>,
    requestedDocumentId: String,
    existingOwners: Map<String, String>
) {
    attachmentIds.forEach { attachmentId ->
        require(existingOwners[attachmentId] == requestedDocumentId) {
            "Identyfikator załącznika nie należy wyłącznie do dokumentu: $attachmentId"
        }
    }
}

internal fun findOrphanedOriginalAttachmentIds(referencedIds: Set<String>, storedIds: Set<String>): Set<String> =
    storedIds - referencedIds

internal fun shouldDeleteInstalledAttachmentsAfterImportFailure(metadataCommitAttempted: Boolean): Boolean =
    !metadataCommitAttempted

internal fun shouldDeleteAddedAttachmentAfterFailure(metadataCommitAttempted: Boolean): Boolean =
    !metadataCommitAttempted

internal fun writeOriginalAttachmentStagingFile(staging: File, encryptedBytes: ByteArray) {
    require(encryptedBytes.isNotEmpty()) { "Zaszyfrowany załącznik jest pusty." }
    FileOutputStream(staging, false).use { output ->
        output.write(encryptedBytes)
        output.fd.sync()
    }
}

private data class OriginalAttachment(val id: String, val name: String, val mime: String)

private object OriginalAttachmentStore {
    private const val PREFS = "original_document_attachments"
    private const val ALIAS = "obywatel_original_attachments_aes"
    private const val MAX_BYTES = 25 * 1024 * 1024
    private const val MAX_ARCHIVE_BYTES = 50 * 1024 * 1024
    private const val MAX_ARCHIVE_ENTRIES = 200


    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }

    private fun crypt(bytes: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, key())
            return cipher.iv + cipher.doFinal(bytes)
        }
        require(bytes.size > 12) { "Uszkodzony załącznik." }
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }

    fun list(context: Context, documentId: String): List<OriginalAttachment> {
        require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
            "Nieprawidłowy identyfikator dokumentu."
        }
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(documentId, "[]") ?: "[]"
        validateOriginalAttachmentMetadata(raw)
        val json = JSONArray(raw)
        return (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)
            OriginalAttachment(
                item.getString("id"),
                item.getString("name"),
                item.getString("mime").substringBefore(';').trim().lowercase()
            )
        }
    }

    /**
     * Reconciles the encrypted attachment directory with committed metadata.
     * Missing referenced files are treated as integrity failures; unreferenced
     * encrypted files and abandoned import staging files are safe to remove.
     */
    @Synchronized
    fun reconcile(context: Context) {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val referencedIds = mutableSetOf<String>()
        preferences.all.forEach { (documentId, value) ->
            require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
                "Nieprawidłowy identyfikator dokumentu w magazynie."
            }
            val raw = value as? String ?: error("Nieprawidłowy rejestr załączników: $documentId")
            validateOriginalAttachmentMetadata(raw)
            val json = JSONArray(raw)
            for (index in 0 until json.length()) {
                val id = json.getJSONObject(index).getString("id")
                require(referencedIds.add(id)) {
                    "Ten sam identyfikator załącznika jest przypisany do więcej niż jednego dokumentu."
                }
            }
        }
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        if (!directory.exists()) return
        // Staging files are never referenced by committed metadata. Clean them
        // before integrity checks so a missing attachment cannot prevent recovery
        // from an interrupted import.
        directory.listFiles().orEmpty()
            .filter { it.isFile && it.name.startsWith(".") && it.name.endsWith(".import") }
            .forEach { temporary ->
                check(temporary.delete() || !temporary.exists()) {
                    "Nie udało się usunąć tymczasowego pliku importu."
                }
            }
        val storedIds = directory.listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".enc") }
            .map { it.name.removeSuffix(".enc") }
            .toSet()
        val missing = referencedIds - storedIds
        check(missing.isEmpty()) {
            "Brakuje zaszyfrowanych plików załączników: ${missing.joinToString()}"
        }
        findOrphanedOriginalAttachmentIds(referencedIds, storedIds).forEach { id ->
            val orphan = File(directory, "$id.enc")
            check(!orphan.exists() || orphan.delete() || !orphan.exists()) {
                "Nie udało się usunąć osieroconego załącznika: $id"
            }
        }
    }

    @Synchronized
    fun add(
        context: Context,
        documentId: String,
        name: String,
        mime: String,
        bytes: ByteArray,
        attachmentId: String = UUID.randomUUID().toString()
    ) {
        require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) { "Nieprawidłowy identyfikator dokumentu." }
        require(attachmentId.matches(Regex("[A-Za-z0-9-]{1,64}"))) { "Nieprawidłowy identyfikator załącznika." }
        val safeMime = mime.substringBefore(';').trim().lowercase()
        validateOriginalAttachmentPayload(safeMime, bytes)
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existingOwners = mutableMapOf<String, String>()
        preferences.all.forEach { (ownerDocumentId, value) ->
            require(ownerDocumentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
                "Nieprawidłowy identyfikator dokumentu w magazynie."
            }
            val rawMetadata = value as? String
                ?: error("Nieprawidłowy rejestr załączników: $ownerDocumentId")
            validateOriginalAttachmentMetadata(rawMetadata)
            val metadata = JSONArray(rawMetadata)
            for (index in 0 until metadata.length()) {
                val existingId = metadata.getJSONObject(index).getString("id")
                val previousOwner = existingOwners.put(existingId, ownerDocumentId)
                require(previousOwner == null) {
                    "Identyfikator załącznika jest przypisany do więcej niż jednego dokumentu: $existingId"
                }
            }
        }
        validateOriginalAttachmentIdOwner(attachmentId, documentId, existingOwners)
        val updated = list(context, documentId).toMutableList()
        require(updated.none { it.id == attachmentId }) { "Załącznik o tym identyfikatorze już istnieje." }
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val target = File(directory, "$attachmentId.enc")
        require(!target.exists()) { "Plik załącznika o tym identyfikatorze już istnieje." }
        val staging = File(directory, ".$attachmentId.${UUID.randomUUID()}.import")
        var targetInstalled = false
        var metadataCommitAttempted = false
        try {
            // Install the encrypted payload through a synced staging file. A
            // failed/partial write must never leave a truncated final .enc file.
            writeOriginalAttachmentStagingFile(staging, crypt(bytes, true))
            check(staging.renameTo(target)) { "Nie udało się atomowo zainstalować pliku załącznika." }
            targetInstalled = true

            updated.add(OriginalAttachment(attachmentId, name.take(180).ifBlank { "Oryginał dokumentu" }, safeMime))
            val json = JSONArray()
            updated.forEach { item ->
                json.put(JSONObject().put("id", item.id).put("name", item.name).put("mime", item.mime))
            }
            metadataCommitAttempted = true
            check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(documentId, json.toString()).commit()) { "Nie udało się zapisać załącznika." }
        } catch (failure: Throwable) {
            // A failed commit may have updated SharedPreferences' in-memory map.
            // Keep the installed encrypted file after the commit attempt;
            // reconciliation removes it only if durable metadata does not reference it.
            if (shouldDeleteAddedAttachmentAfterFailure(metadataCommitAttempted) &&
                targetInstalled && target.exists()) {
                target.delete()
            }
            throw failure
        } finally {
            staging.delete()
        }
    }

    fun addFile(context: Context, documentId: String, uri: Uri) {
        val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Oryginał dokumentu"
        val reportedMime = context.contentResolver.getType(uri)?.lowercase()
        val mime = ((reportedMime?.takeIf { it != "application/octet-stream" && it != "binary/octet-stream" })
            ?: when (name.substringAfterLast('.', "").lowercase()) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                "txt", "md", "log", "rtf" -> "text/plain"
                "csv" -> "text/csv"
                "xml" -> "application/xml"
                else -> ""
            }).lowercase()
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_BYTES) { "Plik przekracza limit 25 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("Nie można odczytać wybranego pliku.")
        add(context, documentId, name, mime, bytes)
    }

    fun addPhoto(context: Context, documentId: String, bitmap: Bitmap) {
        val output = ByteArrayOutputStream()
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) { "Nie udało się przygotować zdjęcia." }
        add(context, documentId, "Zdjęcie oryginału.jpg", "image/jpeg", output.toByteArray())
    }

    @Synchronized
    fun remove(context: Context, documentId: String, id: String) {
        val current = list(context, documentId)
        if (current.none { it.id == id }) return
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existingOwners = mutableMapOf<String, String>()
        preferences.all.forEach { (ownerDocumentId, value) ->
            require(ownerDocumentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
                "Nieprawidłowy identyfikator dokumentu w magazynie."
            }
            val rawMetadata = value as? String
                ?: error("Nieprawidłowy rejestr załączników: $ownerDocumentId")
            validateOriginalAttachmentMetadata(rawMetadata)
            val metadata = JSONArray(rawMetadata)
            for (index in 0 until metadata.length()) {
                val existingId = metadata.getJSONObject(index).getString("id")
                val previousOwner = existingOwners.put(existingId, ownerDocumentId)
                require(previousOwner == null) {
                    "Identyfikator załącznika jest przypisany do więcej niż jednego dokumentu: $existingId"
                }
            }
        }
        validateOriginalAttachmentIdsOwnedByDocument(listOf(id), documentId, existingOwners)
        val updated = current.filterNot { it.id == id }
        val json = JSONArray()
        updated.forEach { item -> json.put(JSONObject().put("id", item.id).put("name", item.name).put("mime", item.mime)) }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(documentId, json.toString()).commit()) { "Nie udało się zaktualizować rejestru załączników." }
        // Metadata is committed first: a failed commit must not orphan a live reference.
        val file = File(File(context.noBackupFilesDir, "original-document-attachments"), "$id.enc")
        if (file.exists()) check(file.delete() || !file.exists()) { "Nie udało się usunąć pliku załącznika." }
    }

    /** Removes one complete, exclusively owned attachment scope. */
    @Synchronized
    fun removeDocument(context: Context, documentId: String) {
        require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
            "Nieprawidłowy identyfikator dokumentu."
        }
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = preferences.getString(documentId, null) ?: return
        val items = JSONArray(raw)
        validateOriginalAttachmentMetadata(raw)
        val ids = (0 until items.length()).map { index ->
            items.getJSONObject(index).getString("id")
        }
        val existingOwners = mutableMapOf<String, String>()
        preferences.all.forEach { (ownerDocumentId, value) ->
            require(ownerDocumentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) {
                "Nieprawidłowy identyfikator dokumentu w magazynie."
            }
            val rawMetadata = value as? String
                ?: error("Nieprawidłowy rejestr załączników: $ownerDocumentId")
            validateOriginalAttachmentMetadata(rawMetadata)
            val metadata = JSONArray(rawMetadata)
            for (index in 0 until metadata.length()) {
                val existingId = metadata.getJSONObject(index).getString("id")
                val previousOwner = existingOwners.put(existingId, ownerDocumentId)
                require(previousOwner == null) {
                    "Identyfikator załącznika jest przypisany do więcej niż jednego dokumentu: $existingId"
                }
            }
        }
        validateOriginalAttachmentIdsOwnedByDocument(ids, documentId, existingOwners)
        check(preferences.edit().remove(documentId).commit()) {
            "Nie udało się usunąć rejestru załączników."
        }
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        ids.forEach { id ->
            val file = File(directory, "$id.enc")
            if (file.exists()) check(file.delete() || !file.exists()) {
                "Nie udało się usunąć pliku załącznika."
            }
        }
    }

    /**
     * Includes both attachment metadata and decrypted source bytes in the
     * application archive. Export fails rather than silently omitting a
     * missing/corrupt source file.
     */
    @Synchronized
    fun exportArchive(context: Context): String {
        val entries = JSONArray()
        var totalBytes = 0L
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val directory = File(context.noBackupFilesDir, "original-document-attachments")
        preferences.all.toSortedMap().forEach { (documentId, rawValue) ->
            require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) { "Nieprawidłowy identyfikator dokumentu w załącznikach." }
            val raw = rawValue as? String ?: error("Nieprawidłowy rejestr załączników: $documentId")
            validateOriginalAttachmentMetadata(raw)
            val items = JSONArray(raw)
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val id = item.getString("id")
                require(id.matches(Regex("[A-Za-z0-9-]{1,64}"))) { "Nieprawidłowy identyfikator załącznika." }
                val encryptedFile = File(directory, "$id.enc")
                require(encryptedFile.isFile) { "Brakuje pliku źródłowego załącznika: ${item.optString("name")}" }
                val bytes = crypt(encryptedFile.readBytes(), false)
                validateOriginalAttachmentPayload(item.getString("mime").lowercase(), bytes)
                totalBytes += bytes.size
                require(totalBytes <= MAX_ARCHIVE_BYTES && entries.length() < MAX_ARCHIVE_ENTRIES) {
                    "Archiwum przekracza limit 50 MB lub 200 załączników."
                }
                entries.put(JSONObject()
                    .put("documentId", documentId)
                    .put("id", id)
                    .put("name", item.getString("name"))
                    .put("mime", item.getString("mime"))
                    .put("dataBase64", android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)))
            }
        }
        return JSONObject().put("version", 1).put("attachments", entries).toString()
    }

    /** Validates the complete archive before committing any imported attachment. */
    @Synchronized
    fun importArchive(context: Context, raw: String) {
        require(raw.length <= (MAX_ARCHIVE_BYTES * 4L / 3L + 1024 * 1024).toInt()) {
            "Archiwum przekracza maksymalny rozmiar."
        }
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja archiwum załączników." }
        require(root.has("attachments") && root.optJSONArray("attachments") != null) {
            "Archiwum nie zawiera prawidłowej listy załączników."
        }
        val entries = root.getJSONArray("attachments")
        require(entries.length() <= MAX_ARCHIVE_ENTRIES) { "Archiwum zawiera ponad 200 załączników." }
        var totalBytes = 0L
        data class Pending(
            val documentId: String,
            val id: String,
            val name: String,
            val mime: String,
            val bytes: ByteArray
        )
        val seen = mutableSetOf<String>()
        val decoded = (0 until entries.length()).map { index ->
            val item = entries.getJSONObject(index)
            val documentId = item.getString("documentId")
            require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) { "Nieprawidłowy identyfikator dokumentu." }
            val archivedId = item.optString("id")
            val id = archivedId.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            require(id.matches(Regex("[A-Za-z0-9-]{1,64}"))) { "Nieprawidłowy identyfikator załącznika." }
            require(seen.add(id)) { "Archiwum zawiera powtórzony identyfikator załącznika: $id" }
            val name = item.getString("name").take(180).ifBlank { "Oryginał dokumentu" }
            val mime = item.getString("mime").substringBefore(';').trim().lowercase()
            val encoded = item.getString("dataBase64")
            require(encoded.length <= (MAX_BYTES * 4L / 3L + 8).toInt()) {
                "Załącznik przekracza limit 25 MB."
            }
            val bytes = decodeOriginalAttachmentBase64(encoded, MAX_BYTES)
            validateOriginalAttachmentPayload(mime, bytes)
            totalBytes += bytes.size
            require(totalBytes <= MAX_ARCHIVE_BYTES) { "Archiwum przekracza łączny limit 50 MB." }
            Pending(documentId, id, name, mime, bytes)
        }

        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existingByDocument = mutableMapOf<String, MutableList<OriginalAttachment>>()
        preferences.all.forEach { (documentId, value) ->
            require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) { "Nieprawidłowy identyfikator dokumentu w magazynie." }
            val rawMetadata = value as? String ?: error("Nieprawidłowy rejestr załączników: $documentId")
            validateOriginalAttachmentMetadata(rawMetadata)
            val json = JSONArray(rawMetadata)
            existingByDocument[documentId] = (0 until json.length()).map { i ->
                val item = json.getJSONObject(i)
                OriginalAttachment(
                    item.getString("id"),
                    item.getString("name"),
                    item.getString("mime").substringBefore(';').trim().lowercase()
                )
            }.toMutableList()
        }
        val existingIds = mutableSetOf<String>()
        existingByDocument.values.flatten().forEach { item ->
            require(existingIds.add(item.id)) {
                "Identyfikator załącznika jest już przypisany do więcej niż jednego dokumentu: ${item.id}"
            }
        }

        val existingOwners = existingByDocument.flatMap { (ownerDocumentId, metadata) ->
            metadata.map { it.id to ownerDocumentId }
        }.toMap()

        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        val staged = mutableListOf<Pair<File, File>>()
        val newlyInstalled = mutableListOf<File>()
        var metadataCommitAttempted = false
        try {
            decoded.forEach { item ->
                validateOriginalAttachmentIdOwner(item.id, item.documentId, existingOwners)
                val metadata = existingByDocument.getOrPut(item.documentId) { mutableListOf() }
                val current = metadata.firstOrNull { it.id == item.id }
                val target = File(directory, "${item.id}.enc")
                if (current != null || target.exists()) {
                    require(current != null && current.name == item.name && current.mime == item.mime &&
                        target.isFile && crypt(target.readBytes(), false).contentEquals(item.bytes)) {
                        "Załącznik o identyfikatorze ${item.id} już istnieje z inną zawartością lub metadanymi."
                    }
                } else {
                    val temporary = File(directory, ".${item.id}.${UUID.randomUUID()}.import")
                    // Register the staging file before I/O so finally can clean it
                    // even when writing or syncing fails partway through.
                    staged.add(temporary to target)
                    writeOriginalAttachmentStagingFile(temporary, crypt(item.bytes, true))
                    metadata.add(OriginalAttachment(item.id, item.name, item.mime))
                }
            }

            staged.forEach { (temporary, target) ->
                check(temporary.renameTo(target)) { "Nie udało się przygotować pliku załącznika." }
                newlyInstalled.add(target)
            }
            val editor = preferences.edit()
            existingByDocument.forEach { (documentId, metadata) ->
                val json = JSONArray()
                metadata.forEach { item ->
                    json.put(JSONObject().put("id", item.id).put("name", item.name).put("mime", item.mime))
                }
                editor.putString(documentId, json.toString())
            }
            metadataCommitAttempted = true
            check(editor.commit()) { "Nie udało się zatwierdzić rejestru importowanych załączników." }
        } catch (failure: Throwable) {
            // A failed commit may still have changed SharedPreferences' in-memory map.
            // Keep installed files after commit was attempted; startup reconciliation
            // removes them only when durable metadata does not reference them.
            if (shouldDeleteInstalledAttachmentsAfterImportFailure(metadataCommitAttempted)) {
                newlyInstalled.forEach { it.delete() }
            }
            throw failure
        } finally {
            staged.forEach { (temporary, _) -> temporary.delete() }
        }
    }
}

internal fun reconcileOriginalAttachments(context: Context) = OriginalAttachmentStore.reconcile(context)

internal fun listOriginalAttachmentIds(context: Context, documentId: String): List<String> =
    OriginalAttachmentStore.list(context, documentId).map { it.id }

internal fun exportOriginalAttachmentArchive(context: Context): String = OriginalAttachmentStore.exportArchive(context)

internal fun importOriginalAttachmentArchive(context: Context, raw: String) = OriginalAttachmentStore.importArchive(context, raw)

internal fun removeOriginalAttachmentDocumentScope(context: Context, documentId: String) =
    OriginalAttachmentStore.removeDocument(context, documentId)

@Composable
internal fun DocumentOriginalAttachments(
    documentId: String,
    readOnly: Boolean = false,
    onAttachmentIdsChanged: (List<String>) -> Unit = {}
) {
    val context = LocalContext.current
    val items = remember(documentId) { mutableStateListOf<OriginalAttachment>().apply {
        addAll(OriginalAttachmentStore.list(context, documentId))
    } }
    val status = remember(documentId) { mutableStateOf("") }
    LaunchedEffect(Unit) {
        runCatching { OriginalAttachmentStore.reconcile(context) }
            .onFailure { status.value = it.localizedMessage ?: "Nie udało się sprawdzić magazynu załączników." }
    }
    LaunchedEffect(documentId, items.size) {
        onAttachmentIdsChanged(items.map { it.id })
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { OriginalAttachmentStore.addFile(context, documentId, uri) }
            .onSuccess { items.clear(); items.addAll(OriginalAttachmentStore.list(context, documentId)); onAttachmentIdsChanged(items.map { it.id }); status.value = "Dołączono oryginał dokumentu." }
            .onFailure { status.value = it.localizedMessage ?: "Nie udało się dołączyć pliku." }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) runCatching { OriginalAttachmentStore.addPhoto(context, documentId, bitmap) }
            .onSuccess { items.clear(); items.addAll(OriginalAttachmentStore.list(context, documentId)); onAttachmentIdsChanged(items.map { it.id }); status.value = "Dołączono zdjęcie oryginału." }
            .onFailure { status.value = it.localizedMessage ?: "Nie udało się zapisać zdjęcia." }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Oryginał dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Dołącz skan lub plik źródłowy. Załączniki są przypisane do tego dokumentu i szyfrowane lokalnie.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (!readOnly) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { filePicker.launch(arrayOf("application/pdf", "image/*", "text/plain", "text/*")) },
                            modifier = Modifier.weight(1f)) { Text("Wybierz plik") }
                        OutlinedButton(onClick = { camera.launch(null) }, modifier = Modifier.weight(1f)) { Text("Zrób zdjęcie") }
                    }
                }
                if (items.isEmpty()) Text("Brak załączonego oryginału.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Text(item.mime, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        if (!readOnly) TextButton(onClick = {
                            OriginalAttachmentStore.remove(context, documentId, item.id)
                            items.remove(item)
                            onAttachmentIdsChanged(items.map { it.id })
                        }) { Text("Usuń") }
                    }
                }
                if (status.value.isNotBlank()) Text(status.value, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
