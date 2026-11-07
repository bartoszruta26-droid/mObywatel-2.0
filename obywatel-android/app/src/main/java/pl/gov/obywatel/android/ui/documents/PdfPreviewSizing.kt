// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

/** Pixel dimensions for a bounded PDF page rasterization. */
internal data class PreviewDimensions(val width: Int, val height: Int)

/**
 * Keeps PDF previews within [maxDimension] on the longest side and avoids
 * enlarging small pages beyond 2x their source dimensions.
 *
 * Integer arithmetic is intentional: Float rounding can push the shorter
 * side one pixel beyond its expected floor (for example, 1754 × 1800 / 2480).
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
    if (longestSide <= maxDimension) {
        return PreviewDimensions(
            width = (width.toLong() * 2L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            height = (height.toLong() * 2L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        )
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
