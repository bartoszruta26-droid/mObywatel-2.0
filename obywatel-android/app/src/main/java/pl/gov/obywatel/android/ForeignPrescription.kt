// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

internal const val FOREIGN_PRESCRIPTION_ID = "foreign_prescription"
private const val FPR_RECORD_ID = "foreign-prescription-single"

private val foreignPrescriptionFields = listOf(
    Triple("patientName", "Patient full name", "Full name as shown on identity document"),
    Triple("patientAddress", "Patient address", "Street, house number, postal code, city, country"),
    Triple("medicineInn", "International nonproprietary name (INN)", "Active substance"),
    Triple("medicineBrand", "Brand name (if prescribed)", "Optional trade name"),
    Triple("medicineForm", "Pharmaceutical form", "e.g. tablet, capsule, solution"),
    Triple("medicineStrength", "Strength", "e.g. 500 mg"),
    Triple("medicineQuantity", "Quantity", "Number of units / packages"),
    Triple("medicineDosage", "Dosage and directions", "Dose, route, frequency and duration"),
    Triple("issueDate", "Date of issue", "YYYY-MM-DD"),
    Triple("prescriberName", "Prescriber's full name", ""),
    Triple("prescriberQualifications", "Professional title / qualifications", "e.g. medical doctor"),
    Triple("prescriberSpecialty", "Medical specialty", "Specialty as stated in the professional register"),
    Triple("prescriberPractice", "Practice / institution name", "Name of the clinic or medical institution"),
    Triple("prescriberContact", "Prescriber's email or phone with international prefix", ""),
    Triple("prescriberAddress", "Prescriber / practice address (optional)", "Optional full postal address"),
    Triple("prescriberCountry", "Country of issue", ""),
    Triple("prescriberRegister", "Professional register / licence number", "e.g. CRM and state in Brazil")
)

internal val foreignPrescriptionRequiredFieldKeys = listOf(
    "patientName", "patientAddress", "medicineForm", "medicineStrength", "medicineQuantity",
    "issueDate", "prescriberName", "prescriberQualifications", "prescriberContact"
)

internal fun missingForeignPrescriptionFields(values: Map<String, String>): List<String> = buildList {
    foreignPrescriptionRequiredFieldKeys.forEach { key ->
        val value = values["fpr_$key"].orEmpty()
        if (if (key == "prescriberContact") !isValidForeignPrescriptionContact(value) else value.isBlank()) add(key)
    }
    // § 15 accepts the common (international) name OR the trade name.
    if (values["fpr_medicineInn"].isNullOrBlank() && values["fpr_medicineBrand"].isNullOrBlank()) {
        add("medicineInn")
    }
}

internal fun isValidForeignPrescriptionContact(value: String): Boolean {
    val contact = value.trim()
    if (contact.isBlank()) return false
    if (Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(contact)) return true
    val normalizedPhone = contact.replace(Regex("[\\s().-]"), "")
    return Regex("^\\+[1-9]\\d{6,14}$").matches(normalizedPhone)
}

internal fun isValidForeignPrescriptionIssueDate(value: String): Boolean = runCatching {
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).apply { isLenient = false }
    val parsed = format.parse(value)
    parsed != null && format.format(parsed) == value
}.getOrDefault(false)

