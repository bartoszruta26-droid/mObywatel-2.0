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
