// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/**
 * Conservative SVG import gate. It accepts a normal XML declaration and the
 * standard external SVG 1.0/1.1 DOCTYPE, but never resolves DTDs or entities.
 * Active content, event handlers and remote references remain forbidden.
 */
internal object SvgSafetyValidator {
    private const val MAX_BYTES = 12 * 1024 * 1024
    private const val DETECTION_BYTES = 16 * 1024
    private val HEX_COLOR = Regex("#[0-9A-Fa-f]{6}")

    /** Restrict dynamic SVG paint values to a single opaque RGB color. */
    internal fun requireSafeHexColor(value: String): String {
        require(HEX_COLOR.matches(value)) { "Kolor musi mieć format #RRGGBB." }
        return value
    }

    internal fun looksLikeSvg(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        val prefix = bytes.copyOfRange(0, minOf(bytes.size, DETECTION_BYTES))
            .toString(Charsets.UTF_8).removePrefix("\uFEFF")
        return Regex("""<svg\b""", RegexOption.IGNORE_CASE).containsMatchIn(prefix)
    }

    internal fun validate(bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES) { "SVG pusty lub przekracza 12 MB." }
        val raw = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
        validateMarkupSecurity(raw)
        val safeMarkup = sanitizeMarkup(raw)
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        runCatching { parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false) }
        parser.setInput(StringReader(safeMarkup))
        var rootSeen = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && !rootSeen) {
                require(parser.name.equals("svg", ignoreCase = true)) {
                    "Dokument XML nie ma elementu głównego SVG."
                }
                rootSeen = true
            }
            event = parser.next()
        }
        require(rootSeen) { "Brak elementu SVG." }
    }

    /** Pure string checks are JVM-testable without Android XML runtime. */
    internal fun validateMarkupSecurity(raw: String) {
        val safeMarkup = sanitizeMarkup(raw)
        require(Regex("""<svg\b""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "Źródło nie zawiera elementu SVG."
        }
        require(!Regex("""<!DOCTYPE|<!ENTITY""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "SVG zawiera niedozwoloną deklarację DTD lub encji."
        }
        require(!Regex("""<\s*(script|foreignObject|animate|animateMotion|animateTransform|set)\b""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "SVG zawiera niedozwolony element aktywny lub animację."
        }
        require(!Regex("""\s(?:on[a-z][a-z0-9_-]*|xml:base)\s*=""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "SVG zawiera atrybut zdarzenia lub xml:base."
        }
        require(!Regex("""(?:href|src)\s*=\s*(['"])(?!#)[^'"]*\1""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "SVG zawiera odwołanie poza lokalnym fragmentem dokumentu."
        }
        require(!Regex("""@\s*import|url\s*\(\s*(['"]?)(?!#)[^)]+\)""", RegexOption.IGNORE_CASE).containsMatchIn(safeMarkup)) {
            "SVG zawiera zewnętrzne odwołanie CSS."
        }
        require(!safeMarkup.contains("<?")) {
            "SVG zawiera niedozwoloną instrukcję przetwarzania XML."
        }
    }

    /**
     * Strip only non-semantic XML prolog material that is commonly emitted by
     * SVG authoring tools. Never strip an internal DTD subset or entity rules.
     */
    internal fun sanitizeMarkup(raw: String): String {
        var value = raw.removePrefix("\uFEFF")
        val declaration = Regex("""^\s*<\?xml\s+[^?]*\?>""", RegexOption.IGNORE_CASE)
        val match = declaration.find(value)
        if (match != null) {
            require(Regex("""\bversion\s*=\s*(['"])1\.[01]\1""", RegexOption.IGNORE_CASE)
                .containsMatchIn(match.value)) {
                "Nieprawidłowa deklaracja XML w pliku SVG."
            }
            value = value.removeRange(match.range)
        }
        // Common SVG 1.0/1.1 external DTDs are unnecessary for rendering.
        // Removing the declaration prevents network/file entity resolution.
        val externalSvgDoctype = Regex(
            """<!DOCTYPE\s+svg\s+(?:PUBLIC\s+(['"])[^'"]+\1\s+(['"])[^'"]+\2|SYSTEM\s+(['"])[^'"]+\3)\s*>""",
            RegexOption.IGNORE_CASE
        )
        value = externalSvgDoctype.replace(value, "")
        return value
    }
}
