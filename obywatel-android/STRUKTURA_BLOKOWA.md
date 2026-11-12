<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->

# Szczegółowa struktura blokowa aplikacji obywatelskiej

> **Zakres dokumentu:** stan kodu gałęzi **main** na dzień 12 listopada 2026 r.  
> **Analizowany punkt odniesienia:** commit `76225967572a923cb12090cd46f5ce785621ccd7`.  
> **Projekt Android:** katalog główny `obywatel-android`, Kotlin, Jetpack Compose, Gradle Kotlin DSL.  
> **Charakter dokumentu:** opis architektury wynikający z istniejącego kodu, a nie docelowa specyfikacja ani deklaracja ukończonych integracji.

## 1. Legenda i zasady odczytu

- **Ekran / interfejs** — element prezentacji i interakcji użytkownika.
- **Koordynator** — komponent wybierający ekran, trasę lub kolejny krok procesu.
- **Logika domenowa** — reguły dokumentów, rejestrów, wymiany danych i weryfikacji.
- **Magazyn lokalny** — pliki, URI, preferencje lub zaszyfrowane archiwum na urządzeniu.
- **Zasób Androida** — grafika, ikona, kolor, motyw lub konfiguracja.
- **Moduł JVM** — komponent uruchamiany poza aplikacją Android; nie oznacza automatycznie aktywnego połączenia z aplikacją.

Strzałka oznacza zależność lub przepływ sterowania/danych widoczny w kodzie. Nie oznacza sama w sobie, że istnieje zewnętrzna usługa sieciowa.

## 2. Mapa systemu — widok najwyższego poziomu

~~~mermaid
flowchart TB
    USER["Użytkownik urządzenia Android"]
    OS["Android OS<br/>Activity, SAF, URI permissions, Keystore"]
    APP["Aplikacja obywatelska — moduł :app<br/>Kotlin + Jetpack Compose"]
    ENTRY["MainActivity<br/>punkt wejścia i host Compose"]
    FLOW["HomeScreen<br/>koordynacja stanu i nawigacji"]
    NAV["Nawigacja główna<br/>Dokumenty · Usługi · Szukaj · Skanuj · Więcej"]
    DOMAIN["Moduły domenowe<br/>dokumenty, rodzina, zdrowie, kościół,<br/>kwalifikacje, PDF, QR, OWD"]
    DATA["Warstwa lokalnych danych<br/>DataStore/SharedPreferences, pliki,<br/>SAF URI, archiwa, Android Keystore"]
    RES["Zasoby interfejsu<br/>drawable, drawable-nodpi, mipmap,<br/>values, xml"]
    JVM["Moduł :pades-validator<br/>Java 17 + DSS"]
    TESTS["Testy<br/>JVM · Android instrumentation · UI"]
    EXT["Usługi zewnętrzne / administracja<br/>brak aktywnego adaptera rządowego w tym stanie"]

    USER --> OS
    OS --> ENTRY
    ENTRY --> FLOW
    FLOW --> NAV
    NAV --> DOMAIN
    DOMAIN <--> DATA
    DOMAIN --> RES
    OS <--> DATA
    DOMAIN -. "odrębny moduł; integracja aplikacyjna niewykazana" .-> JVM
    TESTS -. "weryfikują wybrane komponenty" .-> APP
    DOMAIN -. "brak aktywnego połączenia API" .-> EXT
~~~

### 2.1. Granice projektu

| Obszar | Rola w aktualnym kodzie |
|---|---|
| Android :app | Działająca aplikacja prototypowa, interfejs, nawigacja i lokalne funkcje. |
| :pades-validator | Oddzielny moduł biblioteki JVM do walidacji PAdES; dokumentacja klasy wskazuje na uruchamianie po stronie zaufanego backendu. |
| OWD | Zestaw komponentów protokołu, kryptografii, schematów i transportu. Nie jest to integracja z rządowym mObywatelem. |
| Usługi publiczne | Ekrany informacyjne i lokalne widoki; README aplikacji wyraźnie zastrzega brak logowania do usług państwowych i brak ich API. |
| Rejestr lokalny | Dane i identyfikatory przechowywane na urządzeniu; lokalny QR nie jest urzędowym poświadczeniem. |

