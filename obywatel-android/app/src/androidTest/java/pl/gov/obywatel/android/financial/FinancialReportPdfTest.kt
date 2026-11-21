// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pl.gov.obywatel.android.FinancialTransaction
import pl.gov.obywatel.android.FinancialTransactionType

@RunWith(AndroidJUnit4::class)
class FinancialReportPdfTest {
    private val period = FinancialReportPeriod(
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2027-01-01T00:00:00Z")
    )

    private fun pdfPageCount(report: FinancialReport): Int {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "financial-report-${System.nanoTime()}.pdf")
        try {
            val bytes = FinancialReportPdf.encode(report)
            assertTrue(bytes.size > 100)
            assertTrue(bytes.copyOfRange(0, 5).decodeToString() == "%PDF-")
            file.writeBytes(bytes)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    assertTrue(renderer.pageCount >= 1)
                    for (pageIndex in 0 until renderer.pageCount) {
                        renderer.openPage(pageIndex).use { page ->
                            assertTrue(page.width > 0)
                            assertTrue(page.height > page.width)
                            val bitmap = Bitmap.createBitmap(
                                page.width,
                                page.height,
                                Bitmap.Config.ARGB_8888
                            )
                            try {
                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                assertTrue(bitmap.width > 0 && bitmap.height > 0)
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    }
                    return renderer.pageCount
                }
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun emptyReportProducesAReadableSinglePagePdf() {
        val report = FinancialReportBuilder.build(period, emptyList(), emptyList(), emptyList())
        assertEquals(1, pdfPageCount(report))
    }

    @Test
    fun longReportIsSplitIntoMultipleReadablePages() {
        val transactions = (1..140).map { index ->
            StoredFinancialTransaction(
                transaction = FinancialTransaction(
                    id = "pdf-tx-$index",
                    executedAt = Instant.parse("2026-06-01T12:00:00Z").plusSeconds(index.toLong()),
                    type = FinancialTransactionType.ADJUSTMENT,
                    fromAssetCode = null,
                    fromAmount = null,
                    toAssetCode = "PLN",
                    toAmount = BigDecimal("123.45"),
                    rawDescription = "Opis zdarzenia testowego numer $index — pozycja raportu wielostronicowego"
                ),
                accountingEntryIds = emptyList()
            )
        }
        val report = FinancialReportBuilder.build(period, transactions, emptyList(), emptyList())
        assertTrue(pdfPageCount(report) > 1)
    }
}
