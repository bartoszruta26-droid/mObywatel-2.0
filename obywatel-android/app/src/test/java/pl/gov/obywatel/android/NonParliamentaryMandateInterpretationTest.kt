// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertTrue
import org.junit.Test

class NonParliamentaryMandateInterpretationTest {
    @Test
    fun interpretationExplainsTheFullOrderOfAppointmentAndService() {
        val text = NON_PARLIAMENTARY_MANDATE_INTERPRETATION
        assertTrue(text.length > 12_000)
        assertTrue(text.contains("Powołanie do służby Narodowi"))
        assertTrue(text.contains("PUBLICZNE ŚLUBOWANIE"))
        assertTrue(text.contains("MANDAT NIEPARLAMENTARNY"))
        assertTrue(text.contains("Wybór do Sejmu"))
        assertTrue(text.contains("art. 4"))
        assertTrue(text.contains("art. 104"))
        assertTrue(text.contains("dobro wspólne"))
        assertTrue(text.contains("praw osób trzecich"))
    }

    @Test
    fun publicAuthorityAndPfcInterpretationCoversAppointmentGoalAndFunding() {
        val text = PUBLIC_AUTHORITY_PFC_INTERPRETATION
        assertTrue(text.length > 12_000)
        assertTrue(text.contains("piastuna organu"))
        assertTrue(text.contains("art. 9 pkt 1"))
        assertTrue(text.contains("państwowego funduszu celowego"))
        assertTrue(text.contains("Dyspozytora Celu"))
        assertTrue(text.contains("wniosek do ministra"))
        assertTrue(text.contains("obywateli"))
        assertTrue(text.contains("rozdzielną ewidencję"))
        assertTrue(text.contains("art. 29"))
    }

    @Test
    fun interpretationHasOfficialLegalSourceReferences() {
        val text = NON_PARLIAMENTARY_MANDATE_INTERPRETATION
        assertTrue(text.contains("https://www.sejm.gov.pl/prawo/konst/polski/1.htm"))
        assertTrue(text.contains("https://www.prezydent.pl/prawo/konstytucja-rp/rozdzial-iv-sejm-i-senat"))
        assertTrue(text.contains("D20261282.pdf"))
    }
}
