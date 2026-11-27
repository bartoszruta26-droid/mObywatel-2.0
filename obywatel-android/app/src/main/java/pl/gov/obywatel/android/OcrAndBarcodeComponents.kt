// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

internal fun OcrRoundAction(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(68.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
            shape = CircleShape,
            color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(26.dp))
            }
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal fun LoyaltyGeneratedCodes(barcodeValue: String, qrValue: String) {
    val barcode = remember(barcodeValue) { generateLoyaltyCodeBitmap(barcodeValue, BarcodeFormat.CODE_128) }
    val qr = remember(qrValue) { generateLoyaltyCodeBitmap(qrValue, BarcodeFormat.QR_CODE) }
    if (barcode != null || qr != null) {
        Text("Podgląd kodów", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        if (barcode != null) Image(barcode.asImageBitmap(), contentDescription = "Kod kreskowy karty lojalnościowej",
            modifier = Modifier.fillMaxWidth().height(64.dp), contentScale = ContentScale.Fit)
        if (qr != null) Image(qr.asImageBitmap(), contentDescription = "Kod QR karty lojalnościowej",
            modifier = Modifier.size(170.dp), contentScale = ContentScale.Fit)
    }
}

internal fun generateLoyaltyCodeBitmap(value: String, format: BarcodeFormat): Bitmap? {
    if (value.isBlank()) return null
    return runCatching {
        val width = if (format == BarcodeFormat.QR_CODE) 480 else 900
        val height = if (format == BarcodeFormat.QR_CODE) 480 else 220
        val matrix = MultiFormatWriter().encode(value, format, width, height,
            mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8"))
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (y in 0 until height) for (x in 0 until width) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }.getOrNull()
}
