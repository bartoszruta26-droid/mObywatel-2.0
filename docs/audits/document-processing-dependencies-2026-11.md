SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Audyt zależności przetwarzania dokumentów — 2026-11-27

## Zakres i podstawa audytu

Audyt wykonano na gałęzi `main`, commit `0a06b07d74548a43170685173571b976a91d4515`. Zakres: moduł Android `obywatel-android/app`, konfiguracja Gradle i wskazane pliki Kotlin. Audyt nie obejmuje uruchomienia APK ani pomiaru runtime na urządzeniu; wnioski o wykonaniu funkcji rozróżniają obecność zależności, implementację kodu i potwierdzenie działania.

## Zidentyfikowane zależności

Z `obywatel-android/app/build.gradle.kts`:

| Zależność | Rola | Wniosek |
|---|---|---|
| `com.google.mlkit:text-recognition:16.0.1` | OCR tekstu | Zależność obecna; plik `QrAndOwdScreens.kt` importuje `TextRecognition`, `InputImage` i `TextRecognizerOptions`. W przeanalizowanym pliku nie znaleziono wywołania `TextRecognition.getClient` ani `process`; aktywne OCR nie jest potwierdzone. |
| `com.google.mlkit:barcode-scanning:17.3.0` | Odczyt kodów 1D/2D | Zależność obecna; importy `BarcodeScanning` i `Barcode` w `QrAndOwdScreens.kt`. W przeanalizowanym pliku nie znaleziono utworzenia skanera ani przetwarzania obrazu; aktywne ML Kit barcode nie jest potwierdzone. |
| `com.google.mlkit:translate:17.0.3` | Tłumaczenie na urządzeniu | Implementacja potwierdzona: `MlKitMedicalTranslationProvider.kt` używa `Translation.getClient`, `translator.translate` i zamyka translator. |
| `com.google.zxing:core:3.5.3` | Kodowanie/dekodowanie kodów | Implementacja generowania potwierdzona: `MultiFormatWriter`, `QRCodeWriter`, `BarcodeFormat`. |
| `com.journeyapps:zxing-android-embedded:4.3.0` | Interfejs skanera ZXing | Implementacja uruchamiania skanera potwierdzona: `ScanContract`, `ScanOptions` i `scanLauncher.launch(options)` w `QrAndOwdScreens.kt`. |
| `kotlinx-coroutines-play-services:1.9.0` | Await dla zadań Google Play Services | Używane przez tłumaczenie ML Kit (`await()`). |

Nie znaleziono w konfiguracji aplikacji bezpośrednich zależności CameraX, OpenCV, Tesseract ani ML Kit Document Scanner. Nie dodawać ich bez osobnego uzasadnienia i testu korzyści.

## Funkcje potwierdzone w kodzie

- Generowanie kodów QR z korekcją błędów H oraz opcjonalnym znakiem graficznym: `generateBrandedQr`.
- Generowanie kodów QR i CODE_128 dla kart lojalnościowych: `generateLoyaltyCodeBitmap`.
- Uruchamianie skanera ZXing przez Activity Result API.
- Tłumaczenie tekstu lokalnym modelem ML Kit w `MlKitMedicalTranslationProvider`.
- Zarządzanie modelami tłumaczeń: lista modeli, pobieranie, usuwanie, ustawienia automatycznego pobierania i Wi-Fi.
- Parsowanie rozpoznanego tekstu do sugestii pól dokumentu: `DocumentOcrParser`, w tym reguły kategorii i część obsługi MRZ.
- Testy JVM parsera OCR oraz inwentarza modeli tłumaczeniowych.

## Funkcje niepotwierdzone lub częściowe

- Rzeczywiste wykonanie OCR z obrazu: zależność i importy istnieją, ale w przeanalizowanym ekranie nie znaleziono pipeline'u rozpoznawania.
- Rzeczywisty odczyt kodów przez ML Kit: zależność i importy istnieją, ale ekran korzysta z kontraktu ZXing; brak potwierdzonego wywołania ML Kit.
- Automatyczne połączenie OCR → ekstrakcja → podgląd → zatwierdzenie → zapis w portfelu.
- Wspólny kontrakt wyników OCR/barcode/translation i jednolita obsługa błędów.
- Skanowanie wielostronicowe i automatyczna korekta perspektywy.
- Tłumaczenie ogólne dokumentów; obecna nazwa i implementacja wskazują na dostawcę medycznego.
- Walidacja autentyczności dokumentu na podstawie samego kodu QR (nie należy jej utożsamiać z dekodowaniem).

## Duplikaty i zależności do weryfikacji

ZXing Core oraz ZXing Android Embedded nie są automatycznie duplikatami: pierwszy zapewnia kodowanie, drugi gotowy interfejs skanowania. Należy jednak ustalić, czy aplikacja potrzebuje obu, czy można użyć jednego stosu w zależności od wymagań UI.

ML Kit Barcode Scanning i ZXing Embedded częściowo pokrywają obszar odczytu kodów. Nie utrzymywać dwóch aktywnych silników bez uzasadnionego scenariusza. Rekomendacja: wybrać jeden silnik skanowania po porównawczych testach formatów, stabilności, wydajności i obsługi obrazu z galerii. ZXing Core pozostawić, jeśli generowanie kodów nadal go wymaga.

