// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

/**
 * Calculates a power-of-two sample size that bounds the decoded image's longest side.
 * The decoder may return dimensions smaller than [maxDimension], never larger than
 * the requested bound (apart from formats with decoder-specific rounding).
 */
internal fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    require(width > 0) { "Image width must be positive." }
    require(height > 0) { "Image height must be positive." }
    require(maxDimension > 0) { "Maximum dimension must be positive." }

    var sampleSize = 1
    while (maxOf(width / sampleSize, height / sampleSize) > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}
