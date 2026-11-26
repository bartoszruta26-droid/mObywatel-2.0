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

private fun documentTileWatermarkResource(documentKey: String): Int = when {
    // Sakralne i misyjne dokumenty Kościoła oraz wspólnot.
    documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") ->
        R.drawable.tile_watermark_sacred

    // Tożsamość, profil i dokumenty identyfikacyjne.
    documentKey in setOf("identity", "identity_card", "profile", "parliament") ->
        R.drawable.tile_watermark_identity

    // Paszporty, podróże, dokumenty zagraniczne i komunikacyjne.
    documentKey in setOf("passport", "mission_travel") ->
        R.drawable.tile_watermark_travel
    documentKey in setOf("vehicle_registration", "insurance_vehicle") ->
        R.drawable.tile_watermark_transport

    // Finanse, rachunki i ubezpieczenia.
    documentKey in setOf("bank_accounts", "insurance_business", "insurance_private") ||
        (documentKey.startsWith("insurance_") && documentKey != "insurance_professional") ->
        R.drawable.tile_watermark_finance

    // Dokumenty elektroniczne, importowane i karty handlowe.
    documentKey in setOf("signed_pdf", "imported", "branding", "loyalty", "loyalty_cards") ->
        R.drawable.tile_watermark_digital

    // Kwalifikacje, zawody, uprawnienia i dokumenty służbowe.
    documentKey in setOf("student", "training", "eu_education") ->
        R.drawable.tile_watermark_education

    // Zdrowie, opieka, szczepienia i orzeczenia, także kwalifikacje medyczne.
    documentKey in setOf("disability", "vaccines", "ehic", "blood_donor", "disability_card",
        "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") ->
        R.drawable.tile_watermark_medical

    // Kwalifikacje zawodowe i dokumenty uprawnień.
    documentKey.startsWith("professional_") || documentKey in setOf("professional", "insurance_professional") ->
        R.drawable.tile_watermark_professional

    // Rodzina, Ród i akty stanu cywilnego.
    documentKey.startsWith("rod_") || documentKey in setOf(
        "family", "children", "birth_extract", "marriage_extract", "death_extract",
        "large_family"
    ) ->
        R.drawable.tile_watermark_family

    // Rejestry, podmioty prawne, fundacje, stowarzyszenia i dokumenty urzędowe.
    documentKey.startsWith("organization_") || documentKey.startsWith("entity_") ||
        documentKey in setOf(
            "civil", "official", "registry", "business", "entity",
            "organization_foundation", "organization_company",
            "organization_association", "parliament", "criminal_record"
        ) ->
        R.drawable.tile_watermark_civic

    // Każdy pozostały typ ma czytelny, neutralny znak zabezpieczający.
    else -> R.drawable.tile_watermark_security
}

