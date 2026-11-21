// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Test

class AssetResourceHierarchyTest {
    @Test
    fun heraldryAndInstitutionalMarksAreGroupedIntoTheirRequestedSubgroups() {
        assertEquals("Herby / godła / logo", documentBrandAssetParentGroup("Herb"))
        assertEquals("Herby", documentBrandAssetSubgroup("Herb"))
        assertEquals("Herby / godła / logo", documentBrandAssetParentGroup("Godło"))
        assertEquals("Godła", documentBrandAssetSubgroup("Godło"))
        assertEquals("Herby / godła / logo", documentBrandAssetParentGroup("Logo"))
        assertEquals("Logo", documentBrandAssetSubgroup("Logo"))
    }

    @Test
    fun flagsBackgroundsAndIconsHaveDedicatedParentGroups() {
        assertEquals("Flagi", documentBrandAssetParentGroup("Flaga"))
        assertEquals("Flagi", documentBrandAssetSubgroup("Flaga"))
        assertEquals("Tła", documentBrandAssetParentGroup("Tło wektorowe"))
        assertEquals("Tła wektorowe", documentBrandAssetSubgroup("Tło wektorowe"))
        assertEquals("Tła", documentBrandAssetParentGroup("Tło graficzne"))
        assertEquals("Tła graficzne", documentBrandAssetSubgroup("Tło graficzne"))
        assertEquals("Tła", documentBrandAssetParentGroup("Tło dokumentu"))
        assertEquals("Ikony", documentBrandAssetParentGroup("Ikona wektorowa"))
        assertEquals("Ikony", documentBrandAssetParentGroup("Ikona wektorowa kafelka"))
        assertEquals("Ikony", documentBrandAssetParentGroup("Ikona"))
    }

    @Test
    fun commonsSvgDownloadsAreClassifiedIntoSupportedAssetKinds() {
        assertEquals("Flaga", commonsSvgAssetKind("National flag of Poland.svg"))
        assertEquals("Herb", commonsSvgAssetKind("Coat of arms of Warsaw.svg"))
        assertEquals("Godło", commonsSvgAssetKind("National emblem.svg"))
        assertEquals("Pieczęć", commonsSvgAssetKind("Official seal.svg"))
        assertEquals("Logo", commonsSvgAssetKind("Government logo.svg"))
    }

    @Test
    fun institutionalCatalogMapsEveryVisibleCategoryToAnAcceptedKind() {
        val examples = listOf(
            InstitutionalSymbolCandidate("f", "repo", "main", "flags/flag.svg", "flag", "", "", "", "", "", "", ""),
            InstitutionalSymbolCandidate("h", "repo", "main", "heraldry/coat-of-arms.svg", "heraldry", "", "", "", "", "", "", ""),
            InstitutionalSymbolCandidate("e", "repo", "main", "symbols/eagle-emblem.svg", "institution", "", "", "", "", "", "", ""),
            InstitutionalSymbolCandidate("l", "repo", "main", "logos/public-logo.svg", "government", "", "", "", "", "", "", "")
        )
        assertEquals(listOf("Flagi", "Herby", "Godła", "Logotypy"), examples.map(::institutionalSymbolCategory))
    }
}
