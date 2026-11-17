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


## Klienci Tor i ustawienia aplikacji

Menu Ustawienia zawiera ekran „Klienci sieci Tor”. Rejestr profili jest przechowywany w prywatnych preferencjach aplikacji i obejmuje:
- wbudowany klient Guardian Project (domyślny);
- profil lokalnego SOCKS5 Orbot (domyślnie `127.0.0.1:9050`);
- profile własne z nazwą, adresem i portem;
- wybór preferowanego profilu, aktywowanie/dezaktywowanie i usuwanie profili własnych.

Celowo dopuszczane są wyłącznie adresy loopback (`127.0.0.1`, `localhost`, `::1`). Nie ma obsługi dowolnego zdalnego publicznego SOCKS: operator takiego proxy może obserwować metadane połączeń, a sam endpoint nie dowodzi, że ruch trafia do sieci Tor. Publiczna sieć Tor jest osiągana przez uruchomionego klienta Tor, nie przez anonimowy, współdzielony adres SOCKS.

**Stan integracji:** Guardian Project jest klientem bazowym, a profile zewnętrzne są dopuszczane do puli dopiero po pozytywnym teście SOCKS5 i `IsTor=true` z `check.torproject.org`. Orbot dodatkowo wymaga zainstalowanego pakietu `org.torproject.android`. Wybór preferowanego profilu wpływa na kolejność wyboru; pula używa round-robin, a niedostępny Orbot jest pomijany. Wszystkie połączenia pozostają wymuszone przez lokalny SOCKS — brak przejścia na sieć bezpośrednią.


## Pula lokalnych portów SOCKS i rozdzielanie pobrań

Wbudowany `GuardianTorRuntime` uruchamia jeden proces Tor z podstawowym portem wybieranym z zakresu 19040–19050 oraz maksymalnie czterema dodatkowymi listenerami SOCKS5 z zakresu 19051–19080. Oddzielny zakres pozostawia Orbotowi jego standardowy port 9050. Dodatkowe porty są wybierane z zakresu 19051–19080; zajęte porty są pomijane, aby nie blokować startu całego klienta.

Każdy dodatkowy listener używa flag `IsolateClientAddr`, `IsolateDestAddr` i `IsolateDestPort`. Tor domyślnie utrzymuje odrębne grupy sesji dla różnych portów SOCKS. Runtime odczytuje aktywne listenery przez `GETINFO net/listeners/socks` i publikuje wyłącznie porty, które faktycznie działają.

`TorDownloadService` wybiera w trybie round-robin przede wszystkim dodatkowe listenery Guardian skonfigurowane z flagami izolacji. Podstawowy port Guardian pozostaje ścieżką awaryjną, gdy żaden dodatkowy listener nie został uruchomiony. Wybór uwzględnia preferowanego klienta z ustawień. Klasyfikacja `TorDownloadKind` umożliwia także jawne przypisania typu pliku do profilu przez `TorDownloadRouter` z polityką `PINNED_BY_KIND`.

**Granica izolacji:** dodatkowe porty Guardian są odizolowanymi grupami strumieni jednego procesu Tor, a nie osobnymi demonami z niezależnymi guardami. Nie gwarantują innego adresu wyjściowego dla każdego pliku. Orbot jest odrębnym procesem Tor i może wnosić niezależną ścieżkę, jeśli test wyjścia potwierdzi, że port rzeczywiście należy do Orbot.

Publiczne przekaźniki Tor i mosty nie są publicznymi serwerami SOCKS. Przekaźniki i mosty konfiguruje się wewnątrz klienta Tor jako elementy drogi wejściowej, a nie jako proxy pobierania. Nieznane zdalne SOCKS pozostają odrzucane przez model bezpieczeństwa.

## Integracja Orbot, test wyjścia i rotacja obwodów

