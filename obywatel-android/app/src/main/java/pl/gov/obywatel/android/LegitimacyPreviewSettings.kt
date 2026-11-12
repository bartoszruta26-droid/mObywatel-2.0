// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Visual parity: checkmark scan frame and horizontal gradient identity-card shortcut.
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

/** Resolves a document-specific status/expiry pair without treating missing data as proof of validity. */
internal fun credentialValidityForDocument(documentKey: String, values: Map<String, String>): Boolean? {
    val statusKeys = when (documentKey) {
        "identity" -> listOf("idStatus", "status")
        "passport" -> listOf("passportStatus", "status")
        "parliament" -> listOf("mpStatus", "status")
        "training" -> listOf("trainingStatus", "status")
        "student" -> listOf("studentStatus", "status")
        "disability" -> listOf("disabilityStatus", "status")
        "professional", "professional_medical", "professional_education", "professional_therapeutic", "professional_other" -> listOf("professionalStatus", "status")
        "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister", "church_missionary" -> listOf("churchStatus", "status")
        "family", "organization_family" -> listOf("familyStatus", "status")
        "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business" -> listOf("insuranceStatus", "status")
        "mission_travel" -> listOf("missionStatus", "status")
        "ehic" -> listOf("ehicStatus", "status")
        "vaccines" -> listOf("vaccineStatus", "status")
        "loyalty", "loyalty_cards" -> listOf("loyaltyStatus", "status")
        "bank_accounts" -> listOf("accountStatus", "status")
        "children" -> listOf("childStatus", "status")
        "civil", "birth_extract", "marriage_extract", "death_extract" -> listOf("civilStatus", "status")
        "entity", "business", "registry", "organization_foundation", "organization_company", "organization_association" -> listOf("registryStatus", "entityStatus", "status")
        else -> listOf("status")
    }
    val expiryKeys = when (documentKey) {
        "identity" -> listOf("idExpiryDate", "expiryDate")
        "passport" -> listOf("passportExpiryDate", "expiryDate")
        "parliament" -> listOf("mpEnd", "expiryDate")
        "training" -> listOf("trainingValidUntil", "expiryDate")
        "student" -> listOf("studentValidity", "expiryDate")
        "disability" -> listOf("disabilityValidity", "expiryDate")
        "professional", "professional_medical", "professional_education", "professional_therapeutic", "professional_other" -> listOf("professionalValidity", "expiryDate")
        "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot", "church_minister", "church_missionary" -> listOf("churchValidity", "expiryDate")
        "family", "organization_family" -> listOf("familyExpiryDate", "expiryDate")
        "large_family" -> listOf("largeFamilyExpiry", "expiryDate")
        "disability_card" -> listOf("disabilityCardExpiry", "expiryDate")
        "vehicle_registration" -> listOf("vehicleInspectionDue", "expiryDate")
        "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business" -> listOf("insuranceEnd", "expiryDate")
        "mission_travel" -> listOf("missionEnd", "expiryDate")
        "ehic" -> listOf("ehicExpiry", "expiryDate")
        "vaccines" -> listOf("vaccineExpiry", "expiryDate")
        "loyalty", "loyalty_cards" -> listOf("loyaltyExpiry", "expiryDate")
        "bank_accounts" -> listOf("accountExpiryDate", "expiryDate")
        "children" -> listOf("childDocumentExpiry", "expiryDate")
        "civil", "birth_extract", "marriage_extract", "death_extract" -> listOf("civilExpiryDate", "expiryDate")
        "entity", "business", "registry", "organization_foundation", "organization_company", "organization_association" -> listOf("registryExpiryDate", "entityExpiryDate", "expiryDate")
        else -> listOf("expiryDate", "validUntil", "documentExpiryDate")
    }
    val status = statusKeys.firstNotNullOfOrNull { key -> values[key]?.trim()?.takeIf { it.isNotEmpty() } }
        ?: values.entries.firstOrNull { (key, value) -> key.endsWith("Status", true) && value.isNotBlank() }?.value.orEmpty()
    val expiry = expiryKeys.firstNotNullOfOrNull { key -> values[key]?.trim()?.takeIf { it.isNotEmpty() } }
        ?: values.entries.firstOrNull { (key, value) ->
            value.isNotBlank() && (key.contains("Expiry", true) || key.endsWith("ValidUntil", true) ||
                key.endsWith("Validity", true) || key.endsWith("ExpiryDate", true))
        }?.value.orEmpty()
    if (status.isBlank() && expiry.isBlank()) return null
    val normalized = status.lowercase(java.util.Locale.forLanguageTag("pl-PL"))
    val explicitlyInvalid = listOf(
        "nieważ", "unieważ", "utrac", "zwróc", "wygas", "zawies", "nieaktywn", "zablok", "zamknięt",
        "expired", "invalid", "revoked", "cancelled"
    ).any(normalized::contains)
    return !explicitlyInvalid && isCredentialValidByExpiry(expiry)
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
    val crestScale: Float = 1f,
    val crestRotation: Float = 0f,
    val crestOffsetX: Float = 0f,
    val crestOffsetY: Float = 0f,
    val flagScale: Float = 1f,
    val flagRotation: Float = 0f,
    val flagOffsetX: Float = 0f,
    val flagOffsetY: Float = 0f,
    val labelScale: Float = 1f,
    val labelRotation: Float = 0f,
    val labelOffsetX: Float = 0f,
    val labelOffsetY: Float = 0f,
    val valueScale: Float = 1f,
    val valueRotation: Float = 0f,
    val valueOffsetX: Float = 0f,
    val valueOffsetY: Float = 0f,
    val fontFamily: String = "sans",
    val fontColor: String = "#FF000000",
    val imageFilter: String = "none",
    val individualAppearanceConfigured: Boolean = false
)

