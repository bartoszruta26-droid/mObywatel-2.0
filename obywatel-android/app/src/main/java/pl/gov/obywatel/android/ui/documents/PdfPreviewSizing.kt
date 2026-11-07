// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

/** Pixel dimensions for a bounded PDF page rasterization. */
internal data class PreviewDimensions(val width: Int, val height: Int)

/**
 * Keeps PDF previews within [maxDimension] on the longest side and avoids
 * enlarging small pages beyond 2x their source dimensions.
 */
internal fun calculatePreviewDimensions(
    width: Int,
    height: Int,
    maxDimension: Int
): PreviewDimensions {
    require(width > 0) { "Page width must be positive." }
    require(height > 0) { "Page height must be positive." }
    require(maxDimension > 0) { "Maximum dimension must be positive." }

    val longestSide = maxOf(width, height).toFloat()
    val scale = minOf(2f, maxDimension / longestSide)
    return PreviewDimensions(
        width = (width * scale).toInt().coerceAtLeast(1),
        height = (height * scale).toInt().coerceAtLeast(1)
    )
}
