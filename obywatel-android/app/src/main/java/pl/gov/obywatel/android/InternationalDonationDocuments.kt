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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

internal const val INTERNATIONAL_DONATION_WORSHIP_ID = "international_donation_worship"
internal const val INTERNATIONAL_DONATION_NGO_ID = "international_donation_ngo"
internal const val INTERNATIONAL_DONATION_PERSON_ID = "international_donation_person"

internal data class DonationDocumentField(
    val key: String,
    val label: String,
    val hint: String = "",
    val required: Boolean = false
)

internal data class DonationDocumentDefinition(
    val id: String,
    val title: String,
    val category: String,
    val introduction: String,
    val fields: List<DonationDocumentField>
) {
    val requiredFieldKeys: Set<String> get() = fields.filter { it.required }.map { it.key }.toSet()
}

private fun commonDonorFields() = listOf(
    DonationDocumentField("donor_name", "Imię i nazwisko / pełna nazwa darczyńcy", required = true),
    DonationDocumentField("donor_address", "Adres darczyńcy", "Ulica, kod pocztowy, miejscowość, państwo", true),
    DonationDocumentField("donor_country", "Państwo darczyńcy", required = true),
    DonationDocumentField("donor_identity", "Numer dokumentu tożsamości / identyfikator podatkowy"),
    DonationDocumentField("donor_contact", "Adres e-mail lub telefon")
)

private fun commonDonationFields() = listOf(
    DonationDocumentField("donation_object", "Przedmiot darowizny", "Środki pieniężne lub opis rzeczy/prawa", true),
    DonationDocumentField("donation_value", "Wartość darowizny", required = true),
    DonationDocumentField("donation_currency", "Waluta", "Kod ISO 4217, np. PLN, EUR, USD", true),
    DonationDocumentField("donation_date", "Data przekazania / przyrzeczenia darowizny", "RRRR-MM-DD", true),
    DonationDocumentField("donation_method", "Sposób przekazania", "Przelew, gotówka, przekazanie rzeczy lub inny sposób", true),
    DonationDocumentField("transfer_reference", "Numer / tytuł przelewu lub potwierdzenia"),
    DonationDocumentField("conditions", "Warunki i ograniczenia darowizny"),
    DonationDocumentField("place", "Miejscowość i państwo zawarcia dokumentu", required = true),
    DonationDocumentField("donor_declaration", "Oświadczenie darczyńcy", "Własność przedmiotu, dobrowolność i brak obciążeń — zgodnie z prawem właściwym", true),
    DonationDocumentField("recipient_acceptance", "Oświadczenie o przyjęciu darowizny", required = true),
    DonationDocumentField("additional_notes", "Dodatkowe postanowienia")
)

internal object InternationalDonationDocumentCatalog {
    val all: Map<String, DonationDocumentDefinition> = listOf(
        DonationDocumentDefinition(
            INTERNATIONAL_DONATION_WORSHIP_ID,
            "Międzynarodowy dokument darowizny na cele kultu religijnego",
            "Służba kościelna",
            "Oświadczenie / umowa darowizny przeznaczonej na konkretnie wskazany cel kultu religijnego. Wskaż podmiot religijny, właściwą jednostkę i cel wykorzystania środków.",
            commonDonorFields() + listOf(
                DonationDocumentField("recipient_name", "Nazwa Kościoła, związku wyznaniowego lub jednostki religijnej", required = true),
                DonationDocumentField("recipient_unit", "Parafia, diecezja, zbór, wspólnota lub inna jednostka"),
                DonationDocumentField("recipient_legal_status", "Status prawny odbiorcy w państwie siedziby", required = true),
                DonationDocumentField("recipient_address", "Adres odbiorcy", "Pełny adres wraz z państwem", true),
                DonationDocumentField("recipient_country", "Państwo siedziby odbiorcy", required = true),
                DonationDocumentField("recipient_registry", "Rejestr / numer identyfikacyjny odbiorcy"),
                DonationDocumentField("recipient_representative", "Osoba uprawniona do reprezentacji"),
                DonationDocumentField("religious_purpose", "Szczegółowy cel kultu religijnego", "Np. utrzymanie miejsca kultu, liturgia, wyposażenie liturgiczne", true)
            ) + commonDonationFields()
        ),
        DonationDocumentDefinition(
            INTERNATIONAL_DONATION_NGO_ID,
            "Międzynarodowy dokument darowizny na rzecz krajowej organizacji pozarządowej",
            "Służba stowarzyszeniowa",
            "Dokument darowizny na rzecz organizacji pozarządowej zarejestrowanej w państwie darczyńcy lub odbiorcy. Podaj kraj rejestracji i identyfikatory pozwalające ustalić podmiot.",
            commonDonorFields() + listOf(
                DonationDocumentField("recipient_name", "Pełna nazwa organizacji pozarządowej", required = true),
                DonationDocumentField("recipient_legal_form", "Forma prawna", "Fundacja, stowarzyszenie lub inna forma właściwa dla danego państwa", true),
                DonationDocumentField("recipient_registry", "Nazwa rejestru i numer wpisu", required = true),
                DonationDocumentField("recipient_tax_id", "Numer identyfikacji podatkowej organizacji"),
                DonationDocumentField("recipient_address", "Adres siedziby", "Ulica, kod pocztowy, miejscowość, państwo", true),
                DonationDocumentField("recipient_country", "Państwo rejestracji organizacji", required = true),
                DonationDocumentField("recipient_representative", "Imię i nazwisko oraz funkcja osoby reprezentującej"),
                DonationDocumentField("statutory_purpose", "Cel statutowy / public-benefit purpose", required = true)
            ) + commonDonationFields()
        ),
        DonationDocumentDefinition(
            INTERNATIONAL_DONATION_PERSON_ID,
            "Międzynarodowy dokument darowizny na rzecz osoby fizycznej",
            "Służba cywilna",
            "Dokument darowizny pomiędzy osobami fizycznymi, z rozróżnieniem darczyńcy i obdarowanego oraz opisem przedmiotu, wartości i sposobu przekazania.",
            commonDonorFields() + listOf(
                DonationDocumentField("recipient_name", "Imię i nazwisko obdarowanego", required = true),
                DonationDocumentField("recipient_birth_date", "Data urodzenia obdarowanego", "RRRR-MM-DD"),
                DonationDocumentField("recipient_identity", "Numer dokumentu tożsamości / identyfikator"),
                DonationDocumentField("recipient_address", "Adres obdarowanego", "Ulica, kod pocztowy, miejscowość, państwo", true),
                DonationDocumentField("recipient_country", "Państwo zamieszkania obdarowanego", required = true),
                DonationDocumentField("relationship", "Relacja między darczyńcą a obdarowanym"),
                DonationDocumentField("recipient_contact", "Adres e-mail lub telefon obdarowanego")
            ) + commonDonationFields()
        )
    ).associateBy { it.id }

