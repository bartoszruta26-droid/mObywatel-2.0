// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pl.gov.obywatel.android.DocumentImageRecognition

@RunWith(AndroidJUnit4::class)
class DocumentImageRecognitionInstrumentedTest {

    @Test
    fun blankBitmapProducesEmptyOcrAndRemainsOwnedByCaller() = runBlocking {
        val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        try {
            val result = DocumentImageRecognition().recognizeTextResult(bitmap)
            assertTrue(result.text.isEmpty())
            assertTrue(result.blocks.isEmpty())
            assertTrue(!bitmap.isRecycled)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun blankBitmapProducesNoBarcodesAndRemainsOwnedByCaller() = runBlocking {
        val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        try {
            val result = DocumentImageRecognition().recognizeBarcodes(bitmap, rotationDegrees = 90)
            assertTrue(result.isEmpty())
            assertTrue(!bitmap.isRecycled)
        } finally {
            bitmap.recycle()
        }
    }
}
