// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

private data class FamilyVaccineEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val vaccine: String = "",
    val disease: String = "",
    val dose: String = "",
    val batch: String = "",
    val manufacturer: String = "",
    val provider: String = "",
    val facility: String = "",
    val nextDate: String = "",
    val notes: String = ""
)

private data class FamilyChildRecord(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val pesel: String = "",
    val sex: String = "",
    val birthPlace: String = "",
    val bloodGroup: String = "",
    val allergies: String = "",
    val guardian: String = "",
    val notes: String = "",
    val photoBase64: String = "",
    val vaccines: List<FamilyVaccineEntry> = emptyList()
)

@Composable
internal fun FamilyHealthRecordsScreen(initialSection: String, onBack: () -> Unit, readOnly: Boolean = false) {
    val context = LocalContext.current
    var children by remember { mutableStateOf(FamilyHealthStore.load(context)) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var section by remember { mutableStateOf(initialSection) }
    var status by remember { mutableStateOf("") }
    var showVaccineForm by remember { mutableStateOf(false) }
    var vaccineDate by remember { mutableStateOf("") }
    var vaccineName by remember { mutableStateOf("") }
    var vaccineDisease by remember { mutableStateOf("") }
    var vaccineDose by remember { mutableStateOf("") }
    var vaccineBatch by remember { mutableStateOf("") }
    var vaccineManufacturer by remember { mutableStateOf("") }
    var vaccineProvider by remember { mutableStateOf("") }
    var vaccineFacility by remember { mutableStateOf("") }
    var vaccineNextDate by remember { mutableStateOf("") }
    var vaccineNotes by remember { mutableStateOf("") }

    fun persist(updated: List<FamilyChildRecord>) {
        runCatching { FamilyHealthStore.save(context, updated) }
            .onSuccess { children = updated; status = "Zapisano lokalnie i zaszyfrowano dane." }
            .onFailure { status = "Błąd zapisu: ${it.message ?: "nieznany błąd"}" }
    }
    fun updateChild(id: String, transform: (FamilyChildRecord) -> FamilyChildRecord) {
        persist(children.map { if (it.id == id) transform(it) else it })
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = selectedId
        if (uri != null && id != null) runCatching {
            val source = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Nie można odczytać wybranego zdjęcia.")
            require(source.size <= 20 * 1024 * 1024) { "Zdjęcie przekracza limit 20 MB." }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Wybrany plik nie jest prawidłowym obrazem." }
            var sample = 1
            while (bounds.outWidth / sample > 1200 || bounds.outHeight / sample > 1200) sample *= 2
            val bitmap = BitmapFactory.decodeByteArray(source, 0, source.size,
                BitmapFactory.Options().apply { inSampleSize = sample })
                ?: error("Nie można przetworzyć zdjęcia.")
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
            bitmap.recycle()
            val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            updateChild(id) { it.copy(photoBase64 = encoded) }
        }.onFailure { status = "Nie udało się dodać zdjęcia: ${it.message}" }
    }

    val selected = children.firstOrNull { it.id == selectedId }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = Color(0xFF1769AA), fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text(if (section == "children") "Dane dzieci" else "eKarta Szczepień",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Prywatna dokumentacja rodzinna. Dane wpisuje opiekun; aplikacja nie pobiera ich z rejestrów medycznych i nie potwierdza autentyczności wpisów.",
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF596675))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { section = "children"; selectedId = null }) { Text("Dane dzieci") }
            OutlinedButton(onClick = { section = "vaccines"; selectedId = null }) { Text("Szczepienia") }
        }
        if (selected == null) {
            if (children.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6F9))) {
                    Text(if (readOnly) "Brak zapisanych danych. Uzupełnij je w Więcej → Ustawienia."
                        else "Nie dodano jeszcze profilu dziecka. Wybierz „Dodaj dziecko”, aby utworzyć pierwszą kartę.",
                        Modifier.padding(16.dp), color = Color(0xFF26384A))
                }
            }
            children.forEach { child ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedId = child.id },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (child.photoBase64.isNotBlank()) {
                            val photo = remember(child.photoBase64) {
                                runCatching { BitmapFactory.decodeByteArray(Base64.decode(child.photoBase64, Base64.DEFAULT), 0,
                                    Base64.decode(child.photoBase64, Base64.DEFAULT).size)?.asImageBitmap() }.getOrNull()
                            }
                            if (photo != null) Image(photo, null, Modifier.size(58.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(listOf(child.firstName, child.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Nowe dziecko — uzupełnij dane" },
                                fontWeight = FontWeight.Bold, color = Color(0xFF172B45))
                            Text("Data urodzenia: ${child.birthDate.ifBlank { "nie podano" }}", color = Color(0xFF596675))
                            Text("Wpisy szczepień: ${child.vaccines.size}", color = Color(0xFF1769AA))
                        }
                        Text("Otwórz ›", color = Color(0xFF1769AA), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (!readOnly) Button(onClick = {
                val created = FamilyChildRecord()
                persist(children + created)
                selectedId = created.id
                section = "children"
            }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj dziecko") }
        } else {
            TextButton(onClick = { selectedId = null }) { Text("‹ Lista dzieci") }
            if (section == "children") {
                DocumentIdentityTemplate(
                    "Karta dziecka",
                    mapOf(
                        "Imię i nazwisko" to listOf(selected.firstName, selected.lastName).filter { it.isNotBlank() }.joinToString(" "),
                        "Data urodzenia" to selected.birthDate,
                        "PESEL" to selected.pesel,
                        "Rodzic / opiekun" to selected.guardian,
                        "Liczba wpisów szczepień" to selected.vaccines.size.toString()
                    ),
                    owner = selected.guardian,
                    photoOverride = remember(selected.photoBase64) {
                        runCatching {
                            val bytes = Base64.decode(selected.photoBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }.getOrNull()
                    }
                )
                Text("Zdjęcie dziecka", fontWeight = FontWeight.Bold)
                if (selected.photoBase64.isNotBlank()) {
                    val bytes = remember(selected.photoBase64) { Base64.decode(selected.photoBase64, Base64.DEFAULT) }
                    val bitmap = remember(selected.photoBase64) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
                    if (bitmap != null) Image(bitmap, null, Modifier.fillMaxWidth().height(190.dp))
                }
                if (!readOnly) OutlinedButton(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selected.photoBase64.isBlank()) "Dodaj zdjęcie" else "Zmień zdjęcie")
                }
                ChildDataField("Imię / imiona", selected.firstName, readOnly) { v -> updateChild(selected.id) { it.copy(firstName = v) } }
                ChildDataField("Nazwisko", selected.lastName, readOnly) { v -> updateChild(selected.id) { it.copy(lastName = v) } }
                ChildDataField("Data urodzenia (DD-MM-RRRR)", selected.birthDate, readOnly) { v -> updateChild(selected.id) { it.copy(birthDate = v) } }
                ChildDataField("PESEL (opcjonalnie)", selected.pesel, readOnly) { v -> updateChild(selected.id) { it.copy(pesel = v) } }
                ChildDataField("Płeć — wpis opiekuna", selected.sex, readOnly) { v -> updateChild(selected.id) { it.copy(sex = v) } }
                ChildDataField("Miejsce urodzenia", selected.birthPlace, readOnly) { v -> updateChild(selected.id) { it.copy(birthPlace = v) } }
                ChildDataField("Grupa krwi (jeśli znana)", selected.bloodGroup, readOnly) { v -> updateChild(selected.id) { it.copy(bloodGroup = v) } }
                ChildDataField("Alergie / reakcje niepożądane", selected.allergies, readOnly, singleLine = false) { v -> updateChild(selected.id) { it.copy(allergies = v) } }
                ChildDataField("Rodzic / opiekun", selected.guardian, readOnly) { v -> updateChild(selected.id) { it.copy(guardian = v) } }
                ChildDataField("Uwagi", selected.notes, readOnly, singleLine = false) { v -> updateChild(selected.id) { it.copy(notes = v) } }
                Button(onClick = { section = "vaccines" }, modifier = Modifier.fillMaxWidth()) { Text("Otwórz eKartę Szczepień") }
                if (!readOnly) TextButton(onClick = {
                    persist(children.filterNot { it.id == selected.id })
                    selectedId = null
                }) { Text("Usuń profil dziecka") }
                DocumentPdfExportButton("Karta dziecka", mapOf(
                    "Imię i nazwisko" to listOf(selected.firstName, selected.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    "Data urodzenia" to selected.birthDate, "PESEL" to selected.pesel, "Płeć" to selected.sex,
                    "Miejsce urodzenia" to selected.birthPlace, "Grupa krwi" to selected.bloodGroup,
                    "Alergie" to selected.allergies, "Rodzic / opiekun" to selected.guardian, "Uwagi" to selected.notes
                ))
            } else {
                DocumentIdentityTemplate(
                    "Książeczka szczepień",
                    mapOf(
                        "Imię i nazwisko" to listOf(selected.firstName, selected.lastName).filter { it.isNotBlank() }.joinToString(" "),
                        "Data urodzenia" to selected.birthDate,
                        "Rodzic / opiekun" to selected.guardian,
                        "Liczba dawek" to selected.vaccines.size.toString(),
                        "Ostatnie szczepienie" to selected.vaccines.lastOrNull()?.date.orEmpty()
                    ),
                    owner = selected.guardian
                )
                Text(listOf(selected.firstName, selected.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Dziecko — bez nazwy" },
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Historia szczepień — wpisy ręczne", color = Color(0xFF596675))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
                    Text("Układ karty obejmuje dane szczepionki, chorobę docelową, dawkę, datę, serię, producenta, punkt wykonania, osobę dokonującą wpisu i termin kolejnej dawki. Wpis nie zastępuje oficjalnej dokumentacji medycznej.",
                        Modifier.padding(14.dp), color = Color(0xFF26384A), style = MaterialTheme.typography.bodySmall)
                }
                selected.vaccines.forEach { v ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(v.vaccine.ifBlank { "Szczepienie — nazwa nieuzupełniona" }, fontWeight = FontWeight.Bold)
                            Text("Data: ${v.date}   •   Dawka: ${v.dose}", color = Color(0xFF26384A))
                            if (v.disease.isNotBlank()) Text("Choroba / wskazanie: ${v.disease}")
                            if (v.batch.isNotBlank()) Text("Numer serii: ${v.batch}")
                            if (v.manufacturer.isNotBlank()) Text("Producent: ${v.manufacturer}")
                            if (v.facility.isNotBlank()) Text("Placówka: ${v.facility}")
                            if (v.provider.isNotBlank()) Text("Osoba dokonująca wpisu: ${v.provider}")
                            if (v.nextDate.isNotBlank()) Text("Kolejna dawka / termin: ${v.nextDate}")
                            if (v.notes.isNotBlank()) Text("Uwagi: ${v.notes}")
                            if (!readOnly) TextButton(onClick = {
                                updateChild(selected.id) { child -> child.copy(vaccines = child.vaccines.filterNot { it.id == v.id }) }
                            }) { Text("Usuń wpis") }
                        }
                    }
                }
                if (!readOnly) OutlinedButton(onClick = { showVaccineForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Dodaj wpis szczepienia")
                }
                Text("Liczba wpisów: ${selected.vaccines.size}", color = Color(0xFF596675))
                DocumentPdfExportButton("Książeczka szczepień", buildMap {
                    put("Imię i nazwisko", listOf(selected.firstName, selected.lastName).filter { it.isNotBlank() }.joinToString(" "))
                    put("Data urodzenia", selected.birthDate)
                    put("Rodzic / opiekun", selected.guardian)
                    selected.vaccines.forEachIndexed { index, v ->
                        put("Szczepienie ${index + 1}", listOf(v.vaccine, v.date, v.dose, v.disease, v.batch, v.manufacturer, v.facility, v.provider, v.nextDate, v.notes).filter { it.isNotBlank() }.joinToString(" · "))
                    }
                })
            }
        }
        if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) Color(0xFF1769AA) else Color(0xFFB3261E))
        Spacer(Modifier.height(8.dp))
    }

    if (!readOnly && showVaccineForm && selected != null) {
        AlertDialog(
            onDismissRequest = { showVaccineForm = false },
            title = { Text("Nowy wpis szczepienia") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChildField("Data podania (DD-MM-RRRR)", vaccineDate) { vaccineDate = it }
                    ChildField("Nazwa szczepionki / preparatu", vaccineName) { vaccineName = it }
                    ChildField("Choroba / wskazanie", vaccineDisease) { vaccineDisease = it }
                    ChildField("Dawka (np. 1/2, przypominająca)", vaccineDose) { vaccineDose = it }
                    ChildField("Numer serii / LOT", vaccineBatch) { vaccineBatch = it }
                    ChildField("Producent", vaccineManufacturer) { vaccineManufacturer = it }
                    ChildField("Osoba dokonująca wpisu", vaccineProvider) { vaccineProvider = it }
                    ChildField("Placówka / miejsce podania", vaccineFacility) { vaccineFacility = it }
                    ChildField("Termin kolejnej dawki", vaccineNextDate) { vaccineNextDate = it }
                    ChildField("Uwagi / reakcje po szczepieniu", vaccineNotes, singleLine = false) { vaccineNotes = it }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (vaccineName.isBlank() || vaccineDate.isBlank()) {
                        status = "Uzupełnij przynajmniej nazwę szczepionki i datę podania."
                    } else {
                        val entry = FamilyVaccineEntry(date = vaccineDate.trim(), vaccine = vaccineName.trim(),
                            disease = vaccineDisease.trim(), dose = vaccineDose.trim(), batch = vaccineBatch.trim(),
                            manufacturer = vaccineManufacturer.trim(), provider = vaccineProvider.trim(),
                            facility = vaccineFacility.trim(), nextDate = vaccineNextDate.trim(), notes = vaccineNotes.trim())
                        updateChild(selected.id) { it.copy(vaccines = it.vaccines + entry) }
                        showVaccineForm = false
                        vaccineDate = ""; vaccineName = ""; vaccineDisease = ""; vaccineDose = ""
                        vaccineBatch = ""; vaccineManufacturer = ""; vaccineProvider = ""
                        vaccineFacility = ""; vaccineNextDate = ""; vaccineNotes = ""
                    }
                }) { Text("Zapisz wpis") }
            },
            dismissButton = { TextButton(onClick = { showVaccineForm = false }) { Text("Anuluj") } }
        )
    }
}

