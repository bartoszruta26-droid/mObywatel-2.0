// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

private const val IMC_SOURCE_LANGUAGE_KEY = "imc_source_language"
private const val IMC_OTHER_LANGUAGE_KEY = "imc_other_source_language"
private const val IMC_TARGET_LANGUAGES_KEY = "imc_target_languages"
private const val IMC_IMAGING_STUDIES_KEY = "imc_imaging_studies"

internal fun medicalImagingAttachmentReferencesMatch(
    expectedIds: List<String>,
    storedIds: List<String>
): Boolean =
    expectedIds.size == expectedIds.toSet().size &&
        storedIds.size == storedIds.toSet().size &&
        expectedIds.toSet() == storedIds.toSet()

@Composable
internal fun InternationalMedicalCertificateEditorScreen(recordId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val initialLoad = remember(context) {
        runCatching { InternationalMedicalCertificateRecords.load(context, recordId) }
    }
    val values = remember(context) {
        mutableStateMapOf<String, String>().apply { putAll(initialLoad.getOrDefault(emptyMap())) }
    }
    val imagingLoad = remember {
        runCatching { loadMedicalImagingStudies(values[IMC_IMAGING_STUDIES_KEY]) }
    }
    val loadError = initialLoad.exceptionOrNull()?.message
        ?: imagingLoad.exceptionOrNull()?.message.orEmpty()
    val definitions = remember {
        documentEditorFieldDefinitions(INTERNATIONAL_MEDICAL_CERTIFICATE_ID)
    }
    val doctorDirectory = remember(context) { runCatching { MedicalDoctorDirectory.list(context) }.getOrDefault(emptyList()) }
    var doctorMenuExpanded by remember { mutableStateOf(false) }
    var selectedDoctorId by remember { mutableStateOf(values["imc_doctor_id"].orEmpty()) }
    val selectedDoctor = doctorDirectory.firstOrNull { it.id == selectedDoctorId }
    var sourceLanguage by remember {
        mutableStateOf(values[IMC_SOURCE_LANGUAGE_KEY] ?: "en")
    }
    var otherLanguage by remember {
        mutableStateOf(values[IMC_OTHER_LANGUAGE_KEY].orEmpty())
    }
    val selectedTargets = remember {
        mutableStateMapOf<String, Boolean>().apply {
            values[IMC_TARGET_LANGUAGES_KEY].orEmpty().split(',')
                .filter { it.isNotBlank() }.forEach { put(it, true) }
        }
    }
    var saveMessage by remember { mutableStateOf("") }
    var pendingPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val bytes = pendingPdfBytes
        if (uri != null && bytes != null) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Could not open PDF destination.") }
                .onSuccess { saveMessage = "Bilingual PDF exported successfully." }
                .onFailure { saveMessage = "PDF export failed: " + (it.message ?: "unknown error") }
        }
        pendingPdfBytes = null
    }
    var sourceLanguageExpanded by remember { mutableStateOf(false) }
    var targetLanguagesExpanded by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    val translationValues = remember { mutableStateMapOf<String, String>().apply {
        runCatching { JSONObject(values["imc_translations"].orEmpty()) }.getOrNull()?.let { root ->
            val targets = root.keys()
            while (targets.hasNext()) { val target = targets.next(); val fields = root.optJSONObject(target) ?: continue
                val keys = fields.keys(); while (keys.hasNext()) { val key = keys.next(); put("$target|$key", fields.optString(key)) }
            }
        }
    } }
    val translationScope = rememberCoroutineScope()
    val imagingStudies = remember {
        mutableStateListOf<MedicalImagingStudy>().apply {
            addAll(imagingLoad.getOrDefault(emptyList()))
        }
    }
    var imagingModality by remember { mutableStateOf(MedicalImagingModality.X_RAY) }
    var imagingBodyRegion by remember { mutableStateOf("") }
    var imagingDate by remember { mutableStateOf("") }
    var imagingFacility by remember { mutableStateOf("") }
    var imagingIndication by remember { mutableStateOf("") }
    var imagingFindings by remember { mutableStateOf("") }
    var imagingImpression by remember { mutableStateOf("") }
    var imagingTechnique by remember { mutableStateOf("") }
    var imagingContrast by remember { mutableStateOf("") }
    var imagingRadiologist by remember { mutableStateOf("") }
    var imagingReportNumber by remember { mutableStateOf("") }
    val fieldKey: (String) -> String = { key ->
        "doc_${INTERNATIONAL_MEDICAL_CERTIFICATE_ID}_${key}"
    }
    val languages = MedicalCertificateLanguages.supported
    val validationErrors = InternationalMedicalCertificate(
        sourceLanguage = sourceLanguage,
        otherSourceLanguage = otherLanguage,
        targetLanguages = selectedTargets.filterValues { it }.keys,
        imagingStudies = imagingStudies.toList()
    ).validate()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "‹  Back to documents",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            text = "International Medical Certificate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Complete the original certificate below. Original values are kept separately from any future translations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Attending doctor (informational selection)", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { doctorMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedDoctor?.name ?: "Select doctor from directory")
                    }
                    DropdownMenu(expanded = doctorMenuExpanded, onDismissRequest = { doctorMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("No doctor selected") }, onClick = {
                            selectedDoctorId = ""; doctorMenuExpanded = false
                        })
                        doctorDirectory.forEach { doctor ->
                            DropdownMenuItem(text = { Text(doctor.name + " — " + doctor.city) }, onClick = {
                                selectedDoctorId = doctor.id; doctorMenuExpanded = false
                            })
                        }
                    }
                }
                Text("Selection does not authenticate the doctor or sign the certificate. Verify the current licence and authority separately.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Original Language", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { sourceLanguageExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(languages.firstOrNull { it.code == sourceLanguage }?.englishName
                            ?: if (sourceLanguage == "other") "Other" else "Select language")
                    }
                    DropdownMenu(expanded = sourceLanguageExpanded, onDismissRequest = { sourceLanguageExpanded = false }) {
                        languages.forEach { language ->
                            DropdownMenuItem(text = { Text(language.englishName) }, onClick = {
                                sourceLanguage = language.code
                                sourceLanguageExpanded = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Other") }, onClick = {
                            sourceLanguage = "other"
                            sourceLanguageExpanded = false
                        })
                    }
                }
                if (sourceLanguage == "other") {
                    OutlinedTextField(value = otherLanguage, onValueChange = { otherLanguage = it.take(35) },
                        label = { Text("Other source language") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Target Languages", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { targetLanguagesExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        val selectedNames = languages.filter { selectedTargets[it.code] == true }.joinToString(", ") { it.englishName }
                        Text(selectedNames.ifBlank { "Select target languages" }, maxLines = 2)
                    }
                    DropdownMenu(expanded = targetLanguagesExpanded, onDismissRequest = { targetLanguagesExpanded = false }) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedTargets[language.code] == true,
                                        onCheckedChange = { selectedTargets[language.code] = it })
                                    Text(language.englishName)
                                } },
                                onClick = { selectedTargets[language.code] = selectedTargets[language.code] != true }
                            )
                        }
                    }
                }
                Text("You can select multiple target languages.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        definitions.filterNot { it.key == "sourceLanguage" || it.key == "targetLanguages" }
            .forEach { definition ->
                val key = fieldKey(definition.key)
                OutlinedTextField(
                    value = values[key].orEmpty(),
                    onValueChange = { values[key] = it },
                    label = { Text(definition.label) },
                    placeholder = {
                        if (definition.placeholder.isNotBlank()) Text(definition.placeholder)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (
                        definition.key in setOf(
                            "purpose", "patientAddress", "patientConsent", "facilityAddress",
                            "chiefComplaint", "medicalHistory", "occupationalHistory", "allergies",
                            "currentMedication", "diagnosis", "physicalExamination", "systemsReview",
                            "functionalLimitations", "workCapacity", "hospitalizations",
                            "treatmentAndRehabilitation", "prognosis", "recommendations",
                            "clinicianDeclaration"
                        )
                    ) 3 else 1
                )
            }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Medical Imaging Studies", fontWeight = FontWeight.SemiBold)
                Text("Add each imaging examination separately.", style = MaterialTheme.typography.bodySmall)
                MedicalImagingModality.values().forEach { modality ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = imagingModality == modality, onClick = { imagingModality = modality })
                        Text(modality.label, modifier = Modifier.clickable { imagingModality = modality })
                    }
                }
                OutlinedTextField(value = imagingBodyRegion, onValueChange = { imagingBodyRegion = it }, label = { Text("Body Region") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imagingDate, onValueChange = { imagingDate = it }, label = { Text("Examination Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), isError = imagingDate.isNotBlank() && !isValidMedicalStudyDate(imagingDate))
                if (imagingDate.isNotBlank() && !isValidMedicalStudyDate(imagingDate)) {
                    Text("Enter a real date in YYYY-MM-DD format.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(value = imagingFacility, onValueChange = { imagingFacility = it }, label = { Text("Imaging Facility") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imagingIndication, onValueChange = { imagingIndication = it }, label = { Text("Clinical Indication") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = imagingFindings, onValueChange = { imagingFindings = it }, label = { Text("Findings") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = imagingImpression, onValueChange = { imagingImpression = it }, label = { Text("Impression") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = imagingTechnique, onValueChange = { imagingTechnique = it }, label = { Text("Technique") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = imagingContrast, onValueChange = { imagingContrast = it }, label = { Text("Contrast") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imagingRadiologist, onValueChange = { imagingRadiologist = it }, label = { Text("Radiologist Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imagingReportNumber, onValueChange = { imagingReportNumber = it }, label = { Text("Report Number") }, modifier = Modifier.fillMaxWidth())
                Button(enabled = imagingDate.isBlank() || isValidMedicalStudyDate(imagingDate), onClick = {
                    imagingStudies.add(MedicalImagingStudy(
                        id = UUID.randomUUID().toString(),
                        modality = imagingModality,
                        bodyRegion = imagingBodyRegion,
                        studyDate = imagingDate,
                        facility = imagingFacility,
                        indication = imagingIndication,
                        findings = imagingFindings,
                        impression = imagingImpression,
                        technique = imagingTechnique,
                        contrast = imagingContrast,
                        radiologistName = imagingRadiologist,
                        reportNumber = imagingReportNumber
                    ))
                    imagingBodyRegion = ""
                    imagingDate = ""
                    imagingFacility = ""
                    imagingIndication = ""
                    imagingFindings = ""
                    imagingImpression = ""
                    imagingTechnique = ""
                    imagingContrast = ""
                    imagingRadiologist = ""
                    imagingReportNumber = ""
                }, modifier = Modifier.fillMaxWidth()) { Text("Add imaging study") }
                imagingStudies.forEachIndexed { index, study ->
                    Card {
                        Column(Modifier.fillMaxWidth().padding(10.dp)) {
                            Text(study.modality.label + " — " + study.bodyRegion, fontWeight = FontWeight.SemiBold)
                            Text("Date: " + study.studyDate + " · Facility: " + study.facility)
                            if (study.findings.isNotBlank()) Text("Findings: " + study.findings)
                            if (study.impression.isNotBlank()) Text("Impression: " + study.impression)
                            if (study.technique.isNotBlank()) Text("Technique: " + study.technique)
                            if (study.contrast.isNotBlank()) Text("Contrast: " + study.contrast)
                            if (study.radiologistName.isNotBlank()) Text("Radiologist: " + study.radiologistName)
                            if (study.reportNumber.isNotBlank()) Text("Report number: " + study.reportNumber)
                            DocumentOriginalAttachments(
                                documentId = medicalImagingAttachmentDocumentId(study.id),
                                onAttachmentIdsChanged = { ids ->
                                    if (index < imagingStudies.size && imagingStudies[index].id == study.id && imagingStudies[index].attachmentIds != ids) {
                                        imagingStudies[index] = imagingStudies[index].copy(attachmentIds = ids)
                                    }
                                }
                            )
                            Text("Remove", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { imagingStudies.remove(study) })
                        }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Automatic on-device translation", fontWeight = FontWeight.SemiBold)
                Text("Translation is processed on this device. The first use may download language models. Original values are preserved and never overwritten.", style = MaterialTheme.typography.bodySmall)
                Button(enabled = !translating && selectedTargets.any { it.value } && sourceLanguage != "other", onClick = {
                    translating = true
                    translationScope.launch {
                        val provider = MlKitMedicalTranslationProvider(context)
                        var completed = 0
                        var failed = 0
                        val eligible = definitions.filter { definition -> values[fieldKey(definition.key)].orEmpty().isNotBlank() }
                        selectedTargets.filterValues { it }.keys.toList().forEach { target ->
                            if (target.substringBefore('-') == sourceLanguage.substringBefore('-')) return@forEach
                            eligible.forEach { definition ->
                                val key = definition.key
                                if (isMedicalCertificateTranslatableField(key)) {
                                    val result = runCatching { provider.translate(values[fieldKey(key)].orEmpty(), sourceLanguage, target) }
                                    result.onSuccess { translationValues["$target|$key"] = it; completed++ }.onFailure { failed++ }
                                }
                                val labelResult = if (target == "en") Result.success(definition.label) else runCatching { provider.translate(definition.label, "en", target) }
                                labelResult.onSuccess { translationValues["$target|label:$key"] = it }.onFailure { failed++ }
                                if (sourceLanguage != "en") {
                                    val sourceLabelResult = runCatching { provider.translate(definition.label, "en", sourceLanguage) }
                                    sourceLabelResult.onSuccess { translationValues["$sourceLanguage|label:$key"] = it }.onFailure { failed++ }
                                }
                            }
                        }
                        translating = false
                        saveMessage = "Translation finished: $completed field(s) translated; $failed failed. Review and correct the translated text before use."
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text(if (translating) "Translating…" else "Translate selected languages") }
                if (translating) Text("Downloading language model or translating fields…", color = MaterialTheme.colorScheme.primary)
            }
        }

        selectedTargets.filterValues { it }.keys.forEach { target ->
            val translatedFields = definitions.filter { definition -> translationValues.containsKey("$target|" + definition.key) || translationValues.containsKey("$target|label:" + definition.key) }
            if (translatedFields.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Translation — ${languages.firstOrNull { it.code == target }?.englishName ?: target}", fontWeight = FontWeight.SemiBold)
                        Text("Edit translated field names and values. Source text remains separately preserved.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            val previousSource = sourceLanguage
                            definitions.forEach { definition ->
                                val key = definition.key
                                val translatedValue = translationValues["$target|$key"] ?: values[fieldKey(key)].orEmpty()
                                translationValues["$previousSource|$key"] = values[fieldKey(key)].orEmpty()
                                values[fieldKey(key)] = translatedValue
                                if (!translationValues.containsKey("$previousSource|label:$key")) translationValues["$previousSource|label:$key"] = definition.label
                            }
                            sourceLanguage = target
                            selectedTargets.clear()
                            selectedTargets[previousSource] = true
                            saveMessage = "Language direction switched. Save the certificate to persist the new source language."
                        }, modifier = Modifier.fillMaxWidth()) { Text("Swap original and translation") }
                        translatedFields.forEach { definition ->
                            val translationKey = "$target|" + definition.key
                            OutlinedTextField(value = translationValues["$target|label:" + definition.key] ?: definition.label,
                                onValueChange = { translationValues["$target|label:" + definition.key] = it },
                                label = { Text("Translated field name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = translationValues[translationKey].orEmpty(), onValueChange = { translationValues[translationKey] = it },
                                label = { Text("Translated value") }, modifier = Modifier.fillMaxWidth(),
                                minLines = if (definition.key in setOf("chiefComplaint", "medicalHistory", "diagnosis", "physicalExamination", "systemsReview", "functionalLimitations", "prognosis", "recommendations")) 3 else 1)
                        }
                    }
                }
            }
        }

        if (loadError.isNotBlank()) {
            Text(
                "Saved medical data could not be decrypted. Saving is disabled to protect the existing file.",
                color = MaterialTheme.colorScheme.error
            )
        }
        validationErrors.forEach { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            enabled = loadError.isBlank() && selectedTargets.any { it.value },
            onClick = {
                val targets = selectedTargets.filterValues { it }.keys.toList()
                val fields = definitions.filter { definition -> values[fieldKey(definition.key)].orEmpty().isNotBlank() }
                    .flatMap { definition ->
                        val sourceValue = values[fieldKey(definition.key)].orEmpty()
                        targets.map { target ->
                            val languageName = languages.firstOrNull { it.code == target }?.englishName ?: target
                            MedicalCertificatePdfField(
                                sourceLabel = translationValues["$sourceLanguage|label:" + definition.key] ?: definition.label,
                                sourceValue = sourceValue,
                                targetLabel = languageName + " — " + (translationValues["$target|label:" + definition.key] ?: definition.label),
                                targetValue = translationValues["$target|" + definition.key] ?: if (isMedicalCertificateTranslatableField(definition.key)) "[Translation missing]" else sourceValue
                            )
                        }
                    }
                pendingPdfBytes = runCatching {
                    createBilingualMedicalCertificatePdf(sourceLanguage, targets.joinToString(", ") { target -> languages.firstOrNull { it.code == target }?.englishName ?: target }, fields)
                }.getOrElse { saveMessage = "PDF generation failed: " + (it.message ?: "unknown error"); null }
                if (pendingPdfBytes != null) pdfLauncher.launch("international-medical-certificate-bilingual.pdf")
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export bilingual PDF") }
        Button(
            onClick = {
                val integrity = runCatching {
                    reconcileOriginalAttachments(context)
                    imagingStudies.filterNot { study ->
                        medicalImagingAttachmentReferencesMatch(
                            study.attachmentIds,
                            listOriginalAttachmentIds(context, medicalImagingAttachmentDocumentId(study.id))
                        )
                    }.map { it.id }
                }
                if (integrity.isFailure) {
                    saveMessage = "Attachment integrity check failed; certificate data was not changed."
                } else {
                    val mismatchedStudyIds = integrity.getOrThrow()
                    if (mismatchedStudyIds.isNotEmpty()) {
                        saveMessage = "Attachment references do not match the encrypted attachment store for: ${mismatchedStudyIds.joinToString()}."
                    } else {
                        val previousStudies = values[IMC_IMAGING_STUDIES_KEY]
                            ?.takeIf { it.isNotBlank() }
                            ?.let(::decodeMedicalImagingStudies)
                            .orEmpty()
                        val currentStudies = imagingStudies.toList()
                        val updatedValues = values.toMutableMap().apply {
                            put(fieldKey("sourceLanguage"), sourceLanguage)
                            put(fieldKey("targetLanguages"), selectedTargets.filterValues { it }.keys.sorted().joinToString(","))
                            put(IMC_SOURCE_LANGUAGE_KEY, sourceLanguage)
                            put(IMC_OTHER_LANGUAGE_KEY, otherLanguage)
                            put(IMC_TARGET_LANGUAGES_KEY, selectedTargets.filterValues { it }.keys.sorted().joinToString(","))
                            put("imc_doctor_id", selectedDoctor?.id.orEmpty())
                            put("imc_doctor_name", selectedDoctor?.name.orEmpty())
                            put("imc_doctor_practice", selectedDoctor?.practice.orEmpty())
                            put("imc_doctor_address", selectedDoctor?.address.orEmpty())
                            put("imc_doctor_register", selectedDoctor?.register.orEmpty())
                            put("imc_doctor_license", selectedDoctor?.license.orEmpty())
                            val translatedJson = JSONObject()
                            selectedTargets.filterValues { it }.keys.forEach { target ->
                                val targetJson = JSONObject()
                                translationValues.filterKeys { it.startsWith("$target|") }.forEach { (compound, translated) -> targetJson.put(compound.substringAfter("|"), translated) }
                                if (targetJson.length() > 0) translatedJson.put(target, targetJson)
                            }
                            put("imc_translations", translatedJson.toString())
                            put(IMC_IMAGING_STUDIES_KEY, encodeMedicalImagingStudies(currentStudies))
                        }
                        val saveResult = runCatching {
                            InternationalMedicalCertificateRecords.save(context, recordId, updatedValues)
                        }
                        if (saveResult.isFailure) {
                            saveMessage = "Certificate data could not be saved; existing attachment scopes were preserved."
                        } else {
                            values.putAll(updatedValues)
                            val removedStudyIds = removedMedicalImagingStudyIds(previousStudies, currentStudies)
                            val cleanupFailures = removedStudyIds.count { studyId ->
                                runCatching {
                                    removeOriginalAttachmentDocumentScope(
                                        context,
                                        medicalImagingAttachmentDocumentId(studyId)
                                    )
                                }.isFailure
                            }
                            saveMessage = if (cleanupFailures == 0) {
                                "Certificate data saved securely on this device."
                            } else {
                                "Certificate data saved, but $cleanupFailures removed study attachment scope(s) could not be fully cleaned up."
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loadError.isBlank() && validationErrors.isEmpty()
        ) {
            Text("Save certificate")
        }
        if (saveMessage.isNotBlank()) {
            Text(saveMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}

internal fun isMedicalCertificateTranslatableField(fieldKey: String): Boolean {
    val key = fieldKey.lowercase()
    val protectedTokens = listOf("date", "number", "code", "id", "phone", "email", "postal", "zip", "icd", "height", "weight", "pressure", "pulse", "temperature", "saturation", "signature")
    return protectedTokens.none { it in key } && !key.endsWith("name")
}

internal fun removedMedicalImagingStudyIds(
    previous: List<MedicalImagingStudy>,
    current: List<MedicalImagingStudy>
): List<String> {
    val currentIds = current.map { it.id }.toSet()
    return previous.filterNot { it.id in currentIds }.map { it.id }
}

internal fun medicalImagingAttachmentDocumentId(studyId: String): String {
    require(studyId.matches(Regex("[A-Za-z0-9_.:-]{1,96}"))) { "Invalid imaging study identifier." }
    return "imaging_study_$studyId"
}

internal fun encodeMedicalImagingStudies(studies: List<MedicalImagingStudy>): String {
    val array = JSONArray()
    studies.forEach { study ->
        array.put(JSONObject().apply {
            put("id", study.id)
            put("modality", study.modality.name)
            put("bodyRegion", study.bodyRegion)
            put("studyDate", study.studyDate)
            put("facility", study.facility)
            put("indication", study.indication)
            put("findings", study.findings)
            put("impression", study.impression)
            put("technique", study.technique)
            put("contrast", study.contrast)
            put("radiologistName", study.radiologistName)
            put("reportNumber", study.reportNumber)
            put("attachmentIds", JSONArray(study.attachmentIds))
        })
    }
    return array.toString()
}

internal fun loadMedicalImagingStudies(serialized: String?): List<MedicalImagingStudy> =
    serialized?.takeIf { it.isNotBlank() }?.let(::decodeMedicalImagingStudies).orEmpty()

internal fun decodeMedicalImagingStudies(serialized: String): List<MedicalImagingStudy> {
    val array = JSONArray(serialized)
    val studies = (0 until array.length()).map { index ->
        val item = array.getJSONObject(index)
        MedicalImagingStudy(
            id = item.getString("id"),
            modality = MedicalImagingModality.valueOf(item.getString("modality")),
            bodyRegion = item.optString("bodyRegion"),
            studyDate = item.optString("studyDate"),
            facility = item.optString("facility"),
            indication = item.optString("indication"),
            findings = item.optString("findings"),
            impression = item.optString("impression"),
            technique = item.optString("technique"),
            contrast = item.optString("contrast"),
            radiologistName = item.optString("radiologistName"),
            reportNumber = item.optString("reportNumber"),
            attachmentIds = item.optJSONArray("attachmentIds")?.let { items ->
                (0 until items.length()).map { items.getString(it) }
            }.orEmpty()
        )
    }
    val ids = studies.map { it.id }
    require(ids.all { it.matches(Regex("[A-Za-z0-9_.:-]{1,96}")) } && ids.size == ids.toSet().size) {
        "Saved imaging studies contain invalid or duplicate identifiers."
    }
    require(studies.all { isValidMedicalStudyDate(it.studyDate) }) {
        "Saved imaging studies contain an invalid examination date."
    }
    require(studies.all { study ->
        study.attachmentIds.all { it.matches(Regex("[A-Za-z0-9-]{1,64}")) } &&
            study.attachmentIds.size == study.attachmentIds.toSet().size
    }) {
        "Saved imaging studies contain invalid or duplicate attachment identifiers."
    }
    return studies
}


/** Accepts an optional ISO date; rejects impossible calendar dates and trailing text. */
internal fun isValidMedicalStudyDate(value: String): Boolean {
    if (value.isBlank()) return true
    if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    val position = ParsePosition(0)
    val parsed = formatter.parse(value, position)
    return parsed != null && position.index == value.length
}
