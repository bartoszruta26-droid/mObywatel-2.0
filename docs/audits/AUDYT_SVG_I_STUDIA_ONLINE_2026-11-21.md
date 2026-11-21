# Audyt biblioteki SVG i pobierania materiałów Studia Online

**Data audytu:** 2026-11-21  
**Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`  
**Audytowany punkt odniesienia:** `main`, commit `370bfa5621b059c9a6f9ab7191f7525c16e99cee`  
**Zakres dodatkowy:** przegląd zmian przedstawionych w otwartych PR #133 i #134.  
**Charakter:** audyt statyczny kodu i dostępnych zmian; bez uruchomienia aplikacji na urządzeniu i bez rzeczywistego logowania do konta Studia Online.

---

## 1. Streszczenie kierownicze

Audyt obejmuje dwie funkcje, które należy traktować jako osobne podsystemy:

1. bibliotekę zasobów SVG: wyszukiwanie źródeł, weryfikację metadanych, pobieranie, walidację, zapis, katalogowanie, grupowanie i późniejsze odtwarzanie zasobów;
2. pobieranie materiałów Studia Online: logowanie WebView, przechowywanie danych logowania, dostęp do ciasteczek sesyjnych, przekazywanie sesji do klienta HTTP, wybór trasy sieciowej, pobieranie, kontrolę odpowiedzi i zapis plików.

**Wniosek ogólny: funkcje mają znaczną część potrzebnego fundamentu, ale nie można jeszcze uznać obu przepływów za w pełni zweryfikowane i bezwarunkowo poprawne.** W szczególności:
- katalog SVG ma mechanizmy ochronne i trwały zapis, lecz klasyfikacja na podstawie nazwy pliku jest heurystyczna, a status licencji nie może sugerować zatwierdzenia prawnego;
- zmiany grupowania z PR #133 porządkują prezentację, ale trzeba sprawdzić migrację i odtworzenie starych rekordów oraz rzeczywisty widok po aktualizacji;
- mechanizm Studia Online rozdziela logowanie WebView i pobieranie natywne, ale samo istnienie ciasteczek nie dowodzi aktywnej sesji;
- ręczne przekazywanie nagłówka Cookie wymaga ograniczenia do dokładnej ścieżki pobierania przy każdym przekierowaniu;
- odpowiedź HTTP 200 nie może być automatycznie uznawana za prawidłowy plik materiału: serwer może zwrócić stronę logowania, komunikat blokady lub HTML;
- diagnostyka HTTP 403 jest użyteczna, ale nie rozwiązuje przyczyny odmowy dostępu i nie powinna próbować omijać zabezpieczeń serwisu.

**Nie stwierdzono podstaw do stwierdzenia, że rzeczywiste pobieranie materiałów po zalogowaniu działa poprawnie na koncie użytkownika.** Do takiego potwierdzenia potrzebny jest test na urządzeniu z prawidłowym kontem i rzeczywistym materiałem, a następnie kontrola zapisanych bajtów i ich formatu.

## 2. Zakres, metoda i ograniczenia

Przeanalizowano:
- aktualny punkt odniesienia `main`;
- historię ostatnich zmian widoczną w repozytorium;
- opis i zmienione pliki PR #133 (grupowanie zasobów);
- opis i zmienione pliki PR #134 (sesja Studia Online i HTTP 403);
- fragmenty implementacji biblioteki Commons SVG, magazynu zasobów, transportu HTTP i pobieracza materiałów;
- testy jednostkowe i instrumentacyjne widoczne w analizowanych zmianach.

Nie wykonano:
- kompilacji Gradle;
- testów JVM ani instrumentacyjnych;
- uruchomienia aplikacji na fizycznym urządzeniu;
- testu rzeczywistego logowania do Studia Online;
- testu pobrania konkretnego materiału z konta;
- testu zachowania serwera, Cloudflare lub innych zabezpieczeń antyautomatycznych;
- pełnej analizy wszystkich plików repozytorium i wszystkich gałęzi historycznych.

W konsekwencji dokument rozróżnia: **potwierdzone w kodzie**, **ryzyko wymagające poprawy** i **wymagane potwierdzenie praktyczne**. Brak testu nie jest dowodem błędu, ale nie pozwala uznać funkcji za zweryfikowaną.

## 3. Stan repozytorium i granice zmian

Punktem odniesienia jest `main` na SHA `370bfa5621b059c9a6f9ab7191f7525c16e99cee`, którego opis wskazuje na integrację zmian bezpieczeństwa SVG i modułów ewidencji finansowej (PR #132).

W czasie audytu widoczne były otwarte:
- [PR #133 – Organize application assets into graphic resource groups](https://github.com/bartoszruta26-droid/mObywatel-2.0/pull/133);
- [PR #134 – Fix Studia Online login session handoff and HTTP 403 diagnostics](https://github.com/bartoszruta26-droid/mObywatel-2.0/pull/134).

Zmiany z otwartych PR nie są częścią `main`, dopóki nie zostaną scalone. Ich poprawność należy oceniać oddzielnie od stanu stabilnej gałęzi. Sam opis PR i deklaracja testów nie stanowią dowodu zaliczenia CI.

---

# CZĘŚĆ I. BIBLIOTEKA SVG

## 4. Architektura przepływu SVG

Docelowy przepływ powinien wyglądać następująco:

1. użytkownik wybiera kategorię lub wpisuje frazę;
2. aplikacja pobiera wyniki wyszukiwania i prezentuje metadane kandydatów;
3. użytkownik wybiera konkretny plik;
4. aplikacja pobiera wyłącznie wybrany zasób;
5. transport sprawdza trasę, odpowiedź HTTP i limity transferu;
6. aplikacja sprawdza zawartość pliku, a nie tylko rozszerzenie lub MIME;
7. walidator SVG odrzuca niebezpieczną strukturę;
8. obliczana jest suma SHA-256;
9. zapis następuje w magazynie zasobów wraz z metadanymi źródła i praw;
10. katalog prezentuje zasób w grupie i podgrupie;
11. po ponownym uruchomieniu aplikacji indeks i plik są odtwarzane;
12. użytkownik może przypisać zasób do właściwego dokumentu lub elementu wyglądu.

Ważne jest zachowanie rozdziału między **odkryciem**, **pobraniem**, **bezpiecznym zapisaniem**, **klasyfikacją** i **uprawnieniem do użycia**. Są to różne stany, nie jeden status „pobrano”.

## 5. Wyszukiwanie i odkrywanie zasobów

W analizowanym kodzie Commons:
- stosowane są zapytania ukierunkowane na herby, godła, flagi, pieczęcie, logotypy i organizacje;
- najpierw wykonywane jest wyszukiwanie z filtrem MIME SVG, a następnie zapytanie zapasowe;
- wyniki są ograniczane do przestrzeni plików i nazw z rozszerzeniem SVG;
- metadane są weryfikowane przez API Commons;
- pobieranie jest ograniczane przez limit wyników i kolejkę;
- żądania są celowo spowalniane, a część błędów przechodzi przez ponowienia.

**Ocena:** fundament jest sensowny. Szczególnie właściwe jest rozdzielenie wyszukiwania metadanych od pobrania zawartości pliku oraz ograniczenie liczby żądań.

### 5.1. Ograniczenia wyszukiwania

Wyszukiwanie oparte na stałej liście fraz nie gwarantuje kompletności katalogu. Może nie znaleźć pliku, jeżeli:
- tytuł używa innego języka;
- nazwa instytucji nie zawiera typowych słów „logo”, „emblem”, „flag” lub „coat of arms”;
- plik ma nazwę techniczną lub historyczną;
- wynik nie mieści się w ograniczeniu liczby wyników API.

Nie należy przedstawiać liczby znalezionych rekordów jako kompletnej liczby istniejących herbów, flag lub logotypów.

### 5.2. Wznawianie i kolejka

W kodzie istnieją pliki stanu kolejki, raport pobierania, diagnostyka, obsługa częściowego pliku, kontrola odpowiedzi 206 i sprawdzanie sumy SHA-256, gdy znana suma oczekiwana jest dostępna.

Należy potwierdzić testami:
- przerwanie aplikacji w trakcie transferu;
- ponowne uruchomienie i wznowienie;
- odpowiedź 200 zamiast 206 dla żądania zakresowego;
- błędny lub niezgodny Content-Range;
- niezgodną długość pliku;
- niezgodną sumę SHA-256;
- brak miejsca na dysku;
- uszkodzony plik stanu kolejki;
- duplikat tego samego pliku pod inną nazwą lub adresem.

**Uwaga:** mechanizm wznawiania powinien odrzucać częściowy plik, jeśli serwer nie potwierdzi poprawnego zakresu bajtów. Nie wolno dopisywać pełnej odpowiedzi 200 do istniejącego pliku częściowego.

## 6. Walidacja bezpieczeństwa SVG

W analizowanym przepływie pobrany plik jest sprawdzany przez wspólny walidator SVG przed przekazaniem do magazynu. Występuje limit 12 MB, sprawdzanie sumy SHA-256, a zapis danych magazynu korzysta z Android Keystore i AES-GCM.

To dobry kierunek, ale testy powinny obejmować co najmniej:
- deklarację XML i różne warianty nagłówka;
- MIME `image/svg+xml` oraz MIME ogólny, gdy zawartość jest rzeczywiście SVG;
- brak lub błędną deklarację XML;
- zewnętrzne encje i DTD;
- skrypty, elementy `foreignObject`, odwołania zewnętrzne i niebezpieczne atrybuty;
- zagnieżdżone odwołania do plików;
- nadmierną głębokość i liczbę elementów;
- nieprawidłowe kodowanie;
- plik HTML podszywający się pod SVG;
- plik z poprawnym rozszerzeniem, ale niepoprawną treścią.

Walidacja nie powinna opierać się wyłącznie na rozszerzeniu, MIME ani deklaracji XML. Powinna sprawdzać strukturę dokumentu i odrzucać aktywną lub zewnętrzną zawartość zgodnie z przyjętą polityką bezpieczeństwa.

**Ocena:** w kodzie istnieje centralny punkt walidacji, ale bez wykonania testów bezpieczeństwa nie można potwierdzić kompletności reguł walidatora.

## 7. Metadane prawne i licencje — istotny problem semantyczny

Kod zapisuje metadane źródła, stronę pliku, URL pobrania, autora, licencję, adres licencji, ograniczenia i SHA-256. Jednocześnie komentarz wskazuje, że na etapie odkrywania nie jest wykonywana ocena ani selekcja licencji.

W niektórych zapisach występuje status:
`open-license-metadata-found; trademark-and-use-review-required`.

To sformułowanie jest zbyt mocne, jeżeli kod nie sprawdza, czy pole licencji jest rzeczywiście wypełnione i czy wskazana licencja dopuszcza dany sposób użycia. Samo pobranie metadanych nie jest równoznaczne z odnalezieniem otwartej licencji.

**Zalecenie blokujące:** rozdzielić co najmniej następujące stany:
- `metadata-collected` – zebrano metadane;
- `license-missing` – brak informacji o licencji;
- `license-identified-review-required` – licencja została rozpoznana, ale wymaga oceny warunków;
- `license-verified` – warunki licencji zostały sprawdzone;
- `permission-required` – wymagane odrębne zezwolenie;
- `usage-restricted` – użycie ograniczone lub niedozwolone w danym celu.

Pole `permittedKinds` pozostające puste jest właściwym zabezpieczeniem domyślnym. Nie należy automatycznie dopuszczać użycia herbu, godła, logotypu ani znaku organizacji tylko dlatego, że plik jest dostępny do pobrania.

## 8. Katalogowanie i grupowanie zasobów

W PR #133 wprowadzono funkcje:
- `documentBrandAssetParentGroup(kind)`;
- `documentBrandAssetSubgroup(kind)`;
- dodatkowe rodzaje zasobów, m.in. tło wektorowe i graficzne;
- normalizację dawnych nazw kategorii;
- grupowanie listy zasobów według grup nadrzędnych i podgrup;
- filtrowanie po grupie nadrzędnej;
- testy klasyfikacji i grupowania.

Docelowa hierarchia:
- **Herby / godła / logo** → Herby, Godła, Logo;
- **Flagi** → Flagi;
- **Tła** → Tła wektorowe, Tła graficzne;
- **Ikony** → Ikony;
- **Pozostałe zasoby** → pozostałe rodzaje.

**Ocena PR #133:** kierunek jest prawidłowy i odpowiada potrzebie czytelnego katalogu. Wykonane zmiany nie dowodzą jednak jeszcze, że wszystkie historyczne rekordy i wszystkie ścieżki importu są poprawnie sklasyfikowane.

### 8.1. Ryzyka klasyfikacji

Klasyfikacja Commons na podstawie fragmentów nazwy pliku jest heurystyczna. Przykładowo:
- słowo „eagle” może oznaczać godło, ilustrację lub logo;
- słowo „seal” może oznaczać pieczęć, ale także fokę;
- „emblem” nie zawsze jest godłem państwowym;
- nazwa organizacji nie zawsze zawiera słowo „logo”.

Klasyfikacja powinna uwzględniać metadane kategorii Commons, opis, nazwę źródła i ewentualną ręczną korektę. Użytkownik powinien mieć możliwość poprawienia kategorii bez ponownego pobierania pliku.

### 8.2. Wsteczna zgodność

Należy sprawdzić, czy:
- stare rekordy z kategorią `Logo lub emblemat` trafiają do Logo;
- `Znak instytucji publicznej` jest normalizowany do Logo;
- dawne kategorie tła nie są gubione;
- stare pliki zachowują identyfikatory, przypisania i metadane;
- filtr „Wszystkie” pokazuje każdy rekord;
- filtr nadrzędny pokazuje rekordy wszystkich podgrup;
- licznik grupy odpowiada liczbie widocznych rekordów;
- rozwijanie i zwijanie grupy nie powoduje utraty stanu lub ukrycia plików;
- katalog pozostaje poprawny po aktualizacji aplikacji.

W PR #133 testy obejmują wybrane kategorie, ale należy dodać testy migracji i odtworzenia pełnego zestawu historycznych kategorii.

## 9. Magazyn plików SVG

W `DocumentBrandStore` widoczne są:
- zapis plików w katalogu aplikacji;
- indeks zasobów i indeks przypisań;
- zapis atomowy przez `AtomicFile`;
- szyfrowanie AES-GCM z kluczem w Android Keystore;
- limit rozmiaru;
- normalizacja kategorii;
- identyfikatory zasobów i podgląd.

To mocna podstawa. Do pełnego odbioru potrzebne są testy:
- zapis → odczyt → restart → odczyt;
- zapis → zmiana kategorii → restart;
- przypisanie do dokumentu → restart;
- usunięcie pliku i rekordu indeksu;
- uszkodzenie indeksu;
- brak klucza Keystore lub unieważnienie klucza;
- przerwanie zapisu;
- duplikat identyfikatora;
- import ponownie tego samego pliku;
- import pliku o identycznej zawartości, ale innym tytule.

Warto dodać identyfikację zawartości przez SHA-256 i świadomą politykę duplikatów, niezależną od identyfikatora pochodzącego z tytułu Commons.

## 10. Wnioski dla SVG

**Status: częściowo poprawnie zaimplementowane; wymagane poprawki i testy odbiorcze.**

Elementy pozytywne:
- oddzielono wyszukiwanie od pobierania;
- weryfikowane są metadane plików;
- istnieje limit wielkości i mechanizm kontroli transferu;
- używany jest walidator SVG;
- występują sumy SHA-256;
- magazyn używa szyfrowania i zapisu atomowego;
- PR #133 wprowadza czytelniejszą hierarchię katalogu.

Elementy do poprawy:
1. status licencji nie może sugerować potwierdzenia otwartej licencji, jeżeli nie przeprowadzono jej weryfikacji;
2. klasyfikacja nazw plików musi być traktowana jako propozycja, nie pewnik;
3. potrzebne są testy odtworzenia i migracji starych rekordów;
4. należy potwierdzić zachowanie filtrów, liczników i rozwijania grup na emulatorze;
5. należy przetestować przerwane pobieranie, wznowienie, błędny zakres i niezgodność sum;
6. należy potwierdzić, że każdy kanał importu przechodzi przez tę samą walidację bezpieczeństwa.

---

# CZĘŚĆ II. STUDIA ONLINE

## 11. Architektura logowania i pobierania

Przepływ składa się z:
- magazynu danych logowania;
- WebView otwierającego oficjalną stronę HTTPS;
- systemowego `CookieManager`;
- natywnego klienta pobierania;
- transportu przez wybraną trasę Guardian Tor, Orbot lub połączenie bezpośrednie;
- katalogu identyfikatorów materiałów i zapisu do folderu wskazanego przez użytkownika.

To rozdzielenie jest uzasadnione: WebView obsługuje interaktywny proces logowania, a natywny klient może kontrolować transfer, postęp, przerwanie i zapis pliku.

## 12. Magazyn danych logowania

W analizowanym kodzie magazyn danych logowania używa:
- Android Keystore;
- klucza AES o długości 256 bitów;
- trybu GCM;
- losowego wektora IV;
- szyfrowania danych przed zapisem w SharedPreferences;
- limitu rozmiaru pól.

**Ocena:** zastosowany kierunek ochrony danych jest prawidłowy. Nie należy jednak utożsamiać szyfrowania zapisanych danych z pełnym bezpieczeństwem procesu logowania.

Wymagane kontrole:
- brak zapisu hasła w logach;
- brak wyświetlania hasła w diagnostyce;
- czyszczenie tablic znaków i buforów po użyciu;
- obsługa utraty lub unieważnienia klucza Keystore;
- bezpieczne zachowanie po zmianie blokady ekranu i odtworzeniu kopii aplikacji;
- brak automatycznego logowania, jeśli serwis zmienił formularz lub warunki logowania;
- jawna zgoda użytkownika na zapis danych.

## 13. WebView i domena logowania

WebView powinien:
- dopuszczać wyłącznie oficjalną domenę i wymagane zasoby logowania;
- nie dopuszczać nawigacji do obcych domen z zachowaniem sesji;
- nie wyświetlać ani nie logować wartości ciasteczek;
- rozróżniać błąd ładowania strony od błędu logowania;
- nie uznawać samego załadowania strony za potwierdzenie sesji.

PR #134 poprawia komunikat, aby jasno wskazywał, że załadowanie strony nie potwierdza logowania. Dodaje też obsługę błędów ładowania i błędów HTTP dla głównej ramki WebView.

**Ocena:** poprawa diagnostyczna jest zasadna. Trzeba jednak pamiętać, że ograniczenie nawigacji w WebView musi uwzględniać wymagane zewnętrzne systemy uwierzytelnienia, jeżeli oficjalny serwis z nich korzysta. Nie wolno rozszerzać listy dozwolonych domen bez weryfikacji.

## 14. Ciasteczka sesyjne — krytyczny obszar

PR #134 wprowadza wspólny obiekt `StudiaOnlineSessionCookies`, który odczytuje ciasteczka z `CookieManager` dla adresu:
`https://studia-online.pl//pobierz-material/`.

