// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Local, editable identity-card preview. It is not an official credential. */
@Composable
internal fun DocumentIdentityTemplate(
    title: String,
    fields: Map<String, String>,
    owner: String = "",
    photoOverride: ImageBitmap? = null
) {
    val context = LocalContext.current
    val normalized = title.lowercase()
    val documentType = when {
        normalized.contains("paszport") -> "passport"
        normalized.contains("rodow") || normalized.contains("rodu") || normalized.contains("rodzinn") -> "family"
        normalized.contains("poselsk") || normalized.contains("parlament") -> "parliament"
        normalized.contains("kursant") || normalized.contains("uczestnika kurs") || normalized.contains("kursu") -> "training"
        normalized.contains("student") || normalized.contains("uczn") || normalized.contains("szkoln") -> "student"
        normalized.contains("kościel") || normalized.contains("kosciel") || normalized.contains("misjon") -> "church"
        normalized.contains("zawod") || normalized.contains("kwalifikac") -> "professional"
        normalized.contains("fundac") || normalized.contains("stowarz") || normalized.contains("podmiot") || normalized.contains("organizac") -> "entity"
        normalized.contains("tożsamo") || normalized.contains("tozsamos") || normalized.contains("dowód") || normalized.contains("dowod") -> "identity"
        else -> "official"
    }
    var revision by remember(title, owner) { mutableIntStateOf(0) }
    var showReverse by remember(title) { mutableStateOf(false) }
    val assets = remember(revision) { DocumentBrandStore.list(context) }
    val assignments = remember(revision) { DocumentBrandStore.assignments(context) }
    val emblemKinds = listOf("Godło", "Herb", "Logo")
    val selectedFlag = assignments.lastOrNull { it.documentType == documentType && it.kind == "Flaga" }?.assetId.orEmpty()
    val selectedEmblem = assignments.lastOrNull { it.documentType == documentType && it.kind in emblemKinds }?.assetId.orEmpty()
    val selectedBackground = assignments.lastOrNull { it.documentType == documentType && it.kind == "Tło dokumentu" }?.assetId.orEmpty()

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Awers i rewers dokumentu")
                Text("Układ: fotografia, flaga i godło po lewej; dane po prawej.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            Switch(checked = showReverse, onCheckedChange = { showReverse = it })
        }
        Text(if (showReverse) "Rewers włączony" else "Rewers wyłączony", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
        DocumentAssetPicker(
            label = "Flaga",
            assets = assets.filter { it.kind == "Flaga" },
            selectedId = selectedFlag,
            onSelected = { asset ->
                if (asset == null) DocumentBrandStore.unassign(context, documentType, "Flaga")
                else DocumentBrandStore.assign(context, documentType, "Flaga", asset.id)
                revision++
            }
        )
        DocumentAssetPicker(
            label = "Godło / herb / logo",
            assets = assets.filter { it.kind in emblemKinds },
            selectedId = selectedEmblem,
            onSelected = { asset ->
                emblemKinds.forEach { DocumentBrandStore.unassign(context, documentType, it) }
                if (asset != null) DocumentBrandStore.assign(context, documentType, asset.kind, asset.id)
                revision++
            }
        )
        DocumentAssetPicker(
            label = "Tło legitymacji",
            assets = assets.filter { it.kind == "Tło dokumentu" },
            selectedId = selectedBackground,
            onSelected = { asset ->
                if (asset == null) DocumentBrandStore.unassign(context, documentType, "Tło dokumentu")
                else DocumentBrandStore.assign(context, documentType, "Tło dokumentu", asset.id)
                revision++
            }
        )
        DocumentIdentityTemplate(
            title = title,
            values = fields,
            owner = owner,
            photoOverride = photoOverride?.asAndroidBitmap(),
            documentType = documentType,
            showReverse = showReverse,
            assetRevision = revision
        )
    }
}

@Composable
private fun DocumentAssetPicker(
    label: String,
    assets: List<BrandAsset>,
    selectedId: String,
    onSelected: (BrandAsset?) -> Unit
) {
    var expanded by remember(label) { mutableStateOf(false) }
    val selected = assets.firstOrNull { it.id == selectedId }
    Column(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selected == null) "$label — domyślny zasób" else "$label — ${selected.label}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Domyślny zasób") },
                onClick = { expanded = false; onSelected(null) }
            )
            assets.forEach { asset ->
                DropdownMenuItem(
                    text = { Text(asset.label + if (asset.file.startsWith("builtin:")) " · SVG" else "") },
                    onClick = { expanded = false; onSelected(asset) }
                )
            }
        }
        if (assets.isEmpty()) {
            Text("Brak własnych zasobów tej kategorii. Domyślna grafika pozostaje dostępna.", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
        }
    }
}
