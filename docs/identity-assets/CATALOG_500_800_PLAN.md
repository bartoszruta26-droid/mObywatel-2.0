# Plan rozbudowy katalogu symboli instytucjonalnych

## Cel

Docelowy katalog: **800 rekordów**. Pierwsze wydanie użytkowe: minimum **500 zweryfikowanych rekordów**. Licznik obejmuje odrębne organizacje lub odrębne oficjalne warianty symbolu, nigdy duplikaty plików. Flagi państw i terytoriów są osobną grupą i nie są liczone jako herby ani logotypy NGO.

## Nowa taksonomia

1. Państwa, godła i herby państwowe.
2. Herby regionalne, wojewódzkie, powiatowe, miejskie i gminne.
3. Polskie instytucje publiczne: ZUS, NFZ, KRUS, PFRON, GIS, GUS, NBP, PIP, UOKiK, UODO, RPO, KPRM, ministerstwa, urzędy i służby.
4. Polskie NGO: fundacje, stowarzyszenia, organizacje pożytku publicznego, federacje, hospicja i organizacje pomocowe.
5. Międzynarodowe NGO i organizacje humanitarne: IFRC, Oxfam, Save the Children, CARE, Mercy Corps, AMREF, Internews, PATH, VSO i inne.
6. Organizacje międzyrządowe i instytucje międzynarodowe: ONZ, WHO, UNICEF, UNHCR, OCHA, UNESCO, ILO, IOM, WFP, NATO, UE i inne.
7. Organizacje religijne, ekumeniczne i charytatywne.
8. Flagi państw, terytoriów i organizacji — odrębna sekcja pomocnicza.

## Docelowy podział 800 rekordów

| Grupa | 500 – wydanie pierwsze | 800 – katalog docelowy |
|---|---:|---:|
| Godła i herby państwowe | 35 | 55 |
| Herby regionalne i samorządowe | 120 | 200 |
| Polskie instytucje publiczne | 65 | 100 |
| Polskie NGO, zdrowie, edukacja i pomoc | 60 | 100 |
| Międzynarodowe NGO i humanitarne | 80 | 130 |
| Organizacje międzyrządowe | 70 | 100 |
| Organizacje religijne i ekumeniczne | 30 | 45 |
| Flagi państw i terytoriów | 40 | 70 |
| **Razem** | **500** | **800** |

## Źródła GitHub ocenione w audycie

- `simple-icons/simple-icons`: ponad 3 400 plików SVG i plik JSON z nazwą, źródłem, opcjonalną licencją i wytycznymi. Licencja CC0 repozytorium **nie oznacza**, że wszystkie poszczególne znaki są CC0; wymagana jest ocena per ikona i praw do znaków.
- `glincker/thesvg`: ponad 7 900 znaków według README i 14 797 plików SVG w drzewie repozytorium. MIT repozytorium nie rozstrzyga praw do poszczególnych znaków i znaków towarowych.
- `collective-service/organizations-logo`: realny zbiór znaków NGO, agencji humanitarnych i administracji (m.in. GOAL, IASC, IFRC, Internews, GOARN, AMREF, CARE, Oxfam, Mercy Corps, OCHA, UNICEF, UNHCR, WHO, VSO). Nie znaleziono pliku LICENSE — zasoby tylko jako referencje do czasu zgody lub potwierdzenia licencji.
- `HatScripts/circle-flags` i `lipis/flag-icons`: duże zbiory flag z licencją MIT; używać w osobnej grupie flag, z dodatkową kontrolą ograniczeń prawnych.
- `TiltTek/heraldry`: biblioteka generowania heraldyki, a nie zbiór autentycznych herbów instytucji. Nie przedstawiać generowanych wzorów jako oficjalnych.

## Model rekordów

Każdy rekord ma: stabilny `assetId`, nazwę, nazwę alternatywną, grupę, podgrupę, kraj/obszar, typ symbolu, właściciela, okres obowiązywania, źródło oryginalne, adres pliku, autora, licencję, odnośnik do licencji, wersję źródła, sumę SHA-256, format, rozmiar, status prawny, status weryfikacji, status zgody, dozwolone zastosowania i notatki.

Statusy prawne i operacyjne muszą być rozdzielone:
- `redistributable` — można kopiować plik z zachowaniem licencji;
- `reference_only` — rekord i odnośnik, bez kopiowania pliku;
- `permission_required` — można prezentować jako pozycję chronioną, ale użycie wymaga zgody;
- `restricted_emblem` — szczególne ograniczenia ustawowe/międzynarodowe;
- `license_unknown` — bez kopiowania do zasobów aplikacji do czasu wyjaśnienia.

## Zasady importu z GitHub Actions

GitHub Actions może klonować publiczne repozytoria, pobierać wybrane pliki, optymalizować SVG, wyliczać SHA-256 i przygotowywać PR z zasobami. Nie należy automatycznie kopiować całych repozytoriów ani traktować licencji repozytorium jako licencji każdego logo. Import musi działać z allowlisty rekordów, zachowywać upstream repo/ref/path, plik licencji, źródło, atrybucję i status prawny. Każdy import ma być powtarzalny i nie może nadpisywać zatwierdzonych zasobów bez kontroli różnic.

## Etapy

1. Zbudować indeks kandydatów i rejestr źródeł (ten etap).
2. Zmapować nazwy organizacji i deduplikować po organizacji, nie tylko nazwie pliku.
3. Pobrać metadane per plik i rozdzielić pliki możliwe do redystrybucji od chronionych.
4. Importować zatwierdzone zasoby do `app/src/main/assets/institutions/<group>/`.
5. Dodać testy kompletności manifestu, integralności SHA-256, poprawności SVG, unikalności identyfikatorów i zgodności grup.
6. Zwiększać licznik 500 → 800 dopiero po przejściu kontroli źródeł, praw i jakości.

## Ważne

Rekordy referencyjne i znaki wymagające zgody pozostają w katalogu i są wyszukiwalne. Nie otrzymują automatycznie prawa do użycia na dokumentach. Nie tworzymy fałszywych herbów ani nie przypisujemy organizacji grafik znalezionych bez wiarygodnego źródła.
