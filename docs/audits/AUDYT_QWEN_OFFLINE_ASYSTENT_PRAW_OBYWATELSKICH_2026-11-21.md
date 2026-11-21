<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->

# Audyt techniczno-architektoniczny wdrożenia lokalnego Qwen dla Asystenta Praw Obywatelskich

- **Data opracowania:** 21 listopada 2026 r.
- **Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`
- **Gałąź bazowa:** `main`
- **Commit bazowy audytu:** `370bfa5621b059c9a6f9ab7191f7525c16e99cee`
- **Zakres urządzenia docelowego:** Xiaomi Pad 6, Snapdragon 870, 8 GB RAM, 256 GB pamięci wewnętrznej
- **Cel:** zaprojektowanie i ocena wykonalności całkowicie lokalnego, bezpłatnego w użyciu asystenta AI opartego na modelach Qwen, bez płatnego API i bez wysyłania treści dokumentów do zewnętrznego dostawcy modelu.
- **Charakter dokumentu:** audyt statyczny repozytorium połączony z analizą wykonalności i projektem architektury. Nie jest to raport z wdrożenia ani z pomiarów na fizycznym tablecie.

---

## 1. Streszczenie kierownicze

Celem audytu jest ustalenie, czy istniejąca aplikacja Android może zostać rozszerzona o lokalny model językowy Qwen i wykorzystana jako silnik trzech funkcji Asystenta Praw Obywatelskich:

1. analizatora pism urzędowych i podstaw prawnych żądań;
2. generatora odpowiedzi, wniosków, skarg, petycji i odwołań;
3. nawigatora procedur administracyjnych i innych ścieżek prawnych.

### 1.1. Wniosek główny

**Wdrożenie lokalnego Qwen na Xiaomi Pad 6 jest technicznie wykonalne, ale wymaga dodania nowej warstwy natywnego wnioskowania i odrębnego, kontrolowanego podsystemu wiedzy prawnej.**

W aktualnie sprawdzonej gałęzi `main` nie stwierdzono zintegrowanego silnika llama.cpp, biblioteki JNI do inferencji LLM, menedżera plików GGUF ani modułu Qwen. Nie znaleziono też kodu C/C++ ani konfiguracji CMake należącej do aplikacji Android. Nie oznacza to, że projekt nie może używać takiego silnika; oznacza, że integracja nie została jeszcze wykonana w sprawdzonym commicie.

Projekt ma już użyteczne elementy, które można ponownie wykorzystać:

- Kotlin i Jetpack Compose;
- istniejący mechanizm importowania dokumentów przez Storage Access Framework (SAF);
- OCR oparty na ML Kit;
- parser OCR, który traktuje wyniki jako sugestie, a nie zweryfikowane dane;
- lokalne magazyny i wybrane mechanizmy ochrony danych z użyciem Android Keystore;
- testy JVM i testy instrumentacyjne;
- centralny rejestr tras dokumentów;
- istniejące mechanizmy tłumaczeń na urządzeniu, które pokazują wzorzec pobierania i lokalnego używania modeli.

### 1.2. Rekomendacja modelowa

Jako pierwszy wariant do prototypowania rekomenduje się **Qwen3.5-4B w kwantyzacji GGUF Q4_K_M**, uruchamiany przez llama.cpp. Jest to kandydat do testów, a nie model już zintegrowany ani zatwierdzony do produkcji.

Nie rekomenduje się rozpoczynania od dużych wariantów Qwen3.6 lub Qwen3.8, jeżeli dostępne wydanie ma 27B parametrów lub więcej. Nie należy utożsamiać liczby aktywnych parametrów w modelu MoE z ilością pamięci potrzebnej do przechowania wszystkich wag. Model Qwen4 można dodać dopiero po potwierdzeniu dostępności otwartych wag, zgodności formatu i obsługi architektury przez wybraną wersję silnika.

### 1.3. Najważniejsze zastrzeżenie

Rozmiar pliku modelu GGUF nie jest równy całkowitemu zapotrzebowaniu na RAM. Do wag dochodzą między innymi pamięć kontekstu, bufory obliczeniowe, tokenizer, biblioteka natywna i pamięć używana przez Androida. Z tego powodu 4B Q4 jest rozsądnym kandydatem do próby na urządzeniu 8 GB, ale nie wolno deklarować płynności ani stabilności bez pomiaru na fizycznym Xiaomi Pad 6.

### 1.4. Decyzja architektoniczna

Rekomenduje się:

- lokalny model Qwen i lokalny silnik llama.cpp;
- brak płatnego API i brak klucza API w aplikacji;
- lokalne przetwarzanie dokumentów;
- lokalną bazę prawa z wersjonowaniem przepisów;
- deterministyczny silnik terminów i reguł proceduralnych;
- walidator odpowiedzi AI;
- jawne zatwierdzanie każdego dokumentu przez użytkownika;
- możliwość wymiany modelu bez przebudowy pozostałej części aplikacji.

---

## 2. Zakres, metoda i ograniczenia audytu

### 2.1. Zakres sprawdzenia

Przeprowadzono statyczny przegląd gałęzi `main` repozytorium, w szczególności:

- konfiguracji Gradle i Androida;
- manifestu aplikacji;
- zależności aplikacji;
- istniejącego OCR i parsera OCR;
- lokalnego importu dokumentów;
- wybranych magazynów i mechanizmów bezpieczeństwa;
- nawigacji i rejestru tras;
- istniejących testów;
- przepływów CI dla kompilacji i testów emulatora;
- poprzedniego audytu architektury Android.

Sprawdzono również drzewo repozytorium dla występowania plików związanych z Qwen, LLM, llama.cpp, JNI, C/C++, CMake i menedżerem modeli.

### 2.2. Metoda

1. Odczyt metadanych repozytorium i identyfikacja commitu bazowego.
2. Przegląd drzewa plików.
3. Odczyt wskazanych plików konfiguracyjnych i źródłowych.
4. Identyfikacja istniejących komponentów możliwych do ponownego wykorzystania.
5. Porównanie wymagań integracji Android/NDK z aktualną strukturą projektu.
6. Analiza ograniczeń sprzętowych urządzenia docelowego.
7. Opracowanie docelowej architektury, ryzyk, planu testów i kryteriów odbioru.

### 2.3. Czego audyt nie potwierdza

Nie wykonano w ramach tego audytu:

- kompilacji aplikacji po dodaniu llama.cpp;
- uruchomienia Qwen na fizycznym Xiaomi Pad 6;
- pomiaru tokenów na sekundę;
- pomiaru szczytowego PSS/RSS i wykorzystania pamięci natywnej;
- pomiaru temperatury i throttlingu;
- testu backendu Vulkan na Adreno 650;
- testu długiej sesji inferencji;
- testu jakości prawnej modelu na korpusie rzeczywistych spraw;
- testu penetracyjnego aplikacji;
- formalnej oceny zgodności z RODO lub AI Act.

Wszelkie parametry wydajnościowe w dalszej części są zatem celami testowymi albo założeniami projektowymi, a nie wynikami pomiarów.

---

## 3. Stan istniejącego repozytorium

### 3.1. Konfiguracja projektu Android

W sprawdzonym commicie projekt Android znajduje się w katalogu `obywatel-android`.

Ustalenia z `obywatel-android/app/build.gradle.kts`:

- wtyczki: Android Application i Kotlin Compose;
- namespace: `pl.gov.obywatel.android`;
- `compileSdk = 37`;
- `minSdk = 26`;
- `targetSdk = 35`;
- Java 17;
- Kotlin JVM target 17;
- Jetpack Compose;
- testy JVM i testy instrumentacyjne AndroidX.

Projekt ma zatem nowoczesną bazę Android, ale nie ma jeszcze zdefiniowanej integracji NDK/CMake ani natywnego modułu inferencji.

### 3.2. Zależności istniejącej aplikacji

W module aplikacji stwierdzono między innymi:

- AndroidX Activity Compose;
- AndroidX Core i DocumentFile;
- Jetpack Compose UI i Material 3;
- Lifecycle Runtime;
- OkHttp;
- Tor Android;
- ZXing;
- AndroidSVG;
- ML Kit Text Recognition;
- ML Kit Barcode Scanning;
- ML Kit Translate;
- Kotlin Coroutines.

Nie stwierdzono zależności llama.cpp, LiteRT-LM, ONNX Runtime ani innego silnika LLM w analizowanym pliku zależności.

**Wniosek:** nie ma potrzeby zastępowania obecnego stosu UI. Silnik LLM należy dodać jako wydzielony komponent, najlepiej przez bibliotekę natywną ARM64 i cienką warstwę JNI.

### 3.3. Manifest Androida

W `app/src/main/AndroidManifest.xml` istnieje uprawnienie `INTERNET`, wymagane przez pozostałe funkcje aplikacji. Aplikacja deklaruje również `android:allowBackup="false"`.

Konsekwencje:

- globalnego uprawnienia INTERNET nie można usunąć bez analizy pozostałych funkcji aplikacji;
- sam lokalny moduł AI nie powinien wykonywać żądań sieciowych podczas inferencji;
- pobieranie modelu powinno być odrębną, świadomą operacją menedżera modeli;
- należy sprawdzić, czy nowe biblioteki nie wprowadzają telemetrii, zdalnych usług lub nieoczekiwanych połączeń;
- wyłączenie kopii zapasowej aplikacji jest korzystnym punktem wyjścia, ale nie zastępuje przeglądu eksportów, plików tymczasowych, logów i danych udostępnianych przez SAF.

### 3.4. Istniejący OCR

W repozytorium znajduje się `DocumentOcrParser.kt`. Parser:

- normalizuje tekst i etykiety pól;
- rozpoznaje wybrane pola dokumentów;
- obsługuje m.in. daty, numery dokumentów, PESEL i strefy MRZ;
- ogranicza długość przejmowanych wartości;
- opisuje sugestie OCR jako niezweryfikowane.

To dobry wzorzec dla nowego analizatora pism, ale obecny parser nie jest analizatorem prawnym. Rozpoznawanie pól dowodu lub paszportu nie rozwiązuje problemu rozpoznania rodzaju czynności administracyjnej, podstawy prawnej, pouczenia ani skutków procesowych.

Nowy analizator powinien mieć odrębny model danych i osobny zestaw testów. Nie należy rozszerzać istniejącego parsera dokumentów tożsamości o logikę postępowań administracyjnych.

### 3.5. Istniejący mechanizm tłumaczeń na urządzeniu

`MlKitMedicalTranslationProvider.kt` pokazuje, że aplikacja potrafi korzystać z modeli pobieranych i następnie używanych na urządzeniu. Kod sprawdza dostępność modelu językowego, respektuje ustawienie automatycznego pobierania i opcję pobierania tylko przez Wi-Fi.

Ten wzorzec można wykorzystać w menedżerze Qwen w zakresie:

- jawnego stanu pobierania;
- kontroli warunków sieciowych;
- rozróżnienia modelu pobranego i niedostępnego;
- prezentowania komunikatów o braku modelu.

Nie należy jednak kopiować mechanizmu ML Kit jako silnika LLM. Modele tłumaczeniowe i generatywny model językowy mają inne wymagania pamięciowe, formaty, licencje i cykle życia.

### 3.6. Import dokumentów i SAF

`ImportedDocumentStore.kt` przechowuje odwołania URI do plików wybranych przez użytkownika i trwałe uprawnienia SAF. Kod nie kopiuje bezwarunkowo każdego dokumentu do katalogu aplikacji. Ma również limity rozmiaru dokumentu i archiwum.

To użyteczny mechanizm wyboru źródłowego dokumentu dla analizatora. Należy jednak rozróżnić:

- URI źródłowego dokumentu;
- tekst wydobyty przez OCR;
- kopię roboczą przekazaną do parsera;
- wynik analizy;
- wygenerowane pismo;
- eksport pliku poza aplikację.

Każdy z tych elementów powinien mieć osobną politykę przechowywania i usuwania.

### 3.7. Nawigacja i architektura aplikacji

`DocumentRouteRegistry.kt` jest centralnym rejestrem tras dokumentów i rozróżnia trasy typu GROUP, DOCUMENT i SPECIALIZED. Nowy Asystent Praw Obywatelskich powinien być dodany jako funkcja specjalistyczna, a nie jako fikcyjny dokument w katalogu dokumentów.

Poprzedni `obywatel-android/AUDYT_ARCHITEKTURY.md` wskazywał na koncentrację koordynacji w `MainActivity.kt` oraz niejednorodność warstwy trwałości. Wdrożenie AI powinno uwzględnić te ustalenia: logiki inferencji, zarządzania modelem, spraw prawnych i terminów nie należy umieszczać w MainActivity.

### 3.8. Testy i CI

Repozytorium ma workflow:

- `.github/workflows/android-build.yml` – testy JVM i kompilacja debug APK;
- `.github/workflows/android-gui.yml` – testy JVM, testy Compose na emulatorze Android x86_64 i kompilacja APK;
- `.github/workflows/reuse.yaml` – kontrola REUSE.

Aktualny workflow emulatora korzysta z obrazu x86_64. Nie potwierdzi on wydajności natywnej biblioteki ARM64 na Snapdragonie 870. Wymagane będą osobne testy na fizycznym tablecie albo na urządzeniu ARM64 o zbliżonej konfiguracji.

### 3.9. Brakujące elementy integracji LLM

W drzewie repozytorium bazowego nie znaleziono:

- źródeł llama.cpp;
- plików C/C++;
- `CMakeLists.txt` dla silnika;
- konfiguracji Android NDK dla inferencji;
- katalogu `jniLibs/arm64-v8a` zawierającego bibliotekę LLM;
- mostu JNI;
- menedżera modeli GGUF;
- manifestu modeli i sum kontrolnych;
- bazy wiedzy prawnej;
- indeksu wyszukiwania przepisów;
- silnika terminów prawnych;
- analizatora prawnego opartego na LLM;
- generatora pism prawnych opartego na LLM;
- testów jakości modelu i testów wydajności na urządzeniu.

**Wniosek:** wdrożenie należy traktować jako nową funkcjonalność, a nie jako aktywację istniejącego, ukrytego komponentu.

---

## 4. Charakterystyka urządzenia docelowego

### 4.1. Konfiguracja

Urządzenie wskazane przez użytkownika:

- Xiaomi Pad 6;
- Qualcomm Snapdragon 870;
- 8 GB RAM;
- 256 GB pamięci wewnętrznej.

Właściwym docelowym ABI dla biblioteki natywnej jest `arm64-v8a`. Konfiguracja musi być kompilowana dla Android NDK, a nie jako biblioteka Linux ARM64 przeznaczona dla zwykłego systemu Linux.

### 4.2. Znaczenie 8 GB RAM

8 GB RAM wystarcza do rozpoczęcia testów modelu z rodziny 4B w kwantyzacji 4-bitowej, ale nie oznacza, że cały budżet pamięci jest dostępny dla modelu.

Na całkowite zapotrzebowanie składają się:

1. system Android i usługi producenta;
2. interfejs aplikacji i inne procesy;
3. wagi modelu;
4. bufor kontekstu/KV cache;
5. bufory obliczeniowe backendu;
6. tokenizer i struktury sterujące;
7. OCR i dekodowanie dokumentów;
8. pamięć tymczasowa podczas generowania i eksportu.

Rozmiar pliku GGUF jest jedynie jednym ze składników tego bilansu.

### 4.3. Konfiguracja startowa

Do pierwszych testów proponuje się:

| Parametr | Wartość początkowa | Uwagi |
|---|---:|---|
| Model | Qwen3.5-4B Instruct | Kandydat do testów |
| Format | GGUF | Wymagany zgodny chat template |
| Kwantyzacja | Q4_K_M | Punkt wyjścia jakościowo-pamięciowy |
| Kontekst | 2048 tokenów | Konserwatywny profil startowy |
| Profil rozszerzony | 4096 tokenów | Dopiero po pomiarze pamięci |
| Równoległe generowanie | 1 sesja | Bez współbieżnego obciążenia |
| Wątki CPU | 4 na start | Wartość do pomiaru, nie gwarancja optimum |
| Backend | CPU ARM64 | Obowiązkowy profil bazowy |
| Vulkan | Eksperymentalnie | Włączyć dopiero po testach stabilności |
| OCR podczas generowania | Sekwencyjnie | Nie uruchamiać ciężkiego OCR równolegle z inferencją |

Nie należy ustawiać dużego kontekstu wyłącznie dlatego, że model deklaruje obsługę długiego kontekstu. Koszt pamięci i czas przetwarzania rosną wraz z ilością tekstu wejściowego.

### 4.4. Oczekiwana wydajność

Nie podaje się z góry wartości tokenów na sekundę dla Xiaomi Pad 6. Wynik zależy od wersji llama.cpp, kwantyzacji, backendu, temperatury, liczby wątków, kontekstu, sterowników i obciążenia systemu.

Należy wykonać co najmniej trzy serie pomiarowe:

- krótki prompt i odpowiedź 300–500 tokenów;
- analiza dokumentu o objętości 2–4 stron;
- wygenerowanie projektu pisma o objętości 1–3 stron.

Wyniki muszą zawierać: czas ładowania modelu, czas do pierwszego tokenu, szybkość generacji, całkowity czas zadania, maksymalny PSS/RSS, liczbę błędów, temperaturę i informację o throttlingu.

---

## 5. Dobór rodziny Qwen

### 5.1. Model bazowy do prototypu

Rekomendowany kandydat: **Qwen3.5-4B Instruct w GGUF Q4_K_M**.

Uzasadnienie:

- wariant 4B jest znacznie bardziej realistyczny dla 8 GB RAM niż modele 27B lub większe;
- dostępna jest konwersja GGUF przeznaczona dla llama.cpp;
- model może wykonywać zadania streszczania, klasyfikacji, ekstrakcji i redagowania tekstu;
- można go wymienić bez zmiany kontraktów modułów aplikacji.

Rozmiar pliku podawany przez repozytoria GGUF jest orientacyjny i zależy od konkretnej konwersji, wersji, wariantu i dodatkowych tensorów. Menedżer modeli nie może polegać wyłącznie na nazwie pliku lub deklarowanym rozmiarze.

### 5.2. Qwen3.6 i Qwen3.8

W oficjalnym repozytorium rodziny Qwen3.8 wymieniono między innymi Qwen3.8-27B i większe warianty MoE. Wersje Qwen3.6 dostępne w publicznych kartach modeli również obejmują duże warianty, m.in. 27B i 35B-A3B.

Nie są one rekomendowane do pierwszego wdrożenia na tablecie z 8 GB RAM. Wariant MoE nie staje się małym modelem tylko dlatego, że aktywuje mniejszą część parametrów podczas pojedynczego kroku generacji. Wagi ekspertów nadal muszą być dostępne w pamięci lub odpowiednim magazynie.

### 5.3. Qwen4 i przyszłe wydania

Wersję Qwen4 należy traktować jako przyszły kandydat do wymiany modelu, a nie jako warunek rozpoczęcia prac.

Przed dodaniem wydania trzeba potwierdzić:

- oficjalny identyfikator modelu i autora;
- dostępność otwartych wag;
- licencję;
- dostępność GGUF lub możliwość legalnej i technicznie poprawnej konwersji;
- obsługę architektury przez konkretny commit llama.cpp;
- wymagania pamięciowe;
- poprawność chat template;
- testy języka polskiego;
- stabilność na ARM64 Android.

Nie wolno zakładać, że każda wersja oznaczona numerem 4, 3.8, 3.7 lub 3.6 ma mały wariant nadający się do urządzenia mobilnego.

### 5.4. Rejestr modeli

Model musi być opisany przez manifest, a nie przez samą nazwę w interfejsie.

Proponowany rekord:

```json
{
  "modelId": "qwen35-4b-instruct-q4km",
  "displayName": "Qwen3.5 4B Instruct Q4_K_M",
  "sourceRepository": "ggml-org/Qwen3.5-4B-GGUF",
  "sourceRevision": "PINNED_COMMIT_REQUIRED",
  "fileName": "MODEL_FILE_FROM_PINNED_REVISION.gguf",
  "quantization": "Q4_K_M",
  "architecture": "qwen35",
  "license": "VERIFY_MODEL_CARD",
  "sha256": "REQUIRED_AT_RELEASE",
  "minimumAbi": "arm64-v8a",
  "engineRevision": "PINNED_LLAMA_CPP_COMMIT",
  "chatTemplate": "read_from_gguf_metadata",
  "recommendedContext": 2048
}
```

Wartości `PINNED_COMMIT_REQUIRED`, `MODEL_FILE_FROM_PINNED_REVISION.gguf` i `REQUIRED_AT_RELEASE` są celowymi polami do uzupełnienia po wyborze konkretnej rewizji modelu. Nie należy ich zastępować niezweryfikowanymi wartościami.

---

## 6. Docelowa architektura techniczna

### 6.1. Zasada separacji

Moduł prawny nie powinien zależeć bezpośrednio od konkretnego modelu. Aplikacja powinna znać abstrakcyjny interfejs inferencji, a Qwen ma być jednym z dostawców implementujących ten interfejs.

Warstwy:

1. **Interfejs użytkownika** – ekrany Asystenta Praw Obywatelskich.
2. **Warstwa sprawy** – dokumenty, stan postępowania, terminy i historia.
3. **Orkiestrator AI** – dzieli zadanie na etapy i wybiera narzędzia lokalne.
4. **Silnik inferencji** – interfejs Kotlin i most JNI do llama.cpp.
5. **Baza wiedzy prawnej** – akty, przepisy, wersje, orzeczenia i źródła.
6. **Silnik proceduralny** – reguły i obliczenia deterministyczne.
7. **Walidator** – kontrola cytowań, struktury, kompletności i niespójności.
8. **Warstwa bezpieczeństwa** – szyfrowanie, uprawnienia, audyt operacji i retencja.

### 6.2. Proponowany podział pakietów

```text
obywatel-android/app/src/main/
├── cpp/
│   ├── CMakeLists.txt
│   ├── qwen_jni.cpp
│   └── llama_runtime/
│
├── jniLibs/
│   └── arm64-v8a/
│       └── liblegal_llm.so
│
└── java/pl/gov/obywatel/android/
    └── legal/
        ├── ui/
        │   ├── LegalAssistantScreen.kt
        │   ├── LegalAnalysisScreen.kt
        │   ├── LegalGeneratorScreen.kt
        │   ├── LegalNavigatorScreen.kt
        │   └── LegalCaseScreen.kt
        │
        ├── ai/
        │   ├── LocalLlmEngine.kt
        │   ├── LegalModelProvider.kt
        │   ├── LegalAiOrchestrator.kt
        │   ├── LegalPromptBuilder.kt
        │   ├── LegalOutputParser.kt
        │   └── LegalResponseValidator.kt
        │
        ├── model/
        │   ├── ModelManager.kt
        │   ├── ModelManifest.kt
        │   ├── ModelDownloadWorker.kt
        │   ├── ModelIntegrityVerifier.kt
        │   └── ModelRuntimeSettings.kt
        │
        ├── knowledge/
        │   ├── LegalDatabase.kt
        │   ├── LegalActRepository.kt
        │   ├── LegalProvisionRepository.kt
        │   ├── LegalSearchEngine.kt
        │   └── LegalVersionResolver.kt
        │
        ├── analysis/
        │   ├── LegalDocumentClassifier.kt
        │   ├── LegalRequestAnalyzer.kt
        │   ├── AuthorityCompetenceChecker.kt
        │   └── LegalEvidenceExtractor.kt
        │
        ├── generator/
        │   ├── LegalLetterGenerator.kt
        │   ├── LegalTemplateRepository.kt
        │   └── LegalDocumentValidator.kt
        │
        ├── procedure/
        │   ├── LegalProcedureEngine.kt
        │   ├── LegalPathResolver.kt
        │   └── LegalDeadlineCalculator.kt
        │
        └── security/
            ├── LegalDataStore.kt
            ├── LegalEncryption.kt
            ├── LegalConsentController.kt
            └── LegalAuditLogger.kt
