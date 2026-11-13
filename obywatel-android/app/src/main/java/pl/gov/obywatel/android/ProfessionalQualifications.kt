// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

private data class ProfessionCredential(
    val id: String,
    val profession: String,
    val number: String,
    val numberType: String,
    val issuer: String,
    val issueDate: String,
    val validUntil: String,
    val registerName: String,
    val registerUrl: String,
    val scope: String,
    val legalBasis: String,
    val status: String
)

private data class QualificationFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val profession: String,
    val addedAt: Long,
    val size: Long
)

private object ProfessionalCredentialStore {
    private const val KEY_ALIAS = "obywatel_professional_credentials_aes"
    private const val INDEX = "professional-credentials.enc"
    private const val MAX_FILE = 25 * 1024 * 1024

    private fun dir(context: Context) = File(context.noBackupFilesDir, "professional-credentials").apply { mkdirs() }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    private fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(bytes)
    }

    private fun decrypt(bytes: ByteArray): ByteArray {
        require(bytes.size > 12) { "Uszkodzony zaszyfrowany dokument." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }

    @Synchronized
    fun professions(context: Context): List<ProfessionCredential> = runCatching {
        val file = File(dir(context), INDEX)
        if (!file.exists()) return emptyList()
        val array = JSONArray(String(decrypt(file.readBytes()), Charsets.UTF_8))
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            ProfessionCredential(o.getString("id"), o.getString("profession"), o.optString("number"),
                o.optString("numberType", "other"), o.optString("issuer"), o.optString("issueDate"),
                o.optString("validUntil"), o.optString("registerName"), o.optString("registerUrl"),
                o.optString("scope"), o.optString("legalBasis"), o.optString("status", "Do uzupełnienia"))
        }
    }.getOrDefault(emptyList())

    @Synchronized
    fun saveProfessions(context: Context, records: List<ProfessionCredential>) {
        val array = JSONArray()
        records.forEach { p ->
            array.put(JSONObject().put("id", p.id).put("profession", p.profession).put("number", p.number)
                .put("numberType", p.numberType).put("issuer", p.issuer).put("issueDate", p.issueDate)
                .put("validUntil", p.validUntil).put("registerName", p.registerName).put("registerUrl", p.registerUrl)
                .put("scope", p.scope).put("legalBasis", p.legalBasis).put("status", p.status))
        }
        atomicWrite(File(dir(context), INDEX), encrypt(array.toString().toByteArray(Charsets.UTF_8)))
    }

    @Synchronized
    fun files(context: Context): List<QualificationFile> = runCatching {
        val index = File(dir(context), "qualification-files.enc")
        if (!index.exists()) return emptyList()
        val array = JSONArray(String(decrypt(index.readBytes()), Charsets.UTF_8))
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            QualificationFile(o.getString("id"), o.getString("name"), o.getString("mimeType"),
                o.optString("profession"), o.getLong("addedAt"), o.getLong("size"))
        }
    }.getOrDefault(emptyList())

    @Synchronized
    fun addFile(context: Context, uri: Uri, profession: String): QualificationFile {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                total += n
                require(total <= MAX_FILE) { "Plik przekracza limit 25 MB." }
                out.write(buffer, 0, n)
            }
            out.toByteArray()
        } ?: error("Nie można odczytać wybranego pliku.")
        require(bytes.isNotEmpty()) { "Wybrany plik jest pusty." }
        val mime = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.ROOT)
        val isPdf = mime == "application/pdf" || uri.toString().lowercase(Locale.ROOT).endsWith(".pdf")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (!isPdf) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Obsługiwane są zdjęcia oraz dokumenty PDF." }
        } else {
            require(bytes.size >= 5 && String(bytes, 0, 5, Charsets.US_ASCII) == "%PDF-") { "Nieprawidłowy plik PDF." }
        }
        val id = UUID.randomUUID().toString()
        val name = queryName(context, uri)
        atomicWrite(File(dir(context), "file-" + id + ".enc"), encrypt(bytes))
        val record = QualificationFile(id, name, if (isPdf) "application/pdf" else mime.ifBlank { "image/*" },
            profession, System.currentTimeMillis(), bytes.size.toLong())
        saveFiles(context, files(context) + record)
        ActivityHistoryStore.record(context, "Kwalifikacje", "Dodano dokument kwalifikacyjny: " + name)
        return record
    }

    @Synchronized
    private fun saveFiles(context: Context, records: List<QualificationFile>) {
        val array = JSONArray()
        records.forEach { f ->
            array.put(JSONObject().put("id", f.id).put("name", f.name).put("mimeType", f.mimeType)
                .put("profession", f.profession).put("addedAt", f.addedAt).put("size", f.size))
        }
        atomicWrite(File(dir(context), "qualification-files.enc"), encrypt(array.toString().toByteArray(Charsets.UTF_8)))
    }

    fun readFile(context: Context, id: String): ByteArray =
        decrypt(File(dir(context), "file-" + id + ".enc").readBytes())

    @Synchronized
    fun deleteFile(context: Context, record: QualificationFile) {
        File(dir(context), "file-" + record.id + ".enc").delete()
        saveFiles(context, files(context).filterNot { it.id == record.id })
        ActivityHistoryStore.record(context, "Kwalifikacje", "Usunięto dokument kwalifikacyjny: " + record.name)
    }


    @Synchronized
    fun exportArchive(context: Context): String {
        val root = JSONObject().put("version", 1)
        val professionArray = JSONArray()
        professions(context).forEach { p ->
            professionArray.put(JSONObject().put("id", p.id).put("profession", p.profession)
                .put("number", p.number).put("numberType", p.numberType).put("issuer", p.issuer)
                .put("issueDate", p.issueDate).put("validUntil", p.validUntil)
                .put("registerName", p.registerName).put("registerUrl", p.registerUrl)
                .put("scope", p.scope).put("legalBasis", p.legalBasis).put("status", p.status))
        }
        val fileArray = JSONArray()
        files(context).forEach { file ->
            fileArray.put(JSONObject().put("id", file.id).put("name", file.name)
                .put("mimeType", file.mimeType).put("profession", file.profession)
                .put("addedAt", file.addedAt).put("size", file.size)
                .put("bytes", java.util.Base64.getEncoder().encodeToString(readFile(context, file.id))))
        }
        root.put("professions", professionArray).put("files", fileArray)
        return root.toString()
    }

    @Synchronized
    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja archiwum kwalifikacji." }
        val professionArray = root.optJSONArray("professions") ?: JSONArray()
        val professionRecords = (0 until professionArray.length()).map { i ->
            val o = professionArray.getJSONObject(i)
            ProfessionCredential(o.getString("id"), o.getString("profession"), o.optString("number"),
                o.optString("numberType", "other"), o.optString("issuer"), o.optString("issueDate"),
                o.optString("validUntil"), o.optString("registerName"), o.optString("registerUrl"),
                o.optString("scope"), o.optString("legalBasis"), o.optString("status", "Do uzupełnienia"))
        }
        val existingProfessions = professions(context).associateBy { it.id }.toMutableMap()
        professionRecords.forEach { existingProfessions[it.id] = it }
        saveProfessions(context, existingProfessions.values.toList())

        val fileArray = root.optJSONArray("files") ?: JSONArray()
        val existingFiles = files(context).associateBy { it.id }.toMutableMap()
        for (i in 0 until fileArray.length()) {
            val o = fileArray.getJSONObject(i)
            val id = o.getString("id")
            require(id.matches(Regex("[A-Fa-f0-9-]{16,64}"))) { "Nieprawidłowy identyfikator załącznika kwalifikacji." }
            val bytes = java.util.Base64.getDecoder().decode(o.getString("bytes"))
            require(bytes.isNotEmpty() && bytes.size <= MAX_FILE) { "Załącznik kwalifikacji przekracza limit 25 MB." }
            atomicWrite(File(dir(context), "file-$id.enc"), encrypt(bytes))
            existingFiles[id] = QualificationFile(id, o.getString("name").take(180),
                o.getString("mimeType").take(120), o.optString("profession").take(180),
                o.optLong("addedAt", System.currentTimeMillis()), bytes.size.toLong())
        }
        saveFiles(context, existingFiles.values.toList())
    }

    private fun atomicWrite(target: File, bytes: ByteArray) {
        val temp = File(target.parentFile, target.name + ".tmp")
        temp.writeBytes(bytes)
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        var name = "Dokument kwalifikacyjny"
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0) ?: name
        }
        return name.take(180)
    }
}

