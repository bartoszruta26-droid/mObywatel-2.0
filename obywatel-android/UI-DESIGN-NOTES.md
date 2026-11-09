<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Notatki projektowe UI

## Zakres zmian

- Ekran startowy niezależnego prototypu Android w Jetpack Compose.
- Oryginalne piktogramy usług przeniesione z katalogu źródłowego iOS w tym repozytorium do Android VectorDrawable.
- Ustawienia i pozycja „Pozostałe” używają ikon ogólnego przeznaczenia; nie przypisują im ikon innych usług.
- Paleta kolorów pozostaje oznaczona jako demonstracyjna do czasu pełnego odwzorowania i sprawdzenia wartości z zasobów systemu designu.

## Pochodzenie zasobów

Piktogramy usług pochodzą z:
`iOS/Sources/Assets.xcassets/Icons/Brand/Services/`.
Wykorzystane zasoby obejmują DA001, DA002, DA003, DA004, DA006, DA008, DA009, DA013, DA025, DA035 oraz DA043 (sprawdzenie dowodu). Źródłowe pliki są oznaczone w nadrzędnym `REUSE.toml` jako © 2025 Centralny Ośrodek Informatyki oraz `LicenseRef-COI-MIT-PL`. Tekst licencji znajduje się w `LICENSES/LicenseRef-COI-MIT-PL.txt`; konwersje Android VectorDrawable zachowują oznaczenie prawnoautorskie i identyfikator licencji.

## Zweryfikowane tokeny źródłowego systemu designu

W `iOS/Sources/Tokens/Spacings.swift` zdefiniowano skalę odstępów: 2, 4, 8, 12, 16, 20, 24, 32, 40, 48 i 56 punktów. W `iOS/Sources/Tokens/Radiuses.swift` zdefiniowano promienie: 4, 12, 16, 24 i 40 punktów. Nazwy tokenów kolorystycznych znajdują się w `iOS/Sources/Tokens/Colors.swift`; wartości barw należy pobrać z odpowiadających im zasobów kolorów, a nie wyprowadzać z nazw.

## Granice identyfikacji i bezpieczeństwa

- Prototyp nie jest oficjalną aplikacją mObywatel.
- Nie dodano dokumentów poświadczających, danych użytkowników, logowania ani mechanizmów bezpieczeństwa.
- Piktogram sprawdzenia dowodu jest wyłącznie ikoną kafla usług, nie elementem dokumentu ani potwierdzeniem tożsamości.
- Oficjalne znaki i godło należy wykorzystywać wyłącznie w granicach ich właściwych zasad prawnych i licencyjnych.

## Formularz danych demonstracyjnych

Ekran „Dokumenty” zawiera wyłącznie formularz ewidencyjny z pustymi polami na dane osobowe, dane dowodu osobistego, dane paszportowe i podstawowe dane dotyczące legitymacji poselskiej oraz neutralny obszar na zdjęcie profilowe. Wszystkie sekcje są jednoznacznie oznaczone jako DEMO — NIE JEST DOKUMENTEM. Pola nie są zapisywane ani wysyłane. Ekran nie odtwarza oficjalnych wzorów dokumentów, legitymacji, zabezpieczeń, kodów QR, kodów kreskowych ani strefy MRZ i nie potwierdza tożsamości ani statusu publicznego.

### Konfiguracja, trwały zapis i zdjęcie

Ekran demonstracyjny umożliwia wybranie fotografii przez systemowy selektor Androida (limit 5 MB), edycję pól osobowych/dowodu/paszportu/ewidencji poselskiej oraz jawne zapisanie całości. Zapis jest lokalny w `noBackupFilesDir`, szyfrowany AES-256-GCM z kluczem zarządzanym przez Android Keystore; aplikacja nie deklaruje uprawnień do Internetu ani nie wysyła danych. `allowBackup=false` pozostaje w manifeście. Dane są wrażliwe: to prototyp, nie audytowany sejf na dokumenty. Utrata/wyczyszczenie danych aplikacji lub odinstalowanie usuwa lokalny profil. Nie jest to cyfrowy dokument, dowód statusu publicznego ani kanał weryfikacji.