```

Jest to struktura docelowa, a nie opis aktualnego stanu repozytorium. Przed implementacją należy dopasować nazwy i granice pakietów do rzeczywistego kodu, unikając duplikowania istniejących magazynów dokumentów.

### 6.3. Kontrakt silnika inferencji

Warstwa Kotlin powinna udostępniać interfejs niezależny od llama.cpp:

```kotlin
interface LocalLlmEngine {
    suspend fun loadModel(
        modelPath: String,
        config: InferenceConfig
    ): ModelLoadResult

    fun generate(
        request: GenerationRequest,
        onToken: (String) -> Unit
    ): GenerationHandle

    suspend fun cancel(handle: GenerationHandle)

    suspend fun unloadModel()

    fun runtimeStatus(): RuntimeStatus
}
```

Wymagania:

- inferencja nigdy nie może wykonywać się na głównym wątku UI;
- generowanie musi obsługiwać anulowanie;
- jeden model i jedna sesja generowania w profilu podstawowym;
- błędy natywne muszą być mapowane na kontrolowane wyniki Kotlin;
- zwolnienie modelu musi być deterministyczne;
- należy obsłużyć przerwanie aplikacji, zmianę konfiguracji i brak pamięci;
- strumień tokenów nie może powodować nadmiernej liczby aktualizacji Compose;
- tekst częściowy musi być wyraźnie oznaczony jako nieukończony, dopóki generowanie się nie zakończy.

---

## 7. Integracja llama.cpp i Android NDK

### 7.1. Uzasadnienie wyboru

llama.cpp jest dojrzałym, natywnym silnikiem inferencji dla modeli GGUF i udostępnia ścieżkę budowania dla Android ARM64. Umożliwia uruchamianie modelu bez płatnej usługi API.

W projekcie należy przypiąć konkretny commit llama.cpp. Nie należy budować aplikacji produkcyjnej z niezablokowanej gałęzi `master`, ponieważ zmiany API, obsługi architektur i formatu GGUF mogą powodować regresje.

### 7.2. Docelowa konfiguracja budowania

Podstawowa konfiguracja powinna:

- używać Android NDK i CMake;
- kompilować ABI `arm64-v8a`;
- używać `GGML_NATIVE=OFF` przy cross-kompilacji;
- wyłączyć OpenMP, jeśli nie jest dostępny i poprawnie obsługiwany w wybranym środowisku NDK;
- wyłączyć llamafile, jeśli nie jest obsługiwany na Androidzie;
- nie uzależniać podstawowego profilu od OpenSSL;
- rozważyć KleidiAI dla ARM64, po potwierdzeniu zgodności i wyników pomiarów.

Przykładowa konfiguracja bazowa zgodna z dokumentacją llama.cpp:

```bash
cmake -S . -B build-android \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_PLATFORM=android-28 \
  -DGGML_NATIVE=OFF \
  -DGGML_OPENMP=OFF \
  -DGGML_LLAMAFILE=OFF \
  -DLLAMA_OPENSSL=OFF

