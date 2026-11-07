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
class DocxTextExtractorTest {
    @Test
    fun extractsTextFromParagraphsAndPreservesTabsAndBreaks() {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body><w:p><w:r><w:t>Pierwszy</w:t></w:r><w:r><w:tab/></w:r><w:r><w:t>wiersz</w:t></w:r></w:p>
              <w:p><w:r><w:t>Drugi</w:t><w:br/><w:t>ciąg dalszy</w:t></w:r></w:p></w:body>
            </w:document>""".trimIndent()

        assertEquals(
            "Pierwszy\twiersz\nDrugi\nciąg dalszy",
            DocxTextExtractor.extract(docx("word/document.xml" to xml.toByteArray()))
        )
    }

    @Test
    fun rejectsArchiveWithoutMainDocument() {
        val archive = docx("word/styles.xml" to "<styles/>".toByteArray())
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(archive)
        }
    }

    @Test
    fun rejectsDocumentWithoutDisplayableText() {
        val xml = """<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body><w:p/></w:body></w:document>"""
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(docx("word/document.xml" to xml.toByteArray()))
        }
    }

    @Test
    fun rejectsArchiveWithoutReadableZipEntries() {
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(ByteArrayInputStream("not-a-zip-archive".toByteArray()))
        }
    }

    @Test
    fun rejectsMalformedDocumentXml() {
        val archive = docx("word/document.xml" to "<document><".toByteArray())
        assertThrows(Exception::class.java) {
            DocxTextExtractor.extract(archive)
        }
    }

    @Test
    fun rejectsMoreThanMaximumNumberOfZipEntries() {
        val entries = (0..128).map { "word/extra-$it.xml" to byteArrayOf() }
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(docx(*entries.toTypedArray()))
        }
    }

    @Test
    fun rejectsDocumentXmlLargerThanTwoMiB() {
        val oversizedXml = ByteArray(2 * 1024 * 1024 + 1) { ' '.code.toByte() }
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(docx("word/document.xml" to oversizedXml))
        }
    }

    @Test
    fun rejectsArchiveExceedingTotalUncompressedLimit() {
        val payload = ByteArray(1024 * 1024) { 'x'.code.toByte() }
        val entries = (0..12).map { "word/part-$it.xml" to payload }
        assertThrows(IllegalArgumentException::class.java) {
            DocxTextExtractor.extract(docx(*entries.toTypedArray()))
        }
    }

    private fun docx(vararg entries: Pair<String, ByteArray>): ByteArrayInputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return ByteArrayInputStream(output.toByteArray())
    }
}
