<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->

# Audyt architektury aplikacji obywatelskiej — Android

- **Data audytu:** 12 listopada 2026 r.
- **Gałąź:** `main`
- **Commit bazowy:** `dd64388a9165282217e9fad184d93e353ecf50d5`
- **Zakres:** katalog `obywatel-android`, diagramy z `STRUKTURA_BLOKOWA.md`, kod Kotlin/Compose, moduły Gradle, magazyny lokalne, OWD, QR/OCR/PDF i testy.
- **Metoda:** przegląd statyczny struktury repozytorium i wskazanych plików. Nie wykonywano kompilacji, testów, analizy dynamicznej ani testu penetracyjnego.

## 1. Ocena ogólna

Projekt ma już szeroki zakres funkcjonalny i czytelne granice kilku domen: dokumenty, dane rodzinne i podmiotowe, kwalifikacje, dokumentacja zdrowotna, QR/vCard, OWD oraz obsługa PDF. Zaletą jest centralny rejestr tras dokumentów, rozdzielenie części ekranów do pakietu `ui`, użycie SAF do dostępu do plików użytkownika, wykorzystanie Android Keystore w wybranych magazynach oraz obecność testów jednostkowych i instrumentacyjnych.

Największe ryzyko architektoniczne wynika z koncentracji sterowania w `MainActivity.kt`, niejednorodności magazynów danych i niepełnego rozdzielenia prototypowych funkcji lokalnych od usług, które wymagają zaufanego backendu lub formalnej integracji zewnętrznej. Kod OWD zawiera istotne elementy kryptograficzne i transportowe, ale sam przegląd statyczny nie potwierdza odporności całego przepływu na ataki ani gotowości produkcyjnej.

**Ocena:** dobry fundament prototypu funkcjonalnego; brak podstaw, aby na podstawie samego kodu uznać całość za system produkcyjny obsługujący urzędowe poświadczenia lub dane o wysokiej wrażliwości.

## 2. Mocne strony

1. **Jawny rejestr tras.** `DocumentRouteRegistry` rozróżnia trasy dokumentów, grup i funkcji specjalistycznych. Ułatwia to kontrolę zachowania kafelków i zgodność z aliasami.
2. **Rozdzielenie części interfejsu.** Istnieją odrębne ekrany pulpitu, dokumentów, tożsamości, rejestru, usług, sekcji i nawigacji.
3. **Dostęp do plików przez SAF.** `ImportedDocumentStore` zachowuje trwałe URI i uprawnienia zamiast bezwarunkowo kopiować wszystkie pliki do prywatnej pamięci.
4. **Wybrane magazyny szyfrowane.** `SignedPdfDocuments`, `DocumentBranding` i `ActivityHistory` wykorzystują Android Keystore do ochrony kluczy dla swoich danych.
5. **Rozdzielenie OWD od integracji rządowej.** `OwdIntegration.kt` opisuje granicę adaptera; rejestr schematów OWD jawnie wskazuje, że identyfikatory schematów są własnością aplikacji.
6. **Weryfikacja poświadczeń z kotwicą zaufania.** `OwdCredentialVerifier` nie traktuje klucza dołączonego do poświadczenia jako samodzielnego zaufanego klucza wystawcy.
7. **Moduł PAdES poza APK.** `pades-validator` jest osobnym modułem JVM, a komentarz silnika zaleca hostowanie go za uwierzytelnionym API backendu.
8. **Testy logiki i UI.** Repozytorium zawiera testy tras, ustawień legitymacji, parsera OCR, schematów OWD, vCard, magazynu plików, podglądów PDF/obrazów i wybranych ekranów.

## 3. Ustalenia i ryzyka

### A-01. Nadmierna koncentracja koordynacji w MainActivity — wysoki priorytet

**Ustalenie:** `MainActivity.kt` zawiera host aplikacji, stan wielu ekranów, obsługę Wstecz, przejścia do dokumentów i usług, ustawienia, portfel oraz rozbudowane funkcje ekranowe. Występuje wiele flag stanu i rozgałęzień warunkowych.

**Skutek:** rośnie koszt zmian, ryzyko przypadkowego zamknięcia niewłaściwego ekranu, trudność testowania przejść oraz ryzyko regresji podczas dodawania kolejnych dokumentów.

