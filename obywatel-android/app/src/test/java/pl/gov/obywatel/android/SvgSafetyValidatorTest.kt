// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SvgSafetyValidatorTest {
    @Test
    fun acceptsUtf8BomAndStandardXmlDeclaration() {
        val svg = "\uFEFF<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\"><path d=\"M0 0h10v10z\"/></svg>"
        SvgSafetyValidator.validateMarkupSecurity(svg)
    }

    @Test
    fun acceptsAndStripsStandardExternalSvg11DoctypeWithoutResolvingIt() {
        val svg = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10"><path d="M0 0h10v10z"/></svg>"""
        val normalized = SvgSafetyValidator.sanitizeMarkup(svg)
        assertFalse(normalized.contains("<!DOCTYPE", ignoreCase = true))
        SvgSafetyValidator.validateMarkupSecurity(svg)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInternalDtdAndEntityDeclarations() {
        SvgSafetyValidator.validateMarkupSecurity(
            """<!DOCTYPE svg [<!ENTITY x "unsafe">]><svg xmlns="http://www.w3.org/2000/svg">&x;</svg>"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsXmlStylesheetProcessingInstruction() {
        SvgSafetyValidator.validateMarkupSecurity(
            """<?xml-stylesheet type="text/css" href="https://example.org/style.css"?><svg xmlns="http://www.w3.org/2000/svg"/>"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRemoteReferences() {
        SvgSafetyValidator.validateMarkupSecurity(
            """<svg xmlns="http://www.w3.org/2000/svg"><image href="https://example.org/pixel.png"/></svg>"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsActiveScriptElements() {
        SvgSafetyValidator.validateMarkupSecurity(
            """<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>"""
        )
    }


    @Test
    fun textSignatureGeneratorProducesSafeScalableSvgAndEscapesMarkup() {
        val svg = makeTextSignatureSvg("Anna <script>alert(1)</script>", "#183A63").toString(Charsets.UTF_8)
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("&lt;script&gt;"))
        SvgSafetyValidator.validateMarkupSecurity(svg)
    }

    @Test
    fun customStampGeneratorProducesSafeSvgAndLabelsItAsAGraphicPattern() {
        val svg = makeStampSvg("Fundacja Aria", "PIECZĘĆ", "F-2026/01", "#244B75").toString(Charsets.UTF_8)
        assertTrue(svg.contains("WZÓR GRAFICZNY"))
        assertTrue(svg.contains("Fundacja Aria"))
        SvgSafetyValidator.validateMarkupSecurity(svg)
    }

    @Test(expected = IllegalArgumentException::class)
    fun generatedSignatureRejectsExternalSvgReferencesThroughValidator() {
        SvgSafetyValidator.validateMarkupSecurity(
            """<svg xmlns="http://www.w3.org/2000/svg"><text href="https://example.org">x</text></svg>"""
        )
    }

    @Test
    fun recognizesSvgEvenWhenXmlDeclarationPrecedesRoot() {
        val bytes = """<?xml version="1.0"?><svg xmlns="http://www.w3.org/2000/svg"/>""".toByteArray(Charsets.UTF_8)
        assertTrue(SvgSafetyValidator.looksLikeSvg(bytes))
    }
}
