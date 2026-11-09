// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.HybridBinarizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Vaccines
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
import androidx.compose.material3.RadioButton
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
import pl.gov.obywatel.android.PublicServicesBrowserScreen

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
    ServiceItem("Przeglądarka usług publicznych", "Katalog usług gov.pl, mObywatel i administracji publicznej", fallbackIcon = Icons.Filled.Public, category = "Sprawy urzędowe"),
    ServiceItem("Odpis z rejestru podmiotów", "Dane spółki, fundacji, kościoła i instytucji szkoleniowej", fallbackIcon = Icons.Filled.AccountBalance, category = "Rejestry"),
    ServiceItem("Polak za granicą", "Informacje konsularne", R.drawable.mob_abroad, category = "Transport"),
    ServiceItem("Płatności", "Usługi i opłaty", R.drawable.mob_payments, category = "Sprawy urzędowe"),
    ServiceItem("Zdrowie", "Informacje zdrowotne", R.drawable.mob_health, category = "Zdrowie"),
    ServiceItem("Historia chorób", "Prywatne archiwum zdjęć i dokumentacji medycznej", fallbackIcon = Icons.Filled.LocalHospital, category = "Zdrowie"),
    ServiceItem("Dane dzieci", "Rodzinny rejestr danych i zdjęć dzieci", fallbackIcon = Icons.Filled.FamilyRestroom, category = "Rodzina"),
    ServiceItem("eKarta Szczepień", "Prywatna karta szczepień dziecka", fallbackIcon = Icons.Filled.Vaccines, category = "Zdrowie"),
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
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<Map<String, String>?>(null) }
    var importStatus by remember { mutableStateOf("") }
    var exportSnapshot by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val exportVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/vcard")) { uri ->
        if (uri != null) runCatching {
            require(exportSnapshot.isNotEmpty()) { "Brak zapisanych danych do eksportu." }
            val payload = VCardCodec.encodeArchive(exportSnapshot)
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: error("Android nie udostępnił strumienia zapisu dla wybranego pliku.")
            output.use {
                it.write(bytes)
                it.flush()
            }
            val savedBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Nie można ponownie odczytać zapisanego pliku vCard.")
            require(savedBytes.isNotEmpty()) { "Zapisany plik vCard jest pusty." }
            require(savedBytes.contentEquals(bytes)) {
                "Weryfikacja zapisu nie powiodła się: zawartość pliku różni się od eksportowanych danych."
            }
            "Zapisano ${exportSnapshot.size} pól (${bytes.size} bajtów) do pliku vCard."
        }.onSuccess { importStatus = it }
            .onFailure { importStatus = "Błąd eksportu: ${it.message ?: "nieznany błąd"}" }
        else {
            importStatus = "Eksport anulowany — plik nie został zapisany."
        }
    }
    val importVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 10 * 1024 * 1024) { "Plik przekracza limit 10 MB." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: error("Nie można otworzyć wybranego pliku.")
            require(bytes.isNotEmpty()) { "Wybrany plik jest pusty." }
            val raw = runCatching {
                Charsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            }.getOrElse {
                String(bytes, java.nio.charset.Charset.forName("windows-1250"))
            }
            val imported = VCardCodec.decodeForImport(raw)
            require(imported.isNotEmpty()) { "Archiwum nie zawiera danych." }
            imported
        }.onSuccess {
            pendingImport = it
            importStatus = "Plik odczytany i poprawny. Sprawdź zakres danych przed zatwierdzeniem."
        }.onFailure {
            pendingImport = null
            importStatus = "Błąd importu: ${it.message ?: "nieprawidłowy plik"}"
        }
        else {
            importStatus = "Import anulowany — dane lokalne pozostają bez zmian."
        }
    }
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
    val favoriteServices = remember { mutableStateMapOf("Zastrzeż PESEL" to true, "Zdrowie" to true, "Transport" to true) }

    fun handleSystemBack() {
        when {
            showDocumentsEditor -> { showDocumentsEditor = false; showSettingsMenu = true }
            showSettingsMenu -> showSettingsMenu = false
            selectedDocumentId != null -> selectedDocumentId = null
            showDemoDocuments -> showDemoDocuments = false
            showDisabilitySettings -> { showDisabilitySettings = false; showSettingsMenu = true }
            showCertificates -> showCertificates = false
            activeService != null -> activeService = null
            selectedTab == 1 || selectedTab == 2 || selectedTab == 3 || selectedTab == 4 -> selectedTab = 0
            // Na ekranie głównym pozostawiamy domyślne zachowanie Androida.
        }
    }

    BackHandler(
        enabled = showDocumentsEditor || showSettingsMenu || selectedDocumentId != null ||
            showDemoDocuments || showDisabilitySettings || showCertificates ||
            activeService != null || selectedTab != 0,
        onBack = ::handleSystemBack
    )

    fun openService(title: String) {
        ActivityHistoryStore.record(context, "Nawigacja", "Otwarto moduł: $title")
        when (title) {
            "Portfel" -> showDemoDocuments = true
            "Dane dzieci", "eKarta Szczepień", "Historia chorób" -> activeService = title
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
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true },
                        onExportAll = {
                            exportSnapshot = DemoIdentityStore.load(context)
                            runCatching {
                                require(exportSnapshot.isNotEmpty()) {
                                    "Brak zapisanych danych. Najpierw uzupełnij i zapisz dane w ustawieniach."
                                }
                                VCardCodec.encodeArchive(exportSnapshot)
                            }.onSuccess { exportVCardLauncher.launch("obywatel-archiwum.vcf") }
                                .onFailure { importStatus = "Nie można przygotować eksportu: ${it.message}" }
                        },
                        onImportAll = { importVCardLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) },
                        importStatus = importStatus
                    )
                    selectedDocumentId == "official_documents" -> OfficialDocumentsScreen(onBack = { selectedDocumentId = null }, readOnly = true)
                    selectedDocumentId == "family_children" -> FamilyHealthRecordsScreen(initialSection = "children", onBack = { selectedDocumentId = null })
                    selectedDocumentId == "family_vaccinations" -> FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { selectedDocumentId = null })
                    selectedDocumentId != null -> DocumentDetailsScreen(
                        documentId = selectedDocumentId!!,
                        onBack = { selectedDocumentId = null },
                        onOpenService = { title -> selectedDocumentId = null; activeService = title }
                    )
                    showDemoDocuments -> DocumentsWalletScreen(
                        onBack = { showDemoDocuments = false },
                        onOpenDetails = { selectedDocumentId = it }
                    )
                    showDisabilitySettings -> DisabilitySettingsScreen(onBack = { showDisabilitySettings = false; showSettingsMenu = true })
                    showCertificates -> CertificatesScreen(onBack = { showCertificates = false })
                    activeService == "Dane dzieci" -> FamilyHealthRecordsScreen(initialSection = "children", onBack = { activeService = null })
                    activeService == "eKarta Szczepień" -> FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { activeService = null })
                    activeService == "Historia chorób" -> MedicalHistoryRecordsScreen(onBack = { activeService = null })
                    activeService == "Historia działań" -> ActivityHistoryScreen(onBack = { activeService = null })
                    activeService == "Formularze dokumentów" -> OfficialDocumentsScreen(onBack = { activeService = null })
                    activeService == "Dokumenty Rodowe" -> FamilyEntityDocumentsScreen(entityMode = false, onBack = { activeService = null })
                    activeService == "Dokumenty rejestru podmiotów" -> FamilyEntityDocumentsScreen(entityMode = true, onBack = { activeService = null })
                    activeService == "Dokumenty Kościelne" -> ChurchDocumentsScreen(onBack = { activeService = null })
                    activeService == "Zasoby identyfikacji dokumentów" -> DocumentBrandAssetsScreen(onBack = { activeService = null })
                    activeService == "Dokumenty PDF i podpisy elektroniczne" -> SignedPdfDocumentsScreen(onBack = { activeService = null })
                    activeService == "Podgląd dokumentów podpisanych" -> SignedPdfDocumentsScreen(onBack = { activeService = null }, allowImport = false)
                    activeService == "Prawo wykonywania zawodu i kwalifikacje" -> ProfessionalQualificationsScreen(onBack = { activeService = null })
                    activeService == "Odpis z rejestru podmiotów" -> EntityRegistryExtractScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Przeglądarka usług publicznych" -> PublicServicesBrowserScreen(onBack = { activeService = null })
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
                    selectedTab == 4 -> MoreMenuScreen(
                        onOpenSettings = {
                            ActivityHistoryStore.record(context, "Ustawienia", "Otwarto ustawienia")
                            showSettingsMenu = true
                        },
                        onOpenCertificates = {
                            ActivityHistoryStore.record(context, "Certyfikaty", "Otwarto rejestr certyfikatów i zaświadczeń")
                            showCertificates = true
                        },
                        onOpenHelp = { activeService = "Pomoc i informacje" },
                        onOpenHistory = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto historię działań")
                            activeService = "Historia działań"
                        },
                        onOpenSignedPdfs = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto dokumenty PDF i podpisy elektroniczne")
                            activeService = "Dokumenty PDF i podpisy elektroniczne"
                        },
                        onOpenProfessionalQualifications = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto rejestr prawa wykonywania zawodu i kwalifikacji")
                            activeService = "Prawo wykonywania zawodu i kwalifikacje"
                        },
                        onOpenOfficialDocuments = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto formularze dokumentów urzędowych i ubezpieczeniowych")
                            activeService = "Formularze dokumentów"
                        },
                        onOpenFamilyDocuments = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto dokumenty Rodowe")
                            activeService = "Dokumenty Rodowe"
                        },
                        onOpenEntityDocuments = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto dokumenty rejestru podmiotów")
                            activeService = "Dokumenty rejestru podmiotów"
                        },
                        onOpenChurchDocuments = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto Dokumenty Kościelne")
                            activeService = "Dokumenty Kościelne"
                        },
                        onOpenDocumentBrandAssets = {
                            ActivityHistoryStore.record(context, "Nawigacja", "Otwarto zasoby identyfikacji dokumentów")
                            activeService = "Zasoby identyfikacji dokumentów"
                        }
                    )
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (selectedTab == 0) Header(
                                onOpenSettings = {
                                    showSettingsMenu = true
                                    showDemoDocuments = false
                                    selectedDocumentId = null
                                },
                                onOpenNotifications = { activeService = "Powiadomienia" }
                            )
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp)) {
                                Spacer(Modifier.height(18.dp))
                                if (selectedTab == 0) {
                                    SectionHeading("Dokumenty", "Lokalny portfel dokumentów")
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text("Dodaj", color = Blue, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { selectedTab = 4; showSettingsMenu = true }.padding(vertical = 8.dp))
                                        Text("Wszystkie", color = Blue, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { selectedTab = 1 }.padding(vertical = 8.dp))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf(
                                            Triple("Dowód tożsamości", R.drawable.documents_identity_card, "1"),
                                            Triple("Dane paszportowe", R.drawable.documents_passport, "2"),
                                            Triple("Dane rejestru podmiotów", R.drawable.documents_family_record, "registry"),
                                            Triple("Legitymacje", R.drawable.documents_student_legitimacy, "6"),
                                            Triple("Dokumenty podpisane elektronicznie", R.drawable.documents_medical_certificates, "signedpdf")
                                        ).forEach { (label, imageRes, id) ->
                                            Card(modifier = Modifier.width(150.dp).clickable {
                                                when (id) {
                                                    "registry" -> activeService = "Odpis z rejestru podmiotów"
                                                    "signedpdf" -> activeService = "Podgląd dokumentów podpisanych"
                                                    else -> selectedDocumentId = id
                                                }
                                            },
                                                colors = CardDefaults.cardColors(containerColor = White)) {
                                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                                    Box(
                                                        modifier = Modifier.fillMaxWidth().height(88.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = when (id) {
                                                                "1" -> Icons.Filled.CreditCard
                                                                "2" -> Icons.Filled.Public
                                                                "registry" -> Icons.Filled.AccountBalance
                                                                "signedpdf" -> Icons.Filled.Shield
                                                                else -> Icons.Filled.Badge
                                                            },
                                                            contentDescription = null,
                                                            tint = Blue,
                                                            modifier = Modifier.size(52.dp)
                                                        )
                                                    }
                                                    Text(label, color = Navy, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(18.dp))
                                    SectionHeading("Usługi", "Szybki dostęp do wybranych funkcji")
                                    Text("Wszystkie usługi", color = Blue, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { selectedTab = 2 }.padding(vertical = 8.dp))
                                } else {
                                    CivicIllustrationBanner(
                                        imageRes = R.drawable.header_poland_banner,
                                        description = "Baner informacyjny Polski"
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    SectionHeading("Usługi", "Katalog usług według kategorii")
                                    Spacer(Modifier.height(12.dp))
                                }
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
                                        listOf("Wszystkie", "Zdrowie", "Kierowca i pojazdy", "Sprawy urzędowe", "Rodzina", "Edukacja", "Praca", "Finanse i podatki", "Transport", "Nieruchomości", "Środowisko", "Bezpieczeństwo", "Prawo i obywatel", "Kultura i dziedzictwo", "Sport", "Organizacje społeczne", "Kościół i wspólnoty", "Rejestry", "Dokumenty", "Podróże", "Pozostałe")
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
                                } else services.filter { favoriteServices[it.title] == true }.take(4)
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
                                            ServiceCard(
                                                service,
                                                isFavorite = favoriteServices[service.title] == true,
                                                onFavorite = { favoriteServices[service.title] = favoriteServices[service.title] != true },
                                                onClick = { openService(service.title) }
                                            )
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
                    ActivityHistoryStore.record(context, "Nawigacja", "Otwarto zakładkę: " + listOf("Pulpit", "Dokumenty", "Usługi", "Kod QR", "Więcej").getOrElse(it) { "Inna" })
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
        pendingImport?.let { incoming ->
            AlertDialog(
                onDismissRequest = { pendingImport = null },
                title = { Text("Import danych vCard") },
                text = {
                    val current = remember { DemoIdentityStore.load(context) }
                    val overwritten = incoming.keys.count { current.containsKey(it) }
                    val added = incoming.size - overwritten
                    Text(
                        "Plik zawiera ${incoming.size} pól. Nowe: ${added}; zastąpią istniejące: ${overwritten}. " +
                            "Dane zostaną połączone z lokalnymi. Po zapisie aplikacja ponownie odczyta profil i porówna wszystkie wartości."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        runCatching {
                            val current = DemoIdentityStore.loadStrict(context)
                            val merged = current.toMutableMap().apply { putAll(incoming) }
                            DemoIdentityStore.save(context, merged)
                            val persisted = DemoIdentityStore.loadStrict(context)
                            require(persisted == merged) {
                                "Weryfikacja zapisu nie powiodła się. Dane lokalne nie odpowiadają importowi."
                            }
                            val changed = incoming.keys.count { persisted[it] == incoming[it] }
                            require(changed == incoming.size) {
                                "Weryfikacja importu wykazała niezgodność części pól."
                            }
                            "Zaimportowano i zweryfikowano ${changed} pól. Łącznie zapisano ${persisted.size} pól."
                        }.onSuccess { importStatus = it }
                            .onFailure { importStatus = "Błąd importu lub weryfikacji: ${it.message ?: "nieznany błąd"}" }
                        pendingImport = null
                    }) { Text("Importuj i zweryfikuj") }
                },
                dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Anuluj") } }
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
    var activeGenerator by remember { mutableStateOf("owd") }
    BackHandler(enabled = activeGenerator != "owd") { activeGenerator = "owd" }
    when (activeGenerator) {
        "vcard" -> Column(Modifier.fillMaxSize()) { QrSubscreenHeader("Wróć do generatorów") { activeGenerator = "owd" }; Box(Modifier.weight(1f)) { VCardQrScreen() } }
        "url" -> Column(Modifier.fillMaxSize()) { QrSubscreenHeader("Wróć do generatorów") { activeGenerator = "owd" }; Box(Modifier.weight(1f)) { WebsiteQrScreen() } }
        else -> OwdHubScreen(onOpenVCard = { activeGenerator = "vcard" }, onOpenWebsite = { activeGenerator = "url" })
    }
}

@Composable
private fun QrSubscreenHeader(label: String, onBack: () -> Unit) {
    Text("‹  $label", color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 20.dp, vertical = 8.dp))
}

private const val QR_BRAND_NONE = "none"
private const val QR_BRAND_STATE = "state"
private const val QR_BRAND_FILE = "file"

@Composable
private fun QrBrandingControls(branding: String, onBrandingChange: (String) -> Unit, logoLoaded: Boolean, onChooseLogo: () -> Unit, onClearLogo: () -> Unit) {
    Text("Znak graficzny obok kodu QR", color = Navy, fontWeight = FontWeight.Bold)
    listOf(QR_BRAND_STATE to "Godło państwowe", QR_BRAND_FILE to "Logo z pliku", QR_BRAND_NONE to "Bez znaku").forEach { (value, label) ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onBrandingChange(value) }) {
            RadioButton(selected = branding == value, onClick = { onBrandingChange(value) })
            Text(label, color = Ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (branding == QR_BRAND_FILE) {
        OutlinedButton(onClick = onChooseLogo, modifier = Modifier.fillMaxWidth()) { Text(if (logoLoaded) "Zmień załączone logo" else "Wybierz logo z pliku") }
        if (logoLoaded) OutlinedButton(onClick = onClearLogo, modifier = Modifier.fillMaxWidth()) { Text("Usuń załączone logo") }
        Text("Logo jest wyświetlane poza kodem QR. Żaden jego fragment nie zasłania modułów kodu.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

private fun decodeLoyaltyCode(bitmap: Bitmap): String? = runCatching {
    val width = bitmap.width
    val height = bitmap.height
    require(width > 0 && height > 0 && width.toLong() * height.toLong() <= 12_000_000L)
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels)))).text
}.getOrNull()

private fun generateLoyaltyCodeBitmap(value: String, format: BarcodeFormat): Bitmap? {
    if (value.isBlank()) return null
    return runCatching {
        val width = if (format == BarcodeFormat.QR_CODE) 480 else 900
        val height = if (format == BarcodeFormat.QR_CODE) 480 else 220
        val matrix = MultiFormatWriter().encode(value, format, width, height,
            mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8"))
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (y in 0 until height) for (x in 0 until width) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }.getOrNull()
}

@Composable
private fun LoyaltyGeneratedCodes(barcodeValue: String, qrValue: String) {
    val barcode = remember(barcodeValue) { generateLoyaltyCodeBitmap(barcodeValue, BarcodeFormat.CODE_128) }
    val qr = remember(qrValue) { generateLoyaltyCodeBitmap(qrValue, BarcodeFormat.QR_CODE) }
    if (barcode != null || qr != null) {
        Text("Wygenerowane kody", color = Navy, fontWeight = FontWeight.SemiBold)
        if (barcode != null) Image(barcode.asImageBitmap(), contentDescription = "Wygenerowany kod kreskowy Code 128",
            modifier = Modifier.fillMaxWidth().height(90.dp), contentScale = ContentScale.Fit)
        if (qr != null) Image(qr.asImageBitmap(), contentDescription = "Wygenerowany kod QR",
            modifier = Modifier.size(190.dp), contentScale = ContentScale.Fit)
        Text("To obrazy kodów wygenerowane z wpisanej treści; nie tworzą ani nie aktywują karty w systemie sklepu.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

private fun loadQrLogo(context: Context, uri: Uri): Bitmap {
    val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: error("Nie udało się odczytać obrazu.")
    require(source.width > 0 && source.height > 0) { "Nieprawidłowy plik obrazu." }
    val scale = minOf(1f, 1024f / maxOf(source.width, source.height))
    return if (scale < 1f) Bitmap.createScaledBitmap(source, (source.width * scale).toInt().coerceAtLeast(1), (source.height * scale).toInt().coerceAtLeast(1), true) else source
}

/**
 * Generuje kod QR z korekcją błędów H i umieszcza znak graficzny centralnie.
 * Logo jest ograniczone do 9% szerokości kodu, z białym polem ochronnym.
 * Mały rozmiar i korekcja H ograniczają ryzyko utraty czytelności; odczyt
 * należy dodatkowo sprawdzać dla każdego rodzaju ładunku i użytego logo.
 */
private fun generateBrandedQr(context: Context, payload: String, branding: String, uploadedLogo: Bitmap?): Bitmap {
    require(payload.isNotBlank()) { "Treść kodu QR jest pusta." }
    val logo = when (branding) {
        QR_BRAND_STATE -> requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.institutions_eagle_emblem)) {
            "Nie można wczytać godła."
        }
        QR_BRAND_FILE -> requireNotNull(uploadedLogo) { "Wybierz plik z logo." }
        else -> null
    }
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to 4,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 1000, 1000, hints)
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    if (logo != null) {
        val canvas = Canvas(bitmap)
        val side = matrix.width * 0.09f
        val padding = matrix.width * 0.018f
        val box = side + padding * 2f
        val left = (matrix.width - box) / 2f
        val top = (matrix.height - box) / 2f
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(left, top, left + box, top + box), box * 0.12f, box * 0.12f, backgroundPaint)
        val scale = side / maxOf(logo.width, logo.height).toFloat()
        val width = logo.width * scale
        val height = logo.height * scale
        val destination = RectF(
            (matrix.width - width) / 2f,
            (matrix.height - height) / 2f,
            (matrix.width + width) / 2f,
            (matrix.height + height) / 2f
        )
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(logo, null, destination, logoPaint)
    }
    return bitmap
}


private data class OwdDocumentGroup(
    val id: String,
    val title: String,
    val fields: List<Pair<String, String>>
)

private fun owdDocumentGroups(): List<OwdDocumentGroup> = listOf(
    OwdDocumentGroup("profile", "Profil kontaktowy i dane rodzinne", listOf(
        "Telefon" to "contactPhone", "E-mail" to "contactEmail", "Organizacja" to "contactOrganization",
        "Stanowisko" to "contactTitle", "Strona internetowa" to "contactUrl", "Adres kontaktowy" to "contactAddress",
        "Imiona ojca" to "fatherNames", "Nazwisko ojca" to "fatherSurname",
        "Imiona matki" to "motherNames", "Nazwisko rodowe matki" to "motherSurname"
    ) + listOf("personExtra" to "Informacje dodatkowe").flatMap { (prefix, _) ->
        listOf("Źródło danych" to "${prefix}Source", "Podstawa prawna" to "${prefix}LegalBasis",
            "Data weryfikacji" to "${prefix}VerifiedAt", "Podmiot weryfikujący" to "${prefix}VerifiedBy",
            "Uwagi" to "${prefix}Notes")
    }),
    OwdDocumentGroup("identity", "Dowód tożsamości", listOf(
        "Imiona" to "firstNames", "Nazwisko" to "lastName", "Data urodzenia" to "birthDate",
        "Miejsce urodzenia" to "birthPlace", "Obywatelstwo" to "citizenship", "Płeć" to "sex",
        "Nazwisko rodowe" to "birthSurname", "PESEL" to "pesel",
        "Numer dokumentu" to "idNumber", "Status" to "idStatus", "Poprzedni numer" to "idPreviousNumber",
        "Data wydania" to "idIssueDate", "Data ważności" to "idExpiryDate", "Organ wydający" to "idIssuer",
        "Data utraty / unieważnienia" to "idLossOrInvalidationDate"
    ) + listOf("Źródło danych" to "idExtraSource", "Podstawa prawna" to "idExtraLegalBasis",
        "Data weryfikacji" to "idExtraVerifiedAt", "Podmiot weryfikujący" to "idExtraVerifiedBy", "Uwagi" to "idExtraNotes")),
    OwdDocumentGroup("passport", "Dane paszportowe", listOf(
        "Numer paszportu" to "passportNumber", "Rodzaj dokumentu" to "passportType",
        "Obywatelstwo" to "passportCitizenship", "Status" to "passportStatus",
        "Państwo wydania" to "passportCountry", "Imiona i nazwisko" to "passportHolderNames",
        "Miejsce urodzenia" to "passportBirthPlace", "Płeć" to "passportSex",
        "Data wydania" to "passportIssueDate", "Data ważności" to "passportExpiryDate",
        "Organ wydający" to "passportIssuer", "Data utraty" to "passportLossDate"
    ) + listOf("Źródło danych" to "passportExtraSource", "Podstawa prawna" to "passportExtraLegalBasis",
        "Data weryfikacji" to "passportExtraVerifiedAt", "Podmiot weryfikujący" to "passportExtraVerifiedBy", "Uwagi" to "passportExtraNotes")),
    OwdDocumentGroup("family", "Dokument rodowy — ewidencja prywatna", listOf(
        "Numer dokumentu" to "familyDocNumber", "Nazwa rodu" to "familyName", "Linia / gałąź" to "familyLine",
        "Miejsce w strukturze rodu" to "familyRole", "Prowadzący ewidencję" to "familyRegistry",
        "Data sporządzenia" to "familyIssueDate", "Data aktualizacji" to "familyExpiryDate",
        "Miejsce ewidencji" to "familyRegistryPlace", "Miejsce zamieszkania" to "familyResidence",
        "Linia ojca" to "familyFatherLine", "Linia matki" to "familyMotherLine",
        "Podstawa wpisu" to "familySource", "Uwagi" to "familyNotes",
        "Podpis posiadacza" to "familyHolderSignature", "Podpis wystawcy" to "familyRegistrarSignature"
    ) + listOf("Źródło danych" to "familyExtraSource", "Podstawa prawna" to "familyExtraLegalBasis",
        "Data weryfikacji" to "familyExtraVerifiedAt", "Podmiot weryfikujący" to "familyExtraVerifiedBy", "Uwagi dodatkowe" to "familyExtraNotes")),
    OwdDocumentGroup("parliament", "Legitymacja poselska — ewidencja", listOf(
        "Numer legitymacji" to "mpCardNumber", "Kadencja" to "mpTerm", "Wystawca" to "mpIssuer",
        "Status" to "mpStatus", "Okręg wyborczy" to "mpDistrict", "Początek mandatu" to "mpStart",
        "Koniec mandatu" to "mpEnd", "Data wydania legitymacji" to "mpCardIssueDate",
        "Data ślubowania" to "mpOathDate", "Miejsce ślubowania" to "mpOathPlace",
        "Źródło potwierdzenia ślubowania" to "mpOathEvidence", "Źródło potwierdzenia mandatu" to "mpMandateSource"
    ) + listOf("Źródło danych" to "mpExtraSource", "Podstawa prawna" to "mpExtraLegalBasis",
        "Data weryfikacji" to "mpExtraVerifiedAt", "Podmiot weryfikujący" to "mpExtraVerifiedBy", "Uwagi" to "mpExtraNotes")),
    OwdDocumentGroup("training", "Legitymacja uczestnika kursu", listOf(
        "Instytucja" to "trainingInstitution", "Adres instytucji" to "trainingInstitutionAddress",
        "Numer rejestru instytucji" to "trainingInstitutionRegistry", "Numer legitymacji" to "trainingCardNumber",
        "Nazwa kursu" to "trainingCourseName", "Kod kursu" to "trainingCourseCode",
        "Data rozpoczęcia" to "trainingStartDate", "Data zakończenia" to "trainingEndDate",
        "Data wydania legitymacji" to "trainingIssueDate", "Ważna do" to "trainingValidUntil",
        "Status uczestnictwa" to "trainingStatus", "Wystawca" to "trainingIssuer",
        "Podpis wystawcy" to "trainingSignature", "Data ukończenia" to "trainingCompletionDate",
        "Liczba godzin" to "trainingHours", "Wynik" to "trainingAssessment",
        "Numer zaświadczenia" to "trainingCertificateNumber", "Forma kształcenia" to "trainingForm"
    ) + listOf("Źródło danych" to "trainingExtraSource", "Podstawa prawna" to "trainingExtraLegalBasis",
        "Data weryfikacji" to "trainingExtraVerifiedAt", "Podmiot weryfikujący" to "trainingExtraVerifiedBy", "Uwagi" to "trainingExtraNotes")),
    OwdDocumentGroup("student", "Legitymacja ucznia / studenta", listOf(
        "Numer legitymacji" to "studentCardNumber", "Uczelnia" to "studentUniversity",
        "Kierunek" to "studentProgramme", "Rok akademicki" to "studentAcademicYear",
        "Adres uczelni" to "studentUniversityAddress", "Identyfikator uczelni" to "studentUniversityId",
        "Imię i nazwisko" to "studentNames", "Wydział" to "studentFaculty",
        "Poziom studiów" to "studentLevel", "Profil / forma" to "studentProfile",
        "Rok i semestr" to "studentYear", "Data wydania" to "studentIssueDate",
        "Status" to "studentStatus", "Ważność" to "studentValidity", "Wystawca" to "studentIssuer"
    ) + listOf("Źródło danych" to "studentExtraSource", "Podstawa prawna" to "studentExtraLegalBasis",
        "Data weryfikacji" to "studentExtraVerifiedAt", "Podmiot weryfikujący" to "studentExtraVerifiedBy", "Uwagi" to "studentExtraNotes")),
    OwdDocumentGroup("disability", "Orzeczenia i zaświadczenia", listOf(
        "Numer dokumentu / sprawy" to "disabilityDecisionNumber", "Wystawca" to "disabilityAuthorityName",
        "Data dokumentu" to "disabilityDecisionDate", "Stopień / kategoria" to "disabilityDegree",
        "Okres obowiązywania" to "disabilityValidity", "Rodzaj rozstrzygnięcia" to "disabilityDecisionType",
        "Osoba wystawiająca" to "disabilityIssuerName", "Powiat / miasto" to "disabilityCounty",
        "Podstawa prawna" to "disabilityLegalBasis", "Przepis określający skutki" to "disabilityLegalProvision",
        "Zakres uznania" to "disabilityEquivalentEffect", "Podmiot prowadzący" to "disabilityFoundationName",
        "Podstawa statutowa" to "disabilityFoundationStatute", "Kwalifikacje wystawiającego" to "disabilityMedicalQualification",
        "Ocena medyczna" to "disabilityMedicalAssessment", "Skład zespołu" to "disabilityPanelMembers",
        "Przewodniczący" to "disabilityChairperson", "Osoba zatwierdzająca" to "disabilitySignature",
        "Tryb odwołania" to "disabilityAppealInfo", "Data wniosku" to "disabilityApplicationDate",
        "Numer sprawy" to "disabilityCaseNumber", "Data doręczenia" to "disabilityDeliveryDate",
        "Wynik odwołania" to "disabilityAppealOutcome", "Duplikat / zmiana" to "disabilityDuplicateInfo",
        "Dokument źródłowy" to "disabilitySourceAttachment"
    ) + listOf("Źródło danych" to "disabilityExtraSource", "Podstawa prawna" to "disabilityExtraLegalBasis",
        "Data weryfikacji" to "disabilityExtraVerifiedAt", "Podmiot weryfikujący" to "disabilityExtraVerifiedBy", "Uwagi" to "disabilityExtraNotes"))
)

@Composable
private fun OwdHubScreen(onOpenVCard: () -> Unit, onOpenWebsite: () -> Unit) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    val shareable = fields.entries
        .filter { it.value.isNotBlank() }
        .sortedBy { it.key }
        .map { it.key to it.key.replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { ch -> ch.uppercase() } }
    val populated = shareable.filter { fields[it.first].orEmpty().isNotBlank() }
    val documentGroups = remember { owdDocumentGroups() }
    val populatedByDocument = remember(populated, documentGroups) {
        documentGroups.associate { group ->
            group.id to group.fields.map { it.second }.filter { key -> fields[key].orEmpty().isNotBlank() && populated.any { it.first == key } }.toSet()
        }
    }
    val allPopulatedKeys = populated.map { it.first }.toSet()
    var selectionMode by remember { mutableStateOf("all") }
    var selectedDocuments by remember { mutableStateOf(documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }.map { it.id }.toSet()) }
    var selectedDocumentId by remember { mutableStateOf(documentGroups.firstOrNull { populatedByDocument[it.id].orEmpty().isNotEmpty() }?.id ?: "profile") }
    var selectedDocumentFields by remember { mutableStateOf(populatedByDocument[selectedDocumentId].orEmpty()) }
    var expandedDocumentId by remember { mutableStateOf<String?>(null) }
    val selected = when (selectionMode) {
        "documents" -> selectedDocuments.flatMap { populatedByDocument[it].orEmpty() }.toSet()
        "document_fields" -> selectedDocumentFields.intersect(populatedByDocument[selectedDocumentId].orEmpty())
        else -> allPopulatedKeys
    }
    var consent by remember { mutableStateOf(false) }
    var purpose by remember { mutableStateOf("Przekazanie danych wskazanemu odbiorcy") }
    var offerQr by remember { mutableStateOf<Bitmap?>(null) }
    var qrBranding by remember { mutableStateOf(QR_BRAND_NONE) }
    var qrUploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    val qrLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }.onSuccess { qrUploadedLogo = it; offerQr = null }.onFailure { offerQr = null }
    }
    var sessionMessage by remember { mutableStateOf("Wybierz zakres danych i zapoznaj się z informacją o ich pochodzeniu.") }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scanned = result.contents
        if (scanned != null) {
            sessionMessage = runCatching {
                val offer = OwdSecurity.validateOffer(scanned)
                "Rozpoznano ofertę OWD. Sesja: ${offer.getString("sessionId").take(12)}… Odbiorca musi jeszcze zaakceptować zakres i cel przed przyjęciem danych."
            }.getOrElse { "Kod QR nie jest prawidłową lub aktywną ofertą OWD: ${it.message ?: "błąd walidacji"}" }
        } else {
            sessionMessage = "Skanowanie anulowano."
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Obywatelska Wymiana Danych", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("OWD • niezależna wymiana danych między zgodnymi aplikacjami", color = Muted, style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Dwa odrębne kanały rozwoju", color = Navy, fontWeight = FontWeight.Bold)
                Text("1. OWD peer-to-peer: wymiana między zgodnymi instalacjami aplikacji obywatelskiej. 2. Integracja państwowa: osobny adapter do oficjalnej usługi mObywatela, uruchamiany dopiero po uzyskaniu zgody, certyfikatów i dokumentacji integracyjnej.", color = Ink, style = MaterialTheme.typography.bodySmall)
                Text("Przyszła certyfikacja jest założeniem architektonicznym, a nie obecnym uprawnieniem. Do tego czasu aplikacja nie łączy się z rządowym mObywatelem.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Wybór danych do udostępnienia", color = Navy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (populated.isEmpty()) {
            Text("Brak zapisanych danych do udostępnienia. Uzupełnij je w Ustawieniach.", color = Muted, style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Zakres udostępnienia", color = Navy, fontWeight = FontWeight.Bold)
            listOf(
                "all" to "Wszystkie dane i dokumenty",
                "documents" to "Wybrane dokumenty",
                "document_fields" to "Dane z wybranego dokumentu"
            ).forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectionMode == mode, onClick = {
                        selectionMode = mode
                        offerQr = null
                        consent = false
                        if (mode == "document_fields") selectedDocumentFields = populatedByDocument[selectedDocumentId].orEmpty()
                    })
                    Text(label, color = Navy, fontWeight = if (selectionMode == mode) FontWeight.Bold else FontWeight.Medium)
                }
            }
            when (selectionMode) {
                "documents" -> {
                    val availableGroups = documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }
                    val allSelected = availableGroups.isNotEmpty() && availableGroups.all { it.id in selectedDocuments }
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(checked = allSelected, onCheckedChange = { checked ->
                            selectedDocuments = if (checked) availableGroups.map { it.id }.toSet() else emptySet()
                            offerQr = null
                            consent = false
                        })
                        Column(Modifier.weight(1f)) {
                            Text("Wszystkie dostępne dokumenty", color = Navy, fontWeight = FontWeight.SemiBold)
                            Text("${availableGroups.size} dokumentów", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Rozwiń wybrany dokument, aby sprawdzić, jakie pola zostaną udostępnione.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    availableGroups.forEach { group ->
                        val keys = populatedByDocument[group.id].orEmpty()
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                            Column {
                                Row(Modifier.fillMaxWidth().clickable { expandedDocumentId = if (expandedDocumentId == group.id) null else group.id }.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Checkbox(checked = group.id in selectedDocuments, onCheckedChange = { checked ->
                                        selectedDocuments = if (checked) selectedDocuments + group.id else selectedDocuments - group.id
                                        offerQr = null
                                        consent = false
                                    })
                                    Column(Modifier.weight(1f)) {
                                        Text(group.title, color = Navy, fontWeight = FontWeight.SemiBold)
                                        Text("${keys.size} dostępnych pól", color = Muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(if (expandedDocumentId == group.id) "Zwiń －" else "Rozwiń ＋", color = Blue, style = MaterialTheme.typography.labelMedium)
                                }
                                if (expandedDocumentId == group.id) {
                                    HorizontalDivider(color = Color(0xFFE5EAF0))
                                    group.fields.filter { it.second in keys }.forEach { (label, key) ->
                                        Text("• $label", Modifier.padding(start = 52.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                                            color = Muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
                "document_fields" -> {
                    Text("Wybierz dokument z rozwijanej listy, a następnie zaznacz pola, które chcesz przekazać.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    val availableGroups = documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }
                    availableGroups.forEach { group ->
                        val keys = populatedByDocument[group.id].orEmpty()
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                            Column {
                                Row(Modifier.fillMaxWidth().clickable {
                                    if (selectedDocumentId != group.id) {
                                        selectedDocumentId = group.id
                                        selectedDocumentFields = keys
                                    }
                                    expandedDocumentId = if (expandedDocumentId == group.id) null else group.id
                                    offerQr = null
                                    consent = false
                                }.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedDocumentId == group.id, onClick = {
                                        selectedDocumentId = group.id
                                        selectedDocumentFields = keys
                                        expandedDocumentId = group.id
                                        offerQr = null
                                        consent = false
                                    })
                                    Column(Modifier.weight(1f)) {
                                        Text(group.title, color = Navy, fontWeight = if (selectedDocumentId == group.id) FontWeight.Bold else FontWeight.Medium)
                                        Text("${keys.size} zapisanych pól", color = Muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(if (expandedDocumentId == group.id) "Zwiń －" else "Pola ＋", color = Blue, style = MaterialTheme.typography.labelMedium)
                                }
                                if (expandedDocumentId == group.id) {
                                    HorizontalDivider(color = Color(0xFFE5EAF0))
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { selectedDocumentId = group.id; selectedDocumentFields = keys; offerQr = null; consent = false }) { Text("Zaznacz wszystkie pola") }
                                        TextButton(onClick = { selectedDocumentId = group.id; selectedDocumentFields = emptySet(); offerQr = null; consent = false }) { Text("Wyczyść") }
                                    }
                                    group.fields.filter { it.second in keys }.forEach { (label, key) ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.material3.Checkbox(checked = selectedDocumentId == group.id && key in selectedDocumentFields, onCheckedChange = { checked ->
                                                selectedDocumentId = group.id
                                                selectedDocumentFields = if (checked) selectedDocumentFields + key else selectedDocumentFields - key
                                                offerQr = null
                                                consent = false
                                            })
                                            Column(Modifier.weight(1f)) {
                                                Text(label, color = Navy, fontWeight = FontWeight.Medium)
                                                Text(fields[key].orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Text("Wybrano ${selected.size} pól z ${allPopulatedKeys.size}. Zmiana zakresu unieważnia poprzednią zgodę i kod QR.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(value = purpose, onValueChange = { purpose = it; consent = false; offerQr = null },
            modifier = Modifier.fillMaxWidth(), label = { Text("Cel udostępnienia") }, minLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = consent, onCheckedChange = { consent = it; offerQr = null })
            Text("Potwierdzam świadomy wybór zakresu i celu udostępnienia danych.", color = Ink, style = MaterialTheme.typography.bodySmall)
        }
        QrBrandingControls(
            branding = qrBranding,
            onBrandingChange = { qrBranding = it; offerQr = null },
            logoLoaded = qrUploadedLogo != null,
            onChooseLogo = { qrLogoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { qrUploadedLogo = null; offerQr = null }
        )
        Button(
            onClick = {
                runCatching {
                    require(selected.isNotEmpty()) { "Wybrany zakres nie zawiera żadnych uzupełnionych pól." }
                    require(purpose.isNotBlank()) { "Podaj cel udostępnienia." }
                    val (offer, _) = OwdSecurity.createOffer()
                    generateBrandedQr(context, offer.encodedQrPayload, qrBranding, qrUploadedLogo)
                }.onSuccess {
                    offerQr = it
                    sessionMessage = "Utworzono krótkotrwałą ofertę sesji. QR nie zawiera wybranych danych osobowych."
                }.onFailure { sessionMessage = it.message ?: "Nie udało się utworzyć sesji." }
            },
            enabled = consent && selected.isNotEmpty() && purpose.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Utwórz kod QR sesji OWD") }
        OutlinedButton(
            onClick = {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Zeskanuj kod sesji OWD drugiego urządzenia")
                    setBeepEnabled(false)
                    setOrientationLocked(false)
                }
                scanLauncher.launch(options)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Skanuj kod QR OWD") }
        Text(sessionMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        offerQr?.let { bitmap ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Oferta sesji OWD • ważność 180 sekund", color = Navy, fontWeight = FontWeight.Bold)
                    Image(bitmap.asImageBitmap(), contentDescription = "Kod QR oferty sesji OWD", modifier = Modifier.size(250.dp))
                     Text("To inicjuje sesję kryptograficzną. Skaner OWD potrafi rozpoznać i zweryfikować ofertę; wymiana kluczy, przesłanie rekordów i potwierdzenie odbioru wymagają jeszcze pełnego połączenia obu urządzeń.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Status funkcjonalny — etap alfa", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
                Text("Dostępne: selekcja pól, cel i zgoda, krótkotrwały QR, skaner QR z walidacją oferty, prymitywy ECDH/HKDF/AES-GCM, bezpośredni transport TCP, uwierzytelnione potwierdzenie i trwały rejestr sesji. Jeszcze niedostępne: spięcie nadawcy i odbiorcy w kompletny przepływ, adres urządzenia w QR, wzajemna wymiana kluczy w UI, uruchamianie odbiornika, podłączenie trwałego rejestru do przyjęcia sesji i działający serwer przekaźnikowy. Nie pokazujemy fikcyjnego potwierdzenia przesłania.", color = Ink, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = onOpenVCard, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator wizytówki vCard") }
        OutlinedButton(onClick = onOpenWebsite, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator kodu strony internetowej") }
        Text("Prywatność: dane nie są umieszczane w kodzie inicjującym. Odbiorca i cel muszą zostać zweryfikowani przed właściwym transferem. Dane rodzinne/rodowe są całkowicie wyłączone z obecnego zakresu OWD.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SaveQrImageButton(bitmap: Bitmap, fileName: String) {
    val context = LocalContext.current
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var saveMessage by remember { mutableStateOf("") }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        val image = pendingBitmap
        pendingBitmap = null
        if (uri == null) {
            saveMessage = "Zapis obrazu anulowano."
        } else if (image == null) {
            saveMessage = "Brak obrazu do zapisania. Wygeneruj kod ponownie."
        } else {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri, "wt")
                    ?: error("Android nie udostępnił strumienia zapisu.")
                output.use { stream ->
                    check(image.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Nie udało się zakodować obrazu PNG." }
                    stream.flush()
                }
                val saved = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    ?: error("Nie można ponownie odczytać zapisanego obrazu.")
                require(saved.width == image.width && saved.height == image.height) {
                    "Weryfikacja zapisu PNG nie powiodła się."
                }
                "Zapisano grafikę QR jako PNG (${saved.width} × ${saved.height} px)."
            }.onSuccess { saveMessage = it }
                .onFailure { saveMessage = "Błąd zapisu grafiki: ${it.message ?: "nieznany błąd"}" }
        }
    }
    OutlinedButton(
        onClick = {
            pendingBitmap = bitmap
            saveLauncher.launch(fileName)
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Zapisz grafikę QR (PNG)") }
    if (saveMessage.isNotBlank()) {
        Text(saveMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WebsiteQrScreen() {
    val context = LocalContext.current
    var address by remember { mutableStateOf("https://") }
    var branding by remember { mutableStateOf(QR_BRAND_NONE) }
    var uploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf("Wpisz pełny adres strony internetowej.") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }
            .onSuccess { uploadedLogo = it; message = "Wczytano logo z pliku." }
            .onFailure { message = "Błąd wczytywania logo: " + (it.message ?: "nieprawidłowy obraz") }
    }
    val normalized = address.trim()
    val validUrl = runCatching { val parsed = Uri.parse(normalized); (parsed.scheme == "https" || parsed.scheme == "http") && !parsed.host.isNullOrBlank() }.getOrDefault(false)
    val qrBitmap = remember(normalized, branding, uploadedLogo) {
        if (!validUrl || (branding == QR_BRAND_FILE && uploadedLogo == null)) null
        else runCatching { generateBrandedQr(context, normalized, branding, uploadedLogo) }.getOrNull()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kod QR strony internetowej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Kod zawiera adres strony. Zeskanowanie otworzy go w przeglądarce po zatwierdzeniu przez użytkownika.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Adres strony (URL)") }, placeholder = { Text("https://example.org") }, singleLine = true)
        QrBrandingControls(branding, { branding = it }, uploadedLogo != null, { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) }, { uploadedLogo = null; message = "Usunięto logo." })
        Text(message, color = Muted, style = MaterialTheme.typography.bodySmall)
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kod QR strony", color = Navy, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR strony internetowej", modifier = Modifier.size(260.dp), contentScale = ContentScale.Fit)
                     Text(normalized, color = Muted, style = MaterialTheme.typography.bodySmall)
                    SaveQrImageButton(qrBitmap, "adres-url-qr.png")
                }
            }
        } else Text(if (!validUrl) "Podaj poprawny adres rozpoczynający się od http:// lub https://." else "Wybierz logo z pliku albo ustaw opcję „Bez znaku”.", color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Godło lub logo jest wyświetlane obok kodu. Sam kod pozostaje czysty, bez nakładek. Przed publikacją sprawdź skanowanie na kilku urządzeniach.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
private fun VCardQrScreen() {
    val context = LocalContext.current
    val contactFields = remember {
        val profile = DemoIdentityStore.load(context)
        val firstNames = profile["firstNames"].orEmpty().trim()
        val lastName = profile["lastName"].orEmpty().trim()
        val displayName = listOf(firstNames, lastName).filter { it.isNotBlank() }.joinToString(" ")
        mutableStateMapOf(
            "FN" to displayName,
            "N" to listOf(lastName, firstNames, "", "", "").joinToString(";"),
            "ORG" to profile["contactOrganization"].orEmpty(),
            "TITLE" to profile["contactTitle"].orEmpty(),
            "TEL" to profile["contactPhone"].orEmpty(),
            "EMAIL" to profile["contactEmail"].orEmpty(),
            "URL" to profile["contactUrl"].orEmpty(),
            "ADR" to profile["contactAddress"].orEmpty()
        )
    }
    var importMessage by remember {
        val hasProfileName = contactFields["FN"].orEmpty().isNotBlank()
        mutableStateOf(
            if (hasProfileName) "Wstępnie uzupełniono dane z lokalnego profilu mObywatela. Sprawdź i popraw pola przed utworzeniem kodu."
            else "Brak imienia i nazwiska w profilu. Uzupełnij dane ręcznie lub wczytaj plik .vcf."
        )
    }
    var branding by remember { mutableStateOf(QR_BRAND_NONE) }
    var uploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    var logoMessage by remember { mutableStateOf("Nie wybrano pliku logo.") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }
            .onSuccess { uploadedLogo = it; logoMessage = "Wczytano logo z pliku." }
            .onFailure { logoMessage = "Błąd wczytywania logo: " + (it.message ?: "nieprawidłowy obraz") }
    }
    val vCardPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Nie udało się odczytać pliku.")
            val parsed = parseVCardContact(raw)
            parsed.forEach { (key, value) -> contactFields[key] = value }
            require(contactFields["FN"].orEmpty().isNotBlank() || contactFields["N"].orEmpty().isNotBlank()) {
                "Plik nie zawiera nazwy kontaktu (FN/N)."
            }
            importMessage = "Zaimportowano dane. Sprawdź ich zakres przed udostępnieniem."
        }.onFailure { importMessage = "Błąd importu: " + (it.message ?: "nieprawidłowy plik vCard") }
    }
    val contactSnapshot = contactFields.toMap()
    val displayName = contactSnapshot["FN"].orEmpty().trim()
    // encodeContact requires FN; do not invoke it while the form is still empty.
    // A validation error must never escape composition and terminate the activity.
    val payload = remember(contactSnapshot) {
        if (displayName.isBlank()) ""
        else runCatching { buildVCardPayload(contactSnapshot) }.getOrDefault("")
    }
    val qrBitmap = remember(payload, branding, uploadedLogo) {
        if (payload.isBlank() || (branding == QR_BRAND_FILE && uploadedLogo == null)) null
        else runCatching { generateBrandedQr(context, payload, branding, uploadedLogo) }.getOrNull()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Kod QR", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Generator kodu kontaktowego", color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Utwórz kod QR zawierający wizytówkę vCard. Zgodna aplikacja odbiorcy może odczytać dane i zaproponować zapis kontaktu.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Wizytówka kontaktowa", color = Navy, fontWeight = FontWeight.Bold)
                Text("Kod zawiera dane kontaktowe wybrane przez użytkownika.", color = Ink, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { vCardPicker.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Wczytaj plik vCard (.vcf)")
        }
        Text(importMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        QrBrandingControls(
            branding = branding,
            onBrandingChange = { branding = it },
            logoLoaded = uploadedLogo != null,
            onChooseLogo = { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { uploadedLogo = null; logoMessage = "Usunięto logo." }
        )
        Text(logoMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Dane kontaktowe", color = Navy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Pola są domyślnie uzupełniane z danych zapisanych lokalnie w profilu aplikacji. Możesz je edytować przed wygenerowaniem wizytówki. Dane identyfikacyjne, takie jak PESEL i numery dokumentów, nie są kopiowane do kodu QR.", color = Muted, style = MaterialTheme.typography.bodySmall)
        val fields = listOf(
            "FN" to "Nazwa wyświetlana",
            "N" to "Nazwisko;Imię;Drugie imię;Prefiks;Sufiks",
            "ORG" to "Organizacja",
            "TITLE" to "Stanowisko",
            "TEL" to "Telefon",
            "EMAIL" to "Adres e-mail",
            "URL" to "Strona internetowa",
            "ADR" to "Ulica;Miasto;Region;Kod;Kraj"
        )
        fields.forEach { (key, label) ->
            OutlinedTextField(
                value = contactFields[key].orEmpty(),
                onValueChange = { contactFields[key] = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = key != "N" && key != "ADR"
            )
        }
        OutlinedButton(onClick = {
            contactFields.keys.toList().forEach { contactFields[it] = "" }
            importMessage = "Wyczyszczono formularz."
        }, modifier = Modifier.fillMaxWidth()) { Text("Wyczyść dane") }
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Twój kod QR", color = Navy, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR z wizytówką vCard", modifier = Modifier.size(250.dp), contentScale = ContentScale.Fit)
                    SaveQrImageButton(qrBitmap, "wizytowka-vcard-qr.png")
                  }
            }
        } else {
            val hint = when {
                displayName.isBlank() -> "Wpisz nazwę wyświetlaną (FN), aby wygenerować wizytówkę."
                branding == QR_BRAND_FILE && uploadedLogo == null -> "Wybierz logo z pliku albo ustaw opcję „Bez znaku”."
                else -> "Nie udało się przygotować kodu QR. Sprawdź dane kontaktowe i spróbuj ponownie."
            }
            Text(hint, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text("Prywatność: kod ujawnia każdej osobie skanującej wszystkie zapisane w nim dane. Nie dodawaj numeru PESEL, danych dokumentu ani informacji poufnych.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

private fun parseVCardContact(raw: String): Map<String, String> {
    val unfolded = raw.replace("\r\n ", "").replace("\n ", "").replace("\r ", "")
    val result = mutableMapOf<String, String>()
    unfolded.lineSequence().forEach { line ->
        val colon = line.indexOf(':')
        if (colon <= 0) return@forEach
        val property = line.substring(0, colon).substringBefore(';').substringBefore('.').uppercase()
        val value = line.substring(colon + 1)
            .replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
        if (property in setOf("FN", "N", "ORG", "TITLE", "TEL", "EMAIL", "URL", "ADR") && value.isNotBlank()) {
            result[property] = if (result[property].isNullOrBlank()) value else result[property] + "; " + value
        }
    }
    return result
}

private fun buildVCardPayload(fields: Map<String, String>): String =
    VCardCodec.encodeContact(fields)

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
private fun MoreMenuScreen(
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSignedPdfs: () -> Unit,
    onOpenProfessionalQualifications: () -> Unit,
    onOpenOfficialDocuments: () -> Unit,
    onOpenFamilyDocuments: () -> Unit,
    onOpenEntityDocuments: () -> Unit,
    onOpenChurchDocuments: () -> Unit,
    onOpenDocumentBrandAssets: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Więcej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Informacje, bezpieczeństwo i ustawienia", color = Muted, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Historia działań", "Chronologiczny rejestr czynności wykonywanych w aplikacji", Icons.Filled.CalendarMonth, onOpenHistory)
        ProfileActionCard("Dokumenty PDF i podpisy elektroniczne", "Import, podgląd PDF i oznaczenia podpisu ePUAP lub kwalifikowanego", Icons.Filled.Description, onOpenSignedPdfs)
        ProfileActionCard("Prawo wykonywania zawodu i kwalifikacje", "Numery PWZ/PWZF, dane zawodów oraz archiwum zdjęć i PDF certyfikatów", Icons.Filled.Badge, onOpenProfessionalQualifications)
        ProfileActionCard("Formularze dokumentów urzędowych i ubezpieczeniowych", "Akty stanu cywilnego, KDR, EKUZ, OC, KRK, podróże służbowe i kwalifikacje UE", Icons.Filled.Description, onOpenOfficialDocuments)
        ProfileActionCard("Dokumenty Rodowe", "Akty, księgi, ustrój Rodu, władze, majątek oraz dokumenty społeczne i religijne", Icons.Filled.FamilyRestroom, onOpenFamilyDocuments)
        ProfileActionCard("Dokumenty rejestru podmiotów", "Dokumenty założycielskie, rejestrowe, organów, władz i właścicieli", Icons.Filled.AccountBalance, onOpenEntityDocuments)
        ProfileActionCard("Dokumenty Kościelne", "Posługa, Kościół jako osoba prawna, misje PDM/PUM oraz współpraca ekumeniczna", Icons.Filled.AccountBalance, onOpenChurchDocuments)
        ProfileActionCard("Zasoby identyfikacji dokumentów", "Loga, godła, herby, flagi, pieczęcie, wzory podpisów, tła i znaki wodne dla prowadzonych podmiotów", Icons.Filled.Description, onOpenDocumentBrandAssets)
        ProfileActionCard("Certyfikaty i zaświadczenia", "Rejestr dokumentów, uprawnień i danych wystawcy", Icons.Filled.Security, onOpenCertificates)
        ProfileActionCard("Powiadomienia i komunikaty", "Informacje o statusach, przypomnieniach i ważności dokumentów", Icons.Filled.NotificationsNone, onOpenHelp)
        ProfileActionCard("Pomoc i instrukcje", "Obsługa aplikacji, import, eksport i bezpieczeństwo danych", Icons.Filled.Info, onOpenHelp)
        ProfileActionCard("Ustawienia i prywatność", "Dane lokalne, preferencje i kontrola informacji", Icons.Filled.Settings, onOpenSettings)
        ProfileActionCard("Informacje prawne", "Charakter ewidencji, zasady przetwarzania i ograniczenia dokumentów", Icons.Filled.Description, onOpenHelp)
        ProfileActionCard("Informacje o aplikacji", "Wersja, zakres funkcji i status integracji", Icons.Filled.Info, onOpenHelp)
    }
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
            description = "Panel znaku i identyfikacji aplikacji"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Blue, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Użytkownik" },
                        color = Navy, fontWeight = FontWeight.Bold
                    )
                    Text("Profil lokalny", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileActionCard("Dokumenty i dane", "Przeglądaj lokalną ewidencję", Icons.Filled.CreditCard, onOpenDocuments)
        ProfileActionCard("Zaświadczenia", "Zapisane dane dokumentów i zaświadczeń", Icons.Filled.Description, onOpenCertificates)
        ProfileActionCard("Ustawienia", "Otwórz menu zarządzania lokalnymi danymi", Icons.Filled.Settings, onOpenSettings)
        Text(
            "Dane profilu są przechowywane lokalnie na tym urządzeniu.",
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
        "Sprawdź PESEL" to "Ekran informacyjny dotyczący numeru PESEL. Aplikacja nie odpytuje rejestru PESEL i nie potwierdza danych.",
        "Zastrzeż PESEL" to "Ekran informacyjny dotyczący zastrzeżenia numeru PESEL. Aplikacja nie wysyła dyspozycji do rejestru i nie zmienia statusu numeru.",
        "Bilety" to "Miejsce na prezentację własnych zapisów dotyczących biletów. Aplikacja nie sprzedaje ani nie waliduje biletów.",
        "Bezpieczeństwo" to "Ustawienia prywatności, wskazówki bezpieczeństwa i informacje o ochronie danych.",
        "Środowisko" to "Informacje środowiskowe i lokalne materiały edukacyjne.",
        "Pozostałe" to "Katalog dodatkowych modułów i informacji.",
        "Pomoc i informacje" to "Centrum pomocy: instrukcje korzystania z modułów, wyjaśnienie lokalnego charakteru danych, zasady importu i eksportu oraz informacje o ograniczeniach integracji.",
        "Powiadomienia" to "Centrum komunikatów. W tej wersji aplikacja nie odbiera jeszcze urzędowych powiadomień ani komunikatów z systemów zewnętrznych."
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
                Text(descriptions[title] ?: "Moduł informacyjny.", color = Ink)
            }
        }
        if (title == "Bezpieczeństwo") {
            ProfileActionCard("Dane i dokumenty lokalne", "Otwórz formularz danych przechowywanych na urządzeniu", Icons.Filled.Security, onOpenDocuments)
        }
        if (title == "Zdrowie") {
            ProfileActionCard("Dane o niepełnosprawności", "Otwórz ewidencję dokumentów i wskazań", Icons.Filled.HealthAndSafety, onOpenDocuments)
        }
        Text("Dostępne funkcje i zakres danych są opisane bezpośrednio w poszczególnych modułach.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit, onOpenNotifications: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Navy)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.institutions_eagle_emblem),
                contentDescription = "Godło państwowe Rzeczypospolitej Polskiej",
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("mObywatel 2.0", color = White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF234665))
                        .clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = "Powiadomienia", tint = White)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Wszystko w jednym miejscu", color = White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text("Usługi, informacje i dokumenty w jednym miejscu", color = Color(0xFFD4E3EF), style = MaterialTheme.typography.bodyMedium)
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
            Icon(Icons.Filled.Public, contentDescription = "Autorski znak aplikacji", tint = White, modifier = Modifier.size(30.dp))
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
private fun ServiceCard(item: ServiceItem, isFavorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.14f).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                Text(if (isFavorite) "★" else "☆", color = if (isFavorite) Color(0xFFB7791F) else Muted,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable(onClick = onFavorite).padding(3.dp))
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
    onEditDisability: () -> Unit,
    onExportAll: () -> Unit,
    onImportAll: () -> Unit,
    importStatus: String
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
        Text("Zarządzanie lokalnymi danymi", color = Muted,
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
        Text("Kopia danych vCard", color = Navy, fontWeight = FontWeight.Bold)
        Text("Eksport obejmuje zapisane lokalnie pola profilu i ewidencji, w tym załączone dane tekstowe. Plik może zawierać dane wrażliwe — przechowuj go bezpiecznie.", color = Muted, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Eksportuj wszystkie dane", "Zapisz jedno archiwum .vcf", Icons.Filled.Description, onExportAll)
        ProfileActionCard("Importuj dane z pliku vCard", "Wczytaj archiwum .vcf i zatwierdź zakres zmian", Icons.Filled.Description, onImportAll)
        if (importStatus.isNotBlank()) Text(importStatus, color = Blue, style = MaterialTheme.typography.bodySmall)
        Text("Dane profilu i dokumenty", color = Navy, fontWeight = FontWeight.Bold)
        ProfileActionCard("Dane osobowe", "Zdjęcie profilowe, dane osoby i rodziców",
            Icons.Filled.AccountCircle, { onEditDocuments("profile") })
        ProfileActionCard("Dowód tożsamości", "Prywatna ewidencja danych dokumentu tożsamości",
            Icons.Filled.CreditCard, { onEditDocuments("identity_card") })
        ProfileActionCard("Dane paszportowe", "Prywatna ewidencja dokumentu paszportowego",
            Icons.Filled.Public, { onEditDocuments("passport") })
        ProfileActionCard("Dokument rodowy", "Dane prywatnej ewidencji rodowej",
            Icons.Filled.Fingerprint, { onEditDocuments("family") })
        ProfileActionCard("Legitymacja poselska", "Ewidencja danych i źródeł potwierdzenia",
            Icons.Filled.AccountBalance, { onEditDocuments("parliament") })
        ProfileActionCard("Legitymacja ucznia/studenta", "Ewidencja danych ucznia lub studenta",
            Icons.Filled.Description, { onEditDocuments("training") })
        ProfileActionCard("Legitymacja ucznia/studenta", "Ewidencja danych uczelni i statusu",
            Icons.Filled.Badge, { onEditDocuments("student") })
        ProfileActionCard("Orzeczenia i zaświadczenia", "Ewidencja danych o rozstrzygnięciach",
            Icons.Filled.HealthAndSafety, onEditDisability)
        ProfileActionCard("Wszystkie dane", "Otwórz dotychczasowy formularz zbiorczy",
            Icons.Filled.Description, { onEditDocuments("all") })
        Text("Zmiany są zapisywane lokalnie w zaszyfrowanym magazynie. Nie są wysyłane do rejestrów ani podmiotów publicznych.",
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
        Text("Uzupełnij dane zgodnie z posiadanym dokumentem lub informacją źródłową.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
        val hasTrainingCertificate = listOf("trainingCourseName", "trainingCardNumber", "trainingInstitution").any { !fields[it].isNullOrBlank() }
        val hasEducationCertificate = listOf("studentCardNumber", "studentUniversity", "studentCourseName", "studentDiplomaNumber").any { !fields[it].isNullOrBlank() }
        Text("Rejestr certyfikatów i zaświadczeń", color = Navy, fontWeight = FontWeight.Bold)
        Text("Zestawienie danych dokumentów zapisanych w profilu. Wpis w tej aplikacji nie oznacza, że dokument został wystawiony, zweryfikowany lub uznany przez organ zewnętrzny.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
        }
        if (hasTrainingCertificate) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Legitymacja / dokument uczestnictwa w szkoleniu", color = Navy, fontWeight = FontWeight.Bold)
                    listOf("trainingInstitution" to "Instytucja", "trainingCourseName" to "Kurs / kwalifikacja",
                        "trainingCardNumber" to "Numer dokumentu", "trainingValidUntil" to "Ważność").forEach { (key, label) ->
                        fields[key].orEmpty().takeIf { it.isNotBlank() }?.let { Text("$label: $it", color = Ink, style = MaterialTheme.typography.bodySmall) }
                    }
                    Text("Status: dane zapisane w profilu — brak automatycznej weryfikacji wystawcy.", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (hasEducationCertificate) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Dokumenty edukacyjne", color = Navy, fontWeight = FontWeight.Bold)
                    listOf("studentUniversity" to "Uczelnia / szkoła", "studentCourseName" to "Kierunek / kurs",
                        "studentCardNumber" to "Numer legitymacji", "studentDiplomaNumber" to "Numer dyplomu").forEach { (key, label) ->
                        fields[key].orEmpty().takeIf { it.isNotBlank() }?.let { Text("$label: $it", color = Ink, style = MaterialTheme.typography.bodySmall) }
                    }
                    Text("Status: dane ewidencyjne użytkownika.", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (!hasDisabilityData && !hasTrainingCertificate && !hasEducationCertificate) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                    Text("Brak zapisanych certyfikatów i zaświadczeń", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Uzupełnij odpowiednie dane w edycji dokumentów. Zostaną zestawione w tym rejestrze.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Ważne rozróżnienie", color = Navy, fontWeight = FontWeight.Bold)
                Text("Aplikacja może przechowywać dane o certyfikacie, zaświadczeniu, orzeczeniu, legitymacji lub uprawnieniu. Nie wystawia dokumentów w imieniu szkoły, fundacji, pracodawcy ani organu publicznego. Status „wystawiony” powinien wynikać z rzeczywistego dokumentu źródłowego i być potwierdzony przez jego wystawcę.", color = Navy, style = MaterialTheme.typography.bodySmall)
            }
        }
     }
}

@Composable
private fun DocumentsWalletScreen(onBack: () -> Unit, onOpenDetails: (String) -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val groups = listOf(
        Triple("Dane osobowe profilu", Icons.Filled.AccountCircle, listOf("contactPhone", "contactEmail", "contactOrganization", "contactAddress")),
        Triple("Dowód tożsamości", Icons.Filled.CreditCard, listOf("firstNames", "lastName", "birthDate", "pesel", "idNumber", "idStatus")),
        Triple("Dane paszportowe", Icons.Filled.Public, listOf("passportNumber", "passportStatus")),
        Triple("Dokument rodowy — ewidencja prywatna", Icons.Filled.Fingerprint, listOf("familyDocNumber", "familyName")),
        Triple("Legitymacja poselska — ewidencja", Icons.Filled.AccountBalance, listOf("mpCardNumber", "mpTerm")),
        Triple("Legitymacja ucznia/studenta", Icons.Filled.Description, listOf("trainingCardNumber", "trainingInstitution")),
        Triple("Legitymacja ucznia/studenta — ewidencja", Icons.Filled.Badge, listOf("studentCardNumber", "studentUniversity")),
        Triple("Orzeczenia i zaświadczenia", Icons.Filled.HealthAndSafety, listOf("disabilityDecisionNumber", "disabilityAuthorityName")),
        Triple("Prawo jazdy", Icons.Filled.CreditCard, listOf("dlNumber", "dlCategories", "dlExpiryDate", "dlStatus")),
        Triple("Karty lojalnościowe", Icons.Filled.CreditCard, listOf("loyalty1Store", "loyalty1Number", "loyalty1Barcode", "loyalty1Qr")),
        Triple("Dane dzieci", Icons.Filled.FamilyRestroom, listOf("childrenRecordsCount")),
        Triple("eKarta Szczepień", Icons.Filled.Vaccines, listOf("vaccinationRecordsCount")),
        Triple("Karta biblioteczna", Icons.Filled.Description, listOf("library1Name", "library1CardNumber", "library1Status", "library2Name", "library2CardNumber", "library2Status"))
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Pulpit", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Dokumenty", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Twoje dokumenty i dane", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text("Twoje dokumenty i dane", color = Navy, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        groups.forEachIndexed { index, (title, icon, keys) ->
            val hasData = keys.any { !fields[it].isNullOrBlank() }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenDetails(when (index) { 11 -> "family_children"; 12 -> "family_vaccinations"; else -> index.toString() }) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (index == 11) {
                        Icon(Icons.Filled.FamilyRestroom, contentDescription = "Ikona: dane dzieci",
                            tint = Blue, modifier = Modifier.size(48.dp))
                    } else if (index == 12) {
                        Icon(Icons.Filled.Vaccines, contentDescription = "Ikona: eKarta Szczepień",
                            tint = Blue, modifier = Modifier.size(48.dp))
                    } else {
                        Image(
                            painter = painterResource(when (index) {
                                0 -> R.drawable.documents_family_record
                                1 -> R.drawable.documents_identity_card
                                2 -> R.drawable.documents_passport
                                3 -> R.drawable.documents_family_record
                                4 -> R.drawable.documents_deputy_legitimacy
                                5 -> R.drawable.documents_course_legitimacy
                                6 -> R.drawable.documents_student_legitimacy
                                8 -> R.drawable.documents_identity_card
                                9 -> R.drawable.documents_course_legitimacy
                                else -> R.drawable.documents_medical_certificates
                            }),
                            contentDescription = "Ilustracja: $title",
                            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(title, color = Navy, fontWeight = FontWeight.SemiBold)
                        Text(if (hasData) "Dane dostępne" else "Brak zapisanych danych",
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    val actionLabel = when (index) {
                        0 -> "Dane osobowe"
                        1 -> "Dowód tożsamości"
                        2 -> "Dane paszportowe"
                        else -> "Otwórz"
                    }
                    Text(actionLabel, color = Blue,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable { onOpenDetails("official_documents") }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Description, contentDescription = "Formularze dokumentów", tint = Blue, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Dokumenty urzędowe i ubezpieczeniowe", color = Navy, fontWeight = FontWeight.SemiBold)
                    Text("Odpisy USC, KDR, EKUZ, OC, KRK, misje i kwalifikacje UE", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                Text("Otwórz", color = Blue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        Text("Wybierz kartę, aby wyświetlić jej szczegóły.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit,
    onOpenService: (String) -> Unit
) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    val sections = listOf(
        Triple("Dane osobowe profilu", "Profil kontaktowy i dane rodzinne — ewidencja prywatna", listOf(
            "Telefon" to "contactPhone", "E-mail" to "contactEmail", "Organizacja" to "contactOrganization",
            "Stanowisko" to "contactTitle", "Strona internetowa" to "contactUrl", "Adres kontaktowy" to "contactAddress",
            "Imiona ojca" to "fatherNames", "Nazwisko ojca" to "fatherSurname",
            "Imiona matki" to "motherNames", "Nazwisko rodowe matki" to "motherSurname"
        )),
        Triple("Dowód tożsamości — ewidencja", "Podstawowe dane identyfikacyjne oraz dane dokumentu — wpis użytkownika", listOf(
            "Imiona" to "firstNames", "Nazwisko" to "lastName", "Data urodzenia" to "birthDate",
            "Miejsce urodzenia" to "birthPlace", "Obywatelstwo" to "citizenship", "Płeć — wpis deklarowany" to "sex",
            "Nazwisko rodowe" to "birthSurname", "PESEL" to "pesel",
            "Numer dokumentu" to "idNumber", "Status" to "idStatus",
            "Numer poprzedniego dokumentu" to "idPreviousNumber", "Data wydania" to "idIssueDate", "Data ważności" to "idExpiryDate", "Organ wydający" to "idIssuer", "Data utraty / unieważnienia" to "idLossOrInvalidationDate"
        )),
        Triple("Dane paszportowe — ewidencja", "Dane dokumentu podróży — wpis użytkownika", listOf(
            "Numer dokumentu paszportowego" to "passportNumber", "Rodzaj dokumentu" to "passportType",
            "Obywatelstwo" to "passportCitizenship", "Status" to "passportStatus",
            "Państwo wydania" to "passportCountry", "Klasyfikacja prawna dokumentu" to "passportLegalClass",
            "Status / funkcja reprezentanta" to "passportRepresentativeRole", "Reprezentowana organizacja" to "passportOrganizationName",
            "Rodzaj organizacji" to "passportOrganizationType", "Akredytacja lub uznanie" to "passportAccreditation",
            "Podstawa prawna / umowa" to "passportLegalBasis", "Status dyplomatyczny potwierdzony przez państwo" to "passportDiplomaticStatus",
            "Imiona i nazwisko w dokumencie" to "passportHolderNames", "Miejsce urodzenia" to "passportBirthPlace",
            "Płeć w dokumencie" to "passportSex", "Data wydania" to "passportIssueDate", "Data ważności" to "passportExpiryDate",
            "Organ wydający" to "passportIssuer", "Data utraty / unieważnienia" to "passportLossDate",
            "Typ laissez-passer" to "passportLpType", "Numer laissez-passer" to "passportLpNumber",
            "Organizacja wydająca laissez-passer" to "passportLpIssuer", "Status posiadacza w organizacji" to "passportLpHolderStatus",
            "Stanowisko / funkcja" to "passportLpPosition", "Podstawa prawna wydania" to "passportLpLegalBasis",
            "Dokument potwierdzający podróż służbową" to "passportLpMissionCertificate",
            "Cel i zakres podróży służbowej" to "passportLpMissionPurpose", "Państwo / państwa docelowe" to "passportLpDestination",
            "Państwo uznające dokument / uwagi wizowe" to "passportLpRecognition", "Data wydania laissez-passer" to "passportLpIssueDate",
            "Data ważności laissez-passer" to "passportLpExpiryDate", "Status laissez-passer" to "passportLpStatus",
            "Uwagi / ograniczenia dokumentu" to "passportLpNotes"
        )),
        Triple("Prawo jazdy — ewidencja", "Dane dokumentu i kategorii uprawnień — wpis użytkownika", listOf(
            "Numer prawa jazdy" to "dlNumber", "Imię i nazwisko posiadacza" to "dlHolderNames",
            "Data urodzenia" to "dlBirthDate", "Data wydania" to "dlIssueDate",
            "Data ważności" to "dlExpiryDate", "Organ wydający" to "dlAuthority",
            "Kategorie uprawnień" to "dlCategories", "Daty uzyskania kategorii" to "dlCategoryDates",
            "Ograniczenia / kody" to "dlRestrictions", "Status dokumentu" to "dlStatus",
            "Numer poprzedniego dokumentu" to "dlReplacementNumber", "Data utraty / unieważnienia" to "dlLossDate"
        )),
        Triple("Karty lojalnościowe — ewidencja", "Dane kart programów lojalnościowych sklepów", (1..10).flatMap { n ->
            val p = "loyalty$n"
            listOf("Sklep / marka" to "${p}Store", "Program" to "${p}Program", "Numer karty" to "${p}Number",
                "Kod cyfrowy" to "${p}DigitalCode", "Treść kodu kreskowego" to "${p}Barcode",
                "Treść kodu QR" to "${p}Qr", "Status" to "${p}Status")
        }),
        Triple("Karty biblioteczne — ewidencja", "Dane kart wydanych przez różne biblioteki", listOf(
            "Biblioteka 1 — nazwa" to "library1Name", "Biblioteka 1 — numer karty" to "library1CardNumber",
            "Biblioteka 1 — typ karty" to "library1Type", "Biblioteka 1 — data wydania" to "library1IssueDate",
            "Biblioteka 1 — ważna do" to "library1ExpiryDate", "Biblioteka 1 — status" to "library1Status",
            "Biblioteka 1 — kod kreskowy / identyfikator" to "library1Barcode", "Biblioteka 1 — uwagi" to "library1Notes",
            "Biblioteka 2 — nazwa" to "library2Name", "Biblioteka 2 — numer karty" to "library2CardNumber",
            "Biblioteka 2 — typ karty" to "library2Type", "Biblioteka 2 — data wydania" to "library2IssueDate",
            "Biblioteka 2 — ważna do" to "library2ExpiryDate", "Biblioteka 2 — status" to "library2Status",
            "Biblioteka 2 — kod kreskowy / identyfikator" to "library2Barcode", "Biblioteka 2 — uwagi" to "library2Notes",
            "Biblioteka 3 — nazwa" to "library3Name", "Biblioteka 3 — numer karty" to "library3CardNumber",
            "Biblioteka 3 — typ karty" to "library3Type", "Biblioteka 3 — data wydania" to "library3IssueDate",
            "Biblioteka 3 — ważna do" to "library3ExpiryDate", "Biblioteka 3 — status" to "library3Status",
            "Biblioteka 3 — kod kreskowy / identyfikator" to "library3Barcode", "Biblioteka 3 — uwagi" to "library3Notes"
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
        Triple("Legitymacja ucznia/studenta", "Ewidencja danych legitymacji ucznia lub studenta", listOf(
            "Numer legitymacji" to "trainingCardNumber", "Instytucja" to "trainingInstitution",
            "Nazwa kursu" to "trainingCourseName", "Status uczestnictwa" to "trainingStatus",
            "Adres instytucji" to "trainingInstitutionAddress", "Numer wpisu instytucji" to "trainingInstitutionRegistry", "Symbol kursu" to "trainingCourseCode", "Data rozpoczęcia" to "trainingStartDate", "Data zakończenia" to "trainingEndDate", "Data wydania legitymacji" to "trainingIssueDate", "Ważna do" to "trainingValidUntil", "Wystawca" to "trainingIssuer", "Podpis wystawcy" to "trainingSignature", "Data ukończenia" to "trainingCompletionDate", "Liczba godzin" to "trainingHours", "Wynik zaliczenia" to "trainingAssessment", "Numer zaświadczenia" to "trainingCertificateNumber", "Forma kształcenia" to "trainingForm"
        )),
        Triple("Legitymacja ucznia/studenta — ewidencja", "Ewidencja danych podanych przez użytkownika", listOf(
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
    val extraPrefix = listOf("personExtra", "idExtra", "passportExtra", "familyExtra", "mpExtra", "trainingExtra", "studentExtra", "disabilityExtra", "dlExtra", "loyaltyExtra", "libraryExtra").getOrNull(documentId.toIntOrNull() ?: -1) ?: "personExtra"
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
                8 -> R.drawable.documents_identity_card
                9, 10 -> R.drawable.documents_course_legitimacy
                else -> R.drawable.documents_medical_certificates
            }),
            contentDescription = "Ilustracja ewidencji: ${section.first}",
            modifier = Modifier.fillMaxWidth().height(148.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )
        if (documentId == "2") {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Rodzaj paszportu i status reprezentanta — informacja prawna", color = Navy, fontWeight = FontWeight.Bold)
                    Text("W polskim prawie paszport zwykły, tymczasowy, dyplomatyczny i służbowy MSZ to odrębne rodzaje dokumentów. Paszport dyplomatyczny wydaje minister właściwy do spraw zagranicznych osobom określonym w ustawie o dokumentach paszportowych, w tym — w przewidzianych ustawą przypadkach — osobom pełniącym funkcje związane z przywilejami i immunitetami albo skierowanym do pracy w organizacjach międzynarodowych.", color = Navy, style = MaterialTheme.typography.bodySmall)
                    Text("Reprezentant międzynarodowej organizacji pozarządowej (NGO) nie otrzymuje z samego tytułu reprezentanta odrębnego paszportu dyplomatycznego ani immunitetu. Zwykle posługuje się paszportem wydanym przez państwo obywatelstwa oraz odrębnym dokumentem mandatu, zaproszeniem lub akredytacją. O uznaniu statusu i ewentualnych przywilejach decyduje właściwa podstawa prawna i państwo przyjmujące.", color = Navy, style = MaterialTheme.typography.bodySmall)
                    Text("Odrębną kategorią są dokumenty podróży wydawane przez niektóre organizacje międzyrządowe. Przykładowo ONZ może wydawać swoim urzędnikom laissez-passer na podstawie Konwencji o przywilejach i immunitetach Narodów Zjednoczonych z 1946 r. Nie jest to paszport dyplomatyczny państwa.", color = Navy, style = MaterialTheme.typography.bodySmall)
                    Text("Wpis w tej aplikacji jest wyłącznie ewidencją. Nie nadaje statusu dyplomatycznego, nie stanowi akredytacji i nie potwierdza przywilejów ani immunitetów.", color = Color(0xFF805000), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Dane dokumentu", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
            }
        }
        if (photoBitmap != null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Wspólne zdjęcie dokumentowe", color = Navy, fontWeight = FontWeight.Bold)
                    Image(photoBitmap.asImageBitmap(), contentDescription = "Wspólne zdjęcie profilowe do dokumentów",
                        modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Fit)
                    Text("Fotografia pochodzi ze zdjęcia profilowego zapisanego w danych osobowych.", color = Muted,
                        style = MaterialTheme.typography.bodySmall)
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
        val relatedServices = when (documentId) {
            "1" -> listOf("Zastrzeż PESEL", "Mandaty", "Sprawdź PESEL")
            "2" -> listOf("Polak za granicą")
            "5", "6" -> listOf("Edukacja")
            "7" -> listOf("Zdrowie")
            else -> listOf("Bezpieczeństwo")
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Powiązane moduły", color = Navy, fontWeight = FontWeight.Bold)
                Text("Skróty otwierają powiązane sekcje aplikacji.", color = Muted, style = MaterialTheme.typography.bodySmall)
                relatedServices.forEach { serviceTitle ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenService(serviceTitle) }.padding(vertical = 9.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = Blue, modifier = Modifier.size(19.dp))
                        Text(serviceTitle, color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 9.dp))
                    }
                }
            }
        }
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

private fun parseDocumentOcr(kind: String, rawText: String): Map<String, String> {
    val text = rawText.replace('\r', '\n').trim()
    val compactLines = text.lines().map { it.trim().replace(" ", "").uppercase() }.filter { it.isNotBlank() }
    val result = linkedMapOf<String, String>()
    result["${kind}OcrText"] = rawText.trim()
    fun put(key: String, value: String?) { if (!value.isNullOrBlank()) result[key] = value.trim() }
    fun labelled(vararg labels: String): String? {
        val pattern = Regex("(?i)^(?:${labels.joinToString("|") { Regex.escape(it) }})\\s*[:#.-]?\\s*(.+)$")
        return text.lines().firstNotNullOfOrNull { line -> pattern.find(line.trim())?.groupValues?.getOrNull(1)?.trim() }
    }
    fun date(value: String?): String? {
        if (value == null) return null
        val digits = value.filter(Char::isDigit)
        return when {
            digits.length == 8 && digits.startsWith("19") || digits.length == 8 && digits.startsWith("20") ->
                "${digits.substring(6,8)}-${digits.substring(4,6)}-${digits.substring(0,4)}"
            digits.length == 8 -> "${digits.substring(0,2)}-${digits.substring(2,4)}-${digits.substring(4,8)}"
            else -> value
        }
    }
    fun mrzDate(value: String): String? {
        if (!value.matches(Regex("\\d{6}"))) return null
        val yy = value.substring(0,2).toIntOrNull() ?: return null
        val year = if (yy <= 49) 2000 + yy else 1900 + yy
        return "${value.substring(4,6)}-${value.substring(2,4)}-$year"
    }
    val mrz = compactLines.filter { it.matches(Regex("[A-Z0-9<]{25,44}")) }
    when (kind) {
        "id" -> {
            put("firstNames", labelled("Imiona", "Imię", "Given names"))
            put("lastName", labelled("Nazwisko", "Surname"))
            put("birthDate", date(labelled("Data urodzenia", "Date of birth")))
            put("birthPlace", labelled("Miejsce urodzenia", "Place of birth"))
            put("citizenship", labelled("Obywatelstwo", "Nationality"))
            put("pesel", labelled("PESEL"))
            put("idNumber", labelled("Seria i numer", "Numer dokumentu", "Document number"))
            put("idIssueDate", date(labelled("Data wydania", "Date of issue")))
            put("idExpiryDate", date(labelled("Termin ważności", "Data ważności", "Date of expiry")))
            put("idIssuer", labelled("Organ wydający", "Authority"))
            val first = mrz.firstOrNull { it.startsWith("ID") || it.startsWith("I<") }
            val second = first?.let { mrz.getOrNull(mrz.indexOf(it) + 1) }
            val names = first?.let { mrz.getOrNull(mrz.indexOf(it) + 2) }
            if (first != null) put("idNumber", first.drop(5).take(9).replace("<", "").takeIf { it.isNotBlank() })
            if (second != null && second.length >= 28) {
                put("birthDate", mrzDate(second.substring(0,6)))
                put("sex", when (second.getOrNull(7)) { 'M' -> "M"; 'F' -> "K"; else -> null })
                put("idExpiryDate", mrzDate(second.substring(8,14)))
                put("citizenship", second.substring(12,15))
                put("pesel", second.substring(15,26).replace("<", "").takeIf { it.matches(Regex("\\d{11}")) })
            }
            if (names != null) {
                val parts = names.split("<<", limit = 2)
                put("lastName", parts.getOrNull(0)?.replace("<", " ")?.trim())
                put("firstNames", parts.getOrNull(1)?.replace("<", " ")?.trim())
            }
        }
        "passport" -> {
            put("passportNumber", labelled("Numer paszportu", "Nr paszportu", "Passport number"))
            put("passportHolderNames", labelled("Nazwisko i imię", "Nazwisko", "Surname"))
            put("passportBirthPlace", labelled("Miejsce urodzenia", "Place of birth"))
            put("passportCountry", labelled("Państwo wydania", "Country"))
            put("passportCitizenship", labelled("Obywatelstwo", "Nationality"))
            put("passportIssueDate", date(labelled("Data wydania", "Date of issue")))
            put("passportExpiryDate", date(labelled("Data ważności", "Date of expiry")))
            put("passportIssuer", labelled("Organ wydający", "Authority"))
            val first = mrz.firstOrNull { it.startsWith("P<") }
            val second = first?.let { mrz.getOrNull(mrz.indexOf(it) + 1) }
            if (first != null) {
                val parts = first.drop(5).split("<<", limit = 2)
                put("passportHolderNames", listOf(parts.getOrNull(1), parts.getOrNull(0)).filterNotNull().joinToString(" ").replace("<", " ").trim())
            }
            if (second != null && second.length >= 28) {
                put("passportNumber", second.take(9).replace("<", "").takeIf { it.isNotBlank() })
                put("passportCountry", second.substring(10,13))
                put("passportBirthDate", mrzDate(second.substring(13,19)))
                put("passportSex", when (second.getOrNull(20)) { 'M' -> "M"; 'F' -> "K"; else -> null })
                put("passportExpiryDate", mrzDate(second.substring(21,27)))
            }
        }
        "driving" -> {
            put("dlNumber", labelled("Numer prawa jazdy", "Nr prawa jazdy", "5"))
            put("dlHolderNames", labelled("Nazwisko i imię", "Nazwisko", "1", "2"))
            put("dlBirthDate", date(labelled("Data urodzenia", "3")))
            put("dlIssueDate", date(labelled("Data wydania", "4a")))
            put("dlExpiryDate", date(labelled("Data ważności", "4b")))
            put("dlAuthority", labelled("Organ wydający", "4c"))
            put("dlCategories", labelled("Kategorie", "Kategorie uprawnień", "9"))
            put("dlCategoryDates", labelled("Daty kategorii", "10"))
            put("dlRestrictions", labelled("Ograniczenia", "12"))
            val categories = Regex("(?i)\\b(AM|A1|A2|A|B1|B|BE|C1|C1E|C|CE|D1|D1E|D|DE|T)\\b")
                .findAll(text).map { it.value.uppercase() }.distinct().toList()
            if (result["dlCategories"].isNullOrBlank() && categories.isNotEmpty()) put("dlCategories", categories.joinToString(", "))
        }
        else -> if (kind.startsWith("library")) {
            val index = kind.removePrefix("library").toIntOrNull()?.coerceIn(1, 3) ?: 1
            put("library${index}Name", labelled("Biblioteka", "Nazwa biblioteki", "Library"))
            put("library${index}CardNumber", labelled("Numer karty", "Nr karty", "Card number", "ID"))
            val numeric = Regex("(?<!\\d)\\d{6,20}(?!\\d)").find(text)?.value
            if (result["library${index}CardNumber"].isNullOrBlank()) put("library${index}CardNumber", numeric)
        }
    }
    return result.filterValues { it.isNotBlank() }
}

@Composable
private fun DemoDocumentsScreen(categoryId: String = "all", onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var photoBase64 by remember { mutableStateOf(fields["photo"] ?: "") }
    var saveMessage by remember { mutableStateOf("") }
    var pendingDocumentOcr by remember { mutableStateOf("") }
    val editorTitle = when (categoryId) {
        "profile" -> "Dane osobowe i zdjęcie profilowe"
        "family" -> "Dokument rodowy — ewidencja prywatna"
        "identity_card" -> "Dowód tożsamości — ewidencja"
        "parliament" -> "Legitymacja poselska — ewidencja"
        "training" -> "Legitymacja ucznia/studenta — ewidencja"
        "student" -> "Legitymacja ucznia/studenta — ewidencja"
        "disability" -> "Orzeczenia i zaświadczenia — ewidencja"
        "passport" -> "Dane paszportowe — ewidencja"
        "driving_license" -> "Prawo jazdy — ewidencja"
        "loyalty_cards" -> "Karty lojalnościowe — ewidencja"
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

    var pendingLoyaltyImageKey by remember { mutableStateOf("") }
    val loyaltyImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val targetKey = pendingLoyaltyImageKey
        if (uri != null && targetKey.isNotBlank()) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego obrazu.")
                require(bytes.size <= 5 * 1024 * 1024) { "Obraz przekracza limit 5 MB." }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("Plik nie jest prawidłowym obrazem.")
                Triple(Base64.encodeToString(bytes, Base64.NO_WRAP), decodeLoyaltyCode(bitmap), bitmap)
            }.onSuccess { (encoded, decoded, bitmap) ->
                fields[targetKey] = encoded
                val cardPrefix = targetKey.substringBeforeLast("Image")
                if (decoded != null) {
                    when {
                        targetKey.endsWith("BarcodeImage") -> fields["${cardPrefix}Barcode"] = decoded
                        targetKey.endsWith("QrImage") -> fields["${cardPrefix}Qr"] = decoded
                        else -> {
                            if (fields["${cardPrefix}Barcode"].isNullOrBlank()) fields["${cardPrefix}Barcode"] = decoded
                            else if (fields["${cardPrefix}Qr"].isNullOrBlank()) fields["${cardPrefix}Qr"] = decoded
                        }
                    }
                }
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { visionText ->
                        val recognized = visionText.textBlocks.flatMap { it.lines }.joinToString("\n") { it.text }.trim()
                        if (recognized.isNotBlank()) {
                            fields["${cardPrefix}RecognizedText"] = recognized
                            val numberCandidate = Regex("(?<!\\d)(?:\\d[ -]?){8,20}(?!\\d)")
                                .findAll(recognized).map { it.value.replace(Regex("[ -]"), "") }
                                .firstOrNull { it.length in 8..20 }
                            if (numberCandidate != null && fields["${cardPrefix}Number"].isNullOrBlank()) {
                                fields["${cardPrefix}Number"] = numberCandidate
                            }
                            if (fields["${cardPrefix}Store"].isNullOrBlank()) {
                                val firstLine = recognized.lineSequence().map { it.trim() }
                                    .firstOrNull { it.length in 3..40 && it.any(Char::isLetter) }
                                if (firstLine != null) fields["${cardPrefix}Store"] = firstLine
                            }
                            saveMessage = "Zdjęcie przeanalizowane przez OCR. Rozpoznany tekst i sugerowany numer wpisano do pól — sprawdź je przed zapisem."
                        } else saveMessage = if (decoded != null) "Kod odczytany. OCR nie rozpoznał tekstu karty." else "Zdjęcie dodane, ale OCR nie rozpoznał tekstu ani kodu."
                        recognizer.close()
                    }
                    .addOnFailureListener { error ->
                        saveMessage = "Zdjęcie dodane; OCR nie powiódł się: ${error.localizedMessage ?: "błąd rozpoznawania"}"
                        recognizer.close()
                    }
                if (decoded != null) saveMessage = "Wykryto kod. Trwa rozpoznawanie tekstu karty…"
                else saveMessage = "Trwa rozpoznawanie tekstu ze zdjęcia…"
            }.onFailure { saveMessage = "Nie udało się przetworzyć obrazu: ${it.message ?: "błąd"}" }
        }
        pendingLoyaltyImageKey = ""
    }
    val documentOcrPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val kind = pendingDocumentOcr
        if (uri != null && kind.isNotBlank()) {
            runCatching {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    ?: error("Nie można odczytać wybranego zdjęcia.")
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { visionText ->
                        val recognized = visionText.text
                        if (recognized.isBlank()) {
                            saveMessage = "OCR nie rozpoznał tekstu. Wykonaj wyraźniejsze zdjęcie całego dokumentu."
                        } else {
                            val parsed = parseDocumentOcr(kind, recognized)
                            parsed.forEach { (key, value) ->
                                if (key.endsWith("OcrText") || fields[key].isNullOrBlank()) fields[key] = value
                            }
                            saveMessage = "OCR rozpoznał tekst i zaproponował wartości w ${parsed.keys.count { !it.endsWith("OcrText") }} polach. Sprawdź je z oryginałem przed zapisem."
                        }
                        recognizer.close()
                    }
                    .addOnFailureListener { error ->
                        saveMessage = "Błąd OCR: ${error.localizedMessage ?: "nie udało się rozpoznać tekstu"}"
                        recognizer.close()
                    }
            }.onFailure { saveMessage = "Nie udało się przetworzyć zdjęcia: ${it.localizedMessage ?: "błąd odczytu"}" }
        }
        pendingDocumentOcr = ""
    }
    fun launchDocumentOcr(kind: String) {
        pendingDocumentOcr = kind
        documentOcrPicker.launch("image/*")
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
        if (categoryId != "all" && categoryId != "profile") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Wspólne zdjęcie do dokumentów", color = Navy, fontWeight = FontWeight.Bold)
                    val sharedPhotoBytes = remember(photoBase64) {
                        runCatching { if (photoBase64.isBlank()) null else Base64.decode(photoBase64, Base64.DEFAULT) }.getOrNull()
                    }
                    val sharedPhotoBitmap = remember(sharedPhotoBytes) {
                        sharedPhotoBytes?.let { bytes ->
                            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                            var sample = 1
                            while (bounds.outWidth / sample > 900 || bounds.outHeight / sample > 900) sample *= 2
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
                                BitmapFactory.Options().apply { inSampleSize = sample })
                        }
                    }
                    if (sharedPhotoBitmap != null) {
                        Image(sharedPhotoBitmap.asImageBitmap(), contentDescription = "Wspólne zdjęcie do dokumentów",
                            modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit)
                    } else {
                        Text("Nie dodano jeszcze zdjęcia. Możesz wybrać je z pliku lub galerii.",
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Importuj zdjęcie", color = Blue, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { photoPicker.launch("image/*") }.padding(vertical = 8.dp))
                        if (photoBase64.isNotBlank()) {
                            Text("Usuń zdjęcie", color = Color(0xFF9B2C2C), fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { photoBase64 = "" }.padding(vertical = 8.dp))
                        }
                    }
                    Text("To samo zdjęcie jest wykorzystywane w podglądach dowodu tożsamości, danych paszportowych, dokumentu rodowego i innych kart, które przewidują fotografię.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                }
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
                DemoDataField("contactPhone", "Telefon kontaktowy", fields)
                DemoDataField("contactEmail", "Adres poczty elektronicznej", fields)
                DemoDataField("contactOrganization", "Organizacja", fields)
                DemoDataField("contactTitle", "Stanowisko", fields)
                DemoDataField("contactUrl", "Strona internetowa", fields)
                DemoDataField("contactAddress", "Adres kontaktowy", fields)
                DemoDataField("fatherNames", "Imiona ojca", fields)
                DemoDataField("fatherSurname", "Nazwisko ojca", fields)
                DemoDataField("motherNames", "Imiona matki", fields)
                DemoDataField("motherSurname", "Nazwisko rodowe matki", fields)
                AdditionalInformationSection("Informacje dodatkowe — dane osobowe", fields, "personExtra", "Uzupełnij dane zgodnie z aktualnym dokumentem źródłowym.")
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
                Text("Podstawowe dane identyfikacyjne", color = Navy, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { launchDocumentOcr("id") }, modifier = Modifier.fillMaxWidth()) { Text("Odczytaj dane ze zdjęcia dowodu osobistego (OCR)") }
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("birthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("birthPlace", "Miejsce urodzenia", fields)
                DemoDataField("citizenship", "Obywatelstwo", fields)
                DemoDataField("sex", "Płeć (dane deklarowane)", fields)
                DemoDataField("birthSurname", "Nazwisko rodowe", fields)
                DemoDataField("pesel", "Numer PESEL", fields)
                Text("Dane samego dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("idNumber", "Numer dokumentu tożsamości", fields)
                DemoDataField("idIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("idExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("idIssuer", "Organ wydający", fields)
                DemoDataField("idStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("idPreviousNumber", "Numer poprzedniego dokumentu (jeśli dotyczy)", fields)
                DemoDataField("idLossOrInvalidationDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Status dokumentu prywatnego", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
                        Text("To prywatna karta danych identyfikacyjnych utworzona przez użytkownika. Nie jest dowodem osobistym wydanym przez państwo, nie potwierdza urzędowo tożsamości i nie zastępuje dokumentu z rejestru państwowego. Wiarygodność wpisów zależy od wskazanego źródła i jego niezależnej weryfikacji.", color = Ink, style = MaterialTheme.typography.bodySmall)
                    }
                }
                AdditionalInformationSection("Źródła i weryfikacja danych identyfikacyjnych", fields, "idExtra", "Wskaż dokument źródłowy i datę jego sprawdzenia. Sam wpis użytkownika nie stanowi potwierdzenia urzędowego.")
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
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "trainingExtra", "Legitymacja instytucji szkoleniowej potwierdza status w ewidencji wystawcy w granicach regulaminu i właściwej podstawy działania. Nie jest legitymacją studencką i sama nie nadaje statusu studenta uczelni.")
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
                Text("Legitymacja ucznia/studenta — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
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
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "studentExtra", "Ten formularz służy wyłącznie do ewidencji danych. Nie nadaje statusu studenta, nie potwierdza wpisu na listę studentów i nie zastępuje dokumentu ani certyfikatu wydanego przez uczelnię.")
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
                    "medical_opinion" -> "Opinia lub zaświadczenie medyczne dokumentuje ocenę albo stan zdrowia w zakresie wskazanym przez wystawcę."
                    "self_declaration" -> "Oświadczenie własne jest deklaracją osoby. Jego znaczenie zależy od procedury, w której jest składane; samo nie ustanawia ustawowego stopnia niepełnosprawności."
                    else -> "Karta ewidencyjna porządkuje dane wpisane przez użytkownika."
                })
            }
        }
        }
        if (categoryId == "all" || categoryId == "loyalty_cards") {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Karty lojalnościowe sklepów", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Zapisz maksymalnie dziesięć kart. Możesz importować zdjęcie całej karty, osobny kod kreskowy lub kod QR. Aplikacja spróbuje odczytać kod ze zdjęcia.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    (1..10).forEach { number ->
                        val prefix = "loyalty$number"
                        Text("Karta $number", color = Navy, fontWeight = FontWeight.SemiBold)
                        DemoDataField("${prefix}Store", "Sklep / marka", fields)
                        DemoDataField("${prefix}Program", "Nazwa programu lojalnościowego", fields)
                        DemoDataField("${prefix}Number", "Numer karty / identyfikator", fields)
                        DemoDataField("${prefix}DigitalCode", "Kod cyfrowy", fields)
                        DemoDataField("${prefix}Barcode", "Treść kodu kreskowego", fields)
                        DemoDataField("${prefix}Qr", "Treść kodu QR", fields)
                        DemoDataField("${prefix}Status", "Status karty", fields, "aktywna / nieaktywna")
                        DemoDataField("${prefix}RecognizedText", "Tekst rozpoznany ze zdjęcia (OCR) — sprawdź i popraw", fields)
                        listOf("Card" to "zdjęcie karty", "Barcode" to "zdjęcie kodu kreskowego", "Qr" to "zdjęcie kodu QR").forEach { (kind, label) ->
                            val imageKey = "${prefix}${kind}Image"
                            OutlinedButton(onClick = { pendingLoyaltyImageKey = imageKey; loyaltyImagePicker.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()) {
                                Text(if (fields[imageKey].isNullOrBlank()) "Importuj $label" else "Zmień $label")
                            }
                            val imageBitmap = remember(fields[imageKey]) {
                                runCatching {
                                    val encoded = fields[imageKey].orEmpty()
                                    if (encoded.isBlank()) null else {
                                        val bytes = Base64.decode(encoded, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    }
                                }.getOrNull()
                            }
                            if (imageBitmap != null) Image(imageBitmap.asImageBitmap(), contentDescription = label,
                                modifier = Modifier.fillMaxWidth().height(110.dp), contentScale = ContentScale.Fit)
                        }
                        LoyaltyGeneratedCodes(fields["${prefix}Barcode"].orEmpty(), fields["${prefix}Qr"].orEmpty())
                    }
                    Text("Odczyt działa dla kodów obsługiwanych przez ZXing. Nie gwarantuje odczytu kodów dynamicznych, zaszyfrowanych ani wymagających aplikacji wystawcy.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    AdditionalInformationSection("Informacje dodatkowe — karty lojalnościowe", fields, "loyaltyExtra",
                        "Zapisz źródło danych lub datę weryfikacji. Kody mogą umożliwiać dostęp do konta lojalnościowego — chroń je jak dane dostępowe.")
                }
            }
        }
        if (categoryId == "all" || categoryId == "library_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Karty biblioteczne z różnych bibliotek", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Możesz zapisać dane maksymalnie trzech kart. Każda karta ma osobny zestaw danych biblioteki i numeru identyfikacyjnego.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    (1..3).forEach { number ->
                        val prefix = "library$number"
                        Text("Karta biblioteczna $number", color = Navy, fontWeight = FontWeight.SemiBold)
                        OutlinedButton(onClick = { launchDocumentOcr("library$number") }, modifier = Modifier.fillMaxWidth()) { Text("Odczytaj dane z karty $number (OCR)") }
                        DemoDataField("${prefix}Name", "Nazwa biblioteki / filii", fields)
                        DemoDataField("${prefix}CardNumber", "Numer karty bibliotecznej", fields)
                        DemoDataField("${prefix}Type", "Rodzaj karty", fields, "np. miejska / uczelniana / szkolna / dziecięca")
                        DemoDataField("${prefix}IssueDate", "Data wydania", fields, "DD-MM-RRRR")
                        DemoDataField("${prefix}ExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                        DemoDataField("${prefix}Status", "Status karty", fields, "aktywna / zawieszona / utracona / nieaktywna")
                        DemoDataField("${prefix}Barcode", "Kod kreskowy / identyfikator", fields)
                        DemoDataField("${prefix}Notes", "Uwagi", fields)
                    }
                    Text("Wspólne zdjęcie dokumentowe może być użyte, jeśli dana biblioteka przewiduje fotografię na karcie.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    AdditionalInformationSection("Informacje dodatkowe — karty biblioteczne", fields, "libraryExtra",
                        "Zapisz regulamin biblioteki, źródło danych lub datę ich sprawdzenia. Dane karty nie zastępują oryginalnej karty ani systemu bibliotecznego.")
                }
            }
        }
        if (categoryId == "all" || categoryId == "driving_license") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Dane prawa jazdy", color = Navy, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { launchDocumentOcr("driving") }, modifier = Modifier.fillMaxWidth()) { Text("Odczytaj dane ze zdjęcia prawa jazdy (OCR)") }
                    DemoDataField("dlNumber", "Numer prawa jazdy", fields)
                    DemoDataField("dlHolderNames", "Imię i nazwisko posiadacza", fields)
                    DemoDataField("dlBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                    DemoDataField("dlIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                    DemoDataField("dlExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                    DemoDataField("dlAuthority", "Organ wydający", fields)
                    DemoDataField("dlCategories", "Kategorie uprawnień", fields, "np. AM, A1, A2, A, B, BE, C, D, T")
                    DemoDataField("dlCategoryDates", "Daty uzyskania poszczególnych kategorii", fields)
                    DemoDataField("dlRestrictions", "Ograniczenia / kody", fields)
                    DemoDataField("dlStatus", "Status dokumentu", fields, "ważny / zatrzymany / cofnięty / unieważniony")
                    DemoDataField("dlReplacementNumber", "Numer poprzedniego dokumentu", fields)
                    DemoDataField("dlLossDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                    Text("Fotografia", color = Navy, fontWeight = FontWeight.Bold)
                    val drivingPhotoBytes = remember(photoBase64) {
                        runCatching { if (photoBase64.isBlank()) null else Base64.decode(photoBase64, Base64.DEFAULT) }.getOrNull()
                    }
                    val drivingPhotoBitmap = remember(drivingPhotoBytes) {
                        drivingPhotoBytes?.let { bytes ->
                            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                            var sample = 1
                            while (bounds.outWidth / sample > 700 || bounds.outHeight / sample > 700) sample *= 2
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
                                BitmapFactory.Options().apply { inSampleSize = sample })
                        }
                    }
                    if (drivingPhotoBitmap != null) {
                        Image(drivingPhotoBitmap.asImageBitmap(), contentDescription = "Zdjęcie posiadacza prawa jazdy",
                            modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit)
                    } else {
                        Text("Brak wspólnego zdjęcia. Dodaj je w sekcji zdjęcia dokumentowego powyżej.",
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    AdditionalInformationSection("Informacje dodatkowe — prawo jazdy", fields, "dlExtra",
                        "Wpisz dane zgodnie z dokumentem źródłowym. Sama ewidencja nie potwierdza posiadania uprawnień do kierowania pojazdami.")
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Informacja o charakterze ewidencji", color = Color(0xFF805000), fontWeight = FontWeight.Bold)
                            Text("To prywatna karta danych uzupełnianych przez użytkownika. Nie jest prawem jazdy, nie potwierdza uprawnień i nie zastępuje dokumentu ani danych z rejestrów państwowych.",
                                color = Ink, style = MaterialTheme.typography.bodySmall)
                        }
                    }
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
                OutlinedButton(onClick = { launchDocumentOcr("passport") }, modifier = Modifier.fillMaxWidth()) { Text("Odczytaj dane ze zdjęcia paszportu (OCR)") }
                DemoDataField("passportNumber", "Numer dokumentu paszportowego", fields)
                DemoDataField("passportCountry", "Państwo wydania", fields)
                DemoDataField("passportIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("passportExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("passportIssuer", "Organ wydający", fields)
                DemoDataField("passportType", "Rodzaj dokumentu podróży", fields, "paszport zwykły / tymczasowy / dyplomatyczny / służbowy MSZ / laissez-passer / inny")
                DemoDataField("passportLegalClass", "Klasyfikacja prawna dokumentu", fields, "paszport krajowy / dyplomatyczny / służbowy / dokument organizacji międzynarodowej / inny")
                DemoDataField("passportRepresentativeRole", "Status / funkcja reprezentanta", fields, "np. delegat, przedstawiciel, obserwator, ekspert")
                DemoDataField("passportOrganizationName", "Reprezentowana organizacja", fields)
                DemoDataField("passportOrganizationType", "Rodzaj organizacji", fields, "IGO / NGO krajowa / NGO międzynarodowa / organ państwa / inna")
                DemoDataField("passportAccreditation", "Akredytacja lub uznanie przez państwo przyjmujące", fields)
                DemoDataField("passportLegalBasis", "Podstawa prawna lub umowa międzynarodowa", fields)
                DemoDataField("passportDiplomaticStatus", "Status dyplomatyczny potwierdzony przez państwo", fields, "tak / nie / brak danych — podaj źródło")
                DemoDataField("passportHolderNames", "Imiona i nazwisko zapisane w paszporcie", fields)
                DemoDataField("passportBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("passportBirthPlace", "Miejsce urodzenia", fields)
                DemoDataField("passportCitizenship", "Obywatelstwo w dokumencie", fields)
                DemoDataField("passportSex", "Płeć w dokumencie", fields)
                DemoDataField("passportStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("passportLossDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                Text("Dane dokumentu laissez-passer", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("passportLpType", "Typ laissez-passer", fields, "ONZ / Unia Europejska / inna organizacja międzynarodowa")
                DemoDataField("passportLpNumber", "Numer laissez-passer", fields)
                DemoDataField("passportLpIssuer", "Organizacja wydająca laissez-passer", fields)
                DemoDataField("passportLpHolderStatus", "Status posiadacza w organizacji wydającej", fields, "urzędnik / funkcjonariusz / ekspert / inny — zgodnie z dokumentem")
                DemoDataField("passportLpPosition", "Stanowisko / funkcja", fields)
                DemoDataField("passportLpLegalBasis", "Podstawa prawna wydania", fields, "konwencja / statut / umowa / regulamin organizacji")
                DemoDataField("passportLpMissionCertificate", "Zaświadczenie o podróży służbowej", fields, "numer, data, wystawca lub odniesienie do zaświadczenia")
                DemoDataField("passportLpMissionPurpose", "Cel i zakres podróży służbowej", fields)
                DemoDataField("passportLpDestination", "Państwo / państwa docelowe", fields)
                DemoDataField("passportLpRecognition", "Państwo uznające dokument / uwagi wizowe", fields)
                DemoDataField("passportLpIssueDate", "Data wydania laissez-passer", fields, "DD-MM-RRRR")
                DemoDataField("passportLpExpiryDate", "Data ważności laissez-passer", fields, "DD-MM-RRRR")
                DemoDataField("passportLpStatus", "Status laissez-passer", fields, "ważny / wygasły / utracony / unieważniony")
                DemoDataField("passportLpNotes", "Uwagi / ograniczenia dokumentu", fields, singleLine = false)
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Jak prawidłowo określić rodzaj paszportu?", color = Navy, fontWeight = FontWeight.Bold)
                        Text("O rodzaju dokumentu rozstrzyga dokument wydany przez właściwe państwo lub organizację międzynarodową, a nie sama funkcja reprezentanta. W Polsce ustawa o dokumentach paszportowych wyróżnia paszport, paszport tymczasowy, paszport dyplomatyczny i paszport służbowy MSZ. Laissez-passer jest odrębnym dokumentem podróży wydawanym na podstawie przepisów właściwej organizacji międzynarodowej; nie jest polskim paszportem dyplomatycznym. Paszport dyplomatyczny wydaje minister właściwy do spraw zagranicznych osobom wskazanym w ustawie, w tym w określonych przypadkach osobom skierowanym do pracy w organizacjach międzynarodowych. Fundacja użytkownika może być opisana w tej ewidencji jako międzynarodowa organizacja pozarządowa (NGO), jeżeli taki jest jej rzeczywisty status i zakres działania; należy odrębnie odnotować podstawę tego statusu, zasięg działalności i ewentualne uznanie lub akredytację. Sam międzynarodowy charakter NGO nie czyni jej organizacją międzyrządową ani nie uprawnia jej automatycznie do wydawania laissez-passer lub uzyskania polskiego paszportu dyplomatycznego.", color = Navy, style = MaterialTheme.typography.bodySmall)
                        Text("Przedstawiciel NGO co do zasady podróżuje na podstawie zwykłego paszportu swojego państwa oraz dokumentu potwierdzającego mandat, zaproszenie lub akredytację. Nie istnieje powszechna kategoria „paszportu NGO”. Organizacje międzyrządowe mogą wydawać własne dokumenty podróży uprawnionym osobom. Przykładowo ONZ wydaje UN laissez-passer swoim urzędnikom na podstawie sekcji 24 Konwencji o przywilejach i immunitetach ONZ; sekcja 26 przewiduje ułatwienia wizowe i podróżne także dla ekspertów oraz innych osób podróżujących w sprawach ONZ na podstawie odpowiedniego zaświadczenia, ale nie przyznaje im automatycznie dokumentu laissez-passer. Wpisz zatem rzeczywistego wystawcę i podstawę dokumentu.", color = Navy, style = MaterialTheme.typography.bodySmall)
                        Text("Przywileje i immunitety nie wynikają z samego posiadania paszportu ani tytułu reprezentanta. Zależą od właściwej podstawy prawnej, funkcji, akredytacji i uznania przez państwo przyjmujące.", color = Navy, style = MaterialTheme.typography.bodySmall)
                    }
                }
                AdditionalInformationSection("Informacje dodatkowe — dane paszportowe", fields, "passportExtra", "Jest to ewidencja danych wpisanych przez użytkownika, a nie dokument paszportowy ani jego cyfrowy odpowiednik. Nie odtwarzamy strefy MRZ, kodów ani zabezpieczeń dokumentu.")
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
                            ActivityHistoryStore.record(context, "Dokumenty", "Zapisano dane profilu i dokumentów")
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
                    Text("DANE DOKUMENTU", color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
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
                    when (authorityType) { "foundation" -> "DOKUMENT WYSTAWCY"; "other_law" -> "DOKUMENT NA PODSTAWIE ODRĘBNYCH PRZEPISÓW"; "medical_opinion" -> "OPINIA MEDYCZNA"; "self_declaration" -> "OŚWIADCZENIE OSOBY"; else -> "DANE EWIDENCYJNE" },
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
                Text("LEGITYMACJA UCZESTNIKA KURSU", color = White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


@Composable
private fun EntityRegistryExtractScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var selectedEntity by remember { mutableStateOf("company") }
    var status by remember { mutableStateOf("") }
    val sections = listOf(
        "company" to "Spółka z ograniczoną odpowiedzialnością — KRS",
        "foundation" to "Fundacja — KRS",
        "church" to "Kościół / związek wyznaniowy — rejestr poza KRS",
        "training" to "Instytucja szkoleniowa — rejestr poza KRS",
        "legalUnit" to "Jednostka lokalna osoby prawnej — REGON",
        "naturalUnit" to "Jednostka lokalna osoby fizycznej — REGON",
        "soleTrader" to "Firma osoby fizycznej — CEIDG / REGON"
    )
    val fieldSets = mapOf(
        "company" to listOf(
            Triple("registryCompanyName", "Pełna firma spółki", "Nazwa zgodna z odpisem"),
            Triple("registryCompanyKrs", "Numer KRS", "0000000000"),
            Triple("registryCompanyNip", "NIP", "0000000000"),
            Triple("registryCompanyRegon", "REGON", "000000000"),
            Triple("registryCompanySeat", "Siedziba", "Miejscowość"),
            Triple("registryCompanyAddress", "Adres siedziby", "Ulica, numer, kod, miejscowość"),
            Triple("registryCompanyCourt", "Sąd rejestrowy", "Nazwa sądu"),
            Triple("registryCompanyDate", "Data wpisu / odpisu", "DD-MM-RRRR"),
            Triple("registryCompanyCapital", "Kapitał zakładowy", "Kwota i waluta"),
            Triple("registryCompanyRepresentation", "Sposób reprezentacji", "Zasady reprezentacji"),
            Triple("registryCompanyBoard", "Zarząd", "Imiona, nazwiska, funkcje"),
            Triple("registryCompanyActivity", "Przedmiot działalności / PKD", "Opis lub kody PKD")
        ),
        "foundation" to listOf(
            Triple("registryFoundationName", "Nazwa fundacji", "Nazwa zgodna z odpisem"),
            Triple("registryFoundationKrs", "Numer KRS", "0000000000"),
            Triple("registryFoundationNip", "NIP", "0000000000"),
            Triple("registryFoundationRegon", "REGON", "000000000"),
            Triple("registryFoundationSeat", "Siedziba", "Miejscowość"),
            Triple("registryFoundationAddress", "Adres", "Ulica, numer, kod, miejscowość"),
            Triple("registryFoundationCourt", "Sąd rejestrowy", "Nazwa sądu"),
            Triple("registryFoundationDate", "Data wpisu / odpisu", "DD-MM-RRRR"),
            Triple("registryFoundationPurpose", "Cele statutowe", "Cele fundacji"),
            Triple("registryFoundationBoard", "Zarząd fundacji", "Imiona, nazwiska, funkcje"),
            Triple("registryFoundationRepresentation", "Sposób reprezentacji", "Zasady reprezentacji"),
            Triple("registryFoundationPublicBenefit", "Status OPP", "Tak / nie / data")
        ),
        "church" to listOf(
            Triple("registryChurchName", "Pełna nazwa kościoła / związku", "Nazwa"),
            Triple("registryChurchRegister", "Nazwa właściwego rejestru", "Rejestr kościołów i innych związków wyznaniowych lub inny właściwy rejestr"),
            Triple("registryChurchNumber", "Numer wpisu / pozycja rejestru", "Numer"),
            Triple("registryChurchAuthority", "Organ prowadzący rejestr", "Nazwa organu"),
            Triple("registryChurchDate", "Data wpisu / zaświadczenia", "DD-MM-RRRR"),
            Triple("registryChurchSeat", "Siedziba", "Miejscowość"),
            Triple("registryChurchAddress", "Adres", "Adres"),
            Triple("registryChurchLegalBasis", "Podstawa prawna / status", "Podstawa"),
            Triple("registryChurchRepresentation", "Osoby uprawnione do reprezentacji", "Imiona, nazwiska, funkcje"),
            Triple("registryChurchDocument", "Dokument źródłowy", "Nazwa i data dokumentu")
        ),
        "training" to listOf(
            Triple("registryTrainingName", "Pełna nazwa instytucji szkoleniowej", "Nazwa"),
            Triple("registryTrainingRegister", "Rejestr / ewidencja", "Np. RIS, BUR lub inny właściwy rejestr"),
            Triple("registryTrainingNumber", "Numer wpisu / identyfikator", "Numer"),
            Triple("registryTrainingAuthority", "Organ / operator rejestru", "Nazwa"),
            Triple("registryTrainingNip", "NIP", "0000000000"),
            Triple("registryTrainingRegon", "REGON", "000000000"),
            Triple("registryTrainingSeat", "Siedziba i adres", "Adres"),
            Triple("registryTrainingDate", "Data wpisu / aktualizacji", "DD-MM-RRRR"),
            Triple("registryTrainingScope", "Zakres działalności szkoleniowej", "Zakres"),
            Triple("registryTrainingAccreditation", "Akredytacje / uprawnienia", "Nazwa, zakres, numer, data"),
            Triple("registryTrainingDocument", "Dokument źródłowy", "Nazwa i data dokumentu")
        ),
        "legalUnit" to listOf(
            Triple("registryLegalUnitParentName", "Nazwa osoby prawnej — jednostki nadrzędnej", "Pełna nazwa"),
            Triple("registryLegalUnitParentKrs", "KRS jednostki nadrzędnej (jeśli dotyczy)", "0000000000"),
            Triple("registryLegalUnitParentRegon", "REGON jednostki nadrzędnej", "000000000"),
            Triple("registryLegalUnitName", "Nazwa jednostki lokalnej", "Nazwa oddziału / zakładu / placówki"),
            Triple("registryLegalUnitRegon", "REGON jednostki lokalnej", "000000000"),
            Triple("registryLegalUnitNip", "NIP jednostki lokalnej (jeśli nadany)", "0000000000"),
            Triple("registryLegalUnitType", "Rodzaj jednostki lokalnej", "Oddział, zakład, placówka, filia"),
            Triple("registryLegalUnitAddress", "Adres jednostki lokalnej", "Ulica, numer, kod, miejscowość"),
            Triple("registryLegalUnitStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registryLegalUnitEnd", "Data zakończenia działalności (jeśli dotyczy)", "DD-MM-RRRR"),
            Triple("registryLegalUnitActivity", "Przeważający rodzaj działalności / PKD", "Kod i opis"),
            Triple("registryLegalUnitContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        ),
        "naturalUnit" to listOf(
            Triple("registryNaturalUnitPerson", "Imię i nazwisko osoby fizycznej", "Dane osoby prowadzącej"),
            Triple("registryNaturalUnitParentRegon", "REGON podmiotu nadrzędnego (jeśli dotyczy)", "000000000"),
            Triple("registryNaturalUnitName", "Nazwa jednostki lokalnej", "Nazwa placówki / zakładu"),
            Triple("registryNaturalUnitRegon", "REGON jednostki lokalnej", "000000000"),
            Triple("registryNaturalUnitNip", "NIP (jeśli nadany)", "0000000000"),
            Triple("registryNaturalUnitType", "Rodzaj jednostki lokalnej", "Zakład, placówka, punkt"),
            Triple("registryNaturalUnitAddress", "Adres jednostki lokalnej", "Ulica, numer, kod, miejscowość"),
            Triple("registryNaturalUnitStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registryNaturalUnitEnd", "Data zakończenia działalności (jeśli dotyczy)", "DD-MM-RRRR"),
            Triple("registryNaturalUnitActivity", "Rodzaj działalności / PKD", "Kod i opis"),
            Triple("registryNaturalUnitContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        ),
        "soleTrader" to listOf(
            Triple("registrySoleTraderName", "Firma przedsiębiorcy", "Imię i nazwisko oraz ewentualne dodatki"),
            Triple("registrySoleTraderFirstName", "Imię", "Imię"),
            Triple("registrySoleTraderLastName", "Nazwisko", "Nazwisko"),
            Triple("registrySoleTraderNip", "NIP", "0000000000"),
            Triple("registrySoleTraderRegon", "REGON", "000000000"),
            Triple("registrySoleTraderCeidg", "Identyfikator / wpis CEIDG", "Numer lub identyfikator"),
            Triple("registrySoleTraderStatus", "Status działalności", "Aktywna, zawieszona, zakończona"),
            Triple("registrySoleTraderAddress", "Adres do doręczeń", "Adres"),
            Triple("registrySoleTraderPlace", "Stałe miejsce wykonywania działalności", "Adres lub informacja o braku stałego miejsca"),
            Triple("registrySoleTraderStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registrySoleTraderSuspension", "Data zawieszenia / wznowienia", "Daty"),
            Triple("registrySoleTraderEnd", "Data zakończenia działalności", "DD-MM-RRRR"),
            Triple("registrySoleTraderActivity", "Przeważający rodzaj działalności / PKD", "Kod i opis"),
            Triple("registrySoleTraderProxy", "Pełnomocnik / prokurent (jeśli wpisany)", "Imię, nazwisko, zakres umocowania"),
            Triple("registrySoleTraderContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        )
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Menu główne", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Odpis z rejestru podmiotów", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Ewidencja danych identyfikacyjnych i rejestrowych podmiotów. Wybierz rodzaj podmiotu i przepisz dane z właściwego dokumentu lub rejestru.",
            color = Muted, style = MaterialTheme.typography.bodyMedium)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FA))) {
            Text("Wpisy w tym module są lokalnym zapisem danych. Nie stanowią urzędowego odpisu, nie potwierdzają aktualnego statusu i nie pobierają danych z KRS ani innych rejestrów.",
                Modifier.padding(14.dp), color = Navy, style = MaterialTheme.typography.bodySmall)
        }
        sections.forEach { (key, label) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { selectedEntity = key },
                colors = CardDefaults.cardColors(containerColor = if (selectedEntity == key) BlueLight else White)
            ) {
                Text(label, Modifier.fillMaxWidth().padding(13.dp),
                    color = if (selectedEntity == key) Blue else Navy,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        Text(sections.first { it.first == selectedEntity }.second, color = Navy,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        fieldSets[selectedEntity].orEmpty().forEach { (key, label, placeholder) ->
            DemoDataField(key, label, fields, placeholder)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
        Text("Źródło i kontrola aktualności", color = Navy, fontWeight = FontWeight.Bold)
        DemoDataField("registry_${selectedEntity}_source", "Źródło danych / adres rejestru", fields, "Adres strony lub nazwa dokumentu")
        DemoDataField("registry_${selectedEntity}_checked", "Data sprawdzenia", fields, "DD-MM-RRRR")
        DemoDataField("registry_${selectedEntity}_documentDate", "Data sporządzenia odpisu / dokumentu", fields, "DD-MM-RRRR")
        DemoDataField("registry_${selectedEntity}_notes", "Uwagi", fields, "Informacje uzupełniające")
        Button(onClick = {
            runCatching { DemoIdentityStore.save(context, fields.toMap()) }
                .onSuccess { status = "Zapisano dane lokalnie i zaszyfrowano." }
                .onFailure { status = "Nie udało się zapisać danych: ${it.message}" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz dane podmiotu") }
        if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) Blue else Color(0xFFB3261E),
            style = MaterialTheme.typography.bodySmall)
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
                    "Informacje dodatkowe uzupełniają zapis danych o źródło, podstawę i datę weryfikacji.",
                    color = Muted, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DemoDataField(key: String, label: String, fields: MutableMap<String, String>, placeholder: String = "Wpisz dane", singleLine: Boolean = true) {
    OutlinedTextField(
        value = fields[key] ?: "",
        onValueChange = { fields[key] = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine
    )
}

private object DemoIdentityStore {
    private const val KEY_ALIAS = "obywatel_demo_identity_aes"
    private const val FILE_NAME = "identity_profile.enc"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun load(context: Context): Map<String, String> = runCatching { loadStrict(context) }.getOrElse { emptyMap() }

    fun loadStrict(context: Context): Map<String, String> {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()
        val encrypted = file.readBytes()
        require(encrypted.size > 12) { "Nieprawidłowy zaszyfrowany plik profilu." }
        val iv = encrypted.copyOfRange(0, 12)
        val ciphertext = encrypted.copyOfRange(12, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONObject(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        val result = mutableMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.matches(Regex("[A-Za-z0-9-]{1,64}"))) { "Nieprawidłowa nazwa pola profilu." }
            require(!json.isNull(key)) { "Pole profilu zawiera wartość pustą typu null." }
            result[key] = json.getString(key)
        }
        return result
    }

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
