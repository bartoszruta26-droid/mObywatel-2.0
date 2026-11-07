<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->
# obywatel-android

Minimalny szkielet aplikacji Android w Kotlinie i Jetpack Compose.

## Zakres

- Material 3 i centralne tokeny kolorów/kształtów.
- Ekran startowy demonstracyjny z kaflami usług.
- Lokalny portfel plików: import PDF/obrazów przez Storage Access Framework (SAF), zapamiętywanie trwałych URI, lista i usuwanie wpisów.
- Podgląd PDF przez `PdfRenderer` z nawigacją stron oraz podgląd obrazów z gestami powiększania i przesuwania.

## Ważne

Repozytorium nadrzędne opisuje publiczny wycinek materiałów dotyczących mObywatela; nie zawiera logiki biznesowej, API ani modułu uwierzytelniania aplikacji. Ten katalog jest niezależnym prototypem UI, a nie oficjalną aplikacją mObywatel. Nie implementuje logowania ani usług państwowych. Portfel obsługuje wyłącznie pliki lokalne wskazane przez użytkownika; nie tworzy ani nie weryfikuje urzędowych dokumentów i nie powinien sugerować autentyczności takich funkcji.

Kolory i komponenty w `app/src/main/java/pl/gov/obywatel/android/ui/theme/` są wstępnymi tokenami demonstracyjnymi. Po pobraniu i zweryfikowaniu właściwych plików z galerii design systemu należy je zastąpić rzeczywistymi wartościami i assetami, z zachowaniem warunków ich licencji.

## Uruchomienie

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL i Jetpack Compose. W razie potrzeby wygeneruj wrapper Gradle z poziomu Android Studio.

## Następne kroki

1. Zweryfikować dokładne kolory, typografię, odstępy, piktogramy i licencje w opublikowanych zasobach.
2. Wydzielić komponenty wielokrotnego użytku.
3. Dodać testy UI i sprawdzanie dostępności.
4. Dodać testy jednostkowe magazynu URI i testy UI importu/podglądu.
5. Utrzymać wyraźne oznaczenie prototypu, dopóki nie zostanie zbudowana rzeczywista i uprawniona integracja z usługami.
