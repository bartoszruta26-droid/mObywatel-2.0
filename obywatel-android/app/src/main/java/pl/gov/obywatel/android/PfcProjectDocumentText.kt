// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context

internal const val PFC_PROJECT_DOCUMENT_ASSET = "pfc-project-document.md"

/**
 * Loads the full PFC document from app assets rather than embedding a very
 * large string in a JVM class constant.
 */
internal fun loadPfcProjectDocumentText(context: Context): String =
    context.assets.open(PFC_PROJECT_DOCUMENT_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
