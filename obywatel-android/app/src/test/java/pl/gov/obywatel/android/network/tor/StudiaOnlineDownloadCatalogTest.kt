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

    @Test(expected = IllegalArgumentException::class)
    fun acceptsOnlyTwoThousandItemsPerRange() {
        StudiaOnlineDownloadCatalog.fromRange(1, 2_001)
    }

    @Test fun statusReportsRemainingItems() {
        val progress = StudiaOnlineDownloadProgress(2, 5, 3, listOf(1, 2), emptyMap(), false, false)
        assertEquals(3, progress.remaining)
        assertFalse(progress.finished)
        assertTrue(progress.succeeded.contains(2))
    }
}
