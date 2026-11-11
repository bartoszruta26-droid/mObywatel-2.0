// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class CredentialFieldOption(val key: String, val label: String)

internal fun credentialFieldOptions(documentKey: String): List<Pair<String, String>> = (when (documentKey) {
    "parliament" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "mpCardNumber" to "Numer legitymacji",
        "mpTerm" to "Kadencja", "mpDistrict" to "Okręg wyborczy", "mpStart" to "Początek mandatu",
        "mpEnd" to "Koniec mandatu", "mpCardIssueDate" to "Data wydania", "mpIssuer" to "Wystawca"
    )
    "training" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "trainingInstitution" to "Instytucja",
        "trainingCourseName" to "Kurs / kwalifikacja", "trainingCardNumber" to "Numer legitymacji",
        "trainingIssueDate" to "Data wydania", "trainingValidUntil" to "Termin ważności",
        "trainingCourseCode" to "Kod kursu", "trainingStartDate" to "Data rozpoczęcia",
        "trainingEndDate" to "Data zakończenia", "trainingStatus" to "Status uczestnictwa",
        "trainingIssuer" to "Wystawca", "trainingCompletionDate" to "Data ukończenia",
        "trainingHours" to "Liczba godzin", "trainingAssessment" to "Wynik", "trainingCertificateNumber" to "Numer zaświadczenia"
    )
    "professional", "professional_medical", "professional_education", "professional_therapeutic", "professional_other" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "professionalTitle" to "Tytuł / kwalifikacja",
        "professionalNumber" to "Numer dokumentu", "professionalIssuer" to "Wystawca",
        "professionalValidity" to "Termin ważności", "professionalSpeciality" to "Specjalność",
        "professionalQualification" to "Zakres kwalifikacji"
    )
    "student" -> listOf(
        "studentNames" to "Imię i nazwisko", "studentUniversity" to "Uczelnia",
        "studentProgramme" to "Kierunek", "studentCardNumber" to "Numer legitymacji",
        "studentIssueDate" to "Data wydania", "studentValidity" to "Termin ważności",
        "studentFaculty" to "Wydział", "studentLevel" to "Poziom studiów",
        "studentProfile" to "Profil / forma", "studentYear" to "Rok i semestr",
        "studentAcademicYear" to "Rok akademicki", "studentStatus" to "Status"
    )
    "identity" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia",
        "birthPlace" to "Miejsce urodzenia", "citizenship" to "Obywatelstwo", "sex" to "Płeć",
        "pesel" to "PESEL", "idNumber" to "Numer dokumentu", "idIssueDate" to "Data wydania",
        "idExpiryDate" to "Data ważności", "idIssuer" to "Organ wydający"
    )
    "passport" -> listOf(
        "passportHolderNames" to "Imię i nazwisko", "passportNumber" to "Numer paszportu",
        "passportCitizenship" to "Obywatelstwo", "birthDate" to "Data urodzenia",
        "passportIssueDate" to "Data wydania", "passportExpiryDate" to "Data ważności",
        "passportType" to "Rodzaj paszportu", "passportCountry" to "Państwo wydania",
        "passportIssuer" to "Organ wydający"
    )
    "family", "organization_family" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "familyName" to "Nazwa Rodu",
        "familyLine" to "Linia / gałąź", "familyRole" to "Miejsce w Rodzie",
        "familyDocNumber" to "Numer dokumentu", "familyIssueDate" to "Data sporządzenia",
        "familyRegistry" to "Prowadzący ewidencję", "familySource" to "Podstawa wpisu"
    )
    "disability" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "disabilityDecisionType" to "Rodzaj orzeczenia",
        "disabilityDecisionNumber" to "Numer orzeczenia", "disabilityCaseNumber" to "Numer sprawy",
        "disabilityDegree" to "Stopień", "disabilityValidity" to "Okres obowiązywania",
        "disabilityIssuerName" to "Wystawca", "disabilityLegalBasis" to "Podstawa prawna"
    )
    "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister", "church_missionary" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "churchRole" to "Posługa / godność",
        "churchDocumentNumber" to "Numer dokumentu", "churchIssueDate" to "Data wydania",
        "churchValidity" to "Okres obowiązywania", "churchIssuer" to "Wystawca"
    )
    "organization_foundation", "organization_association", "organization_church", "organization_company", "entity", "business", "registry" -> listOf(
        "entityName" to "Nazwa podmiotu", "entityKrs" to "Numer KRS", "entityNip" to "NIP",
        "entityRegon" to "REGON", "entityRepresentative" to "Reprezentant"
    )
    "loyalty", "loyalty_cards" -> listOf(
        "loyaltyName" to "Nazwa programu", "loyaltyNumber" to "Numer karty", "loyaltyHolder" to "Posiadacz"
    )
    "bank_accounts" -> listOf(
        "bankName" to "Bank", "accountName" to "Nazwa rachunku", "accountNumber" to "Numer rachunku",
        "accountOwner" to "Właściciel", "accountCurrency" to "Waluta", "accountStatus" to "Status rachunku"
    )
    "children" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia",
        "childRelationship" to "Relacja", "guardianName" to "Opiekun", "childDocumentNumber" to "Numer dokumentu"
    )
    "vaccines" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "vaccineName" to "Preparat",
        "vaccineDate" to "Data szczepienia", "vaccineDose" to "Dawka", "vaccineBatch" to "Seria",
        "vaccineIssuer" to "Podmiot wykonujący"
    )
    "civil" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "civilDocumentNumber" to "Numer dokumentu",
        "civilIssueDate" to "Data sporządzenia", "civilIssuer" to "Wystawca"
    )
    "profile" -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia",
        "contactPhone" to "Telefon", "contactEmail" to "E-mail", "contactOrganization" to "Organizacja",
        "contactTitle" to "Stanowisko", "contactUrl" to "Strona internetowa", "contactAddress" to "Adres"
    )
    "birth_extract" -> listOf("civilActType" to "Rodzaj aktu", "firstNames" to "Imiona", "lastName" to "Nazwisko", "birthDate" to "Data urodzenia", "civilDocumentNumber" to "Numer aktu", "civilExtractType" to "Rodzaj odpisu", "civilIssueDate" to "Data wydania", "civilIssuer" to "Wystawca")
    "marriage_extract" -> listOf("civilActType" to "Rodzaj aktu", "spouseNames" to "Małżonkowie", "marriageDate" to "Data zawarcia małżeństwa", "civilDocumentNumber" to "Numer aktu", "civilExtractType" to "Rodzaj odpisu", "civilIssueDate" to "Data wydania", "civilIssuer" to "Wystawca")
    "death_extract" -> listOf("deceasedName" to "Osoba zmarła", "deathDate" to "Data zgonu", "civilDocumentNumber" to "Numer aktu", "civilIssueDate" to "Data wydania", "civilIssuer" to "Wystawca")
    "large_family" -> listOf("firstNames" to "Imię i nazwisko", "pesel" to "PESEL / data urodzenia", "largeFamilyNumber" to "Numer karty", "largeFamilyType" to "Typ karty", "largeFamilyIssueDate" to "Data wydania", "largeFamilyExpiry" to "Ważna do", "issuer" to "Organ wydający", "status" to "Status")
    "blood_donor" -> listOf("firstNames" to "Imię i nazwisko", "bloodDonorNumber" to "Numer legitymacji", "bloodDonorTitle" to "Stopień / tytuł", "bloodDonorLitres" to "Liczba oddanych litrów", "bloodDonorIssueDate" to "Data przyznania", "issuer" to "Organ wydający", "status" to "Status")
    "disability_card" -> listOf("firstNames" to "Imię i nazwisko", "disabilityCardNumber" to "Numer legitymacji", "disabilityCardIssueDate" to "Data wydania", "disabilityCardExpiry" to "Data ważności", "issuer" to "Organ wydający", "disabilityDecisionNumber" to "Numer orzeczenia", "disabilityDegree" to "Stopień", "status" to "Status")
    "vehicle_registration" -> listOf("vehicleRegistrationNumber" to "Numer rejestracyjny", "vehicleVin" to "VIN", "vehicleMakeModel" to "Marka i model", "vehicleDocumentNumber" to "Numer dowodu", "vehicleOwner" to "Właściciel", "vehicleFirstRegistration" to "Data pierwszej rejestracji", "issuer" to "Organ rejestrujący", "vehicleInspectionDue" to "Termin badania technicznego", "status" to "Status")
    "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business" -> listOf("insuranceProvider" to "Ubezpieczyciel", "insurancePolicyNumber" to "Numer polisy", "insuranceInsured" to "Ubezpieczony", "insuranceScope" to "Zakres ochrony", "insuranceStart" to "Okres ochrony od", "insuranceEnd" to "Okres ochrony do", "insuranceCoverageAmount" to "Suma gwarancyjna", "insuranceExclusions" to "Wyłączenia / uwagi", "insurancePremiumPaidDate" to "Data opłacenia składki", "entityName" to "Nazwa firmy / podmiotu", "entityNip" to "NIP", "insuranceStatus" to "Status")
    "criminal_record" -> listOf("firstNames" to "Imię i nazwisko", "pesel" to "PESEL", "criminalRecordNumber" to "Numer zaświadczenia", "criminalRecordIssueDate" to "Data wydania", "issuer" to "Organ wydający", "criminalRecordScope" to "Zakres informacji", "status" to "Status weryfikacji pliku")
    "mission_travel" -> listOf("missionOrganization" to "Organizacja", "missionBasis" to "Podstawa delegowania", "missionHolder" to "Delegowany", "missionRole" to "Funkcja / rola", "missionScope" to "Cel i zakres misji", "missionDestination" to "Państwo docelowe", "missionStart" to "Data rozpoczęcia", "missionEnd" to "Data zakończenia", "missionNumber" to "Numer decyzji", "missionInsurance" to "Ubezpieczenie i kontakt", "missionPermits" to "Wizy / zgody / akredytacje", "status" to "Status dokumentów")
    "ehic" -> listOf("firstNames" to "Imię i nazwisko", "ehicNumber" to "Numer karty", "pesel" to "Numer identyfikacyjny", "issuer" to "Instytucja wydająca", "ehicIssueDate" to "Data wydania", "ehicExpiry" to "Data ważności", "status" to "Status")
    "eu_education" -> listOf("firstNames" to "Imię i nazwisko", "professionalTitle" to "Kwalifikacja / zawód", "educationInstitution" to "Instytucja kształcąca", "educationLevel" to "Poziom kształcenia", "educationDiplomaNumber" to "Numer dyplomu", "issuer" to "Organ wydający", "educationLegalBasis" to "Podstawa prawna", "educationDestination" to "Państwo docelowe", "educationIssueDate" to "Data wydania", "educationCaseNumber" to "Numer sprawy", "educationScope" to "Zakres potwierdzenia", "status" to "Status")
    "signed_pdf", "imported" -> listOf(
        "documentTitle" to "Nazwa dokumentu", "documentNumber" to "Numer dokumentu",
        "documentIssuer" to "Wystawca", "documentDate" to "Data dokumentu"
    )
    else -> listOf(
        "firstNames" to "Imiona", "lastName" to "Nazwisko", "documentNumber" to "Numer dokumentu",
        "issueDate" to "Data wydania", "expiryDate" to "Termin ważności", "issuer" to "Wystawca"
    )
}) + listOf("details" to "Informacje dodatkowe")

