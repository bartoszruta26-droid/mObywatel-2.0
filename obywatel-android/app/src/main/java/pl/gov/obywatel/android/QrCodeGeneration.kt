// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

internal const val QR_BRAND_NONE = "none"
internal const val QR_BRAND_STATE = "state"
internal const val QR_BRAND_FILE = "file"

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

internal fun loadQrLogo(context: Context, uri: Uri): Bitmap {
    val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: error("Nie udało się odczytać obrazu.")
    require(source.width > 0 && source.height > 0) { "Nieprawidłowy plik obrazu." }
    val scale = minOf(1f, 1024f / maxOf(source.width, source.height))
    return if (scale < 1f) Bitmap.createScaledBitmap(source, (source.width * scale).toInt().coerceAtLeast(1), (source.height * scale).toInt().coerceAtLeast(1), true) else source
}

/**
 * Generuje kod QR z korekcją błędów H i umieszcza znak graficzny centralnie.
 * Logo jest ograniczone do 9% szerokości kodu, z białym polem ochronnym.
 * Mały rozmiar i korekcja H ograniczają ryzyko utraty czytelności; odczyt
 * należy dodatkowo sprawdzać dla każdego rodzaju ładunku i użytego logo.
 */
internal fun generateBrandedQr(context: Context, payload: String, branding: String, uploadedLogo: Bitmap?): Bitmap {
    require(payload.isNotBlank()) { "Treść kodu QR jest pusta." }
    val logo = when (branding) {
        QR_BRAND_STATE -> requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.institutions_eagle_emblem)) {
            "Nie można wczytać godła."
        }
        QR_BRAND_FILE -> requireNotNull(uploadedLogo) { "Wybierz plik z logo." }
        else -> null
    }
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to 4,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 1000, 1000, hints)
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    if (logo != null) {
        val canvas = Canvas(bitmap)
        val side = matrix.width * 0.09f
        val padding = matrix.width * 0.018f
        val box = side + padding * 2f
        val left = (matrix.width - box) / 2f
        val top = (matrix.height - box) / 2f
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(left, top, left + box, top + box), box * 0.12f, box * 0.12f, backgroundPaint)
        val scale = side / maxOf(logo.width, logo.height).toFloat()
        val width = logo.width * scale
        val height = logo.height * scale
        val destination = RectF(
            (matrix.width - width) / 2f,
            (matrix.height - height) / 2f,
            (matrix.width + width) / 2f,
            (matrix.height + height) / 2f
        )
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(logo, null, destination, logoPaint)
    }
    return bitmap
}
