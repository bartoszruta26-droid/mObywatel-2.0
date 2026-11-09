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

### Orzeczenie / zaświadczenie dotyczące niepełnosprawności
Formularz ewidencyjny obejmuje dane dokumentu analogiczne do zakresu informacji występujących w orzeczeniu powiatowego zespołu: dane osoby, organu lub wystawcy, numer i datę, kategorię/stopień, symbole przyczyn, okres obowiązywania, datę ustalenia, wskazania dotyczące zatrudnienia, szkolenia, rehabilitacji, sprzętu pomocniczego, opieki, wsparcia środowiskowego i karty parkingowej. Źródła obejmują zespół powiatowy/miejski, inny organ wskazany przez użytkownika, zespół medyczny fundacji, opinię/zaświadczenie medyczne i oświadczenie własne. Formularz pozwala zapisać podstawę prawną lub regulacyjną oraz dane kwalifikacji osoby oceniającej; pola metodologiczne obejmują ICF i WHODAS 2.0. Interfejs nie rozstrzyga samodzielnie skutków prawnych dokumentu — zapisuje dane podane przez użytkownika i nie weryfikuje autentyczności ani kompetencji wystawcy.


### Ustawienia danych i zakładka „Zaświadczenia”
Dodano osobną pozycję „Ustawienia”, w której użytkownik może wprowadzać i zapisywać dane dotyczące niepełnosprawności, w tym źródło dokumentu, wystawcę, podstawę prawną/regulacyjną, dane osoby, numer i datę, stopień/kategorię, symbole przyczyn, okres, wskazania i informacje o ocenie funkcjonalnej (ICF/WHODAS 2.0). Dodano osobną pozycję „Zaświadczenia”, która odczytuje zapisane dane i prezentuje je w formie karty informacyjnej. Dane pozostają w istniejącym lokalnym, szyfrowanym magazynie. Ekran jest ewidencyjny: pokazuje informacje wprowadzone przez użytkownika, nie potwierdza autentyczności dokumentu ani nie nadaje mu skutków prawnych. Moduł nie obejmuje wewnętrznego zarządzania postępowaniami fundacji.

## Rozszerzenie ewidencji dokumentów i sekcji informacji dodatkowych

W formularzu dokumentów rozszerzono zakres danych:
- profil osoby: nazwisko rodowe i PESEL jako osobne pola;
- dowód osobisty: status dokumentu, numer poprzedniego dokumentu oraz data utraty lub unieważnienia;
- paszport: rodzaj dokumentu podróży, dane posiadacza zapisane w dokumencie, miejsce urodzenia, obywatelstwo, płeć, status i data utraty/unieważnienia;
- ewidencja poselska: data wydania legitymacji, wystawca, status legitymacji i źródło potwierdzenia mandatu;
- legitymacja kursanta: rzeczywista data ukończenia, liczba godzin, forma kształcenia, zaliczenie/egzamin i numer zaświadczenia o ukończeniu;
- legitymacja studencka: dodano odrębny formularz ewidencyjny uczelni, jednostki, numeru legitymacji/albumu, kierunku, poziomu, formy, roku akademickiego, ważności, statusu i wystawcy;
- orzeczenie / zaświadczenie o niepełnosprawności: data wniosku, numer sprawy, data doręczenia, rodzaj rozstrzygnięcia, wynik odwołania, duplikat/zmiana dokumentu i opis dokumentu źródłowego.

Dodano wspólny, rozwijany komponent „Informacje dodatkowe” na końcu sekcji ewidencyjnych. Zawiera pola źródła danych, podstawy prawnej/regulaminowej, daty ostatniej weryfikacji, podmiotu potwierdzającego i adnotacji. Wyjaśnienia prawne oraz ograniczenia dokumentów zostały przeniesione do tych rozwijanych sekcji dla dokumentów dowodu, paszportu, ewidencji poselskiej, legitymacji kursanta i orzeczenia/zaświadczenia. Dodano również sekcję informacji dodatkowych do danych profilu i osobny moduł legitymacji studenckiej.

Ważne ograniczenia:
- pola są lokalną ewidencją i nie stanowią weryfikacji danych ani autentyczności dokumentu;
- formularz legitymacji studenckiej nie nadaje statusu studenta i nie zastępuje dokumentu uczelni;
- formularz ewidencji poselskiej nie potwierdza mandatu ani prawa do reprezentowania Sejmu;
- nie dodano urzędowych kodów, MRZ, zabezpieczeń ani mechanizmów potwierdzania autentyczności;
- dotychczasowy lokalny zapis szyfrowany pozostaje wspólnym magazynem danych.

Zmiany są częścią funkcjonalnej wersji alfa. Nie uruchamiano kompilacji, testów budowania ani generowania APK.


## Nawigacja i ekrany usług — kontynuacja wersji alfa

Główna nawigacja dolna jest funkcjonalna:
- **Start** wyświetla ekran główny z nagłówkiem, komunikatem o charakterze prototypu i czterema kafelkami szybkiego dostępu;
- **Usługi** otwiera pełny katalog usług, w tym dodatkowe moduły PESEL i Mandaty;
- **Profil** otwiera lokalny profil użytkownika z przejściami do danych i dokumentów, zaświadczeń oraz ustawień danych.

Kafelki usług prowadzą do odpowiednich modułów. Dokumenty, Ustawienia i Zaświadczenia otwierają istniejące ekrany ewidencyjne. Pozostałe pozycje otwierają dedykowane ekrany informacyjne zamiast pozostawać martwymi przyciskami. Ekrany informacyjne jawnie wskazują funkcje, które wymagają przyszłej integracji i obecnie nie są wykonywane (np. płatności, walidacja biletów, pobieranie danych z rejestrów).