internal fun isCredentialValidByExpiry(rawExpiry: String): Boolean {
    val value = rawExpiry.trim()
    // Pusty termin lub opis tekstowy (np. „bezterminowo”, „do odwołania”)
    // nie oznacza utraty ważności. Nieważność wynika wyłącznie z rozpoznanej,
    // kalendarzowej daty, która już upłynęła.
    if (value.isEmpty()) return true

    val polishLocale = java.util.Locale.forLanguageTag("pl-PL")
    val formats = listOf(
        java.time.format.DateTimeFormatter.ofPattern("d-M-uuuu"),
        java.time.format.DateTimeFormatter.ofPattern("d.M.uuuu"),
        java.time.format.DateTimeFormatter.ofPattern("d/M/uuuu"),
        java.time.format.DateTimeFormatter.ofPattern("d MMMM uuuu", polishLocale),
        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
    )
    val parsed = formats.firstNotNullOfOrNull { formatter ->
        runCatching {
            java.time.LocalDate.parse(value, formatter.withResolverStyle(java.time.format.ResolverStyle.STRICT))
        }.getOrNull()
    }
    // Nieznany format pozostaje wartością tekstową i zgodnie z regułą jest ważny.
    // Dzień wskazany jako ostatni dzień ważności pozostaje ważny do końca tego dnia.
    return parsed == null || !parsed.isBefore(java.time.LocalDate.now())
}

