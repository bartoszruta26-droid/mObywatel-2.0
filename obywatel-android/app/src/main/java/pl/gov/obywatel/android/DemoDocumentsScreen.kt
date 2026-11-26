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

@Composable
private fun OwdDocumentArtworkCard(
    imageRes: Int,
    title: String,
    subtitle: String,
    holder: String,
    details: String,
    privateRecord: Boolean,
    credentialFields: List<Triple<String, String, String>> = emptyList(),
    validity: Boolean? = null
) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = when {
        title.contains("rodow", true) -> "family"
        title.contains("tożsamo", true) || title.contains("tozsamos", true) -> "identity"
        title.contains("paszport", true) -> "passport"
        title.contains("niepełnospraw", true) || title.contains("orzeczen", true) -> "disability"
        title.contains("kościel", true) || title.contains("kosciel", true) -> "church"
        title.contains("cywil", true) -> "civil"
        title.contains("podmiot", true) || title.contains("firm", true) -> "entity"
        else -> "official"
    }
    val settings = rememberLegitimacyPreviewSettings(documentKey).value
    val resolvedValidity = validity ?: credentialValidityForDocument(
        documentKey, credentialFields.associate { it.first to it.third }
    )
    if (settings.showCard) {
        UnifiedCredentialCard(
            documentKey = documentKey, title = title, subtitle = subtitle, holderName = holder,
            fields = credentialFields.ifEmpty { listOf(Triple("details", "DANE DOKUMENTU", details)) }, settings = settings,
            fallbackBackgroundRes = imageRes,
            validity = resolvedValidity
        )
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
            Text("Źródła i informacje dodatkowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (populated.isEmpty()) {
                Text("Brak wpisów dotyczących źródła, podstawy, weryfikacji lub adnotacji.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                populated.forEach { (label, key) ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        Text(fields[key].orEmpty(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text("Oświadczenie użytkownika opisuje dane zgodnie z jego wiedzą oraz wskazaną podstawą i źródłem.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}



@Composable
internal fun DemoDocumentsScreen(
    categoryId: String = "all",
    onBack: () -> Unit,
    onOpenDocumentAppearance: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var photoBase64 by remember { mutableStateOf(fields["photo"] ?: "") }
    var selectedPhotoField by remember { mutableStateOf("photo") }
    var saveMessage by remember { mutableStateOf("") }
    val editorTitle = when (categoryId) {
        "profile" -> "Dane osobowe i zdjęcie profilowe"
        "family" -> "Dokument rodowy"
        "identity_card" -> "Dowód tożsamości"
        "parliament" -> "Legitymacja poselska"
        "training" -> "Legitymacja ucznia/studenta"
        "student" -> "Legitymacja ucznia/studenta"
        "disability" -> "Orzeczenia i zaświadczenia"
        "loyalty_cards" -> "Karty lojalnościowe"
        "passport" -> "Dane paszportowe"
        else -> if (categoryId.startsWith("catalog:")) {
            documentDisplayOptions.firstOrNull { it.first == categoryId.removePrefix("catalog:") }?.second ?: "Dane dokumentu"
        } else "Wszystkie dane i dokumenty"
    }
    val credentialKey = when (categoryId) {
        "identity_card" -> "identity"
        "profile" -> "profile"
        "family" -> "family"
        "parliament" -> "parliament"
        "training" -> "training"
        "student" -> "student"
        "disability" -> "disability"
        "passport" -> "passport"
        "loyalty_cards" -> "loyalty_cards"
        else -> if (categoryId.startsWith("catalog:")) categoryId.removePrefix("catalog:") else categoryId
    }
    val appearance = rememberLegitimacyPreviewSettings(credentialKey)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego zdjęcia")
            }.onSuccess { bytes ->
                if (bytes.size > 5 * 1024 * 1024) {
                    saveMessage = "Zdjęcie przekracza limit 5 MB. Wybierz mniejszy plik."
                } else {
                    val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    fields[selectedPhotoField] = encoded
                    if (selectedPhotoField == "photo") photoBase64 = encoded
                    saveMessage = "Zdjęcie zapisane w polu: $selectedPhotoField. Zapisz zmiany."
                }
            }.onFailure { saveMessage = "Nie udało się wczytać zdjęcia: ${it.localizedMessage ?: "błąd odczytu"}" }
        }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDocumentScreenMode provides DocumentScreenMode.EDITOR,
        LocalCredentialFieldVisibility provides if (categoryId == "all") null
            else CredentialFieldVisibilityBinding(credentialKey, appearance, fields)
    ) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "‹  Powrót do ustawień",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            editorTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (categoryId != "all") {
            GeneralDocumentOcrEditor(credentialKey, fields, context) { message ->
                saveMessage = message
                DemoIdentityStore.save(context, fields.toMap())
            }
            DocumentOriginalAttachments(credentialKey)
        }
        if (categoryId.startsWith("catalog:")) {
            val documentId = categoryId.removePrefix("catalog:")
            val definitions = documentEditorFieldDefinitions(documentId)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Dane właściwe dla dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    definitions.forEach { definition ->
                        DemoDataField("doc_${documentId}_${definition.key}", definition.label, fields, definition.placeholder)
                    }
                }
            }
        }
         if (categoryId == "all" || categoryId == "profile") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zdjęcie profilowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F3F6))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
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
                        else Text("Nie można wyświetlić zdjęcia", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(42.dp))
                            Text("MIEJSCE NA ZDJĘCIE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("Nie wybrano fotografii", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Wybierz zdjęcie", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { selectedPhotoField = "photo"; photoPicker.launch("image/*") }.padding(vertical = 8.dp))
                    if (photoBase64.isNotBlank()) Text("Usuń zdjęcie", color = Color(0xFF9B2C2C), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { photoBase64 = ""; fields["photo"] = "" }.padding(vertical = 8.dp))
                }
                Text("Pozostałe fotografie", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                listOf(
                    "photoAlternative1" to "Zdjęcie alternatywne 1",
                    "photoAlternative2" to "Zdjęcie alternatywne 2",
                    "wifePhoto" to "Zdjęcie żony",
                    "child1Photo" to "Zdjęcie dziecka 1",
                    "child2Photo" to "Zdjęcie dziecka 2",
                    "familyMember1Photo" to "Zdjęcie członka rodziny 1",
                    "familyMember2Photo" to "Zdjęcie członka rodziny 2"
                ).forEach { (photoKey, photoLabel) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(photoLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { selectedPhotoField = photoKey; photoPicker.launch("image/*") }) {
                            Text(if (fields[photoKey].isNullOrBlank()) "Dodaj" else "Zmień")
                        }
                        if (!fields[photoKey].isNullOrBlank()) {
                            TextButton(onClick = { fields[photoKey] = "" }) { Text("Usuń") }
                        }
                    }
                }
                Text("Dane osobowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rodowy dokument tożsamości — dane dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.documents_family_record,
                    title = "Dokument rodowy",
                    subtitle = "Ewidencja rodowa",
                    holder = listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Dane posiadacza" },
                    details = "Nr: ${fields["familyDocNumber"].orEmpty().ifBlank { "—" }} • Ród: ${fields["familyName"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("familyName", "NAZWA RODU", fields["familyName"].orEmpty()),
                        Triple("familyRole", "MIEJSCE W RODZIE", fields["familyRole"].orEmpty()),
                        Triple("familyDocNumber", "NUMER DOKUMENTU", fields["familyDocNumber"].orEmpty()),
                        Triple("familyIssueDate", "DATA SPORZĄDZENIA", fields["familyIssueDate"].orEmpty()),
                        Triple("familyExpiryDate", "DATA AKTUALIZACJI", fields["familyExpiryDate"].orEmpty())
                    )
                )
            }
        }
        }
        if (categoryId == "all" || categoryId == "identity_card") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Podstawowe dane identyfikacyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("birthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("birthPlace", "Miejsce urodzenia", fields)
                DemoDataField("citizenship", "Obywatelstwo", fields)
                DemoDataField("sex", "Płeć (dane deklarowane)", fields)
                DemoDataField("birthSurname", "Nazwisko rodowe", fields)
                DemoDataField("pesel", "Numer PESEL", fields)
                Text("Dane samego dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("idNumber", "Numer dokumentu tożsamości", fields)
                DemoDataField("idIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("idExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("idIssuer", "Organ wydający", fields)
                DemoDataField("idStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("idPreviousNumber", "Numer poprzedniego dokumentu (jeśli dotyczy)", fields)
                DemoDataField("idLossOrInvalidationDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dane identyfikacyjne i dokumentowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Uzupełnij dane zgodnie z dokumentem źródłowym, w tym numer, organ wydający i terminy ważności.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.karta_rodowa_jasna,
                    title = "Karta danych tożsamości",
                    subtitle = "Dane identyfikacyjne",
                    holder = listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Dane użytkownika" },
                    details = "Nr: ${fields["idNumber"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("firstNames", "IMIONA", fields["firstNames"].orEmpty()),
                        Triple("lastName", "NAZWISKO", fields["lastName"].orEmpty()),
                        Triple("birthDate", "DATA URODZENIA", fields["birthDate"].orEmpty()),
                        Triple("pesel", "PESEL", fields["pesel"].orEmpty()),
                        Triple("idNumber", "NUMER DOKUMENTU", fields["idNumber"].orEmpty()),
                        Triple("idIssueDate", "DATA WYDANIA", fields["idIssueDate"].orEmpty()),
                        Triple("idExpiryDate", "DATA WAŻNOŚCI", fields["idExpiryDate"].orEmpty())
                    ),
                    validity = when (fields["idStatus"].orEmpty().trim().lowercase()) {
                        "nieważny", "nieważna", "unieważniony", "unieważniona", "utracony", "utracona", "expired", "invalid" -> false
                        "ważny", "ważna", "aktywny", "aktywna", "valid" -> true
                        else -> fields["idExpiryDate"].orEmpty().takeIf { it.isNotBlank() }
                            ?.let(::isCredentialValidByExpiry)
                    }
                )
                AdditionalInformationSection("Źródła i weryfikacja danych identyfikacyjnych", fields, "idExtra", "Wskaż dokument źródłowy i datę jego sprawdzenia. Sam wpis użytkownika nie stanowi potwierdzenia urzędowego.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "parliament") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja poselska — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("mpCardNumber", "Numer ewidencyjny legitymacji", fields)
                DemoDataField("mpTerm", "Kadencja", fields)
                DemoDataField("mpDistrict", "Okręg wyborczy", fields)
                DemoDataField("mpStart", "Data rozpoczęcia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpEnd", "Data zakończenia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpCardIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("mpIssuer", "Organ / osoba wystawiająca", fields)
                DemoDataField("mpStatus", "Status legitymacji", fields, "aktywna / zwrócona / unieważniona")
                DemoDataField("mpMandateSource", "Źródło potwierdzenia mandatu", fields)
                Text("Dane dotyczące ślubowania — oświadczenie własne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("mpOathDate", "Data złożenia ślubowania", fields, "DD-MM-RRRR")
                DemoDataField("mpOathPlace", "Miejsce / okoliczności wypowiedzenia ślubowania", fields)
                DemoDataField("mpOathEvidence", "Źródło potwierdzenia (np. protokół, nagranie, publikacja)", fields)
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd cyfrowej legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    ParliamentaryCredentialPreview(fields)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja poselska", fields, "mpExtra", "Art. 104 Konstytucji RP określa wolny mandat poselski oraz obowiązek złożenia ślubowania przed Sejmem przed rozpoczęciem sprawowania mandatu. Odmowa ślubowania oznacza zrzeczenie się mandatu. Sam wpis ewidencyjny nie potwierdza uzyskania mandatu ani prawa do reprezentowania Sejmu.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "training") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja uczestnika kursu — instytucja szkoleniowa", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    SchoolLegitimacyPreview(fields, photoBase64, student = false)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "trainingExtra", "Legitymacja instytucji szkoleniowej potwierdza status w ewidencji wystawcy w granicach regulaminu i właściwej podstawy działania. Nie jest legitymacją studencką i sama nie nadaje statusu studenta uczelni.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "student") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja ucznia/studenta — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
                Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    SchoolLegitimacyPreview(fields, photoBase64, student = true)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "studentExtra", "")
            }
        }
        }
        if (categoryId == "loyalty_cards") {
            LoyaltyCardsEditor(fields = fields, context = context, saveMessage = { saveMessage = it })
        }
        if (categoryId == "all" || categoryId == "disability") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Orzeczenie o niepełnosprawności — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Źródło ustalenia / dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
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
                                .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                                .clickable { fields["disabilityAuthorityType"] = value }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(label, modifier = Modifier.padding(start = 9.dp), color = MaterialTheme.colorScheme.onSurface,
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
                        DemoDataField("disabilityLegalBasis", "Podstawa prawna / cel wydania", fields, "Ustawa z 27.08.1997 r. i przepisy wykonawcze")
                    }
                    "other_law" -> {
                        DemoDataField("disabilityLegalBasis", "Podstawa prawna / cel wydania", fields)
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
                        DemoDataField("disabilityLegalBasis", "Podstawa prawna / cel wydania", fields)
                    }
                    "self_declaration" -> {
                        DemoDataField("disabilitySelfDeclarationDate", "Data złożenia oświadczenia", fields, "DD-MM-RRRR")
                        DemoDataField("disabilitySelfDeclarationBasis", "Podstawa i cel oświadczenia", fields)
                        Text("Oświadczenie własne zapisuje deklarację osoby. Samo w sobie nie jest orzeczeniem ustawowym; jego znaczenie zależy od przepisu lub procedury, w której jest składane.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Dane osoby i rozstrzygnięcia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / rozstrzygnięcia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                DemoDataField("disabilityPersonName", "Osoba, której dotyczy orzeczenie", fields, "Imię i nazwisko")
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator ewidencyjny", fields)
                Text("Ustalony status / stopień", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityCategory", "Kategoria", fields, "np. osoba do 16 lat / osoba powyżej 16 lat")
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki / nie dotyczy")
                DemoDataField("disabilityCauseCodes", "Symbole przyczyn (maks. 3 w orzeczeniu powiatowym)", fields, "np. 05-R, 10-N")
                DemoDataField("disabilityValidity", "Okres, na jaki ustalono status", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data, od której ustalono niepełnosprawność", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityEmployment", "Wskazania dotyczące zatrudnienia", fields)
                DemoDataField("disabilityTraining", "Wskazania dotyczące szkolenia", fields)
                DemoDataField("disabilityTherapy", "Terapia zajęciowa / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Przedmioty ortopedyczne / środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Środowiskowy system wsparcia", fields)
                DemoDataField("disabilityParking", "Wskazania dotyczące karty parkingowej", fields)
                Text("Skład i podpisy", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
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
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    DisabilityDecisionPreview(fields)
                }
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
        if (categoryId == "all" || categoryId == "passport") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dane paszportowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("passportNumber", "Numer dokumentu paszportowego", fields)
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
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.paszport_rodowy_jasny,
                    title = "Dane paszportowe",
                    subtitle = "Dane dokumentu podróży",
                    holder = fields["passportHolderNames"].orEmpty().ifBlank { "Dane posiadacza" },
                    details = "Nr paszportu: ${fields["passportNumber"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("passportHolderNames", "IMIĘ I NAZWISKO", fields["passportHolderNames"].orEmpty()),
                        Triple("passportNumber", "NUMER PASZPORTU", fields["passportNumber"].orEmpty()),
                        Triple("passportCitizenship", "OBYWATELSTWO", fields["passportCitizenship"].orEmpty()),
                        Triple("birthDate", "DATA URODZENIA", fields["birthDate"].orEmpty()),
                        Triple("passportIssueDate", "DATA WYDANIA", fields["passportIssueDate"].orEmpty()),
                        Triple("passportExpiryDate", "DATA WAŻNOŚCI", fields["passportExpiryDate"].orEmpty())
                    )
                )
                AdditionalInformationSection("Informacje dodatkowe — dane paszportowe", fields, "passportExtra", "Jest to ewidencja danych wpisanych przez użytkownika, a nie dokument paszportowy ani jego cyfrowy odpowiednik. Nie odtwarzamy strefy MRZ, kodów ani zabezpieczeń dokumentu.")
            }
        }
        }
        if (categoryId != "all") {
            Text("Wygląd dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zapis dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Dane i zdjęcie są zabezpieczane przed nieuprawnionym dostępem.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.Button(
                    onClick = {
                        runCatching {
                            val snapshot = fields.toMutableMap()
                            snapshot["photo"] = photoBase64
                            DemoIdentityStore.save(context, snapshot)
                        }.onSuccess { saveMessage = "Zapisano i zabezpieczono dane oraz zdjęcie." }
                            .onFailure { saveMessage = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz dane") }
                if (saveMessage.isNotBlank()) Text(saveMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (categoryId != "all") {
            OutlinedButton(
                onClick = { onOpenDocumentAppearance(credentialKey) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Przejdź do edycji graficznej") }
            CredentialTemplatePreview(credentialKey, appearance.value, values = fields.toMap())
            DocumentPdfExportButton(editorTitle, fields.toMap(), documentType = credentialKey)
        }
        Text(
            "Dane są zabezpieczane z użyciem mechanizmów systemu Android. Zachowaj ostrożność: mogą zawierać informacje wrażliwe.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
    }
    }
}