## 3. Struktura uruchomieniowa i nawigacja

~~~mermaid
flowchart TD
    LAUNCH["Launcher Activity Alias"]
    ACT["MainActivity : ComponentActivity"]
    COMPOSE["setContent / Compose"]
    ENTRYFLOW["ApplicationEntryFlow<br/>ekran powitalny i wejściowy"]
    WELCOME["EntryWelcomeScreen"]
    LOGIN["EntryLoginScreen<br/>tryb demonstracyjny"]
    ROOT["HomeScreen"]
    BACK["Obsługa systemowego Wstecz<br/>priorytety ekranów podrzędnych"]
    BOTTOM["Dolna nawigacja"]
    DOCS["Dokumenty"]
    SERVICES["Usługi"]
    SEARCH["Szukaj"]
    SCAN["Skanuj"]
    MORE["Więcej"]
    HOME["Pulpit / ekran główny"]
    SETTINGS["Ustawienia"]
    WALLET["Portfel dokumentów"]
    DETAILS["Szczegóły dokumentu"]
    SERVICE_DETAIL["Szczegół usługi"]

    LAUNCH --> ACT --> COMPOSE --> ENTRYFLOW
    ENTRYFLOW --> WELCOME
    ENTRYFLOW --> LOGIN
    ENTRYFLOW --> ROOT
    ROOT --> BOTTOM
    BOTTOM --> DOCS
    BOTTOM --> SERVICES
    BOTTOM --> SEARCH
    BOTTOM --> SCAN
    BOTTOM --> MORE
    DOCS --> HOME
    DOCS --> WALLET
    WALLET --> DETAILS
    SERVICES --> SERVICE_DETAIL
    MORE --> SETTINGS
    ROOT -. "systemowy przycisk Wstecz" .-> BACK
    BACK -. "zamyka aktywny ekran podrzędny" .-> ROOT
~~~

### 3.1. Główne punkty sterowania

1. Manifest deklaruje MainActivity oraz zestaw aktywowanych/dezaktywowanych aliasów ikony uruchomieniowej.
2. MainActivity uruchamia warstwę Compose i przekazuje sterowanie do ApplicationEntryFlow oraz HomeScreen.
3. HomeScreen przechowuje stan aktywnego widoku, zaznaczonej sekcji, wybranego dokumentu, aktywnej usługi i edytorów ustawień.
4. Obsługa Wstecz zamyka najpierw najbardziej zagnieżdżony ekran lub edytor, a następnie wraca do sekcji nadrzędnej.
5. ObywatelBottomNavigation definiuje pięć pozycji: Dokumenty, Usługi, Szukaj, Skanuj i Więcej. W MainActivity część ekranów jest ponadto otwierana jako widoki podrzędne.

**Uwaga architektoniczna:** znaczna część koordynacji ekranów nadal znajduje się w dużym pliku MainActivity.kt. Jest to rzeczywisty punkt centralny przepływu aplikacji, ale również istotny obszar przyszłego rozdzielenia na mniejsze koordynatory i graf nawigacji.

## 4. Moduły funkcjonalne aplikacji

