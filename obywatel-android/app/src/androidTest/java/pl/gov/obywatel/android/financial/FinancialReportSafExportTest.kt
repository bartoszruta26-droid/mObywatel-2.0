// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.time.Instant
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinancialReportSafExportTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val directory = File(context.filesDir, "donation-attachments")

    private fun emptyReport(): FinancialReport = FinancialReport(
        period = FinancialReportPeriod(
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2027-01-01T00:00:00Z")
        ),
        assetTotals = emptyList(),
        donationTotals = emptyList(),
        rows = emptyList()
    )

    private fun writeThroughContentResolver(fileName: String, bytes: ByteArray) {
        assertTrue(directory.exists() || directory.mkdirs())
        val target = File(directory, fileName)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.ocrfiles",
            target
        )
        FinancialReportSafWriter.write(context.contentResolver, uri, bytes)
        assertArrayEquals(bytes, target.readBytes())
        target.delete()
    }

    @Test
    fun writesActualCsvReportThroughContentUriWithoutChangingItsEncoding() {
        val csv = FinancialReportCsv.encode(emptyReport()).toByteArray(Charsets.UTF_8)

        writeThroughContentResolver("report-saf-test.csv", csv)

        assertTrue(
            csv.copyOfRange(0, 3).contentEquals(
                byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            )
        )
    }

    @Test
    fun writesActualPdfReportThroughContentUriAndPreservesPdfSignature() {
        val pdf = FinancialReportPdf.encode(emptyReport())

        writeThroughContentResolver("report-saf-test.pdf", pdf)

        assertTrue(pdf.copyOfRange(0, 5).decodeToString() == "%PDF-")
    }

    @Test
    fun reportsOutputProviderFailureInsteadOfTreatingItAsSuccessfulExport() {
        val missingProviderUri = Uri.parse(
            "content://pl.gov.obywatel.android.prototype.missing/report.csv"
        )

        assertThrows(Exception::class.java) {
            FinancialReportSafWriter.write(
                context.contentResolver,
                missingProviderUri,
                "test".toByteArray()
            )
        }
    }
}
