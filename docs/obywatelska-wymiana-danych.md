# Obywatelska Wymiana Danych (OWD)

## Cel i granice

OWD jest niezależnym protokołem wymiany danych między instalacjami obywatelskiego prototypu. Nie jest częścią rządowego mObywatela, nie korzysta z rejestrów publicznych i nie potwierdza tożsamości ani autentyczności dokumentów państwowych.

Integracja z oficjalną usługą przekazywania danych mObywatela pozostaje osobnym przedsięwzięciem. Wymaga uzyskania dostępu na warunkach właściwego operatora. Protokół OWD nie może podszywać się pod oficjalny weryfikator ani oznaczać danych jako państwowo potwierdzonych.

## Model wymiany

1. Nadawca wybiera wyłącznie dane lokalne, które chce udostępnić.
2. Aplikacja przedstawia odbiorcę, cel, zakres i czas udostępnienia.
3. Dopiero po świadomym zatwierdzeniu tworzona jest oferta sesji OWD.
4. Kod QR zawiera typ protokołu, wersję, losowy identyfikator sesji, czas utworzenia i wygaśnięcia, jednorazowy nonce oraz efemeryczny klucz publiczny. Nie zawiera danych osobowych.
5. Odbiorca skanuje ofertę i widzi podsumowanie żądania. Nie otrzymuje danych przed zgodą nadawcy.
6. Obie strony uzgadniają klucz sesyjny przez ECDH na krzywej P-256; klucz jest wyprowadzany przez HKDF-SHA-256.
7. Ładunek jest szyfrowany AES-256-GCM z uwierzytelnionymi danymi dodatkowymi wiążącymi szyfrogram z identyfikatorem sesji i kategorią.
8. Sesja jest jednorazowa i wygasa po 180 sekundach.

## Kanał hybrydowy

Docelowy wybór kanału:
- bezpośredni: Wi-Fi lokalne / Wi-Fi Direct, a w ograniczonych scenariuszach Bluetooth;
- pośredniczący: serwer przekazujący wyłącznie zaszyfrowane pakiety, bez kluczy i bez jawnych danych;
- wybór automatyczny: najpierw próba kanału bezpośredniego, następnie kanał pośredniczący, jeżeli użytkownik ma łączność i zaakceptował ten wariant.

Oba kanały muszą używać tego samego identyfikatora sesji, klucza sesyjnego i reguł zgody. Serwer nie może być źródłem zaufania ani zmieniać zakresu udostępnienia. Transport wymaga dodatkowego uwierzytelnienia urządzeń i ochrony przed przekierowaniem sesji.

## Rozdzielenie źródeł danych

Każdy rekord przekazywany przez OWD musi mieć jawne pochodzenie, np.:
- `LOCAL_USER` – wpis wprowadzony przez użytkownika;
- `PEER_SHARED` – dane otrzymane od innego użytkownika;
- `ISSUER_ATTESTED` – dane potwierdzone przez konkretnego wystawcę, wraz z weryfikowalnym dowodem i identyfikacją wystawcy;
- `GOVERNMENT_SERVICE` – dane pozyskane przez oficjalną, autoryzowaną usługę.

Nie wolno przekształcać `LOCAL_USER` ani `PEER_SHARED` w dane urzędowe przez samo przesłanie QR. Dane rodzinne/rodowe pozostają oddzielną kategorią prywatnej ewidencji.

## Aktualny stan implementacji — wersja alfa

W aplikacji dodano ekran OWD w zakładce „Kod QR”: wybór wypełnionych pól dotyczących niepełnosprawności i ewidencji mandatu, określenie celu, wyraźne potwierdzenie zgody oraz krótkotrwały QR inicjujący sesję (180 sekund), który nie zawiera danych osobowych. Dotychczasowy generator vCard pozostaje osobną funkcją.

Dodano OwdIntegration.kt jako granicę architektoniczną dla przyszłej integracji państwowej: model pochodzenia danych, zakres zgody, potwierdzenie transakcji, kontrolę gotowości i interfejs adaptera. Nie ma implementacji adaptera ani połączenia z systemami administracji. Sam interfejs nie oznacza certyfikacji, dostępu do API ani zgody na przekazywanie danych.

