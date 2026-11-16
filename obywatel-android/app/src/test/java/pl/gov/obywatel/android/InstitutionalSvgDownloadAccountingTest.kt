// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstitutionalSvgDownloadAccountingTest {
    @Test
    fun progressLabelShowsExactCountOutOfDiscoveredSvgFiles() {
        assertEquals("Postęp: 0/200 plików SVG", commonsDownloadProgressLabel(0, 200))
        assertEquals("Postęp: 1/200 plików SVG", commonsDownloadProgressLabel(1, 200))
        assertEquals("Postęp: 200/200 plików SVG", commonsDownloadProgressLabel(200, 200))
    }

    @Test
    fun progressLabelClampsCompletedCountToValidRange() {
        assertEquals("Postęp: 0/200 plików SVG", commonsDownloadProgressLabel(-5, 200))
        assertEquals("Postęp: 200/200 plików SVG", commonsDownloadProgressLabel(250, 200))
    }
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
