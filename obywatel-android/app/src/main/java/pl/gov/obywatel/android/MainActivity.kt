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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.Blue
import pl.gov.obywatel.android.ui.theme.BlueLight
import pl.gov.obywatel.android.ui.theme.Border
import pl.gov.obywatel.android.ui.theme.Ink
import pl.gov.obywatel.android.ui.theme.Muted
import pl.gov.obywatel.android.ui.theme.Navy
import pl.gov.obywatel.android.ui.theme.ObywatelTheme
import pl.gov.obywatel.android.ui.theme.White
import pl.gov.obywatel.android.ui.identity.IdentityDocumentsScreen
import pl.gov.obywatel.android.ui.documents.DocumentsScreen
import pl.gov.obywatel.android.ui.services.PublicServicesScreen
import kotlinx.coroutines.delay

@Composable
private fun GeneralDocumentOcrEditor(categoryId: String, fields: MutableMap<String, String>, context: Context, saveMessage: (String) -> Unit) {
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

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Odczyt dokumentu z filmu przez OCR", color = Navy, fontWeight = FontWeight.Bold)
            Text("Nagraj krótki film, powoli pokazując dokument z bliska. Aplikacja pobierze do 24 klatek nagrania, rozpozna tekst na każdej z nich i połączy wyniki, aby uzupełnić pasujące pola. Możesz też wybrać wcześniej nagrany film albo zdjęcie/skan. OCR nie potwierdza autentyczności dokumentu.",
                color = Muted, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                runCatching {
                    val file = File.createTempFile("document-ocr-video-", ".mp4", context.cacheDir)
                    androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.ocrfiles", file)
                }.onSuccess { pendingVideoUri = it; videoCamera.launch(it) }
                    .onFailure { saveMessage("Nie można uruchomić kamery wideo: ${it.localizedMessage ?: "błąd przygotowania nagrania"}") }
            }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Nagraj film dokumentu") }
            OutlinedButton(onClick = { videoPicker.launch("video/*") }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Wybierz nagrany film") }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, enabled = !processing, modifier = Modifier.fillMaxWidth()) { Text("Wybierz zdjęcie lub skan") }
            if (processing) Text("Analiza klatek filmu i uzupełnianie pól…", color = Blue)
            if (mappedCount > 0) Text("Wstępnie uzupełniono $mappedCount pustych pól", color = Blue, fontWeight = FontWeight.SemiBold)
            if (recognizedText.isNotBlank()) {
                Text("Tekst rozpoznany z nagrania — sprawdź", color = Navy, fontWeight = FontWeight.SemiBold)
                Text(recognizedText, color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text("Karty lojalnościowe sklepów", color = Navy, fontWeight = FontWeight.Bold)
        Text("Można prowadzić maksymalnie 25 kart. Dane można ręcznie poprawiać. Zdjęcie jest przetwarzane bez zapisywania pliku w pamięci aplikacji; zapisujesz wyłącznie sprawdzone dane tekstowe.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text("Karta $selectedCard z $cardCount", color = Navy, fontWeight = FontWeight.SemiBold)
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
        if (processing) Text("Odczytywanie obrazu…", color = Blue)
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
            Text("Pełny tekst rozpoznany przez OCR", color = Navy, fontWeight = FontWeight.SemiBold)
            Text(fields["${prefix}OcrText"].orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text("Podgląd kodów", color = Navy, fontWeight = FontWeight.SemiBold)
        if (barcode != null) Image(barcode.asImageBitmap(), contentDescription = "Kod kreskowy karty lojalnościowej",
            modifier = Modifier.fillMaxWidth().height(76.dp), contentScale = ContentScale.Fit)
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
    var showIntro by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1_500)
        showIntro = false
    }
    if (showIntro) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6F8)),
            contentAlignment = Alignment.Center
        ) {
            val appearance = LocalApplicationAppearance.current
            val context = LocalContext.current
            Image(
                painter = painterResource(applicationDrawable(context, appearance.introEmblem,
                    R.drawable.file_00000000532082108d561dcd2385f8f9)),
                contentDescription = "Godło aplikacji — plansza wprowadzająca",
                modifier = Modifier.size(184.dp),
                contentScale = ContentScale.Fit
            )
        }
    } else {
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
private fun EntryWelcomeScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    val cream = Color(0xFFFFF1D1)
    val context = LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(applicationDrawable(context, appearance.welcomeBackground,
                R.drawable.file_00000000e0e4820a839fa3184993f41b)),
            contentDescription = "Polska flaga na maszcie na tle nieba",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color(0x55000000)))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BrandMark(size = 64.dp)
                Text(
                    "mObywatel",
                    color = cream,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(34.dp))
            Text(
                "Dzień dobry!",
                color = cream,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Zaloguj się do aplikacji",
                color = cream,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(76.dp),
                shape = RoundedCornerShape(22.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9FD5FF),
                    contentColor = Color(0xFF062A59)
                )
            ) {
                Text("Zaloguj się", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Więcej o Krajowym Planie\nOdbudowy",
                    modifier = Modifier.weight(1f),
                    color = cream,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "wersja\n4.93.1\n(6704)",
                    color = cream,
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
    val context = LocalContext.current
    val appearance = LocalApplicationAppearance.current

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(applicationDrawable(context, appearance.loginBackground,
                R.drawable.file_00000000e0e4820a839fa3184993f41b)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color(0x55000000)))
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
                    containerColor = Color(0xFF9FD5FF),
                    contentColor = Color(0xFF062A59)
                )
            ) {
                Text("Zaloguj się", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
    val tint: Color = Blue,
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
private val documentDisplayOptions = listOf(
    Triple("profile", "Dane osobowe profilu", R.drawable.lib_ic_account),
    Triple("identity", "Dowód tożsamości", R.drawable.documents_identity_card),
    Triple("passport", "Dane paszportowe", R.drawable.documents_passport),
    Triple("registry", "Odpis z rejestru podmiotów", R.drawable.court_registry_icon),
    Triple("family", "Dokument rodowy", R.drawable.documents_family_record),
    Triple("parliament", "Legitymacja poselska", R.drawable.documents_deputy_legitimacy),
    Triple("training", "Legitymacja kursanta", R.drawable.documents_course_legitimacy),
    Triple("student", "Legitymacja ucznia/studenta", R.drawable.documents_student_legitimacy),
    Triple("disability", "Orzeczenia i zaświadczenia", R.drawable.documents_medical_certificates),
    Triple("church", "Dokumenty kościelne", R.drawable.church_communities),
    Triple("civil", "Dokumenty cywilne", R.drawable.civic_service_illustration),
    Triple("business", "Dokumenty firmowe i podmiotowe", R.drawable.lib_ic_building),
    Triple("bank_accounts", "Konta bankowe", R.drawable.lib_ic_wallet),
    Triple("vaccines", "Książeczka szczepień WHO", R.drawable.lib_ic_medical_record),
    Triple("signed_pdf", "Dokumenty PDF i podpisy", R.drawable.lib_ic_document_signed),
    Triple("imported", "Dokumenty własne — pliki", R.drawable.lib_ic_folder),
    Triple("children", "Dokumentacja dzieci", R.drawable.family),
    Triple("branding", "Zasoby identyfikacji dokumentów", R.drawable.lib_ic_image),
    Triple("loyalty", "Karty lojalnościowe sklepów", R.drawable.lib_ic_credit_card)
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
private fun loadDocumentPlacements(context: Context): Map<String, String> {
    val prefs = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
    if (!prefs.getBoolean(DOCUMENT_VISIBILITY_MIGRATION_KEY, false)) {
        val editor = prefs.edit()
        documentDisplayOptions.forEach { (id, _, _) ->
            editor.putString(DOCUMENT_DISPLAY_KEY_PREFIX + id, if (id == "identity") "dashboard" else "hidden")
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
    val exportVCardLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/vcard")) { uri ->
        if (uri != null) runCatching {
            require(exportSnapshot.isNotEmpty()) { "Brak zapisanych danych do eksportu." }
            val payload = VCardCodec.encodeArchive(exportSnapshot)
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: error("Android nie udostępnił strumienia zapisu dla wybranego pliku.")
            output.use {
                it.write(bytes)
                it.flush()
            }
            val savedBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Nie można ponownie odczytać zapisanego pliku vCard.")
            require(savedBytes.isNotEmpty()) { "Zapisany plik vCard jest pusty." }
            require(savedBytes.contentEquals(bytes)) {
                "Weryfikacja zapisu nie powiodła się: zawartość pliku różni się od eksportowanych danych."
            }
            "Zapisano ${exportSnapshot.size} pól (${bytes.size} bajtów) do pliku vCard."
        }.onSuccess { importStatus = it }
            .onFailure { importStatus = "Błąd eksportu: ${it.message ?: "nieznany błąd"}" }
        else {
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

    fun handleSystemBack() {
        when {
            showCredentialAppearanceEditor -> { showCredentialAppearanceEditor = false }
            showApplicationAppearanceSettings -> { showApplicationAppearanceSettings = false; showSettingsMenu = true }
            showIdentityDesigner -> { showIdentityDesigner = false }
            showPublicServicesBrowser -> { showPublicServicesBrowser = false }
            showImportedDocuments -> { showImportedDocuments = false; selectedTab = 1 }
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
        enabled = showCredentialAppearanceEditor || showApplicationAppearanceSettings || showIdentityDesigner || showPublicServicesBrowser || showImportedDocuments || showBankAccounts || showDocumentsEditor || showSettingsMenu || showDocumentVisibilitySettings || showDocumentBrandAssets || documentBrandTarget != null || showRegistryEditor || settingsDocumentEditor != null || selectedDocumentId != null ||
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
        when (documentId) {
            "identity" -> selectedDocumentId = "1"
            "passport" -> selectedDocumentId = "2"
            "family" -> selectedDocumentId = "3"
            "parliament" -> selectedDocumentId = "4"
            "training" -> selectedDocumentId = "5"
            "student" -> selectedDocumentId = "6"
            "disability" -> selectedDocumentId = "7"
            "loyalty_cards" -> selectedDocumentId = "8"
            "registry" -> activeService = "Odpis z rejestru podmiotów"
            "church" -> activeService = "Dokumenty Kościelne"
            "civil" -> activeService = "Formularze dokumentów"
            "business" -> activeService = "Dokumenty rejestru podmiotów"
            "bank_accounts" -> showBankAccounts = true
            "vaccines" -> activeService = "eKarta Szczepień"
            "signed_pdf" -> activeService = "Dokumenty PDF i podpisy elektroniczne"
            "branding" -> activeService = "Zasoby identyfikacji dokumentów"
            else -> activeService = "Dokumenty"
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    showCredentialAppearanceEditor -> CredentialAppearanceDefaultsScreen(onBack = { showCredentialAppearanceEditor = false })
                    showApplicationAppearanceSettings -> ApplicationAppearanceSettingsScreen(
                        appearance = LocalApplicationAppearance.current,
                        onChange = onAppearanceChange,
                        onBack = { showApplicationAppearanceSettings = false; showSettingsMenu = true }
                    )
                    showIdentityDesigner -> IdentityDocumentsScreen(onBack = { showIdentityDesigner = false })
                    showPublicServicesBrowser -> PublicServicesScreen(onBack = { showPublicServicesBrowser = false })
                    showImportedDocuments -> DocumentsScreen(onBack = { showImportedDocuments = false; selectedTab = 1 }, readOnly = true)
                    showBankAccounts -> FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, readOnly = true, onBack = { showBankAccounts = false })
                    showDocumentsEditor -> DemoDocumentsScreen(categoryId = selectedEditorCategory, onBack = { showDocumentsEditor = false; showSettingsMenu = true })
                    showRegistryEditor -> EntityRegistryExtractScreen(onBack = { showRegistryEditor = false; showSettingsMenu = true }, editable = true)
                    settingsDocumentEditor == "imported" -> DocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, readOnly = false)
                    settingsDocumentEditor == "signed_pdf" -> SignedPdfDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true }, allowImport = true, readOnly = false)
                    settingsDocumentEditor == "family" -> FamilyEntityDocumentsScreen(entityMode = false, onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "business" -> FamilyEntityDocumentsScreen(entityMode = true, onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "bank_accounts" -> FamilyEntityDocumentsScreen(entityMode = false, bankAccountsOnly = true, onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "church" -> ChurchDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "civil" -> OfficialDocumentsScreen(onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "children_health" -> FamilyHealthRecordsScreen(initialSection = "children", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
                    settingsDocumentEditor == "vaccines" -> FamilyHealthRecordsScreen(initialSection = "vaccines", onBack = { settingsDocumentEditor = null; showSettingsMenu = true })
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
                        onManageDocumentVisibility = { showSettingsMenu = false; showDocumentVisibilitySettings = true },
                        onManageDocumentBrandAssets = { showSettingsMenu = false; showDocumentBrandAssets = true },
                        onEditDocumentBranding = { target -> showSettingsMenu = false; documentBrandTarget = target },
                        onEditDocuments = { category -> selectedEditorCategory = category; showSettingsMenu = false; showDocumentsEditor = true },
                        onEditDisability = { showSettingsMenu = false; showDisabilitySettings = true },
                        onEditRegistry = { showSettingsMenu = false; showRegistryEditor = true },
                        onEditOtherDocuments = { key -> settingsDocumentEditor = key; showSettingsMenu = false },
                        onExportAll = {
                            exportSnapshot = DemoIdentityStore.load(context) +
                                LegitimacyPreviewSettingsStore.exportAll(context) +
                                loadDocumentPlacements(context).mapKeys { "dp_${it.key}" } +
                                mapOf(
                                    "churchDocumentArchive" to ChurchDocumentStore.exportArchive(context),
                                    "familyEntityDocumentArchive" to FamilyEntityDocumentStore.exportArchive(context),
                                    "officialDocumentArchive" to OfficialDocumentStore.exportArchive(context),
                                    "signedPdfArchive" to exportSignedPdfArchive(context),
                                    "brandAssetArchive" to DocumentBrandStore.exportArchive(context),
                                    "professionalQualificationArchive" to exportProfessionalQualificationArchive(context),
                                    "familyHealthArchive" to exportFamilyHealthArchive(context),
                                    "medicalHistoryArchive" to exportMedicalHistoryArchive(context),
                                    "importedAttachmentArchive" to pl.gov.obywatel.android.data.ImportedDocumentStore.exportArchive(context),
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
                        onBack = { selectedDocumentId = null },
                        onOpenService = { title -> selectedDocumentId = null; activeService = title }
                    )
                    showDemoDocuments -> DocumentsWalletScreen(
                        onBack = { showDemoDocuments = false },
                        onAddDocuments = { showDemoDocuments = false; showDocumentVisibilitySettings = true },
                        documentPlacements = documentPlacements,
                        documentCardStyle = documentCardStyle,
                        onOpenDetails = { selectedDocumentId = it },
                        onOpenService = { title ->
                            when (title) { "Konta bankowe" -> showBankAccounts = true; "Dokumenty własne — pliki" -> showImportedDocuments = true; else -> activeService = title }
                        }
                    )
                    showDisabilitySettings -> DisabilitySettingsScreen(onBack = { showDisabilitySettings = false; showSettingsMenu = true })
                    showCertificates -> CertificatesScreen(onBack = { showCertificates = false })
                    activeService == "Informacje prawne" -> LegalInformationScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Odpis z rejestru podmiotów" -> EntityRegistryExtractScreen(
                        onBack = { activeService = null }
                    )
                    activeService == "Dokumenty Kościelne" -> ChurchDocumentsScreen(onBack = { activeService = null }, readOnly = true)
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
                                            Image(painter = painterResource(iconRes), contentDescription = null,
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
                        onOpenCredentialAppearance = { showCredentialAppearanceEditor = true },
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
                                    IdentityDashboardCard(onOpen = { openDocumentCard("identity") })
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Zarządzaj",
                                        color = Color(0xFF83DFFF),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { showDemoDocuments = true }
                                            .padding(vertical = 4.dp)
                                    )
                                    Box(Modifier.fillMaxWidth().height(390.dp)) {
                                        Button(
                                            onClick = {
                                                showSettingsMenu = false
                                                showDocumentVisibilitySettings = true
                                            },
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                                .fillMaxWidth(0.54f)
                                                .height(64.dp),
                                            shape = RoundedCornerShape(22.dp),
                                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF91D9F8),
                                                contentColor = Color(0xFF102D5D)
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.lib_ic_plus),
                                                contentDescription = null,
                                                tint = Color(0xFF102D5D),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Dodaj dokument",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
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
                                            Text("Otwórz", color = Blue, fontWeight = FontWeight.Bold)
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
                                                    .background(if (active) Blue else MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, if (active) Blue else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(18.dp))
                                                    .clickable { serviceCategory = category }
                                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                                            ) {
                                                Text(category,
                                                    color = if (active) White else MaterialTheme.colorScheme.onSurface,
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
                                                service,
                                                isFavorite = favoriteServices[service.title] == true,
                                                onFavorite = {
                                                    favoriteServices[service.title] = favoriteServices[service.title] != true
                                                },
                                                onClick = { openService(service.title) }
                                            )
                                            Spacer(Modifier.height(10.dp))
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
                                        "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive",
                                        "identityDocumentsArchive", "localRegistryArchive", "activityHistoryArchive"
                                    )
                            }
                            DemoIdentityStore.save(context, current.toMutableMap().apply { putAll(profileValues) })
                            incoming["churchDocumentArchive"]?.let { ChurchDocumentStore.importArchive(context, it) }
                            incoming["familyEntityDocumentArchive"]?.let { FamilyEntityDocumentStore.importArchive(context, it) }
                            incoming["officialDocumentArchive"]?.let { OfficialDocumentStore.importArchive(context, it) }
                            incoming["signedPdfArchive"]?.let { importSignedPdfArchive(context, it) }
                            incoming["brandAssetArchive"]?.let { DocumentBrandStore.importArchive(context, it) }
                            incoming["professionalQualificationArchive"]?.let { importProfessionalQualificationArchive(context, it) }
                            incoming["familyHealthArchive"]?.let { importFamilyHealthArchive(context, it) }
                            incoming["medicalHistoryArchive"]?.let { importMedicalHistoryArchive(context, it) }
                            incoming["importedAttachmentArchive"]?.let { pl.gov.obywatel.android.data.ImportedDocumentStore.importArchive(context, it) }
                            incoming["identityDocumentsArchive"]?.let { pl.gov.obywatel.android.ui.identity.importIdentityDocumentsArchive(context, it) }
                            incoming["localRegistryArchive"]?.let { pl.gov.obywatel.android.ui.registry.importLocalRegistryArchive(context, it) }
                            incoming["activityHistoryArchive"]?.let { importActivityHistoryArchive(context, it) }
                            LegitimacyPreviewSettingsStore.importAll(context, incoming)
                            val importedPlacements = incoming.filterKeys { it.startsWith("dp_") }
                                .mapKeys { it.key.removePrefix("dp_") }
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
                                        "churchDocumentArchive", "familyEntityDocumentArchive", "officialDocumentArchive",
                                        "signedPdfArchive", "brandAssetArchive", "professionalQualificationArchive",
                                        "familyHealthArchive", "medicalHistoryArchive", "importedAttachmentArchive",
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
    Text("‹  $label", color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 20.dp, vertical = 8.dp))
}

private const val QR_BRAND_NONE = "none"
private const val QR_BRAND_STATE = "state"
private const val QR_BRAND_FILE = "file"

@Composable
private fun QrBrandingControls(branding: String, onBrandingChange: (String) -> Unit, logoLoaded: Boolean, onChooseLogo: () -> Unit, onClearLogo: () -> Unit) {
    Text("Znak graficzny obok kodu QR", color = Navy, fontWeight = FontWeight.Bold)
    listOf(QR_BRAND_STATE to "Godło państwowe", QR_BRAND_FILE to "Logo z pliku", QR_BRAND_NONE to "Bez znaku").forEach { (value, label) ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onBrandingChange(value) }) {
            RadioButton(selected = branding == value, onClick = { onBrandingChange(value) })
            Text(label, color = Ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (branding == QR_BRAND_FILE) {
        OutlinedButton(onClick = onChooseLogo, modifier = Modifier.fillMaxWidth()) { Text(if (logoLoaded) "Zmień załączone logo" else "Wybierz logo z pliku") }
        if (logoLoaded) OutlinedButton(onClick = onClearLogo, modifier = Modifier.fillMaxWidth()) { Text("Usuń załączone logo") }
        Text("Logo jest wyświetlane poza kodem QR. Żaden jego fragment nie zasłania modułów kodu.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
    onBack: () -> Unit,
    onOpenService: (String) -> Unit
) {
    val context = LocalContext.current
    val allValues = remember { DemoIdentityStore.load(context) }
    val group = remember(documentId) {
        val normalizedId = when (documentId) {
            "0" -> "profile"
            "1" -> "identity"
            "2" -> "passport"
            "3" -> "family"
            "4" -> "parliament"
            "5" -> "training"
            "6" -> "student"
            "7" -> "disability"
            "8" -> "loyalty"
            else -> documentId
        }
        owdDocumentGroups().firstOrNull { it.id == normalizedId }
            ?: if (normalizedId == "loyalty") OwdDocumentGroup("loyalty", "Karty lojalnościowe", listOf(
                "Posiadacz" to "loyaltyHolder", "Nazwa programu" to "loyaltyName",
                "Numer karty" to "loyaltyNumber", "Status" to "status"
            )) else null
    }
    val title = group?.title ?: "Dokument"
    val values = remember(allValues, group) {
        group?.fields.orEmpty().mapNotNull { (label, key) ->
            allValues[key]?.takeIf { it.isNotBlank() }?.let { label to it }
        }.toMap() + listOfNotNull(allValues["photo"]?.takeIf { it.isNotBlank() }?.let { "photo" to it })
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "‹  Wróć do dokumentów",
            color = Blue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(title, color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (values.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BlueLight)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Brak zapisanych danych tego dokumentu.", fontWeight = FontWeight.SemiBold, color = Navy)
                    Text("Uzupełnij dane w ustawieniach, aby wyświetlić podgląd.", color = Muted)
                }
            }
        } else {
            DocumentIdentityTemplate(
                title = title,
                values = values,
                documentType = group?.id ?: "official"
            )
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
        Text("Obywatelska Wymiana Danych", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("OWD • niezależna wymiana danych między zgodnymi aplikacjami", color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Wybór danych do udostępnienia", color = Navy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (populated.isEmpty()) {
            Text("Brak zapisanych danych do udostępnienia. Uzupełnij je w Ustawieniach.", color = Muted, style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Zakres udostępnienia", color = Navy, fontWeight = FontWeight.Bold)
            listOf(
                "all" to "Wszystkie dane i dokumenty",
                "documents" to "Wybrane dokumenty",
                "document_fields" to "Dane z wybranego dokumentu"
            ).forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectionMode == mode, onClick = {
                        selectionMode = mode
                        offerQr = null
                        consent = false
                        if (mode == "document_fields") selectedDocumentFields = populatedByDocument[selectedDocumentId].orEmpty()
                    })
                    Text(label, color = Navy, fontWeight = if (selectionMode == mode) FontWeight.Bold else FontWeight.Medium)
                }
            }
            when (selectionMode) {
                "documents" -> {
                    Text("Zaznacz dokumenty, których dane mają wejść do zakresu.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }.forEach { group ->
                        val keys = populatedByDocument[group.id].orEmpty()
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = group.id in selectedDocuments, onCheckedChange = { checked ->
                                selectedDocuments = if (checked) selectedDocuments + group.id else selectedDocuments - group.id
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(group.title, color = Navy, fontWeight = FontWeight.SemiBold)
                                Text("${keys.size} dostępnych pól", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                "document_fields" -> {
                    Text("Wybierz jeden dokument, a następnie zaznacz konkretne pola.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    val availableGroups = documentGroups.filter { populatedByDocument[it.id].orEmpty().isNotEmpty() }
                    availableGroups.forEach { group ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedDocumentId == group.id, onClick = {
                                selectedDocumentId = group.id
                                selectedDocumentFields = populatedByDocument[group.id].orEmpty()
                                offerQr = null
                                consent = false
                            })
                            Text(group.title, color = Navy, fontWeight = if (selectedDocumentId == group.id) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                    val currentGroup = documentGroups.firstOrNull { it.id == selectedDocumentId }
                    currentGroup?.fields?.filter { it.second in populatedByDocument[selectedDocumentId].orEmpty() }?.forEach { (label, key) ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(White).padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = key in selectedDocumentFields, onCheckedChange = { checked ->
                                selectedDocumentFields = if (checked) selectedDocumentFields + key else selectedDocumentFields - key
                                offerQr = null
                                consent = false
                            })
                            Column(Modifier.weight(1f)) {
                                Text(label, color = Navy, fontWeight = FontWeight.Medium)
                                Text(fields[key].orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Text("Wybrano ${selected.size} pól z ${allPopulatedKeys.size}. Zmiana zakresu unieważnia poprzednią zgodę i kod QR.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(value = purpose, onValueChange = { purpose = it; consent = false; offerQr = null },
            modifier = Modifier.fillMaxWidth(), label = { Text("Cel udostępnienia") }, minLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = consent, onCheckedChange = { consent = it; offerQr = null })
            Text("Potwierdzam świadomy wybór zakresu i celu udostępnienia danych.", color = Ink, style = MaterialTheme.typography.bodySmall)
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
        Text(sessionMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        offerQr?.let { bitmap ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Oferta sesji OWD • pozostało ${offerQrRemainingSeconds / 60}:${(offerQrRemainingSeconds % 60).toString().padStart(2, '0')}",
                        color = Navy, fontWeight = FontWeight.Bold
                    )
                    Image(bitmap.asImageBitmap(), contentDescription = "Kod QR oferty sesji OWD", modifier = Modifier.size(250.dp))
                     Text("To inicjuje sesję kryptograficzną. Skaner OWD potrafi rozpoznać i zweryfikować ofertę; wymiana kluczy, przesłanie rekordów i potwierdzenie odbioru wymagają jeszcze pełnego połączenia obu urządzeń.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        OutlinedButton(onClick = onOpenVCard, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator wizytówki vCard") }
        OutlinedButton(onClick = onOpenWebsite, modifier = Modifier.fillMaxWidth()) { Text("Otwórz generator kodu strony internetowej") }
        Text("Prywatność: dane nie są umieszczane w kodzie inicjującym. Odbiorca i cel muszą zostać zweryfikowani przed właściwym transferem. Dane rodzinne/rodowe są całkowicie wyłączone z obecnego zakresu OWD.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text(saveMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text("Kod QR strony internetowej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Kod zawiera adres strony. Zeskanowanie otworzy go w przeglądarce po zatwierdzeniu przez użytkownika.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(value = address, onValueChange = { address = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Adres strony (URL)") }, placeholder = { Text("https://example.org") }, singleLine = true)
        QrBrandingControls(branding, { branding = it }, uploadedLogo != null, { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) }, { uploadedLogo = null; message = "Usunięto logo." })
        Text(message, color = Muted, style = MaterialTheme.typography.bodySmall)
        if (qrBitmap != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kod QR strony", color = Navy, fontWeight = FontWeight.Bold)
                    Image(qrBitmap.asImageBitmap(), contentDescription = "Kod QR strony internetowej", modifier = Modifier.size(260.dp), contentScale = ContentScale.Fit)
                     Text(normalized, color = Muted, style = MaterialTheme.typography.bodySmall)
                    SaveQrImageButton(qrBitmap, "adres-url-qr.png")
                }
            }
        } else Text(if (!validUrl) "Podaj poprawny adres rozpoczynający się od http:// lub https://." else "Wybierz logo z pliku albo ustaw opcję „Bez znaku”.", color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Godło lub logo jest wyświetlane obok kodu. Sam kod pozostaje czysty, bez nakładek. Przed publikacją sprawdź skanowanie na kilku urządzeniach.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
        Text("Kod QR", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Generator kodu kontaktowego", color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Utwórz kod QR zawierający wizytówkę vCard. Zgodna aplikacja odbiorcy może odczytać dane i zaproponować zapis kontaktu.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Wizytówka kontaktowa", color = Navy, fontWeight = FontWeight.Bold)
                Text("Kod zawiera dane kontaktowe wybrane przez użytkownika.", color = Ink, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { vCardPicker.launch(arrayOf("text/vcard", "text/x-vcard", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Wczytaj plik vCard (.vcf)")
        }
        Text(importMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        QrBrandingControls(
            branding = branding,
            onBrandingChange = { branding = it },
            logoLoaded = uploadedLogo != null,
            onChooseLogo = { logoPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/*")) },
            onClearLogo = { uploadedLogo = null; logoMessage = "Usunięto logo." }
        )
        Text(logoMessage, color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Dane kontaktowe", color = Navy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Pola są domyślnie uzupełniane z profilu. Możesz je edytować przed wygenerowaniem wizytówki. Dane identyfikacyjne, takie jak PESEL i numery dokumentów, nie są kopiowane do kodu QR.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Twój kod QR", color = Navy, fontWeight = FontWeight.Bold)
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
            Text(hint, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text("Prywatność: kod ujawnia każdej osobie skanującej wszystkie zapisane w nim dane. Nie dodawaj numeru PESEL, danych dokumentu ani informacji poufnych.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
    onOpenCredentialAppearance: () -> Unit,
    onOpenImportedDocuments: () -> Unit,
    onOpenLegalInformation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Więcej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Informacje, bezpieczeństwo i ustawienia", color = Muted, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Historia działań", "Lista czynności w aplikacji", Icons.Filled.CalendarMonth, onOpenHistory)
        ProfileActionCard("Wydane certyfikaty", "Informacje o certyfikatach urządzenia", Icons.Filled.Security, onOpenCertificates)
        ProfileActionCard("Powiadomienia", "Ustawienia powiadomień", Icons.Filled.NotificationsNone, onOpenHelp)
        ProfileActionCard("Projektant dokumentów", "Tworzenie dokumentów, układ, podgląd i eksport PDF", Icons.Filled.Description, onOpenIdentityDesigner)
        ProfileActionCard("Wspólny wygląd legitymacji", "Jeden układ, rozmieszczenie, skala i typografia dla wszystkich legitymacji", Icons.Filled.Badge, onOpenCredentialAppearance)
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
        Text("Więcej", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = R.drawable.header_logo_panel,
            description = "Panel znaku i identyfikacji aplikacji"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Blue, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Użytkownik" },
                        color = Navy, fontWeight = FontWeight.Bold
                    )
                    Text("Profil użytkownika", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileActionCard("Dokumenty i dane", "Przeglądaj dane i oświadczenia", Icons.Filled.CreditCard, onOpenDocuments)
        ProfileActionCard("Zaświadczenia", "Zapisane dane dokumentów i zaświadczeń", Icons.Filled.Description, onOpenCertificates)
        ProfileActionCard("Ustawienia", "Otwórz menu zarządzania danymi", Icons.Filled.Settings, onOpenSettings)
        Text(
            "Dane profilu możesz uzupełniać i aktualizować w ustawieniach.",
            color = Muted, style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ProfileActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(title, color = Navy, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.MoreHoriz, contentDescription = "Otwórz", tint = Muted)
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
        Text("‹  Powrót do usług", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text(title, color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = when (title) {
                "Zdrowie" -> R.drawable.navigation_help
                "Polak za granicą", "Transport", "Bilety" -> R.drawable.navigation_mountain_landscape
                "Środowisko" -> R.drawable.navigation_services
                else -> R.drawable.navigation_profile
            },
            description = "Ilustracja tematyczna modułu $title"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(descriptions[title] ?: "Moduł informacyjny.", color = Ink)
            }
        }
        if (title == "Bezpieczeństwo") {
            ProfileActionCard("Dane i dokumenty", "Otwórz formularz danych", Icons.Filled.Security, onOpenDocuments)
        }
        if (title == "Zdrowie") {
            ProfileActionCard("Dane o niepełnosprawności", "Otwórz ewidencję dokumentów i wskazań", Icons.Filled.HealthAndSafety, onOpenDocuments)
        }
        Text("Dostępne funkcje i zakres danych są opisane bezpośrednio w poszczególnych modułach.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(27.dp))
        }
        IconButton(onClick = onOpenMessages) {
            Icon(painterResource(R.drawable.ic_header_mail), contentDescription = "Wiadomości",
                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(27.dp))
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
private fun IdentityDashboardCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(218.dp).clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF078FE9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9E8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.tlo_poziome_oryginal),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                "mDowód",
                modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 18.dp),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Orzeł pozostaje czytelny; znacznik nachodzi na jego dolną prawą część przy nodze.
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(78.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .size(62.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x557AA8C6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.file_5320_eagle_vector),
                        contentDescription = "Biały orzeł w koronie ze złotą koroną",
                        modifier = Modifier.size(61.dp),
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
            Text(title, color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text("ZOBACZ", color = Blue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ServiceCard(item: ServiceItem, isFavorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.14f).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.icon != null) {
                        Icon(painter = painterResource(item.icon), contentDescription = item.title, tint = item.tint, modifier = Modifier.size(24.dp))
                    } else {
                        item.fallbackIcon?.let { Icon(it, contentDescription = item.title, tint = item.tint, modifier = Modifier.size(24.dp)) }
                    }
                }
                Text(if (isFavorite) "★" else "☆", color = if (isFavorite) Color(0xFFB7791F) else Muted,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable(onClick = onFavorite).padding(3.dp))
            }
            Column {
                Text(item.title, color = Navy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(item.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}



@Composable
private fun DocumentVisibilitySettingsScreen(
    documentPlacements: Map<String, String>,
    onDocumentPlacementsChange: (Map<String, String>) -> Unit,
    documentCardStyle: String,
    onDocumentCardStyleChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ustawień", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Widoczność dokumentów", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Dla każdego dokumentu wybierz miejsce wyświetlania. Ukrycie dokumentu nie usuwa jego danych ani załączników.",
            color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text("Wygląd kafelków w oknie Dokumenty", color = Navy, fontWeight = FontWeight.Bold)
        Text("Wybierz sposób prezentacji dokumentów.", color = Muted, style = MaterialTheme.typography.bodySmall)
        listOf("large" to "Duże kafelki", "small" to "Małe kafelki", "list" to "Kafelki listy").forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (documentCardStyle == value) BlueLight else Color.Transparent)
                    .clickable { onDocumentCardStyleChange(value) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = documentCardStyle == value, onClick = { onDocumentCardStyleChange(value) })
                Text(label, color = Ink, style = MaterialTheme.typography.bodyMedium)
            }
        }
        documentDisplayOptions.forEach { (id, title, iconRes) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(42.dp))
                        Text(title, color = Navy, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    }
                    listOf("dashboard" to "Dokumenty", "other" to "Inne dokumenty", "hidden" to "Ukryj").forEach { (value, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(if (documentPlacements[id] == value) BlueLight else Color.Transparent)
                                .clickable { onDocumentPlacementsChange(documentPlacements + (id to value)) }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = documentPlacements[id] == value,
                                onClick = { onDocumentPlacementsChange(documentPlacements + (id to value)) }
                            )
                            Text(label, color = Ink, style = MaterialTheme.typography.bodyMedium)
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
        Text("‹  Powrót do ekranu głównego", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Ustawienia", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zarządzanie danymi", color = Muted,
            style = MaterialTheme.typography.bodyMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Edycja tylko w Ustawieniach", color = Navy, fontWeight = FontWeight.Bold)
                Text("Portfel dokumentów i jego szczegóły są widokami do odczytu. Formularze edycji można otworzyć wyłącznie z tego menu.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProfileActionCard(
            "Ustawienia aplikacji",
            "Motyw kolorystyczny, godło oraz tła ekranów uruchamiania i logowania",
            Icons.Filled.Settings,
            onManageApplicationAppearance
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
        Text("Kopia danych vCard", color = Navy, fontWeight = FontWeight.Bold)
        Text("Eksport obejmuje pola profilu i ewidencji, w tym załączone dane tekstowe. Plik może zawierać dane wrażliwe — przechowuj go bezpiecznie.", color = Muted, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Eksportuj wszystkie dane", "Zapisz jedno archiwum .vcf", Icons.Filled.Description, onExportAll)
        ProfileActionCard("Importuj dane z pliku vCard", "Wczytaj archiwum .vcf i zatwierdź zakres zmian", Icons.Filled.Description, onImportAll)
        if (importStatus.isNotBlank()) Text(importStatus, color = Blue, style = MaterialTheme.typography.bodySmall)
        Text("Dane profilu i dokumenty", color = Navy, fontWeight = FontWeight.Bold)
        ProfileActionCard("Odpis z rejestru podmiotów", "Wprowadzanie i aktualizacja danych podmiotów", Icons.Filled.AccountBalance, onEditRegistry)
        ProfileActionCard("Dane osobowe", "Zdjęcie profilowe, dane osoby i rodziców",
            Icons.Filled.AccountCircle, { onEditDocuments("profile") })
        ProfileActionCard("Dowód tożsamości", "Dane dokumentu tożsamości",
            Icons.Filled.CreditCard, { onEditDocuments("identity_card") })
        ProfileActionCard("Dane paszportowe", "Dane dokumentu podróży",
            Icons.Filled.Public, { onEditDocuments("passport") })
        ProfileActionCard("Dokument rodowy", "Dane ewidencji rodowej",
            Icons.Filled.Fingerprint, { onEditDocuments("family") })
        ProfileActionCard("Legitymacja poselska", "Ewidencja danych i źródeł potwierdzenia",
            Icons.Filled.AccountBalance, { onEditDocuments("parliament") })
        ProfileActionCard("Legitymacja kursanta", "Ewidencja danych uczestnika kursu lub szkolenia",
            Icons.Filled.Description, { onEditDocuments("training") })
        ProfileActionCard("Legitymacja ucznia/studenta", "Ewidencja danych uczelni i statusu",
            Icons.Filled.Badge, { onEditDocuments("student") })
        ProfileActionCard("Orzeczenia i zaświadczenia", "Ewidencja danych o rozstrzygnięciach",
            Icons.Filled.HealthAndSafety, onEditDisability)
        ProfileActionCard("Karty lojalnościowe sklepów", "Ewidencja do 10 kart, numerów i kodów kreskowych oraz QR",
            Icons.Filled.CreditCard, { onEditDocuments("loyalty_cards") })
        ProfileActionCard("Dokumenty rodowe", "Księgi, akty i rejestry Rodu", Icons.Filled.Fingerprint, { onEditOtherDocuments("family") })
        ProfileActionCard("Dokumenty kościelne", "Dokumenty liturgiczne i organizacyjne", Icons.Filled.AccountBalance, { onEditOtherDocuments("church") })
        ProfileActionCard("Dokumenty cywilne", "Akty i dokumenty cywilne", Icons.Filled.Description, { onEditOtherDocuments("civil") })
        ProfileActionCard("Dane dzieci i dane medyczne", "Edycja danych dzieci, opiekunów, alergii, grupy krwi i uwag medycznych", Icons.Filled.AccountCircle, { onEditOtherDocuments("children_health") })
        ProfileActionCard("Dane karty szczepień", "Edycja historii dawek, preparatów, serii, placówek i terminów kolejnych szczepień", Icons.Filled.HealthAndSafety, { onEditOtherDocuments("vaccines") })
        ProfileActionCard("Dokumenty firmowe i podmiotowe", "Dokumenty spółek, fundacji i organizacji", Icons.Filled.Business, { onEditOtherDocuments("business") })
        ProfileActionCard("Konta bankowe", "Osobna ewidencja rachunków prywatnych i podmiotowych", Icons.Filled.AccountBalance, { onEditOtherDocuments("bank_accounts") })
        ProfileActionCard("Dokumenty własne — pliki", "Dodawanie, porządkowanie i usuwanie importowanych plików", Icons.Filled.Description, { onEditOtherDocuments("imported") })
        ProfileActionCard("Pliki PDF i podpisy", "Import dokumentów, dołączanie plików podpisów i zarządzanie ewidencją", Icons.Filled.PictureAsPdf, { onEditOtherDocuments("signed_pdf") })
        ProfileActionCard("Wszystkie dane", "Otwórz dotychczasowy formularz zbiorczy",
            Icons.Filled.Description, { onEditDocuments("all") })
        Text("Dane możesz uzupełniać własnym oświadczeniem, wskazując ich źródło, podstawę i zakres odpowiedzialności.",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DisabilitySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fields = remember {
        mutableStateMapOf<String, String>().apply { putAll(DemoIdentityStore.load(context)) }
    }
    var saveMessage by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Ustawienia danych o niepełnosprawności", color = Navy,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Uzupełnij dane zgodnie z posiadanym dokumentem lub informacją źródłową.", color = Muted, style = MaterialTheme.typography.bodySmall)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Źródło dokumentu", color = Navy, fontWeight = FontWeight.Bold)
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
                            .background(if (selected) BlueLight else Color(0xFFF3F4EF))
                            .border(1.dp, if (selected) Blue else Border, RoundedCornerShape(9.dp))
                            .clickable { fields["disabilityAuthorityType"] = value }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null, tint = if (selected) Blue else Muted)
                        Text(label, modifier = Modifier.padding(start = 9.dp), color = Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                DemoDataField("disabilityAuthorityName", "Organ / podmiot / wystawca", fields)
                DemoDataField("disabilityIssuerName", "Osoba podpisująca / wystawca", fields)
                DemoDataField("disabilityLegalBasis", "Podstawa prawna lub regulacyjna", fields)
                DemoDataField("disabilityFoundationStatute", "Statut / uchwała fundacji (jeśli dotyczy)", fields)
                Text("Dane osoby i dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityPersonName", "Imię i nazwisko", fields)
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator", fields)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / zaświadczenia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                Text("Ustalony stopień i okres", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityCategory", "Kategoria", fields)
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki")
                DemoDataField("disabilityCauseCodes", "Symbole / przyczyny", fields)
                DemoDataField("disabilityValidity", "Okres obowiązywania", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data ustalenia niepełnosprawności", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("disabilityEmployment", "Zatrudnienie", fields)
                DemoDataField("disabilityTraining", "Szkolenie", fields)
                DemoDataField("disabilityTherapy", "Terapia / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Sprzęt i środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Wsparcie środowiskowe", fields)
                DemoDataField("disabilityParking", "Karta parkingowa — wskazania", fields)
                Text("Metodologia i ocena funkcjonalna (opcjonalnie)", color = Navy, fontWeight = FontWeight.Bold)
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
                if (saveMessage.isNotBlank()) Text(saveMessage, color = Blue, style = MaterialTheme.typography.bodySmall)
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
        Text("‹  Powrót", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Zaświadczenia", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zapisane dane dokumentów i zaświadczeń", color = Muted, style = MaterialTheme.typography.bodySmall)
        if (hasDisabilityData) {
            Text("Orzeczenie / zaświadczenie o niepełnosprawności", color = Navy, fontWeight = FontWeight.Bold)
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
                        Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
                        Text(value, color = Ink, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(190.dp))
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                    Text("Brak zapisanych danych", color = Navy, fontWeight = FontWeight.Bold)
                    Text("Dodaj dane o niepełnosprawności w Ustawieniach. Po zapisaniu pojawią się tutaj.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
    onOpenDetails: (String) -> Unit,
    onOpenService: (String) -> Unit
) {
    data class CatalogDocument(
        val id: String,
        val title: String,
        val subtitle: String,
        val categories: List<String>,
        val detailsIndex: String? = null,
        val service: String? = null,
        val icon: ImageVector = Icons.Filled.Description
    )

    val documents = listOf(
        CatalogDocument("profile", "Dane osobowe profilu", "Dane kontaktowe i podstawowe dane rodzinne", listOf("Osobiste", "Cywilne", "Rodowe"), "0", icon = Icons.Filled.AccountCircle),
        CatalogDocument("identity", "Dowód tożsamości", "Dane dokumentu tożsamości i identyfikacyjne", listOf("Państwowe", "Cywilne", "Rodowe", "OC", "Osobiste"), "1", icon = Icons.Filled.CreditCard),
        CatalogDocument("passport", "Dane paszportowe", "Dane paszportu i status dokumentu", listOf("Państwowe", "Cywilne", "Rodowe", "Osobiste"), "2", icon = Icons.Filled.Public),
        CatalogDocument("registry", "Odpis z rejestru podmiotów", "Podgląd danych podmiotów i wpisów rejestrowych", listOf("Państwowe", "Cywilne", "Kościelne", "Rodowe", "Rejestry"), service = "Odpis z rejestru podmiotów", icon = Icons.Filled.AccountBalance),
        CatalogDocument("family", "Dokument rodowy", "Księgi, akty i dane ewidencji Rodu", listOf("Cywilne", "Kościelne", "Rodowe", "OC", "Osobiste", "Rejestry"), "3", icon = Icons.Filled.Fingerprint),
        CatalogDocument("parliament", "Legitymacja poselska", "Dane legitymacji i kadencji", listOf("Państwowe", "Cywilne", "Osobiste", "Zawodowe"), "4", icon = Icons.Filled.AccountBalance),
        CatalogDocument("training", "Legitymacja kursanta", "Dane uczestnictwa w kursie i instytucji szkolącej", listOf("Osobiste", "Zawodowe", "Rejestry"), "5", icon = Icons.Filled.Description),
        CatalogDocument("student", "Legitymacja ucznia / studenta", "Dane edukacyjne i identyfikator legitymacji", listOf("Państwowe", "Osobiste", "Zawodowe"), "6", icon = Icons.Filled.Badge),
        CatalogDocument("disability", "Orzeczenia i zaświadczenia", "Dane orzeczeń, stopnia i okresu obowiązywania", listOf("Państwowe", "Cywilne", "OC", "Zdrowotne", "Osobiste", "Zawodowe", "Rejestry"), "7", icon = Icons.Filled.HealthAndSafety),
        CatalogDocument("church", "Dokumenty kościelne", "Dokumenty kościelne, liturgiczne i organizacyjne", listOf("Kościelne", "Rodowe", "Osobiste", "Zawodowe", "Rejestry"), service = "Dokumenty Kościelne", icon = Icons.Filled.AccountBalance),
        CatalogDocument("civil", "Dokumenty cywilne", "Formularze, oświadczenia i akty cywilne", listOf("Państwowe", "Cywilne", "Rodowe", "Osobiste", "Rejestry"), service = "Formularze dokumentów", icon = Icons.Filled.Description),
        CatalogDocument("business", "Dokumenty firmowe i podmiotowe", "Dane organizacji, firm i jednostek", listOf("Cywilne", "Kościelne", "Rodowe", "Zawodowe", "Rejestry"), service = "Dokumenty rejestru podmiotów", icon = Icons.Filled.Business),
        CatalogDocument("bank_accounts", "Konta bankowe", "Ewidencja rachunków bankowych", listOf("Państwowe", "Cywilne", "Osobiste", "Rejestry"), service = "Konta bankowe", icon = Icons.Filled.Payments),
        CatalogDocument("vaccines", "Książeczka szczepień WHO", "Historia szczepień i dawek", listOf("Zdrowotne", "Osobiste", "Rodowe"), service = "eKarta Szczepień", icon = Icons.Filled.HealthAndSafety),
        CatalogDocument("signed_pdf", "Dokumenty PDF i podpisy", "Pliki PDF, podgląd oraz ewidencja importu podpisanych dokumentów", listOf("Państwowe", "Cywilne", "Kościelne", "Rodowe", "OC", "Zdrowotne", "Osobiste", "Zawodowe", "Rejestry"), service = "Dokumenty PDF i podpisy elektroniczne", icon = Icons.Filled.PictureAsPdf),
        CatalogDocument("branding", "Zasoby identyfikacji dokumentów", "Flagi, godła, herby, pieczęcie, logo i grafiki dokumentów", listOf("Państwowe", "Kościelne", "Rodowe", "Zawodowe", "Rejestry"), service = "Zasoby identyfikacji dokumentów", icon = Icons.Filled.Description),
        CatalogDocument("imported", "Dokumenty własne — pliki", "Importowane pliki PDF, DOCX i obrazy", listOf("Państwowe", "Cywilne", "Kościelne", "Rodowe", "OC", "Zdrowotne", "Osobiste", "Zawodowe", "Rejestry"), service = "Dokumenty własne — pliki", icon = Icons.Filled.Description),
        CatalogDocument("children", "Dokumentacja dzieci", "Rodzinna ewidencja danych i dokumentacji dzieci", listOf("Cywilne", "Rodowe", "OC", "Zdrowotne", "Osobiste", "Rejestry"), service = "Dane dzieci", icon = Icons.Filled.AccountCircle),
        CatalogDocument("loyalty", "Karty lojalnościowe sklepów", "Do 25 kart, numery i kody QR / kreskowe", listOf("Osobiste", "Cywilne"), "8", icon = Icons.Filled.CreditCard)
    )
    val visibleDocuments = documents.filter { documentPlacements[it.id] == "dashboard" || documentPlacements[it.id] == "other" }
    val categories = listOf("Państwowe", "Cywilne", "Kościelne", "Rodowe", "OC", "Zdrowotne", "Osobiste", "Zawodowe", "Rejestry")

    fun openDocument(document: CatalogDocument) {
        when {
            document.detailsIndex != null -> onOpenDetails(document.detailsIndex)
            document.service != null -> onOpenService(document.service)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Pulpit", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Dokumenty", color = Navy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Wybierz dokument. Grupy porządkują katalog, a każda pozycja otwiera bezpośrednio podgląd dokumentu.",
            color = Muted, style = MaterialTheme.typography.bodySmall
        )
        categories.forEach { category ->
            val entries = visibleDocuments.filter { category in it.categories }
            if (entries.isNotEmpty()) {
                Text(category, color = Navy, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp))
                if (documentCardStyle == "list") {
                    entries.forEach { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { openDocument(document) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Icon(document.icon, contentDescription = null, tint = Blue, modifier = Modifier.size(27.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(document.title, color = Navy, fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text(document.subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Podgląd  ›", color = Blue, style = MaterialTheme.typography.labelSmall)
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
                                    colors = CardDefaults.cardColors(containerColor = White)
                                ) {
                                    Box(Modifier.fillMaxSize().padding(10.dp)) {
                                        Icon(document.icon, contentDescription = null, tint = Blue,
                                            modifier = Modifier.align(Alignment.TopStart).size(if (documentCardStyle == "small") 23.dp else 30.dp))
                                        Image(
                                            painter = painterResource(R.drawable.file_5320_eagle_vector),
                                            contentDescription = "Orzeł — znak dokumentu",
                                            modifier = Modifier.align(Alignment.TopEnd).size(if (documentCardStyle == "small") 24.dp else 32.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(document.title, color = Navy, fontWeight = FontWeight.SemiBold,
                                                style = if (documentCardStyle == "small") MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            if (documentCardStyle == "large") Text(document.subtitle, color = Muted,
                                                style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
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
    if (settings.showCard) {
        UnifiedCredentialCard(
            documentKey = documentKey, title = title, subtitle = subtitle, holderName = holder,
            fields = credentialFields.ifEmpty { listOf(Triple("details", "DANE DOKUMENTU", details)) }, settings = settings,
            fallbackBackgroundRes = imageRes,
            validity = validity
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
            Text("Źródła i informacje dodatkowe", color = Navy, fontWeight = FontWeight.Bold)
            if (populated.isEmpty()) {
                Text("Brak wpisów dotyczących źródła, podstawy, weryfikacji lub adnotacji.", color = Muted, style = MaterialTheme.typography.bodySmall)
            } else {
                populated.forEach { (label, key) ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                        Text(fields[key].orEmpty(), color = Navy, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Text("Oświadczenie użytkownika opisuje dane zgodnie z jego wiedzą oraz wskazaną podstawą i źródłem.", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private data class CredentialFieldVisibilityBinding(
    val documentKey: String,
    val settings: MutableState<LegitimacyPreviewSettings>,
    val fields: MutableMap<String, String>
)
private val LocalCredentialFieldVisibility = androidx.compose.runtime.compositionLocalOf<CredentialFieldVisibilityBinding?> { null }

@Composable
private fun DemoDocumentsScreen(categoryId: String = "all", onBack: () -> Unit) {
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
            color = Blue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            editorTitle,
            color = Navy,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (categoryId in setOf("identity_card", "parliament", "training", "student", "disability", "passport")) {
            GeneralDocumentOcrEditor(categoryId, fields, context) { saveMessage = it }
        }
         if (categoryId == "all" || categoryId == "profile") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zdjęcie profilowe", color = Navy, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F3F6))
                        .border(1.dp, Border, RoundedCornerShape(12.dp)),
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
                        else Text("Nie można wyświetlić zdjęcia", color = Muted)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Muted, modifier = Modifier.size(42.dp))
                            Text("MIEJSCE NA ZDJĘCIE", color = Muted, fontWeight = FontWeight.Bold)
                            Text("Nie wybrano fotografii", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Wybierz zdjęcie", color = Blue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { selectedPhotoField = "photo"; photoPicker.launch("image/*") }.padding(vertical = 8.dp))
                    if (photoBase64.isNotBlank()) Text("Usuń zdjęcie", color = Color(0xFF9B2C2C), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { photoBase64 = ""; fields["photo"] = "" }.padding(vertical = 8.dp))
                }
                Text("Pozostałe fotografie", color = Navy, fontWeight = FontWeight.Bold)
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
                Text("Dane osobowe", color = Navy, fontWeight = FontWeight.Bold)
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
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rodowy dokument tożsamości — dane dokumentu", color = Navy, fontWeight = FontWeight.Bold)
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
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Podstawowe dane identyfikacyjne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("firstNames", "Imię / imiona", fields)
                DemoDataField("lastName", "Nazwisko", fields)
                DemoDataField("birthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("birthPlace", "Miejsce urodzenia", fields)
                DemoDataField("citizenship", "Obywatelstwo", fields)
                DemoDataField("sex", "Płeć (dane deklarowane)", fields)
                DemoDataField("birthSurname", "Nazwisko rodowe", fields)
                DemoDataField("pesel", "Numer PESEL", fields)
                Text("Dane samego dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("idNumber", "Numer dokumentu tożsamości", fields)
                DemoDataField("idIssueDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("idExpiryDate", "Data ważności", fields, "DD-MM-RRRR")
                DemoDataField("idIssuer", "Organ wydający", fields)
                DemoDataField("idStatus", "Status dokumentu", fields, "ważny / utracony / unieważniony / wymieniony")
                DemoDataField("idPreviousNumber", "Numer poprzedniego dokumentu (jeśli dotyczy)", fields)
                DemoDataField("idLossOrInvalidationDate", "Data utraty / unieważnienia", fields, "DD-MM-RRRR")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2D9))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dane identyfikacyjne i dokumentowe", color = Navy, fontWeight = FontWeight.SemiBold)
                        Text("Uzupełnij dane zgodnie z dokumentem źródłowym, w tym numer, organ wydający i terminy ważności.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja poselska — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("mpCardNumber", "Numer ewidencyjny legitymacji", fields)
                DemoDataField("mpTerm", "Kadencja", fields)
                DemoDataField("mpDistrict", "Okręg wyborczy", fields)
                DemoDataField("mpStart", "Data rozpoczęcia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpEnd", "Data zakończenia mandatu", fields, "DD-MM-RRRR")
                DemoDataField("mpCardIssueDate", "Data wydania legitymacji", fields, "DD-MM-RRRR")
                DemoDataField("mpIssuer", "Organ / osoba wystawiająca", fields)
                DemoDataField("mpStatus", "Status legitymacji", fields, "aktywna / zwrócona / unieważniona")
                DemoDataField("mpMandateSource", "Źródło potwierdzenia mandatu", fields)
                Text("Dane dotyczące ślubowania — oświadczenie własne", color = Navy, fontWeight = FontWeight.Bold)
                DemoDataField("mpOathDate", "Data złożenia ślubowania", fields, "DD-MM-RRRR")
                DemoDataField("mpOathPlace", "Miejsce / okoliczności wypowiedzenia ślubowania", fields)
                DemoDataField("mpOathEvidence", "Źródło potwierdzenia (np. protokół, nagranie, publikacja)", fields)
                Text("Podgląd cyfrowej legitymacji", color = Navy, fontWeight = FontWeight.Bold)
                ParliamentaryCredentialPreview(fields)
                AdditionalInformationSection("Informacje dodatkowe — legitymacja poselska", fields, "mpExtra", "Art. 104 Konstytucji RP określa wolny mandat poselski oraz obowiązek złożenia ślubowania przed Sejmem przed rozpoczęciem sprawowania mandatu. Odmowa ślubowania oznacza zrzeczenie się mandatu. Sam wpis ewidencyjny nie potwierdza uzyskania mandatu ani prawa do reprezentowania Sejmu.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "training") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja uczestnika kursu — instytucja szkoleniowa", color = Navy, fontWeight = FontWeight.Bold)
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
                Text("Podgląd legitymacji", color = Navy, fontWeight = FontWeight.Bold)
                SchoolLegitimacyPreview(fields, photoBase64, student = false)
                AdditionalInformationSection("Informacje dodatkowe — legitymacja ucznia/studenta", fields, "trainingExtra", "Legitymacja instytucji szkoleniowej potwierdza status w ewidencji wystawcy w granicach regulaminu i właściwej podstawy działania. Nie jest legitymacją studencką i sama nie nadaje statusu studenta uczelni.")
            }
        }
        }
        if (categoryId == "all" || categoryId == "student") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Legitymacja ucznia/studenta — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
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
                Text("Podgląd legitymacji", color = Navy, fontWeight = FontWeight.Bold)
                SchoolLegitimacyPreview(fields, photoBase64, student = true)
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
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Orzeczenie o niepełnosprawności — dane ewidencyjne", color = Navy, fontWeight = FontWeight.Bold)
                Text("Źródło ustalenia / dokumentu", color = Navy, fontWeight = FontWeight.SemiBold)
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
                                .background(if (selected) BlueLight else Color(0xFFF3F4EF))
                                .border(1.dp, if (selected) Blue else Border, RoundedCornerShape(9.dp))
                                .clickable { fields["disabilityAuthorityType"] = value }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null, tint = if (selected) Blue else Muted
                            )
                            Text(label, modifier = Modifier.padding(start = 9.dp), color = if (selected) Navy else Ink,
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
                        Text("Oświadczenie własne zapisuje deklarację osoby. Samo w sobie nie jest orzeczeniem ustawowym; jego znaczenie zależy od przepisu lub procedury, w której jest składane.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Dane osoby i rozstrzygnięcia", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityDecisionNumber", "Numer orzeczenia / rozstrzygnięcia", fields)
                DemoDataField("disabilityDecisionDate", "Data wydania", fields, "DD-MM-RRRR")
                DemoDataField("disabilityDecisionPlace", "Miejsce wydania", fields)
                DemoDataField("disabilityPersonName", "Osoba, której dotyczy orzeczenie", fields, "Imię i nazwisko")
                DemoDataField("disabilityBirthDate", "Data urodzenia", fields, "DD-MM-RRRR")
                DemoDataField("disabilityPeselOrId", "PESEL / identyfikator ewidencyjny", fields)
                Text("Ustalony status / stopień", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityCategory", "Kategoria", fields, "np. osoba do 16 lat / osoba powyżej 16 lat")
                DemoDataField("disabilityDegree", "Stopień niepełnosprawności", fields, "znaczny / umiarkowany / lekki / nie dotyczy")
                DemoDataField("disabilityCauseCodes", "Symbole przyczyn (maks. 3 w orzeczeniu powiatowym)", fields, "np. 05-R, 10-N")
                DemoDataField("disabilityValidity", "Okres, na jaki ustalono status", fields, "na stałe / do DD-MM-RRRR")
                DemoDataField("disabilityFromDate", "Data, od której ustalono niepełnosprawność", fields, "DD-MM-RRRR")
                Text("Wskazania i potrzeby wsparcia", color = Navy, fontWeight = FontWeight.SemiBold)
                DemoDataField("disabilityEmployment", "Wskazania dotyczące zatrudnienia", fields)
                DemoDataField("disabilityTraining", "Wskazania dotyczące szkolenia", fields)
                DemoDataField("disabilityTherapy", "Terapia zajęciowa / rehabilitacja", fields)
                DemoDataField("disabilityAssistiveDevices", "Przedmioty ortopedyczne / środki pomocnicze", fields)
                DemoDataField("disabilityCare", "Opieka / pomoc innej osoby", fields)
                DemoDataField("disabilityMobility", "Środowiskowy system wsparcia", fields)
                DemoDataField("disabilityParking", "Wskazania dotyczące karty parkingowej", fields)
                Text("Skład i podpisy", color = Navy, fontWeight = FontWeight.SemiBold)
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
                Text("Podgląd dokumentu", color = Navy, fontWeight = FontWeight.Bold)
                DisabilityDecisionPreview(fields)
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
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dane paszportowe", color = Navy, fontWeight = FontWeight.Bold)
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
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Konfiguracja i zapis", color = Navy, fontWeight = FontWeight.Bold)
                Text("Dane i zdjęcie są zabezpieczane przed nieuprawnionym dostępem.", color = Muted, style = MaterialTheme.typography.bodySmall)
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
                if (saveMessage.isNotBlank()) Text(saveMessage, color = Blue, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (categoryId != "all") {
            Text("Grafiki przypisane do tego dokumentu", color = Navy, fontWeight = FontWeight.Bold)
            LegitimacyPreviewSettingsPanel(
                documentKey = credentialKey,
                settings = appearance.value,
                onChange = { appearance.value = it },
                assetSelectionOnly = true
            )
        }
        Text(
            "Dane są zabezpieczane z użyciem mechanizmów systemu Android. Zachowaj ostrożność: mogą zawierać informacje wrażliwe.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
    }
    }
}

@Composable
private fun DisabilityDecisionPreview(fields: Map<String, String>) {
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
    val status = if (student) fields["studentStatus"].orEmpty().trim().lowercase() else fields["trainingStatus"].orEmpty().trim().lowercase()
    val explicitlyInvalid = status in setOf("nieważny", "nieważna", "unieważniony", "unieważniona", "zwrócony", "zwrócona", "utracony", "utracona", "expired", "invalid")
    val valid = !explicitlyInvalid && isCredentialValidByExpiry(validity)
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
    val status = fields["mpStatus"].orEmpty().trim().lowercase()
    val explicitlyInvalid = status in setOf("nieważny", "nieważna", "unieważniony", "unieważniona", "zwrócony", "zwrócona", "utracony", "utracona", "expired", "invalid")
    val valid = !explicitlyInvalid && isCredentialValidByExpiry(expiry)
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
    val statusKey = when (documentId) {
        "1" -> "idStatus"; "2" -> "passportStatus"; "4" -> "mpStatus"
        "5" -> "trainingStatus"; "6" -> "studentStatus"; "7" -> "disabilityStatus"
        else -> "familyStatus"
    }
    val expiryKey = when (documentId) {
        "1" -> "idExpiryDate"; "2" -> "passportExpiryDate"; "4" -> "mpEnd"
        "5" -> "trainingValidUntil"; "6" -> "studentValidity"; "7" -> "disabilityValidity"
        else -> "familyExpiryDate"
    }
    val rawStatus = fields[statusKey].orEmpty().trim().lowercase()
    val explicitlyInvalid = rawStatus in setOf(
        "nieważny", "nieważna", "nieważne", "unieważniony", "unieważniona",
        "zwrócona", "zwrócony", "utracony", "utracona", "expired", "invalid"
    )
    CredentialValidityTile(!explicitlyInvalid && isCredentialValidByExpiry(fields[expiryKey].orEmpty()))
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
        Text("‹  Menu główne", color = Blue, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Odpis z rejestru podmiotów", color = Navy, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(if (editable) "Wprowadzanie i aktualizacja danych podmiotów. Wybierz rodzaj podmiotu i uzupełnij dane na podstawie właściwego dokumentu lub rejestru." else "Podgląd zapisanych danych identyfikacyjnych i rejestrowych podmiotów. Wybierz kafelek, aby wyświetlić szczegóły.",
            color = Muted, style = MaterialTheme.typography.bodyMedium)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC))) {
            Text(if (editable) "Wpisy są przechowywane lokalnie jako dane wprowadzone przez użytkownika. Podaj źródło i datę sprawdzenia." else "Dane zapisane w ustawieniach. W razie potrzeby porównaj je z właściwym rejestrem; aplikacja nie pobiera urzędowego odpisu.",
                Modifier.padding(14.dp), color = Navy, style = MaterialTheme.typography.bodySmall)
        }
        if (selectedEntity == null) {
            Text("Wybierz rodzaj podmiotu", color = Navy, fontWeight = FontWeight.Bold,
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
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null,
                                tint = Blue, modifier = Modifier.size(30.dp))
                            Text(label, color = Navy, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Otwórz dokument  ›", color = Blue,
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val entityKey = selectedEntity!!
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹  Wszystkie rodzaje podmiotów", color = Blue, fontWeight = FontWeight.SemiBold,
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
            Text(entityTitle, color = Navy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DocumentIdentityTemplate(
                entityTitle,
                entityValues,
                owner = entityFields.firstOrNull()?.let { fields[it.first].orEmpty() }.orEmpty()
            )
            entityFields.forEach { (key, label, placeholder) ->
                if (editable) DemoDataField(key, label, fields, placeholder)
                else RegistryReadOnlyField(label, fields[key])
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
            Text("Źródło i kontrola aktualności", color = Navy, fontWeight = FontWeight.Bold)
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
                if (status.isNotBlank()) Text(status, color = if (status.startsWith("Zapisano")) Blue else Color(0xFFB3261E), style = MaterialTheme.typography.bodySmall)
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
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
        Text(value?.takeIf { it.isNotBlank() } ?: "Brak zapisanego wpisu", color = if (value.isNullOrBlank()) Muted else Navy,
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
                Icon(Icons.Filled.Info, contentDescription = null, tint = Blue)
                Text(title, modifier = Modifier.weight(1f).padding(start = 9.dp),
                    color = Navy, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "Zwiń" else "Rozwiń", color = Blue,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                if (explanation.isNotBlank()) Text(explanation, color = Ink, style = MaterialTheme.typography.bodySmall)
                DemoDataField("${prefix}Source", "Źródło danych / dokument źródłowy", fields)
                DemoDataField("${prefix}LegalBasis", "Podstawa prawna / regulaminowa", fields)
                DemoDataField("${prefix}VerifiedAt", "Data ostatniej weryfikacji danych", fields, "DD-MM-RRRR")
                DemoDataField("${prefix}VerifiedBy", "Podmiot / osoba potwierdzająca dane", fields)
                DemoDataField("${prefix}Notes", "Adnotacje i informacje uzupełniające", fields)
                Text(
                    "Informacje dodatkowe uzupełniają zapis danych o źródło, podstawę i datę weryfikacji.",
                    color = Muted, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DemoDataField(key: String, label: String, fields: MutableMap<String, String>, placeholder: String = "Wpisz dane") {
    val binding = LocalCredentialFieldVisibility.current
    val settings = binding?.settings?.value
    val checked = settings?.let { !it.visibleFieldsConfigured || key in it.visibleFields } ?: true
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
                    val current = if (settings?.visibleFieldsConfigured == true) settings.visibleFields
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

private object DemoIdentityStore {
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
