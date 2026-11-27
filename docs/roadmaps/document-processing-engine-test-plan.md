SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Document Processing Engine — etapy wdrożenia i plan testów

## Zasady prowadzenia prac

- Każdy etap w osobnej gałęzi i PR kierowanym do `main`.
- PR-y zależne od poprzedniego etapu; nie scalać gałęzi równolegle w sposób powodujący rozjazd baz.
- Bez scalania przy czerwonym, pominiętym lub wymagającym ręcznej akceptacji teście.
- Nie dodawać biblioteki, jeśli istniejący stos realizuje wymagany przypadek.
- Każdy PR zawiera listę plików Kotlin przeniesionych/refaktoryzowanych i uzasadnienie.
- Refaktoryzacja bez zmiany zachowania poprzedza dodawanie funkcji.
- Aktualizować REUSE/licencje dla każdego nowego pliku i zależności.

## Plan PR

| Kolejność | Proponowany PR | Zakres | Kryteria |
|---|---|---|---|
| 0 | Audit: OCR/QR/translation dependencies | Raport zależności, użycia, ryzyk i braków | Weryfikacja źródeł i brak nieuzasadnionych zależności |
| 1 | Document Processing contracts | Modele, interfejsy, błędy, provenance | Testy JVM kontraktów; brak zależności dostawcy w domenie |
| 2 | Refactor QR and scanner Kotlin files | Rozbicie `QrAndOwdScreens.kt` i komponentów na mniejsze pliki | Brak zmiany zachowania; testy JVM, kompilacja, testy UI |
| 3 | ML Kit OCR adapter | Rzeczywiste OCR obrazu z blokami, liniami, bounding boxes | Testy adaptera, instrumentacyjne na rzeczywistych fixture'ach |
| 4 | Barcode adapter consolidation | Porównanie ML Kit Barcode i ZXing; wybór jednego czytnika; zachowanie generatora | Macierz formatów, zdjęcie/galeria/kamera, brak regresji QR |
| 5 | Extraction and validation pipeline | Parser + walidatory pól i MRZ z poziomami pewności | Testy pozytywne, negatywne, graniczne, błędne OCR |
| 6 | Translation abstraction | Wspólny interfejs dla obecnego ML Kit Translation | Testy języków, brak modelu, offline, anulowanie i błędy |
| 7 | Document preview and approval | Podgląd OCR, korekta pól, jawny zapis do portfela | Testy Compose, brak zapisu bez potwierdzenia |
| 8 | Module integrations | Osobne integracje dokumentów, finansów, kontaktów i certyfikatów | Każdy moduł ma własny zestaw testów regresyjnych |

## Testy JVM

### Kontrakty i orkiestracja
- Pusta prośba, brak operacji, nieobsługiwany typ źródła.
- Kolejność etapów i zachowanie błędów.
- Anulowanie w każdym etapie.
- Timeout i ponowienie tylko dla błędów przejściowych.
- Wynik częściowy, np. OCR sukces / barcode błąd.
- Confidence null, wartości poza zakresem i puste wyniki.

### OCR i ekstrakcja
- Tekst pusty, wielowierszowy, mieszany językowo i z polskimi znakami.
- Zmieniona kolejność pól i brak etykiet.
- Wielokrotne etykiety, duplikaty i wartości przekraczające limit.
- MRZ TD1/TD3 poprawne i uszkodzone; sumy kontrolne poprawne i błędne.
- PESEL i IBAN poprawne, błędne, graniczne; walidacja nie może utożsamiać poprawnej sumy kontrolnej z autentycznością.
- Daty niepoprawne, przestępne, niejednoznaczne i z różnymi separatorami.
- Niska pewność OCR: pole wymaga ręcznej weryfikacji.
- Regresja istniejącego `DocumentOcrParserTest`.

### QR i barcode
- Generowanie QR z polskimi znakami, długim ładunkiem i pustą wartością.
- Korekcja błędów i margines.
- Obsługiwane/nieobsługiwane formaty.
- Weryfikacja round-trip generator → dekoder.
- Uszkodzone dane i niepoprawny payload.
- Testy wielu kodów w jednym obrazie.

### Tłumaczenie
- Mapowanie kodów językowych, nieobsługiwany język.
- Pusty tekst, długi tekst i tekst wielojęzyczny.
- Brak modelu, błąd pobrania, brak sieci, tryb Wi-Fi-only.
- Anulowanie i zwalnianie translatora.
- Zachowanie oryginału po błędzie tłumaczenia.

