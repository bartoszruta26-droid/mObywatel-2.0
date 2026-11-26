// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import pl.gov.obywatel.android.network.tor.TorClientSettingsScreen
import pl.gov.obywatel.android.network.tor.StudiaOnlineBulkDownloadScreen
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.util.UUID
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.ObywatelTheme
import pl.gov.obywatel.android.ui.identity.IdentityDocumentsScreen
import pl.gov.obywatel.android.ui.documents.DocumentsScreen
import pl.gov.obywatel.android.ui.services.PublicServicesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun GeneralDocumentOcrEditor(categoryId: String, fields: MutableMap<String, String>, context: Context, saveMessage: (String) -> Unit) {
    var processing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf(fields["${categoryId}OcrText"].orEmpty()) }
    var mappedCount by remember { mutableIntStateOf(0) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    val key = "${categoryId}OcrText"

    fun applyRecognizedText(rawText: String) {
        val raw = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString("\n")
        recognizedText = raw
        fields[key] = raw
        val suggestions = DocumentOcrParser.extract(categoryId, raw)
        var applied = 0
        suggestions.forEach { (field, value) ->
            if (value.isNotBlank() && fields[field].isNullOrBlank()) { fields[field] = value; applied++ }
        }
        mappedCount = applied
        saveMessage(if (raw.isBlank()) "Nie wykryto tekstu w nagraniu. Nagraj dokument ponownie, prowadząc kamerę powoli i utrzymując ostrość."
            else "Analiza filmu zakończona. Rozpoznano tekst z klatek nagrania i zaproponowano uzupełnienie $applied pustych pól. Sprawdź każdą wartość przed zapisaniem.")
    }

    fun recognizeImage(uri: Uri) {
        processing = true
        runCatching { InputImage.fromFilePath(context, uri) }.onSuccess { image ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image).addOnSuccessListener { applyRecognizedText(it.text) }
                .addOnFailureListener { saveMessage("OCR nie powiódł się: ${it.localizedMessage ?: "błąd odczytu"}") }
                .addOnCompleteListener { recognizer.close(); processing = false }
        }.onFailure { processing = false; saveMessage("Nie można otworzyć obrazu: ${it.localizedMessage ?: "błąd pliku"}") }
    }

    fun recognizeVideo(uri: Uri, deleteAfterProcessing: Boolean) {
        processing = true
        Thread {
            val frames = mutableListOf<Bitmap>()
            val extractionError = runCatching {
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    val durationMs = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    require(durationMs > 0L) { "Nagranie jest puste albo ma nieprawidłowy format." }
                    val sampleCount = minOf(24, maxOf(1, (durationMs / 650L).toInt() + 1))
                    for (index in 0 until sampleCount) {
                        val timeUs = ((durationMs * index / sampleCount).coerceAtLeast(0L)) * 1000L
                        val original = retriever.getFrameAtTime(timeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST)
                        if (original != null) {
                            val bitmap = if (original.width > 1800) {
                                val scaled = Bitmap.createScaledBitmap(original, 1800, (original.height * 1800f / original.width).toInt(), true)
                                if (scaled !== original) original.recycle()
                                scaled
                            } else original
                            frames.add(bitmap)
                        }
                    }
                } finally { retriever.release() }
                require(frames.isNotEmpty()) { "Nie udało się pobrać klatek z nagrania." }
            }.exceptionOrNull()

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (extractionError != null) {
                    frames.forEach { if (!it.isRecycled) it.recycle() }
                    processing = false
                    if (deleteAfterProcessing) runCatching { context.contentResolver.delete(uri, null, null) }
                    pendingVideoUri = null
                    saveMessage("Nie można odczytać filmu: ${extractionError.localizedMessage ?: "błąd nagrania"}")
                    return@post
                }
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val recognizedParts = mutableListOf<String>()
                lateinit var processNextFrame: (Int) -> Unit
                processNextFrame = { index ->
                    if (index >= frames.size) {
                        recognizer.close()
                        frames.forEach { if (!it.isRecycled) it.recycle() }
                        applyRecognizedText(recognizedParts.flatMap { it.lines() }.map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString("\n"))
                        processing = false
                        if (deleteAfterProcessing) runCatching { context.contentResolver.delete(uri, null, null) }
                        pendingVideoUri = null
                    } else {
                        val frame = frames[index]
                        recognizer.process(InputImage.fromBitmap(frame, 0))
                            .addOnSuccessListener { if (it.text.isNotBlank()) recognizedParts.add(it.text) }
                            .addOnFailureListener { /* Pojedyncza nieczytelna klatka nie przerywa analizy. */ }
                            .addOnCompleteListener { if (!frame.isRecycled) frame.recycle(); processNextFrame(index + 1) }
                    }
                }
                processNextFrame(0)
            }
        }.start()
    }

    fun recognizeTextFile(uri: Uri) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 5 * 1024 * 1024) { "Plik tekstowy przekracza limit 5 MB." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: error("Nie można otworzyć pliku tekstowego.")
            require(bytes.isNotEmpty()) { "Plik tekstowy jest pusty." }
            runCatching {
                Charsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            }.getOrElse { String(bytes, java.nio.charset.Charset.forName("windows-1250")) }
        }.onSuccess { applyRecognizedText(it) }
            .onFailure { saveMessage("Nie można odczytać pliku tekstowego: ${it.localizedMessage ?: "błąd pliku"}") }
    }

    fun recognizePdf(uri: Uri) {
        processing = true
        Thread {
            val pages = mutableListOf<Bitmap>()
            var totalPages = 0
            val error = runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    val renderer = android.graphics.pdf.PdfRenderer(descriptor)
                    try {
                        totalPages = renderer.pageCount
                        require(totalPages > 0) { "Dokument PDF nie zawiera stron." }
                        val pageLimit = minOf(totalPages, 10)
                        for (pageIndex in 0 until pageLimit) {
                            val page = renderer.openPage(pageIndex)
                            try {
                                val scale = minOf(1f, 1400f / maxOf(page.width, page.height))
                                val width = (page.width * scale).toInt().coerceAtLeast(1)
                                val height = (page.height * scale).toInt().coerceAtLeast(1)
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                pages.add(bitmap)
                            } finally { page.close() }
                        }
                    } finally { renderer.close() }
                } ?: error("Nie można otworzyć pliku PDF.")
            }.exceptionOrNull()
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (error != null) {
                    pages.forEach { if (!it.isRecycled) it.recycle() }
                    processing = false
                    saveMessage("Nie można przetworzyć PDF: ${error.localizedMessage ?: "błąd dokumentu"}")
                    return@post
                }
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val recognizedPages = mutableListOf<String>()
                lateinit var processPage: (Int) -> Unit
                processPage = { index ->
                    if (index >= pages.size) {
                        recognizer.close()
                        pages.forEach { if (!it.isRecycled) it.recycle() }
                        applyRecognizedText(recognizedPages.joinToString("\n"))
                        if (totalPages > pages.size) saveMessage("OCR przetworzył pierwsze ${pages.size} z ${totalPages} stron PDF. Sprawdź tekst i w razie potrzeby podziel dokument.")
                        processing = false
                    } else {
                        val bitmap = pages[index]
                        recognizer.process(InputImage.fromBitmap(bitmap, 0))
                            .addOnSuccessListener { if (it.text.isNotBlank()) recognizedPages.add(it.text) }
                            .addOnFailureListener { /* Pojedyncza strona bez czytelnego tekstu nie przerywa analizy. */ }
                            .addOnCompleteListener { if (!bitmap.isRecycled) bitmap.recycle(); processPage(index + 1) }
                    }
                }
                processPage(0)
            }
        }.start()
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) recognizeImage(it) }
    val documentOcrPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
            val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0).orEmpty() else "" }.orEmpty()
            when {
                mime == "application/pdf" || name.endsWith(".pdf", true) -> recognizePdf(uri)
                mime.startsWith("text/") || name.substringAfterLast('.', "").lowercase() in setOf("txt", "csv", "md", "rtf", "xml") -> recognizeTextFile(uri)
                mime.startsWith("image/") || name.substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp") -> recognizeImage(uri)
                else -> saveMessage("Wybierz PDF, JPG/PNG/WebP albo plik tekstowy.")
            }
        }
    }
    val cameraPhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            processing = true
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { applyRecognizedText(it.text) }
                .addOnFailureListener { saveMessage("OCR zdjęcia nie powiódł się: ${it.localizedMessage ?: "błąd odczytu"}") }
                .addOnCompleteListener {
                    recognizer.close()
                    if (!bitmap.isRecycled) bitmap.recycle()
                    processing = false
                }
        } else saveMessage("Fotografowanie dokumentu anulowano.")
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) recognizeVideo(it, false) }
    val videoCamera = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { captured ->
        val uri = pendingVideoUri
        if (captured && uri != null) recognizeVideo(uri, true) else {
            uri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
            pendingVideoUri = null
            saveMessage("Nagrywanie filmu anulowano.")
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Odczyt dokumentu przez OCR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text("Możesz rozpoznać tekst ze zdjęcia, fotografii wykonanej aparatem, pliku PDF, pliku tekstowego albo filmu. OCR proponuje uzupełnienie pasujących pól; sprawdź każdą wartość. Odczyt PDF obejmuje maksymalnie 10 pierwszych stron. OCR nie potwierdza autentyczności dokumentu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                OcrRoundAction("Nagraj film", Icons.Filled.Videocam, enabled = !processing) {
                    runCatching {
                        val file = File.createTempFile("document-ocr-video-", ".mp4", context.cacheDir)
                        androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.ocrfiles", file)
                    }.onSuccess { pendingVideoUri = it; videoCamera.launch(it) }
                        .onFailure { saveMessage("Nie można uruchomić kamery wideo: ${it.localizedMessage ?: "błąd przygotowania nagrania"}") }
                }
                OcrRoundAction("Film", Icons.Filled.VideoLibrary, enabled = !processing) {
                    videoPicker.launch("video/*")
                }
                OcrRoundAction("Plik / PDF", Icons.Filled.PictureAsPdf, enabled = !processing) {
                    documentOcrPicker.launch(arrayOf("application/pdf", "image/*", "text/plain", "text/*"))
                }
                OcrRoundAction("Aparat", Icons.Filled.PhotoCamera, enabled = !processing) {
                    cameraPhoto.launch(null)
                }
                OcrRoundAction("Zdjęcie", Icons.Filled.ImageIcon, enabled = !processing) {
                    photoPicker.launch("image/*")
                }
            }
            if (processing) Text("Analiza klatek filmu i uzupełnianie pól…", color = MaterialTheme.colorScheme.primary)
            if (mappedCount > 0) Text("Wstępnie uzupełniono $mappedCount pustych pól", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            if (recognizedText.isNotBlank()) {
                Text("Tekst rozpoznany z nagrania — sprawdź", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(recognizedText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonsSvgRetryScheduler.ensureScheduled(this)
        lifecycleScope.launch(Dispatchers.IO) { reconcileCommonsDownloadState(applicationContext) }
        // Ten sam znak i tło są wyświetlane przez okno startowe oraz ekran powitalny.
        window.setBackgroundDrawableResource(R.drawable.splash_screen)
        ApplicationAppearanceStore.applyLauncherIcon(this, ApplicationAppearanceStore.load(this).launcherIcon)
        setContent {
            var appearance by remember { mutableStateOf(ApplicationAppearanceStore.load(this@MainActivity)) }
            ObywatelTheme(themeMode = appearance.theme) {
                CompositionLocalProvider(LocalApplicationAppearance provides appearance) {
                    ApplicationEntryFlow(onAppearanceChange = { updated ->
                        ApplicationAppearanceStore.save(this@MainActivity, updated)
                        appearance = updated
                    })
                }
            }
        }
    }
}



private val services = listOf(
    ServiceItem("Portfel", "Wszystkie dokumenty i ewidencje", icon = R.drawable.service_folder, fallbackIcon = Icons.Filled.CreditCard, category = "Dokumenty"),
    ServiceItem("Odpis z rejestru podmiotów", "Dane spółki, fundacji, kościoła i instytucji szkoleniowej", fallbackIcon = Icons.Filled.AccountBalance, category = "Rejestry"),
    ServiceItem("Polak za granicą", "Informacje konsularne", icon = R.drawable.mob_abroad, fallbackIcon = Icons.Filled.Public, category = "Transport"),
    ServiceItem("Płatności", "Usługi i opłaty", icon = R.drawable.mob_payments, fallbackIcon = Icons.Filled.Payments, category = "Sprawy urzędowe"),
    ServiceItem("Zdrowie", "Informacje zdrowotne", icon = R.drawable.mob_health, fallbackIcon = Icons.Filled.LocalHospital, category = "Zdrowie"),
    ServiceItem("eKarta Szczepień", "Książeczka szczepień i historia dawek", icon = R.drawable.service_prescription, fallbackIcon = Icons.Filled.HealthAndSafety, category = "Zdrowie"),
    ServiceItem("Dane dzieci", "Rodzinny rejestr danych i dokumentacji dzieci", fallbackIcon = Icons.Filled.AccountCircle, category = "Rodzina"),
    ServiceItem("Formularze dokumentów", "Akty i dokumenty cywilne — ewidencja i załączniki", icon = R.drawable.mob_document_check, fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Dokumenty Rodowe", "Księgi, akty, rejestry i dokumenty Rodu", fallbackIcon = Icons.Filled.Fingerprint, category = "Rodzina"),
    ServiceItem("Dokumenty rejestru podmiotów", "Dokumenty firmowe i organizacyjne", fallbackIcon = Icons.Filled.AccountBalance, category = "Rejestry"),
    ServiceItem("Dokumenty Kościelne", "Dokumenty kościelne, liturgiczne i organizacyjne", fallbackIcon = Icons.Filled.AccountBalance, category = "Kościół i wspólnoty"),
    ServiceItem("Zasoby identyfikacji dokumentów", "Logo, godła, flagi, pieczęcie i tła dokumentów", fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Dokumenty PDF i podpisy elektroniczne", "Import, przechowywanie i podgląd dokumentów PDF", fallbackIcon = Icons.Filled.Description, category = "Dokumenty"),
    ServiceItem("Transport", "Podróże i przejazdy", icon = R.drawable.mob_transport, fallbackIcon = Icons.Filled.Train, category = "Podróże"),
    ServiceItem("Mandaty", "Ewidencja i informacje", icon = R.drawable.mob_penalties, fallbackIcon = Icons.Filled.Description, category = "Kierowca i pojazdy"),
    ServiceItem("Sprawdź PESEL", "Informacje o numerze PESEL", icon = R.drawable.mob_pesel_check, fallbackIcon = Icons.Filled.Fingerprint, category = "Bezpieczeństwo"),
    ServiceItem("Zastrzeż PESEL", "Informacje o zastrzeżeniu", icon = R.drawable.mob_pesel_lock, fallbackIcon = Icons.Filled.Security, category = "Bezpieczeństwo"),
    ServiceItem("Bilety", "Bilety i przejazdy", icon = R.drawable.mob_ticket, fallbackIcon = Icons.Filled.ConfirmationNumber, category = "Podróże"),
    ServiceItem("Bezpieczeństwo", "Prywatność i ochrona", icon = R.drawable.mob_cyber_safety, fallbackIcon = Icons.Filled.Shield, category = "Bezpieczeństwo"),
    ServiceItem("Środowisko", "Informacje środowiskowe", icon = R.drawable.mob_environment, fallbackIcon = Icons.Filled.Public, category = "Środowisko"),
    ServiceItem("Ustawienia", "Preferencje i dane ewidencyjne", fallbackIcon = Icons.Filled.Settings),
    ServiceItem("Zaświadczenia", "Dokumenty i dane o uprawnieniach", fallbackIcon = Icons.Filled.Description),
    ServiceItem("Sprawy rodzinne", "Dane dotyczące spraw rodzinnych", fallbackIcon = Icons.Filled.AccountCircle, category = "Rodzina"),
    ServiceItem("Edukacja", "Ewidencja nauki i dokumentów edukacyjnych", fallbackIcon = Icons.Filled.Description, category = "Edukacja"),
    ServiceItem("Praca", "Informacje i dokumenty związane z pracą", fallbackIcon = Icons.Filled.Badge, category = "Praca"),
    ServiceItem("Finanse i podatki", "Informacje dotyczące finansów i podatków", fallbackIcon = Icons.Filled.Payments, category = "Finanse i podatki"),
    ServiceItem("Nieruchomości", "Ewidencja nieruchomości użytkownika", fallbackIcon = Icons.Filled.Home, category = "Nieruchomości"),
    ServiceItem("Prawo i obywatel", "Informacje obywatelskie i prawne", fallbackIcon = Icons.Filled.AccountBalance, category = "Prawo i obywatel"),
    ServiceItem("Kultura i dziedzictwo", "Katalog instytucji i wydarzeń", fallbackIcon = Icons.Filled.Public, category = "Kultura i dziedzictwo"),
    ServiceItem("Sport", "Aktywność i lokalne obiekty sportowe", fallbackIcon = Icons.Filled.HealthAndSafety, category = "Sport"),
    ServiceItem("Organizacje społeczne", "Katalog organizacji i inicjatyw", fallbackIcon = Icons.Filled.AccountCircle, category = "Organizacje społeczne"),
    ServiceItem("Kościół i wspólnoty", "Katalog wspólnot i instytucji religijnych", fallbackIcon = Icons.Filled.AccountBalance, category = "Kościół i wspólnoty"),
    ServiceItem("Pozostałe", "Więcej modułów", fallbackIcon = Icons.Filled.MoreHoriz)
)

internal val documentDisplayOptions = (listOf(
    Triple("profile", "Dane osobowe profilu", R.drawable.lib_ic_account),
    Triple("identity", "Dowód tożsamości", R.drawable.documents_identity_card),
    Triple("passport", "Dane paszportowe", R.drawable.documents_passport),
    Triple("registry", "Odpis z rejestru podmiotów", R.drawable.court_registry_icon),
    Triple("family", "Dokument rodowy", R.drawable.documents_family_record),
    Triple("parliament", "Legitymacja poselska", R.drawable.documents_deputy_legitimacy),
    Triple("fund_purpose_disposition", "Akt powierzenia środków Dyspozytorowi Celu", R.drawable.lib_ic_wallet),
    Triple("training", "Legitymacja kursanta", R.drawable.documents_course_legitimacy),
    Triple("student", "Legitymacja ucznia/studenta", R.drawable.tile_ic_student_outline),
    Triple("disability", "Orzeczenia i zaświadczenia", R.drawable.tile_ic_disability_outline),
    Triple("church", "Dokumenty kościelne", R.drawable.tile_ic_church_outline),
    Triple("civil", "Dokumenty cywilne", R.drawable.tile_ic_civil_outline),
    Triple("business", "Dokumenty firmowe i podmiotowe", R.drawable.tile_ic_business_outline),
    Triple("bank_accounts", "Konta bankowe", R.drawable.lib_ic_wallet),
    Triple("rod_bank_accounts", "Rachunki bankowe Rodu", R.drawable.lib_ic_wallet),
    Triple("entity_bank_accounts", "Rachunki bankowe podmiotów", R.drawable.lib_ic_wallet),
    ) + paymentAccountDocumentTargets().map { (id, title, _) -> Triple(id, title, R.drawable.lib_ic_wallet) } + listOf(
    Triple("vaccines", "Książeczka szczepień WHO", R.drawable.lib_ic_medical_record),
    Triple("signed_pdf", "Dokumenty PDF i podpisy", R.drawable.lib_ic_document_signed),
    Triple("receipts", "Rachunki", R.drawable.lib_ic_document_signed),
    Triple("payment_transactions", "Rachunki płatnicze", R.drawable.lib_ic_wallet),
    Triple("imported", "Dokumenty własne — pliki", R.drawable.lib_ic_folder),
    Triple("children", "Dokumentacja dzieci", R.drawable.family),
    Triple("branding", "Zasoby identyfikacji dokumentów", R.drawable.lib_ic_image),
    Triple("loyalty", "Karty lojalnościowe sklepów", R.drawable.lib_ic_credit_card),
    Triple("birth_extract", "Akt urodzenia", R.drawable.civic_service_illustration),
    Triple("marriage_extract", "Akt małżeństwa", R.drawable.civic_service_illustration),
    Triple("death_extract", "Odpis aktu zgonu", R.drawable.civic_service_illustration),
    Triple("large_family", "Karta Dużej Rodziny", R.drawable.family),
    Triple("blood_donor", "Legitymacja Honorowego Dawcy Krwi", R.drawable.lib_ic_medical_record),
    Triple("disability_card", "Legitymacja osoby z niepełnosprawnością", R.drawable.documents_medical_certificates),
    Triple("international_medical_certificate", "International Medical Certificate", R.drawable.documents_medical_certificates),
    Triple("foreign_prescription", "Recepta zagraniczna", R.drawable.documents_medical_certificates),
    Triple(INTERNATIONAL_DONATION_WORSHIP_ID, "Międzynarodowa darowizna na cele kultu religijnego", R.drawable.tile_ic_church_outline),
    Triple(INTERNATIONAL_DONATION_NGO_ID, "Międzynarodowa darowizna na rzecz organizacji pozarządowej", R.drawable.lib_ic_building),
    Triple(INTERNATIONAL_DONATION_PERSON_ID, "Międzynarodowa darowizna na rzecz osoby fizycznej", R.drawable.lib_ic_account),
    Triple(DonationLedgerCatalog.PERSON, "Ewidencja darowizn osoby fizycznej", R.drawable.lib_ic_account),
    Triple(DonationLedgerCatalog.FOUNDATION, "Ewidencja darowizn fundacji", R.drawable.lib_ic_building),
    Triple(DonationLedgerCatalog.ASSOCIATION, "Ewidencja darowizn stowarzyszenia", R.drawable.lib_ic_building),
    Triple(DonationLedgerCatalog.CHURCH, "Ewidencja darowizn Kościoła", R.drawable.tile_ic_church_outline),
    Triple("vehicle_registration", "Dowód rejestracyjny pojazdu", R.drawable.mob_transport),
    Triple("insurance_vehicle", "Ubezpieczenie OC pojazdu", R.drawable.lib_ic_document),
    Triple("insurance_professional", "Ubezpieczenie OC zawodowe", R.drawable.lib_ic_document),
    Triple("insurance_private", "Ubezpieczenie OC w życiu prywatnym", R.drawable.lib_ic_document),
    Triple("insurance_business", "Ubezpieczenie OC działalności", R.drawable.lib_ic_document),
    Triple("criminal_record", "Zaświadczenie o niekaralności", R.drawable.lib_ic_lock),
    Triple("mission_travel", "Dokument podróży służbowej / misyjnej", R.drawable.church_communities),
    Triple("ehic", "Europejska Karta Ubezpieczenia Zdrowotnego", R.drawable.lib_ic_medical_record),
    Triple("eu_education", "Zaświadczenie o zgodności kwalifikacji z UE", R.drawable.education),
    Triple("church_bishop", "Dokument godności biskupiej", R.drawable.church_communities),
    Triple("church_archbishop", "Dokument godności arcybiskupiej", R.drawable.church_communities),
    Triple("church_cardinal", "Dokument godności kardynalskiej", R.drawable.church_communities),
    Triple("church_abbot", "Dokument godności opackiej", R.drawable.church_communities),
    Triple("church_minister", "Dokument posługi kościelnej", R.drawable.church_communities),
    Triple("church_missionary", "Dokument posługi misyjnej", R.drawable.church_communities),
    Triple("professional", "Dokumenty kwalifikacji zawodowych", R.drawable.work),
    Triple("professional_medical", "Kwalifikacje medyczne", R.drawable.work),
    Triple("professional_education", "Kwalifikacje edukacyjne", R.drawable.education),
    Triple("professional_therapeutic", "Kwalifikacje terapeutyczne", R.drawable.work),
    Triple("professional_other", "Pozostałe kwalifikacje zawodowe", R.drawable.work),
    Triple("organization_foundation", "Dokumenty fundacji", R.drawable.lib_ic_building),
    Triple("organization_church", "Dokumenty Kościoła / wspólnoty", R.drawable.church_communities),
    Triple("organization_company", "Dokumenty przedsiębiorstwa", R.drawable.lib_ic_building),
    Triple("organization_association", "Dokumenty stowarzyszenia", R.drawable.lib_ic_building),
    Triple("entity", "Dokumenty organizacji i podmiotów", R.drawable.lib_ic_building),
    Triple("official", "Pozostałe dokumenty urzędowe", R.drawable.lib_ic_document),
    Triple("military_book", "Książeczka wojskowa", R.drawable.tile_ic_armed_service_outline),
    Triple("martial_arts", "Legitymacja sztuk walki", R.drawable.tile_ic_armed_service_outline),
    Triple("shooting_sport", "Legitymacja strzelecka i sportowa", R.drawable.tile_ic_armed_service_outline),
    Triple("archery", "Legitymacja łucznicza", R.drawable.tile_ic_armed_service_outline),
    Triple("tactical_training", "Certyfikaty szkolenia taktycznego", R.drawable.tile_ic_armed_service_outline),
    Triple("survival_training", "Certyfikaty surwiwalu i ratownictwa terenowego", R.drawable.tile_ic_armed_service_outline),
    Triple("security_guard", "Legitymacja i kwalifikacje ochroniarskie", R.drawable.tile_ic_armed_service_outline),
    Triple("defense_training", "Dokumenty szkolenia obronnego", R.drawable.tile_ic_armed_service_outline),
    Triple("historical_reenactment", "Legitymacja członka grupy rekonstrukcji historycznej", R.drawable.tile_ic_armed_service_outline),
    Triple("reenactment_qualification", "Certyfikat kwalifikacji rekonstruktora", R.drawable.tile_ic_armed_service_outline),
    Triple("reenactment_event", "Karta uczestnictwa w wydarzeniu historycznym", R.drawable.tile_ic_armed_service_outline)
) + documentBrandTargets
    .filterNot { (id, _) -> listOf(
        "profile", "identity", "passport", "registry", "family", "parliament", "training", "student",
        "disability", "church", "civil", "business", "bank_accounts", "vaccines", "signed_pdf",
        "imported", "children", "branding", "loyalty", "birth_extract", "marriage_extract",
        "death_extract", "large_family", "blood_donor", "disability_card", "vehicle_registration",
        "insurance_vehicle", "insurance_professional", "insurance_private", "insurance_business",
        "criminal_record", "mission_travel", "ehic", "eu_education", "church_bishop",
        "church_archbishop", "church_cardinal", "church_abbot", "church_minister",
        "church_missionary", "professional", "professional_medical", "professional_education",
        "professional_therapeutic", "professional_other", "organization_foundation",
        "organization_church", "organization_company", "organization_association", "entity", "official"
    ).contains(id) }
    .map { (id, title) ->
        val icon = when {
            id.startsWith("church_") || id == "church" -> R.drawable.tile_ic_church_outline
            id.startsWith("rod_") || id == "family" -> R.drawable.documents_family_record
            id.startsWith("entity_") || id.startsWith("organization_") || id == "entity" || id == "business" -> R.drawable.lib_ic_building
            id.startsWith("professional_") || id == "professional" -> R.drawable.work
            id.startsWith("insurance_") || id == "official" -> R.drawable.lib_ic_document
            else -> R.drawable.lib_ic_document
        }
        Triple(id, title, icon)
    }
 + churchGraphicDocumentTargets().map { (id, title) -> Triple(id, title, R.drawable.tile_ic_church_outline) } +
    familyEntityGraphicDocumentTargets().map { (id, title) ->
        Triple(id, title, if (id.startsWith("rod_")) R.drawable.documents_family_record else R.drawable.lib_ic_building)
    } +
    officialGraphicDocumentTargets().map { (id, title) -> Triple(id, title, R.drawable.lib_ic_document) }
).distinctBy { it.first }
