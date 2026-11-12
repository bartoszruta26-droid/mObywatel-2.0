<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Audyt pokrycia edycji graficznej dokumentów

Data audytu: 2026-11-12  
Audytowana gałąź: `main`  
Commit: `a3c0c433ddee7b461e77eeb70fa4700ca3d40873`

## Cel i kryteria

Sprawdzono katalog `documentDisplayOptions`, wejścia w ekranie `Więcej → Ustawienia`, rozgałęzienia `settingsDocumentEditor`, `CredentialAppearanceEditorEntry`, kategorie `DemoDocumentsScreen`, mapowanie `credentialKey` oraz dostępność `CredentialAppearanceSettingsScreen` i `CredentialTemplatePreview`.

Za pełną edycję graficzną uznano dostęp do:
- podglądu wzoru konkretnego dokumentu;
- przypisania fotografii do konkretnego dokumentu;
- wyboru flagi, godła/herbu/logo i tła;
- konfiguracji widocznych pól;
- zapisania ustawień pod identyfikatorem tego dokumentu, niezależnie od pozostałych.

Samo wyświetlenie karty w portfelu, podgląd pliku PDF lub formularz danych nie spełnia tych kryteriów.

## Wynik

W katalogu znajduje się **51 identyfikatorów**. Identyfikator `branding` jest katalogiem zasobów graficznych, a nie dokumentem użytkownika. Dla pozostałych **50 pozycji** nie ma kompletnej, indywidualnej ścieżki edycji graficznej. Część dokumentów jest obecnie grupowana pod jednym ogólnym edytorem (np. kościelnym, cywilnym, zawodowym lub podmiotowym), przez co nie otrzymuje własnego klucza ustawień i odrębnego podglądu.

### A. Brak odrębnego podglądu i edytora graficznego — 32 pozycje

| Identyfikator | Nazwa w katalogu | Obecny stan / luka |
|---|---|---|
| `registry` | Odpis z rejestru podmiotów | Ekran edycji używa klucza `entity`, nie `registry`; brak odrębnej konfiguracji wzoru |
| `birth_extract` | Akt urodzenia | Trafia do ogólnych formularzy cywilnych; brak własnego klucza i podglądu |
| `marriage_extract` | Akt małżeństwa | Trafia do ogólnych formularzy cywilnych; brak własnego klucza i podglądu |
| `death_extract` | Odpis aktu zgonu | Trafia do ogólnych formularzy cywilnych; brak własnego klucza i podglądu |
| `large_family` | Karta Dużej Rodziny | Brak osobnej ścieżki edycji graficznej |
| `blood_donor` | Legitymacja Honorowego Dawcy Krwi | Brak osobnej ścieżki edycji graficznej |
| `disability_card` | Legitymacja osoby z niepełnosprawnością | Brak osobnej ścieżki; nie należy utożsamiać jej z edytorem orzeczeń `disability` |
| `vehicle_registration` | Dowód rejestracyjny pojazdu | Brak osobnej ścieżki edycji graficznej |
| `insurance_vehicle` | Ubezpieczenie OC pojazdu | Brak osobnej ścieżki edycji graficznej |
| `insurance_professional` | Ubezpieczenie OC zawodowe | Brak osobnej ścieżki edycji graficznej |
| `insurance_private` | Ubezpieczenie OC w życiu prywatnym | Brak osobnej ścieżki edycji graficznej |
| `insurance_business` | Ubezpieczenie OC działalności | Brak osobnej ścieżki edycji graficznej |
| `criminal_record` | Zaświadczenie o niekaralności | Brak osobnej ścieżki edycji graficznej |
| `mission_travel` | Dokument podróży służbowej / misyjnej | Brak osobnej ścieżki edycji graficznej |
| `ehic` | Europejska Karta Ubezpieczenia Zdrowotnego | Brak osobnej ścieżki edycji graficznej |
| `eu_education` | Zaświadczenie o zgodności kwalifikacji z UE | Brak osobnej ścieżki edycji graficznej |
| `church_bishop` | Dokument godności biskupiej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `church_archbishop` | Dokument godności arcybiskupiej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `church_cardinal` | Dokument godności kardynalskiej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `church_abbot` | Dokument godności opackiej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `church_minister` | Dokument posługi kościelnej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `church_missionary` | Dokument posługi misyjnej | Grupowany pod `church`; brak odrębnego klucza podglądu |
| `professional_medical` | Kwalifikacje medyczne | Grupowany pod `professional`; brak odrębnego klucza podglądu |
| `professional_education` | Kwalifikacje edukacyjne | Grupowany pod `professional`; brak odrębnego klucza podglądu |
| `professional_therapeutic` | Kwalifikacje terapeutyczne | Grupowany pod `professional`; brak odrębnego klucza podglądu |
| `professional_other` | Pozostałe kwalifikacje zawodowe | Grupowany pod `professional`; brak odrębnego klucza podglądu |
| `organization_foundation` | Dokumenty fundacji | Grupowany pod ogólny edytor podmiotów; brak odrębnego klucza |
| `organization_church` | Dokumenty Kościoła / wspólnoty | Grupowany pod `church` lub edytor podmiotów; brak jednoznacznej własnej ścieżki |
| `organization_company` | Dokumenty przedsiębiorstwa | Grupowany pod `business`; brak odrębnego klucza |
| `organization_association` | Dokumenty stowarzyszenia | Grupowany pod ogólny edytor podmiotów; brak odrębnego klucza |
| `official` | Pozostałe dokumenty urzędowe | Brak osobnej ścieżki edycji graficznej |
| `loyalty_cards` | Karty lojalnościowe sklepów | Wpis używa aliasu `loyalty`; brak spójnego, osobnego klucza ustawień i podglądu |