internal data class LegitimacyPreviewSettings(
    val showCard: Boolean = true,
    val showReverse: Boolean = false,
    val crestAssetId: String = "",
    val flagAssetId: String = "",
    val backgroundAssetId: String = "",
    val logoAssetId: String = "",
    val crestCaption: String = "",
    val captionPosition: String = "Pod herbem",
    val orientation: String = "PIONOWA",
    val visibleFields: Set<String> = emptySet(),
    val visibleFieldsConfigured: Boolean = false,
    val showPhoto: Boolean = true,
    val showFlag: Boolean = true,
    val showCrest: Boolean = true,
    val showLogo: Boolean = true,
    val showBackground: Boolean = true,
    val graphicScale: Float = 1f,
    val graphicRotation: Float = 0f,
    val graphicOffsetX: Float = 0f,
    val graphicOffsetY: Float = 0f,
    val labelScale: Float = 1f,
    val labelRotation: Float = 0f,
    val labelOffsetX: Float = 0f,
    val labelOffsetY: Float = 0f,
    val valueScale: Float = 1f,
    val valueRotation: Float = 0f,
    val valueOffsetX: Float = 0f,
    val valueOffsetY: Float = 0f,
    val fontFamily: String = "sans"
)

internal object LegitimacyPreviewSettingsStore {
    private const val PREFS = "legitimacy_preview_preferences"

    fun load(context: Context, key: String): LegitimacyPreviewSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedVisibleFields = p.getStringSet("${key}_visible_fields", emptySet())?.toSet().orEmpty()
        return LegitimacyPreviewSettings(
            showCard = p.getBoolean("${key}_show_card", true),
            showReverse = p.getBoolean("${key}_show_reverse", false),
            crestAssetId = p.getString("${key}_crest", "").orEmpty(),
            flagAssetId = p.getString("${key}_flag", "").orEmpty(),
            backgroundAssetId = p.getString("${key}_background", "").orEmpty(),
            logoAssetId = p.getString("${key}_logo", "").orEmpty(),
            crestCaption = p.getString("${key}_caption", "").orEmpty(),
            captionPosition = p.getString("${key}_caption_position", "Pod herbem").orEmpty(),
            orientation = p.getString("${key}_orientation", "PIONOWA").orEmpty(),
            visibleFields = storedVisibleFields,
            visibleFieldsConfigured = p.getBoolean("${key}_visible_fields_configured", storedVisibleFields.isNotEmpty()),
            showPhoto = p.getBoolean("${key}_show_photo", true),
            showFlag = p.getBoolean("${key}_show_flag", true),
            showCrest = p.getBoolean("${key}_show_crest", true),
            showLogo = p.getBoolean("${key}_show_logo", true),
            showBackground = p.getBoolean("${key}_show_background", true),
            graphicScale = p.getFloat("${key}_graphic_scale", 1f),
            graphicRotation = p.getFloat("${key}_graphic_rotation", 0f),
            graphicOffsetX = p.getFloat("${key}_graphic_x", 0f),
            graphicOffsetY = p.getFloat("${key}_graphic_y", 0f),
            labelScale = p.getFloat("${key}_label_scale", 1f),
            labelRotation = p.getFloat("${key}_label_rotation", 0f),
            labelOffsetX = p.getFloat("${key}_label_x", 0f),
            labelOffsetY = p.getFloat("${key}_label_y", 0f),
            valueScale = p.getFloat("${key}_value_scale", 1f),
            valueRotation = p.getFloat("${key}_value_rotation", 0f),
            valueOffsetX = p.getFloat("${key}_value_x", 0f),
            valueOffsetY = p.getFloat("${key}_value_y", 0f),
            fontFamily = p.getString("${key}_font_family", "sans").orEmpty()
        )
    }

    fun save(context: Context, key: String, value: LegitimacyPreviewSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("${key}_show_card", value.showCard)
            .putBoolean("${key}_show_reverse", value.showReverse)
            .putString("${key}_crest", value.crestAssetId)
            .putString("${key}_flag", value.flagAssetId)
            .putString("${key}_background", value.backgroundAssetId)
            .putString("${key}_logo", value.logoAssetId)
            .putString("${key}_caption", value.crestCaption)
            .putString("${key}_caption_position", value.captionPosition)
            .putString("${key}_orientation", value.orientation)
            .putStringSet("${key}_visible_fields", value.visibleFields)
            .putBoolean("${key}_visible_fields_configured", value.visibleFieldsConfigured)
            .putBoolean("${key}_show_photo", value.showPhoto)
            .putBoolean("${key}_show_flag", value.showFlag)
            .putBoolean("${key}_show_crest", value.showCrest)
            .putBoolean("${key}_show_logo", value.showLogo)
            .putBoolean("${key}_show_background", value.showBackground)
            .putFloat("${key}_graphic_scale", value.graphicScale)
            .putFloat("${key}_graphic_rotation", value.graphicRotation)
            .putFloat("${key}_graphic_x", value.graphicOffsetX)
            .putFloat("${key}_graphic_y", value.graphicOffsetY)
            .putFloat("${key}_label_scale", value.labelScale)
            .putFloat("${key}_label_rotation", value.labelRotation)
            .putFloat("${key}_label_x", value.labelOffsetX)
            .putFloat("${key}_label_y", value.labelOffsetY)
            .putFloat("${key}_value_scale", value.valueScale)
            .putFloat("${key}_value_rotation", value.valueRotation)
            .putFloat("${key}_value_x", value.valueOffsetX)
            .putFloat("${key}_value_y", value.valueOffsetY)
            .putString("${key}_font_family", value.fontFamily)
            .apply()
    }
    
    /** Serializes every per-document layout preference into the vCard archive. */
    fun exportAll(context: Context): Map<String, String> {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return preferences.all.mapNotNull { (key, value) ->
            val encoded = when (value) {
                is Boolean -> "b:" + value.toString()
                is String -> "s:" + value
                is Set<*> -> "a:" + org.json.JSONArray(value.filterIsInstance<String>()).toString()
                else -> null
            }
            encoded?.let { "cs_$key" to it }
        }.toMap()
    }

    /** Restores only recognized credential-setting values; unrelated profile fields are untouched. */
    fun importAll(context: Context, values: Map<String, String>) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        values.filterKeys { it.startsWith("cs_") }.forEach { (archiveKey, encoded) ->
            val key = archiveKey.removePrefix("cs_")
            require(key.matches(Regex("[A-Za-z0-9_-]{1,96}"))) { "Nieprawidłowy klucz ustawienia legitymacji." }
            when {
                encoded.startsWith("b:") -> editor.putBoolean(key, encoded.removePrefix("b:").toBooleanStrict())
                encoded.startsWith("s:") -> editor.putString(key, encoded.removePrefix("s:"))
                encoded.startsWith("a:") -> {
                    val json = org.json.JSONArray(encoded.removePrefix("a:"))
                    editor.putStringSet(key, (0 until json.length()).map { json.getString(it) }.toSet())
                }
                else -> error("Nieobsługiwany format ustawienia legitymacji: $key")
            }
        }
        check(editor.commit()) { "Nie udało się przywrócić ustawień legitymacji." }
    }
}

