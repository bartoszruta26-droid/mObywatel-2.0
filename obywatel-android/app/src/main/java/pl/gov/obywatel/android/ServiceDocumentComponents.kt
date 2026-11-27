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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag

@Composable
internal fun Header(
    onOpenAssistant: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark()
        Text(
            "Dokumenty",
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onOpenAssistant) {
            Icon(painterResource(R.drawable.ic_header_robot_head), contentDescription = "Asystent",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onOpenMessages) {
            Icon(painterResource(R.drawable.ic_header_mail), contentDescription = "Wiadomości",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
        }
        Box(
            modifier = Modifier.size(36.dp).clickable(onClick = onOpenNotifications),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_header_bell), contentDescription = "Powiadomienia",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(29.dp))
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(10.dp)
                    .clip(CircleShape).background(Color(0xFFFF3028))
            )
        }
    }
}

@Composable
internal fun IdentityDashboardCard(
    onOpen: () -> Unit,
    tileStyle: String = "large",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val cardHeight = when (tileStyle) {
        "small" -> 92.dp
        "list" -> 68.dp
        else -> 218.dp
    }
    Card(
        modifier = modifier.height(cardHeight)
            .clickable(onClick = onOpen)
            .testTag("identity-dashboard-card-$tileStyle"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF078FE9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9E8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            DocumentTileArtwork("identity", Modifier.fillMaxSize())
            Text(
                "mDowód",
                modifier = Modifier.align(Alignment.TopStart).padding(
                    start = if (tileStyle == "small") 10.dp else 20.dp,
                    top = if (tileStyle == "small") 10.dp else 18.dp
                ),
                color = Color.White,
                style = if (tileStyle == "small") MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Orzeł pozostaje czytelny; znacznik nachodzi na jego dolną prawą część przy nodze.
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(
                        top = if (tileStyle == "small") 8.dp else 12.dp,
                        end = if (tileStyle == "small") 8.dp else 12.dp
                    )
                    .size(if (tileStyle == "small") 22.dp else if (tileStyle == "list") 28.dp else 32.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .size(if (tileStyle == "small") 18.dp else if (tileStyle == "list") 24.dp else 28.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x557AA8C6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.a_clean_vector_style_emblem_on_a_transparent_backg),
                        contentDescription = "Godło Polski — czysty znak wektorowy na przezroczystym tle",
                        modifier = Modifier.size(if (tileStyle == "small") 13.dp else 20.dp),
                        contentScale = ContentScale.Fit
                    )
                }

            }
        }
    }
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