~~~mermaid
flowchart TB
    ROOT["HomeScreen / MainActivity"]
    subgraph UI["Warstwa interfejsu"]
      HOME["HomeScreen"]
      NAV["ObywatelBottomNavigation"]
      SECTIONS["SectionScreen"]
      SERVICES["PublicServicesScreen"]
      DOCUI["DocumentsScreen"]
      IDUI["IdentityDocumentsScreen"]
      REGUI["RegistryScreen"]
      THEME["Theme + Color"]
    end
    subgraph DOC["Dokumenty i tożsamość"]
      ROUTES["DocumentRouteRegistry"]
      CATALOG["DocumentGraphicCatalog"]
      BRAND["DocumentBranding"]
      APPEAR["LegitimacyPreviewSettings"]
      OFFICIAL["OfficialDocuments"]
      CHURCH["ChurchDocuments"]
      FAMILY["FamilyEntityDocuments"]
      QUAL["ProfessionalQualifications"]
      HEALTH["FamilyHealthRecords"]
      HISTORY["MedicalHistoryRecords"]
      PDF["SignedPdfDocuments"]
      WALLET["ImportedDocumentStore"]
    end
    subgraph UTIL["Usługi pomocnicze"]
      OCR["DocumentOcrParser + GeneralDocumentOcrEditor"]
      QR["QR / ZXing / VCard"]
      ACTH["ActivityHistory"]
      LEGAL["LegalInformationScreen"]
      PROFILE["Profil i ustawienia"]
    end
    subgraph OWD["Obywatelska Wymiana Danych"]
      OWDUI["OWD Hub / ekrany QR"]
      SCHEMA["OwdSchemaRegistry"]
      SECURITY["OwdSecurity"]
      VCARD["OwdVCardProtocol + VCardCodec"]
      VERIFY["OwdCredentialVerifier"]
      REPLAY["OwdReplayStore"]
      TRANSPORT["OwdTransport"]
      ADAPTER["OwdIntegration<br/>granica przyszłego adaptera"]
    end

    ROOT --> UI
    ROOT --> DOC
    ROOT --> UTIL
    ROOT --> OWD
    DOCUI --> WALLET
    HOME --> ROUTES
    ROUTES --> DOC
    CATALOG --> BRAND
    APPEAR --> BRAND
    OFFICIAL --> OCR
    PDF --> QR
    OWDUI --> SCHEMA
    OWDUI --> SECURITY
    OWDUI --> VCARD
    VCARD --> SECURITY
    OWDUI --> VERIFY
    OWDUI --> REPLAY
    OWDUI --> TRANSPORT
    ADAPTER -. "kontrakt / granica integracji" .-> SCHEMA
~~~

### 4.1. Dokumenty, portfel i rejestry

| Plik / obszar | Odpowiedzialność |
|---|---|
| MainActivity.kt | Główne stany ekranów, wejścia do funkcji, pulpit, portfel dokumentów, edytory i obsługa powrotu. |
| DocumentRouteRegistry.kt | Centralny rejestr identyfikatorów i typów tras: DOCUMENT, GROUP, SPECIALIZED. |
| DocumentGraphicCatalog.kt | Katalog typów dokumentów oraz metadane potrzebne do edycji ich grafiki. |
| LegitimacyPreviewSettings.kt | Ustawienia wyglądu wzorów legitymacji i dokumentów. |
| DocumentBranding.kt | Zasoby identyfikacji dokumentów: grafiki, przypisania, podglądy, szablony i eksport PDF. |
| OfficialDocuments.kt | Formularze i dokumenty cywilne/urzędowe w zakresie lokalnego prototypu. |
| ChurchDocuments.kt | Dokumenty i ewidencje kościelne. |
| FamilyEntityDocuments.kt | Dokumenty rodzinne, rodowe, podmiotowe i dane organizacji. |
| FamilyHealthRecords.kt | Dokumentacja rodzinna i zdrowotna, w tym sekcje dzieci i szczepień. |
| MedicalHistoryRecords.kt | Dane i historia medyczna w lokalnym modelu aplikacji. |
| ProfessionalQualifications.kt | Kwalifikacje zawodowe i ich dokumentowanie. |
| SignedPdfDocuments.kt | Import, lokalne przechowywanie, szyfrowanie, podgląd i obsługa plików PDF oraz załączników podpisu. |
| data/ImportedDocumentStore.kt | Lokalne URI dokumentów importowanych przez Storage Access Framework. |
| ui/documents/DocumentsScreen.kt | Lista dokumentów importowanych, wybór, podgląd PDF/obrazów i ekstrakcja treści DOCX/ODT. |
| DocumentOcrParser.kt | Ostrożne propozycje pól na podstawie tekstu rozpoznanego przez OCR; wynik nie jest automatycznie potwierdzonym faktem. |