To ogranicza rozbieżność między WebView i downloaderem. Samo odczytanie ciasteczek nie potwierdza jednak:
- że sesja jest aktywna;
- że konto ma dostęp do konkretnego materiału;
- że ciasteczko nie wygasło po stronie serwera;
- że serwis zaakceptuje żądanie z natywnego klienta;
- że żądanie z wybranej trasy sieciowej zostanie zaakceptowane.

Dlatego liczba ciasteczek powinna być prezentowana wyłącznie jako informacja techniczna. Potwierdzenie logowania powinno następować przez odpowiedź serwera na bezpieczne żądanie do zasobu, do którego użytkownik ma uprawniony dostęp.

### 14.1. Ograniczenie nagłówka Cookie przy przekierowaniach

W aktualnym transporcie natywnym nagłówek Cookie jest dodawany, gdy host URL jest równy `studia-online.pl`. Klient ręcznie obsługuje przekierowania. W takim modelu po przekierowaniu na inną ścieżkę tej samej domeny warunek hosta nadal może być spełniony.

To oznacza, że ograniczenie `Path` zastosowane przez CookieManager podczas pierwotnego pobrania ciasteczek nie musi być zachowane po ręcznym przekazaniu całego nagłówka Cookie do kolejnego żądania.

**Zalecenie blokujące:** przy każdym żądaniu, również po przekierowaniu, ponownie sprawdzać dokładny host, schemat i ścieżkę. Najbezpieczniej pobierać ciasteczka z `CookieManager.getCookie(currentUrl)` dla bieżącego adresu, zamiast bezwarunkowo przenosić pierwotny nagłówek Cookie. Przekierowanie poza zatwierdzoną domenę powinno nigdy nie otrzymać ciasteczek sesyjnych. Przekierowanie w obrębie domeny, ale poza dozwoloną ścieżkę pobierania, również nie powinno otrzymać ciasteczek, chyba że serwis wymaga innego, jawnie zatwierdzonego zakresu.

