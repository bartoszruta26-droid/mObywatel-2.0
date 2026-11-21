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
