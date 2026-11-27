SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Document Processing Engine — architektura docelowa

## Cel

Wspólny, testowalny moduł aplikacji Android obsługujący wejścia: aparat, zdjęcie, PDF i tekst; oraz operacje OCR, odczytu kodów, ekstrakcji pól, walidacji i tłumaczenia. Moduły domenowe nie mogą bezpośrednio zależeć od ML Kit, ZXing ani UI.

## Granice modułów

Proponowany pakiet: `pl.gov.obywatel.android.documentprocessing`.

- `api/`: publiczne kontrakty, modele wejścia/wyjścia, błędy.
- `core/`: orkiestracja pipeline'u, polityka prywatności, limity i anulowanie.
- `image/`: przygotowanie obrazu i metadane; bez nowej biblioteki w pierwszym etapie.
- `ocr/`: adapter ML Kit Text Recognition.
- `barcode/`: jeden wybrany adapter skanowania; generowanie kodów pozostaje osobnym przypadkiem użycia.
- `translation/`: adapter obecnego ML Kit Translation i mapowanie języków.
- `extraction/`: obecny parser dokumentów jako logika domenowa bez zależności od Android UI.
- `validation/`: walidatory pól, MRZ, PESEL, IBAN, dat i reguł specyficznych dla dokumentu.
- `storage/`: wyłącznie interfejsy; integracja z istniejącym repozytorium dokumentów po zatwierdzeniu użytkownika.
- `ui/`: Compose screens i komponenty, wydzielone z dużych plików.

Nie tworzyć nowego modułu Gradle, dopóki granice pakietów i zależności nie zostaną ustabilizowane. Pierwszy etap może pozostać w istniejącym module app, ale z czystymi granicami pakietów.

## Kontrakty

```kotlin
data class ProcessingRequest(
    val source: DocumentSource,
    val operations: Set<ProcessingOperation>,
    val documentTypeHint: String? = null,
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null
)

sealed interface DocumentSource {
    data class ImageUri(val uri: Uri) : DocumentSource
    data class PdfUri(val uri: Uri) : DocumentSource
    data class PlainText(val value: String) : DocumentSource
}

enum class ProcessingOperation { OCR, BARCODE, EXTRACT_FIELDS, VALIDATE, TRANSLATE }

data class ProcessingResult(
    val text: OcrText? = null,
    val barcodes: List<BarcodeValue> = emptyList(),
    val fields: List<ExtractedField> = emptyList(),
    val validations: List<FieldValidation> = emptyList(),
    val translation: TranslationResult? = null,
    val provenance: ProcessingProvenance
)

data class ExtractedField(
    val key: String,
    val value: String,
    val confidence: Float?,
    val sourceRegion: BoundingBox?,
    val requiresReview: Boolean = true
)
```

Powyższe to kontrakt projektowy; typy `Uri`, prostokąt i modele wynikowe należy dopasować do istniejących abstrakcji. Warstwa czysta JVM nie może importować Android SDK. W takim przypadku źródło powinno mieć abstrakcyjny identyfikator/strumień dostarczany przez adapter Android.

## Interfejsy adapterów

- `TextRecognizerEngine.recognize(input): ProcessingTask<OcrText>`
- `BarcodeReaderEngine.read(input): ProcessingTask<List<BarcodeValue>>`
- `TextTranslator.translate(text, source, target): ProcessingTask<TranslationResult>`
- `DocumentFieldExtractor.extract(text, typeHint): List<ExtractedField>`
- `FieldValidator.validate(fields, typeHint): List<FieldValidation>`

Adaptery ML Kit i ZXing implementują interfejsy; domena nie zna klas dostawców. Zadania muszą wspierać anulowanie, timeout, kontrolowane zamykanie zasobów i jednoznaczne błędy.

## Pipeline

1. Przyjęcie pliku przez SAF/URI i kontrola MIME/rozmiaru.
2. Odczyt strumienia bez niepotrzebnego kopiowania całego pliku do pamięci.
3. Normalizacja orientacji i ograniczenie rozdzielczości do wymagań silnika.
4. Równoległe OCR/barcode tylko wtedy, gdy użytkownik wybrał obie operacje i urządzenie ma zasoby.
5. Zachowanie bloków, linii, bounding boxes i confidence z OCR.
6. Ekstrakcja pól wg typu dokumentu.
7. Walidacja składniowa i kontrolna; nie deklarować autentyczności.
8. Opcjonalne tłumaczenie tekstu z zachowaniem oryginału.
9. Podgląd oryginału, wyniku i niepewnych pól.
10. Jawna akceptacja użytkownika, dopiero potem przekazanie do istniejącego repozytorium dokumentów.

## Prywatność i bezpieczeństwo

- Przetwarzanie lokalne jako ustawienie domyślne.
- Żadnego automatycznego wysyłania zdjęć lub tekstu dokumentów do API.
- URI permissions ograniczone do czasu potrzebnego przetwarzania.
- Pliki tymczasowe usuwane po zakończeniu/anulowaniu.
- Brak treści dokumentów w logach, crash reports i analityce.
- Wynik OCR i tłumaczenie oznaczone jako niezweryfikowane.
- Nie nadpisywać oryginału.
- Użytkownik zatwierdza zapis danych do portfela.
- Szyfrowanie i uprawnienia wykorzystują istniejący mechanizm aplikacji; nie wprowadzać drugiego magazynu kluczy.

