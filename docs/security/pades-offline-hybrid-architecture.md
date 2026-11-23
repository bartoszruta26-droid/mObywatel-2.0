<!-- SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors -->
<!-- SPDX-License-Identifier: EUPL-1.2 -->

# PAdES: architektura lokalna z opcjonalną synchronizacją

## Cel

Domyślnym miejscem przetwarzania dokumentu i klucza prywatnego jest urządzenie Android. Brak połączenia z Internetem nie może blokować przygotowania PDF, wykonania podpisu kluczem Android Keystore ani sprawdzenia matematycznej poprawności podpisu.

Sieć jest dodatkiem do aktualizacji danych zaufania i uzyskania usług, których nie da się wiarygodnie zastąpić lokalnym zegarem lub nieaktualną kopią danych.

## Granica zaufania

### Telefon – lokalnie

- oryginalny PDF pozostaje niezmieniony; podpis powstaje jako osobny plik;
- przygotowanie PAdES, obliczenie danych DTBS i osadzenie podpisu;
- klucz prywatny w Android Keystore, bez eksportu;
- weryfikacja kryptograficzna podpisu;
- odczyt certyfikatów, okresów ważności i budowa PKIX względem jawnie zaufanych kotwic;
- lokalne raporty i zachowanie dowodów walidacji;
- szyfrowany cache LOTL/TL, CRL, OCSP, tokenów TSA i polityk.

### Połączenie sieciowe – wyłącznie jawne operacje

- synchronizacja EU LOTL i krajowych Trusted Lists;
- pobieranie CRL i odpowiedzi OCSP;
- uzyskanie nowego znacznika czasu TSA;
- pobieranie aktualizacji polityk i źródeł zaufania;
- opcjonalna kontrola zewnętrzna.

PDF i klucz prywatny nie są wysyłane do serwera w celu wykonania zwykłego podpisu lokalnego. Ewentualna zewnętrzna walidacja może otrzymać dokument tylko po osobnej, świadomej decyzji użytkownika.

## Stany walidacji offline

Raport nie może sprowadzać wszystkich wyników do jednego pola „poprawny”.

1. **Poprawność kryptograficzna** – czy podpis matematycznie odpowiada podpisanym bajtom i czy zakres ByteRange jest spójny.
2. **Ważność certyfikatu** – czy data walidacji mieści się w notBefore/notAfter.
3. **Ścieżka certyfikacji** – czy łańcuch prowadzi do lokalnej kotwicy, która pochodzi z wcześniej zweryfikowanego źródła.
4. **Unieważnienie** – GOOD tylko wtedy, gdy dostępny jest wiarygodny dowód CRL/OCSP właściwy dla certyfikatu i czasu walidacji. Brak lub przeterminowany dowód oznacza UNKNOWN/STALE, nigdy GOOD.
5. **Znacznik czasu** – ważny wyłącznie po weryfikacji podpisu TSA, certyfikatu TSA, ścieżki i zakresu czasu.
6. **Kwalifikacja eIDAS** – wymaga odpowiedniej usługi zaufania, statusu kwalifikowanego dostawcy i kwalifikowanego urządzenia/usługi. Samo lokalne użycie Android Keystore nie nadaje podpisowi kwalifikowanego statusu.

## Poziomy PAdES i dostępność

- **B-B** – możliwy do utworzenia offline po uzyskaniu certyfikatu podpisującego.
- **B-T** – wymaga odpowiedzi zaufanej usługi TSA dla konkretnego skrótu; standardowo wymaga połączenia w chwili podpisywania lub rozszerzania.
- **B-LT** – wymaga dołączenia zweryfikowanych danych walidacyjnych (m.in. certyfikatów i dowodów unieważnienia); może być przygotowany offline, jeśli komplet właściwych danych jest już w lokalnym cache.
- **B-LTA** – wymaga archiwalnych znaczników czasu i kolejnych aktualizacji materiału dowodowego; nie należy obiecywać pełnej aktualizacji offline, jeśli brakuje nowego TSA lub aktualnych dowodów.

## Szyfrowany magazyn materiału dowodowego

Moduł `:pades-android` przechowuje dowody w katalogu `noBackupFilesDir`. Każdy obiekt jest szyfrowany AES-256-GCM kluczem Android Keystore; nazwa pliku jest skrótem SHA-256 identyfikatora. Cache ma limit 12 MiB na obiekt i 100 MiB łącznie. Obiekty są opatrzone czasem pobrania i wygaśnięcia. Przeterminowany materiał może być zachowany do analizy historycznej, ale jest zwracany ze stanem `fresh=false`.

Samo zapisanie bajtów nie oznacza, że dowód jest zaufany. Przed zapisem LOTL/TL, CRL, OCSP lub tokenu TSA warstwa importu musi zweryfikować jego podpis, zakres, wystawcę i powiązanie z właściwym certyfikatem. Nie wolno ustanawiać kotwicy zaufania tylko dlatego, że użytkownik wskazał plik.

## Stan implementacji

- istnieje JVM-owy moduł DSS 6.5.1;
- dodano moduł Android `:pades-android` z zależnościami DSS 6.5.1 jako próbę zgodności pakowania;
- dodano lokalny magazyn dowodów AES-GCM oraz testy instrumentacyjne świeżości, odczytu i limitów;
- Android Keystore wykonuje lokalne podpisywanie danych DTBS;
- **nie uznajemy jeszcze integracji PAdES DSS w APK za produkcyjną**: potrzebne są testy urządzeniowe rzeczywistego przygotowania PDF, finalizacji i walidacji. Zależność PDFBox DSS może wymagać adaptera Android; sama kompilacja nie potwierdza działania w czasie wykonywania;
- synchronizacja LOTL/TL, CRL/OCSP/TSA i walidacja podpisów tych materiałów pozostają kolejnym etapem.

## Warunek dopuszczenia do wydania

Nie usuwać istniejącej ścieżki JVM i nie włączać automatycznego przesyłania dokumentów. Moduł lokalny może zostać uznany za aktywny dopiero po testach na emulatorze i urządzeniu fizycznym z poprawnymi oraz uszkodzonymi plikami PAdES, offline i po synchronizacji, w tym po restarcie aplikacji i przy nieaktualnym materiale CRL/OCSP.
