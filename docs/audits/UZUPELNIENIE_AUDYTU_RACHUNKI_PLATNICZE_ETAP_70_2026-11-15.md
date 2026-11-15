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