Należy dodać testy:
- przekierowanie do obcej domeny;
- przekierowanie do subdomeny;
- przekierowanie do innej ścieżki głównej tej samej domeny;
- przekierowanie do ścieżki pobierania;
- ciasteczko z Path=/;
- ciasteczko z Path=/pobierz-material/;
- ciasteczko z Path=//pobierz-material/;
- ciasteczko Secure;
- ciasteczko wygasłe.

## 15. Adres ścieżki pobierania

Kod zachowuje adres:
`https://studia-online.pl//pobierz-material/`.

Nie należy go samodzielnie zmieniać bez potwierdzenia, jaki adres rzeczywiście obsługuje serwis. Podwójny ukośnik może być celowym historycznym zachowaniem aplikacji albo błędem ścieżki; wymaga testu na oficjalnym serwisie.

Test powinien potwierdzić:
- status odpowiedzi;
- końcowy URL po przekierowaniach;
- typ treści;
- zachowanie ciasteczek;
- czy serwer zwraca plik, czy stronę HTML;
- czy ten sam adres działa po zalogowaniu w WebView.

## 16. Transport HTTP i wybór trasy

Transport:
- używa HTTPS;
- wyłącza automatyczne przekierowania i obsługuje je ręcznie;
- ma limit liczby przekierowań;
- ma limit wielkości odpowiedzi;
- kontroluje czas połączenia i odczytu;
- oddziela klienta bezpośredniego od klienta SOCKS;
- nie powinien przekazywać ciasteczek do obcych hostów.

