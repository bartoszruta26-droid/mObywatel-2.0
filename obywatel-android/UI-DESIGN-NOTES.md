<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Notatki projektowe UI

## Zakres zmian

- Ekran startowy niezależnego prototypu Android w Jetpack Compose.
- Oryginalne piktogramy usług przeniesione z katalogu źródłowego iOS w tym repozytorium do Android VectorDrawable.
- Ustawienia i pozycja „Pozostałe” używają ikon ogólnego przeznaczenia; nie przypisują im ikon innych usług.
- Paleta kolorów pozostaje oznaczona jako demonstracyjna do czasu pełnego odwzorowania i sprawdzenia wartości z zasobów systemu designu.

## Pochodzenie zasobów

Piktogramy usług pochodzą z:
`iOS/Sources/Assets.xcassets/Icons/Brand/Services/`.
Wykorzystane zasoby obejmują DA001, DA002, DA003, DA004, DA006, DA008, DA009, DA013, DA025, DA035 oraz DA043 (sprawdzenie dowodu). Źródłowe pliki są oznaczone w nadrzędnym `REUSE.toml` jako © 2025 Centralny Ośrodek Informatyki oraz `LicenseRef-COI-MIT-PL`. Tekst licencji znajduje się w `LICENSES/LicenseRef-COI-MIT-PL.txt`; konwersje Android VectorDrawable zachowują oznaczenie prawnoautorskie i identyfikator licencji.

## Zweryfikowane tokeny źródłowego systemu designu

W `iOS/Sources/Tokens/Spacings.swift` zdefiniowano skalę odstępów: 2, 4, 8, 12, 16, 20, 24, 32, 40, 48 i 56 punktów. W `iOS/Sources/Tokens/Radiuses.swift` zdefiniowano promienie: 4, 12, 16, 24 i 40 punktów. Nazwy tokenów kolorystycznych znajdują się w `iOS/Sources/Tokens/Colors.swift`; wartości barw należy pobrać z odpowiadających im zasobów kolorów, a nie wyprowadzać z nazw.

## Granice identyfikacji i bezpieczeństwa

- Prototyp nie jest oficjalną aplikacją mObywatel.
- Nie dodano dokumentów poświadczających, danych użytkowników, logowania ani mechanizmów bezpieczeństwa.
- Piktogram sprawdzenia dowodu jest wyłącznie ikoną kafla usług, nie elementem dokumentu ani potwierdzeniem tożsamości.
- Oficjalne znaki i godło należy wykorzystywać wyłącznie w granicach ich właściwych zasad prawnych i licencyjnych.

## Formularz danych demonstracyjnych

Ekran „Dokumenty” zawiera wyłącznie formularz ewidencyjny z pustymi polami na dane osobowe, dane dowodu osobistego, dane paszportowe i podstawowe dane dotyczące legitymacji poselskiej oraz neutralny obszar na zdjęcie profilowe. Wszystkie sekcje są jednoznacznie oznaczone jako DEMO — NIE JEST DOKUMENTEM. Pola nie są zapisywane ani wysyłane. Ekran nie odtwarza oficjalnych wzorów dokumentów, legitymacji, zabezpieczeń, kodów QR, kodów kreskowych ani strefy MRZ i nie potwierdza tożsamości ani statusu publicznego.

### Konfiguracja, trwały zapis i zdjęcie

Ekran demonstracyjny umożliwia wybranie fotografii przez systemowy selektor Androida (limit 5 MB), edycję pól osobowych/dowodu/paszportu/ewidencji poselskiej oraz jawne zapisanie całości. Zapis jest lokalny w `noBackupFilesDir`, szyfrowany AES-256-GCM z kluczem zarządzanym przez Android Keystore; aplikacja nie deklaruje uprawnień do Internetu ani nie wysyła danych. `allowBackup=false` pozostaje w manifeście. Dane są wrażliwe: to prototyp, nie audytowany sejf na dokumenty. Utrata/wyczyszczenie danych aplikacji lub odinstalowanie usuwa lokalny profil. Nie jest to cyfrowy dokument, dowód statusu publicznego ani kanał weryfikacji.

### Informacje konstytucyjne w sekcji ewidencji poselskiej

