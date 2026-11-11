// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

private data class MedicalHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: String = "",
    val diagnosis: String = "",
    val facility: String = "",
    val physician: String = "",
    val notes: String = "",
    val images: List<String> = emptyList()
)

internal fun exportMedicalHistoryArchive(context: Context): String = MedicalHistoryStore.exportArchive(context)
internal fun importMedicalHistoryArchive(context: Context, raw: String) = MedicalHistoryStore.importArchive(context, raw)

@Composable
internal fun MedicalHistoryRecordsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(MedicalHistoryStore.load(context)) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var facility by remember { mutableStateOf("") }
    var physician by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var pendingImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var status by remember { mutableStateOf("") }

    fun persist(updated: List<MedicalHistoryEntry>) {
        runCatching { MedicalHistoryStore.save(context, updated) }
            .onSuccess { entries = updated; status = "Zapisano lokalnie. Archiwum jest szyfrowane." }
            .onFailure { status = "Błąd zapisu: ${it.message ?: "nieznany błąd"}" }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) runCatching {
            val imported = uris.map { uri ->
                val source = context.contentResolver.openInputStream(uri)?.use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 15 * 1024 * 1024) { "Pojedynczy plik przekracza limit 15 MB." }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: error("Nie można odczytać jednego z wybranych zdjęć.")
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Wybrany plik nie jest prawidłowym obrazem." }
                var sample = 1
                while (bounds.outWidth / sample > 1800 || bounds.outHeight / sample > 1800) sample *= 2
                val bitmap = BitmapFactory.decodeByteArray(source, 0, source.size,
                    BitmapFactory.Options().apply { inSampleSize = sample })
                    ?: error("Nie można przetworzyć wybranego obrazu.")
                val compressed = ByteArrayOutputStream()
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 86, compressed)) { "Nie udało się przygotować zdjęcia." }
                bitmap.recycle()
                android.util.Base64.encodeToString(compressed.toByteArray(), android.util.Base64.NO_WRAP)
            }
            require(pendingImages.size + imported.size <= 40) { "Jeden wpis może zawierać maksymalnie 40 zdjęć." }
            pendingImages = pendingImages + imported
            status = "Dodano ${imported.size} zdjęć do formularza. Zapisz wpis, aby utrwalić archiwum."
        }.onFailure { status = "Nie udało się dodać zdjęć: ${it.message ?: "błąd odczytu"}" }
    }

    val selected = entries.firstOrNull { it.id == selectedId }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = Color(0xFF1769AA), fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Historia chorób", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Prywatne archiwum dokumentacji zdrowotnej — zdjęcia wyników badań, wypisów, rozpoznań, konsultacji, kart informacyjnych i innych dokumentów medycznych.",
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF596675))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Prywatność i status danych", color = Color(0xFF172B45), fontWeight = FontWeight.Bold)
                Text("Zdjęcia i opisy są przechowywane lokalnie w prywatnym obszarze aplikacji i szyfrowane kluczem Android Keystore. Moduł nie łączy się z IKP, placówkami ani rejestrami i nie potwierdza autentyczności dokumentów. To osobiste archiwum, a nie oficjalna dokumentacja medyczna.",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF26384A))
            }
        }
        if (selected == null) {
            if (entries.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6F9))) {
                    Text("Archiwum jest puste. Dodaj pierwszy wpis i dołącz do niego dowolną liczbę zdjęć (maksymalnie 40 na wpis).",
                        Modifier.padding(16.dp), color = Color(0xFF26384A))
                }
            }
            entries.forEach { entry ->
                Card(Modifier.fillMaxWidth().clickable { selectedId = entry.id },
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(entry.title.ifBlank { "Historia choroby — bez tytułu" }, color = Color(0xFF172B45), fontWeight = FontWeight.Bold)
                        Text("Data: ${entry.date.ifBlank { "nie podano" }}", color = Color(0xFF596675))
                        if (entry.diagnosis.isNotBlank()) Text("Rozpoznanie: ${entry.diagnosis}", color = Color(0xFF26384A))
                        Text("Zdjęcia: ${entry.images.size}  •  Otwórz ›", color = Color(0xFF1769AA), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Button(onClick = {
                title = ""; date = ""; diagnosis = ""; facility = ""; physician = ""; notes = ""
                pendingImages = emptyList()
                showForm = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj historię choroby") }
        } else {
            TextButton(onClick = { selectedId = null }) { Text("‹ Lista historii") }
            Text(selected.title.ifBlank { "Historia choroby" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            listOf("Data zdarzenia / dokumentu" to selected.date,
                "Rozpoznanie / problem zdrowotny" to selected.diagnosis,
                "Placówka / laboratorium" to selected.facility,
                "Lekarz / osoba wystawiająca" to selected.physician).forEach { (label, value) ->
                if (value.isNotBlank()) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, color = Color(0xFF596675), style = MaterialTheme.typography.labelSmall)
                    Text(value, color = Color(0xFF172B45))
                }
            }
            if (selected.notes.isNotBlank()) Text("Opis i uwagi: ${selected.notes}", color = Color(0xFF26384A))
            Text("Załączone zdjęcia: ${selected.images.size}", fontWeight = FontWeight.Bold, color = Color(0xFF172B45))
            selected.images.forEachIndexed { index, encoded ->
                val bitmap = remember(encoded) {
                    runCatching {
                        val bytes = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }.getOrNull()
                }
                if (bitmap != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Image(bitmap, contentDescription = "Zdjęcie dokumentu medycznego ${index + 1}",
                                modifier = Modifier.fillMaxWidth().height(210.dp))
                            Text("Zdjęcie ${index + 1}", color = Color(0xFF596675))
                            TextButton(onClick = {
                                persist(entries.map { if (it.id == selected.id) it.copy(images = it.images.filterIndexed { i, _ -> i != index }) else it })
                            }) { Text("Usuń zdjęcie") }
                        }
                    }
                } else Text("Nie można wyświetlić zdjęcia ${index + 1}.", color = Color(0xFFB3261E))
            }
            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Dodaj kolejne zdjęcia")
            }
            OutlinedButton(onClick = {
                persist(entries.filterNot { it.id == selected.id })
                selectedId = null
            }, modifier = Modifier.fillMaxWidth()) { Text("Usuń całą historię") }
        }
        if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) Color(0xFF1769AA) else Color(0xFFB3261E))
        Spacer(Modifier.height(8.dp))
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = { Text("Nowa historia choroby") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tytuł / nazwa zdarzenia") })
                    OutlinedTextField(date, { date = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Data (DD-MM-RRRR)") })
                    OutlinedTextField(diagnosis, { diagnosis = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Rozpoznanie / problem zdrowotny") }, minLines = 2)
                    OutlinedTextField(facility, { facility = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Placówka / laboratorium") })
                    OutlinedTextField(physician, { physician = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Lekarz / osoba wystawiająca") })
                    OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Opis, przebieg, uwagi") }, minLines = 3)
                    OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Wybierz wiele zdjęć")
                    }
                    Text("Dołączono zdjęć: ${pendingImages.size} / 40. Zdjęcia są zmniejszane do maksymalnie 1800 px i zapisywane jako JPEG.",
                        color = Color(0xFF596675), style = MaterialTheme.typography.bodySmall)
                    pendingImages.take(4).forEachIndexed { index, encoded ->
                        val preview = remember(encoded) {
                            runCatching {
                                val bytes = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            }.getOrNull()
                        }
                        if (preview != null) Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(preview, null, Modifier.size(52.dp))
                            Text("Zdjęcie ${index + 1}", Modifier.weight(1f))
                            TextButton(onClick = { pendingImages = pendingImages.filterIndexed { i, _ -> i != index } }) { Text("Usuń") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isBlank() && diagnosis.isBlank() && pendingImages.isEmpty()) {
                        status = "Podaj tytuł, rozpoznanie lub dołącz przynajmniej jedno zdjęcie."
                    } else {
                        val entry = MedicalHistoryEntry(title = title.trim(), date = date.trim(),
                            diagnosis = diagnosis.trim(), facility = facility.trim(), physician = physician.trim(),
                            notes = notes.trim(), images = pendingImages)
                        persist(entries + entry)
                        selectedId = entry.id
                        showForm = false
                    }
                }) { Text("Zapisz historię") }
            },
            dismissButton = { TextButton(onClick = { showForm = false }) { Text("Anuluj") } }
        )
    }
}

private object MedicalHistoryStore {
    private const val FILE_NAME = "medical_history_archive.enc"
    private const val KEY_ALIAS = "obywatel_medical_history_aes"

    fun load(context: Context): List<MedicalHistoryEntry> = runCatching {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val bytes = file.readBytes()
        require(bytes.size > 12) { "Uszkodzony zaszyfrowany plik archiwum." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        val root = JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        val array = root.optJSONArray("entries") ?: JSONArray()
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val images = item.optJSONArray("images") ?: JSONArray()
            MedicalHistoryEntry(id = item.optString("id", UUID.randomUUID().toString()),
                title = item.optString("title"), date = item.optString("date"),
                diagnosis = item.optString("diagnosis"), facility = item.optString("facility"),
                physician = item.optString("physician"), notes = item.optString("notes"),
                images = (0 until images.length()).map { images.getString(it) })
        }
    }.getOrElse { emptyList() }

    fun save(context: Context, entries: List<MedicalHistoryEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val images = JSONArray()
            entry.images.forEach { images.put(it) }
            array.put(JSONObject().put("id", entry.id).put("title", entry.title).put("date", entry.date)
                .put("diagnosis", entry.diagnosis).put("facility", entry.facility)
                .put("physician", entry.physician).put("notes", entry.notes).put("images", images))
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(JSONObject().put("entries", array).toString().toByteArray(Charsets.UTF_8))
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temporary = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        temporary.writeBytes(encrypted)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }
    }


    fun exportArchive(context: Context): String {
        val array = JSONArray()
        load(context).forEach { entry ->
            val images = JSONArray()
            entry.images.forEach { images.put(it) }
            array.put(JSONObject().put("id", entry.id).put("title", entry.title).put("date", entry.date)
                .put("diagnosis", entry.diagnosis).put("facility", entry.facility)
                .put("physician", entry.physician).put("notes", entry.notes).put("images", images))
        }
        return JSONObject().put("version", 1).put("entries", array).toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        require(root.optInt("version") == 1) { "Nieobsługiwana wersja historii medycznej." }
        val array = root.optJSONArray("entries") ?: JSONArray()
        val imported = (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val images = item.optJSONArray("images") ?: JSONArray()
            MedicalHistoryEntry(
                id = item.optString("id", UUID.randomUUID().toString()),
                title = item.optString("title"), date = item.optString("date"),
                diagnosis = item.optString("diagnosis"), facility = item.optString("facility"),
                physician = item.optString("physician"), notes = item.optString("notes"),
                images = (0 until images.length()).map { images.getString(it) }
            )
        }
        val merged = load(context).associateBy { it.id }.toMutableMap()
        imported.forEach { merged[it.id] = it }
        save(context, merged.values.toList())
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
}
