<!-- SPDX-FileCopyrightText: 2026 bartoszruta26-droid -->
<!-- SPDX-License-Identifier: CC0-1.0 -->

# Katalog herbów, godeł, flag i znaków instytucjonalnych

Aktualizacja: 2026-11-20  
Rejestr CSV: institutional-svg-sources.csv  
Docelowe zasoby Android: obywatel-android/app/src/main/assets/institutions/

## Zasady kwalifikacji

- Rekord w CSV jest kandydatem do pobrania, a nie zbiorowym potwierdzeniem licencji.
- Przed pobraniem aplikacja musi pobrać stronę opisu konkretnego pliku i odczytać autora, źródło, licencję, wersję i ewentualne ograniczenia.
- Preferować domenę publiczną, CC0 i licencje zgodne z polityką projektu. Dla CC BY/CC BY-SA zachować autora, tytuł, źródło, licencję i wymagane oznaczenia.
- Nie zakładać, że plik jest wolny od ograniczeń tylko dlatego, że znajduje się w Wikimedia Commons. Ochrona symboli państwowych, emblematów organizacji i znaków towarowych może obowiązywać niezależnie od licencji pliku.
- Nieaktualne, historyczne, rekonstrukcyjne i niezweryfikowane warianty pozostają w katalogu badawczym; nie trafiają automatycznie do biblioteki produkcyjnej.
- Dodanie grafiki do biblioteki nie upoważnia do umieszczenia jej na dokumencie urzędowym. Zachować ograniczenia permittedKinds i wymagać właściwego uprawnienia.
- Pobieranie ma następować na żądanie, z podglądem, informacją o licencji, rozmiarem, potwierdzeniem i możliwością usunięcia.
- SVG trzeba walidować jako XML/SVG, ograniczyć rozmiar, odrzucić skrypty, zewnętrzne odwołania, encje zewnętrzne i niebezpieczne elementy. Renderować w bezpiecznym rendererze.
- Błąd sieci, licencji, walidacji lub zapisu nie może naruszać istniejących zasobów.

## Priorytet: Państwo Watykańskie i Stolica Apostolska

### Herb Państwa Watykańskiego od 2023 r.

Plik: Coat of arms of Vatican City (2023–present).svg  
Strona pliku: https://commons.wikimedia.org/wiki/File:Coat_of_arms_of_Vatican_City_(2023%E2%80%93present).svg  
Oryginalny SVG: https://upload.wikimedia.org/wikipedia/commons/3/3a/Coat_of_arms_of_Vatican_City_%282023%E2%80%93present%29.svg  
Podstawa: załącznik B do Fundamental Law of Vatican City State z 2023 r. Strona pliku wskazuje domenę publiczną. Zachować oryginalny plik, nie zastępować uproszczonym rysunkiem.

### Emblemat Stolicy Apostolskiej publikowany przez Vatican.va

Plik: Emblem of the Holy See (vatican.va).svg  
Strona pliku: https://commons.wikimedia.org/wiki/File:Emblem_of_the_Holy_See_(vatican.va).svg  
Oryginalny SVG: https://upload.wikimedia.org/wikipedia/commons/d/d4/Emblem_of_the_Holy_See_%28vatican.va%29.svg  
Pochodzenie: emblemat publikowany na Vatican.va; strona pliku podaje CC0. Odróżniać go od herbu Państwa Watykańskiego.

### Pozostałe zasoby

- holy-see-emblem-heraldic-reference.svg — grafika referencyjna; zastąpić dopiero po weryfikacji oficjalnego oryginału.
- pope-leo-xiv-coat-of-arms-reference.svg — grafika referencyjna; nie traktować jako oficjalnego wzoru bez potwierdzenia źródła i licencji.
- banner-holy-roman-church-historical.svg — zasób historyczny, nie współczesny znak Stolicy Apostolskiej.

## Kolekcje organizacji międzynarodowych

Wikimedia Commons:  
https://commons.wikimedia.org/wiki/Category:SVG_flags_of_international_organizations  
https://commons.wikimedia.org/wiki/Category:United_Nations_flags_and_insignia  
https://commons.wikimedia.org/wiki/Category:SVG_international_flags  
https://commons.wikimedia.org/wiki/International_flags  
https://commons.wikimedia.org/wiki/Category:SVG_coats_of_arms  
https://commons.wikimedia.org/wiki/Category:SVG_coats_of_arms_by_country

Kolekcja SVG flags of international organizations zawiera 118 plików. Wymieniane przykłady obejmują flagi ICAO, ILO, IMO, ITU, NATO, UNESCO, UNIDO, UPU, WHO, IAEA i WFP. Inne rozpoznane instytucje i organizacje w rejestrze obejmują ONZ, UNICEF, UNFCCC, UE, Radę Europy, ASEAN, Unię Afrykańską, Ligę Państw Arabskich, OPEC, OAS, OBWE, Wspólnotę Narodów, Mercosur, CARICOM, SADC, EAEU, Radę Nordycką, OIC, Organizację Państw Turkijskich, FIFA, MKOl, IPC, WIPO i Międzynarodowy Ruch Czerwonego Krzyża.

