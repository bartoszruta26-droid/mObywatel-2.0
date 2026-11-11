// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class LegitimacyPreviewSettings(
    val showCard: Boolean = true,
    val showReverse: Boolean = false,
    val crestAssetId: String = "",
    val flagAssetId: String = "",
    val crestCaption: String = "",
    val captionPosition: String = "Pod herbem"
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
            crestCaption = p.getString("${key}_caption", "").orEmpty(),
            captionPosition = p.getString("${key}_caption_position", "Pod herbem").orEmpty()
        )
    }

    fun save(context: Context, key: String, value: LegitimacyPreviewSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("${key}_show_card", value.showCard)
            .putBoolean("${key}_show_reverse", value.showReverse)
            .putString("${key}_crest", value.crestAssetId)
            .putString("${key}_flag", value.flagAssetId)
            .putString("${key}_caption", value.crestCaption)
            .putString("${key}_caption_position", value.captionPosition)
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

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ustawienia podglądu legitymacji", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showCard, onCheckedChange = { update(settings.copy(showCard = it)) })
                Text("Wyświetlaj całą legitymację w podglądzie")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showReverse, onCheckedChange = { update(settings.copy(showReverse = it)) })
                Text("Wyświetlaj rewers (domyślnie ukryty)")
            }
            Text("Herb", fontWeight = FontWeight.SemiBold)
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
            if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text("Ustawienia są zapisywane lokalnie i pozostają zachowane po ponownym uruchomieniu aplikacji.", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
internal fun LegitimacyHeraldry(settings: LegitimacyPreviewSettings, documentKey: String) {
    val context = LocalContext.current
    val selectedCrest = remember(settings.crestAssetId) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
    }
    val selectedFlag = remember(settings.flagAssetId) {
        settings.flagAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
    }
    val assignedLogo = remember(documentKey) {
        DocumentBrandStore.imageForDocument(context, documentKey, "", "Logo")
    }
    val defaultCrest = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.institutions_eagle_emblem)
    }
    val defaultFlag = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.institutions_polish_flag)
    }
    val crest = selectedCrest ?: defaultCrest
    val flag = selectedFlag ?: defaultFlag

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (flag != null) {
            Image(flag.asImageBitmap(), contentDescription = "Flaga dokumentu",
                modifier = Modifier.size(width = 42.dp, height = 27.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
        if (assignedLogo != null) {
            Image(assignedLogo.asImageBitmap(), contentDescription = "Logo wystawcy",
                modifier = Modifier.size(36.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
        if (settings.captionPosition == "Po lewej" && settings.crestCaption.isNotBlank()) {
            Text(settings.crestCaption, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (settings.captionPosition == "Nad herbem" && settings.crestCaption.isNotBlank()) {
                Text(settings.crestCaption, style = MaterialTheme.typography.labelSmall)
            }
            if (crest != null) {
                Image(crest.asImageBitmap(), contentDescription = "Godło lub herb dokumentu",
                    modifier = Modifier.size(48.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            }
            if (settings.captionPosition == "Pod herbem" && settings.crestCaption.isNotBlank()) {
                Text(settings.crestCaption, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (settings.captionPosition == "Po prawej" && settings.crestCaption.isNotBlank()) {
            Text(settings.crestCaption, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun LegitimacyReverseSide(title: String, issuer: String, number: String, validity: String) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.586f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("REWERS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Text(title, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            Text("Wystawca: ${issuer.ifBlank { "Nie wskazano" }}")
            Text("Numer dokumentu: ${number.ifBlank { "—" }}")
            Text("Termin ważności: ${validity.ifBlank { "Nie określono" }}")
            Spacer(Modifier.weight(1f))
            Text("Strona informacyjna wzoru — dane wymagają potwierdzenia przez właściwego wystawcę.", style = MaterialTheme.typography.labelSmall)
        }
    }
}