Sekcja wyświetla treść art. 104 ust. 1 Konstytucji RP (posłowie jako przedstawiciele Narodu; wolny mandat) oraz art. 104 ust. 2–3 (ślubowanie przed Sejmem przed rozpoczęciem sprawowania mandatu; odmowa oznacza zrzeczenie się mandatu). Dodatkowe pola pozwalają opisać datę, miejsce/okoliczności i źródło potwierdzenia ślubowania. Interfejs wyraźnie zaznacza, że Konstytucja mówi „przed Sejmem”, a nie „przed budynkiem Sejmu”; wpis o wypowiedzeniu roty przed budynkiem jest wyłącznie oświadczeniem opisującym okoliczność, nie dowodem mandatu. Samo szerokie, potoczne lub słownikowe rozumienie słowa „poseł” jako reprezentanta interesów nie jest równoznaczne z uzyskaniem konstytucyjnego mandatu posła na Sejm.


### Rodowy dokument tożsamości — karta i ewidencja

Dodano odrębną sekcję rodowego dokumentu tożsamości z polami: numer dokumentu rodowego, nazwa rodu, linia lub gałąź, miejsce w strukturze rodu, prowadzący ewidencję, data sporządzenia oraz podstawa wpisu. Rozszerzono dane osobowe o płeć deklarowaną, imiona i nazwiska rodziców. Dodano podgląd karty w proporcji zbliżonej do karty identyfikacyjnej, z fotografią zastępczą, imieniem i nazwiskiem, datą urodzenia oraz numerem rodowym. Projekt ma odrębną nazwę, znak i kolorystykę; pasek informacyjny na karcie wskazuje, że jest to dokument prywatny, a nie dokument państwowy. Dane są objęte istniejącym lokalnym szyfrowanym zapisem.


### Rewers i rozszerzona ewidencja rodowa

Podgląd dokumentu rodowego obejmuje teraz awers i rewers. Rewers pokazuje wpisane dane rodziców, nazwę rodu, linię/gałąź, osobę prowadzącą ewidencję i miejsce jej prowadzenia. Formularz rozszerzono o datę przeglądu wpisu, miejsce ewidencji, dobrowolne miejsce zamieszkania, linie rodowe rodziców, uwagi ewidencyjne oraz pola imienia i nazwiska posiadacza i wystawcy (do podpisu poza aplikacją). Pola korzystają z istniejącego szyfrowanego zapisu lokalnego. Podgląd jest wizualizacją prywatnej karty ewidencyjnej, a nie wzorem państwowego dokumentu; nie generuje kodów ani poświadczeń urzędowych.


### Legitymacja uczestnika kursu instytucji szkoleniowej

Dodano osobny formularz i podgląd legitymacji uczestnika kursu dla prywatnej instytucji szkoleniowej. Pola obejmują nazwę i adres instytucji, ewentualny numer wpisu do rejestru, numer legitymacji, nazwę i symbol kursu, daty rozpoczęcia i planowanego zakończenia, datę wydania, termin ważności, status uczestnictwa oraz dane osoby wystawiającej. Podgląd korzysta z fotografii wybranej w profilu i danych osobowych uczestnika. Dokument ma własną kolorystykę i oznaczenie „nie jest legitymacją studencką”; potwierdza wyłącznie wpis w ewidencji wystawcy i nie przypisuje uprawnień ustawowych legitymacji studenckiej. Dane wykorzystują istniejący szyfrowany zapis lokalny.

### Orzeczenie o niepełnosprawności — ewidencja i dwa źródła
Dodano osobny moduł ewidencyjny z przełącznikiem źródła: powiatowy/miejski zespół do spraw orzekania albo zespół powołany przez fundację. Formularz zawiera dane organu lub fundacji, podstawę prawną/statutową, uchwałę o powołaniu zespołu, regulamin, numer i datę orzeczenia, dane osoby, kategorię/stopień, symbole przyczyn, okres obowiązywania, datę ustalenia, wskazania wsparcia, dane składu i osoby podpisującej oraz tryb odwołania lub wewnętrznej weryfikacji. Dodano podgląd o odmiennym oznaczeniu dla dokumentu fundacyjnego. Interfejs wyjaśnia, że dokument fundacyjny jest rozstrzygnięciem wewnętrznym na potrzeby fundacji i nie jest orzeczeniem ustawowym ani źródłem ulg, świadczeń lub uprawnień publicznoprawnych. Dla zespołu powiatowego ekran pozostaje wyłącznie lokalną ewidencją i nie zastępuje oryginału. Dane korzystają z dotychczasowego szyfrowanego zapisu lokalnego.
