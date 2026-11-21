// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that bundled public institutional flag vectors are packaged,
 * discoverable in the asset library and renderable by Android's SVG pipeline.
 */
@RunWith(AndroidJUnit4::class)
class InstitutionalAssetResourcesTest {
    private val assets = listOf(
        "builtin_flag_poland_official_layout" to "Flaga państwowa Rzeczypospolitej Polskiej — układ ustawowy",
        "builtin_flag_european_union_official_layout" to "Flaga Unii Europejskiej — oficjalny układ 12 gwiazd",
        "builtin_flag_vatican_city_specification" to "Flaga Państwa Watykańskiego — układ i symbole według opisu urzędowego"
    )

    @Test
    fun bundledFlagsAreDiscoverableAndRenderable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val library = DocumentBrandStore.list(context)

        assets.forEach { (id, expectedLabel) ->
            val asset = library.firstOrNull { it.id == id }
            assertNotNull("Missing bundled institutional asset: $id", asset)
            assertEquals(expectedLabel, asset!!.label)
            assertEquals("Flaga", asset.kind)
            val bitmap = DocumentBrandStore.preview(context, id)
            assertNotNull("Could not render bundled SVG asset: $id", bitmap)
            assertTrue("Rendered asset has invalid dimensions: $id", bitmap!!.width > 0 && bitmap.height > 0)
            bitmap.recycle()
        }
    }

    @Test
    fun importsSvgWithXmlDeclarationAndStandardExternalDoctypeEvenWhenMimeIsGeneric() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val svg = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10"><path d="M0 0h10v10z"/></svg>"""
        val assetId = "test_svg_xml_prolog"
        try {
            val asset = DocumentBrandStore.importBytes(
                context = context,
                owner = "test",
                kind = "SVG",
                label = "SVG test",
                bytes = svg.toByteArray(Charsets.UTF_8),
                mimeType = "application/octet-stream",
                assetId = assetId
            )
            assertEquals("Logo", asset.kind)
            assertTrue(asset.file.endsWith(".svg.enc"))
            val preview = DocumentBrandStore.preview(context, assetId)
            assertNotNull("SVG with standard XML prolog should render", preview)
            preview!!.recycle()
        } finally {
            DocumentBrandStore.delete(context, assetId)
        }
    }

    @Test
    fun bundledFlagFilesArePackagedInTheApplicationAssets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(
            "institutions/poland-national-flag.svg",
            "institutions/european-union-flag.svg",
            "institutions/vatican-city-flag.svg"
        ).forEach { path ->
            context.assets.open(path).use { stream ->
                assertTrue("Empty bundled SVG: $path", stream.readBytes().isNotEmpty())
            }
        }
    }
}
