<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Inwentarz grafiki, ikon i symboli

## Cel

Dokument opisuje stan zasobów graficznych Androida oraz docelowy zakres biblioteki wizualnej. Prototyp ma korzystać ze spójnego, czytelnego układu usług publicznych, ale musi pozostawać jednoznacznie niezależnym prototypem. Nie może sugerować oficjalnego pochodzenia, uwierzytelnienia ani mocy prawnej dokumentu, której aplikacja nie zapewnia.

## Zasoby istniejące w Android UI

Ekran główny odwołuje się do następujących zasobów VectorDrawable:

| Zasób | Zastosowanie |
|---|---|
| `mob_document_check` | Dokumenty |
| `mob_abroad` | Polak za granicą |
| `mob_payments` | Płatności |
| `mob_health` | Zdrowie |
| `mob_transport` | Transport |
| `mob_penalties` | Mandaty / ewidencja informacji |
| `mob_pesel_check` | Sprawdź PESEL — ekran informacyjny |
| `mob_pesel_lock` | Zastrzeż PESEL — ekran informacyjny |
| `mob_ticket` | Bilety |
| `mob_cyber_safety` | Bezpieczeństwo |
| `mob_environment` | Środowisko |

Dokumentacja pochodzenia i oznaczenia licencyjnego tych zasobów znajduje się w `UI-DESIGN-NOTES.md`. Przed kopiowaniem, modyfikacją lub publikacją należy respektować wskazane tam warunki licencji.

## Robocze założenie licencyjne na etap alfa

Na polecenie użytkownika przyjmuje się tymczasowo, że projekt może korzystać z godła państwowego, pieczęci Fundacji oraz pozostałych grafik mObywatela pozyskanych z oficjalnych stron rządowych. Ręczna kontrola licencji i warunków użycia zostaje odłożona na późniejszy etap; nie blokuje to implementacji alfa. W miarę możliwości każdy plik pozostaje odrębnym zasobem, z zapisanym źródłem i możliwością wymiany. Jest to założenie robocze, a nie wynik audytu prawnego. Godło państwowe musi zachować oficjalny wzór i właściwe ograniczenia ustawowe.

## Elementy istniejące, ale tymczasowe

- Znak aplikacji jest obecnie kompozycją ikony globu Material Icons. To tymczasowy znak prototypu, nie gotowe logo.
- Ustawienia, zaświadczenia i pozostałe używają ogólnych ikon Material Icons.
- Podgląd legitymacji kursanta korzysta z ogólnej ikony dokumentu jako zastępczego znaku wystawcy.
- Dokument rodowy ma odrębną, neutralną ikonę ewidencji prywatnej.

## Brakujące zasoby — lista prac

| Grupa | Wymagane zasoby | Priorytet |
|---|---|---:|
| Identyfikacja aplikacji | Autorskie logo, sygnet, wariant jasny/ciemny, ikona launchera | Wysoki |
| Dowód osobisty | Ikona kategorii, neutralna ilustracja karty, ikona statusu | Wysoki |
| Paszport | Ikona kategorii, neutralna ilustracja dokumentu podróży | Wysoki |
| Legitymacja poselska | Ikona kategorii i neutralna ilustracja ewidencyjna | Wysoki |
| Legitymacja kursanta | Ikona kursu, instytucji i statusu uczestnictwa | Średni |
| Legitymacja studencka | Ikona uczelni, statusu studenta i ważności | Wysoki |
| Niepełnosprawność | Ikona orzeczenia, wsparcia, dostępności i legitymacji | Wysoki |
| Wystawcy | Neutralne symbole organu, uczelni, instytucji szkoleniowej i fundacji | Średni |
| Statusy | Ważny, wygasający, wygasły, unieważniony, utracony, niezweryfikowany, roboczy | Wysoki |
| Dostępność | Ilustracje i piktogramy ułatwień, czytelne warianty kontrastowe | Średni |

## Zasady katalogu godeł, herbów i znaków

Nie dodano do Android UI kompletnego katalogu godeł, herbów ani znaków urzędowych. Przed dodaniem każdego symbolu należy zapisać:

1. oficjalną nazwę i kategorię symbolu;
2. podmiot, którego dotyczy;
3. źródło pliku i datę pozyskania;
4. podstawę prawną albo licencję i warunki wykorzystania;
5. dopuszczalne warianty kolorystyczne i minimalny rozmiar;
6. ograniczenia użycia w aplikacji nieoficjalnej.

Katalog należy rozdzielić na: symbole państwowe, znaki administracji rządowej, herby samorządowe, znaki instytucji publicznych, znaki uczelni, znaki organizacji społecznych oraz autorskie symbole prototypu. Nie wolno tworzyć własnych przeróbek godła lub herbu, które mogłyby być mylone z oficjalnym wzorem.

## Zasady użycia w UI

