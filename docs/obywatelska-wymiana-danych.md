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

## Kompatybilność z rządową aplikacją mObywatel — ważne ograniczenie

OWD nie jest obecnie kompatybilny z rządową aplikacją mObywatel. Oficjalna funkcja QR służy do przepływu, w którym aplikacja mObywatel osoby okazującej dane skanuje kod wygenerowany przez osobę lub podmiot weryfikujący, a następnie przekazuje własne dane z aplikacji mObywatel. Nie jest to ogólny czytnik kodów QR ani mechanizm importowania dowolnych danych z aplikacji zewnętrznej.

W konsekwencji:
- użytkownik rządowego mObywatela nie zeskanuje kodu OWD i nie zobaczy w nim automatycznie danych o stopniu niepełnosprawności ani o mandacie poselskim;
- kod OWD jest przeznaczony dla innej instalacji obywatelskiego prototypu, która rozumie protokół OWD;
- zwykły, jawny QR lub dokument PDF może być otwarty przez ogólny czytnik / przeglądarkę, ale nie staje się przez to wpisem ani dokumentem w rządowym mObywatelu;
- nie wolno oznaczać danych wpisanych przez użytkownika lub przesłanych z prototypu jako potwierdzonych przez państwo.

Oficjalna usługa przekazywania danych działa w przeciwnym kierunku: użytkownik rządowego mObywatela przekazuje dane przechowywane w tej aplikacji do systemu podmiotu, który uzyskał zgodę Ministra Cyfryzacji, przeszedł procedurę integracji, używa certyfikatów i wymaganego zaplecza. Nie jest to interfejs, przez który zewnętrzna aplikacja może wstrzyknąć własne rekordy do mObywatela.

### Dane o niepełnosprawności i mandacie poselskim

Samo zaszyfrowanie lub podpisanie danych w OWD nie potwierdza ich prawdziwości. Dla stopnia niepełnosprawności wymagany byłby dokument lub elektroniczne poświadczenie wydane przez właściwy organ albo dostęp do właściwego, autoryzowanego źródła. Dla mandatu poselskiego potrzebne byłoby aktualne, wiarygodne poświadczenie pochodzące z właściwego źródła urzędowego, np. organu prowadzącego oficjalną ewidencję mandatu. Dane wpisane ręcznie przez użytkownika mogą być przekazane jako deklaracja, ale nie jako urzędowo zweryfikowany status.

**Wniosek implementacyjny:** OWD może przekazać te informacje do drugiej obywatelskiej aplikacji. Dla użytkownika rządowego mObywatela należy przygotować odrębny, czytelny eksport (np. PDF z danymi i informacją o źródle oraz podpisanym poświadczeniem, jeśli wystawca je zapewnia) albo uzyskać formalną integrację / oficjalną ścieżkę publikacji dokumentu. Żaden z tych wariantów nie gwarantuje importu do portfela rządowej aplikacji.


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

To są elementy protokołu i transportu, a nie jeszcze kompletny, zintegrowany przepływ ekranowy. Ekran OWD nadal generuje ofertę QR bez adresu nasłuchującego urządzenia i nadal nie uruchamia odbiornika ani skanera aparatem. Nie ma jeszcze mechanizmu odkrywania urządzenia/adresu IP i umieszczania endpointu w QR, pełnego powiązania klucza efemerycznego z cyklem życia serwera, integracji odbioru z trwałym rejestrem sesji i interfejsem zgody, ani rzeczywistej usługi przekaźnikowej. Adapter państwowy pozostaje wyłącznie kontraktem architektonicznym. Nie oznaczamy więc transferu jako ukończonego ani przetestowanego na dwóch telefonach.

Przed testem na urządzeniach trzeba jeszcze połączyć ekran z przepływem nadawcy i odbiorcy, przekazywać adres nasłuchującego urządzenia w ofercie QR, podłączyć `OwdReplayStore` w miejscu atomowego przyjęcia sesji, obsłużyć anulowanie/wygaśnięcie i wyczyścić klucze po zakończeniu. Dla transmisji poza jedną siecią lokalną potrzebny będzie zatwierdzony, utrzymywany przekaźnik HTTPS, który przechowuje wyłącznie szyfrogramy.
