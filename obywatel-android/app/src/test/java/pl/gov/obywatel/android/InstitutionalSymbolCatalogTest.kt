// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstitutionalSymbolCatalogTest {
    private val header = "source_id,repository,ref,path,group,rights_status,import_status,source_url,download_url,license_short_name,license_url,author,attribution_required,source_credit,commons_categories"

    private fun csvRow(vararg fields: String): String =
        fields.joinToString(",") { "\"" + it.replace("\"", "\"\"") + "\"" }

    @Test
    fun derivesRawGithubUrlFromBlobSourceAndEncodesPath() {
        val row = csvRow(
            "flag-icons", "lipis/flag-icons", "main", "flags/4x3/my flag.svg",
            "flags", "MIT_plus_flag_law_review", "not_imported",
            "https://github.com/lipis/flag-icons/blob/main/flags/4x3/my flag.svg",
            "", "MIT", "", "", "false", "", ""
        )
        val item = parseInstitutionalSymbolCsv(header + "\n" + row).single()
        assertEquals("https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/my%20flag.svg", item.downloadUrl)
        assertEquals("flags/4x3/my flag", item.displayName)
    }

    @Test
    fun preservesDirectCommonsDownloadUrlAndRightsMetadata() {
        val row = csvRow(
            "wikimedia-commons", "Wikimedia Commons", "live-category-snapshot",
            "File:Coat of Arms.svg", "polish_city_arms",
            "per_file_metadata_review_required", "not_imported",
            "https://commons.wikimedia.org/wiki/File:Coat_of_Arms.svg",
            "https://upload.wikimedia.org/wikipedia/commons/a/ab/Coat.svg",
            "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
            "Artist", "true", "Source credit", "Category:SVG coats of arms"
        )
        val item = parseInstitutionalSymbolCsv(header + "\n" + row).single()
        assertEquals("https://upload.wikimedia.org/wikipedia/commons/a/ab/Coat.svg", item.downloadUrl)
        assertEquals("CC BY-SA 4.0", item.license)
        assertEquals("Artist", item.author)
        assertTrue(item.rightsStatus.contains("per_file"))
    }

    @Test
    fun excludesRasterFilesAndRowsWithoutResolvableDownloadUrl() {
        val png = csvRow(
            "source", "org/repo", "main", "logo.png", "logos", "reference_only",
            "not_imported", "https://github.com/org/repo/blob/main/logo.png",
            "", "", "", "", "", "", ""
        )
        val missing = csvRow(
            "source", "org/repo", "main", "logo.svg", "logos", "reference_only",
            "not_imported", "https://example.org/not-a-github-blob/logo.svg",
            "", "", "", "", "", "", ""
        )
        assertTrue(parseInstitutionalSymbolCsv(header + "\n" + png + "\n" + missing).isEmpty())
    }
}
