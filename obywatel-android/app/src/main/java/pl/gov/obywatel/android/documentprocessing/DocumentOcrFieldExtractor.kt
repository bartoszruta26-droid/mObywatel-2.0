// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import pl.gov.obywatel.android.DocumentOcrParser

/**
 * Bridges the existing conservative document parser into the shared engine.
 * Extracted values remain suggestions and always require user review.
 */
internal class DocumentOcrFieldExtractor : DocumentFieldExtractor {
    override fun extract(text: OcrText, documentTypeHint: String?): List<ExtractedField> {
        val category = documentTypeHint?.takeIf { it.isNotBlank() } ?: return emptyList()
        if (text.text.isBlank()) return emptyList()

        return DocumentOcrParser.extract(category, text.text).map { (key, value) ->
            ExtractedField(
                key = key,
                value = value,
                confidence = null,
                sourceBounds = null,
                requiresReview = true
            )
        }
    }
}
