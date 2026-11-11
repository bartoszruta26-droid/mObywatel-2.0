// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.junit.Assert.assertArrayEquals
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
    fun importsLegacyArchiveWithLiteralEscapedLineSeparators() {
        val source = mapOf("firstNames" to "Zażółć", "lastName" to "Gęślą")
        val legacy = VCardCodec.encodeArchive(source).replace("\r\n", "\\\\r\\\\n")
        assertEquals(source, VCardCodec.decodeForImport(legacy))
    }

    @Test
    fun importsArchiveWithLfOnlyLineEndings() {
        val source = mapOf("firstNames" to "Jan", "lastName" to "Kowalski")
        val encoded = VCardCodec.encodeArchive(source).replace("\r\n", "\n")
        assertEquals(source, VCardCodec.decodeForImport(encoded))
    }

    @Test
    fun importsArchiveWithCrOnlyLineEndings() {
        val source = mapOf("firstNames" to "Jan", "lastName" to "Kowalski")
        val encoded = VCardCodec.encodeArchive(source).replace("\r\n", "\r")
        assertEquals(source, VCardCodec.decodeForImport(encoded))
    }

    @Test
    fun importsArchiveWithMixedPhysicalAndEscapedLineEndings() {
        val source = mapOf("firstNames" to "Jan", "lastName" to "Kowalski", "notes" to "Linia 1")
        val encoded = VCardCodec.encodeArchive(source)
            .replaceFirst("\r\n", "\n")
            .replace("X-OWD-ARCHIVE-VERSION:1\r\n", "X-OWD-ARCHIVE-VERSION:1\\r\\n")
        assertEquals(source, VCardCodec.decodeForImport(encoded))
    }

    @Test
    fun repairsPolishUtf8TextPreviouslyDecodedAsWindows1250() {
        val input = """BEGIN:VCARD
VERSION:3.0
FN:ZaÅ¼Ã³Å‚Ä‡ GÄ™Å›lÄ…
N:GÄ™Å›lÄ…;ZaÅ¼Ã³Å‚Ä‡;;;
END:VCARD
"""
        val imported = VCardCodec.decodeForImport(input)
        assertEquals("Zażółć", imported["firstNames"])
        assertEquals("Gęślą", imported["lastName"])
    }

    @Test
    fun importsStandardVCardThreePointZeroIntoProfileFields() {
        val input = """BEGIN:VCARD
VERSION:3.0
N:Kowalski;Jan;Adam;;
FN:Jan Adam Kowalski
TEL;TYPE=CELL:+48123456789
EMAIL;TYPE=HOME:jan@example.com
ORG:Fundacja Aria
TITLE:Dyrektor
URL:https://example.org
END:VCARD
"""
        val imported = VCardCodec.decodeForImport(input)
        assertEquals("Jan Adam", imported["firstNames"])
        assertEquals("Kowalski", imported["lastName"])
        assertEquals("+48123456789", imported["contactPhone"])
        assertEquals("jan@example.com", imported["contactEmail"])
        assertEquals("Fundacja Aria", imported["contactOrganization"])
        assertEquals("Dyrektor", imported["contactTitle"])
        assertEquals("https://example.org", imported["contactUrl"])
    }

    @Test
    fun importsInlineBase64PhotoFromLegacyVCard() {
        val photoBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0x01, 0x02)
        val photoBase64 = java.util.Base64.getEncoder().encodeToString(photoBytes)
        val input = """BEGIN:VCARD
VERSION:3.0
FN:Jan Kowalski
N:Kowalski;Jan;;;
PHOTO;ENCODING=b;TYPE=JPEG:$photoBase64
END:VCARD
"""
        val imported = VCardCodec.decodeForImport(input)
        assertEquals(photoBase64, imported["photo"])
        assertArrayEquals(photoBytes, java.util.Base64.getDecoder().decode(imported.getValue("photo")))
    }

    @Test
    fun importsVCardDataUriPhotoAndIgnoresRemotePhotoUri() {
        val photoBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x01)
        val photoBase64 = java.util.Base64.getEncoder().encodeToString(photoBytes)
        val dataUri = "data:image/png;base64,$photoBase64"
        val input = """BEGIN:VCARD
VERSION:4.0
FN:Jan Kowalski
PHOTO:$dataUri
END:VCARD
"""
        assertEquals(photoBase64, VCardCodec.decodeForImport(input)["photo"])

        val remote = """BEGIN:VCARD
VERSION:4.0
FN:Jan Kowalski
PHOTO;VALUE=uri:https://example.org/photo.jpg
END:VCARD
"""
        assertTrue(VCardCodec.decodeForImport(remote)["photo"].isNullOrBlank())
    }

    @Test
    fun importsApplicationArchiveWithoutLosingCustomProfileFields() {
        val source = mapOf("firstNames" to "Jan", "lastName" to "Kowalski", "pesel" to "123")
        assertEquals(source, VCardCodec.decodeForImport(VCardCodec.encodeArchive(source)))
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


    @Test
    fun archiveUsesRealCrLfAndRoundTripsEveryField() {
        val fields = mapOf(
            "firstNames" to "Jan",
            "lastName" to "Kowalski",
            "idNumber" to "ABC123",
            "passportNumber" to "P,123;45",
            "notes" to "Linia 1\nLinia 2"
        )
        val encoded = VCardCodec.encodeArchive(fields)
        assertTrue(encoded.startsWith("BEGIN:VCARD\r\nVERSION:4.0\r\n"))
        assertTrue(encoded.endsWith("END:VCARD\r\n"))
        assertTrue(!encoded.contains("\\r\\n"))
        assertEquals(fields, VCardCodec.decodeArchive(encoded))
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

    @Test
    fun archiveRoundTripsFieldsFromAllDocumentGroupsIncludingProfilePhoto() {
        val photoBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0x00, 0x11)
        val photoBase64 = java.util.Base64.getEncoder().encodeToString(photoBytes)
        val fields = linkedMapOf(
            "firstNames" to "Jan Adam",
            "lastName" to "Kowalski",
            "birthDate" to "1980-01-02",
            "pesel" to "80010212345",
            "idNumber" to "ABC123456",
            "idIssueDate" to "2020-01-01",
            "idExpiryDate" to "2030-01-01",
            "passportNumber" to "PA1234567",
            "passportIssuer" to "Wojewoda",
            "passportLpNumber" to "LP-123",
            "disabilityNumber" to "ON-123",
            "disabilityDegree" to "umiarkowany",
            "disabilityCauseCodes" to "05-R",
            "registry_association_name" to "Fundacja Aria",
            "registry_association_krs" to "0000123456",
            "contactPhone" to "+48123456789",
            "contactEmail" to "jan@example.org",
            "photo" to photoBase64
        )
        val encoded = VCardCodec.encodeArchive(fields)
        val imported = VCardCodec.decodeForImport(encoded)
        assertEquals(fields, imported)
        assertEquals(photoBase64, imported["photo"])
        assertArrayEquals(photoBytes, java.util.Base64.getDecoder().decode(imported.getValue("photo")))
    }

}
