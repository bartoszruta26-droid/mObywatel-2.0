// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.registry

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class RegistryEntry(
    val id: String,
    val title: String,
    val createdAt: Long,
    val uri: String,
    val sha256: String
)

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
                RegistryEntry(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    createdAt = item.getLong("createdAt"),
                    uri = item.optString("uri", ""),
                    sha256 = item.optString("sha256", "")
                )
            }
        }.getOrDefault(emptyList()).sortedByDescending { it.createdAt }
    }

    fun add(context: Context, entry: RegistryEntry) {
        val entries = read(context).toMutableList()
        require(entries.none { it.id == entry.id })
        entries.add(entry)
        val array = JSONArray()
        entries.forEach {
            array.put(JSONObject()
                .put("id", it.id)
                .put("title", it.title)
                .put("createdAt", it.createdAt)
                .put("uri", it.uri)
                .put("sha256", it.sha256))
        }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ENTRIES, array.toString()).commit()) {
            "Nie udało się zapisać wpisu rejestru."
        }
    }
}

@Composable
fun RegistryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries = remember {
        mutableStateListOf<RegistryEntry>().apply { addAll(LocalRegistryStore.read(context)) }
    }
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<RegistryEntry?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var verification by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            error = null
            scope.launch {
                try {
                    val entry = withContext(Dispatchers.IO) {
                        val name = displayName(context, uri)
                        val digest = context.contentResolver.openInputStream(uri)?.use(::sha256)
                            ?: error("Nie można odczytać wybranego pliku.")
                        RegistryEntry(
                            id = UUID.randomUUID().toString(),
                            title = title.trim().ifBlank { name },
                            createdAt = System.currentTimeMillis(),
                            uri = uri.toString(),
                            sha256 = digest
                        )
                    }
                    val hadPermission = context.contentResolver.persistedUriPermissions.any {
                        it.uri == uri && it.isReadPermission
                    }
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        withContext(Dispatchers.IO) { LocalRegistryStore.add(context, entry) }
                        entries.add(0, entry)
                        selected = entry
                        title = ""
                        verification = "Zarejestrowano plik. Zapisano jego SHA-256."
                    } catch (failure: Exception) {
                        if (!hadPermission) runCatching {
                            context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        throw failure
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    error = failure.message ?: "Nie udało się zarejestrować pliku."
                } finally {
                    busy = false
                }
            }
        }
    }

    val qrImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            verification = null
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        val bitmap = context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        } ?: error("Nie można odczytać obrazu.")
                        decodeQr(bitmap)
                    }
                    val prefix = "urn:rp:local-document-registry:"
                    if (!result.startsWith(prefix)) {
                        verification = "Kod QR nie należy do lokalnego formatu rejestru tej aplikacji."
                    } else {
                        val id = result.removePrefix(prefix)
                        val match = entries.firstOrNull { it.id == id }
                        verification = if (match == null) {
                            "Odczytano identyfikator $id, ale brak odpowiadającego wpisu w tym urządzeniu."
                        } else {
                            val currentHash = withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(Uri.parse(match.uri))?.use(::sha256)
                                    ?: error("Nie można ponownie odczytać pliku źródłowego.")
                            }
                            if (currentHash.equals(match.sha256, ignoreCase = true)) {
                                "Zgodność: plik ma taki sam SHA-256 jak w chwili rejestracji. To lokalne sprawdzenie integralności, a nie potwierdzenie autentyczności lub pochodzenia."
                            } else {
                                "OSTRZEŻENIE: SHA-256 pliku różni się od wartości zapisanej w rejestrze. Plik mógł zostać zmieniony."
                            }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    verification = failure.message ?: "Nie udało się odczytać kodu QR."
                } finally {
                    busy = false
                }
            }
        }
    }

    Surface(
        Modifier.fillMaxSize().semantics { paneTitle = "Rejestr dokumentów" },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    "Rejestr dokumentów",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
            }
            Text(
                "Rejestr lokalny wiąże wpis z plikiem przez SHA-256. Sprawdza integralność pliku względem zapisanego skrótu; nie potwierdza urzędowego pochodzenia ani tożsamości wystawcy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; error = null },
                label = { Text("Nazwa dokumentu (opcjonalnie)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    documentPicker.launch(arrayOf("*/*"))
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCode2, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Wybierz plik i zarejestruj SHA-256")
            }
            OutlinedButton(
                onClick = { qrImagePicker.launch(arrayOf("image/*")) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Odczytaj QR z obrazu")
            }
            if (busy) CircularProgressIndicator(Modifier.padding(8.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite })
            }
            verification?.let {
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(it, Modifier.padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            selected?.let { entry ->
                val qr = remember(entry.id) { createQrBitmap("urn:rp:local-document-registry:${entry.id}") }
                Card(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        qr?.let {
                            Image(it.asImageBitmap(), contentDescription = "Kod QR lokalnego identyfikatora dokumentu",
                                modifier = Modifier.size(112.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.title, fontWeight = FontWeight.Bold)
                            Text("ID: ${entry.id}", style = MaterialTheme.typography.labelSmall)
                            Text("SHA-256: ${entry.sha256}", style = MaterialTheme.typography.labelSmall)
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
                        Card(onClick = { selected = entry; verification = null }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(entry.title, fontWeight = FontWeight.SemiBold)
                                Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("pl", "PL")).format(Date(entry.createdAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("SHA-256: ${entry.sha256.take(16)}…",
                                    style = MaterialTheme.typography.labelSmall)
                                Text("Pokaż kod i sprawdź integralność",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun displayName(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        ?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment ?: "Dokument"
}

private fun sha256(input: InputStream): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(64 * 1024)
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        digest.update(buffer, 0, count)
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

private fun decodeQr(bitmap: Bitmap): String {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
    val reader = MultiFormatReader()
    reader.setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
    return reader.decode(BinaryBitmap(HybridBinarizer(source))).text
}

private fun createQrBitmap(payload: String): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320)
    Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until 320) for (y in 0 until 320) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}.getOrNull()
