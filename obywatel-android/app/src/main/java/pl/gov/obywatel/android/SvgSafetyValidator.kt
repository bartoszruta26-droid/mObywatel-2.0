// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/** Conservative SVG import gate: local fragments are allowed; active and remote content is rejected. */
internal object SvgSafetyValidator {
    private const val MAX_BYTES = 12 * 1024 * 1024

    internal fun validate(bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES) { "SVG pusty lub przekracza 12 MB." }
        val raw = bytes.toString(Charsets.UTF_8)
        validateMarkupSecurity(raw)
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(StringReader(raw))
        var rootSeen = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && !rootSeen) {
                require(parser.name.equals("svg", ignoreCase = true)) { "Dokument XML nie ma elementu głównego SVG." }
                rootSeen = true
            }
            event = parser.next()
        }
        require(rootSeen) { "Brak elementu SVG." }
    }

    /** Pure string checks are JVM-testable without Android XML runtime. */
    internal fun validateMarkupSecurity(raw: String) {
        require(Regex("<svg\\b", RegexOption.IGNORE_CASE).containsMatchIn(raw)) { "Źródło nie zawiera elementu SVG." }
        require(!Regex("<!DOCTYPE|<!ENTITY|<\\?(?!xml\\s)", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
            "SVG zawiera niedozwolone deklaracje XML."
        }
        require(!Regex("<\\s*(script|foreignObject|animate|animateMotion|animateTransform|set)\\b", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
            "SVG zawiera niedozwolony element aktywny lub animację."
        }
        require(!Regex("\\s(?:on[a-z][a-z0-9_-]*|xml:base)\\s*=", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
            "SVG zawiera atrybut zdarzenia lub xml:base."
        }
        require(!Regex("(?:href|src)\\s*=\\s*(['\\\"])\\s*(?:https?:|file:|javascript:|data:|//)", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
            "SVG zawiera zewnętrzne lub aktywne odwołanie."
        }
        // CSS resource URLs are rejected except local fragment references such as url(#gradient).
        require(!Regex("@\\s*import|url\\s*\\(\\s*(['\\\"]?)(?!#)(?:https?:|file:|javascript:|data:|//)[^)]*\\)", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
            "SVG zawiera zewnętrzne odwołanie CSS."
        }
    }
}
