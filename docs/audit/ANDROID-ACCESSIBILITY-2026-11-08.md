<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: CC0-1.0
-->

# Audyt dostępności Android GUI — TalkBack, skalowanie, cele dotykowe i kontrast

**Data audytu:** 2026-11-08  
**Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`  
**Gałąź:** `main`  
**Audytowany commit:** `afd3a7e8409b589c735ba2d32dbec92d2e01f05f`  
**Zakres:** wyłącznie `obywatel-android/` — Jetpack Compose. TUI, Desktop GUI i Web UI nie były audytowane.

## 1. Podsumowanie

Kod ma już podstawowe mechanizmy dostępności Compose: semantyczne nagłówki i nazwy paneli, opisy przycisków ikonowych, stany dostępności usług, komunikaty Live Region oraz natywne komponenty Material 3. W kodzie nie znaleziono jawnie zadanych rozmiarów tekstu w `dp` ani małych, własnoręcznie zbudowanych przycisków.

Nie można jednak uznać dostępności za zweryfikowaną na urządzeniu: brak testów TalkBack, testów dużej czcionki i pomiaru faktycznych obszarów dotykowych. Najważniejszy problem funkcjonalny dotyczy podglądu PDF i obrazów — TalkBack otrzymuje jedynie ogólny opis bitmapy, a nie treść dokumentu.

## 2. Ustalenia według obszaru

### 2.1. TalkBack i semantyka

**Działające elementy**
- Ekran główny ma `paneTitle = "Ekran główny"`; ekran dokumentów zmienia nazwę panelu zależnie od widoku.
- Nagłówki ekranu głównego, sekcji „Usługi” i tytułu dokumentu są oznaczone semantyką `heading()`.
- Kafle usług scalają potomne elementy semantyczne; przekazują `stateDescription` „Dostępna” lub „W przygotowaniu — funkcja niedostępna”. Niedostępne usługi są rzeczywiście wyłączone.
- Przyciski powrotu, usuwania, powiększania i pomniejszania mają opisy dostępności.
- Błędy listy i podglądu oraz licznik stron korzystają z `LiveRegionMode.Polite`.
- Ilustracje dekoracyjne mają `contentDescription = null`, co zapobiega odczytywaniu ozdobników.

**Wady i ryzyka**
- **Wysoki priorytet — treść PDF/obrazu jest niedostępna dla czytnika ekranu.** Podgląd renderuje dokument jako bitmapę i przekazuje opis w rodzaju „Podgląd dokumentu …, strona …”. Nie udostępnia tekstu strony, struktury nagłówków ani elementów dokumentu. Użytkownik TalkBack nie może odczytać rzeczywistej treści PDF; dla obrazu opis również nie opisuje jego zawartości.
- Gest przesuwania powiększonej bitmapy jest gestem bez odpowiednika sterowanego przyciskami. Przyciski zoomu istnieją, ale nawigacja po przesuniętym obrazie nie ma alternatywy dla osób, które nie mogą wykonać gestu.
- Wskaźniki ładowania nie mają jawnej etykiety tekstowej. Semantyka postępu może być dostępna, ale komunikat „Wczytywanie dokumentów” / „Wczytywanie podglądu” byłby bardziej jednoznaczny.
- Obecne testy sprawdzają wybrane etykiety, stany usług i nawigację, ale nie weryfikują odczytu drzewa semantycznego, kolejności fokusu, nazw paneli ani ogłoszeń dynamicznych.

**Zalecenia**
1. Dla PDF udostępnić warstwę tekstową i semantyczną strony, jeśli dokument zawiera tekst; dla skanów rozważyć OCR lub otwieranie w dostępnym czytniku systemowym. Nie przedstawiać bitmapy jako równoważnej dostępnej treści.
2. Dla obrazów dokumentów zapewnić OCR lub opis tekstowy, jeśli ich treść ma znaczenie informacyjne.
3. Dodać etykiety postępu oraz testy semantyki paneli, nagłówków, przycisków i ogłoszeń.
4. Zapewnić alternatywną nawigację po powiększonej stronie, jeśli podgląd pozostanie wewnątrz aplikacji.

### 2.2. Skalowanie czcionek i reflow

**Działające elementy**
- Teksty korzystają ze stylów `MaterialTheme.typography`, a więc z jednostek skalowalnych typografii Compose.
- Nie znaleziono stałych wysokości ani szerokości przypisanych bezpośrednio do komponentów tekstowych.
- Karty usług mają `heightIn(min = 142.dp)`, a nie sztywną wysokość; tekst może zwiększyć wysokość karty.
- Lista dokumentów i siatka usług mają własne przewijanie; podgląd DOCX ma `verticalScroll`.

**Ryzyka**
- **Średni/wysoki priorytet — brak testów przy dużym fontScale.** Nie ma testów przy 1,3×, 1,5× ani 2,0×, w orientacji pionowej i poziomej.
- Ekran główny ma stałe sekcje nad siatką przewijaną. Ekran dokumentów ma nieprzewijaną zewnętrzną kolumnę z nagłówkiem, komunikatem, przyciskiem i ewentualnym błędem nad obszarem ważonym. Przy dużej czcionce i małej wysokości ekranu elementy nad listą mogą zająć większość dostępnej przestrzeni.
- Wiersz paginacji PDF jest poziomy i zawiera dwa przyciski oraz licznik. Przy dużym fontScale i wąskim ekranie może zabraknąć szerokości.
- Nagłówek ekranu głównego łączy tekst z ilustracją o rozmiarze 104 dp. Wysokość wiersza rośnie wraz z tekstem, ale przy dużym powiększeniu należy sprawdzić, czy układ nie staje się nieproporcjonalny.

**Zalecenia**
1. Dodać testy UI z `LocalDensity` i fontScale co najmniej 1,0 / 1,3 / 1,5 / 2,0; sprawdzić brak obcięcia tekstu i możliwość dotarcia do wszystkich działań.
2. Przetestować najmniejszy wspierany ekran, orientację poziomą i powiększenie ekranu.
3. Rozważyć przewijanie całej zawartości ekranów głównych, gdy wysokość dostępna dla sekcji przewijanej spada poniżej praktycznego minimum.
4. W paginacji PDF przejść na układ zawijany lub pionowy przy ograniczonej szerokości.

### 2.3. Obszary dotykowe

**Ocena źródłowa**
- Kafle usług mają minimalną wysokość 142 dp i są klikalne na całej powierzchni.
- Karty dokumentów mają klikalny wiersz; sama ikona ma 28 dp, ale wiersz ma pionowe wypełnienie 14 dp po obu stronach, zatem jego wysokość przekracza 48 dp.
- Powrót, usuwanie i zoom korzystają z Material 3 `IconButton`.
- Import i paginacja korzystają z Material 3 `Button` / `OutlinedButton`.
- Nie znaleziono niestandardowych kontrolek dotykowych o jawnie zadanym rozmiarze mniejszym niż 48 dp.

**Wniosek**
Nie stwierdzono oczywistego naruszenia minimalnego rozmiaru 48 × 48 dp w kodzie. To ocena statyczna, nie pomiar rzeczywistych granic hitboxów na urządzeniu. Szczególnie trzeba sprawdzić zagnieżdżenie przycisku usuwania wewnątrz klikalnej karty dokumentu oraz ewentualne nakładanie się obszarów dotykowych.

**Zalecenia**
- Uruchomić Accessibility Scanner na urządzeniu i sprawdzić każdy stan ekranu.
- Dodać testy granic semantycznych / rozmiarów dla przycisków i kart, jeżeli środowisko testowe pozwala wiarygodnie odczytać ich wymiary.
- Zachować odstępy między sąsiednimi celami, zwłaszcza przy ikonach.

### 2.4. Kontrast rzeczywistych par kolorów

Obliczenia wykonano dla kolorów z `Color.kt` i `Theme.kt` oraz par używanych przez komponenty. Są to obliczenia tokenów kolorystycznych, a nie pomiar zrzutu ekranu ani pełny certyfikat zgodności.

| Komponent / para | Kontrast | Ocena dla zwykłego tekstu |
|---|---:|---|
| Jasny: tekst główny `#172B4D` na powierzchni `#F5F7FA` | 13,14:1 | spełnia 4,5:1 |
| Jasny: tekst wariantowy `#5B6B7A` na bieli | 5,48:1 | spełnia 4,5:1 |
| Jasny: tekst wariantowy `#5B6B7A` na `#F5F7FA` | 5,11:1 | spełnia 4,5:1 |
| Jasny: biały tekst `#FFFFFF` na niebieskim `#1769AA` | 5,77:1 | spełnia 4,5:1 |
| Jasny: `#102A43` na `#EAF3FA` | 13,04:1 | spełnia 4,5:1 |
| Ciemny: tekst `#E5EDF4` na tle `#101820` | 15,13:1 | spełnia 4,5:1 |
| Ciemny: tekst `#E5EDF4` na powierzchni `#17232D` | 13,50:1 | spełnia 4,5:1 |
| Ciemny: tekst wariantowy `#B8C7D3` na `#293945` | 6,88:1 | spełnia 4,5:1 |
| Ciemny: `#D0E8FF` na `#174B70` | 7,32:1 | spełnia 4,5:1 |
| Ciemny: `#003354` na `#8FC7FF` | 7,36:1 | spełnia 4,5:1 |

