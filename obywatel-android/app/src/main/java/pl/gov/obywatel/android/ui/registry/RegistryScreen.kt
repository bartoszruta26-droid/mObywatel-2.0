// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.registry

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class RegistryEntry(val id: String, val title: String, val createdAt: Long)

private object LocalRegistryStore {
    private const val PREFS = "local_document_registry"
    private const val KEY_ENTRIES = "entries"

    fun read(context: Context): List<RegistryEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ENTRIES, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                RegistryEntry(item.getString("id"), item.getString("title"), item.getLong("createdAt"))
            }
        }.getOrDefault(emptyList()).sortedByDescending { it.createdAt }
    }

    fun add(context: Context, title: String): RegistryEntry {
        val entries = read(context).toMutableList()
        val entry = RegistryEntry(UUID.randomUUID().toString(), title, System.currentTimeMillis())
        entries.add(entry)
        val array = JSONArray()
        entries.forEach {
            array.put(JSONObject().put("id", it.id).put("title", it.title).put("createdAt", it.createdAt))
        }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ENTRIES, array.toString()).commit()) {
            "Nie udało się zapisać wpisu rejestru."
        }
        return entry
    }
}

@Composable
fun RegistryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val entries = remember { mutableStateListOf<RegistryEntry>().apply { addAll(LocalRegistryStore.read(context)) } }
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<RegistryEntry?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize().semantics { paneTitle = "Rejestr dokumentów" },
        color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text("Rejestr dokumentów", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).semantics { heading() })
            }
            Text(
                "Lokalny rejestr identyfikatorów. Wpis nie potwierdza urzędowego pochodzenia dokumentu ani uprawnień wystawcy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; error = null },
                label = { Text("Nazwa lub tytuł dokumentu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val clean = title.trim()
                    if (clean.isBlank()) {
                        error = "Wpisz tytuł dokumentu."
                    } else {
                        runCatching { LocalRegistryStore.add(context, clean) }
                            .onSuccess { entries.add(0, it); title = ""; selected = it; error = null }
                            .onFailure { error = it.message ?: "Nie udało się utworzyć wpisu." }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Icon(Icons.Filled.QrCode2, contentDescription = null); Spacer(Modifier.size(8.dp)); Text("Utwórz wpis i kod QR") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp)) }

            selected?.let { entry ->
                val qr = remember(entry.id) { createQrBitmap("urn:rp:local-document-registry:${entry.id}") }
                Card(Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        qr?.let { Image(it.asImageBitmap(), contentDescription = "Kod QR lokalnego identyfikatora dokumentu", modifier = Modifier.size(116.dp)) }
                        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.title, fontWeight = FontWeight.Bold)
                            Text("ID: ${entry.id}", style = MaterialTheme.typography.labelSmall)
                            Text("Kod zawiera lokalny identyfikator, nie poświadczenie państwowe.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Wpisy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() })
            if (entries.isEmpty()) {
                Text("Rejestr jest pusty.", modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries, key = { it.id }) { entry ->
                        Card(onClick = { selected = entry }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(entry.title, fontWeight = FontWeight.SemiBold)
                                Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("pl", "PL")).format(Date(entry.createdAt)),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Pokaż kod QR", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createQrBitmap(payload: String): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320)
    Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until 320) for (y in 0 until 320) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}.getOrNull()
