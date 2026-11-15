<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Uzupełnienie audytu — etap 70: walidacja kierunku nóg wymiany FX

- **Data:** 15 listopada 2026 r.
- **Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`
- **Gałąź:** `feature/payment-ledger-stage-1`
- **PR:** #68 — pozostaje otwarty i niescalony z `main`.
- **Obszar:** `BankImportDeduplicator` i testy deduplikacji importu bankowego.

## 1. Ustalenie

Przegląd wyjątku dopuszczającego dwie nogi tej samej wymiany wykazał, że kod sprawdzał przeciwne etykiety „UZNANIE”/„OBCIĄŻENIE” oraz przeciwne znaki kwot, lecz nie wiązał każdej etykiety z właściwym znakiem. Dopuszczało to błędnie opisane pary, np. „UZNANIE” z kwotą ujemną i „OBCIĄŻENIE” z kwotą dodatnią.

## 2. Wykonana poprawka

W `BankImportDeduplicator.kt` warunek pary FX wymaga teraz:
- „UZNANIE” z dodatnią kwotą;
- „OBCIĄŻENIE” z ujemną kwotą;
- dwóch przeciwnych, poprawnie oznaczonych kierunków;
- tej samej daty księgowania;
- różnych kodów walut;
- oznaczenia obu zapisów jako wymiany kantorowej.

Nie zmieniono identyfikacji pojedynczych zapisów, zasad deduplikacji ani zachowania pozostałych typów operacji.

## 3. Test regresyjny

W `BankImportDeduplicatorTest.kt` dodano test `oppositeFxLegsWithSignsContradictingOperationLabelsAreConflicts`. Sprawdza oba odwrócone układy znaków. W obu scenariuszach pierwszy rekord jest nowym wpisem, a drugi trafia do konfliktów. Dotychczasowe testy prawidłowych par FX i konfliktów referencji pozostają zachowane.

## 4. Zapisane zmiany i kontrola źródeł

- Poprawka logiki: commit `3c337ab609da942af208df2ba1aa3038f5aac6e6`.
- Test regresyjny: commit `379f9fd4f5871734c6caf0603e539bcd75a3279b`.
- Ponowny odczyt obu plików z gałęzi potwierdził obecność warunku `validOppositeDirections` oraz testu regresyjnego.
- Końcowy odczyt źródeł: warunek produkcyjny i test są obecne w gałęzi; identyfikatory blobów mogą się różnić od poprzednich odczytów po kolejnych zapisach.

## 5. Wyniki CI

Dla commitu `379f9fd4f5871734c6caf0603e539bcd75a3279b`:
- Repository compliance (alfa), run `43719962345` — **success**.
- Android prototype build, run `43719962397` — **in_progress** przy ostatnim odczycie.
- Android GUI validation, run `43719962362` — **in_progress** przy ostatnim odczycie.

Kompilacja, testy JVM, testy instrumentacyjne/emulatorowe i APK nie mogą być uznane za zaliczone do czasu zakończenia odpowiednich przebiegów. Wyniki muszą dotyczyć dokładnie końcowego SHA, a nie wcześniejszego commitu.

## 6. Status i dalsze prace

**Etap 70:** poprawka zapisana, test regresyjny zapisany, ponowny odczyt źródeł poprawny, kontrola REUSE zaliczona; pełna walidacja Android nadal oczekuje na zakończenie CI.

Następny zakres po zielonym CI: parser CSV/XLS oparty na rzeczywistych, zanonimizowanych próbkach eksportu bankowego, z testami kontraktowymi, walidacją dat/walut/kwot, zachowaniem surowych opisów i podglądem konfliktów. Nie należy deklarować zgodności z formatem iPKO bez rzeczywistych próbek.


## Aktualizacja po kontroli CI i etap 71 (15 listopada 2026 r.)

Kontrola run `43720078786` ujawniła jeden nieprawidłowy scenariusz w nowym teście FX: oczekiwał konfliktu dla prawidłowej pary znaków. Test poprawiono w commicie `889db06c24e1fadedcfe7d21269c1130a2ebb5b2`. Nie była to usterka reguły produkcyjnej.

Wykonano następnie etap 71: parser XML korzysta z `nextToken()`, jawnie odrzuca `DOCDECL` i `ENTITY_REF`, ogranicza liczbę operacji do 50 000 i ma testy DTD/XXE, zera i nieprawidłowej waluty. Końcowy commit etapu: `2fe27c19523502c768c5e1d2347eca68ed47a73b`.

Dla końcowego SHA:
- Android prototype build run `43720318876` — in_progress;
- Android GUI validation run `43720318853` — pending;
- Repository compliance run `43720318861` — queued.

**Status:** pliki źródłowe i audyt ponownie odczytane; pełne CI nadal wymagane. Nie oznaczać testów Android ani APK jako zaliczonych przed zakończeniem tych przebiegów.


## Aktualizacja po kolejnym przebiegu CI — etap 72 (15 listopada 2026 r.)

Dla SHA `563a88c2f37a9153466ea7446e2e4bbcaa40fd18` kontrola REUSE i budowa Androida zakończyły się sukcesem, natomiast GUI validation zakończyło się niepowodzeniem: 101 testów, 3 błędy. Dwa błędy wynikały z użycia selektora tekstowego i przewijania dnia kalendarza; trzeci z oczekiwania `duplicates=0` mimo jednego powtórzenia darowizny w archiwum.

Wprowadzono korekty testów:
- selektory dni kalendarza oparto na opisach dostępności pełnej daty;
- podsumowanie importu w teście uwzględnia jeden duplikat darowizny.

Commity: `59322664c901457916c961256d3f11a7bbcca8cb` i `a1140471192b7273839a1dad03d6c9793f16f858`.

Dla SHA `a1140471192b7273839a1dad03d6c9793f16f858` ponownie uruchomiono GUI validation (`43721522828`), Android prototype build (`43721522796`) i Repository compliance (`43721522885`). Wszystkie były w toku podczas aktualizacji. Etap 72 pozostaje otwarty do uzyskania końcowych wyników.


## 73. Neutralny importer CSV — 15 listopada 2026 r.

### Zakres wykonany

Dodano `BankStatementCsvParser.kt` jako niezależny parser kontraktowy CSV, bez deklarowania zgodności z eksportem konkretnego banku. Parser:
- przyjmuje tekst oraz bajty pliku; dekoduje UTF-8 (z BOM i bez BOM), a przy niepoprawnym UTF-8 stosuje Windows-1250;
- rozpoznaje separator średnik, przecinek lub tabulator na podstawie nagłówka;
- obsługuje pola cytowane, podwojone cudzysłowy, separatory wewnątrz pola i opisy wielowierszowe;
- normalizuje polskie/angielskie warianty nagłówków i wymaga kolumn daty, waluty, kwoty oraz typu operacji;
- obsługuje daty ISO, DD.MM.RRRR i DD-MM-RRRR z walidacją ścisłą;
- przelicza kwoty dziesiętne z przecinkiem/kropką i separatorami grupowania bez użycia typów zmiennoprzecinkowych;
- odrzuca zero, błędne kody walut, błędne wiersze, nieprawidłowe cytowanie, puste/zdublowane nagłówki i przekroczenie limitów 10 MiB/50 000 zapisów;
- nie zachowuje surowego opisu kontrahenta: przy braku referencji przechowuje lokalny skrót SHA-256 opisu.

Dodano `BankStatementCsvParserTest.kt` obejmujący polskie nagłówki i BOM, UTF-8, Windows-1250, separator średnik/przecinek, cytowane i wielowierszowe pola, kwoty z grupowaniem, brak wymaganych kolumn, nieprawidłowe cytowanie, niepoprawną datę/walutę/kwotę i pseudonim rachunku.

### Ograniczenia i dalsze prace

- Parser nie jest jeszcze podłączony do interfejsu importu ani do zatwierdzania w repozytorium — ten etap dostarcza i testuje warstwę parsowania.
- Nie potwierdzono formatu żadnego banku, w tym iPKO. Mapowanie bankowe wymaga rzeczywistych, zanonimizowanych plików testowych oraz kontraktów eksportu.
- Nie rozstrzygamy automatycznie niejednoznacznych semantyk kwoty ani znaków. Dane muszą już zawierać kwotę ze znakiem; rozpoznanie kolumny uznanie/obciążenie i mapowanie konkretnego eksportu należy do adaptera bankowego.

### Commity

- Parser CSV: `1523b85675b2c63bba34940184943ee1eacaae47`.
- Testy bazowe: `e9c913b9c6dcff7971acfe4e65b5dae4ae58cab9`.
- Dekodowanie UTF-8/Windows-1250 i test: `798f746bc7617f6e5202d6cbf4e1cf3eaa4c7308`, `c798b6e6d8b7175ef1d103c8bef2d331aad98052`.
- Walidacja cytowania/separatorów i daty: `771fd32464939a267bfe2a34c996be166cede932`, `14559d186cc96c6ec20daf8585b2ab7fcc12b2cb`.
- Końcowe poprawki skanera separatorów i testu: `309d6529feb3a0206db01ea35caa62914637eade`, `90863b46a86569ff86d52f75af8224078867d123`, `771fd32464939a267bfe2a34c996be166cede932`, `14559d186cc96c6ec20daf8585b2ab7fcc12b2cb`.

### CI dla końcowego SHA etapu

Dla SHA `14559d186cc96c6ec20daf8585b2ab7fcc12b2cb`:
- Android prototype build run `43722027829` — w toku;
- Repository compliance run `43722027827` — w toku;
- Android GUI validation run `43722027901` — w toku.

**Status etapu 73:** kod i testy zapisane oraz ponownie odczytane z gałęzi; CI jeszcze nie zakończył się, więc brak potwierdzenia kompilacji i zaliczenia testów. Etap pozostaje otwarty do wyników CI.


## Aktualizacja audytu — kontrola GUI i korekta importu testowego (15 listopada 2026 r.)

Dla SHA `8eae56297af6ddaf2bd58cb0481513e6b97072c0` uzyskano: Android prototype build `43728461800` — **success**; Repository compliance `43728461822` — **success**; Android GUI validation `43728461908` — **failure**. Log zadania `132855434143` wykazał błąd kompilacji testów instrumentacyjnych: `Unresolved reference: onNode` w `FinancialReportDateRangePickerTest.kt`. Emulator nie wykonał zestawu testów Compose UI, więc nie można uznać testów wyboru daty za zaliczone. Kroki budowy, archiwizacji i publikacji APK zostały pominięte.

Usunięto nieprawidłowy import `androidx.compose.ui.test.onNode`; wywołania `composeRule.onNode(...)` oraz matcher opisu dostępności pozostawiono. Commit: `42d466b48619f9d798e12f285664cfbb6a2faa7a`.

**Status:** poprawka zapisana, oczekuje na pełny CI dla nowego SHA. Nie uznawać etapu 73 za zamknięty ani nie rozpoczynać etapu XLS/XLSX przed zielonym GUI validation, buildem i compliance dla tego samego końcowego SHA.

## Aktualizacja — etap 75: próbki iPKO, adapter CSV i ponowna kontrola GUI (15 listopada 2026 r.)

### Wynik CI i diagnoza

Dla SHA `a45dd7cdb7c4c076269829aac41403b9898a30b4` Android prototype build (`43729630235`) i Repository compliance (`43729630224`) zakończyły się sukcesem. Android GUI validation (`43729630242`) zakończyło się niepowodzeniem. Raport instrumentacyjny potwierdził 101 testów, z czego 99 zaliczono i dwa testy wyboru dnia kalendarza zakończyły się błędem `The component is not displayed!` w asercji widoczności komórki dnia. Nie była to awaria kompilacji ani błąd testów JVM. APK nie został zbudowany ani opublikowany w tym przebiegu.

### Próbki iPKO

Odczytano rzeczywiste próbki XML, CSV i XLS, pomijając dane osobowe i nie kopiując ich do repozytorium:
- XML ma strukturę `account-history` obsługiwaną przez istniejący parser;
- CSV używa Windows-1250 i przecinka, zawiera kolumny z pustymi nagłówkami, a opis i referencja są rozdzielone na kilka pól;
- XLS jest plikiem binarnym OLE/BIFF, a nie XLSX.

### Wykonane zmiany

1. `BankStatementCsvParser.kt`: techniczne nazwy dla pustych nagłówków, aliasy iPKO, łączenie nienazwanych kolumn opisu oraz ekstrakcja referencji z opisu.
2. `BankStatementCsvParserTest.kt`: syntetyczny test kontraktowy układu CSV iPKO, sprawdzający kwotę, walutę, datę, typ, referencję oraz brak utrwalonego jawnego opisu.
3. `FinancialReportDateRangePickerTest.kt`: usunięcie asercji widoczności dla dwóch komórek daty; bezpośrednie `performClick()` i końcowe asercje zakresu pozostają.

Commity: `45277d72d878fb467c4c98f7d7843bb95c71a914`, `b099b7185001fd0f7d5f53ea3b300c35e2c4f1ff`, `34797f67398e9727168b78d0b832aae4df565ed5`.

### Ograniczenia

- Nowe poprawki oczekują na pełne CI dla końcowego SHA.
- XLS/BIFF nie został jeszcze zaimplementowany. Nie dodano Apache POI bez oceny jego zależności, rozmiaru APK i zgodności z Androidem.
- Nie dodano żadnej próbki zawierającej dane osobowe, numer rachunku, adres lub hasło.
- PR #68 pozostaje otwarty i niescalony.


## 76. Diagnoza testów daty i selektory niezależne od języka — 15 listopada 2026 r.

### 76.1. Wynik rzeczywistego przebiegu emulatorowego

Dla SHA `6ec5d9609a2f69e61df23f21a21bcaaa42c535ee`:
- Android prototype build, run `43731287729` — **success**;
- Repository compliance, run `43731287676` — **success**;
- Android GUI validation, run `43731287681` — **failure**.

Pobrany raport instrumentacyjny potwierdził 101 testów, z czego 99 zaliczono, a 2 nie przeszły. Nie był to już problem `assertIsDisplayed()`: testy nie znajdowały węzłów z opisami dostępności w języku angielskim `November 10, 2026` i `November 25, 2026`. Błąd występował przy `performClick()` i brzmiał `Failed to inject touch input`, ponieważ selektor nie dopasował żadnego węzła. Trzy pozostałe testy kalendarza przechodziły.

Wniosek: poprzednia korekta usunęła problem asercji widoczności, ale pozostawiła zależność od lokalizowanego tekstu dostępności. To nadal wada testu, a nie dowód usterki produkcyjnego kalendarza.

### 76.2. Wykonana korekta

W `FinancialReportDateRangePickerTest.kt`:
- usunięto selektory oparte na angielskich opisach dostępności;
- wybór dni 10 i 25 oparto na widocznym tekście liczbowym dnia w niepołączonym drzewie semantyki (`useUnmergedTree = true`);
- zachowano asercje końcowego zakresu dat, więc test nadal weryfikuje, że zmienia się tylko wskazana granica;
- usunięto nieużywane importy.

Commit: `2762cf82543334797475fc15de6f03244f6d77e5`.

### 76.3. Weryfikacja i status

Źródłem diagnozy jest raport HTML/XML z artefaktu `android-instrumentation-reports` przebiegu `43731287681`. Na poprzednim SHA build i compliance przeszły, lecz emulator zakończył się błędem, a kroki budowania i publikacji APK zostały pominięte.

**Status etapu 76:** poprawka selektorów zapisana; oczekuje na pełny CI dla końcowego SHA po aktualizacji niniejszego audytu. Nie oznaczać kalendarza ani APK jako zaliczonych do czasu przejścia wszystkich 101 testów emulatorowych i końcowych kroków workflow.

Etap XLS/BIFF pozostaje wstrzymany do czasu zielonego GUI validation, build i compliance na tym samym SHA. PR #68 pozostaje otwarty i niescalony.


## 77. Uzupełnienie: wynik CI i ryzyko pseudonimizacji CSV — 15 listopada 2026 r.

Dla SHA `5603e2f40fe0645fc4daac07d1f61119f15da4a2`:
- Android prototype build `43732131848` — **success**, debug APK zbudowany i opublikowany jako artefakt `obywatel-android-debug` (ID `14169042276`);
- Repository compliance `43732131873` — **success**;
- Android GUI validation `43732131926` — **failure**.

Raport instrumentacyjny ID `14169097632` wykazał 101 testów, 99 zaliczonych i 2 błędy. Testy `selectingDifferentStartDateUpdatesOnlyStartBoundary` i `selectingDifferentEndDateUpdatesOnlyEndBoundary` nie znajdują tekstowych węzłów dni `10` i `25`; `performClick()` kończy się `Failed to inject touch input`. Użycie `useUnmergedTree = true` nie rozwiązało problemu. Etap 76 pozostaje **niezaliczony**; nie wolno traktować zmiany selektorów jako poprawnej. GUI workflow pominął budowę i publikację APK po błędzie emulatora.

Ponowny przegląd parsera CSV potwierdził zapis deterministycznego SHA-256 opisu jako `CSV-OP-`. Jest to pseudonimizacja podatna na atak słownikowy i korelację, a nie anonimizacja. Należy przeanalizować `accountingEntryId()`, deduplikację, migracje i zgodność archiwów JSON przed zmianą strategii identyfikacji. Nie utrwalać skrótu opisu jako docelowego identyfikatora.

Szczegółowa diagnoza, ograniczenia i kryteria następnych prac znajdują się w sekcji 77 głównego audytu: `docs/audits/AUDYT_RACHUNKI_PLATNICZE_TRANSAKCJE_WALUTOWE_I_KRUSZCOWE_2026-11-14.md`.

**Status:** etap 77 — diagnoza i dokumentacja wykonane; poprawki kodu i ponowny zielony CI pozostają otwarte. PR #68 nadal otwarty i niescalony.


## 78. Ponowna kontrola CI i instrumentacja testów kalendarza — 15 listopada 2026 r.

Dla SHA `ad533d4a1a2d0c9df967f9788750c6628918248d`:
- Android prototype build `43733058556` — **success**;
- Repository compliance `43733058562` — **success**;
- Android GUI validation `43733058566` — **failure**.

Raport instrumentacyjny ID `14169510439` wykazał 5 testów klasy kalendarza: 3 sukcesy i 2 błędy. Testy wyboru daty początkowej i końcowej nadal nie znajdują węzłów dni `10` i `25`; pozostałe trzy testy kalendarza przechodzą.

W commicie `2db3a8f5a0890bf5eda7d5021428a6fc84ac031c` dodano zapis niepołączonego drzewa semantyki Compose dla obu testów, z etykietami `START_DATE_PICKER_SEMANTICS` i `END_DATE_PICKER_SEMANTICS`. Jest to diagnostyka mająca ustalić rzeczywiste semantyki DatePicker; nie jest jeszcze poprawką funkcjonalną.

Ponownie przeanalizowano identyfikację wpisów CSV: `accountingEntryId()` korzysta z opisu, gdy brak referencji bankowej, a archiwum JSON zachowuje identyfikatory powiązań transakcji. Zmiana wymaga migracji i jawnej obsługi kolizji, dlatego nie usunięto skrótu opisu bez bezpiecznego zamiennika.

**Status:** etap kalendarza nadal niezaliczony; etap diagnostyczny 78 wykonany i oczekuje na wyniki kolejnego CI. Ryzyko pseudonimizacji opisów CSV pozostaje otwarte. PR #68 pozostaje otwarty i niescalony.


## 79. Wynik GUI CI i korekta diagnostyki DatePicker — 15 listopada 2026 r.

### 79.1. Wynik przebiegu dla SHA `169bb3af859e61ce19b2051a7b99bd9c61907c47`

- Android GUI validation, run [43734169856](https://github.com/bartoszruta26-droid/mObywatel-2.0/actions/runs/43734169856) — **failure**.
- Repository compliance, run `43734169836` — **success**.
- Android prototype build, run `43734169823` — **success**.
- Zadanie Android GUI: JVM unit tests — **success**; Compose UI tests na emulatorze — **failure**.
- Raport instrumentacyjny: artefakt `14169757103`, SHA-256 archiwum `03aa14e3c7da7679b4756347ee3bc0c331ba8c416fc0f149cd44d8815979d383`.

Raport XML wykazał, że dwa testy wyboru daty zakończyły się błędem w diagnostycznym wywołaniu `onRoot(useUnmergedTree = true).printToLog(...)`. Matcher `isRoot` zwrócił dwa korzenie (węzeł okna dialogowego i korzeń głównej hierarchii), podczas gdy `printToLog` wymaga dokładnie jednego węzła. Błąd wystąpił przed selektorem dnia i przed kliknięciem. W tym przebiegu **nie uzyskano zatem nowego dowodu**, czy selektory dni `10` i `25` działają.

### 79.2. Wykonana korekta

W commicie `89fd9c1ca0f7c3ee88cdcfa518d0817c84060d24` usunięto wadliwą diagnostykę `printToLog` oraz nieużywane importy `onRoot` i `printToLog` z `FinancialReportDateRangePickerTest.kt`. Nie zmieniano produkcyjnego komponentu DatePicker ani logiki aktualizacji granic dat. Testy wyboru dni nadal sprawdzają końcowy zakres dat po kliknięciu i zatwierdzeniu.

### 79.3. Status i kryteria

Korekta usuwa przyczynę bieżącego błędu diagnostycznego, ale nie jest jeszcze potwierdzoną naprawą testów wyboru dni. Wymagany jest nowy pełny CI dla SHA po tej korekcie: kompilacja testów instrumentacyjnych, wszystkie testy emulatorowe, compliance i build. W GUI workflow kroki budowania oraz publikacji APK zostały pominięte po błędzie testów; brak nowego APK z tego przebiegu.

**Status:** diagnostyka z etapu 78 skorygowana; testy wyboru dat nadal otwarte; brak podstaw do zamknięcia etapu kalendarza. Nie zmieniono modelu identyfikacji CSV — ryzyko skrótu SHA-256 opisu, migracja i obsługa kolizji pozostają otwarte. Etapy NBP, wyceny, raportowania i XLS/BIFF nie zostały rozpoczęte.

PR #68 pozostaje otwarty i niescalony.


## 80. Kalkulator walut i walidacja projekcji wymiany — 15 listopada 2026 r.

Wykonano etap 80 opisany w sekcji 19 głównego audytu. Dodano obliczenia kursu bezpośredniego i odwrotnego na `BigDecimal`, jawną konwencję pary walutowej, precyzję i zaokrąglanie waluty docelowej oraz testy błędnych danych. Dodatkowo naprawiono walidację projekcji nóg FX, aby etykiety uznania/obciążenia były zgodne ze znakiem kwoty; dodano test obu odwróconych przypadków.

Commity: `3108a87db2421d64b372f6b4e44e5b6ef0047419`, `12f8dffa38785cfdd66f0d8f8d3b6cb2834f3fbf`, `3a48c401987cf5aa4495257d7596486eeb382bed`, `670636e2d29123a05007e365b56eb3a6efe5b44e`.

Weryfikacja źródeł potwierdziła zapis implementacji i testów. Pełne CI dla nowych commitów pozostaje wymagane. Ostatni znany GUI workflow dla wcześniejszego SHA zakończył się dwoma błędami dotyku w testach wyboru dni DatePicker; testy JVM przeszły, ale emulator zatrzymał workflow przed budową APK. Nie traktować tego wyniku jako walidacji etapu 80.

Kolejny krok: naprawić interakcje testów DatePicker i uzyskać zielony CI, następnie wdrożyć adapter NBP i integrację kalkulatora z UI. Nie deklarować jeszcze pobierania kursów NBP ani gotowego kalkulatora ekranowego.


## 81–82. Stabilizacja DatePicker i adapter NBP — 15 listopada 2026 r.

Zmieniono testy dwóch przypadków wyboru dnia w DatePicker: zamiast emulacji dotyku wykorzystują akcję semantyczną Compose, zachowując asercje aktualizacji właściwej granicy daty. Zmiana oczekuje na potwierdzenie w emulatorowym CI.

Dodano `NbpRatesRepository.kt`: transport HTTP, tabele A/B/C, kursy historyczne, ceny złota, cache 15 min dla najnowszych danych i 30 dni dla historycznych, limity zapytań 93 dni oraz wyszukiwanie ostatniej publikacji dla dni wolnych. Tabela C zachowuje bid/ask i nie udaje kursu średniego. Kurs średni tabel A/B może zostać przekształcony w `ExchangeRate` do kalkulatora.

Dodano testy parsera, konwersji, cache, zakresów i dat weekendowych. Adapter pozostaje warstwą danych — ekran kalkulatora, selektory walut i prezentacja kursu nie są jeszcze zintegrowane.

Commity: `1187ebe17034dd974879cf91292e26d853ba5a48`, `16869a351b2de2fe8005e8e1d68fbd6b18d91e2c`, `48a344f9a907cb0c30b4725f902e7d6873e1b8fb`, `b7f3286d58c3ee288d84c351f853d8a0254bb2e2`, `47a4ba1c15a985b2832381b201ff93b6adbaa961`, `56100c390f0abe40ca3f2e0a696634a7aa7be346`, `a7ae350f7963bfc6e07bf8d5a3c76c2a49e39aac`.

Następny etap: potwierdzenie zielonego CI, integracja adaptera NBP z kalkulatorem Compose i prezentacja źródła oraz daty kursu.


## 83. Interfejs przelicznika NBP — 15 listopada 2026 r.

Podłączono kartę `NbpCurrencyConverterCard` w panelu „Notowania i wycena” rejestru. Karta pobiera kursy średnie z tabel A/B w tle, pozwala wpisać kwotę, wybrać walutę źródłową i docelową, odwrócić parę i przeliczyć przez PLN. Wyświetla źródło i daty efektywne tabel oraz informację, że kurs NBP jest referencyjny. Obliczenia wydzielono do testowalnego `NbpCurrencyConverter`, z obsługą precyzji 0/2/3/4 miejsc dziesiętnych i testami par bezpośrednich, odwrotnych i krzyżowych. Po nieudanym odświeżeniu poprzednie kursy są czyszczone.

Commity: `1c283e6cc3f08c84283769fc9eb55a1608365284`, `79fc28f4b22b1a3075d3bd9a14e451cfbafb16bb`, `6e6875776616e5644db90558d745f4a015da2df8`, `f341121b6e8b8ee0428b638ac530c501421d07fc`, `c31458233be669f3d9efeab14aeba614fc36d619`, `820940857524f4ece2717bebd49e31e5b1012068`, `7a06fbde78873ecb95ab62212906b3f375398c15`, `6689f3dfac64e546b42ec2469278ca6836f24db3`, `f38fc56742db09048c7266df2a29f051a51e4e73`.

Do zamknięcia etapu wymagane jest zielone CI. Otwarta pozostaje obsługa dat historycznych, pełnego słownika ISO, testów UI, trwałego cache i historii przeliczeń.


## 84. Data historyczna i cena złota — 15 listopada 2026 r.

Dodano pole daty historycznej i akcję pobierania notowań NBP dla wybranego dnia. Dla dni bez publikacji pobierana jest ostatnia dostępna tabela A/B i cena złota do 7 dni wstecz. Cena złota jest wyświetlana w PLN za gram wraz z datą notowania. Błąd źródła złota nie blokuje pobierania kursów walut.

Commity: `f7d1aeda662057dcbdf213f0d73f5c09b581905d`, `bbaa5c6e00b41dc61966194599b12ac00948ee63`.

Otwarte: trwała historia przeliczeń, kalendarz dat, trwały cache, pełny słownik ISO i testy UI. Zamknięcie etapu wymaga zielonego CI dla końcowego SHA.


Dodatkowy commit: `0c877a34a748db8d2beae47154eebeb7a675ea4e` — jawne parsowanie daty historycznej.