- Ikona usługi jest elementem nawigacji, nie dowodem ani zabezpieczeniem dokumentu.
- Ilustracje kart w prototypie muszą mieć własną identyfikację i oznaczenie demonstracyjne.
- Nie odtwarzać kodów QR, kodów kreskowych, MRZ, hologramów, mikrodruków ani innych elementów zabezpieczających dokumenty publiczne.
- Nie używać godła ani herbu jako dekoracji karty prywatnej bez ustalonej podstawy prawnej i uprawnienia.
- Ikony statusu muszą mieć opis tekstowy; kolor nie może być jedynym nośnikiem znaczenia.
- Zasoby powinny mieć warianty dla jasnego i ciemnego motywu oraz skalowania.
- Grafiki powinny być wektorowe, gdzie to możliwe, z opisami dostępności i licencją.

## Stan realizacji

To jest inwentarz i plan zasobów, a nie potwierdzenie, że brakujące grafiki zostały już wykonane. Funkcjonalna alfa nadal wymaga osobnego projektu autorskiego logo oraz biblioteki ilustracji dokumentów. Nie uruchomiono kompilacji ani generowania APK.


## Dodatkowe zasoby znalezione w katalogu Android drawable

W repozytorium znajdują się także:
- `mob_penalties.xml` — piktogram Mandaty;
- `mob_pesel_check.xml` — piktogram Sprawdź PESEL;
- `mob_pesel_lock.xml` — piktogram Zastrzeż PESEL.

Dodano dla nich kafelki usług. Ekrany informacyjne nie wykonują połączeń z rejestrami państwowymi i nie wysyłają dyspozycji zastrzeżenia PESEL.


## Zasoby przekazane przez użytkownika — identyfikacja poselska

Projekt przewiduje odrębne zasoby graficzne dla identyfikacji poselskiej użytkownika:
- logo posła zawierające godło Rzeczypospolitej Polskiej;
- wzór pieczęci posła;
- ewentualne warianty kolorystyczne i monochromatyczne, o ile zostały dostarczone jako zatwierdzone oryginały.

Pliki źródłowe nie zostały jeszcze przekazane do repozytorium. Po ich otrzymaniu należy zachować oryginalny wygląd i proporcje, zapisać źródło pliku oraz używać ich wyłącznie w kontekstach wskazanych przez użytkownika. Nie odtwarzać pieczęci na podstawie opisu ani nie generować jej zamiennika.

W interfejsie należy odróżnić:
- prezentację znaku identyfikacyjnego posła;
- informację ewidencyjną o pieczęci;
- rzeczywiste uwierzytelnianie dokumentu, którego sama grafika w aplikacji nie zapewnia.

Zasób godła państwowego powinien pozostać odrębny od autorskiego logo i być używany w zgodnym z prawem, niezmienionym wzorze.


## Biblioteka heraldyczna i przypisania znaków do dokumentów

Dodano katalog 14 wbudowanych ilustracji SVG w:
`obywatel-android/app/src/main/assets/heraldry/`.

Katalog obejmuje:
- herb Ciechomskich — wąż jedzący jabłko;
- dwa wzory herbów rodowych;
- wzory biskupi, arcybiskupi, kardynalski, opacki i rycerski;
- znaki medyczny, naukowy, fundacyjny, misyjny, samorządowy i zasługi.

Są to autorskie ilustracje koncepcyjne, a nie urzędowe godła ani potwierdzone historyczne odwzorowania herbów. W szczególności zgodność herbu Ciechomskich z historycznym wzorem wymaga źródłowej weryfikacji.

### Przypisywanie do dokumentów

Ekran **Więcej → Ustawienia → Zasoby identyfikacji dokumentów** pozwala:
- importować własne pliki SVG, PNG, JPEG i WebP;
- wybrać rodzaj zasobu: logo, godło, herb, flaga, pieczęć i inne;
- przypisać wybrany zasób do konkretnego typu dokumentu lub grupy godności;
- zastąpić wcześniejsze przypisanie dla tego samego dokumentu i rodzaju znaku;
- przejrzeć i usunąć własne zasoby.

Przypisania są przechowywane w zaszyfrowanym indeksie lokalnym. Wbudowane SVG są zasobami aplikacji i nie można ich usunąć z poziomu ekranu.

### Zakres podłączenia

- legitymacja poselska — podgląd ewidencyjny;
- legitymacja uczestnika kursu;
- legitymacja ucznia / studenta;
- dokument rodowy;
- dokumenty kościelne — podgląd znaku i eksport PDF;
- karty kwalifikacji zawodowych — podgląd znaku i eksport PDF;
- eksport PDF obsługuje przypisane logo, herb/godło, pieczęć i pozostałe elementy identyfikacji.

Grupy przypisań obejmują godności biskupią, arcybiskupią, kardynalską, opacką i posługę kościelną, a także kwalifikacje medyczne, edukacyjne i terapeutyczne oraz fundacje, Kościoły, przedsiębiorstwa i stowarzyszenia.

### Zasady bezpieczeństwa i wiarygodności

- Grafika herbu, godła, pieczęci ani logo nie potwierdza sama z siebie godności, urzędu, kwalifikacji, mandatu, członkostwa ani umocowania.
- Przypisanie znaku ma charakter prezentacyjny i ewidencyjny.
- Dokumenty prywatne i wzory legitymacji pozostają jednoznacznie oznaczone jako ewidencyjne.
- Oficjalne godło RP i znaki innych instytucji muszą być wykorzystywane wyłącznie w prawidłowym wzorze i zgodnie z warunkami prawnymi.