### B. Pozycje z edycją częściową lub wymagające doprecyzowania

- `loyalty`: wejście w starszych ustawieniach używało aliasu `loyalty`, a kategoria edycji `loyalty_cards`. W ramach nowego katalogu oba obszary trzeba ujednolicić do klucza `loyalty_cards`.
- `registry`: istnieje ekran rejestru, ale jest opakowany kluczem `entity`; należy rozdzielić wzór odpisu rejestrowego od ogólnej karty podmiotu.
- `business`, `entity`, `organization_*`: dostępne są ogólne ekrany podmiotowe, ale rodzaje podmiotów nie mają konsekwentnie osobnych ustawień graficznych.
- `church` i `professional`: istnieją ogólne edytory, lecz warianty szczegółowe są kierowane do nich zbiorczo.
- `civil`: istnieje ogólny edytor cywilny, ale akty urodzenia, małżeństwa i zgonu nie mają własnych konfiguracji.
- `children` i `vaccines`: istnieją ekrany ewidencji, ale należy rozstrzygnąć, które wpisy mają być kartą/legitymacją, a które pozostają rekordami medycznymi bez sztucznego wzoru legitymacji.

## Ważne rozróżnienie funkcjonalne

Nie każdy plik lub rekord powinien być stylizowany na legitymację. Dla dokumentów PDF, skanów, zaświadczeń i aktów należy zapewnić właściwy podgląd dokumentu źródłowego oraz możliwość przypisania grafiki tylko tam, gdzie ma ona sens. Nie wolno zastępować oryginalnego podglądu pliku fikcyjną kartą ani sugerować, że wygenerowany wzór jest dokumentem urzędowym.

## Zalecany zakres naprawy

1. Wprowadzić jedną centralną tabelę definicji dokumentów: identyfikator, tytuł, typ podglądu, klucz ustawień, dostępne pola, obsługiwane zasoby graficzne i sposób wyboru fotografii.
2. Dodać odrębne wejście z `Więcej → Ustawienia` dla każdej pozycji, która ma własny formularz edycji.
3. Dla dokumentów legitymacyjnych dodać wspólny komponent podglądu z indywidualnymi ustawieniami fotografii, flagi, godła/herbu/logo, tła i widoczności pól.
4. Dla aktów, polis, rejestrów i dokumentów źródłowych zastosować podgląd właściwy dla typu (karta ewidencyjna albo podgląd PDF/obrazu), zachowując oryginał.
5. Rozdzielić klucze wariantów: `church_*`, `professional_*`, `organization_*`, `insurance_*` oraz `*_extract`; ustawienia jednego wariantu nie mogą zmieniać pozostałych.
6. Ujednolicić aliasy `identity/identity_card`, `loyalty/loyalty_cards` i `registry/entity` oraz dodać testy kompletności katalogu.
7. Dodać test parametryzowany, który dla każdego edytowalnego dokumentu sprawdza: dostęp do ekranu edycji, podgląd, selektor zdjęcia (jeśli właściwy), zasoby graficzne, zapis i odczyt ustawień pod tym samym kluczem.

## Lokalizacja głównych punktów kodu

- `MainActivity.kt`: `documentDisplayOptions` (około linii 768), rozgałęzienia edytorów (około 1146–1177), kafelki ustawień (około 2924–2953), `DemoDocumentsScreen` (około 3634–4190), `CredentialAppearanceEditorEntry` i `CredentialAppearanceSettingsScreen` (około 4195–4270).
- `LegitimacyPreviewSettings.kt`: `credentialFieldOptions`, mapowanie ważności i wspólny panel podglądu.
- `DocumentBranding.kt`: przypisywanie zasobów graficznych.
- `ChurchDocuments.kt`, `FamilyEntityDocuments.kt`, `OfficialDocuments.kt`, `ProfessionalQualifications.kt` (jeśli obecny w drzewie): formularze dziedzinowe.

## Ograniczenia audytu

To audyt statyczny kodu `main`, nie test manualny wszystkich ekranów na emulatorze. Lista wskazuje brak osobnej ścieżki/klucza w aktualnym mapowaniu. Przed implementacją trzeba zweryfikować, czy dany typ ma być kartą legitymacyjną, kartą ewidencyjną, czy wyłącznie podglądem oryginalnego pliku.
