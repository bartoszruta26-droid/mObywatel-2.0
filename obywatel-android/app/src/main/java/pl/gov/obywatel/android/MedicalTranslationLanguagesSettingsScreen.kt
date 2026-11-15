// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

internal object MedicalTranslationModelSettings {
    private const val PREFS = "medical_translation_model_settings"
    private const val AUTO_DOWNLOAD = "automatic_download"
    private const val WIFI_ONLY = "wifi_only"

    fun automaticDownload(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(AUTO_DOWNLOAD, true)

    fun wifiOnly(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(WIFI_ONLY, false)

    fun setAutomaticDownload(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(AUTO_DOWNLOAD, enabled).apply()
    }

    fun setWifiOnly(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(WIFI_ONLY, enabled).apply()
    }
}

internal object MedicalTranslationModelRepository {
    private fun model(languageCode: String): TranslateRemoteModel =
        TranslateRemoteModel.Builder(mlKitLanguageCode(languageCode)).build()

    suspend fun downloadedLanguages(): Set<String> {
        val models = RemoteModelManager.getInstance()
            .getDownloadedModels(TranslateRemoteModel::class.java).await()
        val downloadedCodes = models.map { it.language }.toSet()
        return MedicalCertificateLanguages.supported
            .filter { runCatching { mlKitLanguageCode(it.code) in downloadedCodes }.getOrDefault(false) }
            .map { it.code }.toSet()
    }

    suspend fun download(context: Context, languageCode: String) {
        val conditions = DownloadConditions.Builder().apply {
            if (MedicalTranslationModelSettings.wifiOnly(context)) requireWifi()
        }.build()
        RemoteModelManager.getInstance().download(model(languageCode), conditions).await()
    }

    suspend fun delete(languageCode: String) {
        RemoteModelManager.getInstance().deleteDownloadedModel(model(languageCode)).await()
    }

    suspend fun isDownloaded(languageCode: String): Boolean =
        runCatching { mlKitLanguageCode(languageCode) in RemoteModelManager.getInstance()
            .getDownloadedModels(TranslateRemoteModel::class.java).await().map { it.language }.toSet() }
            .getOrDefault(false)

    fun sizeDescription(): String =
        "ML Kit nie udostępnia publicznego interfejsu odczytu rozmiaru poszczególnych modeli."
}

@Composable
internal fun MedicalTranslationLanguagesSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var automatic by remember { mutableStateOf(MedicalTranslationModelSettings.automaticDownload(context)) }
    var wifiOnly by remember { mutableStateOf(MedicalTranslationModelSettings.wifiOnly(context)) }
    var downloaded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var busyLanguage by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }

    suspend fun refresh() { downloaded = MedicalTranslationModelRepository.downloadedLanguages() }
    LaunchedEffect(Unit) { runCatching { refresh() }.onFailure { status = "Nie udało się odczytać modeli: ${it.message}" } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Języki i tłumaczenia", style = MaterialTheme.typography.headlineSmall)
        Text("Modele tłumaczeniowe ML Kit są przechowywane lokalnie na urządzeniu. Treść zaświadczenia jest tłumaczona lokalnie.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = automatic, onCheckedChange = {
                        automatic = it
                        MedicalTranslationModelSettings.setAutomaticDownload(context, it)
                    })
                    Text("Pobieraj automatycznie brakujące modele")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wifiOnly, onCheckedChange = {
                        wifiOnly = it
                        MedicalTranslationModelSettings.setWifiOnly(context, it)
                    })
                    Text("Pobieraj wyłącznie przez Wi-Fi")
                }
            }
        }
        Text("Dostępne modele językowe", style = MaterialTheme.typography.titleMedium)
        MedicalCertificateLanguages.supported.distinctBy { it.code.substringBefore('-') }.forEach { language ->
            val code = language.code.substringBefore('-')
            val ready = downloaded.any { it.substringBefore('-') == code }
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(language.englishName, modifier = Modifier.weight(1f))
                        Text(if (ready) "Gotowy" else "Brak modelu",
                            color = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = busyLanguage == null && !ready, onClick = {
                            busyLanguage = code
                            status = "Pobieranie modelu: ${language.englishName}…"
                            scope.launch {
                                runCatching { MedicalTranslationModelRepository.download(context, code) }
                                    .onSuccess { status = "Model ${language.englishName} został pobrany." }
                                    .onFailure { status = "Błąd pobierania ${language.englishName}: ${it.message}" }
                                runCatching { refresh() }
                                busyLanguage = null
                            }
                        }) { Text(if (busyLanguage == code) "Pobieranie…" else "Pobierz model") }
                        OutlinedButton(enabled = busyLanguage == null && ready, onClick = {
                            busyLanguage = code
                            scope.launch {
                                runCatching { MedicalTranslationModelRepository.delete(code) }
                                    .onSuccess { status = "Usunięto model ${language.englishName}." }
                                    .onFailure { status = "Nie udało się usunąć modelu: ${it.message}" }
                                runCatching { refresh() }
                                busyLanguage = null
                            }
                        }) { Text("Usuń") }
                    }
                }
            }
        }
        Text(MedicalTranslationModelRepository.sizeDescription(), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (busyLanguage != null) Text("Operacja w toku: ${busyLanguage}")
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary)
    }
}
