// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun DocumentEditorCatalogScreen(
    documentGroups: List<String>,
    documentGroupAssignments: Map<String, String>,
    onOpenDocument: (String) -> Unit,
    onBack: () -> Unit
) {
    var expandedGroups by remember { mutableStateOf(emptySet<String>()) }
    val documents = documentDisplayOptions.distinctBy { it.first }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Edytuj dane dokumentów", style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Dokumenty są uporządkowane według aktualnych przypisań grup. Podgląd i edycja korzystają z tego samego identyfikatora dokumentu.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        documentGroups.forEach { group ->
            val entries = documents.filter { (id, _, _) -> (resolveDocumentGroup(id, documentGroupAssignments)) == group }
                .sortedBy { it.second.lowercase(java.util.Locale("pl", "PL")) }
            if (entries.isNotEmpty()) {
                val expanded = group in expandedGroups
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .clickable { expandedGroups = if (expanded) expandedGroups - group else expandedGroups + group }
                            .padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(documentGroupIconResource(group)),
                                contentDescription = "Ikona grupy: $group",
                                modifier = Modifier.size(34.dp)
                            )
                            Text(group, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("${entries.size} dokumentów", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                            Text(if (expanded) "⌃" else "⌄", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        }
                        if (expanded) entries.forEach { (id, title, iconRes) ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onOpenDocument(id) }
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(32.dp))
                                Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                TextButton(onClick = { onOpenDocument(id) }) { Text("Edytuj") }
                            }
                        }
                    }
                }
            }
        }
        Text("Liczba dokumentów: ${documents.size}. Każdy dokument jest przypisany do jednej grupy.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
    }
}

