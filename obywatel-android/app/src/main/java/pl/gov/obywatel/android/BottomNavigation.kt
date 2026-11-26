// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun BottomNavigation(selectedTab: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Dokumenty", 0, R.drawable.nav_documents),
        Triple("Usługi", 2, R.drawable.nav_services),
        Triple("Kod QR", 3, R.drawable.nav_scan),
        Triple("Szukaj", 1, R.drawable.nav_search),
        Triple("Więcej", 4, R.drawable.nav_more)
    )
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.6.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, index, iconRes) ->
            val selected = selectedTab == index
            val selectedIcon = when (index) {
                0 -> R.drawable.nav_documents_selected
                2 -> R.drawable.nav_services_selected
                3 -> R.drawable.nav_scan_selected
                1 -> R.drawable.nav_search_selected
                else -> R.drawable.nav_more_selected
            }
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(index) }.padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.88f)
                        .height(43.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) Color(0x33218FEA) else Color.Transparent
                        )
                        .then(
                            if (selected) Modifier.border(
                                width = 2.dp,
                                color = Color(0xFF168FEA),
                                shape = RoundedCornerShape(24.dp)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (selected) selectedIcon else iconRes),
                        contentDescription = label,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(27.dp)
                    )
                }
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