To dobry fundament techniczny. Wybór Tor/Orbot/DIRECT musi jednak pozostać jawny dla użytkownika. Nie wolno opisywać trybu DIRECT jako Tor ani twierdzić, że Tor został potwierdzony tylko dlatego, że wybrano taką opcję. Potwierdzenie wymaga rzeczywistego testu wyjścia przez węzeł Tor.

### 16.1. Różnica trasy WebView i downloadera

WebView korzysta z systemowej trasy sieciowej Androida, a pobieranie może używać Guardian Tor, Orbot lub połączenia bezpośredniego. Serwis może uzależniać sesję lub ochronę antyautomatyczną od adresu IP, reputacji trasy, nagłówków, JavaScript, tokenów CSRF i zachowania przeglądarki.

W efekcie poprawne logowanie w WebView nie gwarantuje, że natywny klient pobierania zostanie zaakceptowany. HTTP 403 może być skutkiem:
- wygasłej sesji;
- braku uprawnienia do materiału;
- błędnego identyfikatora;
- braku wymaganego tokenu;
- ograniczeń antyautomatycznych;
- odmiennej trasy sieciowej;
- odpowiedzi ochronnej serwera.

Nie należy próbować obchodzić zabezpieczeń serwisu. Prawidłowym działaniem jest diagnostyka, ograniczenie częstotliwości żądań i korzystanie z oficjalnej ścieżki dostępu.

