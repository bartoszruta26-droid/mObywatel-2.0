// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

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
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

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

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) recognizeImage(it) }
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
            Text("Odczyt dokumentu z filmu przez OCR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text("Nagraj krótki film, powoli pokazując dokument z bliska. Aplikacja pobierze do 24 klatek nagrania, rozpozna tekst na każdej z nich i połączy wyniki, aby uzupełnić pasujące pola. Możesz też wybrać wcześniej nagrany film albo zdjęcie/skan. OCR nie potwierdza autentyczności dokumentu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                runCatching {
                    val file = File.createTempFile("document-ocr-video-", ".mp4", context.cacheDir)
                    androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.ocrfiles", file)
                }.onSuccess { pendingVideoUri = it; videoCamera.launch(it) }
                    .onFailure { saveMessage("Nie można uruchomić kamery wideo: ${it.localizedMessage ?: "błąd przygotowania nagrania"}") }
            }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Nagraj film dokumentu") }
            OutlinedButton(onClick = { videoPicker.launch("video/*") }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Wybierz nagrany film") }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Wybierz zdjęcie lub skan") }
            if (processing) Text("Analiza klatek filmu i uzupełnianie pól…", color = MaterialTheme.colorScheme.primary)
            if (mappedCount > 0) Text("Wstępnie uzupełniono $mappedCount pustych pól", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            if (recognizedText.isNotBlank()) {
                Text("Tekst rozpoznany z nagrania — sprawdź", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(recognizedText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
@Composable
private fun LoyaltyCardsEditor(
    fields: MutableMap<String, String>,
    context: Context,
    saveMessage: (String) -> Unit
) {
    val existingCount = (1..25).lastOrNull { n -> fields.keys.any { it.startsWith("loyalty$n") && fields[it].orEmpty().isNotBlank() } } ?: 0
    var cardCount by remember { mutableIntStateOf(maxOf(1, fields["loyaltyCardCount"]?.toIntOrNull() ?: existingCount)) }
    var selectedCard by remember { mutableIntStateOf(1) }
    var processing by remember { mutableStateOf(false) }
    val prefix = "loyalty$selectedCard"
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            processing = true
            // Obraz jest czytany bezpośrednio z URI dostawcy treści i przekazywany ML Kit.
            // Nie kopiujemy go do filesDir/cacheDir ani nie zapisujemy jego bajtów w ewidencji.
            val image = runCatching { InputImage.fromFilePath(context, uri) }
            image.onSuccess { input ->
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val scanner = BarcodeScanning.getClient()
                recognizer.process(input)
                    .addOnSuccessListener { text ->
                        val lines = text.textBlocks.flatMap { it.lines }.map { it.text.trim() }.filter { it.isNotBlank() }
                        if (lines.isNotEmpty()) {
                            if (fields["${prefix}Store"].isNullOrBlank()) fields["${prefix}Store"] = lines.first()
                            val numberCandidate = lines.firstOrNull { it.filter(Char::isDigit).length >= 6 }
                            if (numberCandidate != null && fields["${prefix}Number"].isNullOrBlank()) fields["${prefix}Number"] = numberCandidate.filter { it.isLetterOrDigit() }
                            fields["${prefix}OcrText"] = text.text
                        }
                    }
                    .addOnFailureListener { saveMessage("OCR nie odczytał tekstu: ${it.localizedMessage ?: "błąd"}") }
                    .addOnCompleteListener {
                        scanner.process(input)
                            .addOnSuccessListener { codes ->
                                codes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue?.let { fields["${prefix}Qr"] = it }
                                codes.firstOrNull { it.format != Barcode.FORMAT_QR_CODE }?.rawValue?.let { fields["${prefix}Barcode"] = it }
                            }
                            .addOnFailureListener { saveMessage("Odczyt kodu nie powiódł się: ${it.localizedMessage ?: "błąd"}") }
                            .addOnCompleteListener {
                                recognizer.close()
                                scanner.close()
                                processing = false
                                saveMessage("Zakończono odczyt zdjęcia. Sprawdź rozpoznane pola i zapisz dane.")
                            }
                    }
            }.onFailure {
                processing = false
                saveMessage("Nie można otworzyć zdjęcia: ${it.localizedMessage ?: "błąd pliku"}")
            }
        }
    }
    val liveScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { value ->
            if (result.formatName == BarcodeFormat.QR_CODE.name) fields["${prefix}Qr"] = value
            else fields["${prefix}Barcode"] = value
            saveMessage("Odczytano kod. Sprawdź dane i zapisz kartę.")
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Karty lojalnościowe sklepów", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Można prowadzić maksymalnie 25 kart. Dane można ręcznie poprawiać. Zdjęcie jest przetwarzane bez zapisywania pliku w pamięci aplikacji; zapisujesz wyłącznie sprawdzone dane tekstowe.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                if (cardCount < 25) {
                    cardCount += 1
                    fields["loyaltyCardCount"] = cardCount.toString()
                    selectedCard = cardCount
                    saveMessage("Dodano miejsce na kartę nr $cardCount. Zapisz zmiany.")
                } else saveMessage("Osiągnięto limit 25 kart.")
            }, enabled = cardCount < 25, modifier = Modifier.weight(1f)) { Text("Dodaj kartę ($cardCount/25)") }
            OutlinedButton(onClick = {
                val hasData = fields.keys.any { it.startsWith(prefix) && fields[it].orEmpty().isNotBlank() }
                if (hasData) {
                    val keys = fields.keys.filter { it.startsWith(prefix) }
                    keys.forEach { fields.remove(it) }
                    if (selectedCard == cardCount && cardCount > 1) {
                        cardCount -= 1
                        fields["loyaltyCardCount"] = cardCount.toString()
                        selectedCard = cardCount
                    }
                    saveMessage("Usunięto dane wybranej karty. Zapisz zmiany, aby zatwierdzić usunięcie.")
                } else saveMessage("Wybrana karta jest już pusta.")
            }, enabled = selectedCard <= cardCount, modifier = Modifier.weight(1f)) { Text("Usuń wybraną") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            (1..cardCount).forEach { n ->
                androidx.compose.material3.FilterChip(selected = selectedCard == n, onClick = { selectedCard = n }, label = { Text(n.toString()) })
            }
        }
        Text("Karta $selectedCard z $cardCount", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        DemoDataField("${prefix}Store", "Sklep / marka", fields)
        DemoDataField("${prefix}Program", "Nazwa programu lojalnościowego", fields)
        DemoDataField("${prefix}Number", "Numer karty nadany przez sklep", fields)
        DemoDataField("${prefix}LocalId", "Lokalny numer ewidencyjny", fields)
        if (fields["${prefix}LocalId"].isNullOrBlank()) {
            OutlinedButton(onClick = {
                fields["${prefix}LocalId"] = "LOK-" + UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
            }, modifier = Modifier.fillMaxWidth()) { Text("Wygeneruj lokalny numer ewidencyjny") }
        }
        DemoDataField("${prefix}Barcode", "Treść kodu kreskowego", fields)
        DemoDataField("${prefix}Qr", "Treść kodu QR", fields)
        DemoDataField("${prefix}Status", "Status karty", fields, "aktywna / nieaktywna")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("OCR ze zdjęcia") }
            OutlinedButton(onClick = {
                val options = ScanOptions().setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                    .setPrompt("Zeskanuj kod karty lojalnościowej").setBeepEnabled(false)
                liveScanner.launch(options)
            }, modifier = Modifier.weight(1f)) { Text("Skanuj kod") }
        }
        if (processing) Text("Odczytywanie obrazu…", color = MaterialTheme.colorScheme.primary)
        val barcodePayload = fields["${prefix}Barcode"].orEmpty().ifBlank { fields["${prefix}Number"].orEmpty() }
        val qrPayload = fields["${prefix}Qr"].orEmpty().ifBlank { fields["${prefix}Number"].orEmpty() }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val payload = fields["${prefix}Number"].orEmpty().ifBlank { fields["${prefix}LocalId"].orEmpty() }
                if (payload.isNotBlank()) fields["${prefix}Barcode"] = payload
                else saveMessage("Wpisz numer karty lub wygeneruj lokalny numer ewidencyjny.")
            }, modifier = Modifier.weight(1f)) { Text("Utwórz kod kreskowy") }
            OutlinedButton(onClick = {
                val payload = fields["${prefix}Number"].orEmpty().ifBlank { fields["${prefix}LocalId"].orEmpty() }
                if (payload.isNotBlank()) fields["${prefix}Qr"] = payload
                else saveMessage("Wpisz numer karty lub wygeneruj lokalny numer ewidencyjny.")
            }, modifier = Modifier.weight(1f)) { Text("Utwórz kod QR") }
        }
        LoyaltyGeneratedCodes(barcodePayload, qrPayload)
        if (fields["${prefix}OcrText"].orEmpty().isNotBlank()) {
            Text("Pełny tekst rozpoznany przez OCR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(fields["${prefix}OcrText"].orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        AdditionalInformationSection("Źródło i weryfikacja danych karty", fields, "loyaltyExtra",
            "OCR i skaner dostarczają propozycję danych, a nie potwierdzenie ich poprawności. Numer lokalny jest identyfikatorem tej ewidencji i nie jest numerem wydanym przez sklep.")
    }
}