@Composable
private fun ChildDataField(label: String, value: String, readOnly: Boolean, singleLine: Boolean = true, onChange: (String) -> Unit) {
    if (readOnly) {
        Column(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF667788))
            Text(value.ifBlank { "Nie podano" }, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF26384A))
        }
    } else {
        ChildField(label, value, singleLine, onChange)
    }
}

@Composable
private fun ChildField(label: String, value: String, singleLine: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, singleLine = singleLine, minLines = if (singleLine) 1 else 3)
}

private object FamilyHealthStore {
    private const val FILE_NAME = "family_health_records.enc"
    private const val KEY_ALIAS = "obywatel_family_health_aes"

    fun load(context: Context): List<FamilyChildRecord> = runCatching {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val bytes = file.readBytes()
        require(bytes.size > 12) { "Uszkodzony zaszyfrowany plik danych rodzinnych." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        val root = JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        val array = root.optJSONArray("children") ?: JSONArray()
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val vaccines = o.optJSONArray("vaccines") ?: JSONArray()
            FamilyChildRecord(
                id = o.optString("id", UUID.randomUUID().toString()),
                firstName = o.optString("firstName"), lastName = o.optString("lastName"),
                birthDate = o.optString("birthDate"), pesel = o.optString("pesel"),
                sex = o.optString("sex"), birthPlace = o.optString("birthPlace"),
                bloodGroup = o.optString("bloodGroup"), allergies = o.optString("allergies"),
                guardian = o.optString("guardian"), notes = o.optString("notes"),
                photoBase64 = o.optString("photoBase64"),
                vaccines = (0 until vaccines.length()).map { j ->
                    val v = vaccines.getJSONObject(j)
                    FamilyVaccineEntry(id = v.optString("id", UUID.randomUUID().toString()),
                        date = v.optString("date"), vaccine = v.optString("vaccine"),
                        disease = v.optString("disease"), dose = v.optString("dose"),
                        batch = v.optString("batch"), manufacturer = v.optString("manufacturer"),
                        provider = v.optString("provider"), facility = v.optString("facility"),
                        nextDate = v.optString("nextDate"), notes = v.optString("notes"))
                }
            )
        }
    }.getOrElse { emptyList() }

    fun save(context: Context, children: List<FamilyChildRecord>) {
        val array = JSONArray()
        children.forEach { child ->
            val vaccines = JSONArray()
            child.vaccines.forEach { v ->
                vaccines.put(JSONObject().put("id", v.id).put("date", v.date).put("vaccine", v.vaccine)
                    .put("disease", v.disease).put("dose", v.dose).put("batch", v.batch)
                    .put("manufacturer", v.manufacturer).put("provider", v.provider)
                    .put("facility", v.facility).put("nextDate", v.nextDate).put("notes", v.notes))
            }
            array.put(JSONObject().put("id", child.id).put("firstName", child.firstName)
                .put("lastName", child.lastName).put("birthDate", child.birthDate).put("pesel", child.pesel)
                .put("sex", child.sex).put("birthPlace", child.birthPlace).put("bloodGroup", child.bloodGroup)
                .put("allergies", child.allergies).put("guardian", child.guardian).put("notes", child.notes)
                .put("photoBase64", child.photoBase64).put("vaccines", vaccines))
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(JSONObject().put("children", array).toString().toByteArray(Charsets.UTF_8))
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temporary = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        temporary.writeBytes(encrypted)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }
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
