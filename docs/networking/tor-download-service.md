# Reusable Tor download service

## Purpose

The Tor networking layer is independent from resource catalogues and storage. SVG, documents,
archives, images, and future download consumers use the same transport API. The transport returns
a closeable response stream; it does not buffer or persist the complete response.

## Layers

- `TorRuntime`: lifecycle and bootstrap state. Implementations may embed a Tor runtime or connect
  to a separately managed local Tor service.
- `TorProxyEndpoint`: loopback-only SOCKS5 endpoint. The unresolved target address asks the
  Java SOCKS implementation to resolve the destination through the proxy rather than locally.
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

1. Select and integrate a maintained Android Tor runtime.
2. Implement `TorRuntime` with bootstrap progress and lifecycle management.
3. Add instrumented tests against a local SOCKS5 test server and a Tor test network.
4. Migrate Wikimedia Commons API and SVG downloads to an injected transport.
5. Add a settings surface for AUTO / DIRECT / TOR only after the runtime and fallback policy
   are fully tested. TOR mode must fail closed; AUTO fallback must be explicit and observable.
