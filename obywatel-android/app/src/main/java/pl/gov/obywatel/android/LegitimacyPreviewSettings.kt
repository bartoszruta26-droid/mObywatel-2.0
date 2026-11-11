// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class CredentialFieldOption(val key: String, val label: String)

internal fun credentialFieldOptions(documentKey: String): List<Pair<String, String>> = when (documentKey) {
    "parliament" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "mpCardNumber" to "Numer legitymacji", "mpCardIssueDate" to "Data wydania", "mpTerm" to "Numer kadencji", "mpIssuer" to "Wystawca")
    "training", "professional", "professional_medical", "professional_education", "professional_therapeutic", "professional_other" ->
        listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "trainingInstitution" to "Instytucja", "trainingCourseName" to "Kurs / kwalifikacja", "trainingCardNumber" to "Numer legitymacji", "trainingIssueDate" to "Data wydania", "trainingValidUntil" to "Termin ważności")
    "student" -> listOf("studentNames" to "Imię i nazwisko", "studentUniversity" to "Uczelnia", "studentProgramme" to "Kierunek", "studentCardNumber" to "Numer legitymacji", "studentIssueDate" to "Data wydania", "studentValidity" to "Termin ważności")
    "identity" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia", "pesel" to "PESEL", "idNumber" to "Numer dokumentu", "idIssueDate" to "Data wydania", "idExpiryDate" to "Data ważności")
    "passport" -> listOf("passportHolderNames" to "Imię i nazwisko", "passportNumber" to "Numer paszportu", "passportCitizenship" to "Obywatelstwo", "birthDate" to "Data urodzenia", "passportIssueDate" to "Data wydania", "passportExpiryDate" to "Data ważności")
    "family" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "familyName" to "Nazwa Rodu", "familyRole" to "Miejsce w Rodzie", "familyDocNumber" to "Numer dokumentu", "familyIssueDate" to "Data sporządzenia", "familyExpiryDate" to "Data aktualizacji")
    "disability" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "disabilityDecisionType" to "Rodzaj orzeczenia", "disabilityCaseNumber" to "Numer sprawy", "disabilityValidity" to "Okres obowiązywania", "disabilityIssuerName" to "Wystawca")
    "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister", "church_missionary" ->
        listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "churchRole" to "Posługa / godność", "churchDocumentNumber" to "Numer dokumentu", "churchIssueDate" to "Data wydania", "churchValidity" to "Okres obowiązywania", "churchIssuer" to "Wystawca")
    "organization_foundation", "organization_association", "organization_church", "organization_company", "entity" ->
        listOf("entityName" to "Nazwa podmiotu", "entityKrs" to "Numer KRS", "entityNip" to "NIP", "entityRegon" to "REGON", "entityRepresentative" to "Reprezentant")
    "loyalty" -> listOf("loyaltyName" to "Nazwa programu", "loyaltyNumber" to "Numer karty", "loyaltyHolder" to "Posiadacz")
    "bank_accounts" -> listOf("bankName" to "Bank", "accountName" to "Nazwa rachunku", "accountNumber" to "Numer rachunku", "accountOwner" to "Właściciel")
    "children" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia", "childRelationship" to "Relacja", "guardianName" to "Opiekun")
    "vaccines" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "vaccineName" to "Preparat", "vaccineDate" to "Data szczepienia", "vaccineDose" to "Dawka")
    "civil" -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "civilDocumentNumber" to "Numer dokumentu", "civilIssueDate" to "Data sporządzenia", "civilIssuer" to "Wystawca")
    else -> listOf("firstNames" to "Imiona", "lastName" to "Nazwisko", "documentNumber" to "Numer dokumentu", "issueDate" to "Data wydania", "expiryDate" to "Termin ważności", "issuer" to "Wystawca")
}

internal fun isCredentialValidByExpiry(rawExpiry: String): Boolean {
    val value = rawExpiry.trim()
    if (value.isEmpty()) return true
    val formats = listOf(
        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
    )
    val parsed = formats.firstNotNullOfOrNull { formatter ->
        runCatching { java.time.LocalDate.parse(value, formatter) }.getOrNull()
    }
    return parsed == null || !parsed.isBefore(java.time.LocalDate.now())
}

