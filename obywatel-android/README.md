# obywatel-android

Minimalny niezależny prototyp Android w Kotlinie i Jetpack Compose.

## Zakres

- Material 3 i centralne tokeny kolorów/kształtów.
- Ekran startowy demonstracyjny z kaflami usług.
- Oryginalne piktogramy systemu designu przeniesione z publicznego katalogu iOS w tym repozytorium do Android VectorDrawable:
  - DA001 — punkty karne,
  - DA002 — naruszenie środowiskowe,
  - DA003 — Polak za granicą,
  - DA004 — eRecepta,
  - DA006 — Bilkom,
  - DA008 — zastrzeż PESEL,
  - DA009 — weryfikuj PESEL,
  - DA013 — ePłatności,
  - DA025 — bilet,
  - DA035 — bezpiecznie w sieci.

## Pochodzenie i licencje

Powyższe piktogramy pochodzą z `iOS/Sources/Assets.xcassets/Icons/Brand/Services/` w tym repozytorium. W nadrzędnym pliku `REUSE.toml` zasoby źródłowe są oznaczone jako © 2025 Centralny Ośrodek Informatyki, `LicenseRef-COI-MIT-PL`. Wersje VectorDrawable w `app/src/main/res/drawable/mob_*.xml` zachowują to oznaczenie. Przed dystrybucją należy stosować warunki tej licencji i zachować informacje o pochodzeniu.

## Ważne

Ten katalog jest niezależnym prototypem UI, a nie oficjalną aplikacją mObywatel. Nie implementuje logowania, dokumentów ani usług państwowych i nie powinien sugerować autentyczności takich funkcji. Nie należy odtwarzać mechanizmów uwierzytelniania ani zabezpieczeń dokumentów.

Kolory i komponenty w `app/src/main/java/pl/gov/obywatel/android/ui/theme/` pozostają wstępnymi tokenami demonstracyjnymi. Należy je zastąpić dokładnymi wartościami z legalnie udostępnionego design systemu, gdy potwierdzona zostanie licencja i aktualność wersji.

## Uruchomienie

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL i Jetpack Compose.

## Następne kroki

1. Zweryfikować dokładne kolory, typografię, odstępy i piktogramy w opublikowanych zasobach.
2. Wydzielić komponenty wielokrotnego użytku.
3. Dodać testy UI i sprawdzanie dostępności.
4. Utrzymać wyraźne oznaczenie prototypu, dopóki nie zostanie zbudowana rzeczywista i uprawniona integracja z usługami.