private const val GLOBAL_LEGITIMACY_APPEARANCE_KEY = "credential_template_global"

private const val BUILTIN_BACKGROUND_PREFIX = "builtin:"
private val builtInCredentialBackgrounds = listOf(
    "file_000000000480820ab196baceb9149967" to "Wzór firmowy 01",
    "file_0000000009b8820abb055566e7a0d510" to "Wzór firmowy 02",
    "file_000000000fcc820a9f7e3c368750a9f5" to "Wzór firmowy 03",
    "file_00000000311c8246ad9be95179179050" to "Wzór firmowy 04",
    "file_0000000035e081f4a0a7990b576e5398" to "Wzór firmowy 05",
    "file_0000000041a881f4a930bd827168a8fa" to "Wzór firmowy 06",
    "file_00000000532082108d561dcd2385f8f9" to "Wzór firmowy 07",
    "file_000000005f44820abb5e05d54fcff000" to "Wzór firmowy 08",
    "file_0000000064c88210af96e02cb0511406" to "Wzór firmowy 09",
    "file_0000000068bc82108afd8c3f9a7876bf" to "Wzór firmowy 10",
    "file_0000000073d88210a0cf298c8b74878b" to "Wzór firmowy 11",
    "file_000000007db0820a9085e7c796d4701a" to "Wzór firmowy 12",
    "file_000000007f3c820a942cfb69396276d6" to "Wzór firmowy 13",
    "file_00000000818c8210b6fe5da7d5c5f9f1" to "Wzór firmowy 14",
    "file_000000008608820c90a96b72e9bb83ac" to "Wzór firmowy 15",
    "file_0000000088f0821083e4160489338d4d" to "Wzór firmowy 16",
    "file_0000000089ac81f690fed3eb6a132e54" to "Wzór firmowy 17",
    "file_0000000091148210832c52e04697ec21" to "Wzór firmowy 18",
    "file_00000000924c820abedccdc4a7f1d5be" to "Wzór firmowy 19",
    "file_0000000092f082109e9799ec12a74c01" to "Wzór firmowy 20",
    "file_000000009c9881f4815513656695564d" to "Wzór firmowy 21",
    "file_000000009d10820a995d6315d156b72e" to "Wzór firmowy 22",
    "file_00000000a704820a841a6bda2c3223cb" to "Wzór firmowy 23",
    "file_00000000b02082109be48ee61b0e7c96" to "Wzór firmowy 24",
    "file_00000000b0cc81f69235793665bc9eff" to "Wzór firmowy 25",
    "file_00000000c0f081f5990d08be15bbf139" to "Wzór firmowy 26",
    "file_00000000d43881f6b46d4ea66bdeb1fd" to "Wzór firmowy 27",
    "file_00000000d588820a9b141a8f2f8df7c5" to "Wzór firmowy 28",
    "file_00000000e0e4820a839fa3184993f41b" to "Wzór firmowy 29",
    "file_00000000e6d88210b265ef420ff089cd" to "Wzór firmowy 30",
    "file_00000000e8948210a70bd85e83a2e205" to "Wzór firmowy 31",
    "file_00000000e92482108cb3b56796451794" to "Wzór firmowy 32",
    "file_00000000f5b0820a9499b70ab02da04b" to "Wzór firmowy 33"
)

internal fun credentialBackgroundResourceId(context: Context, assetId: String): Int? {
    val resourceName = assetId.takeIf { it.startsWith(BUILTIN_BACKGROUND_PREFIX) }
        ?.removePrefix(BUILTIN_BACKGROUND_PREFIX) ?: return null
    return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        .takeIf { it != 0 }
}


