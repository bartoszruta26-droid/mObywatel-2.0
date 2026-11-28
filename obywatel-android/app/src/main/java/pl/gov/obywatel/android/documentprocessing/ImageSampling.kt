// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

/** Returns a power-of-two sample size for a bounded bitmap decode. */
internal fun calculateImageSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    require(width > 0) { "Image width must be positive." }
    require(height > 0) { "Image height must be positive." }
    require(maxDimension > 0) { "Maximum dimension must be positive." }

    var sampleSize = 1
    while (maxOf(width / sampleSize, height / sampleSize) > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}