internal data class LegitimacyPreviewSettings(
    val showCard: Boolean = true,
    val showReverse: Boolean = false,
    val crestAssetId: String = "",
    val flagAssetId: String = "",
    val backgroundAssetId: String = "",
    val crestCaption: String = "",
    val captionPosition: String = "Pod herbem",
    val orientation: String = "PIONOWA",
    val visibleFields: Set<String> = emptySet()
)

private object LegitimacyPreviewSettingsStore {
    private const val PREFS = "legitimacy_preview_preferences"

    fun load(context: Context, key: String): LegitimacyPreviewSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return LegitimacyPreviewSettings(
            showCard = p.getBoolean("${key}_show_card", true),
            showReverse = p.getBoolean("${key}_show_reverse", false),
            crestAssetId = p.getString("${key}_crest", "").orEmpty(),
            flagAssetId = p.getString("${key}_flag", "").orEmpty(),
            backgroundAssetId = p.getString("${key}_background", "").orEmpty(),
            crestCaption = p.getString("${key}_caption", "").orEmpty(),
            captionPosition = p.getString("${key}_caption_position", "Pod herbem").orEmpty(),
            orientation = p.getString("${key}_orientation", "PIONOWA").orEmpty(),
            visibleFields = p.getStringSet("${key}_visible_fields", emptySet())?.toSet().orEmpty()
        )
    }

    fun save(context: Context, key: String, value: LegitimacyPreviewSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("${key}_show_card", value.showCard)
            .putBoolean("${key}_show_reverse", value.showReverse)
            .putString("${key}_crest", value.crestAssetId)
            .putString("${key}_flag", value.flagAssetId)
            .putString("${key}_background", value.backgroundAssetId)
            .putString("${key}_caption", value.crestCaption)
            .putString("${key}_caption_position", value.captionPosition)
            .putString("${key}_orientation", value.orientation)
            .putStringSet("${key}_visible_fields", value.visibleFields)
            .apply()
    }
}

@Composable
internal fun rememberLegitimacyPreviewSettings(documentKey: String): MutableState<LegitimacyPreviewSettings> {
    val context = LocalContext.current
    val state = remember(documentKey) { mutableStateOf(LegitimacyPreviewSettingsStore.load(context, documentKey)) }
    return state
}

