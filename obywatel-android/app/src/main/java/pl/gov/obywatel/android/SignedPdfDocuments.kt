// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class SignedPdfRecord(
    val id: String,
    val name: String,
    val importedAt: Long,
    val signatureKind: String,
    val signatureFileName: String,
    val pdfHasSignatureField: Boolean
)

private object SignedPdfStore {
    private const val KEY_ALIAS = "obywatel_signed_pdf_store_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val META_FILE = "signed-pdf-index.enc"
    private const val MAX_PDF_BYTES = 50 * 1024 * 1024
    private const val MAX_SIGNATURE_BYTES = 10 * 1024 * 1024

    private fun directory(context: Context) = File(context.noBackupFilesDir, "signed-pdf").apply { mkdirs() }

    @Synchronized
    fun list(context: Context): List<SignedPdfRecord> = runCatching {
        val file = File(directory(context), META_FILE)
        if (!file.exists()) return emptyList()
        val bytes = decrypt(file.readBytes())
        val json = JSONArray(String(bytes, Charsets.UTF_8))
        (0 until json.length()).map { i ->
            val o = json.getJSONObject(i)
            SignedPdfRecord(o.getString("id"), o.getString("name"), o.getLong("importedAt"),
                o.optString("signatureKind", "unknown"), o.optString("signatureFileName"),
                o.optBoolean("pdfHasSignatureField", false))
        }
    }.getOrDefault(emptyList())

    @Synchronized
    fun importPdf(context: Context, uri: Uri): SignedPdfRecord {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_PDF_BYTES) { "PDF przekracza limit 50 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("Nie można odczytać wybranego pliku.")
        require(bytes.size >= 5 && String(bytes, 0, 5, Charsets.US_ASCII) == "%PDF-") {
            "Wybrany plik nie ma prawidłowego nagłówka PDF."
        }
        val id = UUID.randomUUID().toString()
        val name = queryName(context, uri).ifBlank { "dokument-$id.pdf" }.take(180)
        val hasSignatureField = String(bytes, Charsets.ISO_8859_1).let {
            it.contains("/ByteRange") && it.contains("/Contents") &&
                (it.contains("/Sig") || it.contains("/ETSI.CAdES.detached") || it.contains("/adbe.pkcs7.detached"))
        }
        File(directory(context), "$id.pdf.enc").writeBytes(encrypt(bytes))
        val record = SignedPdfRecord(id, name, System.currentTimeMillis(), "unknown", "", hasSignatureField)
        save(context, list(context) + record)
        return record
    }

    @Synchronized
    fun setSignatureKind(context: Context, id: String, kind: String) {
        val updated = list(context).map { if (it.id == id) it.copy(signatureKind = kind) else it }
        save(context, updated)
    }

    @Synchronized
    fun attachSignature(context: Context, id: String, uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_SIGNATURE_BYTES) { "Plik podpisu przekracza limit 10 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("Nie można odczytać pliku podpisu.")
        require(bytes.isNotEmpty()) { "Plik podpisu jest pusty." }
        val fileName = queryName(context, uri).ifBlank { "podpis.xml" }.take(180)
        File(directory(context), "$id.signature.enc").writeBytes(encrypt(bytes))
        val updated = list(context).map { if (it.id == id) it.copy(signatureFileName = fileName) else it }
        save(context, updated)
        return fileName
    }

    fun decryptPdfToCache(context: Context, id: String): File {
        val encrypted = File(directory(context), "$id.pdf.enc")
        require(encrypted.exists()) { "Nie znaleziono pliku PDF." }
        return File(context.cacheDir, "preview-$id.pdf").apply {
            writeBytes(decrypt(encrypted.readBytes()))
        }
    }

    @Synchronized
    fun delete(context: Context, id: String) {
        File(directory(context), "$id.pdf.enc").delete()
        File(directory(context), "$id.signature.enc").delete()
        File(context.cacheDir, "preview-$id.pdf").delete()
        save(context, list(context).filterNot { it.id == id })
    }


