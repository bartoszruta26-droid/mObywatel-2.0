package pl.gov.obywatel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.Border
import pl.gov.obywatel.android.ui.theme.Blue
import pl.gov.obywatel.android.ui.theme.BlueLight
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

private data class ServiceItem(val title: String, val subtitle: String, val icon: ImageVector)

private val services = listOf(
    ServiceItem("Dokumenty", "Podgląd dokumentów (demo)", Icons.Filled.Description),
    ServiceItem("Moje dane", "Profil demonstracyjny", Icons.Filled.AccountCircle),
    ServiceItem("Bezpieczeństwo", "Ustawienia prywatności", Icons.Filled.Security),
    ServiceItem("Płatności", "Miejsce na przyszłą usługę", Icons.Filled.CreditCard),
    ServiceItem("Zdrowie", "Sekcja demonstracyjna", Icons.Filled.HealthAndSafety),
    ServiceItem("Więcej", "Pozostałe moduły", Icons.Filled.MoreHoriz)
)

@Composable
private fun HomeScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Navy).padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("OBYWATEL", color = White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Twoje sprawy w jednym miejscu", color = White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Filled.NotificationsNone, contentDescription = "Powiadomienia — demonstracja", tint = White, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(18.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Badge, contentDescription = null, tint = White, modifier = Modifier.size(42.dp))
                        Column(Modifier.padding(start = 14.dp)) {
                            Text("Witaj!", color = White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("To prototyp interfejsu. Brak prawdziwych dokumentów i usług.", color = White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text("Usługi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services) { service -> ServiceCard(service) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(White).padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(Icons.Filled.Badge, "Start", true)
                BottomItem(Icons.Filled.Fingerprint, "Usługi", false)
                BottomItem(Icons.Filled.AccountCircle, "Profil", false)
            }
        }
    }
}

@Composable
private fun ServiceCard(item: ServiceItem) {
    Card(
        modifier = Modifier.fillMaxWidth().height(142.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(42.dp).background(BlueLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Blue, modifier = Modifier.size(23.dp))
            }
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Navy)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = if (selected) Blue else Muted, modifier = Modifier.size(22.dp))
        Text(label, color = if (selected) Blue else Muted, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
