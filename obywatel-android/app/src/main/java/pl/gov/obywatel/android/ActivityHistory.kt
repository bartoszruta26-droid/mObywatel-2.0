// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class ActivityHistoryEntry(
    val id: String,
    val timestamp: Long,
    val category: String,
    val action: String,
    val details: String
)

internal object ActivityHistoryStore {
    private const val FILE_NAME = "activity-history.enc"
    private const val KEY_ALIAS = "obywatel_activity_history_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    @Synchronized
    fun record(context: Context, category: String, action: String, details: String = "") {
        val entries = load(context).toMutableList()
        entries.add(0, ActivityHistoryEntry(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            category = category.take(60),
            action = action.take(160),
            details = details.take(500)
        ))
        save(context, entries.take(300))
    }

    @Synchronized
    fun load(context: Context): List<ActivityHistoryEntry> = runCatching {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val encrypted = file.readBytes()
        require(encrypted.size > 28) { "Uszkodzony plik historii." }
        val iv = encrypted.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONArray(String(cipher.doFinal(encrypted.copyOfRange(12, encrypted.size)), Charsets.UTF_8))
        (0 until json.length()).map { i ->
            val item = json.getJSONObject(i)
            ActivityHistoryEntry(
                item.getString("id"), item.getLong("timestamp"),
                item.getString("category"), item.getString("action"),
                item.optString("details")
            )
        }
    }.getOrDefault(emptyList())

    @Synchronized
    fun clear(context: Context) {
        File(context.noBackupFilesDir, FILE_NAME).delete()
    }

    private fun save(context: Context, entries: List<ActivityHistoryEntry>) {
        val json = JSONArray()
        entries.forEach { entry ->
            json.put(JSONObject().apply {
                put("id", entry.id)
                put("timestamp", entry.timestamp)
                put("category", entry.category)
                put("action", entry.action)
                put("details", entry.details)
            })
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temporary = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        temporary.writeBytes(encrypted)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
}

@Composable
internal fun ActivityHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(ActivityHistoryStore.load(context)) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    var showClearDialog by remember { mutableStateOf(false) }
    val categories = listOf("Wszystkie", "Nawigacja", "Dokumenty", "Certyfikaty", "Ustawienia", "Informacje")
    val visible = entries.filter { entry ->
        (selectedCategory == "Wszystkie" || entry.category == selectedCategory) &&
            (query.isBlank() || entry.action.contains(query, true) ||
                entry.details.contains(query, true) || entry.category.contains(query, true))
    }
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy 'o' HH:mm:ss", Locale("pl", "PL")) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("‹  Więcej", color = Color(0xFF1769AA), fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Historia działań", color = Color(0xFF142D4E), fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Text("Lokalny rejestr zdarzeń aplikacji. Zapisywane są czynności nawigacyjne i operacje, które aplikacja jawnie rejestruje.",
            color = Color(0xFF667085), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Szukaj w historii") })
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { category ->
                OutlinedButton(onClick = { selectedCategory = category }) {
                    Text(if (selectedCategory == category) "✓ $category" else category)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Zdarzenia: ${visible.size} / ${entries.size}", color = Color(0xFF344054),
                fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { showClearDialog = true }, enabled = entries.isNotEmpty()) {
                Text("Wyczyść historię")
            }
        }
        if (visible.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Brak zdarzeń", fontWeight = FontWeight.Bold, color = Color(0xFF142D4E))
                    Text("Nowe czynności będą pojawiać się tutaj w miarę korzystania z aplikacji.",
                        color = Color(0xFF667085))
                }
            }
        } else visible.forEach { entry ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.category, color = Color(0xFF1769AA), fontWeight = FontWeight.Bold)
                        Text(formatter.format(Date(entry.timestamp)), color = Color(0xFF667085),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                    Text(entry.action, color = Color(0xFF142D4E), fontWeight = FontWeight.SemiBold)
                    if (entry.details.isNotBlank()) Text(entry.details, color = Color(0xFF475467),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (showClearDialog) AlertDialog(
        onDismissRequest = { showClearDialog = false },
        title = { Text("Wyczyścić historię?") },
        text = { Text("Usunięte zostaną lokalne wpisy historii działań. Operacja nie usuwa dokumentów ani danych profilu.") },
        confirmButton = {
            Button(onClick = {
                ActivityHistoryStore.clear(context)
                entries = emptyList()
                showClearDialog = false
            }) { Text("Wyczyść") }
        },
        dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Anuluj") } }
    )
}