@Composable
private fun LoyaltyGeneratedCodes(barcodeValue: String, qrValue: String) {
    val barcode = remember(barcodeValue) { generateLoyaltyCodeBitmap(barcodeValue, BarcodeFormat.CODE_128) }
    val qr = remember(qrValue) { generateLoyaltyCodeBitmap(qrValue, BarcodeFormat.QR_CODE) }
    if (barcode != null || qr != null) {
        Text("Podgląd kodów", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        if (barcode != null) Image(barcode.asImageBitmap(), contentDescription = "Kod kreskowy karty lojalnościowej",
            modifier = Modifier.fillMaxWidth().height(64.dp), contentScale = ContentScale.Fit)
        if (qr != null) Image(qr.asImageBitmap(), contentDescription = "Kod QR karty lojalnościowej",
            modifier = Modifier.size(170.dp), contentScale = ContentScale.Fit)
    }
}

private fun generateLoyaltyCodeBitmap(value: String, format: BarcodeFormat): Bitmap? {
    if (value.isBlank()) return null
    return runCatching {
        val width = if (format == BarcodeFormat.QR_CODE) 480 else 900
        val height = if (format == BarcodeFormat.QR_CODE) 480 else 220
        val matrix = MultiFormatWriter().encode(value, format, width, height,
            mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8"))
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (y in 0 until height) for (x in 0 until width) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }.getOrNull()
}


@Composable
private fun ApplicationEntryFlow(onAppearanceChange: (ApplicationAppearance) -> Unit) {
    var stage by remember { mutableIntStateOf(0) }
    // The welcome screen is the first in-app screen; do not show a separate
    // timed emblem-only intro before it.
    when (stage) {
        0 -> EntryWelcomeScreen(
            onStart = { stage = 1 },
            onSkip = { stage = 1 }
        )
        1 -> EntryLoginScreen(
            onDemo = { stage = 2 },
            onBack = { stage = 0 }
        )
        else -> HomeScreen(onAppearanceChange = onAppearanceChange)
    }
}

@Composable
private fun BrandMark(size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Image(
        painter = painterResource(applicationDrawable(context, appearance.headerEmblem,
            R.drawable.file_00000000532082108d561dcd2385f8f9)),
        contentDescription = "Godło aplikacji",
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun EntryBackground() {
    val context = LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Image(
        painter = painterResource(
            applicationDrawable(
                context,
                appearance.loginBackground,
                R.drawable.file_00000000e0e4820a839fa3184993f41b
            )
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(Modifier.fillMaxSize().background(Color(0x55000000)))
}

@Composable
private fun EntryWelcomeScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    val foreground = Color(0xFFFFF1D1)
    Box(Modifier.fillMaxSize()) {
        EntryBackground()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.file_00000000532082108d561dcd2385f8f9),
                    contentDescription = "Godło Polski w zaokrąglonym kwadratowym znaku aplikacji",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    "mObywatel",
                    color = foreground,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(34.dp))
            Text(
                "Dzień dobry!",
                color = foreground,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Zaloguj się do aplikacji",
                color = foreground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(17.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Zaloguj się", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Więcej o Krajowym Planie\nOdbudowy",
                    modifier = Modifier.weight(1f),
                    color = foreground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "wersja\n4.93.1\n(6704)",
                    color = foreground,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EntryLoginScreen(onDemo: () -> Unit, onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val cream = Color(0xFFFFF1D1)
    val skyBlue = Color(0xFF9EDCFF)
    Box(Modifier.fillMaxSize()) {
        EntryBackground()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BrandMark(size = 72.dp)
                Text(
                    "mObywatel",
                    color = cream,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(38.dp))
            Text(
                "Wpisz hasło",
                color = cream,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Zaloguj się do aplikacji",
                color = cream,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(42.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x665B555C))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Hasło",
                    color = cream,
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = cream
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = cream,
                        focusedBorderColor = cream,
                        unfocusedBorderColor = cream,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Text(
                    "Nie pamiętasz hasła?",
                    color = skyBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onDemo,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(17.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Zaloguj się", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Więcej o Krajowym Planie",
                    color = cream,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "wersja",
                    color = cream,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

private data class ServiceItem(
    val title: String,
    val subtitle: String,
    val icon: Int? = null,
    val fallbackIcon: ImageVector? = null,
    val category: String = "Pozostałe"
)

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

private const val DOCUMENT_DISPLAY_PREFS = "document_display_preferences"
private const val DOCUMENT_DISPLAY_KEY_PREFIX = "placement_"
internal val documentDisplayOptions = (listOf(
    Triple("profile", "Dane osobowe profilu", R.drawable.lib_ic_account),
    Triple("identity", "Dowód tożsamości", R.drawable.documents_identity_card),
    Triple("passport", "Dane paszportowe", R.drawable.documents_passport),
    Triple("registry", "Odpis z rejestru podmiotów", R.drawable.court_registry_icon),
    Triple("family", "Dokument rodowy", R.drawable.documents_family_record),
    Triple("parliament", "Legitymacja poselska", R.drawable.documents_deputy_legitimacy),
    Triple("training", "Legitymacja kursanta", R.drawable.documents_course_legitimacy),
    Triple("student", "Legitymacja ucznia/studenta", R.drawable.tile_ic_student_outline),
    Triple("disability", "Orzeczenia i zaświadczenia", R.drawable.tile_ic_disability_outline),
    Triple("church", "Dokumenty kościelne", R.drawable.tile_ic_church_outline),
    Triple("civil", "Dokumenty cywilne", R.drawable.tile_ic_civil_outline),
    Triple("business", "Dokumenty firmowe i podmiotowe", R.drawable.tile_ic_business_outline),
    Triple("bank_accounts", "Konta bankowe", R.drawable.lib_ic_wallet),
    Triple("rod_bank_accounts", "Rachunki bankowe Rodu", R.drawable.lib_ic_wallet),
    Triple("entity_bank_accounts", "Rachunki bankowe podmiotów", R.drawable.lib_ic_wallet),
    Triple("vaccines", "Książeczka szczepień WHO", R.drawable.lib_ic_medical_record),
    Triple("signed_pdf", "Dokumenty PDF i podpisy", R.drawable.lib_ic_document_signed),
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
    Triple("official", "Pozostałe dokumenty urzędowe", R.drawable.lib_ic_document)
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
    id == "church" || id.startsWith("church_") || id == "organization_church" ||
        id == "mission_travel" || churchGraphicDocumentTargets().any { it.first == id } ->
        "Dokumenty kościelne"
    id == "family" || id.startsWith("rod_") || id == "large_family" ->
        "Dokumenty rodowe i rodzinne"
    id == "business" || id == "entity" || id == "registry" || id == "bank_accounts" ||
        id == "entity_bank_accounts" || id.startsWith("entity_") ||
        id.startsWith("organization_") ->
        "Dokumenty firmowe i podmiotowe"
    id == "civil" || id in setOf(
        "birth_extract", "marriage_extract", "death_extract", "criminal_record",
        "vehicle_registration", "insurance_vehicle", "insurance_professional",
        "insurance_private", "insurance_business", "official"
    ) || officialGraphicDocumentTargets().any { it.first == id } ->
        "Dokumenty cywilne i urzędowe"
    id == "professional" || id.startsWith("professional_") || id in setOf("training", "student", "eu_education") ->
        "Dokumenty zawodowe i edukacyjne"
    id in setOf("disability", "disability_card", "blood_donor", "ehic", "vaccines", "children") ->
        "Dokumenty zdrowotne i rodzinne"
    id in setOf("imported", "signed_pdf", "branding", "loyalty", "loyalty_cards") ->
        "Pliki i pozostałe dokumenty"
    else -> "Dokumenty osobiste i tożsamościowe"
}
internal val documentVisibilityGroupOrder = listOf(
    "Dokumenty osobiste i tożsamościowe",
    "Dokumenty cywilne i urzędowe",
    "Dokumenty kościelne",
    "Dokumenty rodowe i rodzinne",
    "Dokumenty firmowe i podmiotowe",
    "Dokumenty zawodowe i edukacyjne",
    "Dokumenty zdrowotne i rodzinne",
    "Pliki i pozostałe dokumenty"
)
private val defaultDocumentPlacements = documentDisplayOptions.associate { (id, _, _) ->
    id to if (id == "identity") "dashboard" else "hidden"
}
private const val DOCUMENT_VISIBILITY_MIGRATION_KEY = "all_hidden_except_identity_v1"
private const val DOCUMENT_CARD_STYLE_KEY = "document_card_style"
private fun loadDocumentCardStyle(context: Context): String =
    context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .getString(DOCUMENT_CARD_STYLE_KEY, "large")?.takeIf { it in setOf("large", "small", "list") } ?: "large"
private fun saveDocumentCardStyle(context: Context, style: String) {
    require(style in setOf("large", "small", "list")) { "Nieprawidłowy układ kafelków dokumentów." }
    check(context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(DOCUMENT_CARD_STYLE_KEY, style).commit()) {
        "Nie udało się zapisać układu kafelków dokumentów."
    }
}

private const val DOCUMENT_DEFAULT_ICON_KEY = "default_document_icon"
private const val DOCUMENT_ICON_OVERRIDE_PREFIX = "document_icon_"
private val documentIconChoices = listOf(
    "lib_ic_document" to "Dokument",
    "lib_ic_account" to "Osoba",
    "lib_ic_credit_card" to "Karta",
    "lib_ic_folder" to "Folder",
    "lib_ic_medical_record" to "Dokument medyczny",
    "lib_ic_school" to "Edukacja",
    "lib_ic_building" to "Budynek",
    "lib_ic_wallet" to "Portfel",
    "lib_ic_fingerprint" to "Odcisk palca",
    "lib_ic_flag" to "Flaga",
    "documents_identity_card" to "Dowód tożsamości",
    "documents_passport" to "Paszport",
    "documents_family_record" to "Dokument rodowy",
    "documents_student_legitimacy" to "Legitymacja uczniowska",
    "tile_ic_student_outline" to "Legitymacja ucznia/studenta",
    "tile_ic_disability_outline" to "Orzeczenia i zaświadczenia",
    "tile_ic_church_outline" to "Dokumenty kościelne",
    "tile_ic_civil_outline" to "Dokumenty cywilne",
    "tile_ic_business_outline" to "Dokumenty firmowe i podmiotowe"
)
private fun loadDefaultDocumentIcon(context: Context): String =
    context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .getString(DOCUMENT_DEFAULT_ICON_KEY, "automatic")
        ?.takeIf { it == "automatic" || documentIconChoices.any { choice -> choice.first == it } }
        ?: "automatic"
private fun saveDefaultDocumentIcon(context: Context, value: String) {
    require(value == "automatic" || documentIconChoices.any { it.first == value })
    check(context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(DOCUMENT_DEFAULT_ICON_KEY, value).commit()) {
        "Nie udało się zapisać domyślnej ikony dokumentów."
    }
}
private fun loadDocumentIconOverrides(context: Context): Map<String, String> {
    val prefs = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
    return documentDisplayOptions.mapNotNull { (id, _, _) ->
        prefs.getString(DOCUMENT_ICON_OVERRIDE_PREFIX + id, null)
            ?.takeIf { selected -> documentIconChoices.any { it.first == selected } }
            ?.let { id to it }
    }.toMap()
}
private fun saveDocumentIconOverrides(context: Context, values: Map<String, String>) {
    val editor = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE).edit()
    documentDisplayOptions.forEach { (id, _, _) ->
        val value = values[id]
        if (value.isNullOrBlank()) editor.remove(DOCUMENT_ICON_OVERRIDE_PREFIX + id)
        else {
            require(documentIconChoices.any { it.first == value }) { "Nieprawidłowa ikona dokumentu: $id" }
            editor.putString(DOCUMENT_ICON_OVERRIDE_PREFIX + id, value)
        }
    }
    check(editor.commit()) { "Nie udało się zapisać indywidualnych ikon dokumentów." }
}
private fun resolveDocumentIconResource(
    context: Context,
    documentId: String,
    defaultIcon: String,
    overrides: Map<String, String>
): Int {
    val selected = overrides[documentId] ?: defaultIcon
    if (selected != "automatic") {
        context.resources.getIdentifier(selected, "drawable", context.packageName)
            .takeIf { it != 0 }?.let { return it }
    }
    return documentDisplayOptions.firstOrNull { it.first == documentId }?.third
        ?: R.drawable.lib_ic_document
}
private fun loadDocumentPlacements(context: Context): Map<String, String> {
    val prefs = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
    if (!prefs.getBoolean(DOCUMENT_VISIBILITY_MIGRATION_KEY, false)) {
        // Migracja uzupełnia wyłącznie brakujące wpisy. Nie nadpisuje wyborów
        // użytkownika zapisanych we wcześniejszej wersji aplikacji.
        val editor = prefs.edit()
        documentDisplayOptions.forEach { (id, _, _) ->
            val key = DOCUMENT_DISPLAY_KEY_PREFIX + id
            if (!prefs.contains(key)) {
                val legacy = if (id == "loyalty") prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + "loyalty_cards", null) else null
                editor.putString(key, legacy ?: defaultDocumentPlacements[id] ?: "hidden")
            }
        }
        editor.putBoolean(DOCUMENT_VISIBILITY_MIGRATION_KEY, true)
        check(editor.commit()) { "Nie udało się zastosować domyślnej widoczności dokumentów." }
    }
    return documentDisplayOptions.associate { (id, _, _) ->
        val savedPlacement = prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + id, null)
            ?: if (id == "loyalty") prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + "loyalty_cards", null) else null
        id to (savedPlacement ?: defaultDocumentPlacements[id] ?: "hidden")
    }
}
private fun saveDocumentPlacements(context: Context, placements: Map<String, String>) {
    val editor = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE).edit()
    documentDisplayOptions.forEach { (id, _, _) ->
        val placement = placements[id] ?: "hidden"
        require(placement in setOf("dashboard", "other", "hidden")) { "Nieprawidłowe miejsce wyświetlania dokumentu: $id" }
        editor.putString(DOCUMENT_DISPLAY_KEY_PREFIX + id, placement)
    }
    check(editor.commit()) { "Nie udało się zapisać ustawień widoczności dokumentów." }
}

@Composable
private fun HomeScreen(onAppearanceChange: (ApplicationAppearance) -> Unit) {
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<Map<String, String>?>(null) }
    var importStatus by remember { mutableStateOf("") }
    var exportSnapshot by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    val exportVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/vcard")) { uri ->
        if (uri != null) {
            pendingExportUri = uri
            importStatus = "Wybrano miejsce zapisu. Potwierdź zapis archiwum; istniejący plik o tej nazwie może zostać zastąpiony."
        } else {
            importStatus = "Eksport anulowany — plik nie został zapisany."
        }
    }
    val importVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 256 * 1024 * 1024) { "Plik przekracza limit 10 MB." }
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
            }.getOrElse {
                String(bytes, java.nio.charset.Charset.forName("windows-1250"))
            }
            val imported = VCardCodec.decodeForImport(raw)
            require(imported.isNotEmpty()) { "Archiwum nie zawiera danych." }
            imported
        }.onSuccess {
            pendingImport = it
            importStatus = "Plik odczytany i poprawny. Sprawdź zakres danych przed zatwierdzeniem."
        }.onFailure {
            pendingImport = null
            importStatus = "Błąd importu: ${it.message ?: "nieprawidłowy plik"}"
        }
        else {
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
    var showApplicationAppearanceSettings by remember { mutableStateOf(false) }
    var showDocumentVisibilitySettings by remember { mutableStateOf(false) }
    var showDocumentBrandAssets by remember { mutableStateOf(false) }
    var documentBrandTarget by remember { mutableStateOf<String?>(null) }
    var showRegistryEditor by remember { mutableStateOf(false) }
    var settingsDocumentEditor by remember { mutableStateOf<String?>(null) }
    var graphicEditorReturnTarget by remember { mutableStateOf("graphic_catalog") }
    var credentialAppearanceTarget by remember { mutableStateOf<String?>(null) }
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    var showDisabilitySettings by remember { mutableStateOf(false) }
    var showCertificates by remember { mutableStateOf(false) }
    var showIdentityDesigner by remember { mutableStateOf(false) }
    var showCredentialAppearanceEditor by remember { mutableStateOf(false) }
    var showImportedDocuments by remember { mutableStateOf(false) }
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

    fun handleSystemBack() {
        when {
            credentialAppearanceTarget != null -> credentialAppearanceTarget = null
            showCredentialAppearanceEditor -> { showCredentialAppearanceEditor = false; showSettingsMenu = true }
            showApplicationAppearanceSettings -> { showApplicationAppearanceSettings = false; showSettingsMenu = true }
            showIdentityDesigner -> { showIdentityDesigner = false }
            showPublicServicesBrowser -> { showPublicServicesBrowser = false }
            showImportedDocuments -> showImportedDocuments = false
            showBankAccounts -> showBankAccounts = false
            showDocumentsEditor -> { showDocumentsEditor = false; showSettingsMenu = true }
            showRegistryEditor -> { showRegistryEditor = false; showSettingsMenu = true }
            settingsDocumentEditor != null -> { settingsDocumentEditor = null; showSettingsMenu = true }
            showDocumentVisibilitySettings -> { showDocumentVisibilitySettings = false; showSettingsMenu = true }
            documentBrandTarget != null -> { documentBrandTarget = null; showSettingsMenu = true }
            showDocumentBrandAssets -> { showDocumentBrandAssets = false; showSettingsMenu = true }
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
        enabled = credentialAppearanceTarget != null || showCredentialAppearanceEditor || showApplicationAppearanceSettings || showIdentityDesigner || showPublicServicesBrowser || showImportedDocuments || showBankAccounts || showDocumentsEditor || showSettingsMenu || showDocumentVisibilitySettings || showDocumentBrandAssets || documentBrandTarget != null || showRegistryEditor || settingsDocumentEditor != null || selectedDocumentId != null ||
            showDemoDocuments || showDisabilitySettings || showCertificates ||
            activeService != null || selectedTab != 0,
        onBack = ::handleSystemBack
    )

    fun openService(title: String) {
        when (title) {
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
            selectedDocumentId = documentId
            return
        }
        when (route.type) {
            DocumentRouteType.GROUP -> activeService = requireNotNull(route.destination)
            DocumentRouteType.DOCUMENT -> selectedDocumentId = route.documentId
            DocumentRouteType.SPECIALIZED -> when (route.destination) {
                "registry_extract" -> activeService = "Odpis z rejestru podmiotów"
                "children" -> activeService = "Dane dzieci"
                "bank_accounts" -> showBankAccounts = true
                "vaccines" -> activeService = "eKarta Szczepień"
                "signed_pdf" -> activeService = "Dokumenty PDF i podpisy elektroniczne"
                "imported" -> showImportedDocuments = true
                "branding" -> activeService = "Zasoby identyfikacji dokumentów"
                else -> selectedDocumentId = route.documentId
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
                    showBankAccounts -> FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, readOnly = true, onBack = { showBankAccounts = false })
                    showDocumentsEditor -> DemoDocumentsScreen(
                        categoryId = selectedEditorCategory,
                        onBack = { showDocumentsEditor = false; showSettingsMenu = true },
                        onOpenDocumentAppearance = { key -> credentialAppearanceTarget = key }
                    )
                    showRegistryEditor -> CredentialAppearanceEditorEntry("entity", onOpenAppearance = { credentialAppearanceTarget = it }, showOriginalAttachment = false) {
                        EntityRegistryExtractScreen(onBack = { showRegistryEditor = false; showSettingsMenu = true }, editable = true)
                    }
                    settingsDocumentEditor == "imported" -> CredentialAppearanceEditorEntry("imported", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        DocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    settingsDocumentEditor == "signed_pdf" -> CredentialAppearanceEditorEntry("signed_pdf", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        SignedPdfDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, allowImport = true, readOnly = false)
                    }
                    settingsDocumentEditor == "professional" -> CredentialAppearanceEditorEntry("professional", onOpenAppearance = { credentialAppearanceTarget = it }) {
                        ProfessionalQualificationsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    }
                    settingsDocumentEditor == "family" -> CredentialAppearanceEditorEntry("family", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = false, onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "business" -> CredentialAppearanceEditorEntry("business", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = true, onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "bank_accounts" -> CredentialAppearanceEditorEntry("bank_accounts", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "church" -> CredentialAppearanceEditorEntry("church", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        ChurchDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
                            onEditGraphic = { key -> credentialAppearanceTarget = key })
                    }
                    settingsDocumentEditor == "civil" -> CredentialAppearanceEditorEntry("civil", onOpenAppearance = { credentialAppearanceTarget = it }, showAppearanceAction = true, showOcrStage = true, showOriginalAttachment = true) {
                        OfficialDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true },
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
                    showSettingsMenu -> SettingsMenuScreen(
                        documentPlacements = documentPlacements,
                        onDocumentPlacementsChange = { updated ->
                            saveDocumentPlacements(context, updated)
                            documentPlacements = updated
                        },
                        onBack = { showSettingsMenu = false },
                        onManageApplicationAppearance = { showSettingsMenu = false; showApplicationAppearanceSettings = true },
                        onManageCredentialAppearance = { showSettingsMenu = false; showCredentialAppearanceEditor = true },
                        onManageDocumentVisibility = { openDocumentVisibilityCatalog() },
                        onManageDocumentBrandAssets = { showSettingsMenu = false; showDocumentBrandAssets = true },
                        onEditDocumentBranding = { target -> showSettingsMenu = false; documentBrandTarget = target },
                        onEditDocuments = { category -> selectedEditorCategory = category; showSettingsMenu = false; showDocumentsEditor = true },
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true },
                        onEditRegistry = { showSettingsMenu = false; showRegistryEditor = true },
                        onEditOtherDocuments = { key -> settingsDocumentEditor = key; showSettingsMenu = false },
                        onExportAll = {
                            val catalogManifest = org.json.JSONArray().apply {
                                documentDisplayOptions.distinctBy { it.first }.forEach { (id, title, _) ->
                                    put(org.json.JSONObject().apply {
                                        put("id", id)
                                        put("title", title)
                                        put("group", documentVisibilityGroup(id))
                                    })
                                }
                            }.toString()
                            val allCatalogPlacements = documentDisplayOptions.distinctBy { it.first }.associate { (id, _, _) ->
                                id to (loadDocumentPlacements(context)[id] ?: if (id == "identity") "dashboard" else "hidden")
                            }
                            exportSnapshot = DemoIdentityStore.load(context) +
                                LegitimacyPreviewSettingsStore.exportAll(context) +
                                allCatalogPlacements.mapKeys { "dp_${it.key}" } +
                                mapOf(
                                    "documentCatalogManifest" to catalogManifest,
                                    "documentCatalogGroupOrder" to org.json.JSONArray(documentVisibilityGroupOrder).toString(),
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
                            runCatching {
                                require(exportSnapshot.isNotEmpty()) {
                                    "Brak zapisanych danych. Najpierw uzupełnij i zapisz dane w ustawieniach."
                                }
                                VCardCodec.encodeArchive(exportSnapshot)
                            }.onSuccess { exportVCardLauncher.launch("obywatel-archiwum.vcf") }
                                .onFailure { importStatus = "Nie można przygotować eksportu: ${it.message}" }
                        },
                        onImportAll = { importVCardLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) },
                        importStatus = importStatus
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
                    showCertificates -> CertificatesScreen(onBack = { showCertificates = false })
                    activeService == "Informacje prawne" -> LegalInformationScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Odpis z rejestru podmiotów" -> EntityRegistryExtractScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Dokumenty Kościelne" -> ChurchDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Dokumenty kwalifikacji zawodowych" -> ProfessionalQualificationsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Pozostałe dokumenty" -> ServiceDetailScreen(title = activeService!!, onBack = { activeService = null }, onOpenDocuments = { activeService = null; showDemoDocuments = true })
                    activeService == "Dokumenty rejestru podmiotów" -> FamilyEntityDocumentsScreen(entityMode = true, readOnly = true, onBack = { activeService = null })
                    activeService == "Formularze dokumentów" -> OfficialDocumentsScreen(onBack = { activeService = null }, readOnly = true)
                    activeService == "Zasoby identyfikacji dokumentów" -> DocumentBrandAssetsScreen(onBack = { activeService = null }, readOnly = true)
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
                        onOpenCertificates = { showCertificates = true },
                        onOpenHelp = { activeService = "Pomoc i informacje" },
                        onOpenHistory = { activeService = "Historia działań" },
                        onOpenIdentityDesigner = { showIdentityDesigner = true },
                        onOpenImportedDocuments = { showImportedDocuments = true },
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
                    selectedTab = it
                    activeService = null
                    showDemoDocuments = false
                    selectedDocumentId = null
                    showDocumentsEditor = false
                    showSettingsMenu = false
                    showRegistryEditor = false
                    showDisabilitySettings = false
                    showCertificates = false
                    showBankAccounts = false
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
                    TextButton(onClick = {
                        runCatching {
                            require(exportSnapshot.isNotEmpty()) { "Brak danych do eksportu." }
                            val bytes = VCardCodec.encodeArchive(exportSnapshot).toByteArray(Charsets.UTF_8)
                            val output = context.contentResolver.openOutputStream(destinationUri, "wt")
                                ?: error("Android nie udostępnił strumienia zapisu dla wybranego pliku.")
                            output.use { it.write(bytes); it.flush() }
                            val savedBytes = context.contentResolver.openInputStream(destinationUri)?.use { it.readBytes() }
                                ?: error("Nie można ponownie odczytać zapisanego pliku vCard.")
                            require(savedBytes.isNotEmpty()) { "Zapisany plik vCard jest pusty." }
                            require(savedBytes.contentEquals(bytes)) {
                                "Weryfikacja zapisu nie powiodła się: zawartość pliku różni się od eksportowanych danych."
                            }
                            "Zapisano ${exportSnapshot.size} pól i katalog dokumentów (${bytes.size} bajtów) do pliku vCard."
                        }.onSuccess { importStatus = it }
                            .onFailure { importStatus = "Błąd eksportu: ${it.message ?: "nieznany błąd"}" }
                        pendingExportUri = null
                    }) { Text("Potwierdź zapis") }
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
                    val overwritten = incoming.keys.count { current.containsKey(it) }
                    val added = incoming.size - overwritten
                    Text(
                        "Plik zawiera ${incoming.size} pól. Nowe: ${added}; zastąpią istniejące: ${overwritten}. " +
                            "Dane zostaną połączone z profilem. Po zapisie aplikacja sprawdzi zgodność zapisanych wartości."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        runCatching {
                            val current = DemoIdentityStore.loadStrict(context)
                            val profileValues = incoming.filterKeys {
                                !it.startsWith("cs_") && !it.startsWith("dp_") &&
                                    it !in setOf(
                                        "documentCatalogManifest", "documentCatalogGroupOrder", "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive", "originalAttachmentArchive",
                                        "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
                                    )
                            }
                            val mergedProfile = current.toMutableMap().apply { putAll(profileValues) }
                            PersonalDataPropagation.apply(mergedProfile)
                            DemoIdentityStore.save(context, mergedProfile)
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
                                        require(id.isNotBlank() && title.isNotBlank() && group in documentVisibilityGroupOrder) {
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
                                .mapValues { (_, value) -> value }
                            if (importedPlacements.isNotEmpty()) {
                                val mergedPlacements = loadDocumentPlacements(context).toMutableMap().apply {
                                    putAll(importedPlacements.filterValues { it in setOf("dashboard", "other", "hidden") })
                                }
                                saveDocumentPlacements(context, mergedPlacements)
                                documentPlacements = mergedPlacements
                            }
                            val persisted = DemoIdentityStore.loadStrict(context)
                            require(profileValues.all { (key, value) -> persisted[key] == value }) {
                                "Weryfikacja zapisu nie powiodła się. Dane profilu nie odpowiadają importowi."
                            }
                            val auxiliaryCount = incoming.keys.count {
                                it.startsWith("cs_") || it.startsWith("dp_") ||
                                    it in setOf(
                                        "documentCatalogManifest", "documentCatalogGroupOrder", "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive", "originalAttachmentArchive",
                                        "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
                                    )
                            }
                            val changed = profileValues.keys.count { persisted[it] == profileValues[it] } + auxiliaryCount
                            require(changed == incoming.size) {
                                "Weryfikacja importu wykazała niezgodność części pól lub ustawień."
                            }
                            "Zaimportowano i zweryfikowano ${changed} pól oraz ustawień. Łącznie zapisano ${persisted.size} pól profilu."
                        }.onSuccess { importStatus = it }
                            .onFailure { importStatus = "Błąd importu lub weryfikacji: ${it.message ?: "nieznany błąd"}" }
                        pendingImport = null
                    }) { Text("Importuj i zweryfikuj") }
                },
                dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Anuluj") } }
            )
        }
    }
}

private fun documentIconFor(documentId: String): ImageVector = when (documentId) {
    "identity" -> Icons.Filled.CreditCard
    "passport" -> Icons.Filled.Public
    "registry", "parliament", "church", "business", "bank_accounts" -> Icons.Filled.AccountBalance
    "family" -> Icons.Filled.Fingerprint
    "training", "student" -> Icons.Filled.Badge
    "disability", "vaccines" -> Icons.Filled.HealthAndSafety
    "civil", "signed_pdf" -> Icons.Filled.Description
    "loyalty_cards" -> Icons.Filled.CreditCard
    "branding" -> Icons.Filled.Info
    else -> Icons.Filled.Description
}

private fun categoryIconFor(category: String): Int? = when (category) {
    "Zdrowie" -> R.drawable.mob_health
    "Rodzina" -> R.drawable.family
    "Edukacja" -> R.drawable.education
    "Praca" -> R.drawable.work
    "Finanse i podatki" -> R.drawable.mob_payments
    "Transport", "Kierowca i pojazdy", "Podróże" -> R.drawable.mob_transport
    "Nieruchomości" -> R.drawable.real_estate
    "Środowisko" -> R.drawable.mob_environment
    "Bezpieczeństwo" -> R.drawable.mob_cyber_safety
    "Prawo i obywatel", "Sprawy urzędowe" -> R.drawable.law_citizenship
    "Kultura i dziedzictwo" -> R.drawable.culture_heritage
    "Sport" -> R.drawable.sport
    "Organizacje społeczne" -> R.drawable.social_organizations
    "Kościół i wspólnoty" -> R.drawable.church_communities
    else -> null
}


@Composable
private fun QrToolsScreen() {
    var activeGenerator by remember { mutableStateOf("owd") }
    BackHandler(enabled = activeGenerator != "owd") { activeGenerator = "owd" }
    when (activeGenerator) {
        "vcard" -> Column(Modifier.fillMaxSize()) { QrSubscreenHeader("Wróć do generatorów") { activeGenerator = "owd" }; Box(Modifier.weight(1f)) { VCardQrScreen() } }
        "url" -> Column(Modifier.fillMaxSize()) { QrSubscreenHeader("Wróć do generatorów") { activeGenerator = "owd" }; Box(Modifier.weight(1f)) { WebsiteQrScreen() } }
        else -> OwdHubScreen(onOpenVCard = { activeGenerator = "vcard" }, onOpenWebsite = { activeGenerator = "url" })
    }
}

@Composable
private fun QrSubscreenHeader(label: String, onBack: () -> Unit) {
    Text("‹  $label", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 20.dp, vertical = 8.dp))
}

private const val QR_BRAND_NONE = "none"
private const val QR_BRAND_STATE = "state"
private const val QR_BRAND_FILE = "file"

@Composable
private fun QrBrandingControls(branding: String, onBrandingChange: (String) -> Unit, logoLoaded: Boolean, onChooseLogo: () -> Unit, onClearLogo: () -> Unit) {
    Text("Znak graficzny obok kodu QR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    listOf(QR_BRAND_STATE to "Godło państwowe", QR_BRAND_FILE to "Logo z pliku", QR_BRAND_NONE to "Bez znaku").forEach { (value, label) ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onBrandingChange(value) }) {
            RadioButton(selected = branding == value, onClick = { onBrandingChange(value) })
            Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (branding == QR_BRAND_FILE) {
        OutlinedButton(onClick = onChooseLogo, modifier = Modifier.fillMaxWidth()) { Text(if (logoLoaded) "Zmień załączone logo" else "Wybierz logo z pliku") }
        if (logoLoaded) OutlinedButton(onClick = onClearLogo, modifier = Modifier.fillMaxWidth()) { Text("Usuń załączone logo") }
        Text("Logo jest wyświetlane poza kodem QR. Żaden jego fragment nie zasłania modułów kodu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

private fun loadQrLogo(context: Context, uri: Uri): Bitmap {
    val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: error("Nie udało się odczytać obrazu.")
    require(source.width > 0 && source.height > 0) { "Nieprawidłowy plik obrazu." }
    val scale = minOf(1f, 1024f / maxOf(source.width, source.height))
    return if (scale < 1f) Bitmap.createScaledBitmap(source, (source.width * scale).toInt().coerceAtLeast(1), (source.height * scale).toInt().coerceAtLeast(1), true) else source
}

/**
 * Generuje kod QR z korekcją błędów H i umieszcza znak graficzny centralnie.
 * Logo jest ograniczone do 9% szerokości kodu, z białym polem ochronnym.
 * Mały rozmiar i korekcja H ograniczają ryzyko utraty czytelności; odczyt
 * należy dodatkowo sprawdzać dla każdego rodzaju ładunku i użytego logo.
 */
private fun generateBrandedQr(context: Context, payload: String, branding: String, uploadedLogo: Bitmap?): Bitmap {
    require(payload.isNotBlank()) { "Treść kodu QR jest pusta." }
    val logo = when (branding) {
        QR_BRAND_STATE -> requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.institutions_eagle_emblem)) {
            "Nie można wczytać godła."
        }
        QR_BRAND_FILE -> requireNotNull(uploadedLogo) { "Wybierz plik z logo." }
        else -> null
    }
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to 4,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 1000, 1000, hints)
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    if (logo != null) {
        val canvas = Canvas(bitmap)
        val side = matrix.width * 0.09f
        val padding = matrix.width * 0.018f
        val box = side + padding * 2f
        val left = (matrix.width - box) / 2f
        val top = (matrix.height - box) / 2f
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(left, top, left + box, top + box), box * 0.12f, box * 0.12f, backgroundPaint)
        val scale = side / maxOf(logo.width, logo.height).toFloat()
        val width = logo.width * scale
        val height = logo.height * scale
        val destination = RectF(
            (matrix.width - width) / 2f,
            (matrix.height - height) / 2f,
            (matrix.width + width) / 2f,
            (matrix.height + height) / 2f
        )
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(logo, null, destination, logoPaint)
    }
    return bitmap
}


private data class OwdDocumentGroup(
    val id: String,
    val title: String,
    val fields: List<Pair<String, String>>
)

private fun owdDocumentGroups(): List<OwdDocumentGroup> = listOf(
    OwdDocumentGroup("profile", "Profil kontaktowy i dane rodzinne", listOf(
        "Telefon" to "contactPhone", "E-mail" to "contactEmail", "Organizacja" to "contactOrganization",
        "Stanowisko" to "contactTitle", "Strona internetowa" to "contactUrl", "Adres kontaktowy" to "contactAddress",
        "Imiona ojca" to "fatherNames", "Nazwisko ojca" to "fatherSurname",
        "Imiona matki" to "motherNames", "Nazwisko rodowe matki" to "motherSurname"
    ) + listOf("personExtra" to "Informacje dodatkowe").flatMap { (prefix, _) ->
        listOf("Źródło danych" to "${prefix}Source", "Podstawa prawna" to "${prefix}LegalBasis",
            "Data weryfikacji" to "${prefix}VerifiedAt", "Podmiot weryfikujący" to "${prefix}VerifiedBy",
            "Uwagi" to "${prefix}Notes")
    }),
    OwdDocumentGroup("identity", "Dowód tożsamości", listOf(
        "Imiona" to "firstNames", "Nazwisko" to "lastName", "Data urodzenia" to "birthDate",
        "Miejsce urodzenia" to "birthPlace", "Obywatelstwo" to "citizenship", "Płeć" to "sex",
        "Nazwisko rodowe" to "birthSurname", "PESEL" to "pesel",
        "Numer dokumentu" to "idNumber", "Status" to "idStatus", "Poprzedni numer" to "idPreviousNumber",
        "Data wydania" to "idIssueDate", "Data ważności" to "idExpiryDate", "Organ wydający" to "idIssuer",
        "Data utraty / unieważnienia" to "idLossOrInvalidationDate"
    ) + listOf("Źródło danych" to "idExtraSource", "Podstawa prawna" to "idExtraLegalBasis",
        "Data weryfikacji" to "idExtraVerifiedAt", "Podmiot weryfikujący" to "idExtraVerifiedBy", "Uwagi" to "idExtraNotes")),
    OwdDocumentGroup("passport", "Dane paszportowe", listOf(
        "Numer paszportu" to "passportNumber", "Rodzaj dokumentu" to "passportType",
        "Obywatelstwo" to "passportCitizenship", "Status" to "passportStatus",
        "Państwo wydania" to "passportCountry", "Imiona i nazwisko" to "passportHolderNames",
        "Miejsce urodzenia" to "passportBirthPlace", "Płeć" to "passportSex",
        "Data wydania" to "passportIssueDate", "Data ważności" to "passportExpiryDate",
        "Organ wydający" to "passportIssuer", "Data utraty" to "passportLossDate"
    ) + listOf("Źródło danych" to "passportExtraSource", "Podstawa prawna" to "passportExtraLegalBasis",
        "Data weryfikacji" to "passportExtraVerifiedAt", "Podmiot weryfikujący" to "passportExtraVerifiedBy", "Uwagi" to "passportExtraNotes")),
    OwdDocumentGroup("family", "Dokument rodowy", listOf(
        "Numer dokumentu" to "familyDocNumber", "Nazwa rodu" to "familyName", "Linia / gałąź" to "familyLine",
        "Miejsce w strukturze rodu" to "familyRole", "Prowadzący ewidencję" to "familyRegistry",
        "Data sporządzenia" to "familyIssueDate", "Data aktualizacji" to "familyExpiryDate",
        "Miejsce ewidencji" to "familyRegistryPlace", "Miejsce zamieszkania" to "familyResidence",
        "Linia ojca" to "familyFatherLine", "Linia matki" to "familyMotherLine",
        "Podstawa wpisu" to "familySource", "Uwagi" to "familyNotes",
        "Podpis posiadacza" to "familyHolderSignature", "Podpis wystawcy" to "familyRegistrarSignature"
    ) + listOf("Źródło danych" to "familyExtraSource", "Podstawa prawna" to "familyExtraLegalBasis",
        "Data weryfikacji" to "familyExtraVerifiedAt", "Podmiot weryfikujący" to "familyExtraVerifiedBy", "Uwagi dodatkowe" to "familyExtraNotes")),
    OwdDocumentGroup("parliament", "Legitymacja poselska", listOf(
        "Numer legitymacji" to "mpCardNumber", "Kadencja" to "mpTerm", "Wystawca" to "mpIssuer",
        "Status" to "mpStatus", "Okręg wyborczy" to "mpDistrict", "Początek mandatu" to "mpStart",
        "Koniec mandatu" to "mpEnd", "Data wydania legitymacji" to "mpCardIssueDate",
        "Data ślubowania" to "mpOathDate", "Miejsce ślubowania" to "mpOathPlace",
        "Źródło potwierdzenia ślubowania" to "mpOathEvidence", "Źródło potwierdzenia mandatu" to "mpMandateSource"
    ) + listOf("Źródło danych" to "mpExtraSource", "Podstawa prawna" to "mpExtraLegalBasis",
        "Data weryfikacji" to "mpExtraVerifiedAt", "Podmiot weryfikujący" to "mpExtraVerifiedBy", "Uwagi" to "mpExtraNotes")),
    OwdDocumentGroup("training", "Legitymacja uczestnika kursu", listOf(
        "Instytucja" to "trainingInstitution", "Adres instytucji" to "trainingInstitutionAddress",
        "Numer rejestru instytucji" to "trainingInstitutionRegistry", "Numer legitymacji" to "trainingCardNumber",
        "Nazwa kursu" to "trainingCourseName", "Kod kursu" to "trainingCourseCode",
        "Data rozpoczęcia" to "trainingStartDate", "Data zakończenia" to "trainingEndDate",
        "Data wydania legitymacji" to "trainingIssueDate", "Ważna do" to "trainingValidUntil",
        "Status uczestnictwa" to "trainingStatus", "Wystawca" to "trainingIssuer",
        "Podpis wystawcy" to "trainingSignature", "Data ukończenia" to "trainingCompletionDate",
        "Liczba godzin" to "trainingHours", "Wynik" to "trainingAssessment",
        "Numer zaświadczenia" to "trainingCertificateNumber", "Forma kształcenia" to "trainingForm"
    ) + listOf("Źródło danych" to "trainingExtraSource", "Podstawa prawna" to "trainingExtraLegalBasis",
        "Data weryfikacji" to "trainingExtraVerifiedAt", "Podmiot weryfikujący" to "trainingExtraVerifiedBy", "Uwagi" to "trainingExtraNotes")),
    OwdDocumentGroup("student", "Legitymacja ucznia / studenta", listOf(
        "Numer legitymacji" to "studentCardNumber", "Uczelnia" to "studentUniversity",
        "Kierunek" to "studentProgramme", "Rok akademicki" to "studentAcademicYear",
        "Adres uczelni" to "studentUniversityAddress", "Identyfikator uczelni" to "studentUniversityId",
        "Imię i nazwisko" to "studentNames", "Wydział" to "studentFaculty",
        "Poziom studiów" to "studentLevel", "Profil / forma" to "studentProfile",
        "Rok i semestr" to "studentYear", "Data wydania" to "studentIssueDate",
        "Status" to "studentStatus", "Ważność" to "studentValidity", "Wystawca" to "studentIssuer"
    ) + listOf("Źródło danych" to "studentExtraSource", "Podstawa prawna" to "studentExtraLegalBasis",
        "Data weryfikacji" to "studentExtraVerifiedAt", "Podmiot weryfikujący" to "studentExtraVerifiedBy", "Uwagi" to "studentExtraNotes")),
    OwdDocumentGroup("disability", "Orzeczenia i zaświadczenia", listOf(
        "Numer dokumentu / sprawy" to "disabilityDecisionNumber", "Wystawca" to "disabilityAuthorityName",
        "Data dokumentu" to "disabilityDecisionDate", "Stopień / kategoria" to "disabilityDegree",
        "Okres obowiązywania" to "disabilityValidity", "Rodzaj rozstrzygnięcia" to "disabilityDecisionType",
        "Osoba wystawiająca" to "disabilityIssuerName", "Powiat / miasto" to "disabilityCounty",
        "Podstawa prawna" to "disabilityLegalBasis", "Przepis określający skutki" to "disabilityLegalProvision",
        "Zakres uznania" to "disabilityEquivalentEffect", "Podmiot prowadzący" to "disabilityFoundationName",
        "Podstawa statutowa" to "disabilityFoundationStatute", "Kwalifikacje wystawiającego" to "disabilityMedicalQualification",
        "Ocena medyczna" to "disabilityMedicalAssessment", "Skład zespołu" to "disabilityPanelMembers",
        "Przewodniczący" to "disabilityChairperson", "Osoba zatwierdzająca" to "disabilitySignature",
        "Tryb odwołania" to "disabilityAppealInfo", "Data wniosku" to "disabilityApplicationDate",
        "Numer sprawy" to "disabilityCaseNumber", "Data doręczenia" to "disabilityDeliveryDate",
        "Wynik odwołania" to "disabilityAppealOutcome", "Duplikat / zmiana" to "disabilityDuplicateInfo",
        "Dokument źródłowy" to "disabilitySourceAttachment"
    ) + listOf("Źródło danych" to "disabilityExtraSource", "Podstawa prawna" to "disabilityExtraLegalBasis",
        "Data weryfikacji" to "disabilityExtraVerifiedAt", "Podmiot weryfikujący" to "disabilityExtraVerifiedBy", "Uwagi" to "disabilityExtraNotes"))
)


@Composable
private fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val normalizedId = when (documentId) {
        "0" -> "profile"
        "1" -> "identity"
        "2" -> "passport"
        "3" -> "family"
        "4" -> "parliament"
        "5" -> "training"
        "6" -> "student"
        "7" -> "disability"
        "8", "loyalty_cards" -> "loyalty"
        "identity_card" -> "identity"
        else -> documentId
    }
    val allValues = remember { DemoIdentityStore.load(context) }
    val title = documentDisplayOptions.firstOrNull { it.first == normalizedId }?.second
        ?: when (normalizedId) {
            "loyalty" -> "Karty lojalnościowe"
            else -> normalizedId.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
    val settings = rememberLegitimacyPreviewSettings(normalizedId).value
    val previewValues = remember(allValues, normalizedId) {
        allValues.toMutableMap().apply {
            if (normalizedId == "disability" && this["firstNames"].isNullOrBlank()) {
                this["firstNames"] = this["disabilityPersonName"].orEmpty()
            }
        }
    }
    val options = credentialFieldOptions(normalizedId)
    val selectedOptions = if (!settings.visibleFieldsConfigured || settings.visibleFields.isEmpty()) options
        else options.filter { it.first in settings.visibleFields }
    val previewFields = selectedOptions.map { (key, label) ->
        Triple(key, label.uppercase(), previewValues[key].orEmpty().ifBlank { "—" })
    }
    val holder = when {
        previewValues["studentNames"].orEmpty().isNotBlank() -> previewValues["studentNames"].orEmpty()
        previewValues["passportHolderNames"].orEmpty().isNotBlank() -> previewValues["passportHolderNames"].orEmpty()
        previewValues["disabilityPersonName"].orEmpty().isNotBlank() -> previewValues["disabilityPersonName"].orEmpty()
        else -> listOf(previewValues["firstNames"].orEmpty(), previewValues["lastName"].orEmpty())
            .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "DANE POSIADACZA" }
    }
    val photoBitmap = remember(previewValues["photo"]) {
        runCatching {
            val encoded = previewValues["photo"].orEmpty()
            if (encoded.isBlank()) null else {
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.getOrNull()
    }
    val validity = credentialValidityForDocument(normalizedId, previewValues)
    val hasData = options.any { (key, _) -> !previewValues[key].isNullOrBlank() } ||
        (normalizedId == "disability" && !previewValues["disabilityPersonName"].isNullOrBlank())
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "‹  Wróć do dokumentów",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Podgląd dokumentu", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (settings.showCard) {
            UnifiedCredentialCard(
                documentKey = normalizedId,
                title = title.uppercase(),
                subtitle = "Podgląd danych zapisanych w aplikacji",
                holderName = holder,
                fields = previewFields,
                settings = settings,
                photo = photoBitmap,
                validity = validity
            )
        } else {
            Text("Podgląd karty jest wyłączony w ustawieniach wyglądu tego dokumentu.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        if (!hasData) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text("Brak zapisanych danych. Ten ekran prezentuje wyłącznie podgląd; edycja znajduje się w ustawieniach konkretnego dokumentu.",
                    Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun OwdHubScreen(onOpenVCard: () -> Unit, onOpenWebsite: () -> Unit) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    val shareable = fields.entries
        .filter { it.value.isNotBlank() }
        .sortedBy { it.key }
        .map { it.key to it.key.replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { ch -> ch.uppercase() } }
    val populated = shareable.filter { fields[it.first].orEmpty().isNotBlank() }
    val documentGroups = remember { owdDocumentGroups() }
    val populatedByDocument = remember(populated, documentGroups) {
        documentGroups.associate { group ->
            group.id to group.fields.map { it.second }.filter { key -> fields[key].orEmpty().isNotBlank() && populated.any { it.first == key } }.toSet()
        }
    }
    val allPopulatedKeys = populated.map { it.first }.toSet()
    var selectionMode by remember { mutableStateOf("all") }
    var selectedDocuments by remember { mutableStateOf(documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }.map { it.id }.toSet()) }
    var selectedDocumentId by remember { mutableStateOf(documentGroups.firstOrNull { populatedByDocument[it.id].orEmpty().isNotEmpty() }?.id ?: "profile") }
    var selectedDocumentFields by remember { mutableStateOf(populatedByDocument[selectedDocumentId].orEmpty()) }
    val selected = when (selectionMode) {
        "documents" -> selectedDocuments.flatMap { populatedByDocument[it].orEmpty() }.toSet()
        "document_fields" -> selectedDocumentFields.intersect(populatedByDocument[selectedDocumentId].orEmpty())
        else -> allPopulatedKeys
    }
    var consent by remember { mutableStateOf(false) }
    var purpose by remember { mutableStateOf("Przekazanie danych wskazanemu odbiorcy") }
    var offerQr by remember { mutableStateOf<Bitmap?>(null) }
    var offerQrRemainingSeconds by remember { mutableIntStateOf(0) }
    var sessionMessage by remember { mutableStateOf("Wybierz zakres danych i zapoznaj się z informacją o ich pochodzeniu.") }
    LaunchedEffect(offerQr) {
        if (offerQr == null) {
            offerQrRemainingSeconds = 0
        } else {
            offerQrRemainingSeconds = 180
            while (offerQrRemainingSeconds > 0) {
                delay(1_000)
                offerQrRemainingSeconds = (offerQrRemainingSeconds - 1).coerceAtLeast(0)
            }
            offerQr = null
            sessionMessage = "Oferta QR OWD wygasła po 180 sekundach. Utwórz nowy kod, aby rozpocząć kolejną sesję."
        }
    }
    var qrBranding by remember { mutableStateOf(QR_BRAND_NONE) }
    var qrUploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    val qrLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }.onSuccess { qrUploadedLogo = it; offerQr = null }.onFailure { offerQr = null }
    }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scanned = result.contents
        if (scanned != null) {
            sessionMessage = runCatching {
                val offer = OwdSecurity.validateOffer(scanned)
                "Rozpoznano ofertę OWD. Sesja: ${offer.getString("sessionId").take(12)}… Odbiorca musi jeszcze zaakceptować zakres i cel przed przyjęciem danych."
            }.getOrElse { "Kod QR nie jest prawidłową lub aktywną ofertą OWD: ${it.message ?: "błąd walidacji"}" }
        } else {
            sessionMessage = "Skanowanie anulowano."
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Obywatelska Wymiana Danych", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("OWD • niezależna wymiana danych między zgodnymi aplikacjami", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Wybór danych do udostępnienia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (populated.isEmpty()) {
            Text("Brak zapisanych danych do udostępnienia. Uzupełnij je w Ustawieniach.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Zakres udostępnienia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            listOf(
                "all" to "Wszystkie dane i dokumenty",
                "documents" to "Wybrane dokumenty",
                "document_fields" to "Dane z wybranego dokumentu"
            ).forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectionMode == mode, onClick = {
                        selectionMode = mode
                        offerQr = null
                        consent = false
                        if (mode == "document_fields") selectedDocumentFields = populatedByDocument[selectedDocumentId].orEmpty()
                    })
                    Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (selectionMode == mode) FontWeight.Bold else FontWeight.Medium)
                }
            }
            when (selectionMode) {
                "documents" -> {
                    Text("Zaznacz dokumenty, których dane mają wejść do zakresu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }.forEach { group ->
                        val keys = populatedByDocument[group.id].orEmpty()
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = group.id in selectedDocuments, onCheckedChange = { checked ->
                                selectedDocuments = if (checked) selectedDocuments + group.id else selectedDocuments - group.id
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(group.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text("${keys.size} dostępnych pól", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                "document_fields" -> {
                    Text("Wybierz jeden dokument, a następnie zaznacz konkretne pola.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    val availableGroups = documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }
                    availableGroups.forEach { group ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedDocumentId == group.id, onClick = {
                                selectedDocumentId = group.id
                                selectedDocumentFields = populatedByDocument[group.id].orEmpty()
                                offerQr = null
                                consent = false
                            })
                            Text(group.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (selectedDocumentId == group.id) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                    val currentGroup = documentGroups.firstOrNull { it.id == selectedDocumentId }
                    currentGroup?.fields?.filter { it.second in populatedByDocument[selectedDocumentId].orEmpty() }?.forEach { (label, key) ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = key in selectedDocumentFields, onCheckedChange = { checked ->
                                selectedDocumentFields = if (checked) selectedDocumentFields + key else selectedDocumentFields - key
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text(fields[key].orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Text("Wybrano ${selected.size} pól z ${allPopulatedKeys.size}. Zmiana zakresu unieważnia poprzednią zgodę i kod QR.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(value = purpose, onValueChange = { purpose = it; consent = false; offerQr = null },
            modifier = Modifier.fillMaxWidth(), label = { Text("Cel udostępnienia") }, minLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = consent, onCheckedChange = { consent = it; offerQr = null })
            Text("Potwierdzam świadomy wybór zakresu i celu udostępnienia danych.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
        }
        QrBrandingControls(
            branding = qrBranding,
            onBrandingChange = { qrBranding = it; offerQr = null },
            logoLoaded = qrUploadedLogo != null,
            onChooseLogo = { qrLogoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { qrUploadedLogo = null; offerQr = null }
        )
        Button(
            onClick = {
                runCatching {
                    require(selected.isNotEmpty()) { "Wybrany zakres nie zawiera żadnych uzupełnionych pól." }
                    require(purpose.isNotBlank()) { "Podaj cel udostępnienia." }
                    val (offer, _) = OwdSecurity.createOffer()
                    generateBrandedQr(context, offer.encodedQrPayload, qrBranding, qrUploadedLogo)
                }.onSuccess {
                    offerQr = it
                    sessionMessage = "Utworzono krótkotrwałą ofertę sesji. QR nie zawiera wybranych danych osobowych."
                }.onFailure { sessionMessage = it.message ?: "Nie udało się utworzyć sesji." }
            },
            enabled = consent && selected.isNotEmpty() && purpose.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Utwórz kod QR sesji OWD") }
        OutlinedButton(
            onClick = {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Zeskanuj kod sesji OWD drugiego urządzenia")
                    setBeepEnabled(false)
                    setOrientationLocked(false)
                }
                scanLauncher.launch(options)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Skanuj kod QR OWD") }
        Text(sessionMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        offerQr?.let { bitmap ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Oferta sesji OWD • pozostało ${offerQrRemainingSeconds / 60}:${(offerQrRemainingSeconds % 60).toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                    Image(bitmap.asImageBitmap(), contentDescription = "Kod QR oferty sesji OWD", modifier = Modifier.size(250.dp))
                     Text("To inicjuje sesję kryptograficzną. Skaner OWD potrafi rozpoznać i zweryfikować ofertę; wymiana kluczy, przesłanie rekordów i potwierdzenie odbioru wymagają jeszcze pełnego połączenia obu urządzeń.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        OutlinedButton(onClick = onOpenVCard, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator wizytówki vCard") }
        OutlinedButton(onClick = onOpenWebsite, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator kodu strony internetowej") }
        Text("Prywatność: dane nie są umieszczane w kodzie inicjującym. Odbiorca i cel muszą zostać zweryfikowani przed właściwym transferem. Dane rodzinne/rodowe są całkowicie wyłączone z obecnego zakresu OWD.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SaveQrImageButton(bitmap: Bitmap, fileName: String) {
    val context = LocalContext.current
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var saveMessage by remember { mutableStateOf("") }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        val image = pendingBitmap
        pendingBitmap = null
        if (uri == null) {
            saveMessage = "Zapis obrazu anulowano."
        } else if (image == null) {
            saveMessage = "Brak obrazu do zapisania. Wygeneruj kod ponownie."
        } else {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri, "wt")
                    ?: error("Android nie udostępnił strumienia zapisu.")
                output.use { stream ->
                    check(image.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Nie udało się zakodować obrazu PNG." }
                    stream.flush()
                }
                val saved = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    ?: error("Nie można ponownie odczytać zapisanego obrazu.")
                require(saved.width == image.width && saved.height == image.height) {
                    "Weryfikacja zapisu PNG nie powiodła się."
                }
                "Zapisano grafikę QR jako PNG (${saved.width} × ${saved.height} px)."
            }.onSuccess { saveMessage = it }
                .onFailure { saveMessage = "Błąd zapisu grafiki: ${it.message ?: "nieznany błąd"}" }
        }
    }
    OutlinedButton(
        onClick = {
            pendingBitmap = bitmap
            saveLauncher.launch(fileName)
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Zapisz grafikę QR (PNG)") }
    if (saveMessage.isNotBlank()) {
        Text(saveMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WebsiteQrScreen() {
    val context = LocalContext.current
    var address by remember { mutableStateOf("https://") }
    var branding by remember { mutableStateOf(QR_BRAND_NONE) }
    var uploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf("Wpisz pełny adres strony internetowej.") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }
            .onSuccess { uploadedLogo = it; message = "Wczytano logo z pliku." }
            .onFailure { message = "Błąd wczytywania logo: " + (it.message ?: "nieprawidłowy obraz") }
    }
    val normalized = address.trim()
    val validUrl = runCatching { val parsed = Uri.parse(normalized); (parsed.scheme == "https" || parsed.scheme == "http") && !parsed.host.isNullOrBlank() }.getOrDefault(false)
    val qrBitmap = remember(normalized, branding, uploadedLogo) {
        if (!validUrl || (branding == QR_BRAND_FILE && uploadedLogo == null)) null
        else runCatching { generateBrandedQr(context, normalized, branding, uploadedLogo) }.getOrNull()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kod QR strony internetowej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Kod zawiera adres strony. Zeskanowanie otworzy go w przeglądarce po zatwierdzeniu przez użytkownika.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Adres strony (URL)") }, placeholder = { Text("https://example.org") }, singleLine = true)
        QrBrandingControls(branding, { branding = it }, uploadedLogo != null, { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) }, { uploadedLogo = null; message = "Usunięto logo." })
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kod QR strony", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR strony internetowej", modifier = Modifier.size(260.dp), contentScale = ContentScale.Fit)
                     Text(normalized, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    SaveQrImageButton(qrBitmap, "adres-url-qr.png")
                }
            }
        } else Text(if (!validUrl) "Podaj poprawny adres rozpoczynający się od http:// lub https://." else "Wybierz logo z pliku albo ustaw opcję „Bez znaku”.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Godło lub logo jest wyświetlane obok kodu. Sam kod pozostaje czysty, bez nakładek. Przed publikacją sprawdź skanowanie na kilku urządzeniach.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
private fun VCardQrScreen() {
    val context = LocalContext.current
    val contactFields = remember {
        val profile = DemoIdentityStore.load(context)
        val firstNames = profile["firstNames"].orEmpty().trim()
        val lastName = profile["lastName"].orEmpty().trim()
        val displayName = listOf(firstNames, lastName).filter { it.isNotBlank() }.joinToString(" ")
        mutableStateMapOf(
            "FN" to displayName,
            "N" to listOf(lastName, firstNames, "", "", "").joinToString(";"),
            "ORG" to profile["contactOrganization"].orEmpty(),
            "TITLE" to profile["contactTitle"].orEmpty(),
            "TEL" to profile["contactPhone"].orEmpty(),
            "EMAIL" to profile["contactEmail"].orEmpty(),
            "URL" to profile["contactUrl"].orEmpty(),
            "ADR" to profile["contactAddress"].orEmpty()
        )
    }
    var importMessage by remember {
        val hasProfileName = contactFields["FN"].orEmpty().isNotBlank()
        mutableStateOf(
            if (hasProfileName) "Wstępnie uzupełniono dane profilu. Sprawdź i popraw pola przed utworzeniem kodu."
            else "Brak imienia i nazwiska w profilu. Uzupełnij dane ręcznie lub wczytaj plik .vcf."
        )
    }
    var branding by remember { mutableStateOf(QR_BRAND_NONE) }
    var uploadedLogo by remember { mutableStateOf<Bitmap?>(null) }
    var logoMessage by remember { mutableStateOf("Nie wybrano pliku logo.") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching { loadQrLogo(context, uri) }
            .onSuccess { uploadedLogo = it; logoMessage = "Wczytano logo z pliku." }
            .onFailure { logoMessage = "Błąd wczytywania logo: " + (it.message ?: "nieprawidłowy obraz") }
    }
    val vCardPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Nie udało się odczytać pliku.")
            val parsed = parseVCardContact(raw)
            parsed.forEach { (key, value) -> contactFields[key] = value }
            require(contactFields["FN"].orEmpty().isNotBlank() || contactFields["N"].orEmpty().isNotBlank()) {
                "Plik nie zawiera nazwy kontaktu (FN/N)."
            }
            importMessage = "Zaimportowano dane. Sprawdź ich zakres przed udostępnieniem."
        }.onFailure { importMessage = "Błąd importu: " + (it.message ?: "nieprawidłowy plik vCard") }
    }
    val contactSnapshot = contactFields.toMap()
    val displayName = contactSnapshot["FN"].orEmpty().trim()
    // encodeContact requires FN; do not invoke it while the form is still empty.
    // A validation error must never escape composition and terminate the activity.
    val payload = remember(contactSnapshot) {
        if (displayName.isBlank()) ""
        else runCatching { buildVCardPayload(contactSnapshot) }.getOrDefault("")
    }
    val qrBitmap = remember(payload, branding, uploadedLogo) {
        if (payload.isBlank() || (branding == QR_BRAND_FILE && uploadedLogo == null)) null
        else runCatching { generateBrandedQr(context, payload, branding, uploadedLogo) }.getOrNull()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Kod QR", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Generator kodu kontaktowego", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Utwórz kod QR zawierający wizytówkę vCard. Zgodna aplikacja odbiorcy może odczytać dane i zaproponować zapis kontaktu.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Wizytówka kontaktowa", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Kod zawiera dane kontaktowe wybrane przez użytkownika.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { vCardPicker.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Wczytaj plik vCard (.vcf)")
        }
        Text(importMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        QrBrandingControls(
            branding = branding,
            onBrandingChange = { branding = it },
            logoLoaded = uploadedLogo != null,
            onChooseLogo = { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { uploadedLogo = null; logoMessage = "Usunięto logo." }
        )
        Text(logoMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Dane kontaktowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Pola są domyślnie uzupełniane z profilu. Możesz je edytować przed wygenerowaniem wizytówki. Dane identyfikacyjne, takie jak PESEL i numery dokumentów, nie są kopiowane do kodu QR.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        val fields = listOf(
            "FN" to "Nazwa wyświetlana",
            "N" to "Nazwisko;Imię;Drugie imię;Prefiks;Sufiks",
            "ORG" to "Organizacja",
            "TITLE" to "Stanowisko",
            "TEL" to "Telefon",
            "EMAIL" to "Adres e-mail",
            "URL" to "Strona internetowa",
            "ADR" to "Ulica;Miasto;Region;Kod;Kraj"
        )
        fields.forEach { (key, label) ->
            OutlinedTextField(
                value = contactFields[key].orEmpty(),
                onValueChange = { contactFields[key] = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = key != "N" && key != "ADR"
            )
        }
        OutlinedButton(onClick = {
            contactFields.keys.toList().forEach { contactFields[it] = "" }
            importMessage = "Wyczyszczono formularz."
        }, modifier = Modifier.fillMaxWidth()) { Text("Wyczyść dane") }
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Twój kod QR", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR z wizytówką vCard", modifier = Modifier.size(250.dp), contentScale = ContentScale.Fit)
                    SaveQrImageButton(qrBitmap, "wizytowka-vcard-qr.png")
                  }
            }
        } else {
            val hint = when {
                displayName.isBlank() -> "Wpisz nazwę wyświetlaną (FN), aby wygenerować wizytówkę."
                branding == QR_BRAND_FILE && uploadedLogo == null -> "Wybierz logo z pliku albo ustaw opcję „Bez znaku”."
                else -> "Nie udało się przygotować kodu QR. Sprawdź dane kontaktowe i spróbuj ponownie."
            }
            Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text("Prywatność: kod ujawnia każdej osobie skanującej wszystkie zapisane w nim dane. Nie dodawaj numeru PESEL, danych dokumentu ani informacji poufnych.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

private fun parseVCardContact(raw: String): Map<String, String> {
    val unfolded = raw.replace("\r\n ", "").replace("\n ", "").replace("\r ", "")
    val result = mutableMapOf<String, String>()
    unfolded.lineSequence().forEach { line ->
        val colon = line.indexOf(':')
        if (colon <= 0) return@forEach
        val property = line.substring(0, colon).substringBefore(';').substringBefore('.').uppercase()
        val value = line.substring(colon + 1)
            .replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
        if (property in setOf("FN", "N", "ORG", "TITLE", "TEL", "EMAIL", "URL", "ADR") && value.isNotBlank()) {
            result[property] = if (result[property].isNullOrBlank()) value else result[property] + "; " + value
        }
    }
    return result
}

private fun buildVCardPayload(fields: Map<String, String>): String =
    VCardCodec.encodeContact(fields)

@Composable
private fun CivicIllustrationBanner(imageRes: Int, description: String) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = description,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun MoreMenuScreen(
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenIdentityDesigner: () -> Unit,
    onOpenImportedDocuments: () -> Unit,
    onOpenLegalInformation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Więcej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Informacje, bezpieczeństwo i ustawienia", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Historia działań", "Lista czynności w aplikacji", Icons.Filled.CalendarMonth, onOpenHistory)
        ProfileActionCard("Wydane certyfikaty", "Informacje o certyfikatach urządzenia", Icons.Filled.Security, onOpenCertificates)
        ProfileActionCard("Powiadomienia", "Ustawienia powiadomień", Icons.Filled.NotificationsNone, onOpenHelp)
        ProfileActionCard("Projektant dokumentów", "Tworzenie dokumentów, układ, podgląd i eksport PDF", Icons.Filled.Description, onOpenIdentityDesigner)
        ProfileActionCard("Pliki i załączniki", "Import i podgląd plików PDF, DOCX oraz obrazów", Icons.Filled.Description, onOpenImportedDocuments)
        ProfileActionCard("Pomoc i informacje", "Instrukcje korzystania z aplikacji", Icons.Filled.Info, onOpenHelp)
        ProfileActionCard("Ustawienia", "Dane, wygląd i preferencje", Icons.Filled.Settings, onOpenSettings)
        ProfileActionCard("Informacje prawne", "Podstawy prawne, prawa obywatelskie i zasady działania aplikacji", Icons.Filled.Description, onOpenLegalInformation)
    }
}

@Composable
private fun ProfileLandingScreen(
    onOpenDocuments: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit
) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Więcej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = R.drawable.header_logo_panel,
            description = "Panel znaku i identyfikacji aplikacji"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Użytkownik" },
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                    Text("Profil użytkownika", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileActionCard("Dokumenty i dane", "Przeglądaj dane i oświadczenia", Icons.Filled.CreditCard, onOpenDocuments)
        ProfileActionCard("Zaświadczenia", "Zapisane dane dokumentów i zaświadczeń", Icons.Filled.Description, onOpenCertificates)
        ProfileActionCard("Ustawienia", "Otwórz menu zarządzania danymi", Icons.Filled.Settings, onOpenSettings)
        Text(
            "Dane profilu możesz uzupełniać i aktualizować w ustawieniach.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ProfileActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.MoreHoriz, contentDescription = "Otwórz", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ServiceDetailScreen(title: String, onBack: () -> Unit, onOpenDocuments: () -> Unit) {
    val descriptions = mapOf(
        "Polak za granicą" to "Informacje dla obywateli przebywających poza granicami kraju.",
        "Płatności" to "Informacje o opłatach i usługach płatniczych.",
        "Zdrowie" to "Informacje zdrowotne i odnośniki do usług związanych ze zdrowiem.",
        "Transport" to "Informacje transportowe i dane związane z podróżą.",
        "Mandaty" to "Informacje dotyczące mandatów i dane związane ze sprawami kierowcy.",
        "Sprawdź PESEL" to "Informacje dotyczące numeru PESEL.",
        "Zastrzeż PESEL" to "Informacje dotyczące zastrzeżenia numeru PESEL.",
        "Bilety" to "Informacje i dane dotyczące biletów.",
        "Bezpieczeństwo" to "Ustawienia prywatności, wskazówki bezpieczeństwa i informacje o ochronie danych.",
        "Środowisko" to "Informacje środowiskowe i lokalne materiały edukacyjne.",
        "Pozostałe" to "Katalog dodatkowych modułów i informacji."
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do usług", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = when (title) {
                "Zdrowie" -> R.drawable.navigation_help
                "Polak za granicą", "Transport", "Bilety" -> R.drawable.navigation_mountain_landscape
                "Środowisko" -> R.drawable.navigation_services
                else -> R.drawable.navigation_profile
            },
            description = "Ilustracja tematyczna modułu $title"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(descriptions[title] ?: "Moduł informacyjny.", color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (title == "Bezpieczeństwo") {
            ProfileActionCard("Dane i dokumenty", "Otwórz formularz danych", Icons.Filled.Security, onOpenDocuments)
        }
        if (title == "Zdrowie") {
            ProfileActionCard("Dane o niepełnosprawności", "Otwórz ewidencję dokumentów i wskazań", Icons.Filled.HealthAndSafety, onOpenDocuments)
        }
        Text("Dostępne funkcje i zakres danych są opisane bezpośrednio w poszczególnych modułach.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Header(
    onOpenAssistant: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark()
        Text(
            "Dokumenty",
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onOpenAssistant) {
            Icon(painterResource(R.drawable.ic_header_robot_head), contentDescription = "Asystent",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onOpenMessages) {
            Icon(painterResource(R.drawable.ic_header_mail), contentDescription = "Wiadomości",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
        }
        Box(
            modifier = Modifier.size(36.dp).clickable(onClick = onOpenNotifications),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_header_bell), contentDescription = "Powiadomienia",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(29.dp))
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(10.dp)
                    .clip(CircleShape).background(Color(0xFFFF3028))
            )
        }
    }
}

@Composable
private fun BrandMark() {
    val context = LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Image(
        painter = painterResource(applicationDrawable(context, appearance.headerEmblem,
            R.drawable.file_00000000532082108d561dcd2385f8f9)),
        contentDescription = "Godło aplikacji",
        modifier = Modifier.size(48.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun IdentityDashboardCard(
    onOpen: () -> Unit,
    tileStyle: String = "large",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val cardHeight = when (tileStyle) {
        "small" -> 92.dp
        "list" -> 68.dp
        else -> 218.dp
    }
    Card(
        modifier = modifier.height(cardHeight)
            .clickable(onClick = onOpen)
            .testTag("identity-dashboard-card-$tileStyle"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF078FE9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9E8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            DocumentTileArtwork("identity", Modifier.fillMaxSize())
            Text(
                "mDowód",
                modifier = Modifier.align(Alignment.TopStart).padding(
                    start = if (tileStyle == "small") 10.dp else 20.dp,
                    top = if (tileStyle == "small") 10.dp else 18.dp
                ),
                color = Color.White,
                style = if (tileStyle == "small") MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Orzeł pozostaje czytelny; znacznik nachodzi na jego dolną prawą część przy nodze.
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(
                        top = if (tileStyle == "small") 8.dp else 12.dp,
                        end = if (tileStyle == "small") 8.dp else 12.dp
                    )
                    .size(if (tileStyle == "small") 22.dp else if (tileStyle == "list") 28.dp else 32.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .size(if (tileStyle == "small") 18.dp else if (tileStyle == "list") 24.dp else 28.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x557AA8C6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.a_clean_vector_style_emblem_on_a_transparent_backg),
                        contentDescription = "Godło Polski — czysty znak wektorowy na przezroczystym tle",
                        modifier = Modifier.size(if (tileStyle == "small") 13.dp else 20.dp),
                        contentScale = ContentScale.Fit
                    )
                }

            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text("ZOBACZ", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ServiceCard(
    item: ServiceItem,
    tileStyle: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val compact = tileStyle == "small"
    val list = tileStyle == "list"
    val cardModifier = when {
        list -> Modifier.fillMaxWidth().height(64.dp)
        compact -> Modifier.fillMaxWidth().height(76.dp)
        else -> Modifier.fillMaxWidth().height(92.dp)
    }
    Card(
        modifier = cardModifier.clickable(onClick = onClick)
            .testTag("service-card-${item.title}"),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxSize().padding(if (list) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(if (list || compact) 36.dp else 44.dp)
                    .clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(painter = painterResource(item.icon), contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                } else {
                    item.fallbackIcon?.let {
                        Icon(it, contentDescription = item.title, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp))
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(item.title, color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!list) {
                    Spacer(Modifier.height(3.dp))
                    Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(if (isFavorite) "★" else "☆",
                color = if (isFavorite) Color(0xFFB7791F) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable(onClick = onFavorite).padding(3.dp))
        }
    }
}



@Composable
private fun DocumentVisibilitySettingsScreen(
    documentPlacements: Map<String, String>,
    onDocumentPlacementsChange: (Map<String, String>) -> Unit,
    documentCardStyle: String,
    onDocumentCardStyleChange: (String) -> Unit,
    defaultDocumentIcon: String,
    onDefaultDocumentIconChange: (String) -> Unit,
    documentIconOverrides: Map<String, String>,
    onDocumentIconOverridesChange: (Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    var defaultIconMenu by remember { mutableStateOf(false) }
    var expandedDocumentIcon by remember { mutableStateOf<String?>(null) }
    var expandedVisibilityGroups by remember { mutableStateOf(emptySet<String>()) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Widoczność dokumentów", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Dla każdego dokumentu wybierz miejsce wyświetlania. Ukrycie dokumentu nie usuwa jego danych ani załączników.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text("Wygląd kafelków w oknie Dokumenty", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Wybierz sposób prezentacji dokumentów.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Domyślna ikona dokumentów", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Ustawienie obejmuje wszystkie dokumenty. Ikona indywidualna wybrana niżej ma pierwszeństwo.", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(onClick = { defaultIconMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (defaultDocumentIcon == "automatic") "Automatycznie — ikona przypisana do dokumentu"
                    else documentIconChoices.firstOrNull { it.first == defaultDocumentIcon }?.second ?: "Wybierz ikonę")
            }
            DropdownMenu(expanded = defaultIconMenu, onDismissRequest = { defaultIconMenu = false }) {
                DropdownMenuItem(text = { Text("Automatycznie — ikona przypisana do dokumentu") }, onClick = {
                    onDefaultDocumentIconChange("automatic"); defaultIconMenu = false
                })
                documentIconChoices.forEach { (resource, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = {
                        onDefaultDocumentIconChange(resource); defaultIconMenu = false
                    })
                }
            }
        }
        listOf("large" to "Duże kafelki", "small" to "Małe kafelki", "list" to "Kafelki listy").forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (documentCardStyle == value) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable { onDocumentCardStyleChange(value) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = documentCardStyle == value, onClick = { onDocumentCardStyleChange(value) })
                Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }
        val groupedVisibilityDocuments = documentDisplayOptions
            .distinctBy { it.first }
            .groupBy { (id, _, _) -> documentVisibilityGroup(id) }
        val visibilityGroupOrder = documentVisibilityGroupOrder
        visibilityGroupOrder.forEach { groupTitle ->
            val groupDocuments = groupedVisibilityDocuments[groupTitle].orEmpty()
            if (groupDocuments.isEmpty()) return@forEach
            val expanded = groupTitle in expandedVisibilityGroups
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .clickable {
                                expandedVisibilityGroups = if (expanded) expandedVisibilityGroups - groupTitle
                                else expandedVisibilityGroups + groupTitle
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(groupTitle, color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${groupDocuments.size} dokumentów", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium)
                        Text(if (expanded) "⌃" else "⌄", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge)
                    }
                    if (expanded) {
                        groupDocuments.forEach { (id, title, iconRes) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(resolveDocumentIconResource(
                                        LocalContext.current, id, defaultDocumentIcon, documentIconOverrides
                                    )),
                                    contentDescription = "Ikona: $title",
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Box {
                                    TextButton(
                                        onClick = { expandedDocumentIcon = if (expandedDocumentIcon == id) null else id },
                                        modifier = Modifier.testTag("document-icon-selector-$id")
                                    ) { Text("Ikona", style = MaterialTheme.typography.labelSmall) }
                                    DropdownMenu(
                                        expanded = expandedDocumentIcon == id,
                                        onDismissRequest = { expandedDocumentIcon = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Automatycznie — ikona przypisana do dokumentu") },
                                            onClick = {
                                                onDocumentIconOverridesChange(documentIconOverrides - id)
                                                expandedDocumentIcon = null
                                            }
                                        )
                                        documentIconChoices.forEach { (resource, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    onDocumentIconOverridesChange(documentIconOverrides + (id to resource))
                                                    expandedDocumentIcon = null
                                                }
                                            )
                                        }
                                    }
                                }
                                listOf("dashboard" to "Dokument", "other" to "Inny dokument", "hidden" to "Ukryj").forEach { (value, label) ->
                                    Row(
                                        modifier = Modifier.testTag("document-placement-$id-$value")
                                            .clickable { onDocumentPlacementsChange(documentPlacements + (id to value)) },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        RadioButton(
                                            selected = documentPlacements[id] == value,
                                            onClick = { onDocumentPlacementsChange(documentPlacements + (id to value)) },
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuScreen(
    documentPlacements: Map<String, String>,
    onManageApplicationAppearance: () -> Unit,
    onManageCredentialAppearance: () -> Unit,
    onDocumentPlacementsChange: (Map<String, String>) -> Unit,
    onBack: () -> Unit,
    onManageDocumentVisibility: () -> Unit,
    onManageDocumentBrandAssets: () -> Unit,
    onEditDocumentBranding: (String) -> Unit,
    onEditDocuments: (String) -> Unit,
    onEditDisability: () -> Unit,
    onEditRegistry: () -> Unit,
    onEditOtherDocuments: (String) -> Unit,
    onExportAll: () -> Unit,
    onImportAll: () -> Unit,
    importStatus: String
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ekranu głównego", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Ustawienia", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zarządzanie danymi", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Edycja tylko w Ustawieniach", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Portfel dokumentów i jego szczegóły są widokami do odczytu. Formularze edycji można otworzyć wyłącznie z tego menu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProfileActionCard(
            "Ustawienia aplikacji",
            "Motyw kolorystyczny, godło oraz tła ekranów uruchamiania i logowania",
            Icons.Filled.Settings,
            onManageApplicationAppearance
        )
        ProfileActionCard(
            "Wspólny wygląd legitymacji",
            "Jeden układ, rozmieszczenie, skala i typografia dla wszystkich legitymacji",
            Icons.Filled.Badge,
            onManageCredentialAppearance
        )
        ProfileActionCard(
            "Widoczność dokumentów",
            "Wybierz: Dokumenty, Inne dokumenty albo Ukryj",
            Icons.Filled.Security,
            onManageDocumentVisibility
        )
        ProfileActionCard(
            "Zasoby graficzne dokumentów",
            "Dodawanie i przypisywanie teł, herbów, godła, logo, flag oraz fotografii",
            Icons.Filled.Description,
            onManageDocumentBrandAssets
        )
        Text("Kopia danych vCard", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Eksport obejmuje pola profilu i ewidencji, w tym załączone dane tekstowe. Plik może zawierać dane wrażliwe — przechowuj go bezpiecznie.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Eksportuj wszystkie dane", "Zapisz jedno archiwum .vcf", Icons.Filled.Description, onExportAll)
        ProfileActionCard("Importuj dane z pliku vCard", "Wczytaj archiwum .vcf i zatwierdź zakres zmian", Icons.Filled.Description, onImportAll)
        if (importStatus.isNotBlank()) Text(importStatus, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        Text("Dokumenty — katalog wspólny", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Lista jest budowana z tego samego rejestru dokumentów co katalog i ustawienia widoczności. Zmiana grupy lub dodanie dokumentu aktualizuje wszystkie widoki.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        val settingsGroups = documentVisibilityGroupOrder
        settingsGroups.forEach { group ->
            val entries = documentDisplayOptions.distinctBy { it.first }
                .filter { documentVisibilityGroup(it.first) == group }
            if (entries.isNotEmpty()) {
                Text(group, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
                entries.forEach { (id, title, _) ->
                    val action: () -> Unit = {
                        when (id) {
                            "profile" -> onEditDocuments("profile")
                            "identity" -> onEditDocuments("identity_card")
                            "passport" -> onEditDocuments("passport")
                            "family" -> onEditOtherDocuments("family")
                            "parliament" -> onEditDocuments("parliament")
                            "training" -> onEditDocuments("training")
                            "student" -> onEditDocuments("student")
                            "disability" -> onEditDisability()
                            "church" -> onEditOtherDocuments("church")
                            "civil" -> onEditOtherDocuments("civil")
                            "business", "entity" -> onEditOtherDocuments("business")
                            "bank_accounts", "rod_bank_accounts", "entity_bank_accounts" -> onEditOtherDocuments("bank_accounts")
                            "vaccines" -> onEditOtherDocuments("vaccines")
                            "children" -> onEditOtherDocuments("children_health")
                            "signed_pdf" -> onEditOtherDocuments("signed_pdf")
                            "imported" -> onEditOtherDocuments("imported")
                            "professional" -> onEditOtherDocuments("professional")
                            "registry" -> onEditRegistry()
                            "loyalty" -> onEditDocuments("loyalty_cards")
                            else -> onEditOtherDocuments("graphic_catalog")
                        }
                    }
                    val icon = when (group) {
                        "Dokumenty kościelne" -> Icons.Filled.AccountBalance
                        "Dokumenty rodowe i rodzinne" -> Icons.Filled.Fingerprint
                        "Dokumenty firmowe i podmiotowe" -> Icons.Filled.Business
                        "Dokumenty zawodowe i edukacyjne" -> Icons.Filled.Badge
                        "Dokumenty zdrowotne i rodzinne" -> Icons.Filled.HealthAndSafety
                        "Pliki i pozostałe dokumenty" -> Icons.Filled.Description
                        "Dokumenty cywilne i urzędowe" -> Icons.Filled.Description
                        else -> Icons.Filled.CreditCard
                    }
                    ProfileActionCard(title, "Otwórz ustawienia danych i dokumentu", icon, action)
                }
            }
        }
        ProfileActionCard("Wszystkie dane", "Otwórz dotychczasowy formularz zbiorczy",
            Icons.Filled.Description, { onEditDocuments("all") })
        Text("Dane możesz uzupełniać własnym oświadczeniem, wskazując ich źródło, podstawę i zakres odpowiedzialności.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
                DemoDataField("disabilityAuthorityName", "Organ / podmiot / wystawca", fields)
                DemoDataField("disabilityIssuerName", "Osoba podpisująca / wystawca", fields)
                DemoDataField("disabilityLegalBasis", "Podstawa prawna lub regulacyjna", fields)
                DemoDataField("disabilityFoundationStatute", "Statut / uchwała fundacji (jeśli dotyczy)", fields)
                Text("Dane osoby i dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityPersonName", "Imię i nazwisko", fields)
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator", fields)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / zaświadczenia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                Text("Ustalony stopień i okres", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityCategory", "Kategoria", fields)
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki")
                DemoDataField("disabilityCauseCodes", "Symbole / przyczyny", fields)
                DemoDataField("disabilityValidity", "Okres obowiązywania", fields, "na stałe / do DD-MM-RRRR")
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
                DemoDataField("disabilityMedicalQualification", "Kwalifikacje osoby oceniającej", fields)
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
private fun DocumentsWalletScreen(
    onBack: () -> Unit,
    onAddDocuments: () -> Unit,
    documentPlacements: Map<String, String>,
    documentCardStyle: String,
    defaultDocumentIcon: String,
    documentIconOverrides: Map<String, String>,
    onOpenDetails: (String) -> Unit,
    onOpenService: (String) -> Unit
) {
    val context = LocalContext.current
    data class CatalogDocument(
        val id: String,
        val title: String,
        val subtitle: String,
        val categories: List<String>,
        val detailsIndex: String? = null,
        val service: String? = null,
        val icon: ImageVector = Icons.Filled.Description
    )

    val catalogGroupOrder = documentVisibilityGroupOrder + "Inne dokumenty"
    val documents = documentDisplayOptions.distinctBy { it.first }.map { (id, title, _) ->
        val group = documentVisibilityGroup(id)
        val service = when {
            id == "bank_accounts" || id == "rod_bank_accounts" || id == "entity_bank_accounts" -> "Konta bankowe"
            id == "imported" -> "Dokumenty własne — pliki"
            id == "signed_pdf" -> "Dokumenty PDF i podpisy elektroniczne"
            id == "branding" -> "Zasoby identyfikacji dokumentów"
            id == "children" -> "Dane dzieci"
            id == "vaccines" -> "eKarta Szczepień"
            id == "church" || id.startsWith("church_") || id == "organization_church" -> "Dokumenty Kościelne"
            id == "professional" || id.startsWith("professional_") -> "Dokumenty kwalifikacji zawodowych"
            id == "business" || id == "entity" || id.startsWith("organization_") || id.startsWith("entity_") -> "Dokumenty rejestru podmiotów"
            id == "civil" || id == "official" || id in setOf("birth_extract", "marriage_extract", "death_extract") -> "Formularze dokumentów"
            else -> "Pozostałe dokumenty"
        }
        val icon = when (group) {
            "Dokumenty kościelne" -> Icons.Filled.AccountBalance
            "Dokumenty rodowe i rodzinne" -> Icons.Filled.Fingerprint
            "Dokumenty firmowe i podmiotowe" -> Icons.Filled.Business
            "Dokumenty zawodowe i edukacyjne" -> Icons.Filled.Badge
            "Dokumenty zdrowotne i rodzinne" -> Icons.Filled.HealthAndSafety
            "Pliki i pozostałe dokumenty" -> Icons.Filled.Description
            "Dokumenty cywilne i urzędowe" -> Icons.Filled.Description
            else -> Icons.Filled.CreditCard
        }
        CatalogDocument(
            id = id,
            title = title,
            subtitle = "Dane dokumentu i ewidencja: $title",
            categories = listOf(group),
            service = service,
            icon = icon
        )
    }
    val allDocuments = documents
    val visibleDocuments = allDocuments.filter { documentPlacements[it.id] != "hidden" }
    val categories = catalogGroupOrder

    fun openDocument(document: CatalogDocument) {
        onOpenDetails(document.id)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Pulpit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Dokumenty", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Wybierz dokument. Grupy porządkują katalog, a każda pozycja otwiera bezpośrednio podgląd dokumentu.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
        )
        categories.forEach { category ->
            val entries = if (category == "Inne dokumenty") {
                visibleDocuments.filter { documentPlacements[it.id] == "other" }
            } else {
                visibleDocuments.filter { documentPlacements[it.id] == "dashboard" && category in it.categories }
            }
            if (entries.isNotEmpty()) {
                Text(category, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp))
                if (documentCardStyle == "list") {
                    entries.forEach { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { openDocument(document) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (documentPlacements[document.id] == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                DocumentTileArtwork(document.id, Modifier.matchParentSize())
                                Row(
                                    Modifier.fillMaxWidth().padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                                ) {
                                    Image(painter = painterResource(documentTileIconResource(context, document.id, resolveDocumentIconResource(context, document.id, defaultDocumentIcon, documentIconOverrides))),
                                        contentDescription = document.title, modifier = Modifier.size(30.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(document.title, color = Color.White, fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text(document.subtitle, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("Podgląd  ›", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                } else {
                    val columns = if (documentCardStyle == "small") 3 else 2
                    entries.chunked(columns).forEach { rowDocuments ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowDocuments.forEach { document ->
                                Card(
                                    modifier = Modifier.weight(1f)
                                        .height(if (documentCardStyle == "small") 112.dp else 148.dp)
                                        .clickable { openDocument(document) },
                                    shape = RoundedCornerShape(13.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, if (documentPlacements[document.id] == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Box(Modifier.fillMaxSize()) {
                                        DocumentTileArtwork(document.id, Modifier.fillMaxSize())
                                        Text(
                                            document.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            style = if (documentCardStyle == "small") MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.TopStart)
                                                .fillMaxWidth(0.78f).padding(10.dp)
                                        )
                                        if (documentCardStyle == "large") {
                                            Text(
                                                document.subtitle,
                                                color = Color.White.copy(alpha = 0.84f),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                                            )
                                        }
                                        Image(
                                            painter = painterResource(documentTileIconResource(context, document.id, resolveDocumentIconResource(context, document.id, defaultDocumentIcon, documentIconOverrides))),
                                            contentDescription = "Ikona wektorowa: " + document.title,
                                            modifier = Modifier.align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(if (documentCardStyle == "small") 22.dp else 26.dp)
                                        )
                                    }
                                }
                            }
                            repeat(columns - rowDocuments.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAddDocuments, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = painterResource(R.drawable.lib_ic_plus), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Dodaj dokument")
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
                                    Image(painterResource(documentTileIconResource(context, id, iconRes)),
                                        contentDescription = "Ikona wektorowa: " + title,
                                        modifier = Modifier.size(22.dp))
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                DocumentTileArtwork(id, Modifier.fillMaxSize())
                                Text(title, color = Color.White,
                                    style = if (tileStyle == "small") MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(0.78f).padding(10.dp))
                                Image(painterResource(documentTileIconResource(context, id, iconRes)),
                                    contentDescription = "Ikona wektorowa: " + title,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                        .size(if (tileStyle == "small") 20.dp else 24.dp))
                            }
                        }
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun documentTileIconResource(context: Context, documentKey: String, fallback: Int): Int {
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
private fun DocumentTileArtwork(documentKey: String, modifier: Modifier = Modifier) {
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

@Composable
private fun OwdDocumentArtworkCard(
    imageRes: Int,
    title: String,
    subtitle: String,
    holder: String,
    details: String,
    privateRecord: Boolean,
    credentialFields: List<Triple<String, String, String>> = emptyList(),
    validity: Boolean? = null
) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = when {
        title.contains("rodow", true) -> "family"
        title.contains("tożsamo", true) || title.contains("tozsamos", true) -> "identity"
        title.contains("paszport", true) -> "passport"
        title.contains("niepełnospraw", true) || title.contains("orzeczen", true) -> "disability"
        title.contains("kościel", true) || title.contains("kosciel", true) -> "church"
        title.contains("cywil", true) -> "civil"
        title.contains("podmiot", true) || title.contains("firm", true) -> "entity"
        else -> "official"
    }
    val settings = rememberLegitimacyPreviewSettings(documentKey).value
    val resolvedValidity = validity ?: credentialValidityForDocument(
        documentKey, credentialFields.associate { it.first to it.third }
    )
    if (settings.showCard) {
        UnifiedCredentialCard(
            documentKey = documentKey, title = title, subtitle = subtitle, holderName = holder,
            fields = credentialFields.ifEmpty { listOf(Triple("details", "DANE DOKUMENTU", details)) }, settings = settings,
            fallbackBackgroundRes = imageRes,
            validity = resolvedValidity
        )
    }
}


@Composable
private fun ReadOnlyAdditionalInformationSection(fields: Map<String, String>, prefix: String) {
    val rows = listOf(
        "Źródło danych / dokument źródłowy" to prefix + "Source",
        "Podstawa prawna / regulaminowa" to prefix + "LegalBasis",
        "Data ostatniej weryfikacji" to prefix + "VerifiedAt",
        "Podmiot / osoba potwierdzająca dane" to prefix + "VerifiedBy",
        "Adnotacje i informacje uzupełniające" to prefix + "Notes"
    )
    val populated = rows.filter { !fields[it.second].isNullOrBlank() }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5F8))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Źródła i informacje dodatkowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (populated.isEmpty()) {
                Text("Brak wpisów dotyczących źródła, podstawy, weryfikacji lub adnotacji.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                populated.forEach { (label, key) ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        Text(fields[key].orEmpty(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text("Oświadczenie użytkownika opisuje dane zgodnie z jego wiedzą oraz wskazaną podstawą i źródłem.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private enum class DocumentScreenMode { PREVIEW, EDITOR }
private val LocalDocumentScreenMode = androidx.compose.runtime.compositionLocalOf { DocumentScreenMode.PREVIEW }

private data class CredentialFieldVisibilityBinding(
    val documentKey: String,
    val settings: MutableState<LegitimacyPreviewSettings>,
    val fields: MutableMap<String, String>
)
private val LocalCredentialFieldVisibility = androidx.compose.runtime.compositionLocalOf<CredentialFieldVisibilityBinding?> { null }

@Composable
private fun DemoDocumentsScreen(
    categoryId: String = "all",
    onBack: () -> Unit,
    onOpenDocumentAppearance: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var photoBase64 by remember { mutableStateOf(fields["photo"] ?: "") }
    var selectedPhotoField by remember { mutableStateOf("photo") }
    var saveMessage by remember { mutableStateOf("") }
    val editorTitle = when (categoryId) {
        "profile" -> "Dane osobowe i zdjęcie profilowe"
        "family" -> "Dokument rodowy"
        "identity_card" -> "Dowód tożsamości"
        "parliament" -> "Legitymacja poselska"
        "training" -> "Legitymacja ucznia/studenta"
        "student" -> "Legitymacja ucznia/studenta"
        "disability" -> "Orzeczenia i zaświadczenia"
        "loyalty_cards" -> "Karty lojalnościowe"
        "passport" -> "Dane paszportowe"
        else -> "Wszystkie dane i dokumenty"
    }
    val credentialKey = when (categoryId) {
        "identity_card" -> "identity"
        "profile" -> "profile"
        "family" -> "family"
        "parliament" -> "parliament"
        "training" -> "training"
        "student" -> "student"
        "disability" -> "disability"
        "passport" -> "passport"
        "loyalty_cards" -> "loyalty_cards"
        else -> categoryId
    }
    val appearance = rememberLegitimacyPreviewSettings(credentialKey)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego zdjęcia")
            }.onSuccess { bytes ->
                if (bytes.size > 5 * 1024 * 1024) {
                    saveMessage = "Zdjęcie przekracza limit 5 MB. Wybierz mniejszy plik."
                } else {
                    val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    fields[selectedPhotoField] = encoded
                    if (selectedPhotoField == "photo") photoBase64 = encoded
                    saveMessage = "Zdjęcie zapisane w polu: $selectedPhotoField. Zapisz zmiany."
                }
            }.onFailure { saveMessage = "Nie udało się wczytać zdjęcia: ${it.localizedMessage ?: "błąd odczytu"}" }
        }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDocumentScreenMode provides DocumentScreenMode.EDITOR,
        LocalCredentialFieldVisibility provides if (categoryId == "all") null
            else CredentialFieldVisibilityBinding(credentialKey, appearance, fields)
    ) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "‹  Powrót do ustawień",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            editorTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (categoryId in setOf(
                "identity_card", "parliament", "training", "student", "disability", "passport",
                "family", "church", "civil", "business", "bank_accounts", "children_health",
                "vaccines", "professional", "imported", "signed_pdf", "entity"
            )) {
            GeneralDocumentOcrEditor(categoryId, fields, context) { saveMessage = it }
        }
         if (categoryId == "all" || categoryId == "profile") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zdjęcie profilowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F3F6))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBase64.isNotBlank()) {
                        val bytes = remember(photoBase64) { runCatching { Base64.decode(photoBase64, Base64.DEFAULT) }.getOrNull() }
                        val bitmap = remember(bytes) {
                            bytes?.let {
                                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeByteArray(it, 0, it.size, bounds)
                                var sample = 1
                                while (bounds.outWidth / sample > 900 || bounds.outHeight / sample > 900) sample *= 2
                                BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options().apply { inSampleSize = sample })
                            }
                        }
                        if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = "Zdjęcie profilowe", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text("Nie można wyświetlić zdjęcia", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(42.dp))
                            Text("MIEJSCE NA ZDJĘCIE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("Nie wybrano fotografii", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Wybierz zdjęcie", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { selectedPhotoField = "photo"; photoPicker.launch("image/*") }.padding(vertical = 8.dp))
                    if (photoBase64.isNotBlank()) Text("Usuń zdjęcie", color = Color(0xFF9B2C2C), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { photoBase64 = ""; fields["photo"] = "" }.padding(vertical = 8.dp))
                }
                Text("Pozostałe fotografie", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                listOf(
                    "photoAlternative1" to "Zdjęcie alternatywne 1",
                    "photoAlternative2" to "Zdjęcie alternatywne 2",
                    "wifePhoto" to "Zdjęcie żony",
                    "child1Photo" to "Zdjęcie dziecka 1",
                    "child2Photo" to "Zdjęcie dziecka 2",
                    "familyMember1Photo" to "Zdjęcie członka rodziny 1",
                    "familyMember2Photo" to "Zdjęcie członka rodziny 2"
                ).forEach { (photoKey, photoLabel) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(photoLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { selectedPhotoField = photoKey; photoPicker.launch("image/*") }) {
                            Text(if (fields[photoKey].isNullOrBlank()) "Dodaj" else "Zmień")
                        }
                        if (!fields[photoKey].isNullOrBlank()) {
                            TextButton(onClick = { fields[photoKey] = "" }) { Text("Usuń") }
                        }
                    }
                }
                Text("Dane osobowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("contactPhone", "Telefon kontaktowy", fields)
                DemoDataField("contactEmail", "Adres poczty elektronicznej", fields)
                DemoDataField("contactOrganization", "Organizacja", fields)
                DemoDataField("contactTitle", "Stanowisko", fields)
                DemoDataField("contactUrl", "Strona internetowa", fields)
                DemoDataField("contactAddress", "Adres kontaktowy", fields)
                DemoDataField("fatherNames", "Imiona ojca", fields)
                DemoDataField("fatherSurname", "Nazwisko ojca", fields)
                DemoDataField("motherNames", "Imiona matki", fields)
                DemoDataField("motherSurname", "Nazwisko rodowe matki", fields)
                AdditionalInformationSection("Informacje dodatkowe — dane osobowe", fields, "personExtra", "Uzupełnij dane zgodnie z aktualnym dokumentem źródłowym.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "family") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rodowy dokument tożsamości — dane dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("familyDocNumber", "Numer rodowego dokumentu", fields)
                DemoDataField("familyName", "Nazwa rodu", fields)
                DemoDataField("familyLine", "Linia / gałąź rodu", fields)
                DemoDataField("familyRole", "Miejsce w strukturze rodu", fields)
                DemoDataField("familyRegistry", "Osoba lub organ prowadzący ewidencję", fields)
                DemoDataField("familyIssueDate", "Data sporządzenia", fields, "DD-MM-RRRR")
                DemoDataField("familyExpiryDate", "Data przeglądu / aktualizacji wpisu", fields, "DD-MM-RRRR")
                DemoDataField("familySource", "Podstawa wpisu / dokument źródłowy", fields)
                DemoDataField("familyRegistryPlace", "Miejsce prowadzenia ewidencji", fields)
                DemoDataField("familyResidence", "Miejsce zamieszkania (wpis dobrowolny)", fields)
                DemoDataField("familyFatherLine", "Linia rodowa ojca", fields)
                DemoDataField("familyMotherLine", "Linia rodowa matki", fields)
                DemoDataField("familyNotes", "Uwagi ewidencyjne", fields)
                DemoDataField("familyHolderSignature", "Imię i nazwisko posiadacza / podpis", fields)
                DemoDataField("familyRegistrarSignature", "Imię i nazwisko wystawcy / podpis", fields)
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.documents_family_record,
                    title = "Dokument rodowy",
                    subtitle = "Ewidencja rodowa",
                    holder = listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Dane posiadacza" },
                    details = "Nr: ${fields["familyDocNumber"].orEmpty().ifBlank { "—" }} • Ród: ${fields["familyName"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("familyName", "NAZWA RODU", fields["familyName"].orEmpty()),
                        Triple("familyRole", "MIEJSCE W RODZIE", fields["familyRole"].orEmpty()),
                        Triple("familyDocNumber", "NUMER DOKUMENTU", fields["familyDocNumber"].orEmpty()),
                        Triple("familyIssueDate", "DATA SPORZĄDZENIA", fields["familyIssueDate"].orEmpty()),
                        Triple("familyExpiryDate", "DATA AKTUALIZACJI", fields["familyExpiryDate"].orEmpty())
                    )
                )
            }
        }
        }
        if (categoryId == "all" || categoryId == "identity_card") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Podstawowe dane identyfikacyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("birthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("birthPlace", "Miejsce urodzenia", fields)
                DemoDataField("citizenship", "Obywatelstwo", fields)
                DemoDataField("sex", "Płeć (dane deklarowane)", fields)
                DemoDataField("birthSurname", "Nazwisko rodowe", fields)
                DemoDataField("pesel", "Numer PESEL", fields)
                Text("Dane samego dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("idNumber", "Numer dokumentu tożsamości", fields)
                DemoDataField("idIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("idExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("idIssuer", "Organ wydający", fields)
                DemoDataField("idStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("idPreviousNumber", "Numer poprzedniego dokumentu (jeśli dotyczy)", fields)
                DemoDataField("idLossOrInvalidationDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dane identyfikacyjne i dokumentowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Uzupełnij dane zgodnie z dokumentem źródłowym, w tym numer, organ wydający i terminy ważności.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.karta_rodowa_jasna,
                    title = "Karta danych tożsamości",
                    subtitle = "Dane identyfikacyjne",
                    holder = listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Dane użytkownika" },
                    details = "Nr: ${fields["idNumber"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("firstNames", "IMIONA", fields["firstNames"].orEmpty()),
                        Triple("lastName", "NAZWISKO", fields["lastName"].orEmpty()),
                        Triple("birthDate", "DATA URODZENIA", fields["birthDate"].orEmpty()),
                        Triple("pesel", "PESEL", fields["pesel"].orEmpty()),
                        Triple("idNumber", "NUMER DOKUMENTU", fields["idNumber"].orEmpty()),
                        Triple("idIssueDate", "DATA WYDANIA", fields["idIssueDate"].orEmpty()),
                        Triple("idExpiryDate", "DATA WAŻNOŚCI", fields["idExpiryDate"].orEmpty())
                    ),
                    validity = when (fields["idStatus"].orEmpty().trim().lowercase()) {
                        "nieważny", "nieważna", "unieważniony", "unieważniona", "utracony", "utracona", "expired", "invalid" -> false
                        "ważny", "ważna", "aktywny", "aktywna", "valid" -> true
                        else -> fields["idExpiryDate"].orEmpty().takeIf { it.isNotBlank() }
                            ?.let(::isCredentialValidByExpiry)
                    }
                )
                AdditionalInformationSection("Źródła i weryfikacja danych identyfikacyjnych", fields, "idExtra", "Wskaż dokument źródłowy i datę jego sprawdzenia. Sam wpis użytkownika nie stanowi potwierdzenia urzędowego.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "parliament") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja poselska — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("mpCardNumber", "Numer ewidencyjny legitymacji", fields)
                DemoDataField("mpTerm", "Kadencja", fields)
                DemoDataField("mpDistrict", "Okręg wyborczy", fields)
                DemoDataField("mpStart", "Data rozpoczęcia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpEnd", "Data zakończenia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpCardIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("mpIssuer", "Organ / osoba wystawiająca", fields)
                DemoDataField("mpStatus", "Status legitymacji", fields, "aktywna / zwrócona / unieważniona")
                DemoDataField("mpMandateSource", "Źródło potwierdzenia mandatu", fields)
                Text("Dane dotyczące ślubowania — oświadczenie własne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("mpOathDate", "Data złożenia ślubowania", fields, "DD-MM-RRRR")
                DemoDataField("mpOathPlace", "Miejsce / okoliczności wypowiedzenia ślubowania", fields)
                DemoDataField("mpOathEvidence", "Źródło potwierdzenia (np. protokół, nagranie, publikacja)", fields)
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd cyfrowej legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    ParliamentaryCredentialPreview(fields)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja poselska", fields, "mpExtra", "Art. 104 Konstytucji RP określa wolny mandat poselski oraz obowiązek złożenia ślubowania przed Sejmem przed rozpoczęciem sprawowania mandatu. Odmowa ślubowania oznacza zrzeczenie się mandatu. Sam wpis ewidencyjny nie potwierdza uzyskania mandatu ani prawa do reprezentowania Sejmu.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "training") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja uczestnika kursu — instytucja szkoleniowa", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("trainingInstitution", "Nazwa instytucji szkoleniowej", fields)
                DemoDataField("trainingInstitutionAddress", "Adres instytucji", fields)
                DemoDataField("trainingInstitutionRegistry", "Numer wpisu / rejestru instytucji (jeśli dotyczy)", fields)
                DemoDataField("trainingCardNumber", "Numer legitymacji uczestnika", fields)
                DemoDataField("trainingCourseName", "Nazwa kursu / kierunek kształcenia", fields)
                DemoDataField("trainingCourseCode", "Symbol lub numer kursu", fields)
                DemoDataField("trainingStartDate", "Data rozpoczęcia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingEndDate", "Planowana data zakończenia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("trainingValidUntil", "Ważna do", fields, "DD-MM-RRRR")
                DemoDataField("trainingStatus", "Status uczestnictwa", fields, "np. aktywny / ukończony / zawieszony")
                DemoDataField("trainingIssuer", "Osoba upoważniona do wystawienia", fields)
                DemoDataField("trainingSignature", "Imię i nazwisko / podpis wystawcy", fields)
                DemoDataField("trainingCompletionDate", "Rzeczywista data ukończenia kursu", fields, "DD-MM-RRRR")
                DemoDataField("trainingHours", "Liczba godzin kształcenia", fields)
                DemoDataField("trainingAssessment", "Zaliczenie / egzamin / wynik", fields)
                DemoDataField("trainingCertificateNumber", "Numer zaświadczenia o ukończeniu", fields)
                DemoDataField("trainingForm", "Forma kształcenia", fields, "stacjonarna / zdalna / hybrydowa")
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    SchoolLegitimacyPreview(fields, photoBase64, student = false)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "trainingExtra", "Legitymacja instytucji szkoleniowej potwierdza status w ewidencji wystawcy w granicach regulaminu i właściwej podstawy działania. Nie jest legitymacją studencką i sama nie nadaje statusu studenta uczelni.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "student") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja ucznia/studenta — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("studentUniversity", "Nazwa uczelni", fields)
                DemoDataField("studentUniversityAddress", "Adres uczelni", fields)
                DemoDataField("studentUniversityId", "Identyfikator uczelni / jednostki", fields)
                DemoDataField("studentCardNumber", "Numer legitymacji / numer albumu", fields)
                DemoDataField("studentNames", "Imię i nazwisko studenta zapisane w dokumencie", fields)
                DemoDataField("studentFaculty", "Wydział / jednostka organizacyjna", fields)
                DemoDataField("studentProgramme", "Kierunek studiów", fields)
                DemoDataField("studentLevel", "Poziom studiów", fields, "I stopnia / II stopnia / jednolite magisterskie")
                DemoDataField("studentProfile", "Profil / forma studiów", fields, "ogólnoakademicki / praktyczny; stacjonarne / niestacjonarne")
                DemoDataField("studentYear", "Rok i semestr studiów", fields)
                DemoDataField("studentAcademicYear", "Rok akademicki", fields)
                DemoDataField("studentIssueDate", "Data wydania dokumentu", fields, "DD-MM-RRRR")
                DemoDataField("studentValidity", "Termin ważności / aktualizacji", fields, "DD-MM-RRRR")
                DemoDataField("studentStatus", "Status studenta", fields, "aktywny / urlop / skreślony / ukończony")
                DemoDataField("studentIssuer", "Osoba / jednostka wydająca", fields)
                Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd legitymacji", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    SchoolLegitimacyPreview(fields, photoBase64, student = true)
                }
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "studentExtra", "")
            }
        }
        }
        if (categoryId == "loyalty_cards") {
            LoyaltyCardsEditor(fields = fields, context = context, saveMessage = { saveMessage = it })
        }
        if (categoryId == "all" || categoryId == "disability") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Orzeczenie o niepełnosprawności — dane ewidencyjne", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Źródło ustalenia / dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                val authorityOptions = listOf(
                    "powiat" to "Zespół powiatowy / miejski",
                    "other_law" to "Orzeczenie na podstawie innych przepisów",
                    "foundation" to "Zespół medyczny fundacji",
                    "medical_opinion" to "Opinia lub zaświadczenie medyczne",
                    "self_declaration" to "Własne oświadczenie"
                )
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    authorityOptions.forEach { (value, label) ->
                        val selected = (fields["disabilityAuthorityType"] ?: "powiat") == value
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
                                .clickable { fields["disabilityAuthorityType"] = value }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(label, modifier = Modifier.padding(start = 9.dp), color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                val authorityType = fields["disabilityAuthorityType"] ?: "powiat"
                if (authorityType != "self_declaration") {
                    DemoDataField("disabilityAuthorityName", "Nazwa organu / podmiotu / wystawcy", fields)
                    DemoDataField("disabilityIssuerName", "Imię, nazwisko lub nazwa wystawcy", fields)
                }
                when (authorityType) {
                    "powiat" -> {
                        DemoDataField("disabilityCounty", "Powiat / miasto na prawach powiatu", fields)
                        DemoDataField("disabilityLegalBasis", "Podstawa prawna", fields, "Ustawa z 27.08.1997 r. i przepisy wykonawcze")
                    }
                    "other_law" -> {
                        DemoDataField("disabilityLegalBasis", "Odrębna podstawa prawna", fields)
                        DemoDataField("disabilityLegalProvision", "Przepis określający skutki lub równoważność", fields)
                        DemoDataField("disabilityEquivalentEffect", "Zakres uznania / równoważności", fields)
                    }
                    "foundation" -> {
                        DemoDataField("disabilityFoundationName", "Fundacja / podmiot prowadzący", fields)
                        DemoDataField("disabilityFoundationStatute", "Podstawa statutowa / uchwała fundacji", fields)
                        DemoDataField("disabilityFoundationLegalBasis", "Podstawa prawna i regulacyjna wskazana przez wystawcę", fields)
                    }
                    "medical_opinion" -> {
                        DemoDataField("disabilityMedicalQualification", "Zawód / kwalifikacje osoby wystawiającej", fields)
                        DemoDataField("disabilityLegalBasis", "Podstawa i cel wydania opinii / zaświadczenia", fields)
                    }
                    "self_declaration" -> {
                        DemoDataField("disabilitySelfDeclarationDate", "Data złożenia oświadczenia", fields, "DD-MM-RRRR")
                        DemoDataField("disabilitySelfDeclarationBasis", "Podstawa i cel oświadczenia", fields)
                        Text("Oświadczenie własne zapisuje deklarację osoby. Samo w sobie nie jest orzeczeniem ustawowym; jego znaczenie zależy od przepisu lub procedury, w której jest składane.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Dane osoby i rozstrzygnięcia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / rozstrzygnięcia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                DemoDataField("disabilityPersonName", "Osoba, której dotyczy orzeczenie", fields, "Imię i nazwisko")
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator ewidencyjny", fields)
                Text("Ustalony status / stopień", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityCategory", "Kategoria", fields, "np. osoba do 16 lat / osoba powyżej 16 lat")
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki / nie dotyczy")
                DemoDataField("disabilityCauseCodes", "Symbole przyczyn (maks. 3 w orzeczeniu powiatowym)", fields, "np. 05-R, 10-N")
                DemoDataField("disabilityValidity", "Okres, na jaki ustalono status", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data, od której ustalono niepełnosprawność", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityEmployment", "Wskazania dotyczące zatrudnienia", fields)
                DemoDataField("disabilityTraining", "Wskazania dotyczące szkolenia", fields)
                DemoDataField("disabilityTherapy", "Terapia zajęciowa / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Przedmioty ortopedyczne / środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Środowiskowy system wsparcia", fields)
                DemoDataField("disabilityParking", "Wskazania dotyczące karty parkingowej", fields)
                Text("Skład i podpisy", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityMedicalAssessment", "Lekarz / osoba dokonująca oceny", fields)
                DemoDataField("disabilityPanelMembers", "Skład zespołu / osoby uczestniczące w ocenie", fields)
                DemoDataField("disabilityChairperson", "Przewodniczący zespołu", fields)
                DemoDataField("disabilitySignature", "Osoba podpisująca / zatwierdzająca", fields)
                DemoDataField("disabilityAppealInfo", "Tryb odwołania / weryfikacji", fields)
                DemoDataField("disabilityApplicationDate", "Data złożenia wniosku", fields, "DD-MM-RRRR")
                DemoDataField("disabilityCaseNumber", "Numer sprawy", fields)
                DemoDataField("disabilityDeliveryDate", "Data doręczenia rozstrzygnięcia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionType", "Rodzaj rozstrzygnięcia", fields)
                DemoDataField("disabilityAppealOutcome", "Wynik odwołania / sprzeciwu", fields)
                DemoDataField("disabilityDuplicateInfo", "Duplikat / zmiana dokumentu", fields)
                DemoDataField("disabilitySourceAttachment", "Nazwa / opis dokumentu źródłowego", fields)
                if (LocalDocumentScreenMode.current == DocumentScreenMode.PREVIEW) {
                    Text("Podgląd dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    DisabilityDecisionPreview(fields)
                }
                AdditionalInformationSection("Informacje dodatkowe — orzeczenie / zaświadczenie", fields, "disabilityExtra", when (fields["disabilityAuthorityType"] ?: "powiat") {
                    "foundation" -> "Dokument fundacji opisuje się zgodnie z rzeczywistą podstawą działania, kwalifikacjami wystawców i zakresem przeznaczenia. Nie przypisujemy mu automatycznie skutków orzeczenia ustawowego."
                    "other_law" -> "Skutki dokumentu z odrębnego systemu wynikają z konkretnego przepisu. Należy wskazać podstawę i zakres równoważności."
                    "medical_opinion" -> "Opinia lub zaświadczenie medyczne dokumentuje ocenę albo stan zdrowia w zakresie wskazanym przez wystawcę."
                    "self_declaration" -> "Oświadczenie własne jest deklaracją osoby. Jego znaczenie zależy od procedury, w której jest składane; samo nie ustanawia ustawowego stopnia niepełnosprawności."
                    else -> "Karta ewidencyjna porządkuje dane wpisane przez użytkownika."
                })
            }
        }
        }
        if (categoryId == "all" || categoryId == "passport") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dane paszportowe", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                DemoDataField("passportNumber", "Numer dokumentu paszportowego", fields)
                DemoDataField("passportCountry", "Państwo wydania", fields)
                DemoDataField("passportIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("passportExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("passportIssuer", "Organ wydający", fields)
                DemoDataField("passportType", "Rodzaj dokumentu podróży", fields, "zwykły / tymczasowy / inny")
                DemoDataField("passportHolderNames", "Imiona i nazwisko zapisane w paszporcie", fields)
                DemoDataField("passportBirthPlace", "Miejsce urodzenia", fields)
                DemoDataField("passportCitizenship", "Obywatelstwo w dokumencie", fields)
                DemoDataField("passportSex", "Płeć w dokumencie", fields)
                DemoDataField("passportStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("passportLossDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                OwdDocumentArtworkCard(
                    imageRes = R.drawable.paszport_rodowy_jasny,
                    title = "Dane paszportowe",
                    subtitle = "Dane dokumentu podróży",
                    holder = fields["passportHolderNames"].orEmpty().ifBlank { "Dane posiadacza" },
                    details = "Nr paszportu: ${fields["passportNumber"].orEmpty().ifBlank { "—" }}",
                    privateRecord = true,
                    credentialFields = listOf(
                        Triple("passportHolderNames", "IMIĘ I NAZWISKO", fields["passportHolderNames"].orEmpty()),
                        Triple("passportNumber", "NUMER PASZPORTU", fields["passportNumber"].orEmpty()),
                        Triple("passportCitizenship", "OBYWATELSTWO", fields["passportCitizenship"].orEmpty()),
                        Triple("birthDate", "DATA URODZENIA", fields["birthDate"].orEmpty()),
                        Triple("passportIssueDate", "DATA WYDANIA", fields["passportIssueDate"].orEmpty()),
                        Triple("passportExpiryDate", "DATA WAŻNOŚCI", fields["passportExpiryDate"].orEmpty())
                    )
                )
                AdditionalInformationSection("Informacje dodatkowe — dane paszportowe", fields, "passportExtra", "Jest to ewidencja danych wpisanych przez użytkownika, a nie dokument paszportowy ani jego cyfrowy odpowiednik. Nie odtwarzamy strefy MRZ, kodów ani zabezpieczeń dokumentu.")
            }
        }
        }
        if (categoryId != "all") {
            DocumentOriginalAttachments(credentialKey)
            Text("Wygląd dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { onOpenDocumentAppearance(credentialKey) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edytuj wygląd graficzny") }
            CredentialTemplatePreview(credentialKey, appearance.value, values = fields.toMap())
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zapis dokumentu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Dane i zdjęcie są zabezpieczane przed nieuprawnionym dostępem.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.Button(
                    onClick = {
                        runCatching {
                            val snapshot = fields.toMutableMap()
                            snapshot["photo"] = photoBase64
                            DemoIdentityStore.save(context, snapshot)
                        }.onSuccess { saveMessage = "Zapisano i zabezpieczono dane oraz zdjęcie." }
                            .onFailure { saveMessage = "Błąd zapisu: ${it.localizedMessage ?: "nieznany błąd"}" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zapisz dane i zdjęcie") }
                if (saveMessage.isNotBlank()) Text(saveMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (categoryId != "all") {
            DocumentPdfExportButton(editorTitle, fields.toMap(), documentType = credentialKey)
        }
        Text(
            "Dane są zabezpieczane z użyciem mechanizmów systemu Android. Zachowaj ostrożność: mogą zawierać informacje wrażliwe.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
    }
    }
}

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
    val ocrPrefix = "groupedOcr:$documentKey:"
    val ocrFields = remember(documentKey) {
        mutableStateMapOf<String, String>().apply {
            DemoIdentityStore.load(context).filterKeys { it.startsWith(ocrPrefix) }
                .forEach { (key, value) -> put(key.removePrefix(ocrPrefix), value) }
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (showOcrStage) {
            GeneralDocumentOcrEditor(documentKey, ocrFields, context) {
                val snapshot = DemoIdentityStore.load(context).toMutableMap()
                ocrFields.forEach { (key, value) -> snapshot[ocrPrefix + key] = value }
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
        Box(Modifier.weight(1f)) { content() }
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
private fun CredentialAppearanceSettingsScreen(
    documentKey: String,
    onBack: () -> Unit,
    onOpenSharedAppearance: () -> Unit
) {
    val context = LocalContext.current
    val state = rememberLegitimacyPreviewSettings(documentKey)
    val options = remember(documentKey) { credentialFieldOptions(documentKey) }
    val savedValues = remember(documentKey) {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    val documentPhotoKey = "credentialPhoto_$documentKey"
    var photoMessage by remember(documentKey) { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Nie można odczytać wybranego zdjęcia.")
                require(bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024) {
                    "Zdjęcie przekracza limit 5 MB albo jest puste."
                }
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }.onSuccess { encoded ->
                savedValues[documentPhotoKey] = encoded
                DemoIdentityStore.save(context, savedValues.toMap())
                photoMessage = "Zapisano osobne zdjęcie dla dokumentu."
            }.onFailure { error ->
                photoMessage = error.localizedMessage ?: "Nie udało się wczytać zdjęcia."
            }
        }
    }
    val title = documentDisplayOptions.firstOrNull { it.first == documentKey }?.second
        ?: documentKey.replace('_', ' ').replaceFirstChar { it.uppercase() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do edycji dokumentu", color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Wygląd podglądu — $title", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zaznacz pola, które mają być widoczne na karcie. Herby, flagi, logo, tło i kolor tekstu przypisujesz osobno do tego dokumentu. Orientacja, filtry zdjęcia, widoczność całej karty oraz rozmieszczenie i rozmiar elementów pozostają wspólne dla wszystkich legitymacji.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onOpenSharedAppearance, modifier = Modifier.fillMaxWidth()) {
            Text("Edytuj wspólny wzór wszystkich legitymacji")
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Pola wyświetlane na legitymacji", fontWeight = FontWeight.Bold)
                options.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = !state.value.visibleFieldsConfigured || state.value.visibleFields.isEmpty() || key in state.value.visibleFields,
                            onCheckedChange = { checked ->
                                val current = if (state.value.visibleFieldsConfigured && state.value.visibleFields.isNotEmpty()) state.value.visibleFields
                                    else options.map { it.first }.toSet()
                                val updated = state.value.copy(
                                    visibleFields = if (checked) current + key else current - key,
                                    visibleFieldsConfigured = true
                                )
                                LegitimacyPreviewSettingsStore.save(context, documentKey, updated)
                                state.value = updated
                            }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fotografia dokumentu", fontWeight = FontWeight.Bold)
                Text(
                    if (savedValues[documentPhotoKey].isNullOrBlank())
                        "Dla tego dokumentu używana jest fotografia profilu, dopóki nie wybierzesz osobnego zdjęcia."
                    else "Wybrano osobną fotografię dla tego dokumentu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (photoMessage.isNotBlank()) {
                    Text(photoMessage, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Wybierz zdjęcie dokumentu") }
                if (!savedValues[documentPhotoKey].isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            savedValues.remove(documentPhotoKey)
                            DemoIdentityStore.save(context, savedValues.toMap())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Użyj ponownie zdjęcia profilu") }
                }
            }
        }
        LegitimacyPreviewSettingsPanel(
            documentKey = documentKey,
            settings = state.value,
            onChange = { state.value = it },
            assetSelectionOnly = false
        )
        val previewValues = savedValues.toMap() + (
            "photo" to (savedValues[documentPhotoKey] ?: savedValues["photo"].orEmpty())
        )
        CredentialTemplatePreview(documentKey, state.value, values = previewValues)
    }
}

@Composable
private fun DisabilityDecisionPreview(fields: Map<String, String>) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val authorityType = fields["disabilityAuthorityType"] ?: "powiat"
    val label = when (authorityType) {
        "foundation" -> "Dokument zespołu medycznego fundacji"
        "other_law" -> "Dokument na podstawie odrębnych przepisów"
        "medical_opinion" -> "Opinia / zaświadczenie medyczne"
        "self_declaration" -> "Oświadczenie własne"
        else -> "Ewidencja danych orzeczenia"
    }
    OwdDocumentArtworkCard(
        imageRes = R.drawable.orzeczenie_rodowe_o_niepelnosprawnosci,
        title = "Dane o niepełnosprawności",
        subtitle = label,
        holder = fields["disabilityPersonName"].orEmpty().ifBlank { "Dane osoby" },
        details = "Nr: ${fields["disabilityDecisionNumber"].orEmpty().ifBlank { "—" }} • Stopień: ${fields["disabilityDegree"].orEmpty().ifBlank { "—" }}",
        privateRecord = true,
        credentialFields = listOf(
            Triple("disabilityDecisionType", "RODZAJ ORZECZENIA", fields["disabilityDecisionType"].orEmpty()),
            Triple("disabilityDecisionNumber", "NUMER ORZECZENIA", fields["disabilityDecisionNumber"].orEmpty()),
            Triple("disabilityDegree", "STOPIEŃ", fields["disabilityDegree"].orEmpty()),
            Triple("disabilityValidity", "OKRES OBOWIĄZYWANIA", fields["disabilityValidity"].orEmpty()),
            Triple("disabilityIssuerName", "WYSTAWCA", fields["disabilityIssuerName"].orEmpty())
        )
    )
}

@Composable
private fun SchoolLegitimacyPreview(fields: Map<String, String>, photoBase64: String, student: Boolean) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = if (student) "student" else "training"
    val settingsState = rememberLegitimacyPreviewSettings(documentKey)
    val settings = settingsState.value
    val context = LocalContext.current
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val name = if (student) fields["studentNames"].orEmpty().ifBlank {
        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ")
    } else listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ")
    val institution = if (student) fields["studentUniversity"].orEmpty() else fields["trainingInstitution"].orEmpty()
    val validity = if (student) fields["studentValidity"].orEmpty() else fields["trainingValidUntil"].orEmpty()
    val valid = credentialValidityForDocument(documentKey, fields)
    val cardFields = if (student) listOf(
        Triple("studentUniversity", "UCZELNIA", institution),
        Triple("studentProgramme", "KIERUNEK", fields["studentProgramme"].orEmpty()),
        Triple("studentCardNumber", "NUMER LEGITYMACJI", fields["studentCardNumber"].orEmpty()),
        Triple("studentIssueDate", "DATA WYDANIA", fields["studentIssueDate"].orEmpty()),
        Triple("studentValidity", "WAŻNA DO", validity)
    ) else listOf(
        Triple("trainingInstitution", "INSTYTUCJA", institution),
        Triple("trainingCourseName", "KURS / KWALIFIKACJA", fields["trainingCourseName"].orEmpty()),
        Triple("trainingCardNumber", "NUMER LEGITYMACJI", fields["trainingCardNumber"].orEmpty()),
        Triple("trainingIssueDate", "DATA WYDANIA", fields["trainingIssueDate"].orEmpty()),
        Triple("trainingValidUntil", "WAŻNA DO", validity)
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (settings.showCard) {
            UnifiedCredentialCard(
                documentKey = documentKey,
                title = if (student) "LEGITYMACJA UCZNIA / STUDENTA" else "LEGITYMACJA KURSANTA",
                subtitle = institution,
                holderName = name,
                fields = cardFields,
                settings = settings,
                photo = photoBitmap,
                fallbackBackgroundRes = if (student) R.drawable.tlo_legitymacji_niebieskie else R.drawable.tlo_legitymacji_brazowe,
                validity = valid
            )
            if (settings.showReverse) LegitimacyReverseSide(
                title = if (student) "Legitymacja szkolna — rewers" else "Legitymacja uczestnika kursu — rewers",
                issuer = institution,
                number = if (student) fields["studentCardNumber"].orEmpty() else fields["trainingCardNumber"].orEmpty(),
                validity = validity,
                orientation = settings.orientation
            )
        }
    }
}

@Composable
private fun ParliamentaryCredentialPreview(fields: Map<String, String>) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = "parliament"
    val settingsState = rememberLegitimacyPreviewSettings(documentKey)
    val settings = settingsState.value
    val photoBase64 = fields["photo"].orEmpty()
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val fullName = listOf(fields["firstNames"], fields["lastName"])
        .filterNot { it.isNullOrBlank() }.joinToString(" ")
    val issuer = fields["mpIssuer"].orEmpty()
    val number = fields["mpCardNumber"].orEmpty()
    val term = fields["mpTerm"].orEmpty()
    val expiry = fields["mpEnd"].orEmpty()
    val valid = credentialValidityForDocument(documentKey, fields)
    val cardFields = listOf(
        Triple("mpCardNumber", "NUMER LEGITYMACJI", number),
        Triple("mpTerm", "KADENCJA", term),
        Triple("mpCardIssueDate", "DATA WYDANIA", fields["mpCardIssueDate"].orEmpty()),
        Triple("mpEnd", "KONIEC OKRESU", expiry),
        Triple("mpIssuer", "WYSTAWCA", issuer)
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (settings.showCard) {
            UnifiedCredentialCard(
                documentKey = documentKey,
                title = "LEGITYMACJA POSELSKA",
                subtitle = "SEJM RZECZYPOSPOLITEJ POLSKIEJ",
                holderName = fullName,
                fields = cardFields,
                settings = settings,
                photo = photoBitmap,
                fallbackBackgroundRes = R.drawable.tlo_legitymacji_szare,
                validity = valid
            )
            if (settings.showReverse) LegitimacyReverseSide(
                "Legitymacja poselska — rewers", issuer, number, expiry, settings.orientation
            )
        }
    }
}

@Composable
private fun CivicDocumentStatusTile(fields: Map<String, String>, documentId: String) {
    val documentKey = when (documentId) {
        "1" -> "identity"
        "2" -> "passport"
        "4" -> "parliament"
        "5" -> "training"
        "6" -> "student"
        "7" -> "disability"
        else -> "family"
    }
    CredentialValidityTile(credentialValidityForDocument(documentKey, fields))
}

@Composable
private fun EntityRegistryExtractScreen(onBack: () -> Unit, editable: Boolean = false) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var selectedEntity by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
    val sections = listOf(
        "company" to "Spółka z ograniczoną odpowiedzialnością — KRS",
        "foundation" to "Fundacja — KRS",
        "church" to "Kościół / związek wyznaniowy — rejestr poza KRS",
        "training" to "Instytucja szkoleniowa — rejestr poza KRS",
        "legalUnit" to "Jednostka lokalna osoby prawnej — REGON",
        "naturalUnit" to "Jednostka lokalna osoby fizycznej — REGON",
        "soleTrader" to "Firma osoby fizycznej — CEIDG / REGON"
    )
    val fieldSets = mapOf(
        "company" to listOf(
            Triple("registryCompanyName", "Pełna firma spółki", "Nazwa zgodna z odpisem"),
            Triple("registryCompanyKrs", "Numer KRS", "0000000000"),
            Triple("registryCompanyNip", "NIP", "0000000000"),
            Triple("registryCompanyRegon", "REGON", "000000000"),
            Triple("registryCompanySeat", "Siedziba", "Miejscowość"),
            Triple("registryCompanyAddress", "Adres siedziby", "Ulica, numer, kod, miejscowość"),
            Triple("registryCompanyCourt", "Sąd rejestrowy", "Nazwa sądu"),
            Triple("registryCompanyDate", "Data wpisu / odpisu", "DD-MM-RRRR"),
            Triple("registryCompanyCapital", "Kapitał zakładowy", "Kwota i waluta"),
            Triple("registryCompanyRepresentation", "Sposób reprezentacji", "Zasady reprezentacji"),
            Triple("registryCompanyBoard", "Zarząd", "Imiona, nazwiska, funkcje"),
            Triple("registryCompanyActivity", "Przedmiot działalności / PKD", "Opis lub kody PKD")
        ),
        "foundation" to listOf(
            Triple("registryFoundationName", "Nazwa fundacji", "Nazwa zgodna z odpisem"),
            Triple("registryFoundationKrs", "Numer KRS", "0000000000"),
            Triple("registryFoundationNip", "NIP", "0000000000"),
            Triple("registryFoundationRegon", "REGON", "000000000"),
            Triple("registryFoundationSeat", "Siedziba", "Miejscowość"),
            Triple("registryFoundationAddress", "Adres", "Ulica, numer, kod, miejscowość"),
            Triple("registryFoundationCourt", "Sąd rejestrowy", "Nazwa sądu"),
            Triple("registryFoundationDate", "Data wpisu / odpisu", "DD-MM-RRRR"),
            Triple("registryFoundationPurpose", "Cele statutowe", "Cele fundacji"),
            Triple("registryFoundationBoard", "Zarząd fundacji", "Imiona, nazwiska, funkcje"),
            Triple("registryFoundationRepresentation", "Sposób reprezentacji", "Zasady reprezentacji"),
            Triple("registryFoundationPublicBenefit", "Status OPP", "Tak / nie / data")
        ),
        "church" to listOf(
            Triple("registryChurchName", "Pełna nazwa kościoła / związku", "Nazwa"),
            Triple("registryChurchRegister", "Nazwa właściwego rejestru", "Rejestr kościołów i innych związków wyznaniowych lub inny właściwy rejestr"),
            Triple("registryChurchNumber", "Numer wpisu / pozycja rejestru", "Numer"),
            Triple("registryChurchAuthority", "Organ prowadzący rejestr", "Nazwa organu"),
            Triple("registryChurchDate", "Data wpisu / zaświadczenia", "DD-MM-RRRR"),
            Triple("registryChurchSeat", "Siedziba", "Miejscowość"),
            Triple("registryChurchAddress", "Adres", "Adres"),
            Triple("registryChurchLegalBasis", "Podstawa prawna / status", "Podstawa"),
            Triple("registryChurchRepresentation", "Osoby uprawnione do reprezentacji", "Imiona, nazwiska, funkcje"),
            Triple("registryChurchDocument", "Dokument źródłowy", "Nazwa i data dokumentu")
        ),
        "training" to listOf(
            Triple("registryTrainingName", "Pełna nazwa instytucji szkoleniowej", "Nazwa"),
            Triple("registryTrainingRegister", "Rejestr / ewidencja", "Np. RIS, BUR lub inny właściwy rejestr"),
            Triple("registryTrainingNumber", "Numer wpisu / identyfikator", "Numer"),
            Triple("registryTrainingAuthority", "Organ / operator rejestru", "Nazwa"),
            Triple("registryTrainingNip", "NIP", "0000000000"),
            Triple("registryTrainingRegon", "REGON", "000000000"),
            Triple("registryTrainingSeat", "Siedziba i adres", "Adres"),
            Triple("registryTrainingDate", "Data wpisu / aktualizacji", "DD-MM-RRRR"),
            Triple("registryTrainingScope", "Zakres działalności szkoleniowej", "Zakres"),
            Triple("registryTrainingAccreditation", "Akredytacje / uprawnienia", "Nazwa, zakres, numer, data"),
            Triple("registryTrainingDocument", "Dokument źródłowy", "Nazwa i data dokumentu")
        ),
        "legalUnit" to listOf(
            Triple("registryLegalUnitParentName", "Nazwa osoby prawnej — jednostki nadrzędnej", "Pełna nazwa"),
            Triple("registryLegalUnitParentKrs", "KRS jednostki nadrzędnej (jeśli dotyczy)", "0000000000"),
            Triple("registryLegalUnitParentRegon", "REGON jednostki nadrzędnej", "000000000"),
            Triple("registryLegalUnitName", "Nazwa jednostki lokalnej", "Nazwa oddziału / zakładu / placówki"),
            Triple("registryLegalUnitRegon", "REGON jednostki lokalnej", "000000000"),
            Triple("registryLegalUnitNip", "NIP jednostki lokalnej (jeśli nadany)", "0000000000"),
            Triple("registryLegalUnitType", "Rodzaj jednostki lokalnej", "Oddział, zakład, placówka, filia"),
            Triple("registryLegalUnitAddress", "Adres jednostki lokalnej", "Ulica, numer, kod, miejscowość"),
            Triple("registryLegalUnitStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registryLegalUnitEnd", "Data zakończenia działalności (jeśli dotyczy)", "DD-MM-RRRR"),
            Triple("registryLegalUnitActivity", "Przeważający rodzaj działalności / PKD", "Kod i opis"),
            Triple("registryLegalUnitContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        ),
        "naturalUnit" to listOf(
            Triple("registryNaturalUnitPerson", "Imię i nazwisko osoby fizycznej", "Dane osoby prowadzącej"),
            Triple("registryNaturalUnitParentRegon", "REGON podmiotu nadrzędnego (jeśli dotyczy)", "000000000"),
            Triple("registryNaturalUnitName", "Nazwa jednostki lokalnej", "Nazwa placówki / zakładu"),
            Triple("registryNaturalUnitRegon", "REGON jednostki lokalnej", "000000000"),
            Triple("registryNaturalUnitNip", "NIP (jeśli nadany)", "0000000000"),
            Triple("registryNaturalUnitType", "Rodzaj jednostki lokalnej", "Zakład, placówka, punkt"),
            Triple("registryNaturalUnitAddress", "Adres jednostki lokalnej", "Ulica, numer, kod, miejscowość"),
            Triple("registryNaturalUnitStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registryNaturalUnitEnd", "Data zakończenia działalności (jeśli dotyczy)", "DD-MM-RRRR"),
            Triple("registryNaturalUnitActivity", "Rodzaj działalności / PKD", "Kod i opis"),
            Triple("registryNaturalUnitContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        ),
        "soleTrader" to listOf(
            Triple("registrySoleTraderName", "Firma przedsiębiorcy", "Imię i nazwisko oraz ewentualne dodatki"),
            Triple("registrySoleTraderFirstName", "Imię", "Imię"),
            Triple("registrySoleTraderLastName", "Nazwisko", "Nazwisko"),
            Triple("registrySoleTraderNip", "NIP", "0000000000"),
            Triple("registrySoleTraderRegon", "REGON", "000000000"),
            Triple("registrySoleTraderCeidg", "Identyfikator / wpis CEIDG", "Numer lub identyfikator"),
            Triple("registrySoleTraderStatus", "Status działalności", "Aktywna, zawieszona, zakończona"),
            Triple("registrySoleTraderAddress", "Adres do doręczeń", "Adres"),
            Triple("registrySoleTraderPlace", "Stałe miejsce wykonywania działalności", "Adres lub informacja o braku stałego miejsca"),
            Triple("registrySoleTraderStart", "Data rozpoczęcia działalności", "DD-MM-RRRR"),
            Triple("registrySoleTraderSuspension", "Data zawieszenia / wznowienia", "Daty"),
            Triple("registrySoleTraderEnd", "Data zakończenia działalności", "DD-MM-RRRR"),
            Triple("registrySoleTraderActivity", "Przeważający rodzaj działalności / PKD", "Kod i opis"),
            Triple("registrySoleTraderProxy", "Pełnomocnik / prokurent (jeśli wpisany)", "Imię, nazwisko, zakres umocowania"),
            Triple("registrySoleTraderContact", "Dane kontaktowe", "Telefon, e-mail, strona")
        )
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Menu główne", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Odpis z rejestru podmiotów", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(if (editable) "Wprowadzanie i aktualizacja danych podmiotów. Wybierz rodzaj podmiotu i uzupełnij dane na podstawie właściwego dokumentu lub rejestru." else "Podgląd zapisanych danych identyfikacyjnych i rejestrowych podmiotów. Wybierz kafelek, aby wyświetlić szczegóły.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Text(if (editable) "Wpisy są przechowywane lokalnie jako dane wprowadzone przez użytkownika. Podaj źródło i datę sprawdzenia." else "Dane zapisane w ustawieniach. W razie potrzeby porównaj je z właściwym rejestrem; aplikacja nie pobiera urzędowego odpisu.",
                Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
        }
        if (selectedEntity == null) {
            Text("Wybierz rodzaj podmiotu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { (key, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().height(142.dp).clickable { selectedEntity = key },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Otwórz dokument  ›", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val entityKey = selectedEntity!!
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹  Wszystkie rodzaje podmiotów", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { selectedEntity = null }.padding(vertical = 5.dp))
            }
            val entityTitle = sections.first { it.first == entityKey }.second
            val entityFields = fieldSets[entityKey].orEmpty()
            val entityValues = entityFields.associate { (key, label, _) -> label to fields[key].orEmpty() } +
                mapOf(
                    "Źródło danych / adres rejestru" to fields["registry_${entityKey}_source"].orEmpty(),
                    "Data sprawdzenia" to fields["registry_${entityKey}_checked"].orEmpty(),
                    "Data sporządzenia dokumentu" to fields["registry_${entityKey}_documentDate"].orEmpty()
                )
            Text(entityTitle, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!editable) {
                val previewSettings = rememberLegitimacyPreviewSettings("entity").value.copy(visibleFieldsConfigured = false)
                val previewFields = entityValues.entries.take(12).mapIndexed { index, entry ->
                    Triple("registryField_$index", entry.key.uppercase(), entry.value.ifBlank { "—" })
                }
                UnifiedCredentialCard(
                    documentKey = "entity",
                    title = entityTitle.uppercase(),
                    subtitle = "PODGLĄD DANYCH REJESTROWYCH",
                    holderName = entityFields.firstOrNull()?.let { fields[it.first].orEmpty() }.orEmpty().ifBlank { "DANE PODMIOTU" },
                    fields = previewFields,
                    settings = previewSettings
                )
            }
            entityFields.forEach { (key, label, placeholder) ->
                if (editable) DemoDataField(key, label, fields, placeholder)
                else RegistryReadOnlyField(label, fields[key])
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            Text("Źródło i kontrola aktualności", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (editable) {
                DemoDataField("registry_${entityKey}_source", "Źródło danych / adres rejestru", fields, "Adres strony lub nazwa dokumentu")
                DemoDataField("registry_${entityKey}_checked", "Data sprawdzenia", fields, "DD-MM-RRRR")
                DemoDataField("registry_${entityKey}_documentDate", "Data sporządzenia odpisu / dokumentu", fields, "DD-MM-RRRR")
                DemoDataField("registry_${entityKey}_notes", "Uwagi", fields, "Informacje uzupełniające")
                Button(onClick = {
                    runCatching { DemoIdentityStore.save(context, fields.toMap()) }
                        .onSuccess { status = "Zapisano i zabezpieczono dane." }
                        .onFailure { status = "Nie udało się zapisać danych: ${it.message}" }
                }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz dane podmiotu") }
                if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                RegistryReadOnlyField("Źródło danych / adres rejestru", fields["registry_${entityKey}_source"])
                RegistryReadOnlyField("Data sprawdzenia", fields["registry_${entityKey}_checked"])
                RegistryReadOnlyField("Data sporządzenia odpisu / dokumentu", fields["registry_${entityKey}_documentDate"])
                RegistryReadOnlyField("Uwagi", fields["registry_${entityKey}_notes"])
            }
            DocumentPdfExportButton(entityTitle, entityValues)
        }
    }
}

@Composable
private fun RegistryReadOnlyField(label: String, value: String?) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(value?.takeIf { it.isNotBlank() } ?: "Brak zapisanego wpisu", color = if (value.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (value.isNullOrBlank()) FontWeight.Normal else FontWeight.SemiBold)
    }
}

@Composable
private fun AdditionalInformationSection(
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
private fun DemoDataField(key: String, label: String, fields: MutableMap<String, String>, placeholder: String = "Wpisz dane") {
    val binding = LocalCredentialFieldVisibility.current
    val settings = binding?.settings?.value
    val checked = settings?.let { !it.visibleFieldsConfigured || it.visibleFields.isEmpty() || key in it.visibleFields } ?: true
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = fields[key] ?: "",
            onValueChange = { fields[key] = it },
            modifier = Modifier.weight(1f),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true
        )
        if (binding != null && binding.documentKey.isNotBlank()) {
            Checkbox(
                checked = checked,
                onCheckedChange = { selected ->
                    val current = if (settings?.visibleFieldsConfigured == true && settings.visibleFields.isNotEmpty()) settings.visibleFields
                        else (binding.fields.keys + credentialFieldOptions(binding.documentKey).map { it.first }).toSet()
                    val next = if (selected) current + key else current - key
                    val updated = (settings ?: LegitimacyPreviewSettings()).copy(
                        visibleFields = next,
                        visibleFieldsConfigured = true
                    )
                    LegitimacyPreviewSettingsStore.save(context, binding.documentKey, updated)
                    binding.settings.value = updated
                }
            )
        }
    }
}

internal object DemoIdentityStore {
    private const val KEY_ALIAS = "obywatel_demo_identity_aes"
    private const val FILE_NAME = "identity_profile.enc"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun load(context: Context): Map<String, String> = runCatching { loadStrict(context) }.getOrElse { emptyMap() }

    fun loadStrict(context: Context): Map<String, String> {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()
        val encrypted = file.readBytes()
        require(encrypted.size > 12) { "Nieprawidłowy zaszyfrowany plik profilu." }
        val iv = encrypted.copyOfRange(0, 12)
        val ciphertext = encrypted.copyOfRange(12, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONObject(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        val result = mutableMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.matches(Regex("[A-Za-z0-9_-]{1,64}"))) { "Nieprawidłowa nazwa pola profilu." }
            require(!json.isNull(key)) { "Pole profilu zawiera wartość pustą typu null." }
            result[key] = json.getString(key)
        }
        return result
    }

    fun save(context: Context, values: Map<String, String>) {
        val json = JSONObject()
        values.forEach { (key, value) -> json.put(key, value) }
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
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}

@Composable
private fun BottomNavigation(selectedTab: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Dokumenty", 0, R.drawable.nav_documents),
        Triple("Usługi", 2, R.drawable.nav_services),
        Triple("Kod QR", 3, R.drawable.nav_scan),
        Triple("Szukaj", 1, R.drawable.nav_search),
        Triple("Więcej", 4, R.drawable.nav_more)
    )
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.6.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, index, iconRes) ->
            val selected = selectedTab == index
            val selectedIcon = when (index) {
                0 -> R.drawable.nav_documents_selected
                2 -> R.drawable.nav_services_selected
                3 -> R.drawable.nav_scan_selected
                1 -> R.drawable.nav_search_selected
                else -> R.drawable.nav_more_selected
            }
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(index) }.padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.88f)
                        .height(43.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) Color(0x33218FEA) else Color.Transparent
                        )
                        .then(
                            if (selected) Modifier.border(
                                width = 2.dp,
                                color = Color(0xFF168FEA),
                                shape = RoundedCornerShape(24.dp)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (selected) selectedIcon else iconRes),
                        contentDescription = label,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(27.dp)
                    )
                }
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
