// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import pl.gov.obywatel.android.network.tor.TorClientSettingsScreen
import pl.gov.obywatel.android.network.tor.StudiaOnlineBulkDownloadScreen

import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.util.UUID
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
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
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.ObywatelTheme
import pl.gov.obywatel.android.ui.identity.IdentityDocumentsScreen
import pl.gov.obywatel.android.ui.documents.DocumentsScreen
import pl.gov.obywatel.android.ui.services.PublicServicesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun documentVisibilityGroup(id: String): String = when {
    id == "fund_purpose_disposition" -> "Służba państwowa"
    id == "financial_service" || DonationLedgerCatalog.find(id) != null -> "Służba finansowa"
    id == "payment_account_foundation" -> "Służba fundacyjna"
    id == "payment_account_association" -> "Służba stowarzyszeniowa"
    id == "payment_account_church" -> "Służba kościelna"
    id == "payment_account_business" -> "Służba gospodarcza"
    id == "payment_account_person" -> "Służba osobista"
    id == INTERNATIONAL_DONATION_WORSHIP_ID -> "Służba kościelna"
    id == INTERNATIONAL_DONATION_NGO_ID -> "Służba społeczna"
    id == INTERNATIONAL_DONATION_PERSON_ID -> "Służba cywilna"
    DonationLedgerCatalog.find(id) != null -> DonationLedgerCatalog.find(id)!!.group
    id == "icvp" -> "Służba zdrowotna"
    id == "medical_summary" -> "Służba zdrowotna"
    id == "allergy_card" -> "Służba zdrowotna"
    id == "medication_card" -> "Służba zdrowotna"
    id == "fit_to_travel" -> "Służba zdrowotna"
    id == "fit_to_fly" -> "Służba zdrowotna"
    id == "rehabilitation_report" -> "Służba zdrowotna"
    id == "functional_report" -> "Służba zdrowotna"
    id == "disability_opinion" -> "Służba zdrowotna"
    id == "eu_parking_card" -> "Służba zdrowotna"
    id == "prc" -> "Służba zdrowotna"
    id == "s1" -> "Służba zdrowotna"
    id == "s2" -> "Służba zdrowotna"
    id == "s3" -> "Służba zdrowotna"
    id == "da1" -> "Służba zdrowotna"
    id == "cross_border_prescription" -> "Służba zdrowotna"
    id == "refund_claim" -> "Służba zdrowotna"
    id == "a1" -> "Służba finansowa"
    id == "u1" -> "Służba finansowa"
    id == "u2" -> "Służba finansowa"
    id == "epc" -> "Służba zawodowa"
    id == "good_standing" -> "Służba zawodowa"
    id == "professional_status" -> "Służba zawodowa"
    id == "qualification_recognition" -> "Służba zawodowa"
    id == "diploma" -> "Służba zawodowa"
    id == "education_certificate" -> "Służba zawodowa"
    id == "diploma_supplement" -> "Służba zawodowa"
    id == "birth_certificate" -> "Służba cywilna"
    id == "marriage_certificate" -> "Służba cywilna"
    id == "death_certificate" -> "Służba cywilna"
    id == "marital_status" -> "Służba cywilna"
    id == "capacity_to_marry" -> "Służba cywilna"
    id == "residence_certificate" -> "Służba cywilna"
    id == "citizenship_certificate" -> "Służba cywilna"
    id == "life_certificate" -> "Służba cywilna"
    id == "criminal_record_foreign" -> "Służba cywilna"
    id == "divorce_decree" -> "Służba cywilna"
    id == "adoption_certificate" -> "Służba cywilna"
    id == "parentage_certificate" -> "Służba cywilna"
    id == "international_driving_permit" -> "Służba osobista"
    id == "travel_document" -> "Służba osobista"
    id == "residence_card" -> "Służba osobista"
    id == "passport_record" -> "Służba osobista"
    id == "vatican_passport_ordinary" -> "Służba osobista"
    id == "holy_see_passport_diplomatic" -> "Służba osobista"
    id == "holy_see_passport_service" -> "Służba osobista"
    id == "holy_see_passport_temporary_service" -> "Służba osobista"
    id == "vatican_citizenship_certificate" -> "Służba cywilna"
    id == "ecclesiastical_baptism_certificate" -> "Służba kościelna"
    id == "ecclesiastical_marriage_certificate" -> "Służba kościelna"
    id == "ecclesiastical_death_certificate" -> "Służba kościelna"
    id == "canonical_status_certificate" -> "Służba kościelna"
    id == "clerical_ordination_certificate" -> "Służba kościelna"
    id == "apostolic_nunciature_credential" -> "Służba kościelna"
    id in armedServiceDocumentIds -> "Służba zbrojna"
    id in setOf("profile", "identity", "passport") -> "Służba osobista"
    id == "church" || id.startsWith("church_") || id == "organization_church" ||
        id == "mission_travel" || churchGraphicDocumentTargets().any { it.first == id } -> "Służba kościelna"
    id == "family" || (id.startsWith("rod_") && id != "rod_bank_accounts") || id == "large_family" -> "Służba rodowa"
    id in setOf("financial_service", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts", "payment_transactions") ||
        id.startsWith("payment_account_") || id.startsWith("insurance_") || id.startsWith("donation_ledger_") ||
        id in setOf(INTERNATIONAL_DONATION_WORSHIP_ID, INTERNATIONAL_DONATION_NGO_ID, INTERNATIONAL_DONATION_PERSON_ID) -> "Służba finansowa"
    id.startsWith("service_foundation_") -> "Służba fundacyjna"
    id.startsWith("service_business_") -> "Służba gospodarcza"
    id.startsWith("service_association_") -> "Służba stowarzyszeniowa"
    id in setOf("org_president", "org_general_director", "org_regional_director", "org_county_director", "org_local_representative", "org_general_secretary", "org_treasurer", "org_ambassador", "org_foreign_representative", "org_proxy", "org_delegate") -> "Służba gospodarcza"
    id in setOf("org_members_rights_ombudsman", "org_patient_ombudsman", "org_family_ombudsman", "civil_mediator", "civil_arbitrator", "civil_social_caretaker", "civil_org_curator") -> "Służba cywilna"
    id in setOf("civil_expert", "civil_appraiser", "civil_auditor", "civil_internal_inspector", "civil_controller", "civil_consultant") -> "Służba zawodowa"
    id == "civil_rescuer" -> "Służba zdrowotna"
    id == "parliament" || id in setOf("mayor", "village_head", "county_starosta", "citizen_record", "ombudsman", "children_ombudsman", "patient_ombudsman", "pretrial_mediator", "milicja_officer", "foreign_service_member", "diplomat", "consul", "ambassador", "foreign_mission_head", "consular_employee", "inspector") -> "Służba państwowa"
    id == "organization_foundation" -> "Służba fundacyjna"
    id == "organization_association" -> "Służba stowarzyszeniowa"
    id == "organization_company" || id == "business" || id == "entity" || id == "registry" ||
        id.startsWith("entity_") || id.startsWith("organization_") -> "Służba gospodarcza"
    id == "professional" || id.startsWith("professional_") ||
        id in setOf("training", "student", "eu_education") -> "Służba zawodowa"
    id in setOf("disability", "disability_card", "blood_donor", "ehic", "vaccines", "children", "international_medical_certificate", "foreign_prescription") -> "Służba zdrowotna"
    id == "civil" || id in setOf(
        "birth_extract", "marriage_extract", "death_extract", "criminal_record",
        "vehicle_registration", "official"
    ) || officialGraphicDocumentTargets().any { it.first == id } -> "Służba cywilna"
    id in setOf("imported", "signed_pdf", "receipts", "branding", "loyalty", "loyalty_cards") ||
        documentBrandTargets.any { it.first == id } -> "Zasoby i dokumenty własne"
    else -> "Służba osobista"
}
internal val documentVisibilityGroupOrder = listOf(
    "Służba osobista", "Służba rodowa", "Służba cywilna", "Służba państwowa",
    "Służba kościelna", "Służba zbrojna", "Służba zawodowa",
    "Służba fundacyjna", "Służba gospodarcza", "Służba stowarzyszeniowa", "Służba społeczna",
    "Służba finansowa", "Służba zdrowotna", "Zasoby i dokumenty własne"
)
internal fun documentGroupIconResource(group: String): Int = when (group) {
    "Służba osobista" -> R.drawable.lib_ic_account
    "Służba rodowa" -> R.drawable.lib_ic_group
    "Służba cywilna", "Służba państwowa" -> R.drawable.tile_ic_civil_outline
    "Służba kościelna" -> R.drawable.tile_ic_church_outline
    "Służba zbrojna" -> R.drawable.tile_ic_armed_service_outline
    "Służba zawodowa" -> R.drawable.tile_ic_student_outline
    "Służba fundacyjna", "Służba gospodarcza", "Służba stowarzyszeniowa", "Służba społeczna" -> R.drawable.tile_ic_business_outline
    "Służba finansowa" -> R.drawable.lib_ic_wallet
    "Służba zdrowotna" -> R.drawable.tile_ic_disability_outline
    "Zasoby i dokumenty własne" -> R.drawable.lib_ic_folder
    else -> R.drawable.lib_ic_document
}
internal val armedServiceDocumentIds = setOf(
    "military_book", "martial_arts", "shooting_sport", "archery",
    "tactical_training", "survival_training", "security_guard", "defense_training",
    "historical_reenactment", "reenactment_qualification", "reenactment_event"
)
internal val defaultDocumentPlacements = documentDisplayOptions.associate { (id, _, _) ->
    id to when {
        id in setOf("identity", "receipts", "foreign_prescription", "payment_transactions") -> "dashboard"
        id == "financial_service" -> "other"
        // Donation ledger cards are available in the catalog but never shown by default.
        id in setOf(DonationLedgerCatalog.PERSON, DonationLedgerCatalog.FOUNDATION,
            DonationLedgerCatalog.ASSOCIATION, DonationLedgerCatalog.CHURCH) -> "hidden"
        else -> "hidden"
    }
}
internal const val DOCUMENT_VISIBILITY_MIGRATION_KEY = "all_hidden_except_identity_v1"
internal const val DONATION_LEDGER_VISIBILITY_MIGRATION_KEY = "donation_ledgers_hidden_by_default_v1"
internal const val ARMED_SERVICE_VISIBILITY_MIGRATION_KEY = "armed_service_group_visible_v1"
internal const val ARMED_SERVICE_DASHBOARD_CLEANUP_MIGRATION_KEY = "armed_service_dashboard_cleanup_v1"

internal data class ServiceItem(
    val title: String,
    val subtitle: String,
    val icon: Int? = null,
    val fallbackIcon: ImageVector? = null,
    val category: String = "Pozostałe"
)

internal val services = listOf(
    ServiceItem("Portfel", "Wszystkie dokumenty i ewidencje", icon = R.drawable.service_folder, fallbackIcon = Icons.Filled.CreditCard, category = "Dokumenty"),
    ServiceItem("Odpis z rejestru podmiotów", "Dane spółki, fundacji, kościoła i instytucji szkoleniowej", fallbackIcon = Icons.Filled.AccountBalance, category = "Rejestry"),
    ServiceItem("Polak za granicą", "Informacje konsularne", icon = R.drawable.mob_abroad, fallbackIcon = Icons.Filled.Public, category = "Transport"),
    ServiceItem("Płatności", "Usługi i opłaty", icon = R.drawable.mob_payments, fallbackIcon = Icons.Filled.Payments, category = "Sprawy urzędowe"),
    ServiceItem("Zdrowie", "Informacje zdrowotne", icon = R.drawable.mob_health, fallbackIcon = Icons.Filled.LocalHospital, category = "Zdrowie"),
    ServiceItem("eKarta Szczepień", "Książeczka szczepień i historia dawek", icon = R.drawable.service_prescription, fallbackIcon = Icons.Filled.HealthAndSafety, category = "Zdrowie"),
    ServiceItem("Dane dzieci", "Rodzinny rejestr danych i dokumentacji dzieci", fallbackIcon = Icons.Filled.AccountCircle, category = "Rodzina"),
    ServiceItem("Formularze dokumentów", "Akty i dokumenty cywilne — ewidencja i załączniki", icon = R.drawable.mob_document_check, fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Dokumenty Rodowe", "Księgi, akty, rejestry i dokumenty Rodu", fallbackIcon = Icons.Filled.Fingerprint, category = "Rodzina"),
    ServiceItem("Dokumenty rejestru podmiotów", "Dokumenty firmowe i organizacyjne", fallbackIcon = Icons.Filled.AccountBalance, category = "Rejestry"),
    ServiceItem("Dokumenty Kościelne", "Dokumenty kościelne, liturgiczne i organizacyjne", fallbackIcon = Icons.Filled.AccountBalance, category = "Kościół i wspólnoty"),
    ServiceItem("Zasoby identyfikacji dokumentów", "Logo, godła, flagi, pieczęcie i tła dokumentów", fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Dokumenty PDF i podpisy elektroniczne", "Import, przechowywanie i podgląd dokumentów PDF", fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Transport", "Podróże i przejazdy", icon = R.drawable.mob_transport, fallbackIcon = Icons.Filled.Train, category = "Podróże"),
    ServiceItem("Mandaty", "Ewidencja i informacje", icon = R.drawable.mob_penalties, fallbackIcon = Icons.Filled.Description, category = "Kierowca i pojazdy"),
    ServiceItem("Sprawdź PESEL", "Informacje o numerze PESEL", icon = R.drawable.mob_pesel_check, fallbackIcon = Icons.Filled.Fingerprint, category = "Bezpieczeństwo"),
    ServiceItem("Zastrzeż PESEL", "Informacje o zastrzeżeniu", icon = R.drawable.mob_pesel_lock, fallbackIcon = Icons.Filled.Security, category = "Bezpieczeństwo"),
    ServiceItem("Bilety", "Bilety i przejazdy", icon = R.drawable.mob_ticket, fallbackIcon = Icons.Filled.ConfirmationNumber, category = "Podróże"),
    ServiceItem("Bezpieczeństwo", "Prywatność i ochrona", icon = R.drawable.mob_cyber_safety, fallbackIcon = Icons.Filled.Shield, category = "Bezpieczeństwo"),
    ServiceItem("Środowisko", "Informacje środowiskowe", icon = R.drawable.mob_environment, fallbackIcon = Icons.Filled.Public, category = "Środowisko"),
    ServiceItem("Ustawienia", "Preferencje i dane ewidencyjne", fallbackIcon = Icons.Filled.Settings),
    ServiceItem("Zaświadczenia", "Dokumenty i dane o uprawnieniach", fallbackIcon = Icons.Filled.Description),
    ServiceItem("Sprawy rodzinne", "Dane dotyczące spraw rodzinnych", fallbackIcon = Icons.Filled.AccountCircle, category = "Rodzina"),
    ServiceItem("Edukacja", "Ewidencja nauki i dokumentów edukacyjnych", fallbackIcon = Icons.Filled.Description, category = "Edukacja"),
    ServiceItem("Praca", "Informacje i dokumenty związane z pracą", fallbackIcon = Icons.Filled.Badge, category = "Praca"),
    ServiceItem("Finanse i podatki", "Informacje dotyczące finansów i podatków", fallbackIcon = Icons.Filled.Payments, category = "Finanse i podatki"),
    ServiceItem("Nieruchomości", "Ewidencja nieruchomości użytkownika", fallbackIcon = Icons.Filled.Home, category = "Nieruchomości"),
    ServiceItem("Prawo i obywatel", "Informacje obywatelskie i prawne", fallbackIcon = Icons.Filled.AccountBalance, category = "Prawo i obywatel"),
    ServiceItem("Kultura i dziedzictwo", "Katalog instytucji i wydarzeń", fallbackIcon = Icons.Filled.Public, category = "Kultura i dziedzictwo"),
    ServiceItem("Sport", "Aktywność i lokalne obiekty sportowe", fallbackIcon = Icons.Filled.HealthAndSafety, category = "Sport"),
    ServiceItem("Organizacje społeczne", "Katalog organizacji i inicjatyw", fallbackIcon = Icons.Filled.AccountCircle, category = "Organizacje społeczne"),
    ServiceItem("Kościół i wspólnoty", "Katalog wspólnot i instytucji religijnych", fallbackIcon = Icons.Filled.AccountBalance, category = "Kościół i wspólnoty"),
    ServiceItem("Pozostałe", "Więcej modułów", fallbackIcon = Icons.Filled.MoreHoriz)
)


@Composable
internal fun HomeScreen(onAppearanceChange: (ApplicationAppearance) -> Unit) {
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<Map<String, String>?>(null) }
    var importStatus by remember { mutableStateOf("") }
    var exportSnapshot by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var importBusy by remember { mutableStateOf(false) }
    var exportBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val exportVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-vcard")) { uri ->
        if (uri != null) {
            pendingExportUri = uri
            importStatus = "Wybrano miejsce zapisu. Potwierdź zapis archiwum; istniejący plik o tej nazwie może zostać zastąpiony."
        } else {
            importStatus = "Eksport anulowany — plik nie został zapisany."
        }
    }
    val importVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                importBusy = true
                importStatus = "Odczytywanie i sprawdzanie pliku vCard…"
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            var total = 0
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                require(total <= 256 * 1024 * 1024) { "Plik vCard przekracza limit 256 MB." }
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
                        }.getOrElse { String(bytes, java.nio.charset.Charset.forName("windows-1250")) }
                        VCardCodec.decodeForImport(raw) { percent ->
                            scope.launch(Dispatchers.Main) { importStatus = "Odczytywanie vCard: $percent%" }
                        }.also {
                            require(it.isNotEmpty()) { "Archiwum nie zawiera danych." }
                        }
                    }
                }
                result.onSuccess {
                    pendingImport = it
                    importStatus = "Plik odczytany i poprawny. Sprawdź zakres danych przed zatwierdzeniem."
                }.onFailure {
                    pendingImport = null
                    importStatus = "Błąd importu: ${it.message ?: "nieprawidłowy plik"}"
                }
                importBusy = false
            }
        } else {
            importStatus = "Import anulowany — dane lokalne pozostają bez zmian."
        }
    }
    var documentPlacements by remember(context) { mutableStateOf(loadDocumentPlacements(context)) }
    var documentGroupConfiguration by remember(context) { mutableStateOf(loadDocumentGroupConfiguration(context)) }
    var documentCardStyle by remember(context) { mutableStateOf(loadDocumentCardStyle(context)) }
    var defaultDocumentIcon by remember(context) { mutableStateOf(loadDefaultDocumentIcon(context)) }
    var documentIconOverrides by remember(context) { mutableStateOf(loadDocumentIconOverrides(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeService by remember { mutableStateOf<String?>(null) }
    var showDemoDocuments by remember { mutableStateOf(false) }
    var showDocumentsEditor by remember { mutableStateOf(false) }
    var selectedEditorCategory by remember { mutableStateOf("all") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showTorClientSettings by remember { mutableStateOf(false) }
    var showStudiaOnlineBulkDownload by remember { mutableStateOf(false) }
    var showDocumentEditorCatalog by remember { mutableStateOf(false) }
    var showApplicationAppearanceSettings by remember { mutableStateOf(false) }
    var showTranslationLanguageSettings by remember { mutableStateOf(false) }
    var showDocumentVisibilitySettings by remember { mutableStateOf(false) }
    var showDocumentBrandAssets by remember { mutableStateOf(false) }
    var documentBrandTarget by remember { mutableStateOf<String?>(null) }
    var showRegistryEditor by remember { mutableStateOf(false) }
    var settingsDocumentEditor by remember { mutableStateOf<String?>(null) }
    var graphicEditorReturnTarget by remember { mutableStateOf("graphic_catalog") }
    var credentialAppearanceTarget by remember { mutableStateOf<String?>(null) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var selectedMedicalCertificateRecordId by remember { mutableStateOf<String?>(null) }
    var showDisabilitySettings by remember { mutableStateOf(false) }
    var showCertificates by remember { mutableStateOf(false) }
    var showIdentityDesigner by remember { mutableStateOf(false) }
    var showCredentialAppearanceEditor by remember { mutableStateOf(false) }
    var showDefaultDocumentAppearanceSettings by remember { mutableStateOf(false) }
    var showCertificateAppearanceSettings by remember { mutableStateOf(false) }
    var showImportedDocuments by remember { mutableStateOf(false) }
    var showReceipts by remember { mutableStateOf(false) }
    var showPaymentLedger by remember { mutableStateOf(false) }
    var activeDonationLedgerId by remember { mutableStateOf<String?>(null) }
    var showBankAccounts by remember { mutableStateOf(false) }
    var showPublicServicesBrowser by remember { mutableStateOf(false) }
    var serviceSearch by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("Wszystkie") }
    val favoriteServices = remember { mutableStateMapOf("Zastrzeż PESEL" to true, "Zdrowie" to true, "Transport" to true) }

    // Wszystkie wejścia do dodawania i zarządzania dokumentami kierują do jednego katalogu.
    fun openDocumentVisibilityCatalog() {
        showDemoDocuments = false
        showSettingsMenu = false
        showDocumentVisibilitySettings = true
    }

    fun openSettingsDocumentEditor(documentId: String) {
        showDocumentEditorCatalog = false
        showSettingsMenu = false
        showDemoDocuments = false
        selectedDocumentId = null
        activeService = null
        when {
            documentId == "profile" -> { selectedEditorCategory = "profile"; showDocumentsEditor = true }
            documentId == "identity" -> { selectedEditorCategory = "identity_card"; showDocumentsEditor = true }
            documentId == "passport" -> { selectedEditorCategory = "passport"; showDocumentsEditor = true }
            documentId == "parliament" -> { selectedEditorCategory = "parliament"; showDocumentsEditor = true }
            documentId == "training" -> { selectedEditorCategory = "training"; showDocumentsEditor = true }
            documentId == "student" -> { selectedEditorCategory = "student"; showDocumentsEditor = true }
            documentId == "loyalty" || documentId == "loyalty_cards" -> { selectedEditorCategory = "loyalty_cards"; showDocumentsEditor = true }
            documentId == "disability" -> showDisabilitySettings = true
            documentId == "registry" -> showRegistryEditor = true
            documentId == "bank_accounts" -> settingsDocumentEditor = "bank_accounts:rod_bank_accounts"
            documentId == "rod_bank_accounts" -> settingsDocumentEditor = "bank_accounts:rod_bank_accounts"
            documentId == "entity_bank_accounts" -> settingsDocumentEditor = "entity_bank_accounts:entity_bank_accounts"
            documentId == "family" -> settingsDocumentEditor = "family"
            documentId.startsWith("rod_") -> settingsDocumentEditor = "family:$documentId"
            documentId == "church" -> settingsDocumentEditor = "church"
            documentId == "mission_travel" -> { selectedEditorCategory = "catalog:mission_travel"; showDocumentsEditor = true }
            documentId.startsWith("church_") || churchGraphicDocumentTargets().any { it.first == documentId } -> {
                if (churchGraphicDocumentTargets().any { it.first == documentId }) {
                    settingsDocumentEditor = "church:$documentId"
                } else {
                    // Category aliases such as church_bishop have their own
                    // schema-backed form; they must not fall back to the group list.
                    selectedEditorCategory = "catalog:$documentId"
                    showDocumentsEditor = true
                }
            }
            documentId == "civil" -> settingsDocumentEditor = "civil"
            documentId in setOf("birth_extract", "marriage_extract", "death_extract", "vehicle_registration", "official") ||
                officialGraphicDocumentTargets().any { it.first == documentId } -> settingsDocumentEditor = "civil:$documentId"
            documentId == "business" || documentId == "entity" -> settingsDocumentEditor = "business"
            documentId.startsWith("organization_") -> { selectedEditorCategory = "catalog:$documentId"; showDocumentsEditor = true }
            documentId.startsWith("service_") -> settingsDocumentEditor = "service:$documentId"
                        documentId.startsWith("entity_") -> settingsDocumentEditor = "business:$documentId"
            DonationLedgerCatalog.find(documentId) != null -> settingsDocumentEditor = "donation_ledger:$documentId"
            documentId in InternationalDonationDocumentCatalog.all.keys -> selectedDocumentId = documentId
            documentId == "vaccines" -> settingsDocumentEditor = "vaccines"
            documentId == "children" -> settingsDocumentEditor = "children_health"
            documentId == "signed_pdf" -> settingsDocumentEditor = "signed_pdf"
            documentId == "receipts" -> showReceipts = true
            documentId == "payment_transactions" -> showPaymentLedger = true
            documentId == "imported" -> settingsDocumentEditor = "imported"
            documentId == "professional" -> settingsDocumentEditor = "professional"
            documentId == "branding" -> settingsDocumentEditor = "graphic_catalog"
            else -> { selectedEditorCategory = "catalog:$documentId"; showDocumentsEditor = true }
        }
    }

    fun handleSystemBack() {
        when {
            selectedMedicalCertificateRecordId != null -> selectedMedicalCertificateRecordId = null
            credentialAppearanceTarget != null -> credentialAppearanceTarget = null
            showCertificateAppearanceSettings -> { showCertificateAppearanceSettings = false; showDefaultDocumentAppearanceSettings = true }
            showDefaultDocumentAppearanceSettings -> { showDefaultDocumentAppearanceSettings = false; showSettingsMenu = true }
            showCredentialAppearanceEditor -> { showCredentialAppearanceEditor = false; showDefaultDocumentAppearanceSettings = true }
            showTranslationLanguageSettings -> { showTranslationLanguageSettings = false; showSettingsMenu = true }
            showTorClientSettings -> { showTorClientSettings = false; showSettingsMenu = true }
            showStudiaOnlineBulkDownload -> { showStudiaOnlineBulkDownload = false; selectedTab = 4 }
            showApplicationAppearanceSettings -> { showApplicationAppearanceSettings = false; showSettingsMenu = true }
            showIdentityDesigner -> { showIdentityDesigner = false }
            showPublicServicesBrowser -> { showPublicServicesBrowser = false }
            showImportedDocuments -> showImportedDocuments = false
            showReceipts -> showReceipts = false
            activeDonationLedgerId != null -> { activeDonationLedgerId = null; showPaymentLedger = true }
            showPaymentLedger -> showPaymentLedger = false
            showBankAccounts -> showBankAccounts = false
            showDocumentsEditor -> { showDocumentsEditor = false; showSettingsMenu = true }
            showRegistryEditor -> { showRegistryEditor = false; showSettingsMenu = true }
            settingsDocumentEditor != null -> { settingsDocumentEditor = null; showSettingsMenu = true }
            showDocumentVisibilitySettings -> { showDocumentVisibilitySettings = false; showSettingsMenu = true }
            documentBrandTarget != null -> { documentBrandTarget = null; showSettingsMenu = true }
            showDocumentBrandAssets -> { showDocumentBrandAssets = false; showSettingsMenu = true }
            showDocumentEditorCatalog -> { showDocumentEditorCatalog = false; showSettingsMenu = true }
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
        enabled = credentialAppearanceTarget != null || showDefaultDocumentAppearanceSettings || showCertificateAppearanceSettings || showCredentialAppearanceEditor || showApplicationAppearanceSettings || showTranslationLanguageSettings || showTorClientSettings || showStudiaOnlineBulkDownload || showIdentityDesigner || showPublicServicesBrowser || showImportedDocuments || showReceipts || activeDonationLedgerId != null || showPaymentLedger || showBankAccounts || showDocumentsEditor || showSettingsMenu || showDocumentEditorCatalog || showDocumentVisibilitySettings || showDocumentBrandAssets || documentBrandTarget != null || showRegistryEditor || settingsDocumentEditor != null || selectedDocumentId != null ||
            showDemoDocuments || showDisabilitySettings || showCertificates ||
            activeService != null || selectedTab != 0,
        onBack = ::handleSystemBack
    )

    fun openService(title: String) {
        when (title) {
            "Służba finansowa" -> showPaymentLedger = true
            "Portfel" -> showDemoDocuments = true
            "Ustawienia" -> {
                selectedTab = 4
                showSettingsMenu = false
                showDemoDocuments = false
                selectedDocumentId = null
            }
            "Zaświadczenia" -> showCertificates = true
            else -> activeService = title
        }
    }

    fun openDocumentCard(documentId: String) {
        // The same route is used from both home sections and the wallet.
        // Keep the source ID intact unless the registry declares a legacy alias.
        val route = DocumentRouteRegistry.find(documentId)
        if (route == null) {
            openSettingsDocumentEditor(documentId)
            return
        }
        when (route.type) {
            DocumentRouteType.GROUP -> {
                if (route.destination == "Służba finansowa") showPaymentLedger = true
                else activeService = requireNotNull(route.destination)
            }
            DocumentRouteType.DOCUMENT -> {
                // Kafelek dokumentu służy do podglądu; edycja pozostaje dostępna
                // wyłącznie z ustawień dokumentu.
                showDocumentEditorCatalog = false
                showSettingsMenu = false
                showDemoDocuments = false
                showDocumentsEditor = false
                showRegistryEditor = false
                showDisabilitySettings = false
                showBankAccounts = false
                settingsDocumentEditor = null
                activeService = null
                selectedDocumentId = route.documentId
            }
            DocumentRouteType.SPECIALIZED -> when (route.destination) {
                "registry_extract" -> openSettingsDocumentEditor("registry")
                "children" -> openSettingsDocumentEditor("children")
                "bank_accounts" -> showBankAccounts = true
                "vaccines" -> openSettingsDocumentEditor("vaccines")
                "foreign_prescription" -> selectedDocumentId = route.documentId
                "signed_pdf" -> openSettingsDocumentEditor("signed_pdf")
                "receipts" -> showReceipts = true
                "imported" -> openSettingsDocumentEditor("imported")
                "branding" -> activeService = "Zasoby identyfikacji dokumentów"
                else -> openSettingsDocumentEditor(route.documentId)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    showDefaultDocumentAppearanceSettings -> DefaultDocumentAppearanceSettingsScreen(
                        onBack = { showDefaultDocumentAppearanceSettings = false; showSettingsMenu = true },
                        onOpenLegitimacy = { showDefaultDocumentAppearanceSettings = false; showCredentialAppearanceEditor = true },
                        onOpenCertificate = { showDefaultDocumentAppearanceSettings = false; showCertificateAppearanceSettings = true }
                    )
                    showCertificateAppearanceSettings -> CertificateAppearanceSettingsScreen(
                        onBack = { showCertificateAppearanceSettings = false; showDefaultDocumentAppearanceSettings = true }
                    )
                    credentialAppearanceTarget != null -> CredentialAppearanceSettingsScreen(
                        documentKey = credentialAppearanceTarget!!,
                        onBack = { credentialAppearanceTarget = null },
                        onOpenSharedAppearance = {
                            credentialAppearanceTarget = null
                            showCredentialAppearanceEditor = true
                        }
                    )
                    showCredentialAppearanceEditor -> CredentialAppearanceDefaultsScreen(onBack = {
                        showCredentialAppearanceEditor = false
                        showDefaultDocumentAppearanceSettings = true
                    })
                    showTranslationLanguageSettings -> MedicalTranslationLanguagesSettingsScreen(onBack = { showTranslationLanguageSettings = false; showSettingsMenu = true })
                    showTorClientSettings -> TorClientSettingsScreen(onBack = { showTorClientSettings = false; showSettingsMenu = true })
                    showStudiaOnlineBulkDownload -> StudiaOnlineBulkDownloadScreen(
                        onBack = { showStudiaOnlineBulkDownload = false; selectedTab = 4 }
                    )
                    showApplicationAppearanceSettings -> ApplicationAppearanceSettingsScreen(
                        appearance = LocalApplicationAppearance.current,
                        onChange = onAppearanceChange,
                        documentCardStyle = documentCardStyle,
                        onDocumentCardStyleChange = { style ->
                            saveDocumentCardStyle(context, style)
                            documentCardStyle = style
                        },
                        onBack = { showApplicationAppearanceSettings = false; showSettingsMenu = true }
                    )
                    showIdentityDesigner -> IdentityDocumentsScreen(onBack = { showIdentityDesigner = false })
                    showPublicServicesBrowser -> PublicServicesScreen(onBack = { showPublicServicesBrowser = false })
                    showImportedDocuments -> DocumentsScreen(onBack = { showImportedDocuments = false }, readOnly = true)
                    showReceipts -> ReceiptDocumentsScreen(onBack = { showReceipts = false })
                    activeDonationLedgerId != null -> DonationLedgerScreen(
                        documentId = activeDonationLedgerId!!,
                        readOnly = false,
                        onBack = { activeDonationLedgerId = null; showPaymentLedger = true }
                    )
                    showPaymentLedger -> PaymentLedgerScreen(
                        onBack = { showPaymentLedger = false },
                        onOpenDonationLedger = { id ->
                            activeDonationLedgerId = id
                            showPaymentLedger = false
                        }
                    )
                    settingsDocumentEditor == "receipts" -> ReceiptTemplateSettingsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor?.startsWith("donation_ledger:") == true -> DonationLedgerScreen(
                        documentId = settingsDocumentEditor!!.substringAfter("donation_ledger:"), readOnly = false,
                        onBack = { settingsDocumentEditor = null; showSettingsMenu = true }
                    )
                    showBankAccounts -> FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, readOnly = true, onBack = { showBankAccounts = false })
                    showDocumentsEditor -> DemoDocumentsScreen(
                        categoryId = selectedEditorCategory,
                        onBack = { showDocumentsEditor = false; showSettingsMenu = true },
                        onOpenDocumentAppearance = { key -> credentialAppearanceTarget = key }
                    )
                    showRegistryEditor -> CredentialAppearanceEditorEntry("entity", onOpenAppearance = { credentialAppearanceTarget = it }, showOriginalAttachment = true) {
                        EntityRegistryExtractScreen(onBack = { showRegistryEditor = false; showSettingsMenu = true }, editable = true)
                    }
                    settingsDocumentEditor == "imported" -> CredentialAppearanceEditorEntry("imported", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        DocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    settingsDocumentEditor == "signed_pdf" -> SignedPdfDocumentsScreen(onBack = { settingsDocumentEditor = null; showCertificates = true }, allowImport = true, readOnly = false)
                    settingsDocumentEditor == "professional" -> CredentialAppearanceEditorEntry("professional", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        ProfessionalQualificationsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    (settingsDocumentEditor == "family" || settingsDocumentEditor?.startsWith("family:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("family:", "")?.ifBlank { "family" } ?: "family", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = false, initialDocumentId = settingsDocumentEditor?.substringAfter("family:", "")?.ifBlank { null },
                            onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    (settingsDocumentEditor == "business" || settingsDocumentEditor?.startsWith("business:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("business:", "")?.ifBlank { "business" } ?: "business", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = true, initialDocumentId = settingsDocumentEditor?.substringAfter("business:", "")?.ifBlank { null },
                            onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor?.startsWith("service:") == true -> {
                        val serviceDocumentId = settingsDocumentEditor!!.substringAfter("service:")
                        val serviceKey = serviceDocumentId.removePrefix("service_").substringBefore("_")
                        CredentialAppearanceEditorEntry(serviceDocumentId, onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                            FamilyEntityDocumentsScreen(entityMode = true, serviceKey = serviceKey, initialDocumentId = serviceDocumentId,
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    }
                    (settingsDocumentEditor == "bank_accounts" || settingsDocumentEditor?.startsWith("bank_accounts:") == true) ->
                        CredentialAppearanceEditorEntry(
                            settingsDocumentEditor?.substringAfter("bank_accounts:", "")?.ifBlank { "rod_bank_accounts" } ?: "rod_bank_accounts",
                            onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true
                        ) {
                            FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, initialDocumentId = settingsDocumentEditor?.substringAfter("bank_accounts:", "")?.ifBlank { null },
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    settingsDocumentEditor?.startsWith("entity_bank_accounts:") == true ->
                        CredentialAppearanceEditorEntry(
                            settingsDocumentEditor?.substringAfter("entity_bank_accounts:", "")?.ifBlank { "entity_bank_accounts" } ?: "entity_bank_accounts",
                            onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true
                        ) {
                            FamilyEntityDocumentsScreen(entityMode = true, bankAccountsOnly = true, initialDocumentId = settingsDocumentEditor?.substringAfter("entity_bank_accounts:", "")?.ifBlank { null },
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    (settingsDocumentEditor == "church" || settingsDocumentEditor?.startsWith("church:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("church:", "")?.ifBlank { "church" } ?: "church", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        ChurchDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            initialDocumentId = settingsDocumentEditor?.substringAfter("church:", "")?.ifBlank { null },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    (settingsDocumentEditor == "civil" || settingsDocumentEditor?.startsWith("civil:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("civil:", "")?.ifBlank { "civil" } ?: "civil", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        OfficialDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            initialDocumentId = settingsDocumentEditor?.substringAfter("civil:", "")?.ifBlank { null },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "children_health" -> CredentialAppearanceEditorEntry("children", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        FamilyHealthRecordsScreen(initialSection = "children", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    }
                    settingsDocumentEditor == "vaccines" -> CredentialAppearanceEditorEntry("vaccines", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    }
                    settingsDocumentEditor == "graphic_catalog" -> DocumentGraphicCatalogScreen(
                        onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                        onSelect = { key -> graphicEditorReturnTarget = "graphic_catalog"; settingsDocumentEditor = "graphic:$key" }
                    )
                    settingsDocumentEditor?.startsWith("graphic:") == true -> DocumentGraphicEditorScreen(
                        documentKey = settingsDocumentEditor!!.substringAfter("graphic:"),
                        onBack = {
                            if (graphicEditorReturnTarget == "disability") {
                                settingsDocumentEditor = null
                                showDisabilitySettings = true
                            } else settingsDocumentEditor = graphicEditorReturnTarget
                        }
                    )
                    documentBrandTarget != null -> DocumentBrandAssetsScreen(onBack = { documentBrandTarget = null; showSettingsMenu = true }, fixedTarget = documentBrandTarget!!)
                    showDocumentBrandAssets -> DocumentBrandAssetsScreen(onBack = { showDocumentBrandAssets = false; showSettingsMenu = true })
                    showDocumentVisibilitySettings -> DocumentVisibilitySettingsScreen(
                        documentPlacements = documentPlacements,
                        onDocumentPlacementsChange = { updated ->
                            saveDocumentPlacements(context, updated)
                            documentPlacements = updated
                        },
                        documentCardStyle = documentCardStyle,
                        onDocumentCardStyleChange = { style ->
                            saveDocumentCardStyle(context, style)
                            documentCardStyle = style
                        },
                        defaultDocumentIcon = defaultDocumentIcon,
                        onDefaultDocumentIconChange = { selected ->
                            saveDefaultDocumentIcon(context, selected)
                            defaultDocumentIcon = selected
                        },
                        documentIconOverrides = documentIconOverrides,
                        documentGroups = documentGroupConfiguration.groups,
                        documentGroupAssignments = documentGroupConfiguration.assignments,
                        onDocumentGroupAssignmentsChange = { updated ->
                            val next = documentGroupConfiguration.copy(assignments = updated)
                            saveDocumentGroupConfiguration(context, next)
                            documentGroupConfiguration = loadDocumentGroupConfiguration(context)
                        },
                        onDocumentIconOverridesChange = { updated ->
                            saveDocumentIconOverrides(context, updated)
                            documentIconOverrides = updated
                        },
                        onBack = { showDocumentVisibilitySettings = false; showSettingsMenu = true }
                    )
                    showDocumentEditorCatalog -> DocumentEditorCatalogScreen(
                        documentGroups = documentGroupConfiguration.groups,
                        documentGroupAssignments = documentGroupConfiguration.assignments,
                        onOpenDocument = ::openSettingsDocumentEditor,
                        onBack = { showDocumentEditorCatalog = false; showSettingsMenu = true }
                    )
                    showSettingsMenu -> SettingsMenuScreen(
                        documentPlacements = documentPlacements,
                        onDocumentPlacementsChange = { updated ->
                            saveDocumentPlacements(context, updated)
                            documentPlacements = updated
                        },
                        onBack = { showSettingsMenu = false },
                        onManageApplicationAppearance = { showSettingsMenu = false; showApplicationAppearanceSettings = true },
                        onManageTranslationLanguages = { showSettingsMenu = false; showTranslationLanguageSettings = true },
                        onManageTorClients = { showSettingsMenu = false; showTorClientSettings = true },
                        onManageCredentialAppearance = { showSettingsMenu = false; showDefaultDocumentAppearanceSettings = true },
                        onManageDocumentEditing = { showSettingsMenu = false; showDocumentEditorCatalog = true },
                        onManageDocumentVisibility = { openDocumentVisibilityCatalog() },
                        onManageDocumentBrandAssets = { showSettingsMenu = false; showDocumentBrandAssets = true },
                        onEditDocumentBranding = { target -> showSettingsMenu = false; documentBrandTarget = target },
                        onEditDocuments = { category -> selectedEditorCategory = category; showSettingsMenu = false; showDocumentsEditor = true },
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true },
                        onEditRegistry = { showSettingsMenu = false; showRegistryEditor = true },
                        onEditReceipts = { showSettingsMenu = false; settingsDocumentEditor = "receipts" },
                        onEditOtherDocuments = { key -> settingsDocumentEditor = key; showSettingsMenu = false },
                        onExportAll = {
                            scope.launch {
                                exportBusy = true
                                importStatus = "Przygotowywanie archiwum vCard…"
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val groupConfiguration = loadDocumentGroupConfiguration(context)
                                        val catalogManifest = org.json.JSONArray().apply {
                                            documentDisplayOptions.distinctBy { it.first }.forEach { (id, title, _) ->
                                                put(org.json.JSONObject().apply {
                                                    put("id", id)
                                                    put("title", title)
                                                    put("group", groupConfiguration.assignments[id] ?: documentVisibilityGroup(id))
                                                })
                                            }
                                        }.toString()
                                        val placements = loadDocumentPlacements(context)
                                        val allCatalogPlacements = documentDisplayOptions.distinctBy { it.first }.associate { (id, _, _) ->
                                            id to (placements[id] ?: if (id == "identity") "dashboard" else "hidden")
                                        }
                                        val snapshot = DemoIdentityStore.load(context) +
                                            LegitimacyPreviewSettingsStore.exportAll(context) +
                                            allCatalogPlacements.mapKeys { "dp_${it.key}" } +
                                            mapOf(
                                                "documentCatalogManifest" to catalogManifest,
                                                "documentCatalogGroupOrder" to org.json.JSONArray(groupConfiguration.groups).toString(),
                                                "documentGroupConfiguration" to exportDocumentGroupConfiguration(context),
                                                "documentEditorSchemaVersion" to DOCUMENT_EDITOR_SCHEMA_VERSION,
                                                "churchDocumentArchive" to ChurchDocumentStore.exportArchive(context),
                                                "familyEntityDocumentArchive" to FamilyEntityDocumentStore.exportArchive(context),
                                                "officialDocumentArchive" to OfficialDocumentStore.exportArchive(context),
                                                "signedPdfArchive" to exportSignedPdfArchive(context),
                                                "brandAssetArchive" to DocumentBrandStore.exportArchive(context),
                                                "professionalQualificationArchive" to exportProfessionalQualificationArchive(context),
                                                "familyHealthArchive" to exportFamilyHealthArchive(context),
                                                "medicalHistoryArchive" to exportMedicalHistoryArchive(context),
                                                "importedAttachmentArchive" to pl.gov.obywatel.android.data.ImportedDocumentStore.exportArchive(context),
                                                "originalAttachmentArchive" to exportOriginalAttachmentArchive(context),
                                                "identityDocumentsArchive" to pl.gov.obywatel.android.ui.identity.exportIdentityDocumentsArchive(context),
                                                "localRegistryArchive" to pl.gov.obywatel.android.ui.registry.exportLocalRegistryArchive(context),
                                                "activityHistoryArchive" to exportActivityHistoryArchive(context)
                                            )
                                        require(snapshot.isNotEmpty()) {
                                            "Brak danych do eksportu. Najpierw uzupełnij i zapisz dane w ustawieniach."
                                        }
                                        VCardCodec.encodeArchive(snapshot) { percent ->
                                            scope.launch(Dispatchers.Main) { importStatus = "Przygotowywanie archiwum vCard: $percent%" }
                                        }
                                        snapshot
                                    }
                                }
                                result.onSuccess {
                                    exportSnapshot = it
                                    importStatus = "Archiwum przygotowane. Wybierz miejsce zapisu."
                                    exportVCardLauncher.launch("obywatel-archiwum.vcf")
                                }.onFailure {
                                    importStatus = "Nie można przygotować eksportu: ${it.message ?: "nieznany błąd"}"
                                }
                                exportBusy = false
                            }
                        },
                        onImportAll = { importVCardLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) },
                        importStatus = importStatus
                    )
                    selectedDocumentId == FOREIGN_PRESCRIPTION_ID -> ForeignPrescriptionEditorScreen(
                        onBack = { selectedDocumentId = null }
                    )
                    selectedDocumentId?.let { DonationLedgerCatalog.find(it) != null } == true -> DonationLedgerScreen(
                        documentId = selectedDocumentId!!,
                        readOnly = true,
                        onBack = { selectedDocumentId = null },
                        onEdit = {
                            activeDonationLedgerId = selectedDocumentId
                            selectedDocumentId = null
                        }
                    )
                    selectedDocumentId?.let { it in InternationalDonationDocumentCatalog.all.keys } == true ->
                        InternationalDonationDocumentEditorScreen(
                            documentId = selectedDocumentId!!,
                            onBack = { selectedDocumentId = null }
                        )
                    selectedDocumentId == INTERNATIONAL_MEDICAL_CERTIFICATE_ID && selectedMedicalCertificateRecordId != null ->
                        InternationalMedicalCertificateEditorScreen(
                            recordId = selectedMedicalCertificateRecordId!!,
                            onBack = { selectedMedicalCertificateRecordId = null }
                        )
                    selectedDocumentId == INTERNATIONAL_MEDICAL_CERTIFICATE_ID ->
                        InternationalMedicalCertificateManagerScreen(
                            onBack = { selectedDocumentId = null },
                            onEdit = { selectedMedicalCertificateRecordId = it }
                        )
                    selectedDocumentId != null -> DocumentDetailsScreen(
                        documentId = selectedDocumentId!!,
                        onBack = { selectedDocumentId = null }
                    )
                    showDemoDocuments -> DocumentsWalletScreen(
                        onBack = { showDemoDocuments = false },
                        onAddDocuments = { openDocumentVisibilityCatalog() },
                        documentPlacements = documentPlacements,
                        documentCardStyle = documentCardStyle,
                        defaultDocumentIcon = defaultDocumentIcon,
                        documentIconOverrides = documentIconOverrides,
                        documentGroups = documentGroupConfiguration.groups,
                        documentGroupAssignments = documentGroupConfiguration.assignments,
                        onOpenDetails = { openDocumentCard(it) },
                        onEditDocument = { openSettingsDocumentEditor(it) },
                        onOpenService = { title ->
                            when (title) { "Konta bankowe" -> showBankAccounts = true; "Dokumenty własne — pliki" -> showImportedDocuments = true; else -> activeService = title }
                        }
                    )
                    showDisabilitySettings -> CredentialAppearanceEditorEntry("disability", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        CompositionLocalProvider(LocalDocumentScreenMode provides DocumentScreenMode.EDITOR) {
                            DisabilitySettingsScreen(
                                onBack = { showDisabilitySettings = false; showSettingsMenu = true },
                                onEditGraphic = {
                                    graphicEditorReturnTarget = "disability"
                                    showDisabilitySettings = false
                                    settingsDocumentEditor = "graphic:disability"
                                }
                            )
                        }
                    }
                    showCertificates -> CertificateStudioScreen(onBack = { showCertificates = false }, onOpenDigitalCertificates = { settingsDocumentEditor = "signed_pdf" })
                    activeService == "Informacje prawne" -> LegalInformationScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Odpis z rejestru podmiotów" -> EntityRegistryExtractScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Dokumenty Kościelne" -> ChurchDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Dokumenty kwalifikacji zawodowych" -> ProfessionalQualificationsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Pozostałe dokumenty" -> ServiceDetailScreen(title = activeService!!, onBack = { activeService = null }, onOpenDocuments = { activeService = null; showDemoDocuments = true })
                    activeService == "Służba fundacyjna" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "foundation", readOnly = true, onBack = { activeService = null })
                    activeService == "Służba gospodarcza" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "business", readOnly = true, onBack = { activeService = null })
                    activeService == "Służba stowarzyszeniowa" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "association", readOnly = true, onBack = { activeService = null })
                    activeService == "Dokumenty rejestru podmiotów" -> FamilyEntityDocumentsScreen(entityMode = true, readOnly = true, onBack = { activeService = null })
                    activeService == "Formularze dokumentów" -> OfficialDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Zasoby identyfikacji dokumentów" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { InstitutionalSymbolCatalogPanel(context = LocalContext.current, onSaved = { }); DocumentBrandAssetsScreen(onBack = { activeService = null }, readOnly = true) }
                    activeService == "Dokumenty PDF i podpisy elektroniczne" -> SignedPdfDocumentsScreen(onBack = { activeService = null }, allowImport = false, readOnly = true)
                    activeService == "Dane dzieci" -> FamilyHealthRecordsScreen(initialSection = "children", onBack = { activeService = null }, readOnly = true)
                    activeService == "eKarta Szczepień" -> FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { activeService = null }, readOnly = true)
                    activeService == "Dokumenty Rodowe" -> FamilyEntityDocumentsScreen(entityMode = false, readOnly = true, onBack = { activeService = null })
                    activeService != null -> ServiceDetailScreen(
                        title = activeService!!,
                        onBack = { activeService = null },
                        onOpenDocuments = { activeService = null; showDemoDocuments = true }
                    )
                    selectedTab == 1 -> {
                        val query = serviceSearch.trim()
                        val matchingDocuments = if (query.isBlank()) emptyList() else
                            documentDisplayOptions.filter { (id, title, _) ->
                                title.contains(query, ignoreCase = true) ||
                                    id.contains(query, ignoreCase = true)
                            }
                        val matchingServices = if (query.isBlank()) emptyList() else
                            services.filter { item ->
                                item.title.contains(query, ignoreCase = true) ||
                                    item.subtitle.contains(query, ignoreCase = true) ||
                                    item.category.contains(query, ignoreCase = true)
                            }
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Szukaj", color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = serviceSearch,
                                onValueChange = { serviceSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Szukaj dokumentu, usługi lub funkcji") },
                                leadingIcon = { Icon(painterResource(R.drawable.nav_search), contentDescription = null) },
                                trailingIcon = {
                                    if (serviceSearch.isNotEmpty()) {
                                        IconButton(onClick = { serviceSearch = "" }) {
                                            Icon(painterResource(R.drawable.lib_ic_close), contentDescription = "Wyczyść wyszukiwanie")
                                        }
                                    }
                                }
                            )
                            if (query.isBlank()) {
                                Text(
                                    "Wpisz nazwę dokumentu, usługi, modułu lub kategorii. Wyniki obejmują dokumenty i funkcje dostępne w aplikacji.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text("Dokumenty (" + matchingDocuments.size + ")",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (matchingDocuments.isEmpty()) {
                                    Text("Brak pasujących dokumentów.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall)
                                } else matchingDocuments.forEach { (id, title, iconRes) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { openDocumentCard(id) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Image(painter = painterResource(documentTileIconResource(context, id, iconRes)), contentDescription = null,
                                                modifier = Modifier.size(38.dp), contentScale = ContentScale.Fit)
                                            Text(title, modifier = Modifier.weight(1f).padding(start = 12.dp),
                                                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Text("Usługi i funkcje (" + matchingServices.size + ")",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (matchingServices.isEmpty()) {
                                    Text("Brak pasujących usług i funkcji.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall)
                                } else matchingServices.forEach { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { openService(item.title) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            if (item.icon != null) {
                                                Icon(painterResource(item.icon), contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            } else {
                                                Icon(item.fallbackIcon ?: Icons.Filled.Description, contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            }
                                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                                Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                                Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall, maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis)
                                            }
                                            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    selectedTab == 3 -> QrToolsScreen()
                    selectedTab == 4 -> MoreMenuScreen(
                        onOpenSettings = { showSettingsMenu = true },
                        onOpenStudiaOnlineArchive = { showStudiaOnlineBulkDownload = true },
                        onOpenCertificates = { showCertificates = true },
                        onOpenHelp = { activeService = "Pomoc i informacje" },
                        onOpenHistory = { activeService = "Historia działań" },
                        onOpenIdentityDesigner = { showIdentityDesigner = true },
                        onOpenImportedDocuments = { showImportedDocuments = true },
                        onOpenPaymentLedger = { showPaymentLedger = true },
                        onOpenLegalInformation = { activeService = "Informacje prawne" }
                    )
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (selectedTab == 0) {
                                Header(
                                    onOpenAssistant = { activeService = "Asystent" },
                                    onOpenMessages = { activeService = "Wiadomości" },
                                    onOpenNotifications = { activeService = "Powiadomienia" }
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                                    .padding(horizontal = 18.dp)
                            ) {
                                Spacer(Modifier.height(18.dp))
                                if (selectedTab == 0) {
                                    if (documentCardStyle == "small") {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IdentityDashboardCard(
                                                onOpen = { openDocumentCard("identity") },
                                                tileStyle = documentCardStyle,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Spacer(Modifier.weight(1f))
                                        }
                                    } else {
                                        IdentityDashboardCard(
                                            onOpen = { openDocumentCard("identity") },
                                            tileStyle = documentCardStyle
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    val dashboardDocuments = documentDisplayOptions.filter {
                                        it.first != "identity" && documentPlacements[it.first] == "dashboard"
                                    }
                                    if (dashboardDocuments.isNotEmpty()) {
                                        Text("Dokumenty", color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        DashboardDocumentGrid(dashboardDocuments, documentCardStyle, ::openDocumentCard)
                                    }
                                    val otherDashboardDocuments = documentDisplayOptions.filter {
                                        documentPlacements[it.first] == "other"
                                    }
                                    if (otherDashboardDocuments.isNotEmpty()) {
                                        Text("Inne dokumenty", color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        DashboardDocumentGrid(otherDashboardDocuments, documentCardStyle, ::openDocumentCard)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Zarządzaj",
                                        color = Color(0xFF83DFFF),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { showDemoDocuments = true }
                                            .padding(vertical = 4.dp)
                                    )
                                    Button(
                                        onClick = { openDocumentVisibilityCatalog() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF91D9F8),
                                            contentColor = Color(0xFF102D5D)
                                        )
                                    ) {
                                        Icon(painter = painterResource(R.drawable.lib_ic_plus),
                                            contentDescription = null, tint = Color(0xFF102D5D),
                                            modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Dodaj dokument", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    CivicIllustrationBanner(
                                        imageRes = R.drawable.header_poland_banner,
                                        description = "Baner informacyjny Polski"
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    SectionHeading("Usługi", "Katalog usług według kategorii")
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { showPublicServicesBrowser = true },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Public, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                                Text("Przeglądarka usług publicznych",
                                                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                Text("Otwieraj portale gov.pl i inne serwisy wewnątrz aplikacji",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text("Otwórz", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = serviceSearch,
                                        onValueChange = { serviceSearch = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Szukaj usługi") },
                                        leadingIcon = { Icon(painterResource(R.drawable.nav_search), contentDescription = null) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "Wszystkie", "Zdrowie", "Kierowca i pojazdy", "Sprawy urzędowe",
                                            "Rodzina", "Edukacja", "Praca", "Finanse i podatki", "Transport",
                                            "Nieruchomości", "Środowisko", "Bezpieczeństwo", "Prawo i obywatel",
                                            "Kultura i dziedzictwo", "Sport", "Organizacje społeczne",
                                            "Kościół i wspólnoty", "Rejestry", "Dokumenty", "Podróże", "Pozostałe"
                                        ).forEach { category ->
                                            val active = serviceCategory == category
                                            Box(
                                                modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(18.dp))
                                                    .clickable { serviceCategory = category }
                                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                                            ) {
                                                Text(category,
                                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    val visibleServices = services.filter { service ->
                                        (serviceCategory == "Wszystkie" || service.category == serviceCategory) &&
                                            (serviceSearch.isBlank() ||
                                                service.title.contains(serviceSearch, ignoreCase = true) ||
                                                service.subtitle.contains(serviceSearch, ignoreCase = true))
                                    }
                                    if (visibleServices.isEmpty()) {
                                        Text("Nie znaleziono usług. Zmień wyszukiwaną frazę lub kategorię.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
                                    } else {
                                        visibleServices.forEach { service ->
                                            ServiceCard(
                                                item = service,
                                                tileStyle = documentCardStyle,
                                                isFavorite = favoriteServices[service.title] == true,
                                                onFavorite = {
                                                    favoriteServices[service.title] = favoriteServices[service.title] != true
                                                },
                                                onClick = { openService(service.title) }
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BottomNavigation(
                selectedTab = selectedTab,
                onSelect = {
                    // Nawigacja dolna zawsze wraca do wybranego ekranu głównego.
                    selectedTab = it
                    activeService = null
                    showDemoDocuments = false
                    showDocumentsEditor = false
                    showSettingsMenu = false
                    showDocumentEditorCatalog = false
                    showApplicationAppearanceSettings = false
                    showDocumentVisibilitySettings = false
                    showDocumentBrandAssets = false
                    documentBrandTarget = null
                    showRegistryEditor = false
                    settingsDocumentEditor = null
                    credentialAppearanceTarget = null
                    selectedDocumentId = null
                    showDisabilitySettings = false
                    showCertificates = false
                    showIdentityDesigner = false
                    showCredentialAppearanceEditor = false
                    showImportedDocuments = false
                    showBankAccounts = false
                    showPublicServicesBrowser = false
                }
            )
        }
        pendingExportUri?.let { destinationUri ->
            AlertDialog(
                onDismissRequest = { pendingExportUri = null },
                title = { Text("Potwierdzenie eksportu vCard") },
                text = {
                    Text("Zapiszesz ${exportSnapshot.size} pól oraz katalog dokumentów z ich grupami i ustawieniami widoczności. Jeżeli w wybranym miejscu istnieje plik o tej samej nazwie, jego zawartość zostanie zastąpiona. Czy kontynuować?")
                },
                confirmButton = {
                    TextButton(enabled = !exportBusy, onClick = {
                        scope.launch {
                            exportBusy = true
                            importStatus = "Zapisywanie i weryfikowanie archiwum vCard…"
                            val snapshot = exportSnapshot.toMap()
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    require(snapshot.isNotEmpty()) { "Brak danych do eksportu." }
                                    val bytes = VCardCodec.encodeArchive(snapshot).toByteArray(Charsets.UTF_8)
                                    val output = context.contentResolver.openOutputStream(destinationUri, "wt")
                                        ?: error("Android nie udostępnił strumienia zapisu dla wybranego pliku.")
                                    output.use { it.write(bytes); it.flush() }
                                    val savedBytes = context.contentResolver.openInputStream(destinationUri)?.use { it.readBytes() }
                                        ?: error("Nie można ponownie odczytać zapisanego pliku vCard.")
                                    require(savedBytes.isNotEmpty()) { "Zapisany plik vCard jest pusty." }
                                    val verified = VCardCodec.decodeArchive(String(savedBytes, Charsets.UTF_8))
                                    require(verified == snapshot) {
                                        "Weryfikacja zapisu nie powiodła się: odczytane dane różnią się od eksportowanych."
                                    }
                                    "Zapisano i zweryfikowano ${snapshot.size} pól oraz katalog dokumentów (${bytes.size} bajtów) w pliku vCard."
                                }
                            }
                            result.onSuccess { importStatus = it }
                                .onFailure { importStatus = "Błąd eksportu: ${it.message ?: "nieznany błąd"}" }
                            exportBusy = false
                            pendingExportUri = null
                        }
                    }) { Text(if (exportBusy) "Zapisywanie…" else "Potwierdź zapis") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingExportUri = null
                        importStatus = "Eksport anulowany — plik docelowy nie został zmieniony."
                    }) { Text("Anuluj") }
                }
            )
        }
        pendingImport?.let { incoming ->
            AlertDialog(
                onDismissRequest = { pendingImport = null },
                title = { Text("Import danych vCard") },
                text = {
                    val current = remember { DemoIdentityStore.load(context) }
                    val supportedProfileKeys = current.keys + documentEditorImportableFieldKeys()
                    val supportedArchiveKeys = documentArchiveImportKeys()
                    val recognizedIncoming = incoming.keys.count { it in supportedProfileKeys || it in supportedArchiveKeys || it.startsWith("cs_") || it.startsWith("dp_") || isSupportedDynamicDocumentField(it) }
                    val overwritten = incoming.keys.count { it in supportedProfileKeys && current.containsKey(it) }
                    val added = recognizedIncoming - overwritten
                    Text(
                        "Plik zawiera ${incoming.size} pól. Nowe: ${added}; zastąpią istniejące: ${overwritten}. " +
                            "Dane zostaną połączone z profilem. Po zapisie aplikacja sprawdzi zgodność zapisanych wartości."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            importBusy = true
                            importStatus = "Zapisywanie i weryfikowanie zaimportowanych danych…"
                            val result = withContext(Dispatchers.IO) {
                                var placementUpdate: Map<String, String>? = null
                                val outcome = runCatching {
                            val current = DemoIdentityStore.loadStrict(context)
                            val importedSchemaVersion = incoming["documentEditorSchemaVersion"]?.toIntOrNull() ?: 1
                            require(importedSchemaVersion in 1..DOCUMENT_EDITOR_SCHEMA_VERSION.toInt()) {
                                "Archiwum używa nieobsługiwanej wersji schematu dokumentów: $importedSchemaVersion."
                            }
                            val profileValues = incoming.filterKeys {
                                // Importuj wyłącznie pola należące do aktualnego modelu profilu.
                                // Nieznane lub wycofane nazwy pól ze starszych archiwów vCard
                                // są pomijane — nie tworzymy przez nie osieroconych wpisów.
                                (it in current.keys || it in documentEditorImportableFieldKeys() || isSupportedDynamicDocumentField(it)) &&
                                    !it.startsWith("cs_") && !it.startsWith("dp_")
                            }
                            val mergedProfile = current.toMutableMap().apply { putAll(profileValues) }
                            PersonalDataPropagation.apply(mergedProfile)
                            DemoIdentityStore.save(context, mergedProfile)
                            incoming["documentGroupConfiguration"]?.let { importDocumentGroupConfiguration(context, it) }
                            incoming["churchDocumentArchive"]?.let { ChurchDocumentStore.importArchive(context, it) }
                            incoming["familyEntityDocumentArchive"]?.let { FamilyEntityDocumentStore.importArchive(context, it) }
                            incoming["officialDocumentArchive"]?.let { OfficialDocumentStore.importArchive(context, it) }
                            incoming["signedPdfArchive"]?.let { importSignedPdfArchive(context, it) }
                            incoming["brandAssetArchive"]?.let { DocumentBrandStore.importArchive(context, it) }
                            incoming["professionalQualificationArchive"]?.let { importProfessionalQualificationArchive(context, it) }
                            incoming["familyHealthArchive"]?.let { importFamilyHealthArchive(context, it) }
                            incoming["medicalHistoryArchive"]?.let { importMedicalHistoryArchive(context, it) }
                            incoming["importedAttachmentArchive"]?.let { pl.gov.obywatel.android.data.ImportedDocumentStore.importArchive(context, it) }
                            incoming["originalAttachmentArchive"]?.let { importOriginalAttachmentArchive(context, it) }
                            incoming["identityDocumentsArchive"]?.let { pl.gov.obywatel.android.ui.identity.importIdentityDocumentsArchive(context, it) }
                            incoming["localRegistryArchive"]?.let { pl.gov.obywatel.android.ui.registry.importLocalRegistryArchive(context, it) }
                            incoming["activityHistoryArchive"]?.let { importActivityHistoryArchive(context, it) }
                            LegitimacyPreviewSettingsStore.importAll(context, incoming)
                            val knownDocumentIds = documentDisplayOptions.map { it.first }.toSet()
                            val importedCatalogIds = incoming["documentCatalogManifest"]?.let { rawManifest ->
                                val manifest = org.json.JSONArray(rawManifest)
                                buildSet {
                                    for (index in 0 until manifest.length()) {
                                        val item = manifest.optJSONObject(index)
                                            ?: error("Nieprawidłowy wpis katalogu dokumentów w archiwum.")
                                        val id = item.optString("id").trim()
                                        val title = item.optString("title").trim()
                                        val group = item.optString("group").trim()
                                        require(
                                            id.isNotBlank() && title.isNotBlank() &&
                                                migrateDocumentGroupName(group) in loadDocumentGroupConfiguration(context).groups
                                        ) {
                                            "Archiwum zawiera nieprawidłowy opis dokumentu: $id."
                                        }
                                        require(add(id)) { "Archiwum zawiera powtórzony identyfikator dokumentu: $id." }
                                    }
                                }
                            } ?: knownDocumentIds
                            val importedPlacements = incoming.filterKeys {
                                it.startsWith("dp_") &&
                                    it.removePrefix("dp_") in knownDocumentIds &&
                                    it.removePrefix("dp_") in importedCatalogIds
                            }.mapKeys { it.key.removePrefix("dp_") }
                            require(importedPlacements.values.all { it in setOf("dashboard", "other", "hidden") }) {
                                "Archiwum zawiera nieprawidłowe ustawienie widoczności dokumentu."
                            }
                            if (importedPlacements.isNotEmpty()) {
                                // Ustawienia z jawnie wskazanego archiwum mają pierwszeństwo
                                // przed wartościami lokalnymi, również dla służby zbrojnej.
                                val mergedPlacements = loadDocumentPlacements(context).toMutableMap().apply {
                                    putAll(importedPlacements)
                                }
                                saveDocumentPlacements(context, mergedPlacements)
                                val verifiedPlacements = loadDocumentPlacements(context)
                                require(importedPlacements.all { (id, placement) -> verifiedPlacements[id] == placement }) {
                                    "Weryfikacja importu nie potwierdziła wszystkich ustawień widoczności dokumentów."
                                }
                                placementUpdate = verifiedPlacements
                            }
                            val persisted = DemoIdentityStore.loadStrict(context)
                            require(profileValues.all { (key, value) -> persisted[key] == value }) {
                                "Weryfikacja zapisu nie powiodła się. Dane profilu nie odpowiadają importowi."
                            }
                            val auxiliaryCount = incoming.keys.count {
                                it.startsWith("cs_") || it.startsWith("dp_") ||
                                    it in setOf(
                                        "documentCatalogManifest", "documentCatalogGroupOrder", "documentGroupConfiguration", "documentEditorSchemaVersion", "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive", "originalAttachmentArchive",
                                        "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
                                    )
                            }
                            val recognizedCount = profileValues.size + auxiliaryCount
                            val changed = profileValues.keys.count { persisted[it] == profileValues[it] } + auxiliaryCount
                            require(changed == recognizedCount) {
                                "Weryfikacja importu wykazała niezgodność części rozpoznanych pól lub ustawień."
                            }
                            val ignoredCount = incoming.size - recognizedCount
                            "Zaimportowano i zweryfikowano ${changed} rozpoznanych pól oraz ustawień. Pominięto ${ignoredCount.coerceAtLeast(0)} pól o nieaktualnych lub nieobsługiwanych nazwach. Łącznie zapisano ${persisted.size} pól profilu."
                                }
                                outcome to placementUpdate
                            }
                            result.first.onSuccess { message ->
                                result.second?.let { documentPlacements = it }
                                importStatus = message
                            }.onFailure { importStatus = "Błąd importu lub weryfikacji: ${it.message ?: "nieznany błąd"}" }
                            pendingImport = null
                            importBusy = false
                        }
                    }) { Text(if (importBusy) "Importowanie…" else "Importuj i zweryfikuj") }
                },
                dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Anuluj") } }
            )
        }
    }
}