cmake --build build-android --config Release --parallel
```

Wariant z `GGML_CPU_KLEIDIAI=ON` powinien być osobnym wariantem testowym. Należy porównać wynik z przenośnym wariantem bazowym i nie zakładać, że dodatkowa optymalizacja automatycznie poprawi każdy typ obciążenia.

### 7.3. CPU jako profil obowiązkowy

Pierwszy działający wariant powinien być CPU-only. Daje to:

- prostszą diagnostykę;
- mniejszą liczbę zależności od sterowników;
- stabilną bazę porównawczą;
- możliwość odróżnienia problemów modelu od problemów backendu GPU.

### 7.4. Vulkan jako profil opcjonalny

Snapdragon 870 wykorzystuje układ graficzny Adreno 650. Obsługa Vulkan przez urządzenie nie jest wystarczającym dowodem, że konkretny backend llama.cpp będzie stabilny i szybszy.

Vulkan należy wdrożyć dopiero po:

1. zbudowaniu i uruchomieniu CPU-only;
2. uzyskaniu powtarzalnych pomiarów;
3. przygotowaniu osobnego wariantu biblioteki;
4. porównaniu czasu generowania, pamięci i temperatury;
5. wykonaniu długiego testu obciążeniowego;
6. potwierdzeniu poprawnego zwalniania zasobów GPU.

W przypadku błędu GPU aplikacja powinna wrócić do CPU albo poinformować o niedostępności backendu, bez utraty dokumentu i treści sprawy.

### 7.5. ABI emulatora

Obecny workflow UI uruchamia emulator x86_64. Należy zdecydować, czy:

- budować bibliotekę także dla x86_64, aby testować most JNI w emulatorze;
- czy testować UI z atrapą `LocalLlmEngine`, a natywną inferencję uruchamiać wyłącznie w testach ARM64.

Rekomendowane jest połączenie obu rozwiązań: emulator testuje przepływ UI i kontrakty przez implementację testową, a osobny test ARM64 weryfikuje bibliotekę natywną.

---

## 8. Menedżer modeli GGUF

### 8.1. Lokalizacja modelu

Model nie może być umieszczony w APK ani w repozytorium Git. Plik powinien być pobierany na żądanie do prywatnego katalogu aplikacji, np.:

```text
files/models/
  qwen/
    qwen35-4b/
      model.gguf
      manifest.json
      verified.sha256
      model.state
