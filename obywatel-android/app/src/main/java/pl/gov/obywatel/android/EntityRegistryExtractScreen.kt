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
internal fun EntityRegistryExtractScreen(onBack: () -> Unit, editable: Boolean = false) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var selectedEntity by remember { mutableStateOf<String?>(null) }
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
        Text("‹  Menu główne", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Odpis z rejestru podmiotów", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(if (editable) "Wprowadzanie i aktualizacja danych podmiotów. Wybierz rodzaj podmiotu i uzupełnij dane na podstawie właściwego dokumentu lub rejestru." else "Podgląd zapisanych danych identyfikacyjnych i rejestrowych podmiotów. Wybierz kafelek, aby wyświetlić szczegóły.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Text(if (editable) "Wpisy są przechowywane lokalnie jako dane wprowadzone przez użytkownika. Podaj źródło i datę sprawdzenia." else "Dane zapisane w ustawieniach. W razie potrzeby porównaj je z właściwym rejestrem; aplikacja nie pobiera urzędowego odpisu.",
                Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
        }
        if (selectedEntity == null) {
            Text("Wybierz rodzaj podmiotu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { (key, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().height(142.dp).clickable { selectedEntity = key },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Otwórz dokument  ›", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val entityKey = selectedEntity!!
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹  Wszystkie rodzaje podmiotów", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { selectedEntity = null }.padding(vertical = 5.dp))
            }
            val entityTitle = sections.first { it.first == entityKey }.second
            val entityFields = fieldSets[entityKey].orEmpty()
            val entityValues = entityFields.associate { (key, label, _) -> label to fields[key].orEmpty() } +
                mapOf(
                    "Źródło danych / adres rejestru" to fields["registry_${entityKey}_source"].orEmpty(),
                    "Data sprawdzenia" to fields["registry_${entityKey}_checked"].orEmpty(),
                    "Data sporządzenia dokumentu" to fields["registry_${entityKey}_documentDate"].orEmpty()
                )
            Text(entityTitle, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!editable) {
                val previewSettings = rememberLegitimacyPreviewSettings("entity").value.copy(visibleFieldsConfigured = false)
                val previewFields = entityValues.entries.take(12).mapIndexed { index, entry ->
                    Triple("registryField_$index", entry.key.uppercase(), entry.value.ifBlank { "—" })
                }
                UnifiedCredentialCard(
                    documentKey = "entity",
                    title = entityTitle.uppercase(),
                    subtitle = "PODGLĄD DANYCH REJESTROWYCH",
                    holderName = entityFields.firstOrNull()?.let { fields[it.first].orEmpty() }.orEmpty().ifBlank { "DANE PODMIOTU" },
                    fields = previewFields,
                    settings = previewSettings
                )
            }
            entityFields.forEach { (key, label, placeholder) ->
                if (editable) DemoDataField(key, label, fields, placeholder)
                else RegistryReadOnlyField(label, fields[key])
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            Text("Źródło i kontrola aktualności", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (editable) {
                DemoDataField("registry_${entityKey}_source", "Źródło danych / adres rejestru", fields, "Adres strony lub nazwa dokumentu")
                DemoDataField("registry_${entityKey}_checked", "Data sprawdzenia", fields, "DD-MM-RRRR")
                DemoDataField("registry_${entityKey}_documentDate", "Data sporządzenia odpisu / dokumentu", fields, "DD-MM-RRRR")
                DemoDataField("registry_${entityKey}_notes", "Uwagi", fields, "Informacje uzupełniające")
                Button(onClick = {
                    runCatching { DemoIdentityStore.save(context, fields.toMap()) }
                        .onSuccess { status = "Zapisano i zabezpieczono dane." }
                        .onFailure { status = "Nie udało się zapisać danych: ${it.message}" }
                }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz dane podmiotu") }
                if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                RegistryReadOnlyField("Źródło danych / adres rejestru", fields["registry_${entityKey}_source"])
                RegistryReadOnlyField("Data sprawdzenia", fields["registry_${entityKey}_checked"])
                RegistryReadOnlyField("Data sporządzenia odpisu / dokumentu", fields["registry_${entityKey}_documentDate"])
                RegistryReadOnlyField("Uwagi", fields["registry_${entityKey}_notes"])
            }
            DocumentPdfExportButton(entityTitle, entityValues)
        }
    }
}

@Composable
private fun RegistryReadOnlyField(label: String, value: String?) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(value?.takeIf { it.isNotBlank() } ?: "Brak zapisanego wpisu", color = if (value.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (value.isNullOrBlank()) FontWeight.Normal else FontWeight.SemiBold)
    }
}
