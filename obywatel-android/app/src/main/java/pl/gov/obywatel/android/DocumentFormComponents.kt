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

internal fun documentTileIconResource(context: Context, documentKey: String, fallback: Int): Int {
    val selected = context.getSharedPreferences("legitimacy_preview_preferences", Context.MODE_PRIVATE)
        .getString("${documentKey}_tile_icon", "").orEmpty()
    if (selected.isNotBlank()) context.resources.getIdentifier(selected, "drawable", context.packageName)
        .takeIf { it != 0 }?.let { return it }
    return when (documentKey) {
    "training", "student" -> R.drawable.tile_ic_student_outline
    "disability", "disability_card" -> R.drawable.tile_ic_disability_outline
    "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot",
    "church_minister", "church_missionary", "organization_church" -> R.drawable.tile_ic_church_outline
    "civil", "birth_extract", "marriage_extract", "death_extract" -> R.drawable.tile_ic_civil_outline
    "business", "entity", "official", "organization_foundation", "organization_company",
    "organization_association" -> R.drawable.tile_ic_business_outline
    in armedServiceDocumentIds -> R.drawable.tile_ic_armed_service_outline
    // Każdy typ dokumentu otrzymuje wektorową ikonę, także gdy nie ma
    // dedykowanego piktogramu dla konkretnej kategorii.
    else -> R.drawable.tile_ic_civil_outline
    }
}

internal enum class DocumentScreenMode { PREVIEW, EDITOR }
internal val LocalDocumentScreenMode = androidx.compose.runtime.compositionLocalOf { DocumentScreenMode.PREVIEW }
internal data class CredentialFieldVisibilityBinding(
    val documentKey: String,
    val settings: MutableState<LegitimacyPreviewSettings>,
    val fields: MutableMap<String, String>
)
internal val LocalCredentialFieldVisibility = androidx.compose.runtime.compositionLocalOf<CredentialFieldVisibilityBinding?> { null }
internal val LocalDocumentOcrStageRendered = androidx.compose.runtime.compositionLocalOf { false }



@Composable
internal fun AdditionalInformationSection(
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
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, modifier = Modifier.weight(1f).padding(start = 9.dp),
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "Zwiń" else "Rozwiń", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                if (explanation.isNotBlank()) Text(explanation, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                DemoDataField("${prefix}Source", "Źródło danych / dokument źródłowy", fields)
                DemoDataField("${prefix}LegalBasis", "Podstawa prawna / regulaminowa", fields)
                DemoDataField("${prefix}VerifiedAt", "Data ostatniej weryfikacji danych", fields, "DD-MM-RRRR")
                DemoDataField("${prefix}VerifiedBy", "Podmiot / osoba potwierdzająca dane", fields)
                DemoDataField("${prefix}Notes", "Adnotacje i informacje uzupełniające", fields)
                Text(
                    "Informacje dodatkowe uzupełniają zapis danych o źródło, podstawę i datę weryfikacji.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
internal fun DemoDataField(
    key: String,
    label: String,
    fields: MutableMap<String, String>,
    placeholder: String = "Wpisz dane",
    storageKey: String = key
) {
    val binding = LocalCredentialFieldVisibility.current
    val settings = binding?.settings?.value
    val selectableKeys = binding?.let { credentialFieldOptions(it.documentKey).map { option -> option.first }.toSet() }.orEmpty()
    val side = settings?.let {
        credentialFieldSide(key, it.visibleFieldsConfigured, it.visibleFields, it.fieldSides)
    } ?: CREDENTIAL_FIELD_FRONT
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        OutlinedTextField(
            value = fields[storageKey] ?: "",
            onValueChange = { fields[storageKey] = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true
        )
        if (binding != null && binding.documentKey.isNotBlank() && key in selectableKeys) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    CREDENTIAL_FIELD_HIDDEN to "Brak",
                    CREDENTIAL_FIELD_FRONT to "Awers",
                    CREDENTIAL_FIELD_BACK to "Rewers"
                ).forEach { (target, caption) ->
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).clickable {
                            val currentSettings = binding.settings.value
                            val updated = currentSettings.copy(
                                fieldSides = currentSettings.fieldSides + (key to target),
                                visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) currentSettings.visibleFields - key
                                    else currentSettings.visibleFields + key,
                                visibleFieldsConfigured = true
                            )
                            LegitimacyPreviewSettingsStore.save(context, binding.documentKey, updated)
                            binding.settings.value = updated
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = side == target,
                            onClick = {
                                val currentSettings = binding.settings.value
                                val updated = currentSettings.copy(
                                    fieldSides = currentSettings.fieldSides + (key to target),
                                    visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) currentSettings.visibleFields - key
                                        else currentSettings.visibleFields + key,
                                    visibleFieldsConfigured = true
                                )
                                LegitimacyPreviewSettingsStore.save(context, binding.documentKey, updated)
                                binding.settings.value = updated
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(caption, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}