@Composable
internal fun DocumentTileArtwork(documentKey: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tilePreferences = context.getSharedPreferences("legitimacy_preview_preferences", Context.MODE_PRIVATE)
    val tileBackgroundAssetId = tilePreferences.getString("${documentKey}_tile_background", "").orEmpty()
    val tileVectorBackgroundAssetId = tilePreferences.getString("${documentKey}_tile_vector_background", null)
        ?.takeIf { it.isNotBlank() }
        ?: tilePreferences.getString(
            "credential_template_global_tile_vector_background",
            tilePreferences.getString("global_document_tile_vector_background", "")
        ).orEmpty()
    val tileVectorBackgroundBitmap = remember(documentKey, tileVectorBackgroundAssetId) { tileVectorBackgroundAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } }
    val tileBackgroundBitmap = remember(documentKey, tileBackgroundAssetId) {
        when {
            tileBackgroundAssetId.isBlank() -> null
            tileBackgroundAssetId.startsWith("builtin:") -> {
                val resourceName = tileBackgroundAssetId.removePrefix("builtin:")
                val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                resourceId.takeIf { it != 0 }?.let { BitmapFactory.decodeResource(context.resources, it) }
            }
            else -> DocumentBrandStore.preview(context, tileBackgroundAssetId)
        }
    }
    val defaultPalette = when {
        documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") ->
            listOf(Color(0xFF4A145F), Color(0xFF762B83), Color(0xFFB27BB5))
        documentKey.startsWith("rod_") || documentKey in setOf(
            "family", "children", "birth_extract", "marriage_extract", "death_extract",
            "large_family"
        ) -> listOf(Color(0xFF9B315D), Color(0xFFD45B83), Color(0xFFF0A0B4))
        documentKey in setOf("disability", "disability_card", "vaccines", "ehic", "blood_donor",
            "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") -> listOf(Color(0xFF064B35), Color(0xFF167A50), Color(0xFF71A878))
        documentKey in setOf("student", "training", "eu_education") ||
            documentKey.startsWith("professional_education") -> listOf(Color(0xFF25205F), Color(0xFF4D4696), Color(0xFF9994D0))
        documentKey in setOf("bank_accounts", "insurance_business", "insurance_vehicle", "insurance_professional", "insurance_private") ||
            documentKey.startsWith("insurance_") || documentKey.startsWith("entity_") ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey in setOf("vehicle_registration", "mission_travel", "passport") ->
            listOf(Color(0xFF176B83), Color(0xFF3C9FB0), Color(0xFF9AD5D4))
        documentKey in setOf("signed_pdf", "imported", "branding", "loyalty", "loyalty_cards") ->
            listOf(Color(0xFF171717), Color(0xFF424242), Color(0xFF777777))
        documentKey.startsWith("professional_") || documentKey == "professional" ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey.startsWith("organization_") || documentKey in setOf("registry", "business", "entity") ->
            listOf(Color(0xFF064B35), Color(0xFF167A50), Color(0xFF71A878))
        documentKey in setOf("identity", "identity_card", "profile", "parliament") ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey in setOf("civil", "official", "criminal_record", "registry_extract") ->
            listOf(Color(0xFF790018), Color(0xFFB5122B), Color(0xFFE36A72))
        else -> listOf(Color(0xFF46515B), Color(0xFF89949E), Color(0xFFD5DCE2))
    }
    val selectedPaletteId = tilePreferences.getString("${documentKey}_tile_gradient_palette", "auto").orEmpty()
    val palette = tileGradientPaletteOptions.firstOrNull { it.id == selectedPaletteId && it.id != "auto" }
        ?.colors?.map { Color(android.graphics.Color.parseColor(it)) } ?: defaultPalette
    val motif = when {
        documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") -> 3
        documentKey.startsWith("rod_") || documentKey in setOf(
            "family", "children", "birth_extract", "marriage_extract", "death_extract", "large_family"
        ) -> 4
        documentKey in setOf("disability", "disability_card", "vaccines", "blood_donor", "ehic",
            "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") -> 7
        documentKey in setOf("student", "training", "eu_education") ||
            documentKey.startsWith("professional_education") -> 6
        documentKey in setOf("passport", "mission_travel", "vehicle_registration") -> 5
        documentKey.startsWith("professional_") || documentKey == "professional" -> 6
        documentKey.startsWith("organization_") || documentKey.startsWith("entity_") ||
            documentKey in setOf("registry", "business", "entity", "parliament", "bank_accounts") -> 2
        documentKey in setOf("identity", "identity_card", "profile", "official", "criminal_record") -> 0
        else -> 1
    }
    val selectedMotif = tilePreferences.getString("${documentKey}_tile_vector_motif", "auto").orEmpty()
    val activeMotif = selectedMotif.toIntOrNull()?.takeIf { it in 0..7 } ?: motif
    Box(modifier.clip(RoundedCornerShape(13.dp))) {
    if (tileBackgroundBitmap != null) {
        Image(tileBackgroundBitmap.asImageBitmap(), contentDescription = "Indywidualne tło kafelka dokumentu",
            modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
    }
    if (tileVectorBackgroundBitmap != null) {
        Image(tileVectorBackgroundBitmap.asImageBitmap(), contentDescription = "Wektorowe tło kafelka dokumentu", modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.24f)
    }
    ComposeCanvas(Modifier.matchParentSize()) {
        drawRect(brush = Brush.linearGradient(
            colors = palette,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f)
        ), alpha = if (tileBackgroundBitmap != null || tileVectorBackgroundBitmap != null) 0.22f else 1f)
        val cx = size.width * 0.82f
        val cy = size.height * 0.28f
        val radius = maxOf(size.width, size.height) * 0.34f
        val watermark = Color.White.copy(alpha = 0.13f)
        val fine = Color.White.copy(alpha = 0.09f)
        drawCircle(watermark, radius, Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
        drawCircle(fine, radius * 0.76f, Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
        drawArc(watermark, -28f, 238f, false,
            topLeft = Offset(cx - radius * 0.88f, cy - radius * 0.88f),
            size = Size(radius * 1.76f, radius * 1.76f),
            style = Stroke(width = 3.dp.toPx())
        )
        val stroke = Stroke(width = maxOf(1.5.dp.toPx(), size.minDimension * 0.012f))
        when (activeMotif) {
            0 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.48f), Offset(cx, cy + radius * 0.48f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.42f, cy), Offset(cx + radius * 0.42f, cy), strokeWidth = stroke.width)
                drawCircle(watermark, radius * 0.24f, Offset(cx, cy), style = stroke)
            }
            1 -> {
                val left = cx - radius * 0.32f
                val top = cy - radius * 0.42f
                drawRoundRect(watermark, Offset(left, top), Size(radius * 0.64f, radius * 0.84f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.12f), style = stroke)
                drawLine(watermark, Offset(left + radius * 0.13f, cy - radius * 0.12f),
                    Offset(left + radius * 0.51f, cy - radius * 0.12f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(left + radius * 0.13f, cy + radius * 0.10f),
                    Offset(left + radius * 0.45f, cy + radius * 0.10f), strokeWidth = stroke.width)
            }
            2 -> {
                drawLine(watermark, Offset(cx - radius * 0.46f, cy + radius * 0.34f),
                    Offset(cx + radius * 0.46f, cy + radius * 0.34f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.36f, cy + radius * 0.34f),
                    Offset(cx - radius * 0.36f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx + radius * 0.36f, cy + radius * 0.34f),
                    Offset(cx + radius * 0.36f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.46f, cy - radius * 0.20f),
                    Offset(cx + radius * 0.46f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.50f, cy - radius * 0.20f),
                    Offset(cx, cy - radius * 0.48f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.48f),
                    Offset(cx + radius * 0.50f, cy - radius * 0.20f), strokeWidth = stroke.width)
            }
            3 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.52f),
                    Offset(cx, cy + radius * 0.48f), strokeWidth = stroke.width * 1.25f)
                drawLine(watermark, Offset(cx - radius * 0.32f, cy - radius * 0.20f),
                    Offset(cx + radius * 0.32f, cy - radius * 0.20f), strokeWidth = stroke.width * 1.25f)
                drawArc(watermark, 205f, 130f, false,
                    topLeft = Offset(cx - radius * 0.44f, cy + radius * 0.05f),
                    size = Size(radius * 0.88f, radius * 0.70f), style = stroke)
            }
            4 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.36f),
                    Offset(cx, cy + radius * 0.43f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.08f),
                    Offset(cx - radius * 0.34f, cy - radius * 0.34f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.08f),
                    Offset(cx + radius * 0.34f, cy - radius * 0.34f), strokeWidth = stroke.width)
                drawCircle(watermark, radius * 0.13f, Offset(cx, cy - radius * 0.42f), style = stroke)
                drawCircle(watermark, radius * 0.12f, Offset(cx - radius * 0.38f, cy - radius * 0.38f), style = stroke)
                drawCircle(watermark, radius * 0.12f, Offset(cx + radius * 0.38f, cy - radius * 0.38f), style = stroke)
                drawArc(watermark, 190f, 160f, false,
                    Offset(cx - radius * 0.42f, cy + radius * 0.10f),
                    Size(radius * 0.84f, radius * 0.62f), style = stroke)
            }
            5 -> {
                drawCircle(watermark, radius * 0.46f, Offset(cx, cy), style = stroke)
                drawOval(watermark, Offset(cx - radius * 0.23f, cy - radius * 0.46f),
                    Size(radius * 0.46f, radius * 0.92f), style = stroke)
                drawLine(watermark, Offset(cx - radius * 0.43f, cy),
                    Offset(cx + radius * 0.43f, cy), strokeWidth = stroke.width)
                drawArc(watermark, 0f, 180f, false,
                    Offset(cx - radius * 0.43f, cy - radius * 0.22f),
                    Size(radius * 0.86f, radius * 0.44f), style = stroke)
                drawArc(watermark, 180f, 180f, false,
                    Offset(cx - radius * 0.43f, cy - radius * 0.22f),
                    Size(radius * 0.86f, radius * 0.44f), style = stroke)
            }
            6 -> {
                val book = Path().apply {
                    moveTo(cx, cy + radius * 0.38f)
                    cubicTo(cx - radius * 0.20f, cy + radius * 0.22f, cx - radius * 0.38f, cy + radius * 0.28f, cx - radius * 0.48f, cy + radius * 0.20f)
                    lineTo(cx - radius * 0.48f, cy - radius * 0.36f)
                    cubicTo(cx - radius * 0.28f, cy - radius * 0.46f, cx - radius * 0.12f, cy - radius * 0.30f, cx, cy - radius * 0.18f)
                    cubicTo(cx + radius * 0.12f, cy - radius * 0.30f, cx + radius * 0.28f, cy - radius * 0.46f, cx + radius * 0.48f, cy - radius * 0.36f)
                    lineTo(cx + radius * 0.48f, cy + radius * 0.20f)
                    cubicTo(cx + radius * 0.28f, cy + radius * 0.28f, cx + radius * 0.20f, cy + radius * 0.22f, cx, cy + radius * 0.38f)
                    close()
                }
                drawPath(book, watermark, style = stroke)
                drawLine(watermark, Offset(cx, cy - radius * 0.18f),
                    Offset(cx, cy + radius * 0.36f), strokeWidth = stroke.width)
            }
            7 -> {
                drawRoundRect(watermark, Offset(cx - radius * 0.15f, cy - radius * 0.48f),
                    Size(radius * 0.30f, radius * 0.96f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.08f), style = stroke)
                drawRoundRect(watermark, Offset(cx - radius * 0.48f, cy - radius * 0.15f),
                    Size(radius * 0.96f, radius * 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.08f), style = stroke)
                drawCircle(fine, radius * 0.58f, Offset(cx, cy), style = stroke)
            }
        }
        // Layered security-print treatment shared by every document tile:
        // guilloche rosette, micro-grid, diagonal engraving and precision frame.
        val minSide = size.minDimension
        val inset = minSide * 0.055f
        val frameColor = Color.White.copy(alpha = 0.16f)
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2f, size.height - inset * 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(minSide * 0.055f),
            style = Stroke(width = maxOf(0.7.dp.toPx(), minSide * 0.0025f))
        )
        // Fine engraved diagonal lines are deliberately clipped to the lower-left field.
        val engraving = Color.White.copy(alpha = 0.055f)
        val spacing = maxOf(5.dp.toPx(), minSide * 0.035f)
        var line = -size.height
        while (line < size.width * 0.72f) {
            drawLine(
                color = engraving,
                start = Offset(line, size.height),
                end = Offset(line + size.height * 0.72f, size.height * 0.28f),
                strokeWidth = maxOf(0.55.dp.toPx(), minSide * 0.0018f)
            )
            line += spacing
        }
        // Halftone security field.
        val dotStep = maxOf(5.dp.toPx(), minSide * 0.045f)
        var dotY = size.height * 0.58f
        while (dotY < size.height * 0.91f) {
            var dotX = size.width * 0.07f
            while (dotX < size.width * 0.48f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = maxOf(0.45.dp.toPx(), minSide * 0.0022f),
                    center = Offset(dotX, dotY)
                )
                dotX += dotStep
            }
            dotY += dotStep
        }
        // Guilloche rosette: concentric rings and radial spokes.
        val rosetteCenter = Offset(size.width * 0.84f, size.height * 0.76f)
        val rosetteRadius = minSide * 0.20f
        val rosetteColor = Color.White.copy(alpha = 0.13f)
        for (ring in 0..3) {
            drawCircle(
                color = rosetteColor.copy(alpha = 0.12f + ring * 0.025f),
                radius = rosetteRadius * (0.42f + ring * 0.19f),
                center = rosetteCenter,
                style = Stroke(width = maxOf(0.65.dp.toPx(), minSide * 0.002f))
            )
        }
        for (spoke in 0 until 16) {
            val angle = spoke * (2f * Math.PI.toFloat() / 16f)
            val inner = rosetteRadius * 0.34f
            val outer = rosetteRadius * (if (spoke % 2 == 0) 1.12f else 0.88f)
            drawLine(
                color = rosetteColor,
                start = Offset(
                    rosetteCenter.x + kotlin.math.cos(angle.toDouble()).toFloat() * inner,
                    rosetteCenter.y + kotlin.math.sin(angle.toDouble()).toFloat() * inner
                ),
                end = Offset(
                    rosetteCenter.x + kotlin.math.cos(angle.toDouble()).toFloat() * outer,
                    rosetteCenter.y + kotlin.math.sin(angle.toDouble()).toFloat() * outer
                ),
                strokeWidth = maxOf(0.6.dp.toPx(), minSide * 0.002f)
            )
        }
        // Four registration marks and a discreet security seal at the lower edge.
        val mark = minSide * 0.035f
        val corners = listOf(
            Offset(inset + mark, inset + mark),
            Offset(size.width - inset - mark, inset + mark),
            Offset(inset + mark, size.height - inset - mark),
            Offset(size.width - inset - mark, size.height - inset - mark)
        )
        corners.forEach { p ->
            drawCircle(frameColor, radius = maxOf(1.dp.toPx(), minSide * 0.006f), center = p)
        }
        drawCircle(Color.White.copy(alpha = 0.08f), radius * 0.10f,
            Offset(size.width * 0.16f, size.height * 0.82f))
    }
        Image(
            painter = painterResource(documentTileWatermarkResource(documentKey)),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.30f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )
        // Delikatna, wspólna warstwa zabezpieczająca: samopodobna rozeta
        // geometryczna pozostaje czytelna na wszystkich tłach kategorii.
        Image(
            painter = painterResource(R.drawable.tile_watermark_fractal),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.13f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )
    }
}

@Composable
private fun DocumentCardEmblem(documentKey: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = remember(documentKey) { LegitimacyPreviewSettingsStore.load(context, documentKey) }
    val documentTitle = documentBrandTargets.firstOrNull { it.first == documentKey }?.second ?: documentKey
    val assigned = remember(documentKey, settings.crestAssetId) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, documentKey, documentTitle, "Godło")
            ?: DocumentBrandStore.imageForDocument(context, documentKey, documentTitle, "Herb")
    }
    val selected = assigned ?: remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.a_clean_vector_style_emblem_on_a_transparent_backg)
    }
    if (selected != null) {
        Box(
            modifier.clip(RoundedCornerShape(5.dp))
                .background(if (assigned == null) Color.White.copy(alpha = 0.16f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                selected.asImageBitmap(),
                contentDescription = "Indywidualne godło dokumentu",
                modifier = Modifier.fillMaxSize().padding(2.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
