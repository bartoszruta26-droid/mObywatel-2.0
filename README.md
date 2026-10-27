# Moje Dokumenty — rządowy portfel dokumentów (aplikacja Android)

Aplikacja **nie łączy się z serwerami państwowymi** — wyświetla dokumenty (PDF/obrazy),
które użytkownik sam jej poda, oraz wzory umieszczone lokalnie. Zachowuje urzędową
szatę graficzną: godło RP, barwy herbowe, flagi RP i UE.

## Stan obecny (v0.1.0-szkielet)

Szkielet projektu Gradle (AGP 8.5, Kotlin 1.9, Material 3, Navigation):

```
app/src/main/
├── AndroidManifest.xml            # uprawnienia lokalne, obsługa ACTION_VIEW dla PDF
├── java/pl/gov/mojedokumenty/
│   ├── MainActivity.kt            # dolna nawigacja + otwieranie plików z innych apek
│   ├── ui/SplashActivity.kt       # ekran powitalny z godłem
│   ├── ui/PortfolioFragment.kt    # lista dokumentów („Portfel”)
│   ├── ui/DocumentViewerFragment.kt # podgląd dokumentu (ramka na renderer PDF)
│   ├── ui/PlaceholderFragments.kt # Rejestr / Profil (do zbudowania)
│   ├── model/GovernmentDocument.kt
│   └── data/DocumentRepository.kt # dane lokalne (na razie przykładowe)
├── res/
│   ├── drawable/  godlo_orla.xml, flaga_rp.xml, flaga_ue.xml, ikony nawigacji…
│   ├── layout/    activity_splash, activity_main, fragment_portfolio, …
│   ├── navigation/nav_graph.xml
│   ├── values/ + values-night/    motyw dzienny/nocny „urzędowy”
│   └── mipmap-anydpi-v26/         adaptive icon
└── assets/docs/                   # ← TU WKŁADASZ SWOJE DOKUMENTY (patrz niżej)
```

## 📁 Gdzie umieścić dokumenty i symbole państwowe

### Twoje dokumenty do wyświetlania
- **Ścieżka:** `app/src/main/assets/docs/`
- Wkładaj pliki **PDF** lub **PNG/JPG**, np. `docs/dowod_wzor.pdf`.
- Nazwy zarezerwowane w repozytorium szkieletu:
  `dowod_wzor.pdf`, `prawojazdy_wzor.pdf` (dodaj je, a podłączymy je w renderze).
- Alternatywnie: dokument można wskazać z poziomu aplikacji (Storage Access
  Framework — zadanie P1) lub udostępnić plik do aplikacji z innej apki (intent
  `ACTION_VIEW` dla `application/pdf` — już zadeklarowany w manifeście).

### Symbole państwowe (zastąp szkice oficjalnymi plikami!)
Obecne `godlo_orla.xml`, `flaga_rp.xml`, `flaga_ue.xml` to **przybliżone szkice
wektorowe**. Oficjalne pliki włóż w:

| Element | Miejsce docelowe | Format |
|---|---|---|
| Godło RP (orzeł) | `app/src/main/res/drawable/godlo_orla.xml` (podmień zawartość) lub `godlo.png` w `drawable-xxxhdpi/` | SVG→Vector Asset / PNG 108×126+ |
| Flaga RP / bandera | `app/src/main/res/drawable/flaga_rp.*` | wektor/PNG |
| Flaga UE | `app/src/main/res/drawable/flaga_ue.*` | wektor/PNG |
| Ikona aplikacji | `app/src/main/res/mipmap-*` + `ic_launcher_foreground.xml` | adaptive icon 108dp |
| Pieczęć / winieta dokumentu | `app/src/main/res/drawable/ic pieczec*.xml`, tła w `assets/` | wektor/PNG |

Źródła oficjalnych znaków: KPWiC / ministerstwo cyfryzacji (wzory z Księgi Znaku
Rzeczypospolitej Polskiej), EUR-Lex (flaga UE). Przed publikacją zweryfikuj
prawo do użycia symboli (ustawa o godle, barwach i hymnie RP).

## Uruchomienie

```bash
./gradlew :app:assembleDebug   # wymaga Android SDK; wrapper do pobrania: ./gradlew wrapper
```
lub otwórz projekt w Android Studio (Hedgehog+) i kliknij Run.
MinSdk 26, compileSdk/targetSdk 34.

---

# 📋 PLAN ZADAŃ (kolejne miesiące)

Legenda priorytetów: **P0** krytyczne, **P1** ważne, **P2** rozwojowe, **P3** kosmetyka.

## Miesiąc 1 — Fundament i budowanie bez błędów
1. [P0] Dodać pełny Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) i zweryfikować `./gradlew assembleDebug`.
2. [P0] Ustawić `JAVA_HOME`/SDK; przejść test kompilacji całego modułu `:app`.
3. [P0] Naprawić/lintować zasoby (lint `missingTranslation`, `unusedResources`).
4. [P0] Dodać brakujący pakiet `ui/theme` (Material3 `Color.kt`, `Theme.kt`, `Type.kt`) jako źródło motywu obok XML.
5. [P1] Utworzyć osobne `values-pl` i `values-en` (umowne szablony tłumaczeń).
6. [P1] Dodać `google-services`-free konfigurację podpisu debug/release (`keystore.properties` w `.gitignore`).
7. [P1] CI: GitHub Actions — build + lint + test jednostkowy.
8. [P2] Uzupełnić README sekcją „Jak wgrać swoje PDF-y” z przykładem struktury `assets/docs/`.

