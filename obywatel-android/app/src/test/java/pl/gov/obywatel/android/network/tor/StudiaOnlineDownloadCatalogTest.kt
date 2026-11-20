// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudiaOnlineDownloadCatalogTest {
    @Test fun preservesDoubleSlashInTheConfiguredUrl() {
        val item = StudiaOnlineDownloadCatalog.fromIds(listOf(12345)).single()
        assertEquals("https://studia-online.pl//pobierz-material/12345", item.url)
    }

    @Test fun deduplicatesIdsWithoutChangingTheirOrder() {
        assertEquals(
            listOf(3, 1, 2),
            StudiaOnlineDownloadCatalog.fromIds(listOf(3, 1, 3, 2)).map { it.id }
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroIds() {
        StudiaOnlineDownloadCatalog.fromIds(listOf(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRangesAboveTheSafetyLimit() {
        StudiaOnlineDownloadCatalog.fromRange(1, 2_001)
    }


    @Test fun rangeIsInclusiveAndPreservesDoubleSlash() {
        val items = StudiaOnlineDownloadCatalog.fromRange(41, 43)
        assertEquals(listOf(41, 42, 43), items.map { it.id })
        assertEquals(
            listOf(
                "https://studia-online.pl//pobierz-material/41",
                "https://studia-online.pl//pobierz-material/42",
                "https://studia-online.pl//pobierz-material/43"
            ),
            items.map { it.url }
        )
    }

    @Test fun acceptsExactlyTwoThousandItems() {
        assertEquals(2_000, StudiaOnlineDownloadCatalog.fromRange(10, 2_009).size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReversedRange() {
        StudiaOnlineDownloadCatalog.fromRange(10, 9)
    }

    @Test fun htmlErrorPageWithHttp200IsNotAcceptedAsMaterial() {
        val html = "<!doctype html><html><body>Nie znaleziono materiału</body></html>".toByteArray()
        assertEquals(
            "Materiał nie został znaleziony — serwis zwrócił stronę błędu zamiast pliku.",
            StudiaOnlineBulkDownloader.htmlResponseErrorMessage("text/html; charset=UTF-8", html)
        )
    }

    @Test fun genericHtmlPageWithHttp200IsRejectedRatherThanSaved() {
        val html = "<html><body>Witamy w serwisie</body></html>".toByteArray()
        assertEquals(
            "Serwis zwrócił stronę HTML zamiast pliku. Materiał nie został zapisany.",
            StudiaOnlineBulkDownloader.htmlResponseErrorMessage("text/html", html)
        )
    }

    @Test fun htmlLoginPageWithHttp200IsReportedAsAuthenticationRequirement() {
        val html = "<html><body>Zaloguj się</body></html>".toByteArray()
        assertEquals(
            "Serwis zwrócił stronę logowania zamiast pliku. Sprawdź wymagane uprawnienia lub sesję.",
            StudiaOnlineBulkDownloader.htmlResponseErrorMessage("text/html", html)
        )
    }

    @Test fun http401And403AreReportedAsAccessErrors() {
        assertTrue(StudiaOnlineBulkDownloader.describeFailure(
            TorHttpException(401, null, "HTTP 401")
        ).contains("odmówił dostępu"))
        assertTrue(StudiaOnlineBulkDownloader.describeFailure(
            TorHttpException(403, null, "HTTP 403")
        ).contains("odmówił dostępu"))
    }

    @Test fun retryAfterHugeDeltaSecondsIsCappedWithoutOverflow() {
        assertEquals(
            300_000L,
            StudiaOnlineBulkDownloader.retryAfterDelayMillis("999999999999999999999999", 0L)
        )
    }

    @Test fun binaryMaterialIsNotRejectedByHtmlResponseDetection() {
        val binary = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        assertEquals(null, StudiaOnlineBulkDownloader.notFoundPageMessage("application/pdf", binary))
    }

    @Test fun http404IsReportedAsMissingMaterial() {
        assertEquals(
            "Materiał nie został znaleziony (HTTP 404).",
            StudiaOnlineBulkDownloader.describeFailure(TorHttpException(404, null, "HTTP 404"))
        )
    }

    @Test fun http410IsReportedAsRemovedMaterial() {
        assertEquals(
            "Materiał został usunięty z serwisu (HTTP 410).",
            StudiaOnlineBulkDownloader.describeFailure(TorHttpException(410, null, "HTTP 410"))
        )
    }

    @Test fun otherHttpFailuresKeepTheirTransportMessage() {
        assertEquals(
            "Tor HTTP request failed: HTTP 503",
            StudiaOnlineBulkDownloader.describeFailure(TorHttpException(503, "10", "Tor HTTP request failed: HTTP 503"))
        )
    }

    @Test fun retryAfterDeltaSecondsIsConvertedToMilliseconds() {
        assertEquals(5_000L, StudiaOnlineBulkDownloader.retryAfterDelayMillis("5", 1_000L))
    }

    @Test fun retryAfterHttpDateIsConvertedToRemainingDelay() {
        val now = java.time.ZonedDateTime.parse(
            "Wed, 21 Oct 2015 07:27:00 GMT",
            java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
        ).toInstant().toEpochMilli()
        assertEquals(
            120_000L,
            StudiaOnlineBulkDownloader.retryAfterDelayMillis("Wed, 21 Oct 2015 07:29:00 GMT", now)
        )
    }

    @Test fun retryAfterPastDateDoesNotCreateNegativeDelay() {
        assertEquals(
            0L,
            StudiaOnlineBulkDownloader.retryAfterDelayMillis("Wed, 21 Oct 2015 07:29:00 GMT", 2_000_000_000_000L)
        )
    }

    @Test fun statusReportsRemainingItems() {
        val progress = StudiaOnlineDownloadProgress(2, 5, 3, listOf(1, 2), emptyMap(), false, false)
        assertEquals(3, progress.remaining)
        assertFalse(progress.finished)
        assertTrue(progress.succeeded.contains(2))
    }
}