@Composable
internal fun ForeignPrescriptionEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val loaded = remember(context) { runCatching { InternationalMedicalCertificateRecords.load(context, FPR_RECORD_ID) }.getOrDefault(emptyMap()) }
    val values = remember { mutableStateMapOf<String, String>().apply { putAll(loaded) } }
    val translations = remember {
        mutableStateMapOf<String, String>().apply {
            runCatching { JSONObject(values["fpr_translations"].orEmpty()) }.getOrNull()?.let { json ->
                val keys = json.keys()
                while (keys.hasNext()) { val key = keys.next(); put(key, json.optString(key)) }
            }
        }
    }
    val doctors = remember(context) { runCatching { MedicalDoctorDirectory.list(context) }.getOrDefault(emptyList()) }
    var selectedDoctorId by remember { mutableStateOf(values["fpr_selected_doctor"].orEmpty()) }
    var doctorMenu by remember { mutableStateOf(false) }
    var sourceLanguage by remember { mutableStateOf(values["fpr_source_language"] ?: "pt") }
    var targetLanguage by remember { mutableStateOf(values["fpr_target_language"] ?: "pl") }
    var sourceMenu by remember { mutableStateOf(false) }
    var targetMenu by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var showLegalInfo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pendingPdf by remember { mutableStateOf<ByteArray?>(null) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val bytes = pendingPdf
        if (uri != null && bytes != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Nie można zapisać pliku PDF.")
        }.onSuccess { message = "Zapisano PDF." }.onFailure { message = it.message ?: "Błąd eksportu PDF." }
        pendingPdf = null
    }
    val languages = MedicalCertificateLanguages.supported
    val selectedDoctor = doctors.firstOrNull { it.id == selectedDoctorId }
    val missing = missingForeignPrescriptionFields(values)
    val validDate = isValidForeignPrescriptionIssueDate(values["fpr_issueDate"].orEmpty())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹ Powrót do dokumentów", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Recepta zagraniczna", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Recepta wystawiana poza UE/EFTA, przeznaczona do okazania w polskiej aptece. Dane oryginalne i tłumaczenie są przechowywane oddzielnie.")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dane lekarza", fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { doctorMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedDoctor?.name ?: "Wybierz lekarza z katalogu")
                }
                DropdownMenu(expanded = doctorMenu, onDismissRequest = { doctorMenu = false }) {
                    doctors.forEach { doctor ->
                        DropdownMenuItem(text = { Text("${doctor.name} — ${doctor.city}") }, onClick = {
                            selectedDoctorId = doctor.id
                            values["fpr_prescriberName"] = doctor.name
                            // Do not infer a regulated professional title from the "Dr" display name.
                            // The issuer must confirm and enter the exact qualification shown on the source.
                            values["fpr_prescriberContact"] = doctor.phone
                            values["fpr_prescriberPractice"] = doctor.practice\n                            values["fpr_prescriberAddress"] = doctor.address
                            values["fpr_prescriberCountry"] = doctor.country
                            values["fpr_prescriberRegister"] = listOf(doctor.register, doctor.license).filter { it.isNotBlank() }.joinToString(" — ")
                            doctorMenu = false
                        })
                    }
                }
                Text("Po wybraniu lekarza dane są kopiowane do formularza i pozostają edytowalne.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Język dokumentu", fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { sourceMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Język oryginału: " + (languages.firstOrNull { it.code == sourceLanguage }?.englishName ?: sourceLanguage))
                }
                DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                    languages.forEach { language -> DropdownMenuItem(text = { Text(language.englishName) }, onClick = {
                        sourceLanguage = language.code; sourceMenu = false
                    }) }
                }
                OutlinedButton(onClick = { targetMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Język tłumaczenia: " + (languages.firstOrNull { it.code == targetLanguage }?.englishName ?: targetLanguage))
                }
                DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) {
                    languages.filter { it.code != sourceLanguage }.forEach { language -> DropdownMenuItem(
                        text = { Text(language.englishName) }, onClick = { targetLanguage = language.code; targetMenu = false }) }
                }
                Text("Automatyczne tłumaczenie działa na urządzeniu. Przed użyciem recepty lekarz sprawdza treść, a tłumaczenie nie zastępuje oryginału.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Dane pacjenta, produktu leczniczego i wystawcy", fontWeight = FontWeight.SemiBold)
        foreignPrescriptionFields.forEach { (key, label, hint) ->
            OutlinedTextField(
                value = values["fpr_$key"].orEmpty(),
                onValueChange = { values["fpr_$key"] = it; translations.remove("$targetLanguage|$key") },
                label = { Text(label) }, placeholder = { if (hint.isNotBlank()) Text(hint) },
                modifier = Modifier.fillMaxWidth(), minLines = if (key == "medicineDosage") 2 else 1
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !busy && sourceLanguage != targetLanguage, onClick = {
                busy = true; message = ""
                scope.launch {
                    var done = 0; var failed = 0
                    runCatching { MlKitMedicalTranslationProvider(context) }.onSuccess { provider ->
                        foreignPrescriptionFields.forEach { (key, label, _) ->
                            val source = values["fpr_$key"].orEmpty()
                            if (source.isNotBlank()) {
                                runCatching { provider.translate(source, sourceLanguage, targetLanguage).trim() }
                                    .onSuccess { translated -> if (isValidMedicalCertificateValueTranslation(source, translated, label)) {
                                        translations["$targetLanguage|$key"] = translated; done++
                                    } else failed++ }.onFailure { failed++ }
                            }
                            runCatching { provider.translate(label, "en", targetLanguage).trim() }
                                .onSuccess { translations["$targetLanguage|label:$key"] = it }
                        }
                    }.onFailure { failed++ }
                    busy = false
                    message = "Przetłumaczono pól: $done; błędy lub brak danych: $failed. Sprawdź każde tłumaczenie."
                }
            }) { Text(if (busy) "Tłumaczenie…" else "Przetłumacz automatycznie") }
            OutlinedButton(onClick = { showLegalInfo = true }) { Text("Wymogi prawne") }
        }
        if (translations.keys.any { it.startsWith("$targetLanguage|") }) {
            Text("Tłumaczenie — edytowalne", fontWeight = FontWeight.SemiBold)
            foreignPrescriptionFields.forEach { (key, label, _) ->
                val valueKey = "$targetLanguage|$key"
                if (translations.containsKey(valueKey)) {
                    OutlinedTextField(value = translations[valueKey].orEmpty(),
                        onValueChange = { translations[valueKey] = it }, label = {
                            Text(translations["$targetLanguage|label:$key"] ?: label)
                        }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        if (missing.isNotEmpty()) Text("Brak wymaganych pól: " + missing.joinToString { key ->
            if (key == "medicineInn") "medicineInn lub medicineBrand" else key
        }, color = MaterialTheme.colorScheme.error)
        if (!validDate) Text("Data wystawienia musi mieć prawidłowy format RRRR-MM-DD.", color = MaterialTheme.colorScheme.error)
        if (!isValidForeignPrescriptionContact(values["fpr_prescriberContact"].orEmpty())) {
            Text("Kontakt wystawcy musi być adresem e-mail lub numerem telefonu z prefiksem międzynarodowym.", color = MaterialTheme.colorScheme.error)
        }
        Text("Recepta zagraniczna spoza UE/EFTA jest realizowana w Polsce za pełną odpłatnością. Wydanie leku zależy również od polskich ograniczeń dotyczących danego produktu i oceny farmaceuty.", style = MaterialTheme.typography.bodySmall)
        Button(enabled = missing.isEmpty() && validDate && !busy, onClick = {
            val data = values.toMutableMap().apply {
                put("fpr_source_language", sourceLanguage)
                put("fpr_target_language", targetLanguage)
                put("fpr_selected_doctor", selectedDoctorId)
                val json = JSONObject(); translations.forEach { (k, v) -> json.put(k, v) }
                put("fpr_translations", json.toString())
            }
            runCatching { InternationalMedicalCertificateRecords.save(context, FPR_RECORD_ID, data) }
                .onSuccess { message = "Zapisano receptę." }
                .onFailure { message = it.message ?: "Nie udało się zapisać recepty." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz receptę") }
        Button(enabled = missing.isEmpty() && validDate && !busy, onClick = {
            val pdfFields = foreignPrescriptionFields.map { (key, label, _) ->
                MedicalCertificatePdfField(label, values["fpr_$key"].orEmpty(),
                    translations["$targetLanguage|label:$key"] ?: label,
                    translations["$targetLanguage|$key"].orEmpty())
            }
            pendingPdf = createBilingualMedicalCertificatePdf(
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                fields = pdfFields,
                doctorName = values["fpr_prescriberName"].orEmpty(),
                doctorPractice = values["fpr_prescriberAddress"].orEmpty(),
                doctorAddress = values["fpr_prescriberCountry"].orEmpty(),
                doctorRegister = values["fpr_prescriberRegister"].orEmpty(),
                doctorContact = values["fpr_prescriberContact"].orEmpty(),
                documentTitle = "FOREIGN PRESCRIPTION / RECEPTA ZAGRANICZNA",
                issuerHeading = "PRESCRIBER / WYSTAWCA RECEPTY",
                signatureCaption = "Prescriber's handwritten signature (sign after printing)",
                stampCaption = "Prescriber identification / stamp details"
            )
            pdfLauncher.launch("recepta-zagraniczna.pdf")
        }, modifier = Modifier.fillMaxWidth()) { Text("Eksportuj PDF dwujęzyczny") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
    }
    if (showLegalInfo) AlertDialog(onDismissRequest = { showLegalInfo = false },
        title = { Text("Wymogi § 15 rozporządzenia w sprawie recept") },
        text = { Text("Recepta z państwa spoza UE/EFTA może być zrealizowana, jeżeli zawiera: imię i nazwisko pacjenta, adres pacjenta, nazwę powszechną (międzynarodową) albo handlową produktu, postać, moc, ilość, datę wystawienia oraz dane wystawcy: imię i nazwisko, kwalifikacje zawodowe i dane kontaktowe (e-mail lub telefon z prefiksem międzynarodowym). Wymagane jest nadrukowanie lub pieczęć danych wystawcy oraz jego własnoręczny podpis na papierowym dokumencie. Realizacja za pełną odpłatnością. Aplikacja przygotowuje formularz i PDF do podpisania; nie wystawia recepty samodzielnie ani nie składa podpisu za lekarza.") },
        confirmButton = { TextButton(onClick = { showLegalInfo = false }) { Text("Zamknij") } })
}