## Miesiąc 2 — Wyświetlanie dokumentów (rdzeń aplikacji)
9. [P0] Zaimplementować renderer PDF (`PdfRenderer` z Android SDK — bez zależności zewnętrznych) w `DocumentViewerFragment` (stronicowanie, zoom pinch).
10. [P0] Import dokumentów przez SAF (`ACTION_OPEN_DOCUMENT`, filtry MIME `application/pdf`, `image/*`) + trwały `takePersistableUriPermission`.
11. [P0] Podłączenie plików z `assets/docs/` do kart `DocumentRepository` (asset manager → cache → PdfRenderer).
12. [P1] Obsługa obrazów (JPEG/PNG) w podglądzie z gestem obracania.
13. [P1] Lista stron/miniatury + suwak stron w viewerze.
14. [P1] Udostępnianie/eksport kopii dokumentu (`FileProvider`, share sheet).
15. [P2] Tryb „pełny ekran” z ukryciem pasków (np. okazanie dokumentu urzędnikowi).
16. [P2] Wyszukiwarka dokumentów po numerze seryjnym/tytule.

## Miesiąc 3 — Trwałość danych i portfel
17. [P0] Baza Room: encja `DocumentEntity`, DAO, migracje; zastąpienie `DocumentRepository` (in-memory).
18. [P1] Szyfrowanie metadanych (SQLCipher lub EncryptedSharedPreferences) + blokada PIN/biometria (`BiometricPrompt`).
19. [P1] Ekran „Dodaj dokument”: formularz (typ, numer, organ wydający, daty ważności) z walidacją.
20. [P1] Automatyczne wykrywanie kończącej się ważności (badge „Do odnowienia”, przypomnienie lokalne `WorkManager`).
21. [P2] Grupowanie dokumentów w portfelu (osobiste / pojazdy / nieruchomości).
22. [P2] Usuwanie/archiwizacja dokumentu z potwierdzeniem.
23. [P3] Sortowanie: ostatnio używane / alfabetycznie / wg ważności.

## Miesiąc 4 — Oryginalna szata graficzna („urzędowa, ale z klasą”)
24. [P0] Podmienić szkice na **oficjalne wektory** godła/flag (patrz tabela powyżej).
25. [P1] Autorska czcionka nagłówków (np. krój urzędowy inspirowany „Bona Nova”, licencja OFL) w `res/font`.
26. [P1] Tła kart z mikroteksturą „papier security” + winieta/sieć antykopiowa jak na druku urzędowym (layer-list/shader).
27. [P1] Animowane godło na splashu (wektor path morph) + dźwięk/wibracja potwierdzenia.
28. [P2] Widok karty dokumentu „awers/rewers” (obrot 3D karty — okazanie danych).
29. [P2] Spójny tryb nocny (walidacja kontrastu WCAG AA, atrybuty `?attr`).
30. [P2] Widget ekranu głównego: najszybszy dostęp do 1 dokumentu („pokaż prawo jazdy”).
31. [P3] Ekran „Rejestr” — historia wyświetleń z osią czasu w stylu księgi wpisów.
32. [P3] Ekran „Profil” — awatar z orzełkiem, dane demonstracyjne, przełącznik motywu.

## Miesiąc 5 — Bezpieczeństwo, RODO, jakość
33. [P0] Zgoda/klauzula informacyjna przy pierwszym uruchomieniu (RODO — dane lokalne).
34. [P1] Screenshot-guard (`FLAG_SECURE`) dla dokumentów oznaczonych jako wrażliwe.
35. [P1] Sesja z auto-blokadą po 60 s nieaktywności.
36. [P1] Testy jednostkowe: `DocumentRepository`, formatowanie dat, logika statusów.
37. [P2] Testy UI (Espresso): przepływ Portfel → Podgląd → import pliku.
38. [P2] Audyt uprawnień: usunąć zbędne (`READ_MEDIA_IMAGES` tylko jeśli potrzebne).
39. [P3] ProGuard/R8 reguły + włączenie minifikacji release.

## Miesiąc 6 — Wydanie 1.0 i dodatni dług techniczny
40. [P1] Wersjonowanie i changelog (`CHANGELOG.md`), semver, sign + mapping upload.
41. [P1] Optymalizacja startu cold (<1s): baseline profiles, SplashScreen API (Android 12+).
42. [P2] Multiwindow/tablet (list-detail pane).
43. [P2] Ułatwienia: TalkBack, dynamic type, rozmiar dotyku ≥48dp.
44. [P2] Eksport/import portfela (plik `.zip` z PDF-ami + manifest JSON) — backup między telefonami.
45. [P3] Tryb „kiosk okazania” — jeden dokument, wielki podpis elektroniczny wzrokowy (QR dekoracyjny).
46. [P3] Sklepowe materiały: zrzuty, opis „aplikacja wizerunkowa, nieinstytucjonalna”.

> Każde zadanie wykonuję w kolejnych iteracjach na tym repozytorium; po każdej
> partii aktualizuję checklistę ✓ oraz notatki w `CHANGELOG.md`.
