// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
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
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class OriginalAttachment(val id: String, val name: String, val mime: String)

private object OriginalAttachmentStore {
    private const val PREFS = "original_document_attachments"
    private const val ALIAS = "obywatel_original_attachments_aes"
    private const val MAX_BYTES = 25 * 1024 * 1024

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

    fun list(context: Context, documentId: String): List<OriginalAttachment> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(documentId, "[]") ?: "[]"
        val json = JSONArray(raw)
        (0 until json.length()).map { i ->
            val item = json.getJSONObject(i)
            OriginalAttachment(item.getString("id"), item.getString("name"), item.getString("mime"))
        }
    }.getOrDefault(emptyList())

    fun add(context: Context, documentId: String, name: String, mime: String, bytes: ByteArray) {
        require(documentId.matches(Regex("[A-Za-z0-9_.:-]{1,128}"))) { "Nieprawidłowy identyfikator dokumentu." }
        require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES) { "Plik musi mieć od 1 bajta do 25 MB." }
        val safeMime = mime.substringBefore(';').trim().lowercase()
        require(safeMime == "application/pdf" || safeMime.startsWith("image/") ||
            safeMime == "text/plain" || safeMime == "text/csv" || safeMime == "text/xml" ||
            safeMime == "application/xml") { "Obsługiwane są PDF, obrazy JPG/PNG/WebP oraz pliki tekstowe." }
        if (safeMime == "application/pdf") require(bytes.size >= 5 && String(bytes, 0, 5, Charsets.US_ASCII) == "%PDF-") {
            "Wybrany plik nie ma prawidłowego nagłówka PDF."
        }
        if (safeMime.startsWith("text/") || safeMime.contains("xml")) {
            val sample = String(bytes.take(4096).toByteArray(), Charsets.UTF_8)
            require(!sample.contains("<!DOCTYPE", true) && !sample.contains("<!ENTITY", true)) {
                "Plik zawiera niedozwoloną deklarację DTD/ENTITY."
            }
        }
        val id = UUID.randomUUID().toString()
        val directory = File(context.noBackupFilesDir, "original-document-attachments").apply { mkdirs() }
        File(directory, "$id.enc").writeBytes(crypt(bytes, true))
        val updated = list(context, documentId).toMutableList()
        updated.add(OriginalAttachment(id, name.take(180).ifBlank { "Oryginał dokumentu" }, safeMime))
        val json = JSONArray()
        updated.forEach { item ->
            json.put(JSONObject().put("id", item.id).put("name", item.name).put("mime", item.mime))
        }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(documentId, json.toString()).commit()) { "Nie udało się zapisać załącznika." }
    }

    fun addFile(context: Context, documentId: String, uri: Uri) {
        val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Oryginał dokumentu"
        val mime = (context.contentResolver.getType(uri)
            ?: when (name.substringAfterLast('.', "").lowercase()) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                "txt" -> "text/plain"
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

    fun remove(context: Context, documentId: String, id: String) {
        val current = list(context, documentId)
        if (current.none { it.id == id }) return
        File(File(context.noBackupFilesDir, "original-document-attachments"), "$id.enc").delete()
        val updated = current.filterNot { it.id == id }
        val json = JSONArray()
        updated.forEach { item -> json.put(JSONObject().put("id", item.id).put("name", item.name).put("mime", item.mime)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(documentId, json.toString()).apply()
    }
}

@Composable
internal fun DocumentOriginalAttachments(documentId: String, readOnly: Boolean = false) {
    val context = LocalContext.current
    val items = remember(documentId) { mutableStateListOf<OriginalAttachment>().apply {
        addAll(OriginalAttachmentStore.list(context, documentId))
    } }
    val status = remember(documentId) { mutableStateOf("") }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { OriginalAttachmentStore.addFile(context, documentId, uri) }
            .onSuccess { items.clear(); items.addAll(OriginalAttachmentStore.list(context, documentId)); status.value = "Dołączono oryginał dokumentu." }
            .onFailure { status.value = it.localizedMessage ?: "Nie udało się dołączyć pliku." }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) runCatching { OriginalAttachmentStore.addPhoto(context, documentId, bitmap) }
            .onSuccess { items.clear(); items.addAll(OriginalAttachmentStore.list(context, documentId)); status.value = "Dołączono zdjęcie oryginału." }
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
                        }) { Text("Usuń") }
                    }
                }
                if (status.value.isNotBlank()) Text(status.value, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
