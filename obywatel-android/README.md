# Aplikacja Obywatelska — Android

**Aplikacja Obywatelska** to niezależny projekt obywatelski na system Android. Jej celem jest zapewnienie użytkownikowi jednego, uporządkowanego miejsca do prowadzenia własnych danych, dokumentów, ewidencji rodzinnych i organizacyjnych, informacji o kwalifikacjach oraz narzędzi do przygotowywania i prezentowania dokumentów.

Projekt rozwija ideę cyfrowego portfela obywatelskiego: użytkownik zarządza swoimi danymi i materiałami, a interfejs porządkuje je według obszarów życia i spraw publicznych. Aplikacja **nie jest oficjalną aplikacją mObywatel, nie jest wydawana przez administrację państwową i nie stanowi rządowego systemu teleinformatycznego**. Podobieństwo części kategorii usług lub ikon do rozwiązań publicznych nie oznacza powiązania instytucjonalnego.

> **Status projektu:** rozwijana alfa / prototyp funkcjonalny. Dostępność ekranu lub formularza nie oznacza, że dana usługa jest połączona z rejestrem państwowym, że dane zostały urzędowo potwierdzone ani że wygenerowany dokument ma moc dokumentu urzędowego.

## Główne założenia

- **Obywatelski charakter** — narzędzia do organizowania informacji i dokumentów osoby, rodziny, wspólnoty oraz organizacji.
- **Portfel użytkownika** — jeden interfejs do przeglądania lokalnych dokumentów, ewidencji i podglądów.
- **Samodzielne zarządzanie danymi** — edycja danych odbywa się w przeznaczonych do tego ustawieniach i formularzach; portfel służy przede wszystkim do prezentacji zapisanych informacji.
- **Różne obszary życia** — tożsamość, rodzina, zdrowie, edukacja, praca, kwalifikacje, finanse, transport, nieruchomości, działalność społeczna i religijna.
- **Przejrzyste pochodzenie informacji** — należy odróżniać dane wpisane przez użytkownika, dane przekazane przez inną osobę, dane potwierdzone przez wystawcę i dane uzyskane z usługi publicznej.
- **Brak pozorowania urzędowej autentyczności** — prywatna ewidencja, wzór legitymacji, herb, pieczęć, kod QR ani plik PDF nie stają się przez samo umieszczenie w aplikacji dokumentem urzędowym.

## Interfejs i nawigacja

Główna nawigacja obejmuje pięć obszarów:

1. **Pulpit** — kafelki usług i skróty do najważniejszych obszarów aplikacji.
2. **Dokumenty / Portfel** — lokalne dokumenty, ewidencje i szczegóły zapisanych pozycji.
3. **Usługi** — katalog obszarów tematycznych i modułów obywatelskich.
4. **Kod QR** — narzędzia do przygotowywania kodów i danych do wymiany.
5. **Więcej** — ustawienia, certyfikaty i zaświadczenia, historia działań, dokumenty PDF, kwalifikacje oraz pozostałe moduły administracyjne.

## Funkcje i moduły

Poniższy opis rozróżnia funkcje interfejsu, lokalne ewidencje i narzędzia dokumentowe od usług wymagających zewnętrznej integracji.

### 1. Tożsamość i portfel dokumentów

- Portfel lokalnych dokumentów i ewidencji.
- Kategorie dokumentów tożsamości, danych paszportowych, legitymacji i innych dokumentów użytkownika.
- Podglądy dokumentów oraz ekran szczegółów.
- Formularze do uzupełniania i aktualizowania danych ewidencyjnych w ustawieniach.
- Osobne obszary dla dokumentów podpisanych elektronicznie, zaświadczeń i certyfikatów.
- Oznaczenia statusów i informacje o dokumencie w zakresie obsługiwanym przez prototyp.

Dane w portfelu są prezentowane jako dane lokalnej ewidencji aplikacji. Nie są automatycznie pobierane z rejestru dowodów osobistych, rejestru paszportowego ani innych rejestrów państwowych.

