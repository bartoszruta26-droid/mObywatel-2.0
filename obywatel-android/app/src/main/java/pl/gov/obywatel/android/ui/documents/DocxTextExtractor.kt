// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.documents

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Odczytuje tekst głównego dokumentu DOCX bez rozpakowywania archiwum na dysk.
 *
 * Ograniczenia chroniące podgląd przed nadmiernym zużyciem pamięci:
 * - maksymalnie 128 wpisów ZIP,
 * - maksymalnie 12 MiB danych rozpakowanych łącznie,
 * - maksymalnie 2 MiB dla word/document.xml,
 * - maksymalnie 200 000 znaków tekstu wynikowego.
 */
internal object DocxTextExtractor {
    private const val MAX_ENTRIES = 128
    private const val MAX_TOTAL_UNCOMPRESSED_BYTES = 12 * 1024 * 1024
    private const val MAX_DOCUMENT_XML_BYTES = 2 * 1024 * 1024
    private const val MAX_TEXT_CHARS = 200_000
    private const val DOCUMENT_XML = "word/document.xml"

    fun extract(input: InputStream): String {
        var totalBytes = 0
        var entryCount = 0
        var documentXml: ByteArray? = null

        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount += 1
                require(entryCount <= MAX_ENTRIES) { "Dokument DOCX zawiera zbyt wiele elementów." }

                val isDocument = entry.name == DOCUMENT_XML
                val entryLimit = if (isDocument) MAX_DOCUMENT_XML_BYTES else MAX_TOTAL_UNCOMPRESSED_BYTES
                val output = if (isDocument) ByteArrayOutputStream() else null
                val buffer = ByteArray(8 * 1024)
                var entryBytes = 0

                while (true) {
                    val count = zip.read(buffer)
                    if (count < 0) break
                    entryBytes += count
                    totalBytes += count
                    require(totalBytes <= MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        "Dokument DOCX przekracza dopuszczalny rozmiar podglądu."
                    }
                    require(entryBytes <= entryLimit) {
                        "Główna część dokumentu DOCX jest zbyt duża."
                    }
                    output?.write(buffer, 0, count)
                }
                zip.closeEntry()
                if (isDocument) documentXml = output?.toByteArray()
            }
        }

        val xml = requireNotNull(documentXml) { "Plik nie zawiera głównej treści dokumentu DOCX." }
        return parseDocumentXml(xml).trim().also {
            require(it.isNotEmpty()) { "Dokument DOCX nie zawiera tekstu do wyświetlenia." }
        }
    }

    private fun parseDocumentXml(bytes: ByteArray): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

        val result = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "t" -> {
                        val text = parser.nextText()
                        appendBounded(result, text)
                    }
                    "tab" -> appendBounded(result, "\t")
                    "br", "cr" -> appendBounded(result, "\n")
                }
                XmlPullParser.END_TAG -> if (parser.name == "p") {
                    appendBounded(result, "\n")
                }
            }
            event = parser.next()
        }
        return result.toString()
    }

    private fun appendBounded(target: StringBuilder, value: String) {
        require(target.length + value.length <= MAX_TEXT_CHARS) {
            "Tekst dokumentu przekracza limit podglądu."
        }
        target.append(value)
    }
}
