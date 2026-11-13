// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Central definition of documents that previously had no dedicated graphic editor.
 * A document key is also the persistent appearance-settings namespace.
 */
internal enum class DocumentPreviewKind { CREDENTIAL, RECORD }

internal data class GraphicDocumentDefinition(
    val id: String,
    val title: String,
    val kind: DocumentPreviewKind,
    val photoLabel: String = "Fotografia posiadacza",
    val supportsPhoto: Boolean = true
)

internal object GraphicDocumentCatalog {
    private val baseDefinitions = listOf(
        GraphicDocumentDefinition("registry", "Odpis z rejestru podmiotów", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("birth_extract", "Akt urodzenia", DocumentPreviewKind.RECORD, "Skan / fotografia aktu"),
        GraphicDocumentDefinition("marriage_extract", "Akt małżeństwa", DocumentPreviewKind.RECORD, "Skan / fotografia aktu"),
        GraphicDocumentDefinition("death_extract", "Odpis aktu zgonu", DocumentPreviewKind.RECORD, "Skan / fotografia aktu"),
        GraphicDocumentDefinition("large_family", "Karta Dużej Rodziny", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("blood_donor", "Legitymacja Honorowego Dawcy Krwi", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("disability_card", "Legitymacja osoby z niepełnosprawnością", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("vehicle_registration", "Dowód rejestracyjny pojazdu", DocumentPreviewKind.RECORD, "Skan / fotografia dowodu"),
        GraphicDocumentDefinition("insurance_vehicle", "Ubezpieczenie OC pojazdu", DocumentPreviewKind.RECORD, "Skan / fotografia polisy"),
        GraphicDocumentDefinition("insurance_professional", "Ubezpieczenie OC zawodowe", DocumentPreviewKind.RECORD, "Skan / fotografia polisy"),
        GraphicDocumentDefinition("insurance_private", "Ubezpieczenie OC w życiu prywatnym", DocumentPreviewKind.RECORD, "Skan / fotografia polisy"),
        GraphicDocumentDefinition("insurance_business", "Ubezpieczenie OC działalności", DocumentPreviewKind.RECORD, "Skan / fotografia polisy"),
        GraphicDocumentDefinition("criminal_record", "Zaświadczenie o niekaralności", DocumentPreviewKind.RECORD, "Skan / fotografia zaświadczenia"),
        GraphicDocumentDefinition("mission_travel", "Dokument podróży służbowej / misyjnej", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("ehic", "Europejska Karta Ubezpieczenia Zdrowotnego", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("eu_education", "Zaświadczenie o zgodności kwalifikacji z UE", DocumentPreviewKind.RECORD, "Skan / fotografia zaświadczenia"),
        GraphicDocumentDefinition("church_bishop", "Dokument godności biskupiej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("church_archbishop", "Dokument godności arcybiskupiej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("church_cardinal", "Dokument godności kardynalskiej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("church_abbot", "Dokument godności opackiej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("church_minister", "Dokument posługi kościelnej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("church_missionary", "Dokument posługi misyjnej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("professional_medical", "Kwalifikacje medyczne", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("professional_education", "Kwalifikacje edukacyjne", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("professional_therapeutic", "Kwalifikacje terapeutyczne", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("professional_other", "Pozostałe kwalifikacje zawodowe", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("organization_foundation", "Dokumenty fundacji", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("organization_church", "Dokumenty Kościoła / wspólnoty", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("organization_company", "Dokumenty przedsiębiorstwa", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("organization_association", "Dokumenty stowarzyszenia", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("official", "Pozostałe dokumenty urzędowe", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("military_book", "Książeczka wojskowa", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("martial_arts", "Legitymacja sztuk walki", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("shooting_sport", "Legitymacja strzelecka i sportowa", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("archery", "Legitymacja łucznicza", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("tactical_training", "Certyfikat szkolenia taktycznego", DocumentPreviewKind.RECORD, "Skan / fotografia certyfikatu"),
        GraphicDocumentDefinition("survival_training", "Certyfikat surwiwalu i ratownictwa terenowego", DocumentPreviewKind.RECORD, "Skan / fotografia certyfikatu"),
        GraphicDocumentDefinition("security_guard", "Legitymacja i kwalifikacje ochroniarskie", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("defense_training", "Dokument szkolenia obronnego", DocumentPreviewKind.RECORD, "Skan / fotografia dokumentu"),
        GraphicDocumentDefinition("historical_reenactment", "Legitymacja członka grupy rekonstrukcji historycznej", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("reenactment_qualification", "Certyfikat kwalifikacji rekonstruktora", DocumentPreviewKind.RECORD, "Skan / fotografia certyfikatu"),
        GraphicDocumentDefinition("reenactment_event", "Karta uczestnictwa w wydarzeniu historycznym", DocumentPreviewKind.CREDENTIAL),
        GraphicDocumentDefinition("loyalty_cards", "Karty lojalnościowe sklepów", DocumentPreviewKind.CREDENTIAL)
    )

    // Katalog jest rozszerzany automatycznie o wszystkie typy z rejestru dokumentów.
    // Dzięki temu dodanie nowego dokumentu do ustawień nie wymaga drugiej, ręcznej
    // rejestracji w katalogu edytora graficznego.
    private val credentialKeys = setOf(
        "identity", "passport", "parliament", "training", "student", "family",
        "church", "church_bishop", "church_archbishop", "church_cardinal",
        "church_abbot", "church_minister", "church_missionary", "church_bishop_title",
        "church_minister_card", "missionary_identity", "professional",
        "professional_medical", "professional_education", "professional_therapeutic",
        "professional_other", "large_family", "blood_donor", "disability_card",
        "ehic", "loyalty", "loyalty_cards", "profile"
    )

    val definitions: List<GraphicDocumentDefinition> by lazy {
        val missing = documentBrandTargets
            .filterNot { target -> baseDefinitions.any { it.id == target.first } }
            .map { (id, title) ->
                val credential = id in credentialKeys ||
                    id.startsWith("professional_") ||
                    title.contains("legitymacja", ignoreCase = true) ||
                    title.contains("karta", ignoreCase = true) ||
                    title.contains("świadectwo", ignoreCase = true) ||
                    title.contains("dyplom", ignoreCase = true)
                GraphicDocumentDefinition(
                    id = id,
                    title = title,
                    kind = if (credential) DocumentPreviewKind.CREDENTIAL else DocumentPreviewKind.RECORD,
                    photoLabel = if (credential) "Fotografia posiadacza" else "Skan / fotografia dokumentu",
                    supportsPhoto = true
                )
            }
        (baseDefinitions + missing).distinctBy { it.id }
    }

    fun find(id: String): GraphicDocumentDefinition? = definitions.firstOrNull { it.id == id }
}

@Composable
internal fun DocumentGraphicCatalogScreen(
    onBack: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp))
        Text("Indywidualna edycja dokumentów", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Wybierz dokument, aby edytować jego dane, fotografię lub skan, zasoby graficzne i podgląd. Ustawienia każdego dokumentu są zapisywane osobno.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GraphicDocumentCatalog.definitions, key = { it.id }) { definition ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(definition.id) }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(definition.title, fontWeight = FontWeight.SemiBold)
                            Text(if (definition.kind == DocumentPreviewKind.CREDENTIAL) "Podgląd legitymacji / karty"
                                else "Podgląd dokumentu ewidencyjnego",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Edytuj  ›", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private val bankGraphicFieldKeys = listOf(
    "account_name", "account_bank", "account_currency", "account_iban", "account_bic",
    "account_holder", "account_type", "account_country", "account_status",
    "account_open_date", "account_close_date", "account_purpose", "account_notes"
)

private fun loadSubdocumentGraphicValues(context: android.content.Context, documentKey: String): Map<String, String> {
    if (documentKey == "disability") {
        val stored = DemoIdentityStore.load(context)
        return mapOf(
            "firstNames" to stored["disabilityPersonName"].orEmpty(),
            "disabilityDecisionType" to stored["disabilityCategory"].orEmpty(),
            "disabilityDecisionNumber" to stored["disabilityDecisionNumber"].orEmpty(),
            "disabilityCaseNumber" to stored["disabilityCaseNumber"].orEmpty(),
            "disabilityDegree" to stored["disabilityDegree"].orEmpty(),
            "disabilityValidity" to stored["disabilityValidity"].orEmpty(),
            "disabilityIssuerName" to stored["disabilityIssuerName"].orEmpty(),
            "disabilityLegalBasis" to stored["disabilityLegalBasis"].orEmpty()
        ).filterValues { it.isNotBlank() }
    }
    val source = when {
        churchGraphicDocumentTargets().any { it.first == documentKey } ->
            ChurchDocumentStore.load(context)[documentKey].orEmpty()
        familyEntityGraphicDocumentTargets().any { it.first == documentKey } ->
            FamilyEntityDocumentStore.load(context)[documentKey].orEmpty()
        officialGraphicDocumentTargets().any { it.first == documentKey } ->
            OfficialDocumentStore.load(context)[documentKey].orEmpty()
        else -> emptyMap()
    }
    if (documentKey == "rod_bank_accounts" || documentKey == "entity_bank_accounts") {
        return bankGraphicFieldKeys.mapIndexedNotNull { index, key ->
            source["account_0_$index"]?.let { key to it }
        }.toMap()
    }
    return source.mapNotNull { (key, value) ->
        key.removePrefix("f").toIntOrNull()?.let { "field_$it" to value }
    }.toMap()
}

private fun saveSubdocumentGraphicValues(
    context: android.content.Context,
    documentKey: String,
    values: Map<String, String>
) {
    if (documentKey == "disability") {
        val current = DemoIdentityStore.load(context)
        val mapped = mapOf(
            "disabilityPersonName" to values["firstNames"].orEmpty(),
            "disabilityCategory" to values["disabilityDecisionType"].orEmpty(),
            "disabilityDecisionNumber" to values["disabilityDecisionNumber"].orEmpty(),
            "disabilityCaseNumber" to values["disabilityCaseNumber"].orEmpty(),
            "disabilityDegree" to values["disabilityDegree"].orEmpty(),
            "disabilityValidity" to values["disabilityValidity"].orEmpty(),
            "disabilityIssuerName" to values["disabilityIssuerName"].orEmpty(),
            "disabilityLegalBasis" to values["disabilityLegalBasis"].orEmpty()
        )
        DemoIdentityStore.save(context, current + mapped)
        return
    }
    val sourceValues = if (documentKey == "rod_bank_accounts" || documentKey == "entity_bank_accounts") {
        bankGraphicFieldKeys.mapIndexedNotNull { index, key ->
            values[key]?.let { "account_0_$index" to it }
        }.toMap()
    } else {
        values.mapNotNull { (key, value) ->
            key.removePrefix("field_").toIntOrNull()?.let { "f$it" to value }
        }.toMap()
    }
    when {
        churchGraphicDocumentTargets().any { it.first == documentKey } -> {
            val current = ChurchDocumentStore.load(context)
            ChurchDocumentStore.save(context, current + (documentKey to (current[documentKey].orEmpty() + sourceValues)))
        }
        familyEntityGraphicDocumentTargets().any { it.first == documentKey } -> {
            val current = FamilyEntityDocumentStore.load(context)
            FamilyEntityDocumentStore.save(context, current + (documentKey to (current[documentKey].orEmpty() + sourceValues)))
        }
        officialGraphicDocumentTargets().any { it.first == documentKey } -> {
            val current = OfficialDocumentStore.load(context)
            OfficialDocumentStore.save(context, current + (documentKey to (current[documentKey].orEmpty() + sourceValues)))
        }
    }
}

@Composable
internal fun DocumentGraphicEditorScreen(
    documentKey: String,
    onBack: () -> Unit
) {
    val definition = GraphicDocumentCatalog.find(documentKey) ?: return
    val context = LocalContext.current
    val prefix = "graphic_document:$documentKey:"
    val options = remember(documentKey) { credentialFieldOptions(documentKey).distinctBy { it.first } }
    val stored = remember(documentKey) { DemoIdentityStore.load(context) }
    val sourceValues = remember(documentKey) { loadSubdocumentGraphicValues(context, documentKey) }
    val values = remember(documentKey) {
        mutableStateMapOf<String, String>().apply {
            options.forEach { (key, _) -> put(key, sourceValues[key] ?: stored[prefix + key].orEmpty()) }
            put("photo", stored[prefix + "photo"].orEmpty())
        }
    }
    var message by remember(documentKey) { mutableStateOf("") }
    var photoRevision by remember(documentKey) { mutableIntStateOf(0) }
    val appearance = rememberLegitimacyPreviewSettings(documentKey)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Nie można odczytać wybranego obrazu.")
        }.onSuccess { bytes ->
            if (bytes.size > 5 * 1024 * 1024) message = "Plik przekracza limit 5 MB."
            else {
                values["photo"] = Base64.encodeToString(bytes, Base64.NO_WRAP)
                photoRevision++
                message = "Fotografia / skan został wczytany. Zapisz zmiany."
            }
        }.onFailure { message = "Nie udało się wczytać obrazu: ${it.message}" }
    }

    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót do katalogu dokumentów", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text(definition.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Dane, zasoby graficzne i wygląd tego dokumentu są niezależne od pozostałych dokumentów.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Dane dokumentu", fontWeight = FontWeight.Bold)
                options.forEach { (key, label) ->
                    OutlinedTextField(
                        value = values[key].orEmpty(),
                        onValueChange = { values[key] = it },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = label.length < 30,
                        maxLines = 3
                    )
                }
                if (definition.supportsPhoto) {
                    Text(definition.photoLabel, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { photoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                        modifier = Modifier.fillMaxWidth()) { Text("Wybierz zdjęcie / skan") }
                    if (values["photo"].orEmpty().isNotBlank()) {
                        val bitmap = remember(values["photo"], photoRevision) {
                            runCatching {
                                val bytes = Base64.decode(values["photo"], Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }.getOrNull()
                        }
                        bitmap?.let { Image(it.asImageBitmap(), contentDescription = definition.photoLabel,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)) }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pola widoczne w podglądzie", fontWeight = FontWeight.Bold)
                options.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = !appearance.value.visibleFieldsConfigured ||
                                appearance.value.visibleFields.isEmpty() || key in appearance.value.visibleFields,
                            onCheckedChange = { checked ->
                                val current = if (appearance.value.visibleFieldsConfigured && appearance.value.visibleFields.isNotEmpty())
                                    appearance.value.visibleFields else options.map { it.first }.toSet()
                                val updated = appearance.value.copy(
                                    visibleFields = if (checked) current + key else current - key,
                                    visibleFieldsConfigured = true
                                )
                                LegitimacyPreviewSettingsStore.save(context, documentKey, updated)
                                appearance.value = updated
                            }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        LegitimacyPreviewSettingsPanel(
            documentKey = documentKey,
            settings = appearance.value,
            onChange = { appearance.value = it },
            assetSelectionOnly = false,
            showCredentialPreview = definition.kind == DocumentPreviewKind.CREDENTIAL
        )

        Text(if (definition.kind == DocumentPreviewKind.CREDENTIAL) "Podgląd legitymacji / karty"
            else "Podgląd ewidencyjny — bez imitowania dokumentu urzędowego",
            fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = appearance.value.showCard,
                onCheckedChange = { checked ->
                    val updated = appearance.value.copy(showCard = checked)
                    LegitimacyPreviewSettingsStore.save(context, documentKey, updated)
                    appearance.value = updated
                }
            )
            Text("Wyświetlaj legitymację / podgląd dokumentu")
        }
        if (appearance.value.showCard) {
            if (definition.kind == DocumentPreviewKind.CREDENTIAL) {
                CredentialTemplatePreview(documentKey, appearance.value, values = values.toMap())
            } else {
                DocumentRecordGraphicPreview(definition, options, values.toMap(), appearance.value)
            }
        } else {
            Text("Podgląd legitymacji / dokumentu jest wyłączony.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }

        Button(onClick = {
            runCatching {
                val updated = DemoIdentityStore.load(context).toMutableMap()
                options.forEach { (key, _) -> updated[prefix + key] = values[key].orEmpty() }
                updated[prefix + "photo"] = values["photo"].orEmpty()
                DemoIdentityStore.save(context, updated)
                saveSubdocumentGraphicValues(context, documentKey, values.toMap())
                LegitimacyPreviewSettingsStore.save(context, documentKey, appearance.value)
            }.onSuccess { message = "Zapisano dane, fotografię i ustawienia graficzne dokumentu." }
                .onFailure { message = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz dokument i wygląd") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DocumentRecordGraphicPreview(
    definition: GraphicDocumentDefinition,
    options: List<Pair<String, String>>,
    values: Map<String, String>,
    settings: LegitimacyPreviewSettings
) {
    val context = LocalContext.current
    val crest = remember(settings.crestAssetId, definition.id, definition.title) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, definition.id, definition.title, "Herb")
            ?: DocumentBrandStore.imageForDocument(context, definition.id, definition.title, "Godło")
            ?: defaultDocumentEmblem(context, definition.title)
    }
    val flag = remember(settings.flagAssetId, definition.id, definition.title) {
        settings.flagAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, definition.id, definition.title, "Flaga")
            ?: defaultDocumentFlag(context, definition.title)
    }
    val background = remember(settings.backgroundAssetId, definition.id, definition.title) {
        settings.backgroundAssetId.takeIf { it.isNotBlank() }?.let { selected ->
            credentialBackgroundResourceId(context, selected)?.let { android.graphics.BitmapFactory.decodeResource(context.resources, it) }
                ?: DocumentBrandStore.preview(context, selected)
        } ?: DocumentBrandStore.imageForDocument(context, definition.id, definition.title, "Tło dokumentu")
            ?: defaultDocumentBackground(context, definition.title)
    }
    val logo = remember(settings.logoAssetId, definition.id, definition.title) {
        settings.logoAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, definition.id, definition.title, "Logo")
    }
    val sourceImage = remember(values["photo"]) {
        runCatching {
            val bytes = Base64.decode(values["photo"].orEmpty(), Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
    val textColor = runCatching {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(settings.fontColor.ifBlank { "#FF202A35" }))
    }.getOrDefault(MaterialTheme.colorScheme.onSurface)
    Card(
        Modifier.fillMaxWidth().aspectRatio(if (settings.orientation == "POZIOMA") 1.414f else 0.707f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (settings.showBackground && background != null) {
                Image(background.asImageBitmap(), contentDescription = "Tło dokumentu",
                    modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop, alpha = 0.24f)
            }
            Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (settings.showCrest && crest != null) Image(crest.asImageBitmap(), contentDescription = "Herb dokumentu",
                        modifier = Modifier.size(48.dp).graphicsLayer {
                            scaleX = settings.crestScale; scaleY = settings.crestScale
                            rotationZ = settings.crestRotation
                            translationX = settings.crestOffsetX * density; translationY = settings.crestOffsetY * density
                        })
                    if (settings.showFlag && flag != null) Image(flag.asImageBitmap(), contentDescription = "Flaga dokumentu",
                        modifier = Modifier.size(48.dp).graphicsLayer {
                            scaleX = settings.flagScale; scaleY = settings.flagScale
                            rotationZ = settings.flagRotation
                            translationX = settings.flagOffsetX * density; translationY = settings.flagOffsetY * density
                        })
                    if (settings.showLogo && logo != null) Image(logo.asImageBitmap(), contentDescription = "Logo wystawcy",
                        modifier = Modifier.size(48.dp).graphicsLayer {
                            scaleX = settings.graphicScale; scaleY = settings.graphicScale
                            rotationZ = settings.graphicRotation
                            translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density
                        })
                    Spacer(Modifier.weight(1f))
                    Text("WZÓR EWIDENCYJNY", style = MaterialTheme.typography.labelSmall, color = textColor)
                }
                Text(definition.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = textColor)
                Text("Podgląd danych ewidencyjnych. Oryginalny dokument lub plik źródłowy pozostaje odrębnym załącznikiem.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sourceImage != null) {
                    Image(sourceImage.asImageBitmap(), contentDescription = definition.photoLabel,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                }
                HorizontalDivider()
                Column(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    options.filter { !settings.visibleFieldsConfigured || settings.visibleFields.isEmpty() || it.first in settings.visibleFields }
                        .forEach { (key, label) ->
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = settings.labelScale; scaleY = settings.labelScale
                                        rotationZ = settings.labelRotation
                                        translationX = settings.labelOffsetX * density; translationY = settings.labelOffsetY * density
                                    })
                                Text(values[key].orEmpty().ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium,
                                    color = textColor, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = settings.valueScale; scaleY = settings.valueScale
                                        rotationZ = settings.valueRotation
                                        translationX = settings.valueOffsetX * density; translationY = settings.valueOffsetY * density
                                    })
                            }
                        }
                }
            }
        }
    }
}
