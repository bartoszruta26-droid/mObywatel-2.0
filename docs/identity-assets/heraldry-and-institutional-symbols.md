<!-- SPDX-FileCopyrightText: 2026 bartoszruta26-droid -->
<!-- SPDX-License-Identifier: CC0-1.0 -->

# Katalog herbów, godeł, flag i znaków instytucjonalnych

Aktualizacja: 2026-11-16  
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

## Katalog indeksowy i pakiet podstawowy (2026-11-20)

- `docs/identity-assets/generated/institutional-symbol-candidates.csv` zawiera 2 734 rekordy kandydatów z GitHub i Wikimedia Commons; szczegółowy raport źródeł znajduje się w `institutional-symbol-source-audit.json`.
- Aplikacja otrzymuje kopię indeksu w `app/src/main/assets/identity-symbols/catalog.csv`. Indeks jest przeszukiwany lokalnie po nazwie, grupie, repozytorium i identyfikatorze źródła.
- Dla adresów GitHub typu `/blob/{ref}/{path}` aplikacja wyprowadza bezpośredni adres `raw.githubusercontent.com`; dla Commons wykorzystuje bezpośredni adres pliku z metadanych. Pobierany jest tylko wybrany plik SVG.
- Pobranie zachowuje źródło, rewizję, ścieżkę, autora, licencję, adres licencji, opis źródła i SHA-256. Nie ma blokady wyników wyszukiwania na etapie indeksowania; status prawny i ograniczenia są metadanymi.
- Wbudowany pakiet podstawowy zawiera 50 flag SVG z projektu `lipis/flag-icons` w proporcji 4:3. Tekst licencji MIT jest w `FLAG-ICONS-LICENSE.txt`, a zakres plików i kody ISO opisuje `assets/identity-symbols/README.md`.
- Flagi są odrębnym zasobem identyfikacyjnym; sam pakiet MIT nie znosi krajowych zasad używania flag. Nie należy utożsamiać flagi z herbem, godłem, pieczęcią ani logo organizacji.
- Pobrane z indeksu znaki nie otrzymują automatycznie `permittedKinds`; to pole pozostaje puste do czasu zatwierdzenia konkretnego zastosowania. Nie ogranicza to wyszukiwania ani pobrania pliku do biblioteki.
- Każdy pobrany plik przechodzi walidację XML/SVG, limit 12 MB oraz odrzucenie DTD, encji, skryptów, `foreignObject` i zewnętrznych/aktywnych odwołań.