## 17. Diagnostyka HTTP 403

PR #134 dodaje metadane:
- Server;
- CF-RAY;
- Content-Type.

Wartości te są przydatne do rozpoznania, czy odpowiedź mogła pochodzić z warstwy ochronnej, oraz do przekazania identyfikatora administratorowi serwisu. Nie należy logować wartości ciasteczek, haseł, tokenów CSRF ani treści prywatnych odpowiedzi.

**Ocena:** zmiana jest użyteczna, ale nie usuwa błędu 403. Powinna być uzupełniona o:
- kod statusu;
- końcowy URL bez parametrów zawierających sekrety;
- metodę i typ żądania;
- wybrany tryb sieciowy;
- typ odpowiedzi;
- długość odpowiedzi;
- identyfikator materiału;
- znacznik czasu;
- bezpieczny identyfikator diagnostyczny.

## 18. Walidacja pobranego materiału — wymaganie blokujące

W analizowanym przepływie downloader pobiera treść i zapisuje ją do wybranego folderu. Sam status 2xx i zapis bajtów nie wystarczają do potwierdzenia, że materiał jest prawidłowym dokumentem.

Serwer może zwrócić:
- PDF;
- inny prawidłowy format;
- HTML strony logowania;
- HTML błędu lub blokady;
- pustą odpowiedź;
- plik niekompletny;
- plik uszkodzony.

