<!-- SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors -->
<!-- SPDX-License-Identifier: EUPL-1.2 -->

# Reusable Tor download service

## Purpose

The Tor networking layer is independent from resource catalogues and storage. SVG, documents,
archives, images, and future download consumers use the same transport API. The transport returns
a closeable response stream; it does not buffer or persist the complete response.

## Layers

- `TorRuntime`: lifecycle/state contract; `GuardianTorRuntime` embeds Guardian Project's Tor service.
- `TorProxyEndpoint`: loopback-only SOCKS5 endpoint. OkHttp routes destination hostnames through SOCKS so DNS resolution is performed by the Tor proxy.
- `TorHttpTransport`: HTTPS GET transport, explicit redirect handling, timeouts, response-size
  enforcement, and close/disconnect ownership.
- Consumer: validates media/content, computes hashes, and decides whether/how to persist data.

## Embedded Android runtime

The app integrates Guardian Project tor-android 0.4.9.14 from Maven Central and uses its
bound org.torproject.jni.TorService. The adapter listens for the service ON status and
publishes READY only after the first Tor circuit is established and a valid SOCKS port is
available. The runtime owns the service binding and unbinds it on stop.

The Tor daemon maintains its own private operational state/cache under the app's internal
service directory. This is distinct from downloaded response payloads: TorHttpTransport
does not persist the requested files and returns a streaming response.

The app download service remains independent from the Tor implementation. Runtime lifecycle
and real-device behaviour still require instrumentation tests before release.

## Security properties

- Only loopback SOCKS endpoints are accepted.
- Only absolute HTTPS URLs without embedded credentials are accepted.
- Redirects are followed manually and every target is revalidated as HTTPS.
- Response bodies are streamed; known and unknown-length responses are bounded.
- TLS certificate validation remains the platform default; no trust-all manager is installed.
- Callers must close `TorHttpResponse` with `use { }`.
- The service does not save files, use FIFO, or fall back to direct HTTP.

## Follow-up integration

1. Add instrumentation tests for TorService binding, bootstrap, stop/restart, and SOCKS availability.
2. Add an end-to-end test proving observed egress is a Tor exit, without logging sensitive URLs.
3. Migrate Wikimedia Commons API and SVG downloads to an injected transport.
4. Add a settings surface for AUTO / DIRECT / TOR only after runtime and fallback policy are fully tested.
   TOR mode must fail closed; AUTO fallback must be explicit and observable.


## Klienci Tor i ustawienia aplikacji

Menu Ustawienia zawiera ekran „Klienci sieci Tor”. Rejestr profili jest przechowywany w prywatnych preferencjach aplikacji i obejmuje:
- wbudowany klient Guardian Project (domyślny);
- profil lokalnego SOCKS5 Orbot (domyślnie `127.0.0.1:9050`);
- profile własne z nazwą, adresem i portem;
- wybór preferowanego profilu, aktywowanie/dezaktywowanie i usuwanie profili własnych.

Celowo dopuszczane są wyłącznie adresy loopback (`127.0.0.1`, `localhost`, `::1`). Nie ma obsługi dowolnego zdalnego publicznego SOCKS: operator takiego proxy może obserwować metadane połączeń, a sam endpoint nie dowodzi, że ruch trafia do sieci Tor. Publiczna sieć Tor jest osiągana przez uruchomionego klienta Tor, nie przez anonimowy, współdzielony adres SOCKS.

**Stan integracji:** wbudowany klient Guardian Project pozostaje implementacją runtime w tej gałęzi. Ekran i rejestr profili są już dodane, ale wybór profilu zewnętrznego (Orbot) nie jest jeszcze podłączony do fabryki runtime/transportu. Włączenie profilu w ustawieniach nie oznacza jeszcze, że żądania są przez niego kierowane. Następny etap: adapter zewnętrznego SOCKS5 z kontrolą gotowości i testem braku wycieku DNS, następnie wybór runtime na podstawie aktywnego profilu.


## Pula lokalnych portów SOCKS i rozdzielanie pobrań

Wbudowany `GuardianTorRuntime` konfiguruje jeden proces Tor z dodatkowym zestawem lokalnych portów SOCKS5:
- port podstawowy wybierany przez TorService (zwykle 9050);
- `127.0.0.1:19051` do `127.0.0.1:19054` jako dodatkowe listenery;
- dodatkowe listenery używają flag `IsolateClientAddr`, `IsolateDestAddr` i `IsolateDestPort`.

TorService udostępnia listę aktywnych listenerów przez Control Protocol (`GETINFO net/listeners/socks`). Runtime odczytuje tę listę i publikuje wyłącznie porty, które rzeczywiście nasłuchują. Jeżeli dodatkowy port nie uruchomi się, nie trafia do puli. W razie braku gotowego portu usługa kończy żądanie błędem — bez połączenia bezpośredniego.

`TorDownloadService` wybiera port dla każdego żądania w trybie round-robin. Klasyfikacja `TorDownloadKind` pozwala też wdrożyć jawne przypisania typu pliku do profilu przez `TorDownloadRouter` z polityką `PINNED_BY_KIND`. Przy przypisaniu niedostępnego profilu router odmawia pobrania zamiast po cichu użyć innego klienta.

