package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicalTranslationModelSettingsTest {
    @Test
    fun defaultsAndPersistedAutomaticDownloadAndWifiPreferencesAreCorrect() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("medical_translation_model_settings", 0)
        val previousAutomatic = MedicalTranslationModelSettings.automaticDownload(context)
        val previousWifiOnly = MedicalTranslationModelSettings.wifiOnly(context)
        try {
            preferences.edit().clear().commit()
            assertTrue(MedicalTranslationModelSettings.automaticDownload(context))
            assertFalse(MedicalTranslationModelSettings.wifiOnly(context))

            MedicalTranslationModelSettings.setAutomaticDownload(context, false)
            MedicalTranslationModelSettings.setWifiOnly(context, true)
            assertFalse(MedicalTranslationModelSettings.automaticDownload(context))
            assertTrue(MedicalTranslationModelSettings.wifiOnly(context))

            // Read again through the settings API to verify persisted values, not UI state.
            assertFalse(MedicalTranslationModelSettings.automaticDownload(context))
            assertTrue(MedicalTranslationModelSettings.wifiOnly(context))
        } finally {
            MedicalTranslationModelSettings.setAutomaticDownload(context, previousAutomatic)
            MedicalTranslationModelSettings.setWifiOnly(context, previousWifiOnly)
        }
    }

    @Test
    fun automaticDownloadCanBeReenabledIndependentlyOfWifiPreference() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previousAutomatic = MedicalTranslationModelSettings.automaticDownload(context)
        val previousWifiOnly = MedicalTranslationModelSettings.wifiOnly(context)
        try {
            MedicalTranslationModelSettings.setAutomaticDownload(context, false)
            MedicalTranslationModelSettings.setWifiOnly(context, true)
            MedicalTranslationModelSettings.setAutomaticDownload(context, true)
            assertTrue(MedicalTranslationModelSettings.automaticDownload(context))
            assertTrue(MedicalTranslationModelSettings.wifiOnly(context))
        } finally {
            MedicalTranslationModelSettings.setAutomaticDownload(context, previousAutomatic)
            MedicalTranslationModelSettings.setWifiOnly(context, previousWifiOnly)
        }
    }
}