### 2. Ustawienia i zarządzanie danymi

W sekcji **Więcej → Ustawienia** znajdują się narzędzia do zarządzania danymi i modułami dokumentowymi, między innymi:

- edycja danych dokumentów i ewidencji;
- ustawienia informacji dotyczących niepełnosprawności;
- certyfikaty i zaświadczenia;
- dokumenty PDF i podpisy elektroniczne;
- prawo wykonywania zawodu i kwalifikacje;
- formularze dokumentów;
- dokumenty Rodowe;
- dokumenty rejestru podmiotów;
- dokumenty kościelne;
- zasoby identyfikacji dokumentów;
- import i eksport archiwum danych w formacie vCard.

Portfel służy do przeglądania zapisanych informacji. Zmiany danych należy wykonywać w odpowiednich formularzach i ustawieniach, a nie przez edycję podglądu dokumentu.

### 3. Dokumenty PDF i podpisy elektroniczne

- Generowanie dokumentów PDF na podstawie wypełnionych formularzy.
- Wstawianie przypisanych elementów identyfikacji wizualnej do obsługiwanych dokumentów.
- Otwieranie i prezentowanie dokumentów PDF oraz materiałów podpisanych elektronicznie w dedykowanym module.
- Przygotowanie dokumentu do dalszego podpisania narzędziem obsługującym odpowiedni podpis elektroniczny.

Samo wygenerowanie PDF nie oznacza złożenia podpisu elektronicznego. Aplikacja nie nadaje dokumentowi kwalifikowanego podpisu, pieczęci elektronicznej ani urzędowego poświadczenia, jeśli nie zostanie wdrożona i zweryfikowana odpowiednia integracja.

### 4. Kwalifikacje zawodowe i edukacja

- Ewidencja prawa wykonywania zawodu i kwalifikacji.
- Formularze dokumentów potwierdzających kwalifikacje oraz dane wystawcy.
- Dokumenty edukacyjne, uczniowskie i studenckie.
- Legitymacje uczestników kursów i podglądy dokumentów edukacyjnych.
- Przypisywanie znaków wystawcy lub instytucji do odpowiednich kategorii dokumentów.

Wpis użytkownika nie jest sam w sobie potwierdzeniem kwalifikacji ani prawa wykonywania zawodu. Weryfikacja wymaga właściwego wystawcy lub uprawnionego rejestru.

### 5. Dokumenty kościelne i wspólnotowe

Moduł dokumentów kościelnych obejmuje formularze ewidencyjne dotyczące między innymi:

- posługi kościelnej i liturgicznej;
- dekretów i aktów nominacyjnych;
- tytułów biskupich;
- święceń i konsekracji;
- jurysdykcji oraz zakresu kompetencji;
- ustanowienia i upoważnienia do posługi;
- legitymacji szafarzy;
- formacji liturgicznej;
- rejestrów celebracji, nabożeństw i posługi.

Dostępne są także mechanizmy doboru znaku na podstawie wpisanej nazwy godności lub rodzaju dokumentu. Rozpoznawanie tekstu służy wyłącznie doborowi grafiki i nie weryfikuje godności, święceń, urzędu ani jurysdykcji.

### 6. Dokumenty Rodowe i rejestr podmiotów

Aplikacja udostępnia odrębne moduły dla dokumentów Rodowych oraz dokumentów organizacji i innych podmiotów.

Zakres ewidencji obejmuje między innymi:

- dane identyfikacyjne Rodu lub podmiotu;
- członkostwo, role, funkcje i strukturę organizacyjną;
- uchwały, protokoły, decyzje i pełnomocnictwa;
- majątek, nieruchomości, pojazdy, zobowiązania i należności;
- dokumenty społeczne, edukacyjne i religijne;
- formularze oraz eksport wybranych danych do PDF;
- odpis z rejestru podmiotów w zakresie obsługiwanym przez lokalny formularz.

