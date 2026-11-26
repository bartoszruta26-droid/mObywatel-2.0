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

internal fun documentVisibilityGroup(id: String): String = when {
    id == "fund_purpose_disposition" -> "Służba państwowa"
    id == "financial_service" || DonationLedgerCatalog.find(id) != null -> "Służba finansowa"
    id == "payment_account_foundation" -> "Służba fundacyjna"
    id == "payment_account_association" -> "Służba stowarzyszeniowa"
    id == "payment_account_church" -> "Służba kościelna"
    id == "payment_account_business" -> "Służba gospodarcza"
    id == "payment_account_person" -> "Służba osobista"
    id == INTERNATIONAL_DONATION_WORSHIP_ID -> "Służba kościelna"
    id == INTERNATIONAL_DONATION_NGO_ID -> "Służba społeczna"
    id == INTERNATIONAL_DONATION_PERSON_ID -> "Służba cywilna"
    DonationLedgerCatalog.find(id) != null -> DonationLedgerCatalog.find(id)!!.group
    id == "icvp" -> "Służba zdrowotna"
    id == "medical_summary" -> "Służba zdrowotna"
    id == "allergy_card" -> "Służba zdrowotna"
    id == "medication_card" -> "Służba zdrowotna"
    id == "fit_to_travel" -> "Służba zdrowotna"
    id == "fit_to_fly" -> "Służba zdrowotna"
    id == "rehabilitation_report" -> "Służba zdrowotna"
    id == "functional_report" -> "Służba zdrowotna"
    id == "disability_opinion" -> "Służba zdrowotna"
    id == "eu_parking_card" -> "Służba zdrowotna"
    id == "prc" -> "Służba zdrowotna"
    id == "s1" -> "Służba zdrowotna"
    id == "s2" -> "Służba zdrowotna"
    id == "s3" -> "Służba zdrowotna"
    id == "da1" -> "Służba zdrowotna"
    id == "cross_border_prescription" -> "Służba zdrowotna"
    id == "refund_claim" -> "Służba zdrowotna"
    id == "a1" -> "Służba finansowa"
    id == "u1" -> "Służba finansowa"
    id == "u2" -> "Służba finansowa"
    id == "epc" -> "Służba zawodowa"
    id == "good_standing" -> "Służba zawodowa"
    id == "professional_status" -> "Służba zawodowa"
    id == "qualification_recognition" -> "Służba zawodowa"
    id == "diploma" -> "Służba zawodowa"
    id == "education_certificate" -> "Służba zawodowa"
    id == "diploma_supplement" -> "Służba zawodowa"
    id == "birth_certificate" -> "Służba cywilna"
    id == "marriage_certificate" -> "Służba cywilna"
    id == "death_certificate" -> "Służba cywilna"
    id == "marital_status" -> "Służba cywilna"
    id == "capacity_to_marry" -> "Służba cywilna"
    id == "residence_certificate" -> "Służba cywilna"
    id == "citizenship_certificate" -> "Służba cywilna"
    id == "life_certificate" -> "Służba cywilna"
    id == "criminal_record_foreign" -> "Służba cywilna"
    id == "divorce_decree" -> "Służba cywilna"
    id == "adoption_certificate" -> "Służba cywilna"
    id == "parentage_certificate" -> "Służba cywilna"
    id == "international_driving_permit" -> "Służba osobista"
    id == "travel_document" -> "Służba osobista"
    id == "residence_card" -> "Służba osobista"
    id == "passport_record" -> "Służba osobista"
    id == "vatican_passport_ordinary" -> "Służba osobista"
    id == "holy_see_passport_diplomatic" -> "Służba osobista"
    id == "holy_see_passport_service" -> "Służba osobista"
    id == "holy_see_passport_temporary_service" -> "Służba osobista"
    id == "vatican_citizenship_certificate" -> "Służba cywilna"
    id == "ecclesiastical_baptism_certificate" -> "Służba kościelna"
    id == "ecclesiastical_marriage_certificate" -> "Służba kościelna"
    id == "ecclesiastical_death_certificate" -> "Służba kościelna"
    id == "canonical_status_certificate" -> "Służba kościelna"
    id == "clerical_ordination_certificate" -> "Służba kościelna"
    id == "apostolic_nunciature_credential" -> "Służba kościelna"
    id in armedServiceDocumentIds -> "Służba zbrojna"
    id in setOf("profile", "identity", "passport") -> "Służba osobista"
    id == "church" || id.startsWith("church_") || id == "organization_church" ||
        id == "mission_travel" || churchGraphicDocumentTargets().any { it.first == id } -> "Służba kościelna"
    id == "family" || (id.startsWith("rod_") && id != "rod_bank_accounts") || id == "large_family" -> "Służba rodowa"
    id in setOf("financial_service", "bank_accounts", "rod_bank_accounts", "entity_bank_accounts", "payment_transactions") ||
        id.startsWith("payment_account_") || id.startsWith("insurance_") || id.startsWith("donation_ledger_") ||
        id in setOf(INTERNATIONAL_DONATION_WORSHIP_ID, INTERNATIONAL_DONATION_NGO_ID, INTERNATIONAL_DONATION_PERSON_ID) -> "Służba finansowa"
    id.startsWith("service_foundation_") -> "Służba fundacyjna"
    id.startsWith("service_business_") -> "Służba gospodarcza"
    id.startsWith("service_association_") -> "Służba stowarzyszeniowa"
    id in setOf("org_president", "org_general_director", "org_regional_director", "org_county_director", "org_local_representative", "org_general_secretary", "org_treasurer", "org_ambassador", "org_foreign_representative", "org_proxy", "org_delegate") -> "Służba gospodarcza"
    id in setOf("org_members_rights_ombudsman", "org_patient_ombudsman", "org_family_ombudsman", "civil_mediator", "civil_arbitrator", "civil_social_caretaker", "civil_org_curator") -> "Służba cywilna"
    id in setOf("civil_expert", "civil_appraiser", "civil_auditor", "civil_internal_inspector", "civil_controller", "civil_consultant") -> "Służba zawodowa"
    id == "civil_rescuer" -> "Służba zdrowotna"
    id == "parliament" || id in setOf("mayor", "village_head", "county_starosta", "citizen_record", "ombudsman", "children_ombudsman", "patient_ombudsman", "pretrial_mediator", "milicja_officer", "foreign_service_member", "diplomat", "consul", "ambassador", "foreign_mission_head", "consular_employee", "inspector") -> "Służba państwowa"
    id == "organization_foundation" -> "Służba fundacyjna"
    id == "organization_association" -> "Służba stowarzyszeniowa"
    id == "organization_company" || id == "business" || id == "entity" || id == "registry" ||
        id.startsWith("entity_") || id.startsWith("organization_") -> "Służba gospodarcza"
    id == "professional" || id.startsWith("professional_") ||
        id in setOf("training", "student", "eu_education") -> "Służba zawodowa"
    id in setOf("disability", "disability_card", "blood_donor", "ehic", "vaccines", "children", "international_medical_certificate", "foreign_prescription") -> "Służba zdrowotna"
    id == "civil" || id in setOf(
        "birth_extract", "marriage_extract", "death_extract", "criminal_record",
        "vehicle_registration", "official"
    ) || officialGraphicDocumentTargets().any { it.first == id } -> "Służba cywilna"
    id in setOf("imported", "signed_pdf", "receipts", "branding", "loyalty", "loyalty_cards") ||
        documentBrandTargets.any { it.first == id } -> "Zasoby i dokumenty własne"
    else -> "Służba osobista"
}
internal val documentVisibilityGroupOrder = listOf(
    "Służba osobista", "Służba rodowa", "Służba cywilna", "Służba państwowa",
    "Służba kościelna", "Służba zbrojna", "Służba zawodowa",
    "Służba fundacyjna", "Służba gospodarcza", "Służba stowarzyszeniowa", "Służba społeczna",
    "Służba finansowa", "Służba zdrowotna", "Zasoby i dokumenty własne"
)
internal fun documentGroupIconResource(group: String): Int = when (group) {
    "Służba osobista" -> R.drawable.lib_ic_account
    "Służba rodowa" -> R.drawable.lib_ic_group
    "Służba cywilna", "Służba państwowa" -> R.drawable.tile_ic_civil_outline
    "Służba kościelna" -> R.drawable.tile_ic_church_outline
    "Służba zbrojna" -> R.drawable.tile_ic_armed_service_outline
    "Służba zawodowa" -> R.drawable.tile_ic_student_outline
    "Służba fundacyjna", "Służba gospodarcza", "Służba stowarzyszeniowa", "Służba społeczna" -> R.drawable.tile_ic_business_outline
    "Służba finansowa" -> R.drawable.lib_ic_wallet
    "Służba zdrowotna" -> R.drawable.tile_ic_disability_outline
    "Zasoby i dokumenty własne" -> R.drawable.lib_ic_folder
    else -> R.drawable.lib_ic_document
}
internal val armedServiceDocumentIds = setOf(
    "military_book", "martial_arts", "shooting_sport", "archery",
    "tactical_training", "survival_training", "security_guard", "defense_training",
    "historical_reenactment", "reenactment_qualification", "reenactment_event"
)
internal val defaultDocumentPlacements = documentDisplayOptions.associate { (id, _, _) ->
    id to when {
        id in setOf("identity", "receipts", "foreign_prescription", "payment_transactions") -> "dashboard"
        id == "financial_service" -> "other"
        // Donation ledger cards are available in the catalog but never shown by default.
        id in setOf(DonationLedgerCatalog.PERSON, DonationLedgerCatalog.FOUNDATION,
            DonationLedgerCatalog.ASSOCIATION, DonationLedgerCatalog.CHURCH) -> "hidden"
        else -> "hidden"
    }
}
private const val DOCUMENT_VISIBILITY_MIGRATION_KEY = "all_hidden_except_identity_v1"
private const val DONATION_LEDGER_VISIBILITY_MIGRATION_KEY = "donation_ledgers_hidden_by_default_v1"
private const val ARMED_SERVICE_VISIBILITY_MIGRATION_KEY = "armed_service_group_visible_v1"
private const val ARMED_SERVICE_DASHBOARD_CLEANUP_MIGRATION_KEY = "armed_service_dashboard_cleanup_v1"
@Composable
internal fun HomeScreen(onAppearanceChange: (ApplicationAppearance) -> Unit) {
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<Map<String, String>?>(null) }
    var importStatus by remember { mutableStateOf("") }
    var exportSnapshot by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var importBusy by remember { mutableStateOf(false) }
    var exportBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val exportVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-vcard")) { uri ->
        if (uri != null) {
            pendingExportUri = uri
            importStatus = "Wybrano miejsce zapisu. Potwierdź zapis archiwum; istniejący plik o tej nazwie może zostać zastąpiony."
        } else {
            importStatus = "Eksport anulowany — plik nie został zapisany."
        }
    }
    val importVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                importBusy = true
                importStatus = "Odczytywanie i sprawdzanie pliku vCard…"
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            var total = 0
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                require(total <= 256 * 1024 * 1024) { "Plik vCard przekracza limit 256 MB." }
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        } ?: error("Nie można otworzyć wybranego pliku.")
                        require(bytes.isNotEmpty()) { "Wybrany plik jest pusty." }
                        val raw = runCatching {
                            Charsets.UTF_8.newDecoder()
                                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                                .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                        }.getOrElse { String(bytes, java.nio.charset.Charset.forName("windows-1250")) }
                        VCardCodec.decodeForImport(raw) { percent ->
                            scope.launch(Dispatchers.Main) { importStatus = "Odczytywanie vCard: $percent%" }
                        }.also {
                            require(it.isNotEmpty()) { "Archiwum nie zawiera danych." }
                        }
                    }
                }
                result.onSuccess {
                    pendingImport = it
                    importStatus = "Plik odczytany i poprawny. Sprawdź zakres danych przed zatwierdzeniem."
                }.onFailure {
                    pendingImport = null
                    importStatus = "Błąd importu: ${it.message ?: "nieprawidłowy plik"}"
                }
                importBusy = false
            }
        } else {
            importStatus = "Import anulowany — dane lokalne pozostają bez zmian."
        }
    }
    var documentPlacements by remember(context) { mutableStateOf(loadDocumentPlacements(context)) }
    var documentCardStyle by remember(context) { mutableStateOf(loadDocumentCardStyle(context)) }
    var defaultDocumentIcon by remember(context) { mutableStateOf(loadDefaultDocumentIcon(context)) }
    var documentIconOverrides by remember(context) { mutableStateOf(loadDocumentIconOverrides(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeService by remember { mutableStateOf<String?>(null) }
    var showDemoDocuments by remember { mutableStateOf(false) }
    var showDocumentsEditor by remember { mutableStateOf(false) }
    var selectedEditorCategory by remember { mutableStateOf("all") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showTorClientSettings by remember { mutableStateOf(false) }
    var showStudiaOnlineBulkDownload by remember { mutableStateOf(false) }
    var showDocumentEditorCatalog by remember { mutableStateOf(false) }
    var showApplicationAppearanceSettings by remember { mutableStateOf(false) }
    var showTranslationLanguageSettings by remember { mutableStateOf(false) }
    var showDocumentVisibilitySettings by remember { mutableStateOf(false) }
    var showDocumentBrandAssets by remember { mutableStateOf(false) }
    var documentBrandTarget by remember { mutableStateOf<String?>(null) }
    var showRegistryEditor by remember { mutableStateOf(false) }
    var settingsDocumentEditor by remember { mutableStateOf<String?>(null) }
    var graphicEditorReturnTarget by remember { mutableStateOf("graphic_catalog") }
    var credentialAppearanceTarget by remember { mutableStateOf<String?>(null) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var selectedMedicalCertificateRecordId by remember { mutableStateOf<String?>(null) }
    var showDisabilitySettings by remember { mutableStateOf(false) }
    var showCertificates by remember { mutableStateOf(false) }
    var showIdentityDesigner by remember { mutableStateOf(false) }
    var showCredentialAppearanceEditor by remember { mutableStateOf(false) }
    var showImportedDocuments by remember { mutableStateOf(false) }
    var showReceipts by remember { mutableStateOf(false) }
    var showPaymentLedger by remember { mutableStateOf(false) }
    var activeDonationLedgerId by remember { mutableStateOf<String?>(null) }
    var showBankAccounts by remember { mutableStateOf(false) }
    var showPublicServicesBrowser by remember { mutableStateOf(false) }
    var serviceSearch by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("Wszystkie") }
    val favoriteServices = remember { mutableStateMapOf("Zastrzeż PESEL" to true, "Zdrowie" to true, "Transport" to true) }

    // Wszystkie wejścia do dodawania i zarządzania dokumentami kierują do jednego katalogu.
    fun openDocumentVisibilityCatalog() {
        showDemoDocuments = false
        showSettingsMenu = false
        showDocumentVisibilitySettings = true
    }

    fun openSettingsDocumentEditor(documentId: String) {
        showDocumentEditorCatalog = false
        showSettingsMenu = false
        showDemoDocuments = false
        selectedDocumentId = null
        activeService = null
        when {
            documentId == "profile" -> { selectedEditorCategory = "profile"; showDocumentsEditor = true }
            documentId == "identity" -> { selectedEditorCategory = "identity_card"; showDocumentsEditor = true }
            documentId == "passport" -> { selectedEditorCategory = "passport"; showDocumentsEditor = true }
            documentId == "parliament" -> { selectedEditorCategory = "parliament"; showDocumentsEditor = true }
            documentId == "training" -> { selectedEditorCategory = "training"; showDocumentsEditor = true }
            documentId == "student" -> { selectedEditorCategory = "student"; showDocumentsEditor = true }
            documentId == "loyalty" || documentId == "loyalty_cards" -> { selectedEditorCategory = "loyalty_cards"; showDocumentsEditor = true }
            documentId == "disability" -> showDisabilitySettings = true
            documentId == "registry" -> showRegistryEditor = true
            documentId == "bank_accounts" -> settingsDocumentEditor = "bank_accounts:rod_bank_accounts"
            documentId == "rod_bank_accounts" -> settingsDocumentEditor = "bank_accounts:rod_bank_accounts"
            documentId == "entity_bank_accounts" -> settingsDocumentEditor = "entity_bank_accounts:entity_bank_accounts"
            documentId == "family" -> settingsDocumentEditor = "family"
            documentId.startsWith("rod_") -> settingsDocumentEditor = "family:$documentId"
            documentId == "church" -> settingsDocumentEditor = "church"
            documentId == "mission_travel" -> { selectedEditorCategory = "catalog:mission_travel"; showDocumentsEditor = true }
            documentId.startsWith("church_") || churchGraphicDocumentTargets().any { it.first == documentId } -> {
                if (churchGraphicDocumentTargets().any { it.first == documentId }) {
                    settingsDocumentEditor = "church:$documentId"
                } else {
                    // Category aliases such as church_bishop have their own
                    // schema-backed form; they must not fall back to the group list.
                    selectedEditorCategory = "catalog:$documentId"
                    showDocumentsEditor = true
                }
            }
            documentId == "civil" -> settingsDocumentEditor = "civil"
            documentId in setOf("birth_extract", "marriage_extract", "death_extract", "vehicle_registration", "official") ||
                officialGraphicDocumentTargets().any { it.first == documentId } -> settingsDocumentEditor = "civil:$documentId"
            documentId == "business" || documentId == "entity" -> settingsDocumentEditor = "business"
            documentId.startsWith("organization_") -> { selectedEditorCategory = "catalog:$documentId"; showDocumentsEditor = true }
            documentId.startsWith("service_") -> settingsDocumentEditor = "service:$documentId"
                        documentId.startsWith("entity_") -> settingsDocumentEditor = "business:$documentId"
            DonationLedgerCatalog.find(documentId) != null -> settingsDocumentEditor = "donation_ledger:$documentId"
            documentId in InternationalDonationDocumentCatalog.all.keys -> selectedDocumentId = documentId
            documentId == "vaccines" -> settingsDocumentEditor = "vaccines"
            documentId == "children" -> settingsDocumentEditor = "children_health"
            documentId == "signed_pdf" -> settingsDocumentEditor = "signed_pdf"
            documentId == "receipts" -> showReceipts = true
            documentId == "payment_transactions" -> showPaymentLedger = true
            documentId == "imported" -> settingsDocumentEditor = "imported"
            documentId == "professional" -> settingsDocumentEditor = "professional"
            documentId == "branding" -> settingsDocumentEditor = "graphic_catalog"
            else -> { selectedEditorCategory = "catalog:$documentId"; showDocumentsEditor = true }
        }
    }

    fun handleSystemBack() {
        when {
            selectedMedicalCertificateRecordId != null -> selectedMedicalCertificateRecordId = null
            credentialAppearanceTarget != null -> credentialAppearanceTarget = null
            showCredentialAppearanceEditor -> { showCredentialAppearanceEditor = false; showSettingsMenu = true }
            showTranslationLanguageSettings -> { showTranslationLanguageSettings = false; showSettingsMenu = true }
            showTorClientSettings -> { showTorClientSettings = false; showSettingsMenu = true }
            showStudiaOnlineBulkDownload -> { showStudiaOnlineBulkDownload = false; selectedTab = 4 }
            showApplicationAppearanceSettings -> { showApplicationAppearanceSettings = false; showSettingsMenu = true }
            showIdentityDesigner -> { showIdentityDesigner = false }
            showPublicServicesBrowser -> { showPublicServicesBrowser = false }
            showImportedDocuments -> showImportedDocuments = false
            showReceipts -> showReceipts = false
            activeDonationLedgerId != null -> { activeDonationLedgerId = null; showPaymentLedger = true }
            showPaymentLedger -> showPaymentLedger = false
            showBankAccounts -> showBankAccounts = false
            showDocumentsEditor -> { showDocumentsEditor = false; showSettingsMenu = true }
            showRegistryEditor -> { showRegistryEditor = false; showSettingsMenu = true }
            settingsDocumentEditor != null -> { settingsDocumentEditor = null; showSettingsMenu = true }
            showDocumentVisibilitySettings -> { showDocumentVisibilitySettings = false; showSettingsMenu = true }
            documentBrandTarget != null -> { documentBrandTarget = null; showSettingsMenu = true }
            showDocumentBrandAssets -> { showDocumentBrandAssets = false; showSettingsMenu = true }
            showDocumentEditorCatalog -> { showDocumentEditorCatalog = false; showSettingsMenu = true }
            showSettingsMenu -> showSettingsMenu = false
            selectedDocumentId != null -> selectedDocumentId = null
            showDemoDocuments -> showDemoDocuments = false
            showDisabilitySettings -> { showDisabilitySettings = false; showSettingsMenu = true }
            showCertificates -> showCertificates = false
            activeService != null -> activeService = null
            selectedTab == 1 || selectedTab == 2 || selectedTab == 3 || selectedTab == 4 -> selectedTab = 0
            // Na ekranie głównym pozostawiamy domyślne zachowanie Androida.
        }
    }

    BackHandler(
        enabled = credentialAppearanceTarget != null || showCredentialAppearanceEditor || showApplicationAppearanceSettings || showTranslationLanguageSettings || showTorClientSettings || showStudiaOnlineBulkDownload || showIdentityDesigner || showPublicServicesBrowser || showImportedDocuments || showReceipts || activeDonationLedgerId != null || showPaymentLedger || showBankAccounts || showDocumentsEditor || showSettingsMenu || showDocumentEditorCatalog || showDocumentVisibilitySettings || showDocumentBrandAssets || documentBrandTarget != null || showRegistryEditor || settingsDocumentEditor != null || selectedDocumentId != null ||
            showDemoDocuments || showDisabilitySettings || showCertificates ||
            activeService != null || selectedTab != 0,
        onBack = ::handleSystemBack
    )

    fun openService(title: String) {
        when (title) {
            "Służba finansowa" -> showPaymentLedger = true
            "Portfel" -> showDemoDocuments = true
            "Ustawienia" -> {
                selectedTab = 4
                showSettingsMenu = false
                showDemoDocuments = false
                selectedDocumentId = null
            }
            "Zaświadczenia" -> showCertificates = true
            else -> activeService = title
        }
    }

    fun openDocumentCard(documentId: String) {
        // The same route is used from both home sections and the wallet.
        // Keep the source ID intact unless the registry declares a legacy alias.
        val route = DocumentRouteRegistry.find(documentId)
        if (route == null) {
            openSettingsDocumentEditor(documentId)
            return
        }
        when (route.type) {
            DocumentRouteType.GROUP -> {
                if (route.destination == "Służba finansowa") showPaymentLedger = true
                else activeService = requireNotNull(route.destination)
            }
            DocumentRouteType.DOCUMENT -> {
                // Kafelek dokumentu służy do podglądu; edycja pozostaje dostępna
                // wyłącznie z ustawień dokumentu.
                showDocumentEditorCatalog = false
                showSettingsMenu = false
                showDemoDocuments = false
                showDocumentsEditor = false
                showRegistryEditor = false
                showDisabilitySettings = false
                showBankAccounts = false
                settingsDocumentEditor = null
                activeService = null
                selectedDocumentId = route.documentId
            }
            DocumentRouteType.SPECIALIZED -> when (route.destination) {
                "registry_extract" -> openSettingsDocumentEditor("registry")
                "children" -> openSettingsDocumentEditor("children")
                "bank_accounts" -> showBankAccounts = true
                "vaccines" -> openSettingsDocumentEditor("vaccines")
                "foreign_prescription" -> selectedDocumentId = route.documentId
                "signed_pdf" -> openSettingsDocumentEditor("signed_pdf")
                "receipts" -> showReceipts = true
                "imported" -> openSettingsDocumentEditor("imported")
                "branding" -> activeService = "Zasoby identyfikacji dokumentów"
                else -> openSettingsDocumentEditor(route.documentId)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    credentialAppearanceTarget != null -> CredentialAppearanceSettingsScreen(
                        documentKey = credentialAppearanceTarget!!,
                        onBack = { credentialAppearanceTarget = null },
                        onOpenSharedAppearance = {
                            credentialAppearanceTarget = null
                            showCredentialAppearanceEditor = true
                        }
                    )
                    showCredentialAppearanceEditor -> CredentialAppearanceDefaultsScreen(onBack = {
                        showCredentialAppearanceEditor = false
                        if (!showDocumentsEditor && settingsDocumentEditor == null && !showRegistryEditor && !showDisabilitySettings) {
                            showSettingsMenu = true
                        }
                    })
                    showTranslationLanguageSettings -> MedicalTranslationLanguagesSettingsScreen(onBack = { showTranslationLanguageSettings = false; showSettingsMenu = true })
                    showTorClientSettings -> TorClientSettingsScreen(onBack = { showTorClientSettings = false; showSettingsMenu = true })
                    showStudiaOnlineBulkDownload -> StudiaOnlineBulkDownloadScreen(
                        onBack = { showStudiaOnlineBulkDownload = false; selectedTab = 4 }
                    )
                    showApplicationAppearanceSettings -> ApplicationAppearanceSettingsScreen(
                        appearance = LocalApplicationAppearance.current,
                        onChange = onAppearanceChange,
                        documentCardStyle = documentCardStyle,
                        onDocumentCardStyleChange = { style ->
                            saveDocumentCardStyle(context, style)
                            documentCardStyle = style
                        },
                        onBack = { showApplicationAppearanceSettings = false; showSettingsMenu = true }
                    )
                    showIdentityDesigner -> IdentityDocumentsScreen(onBack = { showIdentityDesigner = false })
                    showPublicServicesBrowser -> PublicServicesScreen(onBack = { showPublicServicesBrowser = false })
                    showImportedDocuments -> DocumentsScreen(onBack = { showImportedDocuments = false }, readOnly = true)
                    showReceipts -> ReceiptDocumentsScreen(onBack = { showReceipts = false })
                    activeDonationLedgerId != null -> DonationLedgerScreen(
                        documentId = activeDonationLedgerId!!,
                        readOnly = false,
                        onBack = { activeDonationLedgerId = null; showPaymentLedger = true }
                    )
                    showPaymentLedger -> PaymentLedgerScreen(
                        onBack = { showPaymentLedger = false },
                        onOpenDonationLedger = { id ->
                            activeDonationLedgerId = id
                            showPaymentLedger = false
                        }
                    )
                    settingsDocumentEditor == "receipts" -> ReceiptTemplateSettingsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor?.startsWith("donation_ledger:") == true -> DonationLedgerScreen(
                        documentId = settingsDocumentEditor!!.substringAfter("donation_ledger:"), readOnly = false,
                        onBack = { settingsDocumentEditor = null; showSettingsMenu = true }
                    )
                    showBankAccounts -> FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, readOnly = true, onBack = { showBankAccounts = false })
                    showDocumentsEditor -> DemoDocumentsScreen(
                        categoryId = selectedEditorCategory,
                        onBack = { showDocumentsEditor = false; showSettingsMenu = true },
                        onOpenDocumentAppearance = { key -> credentialAppearanceTarget = key }
                    )
                    showRegistryEditor -> CredentialAppearanceEditorEntry("entity", onOpenAppearance = { credentialAppearanceTarget = it }, showOriginalAttachment = true) {
                        EntityRegistryExtractScreen(onBack = { showRegistryEditor = false; showSettingsMenu = true }, editable = true)
                    }
                    settingsDocumentEditor == "imported" -> CredentialAppearanceEditorEntry("imported", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        DocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    settingsDocumentEditor == "signed_pdf" -> SignedPdfDocumentsScreen(onBack = { settingsDocumentEditor = null; showCertificates = true }, allowImport = true, readOnly = false)
                    settingsDocumentEditor == "professional" -> CredentialAppearanceEditorEntry("professional", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        ProfessionalQualificationsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    (settingsDocumentEditor == "family" || settingsDocumentEditor?.startsWith("family:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("family:", "")?.ifBlank { "family" } ?: "family", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = false, initialDocumentId = settingsDocumentEditor?.substringAfter("family:", "")?.ifBlank { null },
                            onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    (settingsDocumentEditor == "business" || settingsDocumentEditor?.startsWith("business:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("business:", "")?.ifBlank { "business" } ?: "business", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = true, initialDocumentId = settingsDocumentEditor?.substringAfter("business:", "")?.ifBlank { null },
                            onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor?.startsWith("service:") == true -> {
                        val serviceDocumentId = settingsDocumentEditor!!.substringAfter("service:")
                        val serviceKey = serviceDocumentId.removePrefix("service_").substringBefore("_")
                        CredentialAppearanceEditorEntry(serviceDocumentId, onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                            FamilyEntityDocumentsScreen(entityMode = true, serviceKey = serviceKey, initialDocumentId = serviceDocumentId,
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    }
                    (settingsDocumentEditor == "bank_accounts" || settingsDocumentEditor?.startsWith("bank_accounts:") == true) ->
                        CredentialAppearanceEditorEntry(
                            settingsDocumentEditor?.substringAfter("bank_accounts:", "")?.ifBlank { "rod_bank_accounts" } ?: "rod_bank_accounts",
                            onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true
                        ) {
                            FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, initialDocumentId = settingsDocumentEditor?.substringAfter("bank_accounts:", "")?.ifBlank { null },
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    settingsDocumentEditor?.startsWith("entity_bank_accounts:") == true ->
                        CredentialAppearanceEditorEntry(
                            settingsDocumentEditor?.substringAfter("entity_bank_accounts:", "")?.ifBlank { "entity_bank_accounts" } ?: "entity_bank_accounts",
                            onOpenAppearance = { credentialAppearanceTarget = it },
                            showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true
                        ) {
                            FamilyEntityDocumentsScreen(entityMode = true, bankAccountsOnly = true, initialDocumentId = settingsDocumentEditor?.substringAfter("entity_bank_accounts:", "")?.ifBlank { null },
                                onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                                onEditGraphic = { key -> credentialAppearanceTarget = key })
                        }
                    (settingsDocumentEditor == "church" || settingsDocumentEditor?.startsWith("church:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("church:", "")?.ifBlank { "church" } ?: "church", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        ChurchDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            initialDocumentId = settingsDocumentEditor?.substringAfter("church:", "")?.ifBlank { null },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    (settingsDocumentEditor == "civil" || settingsDocumentEditor?.startsWith("civil:") == true) -> CredentialAppearanceEditorEntry(settingsDocumentEditor?.substringAfter("civil:", "")?.ifBlank { "civil" } ?: "civil", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        OfficialDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            initialDocumentId = settingsDocumentEditor?.substringAfter("civil:", "")?.ifBlank { null },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "children_health" -> CredentialAppearanceEditorEntry("children", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        FamilyHealthRecordsScreen(initialSection = "children", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    }
                    settingsDocumentEditor == "vaccines" -> CredentialAppearanceEditorEntry("vaccines", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    }
                    settingsDocumentEditor == "graphic_catalog" -> DocumentGraphicCatalogScreen(
                        onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                        onSelect = { key -> graphicEditorReturnTarget = "graphic_catalog"; settingsDocumentEditor = "graphic:$key" }
                    )
                    settingsDocumentEditor?.startsWith("graphic:") == true -> DocumentGraphicEditorScreen(
                        documentKey = settingsDocumentEditor!!.substringAfter("graphic:"),
                        onBack = {
                            if (graphicEditorReturnTarget == "disability") {
                                settingsDocumentEditor = null
                                showDisabilitySettings = true
                            } else settingsDocumentEditor = graphicEditorReturnTarget
                        }
                    )
                    documentBrandTarget != null -> DocumentBrandAssetsScreen(onBack = { documentBrandTarget = null; showSettingsMenu = true }, fixedTarget = documentBrandTarget!!)
                    showDocumentBrandAssets -> DocumentBrandAssetsScreen(onBack = { showDocumentBrandAssets = false; showSettingsMenu = true })
                    showDocumentVisibilitySettings -> DocumentVisibilitySettingsScreen(
                        documentPlacements = documentPlacements,
                        onDocumentPlacementsChange = { updated ->
                            saveDocumentPlacements(context, updated)
                            documentPlacements = updated
                        },
                        documentCardStyle = documentCardStyle,
                        onDocumentCardStyleChange = { style ->
                            saveDocumentCardStyle(context, style)
                            documentCardStyle = style
                        },
                        defaultDocumentIcon = defaultDocumentIcon,
                        onDefaultDocumentIconChange = { selected ->
                            saveDefaultDocumentIcon(context, selected)
                            defaultDocumentIcon = selected
                        },
                        documentIconOverrides = documentIconOverrides,
                        onDocumentIconOverridesChange = { updated ->
                            saveDocumentIconOverrides(context, updated)
                            documentIconOverrides = updated
                        },
                        onBack = { showDocumentVisibilitySettings = false; showSettingsMenu = true }
                    )
                    showDocumentEditorCatalog -> DocumentEditorCatalogScreen(
                        onOpenDocument = ::openSettingsDocumentEditor,
                        onBack = { showDocumentEditorCatalog = false; showSettingsMenu = true }
                    )
                    showSettingsMenu -> SettingsMenuScreen(
                        documentPlacements = documentPlacements,
                        onDocumentPlacementsChange = { updated ->
                            saveDocumentPlacements(context, updated)
                            documentPlacements = updated
                        },
                        onBack = { showSettingsMenu = false },
                        onManageApplicationAppearance = { showSettingsMenu = false; showApplicationAppearanceSettings = true },
                        onManageTranslationLanguages = { showSettingsMenu = false; showTranslationLanguageSettings = true },
                        onManageTorClients = { showSettingsMenu = false; showTorClientSettings = true },
                        onManageCredentialAppearance = { showSettingsMenu = false; showCredentialAppearanceEditor = true },
                        onManageDocumentEditing = { showSettingsMenu = false; showDocumentEditorCatalog = true },
                        onManageDocumentVisibility = { openDocumentVisibilityCatalog() },
                        onManageDocumentBrandAssets = { showSettingsMenu = false; showDocumentBrandAssets = true },
                        onEditDocumentBranding = { target -> showSettingsMenu = false; documentBrandTarget = target },
                        onEditDocuments = { category -> selectedEditorCategory = category; showSettingsMenu = false; showDocumentsEditor = true },
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true },
                        onEditRegistry = { showSettingsMenu = false; showRegistryEditor = true },
                        onEditReceipts = { showSettingsMenu = false; settingsDocumentEditor = "receipts" },
                        onEditOtherDocuments = { key -> settingsDocumentEditor = key; showSettingsMenu = false },
                        onExportAll = {
                            scope.launch {
                                exportBusy = true
                                importStatus = "Przygotowywanie archiwum vCard…"
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val groupConfiguration = loadDocumentGroupConfiguration(context)
                                        val catalogManifest = org.json.JSONArray().apply {
                                            documentDisplayOptions.distinctBy { it.first }.forEach { (id, title, _) ->
                                                put(org.json.JSONObject().apply {
                                                    put("id", id)
                                                    put("title", title)
                                                    put("group", groupConfiguration.assignments[id] ?: documentVisibilityGroup(id))
                                                })
                                            }
                                        }.toString()
                                        val placements = loadDocumentPlacements(context)
                                        val allCatalogPlacements = documentDisplayOptions.distinctBy { it.first }.associate { (id, _, _) ->
                                            id to (placements[id] ?: if (id == "identity") "dashboard" else "hidden")
                                        }
                                        val snapshot = DemoIdentityStore.load(context) +
                                            LegitimacyPreviewSettingsStore.exportAll(context) +
                                            allCatalogPlacements.mapKeys { "dp_${it.key}" } +
                                            mapOf(
                                                "documentCatalogManifest" to catalogManifest,
                                                "documentCatalogGroupOrder" to org.json.JSONArray(groupConfiguration.groups).toString(),
                                                "documentGroupConfiguration" to exportDocumentGroupConfiguration(context),
                                                "documentEditorSchemaVersion" to DOCUMENT_EDITOR_SCHEMA_VERSION,
                                                "churchDocumentArchive" to ChurchDocumentStore.exportArchive(context),
                                                "familyEntityDocumentArchive" to FamilyEntityDocumentStore.exportArchive(context),
                                                "officialDocumentArchive" to OfficialDocumentStore.exportArchive(context),
                                                "signedPdfArchive" to exportSignedPdfArchive(context),
                                                "brandAssetArchive" to DocumentBrandStore.exportArchive(context),
                                                "professionalQualificationArchive" to exportProfessionalQualificationArchive(context),
                                                "familyHealthArchive" to exportFamilyHealthArchive(context),
                                                "medicalHistoryArchive" to exportMedicalHistoryArchive(context),
                                                "importedAttachmentArchive" to pl.gov.obywatel.android.data.ImportedDocumentStore.exportArchive(context),
                                                "originalAttachmentArchive" to exportOriginalAttachmentArchive(context),
                                                "identityDocumentsArchive" to pl.gov.obywatel.android.ui.identity.exportIdentityDocumentsArchive(context),
                                                "localRegistryArchive" to pl.gov.obywatel.android.ui.registry.exportLocalRegistryArchive(context),
                                                "activityHistoryArchive" to exportActivityHistoryArchive(context)
                                            )
                                        require(snapshot.isNotEmpty()) {
                                            "Brak danych do eksportu. Najpierw uzupełnij i zapisz dane w ustawieniach."
                                        }
                                        VCardCodec.encodeArchive(snapshot) { percent ->
                                            scope.launch(Dispatchers.Main) { importStatus = "Przygotowywanie archiwum vCard: $percent%" }
                                        }
                                        snapshot
                                    }
                                }
                                result.onSuccess {
                                    exportSnapshot = it
                                    importStatus = "Archiwum przygotowane. Wybierz miejsce zapisu."
                                    exportVCardLauncher.launch("obywatel-archiwum.vcf")
                                }.onFailure {
                                    importStatus = "Nie można przygotować eksportu: ${it.message ?: "nieznany błąd"}"
                                }
                                exportBusy = false
                            }
                        },
                        onImportAll = { importVCardLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) },
                        importStatus = importStatus
                    )
                    selectedDocumentId == FOREIGN_PRESCRIPTION_ID -> ForeignPrescriptionEditorScreen(
                        onBack = { selectedDocumentId = null }
                    )
                    selectedDocumentId?.let { DonationLedgerCatalog.find(it) != null } == true -> DonationLedgerScreen(
                        documentId = selectedDocumentId!!,
                        readOnly = true,
                        onBack = { selectedDocumentId = null },
                        onEdit = {
                            activeDonationLedgerId = selectedDocumentId
                            selectedDocumentId = null
                        }
                    )
                    selectedDocumentId?.let { it in InternationalDonationDocumentCatalog.all.keys } == true ->
                        InternationalDonationDocumentEditorScreen(
                            documentId = selectedDocumentId!!,
                            onBack = { selectedDocumentId = null }
                        )
                    selectedDocumentId == INTERNATIONAL_MEDICAL_CERTIFICATE_ID && selectedMedicalCertificateRecordId != null ->
                        InternationalMedicalCertificateEditorScreen(
                            recordId = selectedMedicalCertificateRecordId!!,
                            onBack = { selectedMedicalCertificateRecordId = null }
                        )
                    selectedDocumentId == INTERNATIONAL_MEDICAL_CERTIFICATE_ID ->
                        InternationalMedicalCertificateManagerScreen(
                            onBack = { selectedDocumentId = null },
                            onEdit = { selectedMedicalCertificateRecordId = it }
                        )
                    selectedDocumentId != null -> DocumentDetailsScreen(
                        documentId = selectedDocumentId!!,
                        onBack = { selectedDocumentId = null }
                    )
                    showDemoDocuments -> DocumentsWalletScreen(
                        onBack = { showDemoDocuments = false },
                        onAddDocuments = { openDocumentVisibilityCatalog() },
                        documentPlacements = documentPlacements,
                        documentCardStyle = documentCardStyle,
                        defaultDocumentIcon = defaultDocumentIcon,
                        documentIconOverrides = documentIconOverrides,
                        onOpenDetails = { openDocumentCard(it) },
                        onOpenService = { title ->
                            when (title) { "Konta bankowe" -> showBankAccounts = true; "Dokumenty własne — pliki" -> showImportedDocuments = true; else -> activeService = title }
                        }
                    )
                    showDisabilitySettings -> CredentialAppearanceEditorEntry("disability", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        CompositionLocalProvider(LocalDocumentScreenMode provides DocumentScreenMode.EDITOR) {
                            DisabilitySettingsScreen(
                                onBack = { showDisabilitySettings = false; showSettingsMenu = true },
                                onEditGraphic = {
                                    graphicEditorReturnTarget = "disability"
                                    showDisabilitySettings = false
                                    settingsDocumentEditor = "graphic:disability"
                                }
                            )
                        }
                    }
                    showCertificates -> CertificateStudioScreen(onBack = { showCertificates = false }, onOpenDigitalCertificates = { settingsDocumentEditor = "signed_pdf" })
                    activeService == "Informacje prawne" -> LegalInformationScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Odpis z rejestru podmiotów" -> EntityRegistryExtractScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Dokumenty Kościelne" -> ChurchDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Dokumenty kwalifikacji zawodowych" -> ProfessionalQualificationsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Pozostałe dokumenty" -> ServiceDetailScreen(title = activeService!!, onBack = { activeService = null }, onOpenDocuments = { activeService = null; showDemoDocuments = true })
                    activeService == "Służba fundacyjna" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "foundation", readOnly = true, onBack = { activeService = null })
                    activeService == "Służba gospodarcza" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "business", readOnly = true, onBack = { activeService = null })
                    activeService == "Służba stowarzyszeniowa" -> FamilyEntityDocumentsScreen(entityMode = true, serviceKey = "association", readOnly = true, onBack = { activeService = null })
                    activeService == "Dokumenty rejestru podmiotów" -> FamilyEntityDocumentsScreen(entityMode = true, readOnly = true, onBack = { activeService = null })
                    activeService == "Formularze dokumentów" -> OfficialDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Zasoby identyfikacji dokumentów" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { InstitutionalSymbolCatalogPanel(context = LocalContext.current, onSaved = { }); DocumentBrandAssetsScreen(onBack = { activeService = null }, readOnly = true) }
                    activeService == "Dokumenty PDF i podpisy elektroniczne" -> SignedPdfDocumentsScreen(onBack = { activeService = null }, allowImport = false, readOnly = true)
                    activeService == "Dane dzieci" -> FamilyHealthRecordsScreen(initialSection = "children", onBack = { activeService = null }, readOnly = true)
                    activeService == "eKarta Szczepień" -> FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { activeService = null }, readOnly = true)
                    activeService == "Dokumenty Rodowe" -> FamilyEntityDocumentsScreen(entityMode = false, readOnly = true, onBack = { activeService = null })
                    activeService != null -> ServiceDetailScreen(
                        title = activeService!!,
                        onBack = { activeService = null },
                        onOpenDocuments = { activeService = null; showDemoDocuments = true }
                    )
                    selectedTab == 1 -> {
                        val query = serviceSearch.trim()
                        val matchingDocuments = if (query.isBlank()) emptyList() else
                            documentDisplayOptions.filter { (id, title, _) ->
                                title.contains(query, ignoreCase = true) ||
                                    id.contains(query, ignoreCase = true)
                            }
                        val matchingServices = if (query.isBlank()) emptyList() else
                            services.filter { item ->
                                item.title.contains(query, ignoreCase = true) ||
                                    item.subtitle.contains(query, ignoreCase = true) ||
                                    item.category.contains(query, ignoreCase = true)
                            }
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Szukaj", color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = serviceSearch,
                                onValueChange = { serviceSearch = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Szukaj dokumentu, usługi lub funkcji") },
                                leadingIcon = { Icon(painterResource(R.drawable.nav_search), contentDescription = null) },
                                trailingIcon = {
                                    if (serviceSearch.isNotEmpty()) {
                                        IconButton(onClick = { serviceSearch = "" }) {
                                            Icon(painterResource(R.drawable.lib_ic_close), contentDescription = "Wyczyść wyszukiwanie")
                                        }
                                    }
                                }
                            )
                            if (query.isBlank()) {
                                Text(
                                    "Wpisz nazwę dokumentu, usługi, modułu lub kategorii. Wyniki obejmują dokumenty i funkcje dostępne w aplikacji.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text("Dokumenty (" + matchingDocuments.size + ")",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (matchingDocuments.isEmpty()) {
                                    Text("Brak pasujących dokumentów.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall)
                                } else matchingDocuments.forEach { (id, title, iconRes) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { openDocumentCard(id) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Image(painter = painterResource(documentTileIconResource(context, id, iconRes)), contentDescription = null,
                                                modifier = Modifier.size(38.dp), contentScale = ContentScale.Fit)
                                            Text(title, modifier = Modifier.weight(1f).padding(start = 12.dp),
                                                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Text("Usługi i funkcje (" + matchingServices.size + ")",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (matchingServices.isEmpty()) {
                                    Text("Brak pasujących usług i funkcji.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall)
                                } else matchingServices.forEach { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { openService(item.title) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            if (item.icon != null) {
                                                Icon(painterResource(item.icon), contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            } else {
                                                Icon(item.fallbackIcon ?: Icons.Filled.Description, contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            }
                                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                                Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                                Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall, maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis)
                                            }
                                            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    selectedTab == 3 -> QrToolsScreen()
                    selectedTab == 4 -> MoreMenuScreen(
                        onOpenSettings = { showSettingsMenu = true },
                        onOpenStudiaOnlineArchive = { showStudiaOnlineBulkDownload = true },
                        onOpenCertificates = { showCertificates = true },
                        onOpenHelp = { activeService = "Pomoc i informacje" },
                        onOpenHistory = { activeService = "Historia działań" },
                        onOpenIdentityDesigner = { showIdentityDesigner = true },
                        onOpenImportedDocuments = { showImportedDocuments = true },
                        onOpenPaymentLedger = { showPaymentLedger = true },
                        onOpenLegalInformation = { activeService = "Informacje prawne" }
                    )
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (selectedTab == 0) {
                                Header(
                                    onOpenAssistant = { activeService = "Asystent" },
                                    onOpenMessages = { activeService = "Wiadomości" },
                                    onOpenNotifications = { activeService = "Powiadomienia" }
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                                    .padding(horizontal = 18.dp)
                            ) {
                                Spacer(Modifier.height(18.dp))
                                if (selectedTab == 0) {
                                    if (documentCardStyle == "small") {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IdentityDashboardCard(
                                                onOpen = { openDocumentCard("identity") },
                                                tileStyle = documentCardStyle,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Spacer(Modifier.weight(1f))
                                        }
                                    } else {
                                        IdentityDashboardCard(
                                            onOpen = { openDocumentCard("identity") },
                                            tileStyle = documentCardStyle
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    val dashboardDocuments = documentDisplayOptions.filter {
                                        it.first != "identity" && documentPlacements[it.first] == "dashboard"
                                    }
                                    if (dashboardDocuments.isNotEmpty()) {
                                        Text("Dokumenty", color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        DashboardDocumentGrid(dashboardDocuments, documentCardStyle, ::openDocumentCard)
                                    }
                                    val otherDashboardDocuments = documentDisplayOptions.filter {
                                        documentPlacements[it.first] == "other"
                                    }
                                    if (otherDashboardDocuments.isNotEmpty()) {
                                        Text("Inne dokumenty", color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        DashboardDocumentGrid(otherDashboardDocuments, documentCardStyle, ::openDocumentCard)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Zarządzaj",
                                        color = Color(0xFF83DFFF),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { showDemoDocuments = true }
                                            .padding(vertical = 4.dp)
                                    )
                                    Button(
                                        onClick = { openDocumentVisibilityCatalog() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF91D9F8),
                                            contentColor = Color(0xFF102D5D)
                                        )
                                    ) {
                                        Icon(painter = painterResource(R.drawable.lib_ic_plus),
                                            contentDescription = null, tint = Color(0xFF102D5D),
                                            modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Dodaj dokument", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    CivicIllustrationBanner(
                                        imageRes = R.drawable.header_poland_banner,
                                        description = "Baner informacyjny Polski"
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    SectionHeading("Usługi", "Katalog usług według kategorii")
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { showPublicServicesBrowser = true },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Public, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                                Text("Przeglądarka usług publicznych",
                                                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                Text("Otwieraj portale gov.pl i inne serwisy wewnątrz aplikacji",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text("Otwórz", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = serviceSearch,
                                        onValueChange = { serviceSearch = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Szukaj usługi") },
                                        leadingIcon = { Icon(painterResource(R.drawable.nav_search), contentDescription = null) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "Wszystkie", "Zdrowie", "Kierowca i pojazdy", "Sprawy urzędowe",
                                            "Rodzina", "Edukacja", "Praca", "Finanse i podatki", "Transport",
                                            "Nieruchomości", "Środowisko", "Bezpieczeństwo", "Prawo i obywatel",
                                            "Kultura i dziedzictwo", "Sport", "Organizacje społeczne",
                                            "Kościół i wspólnoty", "Rejestry", "Dokumenty", "Podróże", "Pozostałe"
                                        ).forEach { category ->
                                            val active = serviceCategory == category
                                            Box(
                                                modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(18.dp))
                                                    .clickable { serviceCategory = category }
                                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                                            ) {
                                                Text(category,
                                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    val visibleServices = services.filter { service ->
                                        (serviceCategory == "Wszystkie" || service.category == serviceCategory) &&
                                            (serviceSearch.isBlank() ||
                                                service.title.contains(serviceSearch, ignoreCase = true) ||
                                                service.subtitle.contains(serviceSearch, ignoreCase = true))
                                    }
                                    if (visibleServices.isEmpty()) {
                                        Text("Nie znaleziono usług. Zmień wyszukiwaną frazę lub kategorię.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
                                    } else {
                                        visibleServices.forEach { service ->
                                            ServiceCard(
                                                item = service,
                                                tileStyle = documentCardStyle,
                                                isFavorite = favoriteServices[service.title] == true,
                                                onFavorite = {
                                                    favoriteServices[service.title] = favoriteServices[service.title] != true
                                                },
                                                onClick = { openService(service.title) }
                                            )
                                            Spacer(Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BottomNavigation(
                selectedTab = selectedTab,
                onSelect = {
                    // Nawigacja dolna zawsze wraca do wybranego ekranu głównego.
                    selectedTab = it
                    activeService = null
                    showDemoDocuments = false
                    showDocumentsEditor = false
                    showSettingsMenu = false
                    showDocumentEditorCatalog = false
                    showApplicationAppearanceSettings = false
                    showDocumentVisibilitySettings = false
                    showDocumentBrandAssets = false
                    documentBrandTarget = null
                    showRegistryEditor = false
                    settingsDocumentEditor = null
                    credentialAppearanceTarget = null
                    selectedDocumentId = null
                    showDisabilitySettings = false
                    showCertificates = false
                    showIdentityDesigner = false
                    showCredentialAppearanceEditor = false
                    showImportedDocuments = false
                    showBankAccounts = false
                    showPublicServicesBrowser = false
                }
            )
        }
        pendingExportUri?.let { destinationUri ->
            AlertDialog(
                onDismissRequest = { pendingExportUri = null },
                title = { Text("Potwierdzenie eksportu vCard") },
                text = {
                    Text("Zapiszesz ${exportSnapshot.size} pól oraz katalog dokumentów z ich grupami i ustawieniami widoczności. Jeżeli w wybranym miejscu istnieje plik o tej samej nazwie, jego zawartość zostanie zastąpiona. Czy kontynuować?")
                },
                confirmButton = {
                    TextButton(enabled = !exportBusy, onClick = {
                        scope.launch {
                            exportBusy = true
                            importStatus = "Zapisywanie i weryfikowanie archiwum vCard…"
                            val snapshot = exportSnapshot.toMap()
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    require(snapshot.isNotEmpty()) { "Brak danych do eksportu." }
                                    val bytes = VCardCodec.encodeArchive(snapshot).toByteArray(Charsets.UTF_8)
                                    val output = context.contentResolver.openOutputStream(destinationUri, "wt")
                                        ?: error("Android nie udostępnił strumienia zapisu dla wybranego pliku.")
                                    output.use { it.write(bytes); it.flush() }
                                    val savedBytes = context.contentResolver.openInputStream(destinationUri)?.use { it.readBytes() }
                                        ?: error("Nie można ponownie odczytać zapisanego pliku vCard.")
                                    require(savedBytes.isNotEmpty()) { "Zapisany plik vCard jest pusty." }
                                    val verified = VCardCodec.decodeArchive(String(savedBytes, Charsets.UTF_8))
                                    require(verified == snapshot) {
                                        "Weryfikacja zapisu nie powiodła się: odczytane dane różnią się od eksportowanych."
                                    }
                                    "Zapisano i zweryfikowano ${snapshot.size} pól oraz katalog dokumentów (${bytes.size} bajtów) w pliku vCard."
                                }
                            }
                            result.onSuccess { importStatus = it }
                                .onFailure { importStatus = "Błąd eksportu: ${it.message ?: "nieznany błąd"}" }
                            exportBusy = false
                            pendingExportUri = null
                        }
                    }) { Text(if (exportBusy) "Zapisywanie…" else "Potwierdź zapis") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingExportUri = null
                        importStatus = "Eksport anulowany — plik docelowy nie został zmieniony."
                    }) { Text("Anuluj") }
                }
            )
        }
        pendingImport?.let { incoming ->
            AlertDialog(
                onDismissRequest = { pendingImport = null },
                title = { Text("Import danych vCard") },
                text = {
                    val current = remember { DemoIdentityStore.load(context) }
                    val supportedProfileKeys = current.keys + documentEditorImportableFieldKeys()
                    val supportedArchiveKeys = documentArchiveImportKeys()
                    val recognizedIncoming = incoming.keys.count { it in supportedProfileKeys || it in supportedArchiveKeys || it.startsWith("cs_") || it.startsWith("dp_") || isSupportedDynamicDocumentField(it) }
                    val overwritten = incoming.keys.count { it in supportedProfileKeys && current.containsKey(it) }
                    val added = recognizedIncoming - overwritten
                    Text(
                        "Plik zawiera ${incoming.size} pól. Nowe: ${added}; zastąpią istniejące: ${overwritten}. " +
                            "Dane zostaną połączone z profilem. Po zapisie aplikacja sprawdzi zgodność zapisanych wartości."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            importBusy = true
                            importStatus = "Zapisywanie i weryfikowanie zaimportowanych danych…"
                            val result = withContext(Dispatchers.IO) {
                                var placementUpdate: Map<String, String>? = null
                                val outcome = runCatching {
                            val current = DemoIdentityStore.loadStrict(context)
                            val importedSchemaVersion = incoming["documentEditorSchemaVersion"]?.toIntOrNull() ?: 1
                            require(importedSchemaVersion in 1..DOCUMENT_EDITOR_SCHEMA_VERSION.toInt()) {
                                "Archiwum używa nieobsługiwanej wersji schematu dokumentów: $importedSchemaVersion."
                            }
                            val profileValues = incoming.filterKeys {
                                // Importuj wyłącznie pola należące do aktualnego modelu profilu.
                                // Nieznane lub wycofane nazwy pól ze starszych archiwów vCard
                                // są pomijane — nie tworzymy przez nie osieroconych wpisów.
                                (it in current.keys || it in documentEditorImportableFieldKeys() || isSupportedDynamicDocumentField(it)) &&
                                    !it.startsWith("cs_") && !it.startsWith("dp_")
                            }
                            val mergedProfile = current.toMutableMap().apply { putAll(profileValues) }
                            PersonalDataPropagation.apply(mergedProfile)
                            DemoIdentityStore.save(context, mergedProfile)
                            incoming["documentGroupConfiguration"]?.let { importDocumentGroupConfiguration(context, it) }
                            incoming["churchDocumentArchive"]?.let { ChurchDocumentStore.importArchive(context, it) }
                            incoming["familyEntityDocumentArchive"]?.let { FamilyEntityDocumentStore.importArchive(context, it) }
                            incoming["officialDocumentArchive"]?.let { OfficialDocumentStore.importArchive(context, it) }
                            incoming["signedPdfArchive"]?.let { importSignedPdfArchive(context, it) }
                            incoming["brandAssetArchive"]?.let { DocumentBrandStore.importArchive(context, it) }
                            incoming["professionalQualificationArchive"]?.let { importProfessionalQualificationArchive(context, it) }
                            incoming["familyHealthArchive"]?.let { importFamilyHealthArchive(context, it) }
                            incoming["medicalHistoryArchive"]?.let { importMedicalHistoryArchive(context, it) }
                            incoming["importedAttachmentArchive"]?.let { pl.gov.obywatel.android.data.ImportedDocumentStore.importArchive(context, it) }
                            incoming["originalAttachmentArchive"]?.let { importOriginalAttachmentArchive(context, it) }
                            incoming["identityDocumentsArchive"]?.let { pl.gov.obywatel.android.ui.identity.importIdentityDocumentsArchive(context, it) }
                            incoming["localRegistryArchive"]?.let { pl.gov.obywatel.android.ui.registry.importLocalRegistryArchive(context, it) }
                            incoming["activityHistoryArchive"]?.let { importActivityHistoryArchive(context, it) }
                            LegitimacyPreviewSettingsStore.importAll(context, incoming)
                            val knownDocumentIds = documentDisplayOptions.map { it.first }.toSet()
                            val importedCatalogIds = incoming["documentCatalogManifest"]?.let { rawManifest ->
                                val manifest = org.json.JSONArray(rawManifest)
                                buildSet {
                                    for (index in 0 until manifest.length()) {
                                        val item = manifest.optJSONObject(index)
                                            ?: error("Nieprawidłowy wpis katalogu dokumentów w archiwum.")
                                        val id = item.optString("id").trim()
                                        val title = item.optString("title").trim()
                                        val group = item.optString("group").trim()
                                        require(
                                            id.isNotBlank() && title.isNotBlank() &&
                                                migrateDocumentGroupName(group) in loadDocumentGroupConfiguration(context).groups
                                        ) {
                                            "Archiwum zawiera nieprawidłowy opis dokumentu: $id."
                                        }
                                        require(add(id)) { "Archiwum zawiera powtórzony identyfikator dokumentu: $id." }
                                    }
                                }
                            } ?: knownDocumentIds
                            val importedPlacements = incoming.filterKeys {
                                it.startsWith("dp_") &&
                                    it.removePrefix("dp_") in knownDocumentIds &&
                                    it.removePrefix("dp_") in importedCatalogIds
                            }.mapKeys { it.key.removePrefix("dp_") }
                            require(importedPlacements.values.all { it in setOf("dashboard", "other", "hidden") }) {
                                "Archiwum zawiera nieprawidłowe ustawienie widoczności dokumentu."
                            }
                            if (importedPlacements.isNotEmpty()) {
                                // Ustawienia z jawnie wskazanego archiwum mają pierwszeństwo
                                // przed wartościami lokalnymi, również dla służby zbrojnej.
                                val mergedPlacements = loadDocumentPlacements(context).toMutableMap().apply {
                                    putAll(importedPlacements)
                                }
                                saveDocumentPlacements(context, mergedPlacements)
                                val verifiedPlacements = loadDocumentPlacements(context)
                                require(importedPlacements.all { (id, placement) -> verifiedPlacements[id] == placement }) {
                                    "Weryfikacja importu nie potwierdziła wszystkich ustawień widoczności dokumentów."
                                }
                                placementUpdate = verifiedPlacements
                            }
                            val persisted = DemoIdentityStore.loadStrict(context)
                            require(profileValues.all { (key, value) -> persisted[key] == value }) {
                                "Weryfikacja zapisu nie powiodła się. Dane profilu nie odpowiadają importowi."
                            }
                            val auxiliaryCount = incoming.keys.count {
                                it.startsWith("cs_") || it.startsWith("dp_") ||
                                    it in setOf(
                                        "documentCatalogManifest", "documentCatalogGroupOrder", "documentGroupConfiguration", "documentEditorSchemaVersion", "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive", "originalAttachmentArchive",
                                        "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
                                    )
                            }
                            val recognizedCount = profileValues.size + auxiliaryCount
                            val changed = profileValues.keys.count { persisted[it] == profileValues[it] } + auxiliaryCount
                            require(changed == recognizedCount) {
                                "Weryfikacja importu wykazała niezgodność części rozpoznanych pól lub ustawień."
                            }
                            val ignoredCount = incoming.size - recognizedCount
                            "Zaimportowano i zweryfikowano ${changed} rozpoznanych pól oraz ustawień. Pominięto ${ignoredCount.coerceAtLeast(0)} pól o nieaktualnych lub nieobsługiwanych nazwach. Łącznie zapisano ${persisted.size} pól profilu."
                                }
                                outcome to placementUpdate
                            }
                            result.first.onSuccess { message ->
                                result.second?.let { documentPlacements = it }
                                importStatus = message
                            }.onFailure { importStatus = "Błąd importu lub weryfikacji: ${it.message ?: "nieznany błąd"}" }
                            pendingImport = null
                            importBusy = false
                        }
                    }) { Text(if (importBusy) "Importowanie…" else "Importuj i zweryfikuj") }
                },
                dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Anuluj") } }
            )
        }
    }
}

@Composable
private fun DisabilitySettingsScreen(onBack: () -> Unit, onEditGraphic: () -> Unit = {}) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val appearance = rememberLegitimacyPreviewSettings("disability")
    var saveMessage by remember { mutableStateOf("") }
    CompositionLocalProvider(
        LocalCredentialFieldVisibility provides CredentialFieldVisibilityBinding("disability", appearance, fields)
    ) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Ustawienia danych o niepełnosprawności", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Uzupełnij dane zgodnie z posiadanym dokumentem lub informacją źródłową.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onEditGraphic, modifier = Modifier.fillMaxWidth()) {
            Text("Edytuj wygląd graficzny i podgląd orzeczenia / zaświadczenia")
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Źródło dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                val options = listOf(
                    "powiat" to "Zespół powiatowy / miejski",
                    "other_law" to "Inny organ — odrębna podstawa prawna",
                    "foundation" to "Zespół medyczny fundacji",
                    "medical_opinion" to "Opinia / zaświadczenie medyczne",
                    "self_declaration" to "Własne oświadczenie"
                )
                options.forEach { (value, label) ->
                    val selected = (fields["disabilityAuthorityType"] ?: "powiat") == value
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                            .clickable { fields["disabilityAuthorityType"] = value }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(label, modifier = Modifier.padding(start = 9.dp), color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                DemoDataField("disabilityAuthorityName", "Nazwa organu / podmiotu / wystawcy", fields)
                DemoDataField("disabilityIssuerName", "Imię, nazwisko lub nazwa wystawcy", fields)
                DemoDataField("disabilityLegalBasis", "Podstawa prawna / cel wydania", fields)
                DemoDataField("disabilityFoundationStatute", "Podstawa statutowa / uchwała fundacji", fields)
                Text("Dane osoby i dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityPersonName", "Osoba, której dotyczy orzeczenie", fields)
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator", fields)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / rozstrzygnięcia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                Text("Ustalony stopień i okres", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityCategory", "Kategoria", fields)
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki")
                DemoDataField("disabilityCauseCodes", "Symbole / przyczyny", fields)
                DemoDataField("disabilityValidity", "Okres, na jaki ustalono status", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data ustalenia niepełnosprawności", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityEmployment", "Zatrudnienie", fields)
                DemoDataField("disabilityTraining", "Szkolenie", fields)
                DemoDataField("disabilityTherapy", "Terapia / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Sprzęt i środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Wsparcie środowiskowe", fields)
                DemoDataField("disabilityParking", "Karta parkingowa — wskazania", fields)
                Text("Metodologia i ocena funkcjonalna (opcjonalnie)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityAssessmentMethod", "Metoda oceny (np. ICF)", fields)
                DemoDataField("disabilityWhodasScore", "WHODAS 2.0 — wynik / data oceny", fields)
                DemoDataField("disabilityMedicalQualification", "Zawód / kwalifikacje osoby wystawiającej", fields)
                DemoDataField("disabilityAppealInfo", "Tryb odwołania / weryfikacji", fields)
                androidx.compose.material3.Button(
                    onClick = {
                        runCatching { DemoIdentityStore.save(context, fields.toMap()) }
                            .onSuccess { saveMessage = "Zapisano i zabezpieczono dane." }
                            .onFailure { saveMessage = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz dane") }
                if (saveMessage.isNotBlank()) Text(saveMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    }
}

@Composable
private fun CertificatesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val hasDisabilityData = listOf("disabilityPersonName", "disabilityDecisionNumber", "disabilityDecisionDate",
        "disabilityDegree", "disabilityAuthorityName").any { !fields[it].isNullOrBlank() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Zaświadczenia", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zapisane dane dokumentów i zaświadczeń", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (hasDisabilityData) {
            Text("Orzeczenie / zaświadczenie o niepełnosprawności", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            DisabilityDecisionPreview(fields)
            listOf(
                "Organ / wystawca" to "disabilityAuthorityName",
                "Numer dokumentu" to "disabilityDecisionNumber",
                "Data wydania" to "disabilityDecisionDate",
                "Stopień" to "disabilityDegree",
                "Okres obowiązywania" to "disabilityValidity",
                "Podstawa prawna / regulacyjna" to "disabilityLegalBasis"
            ).forEach { (label, key) ->
                val value = fields[key].orEmpty()
                if (value.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(190.dp))
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Text("Brak zapisanych danych", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("Dodaj dane o niepełnosprawności w Ustawieniach. Po zapisaniu pojawią się tutaj.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
     }
}

@Composable
private fun DashboardDocumentGrid(
    documents: List<Triple<String, String, Int>>,
    tileStyle: String,
    onOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val columns = when (tileStyle) {
        "small" -> 3
        "list" -> 1
        else -> 2
    }
    val rows = documents.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (id, title, iconRes) ->
                    Card(
                        modifier = Modifier.weight(1f)
                            .height(when (tileStyle) { "list" -> 68.dp; "small" -> 92.dp; else -> 116.dp })
                            .clickable { onOpen(id) }
                            .testTag("dashboard-document-$id"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        if (tileStyle == "list") {
                            Box(Modifier.fillMaxSize()) {
                                DocumentTileArtwork(id, Modifier.fillMaxSize())
                                Row(Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(title, color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp))
                                    Image(
                                        painter = painterResource(documentTileIconResource(context, id, iconRes)),
                                        contentDescription = "Ikona wektorowa: " + title,
                                        modifier = Modifier.size(22.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                    )
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                DocumentTileArtwork(id, Modifier.fillMaxSize())
                                Text(title, color = Color.White,
                                    style = if (tileStyle == "small") MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(0.78f).padding(10.dp))
                                Image(
                                    painter = painterResource(documentTileIconResource(context, id, iconRes)),
                                    contentDescription = "Ikona wektorowa: " + title,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                        .size(if (tileStyle == "small") 20.dp else 24.dp),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

internal fun documentTileIconResource(context: Context, documentKey: String, fallback: Int): Int {
    val selected = context.getSharedPreferences("legitimacy_preview_preferences", Context.MODE_PRIVATE)
        .getString("${documentKey}_tile_icon", "").orEmpty()
    if (selected.isNotBlank()) context.resources.getIdentifier(selected, "drawable", context.packageName)
        .takeIf { it != 0 }?.let { return it }
    return when (documentKey) {
    "training", "student" -> R.drawable.tile_ic_student_outline
    "disability", "disability_card" -> R.drawable.tile_ic_disability_outline
    "church", "church_bishop", "church_archbishop", "church_cardinal", "church_abbot",
    "church_minister", "church_missionary", "organization_church" -> R.drawable.tile_ic_church_outline
    "civil", "birth_extract", "marriage_extract", "death_extract" -> R.drawable.tile_ic_civil_outline
    "business", "entity", "official", "organization_foundation", "organization_company",
    "organization_association" -> R.drawable.tile_ic_business_outline
    in armedServiceDocumentIds -> R.drawable.tile_ic_armed_service_outline
    // Każdy typ dokumentu otrzymuje wektorową ikonę, także gdy nie ma
    // dedykowanego piktogramu dla konkretnej kategorii.
    else -> R.drawable.tile_ic_civil_outline
    }
}

private fun documentTileWatermarkResource(documentKey: String): Int = when {
    // Sakralne i misyjne dokumenty Kościoła oraz wspólnot.
    documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") ->
        R.drawable.tile_watermark_sacred

    // Tożsamość, profil i dokumenty identyfikacyjne.
    documentKey in setOf("identity", "identity_card", "profile", "parliament") ->
        R.drawable.tile_watermark_identity

    // Paszporty, podróże, dokumenty zagraniczne i komunikacyjne.
    documentKey in setOf("passport", "mission_travel") ->
        R.drawable.tile_watermark_travel
    documentKey in setOf("vehicle_registration", "insurance_vehicle") ->
        R.drawable.tile_watermark_transport

    // Finanse, rachunki i ubezpieczenia.
    documentKey in setOf("bank_accounts", "insurance_business", "insurance_private") ||
        (documentKey.startsWith("insurance_") && documentKey != "insurance_professional") ->
        R.drawable.tile_watermark_finance

    // Dokumenty elektroniczne, importowane i karty handlowe.
    documentKey in setOf("signed_pdf", "imported", "branding", "loyalty", "loyalty_cards") ->
        R.drawable.tile_watermark_digital

    // Kwalifikacje, zawody, uprawnienia i dokumenty służbowe.
    documentKey in setOf("student", "training", "eu_education") ->
        R.drawable.tile_watermark_education

    // Zdrowie, opieka, szczepienia i orzeczenia, także kwalifikacje medyczne.
    documentKey in setOf("disability", "vaccines", "ehic", "blood_donor", "disability_card",
        "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") ->
        R.drawable.tile_watermark_medical

    // Kwalifikacje zawodowe i dokumenty uprawnień.
    documentKey.startsWith("professional_") || documentKey in setOf("professional", "insurance_professional") ->
        R.drawable.tile_watermark_professional

    // Rodzina, Ród i akty stanu cywilnego.
    documentKey.startsWith("rod_") || documentKey in setOf(
        "family", "children", "birth_extract", "marriage_extract", "death_extract",
        "large_family"
    ) ->
        R.drawable.tile_watermark_family

    // Rejestry, podmioty prawne, fundacje, stowarzyszenia i dokumenty urzędowe.
    documentKey.startsWith("organization_") || documentKey.startsWith("entity_") ||
        documentKey in setOf(
            "civil", "official", "registry", "business", "entity",
            "organization_foundation", "organization_company",
            "organization_association", "parliament", "criminal_record"
        ) ->
        R.drawable.tile_watermark_civic

    // Każdy pozostały typ ma czytelny, neutralny znak zabezpieczający.
    else -> R.drawable.tile_watermark_security
}

@Composable
internal fun DocumentTileArtwork(documentKey: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tilePreferences = context.getSharedPreferences("legitimacy_preview_preferences", Context.MODE_PRIVATE)
    val tileBackgroundAssetId = tilePreferences.getString("${documentKey}_tile_background", "").orEmpty()
    val tileVectorBackgroundAssetId = tilePreferences.getString("${documentKey}_tile_vector_background", null)
        ?.takeIf { it.isNotBlank() }
        ?: tilePreferences.getString(
            "credential_template_global_tile_vector_background",
            tilePreferences.getString("global_document_tile_vector_background", "")
        ).orEmpty()
    val tileVectorBackgroundBitmap = remember(documentKey, tileVectorBackgroundAssetId) { tileVectorBackgroundAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) } }
    val tileBackgroundBitmap = remember(documentKey, tileBackgroundAssetId) {
        when {
            tileBackgroundAssetId.isBlank() -> null
            tileBackgroundAssetId.startsWith("builtin:") -> {
                val resourceName = tileBackgroundAssetId.removePrefix("builtin:")
                val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                resourceId.takeIf { it != 0 }?.let { BitmapFactory.decodeResource(context.resources, it) }
            }
            else -> DocumentBrandStore.preview(context, tileBackgroundAssetId)
        }
    }
    val defaultPalette = when {
        documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") ->
            listOf(Color(0xFF4A145F), Color(0xFF762B83), Color(0xFFB27BB5))
        documentKey.startsWith("rod_") || documentKey in setOf(
            "family", "children", "birth_extract", "marriage_extract", "death_extract",
            "large_family"
        ) -> listOf(Color(0xFF9B315D), Color(0xFFD45B83), Color(0xFFF0A0B4))
        documentKey in setOf("disability", "disability_card", "vaccines", "ehic", "blood_donor",
            "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") -> listOf(Color(0xFF064B35), Color(0xFF167A50), Color(0xFF71A878))
        documentKey in setOf("student", "training", "eu_education") ||
            documentKey.startsWith("professional_education") -> listOf(Color(0xFF25205F), Color(0xFF4D4696), Color(0xFF9994D0))
        documentKey in setOf("bank_accounts", "insurance_business", "insurance_vehicle", "insurance_professional", "insurance_private") ||
            documentKey.startsWith("insurance_") || documentKey.startsWith("entity_") ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey in setOf("vehicle_registration", "mission_travel", "passport") ->
            listOf(Color(0xFF176B83), Color(0xFF3C9FB0), Color(0xFF9AD5D4))
        documentKey in setOf("signed_pdf", "imported", "branding", "loyalty", "loyalty_cards") ->
            listOf(Color(0xFF171717), Color(0xFF424242), Color(0xFF777777))
        documentKey.startsWith("professional_") || documentKey == "professional" ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey.startsWith("organization_") || documentKey in setOf("registry", "business", "entity") ->
            listOf(Color(0xFF064B35), Color(0xFF167A50), Color(0xFF71A878))
        documentKey in setOf("identity", "identity_card", "profile", "parliament") ->
            listOf(Color(0xFF003F87), Color(0xFF1769AA), Color(0xFF5AA6D1))
        documentKey in setOf("civil", "official", "criminal_record", "registry_extract") ->
            listOf(Color(0xFF790018), Color(0xFFB5122B), Color(0xFFE36A72))
        else -> listOf(Color(0xFF46515B), Color(0xFF89949E), Color(0xFFD5DCE2))
    }
    val selectedPaletteId = tilePreferences.getString("${documentKey}_tile_gradient_palette", "auto").orEmpty()
    val palette = tileGradientPaletteOptions.firstOrNull { it.id == selectedPaletteId && it.id != "auto" }
        ?.colors?.map { Color(android.graphics.Color.parseColor(it)) } ?: defaultPalette
    val motif = when {
        documentKey.startsWith("church_") || documentKey in setOf("church", "organization_church") -> 3
        documentKey.startsWith("rod_") || documentKey in setOf(
            "family", "children", "birth_extract", "marriage_extract", "death_extract", "large_family"
        ) -> 4
        documentKey in setOf("disability", "disability_card", "vaccines", "blood_donor", "ehic",
            "professional_medical", "professional_therapeutic") || documentKey.startsWith("medical_") -> 7
        documentKey in setOf("student", "training", "eu_education") ||
            documentKey.startsWith("professional_education") -> 6
        documentKey in setOf("passport", "mission_travel", "vehicle_registration") -> 5
        documentKey.startsWith("professional_") || documentKey == "professional" -> 6
        documentKey.startsWith("organization_") || documentKey.startsWith("entity_") ||
            documentKey in setOf("registry", "business", "entity", "parliament", "bank_accounts") -> 2
        documentKey in setOf("identity", "identity_card", "profile", "official", "criminal_record") -> 0
        else -> 1
    }
    val selectedMotif = tilePreferences.getString("${documentKey}_tile_vector_motif", "auto").orEmpty()
    val activeMotif = selectedMotif.toIntOrNull()?.takeIf { it in 0..7 } ?: motif
    Box(modifier.clip(RoundedCornerShape(13.dp))) {
    if (tileBackgroundBitmap != null) {
        Image(tileBackgroundBitmap.asImageBitmap(), contentDescription = "Indywidualne tło kafelka dokumentu",
            modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
    }
    if (tileVectorBackgroundBitmap != null) {
        Image(tileVectorBackgroundBitmap.asImageBitmap(), contentDescription = "Wektorowe tło kafelka dokumentu", modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.24f)
    }
    ComposeCanvas(Modifier.matchParentSize()) {
        drawRect(brush = Brush.linearGradient(
            colors = palette,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f)
        ), alpha = if (tileBackgroundBitmap != null || tileVectorBackgroundBitmap != null) 0.22f else 1f)
        val cx = size.width * 0.82f
        val cy = size.height * 0.28f
        val radius = maxOf(size.width, size.height) * 0.34f
        val watermark = Color.White.copy(alpha = 0.13f)
        val fine = Color.White.copy(alpha = 0.09f)
        drawCircle(watermark, radius, Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
        drawCircle(fine, radius * 0.76f, Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
        drawArc(watermark, -28f, 238f, false,
            topLeft = Offset(cx - radius * 0.88f, cy - radius * 0.88f),
            size = Size(radius * 1.76f, radius * 1.76f),
            style = Stroke(width = 3.dp.toPx())
        )
        val stroke = Stroke(width = maxOf(1.5.dp.toPx(), size.minDimension * 0.012f))
        when (activeMotif) {
            0 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.48f), Offset(cx, cy + radius * 0.48f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.42f, cy), Offset(cx + radius * 0.42f, cy), strokeWidth = stroke.width)
                drawCircle(watermark, radius * 0.24f, Offset(cx, cy), style = stroke)
            }
            1 -> {
                val left = cx - radius * 0.32f
                val top = cy - radius * 0.42f
                drawRoundRect(watermark, Offset(left, top), Size(radius * 0.64f, radius * 0.84f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.12f), style = stroke)
                drawLine(watermark, Offset(left + radius * 0.13f, cy - radius * 0.12f),
                    Offset(left + radius * 0.51f, cy - radius * 0.12f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(left + radius * 0.13f, cy + radius * 0.10f),
                    Offset(left + radius * 0.45f, cy + radius * 0.10f), strokeWidth = stroke.width)
            }
            2 -> {
                drawLine(watermark, Offset(cx - radius * 0.46f, cy + radius * 0.34f),
                    Offset(cx + radius * 0.46f, cy + radius * 0.34f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.36f, cy + radius * 0.34f),
                    Offset(cx - radius * 0.36f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx + radius * 0.36f, cy + radius * 0.34f),
                    Offset(cx + radius * 0.36f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.46f, cy - radius * 0.20f),
                    Offset(cx + radius * 0.46f, cy - radius * 0.20f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx - radius * 0.50f, cy - radius * 0.20f),
                    Offset(cx, cy - radius * 0.48f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.48f),
                    Offset(cx + radius * 0.50f, cy - radius * 0.20f), strokeWidth = stroke.width)
            }
            3 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.52f),
                    Offset(cx, cy + radius * 0.48f), strokeWidth = stroke.width * 1.25f)
                drawLine(watermark, Offset(cx - radius * 0.32f, cy - radius * 0.20f),
                    Offset(cx + radius * 0.32f, cy - radius * 0.20f), strokeWidth = stroke.width * 1.25f)
                drawArc(watermark, 205f, 130f, false,
                    topLeft = Offset(cx - radius * 0.44f, cy + radius * 0.05f),
                    size = Size(radius * 0.88f, radius * 0.70f), style = stroke)
            }
            4 -> {
                drawLine(watermark, Offset(cx, cy - radius * 0.36f),
                    Offset(cx, cy + radius * 0.43f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.08f),
                    Offset(cx - radius * 0.34f, cy - radius * 0.34f), strokeWidth = stroke.width)
                drawLine(watermark, Offset(cx, cy - radius * 0.08f),
                    Offset(cx + radius * 0.34f, cy - radius * 0.34f), strokeWidth = stroke.width)
                drawCircle(watermark, radius * 0.13f, Offset(cx, cy - radius * 0.42f), style = stroke)
                drawCircle(watermark, radius * 0.12f, Offset(cx - radius * 0.38f, cy - radius * 0.38f), style = stroke)
                drawCircle(watermark, radius * 0.12f, Offset(cx + radius * 0.38f, cy - radius * 0.38f), style = stroke)
                drawArc(watermark, 190f, 160f, false,
                    Offset(cx - radius * 0.42f, cy + radius * 0.10f),
                    Size(radius * 0.84f, radius * 0.62f), style = stroke)
            }
            5 -> {
                drawCircle(watermark, radius * 0.46f, Offset(cx, cy), style = stroke)
                drawOval(watermark, Offset(cx - radius * 0.23f, cy - radius * 0.46f),
                    Size(radius * 0.46f, radius * 0.92f), style = stroke)
                drawLine(watermark, Offset(cx - radius * 0.43f, cy),
                    Offset(cx + radius * 0.43f, cy), strokeWidth = stroke.width)
                drawArc(watermark, 0f, 180f, false,
                    Offset(cx - radius * 0.43f, cy - radius * 0.22f),
                    Size(radius * 0.86f, radius * 0.44f), style = stroke)
                drawArc(watermark, 180f, 180f, false,
                    Offset(cx - radius * 0.43f, cy - radius * 0.22f),
                    Size(radius * 0.86f, radius * 0.44f), style = stroke)
            }
            6 -> {
                val book = Path().apply {
                    moveTo(cx, cy + radius * 0.38f)
                    cubicTo(cx - radius * 0.20f, cy + radius * 0.22f, cx - radius * 0.38f, cy + radius * 0.28f, cx - radius * 0.48f, cy + radius * 0.20f)
                    lineTo(cx - radius * 0.48f, cy - radius * 0.36f)
                    cubicTo(cx - radius * 0.28f, cy - radius * 0.46f, cx - radius * 0.12f, cy - radius * 0.30f, cx, cy - radius * 0.18f)
                    cubicTo(cx + radius * 0.12f, cy - radius * 0.30f, cx + radius * 0.28f, cy - radius * 0.46f, cx + radius * 0.48f, cy - radius * 0.36f)
                    lineTo(cx + radius * 0.48f, cy + radius * 0.20f)
                    cubicTo(cx + radius * 0.28f, cy + radius * 0.28f, cx + radius * 0.20f, cy + radius * 0.22f, cx, cy + radius * 0.38f)
                    close()
                }
                drawPath(book, watermark, style = stroke)
                drawLine(watermark, Offset(cx, cy - radius * 0.18f),
                    Offset(cx, cy + radius * 0.36f), strokeWidth = stroke.width)
            }
            7 -> {
                drawRoundRect(watermark, Offset(cx - radius * 0.15f, cy - radius * 0.48f),
                    Size(radius * 0.30f, radius * 0.96f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.08f), style = stroke)
                drawRoundRect(watermark, Offset(cx - radius * 0.48f, cy - radius * 0.15f),
                    Size(radius * 0.96f, radius * 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.08f), style = stroke)
                drawCircle(fine, radius * 0.58f, Offset(cx, cy), style = stroke)
            }
        }
        // Layered security-print treatment shared by every document tile:
        // guilloche rosette, micro-grid, diagonal engraving and precision frame.
        val minSide = size.minDimension
        val inset = minSide * 0.055f
        val frameColor = Color.White.copy(alpha = 0.16f)
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2f, size.height - inset * 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(minSide * 0.055f),
            style = Stroke(width = maxOf(0.7.dp.toPx(), minSide * 0.0025f))
        )
        // Fine engraved diagonal lines are deliberately clipped to the lower-left field.
        val engraving = Color.White.copy(alpha = 0.055f)
        val spacing = maxOf(5.dp.toPx(), minSide * 0.035f)
        var line = -size.height
        while (line < size.width * 0.72f) {
            drawLine(
                color = engraving,
                start = Offset(line, size.height),
                end = Offset(line + size.height * 0.72f, size.height * 0.28f),
                strokeWidth = maxOf(0.55.dp.toPx(), minSide * 0.0018f)
            )
            line += spacing
        }
        // Halftone security field.
        val dotStep = maxOf(5.dp.toPx(), minSide * 0.045f)
        var dotY = size.height * 0.58f
        while (dotY < size.height * 0.91f) {
            var dotX = size.width * 0.07f
            while (dotX < size.width * 0.48f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = maxOf(0.45.dp.toPx(), minSide * 0.0022f),
                    center = Offset(dotX, dotY)
                )
                dotX += dotStep
            }
            dotY += dotStep
        }
        // Guilloche rosette: concentric rings and radial spokes.
        val rosetteCenter = Offset(size.width * 0.84f, size.height * 0.76f)
        val rosetteRadius = minSide * 0.20f
        val rosetteColor = Color.White.copy(alpha = 0.13f)
        for (ring in 0..3) {
            drawCircle(
                color = rosetteColor.copy(alpha = 0.12f + ring * 0.025f),
                radius = rosetteRadius * (0.42f + ring * 0.19f),
                center = rosetteCenter,
                style = Stroke(width = maxOf(0.65.dp.toPx(), minSide * 0.002f))
            )
        }
        for (spoke in 0 until 16) {
            val angle = spoke * (2f * Math.PI.toFloat() / 16f)
            val inner = rosetteRadius * 0.34f
            val outer = rosetteRadius * (if (spoke % 2 == 0) 1.12f else 0.88f)
            drawLine(
                color = rosetteColor,
                start = Offset(
                    rosetteCenter.x + kotlin.math.cos(angle.toDouble()).toFloat() * inner,
                    rosetteCenter.y + kotlin.math.sin(angle.toDouble()).toFloat() * inner
                ),
                end = Offset(
                    rosetteCenter.x + kotlin.math.cos(angle.toDouble()).toFloat() * outer,
                    rosetteCenter.y + kotlin.math.sin(angle.toDouble()).toFloat() * outer
                ),
                strokeWidth = maxOf(0.6.dp.toPx(), minSide * 0.002f)
            )
        }
        // Four registration marks and a discreet security seal at the lower edge.
        val mark = minSide * 0.035f
        val corners = listOf(
            Offset(inset + mark, inset + mark),
            Offset(size.width - inset - mark, inset + mark),
            Offset(inset + mark, size.height - inset - mark),
            Offset(size.width - inset - mark, size.height - inset - mark)
        )
        corners.forEach { p ->
            drawCircle(frameColor, radius = maxOf(1.dp.toPx(), minSide * 0.006f), center = p)
        }
        drawCircle(Color.White.copy(alpha = 0.08f), radius * 0.10f,
            Offset(size.width * 0.16f, size.height * 0.82f))
    }
        Image(
            painter = painterResource(documentTileWatermarkResource(documentKey)),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.30f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )
        // Delikatna, wspólna warstwa zabezpieczająca: samopodobna rozeta
        // geometryczna pozostaje czytelna na wszystkich tłach kategorii.
        Image(
            painter = painterResource(R.drawable.tile_watermark_fractal),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.13f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )
    }
}

@Composable
private fun DocumentCardEmblem(documentKey: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = remember(documentKey) { LegitimacyPreviewSettingsStore.load(context, documentKey) }
    val documentTitle = documentBrandTargets.firstOrNull { it.first == documentKey }?.second ?: documentKey
    val assigned = remember(documentKey, settings.crestAssetId) {
        settings.crestAssetId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?: DocumentBrandStore.imageForDocument(context, documentKey, documentTitle, "Godło")
            ?: DocumentBrandStore.imageForDocument(context, documentKey, documentTitle, "Herb")
    }
    val selected = assigned ?: remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.a_clean_vector_style_emblem_on_a_transparent_backg)
    }
    if (selected != null) {
        Box(
            modifier.clip(RoundedCornerShape(5.dp))
                .background(if (assigned == null) Color.White.copy(alpha = 0.16f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                selected.asImageBitmap(),
                contentDescription = "Indywidualne godło dokumentu",
                modifier = Modifier.fillMaxSize().padding(2.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

internal enum class DocumentScreenMode { PREVIEW, EDITOR }
internal val LocalDocumentScreenMode = androidx.compose.runtime.compositionLocalOf { DocumentScreenMode.PREVIEW }
internal data class CredentialFieldVisibilityBinding(
    val documentKey: String,
    val settings: MutableState<LegitimacyPreviewSettings>,
    val fields: MutableMap<String, String>
)
internal val LocalCredentialFieldVisibility = androidx.compose.runtime.compositionLocalOf<CredentialFieldVisibilityBinding?> { null }
internal val LocalDocumentOcrStageRendered = androidx.compose.runtime.compositionLocalOf { false }

@Composable
private fun CredentialAppearanceEditorEntry(
    documentKey: String,
    onOpenAppearance: (String) -> Unit,
    showAppearanceAction: Boolean = true,
    showOcrStage: Boolean = true,
    showOriginalAttachment: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val ocrStorageKey = "groupedOcr_${documentKey.hashCode().toUInt().toString(16)}"
    val ocrFields = remember(documentKey) {
        mutableStateMapOf<String, String>().apply {
            val encoded = DemoIdentityStore.load(context)[ocrStorageKey]
            runCatching {
                val saved = JSONObject(encoded ?: "{}")
                saved.keys().forEach { key -> put(key, saved.optString(key)) }
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        // OCR jest renderowany w jednym, stałym miejscu nad edytorem dokumentu.
        // Formularz potomny nie może dodawać kolejnego panelu OCR do nagłówka.
        if (showOcrStage) {
            GeneralDocumentOcrEditor(documentKey, ocrFields, context) {
                val snapshot = DemoIdentityStore.load(context).toMutableMap()
                val savedOcr = JSONObject()
                ocrFields.forEach { (key, value) -> savedOcr.put(key, value) }
                snapshot[ocrStorageKey] = savedOcr.toString()
                DemoIdentityStore.save(context, snapshot)
            }
            val suggestions = ocrFields.filterKeys { it != "${documentKey}OcrText" && it.isNotBlank() }
            if (suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Propozycje odczytane przez OCR — do ręcznej weryfikacji",
                            color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                        suggestions.forEach { (key, value) ->
                            Text("${key}: ${value}", color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        if (showOriginalAttachment) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                DocumentOriginalAttachments(documentKey)
            }
        }
        CompositionLocalProvider(LocalDocumentOcrStageRendered provides showOcrStage) {
            Box(Modifier.weight(1f)) { content() }
        }
        if (showAppearanceAction) {
            OutlinedButton(
                onClick = { onOpenAppearance(documentKey) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Edytuj wygląd graficzny")
            }
        }
    }
}


@Composable
internal fun AdditionalInformationSection(
    title: String,
    fields: MutableMap<String, String>,
    prefix: String,
    explanation: String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, modifier = Modifier.weight(1f).padding(start = 9.dp),
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "Zwiń" else "Rozwiń", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                if (explanation.isNotBlank()) Text(explanation, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                DemoDataField("${prefix}Source", "Źródło danych / dokument źródłowy", fields)
                DemoDataField("${prefix}LegalBasis", "Podstawa prawna / regulaminowa", fields)
                DemoDataField("${prefix}VerifiedAt", "Data ostatniej weryfikacji danych", fields, "DD-MM-RRRR")
                DemoDataField("${prefix}VerifiedBy", "Podmiot / osoba potwierdzająca dane", fields)
                DemoDataField("${prefix}Notes", "Adnotacje i informacje uzupełniające", fields)
                Text(
                    "Informacje dodatkowe uzupełniają zapis danych o źródło, podstawę i datę weryfikacji.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
internal fun DemoDataField(
    key: String,
    label: String,
    fields: MutableMap<String, String>,
    placeholder: String = "Wpisz dane",
    storageKey: String = key
) {
    val binding = LocalCredentialFieldVisibility.current
    val settings = binding?.settings?.value
    val selectableKeys = binding?.let { credentialFieldOptions(it.documentKey).map { option -> option.first }.toSet() }.orEmpty()
    val side = settings?.let {
        credentialFieldSide(key, it.visibleFieldsConfigured, it.visibleFields, it.fieldSides)
    } ?: CREDENTIAL_FIELD_FRONT
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        OutlinedTextField(
            value = fields[storageKey] ?: "",
            onValueChange = { fields[storageKey] = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true
        )
        if (binding != null && binding.documentKey.isNotBlank() && key in selectableKeys) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    CREDENTIAL_FIELD_HIDDEN to "Brak",
                    CREDENTIAL_FIELD_FRONT to "Awers",
                    CREDENTIAL_FIELD_BACK to "Rewers"
                ).forEach { (target, caption) ->
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).clickable {
                            val currentSettings = binding.settings.value
                            val updated = currentSettings.copy(
                                fieldSides = currentSettings.fieldSides + (key to target),
                                visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) currentSettings.visibleFields - key
                                    else currentSettings.visibleFields + key,
                                visibleFieldsConfigured = true
                            )
                            LegitimacyPreviewSettingsStore.save(context, binding.documentKey, updated)
                            binding.settings.value = updated
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = side == target,
                            onClick = {
                                val currentSettings = binding.settings.value
                                val updated = currentSettings.copy(
                                    fieldSides = currentSettings.fieldSides + (key to target),
                                    visibleFields = if (target == CREDENTIAL_FIELD_HIDDEN) currentSettings.visibleFields - key
                                        else currentSettings.visibleFields + key,
                                    visibleFieldsConfigured = true
                                )
                                LegitimacyPreviewSettingsStore.save(context, binding.documentKey, updated)
                                binding.settings.value = updated
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(caption, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}