### 4.2. Trasy dokumentów

DocumentRouteRegistry jest źródłem prawdy dla tras uruchamianych z kafelków. Rozróżnia trzy rodzaje zachowania:

~~~mermaid
flowchart TD
    CLICK["Kliknięcie kafelka / wyniku wyszukiwania"]
    LOOKUP["DocumentRouteRegistry.find(documentId)"]
    TYPE{"Typ trasy"}
    DOC["DOCUMENT"]
    GROUP["GROUP"]
    SPEC["SPECIALIZED"]
    PREVIEW["DocumentDetailsScreen<br/>podgląd konkretnego dokumentu"]
    COLLECTION["Ekran kolekcji / usługi<br/>np. dokumenty kościelne"]
    FEATURE{"Identyfikator celu"}
    BANK["FamilyEntityDocumentsScreen<br/>rachunki bankowe"]
    VACC["FamilyHealthRecordsScreen<br/>szczepienia"]
    PDF["SignedPdfDocumentsScreen"]
    IMPORT["DocumentsScreen"]
    OTHER["Właściwy ekran specjalistyczny"]
    FALLBACK["Nieznana trasa<br/>zachowanie awaryjne"]

    CLICK --> LOOKUP --> TYPE
    TYPE --> DOC --> PREVIEW
    TYPE --> GROUP --> COLLECTION
    TYPE --> SPEC --> FEATURE
    FEATURE --> BANK
    FEATURE --> VACC
    FEATURE --> PDF
    FEATURE --> IMPORT
    FEATURE --> OTHER
    LOOKUP -. "brak wpisu" .-> FALLBACK
~~~

**Ważne:** aliasy zgodności, takie jak identity_card → identity i loyalty_cards → loyalty, zachowują obsługę starszych identyfikatorów. Trasy typu GROUP otwierają kolekcję, a nie podgląd pojedynczego dokumentu.

## 5. Dane, trwałość i kopie zapasowe

~~~mermaid
flowchart TB
    UI["Ekrany i edytory"]
    PREF["SharedPreferences<br/>ustawienia, wybory, indeksy"]
    SAF["Storage Access Framework<br/>wybrane dokumenty użytkownika"]
    URI["Trwałe uprawnienia URI<br/>bez kopiowania źródłowego pliku"]
    PRIVATE["Prywatny katalog aplikacji<br/>noBackupFilesDir / cache"]
    KEY["Android Keystore<br/>klucze szyfrujące"]
    CRYPTO["Szyfrowanie lokalnych danych<br/>AES-GCM / bezpieczne operacje"]
    STORES["Magazyny domenowe<br/>SignedPdfStore, BrandStore,<br/>ActivityHistoryStore i inne"]
    ARCH["Eksport / import archiwów JSON"]
    HASH["SHA-256<br/>kontrola zgodności lokalnego pliku"]
    QRID["Lokalny identyfikator wpisu<br/>urn:rp:local-document-registry:UUID"]

    UI --> PREF
    UI --> SAF --> URI
    UI --> STORES
    STORES <--> PRIVATE
    STORES <--> KEY
    KEY --> CRYPTO
    CRYPTO <--> PRIVATE
    STORES <--> ARCH
    URI --> HASH
    HASH --> QRID
    QRID --> UI
~~~

### 5.1. Kategorie przechowywanych danych

