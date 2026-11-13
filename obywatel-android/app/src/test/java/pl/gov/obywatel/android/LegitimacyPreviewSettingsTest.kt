// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegitimacyPreviewSettingsTest {
    @Test
    fun explicitlyEmptyVisibleFieldSelectionHidesEveryField() {
        val fields = listOf(
            Triple("firstNames", "Imiona", "Anna"),
            Triple("lastName", "Nazwisko", "Kowalska")
        )

        assertEquals(fields, credentialVisibleFields(fields, false, emptySet()))
        assertTrue(credentialVisibleFields(fields, true, emptySet()).isEmpty())
        assertEquals(listOf(fields.first()), credentialVisibleFields(fields, true, setOf("firstNames")))
    }

    @Test
    fun fieldsArePlacedOnlyOnTheExplicitlySelectedSide() {
        val fields = (1..7).map { Triple("p$it", "Pole $it", "Wartość $it") }
        val (front, reverse) = splitCredentialFieldsForSides(
            fields,
            "PIONOWA",
            visibleFieldsConfigured = true,
            visibleFieldKeys = fields.map { it.first }.toSet(),
            fieldSides = mapOf(
                "p1" to CREDENTIAL_FIELD_FRONT,
                "p2" to CREDENTIAL_FIELD_BACK,
                "p3" to CREDENTIAL_FIELD_HIDDEN
            )
        )
        assertEquals(listOf("p1", "p4", "p5", "p6", "p7"), front.map { it.first })
        assertEquals(listOf("p2"), reverse.map { it.first })
        assertTrue("Ukryte i niewybrane pola nie mogą być automatycznie przenoszone na rewers.",
            "p3" !in front.map { it.first } && "p3" !in reverse.map { it.first })
    }

    @Test
    fun documentFieldOptionsMatchTextEditorKeysAndLabels() {
        val identityOptions = credentialFieldOptions("identity").toMap()
        assertEquals("Status dokumentu", identityOptions["idStatus"])
        assertEquals("Numer poprzedniego dokumentu (jeśli dotyczy)", identityOptions["idPreviousNumber"])
        assertEquals("Data utraty / unieważnienia", identityOptions["idLossOrInvalidationDate"])
        assertEquals(
            listOf("passportNumber", "passportCountry", "passportIssueDate", "passportExpiryDate",
                "passportIssuer", "passportType", "passportHolderNames", "passportBirthPlace",
                "passportCitizenship", "passportSex", "passportStatus", "passportLossDate"),
            credentialFieldOptions("passport").map { it.first }
        )
        val familyOptions = credentialFieldOptions("family")
        assertTrue("Podgląd Rodu musi mieć pola wynikające z formularza.",
            familyOptions.isNotEmpty() && familyOptions.none { it.first == "details" })
        val disabilityKeys = credentialFieldOptions("disability").map { it.first }
        assertTrue("Podgląd nie może dopisywać ogólnej etykiety bez pola w edytorze.",
            "details" !in disabilityKeys && "details" !in credentialFieldOptions("identity").map { it.first })
        assertTrue("Pole sprawy musi istnieć w podglądzie.", "disabilityCaseNumber" in disabilityKeys)
        assertTrue("Pole załącznika źródłowego musi istnieć w podglądzie.", "disabilitySourceAttachment" in disabilityKeys)
        assertTrue("Podgląd nie może dodawać nieistniejącego pola PESEL o innym kluczu.", "pesel" !in disabilityKeys)
    }


    @Test
    fun loyaltyCardPreviewUsesNumberedEditorKeysForAllTwentyFiveCards() {
        val options = credentialFieldOptions("loyalty_cards")
        val keys = options.map { it.first }
        assertEquals(180, options.size)
        assertEquals("loyalty1Store", keys.first())
        assertTrue("Każda karta musi mieć własny numer sklepu.",
            (1..25).all { "loyalty${it}Store" in keys })
        assertTrue("Każda karta musi mieć własny numer karty.",
            (1..25).all { "loyalty${it}Number" in keys })
        assertTrue("Podgląd nie może używać ogólnych, nieistniejących kluczy.",
            listOf("loyaltyName", "loyaltyNumber", "loyaltyHolder").none { it in keys })
        assertTrue("Dane źródłowe i weryfikacyjne muszą mieć te same klucze co edytor.",
            listOf("loyaltyExtraSource", "loyaltyExtraLegalBasis", "loyaltyExtraVerifiedAt",
                "loyaltyExtraVerifiedBy", "loyaltyExtraNotes").all { it in keys })
    }

    @Test
    fun dynamicChurchFamilyAndOfficialPreviewsReuseTheirEditorFieldDefinitions() {
        val churchId = "church_orders"
        assertEquals(churchGraphicDocumentFields(churchId), credentialFieldOptions(churchId))

        val familyId = familyEntityGraphicDocumentTargets().first().first
        assertEquals(familyEntityGraphicDocumentFields(familyId), credentialFieldOptions(familyId))

        val officialId = "birth_extract"
        assertEquals(officialGraphicDocumentFields(officialId), credentialFieldOptions(officialId))
    }

    @Test
    fun bankAccountPreviewKeysMatchTheDynamicThirtyAccountEditor() {
        val options = credentialFieldOptions("bank_accounts").map { it.first }
        assertEquals(390, options.size)
        assertTrue("Pierwsze konto musi odpowiadać kluczom formularza.",
            (0..12).all { "account_0_${it}" in options })
        assertTrue("Trzydzieste konto musi odpowiadać kluczom formularza.",
            (0..12).all { "account_29_${it}" in options })
    }

    @Test
    fun schemaBackedDocumentPreviewUsesOnlyEditorDefinitions() {
        val documentId = "professional_medical"
        val expected = documentEditorFieldDefinitions(documentId).map {
            "doc_${documentId}_${it.key}" to it.label
        }
        assertEquals(expected, credentialFieldOptions(documentId))
    }

    @Test
    fun profilePreviewDoesNotInventBirthDateAndIncludesParentFields() {
        val options = credentialFieldOptions("profile").toMap()
        assertEquals("Telefon kontaktowy", options["contactPhone"])
        assertEquals("Imiona ojca", options["fatherNames"])
        assertEquals("Nazwisko rodowe matki", options["motherSurname"])
        assertTrue("Profil nie powinien dodawać pola, którego edytor profilu nie wyświetla.",
            "birthDate" !in options)
    }

    @Test
    fun sharedAppearanceOverridesLayoutButPreservesDocumentAssetsAndFields() {
        val document = LegitimacyPreviewSettings(
            showCard = true,
            tileBackgroundAssetId = "tile-student",
            crestAssetId = "crest-student",
            flagAssetId = "flag-student",
            backgroundAssetId = "background-student",
            logoAssetId = "logo-student",
            visibleFields = setOf("firstNames", "lastName"),
            visibleFieldsConfigured = true,
            orientation = "PIONOWA",
            graphicScale = 1.0f,
            fontFamily = "sans",
            fontColor = "#FF7A1F2B"
        )
        val shared = LegitimacyPreviewSettings(
            orientation = "POZIOMA",
            showCard = false,
            showReverse = true,
            showPhoto = false,
            showFlag = false,
            showCrest = true,
            showLogo = false,
            showBackground = true,
            graphicScale = 1.4f,
            graphicRotation = 12f,
            graphicOffsetX = 8f,
            crestScale = 1.8f,
            crestRotation = 23f,
            crestOffsetX = 9f,
            crestOffsetY = -4f,
            flagScale = 0.7f,
            flagRotation = -12f,
            flagOffsetX = -6f,
            flagOffsetY = 5f,
            labelScale = 1.2f,
            valueScale = 0.9f,
            fontFamily = "serif",
            fontColor = "#FF17365D"
        )

        val result = mergeSharedLegitimacyAppearance(document, shared)

        assertEquals("POZIOMA", result.orientation)
        assertEquals(true, result.showCard)
        assertEquals("tile-student", result.tileBackgroundAssetId)
        assertEquals(true, result.showReverse)
        assertEquals(1.4f, result.graphicScale)
        assertEquals(12f, result.graphicRotation)
        assertEquals(1.8f, result.crestScale)
        assertEquals(23f, result.crestRotation)
        assertEquals(9f, result.crestOffsetX)
        assertEquals(-4f, result.crestOffsetY)
        assertEquals(0.7f, result.flagScale)
        assertEquals(-12f, result.flagRotation)
        assertEquals(-6f, result.flagOffsetX)
        assertEquals(5f, result.flagOffsetY)
        assertEquals("serif", result.fontFamily)
        assertEquals("#FF7A1F2B", result.fontColor)
        assertEquals("crest-student", result.crestAssetId)
        assertEquals("flag-student", result.flagAssetId)
        assertEquals("background-student", result.backgroundAssetId)
        assertEquals("logo-student", result.logoAssetId)
        assertEquals(setOf("firstNames", "lastName"), result.visibleFields)
        assertTrue(result.visibleFieldsConfigured)
    }
    @Test
    fun sharedLayoutOverridesPerDocumentGeometryButPreservesDocumentAssets() {
        val document = LegitimacyPreviewSettings(
            showCard = false,
            tileBackgroundAssetId = "tile-specific",
            crestAssetId = "crest-specific",
            flagAssetId = "flag-specific",
            backgroundAssetId = "background-specific",
            logoAssetId = "logo-specific",
            orientation = "PIONOWA",
            showPhoto = false,
            showFlag = true,
            imageFilter = "grayscale",
            graphicScale = 1.7f,
            crestScale = 2.1f,
            crestRotation = 31f,
            crestOffsetX = 14f,
            crestOffsetY = -7f,
            flagScale = 0.6f,
            flagRotation = -19f,
            flagOffsetX = -12f,
            flagOffsetY = 8f,
            fontColor = "#FF7A1F2B",
            individualAppearanceConfigured = true
        )
        val shared = LegitimacyPreviewSettings(
            orientation = "POZIOMA",
            showPhoto = true,
            showFlag = false,
            imageFilter = "sepia",
            graphicScale = 0.8f,
            crestScale = 1.3f,
            crestRotation = 12f,
            crestOffsetX = 4f,
            crestOffsetY = 5f,
            flagScale = 0.9f,
            flagRotation = -8f,
            flagOffsetX = 6f,
            flagOffsetY = -3f,
            fontColor = "#FF17365D"
        )

        val result = mergeSharedLegitimacyAppearance(document, shared)

        assertEquals("POZIOMA", result.orientation)
        assertEquals(false, result.showCard)
        assertEquals("tile-specific", result.tileBackgroundAssetId)
        assertEquals(true, result.showPhoto)
        assertEquals(false, result.showFlag)
        assertEquals("sepia", result.imageFilter)
        assertEquals(0.8f, result.graphicScale)
        assertEquals(1.3f, result.crestScale)
        assertEquals(12f, result.crestRotation)
        assertEquals(4f, result.crestOffsetX)
        assertEquals(5f, result.crestOffsetY)
        assertEquals(0.9f, result.flagScale)
        assertEquals(-8f, result.flagRotation)
        assertEquals(6f, result.flagOffsetX)
        assertEquals(-3f, result.flagOffsetY)
        assertEquals("#FF7A1F2B", result.fontColor)
        assertEquals("crest-specific", result.crestAssetId)
        assertEquals("flag-specific", result.flagAssetId)
        assertEquals("background-specific", result.backgroundAssetId)
        assertEquals("logo-specific", result.logoAssetId)
    }
}

