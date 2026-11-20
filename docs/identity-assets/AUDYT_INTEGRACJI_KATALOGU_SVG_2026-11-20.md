# Audyt katalogu SVG — integracja na aktualnej gałęzi main

Data: 2026-11-20
Gałąź bazowa: main
SHA bazowy: cd1523d515895327ec19afa4d521b36249943c2f

## Wykonano w tej iteracji

- Odtworzono na aktualnym main moduł InstitutionalSymbolCatalog.kt.
- Dodano SvgSafetyValidator.kt jako wspólną bramkę walidacji przy pobieraniu SVG.
- Dodano testy JVM InstitutionalSymbolCatalogTest.kt.
- Dodano do assets aplikacji katalog źródłowy z 2 734 rekordami kandydackimi.
- Wyszukiwanie aplikacyjne filtruje rekordy do pozycji SVG, dla których można wyprowadzić URL z dozwolonych źródeł.
- Pobieranie odbywa się pojedynczo przez TorDownloadServiceProvider, bez awaryjnego połączenia bezpośredniego.
- Pochodzenie, rewizja źródła, ścieżka, autor, licencja, atrybucja i status ograniczeń są zachowywane w metadanych zasobu.
- Dla pobranych znaków permittedKinds pozostaje puste; samo pobranie nie przyznaje uprawnienia do użycia w dokumencie.

## Kontrole przed scaleniem

- Uruchomić Repository compliance, Android prototype build, JVM unit tests oraz Compose UI na emulatorze dla bieżącego HEAD.
- Podłączono panel InstitutionalSymbolCatalogPanel do ekranu usługi „Zasoby identyfikacji dokumentów” w MainActivity; panel biblioteki dotychczasowych zasobów pozostaje dostępny poniżej.
- Zweryfikować każdą ścieżkę pliku i URL; rekordy rastrowe i źródła bez możliwego adresu SVG pozostają w indeksie źródłowym, ale nie są wynikami wyszukiwania SVG.
- Walidator odrzuca DTD, encje, skrypty, foreignObject, animacje, atrybuty zdarzeń i zewnętrzne odwołania CSS/URI.
- Wymagany jest test rzeczywistego pobierania przez dostępny klient Tor na urządzeniu; CI emulatora nie zastępuje testu z działającym Orbot/Guardian.

## Ograniczenia prawne i danych

Indeks 2 734 pozycji jest indeksem plików i ścieżek kandydackich, nie listą 2 734 niezależnych organizacji ani katalogiem 2 734 plików dopuszczonych do redystrybucji. Licencja repozytorium nie rozstrzyga automatycznie praw do znaku towarowego ani praw do konkretnego herbu, godła, emblematu czy logo. Zachowujemy także referencje do pozycji, których status wymaga potwierdzenia.

## Status

Zmiany zapisano na osobnej gałęzi. Nie scalono do main. Nie ogłoszono gotowości APK. CI musi zostać potwierdzone dla nowego SHA gałęzi integracyjnej.