## Testy instrumentacyjne i emulatorowe

- Uprawnienia i dostęp do aparatu; odmowa, ponowna zgoda i brak kamery.
- SAF: zdjęcie JPEG/PNG, obrócony EXIF, plik uszkodzony, zbyt duży, brak dostępu do URI.
- OCR na fixture'ach o kontrolowanym oświetleniu, rozdzielczości, pochyleniu i kontraście.
- Kody QR, EAN-13, Code 128, PDF417, Data Matrix, Aztec; testować wyłącznie formaty deklarowane jako obsługiwane.
- Kamera na emulatorze i urządzeniu fizycznym; emulator nie zastępuje testów optycznych.
- Offline: OCR/barcode bez sieci; tłumaczenie z modelem pobranym oraz bez modelu.
- Obrót ekranu, lifecycle, powrót z tła, anulowanie procesu i zwalnianie zasobów.
- Compose: podgląd, korekta, błędy, ponowienie i zatwierdzenie.
- Brak zapisu w bazie przed jawnej akceptacji.
- Brak danych dokumentów w logcat.
- Testy na API 26 (minSdk) oraz docelowym API urządzenia CI.

## Kryteria jakości i wydajności

- Brak wycieków bitmap i niezamykania klienta ML Kit.
- Przetwarzanie bitmap z ograniczeniem pamięci i rozdzielczości.
- Płynność UI: ciężkie operacje poza głównym wątkiem.
- Powtarzalne wyniki testów na zestawie fixture'ów.
- Pomiar czasu OCR, barcode i zużycia pamięci na urządzeniu referencyjnym.
- Pomiar zmiany rozmiaru APK/AAB po każdej zmianie zależności.
- Kontrola licencji i REUSE.

## Bramka scalania

PR można scalać dopiero, gdy:
1. Gradle build debug przechodzi.
2. Wszystkie testy JVM przechodzą.
3. Testy instrumentacyjne/Compose wymagane dla danego etapu przechodzą.
4. Kontrole GitHub Actions i REUSE są zielone.
5. Brak nierozwiązanych uwag bezpieczeństwa i regresji.
6. PR jest oparty na aktualnym `main`.
7. Dla PR wymagającego testu kamery/urządzenia fizycznego istnieje jawny raport testu; sam build emulatora nie zastępuje go.

## Status

Ten dokument określa plan. Nie deklaruje, że opisane etapy implementacji zostały już wykonane ani że testy zostały uruchomione.

## Aktualizacja kolejności i statusu PR — 2026-11-27

- PR #183 — audyt zależności: dokumentacja przygotowana; kontrola zgodności repozytorium zakończona powodzeniem. PR nadal otwarty jako wersja robocza.
- PR #184 — architektura: dokumentacja przygotowana; kontrola zgodności repozytorium zakończona powodzeniem. PR nadal otwarty jako wersja robocza.
- PR #185 — niniejszy plan: kontrola zgodności repozytorium zakończona powodzeniem. PR nadal otwarty jako wersja robocza.
- PR #186 — wydzielenie generowania QR: testy JVM zakończone powodzeniem; kompilacja APK i testy GUI/emulatora były w toku przy ostatnim odczycie. Nie uznawać PR za gotowy do scalenia przed zakończeniem wszystkich wymaganych zadań.
- PR #188 — adapter OCR/barcode ML Kit: kod prototypowy dodany, ale wymaga testów, kompilacji i integracji. Nie ma potwierdzonego zielonego przebiegu CI.

### Doprecyzowane bramki dla adaptera OCR/barcode

1. Test odrzuconego, zwolnionego bitmapu oraz poprawnej obsługi anulowania.
2. Testy mapowania wyników barcode i pustych/nieczytelnych danych.
3. Test OCR na kontrolowanym obrazie z tekstem, z uwzględnieniem dostępności modelu ML Kit na urządzeniu testowym.
4. Testy instrumentacyjne na emulatorze/urządzeniu; samo przejście testów JVM nie potwierdza działania ML Kit.
5. Weryfikacja zamykania recognizera/scannera także przy wyjątku.
6. Test obrotu obrazu/EXIF i limitów rozdzielczości przed podłączeniem do importu plików.
7. Integracja z parserem ma zwracać propozycje pól, a zapis do dokumentów musi wymagać jawnej akceptacji użytkownika.

Statusy powyżej są migawką, nie automatycznym potwierdzeniem aktualnego stanu CI. Przed każdym scaleniem ponownie odczytać wszystkie wymagane kontrole i ich logi.
