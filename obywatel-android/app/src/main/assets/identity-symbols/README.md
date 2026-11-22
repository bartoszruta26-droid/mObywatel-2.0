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

## Indeksy plików GitHub
- `github-symbol-candidates.csv`: 2 734 rekordy kandydatów z GitHub i Wikimedia Commons (2 378 SVG, 175 PNG oraz pozostałe formaty referencyjne). Zapisano stronę źródłową, bezpośredni URL, status praw, licencję, autora i atrybucję, jeśli dane źródło je publikuje. Jest to indeks odkrywania, a nie zgoda na pobranie ani użycie. Rekordy z `reference_only`, `per_asset_review`, `per_file_metadata_review_required` lub ograniczeniami flag pozostają zablokowane do indywidualnej weryfikacji.
- `iconify-collections.csv`: 280 zestawów Iconify obejmujących 556 207 ikon. Dla każdego zestawu zapisano autora, licencję SPDX, adres licencji i bezpośredni JSON. Zestaw jest w formacie Iconify JSON, nie samodzielnym SVG; przed importem trzeba go bezpiecznie przekształcić do SVG i przeprowadzić walidację.


## Pobieranie i klasyfikacja zasobów — 22 listopada 2026

- Pobieranie SVG/PNG jest dostępne niezależnie od pola `rights_status`, nazwy licencji, pustego adresu licencji oraz grupy zasobu.
- Aplikacja nie rozstrzyga praw do użycia grafiki w dokumencie. Zachowuje adres źródła, bezpośredni adres pliku, autora, licencję, atrybucję, status prawny i SHA-256, aby użytkownik mógł dokonać ręcznej oceny.
- Nadal obowiązuje kontrola bezpieczeństwa technicznego: HTTPS i dozwolony host źródła, limit rozmiaru, walidacja XML/SVG oraz sygnatury i dekodowalności PNG.
- Wyniki pokazują bezpośredni adres pojedynczego pliku i przyciski „Otwórz plik” oraz „Pobierz do zasobów”. Nie kierują użytkownika do strony kategorii Wiki/GitHub zamiast grafiki.
- Kategorie aplikacji: Herby, Godła, Logotypy, Flagi, Ikony, Tła, Symbole i Pozostałe. Klasyfikacja wykorzystuje grupę, nazwę pliku, ścieżkę i źródło.
- Dodane bezpośrednie pliki z baz: Bootstrap Icons, Lucide, Tabler Icons, OpenMoji i Simple Icons (102 rekordy SVG). Zasoby GitHub/Wikimedia pozostają w tym samym katalogu i są automatycznie przypisywane do kategorii.
- Pusty opcjonalny `catalog.csv` nie może już przerwać wczytywania właściwego indeksu `github-symbol-candidates.csv`.
