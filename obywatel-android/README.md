<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: EUPL-1.2
-->
# obywatel-android

Niezależny prototyp Android w Kotlinie i Jetpack Compose, przeznaczony do lokalnego porządkowania dokumentów i identyfikatorów rejestru. Interfejs wykorzystuje wybrane, opublikowane przez Centralny Ośrodek Informatyki elementy systemu projektowego mObywatela jako punkt odniesienia wizualnego.

## Obywatelska idea aplikacji i godność człowieka

### Cel i deklaracja zasad

Obywatelski mObywatel jest niezależnym narzędziem cyfrowym służącym człowiekowi: porządkowaniu jego dokumentów, informacji, oświadczeń i spraw oraz ułatwianiu świadomego korzystania z praw. Projekt wyrasta z przekonania, że technologia ma pozostawać w służbie osoby ludzkiej, jej godności, wolności, odpowiedzialności, rodziny i dobra wspólnego.

Aplikacja przyjmuje następującą deklarację etyczną:

> Każdy człowiek ma godność, która poprzedza działanie jakiejkolwiek aplikacji, urzędu lub instytucji. Człowiek jest podmiotem praw, a nie przedmiotem systemu. Należy mu się szacunek, równe traktowanie, ochrona życia prywatnego, możliwość wyrażania myśli, ochrony własnych interesów oraz dochodzenia praw na zasadach określonych przez prawo. W duchu prawdy, sprawiedliwości i miłości bliźniego projekt ma wspierać korzystanie z tych praw i odpowiedzialne wypełnianie obowiązków.

W perspektywie wiary chrześcijańskiej człowiek może wyznawać, że jest dzieckiem Bożym i że jego godność ma źródło w Stwórcy. Aplikacja szanuje takie przekonanie oraz wolność sumienia i religii. Określenie „Syn Boży” w odniesieniu do konkretnej osoby może być elementem jej osobistej deklaracji religijnej; program nie rozstrzyga twierdzeń teologicznych ani nie ustanawia ich skutków prawnych.

### Prawa, które projekt ma pomagać rozpoznawać i wykonywać

Projekt odwołuje się w szczególności do następujących zasad polskiego porządku prawnego:

- **Godność człowieka** — przyrodzona i niezbywalna godność człowieka stanowi źródło wolności i praw człowieka i obywatela; jej poszanowanie i ochrona są obowiązkiem władz publicznych (Konstytucja RP, art. 30).
- **Równość i zakaz dyskryminacji** — wszyscy są wobec prawa równi i mają prawo do równego traktowania przez władze publiczne (art. 32).
- **Wolności i prawa człowieka** — korzystanie z konstytucyjnych wolności i praw przysługuje każdemu, kto znajduje się pod władzą Rzeczypospolitej Polskiej, z uwzględnieniem warunków i wyjątków wynikających z Konstytucji (art. 37).
- **Wolność sumienia i religii, wyrażania poglądów oraz ochrona życia prywatnego** — korzystanie z tych wolności odbywa się w granicach i na zasadach określonych w Konstytucji (w szczególności art. 47, 53 i 54).
- **Prawa obywatelskie** — obywatelstwo polskie, prawa wyborcze, dostęp do służby publicznej i inne uprawnienia obywatelskie wynikają z Konstytucji oraz ustaw; ich zakres zależy od konkretnego prawa i spełnienia ustawowych przesłanek.
- **Podmiotowość cywilnoprawna osoby fizycznej** — każdy człowiek od chwili urodzenia ma zdolność prawną (Kodeks cywilny, art. 8). Zdolność do czynności prawnych oraz możliwość samodzielnego dokonywania określonych czynności podlegają odrębnym regułom ustawowym.

Powyższe zasady nie są nadawane przez aplikację. Przysługują na podstawie właściwych norm prawnych albo — w wymiarze etycznym i religijnym — są wyrażane jako przekonania i wartości. Poszczególne prawa mogą podlegać ograniczeniom przewidzianym w Konstytucji i ustawach. Aplikacja nie może sama przyznać obywatelstwa, pełnej zdolności do czynności prawnych, statusu urzędowego, tytułu, uprawnienia zawodowego ani zwolnienia z obowiązków prawnych.

### Znaczenie określeń używanych w projekcie

- **Człowiek** — osoba ludzka, której godność i prawa wymagają poszanowania.
- **Obywatel** — osoba posiadająca obywatelstwo określonego państwa; w tej aplikacji pojęcie to oznacza użytkownika i uczestnika życia publicznego, a nie urzędowo potwierdzony status.
- **Osoba fizyczna** — człowiek będący podmiotem prawa cywilnego, w zakresie określonym przez przepisy.
- **Cywil** — określenie używane potocznie na oznaczenie osoby niebędącej członkiem sił zbrojnych; samo w sobie nie stanowi odrębnego dokumentu ani uniwersalnej kategorii zdolności prawnej.
- **Dziecko Boże / Syn Boży** — określenie o znaczeniu religijnym i teologicznym, którego aplikacja nie przedstawia jako urzędowego statusu ani dowodu prawnego.

### Granice działania i uczciwość wobec użytkownika

Aplikacja ma wspierać samodzielność, przejrzystość i świadome działanie. Może przechowywać dane podane przez użytkownika, tworzyć projekty dokumentów, porządkować informacje oraz ułatwiać dotarcie do usług publicznych. Nie zastępuje organów administracji, sądów, rejestrów państwowych ani profesjonalnych systemów identyfikacji i zaufania.

W szczególności:

1. Wpisanie danych, deklaracji, tytułu lub statusu do aplikacji nie jest urzędowym potwierdzeniem ich prawdziwości.
2. Dokument wygenerowany przez użytkownika jest dokumentem prywatnym lub wzorem, chyba że właściwe przepisy i właściwy organ nadają mu inny status.
3. Kod QR, grafika pieczęci, wzór podpisu i identyfikator lokalny nie są same w sobie podpisem elektronicznym, pieczęcią elektroniczną ani urzędowym poświadczeniem.
4. Podpis elektroniczny ma skutki prawne wyłącznie w zakresie wynikającym z właściwych przepisów i zastosowanej usługi zaufania.
5. Dane z rejestrów publicznych wymagają weryfikacji w źródle właściwym dla danego rejestru.
6. Projekt nie sugeruje afiliacji z administracją państwową ani upoważnienia do wydawania dokumentów urzędowych.

### Podstawowe źródła prawne

- [Konstytucja Rzeczypospolitej Polskiej](https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19970780483) — w szczególności art. 30, 32, 37, 47, 53 i 54.
- [Kodeks cywilny](https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19640160093) — w szczególności art. 8 i przepisy o zdolności do czynności prawnych.

Treść przepisów należy odczytywać w aktualnym brzmieniu, łącznie z właściwymi ustawami szczególnymi i orzecznictwem.

---

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