internal fun exportProfessionalQualificationArchive(context: Context): String = ProfessionalCredentialStore.exportArchive(context)
internal fun importProfessionalQualificationArchive(context: Context, raw: String) = ProfessionalCredentialStore.importArchive(context, raw)

@Composable
internal fun ProfessionalQualificationsScreen(onBack: () -> Unit, readOnly: Boolean = false) {
    val context = LocalContext.current
    var professions by remember { mutableStateOf(ProfessionalCredentialStore.professions(context)) }
    var files by remember { mutableStateOf(ProfessionalCredentialStore.files(context)) }
    var editing by remember { mutableStateOf<ProfessionCredential?>(null) }
    var adding by remember { mutableStateOf(false) }
    var selectedProfession by remember { mutableStateOf("Wszystkie zawody") }
    var status by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) runCatching {
            val imported = uris.map { ProfessionalCredentialStore.addFile(context, it,
                if (selectedProfession == "Wszystkie zawody") "" else selectedProfession) }
            files = ProfessionalCredentialStore.files(context)
            status = "Dodano dokumentów: " + imported.size + ". Pliki są zaszyfrowane lokalnie."
        }.onFailure { status = "Błąd importu: " + (it.message ?: "nieznany błąd") }
    }

    fun persist(records: List<ProfessionCredential>) {
        runCatching { ProfessionalCredentialStore.saveProfessions(context, records) }
            .onSuccess { professions = records; status = "Zapisano dane zawodów lokalnie." }
            .onFailure { status = "Błąd zapisu: " + (it.message ?: "nieznany błąd") }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Więcej", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text(if (readOnly) "Podgląd kwalifikacji zawodowych" else "Zawody i numery prawa wykonywania zawodu", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (readOnly) "Zapisane dane kwalifikacji i dokumentów. Edycja jest dostępna wyłącznie w Więcej → Ustawienia → Dokumenty kwalifikacji zawodowych."
            else "Samodzielnie dodawaj nazwy wykonywanych zawodów oraz numery praw wykonywania zawodu i innych dokumentów.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Ważne rozróżnienie prawne", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Numer PWZ wpisuj wyłącznie wtedy, gdy właściwe przepisy przewidują prawo wykonywania danego zawodu i dokument został rzeczywiście nadany. Dla pozostałych ról zapisuj numer dyplomu, certyfikatu, zaświadczenia, legitymacji lub innego dokumentu — bez nazywania go PWZ. Przykładowo fizjoterapeuta posiada ustawowe prawo wykonywania zawodu; masażysta, naturopata, zielarz i misjonarz nie otrzymują automatycznie numeru PWZ z samego tytułu tej roli.",
                    color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (professions.isEmpty()) Text(if (readOnly) "Brak zapisanych kwalifikacji zawodowych." else "Nie dodano jeszcze żadnego zawodu. Wybierz „Dodaj kolejny zawód”, aby wpisać własną nazwę i dane dokumentu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        professions.forEach { p ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Filled.Badge, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
                        Text(p.profession, Modifier.weight(1f).padding(start = 9.dp),
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        if (!readOnly) TextButton(onClick = { editing = p }) { Text("Edytuj") }
                    }
                    val numberLabel = if (p.numberType == "pwzf") "Numer prawa wykonywania zawodu" else "Numer dokumentu / kwalifikacji"
                    Text(numberLabel + ": " + p.number.ifBlank { "Nie uzupełniono" }, color = MaterialTheme.colorScheme.onSurface)
                    if (p.issuer.isNotBlank()) Text("Organ / wystawca: " + p.issuer, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    if (p.registerName.isNotBlank()) Text("Rejestr: " + p.registerName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("Status: " + p.status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    if (readOnly) {
                        val previewSettings = rememberLegitimacyPreviewSettings("professional").value
                        CredentialCardSides(
                            documentKey = "professional",
                            title = p.profession.uppercase(),
                            subtitle = p.issuer.ifBlank { "KWALIFIKACJA ZAWODOWA" },
                            holderName = p.profession,
                            fields = listOf(
                                Triple("professionalTitle", "Nazwa zawodu / roli", p.profession),
                                Triple("professionalNumber", "Numer dokumentu / kwalifikacji", p.number.ifBlank { "—" }),
                                Triple("professionalNumberType", "Rodzaj numeru dokumentu", if (p.numberType == "pwzf") "PWZ/PWZF" else "Inny dokument"),
                                Triple("professionalIssuer", "Organ / wystawca", p.issuer.ifBlank { "—" }),
                                Triple("professionalIssueDate", "Data wydania", p.issueDate.ifBlank { "—" }),
                                Triple("professionalValidity", "Ważny do / bezterminowo", p.validUntil.ifBlank { "—" }),
                                Triple("professionalSpeciality", "Nazwa rejestru", p.registerName.ifBlank { "—" }),
                                Triple("professionalRegisterUrl", "Adres rejestru / wpisu", p.registerUrl.ifBlank { "—" }),
                                Triple("professionalQualification", "Zakres uprawnień / czynności", p.scope.ifBlank { "—" }),
                                Triple("professionalLegalBasis", "Podstawa prawna / regulaminowa", p.legalBasis.ifBlank { "—" }),
                                Triple("professionalStatus", "Status (np. aktywny, wygasły, do weryfikacji)", p.status.ifBlank { "—" })
                            ),
                            settings = previewSettings
                        )
                    }
                    DocumentPdfExportButton("Karta kwalifikacji — " + p.profession, mapOf(
                        "Zawód / rola" to p.profession,
                        (if (p.numberType == "pwzf") "Numer prawa wykonywania zawodu" else "Numer dokumentu / kwalifikacji") to p.number,
                        "Organ / wystawca" to p.issuer,
                        "Data wydania" to p.issueDate,
                        "Ważny do" to p.validUntil,
                        "Rejestr" to p.registerName,
                        "Adres rejestru" to p.registerUrl,
                        "Zakres uprawnień" to p.scope,
                        "Podstawa prawna / regulaminowa" to p.legalBasis,
                        "Status" to p.status
                    ), documentType = "professional", ownerDefault = p.profession)
                    TextButton(onClick = { selectedProfession = p.profession }) { Text("Pokaż dokumenty tego zawodu") }
                }
            }
        }
        if (!readOnly) OutlinedButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj kolejny zawód") }

        Spacer(Modifier.height(8.dp))
        Text("Uzyskane kwalifikacje", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Dodawaj zdjęcia, skany i pliki PDF dyplomów, certyfikatów, zaświadczeń, legitymacji oraz innych dokumentów potwierdzających kwalifikacje.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (!readOnly) OutlinedButton(onClick = { picker.launch(arrayOf("image/*", "application/pdf")) },
            modifier = Modifier.fillMaxWidth()) { Text("＋  Dodaj zdjęcia lub pliki PDF") }
        Text("Powiązanie dokumentów: " + selectedProfession, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        files.filter { selectedProfession == "Wszystkie zawody" || it.profession == selectedProfession || it.profession.isBlank() }
            .forEach { f ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(if (f.mimeType == "application/pdf") Icons.Filled.Description else Icons.Filled.Image,
                            contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(f.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text((if (f.mimeType == "application/pdf") "PDF" else "Zdjęcie") + " • " +
                                String.format(Locale("pl", "PL"), "%.1f kB", f.size / 1024.0),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            if (f.profession.isNotBlank()) Text("Zawód: " + f.profession, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        if (!readOnly) TextButton(onClick = {
                            runCatching { ProfessionalCredentialStore.deleteFile(context, f) }
                                .onSuccess { files = ProfessionalCredentialStore.files(context); status = "Usunięto dokument." }
                                .onFailure { status = "Błąd usuwania: " + (it.message ?: "nieznany błąd") }
                        }) { Text("Usuń") }
                    }
                }
            }
        if (files.isEmpty()) Text("Nie dodano jeszcze dokumentów kwalifikacyjnych.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        Text("Pliki i dane są przechowywane lokalnie w zaszyfrowanej postaci. Kopia zapasowa Androida nie obejmuje tego archiwum. Sam zapis w aplikacji nie potwierdza autentyczności ani uznania kwalifikacji przez właściwy organ.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }

    val target = editing ?: if (adding) ProfessionCredential(UUID.randomUUID().toString(), "", "", "other",
        "", "", "", "", "", "", "", "Do uzupełnienia") else null
    if (!readOnly && target != null) {
        var name by remember(target.id) { mutableStateOf(target.profession) }
        var number by remember(target.id) { mutableStateOf(target.number) }
        var numberType by remember(target.id) { mutableStateOf(target.numberType) }
        var issuer by remember(target.id) { mutableStateOf(target.issuer) }
        var issueDate by remember(target.id) { mutableStateOf(target.issueDate) }
        var validUntil by remember(target.id) { mutableStateOf(target.validUntil) }
        var register by remember(target.id) { mutableStateOf(target.registerName) }
        var registerUrl by remember(target.id) { mutableStateOf(target.registerUrl) }
        var scope by remember(target.id) { mutableStateOf(target.scope) }
        var legalBasis by remember(target.id) { mutableStateOf(target.legalBasis) }
        var recordStatus by remember(target.id) { mutableStateOf(target.status) }
        AlertDialog(onDismissRequest = { editing = null; adding = false },
            title = { Text("Dane zawodu i uprawnienia") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Nazwa zawodu / roli") }, singleLine = true)
                    OutlinedTextField(number, { number = it }, label = { Text(if (numberType == "pwzf") "Numer prawa wykonywania zawodu (PWZ/PWZF)" else "Numer dokumentu / kwalifikacji") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { numberType = "pwzf" }) { Text(if (numberType == "pwzf") "✓ PWZF" else "PWZF") }
                        TextButton(onClick = { numberType = "other" }) { Text(if (numberType == "other") "✓ Inny dokument" else "Inny dokument") }
                    }
                    OutlinedTextField(issuer, { issuer = it }, label = { Text("Organ / wystawca") }, singleLine = true)
                    OutlinedTextField(issueDate, { issueDate = it }, label = { Text("Data wydania") }, singleLine = true)
                    OutlinedTextField(validUntil, { validUntil = it }, label = { Text("Ważny do / bezterminowo") }, singleLine = true)
                    OutlinedTextField(register, { register = it }, label = { Text("Nazwa rejestru") }, singleLine = true)
                    OutlinedTextField(registerUrl, { registerUrl = it }, label = { Text("Adres rejestru / wpisu") }, singleLine = true)
                    OutlinedTextField(scope, { scope = it }, label = { Text("Zakres uprawnień / czynności") }, minLines = 2)
                    OutlinedTextField(legalBasis, { legalBasis = it }, label = { Text("Podstawa prawna / regulaminowa") }, minLines = 2)
                    OutlinedTextField(recordStatus, { recordStatus = it }, label = { Text("Status (np. aktywny, wygasły, do weryfikacji)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        val updated = if (professions.any { it.id == target.id })
                            professions.map { if (it.id == target.id) target.copy(profession = name, number = number,
                                numberType = numberType, issuer = issuer, issueDate = issueDate, validUntil = validUntil,
                                registerName = register, registerUrl = registerUrl, scope = scope, legalBasis = legalBasis, status = recordStatus) else it }
                        else professions + target.copy(profession = name, number = number, numberType = numberType,
                            issuer = issuer, issueDate = issueDate, validUntil = validUntil, registerName = register,
                            registerUrl = registerUrl, scope = scope, legalBasis = legalBasis, status = recordStatus)
                        persist(updated)
                        editing = null
                        adding = false
                    }
                }) { Text("Zapisz") }
            },
            dismissButton = { TextButton(onClick = { editing = null; adding = false }) { Text("Anuluj") } })
    }
}
