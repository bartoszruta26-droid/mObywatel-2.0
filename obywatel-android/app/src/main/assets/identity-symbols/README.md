# Katalog symboli instytucjonalnych SVG

- `catalog.csv` zawiera 2 734 kandydackie rekordy źródeł z GitHub i Wikimedia Commons.
- Katalog jest indeksem odkrywania, nie zatwierdzoną biblioteką redystrybucyjną.
- Ekran aplikacji pokazuje tylko rekordy SVG z możliwym, dozwolonym adresem pobrania.
- Pojedynczy wybrany SVG jest pobierany przez wspólną warstwę Tor i walidowany przed zapisem.
- Źródło, autora, licencję, atrybucję, status prawny i SHA-256 zachowujemy w metadanych.
- Status prawny nie filtruje odkrywania; nie oznacza jednak zgody na redystrybucję lub użycie na dokumencie.
- Znaki wymagające zgody albo kontroli pozostają wyszukiwalne jako referencje; `permittedKinds` pozostaje puste do czasu odrębnego zatwierdzenia.

## Pakiet flag SVG

- `flags/` zawiera 50 flag z projektu `lipis/flag-icons`, katalog `flags/4x3`.
- Licencja pakietu: MIT; pełny tekst znajduje się w `FLAG-ICONS-LICENSE.txt` oraz `LICENSES/MIT.txt`.
- Rekordy są oznaczone w katalogu jako `country_flags`; pobieranie odbywa się pojedynczo przez wspólną usługę wybranego trybu sieciowego.
- Pakiet flag nie stanowi zgody na używanie herbów, godeł, pieczęci ani logotypów i nie oznacza oficjalnego poparcia przez państwo.

## Hierarchia katalogu ikon
- Nadkategoria: **Ikony**.
- Nowa kategoria: **Symbole** — insygnia, odznaki, oznaki, pieczęcie, medale, ordery i monogramy.
- Pozostałe kategorie: **Herby**, **Godła**, **Flagi**, **Logotypy**, **Pozostałe**.
- Lista polskich punktów źródłowych i oficjalnych stron: `docs/identity-assets/institutional-svg-priority-sources.csv` (42 dodane rekordy, w tym Sejm, Senat, Prezydent RP, ZUS, NFZ, KRUS, PFRON, organy centralne, samorządy i organizacje).
- Opis źródeł i ograniczeń: `docs/identity-assets/heraldry-and-institutional-symbols.md`.
- Obecność adresu w rejestrze oznacza możliwość dalszej weryfikacji, a nie pobranie grafiki ani zgodę na jej wykorzystanie.

## Rejestr 18 wybranych kolekcji źródłowych
Plik `collections.csv` jest używany przez ekran katalogu symboli. Zawiera adres strony kolekcji, adres API odkrywania, formaty, politykę praw, status prawny i niezależne stany procesu:
- wykrycie źródła: `source_registered`
- weryfikacja pojedynczego pliku: `per_file_pending`
- pobranie: `not_downloaded`
- trwały zapis: `not_saved`
- dostępność w aplikacji: `not_available`

Obsługiwane w katalogu lokalnych kandydatów formaty to SVG i PNG. SVG przechodzi walidację bezpieczeństwa, a PNG kontrolę sygnatury, IHDR, wymiarów i dekodowalności. Zasoby z niezweryfikowaną licencją są blokowane przed pobraniem. Sama licencja MIT/CC0 pliku nie przesądza o prawach do znaku towarowego, godła, flagi ani odznaczenia.