```

Dokładna lokalizacja musi uwzględniać politykę backupu i cykl życia aplikacji.

### 8.2. Pobieranie

Menedżer modeli powinien zapewnić:

- jawne rozpoczęcie pobierania przez użytkownika;
- informację o rozmiarze i wymaganym wolnym miejscu;
- możliwość pobierania tylko przez Wi-Fi;
- wznowienie przerwanego transferu, jeżeli serwer obsługuje zakresy bajtów;
- zapis do pliku tymczasowego;
- kontrolę długości i integralności;
- atomowe przemianowanie pliku dopiero po weryfikacji;
- usunięcie niekompletnego pliku po anulowaniu;
- blokadę uruchomienia modelu, który nie przeszedł weryfikacji.

### 8.3. Weryfikacja integralności

Nie wystarczy sprawdzić, czy plik ma rozszerzenie `.gguf`. Wymagane są:

- SHA-256 pobranego pliku;
- porównanie z wartością przypiętą w manifeście wydania;
- odczyt nagłówka GGUF;
- weryfikacja architektury i metadanych;
- kontrola zgodności z wersją silnika;
- kontrola licencji i źródła.

Nie należy pobierać modelu z nieznanego repozytorium tylko dlatego, że ma w nazwie „Qwen”.

### 8.4. Aktualizacje i usuwanie

Aktualizacja modelu powinna być transakcyjna: pobrać nową wersję obok starej, zweryfikować, dopiero wtedy przełączyć aktywną wersję. Nie wolno usuwać działającego modelu przed potwierdzeniem nowego.

Użytkownik powinien mieć możliwość:

- sprawdzenia nazwy, wersji, kwantyzacji, licencji i rozmiaru;
- pobrania modelu;
- anulowania pobierania;
- wyboru modelu aktywnego;
- zwolnienia modelu z RAM;
- usunięcia pliku z pamięci;
- sprawdzenia sumy kontrolnej;
- odczytania informacji o ograniczeniach modelu.

---

## 9. Baza wiedzy prawnej i RAG offline

### 9.1. Zasada

LLM nie może być traktowany jako samodzielna, aktualna baza polskiego prawa. Wiedza parametryczna modelu może być nieaktualna, niepełna albo błędna.

Asystent musi otrzymywać właściwe przepisy z lokalnej bazy wiedzy, a odpowiedź musi być powiązana z konkretnymi źródłami.

### 9.2. Źródła

Podstawowe źródła do zasilania i aktualizacji bazy:

- Elektroniczny Dziennik Ustaw / ELI;
- ISAP jako pomocniczy dostęp do informacji o aktach i procesie legislacyjnym;
- oficjalne publikatory orzeczeń sądów;
- oficjalne strony organów i instytucji, jeżeli publikują stanowiska lub wytyczne.

Treści źródeł należy zachowywać z metadanymi, datą pobrania i informacją o wersji. Materiał nieoficjalny nie może być prezentowany jako tekst obowiązującego aktu.

### 9.3. Wersjonowanie temporalne

Każdy przepis powinien zawierać co najmniej:

- identyfikator aktu;
- tytuł aktu;
- publikator;
- jednostkę redakcyjną;
- treść;
- datę ogłoszenia;
- datę wejścia w życie;
- datę utraty mocy albo koniec obowiązywania;
- informację o zmianach;
- źródłowy URL;
- datę pobrania;
- identyfikator wersji źródłowej.

System musi odróżniać stan prawny na dzień zdarzenia, na dzień doręczenia decyzji i na dzień sporządzania analizy. Wybór daty właściwej dla konkretnej normy może zależeć od przepisów przejściowych i rodzaju sprawy.

### 9.4. Indeksowanie

Dla pierwszej wersji wystarczy:

- SQLite;
- tabele aktów i przepisów;
- SQLite FTS5 do wyszukiwania pełnotekstowego;
- indeksy po numerze artykułu, tytule, publikatorze i dacie;
- podział treści na jednostki redakcyjne i logiczne fragmenty.

Wektory i embeddingi można dodać w kolejnym etapie, ale nie są warunkiem uruchomienia pierwszego RAG. Najpierw należy uzyskać wiarygodne wyszukiwanie po tytułach, numerach artykułów, frazach i metadanych.

### 9.5. Format materiału przekazywanego do Qwen

Każdy fragment prawa przekazany modelowi powinien mieć strukturę:

```text
[ŹRÓDŁO]
Akt: ...
Publikator: ...
Jednostka redakcyjna: ...
Wersja obowiązująca od: ...
Wersja obowiązująca do: ...
Data pobrania: ...
URL źródłowy: ...