**Zalecenie:**
- wydzielić graf nawigacji i typowany model celu nawigacji;
- przenieść stan ekranów do ViewModeli lub dedykowanych koordynatorów;
- wydzielić pulpit, ustawienia, portfel i szczegóły dokumentu do osobnych plików;
- utrzymywać jedną obsługę systemowego Wstecz opartą o stos nawigacji;
- dodawać testy przejść i powrotów dla każdego typu trasy.

**Kryterium zamknięcia:** `MainActivity` pełni rolę punktu wejścia i kompozycji najwyższego poziomu, a nie miejsca implementacji logiki wszystkich domen.

### A-02. Niejednorodna warstwa trwałości i migracji danych — wysoki priorytet

**Ustalenie:** dane są przechowywane w kilku mechanizmach: SharedPreferences, plikach prywatnych, URI SAF, zaszyfrowanych plikach i archiwach JSON. Magazyny mają odrębne implementacje.

**Skutek:** różna obsługa błędów, częściowego zapisu, konfliktów importu, migracji formatu, usuwania oraz odtwarzania po utracie klucza.

**Zalecenie:**
- zdefiniować wspólny kontrakt magazynu: odczyt, zapis atomowy, usunięcie, eksport, import, wersja schematu i migracja;
- stosować wersjonowane koperty archiwów z kontrolą integralności i walidacją rozmiarów;
- opisać zachowanie po reinstalacji, wyczyszczeniu danych i unieważnieniu klucza Keystore;
- zapewnić testy migracji ze wszystkich wspieranych wersji archiwum;
- nie deklarować kopii zapasowej jako kompletnej, jeśli nie obejmuje wszystkich domen i kluczy potrzebnych do odtworzenia.

**Kryterium zamknięcia:** każda domena ma udokumentowany cykl życia danych, testy migracji oraz przewidywalną obsługę błędów i usuwania.

### A-03. Model ochrony danych wymaga pełnego przeglądu — krytyczny przed wdrożeniem publicznym

**Ustalenie:** część danych jest szyfrowana z użyciem Keystore, ale sam fakt szyfrowania wybranych magazynów nie dowodzi jednolitej ochrony wszystkich danych. URI SAF, preferencje, cache, archiwa eksportowe i kopie plików mają różne granice bezpieczeństwa.

**Ryzyko:** wyciek danych przez eksport, logi, podglądy, pliki tymczasowe, kopie systemowe lub niekontrolowane udostępnianie URI.

**Zalecenie:**
- sporządzić mapę kategorii danych, podstaw przetwarzania, lokalizacji, odbiorców i retencji;
- wykonać model zagrożeń dla urządzenia odblokowanego, utraty urządzenia, złośliwej aplikacji, kopii zapasowej, eksportu i udostępnienia pliku;
- sprawdzić konfigurację backupu Androida, logowanie, cache, eksporty i FileProvider;
- ograniczyć uprawnienia do minimum, weryfikować URI i typy MIME oraz usuwać zbędne uprawnienia;
- opisać procedurę usunięcia danych i zachowanie kluczy po reinstalacji;
- przed publicznym wdrożeniem przygotować kompletną informację o przetwarzaniu danych i ocenę obowiązków administratora.

**Kryterium zamknięcia:** udokumentowany model zagrożeń, przegląd wszystkich magazynów i eksportów, testy bezpieczeństwa oraz zatwierdzona dokumentacja prywatności.

### A-04. OWD wymaga audytu bezpieczeństwa protokołu i transportu — krytyczny przed użyciem produkcyjnym

**Ustalenie:** kod zawiera ofertę sesji, klucze efemeryczne, szyfrowanie AES-GCM, schematy, vCard, ochronę przed powtórzeniem i transport TCP. OwdReplayStore sam opisuje ochronę jako „process-safe-enough”, co nie jest deklaracją odporności na wszystkie współbieżne i wieloprocesowe scenariusze.

**Ryzyko:** błędy cyklu życia sesji, ujawnienie endpointu, ponowne użycie nonce/kluczy, niejednoznaczna autoryzacja odbiorcy, błędy walidacji rozmiarów i schematów, niepełna obsługa zerwania połączenia lub powtórzenia transferu.

