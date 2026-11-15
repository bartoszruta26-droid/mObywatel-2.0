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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable

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

internal sealed interface MedicalTranslationModelInventory {
    data object Loading : MedicalTranslationModelInventory
    data class Available(val downloadedLanguages: Set<String>) : MedicalTranslationModelInventory
    data class ReadError(val message: String) : MedicalTranslationModelInventory
}

internal fun medicalTranslationModelStatus(
    inventory: MedicalTranslationModelInventory,
    languageCode: String
): String = when (inventory) {
    MedicalTranslationModelInventory.Loading -> "Sprawdzanie…"
    is MedicalTranslationModelInventory.ReadError -> "Błąd odczytu"
    is MedicalTranslationModelInventory.Available ->
        if (inventory.downloadedLanguages.any { it.substringBefore('-') == languageCode.substringBefore('-') }) {
            "Gotowy"
        } else {
            "Brak modelu"
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

    fun sizeDescription(): String =
        "ML Kit nie udostępnia publicznego interfejsu odczytu rozmiaru poszczególnych modeli."
}

@Composable
internal fun MedicalTranslationLanguagesSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var automatic by remember { mutableStateOf(MedicalTranslationModelSettings.automaticDownload(context)) }
    var wifiOnly by remember { mutableStateOf(MedicalTranslationModelSettings.wifiOnly(context)) }
    var inventory by remember { mutableStateOf<MedicalTranslationModelInventory>(MedicalTranslationModelInventory.Loading) }
    var busyLanguage by remember { mutableStateOf<String?>(null) }
    var operationJob by remember { mutableStateOf<Job?>(null) }
    var status by remember { mutableStateOf("") }

    suspend fun refresh() {
        inventory = try {
            MedicalTranslationModelInventory.Available(MedicalTranslationModelRepository.downloadedLanguages())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            MedicalTranslationModelInventory.ReadError(failure.message ?: "Nieznany błąd odczytu")
        }
    }

    LaunchedEffect(Unit) { refresh() }

    fun startOperation(languageCode: String, operation: String, action: suspend () -> Unit) {
        if (operationJob?.isActive == true) return
        busyLanguage = languageCode
        status = "$operation: $languageCode…"
        operationJob = scope.launch {
            try {
                action()
                status = "$operation zakończona: $languageCode."
            } catch (cancelled: CancellationException) {
                status = "$operation anulowana: $languageCode."
                throw cancelled
            } catch (failure: Exception) {
                status = "Błąd — $operation ($languageCode): ${failure.message ?: "nieznana przyczyna"}"
            } finally {
                withContext(NonCancellable) {
                    refresh()
                    busyLanguage = null
                    operationJob = null
                }
            }
        }
    }

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
        if (inventory is MedicalTranslationModelInventory.ReadError) {
            Text(
                "Nie udało się odczytać listy modeli. Statusów dostępności nie można teraz ustalić; nie oznaczają one braku modelu.",
                color = MaterialTheme.colorScheme.error
            )
            OutlinedButton(onClick = { scope.launch { refresh() } }, enabled = busyLanguage == null) {
                Text("Ponów odczyt")
            }
        }
        MedicalCertificateLanguages.supported.distinctBy { it.code.substringBefore('-') }.forEach { language ->
            val code = language.code.substringBefore('-')
            val modelStatus = medicalTranslationModelStatus(inventory, code)
            val ready = modelStatus == "Gotowy"
            val statusUnknown = inventory !is MedicalTranslationModelInventory.Available
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(language.englishName, modifier = Modifier.weight(1f))
                        Text(modelStatus,
                            color = when (modelStatus) {
                                "Błąd odczytu" -> MaterialTheme.colorScheme.error
                                "Gotowy" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = busyLanguage == null && !ready && !statusUnknown, onClick = {
                            startOperation(code, "Pobieranie") {
                                MedicalTranslationModelRepository.download(context, code)
                            }
                        }) { Text(if (busyLanguage == code) "Pobieranie…" else "Pobierz model") }
                        OutlinedButton(enabled = busyLanguage == null && ready, onClick = {
                            startOperation(code, "Usuwanie") {
                                MedicalTranslationModelRepository.delete(code)
                            }
                        }) { Text("Usuń") }
                    }
                }
            }
        }
        Text(MedicalTranslationModelRepository.sizeDescription(), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (busyLanguage != null) {
            Text("Operacja w toku: $busyLanguage")
            OutlinedButton(onClick = { operationJob?.cancel() }) { Text("Anuluj operację") }
        }
        if (status.isNotBlank()) Text(status, color = if (status.startsWith("Błąd")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}