Dodano OwdSecurity.kt z podstawowymi funkcjami:
- generowanie krótkotrwałej oferty QR bez danych osobowych;
- walidacja typu, wersji, rozmiaru, czasu ważności i parametrów oferty;
- uzgadnianie klucza ECDH P-256 i wyprowadzanie klucza HKDF-SHA-256;
- szyfrowanie i odszyfrowywanie AES-256-GCM z AAD;
- procesowy rejestr jednorazowego użycia identyfikatorów sesji.

**Przepływ użytkowy jest częściowy.** Ekran zgody i generator oferty sesji działają na poziomie interfejsu, ale kod QR inicjuje wyłącznie sesję kryptograficzną. Nie przesyła wybranych rekordów. Dodano skaner QR z walidacją oferty, bezpośredni transport TCP, ramkowanie pakietu, potwierdzenie HMAC, weryfikator podpisów wystawców i trwały rejestr zużytych sesji. Nie połączono jeszcze tych elementów w kompletny przepływ ekranowy: QR nie zawiera adresu nasłuchującego urządzenia, UI nie wymienia kluczy ani nie uruchamia odbiornika, a trwały rejestr nie jest jeszcze wywoływany przy przyjmowaniu sesji. Nie skonfigurowano serwera przekaźnikowego ani integracji z oficjalną usługą. Nie wolno przedstawiać tej wersji jako działającego transferu danych. Nie należy deklarować, że parowanie lub przesyłanie danych działa, dopóki te elementy nie zostaną wdrożone i przetestowane.

## Wymagania przed użyciem produkcyjnym

- atomowe, trwałe zużywanie identyfikatorów sesji i ochrona przed powtórzeniem po restarcie;
- wiązanie zgody z dokładnym zestawem pól, odbiorcą, celem i kanałem;
- uwierzytelnienie obu stron oraz ochrona przed atakiem pośrednika;
- bezpieczne przechowywanie kluczy prywatnych w Android Keystore;
- rotacja i usuwanie kluczy efemerycznych po zakończeniu lub anulowaniu sesji;
- ograniczenie rozmiaru, typów i liczby rekordów;
- testy kryptograficzne, testy negatywne, audyt bezpieczeństwa i testy na dwóch urządzeniach;
- polityka retencji i usuwania danych po stronie odbiorcy;
- odrębna ścieżka formalnej integracji z usługą państwową.

## Zasada nadrzędna

Kod QR inicjuje sesję, ale nie jest dokumentem, poświadczeniem ani dowodem tożsamości. Użytkownik zachowuje kontrolę nad zakresem danych, a aplikacja jasno pokazuje ich źródło i status weryfikacji.

## Kompatybilność QR z rządowym mObywatelem — stan faktyczny

Oficjalna dokumentacja opisuje dwa różne scenariusze, których nie wolno utożsamiać:

1. **Weryfikacja mDowodu przez QR** — osoba weryfikująca generuje kod, a właściciel mDowodu skanuje go w rządowym mObywatelu i zatwierdza przekazanie danych. W oficjalnym procesie dane pochodzą z rządowej aplikacji i rejestrów; samo zeskanowanie dowolnego QR przez mObywatela nie importuje zewnętrznych danych.
2. **Przekazanie offline do aplikacji mWeryfikator** — regulamin mWeryfikatora opisuje wymianę danych certyfikatu z aplikacji mObywatel do aplikacji weryfikującej, także jako serię kodów QR dla określonych kombinacji Android/iOS. Certyfikat można zweryfikować lokalnie bez aktywnego Internetu; opcjonalne potwierdzenie aktualnej ważności online wymaga łączności.

Wniosek: QR i transfer offline są rzeczywiście stosowane w ekosystemie mObywatela, ale w określonym, kontrolowanym protokole certyfikatów mObywatel/mWeryfikator. Nie jest to ogólny mechanizm importowania dowolnego dokumentu z aplikacji zewnętrznej do rządowego portfela.

### Co wdrażamy w OWD

