// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Test-only provider that emits part of a PDF and then fails the pipe.
 * Implemented in Java so its isolated test-APK process does not require Kotlin runtime classes.
 */
public final class FailingStreamContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "application/pdf";
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder
    ) {
        String[] columns = projection != null
                ? projection
                : new String[]{OpenableColumns.DISPLAY_NAME};
        MatrixCursor cursor = new MatrixCursor(columns);
        Object[] row = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) {
                row[i] = "controlled-failure.pdf";
            } else if (OpenableColumns.SIZE.equals(columns[i])) {
                row[i] = 64L;
            }
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Only read access is supported.");
        }
        final ParcelFileDescriptor[] pipe;
        try {
            pipe = ParcelFileDescriptor.createReliablePipe();
        } catch (IOException error) {
            throw new FileNotFoundException(error.getMessage());
        }
        final ParcelFileDescriptor readEnd = pipe[0];
        final ParcelFileDescriptor writeEnd = pipe[1];
        Thread writer = new Thread(() -> {
            try {
                FileOutputStream output = new FileOutputStream(writeEnd.getFileDescriptor());
                output.write("%PDF-1.7\npartial-payload".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                output.flush();
                writeEnd.closeWithError("controlled read failure");
            } catch (IOException error) {
                try {
                    writeEnd.closeWithError(
                            error.getMessage() != null ? error.getMessage() : "controlled read failure"
                    );
                } catch (IOException ignored) {
                    // The consumer may already have closed the pipe.
                }
            }
        }, "failing-stream-provider");
        writer.start();
        return readEnd;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read-only test provider.");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only test provider.");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only test provider.");
    }
}
