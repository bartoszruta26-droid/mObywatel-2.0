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
    fun archiveRoundTripsAlternativeSpouseChildrenAndFamilyPhotosWithCredentialSettings() {
        fun photo(seed: Int): String = java.util.Base64.getEncoder().encodeToString(
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), seed.toByte(), 0x01, 0x02)
        )
        val fields = linkedMapOf(
            "firstNames" to "Jan",
            "lastName" to "Kowalski",
            "photo" to photo(1),
            "photoAlternative1" to photo(2),
            "photoAlternative2" to photo(3),
            "wifePhoto" to photo(4),
            "child1Photo" to photo(5),
            "child2Photo" to photo(6),
            "familyMember1Photo" to photo(7),
            "familyMember2Photo" to photo(8),
            "passportNumber" to "PA1234567",
            "trainingCourseName" to "Kurs kwalifikacyjny",
            "cs_identity_orientation" to "s:POZIOMA",
            "cs_identity_visible_fields" to """a:["firstNames","lastName","idNumber"]""",
            "dp_identity" to "dashboard"
        )
        assertEquals(fields, VCardCodec.decodeForImport(VCardCodec.encodeArchive(fields)))
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


    @Test
    fun vCardArchiveRoundTripsEveryNewStateServiceDocumentRecord() {
        val documentIds = listOf(
            "mayor", "village_head", "county_starosta", "citizen_record",
            "ombudsman", "children_ombudsman", "patient_ombudsman",
            "pretrial_mediator", "milicja_officer", "foreign_service_member",
            "diplomat", "consul", "ambassador", "foreign_mission_head",
            "consular_employee", "inspector",
            "org_president", "org_general_director", "org_regional_director",
            "org_county_director", "org_local_representative", "org_general_secretary",
            "org_treasurer", "org_members_rights_ombudsman", "org_patient_ombudsman",
            "org_family_ombudsman", "civil_mediator", "civil_arbitrator",
            "civil_expert", "civil_appraiser", "civil_auditor",
            "civil_internal_inspector", "civil_controller", "civil_consultant",
            "civil_social_caretaker", "civil_org_curator", "civil_rescuer",
            "org_ambassador", "org_foreign_representative", "org_proxy", "org_delegate"
        )
        val officialDocuments = documentIds.joinToString(
            prefix = "{", postfix = "}", separator = ","
        ) { id -> "\"$id\":{\"field_0\":\"Dane ${id}\"}" }
        val source = linkedMapOf(
            "firstNames" to "Jan",
            "lastName" to "Kowalski",
            "officialDocumentArchive" to officialDocuments
        )

        val exportedVCard = VCardCodec.encodeArchive(source)
        val imported = VCardCodec.decodeForImport(exportedVCard)

        assertEquals(source, imported)
        documentIds.forEach { id ->
            assertTrue(imported.getValue("officialDocumentArchive").contains("\"$id\""))
        }
    }

    @Test
    fun archiveRoundTripsChildrenVaccinationsAndOriginalDocumentAttachments() {
        val familyHealth = """{"version":1,"children":[{"id":"child-1","firstName":"Anna","lastName":"Kowalska","birthDate":"2018-04-05","pesel":"18040512345","allergies":"jaja","guardian":"Jan Kowalski","photoBase64":"YW5uYQ==","vaccines":[{"id":"dose-1","date":"2024-01-10","vaccine":"MMR","disease":"odra, świnka, różyczka","dose":"2/2","batch":"LOT-123","manufacturer":"Producent","provider":"Lekarz","facility":"Przychodnia","nextDate":"2030-01-10","notes":"reakcja miejscowa"}]}]}"""
        val originalAttachments = """{"version":1,"attachments":[{"documentId":"child_vaccination_card","name":"Książeczka szczepień.pdf","mime":"application/pdf","dataBase64":"JVBERi0xLjQ="}]}"""
        val fields = linkedMapOf(
            "familyHealthArchive" to familyHealth,
            "originalAttachmentArchive" to originalAttachments,
            "familyEntityDocumentArchive" to """{"rod_birth_extract":{"field_0":"Anna"}}""",
            "officialDocumentArchive" to """{"passport":{"number":"PA123"}}"""
        )
        assertEquals(fields, VCardCodec.decodeForImport(VCardCodec.encodeArchive(fields)))
    }

}