**Wniosek:** wszystkie zbadane pary tekst–tło w aktywnych komponentach przekraczają próg 4,5:1. Nie ma obecnie podstaw do zmiany tych kolorów.

**Zastrzeżenia**
- Kafle usług wyłączonych korzystają z domyślnego stylu wyłączonego Material 3. Wyłączone elementy są wyłączone z kryterium minimalnego kontrastu WCAG, ale ich stan musi pozostać czytelny i zrozumiały.
- Tokeny `Border` i `outline` nie są w badanych ekranach bezpośrednio przypisane do widocznych obrysów kontrolek. Nie należy traktować wcześniejszych obliczeń dla tych tokenów jako dowodu kontrastu rzeczywistych komponentów.
- Nie oceniano kolorów systemowego paska stanu, nałożonych warstw, stanów fokusu, ripple ani wszystkich stanów komponentów Material 3. Wymagają kontroli zrzutów ekranu w jasnym i ciemnym motywie.

## 3. Braki w pokryciu testami

Aktualne testy Compose pokrywają podstawową nawigację, stan usług, pusty stan dokumentów i etykietę powrotu. Brakuje testów:
- fontScale 1,3 / 1,5 / 2,0 i orientacji poziomej;
- semantyki `paneTitle`, `heading`, `contentDescription` i `liveRegion` dla wszystkich stanów;
- dostępności treści PDF i obrazów;
- wymiarów / hitboxów celów dotykowych;
- jasnego i ciemnego motywu na rzeczywistych komponentach;
- stanów błędu, ładowania i długich nazw dokumentów przy dużej czcionce.

