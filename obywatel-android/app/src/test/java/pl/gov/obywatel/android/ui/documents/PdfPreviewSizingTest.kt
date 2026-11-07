// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PdfPreviewSizingTest {
    @Test
    fun boundsLandscapePageByLongestSideWithoutFloatRounding() {
        assertEquals(
            PreviewDimensions(1800, 1272),
            calculatePreviewDimensions(2480, 1754, 1800)
        )
    }

    @Test
    fun boundsPortraitPageByLongestSideWithoutFloatRounding() {
        assertEquals(
            PreviewDimensions(1272, 1800),
            calculatePreviewDimensions(1754, 2480, 1800)
        )
    }

    @Test
    fun doesNotUpscaleSmallPagesBeyondTwoTimes() {
        assertEquals(
            PreviewDimensions(1200, 800),
            calculatePreviewDimensions(600, 400, 1800)
        )
    }

    @Test
    fun doesNotExceedMaximumDimensionForExtremeAspectRatio() {
        assertEquals(
            PreviewDimensions(1, 1800),
            calculatePreviewDimensions(1, 5000, 1800)
        )
    }

    @Test
    fun keepsSmallPagesAtNativeDimensionsWhenDoublingWouldOverflow() {
        assertEquals(
            PreviewDimensions(Int.MAX_VALUE, 2),
            calculatePreviewDimensions(Int.MAX_VALUE, 1, Int.MAX_VALUE)
        )
    }

    @Test
    fun rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            calculatePreviewDimensions(0, 100, 1800)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculatePreviewDimensions(100, 100, 0)
        }
    }
}
