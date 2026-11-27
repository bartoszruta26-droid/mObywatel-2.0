// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal fun documentIconFor(documentId: String): ImageVector = when (documentId) {
    "identity" -> Icons.Filled.CreditCard
    "passport" -> Icons.Filled.Public
    "registry", "parliament", "church", "business", "bank_accounts" -> Icons.Filled.AccountBalance
    "family" -> Icons.Filled.Fingerprint
    "training", "student" -> Icons.Filled.Badge
    "disability", "vaccines" -> Icons.Filled.HealthAndSafety
    "civil", "signed_pdf" -> Icons.Filled.Description
    "loyalty_cards" -> Icons.Filled.CreditCard
    "branding" -> Icons.Filled.Info
    else -> Icons.Filled.Description
}

internal fun categoryIconFor(category: String): Int? = when (category) {
    "Zdrowie" -> R.drawable.mob_health
    "Rodzina" -> R.drawable.family
    "Edukacja" -> R.drawable.education
    "Praca" -> R.drawable.work
    "Finanse i podatki" -> R.drawable.mob_payments
    "Transport", "Kierowca i pojazdy", "Podróże" -> R.drawable.mob_transport
    "Nieruchomości" -> R.drawable.real_estate
    "Środowisko" -> R.drawable.mob_environment
    "Bezpieczeństwo" -> R.drawable.mob_cyber_safety
    "Prawo i obywatel", "Sprawy urzędowe" -> R.drawable.law_citizenship
    "Kultura i dziedzictwo" -> R.drawable.culture_heritage
    "Sport" -> R.drawable.sport
    "Organizacje społeczne" -> R.drawable.social_organizations
    "Kościół i wspólnoty" -> R.drawable.church_communities
    else -> null
}

@Composable
internal fun ServiceCard(
    item: ServiceItem,
    tileStyle: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val compact = tileStyle == "small"
    val list = tileStyle == "list"
    val cardModifier = when {
        list -> Modifier.fillMaxWidth().height(64.dp)
        compact -> Modifier.fillMaxWidth().height(76.dp)
        else -> Modifier.fillMaxWidth().height(92.dp)
    }
    Card(
        modifier = cardModifier.clickable(onClick = onClick)
            .testTag("service-card-${item.title}"),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxSize().padding(if (list) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(if (list || compact) 36.dp else 44.dp)
                    .clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(painter = painterResource(item.icon), contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                } else {
                    item.fallbackIcon?.let {
                        Icon(it, contentDescription = item.title, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp))
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(item.title, color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!list) {
                    Spacer(Modifier.height(3.dp))
                    Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(if (isFavorite) "★" else "☆",
                color = if (isFavorite) Color(0xFFB7791F) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable(onClick = onFavorite).padding(3.dp))
        }
    }
}