Wpisy w CSV oznaczone verify-per-file wymagają sprawdzenia strony konkretnego pliku przed pobraniem. Wpisy not-identified są wyłącznie zapytaniami wyszukiwawczymi, nie gotowymi adresami plików.

## Minimalny model metadanych

assetId, label, organization, symbolType, validityPeriod, sourcePageUrl, downloadUrl, author, license, licenseUrl, sourceRevision, downloadedAt, sha256, mimeType, byteSize, verificationStatus, permittedKinds, notes.

## Status implementacji (2026-11-16)

- W bibliotece zasobów aplikacji dodano panel katalogu i pobierania wybranych plików SVG.
- Pobranie zapisuje SVG w istniejącym szyfrowanym magazynie aplikacji oraz metadane źródła, notę licencyjną, SHA-256 i status `pending`.
- Dodano eksport do ZIP przez systemowy selektor zapisu Androida. Archiwum zawiera oryginalne SVG, `manifest.json` i `README.md`; można je następnie ręcznie rozpakować i dodać do repozytorium.
- Walidacja odrzuca pliki z DTD/encjami, skryptami, `foreignObject`, zewnętrznymi/aktywnymi odwołaniami oraz pliki większe niż 12 MB.
- Emblemat Stolicy Apostolskiej przeniesiono do `assets/institutions/holy-see-emblem-vatican-va.svg` i zarejestrowano jako zasób wbudowany.
- Wszystkie pobrane symbole z katalogu pozostają z pustym `permittedKinds`, dopóki licencja i uprawnienie do konkretnego zastosowania nie zostaną zatwierdzone.
- Adresy źródłowe wymagają testu odpowiedzi serwera; plik niedostępny lub niezgodny z SVG zostanie odrzucony. Rejestr nie jest gwarancją dostępności każdego historycznego URL.

Status kompilacji i testów należy odczytać z PR #80; nie uznawać implementacji za gotową do scalenia, dopóki Android build i walidacja GUI nie zakończą się powodzeniem.

## Rozszerzony indeks kandydatów — 20 listopada 2026

Zapisany indeks źródeł: `generated/institutional-symbol-candidates.csv`  
Raport źródeł: `generated/institutional-symbol-source-audit.json`

- **2 734 rekordy kandydackie** po deduplikacji ścieżek źródłowych. Nie jest to liczba 2 734 odrębnych organizacji ani 2 734 plików SVG.
- Źródła obejmują Wikimedia Commons, publiczne repozytoria GitHub, kolekcje organizacji humanitarnych i osobne zbiory flag.
- Wygenerowany indeks należy filtrować po kolumnie `path` kończącej się na `.svg`, aby otrzymać pliki SVG.
- Wyszukiwanie powinno obejmować nazwę ścieżki, repozytorium, grupę, źródło, autora i opis źródłowy.
- Pobieranie dotyczy pojedynczego wybranego rekordu: użyj `download_url` albo `source_url`, zachowaj upstream repository/ref/path oraz metadane źródłowe.
- `rights_status` jest statusem informacyjnym konkretnego rekordu. Nie blokuje odkrywania ani wyszukiwania. Nie oznacza automatycznie prawa do dowolnego użycia znaku.
- Znaki chronione i niezweryfikowane pozostają widoczne w katalogu; ich pobranie, redystrybucję i zastosowanie ocenia się osobno dla konkretnego kontekstu.

### Model działania

1. Wyszukiwanie działa na indeksie nazw i metadanych, bez pobierania grafiki.
2. Użytkownik wybiera konkretny rekord i wariant znaku.
3. Aplikacja pobiera tylko wskazany plik, waliduje SVG, zapisuje SHA-256, źródło, autora, wersję i metadane.
4. Ponowne wybranie istniejącego pliku wykorzystuje lokalną kopię/cache.
5. Osobny pakiet podstawowy przechowuje do 50 często wykorzystywanych SVG. Nie jest powiązany z ograniczeniem wyszukiwania katalogu.

### Ograniczenia aktualnego indeksu

Indeks upstream obejmuje także 356 plików rastrowych i formatów nie-SVG; ich obecność jest celowa jako referencja źródłowa, ale nie mogą przejść do importera SVG bez konwersji i odrębnej walidacji. Nie należy traktować ich jako SVG. Zbiór `collective-service/organizations-logo` ma status referencyjny, a `country-flags` nie ma wykrytego pliku LICENSE w audytowanym drzewie. Repozytorium `thesvg` jest zinwentaryzowane (14 797 SVG), lecz nie zostało automatycznie rozwinięte do indeksu kandydatów; ten zbiór wymaga osobnej selekcji i odczytu metadanych licencji poszczególnych marek.


## Priorytetowy wykaz oficjalnych znaków państwowych i instytucjonalnych

Dodatkowa lista nazw i oficjalnych punktów wejścia do dalszego indeksowania:
`institutional-svg-priority-sources.csv`.

