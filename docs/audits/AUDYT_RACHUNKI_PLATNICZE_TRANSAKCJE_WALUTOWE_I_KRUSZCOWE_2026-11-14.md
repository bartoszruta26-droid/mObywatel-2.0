<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Audyt funkcjonalno-architektoniczny: „Rachunki płatnicze” — rejestr transakcji walutowych i kruszcowych

- **Data:** 14 listopada 2026 r.
- **Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`
- **Gałąź audytowana:** `main`
- **Commit bazowy:** `631aa68ed0e2bcbedd70201751214bd4c08fd5cc`
- **Obszar:** `obywatel-android` (Android, Kotlin, Jetpack Compose)
- **Status:** etap 1 wdrożony na gałęzi `feature/payment-ledger-stage-1`; zmiany oczekują na wynik CI i przegląd PR. Etapy trwałego magazynu, importu, JSON i NBP pozostają do wykonania.
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
