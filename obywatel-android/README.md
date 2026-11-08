<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->
# obywatel-android

Niezależny prototyp Android w Kotlinie i Jetpack Compose, przeznaczony do lokalnego porządkowania dokumentów i identyfikatorów rejestru. Interfejs wykorzystuje wybrane, opublikowane przez Centralny Ośrodek Informatyki elementy systemu projektowego mObywatela jako punkt odniesienia wizualnego.

## Warstwa wizualna

- Przebudowany ekran główny z jasną hierarchią nagłówka, kartami usług, zaokrągleniami i spokojną paletą błękitów.
- Stały dolny pasek pięciu sekcji: Dokumenty, Usługi, Szukaj, Skanuj i Więcej.
- Oryginalne piktogramy COI z katalogu iOS przeniesione do natywnych zasobów Android VectorDrawable: ikony paska dolnego (warianty zwykłe i zaznaczone), a także piktogramy profilu, usług, zabezpieczenia PESEL, e-Recepty i biletu.
- Zasoby wektorowe są skalowalne i barwione przez motyw Compose; nie są zrzutami ekranu ani ręcznie odrysowanymi zamiennikami.
- Znak w nagłówku i adaptacyjna ikona uruchomieniowa są własnymi znakami graficznymi tego prototypu, a nie oficjalnym logo mObywatela.
- Repozytorium nie zawiera zweryfikowanego oficjalnego logo mObywatela ani pliku godła RP przeznaczonego do użycia w tym prototypie. Nie dodano imitacji godła, pieczęci ani urzędowego oznaczenia. Włączenie godła wymaga osobnej weryfikacji wzoru, źródła i zasad jego użycia.

### Pochodzenie i licencja piktogramów

Piktogramy pochodzą z repozytorium źródłowego mObywatela, z katalogu `iOS/Sources/Assets.xcassets/Icons`, opublikowanego przez Centralny Ośrodek Informatyki. Pliki są oznaczone w pliku `REUSE.toml` źródła jako `LicenseRef-COI-MIT-PL`, Copyright (c) 2025 Centralny Ośrodek Informatyki. Kopia licencji znajduje się w `obywatel-android/LICENSES/LicenseRef-COI-MIT-PL.txt`. Zachowano informacje o prawach autorskich i oznaczenia SPDX w każdym przeniesionym zasobie.

Źródła:
- https://github.com/bartoszruta26-droid/mObywatel-2.0/tree/main/iOS/Sources/Assets.xcassets/Icons/Lift/BottomBar
- https://github.com/bartoszruta26-droid/mObywatel-2.0/tree/main/iOS/Sources/Assets.xcassets/Icons/Brand/Services
- https://github.com/bartoszruta26-droid/mObywatel-2.0/tree/main/iOS/Sources/Assets.xcassets/Icons/Filled

## Zakres funkcjonalny

- Ekran główny z nawigacją do wszystkich widocznych sekcji.
- Lokalny portfel plików: import PDF/DOCX/obrazów przez Storage Access Framework (SAF), trwałe URI, lista, usuwanie i podgląd.
- Lokalny rejestr metadanych dokumentów: tytuł, identyfikator, data utworzenia, URI źródła i SHA-256 pliku.
- Generowanie kodu QR zawierającego lokalny identyfikator wpisu w formacie `urn:rp:local-document-registry:<UUID>`.
- Odczyt kodu QR z wybranego obrazu i porównanie aktualnego skrótu pliku ze skrótem zapisanym w lokalnym rejestrze.
- Ekrany informacyjne profilu, bezpieczeństwa, płatności i zdrowia.
- Dostępność: semantyczne tytuły paneli, nagłówki, opisy kontrolek i obsługa systemowego przycisku Wstecz.

## Ograniczenia i status prawny

To niezależna aplikacja demonstracyjna. Nie jest oficjalną aplikacją mObywatel, systemem Sejmu, urzędu ani administracji państwowej. Zastosowanie piktogramów z opublikowanego systemu projektowego nie oznacza afiliacji z COI ani administracją publiczną. Znak prototypu nie jest godłem państwowym ani urzędową pieczęcią.

Lokalny rejestr nie publikuje wpisów w sieci i nie zapewnia zewnętrznej weryfikacji. Kod QR identyfikuje wpis lokalny; nie jest podpisem elektronicznym, pieczęcią elektroniczną ani urzędowym poświadczeniem. Odczyt kodu z obrazu nie jest skanowaniem aparatem na żywo. Porównanie SHA-256 wykrywa zmianę zawartości pliku względem lokalnie zapisanego skrótu, ale nie uwierzytelnia wystawcy ani pochodzenia dokumentu.

Ekrany profilu, bezpieczeństwa, płatności i zdrowia są informacyjne. Nie ma logowania do usług państwowych ani integracji z ich API. Nie należy używać prototypu do przedstawiania dokumentów jako wydanych lub zatwierdzonych przez organ państwowy.

## Uruchomienie

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL, Jetpack Compose oraz ZXing Core do lokalnego generowania i odczytu kodów QR.

## Następne kroki techniczne

1. Dokończyć wizualne odwzorowanie kart dokumentów i szczegółów dokumentu na podstawie dostępnych wzorców design systemu.
2. Zweryfikować i ewentualnie dołączyć oficjalny wzór godła RP z odrębnego, autorytatywnego źródła oraz opisać zasady użycia.
3. Dodać skaner QR aparatem i walidację identyfikatorów w obrębie lokalnego rejestru.
4. Zaprojektować kontrolowany eksport i kopię zapasową rejestru.
5. Dodać ochronę lokalnego profilu oraz kontrolę dostępu do danych.
