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
    fun recognizesSvgEvenWhenXmlDeclarationPrecedesRoot() {
        val bytes = """<?xml version="1.0"?><svg xmlns="http://www.w3.org/2000/svg"/>""".toByteArray(Charsets.UTF_8)
        assertTrue(SvgSafetyValidator.looksLikeSvg(bytes))
    }

    @Test
    fun acceptsOnlySixDigitHexColorsForGeneratedSvg() {
        assertTrue(SvgSafetyValidator.requireSafeHexColor("#183A63") == "#183A63")
        listOf("red", "#FFF", "#112233;url(https://evil.test)", "url(#x)", "#123456\n").forEach { value ->
            try {
                SvgSafetyValidator.requireSafeHexColor(value)
                throw AssertionError("Expected invalid color: $value")
            } catch (_: IllegalArgumentException) {
                // expected
            }
        }
    }


    @Test
    fun certificateSignatureAndStampGeneratorsEscapeTextAndValidateColors() {
        val signature = makeTextSignatureSvg("Fundacja <Aria> & partnerzy", "#183A63").toString(Charsets.UTF_8)
        assertTrue(signature.contains("Fundacja &lt;Aria&gt; &amp; partnerzy"))
        SvgSafetyValidator.validateMarkupSecurity(signature)

        val stamp = makeStampSvg("Fundacja Aria", "PIECZĘĆ", "F-2026/01", "#244B75").toString(Charsets.UTF_8)
        assertTrue(stamp.contains("WZÓR GRAFICZNY"))
        SvgSafetyValidator.validateMarkupSecurity(stamp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun signatureGeneratorRejectsUnsafeColorValue() {
        makeTextSignatureSvg("Jan Kowalski", "#112233;url(https://attacker.invalid)")
    }

    @Test(expected = IllegalArgumentException::class)
    fun stampGeneratorRejectsUnsafeColorValue() {
        makeStampSvg("Fundacja", "PIECZĘĆ", "1", "url(#malicious)")
    }