    /** Portable backup of original PDF bytes, detached signatures and metadata. */
    @Synchronized
    fun exportArchive(context: Context): String {
        val root = JSONObject().put("version", 1)
        val items = JSONArray()
        list(context).forEach { record ->
            val pdf = File(directory(context), "${record.id}.pdf.enc")
            require(pdf.isFile) { "Brak pliku PDF dla dokumentu: ${record.name}" }
            val item = JSONObject()
                .put("id", record.id).put("name", record.name).put("importedAt", record.importedAt)
                .put("signatureKind", record.signatureKind).put("signatureFileName", record.signatureFileName)
                .put("pdfHasSignatureField", record.pdfHasSignatureField)
                .put("pdf", java.util.Base64.getEncoder().encodeToString(decrypt(pdf.readBytes())))
            val signature = File(directory(context), "${record.id}.signature.enc")
            if (signature.isFile) item.put("signature",
                java.util.Base64.getEncoder().encodeToString(decrypt(signature.readBytes())))
            items.put(item)
        }
        root.put("documents", items)
        return root.toString()
    }

    @Synchronized
    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja archiwum PDF." }
        val items = root.optJSONArray("documents") ?: JSONArray()
        val restored = linkedMapOf<String, SignedPdfRecord>()
        list(context).forEach { restored[it.id] = it }
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val id = item.getString("id")
            require(id.matches(Regex("[A-Fa-f0-9-]{16,64}"))) { "Nieprawidłowy identyfikator PDF." }
            val pdf = java.util.Base64.getDecoder().decode(item.getString("pdf"))
            require(pdf.size in 6..MAX_PDF_BYTES && String(pdf, 0, 5, Charsets.US_ASCII) == "%PDF-") {
                "Nieprawidłowy plik PDF w archiwum."
            }
            File(directory(context), "$id.pdf.enc").writeBytes(encrypt(pdf))
            val signatureName = item.optString("signatureFileName")
            if (item.has("signature")) {
                val signature = java.util.Base64.getDecoder().decode(item.getString("signature"))
                require(signature.isNotEmpty() && signature.size <= MAX_SIGNATURE_BYTES) {
                    "Nieprawidłowy plik podpisu w archiwum."
                }
                File(directory(context), "$id.signature.enc").writeBytes(encrypt(signature))
            }
            restored[id] = SignedPdfRecord(
                id, item.getString("name").take(180), item.optLong("importedAt", System.currentTimeMillis()),
                item.optString("signatureKind", "unknown").take(40), signatureName.take(180),
                item.optBoolean("pdfHasSignatureField", false)
            )
        }
        save(context, restored.values.toList())
    }

    private fun save(context: Context, records: List<SignedPdfRecord>) {
        val json = JSONArray()
        records.forEach { r -> json.put(JSONObject().apply {
            put("id", r.id); put("name", r.name); put("importedAt", r.importedAt)
            put("signatureKind", r.signatureKind); put("signatureFileName", r.signatureFileName)
            put("pdfHasSignatureField", r.pdfHasSignatureField)
        }) }
        File(directory(context), META_FILE).writeBytes(encrypt(json.toString().toByteArray(Charsets.UTF_8)))
    }

    private fun queryName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0).orEmpty() else "" }.orEmpty()
    }.getOrDefault("")

    private fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(bytes)
    }

    private fun decrypt(bytes: ByteArray): ByteArray {
        require(bytes.size > 28) { "Nieprawidłowe zaszyfrowane archiwum." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
}

internal fun exportSignedPdfArchive(context: Context): String = SignedPdfStore.exportArchive(context)
internal fun importSignedPdfArchive(context: Context, raw: String) = SignedPdfStore.importArchive(context, raw)

@Composable
internal fun SignedPdfDocumentsScreen(onBack: () -> Unit, allowImport: Boolean = true, readOnly: Boolean = false) {
    val context = LocalContext.current
    var records by remember { mutableStateOf(SignedPdfStore.list(context)) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var pendingId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
    var pageIndex by remember { mutableStateOf(0) }
    var showSignatureChoice by remember { mutableStateOf(false) }
    var pendingRecord by remember { mutableStateOf<SignedPdfRecord?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<SignedPdfRecord?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { SignedPdfStore.importPdf(context, uri) }
            .onSuccess {
                records = SignedPdfStore.list(context)
                pendingRecord = it
                showSignatureChoice = true
                ActivityHistoryStore.record(context, "Dokumenty", "Zaimportowano dokument PDF", it.name)
            }
            .onFailure { status = "Import PDF nie powiódł się: ${it.message ?: "błąd odczytu"}" }
    }
    val signaturePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val id = pendingId
        if (uri != null && id != null) runCatching { SignedPdfStore.attachSignature(context, id, uri) }
            .onSuccess {
                status = "Dołączono plik podpisu: $it"
                records = SignedPdfStore.list(context)
                ActivityHistoryStore.record(context, "Dokumenty", "Dołączono plik podpisu do PDF", it)
            }
            .onFailure { status = "Nie udało się dołączyć podpisu: ${it.message ?: "błąd odczytu"}" }
        pendingId = null
    }

    val selected = if (readOnly) records.firstOrNull { it.id == selectedId } else null
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (readOnly) "‹  Wróć do dokumentów" else "‹  Więcej",
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = { if (readOnly && selected != null) selectedId = null else onBack() }).padding(vertical = 5.dp))
        Text(if (readOnly && selected != null) selected.name else if (readOnly) "Podgląd dokumentów PDF" else "Zarządzanie dokumentami PDF i podpisami",
            color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (readOnly && selected != null) {
            val document = selected
            SignatureKindLabel(document.signatureKind)
            Text("Data importu: " + SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("pl", "PL")).format(Date(document.importedAt)),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            PdfPagePreview(context, document.id, pageIndex, onPageCount = { count ->
                if (pageIndex >= count) pageIndex = (count - 1).coerceAtLeast(0)
            })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) }, enabled = pageIndex > 0) { Text("Poprzednia") }
                Text("Strona ${pageIndex + 1}", color = MaterialTheme.colorScheme.onSurface)
                OutlinedButton(onClick = { pageIndex++ }) { Text("Następna") }
            }
            Text("Podgląd wyłącznie do odczytu. Zarządzanie plikami i podpisami znajduje się w Więcej → Ustawienia → Pliki PDF i podpisy.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (!readOnly) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Oznaczenie podpisu a jego weryfikacja", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Aplikacja przechowuje PDF i może dołączyć zewnętrzny plik podpisu (np. XML/XAdES). Samo pole podpisu w PDF, nazwa pliku ani deklaracja użytkownika nie potwierdzają ważności podpisu, tożsamości podpisującego ani kwalifikowanego statusu certyfikatu.",
                            color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (allowImport) Button(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Importuj dokument PDF")
                }
            } else Text("Wybierz dokument, aby otworzyć jego podgląd. Ten widok nie umożliwia modyfikacji plików ani podpisów.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (records.isEmpty()) Text("Nie dodano jeszcze dokumentów PDF.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            records.sortedByDescending { it.importedAt }.forEach { record ->
                Card(
                    Modifier.fillMaxWidth().then(if (readOnly) Modifier.clickable { selectedId = record.id; pageIndex = 0 } else Modifier),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, contentDescription = "Dokument PDF", tint = MaterialTheme.colorScheme.primary)
                            Text(record.name, modifier = Modifier.weight(1f).padding(start = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            if (readOnly) Text("Podgląd", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                        SignatureKindLabel(record.signatureKind)
                        if (record.pdfHasSignatureField) Text("PDF zawiera strukturę pola podpisu cyfrowego — bez potwierdzenia jego ważności.",
                            color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                        Text(if (record.signatureFileName.isNotBlank()) "Plik podpisu: ${record.signatureFileName}" else "Brak dołączonego pliku podpisu",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        if (!readOnly) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { pendingId = record.id; signaturePicker.launch(arrayOf("application/xml", "text/xml", "application/octet-stream", "*/*")) },
                                    modifier = Modifier.weight(1f)) { Text(if (record.signatureFileName.isBlank()) "Dołącz podpis" else "Zmień podpis") }
                                OutlinedButton(onClick = { pendingRecord = record; showSignatureChoice = true },
                                    modifier = Modifier.weight(1f)) { Text("Oznacz rodzaj") }
                            }
                            TextButton(onClick = { deleteTarget = record; showDelete = true }) { Text("Usuń dokument") }
                        }
                    }
                }
            }
        }
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
    }

    if (!readOnly && showSignatureChoice && pendingRecord != null) {
        AlertDialog(
            onDismissRequest = { showSignatureChoice = false; pendingRecord = null },
            title = { Text("Rodzaj podpisu") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wybierz oznaczenie zgodne z dokumentem lub wynikiem jego weryfikacji.")
                listOf("epuap" to "Podpis zaufany ePUAP",
                    "qualified" to "Kwalifikowany podpis elektroniczny",
                    "unknown" to "Nieustalony / brak podpisu").forEach { (kind, label) ->
                    OutlinedButton(onClick = {
                        val target = pendingRecord!!
                        SignedPdfStore.setSignatureKind(context, target.id, kind)
                        ActivityHistoryStore.record(context, "Certyfikaty", "Oznaczono rodzaj podpisu dokumentu", target.name + " — " + kind)
                        records = SignedPdfStore.list(context)
                        showSignatureChoice = false
                        pendingRecord = null
                        status = "Zapisano oznaczenie rodzaju podpisu. Nie jest to wynik walidacji kryptograficznej."
                    }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            } },
            confirmButton = { TextButton(onClick = { showSignatureChoice = false; pendingRecord = null }) { Text("Zamknij") } }
        )
    }
    if (!readOnly && showDelete && deleteTarget != null) AlertDialog(
        onDismissRequest = { showDelete = false; deleteTarget = null },
        title = { Text("Usunąć dokument?") },
        text = { Text("Usunięty zostanie PDF oraz dołączony plik podpisu z lokalnego archiwum.") },
        confirmButton = { Button(onClick = {
            val target = deleteTarget ?: return@Button
            ActivityHistoryStore.record(context, "Dokumenty", "Usunięto dokument PDF", target.name)
            SignedPdfStore.delete(context, target.id)
            records = SignedPdfStore.list(context)
            deleteTarget = null
            showDelete = false
        }) { Text("Usuń") } },
        dismissButton = { TextButton(onClick = { showDelete = false; deleteTarget = null }) { Text("Anuluj") } }
    )
}

