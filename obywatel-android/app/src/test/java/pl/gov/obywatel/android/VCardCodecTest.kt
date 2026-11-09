// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VCardCodecTest {
    @Test
    fun contactVCardUsesRfc6350VersionAndEscapesText() {
        val encoded = VCardCodec.encodeContact(mapOf(
            "FN" to "Jan Kowalski",
            "N" to "Kowalski;Jan;;;",
            "ORG" to "Fundacja; Aria, oddział"
        ))
        assertTrue(encoded.contains("VERSION:4.0"))
        assertTrue(encoded.contains("ORG:Fundacja\\; Aria\\, oddział"))
        val decoded = VCardCodec.decode(encoded)
        assertEquals("Jan Kowalski", decoded.first("FN"))
        assertEquals("Fundacja; Aria, oddział", decoded.first("ORG"))
    }

    @Test
    fun owdRecordRoundTripsThroughVCardExtensions() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.DISABILITY_STATUS,
            fields = mapOf("status" to "aktywny", "degree" to "umiarkowany"),
            provenance = DataProvenance.LOCAL_USER
        )
        val encoded = VCardCodec.encodeOwdRecord(record)
        assertTrue(encoded.contains("X-OWD-SCHEMA:"))
        assertTrue(encoded.contains("X-OWD-FIELD;X-OWD-NAME=degree:"))
        assertEquals(record, VCardCodec.decodeOwdRecord(encoded))
    }

    @Test
    fun decoderUnfoldsUtf8FoldedLines() {
        val longName = "Ł".repeat(100)
        val encoded = VCardCodec.encodeContact(mapOf("FN" to longName))
        assertTrue(encoded.contains("\r\n "))
        assertEquals(longName, VCardCodec.decode(encoded).first("FN"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsVCardVersionsOtherThanFour() {
        VCardCodec.decode("BEGIN:VCARD\r\nVERSION:3.0\r\nFN:Jan\r\nEND:VCARD\r\n")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOwdFieldsThatDoNotMatchTheRegisteredSchema() {
        val record = ShareableRecord(
            schema = OwdSchemaRegistry.DISABILITY_STATUS,
            fields = mapOf("status" to "aktywny", "unknownField" to "x"),
            provenance = DataProvenance.LOCAL_USER
        )
        VCardCodec.encodeOwdRecord(record)
    }
}