**Granica izolacji:** wiele portów SOCKS w tym rozwiązaniu oznacza wiele izolowanych grup strumieni jednego procesu/klienta Tor, a nie kilka niezależnych demonów Tor. Nie gwarantuje to różnych adresów wyjściowych dla każdego pliku. To rozwiązanie jest lżejsze i łatwiejsze do utrzymania niż uruchamianie wielu niezależnych procesów, które wymagałyby odrębnych katalogów danych, portów kontrolnych i cykli życia.

Orbot może działać jako dodatkowy, zewnętrzny klient, jeśli zostanie skonfigurowany na innym lokalnym porcie SOCKS (np. 9150). Adapter Orbot i rzeczywiste kierowanie żądań przez niego pozostają osobnym etapem; sama pozycja w rejestrze nie jest dowodem gotowości.

Publiczne przekaźniki Tor i mosty nie są publicznymi serwerami SOCKS. Przekaźniki/mosty konfiguruje się w kliencie Tor jako elementy drogi wejściowej do sieci, a nie jako proxy pobierania. Nieznane zdalne SOCKS pozostają odrzucane przez model bezpieczeństwa.

## Integracja Orbot, test wyjścia i rotacja obwodów

- Wbudowany Guardian Project Tor jest wymaganym klientem bazowym. Runtime czeka na status pierwszego zbudowanego obwodu i udostępnia wyłącznie aktywne lokalne porty SOCKS.
- Orbot jest profilem zewnętrznym na loopback SOCKS5 (domyślny port SOCKS 9050; gdy Guardian zajmie ten port, użytkownik może skonfigurować Orbot na 9150). Samo otwarte gniazdo nie wystarcza: aplikacja wykonuje żądanie przez wskazany SOCKS do `https://check.torproject.org/api/ip` i wymaga `IsTor=true`. Profil Orbot jest pomijany, gdy pakiet `org.torproject.android` nie jest zainstalowany, port nie odpowiada albo test wyjścia nie potwierdza Tor.
- Gdy zewnętrzny SOCKS przestaje przyjmować połączenia, profil jest czasowo wyłączany z puli, a kolejne żądanie trafia do Guardian. Nie ma bezpośredniego połączenia awaryjnego. Błędy HTTP serwera docelowego nie są mylone z awarią Orbot.
- Test Orbot jest dostępny w ekranie Ustawienia → Klienci sieci Tor. Najlepiej uruchomić Orbot przed pierwszym uruchomieniem wbudowanego Guardiana, aby Orbot zajął standardowy port 9050, a Guardian dobrał wolny port automatycznie. Jeżeli Guardian już zajmuje 9050, użytkownik może ustawić Orbot na 9150 i dodać drugi lokalny profil. Aplikacja nie próbuje uruchamiać chronionej usługi VPN Orbot z pominięciem uprawnień systemowych.
- Wbudowany Guardian otrzymuje `MaxCircuitDirtiness 480`: po 8 minutach Tor nie przypina nowych strumieni do zbyt starego obwodu. Aktywne połączenie TCP pozostaje na dotychczasowym obwodzie do zakończenia. To celowe; nie przenosimy aktywnego strumienia między obwodami.
- Orbot jako osobny proces zachowuje własną konfigurację Tor i domyślną politykę rotacji (zwykle 10 minut). Aplikacja nie zmienia tej konfiguracji, ponieważ zewnętrzny interfejs Orbot nie udostępnia bezpiecznego, wspieranego kontraktu do ustawiania `MaxCircuitDirtiness` dla obcego procesu.
- Oddzielne porty SOCKS Guardian mają domyślnie odrębne grupy sesji, a flagi `IsolateClientAddr`, `IsolateDestAddr`, `IsolateDestPort` dodatkowo ograniczają współdzielenie obwodów. Rotacja 8-minutowa dotyczy nowych strumieni, nie przerywa już rozpoczętych pobrań.

## Pobieranie SVG przez Tor i wznawianie

- Zapytania API Commons i treść plików SVG przechodzą przez `TorDownloadService`; nie pozostał bezpośredni `HttpURLConnection` w ścieżce pobierania katalogu.
- Plik częściowy jest przechowywany tymczasowo w prywatnym katalogu cache aplikacji jako `.part`, wraz z ETag/Last-Modified. To umożliwia wznowienie po przerwaniu procesu; po zatwierdzeniu zasobu plik częściowy i metadane są usuwane.
- Wznowienie używa `Range` i `If-Range`; dopisywanie bajtów następuje wyłącznie po otrzymaniu prawidłowego `206 Partial Content` i zgodnego `Content-Range`. Przy braku walidatora albo niezgodnej odpowiedzi pobieranie zaczyna się od początku.
- Po pobraniu aplikacja sprawdza limit rozmiaru, strukturę XML/SVG, niedozwolone elementy i SHA-256. Suma jest zapisywana w metadanych zasobu, a istniejące zasoby są ponownie haszowane przed uznaniem ich za już pobrane.
- Ponieważ Wikimedia Commons nie dostarcza w tym przepływie zaufanej sumy SHA-256 dla pliku, pierwsza poprawna suma jest lokalnie obliczana i zapisywana; nie jest przedstawiana jako suma opublikowana przez Wikimedia.
