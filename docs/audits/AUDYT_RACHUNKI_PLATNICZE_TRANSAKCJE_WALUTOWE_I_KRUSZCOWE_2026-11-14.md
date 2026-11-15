<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Audyt funkcjonalno-architektoniczny: „Rachunki płatnicze” — rejestr transakcji walutowych i kruszcowych

- **Data:** 14 listopada 2026 r.
- **Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`
- **Gałąź audytowana:** `feature/payment-ledger-stage-1`
- **Commit bazowy:** `631aa68ed0e2bcbedd70201751214bd4c08fd5cc`
- **Obszar:** `obywatel-android` (Android, Kotlin, Jetpack Compose)
- **Status (aktualizacja 15 listopada 2026 r.):** etapy 1–69 są zapisane w gałęzi PR. Etap 70 zaostrza kontrolę kierunku nóg FX; w jego teście regresyjnym CI wykryło błędny scenariusz testowy (test oczekiwał konfliktu dla prawidłowej pary), który poprawiono. Etap 71 utwardza parser XML przeciw DTD/encji zewnętrznych i ogranicza liczbę operacji. Dla końcowego SHA `2fe27c19523502c768c5e1d2347eca68ed47a73b` CI jest w toku/kolejce; nie ma jeszcze potwierdzenia testów JVM, emulatorowych ani APK. CSV/XLS, adapter PDF, formularze ręczne i notowania NBP pozostają do wykonania. PR #68 pozostaje otwarty i niescalony.
- **Cel:** przygotowanie nowego, domyślnie widocznego kafelka **Rachunki płatnicze** dla lokalnej ewidencji transakcji wymiany walut i operacji na kruszcach, importu/eksportu danych w JSON oraz wyceny historycznej i bieżącej z wykorzystaniem publicznych danych NBP.

> **Zasada nadrzędna:** rejestr ma wiernie przechowywać to, co faktycznie wydarzyło się w transakcji, i wyraźnie oddzielać dane źródłowe od obliczeń, wycen referencyjnych i hipotetycznych wyników. Kurs NBP nie jest kursem wykonania transakcji w banku ani ofertą, po której użytkownik może rzeczywiście kupić lub sprzedać walutę czy kruszec.

---

## 1. Podsumowanie wykonawcze

Projekt ma już osobny moduł „Rachunki” przeznaczony do wystawiania i archiwizacji rachunków sprzedaży. Ten moduł pozostaje nienaruszony. Nowe „Rachunki płatnicze” powinny być niezależną funkcją finansową, której zadaniem jest ewidencja przepływów walutowych i kruszcowych, a nie wystawianie dokumentów księgowych.

Rekomendowany zakres obejmuje:
1. nowy kafelek na pulpicie, domyślnie widoczny, z własnym ekranem specjalistycznym i identyfikatorem trasy;
2. model transakcji dwustronnej: co oddano, co otrzymano, kiedy, po jakim kursie i z jakiego źródła;
3. import pliku historii transakcji iPKO / kantoru internetowego PKO BP oraz plików z innych źródeł, z parserami niezależnymi od dostawcy;
4. ręczną korektę danych odczytanych z pliku i jawny status pewności parsowania;
5. bazę lokalną i wersjonowane archiwum JSON, obsługujące import, eksport, kopię, scalanie, konflikty i migracje;
6. adapter NBP do pobierania kursów walut tabel A, B i C oraz cen złota;
7. silnik kalkulacji wartości referencyjnej, zysku/straty niezrealizowanej, zrealizowanej i wyniku po kosztach;
8. zestawienia, filtry, wykresy, szczegóły operacji i historię zmian;
9. ochronę danych finansowych, obsługę błędów, testy jednostkowe, instrumentacyjne, regresyjne i wydajnościowe.

**Rekomendacja architektoniczna:** wydzielić domenę `payments` lub `financialledger` w osobne pliki i pakiety, nie rozbudowywać dalej `MainActivity.kt` ponad niezbędną rejestrację przejścia. W obecnym projekcie centralny koordynator ma już 5876 linii, a architektoniczny audyt z 12 listopada wskazuje jego nadmierną koncentrację jako ryzyko wysokiego priorytetu.

**Ocena wykonalności:** wysoka dla ręcznej ewidencji, importu iPKO z pliku oraz NBP; średnia dla pełnej automatyzacji rozpoznawania plików, zależna od rzeczywistego formatu eksportu i próbek; brak podstaw do zakładania oficjalnego, bezpośredniego połączenia z kontem iPKO bez udostępnionego przez bank, autoryzowanego interfejsu.

---

## 2. Stan istniejący i ustalenia z repozytorium

### 2.1. Potwierdzone elementy

Przegląd gałęzi `main` i kodu aplikacji potwierdził:
- aplikacja Android wykorzystuje Kotlin, Jetpack Compose i Material 3;
- podstawowym koordynatorem UI jest `obywatel-android/app/src/main/java/pl/gov/obywatel/android/MainActivity.kt`;
- istnieje centralny `DocumentRouteRegistry`, rozróżniający trasy typu `GROUP`, `DOCUMENT` i `SPECIALIZED`;
- katalog kafelków jest utrzymywany w `documentDisplayOptions` w `MainActivity.kt`;
- domyślna widoczność kafelków jest określana w `defaultDocumentPlacements`; obecnie do pulpitu domyślnie trafiają m.in. `identity` oraz `receipts`;
- istnieją odrębne ekrany dla rachunków oraz rachunków bankowych;
- aplikacja ma uprawnienie `INTERNET`;
- istnieją testy JVM i testy instrumentacyjne Compose;
- w repozytorium jest już rozbudowany opis architektury i struktury blokowej.

### 2.2. Aktualny moduł „Rachunki” — nie łączyć domen

Plik `ReceiptDocuments.kt` zawiera:
- lokalny `ReceiptStore`, indeks JSON i prywatny katalog `noBackupFilesDir/receipts`;
- obsługę importu PDF z walidacją nagłówka i limitu wielkości 50 MB;
- opcjonalny zapis kopii PDF do katalogu wybranego przez użytkownika przez SAF;
- automatyczne tworzenie podkatalogów według roku i miesiąca;
- formularz wystawiania rachunku, dane stron, pozycje, sumy, podpisującego i ustawienia wzoru;
- import zdjęcia / obrazu i przekształcenie go do PDF;
- ekran ustawień rachunków i katalogu zapisu.

To nie jest model historii operacji finansowych. Nie należy dopisywać transakcji do `ReceiptRecord` ani zmieniać znaczenia `receipts`. Nowy rejestr otrzymuje własny identyfikator, model danych, magazyn i ekran.

### 2.3. Kontekst zmian ostatnich pull requestów

- PR #63 rozwijał import zdjęcia ręcznie wypisanego rachunku, zapis do folderów rok/miesiąc i akcje na PDF.
- PR #64 rozwijał formularz, podmioty, obliczanie sum, ustawienia podpisującego i jakość PDF.
- PR #65 poprawiał import/eksport vCard, przenoszenie operacji plikowych do `Dispatchers.IO`, weryfikację zapisu i potwierdzenie usuwania rachunków.
- PR #67 pozostaje otwarty i dotyczy dokumentów służb publicznych; nie należy mieszać zakresu nowej funkcji finansowej z tym PR.
- Poprzedni audyt architektury wskazywał nadmierną koncentrację stanu i nawigacji w `MainActivity.kt` oraz niejednorodność mechanizmów trwałości danych.

### 2.4. Konsekwencje dla implementacji

1. Nie zmieniać istniejącego kafelka `receipts`, ekranu `ReceiptDocumentsScreen`, ustawień wzoru ani magazynu PDF.
2. Dodać nową trasę `payments` / `payment_transactions` typu `SPECIALIZED`.
3. Kafelek nowej domeny umieścić w katalogu dokumentów jako odrębną pozycję w sekcji finansowej.
4. Utrzymać zgodność starych archiwów i identyfikatorów; nowa baza ma odrębny schemat.
5. Operacje importu, eksportu, pobierania notowań i większe obliczenia wykonywać poza głównym wątkiem.

---

## 3. Zakres funkcjonalny

### 3.1. Kafelek i ekran główny

**Identyfikator proponowany:** `payment_transactions` (ewentualny alias `payments` wyłącznie, jeśli repozytorium potrzebuje krótszej trasy).

**Nazwa użytkowa:** Rachunki płatnicze.

**Opis:** Rejestr transakcji walutowych i kruszcowych.

**Widoczność:** domyślnie `dashboard`, z możliwością ukrycia i ponownego dodania w ustawieniach widoczności dokumentów.

**Ikona:** istniejąca ikona portfela / wymiany / wykresu z biblioteki zasobów; nie tworzyć przypadkowej ikony emoji ani nie kopiować zastrzeżonego oznaczenia banku.

**Ekran główny powinien pokazywać:**
- liczbę operacji i ostatni import;
- podsumowanie ilości posiadanych aktywów wg waluty/kruszcu;
- orientacyjną wartość portfela w wybranej walucie bazowej;
- niezrealizowany wynik referencyjny wraz z datą i rodzajem kursu;
- zrealizowany wynik z zamkniętych pozycji, jeżeli użytkownik wybierze metodę rozliczania;
- ostatnie operacje;
- skróty: Dodaj transakcję, Importuj historię, Kursy NBP, Eksportuj JSON;
- czytelny stan pobrania notowań: aktualne / ostatnia udana aktualizacja / offline / błąd.

Każda wycena ma pokazywać źródło, datę notowania i metodę kalkulacji. Nie należy wyświetlać kwoty „zysku” bez informacji, że jest to wynik hipotetyczny, jeśli wyliczono go z kursu średniego NBP.

### 3.2. Typy operacji

Wymagane typy:
- wymiana waluty na walutę, np. PLN → EUR;
- wymiana waluty na PLN;
- wymiana waluty na inną walutę obcą, np. EUR → USD;
- zakup waluty za środki z rachunku;
- sprzedaż waluty i wpływ środków;
- zakup kruszcu za walutę;
- sprzedaż kruszcu za walutę;
- zakup / sprzedaż z opłatą prowizyjną;
- transfer między własnymi rachunkami, bez zmiany posiadanych aktywów netto;
- korekta, zwrot, anulowanie lub odwrócenie transakcji;
- operacja ręczna, jeśli nie występuje w importowanej historii;
- w przyszłości: lokata walutowa, odsetki, opłata za przechowywanie kruszcu i inne zdarzenia portfelowe.

Nie należy utożsamiać transferu waluty między własnymi rachunkami z zakupem lub sprzedażą. W przeciwnym razie bilans ilości aktywa i wynik inwestycyjny zostaną zafałszowane.

### 3.3. Minimalne dane transakcji

Każda operacja powinna mieć co najmniej:
- trwały identyfikator UUID;
- datę i — jeśli dostępna — godzinę wykonania;
- datę księgowania / rozliczenia, jeśli różna;
- źródło: bank, kantor internetowy, kantor stacjonarny, broker, użytkownik, import;
- nazwę instytucji i opcjonalny rachunek/portfel, z maskowaniem danych;
- rodzaj operacji;
- walutę lub jednostkę aktywa oddanego;
- dokładną ilość oddaną;
- walutę lub jednostkę aktywa otrzymanego;
- dokładną ilość otrzymaną;
- kurs wykonania oraz kierunek pary walutowej;
- prowizję i inne opłaty: kwotę, walutę, typ i sposób naliczenia;
- numer transakcji zewnętrznej (jeśli dostępny);
- oryginalny opis z pliku;
- źródło i lokalizację importu / identyfikator pliku;
- status parsowania i status weryfikacji przez użytkownika;
- datę utworzenia i ostatniej modyfikacji rekordu;
- pole uwag;
- opcjonalny odnośnik do dokumentu źródłowego, bez niekontrolowanego kopiowania wrażliwych plików.

Kwoty przechowywać jako wartości dziesiętne o ustalonej precyzji i skali, nigdy jako `Float` ani `Double` w logice finansowej. W Kotlinie rekomendowane `BigDecimal` z jawnie określonym `RoundingMode`; w JSON kwoty zapisywać jako ciągi dziesiętne (np. `"42.63"`), a nie binarne liczby zmiennoprzecinkowe.

### 3.4. Waluty

Podstawą identyfikacji jest kod ISO 4217 (np. PLN, EUR, USD, CHF, GBP, CZK, JPY), ale słownik musi być elastyczny:
- nazwa, symbol, kod, liczba miejsc dziesiętnych;
- waluta aktywna/wycofana;
- źródło kursu obsługujące tę walutę;
- opcjonalna flaga jako zasób aplikacji, nie część klucza danych.

Nie zakładać, że każda waluta z transakcji pojawi się w tabeli A. Tabela B obejmuje inną grupę walut, a tabela C tylko waluty, dla których NBP publikuje kupno i sprzedaż. Należy obsłużyć brak notowania.

### 3.5. Kruszce i metale

Pierwszy zakres:
- złoto (XAU) z ewidencją masy w gramach;
- rozróżnienie masy brutto, masy czystego złota i próby;
- jednostki gram, uncja trojańska (ozt), kilogram;
- próby np. 999, 986, 750, 585 i inne, z walidacją zakresu.

Dalszy zakres po potwierdzeniu źródeł:
- srebro (XAG), platyna (XPT), pallad (XPD);
- monety bulionowe i numizmatyczne;
- sztabki według producenta, masy, próby, numeru seryjnego i kosztów;
- osobne ceny referencyjne spot i ceny konkretnego produktu.

**Ważne ograniczenie NBP:** publiczny endpoint cen złota NBP publikuje cenę 1 grama złota próby 1000. Nie jest to cennik skupu i sprzedaży dowolnej monety, sztabki, biżuterii, próby ani produktu dealera. Nie wolno stosować tej ceny bez korekty do każdego przedmiotu bez uwzględnienia czystości i kosztów transakcyjnych.

---

## 4. Import historii iPKO / Kantoru PKO BP

### 4.1. Zasada integracji

W pierwszej kolejności obsłużyć plik udostępniony przez użytkownika z poziomu serwisu bankowości internetowej, kantoru iPKO lub innego eksportu bankowego. Aplikacja nie powinna:
- żądać loginu i hasła do iPKO;
- przechowywać haseł, kodów SMS, tokenów lub danych sesyjnych;
- automatycznie logować się do banku przez przeglądarkę lub symulację UI;
- omijać uwierzytelniania wieloskładnikowego ani zabezpieczeń banku;
- wysyłać pliku historii do zewnętrznego serwisu parsera;
- twierdzić, że ma bezpośrednią integrację z bankiem, jeżeli faktycznie importuje plik.

Dalsza integracja API może być rozważona wyłącznie po potwierdzeniu oficjalnego interfejsu i warunków dostępu PKO BP. Do tego czasu integracja jest importem plikowym.

### 4.2. Źródło wiedzy o formacie

Instrukcja „Instrukcja użytkownika kantoru PKO Banku Polskiego SA”, wersja 1.8, potwierdza istnienie historii transakcji dostępnej w iPKO i IKO. Wskazuje filtry po dacie, kwocie i walucie. Pokazuje tabelę z polami: Data, Numer transakcji, Kupiono, Sprzedano, Para walutowa i Kurs. Szczegóły transakcji mogą zawierać rachunek docelowy, rachunek źródłowy, datę i godzinę wykonania, datę rozliczenia oraz informację o kanale wykonania.

Instrukcja jest przydatna do zdefiniowania mapowania semantycznego, ale nie stanowi specyfikacji formatu CSV/PDF/HTML aktualnie pobieranego przez użytkownika. Parser nie może być uznany za gotowy, dopóki nie otrzyma rzeczywistych, zanonimizowanych plików wzorcowych.

### 4.3. Obsługiwane rodzaje plików

Proponowana kolejność:
1. CSV — preferowany format transakcyjny, jeśli iPKO udostępnia eksport w danym widoku;
2. XLSX — jeżeli eksport oferuje arkusz kalkulacyjny;
3. PDF — odczyt struktury tekstowej, a dla skanów OCR jako tryb pomocniczy;
4. HTML — tylko gdy bank udostępnia zapisany raport/stronę i można go lokalnie przetworzyć;
5. ręczne wprowadzenie / poprawa wpisu.

Nie obiecywać obsługi wszystkich formatów przed otrzymaniem próbek. Każdy parser powinien mieć identyfikację wersji i testy z plikami wzorcowymi, bez przechowywania danych rzeczywistych użytkownika w repozytorium.

### 4.4. Mapowanie danych

Przykładowe mapowanie:
- Data → `executedAt`;
- Numer transakcji → `externalTransactionId`;
- Kupiono → `receivedAmount` + `receivedAsset`;
- Sprzedano → `spentAmount` + `spentAsset`;
- Para walutowa → `pair`;
- Kurs → `executionRate` z normalizacją kierunku;
- Data/godzina transakcji → `executedAt`;
- Data rozliczenia → `settledAt`;
- rachunek „Na konto” → zamaskowany rachunek docelowy;
- rachunek „Płacę z” → zamaskowany rachunek źródłowy;
- kanał wykonania → `channel`.

**Nie opierać importu wyłącznie na kolejności kolumn.** Rozpoznawać nagłówki, aliasy polskie i angielskie, kodowanie (UTF-8, ewentualnie Windows-1250), separator (przecinek, średnik, tabulator), separator dziesiętny oraz warianty dat. W razie niejednoznaczności pokazać podgląd mapowania i wymagać zatwierdzenia.

### 4.5. Bezpieczny proces importu

1. Użytkownik wybiera plik przez Android Storage Access Framework.
2. Aplikacja sprawdza typ MIME, rozszerzenie, limit rozmiaru, kodowanie i czytelność.
3. Parser pracuje w `Dispatchers.IO`, z limitem czasu / pamięci i ochroną przed plikiem nadmiernie dużym.
4. Parser rozpoznaje format i tworzy zestaw kandydatów transakcji.
5. UI pokazuje podsumowanie: liczba poprawnych wierszy, ostrzeżeń, błędów, duplikatów i rekordów wymagających ręcznej weryfikacji.
6. Użytkownik może przejść do szczegółu każdego podejrzanego wiersza.
7. Przed zapisem pokazujemy podsumowanie zmian; import ma operację atomową lub bezpieczny etap tymczasowy.
8. Rekordy zachowują informację o źródle i surowy tekst wejściowy (z ograniczeniem długości).
9. Po zapisie generujemy raport importu, nie logując numerów rachunków ani pełnych danych finansowych.

### 4.6. Wykrywanie duplikatów

Priorytet kluczy:
- dostawca + identyfikator transakcji zewnętrznej;
- ewentualnie identyfikator rachunku zewnętrznego, jeżeli nie jest danymi nadmiarowymi;
- awaryjny odcisk: data/godzina + waluta oddana + kwota + waluta otrzymana + kwota + źródło;
- dla ręcznych rekordów UUID.

Odcisk nie może automatycznie scalać transakcji o identycznej kwocie i dacie, jeśli nie ma wystarczającej pewności. W takim przypadku oznaczyć możliwy duplikat i dać użytkownikowi decyzję. Powtórny import tego samego pliku nie powinien tworzyć duplikatów.

### 4.7. Jakość odczytu

Status parsera:
- `AUTO_CONFIRMED` — komplet pól rozpoznany jednoznacznie i zgodny ze schematem;
- `NEEDS_REVIEW` — odczyt prawdopodobny, ale wymaga potwierdzenia;
- `PARTIAL` — rozpoznano tylko część danych;
- `REJECTED` — plik/wiersz niepoprawny;
- `USER_VERIFIED` — użytkownik potwierdził lub skorygował dane.

OCR ma tylko proponować wartości, nigdy bezwarunkowo tworzyć zatwierdzony rekord finansowy.

---

## 5. Baza danych i archiwum JSON

### 5.1. Rozdzielenie magazynu operacyjnego i formatu wymiany

Rekomendacja: wewnętrzna baza Room/SQLite jako źródło prawdy, a JSON jako przenośny format importu i eksportu. Jeśli obecny projekt z przyczyn architektonicznych pozostanie przy magazynie plikowym, musi zapewnić transakcje atomowe, blokadę współbieżnego zapisu, spójny indeks, wersjonowanie i migracje. Dla historii transakcji, indeksowania, filtrów i relacji Room będzie rozwiązaniem bezpieczniejszym i łatwiejszym do rozwoju.

Nie używać jednego pliku JSON jako jedynej bazy wieloletniej bez strategii atomowego zapisu i indeksów.

### 5.2. Proponowane tabele / encje

- `FinancialTransaction` — operacja źródłowa;
- `Asset` — waluty i kruszce;
- `Account` — rachunek/portfel (opcjonalnie, z maskowaniem numeru);
- `TransactionFee` — opłaty i prowizje;
- `Quote` — kursy i ceny referencyjne z datą publikacji i źródłem;
- `ValuationSnapshot` — zapis wyniku wyceny na konkretny moment;
- `ImportBatch` — plik/partia importu, parser, wersja, raport;
- `ImportRow` — ślad wiersza źródłowego, jeśli potrzebny do audytu importu;
- `PositionLot` — partia nabycia aktywa, dla kosztu nabycia i rozliczania sprzedaży;
- `AuditEvent` — historia dodania, edycji, usunięcia, importu i korekty;
- `ArchiveMetadata` — schemat, data eksportu, identyfikator instalacji (losowy), zakres archiwum.

### 5.3. Proponowany model domenowy (logiczny)

```kotlin
data class FinancialTransaction(
    val id: String,
    val executedAt: Instant,
    val settledAt: Instant?,
    val source: TransactionSource,
    val provider: String?,
    val externalTransactionId: String?,
    val kind: TransactionKind,
    val spentAssetCode: String,
    val spentAmount: BigDecimal,
    val receivedAssetCode: String,
    val receivedAmount: BigDecimal,
    val executionRate: BigDecimal?,
    val executionRateBase: String?,
    val executionRateQuote: String?,
    val fees: List<TransactionFee>,
    val rawDescription: String?,
    val importBatchId: String?,
    val parseStatus: ParseStatus,
    val userVerified: Boolean,
    val note: String?
)
```

To schemat poglądowy; finalne typy muszą dopasować się do istniejących konwencji modelowania i wersji Kotlin w repozytorium.

### 5.4. Format JSON

Proponowana koperta:
```json
{
  "format": "mobywatel-payment-ledger",
  "schemaVersion": 1,
  "exportedAt": "2026-11-14T17:00:00Z",
  "baseCurrency": "PLN",
  "assets": [],
  "transactions": [],
  "quotes": [],
  "accounts": [],
  "metadata": {
    "application": "mObywatelska",
    "dataClassification": "private-user-financial-records"
  }
}
```

Wymagania:
- wersja schematu obowiązkowa;
- walidacja wymaganych pól przed importem;
- pola nieznane zachowywać w rozszerzeniach zgodnie z polityką kompatybilności;
- import starej wersji przez migrator, nie przez zgadywanie;
- zachować UUID i źródłowe identyfikatory;
- atomowa wymiana / bezpieczne scalanie;
- raport importu i możliwość cofnięcia partii;
- opcjonalny skrót integralności całej koperty;
- eksport wybranego okresu, całości lub wybranych klas danych;
- eksport danych finansowych traktować jako wrażliwy — jawne potwierdzenie i informacja o miejscu zapisu;
- nigdy nie eksportować haseł, tokenów, danych logowania ani sekretów.

### 5.5. Scalanie i konflikty

Tryby:
- **Dodaj tylko nowe** — bez modyfikowania istniejących rekordów;
- **Scal po identyfikatorze** — aktualizacja zgodnie z regułą źródła i znacznika czasu;
- **Zastąp całą bazę** — operacja z kopią bezpieczeństwa i potwierdzeniem;
- **Podgląd konfliktów** — wybór użytkownika przy sprzecznych wartościach.

Źródłowe dane transakcji nie mogą być po cichu nadpisywane danymi referencyjnymi z NBP. Edycje użytkownika muszą zostać odnotowane.

---

## 6. NBP — źródło kursów i cen

### 6.1. Potwierdzony zakres API

Oficjalny NBP Web API (`https://api.nbp.pl/`) udostępnia format JSON i XML. Obsługuje:
- tabela A — średnie kursy walut obcych;
- tabela B — średnie kursy innych walut obcych;
- tabela C — kursy kupna i sprzedaży;
- kursy pojedynczych walut i całe tabele;
- archiwalne kursy walut od 2 stycznia 2002 r.;
- ceny złota wyliczone przez NBP, historycznie od 2 stycznia 2013 r.;
- zakres dat do 93 dni w jednym zapytaniu.

Od 1 sierpnia 2025 r. wymagany jest HTTPS.

### 6.2. Endpointy

Kurs średni aktualny:
`GET https://api.nbp.pl/api/exchangerates/tables/A/?format=json`

Kurs kupna/sprzedaży aktualny:
`GET https://api.nbp.pl/api/exchangerates/tables/C/?format=json`

Historia jednej waluty:
`GET https://api.nbp.pl/api/exchangerates/rates/A/EUR/2026-01-01/2026-03-31/?format=json`

Cena złota aktualna:
`GET https://api.nbp.pl/api/cenyzlota/?format=json`

Historia cen złota:
`GET https://api.nbp.pl/api/cenyzlota/2026-01-01/2026-03-31/?format=json`

Zakres dat w przykładach należy dynamicznie podzielić na segmenty nie dłuższe niż 93 dni. Dni wolne i brak opublikowanej tabeli mogą skutkować odpowiedzią 404, która nie powinna być traktowana jako awaria całej aplikacji.

### 6.3. Zasady synchronizacji

- ręczne pobranie na żądanie użytkownika;
- opcjonalne okresowe odświeżanie przez WorkManager, z zachowaniem ograniczeń systemu Android;
- cache odpowiedzi z identyfikatorem tabeli, numerem tabeli, datą efektywną, datą notowania i datą pobrania;
- etag/Last-Modified, jeśli serwis je udostępni;
- limit czasu, ponowienia z wykładniczym opóźnieniem i limit liczby prób;
- obsługa offline przez ostatnie poprawne notowania;
- wyraźny komunikat o wieku danych;
- walidacja kodu waluty, daty i zakresu;
- nie nadpisywać ręcznie wprowadzonych kursów wykonania;
- nie wykonywać zapytań w głównym wątku.

### 6.4. Jak wybrać właściwą tabelę

- Tabela A/B: kurs średni — użyteczny do wyceny referencyjnej.
- Tabela C: kurs kupna i sprzedaży — lepszy do scenariusza przybliżonej wartości odsprzedaży / odtworzenia zakupu, ale nadal nie jest ofertą gwarantowaną dla konkretnego klienta.
- Cena złota NBP: referencyjna cena grama złota próby 1000, nie indywidualna cena sztabki/monety ani biżuterii.

Użytkownik musi móc wskazać metodę:
1. kurs średni NBP;
2. kurs kupna NBP;
3. kurs sprzedaży NBP;
4. ręczna aktualna cena rynkowa;
5. własna wycena.

Nie mieszać kursów tabeli A z kursem transakcyjnym z banku. Oryginalny kurs wykonania zostaje niezmienny.

---

## 7. Obliczanie zysku i straty

### 7.1. Rozróżnienie pojęć

- **Wartość historycznej transakcji:** ilość i waluta rzeczywiście otrzymana oraz oddana.
- **Koszt nabycia:** wartość aktywa nabytego, wraz z opłatami przypisanymi zgodnie z przyjętą metodą.
- **Wartość referencyjna obecna:** ilość aktywa pomnożona przez wybrany kurs/cenę referencyjną.
- **Niezrealizowany wynik:** różnica między aktualną wartością referencyjną a kosztem pozostałego aktywa.
- **Zrealizowany wynik:** wynik ze sprzedaży partii aktywa, wyliczony według wybranej metody kosztu.
- **Wynik netto:** wynik po uwzględnieniu prowizji i innych kosztów, o ile są znane i przypisane.
- **Wynik hipotetyczny:** różnica referencyjna; nie jest gwarantowanym zyskiem ani wynikiem możliwym do osiągnięcia w chwili wyceny.

### 7.2. Waluta bazowa

Domyślnie PLN, z możliwością wyboru waluty bazowej. Każda wycena musi być przeliczona ścieżką, której kursy istnieją w źródle. Nie zakładać bezpośredniego kursu każdej pary obcej.

Dla konwersji A → PLN → B należy jawnie wyliczyć:
- wartość aktywa A w PLN;
- wartość aktywa B w PLN;
- zastosowane kursy i daty;
- różnicę wynikającą z metodologii.

### 7.3. Przykład

Załóżmy, że użytkownik zapłacił 4 000 PLN i otrzymał 1 000 EUR, a prowizja wyniosła 10 PLN. Koszt historyczny wynosi 4 010 PLN, jeżeli prowizja jest przypisana do nabycia. Jeżeli obecny kurs referencyjny wynosi 4,35 PLN/EUR, obecna wartość referencyjna to 4 350 PLN, a hipotetyczna różnica wynosi 340 PLN. Kwota nie oznacza, że bank odkupi 1 000 EUR po 4,35 PLN.

### 7.4. Metody rozliczania partii

Przewidzieć rozszerzalny silnik:
- FIFO;
- średni ważony koszt;
- konkretna partia (specific identification).

Metoda powinna być konfigurowalna w ustawieniach i jawnie wskazana w raporcie. Nie przedstawiać obliczeń jako rozliczenia podatkowego. Przed dodaniem deklaracji podatkowych konieczna jest odrębna analiza prawna i podatkowa.

### 7.5. Wymagania matematyczne

- `BigDecimal` i kontrolowane zaokrąglenie;
- testy graniczne: kurs odwrotny, pary bez PLN, bardzo małe i duże kwoty, wiele miejsc dziesiętnych;
- oddzielne precyzje kwoty waluty, kruszcu, kursu i wyniku;
- uniknąć podwójnego ujęcia prowizji;
- przy sprzedaży uwzględnić ilość pozostałą po wcześniejszych transakcjach;
- obsłużyć zwroty i korekty;
- wykrywać ujemną pozycję aktywa i wymagać świadomej decyzji przy sprzedaży większej ilości niż posiadana;
- wynik dla niepełnej historii oznaczać jako niekompletny.

---

## 8. Interfejs użytkownika

### 8.1. Nawigacja wewnętrzna

Proponowane zakładki / sekcje:
- **Pulpit** — podsumowanie aktywów i wycen;
- **Transakcje** — lista z filtrowaniem i sortowaniem;
- **Portfel** — ilość posiadanych walut i kruszców;
- **Notowania** — NBP i ręczne kursy;
- **Import / eksport** — archiwum JSON oraz import historii bankowej;
- **Ustawienia** — waluta bazowa, metodologia, źródła, cache, prywatność.

### 8.2. Lista transakcji

Kolumny / wiersze:
- data;
- źródło;
- sprzedano (ilość + symbol);
- kupiono (ilość + symbol);
- kurs;
- status importu/weryfikacji;
- skrócone koszty.

Filtry:
- zakres dat;
- typ operacji;
- źródło;
- waluta/kruszec;
- para;
- kwoty;
- status;
- tylko operacje wymagające weryfikacji;
- możliwe duplikaty.

Sortowanie: data malejąco, kwota, waluta, kurs i źródło.

### 8.3. Szczegóły transakcji

Pokazywać:
- surowe dane importu;
- dane znormalizowane;
- wartości wydane i otrzymane;
- wyliczony kurs i kierunek;
- koszty;
- kurs NBP z dnia transakcji (jeśli istnieje);
- bieżącą wycenę i zmianę;
- źródło danych, identyfikator importu i status weryfikacji;
- historię korekt.

### 8.4. Dodawanie ręczne

Formularz powinien pozwolić określić:
- rodzaj transakcji;
- datę i czas;
- walutę/kruszec oddany i otrzymany;
- ilość;
- prowizję;
- kurs (obliczony automatycznie z kwot, z możliwością ręcznej korekty);
- źródło, instytucję i identyfikator transakcji;
- notatkę i załącznik opcjonalny.

Walidacje:
- kwoty dodatnie dla transakcji wymiany;
- różnica między stronami i opłatami powinna być wyjaśniona;
- kurs liczony w spójnym kierunku;
- data nie może być przypadkowo interpretowana w innej strefie czasowej;
- waluta musi należeć do słownika lub zostać dodana do słownika niestandardowego.

### 8.5. Dostępność i responsywność

- układ dopasowany do małych i dużych ekranów;
- brak poziomego przewijania kluczowych informacji;
- skalowanie rozmiarów tekstu;
- wspieranie trybu jasnego i ciemnego;
- etykiety tekstowe zamiast samego koloru;
- wartości dodatnie i ujemne wyróżnione kolorem, ale także symbolem i opisem;
- dostępne nazwy dla TalkBack;
- stany ładowania, braku danych, błędu, offline i braku notowań.

---

## 9. Bezpieczeństwo, prywatność i integralność

Dane finansowe są wrażliwe. Minimalne wymagania:
1. dane pozostają lokalnie, bez konta użytkownika i bez wysyłania do chmury, dopóki nie zapadnie osobna decyzja;
2. brak danych logowania do banku;
3. ewentualne szyfrowanie danych finansowych w spoczynku kluczem chronionym przez Android Keystore;
4. weryfikacja konfiguracji kopii zapasowych: istniejący manifest wskazuje `android:allowBackup="false"`; trzeba świadomie ocenić skutki migracji urządzenia i eksportu ręcznego;
5. nie logować kwot, numerów kont, identyfikatorów bankowych ani treści historii;
6. przy podglądzie i eksporcie pokazywać ostrzeżenie o poufności;
7. bezpieczny import: limit rozmiaru, liczby rekordów, długości pól, kontrola typów i odporność na złośliwe pliki;
8. pliki tymczasowe usuwać po sukcesie i błędzie;
9. eksport JSON traktować jako jawny plik zawierający dane prywatne; opcjonalnie zaoferować szyfrowanie archiwum hasłem;
10. nie dołączać prawdziwych wyciągów bankowych do repozytorium GitHub ani testów CI;
11. zapewnić możliwość usunięcia pojedynczego rekordu, partii importu i całej bazy, z odpowiednimi potwierdzeniami;
12. zachowywać ślad pochodzenia i korekt, bez niepotrzebnego gromadzenia danych osobowych.

---

## 10. Architektura modułu — propozycja

### 10.1. Pakiety / pliki

Proponowany układ, do dopasowania po pełnym przeglądzie źródeł:
- `financial/FinancialModels.kt` — domenowe modele;
- `financial/FinancialEnums.kt` — źródła, typy, statusy;
- `financial/FinancialDatabase.kt` — Room database;
- `financial/FinancialTransactionDao.kt` — DAO i zapytania;
- `financial/FinancialRepository.kt` — warstwa operacji;
- `financial/FinancialCalculator.kt` — obliczenia kursu i wyniku;
- `financial/PositionLotEngine.kt` — FIFO / średni koszt;
- `financial/import/BankImportCoordinator.kt` — orkiestracja importu;
- `financial/import/IpkOParser.kt` — parser iPKO z testami;
- `financial/import/CsvTransactionParser.kt` — parser CSV;
- `financial/import/PdfTransactionParser.kt` — parser PDF, jeśli potrzebny;
- `financial/import/ImportPreview.kt` — podgląd i korekta;
- `financial/archive/FinancialJsonCodec.kt` — serializacja;
- `financial/archive/FinancialArchiveImporter.kt` — migracje i scalanie;
- `financial/quotes/NbpApiClient.kt` — transport HTTPS;
- `financial/quotes/NbpQuoteRepository.kt` — cache i historia;
- `financial/quotes/NbpModels.kt` — DTO API;
- `financial/quotes/QuoteSyncWorker.kt` — opcjonalne odświeżanie;
- `ui/financial/PaymentLedgerScreen.kt` — ekran główny;
- `ui/financial/TransactionListScreen.kt`;
- `ui/financial/TransactionDetailsScreen.kt`;
- `ui/financial/TransactionEditorScreen.kt`;
- `ui/financial/FinancialImportScreen.kt`;
- `ui/financial/FinancialSettingsScreen.kt`;
- `ui/financial/FinancialViewModel.kt`.

Wydzielenie musi być rzeczywiste: logika parserów, danych, kursów i kalkulacji nie może mieszkać w `MainActivity.kt`.

### 10.2. Integracja z nawigacją

- dopisać unikalną pozycję w `documentDisplayOptions`;
- przypisać do sekcji „Służba finansowa”;
- dopisać specjalistyczną trasę w `DocumentRouteRegistry`;
- dodać gałąź otwierającą `PaymentLedgerScreen`;
- ustawić domyślne położenie kafelka na pulpicie;
- uzupełnić ustawienia widoczności, wyszukiwanie/katalog i testy zgodności katalogu tras;
- nie używać istniejącej trasy `receipts` ani `bank_accounts`.

### 10.3. Zależności

Najpierw sprawdzić istniejące biblioteki Gradle i ich wersje. Nie wprowadzać nowej biblioteki sieciowej, JSON, PDF ani bazy danych, jeżeli projekt już ma zgodną zależność. W szczególności:
- Room: jeżeli nie ma go w projekcie i decyzja o migracji do relacyjnej bazy jest zatwierdzona;
- Kotlin Serialization lub Moshi: preferować istniejącą bibliotekę; jeśli pozostaje `org.json`, zbudować jawny walidator i kodek;
- OkHttp/Ktor: użyć już dostępnego klienta albo minimalnego klienta zgodnego z architekturą;
- WorkManager: wyłącznie jeśli harmonogram odświeżania będzie elementem zakresu.

---

## 11. Plan realizacji i kolejność prac

### Etap 0 — doprecyzowanie wzorców i kryteriów
- Otrzymać zanonimizowane pliki wzorcowe z historii transakcji iPKO/kantoru.
- Ustalić: CSV/XLSX/PDF/HTML, nazwy kolumn, kodowanie, separator, format daty i liczb.
- Potwierdzić, czy import obejmie również zwykłą historię konta bankowego, czy na początek wyłącznie historię kantoru.
- Ustalić źródła i formaty operacji kruszcowych.
- Ustalić, czy aplikacja ma uwzględniać wiele rachunków i wielu dostawców.
- Ustalić walutę bazową, domyślną metodę kosztu i sposób obsługi opłat.
- Kryterium: przykładowy plik można opisać kompletną tabelą mapowania bez ujawniania danych wrażliwych.

### Etap 1 — domena i katalog nawigacji
- Dodać kafelek `payment_transactions` domyślnie do pulpitu.
- Dodać trasę specjalistyczną.
- Zbudować szkielet ekranu.
- Utworzyć modele Asset, Transaction, Fee, Source i ParseStatus.
- Dodać testy identyfikatorów, sekcji, domyślnej widoczności i nawigacji.
- Kryterium: kafelek otwiera właściwy ekran i nie narusza żadnych dotychczasowych tras.

### Etap 2 — lokalna baza i ręczne operacje
- Zaimplementować trwały magazyn i migracje.
- Dodać dodawanie, edycję, usuwanie i szczegóły transakcji.
- Dodać słownik walut oraz złota z próbą i jednostkami.
- Zaimplementować walidację kwot i obliczanie kursu.
- Kryterium: operacje ręczne są trwałe po zamknięciu aplikacji i zgodne matematycznie.

### Etap 3 — import iPKO
- Opracować parser na podstawie rzeczywistego wzorca.
- Dodać obsługę kodowania i separatorów.
- Dodać import przez SAF.
- Dodać podgląd, raport, korektę, duplikaty i zatwierdzanie.
- Dodać testy parsera dla wersji wzorca oraz testy negatywne.
- Kryterium: powtórny import tego samego pliku nie tworzy duplikatów; żaden błędny wiersz nie jest zapisywany jako zweryfikowany.

### Etap 4 — JSON
- Wdrożyć kopertę archiwum z `schemaVersion`.
- Eksport całości i zakresu.
- Import z podglądem, walidacją i raportem.
- Scalanie i obsługa konfliktów.
- Migracje wersji.
- Kryterium: eksport → import → eksport zachowuje semantyczną zgodność transakcji, kwot, kursów, źródeł i identyfikatorów.

### Etap 5 — NBP
- Adapter tabel A/B/C i cen złota.
- Obsługa okresów do 93 dni przez segmentację.
- Cache i historia.
- Obsługa 404 / weekendów / świąt / offline.
- Kryterium: dane mają datę i typ tabeli, a obliczenia nie korzystają z kursu innego niż jawnie wybrany.

### Etap 6 — silnik wyceny i wyniku
- Przeliczanie wartości na PLN lub walutę bazową.
- Wycena walut i złota.
- FIFO i średni koszt, jeśli decyzja o metodach została podjęta.
- Wynik zrealizowany/niezrealizowany/brak danych.
- Prowizje i koszty.
- Kryterium: testy przykładów referencyjnych, ułamków i odwróconych par przechodzą.

### Etap 7 — rozbudowany interfejs i raporty
- Pulpit, lista, filtry, szczegóły, portfel, wykresy.
- Eksport raportu JSON, ewentualnie CSV/PDF jako dodatkowy zakres.
- Ustawienia waluty bazowej i metodologii.
- Kryterium: użyteczność i poprawność na małych ekranach, trybie ciemnym i przy dużej liczbie rekordów.

### Etap 8 — bezpieczeństwo i optymalizacja
- Szyfrowanie danych / klucze, logi, pliki tymczasowe, eksport.
- Testy importu z plikami granicznymi i uszkodzonymi.
- Testy wydajności 1 tys., 10 tys. i 100 tys. transakcji.
- Testy zachowania offline i przerwania operacji.
- Kryterium: brak zawieszeń UI, utraty danych lub niekontrolowanego ujawnienia danych.

### Etap 9 — integracja i wydanie
- Pełny zestaw testów JVM i Android instrumented.
- Testy przepływów UI.
- Kompilacja debug i release według obecnej konfiguracji.
- Kontrola REUSE i zależności.
- Weryfikacja APK na emulatorze i urządzeniu.
- PR pozostaje otwarty do zakończenia przeglądu, CI i testów; nie scalać niezweryfikowanych zmian.
- Kryterium: APK z testowanej rewizji, kompletna dokumentacja i brak regresji istniejących funkcji rachunków, vCard, dokumentów oraz kafelków.

---

## 12. Plan testów — kryteria jakości

### 12.1. Testy modelu i walidacji
- kwoty zerowe, ujemne, puste i skrajnie duże;
- waluty tej samej i różnej;
- kruszec w gramach i uncjach;
- próba i masa czysta;
- brak kursu NBP w dniu transakcji;
- brak kursu dla waluty;
- daty niepoprawne i strefy czasowe;
- transakcja odwrócona / zwrot / transfer własny;
- prowizja w walucie wydanej, otrzymanej i trzeciej.

### 12.2. Testy parsera
- UTF-8 i Windows-1250;
- separator CSV: przecinek, średnik, tabulator;
- separator dziesiętny kropka i przecinek;
- kwoty z odstępami i separatorami tysięcy;
- daty dd.MM.yyyy, yyyy-MM-dd i warianty rzeczywiście występujące w pliku;
- zmieniona kolejność kolumn;
- nieznany nagłówek;
- brak kolumny;
- pusty wiersz, duplikat, uszkodzony wiersz;
- plik pusty, nieczytelny, przekraczający limit i celowo spreparowany;
- poprawne rozpoznawanie kierunku „Kupiono / Sprzedano”.

### 12.3. Testy NBP
- tabela A, B, C;
- tabela bez konkretnej waluty;
- 404 w dniu wolnym;
- zakres 93 dni oraz zakres dłuższy segmentowany;
- brak Internetu;
- timeout i ponowienie;
- błędny JSON;
- niezgodna data efektywna;
- aktualizacja zachowująca poprzedni cache.

### 12.4. Testy finansowe
- dokładna konwersja PLN → EUR;
- EUR → PLN;
- EUR → USD;
- kurs odwrotny;
- opłaty w tej samej i innej walucie;
- częściowa sprzedaż partii;
- kilka partii i FIFO;
- średnia ważona;
- zrealizowany i niezrealizowany wynik;
- pozycja bez kompletnej historii;
- złoto różnych prób.

### 12.5. Testy integracyjne / regresyjne
- kafelek domyślnie widoczny;
- ukrycie i przywrócenie kafelka;
- wejście i powrót;
- zachowanie innych kafelków i kolejności;
- brak wpływu na `receipts`, PDF i katalog rok/miesiąc;
- brak wpływu na rachunki bankowe Rodu/podmiotów;
- archiwum ogólne vCard zachowuje dotychczasową zgodność;
- brak blokowania głównego wątku;
- trwałość danych po odtworzeniu procesu.

---

## 13. Ryzyka i ich ograniczanie

| ID | Ryzyko | Waga | Ograniczenie |
|---|---|---:|---|
| R-01 | Pomylenie rachunków sprzedaży z rejestrem transakcji | Wysoka | Oddzielne ID, trasa, modele, magazyn i ekran |
| R-02 | Zmiana formatu eksportu PKO | Wysoka | Parsery wersjonowane i testy wzorców |
| R-03 | Błędny kierunek pary walutowej | Krytyczna | Normalizacja i testy kursu odwrotnego |
| R-04 | Podwójne ujęcie prowizji | Wysoka | Jawny model Fee i testy obliczeń |
| R-05 | Uznanie kursu średniego NBP za rzeczywistą cenę wykonania | Wysoka | Widoczny typ kursu i etykieta „wycena referencyjna” |
| R-06 | Brak danych NBP w weekend / święto | Średnia | Cache i ostatnie dostępne notowanie |
| R-07 | Duplikaty przy ponownym imporcie | Wysoka | Klucz dostawcy i odcisk z ręcznym rozstrzyganiem |
| R-08 | Utrata danych przy scalaniu JSON | Krytyczna | Kopia, podgląd, atomowy zapis i cofnięcie importu |
| R-09 | Ujawnienie historii bankowej w logach/backupie | Krytyczna | Minimalizacja, szyfrowanie, redakcja logów, kontrola backupu |
| R-10 | Błędy zaokrągleń Double/Float | Krytyczna | BigDecimal, jawne skale i rounding |
| R-11 | Przeciążenie MainActivity | Wysoka | Wydzielone pakiety, ViewModel i repozytorium |
| R-12 | Niedopasowanie ceny złota NBP do próby/produktu | Wysoka | Przeliczenie czystości i osobna wycena produktu |
| R-13 | Niekompletna historia pozycji | Wysoka | Status niekompletności i brak pozornej dokładności |
| R-14 | Nieznany plik / złośliwy import | Wysoka | Limity, walidacja, parser bezpieczny i testy negatywne |

---

## 14. Wymagania akceptacyjne końcowe

Funkcja będzie gotowa do uznania za wdrożoną dopiero, gdy:
1. kafelek „Rachunki płatnicze” jest domyślnie widoczny i ma działającą, odrębną trasę;
2. użytkownik może dodać, edytować, usunąć i przeglądać transakcję walutową oraz złotą;
3. rejestr zachowuje dokładne ilości, waluty, kursy, opłaty, źródła i daty;
4. import iPKO działa na rzeczywistym, zanonimizowanym formacie wzorcowym;
5. parser nie zatwierdza niejednoznacznych wierszy bez weryfikacji;
6. powtórny import nie mnoży tych samych operacji;
7. JSON ma wersjonowany schemat i bezpieczny cykl import/eksport;
8. notowania NBP A/B/C i ceny złota pobierają się z HTTPS;
9. brak kursu i brak połączenia są obsługiwane czytelnie;
10. obliczenia używają BigDecimal i jawnej metodologii;
11. wyniki referencyjne są opisane jako hipotetyczne i zawierają źródło oraz datę;
12. prowizje, zwroty i transfery własne nie zniekształcają bilansu;
13. baza i eksport przechodzą testy bezpieczeństwa oraz migracji;
14. dotychczasowy moduł „Rachunki” działa identycznie jak przed zmianą;
15. testy JVM, instrumentacyjne, UI i kompilacja przechodzą;
16. wynikowa APK pochodzi z tej samej zweryfikowanej rewizji, która zostanie przedstawiona do scalenia.

---

## 15. Decyzje wymagające materiału od użytkownika

Nie blokują one przygotowania architektury, ale blokują uznanie parsera za gotowy:
- rzeczywisty, zanonimizowany eksport historii kantoru iPKO;
- informacja, czy importować także historię zwykłego rachunku bankowego, czy na początek tylko operacje kantorowe;
- przykładowy eksport historii transakcji kruszcowych, jeśli pochodzi z konkretnego sprzedawcy;
- wybór waluty bazowej (propozycja domyślna: PLN);
- preferowana metoda kosztu: FIFO czy średni koszt (propozycja: ustawienie z domyślnym FIFO dla ewidencji partii, bez deklarowania skutku podatkowego);
- czy do ewidencji kruszców dodać od razu srebro, platynę i pallad, czy zacząć od złota.

**Ważne:** dane z iPKO należy przesłać po usunięciu numerów rachunków, nazwisk, adresów, sald niezwiązanych z transakcją, identyfikatorów klienta i wszelkich danych logowania. Kwoty i kursy mogą pozostać, o ile użytkownik wyraża zgodę na użycie ich wyłącznie do przygotowania parsera.

---

## 16. Źródła i dokumentacja odniesienia

1. **NBP Web API** — oficjalna dokumentacja endpointów kursów A/B/C, cen złota, formatu JSON, danych archiwalnych, ograniczenia 93 dni i wymogu HTTPS: https://api.nbp.pl/
2. **NBP — Kursy** — opis aktualnych i archiwalnych tabel kursów oraz archiwum cen złota: https://nbp.pl/statystyka-i-sprawozdawczosc/kursy/
3. **NBP — Informacja o terminach publikacji kursów walut** — częstotliwość aktualizacji tabel A, B i C: https://nbp.pl/statystyka-i-sprawozdawczosc/kursy/informacja-o-terminach-publikacji-kursow-walut/
4. **PKO Bank Polski — Instrukcja użytkownika kantoru PKO Banku Polskiego SA, wersja 1.8** — historia transakcji iPKO/IKO, filtry, lista transakcji i szczegóły: https://www.pkobp.pl/media_files/adf632c7-847b-47ce-953c-05c752377401.pdf
5. **Repozytorium — istniejący audyt architektury:** `obywatel-android/AUDYT_ARCHITEKTURY.md`.
6. **Repozytorium — struktura blokowa:** `obywatel-android/STRUKTURA_BLOKOWA.md`.
7. **Repozytorium — implementacja rachunków:** `obywatel-android/app/src/main/java/pl/gov/obywatel/android/ReceiptDocuments.kt`.
8. **Repozytorium — rejestr tras:** `obywatel-android/app/src/main/java/pl/gov/obywatel/android/DocumentRouteRegistry.kt`.

---

## 17. Lista kontrolna dalszych prac

- [ ] Pozyskać i opisać wzorcowy plik iPKO.
- [ ] Ustalić zakres historii bankowej i kantorowej.
- [ ] Dodać kafelek `payment_transactions` jako domyślny.
- [ ] Dodać trasę specjalistyczną.
- [ ] Wydzielić domenę finansową poza `MainActivity.kt`.
- [ ] Zaimplementować model walut, kruszców, transakcji i opłat.
- [ ] Zaimplementować lokalny magazyn i migracje.
- [ ] Dodać formularz ręczny i listę transakcji.
- [ ] Zbudować parser iPKO na podstawie wzorca.
- [ ] Dodać podgląd, weryfikację i duplikaty.
- [ ] Wdrożyć wersjonowany JSON.
- [ ] Dodać klienta NBP A/B/C i złota.
- [ ] Zaimplementować wycenę i wynik hipotetyczny.
- [ ] Dodać portfel i raporty.
- [ ] Przeprowadzić testy matematyczne, parsera, migracji, UI i bezpieczeństwa.
- [ ] Skompilować APK i wykonać test regresji istniejących rachunków.

**Zasada kontynuacji:** każdy etap kończyć testami i czytelnym podsumowaniem w PR. Nie scalać zmian częściowych bez kontroli regresji, a dane rzeczywistych operacji finansowych pozostawić poza repozytorium.


---

## 18. Rozszerzenie zakresu: rejestr darowizn gotówkowych

### 18.1. Cel i miejsce funkcji

Moduł „Rachunki płatnicze” powinien obejmować również ewidencję **darowizn otrzymanych w gotówce**. Darowizny są odrębnym rodzajem zdarzenia finansowego: nie są wymianą walutową, zakupem aktywa ani przychodem ze sprzedaży. Powinny być przechowywane w tej samej domenie finansowej, ale w osobnym typie rekordu i z własnymi regułami walidacji oraz raportowania.

W interfejsie należy dodać sekcję **Darowizny gotówkowe**, dostępną z pulpitu „Rachunki płatnicze” oraz z listy operacji. Na ekranie głównym może być widoczne podsumowanie liczby darowizn i ich łącznej wartości w wybranym okresie.

### 18.2. Obowiązkowe i zalecane dane darowizny

Każdy rekord darowizny powinien obejmować:

**Pola wymagane do zapisania rekordu:**
- unikalny identyfikator UUID;
- datę otrzymania darowizny;
- kwotę darowizny;
- walutę gotówki, np. PLN, EUR lub USD;
- **pełną nazwę organizacji, na rzecz której przekazano darowiznę**;
- **cel darowizny** — opis przeznaczenia środków, np. pomoc rodzinom, zakup sprzętu medycznego, edukacja dzieci, działalność statutowa, konkretny projekt lub zbiórka;
- status rekordu, np. roboczy / potwierdzony / skorygowany.

Nazwa organizacji i cel darowizny są polami obowiązkowymi. Aplikacja nie powinna pozwalać zatwierdzić darowizny bez ich uzupełnienia. Jeżeli darowizna jest anonimowa wobec obdarowanego albo dane darczyńcy nie są znane, nie należy blokować zapisu z tego powodu.

**Pola dodatkowe:**
- imię i nazwisko darczyńcy — wyłącznie gdy użytkownik ma uzasadnioną potrzebę i podstawę do ich zapisania;
- oznaczenie „darowizna anonimowa”;
- nazwa zbiórki, kampanii lub projektu;
- osoba przyjmująca gotówkę / kasjer;
- miejsce przyjęcia środków;
- numer pokwitowania lub dokumentu potwierdzającego;
- opis lub uwagi;
- załącznik, np. zdjęcie pokwitowania, z zachowaniem zasad ochrony danych;
- informacja, czy darowizna została wpłacona do kasy, na rachunek bankowy, czy pozostaje w przechowaniu;
- data i kwota późniejszego przekazania środków organizacji, jeśli ewidencja ma obejmować także rozliczenie zbiórki;
- źródło wprowadzenia rekordu: ręcznie / import JSON / inny zatwierdzony import.

Należy odróżnić **organizację będącą beneficjentem darowizny** od podmiotu lub osoby, która fizycznie przyjęła gotówkę. W razie potrzeby powinny to być dwa osobne pola.

### 18.3. Model danych

Rekomendowany jest osobny model domenowy, np. `CashDonation`, zamiast dopisywania niejednoznacznych pól do modelu wymiany walutowej.

Przykładowy model logiczny:

```kotlin
data class CashDonation(
    val id: String,
    val receivedAt: Instant,
    val amount: BigDecimal,
    val currencyCode: String,
    val beneficiaryOrganizationName: String,
    val purpose: String,
    val donorName: String?,
    val isAnonymous: Boolean,
    val campaignName: String?,
    val collectedBy: String?,
    val receiptNumber: String?,
    val custodyStatus: DonationCustodyStatus,
    val transferredAt: Instant?,
    val transferredAmount: BigDecimal?,
    val transferredCurrencyCode: String?,
    val note: String?,
    val source: DonationRecordSource,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

Model jest propozycją i powinien zostać dostosowany do faktycznej wersji Kotlin oraz przyjętej architektury bazy danych. Kwoty muszą być przechowywane jako wartości dziesiętne o jawnej precyzji; nie stosować `Float` ani `Double` do obliczeń finansowych.

### 18.4. Formularz i przebieg rejestracji

Formularz „Dodaj darowiznę gotówkową” powinien zawierać:
1. datę otrzymania;
2. kwotę;
3. wybór waluty;
4. nazwę organizacji-beneficjenta;
5. cel darowizny;
6. opcjonalne dane darczyńcy albo oznaczenie anonimowości;
7. opcjonalną nazwę zbiórki/projektu;
8. osobę przyjmującą środki;
9. numer pokwitowania;
10. informację o miejscu przechowywania lub przekazaniu gotówki;
11. uwagi i opcjonalny załącznik.

Walidacja:
- kwota musi być większa od zera;
- waluta musi być rozpoznana przez słownik aktywów;
- nazwa organizacji po usunięciu zbędnych spacji nie może być pusta;
- cel darowizny nie może być pusty;
- data musi być poprawna;
- kwota przekazania organizacji nie może przekraczać kwoty przyjętej, chyba że użytkownik rejestruje osobno dodatkowe środki lub korektę;
- nie wolno automatycznie oznaczać darowizny jako przekazanej organizacji wyłącznie na podstawie jej przyjęcia.

Po zapisaniu aplikacja powinna wyświetlić szczegóły darowizny i umożliwić wygenerowanie lub dołączenie pokwitowania, jeżeli zakres dokumentów i wymogi prawne zostaną odrębnie ustalone. Sam wpis w aplikacji nie powinien być przedstawiany jako urzędowe potwierdzenie darowizny.

### 18.5. Ewidencja przepływu gotówki i rozliczenie zbiórki

Warto rozdzielić trzy zdarzenia:
- **otrzymanie darowizny** — powstaje zapis wpływu gotówkowego;
- **wpłata gotówki do kasy lub na rachunek** — jest transferem środków, a nie kolejną darowizną;
- **przekazanie środków organizacji** — jest rozchodem / rozliczeniem z beneficjentem, powiązanym z jedną lub wieloma darowiznami.

Dzięki temu ta sama kwota nie zostanie podwójnie policzona jako darowizna przyjęta i ponownie jako darowizna przekazana. Przy zbiórkach obejmujących wiele wpłat aplikacja powinna pozwolić filtrować rekordy według organizacji, celu, kampanii, okresu i statusu rozliczenia.

### 18.6. Raporty i zestawienia

Dodać następujące zestawienia:
- suma darowizn według organizacji;
- suma darowizn według celu;
- suma według kampanii / projektu;
- suma według waluty;
- zestawienie dzienne, miesięczne i roczne;
- darowizny przyjęte, lecz jeszcze nieprzekazane;
- darowizny przekazane i pozostałe środki;
- lista rekordów bez numeru pokwitowania lub wymagających uzupełnienia.

Kwoty w różnych walutach należy raportować osobno, chyba że użytkownik wybierze przeliczenie na walutę bazową. Przeliczenie ma być oznaczone jako referencyjne i zawierać kurs, datę oraz źródło. Nie wolno sumować np. 100 PLN i 100 EUR jako jednej kwoty bez przeliczenia.

### 18.7. Import i eksport JSON

Wersjonowany format archiwum powinien otrzymać odrębną kolekcję, np. `cashDonations`, bez zmiany semantyki istniejących pól `transactions`.

Przykładowy rekord:

```json
{
  "id": "uuid",
  "receivedAt": "2026-11-14T12:30:00Z",
  "amount": "250.00",
  "currencyCode": "PLN",
  "beneficiaryOrganizationName": "Fundacja Przykładowa",
  "purpose": "Zakup materiałów edukacyjnych dla dzieci",
  "donorName": null,
  "isAnonymous": true,
  "campaignName": "Edukacja dla rodzin",
  "receiptNumber": "D-2026-001",
  "custodyStatus": "HELD"
}
```

Powyższe dane są wyłącznie przykładem struktury, a nie rzeczywistym rekordem. Import musi sprawdzać obecność nazwy organizacji i celu, poprawność kwoty, waluty i daty. Duplikaty należy rozpoznawać przede wszystkim po UUID, a przy imporcie z zewnętrznego źródła dodatkowo po numerze pokwitowania i zestawie danych pomocniczych; nie scalać automatycznie dwóch darowizn o takiej samej kwocie.

Eksport powinien umożliwiać:
- całą ewidencję;
- wybrany zakres dat;
- wybraną organizację;
- wybrany cel lub kampanię;
- same darowizny albo pełne archiwum finansowe.

### 18.8. Prywatność, bezpieczeństwo i rzetelność ewidencji

Dane darczyńców mogą stanowić dane osobowe. Ich gromadzenie powinno być opcjonalne, ograniczone do niezbędnego zakresu i uzasadnione celem ewidencji. Nie należy wymagać danych darczyńcy, jeśli ewidencja ma służyć wyłącznie wykazaniu kwoty, organizacji i celu.

Należy zapewnić:
- ograniczenie dostępu do lokalnej ewidencji;
- brak danych darczyńców i kwot w logach diagnostycznych;
- bezpieczne załączniki i eksport;
- historię korekt, bez cichego nadpisywania istotnych danych;
- możliwość poprawienia błędnego wpisu z odnotowaniem zmiany;
- możliwość usunięcia danych zgodnie z przyjętą polityką retencji;
- wyraźne odróżnienie danych wpisanych przez użytkownika od danych potwierdzonych dokumentem.

Audyt nie przesądza o obowiązkach prawnych, podatkowych ani rachunkowych konkretnej organizacji. Przed wdrożeniem funkcji jako formalnej księgi kasowej lub systemu pokwitowań należy osobno zweryfikować właściwe przepisy, status organizacji, sposób prowadzenia zbiórki i wymagane dokumenty. Funkcja w tej aplikacji jest na tym etapie ewidencją pomocniczą, a nie certyfikowanym systemem księgowym.

### 18.9. Testy i kryteria akceptacyjne darowizn

Testy jednostkowe:
- brak nazwy organizacji;
- brak celu darowizny;
- kwota zerowa, ujemna i zbyt precyzyjna;
- nieznany kod waluty;
- darowizna anonimowa bez danych darczyńcy;
- darowizna z opcjonalnym numerem pokwitowania;
- dwie darowizny o tej samej kwocie i dacie, ale różnych identyfikatorach;
- częściowe i pełne przekazanie zebranej kwoty;
- przekazanie większej kwoty niż stan środków;
- raporty wielowalutowe;
- import/eksport JSON z zachowaniem organizacji i celu.

Testy UI i integracyjne:
- sekcja darowizn dostępna z kafelka;
- zapis wymaga nazwy organizacji i celu;
- można zapisać darowiznę anonimową;
- edycja i korekta pozostawiają ślad;
- import JSON nie gubi danych organizacji, celu, kwoty ani waluty;
- zestawienia nie liczą transferu do organizacji jako drugiej darowizny;
- funkcja nie wpływa na istniejące rachunki sprzedaży ani transakcje wymiany.

**Dodatkowe kryteria akceptacyjne:**
1. Użytkownik może zarejestrować darowiznę gotówkową w wybranej walucie.
2. Nazwa organizacji i cel darowizny są obowiązkowe.
3. Dane darczyńcy pozostają opcjonalne.
4. Można oznaczyć darowiznę jako anonimową.
5. Można odnotować pokwitowanie i późniejsze przekazanie środków.
6. Raporty grupują darowizny według organizacji i celu.
7. Dane darowizn są eksportowane i importowane w JSON.
8. Operacje przyjęcia, wpłaty i przekazania gotówki są rozróżnione i nie powodują podwójnego liczenia.

### 18.10. Aktualizacja planu realizacji

Zakres darowizn należy włączyć do planu jako osobny podetap:
- **Etap 2A — ewidencja darowizn gotówkowych:** model `CashDonation`, formularz, walidacja, lista, szczegóły, status przechowywania i przekazania;
- **Etap 4A — archiwum JSON darowizn:** eksport/import kolekcji `cashDonations`, walidacja i scalanie;
- **Etap 7A — raporty darowizn:** zestawienia według organizacji, celu, kampanii, waluty i okresu;
- **Etap 8A — testy prywatności i integralności:** anonimowość darczyńcy, brak wycieku danych do logów i brak podwójnego księgowania.

### 18.11. Aktualizacja listy kontrolnej

- [ ] Dodać osobny model `CashDonation`.
- [ ] Dodać sekcję „Darowizny gotówkowe” w module „Rachunki płatnicze”.
- [ ] Wymagać nazwy organizacji-beneficjenta i celu darowizny.
- [ ] Umożliwić ewidencję anonimową bez danych darczyńcy.
- [ ] Dodać datę, kwotę, walutę, pokwitowanie i status przechowywania środków.
- [ ] Rozróżnić przyjęcie darowizny, wpłatę gotówki i przekazanie środków.
- [ ] Dodać raporty według organizacji i celu.
- [ ] Dodać kolekcję `cashDonations` do wersjonowanego JSON.
- [ ] Dodać testy walidacji, importu/eksportu, raportowania i prywatności.


## 19. Dziennik wykonania — etap 1 (14 listopada 2026 r.)

### 19.1. Zakres wykonany w repozytorium

Zmiany wykonano na gałęzi `feature/payment-ledger-stage-1`, bez bezpośredniego modyfikowania `main`.

1. **Kafelek katalogu**
   - dodano identyfikator `payment_transactions`;
   - etykieta: „Rachunki płatnicze”;
   - ikona wykorzystuje istniejący zasób portfela;
   - kafelek jest domyślnie umieszczany na pulpicie;
   - grupa katalogu: „Służba finansowa”.

2. **Nawigacja**
   - dodano trasę typu `SPECIALIZED` w `DocumentRouteRegistry`;
   - dodano osobny stan ekranu, obsługę trasy i powrotu systemowego w `MainActivity.kt`;
   - utworzono `PaymentLedgerScreen.kt` jako niezależny szkielet ekranu;
   - dotychczasowa trasa `receipts` i ekran `ReceiptDocumentsScreen` pozostały odrębne.

3. **Modele domenowe**
   - dodano `financial/FinancialModels.kt`;
   - modele obejmują `Asset`, `Fee`, `FinancialTransaction` i `CashDonation`;
   - dodano klasyfikacje aktywów, typów transakcji, źródeł danych, statusów parsowania i statusów przechowywania darowizn;
   - darowizna pozostaje odrębnym zdarzeniem, a nazwa organizacji-beneficjenta i cel są obowiązkowe;
   - kwoty i obliczenia używają `BigDecimal`, bez `Float` i `Double`.

4. **Walidacja i obliczenia**
   - dodano kontrolę kodu waluty, próby kruszcu, dodatnich ilości, spójności par aktywo/kwota i nieujemnej prowizji;
   - dodano walidację darowizny, w tym limitu kwoty przekazania względem kwoty przyjętej;
   - dodano obliczanie jednostkowego kursu wymiany i procentowej zmiany wartości z jawnym kontekstem dziesiętnym.

5. **Testy**
   - rozszerzono `DocumentRouteRegistryTest` o trasę i domyślną widoczność nowego kafelka;
   - dodano `FinancialModelsTest` obejmujący aktywa, transakcje, prowizje, darowizny i obliczenia dziesiętne.

### 19.2. Pliki zmienione lub dodane

- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/MainActivity.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/DocumentRouteRegistry.kt`
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/PaymentLedgerScreen.kt` — nowy
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/FinancialModels.kt` — nowy
- `obywatel-android/app/src/test/java/pl/gov/obywatel/android/DocumentRouteRegistryTest.kt`
- `obywatel-android/app/src/test/java/pl/gov/obywatel/android/FinancialModelsTest.kt` — nowy

### 19.3. Weryfikacja i ograniczenia

- Po zapisie ponownie pobrano z GitHub wszystkie sześć plików i potwierdzono obecność zmian.
- Sprawdzono tekstowo: wpis katalogu, domyślne położenie kafelka, trasę specjalistyczną, stan ekranu, obsługę powrotu oraz osobny ekran modułu.
- Sprawdzono, że istniejący identyfikator `receipts` i dotychczasowy ekran rachunków nie zostały zastąpione nową funkcją.
- Testy jednostkowe zostały dodane, ale samo ich zapisanie nie oznacza, że zostały uruchomione. Wynik kompilacji i testów wymaga potwierdzenia w GitHub Actions dla PR tej gałęzi.
- Na tym etapie nie ma jeszcze trwałej bazy danych, formularzy transakcji, importu iPKO, archiwum JSON ani integracji NBP. Ekran jest szkieletem nawigacyjnym, a nie ukończoną funkcją użytkową.
- Parser iPKO nadal wymaga rzeczywistego, zanonimizowanego pliku wzorcowego.

### 19.4. Status etapów po tej zmianie

| Etap | Status |
|---|---|
| Etap 0 — doprecyzowanie wzorców | Częściowo; oczekuje na plik iPKO oraz decyzje dotyczące waluty bazowej i metod kosztu |
| Etap 1 — domena i katalog nawigacji | Zaimplementowany w gałęzi PR; oczekuje na CI i przegląd |
| Etap 2 — trwały magazyn i ręczne operacje | Nierozpoczęty |
| Etap 2A — rejestr darowizn | Modele i walidacja przygotowane; magazyn i formularz nierozpoczęte |
| Etap 3 — import iPKO | Oczekuje na wzorzec pliku |
| Etap 4 — archiwum JSON | Nierozpoczęty |
| Etap 4A — JSON darowizn | Nierozpoczęty |
| Etap 5 — notowania NBP | Nierozpoczęty |
| Etap 6 — wycena portfela | Tylko podstawowe funkcje matematyczne; silnik kosztu i wyceny nierozpoczęty |
| Etap 7 — pełny interfejs i raporty | Szkielet ekranu; reszta nierozpoczęta |
| Etap 7A — raporty darowizn | Nierozpoczęty |
| Etap 8 — bezpieczeństwo i optymalizacja | Nierozpoczęty |
| Etap 9 — integracja i wydanie | Nierozpoczęty |

### 19.5. Następny krok

Po pozytywnym CI należy przejść do etapu 2: trwałego lokalnego magazynu i ręcznego dodawania/edycji transakcji oraz darowizn. Przed wyborem Room trzeba ponownie sprawdzić aktualne zależności i uzgodnić migracje; nie dodawać biblioteki ani migracji schematu bez uzasadnienia. Import iPKO pozostaje osobnym etapem zależnym od wzorca.


## 20. Kontrola regresyjna i korekta etapu 1

### 20.1. Wynik pierwszego uruchomienia CI

Pierwsze pełne uruchomienie dla rewizji etapu 1 potwierdziło kompilację kodu produkcyjnego i testowego Kotlin, ale zadanie `testDebugUnitTest` zakończyło się niepowodzeniem: 112 testów uruchomiono, 4 testy `DocumentGroupCatalogTest` zakończyły się błędem `IllegalArgumentException`. Dotyczyły one założeń katalogu dokumentów: każdy kafelek musi mieć jawny schemat pól albo być oznaczony jako obsługiwany przez dedykowany edytor.

### 20.2. Przyczyna i korekta

Nowy kafelek `payment_transactions` otwiera własny ekran `PaymentLedgerScreen`, a więc korzysta z dedykowanego ekranu, a nie ze standardowego edytora pól dokumentu. W pierwszej implementacji identyfikator nie został dopisany do dwóch list wyjątków w `DocumentEditorSchemas.kt`. Powodowało to traktowanie go jak nieznanego dokumentu przez testy katalogu.

Korekta:
- dodano `payment_transactions` do listy dokumentów bez standardowego schematu pól;
- dodano ten sam identyfikator do listy dokumentów obsługiwanych przez dedykowany edytor;
- zachowano istniejące definicje pól i zachowanie pozostałych dokumentów.

### 20.3. Status ponownej weryfikacji

- Korektę zapisano na tej samej gałęzi PR w commicie `ee625ce81f9e6b35df87cb9483f90880443b10f5`.
- Ponownie pobrano `DocumentEditorSchemas.kt` i potwierdzono dwa wystąpienia identyfikatora w odpowiednich listach.
- Oczekuje się na świeże uruchomienie GitHub Actions dla nowego SHA; do czasu jego zakończenia nie można uznać testów regresyjnych ani kompilacji APK za zaliczone.
- Nie zmieniano istniejącego modułu `receipts`.

### 20.4. Decyzja o kolejności etapów

Etap 2 (trwały magazyn i formularze) rozpocznie się po zielonym wyniku testów i kompilacji etapu 1. Nie należy budować warstwy danych na bazie nawigacji, która nie przeszła regresji katalogu dokumentów.


## 21. Wzorce danych iPKO — analiza czterech załączników (14 listopada 2026 r.)

### 21.1. Zakres materiału źródłowego

Przeanalizowano cztery dostarczone reprezentacje historii operacji:
- CSV — 3 wiersze operacji;
- XLS — 3 wiersze operacji;
- XML — 5 elementów `operation`;
- PDF — 5 zapisów tabeli historii rachunku.

CSV i XLS zawierają te same trzy zapisy w walucie CHF. XML i PDF zawierają te same pięć zapisów w walucie USD. Nie wolno zakładać, że wszystkie cztery pliki są identycznymi eksportami: obejmują różne zestawy operacji/rachunków/walut.

Łącznie wzorce reprezentują **8 odrębnych zapisów księgowych**:
- CHF: uznania 617,42 (08.11.2026), 622,29 (03.11.2026) i 6 183,20 (04.03.2026);
- USD: uznania 749,33 i 751,30 (12.11.2026), 825,85 (20.12.2025) i 829,03 (17.12.2025), a także obciążenie 8 000,00 (04.03.2026).

CSV/XLS stanowią duplikujące się reprezentacje tych samych trzech zapisów. XML/PDF stanowią duplikujące się reprezentacje tych samych pięciu zapisów. Własna referencja zleceniodawcy `90124777` występuje po obu stronach wymiany: jako obciążenie 8 000,00 USD oraz uznanie 6 183,20 CHF. Są to dwa odrębne zapisy księgowe jednej operacji wymiany, a nie duplikat.

### 21.2. Wnioski dla importu

1. Import musi rozdzielać **identyfikator zapisu księgowego** od **identyfikatora operacji nadrzędnej**.
2. Ten sam zapis z CSV i XLS albo XML i PDF musi otrzymać identyczny identyfikator niezależnie od formatu źródłowego.
3. Przeciwne nogi wymiany z tym samym numerem FX muszą pozostać osobnymi zapisami (różne waluty, kwoty i kierunki), lecz być powiązane jednym identyfikatorem operacji.
4. Identyfikacja zapisu uwzględnia zakres rachunku, datę operacji, walutę, kwotę ze znakiem, kierunek, typ operacji i referencję bankową. Kwoty są normalizowane dziesiętnie, bez konwersji do typów zmiennoprzecinkowych.
5. Gdy referencja bankowa nie występuje, przewidziany jest klucz zastępczy oparty na znormalizowanym opisie. Takie dopasowanie należy traktować ostrożniej i umożliwić oznaczenie konfliktu do ręcznej kontroli.
6. Pełne numery rachunków, nazwiska, adresy i treści prywatnych wyciągów nie są zapisywane w repozytorium ani w logach CI. Zakres rachunku w identyfikatorze powinien być lokalnym pseudonimem/skrótowym identyfikatorem, a nie pełnym numerem.

### 21.3. Ograniczenia wzorca

- Pliki nie dostarczają kompletnego schematu wszystkich możliwych wariantów eksportu iPKO.
- XLS jest arkuszem binarnym; w środowisku analitycznym został odczytany przez konwersję do CSV. Aplikacyjny parser XLS nie został jeszcze zaimplementowany.
- PDF jest dokumentem prezentacyjnym; ekstrakcja tekstu nie jest równoważna stabilnemu API. Parser PDF/OCR wymaga osobnego etapu i testów na kolejnych wzorcach.
- Nie należy importować podsumowań z pierwszej strony PDF jako dodatkowych transakcji — są agregatami tych samych operacji.
- Import nie obejmuje logowania do banku ani pobierania danych z konta.

## 22. Etap 2B — identyfikacja i idempotencja importu

### 22.1. Implementacja

Dodano:
- `financial/BankImportDeduplicator.kt` — model pojedynczego zapisu wyciągu, walidację, stabilny identyfikator zapisu oparty na SHA-256 oraz identyfikator operacji nadrzędnej oparty na referencji FX;
- `financial/BankImportDeduplicatorTest.kt` — testy identyczności między formatami, deduplikacji wewnątrz jednej paczki i względem istniejącego rejestru, rozdzielenia nóg wymiany oraz klucza zastępczego bez referencji.

Mechanizm `BankImportDeduplicator.classify` klasyfikuje każdy rekord jako nowy albo już istniejący. Jest czystą funkcją planującą import — nie zapisuje danych do magazynu i nie usuwa istniejących rekordów.

### 22.2. Zasada identyfikacji

- `accountingEntryId` identyfikuje pojedynczy zapis księgowy. Obejmuje rachunek w postaci lokalnego zakresu, datę, walutę, kwotę ze znakiem, kierunek, typ operacji i referencję.
- `exchangeOperationId` wiąże różne zapisy tej samej wymiany na podstawie referencji bankowej. Nie jest kluczem deduplikacji.
- Normalizacja kwoty usuwa wyłącznie nieistotne zera końcowe zapisu dziesiętnego; nie zmienia wartości liczbowej.
- Algorytm nie używa `Float` ani `Double`.

### 22.3. Testy i status

Dodano testy jednostkowe, ale ich dodanie nie jest dowodem wykonania. Dla rewizji poprzedzającej etap 2B CI potwierdził sukces zadania testów jednostkowych oraz kompilacji prototypu; walidacja interfejsu Compose pozostawała w toku podczas kontroli. Nowe pliki i aktualizacja audytu wymagają świeżego CI dla najnowszego SHA.

### 22.4. Zakres nadal otwarty

Etap 2B dostarcza mechanizm identyfikacji i klasyfikacji duplikatów, ale nie jest jeszcze kompletnym importerem plików. Do wykonania pozostają: trwały magazyn, transakcyjne zatwierdzanie paczki, parsowanie CSV/XML, obsługa XLS i PDF, podgląd przed importem, raport duplikatów/konfliktów, kontrola sum kontrolnych i testy integracyjne. Import powinien być atomowy: błąd walidacji nie może pozostawić częściowo zaimportowanej paczki.


## 23. Etap 2C — trwały magazyn zapisów wyciągów

### 23.1. Implementacja

Dodano `financial/PaymentLedgerDatabase.kt`, oparty na natywnym `SQLiteOpenHelper`, bez wprowadzania nowej zależności zewnętrznej. Magazyn:
- zapisuje pojedyncze zapisy księgowe w tabeli `bank_statement_entries`;
- używa `accounting_entry_id` jako klucza głównego i ograniczenia unikalności;
- przechowuje kwoty jako tekst dziesiętny `BigDecimal.toPlainString()`, bez utraty precyzji;
- przechowuje datę w formacie ISO-8601;
- przechowuje identyfikator operacji nadrzędnej FX jako osobne pole i indeks;
- udostępnia odczyt identyfikatorów oraz odtworzenie zapisów z lokalnej bazy;
- wykonuje import paczki w transakcji SQLite. Błąd walidacji lub zapisu powoduje wycofanie całej paczki;
- pomija ponowne rekordy na podstawie klucza głównego (SQLite CONFLICT_IGNORE).

Dodano `androidTest/.../PaymentLedgerDatabaseTest.kt` z testami: ponownego importu tego samego zapisu, wycofania paczki po błędzie walidacji oraz odtworzenia danych po zamknięciu i ponownym otwarciu bazy.

Testy używają oddzielnej nazwy pliku bazy `payment-ledger-test.db`, aby nie dotykać danych aplikacji. Konstruktor magazynu przyjmuje nazwę bazy z domyślną wartością produkcyjną.

### 23.2. Kontrola architektoniczna

- Nie dodano Room ani DataStore; zastosowano natywny SQLite, zgodnie z istniejącym zestawem zależności.
- Nie zmieniano modułu `receipts`, modelu `ReceiptRecord` ani magazynu dokumentów rachunków.
- Magazyn zapisuje wyłącznie znormalizowane zapisy wyciągów bankowych. Nie jest jeszcze połączony z ekranem `PaymentLedgerScreen` ani parserami plików.
- Wersja schematu wynosi 1. Przed zmianą struktury należy dodać jawne migracje, bez destrukcyjnego usuwania danych.
- Nie dodano pełnych numerów rachunków ani danych osobowych do logów.

### 23.3. Status testów

Przed etapem 2C, dla rewizji `515ac4718dbb4c50c9a19def8ef109cf2e6a0e5f`, CI potwierdził:
- Android JVM unit tests — success;
- Build Android debug APK — success;
- Repository compliance (REUSE) — success;
- Compose UI tests na emulatorze — success, w toku pozostawało archiwizowanie artefaktu APK.

Etap 2C został zapisany po tej rewizji. Testy instrumentacyjne nowego magazynu, kompilacja i pełny CI dla najnowszej rewizji wymagają ponownego wykonania. Do czasu uzyskania tych wyników etap 2C ma status **zaimplementowany, lecz nieweryfikowany przez CI**.

### 23.4. Ograniczenia i następne prace

1. Wykonać CI dla najnowszej rewizji, w tym testy instrumentacyjne bazy na emulatorze.
2. Dodać migracje schematu w chwili rozszerzenia magazynu o transakcje finansowe i darowizny.
3. Podłączyć magazyn do warstwy repozytorium i ekranu, z kontrolą cyklu życia oraz bez wykonywania operacji SQLite na głównym wątku.
4. Dodać parsery CSV/XML po utrwaleniu pełnych reguł mapowania pól ze wzorców źródłowych.
5. Dodać podgląd importu, raport duplikatów i konfliktów oraz atomowe zatwierdzanie z UI.
6. Rozszerzyć magazyn o odrębne tabele operacji FX i darowizn gotówkowych, zachowując rozdział zapisów księgowych od operacji nadrzędnych.


## 24. Kontrola CI rewizji etapu 2C i wyników regresji

### 24.1. Rewizja kontrolowana

Kontrola dotyczyła rewizji `e73ce209148da9c1b9cfbed2ceda3654fe5de220`.

- Android prototype build (`43624597335`) — **success**.
- Repository compliance / REUSE (`43624597230`) — **success**.
- Android GUI validation (`43624597221`) — w chwili kontroli zadanie jednostkowe zakończyło się sukcesem, a testy Compose na emulatorze nadal trwały.
- Krok `Run Android unit tests` — **success**.
- Krok `Run Compose UI tests on Android emulator` — **in_progress**; nie ma podstaw do oznaczenia całego zadania GUI jako zakończonego.

Wniosek: kompilacja i testy jednostkowe rewizji 2C zostały potwierdzone. Pełna walidacja emulatorowa oraz testy instrumentacyjne magazynu nie zostały jeszcze potwierdzone wynikiem końcowym zadania.

## 25. Etap 2D — wersjonowany format JSON wymiany danych

### 25.1. Decyzja architektoniczna

Przyjęto podział odpowiedzialności:
- **SQLite** jest lokalnym źródłem prawdy i magazynem operacyjnym.
- **JSON** jest formatem eksportu, importu, kopii zapasowej i przenoszenia danych.
- JSON nie zastępuje bazy SQLite ani nie staje się równoległym, niezależnym magazynem bieżących zmian.

Takie rozdzielenie pozwala zachować transakcyjność, indeksy i ograniczenia unikalności po stronie SQLite, a jednocześnie zapewnia przenośność danych.

### 25.2. Implementacja

Dodano:
- `financial/PaymentLedgerJsonCodec.kt` — kodowanie i dekodowanie archiwum wyciągów;
- `financial/PaymentLedgerJsonCodecTest.kt` — testy formatu i walidacji.

Format zawiera identyfikator `mobywatel-payment-ledger`, pole `schemaVersion` (obecnie 1) i tablicę `entries`. Każdy wpis przechowuje pseudonim zakresu rachunku, datę, kod waluty, kwotę jako tekst dziesiętny, typ operacji, referencję i opis. Referencja może być jawnie pusta. Przed kodowaniem i po dekodowaniu wykonywana jest walidacja modelu.

Kwoty są zapisywane przez `BigDecimal.toPlainString()`; dekoder odtwarza je przez `BigDecimal`, bez konwersji zmiennoprzecinkowej. Nieznana wersja schematu, błędny format, niepoprawny JSON i nieprawidłowe rekordy są odrzucane.

### 25.3. Testy dodane

- pełny cykl encode/decode z zachowaniem dokładnej wartości dziesiętnej i identyfikatora księgowego;
- zapis i odtworzenie ujemnej kwoty oraz pustej referencji;
- odrzucenie nieobsługiwanej wersji schematu;
- odrzucenie nieprawidłowego kodu waluty i uszkodzonego JSON.

Testy zostały dodane do repozytorium; ich wynik wymaga potwierdzenia w CI dla rewizji zawierającej etap 2D.

### 25.4. Zakres i ograniczenia

- Etap 2D dostarcza kodek JSON dla znormalizowanych zapisów wyciągu bankowego. Nie jest jeszcze kompletnym archiwum całej domeny: transakcje finansowe, aktywa, opłaty i darowizny wymagają osobnych sekcji i migracji formatu.
- Nie dodano jeszcze przycisków eksportu/importu w ekranie ani zapisu zdekodowanej paczki do SQLite.
- Nie dodano jeszcze parserów CSV/XML ani adapterów XLS/PDF.
- Kodek nie powinien być traktowany jako bezpieczna kopia zapasowa bez dodatkowej polityki szyfrowania, kontroli integralności i procedury przechowywania pliku.
- Nie zapisuje się pełnych numerów rachunków; `accountScopeId` ma być pseudonimem lokalnym.

### 25.5. Status etapów

| Obszar | Status po tej rewizji |
|---|---|
| Etap 1 — katalog, trasa i ekran bazowy | Zaimplementowany; CI wcześniejszych rewizji wykrył i skorygował regresję katalogu |
| Etap 2B — identyfikacja i deduplikacja | Zaimplementowany; wymaga dalszego rozszerzenia o konflikty referencji |
| Etap 2C — SQLite dla zapisów wyciągów | Zaimplementowany; kompilacja i testy jednostkowe potwierdzone dla SHA z sekcji 24; testy emulatorowe nadal w toku przy ostatnim odczycie |
| Etap 2D — JSON dla zapisów wyciągów | Kodek i testy dodane; oczekuje na CI |
| Repozytorium i połączenie UI–SQLite | Nierozpoczęte |
| Ręczne formularze transakcji i darowizn | Nierozpoczęte |
| Parsery CSV/XML, adaptery XLS/PDF | Nierozpoczęte |
| Podgląd importu i obsługa konfliktów | Nierozpoczęte |
| Kursy NBP i wycena | Nierozpoczęte |

### 25.6. Następny etap

Następny etap powinien obejmować warstwę repozytorium z operacjami na SQLite wykonywanymi poza głównym wątkiem, integrację ekranu z odczytem rejestru oraz zatwierdzanie importu JSON jako jednej transakcji. Następnie należy dodać parser CSV/XML na podstawie dokładnych schematów wzorców i dopiero potem osobne adaptery XLS/PDF. Przed importem trzeba rozróżniać duplikaty od konfliktów: zgodna referencja bankowa przy innej kwocie, dacie, walucie lub kierunku wymaga ręcznej weryfikacji, a nie cichego pominięcia.


## 26. Etap 2E — repozytorium SQLite i odczyt rejestru w interfejsie

### 26.1. Implementacja

Dodano `financial/PaymentLedgerRepository.kt`:
- udostępnia odczyt zapisów z SQLite;
- udostępnia eksport zapisów do wersjonowanego JSON;
- udostępnia import JSON: dekoduje i waliduje paczkę, klasyfikuje duplikaty względem lokalnej bazy, a następnie przekazuje wyłącznie nowe rekordy do transakcyjnego `importBatch`;
- operacje odczytu, eksportu i importu wykonuje w `Dispatchers.IO`, poza głównym wątkiem;
- zwraca podsumowanie liczby dodanych i pominiętych duplikatów.

Zmieniono `PaymentLedgerScreen.kt`:
- ekran tworzy repozytorium powiązane z kontekstem aplikacji;
- pobiera historię z SQLite przy otwarciu;
- wyświetla liczbę i listę zapisów;
- umożliwia ponowne odczytanie danych;
- prezentuje stan ładowania i komunikat błędu;
- zamyka magazyn przy usunięciu ekranu z kompozycji.

### 26.2. Kontrola zakresu

To jest integracja odczytu rejestru, a nie kompletna obsługa importu/eksportu przez użytkownika. Metody repozytorium `importJson` i `exportJson` są gotowe do użycia przez kolejne elementy UI, ale selektor pliku, zapis pliku i ekran podglądu importu pozostają do wykonania.

Mechanizm deduplikacji w repozytorium identyfikuje rekordy o tym samym identyfikatorze księgowym. Nie rozstrzyga jeszcze konfliktów, w których ta sama referencja bankowa ma zmienioną kwotę, datę, walutę lub kierunek. Takie przypadki muszą otrzymać osobną klasyfikację wymagającą weryfikacji przed zatwierdzeniem importu.

### 26.3. Pliki

- `financial/PaymentLedgerRepository.kt` — nowy plik;
- `PaymentLedgerScreen.kt` — integracja odczytu i wyświetlania historii.

### 26.4. Weryfikacja

Po zapisaniu zmian należy potwierdzić:
- poprawność importów i zgodność pakietów Kotlin;
- kompilację wariantu debug;
- testy jednostkowe, w tym kodeka JSON i deduplikacji;
- testy instrumentacyjne SQLite;
- testy Compose na emulatorze.

Wyniki CI dla rewizji zawierającej etap 2E są oczekiwane. Do czasu ich zakończenia etap 2E ma status **zaimplementowany, oczekujący na pełną weryfikację CI**.

### 26.5. Kolejny zakres prac

1. Dodać testy repozytorium obejmujące import JSON, idempotencję i eksport po imporcie.
2. Dodać klasyfikację konfliktów referencji bankowych i raport podglądu.
3. Dodać selektor dokumentu i eksport pliku JSON w interfejsie.
4. Dodać zatwierdzanie importu dopiero po akceptacji podglądu, w jednej transakcji.
5. Rozszerzyć model i magazyn o transakcje finansowe oraz darowizny, zachowując odrębność od zapisów wyciągu.


## 27. Etap 2F/2G — import i eksport JSON z interfejsu oraz podgląd konfliktów

### 27.1. Zakres wdrożenia

Dodano w ekranie `PaymentLedgerScreen.kt` obsługę androidowego selektora dokumentów:
- wybór pliku JSON przez `ActivityResultContracts.OpenDocument`;
- odczyt pliku przez `ContentResolver`;
- przygotowanie podglądu bez modyfikowania bazy;
- prezentację liczby nowych zapisów, duplikatów i konfliktów;
- zatwierdzenie importu tylko wtedy, gdy podgląd nie zawiera nierozstrzygniętych konfliktów;
- eksport aktualnego rejestru do pliku JSON przez `ActivityResultContracts.CreateDocument`.

### 27.2. Klasyfikacja konfliktów

Rozszerzono `BankImportDeduplicator.kt` o:
- `BankImportPreview` — wynik podglądu z listami nowych zapisów, duplikatów i konfliktów;
- `BankImportConflict` — parę zapisu przychodzącego i istniejącego;
- `preview(incoming, existingEntries)` — klasyfikację według identyfikatora księgowego i referencji bankowej w obrębie tego samego pseudonimowego zakresu rachunku.

Identyczny zapis jest duplikatem. Ta sama referencja w tym samym zakresie rachunku przy innej tożsamości księgowej jest konfliktem i blokuje zatwierdzenie paczki. Różne zakresy rachunków zachowują możliwość przedstawienia dwóch nóg jednej wymiany walutowej jako osobnych zapisów.

### 27.3. Warstwa repozytorium

Rozszerzono `PaymentLedgerRepository.kt`:
- `previewJson` dekoduje i waliduje JSON, a następnie przygotowuje podgląd względem SQLite;
- `importJson` ponownie oblicza podgląd, odrzuca import z konfliktami i zapisuje zaakceptowane nowe rekordy przez transakcyjne `importBatch`;
- `exportJson` pozostaje eksportem aktualnego rejestru.

Ponowne obliczenie podglądu przy zatwierdzaniu zabezpiecza przed wykorzystaniem nieaktualnego podglądu, jeśli baza zmieniła się od czasu wybrania pliku.

### 27.4. Testy regresyjne dodane

Rozszerzono `BankImportDeduplicatorTest.kt` o:
- rozdzielenie identycznego duplikatu, konfliktu tej samej referencji i nowego wpisu;
- zachowanie dwóch nóg wymiany przy różnych zakresach rachunków.

Testy zostały zapisane w repozytorium. Wynik wykonania wymaga CI dla najnowszej rewizji.

### 27.5. Kontrola poprzedniej rewizji 2E

Uruchomienia CI dla rewizji `de26477e8564de53c4cab404e18593f398d647e1` zostały zakończone statusem **cancelled** po pojawieniu się kolejnych zmian w gałęzi. Nie stanowią potwierdzenia kompilacji ani testów etapu 2E. Kontrola REUSE dla tej rewizji zakończyła się sukcesem, ale nie zastępuje testów Androida.

### 27.6. Ograniczenia i ryzyka

- Import/eksport obejmuje obecnie wyłącznie zapisy wyciągów `BankStatementEntry`, nie pełny model aktywów, opłat, transakcji ręcznych i darowizn.
- Konflikt jest wykrywany w ramach identycznego `accountScopeId` i referencji bankowej. Referencje powtarzalne w innych bankach lub rachunkach są odrębnymi przypadkami.
- Użytkownik może odrzucić konfliktowy plik i skorygować dane źródłowe; interfejs nie oferuje jeszcze ręcznego mapowania/rozstrzygania konfliktu.
- Odczyt pliku JSON odbywa się do pamięci; nie dodano jeszcze limitu wielkości pliku ani strumieniowego dekodowania.
- Nie dodano jeszcze szyfrowania eksportu ani mechanizmu integralności archiwum.
- Podgląd i zatwierdzenie wykonują osobne odczyty. Ponowne sprawdzenie przy zatwierdzaniu ogranicza ryzyko nieaktualności, a unikalny klucz SQLite pozostaje ostatnią ochroną przed duplikatem.
- Ekran nie może być uznany za pełny rejestr wszystkich rodzajów operacji finansowych: tabele transakcji aktywów i darowizn pozostają do wdrożenia.

### 27.7. Status i dalsze prace

| Element | Status |
|---|---|
| Wybór pliku JSON | Zaimplementowany; oczekuje na testy urządzenia |
| Podgląd nowych zapisów i duplikatów | Zaimplementowany |
| Podgląd konfliktów referencji | Zaimplementowany |
| Blokada importu z nierozstrzygniętymi konfliktami | Zaimplementowana |
| Eksport JSON przez selektor dokumentów | Zaimplementowany; oczekuje na testy urządzenia |
| Testy klasyfikacji konfliktów | Dodane; oczekują na CI |
| Pełny test import–eksport na emulatorze | Do wykonania |
| Ręczne rozstrzyganie konfliktów | Do wykonania |
| Pełny JSON domeny: aktywa, opłaty, transakcje i darowizny | Do wykonania |
| Parsery CSV/XML i adaptery XLS/PDF | Do wykonania |

Następny etap powinien rozpocząć się od uzyskania zielonego CI dla rewizji 2F/2G, poprawy ewentualnych błędów, a następnie testu urządzeniowego: eksport → import do pustej bazy → porównanie wszystkich pól → ponowny import tego samego pliku i potwierdzenie, że wszystkie wpisy są rozpoznane jako duplikaty.


## 28. Etap 2H — atomowość importu, izolowane testy repozytorium i bezpieczna obsługa plików

### 28.1. Ustalenie z przeglądu poprzedniego etapu

Kontrola implementacji 2F/2G wykazała, że odczyt i zapis strumieni SAF odbywał się w korutynach ekranu, ale bez jawnego przeniesienia operacji strumieniowych na `Dispatchers.IO`. Ponadto wstępne sprawdzenie konfliktów i zapis do SQLite były odrębnymi operacjami. Pozostawiało to teoretyczne okno wyścigu: dwa równoległe zatwierdzenia mogłyby sprawdzić ten sam stan przed zapisaniem danych.

### 28.2. Zmiany implementacyjne

**`PaymentLedgerScreen.kt`:**
- przeniesiono odczyt i zapis pliku przez `ContentResolver` na `Dispatchers.IO`;
- dodano limit wejściowego pliku JSON wynoszący 10 MiB;
- dodano ograniczony odczyt strumieniowy, który przerywa operację po przekroczeniu limitu;
- komunikat błędu informuje o przekroczeniu limitu.

**`PaymentLedgerRepository.kt`:**
- konstruktor przyjmuje opcjonalną nazwę bazy danych, domyślnie produkcyjną nazwę `PaymentLedgerDatabase.DATABASE_NAME`;
- umożliwia to testowanie na odizolowanych bazach bez dotykania danych aplikacji;
- zatwierdzanie importu przekazuje całą paczkę do metody transakcyjnej magazynu.

**`PaymentLedgerDatabase.kt`:**
- wydzielono wspólny zapis rekordów do `insertEntries`;
- dodano `importBatchRejectingReferenceConflicts`;
- metoda rozpoczyna transakcję SQLite, odczytuje istniejące zapisy, klasyfikuje duplikaty i konflikty, a następnie zapisuje nowe rekordy w tej samej transakcji;
- konflikt referencji powoduje wyjątek i wycofanie transakcji;
- zachowano unikalny klucz księgowy oraz dotychczasowe przechowywanie kwot jako tekstu dziesiętnego.

### 28.3. Testy integracyjne

Dodano `androidTest/.../PaymentLedgerRepositoryTest.kt` z dwoma scenariuszami:

1. **Pełny cykl i idempotencja:** utworzenie archiwum w bazie źródłowej, eksport JSON, import do odrębnej pustej bazy, porównanie odczytanych wpisów, ponowny import i oczekiwanie na zero nowych wpisów oraz dwa duplikaty.
2. **Konflikt bez zmiany danych:** zapis bazowego rekordu, przygotowanie archiwum z tą samą referencją i inną kwotą, potwierdzenie konfliktu w podglądzie, odrzucenie importu oraz porównanie bazy przed i po próbie.

Testy korzystają z trzech osobnych nazw baz testowych, usuwanych w metodzie `@After`. Scenariusz konfliktu obejmuje teraz jednocześnie jeden poprawny nowy wpis i jeden wpis konfliktowy; oczekiwaniem jest odrzucenie całej paczki bez zapisania również poprawnego wpisu. Dodatkowe testy JVM sprawdzają zmianę daty, waluty, kierunku i typu operacji przy tej samej referencji oraz konflikt wewnątrz jednej paczki. Dodanie testów nie jest równoznaczne z ich wykonaniem; wymagają uruchomienia w CI.

### 28.4. Kontrola poprawności kodu po zmianach

Ponownie pobrano z gałęzi pliki:
- `financial/PaymentLedgerDatabase.kt`;
- `financial/PaymentLedgerRepository.kt`;
- `PaymentLedgerScreen.kt`;
- `androidTest/.../PaymentLedgerRepositoryTest.kt`.

Kontrola tekstowa potwierdziła:
- izolowaną nazwę bazy przekazywaną przez repozytorium;
- obecność transakcyjnej metody importu z kontrolą konfliktów;
- przeniesienie operacji SAF na `Dispatchers.IO`;
- limit 10 MiB dla pliku importu;
- obecność testów pełnego cyklu oraz testu odrzucenia konfliktu.

Kontrola tekstowa nie zastępuje kompilacji Kotlin, testów Android Instrumented ani testu rzeczywistego selektora dokumentów.

### 28.5. Ograniczenia pozostałe po etapie 2H

- Limit 10 MiB dotyczy pliku wejściowego JSON; pełny eksport nadal jest budowany w pamięci jako łańcuch znaków.
- Nie dodano jeszcze limitu liczby wpisów w pojedynczym archiwum ani strumieniowego dekodera JSON.
- Testy integracyjne repozytorium oczekują na wynik emulatorowego CI.
- Nie wykonano jeszcze ręcznego testu na urządzeniu z dostawcami dokumentów (lokalny magazyn, Dysk, menedżer plików).
- JSON obejmuje nadal wyłącznie `BankStatementEntry`, a nie całe aktywa, opłaty, transakcje ręczne i darowizny.
- Parsery CSV/XML oraz adaptery XLS/PDF pozostają do wykonania.
- Nie zmieniano modułu `receipts`, jego schematu, plików ani tras.

### 28.6. Status etapu

| Element | Status |
|---|---|
| Odczyt i zapis plików SAF na IO | Zaimplementowane |
| Limit wielkości importowanego JSON | Zaimplementowany: 10 MiB |
| Izolowana baza testowa repozytorium | Zaimplementowana |
| Test eksport → import → ponowny import | Dodany; oczekuje na wykonanie CI |
| Test konfliktu i niezmienności bazy, również przy mieszanej paczce | Dodany; oczekuje na wykonanie CI |\n| Testy konfliktów po zmianie daty, waluty, kierunku i typu oraz wewnątrz paczki | Dodane; oczekują na wykonanie CI |
| Kontrola konfliktu i zapis w jednej transakcji SQLite | Zaimplementowane |
| Kompilacja Android po etapie 2H | Oczekuje na wynik CI |
| Testy JVM i instrumentacyjne po etapie 2H | Oczekują na wynik CI |
| Ręczny test SAF na urządzeniu | Do wykonania |

### 28.7. Następny etap

Po zielonym CI należy przeprowadzić test urządzeniowy plików SAF oraz rozpocząć etap 2I: parser CSV i XML iPKO. Parsery należy budować na podstawie rzeczywistych, zanonimizowanych próbek i testów wzorcowych. Przed implementacją należy ponownie sprawdzić dostępność próbek i ich dokładne nagłówki/tagi; nie wolno zakładać struktury plików na podstawie samych rozszerzeń.


## 29. Etap 2H.1 — kontrola testu atomowego importu i korekty składniowe

### 29.1. Wynik ponownego przeglądu

Podczas ponownego odczytu pliku `PaymentLedgerRepositoryTest.kt` wykryto dwie niezgodności w scenariuszu testowym:
- wyrażenie wielowierszowe kodowania archiwum zostało zapisane z dosłownymi sekwencjami `\\n`, zamiast jako prawidłowe linie kodu Kotlin;
- po rozszerzeniu paczki o jeden poprawny wpis i jeden konflikt test nadal oczekiwał zera nowych wpisów w samym podglądzie. Prawidłowy podgląd powinien wykazać jeden wpis nowy i jeden konflikt, natomiast dopiero zatwierdzenie powinno odrzucić całą paczkę.

### 29.2. Wykonane korekty

W `androidTest/.../PaymentLedgerRepositoryTest.kt`:
- poprawiono wyrażenie `PaymentLedgerJsonCodec.encode(...)` na prawidłowy zapis wielowierszowy;
- poprawiono oczekiwanie podglądu: jeden konflikt i jeden potencjalnie nowy wpis;
- pozostawiono asercję, że po odrzuceniu zatwierdzenia zawartość bazy jest identyczna z zawartością sprzed importu.

Dzięki temu test rozdziela prawidłowo dwa różne wyniki: klasyfikację paczki i atomowy skutek zatwierdzania.

### 29.3. Kontrola pliku po poprawkach

Ponownie pobrano plik z gałęzi i potwierdzono:
- brak dosłownych sekwencji `\\n` w wyrażeniu Kotlin;
- poprawne trzy linie wywołania `encode`;
- asercję `preview.conflicts.size == 1`;
- asercję `preview.newEntries.size == 1`;
- asercję niezmienności rejestru po odrzuconym imporcie.

### 29.4. Status CI

- Dla wcześniejszej rewizji `1ce4b1f6d390417c0c0ef50d49be8bf52065d979` kontrola REUSE i Android prototype build zakończyły się sukcesem. GUI validation zostało anulowane; nie potwierdzono kompletu testów instrumentacyjnych.
- Uruchomienia dla rewizji pośredniej `f96ab775cebd4511d9f7ffd25c77eacb833b3002` zostały anulowane po kolejnych korektach testu.
- Korekty testu zostały zapisane w rewizji `02d7a48992a23980cdbf0b9849ee12ce49f1fc47`. Dla tej rewizji nie ma jeszcze końcowego wyniku Android CI.

W związku z tym nie oznacza się kompilacji ani testów instrumentacyjnych najnowszej rewizji jako zaliczonych. Kontrola tekstowa potwierdza poprawność wskazanych linii testu, ale nie zastępuje wykonania Gradle.

### 29.5. Status etapu 2H po korekcie

| Element | Status |
|---|---|
| Kod transakcyjnego importu i obsługi SAF | Zaimplementowany |
| Test pełnego cyklu eksport/import/idempotencja | Dodany; oczekuje na CI |
| Test atomowego odrzucenia mieszanej paczki | Poprawiony; oczekuje na CI |
| Testy klasyfikacji zmienionej daty/waluty/kierunku/typu | Dodane; oczekują na CI |
| Kompilacja Android najnowszej rewizji | Oczekuje na CI |
| Testy JVM i instrumentacyjne najnowszej rewizji | Oczekują na CI |
| Ręczny test dostawców SAF | Do wykonania |

### 29.6. Następny krok

Należy uzyskać zielone wyniki CI dla rewizji zawierającej sekcję 29. Dopiero po tym można przejść do parserów CSV/XML iPKO. W repozytorium nie znaleziono plików wzorcowych wyciągów bankowych, dlatego mapowanie nagłówków CSV, struktur XML, kodowania i formatów kwot pozostaje nieustalone. Parserów nie należy implementować na podstawie domysłów.


## 30. Etap 2I — parser XML historii rachunku iPKO

### 30.1. Źródło i rozpoznany schemat

Odnaleziono w załącznikach tej rozmowy rzeczywisty plik XML „Zestawienie operacji za 14.11.2025 - 14.11.2026.xml”. Dokument ma strukturę:
- korzeń `account-history`;
- sekcję `search` z datami zakresu i elementem `account`;
- sekcję `operations` zawierającą powtarzalne elementy `operation`;
- pola operacji: `order-date`, `exec-date`, `type`, `description`, `amount`;
- atrybut `curr` elementu `amount`.

Próbka zawiera pięć operacji: cztery uznania USD i jedno obciążenie USD. Występują kwoty dziesiętne z kropką, znaki dodatnie/ujemne i referencje w tekście opisu pod etykietą „Referencje własne zleceniodawcy”. Wartości daty zlecenia i wykonania w próbce są zgodne.

Nie znaleziono w dostępnych załącznikach pliku CSV ani XLS. Nie należy więc deklarować parserów tych formatów jako wykonanych ani wyprowadzać ich mapowania z XML/PDF.

### 30.2. Implementacja

Dodano `financial/BankStatementXmlParser.kt`:
- parser wykorzystuje Android XmlPullParser;
- wymaga korzenia `account-history`;
- wyłącza przetwarzanie deklaracji dokumentu DTD;
- odczytuje datę wykonania, a przy jej braku datę zlecenia;
- odczytuje typ operacji, kwotę dziesiętną i kod waluty z atrybutu `curr`;
- wyodrębnia referencję bankową z pola opisu;
- wymaga przekazania lokalnego pseudonimu `accountScopeId` — nie pobiera i nie zapisuje pełnego numeru rachunku z nagłówka XML;
- nie zapisuje surowego opisu, który w próbce zawiera numery rachunków kontrahentów, nazwiska i adresy;
- gdy referencji brak, zapisuje jedynie odcisk SHA-256 opisu połączonego z lokalnym pseudonimem, aby zachować stabilną tożsamość do deduplikacji bez przechowywania jawnego opisu;
- używa `BigDecimal` do zachowania dokładności kwot;
- odrzuca brak daty, kwoty, waluty, typu operacji, nieprawidłową walutę/kwotę i dokument bez operacji.

Parser nie jest jeszcze podłączony do przycisku importu w UI ani do automatycznego wyboru zakresu rachunku. Wymagany jest lokalny pseudonim rachunku przekazany przez warstwę wywołującą.

### 30.3. Testy instrumentacyjne

Dodano `androidTest/.../BankStatementXmlParserTest.kt`:
1. test odczytu pięciu operacji i dokładnych kwot;
2. test braku numerów rachunków kontrahentów, nazw i adresów w danych wynikowych;
3. test wymagania pseudonimu rachunku i odrzucania niekompletnej operacji.

Testy korzystają z anonimowej próbki wzorowanej na rzeczywistej strukturze XML. Oczekują czterech dodatnich zapisów i jednego ujemnego, zachowania referencji i dat oraz braku jawnych danych kontrahenta.

### 30.4. Kontrola po implementacji

Ponownie pobrano plik parsera i testu z gałęzi. Kontrola treści potwierdziła:
- użycie prawidłowych wzorców wyrażeń regularnych dla polskiej etykiety referencji;
- przekazywanie lokalnego `accountScopeId`;
- normalizację waluty z użyciem `Locale.ROOT`;
- walidację korzenia i niepustej listy operacji;
- nieprzechowywanie surowego opisu;
- testy dokładności kwot oraz minimalizacji danych.

Jest to kontrola kodu źródłowego. Wynik kompilacji i testów instrumentacyjnych musi zostać potwierdzony w CI dla rewizji zawierającej parser i test.

### 30.5. Ograniczenia i dalsze zadania

- Parser obsługuje rozpoznany wariant XML iPKO `account-history`; inne warianty eksportu wymagają osobnych fixture'ów.
- Parser nie rozstrzyga automatycznie, do którego lokalnego rachunku przypisać plik. Wywołujący musi przekazać pseudonim.
- Nie dodano jeszcze interfejsu importu XML ani raportu podglądu XML przed zatwierdzeniem. Wynik parsera powinien przejść przez istniejący `BankImportDeduplicator.preview` i atomowy import SQLite.
- Nie znaleziono próbek CSV i XLS w dostępnych załącznikach; parsery CSV i XLS są wstrzymane do czasu uzyskania wzorców.
- PDF wymaga osobnego adaptera ekstrakcji tekstu i testów stron, tabel oraz nagłówków. Nie należy traktować podsumowania z pierwszej strony PDF jako wpisów transakcyjnych.
- Nie zapisuje się pełnego numeru rachunku z XML ani surowego opisu kontrahenta.

### 30.6. Status etapu

| Element | Status |
|---|---|
| Rozpoznanie schematu XML na rzeczywistym załączniku | Wykonane |
| Parser XML iPKO | Zaimplementowany |
| Testy XML: pięć operacji, kwoty i referencje | Dodane; oczekują na CI |
| Test minimalizacji danych osobowych | Dodany; oczekuje na CI |
| Integracja parsera XML z selektorem plików i podglądem | Do wykonania |
| Przekazanie lokalnego pseudonimu rachunku z UI | Do wykonania |
| Parser CSV | Oczekuje na próbkę CSV |
| Adapter XLS | Oczekuje na próbkę XLS |
| Adapter PDF | Do wykonania po opracowaniu testów ekstrakcji |
| Kompilacja i pełny CI po etapie 2I | Oczekuje na wynik |

### 30.7. Następny krok

Najpierw uzyskać zielone CI dla parsera XML. Następnie zintegrować wybór XML z podglądem i atomowym zatwierdzaniem importu. Kolejno przygotować adapter PDF na podstawie rzeczywistej próbki i testów ekstrakcji, a parser CSV/XLS wdrażać dopiero po dostarczeniu tych konkretnych plików.


## 31. Etap 2I.1 — integracja importu XML iPKO z ekranem rejestru

### 31.1. Ustalenie z przeglądu

Kontrola po etapie 2I wykazała, że parser XML był dostępny jako klasa, ale ekran `PaymentLedgerScreen` nadal udostępniał wyłącznie import JSON. Użytkownik nie miał zatem możliwości uruchomienia parsera z poziomu aplikacji. Sam parser nie stanowił kompletnej funkcji importu.

### 31.2. Wykonane zmiany

W `PaymentLedgerScreen.kt`:
- dodano osobny selektor pliku XML przez Android Storage Access Framework;
- dodano przycisk „Importuj XML iPKO” obok importu JSON;
- dodano pole lokalnego pseudonimu rachunku z domyślną wartością `ipko-account-1`;
- UI wyraźnie instruuje, aby nie wpisywać numeru rachunku;
- plik jest odczytywany poza głównym wątkiem z limitem 10 MiB;
- parser XML iPKO przekształca operacje do modelu `BankStatementEntry`;
- wynik parsera jest kodowany istniejącym `PaymentLedgerJsonCodec`, a następnie przekazywany do istniejącego `repository.previewJson`;
- zatwierdzenie wykorzystuje dotychczasowy `repository.importJson`, a więc istniejącą atomową transakcję SQLite i kontrolę konfliktów referencji;
- nie powstał odrębny magazyn ani drugi mechanizm deduplikacji.

W `BankStatementXmlParser.kt`:
- dodano walidację pseudonimu rachunku: 1–64 znaki ASCII, cyfry, kropka, myślnik lub podkreślenie;
- dodano limit długości wejściowego dokumentu XML;
- zachowano walidację korzenia `account-history`, wyłączenie przetwarzania DTD, użycie `BigDecimal` i ochronę danych kontrahentów.

### 31.3. Rozszerzone testy

W `BankStatementXmlParserTest.kt`:
- dodano test przejścia od parsera XML przez kodowanie/dekodowanie JSON do podglądu deduplikacji;
- dodano test odrzucania nieprawidłowego aliasu i nieoczekiwanego korzenia XML;
- porównanie kwot w teście round-trip wykonuje się przez `BigDecimal.compareTo`, ponieważ format JSON celowo normalizuje zapis dziesiętny przez usuwanie zbędnych zer końcowych;
- zachowano test pięciu operacji, referencji, znaków kwot i minimalizacji danych osobowych.

### 31.4. Kontrola kodu po zmianach

Ponownie pobrano z gałęzi:
- `PaymentLedgerScreen.kt`;
- `BankStatementXmlParser.kt`;
- `BankStatementXmlParserTest.kt`;
- `PaymentLedgerJsonCodec.kt`;
- `PaymentLedgerRepository.kt`.

Kontrola treści potwierdza obecność selektora XML, pola aliasu, przepływu parser → JSON codec → podgląd → atomowy import oraz walidacji aliasu i limitu XML. Kontrola źródłowa nie zastępuje testu na urządzeniu ani testu instrumentacyjnego.

### 31.5. CI dla rewizji kodu

Rewizja kodu po integracji i poprawkach testu: `957a1c334186bb2daef75b7ab8d6f21b8e72f48a`.

- REUSE / Repository compliance: sukces.
- Android prototype build: uruchomiony; wynik końcowy należy potwierdzić w Actions.
- Android GUI validation: uruchomiony; wynik końcowy należy potwierdzić w Actions.
- Wcześniejsze uruchomienia dla SHA `b93617e6f5652fcb178bd26e89fee764b80a85a2` zostały anulowane po kolejnej poprawce testu i nie są dowodem poprawności najnowszego kodu.

Nie oznacza się kompilacji, testów instrumentacyjnych ani APK jako zaliczonych bez zakończonego wyniku dla właściwej rewizji.

### 31.6. Ograniczenia

- Import XML zakłada rozpoznany wariant eksportu `account-history`.
- Pseudonim rachunku jest obecnie wpisywany ręcznie; nie ma jeszcze konfiguracji i zapamiętywania wielu aliasów rachunków.
- Import obejmuje pojedyncze zapisy bankowe. Nie odtwarza jeszcze pełnej dwustronnej transakcji walutowej z pary obciążenie/uznanie; oba zapisy pozostają osobnymi nogami księgowymi.
- Parser nie jest jeszcze adapterem CSV, XLS ani PDF.
- Pełny model aktywów, opłat, darowizn i wyceny nie jest jeszcze trwale przechowywany w bazie SQLite.

### 31.7. Status

| Element | Status |
|---|---|
| Parser XML iPKO | Zaimplementowany |
| Wybór XML z poziomu ekranu | Zaimplementowany |
| Alias rachunku bez numeru rachunku | Zaimplementowany |
| Limit wejściowego XML | Zaimplementowany |
| Podgląd przez istniejący mechanizm | Zaimplementowany |
| Atomowy zapis przez istniejący mechanizm SQLite | Zaimplementowany |
| Test przepływu XML → JSON → podgląd | Dodany |
| Test walidacji aliasu i korzenia XML | Dodany |
| REUSE dla SHA 957a1c3 | Sukces |
| Kompilacja i testy Android dla SHA 957a1c | Weryfikacja CI |
| Ręczny test SAF na urządzeniu | Do wykonania |
| Parser CSV/XLS i adapter PDF | Oczekują na dalsze etapy |

### 31.8. Następny etap

Po uzyskaniu zielonego CI i teście SAF należy dodać bezpieczną konfigurację wielu lokalnych aliasów rachunków, a następnie rozpocząć adapter PDF na podstawie rzeczywistej próbki. Parser CSV i adapter XLS wymagają osobnych plików wzorcowych.

## 32. Etap 2I.2 — korekta deduplikacji dwóch nóg tej samej wymiany FX (14 listopada 2026 r.)

### 32.1. Wykryta niezgodność

Przegląd rzeczywistego przepływu importu ujawnił lukę pomiędzy założeniem audytu a implementacją BankImportDeduplicator.preview:
- audyt i model rozróżniają pojedynczy zapis księgowy (accountingEntryId) od operacji nadrzędnej (exchangeOperationId);
- próbka iPKO zawiera tę samą referencję własną zleceniodawcy dla obciążenia USD i uznania CHF;
- poprzedni podgląd traktował każdą ponowną referencję w obrębie tego samego pseudonimu rachunku jako konflikt, niezależnie od tego, czy rekord stanowił drugą, przeciwną nogę wymiany.

Wcześniejszy test previewKeepsOppositeExchangeLegsWhenAccountScopesDiffer sprawdzał jedynie przypadek dwóch różnych zakresów rachunku. Nie zabezpieczał przypadku, w którym obie nogi operacji znajdują się w jednym zakresie rachunku. W praktyce mogło to blokować prawidłowy import drugiej nogi FX.

### 32.2. Wykonana korekta

W financial/BankImportDeduplicator.kt:
- zastąpiono mapę pojedynczego rekordu dla referencji grupowaniem rekordów według referencji;
- dodano wąską regułę isOppositeFxLeg;
- ta sama referencja może wskazywać dwie odrębne nogi wyłącznie wtedy, gdy oba typy opisują wymianę kantorową, jedna noga jest uznaniem, druga obciążeniem, waluty są różne, a znaki kwot przeciwne;
- wszystkie pozostałe rozbieżności z tą samą referencją nadal są konfliktami wymagającymi ręcznej weryfikacji;
- duplikacja nadal jest wykrywana wcześniej przez accountingEntryId, a exchangeOperationId pozostaje identyfikatorem wiążącym nogi, nie kluczem deduplikacji.

W financial/BankImportDeduplicatorTest.kt dodano regresję obejmującą dwie przeciwne nogi tej samej wymiany, ten sam pseudonim rachunku i referencję, brak konfliktu w podglądzie oraz późniejszy import drugiej nogi przy obecności pierwszej w rejestrze. Dotychczasowe testy konfliktów zmiany daty, waluty, kierunku i typu pozostawiono bez zmiany.

### 32.3. Kontrola zapisu

Zmiany zapisano na feature/payment-ledger-stage-1:
- BankImportDeduplicator.kt — commit aa667e37fa93b4d80f9340ff6d76131864773195;
- BankImportDeduplicatorTest.kt — commit 3c4050797aa75f65d0614f87620fd538ea2ce992.

Po zapisie ponownie pobrano oba pliki z GitHub i potwierdzono grupowanie rekordów o wspólnej referencji, wywołanie reguły isOppositeFxLeg, warunki rozróżnienia nóg FX oraz obecność testu dla tego samego pseudonimu rachunku. Zachowano testy konfliktów.

### 32.4. CI i artefakty — rozdzielenie rewizji

Dla wcześniejszej rewizji kodu 957a1c334186bb2daef75b7ab8d6f21b8e72f48a:
- REUSE / Repository compliance — sukces;
- Android JVM unit tests w zadaniu assemble-debug — sukces;
- Android debug APK — kompilacja i przesłanie artefaktu zakończone sukcesem;
- artefakt obywatel-android-debug istnieje, ale został zbudowany dla SHA 957a1c3, czyli przed korektą z rozdziału 32;
- Android GUI validation — anulowana podczas kroku Compose UI tests; kompilacja APK w tym zadaniu została pominięta.

Artefakt dla SHA 957a1c3 nie potwierdza poprawności ani nie zawiera korekty z rozdziału 32. Nie należy przedstawiać go jako wydania najnowszego kodu. Dla rewizji 3c4050797aa75f65d0614f87620fd538ea2ce992 w chwili aktualizacji audytu nie było jeszcze powiązanego wyniku workflow. Testy jednostkowe, emulatorowe i kompilacja APK po korekcie pozostają do potwierdzenia.

### 32.5. Status etapu i pozostałe ograniczenia

| Element | Status |
|---|---|
| Rozpoznanie dwóch nóg FX ze wspólną referencją | Skorygowana reguła |
| Dwie nogi w tym samym zakresie rachunku | Test regresyjny dodany |
| Zwykłe konflikty tej samej referencji | Zachowana blokada |
| Ponowny import istniejącej nogi | Deduplikacja przez accountingEntryId |
| Testy JVM po korekcie | Oczekują na CI dla SHA 3c40507 |
| Testy Compose i instrumentacyjne po korekcie | Oczekują na CI |
| APK zawierający korektę | Niepotwierdzony |
| Konfiguracja wielu aliasów rachunków | Do wykonania |
| Ręczny test SAF na urządzeniu | Do wykonania |

Następny krok: uzyskać pełny wynik CI dla aktualnego SHA, a następnie wykonać etap konfiguracji wielu pseudonimów rachunków. Konfiguracja powinna być lokalna, nie przechowywać pełnych numerów rachunków i wymagać jawnego przypisania wybranego pliku do aliasu. Nie należy rozpoczynać parserów CSV/XLS bez rzeczywistych plików wzorcowych ani adaptera PDF bez próbki PDF i testów ekstrakcji.

## 33. Etap 2J — trwała konfiguracja wielu pseudonimów rachunków (14 listopada 2026 r.)

### 33.1. Stan wyjściowy i zakres

Po etapie 2I.1 ekran importu XML przyjmował pojedynczy alias wpisywany ręcznie. Wartość była przechowywana wyłącznie w stanie ekranu Compose, więc lista rachunków nie była trwała i użytkownik musiał ponownie wpisywać pseudonim. Audyt wskazywał konfigurację wielu aliasów jako kolejny krok.

Zakres etapu obejmuje lokalną rejestrację pseudonimów, ich wybór przed importem XML oraz migrację istniejącej bazy bez utraty zapisów. Nie przechowuje się numerów rachunków bankowych ani danych logowania.

### 33.2. Zmiany w SQLite

W PaymentLedgerDatabase.kt:
- podniesiono DATABASE_VERSION z 1 do 2;
- dodano tabelę payment_account_scopes z kluczem głównym account_scope_id i unikalną etykietą display_name;
- przy tworzeniu nowej bazy dodawany jest domyślny alias ipko-account-1 / Rachunek iPKO 1;
- migracja wersji 1 tworzy tabelę aliasów i odtwarza w niej wszystkie dotychczasowe account_scope_id na podstawie istniejących zapisów bank_statement_entries;
- migracja zachowuje identyfikatory i dane istniejących wpisów, a następnie dodaje domyślny alias, jeśli nie koliduje z istniejącym wpisem;
- dodano odczyt listy aliasów oraz zapis/aktualizację aliasu w transakcji SQLite;
- identyfikator aliasu jest walidowany wzorcem 1–64 znaków ASCII: litery, cyfry, kropka, myślnik i podkreślenie; etykieta ma 1–64 znaki;
- blokowana jest próba przypisania tej samej etykiety do dwóch różnych identyfikatorów.

### 33.3. Repozytorium i ekran

W PaymentLedgerRepository.kt dodano metody getAccountScopes i saveAccountScope; operacje bazodanowe wykonują się na Dispatchers.IO.

W PaymentLedgerScreen.kt:
- ekran pobiera listę zapisanych aliasów razem z historią rejestru;
- alias można zapisać z poziomu ekranu;
- zapisane aliasy są prezentowane jako wybieralne pozycje;
- wybrany alias jest przekazywany do parsera XML iPKO, który nadal waliduje identyfikator przed przetworzeniem dokumentu;
- JSON zachowuje dotychczasowy format i istniejący przepływ importu; wybór aliasu dotyczy importu XML, ponieważ JSON zawiera już accountScopeId w rekordach.

Interfejs na tym etapie dodaje i wybiera aliasy. Nie dodano usuwania aliasów, aby uniknąć osierocenia historii bankowej; identyfikator aliasu jest elementem tożsamości księgowej wpisów i nie powinien być zmieniany po imporcie.

### 33.4. Testy dodane

W PaymentLedgerDatabaseTest.kt dodano test, który tworzy bazę w schemacie wersji 1, zapisuje istniejący wpis, otwiera ją przez wersję 2, sprawdza odtworzenie aliasu, zachowanie wpisu, dodanie kolejnego aliasu i trwałość po ponownym otwarciu bazy. Dodano także test odrzucania niebezpiecznego identyfikatora oraz powtórzonej etykiety.

W PaymentLedgerRepositoryTest.kt dodano test metod repozytorium, dwóch aliasów i ich trwałości po zamknięciu oraz ponownym otwarciu repozytorium.

### 33.5. Kontrola kodu i CI

Zmiany kodu zapisano na feature/payment-ledger-stage-1 w commitach:
- PaymentLedgerDatabase.kt — 36ad482d7c06b5d6241fa36fb0e07a87c0acb04f;
- PaymentLedgerRepository.kt — 66cb4df83bc9c2e548152c78044ea89084a004a5;
- PaymentLedgerScreen.kt — 414ff7ffcbb0f516e33efebcf8ab145d941968d2;
- PaymentLedgerDatabaseTest.kt — 707b60ad0e94fe7746a2033b30b2ae991937d202;
- PaymentLedgerRepositoryTest.kt — 6e746d8a4fedd502c155690af0d78ee9f3751da7.

Po zapisaniu ponownie pobrano z GitHub wszystkie pięć plików. Kontrola treści potwierdziła wersję bazy 2, tabelę aliasów, migrację SELECT DISTINCT z dotychczasowych zapisów, metody repozytorium, selektor aliasów na ekranie i oba testy trwałości/migracji.

Dla wcześniejszej rewizji 6763f2abaeecc57105163d3e9b6204038d467ef2 testy JVM i kompilacja Android debug APK zakończyły się sukcesem; artefakt obywatel-android-debug jest przypisany do dokładnie tego SHA. Testy Compose UI na emulatorze zostały anulowane podczas kolejnej aktualizacji gałęzi. Ten artefakt nie zawiera etapu 2J.

Dla bieżącej rewizji 6e746d8a4fedd502c155690af0d78ee9f3751da7 w chwili zapisu rozdziału 33 nie było jeszcze opublikowanego wyniku CI ani artefaktu. Testy JVM, instrumentacyjne (w tym migracja bazy), testy Compose i kompilacja APK dla etapu 2J pozostają do potwierdzenia. Kontrola źródłowa nie zastępuje wykonania testów.

### 33.6. Status i dalsze ograniczenia

| Element | Status |
|---|---|
| Wiele pseudonimów rachunków | Zaimplementowano |
| Trwałość aliasów w SQLite | Zaimplementowano |
| Migracja schematu 1 → 2 | Zaimplementowano; test dodany |
| Odtworzenie aliasów istniejących wpisów | Zaimplementowano w migracji |
| Dodawanie i wybór aliasu w UI | Zaimplementowano |
| Brak przechowywania pełnych numerów rachunków | Zachowano |
| Testy JVM dla rewizji 2J | Oczekują na CI |
| Test migracji i test repozytorium na emulatorze | Dodane; oczekują na CI |
| APK zawierający etap 2J | Niepotwierdzony |
| Zmiana/łączenie aliasów po imporcie | Celowo nieobsługiwane |
| Usuwanie aliasów | Odłożone, aby nie osierocić historii |
| Ręczny test SAF na urządzeniu | Do wykonania |

Następny etap po zielonym CI: powiązanie dwóch nóg FX w odrębny model transakcji nadrzędnej bez łączenia ani usuwania zapisów księgowych, a następnie import PDF na podstawie rzeczywistej próbki. CSV i XLS nadal wymagają plików wzorcowych.

### 33.7. Dodatkowa kontrola bezpieczeństwa wątku UI/IO

Podczas końcowego przeglądu przepływu importu XML wykryto, że identyfikator aliasu Compose był odczytywany wewnątrz bloku Dispatchers.IO. Poprawiono to w PaymentLedgerScreen.kt: wybrany pseudonim jest teraz przechwytywany na wątku UI przed uruchomieniem pracy asynchronicznej i dopiero jego niezmienna wartość jest przekazywana parserowi. Eliminuje to odczyt stanu Compose z wątku roboczego i gwarantuje, że plik zostanie przypisany do aliasu wybranego w chwili rozpoczęcia importu.

Zmiana: commit d9388fdd5fa902ef204da74355cf4f99225f87a5. Po zapisie ponownie pobrano PaymentLedgerScreen.kt i potwierdzono przechwycenie selectedAccountScopeId przed Dispatchers.IO oraz przekazanie tej wartości do BankStatementXmlParser.parse. Ten commit jest najnowszą rewizją kodu etapu 2J. Testy JVM, instrumentacyjne, Compose UI i kompilacja APK dla SHA d9388fdd5fa902ef204da74355cf4f99225f87a5 pozostają do potwierdzenia w CI; wcześniejsze zadania dla SHA 6e746d8 zostały anulowane po kolejnej aktualizacji kodu i nie stanowią dowodu dla końcowej rewizji.

## 34. Etap 2K — projekcja nadrzędnych operacji FX i zabezpieczenie rejestracji aliasu (14 listopada 2026 r.)

### 34.1. Korekta spójności aliasów rachunków

Kontrola etapu 2J wykazała przypadek brzegowy: pole aliasu na ekranie pozwalało wpisać nowy identyfikator, a następnie rozpocząć import XML bez uprzedniego użycia przycisku „Zapisz alias”. Wpis bankowy mógł wtedy otrzymać account_scope_id, którego nie było w tabeli payment_account_scopes. Po ponownym uruchomieniu aplikacji taki zakres nie pojawiałby się na liście aliasów.

Poprawiono PaymentLedgerScreen.kt. Po wyborze pliku XML ekran pobiera zapisane zakresy i przed parsowaniem rejestruje brakujący alias pod wpisanym identyfikatorem. Zapis wykorzystuje walidację i transakcję SQLite istniejącej metody saveAccountScope. Identyfikator wybrany przez użytkownika jest nadal przechwytywany przed pracą Dispatchers.IO i ta sama niezmienna wartość trafia do parsera XML. Zmiana: commit d3159ab61d1850c91250fdfae1664b1f9b833a0b.

### 34.2. Projekcja operacji nadrzędnych FX

Dotychczas model BankStatementEntry reprezentował pojedyncze zapisy księgowe i udostępniał exchangeOperationId(), ale brakowało odrębnej funkcji, która odczytowo grupowałaby nogi tej samej wymiany. Dodano:
- financial/BankExchangeOperation.kt — model BankExchangeOperation, statusy COMPLETE, SINGLE_LEG i REVIEW_REQUIRED oraz BankExchangeOperationProjector;
- test BankExchangeOperationTest.kt.

Projektor grupuje rekordy według exchangeOperationId. Status COMPLETE nadaje wyłącznie grupie zawierającej dokładnie dwa zapisy kantorowe w różnych walutach i o przeciwnych znakach kwot. Pojedynczą nogę oznacza SINGLE_LEG, a grupy wieloelementowe lub niespełniające warunków pary oznacza REVIEW_REQUIRED. Zwraca oryginalne rekordy jako listę legs; nie łączy ich, nie usuwa, nie przelicza kwot i nie modyfikuje magazynu SQLite. W ten sposób zachowano rozdział zapisu księgowego i operacji nadrzędnej.

Testy obejmują rzeczywisty przykład 8 000 USD obciążenia i 6 183,20 CHF uznania z referencją 90124777, zachowanie dwóch odrębnych identyfikatorów księgowych, pojedynczą nogę, niejednoznaczną grupę trzech wpisów oraz rekord bez referencji.

### 34.3. Zapis i kontrola źródeł

Zmiany zapisano na feature/payment-ledger-stage-1:
- PaymentLedgerScreen.kt — d3159ab61d1850c91250fdfae1664b1f9b833a0b;
- BankExchangeOperation.kt — d7f72e170b3cc2f2b7b1e8d27ed443ed840b9f94;
- BankExchangeOperationTest.kt — e46df0a2e12db2806874301f154347b1709fbe29.

Po zapisie ponownie pobrano te pliki z GitHub. Potwierdzono automatyczną rejestrację aliasu przed parsowaniem, przechwycenie identyfikatora poza blokiem IO, istnienie projektora, trzy statusy operacji oraz testy pary kompletnej, pojedynczej nogi i grupy wymagającej kontroli.

### 34.4. CI i zakres potwierdzenia

Dla SHA e46df0a2e12db2806874301f154347b1709fbe29 GitHub Actions uruchomił:
- Repository compliance (alfa) — in_progress w chwili aktualizacji audytu;
- Android prototype build — in_progress;
- Android GUI validation — in_progress.

Nie ma jeszcze potwierdzenia wykonania testów JVM, testów emulatorowych ani kompilacji APK dla tej rewizji. Wcześniejszy APK z rewizji 6763f2a nie zawiera etapów 2J i 2K; nie może być traktowany jako artefakt aktualnego kodu. PR #68 pozostaje otwarty i niescalony.

### 34.5. Status i ograniczenia

| Element | Status |
|---|---|
| Automatyczna rejestracja aliasu użytego w imporcie XML | Zaimplementowano |
| Zachowanie aliasu po ponownym uruchomieniu | Oparte na tabeli SQLite; testy migracji i trwałości dodane w etapie 2J |
| Grupowanie nóg FX według exchangeOperationId | Zaimplementowano jako projekcję odczytową |
| Walidacja pary FX | COMPLETE tylko dla dwóch przeciwnych nóg kantorowych w różnych walutach |
| Niepełne i niejednoznaczne operacje | Oznaczane SINGLE_LEG / REVIEW_REQUIRED |
| Zachowanie odrębnych zapisów księgowych | Zachowano i objęto testem |
| Trwała tabela operacji nadrzędnych FX | Nie dodano; projektor odtwarza grupy z zapisów źródłowych |
| Prezentacja grup FX na ekranie | Do wykonania |
| Testy JVM/Android dla SHA 2K | Oczekują na CI |
| APK zawierający etapy 2J i 2K | Niepotwierdzony |
| Ręczny test importu SAF na urządzeniu | Do wykonania |

Następny etap: po pozytywnym CI wyświetlić operacje FX jako grupy z rozwijanymi nogami źródłowymi, a następnie dodać trwały model transakcji finansowej i prowizji, zachowując relację do zapisów wyciągu. Import PDF należy rozpocząć na podstawie istniejącej rzeczywistej próbki PDF, sprawdzając ekstrakcję tabeli i pomijanie podsumowań. CSV/XLS nie powinny być rozwijane bez dostępnych plików wzorcowych.

## 35. Etap 2L — prezentacja operacji FX jako grup i izolacja identyfikatorów (14 listopada 2026 r.)

### 35.1. Kontrola stanu wyjściowego

Przed rozpoczęciem etapu ponownie sprawdzono PR #68, gałąź `feature/payment-ledger-stage-1`, audyt i workflow dla rewizji `32143b915c40ec70c985e9276c5a317fd1778c8c`.

Dla tej rewizji:
- Repository compliance / REUSE — zakończone sukcesem;
- Android prototype build — zakończony sukcesem; testy JVM oraz budowa debug APK zakończyły się sukcesem;
- artefakt `obywatel-android-debug` został przypisany dokładnie do SHA `32143b915c40ec70c985e9276c5a317fd1778c8c`;
- Android GUI validation — testy Compose na emulatorze pozostawały w toku w chwili rozpoczęcia etapu 2L.

Artefakt poprzedniej rewizji nie jest używany jako dowód dla nowych zmian etapu 2L.

### 35.2. Wykryte ryzyko identyfikacji operacji nadrzędnej

Przegląd `BankStatementEntry.exchangeOperationId()` wykazał, że identyfikator operacji FX był wyliczany wyłącznie z referencji bankowej. Referencja może zostać ponownie użyta w innym dniu, dlatego samo grupowanie po referencji groziło połączeniem niezależnych operacji. Jednocześnie nogi jednej wymiany mogą występować w różnych lokalnych zakresach rachunków, więc zakres rachunku nie może być warunkiem wspólnego identyfikatora operacji.

### 35.3. Wykonane zmiany

1. **Zakres identyfikatora FX**
   - identyfikator operacji jest teraz wyliczany z daty księgowania i znormalizowanej referencji; celowo pomija pseudonim rachunku, aby zachować łączenie nóg wymiany z różnych zakresów;
   - identyfikator nadal jest skrótem SHA-256 i nie zawiera jawnej referencji;
   - dwie nogi tej samej operacji w tym samym rachunku i dniu zachowują wspólny identyfikator;
   - ta sama referencja w innym dniu otrzymuje odrębny identyfikator, a nogi z różnych zakresów rachunków w tym samym dniu nadal mogą tworzyć jedną operację;

2. **Widok grup FX w ekranie**
   - ekran wylicza projekcję `BankExchangeOperationProjector.project(entries)` z danych odczytanych z SQLite;
   - operacje FX są prezentowane jako grupy nadrzędne;
   - każda grupa pokazuje status: kompletna, niepełna — jedna noga, albo wymagająca weryfikacji;
   - przycisk rozwija lub zwija oryginalne zapisy źródłowe z datą, walutą, kwotą ze znakiem i typem operacji;
   - nogi już pokazane w grupie są wyłączone z osobnej listy zapisów, aby nie wyświetlać ich podwójnie;
   - zapisy źródłowe pozostają odrębnymi rekordami; UI nie sumuje ich, nie usuwa i nie przelicza.

3. **Minimalizacja prezentacji danych**
   - w nowej liście zapisów i rozwiniętych nogach nie wyświetla się pola opisu bankowego, które może zawierać dane kontrahenta;
   - zachowano datę, walutę, kwotę, typ operacji i status grupy.

### 35.4. Test regresyjny

Rozszerzono `BankExchangeOperationTest.kt` o test tej samej referencji:
- dwie przeciwne nogi FX z różnych pseudonimów rachunków i tego samego dnia powinny utworzyć jedną grupę `COMPLETE`;
- ponowne użycie referencji w kolejnym dniu powinno utworzyć odrębną grupę `SINGLE_LEG`.

Test chroni jednocześnie przed błędnym rozdzieleniem nóg między zakresami i przed scaleniem ponownie użytej referencji z innego dnia. Istniejący test pary 8 000 USD / 6 183,20 CHF nadal sprawdza zachowanie dwóch odrębnych identyfikatorów księgowych.

### 35.5. Zapis i kontrola źródeł

Zmiany zapisano na `feature/payment-ledger-stage-1`:
- `PaymentLedgerScreen.kt` — commit `1d89b0ed5b2868aab856fca27d724b22a84cdd71`;
- `BankImportDeduplicator.kt` — commit `7e3c1f0b95b2c3a28bb324ecbf874db9f043c554`;
- `BankExchangeOperationTest.kt` — commit `f7f65d7f8e54819e2ae89f926fab3b2fe03847aa`;
- `PaymentLedgerDatabase.kt` — commit `dce1d4194ec32fd5f091d264cd295a4a9df6fc21`;
- `PaymentLedgerDatabaseTest.kt` — commit `d179bb6207e8d4e71ff7dc09efb5ed4c38ebd6b1`;

Po zapisaniu ponownie pobrano wszystkie zmienione pliki z GitHub. Kontrola treści potwierdziła:
- wyświetlanie grup FX i statusów;
- rozwijanie źródłowych nóg operacji;
- wykluczanie nóg z osobnej listy;
- brak prezentacji surowego opisu w nowym widoku;
- włączenie daty i referencji do identyfikatora operacji przy zachowaniu możliwości parowania nóg z różnych zakresów;
- obecność testu parowania nóg z różnych zakresów oraz rozdzielenia ponownie użytej referencji między dniami.

### 35.6. Dodatkowa korekta klasyfikacji i migracji

Przegląd ekranu ujawnił, że `exchangeOperationId()` zwracał identyfikator dla każdego wpisu z referencją, również dla zwykłego przelewu. Projektor mógł zatem prezentować zwykłe operacje jako niepełne wymiany walut. Skorygowano regułę: identyfikator nadrzędny powstaje wyłącznie dla typu zawierającego oznaczenie `WYMIANA W KANTORZE` i niepustej referencji.

Ponieważ SQLite przechowuje również wyliczony `exchange_operation_id`, sama zmiana funkcji nie wystarczała dla już zapisanych rekordów. Podniesiono wersję bazy z 2 do 3 i dodano migrację, która odtwarza identyfikatory z pól źródłowych oraz czyści identyfikatory dla zwykłych operacji. Nie zmienia ona `accounting_entry_id`, kwot ani zapisów źródłowych.

Dodano test jednostkowy potwierdzający, że zwykły przelew z referencją nie jest projektowany jako FX, oraz test instrumentacyjny migracji wersji 2 → 3 sprawdzający aktualizację identyfikatora FX i wyczyszczenie pola dla przelewu. Testy wymagają potwierdzenia w CI.

### 35.7. Status weryfikacji i ograniczenia

| Element | Status |
|---|---|
| Projekcja grup FX na ekranie | Zaimplementowana |
| Rozwijanie i zwijanie źródłowych nóg | Zaimplementowane |
| Statusy COMPLETE / SINGLE_LEG / REVIEW_REQUIRED | Wyświetlane |
| Brak podwójnego wyświetlania nóg FX | Zaimplementowano filtrowanie |
| Ograniczenie identyfikatora FX do operacji kantorowych, daty i referencji (bez zakresu rachunku) | Zaimplementowane |
| Test regresyjny nóg z różnych zakresów i ponownego użycia referencji w innym dniu | Dodany; oczekuje na CI |
| Test zwykłego przelewu z referencją (wykluczenie z FX) | Dodany; oczekuje na CI |
| Migracja bazy 2 → 3 dla identyfikatorów FX | Zaimplementowana; test instrumentacyjny oczekuje na CI |
| Testy jednostkowe dla nowej rewizji | Oczekują na CI |
| Testy Compose na emulatorze dla nowej rewizji | Oczekują na CI |
| APK dla nowej rewizji etapu 2L | Niepotwierdzony |
| Ręczny test importu SAF na urządzeniu | Nadal do wykonania |
| Trwała tabela nadrzędnych operacji FX | Nie dodano; grupy są projekcją odczytową |

Zmiana identyfikatora nie zmienia kluczy księgowych ani danych źródłowych; wymagała jednak migracji pola pomocniczego `exchange_operation_id`, ponieważ jest ono utrwalane w SQLite. Migracja wersji 2 → 3 została dodana. Przed wprowadzeniem trwałej tabeli operacji FX należy nadal zweryfikować, czy w innych wariantach wyciągów nogi jednej wymiany zawsze mają tę samą datę księgowania; pseudonim rachunku może być różny i nie jest częścią identyfikatora nadrzędnego.

### 35.7. Następny etap

Po zielonym CI dla etapu 2L należy przejść do trwałego modelu transakcji finansowych i prowizji, powiązanego z zapisami bankowymi bez ich scalania. Równolegle należy wykonać test urządzeniowy SAF. Adapter PDF powinien być oparty na rzeczywistym wzorcu i testach ekstrakcji; parserów CSV/XLS nie należy implementować bez plików wzorcowych.

## 36. Etap 2M — trwały model transakcji finansowych, prowizji i relacji do zapisów bankowych (14 listopada 2026 r.)

### 36.1. Cel i zakres

Celem etapu jest oddzielenie pojedynczego zapisu księgowego z wyciągu bankowego od transakcji finansowej opisującej zdarzenie ekonomiczne. Transakcja może mieć stronę oddaną i otrzymaną, prowizję oraz jeden lub więcej powiązanych zapisów źródłowych. Nie wolno usuwać ani scalać źródłowych nóg FX.

### 36.2. Zmiany w SQLite — migracja 3 → 4

Podniesiono `PaymentLedgerDatabase.DATABASE_VERSION` z 3 do 4. Dodano migrację sekwencyjną, która tworzy trzy tabele:

1. `financial_transactions`
   - klucz główny `transaction_id`;
   - data i czas wykonania jako ISO-8601;
   - typ transakcji, aktywo i kwota strony oddanej oraz otrzymanej;
   - źródło, identyfikator zewnętrzny, opcjonalny opis i status parsowania;
   - kwoty są przechowywane jako tekst dziesiętny, bez konwersji do `Float`/`Double`.

2. `financial_transaction_fees`
   - relacja 1:1 do transakcji;
   - kod aktywa prowizji i kwota jako tekst dziesiętny;
   - klucz obcy z `ON DELETE CASCADE`.

3. `financial_transaction_bank_entries`
   - relacja wiele zapisów bankowych do jednej transakcji;
   - klucze obce do transakcji i `bank_statement_entries`;
   - `accounting_entry_id` ma ograniczenie `UNIQUE`, zatem jeden zapis bankowy nie może zostać przypisany do wielu transakcji;
   - usunięcie transakcji usuwa wyłącznie relacje, a nie źródłowe zapisy bankowe.

Włączono egzekwowanie kluczy obcych SQLite przez `setForeignKeyConstraintsEnabled(true)`. Migracja nie usuwa ani nie przepisuje istniejących zapisów wyciągów i aliasów rachunków.

### 36.3. Walidacja domenowa

Rozszerzono `FinancialTransaction.validate()`:
- `EXCHANGE`, `METAL_BUY` i `METAL_SELL` wymagają obu stron i różnych kodów aktywów;
- `DEPOSIT` wymaga wyłącznie strony otrzymanej;
- `WITHDRAWAL` wymaga wyłącznie strony oddanej;
- prowizja musi być nieujemna i wyrażona w aktywie jednej ze stron transakcji;
- istniejące wymagania dodatnich ilości i parowania kodu aktywa z kwotą pozostają zachowane.

### 36.4. Atomowy zapis i kontrola powiązań

Dodano w `PaymentLedgerDatabase`:
- `saveFinancialTransaction(transaction, accountingEntryIds)` — waliduje model, sprawdza istnienie zapisów bankowych, sprawdza zgodność waluty, wartości bezwzględnej kwoty i kierunku (obciążenie/uznanie) z jedną ze stron transakcji albo prowizją, a następnie zapisuje transakcję, prowizję i relacje w jednej transakcji SQLite;
- `readFinancialTransactions()` — odtwarza model transakcji, prowizję i listę identyfikatorów źródłowych zapisów.

Powiązanie jest odrzucane, gdy identyfikator źródłowy nie istnieje albo jego waluta/kwota/kierunek nie odpowiadają transakcji. Błąd powoduje wycofanie całego zapisu. Ograniczenie unikalności zapobiega wielokrotnemu przypisaniu tego samego zapisu bankowego.

### 36.5. Repozytorium i ekran

Dodano w `PaymentLedgerRepository` metody `saveFinancialTransaction` i `getFinancialTransactions`. Operacje SQLite są wykonywane przez `Dispatchers.IO`.

Ekran `PaymentLedgerScreen` odczytuje zapisane transakcje i pokazuje:
- typ i czas transakcji;
- kwotę oraz aktywo oddane;
- kwotę oraz aktywo otrzymane;
- prowizję wraz z aktywem;
- liczbę powiązanych zapisów bankowych.

Na tym etapie ekran wyświetla istniejące transakcje, ale nie zawiera jeszcze formularza ich ręcznego tworzenia ani edycji. Nie rozszerzono jeszcze wersjonowanego JSON: obecny `PaymentLedgerJsonCodec` nadal obsługuje zapisy wyciągów bankowych, a transakcje i prowizje nie są jeszcze częścią eksportu/importu JSON.

### 36.6. Testy dodane

- `FinancialModelsTest.kt`: test wymogu dwóch różnych aktywów dla wymiany, zgodności aktywa prowizji oraz reguł stron wpłaty/wypłaty;
- `PaymentLedgerDatabaseTest.kt`: test trwałości transakcji, prowizji i relacji po ponownym otwarciu bazy;
- test odrzucenia nieistniejącego zapisu bankowego i wycofania transakcji;
- test odrzucenia zapisu bankowego o niezgodnej walucie/kwocie;
- test migracji wersji 3 → 4, zachowania istniejącego zapisu bankowego i możliwości utworzenia transakcji po migracji. W kontroli testu poprawiono asercję: identyfikator księgowy jest deterministycznie wyliczany z pól wpisu, więc test migracji sprawdza zachowanie waluty i wartości, a relację FK weryfikuje po istniejącym kluczu zapisanym w SQLite.

Są to testy dodane do repozytorium; ich wykonanie wymaga wyniku GitHub Actions dla rewizji kodowej etapu 2M.

### 36.7. Zmienione pliki i commity

- `PaymentLedgerDatabase.kt` — `6f3ec4bfa6ed21c74da77bad3c72416077bfef1b`;
- `PaymentLedgerRepository.kt` — `457f28bba3e6ac8a824f7b87a5b6080cfa51d390`;
- `FinancialModels.kt` — `7cbc9347c7fe710904417aa5a0cbaea4c84974c1`;
- `PaymentLedgerScreen.kt` — `c07c1e771b2ec06399a120d81aa579ea1fd84462`;
- `PaymentLedgerDatabaseTest.kt` — `f088ef50a831b913926499d3d2831664147ca31b`;
- `FinancialModelsTest.kt` — `8d4839cb774a7e84cb7a9ec6eaebdfba6e7f01c5`;
- `BankImportDeduplicatorTest.kt` — `3bd0cc3cce34ef0cd728d9286c077a0a33f42ae1`.

Po zapisie ponownie pobrano wszystkie sześć plików z GitHub i sprawdzono obecność migracji 3 → 4, tabel, ograniczeń kluczy obcych, metod repozytorium, prezentacji transakcji i prowizji oraz testów. Kontrola treści potwierdziła również, że wersja schematu wynosi 4.

### 36.8. Status CI i ograniczenia

Kontrolowana rewizja kodowa po korekcie testu tożsamości FX: `3bd0cc3cce34ef0cd728d9286c077a0a33f42ae1`.

| Kontrola | Stan w chwili aktualizacji audytu |
|---|---|
| Repository compliance / REUSE | Sukces |
| Android prototype build — JVM unit tests | Sukces dla SHA `3bd0cc3`; 133 testy, bez błędów po korekcie testu daty FX |
| Android prototype build — debug APK | W toku podczas aktualizacji audytu |
| Android GUI validation — JVM unit tests | Sukces dla SHA `3bd0cc3` |
| Android GUI validation — Compose/emulator | W toku podczas aktualizacji audytu |
| Testy Compose | Oczekują na wykonanie |
| Test migracji SQLite 3 → 4 | Dodany; oczekuje na CI |
| Debug APK | Brak artefaktu dla tej rewizji w chwili kontroli |

Wcześniejsze zadania dla SHA `65911c8` zostały anulowane podczas aktualizacji gałęzi i nie są dowodem dla etapu 2M. Nie oznacza się etapu 2M jako przetestowanego ani nie publikuje APK przed uzyskaniem wyników dla aktualnego kodowego SHA.

### 36.9. Dalsze prace

1. Uzyskać zielone CI dla migracji i testów etapu 2M; poprawić ewentualne błędy z logów.
2. Dodać formularz ręcznego tworzenia i edycji transakcji, prowizji oraz wiązania z zapisami bankowymi.
3. Rozszerzyć wersjonowany JSON o transakcje i prowizje z zachowaniem kompatybilności wstecznej.
4. Dodać trwały rejestr aktywów i reguły precyzji dla walut oraz kruszców.
5. Następnie przejść do parsera PDF opartego na rzeczywistym wzorcu iPKO oraz importu CSV/XLS wyłącznie na podstawie dostępnych plików wzorcowych.


## 37. Etap 2N.1 — kompletne archiwum JSON dla transakcji finansowych (14 listopada 2026 r.)

### 37.1. Uzasadnienie i wykryta luka

Kontrola rewizji PR przed etapem wykazała, że SQLite przechowywał już transakcje finansowe, prowizje i relacje do zapisów bankowych, natomiast PaymentLedgerJsonCodec eksportował wyłącznie tablicę entries. Kopia JSON nie obejmowała więc całego rejestru. Rozszerzenie wykonano bez zmiany roli SQLite jako lokalnego źródła prawdy.

### 37.2. Zmiany wykonane

1. Podniesiono wersję formatu JSON z 1 do 2.
2. Schemat 2 zawiera:
   - entries — źródłowe zapisy wyciągów;
   - transactions — transakcje finansowe;
   - strony aktywów i kwoty oddane/otrzymane;
   - prowizję wraz z kodem aktywa i kwotą;
   - źródło, status parsowania, czas wykonania i identyfikator zewnętrzny;
   - listę accountingEntryIds wiążącą transakcję z zapisami źródłowymi.
3. Kwoty pozostają tekstami dziesiętnymi i są odtwarzane jako BigDecimal; nie wprowadzono Float ani Double.
4. decodeArchive() waliduje wersję, strukturę, typ transakcji, prowizję i unikalność identyfikatorów powiązań.
5. Zachowano decode() jako dotychczasowe API zwracające wpisy bankowe, aby nie zmieniać parserów i podglądu importu.
6. Archiwum schematu 1 pozostaje czytelne; brak tablicy transactions w wersji 1 oznacza pustą listę transakcji.
7. Dodano PaymentLedgerDatabase.importArchive(): wpisy bankowe, transakcje, prowizje i relacje są importowane w ramach jednej transakcji SQLite. Konflikt referencji, nieprawidłowa relacja albo niezgodność kwoty/waluty powoduje wycofanie całego archiwum.
8. PaymentLedgerRepository.exportJson() eksportuje teraz wpisy bankowe i zapisane transakcje; importJson() dekoduje pełne archiwum i korzysta z atomowego importu.

### 37.3. Testy dodane lub zmienione

- test pełnego round-trip schematu 2, obejmujący transakcję wymiany, prowizję i relację do zapisu bankowego;
- test odczytu archiwum schematu 1;
- test odrzucenia nieprawidłowej transakcji;
- test integracyjny eksportu/importu transakcji i prowizji przez repozytorium;
- test idempotentnego ponownego importu archiwum;
- test rollback całego archiwum, gdy transakcja wskazuje nieistniejący zapis bankowy;
- dostosowano test nieznanej wersji schematu do nowej wersji 2.

### 37.4. Pliki objęte etapem

- financial/PaymentLedgerJsonCodec.kt;
- financial/PaymentLedgerDatabase.kt;
- financial/PaymentLedgerRepository.kt;
- test/.../financial/PaymentLedgerJsonCodecTest.kt;
- androidTest/.../financial/PaymentLedgerRepositoryTest.kt.

### 37.5. Ograniczenia i zakres niewykonany

Etap 2N.1 zamyka lukę kompletności kopii JSON dla istniejącego modelu transakcji. Nie dodaje jeszcze formularza ręcznego tworzenia/edycji transakcji i operacji kruszcowych, rejestru darowizn w SQLite/JSON, edycji aktywów ani interfejsu wyboru powiązanych zapisów bankowych. Nie należy oznaczać całego etapu 2N jako ukończonego.

Import archiwum wykorzystuje transakcję zewnętrzną SQLite i istniejącą metodę zapisu transakcji. Należy potwierdzić zachowanie zagnieżdżonych transakcji na emulatorze w testach instrumentacyjnych.

### 37.6. Status weryfikacji

Zmiany zapisano na gałęzi PR #68. Testy jednostkowe i instrumentacyjne zostały dodane, ale ich wykonanie dla nowej rewizji wymaga świeżego CI. Do czasu uzyskania wyników nie oznacza się testów jako zaliczonych ani nie publikuje APK dla tej rewizji.

### 37.7. Następny podetap

Następnie należy wykonać formularze tworzenia i edycji transakcji walutowych oraz kupna/sprzedaży kruszców, z wyborem aktywów, datą, kwotami, prowizją i powiązaniami bankowymi. Formularze muszą używać walidacji domenowej i zapisu przez repozytorium; edycja nie może usuwać źródłowych zapisów bankowych.


### 37.8. Kontrola końcowa plików i testów

Po dodaniu testów ponownie pobrano pliki z gałęzi i sprawdzono:
- wersję schematu 2 oraz obsługę wersji 1 w dekoderze;
- zachowanie dotychczasowego API decode() używanego przez importer bankowy;
- obecność pełnego eksportu w repozytorium;
- obecność importArchive() w bazie oraz użycie transakcji SQLite dla całego archiwum;
- testy JSON v2, zgodności wstecznej, walidacji transakcji, idempotencji i rollback;
- poprawienie testu odrzucania nieznanej wersji schematu — oczekuje on wersji 99, a nie wersji 1.

Nie przeprowadzono lokalnej kompilacji w środowisku roboczym tej sesji. GitHub Actions uruchomił walidację dla rewizji audytu, ale wyniki kompilacji, testów JVM i testów emulatorowych trzeba odczytać dla końcowego SHA po zakończeniu zadań. Wynik REUSE jest sukcesem dla rewizji audytu; nie zastępuje testów aplikacji.


## 38. Etap 2N.2 — bezpieczna aktualizacja transakcji finansowej (14 listopada 2026 r.)

### 38.1. Kontrola poprzedzająca

Przegląd kodu etapu 2N.1 wykazał, że zapis istniejącej transakcji używał `SQLiteDatabase.CONFLICT_REPLACE`. SQLite realizuje strategię REPLACE jako usunięcie istniejącego wiersza i wstawienie nowego. Przy relacjach z kluczami obcymi może to uruchamiać akcje usuwania i nie jest równoważne aktualizacji rekordu.

### 38.2. Wykonana korekta

W `PaymentLedgerDatabase.saveFinancialTransaction()`:
- usunięto zapis `CONFLICT_REPLACE` dla tabeli transakcji finansowych;
- zastosowano najpierw `UPDATE` rekordu po `transaction_id`;
- jeżeli rekord nie istnieje, wykonywany jest zwykły `INSERT`;
- wymiana prowizji i relacji zapisów bankowych odbywa się jawnie wewnątrz tej samej transakcji SQLite;
- zachowano walidację źródłowych kwot, walut, kierunków, istnienia wpisów i unikalności powiązań;
- nie zmieniono tabeli ani identyfikatorów źródłowych zapisów bankowych.

### 38.3. Test regresyjny

Do `PaymentLedgerDatabaseTest.kt` dodano `updatingFinancialTransactionPreservesRecordAndReplacesOnlyItsExplicitLinks`:
- zapisuje parę źródłowych wpisów USD/CHF;
- tworzy transakcję z prowizją i relacjami do obu wpisów;
- ponownie zapisuje tę samą transakcję po zmianie statusu i usunięciu prowizji;
- sprawdza zachowanie jednego rekordu transakcji, obu jawnych relacji i dwóch niezmienionych wpisów źródłowych.

Test jest testem instrumentacyjnym Android. Jego dodanie nie oznacza jeszcze wykonania.

### 38.4. Pliki i rewizje

- `PaymentLedgerDatabase.kt` — commit `659c573d63b3ebec9eac8652f6075ea4eb8c50c6`, blob `907b41878e0e9700c88a861848e1364a6bac595c`;
- `PaymentLedgerDatabaseTest.kt` — commit `c14849e8369a1d7282d0505cefe21ae4bfb3c80b`, blob `74378613a49898ae67a0cbb8ba78a409e654fa86`;
- gałąź: `feature/payment-ledger-stage-1`;
- PR: #68.

Pliki zostały ponownie pobrane z GitHub. Kontrola treści potwierdziła brak `CONFLICT_REPLACE` w metodzie `saveFinancialTransaction()`, obecność sekwencji `UPDATE`/`INSERT` oraz obecność testu regresyjnego.

### 38.5. Status weryfikacji

| Kontrola | Status |
|---|---|
| Kontrola źródła po zapisie | Sukces — oba pliki pobrane ponownie |
| Test regresyjny aktualizacji transakcji | Dodany; oczekuje na CI nowego SHA |
| Testy JVM | Oczekują na CI nowego SHA |
| Testy instrumentacyjne SQLite | Oczekują na CI nowego SHA |
| Testy Compose | Oczekują na CI nowego SHA |
| Debug APK | Oczekuje na budowę z nowego SHA |

Ostatni kompletny CI dla wcześniejszego SHA `4e76e02fa9dfe820331254c45ccc63ba31764c48` zakończył się sukcesem:
- REUSE — sukces;
- Android JVM unit tests — sukces;
- Compose UI tests na emulatorze — sukces;
- 54 testy instrumentacyjne — 0 pominiętych, 0 błędnych;
- debug APK — zbudowany i zarchiwizowany.

Wynik ten nie obejmuje korekty etapu 2N.2. Dla nowego SHA nie uzyskano jeszcze danych workflow. Nie wolno przenosić statusu zielonego CI ze starszej rewizji na nową.

### 38.6. Aktualny następny etap

Po zielonym CI dla 2N.2 należy wykonać formularz ręcznego dodawania i edycji transakcji:
- wymiany walutowej;
- kupna i sprzedaży kruszców;
- wpłaty i wypłaty;
- prowizji;
- wyboru istniejących zapisów bankowych do powiązania.

Formularz powinien używać `FinancialTransaction.validate()` i `PaymentLedgerRepository.saveFinancialTransaction()`, a edycja musi zachować źródłowe zapisy bankowe. Rejestr aktywów, darowizny i kursy NBP pozostają odrębnymi etapami.


## 39. Etap 2N.3 — formularz ręcznego dodawania i edycji transakcji (14 listopada 2026 r.)

### 39.1. Zakres wykonany

W pliku PaymentLedgerScreen.kt dodano formularz ręcznego tworzenia i edycji transakcji z następującymi polami:
- typ operacji: wymiana, kupno/sprzedaż kruszcu, wpłata, wypłata i korekta;
- data i czas w formacie ISO-8601;
- aktywo i ilość strony oddanej;
- aktywo i ilość strony otrzymanej;
- aktywo i kwota prowizji;
- wybór istniejących zapisów bankowych do powiązania.

Zapis realizowany jest przez PaymentLedgerRepository.saveFinancialTransaction(). Przed zapisem uruchamiana jest walidacja FinancialTransaction.validate(). Kwoty są parsowane jako BigDecimal, a czas jako Instant; nie używa się typów zmiennoprzecinkowych do wartości pieniężnych.

### 39.2. Edycja i pochodzenie danych

- istniejące transakcje mają akcję „Edytuj”;
- formularz odtwarza wartości transakcji oraz identyfikatory powiązanych zapisów bankowych;
- aktualizacja używa dotychczasowego identyfikatora transakcji;
- edycja zachowuje źródło, identyfikator zewnętrzny i opis źródłowy transakcji zaimportowanej;
- anulowanie edycji czyści stan formularza;
- zapisy bankowe są wybierane jawnie, a nie usuwane lub modyfikowane przez formularz.

### 39.3. Plik i rewizja

- obywatel-android/app/src/main/java/pl/gov/obywatel/android/PaymentLedgerScreen.kt;
- commit formularza: 38adc8f6306ead6560ca9a14a4a8bbbd0e81b775;
- commit korekty zachowania pochodzenia i wyświetlania pozycji: fdad4492e4e356151f9a31e6818285395e5f8f44;
- blob końcowy ekranu po ponownym odczycie: f31f793dcad2ee0c819583b3d04f0d5ece2ade71;
- gałąź: feature/payment-ledger-stage-1;
- PR: #68.

### 39.4. Kontrola po zapisie

Plik ekranu ponownie pobrano z GitHub i potwierdzono:
- obecność formularza dodawania i edycji;
- wywołanie walidacji domenowej przed zapisem;
- wywołanie repozytorium dla trwałego zapisu;
- użycie BigDecimal i Instant;
- wyświetlanie daty, kwoty i waluty wybranego wpisu bankowego;
- zachowanie source, externalId i rawDescription przy edycji istniejącej transakcji;
- odtworzenie powiązań bankowych podczas edycji.

### 39.5. Status testów i ograniczenia

| Kontrola | Status |
|---|---|
| Ponowny odczyt pliku ekranu | Sukces |
| Kontrola statyczna kluczowych ścieżek | Sukces |
| Testy jednostkowe dla końcowego SHA | Oczekują na CI |
| Testy Compose dla końcowego SHA | Oczekują na CI |
| Testy instrumentacyjne SQLite dla końcowego SHA | Oczekują na CI |
| Budowa debug APK dla końcowego SHA | Oczekuje na CI |

Poprzednia rewizja f7fe7557b0dc962ada0d1ab897306260a2e4a54f miała rozpoczęte testy Compose; testy jednostkowe oraz kontrola REUSE zakończyły się sukcesem, natomiast testy emulatorowe i budowa APK były w toku. Nie jest to wynik dla aktualnej rewizji fdad4492e4e356151f9a31e6818285395e5f8f44.

Formularz nie został jeszcze potwierdzony testami UI na emulatorze. Nie oznacza się etapu 2N.3 jako w pełni przetestowanego. Wymagane są testy formularza dla każdego typu operacji, walidacji brakujących/niepoprawnych kwot, edycji transakcji importowanej oraz zachowania powiązań bankowych.

### 39.6. Następny krok

Po zielonym CI dla aktualnego SHA należy:
1. dodać lub rozszerzyć testy Compose formularza;
2. sprawdzić na emulatorze wymianę, kupno/sprzedaż kruszcu, wpłatę i wypłatę;
3. sprawdzić edycję transakcji importowanej bez utraty pochodzenia i powiązań;
4. dopiero potem rozpocząć trwały rejestr aktywów i formularz darowizn.


---

## 40. Etap 2N.4 — ochrona przed ponownym przypisaniem zapisu bankowego (14 listopada 2026 r.)

### Ustalenie audytowe

Weryfikacja formularza ręcznej edycji ujawniła rozbieżność między interfejsem a ograniczeniem bazy danych. Tabela `financial_transaction_bank_entries` definiuje `accounting_entry_id` jako `UNIQUE`, czyli jeden zapis bankowy może należeć najwyżej do jednej transakcji finansowej. Formularz wcześniej wyświetlał wszystkie zapisy bankowe, także już przypisane do innej transakcji. Próba ich ponownego wybrania kończyła się błędem ograniczenia SQLite dopiero podczas zapisu.

### Wykonana korekta

- Dodano w ekranie mapę właścicieli powiązań na podstawie odczytanych transakcji.
- Lista wyboru pokazuje wyłącznie zapisy nieprzypisane albo przypisane do transakcji aktualnie edytowanej.
- Zachowano możliwość odznaczenia i ponownego wybrania powiązań tej samej transakcji.
- Dodano stan pusty, gdy wszystkie zapisy są już przypisane do innych transakcji.
- Nie zmieniano schematu SQLite ani reguły integralności; ograniczenie `UNIQUE` pozostaje zabezpieczeniem po stronie bazy danych.

### Kontrola zakresu

Korekta dotyczy wyłącznie prezentacji dostępnych powiązań. Walidacja zgodności waluty i kwoty oraz atomowy zapis transakcji i relacji pozostają po stronie `PaymentLedgerDatabase.saveFinancialTransaction`. Zmiana nie przenosi danych ani nie usuwa istniejących relacji.

### Weryfikacja

- Zmiana ekranu zapisana w commicie: `462bbe2ce1dae815ea8184388e5d119802d6b616`.
- Ponowny odczyt pliku ekranu i audytu należy wykonać po zapisie.
- GitHub Actions dla SHA `951c8eaad88713198efdf234b4fdeb4e0a6fbfba` pozostawały w toku w chwili rozpoczęcia tego etapu; ich wynik nie obejmuje nowego commita i nie stanowi potwierdzenia kompilacji tej korekty.
- Testy jednostkowe, instrumentacyjne, Compose i kompilacja APK dla nowego SHA pozostają do potwierdzenia przez CI.

**Status etapu 2N.4:** korekta zapisana; weryfikacja automatyczna oczekuje na uruchomienie/zakończenie CI. PR #68 pozostaje otwarty i niescalony.


---

## 41. Etap 2N.5 — trwały rejestr aktywów walutowych i kruszcowych (14 listopada 2026 r.)

### 41.1. Cel i zakres

Rozpoczęto realizację trwałego słownika aktywów wykorzystywanych w transakcjach finansowych. Dotychczas model `Asset` istniał w warstwie domenowej, lecz brakowało tabeli SQLite i metod repozytorium, które zachowywałyby definicje walut i kruszców pomiędzy uruchomieniami aplikacji.

### 41.2. Wykonane zmiany

- Podniesiono wersję bazy `PaymentLedgerDatabase` z 4 do 5.
- Dodano tabelę `financial_assets` z polami: kod aktywa (klucz główny), nazwa, rodzaj, jednostka, precyzja dziesiętna, próba kruszcu i czas aktualizacji.
- Dodano indeks po rodzaju aktywa.
- Dodano migrację z wersji 4 do 5, która tworzy tabelę aktywów bez usuwania dotychczasowych tabel i danych.
- Dodano `saveAsset()`: waliduje model, aktualizuje istniejący kod lub wstawia nowy rekord.
- Dodano `readAssets()`: odtwarza modele `Asset` w stabilnej kolejności.
- Udostępniono `getAssets()` i `saveAsset()` przez `PaymentLedgerRepository`; operacje repozytorium działają na `Dispatchers.IO`.
- Zachowano wartości ilościowe jako parametry modelu i pola całkowite/tekstowe; nie wprowadzono typów zmiennoprzecinkowych.

### 41.3. Testy dodane

W `PaymentLedgerDatabaseTest.kt` dodano:
- `assetRegistryPersistsAndUpdatesCurrencyAndMetalDefinitions` — zapis waluty i kruszcu, odczyt, aktualizację nazwy/precyzji istniejącej waluty oraz ponowny odczyt po zamknięciu i otwarciu bazy;
- `versionFourMigrationCreatesAssetRegistryWithoutDroppingLegacyTables` — migrację bazy v4 do v5, zapis aktywa po migracji i sprawdzenie zachowania przykładowej tabeli starszej wersji.

Testy zostały dodane do zestawu instrumentacyjnego Android; ich obecność nie oznacza, że zostały już wykonane.

### 41.4. Pliki i rewizje

- `PaymentLedgerDatabase.kt` — commit `54d4c5007455884bcd064597f0ff19fe3e35f1b2`, blob `74479d38fbc33d0b1b416cdbf999d65aef06da5c`;
- `PaymentLedgerRepository.kt` — commit `b754a4564f4665e2b1674e9a663538d2f7982f43`, blob `7e79508e6a4cc3bde4b78ff955a1001ae088dc1c`;
- `PaymentLedgerDatabaseTest.kt` — commit `6111e6d3aa2b7af703790e35ff31aa966e2d6648`, blob `78b39af59e432c806fbd880914dc0ebddf2b1885`;
- gałąź: `feature/payment-ledger-stage-1`; PR #68.

### 41.5. Kontrola po zapisie i status CI

Wszystkie trzy pliki ponownie pobrano z GitHub. Potwierdzono obecność tabeli i metod rejestru aktywów, metod repozytorium oraz obu nowych testów.

Dla końcowego SHA `6111e6d3aa2b7af703790e35ff31aa966e2d6648` GitHub Actions utworzył:
- Repository compliance (alfa), run `43633661192` — oczekuje;
- Android GUI validation, run `43633661151` — w kolejce;
- Android prototype build, run `43633661210` — w kolejce.

Wcześniejsze uruchomienia na pośrednich commitach zostały anulowane wskutek kolejnych aktualizacji gałęzi. Nie są wynikiem testowym dla końcowego SHA. Nie potwierdzono jeszcze kompilacji, testów JVM, testów instrumentacyjnych, testów Compose ani APK dla rewizji `6111e6d3aa2b7af703790e35ff31aa966e2d6648`.

### 41.6. Ograniczenia i dalszy zakres

Ten etap dostarcza trwałą warstwę danych i API repozytorium, ale nie kończy obsługi rejestru aktywów w interfejsie. Do wykonania pozostają:
1. ekran przeglądania, dodawania i edycji aktywów;
2. wybór aktywa ze słownika w formularzu transakcji zamiast swobodnego wpisywania kodu;
3. reguły słownika walut zgodne z aktualnym katalogiem ISO 4217 oraz katalog jednostek/standardów próby dla kruszców;
4. zabezpieczenie przed zmianą definicji aktywa w sposób naruszający historyczną interpretację transakcji;
5. testy UI i pełne wykonanie CI dla końcowego SHA.

**Status etapu 2N.5:** warstwa SQLite, migracja i API repozytorium zapisane; testy dodane; weryfikacja automatyczna oczekuje na zakończenie CI. PR #68 pozostaje otwarty i niescalony.


---

## 42. Etap 2N.6 — ochrona niezmienności definicji aktywów użytych w historii (14 listopada 2026 r.)

### 42.1. Ustalenie audytowe

Po wdrożeniu tabeli `financial_assets` metoda `saveAsset()` pozwalała nadpisać wszystkie pola definicji pod tym samym kodem aktywa. Zmiana jednostki, precyzji dziesiętnej, rodzaju albo próby po zapisaniu transakcji mogłaby zmienić interpretację historycznej ilości aktywa. Sam kod aktywa nie wystarcza do ochrony semantyki danych.

### 42.2. Wykonana korekta

W `PaymentLedgerDatabase.saveAsset()`:
- odczytywana jest poprzednia definicja aktywa w tej samej transakcji SQLite, w której następuje zapis;
- sprawdzane są odwołania do kodu aktywa w kolumnach `from_asset_code` i `to_asset_code` tabeli transakcji oraz w tabeli prowizji;
- jeśli aktywo jest użyte w historii, blokowana jest zmiana rodzaju, jednostki, liczby miejsc dziesiętnych i próby;
- dozwolona pozostaje korekta nazwy opisowej, która nie zmienia ilości ani jednostki historycznej;
- dla zmiany semantycznej komunikat wskazuje utworzenie osobnego kodu aktywa zamiast przepisywania historii;
- nowa definicja i aktualizacja istniejącej definicji są wykonywane atomowo.

Ochrona obejmuje zarówno aktywa stron transakcji, jak i aktywo prowizji. Nie dodano kluczy obcych do historycznych transakcji, ponieważ wcześniejsze rekordy i importy mogą zawierać kody spoza lokalnego słownika; wymuszenie takiej relacji wymagałoby osobnej migracji i strategii zgodności wstecznej.

### 42.3. Testy dodane

W `PaymentLedgerDatabaseTest.kt` dodano dwa testy instrumentacyjne:
- `usedAssetKeepsHistoricalMeasurementDefinitionImmutableButAllowsRename`: po użyciu waluty USD w transakcji wpłaty dopuszcza zmianę nazwy, odrzuca zmianę jednostki i precyzji oraz sprawdza zachowanie transakcji;
- `usedMetalCannotChangePurityOrUnit`: po użyciu złota próby 999 w transakcji kupna odrzuca zmianę próby i jednostki oraz potwierdza niezmienność zapisanej definicji.

Testy są dodane do źródeł, ale ich wykonanie wymaga Android CI/emulatora. Nie oznacza się ich jako zaliczonych na podstawie samej obecności.

### 42.4. Pliki i rewizje

- `PaymentLedgerDatabase.kt` — commit `5f13a8aaeff7669eb315edddbeb7c375e917db75`, blob `6348baf67433c0a19ee1efe44e2961be0a418b44`;
- `PaymentLedgerDatabaseTest.kt` — commit `0a6adee6cebf7daedb7bf17f54cfb0019bcc989a`, blob `a2d8c4511e71064c268b907b82aaa5d12785fd55`;
- gałąź: `feature/payment-ledger-stage-1`;
- PR: #68.

### 42.5. Kontrola po zapisie i status weryfikacji

Po zapisaniu zmian ponownie pobrano oba pliki z gałęzi. Kontrola treści potwierdziła obecność ochrony odwołań w transakcjach i prowizjach oraz obu testów regresyjnych. PR #68 pozostaje otwarty i niescalony.

Poprzednie przebiegi Android GUI validation i Android prototype build zostały anulowane przed wykonaniem właściwych testów (w jednym przypadku na kroku konfiguracji Gradle, w drugim podczas testów JVM). Nie są to wyniki pozytywne ani negatywne testów kodu. Dla nowej rewizji etapu 2N.6 wymagane jest świeże uruchomienie:
- testów JVM;
- testów instrumentacyjnych SQLite;
- testów Compose na emulatorze;
- kompilacji debug APK.

**Status etapu 2N.6:** korekta kodu i testy zapisane oraz ponownie odczytane; wykonanie testów automatycznych oczekuje na CI. Nie publikować APK przed potwierdzonym sukcesem budowania i testów.

### 42.6. Następny etap

Po potwierdzeniu CI należy kontynuować 2N.7:
1. ekran przeglądania i zarządzania aktywami;
2. wybór aktywów ze słownika w formularzu transakcji;
3. walidacja liczby miejsc dziesiętnych względem definicji aktywa;
4. testy Compose dla dodawania, edycji i wyboru aktywów;
5. dopiero w następnej kolejności rozważyć pełny katalog walut ISO 4217 i wersjonowanie definicji.


---

## 43. Etap 2N.7 — słownik aktywów w interfejsie i walidacja precyzji (14 listopada 2026 r.)

### 43.1. Korekta błędu wykrytego przez CI po etapie 2N.6

Przebiegi Androida dla SHA `8e3f61c30657484a20b1609a81426bab81b86dfb` zakończyły się niepowodzeniem na kompilacji Kotlin. Przyczyną było odwołanie do niezdefiniowanej stałej `TABLE_FINANCIAL_TRANSACTION_FEES` w metodzie ochrony historii aktywów. Faktyczna stała tabeli w bazie to `TABLE_TRANSACTION_FEES`.

Korekta:
- zastąpiono błędną nazwę rzeczywistą stałą `TABLE_TRANSACTION_FEES`;
- zachowano kontrolę odwołań w tabelach transakcji i prowizji;
- plik ponownie pobrano po zapisie i sprawdzono zgodność nazwy z deklaracją stałej.

### 43.2. Interfejs zarządzania aktywami

W `PaymentLedgerScreen.kt` dodano:
- sekcję „Słownik walut i kruszców”;
- wybór rodzaju definicji: waluta lub kruszec;
- pola kodu, nazwy, jednostki, precyzji oraz opcjonalnej próby;
- zapis przez `PaymentLedgerRepository.saveAsset()`;
- odświeżanie i wyświetlanie zarejestrowanych definicji.

### 43.3. Wybór aktywa w transakcji

W formularzu transakcji dodano wybór zarejestrowanych aktywów dla:
- aktywa oddawanego;
- aktywa otrzymywanego;
- aktywa prowizji.

Pola tekstowe pozostawiono jako możliwość wprowadzenia kodu, ale zapis jest blokowany, jeśli kod nie istnieje w słowniku. Dla ilości stron transakcji i prowizji sprawdzana jest maksymalna liczba miejsc dziesiętnych zgodnie z definicją aktywa. Wartości pozostają typu `BigDecimal`.

### 43.4. Ograniczenia i jawne założenia

Walidacja modelu waluty sprawdza trzyliterowy wzorzec kodu, ale nie pobiera aktualnego katalogu ISO 4217; nie należy utożsamiać tego z pełną walidacją kodów ISO. Słownik jest lokalny i wymaga wprowadzenia definicji przez użytkownika. Nie dodano automatycznego katalogu walut ani notowań NBP.

### 43.5. Pliki zmienione

- `PaymentLedgerDatabase.kt` — poprawka stałej tabeli prowizji; commit `d1ad7ac6fddfbd8e0e15136520939a8b31521c98`, blob `3b62942bbd001da901a910a1714bc8c652d0d470`;
- `PaymentLedgerScreen.kt` — UI słownika, selektory aktywów i kontrola precyzji; commit `ec0d4c9289e10a29e8578a0ea430245569f4ae30`, blob `c9b4160481becf9ffb60602af50a9e41aa82171a`.

### 43.6. Weryfikacja

Po zapisie wymagane są ponowne odczyty plików i świeże przebiegi CI dla końcowego SHA. Poprzedni CI wykazał błąd kompilacji Kotlin; nie zaliczono testów JVM ani instrumentacyjnych dla tej rewizji. Nie publikować APK przed zielonym wynikiem kompilacji i testów.

**Status etapu 2N.7:** kod interfejsu i walidacji zapisany; oczekuje na kompilację, testy i kontrolę UI w CI.

### 43.7. Dalsze prace

1. Wykonać świeże CI dla końcowego SHA i naprawić wszystkie wykryte błędy.
2. Dodać testy UI słownika, wyboru aktywów i walidacji precyzji.
3. Rozważyć wymuszenie wyboru wyłącznie z listy (bez swobodnego kodu), po zapewnieniu migracji lub inicjalnego zestawu aktywów.
4. Dodać kontrolowany katalog walut i jednostek/standardów kruszców, z datą wersji źródła.
5. Pozytywnie zweryfikować ochronę historycznych definicji również dla prowizji.


---

## 44. Etap 2N.8 — zgodność wsteczna aktywów historycznych i test prowizji (14 listopada 2026 r.)

### 44.1. Wyniki CI dla poprzedniej rewizji

Dla SHA `f83a3823812ba6b397c290ce1a2a5db75345a449` potwierdzono:
- Repository compliance — sukces (run `43635946184`);
- Android prototype build — sukces; testy JVM i budowanie debug APK zakończone powodzeniem (run `43635946207`);
- Android GUI validation — testy JVM zakończone powodzeniem, testy Compose na emulatorze pozostawały w toku podczas kontroli (run `43635946160`). Nie oznaczono całego zadania GUI jako zaliczonego.

### 44.2. Wykryte ryzyko zgodności wstecznej

W formularzu transakcji walidacja wymagała istnienia każdego kodu w lokalnym słowniku. Starsze transakcje zaimportowane przed wprowadzeniem słownika mogły zawierać poprawne historycznie kody, których użytkownik nie zarejestrował. Samo otwarcie i ponowne zapisanie takiej transakcji mogło zostać zablokowane, nawet gdy kod i kwota pozostawały bez zmian.

### 44.3. Wykonana poprawka

W `PaymentLedgerScreen.kt`:
- rozpoznawane są rekordy pochodzące ze źródeł innych niż `MANUAL`;
- jeśli kod aktywa i odpowiadająca mu kwota pozostają dokładnie niezmienione względem rekordu historycznego, brak definicji w słowniku nie blokuje zachowania tej wartości;
- każda zmiana kodu lub kwoty nadal wymaga definicji aktywa i przechodzi walidację precyzji;
- reguła dotyczy strony oddawanej, otrzymywanej i prowizji;
- rekordy ręczne nadal wymagają aktywa ze słownika.

To rozwiązanie zachowuje dane historyczne, nie tworząc fikcyjnych definicji aktywów i nie osłabiając walidacji nowych wartości.

### 44.4. Test ochrony aktywa użytego wyłącznie jako prowizja

W `PaymentLedgerDatabaseTest.kt` dodano test `assetUsedOnlyAsTransactionFeeKeepsItsHistoricalDefinitionImmutable`:
- zapisuje aktywo kruszcowe wyłącznie jako aktywo prowizji;
- dopuszcza zmianę nazwy opisowej;
- odrzuca zmianę próby i jednostki;
- potwierdza zachowanie kodu aktywa w zapisanej prowizji.

Test rozszerza pokrycie metody `isAssetReferenced()` o odwołanie wyłącznie z tabeli prowizji.

### 44.5. Zmienione pliki i rewizje

- `PaymentLedgerScreen.kt` — commit `237866ba2cf5cb33cd31b35f7f3cae393f7473b9`, blob `0a712efbf040b6af6776b84507f9a2c95a141fe6`;
- `PaymentLedgerDatabaseTest.kt` — commit `c1bc76f46bb04a5e4c14e8b66ff3847b8ce8ad9c`, blob `2d0a89a0b1cb2b3c7fe2404efc7ad774978710d7`.

### 44.6. Weryfikacja i ograniczenia

Zmiany zapisano na gałęzi `feature/payment-ledger-stage-1`. Należy ponownie pobrać oba pliki z GitHub i sprawdzić treść po zapisie. Nowe przebiegi CI muszą potwierdzić kompilację, test JVM, test instrumentacyjny prowizji oraz testy Compose. Samo dodanie testu nie oznacza jego wykonania.

Nie dodano jeszcze testu Compose dedykowanego formularzowi słownika i scenariuszowi edycji historycznego rekordu. Nie dodano również automatycznego katalogu ISO 4217 ani wersjonowania definicji aktywów.

**Status etapu 2N.8:** poprawka zgodności wstecznej i test prowizji zapisane; ponowny odczyt plików i CI dla nowej rewizji wymagane. PR #68 pozostaje otwarty i niescalony.

### 44.7. Następne działania

1. Zweryfikować świeże CI dla nowego SHA.
2. Dodać testy Compose dla dodawania aktywa, wyboru aktywa i walidacji precyzji.
3. Dodać test scenariusza edycji importowanej transakcji z kodem spoza słownika.
4. Zweryfikować obsługę zera, wartości ujemnych i nadmiarowej skali zgodnie z regułami modelu.
5. Po przejściu testów przeprowadzić końcowy przegląd migracji, eksportu/importu i zachowania historii.


---

## 45. Etap 2N.9 — naprawa błędów wykrytych w testach emulatora (14 listopada 2026 r.)

### 45.1. Rzeczywisty wynik CI etapu 2N.8

Dla SHA `217737b2f99f4d72c5bb229f141654b3938f4d0b`:
- Repository compliance — sukces (run `43636359420`);
- Android prototype build — sukces (run `43636359551`), w tym testy JVM i kompilacja debug APK;
- Android GUI validation — niepowodzenie (run `43636359386`): 60 testów instrumentacyjnych, 3 niepowodzenia.

Log emulatora wskazał trzy konkretne problemy:
1. `updatingFinancialTransactionPreservesRecordAndReplacesOnlyItsExplicitLinks` — asercja porównywała obiekty `BigDecimal` przez `equals`, które rozróżnia skalę (np. `8000.00` i `8000`), mimo równej wartości liczbowej.
2. `assetUsedOnlyAsTransactionFeeKeepsItsHistoricalDefinitionImmutable` — test ujawnił ograniczenie modelu: walidacja wymagała, aby prowizja była w aktywie jednej ze stron transakcji.
3. `assetRegistryPersistsAndUpdatesCurrencyAndMetalDefinitions` — oczekiwana kolejność listy nie odpowiadała kontraktowi `readAssets()`, który sortuje według rodzaju aktywa, a następnie kodu.

### 45.2. Poprawki modelu i testów

W `FinancialModels.kt`:
- usunięto nieuzasadnione ograniczenie, że prowizja musi być rozliczona w jednym z dwóch aktywów transakcji;
- pozostawiono walidację niepustego kodu aktywa i nieujemnej kwoty w `Fee.validate()`;
- dopuszczono jawnie prowizję pobraną w trzecim aktywie, np. osobnej walucie lub kruszcu.

W `FinancialModelsTest.kt`:
- zmieniono test modelu tak, aby potwierdzał dopuszczalność prowizji w trzecim aktywie.

W `PaymentLedgerDatabaseTest.kt`:
- porównanie transakcji rozbito na pola; kwoty `BigDecimal` porównywane są przez `compareTo`, bez fałszywego błędu wynikającego wyłącznie ze skali;
- oczekiwana kolejność aktywów odpowiada rzeczywistemu kontraktowi `readAssets()` (rodzaj aktywa, kod);
- test prowizji w trzecim aktywie może teraz przejść walidację modelu i sprawdzić ochronę historycznej definicji aktywa.

### 45.3. Zapisane rewizje

- `FinancialModels.kt` — commit `823db003f9714bb01d6b058db118548e6dfa6e10`, blob `eabc97f758dac15e31197bfeafbb9bfe72f73a31`;
- `FinancialModelsTest.kt` — commit `f86d2fb77b9fca8885d88555d6276a77123035d4`, blob `1a8bae6a9bfb24639b54ee475725c81e79108311`;
- `PaymentLedgerDatabaseTest.kt` — commit `96d645bf19e210225c42764a690b474d39cd4650`, blob `318785d1ee2ca68197129ce69afd7f1357358134`.

### 45.4. Kontrola po zapisie i dalsza walidacja

Po zapisaniu należy ponownie pobrać trzy pliki i potwierdzić obecność zmian. Świeże przebiegi CI muszą wykonać:
- testy JVM modelu;
- testy instrumentacyjne SQLite, w tym prowizję w trzecim aktywie;
- testy Compose na emulatorze;
- kompilację debug APK;
- kontrolę REUSE.

**Status etapu 2N.9:** poprawki zapisane; wyników CI dla nowego SHA jeszcze nie ma. Nie oznaczać testów jako zaliczonych przed zakończeniem przebiegów.

PR #68 pozostaje otwarty i niescalony. APK nie jest publikowany jako zweryfikowane wydanie.

### 45.5. Następne prace

1. Potwierdzić CI dla nowego SHA i w razie potrzeby naprawić pozostałe błędy.
2. Dodać test Compose dla słownika aktywów, wyboru aktywów i precyzji kwot.
3. Dodać test edycji importowanej transakcji z kodem spoza słownika.
4. Uzupełnić testy graniczne ilości i prowizji: zero, wartości ujemne oraz nadmiarowa skala.
5. Przeprowadzić końcowy przegląd importu/eksportu i integralności historii.

---

## 46. Etap 2O — pełna ewidencja darowizn gotówkowych (15 listopada 2026 r.)

### 46.1. Kontrola stanu wyjściowego i wykryta luka

Kontrola najnowszej gałęzi `feature/payment-ledger-stage-1` i PR #68 wykazała, że:
- model `CashDonation` oraz walidacja podstawowych pól istniały już w `FinancialModels.kt`;
- na ekranie znajdował się wyłącznie opis sekcji „Darowizny gotówkowe”, bez formularza;
- brakowało kompletnej trwałej warstwy CRUD w bieżącej wersji `PaymentLedgerDatabase`;
- wersjonowany JSON w schemacie 2 przenosił zapisy bankowe i transakcje, ale nie darowizny;
- repozytorium i ekran nie zapewniały pełnego cyklu życia darowizny.

Nie powielano więc modelu. Uzupełniono brakujące warstwy: SQLite, repozytorium, JSON, interfejs oraz testy.

### 46.2. Migracja SQLite 5 → 6

Podniesiono `PaymentLedgerDatabase.DATABASE_VERSION` do 6. Dodano tabelę `cash_donations` z polami:
- identyfikator darowizny;
- data i czas przyjęcia;
- kwota dziesiętna oraz kod waluty;
- obowiązkowa nazwa organizacji-beneficjenta;
- obowiązkowy cel darowizny;
- opcjonalna nazwa darczyńcy i jawny znacznik anonimowości;
- kampania/zbiórka, osoba przyjmująca, numer pokwitowania;
- status przechowywania/przekazania;
- data i kwota przekazania;
- uwagi i źródło rekordu.

Dodano indeksy po dacie przyjęcia i nazwie organizacji. Migracja jest nieusuwająca danych: tworzy nową tabelę bez zmiany istniejących zapisów bankowych, transakcji, prowizji i aktywów.

### 46.3. CRUD i walidacja

W bazie dodano:
- `saveCashDonation` — wstawienie z ochroną klucza głównego przed duplikatem;
- `updateCashDonation` — aktualizacja istniejącego rekordu bez zmiany jego identyfikatora;
- `readCashDonations` — odczyt z zachowaniem `BigDecimal`;
- `deleteCashDonation` — jawne usunięcie po identyfikatorze.

Repozytorium udostępnia odpowiadające metody wykonywane na `Dispatchers.IO`.

Walidacja modelu obejmuje:
- dodatnią kwotę darowizny;
- trzyliterowy kod waluty;
- obowiązkową nazwę organizacji-beneficjenta i cel;
- opcjonalność danych darczyńcy przy zachowaniu znacznika anonimowości;
- nieujemną kwotę przekazania, nieprzekraczającą przyjętej kwoty;
- wymóg występowania daty i kwoty przekazania razem;
- zgodność statusu z rozliczeniem: `PARTIALLY_TRANSFERRED` wymaga kwoty większej od zera i mniejszej od darowizny, `TRANSFERRED` wymaga przekazania całej kwoty, a `HELD`/`DEPOSITED` nie mogą wskazywać dodatniego przekazania.

### 46.4. Formularz i lista w interfejsie

W `PaymentLedgerScreen.kt` zastąpiono statyczny opis działającą ewidencją:
- kwota i waluta;
- obowiązkowa nazwa organizacji;
- obowiązkowy cel;
- darczyńca opcjonalny i przełącznik anonimowości;
- kampania, osoba przyjmująca i numer pokwitowania;
- status przechowywania/przekazania;
- kwota przekazana i jawna data przekazania ISO-8601;
- uwagi;
- zapis, edycja, anulowanie edycji i usuwanie;
- lista zapisanych darowizn z kwotą, organizacją, celem, datą i statusem.

Data przekazania nie jest już automatycznie ustawiana na chwilę edycji — użytkownik wpisuje ją jawnie, a walidacja sprawdza zgodność z kwotą i statusem.

### 46.5. Pełne archiwum JSON i deduplikacja darowizn

Rozszerzono bieżący schemat 2 o tablicę `donations`, nie zmieniając numeru schematu, ponieważ dodano opcjonalny element, a dekoder nadal akceptuje archiwa schematu 1 i 2:
- archiwa schematu 1 bez transakcji i darowizn pozostają czytelne;
- archiwa schematu 2 bez tablicy darowizn pozostają czytelne;
- nowe eksporty obejmują zapisy, transakcje, prowizje i darowizny;
- dekoder rekonstruuje komplet pól darowizny i uruchamia walidację.

Import darowizn jest częścią atomowego importu archiwum:
- identyczny identyfikator i zgodne podstawowe dane oznaczają duplikat;
- identyczny identyfikator z inną datą, kwotą, walutą, organizacją lub celem oznacza konflikt;
- konflikt powoduje wycofanie całej transakcji importu;
- import tej samej paczki ponownie nie tworzy kolejnych rekordów darowizn.

### 46.6. Testy dodane

- `PaymentLedgerDatabaseTest.kt`: trwałość po ponownym otwarciu bazy, wymagane pola organizacji i celu, anonimowy darczyńca, ochrona przed duplikatem identyfikatora, aktualizacja/usunięcie oraz migracja wersji 5 → 6 bez utraty danych.
- `PaymentLedgerRepositoryTest.kt`: eksport/import JSON darowizn, idempotencja ponownego importu i konflikt tego samego identyfikatora z innym celem.
- `PaymentLedgerJsonCodecTest.kt`: round-trip JSON z organizacją, celem, anonimowością i częściowym przekazaniem.
- `FinancialModelsTest.kt`: zgodność statusu z kwotą i datą przekazania, w tym przypadki częściowego i pełnego przekazania oraz błędne kombinacje.

### 46.7. Zmienione pliki i commity

- `PaymentLedgerDatabase.kt` — `8e188337e97275eaa19f519b1f7392c83c58f207`;
- `PaymentLedgerRepository.kt` — `cbf7e8044728aa9aad7879c2d802825a75fca2c5`;
- `PaymentLedgerJsonCodec.kt` — `f3dcdb036e872ffc5c6111682a44749e80b32012`;
- `PaymentLedgerScreen.kt` — `aa0bfb781152aea606bf2e39e3d7f9d8583bd06c` oraz `edf6cac93cfe2e905fa26402eb8c4336112b71b6`;
- `FinancialModels.kt` — `8648e854b9a83a3f6db884930ca514c1588ab07c`;
- `PaymentLedgerDatabaseTest.kt` — `df983760e97dca68a504ed709f7465ff107057a7`;
- `PaymentLedgerRepositoryTest.kt` — `45664ba0b05f2c23f18103c5f9482e340bb3a2b2`;
- `PaymentLedgerJsonCodecTest.kt` — `bdae62e08650955c1c726b47072ee67cbe02281a`;
- `FinancialModelsTest.kt` — `2ca5026b7985f42c593dd995157929339f65db9c`.

Po zapisaniu ponownie pobrano wszystkie dziewięć plików i potwierdzono obecność tabeli i migracji, metod CRUD, API repozytorium, JSON, formularza oraz testów.

### 46.8. CI — wynik rzeczywistej kompilacji i korekta audytu

Końcowa rewizja przed kontrolą CI: `4deebe7254db9c9312bb3a6e8ec12301af91dd66`.

Dla tej rewizji GitHub Actions wykonał:
- Android prototype build — run `43681601693`: **niepowodzenie** na `:app:compileDebugKotlin`;
- Android GUI validation — run `43681601617`: **niepowodzenie** na kroku testów JVM, przed testami emulatorowymi;
- Repository compliance (alfa) — run `43681601643`: **sukces**.

Logi ujawniły konkretną przyczynę: w `PaymentLedgerDatabase.kt` dwukrotnie zadeklarowano metody `saveCashDonation`, `updateCashDonation`, `readCashDonations`, `deleteCashDonation` oraz mapper `CashDonation.toContentValues()`. Powodowało to błędy „Conflicting overloads” i niejednoznaczne wywołania w repozytorium. Wcześniejsze stwierdzenie, że nie wykryto błędu kompilacji, było nieprawidłowe — niniejsza korekta zastępuje tamtą ocenę.

### 46.9. Korekta kodu po CI

Usunięto drugi, powielony zestaw deklaracji CRUD z `PaymentLedgerDatabase.kt`, pozostawiając jedną implementację każdej metody. Zachowano pierwotną implementację aktualizacji, która usuwa identyfikator z `ContentValues` i aktualizuje rekord po kluczu `donation_id`.

- plik: `obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/PaymentLedgerDatabase.kt`;
- commit poprawki: `0ed532d8a474d57c3ebdb4a6d8ba0b2c5b2f32f8`.

Po tej poprawce trzeba ponownie wykonać kompilację, testy JVM i testy instrumentacyjne. Do czasu uzyskania wyników nie oznacza się etapu 2O jako zweryfikowanego i nie publikuje APK.


### 46.10. Status i dalsze prace

| Element | Status |
|---|---|
| Model i walidacja darowizny | Rozszerzone i zaostrzone |
| SQLite 5 → 6 | Zaimplementowano migrację |
| CRUD darowizn | Zaimplementowano |
| Formularz i lista darowizn | Zaimplementowano |
| Edycja i usuwanie | Zaimplementowano |
| JSON — eksport i import | Rozszerzono |
| Deduplikacja i konflikty darowizn | Zaimplementowano |
| Testy jednostkowe i instrumentacyjne | Dodano; oczekują na CI |
| Ręczny test formularza na urządzeniu | Do wykonania |
| Pokwitowanie/załącznik dokumentujący darowiznę | Do wykonania |
| Eksport raportu darowizn do PDF/CSV | Do wykonania |

Następny etap po zielonym CI: dodać załączniki/pokwitowania do darowizn z ochroną prywatności i sumami kontrolnymi, a następnie raport okresowy przyjętych oraz przekazanych środków. Nie scalać PR i nie publikować APK jako zweryfikowanego wydania przed uzyskaniem wyników dla aktualnego SHA.


### 46.11. Wynik testów po usunięciu duplikatów i korekta testu modelu

Po commicie `0ed532d8a474d57c3ebdb4a6d8ba0b2c5b2f32f8` kompilacja Kotlin zakończyła się powodzeniem. Następnie uruchomiono 138 testów jednostkowych; 137 zakończyło się powodzeniem, a jeden wykrył niespójność w danych testowych:

- test: `FinancialModelsTest > donationTransferCannotExceedReceivedAmountOrHavePartialTimestamp`;
- błąd: test tworzył darowiznę ze statusem domyślnym `HELD`, jednocześnie ustawiając przekazaną kwotę 200,00 PLN i datę przekazania;
- reguła modelu: dla kwoty przekazanej większej od zera wymagany jest status `PARTIALLY_TRANSFERRED`, a status `HELD` dopuszcza wyłącznie brak przekazania albo kwotę zerową.

Wniosek: błąd dotyczył konfiguracji testu, nie reguły modelu. Poprawiono dane testowe przez jawne ustawienie `DonationCustodyStatus.PARTIALLY_TRANSFERRED`. Test nadal sprawdza przekroczenie kwoty darowizny oraz brak daty przy istniejącej kwocie przekazania.

- plik: `obywatel-android/app/src/test/java/pl/gov/obywatel/android/FinancialModelsTest.kt`;
- commit poprawki testu: `1f6f2c422b92bbead8d9822739aea8358daa1741`.

Ponowne wykonanie pełnego zestawu testów JVM, testów emulatorowych i budowy APK dla tego commita pozostaje wymagane. Nie należy uznawać testu za zaliczony wyłącznie na podstawie poprawienia danych testowych.

### 46.12. Wynik emulatora dla rewizji `f4ebb0889053678b8e86a920f830533216318e82` i poprawka inicjalizacji nowej bazy

Pełny przebieg Android GUI validation (run `43683607045`) potwierdził:
- testy jednostkowe JVM — sukces;
- testy Compose/instrumentacyjne — niepowodzenie: 6 z 65 testów;
- Repository compliance — sukces (run `43683607022`);
- Android prototype build — sukces (run `43683606985`), obejmujący kompilację i etap budowania debug APK w odrębnym przepływie.

Wszystkie sześć niepowodzeń instrumentacyjnych miało wspólną przyczynę: `SQLiteException: no such table: cash_donations`. Inspekcja `PaymentLedgerDatabase.onCreate()` wykazała, że metoda tworzyła tabele zapisów bankowych, transakcji i aktywów, ale pomijała `createCashDonationTable(db)`. Migracja `onUpgrade()` dla baz o wersji poniżej 6 tworzyła tabelę prawidłowo, dlatego problem dotyczył wyłącznie świeżej instalacji / nowej bazy, a nie ścieżki migracyjnej.

**Wykonana poprawka:** dodano wywołanie `createCashDonationTable(db)` w `onCreate()`, obok pozostałych metod tworzących schemat. Metoda tabeli używa `CREATE TABLE IF NOT EXISTS` i indeksów `IF NOT EXISTS`, więc wspólne użycie w ścieżce nowej bazy i migracji pozostaje idempotentne.

- plik: `obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/PaymentLedgerDatabase.kt`;
- commit: `2a63d88c5986f727a4ee0e1eb5626f10a5d53f44`;
- blob po poprawce: `2fdb84c36574ee075ce6984470e4e426e9a7f107`.

**Status po poprawce:** przyczyna została usunięta w kodzie, ale nie jest jeszcze potwierdzona testami. Wymagane jest świeże CI dla nowego SHA: JVM, wszystkie 65 testów instrumentacyjnych/Compose, REUSE compliance oraz debug APK. Do czasu zakończenia przebiegów nie uznaje się etapu 2O za zweryfikowany i nie publikuje APK jako sprawdzonego wydania.

### 46.13. Kolejność następnych prac

1. Potwierdzić, że sześć testów dotyczących darowizn i archiwum przechodzi po utworzeniu tabeli w `onCreate()`.
2. Sprawdzić migrację SQLite 5 → 6 oraz świeżą inicjalizację bazy w odrębnych testach, aby obie ścieżki były chronione regresyjnie.
3. Po zielonym CI przejść do pokwitowań/załączników darowizn: lokalny zapis przez SAF, ograniczenia typu i rozmiaru, brak przesyłania dokumentów poza urządzenie, powiązanie z identyfikatorem darowizny oraz integralność przez sumę kontrolną.
4. Następnie dodać raporty okresowe PDF/CSV przyjętych, przechowywanych i przekazanych darowizn z wyraźnym rozdzieleniem kwot przyjętych i faktycznie przekazanych.
5. Nie scalać PR #68 bez osobnego polecenia.

### 47. Etap 2P — prywatne załączniki i pokwitowania darowizn (15 listopada 2026 r.)

#### 47.1. Warunki wejściowe

Dla rewizji `ca4621f904766672cd925f71393d391159772e77` wszystkie trzy kontrole CI zakończyły się sukcesem:
- Android GUI validation — run `43684198897`: sukces;
- Android prototype build — run `43684198890`: sukces;
- Repository compliance — run `43684198960`: sukces.

W ten sposób potwierdzono poprawkę inicjalizacji tabeli darowizn i można było przejść do załączników.

#### 47.2. Zakres wykonanej implementacji

Dodano model metadanych `DonationAttachment` z identyfikatorem, identyfikatorem darowizny, nazwą pliku, typem MIME, rozmiarem, sumą SHA-256, nazwą pliku w prywatnym magazynie i datą dodania.

Dozwolone typy: PDF, JPEG i PNG. Limit jednego pliku: 10 MiB. Model odrzuca pustą nazwę, niedozwolony MIME, nieprawidłową sumę, rozmiar poza zakresem oraz nazwę pliku magazynowego niespełniającą wzorca UUID z rozszerzeniem.

#### 47.3. Magazyn prywatny

Nowa klasa `DonationAttachmentStorage`:
- przyjmuje dokument przez URI z systemowego selektora SAF;
- kopiuje bajty do prywatnego katalogu `files/donation-attachments`;
- nie zachowuje zależności od pierwotnego URI dostawcy pliku;
- ogranicza rozmiar strumienia w trakcie kopiowania, a nie dopiero po jego odczytaniu;
- oblicza SHA-256 podczas kopiowania;
- usuwa plik tymczasowy w razie błędu;
- usuwa wyłącznie nazwy plików zgodne ze ścisłym wzorcem UUID i dozwolonym rozszerzeniem.

Pliki nie są dodawane do zwykłego archiwum JSON. Eksport JSON przenosi nadal dane ewidencyjne, ale nie kopiuje lokalnych dokumentów ani ich ścieżek urządzenia.

#### 47.4. SQLite i interfejs

Wersję bazy podniesiono z 6 do 7. Tabela `donation_attachments` przechowuje metadane, ma klucz obcy do `cash_donations` z `ON DELETE CASCADE` oraz indeks po identyfikatorze darowizny. Migracja 6 → 7 tworzy tabelę bez usuwania danych.

W ekranie rejestru darowizn dodano:
- wybór PDF/JPEG/PNG przez SAF;
- wyświetlanie nazwy, rozmiaru, MIME i pełnej sumy SHA-256;
- usuwanie pojedynczego załącznika;
- usuwanie metadanych załączników razem z rekordem darowizny oraz czyszczenie odpowiadających plików z magazynu prywatnego.

#### 47.5. Testy dodane

- walidacja metadanych załącznika, typu MIME i limitu rozmiaru;
- test trwałości metadanych w SQLite;
- test kaskadowego usunięcia metadanych przy usunięciu darowizny.

Testy obejmują walidację modelu, zapis/odczyt metadanych, kaskadę usuwania i migrację 6 → 7 z zachowaniem przykładowej darowizny. Testy automatyczne nie zastępują jeszcze ręcznego sprawdzenia selektora SAF i fizycznego kopiowania PDF/JPEG/PNG na urządzeniu. Nie dodano podglądu/otwierania załącznika ani importu załączników w archiwum JSON.

#### 47.6. Pliki i rewizje

- `FinancialModels.kt` — model i walidacja metadanych;
- `DonationAttachmentStorage.kt` — nowy prywatny magazyn plików;
- `PaymentLedgerDatabase.kt` — schemat 7 i CRUD metadanych;
- `PaymentLedgerRepository.kt` — import pliku, zapis metadanych i usuwanie;
- `PaymentLedgerScreen.kt` — selektor SAF, lista i usuwanie;
- `PaymentLedgerDatabaseTest.kt` — testy metadanych, kaskady i migracji 6 → 7;
- `FinancialModelsTest.kt` — test walidacji MIME, limitu rozmiaru, SHA-256 i nazwy prywatnego pliku;
- `FinancialModelsTest.kt` — uzupełnienie testów modelu.

#### 47.7. Weryfikacja

Zmiany zapisano w jednej rewizji, aby uniknąć niepotrzebnego uruchamiania wielu równoległych przebiegów CI. Wymagane są: kompilacja Kotlin, testy JVM, wszystkie testy emulatorowe, migracja 6 → 7, kontrola REUSE oraz budowa debug APK. Dopiero po zielonym CI można uznać etap za zweryfikowany. APK nie jest oznaczany jako wydanie sprawdzone przed zakończeniem tych kontroli.

### 48. Następne prace

1. Dodać testy instrumentalne rzeczywistego kopiowania strumienia SAF do magazynu prywatnego, limitu 10 MiB, pustego pliku i czyszczenia po błędzie.
2. Dodać bezpieczne otwieranie/podgląd plików przez kontrolowany `FileProvider`, bez ujawniania bezpośrednich ścieżek prywatnych.
3. Dodać eksport raportu darowizn PDF/CSV z oddzieleniem sum przyjętych, przechowywanych i przekazanych.
4. Rozważyć jawny eksport/backup załączników jako osobne archiwum, z walidacją sum kontrolnych i limitów.


**Uzupełnienie kontroli implementacji:** po ponownym odczycie plików skorygowano zapis separatora ścieżki w literałach znakowych Kotlin w modelu i magazynie plików. Dodano także jawny test migracji z wersji 6 do 7 oraz jednostkowy test walidacji metadanych. Te uzupełnienia wchodzą do tej samej rewizji przed oceną CI.


#### 47.8. Potwierdzenie CI i dodatkowa kontrola typu pliku

Dla rewizji `5d518a4dae999cce700672391e773ce1de963d19` potwierdzono pełny sukces trzech przepływów:
- Android GUI validation — run `43685358529`: sukces kroków testów jednostkowych JVM, testów Compose na emulatorze, archiwizacji raportów oraz budowy debug APK;
- Android prototype build — run `43685358503`: sukces testów JVM i budowy debug APK;
- Repository compliance — run `43685358543`: sukces kontroli REUSE.

W Android GUI validation kroki publikacji APK i sumy kontrolnej były pominięte przez konfigurację przepływu (brak stabilnego klucza podpisującego); sukces budowy debug APK nie oznacza opublikowania ani podpisania wydania produkcyjnego.

W przeglądzie bezpieczeństwa wykryto, że początkowa implementacja ufała typowi MIME zwróconemu przez dostawcę SAF. Typ MIME nie potwierdza rzeczywistej zawartości pliku. Dodano więc kontrolę sygnatur binarnych:
- PDF: `%PDF-`;
- JPEG: `FF D8 FF`;
- PNG: `89 50 4E 47 0D 0A 1A 0A`.

Kontrola działa podczas importu, po ograniczonym kopiowaniu strumienia; niezgodność MIME i sygnatury powoduje usunięcie częściowego pliku i odrzucenie załącznika. Dodano testy jednostkowe poprawnych i niezgodnych sygnatur. Ta poprawka wymaga osobnego zielonego CI dla nowej rewizji, zanim będzie uznana za zweryfikowaną.

Status etapu 2P: implementacja metadanych, magazynu prywatnego, interfejsu i migracji 6 → 7 jest zbudowana i przeszła CI na rewizji `5d518a4...`; walidacja sygnatur jest dodatkowym zabezpieczeniem oczekującym na CI. Ręczny test importu plików z rzeczywistych dostawców SAF oraz test na urządzeniu pozostają do wykonania.


**Doprecyzowanie rewizji kontroli sygnatur:** po pierwszym zapisie sprawdzono zgodność typów Kotlin w porównaniach bajtów sygnatur PNG i poprawiono jawne konwersje do `Byte`. Rewizja końcowa kodu kontroli sygnatur: `908537392875831fbd04fba6d74211103179d103`. Poprzedni przebieg CI dla `187d57ff...` został anulowany wskutek kolejnej aktualizacji gałęzi; nie stanowi wyniku walidacji rewizji końcowej. Należy uruchomić i ocenić CI na aktualnym HEAD.


**Kontrola testu jednostkowego:** podczas przeglądu importów testu sygnatur wykryto brak jawnych importów `assertTrue` i `assertFalse`; dodano je przed końcową walidacją. Rewizja zawierająca tę korektę: `91f71405d1189e194f1bdd96c544f6b6a211a3f6`. Przebiegi uruchomione dla poprzedniej rewizji mogą zostać anulowane; o wyniku końcowym decyduje CI dla aktualnego HEAD.


### 49. Korekta wspólnej ścieżki usuwania darowizny i jej załączników (15 listopada 2026 r.)

#### 49.1. Ustalenie z przeglądu

W PaymentLedgerRepository istniały dwie publiczne metody usuwania darowizny:
- deleteCashDonation(id) usuwała rekord bezpośrednio z SQLite;
- deleteCashDonationWithAttachments(id) najpierw pobierała metadane załączników, usuwała rekord darowizny (z kaskadą metadanych), a następnie usuwała pliki z prywatnego katalogu.

Ekran rejestru korzystał z drugiej metody, ale pierwsza pozostawała możliwą ścieżką pozostawienia osieroconych plików. Była to niespójność warstwy repozytorium, a nie problem samego ograniczenia klucza obcego SQLite.

#### 49.2. Wykonana poprawka

Ujednolicono publiczną ścieżkę: deleteCashDonation(id) deleguje teraz do deleteCashDonationWithAttachments(id). Dzięki temu wywołujący podstawową metodę repozytorium nie może ominąć usuwania plików prywatnych. Istniejąca metoda rozszerzona pozostaje implementacją operacji w Dispatchers.IO.

#### 49.3. Test regresyjny

Dodano test instrumentalny deletingDonationThroughPrimaryRepositoryMethodAlsoRemovesAttachmentMetadataAndPrivateFile, który:
1. zapisuje darowiznę i metadane załącznika w testowej bazie;
2. tworzy odpowiadający plik w prywatnym katalogu aplikacji;
3. usuwa darowiznę przez podstawową metodę deleteCashDonation;
4. sprawdza brak darowizny, brak metadanych załącznika i fizyczny brak pliku.

Test zabezpiecza jednocześnie spójność SQLite i czyszczenie magazynu prywatnego. Nie zastępuje testów importu z rzeczywistego dostawcy SAF.

#### 49.4. Status weryfikacji

Zmiana kodu i testu została przygotowana w jednym commicie wraz z niniejszą aktualizacją audytu. Wcześniejszy przebieg GUI validation dla poprzedniego SHA jest odrębny od nowej rewizji; ostateczna ocena poprawki wymaga świeżego CI dla nowego HEAD: testów JVM, testów emulatorowych, budowy debug APK i kontroli REUSE. Nie oznacza się poprawki jako przetestowanej przed zakończeniem tych przebiegów.



---

## 50. Etap 2Q.1 — bezpieczne otwieranie prywatnych załączników darowizn (15 listopada 2026 r.)

### 50.1. Ustalenie z przeglądu

Etap 2P zapewniał import, przechowywanie, sumę SHA-256 i usuwanie załączników, ale ekran nie udostępniał akcji otwarcia/podglądu. Istniejący AndroidX FileProvider obsługiwał wyłącznie katalog pamięci podręcznej OCR; prywatny katalog `files/donation-attachments` nie był objęty konfiguracją ścieżek.

### 50.2. Wykonane zmiany

- Rozszerzono `ocr_file_paths.xml` wyłącznie o katalog `donation-attachments/` przez `files-path`; nie udostępniono całego katalogu `files/`.
- Dodano w `DonationAttachmentStorage.contentUri()` kontrolę metadanych, istnienia pliku, dokładnego rozmiaru i ponowne obliczenie SHA-256 przed udostępnieniem.
- URI jest generowane przez AndroidX FileProvider jako `content://`; kod nie przekazuje zewnętrznej aplikacji ścieżki systemowej ani URI `file://`.
- Repozytorium udostępnia `getDonationAttachmentUri(id)`, odczytując metadane i wykonując kontrolę pliku na `Dispatchers.IO`.
- Ekran darowizn otrzymał akcję „Otwórz”, która uruchamia wybór aplikacji obsługującej MIME PDF/JPEG/PNG z flagą `FLAG_GRANT_READ_URI_PERMISSION`.
- Pozostawiono dotychczasowe ograniczenia typów plików i limit 10 MiB. Załączniki nadal nie są dołączane do archiwum JSON.

### 50.3. Testy regresyjne

Dodano `DonationAttachmentStorageTest.kt` z testami:
- generowanie URI `content://` i odczyt bajtów przez ContentResolver;
- odrzucenie pliku zmienionego po imporcie (niezgodna suma SHA-256);
- odrzucenie brakującego pliku.

Testy wymagają emulatora/urządzenia Android i nie są uznawane za wykonane na podstawie samego dodania źródła.

### 50.4. Pliki zmienione

- `DonationAttachmentStorage.kt`;
- `PaymentLedgerRepository.kt`;
- `PaymentLedgerScreen.kt`;
- `res/xml/ocr_file_paths.xml`;
- nowy `DonationAttachmentStorageTest.kt`;
- niniejszy audyt.

### 50.5. Status weryfikacji

Zmiany źródłowe i testy zapisano na `feature/payment-ledger-stage-1`. Wszystkie sześć plików (magazyn, repozytorium, ekran, konfiguracja ścieżek FileProvider, nowy test instrumentacyjny i audyt) ponownie pobrano z GitHub; kontrola potwierdziła obecność akcji „Otwórz”, generowania `content://`, ponownej walidacji SHA-256, ograniczonej ścieżki `files-path` i trzech testów regresyjnych. Korekta inicjalizacji `applicationContext`/authority repozytorium również została ponownie odczytana. Dla końcowego SHA uruchomiono pełne CI: kompilację Kotlin, testy JVM, testy instrumentacyjne/Compose, kontrolę REUSE i budowę debug APK. REUSE zakończył się sukcesem; oba przepływy Android pozostają w toku w chwili aktualizacji audytu. Do czasu zakończenia testów i budowania APK nie oznaczać etapu 2Q.1 jako zweryfikowanego.

### 50.6. Ograniczenia i następne prace

- Rzeczywisty import z dostawców SAF, zachowanie przy pustym strumieniu, przekroczeniu 10 MiB i błędzie odczytu nadal wymaga testów instrumentalnych z dostawcą dokumentów.
- Otwieranie zależy od zainstalowanej aplikacji obsługującej dany MIME; brak takiej aplikacji ma zostać przedstawiony użytkownikowi jako błąd otwarcia, bez ujawniania pliku.
- Należy wykonać ręczny test PDF/JPEG/PNG na emulatorze i urządzeniu.
- Następny etap: raport darowizn PDF/CSV z osobnymi sumami przyjętymi, pozostającymi w przechowaniu i faktycznie przekazanymi; nie łączyć tych kategorii w jedną wartość.


**Kontrola przed CI — korekta dostępu do kontekstu repozytorium:** w przeglądzie przygotowanej zmiany wykryto, że identyfikator pakietu FileProvider odwoływał się bezpośrednio do parametru konstruktora wewnątrz metody instancyjnej. Przeniesiono aplikacyjny Context i wyliczony authority do właściwości klasy, a bazę i magazyn załączników podłączono do tego samego `applicationContext`. Korekta została zapisana przed końcową walidacją; wynik CI musi dotyczyć rewizji po tej korekcie.


### 50.7. Rewizje GitHub

- `DonationAttachmentStorage.kt` — commit `450ea96b5eaad86de6d108fc8bfc4798c5d47365`;
- `PaymentLedgerRepository.kt` — commit `922efcddfade90c376703a5243ac31ff47f30e7a`, korekta kontekstu/authority `8261857e29c1112662bbfbfd4433238f901d3967`;
- `ocr_file_paths.xml` — commit `411d1315b561ac68184c0a2a901b10d02272bc0b`;
- `PaymentLedgerScreen.kt` — commit `d69d46b7c180c8cdca608e02645cf971ae5c32b6`;
- `DonationAttachmentStorageTest.kt` — commit `b1bff5cc613a53ce9d191ff358027116882e9198`;
- końcowa aktualizacja audytu i korekta statusu CI — bieżący commit aktualizujący ten rozdział.

Wyniki REUSE/Android CI należy przypisać wyłącznie do końcowego SHA po tej aktualizacji, a nie do wcześniejszej rewizji.


---

## 51. Etap 2Q.2 — testy importu SAF, limitu i sprzątania po błędach (15 listopada 2026 r.)

### 51.1. Cel

Etap 2Q.1 weryfikował bezpieczne otwieranie istniejących załączników. Nadal brakowało testów przepływu importu z rzeczywistego `ContentResolver`/URI `content://`, w tym zachowania przy pustym i nadmiernie dużym pliku oraz rozbieżności MIME i sygnatury.

### 51.2. Wykonane rozszerzenie testów

Rozszerzono `DonationAttachmentStorageTest.kt`. Testy wykorzystują AndroidX FileProvider jako źródło URI `content://`, a następnie wykonują import przez produkcyjną metodę `DonationAttachmentStorage.import()`. Oznacza to sprawdzenie rzeczywistej ścieżki `ContentResolver.getType/query/openInputStream`, a nie wyłącznie test pomocniczej funkcji walidującej.

Dodane scenariusze:
1. prawidłowy PDF: kopia do magazynu prywatnego, zachowanie rozmiaru, MIME i zgodnej sumy SHA-256;
2. pusty plik: odrzucenie importu i usunięcie częściowego pliku docelowego;
3. plik o rozmiarze `MAX_BYTES + 1`: odrzucenie po przekroczeniu limitu 10 MiB i usunięcie częściowego pliku;
4. MIME `application/pdf` przy sygnaturze PNG: odrzucenie pliku i usunięcie częściowego pliku.

Źródłowy plik testowy pozostaje w katalogu do sprzątnięcia przez `@After`; po nieudanym imporcie liczba plików w katalogu ma pozostać równa jeden (sam plik źródłowy). Test sukcesu sprawdza także bajtową zgodność kopii.

### 51.3. Zakres i ograniczenia

- Test używa FileProvider jako lokalnego dostawcy dokumentów. Sprawdza API ContentResolver i przepływ strumienia, lecz nie zastępuje testów z zewnętrznymi dostawcami SAF (np. Dokumenty/Pliki lub dostawcą chmurowym).
- Testy JPEG i PNG dla poprawnych sygnatur oraz pełna obsługa błędu odczytu strumienia pozostają osobnymi przypadkami do uzupełnienia.
- Testy wymagają Android instrumentation runner/emulatora; samo skompilowanie testów nie stanowi dowodu ich wykonania.

### 51.4. Status weryfikacji

Zmiana źródła testowego została zapisana na `feature/payment-ledger-stage-1`. Dla commitu `2e95cd8f0bf1f4ef47cf4648b3c5af2ed7e13748` wszystkie trzy przepływy CI zakończyły się powodzeniem: Android GUI validation (testy JVM i Compose/instrumentacyjne, następnie budowa debug APK), Android prototype build oraz REUSE. Kroki testów JVM i Compose UI zakończyły się sukcesem; archiwum raportów instrumentacyjnych zostało utworzone. Publikacja APK i przesunięcie tagu pominięto z powodu braku stabilnych danych podpisujących. Etap 2Q.2 jest zweryfikowany przez CI dla tego SHA; nie oznacza to ręcznej walidacji na fizycznym urządzeniu ani zewnętrznym dostawcy SAF.

### 51.5. Następny etap

Po zielonym CI rozszerzyć testy poprawnego importu JPEG/PNG i kontrolowanego błędu odczytu. Następnie rozpocząć raport darowizn PDF/CSV, rozdzielając kwoty przyjęte, pozostające w przechowaniu, przekazane oraz saldo nierozliczone.


---

## 52. Etap 2Q.3 — testy poprawnego importu JPEG/PNG i błędu odczytu (15 listopada 2026 r.)

### 52.1. Cel

Uzupełnić pokrycie importu załączników darowizn o dozwolone formaty graficzne oraz kontrolowany błąd występujący podczas otwierania strumienia. Testy korzystają z produkcyjnej metody `DonationAttachmentStorage.import()`.

### 52.2. Wykonane zmiany

Do `DonationAttachmentStorageTest.kt` dodano trzy scenariusze: (1) import JPEG z MIME `image/jpeg` i zgodną sygnaturą SOI, z kontrolą MIME, rozmiaru i bajtowej zgodności kopii; (2) analogiczny import PNG z MIME `image/png` i ośmiobajtową sygnaturą; (3) błąd odczytu: utworzenie URI FileProvider, usunięcie pliku źródłowego przed importem, oczekiwany wyjątek i brak pozostałości w katalogu.

Tablice bajtów JPEG/PNG są atrapami sygnatur, a nie kompletnymi obrazami. Testy nie potwierdzają dekodowania ani poprawności wizualnej plików.

### 52.3. Zakres i ograniczenia

- Źródłem nadal jest AndroidX FileProvider, nie niezależny zewnętrzny dostawca SAF.
- Błąd jest symulowany przy otwieraniu strumienia; nie obejmuje awarii w połowie odczytu już otwartego strumienia.
- Nie zmieniano produkcyjnego kodu magazynu ani formatu danych.

### 52.4. Weryfikacja i status

Testy zapisano w commicie `affabb3ece285e0d028f1e109e938e3905433253`. Audyt uzupełniono po tym commicie. Pierwszy przebieg CI dla commitu testowego `affabb3ece285e0d028f1e109e938e3905433253` wykazał: REUSE — sukces, budowanie debug — sukces, Android GUI validation — niepowodzenie. Jedyny wykazany błąd instrumentacyjny dotyczył istniejącego testu `BankAccountsNavigationTest.sharedCredentialAppearanceExposesSeparateCrestAndFlagTransforms`: `performScrollTo()` zgłosiło, że węzeł semantyki zniknął z drzewa. Log nie wskazuje awarii żadnego z trzech nowych testów importu. Z tego powodu 2Q.3 nie został uznany za zweryfikowany. W kolejnym commicie poprawiono odporność tego testu interfejsu; wynik poprawki wymaga ponownego CI.

### 52.5. Następny etap

Po zielonym CI dodać kontrolowany błąd strumienia w trakcie odczytu (testowy dostawca treści), a następnie rozpocząć raport darowizn PDF/CSV z odrębnymi sumami: przyjęte, pozostające w przechowaniu, przekazane oraz saldo nierozliczone.


---

## 53. Etap 2Q.3a — korekta niestabilnego testu Compose i ponowna walidacja (15 listopada 2026 r.)

### 53.1. Ustalenie z CI

Analiza logu Android GUI validation (run `43688211066`) wykazała jedną porażkę w teście `BankAccountsNavigationTest.sharedCredentialAppearanceExposesSeparateCrestAndFlagTransforms`. Przy przewijaniu elementu `Skala godła` framework Compose zgłosił, że wcześniej znaleziony węzeł semantyczny nie znajduje się już w drzewie. Pozostałe 78 z 79 testów zakończyło się bez zgłoszonej porażki. Log nie wskazuje, aby błąd pochodził z testów importu załączników dodanych w 2Q.3.

### 53.2. Wykonana korekta

W `BankAccountsNavigationTest.kt` dodano pomocniczą metodę `assertTextCanBeScrolledIntoView`, która ponownie wyszukuje węzeł po każdej próbie przewinięcia i ponawia czynność do 10 sekund, jeśli drzewo semantyczne zmieni się w trakcie przewijania. Cztery sprawdzenia skali i obrotu godła oraz flagi korzystają teraz z tej metody. Test nadal wymaga rzeczywistego wyświetlenia wszystkich czterech etykiet; nie pominięto ani nie osłabiono oczekiwań funkcjonalnych.

Zmiana została zapisana w commicie `505275ad1c0804318c035d032ed3e548afc8e446`.

### 53.3. Weryfikacja

Po korekcie uruchomiono ponownie trzy przepływy dla rewizji `567dec9b9b7707c85c1b77f5a94732892dfa8640`: Android GUI validation — run `43688918284` (w toku przy ostatnim odczycie), Android prototype build — run `43688918245` (w toku), REUSE — run `43688918327` (sukces). Do uzyskania wyników obu przepływów Android poprawka nie jest uznana za potwierdzoną. W szczególności należy sprawdzić testy importu PDF/JPEG/PNG i obsługę błędu otwarcia strumienia w wynikach instrumentacyjnych.

### 53.4. Status i kolejny krok

Etap 2Q.3 oraz korekta 2Q.3a pozostają oczekujące na zielone CI. Po ich potwierdzeniu kolejnym zadaniem jest kontrolowany błąd odczytu w połowie strumienia z testowym dostawcą treści. Raportowanie darowizn PDF/CSV pozostaje dalszym etapem i nie jest częścią tej zmiany.

---

## 54. Etap 2Q.4 — kontrolowany błąd odczytu w połowie strumienia (15 listopada 2026 r.)

### 54.1. Ustalenia przed zmianą

Dotychczasowy test błędu w etapie 2Q.3 usuwał plik źródłowy przed otwarciem strumienia. Weryfikował zatem błąd otwarcia URI, a nie awarię po częściowym skopiowaniu danych. Nie dowodził, że magazyn usuwa niekompletny plik, gdy dostawca zwróci część bajtów, a następnie zgłosi błąd odczytu.

Dodatkowo przebiegi po poprawce testu Compose 2Q.3a nie stanowią pełnego zielonego potwierdzenia: Android GUI validation i Android prototype build uruchomione dla poprzedniego commitu zostały anulowane; REUSE zakończył się powodzeniem. Późniejszy Android prototype build dla commitu audytu zakończył się powodzeniem, ale nie zastępuje testów instrumentacyjnych/Compose.

### 54.2. Wykonane zmiany

Dodano testowy FailingStreamContentProvider, zadeklarowany wyłącznie w manifeście src/androidTest. Dostawca:
- zgłasza typ MIME application/pdf i nazwę pliku;
- tworzy potok ParcelFileDescriptor;
- zapisuje do potoku poprawną sygnaturę PDF oraz część treści;
- następnie kończy końcówkę zapisującą przez closeWithError("controlled read failure").

Dodano test importDeletesPartialFileWhenProviderFailsAfterWritingBytes, który wywołuje produkcyjną metodę DonationAttachmentStorage.import() z URI dostawcy. Test oczekuje IOException z kontrolowanym komunikatem i sprawdza, że prywatny katalog załączników pozostaje pusty po nieudanym imporcie.

Dostawca jest komponentem testowym, nie został dodany do manifestu produkcyjnego aplikacji. W teście wykorzystano kontekst APK instrumentacyjnego, aby zarówno dostawca, jak i katalog plików testowych należały do pakietu testowego.

### 54.3. Zmienione pliki i rewizje

- DonationAttachmentStorageTest.kt — dodano test awarii po częściowym odczycie; commit fc6c1475ba81c38e66343afd57f23e90805aeb7a;
- FailingStreamContentProvider.kt — nowy dostawca testowy z potokiem i kontrolowanym błędem; commit e9c7cbdb8189233f7ca55242603632dc5ed4506d;
- src/androidTest/AndroidManifest.xml — rejestracja dostawcy wyłącznie w APK testowym; commit 923b46f7e297711b0b586666a9fcbc290c6e36ee.

### 54.4. Weryfikacja i ograniczenia

Na moment aktualizacji audytu kod testu i dostawcy został zapisany w gałęzi, ale wyników CI dla tej rewizji jeszcze nie ma. Nie uznaje się etapu 2Q.4 za zweryfikowany przed przejściem kompilacji Kotlin, testów JVM, testów instrumentacyjnych/Compose, budowy debug APK i kontroli REUSE.

Test celowo symuluje błąd ContentProvider po wysłaniu części danych. Nie zastępuje testu z zewnętrznym dostawcą SAF ani ręcznego sprawdzenia na fizycznym urządzeniu. Jeśli CI ujawni problem z rejestracją providera w testowym APK, należy poprawić konfigurację manifestu/authority i ponowić pełny przebieg.

### 54.5. Następny krok

Uruchomić i ocenić pełne CI dla rewizji obejmującej etap 2Q.4. Po zielonym wyniku przejść do raportów darowizn PDF/CSV, z odrębnym wykazaniem kwot przyjętych, pozostających w przechowaniu, przekazanych i salda nierozliczonego. Nie scalać PR #68 bez osobnego polecenia.

### 54.6. Wynik pierwszego CI i korekta testu (15 listopada 2026 r.)

Pierwsze pełne CI dla etapu 2Q.4 zakończyło się następująco:
- Android prototype build — sukces (run 43694436390);
- REUSE — sukces (run 43694436391);
- Android GUI validation — porażka (run 43694436379), 80 testów instrumentacyjnych, 1 niepowodzenie.

Jedyny nieudany test to nowy `importDeletesPartialFileWhenProviderFailsAfterWritingBytes`. Asercja wykazała `NullPointerException` zamiast oczekiwanego `IOException`. Analiza implementacji ujawniła błąd konfiguracji testu: użyto kontekstu APK instrumentacyjnego jako kontekstu magazynu, podczas gdy `DonationAttachmentStorage` pobiera `context.applicationContext` i korzysta z katalogu aplikacji docelowej. Test powinien używać kontekstu aplikacji testowanej, a resolver tej aplikacji może odczytać wyeksportowanego dostawcę z APK testowego.

Poprawiono test: magazyn i katalog kontrolny korzystają teraz z istniejącego `ApplicationProvider.getApplicationContext()` (wspólnego kontekstu testów klasy), natomiast URI nadal wskazuje testowego dostawcę zarejestrowanego w manifeście androidTest. Nie zmieniono produkcyjnej logiki importu ani nie osłabiono asercji. Korekta zapisana w commicie `77c101a8f2fa2684392145e184fa424fb0b162a6`.

Po tej korekcie wymagane jest ponowne pełne CI. Do czasu zakończenia ponownego Android GUI validation etap 2Q.4 pozostaje niezweryfikowany. Nie należy interpretować sukcesu kompilacji APK i REUSE jako potwierdzenia poprawności testu instrumentacyjnego.

## 54.7. Ponowna walidacja etapu 2Q.4 — brak środowiska Kotlin w APK testowym

Ponowna kontrola CI dla commitu audytu `8a7787f0c36197f8e7bbbad210845bc273db48ab` wykazała:
- REUSE Compliance Check — sukces;
- Android prototype build — sukces;
- Android GUI validation — porażka w teście `DonationAttachmentStorageTest.importDeletesPartialFileWhenProviderFailsAfterWritingBytes`.

Log emulatora wykazał awarię procesu testowego:
`NoClassDefFoundError: kotlin/jvm/internal/Intrinsics` podczas wywołania `FailingStreamContentProvider.getType()`. Przyczyną było uruchomienie testowego providera Kotlin w APK instrumentacyjnym, w którym nie była jawnie dostępna biblioteka standardowa Kotlin. W efekcie test nie doszedł do właściwej asercji zachowania przerwanego strumienia; nie wolno uznać go za zaliczony ani traktować tego wyniku jako błędu produkcyjnego magazynu załączników.

### Korekta

W pliku `obywatel-android/app/build.gradle.kts` dodano jawne:
`androidTestImplementation(kotlin("stdlib"))`

Zależność dotyczy wyłącznie wariantu instrumentacyjnego, ponieważ `FailingStreamContentProvider` jest zadeklarowany wyłącznie w manifeście `src/androidTest`. Nie rozszerza zależności produkcyjnego APK.

- Commit korekty konfiguracji: `12da3d845a3b5706230b8cb1394a89566a9e9456`.
- Status: poprawka zapisana; oczekuje na ponowne Android GUI validation i build.
- Etap 2Q.4: nadal **niezweryfikowany** do czasu przejścia testu kontrolowanego błędu i pełnego zestawu GUI.
- Etap raportów PDF/CSV: wstrzymany do czasu zamknięcia tej walidacji.

Ograniczenie: log potwierdza brak klasy Kotlin runtime w APK testowym. Po dodaniu zależności trzeba potwierdzić w kolejnym przebiegu, że provider uruchamia się, rzeczywiście zapisuje część danych do potoku, zgłasza kontrolowany błąd odczytu, a produkcyjny import usuwa częściowy plik.


## 54.8. Korekta providera testowego po ponownym CI — izolacja APK instrumentacyjnego (15 listopada 2026 r.)

### Wynik przebiegu dla rewizji c4d9383e

Dla commitu audytu `c4d9383e594b7ff73fdbba6c4f6fb73862106249` uzyskano:
- Repository compliance — sukces (run `43698572201`);
- Android prototype build — sukces (run `43698572275`), w tym testy JVM i budowa debug APK;
- Android GUI validation — porażka (run `43698572203`), mimo sukcesu testów JVM.

Analiza logu zadania `132764436318` potwierdziła awarię procesu providera testowego podczas uruchomienia `FailingStreamContentProvider.getType()`: `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics`. W efekcie instrumentacja przerwała się po rozpoczęciu testu `importDeletesPartialFileWhenProviderFailsAfterWritingBytes`; nie uzyskano wiarygodnego wyniku testu błędu odczytu w połowie strumienia. Log potwierdza, że wcześniejsza zależność `androidTestImplementation(kotlin("stdlib"))` nie rozwiązała problemu izolowanego procesu providera.

### Wykonana korekta

Zamiast dalej rozszerzać zależności APK testowego, przeniesiono sam testowy `ContentProvider` z Kotlin do Java:
- dodano `FailingStreamContentProvider.java` z równoważnymi metodami MIME, metadanych, otwierania potoku i kontrolowanego błędu po zapisaniu części bajtów;
- usunięto `FailingStreamContentProvider.kt`;
- usunięto nieskuteczną i zbędną zależność `androidTestImplementation(kotlin("stdlib"))` z `app/build.gradle.kts`;
- pozostawiono providera wyłącznie w manifeście `src/androidTest`; manifest produkcyjny nie został zmieniony;
- nie zmieniono produkcyjnego kodu `DonationAttachmentStorage.import()` ani oczekiwań testu sprzątania częściowego pliku.

Zmiany zapisano w commitach:
- Java provider: `d257ae10b1da94f541b9d6ed832fee5ffba85493`;
- usunięcie providera Kotlin: `c154ffb74059f7538b859d689f20ee9ef64db76c`;
- usunięcie zbędnej zależności: `8c74cbe1b3cabc72d114034cd36689862b003d77`.

### Status

To jest korekta przyczyny awarii klasy w procesie providera, ale nie jest jeszcze dowodem poprawności testu. Etap 2Q.4 pozostaje **niezweryfikowany** do czasu ponownego zielonego Android GUI validation obejmującego kontrolowany błąd strumienia i pozostałe testy Compose. Sukces kompilacji i testów JVM nie zastępuje testów instrumentacyjnych. Raporty PDF/CSV pozostają wstrzymane do zamknięcia tej walidacji.

### Następny krok

Uruchomić pełne CI dla rewizji z Java providerem. W razie kolejnego niepowodzenia pobrać raporty instrumentacyjne i poprawić konkretną przyczynę; dopiero po zielonym przebiegu przejść do implementacji raportów finansowych z rozdzieleniem walut, kruszców, kwot przyjętych, przechowywanych, przekazanych i nierozliczonych.

## 54.9. Korekta symulacji awarii strumienia — niezawodny potok (15 listopada 2026 r.)

### Wynik ponownego CI dla rewizji d7b6586f

Android GUI validation (run `43699181019`) zakończył się niepowodzeniem. Testy JVM przeszły, natomiast instrumentacja wykonała 80 testów i zgłosiła jedną porażkę:
`DonationAttachmentStorageTest.importDeletesPartialFileWhenProviderFailsAfterWritingBytes` — `AssertionError: expected java.io.IOException to be thrown, but nothing was thrown`.

Pozostałe 79 testów instrumentacyjnych przeszło. Repository compliance (run `43699181045`) i Android prototype build (run `43699181009`) zakończyły się sukcesem. Raporty instrumentacyjne zostały zarchiwizowane jako artefakt runu `43699181019` (artifact ID `14154699972`). Ponieważ test błędu strumienia nie przeszedł, etap 2Q.4 pozostaje niezweryfikowany.

### Diagnoza i korekta

Wcześniejsze logi dla tego runu potwierdzają, że Java provider uruchomił się bez błędu Kotlin Runtime, zapisał przypadek testowy, a niepowodzenie nastąpiło na asercji oczekiwanego wyjątku. Provider używał `ParcelFileDescriptor.createPipe()`, czyli zwykłego potoku, podczas gdy test wymaga propagacji kontrolowanego stanu błędu przez `closeWithError()`.

Zmieniono tworzenie potoku na `ParcelFileDescriptor.createReliablePipe()`. Jest to mechanizm przeznaczony do propagowania informacji o błędzie z końca zapisującego do odbiorcy; pozostawiono zapis części payloadu, `closeWithError("controlled read failure")`, URI providera, produkcyjną metodę importu i asercje usunięcia częściowego pliku bez zmian.

- Plik: `obywatel-android/app/src/androidTest/java/pl/gov/obywatel/android/financial/FailingStreamContentProvider.java`.
- Commit korekty: `a3baf7c137464f4285b507d3f104a48219547b38`.

### Status i wymagane potwierdzenie

Korekta jest zapisana, ale nie jest jeszcze zweryfikowana. Należy wykonać pełne Android GUI validation i potwierdzić, że wskazany test przechodzi oraz że cały zestaw instrumentacyjny kończy się bez błędów. Do tego czasu nie oznaczać etapu 2Q.4 jako zamkniętego i nie rozpoczynać raportów PDF/CSV na podstawie niezweryfikowanego założenia o sprzątaniu plików po błędzie odczytu.

## 54.10. Zamknięcie walidacji testu kontrolowanego błędu strumienia (15 listopada 2026 r.)

Pełny przebieg Android GUI validation dla commitu audytu `932e034516bcb02e3d932f1e967e566633b972f1` zakończył się sukcesem (run `43699744020`). Job `132768066976` zakończył się sukcesem; kroki testów JVM, Compose UI na emulatorze, archiwizacji raportów instrumentacyjnych i budowy debug APK zakończyły się sukcesem. Repository compliance (run `43699744057`) oraz Android prototype build (run `43699744016`) również zakończyły się sukcesem.

Pobrany artefakt `android-instrumentation-reports` (ID `14156032877`) zawiera raport XML z zestawem 80 testów, 0 błędów, 0 awarii i 0 pominiętych przypadków. W raporcie test `DonationAttachmentStorageTest.importDeletesPartialFileWhenProviderFailsAfterWritingBytes` występuje jako zaliczony (bez elementu `failure`).

Wynik potwierdza korektę Java providera i użycie `ParcelFileDescriptor.createReliablePipe()` do przekazania błędu przez `closeWithError()`. Etap 2Q.4 — kontrolowany błąd odczytu w trakcie importu i sprzątanie częściowego pliku — uznaje się za **zweryfikowany**. Nie jest to jednak test rzeczywistego zewnętrznego dostawcy dokumentów ani test na fizycznym urządzeniu.

## 55. Raportowanie finansowe — model raportu, CSV i eksport SAF (15 listopada 2026 r.)

### Zakres implementacji

Dodano:
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/FinancialReport.kt` — okres raportu z granicą początkową włącznie i końcową wyłącznie, agregaty aktywów, agregaty darowizn, wiersze zdarzeń i generator CSV;
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/PaymentLedgerRepository.kt` — metodę `exportFinancialReportCsv(period)`, odczytującą transakcje, darowizny i słownik aktywów na `Dispatchers.IO`;
- `obywatel-android/app/src/main/java/pl/gov/obywatel/android/PaymentLedgerScreen.kt` — przycisk eksportu całej historii przez Storage Access Framework (`CreateDocument`, MIME `text/csv`), zapis UTF-8 do lokalizacji wybranej przez użytkownika;
- `obywatel-android/app/src/test/java/pl/gov/obywatel/android/financial/FinancialReportTest.kt` — testy czystego modelu raportowego i CSV.

### Zasady księgowe i zakres danych

1. Kwoty pozostają typu `BigDecimal`; nie ma konwersji do `Double` ani sumowania różnych walut/kruszców.
2. Każdy kod aktywa ma oddzielny agregat, a jednostka kruszcu pochodzi ze słownika aktywów (np. g); brak kursu oznacza brak przeliczenia do wspólnej waluty.
3. Transakcja tworzy osobne zdarzenia strony oddanej, otrzymanej i prowizji. Prowizja może dotyczyć trzeciego aktywa i jest ujmowana jako odrębny koszt.
4. Wpisy wyciągu bankowego są pomijane w agregacji raportu transakcji, ponieważ mogą być powiązane z transakcją i ponowne dodanie ich kwot groziłoby podwójnym liczeniem.
5. Darowizna jest odrębnym rejestrem. Przyjęcie trafia do okresu według `receivedAt`, a przekazanie według `transferredAt`; transfery z darowizn przyjętych wcześniej są zatem widoczne w okresie przekazania.
6. Agregaty darowizn rozdzielają przyjęte, przekazane, przechowywane, gotówkę pozostającą w dyspozycji, środki wpłacone na rachunek i kwoty wymagające weryfikacji. Kategorie przechowywania/weryfikacji odzwierciedlają bieżący status rekordów darowizn przy generowaniu raportu, a nie historyczny stan na koniec przeszłego okresu — model nie przechowuje historii zmian statusów.
7. CSV ma separator średnikowy, wszystkie pola w cudzysłowie, podwajanie cudzysłowów, UTF-8 BOM i ochronę tekstowych komórek zaczynających się (także po białych znakach) od `=`, `+`, `-`, `@`, tabulatora lub CR. Pola kwotowe pozostają polami liczbowymi, nie są prefiksowane apostrofem.
8. CSV zawiera podsumowania aktywów (przychody, rozchody, prowizje, zmiana netto), podsumowania darowizn oraz szczegółowe zdarzenia. Eksport z ekranu obejmuje całą historię.

### Testy i status walidacji

Testy `FinancialReportTest` obejmują:
- rozdzielenie USD, CHF, PLN i złota w gramach;
- dokładność dziesiętną oraz oddzielne ujęcie prowizji;
- kwoty przyjęte, przekazane, przechowywane, wpłacone i wymagające weryfikacji;
- raportowanie przekazania według daty przekazania, także dla darowizny przyjętej przed okresem;
- przedział `[fromInclusive, toExclusive)`;
- UTF-8 BOM, quoting, ochronę tekstu przed formułami i zachowanie precyzji kwoty;
- odrzucenie nieprawidłowego okresu.

Pierwszy przebieg dla kodu raportowego (run `43700475571`) zakończył się sukcesem testów JVM i budowy prototypu. Przebieg GUI dla tego commitu został przerwany przez kolejne aktualizacje plików, zanim zakończyły się testy emulatora. Po dodaniu podsumowań CSV, przycisku SAF i końcowych asercji testowych pełne CI dla commitu a2d5062d4e9694e60e93106288dd54d8166eda25 zakończyło się sukcesem: Android GUI validation run 43700851621, Android prototype build run 43700851615 i repository compliance run 43700851597. Job GUI 132771447929 potwierdził sukces testów JVM, testów Compose na emulatorze, archiwizacji raportów i budowy debug APK. Artefakt instrumentacyjny 14156567496 zawiera 80 testów, 0 awarii, 0 błędów i 0 pominiętych przypadków; w zestawie znajduje się 5 testów BankStatementXmlParserTest, 11 testów DonationAttachmentStorageTest, 23 testy PaymentLedgerDatabaseTest i 8 testów PaymentLedgerRepositoryTest. Test importDeletesPartialFileWhenProviderFailsAfterWritingBytes jest zaliczony. Etap 55 — CSV — uznaje się za zweryfikowany.

### Ograniczenia i następny etap

- Nie dodano jeszcze raportu PDF.
- Eksport CSV jest dostępny z ekranu jako raport całej historii; wybór własnego zakresu dat będzie osobnym usprawnieniem.
- Status przechowywania jest stanem bieżącym, ponieważ schemat nie przechowuje historii statusów.
- Raport nie wycenia aktywów w PLN ani nie wylicza zysku/straty bez jawnych kursów i kosztów nabycia.
- Przed wdrożeniem PDF należy dodać testy renderowania i wielostronicowego układu, a dla CSV wykonać końcową walidację na aktualnym commicie.


## 56. Wielostronicowy raport finansowy PDF (15 listopada 2026 r.)

### 56.1. Cel i zakres

Po zamknięciu walidacji CSV rozpoczęto kolejny etap raportowania: generowanie czytelnego, wielostronicowego raportu PDF na podstawie tego samego modelu FinancialReportBuilder. PDF zachowuje rozdzielenie walut i kruszców, osobne ujęcie prowizji oraz rozdział przepływów darowizn od ich stanów przechowywania. Nie wprowadza przeliczeń aktywów na PLN ani nowych reguł księgowych.

### 56.2. Wykonane zmiany

- Dodano financial/FinancialReportPdf.kt. Renderer wykorzystuje natywne android.graphics.pdf.PdfDocument i Canvas, bez zewnętrznych bibliotek i bez uprawnień do pamięci współdzielonej.
- PDF ma format strony A4, tytuł, zakres raportu, osobne zestawienia aktywów i darowizn oraz szczegółowe zdarzenia.
- Długie opisy są zawijane do szerokości strony; szczegółowe zdarzenia są dzielone na kolejne strony, a każda strona otrzymuje numer.
- PaymentLedgerRepository.exportFinancialReportPdf(period) buduje raport i generuje PDF na Dispatchers.IO.
- PaymentLedgerScreen.kt otrzymał eksport przez Storage Access Framework (CreateDocument z MIME application/pdf) i zapis do lokalizacji wskazanej przez użytkownika. Eksport UI obejmuje całą historię, analogicznie do istniejącego CSV.
- Dodano FinancialReportPdfTest.kt: test pustego raportu jako poprawnego jednostronicowego PDF oraz test długiego raportu, który musi zostać rozłożony na więcej niż jedną stronę. Test sprawdza sygnaturę PDF, możliwość otwarcia przez PdfRenderer, wymiary strony i liczbę stron.

### 56.3. Zmienione pliki i rewizje

- obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/FinancialReportPdf.kt — nowy renderer; commit 416b2e5e6f6f9b0f6c6451a1b2208f8398699ca8.
- obywatel-android/app/src/main/java/pl/gov/obywatel/android/PaymentLedgerScreen.kt — przycisk eksportu PDF i zapis SAF; commit 7e46fd18c5eaa6343a71d782306cd1e03b7de907.
- obywatel-android/app/src/main/java/pl/gov/obywatel/android/financial/PaymentLedgerRepository.kt — generowanie PDF poza głównym wątkiem; commit c29c375f4c89202fa772c709c38761a9e0e0588b.
- obywatel-android/app/src/androidTest/java/pl/gov/obywatel/android/financial/FinancialReportPdfTest.kt — testy renderowania i wielostronicowości; commit b1831c5129ca53032f3f58563f5994bc6d28b14a.

### 56.4. Walidacja i status

Pierwszy pełny przebieg dla commitu `1a65ec1550fe31799f47f80b3624263db689d925` wykazał: Android prototype build — sukces (run `43704484468`), repository compliance — sukces (run `43704484394`), Android GUI validation — niepowodzenie (run `43704484493`). Testy JVM przeszły, lecz kompilacja źródeł instrumentacyjnych zakończyła się błędem w `FinancialReportPdfTest.kt`: konstruktor `FinancialTransaction` wymagał jawnego przekazania `fromAssetCode` i `fromAmount`. Błąd nie dotyczył renderera PDF; test nie został uruchomiony. Poprawkę zapisano w commicie `212963efd1180c2cb63b4f012bc969a0b59d8693`, przekazując oba pola jako `null`. Równolegle wzmocniono test tak, aby otwierał i renderował każdą stronę przez `PdfRenderer`, nie tylko pierwszą (commit `745fe24e84841a2addec0c094e7a00acee661e6e`). Po tych zmianach uruchomiono nowy pełny przebieg CI dla aktualnej rewizji; wynik pozostaje oczekujący. Etapu PDF nie uznaje się za zweryfikowany do czasu pomyślnego zakończenia kompilacji testów instrumentacyjnych i emulatora.

### 56.5. Ograniczenia i następne prace

- Interfejs PDF, podobnie jak CSV, eksportuje obecnie całą historię; wybór zakresu dat pozostaje osobnym zadaniem.
- Test emulatorowy nie zastępuje ręcznego sprawdzenia pliku w zewnętrznych czytnikach PDF ani testu zapisu przez dostawcę SAF na fizycznym urządzeniu.
- Raport nadal nie wycenia aktywów w PLN i nie wylicza zysku/straty bez jawnych kursów oraz kosztów nabycia.
- Stany przechowywania darowizn nadal odzwierciedlają bieżące statusy rekordów, ponieważ schemat nie ma historii zmian statusów.
- Po walidacji PDF kolejnym krokiem jest dodanie wyboru zakresu dat dla CSV i PDF, a następnie testów granicznych zakresu i eksportu SAF.


## 57. Wybór zakresu dat dla raportów CSV i PDF (15 listopada 2026 r.)

### 57.1. Cel

Umożliwienie przygotowania raportu za wskazany okres kalendarzowy zamiast ograniczenia eksportu z ekranu do całej historii. Użytkownik podaje datę początkową i końcową w formacie ISO `RRRR-MM-DD`; obie daty są włączne.

### 57.2. Zmiany w modelu okresu

W `FinancialReportPeriod` dodano fabrykę `fromInclusiveDates(startDate, endDateInclusive, zoneId)`:
- zamienia datę początkową na początek dnia w strefie lokalnej urządzenia;
- zamienia datę końcową na początek następnego dnia, co pozwala zachować włączne znaczenie daty końcowej przy wewnętrznym przedziale `[fromInclusive, toExclusive)`;
- odrzuca datę końcową wcześniejszą niż początkowa;
- uwzględnia reguły strefy czasowej, w tym dni o długości 23 lub 25 godzin podczas zmiany czasu.

### 57.3. Zmiany interfejsu

W `PaymentLedgerScreen.kt` dodano dwa pola dat oraz walidację wejścia przed uruchomieniem eksportu. CSV i PDF otrzymują ten sam wyliczony obiekt okresu, więc zakres raportowania jest spójny. Domyślnie pola wskazują bieżący miesiąc kalendarzowy od pierwszego dnia do dnia dzisiejszego. Błędny format lub odwrócony zakres blokuje uruchomienie eksportu i wyświetla komunikat błędu.

### 57.4. Testy

W `FinancialReportTest.kt` dodano test:
- obejmujący oba dni zakresu przechodzącego przez zmianę czasu w strefie `Europe/Warsaw`, z asercjami dokładnych granic UTC;
- odrzucający zakres, w którym data końcowa poprzedza początkową.

W `FinancialReportPdfTest.kt` wzmocniono sprawdzenie PDF: test renderuje każdą stronę przez `PdfRenderer`, sprawdza jej wymiary i zwalnia bitmapę po renderowaniu. Poprawiono także konstruktor testowej transakcji, jawnie przekazując wymagane pola `fromAssetCode` i `fromAmount` jako `null`.

### 57.5. Zmienione pliki i commity

- `FinancialReport.kt` — fabryka lokalnego zakresu dat; commit `d253ee3155b7de607b338c15b6cf739a5ed15df4`.
- `FinancialReportTest.kt` — testy granic i zmiany czasu; commit `b08e90939ac6dde4c006d2d6eea34c9a53f0824b`.
- `PaymentLedgerScreen.kt` — pola dat i wspólny zakres dla CSV/PDF; commit `684ebc5092b7e4f29da9d1bdc791d04586403466`.
- `FinancialReportPdfTest.kt` — renderowanie każdej strony; commit `745fe24e84841a2addec0c094e7a00acee661e6e`.
- `FinancialReportPdfTest.kt` — jawne wartości pól wymaganych konstruktorem; commit `212963efd1180c2cb63b4f012bc969a0b59d8693`.

### 57.6. Status weryfikacji

Kontrola zgodności dla pośrednich rewizji zakończyła się sukcesem. Przebieg Android GUI dla commitu `1a65ec1` wykrył błąd kompilacji testu PDF opisany w sekcji 56.4; poprawka została zapisana. Pełne CI dla najnowszego commitu `212963efd1180c2cb63b4f012bc969a0b59d8693` jest wymagane do zamknięcia etapu 57 i ponownej weryfikacji etapu 56. Do czasu zakończenia tego przebiegu funkcjonalność zakresu dat i PDF pozostaje wdrożona w kodzie, lecz niezweryfikowana w pełnym CI.

### 57.7. Ograniczenia i dalsze prace

- Daty są obecnie wpisywane ręcznie w formacie `RRRR-MM-DD`; nie dodano jeszcze kalendarzowego selektora dat.
- Domyślny zakres to bieżący miesiąc, nie cała historia. Użytkownik może wpisać wcześniejszy początek, aby objąć dłuższy okres.
- Konwersja odbywa się według strefy czasowej urządzenia. Raport przechowuje granice jako chwile UTC.
- W dalszym etapie należy dodać testy interfejsu walidacji pól, sprawdzić zapis obu formatów przez SAF na emulatorze oraz rozważyć selektor kalendarzowy.


## 58. Kalendarzowy wybór dat i testy eksportu SAF (15 listopada 2026 r.)

### 58.1. Cel

Zastąpienie ręcznego wpisywania dat w raporcie CSV/PDF selektorem kalendarzowym oraz rozszerzenie walidacji interfejsu i zapisu dokumentów przez ContentResolver. Zachowany zostaje wspólny zakres raportu dla obu formatów oraz semantyka dat początkowej i końcowej włącznie.

### 58.2. Wdrożone zmiany

- W financial/FinancialReportDateRangePicker.kt dodano wielokrotnego użytku komponent Compose z dwoma selektorami Material 3 DatePicker: daty początkowej i końcowej.
- Wybrany dzień jest przenoszony jako data kalendarzowa (bez przesunięcia zależnego od strefy urządzenia); dopiero FinancialReportPeriod.fromInclusiveDates mapuje granice na chwile w lokalnej strefie urządzenia.
- Anulowanie lub zamknięcie kalendarza nie zmienia wybranej daty.
- Komponent pokazuje komunikat, gdy data końcowa poprzedza początkową. Dodatkowo dotychczasowa walidacja selectedReportPeriod() nadal blokuje uruchomienie eksportu dla odwróconego lub niepoprawnego zakresu.
- W PaymentLedgerScreen.kt ręczne pola tekstowe zastąpiono komponentem kalendarzowym. CSV i PDF nadal korzystają z jednego pendingReportPeriod.
- Dodano FinancialReportDateRangePickerTest.kt: otwieranie kalendarza i zatwierdzenie wybranej daty, anulowanie bez zmiany stanu oraz komunikat dla odwróconego zakresu.
- Dodano FinancialReportSafExportTest.kt: zapis bajtów CSV (z BOM i separatorem) oraz PDF przez ContentResolver/FileProvider, kontrola zachowania bajtów i test błędu nieistniejącego dostawcy.

### 58.3. Zmienione pliki i rewizje

- PaymentLedgerScreen.kt — integracja selektora dat; commit c63d93575ec84a78f62ff2bd3aa2e340a5e25b1c.
- financial/FinancialReportDateRangePicker.kt — nowy komponent kalendarzowy; kolejne korekty układu i jednostek dp w commitach 2b7911bf48036ce93448b21ad269b5afa247f8d1, 9dd01ed8ba69f34548a4b4f603767e49f4d9e5ed i e50201f8ed29956c2e2c438897a2c184fa58732f.
- FinancialReportDateRangePickerTest.kt — testy Compose; commit 5f81d068be5d6d5258278251043651d6d977439e.
- FinancialReportSafExportTest.kt — testy zapisu przez ContentResolver; commit f5dc541eed1c384058cd08db44fcae2223a6c711.

### 58.4. Walidacja i status

Zmiany źródłowe i testowe zostały zapisane na gałęzi feature/payment-ledger-stage-1. Po zapisaniu sekcji audytu należy uruchomić pełne CI dla nowego HEAD. W chwili przygotowania tej sekcji nie ma jeszcze wyników testów dla commitów etapu 58; nie uznaje się zatem etapu za zweryfikowany.

Dla poprzedniej rewizji 23384cc106999694dd25eb9de74feeee993d8551:
- Android prototype build run 43705069384 zakończył się sukcesem; artefakt obywatel-android-debug ID 14158476988, SHA-256 f633d4687122517aa19b84b34697e72f80d77406666a5f1f7655f7523bfa51e9.
- Repository compliance run 43705069378 zakończył się sukcesem.
- Android GUI validation run 43705069390 pozostawał w stanie in_progress; krok testów Compose emulatorowych nie miał wyniku końcowego ani raportu instrumentacyjnego. Ten przebieg nie waliduje zmian etapu 58.

### 58.5. Ograniczenia i dalsze kontrole

- Testy SAF wykorzystują testowy FileProvider i ContentResolver; nie są testem wyboru dostawcy plików przez użytkownika ani fizycznego urządzenia. Anulowanie wyboru dokumentu w launcherze pozostaje obsługiwane przez istniejące zachowanie callbacku uri == null, które nie rozpoczyna zapisu.
- Testy Compose trzeba potwierdzić na emulatorze w nowym przebiegu CI. W szczególności należy sprawdzić semantykę DatePicker na docelowym obrazie emulatora.
- Należy potwierdzić, że testy PDF renderują każdą stronę, testy raportu JVM przechodzą, a zapis CSV/PDF przez ContentResolver przechowuje dokładne bajty.
- Do czasu pomyślnego zakończenia nowego Android GUI validation, Android prototype build i repository compliance etap 58 pozostaje wdrożony, lecz niezweryfikowany.


### 58.6. Korekty po przeglądzie kodu

Przed uruchomieniem walidacji końcowej uproszczono układ komponentu selektora, usuwając zbędne kontenery wierszy i eliminując niepoprawne odwołanie do jednostki dp. Zmieniono też test nieistniejącego dostawcy SAF tak, aby sprawdzał wystąpienie wyjątku wejścia/wyjścia bez uzależniania testu od konkretnej klasy wyjątku implementacji ContentResolver.

- Ostateczna korekta komponentu FinancialReportDateRangePicker.kt — commit d41e337e3a03f3eec2e2a23c91b5c29ed8b17fa8.
- Korekta asercji błędu dostawcy w FinancialReportSafExportTest.kt — commit 547053b9723addb748a15c58e35295f45a2b4583.

Obie poprawki zostały ponownie pobrane z gałęzi i sprawdzone. Wyniki kompilacji i testów dla tych rewizji są nadal oczekujące.

### 58.7. Naprawa błędu kompilacji wykrytego w CI (15 listopada 2026 r.)

Pierwsza walidacja etapu 58 na HEAD `d99749467a7d5d8912e64fa2c60495c1f259b081` wykazała błąd kompilacji Kotlin w `FinancialReportDateRangePicker.kt`. Zadania `:app:compileDebugKotlin` zgłosiły użycie eksperymentalnego API Material 3 bez jawnej zgody kompilatora. Błąd powtórzył się w dwóch niezależnych przebiegach:
- Android GUI validation run `43705829655`, job `132786585199` — niepowodzenie w kroku `Run Android unit tests`;
- Android prototype build run `43705829550`, job `132786577817` — niepowodzenie w kroku `Run Android JVM unit tests`.
- Repository compliance run `43705829528` — sukces.

Wskutek błędu testy Compose na emulatorze, budowa debug APK i publikacja artefaktu zostały pominięte. Nie powstał artefakt APK ani raport instrumentacyjny dla tego przebiegu.

Korekta:
- Dodano import `androidx.compose.material3.ExperimentalMaterial3Api`.
- Oznaczono funkcję `FinancialReportDateRangePicker` adnotacją `@OptIn(ExperimentalMaterial3Api::class)`, ograniczając zgodę na eksperymentalne API do komponentu, który używa `DatePicker` i `DatePickerDialog`.
- Commit poprawki: `89183d9ea57f2ed78b7720ed7f13051f80944024`.

Status po poprawce: zmiana jest zapisana na gałęzi, ale wymaga ponownego pełnego CI. Nie uznaje się etapu 58 za zweryfikowany do czasu sukcesu testów JVM, testów instrumentacyjnych/Compose, budowy APK i kontroli zgodności dla dokładnego nowego HEAD.

## 59. Wzmocnienie testów kalendarzowego wyboru zakresu raportu (15 listopada 2026 r.)

### 59.1. Cel

Uzupełnienie testów komponentu `FinancialReportDateRangePicker` o rzeczywistą zmianę wybranej daty. Dotychczasowe testy potwierdzały otwarcie kalendarza, zatwierdzenie daty początkowej bez zmiany wartości, anulowanie oraz komunikat dla odwróconego zakresu. Nie sprawdzały, czy wskazanie innego dnia aktualizuje właściwą granicę i pozostawia drugą datę bez zmian.

### 59.2. Wykonane zmiany

W pliku `obywatel-android/app/src/androidTest/java/pl/gov/obywatel/android/financial/FinancialReportDateRangePickerTest.kt` dodano dwa testy Compose:
- `selectingDifferentStartDateUpdatesOnlyStartBoundary` — wybór 10 listopada 2026 r. zmienia datę początkową z 1 na 10 listopada i zachowuje datę końcową 20 listopada;
- `selectingDifferentEndDateUpdatesOnlyEndBoundary` — wybór 25 listopada 2026 r. zmienia datę końcową z 15 na 25 listopada i zachowuje datę początkową 1 listopada.

Testy sprawdzają callback komponentu i końcową parę dat, a więc weryfikują obie ścieżki aktualizacji, a nie tylko samo wyświetlenie okna dialogowego.

### 59.3. Rewizja

- Zmieniony plik: `FinancialReportDateRangePickerTest.kt`.
- Commit: `8303bb9fab5c8731f44aab7006d1ca584d7a5dfb`.
- Gałąź: `feature/payment-ledger-stage-1`.

### 59.4. Walidacja

- Repository compliance (alfa), run `43710469128` — **success** dla commitu `8303bb9`.
- Android prototype build, run `43710469143` — uruchomiony; wynik testów JVM i budowy APK oczekuje na zakończenie.
- Android GUI validation, run `43710469168` — uruchomiony; wynik testów JVM, Compose na emulatorze i budowy APK oczekuje na zakończenie.

Etap 59 jest wdrożony w kodzie, lecz pozostaje **niezweryfikowany testami Android** do czasu zakończenia obu przebiegów i sprawdzenia, że nowe testy wyboru dni przeszły na emulatorze. Nie należy uznawać samego uruchomienia zadań ani kontroli REUSE za potwierdzenie poprawności testów interfejsu.

### 59.5. Dalsze kontrole

- Potwierdzić, że selektor Material 3 udostępnia jednoznaczny węzeł dnia w semantyce Compose na obrazie emulatora CI.
- Jeżeli testy nie przejdą z powodu selektora węzła, poprawić asercje na podstawie rzeczywistego drzewa semantyki, bez osłabiania sprawdzenia zmiany daty.
- Po pomyślnym CI zachować PR `#68` otwarty do dalszego przeglądu.

## 60. Naprawa struktury testów kalendarza i kontrola granic okresu przy zmianie czasu (15 listopada 2026 r.)

### 60.1. Ustalenia przeglądu

Ponowny przegląd gałęzi wykazał, że dwa testy dodane w etapie 59 znajdowały się poza zamykającym nawiasem klasy `FinancialReportDateRangePickerTest`. Plik Kotlin zawierał przez to nieprawidłową strukturę deklaracji. Kontrola zgodności REUSE nie wykrywa tego rodzaju błędu kompilacji.

### 60.2. Wykonane poprawki

- Przeniesiono testy `selectingDifferentStartDateUpdatesOnlyStartBoundary` i `selectingDifferentEndDateUpdatesOnlyEndBoundary` do wnętrza klasy testowej.
- Zachowano asercje sprawdzające, że wybór dnia aktualizuje tylko wskazaną granicę zakresu, pozostawiając drugą datę bez zmian.
- Commit: `58c723174d831c6cadfe92fa314be5306ef1aac1`.
- Ponownie pobrano plik z gałęzi i sprawdzono jego końcową strukturę: oba testy są wewnątrz klasy, a plik ma pojedyncze zamknięcie klasy.

### 60.3. Dodatkowy test granic raportu w strefie Europe/Warsaw

W pliku `obywatel-android/app/src/test/java/pl/gov/obywatel/android/financial/FinancialReportTest.kt` dodano test `reportIncludesBothLocalCalendarDaysAcrossDstAndExcludesFollowingMidnight`.

Test buduje okres obejmujący 28 i 29 marca 2026 r. w strefie `Europe/Warsaw`, czyli weekend zmiany czasu na letni. Sprawdza trzy punkty:
- początek pierwszego dnia lokalnego — uwzględniony;
- ostatnia nanosekunda drugiego dnia lokalnego — uwzględniona;
- dokładna chwila początku kolejnego dnia lokalnego — wykluczona.

Kontrolowana jest również suma wartości dwóch uwzględnionych transakcji. Test obejmuje logikę `FinancialReportBuilder`, a nie tylko metodę `contains`.

- Commit: `d0733f4dfed662c6df5a8a8a097ff8eb281263d0`.

### 60.4. Status walidacji

- Potwierdzono zapis obu zmian na gałęzi `feature/payment-ledger-stage-1`.
- Wcześniejszy przebieg `Android prototype build` dla commitu `c87d29b` zakończył się sukcesem, ale nie obejmował naprawy struktury testu z commitu `58c7231) ani testu DST z commitu `d0733f4`.
- Wcześniejszy przebieg `Android GUI validation` dla `c87d29b` pozostawał w toku w chwili odczytu; nie stanowi potwierdzenia testów dla nowych commitów.
- Pełne CI dla aktualnego HEAD jest wymagane przed uznaniem etapu 60 za zweryfikowany. Należy sprawdzić kompilację testów JVM, testy instrumentacyjne DatePicker, testy Compose, budowę APK i kontrolę REUSE.

### 60.5. Kryteria zamknięcia

Etap można uznać za zweryfikowany dopiero po pomyślnym zakończeniu wszystkich wymaganych zadań CI dla dokładnego HEAD. W razie błędu testu DatePicker należy poprawić selektor semantyczny na podstawie rzeczywistego drzewa Compose, bez usuwania asercji potwierdzających zmianę wybranej daty. PR `#68` pozostaje otwarty.


## 61. Naprawa testów wyboru dnia w kalendarzu raportu (15 listopada 2026 r.)

### 61.1. Przyczyna wykryta w CI

Przebieg Android GUI dla commitu `83026e8111d575621d703ace3fbac6060ffa290e` zakończył się niepowodzeniem w zadaniu `:app:connectedDebugAndroidTest`. Testy JVM przeszły. Nie przeszły wyłącznie dwa testy Compose:

- `selectingDifferentStartDateUpdatesOnlyStartBoundary`;
- `selectingDifferentEndDateUpdatesOnlyEndBoundary`.

Raport testów wskazał błąd `Failed to inject touch input`: selektory tekstowe `"10"` i `"25"` nie znajdowały w drzewie semantyki kalendarza węzłów tekstowych. Nie był to błąd logiki wyznaczania dat w komponencie, lecz niezgodność testu z semantyką dostępności Material 3 DatePicker.

### 61.2. Wykonana poprawka

Zastąpiono selektory tekstu liczbowego selektorami dostępnościowego opisu pełnej daty:

- `Tuesday, November 10, 2026`;
- `Wednesday, November 25, 2026`.

Testy nadal wybierają inne dni niż wartości początkowe i nadal sprawdzają, że zmienia się wyłącznie wskazana granica okresu.

- Plik: `FinancialReportDateRangePickerTest.kt`.
- Commit: `54159060eac0bdd7f55cd62e51435d06803d2f94`.

### 61.3. Status

Poprawka została zapisana na gałęzi. Weryfikacja emulatorowa musi potwierdzić, że opisy dostępnościowe są zgodne z lokalizacją i wersją Material 3 używaną przez obraz CI. Sam zapis poprawionego selektora nie zamyka etapu; wymagany jest sukces `connectedDebugAndroidTest` dla dokładnego aktualnego HEAD.

## 62. Wspólny, testowalny zapis raportów CSV i PDF przez SAF (15 listopada 2026 r.)

### 62.1. Ustalenie przeglądu

Ekran raportów uruchamiał systemowy wybór lokalizacji przez `ActivityResultContracts.CreateDocument`, ale zapisywanie bajtów CSV i PDF było zduplikowane w dwóch callbackach. Dotychczasowe testy SAF sprawdzały zapis przykładowych bajtów bezpośrednio przez `ContentResolver`; nie wywoływały wspólnej implementacji używanej przez ekran ani rzeczywistych koderów raportów.

### 62.2. Wdrożone zmiany

Dodano `financial/FinancialReportSafWriter.kt`:

- wspólna metoda zapisuje przekazane bajty przez `ContentResolver` do URI zwróconego przez systemowy selektor dokumentów;
- używa trybu `wt`, jawnie zastępującego wcześniejszą zawartość dokumentu;
- zgłasza błąd, jeżeli dostawca nie udostępnia strumienia zapisu;
- zamyka strumień także w przypadku wyjątku podczas zapisu.

W `PaymentLedgerScreen.kt` callbacki eksportu CSV i PDF korzystają teraz z tej samej metody. Kodowanie raportów pozostaje odpowiednio UTF-8 (w tym BOM dla CSV) i bajtami PDF generowanymi przez `FinancialReportPdf`.

W `FinancialReportSafExportTest.kt` zastąpiono zapis bezpośredni wywołaniem produkcyjnego `FinancialReportSafWriter`. Testy przekazują do niego rzeczywiste wyniki `FinancialReportCsv.encode` i `FinancialReportPdf.encode`, sprawdzają zachowanie pełnych bajtów, BOM CSV i sygnaturę PDF. Test niedostępnego dostawcy sprawdza teraz błąd wspólnej metody.

### 62.3. Zmienione pliki i commity

- `FinancialReportSafWriter.kt` — nowy wspólny zapis; commit `1c6096f13030cafe2f0dd3f8d985b949d75da300`.
- `PaymentLedgerScreen.kt` — użycie wspólnej metody w eksporcie CSV/PDF; commit `6ac3b6a96465cb33798d7381f5f91fa3ac78011b`.
- `FinancialReportSafExportTest.kt` — test rzeczywistych koderów i wspólnego zapisu; commit `421734da7e4f8cec5618c2dda8acfe0eaac234f8`.
- `FinancialReportSafExportTest.kt` — test zastępowania wcześniejszej zawartości; commit `bcf5085dca711d9ba23c2e52dcca1ddac4fad1b2`.
- `PaymentLedgerScreen.kt` — import klasy zapisującej; commit `c17d6d6f5e64b727310e79ed6ec42c15ee6427f1`.

### 62.4. Status walidacji

Zmiany są zapisane na gałęzi `feature/payment-ledger-stage-1`. Przebiegi CI uruchomione dla wcześniejszych rewizji w trakcie tych zmian nie potwierdzają poprawności końcowego HEAD. Do zamknięcia etapu wymagane są dla dokładnego końcowego SHA:

- testy JVM;
- testy instrumentacyjne, w tym oba testy wyboru dat i testy rzeczywistego zapisu CSV/PDF;
- budowa debug APK;
- kontrola zgodności REUSE.

Do czasu zakończenia tych kontroli etapy 61–62 pozostają wdrożone w kodzie, ale niezweryfikowane w pełnym CI. PR `#68` pozostaje otwarty i niescalony.


## 63. Naprawa kompilacji testów Compose kalendarza (15 listopada 2026 r.)

### 63.1. Wynik kontroli CI dla etapu 62

Dla commitu `d011fcf4e97772a656782badb3d5f74c641f6542`:

- Repository compliance (alfa), run `43711825198` — **success**.
- Android prototype build, run `43711825175` — **success**.
- Android GUI validation, run `43711825166` — **failure**.

Testy JVM zakończyły się sukcesem. Testy instrumentacyjne nie doszły do wykonania scenariuszy na emulatorze, ponieważ kompilacja źródeł `androidTest` zakończyła się błędem:

`FinancialReportDateRangePickerTest.kt:8:33 Unresolved reference 'onNode'`.

Błąd wynikał z dodania importu `androidx.compose.ui.test.onNode`, którego nie udostępnia wersja Compose UI Test używana w tym projekcie. Nie był to błąd aplikacji ani testów logiki zakresu dat.

### 63.2. Wykonana poprawka

W pliku `FinancialReportDateRangePickerTest.kt`:

- usunięto import niedostępnego `onNode`;
- usunięto niepotrzebny matcher `hasContentDescription`;
- zastosowano publiczny selektor `onNodeWithContentDescription` z pakietu `androidx.compose.ui.test`;
- zachowano selekcję pełnego opisu dnia oraz asercje, że aktualizowana jest wyłącznie właściwa granica okresu.

Commit: `1d5949e2a31fd9f57179ebc8f2cf0d818b221523`.

### 63.3. Walidacja po poprawce

Dla commitu `1d5949e2a31fd9f57179ebc8f2cf0d818b221523`:

- Repository compliance (alfa), run `43713289536` — **success**.
- Android prototype build, run `43713289546` — uruchomiony.
- Android GUI validation, run `43713289577` — uruchomiony.

Do zamknięcia etapu wymagane jest zakończenie obu przebiegów Android dla tego samego SHA. W szczególności należy potwierdzić kompilację `androidTest`, wykonanie testów DatePicker i SAF na emulatorze oraz utworzenie debug APK. Do tego czasu etap 63 pozostaje poprawką zapisaną, ale niepotwierdzoną pełnym CI.


## 64. Korekta selektorów dni kalendarza po walidacji emulatorowej (15 listopada 2026 r.)

### 64.1. Wynik pełnego przebiegu dla HEAD 4bead0f

- Repository compliance, run `43713349221` — **success**.
- Android prototype build, run `43713349254` — **success**; testy JVM i budowa debug APK zakończone powodzeniem; artefakt `obywatel-android-debug` został opublikowany.
- Android GUI validation, run `43713349290` — **failure** w `:app:connectedDebugAndroidTest`.
- Wykonano 91 testów instrumentacyjnych; 89 zakończyło się powodzeniem, 2 niepowodzeniem.
- Niepowodzenia dotyczyły wyłącznie `selectingDifferentStartDateUpdatesOnlyStartBoundary` i `selectingDifferentEndDateUpdatesOnlyEndBoundary`. Selektory `onNodeWithContentDescription` nie znalazły dni pod oczekiwanym angielskim opisem. Nie jest to potwierdzenie błędu logiki zakresu dat; testy nie dotarły do kliknięcia dnia i sprawdzenia callbacku.

### 64.2. Wykonana korekta

W `FinancialReportDateRangePickerTest.kt` zastąpiono zależne od języka opisy dostępnościowe selektorami tekstu dnia w niepołączonym drzewie semantyki Compose:

- dzień 10: `onNodeWithText("10", useUnmergedTree = true)`;
- dzień 25: `onNodeWithText("25", useUnmergedTree = true)`.

Asercje końcowe pozostały bez zmian: test początku wymaga pary `2026-11-10 / 2026-11-20`, a test końca `2026-11-01 / 2026-11-25`. Zachowano więc kontrolę aktualizacji tylko jednej granicy okresu.

### 64.3. Ograniczenia i dalsza walidacja

To jest korekta testu, nie dowód jego poprawności. Należy uruchomić pełne Android GUI validation i potwierdzić, że oba selektory trafiają w pojedynczy węzeł dnia na emulatorze oraz że callback przekazuje oczekiwane daty. Jeśli selektor tekstu nadal nie zostanie znaleziony, następnym krokiem jest zebranie drzewa semantyki DatePicker w raporcie testowym i dopasowanie selektora do rzeczywistego drzewa, bez usuwania asercji końcowych.

Etap 64 pozostaje **oczekujący na walidację emulatorową**. PR `#68` pozostaje otwarty i niescalony.


## 65. Uszczelnienie parowania nóg wymiany FX przy ponownym użyciu referencji (15 listopada 2026 r.)

### 65.1. Ustalenie przeglądu

Przegląd `BankImportDeduplicator.preview` wykazał, że wyjątek dopuszczający dwie przeciwne nogi tej samej wymiany w obrębie jednego zakresu rachunku sprawdzał typ operacji, różne waluty i przeciwne znaki kwot, ale nie sprawdzał zgodności dat księgowania. W konsekwencji dwie operacje z ponownie użytym numerem referencyjnym, zaksięgowane w różnych dniach, mogły zostać potraktowane jak dozwolona para FX zamiast konfliktu wymagającego kontroli.

### 65.2. Wykonane zmiany

W `BankImportDeduplicator.kt` dodano warunek równości `bookingDate` do `isOppositeFxLeg`. Dwie nogi mogą skorzystać z wyjątku wyłącznie wtedy, gdy:
- obie są oznaczone jako wymiana w kantorze;
- mają tę samą datę księgowania;
- mają przeciwne etykiety uznania i obciążenia;
- dotyczą różnych walut;
- mają przeciwne znaki kwot.

Dodano testy regresyjne w `BankImportDeduplicatorTest.kt`:
- `oppositeFxLegWithSameReferenceButDifferentBookingDateIsAConflict` — ponownie użyta referencja z datą kolejnego dnia jest klasyfikowana jako konflikt;
- `oppositeFxLegWithSameReferenceAndDateButSameDirectionIsAConflict` — dwa obciążenia nie są uznawane za prawidłową parę.

### 65.3. Rewizje i kontrola zapisu

- `BankImportDeduplicator.kt` — commit `c1b95fca08cc54615d4f4eb5cb21931ca59f9baf`.
- `BankImportDeduplicatorTest.kt` — commit `6dce33c8fb8bd004d4e130dbe0745a83f753dea7`.
- Końcowy HEAD sekcji: `6dce33c8fb8bd004d4e130dbe0745a83f753dea7`.
- Po ponownym pobraniu z GitHuba potwierdzono obecność warunku zgodności daty oraz obu testów regresyjnych.

### 65.4. Walidacja CI

Dla końcowego HEAD `6dce33c8fb8bd004d4e130dbe0745a83f753dea7` uruchomiono:
- Repository compliance (alfa), run `43714576528` — **queued**;
- Android prototype build, run `43714576588` — **queued**;
- Android GUI validation, run `43714576480` — **queued**.

Poprzedni przebieg dla HEAD `734e9c99f91fba2e7645d5483e7ea3b723563be0` miał zakończone powodzeniem testy jednostkowe oraz build prototypu, ale walidacja emulatorowa została anulowana wskutek późniejszego pushu. Przebiegi dla pośredniego commitu `c1b95fca08cc54615d4f4eb5cb21931ca59f9baf` również zostały anulowane po kolejnym pushu. Żaden z tych wyników nie potwierdza pełnej poprawności końcowego HEAD sekcji 65.

### 65.5. Status i dalsze kryteria

Zmiana kodu i testy regresyjne są zapisane oraz ponownie odczytane z gałęzi. Etap 65 pozostaje **wdrożony, lecz niezweryfikowany CI**. Do jego zamknięcia wymagane są: powodzenie testów JVM, testów instrumentacyjnych/Compose, budowy debug APK i kontroli zgodności dla dokładnego HEAD `6dce33c8fb8bd004d4e130dbe0745a83f753dea7`. PR `#68` pozostaje otwarty i niescalony.


## 66. Naprawa testów wyboru dni w kalendarzu zakresu raportu (15 listopada 2026 r.)

### 66.1. Wynik walidacji etapu 65

Dla HEAD `2756e7725b71da04feb8700fcf88b7cfd74faa6e`:
- Repository compliance — sukces;
- Android prototype build — sukces, w tym testy jednostkowe;
- Android GUI validation — **niepowodzenie**;
- testy instrumentacyjne: 91 uruchomionych, 2 niepowodzenia.

Niepowodzenia dotyczyły:
- `FinancialReportDateRangePickerTest.selectingDifferentStartDateUpdatesOnlyStartBoundary`;
- `FinancialReportDateRangePickerTest.selectingDifferentEndDateUpdatesOnlyEndBoundary`.

Raport testów i log wskazują na `java.lang.AssertionError: Failed to inject touch input.` Oba przypadki używały selektora `onNodeWithText(..., useUnmergedTree = true)`. Wymuszenie drzewa niepołączonego wybierało wewnętrzny węzeł tekstowy dnia kalendarza, zamiast węzła z połączoną semantyką elementu klikalnego. Test próbował więc symulować dotyk w węźle, który nie reprezentował właściwego celu interakcji.

### 66.2. Wykonana poprawka

W `FinancialReportDateRangePickerTest.kt` usunięto `useUnmergedTree = true` dla wyboru dni 10 i 25. Testy korzystają teraz ze standardowego, połączonego drzewa semantycznego Compose, które powinno wskazać klikalny element dnia. Asercje końcowe pozostały bez zmian i nadal wymagają potwierdzenia, że zmienia się wyłącznie odpowiednia granica zakresu:
- początek: `2026-11-10 / 2026-11-20`;
- koniec: `2026-11-01 / 2026-11-25`.

Nie osłabiono asercji biznesowych ani nie pominięto testów.

### 66.3. Rewizja

- `FinancialReportDateRangePickerTest.kt` — commit `c85765a93a455beaff7bea4ffc2b7b857e2ff50b`.
- Zmiana dotyczy wyłącznie selektorów w dwóch testach instrumentacyjnych; logika produkcyjnego DatePicker pozostaje bez zmian.

### 66.4. Dodatkowe zabezpieczenie testu i wynik CI

W kolejnym commicie `f11f42f79014b625d3fd082d6bb0733163a19770` dodano `assertIsDisplayed()` przed kliknięciem dnia 10 i 25. Ma to odróżnić brak widoczności celu od błędu wstrzyknięcia dotyku i dostarczyć wcześniejszej, czytelniejszej diagnozy.

Dla HEAD `f11f42f79014b625d3fd082d6bb0733163a19770`:
- Repository compliance, run `43715732276` — **success**;
- Android prototype build, run `43715732258` — **success**;
- Android GUI validation, run `43715732250` — **cancelled** wskutek kolejnego pushu przed zakończeniem testów emulatorowych.

Wynik budowy APK i testów JVM jest pozytywny dla tego SHA, ale testy DatePicker na emulatorze nadal nie mają potwierdzonego wyniku. Etap 66 pozostaje **częściowo zweryfikowany; walidacja GUI oczekuje na nowy pełny przebieg**.


## 67. Ochrona pełnego importu archiwum przed nadpisaniem transakcji (15 listopada 2026 r.)

### 67.1. Ustalenie audytu

Ścieżka `previewArchive()` wykrywała konflikt, gdy istniejąca transakcja finansowa i importowana transakcja miały ten sam identyfikator, lecz różniły się danymi. Ścieżka zatwierdzająca `importJson()` przechodziła jednak do `PaymentLedgerDatabase.importArchive()`, gdzie `saveFinancialTransaction()` aktualizowało rekord o tym samym identyfikatorze. W praktyce można było więc nadpisać istniejącą historię finansową mimo konfliktu wykrywanego przez podgląd.

Dodatkowa niespójność: podgląd porównywał obiekty z użyciem równości `data class`, która dla `BigDecimal` rozróżnia skalę (np. `100.0` i `100.00`), mimo że są to równe wartości księgowe.

### 67.2. Wdrożona poprawka

W `PaymentLedgerDatabase.importArchive()`:
- przed zapisem transakcji pobierane są istniejące rekordy;
- ten sam identyfikator i te same dane oznaczają ponowny, idempotentny import — rekord pozostaje bez zmian;
- ten sam identyfikator z innymi danymi lub innymi powiązaniami do wpisów bankowych powoduje wyjątek i wycofanie całej transakcji SQLite;
- nie jest już wywoływana aktualizująca metoda zapisu dla istniejącego identyfikatora archiwum.

Porównanie tożsamości transakcji obejmuje datę/czas, typ, obie strony aktywów i kwot, prowizję, źródło, identyfikator zewnętrzny, opis, status parsowania oraz zbiór identyfikatorów powiązanych wpisów. Kwoty i prowizje są porównywane numerycznie przez `BigDecimal.compareTo`, a nie przez skalowrażliwą równość obiektów.

W `PaymentLedgerRepository.previewArchive()` zastosowano tę samą semantykę porównania, aby podgląd i zatwierdzenie importu nie rozbiegały się dla równoważnych zapisów dziesiętnych.

### 67.3. Testy regresyjne

Dodano testy instrumentacyjne:
- `sameTransactionIdWithChangedDataIsRejectedAndPreservesExistingRecord` — zmiana kwoty pod istniejącym identyfikatorem jest odrzucana, a poprzedni rekord pozostaje zachowany;
- `identicalTransactionArchiveCanBeImportedRepeatedlyWithoutChangingRecord` — wielokrotny import tej samej transakcji nie tworzy duplikatów;
- `archivePreviewUsesNumericTransactionIdentityAndDetectsChangedAmounts` — podgląd uznaje kwoty różniące się wyłącznie skalą za równoważne, a rzeczywistą zmianę kwoty klasyfikuje jako konflikt.

### 67.4. Zmienione pliki i commity

- `PaymentLedgerDatabase.kt` — kontrola istniejącego identyfikatora i porównanie pełnej tożsamości transakcji; commit `fe14a5d8bc20b2282402a21cf6611cad4acf9fff`, doprecyzowanie typu `BigDecimal` w `fd08ab26071d6396a91a55d219e64a2be7faee24`.
- `PaymentLedgerDatabaseTest.kt` — testy konfliktu i idempotencji; commit `9c0c5d7bdf3e83d1615959e8440c0d680b9ca403`, korekta porównań dziesiętnych w `e640f6bc3a54479c8a5de09f58f7dc00a748546b`.
- `PaymentLedgerRepository.kt` — zgodność semantyki podglądu z zatwierdzaniem; commit `3c5b930c4d48e99d4a8f815696cdfb226d1bd787`.
- `PaymentLedgerRepositoryTest.kt` — test podglądu równoważnych i zmienionych kwot; commit `516966bb693e7f5fc13ce0c2b02088f1248eb2ee`.

### 67.5. Kontrola zapisu i walidacja

Po zapisaniu zmian ponownie pobrano z GitHuba pliki `PaymentLedgerDatabase.kt`, `PaymentLedgerDatabaseTest.kt`, `PaymentLedgerRepository.kt` i `PaymentLedgerRepositoryTest.kt`. Potwierdzono obecność kontroli konfliktu, porównania numerycznego oraz trzech testów regresyjnych.

Końcowy HEAD na moment aktualizacji tej sekcji: `516966bb693e7f5fc13ce0c2b02088f1248eb2ee`.

Przebiegi dla wcześniejszego HEAD `e640f6bc3a54479c8a5de09f58f7dc00a748546b`:
- Repository compliance, run `43716125542` — **success**;
- Android prototype build, run `43716125516` — w toku w chwili aktualizacji;
- Android GUI validation, run `43716125495` — **cancelled** po kolejnym pushu.

Dla końcowego HEAD `516966bb693e7f5fc13ce0c2b02088f1248eb2ee`:
- Android prototype build, run `43716177341` — oczekujący;
- Repository compliance, run `43716177346` — w kolejce;
- nie stwierdzono jeszcze potwierdzonego pełnego przebiegu Android GUI dla tego SHA.

Etap 67 jest **zapisany i sprawdzony w źródłach, ale oczekuje na pełną walidację CI**. Nie uznaje się jeszcze testów instrumentacyjnych ani APK za zaliczone dla końcowego HEAD. PR `#68` pozostaje otwarty i niescalony.

## 68. Pełna kontrola konfliktów danych darowizn przy imporcie archiwum (15 listopada 2026 r.)

### 68.1. Ustalenie

Kontrola identyfikatora darowizny w imporcie archiwum porównywała wcześniej tylko pięć pól: datę przyjęcia, kwotę, walutę, beneficjenta i cel. Pozostałe dane — w szczególności darczyńca, anonimowość, kampania, osoba przyjmująca, numer pokwitowania, status przechowywania, data i kwota przekazania, notatka oraz źródło — mogły różnić się przy tym samym identyfikatorze, a import nie zgłaszał konfliktu. Podgląd archiwum używał tego samego niepełnego kryterium.

### 68.2. Wykonane zmiany

W `PaymentLedgerDatabase.importArchive()` rozszerzono odczyt istniejącej darowizny i porównanie na wszystkie pola modelu `CashDonation`. Weryfikacja obejmuje:
- datę przyjęcia, kwotę i walutę;
- beneficjenta oraz cel;
- nazwę darczyńcy i znacznik anonimowości;
- kampanię, osobę przyjmującą i numer pokwitowania;
- status przechowywania;
- datę i kwotę przekazania;
- notatkę oraz źródło rekordu.

Kwoty są porównywane numerycznie przez `BigDecimal.compareTo`, więc sama różnica skali zapisu dziesiętnego nie tworzy fałszywego konfliktu. Zmiana dowolnego z pozostałych pól przy tym samym ID powoduje odrzucenie importu w transakcji SQLite.

W `PaymentLedgerRepository.previewArchive()` zastosowano zgodne kryterium pełnej tożsamości darowizny. Podgląd i zatwierdzenie importu wykrywają więc ten sam rodzaj konfliktu.

### 68.3. Test regresyjny

Dodano rozszerzony test instrumentacyjny:
- `sameDonationIdWithChangedMetadataIsReportedAsConflictAndPreservesRecord` — testuje dziewięć zmian metadanych (darczyńca, anonimowość, kampania, osoba przyjmująca, numer pokwitowania, status przechowywania, dane przekazania, notatka i źródło). Dla każdej zmiany podgląd zgłasza konflikt, import zostaje odrzucony, a pierwotny rekord pozostaje zachowany.
- `donationImportTreatsEquivalentBigDecimalScaleAsDuplicate` — potwierdza, że równoważne kwoty darowizny zapisane z inną skalą dziesiętną są traktowane jako duplikat, a nie konflikt; sprawdzany jest także brak nadpisania istniejącego rekordu.
- Asercja odczytu normalizuje skalę `BigDecimal`, ponieważ SQLite zapisuje wartości po `stripTrailingZeros()`.

### 68.3a. Korekta po nieudanej kompilacji testów (15 listopada 2026 r.)

Przebieg Android GUI validation dla SHA `10f2f0f9677c3584a8887d1c2fe46fd22af6dd8f` zakończył się niepowodzeniem na etapie `compileDebugAndroidTestKotlin`, przed uruchomieniem testów na emulatorze. Log kompilatora wskazał, że dwa testy importu transakcji zostały umieszczone w `PaymentLedgerDatabaseTest`, mimo że używały `PaymentLedgerRepository`, `runBlocking` i pola `conflictDatabase` z klasy `PaymentLedgerRepositoryTest`. Dodatkowo klasa bazy nie miała importu `assertTrue`.

Naprawa:
- usunięto dwa testy repozytorium z `PaymentLedgerDatabaseTest.kt`;
- przeniesiono je bez zmian merytorycznych do `PaymentLedgerRepositoryTest.kt`, który ma właściwe importy i konfigurację bazy;
- zachowano oba przypadki: odrzucenie zmienionej transakcji pod tym samym ID oraz idempotentny ponowny import identycznego archiwum.

Zmiany:
- `PaymentLedgerDatabaseTest.kt` — commit `d9e90db57750b52e54a4d8381aad8f4674f34705`;
- `PaymentLedgerRepositoryTest.kt` — commit `de7de2de13c3e4ee9b769c5183f3931cd3f27c3f`.

Po poprawce ponownie pobrano oba pliki z gałęzi i sprawdzono, że testy repozytorium nie występują już w klasie testowej bazy, a oba znajdują się w klasie repozytorium. Pełna walidacja CI dla nowego HEAD jest w toku.

### 68.4. Zmienione pliki i commity

- `PaymentLedgerDatabase.kt` — pełne porównanie danych darowizny; commit `aaf5ee534b7d51b198c5b8d6b0f49e587194a794`.
- `PaymentLedgerRepository.kt` — zgodna kontrola w podglądzie; commit `07cb282621a543628c58363666d6d374fa4ac5a1`.
- `PaymentLedgerRepositoryTest.kt` — rozszerzony test dziewięciu pól metadanych i test idempotencji równoważnych kwot; commit `53251f0c35cc15a6a66d0216c762ae53334c7206`; korekta asercji skali dziesiętnej; commit `1d5ff946550013414ebccd78734abca035ae76c0`; dodatkowy test idempotencji równoważnych kwot darowizn; commit `e92db4aae375687207170ddb9ea6e436fa55c274`.

### 68.5. Kontrola zapisu i walidacja

Po zapisaniu zmian ponownie pobrano z GitHuba pliki bazy, repozytorium i testu. Potwierdzono pełne porównanie pól modelu darowizny, zgodność kryterium podglądu oraz obecność testu obejmującego dziewięć wariantów konfliktu.

Wynik poprzedniego przebiegu dla SHA `10f2f0f9677c3584a8887d1c2fe46fd22af6dd8f`:
- Repository compliance, run `43717150909` — **success**;
- Android prototype build, run `43717151111` — **success**;
- Android GUI validation, run `43717150912` — **failure** na kompilacji testów instrumentacyjnych. Przyczynę i poprawkę opisano w sekcji 68.3a; nie był to błąd wykonania testu na emulatorze.

Dla nowego HEAD `de7de2de13c3e4ee9b769c5183f3931cd3f27c3f`:
- Android GUI validation, run `43717744443` — **pending**;
- Repository compliance, run `43717744461` — **queued**;
- Android prototype build dla poprzedniego commitu `d9e90db57750b52e54a4d8381aad8f4674f34705`, run `43717735740` — **in_progress**; nie jest to jeszcze przebieg dla końcowego HEAD.

Położenie testów poprawiono i zweryfikowano ponownym odczytem plików, ale etap 68 pozostaje **otwarty walidacyjnie** do zakończenia testów JVM, emulatorowych i budowy APK dla aktualnego HEAD. PR `#68` pozostaje otwarty i niescalony z `main`.


---

## 68.3b. Ponowna walidacja emulatorowa i korekta testów — 15 listopada 2026 r.

Dla SHA `1f260607e130e2684b1343253c050332035ed834` workflow `Android GUI validation` (run `43717791760`) zakończył się niepowodzeniem po uruchomieniu 96 testów instrumentacyjnych: 3 testy nie przeszły. Testy JVM zakończyły się sukcesem. Odrębny workflow budowy Androida (run `43717791762`) oraz kontrola REUSE (run `43717791792`) zakończyły się sukcesem.

Ustalone przyczyny:
- `FinancialReportDateRangePickerTest`: dwa testy nie przewijały komórki dnia kalendarza do widocznego obszaru przed asercją i kliknięciem;
- `PaymentLedgerRepositoryTest.sameDonationIdWithChangedMetadataIsReportedAsConflictAndPreservesRecord`: porównanie całego obiektu uwzględniało skalę `BigDecimal`; SQLite zapisuje kwotę tekstowo po normalizacji, więc wartość `250` była równa liczbowo `2.5E+2`, ale nie była równa obiektowo.

Wprowadzone korekty:
- testy wyboru daty wykonują przewinięcie komórki dnia do widocznego obszaru przed kliknięciem;
- asercja zachowanego rekordu kanonizuje kwoty do postaci dziesiętnej bez wykładnika przed porównaniem obiektów; logika produkcyjna i walidacja konfliktów pozostały bez zmian.

**Status:** poprawki zapisane w gałęzi; wymagane ponowne potwierdzenie pełnym workflow emulatorowym.

## 69. Zgodność podglądu i zatwierdzania archiwum JSON

### 69.1. Ustalenia audytowe

Wcześniejszy podgląd archiwum porównywał transakcje i darowizny wyłącznie z rekordami już zapisanymi w bazie. Nie klasyfikował powtórzonych identyfikatorów występujących wewnątrz jednego importowanego archiwum. Zatwierdzanie odrzucało każdą powtórzoną tożsamość, nawet gdy powielone rekordy miały identyczną treść. Podgląd nie wykrywał również przed zatwierdzeniem sytuacji, w której dwa różne rekordy transakcji wskazywały ten sam zapis bankowy.

### 69.2. Wykonane zmiany

- Podgląd archiwum śledzi identyfikatory transakcji i darowizn zarówno względem bazy, jak i w obrębie importowanej paczki.
- Identyczne powtórzenia są raportowane jako duplikaty; zmieniona treść pod tym samym identyfikatorem jest raportowana jako konflikt.
- Zatwierdzanie archiwum deduplikuje identyczne powtórzenia transakcji i darowizn, a sprzeczne powtórzenia odrzuca.
- Podgląd sprawdza, czy powiązany zapis bankowy istnieje w bazie lub w importowanej paczce, czy kwota i waluta odpowiadają jednej z nóg transakcji albo prowizji oraz czy zapis nie jest przypisany do innej transakcji.
- Do wyniku podglądu dodano liczniki `duplicateTransactions`, `duplicateDonations` i `transactionLinkConflicts`; `hasConflicts` uwzględnia również konflikty powiązań.
- Import pozostaje transakcyjny w SQLite; ograniczenia kluczy obcych i unikalności nadal stanowią ostatnią ochronę przed wyścigiem lub niespójnością.

### 69.3. Testy regresyjne

Dodano testy instrumentacyjne obejmujące:
1. identyczne powtórzenia transakcji i darowizn — podgląd raportuje duplikaty, a import zapisuje po jednym rekordzie;
2. sprzeczne rekordy o tym samym identyfikatorze — podgląd zgłasza konflikty, import zostaje odrzucony bez zmiany bazy;
3. powiązanie jednego zapisu bankowego z dwiema różnymi transakcjami — podgląd wykrywa konflikt, a zatwierdzenie pozostawia bazę bez częściowych zapisów.

**Kryterium odbioru:** testy JVM, wszystkie testy instrumentacyjne, budowa APK oraz REUSE muszą zakończyć się sukcesem dla tego samego końcowego SHA. Samo zapisanie zmian nie oznacza zaliczenia etapu.

## 70. Walidacja kierunku nóg wymiany FX — korekta testu (15 listopada 2026 r.)

### 70.1. Poprawka logiki

W `BankImportDeduplicator.isOppositeFxLeg` dopuszczenie pary wymiany wymaga zgodności znaku kwoty z etykietą każdej nogi: UZNANIE dodatnie, OBCIĄŻENIE ujemne. Zachowano wymagania tej samej daty, różnych walut i rodzaju „WYMIANA W KANTORZE”.

### 70.2. Usterka wykryta w CI i jej usunięcie

Workflow Android GUI validation dla SHA `f47c86ebd657a6922b75dd85bd52097556fb1eb1`, run `43720078786`, zakończył testy JVM niepowodzeniem: 152 testy, 1 błąd. Log wskazał `BankImportDeduplicatorTest.oppositeFxLegsWithSignsContradictingOperationLabelsAreConflicts`, asercję w linii 244. Analiza wykazała, że jeden scenariusz testu zawierał prawidłową parę (+ UZNANIE, − OBCIĄŻENIE), a mimo to oczekiwał konfliktu. Błąd dotyczył testu, nie reguły produkcyjnej.

Poprawiono test tak, aby oddzielnie sprawdzał:
- nieprawidłowe dodatnie OBCIĄŻENIE zestawione z prawidłowym dodatnim UZNANIEM;
- nieprawidłowe ujemne UZNANIE zestawione z prawidłowym ujemnym OBCIĄŻENIEM.

Każdy scenariusz powinien klasyfikować pierwszy wpis jako nowy, a drugi jako konflikt. Commit korekty testu: `889db06c24e1fadedcfe7d21269c1130a2ebb5b2`.

### 70.3. Status walidacji

Ponowny odczyt pliku testowego potwierdził dwa poprawne scenariusze błędnych etykiet i znaków. Wyniki wcześniejszego CI nie są zaliczeniem poprawionej wersji. Oczekuje się na pełne CI dla końcowego SHA etapu 71.

## 71. Utwardzenie parsera XML wyciągu bankowego (15 listopada 2026 r.)

### 71.1. Ustalenie

Parser `BankStatementXmlParser` ograniczał rozmiar dokumentu do 10 MiB i wyłączał przetwarzanie DTD, ale główna pętla korzystała z `next()`, które pomija część tokenów XML, przez co nie zapewniało jawnej kontroli zdarzenia DTD. Nie było też limitu liczby operacji w jednym dokumencie.

### 71.2. Wdrożone zabezpieczenia

- Pętla parsowania korzysta z `nextToken()`, aby kontrolować tokeny deklaracji dokumentu.
- Zdarzenia `DOCDECL` i `ENTITY_REF` są jawnie odrzucane; parser nie akceptuje deklaracji DTD ani odwołań do encji XML.
- Zachowano wyłączenie przetwarzania deklaracji dokumentu przez `FEATURE_PROCESS_DOCDECL=false`.
- Dodano limit 50 000 operacji w jednym pliku, obok dotychczasowego limitu 10 MiB.
- Parser nadal zapisuje wyłącznie pseudonim rachunku, datę, walutę, kwotę, typ i bezpieczną referencję/odcisk opisu; nie zachowuje numeru rachunku, nazwiska ani adresu kontrahenta.

### 71.3. Testy

W testach instrumentacyjnych parsera dodano przypadki:
- odrzucenia dokumentu z DTD i zewnętrzną encją wskazującą lokalny plik;
- odrzucenia kwoty zerowej;
- odrzucenia nieprawidłowego kodu waluty.

Istniejące testy poprawnego importu, ochrony danych kontrahenta, konwersji JSON i podglądu deduplikacji pozostają zachowane.

### 71.4. Zmienione pliki i commity

- `BankStatementXmlParser.kt` — odrzucanie DTD/encji i limit operacji; commit `1bca7987a81fd5bcc3559eaa71edadc1c608b5d6`.
- `BankStatementXmlParser.kt` — użycie `nextToken()` do kontroli DTD; commit `2fe27c19523502c768c5e1d2347eca68ed47a73b`.
- `BankStatementXmlParserTest.kt` — testy DTD, kwoty zerowej i kodu waluty; commit `1030e3490e36ac546594b4125e82f2eebdcdefc3`.
- `BankImportDeduplicatorTest.kt` — korekta scenariuszy testu kierunku FX; commit `889db06c24e1fadedcfe7d21269c1130a2ebb5b2`.

### 71.5. Kontrola źródeł i CI

Ponowny odczyt z gałęzi potwierdził obecność:
- `DOCDECL` i `ENTITY_REF` w ścieżce odrzucania;
- `nextToken()`;
- limitu `MAX_OPERATIONS = 50_000`;
- testu DTD oraz testów nieprawidłowych danych;
- poprawionych scenariuszy testu FX.

Końcowy SHA po zmianach: `2fe27c19523502c768c5e1d2347eca68ed47a73b`.

Dla tego SHA:
- Android prototype build, run `43720318876` — **in_progress** przy ostatnim odczycie;
- Android GUI validation, run `43720318853` — **pending**;
- Repository compliance, run `43720318861` — **queued**.

Etap 71 jest **zapisany i sprawdzony w źródłach; walidacja CI pozostaje otwarta**. Nie deklaruje się jeszcze sukcesu testów JVM, instrumentacyjnych, budowy APK ani publikacji artefaktu.


## 72. Korekta testów wykrytych w CI — 15 listopada 2026 r.

### 72.1. Wynik weryfikacji SHA 563a88c2

Dla HEAD `563a88c2f37a9153466ea7446e2e4bbcaa40fd18`:
- Repository compliance, run `43720380012` — **success**;
- Android prototype build, run `43720379977` — **success**;
- Android GUI validation, run `43720380022` — **failure**.

Testy JVM zakończyły się sukcesem. Emulator wykonał 101 testów instrumentacyjnych; 3 nie przeszły:
- `FinancialReportDateRangePickerTest.selectingDifferentStartDateUpdatesOnlyStartBoundary` — test szukał dnia jako zwykłego tekstu i wywoływał `performScrollTo()`, ale element dnia kalendarza Material 3 jest dostępny przez opis semantyczny daty;
- `FinancialReportDateRangePickerTest.selectingDifferentEndDateUpdatesOnlyEndBoundary` — analogiczny problem selektora;
- `PaymentLedgerRepositoryTest.archivePreviewAndImportDeduplicateIdenticalRepeatedTransactionAndDonationIds` — niezgodna oczekiwana liczba duplikatów w podsumowaniu importu.

### 72.2. Wykonane korekty

- Test wyboru daty początkowej używa teraz semantycznego opisu dostępności `November 10, 2026` zamiast wyszukiwania tekstu `10` i przewijania nieprzewijalnego węzła.
- Test wyboru daty końcowej używa opisu `November 25, 2026`.
- W teście importu archiwum skorygowano oczekiwanie podsumowania z `BatchImportSummary(1, 0)` na `BatchImportSummary(1, 1)`: w paczce występuje jedna nowa darowizna i jedno powtórzenie tej samej darowizny. Test nadal osobno potwierdza, że po deduplikacji zapisano po jednej transakcji i darowiźnie.
- Nie zmieniano logiki produkcyjnej rejestru ani komponentu kalendarza.

### 72.3. Commity i ponowna walidacja

- Test kalendarza: `59322664c901457916c961256d3f11a7bbcca8cb`.
- Test podsumowania importu: `a1140471192b7273839a1dad03d6c9793f16f858`.
- Dla końcowego SHA `a1140471192b7273839a1dad03d6c9793f16f858` uruchomiono ponownie:
  - Android GUI validation, run `43721522828` — **in_progress** przy aktualizacji;
  - Android prototype build, run `43721522796` — **in_progress**;
  - Repository compliance, run `43721522885` — **in_progress**.

**Status etapu 72:** poprawki testów zapisane; ich skuteczność pozostaje do potwierdzenia zakończonym CI dla SHA `a1140471192b7273839a1dad03d6c9793f16f858`. Nie uznaje się jeszcze testów emulatorowych, kompilacji ani APK za zaliczone.


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


## 74. Korekta kompilacji testów kalendarza — 15 listopada 2026 r.

### 74.1. Wynik kontroli CI

Dla SHA `8eae56297af6ddaf2bd58cb0481513e6b97072c0`:
- Android prototype build, run `43728461800` — **success**;
- Repository compliance, run `43728461822` — **success**;
- Android GUI validation, run `43728461908` — **failure**.

Testy JVM zakończyły się sukcesem, lecz zadanie `:app:compileDebugAndroidTestKotlin` nie przeszło kompilacji. Przyczyną był nieprawidłowy import `androidx.compose.ui.test.onNode` w `FinancialReportDateRangePickerTest.kt` (`Unresolved reference: onNode`). Testy kalendarza nie zostały uruchomione, dlatego poprzednie selektory dat nie mają jeszcze potwierdzenia emulatorowego. Nie opublikowano APK z tego przebiegu: kroki budowania i publikacji APK zostały pominięte po błędzie testów.

### 74.2. Wykonana korekta

Usunięto nieprawidłowy import. Wywołania `composeRule.onNode(...)` pozostają dostępne przez interfejs reguły Compose UI Test; matcher `hasContentDescription(..., substring = true)` pozostaje bez zmian.

- Plik: `obywatel-android/app/src/androidTest/java/pl/gov/obywatel/android/financial/FinancialReportDateRangePickerTest.kt`.
- Commit korekty: `42d466b48619f9d798e12f285664cfbb6a2faa7a`.

### 74.3. Status i kryterium zamknięcia

Korekta źródła została zapisana. Oczekuje na nowy pełny przebieg CI dla SHA `42d466b48619f9d798e12f285664cfbb6a2faa7a`. Etap pozostaje **otwarty** do potwierdzenia kompilacji testów instrumentacyjnych, wykonania wszystkich testów emulatorowych, budowy APK, archiwizacji sumy kontrolnej i publikacji artefaktu. Etap 74 dotyczy wyłącznie usunięcia błędu kompilacji testów; nie oznacza jeszcze zaliczenia funkcjonalnych testów kalendarza.

Etap XLS/XLSX pozostaje zablokowany do czasu pełnego zielonego CI etapu CSV i testów Androida.

## 75. Adapter CSV dla rzeczywistego układu eksportu iPKO — 15 listopada 2026 r.

### 75.1. Wynik kontroli wcześniejszego CI

Dla SHA `a45dd7cdb7c4c076269829aac41403b9898a30b4`:
- Android prototype build, run `43729630235` — **success**;
- Repository compliance, run `43729630224` — **success**;
- Android GUI validation, run `43729630242` — **failure**.

Raport instrumentacyjny został pobrany i odczytany. Wykonano 101 testów: 99 zakończyło się sukcesem, a dwa testy `FinancialReportDateRangePickerTest` nie przeszły na asercji `assertIsDisplayed()` dla komórek dni kalendarza (linie 83 i 101). Kompilacja testów, testy JVM i uruchomienie emulatora zakończyły się poprawnie. Budowa i publikacja APK zostały pominięte z powodu niezaliczonych testów GUI.

### 75.2. Analiza dostarczonych próbek iPKO

Przeanalizowano bez zapisywania danych identyfikujących do repozytorium:
- XML `account-history`: parser rozpoznaje datę wykonania, typ, walutę, kwotę i referencję z pola opisu;
- CSV: kodowanie Windows-1250, separator przecinkowy, nagłówki `Data operacji`, `Data waluty`, `Typ transakcji`, `Kwota`, `Waluta`, `Saldo po transakcji`, `Opis transakcji`, a także dodatkowe puste nagłówki; opis jest rozłożony na kilka kolejnych pól;
- XLS: plik jest binarnym skoroszytem OLE/BIFF, a nie plikiem XLSX. Wymaga odrębnego parsera HSSF/BIFF; nie wolno kierować go do parsera OOXML ani CSV.

Pliki źródłowe i hasło do archiwum nie są dodawane do repozytorium. Testy korzystają z syntetycznych danych odtwarzających układ kolumn, bez prawdziwych numerów rachunków, nazwisk ani adresów.

### 75.3. Wykonane poprawki CSV

W `BankStatementCsvParser.kt`:
- puste nazwy kolumn są mapowane na techniczne, lokalne identyfikatory kolumn, zamiast powodować natychmiastowe odrzucenie pliku;
- dodano aliasy `typtransakcji` i `opistransakcji`;
- dodatkowe kolumny o pustych nagłówkach następujące po opisie są łączone wyłącznie na potrzeby ekstrakcji metadanych;
- z opisu wyodrębniana jest referencja `Referencje własne zleceniodawcy`;
- jawna treść opisu nie jest zapisywana do wpisu, gdy referencja została rozpoznana; bez referencji pozostaje dotychczasowy skrót SHA-256.

Dodano test kontraktowy `parsesIpkoCsvWithTrailingUnnamedDescriptionColumnsWithoutPersistingPersonalData` z syntetycznym rekordem. Test weryfikuje datę, walutę, kwotę ze znakiem, typ, referencję i brak jawnego opisu.

### 75.4. Korekta testów kalendarza

W dwóch testach wyboru dnia usunięto asercję `assertIsDisplayed()` na komórce kalendarza. Raport wskazywał, że dopasowany węzeł semantyczny nie był raportowany jako wyświetlony; test wykonuje teraz bezpośrednio akcję `performClick()` na dopasowanym węźle i nadal sprawdza końcowe wartości obu granic okresu. Jest to korekta testu, nie zmiana logiki produkcyjnego kalendarza.

Commity zmian:
- adapter CSV: `45277d72d878fb467c4c98f7d7843bb95c71a914`;
- test CSV iPKO: `b099b7185001fd0f7d5f53ea3b300c35e2c4f1ff`;
- testy kalendarza: `34797f67398e9727168b78d0b832aae4df565ed5`.

### 75.5. Status i kryteria zamknięcia

Źródła i testy zostały zapisane na gałęzi. Poprawność działania nowych zmian wymaga pełnego CI na końcowym SHA po aktualizacji audytów. Nie uznaje się jeszcze adaptera CSV ani testów kalendarza za zaliczone.

Obsługa XLS pozostaje **otwarta**: potrzebny jest wybór i ocena bezpiecznej biblioteki HSSF/BIFF dla Androida albo wąskiego parsera BIFF8, wraz z testami limitów OLE/BIFF i ekstrakcji bez przechowywania danych osobowych. Nie dodano ciężkiej zależności Apache POI bez oceny wpływu na rozmiar APK i zgodność Androida.


## 76. Diagnoza testów daty i selektory niezależne od języka — 15 listopada 2026 r.

### 76.1. Wynik rzeczywistego przebiegu emulatorowego

Dla SHA `6ec5d9609a2f69e61df23f21a21bcaaa42c535ee`:
- Android prototype build, run `43731287729` — **success**;
- Repository compliance, run `43731287676` — **success**;
- Android GUI validation, run `43731287681` — **failure**.

Pobrany raport instrumentacyjny potwierdził 101 testów, z czego 99 zaliczono, a 2 nie przeszły. Nie był to już problem `assertIsDisplayed()`: testy nie znajdowały węzłów z opisami dostępności w języku angielskim `November 10, 2026` i `November 25, 2026`. Błąd występował przy `performClick()` i brzmiał `Failed to inject touch input`, ponieważ selektor nie dopasował żadnego węzła. Trzy pozostałe testy kalendarza przechodziły.

Wniosek: poprzednia korekta usunęła problem asercji widoczności, ale pozostawiła zależność od lokalizowanego tekstu dostępności. To nadal wada testu, a nie dowód usterki produkcyjnego kalendarza.

### 76.2. Wykonana korekta

W `FinancialReportDateRangePickerTest.kt`:
- usunięto selektory oparte na angielskich opisach dostępności;
- wybór dni 10 i 25 oparto na widocznym tekście liczbowym dnia w niepołączonym drzewie semantyki (`useUnmergedTree = true`);
- zachowano asercje końcowego zakresu dat, więc test nadal weryfikuje, że zmienia się tylko wskazana granica;
- usunięto nieużywane importy.

Commit: `2762cf82543334797475fc15de6f03244f6d77e5`.

### 76.3. Weryfikacja i status

Źródłem diagnozy jest raport HTML/XML z artefaktu `android-instrumentation-reports` przebiegu `43731287681`. Na poprzednim SHA build i compliance przeszły, lecz emulator zakończył się błędem, a kroki budowania i publikacji APK zostały pominięte.

**Status etapu 76:** poprawka selektorów zapisana; oczekuje na pełny CI dla końcowego SHA po aktualizacji niniejszego audytu. Nie oznaczać kalendarza ani APK jako zaliczonych do czasu przejścia wszystkich 101 testów emulatorowych i końcowych kroków workflow.

Etap XLS/BIFF pozostaje wstrzymany do czasu zielonego GUI validation, build i compliance na tym samym SHA. PR #68 pozostaje otwarty i niescalony.


## 77. Ponowna diagnoza emulatora i ocena ochrony danych CSV — 15 listopada 2026 r.

### 77.1. Wynik CI dla SHA 5603e2f

Zweryfikowano zakończone przebiegi dla tego samego SHA `5603e2f40fe0645fc4daac07d1f61119f15da4a2`:
- Android prototype build, run `43732131848` — **success**; zadania JVM i budowa APK debug zakończone powodzeniem; artefakt `obywatel-android-debug` (ID `14169042276`, digest SHA-256 `e41723c1ccc9ab49e65b58ec34c2eea32ef53d9727195751e22f3d87c4a77ba2`) został opublikowany;
- Repository compliance, run `43732131873` — **success**;
- Android GUI validation, run `43732131926` — **failure**.

Raport instrumentacyjny (artefakt `android-instrumentation-reports`, ID `14169097632`) potwierdził 101 testów, 99 zaliczonych i 2 niezaliczone. Oba błędy dotyczą:
- `selectingDifferentStartDateUpdatesOnlyStartBoundary` — selektor tekstu dnia `10` nie znajduje węzła;
- `selectingDifferentEndDateUpdatesOnlyEndBoundary` — selektor tekstu dnia `25` nie znajduje węzła.

Błąd występuje w `performClick()`: `Failed to inject touch input`, ponieważ matcher nie znajduje dokładnie jednego węzła. Zmiana na `useUnmergedTree = true` nie rozwiązała problemu. Wcześniejszy wniosek, że selektor liczbowy naprawia testy, jest obalony przez raport z tego przebiegu. To nadal nie dowodzi błędu logiki produkcyjnego kalendarza; dowodzi, że test nie umie wskazać komórki daty w semantyce Material3 DatePicker.

Kroki budowy i publikacji APK w GUI workflow zostały pominięte po awarii testów. Niezależny workflow zbudował debug APK, ale nie jest to równoważne pełnej walidacji GUI ani podpisanemu wydaniu. PR #68 pozostaje otwarty i niescalony.

### 77.2. Wniosek techniczny dla testów kalendarza

Nie należy dalej zmieniać selektora na kolejne warianty tekstu bez zbadania drzewa semantyki. Następna poprawka powinna:
1. ustalić semantykę Material3 DatePicker w używanej wersji biblioteki i konfiguracji emulatora;
2. zastosować stabilny identyfikator testowy/semantykę lub kontrolowany mechanizm testowy stanu wyboru daty, zamiast zależności od lokalizowanego opisu albo samego tekstu dnia;
3. zachować testy zachowania: anulowanie nie zmienia dat, zatwierdzenie zmienia wyłącznie właściwą granicę, zakres odwrócony wyświetla walidację;
4. uruchomić testy instrumentacyjne i odczytać XML/HTML, wymagając 101/101 zaliczonych testów (albo jawnie zaktualizować oczekiwaną liczbę po zmianach zestawu).

### 77.3. Ustalenie bezpieczeństwa CSV — skrót SHA-256 opisu

Ponowny przegląd `BankStatementCsvParser.kt` potwierdził, że przy braku referencji parser zapisuje `CSV-OP-` + SHA-256 z wartości `scope|combinedDescription`. Surowy opis nie trafia do pola `description`, ale deterministyczny skrót danych przewidywalnych lub częściowo znanych nie stanowi anonimizacji. Możliwy jest atak słownikowy, korelacja tych samych opisów i ujawnienie informacji przez porównanie skrótów. Identyfikator zakresu rachunku nie jest tajnym kluczem kryptograficznym.

W konsekwencji obecny zapis należy traktować jako pseudonimizację, a nie anonimizację. Nie wolno opisywać go jako pełnej ochrony danych osobowych. Następny etap musi sprawdzić `BankStatementEntry.accountingEntryId()`, klucze deduplikacji, migracje i archiwum JSON. Docelowo identyfikator techniczny powinien opierać się na danych strukturalnych niebędących opisem osobowym (np. rachunek-scope, data, waluta, kwota, typ i stabilna referencja bankowa, jeśli istnieje), z jawną obsługą kolizji i konfliktów; bez referencji nie należy tworzyć trwałego identyfikatora z treści opisu. Nie można wdrożyć takiej zmiany bez sprawdzenia kompatybilności istniejących archiwów i deduplikacji.

### 77.4. Zakres wykonany i status

- pobrano i przeanalizowano raport XML/HTML z runu `43732131926`;
- potwierdzono dokładnie dwa niezaliczone testy i przyczynę braku dopasowania;
- potwierdzono osobny sukces budowy debug APK i artefakt;
- potwierdzono sukces compliance dla tego SHA;
- przeprowadzono przegląd kodu parsera CSV i udokumentowano ryzyko deterministycznego skrótu opisu.

**Status etapu 76:** niezaliczony — poprawka selektorów nie zadziałała.  
**Status etapu 77:** diagnoza i aktualizacja audytu wykonane; poprawka testu kalendarza i bezpieczna zmiana identyfikacji CSV pozostają otwarte.  
**Etapy 78–81 oraz XLS/BIFF:** wstrzymane do czasu usunięcia błędów testów GUI i przeglądu wpływu zmiany identyfikatora na dane już zapisane.


## 78. Ponowna walidacja GUI i diagnostyka drzewa semantyki DatePicker — 15 listopada 2026 r.

### 78.1. Wyniki dla SHA `ad533d4a1a2d0c9df967f9788750c6628918248d`

Zakończone przebiegi:
- Android prototype build, run `43733058556` — **success**;
- Repository compliance, run `43733058562` — **success**;
- Android GUI validation, run `43733058566` — **failure**.

Artefakt raportu instrumentacyjnego: `android-instrumentation-reports`, ID `14169510439`. Raport HTML klasy `FinancialReportDateRangePickerTest` potwierdza 5 testów: 3 zaliczone, 2 niezaliczone. Nadal zawodzą wyłącznie:
- `selectingDifferentStartDateUpdatesOnlyStartBoundary` — nie znaleziono węzła tekstowego `10`;
- `selectingDifferentEndDateUpdatesOnlyEndBoundary` — nie znaleziono węzła tekstowego `25`.

Pozostałe testy kalendarza: otwarcie i zatwierdzenie bez zmiany daty, anulowanie oraz walidacja odwróconego zakresu — **success**. Pełny zestaw GUI pozostaje niezaliczony. Oddzielny build debug APK i compliance zakończyły się sukcesem, lecz nie zastępują zielonego GUI validation.

### 78.2. Wykonana praca diagnostyczna

Raport HTML i logi emulatora zostały pobrane i przeanalizowane. Potwierdzono, że awaria następuje na etapie dopasowania semantycznego dnia, przed wykonaniem kliknięcia. Nie ma dowodu na błąd logiki aktualizacji dat w komponencie produkcyjnym.

W commicie `2db3a8f5a0890bf5eda7d5021428a6fc84ac031c` dodano do obu niezaliczonych testów zapis pełnego, niepołączonego drzewa semantyki Compose po otwarciu DatePicker:
- `START_DATE_PICKER_SEMANTICS`;
- `END_DATE_PICKER_SEMANTICS`.

Celem jest ustalenie rzeczywistych etykiet, opisów dostępności, identyfikatorów i granic węzłów w używanej wersji Material3, zamiast kolejnego zgadywania selektora. Jest to poprawka diagnostyczna testów, nie zmiana zachowania produkcyjnego kalendarza.

### 78.3. Weryfikacja ochrony danych i identyfikacji CSV

Ponowny odczyt `BankStatementCsvParser.kt`, `BankImportDeduplicator.kt` i `PaymentLedgerJsonCodec.kt` potwierdził:
- przy braku referencji parser umieszcza deterministyczny skrót opisu w polu `description`;
- `BankStatementEntry.accountingEntryId()` wykorzystuje opis w tożsamości wpisu bez referencji;
- identyfikatory powiązań transakcji są eksportowane i importowane w archiwum JSON.

Zmiana tej strategii bez migracji może zmienić identyfikatory istniejących wpisów i zerwać powiązania transakcji. Ponadto zastąpienie opisu pustą wartością i użycie wyłącznie pól strukturalnych grozi scaleniem dwóch odrębnych, lecz identycznych kwotowo i datowo operacji. Dlatego nie wprowadzono pozornej poprawki polegającej na usunięciu skrótu bez zaprojektowania stabilnego identyfikatora, migracji i obsługi kolizji.

### 78.4. Status i następne kryteria

- Etap 76 — **niezaliczony**; dwa testy wyboru dnia nadal nie przechodzą.
- Etap 77 — diagnoza ryzyka pseudonimizacji potwierdzona; bezpieczna zmiana modelu identyfikacji pozostaje otwarta.
- Etap 78 — wykonano instrumentację semantyki testów; oczekuje na raport z kolejnego emulatorowego przebiegu.
- Etapy formularzy, NBP, wyceny i raportowania oraz XLS/BIFF pozostają wstrzymane do czasu stabilizacji importu i zielonego GUI.

PR #68 pozostaje otwarty i niescalony. Nie oznaczać kalendarza jako naprawionego ani APK jako zweryfikowanego końcowym workflow, dopóki nowy przebieg GUI nie zakończy się sukcesem.


## 79. Wynik GUI CI i korekta diagnostyki DatePicker — 15 listopada 2026 r.

### 79.1. Wynik przebiegu dla SHA `169bb3af859e61ce19b2051a7b99bd9c61907c47`

- Android GUI validation, run [43734169856](https://github.com/bartoszruta26-droid/mObywatel-2.0/actions/runs/43734169856) — **failure**.
- Repository compliance, run `43734169836` — **success**.
- Android prototype build, run `43734169823` — **success**.
- Zadanie Android GUI: JVM unit tests — **success**; Compose UI tests na emulatorze — **failure**.
- Raport instrumentacyjny: artefakt `14169757103`, SHA-256 archiwum `03aa14e3c7da7679b4756347ee3bc0c331ba8c416fc0f149cd44d8815979d383`.

Raport XML wykazał, że dwa testy wyboru daty zakończyły się błędem w diagnostycznym wywołaniu `onRoot(useUnmergedTree = true).printToLog(...)`. Matcher `isRoot` zwrócił dwa korzenie (węzeł okna dialogowego i korzeń głównej hierarchii), podczas gdy `printToLog` wymaga dokładnie jednego węzła. Błąd wystąpił przed selektorem dnia i przed kliknięciem. W tym przebiegu **nie uzyskano zatem nowego dowodu**, czy selektory dni `10` i `25` działają.

### 79.2. Wykonana korekta

W commicie `89fd9c1ca0f7c3ee88cdcfa518d0817c84060d24` usunięto wadliwą diagnostykę `printToLog` oraz nieużywane importy `onRoot` i `printToLog` z `FinancialReportDateRangePickerTest.kt`. Nie zmieniano produkcyjnego komponentu DatePicker ani logiki aktualizacji granic dat. Testy wyboru dni nadal sprawdzają końcowy zakres dat po kliknięciu i zatwierdzeniu.

### 79.3. Status i kryteria

Korekta usuwa przyczynę bieżącego błędu diagnostycznego, ale nie jest jeszcze potwierdzoną naprawą testów wyboru dni. Wymagany jest nowy pełny CI dla SHA po tej korekcie: kompilacja testów instrumentacyjnych, wszystkie testy emulatorowe, compliance i build. W GUI workflow kroki budowania oraz publikacji APK zostały pominięte po błędzie testów; brak nowego APK z tego przebiegu.

**Status:** diagnostyka z etapu 78 skorygowana; testy wyboru dat nadal otwarte; brak podstaw do zamknięcia etapu kalendarza. Nie zmieniono modelu identyfikacji CSV — ryzyko skrótu SHA-256 opisu, migracja i obsługa kolizji pozostają otwarte. Etapy NBP, wyceny, raportowania i XLS/BIFF nie zostały rozpoczęte.

PR #68 pozostaje otwarty i niescalony.


## 19. Etap 80 — fundament przelicznika walut i walidacja nóg FX (15 listopada 2026 r.)

### Zakres wykonany
- Dodano `ExchangeRate` z jednoznaczną konwencją: `quotePerBase` oznacza liczbę jednostek waluty kwotowanej za 1 jednostkę waluty bazowej (np. EUR/PLN = 4,30).
- Dodano `ExchangeRateCalculator` obsługujący przeliczenie bezpośrednie i odwrotne, precyzję waluty docelowej oraz jawny `RoundingMode`. Obliczenia pozostają w `BigDecimal`; brak kursu dla innej pary, kurs zerowy, waluta identyczna i kwota ujemna są odrzucane.
- Dodano testy jednostkowe dla obu kierunków, zaokrągleń i błędnych danych.
- Wzmocniono `BankExchangeOperationProjector`: para wymiany jest kompletna tylko wtedy, gdy „UZNANIE” ma dodatnią kwotę, a „OBCIĄŻENIE” ujemną, niezależnie od kolejności nóg. Dodano test regresyjny obu sprzecznych wariantów.

### Ograniczenia
- Jest to silnik obliczeniowy dla jawnie przekazanego kursu, a nie jeszcze pełny ekran kalkulatora ani źródło notowań. Adapter NBP, wybór walut i kursu, cache, daty publikacji i oznaczanie wyceny referencyjnej pozostają kolejnym etapem.
- Nie należy przedstawiać kursu referencyjnego jako kursu wykonania transakcji bankowej.
- Import XLS/BIFF oraz usunięcie ryzyka pseudonimizacji opisów CSV nadal pozostają otwarte.

### Weryfikacja
- Zmiany zapisano w gałęzi `feature/payment-ledger-stage-1` w commitach: `3108a87db2421d64b372f6b4e44e5b6ef0047419` (kalkulator), `12f8dffa38785cfdd66f0d8f8d3b6cb2834f3fbf` (testy kalkulatora), `3a48c401987cf5aa4495257d7596486eeb382bed` (walidacja nóg), `670636e2d29123a05007e365b56eb3a6efe5b44e` (test regresyjny).
- Ponownie pobrano zmienione pliki z GitHub i sprawdzono obecność implementacji oraz testów.
- Ostatni znany przebieg GUI CI dla poprzedniego SHA `6a7411f9566915c9296b0270a60c9fcab0c18d1b` zakończył się niepowodzeniem dwóch testów interakcji DatePicker (`Failed to inject touch input`); testy JVM przeszły, a budowa debug APK i compliance w osobnych zadaniach zakończyły się sukcesem. Nie jest to wynik dla nowych commitów.
- Do zamknięcia etapu wymagane jest pełne CI dla końcowego SHA, w tym ponowne wykonanie testów emulatorowych. Nie deklarować APK ani pełnej regresji jako zaliczonych przed tym wynikiem.

### Następny etap
1. Uzyskać zielone GUI CI i naprawić testy DatePicker bez usuwania asercji sprawdzających zmianę granic okresu.
2. Dodać adapter NBP dla tabel A/B/C i cen złota, z cache, datą efektywną, obsługą dni bez publikacji i zakresów historycznych.
3. Zintegrować kalkulator z interfejsem i etykietami źródła kursu, daty oraz rodzaju wyceny.


## 20. Etap 81–82 — stabilizacja testów dat i adapter oficjalnego API NBP (15 listopada 2026 r.)

### Etap 81 — poprawa testów wyboru dat
- Dwa testy instrumentacyjne kalendarza raportu kończyły się błędem `Failed to inject touch input` przy wskazywaniu dnia miesiąca, mimo że pozostałe testy przebiegały.
- Zmieniono wyłącznie sposób wywołania akcji wyboru dnia: test korzysta z akcji semantycznej Compose `SemanticsActions.OnClick`, zamiast emulować fizyczne dotknięcie ekranu. Asercje zachowują kontrolę, że zmienia się wyłącznie wskazana granica okresu.
- Zmiana wymaga potwierdzenia w emulatorowym CI; nie uznawać jej za zaliczoną przed zielonym przebiegiem.

### Etap 82 — adapter NBP
- Dodano `NbpRatesRepository.kt` z transportem HTTP opartym na `HttpURLConnection`, limitami czasu połączenia/odczytu, nagłówkiem JSON i obsługą błędów HTTP.
- Obsługiwane są tabele A, B i C, odczyt tabeli bieżącej, tabeli z datą, zakresu historycznego oraz ceny złota za gram.
- Model zachowuje numer tabeli, datę efektywną i wartości dziesiętne. Tabela C zachowuje osobno `bid` i `ask`; nie tworzy sztucznego kursu średniego. Konwersja przez `toExchangeRate()` jest dostępna tylko, gdy źródło dostarcza `mid` (A/B).
- Dodano cache procesowy: 15 minut dla najnowszych notowań i 30 dni dla zapytań historycznych; udostępniono jawne czyszczenie cache.
- Dodano obsługę dni bez publikacji przez wyszukanie ostatniej opublikowanej tabeli/ceny w zakresie do 7 dni wstecz (parametr można zmienić). Zakresy API są walidowane do maksymalnie 93 dni kalendarzowych.
- Dodano testy parsera tabeli A i C, ceny złota, konwersji kursu średniego, cache, limitów zakresu i dat weekendowych.

### Zakres nadal otwarty
- Adapter NBP jest warstwą danych; nie został jeszcze podłączony do ekranu kalkulatora, selektorów walut, historii przeliczeń ani trwałego cache.
- Nie dodano jeszcze pobierania pełnej listy walut do wyboru, automatycznego odświeżania w UI ani prezentacji daty efektywnej i rodzaju kursu w ekranie.
- Testy jednostkowe i emulatorowe dla nowych zmian oczekują na wynik CI dla końcowego SHA.

### Weryfikacja i commity
- `1187ebe17034dd974879cf91292e26d853ba5a48` — pierwsza implementacja adaptera NBP.
- `16869a351b2de2fe8005e8e1d68fbd6b18d91e2c` — prawidłowa konwencja kursu NBP: waluta obca → PLN.
- `48a344f9a907cb0c30b4725f902e7d6873e1b8fb` — testy parsera i cache.
- `b7f3286d58c3ee288d84c351f853d8a0254bb2e2` — semantyczna akcja wyboru dnia w testach DatePicker.
- `47a4ba1c15a985b2832381b201ff93b6adbaa961`, `56100c390f0abe40ca3f2e0a696634a7aa7be346`, `a7ae350f7963bfc6e07bf8d5a3c76c2a49e39aac` — obsługa dat bez publikacji, limity kalendarzowe i testy weekendów.
- Ostatni znany CI dla poprzedniego SHA miał sukces budowania debug APK i kontroli REUSE, ale niepowodzenie 2 z 101 testów instrumentacyjnych w DatePicker. Wynik dla nowego SHA musi zostać odczytany oddzielnie.

### Następny etap
1. Uzyskać zielony CI dla końcowego SHA i usunąć ewentualne błędy kompilacji/testów.
2. Zintegrować adapter z kalkulatorem i ekranem Compose: wybór waluty, kwota, odwrócenie pary, kurs, źródło, tabela i data efektywna.
3. Dodać listę walut z NBP, obsługę braku kursu dla waluty źródłowej/docelowej i czytelne komunikaty offline.
4. Dopiero potem rozszerzyć trwały cache i historię wycen.


## 21. Etap 83 — integracja kalkulatora NBP z interfejsem (15 listopada 2026 r.)

### Zakres wykonany
- Dodano `NbpCurrencyConverterCard.kt` i podłączono go w istniejącym panelu „Notowania i wycena” ekranu rejestru płatniczego.
- Interfejs pozwala wpisać kwotę, kody ISO waluty źródłowej i docelowej, zamienić kierunek oraz wybrać waluty z listy pobranej z NBP.
- Pobieranie tabel A i B odbywa się w `Dispatchers.IO`; tabela B jest opcjonalna, aby awaria jednego źródła nie blokowała kursów z drugiego.
- Przeliczenie walut obcych odbywa się przez PLN na podstawie kursów średnich NBP wyrażonych jako PLN za 1 jednostkę waluty. Wynik jest zaokrąglany do precyzji waluty docelowej z użyciem `BigDecimal` i `HALF_EVEN`.
- Dodano obsługę typowych walut o 0, 2, 3 i 4 miejscach dziesiętnych (w tym JPY, KWD, CLF/UYW); pozostałe kody używają 2 miejsc. Weryfikacja pełnego słownika ISO 4217 i wyjątków wymaga dalszego uzupełnienia.
- Interfejs pokazuje źródło NBP, osobną datę efektywną każdej pobranej tabeli i ostrzeżenie, że kurs średni ma charakter referencyjny, a nie transakcyjny.
- Po błędzie odświeżania usuwa poprzednie kursy z aktywnego stanu, aby nie prezentować nieaktualnej wyceny jako bieżącej.
- Wyodrębniono `NbpCurrencyConverter.convert` i dodano testy przeliczeń bezpośrednich, odwrotnych, krzyżowych, precyzji i błędnych danych.

### Ograniczenia i ryzyka
- Nie dodano jeszcze wyboru daty historycznej w interfejsie, automatycznego odświeżania, trwałego cache ani zapisu historii przeliczeń.
- Kody walut są wpisywane lub wybierane z listy; brakuje wyszukiwania po nazwie i pełnej lokalizacji nazw walut.
- Adapter i kalkulator wymagają zielonego CI oraz testów na urządzeniu. Sam zapis plików w GitHub nie jest dowodem poprawności kompilacji.

### Commity etapu 83
- `1c283e6cc3f08c84283769fc9eb55a1608365284` — karta przelicznika.
- `79fc28f4b22b1a3075d3bd9a14e451cfbafb16bb` — poprawa stanu i importów.
- `6e6875776616e5644db90558d745f4a015da2df8` — integracja z ekranem rejestru.
- `f341121b6e8b8ee0428b638ac530c501421d07fc` — wyodrębnienie obliczeń do testowalnego modelu.
- `c31458233be669f3d9efeab14aeba614fc36d619`, `820940857524f4ece2717bebd49e31e5b1012068` — testy i korekta oczekiwanych wyników.
- `7a06fbde78873ecb95ab62212906b3f375398c15` — osobne daty tabel i wybór waluty docelowej.
- `6689f3dfac64e546b42ec2469278ca6836f24db3` — odporne parsowanie numerycznej ceny złota.
- `f38fc56742db09048c7266df2a29f051a51e4e73` — czyszczenie kursów po nieudanym odświeżeniu.

### Następny etap
1. Potwierdzić kompilację, testy jednostkowe i emulatorowe na końcowym SHA.
2. Dodać testy UI kalkulatora oraz testy sieciowe z atrapą transportu, w tym timeout, HTTP 404/500, pustą odpowiedź i niedostępną tabelę B.
3. Dodać wybór daty historycznej z użyciem `tableOnOrBefore` i `goldPriceOnOrBefore` oraz jawne pokazanie faktycznej daty publikacji.
4. Uzupełnić słownik precyzji ISO 4217 i dodać lokalizowane nazwy walut.
5. Dodać trwały cache z oznaczeniem czasu pobrania i historii przeliczeń.


## 22. Etap 84 — wybór daty historycznej i notowanie złota (15 listopada 2026 r.)

### Zakres wykonany
- Rozszerzono kartę przelicznika o pole daty `RRRR-MM-DD` i osobną akcję pobrania notowań dla wskazanego dnia.
- Dla dat historycznych aplikacja pobiera ostatnią opublikowaną tabelę A i opcjonalnie B nie późniejszą niż wybrana data, korzystając z `tableOnOrBefore` i zakresu publikacji do 7 dni wstecz.
- Dodano pobieranie ceny złota z ostatniego dostępnego dnia publikacji do wybranej daty; cena jest wyświetlana jako PLN/g wraz z rzeczywistą datą notowania.
- Błąd pobierania ceny złota jest niezależny od kursów walut i nie blokuje przelicznika. Błąd głównej tabeli A czyści aktywne kursy, aby nie pokazywać starej wyceny jako aktualnej.
- W audycie rozdzielono daty efektywne tabel A i B, ponieważ nie należy zakładać, że obie tabele mają identyczny dzień publikacji.

### Zakres nadal otwarty
- Historia przeliczeń nie jest jeszcze trwale zapisywana.
- Brak wyboru daty przez kalendarz; data jest wprowadzana w formacie ISO.
- Brak trwałego cache, wyszukiwania nazw walut i pełnego słownika jednostek ISO 4217.
- CI dla końcowego SHA musi potwierdzić kompilację, testy JVM i emulatorowe.

### Commity etapu 84
- `f7d1aeda662057dcbdf213f0d73f5c09b581905d` — wybór daty historycznej i cena złota w UI.
- `bbaa5c6e00b41dc61966194599b12ac00948ee63` — niezależność błędu ceny złota od przelicznika.

### Następny etap
1. Potwierdzić CI dla końcowego SHA i poprawić wszystkie wykryte problemy.
2. Dodać trwałą historię przeliczeń z datą kursu, tabelą, parą walutową, kwotą i wynikiem.
3. Dodać pełny słownik ISO 4217 i wyszukiwanie walut po nazwie.
4. Dodać testy UI dla pobierania kursów, wyboru daty i błędów sieciowych.


### Uzupełnienie kontroli kodu
- `0c877a34a748db8d2beae47154eebeb7a675ea4e` — jawne parsowanie daty historycznej w karcie kalkulatora.


## 23. Etap 85 — usunięcie blokady kompilacji i kontrola CI (15 listopada 2026 r.)

### Diagnoza
Kontrola GitHub Actions dla PR #68 wykazała:
- Android prototype build, run `43744986147`, job `132907174308` — **failure** na kroku `:app:compileDebugKotlin`;
- Android GUI validation, run `43744986141` — **cancelled** przed testami JVM i emulatorowymi;
- Repository compliance, run `43744986154` — **success**.

Log kompilacji wskazał jednoznacznie błąd w `NbpCurrencyConverterCard.kt`: import `androidx.compose.foundation.layout.weight` próbował udostępnić wewnętrzną właściwość `RowColumnParentData?.weight`. W obrębie treści `Row` dostęp do `Modifier.weight` zapewnia zakres `RowScope`; jawny import nie jest potrzebny.

### Wykonana poprawka
- Usunięto błędny import `androidx.compose.foundation.layout.weight`.
- Zachowano wywołania `Modifier.weight(1f)` w polach waluty źródłowej i docelowej, ponieważ znajdują się w treści `Row`.
- Nie zmieniano logiki kursów, obliczeń, dat historycznych ani obsługi złota.

Commit poprawki: `6f2772b4449040ad5f0a4eb96bca1f7429444773`.

### Status weryfikacji
Poprawkę zapisano na gałęzi. Wymagane jest ponowne uruchomienie pełnego CI dla nowego SHA: testy JVM, kompilacja debug, testy Compose na emulatorze i compliance. Nie uznaje się jeszcze kompilacji ani testów UI za zaliczone; APK nie jest potwierdzone.

### Kolejne etapy po usunięciu blokady
1. Ustalić wynik CI na aktualnym SHA i naprawić ewentualne dalsze błędy.
2. Przed trwałą historią przeliczeń zinwentaryzować istniejący Room/SQLite, wersję bazy i migracje; nie tworzyć drugiej bazy ani nie zgadywać schematu.
3. Dodać trwałą historię przeliczeń z kursem, tabelą, datą publikacji, kwotą, parą i wynikiem.
4. Uzupełnić słownik ISO 4217 oraz wyszukiwanie nazw.
5. Dodać testy UI i sieciowe dla tabel A/B, błędów HTTP, timeoutów, pustych odpowiedzi i trybu offline.

PR #68 pozostaje otwarty i niescalony.


## 24. Etap 86 — trwały cache notowań NBP na dysku urządzenia (15 listopada 2026 r.)

### Założenie użytkowe
Dane historyczne NBP są publikacjami archiwalnymi: po zapisaniu na urządzeniu nie są ponownie pobierane ani nadpisywane. Dane bieżące mają osobny klucz cache i są odświeżane po upływie 15 minut. Dzięki temu odświeżanie kursów bieżących nie zmienia zapisanych notowań historycznych.

### Wykonane zmiany
- Rozszerzono `NbpRatesRepository` o trwały cache plikowy, oparty na katalogu prywatnym aplikacji przekazanym jako `cacheDirectory`.
- Karta przelicznika przekazuje katalog `filesDir/nbp-rates-cache`; dane pozostają w prywatnej przestrzeni aplikacji i nie wymagają dodatkowych uprawnień do pamięci.
- Odpowiedzi API NBP są zapisywane jako oryginalny JSON wraz ze znacznikiem czasu. Nazwy plików są skrótami SHA-256 kluczy zapytań, więc daty i kody tabel nie tworzą ścieżek plików.
- Cache tabel historycznych A/B/C i zakresów historycznych oraz cen złota jest trwały i działa także po ponownym utworzeniu repozytorium oraz bez połączenia sieciowego.
- Osobne wpisy `latest-table-A`, `latest-table-B` i `latest-gold` są odświeżane po TTL 15 minut. Aktualizacja wpisu bieżącego nie nadpisuje plików historycznych.
- Zapis pliku odbywa się przez plik tymczasowy i zmianę nazwy, z bezpiecznym zapisem zastępczym. Uszkodzony wpis cache jest traktowany jako nieczytelny i pobierany ponownie.
- Dodano testy: odczyt archiwalnego kursu z dysku po utworzeniu nowego repozytorium bez sieci oraz odświeżenie bieżącego kursu po przekroczeniu TTL.

### Pliki i commity
- `NbpRatesRepository.kt` — `a952f8c8a2b6c4b15142c7217ff87872d60b7d06`.
- `NbpCurrencyConverterCard.kt` — `eff36b1989dc5a6239fd40a4a600e715ebf1115c`.
- `NbpRatesRepositoryTest.kt` — `9f7606d89b8358da6012ec650989d74009192c61` oraz `6039d9d2dc6fcb0b89e4fc5074d0a5a4e7209824`.

### Ograniczenia i kontrola
- Cache jest lokalny dla instalacji aplikacji; odinstalowanie aplikacji lub wyczyszczenie jej danych usuwa również archiwum.
- Wpisy historyczne są zachowywane bezterminowo. W przyszłości warto dodać limit rozmiaru i narzędzie do kontroli/eksportu archiwum, jeśli wolumen danych będzie rósł.
- Testy źródłowe zostały dodane, ale zaliczenie kompilacji i testów wymaga świeżego przebiegu GitHub Actions na końcowym SHA. Nie deklaruje się jeszcze gotowego APK.

### Następne etapy
1. Uzupełnić pełny katalog walut ISO 4217: kod, nazwa, symbol, liczba miejsc dziesiętnych i status aktywności; dodać wyszukiwanie po nazwie i kodzie.
2. Dodać testy odporności cache: uszkodzony plik, brak praw do zapisu, przerwane pobieranie, brak sieci, wygasły wpis bieżący i zachowanie wpisów historycznych.
3. Dodać testy UI wyboru daty oraz jawnie oznaczyć źródło jako bieżące albo historyczne.
4. Po zielonym CI rozszerzyć testy integracyjne i przeprowadzić kontrolę końcową PR.


### Uzupełnienie testów etapu 86
Weryfikacja ponownego odczytu pliku testowego ujawniła, że poprzednia próba dopisania dwóch testów nie zmieniła treści pliku. Poprawiono to w commicie `3442726541015b648307ad44009e712318720953`. Ponownie pobrany plik zawiera teraz oba testy:
- `historicalRatesPersistAcrossRepositoryInstancesAndWorkOffline`;
- `latestRatesRefreshAfterTtlWhileHistoricalArchiveRemainsSeparate`.

Testy są zapisane w repozytorium, ale ich wykonanie nadal wymaga potwierdzenia w GitHub Actions. Nie należy utożsamiać obecności testów ze statusem PASS.


---

## Etap 87 — trwała historia przeliczeń walutowych i odporność cache NBP (15 listopada 2026 r.)

Wykorzystano istniejący magazyn SQLite `PaymentLedgerDatabase` (wcześniej wersja 7) i istniejący `PaymentLedgerRepository`; nie utworzono drugiej bazy. Migracja podnosi wersję do 8 i tworzy tabelę `currency_conversion_history`.

Historia przechowuje identyfikator, czas, kwotę źródłową i docelową, kody walut, zastosowany kurs krzyżowy, datę notowania i źródło kursu. Wartości liczbowe są przechowywane jako tekst dziesiętny. Przelicznik zapisuje udane operacje i umożliwia odczyt ostatnich 50. Warstwa repozytorium wykonuje operacje SQLite na `Dispatchers.IO`.

Dodano testy instrumentacyjne:
- trwałość historii po zamknięciu i ponownym otwarciu bazy;
- dokładność wartości dziesiętnych i kolejność od najnowszego;
- odrzucenie kursu zerowego i duplikatu identyfikatora;
- migracja z wersji 7 do 8 bez utraty istniejących tabel;
- dostęp do historii przez repozytorium po ponownym otwarciu;
- testy cache NBP dla przerwanego pobierania i błędu zapisu na dysk.

**Ograniczenie:** historia przeliczeń nie została jeszcze dodana do eksportu/importu JSON. Wymaga to osobnej, wersjonowanej zmiany formatu archiwum. Status aktywności walut ISO wymaga również kontrolowanego źródła danych, a nie tylko bazy platformy.

Wyniki testów instrumentacyjnych i kompilacji muszą zostać potwierdzone przez GitHub Actions dla najnowszego SHA. Nie deklaruje się gotowości APK przed tym potwierdzeniem.


## Etap 88 — integracja historii przeliczeń z archiwum JSON

Format eksportu/importu rejestru podniesiono do wersji 3. Dodano tablicę `currencyConversions`; dekoder zachowuje zgodność z wersjami 1 i 2. Historia jest eksportowana z SQLite, a import obejmuje ją w jednej transakcji wraz z wpisami bankowymi, transakcjami i darowiznami. Powtórny import identycznych zapisów jest idempotentny. Konflikt treści pod istniejącym identyfikatorem jest raportowany w podglądzie i blokuje import bez częściowego zapisu.

Dodano testy kodeka oraz testy instrumentacyjne round-trip, ponownego importu i konfliktu. Wyniki CI dla końcowego SHA pozostają warunkiem uznania etapu za zweryfikowany. PR #68 pozostaje otwarty.


## Etap 89 — archiwizacja pełnej historii przeliczeń (15 listopada 2026 r.)

Usunięto niejawne ograniczenie do 1000 rekordów z operacji archiwalnych. Metoda readAllCurrencyConversions() udostępnia pełną historię do eksportu JSON, podglądu importu i deduplikacji w transakcyjnym imporcie. Limit 1–1000 pozostaje wyłącznie dla odczytu prezentacyjnego. Dodano test instrumentacyjny obejmujący 1005 zapisów. Weryfikacja CI dla końcowego SHA pozostaje wymagana; PR #68 pozostaje otwarty i niescalony.


## Etap 90 — wersjonowany katalog ISO 4217

Dodano migawkę List One SIX (2026-09-17, 178 wpisów) i List Three (2026-01-01, 137 wpisów), kod numeryczny, jednostki mniejsze, rozróżnienie funduszy i daty wycofania. Katalog walut płatniczych odrzuca fundusze i kody specjalne bez jednostki mniejszej; kody historyczne są dostępne osobno i nie nadpisują aktywnych kodów o tym samym identyfikatorze. Nazwy lokalne i symbole pochodzą z Androida, z kontrolowanym zapasowym użyciem nazwy źródłowej/kodu. Testy dodane; CI dla końcowego SHA oczekuje na zakończenie.


### Etap 90 — uzupełnienie kontroli danych

Testy katalogu weryfikują teraz także liczebność migawek SIX (178 bieżących i 137 historycznych), format kodów ISO, wykładniki jednostek mniejszych oraz daty wycofania. Kontrole CI dla najnowszego commitu są uruchomione; nie uznaję kompilacji i testów emulatorowych za zakończone do czasu ich wyniku.


Uzupełnienie etapu 90: po weryfikacji istniejącego testu precyzji zachowano kody funduszy z określonym wykładnikiem w katalogu metadanych (np. CLF), aby nie zmieniać dotychczasowej precyzji obliczeń. Oddzielna lista `paymentCurrencies` i wyszukiwarka nadal wykluczają fundusze z wyboru waluty płatniczej. Dodano test regresyjny.
