// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("my flag", item.displayName)
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
    fun includesSupportedPngAndExcludesRowsWithoutResolvableDownloadUrl() {
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
        val parsed = parseInstitutionalSymbolCsv(header + "\n" + png + "\n" + missing)
        assertEquals(listOf("logo.png"), parsed.map { it.path })
        assertEquals(
            "https://raw.githubusercontent.com/org/repo/main/logo.png",
            parsed.single().downloadUrl
        )
    }
    @Test
    fun svgSecurityGateRejectsEventHandlersAndActiveElements() {
        val unsafe = listOf(
            """<svg xmlns="http://www.w3.org/2000/svg" onload="alert(1)"></svg>""",
            """<svg><script>alert(1)</script></svg>""",
            """<svg><foreignObject /></svg>""",
            """<svg><animate attributeName="x" /></svg>""",
            """<svg xml:base="https://evil.example/"></svg>"""
        )
        unsafe.forEach { markup ->
            try {
                SvgSafetyValidator.validateMarkupSecurity(markup)
                throw AssertionError("Niebezpieczny SVG został zaakceptowany: $markup")
            } catch (_: IllegalArgumentException) { }
        }
    }

    @Test
    fun svgSecurityGateRejectsRemoteCssButAllowsLocalFragments() {
        val unsafe = listOf(
            """<svg><path style="fill:url(https://evil.example/a.svg#x)" /></svg>""",
            """<svg><style>@import url(https://evil.example/a.css);</style></svg>""",
            """<svg><image href="data:image/svg+xml;base64,PHN2Zz4=" /></svg>"""
        )
        unsafe.forEach { markup ->
            try {
                SvgSafetyValidator.validateMarkupSecurity(markup)
                throw AssertionError("Niebezpieczny SVG został zaakceptowany: $markup")
            } catch (_: IllegalArgumentException) { }
        }
        SvgSafetyValidator.validateMarkupSecurity(
            """<svg xmlns="http://www.w3.org/2000/svg"><defs><linearGradient id="g" /></defs><path fill="url(#g)" /></svg>"""
        )
    }


    @Test
    fun classifiesInsigniaAndInstitutionalSymbolsUnderIconsSymbolsCategory() {
        val candidate = InstitutionalSymbolCandidate(
            sourceId = "polish-insignia",
            repository = "Wikimedia Commons",
            ref = "main",
            path = "symbols/Polish_Police_badge.svg",
            group = "institutional_insignia",
            rightsStatus = "per_file_metadata_review_required",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Polish_Police_badge.svg",
            downloadUrl = "https://upload.wikimedia.org/wikipedia/commons/a/ab/Polish_Police_badge.svg",
            license = "",
            licenseUrl = "",
            author = "",
            credit = ""
        )
        assertEquals("Symbole", institutionalSymbolCategory(candidate))
    }

    @Test
    fun loadsAllSelectedSourceCollectionsAndKeepsLifecycleStatesSeparate() {
        val header = "collection_id,label,provider,collection_ref,source_page_url,discovery_api_url,observed_size,source_formats,rights_policy,rights_status,discovery_method,direct_file_pattern_or_notes,checked_at,refresh_interval_days,notes,discovery_state,verification_state,download_state,storage_state,availability_state"
        val row = csvRow(
            "commons_pl_gov_svg", "Polskie instytucje", "Wikimedia Commons", "Category:Gov",
            "https://commons.wikimedia.org/wiki/Category:Gov",
            "https://commons.wikimedia.org/w/api.php?action=query", "52", "SVG",
            "Per-file license", "reference_only", "categorymembers API", "imageinfo.url",
            "2026-11-22", "30", "Check each file", "source_registered", "per_file_pending",
            "not_downloaded", "not_saved", "not_available"
        )
        val result = parseInstitutionalSymbolCollectionsCsv(header + "\n" + row)
        assertEquals(1, result.size)
        assertEquals("52", result.single().observedSize)
        assertEquals("source_registered", result.single().discoveryState)
        assertEquals("per_file_pending", result.single().verificationState)
        assertEquals("not_downloaded", result.single().downloadState)
        assertEquals("not_saved", result.single().storageState)
        assertEquals("not_available", result.single().availabilityState)
    }

    @Test
    fun rightsMetadataDoesNotBlockInstitutionalSymbolDownloads() {
        val referenceOnly = InstitutionalSymbolCandidate(
            "commons", "Wikimedia Commons", "main", "File:coat.svg", "polish_arms",
            "per_file_metadata_review_required", "https://commons.wikimedia.org/wiki/File:coat.svg",
            "https://upload.wikimedia.org/a/coat.svg", "CC BY-SA 4.0",
            "https://creativecommons.org/licenses/by-sa/4.0/", "Artist", "Credit"
        )
        val approvedMit = referenceOnly.copy(
            rightsStatus = "MIT",
            license = "MIT",
            licenseUrl = "https://github.com/lipis/flag-icons/blob/main/LICENSE"
        )
        assertEquals(true, institutionalSymbolCanDownload(referenceOnly))
        assertEquals(true, institutionalSymbolCanDownload(approvedMit))
    }


    @Test
    fun mapsUpstreamGroupsIntoExistingGraphicResourceCategories() {
        fun candidate(group: String, path: String, source: String = "Wikimedia Commons") =
            InstitutionalSymbolCandidate("source", source, "main", path, group, "reference_only",
                "https://example.org/source", "https://upload.wikimedia.org/wikipedia/commons/a/file.svg",
                "", "", "", "")
        assertEquals("Flagi", institutionalSymbolCategory(candidate("international_organization_flags", "flags/UN.svg")))
        assertEquals("Herby", institutionalSymbolCategory(candidate("polish_city_arms", "city/arms.svg")))
        assertEquals("Logotypy", institutionalSymbolCategory(candidate("NGO_and_humanitarian", "organization/logo.svg")))
        assertEquals("Ikony", institutionalSymbolCategory(candidate("ui_icons", "user.svg", "Lucide")))
        assertEquals("Tła", institutionalSymbolCategory(candidate("background_patterns", "backgrounds/waves.svg")))
    }

    @Test
    fun acceptsDirectSvgFilesFromAdditionalGraphicDatabases() {
        val header = "source_id,repository,ref,path,group,rights_status,import_status,source_url,download_url,license_short_name,license_url,author,attribution_required,source_credit,commons_categories"
        val row = csvRow(
            "openmoji", "OpenMoji", "pinned-release", "person.svg", "ui_icons",
            "rights_review_pending", "not_imported", "https://openmoji.org/data/color/svg/1F464.svg",
            "https://openmoji.org/data/color/svg/1F464.svg", "CC BY-SA 4.0",
            "https://creativecommons.org/licenses/by-sa/4.0/", "OpenMoji", "true", "OpenMoji", ""
        )
        val item = parseInstitutionalSymbolCsv(header + "\n" + row).single()
        assertEquals("https://openmoji.org/data/color/svg/1F464.svg", item.downloadUrl)
        assertTrue(institutionalSymbolCanDownload(item))
        assertEquals("Ikony", institutionalSymbolCategory(item))
    }

    @Test
    fun rejectsNonHttpsAndUntrustedDirectGraphicHosts() {
        val base = InstitutionalSymbolCandidate(
            "test", "Test", "main", "icon.svg", "ui_icons", "reference_only",
            "https://example.org/source", "http://evil.example/icon.svg", "", "", "", ""
        )
        assertFalse(institutionalSymbolCanDownload(base))
        assertFalse(institutionalSymbolCanDownload(base.copy(downloadUrl = "https://evil.example/icon.svg")))
    }

    @Test
    fun recognizesSvgAndPngButRejectsUnsupportedRasterFormats() {
        assertEquals("SVG", institutionalSymbolFormat("symbols/crest.svg"))
        assertEquals("PNG", institutionalSymbolFormat("logos/institution.PNG"))
        assertEquals(null, institutionalSymbolFormat("logos/institution.jpg"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPngPayloadWithoutValidSignature() {
        validateInstitutionalSymbolPng(ByteArray(64))
    }

    @Test
    fun parsesIconifyCollectionsWithPerSetLicenseAndDirectJsonSource() {
        val header = "prefix,name,icon_count,author,author_url,license_title,license_spdx,license_url,category,source_page_url,download_url,source_format,rights_status,discovery_status"
        val row = csvRow(
            "example", "Example Icons", "25", "Author", "https://github.com/author",
            "MIT", "MIT", "https://github.com/author/repo/blob/main/LICENSE", "Symbols",
            "https://github.com/iconify/icon-sets/blob/master/json/example.json",
            "https://raw.githubusercontent.com/iconify/icon-sets/master/json/example.json",
            "Iconify JSON", "per_collection_license_review", "source_registered"
        )
        val set = parseIconifyCollectionIndex(header + "\n" + row).single()
        assertEquals("example", set.prefix)
        assertEquals(25, set.iconCount)
        assertEquals("MIT", set.licenseSpdx)
        assertTrue(set.downloadUrl.endsWith("/example.json"))
        assertEquals("per_collection_license_review", set.rightsStatus)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIconifyIndexWithUnsafeSourceUrl() {
        val header = "prefix,name,icon_count,author,author_url,license_title,license_spdx,license_url,category,source_page_url,download_url,source_format,rights_status,discovery_status"
        val row = csvRow("x","X","1","","","","","","","https://example.org/x","http://evil.example/x.json","JSON","review","found")
        parseIconifyCollectionIndex(header + "\n" + row)
    }

    @Test
    fun bundledUpstreamCandidateIndexIsParseableAndAllowsDownloadPendingManualRightsReview() {
        val catalog = java.io.File("src/main/assets/identity-symbols/github-symbol-candidates.csv")
        assertTrue("Indeks kandydatów musi być dołączony do aplikacji.", catalog.isFile)
        val candidates = parseInstitutionalSymbolCsv(catalog.readText(Charsets.UTF_8))
        assertTrue("Indeks powinien zawierać ponad 2 500 pozycji SVG/PNG.", candidates.size > 2500)
        val additionalCatalog = java.io.File("src/main/assets/identity-symbols/additional-graphic-files.csv")
        assertTrue("Dodatkowy indeks bezpośrednich plików graficznych musi być dołączony do aplikacji.", additionalCatalog.isFile)
        val external = parseInstitutionalSymbolCsv(additionalCatalog.readText(Charsets.UTF_8))
        assertEquals("Dodatkowe bazy powinny udostępniać 102 bezpośrednie pliki SVG.", 102, external.size)
        val allCandidates = candidates + external
        assertEquals(
            "Kandydaci muszą mieć stabilne, unikalne identyfikatory.",
            allCandidates.size,
            allCandidates.map { it.sourceId + ":" + it.path }.distinct().size
        )
        assertTrue(allCandidates.all { it.downloadUrl.startsWith("https://") })
        assertTrue(allCandidates.all { institutionalSymbolFormat(it.path) in setOf("SVG", "PNG") })

        val commons = candidates.first { it.sourceId == "wikimedia-commons" }
        assertTrue("Status praw nie może blokować pobrania pliku Commons.", institutionalSymbolCanDownload(commons))
        val flag = candidates.first { it.group.contains("flag", ignoreCase = true) }
        assertTrue("Status praw nie może blokować pobrania flagi.", institutionalSymbolCanDownload(flag))
        assertTrue("Dodatkowe pliki z baz graficznych muszą być dostępne do pobrania.", external.all { institutionalSymbolCanDownload(it) })
    }
    @Test
    fun registersAllEighteenGraphicSourceCollections() {
        val file = java.io.File("src/main/assets/identity-symbols/collections.csv")
        assertTrue("Rejestr 18 kolekcji źródłowych musi być dołączony.", file.isFile)
        val sources = parseInstitutionalSymbolCollectionsCsv(file.readText(Charsets.UTF_8))
        assertEquals(18, sources.size)
        assertEquals(15, sources.count { it.provider.equals("Wikimedia Commons", true) })
        assertTrue(sources.any { it.id == "github_simple_icons" })
        assertTrue(sources.any { it.id == "github_flag_icons" })
        assertTrue(sources.any { it.id == "github_iconify_sets" })
    }

    @Test
    fun iconifyCollectionCreatesOneSafeDownloadableSvgCandidate() {
        val collection = IconifyCollectionSource(
            prefix = "mdi", name = "Material Design Icons", iconCount = 1000,
            author = "Iconify", authorUrl = "https://iconify.design",
            licenseTitle = "Apache 2.0", licenseSpdx = "Apache-2.0",
            licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
            category = "ui_icons", sourcePageUrl = "https://icon-sets.iconify.design/mdi/",
            downloadUrl = "https://raw.githubusercontent.com/iconify/icon-sets/master/json/mdi.json",
            sourceFormat = "Iconify JSON", rightsStatus = "per_collection_license_review",
            discoveryStatus = "source_registered"
        )
        val item = iconifyCandidate(collection, "account-circle")
        assertEquals("mdi/account-circle.svg", item.path)
        assertEquals("https://api.iconify.design/mdi/account-circle.svg", item.downloadUrl)
        assertTrue(institutionalSymbolCanDownload(item))
        assertEquals("Ikony", institutionalSymbolCategory(item))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsafeIconifyIconName() {
        val collection = IconifyCollectionSource(
            "mdi", "Material Design Icons", 1, "Iconify", "https://iconify.design",
            "Apache 2.0", "Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0",
            "ui_icons", "https://icon-sets.iconify.design/mdi/",
            "https://raw.githubusercontent.com/iconify/icon-sets/master/json/mdi.json",
            "Iconify JSON", "review", "source_registered"
        )
        iconifyCandidate(collection, "../evil")
    }

    @Test
    fun recognizesXmlGraphicResources() {
        assertEquals("XML", institutionalSymbolFormat("drawable/ic_emblem.xml"))
    }

}
