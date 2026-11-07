// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

/** Pixel dimensions for a bounded PDF page rasterization. */
internal data class PreviewDimensions(val width: Int, val height: Int)

/**
 * Bounds the longest side by [maxDimension] and never enlarges a page by
 * more than 2x. Integer arithmetic avoids Float rounding that could make the
 * shorter side one pixel larger than its exact floor.
 */
internal fun calculatePreviewDimensions(
    width: Int,
    height: Int,
    maxDimension: Int
): PreviewDimensions {
    require(width > 0) { "Page width must be positive." }
    require(height > 0) { "Page height must be positive." }
    require(maxDimension > 0) { "Maximum dimension must be positive." }

    val longestSide = maxOf(width, height)
    if (longestSide.toLong() * 2L <= maxDimension.toLong()) {
        return PreviewDimensions(width * 2, height * 2)
    }

    return if (width >= height) {
        PreviewDimensions(
            width = maxDimension,
            height = (height.toLong() * maxDimension / width).coerceAtLeast(1L).toInt()
        )
    } else {
        PreviewDimensions(
            width = (width.toLong() * maxDimension / height).coerceAtLeast(1L).toInt(),
            height = maxDimension
        )
    }
}