internal fun mergeSharedLegitimacyAppearance(
    document: LegitimacyPreviewSettings,
    shared: LegitimacyPreviewSettings
): LegitimacyPreviewSettings {
    // Wspólny układ i geometria są nadrzędne dla każdej legitymacji.
    // Indywidualne ustawienia zachowują przypisane zasoby i ewentualny kolor tekstu.
    return document.copy(
        showCard = shared.showCard,
        showReverse = shared.showReverse,
        orientation = shared.orientation,
        showPhoto = shared.showPhoto,
        showFlag = shared.showFlag,
        showCrest = shared.showCrest,
        showLogo = shared.showLogo,
        showBackground = shared.showBackground,
        graphicScale = shared.graphicScale,
        graphicRotation = shared.graphicRotation,
        graphicOffsetX = shared.graphicOffsetX,
        graphicOffsetY = shared.graphicOffsetY,
        crestScale = shared.crestScale,
        crestRotation = shared.crestRotation,
        crestOffsetX = shared.crestOffsetX,
        crestOffsetY = shared.crestOffsetY,
        flagScale = shared.flagScale,
        flagRotation = shared.flagRotation,
        flagOffsetX = shared.flagOffsetX,
        flagOffsetY = shared.flagOffsetY,
        labelScale = shared.labelScale,
        labelRotation = shared.labelRotation,
        labelOffsetX = shared.labelOffsetX,
        labelOffsetY = shared.labelOffsetY,
        valueScale = shared.valueScale,
        valueRotation = shared.valueRotation,
        valueOffsetX = shared.valueOffsetX,
        valueOffsetY = shared.valueOffsetY,
        fontFamily = shared.fontFamily,
        fontColor = document.fontColor.ifBlank { shared.fontColor },
        imageFilter = shared.imageFilter
    )
}

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
            crestScale = p.getFloat("${key}_crest_scale", p.getFloat("${key}_graphic_scale", 1f)),
            crestRotation = p.getFloat("${key}_crest_rotation", p.getFloat("${key}_graphic_rotation", 0f)),
            crestOffsetX = p.getFloat("${key}_crest_x", p.getFloat("${key}_graphic_x", 0f)),
            crestOffsetY = p.getFloat("${key}_crest_y", p.getFloat("${key}_graphic_y", 0f)),
            flagScale = p.getFloat("${key}_flag_scale", p.getFloat("${key}_graphic_scale", 1f)),
            flagRotation = p.getFloat("${key}_flag_rotation", p.getFloat("${key}_graphic_rotation", 0f)),
            flagOffsetX = p.getFloat("${key}_flag_x", 0f),
            flagOffsetY = p.getFloat("${key}_flag_y", 0f),
            labelScale = p.getFloat("${key}_label_scale", 1f),
            labelRotation = p.getFloat("${key}_label_rotation", 0f),
            labelOffsetX = p.getFloat("${key}_label_x", 0f),
            labelOffsetY = p.getFloat("${key}_label_y", 0f),
            valueScale = p.getFloat("${key}_value_scale", 1f),
            valueRotation = p.getFloat("${key}_value_rotation", 0f),
            valueOffsetX = p.getFloat("${key}_value_x", 0f),
            valueOffsetY = p.getFloat("${key}_value_y", 0f),
            fontFamily = p.getString("${key}_font_family", "sans").orEmpty(),
            fontColor = p.getString("${key}_font_color", if (key == GLOBAL_LEGITIMACY_APPEARANCE_KEY) "#FF000000" else "").orEmpty(),
            imageFilter = p.getString("${key}_image_filter", "none").orEmpty(),
            individualAppearanceConfigured = p.getBoolean("${key}_individual_appearance_configured", false)
        )
    }

    fun save(context: Context, key: String, value: LegitimacyPreviewSettings) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (key == GLOBAL_LEGITIMACY_APPEARANCE_KEY) {
            // Jeden globalny zestaw parametrów geometrii i typografii wzoru.
            editor.putBoolean("${key}_show_card", value.showCard)
                .putBoolean("${key}_show_reverse", value.showReverse)
                .putString("${key}_orientation", value.orientation)
                .putBoolean("${key}_show_photo", value.showPhoto)
                .putBoolean("${key}_show_flag", value.showFlag)
                .putBoolean("${key}_show_crest", value.showCrest)
                .putBoolean("${key}_show_logo", value.showLogo)
                .putBoolean("${key}_show_background", value.showBackground)
                .putFloat("${key}_graphic_scale", value.graphicScale)
                .putFloat("${key}_graphic_rotation", value.graphicRotation)
                .putFloat("${key}_graphic_x", value.graphicOffsetX)
                .putFloat("${key}_graphic_y", value.graphicOffsetY)
                .putFloat("${key}_crest_scale", value.crestScale)
                .putFloat("${key}_crest_rotation", value.crestRotation)
                .putFloat("${key}_crest_x", value.crestOffsetX)
                .putFloat("${key}_crest_y", value.crestOffsetY)
                .putFloat("${key}_flag_scale", value.flagScale)
                .putFloat("${key}_flag_rotation", value.flagRotation)
                .putFloat("${key}_flag_x", value.flagOffsetX)
                .putFloat("${key}_flag_y", value.flagOffsetY)
                .putFloat("${key}_label_scale", value.labelScale)
                .putFloat("${key}_label_rotation", value.labelRotation)
                .putFloat("${key}_label_x", value.labelOffsetX)
                .putFloat("${key}_label_y", value.labelOffsetY)
                .putFloat("${key}_value_scale", value.valueScale)
                .putFloat("${key}_value_rotation", value.valueRotation)
                .putFloat("${key}_value_x", value.valueOffsetX)
                .putFloat("${key}_value_y", value.valueOffsetY)
                .putString("${key}_font_family", value.fontFamily)
                .putString("${key}_font_color", value.fontColor)
                .putString("${key}_image_filter", value.imageFilter)
            listOf("crest", "flag", "background", "logo", "caption", "caption_position").forEach {
                editor.remove("${key}_$it")
            }
        } else {
            // W ustawieniach dokumentu zapisujemy wyłącznie przypisane mu zasoby.
            editor.putString("${key}_crest", value.crestAssetId)
                .putString("${key}_flag", value.flagAssetId)
                .putString("${key}_background", value.backgroundAssetId)
                .putString("${key}_logo", value.logoAssetId)
                .putString("${key}_caption", value.crestCaption)
                .putString("${key}_caption_position", value.captionPosition)
                .putStringSet("${key}_visible_fields", value.visibleFields)
                .putBoolean("${key}_visible_fields_configured", value.visibleFieldsConfigured)
                .putString("${key}_font_color", value.fontColor)
                .putBoolean("${key}_individual_appearance_configured", value.individualAppearanceConfigured)
                .putBoolean("${key}_show_card", value.showCard)
                .putBoolean("${key}_show_reverse", value.showReverse)
                .putString("${key}_orientation", value.orientation)
                .putBoolean("${key}_show_photo", value.showPhoto)
                .putBoolean("${key}_show_flag", value.showFlag)
                .putBoolean("${key}_show_crest", value.showCrest)
                .putBoolean("${key}_show_logo", value.showLogo)
                .putBoolean("${key}_show_background", value.showBackground)
                .putFloat("${key}_graphic_scale", value.graphicScale)
                .putFloat("${key}_graphic_rotation", value.graphicRotation)
                .putFloat("${key}_graphic_x", value.graphicOffsetX)
                .putFloat("${key}_graphic_y", value.graphicOffsetY)
                .putFloat("${key}_crest_scale", value.crestScale)
                .putFloat("${key}_crest_rotation", value.crestRotation)
                .putFloat("${key}_crest_x", value.crestOffsetX)
                .putFloat("${key}_crest_y", value.crestOffsetY)
                .putFloat("${key}_flag_scale", value.flagScale)
                .putFloat("${key}_flag_rotation", value.flagRotation)
                .putFloat("${key}_flag_x", value.flagOffsetX)
                .putFloat("${key}_flag_y", value.flagOffsetY)
                .putFloat("${key}_label_scale", value.labelScale)
                .putFloat("${key}_label_rotation", value.labelRotation)
                .putFloat("${key}_label_x", value.labelOffsetX)
                .putFloat("${key}_label_y", value.labelOffsetY)
                .putFloat("${key}_value_scale", value.valueScale)
                .putFloat("${key}_value_rotation", value.valueRotation)
                .putFloat("${key}_value_x", value.valueOffsetX)
                .putFloat("${key}_value_y", value.valueOffsetY)
                .putString("${key}_font_family", value.fontFamily)
                .putString("${key}_image_filter", value.imageFilter)
            // Parametry wzoru indywidualnego są zapisywane powyżej i nie mogą być usuwane.
        }
        check(editor.commit()) { "Nie udało się zapisać ustawień wyglądu legitymacji." }
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
    val state = remember(documentKey) {
        mutableStateOf(
            if (documentKey == GLOBAL_LEGITIMACY_APPEARANCE_KEY) {
                LegitimacyPreviewSettingsStore.load(context, documentKey)
            } else {
                mergeSharedLegitimacyAppearance(
                    LegitimacyPreviewSettingsStore.load(context, documentKey),
                    LegitimacyPreviewSettingsStore.load(context, GLOBAL_LEGITIMACY_APPEARANCE_KEY)
                )
            }
        )
    }
    return state
}

