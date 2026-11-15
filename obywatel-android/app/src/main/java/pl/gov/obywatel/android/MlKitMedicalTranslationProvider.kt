// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import kotlinx.coroutines.tasks.await

/**
 * On-device translation. The clinical text is processed by the ML Kit translator
 * on the device; only the language model is downloaded when it is not cached.
 */
internal class MlKitMedicalTranslationProvider : MedicalTranslationProvider {
    override val providerId: String = "google-mlkit-on-device"

    override suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        if (text.isBlank()) return text
        val source = mlKitLanguageCode(sourceLanguage)
        val target = mlKitLanguageCode(targetLanguage)
        require(source != target) { "Source and target languages must differ." }
        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(source)
                .setTargetLanguage(target)
                .build()
        )
        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            translator.translate(text).await()
        } finally {
            translator.close()
        }
    }
}

private fun mlKitLanguageCode(code: String): String {
    val normalized = code.substringBefore('-').lowercase()
    return when (normalized) {
        "en" -> TranslateLanguage.ENGLISH
        "he" -> TranslateLanguage.HEBREW
        "de" -> TranslateLanguage.GERMAN
        "es" -> TranslateLanguage.SPANISH
        "fr" -> TranslateLanguage.FRENCH
        "it" -> TranslateLanguage.ITALIAN
        "pl" -> TranslateLanguage.POLISH
        "bg" -> TranslateLanguage.BULGARIAN
        "ro" -> TranslateLanguage.ROMANIAN
        "nl" -> TranslateLanguage.DUTCH
        "pt" -> TranslateLanguage.PORTUGUESE
        "sv" -> TranslateLanguage.SWEDISH
        else -> throw IllegalArgumentException("This language is not supported by the on-device translation model.")
    }
}
