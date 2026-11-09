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
    val tint: Color = Blue
)

private val services = listOf(
    ServiceItem("Dokumenty", "Portfel demonstracyjny", R.drawable.mob_document_check),
    ServiceItem("Polak za granicą", "Informacje konsularne", R.drawable.mob_abroad),
    ServiceItem("Płatności", "Usługi i opłaty", R.drawable.mob_payments),
    ServiceItem("Zdrowie", "Informacje zdrowotne", R.drawable.mob_health),
    ServiceItem("Transport", "Podróże i przejazdy", R.drawable.mob_transport),
    ServiceItem("Bilety", "Bilety i przejazdy", R.drawable.mob_ticket),
    ServiceItem("Bezpieczeństwo", "Prywatność i ochrona", R.drawable.mob_cyber_safety),
    ServiceItem("Środowisko", "Informacje środowiskowe", R.drawable.mob_environment),
    ServiceItem("Ustawienia", "Preferencje aplikacji", fallbackIcon = Icons.Filled.Settings),
    ServiceItem("Pozostałe", "Więcej modułów", fallbackIcon = Icons.Filled.MoreHoriz)
)

@Composable
private fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDemoDocuments by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        if (showDemoDocuments) {
            DemoDocumentsScreen(onBack = { showDemoDocuments = false })
        } else {
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
                        items(services) { service ->
                            ServiceCard(service) {
                                if (service.title == "Dokumenty") showDemoDocuments = true
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                BottomNavigation(selectedTab = selectedTab, onSelect = { selectedTab = it })
            }
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
private fun DemoDocumentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var photoBase64 by remember { mutableStateOf(fields["photo"] ?: "") }
    var saveMessage by remember { mutableStateOf("") }
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
            text = "‹  Powrót do usług",
            color = Blue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            "Dane i identyfikatory — demonstracja",
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
            }
        }
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
                DemoDataField("familySource", "Podstawa wpisu / dokument źródłowy", fields)
                Text("Wizualizacja dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                RodowyDocumentPreview(fields = fields)
            }
        }
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
                Text(
                    "Pola mają charakter przykładowy. Formularz nie generuje ani nie odwzorowuje dowodu osobistego.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
                Text("Podstawa konstytucyjna i charakter reprezentacji", color = Navy, fontWeight = FontWeight.Bold)
                Text(
                    "Art. 104 ust. 1 Konstytucji RP: „Posłowie są przedstawicielami Narodu. Nie wiążą ich instrukcje wyborców.” Przepis ustanawia zasadę wolnego mandatu i reprezentacji Narodu.",
                    color = Ink,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Art. 104 ust. 2 Konstytucji RP: przed rozpoczęciem sprawowania mandatu posłowie składają ślubowanie przed Sejmem. Odmowa złożenia ślubowania oznacza zrzeczenie się mandatu (art. 104 ust. 3).",
                    color = Ink,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Dane dotyczące ślubowania — oświadczenie własne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("mpOathDate", "Data złożenia ślubowania", fields, "DD-MM-RRRR")
                DemoDataField("mpOathPlace", "Miejsce / okoliczności wypowiedzenia ślubowania", fields)
                DemoDataField("mpOathEvidence", "Źródło potwierdzenia (np. protokół, nagranie, publikacja)", fields)
                Text(
                    "Ważne: Konstytucja wymaga złożenia ślubowania „przed Sejmem”; nie stanowi, że ma ono nastąpić przed budynkiem Sejmu. Jeżeli osoba składająca oświadczenie wypowiedziała rotę przed budynkiem, można opisać to jako okoliczność faktyczną i wskazać źródło. Sam wpis nie potwierdza uzyskania ani trwania mandatu.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Formularz nie stanowi legitymacji, nie potwierdza mandatu ani prawa do reprezentowania Sejmu.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
                Text(
                    "Nie jest to paszport ani jego cyfrowy odpowiednik. Nie dodano strefy MRZ, kodu QR, kodu kreskowego ani elementów zabezpieczających.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
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
private fun RodowyDocumentPreview(fields: Map<String, String>) {
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
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Miejsce na fotografię", tint = Color(0xFF607D83), modifier = Modifier.size(44.dp))
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
    Text(
        "Projekt wykorzystuje czytelny układ karty identyfikacyjnej, ale ma własną nazwę, znak, kolorystykę i oznaczenia. Nie zawiera państwowego godła, numeru CAN, warstwy elektronicznej ani państwowych zabezpieczeń.",
        color = Muted,
        style = MaterialTheme.typography.bodySmall
    )
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
        Triple("Start", Icons.Filled.Home, 0),
        Triple("Usługi", Icons.Filled.Fingerprint, 1),
        Triple("Profil", Icons.Filled.AccountCircle, 2)
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
