// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstitutionalSvgDownloadAccountingTest {
    @Test
    fun detectsHundredsOfResultsWithOnlySmallFractionSaved() {
        assertTrue(isSvgDownloadDropOff(found = 200, available = 24))
        assertTrue(isSvgDownloadDropOff(found = 500, available = 100))
    }

    @Test
    fun doesNotFlagHealthyOrSmallSearchResults() {
        assertFalse(isSvgDownloadDropOff(found = 200, available = 180))
        assertFalse(isSvgDownloadDropOff(found = 24, available = 12))
        assertFalse(isSvgDownloadDropOff(found = 0, available = 0))
    }

    @Test
    fun clampsNegativeSavedCountBeforeAccounting() {
        assertTrue(isSvgDownloadDropOff(found = 100, available = -1))
    }
}