## 4. Plan naprawczy — kolejność

1. **P1:** rozwiązać dostępność treści PDF i obrazów (warstwa tekstowa / OCR / zewnętrzny czytnik z dostępnością).
2. **P1:** wykonać ręczny test TalkBack dla ekranu głównego, listy, importu, usuwania, podglądu i nawigacji wstecz.
3. **P1:** wykonać testy fontScale 2,0 na małym ekranie, w obu orientacjach; naprawić każde obcięcie i niedostępny element.
4. **P2:** dodać etykiety wskaźników postępu oraz testy semantyki i komunikatów dynamicznych.
5. **P2:** przeprowadzić Accessibility Scanner i pomiar hitboxów, ze szczególnym uwzględnieniem zagnieżdżonego przycisku usuwania.
6. **P2:** wykonać kontrolę kontrastu stanów aktywnych, fokusu i motywu ciemnego na zrzutach ekranu.

## 5. Ograniczenia audytu

Audyt wykonano statycznie na kodzie gałęzi `main` i obliczeniach kontrastu. Nie uruchomiono TalkBack ani Accessibility Scanner na fizycznym urządzeniu; nie wykonano pomiarów ekranowych hitboxów ani testu wzrokowego wszystkich stanów. Nie jest to deklaracja pełnej zgodności z WCAG, EN 301 549 ani Android Accessibility Guidelines.

## 6. Źródła kryteriów

- Android Developers, *Make apps more accessible* — zalecany cel dotykowy co najmniej 48 × 48 dp: https://developer.android.com/guide/topics/ui/accessibility/apps
- Android Developers, *Support user-scalable content* — strategie skalowania i reflow w Jetpack Compose: https://developer.android.com/develop/ui/compose/accessibility/scalable-content
- W3C, *Web Content Accessibility Guidelines (WCAG) 2.2* — kryteria 1.4.3 (kontrast tekstu), 1.4.4 (zmiana rozmiaru tekstu) i 1.4.11 (kontrast nietekstowy): https://www.w3.org/TR/WCAG22/