[TREŚĆ]
Dokładny tekst przepisu...
```

Model może analizować tekst i proponować interpretację, ale nie może zmieniać treści cytowanego przepisu ani przypisywać mu nieistniejących paragrafów.

### 9.6. Zasada odmowy odpowiedzi

Jeżeli baza nie zawiera aktualnej wersji przepisu, nie można ustalić daty obowiązywania albo źródła są sprzeczne, asystent powinien:

- oznaczyć analizę jako niekompletną;
- wskazać brakujące dane;
- nie generować kategorycznego wniosku;
- zaproponować aktualizację bazy lub konsultację z prawnikiem.

---

## 10. Trzy funkcje Asystenta Praw Obywatelskich

### 10.1. Analizator pism i żądań organów

Wejście:

- PDF, zdjęcie, tekst albo opis sytuacji;
- data otrzymania lub doręczenia;
- nazwa organu;
- numer sprawy, jeśli istnieje;
- status użytkownika w postępowaniu.

Proces:

1. OCR i ekstrakcja tekstu.
2. Klasyfikacja rodzaju dokumentu.
3. Wykrycie organu, daty, sygnatury i pouczenia.
4. Ekstrakcja żądania i wskazanych podstaw prawnych.
5. Wyszukanie przepisów w bazie lokalnej.
6. Sprawdzenie wersji temporalnej.
7. Ocena, czy przepis dotyczy danego rodzaju postępowania.
8. Wskazanie brakujących danych i możliwych interpretacji.
9. Utworzenie raportu z cytatami i źródłami.

Wynik nie powinien być prostym werdyktem „legalne/nielegalne”. Zalecane statusy:

- podstawa prawna potwierdzona w zakresie wskazanym w raporcie;
- podstawa warunkowa – potrzebne dodatkowe ustalenia;
- brak wystarczających danych;
- wykryto możliwą niezgodność wymagającą weryfikacji;
- źródła nieaktualne lub niewystarczające.

### 10.2. Generator pism

Generator powinien być oparty na typowanych szablonach i regułach, a nie na swobodnym poleceniu „napisz pismo”.

Obsługiwane typy w pierwszym zakresie:

1. wniosek o udostępnienie informacji publicznej;
2. petycja;
3. skarga lub wniosek w trybie działu VIII KPA;
4. odwołanie od decyzji administracyjnej;
5. wezwanie do usunięcia naruszenia – wyłącznie po ustaleniu, że taki środek ma zastosowanie;
6. wniosek o udostępnienie akt sprawy;
7. wniosek o sprostowanie danych osobowych lub danych w rejestrze;
8. wniosek o wskazanie podstawy prawnej żądania.

Każdy typ powinien mieć:

- kwalifikację prawną;
- wymagane pola;
- właściwego adresata;
- podstawę prawną;
- reguły terminu;
- listę załączników;
- reguły podpisu i wniesienia;
- walidację formalną;
- wersjonowany szablon.

LLM odpowiada za redakcję, klarowność i argumentację. Kod aplikacji odpowiada za pola obowiązkowe, typ pisma, adresata, termin i kontrolę struktury.

### 10.3. Nawigator procedur

Nawigator musi być maszyną stanów, a nie tylko rozmową z modelem.

Przykładowe stany:

- dokument otrzymany;
- dokument rozpoznany;
- kwalifikacja trybu nieustalona;
- ustalono właściwy środek;
- oczekiwanie na dane;
- projekt przygotowany;
- projekt sprawdzony;
- projekt zatwierdzony przez użytkownika;
- pismo wniesione;
- oczekiwanie na odpowiedź;
- odpowiedź otrzymana;
- sprawa zakończona;
- sprawa wymaga konsultacji.

Przejście między stanami musi mieć warunki i ślad audytowy. Model może proponować przejście, ale nie może samodzielnie oznaczyć pisma jako złożonego, jeżeli aplikacja nie ma potwierdzenia.

---

## 11. Terminy prawne – mechanizm deterministyczny

Obliczanie terminów nie powinno być delegowane do LLM.

Wymagany jest osobny moduł, który obsługuje:

- datę zdarzenia początkowego;
- sposób liczenia terminu wynikający z właściwego przepisu;
- dni ustawowo wolne;
- soboty i niedziele, jeżeli właściwa reguła ma zastosowanie;
- reguły przesunięcia końca terminu;
- wyjątki i przepisy szczególne;
- strefę czasową;
- sposób doręczenia i moment skutecznego doręczenia;
- niepewność daty doręczenia.

Wynik powinien zawierać uzasadnienie obliczenia i przepis, z którego wynika reguła. Jeżeli nie można ustalić daty doręczenia lub trybu, aplikacja nie może przedstawiać jednej daty jako pewnej.

---

## 12. Bezpieczeństwo, prywatność i granice lokalnego działania

### 12.1. Brak płatnego API

W docelowym trybie:

- brak klucza API;
- brak żądań do usług generatywnego AI;
- brak wysyłania dokumentów do zewnętrznego dostawcy modelu;
- brak zdalnej inferencji;
- lokalny model, lokalny OCR i lokalna baza prawa.

Internet może być używany do pobrania modelu i aktualizacji bazy prawa, ale po zakończeniu pobierania analiza i generowanie muszą działać bez sieci.

### 12.2. Rozdzielenie pobierania od inferencji

Menedżer pobierania i silnik inferencji powinny być osobnymi komponentami. Po załadowaniu modelu silnik nie może korzystać z OkHttp, WebView ani innych usług sieciowych.

Należy dodać test, który potwierdza, że wykonanie zadania AI nie powoduje połączenia sieciowego. Ponieważ aplikacja jako całość ma uprawnienie INTERNET, kontrola musi dotyczyć rzeczywistego zachowania modułu, a nie samego manifestu.

### 12.3. Ochrona dokumentów użytkownika

Wymagane:

- przechowywanie dokumentów w prywatnym katalogu aplikacji albo zachowanie kontrolowanego URI SAF;
- szyfrowanie danych sprawy i wyników, jeśli zawierają dane osobowe lub wrażliwe;
- klucze ochronne przechowywane w Android Keystore;
- brak pełnych treści dokumentów w logach;
- ograniczenie retencji plików tymczasowych;
- jawna operacja eksportu;
- informacja, że plik wyeksportowany poza aplikację podlega również ochronie miejsca docelowego;
- możliwość usunięcia sprawy wraz z pochodnymi plikami i indeksami.

Plik publicznego modelu GGUF nie musi być szyfrowany jako dane użytkownika. Szyfrowanie kilkugigabajtowego pliku modelu mogłoby powodować dodatkowy koszt odczytu i utrudnić mapowanie pamięci. Ochrona powinna koncentrować się na dokumentach, historii spraw, wynikach i kluczach.

### 12.4. Prompt injection i treści niezaufane

Dokumenty, strony internetowe, załączniki i tekst OCR są danymi, nie instrukcjami dla modelu.

Model nie może:

- wykonywać poleceń zawartych w analizowanym piśmie;
- samodzielnie uruchamiać kodu lub poleceń systemowych;
- uzyskiwać dostępu do innych dokumentów bez upoważnienia warstwy aplikacji;
- wysyłać treści na zewnątrz;
- podpisywać lub wysyłać pism;
- zmieniać danych sprawy bez jawnej operacji użytkownika.

Treść importowanego dokumentu musi być przekazywana jako cytowany, niezaufany materiał wejściowy.

### 12.5. Nadzór człowieka

Wszystkie pisma są projektami do sprawdzenia. Użytkownik musi mieć możliwość:

- odczytania pełnej treści;
- sprawdzenia źródeł;
- edycji;
- odrzucenia;
- zapisania;
- eksportu;
- podpisania i wniesienia samodzielnie.

Nie wolno przedstawiać wygenerowanego dokumentu jako urzędowego ani gwarantować, że organ go uwzględni.

---

## 13. Jakość prawna i kontrola halucynacji

### 13.1. Reguły walidacji

Każda odpowiedź prawna powinna być sprawdzana pod kątem:

1. istnienia wskazanego aktu;
2. istnienia artykułu, paragrafu i ustępu;
3. zgodności cytatu z tekstem źródłowym;
4. obowiązywania przepisu w odpowiednim czasie;
5. właściwości zakresu zastosowania;
6. zgodności typu pisma z trybem postępowania;
7. kompletności wymaganych danych;
8. zgodności terminu z regułą proceduralną;
9. braku twierdzeń o faktach nieprzekazanych przez użytkownika;
10. braku automatycznych klauzul odpowiedzialności karnej bez podstawy prawnej.

### 13.2. Format odpowiedzi modelu

Model powinien zwracać strukturę maszynową, a nie tylko swobodny tekst. Przykładowy schemat:

```json
{
  "classification": "administrative_decision",
  "confidence": "needs_review",
  "facts_extracted": [],
  "missing_information": [],
  "legal_sources": [
    {
      "act_id": "verified-local-act-id",
      "provision_id": "verified-provision-id",
      "source_version": "version-id",
      "quoted_text": "exact-source-excerpt",
      "application_reason": "explanation"
    }
  ],
  "procedure_candidates": [],
  "deadline": {
    "status": "not_calculated",
    "reason": "missing_service_date"
  },
  "draft_allowed": false,
  "warnings": []
}
```

Walidator powinien odrzucić nieznane identyfikatory przepisów, niezgodne pola i niepoprawne typy danych. Model nie może sam tworzyć identyfikatorów źródeł, które następnie zostaną uznane za prawdziwe.

### 13.3. Ocena jakości

Nie należy używać jednego ogólnego wyniku „AI accuracy”. Potrzebne są oddzielne metryki:

- trafność klasyfikacji dokumentu;
- kompletność ekstrakcji dat i sygnatur;
- precyzja odnalezienia przepisów;
- poprawność cytowania;
- poprawność doboru trybu;
- poprawność obliczenia terminu;
- kompletność formalna dokumentu;
- zgodność faktów projektu z dokumentem źródłowym;
- liczba nieuzasadnionych twierdzeń;
- liczba przypadków prawidłowego powstrzymania się od odpowiedzi.

---

## 14. Plan testów

### 14.1. Testy jednostkowe JVM

Wymagane obszary:

- parser manifestu modelu;
- walidacja SHA-256;
- walidacja metadanych GGUF;
- przejścia stanów menedżera modeli;
- obsługa niekompletnego pobierania;
- wybór aktywnego modelu;
- parser odpowiedzi strukturalnej;
- walidacja cytowań;
- dopasowanie przepisu do wersji czasowej;
- reguły terminów;
- walidacja szablonów pism;
- obsługa anulowania i błędów silnika.

### 14.2. Testy instrumentacyjne Android

- przyznanie i odebranie URI SAF;
- import PDF i obrazu;
- OCR bez dostępu do sieci po przygotowaniu wymaganych zasobów;
- brak modelu;
- pobieranie modelu;
- przerwanie pobierania;
- błędna suma kontrolna;
- brak wolnego miejsca;
- ładowanie i zwalnianie modelu;
- obrót ekranu podczas generowania;
- anulowanie generowania;
- przejście aplikacji do tła;
- odzyskanie po ubiciu procesu;
- usuwanie modelu;
- usuwanie danych sprawy;
- eksport wygenerowanego dokumentu.

### 14.3. Testy natywne

- inicjalizacja llama.cpp;
- odczyt GGUF;
- zgodność architektury Qwen;
- poprawność chat template;
- generowanie krótkiej odpowiedzi;
- anulowanie;
- wielokrotne ładowanie i zwalnianie;
- błędny plik modelu;
- niewystarczająca pamięć;
- obsługa błędów JNI;
- wycieki pamięci i zasobów.

### 14.4. Testy na fizycznym Xiaomi Pad 6

Testy na emulatorze nie zastępują testów na docelowym urządzeniu.

Wymagane scenariusze:

1. uruchomienie po świeżej instalacji;
2. pobranie modelu przez Wi-Fi;
3. weryfikacja integralności;
4. pierwsze ładowanie modelu;
5. 10 krótkich generacji;
6. analiza dokumentu 2–4 strony;
7. generowanie projektu pisma;
8. praca bez Internetu;
9. praca po przejściu aplikacji do tła;
10. 30-minutowy test obciążeniowy;
11. test przy niskim poziomie wolnej pamięci;
12. test po nagrzaniu urządzenia.

Raport z pomiaru musi wskazywać dokładny wariant modelu, kwantyzację, rewizję llama.cpp, wersję Androida, wolną pamięć przed testem i temperaturę.

### 14.5. Korpus testów prawnych

Należy przygotować co najmniej 100 scenariuszy, a docelowo większy korpus zatwierdzony przez osobę posiadającą kompetencje prawne.

Zakres:

- decyzja administracyjna;
- postanowienie;
- brak odpowiedzi organu;
- wezwanie do uzupełnienia braków;
- żądanie dodatkowego dokumentu;
- wniosek o informację publiczną;
- dostęp strony do akt;
- petycja;
- skarga powszechna;
- odwołanie;
- sprostowanie danych;
- nieaktualny przepis;
- brak pouczenia;
- błędne pouczenie;
- niepełna data doręczenia;
- przepisy szczególne;
- sprzeczne źródła;
- prompt injection w treści dokumentu.

Każdy przypadek powinien zawierać oczekiwany tryb, prawidłowe źródła, minimalny zakres danych, dopuszczalne wnioski i warunki, w których system powinien odmówić kategorycznego rozstrzygnięcia.

---

## 15. Kryteria akceptacji

### 15.1. Kryteria techniczne

- model pobiera się do prywatnego katalogu;
- model jest weryfikowany sumą SHA-256;
- niekompletny model nie może zostać uruchomiony;
- aplikacja nie zawiesza UI podczas inferencji;
- anulowanie generowania działa;
- model można zwolnić z RAM;
- brak modelu nie powoduje awarii aplikacji;
- działają pozostałe funkcje aplikacji po dodaniu biblioteki natywnej;
- podstawowy profil CPU ARM64 działa na Xiaomi Pad 6;
- brak wywołań płatnego API;
- analiza i generowanie działają po przejściu w tryb samolotowy.

### 15.2. Kryteria prawne

- żadne cytowanie nie może być zaakceptowane bez źródła w lokalnej bazie;
- nieaktualny przepis musi być oznaczony;
- brak daty doręczenia blokuje pewne wyliczenie terminu;
- typ pisma musi być zgodny z wybranym trybem;
- generator nie może dopisywać faktów;
- system rozróżnia skargę z działu VIII KPA od środka zaskarżenia decyzji;
- system nie utożsamia dostępu do akt strony z dostępem do informacji publicznej;
- system nie traktuje wezwania do usunięcia naruszenia jako uniwersalnego obowiązkowego etapu;
- żadne pismo nie jest wysyłane bez jawnej decyzji użytkownika.

### 15.3. Kryteria wydajnościowe

Progi liczbowe należy ustalić po pierwszym pomiarze na fizycznym urządzeniu. Raport powinien zawierać medianę i percentyl 95 dla czasu ładowania, czasu do pierwszego tokenu, szybkości generacji i całkowitego czasu zadania.

Nie należy dopuszczać modelu do profilu podstawowego, jeśli regularnie powoduje awarie pamięci, ubijanie procesu aplikacji lub niestabilność po nagrzaniu urządzenia.

---

## 16. Rejestr ryzyk

| ID | Ryzyko | Poziom | Działanie ograniczające |
|---|---|---|---|
| QW-01 | 4B Q4 nie mieści się stabilnie w dostępnej pamięci | Wysoki | Profil 2B jako awaryjny, test PSS, ograniczenie kontekstu |
| QW-02 | Zbyt wolne generowanie | Średni/wysoki | Pomiar CPU, optymalizacja wątków, opcjonalny Vulkan |
| QW-03 | Niewłaściwa obsługa architektury GGUF | Wysoki | Przypięcie wersji llama.cpp i test modelu |
| QW-04 | Niezweryfikowany model z zewnętrznego repozytorium | Krytyczny | Oficjalne źródło, pin rewizji, SHA-256, licencja |
| QW-05 | Halucynacja przepisu | Krytyczny | RAG, walidacja źródeł, brak kategorycznego wyniku bez źródła |
| QW-06 | Nieprawidłowy termin | Krytyczny | Deterministyczny kalkulator i testy reguł |
| QW-07 | Prompt injection w dokumencie | Wysoki | Treści dokumentów jako dane niezaufane, brak narzędzi wykonawczych |
| QW-08 | Wyciek treści w logach | Krytyczny | Redakcja logów, testy i przegląd logowania |
| QW-09 | Nieprawidłowe zwalnianie pamięci natywnej | Wysoki | RAII po stronie C++, testy długie i JNI |
| QW-10 | Regresja istniejących funkcji Android | Wysoki | Izolacja modułu, pełne CI i testy emulatora |
| QW-11 | Model Qwen nie ma odpowiedniej licencji lub wagi nie są otwarte | Wysoki | Weryfikacja karty modelu przed wydaniem |
| QW-12 | Model generuje pismo niezgodne z trybem | Krytyczny | Silnik proceduralny i walidator formalny |

---

## 17. Plan wdrożenia etapami

### Etap 0 – zamrożenie założeń i wybór artefaktu modelu

Zakres:

- wybrać konkretny model Qwen;
- sprawdzić licencję i kartę modelu;
- wybrać konkretną rewizję GGUF;
- ustalić nazwę pliku, rozmiar i SHA-256;
- przypiąć commit llama.cpp;
- sporządzić manifest modelu.

Warunek zakończenia: powtarzalny zestaw źródeł i artefaktów, bez odwołań do ruchomych gałęzi.

### Etap 1 – proof of concept na ARM64

Zakres:

- zbudować llama.cpp przez Android NDK;
- przygotować minimalny program testowy;
- uruchomić model Qwen na ARM64;
- zweryfikować chat template;
- zmierzyć pamięć i wydajność.

Warunek zakończenia: model wykonuje powtarzalną inferencję na fizycznym Xiaomi Pad 6.

### Etap 2 – integracja JNI

Zakres:

- biblioteka natywna;
- kontrakt Kotlin;
- obsługa strumieniowania tokenów;
- anulowanie;
- zwalnianie modelu;
- obsługa błędów i braku pamięci.

Warunek zakończenia: demonstracyjny ekran czatu działa bez blokowania UI.

### Etap 3 – menedżer modeli

Zakres:

- pobieranie GGUF;
- manifest;
- suma SHA-256;
- wznawianie i anulowanie;
- zarządzanie wolnym miejscem;
- wybór i usuwanie modelu.

Warunek zakończenia: użytkownik może samodzielnie pobrać, zweryfikować, uruchomić i usunąć model.

### Etap 4 – lokalna baza prawa

Zakres:

- model danych aktów i przepisów;
- importer źródeł oficjalnych;
- wersjonowanie temporalne;
- SQLite FTS5;
- wyszukiwanie i cytowanie źródeł.

Warunek zakończenia: aplikacja potrafi wskazać konkretny przepis i wersję z lokalnej bazy.

### Etap 5 – analizator i generator

Zakres:

- klasyfikacja pism;
- ekstrakcja żądań i terminów;
- RAG;
- walidacja źródeł;
- szablony ośmiu rodzajów pism;
- eksport do PDF i edycja.

Warunek zakończenia: scenariusze testowe przechodzą kontrolę formalną i prawną.

### Etap 6 – nawigator procedur

Zakres:

- maszyna stanów sprawy;
- kalkulator terminów;
- kontrola środków prawnych;
- historia czynności;
- przypomnienia;
- archiwizacja potwierdzeń.

Warunek zakończenia: aplikacja prowadzi sprawę przez kolejne etapy bez utraty historii i bez samodzielnego wysyłania dokumentów.

### Etap 7 – audyt bezpieczeństwa i wydanie

Zakres:

- testy prywatności;
- analiza ruchu sieciowego;
- testy prompt injection;
- testy pamięci;
- testy fizycznego urządzenia;
- testy regresji;
- dokumentacja licencji;
- dokumentacja użytkownika.

Warunek zakończenia: brak otwartych błędów krytycznych i wysokich, które blokują bezpieczne użycie.

---

## 18. Proponowane zadania implementacyjne

| ID | Zadanie | Priorytet | Zależności |
|---|---|---|---|
| QW-101 | Ustalenie i przypięcie wersji llama.cpp | P0 | Brak |
| QW-102 | Wybór i przypięcie GGUF Qwen | P0 | QW-101 niezależne |
| QW-103 | Android NDK/CMake ARM64 build | P0 | QW-101 |
| QW-104 | Minimalny benchmark Xiaomi Pad 6 | P0 | QW-102, QW-103 |
| QW-105 | Interfejs LocalLlmEngine | P1 | QW-103 |
| QW-106 | Most JNI i obsługa anulowania | P1 | QW-105 |
| QW-107 | ModelManager i manifest | P1 | QW-102 |
| QW-108 | Weryfikacja SHA-256 i GGUF | P1 | QW-107 |
| QW-109 | Testy ARM64 i x86_64/fake backend | P1 | QW-106 |
| QW-110 | Schemat lokalnej bazy prawa | P1 | Brak |
| QW-111 | Importer i wersjonowanie przepisów | P1 | QW-110 |
| QW-112 | Wyszukiwanie FTS5 | P1 | QW-110 |
| QW-113 | LegalAiOrchestrator i RAG | P2 | QW-106, QW-112 |
| QW-114 | Analizator pism | P2 | QW-113 |
| QW-115 | Generator pism | P2 | QW-113 |
| QW-116 | Silnik procedur i terminów | P2 | QW-110 |
| QW-117 | Nawigator sprawy | P2 | QW-114, QW-115, QW-116 |
| QW-118 | Korpus testów prawnych | P1 | QW-110 |
| QW-119 | Testy prywatności i prompt injection | P1 | QW-113 |
| QW-120 | Audyt wydania i dokumentacja | P0 przed wydaniem | Wszystkie |

---

## 19. Odniesienia do istniejących plików repozytorium

Poniższe pliki były punktami odniesienia w audycie:

- [Konfiguracja aplikacji Android](../../obywatel-android/app/build.gradle.kts)
- [Konfiguracja projektu Gradle](../../obywatel-android/build.gradle.kts)
- [Ustawienia Gradle](../../obywatel-android/settings.gradle.kts)
- [Manifest Android](../../obywatel-android/app/src/main/AndroidManifest.xml)
- [Parser OCR](../../obywatel-android/app/src/main/java/pl/gov/obywatel/android/DocumentOcrParser.kt)
- [Lokalny dostawca tłumaczeń ML Kit](../../obywatel-android/app/src/main/java/pl/gov/obywatel/android/MlKitMedicalTranslationProvider.kt)
- [Magazyn importowanych dokumentów](../../obywatel-android/app/src/main/java/pl/gov/obywatel/android/data/ImportedDocumentStore.kt)
- [Centralny rejestr tras dokumentów](../../obywatel-android/app/src/main/java/pl/gov/obywatel/android/DocumentRouteRegistry.kt)
- [Poprzedni audyt architektury Android](../../obywatel-android/AUDYT_ARCHITEKTURY.md)
- [Workflow kompilacji Android](../../.github/workflows/android-build.yml)
- [Workflow testów GUI Android](../../.github/workflows/android-gui.yml)

---

## 20. Źródła techniczne i modelowe

Źródła należy ponownie sprawdzić w chwili przypinania konkretnego wydania. Karty modeli, gałęzie repozytoriów i dokumentacja silnika mogą się zmieniać.

1. **Qwen3.5-4B – oficjalny model bazowy:**  
   https://huggingface.co/Qwen/Qwen3.5-4B

2. **Konwersja GGUF Qwen3.5-4B przygotowana dla llama.cpp:**  
   https://huggingface.co/ggml-org/Qwen3.5-4B-GGUF

3. **Oficjalne repozytorium Qwen3.8 i wykaz rodzin modeli:**  
   https://github.com/QwenLM/Qwen3.8

4. **llama.cpp – dokumentacja budowania i obsługiwane platformy:**  
   https://github.com/ggml-org/llama.cpp/blob/master/docs/build.md

5. **llama.cpp – dokumentacja Android:**  
   https://github.com/ggml-org/llama.cpp/tree/master/examples/llama.android

6. **Xiaomi Pad 6 – specyfikacja producenta:**  
   https://www.mi.com/global/product/xiaomi-pad-6/specs/

7. **Android NDK – dokumentacja oficjalna:**  
   https://developer.android.com/ndk

8. **Android Keystore – dokumentacja oficjalna:**  
   https://developer.android.com/privacy-and-security/keystore

9. **SQLite FTS5 – dokumentacja:**  
   https://www.sqlite.org/fts5.html

10. **ELI – Europejski Identyfikator Prawodawstwa:**  
    https://eli.gov.pl/

11. **ISAP – Internetowy System Aktów Prawnych:**  
    https://isap.sejm.gov.pl/

### Uwagi licencyjne

Przed dystrybucją modelu lub biblioteki należy zachować licencję i wymagane informacje o autorach. Licencja aplikacji nie zastępuje licencji modelu ani licencji llama.cpp. Należy sprawdzić warunki konkretnej rewizji modelu, konwersji GGUF, ewentualnych adapterów i wszystkich bibliotek natywnych.

---

## 21. Konkluzja i decyzja audytowa

### Ocena wykonalności

| Obszar | Ocena |
|---|---|
| Uruchomienie małego Qwen lokalnie na Xiaomi Pad 6 | Wykonalne do potwierdzenia testem fizycznym |
| Integracja llama.cpp z Androidem | Wykonalna, wymaga nowego modułu NDK/JNI |
| Brak płatnego API | W pełni możliwy |
| Analiza polskich pism | Możliwa z RAG i kontrolą źródeł |
| Generowanie projektów pism | Możliwe, wymaga szablonów i walidacji |
| Automatyczne ustalanie prawa wyłącznie z pamięci LLM | Niezalecane |
| Wiarygodne liczenie terminów przez LLM | Niedopuszczalne jako jedyny mechanizm |
| Praca całkowicie offline po pobraniu modelu i bazy | Możliwa, wymaga testu bez sieci |
| Wydajność 4B Q4 na docelowym tablecie | Niepotwierdzona – wymagany benchmark |
| Gotowość produkcyjna modułu AI | Nieosiągnięta w commicie bazowym |

### Decyzja

**Rekomenduje się rozpoczęcie od Etapu 0 i Etapu 1: przypięcia modelu Qwen3.5-4B GGUF oraz zbudowania i uruchomienia llama.cpp na fizycznym Xiaomi Pad 6.**

Nie należy rozpoczynać od tworzenia rozbudowanego interfejsu prawnego ani od wdrażania generatora pism, zanim nie zostanie potwierdzone, że model stabilnie działa na urządzeniu docelowym.

Najpierw należy udowodnić trzy rzeczy:

1. model ładuje się i generuje tekst na Xiaomi Pad 6;
2. aplikacja może wykonywać inferencję bez Internetu i bez płatnego API;
3. urządzenie zachowuje stabilność pamięciową i termiczną w zadaniach dłuższych niż krótki test demonstracyjny.

Dopiero po zamknięciu tych warunków można bezpiecznie rozwijać bazę prawa, analizator, generator i nawigator.

**Status końcowy audytu:** architektura rekomendowana; implementacja lokalnego Qwen – do wykonania; wydajność na urządzeniu – do pomiaru; jakość prawna – do walidacji na korpusie testowym. Nie stwierdzono w analizowanym commicie istniejącej integracji Qwen/llama.cpp.
