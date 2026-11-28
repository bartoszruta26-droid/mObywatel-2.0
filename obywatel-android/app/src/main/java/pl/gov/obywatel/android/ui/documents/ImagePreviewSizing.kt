// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import pl.gov.obywatel.android.documentprocessing.calculateImageSampleSize

/**
 * Calculates a power-of-two sample size that bounds the decoded image's longest side.
 * The decoder may return dimensions smaller than [maxDimension], never larger than
 * the requested bound (apart from formats with decoder-specific rounding).
 */
internal fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int =
    calculateImageSampleSize(width, height, maxDimension)