**Zalecenie blokujące:** przed zapisaniem jako ukończonego materiału sprawdzać typ treści, sygnaturę pliku i minimalną poprawność formatu. Dla PDF sprawdzać sygnaturę `%PDF-`, a w miarę możliwości także możliwość odczytu struktury dokumentu. Odpowiedź HTML nie może trafić do katalogu jako plik materiału tylko dlatego, że serwer zwrócił 200.

Nazwa pliku powinna być oparta na rzeczywistym typie i bezpiecznych metadanych, np. numerze materiału i rozszerzeniu wynikającym z walidacji. Rozszerzenie `.bin` utrudnia użytkownikowi rozpoznanie i otwieranie dokumentów.

## 19. Zakres numerów i kompletność pobierania

Katalog materiałów generuje adresy na podstawie jawnego zakresu numerów. To jest mechanizm pobierania znanych identyfikatorów, a nie automatyczny katalog wszystkich materiałów przypisanych do konta.

Nie można zakładać, że:
- wszystkie numery w zakresie istnieją;
- numery są ciągłe;
- każdy materiał jest dostępny dla danego konta;
- brak pliku oznacza błąd aplikacji;
- sukces pobrania oznacza, że plik jest właściwym materiałem.

Docelowo lepszy będzie katalog oparty na oficjalnej liście materiałów dostępnej po zalogowaniu, o ile serwis udostępnia ją w sposób zgodny z regulaminem. Jeśli nie, zakres numerów powinien pozostać jawny, z czytelnym raportem brakujących pozycji.

