package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalDataPropagationTest {
    @Test fun copiesMatchingFields() {
        val values = mutableMapOf("firstNames" to "Jan Adam", "lastName" to "Kowalski",
            "birthDate" to "01-02-1990", "birthPlace" to "Warszawa",
            "citizenship" to "polskie", "sex" to "M", "pesel" to "90020112345")
        assertEquals(7, PersonalDataPropagation.apply(values))
        assertEquals("Jan Adam Kowalski", values["passportHolderNames"])
        assertEquals("Jan Adam Kowalski", values["disabilityPersonName"])
        assertEquals("01-02-1990", values["disabilityBirthDate"])
        assertEquals("Warszawa", values["passportBirthPlace"])
        assertEquals("polskie", values["passportCitizenship"])
        assertEquals("M", values["passportSex"])
        assertEquals("90020112345", values["disabilityPeselOrId"])
    }
    @Test fun ignoresBlankAndPreservesDocumentSpecificFields() {
        val values = mutableMapOf("firstNames" to "Jan", "lastName" to "Kowalski",
            "birthDate" to "", "passportNumber" to "ABC123456", "passportIssueDate" to "01-01-2020")
        PersonalDataPropagation.apply(values)
        assertEquals("Jan Kowalski", values["passportHolderNames"])
        assertNull(values["disabilityBirthDate"])
        assertEquals("ABC123456", values["passportNumber"])
        assertEquals("01-01-2020", values["passportIssueDate"])
    }
    @Test fun importedProfileReplacesOldAliases() {
        val values = mutableMapOf("firstNames" to "Anna", "lastName" to "Nowak",
            "passportHolderNames" to "Stare dane")
        PersonalDataPropagation.apply(values)
        assertEquals("Anna Nowak", values["passportHolderNames"])
    }
}