- **OWD QR Offline**: własny, jawnie oznaczony protokół transferu pomiędzy dwiema instalacjami OWD. Może używać pojedynczego QR dla małego ładunku albo serii QR z numeracją, kontrolą kompletności i skrótem integralności dla większego ładunku. Dane są szyfrowane, a odbiorca potwierdza odbiór.
- **Eksport dokumentu do okazania**: użytkownik może wyeksportować czytelny dokument (np. PDF) i jego podpisane poświadczenie, jeśli istnieje podpis wystawcy. Można go przekazać przez QR/plik offline. Rządowy mObywatel nie ma jednak potwierdzonego mechanizmu importu takiego pliku.
- **Ścieżka mWeryfikator**: przygotowujemy odrębny moduł zgodności wyłącznie po uzyskaniu oficjalnej specyfikacji, warunków użycia i testowego zestawu certyfikatów. Nie będziemy generować własnych certyfikatów udających certyfikaty mObywatela ani naśladować jego kodów QR.
- **Adapter rządowy**: pozostaje oddzielnym adapterem, uruchamianym dopiero po formalnym dopuszczeniu podmiotu, schematów i kanału przez operatora usługi.

### Dane o niepełnosprawności i mandacie poselskim

OWD może przekazać te dane offline do drugiej instalacji OWD albo wyeksportować je do dokumentu do okazania. Status musi wskazywać, czy jest to deklaracja użytkownika, kopia dokumentu, czy poświadczenie podpisane przez właściwego wystawcę. Samo zaszyfrowanie, podpisanie przez użytkownika lub przesłanie kodem QR nie nadaje statusu urzędowego.

**Nie można obecnie zagwarantować, że rządowy mObywatel zeskanuje QR OWD i zaimportuje te informacje do swojego portfela.** Oficjalny proces QR mDowodu działa według protokołu operatora i w kierunku opisanym w dokumentacji. Dla danych zewnętrznych potrzebna jest oficjalna ścieżka przyjęcia, dopuszczony schemat lub integracja.

### Źródła oficjalne

- Ministerstwo Cyfryzacji, „Kod QR – skuteczna metoda weryfikacji mDowodu”: https://www.gov.pl/web/cyfryzacja/kod-qr--skuteczna-metoda-weryfikacji-mdowodu
- Regulamin mWeryfikatora, opis przekazania danych i serii QR: https://www.mobywatel.gov.pl/mweryfikator.regulamin.6.0.1.pdf
- Ministerstwo Cyfryzacji, „Informacje o aplikacji mObywatel” — warunki usługi przekazywania danych: https://www.gov.pl/web/cyfryzacja/informacje-o-aplikacji-mobywatel

## Docelowa integracja z rządowym mObywatelem — założenie projektowe

Projekt zakłada, że w przyszłości podmiot odpowiedzialny za aplikację uzyska wymagane zgody, certyfikaty, rejestrację zaplecza i dokumentację integracyjną. Architektura rozdziela dwa adaptery: OWD peer-to-peer dla zgodnych aplikacji oraz osobny Government mObywatel adapter realizujący wyłącznie oficjalnie dopuszczony przepływ i formaty zaakceptowane przez operatora.

Przyszła integracja nie może polegać na podszywaniu się pod aplikację rządową, imitowaniu jej QR ani obchodzeniu procesu certyfikacji. Adapter państwowy pozostaje nieaktywny, dopóki kontrola gotowości nie potwierdzi łącznie formalnej zgody, certyfikatu, rejestracji zaplecza i obsługi danego schematu danych. W alfa nie ma implementacji adaptera.

### Warunki przekazywania danych w przyszłości

- Każdy rekord zachowuje pochodzenie: LOCAL_USER, PEER_SHARED, ISSUER_ATTESTED albo GOVERNMENT_SERVICE.
- Dane o stopniu niepełnosprawności muszą rozróżniać deklarację, dane z dokumentu wystawcy i dane urzędowo potwierdzone.
- Informacja o mandacie poselskim wymaga aktualnego źródła właściwego dla mandatu; ręczny wpis lub sama legitymacja nie wystarczają.
- Zgoda określa odbiorcę, cel, dokładne pola, kanał i termin ważności. Zmiana zakresu wymaga nowej zgody.
- Aplikacja przechowuje potwierdzenie transakcji, listę zaakceptowanych pól i czas odbioru.
- Dane prywatne/rodowe są wyłączone z domyślnego zakresu.
- Operator rządowy musi dopuścić konkretny zestaw danych wprost; ogólna certyfikacja nie oznacza obsługi każdego rodzaju danych.