@Composable
private fun SignatureKindLabel(kind: String) {
    val (label, color) = when (kind) {
        "epuap" -> "Podpisano podpisem elektronicznym ePUAP" to MaterialTheme.colorScheme.tertiary
        "qualified" -> "Podpisano kwalifikowanym podpisem elektronicznym" to MaterialTheme.colorScheme.tertiary
        else -> "Rodzaj podpisu nieustalony" to MaterialTheme.colorScheme.tertiary
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        val signatureIcon = when (kind) {
            "epuap" -> Icons.Filled.Shield
            "qualified" -> Icons.Filled.Verified
            else -> Icons.Filled.Warning
        }
        Icon(signatureIcon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun PdfPagePreview(context: Context, id: String, pageIndex: Int, onPageCount: (Int) -> Unit) {
    val result = remember(id, pageIndex) {
        runCatching {
            val file = SignedPdfStore.decryptPdfToCache(context, id)
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(descriptor)
            val count = renderer.pageCount
            require(count > 0) { "Dokument PDF nie zawiera stron." }
            val index = pageIndex.coerceIn(0, count - 1)
            val page = renderer.openPage(index)
            val width = (page.width * 1.6f).toInt().coerceIn(500, 1600)
            val height = (page.height.toFloat() / page.width * width).toInt().coerceAtMost(2400)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close(); renderer.close(); descriptor.close(); file.delete()
            count to bitmap
        }
    }
    SideEffect { result.getOrNull()?.let { onPageCount(it.first) } }
    if (result.isSuccess) {
        Image(result.getOrThrow().second.asImageBitmap(), contentDescription = "Podgląd strony dokumentu PDF",
            modifier = Modifier.fillMaxWidth(), contentScale = androidx.compose.ui.layout.ContentScale.FillWidth)
    } else {
        Text("Nie można wyświetlić PDF: ${result.exceptionOrNull()?.message ?: "nieznany błąd"}",
            color = MaterialTheme.colorScheme.error)
    }
}
