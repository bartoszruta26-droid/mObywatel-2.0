package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicalTranslationModelInventoryTest {
    @Test
    fun readFailureIsNeverReportedAsMissingModel() {
        val inventory = MedicalTranslationModelInventory.ReadError("network unavailable")
        assertEquals("Błąd odczytu", medicalTranslationModelStatus(inventory, "pl"))
    }

    @Test
    fun emptySuccessfullyReadInventoryMeansModelIsMissing() {
        val inventory = MedicalTranslationModelInventory.Available(emptySet())
        assertEquals("Brak modelu", medicalTranslationModelStatus(inventory, "pl"))
    }

    @Test
    fun downloadedBaseLanguageMarksRegionalVariantReady() {
        val inventory = MedicalTranslationModelInventory.Available(setOf("pl"))
        assertEquals("Gotowy", medicalTranslationModelStatus(inventory, "pl-PL"))
    }

    @Test
    fun loadingInventoryDoesNotPretendModelIsMissing() {
        assertEquals("Sprawdzanie…", medicalTranslationModelStatus(MedicalTranslationModelInventory.Loading, "pl"))
    }
}