- **Preferencje aplikacji:** wybrane ustawienia interfejsu, widoczność dokumentów, konfiguracja ikon i stan niektórych funkcji.
- **URI dokumentów importowanych:** aplikacja zachowuje trwałe uprawnienie do odczytu wybranego zasobu. Nie kopiuje automatycznie całego pliku do własnego katalogu.
- **Dane domenowe:** rekordy dokumentów, dane podmiotów, dokumenty rodzinne, historia działań i konfiguracje.
- **Dane wrażliwe / pliki:** wybrane magazyny stosują szyfrowanie oparte o Android Keystore; szczegóły są implementowane osobno w poszczególnych magazynach.
- **Archiwa:** eksport i import wybranych zbiorów danych w celu przenoszenia lub odtwarzania lokalnych informacji.
- **Cache i podglądy:** pliki tymczasowe i obrazy przygotowane do wyświetlenia.

### 5.2. Granice zaufania i znaczenie lokalnego QR

1. Lokalny identyfikator QR wskazuje wpis w lokalnym rejestrze; nie jest sam w sobie podpisem elektronicznym, pieczęcią ani urzędowym poświadczeniem.
2. Porównanie SHA-256 wykrywa zmianę pliku względem wcześniej zapisanego skrótu; nie uwierzytelnia wystawcy dokumentu.
3. Trwałe URI SAF pozostaje zależne od dostawcy dokumentu i przyznanych przez Androida uprawnień.
4. Szyfrowanie danych w pamięci urządzenia nie oznacza automatycznie synchronizacji, kopii zapasowej ani odzyskiwania kluczy po reinstalacji.
5. Dane lokalne, dane udostępnione przez drugie urządzenie, poświadczenia podpisane przez wystawcę i dane pochodzące z usługi publicznej muszą zachować odrębne oznaczenia pochodzenia.

## 6. Obywatelska Wymiana Danych (OWD)

~~~mermaid
sequenceDiagram
    autonumber
    participant A as Urządzenie A
    participant Q as QR / oferta sesji
    participant B as Urządzenie B
    participant S as OwdSecurity
    participant V as OwdVCardProtocol / VCardCodec
    participant T as OwdTransport
    participant R as OwdReplayStore

    A->>S: Utworzenie krótkotrwałej oferty i klucza efemerycznego
    S-->>A: sessionId, klucz publiczny, nonce, termin ważności
    A->>Q: Zakodowanie oferty sesji w QR
    Q-->>B: Odczyt oferty sesji
    B->>S: Walidacja oferty i uzgodnienie klucza
    S-->>B: Klucz sesyjny
    B->>V: Przygotowanie wybranych pól vCard 4.0
    V->>S: Szyfrowanie ładunku AES-GCM
    S-->>V: Zaszyfrowany transfer
    V->>T: Przekazanie transferu do kanału transportowego
    T-->>A: Dostarczenie zaszyfrowanej wiadomości
    A->>R: Kontrola ponownego użycia sessionId
    R-->>A: Sesja nowa / odrzucona jako powtórzona
    A->>S: Weryfikacja i odszyfrowanie transferu
    S-->>A: Uwierzytelniony ładunek
    A->>V: Dekodowanie vCard i metadanych OWD
~~~

### 6.1. Składniki OWD

| Komponent | Zadanie |
|---|---|
| OwdSecurity.kt | Oferta sesji, uzgadnianie klucza, szyfrowanie i odszyfrowanie danych oraz kontrola czasu ważności. |
| OwdSchemaRegistry.kt | Wersjonowane schematy danych aplikacji i walidacja rekordów. Identyfikatory plików schematów są przestrzenią aplikacji OWD, nie schematami rządowymi. |
| OwdVCardProtocol.kt | Profil danych OWD oparty o vCard 4.0 i szyfrowany ładunek. |
| VCardCodec.kt | Kodowanie, dekodowanie, archiwizacja oraz normalizacja danych vCard. |
| OwdTransport.kt | Interfejs transportu, bezpośredni TCP, automatyczny wybór kanału, kodowanie transferu i potwierdzenia. |
| OwdReplayStore.kt | Lokalna ochrona przed ponownym wykorzystaniem identyfikatora sesji. |
| OwdCredentialVerifier.kt | Weryfikacja podpisanych poświadczeń wystawcy względem zaufanych kluczy dostarczonych z zewnętrznego, zaufanego katalogu. |
| OwdIntegration.kt | Granica i kontrakt przyszłego adaptera do formalnie zatwierdzonej integracji. |

