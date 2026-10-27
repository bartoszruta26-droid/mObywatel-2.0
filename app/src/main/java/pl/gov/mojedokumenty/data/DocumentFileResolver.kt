package pl.gov.mojedokumenty.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import pl.gov.mojedokumenty.model.DocumentSource
import java.io.File
import java.io.FileOutputStream

/**
 * Udostępnia pliki dokumentów rendererom:
 * - asset („docs/xxx.pdf”) → kopia w cacheDir (PdfRenderer wymaga deskryptora pliku),
 * - content:// (SAF/intent) → ParcelFileDescriptor przez ContentResolver,
 * - obrazy PNG/JPG → dekodowanie do Bitmapy.
 */
object DocumentFileResolver {

    fun isImage(location: String?): Boolean =
        location != null && (location.endsWith(".png", true) ||
            location.endsWith(".jpg", true) || location.endsWith(".jpeg", true))

    /** Materiał do renderu: PDF (deskryptor) albo bitmapa obrazu. */
    sealed class Renderable {
        class Pdf(val descriptor: ParcelFileDescriptor) : Renderable()
        class Image(val bitmap: android.graphics.Bitmap) : Renderable()
    }

    fun open(context: Context, location: String, source: DocumentSource): Renderable {
        return when (source) {
            DocumentSource.ASSET -> {
                if (isImage(location)) {
                    val bitmap = context.assets.open(location).use { input ->
                        BitmapFactory.decodeStream(input)
                    } ?: throw IllegalArgumentException("Nie udało się zdekodować obrazu z assets.")
                    Renderable.Image(bitmap)
                } else {
                    val cacheFile = File(context.cacheDir, "asset_" + location.substringAfterLast('/'))
                    if (!cacheFile.exists() || cacheFile.length() == 0L) {
                        context.assets.open(location).use { input ->
                            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
                        }
                    }
                    Renderable.Pdf(
                        ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    )
                }
            }

            DocumentSource.USER_FILE -> {
                val uri = Uri.parse(location)
                if (isImage(uri.lastPathSegment)) {
                    val bitmap = context.contentResolver.openInputStream(uri).use { input ->
                        BitmapFactory.decodeStream(input)
                    } ?: throw IllegalArgumentException("Nie udało się zdekodować obrazu.")
                    Renderable.Image(bitmap)
                } else {
                    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
                        ?: throw IllegalArgumentException("Nie można otworzyć pliku: $uri")
                    Renderable.Pdf(descriptor)
                }
            }

            DocumentSource.NONE -> throw IllegalStateException("Dokument nie ma powiązanego pliku.")
        }
    }

    fun openPdfRenderer(descriptor: ParcelFileDescriptor): PdfRenderer =
        PdfRenderer(descriptor)

    fun displayName(context: Context, uri: Uri): String? =
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (_: Exception) {
            null
        }
}
