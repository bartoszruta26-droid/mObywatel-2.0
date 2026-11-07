// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.data

/**
 * Formats accepted by the current preview implementation.
 *
 * BitmapFactory does not decode every image MIME type (notably SVG), so
 * accepting image/* here would persist documents that the preview cannot show.
 */
internal fun isSupportedDocumentMimeType(mimeType: String): Boolean {
    val normalized = mimeType.trim().lowercase()
    return normalized == "application/pdf" ||
        normalized == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
        normalized in RASTER_IMAGE_MIME_TYPES
}

private val RASTER_IMAGE_MIME_TYPES = setOf(
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/gif",
    "image/bmp"
)
