// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.Blue
import pl.gov.obywatel.android.ui.theme.BlueLight
import pl.gov.obywatel.android.ui.theme.Border
import pl.gov.obywatel.android.ui.theme.Ink
import pl.gov.obywatel.android.ui.theme.Muted
import pl.gov.obywatel.android.ui.theme.Navy
import pl.gov.obywatel.android.ui.theme.ObywatelTheme
import pl.gov.obywatel.android.ui.theme.Surface as AppSurface
import pl.gov.obywatel.android.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ObywatelTheme { HomeScreen() } }
    }
}

private data class ServiceItem(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val tint: Color = Blue
)

private val services = listOf(
    ServiceItem("Dokumenty", "Portfel demonstracyjny", R.drawable.mob_abroad),
    ServiceItem("Moje dane", "Dane profilu", R.drawable.mob_pesel_check),
    ServiceItem("Płatności", "Usługi i opłaty", R.drawable.mob_payments),
    ServiceItem("Zdrowie", "Informacje zdrowotne", R.drawable.mob_health),
    ServiceItem("Transport", "Podróże i przejazdy", R.drawable.mob_transport),
    ServiceItem("Kalendarz", "Terminy i przypomnienia", R.drawable.mob_ticket),
    ServiceItem("Bezpieczeństwo", "Prywatność i ochrona", R.drawable.mob_cyber_safety),
    ServiceItem("Usługi publiczne", "Informacje i sprawy", R.drawable.mob_environment),
    ServiceItem("Ustawienia", "Preferencje aplikacji", R.drawable.mob_penalties),
    ServiceItem("Pozostałe", "Więcej modułów", R.drawable.mob_pesel_lock)
)

@Composable
private fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header()
            Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                PrototypeNotice()
                Spacer(Modifier.height(18.dp))
                SectionHeading("Twoje usługi", "Wybierz obszar")
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services) { service -> ServiceCard(service) }
                }
                Spacer(Modifier.height(10.dp))
            }
            BottomNavigation(selectedTab = selectedTab, onSelect = { selectedTab = it })
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Navy)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark()
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("obywatel", color = White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("PROTOTYP NIEOFICJALNY", color = Color(0xFFB9D4EA), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(Color(0xFF234665)).clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = "Powiadomienia — demonstracja", tint = White)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Wszystko w jednym miejscu", color = White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text("Usługi, informacje i dokumenty w wersji demonstracyjnej", color = Color(0xFFD4E3EF), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp))
            .background(White).padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        // Autorski znak prototypu — nie jest oficjalnym logo mObywatela ani godłem RP.
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp))
                .background(Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Public, contentDescription = "Autorski znak prototypu", tint = White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun PrototypeNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Blue, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Wersja demonstracyjna", color = Navy, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "To niezależny prototyp. Nie zawiera oficjalnych dokumentów, logowania ani usług administracji.",
                    color = Muted, style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(title, color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text("ZOBACZ", color = Blue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ServiceCard(item: ServiceItem) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.14f).clickable { },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(item.icon), contentDescription = item.title, tint = item.tint, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(item.title, color = Navy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(item.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun BottomNavigation(selectedTab: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Start", Icons.Filled.Home, 0),
        Triple("Usługi", Icons.Filled.Fingerprint, 1),
        Triple("Profil", R.drawable.mob_pesel_check, 2)
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(White)
            .border(width = 0.5.dp, color = Border)
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, icon, index) ->
            Column(
                modifier = Modifier.width(78.dp).clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(index) }.padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = label, tint = if (selectedTab == index) Blue else Muted, modifier = Modifier.size(23.dp))
                Text(label, color = if (selectedTab == index) Blue else Muted, style = MaterialTheme.typography.labelSmall, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}