**Zalecenie:**
- opracować formalną specyfikację protokołu i model atakującego;
- zweryfikować uzgadnianie klucza, wiązanie klucza z sesją, nonce, AAD, TTL, integralność potwierdzeń i ochronę przed replay;
- określić, na jakim interfejsie nasłuchuje TCP, jak ograniczany jest dostęp sieciowy i jak zamykany jest listener;
- dodać limity długości wiadomości, czasu, liczby prób i wielkości pól;
- przetestować równoległe sesje, utratę łączności, powtórzenie, modyfikację i odtworzenie transferu;
- wymagać jawnej zgody na konkretny zestaw pól i cel udostępnienia;
- przeprowadzić niezależny przegląd kryptograficzny przed wdrożeniem.

**Kryterium zamknięcia:** zatwierdzona specyfikacja, testy negatywne i interoperacyjności, przegląd kryptograficzny oraz udokumentowane ograniczenia transportu.

### A-05. Granica integracji rządowej musi pozostać jednoznaczna — wysoki priorytet

**Ustalenie:** `OwdIntegration.kt` definiuje kontrakt `GovernmentMobywatelAdapter`, ale w repozytorium nie ma dowodu aktywnego adaptera, certyfikacji ani dostępu do API rządowego. Własne schematy OWD nie są schematami państwowymi.

**Zalecenie:**
- oddzielić pakiety i nazwy typów protokołu OWD od przyszłej integracji publicznej;
- nie prezentować statusu „urzędowo zweryfikowany” bez walidacji w zaufanym źródle;
- wymagać wersjonowanej specyfikacji API, autoryzacji, zgody użytkownika, audytu i formalnych uprawnień;
- przechowywać pochodzenie danych i dowód weryfikacji oddzielnie od wartości pól.

**Kryterium zamknięcia:** testowany adapter oparty na oficjalnej dokumentacji, uzyskane uprawnienia i jasne oznaczenia źródła danych.

### A-06. Model dokumentu i jego status prawny powinny być rozdzielone — wysoki priorytet

**Ustalenie:** aplikacja obsługuje wiele typów dokumentów i formularzy, a część nazw może przypominać dokumenty urzędowe, legitymacje lub poświadczenia. README i ekran informacji prawnych już zawierają zastrzeżenia, ale potrzebne jest ich konsekwentne przeniesienie do widoków, eksportów i podglądów.

**Zalecenie:**
- dodać do modelu dokumentu jawne pole pochodzenia: prywatny wpis, dokument importowany, poświadczenie wystawcy, dane z usługi publicznej;
- rozdzielić status prawny od nazwy i grafiki;
- w podglądach i PDF umieszczać oznaczenie „wzór”, „dokument prywatny” lub „dane niezweryfikowane”, gdy ma to zastosowanie;
- nie generować imitacji godła, pieczęci ani oznaczeń sugerujących wystawcę publicznego;
- każdą kategorię dokumentu opisać pod kątem wystawcy, podstawy prawnej i procedury.

**Kryterium zamknięcia:** spójny model pochodzenia i statusu dokumentu w UI, eksporcie, QR i archiwum.

### A-07. Katalog tras wymaga kontroli kompletności — średni priorytet

**Ustalenie:** `DocumentRouteRegistry` centralizuje trasy, ale widoki i ustawienia nadal zawierają dodatkowe warunki kierujące do ekranów specjalistycznych.

**Zalecenie:**
- testować, że każdy widoczny kafelek ma trasę albo jawnie określony cel zewnętrzny;
- sprawdzać unikalność identyfikatorów i poprawność aliasów;
- generować listę ekranów osiągalnych i nieosiągalnych;
- ograniczyć duplikowanie identyfikatorów i etykiet w MainActivity.

**Kryterium zamknięcia:** test katalogu wykrywa brakującą, zdublowaną i nieosiągalną trasę.

### A-08. Testy wymagają rozszerzenia o scenariusze przekrojowe — średni priorytet

**Ustalenie:** istnieją testy jednostkowe, instrumentacyjne i UI, ale ich obecność nie dowodzi pokrycia wszystkich dokumentów, migracji, rozmiarów ekranu, błędów sieci i granic bezpieczeństwa.

**Zalecenie:**
- dodać testy kontraktowe magazynów i migracji;
- dodać testy dostępności i skalowania interfejsu;
- objąć testami wszystkie trasy dokumentów i każdy typ ekranu specjalistycznego;
- dodać testy awarii SAF, braku uprawnień URI, uszkodzonych plików, braku miejsca i przerwanego eksportu;
- dodać testy OWD dla replay, TTL, równoległości, błędnego podpisu, nieznanego wystawcy i uszkodzonego ładunku.