### 6.2. Ograniczenia interpretacji

- OWD jest odrębnym protokołem aplikacyjnym. Nie należy utożsamiać go z protokołem rządowego mObywatela.
- Obecność interfejsu transportowego i klas TCP nie dowodzi wdrożenia produkcyjnego, publicznego serwera przekaźnikowego ani dostępności kanału poza siecią lokalną.
- OwdIntegration definiuje granicę przyszłego adaptera; nie potwierdza uzyskania certyfikacji ani dostępu do rządowego API.
- Podpisane poświadczenie może być uznane za zaufane tylko przy użyciu właściwie provisionowanego klucza wystawcy. Klucz dołączony przez samo poświadczenie nie może być samodzielną kotwicą zaufania.

## 7. QR, vCard, OCR i dokumenty PDF

~~~mermaid
flowchart LR
    subgraph INPUT["Wejście danych"]
      PICK["SAF / wybór pliku"]
      IMAGE["Obraz / zdjęcie"]
      QRREAD["Odczyt QR z obrazu"]
      TEXT["Tekst dokumentu"]
    end
    subgraph PROCESS["Przetwarzanie"]
      OCR["ML Kit OCR"]
      PARSE["DocumentOcrParser"]
      QRGEN["ZXing QR / kody kreskowe"]
      VCARD["VCardCodec"]
      PDFSTORE["SignedPdfStore"]
      PDFRENDER["PdfRenderer / podgląd stron"]
      DOCX["DocxTextExtractor"]
      ODT["OdtTextExtractor"]
    end
    subgraph OUTPUT["Wynik"]
      FIELDS["Propozycje pól do zatwierdzenia"]
      LOCALQR["Lokalny identyfikator QR"]
      VC["vCard / archiwum kontaktu"]
      SIGNED["Zaszyfrowany plik PDF i metadane"]
      PREVIEW["Podgląd stron / dokumentu"]
    end
    PICK --> PDFSTORE
    PICK --> DOCX
    PICK --> ODT
    PICK --> PDFRENDER
    IMAGE --> OCR --> TEXT --> PARSE --> FIELDS
    IMAGE --> QRREAD --> LOCALQR
    QRGEN --> LOCALQR
    VCARD --> VC
    PDFSTORE --> SIGNED
    PDFRENDER --> PREVIEW
    DOCX --> PREVIEW
    ODT --> PREVIEW
~~~

### 7.1. Biblioteki i zależności istotne dla funkcji

- **Jetpack Compose / Material 3:** deklaratywny interfejs Androida.
- **AndroidX Activity / Core / Lifecycle:** integracja Activity, SAF, cykl życia i API systemowe.
- **ZXing Core:** generowanie i odczyt kodów QR/kreskowych w funkcjach lokalnych.
- **AndroidSVG:** obsługa grafiki SVG w obszarach, w których jest używana.
- **ML Kit Text Recognition:** OCR tekstu z obrazów.
- **ML Kit Barcode Scanning:** biblioteka skanowania kodów; zakres aktywnego użycia należy oceniać oddzielnie od samej obecności zależności.
- **Android PdfRenderer:** lokalny podgląd stron PDF.
- **Android Keystore:** przechowywanie kluczy kryptograficznych na urządzeniu.

## 8. Grafika, wygląd i zasoby

