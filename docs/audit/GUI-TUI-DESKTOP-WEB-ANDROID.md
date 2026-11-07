<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: CC0-1.0
-->

# Audyt GUI/TUI: TUI, Desktop GUI, Web UI i Android GUI

**Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`  
**Gałąź audytowa:** `audit/p0-hardening-2026-11`  
**Audytowany commit (HEAD):** `d40bcf1f06fe965b7a9500ecfd572763d29d4f8e`  
**Punkt odniesienia:** `master` (`8e2efd9890416bfc29b9e8c824d891405ca842e6`)  
**Data:** 2026-11-07  
**Zakres:** statyczny audyt struktury, konfiguracji, kodu UI i dostępnych wyników GitHub Actions. Nie wykonywano ręcznych testów na urządzeniu, czytniku ekranu ani w przeglądarkach.

## 1. Podsumowanie wykonawcze

Repozytorium jest przede wszystkim publicznym wycinkiem zasobów i komponentów interfejsu dla Androida i iOS, a nie gotowym wieloplatformowym klientem. Drzewo analizowanej gałęzi zawiera 1 072 pliki: 196 w katalogu `Android/`, 846 w `iOS/` i osobny szkielet `obywatel-android/`. W Androidzie znajduje się około 192 plików Kotlin, natomiast w iOS 102 pliki Swift; znaczna część pozostałych plików iOS to zasoby graficzne.

**Najważniejsza konkluzja:** Android ma rozwijany prototyp interfejsu i lokalnego portfela plików; TUI, natywna aplikacja desktopowa i pełna aplikacja webowa nie są obecne jako odrębne produkty. Repozytorium nie zawiera też wspólnej warstwy aplikacyjnej, która zapewniałaby równoważne zachowanie tych czterech kanałów.

## 2. Wyniki według kanału

### 2.1 TUI — terminalowy interfejs użytkownika

**Status: brak implementacji (P2 — decyzja produktowa).**

- Nie znaleziono odrębnego programu CLI/TUI, pakietu terminalowego, modelu komend ani konfiguracji biblioteki TUI.
- Wyszukiwanie nazw i ścieżek związanych z terminalem, konsolą, curses i CLI nie wykazało klienta terminalowego.
- Nie ma udokumentowanego kontraktu poleceń, automatyzacji, obsługi błędów terminalowych, nawigacji klawiaturowej ani testów interfejsu tekstowego.

**Rekomendacja:** najpierw zdefiniować, czy TUI ma służyć diagnostyce i administracji lokalnej, czy ma być równorzędnym interfejsem użytkownika. Nie budować imitacji urzędowych funkcji ani dostępu do danych bez jawnego, bezpiecznego kontraktu usług.

### 2.2 Desktop GUI — Windows, macOS, Linux

**Status: brak natywnego klienta desktopowego (P2 — decyzja produktowa).**

- W drzewie nie znaleziono katalogu aplikacji desktopowej ani projektów Windows/Linux/macOS.
- Katalog `iOS/` zawiera źródła Swift i zasoby; nie stanowi dowodu istnienia macOS desktop GUI.
- Nie znaleziono konfiguracji pakowania desktopowego, instalatorów, aktualizacji, podpisywania aplikacji desktopowych ani testów dla desktopu.

**Rekomendacja:** wybór technologii powinien zależeć od celu: osobny klient natywny, wspólna aplikacja Kotlin Multiplatform/Compose Multiplatform albo PWA. Należy odrębnie zaplanować obsługę skalowania, myszy, klawiatury, wielu okien, nawigacji, schowka, drukowania i czytników ekranu.

### 2.3 Web UI

**Status: brak pełnej aplikacji webowej (P2 — decyzja produktowa).**

- Nie znaleziono `package.json`, typowych konfiguracji React/Vue/Angular/Vite/Next ani katalogu aplikacji webowej.
- Pliki HTML/SVG i nazwy zasobów związane z WEB nie oznaczają działającego klienta webowego.
- Nie ma wykazanej konfiguracji routingu webowego, testów przeglądarkowych, procesu budowania i wdrażania SPA/PWA ani polityk CSP.
- Nie ma potwierdzonej warstwy responsywności ani testów WCAG w przeglądarce.

**Rekomendacja:** przed wdrożeniem Web UI przygotować model zagrożeń i architekturę uwierzytelniania; dodać testy E2E, kontrolę dostępności, responsywność i bezpieczne zarządzanie sesją. Nie przenosić wprost wzorców Androida do przeglądarki.

### 2.4 Android GUI

**Status: istniejący prototyp; część prac P1 jest wdrożona na gałęzi audytowej.**

Zidentyfikowane elementy:
- `obywatel-android/`: Kotlin, Jetpack Compose, Material 3.
- `MainActivity.kt` i `HomeScreen.kt`: ekran startowy z kaflami usług.
- `DocumentsScreen.kt`: wybór PDF/obrazu przez Storage Access Framework, lista lokalnych plików, podgląd stron PDF i obrazów.
- `ImportedDocumentStore.kt`: lokalny rejestr URI dostawców plików.
- `ImagePreviewSizing.kt` i testy jednostkowe: ograniczenie rozmiaru dekodowanych podglądów obrazów.

**Mocne strony:**
- UI jest wyraźnie oznaczone jako prototyp i nie deklaruje dostępu do prawdziwych usług państwowych.
- Manifest ma `allowBackup=false`, a aplikacja nie deklaruje uprawnień sieciowych.
- Import odbywa się przez SAF, bez żądania szerokiego dostępu do pamięci urządzenia.
- Cięższe operacje odczytu i dekodowania plików są wykonywane poza głównym wątkiem.
- Dodano ograniczenie wymiaru podglądu obrazu i testy jednostkowe tego mechanizmu.
- CI buduje APK debug i wykonuje testy jednostkowe.

**Wykryte problemy / ryzyka:**

| ID | Priorytet | Ustalenie | Zalecane działanie |
|---|---|---|---|
| AND-01 | P1 | Brak testów interfejsu Compose dla przepływów start → dokumenty → import → podgląd → usunięcie. | Dodać testy UI i testy semantyki. |
| AND-02 | P1 | Kafle i dolny pasek prezentują kilka pozycji jako nawigację, ale tylko „Dokumenty” ma zdefiniowaną akcję w bieżącym HomeScreen. Pozostałe kafle są demonstracyjne. | Oznaczyć je jako nieaktywne lub wdrożyć jednoznaczne ekrany; nie pozostawiać pozornych przycisków. |
| AND-03 | P1 | Gesty podglądu zwiększają przesunięcie obrazu bez ograniczenia do granic widoku; nie widać resetowania skali i przesunięcia przy zmianie strony PDF. | Ograniczyć przesuwanie, dodać reset i testy gestów. |
| AND-04 | P1 | Dostępność nie została potwierdzona testami. Ikony i elementy nawigacji wymagają weryfikacji etykiet, semantyki, kolejności fokusu i rozmiarów celów dotykowych. | Dodać testy Compose semantics oraz ręczne testy TalkBack/font scaling. |
| AND-05 | P1 | Import sprawdza typ MIME, ale walidacja nie zastępuje kontroli rzeczywistego formatu i odpornego odczytu uszkodzonych/niezaufanych plików. | Dodać limity wejścia, testy plików uszkodzonych i formatów niezgodnych z deklaracją MIME. |
| AND-06 | P1 | `ImportedDocumentStore.remove` wywołuje `apply()`, a następnie zwalnia uprawnienie URI; błąd zapisu może pozostawić niespójność rejestru i uprawnień. | Użyć zapisu potwierdzanego i obsługi błędu; dopiero potem zwalniać uprawnienie. |
| AND-07 | P2 | Brakuje testów obrotu ekranu, odtwarzania procesu i ponownego uruchomienia z URI dostawcy, który może przestać być dostępny. | Dodać testy cyklu życia i odzyskiwania niedostępnych URI. |
| AND-08 | P2 | Nie wykazano testów małych ekranów, tabletów, orientacji poziomej, dużej czcionki i trybu ciemnego. Motyw prototypu jest wyłącznie jasny. | Dodać adaptacyjne układy, tryb ciemny i macierz rozmiarów. |
| AND-09 | P2 | Współrzędne i kolory ekranu głównego są wstępne; komponenty publicznego zestawu design systemu nie są jeszcze systematycznie wykorzystane w prototypie. | Zmapować tokeny i komponenty źródłowe oraz zweryfikować licencje i atrybucje. |

### 2.5 Dodatkowa obserwacja: podział między Android i iOS

Repozytorium ma obszerną bibliotekę komponentów Android i zasoby/źródła iOS, ale nie wykazano testów porównawczych, które zapewniałyby spójność zachowania i tokenów pomiędzy platformami. Należy rozdzielić:
- zgodność wizualną (kolory, typografia, odstępy, ikony),
- zgodność zachowania (nawigacja, walidacja, stany błędów),
- dostępność (TalkBack/VoiceOver, fokus, powiększenie tekstu),
- różnice wynikające z konwencji platform.

## 3. CI i stan weryfikacji

Dla HEAD `d40bcf1f06fe965b7a9500ecfd572763d29d4f8e` workflow `Project validation` zakończył się powodzeniem:
- testy regresyjne Python — success,
- kompilacja źródeł Python — success,
- testy jednostkowe Android — success,
- budowa debug APK — success,
- archiwizacja APK — success.

Link: https://github.com/bartoszruta26-droid/mObywatel-2.0/actions/runs/42358627507

To potwierdza poprawne wykonanie zadań w CI dla tego commitu, ale nie jest równoznaczne z testem na fizycznym urządzeniu, oceną dostępności ani audytem bezpieczeństwa aplikacji.

## 4. Rekomendowana kolejność prac

### P0 — wymagane przed rozszerzeniem funkcjonalności
1. Utrzymać jednoznaczne oznaczenie prototypu i blokować sformułowania sugerujące oficjalną autentyczność dokumentów lub usług.
2. Przejrzeć import i podgląd dokumentów pod kątem plików uszkodzonych, MIME spoofing, nadmiernego zużycia pamięci i wyjątków.
3. Zweryfikować cykl życia URI SAF, trwałe uprawnienia i skutki usuwania wpisów.

### P1 — najbliższy etap
1. Testy UI Compose dla wszystkich dostępnych przepływów.
2. Dostępność: TalkBack, etykiety ikon, semantyka, fokus, duża czcionka i kontrast.
3. Dokończenie nawigacji albo jawne wyłączenie niedziałających kafli.
4. Ograniczenie gestów podglądu i testy zmian strony.
5. Testy importu/odczytu plików niepoprawnych i bardzo dużych.

### P2 — rozwój wieloplatformowy
1. Podjąć decyzję, czy TUI/desktop/web to rzeczywiste wymagania produktu.
2. Zdefiniować wspólny kontrakt nawigacji, stanów UI, błędów i lokalizacji.
3. Zaprojektować osobne wzorce dostępności i ergonomii dla każdego kanału.
4. Utrzymywać zestaw testów wizualnych i funkcjonalnych per platforma.

## 5. Ograniczenia audytu

- Audyt ma charakter statyczny i opiera się na drzewie Git, plikach źródłowych i wynikach workflow.
- Nie uruchamiano ręcznie emulatora Androida, czytników ekranu ani testów przeglądarkowych.
- Brak katalogu klienta nie dowodzi, że żadna zewnętrzna implementacja nie istnieje; oznacza, że nie jest ona częścią audytowanego repozytorium/HEAD.
- Nie oceniano pełnej poprawności prawnej, bezpieczeństwa kryptograficznego ani zgodności z oficjalnym systemem mObywatel.

## 6. Konkluzja

Obecny projekt należy traktować jako repozytorium zasobów interfejsowych Android/iOS oraz rozwijany prototyp Androida. Android ma już podstawowe ekrany i lokalną obsługę wybranych plików, ale wymaga domknięcia testów UI, dostępności, nawigacji i obsługi cyklu życia URI. TUI, desktop i Web UI pozostają niezaimplementowane jako osobne klienty. Ich budowa powinna wynikać z decyzji architektonicznej, a nie z mechanicznego kopiowania widoków Androida.


## 7. Kontrola aktualnych wyników CI na gałęzi `main`

Dodatkowa kontrola historii workflow ujawniła, że na `main` commit `fb905751e9894af9deda7d18155f89c76bf4869a` miał nieudany workflow `Project validation`. Zadanie Python zakończyło się powodzeniem, natomiast zadanie Android zakończyło się błędem w `PdfPreviewSizingTest`: testy `boundsPortraitPageByLongestSide` i `boundsLandscapePageByLongestSide` nie przeszły. W konsekwencji budowa APK i archiwizacja zostały pominięte. Log: https://github.com/bartoszruta26-droid/mObywatel-2.0/actions/runs/42362113406

To ustalenie jest ważne: nie należy utożsamiać zielonego CI na gałęzi audytowej z zielonym CI na `main`. Na audytowanym commicie `d40bcf1f06fe965b7a9500ecfd572763d29d4f8e` pełny workflow Android i Python zakończył się sukcesem. Różne SHA oznaczają różne zestawy testów i kodu.

**Nowe zalecenie P1:** naprawić regresję `PdfPreviewSizingTest` na gałęzi, na której występuje, i ponownie uruchomić pełny test jednostkowy oraz debug APK; nie scalać pracy bez potwierdzonego zielonego CI.
