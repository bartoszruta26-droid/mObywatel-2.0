// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

/**
 * Bounded decoder for image documents selected through the existing SAF import flow.
 *
 * The resolver is opened three times: for image bounds, EXIF metadata and sampled decoding.
 * The caller owns the returned bitmap and must recycle it after all processing ends.
 */
internal class DocumentUriImageDecoder(
    private val contentResolver: ContentResolver,
    private val maxDimension: Int = DEFAULT_MAX_DIMENSION
) {
    init {
        require(maxDimension > 0) { "Maksymalny wymiar obrazu musi być dodatni." }
    }

    fun decode(source: DocumentSourceDescriptor, uri: Uri): BitmapProcessingInput {
        require(source.mimeType.lowercase() in SUPPORTED_IMAGE_MIME_TYPES) {
            "To źródło nie jest obsługiwanym obrazem rastrowym."
        }
        require(uri.scheme == "content") {
            "Obraz musi pochodzić z URI content:// dostawcy plików."
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(uri)
            ?: error("Nie można otworzyć obrazu.")
        // decodeStream intentionally returns null in inJustDecodeBounds mode;
        // the dimensions are written to Options, so only the stream itself is nullable.
        boundsStream.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        val width = bounds.outWidth
        val height = bounds.outHeight
        require(width > 0 && height > 0) { "Plik nie zawiera prawidłowego obrazu." }
        require(width <= MAX_SOURCE_DIMENSION && height <= MAX_SOURCE_DIMENSION) {
            "Wymiary obrazu przekraczają bezpieczny limit."
        }
        require(width.toLong() * height.toLong() <= MAX_SOURCE_PIXELS) {
            "Liczba pikseli obrazu przekracza bezpieczny limit."
        }

        val exifOrientation = contentResolver.openInputStream(uri)?.use { input ->
            runCatching { ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            ) }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateImageSampleSize(width, height, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: error("Nie można ponownie otworzyć obrazu do dekodowania.")
        var bitmap = decoded
        try {
            val rotation = exifOrientation.toRotationDegrees()
            if (exifOrientation.isMirrored()) {
                val matrix = Matrix().apply {
                    when (exifOrientation) {
                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
                        ExifInterface.ORIENTATION_TRANSPOSE -> setScale(-1f, 1f)
                        ExifInterface.ORIENTATION_FLIP_VERTICAL,
                        ExifInterface.ORIENTATION_TRANSVERSE -> setScale(1f, -1f)
                    }
                }
                val transformed = Bitmap.createBitmap(
                    decoded, 0, 0, decoded.width, decoded.height, matrix, true
                )
                if (transformed !== decoded) {
                    decoded.recycle()
                    bitmap = transformed
                }
            }
            return BitmapProcessingInput(source, bitmap, rotation)
        } catch (failure: Exception) {
            if (!bitmap.isRecycled) bitmap.recycle()
            throw failure
        }
    }

    private fun Int.toRotationDegrees(): Int = when (this) {
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_TRANSPOSE -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270,
        ExifInterface.ORIENTATION_TRANSVERSE -> 270
        else -> 0
    }

    private fun Int.isMirrored(): Boolean = this in setOf(
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        ExifInterface.ORIENTATION_FLIP_VERTICAL,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE
    )

    private companion object {
        const val DEFAULT_MAX_DIMENSION = 2048
        const val MAX_SOURCE_DIMENSION = 100_000
        const val MAX_SOURCE_PIXELS = 500_000_000L
        val SUPPORTED_IMAGE_MIME_TYPES = setOf(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp"
        )
    }
}
