// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalDocumentContentTest {
    @Test
    fun pfcRegulationContainsAllExpectedChapters() {
        assertTrue(FUND_REGULATION_MARKDOWN.contains("§ 17. Cel rehabilitacyjno-opiekuńczy"))
        assertTrue(FUND_REGULATION_MARKDOWN.contains("§ 19. Samodzielne ustanowienie Projektu i funkcji Dyspozytora"))
        assertTrue(FUND_REGULATION_MARKDOWN.contains("§ 22. Zasada zachowania rozróżnień prawnych"))
    }

    @Test
    fun detailedPfcModelPreservesFullLegalAnalysis() {
        assertTrue(DETAILED_PFC_MODEL_TEXT.length > 100_000)
        assertTrue(DETAILED_PFC_MODEL_TEXT.contains("Art. 29 ust. 3"))
        assertTrue(DETAILED_PFC_MODEL_TEXT.contains("art. 43 ustawy o finansach publicznych"))
        assertTrue(DETAILED_PFC_MODEL_TEXT.contains("Art. 104"))
    }

    @Test
    fun companyAgreementContainsPaidModelAndAllThreeAnnexes() {
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("Nie jest to darowizna"))
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("§ 9. Wynagrodzenie Dyspozytora i rozliczenia podatkowe"))
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("Załącznik nr 1 – Uzasadnienie gospodarcze"))
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("Załącznik nr 2 – Plan działań"))
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("Załącznik nr 3 – Plan finansowania"))
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.contains("Uzupełniona lista kontrolna przed podpisaniem"))
    }

    @Test
    fun kotlinStringConstantsStayBelowJvmUtf8ConstantLimit() {
        val limit = 65_535
        assertTrue(FUND_REGULATION_MARKDOWN.toByteArray(Charsets.UTF_8).size < limit)
        assertTrue(COMPANY_FINANCING_AGREEMENT_TEXT.toByteArray(Charsets.UTF_8).size < limit)
        assertTrue(LEGAL_INFORMATION_TEXT.toByteArray(Charsets.UTF_8).size < limit)
    }

    @Test
    fun generalLegalInformationDoesNotDuplicatePfcRegulation() {
        assertFalse(LEGAL_INFORMATION_TEXT.contains("REGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY"))
    }
}