~~~mermaid
flowchart TB
    THEME["ui/theme<br/>Color.kt · Theme.kt"]
    COMPOSE["Komponenty Compose"]
    BRAND["DocumentBranding"]
    PREVIEW["LegitimacyPreviewSettings"]
    ART["DocumentTileArtwork / DocumentCardEmblem"]
    DRAWABLE["res/drawable<br/>ikony i grafiki skalowalne"]
    NODPI["res/drawable-nodpi<br/>grafiki rastrowe bez skalowania gęstością"]
    MIPMAP["res/mipmap<br/>ikony uruchomieniowe"]
    VALUES["res/values, values-night"]
    XML["res/xml<br/>ścieżki FileProvider i konfiguracje"]

    THEME --> COMPOSE
    BRAND --> PREVIEW
    PREVIEW --> ART
    COMPOSE --> ART
    ART --> DRAWABLE
    ART --> NODPI
    COMPOSE --> MIPMAP
    THEME --> VALUES
    COMPOSE --> XML
~~~

Zasoby znajdują się w katalogu app/src/main/res. W stanie analizowanym na main występują między innymi katalogi drawable, drawable-nodpi, mipmap-anydpi-v26, values, values-night i xml. Wykorzystanie konkretnego zasobu zależy od kodu i konfiguracji Androida; sama obecność grafiki w repozytorium nie oznacza, że jest wyświetlana w aktywnym ekranie.

## 9. Moduły Gradle i układ katalogów

~~~mermaid
flowchart TB
    PROJECT["obywatel-android<br/>rootProject.name = ObywatelAndroid"]
    APP[":app<br/>com.android.application"]
    VALIDATOR[":pades-validator<br/>java-library"]
    APPMAIN["app/src/main<br/>AndroidManifest · Kotlin · res"]
    APPTEST["app/src/test<br/>testy JVM"]
    APPANDROID["app/src/androidTest<br/>testy na urządzeniu / emulatorze"]
    PJAVA["pades-validator/src/main/java<br/>PadesValidationEngine"]
    PTEST["pades-validator/src/test/java<br/>testy silnika PAdES"]

    PROJECT --> APP
    PROJECT --> VALIDATOR
    APP --> APPMAIN
    APP --> APPTEST
    APP --> APPANDROID
    VALIDATOR --> PJAVA
    VALIDATOR --> PTEST
~~~

### 9.1. Najważniejsze katalogi

| Ścieżka | Zawartość |
|---|---|
| app/src/main/java/pl/gov/obywatel/android | Główne ekrany, domeny dokumentów, logika QR/OWD, ustawienia, magazyny i koordynacja. |
| app/src/main/java/pl/gov/obywatel/android/data | Magazyny i typy danych warstwy lokalnej. |
| app/src/main/java/pl/gov/obywatel/android/ui | Podzielone ekrany: documents, home, identity, navigation, registry, sections, services, theme. |
| app/src/main/res | Grafiki, ikony, motywy i konfiguracje Androida. |
| app/src/test | Testy jednostkowe logiki, kodeków, schematów, tras i narzędzi. |
| app/src/androidTest | Testy instrumentacyjne i testy UI działające na Androidzie. |
| pades-validator/src/main/java | Silnik walidacji podpisu PAdES w module JVM. |
| pades-validator/src/test | Testy modułu walidatora. |

### 9.2. PAdES — oddzielny moduł JVM

PadesValidationEngine.java wykorzystuje bibliotekę DSS (Digital Signature Services) do walidacji dokumentów PAdES. Jest to moduł Java 17 typu java-library. Komentarz w kodzie wskazuje, że silnik powinien być hostowany za uwierzytelnionym API backendu i nie powinien być pakowany do APK Androida. W aktualnej konfiguracji Gradle moduł jest uwzględniony w projekcie, ale nie jest zależnością modułu app.

## 10. Testy i punkty kontroli jakości