@Composable
internal fun rememberLegitimacyPreviewSettings(documentKey: String): MutableState<LegitimacyPreviewSettings> {
    val context = LocalContext.current
    val state = remember(documentKey) { mutableStateOf(LegitimacyPreviewSettingsStore.load(context, documentKey)) }
    return state
}

@Composable
internal fun LegitimacyPreviewSettingsPanel(
    documentKey: String,
    settings: LegitimacyPreviewSettings,
    onChange: (LegitimacyPreviewSettings) -> Unit,
    assetRevision: Int = 0
) {
    val context = LocalContext.current
    var assets by remember(documentKey) { mutableStateOf(DocumentBrandStore.list(context)) }
    var crestMenu by remember { mutableStateOf(false) }
    var flagMenu by remember { mutableStateOf(false) }
    var backgroundMenu by remember { mutableStateOf(false) }
    var logoMenu by remember { mutableStateOf(false) }
    var positionMenu by remember { mutableStateOf(false) }
    var importKind by remember { mutableStateOf("Herb") }
    var message by remember { mutableStateOf("") }

    fun update(value: LegitimacyPreviewSettings) {
        LegitimacyPreviewSettingsStore.save(context, documentKey, value)
        val orientationKey = if (value.orientation == "POZIOMA") "horizontal" else "vertical"
        LegitimacyPreviewSettingsStore.save(context, documentKey + "_" + orientationKey, value)
        onChange(value)
    }

    fun selectOrientation(target: String) {
        val orientationKey = if (target == "POZIOMA") "horizontal" else "vertical"
        val stored = LegitimacyPreviewSettingsStore.load(context, documentKey + "_" + orientationKey)
        val next = stored.copy(orientation = target)
        update(next)
    }

    val crestPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Herb", "Herb legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Herb" }
            if (imported != null) update(settings.copy(crestAssetId = imported.id))
            message = "Wczytano herb. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać herbu: ${it.message ?: "błąd"}" }
    }
    val flagPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Flaga", "Flaga legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Flaga" }
            if (imported != null) update(settings.copy(flagAssetId = imported.id))
            message = "Wczytano flagę. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać flagi: ${it.message ?: "błąd"}" }
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:${documentKey}", "Logo", "Logo wystawcy")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:${documentKey}" && it.kind == "Logo" }
            if (imported != null) update(settings.copy(logoAssetId = imported.id))
            message = "Wczytano logo. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać logo." }
    }

    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, "legitimacy:$documentKey", "Tło dokumentu", "Tło legitymacji")
        }.onSuccess {
            assets = DocumentBrandStore.list(context)
            val imported = assets.lastOrNull { it.owner == "legitimacy:$documentKey" && it.kind == "Tło dokumentu" }
            if (imported != null) update(settings.copy(backgroundAssetId = imported.id))
            message = "Wczytano tło legitymacji. Ustawienie zapisano."
        }.onFailure { message = "Nie udało się wczytać tła: ${it.message ?: "błąd"}" }
    }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ustawienia wyglądu dokumentu", fontWeight = FontWeight.Bold)
            Text("Orientacja", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.orientation == "PIONOWA", onClick = { selectOrientation("PIONOWA") })
                Text("Pionowa")
                Spacer(Modifier.width(12.dp))
                RadioButton(selected = settings.orientation == "POZIOMA", onClick = { selectOrientation("POZIOMA") })
                Text("Pozioma")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showCard, onCheckedChange = { update(settings.copy(showCard = it)) })
                Text("Wyświetlaj całą legitymację w podglądzie")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = settings.showReverse, onCheckedChange = { update(settings.copy(showReverse = it)) })
                Text("Wyświetlaj rewers (domyślnie ukryty)")
            }
            Text("Elementy wzoru", fontWeight = FontWeight.SemiBold)
            listOf(
                Triple(settings.showPhoto, "Fotografia", { v: Boolean -> update(settings.copy(showPhoto = v)) }),
                Triple(settings.showFlag, "Flaga", { v: Boolean -> update(settings.copy(showFlag = v)) }),
                Triple(settings.showCrest, "Godło / herb", { v: Boolean -> update(settings.copy(showCrest = v)) }),
                Triple(settings.showLogo, "Logo wystawcy", { v: Boolean -> update(settings.copy(showLogo = v)) }),
                Triple(settings.showBackground, "Tło dokumentu", { v: Boolean -> update(settings.copy(showBackground = v)) })
            ).forEach { (checked, label, onToggle) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = onToggle)
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Widoczność pól ustawiasz bezpośrednio przy odpowiednim polu danych.", style = MaterialTheme.typography.bodySmall)
            Text("Herb / godło / logo", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { crestMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.crestAssetId }?.label ?: "Wybierz herb z katalogu")
                }
                DropdownMenu(expanded = crestMenu, onDismissRequest = { crestMenu = false }) {
                    DropdownMenuItem(text = { Text("Brak herbu") }, onClick = { update(settings.copy(crestAssetId = "")); crestMenu = false })
                    assets.filter { it.kind == "Herb" || it.kind == "Godło" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(crestAssetId = asset.id)); crestMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { crestPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własny herb")
            }
            Text("Dwuwierszowy napis obok godła / herbu", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = settings.crestCaption,
                onValueChange = { update(settings.copy(crestCaption = it)) },
                label = { Text("Napis, np. Rzeczpospolita\nPolska") },
                placeholder = { Text("Rzeczpospolita\nPolska") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 2
            )
            Box {
                OutlinedButton(onClick = { positionMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Położenie napisu: ${settings.captionPosition}")
                }
                DropdownMenu(expanded = positionMenu, onDismissRequest = { positionMenu = false }) {
                    listOf("Nad herbem", "Pod herbem", "Po lewej", "Po prawej").forEach { position ->
                        DropdownMenuItem(text = { Text(position) }, onClick = {
                            update(settings.copy(captionPosition = position)); positionMenu = false
                        })
                    }
                }
            }
            Text("Flaga", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { flagMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.flagAssetId }?.label ?: "Wybierz flagę z katalogu")
                }
                DropdownMenu(expanded = flagMenu, onDismissRequest = { flagMenu = false }) {
                    DropdownMenuItem(text = { Text("Brak własnej flagi") }, onClick = { update(settings.copy(flagAssetId = "")); flagMenu = false })
                    assets.filter { it.kind == "Flaga" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(flagAssetId = asset.id)); flagMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { flagPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własną flagę")
            }
            Text("Logo wystawcy", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { logoMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.logoAssetId }?.label ?: "Wybierz logo z katalogu")
                }
                DropdownMenu(expanded = logoMenu, onDismissRequest = { logoMenu = false }) {
                    DropdownMenuItem(text = { Text("Brak własnego logo") }, onClick = { update(settings.copy(logoAssetId = "")); logoMenu = false })
                    assets.filter { it.kind == "Logo" }.forEach { asset ->
                        DropdownMenuItem(text = { Text(asset.label + " · " + asset.owner) }, onClick = {
                            update(settings.copy(logoAssetId = asset.id)); logoMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własne logo")
            }
            Text("Tło legitymacji", fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(onClick = { backgroundMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assets.firstOrNull { it.id == settings.backgroundAssetId }?.label ?: "Wybierz tło z katalogu")
                }
                DropdownMenu(expanded = backgroundMenu, onDismissRequest = { backgroundMenu = false }) {
                    DropdownMenuItem(text = { Text("Domyślne tło") }, onClick = { update(settings.copy(backgroundAssetId = "")); backgroundMenu = false })
                    assets.filter { it.kind == "Tło dokumentu" }.forEach { asset ->
                        DropdownMenuItem(text = { Text("${asset.label} · ${asset.owner}") }, onClick = {
                            update(settings.copy(backgroundAssetId = asset.id)); backgroundMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { backgroundPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Wczytaj własne tło")
            }
            if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text("Położenie i rozmiar grafiki", fontWeight = FontWeight.SemiBold)
            Text("Skala grafiki: ${"%.2f".format(settings.graphicScale)}")
            Slider(settings.graphicScale, { update(settings.copy(graphicScale = it)) }, valueRange = 0.5f..2.5f)
            Text("Obrót grafiki: ${settings.graphicRotation.toInt()}°")
            Slider(settings.graphicRotation, { update(settings.copy(graphicRotation = it)) }, valueRange = -180f..180f)
            Text("Przesunięcie poziome grafiki: ${settings.graphicOffsetX.toInt()} dp")
            Slider(settings.graphicOffsetX, { update(settings.copy(graphicOffsetX = it)) }, valueRange = -60f..60f)
            Text("Przesunięcie pionowe grafiki: ${settings.graphicOffsetY.toInt()} dp")
            Slider(settings.graphicOffsetY, { update(settings.copy(graphicOffsetY = it)) }, valueRange = -60f..60f)
            Text("Typografia etykiet", fontWeight = FontWeight.SemiBold)
            Text("Skala etykiet: ${"%.2f".format(settings.labelScale)}")
            Slider(settings.labelScale, { update(settings.copy(labelScale = it)) }, valueRange = 0.6f..2f)
            Text("Obrót etykiet: ${settings.labelRotation.toInt()}°")
            Slider(settings.labelRotation, { update(settings.copy(labelRotation = it)) }, valueRange = -180f..180f)
            Text("Przesunięcie etykiet X: ${settings.labelOffsetX.toInt()} dp")
            Slider(settings.labelOffsetX, { update(settings.copy(labelOffsetX = it)) }, valueRange = -40f..40f)
            Text("Przesunięcie etykiet Y: ${settings.labelOffsetY.toInt()} dp")
            Slider(settings.labelOffsetY, { update(settings.copy(labelOffsetY = it)) }, valueRange = -40f..40f)
            Text("Typografia danych", fontWeight = FontWeight.SemiBold)
            Text("Skala danych: ${"%.2f".format(settings.valueScale)}")
            Slider(settings.valueScale, { update(settings.copy(valueScale = it)) }, valueRange = 0.6f..2f)
            Text("Obrót danych: ${settings.valueRotation.toInt()}°")
            Slider(settings.valueRotation, { update(settings.copy(valueRotation = it)) }, valueRange = -180f..180f)
            Text("Przesunięcie danych X: ${settings.valueOffsetX.toInt()} dp")
            Slider(settings.valueOffsetX, { update(settings.copy(valueOffsetX = it)) }, valueRange = -40f..40f)
            Text("Przesunięcie danych Y: ${settings.valueOffsetY.toInt()} dp")
            Slider(settings.valueOffsetY, { update(settings.copy(valueOffsetY = it)) }, valueRange = -40f..40f)
            var fontMenu by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { fontMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Krój pisma: " + when (settings.fontFamily) {
                        "serif" -> "Szeryfowy"
                        "mono" -> "Monospace"
                        "cursive" -> "Ozdobny"
                        else -> "Bezszeryfowy"
                    })
                }
                DropdownMenu(expanded = fontMenu, onDismissRequest = { fontMenu = false }) {
                    listOf("sans" to "Bezszeryfowy (systemowy)", "serif" to "Szeryfowy (systemowy)",
                        "mono" to "Monospace (systemowy)", "cursive" to "Ozdobny (systemowy)").forEach { (id, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            update(settings.copy(fontFamily = id)); fontMenu = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = {
                update(settings.copy(graphicScale = 1f, graphicRotation = 0f, graphicOffsetX = 0f,
                    graphicOffsetY = 0f, labelScale = 1f, labelRotation = 0f, labelOffsetX = 0f,
                    labelOffsetY = 0f, valueScale = 1f, valueRotation = 0f, valueOffsetX = 0f,
                    valueOffsetY = 0f, fontFamily = "sans"))
            }, modifier = Modifier.fillMaxWidth()) { Text("Przywróć domyślne przekształcenia") }
            CredentialTemplatePreview(documentKey, settings, assetRevision)
            Text("Ustawienia są zapisywane lokalnie i pozostają zachowane po ponownym uruchomieniu aplikacji.", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
internal fun LegitimacyHeraldry(
    settings: LegitimacyPreviewSettings,
    documentKey: String,
    vertical: Boolean = false,
    assetRevision: Int = 0,
    title: String = "DOKUMENT",
    subtitle: String = ""
) {
    val context = LocalContext.current
    val assignedCrest = remember(documentKey, assetRevision, title) {
        DocumentBrandStore.imageForDocument(context, documentKey, title, "Herb")
            ?: DocumentBrandStore.imageForDocument(context, documentKey, title, "Godło")
            ?: defaultDocumentEmblem(context, title)
    }
    val assignedFlag = remember(documentKey, assetRevision, title) {
        DocumentBrandStore.imageForDocument(context, documentKey, title, "Flaga")
            ?: defaultDocumentFlag(context, title)
    }
    val selectedCrest = remember(settings.crestAssetId, assignedCrest) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } ?: assignedCrest
    }
    val selectedFlag = remember(settings.flagAssetId, assignedFlag) {
        settings.flagAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } ?: assignedFlag
    }
    val caption = settings.crestCaption.ifBlank { "Rzeczpospolita\nPolska" }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (settings.showFlag && selectedFlag != null) {
            Image(
                selectedFlag.asImageBitmap(),
                contentDescription = "Flaga dokumentu",
                modifier = Modifier.fillMaxWidth(0.78f).height(19.dp).graphicsLayer { scaleX = settings.graphicScale; scaleY = settings.graphicScale; rotationZ = settings.graphicRotation; translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density },
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.showCrest && selectedCrest != null) {
                Image(
                    selectedCrest.asImageBitmap(),
                    contentDescription = "Godło lub herb dokumentu",
                    modifier = Modifier.size(31.dp).graphicsLayer { scaleX = settings.graphicScale; scaleY = settings.graphicScale; rotationZ = settings.graphicRotation; translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density },
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            if (settings.showCrest) Spacer(Modifier.width(4.dp))
            if (settings.showCrest || caption.isNotBlank()) {
                Text(
                    caption,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CredentialFieldValue(
    item: Triple<String, String, String>,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    settings: LegitimacyPreviewSettings
) {
    val family = when (settings.fontFamily) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(item.second.uppercase(), fontSize = labelFontSize * settings.labelScale,
            fontFamily = family, fontWeight = FontWeight.Medium, maxLines = 1,
            modifier = Modifier.graphicsLayer {
                rotationZ = settings.labelRotation
                translationX = settings.labelOffsetX * density
                translationY = settings.labelOffsetY * density
            })
        Text(item.third.ifBlank { "—" }, fontSize = valueFontSize * settings.valueScale,
            fontFamily = family, fontWeight = FontWeight.SemiBold, maxLines = 2,
            modifier = Modifier.graphicsLayer {
                scaleX = settings.valueScale
                scaleY = settings.valueScale
                rotationZ = settings.valueRotation
                translationX = settings.valueOffsetX * density
                translationY = settings.valueOffsetY * density
            })
    }
}

@Composable
internal fun UnifiedCredentialCard(
    documentKey: String,
    title: String,
    subtitle: String,
    holderName: String,
    fields: List<Triple<String, String, String>>,
    settings: LegitimacyPreviewSettings,
    photo: Bitmap? = null,
    fallbackBackgroundRes: Int = R.drawable.tlo_legitymacji_szare,
    assetRevision: Int = 0,
    validity: Boolean? = null
) {
    val context = LocalContext.current
    var showQr by remember(documentKey) { mutableStateOf(false) }
    var showInfo by remember(documentKey) { mutableStateOf(false) }
    val horizontal = settings.orientation == "POZIOMA"
    val background = remember(settings.backgroundAssetId, documentKey, assetRevision, title, fallbackBackgroundRes) {
        settings.backgroundAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, documentKey, title, "Tło dokumentu")
            ?: if (fallbackBackgroundRes != R.drawable.tlo_legitymacji_szare) {
                BitmapFactory.decodeResource(context.resources, fallbackBackgroundRes)
            } else {
                defaultDocumentBackground(context, title)
                    ?: BitmapFactory.decodeResource(context.resources, fallbackBackgroundRes)
            }
    }
    val logo = remember(settings.logoAssetId, documentKey, assetRevision, title) {
        settings.logoAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, documentKey, title, "Logo")
    }
    val visible = if (!settings.visibleFieldsConfigured) fields else fields.filter {
        it.first in settings.visibleFields
    }
    val sharePayload = remember(documentKey, holderName, fields) {
        buildString {
            append("OWD-CREDENTIAL|1\n")
            append("DOCUMENT=").append(documentKey).append('\n')
            append("HOLDER=").append(holderName).append('\n')
            fields.filter { it.third.isNotBlank() }.forEach { (key, label, value) ->
                append(key).append('=').append(label).append(':').append(value).append('\n')
            }
        }
    }
    val qrBitmap = remember(sharePayload) {
        runCatching {
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 2
            )
            val matrix = QRCodeWriter().encode(sharePayload, BarcodeFormat.QR_CODE, 480, 480, hints)
            Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }.getOrNull()
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(if (horizontal) 1.586f else 0.70f)) {
                val cardWidth = maxWidth
                val horizontalPadding = (cardWidth * 0.05f).coerceIn(8.dp, 20.dp)
                val verticalPadding = (cardWidth * 0.05f).coerceIn(8.dp, 20.dp)
                val photoWidth = (cardWidth * if (horizontal) 0.31f else 0.33f).coerceAtLeast(46.dp)
                val photoHeight = photoWidth * (45f / 35f)
                val fieldLabelSize = (cardWidth.value * 0.023f).coerceIn(7f, 14f).sp
                val fieldValueSize = (cardWidth.value * 0.030f).coerceIn(8f, 18f).sp
                val holderSize = (cardWidth.value * 0.041f).coerceIn(11f, 24f).sp
                Box(Modifier.fillMaxSize()) {
                    if (settings.showBackground && background != null) {
                        Image(
                            background.asImageBitmap(),
                            contentDescription = "Tło dokumentu",
                            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = settings.graphicScale; scaleY = settings.graphicScale; rotationZ = settings.graphicRotation; translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density },
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            alpha = 0.48f
                        )
                    }
                    Column(
                        Modifier.fillMaxSize().padding(horizontalPadding, verticalPadding),
                        verticalArrangement = Arrangement.spacedBy((cardWidth * 0.018f).coerceIn(3.dp, 7.dp))
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    title.uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (cardWidth.value * 0.038f).coerceIn(10f, 22f).sp,
                                    maxLines = 2
                                )
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        subtitle,
                                        fontSize = (cardWidth.value * 0.027f).coerceIn(8f, 14f).sp,
                                        maxLines = 2
                                    )
                                }
                            }
                            if (settings.showLogo && logo != null) {
                                Image(
                                    logo.asImageBitmap(),
                                    contentDescription = "Logo wystawcy",
                                    modifier = Modifier.size((cardWidth * 0.13f).coerceIn(30.dp, 46.dp)).graphicsLayer { scaleX = settings.graphicScale; scaleY = settings.graphicScale; rotationZ = settings.graphicRotation; translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density },
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy((cardWidth * 0.025f).coerceIn(5.dp, 10.dp))
                        ) {
                            Column(
                                Modifier.width(photoWidth),
                                verticalArrangement = Arrangement.spacedBy((cardWidth * 0.012f).coerceIn(3.dp, 6.dp)),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (settings.showPhoto) {
                                    Box(
                                        Modifier.fillMaxWidth()
                                            .height(photoHeight)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photo != null) {
                                            Image(
                                                photo.asImageBitmap(),
                                                contentDescription = "Fotografia posiadacza",
                                                modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = settings.graphicScale; scaleY = settings.graphicScale; rotationZ = settings.graphicRotation; translationX = settings.graphicOffsetX * density; translationY = settings.graphicOffsetY * density },
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Text("FOTO", fontSize = fieldLabelSize)
                                        }
                                    }
                                }
                                LegitimacyHeraldry(
                                    settings = settings,
                                    documentKey = documentKey,
                                    vertical = !horizontal,
                                    assetRevision = assetRevision,
                                    title = title
                                )
                            }
                            Column(
                                Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy((cardWidth * 0.01f).coerceIn(2.dp, 5.dp))
                            ) {
                                Text(
                                    holderName.ifBlank { "IMIĘ I NAZWISKO" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = holderSize * settings.valueScale,
                                    fontFamily = when (settings.fontFamily) { "serif" -> FontFamily.Serif; "mono" -> FontFamily.Monospace; "cursive" -> FontFamily.Cursive; else -> FontFamily.SansSerif },
                                    modifier = Modifier.graphicsLayer { rotationZ = settings.valueRotation; translationX = settings.valueOffsetX * density; translationY = settings.valueOffsetY * density },
                                    maxLines = 2
                                )
                                if (horizontal) {
                                    Row(
                                        Modifier.fillMaxWidth().weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy((cardWidth * 0.02f).coerceIn(4.dp, 10.dp))
                                    ) {
                                        Column(
                                            Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            visible.filterIndexed { index, _ -> index % 2 == 0 }.forEach {
                                                CredentialFieldValue(it, fieldLabelSize, fieldValueSize, settings)
                                            }
                                        }
                                        Column(
                                            Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            visible.filterIndexed { index, _ -> index % 2 == 1 }.forEach {
                                                CredentialFieldValue(it, fieldLabelSize, fieldValueSize)
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy((cardWidth * 0.01f).coerceIn(2.dp, 5.dp))
                                    ) {
                                        visible.forEach { CredentialFieldValue(it, fieldLabelSize, fieldValueSize) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (validity != null) {
                val statusColor = if (validity) Color(0xFF237A35) else Color(0xFFB3261E)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = if (validity) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = if (validity) "Dokument ważny" else "Dokument nieważny",
                        tint = statusColor,
                        modifier = Modifier.size(25.dp)
                    )
                    Text(
                        if (validity) "Dokument ważny" else "Dokument nieważny",
                        fontSize = 14.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showQr = true }) {
                        Icon(Icons.Filled.QrCode2, contentDescription = "Udostępnij dane legitymacji — pokaż kod QR")
                    }
                    IconButton(onClick = {
                        runCatching {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, sharePayload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Udostępnij dane legitymacji"))
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Udostępnij dane legitymacji")
                    }
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Informacje o danych legitymacji")
                    }
                }
            }
        }
    }
    if (showQr) {
        AlertDialog(
            onDismissRequest = { showQr = false },
            title = { Text("Kod QR danych legitymacji") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (qrBitmap != null) {
                        Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR zawierający dane legitymacji",
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                    } else {
                        Text("Nie udało się utworzyć kodu QR. Ogranicz liczbę lub długość pól dokumentu.")
                    }
                    Text("Kod zawiera widoczne dane tej legitymacji i można go odczytać zgodnym skanerem QR. To lokalny zapis danych, a nie urzędowe potwierdzenie dokumentu.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showQr = false }) { Text("Zamknij") } }
        )
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Informacje o legitymacji") },
            text = {
                Text("Status ważności jest wyliczany lokalnie z wprowadzonych danych i terminu ważności. Kod QR oraz udostępniany opis zawierają dane zapisane w tej aplikacji; same w sobie nie potwierdzają autentyczności ani umocowania wystawcy.")
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Rozumiem") } }
        )
    }
}

@Composable
internal fun CredentialValidityTile(valid: Boolean) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (valid) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = if (valid) "Dokument ważny" else "Dokument nieważny",
                tint = if (valid) Color(0xFF238548) else Color(0xFFB3261E),
                modifier = Modifier.size(30.dp)
            )
            Text(
                if (valid) "Dokument ważny" else "Dokument nieważny",
                fontSize = 15.sp,
                color = if (valid) Color(0xFF237A35) else Color(0xFFB3261E),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
internal fun CredentialTemplatePreview(documentKey: String, settings: LegitimacyPreviewSettings, assetRevision: Int = 0) {
    val title = documentBrandTargets.firstOrNull { it.first == documentKey }?.second ?: "Dokument"
    val options = credentialFieldOptions(documentKey)
    val selected = if (!settings.visibleFieldsConfigured) options else options.filter { it.first in settings.visibleFields }
    UnifiedCredentialCard(
        documentKey = documentKey,
        title = title,
        subtitle = "",
        holderName = "IMIĘ I NAZWISKO",
        fields = selected.map { (key, label) -> Triple(key, label, "—") },
        settings = settings,
        assetRevision = assetRevision
    )
}

@Composable
internal fun LegitimacyReverseSide(title: String, issuer: String, number: String, validity: String, orientation: String = "PIONOWA") {
    val payload = remember(title, issuer, number, validity) {
        "OWD|${title.take(80)}|${number.take(48)}|${validity.take(32)}"
    }
    val qrBitmap = remember(payload) {
        runCatching {
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 240, 240, hints)
            Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }.getOrNull()
    }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(if (orientation == "POZIOMA") 1.586f else 0.70f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("REWERS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Text(title, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            Text("Wystawca: ${issuer.ifBlank { "Nie wskazano" }}")
            Text("Numer dokumentu: ${number.ifBlank { "—" }}")
            Text("Termin ważności: ${validity.ifBlank { "Nie określono" }}")
            Spacer(Modifier.weight(1f))
            if (qrBitmap != null) {
                Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR rewersu",
                    modifier = Modifier.size(168.dp).align(Alignment.CenterHorizontally))
            }
        }
    }
}


@Composable
internal fun CredentialAppearanceDefaultsScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do zakładki Więcej", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Domyślny wygląd legitymacji", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Ustaw osobny wzór dla każdego rodzaju legitymacji. W panelu wybierz orientację pionową lub poziomą, a następnie dostosuj grafiki, położenie, obrót, skalę i krój pisma. Zmiany są zapisywane lokalnie.",
            style = MaterialTheme.typography.bodySmall)
        CredentialAppearancePreset("student", "Legitymacja ucznia / studenta")
        CredentialAppearancePreset("training", "Legitymacja uczestnika kursu")
        CredentialAppearancePreset("parliament", "Legitymacja poselska")
        CredentialAppearancePreset("church_bishop", "Legitymacja / dokument kościelny")
        CredentialAppearancePreset("professional_medical", "Legitymacja zawodowa — medyczna")
        CredentialAppearancePreset("identity", "Dokument tożsamości")
        CredentialAppearancePreset("passport", "Paszport")
    }
}

@Composable
private fun CredentialAppearancePreset(documentKey: String, title: String) {
    val state = rememberLegitimacyPreviewSettings(documentKey)
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            LegitimacyPreviewSettingsPanel(
                documentKey = documentKey,
                settings = state.value,
                onChange = { state.value = it }
            )
        }
    }
}
