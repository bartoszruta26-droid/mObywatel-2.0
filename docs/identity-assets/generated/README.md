<!-- SPDX-FileCopyrightText: 2026 bartoszruta26-droid -->
<!-- SPDX-License-Identifier: CC0-1.0 -->

# Indeks symboli instytucjonalnych

## Pliki

- `institutional-symbol-candidates.csv` — 2 734 rekordy ścieżek źródłowych.
- `institutional-symbol-source-audit.json` — raport z 15 źródeł i metadane kandydatów.
- `../institutional-svg-priority-sources.csv` — lista priorytetowych oficjalnych znaków do dalszej selekcji.

## Ważne rozróżnienia

Indeks zawiera 2 734 rekordy kandydackie, nie 2 734 zweryfikowane organizacje ani 2 734 pliki SVG. 356 rekordów odnosi się do formatów innych niż SVG. Dla biblioteki SVG stosuj filtr rozszerzenia `.svg` i walidację zawartości po pobraniu.

Pola `rights_status` i `import_status` są metadanymi źródłowymi. Nie wolno filtrować nimi wyszukiwania nazw. Każdy wynik pozostaje odnajdywalny. Pobieranie dotyczy jednego wybranego pliku i zachowuje adres strony źródłowej, autora, licencję, atrybucję, źródłowe repozytorium/wersję/ścieżkę oraz sumę SHA-256.

Nie utożsamiaj licencji repozytorium z licencją każdego logo. Szczególnie emblematy państwowe i organizacji międzynarodowych mogą podlegać ograniczeniom niezależnym od licencji pliku. Dane o takich ograniczeniach nie blokują katalogowania ani wyszukiwania.

## Grupy

- 1 229 wpisów Wikimedia Commons: herby samorządowe, organizacje międzynarodowe i NGO, instytucje, flagi organizacji.
- 1 445 wpisów ze zbiorów flag (circle-flags, flag-icons, country-flags).
- 57 wpisów kolekcji collective-service organisations-logo.
- 3 wpisy wybrane z Simple Icons.

Repozytorium `glincker/thesvg` zostało zinwentaryzowane jako 14 797 plików SVG, ale nie rozwinięto go do rekordów kandydackich, ponieważ wymaga dodatkowej selekcji i weryfikacji praw do poszczególnych marek.

## Zakładany przepływ

1. Szukaj po nazwie, aliasie, państwie, organizacji, typie symbolu i źródle.
2. Otwórz rekord i pokaż metadane źródła.
3. Pobierz wyłącznie wybrany plik SVG.
4. Zweryfikuj MIME/XML/SVG, rozmiar i aktywną zawartość; zachowaj oryginał i SHA-256.
5. Zapisz plik w pamięci podręcznej; kolejne użycia nie wymagają kolejnego pobrania.
6. Do pakietu 50 lokalnych znaków wybierz najczęściej używane po analizie rzeczywistego wykorzystania w dokumentach. Nie zakładaj popularności na podstawie samej nazwy ani kolejności w CSV.

## Aktualizacja

Generator indeksu powinien być powtarzalny, deduplikować po źródłowym identyfikatorze pliku i adresie, zapisywać wersję/ref źródła oraz datę audytu. Raportuj osobno liczbę wszystkich rekordów, plików SVG, unikalnych organizacji i wariantów historycznych.