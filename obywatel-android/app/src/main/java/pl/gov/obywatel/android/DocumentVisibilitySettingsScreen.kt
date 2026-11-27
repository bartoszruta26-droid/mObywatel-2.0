// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun DocumentVisibilitySettingsScreen(
    documentPlacements: Map<String, String>,
    onDocumentPlacementsChange: (Map<String, String>) -> Unit,
    documentCardStyle: String,
    onDocumentCardStyleChange: (String) -> Unit,
    defaultDocumentIcon: String,
    onDefaultDocumentIconChange: (String) -> Unit,
    documentIconOverrides: Map<String, String>,
    onDocumentIconOverridesChange: (Map<String, String>) -> Unit,
    documentGroups: List<String>,
    documentGroupAssignments: Map<String, String>,
    onDocumentGroupAssignmentsChange: (Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    var defaultIconMenu by remember { mutableStateOf(false) }
    var expandedDocumentIcon by remember { mutableStateOf<String?>(null) }
    var expandedVisibilityGroups by remember { mutableStateOf(emptySet<String>()) }
    var expandedGroupDocument by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Widoczność dokumentów", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Dla każdego dokumentu wybierz miejsce wyświetlania. Ukrycie dokumentu nie usuwa jego danych ani załączników.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text("Wygląd kafelków w oknie Dokumenty", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Wybierz sposób prezentacji dokumentów.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Domyślna ikona dokumentów", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Ustawienie obejmuje wszystkie dokumenty. Ikona indywidualna wybrana niżej ma pierwszeństwo.", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(onClick = { defaultIconMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (defaultDocumentIcon == "automatic") "Automatycznie — ikona przypisana do dokumentu"
                    else documentIconChoices.firstOrNull { it.first == defaultDocumentIcon }?.second ?: "Wybierz ikonę")
            }
            DropdownMenu(expanded = defaultIconMenu, onDismissRequest = { defaultIconMenu = false }) {
                DropdownMenuItem(text = { Text("Automatycznie — ikona przypisana do dokumentu") }, onClick = {
                    onDefaultDocumentIconChange("automatic"); defaultIconMenu = false
                })
                documentIconChoices.forEach { (resource, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = {
                        onDefaultDocumentIconChange(resource); defaultIconMenu = false
                    })
                }
            }
        }
        listOf("large" to "Duże kafelki", "small" to "Małe kafelki", "list" to "Kafelki listy").forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (documentCardStyle == value) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable { onDocumentCardStyleChange(value) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = documentCardStyle == value, onClick = { onDocumentCardStyleChange(value) })
                Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }
        val groupedVisibilityDocuments = documentDisplayOptions
            .distinctBy { it.first }
            .groupBy { (id, _, _) -> resolveDocumentGroup(id, documentGroupAssignments) }
        val visibilityGroupOrder = documentGroups
        visibilityGroupOrder.forEach { groupTitle ->
            val groupDocuments = groupedVisibilityDocuments[groupTitle].orEmpty()
            if (groupDocuments.isEmpty()) return@forEach
            val expanded = groupTitle in expandedVisibilityGroups
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .clickable {
                                expandedVisibilityGroups = if (expanded) expandedVisibilityGroups - groupTitle
                                else expandedVisibilityGroups + groupTitle
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(documentGroupIconResource(groupTitle)),
                            contentDescription = "Ikona grupy: $groupTitle",
                            modifier = Modifier.size(34.dp)
                        )
                        Text(groupTitle, color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${groupDocuments.size} dokumentów", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium)
                        Text(if (expanded) "⌃" else "⌄", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge)
                    }
                    if (expanded) {
                        groupDocuments.forEach { (id, title, iconRes) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(resolveDocumentIconResource(
                                        LocalContext.current, id, defaultDocumentIcon, documentIconOverrides
                                    )),
                                    contentDescription = "Ikona: $title",
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Box {
                                    TextButton(onClick = {
                                        expandedGroupDocument = if (expandedGroupDocument == id) null else id
                                    }) {
                                        Text(
                                            documentGroupAssignments[id] ?: resolveDocumentGroup(id, documentGroupAssignments),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedGroupDocument == id,
                                        onDismissRequest = { expandedGroupDocument = null }
                                    ) {
                                        documentGroups.forEach { group ->
                                            DropdownMenuItem(text = { Text(group) }, onClick = {
                                                onDocumentGroupAssignmentsChange(documentGroupAssignments + (id to group))
                                                expandedGroupDocument = null
                                            })
                                        }
                                    }
                                }
                                Box {
                                    TextButton(
                                        onClick = { expandedDocumentIcon = if (expandedDocumentIcon == id) null else id },
                                        modifier = Modifier.testTag("document-icon-selector-$id")
                                    ) {
                                        val selectedIconLabel = documentIconChoices.firstOrNull {
                                            it.first == documentIconOverrides[id]
                                        }?.second
                                        Text(selectedIconLabel ?: "Ikona", style = MaterialTheme.typography.labelSmall)
                                    }
                                    DropdownMenu(
                                        expanded = expandedDocumentIcon == id,
                                        onDismissRequest = { expandedDocumentIcon = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Automatycznie — ikona przypisana do dokumentu") },
                                            onClick = {
                                                onDocumentIconOverridesChange(documentIconOverrides - id)
                                                expandedDocumentIcon = null
                                            }
                                        )
                                        documentIconChoices.forEach { (resource, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    onDocumentIconOverridesChange(documentIconOverrides + (id to resource))
                                                    expandedDocumentIcon = null
                                                }
                                            )
                                        }
                                    }
                                }
                                listOf("dashboard" to "Dokument", "other" to "Inny dokument", "hidden" to "Ukryj").forEach { (value, label) ->
                                    Row(
                                        modifier = Modifier.testTag("document-placement-$id-$value")
                                            .clickable { onDocumentPlacementsChange(documentPlacements + (id to value)) },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        RadioButton(
                                            selected = documentPlacements[id] == value,
                                            onClick = { onDocumentPlacementsChange(documentPlacements + (id to value)) },
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