Wpis w prywatnej ewidencji nie zastępuje wpisu w KRS, CEIDG, rejestrze kościelnym, akcie notarialnym ani innej wymaganej prawem czynności lub formy dokumentu.

### 7. Biblioteka herbów, godeł, logo i pieczęci

W **Więcej → Ustawienia → Zasoby identyfikacji dokumentów** można:

- przeglądać wbudowany katalog znaków;
- importować własne grafiki SVG, PNG, JPEG i WebP;
- przypisywać logo, godło, herb, flagę, pieczęć i inne rodzaje zasobów do określonych dokumentów lub grup;
- odłączać i zastępować przypisania;
- przeglądać podgląd zasobu;
- usuwać własne zasoby.

Wbudowany katalog zawiera 14 wcześniejszych ilustracji heraldycznych oraz dwa dodatkowe zasoby przygotowane na podstawie wzoru przekazanego przez użytkownika:

- pełny herb biskupi — galero, chwosty, krzyż, tarcza i dewiza;
- osobna tarcza herbu biskupiego.

Katalog obejmuje także koncepcyjne znaki rodowe, kościelne, zawodowe, fundacyjne, misyjne, samorządowe i inne. Są to ilustracje do zastosowań prezentacyjnych, a nie automatycznie zatwierdzone historyczne herby lub oficjalne znaki instytucji.

Przypisania mogą być używane w podglądach dokumentów oraz w obsługiwanym eksporcie PDF. Własne pliki i indeksy przypisań są przechowywane lokalnie z użyciem szyfrowania AES-GCM. Wbudowane grafiki są częścią zasobów aplikacji.

**Ważne:** grafika nie potwierdza mandatu, urzędu, godności kościelnej, kwalifikacji, członkostwa, prawa reprezentacji ani autentyczności dokumentu. Oficjalne godła i herby wolno stosować wyłącznie w prawidłowej postaci i z poszanowaniem właściwych przepisów oraz licencji.

### 8. Rodzina i zdrowie

Dostępne moduły obejmują lokalne ewidencje i archiwa:

- dane dzieci;
- rodzinne rejestry i dokumenty;
- prywatną kartę szczepień dziecka;
- historię chorób i archiwum dokumentacji medycznej;
- informacje zdrowotne;
- ustawienia danych dotyczących niepełnosprawności.

Są to narzędzia do prowadzenia własnej dokumentacji. Nie należy utożsamiać ich z Internetowym Kontem Pacjenta, e-receptą, systemem e-zdrowia ani państwowym rejestrem orzeczeń. Aplikacja nie potwierdza statusu zdrowotnego lub orzeczniczego bez odpowiedniej, rzeczywistej integracji i źródła danych.

### 9. Kody QR i wymiana danych

Moduł kodów QR udostępnia narzędzia związane z:

- własnymi schematami danych OWD;
- przygotowaniem danych kontaktowych vCard;
- generowaniem kodów QR dla adresów internetowych;
- dzieleniem większego zaszyfrowanego ładunku na ramki QR;
- oznaczeniem pochodzenia danych i kontrolą zgody na transfer w modelu OWD.

Identyfikatory schematów `pl.owd.*` są identyfikatorami własnymi projektu. Nie są schematami mObywatela ani urzędowym standardem wymiany danych. Integracja z systemem rządowym wymaga zatwierdzonego schematu, jawnego mapowania pól, uzgodnionego interfejsu i odpowiednich uprawnień. W obecnej wersji nie ma aktywnego adaptera rządowego ani potwierdzonego połączenia z rządowym mObywatelem.

### 10. Usługi i informacje obywatelskie

Pulpit i katalog usług porządkują obszary takie jak:

- sprawy publiczne i przeglądarka usług publicznych;
- Polak za granicą i informacje konsularne;
- płatności i finanse;
- transport i bilety;
- mandaty i informacje dla kierowców;
- PESEL i bezpieczeństwo;
- środowisko;
- edukacja, praca i nieruchomości;
- prawo i informacje obywatelskie;
- kultura, sport i organizacje społeczne;
- Kościół i wspólnoty.

Część pozycji jest na tym etapie katalogiem, ekranem informacyjnym lub lokalnym modułem ewidencyjnym. Widoczność kafelka nie oznacza, że aplikacja wykonuje płatność, sprawdza mandat, zastrzega PESEL, pobiera bilet, odczytuje dane konsularne lub łączy się z odpowiednim rejestrem.

### 11. Historia działań i archiwizacja

- Rejestrowanie wybranych działań nawigacyjnych i operacji w historii aplikacji.
- Import i eksport archiwum danych użytkownika w formacie vCard.
- Kontrola zapisu eksportowanego archiwum.
- Lokalne przechowywanie wybranych ewidencji i zasobów.

Zakres historii i archiwizacji zależy od konkretnego modułu. Nie należy zakładać, że wszystkie dane aplikacji są objęte jednym mechanizmem kopii zapasowej lub szyfrowania.

## Architektura i technologie

- Kotlin.
- Android SDK.
- Jetpack Compose i Material 3.
- Gradle Kotlin DSL.
- AndroidSVG — renderowanie importowanych i wbudowanych grafik SVG.
- ZXing — generowanie kodów QR.
- Android Keystore / AES-GCM — ochrona lokalnych zasobów identyfikacji dokumentów i indeksów przypisań.
- Android PdfDocument — generowanie plików PDF.
- Android Storage Access Framework — wybór plików do importu i miejsca zapisu eksportu.

Główne pliki funkcjonalne:

| Plik | Odpowiedzialność |
|---|---|
| `MainActivity.kt` | Pulpit, nawigacja, portfel i uruchamianie modułów |
| `DocumentBranding.kt` | Katalog znaków, import, przypisania, podgląd i eksport PDF |
| `ChurchDocuments.kt` | Formularze i ewidencje dokumentów kościelnych |
| `ProfessionalQualifications.kt` | Kwalifikacje zawodowe i dokumenty edukacyjno-zawodowe |
| `FamilyEntityDocuments.kt` | Dokumenty Rodowe i dokumenty podmiotów |
| `OwdSchemaRegistry.kt` | Własne wersjonowane schematy OWD i walidacja rekordów |
| `OwdQrFrames.kt` | Ramkowanie ładunków na serię kodów QR |
| `OwdIntegration.kt` | Pochodzenie danych, zgoda na transfer i granica mapowania do schematów zewnętrznych |
| `ASSET-INVENTORY.md` | Inwentarz zasobów graficznych i zasady ich użycia |

## Bezpieczeństwo, prywatność i granice zaufania

1. Dane wpisane lokalnie przez użytkownika należy odróżniać od informacji pochodzących od wystawcy lub usługi publicznej.
2. Nie oznaczać danych jako urzędowo potwierdzonych bez weryfikowalnego źródła i poprawnej integracji.
3. Nie traktować grafiki, kodu QR, PDF ani podglądu legitymacji jako zabezpieczenia dokumentu.
4. Nie odtwarzać ani nie symulować zabezpieczeń dokumentów państwowych, w tym hologramów, mikrodruku, MRZ i urzędowych kodów weryfikacyjnych.
5. Dane zdrowotne, rodzinne i identyfikacyjne wymagają szczególnej ochrony, minimalizacji zakresu, kontroli dostępu i przemyślanej polityki retencji.
6. Przed produkcyjnym użyciem należy przeprowadzić audyt bezpieczeństwa, prywatności, uprawnień, szyfrowania, kopii zapasowych, usuwania danych i zgodności prawnej.
7. Nie umieszczać w repozytorium rzeczywistych danych osobowych, numerów dokumentów, danych zdrowotnych, kluczy ani sekretów.