~~~mermaid
flowchart TD
    CODE["Zmiana kodu"]
    JVM["Testy JVM<br/>logika, parsery, kodeki, schematy"]
    ANDROID["Testy instrumentacyjne<br/>magazyny, ekrany, nawigacja"]
    UI["Testy Compose UI<br/>interakcje na emulatorze"]
    PTEST["Testy PAdES<br/>moduł JVM"]
    RESULT{"Wynik"}
    ACCEPT["Potwierdzenie zachowania<br/>w zakresie objętym testami"]
    FIX["Diagnoza i poprawka"]
    CODE --> JVM
    CODE --> ANDROID
    CODE --> UI
    CODE --> PTEST
    JVM --> RESULT
    ANDROID --> RESULT
    UI --> RESULT
    PTEST --> RESULT
    RESULT -->|zaliczone| ACCEPT
    RESULT -->|błąd| FIX --> CODE
~~~

W repozytorium istnieją testy między innymi dla:
- rejestru tras dokumentów i nawigacji;
- katalogu dokumentów graficznych;
- ustawień i terminów ważności legitymacji;
- parsera OCR;
- schematów OWD i ochrony danych osobowych;
- kodowania vCard;
- magazynu dokumentów importowanych;
- ekstrakcji DOCX/ODT;
- skalowania podglądu obrazów i PDF;
- ekranów dokumentów i przepływów nawigacyjnych;
- silnika walidacji PAdES.

**Ten dokument nie uruchamia kompilacji ani testów.** Opisuje rozpoznaną strukturę kodu na wskazanym commicie. Wyniki testów należy sprawdzać w konkretnych przebiegach CI dla konkretnego SHA.

## 11. Obserwacje architektoniczne i zalecana kolejność dalszego porządkowania

1. **Rozdzielenie koordynatora aplikacji.** MainActivity.kt zawiera wiele stanów i rozgałęzień ekranów. W dalszym rozwoju warto wydzielić graf nawigacji, stan aplikacji oraz osobne koordynatory domen.
2. **Jeden rejestr tras.** DocumentRouteRegistry powinien pozostać jedynym źródłem definicji przejść z kafelków dokumentów. Warto dodać test kompletności pomiędzy katalogiem dokumentów, ustawieniami widoczności i rejestrem tras.
3. **Ujednolicenie magazynów.** Magazyny domenowe powinny konsekwentnie definiować format danych, wersjonowanie, szyfrowanie, import/eksport i obsługę błędów.
4. **Jawne pochodzenie danych.** Każdy rekord udostępniany dalej powinien zachowywać informację, czy pochodzi z wpisu użytkownika, od innego urządzenia, od wystawcy czy z usługi publicznej.
5. **OWD jako odrębna granica bezpieczeństwa.** Należy oddzielać protokół wymiany między urządzeniami od przyszłej integracji z administracją. Adapter rządowy wymaga osobnej specyfikacji, uzgodnionych schematów, uwierzytelnienia, zgód i formalnej akceptacji.
6. **Walidacja PAdES po stronie serwera.** Moduł JVM należy wdrażać jako kontrolowaną usługę backendową, a nie przenosić biblioteki walidacyjnej do APK.
7. **Testy kontraktowe i migracyjne.** Dla archiwów, tras, schematów OWD i ustawień należy utrzymywać testy zgodności wstecznej.
8. **Katalog zasobów graficznych.** Warto utrzymywać spis zasobów z nazwą, przeznaczeniem, formatem, wymiarami, licencją i miejscem użycia.

## 12. Zastrzeżenie zakresu i aktualizacji

Dokument opisuje **niezależny prototyp obywatelski**. Nie jest dokumentacją oficjalnego mObywatela ani potwierdzeniem integracji z administracją publiczną. Nie należy interpretować ekranów, dokumentów demonstracyjnych, kodów QR, grafik ani lokalnych rekordów jako urzędowych dokumentów, poświadczeń lub decyzji.

Po istotnej zmianie struktury projektu należy zaktualizować diagramy i tabelę modułów, podając nowy commit bazowy. W szczególności trzeba odświeżyć opis po wydzieleniu nawigacji z MainActivity, zmianach schematów OWD, wprowadzeniu backendu lub podłączeniu formalnie zatwierdzonego adaptera.