**Kryterium zamknięcia:** macierz testów powiązana z wymaganiami i raportami CI dla konkretnego SHA.

### A-09. Dostępność i obsługa błędów — średni priorytet

**Zalecenie:** ujednolicić semantykę Compose, kolejność fokusu, komunikaty błędów, stany ładowania i puste stany. Dodać testy TalkBack, dużych czcionek, orientacji ekranu i ograniczonej przestrzeni. Błędy importu/eksportu powinny informować o skutku i sposobie odzyskania danych bez ujawniania treści wrażliwych.

### A-10. Moduł PAdES wymaga jawnego kontraktu backendowego — wysoki priorytet

**Ustalenie:** silnik DSS/PAdES jest oddzielony od APK, co jest korzystną granicą. Sam moduł nie zapewnia jednak uwierzytelnienia API, polityki walidacji, aktualizacji list zaufania, archiwizacji raportów ani zarządzania kluczami serwera.

**Zalecenie:** przed wdrożeniem przygotować osobny projekt usługi backendowej z uwierzytelnianiem, limitami plików, izolacją przetwarzania, aktualizacją zaufanych list, raportowaniem i polityką retencji.

## 4. Priorytety realizacji

| Priorytet | Zakres | Warunek przejścia dalej |
|---|---|---|
| P0 — przed publicznym wdrożeniem | A-03 ochrona danych, A-04 bezpieczeństwo OWD, A-06 status dokumentów | Model zagrożeń, przegląd bezpieczeństwa i spójne oznaczenia prawne. |
| P1 — najbliższy etap architektury | A-01 MainActivity, A-02 magazyny, A-05 integracje, A-10 PAdES | Wydzielone granice domen, wersjonowanie danych i specyfikacje kontraktów. |
| P2 — jakość i utrzymanie | A-07 trasy, A-08 testy, A-09 dostępność | Macierz pokrycia testami i kontrola kompletności ekranów. |

## 5. Proponowany plan wykonawczy

1. **Etap 1 — inwentaryzacja danych:** spis wszystkich magazynów, pól wrażliwych, plików, URI, eksportów, logów i kopii.
2. **Etap 2 — model domeny:** wspólne typy pochodzenia i statusu dokumentu, wersjonowanie schematów i kontrakty magazynów.
3. **Etap 3 — dekompozycja interfejsu:** wydzielenie nawigacji i koordynatorów z MainActivity bez zmiany zachowania użytkownika.
4. **Etap 4 — bezpieczeństwo OWD:** specyfikacja, testy negatywne, kontrola transportu i niezależny przegląd kryptografii.
5. **Etap 5 — testy regresji:** macierz tras, ekranów, rozmiarów, migracji i błędów I/O.
6. **Etap 6 — gotowość prawna i operacyjna:** dokumentacja prywatności, licencji, statusu dokumentów i granic integracji.

## 6. Ograniczenia audytu

- Audyt statyczny nie potwierdza poprawności działania aplikacji na urządzeniu.
- Nie wykonano kompilacji ani testów automatycznych zgodnie z zakresem zlecenia.
- Nie wykonano testu penetracyjnego, analizy binarnej APK, analizy zależności pod kątem podatności ani przeglądu kryptograficznego.
- Nie oceniano zgodności z RODO w pełnym zakresie; wymaga to ustalenia rzeczywistego administratora, celów, podstaw, procesorów, retencji i przepływów danych.
- Nie potwierdzono integracji z systemami administracji publicznej ani certyfikacji.

## 7. Pliki źródłowe audytu

- `STRUKTURA_BLOKOWA.md` — diagramy i opis architektury.
- `README.md` — zakres funkcjonalny, ograniczenia i instrukcja uruchomienia.
- `app/src/main/java/pl/gov/obywatel/android/MainActivity.kt` — koordynacja UI i nawigacji.
- `DocumentRouteRegistry.kt` — rejestr tras dokumentów.
- `OwdSecurity.kt`, `OwdTransport.kt`, `OwdReplayStore.kt`, `OwdSchemaRegistry.kt`, `OwdCredentialVerifier.kt`, `OwdIntegration.kt` — OWD.
- `data/ImportedDocumentStore.kt`, `SignedPdfDocuments.kt`, `DocumentBranding.kt`, `ActivityHistory.kt` — przykładowe magazyny danych.
- `pades-validator/` — osobny moduł walidacji PAdES.
