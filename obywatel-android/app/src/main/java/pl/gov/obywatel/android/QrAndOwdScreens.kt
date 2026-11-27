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

// Ekrany i narzędzia kodów QR oraz dokumentów OWD.
// Wydzielone z MainActivity.kt; elementy pomocnicze pozostają prywatne dla tego pliku.

private fun documentIconFor(documentId: String): ImageVector = when (documentId) {
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

private fun categoryIconFor(category: String): Int? = when (category) {
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
internal fun QrToolsScreen() {
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
    Text("‹  $label", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
private fun QrBrandingControls(branding: String, onBrandingChange: (String) -> Unit, logoLoaded: Boolean, onChooseLogo: () -> Unit, onClearLogo: () -> Unit) {
    Text("Znak graficzny obok kodu QR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    listOf(QR_BRAND_STATE to "Godło państwowe", QR_BRAND_FILE to "Logo z pliku", QR_BRAND_NONE to "Bez znaku").forEach { (value, label) ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onBrandingChange(value) }) {
            RadioButton(selected = branding == value, onClick = { onBrandingChange(value) })
            Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (branding == QR_BRAND_FILE) {
        OutlinedButton(onClick = onChooseLogo, modifier = Modifier.fillMaxWidth()) { Text(if (logoLoaded) "Zmień załączone logo" else "Wybierz logo z pliku") }
        if (logoLoaded) OutlinedButton(onClick = onClearLogo, modifier = Modifier.fillMaxWidth()) { Text("Usuń załączone logo") }
        Text("Logo jest wyświetlane poza kodem QR. Żaden jego fragment nie zasłania modułów kodu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
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
    OwdDocumentGroup("family", "Dokument rodowy", listOf(
        "Numer dokumentu" to "familyDocNumber", "Nazwa rodu" to "familyName", "Linia / gałąź" to "familyLine",
        "Miejsce w strukturze rodu" to "familyRole", "Prowadzący ewidencję" to "familyRegistry",
        "Data sporządzenia" to "familyIssueDate", "Data aktualizacji" to "familyExpiryDate",
        "Miejsce ewidencji" to "familyRegistryPlace", "Miejsce zamieszkania" to "familyResidence",
        "Linia ojca" to "familyFatherLine", "Linia matki" to "familyMotherLine",
        "Podstawa wpisu" to "familySource", "Uwagi" to "familyNotes",
        "Podpis posiadacza" to "familyHolderSignature", "Podpis wystawcy" to "familyRegistrarSignature"
    ) + listOf("Źródło danych" to "familyExtraSource", "Podstawa prawna" to "familyExtraLegalBasis",
        "Data weryfikacji" to "familyExtraVerifiedAt", "Podmiot weryfikujący" to "familyExtraVerifiedBy", "Uwagi dodatkowe" to "familyExtraNotes")),
    OwdDocumentGroup("parliament", "Legitymacja poselska", listOf(
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
internal fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val normalizedId = when (documentId) {
        "0" -> "profile"
        "1" -> "identity"
        "2" -> "passport"
        "3" -> "family"
        "4" -> "parliament"
        "5" -> "training"
        "6" -> "student"
        "7" -> "disability"
        "8", "loyalty_cards" -> "loyalty"
        "identity_card" -> "identity"
        else -> documentId
    }
    val allValues = remember { DemoIdentityStore.load(context) }
    val title = documentDisplayOptions.firstOrNull { it.first == normalizedId }?.second
        ?: when (normalizedId) {
            "loyalty" -> "Karty lojalnościowe"
            else -> normalizedId.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
    val settings = rememberLegitimacyPreviewSettings(normalizedId).value
    val previewValues = remember(allValues, normalizedId) {
        allValues.toMutableMap().apply {
            if (normalizedId == "disability" && this["firstNames"].isNullOrBlank()) {
                this["firstNames"] = this["disabilityPersonName"].orEmpty()
            }
        }
    }
    val options = credentialFieldOptions(normalizedId)
    val previewFields = options.map { (key, label) ->
        Triple(key, label, previewValues[key].orEmpty().ifBlank { "—" })
    }
    val (frontPreviewFields, reversePreviewFields) =
        splitCredentialFieldsForSides(
            previewFields, settings.orientation, settings.visibleFieldsConfigured, settings.visibleFields, settings.fieldSides
        )
    val holder = when {
        previewValues["studentNames"].orEmpty().isNotBlank() -> previewValues["studentNames"].orEmpty()
        previewValues["passportHolderNames"].orEmpty().isNotBlank() -> previewValues["passportHolderNames"].orEmpty()
        previewValues["disabilityPersonName"].orEmpty().isNotBlank() -> previewValues["disabilityPersonName"].orEmpty()
        else -> listOf(previewValues["firstNames"].orEmpty(), previewValues["lastName"].orEmpty())
            .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "DANE POSIADACZA" }
    }
    val photoBitmap = remember(previewValues["photo"]) {
        runCatching {
            val encoded = previewValues["photo"].orEmpty()
            if (encoded.isBlank()) null else {
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.getOrNull()
    }
    val validity = credentialValidityForDocument(normalizedId, previewValues)
    val hasData = options.any { (key, _) -> !previewValues[key].isNullOrBlank() } ||
        (normalizedId == "disability" && !previewValues["disabilityPersonName"].isNullOrBlank())
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "‹  Wróć do dokumentów",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Podgląd dokumentu", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (settings.showCard) {
            UnifiedCredentialCard(
                documentKey = normalizedId,
                title = title.uppercase(),
                subtitle = "Podgląd danych zapisanych w aplikacji",
                holderName = holder,
                fields = frontPreviewFields,
                settings = settings,
                photo = photoBitmap,
                validity = validity
            )
            if (settings.showReverse && reversePreviewFields.isNotEmpty()) {
                UnifiedCredentialCard(
                    documentKey = "${normalizedId}_reverse",
                    title = "${title.uppercase()} — REWERS",
                    subtitle = "Dalsze dane dokumentu",
                    holderName = holder,
                    fields = reversePreviewFields,
                    settings = settings.copy(showPhoto = false, showFlag = false, showCrest = false, showLogo = false, showHologram = false),
                    validity = validity
                )
            }
        } else {
            Text("Podgląd karty jest wyłączony w ustawieniach wyglądu tego dokumentu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        if (!hasData) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text("Brak zapisanych danych. Ten ekran prezentuje wyłącznie podgląd; edycja znajduje się w ustawieniach konkretnego dokumentu.",
                    Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

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
    val selected = when (selectionMode) {
        "documents" -> selectedDocuments.flatMap { populatedByDocument[it].orEmpty() }.toSet()
        "document_fields" -> selectedDocumentFields.intersect(populatedByDocument[selectedDocumentId].orEmpty())
        else -> allPopulatedKeys
    }
    var consent by remember { mutableStateOf(false) }
    var purpose by remember { mutableStateOf("Przekazanie danych wskazanemu odbiorcy") }
    var offerQr by remember { mutableStateOf<Bitmap?>(null) }
    var offerQrRemainingSeconds by remember { mutableIntStateOf(0) }
    var sessionMessage by remember { mutableStateOf("Wybierz zakres danych i zapoznaj się z informacją o ich pochodzeniu.") }
    LaunchedEffect(offerQr) {
        if (offerQr == null) {
            offerQrRemainingSeconds = 0
        } else {
            offerQrRemainingSeconds = 180
            while (offerQrRemainingSeconds > 0) {
                delay(1_000)
                offerQrRemainingSeconds = (offerQrRemainingSeconds - 1).coerceAtLeast(0)
            }
            offerQr = null
            sessionMessage = "Oferta QR OWD wygasła po 180 sekundach. Utwórz nowy kod, aby rozpocząć kolejną sesję."
        }
    }
    var qrBranding by remember { mutableStateOf(QR_BRAND_NONE) }
    var qrUploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    val qrLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }.onSuccess { qrUploadedLogo = it; offerQr = null }.onFailure { offerQr = null }
    }
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
        Text("Obywatelska Wymiana Danych", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("OWD • niezależna wymiana danych między zgodnymi aplikacjami", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Wybór danych do udostępnienia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (populated.isEmpty()) {
            Text("Brak zapisanych danych do udostępnienia. Uzupełnij je w Ustawieniach.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Zakres udostępnienia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            listOf(
                "all" to "Wszystkie dane i dokumenty",
                "documents" to "Wybrane dokumenty",
                "document_fields" to "Dane z wybranego dokumentu"
            ).forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectionMode == mode, onClick = {
                        selectionMode = mode
                        offerQr = null
                        consent = false
                        if (mode == "document_fields") selectedDocumentFields = populatedByDocument[selectedDocumentId].orEmpty()
                    })
                    Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (selectionMode == mode) FontWeight.Bold else FontWeight.Medium)
                }
            }
            when (selectionMode) {
                "documents" -> {
                    Text("Zaznacz dokumenty, których dane mają wejść do zakresu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }.forEach { group ->
                        val keys = populatedByDocument[group.id].orEmpty()
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = group.id in selectedDocuments, onCheckedChange = { checked ->
                                selectedDocuments = if (checked) selectedDocuments + group.id else selectedDocuments - group.id
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(group.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text("${keys.size} dostępnych pól", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                "document_fields" -> {
                    Text("Wybierz jeden dokument, a następnie zaznacz konkretne pola.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    val availableGroups = documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }
                    availableGroups.forEach { group ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedDocumentId == group.id, onClick = {
                                selectedDocumentId = group.id
                                selectedDocumentFields = populatedByDocument[group.id].orEmpty()
                                offerQr = null
                                consent = false
                            })
                            Text(group.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (selectedDocumentId == group.id) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                    val currentGroup = documentGroups.firstOrNull { it.id == selectedDocumentId }
                    currentGroup?.fields?.filter { it.second in populatedByDocument[selectedDocumentId].orEmpty() }?.forEach { (label, key) ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = key in selectedDocumentFields, onCheckedChange = { checked ->
                                selectedDocumentFields = if (checked) selectedDocumentFields + key else selectedDocumentFields - key
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text(fields[key].orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Text("Wybrano ${selected.size} pól z ${allPopulatedKeys.size}. Zmiana zakresu unieważnia poprzednią zgodę i kod QR.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(value = purpose, onValueChange = { purpose = it; consent = false; offerQr = null },
            modifier = Modifier.fillMaxWidth(), label = { Text("Cel udostępnienia") }, minLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = consent, onCheckedChange = { consent = it; offerQr = null })
            Text("Potwierdzam świadomy wybór zakresu i celu udostępnienia danych.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
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
        Text(sessionMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        offerQr?.let { bitmap ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Oferta sesji OWD • pozostało ${offerQrRemainingSeconds / 60}:${(offerQrRemainingSeconds % 60).toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                    Image(bitmap.asImageBitmap(), contentDescription = "Kod QR oferty sesji OWD", modifier = Modifier.size(250.dp))
                     Text("To inicjuje sesję kryptograficzną. Skaner OWD potrafi rozpoznać i zweryfikować ofertę; wymiana kluczy, przesłanie rekordów i potwierdzenie odbioru wymagają jeszcze pełnego połączenia obu urządzeń.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        OutlinedButton(onClick = onOpenVCard, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator wizytówki vCard") }
        OutlinedButton(onClick = onOpenWebsite, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator kodu strony internetowej") }
        Text("Prywatność: dane nie są umieszczane w kodzie inicjującym. Odbiorca i cel muszą zostać zweryfikowani przed właściwym transferem. Dane rodzinne/rodowe są całkowicie wyłączone z obecnego zakresu OWD.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
        Text(saveMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
        Text("Kod QR strony internetowej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Kod zawiera adres strony. Zeskanowanie otworzy go w przeglądarce po zatwierdzeniu przez użytkownika.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Adres strony (URL)") }, placeholder = { Text("https://example.org") }, singleLine = true)
        QrBrandingControls(branding, { branding = it }, uploadedLogo != null, { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) }, { uploadedLogo = null; message = "Usunięto logo." })
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kod QR strony", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR strony internetowej", modifier = Modifier.size(260.dp), contentScale = ContentScale.Fit)
                     Text(normalized, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    SaveQrImageButton(qrBitmap, "adres-url-qr.png")
                }
            }
        } else Text(if (!validUrl) "Podaj poprawny adres rozpoczynający się od http:// lub https://." else "Wybierz logo z pliku albo ustaw opcję „Bez znaku”.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Godło lub logo jest wyświetlane obok kodu. Sam kod pozostaje czysty, bez nakładek. Przed publikacją sprawdź skanowanie na kilku urządzeniach.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
            if (hasProfileName) "Wstępnie uzupełniono dane profilu. Sprawdź i popraw pola przed utworzeniem kodu."
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
        Text("Kod QR", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Generator kodu kontaktowego", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Utwórz kod QR zawierający wizytówkę vCard. Zgodna aplikacja odbiorcy może odczytać dane i zaproponować zapis kontaktu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Wizytówka kontaktowa", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Kod zawiera dane kontaktowe wybrane przez użytkownika.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { vCardPicker.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Wczytaj plik vCard (.vcf)")
        }
        Text(importMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        QrBrandingControls(
            branding = branding,
            onBrandingChange = { branding = it },
            logoLoaded = uploadedLogo != null,
            onChooseLogo = { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { uploadedLogo = null; logoMessage = "Usunięto logo." }
        )
        Text(logoMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Dane kontaktowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Pola są domyślnie uzupełniane z profilu. Możesz je edytować przed wygenerowaniem wizytówki. Dane identyfikacyjne, takie jak PESEL i numery dokumentów, nie są kopiowane do kodu QR.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Twój kod QR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
            Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text("Prywatność: kod ujawnia każdej osobie skanującej wszystkie zapisane w nim dane. Nie dodawaj numeru PESEL, danych dokumentu ani informacji poufnych.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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

