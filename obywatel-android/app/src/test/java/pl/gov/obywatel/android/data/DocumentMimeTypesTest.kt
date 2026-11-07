// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentMimeTypesTest {
    @Test
    fun acceptsPdfDocxAndRasterFormatsSupportedByPreviewDecoder() {
        listOf(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/bmp"
        ).forEach { mimeType ->
            assertTrue("$mimeType should be supported", isSupportedDocumentMimeType(mimeType))
        }
    }

    @Test
    fun normalizesMimeTypeCaseAndWhitespace() {
        assertTrue(isSupportedDocumentMimeType(" IMAGE/PNG "))
    }

    @Test
    fun rejectsImageFormatsNotSupportedByBitmapFactoryPreview() {
        listOf(
            "image/svg+xml",
            "image/tiff",
            "image/heic",
            "image/avif",
            "image/unknown"
        ).forEach { mimeType ->
            assertFalse("$mimeType should be rejected", isSupportedDocumentMimeType(mimeType))
        }
    }

    @Test
    fun rejectsUnrelatedMimeTypes() {
        listOf(
            "text/plain",
            "application/zip",
            "application/octet-stream",
            ""
        ).forEach { mimeType ->
            assertFalse("$mimeType should be rejected", isSupportedDocumentMimeType(mimeType))
        }
    }
}
