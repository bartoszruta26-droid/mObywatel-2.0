// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegitimacyPreviewSettingsTest {
    @Test
    fun sharedAppearanceOverridesLayoutButPreservesDocumentAssetsAndFields() {
        val document = LegitimacyPreviewSettings(
            showCard = true,
            tileBackgroundAssetId = "tile-student",
            crestAssetId = "crest-student",
            flagAssetId = "flag-student",
            backgroundAssetId = "background-student",
            logoAssetId = "logo-student",
            visibleFields = setOf("firstNames", "lastName"),
            visibleFieldsConfigured = true,
            orientation = "PIONOWA",
            graphicScale = 1.0f,
            fontFamily = "sans",
            fontColor = "#FF7A1F2B"
        )
        val shared = LegitimacyPreviewSettings(
            orientation = "POZIOMA",
            showCard = false,
            showReverse = true,
            showPhoto = false,
            showFlag = false,
            showCrest = true,
            showLogo = false,
            showBackground = true,
            graphicScale = 1.4f,
            graphicRotation = 12f,
            graphicOffsetX = 8f,
            crestScale = 1.8f,
            crestRotation = 23f,
            crestOffsetX = 9f,
            crestOffsetY = -4f,
            flagScale = 0.7f,
            flagRotation = -12f,
            flagOffsetX = -6f,
            flagOffsetY = 5f,
            labelScale = 1.2f,
            valueScale = 0.9f,
            fontFamily = "serif",
            fontColor = "#FF17365D"
        )

        val result = mergeSharedLegitimacyAppearance(document, shared)

        assertEquals("POZIOMA", result.orientation)
        assertEquals(true, result.showCard)
        assertEquals("tile-student", result.tileBackgroundAssetId)
        assertEquals(true, result.showReverse)
        assertEquals(1.4f, result.graphicScale)
        assertEquals(12f, result.graphicRotation)
        assertEquals(1.8f, result.crestScale)
        assertEquals(23f, result.crestRotation)
        assertEquals(9f, result.crestOffsetX)
        assertEquals(-4f, result.crestOffsetY)
        assertEquals(0.7f, result.flagScale)
        assertEquals(-12f, result.flagRotation)
        assertEquals(-6f, result.flagOffsetX)
        assertEquals(5f, result.flagOffsetY)
        assertEquals("serif", result.fontFamily)
        assertEquals("#FF7A1F2B", result.fontColor)
        assertEquals("crest-student", result.crestAssetId)
        assertEquals("flag-student", result.flagAssetId)
        assertEquals("background-student", result.backgroundAssetId)
        assertEquals("logo-student", result.logoAssetId)
        assertEquals(setOf("firstNames", "lastName"), result.visibleFields)
        assertTrue(result.visibleFieldsConfigured)
    }
    @Test
    fun sharedLayoutOverridesPerDocumentGeometryButPreservesDocumentAssets() {
        val document = LegitimacyPreviewSettings(
            showCard = false,
            tileBackgroundAssetId = "tile-specific",
            crestAssetId = "crest-specific",
            flagAssetId = "flag-specific",
            backgroundAssetId = "background-specific",
            logoAssetId = "logo-specific",
            orientation = "PIONOWA",
            showPhoto = false,
            showFlag = true,
            imageFilter = "grayscale",
            graphicScale = 1.7f,
            crestScale = 2.1f,
            crestRotation = 31f,
            crestOffsetX = 14f,
            crestOffsetY = -7f,
            flagScale = 0.6f,
            flagRotation = -19f,
            flagOffsetX = -12f,
            flagOffsetY = 8f,
            fontColor = "#FF7A1F2B",
            individualAppearanceConfigured = true
        )
        val shared = LegitimacyPreviewSettings(
            orientation = "POZIOMA",
            showPhoto = true,
            showFlag = false,
            imageFilter = "sepia",
            graphicScale = 0.8f,
            crestScale = 1.3f,
            crestRotation = 12f,
            crestOffsetX = 4f,
            crestOffsetY = 5f,
            flagScale = 0.9f,
            flagRotation = -8f,
            flagOffsetX = 6f,
            flagOffsetY = -3f,
            fontColor = "#FF17365D"
        )

        val result = mergeSharedLegitimacyAppearance(document, shared)

        assertEquals("POZIOMA", result.orientation)
        assertEquals(false, result.showCard)
        assertEquals("tile-specific", result.tileBackgroundAssetId)
        assertEquals(true, result.showPhoto)
        assertEquals(false, result.showFlag)
        assertEquals("sepia", result.imageFilter)
        assertEquals(0.8f, result.graphicScale)
        assertEquals(1.3f, result.crestScale)
        assertEquals(12f, result.crestRotation)
        assertEquals(4f, result.crestOffsetX)
        assertEquals(5f, result.crestOffsetY)
        assertEquals(0.9f, result.flagScale)
        assertEquals(-8f, result.flagRotation)
        assertEquals(6f, result.flagOffsetX)
        assertEquals(-3f, result.flagOffsetY)
        assertEquals("#FF7A1F2B", result.fontColor)
        assertEquals("crest-specific", result.crestAssetId)
        assertEquals("flag-specific", result.flagAssetId)
        assertEquals("background-specific", result.backgroundAssetId)
        assertEquals("logo-specific", result.logoAssetId)
    }
}

