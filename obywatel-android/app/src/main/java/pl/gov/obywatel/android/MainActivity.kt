// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.os.Bundle
import android.content.Context
import android.graphics.BitmapFactory
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: Int? = null,
    val fallbackIcon: ImageVector? = null,
    val tint: Color = Blue,
    val category: String = "Pozostałe"
)

private val services = listOf(
    ServiceItem("Portfel", "Wszystkie dokumenty i ewidencje", R.drawable.mob_document_check, category = "Dokumenty"),
    ServiceItem("Polak za granicą", "Informacje konsularne", R.drawable.mob_abroad, category = "Transport"),
    ServiceItem("Płatności", "Usługi i opłaty", R.drawable.mob_payments, category = "Sprawy urzędowe"),
    ServiceItem("Zdrowie", "Informacje zdrowotne", R.drawable.mob_health, category = "Zdrowie"),
    ServiceItem("Transport", "Podróże i przejazdy", R.drawable.mob_transport, category = "Podróże"),
    ServiceItem("Mandaty", "Ewidencja i informacje", R.drawable.mob_penalties, category = "Kierowca i pojazdy"),
    ServiceItem("Sprawdź PESEL", "Informacje o numerze PESEL", R.drawable.mob_pesel_check, category = "Bezpieczeństwo"),
    ServiceItem("Zastrzeż PESEL", "Informacje o zastrzeżeniu", R.drawable.mob_pesel_lock, category = "Bezpieczeństwo"),
    ServiceItem("Bilety", "Bilety i przejazdy", R.drawable.mob_ticket, category = "Podróże"),
    ServiceItem("Bezpieczeństwo", "Prywatność i ochrona", R.drawable.mob_cyber_safety, category = "Bezpieczeństwo"),
    ServiceItem("Środowisko", "Informacje środowiskowe", R.drawable.mob_environment, category = "Środowisko"),
    ServiceItem("Ustawienia", "Preferencje i dane ewidencyjne", fallbackIcon = Icons.Filled.Settings),
    ServiceItem("Zaświadczenia", "Dokumenty i dane o uprawnieniach", fallbackIcon = Icons.Filled.Description),
    ServiceItem("Sprawy rodzinne", "Lokalna ewidencja spraw rodzinnych", fallbackIcon = Icons.Filled.AccountCircle, category = "Rodzina"),
    ServiceItem("Edukacja", "Ewidencja nauki i dokumentów edukacyjnych", fallbackIcon = Icons.Filled.Description, category = "Edukacja"),
    ServiceItem("Praca", "Informacje i dokumenty związane z pracą", fallbackIcon = Icons.Filled.Badge, category = "Praca"),
    ServiceItem("Finanse i podatki", "Lokalny katalog spraw finansowych", fallbackIcon = Icons.Filled.Payments, category = "Finanse i podatki"),
    ServiceItem("Nieruchomości", "Ewidencja nieruchomości użytkownika", fallbackIcon = Icons.Filled.Home, category = "Nieruchomości"),
    ServiceItem("Prawo i obywatel", "Informacje obywatelskie i prawne", fallbackIcon = Icons.Filled.AccountBalance, category = "Prawo i obywatel"),
    ServiceItem("Kultura i dziedzictwo", "Katalog instytucji i wydarzeń", fallbackIcon = Icons.Filled.Public, category = "Kultura i dziedzictwo"),
    ServiceItem("Sport", "Aktywność i lokalne obiekty sportowe", fallbackIcon = Icons.Filled.HealthAndSafety, category = "Sport"),
    ServiceItem("Organizacje społeczne", "Katalog organizacji i inicjatyw", fallbackIcon = Icons.Filled.AccountCircle, category = "Organizacje społeczne"),
    ServiceItem("Kościół i wspólnoty", "Katalog wspólnot i instytucji religijnych", fallbackIcon = Icons.Filled.AccountBalance, category = "Kościół i wspólnoty"),
    ServiceItem("Pozostałe", "Więcej modułów", fallbackIcon = Icons.Filled.MoreHoriz)
)