## 20. Usuwanie ciasteczek

W analizowanym ekranie znajduje się działanie `removeAllCookies`. Czyści ono ciasteczka WebView aplikacji dla wszystkich witryn, a nie tylko Studia Online.

Interfejs powinien wyraźnie informować o szerokim zakresie działania i wymagać potwierdzenia. Nie należy opisywać tej funkcji jako usunięcia wyłącznie sesji Studia Online.

---

# CZĘŚĆ III. PLAN POPRAWEK I TESTÓW

## 21. Priorytety

### P0 — przed uznaniem funkcji za poprawną

1. Ograniczyć przekazywanie ciasteczek do zatwierdzonego hosta i ścieżki przy każdym żądaniu, w tym po przekierowaniu.
2. Dodać testy przekierowań i zakresu Path ciasteczek.
3. Odrzucać odpowiedzi HTML i strony logowania udające pobrany plik.
4. Walidować format pliku przed oznaczeniem pobrania jako sukces.
5. Poprawić semantykę statusu licencji SVG, aby brak weryfikacji nie był opisywany jako znalezienie otwartej licencji.
6. Potwierdzić, że każdy import SVG przechodzi przez walidator.

### P1 — przed scaleniem zmian katalogu

1. Dodać testy migracji starych kategorii.
2. Dodać testy trwałości indeksu i przypisań po restarcie.
3. Sprawdzić wszystkie grupy i podgrupy na emulatorze.
4. Dodać ręczną korektę kategorii zasobu.
5. Rozszerzyć klasyfikację Commons o metadane kategorii i opis, nie tylko nazwę.
6. Zweryfikować, że licznik i filtr rodzica obejmują wszystkie podgrupy.

### P2 — poprawa ergonomii i diagnostyki

1. Zapisywać prawidłowe rozszerzenia plików.
2. Uzupełnić raport pobierania o końcowy URL bez sekretów, Content-Type, rozmiar i status walidacji.
3. Dodać eksport diagnostyki bez haseł i ciasteczek.
4. Dodać czytelny raport: pobrano, już istnieje, brak dostępu, nie znaleziono, odpowiedź nie jest plikiem, błąd walidacji.
5. Ułatwić wznowienie kolejki po restarcie aplikacji.

## 22. Macierz testów odbiorczych

