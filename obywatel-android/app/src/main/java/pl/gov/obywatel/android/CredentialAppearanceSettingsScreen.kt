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

@Composable
internal fun CredentialAppearanceSettingsScreen(
    documentKey: String,
    onBack: () -> Unit,
    onOpenSharedAppearance: () -> Unit
) {
    val context = LocalContext.current
    val state = rememberLegitimacyPreviewSettings(documentKey)
    val options = remember(documentKey) { credentialFieldOptions(documentKey) }
    val savedValues = remember(documentKey) {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val documentPhotoKey = "credentialPhoto_$documentKey"
    var photoMessage by remember(documentKey) { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego zdjęcia.")
                require(bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024) {
                    "Zdjęcie przekracza limit 5 MB albo jest puste."
                }
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }.onSuccess { encoded ->
                savedValues[documentPhotoKey] = encoded
                DemoIdentityStore.save(context, savedValues.toMap())
                photoMessage = "Zapisano osobne zdjęcie dla dokumentu."
            }.onFailure { error ->
                photoMessage = error.localizedMessage ?: "Nie udało się wczytać zdjęcia."
            }
        }
    }
    val title = documentDisplayOptions.firstOrNull { it.first == documentKey }?.second
        ?: documentKey.replace('_', ' ').replaceFirstChar { it.uppercase() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do edycji dokumentu", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Wygląd podglądu — $title", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zaznacz pola, które mają być widoczne na karcie. Herby, flagi, logo, tło i kolor tekstu przypisujesz osobno do tego dokumentu. Orientacja, filtry zdjęcia, widoczność całej karty oraz rozmieszczenie i rozmiar elementów pozostają wspólne dla wszystkich legitymacji.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onOpenSharedAppearance, modifier = Modifier.fillMaxWidth()) {
            Text("Edytuj wspólny wzór wszystkich legitymacji")
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Miejsce wyświetlania każdego pola", fontWeight = FontWeight.Bold)
                Text("Wybierz osobno dla każdej danej: brak, awers albo rewers. Rewers pozostaje pusty, dopóki go nie włączysz.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                options.forEach { (key, label) ->
                    val side = credentialFieldSide(key, state.value.visibleFieldsConfigured, state.value.visibleFields, state.value.fieldSides)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        listOf(
                            CREDENTIAL_FIELD_HIDDEN to "Brak",
                            CREDENTIAL_FIELD_FRONT to "Awers",
                            CREDENTIAL_FIELD_BACK to "Rewers"
                        ).forEach { (target, caption) ->
                            Row(
                                Modifier.width(58.dp).clip(RoundedCornerShape(6.dp)).clickable {
                                    val updated = state.value.copy(
                                        fieldSides = state.value.fieldSides + (key to target),
                                        visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) state.value.visibleFields - key
                                            else state.value.visibleFields + key,
                                        visibleFieldsConfigured = true
                                    )
                                    LegitimacyPreviewSettingsStore.save(context, documentKey, updated)
                                    state.value = updated
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = side == target,
                                    onClick = {
                                        val updated = state.value.copy(
                                            fieldSides = state.value.fieldSides + (key to target),
                                            visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) state.value.visibleFields - key
                                                else state.value.visibleFields + key,
                                            visibleFieldsConfigured = true
                                        )
                                        LegitimacyPreviewSettingsStore.save(context, documentKey, updated)
                                        state.value = updated
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
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fotografia dokumentu", fontWeight = FontWeight.Bold)
                Text(
                    if (savedValues[documentPhotoKey].isNullOrBlank())
                        "Dla tego dokumentu używana jest fotografia profilu, dopóki nie wybierzesz osobnego zdjęcia."
                    else "Wybrano osobną fotografię dla tego dokumentu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (photoMessage.isNotBlank()) {
                    Text(photoMessage, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Wybierz zdjęcie dokumentu") }
                if (!savedValues[documentPhotoKey].isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            savedValues.remove(documentPhotoKey)
                            DemoIdentityStore.save(context, savedValues.toMap())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Użyj ponownie zdjęcia profilu") }
                }
            }
        }
        LegitimacyPreviewSettingsPanel(
            documentKey = documentKey,
            settings = state.value,
            onChange = { state.value = it },
            assetSelectionOnly = false
        )
        val previewValues = savedValues.toMap() + (
            "photo" to (savedValues[documentPhotoKey] ?: savedValues["photo"].orEmpty())
        )
        CredentialTemplatePreview(documentKey, state.value, values = previewValues)
    }
}
