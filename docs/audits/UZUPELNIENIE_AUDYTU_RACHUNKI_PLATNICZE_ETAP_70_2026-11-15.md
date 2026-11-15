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