@Composable
internal fun LegitimacyPreviewSettingsPanel(
    documentKey: String,
    settings: LegitimacyPreviewSettings,
    onChange: (LegitimacyPreviewSettings) -> Unit,
    assetRevision: Int = 0,
    assetSelectionOnly: Boolean = false
) {
    val context = LocalContext.current
    var individualFontColor by remember(documentKey) { mutableStateOf(LegitimacyPreviewSettingsStore.load(context, documentKey).fontColor) }
    var assets by remember(documentKey) { mutableStateOf(DocumentBrandStore.list(context)) }
    var crestMenu by remember { mutableStateOf(false) }
    var flagMenu by remember { mutableStateOf(false) }
    var backgroundMenu by remember { mutableStateOf(false) }
    var logoMenu by remember { mutableStateOf(false) }
    var positionMenu by remember { mutableStateOf(false) }
    var importKind by remember { mutableStateOf("Herb") }
    var message by remember { mutableStateOf("") }

    fun update(value: LegitimacyPreviewSettings) {
        val persisted = if (assetSelectionOnly) value.copy(fontColor = individualFontColor)
            else if (documentKey != GLOBAL_LEGITIMACY_APPEARANCE_KEY) value.copy(individualAppearanceConfigured = true)
            else value
        LegitimacyPreviewSettingsStore.save(context, documentKey, persisted)
        val effective = if (assetSelectionOnly && individualFontColor.isBlank()) {
            value.copy(fontColor = LegitimacyPreviewSettingsStore.load(context, GLOBAL_LEGITIMACY_APPEARANCE_KEY).fontColor)
        } else if (assetSelectionOnly) value.copy(fontColor = individualFontColor) else persisted
        onChange(effective)
    }

    fun updateFontColor(color: String) {
        if (assetSelectionOnly) individualFontColor = color
        update(settings.copy(fontColor = color))
    }

    fun selectOrientation(target: String) {
        update(settings.copy(orientation = target))
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
            Text(
                when {
                    assetSelectionOnly -> "Grafiki przypisane do tego dokumentu"
                    documentKey == GLOBAL_LEGITIMACY_APPEARANCE_KEY -> "Wspólny wygląd wszystkich legitymacji"
                    else -> "Indywidualny wygląd legitymacji"
                },
                fontWeight = FontWeight.Bold
            )
            if (!assetSelectionOnly) {
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
            Text(if (assetSelectionOnly) "Indywidualny kolor czcionki" else "Domyślny kolor czcionki wszystkich legitymacji", fontWeight = FontWeight.SemiBold)
            if (assetSelectionOnly) {
                FilterChip(selected = individualFontColor.isBlank(), onClick = { updateFontColor("") }, label = { Text("Kolor wspólny") })
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "#FF000000" to "Czarny", "#FF263238" to "Grafit", "#FF17365D" to "Granat",
                    "#FF174A35" to "Zieleń", "#FF7A1F2B" to "Bordo", "#FF6B4423" to "Brąz",
                    "#FF1565C0" to "Niebieski", "#FF5E35B1" to "Fiolet"
                ).forEach { (hex, label) ->
                    val selectedColor = if (assetSelectionOnly) individualFontColor else settings.fontColor
                    FilterChip(selected = selectedColor.equals(hex, true), onClick = { updateFontColor(hex) }, label = { Text(label) })
                }
            }
            Text("Kolor wspólny jest domyślnie czarny. W ustawieniach dokumentu możesz wybrać kolor indywidualny albo wrócić do koloru wspólnego.", style = MaterialTheme.typography.bodySmall)
            Text("Filtr fotografii i godła", fontWeight = FontWeight.SemiBold)
            listOf(
                "none" to "Oryginał / brak filtra",
                "grayscale" to "Czerń i biel",
                "sepia" to "Sepia"
            ).forEach { (value, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                        .clickable { update(settings.copy(imageFilter = value)) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = settings.imageFilter == value, onClick = {
                        update(settings.copy(imageFilter = value))
                    })
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Filtr dotyczy wyłącznie fotografii posiadacza i godła / herbu. Pozostałe grafiki zachowują oryginalne kolory.",
                style = MaterialTheme.typography.bodySmall)
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
            }
            if (assetSelectionOnly || documentKey != GLOBAL_LEGITIMACY_APPEARANCE_KEY) {
            Text("Wybierz herb, flagę, tło i logo niezależnie dla tego dokumentu.", style = MaterialTheme.typography.bodySmall)
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
                    val builtInLabel = builtInCredentialBackgrounds.firstOrNull {
                        settings.backgroundAssetId == BUILTIN_BACKGROUND_PREFIX + it.first
                    }?.second
                    Text(
                        assets.firstOrNull { it.id == settings.backgroundAssetId }?.label
                            ?: builtInLabel
                            ?: "Wybierz tło z katalogu"
                    )
                }
                DropdownMenu(expanded = backgroundMenu, onDismissRequest = { backgroundMenu = false }) {
                    DropdownMenuItem(text = { Text("Domyślne tło") }, onClick = { update(settings.copy(backgroundAssetId = "")); backgroundMenu = false })
                    builtInCredentialBackgrounds.forEach { (resourceName, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            update(settings.copy(backgroundAssetId = BUILTIN_BACKGROUND_PREFIX + resourceName))
                            backgroundMenu = false
                        })
                    }
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
            }
            if (!assetSelectionOnly) {
            Text("Godło / herb — położenie i rozmiar", fontWeight = FontWeight.SemiBold)
            Text("Skala godła: ${"%.2f".format(settings.crestScale)}")
            Slider(settings.crestScale, { update(settings.copy(crestScale = it)) }, valueRange = 0.25f..3f)
            Text("Obrót godła: ${settings.crestRotation.toInt()}°")
            Slider(settings.crestRotation, { update(settings.copy(crestRotation = it)) }, valueRange = -180f..180f)
            Text("Przesunięcie poziome godła: ${settings.crestOffsetX.toInt()} dp")
            Slider(settings.crestOffsetX, { update(settings.copy(crestOffsetX = it)) }, valueRange = -60f..60f)
            Text("Przesunięcie pionowe godła: ${settings.crestOffsetY.toInt()} dp")
            Slider(settings.crestOffsetY, { update(settings.copy(crestOffsetY = it)) }, valueRange = -60f..60f)

            Text("Flaga — położenie i rozmiar", fontWeight = FontWeight.SemiBold)
            Text("Skala flagi: ${"%.2f".format(settings.flagScale)}")
            Slider(settings.flagScale, { update(settings.copy(flagScale = it)) }, valueRange = 0.25f..3f)
            Text("Obrót flagi: ${settings.flagRotation.toInt()}°")
            Slider(settings.flagRotation, { update(settings.copy(flagRotation = it)) }, valueRange = -180f..180f)
            Text("Przesunięcie poziome flagi: ${settings.flagOffsetX.toInt()} dp")
            Slider(settings.flagOffsetX, { update(settings.copy(flagOffsetX = it)) }, valueRange = -60f..60f)
            Text("Przesunięcie pionowe flagi: ${settings.flagOffsetY.toInt()} dp")
            Slider(settings.flagOffsetY, { update(settings.copy(flagOffsetY = it)) }, valueRange = -60f..60f)
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
                    graphicOffsetY = 0f, crestScale = 1f, crestRotation = 0f, crestOffsetX = 0f,
                    crestOffsetY = 0f, flagScale = 1f, flagRotation = 0f, flagOffsetX = 0f,
                    flagOffsetY = 0f, labelScale = 1f, labelRotation = 0f, labelOffsetX = 0f,
                    labelOffsetY = 0f, valueScale = 1f, valueRotation = 0f, valueOffsetX = 0f,
                    valueOffsetY = 0f, fontFamily = "sans"))
            }, modifier = Modifier.fillMaxWidth()) { Text("Przywróć domyślne przekształcenia") }
            CredentialTemplatePreview(documentKey, settings, assetRevision)
            Text("Wspólny wzór jest stosowany do wszystkich legitymacji. Zasoby graficzne pozostają przypisane do wybranego dokumentu.", style = MaterialTheme.typography.labelSmall)
            } else {
                Text("Przypisanie grafiki dotyczy wyłącznie tego dokumentu i nie zmienia pozostałych legitymacji.", style = MaterialTheme.typography.labelSmall)
            }
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
    val imageFilter = remember(settings.imageFilter) { credentialImageColorFilter(settings.imageFilter) }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (settings.showFlag && selectedFlag != null) {
            Image(
                selectedFlag.asImageBitmap(),
                contentDescription = "Flaga dokumentu — wyrównana do lewego marginesu zdjęcia i godła",
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {
                        // Flaga jest elementem kolumny zdjęcia: pozostaje przy jej lewym
                        // marginesie i nie dziedziczy przesunięć używanych dla godła.
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                        scaleX = settings.flagScale
                        scaleY = settings.flagScale
                        rotationZ = settings.flagRotation
                        translationX = settings.flagOffsetX * density
                        translationY = settings.flagOffsetY * density
                    },
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
                    modifier = Modifier.size(68.dp).graphicsLayer {
                        scaleX = settings.crestScale
                        scaleY = settings.crestScale
                        rotationZ = settings.crestRotation
                        translationX = settings.crestOffsetX * density
                        translationY = settings.crestOffsetY * density
                    },
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    colorFilter = imageFilter
                )
            }
            if (settings.showCrest) Spacer(Modifier.width(6.dp))
            if (settings.showCrest || caption.isNotBlank()) {
                Text(
                    caption,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = credentialFontColor(settings.fontColor),
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
    val reservedGap = (
        4f +
            kotlin.math.abs(settings.valueOffsetY - settings.labelOffsetY) +
            (valueFontSize.value * (settings.valueScale - 1f).coerceAtLeast(0f)) +
            (labelFontSize.value * (settings.labelScale - 1f).coerceAtLeast(0f))
        ).coerceIn(4f, 96f).dp
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(reservedGap)) {
        Text(item.third.ifBlank { "—" }, modifier = Modifier.fillMaxWidth().graphicsLayer {
                rotationZ = settings.valueRotation
                translationX = settings.valueOffsetX * density
                translationY = settings.valueOffsetY * density
            },
            fontSize = valueFontSize * settings.valueScale,
            lineHeight = valueFontSize * settings.valueScale * 1.2f,
            fontFamily = family,
            color = if (settings.fontColor.isBlank()) MaterialTheme.colorScheme.onSurface else credentialFontColor(settings.fontColor),
            fontWeight = FontWeight.SemiBold, maxLines = 2, softWrap = true,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Text(item.second, modifier = Modifier.fillMaxWidth().graphicsLayer {
                rotationZ = settings.labelRotation
                translationX = settings.labelOffsetX * density
                translationY = settings.labelOffsetY * density
            },
            fontSize = labelFontSize * settings.labelScale,
            lineHeight = labelFontSize * settings.labelScale * 1.2f,
            fontFamily = family,
            color = if (settings.fontColor.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else credentialFontColor(settings.fontColor),
            fontWeight = FontWeight.Medium, maxLines = 2, softWrap = true,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

private fun credentialFontColor(value: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(value.ifBlank { "#FF000000" }))
}.getOrDefault(Color.Black)

private fun credentialImageColorFilter(mode: String): ColorFilter? {
    val matrix = when (mode) {
        "grayscale" -> ColorMatrix().apply { setToSaturation(0f) }
        "sepia" -> ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        else -> return null
    }
    return ColorFilter.colorMatrix(matrix)
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
    var showIdentityDetails by remember(documentKey) { mutableStateOf(false) }
    var showShortcuts by remember(documentKey) { mutableStateOf(false) }
    var animationEnabled by remember(documentKey) { mutableStateOf(false) }
    val animationRotation by animateFloatAsState(
        targetValue = if (animationEnabled) 7f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "credential-card-animation"
    )
    val imageFilter = remember(settings.imageFilter) { credentialImageColorFilter(settings.imageFilter) }
    val horizontal = settings.orientation == "POZIOMA"
    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    val background = remember(settings.backgroundAssetId, documentKey, assetRevision, title, fallbackBackgroundRes) {
        settings.backgroundAssetId.takeIf { it.isNotBlank() }?.let { selected ->
            credentialBackgroundResourceId(context, selected)?.let { BitmapFactory.decodeResource(context.resources, it) }
                ?: DocumentBrandStore.preview(context, selected)
        }
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
    val visible = if (!settings.visibleFieldsConfigured || settings.visibleFields.isEmpty()) fields else fields.filter {
        it.first in settings.visibleFields
    }
    val sharePayload = remember(documentKey, holderName, visible) {
        buildString {
            append("OWD-CREDENTIAL|1\n")
            append("DOCUMENT=").append(documentKey).append('\n')
            append("HOLDER=").append(holderName).append('\n')
            visible.filter { it.third.isNotBlank() }.forEach { (key, label, value) ->
                append(key).append('=').append(label).append(':').append(value).append('\n')
            }
        }
    }
    val applicationAppearance = LocalApplicationAppearance.current
    val qrBitmap = remember(sharePayload, applicationAppearance.headerEmblem) {
        runCatching {
            // Używamy dokładnie tego samego zasobu co godło w lewym górnym rogu nagłówka.
            val emblem = BitmapFactory.decodeResource(
                context.resources,
                applicationDrawable(
                    context,
                    applicationAppearance.headerEmblem,
                    R.drawable.file_00000000532082108d561dcd2385f8f9
                )
            )
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )
            val matrix = QRCodeWriter().encode(sharePayload, BarcodeFormat.QR_CODE, 480, 480, hints)
            Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
                if (emblem != null) {
                    val canvas = Canvas(bitmap)
                    // Niewielkie godło (7% szerokości) z białym marginesem ochronnym.
                    // Korekcja H zapewnia większą odporność na uszkodzenia kodu.
                    val emblemSide = matrix.width * 0.07f
                    val padding = matrix.width * 0.018f
                    val boxSide = emblemSide + padding * 2f
                    val left = (matrix.width - boxSide) / 2f
                    val top = (matrix.height - boxSide) / 2f
                    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(
                        RectF(left, top, left + boxSide, top + boxSide),
                        boxSide * 0.14f,
                        boxSide * 0.14f,
                        backgroundPaint
                    )
                    val scale = emblemSide / maxOf(emblem.width, emblem.height).toFloat()
                    val drawWidth = emblem.width * scale
                    val drawHeight = emblem.height * scale
                    val destination = RectF(
                        (matrix.width - drawWidth) / 2f,
                        (matrix.height - drawHeight) / 2f,
                        (matrix.width + drawWidth) / 2f,
                        (matrix.height + drawHeight) / 2f
                    )
                    canvas.drawBitmap(
                        emblem,
                        null,
                        destination,
                        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    )
                    emblem.recycle()
                }
            }
        }.getOrNull()
    }

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            rotationY = animationRotation
            cameraDistance = 24f * density
        },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        // Granica wzoru graficznego obejmuje kartę wraz z paskiem statusu ważności.
        // Przycisk animacji i trzy okrągłe skróty pozostają poza tą powierzchnią.
        BoxWithConstraints(
            Modifier.fillMaxWidth()
                .aspectRatio(if (horizontal) 1.586f else 0.68f)
                .clip(cardShape)
        ) {
            val cardWidth = maxWidth
            val cardHeight = maxHeight
            val footerHeight = if (horizontal) cardHeight * 0.18f else cardHeight * 0.155f
            val bodyHeight = cardHeight - footerHeight
            val horizontalPadding = (cardWidth * 0.055f).coerceIn(9.dp, 22.dp)
            val verticalPadding = (bodyHeight * 0.045f).coerceIn(8.dp, 18.dp)
            val photoWidth = (cardWidth * if (horizontal) 0.27f else 0.37f).coerceAtLeast(54.dp)
            val photoHeight = photoWidth * 1.36f
            val fieldLabelSize = (cardWidth.value * 0.032f).coerceIn(9f, 13f).sp
            val fieldValueSize = (cardWidth.value * 0.044f).coerceIn(12f, 17f).sp
            val leftColumnWidth = photoWidth + (cardWidth * 0.045f)
            Box(Modifier.fillMaxSize()) {
                // Awers: jasne, zabezpieczone tło i dwie wyraźne kolumny.
                Box(
                    Modifier.fillMaxWidth().height(bodyHeight)
                ) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    if (settings.showBackground && background != null) {
                        Image(
                            background.asImageBitmap(),
                            contentDescription = "Tło dokumentu",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alpha = 0.88f
                        )
                    }
                    Row(
                        Modifier.fillMaxSize().padding(
                            horizontal = horizontalPadding,
                            vertical = verticalPadding
                        ),
                        horizontalArrangement = Arrangement.spacedBy((cardWidth * 0.035f).coerceIn(7.dp, 13.dp)),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            Modifier.width(leftColumnWidth).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy((cardWidth * 0.05f).coerceIn(14.dp, 20.dp)),
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (settings.showPhoto) {
                                val photoFrameShape = androidx.compose.foundation.shape.RoundedCornerShape(9.dp)
                                Box(
                                    Modifier.width(photoWidth)
                                        .height(photoHeight.coerceAtMost(bodyHeight * 0.57f))
                                        .shadow(4.dp, photoFrameShape)
                                        .clip(photoFrameShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFFFFFFF),
                                                    Color(0xFFE5E5E5),
                                                    Color(0xFFB8B8B8),
                                                    Color(0xFFF5F5F5),
                                                    Color(0xFF9E9E9E)
                                                )
                                            )
                                        )
                                        .padding(3.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE9E6E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photo != null) {
                                        Image(
                                            photo.asImageBitmap(),
                                            contentDescription = "Fotografia posiadacza",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            colorFilter = imageFilter
                                        )
                                    } else {
                                        Text("FOTOGRAFIA", fontSize = fieldLabelSize, color = Color(0xFF59616A))
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
                            verticalArrangement = Arrangement.spacedBy((cardWidth * 0.018f).coerceIn(4.dp, 8.dp))
                        ) {
                            if (settings.showLogo && logo != null) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Image(
                                        logo.asImageBitmap(),
                                        contentDescription = "Logo wystawcy",
                                        modifier = Modifier.size((cardWidth * 0.10f).coerceIn(26.dp, 38.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            }
                            if (horizontal) {
                                Row(
                                    Modifier.fillMaxWidth().weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(
                                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        visible.filterIndexed { index, _ -> index % 2 == 0 }.forEach {
                                            CredentialFieldValue(it, fieldLabelSize, fieldValueSize, settings)
                                        }
                                    }
                                    Column(
                                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        visible.filterIndexed { index, _ -> index % 2 == 1 }.forEach {
                                            CredentialFieldValue(it, fieldLabelSize, fieldValueSize, settings)
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy((cardWidth * 0.018f).coerceIn(4.dp, 8.dp))
                                ) {
                                    visible.forEach {
                                        CredentialFieldValue(it, fieldLabelSize, fieldValueSize, settings)
                                    }
                                }
                            }
                        }
                    }
                }

                // Pasek statusu stanowi integralną część karty.
                val statusColor = when (validity) {
                    true -> Color(0xFF43D65F)
                    false -> Color(0xFFFF6B6B)
                    null -> Color(0xFFFFC857)
                }
                val statusLabel = when (validity) {
                    true -> "Dokument ważny"
                    false -> "Dokument nieważny"
                    null -> "Ważność dokumentu nieustalona"
                }
                Row(
                    Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(footerHeight)
                        .background(Color(0xFF414047))
                        .padding(horizontal = horizontalPadding, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when (validity) {
                            true -> Icons.Filled.CheckCircle
                            false -> Icons.Filled.Error
                            null -> Icons.Filled.Info
                        },
                        contentDescription = statusLabel,
                        tint = statusColor,
                        modifier = Modifier.size((cardWidth * 0.052f).coerceIn(22.dp, 30.dp))
                    )
                    Text(
                        statusLabel,
                        fontSize = (cardWidth.value * 0.030f).coerceIn(12f, 16f).sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        Button(
            onClick = { animationEnabled = !animationEnabled },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2879D8)),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Text(if (animationEnabled) "Wyłącz animację" else "Włącz animację",
                color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                CredentialRoundAction(
                    label = "Udostępnij\nkod QR",
                    icon = {
                        androidx.compose.foundation.Canvas(Modifier.size(30.dp)) {
                            val c = Color(0xFFB9E9FF)
                            val w = 2.2.dp.toPx()
                            val a = size.minDimension * 0.28f
                            val p = 2.dp.toPx()
                            // Narożniki ramki skanowania i ptaszek w jej środku.
                            drawLine(c, androidx.compose.ui.geometry.Offset(p,p), androidx.compose.ui.geometry.Offset(p+a,p), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(p,p), androidx.compose.ui.geometry.Offset(p,p+a), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(size.width-p-a,p), androidx.compose.ui.geometry.Offset(size.width-p,p), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(size.width-p,p), androidx.compose.ui.geometry.Offset(size.width-p,p+a), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(p,size.height-p-a), androidx.compose.ui.geometry.Offset(p,size.height-p), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(p,size.height-p), androidx.compose.ui.geometry.Offset(p+a,size.height-p), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(size.width-p-a,size.height-p), androidx.compose.ui.geometry.Offset(size.width-p,size.height-p), w)
                            drawLine(c, androidx.compose.ui.geometry.Offset(size.width-p,size.height-p-a), androidx.compose.ui.geometry.Offset(size.width-p,size.height-p), w)
                            val check = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width*0.29f, size.height*0.52f)
                                lineTo(size.width*0.44f, size.height*0.67f)
                                lineTo(size.width*0.73f, size.height*0.35f)
                            }
                            drawPath(check, c, style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            ))
                        }
                    },
                    onClick = { showQr = true }
                )
                CredentialRoundAction(
                    label = "Dane\ndokumentu",
                    icon = {
                        androidx.compose.foundation.Canvas(Modifier.size(width = 36.dp, height = 25.dp)) {
                            val cardBrush = Brush.horizontalGradient(
                                listOf(Color(0xFFE8A8E8), Color(0xFFD8A5F2), Color(0xFFB6B8F5))
                            )
                            drawRoundRect(
                                brush = cardBrush,
                                topLeft = androidx.compose.ui.geometry.Offset(1.dp.toPx(), 2.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(size.width - 2.dp.toPx(), size.height - 4.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                            )
                            // Złota kropka wewnątrz karty, przy lewym górnym rogu.
                            drawCircle(
                                color = Color(0xFFFFD45A),
                                radius = 2.3.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(6.dp.toPx(), 7.dp.toPx())
                            )
                        }
                    },
                    onClick = { showIdentityDetails = true }
                )
                CredentialRoundAction(
                    label = "Opcje\ndodatkowe",
                    icon = { Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color(0xFFB9E9FF), modifier = Modifier.size(29.dp)) },
                    onClick = { showShortcuts = true }
                )
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
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        runCatching {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, sharePayload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Udostępnij dane kodu QR"))
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Udostępnij")
                    }
                    TextButton(onClick = { showQr = false }) { Text("Zamknij") }
                }
            }
        )
    }
    if (showIdentityDetails) {
        AlertDialog(
            onDismissRequest = { showIdentityDetails = false },
            title = { Text("Dane dokumentu") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    visible.forEach { (_, label, value) ->
                        if (value.isNotBlank()) {
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showIdentityDetails = false }) { Text("Zamknij") } }
        )
    }
    if (showShortcuts) {
        AlertDialog(
            onDismissRequest = { showShortcuts = false },
            title = { Text("Pozostałe skróty") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showShortcuts = false
                        runCatching {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, sharePayload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Udostępnij dane legitymacji"))
                        }
                    }) { Icon(Icons.Filled.Share, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Udostępnij dane") }
                    TextButton(onClick = { showShortcuts = false; showInfo = true }) {
                        Icon(Icons.Filled.Info, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Informacje o dokumencie")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showShortcuts = false }) { Text("Zamknij") } }
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
private fun CredentialRoundAction(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(104.dp).clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            Modifier.size(66.dp).clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF747780)),
            contentAlignment = Alignment.Center
        ) { icon() }
        Text(label, color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
internal fun CredentialValidityTile(valid: Boolean?) {
    val label = when (valid) {
        true -> "Dokument ważny"
        false -> "Dokument nieważny"
        null -> "Ważność dokumentu nieustalona"
    }
    val tint = when (valid) {
        true -> Color(0xFF238548)
        false -> Color(0xFFB3261E)
        null -> Color(0xFF9A6700)
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when (valid) {
                    true -> Icons.Filled.CheckCircle
                    false -> Icons.Filled.Error
                    null -> Icons.Filled.Info
                },
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
            Text(label, fontSize = 15.sp, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun CredentialTemplatePreview(
    documentKey: String,
    settings: LegitimacyPreviewSettings,
    assetRevision: Int = 0,
    values: Map<String, String>? = null
) {
    val title = if (documentKey == GLOBAL_LEGITIMACY_APPEARANCE_KEY) "Wspólny wzór legitymacji"
        else documentBrandTargets.firstOrNull { it.first == documentKey }?.second ?: "Dokument"
    val options = credentialFieldOptions(documentKey)
    val selected = if (!settings.visibleFieldsConfigured || settings.visibleFields.isEmpty()) options else options.filter { it.first in settings.visibleFields }
    val holderName = values?.get("studentNames").orEmpty().ifBlank {
        values?.get("passportHolderNames").orEmpty().ifBlank {
            values?.get("disabilityPersonName").orEmpty().ifBlank {
                listOf(values?.get("firstNames").orEmpty(), values?.get("lastName").orEmpty())
                    .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "IMIĘ I NAZWISKO" }
            }
        }
    }
    val photo = remember(values?.get("photo")) {
        runCatching {
            val encoded = values?.get("photo").orEmpty()
            if (encoded.isBlank()) null else {
                val bytes = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.getOrNull()
    }
    if (settings.showCard) {
        UnifiedCredentialCard(
            documentKey = documentKey,
            title = title,
            subtitle = "",
            holderName = holderName,
            fields = selected.map { (key, label) -> Triple(key, label, values?.get(key).orEmpty().ifBlank { "—" }) },
            settings = settings,
            photo = photo,
            assetRevision = assetRevision,
            validity = values?.let { credentialValidityForDocument(documentKey, it) } ?: true
        )
    } else {
        Text("Podgląd legitymacji jest wyłączony.", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
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
        Text("Wspólny wygląd legitymacji", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Jeden wspólny wzór obowiązuje wszystkie legitymacje. Tutaj ustawiasz orientację, rozmieszczenie elementów, skalę, obrót i typografię. Herby, flagi, tła oraz logo wybierasz osobno w ustawieniach każdego dokumentu.",
            style = MaterialTheme.typography.bodySmall)
        CredentialAppearancePreset(GLOBAL_LEGITIMACY_APPEARANCE_KEY, "Wspólny wzór wszystkich legitymacji")
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
                onChange = { state.value = it },
                assetSelectionOnly = documentKey != GLOBAL_LEGITIMACY_APPEARANCE_KEY
            )
        }
    }
}