## Ograniczenia integracji państwowych

Aplikacja jest niezależną aplikacją obywatelską. Nie należy przedstawiać jej jako oficjalnego klienta administracji publicznej ani jako zamiennika rządowego mObywatela.

W szczególności obecna wersja nie zapewnia sama z siebie:

- logowania do systemów administracji państwowej;
- dostępu do rejestru dowodów osobistych lub paszportów;
- pobierania danych z rejestrów PESEL, KRS, CEIDG lub rejestrów medycznych;
- urzędowego potwierdzania niepełnosprawności, kwalifikacji, mandatu lub godności;
- przekazywania danych do rządowego mObywatela;
- urzędowego uwierzytelniania dokumentów, kodów QR ani podpisów.

Przyszła współpraca z usługami publicznymi może być realizowana wyłącznie przez udokumentowane i dozwolone interfejsy, po uzyskaniu wymaganych zgód, uprawnień, certyfikacji i zatwierdzeniu modelu bezpieczeństwa. Do tego czasu wszystkie funkcje lokalne należy opisywać jako prywatne narzędzia obywatelskie lub ewidencyjne.

## Pochodzenie ikon i licencje

Część piktogramów usług Androida została przeniesiona z zasobów iOS znajdujących się w tym repozytorium: `iOS/Sources/Assets.xcassets/Icons/Brand/Services/`. Zasoby źródłowe są oznaczone w nadrzędnym `REUSE.toml` jako © 2025 Centralny Ośrodek Informatyki, `LicenseRef-COI-MIT-PL`. Wersje Android VectorDrawable zachowują oznaczenia pochodzenia. Przed dystrybucją należy przestrzegać warunków licencji i zachować wymagane informacje.

Własne ilustracje heraldyczne i znaki koncepcyjne są odrębnymi zasobami. Ich źródło, charakter i ograniczenia opisuje `ASSET-INVENTORY.md`. Nie należy utożsamiać ich z oficjalnymi znakami państwowymi, kościelnymi lub samorządowymi.

## Uruchomienie i kompilacja

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL i Jetpack Compose.

Kompilacja z zainstalowanym Gradle 8.9:

```bash
cd obywatel-android
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

APK wariantu debug powstaje w:

```
app/build/outputs/apk/debug/app-debug.apk
```

Repozytorium zawiera workflow GitHub Actions `.github/workflows/android-build.yml`, który uruchamia testy JVM, buduje debug APK i publikuje artefakt CI.

## Stan projektu i dalszy rozwój

Projekt jest rozwijany etapami. Przed wydaniem produkcyjnym należy w szczególności:

1. Ukończyć audyt bezpieczeństwa i prywatności wszystkich modułów przechowujących dane osobowe i zdrowotne.
2. Ujednolicić walidację, wersjonowanie i migracje lokalnych schematów danych.
3. Rozbudować testy jednostkowe, testy UI, testy dostępności i testy importu/eksportu.
4. Zweryfikować wszystkie licencje, źródła i uprawnienia do wykorzystania znaków oraz piktogramów.
5. Rozdzielić dane demonstracyjne, dane użytkownika i dane pochodzące od zaufanych wystawców.
6. Udokumentować każdą przyszłą integrację z usługą zewnętrzną, w tym jej zakres, uprawnienia, zgodę użytkownika i model bezpieczeństwa.
7. Zachować jasne oznaczenie aplikacji jako niezależnego projektu obywatelskiego, dopóki nie zostaną formalnie uzgodnione ewentualne integracje z administracją publiczną.

## Dokumentacja dodatkowa

- `ASSET-INVENTORY.md` — katalog i zasady wykorzystania ikon, herbów, godeł i logo.
- `UI-DESIGN-NOTES.md` — notatki dotyczące interfejsu i pochodzenia zasobów.
- `../REUSE.toml` oraz `../LICENSES/` — oznaczenia prawno-licencyjne repozytorium.
