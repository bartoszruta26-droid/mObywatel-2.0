// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentUriImageDecoderInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun decodesContentUriImageWithBoundedDimensionsAndCallerOwnership() {
        val file = File.createTempFile("document-image-", ".png", context.cacheDir)
        val original = Bitmap.createBitmap(3000, 1500, Bitmap.Config.ARGB_8888)
        try {
            file.outputStream().use { output ->
                check(original.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.ocrfiles",
                file
            )
            assertEquals("content", uri.scheme)

            val source = DocumentSourceDescriptor(
                id = "test-image",
                mimeType = "image/png",
                displayName = file.name
            )
            val decoded = DocumentUriImageDecoder(context.contentResolver).decode(source, uri)
            try {
                assertTrue(maxOf(decoded.bitmap.width, decoded.bitmap.height) <= 2048)
                assertFalse(decoded.bitmap.isRecycled)
                assertTrue(decoded.rotationDegrees in setOf(0, 90, 180, 270))
            } finally {
                decoded.bitmap.recycle()
            }
        } finally {
            original.recycle()
            file.delete()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonImageSourceBeforeOpeningUri() {
        DocumentUriImageDecoder(context.contentResolver).decode(
            DocumentSourceDescriptor("test-pdf", "application/pdf"),
            android.net.Uri.parse("content://invalid/document")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFileUriBecauseImportedDocumentsUseContentUris() {
        DocumentUriImageDecoder(context.contentResolver).decode(
            DocumentSourceDescriptor("test-image", "image/png"),
            android.net.Uri.parse("file:///tmp/document.png")
        )
    }
}
