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
    @Test
    fun retryAfterDelayIsCappedBeforeMultiplicationAndCannotOverflow() {
        assertEquals(59000L, commonsRetryDelayMs(0, 59L))
        assertEquals(60000L, commonsRetryDelayMs(0, 60L))
        assertEquals(60000L, commonsRetryDelayMs(0, Long.MAX_VALUE))
    }

    @Test
    fun invalidRetryAfterUsesBoundedExponentialBackoff() {
        assertEquals(4000L, commonsRetryDelayMs(0, -1L))
        assertEquals(4000L, commonsRetryDelayMs(1, null))
        assertEquals(180000L, commonsRetryDelayMs(99, null))
    }

    @Test
    fun rangeResumeRequires206AndExactStartingOffset() {
        assertTrue(commonsCanAppendRange(1024L, 206, "bytes 1024-2047/4096"))
        assertFalse(commonsCanAppendRange(1024L, 200, "bytes 1024-2047/4096"))
        assertFalse(commonsCanAppendRange(1024L, 206, "bytes 0-1023/4096"))
        assertFalse(commonsCanAppendRange(0L, 206, "bytes 0-1023/4096"))
        assertFalse(commonsCanAppendRange(1024L, 206, null))
    }

    @Test
    fun resumeSkipsOnlyAssetsWithMatchingSha256() {
        val valid = "a".repeat(64)
        assertTrue(commonsChecksumMatches(valid, valid))
        assertTrue(commonsChecksumMatches(valid.uppercase(), valid))
        assertFalse(commonsChecksumMatches(valid, "b".repeat(64)))
        assertFalse(commonsChecksumMatches("", valid))
        assertFalse(commonsChecksumMatches("not-a-checksum", valid))
        assertFalse(commonsChecksumMatches(valid, null))
    }

}
