// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.tasks.await

/**
 * On-device translation. The clinical text is processed by the ML Kit translator
 * on the device; only the language model is downloaded when it is not cached.
 */
internal class MlKitMedicalTranslationProvider(private val context: Context) : MedicalTranslationProvider {
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
            listOf(source, target).distinct().forEach { language ->
                val model = TranslateRemoteModel.Builder(language).build()
                val downloaded = RemoteModelManager.getInstance()
                    .getDownloadedModels(TranslateRemoteModel::class.java).await()
                    .any { it.language == language }
                if (!downloaded) {
                    check(MedicalTranslationModelSettings.automaticDownload(context)) {
                        "Brak pobranego modelu językowego. Pobierz modele języków źródłowego i docelowego w Więcej → Ustawienia → Języki i tłumaczenia."
                    }
                    val conditions = DownloadConditions.Builder().apply {
                        if (MedicalTranslationModelSettings.wifiOnly(context)) requireWifi()
                    }.build()
                    RemoteModelManager.getInstance().download(model, conditions).await()
                }
            }
            translator.translate(text).await()
        } finally {
            translator.close()
        }
    }
}

internal fun mlKitLanguageCode(code: String): String {
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
