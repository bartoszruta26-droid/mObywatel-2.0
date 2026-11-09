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

## Aktualny stan implementacji

Dodano `OwdSecurity.kt` z podstawowymi funkcjami:
- generowanie krótkotrwałej oferty QR bez danych osobowych;
- walidacja typu, wersji, rozmiaru, czasu ważności i parametrów oferty;
- uzgadnianie klucza ECDH P-256 i wyprowadzanie klucza HKDF-SHA-256;
- szyfrowanie i odszyfrowywanie AES-256-GCM z AAD;
- procesowy rejestr jednorazowego użycia identyfikatorów sesji.

**To jest warstwa kryptograficzna, a nie ukończony przepływ użytkowy.** Nie dodano jeszcze skanera OWD, widoku zgody, wymiany kluczy po obu stronach, transportu Wi-Fi/Bluetooth, serwera przekaźnikowego, trwałego rejestru zużytych sesji ani integracji z oficjalną usługą. Nie należy deklarować, że parowanie lub przesyłanie danych działa, dopóki te elementy nie zostaną wdrożone i przetestowane.

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
