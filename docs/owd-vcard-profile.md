# Profil OWD oparty na vCard 4.0

## Cel i warstwy

OWD używa vCard 4.0 jako kanonicznego formatu rekordu osoby/danych udostępnianych przez użytkownika. Format bazowy jest zgodny z RFC 6350. Pola specyficzne dla obywatelskich rekordów OWD są przenoszone jako rozszerzenia właściwości `X-OWD-*`, zgodnie z mechanizmem rozszerzeń vCard.

Warstwy są rozdzielone:

1. **Treść danych:** vCard 4.0 UTF-8, w tym standardowe właściwości kontaktowe i rozszerzenia OWD.
2. **Sesja:** krótkotrwały kod QR `OWD_SESSION` zawierający identyfikator sesji, klucz publiczny efemeryczny, nonce i termin ważności. Kod sesji nie jest wizytówką i nie zawiera danych osobowych.
3. **Ochrona transportu:** ECDH/HKDF do wyprowadzenia klucza oraz AES-256-GCM do szyfrowania i uwierzytelnienia zawartości.
4. **Transport:** bezpośredni kanał TCP lub przyszły, jawnie skonfigurowany przekaźnik.
5. **Potwierdzenie:** uwierzytelnione potwierdzenie odbioru powiązane z sesją.

## Profil właściwości OWD

Każdy rekord OWD jest osobną wizytówką vCard 4.0. Wymagane są standardowe właściwości `BEGIN:VCARD`, `VERSION:4.0`, `FN`, `N` i `END:VCARD`. Dodatkowe metadane:

| Właściwość | Znaczenie |
|---|---|
| `X-OWD-SCHEMA` | Identyfikator schematu OWD z przestrzeni nazw aplikacji |
| `X-OWD-SCHEMA-VERSION` | Wersja schematu OWD |
| `X-OWD-PROVENANCE` | Pochodzenie danych, np. LOCAL_USER lub ISSUER_ATTESTED |
| `X-OWD-ISSUER` | Identyfikator wystawcy, jeżeli istnieje |
| `X-OWD-EVIDENCE` | Referencja dowodu źródłowego, jeżeli istnieje |
| `X-OWD-ISSUED-AT` | Czas wystawienia w sekundach Unix |
| `X-OWD-VALID-UNTIL` | Czas końca ważności w sekundach Unix |
| `X-OWD-FIELD;X-OWD-NAME=...` | Wartość pola zdefiniowanego w rejestrze schematów OWD |

Rozszerzenia `X-OWD-*` są profilem aplikacyjnym OWD, a nie właściwościami zdefiniowanymi przez sam RFC 6350. Odbiorca musi rozpoznać identyfikator i wersję schematu oraz walidować każde pole względem rejestru OWD. Nieznane pola, nieobsługiwane wersje i niezgodności schematu są odrzucane.

## Reguły kodowania vCard

- Kodowanie znaków: UTF-8.
- Wersja eksportowana i wymagana dla danych OWD: vCard 4.0.
- Zakończenia linii: CRLF.
- Łamanie długich linii: maksymalnie 75 oktetów na linię, kontynuacja od spacji.
- Znak odwrotnego ukośnika, przecinek, średnik i znak nowej linii są kodowane zgodnie z regułami tekstowymi vCard.
- Wartości strukturalne `N`, `ORG` i `ADR` zachowują średniki rozdzielające składowe.
- Właściwości wielowartościowe i parametry muszą być interpretowane zgodnie z RFC 6350; aplikacja nie może traktować dowolnego tekstu jako zaufanego dowodu tożsamości.

## Ograniczenia bezpieczeństwa i stanu wdrożenia

vCard opisuje dane, ale sam w sobie nie zapewnia poufności, integralności, autentyczności wystawcy ani dowodu urzędowego. Dlatego rekord jest przesyłany jako jawnie określony payload vCard wewnątrz uwierzytelnionego szyfrowania OWD. Pochodzenie LOCAL_USER oznacza deklarację użytkownika; nie może być prezentowane jako dane urzędowo potwierdzone.

W kodzie istnieją codec vCard 4.0, mapowanie rekordów OWD do rozszerzeń vCard oraz adapter szyfrowania/dekodowania payloadu. Nie oznacza to jeszcze kompletnego przepływu użytkowego między dwoma telefonami: ekran akceptacji zakresu i celu, uzgodnienie kluczy, uruchomienie odbiornika, trwałe zużycie sesji i zapis danych muszą zostać połączone i przetestowane na dwóch rzeczywistych urządzeniach.

OWD jest niezależnym protokołem aplikacji. Rozszerzenia OWD nie są schematami rządowymi ani oficjalnym interfejsem mObywatela.


## Archiwum wszystkich danych aplikacji

Ustawienia aplikacji udostępniają eksport i import jednego pliku `.vcf`. Archiwum jest jednym obiektem `VCARD` w wersji 4.0 i przenosi zapisane lokalnie pary klucz–wartość jako powtarzane właściwości `X-OWD-FIELD;X-OWD-NAME=<nazwa>`. W ten sposób możliwe jest zachowanie pól ewidencji, także takich, które nie mają bezpośredniego odpowiednika w standardowych polach kontaktowych vCard. Jest to profil archiwizacyjny tej aplikacji, a nie uniwersalny format kopii zapasowej dowolnego programu vCard.

Import wymaga potwierdzenia użytkownika. Wartości o identycznych nazwach są zastępowane, a pozostałe dane lokalne pozostają zachowane. Plik może zawierać dane wrażliwe, w tym zakodowane zdjęcie profilowe; należy chronić go jak kopię dokumentów.

## Zakres udostępniania OWD

Ekran wyboru danych udostępniania wyświetla wszystkie niepuste pola zapisane w lokalnym profilu. Użytkownik może zaznaczyć całość jednym wyborem albo odznaczyć pojedyncze pola. Przed transferem wymagane jest świadome potwierdzenie zakresu i celu.

**Ograniczenie implementacji:** obecny ekran nadal generuje i skanuje ofertę sesji QR; nie wykonuje jeszcze kompletnego transferu wybranego zestawu vCard między urządzeniami. Wybrane pola muszą zostać w kolejnym etapie powiązane z trwałym stanem sesji, zgodą odbiorcy, szyfrowanym kanałem transportowym i potwierdzeniem odbioru. Samo utworzenie QR nie oznacza wysłania danych.