Lista obejmuje godło i flagę RP, emblemat UE, ONZ, WHO, UNICEF, UNESCO, NATO, Rady Europy, Policję, Państwową Straż Pożarną, Straż Graniczną oraz ZUS, NFZ, KRUS, PFRON, NBP, GUS, PIP, UOKiK, UODO, RPO i KPRM. Pozycje bez dokładnego adresu pliku są rekordami wyszukiwawczymi, a nie zatwierdzonymi plikami SVG.

Katalog nazw pozostaje przeszukiwalny niezależnie od statusu prawnego. Status prawny opisuje natomiast warunki pobrania, redystrybucji i konkretnego zastosowania.

## Rozszerzenie polskich źródeł i nowa kategoria „Ikony → Symbole”

Aktualizacja: 2026-11-22

Pełny rejestr punktów wejścia i adresów źródłowych znajduje się w:
- `institutional-svg-priority-sources.csv` — 42 nowe rekordy polskich instytucji, symboli państwowych, samorządów i organizacji.
- `institutional-svg-sources.csv` — szczegółowy rejestr konkretnych kandydatów SVG, z osobnymi polami strony źródłowej i adresu pobrania.

### Oficjalne źródła pierwszego wyboru
- [Materiały do pobrania — Prezydent RP](https://www.prezydent.pl/dla-mediow/materialy-do-pobrania) — godło RP, znak Prezydent RP i znaki patronatów.
- [Znak Prezydent RP — warianty i zasady stosowania](https://www.prezydent.pl/dla-mediow/materialy-do-pobrania/znak-prezydent-rp)
- [Godło RP — oficjalny plik](https://www.prezydent.pl/dla-mediow/materialy-do-pobrania/godlo-rp)
- [Wspólna identyfikacja wizualna administracji rządowej](https://www.gov.pl/web/premier/wspolna-identyfikacja-wizualna-administracji-rzadowej)
- [ZUS — logo](https://www.zus.pl/o-zus/o-nas/logo-zus)
- [NFZ — logo i materiały dla mediów](https://www.nfz.gov.pl/dla-mediow/logo-nfz/)
- [KRUS — logotypy](https://www.gov.pl/web/krus/logotypy-krus)
- [PFRON — księga identyfikacji wizualnej](https://www.pfron.org.pl/fileadmin/Redakcja/logo/PFRON_Ksiega_Identyfikacji_Wizualnej_2021.pdf)
- [Ustawa o godle, barwach i hymnie RP — tekst jednolity 2025](https://eli.gov.pl/eli/DU/2025/299/ogl)

### Kolekcje do przeszukania
- [SVG logos of governments and government agencies of Poland](https://commons.wikimedia.org/wiki/Category:SVG_logos_of_governments_and_government_agencies_of_Poland) — kategoria obejmuje 52 pliki, m.in. Sejm RP, Senat, NFZ, GUS, NBP, KPRM i ministerstwa.
- [Herby Polski](https://commons.wikimedia.org/wiki/Category:Coats_of_arms_of_Poland)
- [Herby województw](https://commons.wikimedia.org/wiki/Category:Coats_of_arms_of_voivodeships_of_Poland)
- [Herby powiatów](https://commons.wikimedia.org/wiki/Category:Coats_of_arms_of_counties_of_Poland)
- [Herby miast](https://commons.wikimedia.org/wiki/Category:Coats_of_arms_of_cities_in_Poland)
- [Flagi Polski](https://commons.wikimedia.org/wiki/Category:Flags_of_Poland)
- [Pieczęcie Polski](https://commons.wikimedia.org/wiki/Category:Seals_of_Poland)
- [Agencje rządowe Polski](https://commons.wikimedia.org/wiki/Category:Government_agencies_of_Poland)
- [Organizacje pozarządowe w Polsce](https://commons.wikimedia.org/wiki/Category:Non-governmental_organizations_in_Poland)
- [Związki zawodowe w Polsce](https://commons.wikimedia.org/wiki/Category:Trade_unions_in_Poland)

### Hierarchia w aplikacji
Nadkategoria: **Ikony**  
Nowa kategoria: **Symbole**  
Sąsiednie kategorie katalogu: **Herby**, **Godła**, **Flagi**, **Logotypy**, **Pozostałe**.

Do kategorii „Symbole” trafiają m.in. insygnia, odznaki, oznaki, pieczęcie, medale, ordery i monogramy. Rozpoznawanie kategorii nie jest potwierdzeniem autentyczności ani prawa do użycia znaku.

### Ważne rozróżnienie statusów
Adres strony instytucji, strona opisu pliku i bezpośredni adres pobrania to trzy różne pola. Puste `download_url` oznacza, że wskazano źródło, ale nie potwierdzono jeszcze bezpośredniego pliku. Rekordy ZUS, NFZ, KRUS, PFRON, Prezydenta RP i część znaków organów publicznych pozostają pod kontrolą warunków użycia. Kolekcje Wikimedia są punktami odkrywania — każdy plik wymaga indywidualnej kontroli autora, licencji, aktualności i ograniczeń prawnych.

