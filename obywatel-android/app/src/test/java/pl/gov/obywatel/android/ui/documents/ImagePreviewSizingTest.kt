// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImagePreviewSizingTest {
    @Test
    fun keepsSmallImagesAtFullResolution() {
        assertEquals(1, calculateInSampleSize(800, 600, 2048))
    }

    @Test
    fun downsamplesLargeLandscapeImage() {
        assertEquals(8, calculateInSampleSize(12000, 4000, 2048))
    }

    @Test
    fun downsamplesLargePortraitImage() {
        assertEquals(8, calculateInSampleSize(4000, 12000, 2048))
    }

    @Test
    fun handlesSinglePixelDimension() {
        assertEquals(1, calculateInSampleSize(1, 900, 2048))
    }

    @Test
    fun handlesMaximumIntegerDimensionsWithoutOverflow() {
        assertEquals(
            1 shl 30,
            calculateInSampleSize(Int.MAX_VALUE, Int.MAX_VALUE, 1)
        )
    }

    @Test
    fun respectsExactMaximumDimensionBoundary() {
        assertEquals(4, calculateInSampleSize(8192, 4096, 2048))
    }

    @Test
    fun rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            calculateInSampleSize(0, 100, 2048)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculateInSampleSize(100, 100, 0)
        }
    }
}
