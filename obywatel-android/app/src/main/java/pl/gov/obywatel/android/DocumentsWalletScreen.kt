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
internal fun DocumentsWalletScreen(
    onBack: () -> Unit,
    onAddDocuments: () -> Unit,
    documentPlacements: Map<String, String>,
    documentCardStyle: String,
    defaultDocumentIcon: String,
    documentIconOverrides: Map<String, String>,
    documentGroups: List<String>,
    documentGroupAssignments: Map<String, String>,
    onOpenDetails: (String) -> Unit,
    onEditDocument: (String) -> Unit,
    onOpenService: (String) -> Unit
) {
    val context = LocalContext.current
    data class CatalogDocument(
        val id: String,
        val title: String,
        val subtitle: String,
        val categories: List<String>,
        val detailsIndex: String? = null,
        val service: String? = null,
        val icon: ImageVector = Icons.Filled.Description
    )

    val catalogGroupOrder = (documentGroups + "Inne dokumenty").distinct()
    val documents = documentDisplayOptions.distinctBy { it.first }.map { (id, title, _) ->
        val group = documentGroupAssignments[id] ?: documentVisibilityGroup(id)
        val service = when {
            id == "financial_service" -> "Służba finansowa"
            id == "bank_accounts" || id == "rod_bank_accounts" || id == "entity_bank_accounts" -> "Konta bankowe"
            id == "imported" -> "Dokumenty własne — pliki"
            id == "signed_pdf" -> "Dokumenty PDF i podpisy elektroniczne"
            id == "receipts" -> "Rachunki"
            id == "branding" -> "Zasoby identyfikacji dokumentów"
            id == "children" -> "Dane dzieci"
            id == "vaccines" -> "eKarta Szczepień"
            id == "church" || id.startsWith("church_") || id == "organization_church" -> "Dokumenty Kościelne"
            id == "professional" || id.startsWith("professional_") -> "Dokumenty kwalifikacji zawodowych"
            id == "business" || id == "entity" || id.startsWith("organization_") || id.startsWith("entity_") -> "Dokumenty rejestru podmiotów"
            id == "civil" || id == "official" || id in setOf("birth_extract", "marriage_extract", "death_extract") -> "Formularze dokumentów"
            else -> "Pozostałe dokumenty"
        }
        val icon = when (group) {
            "Służba kościelna" -> Icons.Filled.AccountBalance
            "Służba rodowa" -> Icons.Filled.Fingerprint
            "Służba gospodarcza" -> Icons.Filled.Business
            "Służba zawodowa" -> Icons.Filled.Badge
            "Służba zdrowotna" -> Icons.Filled.HealthAndSafety
            "Zasoby i dokumenty własne" -> Icons.Filled.Description
            "Służba cywilna" -> Icons.Filled.Description
            "Służba zbrojna" -> Icons.Filled.Shield
            "Służba finansowa" -> Icons.Filled.Payments
            "Służba państwowa" -> Icons.Filled.Public
            else -> Icons.Filled.CreditCard
        }
        CatalogDocument(
            id = id,
            title = title,
            subtitle = "Dane dokumentu i ewidencja: $title",
            categories = listOf(group),
            service = service,
            icon = icon
        )
    }
    val allDocuments = documents
    val visibleDocuments = allDocuments.filter { documentPlacements[it.id] != "hidden" }
    val categories = catalogGroupOrder

    fun openDocument(document: CatalogDocument) {
        onOpenDetails(document.id)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Pulpit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Dokumenty", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Wybierz pozycję, aby otworzyć podgląd dokumentu. Polecenie „Edytuj” otwiera powiązany formularz; obie czynności dotyczą tego samego identyfikatora dokumentu.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
        )
        categories.forEach { category ->
            val entries = if (category == "Inne dokumenty") {
                visibleDocuments.filter { documentPlacements[it.id] == "other" }
            } else {
                visibleDocuments.filter { documentPlacements[it.id] == "dashboard" && category in it.categories }
            }
            if (entries.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (category == "Służba zbrojna") {
                        Image(painterResource(R.drawable.tile_ic_armed_service_outline),
                            contentDescription = null, modifier = Modifier.size(34.dp))
                    }
                    Text(category, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 5.dp))
                }
                if (documentCardStyle == "list") {
                    entries.forEach { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { openDocument(document) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (documentPlacements[document.id] == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                DocumentTileArtwork(document.id, Modifier.matchParentSize())
                                Row(
                                    Modifier.fillMaxWidth().padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                                ) {
                                    Image(
                                        painter = painterResource(documentTileIconResource(context, document.id, resolveDocumentIconResource(context, document.id, defaultDocumentIcon, documentIconOverrides))),
                                        contentDescription = document.title,
                                        modifier = Modifier.size(30.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                    )
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(document.title, color = Color.White, fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text(document.subtitle, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = { onEditDocument(document.id) }) { Text("Edytuj") }
                                }
                            }
                        }
                    }
                } else {
                    val columns = if (documentCardStyle == "small") 3 else 2
                    entries.chunked(columns).forEach { rowDocuments ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowDocuments.forEach { document ->
                                Card(
                                    modifier = Modifier.weight(1f)
                                        .height(if (documentCardStyle == "small") 112.dp else 148.dp)
                                        .clickable { openDocument(document) },
                                    shape = RoundedCornerShape(13.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, if (documentPlacements[document.id] == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Box(Modifier.fillMaxSize()) {
                                        DocumentTileArtwork(document.id, Modifier.fillMaxSize())
                                        Text(
                                            document.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            style = if (documentCardStyle == "small") MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.TopStart)
                                                .fillMaxWidth(0.78f).padding(10.dp)
                                        )
                                        if (documentCardStyle == "large") {
                                            Text(
                                                document.subtitle,
                                                color = Color.White.copy(alpha = 0.84f),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                                            )
                                        }
                                        Image(
                                            painter = painterResource(documentTileIconResource(context, document.id, resolveDocumentIconResource(context, document.id, defaultDocumentIcon, documentIconOverrides))),
                                            contentDescription = "Ikona wektorowa: " + document.title,
                                            modifier = Modifier.align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(if (documentCardStyle == "small") 22.dp else 26.dp),
                                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                        )
                                        TextButton(
                                            onClick = { onEditDocument(document.id) },
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                        ) { Text("Edytuj") }
                                    }
                                }
                            }
                            repeat(columns - rowDocuments.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAddDocuments, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = painterResource(R.drawable.lib_ic_plus), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Dodaj dokument")
        }
    }
}


