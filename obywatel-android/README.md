<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->
# obywatel-android

Niezależny prototyp Android w Kotlinie i Jetpack Compose, przeznaczony do lokalnego porządkowania dokumentów i identyfikatorów rejestru.

## Zakres obecnej wersji

- Ekran główny z działającą nawigacją do wszystkich widocznych sekcji.
- Lokalny portfel plików: import PDF/DOCX/obrazów przez Storage Access Framework (SAF), trwałe URI, lista, usuwanie i podgląd.
- Lokalny rejestr metadanych dokumentów zapisany w prywatnych preferencjach aplikacji.
- Generowanie kodu QR zawierającego lokalny identyfikator wpisu w formacie `urn:rp:local-document-registry:<UUID>`.
- Ekrany informacyjne profilu, bezpieczeństwa, płatności, zdrowia i przyszłej weryfikacji QR.
- Dostępność: semantyczne tytuły paneli, nagłówki, opisy kontrolek i obsługa systemowego przycisku Wstecz.

## Ograniczenia i status prawny

To niezależna aplikacja demonstracyjna. Nie jest oficjalną aplikacją mObywatel, systemem Sejmu, urzędu ani administracji państwowej. Sam napis „Rzeczpospolita Polska”, oznaczenie „Poseł”, wizerunek godła, wpis w lokalnym rejestrze ani kod QR nie potwierdzają statusu wystawcy, urzędowego pochodzenia dokumentu ani jego mocy prawnej.

Lokalny rejestr przechowuje obecnie tytuł, identyfikator i datę utworzenia wpisu. Nie jest połączony z plikiem źródłowym, nie oblicza skrótu dokumentu, nie publikuje wpisów w sieci i nie zapewnia zewnętrznej weryfikacji. Kod QR identyfikuje wpis lokalny; nie jest podpisem elektronicznym, pieczęcią elektroniczną ani urzędowym poświadczeniem. Skanowanie kodów QR i weryfikacja zdalna nie są jeszcze zaimplementowane.

Ekrany profilu, bezpieczeństwa, płatności i zdrowia są informacyjne. Nie ma logowania do usług państwowych ani integracji z ich API. Nie należy używać prototypu do przedstawiania dokumentów jako wydanych lub zatwierdzonych przez organ państwowy.

Wizerunek godła RP może zostać dodany dopiero po sprawdzeniu właściwego źródła pliku, zgodności z ustawowym wzorem i podstawy prawnej konkretnego sposobu użycia. Obecna wersja nie zawiera godła państwowego ani imitacji urzędowej pieczęci.

## Uruchomienie

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL, Jetpack Compose oraz ZXing Core do lokalnego generowania kodów QR.

## Następne kroki techniczne

1. Powiązać wpis rejestru z rzeczywistym plikiem i obliczać SHA-256 podczas importu lub rejestracji.
2. Dodać skaner QR i walidację identyfikatorów w obrębie lokalnego rejestru.
3. Zaprojektować kontrolowany eksport i kopię zapasową rejestru.
4. Dodać testy jednostkowe magazynu rejestru, generowania identyfikatorów i kodów QR.
5. Dodać ochronę lokalnego profilu oraz kontrolę dostępu do danych.
6. Zweryfikować zasoby wizualne i podstawę prawną użycia oznaczeń państwowych przed ich dołączeniem.
