// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Test-only provider that emits a valid PDF signature and then fails the pipe.
 * It is declared only in src/androidTest/AndroidManifest.xml.
 */
class FailingStreamContentProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = "application/pdf"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME)
        return MatrixCursor(columns).apply {
            addRow(columns.map { column ->
                when (column) {
                    OpenableColumns.DISPLAY_NAME -> "controlled-failure.pdf"
                    OpenableColumns.SIZE -> 64L
                    else -> null
                }
            })
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Only read access is supported.")
        val (readEnd, writeEnd) = ParcelFileDescriptor.createPipe()
        Thread({
            try {
                FileOutputStream(writeEnd.fileDescriptor).apply {
                    write("%PDF-1.7\\npartial-payload".toByteArray(Charsets.US_ASCII))
                    flush()
                }
                writeEnd.closeWithError("controlled read failure")
            } catch (error: IOException) {
                try {
                    writeEnd.closeWithError(error.message ?: "controlled read failure")
                } catch (_: IOException) {
                    // The consumer may already have closed the pipe.
                }
            }
        }, "failing-stream-provider").start()
        return readEnd
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Read-only test provider.")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Read-only test provider.")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw UnsupportedOperationException("Read-only test provider.")
}
