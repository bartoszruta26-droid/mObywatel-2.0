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
