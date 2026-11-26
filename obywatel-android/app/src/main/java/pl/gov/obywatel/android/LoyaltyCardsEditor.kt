// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.UUID

@Composable
internal fun LoyaltyCardsEditor(
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

