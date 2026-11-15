// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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

private const val IMC_SOURCE_LANGUAGE_KEY = "imc_source_language"
private const val IMC_OTHER_LANGUAGE_KEY = "imc_other_source_language"
private const val IMC_TARGET_LANGUAGES_KEY = "imc_target_languages"
private const val IMC_IMAGING_STUDIES_KEY = "imc_imaging_studies"

@Composable
internal fun InternationalMedicalCertificateEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val initialLoad = remember(context) {
        runCatching { InternationalMedicalCertificateStore.loadStrict(context) }
    }
    val values = remember(context) {
        mutableStateMapOf<String, String>().apply { putAll(initialLoad.getOrDefault(emptyMap())) }
    }
    val imagingLoad = remember { runCatching { decodeMedicalImagingStudies(values[IMC_IMAGING_STUDIES_KEY].orEmpty()) } }
    val loadError = initialLoad.exceptionOrNull()?.message
        ?: imagingLoad.exceptionOrNull()?.message.orEmpty()
    val definitions = remember {
        documentEditorFieldDefinitions(INTERNATIONAL_MEDICAL_CERTIFICATE_ID)
    }
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
                Text("Original Language", fontWeight = FontWeight.SemiBold)
                languages.forEach { language ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sourceLanguage == language.code,
                            onClick = { sourceLanguage = language.code }
                        )
                        Text(language.englishName, modifier = Modifier.clickable { sourceLanguage = language.code })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sourceLanguage == "other",
                        onClick = { sourceLanguage = "other" }
                    )
                    Text("Other", modifier = Modifier.clickable { sourceLanguage = "other" })
                }
                if (sourceLanguage == "other") {
                    OutlinedTextField(
                        value = otherLanguage,
                        onValueChange = { otherLanguage = it.take(35) },
                        label = { Text("Other source language") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Target Languages", fontWeight = FontWeight.SemiBold)
                Text(
                    "Select one or more languages for a later translation step.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                languages.forEach { language ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedTargets[language.code] == true,
                            onCheckedChange = { selectedTargets[language.code] = it }
                        )
                        Text(language.englishName, modifier = Modifier.clickable {
                            selectedTargets[language.code] = selectedTargets[language.code] != true
                        })
                    }
                }
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
                        id = "imaging-" + System.currentTimeMillis().toString(),
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
                imagingStudies.forEach { study ->
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
                            Text("Remove", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { imagingStudies.remove(study) })
                        }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                "Automatic translation is not active yet. The original text remains stored locally; no medical data is sent to a translation service from this screen.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (loadError.isNotBlank()) {
            Text(
                "Saved medical data could not be decrypted. Saving is disabled to protect the existing file.",
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = {
                values[fieldKey("sourceLanguage")] = sourceLanguage
                values[fieldKey("targetLanguages")] = selectedTargets.filterValues { it }.keys.sorted().joinToString(",")
                values[IMC_SOURCE_LANGUAGE_KEY] = sourceLanguage
                values[IMC_OTHER_LANGUAGE_KEY] = otherLanguage
                values[IMC_TARGET_LANGUAGES_KEY] = selectedTargets.filterValues { it }.keys.sorted().joinToString(",")
                values[IMC_IMAGING_STUDIES_KEY] = encodeMedicalImagingStudies(imagingStudies)
                InternationalMedicalCertificateStore.save(context, values.toMap())
                saveMessage = "Certificate data saved securely on this device."
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loadError.isBlank()
        ) {
            Text("Save certificate")
        }
        if (saveMessage.isNotBlank()) {
            Text(saveMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
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

internal fun decodeMedicalImagingStudies(serialized: String): List<MedicalImagingStudy> {
    val array = JSONArray(serialized)
    return (0 until array.length()).map { index ->
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
