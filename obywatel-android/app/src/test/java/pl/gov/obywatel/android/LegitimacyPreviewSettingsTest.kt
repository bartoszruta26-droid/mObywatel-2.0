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
        assertEquals(false, result.showCard)
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
    fun individualAppearanceOverridesSharedOrientationFilterAndLayout() {
        val document = LegitimacyPreviewSettings(
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
            individualAppearanceConfigured = true
        )
        val shared = LegitimacyPreviewSettings(
            orientation = "POZIOMA",
            showPhoto = true,
            showFlag = false,
            imageFilter = "sepia",
            graphicScale = 0.8f
        )

        val result = mergeSharedLegitimacyAppearance(document, shared)

        assertEquals("PIONOWA", result.orientation)
        assertEquals(false, result.showPhoto)
        assertEquals(true, result.showFlag)
        assertEquals("grayscale", result.imageFilter)
        assertEquals(1.7f, result.graphicScale)
        assertEquals(2.1f, result.crestScale)
        assertEquals(31f, result.crestRotation)
        assertEquals(14f, result.crestOffsetX)
        assertEquals(-7f, result.crestOffsetY)
        assertEquals(0.6f, result.flagScale)
        assertEquals(-19f, result.flagRotation)
        assertEquals(-12f, result.flagOffsetX)
        assertEquals(8f, result.flagOffsetY)
    }
}

