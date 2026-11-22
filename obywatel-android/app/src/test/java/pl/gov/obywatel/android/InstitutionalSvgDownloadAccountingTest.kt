// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstitutionalSvgDownloadAccountingTest {
    @Test
    fun commonsSearchUsesMimeFilterAndPlainFallback() {
        assertEquals(
            listOf("herb miasta filemime:svg", "herb miasta"),
            commonsSvgSearchExpressions("herb miasta")
        )
    }

    @Test
    fun commonsSearchRejectsBlankTerms() {
        try {
            commonsSvgSearchExpressions("   ")
            throw AssertionError("Pusta fraza powinna zostać odrzucona.")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

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
    fun commonsRetriesWikimediaRateLimitsIncludingHttp420() {
        assertTrue(commonsIsRetryableHttpCode(420))
        assertTrue(commonsIsRetryableHttpCode(429))
        assertTrue(commonsIsRetryableHttpCode(502))
        assertTrue(commonsIsRetryableHttpCode(503))
        assertTrue(commonsIsRetryableHttpCode(504))
        assertFalse(commonsIsRetryableHttpCode(400))
        assertFalse(commonsIsRetryableHttpCode(403))
        assertFalse(commonsIsRetryableHttpCode(404))
    }

    @Test
    fun retryAfterDelayIsCappedBeforeMultiplicationAndCannotOverflow() {
        assertEquals(30000L, commonsRetryDelayMs(0, 59L))
        assertEquals(30000L, commonsRetryDelayMs(0, 60L))
        assertEquals(30000L, commonsRetryDelayMs(0, Long.MAX_VALUE))
    }

    @Test
    fun invalidRetryAfterUsesBoundedExponentialBackoff() {
        assertEquals(2000L, commonsRetryDelayMs(0, -1L))
        assertEquals(2000L, commonsRetryDelayMs(1, null))
        assertEquals(30000L, commonsRetryDelayMs(99, null))
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

    @Test
    fun duplicateCommonsTitlesAndUploadUrlsAreQueuedOnlyOnce() {
        val original = CommonsSvgCandidate(
            "File:crest.svg", "https://upload.wikimedia.org/a/crest.svg",
            "https://commons.wikimedia.org/wiki/File:crest.svg", "CC0", "", "", ""
        )
        val sameTitleDifferentUrl = original.copy(url = "https://upload.wikimedia.org/a/other-copy.svg")
        val sameUrlDifferentTitle = original.copy(title = "File:crest-copy.svg")
        val another = original.copy(
            title = "File:flag.svg",
            url = "https://upload.wikimedia.org/a/flag.svg"
        )

        val queue = distinctCommonsCandidates(
            listOf(original, sameTitleDifferentUrl, sameUrlDifferentTitle, another)
        )

        assertEquals(listOf("File:crest.svg", "File:flag.svg"), queue.map { it.title })
    }

    @Test
    fun duplicateUploadUrlsIgnoreQueryAndFragment() {
        val first = CommonsSvgCandidate(
            "File:logo.svg", "https://upload.wikimedia.org/a/logo.svg?download=1",
            "page", "CC0", "", "", ""
        )
        val duplicate = first.copy(
            title = "File:logo-alias.svg",
            url = "https://upload.wikimedia.org/a/logo.svg#fragment"
        )

        assertEquals(1, distinctCommonsCandidates(listOf(first, duplicate)).size)
    }

    @Test
    fun duplicateAssetAliasUrlIsReservedEvenWhenAssetIdentityAlreadyExists() {
        val first = CommonsSvgCandidate(
            "File:crest.svg", "https://upload.wikimedia.org/a/crest.svg",
            "page", "CC0", "", "", ""
        )
        val duplicateAssetAlias = first.copy(
            url = "https://upload.wikimedia.org/a/crest-alias.svg"
        )
        val differentTitleSameAliasUrl = first.copy(
            title = "File:other.svg",
            url = "https://upload.wikimedia.org/a/crest-alias.svg"
        )

        val queue = distinctCommonsCandidates(
            listOf(first, duplicateAssetAlias, differentTitleSameAliasUrl)
        )

        assertEquals(listOf("File:crest.svg"), queue.map { it.title })
    }

    @Test
    fun candidateCsvExportsSearchMetadataWithoutDownloadingFiles() {
        val candidate = CommonsSvgCandidate(
            "File:International NGO logo.svg", "https://upload.wikimedia.org/a/logo.svg",
            "https://commons.wikimedia.org/wiki/File:International_NGO_logo.svg",
            "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/", "Artist, Name", "Trademark restrictions"
        )
        val csv = commonsCandidatesCsv(listOf(candidate, candidate))
        assertTrue(csv.startsWith("\"filename\",\"symbol_type\",\"commons_page\""))
        assertTrue(csv.contains("\"logo/emblemat\""))
        assertTrue(csv.contains("\"Artist, Name\""))
        assertEquals(2, csv.trimEnd().lines().size)
    }
    @Test
    fun importsCommonsCsvWithMetadataAndDeduplicatesTheSameUploadUrl() {
        val csv = "\"filename\",\"symbol_type\",\"commons_page\",\"svg_url\",\"author\",\"license\",\"license_url\",\"restrictions\",\"sha256\"\n" +
            "\"File:logo.svg\",\"logo/emblemat\",\"page\",\"https://upload.wikimedia.org/a/logo.svg\",\"Author\",\"CC0\",\"license\",\"\",\"\"\n" +
            "\"File:logo-copy.svg\",\"logo/emblemat\",\"page\",\"https://upload.wikimedia.org/a/logo.svg?download=1\",\"Author\",\"CC0\",\"license\",\"\",\"\"\n"
        val imported = parseExternalCommonsSvgList(csv)
        assertEquals(1, imported.size)
        assertEquals("File:logo.svg", imported.single().title)
        assertEquals("Author", imported.single().artist)
    }

    @Test
    fun importsPlainTextListOfWikimediaSvgUrls() {
        val imported = parseExternalCommonsSvgList("https://upload.wikimedia.org/a/coat.svg\n")
        assertEquals(1, imported.size)
        assertEquals("File:coat.svg", imported.single().title)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExternalSvgUrlsOutsideWikimediaUploadHost() {
        parseExternalCommonsSvgList("https://example.org/logo.svg")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExternalCsvWithoutSvgUrlColumn() {
        parseExternalCommonsSvgList("\"filename\",\"author\"\n\"File:logo.svg\",\"Author\"")
    }
}
