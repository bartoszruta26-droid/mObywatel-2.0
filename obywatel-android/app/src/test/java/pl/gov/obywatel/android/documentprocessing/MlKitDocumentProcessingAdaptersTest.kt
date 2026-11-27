// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.documentprocessing

import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.gov.obywatel.android.RecognizedBarcode
import pl.gov.obywatel.android.RecognizedImageBounds
import pl.gov.obywatel.android.RecognizedText
import pl.gov.obywatel.android.RecognizedTextBlock
import pl.gov.obywatel.android.RecognizedTextLine

class MlKitDocumentProcessingAdaptersTest {

    @Test
    fun mapsOcrHierarchyConfidenceAndCoordinatesWithoutFlattening() {
        val mapped = RecognizedText(
            text = "JAN KOWALSKI",
            blocks = listOf(
                RecognizedTextBlock(
                    text = "JAN KOWALSKI",
                    bounds = RecognizedImageBounds(1, 2, 101, 22),
                    lines = listOf(
                        RecognizedTextLine(
                            text = "JAN KOWALSKI",
                            confidence = 0.97f,
                            bounds = RecognizedImageBounds(2, 3, 100, 20)
                        )
                    )
                )
            )
        ).toOcrText()

        assertEquals("JAN KOWALSKI", mapped.text)
        assertEquals(1, mapped.blocks.size)
        assertEquals(1, mapped.blocks.single().lines.size)
        assertEquals(0.97f, mapped.blocks.single().lines.single().confidence)
        assertEquals(2f, mapped.blocks.single().lines.single().bounds?.left)
        assertEquals(20f, mapped.blocks.single().lines.single().bounds?.bottom)
        assertEquals(101f, mapped.blocks.single().bounds?.right)
    }

    @Test
    fun mapsMissingOcrGeometryAndConfidenceAsNull() {
        val mapped = RecognizedText(
            text = "TEKST",
            blocks = listOf(
                RecognizedTextBlock(
                    text = "TEKST",
                    bounds = null,
                    lines = listOf(RecognizedTextLine("TEKST", null, null))
                )
            )
        ).toOcrText()

        assertNull(mapped.blocks.single().bounds)
        assertNull(mapped.blocks.single().lines.single().bounds)
        assertNull(mapped.blocks.single().lines.single().confidence)
    }

    @Test
    fun mapsBarcodeFormatAndValueTypeToStableNames() {
        val mapped = RecognizedBarcode(
            rawValue = "https://example.test",
            displayValue = "example",
            format = Barcode.FORMAT_QR_CODE,
            valueType = Barcode.TYPE_URL,
            bounds = RecognizedImageBounds(4, 5, 40, 45)
        ).toBarcodeValue()

        assertEquals("https://example.test", mapped.rawValue)
        assertEquals("example", mapped.displayValue)
        assertEquals("QR_CODE", mapped.format)
        assertEquals("URL", mapped.valueType)
        assertEquals(4f, mapped.bounds?.left)
        assertEquals(5f, mapped.bounds?.top)
        assertEquals(40f, mapped.bounds?.right)
        assertEquals(45f, mapped.bounds?.bottom)
    }

    @Test
    fun preservesUnknownBarcodeConstantsInsteadOfDiscardingThem() {
        val mapped = RecognizedBarcode(
            rawValue = "payload",
            displayValue = null,
            format = Int.MAX_VALUE,
            valueType = Int.MAX_VALUE,
            bounds = null
        ).toBarcodeValue()

        assertEquals("UNKNOWN_${Int.MAX_VALUE}", mapped.format)
        assertEquals("UNKNOWN_${Int.MAX_VALUE}", mapped.valueType)
    }
}