| Obszar | Test | Oczekiwany wynik |
|---|---|---|
| SVG | Pobranie poprawnego SVG | Plik przechodzi walidację, otrzymuje SHA-256 i zapis |
| SVG | HTML z rozszerzeniem SVG | Odrzucony |
| SVG | SVG z aktywną zawartością | Odrzucony zgodnie z polityką |
| SVG | Plik powyżej limitu | Odrzucony bez zapisu do biblioteki |
| SVG | Przerwany transfer | Możliwe bezpieczne wznowienie albo odrzucenie części |
| SVG | Błędna suma SHA-256 | Odrzucony |
| SVG | Duplikat pliku | Brak niekontrolowanego duplikowania |
| SVG | Import dawnej kategorii | Poprawna grupa i podgrupa |
| SVG | Restart aplikacji | Wszystkie rekordy i przypisania odtworzone |
| SVG | Brak licencji | Status „brak licencji”, bez dopuszczenia użycia |
| Studia Online | Logowanie ręczne | WebView pozostaje w oficjalnej domenie |
| Studia Online | Brak ciasteczek | Czytelna informacja, brak próby udawania aktywnej sesji |
| Studia Online | Ciasteczko z właściwym Path | Dostępne dla właściwego żądania |
| Studia Online | Ciasteczko wygasłe | Serwer odmawia; aplikacja nie zgłasza sukcesu |
| Studia Online | Przekierowanie do obcej domeny | Ciasteczka nie są wysyłane |
| Studia Online | Przekierowanie do innej ścieżki | Ciasteczka nie są wysyłane poza zatwierdzony zakres |
| Studia Online | HTTP 403 | Bezpieczna diagnostyka, bez automatycznego obchodzenia ochrony |
| Studia Online | HTTP 200 + HTML | Plik odrzucony jako nieprawidłowy materiał |
| Studia Online | PDF | Sygnatura i format potwierdzone przed zapisem |
| Studia Online | Brak miejsca | Błąd zapisu, brak fałszywego sukcesu |
| Studia Online | Tryb Tor | Rzeczywiste wyjście Tor potwierdzone testem |
| Studia Online | Tryb DIRECT | Połączenie jawnie bezpośrednie, bez deklaracji Tor |

## 23. Kryteria uznania prac za zakończone

### Biblioteka SVG

- wyszukiwanie i pobieranie działają dla kilku różnych źródeł;
- wszystkie pobrane pliki przechodzą walidację;
- pliki mają sumy SHA-256;
- metadane źródła i licencji są zachowane;
- niezweryfikowane licencje nie są przedstawiane jako zatwierdzone;
- grupy i podgrupy są poprawne dla nowych i starych rekordów;
- filtry i liczniki działają;
- dane przetrwają restart aplikacji;
- testy JVM, instrumentacyjne i kompilacja przechodzą dla dokładnego SHA.

### Studia Online

- logowanie odbywa się na oficjalnej stronie;
- sesja jest sprawdzana odpowiedzią serwera, a nie liczbą ciasteczek;
- ciasteczka są przekazywane wyłącznie w zatwierdzonym zakresie;
- przekierowania nie rozszerzają zakresu przekazania sesji;
- 403 jest diagnozowane bez ujawniania sekretów;
- odpowiedź HTML nie jest zapisywana jako materiał;
- prawidłowy PDF lub inny obsługiwany format zostaje rozpoznany i zapisany;
- raport pokazuje poprawne sukcesy i błędy;
- test rzeczywisty na koncie użytkownika przechodzi dla materiału, do którego konto ma uprawnienie.

## 24. Rekomendowana kolejność dalszych prac

1. Najpierw zabezpieczyć przekazywanie ciasteczek i walidację odpowiedzi materiału.
2. Następnie doprowadzić katalog SVG do pełnej zgodności kategorii i statusów licencyjnych.
3. Uruchomić testy JVM, instrumentacyjne i kompilację dla aktualnych gałęzi.
4. Wykonać testy ręczne na urządzeniu: logowanie, pobranie jednego materiału, pobranie kilku materiałów, odpowiedź 403, przerwanie i wznowienie.
5. Po zielonych testach sprawdzić kompatybilność obu gałęzi z aktualnym `main`.
6. Dopiero potem scalać PR-y i budować APK z potwierdzonego commitu.

## 25. Konkluzja

Oba podsystemy są rozwinięte, ale wymagają domknięcia jakościowego.

**SVG:** architektura pobierania, walidacji i magazynu jest obiecująca. Najważniejsze luki dotyczą semantyki licencji, jakości klasyfikacji i testów migracji oraz odtwarzania katalogu.

**Studia Online:** szyfrowanie danych logowania, WebView, natywny downloader i diagnostyka 403 tworzą właściwy szkielet. Najważniejsze luki dotyczą zakresu ciasteczek przy przekierowaniach oraz walidacji rzeczywistego formatu pobranych plików. Nie ma podstaw, aby na podstawie samych testów ciasteczek uznać logowanie i pobieranie za potwierdzone.

**Status audytu: częściowo pozytywny, z poprawkami P0 przed końcowym odbiorem.**  
Nie wprowadzono w ramach tego dokumentu zmian w kodzie aplikacji. Audyt jest dokumentem oceny i planem dalszych prac, a nie potwierdzeniem wykonania testów na koncie lub urządzeniu.
