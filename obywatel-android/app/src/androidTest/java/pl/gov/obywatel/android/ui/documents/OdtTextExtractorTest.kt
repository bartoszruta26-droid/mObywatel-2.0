// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class OdtTextExtractorTest {
    @Test
    fun extractsParagraphsTabsLineBreaksAndRepeatedSpaces() {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <office:document-content
              xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
              xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
              <office:body><office:text>
                <text:p>Pierwszy<text:tab/>wiersz</text:p>
                <text:p>Drugi<text:line-break/>ciąg<text:s text:c="3"/>dalszy</text:p>
              </office:text></office:body>
            </office:document-content>""".trimIndent()
        assertEquals("Pierwszy\\twiersz\\nDrugi\\nciąg   dalszy",
            OdtTextExtractor.extract(odt(xml.toByteArray())))
    }

    @Test
    fun rejectsArchiveWithWrongMimetype() {
        assertThrows(IllegalArgumentException::class.java) {
            OdtTextExtractor.extract(archive("application/zip", "content.xml" to "<document/>".toByteArray()))
        }
    }

    @Test
    fun rejectsArchiveWithoutContentXml() {
        assertThrows(IllegalArgumentException::class.java) {
            OdtTextExtractor.extract(archive("application/vnd.oasis.opendocument.text", "styles.xml" to "<styles/>".toByteArray()))
        }
    }

    @Test
    fun rejectsDoctypeAndExternalEntityDeclarations() {
        val xml = """<?xml version="1.0"?><!DOCTYPE office:document-content [<!ENTITY payload "expanded">]>
            <office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0">
            <office:body>&payload;</office:body></office:document-content>"""
        assertThrows(Exception::class.java) {
            OdtTextExtractor.extract(odt(xml.toByteArray()))
        }
    }

    private fun odt(content: ByteArray) =
        archive("application/vnd.oasis.opendocument.text", "content.xml" to content)

    private fun archive(mime: String, vararg entries: Pair<String, ByteArray>): ByteArrayInputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("mimetype"))
            zip.write(mime.toByteArray())
            zip.closeEntry()
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return ByteArrayInputStream(output.toByteArray())
    }
}