@Composable
private fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeService by remember { mutableStateOf<String?>(null) }
    var showDemoDocuments by remember { mutableStateOf(false) }
    var showDocumentsEditor by remember { mutableStateOf(false) }
    var selectedEditorCategory by remember { mutableStateOf("all") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var showDisabilitySettings by remember { mutableStateOf(false) }
    var showCertificates by remember { mutableStateOf(false) }
    var serviceSearch by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("Wszystkie") }

    fun openService(title: String) {
        when (title) {
            "Portfel" -> showDemoDocuments = true
            "Ustawienia" -> {
                selectedTab = 2
                showSettingsMenu = false
                showDemoDocuments = false
                selectedDocumentId = null
            }
            "Zaświadczenia" -> showCertificates = true
            else -> activeService = title
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    showDocumentsEditor -> DemoDocumentsScreen(categoryId = selectedEditorCategory, onBack = { showDocumentsEditor = false; showSettingsMenu = true })
                    showSettingsMenu -> SettingsMenuScreen(
                        onBack = { showSettingsMenu = false },
                        onEditDocuments = { category -> selectedEditorCategory = category; showSettingsMenu = false; showDocumentsEditor = true },
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true }
                    )
                    selectedDocumentId != null -> DocumentDetailsScreen(
                        documentId = selectedDocumentId!!,
                        onBack = { selectedDocumentId = null }
                    )
                    showDemoDocuments -> DocumentsWalletScreen(
                        onBack = { showDemoDocuments = false },
                        onOpenDetails = { selectedDocumentId = it }
                    )
                    showDisabilitySettings -> DisabilitySettingsScreen(onBack = { showDisabilitySettings = false; showSettingsMenu = true })
                    showCertificates -> CertificatesScreen(onBack = { showCertificates = false })
                    activeService != null -> ServiceDetailScreen(
                        title = activeService!!,
                        onBack = { activeService = null },
                        onOpenDocuments = { activeService = null; showDemoDocuments = true }
                    )
                    selectedTab == 1 -> DocumentsWalletScreen(
                        onBack = { selectedTab = 0 },
                        onOpenDetails = { selectedDocumentId = it }
                    )
                    selectedTab == 3 -> QrToolsScreen()
                    selectedTab == 4 -> ProfileLandingScreen(
                        onOpenDocuments = { selectedTab = 1; showDemoDocuments = true },
                        onOpenSettings = { showSettingsMenu = true },
                        onOpenCertificates = { showCertificates = true }
                    )
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (selectedTab == 0) Header(onOpenSettings = {
                                selectedTab = 2
                                showSettingsMenu = false
                                showDemoDocuments = false
                                selectedDocumentId = null
                            })
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp)) {
                                Spacer(Modifier.height(18.dp))
                                if (selectedTab == 0) {
                                    PrototypeNotice()
                                    Spacer(Modifier.height(12.dp))
                                    CivicIllustrationBanner(
                                        imageRes = R.drawable.header_main_banner,
                                        description = "Baner główny prototypu obywatelskiego"
                                    )
                                } else {
                                    CivicIllustrationBanner(
                                        imageRes = R.drawable.header_poland_banner,
                                        description = "Baner informacyjny Polski"
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                SectionHeading(
                                    if (selectedTab == 2) "Usługi" else "Pulpit",
                                    if (selectedTab == 2) "Usługi uporządkowane według kategorii" else "Najczęściej używane obszary"
                                )
                                Spacer(Modifier.height(12.dp))
                                if (selectedTab == 2) {
                                    OutlinedTextField(
                                        value = serviceSearch,
                                        onValueChange = { serviceSearch = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Szukaj usługi") },
                                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("Wszystkie", "Zdrowie", "Kierowca i pojazdy", "Sprawy urzędowe", "Rodzina", "Edukacja", "Praca", "Finanse i podatki", "Transport", "Nieruchomości", "Środowisko", "Bezpieczeństwo", "Prawo i obywatel", "Kultura i dziedzictwo", "Sport", "Organizacje społeczne", "Kościół i wspólnoty", "Dokumenty", "Pozostałe")
                                            .forEach { category ->
                                                val active = serviceCategory == category
                                                Box(
                                                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                                        .background(if (active) Blue else White)
                                                        .border(1.dp, if (active) Blue else Border, RoundedCornerShape(18.dp))
                                                        .clickable { serviceCategory = category }
                                                        .padding(horizontal = 11.dp, vertical = 8.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        categoryIconFor(category)?.let { iconRes ->
                                                            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(19.dp), contentScale = ContentScale.Fit)
                                                        }
                                                        Text(category, color = if (active) White else Navy,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                                                    }
                                                }
                                            }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                val visibleServices = if (selectedTab == 2) services.filter { service ->
                                    (serviceCategory == "Wszystkie" || service.category == serviceCategory) &&
                                        (serviceSearch.isBlank() || service.title.contains(serviceSearch, ignoreCase = true) ||
                                            service.subtitle.contains(serviceSearch, ignoreCase = true))
                                } else services.take(4)
                                if (visibleServices.isEmpty()) {
                                    Text(
                                        "Nie znaleziono usług. Zmień wyszukiwaną frazę lub kategorię.",
                                        color = Muted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(visibleServices) { service ->
                                            ServiceCard(service) { openService(service.title) }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
            BottomNavigation(
                selectedTab = selectedTab,
                onSelect = {
                    selectedTab = it
                    activeService = null
                    showDemoDocuments = false
                    selectedDocumentId = null
                    showDocumentsEditor = false
                    showSettingsMenu = false
                    showDisabilitySettings = false
                    showCertificates = false
                }
            )
        }
    }
}

private fun categoryIconFor(category: String): Int? = when (category) {
    "Zdrowie" -> R.drawable.health
    "Rodzina" -> R.drawable.family
    "Edukacja" -> R.drawable.education
    "Praca" -> R.drawable.work
    "Finanse i podatki" -> R.drawable.finance_taxes
    "Transport", "Kierowca i pojazdy", "Podróże" -> R.drawable.transport
    "Nieruchomości" -> R.drawable.real_estate
    "Środowisko" -> R.drawable.environment
    "Bezpieczeństwo" -> R.drawable.security
    "Prawo i obywatel", "Sprawy urzędowe" -> R.drawable.law_citizenship
    "Kultura i dziedzictwo" -> R.drawable.culture_heritage
    "Sport" -> R.drawable.sport
    "Organizacje społeczne" -> R.drawable.social_organizations
    "Kościół i wspólnoty" -> R.drawable.church_communities
    else -> null
}

@Composable
private fun QrToolsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Kod QR", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Icon(Icons.Filled.QrCode2, contentDescription = "Ilustracyjna ikona kodu QR", tint = Blue, modifier = Modifier.size(112.dp))
        Text("Narzędzia kodów QR", color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Ta sekcja prototypu służy do prezentacji przyszłych funkcji skanowania i udostępniania danych. Nie generuje kodów potwierdzających dokumenty ani nie weryfikuje tożsamości.",
            color = Muted, style = MaterialTheme.typography.bodyMedium
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Skanowanie kodu", color = Navy, fontWeight = FontWeight.Bold)
                Text("Obsługa aparatu i bezpiecznego odczytu zostanie dodana po zaprojektowaniu lokalnego, demonstracyjnego formatu danych.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Udostępnianie danych", color = Navy, fontWeight = FontWeight.Bold)
                Text("Przyszła funkcja będzie wymagała świadomego wyboru zakresu danych i potwierdzenia przez użytkownika.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Prototyp obywatelski • bez połączenia z systemami administracji publicznej", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CivicIllustrationBanner(imageRes: Int, description: String) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = description,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ProfileLandingScreen(
    onOpenDocuments: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit
) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Więcej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = R.drawable.header_logo_panel,
            description = "Panel znaku i identyfikacji prototypu"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Blue, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Użytkownik" },
                        color = Navy, fontWeight = FontWeight.Bold
                    )
                    Text("Profil lokalny • dane demonstracyjne", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileActionCard("Dokumenty i dane", "Przeglądaj lokalną ewidencję", Icons.Filled.CreditCard, onOpenDocuments)
        ProfileActionCard("Zaświadczenia", "Zapisane dane dokumentów i zaświadczeń", Icons.Filled.Description, onOpenCertificates)
        ProfileActionCard("Ustawienia", "Otwórz menu zarządzania lokalnymi danymi", Icons.Filled.Settings, onOpenSettings)
        Text(
            "Profil przechowuje dane wyłącznie lokalnie. Nie jest kontem administracji publicznej i nie zapewnia logowania do usług urzędowych.",
            color = Muted, style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ProfileActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(title, color = Navy, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.MoreHoriz, contentDescription = "Otwórz", tint = Muted)
        }
    }
}

@Composable
private fun ServiceDetailScreen(title: String, onBack: () -> Unit, onOpenDocuments: () -> Unit) {
    val descriptions = mapOf(
        "Polak za granicą" to "Informacje dla obywateli przebywających poza granicami kraju.",
        "Płatności" to "Miejsce na ewidencję opłat i informacje o usługach płatniczych. Brak integracji z systemami płatności.",
        "Zdrowie" to "Lokalny zestaw informacji zdrowotnych i odnośników. Dane nie są pobierane z Internetowego Konta Pacjenta.",
        "Transport" to "Informacje transportowe i ewidencja danych związanych z podróżą. Brak połączenia z rejestrami publicznymi.",
        "Mandaty" to "Miejsce na lokalną ewidencję informacji o mandatach. Brak dostępu do policyjnych i administracyjnych rejestrów mandatów.",
        "Sprawdź PESEL" to "Ekran informacyjny dotyczący numeru PESEL. Prototyp nie odpytuje rejestru PESEL i nie potwierdza danych.",
        "Zastrzeż PESEL" to "Ekran informacyjny dotyczący zastrzeżenia numeru PESEL. Prototyp nie wysyła dyspozycji do rejestru i nie zmienia statusu numeru.",
        "Bilety" to "Miejsce na prezentację własnych zapisów dotyczących biletów. Aplikacja nie sprzedaje ani nie waliduje biletów.",
        "Bezpieczeństwo" to "Ustawienia prywatności, wskazówki bezpieczeństwa i informacje o ochronie danych.",
        "Środowisko" to "Informacje środowiskowe i lokalne materiały edukacyjne.",
        "Pozostałe" to "Dodatkowe moduły będą dodawane etapami w wersji alfa."
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do usług", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text(title, color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = when (title) {
                "Zdrowie" -> R.drawable.navigation_help
                "Polak za granicą", "Transport", "Bilety" -> R.drawable.navigation_mountain_landscape
                "Środowisko" -> R.drawable.navigation_services
                else -> R.drawable.navigation_profile
            },
            description = "Ilustracja tematyczna modułu $title"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(descriptions[title] ?: "Moduł informacyjny wersji alfa.", color = Ink)
                Text("MODUŁ WERSJI ALFA", color = Blue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Ekran jest częścią niezależnego prototypu obywatelskiego. Funkcje wymagające połączenia z rejestrami, usługami lub systemami administracji pozostają nieaktywne do czasu uzyskania odpowiednich integracji.",
                    color = Muted, style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (title == "Bezpieczeństwo") {
            ProfileActionCard("Dane i dokumenty lokalne", "Otwórz formularz danych przechowywanych na urządzeniu", Icons.Filled.Security, onOpenDocuments)
        }
        if (title == "Zdrowie") {
            ProfileActionCard("Dane o niepełnosprawności", "Otwórz ewidencję dokumentów i wskazań", Icons.Filled.HealthAndSafety, onOpenDocuments)
        }
        Text("Dostępne funkcje będą oznaczane bezpośrednio w module. Nie prezentujemy fikcyjnych potwierdzeń, płatności, biletów ani statusów urzędowych.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(Color(0xFF234665)).clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ustawienia", tint = White)
                }
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF234665)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = "Powiadomienia — demonstracja", tint = White)
                }
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
private fun ServiceCard(item: ServiceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.14f).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(painter = painterResource(item.icon), contentDescription = item.title, tint = item.tint, modifier = Modifier.size(24.dp))
                } else {
                    item.fallbackIcon?.let { Icon(it, contentDescription = item.title, tint = item.tint, modifier = Modifier.size(24.dp)) }
                }
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
private fun SettingsMenuScreen(
    onBack: () -> Unit,
    onEditDocuments: (String) -> Unit,
    onEditDisability: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ekranu głównego", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Ustawienia", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zarządzanie lokalnymi danymi prototypu", color = Muted,
            style = MaterialTheme.typography.bodyMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Edycja tylko w Ustawieniach", color = Navy, fontWeight = FontWeight.Bold)
                Text("Portfel dokumentów i jego szczegóły są widokami do odczytu. Formularze edycji można otworzyć wyłącznie z tego menu.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Dane profilu i dokumenty", color = Navy, fontWeight = FontWeight.Bold)
        ProfileActionCard("Dane osobowe", "Zdjęcie profilowe, dane osoby i rodziców",
            Icons.Filled.AccountCircle, { onEditDocuments("profile") })
        ProfileActionCard("Dowód osobisty", "Prywatna ewidencja danych dowodu",
            Icons.Filled.CreditCard, { onEditDocuments("identity_card") })
        ProfileActionCard("Paszport", "Prywatna ewidencja danych paszportowych",
            Icons.Filled.Public, { onEditDocuments("passport") })
        ProfileActionCard("Dokument rodowy", "Dane prywatnej ewidencji rodowej",
            Icons.Filled.Fingerprint, { onEditDocuments("family") })
        ProfileActionCard("Legitymacja poselska", "Ewidencja danych i źródeł potwierdzenia",
            Icons.Filled.AccountBalance, { onEditDocuments("parliament") })
        ProfileActionCard("Legitymacja kursanta", "Ewidencja instytucji i uczestnictwa w kursie",
            Icons.Filled.Description, { onEditDocuments("training") })
        ProfileActionCard("Legitymacja studencka", "Ewidencja danych uczelni i statusu",
            Icons.Filled.Badge, { onEditDocuments("student") })
        ProfileActionCard("Orzeczenia i zaświadczenia", "Ewidencja danych o rozstrzygnięciach",
            Icons.Filled.HealthAndSafety, onEditDisability)
        ProfileActionCard("Wszystkie dane", "Otwórz dotychczasowy formularz zbiorczy",
            Icons.Filled.Description, { onEditDocuments("all") })
        Text("Zmiany są zapisywane lokalnie w zaszyfrowanym magazynie prototypu. Nie są wysyłane do rejestrów ani podmiotów publicznych.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DisabilitySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var saveMessage by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Ustawienia danych o niepełnosprawności", color = Navy,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Wprowadź dane z posiadanego orzeczenia, zaświadczenia, opinii lub oświadczenia. Wpis jest przechowywany lokalnie i nie jest automatycznie weryfikowany.", color = Muted, style = MaterialTheme.typography.bodySmall)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Źródło dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                val options = listOf(
                    "powiat" to "Zespół powiatowy / miejski",
                    "other_law" to "Inny organ — odrębna podstawa prawna",
                    "foundation" to "Zespół medyczny fundacji",
                    "medical_opinion" to "Opinia / zaświadczenie medyczne",
                    "self_declaration" to "Własne oświadczenie"
                )
                options.forEach { (value, label) ->
                    val selected = (fields["disabilityAuthorityType"] ?: "powiat") == value
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                            .background(if (selected) BlueLight else Color(0xFFF5F6F7))
                            .border(1.dp, if (selected) Blue else Border, RoundedCornerShape(9.dp))
                            .clickable { fields["disabilityAuthorityType"] = value }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null, tint = if (selected) Blue else Muted)
                        Text(label, modifier = Modifier.padding(start = 9.dp), color = Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                DemoDataField("disabilityAuthorityName", "Organ / podmiot / wystawca", fields)
                DemoDataField("disabilityIssuerName", "Osoba podpisująca / wystawca", fields)
                DemoDataField("disabilityLegalBasis", "Podstawa prawna lub regulacyjna", fields)
                DemoDataField("disabilityFoundationStatute", "Statut / uchwała fundacji (jeśli dotyczy)", fields)
                Text("Dane osoby i dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityPersonName", "Imię i nazwisko", fields)
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator", fields)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / zaświadczenia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                Text("Ustalony stopień i okres", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityCategory", "Kategoria", fields)
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki")
                DemoDataField("disabilityCauseCodes", "Symbole / przyczyny", fields)
                DemoDataField("disabilityValidity", "Okres obowiązywania", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data ustalenia niepełnosprawności", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityEmployment", "Zatrudnienie", fields)
                DemoDataField("disabilityTraining", "Szkolenie", fields)
                DemoDataField("disabilityTherapy", "Terapia / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Sprzęt i środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Wsparcie środowiskowe", fields)
                DemoDataField("disabilityParking", "Karta parkingowa — wskazania", fields)
                Text("Metodologia i ocena funkcjonalna (opcjonalnie)", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityAssessmentMethod", "Metoda oceny (np. ICF)", fields)
                DemoDataField("disabilityWhodasScore", "WHODAS 2.0 — wynik / data oceny", fields)
                DemoDataField("disabilityMedicalQualification", "Kwalifikacje osoby oceniającej", fields)
                DemoDataField("disabilityAppealInfo", "Tryb odwołania / weryfikacji", fields)
                androidx.compose.material3.Button(
                    onClick = {
                        runCatching { DemoIdentityStore.save(context, fields.toMap()) }
                            .onSuccess { saveMessage = "Zapisano dane lokalnie i zaszyfrowano." }
                            .onFailure { saveMessage = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz dane") }
                if (saveMessage.isNotBlank()) Text(saveMessage, color = Blue, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CertificatesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val hasDisabilityData = listOf("disabilityPersonName", "disabilityDecisionNumber", "disabilityDecisionDate",
        "disabilityDegree", "disabilityAuthorityName").any { !fields[it].isNullOrBlank() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Zaświadczenia", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zapisane dane dokumentów i zaświadczeń", color = Muted, style = MaterialTheme.typography.bodySmall)
        if (hasDisabilityData) {
            Text("Orzeczenie / zaświadczenie o niepełnosprawności", color = Navy, fontWeight = FontWeight.Bold)
            DisabilityDecisionPreview(fields)
            listOf(
                "Organ / wystawca" to "disabilityAuthorityName",
                "Numer dokumentu" to "disabilityDecisionNumber",
                "Data wydania" to "disabilityDecisionDate",
                "Stopień" to "disabilityDegree",
                "Okres obowiązywania" to "disabilityValidity",
                "Podstawa prawna / regulacyjna" to "disabilityLegalBasis"
            ).forEach { (label, key) ->
                val value = fields[key].orEmpty()
                if (value.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
                        Text(value, color = Ink, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(190.dp))
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                    Text("Brak zapisanych danych", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Dodaj dane o niepełnosprawności w Ustawieniach. Po zapisaniu pojawią się tutaj.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text("Widok ma charakter ewidencyjny. Aplikacja prezentuje dane wpisane przez użytkownika i nie potwierdza autentyczności dokumentu ani jego skutków prawnych.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DocumentsWalletScreen(onBack: () -> Unit, onOpenDetails: (String) -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val groups = listOf(
        Triple("Dane osobowe profilu", Icons.Filled.AccountCircle, listOf("firstNames", "lastName", "birthDate", "pesel")),
        Triple("Dowód osobisty — ewidencja", Icons.Filled.CreditCard, listOf("idNumber", "idStatus")),
        Triple("Paszport — ewidencja", Icons.Filled.Public, listOf("passportNumber", "passportStatus")),
        Triple("Dokument rodowy — ewidencja prywatna", Icons.Filled.Fingerprint, listOf("familyDocNumber", "familyName")),
        Triple("Legitymacja poselska — ewidencja", Icons.Filled.AccountBalance, listOf("mpCardNumber", "mpTerm")),
        Triple("Legitymacja kursanta", Icons.Filled.Description, listOf("trainingCardNumber", "trainingInstitution")),
        Triple("Legitymacja studencka — ewidencja", Icons.Filled.Badge, listOf("studentCardNumber", "studentUniversity")),
        Triple("Orzeczenia i zaświadczenia", Icons.Filled.HealthAndSafety, listOf("disabilityDecisionNumber", "disabilityAuthorityName"))
    )
    val populatedCount = groups.count { (_, _, keys) -> keys.any { !fields[it].isNullOrBlank() } }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Portfel dokumentów", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Twoja lokalna ewidencja danych i dokumentów", color = Muted,
            style = MaterialTheme.typography.bodyMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Dane przechowywane na urządzeniu", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Wpisy są lokalną ewidencją. Aplikacja nie weryfikuje dokumentów ani statusów w rejestrach publicznych.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("$populatedCount / 8", color = Navy, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text("obszary z danymi", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Lokalnie", color = Navy, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("sposób przechowywania", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text("Twoje dokumenty i dane", color = Navy, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        groups.forEachIndexed { index, (title, icon, keys) ->
            val hasData = keys.any { !fields[it].isNullOrBlank() }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenDetails(index.toString()) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(when (index) {
                            0 -> R.drawable.documents_family_record
                            1 -> R.drawable.documents_identity_card
                            2 -> R.drawable.documents_passport
                            3 -> R.drawable.documents_family_record
                            4 -> R.drawable.documents_deputy_legitimacy
                            5 -> R.drawable.documents_course_legitimacy
                            6 -> R.drawable.documents_student_legitimacy
                            else -> R.drawable.documents_medical_certificates
                        }),
                        contentDescription = "Ilustracja: $title",
                        modifier = Modifier.size(58.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(title, color = Navy, fontWeight = FontWeight.SemiBold)
                        Text(if (hasData) "Dane zapisane lokalnie • ewidencja prywatna" else "Brak zapisanych danych",
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(if (hasData) "Dane" else "Dodaj", color = Blue,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Portfel jest przeznaczony do przeglądania. Zmiana danych jest dostępna wyłącznie w menu Ustawienia otwieranym z ekranu głównego. Karty nie są dokumentami państwowymi, nie nadają uprawnień i nie zastępują oryginałów.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    val sections = listOf(
        Triple("Dane osobowe profilu", "Profil osoby — ewidencja prywatna", listOf(
            "Imiona" to "firstNames", "Nazwisko" to "lastName", "Data urodzenia" to "birthDate",
            "Nazwisko rodowe" to "birthSurname", "PESEL" to "pesel", "Miejsce urodzenia" to "birthPlace", "Obywatelstwo" to "citizenship", "Płeć — wpis deklarowany" to "sex", "Imiona ojca" to "fatherNames", "Nazwisko ojca" to "fatherSurname", "Imiona matki" to "motherNames", "Nazwisko rodowe matki" to "motherSurname"
        )),
        Triple("Dowód osobisty — ewidencja", "Dane dowodu — wpis użytkownika", listOf(
            "Numer dokumentu" to "idNumber", "Status" to "idStatus",
            "Numer poprzedniego dokumentu" to "idPreviousNumber", "Data wydania" to "idIssueDate", "Data ważności" to "idExpiryDate", "Organ wydający" to "idIssuer", "Data utraty / unieważnienia" to "idLossOrInvalidationDate"
        )),
        Triple("Paszport — ewidencja", "Dane dokumentu podróży — wpis użytkownika", listOf(
            "Numer paszportu" to "passportNumber", "Rodzaj dokumentu" to "passportType",
            "Obywatelstwo" to "passportCitizenship", "Status" to "passportStatus",
            "Państwo wydania" to "passportCountry", "Imiona i nazwisko w dokumencie" to "passportHolderNames", "Miejsce urodzenia" to "passportBirthPlace", "Płeć w dokumencie" to "passportSex", "Data wydania" to "passportIssueDate", "Data ważności" to "passportExpiryDate", "Organ wydający" to "passportIssuer", "Data utraty / unieważnienia" to "passportLossDate"
        )),
        Triple("Dokument rodowy — ewidencja prywatna", "Prywatna karta ewidencji rodowej", listOf(
            "Numer dokumentu rodowego" to "familyDocNumber", "Nazwa rodu" to "familyName",
            "Linia / gałąź" to "familyLine", "Miejsce w strukturze rodu" to "familyRole",
            "Prowadzący ewidencję" to "familyRegistry", "Data sporządzenia" to "familyIssueDate", "Data przeglądu / aktualizacji" to "familyExpiryDate", "Miejsce prowadzenia ewidencji" to "familyRegistryPlace", "Miejsce zamieszkania — wpis dobrowolny" to "familyResidence", "Linia rodowa ojca" to "familyFatherLine", "Linia rodowa matki" to "familyMotherLine", "Podstawa wpisu" to "familySource", "Uwagi ewidencyjne" to "familyNotes"
        )),
        Triple("Legitymacja poselska — ewidencja", "Ewidencja informacji o legitymacji i mandacie", listOf(
            "Numer legitymacji" to "mpCardNumber", "Kadencja" to "mpTerm",
            "Wystawca" to "mpIssuer", "Status legitymacji" to "mpStatus",
            "Okręg wyborczy" to "mpDistrict", "Data rozpoczęcia mandatu" to "mpStart", "Data zakończenia mandatu" to "mpEnd", "Data wydania legitymacji" to "mpCardIssueDate", "Data ślubowania" to "mpOathDate", "Miejsce ślubowania" to "mpOathPlace", "Źródło potwierdzenia ślubowania" to "mpOathEvidence", "Źródło potwierdzenia" to "mpMandateSource"
        )),
        Triple("Legitymacja kursanta", "Prywatna legitymacja instytucji szkoleniowej", listOf(
            "Numer legitymacji" to "trainingCardNumber", "Instytucja" to "trainingInstitution",
            "Nazwa kursu" to "trainingCourseName", "Status uczestnictwa" to "trainingStatus",
            "Adres instytucji" to "trainingInstitutionAddress", "Numer wpisu instytucji" to "trainingInstitutionRegistry", "Symbol kursu" to "trainingCourseCode", "Data rozpoczęcia" to "trainingStartDate", "Data zakończenia" to "trainingEndDate", "Data wydania legitymacji" to "trainingIssueDate", "Ważna do" to "trainingValidUntil", "Wystawca" to "trainingIssuer", "Podpis wystawcy" to "trainingSignature", "Data ukończenia" to "trainingCompletionDate", "Liczba godzin" to "trainingHours", "Wynik zaliczenia" to "trainingAssessment", "Numer zaświadczenia" to "trainingCertificateNumber", "Forma kształcenia" to "trainingForm"
        )),
        Triple("Legitymacja studencka — ewidencja", "Ewidencja danych podanych przez użytkownika", listOf(
            "Numer legitymacji" to "studentCardNumber", "Uczelnia" to "studentUniversity",
            "Kierunek" to "studentProgramme", "Rok akademicki" to "studentAcademicYear",
            "Adres uczelni" to "studentUniversityAddress", "Identyfikator uczelni" to "studentUniversityId", "Imię i nazwisko w dokumencie" to "studentNames", "Wydział" to "studentFaculty", "Kierunek" to "studentProgramme", "Poziom studiów" to "studentLevel", "Profil / forma studiów" to "studentProfile", "Rok i semestr" to "studentYear", "Rok akademicki" to "studentAcademicYear", "Data wydania" to "studentIssueDate", "Status" to "studentStatus", "Ważność" to "studentValidity", "Wystawca" to "studentIssuer"
        )),
        Triple("Orzeczenia i zaświadczenia", "Ewidencja informacji o rozstrzygnięciach i zaświadczeniach", listOf(
            "Numer dokumentu / sprawy" to "disabilityDecisionNumber", "Wystawca" to "disabilityAuthorityName",
            "Data dokumentu" to "disabilityDecisionDate", "Stopień / kategoria" to "disabilityDegree",
            "Okres obowiązywania" to "disabilityValidity", "Rodzaj rozstrzygnięcia" to "disabilityDecisionType", "Osoba podpisująca / wystawca" to "disabilityIssuerName", "Powiat / miasto" to "disabilityCounty", "Podstawa prawna" to "disabilityLegalBasis", "Przepis określający skutki" to "disabilityLegalProvision", "Zakres uznania / równoważności" to "disabilityEquivalentEffect", "Fundacja / podmiot prowadzący" to "disabilityFoundationName", "Podstawa statutowa" to "disabilityFoundationStatute", "Kwalifikacje osoby wystawiającej" to "disabilityMedicalQualification", "Ocena medyczna" to "disabilityMedicalAssessment", "Skład zespołu" to "disabilityPanelMembers", "Przewodniczący zespołu" to "disabilityChairperson", "Osoba zatwierdzająca" to "disabilitySignature", "Tryb odwołania" to "disabilityAppealInfo", "Data wniosku" to "disabilityApplicationDate", "Numer sprawy" to "disabilityCaseNumber", "Data doręczenia" to "disabilityDeliveryDate", "Wynik odwołania" to "disabilityAppealOutcome", "Duplikat / zmiana dokumentu" to "disabilityDuplicateInfo", "Dokument źródłowy" to "disabilitySourceAttachment"
        ))
    )
    val section = sections.getOrNull(documentId.toIntOrNull() ?: -1) ?: sections.first()
    val extraPrefix = listOf("personExtra", "idExtra", "passportExtra", "familyExtra", "mpExtra", "trainingExtra", "studentExtra", "disabilityExtra").getOrNull(documentId.toIntOrNull() ?: -1) ?: "personExtra"
    val photoBase64 = fields["photo"].orEmpty()
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do portfela", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text(section.first, color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(section.second, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Image(
            painter = painterResource(when (documentId.toIntOrNull()) {
                0 -> R.drawable.documents_document_wallet_banner
                1 -> R.drawable.documents_identity_card
                2 -> R.drawable.documents_passport
                3 -> R.drawable.documents_family_record
                4 -> R.drawable.documents_deputy_legitimacy
                5 -> R.drawable.documents_course_legitimacy
                6 -> R.drawable.documents_student_legitimacy
                else -> R.drawable.documents_medical_certificates
            }),
            contentDescription = "Ilustracja ewidencji: ${section.first}",
            modifier = Modifier.fillMaxWidth().height(148.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("EWIDENCJA LOKALNA • DEMO", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
                Text("To widok danych zapisanych przez użytkownika. Nie potwierdza autentyczności dokumentu, statusu urzędowego ani uprawnień.",
                    color = Ink, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (documentId == "0" && photoBitmap != null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Zdjęcie profilowe", color = Navy, fontWeight = FontWeight.Bold)
                    Image(photoBitmap.asImageBitmap(), contentDescription = "Zdjęcie profilowe",
                        modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Fit)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                section.third.forEach { (label, key) ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                        Text(fields[key].orEmpty().ifBlank { "Brak wpisu" },
                            color = if (fields[key].isNullOrBlank()) Muted else Navy,
                            fontWeight = if (fields[key].isNullOrBlank()) FontWeight.Normal else FontWeight.SemiBold)
                    }
                }
            }
        }
        ReadOnlyAdditionalInformationSection(fields, extraPrefix)
        Text("Ten ekran służy wyłącznie do przeglądania. Aby zmienić wpis, wróć do ekranu głównego i otwórz menu Ustawienia. Dane są przechowywane lokalnie i nie są sprawdzane w rejestrach publicznych.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReadOnlyAdditionalInformationSection(fields: Map<String, String>, prefix: String) {
    val rows = listOf(
        "Źródło danych / dokument źródłowy" to prefix + "Source",
        "Podstawa prawna / regulaminowa" to prefix + "LegalBasis",
        "Data ostatniej weryfikacji" to prefix + "VerifiedAt",
        "Podmiot / osoba potwierdzająca dane" to prefix + "VerifiedBy",
        "Adnotacje i informacje uzupełniające" to prefix + "Notes"
    )
    val populated = rows.filter { !fields[it.second].isNullOrBlank() }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5F8))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Źródła i informacje dodatkowe", color = Navy, fontWeight = FontWeight.Bold)
            if (populated.isEmpty()) {
                Text("Brak wpisów dotyczących źródła, podstawy, weryfikacji lub adnotacji.", color = Muted, style = MaterialTheme.typography.bodySmall)
            } else {
                populated.forEach { (label, key) ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                        Text(fields[key].orEmpty(), color = Navy, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text("Wpisane źródło lub podstawa nie oznaczają niezależnej weryfikacji danych przez aplikację.", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DemoDocumentsScreen(categoryId: String = "all", onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var photoBase64 by remember { mutableStateOf(fields["photo"] ?: "") }
    var saveMessage by remember { mutableStateOf("") }
    val editorTitle = when (categoryId) {
        "profile" -> "Dane osobowe i zdjęcie profilowe"
        "family" -> "Dokument rodowy — ewidencja prywatna"
        "identity_card" -> "Dowód osobisty — ewidencja"
        "parliament" -> "Legitymacja poselska — ewidencja"
        "training" -> "Legitymacja kursanta — ewidencja"
        "student" -> "Legitymacja studencka — ewidencja"
        "disability" -> "Orzeczenia i zaświadczenia — ewidencja"
        "passport" -> "Paszport — ewidencja"
        else -> "Wszystkie dane i dokumenty"
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego zdjęcia")
            }.onSuccess { bytes ->
                if (bytes.size > 5 * 1024 * 1024) {
                    saveMessage = "Zdjęcie przekracza limit 5 MB. Wybierz mniejszy plik."
                } else {
                    photoBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    saveMessage = "Zdjęcie dodane. Zapisz zmiany."
                }
            }.onFailure { saveMessage = "Nie udało się wczytać zdjęcia: ${it.localizedMessage ?: "błąd odczytu"}" }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "‹  Powrót do ustawień",
            color = Blue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            editorTitle,
            color = Navy,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("DEMO • NIE JEST DOKUMENTEM", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
                Text(
                    "To wyłącznie formularz testowy. Nie potwierdza tożsamości, obywatelstwa, mandatu poselskiego ani uprawnień. Nie używa oficjalnych wzorów, znaków, kodów ani zabezpieczeń.",
                    color = Ink,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (categoryId == "all" || categoryId == "profile") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zdjęcie profilowe", color = Navy, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F3F6))
                        .border(1.dp, Border, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBase64.isNotBlank()) {
                        val bytes = remember(photoBase64) { runCatching { Base64.decode(photoBase64, Base64.DEFAULT) }.getOrNull() }
                        val bitmap = remember(bytes) {
                            bytes?.let {
                                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeByteArray(it, 0, it.size, bounds)
                                var sample = 1
                                while (bounds.outWidth / sample > 900 || bounds.outHeight / sample > 900) sample *= 2
                                BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options().apply { inSampleSize = sample })
                            }
                        }
                        if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = "Zdjęcie profilowe", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text("Nie można wyświetlić zdjęcia", color = Muted)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Muted, modifier = Modifier.size(42.dp))
                            Text("MIEJSCE NA ZDJĘCIE", color = Muted, fontWeight = FontWeight.Bold)
                            Text("Nie wybrano fotografii", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Wybierz zdjęcie", color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { photoPicker.launch("image/*") }.padding(vertical = 8.dp))
                    if (photoBase64.isNotBlank()) Text("Usuń zdjęcie", color = Color(0xFF9B2C2C), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { photoBase64 = "" }.padding(vertical = 8.dp))
                }
                Text("Dane osobowe", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("birthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("birthPlace", "Miejsce urodzenia", fields)
                DemoDataField("citizenship", "Obywatelstwo", fields)
                DemoDataField("sex", "Płeć (dane deklarowane)", fields)
                DemoDataField("fatherNames", "Imiona ojca", fields)
                DemoDataField("fatherSurname", "Nazwisko ojca", fields)
                DemoDataField("motherNames", "Imiona matki", fields)
                DemoDataField("motherSurname", "Nazwisko rodowe matki", fields)
                DemoDataField("birthSurname", "Nazwisko rodowe osoby", fields)
                DemoDataField("pesel", "Numer PESEL", fields)
                AdditionalInformationSection("Informacje dodatkowe — dane osobowe", fields, "personExtra", "Dane profilu są ewidencyjne. Porównuj je z aktualnym dokumentem źródłowym; aplikacja nie potwierdza tożsamości.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "family") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rodowy dokument tożsamości — dane dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("familyDocNumber", "Numer rodowego dokumentu", fields)
                DemoDataField("familyName", "Nazwa rodu", fields)
                DemoDataField("familyLine", "Linia / gałąź rodu", fields)
                DemoDataField("familyRole", "Miejsce w strukturze rodu", fields)
                DemoDataField("familyRegistry", "Osoba lub organ prowadzący ewidencję", fields)
                DemoDataField("familyIssueDate", "Data sporządzenia", fields, "DD-MM-RRRR")
                DemoDataField("familyExpiryDate", "Data przeglądu / aktualizacji wpisu", fields, "DD-MM-RRRR")
                DemoDataField("familySource", "Podstawa wpisu / dokument źródłowy", fields)
                DemoDataField("familyRegistryPlace", "Miejsce prowadzenia ewidencji", fields)
                DemoDataField("familyResidence", "Miejsce zamieszkania (wpis dobrowolny)", fields)
                DemoDataField("familyFatherLine", "Linia rodowa ojca", fields)
                DemoDataField("familyMotherLine", "Linia rodowa matki", fields)
                DemoDataField("familyNotes", "Uwagi ewidencyjne", fields)
                DemoDataField("familyHolderSignature", "Imię i nazwisko posiadacza / podpis", fields)
                DemoDataField("familyRegistrarSignature", "Imię i nazwisko wystawcy / podpis", fields)
                Text("Wizualizacja dokumentu — awers i rewers", color = Navy, fontWeight = FontWeight.Bold)
                RodowyDocumentPreview(fields = fields, photoBase64 = photoBase64)
            }
        }
        }
        if (categoryId == "all" || categoryId == "identity_card") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dane dowodu osobistego", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("idNumber", "Numer dowodu osobistego", fields)
                DemoDataField("idIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("idExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("idIssuer", "Organ wydający", fields)
                DemoDataField("idStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("idPreviousNumber", "Numer poprzedniego dokumentu (jeśli dotyczy)", fields)
                DemoDataField("idLossOrInvalidationDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                AdditionalInformationSection("Informacje dodatkowe — dowód osobisty", fields, "idExtra", "Formularz prowadzi prywatną ewidencję danych. Nie generuje dowodu osobistego, nie potwierdza tożsamości i nie zastępuje usługi publicznej.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "parliament") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja poselska — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("mpCardNumber", "Numer ewidencyjny legitymacji", fields)
                DemoDataField("mpTerm", "Kadencja", fields)
                DemoDataField("mpDistrict", "Okręg wyborczy", fields)
                DemoDataField("mpStart", "Data rozpoczęcia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpEnd", "Data zakończenia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpCardIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("mpIssuer", "Organ / osoba wystawiająca", fields)
                DemoDataField("mpStatus", "Status legitymacji", fields, "aktywna / zwrócona / unieważniona")
                DemoDataField("mpMandateSource", "Źródło potwierdzenia mandatu", fields)
                Text("Dane dotyczące ślubowania — oświadczenie własne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("mpOathDate", "Data złożenia ślubowania", fields, "DD-MM-RRRR")
                DemoDataField("mpOathPlace", "Miejsce / okoliczności wypowiedzenia ślubowania", fields)
                DemoDataField("mpOathEvidence", "Źródło potwierdzenia (np. protokół, nagranie, publikacja)", fields)
                AdditionalInformationSection("Informacje dodatkowe — legitymacja poselska", fields, "mpExtra", "Art. 104 Konstytucji RP określa wolny mandat poselski oraz obowiązek złożenia ślubowania przed Sejmem przed rozpoczęciem sprawowania mandatu. Odmowa ślubowania oznacza zrzeczenie się mandatu. Sam wpis użytkownika ani prywatna ewidencja nie potwierdzają uzyskania mandatu ani prawa do reprezentowania Sejmu.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "training") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja uczestnika kursu — instytucja szkoleniowa", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("trainingInstitution", "Nazwa instytucji szkoleniowej", fields)
                DemoDataField("trainingInstitutionAddress", "Adres instytucji", fields)
                DemoDataField("trainingInstitutionRegistry", "Numer wpisu / rejestru instytucji (jeśli dotyczy)", fields)
                DemoDataField("trainingCardNumber", "Numer legitymacji uczestnika", fields)
                DemoDataField("trainingCourseName", "Nazwa kursu / kierunek kształcenia", fields)
                DemoDataField("trainingCourseCode", "Symbol lub numer kursu", fields)
                DemoDataField("trainingStartDate", "Data rozpoczęcia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingEndDate", "Planowana data zakończenia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("trainingValidUntil", "Ważna do", fields, "DD-MM-RRRR")
                DemoDataField("trainingStatus", "Status uczestnictwa", fields, "np. aktywny / ukończony / zawieszony")
                DemoDataField("trainingIssuer", "Osoba upoważniona do wystawienia", fields)
                DemoDataField("trainingSignature", "Imię i nazwisko / podpis wystawcy", fields)
                DemoDataField("trainingCompletionDate", "Rzeczywista data ukończenia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingHours", "Liczba godzin kształcenia", fields)
                DemoDataField("trainingAssessment", "Zaliczenie / egzamin / wynik", fields)
                DemoDataField("trainingCertificateNumber", "Numer zaświadczenia o ukończeniu", fields)
                DemoDataField("trainingForm", "Forma kształcenia", fields, "stacjonarna / zdalna / hybrydowa")
                Text("Podgląd legitymacji", color = Navy, fontWeight = FontWeight.Bold)
                TrainingParticipantCard(fields = fields, photoBase64 = photoBase64)
                AdditionalInformationSection("Informacje dodatkowe — legitymacja kursanta", fields, "trainingExtra", "Legitymacja instytucji szkoleniowej potwierdza status w ewidencji wystawcy w granicach regulaminu i właściwej podstawy działania. Nie jest legitymacją studencką i sama nie nadaje statusu studenta uczelni.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "student") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja studencka — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("studentUniversity", "Nazwa uczelni", fields)
                DemoDataField("studentUniversityAddress", "Adres uczelni", fields)
                DemoDataField("studentUniversityId", "Identyfikator uczelni / jednostki", fields)
                DemoDataField("studentCardNumber", "Numer legitymacji / numer albumu", fields)
                DemoDataField("studentNames", "Imię i nazwisko studenta zapisane w dokumencie", fields)
                DemoDataField("studentFaculty", "Wydział / jednostka organizacyjna", fields)
                DemoDataField("studentProgramme", "Kierunek studiów", fields)
                DemoDataField("studentLevel", "Poziom studiów", fields, "I stopnia / II stopnia / jednolite magisterskie")
                DemoDataField("studentProfile", "Profil / forma studiów", fields, "ogólnoakademicki / praktyczny; stacjonarne / niestacjonarne")
                DemoDataField("studentYear", "Rok i semestr studiów", fields)
                DemoDataField("studentAcademicYear", "Rok akademicki", fields)
                DemoDataField("studentIssueDate", "Data wydania dokumentu", fields, "DD-MM-RRRR")
                DemoDataField("studentValidity", "Termin ważności / aktualizacji", fields, "DD-MM-RRRR")
                DemoDataField("studentStatus", "Status studenta", fields, "aktywny / urlop / skreślony / ukończony")
                DemoDataField("studentIssuer", "Osoba / jednostka wydająca", fields)
                AdditionalInformationSection("Informacje dodatkowe — legitymacja studencka", fields, "studentExtra", "Ten formularz służy wyłącznie do ewidencji danych. Nie nadaje statusu studenta, nie potwierdza wpisu na listę studentów i nie zastępuje dokumentu ani certyfikatu wydanego przez uczelnię.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "disability") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Orzeczenie o niepełnosprawności — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
                Text("Źródło ustalenia / dokumentu", color = Navy, fontWeight = FontWeight.SemiBold)
                val authorityOptions = listOf(
                    "powiat" to "Zespół powiatowy / miejski",
                    "other_law" to "Orzeczenie na podstawie innych przepisów",
                    "foundation" to "Zespół medyczny fundacji",
                    "medical_opinion" to "Opinia lub zaświadczenie medyczne",
                    "self_declaration" to "Własne oświadczenie"
                )
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    authorityOptions.forEach { (value, label) ->
                        val selected = (fields["disabilityAuthorityType"] ?: "powiat") == value
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(if (selected) BlueLight else Color(0xFFF5F6F7))
                                .border(1.dp, if (selected) Blue else Border, RoundedCornerShape(9.dp))
                                .clickable { fields["disabilityAuthorityType"] = value }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null, tint = if (selected) Blue else Muted
                            )
                            Text(label, modifier = Modifier.padding(start = 9.dp), color = if (selected) Navy else Ink,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                val authorityType = fields["disabilityAuthorityType"] ?: "powiat"
                if (authorityType != "self_declaration") {
                    DemoDataField("disabilityAuthorityName", "Nazwa organu / podmiotu / wystawcy", fields)
                    DemoDataField("disabilityIssuerName", "Imię, nazwisko lub nazwa wystawcy", fields)
                }
                when (authorityType) {
                    "powiat" -> {
                        DemoDataField("disabilityCounty", "Powiat / miasto na prawach powiatu", fields)
                        DemoDataField("disabilityLegalBasis", "Podstawa prawna", fields, "Ustawa z 27.08.1997 r. i przepisy wykonawcze")
                    }
                    "other_law" -> {
                        DemoDataField("disabilityLegalBasis", "Odrębna podstawa prawna", fields)
                        DemoDataField("disabilityLegalProvision", "Przepis określający skutki lub równoważność", fields)
                        DemoDataField("disabilityEquivalentEffect", "Zakres uznania / równoważności", fields)
                    }
                    "foundation" -> {
                        DemoDataField("disabilityFoundationName", "Fundacja / podmiot prowadzący", fields)
                        DemoDataField("disabilityFoundationStatute", "Podstawa statutowa / uchwała fundacji", fields)
                        DemoDataField("disabilityFoundationLegalBasis", "Podstawa prawna i regulacyjna wskazana przez wystawcę", fields)
                    }
                    "medical_opinion" -> {
                        DemoDataField("disabilityMedicalQualification", "Zawód / kwalifikacje osoby wystawiającej", fields)
                        DemoDataField("disabilityLegalBasis", "Podstawa i cel wydania opinii / zaświadczenia", fields)
                    }
                    "self_declaration" -> {
                        DemoDataField("disabilitySelfDeclarationDate", "Data złożenia oświadczenia", fields, "DD-MM-RRRR")
                        DemoDataField("disabilitySelfDeclarationBasis", "Podstawa i cel oświadczenia", fields)
                        Text("Oświadczenie własne zapisuje deklarację osoby. Samo w sobie nie jest orzeczeniem ustawowym; jego znaczenie zależy od przepisu lub procedury, w której jest składane.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Dane osoby i rozstrzygnięcia", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / rozstrzygnięcia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                DemoDataField("disabilityPersonName", "Osoba, której dotyczy orzeczenie", fields, "Imię i nazwisko")
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator ewidencyjny", fields)
                Text("Ustalony status / stopień", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityCategory", "Kategoria", fields, "np. osoba do 16 lat / osoba powyżej 16 lat")
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki / nie dotyczy")
                DemoDataField("disabilityCauseCodes", "Symbole przyczyn (maks. 3 w orzeczeniu powiatowym)", fields, "np. 05-R, 10-N")
                DemoDataField("disabilityValidity", "Okres, na jaki ustalono status", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data, od której ustalono niepełnosprawność", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityEmployment", "Wskazania dotyczące zatrudnienia", fields)
                DemoDataField("disabilityTraining", "Wskazania dotyczące szkolenia", fields)
                DemoDataField("disabilityTherapy", "Terapia zajęciowa / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Przedmioty ortopedyczne / środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Środowiskowy system wsparcia", fields)
                DemoDataField("disabilityParking", "Wskazania dotyczące karty parkingowej", fields)
                Text("Skład i podpisy", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityMedicalAssessment", "Lekarz / osoba dokonująca oceny", fields)
                DemoDataField("disabilityPanelMembers", "Skład zespołu / osoby uczestniczące w ocenie", fields)
                DemoDataField("disabilityChairperson", "Przewodniczący zespołu", fields)
                DemoDataField("disabilitySignature", "Osoba podpisująca / zatwierdzająca", fields)
                DemoDataField("disabilityAppealInfo", "Tryb odwołania / weryfikacji", fields)
                DemoDataField("disabilityApplicationDate", "Data złożenia wniosku", fields, "DD-MM-RRRR")
                DemoDataField("disabilityCaseNumber", "Numer sprawy", fields)
                DemoDataField("disabilityDeliveryDate", "Data doręczenia rozstrzygnięcia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionType", "Rodzaj rozstrzygnięcia", fields)
                DemoDataField("disabilityAppealOutcome", "Wynik odwołania / sprzeciwu", fields)
                DemoDataField("disabilityDuplicateInfo", "Duplikat / zmiana dokumentu", fields)
                DemoDataField("disabilitySourceAttachment", "Nazwa / opis dokumentu źródłowego", fields)
                Text("Podgląd informacyjny", color = Navy, fontWeight = FontWeight.Bold)
                DisabilityDecisionPreview(fields)
                AdditionalInformationSection("Informacje dodatkowe — orzeczenie / zaświadczenie", fields, "disabilityExtra", when (fields["disabilityAuthorityType"] ?: "powiat") {
                    "foundation" -> "Dokument fundacji opisuje się zgodnie z rzeczywistą podstawą działania, kwalifikacjami wystawców i zakresem przeznaczenia. Nie przypisujemy mu automatycznie skutków orzeczenia ustawowego."
                    "other_law" -> "Skutki dokumentu z odrębnego systemu wynikają z konkretnego przepisu. Należy wskazać podstawę i zakres równoważności."
                    "medical_opinion" -> "Opinia lub zaświadczenie medyczne dokumentuje ocenę lub stan zdrowia w granicach kompetencji wystawcy; nie jest automatycznie orzeczeniem o stopniu niepełnosprawności."
                    "self_declaration" -> "Oświadczenie własne jest deklaracją osoby. Jego znaczenie zależy od procedury, w której jest składane; samo nie ustanawia ustawowego stopnia niepełnosprawności."
                    else -> "Karta ewidencyjna prezentuje dane wpisane przez użytkownika. Nie weryfikuje autentyczności dokumentu i nie zastępuje oryginału."
                })
            }
        }
        }
        if (categoryId == "all" || categoryId == "passport") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dane paszportowe", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("passportNumber", "Numer paszportu", fields)
                DemoDataField("passportCountry", "Państwo wydania", fields)
                DemoDataField("passportIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("passportExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("passportIssuer", "Organ wydający", fields)
                DemoDataField("passportType", "Rodzaj dokumentu podróży", fields, "zwykły / tymczasowy / inny")
                DemoDataField("passportHolderNames", "Imiona i nazwisko zapisane w paszporcie", fields)
                DemoDataField("passportBirthPlace", "Miejsce urodzenia", fields)
                DemoDataField("passportCitizenship", "Obywatelstwo w dokumencie", fields)
                DemoDataField("passportSex", "Płeć w dokumencie", fields)
                DemoDataField("passportStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("passportLossDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                AdditionalInformationSection("Informacje dodatkowe — paszport", fields, "passportExtra", "Jest to ewidencja danych wpisanych przez użytkownika, a nie paszport ani jego cyfrowy odpowiednik. Nie odtwarzamy strefy MRZ, kodów ani zabezpieczeń dokumentu.")
            }
        }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Konfiguracja i zapis", color = Navy, fontWeight = FontWeight.Bold)
                Text("Dane i zdjęcie są szyfrowane lokalnie na tym urządzeniu. Nie są wysyłane do sieci.", color = Muted, style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.Button(
                    onClick = {
                        runCatching {
                            val snapshot = fields.toMutableMap()
                            snapshot["photo"] = photoBase64
                            DemoIdentityStore.save(context, snapshot)
                        }.onSuccess { saveMessage = "Zapisano lokalnie i zaszyfrowano dane oraz zdjęcie." }
                            .onFailure { saveMessage = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz dane i zdjęcie") }
                if (saveMessage.isNotBlank()) Text(saveMessage, color = Blue, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            "Zapis odbywa się w prywatnym katalogu aplikacji, z użyciem klucza Android Keystore. Odinstalowanie aplikacji usuwa lokalne dane. Zachowaj ostrożność: są to dane wrażliwe.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RodowyDocumentPreview(fields: Map<String, String>, photoBase64: String) {
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 600 || bounds.outHeight / sample > 600) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val displayName = listOf(fields["firstNames"], fields["lastName"])
        .filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "IMIĘ I NAZWISKO" }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.586f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EDF0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF254B50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Znak ewidencji rodowej", tint = White)
                    }
                    Column(Modifier.padding(start = 8.dp)) {
                        Text("DOKUMENT RODOWY", color = Color(0xFF254B50), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                        Text("DOKUMENT TOŻSAMOŚCI • EWIDENCJA PRYWATNA", color = Color(0xFF455A64), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Row(
                    modifier = Modifier.weight(1f).padding(top = 8.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.width(76.dp).fillMaxSize()
                            .clip(RoundedCornerShape(7.dp)).background(Color(0xFFD2DDE0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoBitmap != null) {
                            Image(photoBitmap.asImageBitmap(), contentDescription = "Fotografia w rodowym dokumencie", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Miejsce na fotografię", tint = Color(0xFF607D83), modifier = Modifier.size(44.dp))
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("IMIĘ I NAZWISKO", color = Color(0xFF52666B), style = MaterialTheme.typography.labelSmall)
                        Text(displayName, color = Color(0xFF172D32), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("DATA URODZENIA", color = Color(0xFF52666B), style = MaterialTheme.typography.labelSmall)
                        Text(fields["birthDate"].orEmpty().ifBlank { "—" }, color = Color(0xFF172D32), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("NUMER RODOWY", color = Color(0xFF52666B), style = MaterialTheme.typography.labelSmall)
                        Text(fields["familyDocNumber"].orEmpty().ifBlank { "—" }, color = Color(0xFF172D32), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF254B50)).padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Text("DOKUMENT PRYWATNY — NIE JEST DOKUMENTEM PAŃSTWOWYM", color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("Nie zastępuje dowodu osobistego ani paszportu.", color = Color(0xFFE1ECEC), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.586f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EDF0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DOKUMENT RODOWY • REWERS", color = Color(0xFF254B50), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                Text(fields["familyDocNumber"].orEmpty().ifBlank { "NR —" }, color = Color(0xFF254B50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("RODZICE", color = Color(0xFF52666B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Ojciec: " + listOf(fields["fatherNames"], fields["fatherSurname"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "—" }, color = Color(0xFF172D32), style = MaterialTheme.typography.bodySmall)
                Text("Matka: " + listOf(fields["motherNames"], fields["motherSurname"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "—" }, color = Color(0xFF172D32), style = MaterialTheme.typography.bodySmall)
                Text("RÓD I EWIDENCJA", color = Color(0xFF52666B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Ród: " + fields["familyName"].orEmpty().ifBlank { "—" } + " • Linia: " + fields["familyLine"].orEmpty().ifBlank { "—" }, color = Color(0xFF172D32), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Ewidencję prowadzi: " + fields["familyRegistry"].orEmpty().ifBlank { "—" }, color = Color(0xFF172D32), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Miejsce: " + fields["familyRegistryPlace"].orEmpty().ifBlank { "—" }, color = Color(0xFF172D32), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF254B50)).padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Text("DOKUMENT PRYWATNY • EWIDENCJA RODOWA", color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Nie stanowi państwowego dokumentu tożsamości ani dokumentu podróży.", color = Color(0xFFE1ECEC), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    Text(
        "Projekt wykorzystuje czytelny układ karty identyfikacyjnej, ale ma własną nazwę, znak, kolorystykę i oznaczenia. Nie zawiera państwowego godła, numeru CAN, warstwy elektronicznej ani państwowych zabezpieczeń.",
        color = Muted,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun DisabilityDecisionPreview(fields: Map<String, String>) {
    val authorityType = fields["disabilityAuthorityType"] ?: "powiat"
    val isFoundation = authorityType == "foundation"
    val previewTitle = when (authorityType) {
        "other_law" -> "DOKUMENT NA PODSTAWIE ODRĘBNYCH PRZEPISÓW"
        "foundation" -> "DOKUMENT ZESPOŁU MEDYCZNEGO FUNDACJI"
        "medical_opinion" -> "OPINIA / ZAŚWIADCZENIE MEDYCZNE"
        "self_declaration" -> "OŚWIADCZENIE WŁASNE"
        else -> "DANE ORZECZENIA POWIATOWEGO"
    }
    val person = fields["disabilityPersonName"].orEmpty().ifBlank { "OSOBA, KTÓREJ DOTYCZY WPIS" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isFoundation) Color(0xFFF0EBF7) else Color(0xFFE8F0F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                previewTitle,
                color = if (isFoundation) Color(0xFF51416E) else Navy,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(fields["disabilityAuthorityName"].orEmpty().ifBlank {
                when (authorityType) { "foundation" -> "ZESPÓŁ MEDYCZNY FUNDACJI"; "other_law" -> "ORGAN WSKAZANY W ODRĘBNYCH PRZEPISACH"; "medical_opinion" -> "OSOBA WYKONUJĄCA ZAWÓD MEDYCZNY"; "self_declaration" -> "OŚWIADCZENIE OSOBY"; else -> "POWIATOWY ZESPÓŁ DO SPRAW ORZEKANIA" }
            }, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(person, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Nr: " + fields["disabilityDecisionNumber"].orEmpty().ifBlank { "—" } +
                "   Data: " + fields["disabilityDecisionDate"].orEmpty().ifBlank { "—" },
                color = Muted, style = MaterialTheme.typography.bodySmall)
            Text("Stopień / kategoria: " + fields["disabilityDegree"].orEmpty().ifBlank {
                fields["disabilityCategory"].orEmpty().ifBlank { "—" }
            }, color = Ink, style = MaterialTheme.typography.bodyMedium)
            Text("Przyczyny / symbole: " + fields["disabilityCauseCodes"].orEmpty().ifBlank { "—" },
                color = Ink, style = MaterialTheme.typography.bodySmall)
            Text("Okres: " + fields["disabilityValidity"].orEmpty().ifBlank { "—" },
                color = Ink, style = MaterialTheme.typography.bodySmall)
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                    .background(if (isFoundation) Color(0xFF51416E) else Color(0xFF315A78))
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            ) {
                Text(
                    when (authorityType) { "foundation" -> "DOKUMENT WYSTAWCY • ZAKRES SKUTKÓW WEDŁUG PODSTAWY"; "other_law" -> "PODSTAWA I SKUTKI WEDŁUG ODRĘBNYCH PRZEPISÓW"; "medical_opinion" -> "OPINIA MEDYCZNA • NIE JEST AUTOMATYCZNIE ORZECZENIEM"; "self_declaration" -> "OŚWIADCZENIE OSOBY • DEKLARACJA WŁASNA"; else -> "PODGLĄD EWIDENCYJNY • NIE ZASTĘPUJE ORYGINAŁU" },
                    color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun TrainingParticipantCard(fields: Map<String, String>, photoBase64: String) {
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val participantName = listOf(fields["firstNames"], fields["lastName"])
        .filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "IMIĘ I NAZWISKO UCZESTNIKA" }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.586f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(11.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(29.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFF39466F)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Description, contentDescription = "Znak instytucji szkoleniowej", tint = White)
                }
                Column(Modifier.weight(1f).padding(start = 7.dp)) {
                    Text(
                        fields["trainingInstitution"].orEmpty().ifBlank { "INSTYTUCJA SZKOLENIOWA" },
                        color = Color(0xFF26345D),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("LEGITYMACJA UCZESTNIKA KURSU", color = Color(0xFF4C587B), style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(
                modifier = Modifier.weight(1f).padding(top = 7.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier.width(66.dp).fillMaxSize().clip(RoundedCornerShape(6.dp)).background(Color(0xFFD5D9E8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(photoBitmap.asImageBitmap(), contentDescription = "Fotografia uczestnika kursu", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Miejsce na fotografię", tint = Color(0xFF66729A), modifier = Modifier.size(40.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("UCZESTNIK", color = Color(0xFF596487), style = MaterialTheme.typography.labelSmall)
                    Text(participantName, color = Color(0xFF202B4C), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("KURS", color = Color(0xFF596487), style = MaterialTheme.typography.labelSmall)
                    Text(fields["trainingCourseName"].orEmpty().ifBlank { "—" }, color = Color(0xFF202B4C), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "NR " + fields["trainingCardNumber"].orEmpty().ifBlank { "—" } +
                            "   WAŻNA DO " + fields["trainingValidUntil"].orEmpty().ifBlank { "—" },
                        color = Color(0xFF35436D),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF39466F)).padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Text("DOKUMENT INSTYTUCJI SZKOLENIOWEJ • NIE JEST LEGITYMACJĄ STUDENCKĄ", color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Potwierdza wyłącznie status w ewidencji wystawcy.", color = Color(0xFFE7EAF5), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


@Composable
private fun AdditionalInformationSection(
    title: String,
    fields: MutableMap<String, String>,
    prefix: String,
    explanation: String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Blue)
                Text(title, modifier = Modifier.weight(1f).padding(start = 9.dp),
                    color = Navy, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "Zwiń" else "Rozwiń", color = Blue,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                Text(explanation, color = Ink, style = MaterialTheme.typography.bodySmall)
                DemoDataField("${prefix}Source", "Źródło danych / dokument źródłowy", fields)
                DemoDataField("${prefix}LegalBasis", "Podstawa prawna / regulaminowa", fields)
                DemoDataField("${prefix}VerifiedAt", "Data ostatniej weryfikacji danych", fields, "DD-MM-RRRR")
                DemoDataField("${prefix}VerifiedBy", "Podmiot / osoba potwierdzająca dane", fields)
                DemoDataField("${prefix}Notes", "Adnotacje i informacje uzupełniające", fields)
                Text(
                    "Informacje dodatkowe są ewidencją użytkownika. Aplikacja nie potwierdza autentyczności dokumentu ani kompetencji wystawcy.",
                    color = Muted, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DemoDataField(key: String, label: String, fields: MutableMap<String, String>, placeholder: String = "Wpisz dane") {
    OutlinedTextField(
        value = fields[key] ?: "",
        onValueChange = { fields[key] = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true
    )
}

private object DemoIdentityStore {
    private const val KEY_ALIAS = "obywatel_demo_identity_aes"
    private const val FILE_NAME = "identity_profile.enc"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun load(context: Context): Map<String, String> = runCatching {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()
        val encrypted = file.readBytes()
        require(encrypted.size > 12) { "Nieprawidłowy zaszyfrowany plik" }
        val iv = encrypted.copyOfRange(0, 12)
        val ciphertext = encrypted.copyOfRange(12, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONObject(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        val result = mutableMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = json.optString(key, "")
        }
        result
    }.getOrElse { emptyMap() }

    fun save(context: Context, values: Map<String, String>) {
        val json = JSONObject()
        values.forEach { (key, value) -> json.put(key, value) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temporary = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        temporary.writeBytes(encrypted)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}

@Composable
private fun BottomNavigation(selectedTab: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Pulpit", Icons.Filled.Home, 0),
        Triple("Dokumenty", null, 1),
        Triple("Usługi", null, 2),
        Triple("Kod QR", Icons.Filled.QrCode2, 3),
        Triple("Więcej", null, 4)
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(White)
            .border(width = 0.5.dp, color = Border)
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, icon, index) ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(index) }.padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (index) {
                    1 -> Image(painterResource(R.drawable.wallet), contentDescription = label, modifier = Modifier.size(23.dp), contentScale = ContentScale.Fit)
                    2 -> Image(painterResource(R.drawable.public_services), contentDescription = label, modifier = Modifier.size(23.dp), contentScale = ContentScale.Fit)
                    4 -> Image(painterResource(R.drawable.profile), contentDescription = label, modifier = Modifier.size(23.dp), contentScale = ContentScale.Fit)
                    else -> Icon(icon!!, contentDescription = label, tint = if (selectedTab == index) Blue else Muted, modifier = Modifier.size(23.dp))
                }
                Text(label, color = if (selectedTab == index) Blue else Muted, style = MaterialTheme.typography.labelSmall, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
            }
        }
    }
}
