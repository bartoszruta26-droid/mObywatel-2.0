// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Local visual preview of user-entered identity data; it is not an official credential. */
@Composable
internal fun DocumentIdentityTemplate(
    title: String,
    fields: Map<String, String>,
    owner: String = "",
    photoOverride: ImageBitmap? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF173B63)))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("DOKUMENT PRYWATNY", color = Color(0xFF173B63), fontWeight = FontWeight.Bold)
                    Text(title, color = Color(0xFF33465B))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (photoOverride != null) {
                    androidx.compose.foundation.Image(photoOverride, contentDescription = "Zdjęcie w podglądzie dokumentu", modifier = Modifier.size(84.dp))
                } else {
                    Box(Modifier.size(84.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E7EF)))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(owner.ifBlank { fields.values.firstOrNull { it.isNotBlank() } ?: "Dane do uzupełnienia" },
                        color = Color(0xFF172B45), fontWeight = FontWeight.Bold)
                    fields.entries.filter { it.value.isNotBlank() }.take(4).forEach { (label, value) ->
                        Text("$label: $value", color = Color(0xFF43566B), maxLines = 2)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD5DEE8)))
            Text("Podgląd graficzny danych wprowadzonych przez użytkownika. Nie potwierdza tożsamości ani statusu urzędowego.",
                color = Color(0xFF6B7785))
        }
    }
}
