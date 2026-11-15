// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException

/**
 * Wspólny zapis bajtów raportu do dokumentu wskazanego przez systemowy selektor SAF.
 * Tryb "wt" jawnie zastępuje wcześniejszą zawartość pliku, zamiast dopisywać dane.
 */
object FinancialReportSafWriter {
    @Throws(IOException::class)
    fun write(contentResolver: ContentResolver, uri: Uri, bytes: ByteArray) {
        val output = contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Dostawca dokumentów nie udostępnił strumienia zapisu.")
        output.use {
            it.write(bytes)
            it.flush()
        }
    }
}