@Composable
internal fun LegitimacyPreviewSettingsPanel(
    documentKey: String,
    settings: LegitimacyPreviewSettings,
    onChange: (LegitimacyPreviewSettings) -> Unit
) {
    val context = LocalContext.current
    var assets by remember(documentKey) { mutableStateOf(DocumentBrandStore.list(context)) }
    var crestMenu by remember { mutableStateOf(false) }
    var flagMenu by remember { mutableStateOf(false) }
    var backgroundMenu by remember { mutableStateOf(false) }
    var positionMenu by remember { mutableStateOf(false) }
    var importKind by remember { mutableStateOf("Herb") }
    var message by remember { mutableStateOf("") }

    fun update(value: LegitimacyPreviewSettings) {
        LegitimacyPreviewSettingsStore.save(context, documentKey, value)
        onChange(value)
    }

    val crestPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Herb", "Herb legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Herb" }
            if (imported != null) update(settings.copy(crestAssetId = imported.id))
            message = "Wczytano herb. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać herbu: ${it.message ?: "błąd"}" }
    }
    val flagPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Flaga", "Flaga legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Flaga" }
            if (imported != null) update(settings.copy(flagAssetId = imported.id))
            message = "Wczytano flagę. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać flagi: ${it.message ?: "błąd"}" }
    }

    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Tło dokumentu", "Tło legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Tło dokumentu" }
            if (imported != null) update(settings.copy(backgroundAssetId = imported.id))
            message = "Wczytano tło legitymacji. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać tła: ${it.message ?: "błąd"}" }
    }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ustawienia wyglądu dokumentu", fontWeight = FontWeight.Bold)
            Text("Orientacja", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.orientation == "PIONOWA", onClick = { update(settings.copy(orientation = "PIONOWA")) })
                Text("Pionowa")
                Spacer(Modifier.width(12.dp))
                RadioButton(selected = settings.orientation == "POZIOMA", onClick = { update(settings.copy(orientation = "POZIOMA")) })
                Text("Pozioma")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showCard, onCheckedChange = { update(settings.copy(showCard = it)) })
                Text("Wyświetlaj całą legitymację w podglądzie")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showReverse, onCheckedChange = { update(settings.copy(showReverse = it)) })
                Text("Wyświetlaj rewers (domyślnie ukryty)")
            }
            Text("Pola na legitymacji", fontWeight = FontWeight.SemiBold)
            val fieldOptions = credentialFieldOptions(documentKey)
            fieldOptions.forEach { (fieldKey, fieldLabel) ->
                val checked = settings.visibleFields.isEmpty() || fieldKey in settings.visibleFields
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { selected ->
                        val current = if (settings.visibleFields.isEmpty()) fieldOptions.map { it.first }.toSet() else settings.visibleFields
                        val next = if (selected) current + fieldKey else current - fieldKey
                        update(settings.copy(visibleFields = next))
                    })
                    Text(fieldLabel, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Herb / godło / logo", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { crestMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.crestAssetId }?.label ?: "Wybierz herb z katalogu")
                }
                DropdownMenu(expanded = crestMenu, onDismissRequest = { crestMenu = false }) {
                    DropdownMenuItem(text = { Text("Brak herbu") }, onClick = { update(settings.copy(crestAssetId = "")); crestMenu = false })
                    assets.filter { it.kind == "Herb" || it.kind == "Godło" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(crestAssetId = asset.id)); crestMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { crestPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własny herb")
            }
            Text("Napis przy herbie", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = settings.crestCaption,
                onValueChange = { update(settings.copy(crestCaption = it)) },
                label = { Text("Własny napis (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            Box {
                OutlinedButton(onClick = { positionMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Położenie napisu: ${settings.captionPosition}")
                }
                DropdownMenu(expanded = positionMenu, onDismissRequest = { positionMenu = false }) {
                    listOf("Nad herbem", "Pod herbem", "Po lewej", "Po prawej").forEach { position ->
                        DropdownMenuItem(text = { Text(position) }, onClick = {
                            update(settings.copy(captionPosition = position)); positionMenu = false
                        })
                    }
                }
            }
            Text("Flaga", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { flagMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.flagAssetId }?.label ?: "Wybierz flagę z katalogu")
                }
                DropdownMenu(expanded = flagMenu, onDismissRequest = { flagMenu = false }) {
                    DropdownMenuItem(text = { Text("Brak własnej flagi") }, onClick = { update(settings.copy(flagAssetId = "")); flagMenu = false })
                    assets.filter { it.kind == "Flaga" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(flagAssetId = asset.id)); flagMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { flagPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własną flagę")
            }
            Text("Tło legitymacji", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { backgroundMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.backgroundAssetId }?.label ?: "Wybierz tło z katalogu")
                }
                DropdownMenu(expanded = backgroundMenu, onDismissRequest = { backgroundMenu = false }) {
                    DropdownMenuItem(text = { Text("Domyślne tło") }, onClick = { update(settings.copy(backgroundAssetId = "")); backgroundMenu = false })
                    assets.filter { it.kind == "Tło dokumentu" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(backgroundAssetId = asset.id)); backgroundMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { backgroundPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własne tło")
            }
            if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            CredentialTemplatePreview(documentKey, settings)
            Text("Ustawienia są zapisywane lokalnie i pozostają zachowane po ponownym uruchomieniu aplikacji.", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
internal fun LegitimacyHeraldry(
    settings: LegitimacyPreviewSettings,
    documentKey: String,
    vertical: Boolean = false,
    title: String = "DOKUMENT",
    subtitle: String = "DANE POSIADACZA"
) {
    val context = LocalContext.current
    val assignedCrest = remember(documentKey) {
        DocumentBrandStore.imageForDocument(context, documentKey, "", "Herb")
            ?: DocumentBrandStore.imageForDocument(context, documentKey, "", "Godło")
    }
    val assignedFlag = remember(documentKey) {
        DocumentBrandStore.imageForDocument(context, documentKey, "", "Flaga")
    }
    val selectedCrest = remember(settings.crestAssetId, assignedCrest) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } ?: assignedCrest
    }
    val selectedFlag = remember(settings.flagAssetId, assignedFlag) {
        settings.flagAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } ?: assignedFlag
    }
    val assignedLogo = remember(documentKey) {
        DocumentBrandStore.imageForDocument(context, documentKey, "", "Logo")
    }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (selectedFlag != null) {
            Image(selectedFlag.asImageBitmap(), contentDescription = "Flaga dokumentu",
                modifier = Modifier.size(width = 46.dp, height = 25.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        } else {
            Image(painterResource(R.drawable.institutions_polish_flag), contentDescription = "Flaga dokumentu",
                modifier = Modifier.size(width = 46.dp, height = 25.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCrest != null) {
                Image(selectedCrest.asImageBitmap(), contentDescription = "Godło lub herb dokumentu",
                    modifier = Modifier.size(42.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            } else {
                Image(painterResource(R.drawable.institutions_eagle_emblem), contentDescription = "Godło lub herb dokumentu",
                    modifier = Modifier.size(42.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            }
            Column(Modifier.weight(1f).padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall,
                    maxLines = 2)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                if (assignedLogo != null) {
                    Image(assignedLogo.asImageBitmap(), contentDescription = "Logo wystawcy",
                        modifier = Modifier.size(width = 54.dp, height = 24.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                }
                if (settings.crestCaption.isNotBlank()) {
                    Text(settings.crestCaption, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                }
            }
        }
    }
}

@Composable
internal fun CredentialTemplatePreview(documentKey: String, settings: LegitimacyPreviewSettings) {
    val context = LocalContext.current
    val title = documentBrandTargets.firstOrNull { it.first == documentKey }?.second ?: "Dokument"
    val options = credentialFieldOptions(documentKey)
    val selected = if (settings.visibleFields.isEmpty()) options else options.filter { it.first in settings.visibleFields }
    val background = remember(settings.backgroundAssetId, documentKey) {
        settings.backgroundAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, documentKey, "", "Tło dokumentu")
            ?: BitmapFactory.decodeResource(context.resources, R.drawable.tlo_legitymacji_szare)
    }
    Card(
        Modifier.fillMaxWidth().aspectRatio(if (settings.orientation == "POZIOMA") 1.586f else 0.70f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (background != null) Image(background.asImageBitmap(), contentDescription = "Tło dokumentu",
                modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop, alpha = 0.42f)
            Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                LegitimacyHeraldry(settings, documentKey, vertical = true,
                    title = title.uppercase(), subtitle = "DOKUMENT")
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.width(54.dp).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("FOTO", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        selected.take(if (settings.orientation == "POZIOMA") 5 else 7).forEach { (key, label) ->
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("—", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LegitimacyReverseSide(title: String, issuer: String, number: String, validity: String) {
    val payload = remember(title, issuer, number, validity) {
        "OWD-DEMO|${title.take(80)}|${number.take(48)}|${validity.take(32)}"
    }
    val qrBitmap = remember(payload) {
        runCatching {
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 240, 240, hints)
            Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }.getOrNull()
    }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.70f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("REWERS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Text(title, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            Text("Wystawca: ${issuer.ifBlank { "Nie wskazano" }}")
            Text("Numer dokumentu: ${number.ifBlank { "—" }}")
            Text("Termin ważności: ${validity.ifBlank { "Nie określono" }}")
            Spacer(Modifier.weight(1f))
            if (qrBitmap != null) {
                Image(qrBitmap.asImageBitmap(), contentDescription = "Demonstracyjny kod QR rewersu",
                    modifier = Modifier.size(112.dp).align(Alignment.CenterHorizontally))
            }
        }
    }
}