Nawigacja dolna pozostaje widoczna także podczas przeglądania modułów. Wybór zakładki czyści bieżący ekran szczegółowy i przełącza do wybranego obszaru.

## Tymczasowa polityka zasobów graficznych

Na etapie alfa przyjęto robocze założenie użytkownika o posiadaniu praw/licencji do grafik przeznaczonych do wykorzystania. Weryfikacja licencji zostaje odłożona do późniejszego ręcznego przeglądu. Każdy zasób powinien być przechowywany oddzielnie i mieć odnotowane źródło, aby można go było wymienić bez zmiany logiki ekranów.

Do wykorzystania przewidziano:
- godło państwowe w oficjalnej postaci;
- pieczęć Fundacji w zatwierdzonym wzorze;
- oryginalne grafiki i piktogramy interfejsu mObywatel pozyskane z oficjalnych materiałów rządowych.

Założenie robocze nie stanowi prawnej weryfikacji licencji. Godło państwowe zachowuje odrębne zasady ochrony i używania. Nie wolno go dowolnie przekształcać ani tworzyć jego wariantów. Nie dodano jeszcze plików godła ani pieczęci Fundacji, ponieważ ich zatwierdzone pliki źródłowe muszą zostać wskazane lub dostarczone; obecny znak globu nadal jest tymczasowym symbolem prototypu.

## Zakres wykonanej kontynuacji

- Naprawiono nawigację Start / Usługi / Profil — każda zakładka wyświetla odrębny ekran.
- Dodano ekran profilu z odnośnikami do ewidencji dokumentów, zaświadczeń i ustawień.
- Dodano ekrany szczegółowe dla usług, które nie mają jeszcze właściwej integracji.
- Kafelki nie pozostają bez reakcji; brakujące funkcje są jawnie opisane jako nieaktywne lub oczekujące na integrację.
- Nie uruchamiano kompilacji, testów budowania ani generowania APK.


## Katalog usług — wyszukiwanie i kategorie

Katalog „Usługi” otrzymał lokalne wyszukiwanie po nazwie i krótkim opisie oraz poziomy wybór kategorii. Kategorie obejmują dokumenty, bezpieczeństwo, zdrowie, kierowcę i pojazdy, sprawy urzędowe, podróże i pozostałe moduły. Filtrowanie odbywa się wyłącznie na danych wbudowanych w aplikację; nie wymaga połączenia z siecią ani nie sugeruje dostępności usług administracji publicznej.

Układ nawiązuje do podejścia prezentowanego w aktualnym interfejsie mObywatela: osobna zakładka usług, kategorie oraz łatwiejsze odnajdywanie modułów. Jest to implementacja własna prototypu, a nie kopia kodu ani oficjalna aplikacja. Źródło odniesienia: https://info.mobywatel.gov.pl/uslugi oraz https://www.gov.pl/web/cyfryzacja/mobywatel--10-milionow-uzytkownikow-nowa-odslona-aplikacji-i-plany-jej-rozwoju.

Wszystkie wyniki prowadzą do ekranów prototypu. Moduły bez integracji nadal nie odpytują rejestrów, nie przyjmują płatności, nie wystawiają dokumentów i nie zmieniają statusów urzędowych.


## Portfel dokumentów — osobny widok i edycja

Ekran „Dokumenty” otwiera teraz portfel z ośmioma odrębnymi pozycjami ewidencyjnymi: profilem danych osobowych, danymi dowodu, paszportu, prywatną ewidencją rodową, ewidencją legitymacji poselskiej, legitymacją kursanta, ewidencją legitymacji studenckiej oraz orzeczeniami i zaświadczeniami. Każda pozycja pokazuje, czy odpowiednie dane są zapisane lokalnie. Portfel udostępnia wspólne przejście do formularza edycji.

Rozdzielono nawigacyjnie portfel i formularz edycji: przycisk powrotu z edycji prowadzi do portfela, a powrót z portfela prowadzi do poprzedniego ekranu. Licznik prezentuje liczbę obszarów z zapisanymi danymi, bez ujawniania danych osobowych na ekranie podsumowania.

Portfel nie jest magazynem cyfrowych dokumentów państwowych. Wpisy są prywatną ewidencją użytkownika; statusy, numery i dane wystawców nie są weryfikowane w rejestrach. Karty i podglądy muszą zachować oznaczenia demonstracyjne i nie mogą odtwarzać zabezpieczeń, kodów ani oficjalnych wzorów dokumentów.


## Portfel dokumentów — ekran szczegółów pozycji

Każda z ośmiu pozycji portfela otwiera osobny ekran szczegółów. Widok odczytuje lokalne dane zapisane w zaszyfrowanym magazynie i prezentuje pola właściwe dla wybranej kategorii: profil, dowód, paszport, ewidencję rodową, ewidencję poselską, legitymację kursanta, ewidencję studencką albo orzeczenia i zaświadczenia. Puste pola są jawnie oznaczane jako „Brak wpisu”.

Z ekranu szczegółów można wrócić do portfela albo przejść do formularza edycji. Edycja nadal wykorzystuje wspólny formularz danych — nie wprowadzono jeszcze automatycznego przewijania do konkretnej sekcji ani osobnych formularzy dla każdej kategorii. Widok szczegółów nie ujawnia danych na ekranie zbiorczym portfela; są one prezentowane dopiero po świadomym otwarciu danej pozycji.

Każdy ekran szczegółów zawiera oznaczenie „EWIDENCJA LOKALNA • DEMO” oraz informację, że dane nie są weryfikowane w rejestrach publicznych i nie potwierdzają autentyczności dokumentu ani uprawnień. Nie dodano urzędowych wzorów, kodów ani zabezpieczeń.
