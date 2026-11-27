// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.UUID
import kotlin.math.abs

internal data class CertificateSignatureEntry(val id: String, val name: String)

internal fun isValidCertificateSignatureId(id: String): Boolean =
    id.matches(Regex("[A-Za-z0-9-]{1,80}"))

internal object CertificateSignatureStore {
    private const val PREFS = "certificate_signature_library"
    private const val ENTRIES = "entries"
    private const val SELECTED = "selected"
    private const val KEY_ALIAS = "certificate_signature_assets_aes"

    private fun directory(context: Context): File =
        File(context.noBackupFilesDir, "certificate-signatures").apply { mkdirs() }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(encrypted: ByteArray): ByteArray {
        require(encrypted.size > 28) { "Zaszyfrowany podpis jest uszkodzony." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, encrypted.copyOfRange(0, 12)))
        return cipher.doFinal(encrypted.copyOfRange(12, encrypted.size))
    }

    private fun writeEncrypted(file: File, plain: ByteArray) {
        val atomic = AtomicFile(file)
        val output = atomic.startWrite()
        try {
            output.write(encrypt(plain))
            atomic.finishWrite(output)
        } catch (failure: Throwable) {
            atomic.failWrite(output)
            throw failure
        }
    }

    private fun readEncrypted(file: File): ByteArray =
        decrypt(AtomicFile(file).openRead().use { it.readBytes() })

    fun list(context: Context): List<CertificateSignatureEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ENTRIES, "[]")
        return runCatching {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optString("id")
                val name = item.optString("name")
                if (!isValidCertificateSignatureId(id) || name.isBlank() ||
                    !File(directory(context), "$id.enc").isFile) null
                else CertificateSignatureEntry(id, name)
            }
        }.getOrDefault(emptyList())
    }

    fun selectedId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(SELECTED, null)?.takeIf(::isValidCertificateSignatureId)

    fun select(context: Context, id: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (id == null) remove(SELECTED) else putString(SELECTED, id)
        }.apply()
    }

    fun save(context: Context, name: String, bitmap: Bitmap): CertificateSignatureEntry {
        require(name.isNotBlank()) { "Podaj nazwę podpisu." }
        require(bitmap.width <= 4096 && bitmap.height <= 4096 && bitmap.width.toLong() * bitmap.height <= 16_000_000L) { "Obraz podpisu ma zbyt duże wymiary." }
        val id = UUID.randomUUID().toString()
        val output = ByteArrayOutputStream()
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
            "Nie udało się przygotować obrazu podpisu."
        }
        val file = File(directory(context), "$id.enc")
        writeEncrypted(file, output.toByteArray())
        val entries = list(context) + CertificateSignatureEntry(id, name.trim().take(60))
        val json = JSONArray().apply {
            entries.forEach { put(JSONObject().put("id", it.id).put("name", it.name)) }
        }
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, json.toString()).putString(SELECTED, id).commit()) {
            "Nie udało się zapisać biblioteki podpisów."
        }
        return CertificateSignatureEntry(id, name.trim().take(60))
    }

    fun delete(context: Context, id: String) {
        require(isValidCertificateSignatureId(id)) { "Nieprawidłowy identyfikator podpisu." }
        File(directory(context), "$id.enc").delete()
        val entries = list(context).filterNot { it.id == id }
        val json = JSONArray().apply {
            entries.forEach { put(JSONObject().put("id", it.id).put("name", it.name)) }
        }
        val edit = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, json.toString())
        if (selectedId(context) == id) {
            if (entries.isEmpty()) edit.remove(SELECTED) else edit.putString(SELECTED, entries.first().id)
        }
        check(edit.commit()) { "Nie udało się zaktualizować biblioteki podpisów." }
    }

    fun bitmap(context: Context, id: String): Bitmap? {
        require(isValidCertificateSignatureId(id)) { "Nieprawidłowy identyfikator podpisu." }
        val file = File(directory(context), "$id.enc")
        return if (file.isFile) runCatching {
            val bytes = readEncrypted(file)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull() else null
    }

    fun selectedBitmap(context: Context): Bitmap? {
        val id = selectedId(context) ?: return null
        return bitmap(context, id)
    }

    fun decode(context: Context, uri: Uri): Bitmap {
        // Najpierw odczytaj wyłącznie nagłówek obrazu. Limit wymiarów musi być
        // sprawdzony przed alokacją bitmapy, a nie dopiero po pełnym dekodowaniu.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: error("Nie można odczytać wybranego pliku graficznego.")
        require(bounds.outWidth in 1..4096 && bounds.outHeight in 1..4096 &&
            bounds.outWidth.toLong() * bounds.outHeight <= 16_000_000L) {
            "Plik ma zbyt duże wymiary. Maksymalnie 4096 × 4096 pikseli i 16 megapikseli."
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: error("Nie można zdekodować wybranego obrazu.")
    }

    fun removeBackground(source: Bitmap, colorHex: String, tolerance: Int): Bitmap {
        require(tolerance in 0..255) { "Tolerancja koloru musi mieścić się w zakresie 0–255." }
        require(!source.isRecycled && source.width > 0 && source.height > 0) { "Obraz źródłowy jest niedostępny." }
        val target = android.graphics.Color.parseColor(colorHex)
        val targetR = android.graphics.Color.red(target)
        val targetG = android.graphics.Color.green(target)
        val targetB = android.graphics.Color.blue(target)
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val row = IntArray(source.width)
        for (y in 0 until source.height) {
            source.getPixels(row, 0, source.width, 0, y, source.width, 1)
            for (x in row.indices) {
                val pixel = row[x]
                val distance = maxOf(
                    abs(android.graphics.Color.red(pixel) - targetR),
                    abs(android.graphics.Color.green(pixel) - targetG),
                    abs(android.graphics.Color.blue(pixel) - targetB)
                )
                if (distance <= tolerance) row[x] = pixel and 0x00FFFFFF
            }
            result.setPixels(row, 0, result.width, 0, y, result.width, 1)
        }
        return result
    }
}

@Composable
internal fun CertificateSignaturesManager(onSelectionChanged: () -> Unit = {}) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(CertificateSignatureStore.list(context)) }
    var selected by remember { mutableStateOf(CertificateSignatureStore.selectedId(context)) }
    var signatureName by remember { mutableStateOf("Podpis wystawcy") }
    var backgroundColor by remember { mutableStateOf("#FFFFFF") }
    var tolerance by remember { mutableStateOf(28f) }
    var message by remember { mutableStateOf("") }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var padWidth by remember { mutableFloatStateOf(1f) }
    var padHeight by remember { mutableFloatStateOf(1f) }
    var exportingSignatureId by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        entries = CertificateSignatureStore.list(context)
        selected = CertificateSignatureStore.selectedId(context)
        onSelectionChanged()
    }

    val signatureExportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val id = exportingSignatureId
        if (uri != null && id != null) runCatching {
            val bitmap = CertificateSignatureStore.bitmap(context, id)
                ?: error("Nie można odczytać podpisu z biblioteki.")
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        "Nie udało się przygotować pliku PNG."
                    }
                } ?: error("Nie można zapisać wybranego pliku.")
            } finally {
                bitmap.recycle()
            }
            message = "Wyeksportowano podpis jako PNG z przezroczystym tłem."
        }.onFailure { message = "Eksport podpisu nie powiódł się: ${it.message ?: "błąd"}" }
        exportingSignatureId = null
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val source = CertificateSignatureStore.decode(context, uri)
                try {
                    val color = backgroundColor.trim()
                    require(runCatching { android.graphics.Color.parseColor(color) }.isSuccess) {
                        "Podaj poprawny kolor tła, np. #FFFFFF."
                    }
                    val transparent = CertificateSignatureStore.removeBackground(source, color, tolerance.toInt())
                    try {
                        CertificateSignatureStore.save(context, signatureName, transparent)
                    } finally {
                        transparent.recycle()
                    }
                } finally {
                    source.recycle()
                }
            }.onSuccess {
                refresh()
                message = "Dodano podpis graficzny do biblioteki."
            }.onFailure {
                message = it.localizedMessage ?: "Nie udało się dodać podpisu."
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Biblioteka podpisów", style = MaterialTheme.typography.titleMedium)
        Text(
            "Dodaj podpis z pliku albo narysuj go palcem lub rysikiem. Obrazy są przechowywane lokalnie w pamięci aplikacji.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = signatureName,
            onValueChange = { signatureName = it.take(60) },
            label = { Text("Nazwa podpisu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Import podpisu z pliku", style = MaterialTheme.typography.titleSmall)
                Text("Obsługiwane formaty: PNG, JPG i WEBP. Plik zostanie zapisany jako PNG z przezroczystym tłem.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = backgroundColor,
                    onValueChange = { backgroundColor = it.take(9) },
                    label = { Text("Kolor tła do usunięcia (np. #FFFFFF)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Tolerancja koloru: ${tolerance.toInt()} / 255", style = MaterialTheme.typography.bodySmall)
                Slider(value = tolerance, onValueChange = { tolerance = it }, valueRange = 0f..120f)
                Button(onClick = { imagePicker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                    modifier = Modifier.fillMaxWidth()) { Text("Wybierz plik graficzny") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2. Podpis odręczny", style = MaterialTheme.typography.titleSmall)
                Text("Rysuj palcem lub rysikiem. Krzywe są wygładzane interpolacją kwadratową; tło pozostaje przezroczyste.",
                    style = MaterialTheme.typography.bodySmall)
                Canvas(
                    Modifier.fillMaxWidth().height(150.dp)
                        .onSizeChanged { padWidth = it.width.toFloat().coerceAtLeast(1f); padHeight = it.height.toFloat().coerceAtLeast(1f) }
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { point -> activeStroke = listOf(point) },
                                onDrag = { change, _ ->
                                    activeStroke = activeStroke + change.position
                                    change.consume()
                                },
                                onDragEnd = {
                                    if (activeStroke.isNotEmpty()) strokes.add(activeStroke)
                                    activeStroke = emptyList()
                                },
                                onDragCancel = { activeStroke = emptyList() }
                            )
                        }
                ) {
                    val all = strokes.toList() + listOfNotNull(activeStroke.takeIf { it.isNotEmpty() })
                    all.forEach { points ->
                        if (points.size == 1) {
                            drawCircle(Color(0xFF17202A), 2.2.dp.toPx(), points.first())
                        } else if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val previous = points[i - 1]
                                    val current = points[i]
                                    val midpoint = Offset((previous.x + current.x) / 2f, (previous.y + current.y) / 2f)
                                    quadraticTo(previous.x, previous.y, midpoint.x, midpoint.y)
                                }
                                lineTo(points.last().x, points.last().y)
                            }
                            drawPath(path, Color(0xFF17202A),
                                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },
                        modifier = Modifier.weight(1f)) { Text("Cofnij") }
                    OutlinedButton(onClick = { strokes.clear(); activeStroke = emptyList() },
                        modifier = Modifier.weight(1f)) { Text("Wyczyść") }
                }
                Button(
                    onClick = {
                        runCatching {
                            require(strokes.isNotEmpty()) { "Najpierw narysuj podpis." }
                            val bitmap = Bitmap.createBitmap(1200, 420, Bitmap.Config.ARGB_8888)
                            try {
                                val canvas = AndroidCanvas(bitmap)
                                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                    color = android.graphics.Color.rgb(23, 32, 42)
                                    style = Paint.Style.STROKE
                                    strokeWidth = 7f
                                    strokeCap = Paint.Cap.ROUND
                                    strokeJoin = Paint.Join.ROUND
                                }
                                val path = AndroidPath()
                                strokes.forEach { points ->
                                    if (points.isEmpty()) return@forEach
                                    if (points.size == 1) {
                                        val point = points.first()
                                        canvas.drawCircle(point.x / padWidth * 1120f + 40f, point.y / padHeight * 340f + 40f, 7f, paint)
                                        return@forEach
                                    }
                                    val first = points.first()
                                    path.moveTo(first.x / padWidth * 1120f + 40f, first.y / padHeight * 340f + 40f)
                                    for (i in 1 until points.size) {
                                        val previous = points[i - 1]
                                        val current = points[i]
                                        val midX = (previous.x + current.x) / 2f
                                        val midY = (previous.y + current.y) / 2f
                                        path.quadTo(
                                            previous.x / padWidth * 1120f + 40f,
                                            previous.y / padHeight * 340f + 40f,
                                            midX / padWidth * 1120f + 40f,
                                            midY / padHeight * 340f + 40f
                                        )
                                    }
                                    val last = points.last()
                                    path.lineTo(last.x / padWidth * 1120f + 40f, last.y / padHeight * 340f + 40f)
                                }
                                canvas.drawPath(path, paint)
                                CertificateSignatureStore.save(context, signatureName, bitmap)
                            } finally {
                                bitmap.recycle()
                            }
                        }.onSuccess {
                            strokes.clear()
                            refresh()
                            message = "Zapisano podpis odręczny w bibliotece."
                        }.onFailure {
                            message = it.localizedMessage ?: "Nie udało się zapisać podpisu."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz podpis odręczny") }
            }
        }
        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        Text("Zapisane podpisy", style = MaterialTheme.typography.titleSmall)
        if (entries.isEmpty()) {
            Text("Biblioteka jest pusta. Dodaj podpis z pliku lub narysuj nowy.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            entries.forEach { entry ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selected == entry.id,
                        onClick = {
                            CertificateSignatureStore.select(context, entry.id)
                            selected = entry.id
                            onSelectionChanged()
                        }
                    )
                    Text(entry.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = {
                        exportingSignatureId = entry.id
                        signatureExportPicker.launch(
                            "podpis-${entry.name.lowercase().replace(Regex("[^a-z0-9ąćęłńóśźż_-]+"), "-").take(36).trim('-').ifBlank { "odreczny" }}.png"
                        )
                    }) { Text("Eksportuj PNG") }
                    OutlinedButton(onClick = {
                        CertificateSignatureStore.delete(context, entry.id)
                        refresh()
                    }) { Text("Usuń") }
                }
            }
            OutlinedButton(onClick = {
                CertificateSignatureStore.select(context, null)
                refresh()
            }, modifier = Modifier.fillMaxWidth()) { Text("Nie używaj podpisu na zaświadczeniach") }
        }
    }
}


@Composable
internal fun CertificateSignatureImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selectedId = CertificateSignatureStore.selectedId(context)
    val bitmap = remember(selectedId) { CertificateSignatureStore.selectedBitmap(context) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Zapisany podpis wystawcy",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