    fun find(id: String): DonationDocumentDefinition? = all[id]

    fun missingRequiredFields(id: String, values: Map<String, String>): Set<String> {
        val definition = requireNotNull(find(id)) { "Nieznany rodzaj dokumentu darowizny." }
        return definition.requiredFieldKeys.filterTo(linkedSetOf()) { values[it].isNullOrBlank() }
    }
}

private fun donationRecordId(documentId: String): String = when (documentId) {
    INTERNATIONAL_DONATION_WORSHIP_ID -> "donation_worship"
    INTERNATIONAL_DONATION_NGO_ID -> "donation_ngo"
    INTERNATIONAL_DONATION_PERSON_ID -> "donation_person"
    else -> error("Unknown donation document.")
}

@Composable
internal fun InternationalDonationDocumentEditorScreen(
    documentId: String,
    onBack: () -> Unit
) {
    val definition = requireNotNull(InternationalDonationDocumentCatalog.find(documentId))
    val context = LocalContext.current
    val recordId = donationRecordId(documentId)
    val loaded = remember(context, recordId) {
        runCatching { InternationalMedicalCertificateRecords.load(context, recordId) }.getOrDefault(emptyMap())
    }
    val values = remember(recordId) {
        mutableStateMapOf<String, String>().apply {
            definition.fields.forEach { field -> put(field.key, loaded["don_${field.key}"].orEmpty()) }
        }
    }
    val translations = remember(recordId) {
        mutableStateMapOf<String, String>().apply {
            runCatching { JSONObject(loaded["don_translations"].orEmpty()) }.getOrNull()?.let { json ->
                val keys = json.keys()
                while (keys.hasNext()) { val key = keys.next(); put(key, json.optString(key)) }
            }
        }
    }
    var sourceLanguage by remember(recordId) { mutableStateOf(loaded["don_source_language"] ?: "pl") }
    var targetLanguage by remember(recordId) { mutableStateOf(loaded["don_target_language"] ?: "en") }
    var sourceMenu by remember { mutableStateOf(false) }
    var targetMenu by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var pendingPdf by remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()
    val languages = MedicalCertificateLanguages.supported
    val missing = InternationalDonationDocumentCatalog.missingRequiredFields(documentId, values)
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val bytes = pendingPdf
        if (uri != null && bytes != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Nie można zapisać dokumentu PDF.")
        }.onSuccess { message = "Zapisano dwujęzyczny dokument PDF." }
            .onFailure { message = it.message ?: "Nie udało się zapisać PDF." }
        pendingPdf = null
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹ Powrót do dokumentów", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text(definition.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(definition.introduction)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Język dokumentu", fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { sourceMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Język oryginału: " + (languages.firstOrNull { it.code == sourceLanguage }?.englishName ?: sourceLanguage))
                }
                DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                    languages.forEach { language -> DropdownMenuItem(
                        text = { Text(language.englishName) },
                        onClick = { sourceLanguage = language.code; translations.clear(); sourceMenu = false }
                    ) }
                }
                OutlinedButton(onClick = { targetMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Język tłumaczenia: " + (languages.firstOrNull { it.code == targetLanguage }?.englishName ?: targetLanguage))
                }
                DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) {
                    languages.filter { it.code != sourceLanguage }.forEach { language -> DropdownMenuItem(
                        text = { Text(language.englishName) },
                        onClick = { targetLanguage = language.code; targetMenu = false }
                    ) }
                }
                Text("Tłumaczenie maszynowe jest przygotowywane na urządzeniu. Oryginalne dane pozostają zachowane; przed podpisaniem obie wersje należy sprawdzić.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Dane stron i przedmiot darowizny", fontWeight = FontWeight.SemiBold)
        definition.fields.forEach { field ->
            OutlinedTextField(
                value = values[field.key].orEmpty(),
                onValueChange = {
                    values[field.key] = it
                    translations.remove("$targetLanguage|${field.key}")
                },
                label = { Text(field.label + if (field.required) " *" else "") },
                placeholder = { if (field.hint.isNotBlank()) Text(field.hint) },
                modifier = Modifier.fillMaxWidth(),
                minLines = if (field.key in setOf("donor_declaration", "recipient_acceptance", "conditions", "additional_notes", "religious_purpose", "statutory_purpose")) 2 else 1
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !busy && sourceLanguage != targetLanguage, onClick = {
                busy = true
                message = ""
                scope.launch {
                    var translatedCount = 0
                    var failedCount = 0
                    runCatching { MlKitMedicalTranslationProvider(context) }.onSuccess { provider ->
                        definition.fields.forEach { field ->
                            val source = values[field.key].orEmpty()
                            if (source.isNotBlank()) {
                                runCatching { provider.translate(source, sourceLanguage, targetLanguage).trim() }
                                    .onSuccess { translated ->
                                        if (isValidMedicalCertificateValueTranslation(source, translated, field.label)) {
                                            translations["$targetLanguage|${field.key}"] = translated
                                            translatedCount++
                                        } else failedCount++
                                    }.onFailure { failedCount++ }
                            }
                            if (sourceLanguage != "pl") {
                                runCatching { provider.translate(field.label, "pl", sourceLanguage).trim() }
                                    .onSuccess { translations["$sourceLanguage|label:${field.key}"] = it }
                                    .onFailure { failedCount++ }
                            }
                            if (targetLanguage != "pl") {
                                runCatching { provider.translate(field.label, "pl", targetLanguage).trim() }
                                    .onSuccess { translations["$targetLanguage|label:${field.key}"] = it }
                                    .onFailure { failedCount++ }
                            } else translations["$targetLanguage|label:${field.key}"] = field.label
                        }
                    }.onFailure { failedCount++ }
                    busy = false
                    message = "Przetłumaczono pól: $translatedCount; błędy lub brak danych: $failedCount. Sprawdź tłumaczenie przed podpisaniem."
                }
            }) { Text(if (busy) "Tłumaczenie…" else "Przetłumacz automatycznie") }
            OutlinedButton(enabled = !busy, onClick = {
                val fields = definition.fields.map { field ->
                    MedicalCertificatePdfField(
                        sourceLabel = translations["$sourceLanguage|label:${field.key}"] ?: field.label,
                        sourceValue = values[field.key].orEmpty(),
                        targetLabel = translations["$targetLanguage|label:${field.key}"] ?: field.label,
                        targetValue = translations["$targetLanguage|${field.key}"].orEmpty()
                    )
                }
                pendingPdf = createBilingualMedicalCertificatePdf(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    fields = fields,
                    sideBySideFields = true,
                    documentTitle = definition.title.uppercase(),
                    issuerHeading = "DONOR / DAROWIZCA",
                    signatureCaption = "Donor's signature / Podpis darczyńcy",
                    stampCaption = "Recipient identification / Dane odbiorcy",
                    patientSignatureCaption = "Recipient's acceptance / Podpis obdarowanego",
                    dateAndPlaceCaption = "Place and date / Miejscowość i data"
                )
                pdfLauncher.launch("darowizna-${documentId}.pdf")
            }) { Text("Eksportuj PDF") }
        }
        if (missing.isNotEmpty()) {
            Text("Uzupełnij wymagane pola: " + definition.fields.filter { it.key in missing }.joinToString { it.label },
                color = MaterialTheme.colorScheme.error)
        }
        Text("Dokument zawiera pola do podpisu stron. Skutki podatkowe, wymagania rejestrowe i forma umowy zależą od prawa właściwego dla stron i miejsca dokonania darowizny. Sam formularz ani tłumaczenie nie potwierdzają prawa do ulgi podatkowej.",
            style = MaterialTheme.typography.bodySmall)
        Button(enabled = !busy && missing.isEmpty(), onClick = {
            val stored = values.mapKeys { "don_${it.key}" }.toMutableMap()
            stored["don_source_language"] = sourceLanguage
            stored["don_target_language"] = targetLanguage
            val json = JSONObject()
            translations.forEach { (key, value) -> json.put(key, value) }
            stored["don_translations"] = json.toString()
            runCatching { InternationalMedicalCertificateRecords.save(context, recordId, stored) }
                .onSuccess { message = "Zapisano dokument darowizny w bezpiecznym magazynie aplikacji." }
                .onFailure { message = it.message ?: "Nie udało się zapisać dokumentu." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz dokument") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
    }
}
