# obywatel-android

Minimalny niezależny prototyp Android w Kotlinie i Jetpack Compose.

## Zakres

- Material 3 i centralne tokeny kolorów/kształtów.
- Ekran startowy demonstracyjny z kaflami usług.
- Oryginalne piktogramy usług COI przekonwertowane do Android VectorDrawable, w tym piktogram sprawdzenia dowodu.
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

## Uruchomienie i kompilacja

Otwórz katalog `obywatel-android` w Android Studio z Android SDK i JDK 17. Projekt korzysta z Gradle Kotlin DSL i Jetpack Compose.

Kompilacja z zainstalowanym Gradle 8.9:

```bash
cd obywatel-android
gradle --no-daemon :app:assembleDebug
```

Plik APK powstaje w `app/build/outputs/apk/debug/app-debug.apk`. Repozytorium ma także workflow GitHub Actions `.github/workflows/android-build.yml`, który buduje wariant debug po zmianach w katalogu Androida i udostępnia APK jako artefakt CI.

### Moduły OWD

- `OwdSchemaRegistry.kt` — katalog własnych, wersjonowanych schematów OWD i walidacja rekordów.
- `OwdQrFrames.kt` — ramkowanie zaszyfrowanych ładunków na serię QR.
- `OwdIntegration.kt` — pochodzenie danych oraz jawna granica mapowania na schematy dopuszczone przez operatora.

Identyfikatory `pl.owd.*` są własnymi identyfikatorami aplikacji. Nie są schematami mObywatela. Mapowanie do schematu zewnętrznego wymaga jawnego zatwierdzenia i pełnego mapowania pól; w kodzie alfa nie ma aktywnego mapowania ani adaptera rządowego.

## Źródła zasobów

Piktogramy pochodzą z `iOS/Sources/Assets.xcassets/Icons/Brand/Services/` i są oznaczone licencją `LicenseRef-COI-MIT-PL` (tekst: `LICENSES/LicenseRef-COI-MIT-PL.txt`). Zweryfikowane skale odstępów i promieni znajdują się w `iOS/Sources/Tokens/Spacings.swift` oraz `iOS/Sources/Tokens/Radiuses.swift`. Szczegóły mapowania i ograniczenia opisano w `UI-DESIGN-NOTES.md`.

## Następne kroki

1. Zweryfikować dokładne kolory, typografię, odstępy i piktogramy w opublikowanych zasobach.
2. Wydzielić komponenty wielokrotnego użytku.
3. Dodać testy UI i sprawdzanie dostępności.
4. Utrzymać wyraźne oznaczenie prototypu, dopóki nie zostanie zbudowana rzeczywista i uprawniona integracja z usługami.
