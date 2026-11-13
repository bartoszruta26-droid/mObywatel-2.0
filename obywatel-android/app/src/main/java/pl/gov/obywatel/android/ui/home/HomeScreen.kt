// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.R
import pl.gov.obywatel.android.ui.theme.Blue

private data class ServiceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val tint: Color = Blue
)

private val services = listOf(
    ServiceItem("documents", "Dokumenty", "Import i podgląd plików", R.drawable.nav_documents),
    ServiceItem("identity", "Zarządzanie dokumentami", "Podmioty, logo, herby, pieczęcie i PDF", R.drawable.nav_documents),
    ServiceItem("registry", "Rejestr dokumentów", "Lokalny wykaz i kody QR", R.drawable.service_ticket),
    ServiceItem("verify", "Weryfikacja QR", "Odczyt identyfikatora rejestru", R.drawable.nav_scan),
    ServiceItem("profile", "Moje dane", "Dane profilu aplikacji", R.drawable.service_profile),
    ServiceItem("security", "Bezpieczeństwo", "Ustawienia ochrony danych", R.drawable.service_pesel_lock),
    ServiceItem("payments", "Płatności", "Moduł informacyjny", R.drawable.service_ticket),
    ServiceItem("health", "Zdrowie", "Moduł informacyjny", R.drawable.service_prescription),
    ServiceItem("more", "Więcej", "Informacje o aplikacji", R.drawable.nav_more)
)

@Composable
fun HomeScreen(onOpenSection: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().semantics { paneTitle = "Ekran główny" }) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp)
                        .background(Blue, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.nav_documents_selected),
                        contentDescription = "Znak graficzny niezależnego prototypu",
                        tint = Color.White,
                        modifier = Modifier.size(29.dp)
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        "Portfel dokumentów",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        "Niezależny prototyp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                IconButton(onClick = { onOpenSection("more") }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Więcej informacji")
                }
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Niezależna aplikacja",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "To prototyp interfejsu, a nie oficjalna aplikacja administracji publicznej. Dokumenty i kody QR mają charakter lokalny.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    "Usługi i funkcje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services, key = { it.id }) { service ->
                        ServiceCard(service) { onOpenSection(service.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(item: ServiceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val watermark = when (item.id) {
                "documents" -> R.drawable.tile_watermark_digital
                "identity" -> R.drawable.tile_watermark_identity
                "registry" -> R.drawable.tile_watermark_civic
                "verify" -> R.drawable.tile_watermark_security
                "profile" -> R.drawable.tile_watermark_family
                "security" -> R.drawable.tile_watermark_security
                "payments" -> R.drawable.tile_watermark_finance
                "health" -> R.drawable.tile_watermark_medical
                else -> R.drawable.tile_watermark_fractal_triangle
            }
            Image(
                painter = painterResource(watermark),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .size(116.dp)
                    .graphicsLayer { alpha = 0.10f }
            )
            Column(
                Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = item.tint,
                    modifier = Modifier.size(27.dp)
                )
            }
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            }
        }
    }
}