Importy ML Kit OCR i Barcode w `QrAndOwdScreens.kt` wymagają weryfikacji kompilatorem/linterem i przeszukaniem całego drzewa. Nie usuwać zależności wyłącznie na podstawie jednego pliku. Przed usunięciem sprawdzić wszystkie źródła, testy, warianty buildów i transitive dependencies.

## Ryzyka i zasady

- OCR zwraca sugestie, nie dane urzędowo potwierdzone.
- Numer PESEL, IBAN i MRZ muszą być walidowane odrębnymi algorytmami; poprawna suma kontrolna nie dowodzi autentyczności dokumentu.
- Oryginał dokumentu należy zachować niezmieniony; poprawiony obraz i tekst OCR przechowywać jako pochodne.
- Wynik OCR wymaga podglądu i akceptacji użytkownika przed zapisem.
- Dokumenty i wyniki OCR mogą zawierać dane szczególnych kategorii; domyślnie przetwarzać lokalnie i nie wysyłać do usług zewnętrznych bez wyraźnej zgody.
- Tłumaczenie maszynowe nie może zastępować tłumaczenia przysięgłego ani być przedstawiane jako urzędowe.

## Decyzje

1. Nie dodawać nowych bibliotek na etapie audytu.
2. Wydzielić wspólny silnik bez uzależniania domeny od ML Kit/ZXing.
3. Najpierw doprowadzić do działającego, testowalnego pipeline'u OCR i barcode.
4. Zachować aktualny dostawca ML Kit Translation i przenieść go pod wspólny kontrakt tłumaczenia, bez zmiany zachowania.
5. Refaktoryzować duże pliki Kotlin małymi krokami; nie łączyć refaktoryzacji z wymianą silnika.
6. Każda zmiana zależności wymaga raportu użycia, wpływu na rozmiar APK i wyników testów.

## Pliki źródłowe przeanalizowane

- `obywatel-android/app/build.gradle.kts`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/OcrAndBarcodeComponents.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/DocumentOcrParser.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/MlKitMedicalTranslationProvider.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/MedicalTranslationLanguagesSettingsScreen.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/QrAndOwdScreens.kt`
- `obywatel-android/app/src/test/java/pl/gov/obywatel/android/DocumentOcrParserTest.kt`
- `obywatel-android/app/src/test/java/pl/gov/obywatel/android/MedicalTranslationModelInventoryTest.kt`
- `obywatel-android/app/src/androidTest/java/pl/gov/obywatel/android/MedicalTranslationModelSettingsTest.kt`

## Uzupełnienie stanu implementacji — 2026-11-27

Audyt powyżej jest migawką stanu main z commitu wskazanego w sekcji zakresu. Po tej migawce powstał osobny, nadal nie scalony PR #188 (feature/document-image-recognition-adapter), który dodaje DocumentImageRecognition.kt i faktyczne wywołania ML Kit Text Recognition oraz Barcode Scanning. Adapter przyjmuje gotowy Bitmap, zwraca tekst OCR i podstawowe dane kodów; nie jest jeszcze podłączony do wyboru pliku/aparatu, parsera dokumentów ani zapisu do portfela. Nie należy zatem zmieniać historycznego wniosku audytu dla bazowego commitu, ale bieżący stan gałęzi roboczych uwzględnia już prototyp adaptera.

PR #186 został scalony do main (commit ddec1e75945427232f1e6b19f80dcc374e9669fe). Generowanie QR znajduje się obecnie w QrCodeGeneration.kt; jest to refaktoryzacja generatora, a nie ukończenie wspólnego skanera ani OCR. Kontrole REUSE, kompilacja Android i testy GUI na emulatorze dla scalonego commitu zakończyły się sukcesem.

## Zmiany funkcjonalne po audycie — aktualizacja

W PR #184 dodano platform-neutralne kontrakty silnika oraz adapter `DocumentOcrFieldExtractor`, który wykorzystuje istniejący `DocumentOcrParser` i zwraca pola jako niezweryfikowane sugestie wymagające akceptacji. PR #184 został następnie scalony do `main` (commit `54f7bb817b93037787d2dfde7ea33d5785a83767`). Nie dodano nowych bibliotek ani nie usunięto istniejących.

PR #188 dodaje wykonawcę ML Kit oraz adaptery `MlKitTextRecognizerEngine` i `MlKitBarcodeReaderEngine` implementujące wspólne kontrakty `TextRecognizerEngine` i `BarcodeReaderEngine`. Zachowuje hierarchię bloków i linii OCR, pewność rozpoznania, współrzędne i obrót obrazu. Adapter działa na dostarczonej bitmapie; jej cykl życia pozostaje po stronie wywołującego. Nie obejmuje jeszcze wyboru źródła (aparat/galeria/SAF), wielostronicowych PDF, ekranu akceptacji ani zapisu do portfela. Dla aktualnego commitu PR #188 kontrola REUSE zakończyła się sukcesem, natomiast kompilacja Android i testy GUI/emulatora nadal trwają.


## Stan PR audytowego

PR #183 pozostaje dokumentacyjnym PR-em i nie powinien być scalany jako zmiana funkcjonalna ani wykorzystywany do usuwania zależności. Gałąź audytowa jest oparta na starszym punkcie `main` (merge-base `0a06b07d74548a43170685173571b976a91d4515`), dlatego przed scaleniem należy uwzględnić różnicę historii względem aktualnego `main`. Treść audytu zachowuje rozróżnienie między migawką historyczną a aktualnym stanem implementacji.
