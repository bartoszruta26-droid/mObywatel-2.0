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

private const val IMC_SOURCE_LANGUAGE_KEY = "imc_source_language"
private const val IMC_OTHER_LANGUAGE_KEY = "imc_other_source_language"
private const val IMC_TARGET_LANGUAGES_KEY = "imc_target_languages"

@Composable
internal fun InternationalMedicalCertificateEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val values = remember(context) {
        mutableStateMapOf<String, String>().apply { putAll(InternationalMedicalCertificateStore.load(context)) }
    }
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

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                "Automatic translation is not active yet. The original text remains stored locally; no medical data is sent to a translation service from this screen.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                values[fieldKey("sourceLanguage")] = sourceLanguage
                values[fieldKey("targetLanguages")] = selectedTargets.filterValues { it }.keys.sorted().joinToString(",")
                values[IMC_SOURCE_LANGUAGE_KEY] = sourceLanguage
                values[IMC_OTHER_LANGUAGE_KEY] = otherLanguage
                values[IMC_TARGET_LANGUAGES_KEY] = selectedTargets.filterValues { it }.keys.sorted().joinToString(",")
                InternationalMedicalCertificateStore.save(context, values.toMap())
                saveMessage = "Certificate data saved securely on this device."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save certificate")
        }
        if (saveMessage.isNotBlank()) {
            Text(saveMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}