- Wbudowany Guardian Project Tor jest wymaganym klientem bazowym. Runtime czeka na status pierwszego zbudowanego obwodu i udostępnia wyłącznie porty SOCKS potwierdzone jako aktywne przez próbę połączenia TCP.
- Orbot jest profilem zewnętrznym na loopback SOCKS5 `127.0.0.1:9050` (domyślna konfiguracja Orbot). Samo otwarte gniazdo nie wystarcza: aplikacja wykonuje żądanie przez wskazany SOCKS do `https://check.torproject.org/api/ip` i wymaga `IsTor=true`. Profil Orbot jest pomijany, gdy pakiet `org.torproject.android` nie jest zainstalowany, port nie odpowiada albo test wyjścia nie potwierdza Tor.
- Gdy zewnętrzny SOCKS przestaje przyjmować połączenia, profil jest czasowo wyłączany z puli, a Guardian jest uruchamiany na żądanie jako zapasowy klient. Nie ma bezpośredniego połączenia awaryjnego. Błędy HTTP serwera docelowego nie są mylone z awarią Orbot.
- Test Orbot jest dostępny w ekranie Ustawienia → Klienci sieci Tor. Domyślny profil aplikacji wskazuje `127.0.0.1:9050`. Guardian korzysta z oddzielnego zakresu 19040–19050 i jest uruchamiany dopiero wtedy, gdy Orbot jest niedostępny lub wybrano Guardian. Jeżeli użytkownik zmieni port SOCKS w Orbot, musi podać ten sam port w profilu aplikacji. Aplikacja nie próbuje uruchamiać chronionej usługi VPN Orbot z pominięciem uprawnień systemowych.
- Wbudowany Guardian otrzymuje `MaxCircuitDirtiness 480`: po 8 minutach Tor nie przypina nowych strumieni do zbyt starego obwodu. Aktywne połączenie TCP pozostaje na dotychczasowym obwodzie do zakończenia. To celowe; nie przenosimy aktywnego strumienia między obwodami.
- Orbot jako osobny proces zachowuje własną konfigurację Tor i domyślną politykę rotacji (zwykle 10 minut). Aplikacja nie zmienia tej konfiguracji, ponieważ zewnętrzny interfejs Orbot nie udostępnia bezpiecznego, wspieranego kontraktu do ustawiania `MaxCircuitDirtiness` dla obcego procesu.
- Oddzielne porty SOCKS Guardian mają domyślnie odrębne grupy sesji, a flagi `IsolateClientAddr`, `IsolateDestAddr`, `IsolateDestPort` dodatkowo ograniczają współdzielenie obwodów. Rotacja 8-minutowa dotyczy nowych strumieni, nie przerywa już rozpoczętych pobrań.

## Pobieranie SVG przez Tor i wznawianie

- Zapytania API Commons i treść plików SVG przechodzą przez `TorDownloadService`; żądania katalogu i plików nie korzystają z bezpośredniego transportu HTTP.
- Plik częściowy jest przechowywany tymczasowo w prywatnym katalogu cache aplikacji jako `.part`, wraz z ETag/Last-Modified. To umożliwia wznowienie po przerwaniu procesu; po zatwierdzeniu zasobu plik częściowy i metadane są usuwane.
- Wznowienie używa `Range` i `If-Range`; dopisywanie bajtów następuje wyłącznie po otrzymaniu prawidłowego `206 Partial Content` i zgodnego `Content-Range`. Przy braku walidatora albo niezgodnej odpowiedzi pobieranie zaczyna się od początku.
- Po pobraniu aplikacja sprawdza limit rozmiaru, strukturę XML/SVG, niedozwolone elementy i SHA-256. Suma jest zapisywana w metadanych zasobu, a istniejące zasoby są ponownie haszowane przed uznaniem ich za już pobrane.
- Ponieważ Wikimedia Commons nie dostarcza w tym przepływie zaufanej sumy SHA-256 dla pliku, pierwsza poprawna suma jest lokalnie obliczana i zapisywana; nie jest przedstawiana jako suma opublikowana przez Wikimedia.
