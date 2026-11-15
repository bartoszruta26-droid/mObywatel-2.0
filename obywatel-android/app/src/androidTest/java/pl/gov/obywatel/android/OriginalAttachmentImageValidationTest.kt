// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class OriginalAttachmentImageValidationTest {
    private fun png(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                output.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun acceptsFullyDecodablePngImage() {
        validateOriginalAttachmentPayload("image/png", png(32, 24))
    }

    @Test
    fun rejectsImageWithValidPngSignatureButInvalidImageData() {
        val invalid = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3, 4, 5, 6, 7, 8)
        assertTrue(runCatching { validateOriginalAttachmentPayload("image/png", invalid) }.isFailure)
    }

    @Test
    fun rejectsImageDimensionsAboveConfiguredLimits() {
        assertTrue(runCatching { validateOriginalAttachmentPayload("image/png", png(12001, 1)) }.isFailure)
    }
}
