// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinancialReportSafExportTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val directory = File(context.filesDir, "donation-attachments")

    private fun writeThroughContentResolver(fileName: String, bytes: ByteArray) {
        assertTrue(directory.exists() || directory.mkdirs())
        val target = File(directory, fileName)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.ocrfiles",
            target
        )
        context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(bytes) }
        assertArrayEquals(bytes, target.readBytes())
        target.delete()
    }

    @Test
    fun writesCsvBytesThroughContentUriWithoutChangingTheirEncoding() {
        val csv = "\uFEFF\"typ_rekordu\";\"kwota\"\r\n\"TRANSACTION\";\"12.50\"\r\n"
            .toByteArray(Charsets.UTF_8)

        writeThroughContentResolver("report-saf-test.csv", csv)
    }

    @Test
    fun writesPdfBytesThroughContentUriAndPreservesPdfSignature() {
        val pdf = "%PDF-1.4\n% SAF test\n".toByteArray(Charsets.US_ASCII)

        writeThroughContentResolver("report-saf-test.pdf", pdf)
    }

    @Test
    fun reportsOutputProviderFailureInsteadOfTreatingItAsSuccessfulExport() {
        val missingProviderUri = Uri.parse(
            "content://pl.gov.obywatel.android.prototype.missing/report.csv"
        )

        assertThrows(Exception::class.java) {
            context.contentResolver.openOutputStream(missingProviderUri)
        }
    }
}