## Refaktoryzacja Kotlin

Najpierw rozdzielić odpowiedzialności w `QrAndOwdScreens.kt` i `OcrAndBarcodeComponents.kt`:

- `QrToolsScreen.kt`: routing i stan ekranu.
- `QrGenerationUseCase.kt`: generowanie QR i ustawienia brandingowe.
- `BarcodeGenerationUseCase.kt`: generowanie kodów 1D.
- `BarcodeScanLauncher.kt`: integracja kontraktu skanera z Activity Result.
- `DocumentProcessingScreen.kt`: UI procesu dokumentu.
- `OcrEngine.kt` i `MlKitTextRecognizer.kt`: kontrakt i adapter.
- `BarcodeReader.kt` i wybrany adapter.
- `DocumentOcrParser.kt`: pozostaje czystą logiką ekstrakcji; podzielić reguły na katalogi typów dokumentów dopiero po testach zgodności.
- `MlKitMedicalTranslationProvider.kt`: zachować jako adapter; wspólny kontrakt nie powinien wymuszać nazwy medycznej.

Nie wykonywać wielkiego przeniesienia plików ani zmian nazw publicznych symboli w jednym PR. Po każdym wydzieleniu sprawdzić wszystkie referencje, kompilację i testy.

## Integracja z modułami

Wyniki silnika przekazywać przez przypadki użycia do:
- portfela dokumentów,
- kontaktów i vCard,
- rachunków płatniczych (paragony/faktury/wyciągi),
- certyfikatów i dokumentów podpisanych,
- tłumaczenia dokumentów medycznych.

Nie wiązać silnika bezpośrednio z ekranami tych modułów. Każda integracja osobno i po akceptacji użytkownika.

## Stan wdrożenia i zgodność prototypu — 2026-11-27

Istnieje prototyp adaptera w osobnym PR #188: DocumentImageRecognition.kt opakowuje ML Kit Text Recognition i Barcode Scanning dla wejściowego Bitmap. Jest to pierwszy adapter techniczny, nie kompletna realizacja kontraktów z niniejszego dokumentu. Obecny interfejs zwraca surowy tekst oraz listę podstawowych wartości kodów; nie zachowuje jeszcze bloków, linii, współrzędnych, confidence ani pochodzenia wyniku. Przed integracją należy rozszerzyć model OCR i dodać jawny parametr obrotu obrazu lub udokumentować wymaganie wejściowego obrazu ustawionego pionowo.

Nie ma jeszcze kompletnego wejścia SAF/PDF, wielostronicowego pipeline'u, ekstrakcji i walidacji w jednym przebiegu ani ekranu podglądu z zatwierdzeniem. Nie deklarować gotowości silnika, dopóki te granice nie zostaną zaimplementowane i przetestowane. PR #186 wydziela generowanie QR z ekranu, lecz nie realizuje jeszcze warstwy BarcodeReaderEngine.

Kolejność techniczna: (1) doprowadzić do zielonego build/testy dla ekstrakcji QR i adaptera obrazu, (2) dodać testy adaptera i modelu wyniku, (3) zdefiniować stabilne kontrakty niezależne od Android/ML Kit, (4) zintegrować wejście obrazu i parser, (5) dopiero potem UI zatwierdzania i repozytorium dokumentów.

## Pierwsza implementacja kontraktów — 2026-11-27

W tej gałęzi dodano pierwszy kod funkcjonalny:
- `documentprocessing/DocumentProcessingContracts.kt` — platform-neutralne modele żądań i wyników, operacje, metadane źródła, bloki/linie OCR, prostokąty, wartości kodów, pola, walidacje, tłumaczenie i pochodzenie wyniku.
- Interfejsy dostawców: `TextRecognizerEngine`, `BarcodeReaderEngine`, `TextTranslator`, `DocumentFieldExtractor`, `FieldValidator`.
- Kontrakty nie importują Android SDK, ML Kit, ZXing ani Compose. `ProcessingInput` jest nieprzezroczystym wejściem dostarczanym przez warstwę platformową.
- Domyślnie każde pole ekstrakcji wymaga ręcznego sprawdzenia; confidence jest ograniczone do zakresu 0..1; wynik walidacji nie oznacza autentyczności dokumentu.
- Test JVM `DocumentProcessingContractsTest` obejmuje walidację pustych operacji, wymóg języka docelowego, confidence, granice, ręczny przegląd pól i pochodzenie.

To jest warstwa kontraktów, nie jeszcze orkiestrator ani integracja z adapterem ML Kit. Nie należy deklarować działającego przepływu end-to-end. Testy zostały zapisane w repozytorium, ale ich wykonanie wymaga CI/Gradle; brak jeszcze potwierdzonego wyniku kompilacji po tym commicie.
