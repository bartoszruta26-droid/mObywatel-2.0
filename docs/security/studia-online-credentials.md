<!-- SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors -->
<!-- SPDX-License-Identifier: EUPL-1.2 -->

# Studia Online — bezpieczne dane logowania i sesja

## Dane logowania

Dane konta nie są częścią kodu źródłowego, konfiguracji Gradle, zasobów Androida ani sekretów CI.
StudiaOnlineCredentialStore zapisuje je lokalnie:
- klucz AES-256 jest generowany i przechowywany w Android Keystore;
- dane są szyfrowane przez AES/GCM/NoPadding z losowym 96-bitowym IV i tagiem 128-bitowym;
- szyfrogram trafia do prywatnych SharedPreferences aplikacji;
- uszkodzony szyfrogram lub brak klucza powoduje odrzucenie danych, bez awaryjnego zapisu jawnego;
- usunięcie danych usuwa szyfrogram i klucz Keystore.

Hasło jest maskowane na ekranie. Wypełnienie formularza zapisanymi danymi wymaga osobnego działania użytkownika; aplikacja nie wysyła formularza automatycznie. Użytkownik sam sprawdza stronę i zatwierdza logowanie.

## Logowanie i sesja

Logowanie odbywa się w WebView pod adresem HTTPS studia-online.pl. Nawigacja poza domenę serwisu lub do niezabezpieczonego HTTP jest blokowana. Aplikacja nie odczytuje tokenów CSRF, nie tworzy własnego endpointu logowania i nie omija CAPTCHA, MFA, zabezpieczeń antybotowych ani odpowiedzi odmownych.

Po zalogowaniu użytkownik jawnie potwierdza przekazanie sesji. Ciasteczka pozostają w mechanizmie CookieManager Androida. Pobierający odczytuje nagłówek ciasteczek z adresu docelowego studia-online.pl i przekazuje go do transportu jako nagłówek żądania. TorHttpTransport dołącza go wyłącznie do żądań skierowanych do dokładnego hosta studia-online.pl; nie przekazuje ciasteczek przy przekierowaniu na inne hosty. Nie ma dodatkowego trwałego magazynu ciasteczek.

WebView loguje się przez systemową trasę sieciową Androida, nie przez lokalny proxy Tor. Późniejsze pobieranie może używać Guardian Tor, Orbot lub trasy bezpośredniej zgodnie z wyborem użytkownika. Jeżeli serwis wiąże sesję z adresem IP, ciasteczka mogą nie działać przy zmianie trasy. Aplikacja nie przełącza automatycznie tras i nie obchodzi ograniczeń serwisu.

## Weryfikacja i ograniczenia

Testy automatyczne obejmują szyfrowanie i odczyt danych na emulatorze. Nie zastępują rzeczywistego logowania na koncie testowym. Należy osobno sprawdzić:
1. prawidłowe logowanie i ewentualne dodatkowe potwierdzenie;
2. pojedynczy dozwolony materiał w trybie bezpośrednim;
3. ten sam materiał przez Tor, jeżeli serwis dopuszcza taką trasę;
4. odpowiedzi 401, 403 i 419 po wygaśnięciu sesji.

Hasła i ciasteczka nie mogą trafiać do zgłoszeń, commitów, logów ani raportów testowych. Jeśli hasło zostało ujawnione poza aplikacją, należy je zmienić.
