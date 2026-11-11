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
 * Bezpieczny, ograniczony pamięciowo odczyt tekstu z pliku OpenDocument Text.
 * Nie rozpakowuje archiwum na dysk i odrzuca deklaracje DTD/encji zewnętrznych.
 */
internal object OdtTextExtractor {
    private const val MAX_ENTRIES = 128
    private const val MAX_TOTAL_UNCOMPRESSED_BYTES = 12 * 1024 * 1024
    private const val MAX_CONTENT_XML_BYTES = 2 * 1024 * 1024
    private const val MAX_TEXT_CHARS = 200_000
    private const val CONTENT_XML = "content.xml"
    private const val ODT_MIME = "application/vnd.oasis.opendocument.text"

    fun extract(input: InputStream): String {
        var totalBytes = 0
        var entryCount = 0
        var mimetype: String? = null
        var contentXml: ByteArray? = null

        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                require(entryCount <= MAX_ENTRIES) { "Dokument ODT zawiera zbyt wiele elementów." }
                val isMime = entry.name == "mimetype"
                val isContent = entry.name == CONTENT_XML
                val limit = if (isContent) MAX_CONTENT_XML_BYTES else MAX_TOTAL_UNCOMPRESSED_BYTES
                val output = if (isMime || isContent) ByteArrayOutputStream() else null
                val buffer = ByteArray(8192)
                var entryBytes = 0
                while (true) {
                    val count = zip.read(buffer)
                    if (count < 0) break
                    entryBytes += count
                    totalBytes += count
                    require(totalBytes <= MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        "Dokument ODT przekracza dopuszczalny rozmiar podglądu."
                    }
                    require(entryBytes <= limit) {
                        "Główna część dokumentu ODT jest zbyt duża."
                    }
                    output?.write(buffer, 0, count)
                }
                zip.closeEntry()
                if (isMime) mimetype = output?.toByteArray()?.toString(Charsets.UTF_8)?.trim()
                if (isContent) contentXml = output?.toByteArray()
            }
        }

        require(mimetype == ODT_MIME) { "Plik nie jest prawidłowym dokumentem OpenDocument Text (ODT)." }
        val xml = requireNotNull(contentXml) { "Plik ODT nie zawiera treści content.xml." }
        return parseContentXml(xml).trim().also {
            require(it.isNotEmpty()) { "Dokument ODT nie zawiera tekstu do wyświetlenia." }
        }
    }

    private fun parseContentXml(bytes: ByteArray): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")
        val result = StringBuilder()
        var paragraphDepth = 0
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "p", "h" -> paragraphDepth++
                    "tab" -> if (paragraphDepth > 0) appendBounded(result, "\\t")
                    "line-break" -> if (paragraphDepth > 0) appendBounded(result, "\\n")
                    "s" -> if (paragraphDepth > 0) {
                        val count = (0 until parser.attributeCount)
                            .firstOrNull { parser.getAttributeName(it) == "c" }
                            ?.let { parser.getAttributeValue(it)?.toIntOrNull() } ?: 1
                        require(count in 1..10_000) { "Nieprawidłowa liczba powtórzonych spacji w ODT." }
                        appendBounded(result, " ".repeat(count))
                    }
                }
                XmlPullParser.TEXT -> if (paragraphDepth > 0) {
                    appendBounded(result, parser.text.orEmpty())
                }
                XmlPullParser.END_TAG -> if (parser.name == "p" || parser.name == "h") {
                    if (paragraphDepth > 0) paragraphDepth--
                    appendBounded(result, "\\n")
                }
            }
            event = parser.next()
        }
        return result.toString()
    }

    private fun appendBounded(target: StringBuilder, value: String) {
        require(target.length + value.length <= MAX_TEXT_CHARS) {
            "Tekst dokumentu ODT przekracza limit podglądu."
        }
        target.append(value)
    }
}