### Plan kolejnych etapów

1. Dokończyć skanowanie QR, wzajemne uwierzytelnienie i uzgadnianie kluczy na dwóch urządzeniach.
2. Wdrożyć transport bezpośredni i pośredniczący, automatyczny wybór kanału oraz trwałą ochronę przed ponownym użyciem sesji.
3. Zaimplementować serializację rekordów z pochodzeniem, podpisy wystawców i weryfikację dowodów.
4. Zbudować testowy adapter państwowy wyłącznie na podstawie oficjalnego środowiska testowego i specyfikacji otrzymanej po formalnej akceptacji.
5. Przeprowadzić audyt bezpieczeństwa, testy zgodności i ocenę ochrony danych przed uruchomieniem produkcyjnym.


## Aktualizacja implementacji — 9 listopada 2026

Dodano kolejne niezależne elementy warstwy protokołu:

- `OwdTransport.kt`: ramkowanie i dekodowanie zaszyfrowanego pakietu, bezpośredni transport TCP w sieci lokalnej, ograniczenia rozmiaru i czasu oczekiwania oraz jednorazowy odbiornik. Odbiornik odszyfrowuje pakiet AES-GCM, przekazuje zweryfikowany ładunek do procedury zatwierdzenia i odsyła potwierdzenie HMAC-SHA-256 związane z identyfikatorem sesji i czasem odbioru.
- `OwdAutomaticTransport`: próbuje najpierw kanału bezpośredniego, a po błędzie może wywołać skonfigurowany adapter przekaźnikowy. Adapter przekaźnikowy pozostaje punktem rozszerzenia — nie skonfigurowano serwera, adresu ani usługi przekaźnikowej.
- `OwdCredentialVerifier.kt`: weryfikacja podpisu ECDSA/SHA-256 wobec klucza wystawcy pobranego z zaufanego katalogu. Klucz dołączony do samego poświadczenia nie jest uznawany za zaufany. Dopiero poprawna weryfikacja może nadać rekordowi pochodzenie `ISSUER_ATTESTED`.
- `OwdReplayStore.kt`: trwały rejestr zużytych identyfikatorów sesji w prywatnych preferencjach aplikacji; identyfikatory są przechowywane jako skróty SHA-256.
- Manifest Androida deklaruje uprawnienie sieciowe wymagane przez gniazda TCP.

### Granice obecnego stanu

To są elementy protokołu i transportu, a nie jeszcze kompletny, zintegrowany przepływ ekranowy. Ekran OWD generuje ofertę QR i uruchamia skaner aparatem, który waliduje typ, wersję i czas ważności oferty. Oferta nadal nie zawiera adresu nasłuchującego urządzenia, a ekran nie uruchamia jeszcze odbiornika ani właściwego transferu. Nie ma jeszcze mechanizmu odkrywania urządzenia/adresu IP i umieszczania endpointu w QR, pełnego powiązania klucza efemerycznego z cyklem życia serwera, integracji odbioru z trwałym rejestrem sesji i interfejsem zgody, ani rzeczywistej usługi przekaźnikowej. Adapter państwowy pozostaje wyłącznie kontraktem architektonicznym. Nie oznaczamy więc transferu jako ukończonego ani przetestowanego na dwóch telefonach.

Przed testem na urządzeniach trzeba jeszcze połączyć ekran z przepływem nadawcy i odbiorcy, przekazywać adres nasłuchującego urządzenia w ofercie QR, podłączyć `OwdReplayStore` w miejscu atomowego przyjęcia sesji, obsłużyć anulowanie/wygaśnięcie i wyczyścić klucze po zakończeniu. Dla transmisji poza jedną siecią lokalną potrzebny będzie zatwierdzony, utrzymywany przekaźnik HTTPS, który przechowuje wyłącznie szyfrogramy.
