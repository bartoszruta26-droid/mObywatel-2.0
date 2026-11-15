SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Audyt: Międzynarodowe zaświadczenie lekarskie

- Data audytu: 2026-11-15
- Repozytorium: `bartoszruta26-droid/mObywatel-2.0`
- Gałąź docelowa wskazana w zleceniu: `zaswaidczenie`
- Stan gałęzi: `zaswaidczenie` istnieje; prace są prowadzone w ramach otwartego, roboczego PR #69.
- Stan PR na 2026-11-15: open, draft, nie scalony. Aktualny punkt kontrolny przed etapem 19: `b3ba8636289ce303ef0e31e7f3282115e72bc588`.
- Zakres audytu: bieżący stan implementacji, wyniki CI i jawnie oznaczone braki. Historyczne wpisy poniżej zachowują kontekst wcześniejszych etapów; ich status należy czytać łącznie z najnowszymi sekcjami.

## 1. Źródła i zakres wzorów

Przeanalizowane załączniki:
1. `PL-USA 7.pdf` — 5 stron. Formularz opinii lekarskiej do celów świadczeń; SSN, dane pacjenta i dokument tożsamości, wywiad lekarski i zawodowy, pomiary, badanie przedmiotowe, układy narządów, opis odchyleń, badania dodatkowe, opieka i ograniczenia.
2. `UA-PL_4.pdf` — 19 stron. Szczegółowy raport medyczny; dane osobowe i zawodowe, rozpoznania z ICD, wywiad wg układów (sercowo-naczyniowy, oddechowy, pokarmowy, endokrynny, moczowo-płciowy, ruchu, nerwowy, psychiczny, onkologiczny i inne), dolegliwości, leczenie i wyniki badań.
3. `PL-UA_4.pdf` — 19 stron. Odpowiednik polsko-ukraiński, potwierdzający strukturę i terminologię raportu.
4. `OpiniaZUSN-11CKanada(pl-en).pdf` — 4 strony. Dane ubezpieczonego i lekarza, diagnozy, istotny wywiad, hospitalizacje, dowody kliniczne (laboratoryjne, RTG, opinie), badanie fizykalne i ograniczenia czynnościowe, konsultacje i leki.
5. `PL-KR5A.pdf` — 7 stron. Formularz koreańskiej instytucji emerytalnej; część wnioskodawcy i lekarza, zgoda/upoważnienie do ujawnienia danych medycznych, przyczyna i początek niepełnosprawności, pierwsze badanie, data wyleczenia/stabilizacji, trwałość, rozpoznanie i stan kliniczny.
6. `KR-PL5A.pdf` — 5 stron. Opinia lekarska koreańsko-polska, dane osobowe, wywiad i badanie przedmiotowe, odchylenia i dokumentacja.
7. `Raport_med__do_PL-CAN_1_(DI)___jezyk__PL-EN.pdf` — 4 strony. Raport Service Canada: applicant / physician sections, rozpoznanie, wywiad, hospitalizacje, dokumentacja wspierająca, wyniki fizykalne i ograniczenia.
8. `e-ZLA-ZUS_wzór.pdf` — 4 strony (w tym opis dostępności). Dane identyfikacyjne i adresowe, okres niezdolności do pracy, hospitalizacja, wskazania, kody, płatnik, miejsce udzielenia świadczeń, lekarz, data, podpis/pieczęć.

Wnioski ze źródeł: wspólny formularz nie powinien kopiować jednego krajowego druku. Powinien być uniwersalnym raportem klinicznym z zestawami pól zgodnymi z ww. wzorami oraz opcjonalnymi sekcjami zależnymi od celu (świadczenia, zdolność do pracy, niepełnosprawność, podróż, konsultacja / ogólne zaświadczenie).

## 2. Cel produktu

Dodać samodzielny kafelek dokumentu specjalnego: **International Medical Certificate** (roboczy identyfikator `international_medical_certificate`). Kafelek otwiera własny edytor wielostronicowego dokumentu, nie grupę i nie ogólny podgląd legitymacji. Etykiety pól formularza i etykiety eksportu mają być w języku angielskim; język treści wypełnianego dokumentu jest wybierany osobno.

Dokument ma przechowywać niezależnie:
- oryginalne wartości wpisane przez wystawcę;
- język oryginału;
- język docelowy / języki docelowe;
- tłumaczenia odpowiadające każdemu polu i każdej stronie;
- status tłumaczenia (pending / machine translated / reviewed / approved);
- historię zmian oraz datę i źródło tłumaczenia.

## 3. Wybór języków

### 3.1 Język wypełniania — pojedynczy RadioButton
- English (`en`)
- Hebrew (`he`, układ RTL)
- German (`de`)
- Spanish (`es`)
- French (`fr`)
- Italian (`it`)
- Polish (`pl`)
- Bulgarian (`bg`)
- Romanian (`ro`)
- Dutch (`nl`)
- Belgian — **wymaga doprecyzowania w UI jako Dutch (Belgium) / French (Belgium) / German (Belgium)**; „Belgian” nie jest odrębnym językiem.
- Portuguese (`pt`)
- Swedish (`sv`)
- Other — pole na kod/nazwę języka i opcjonalny wariant regionalny.

### 3.2 Lista języków docelowych — wielokrotny wybór
Te same języki, z możliwością wybrania wielu języków docelowych. Język oryginału nie może być automatycznie usunięty z danych źródłowych; można go wyłączyć z listy eksportowanych tłumaczeń.

### 3.3 Tłumaczenie
- Interfejs adaptera `TranslationProvider` z wymiennymi implementacjami.
- Kandydaci: Google Cloud Translation API (z kluczem i konfiguracją, nie scraping publicznej strony Google Translate), LibreTranslate / prywatny serwer, ewentualnie inny jawnie skonfigurowany dostawca.
- Automatycznie tłumaczyć wszystkie opisowe pola i treść każdej strony po zmianie treści lub języka; zachować oryginał bez nadpisywania.
- Nie tłumaczyć identyfikatorów, liczb, kodów ICD, numerów dokumentów, wartości pomiarów i dat jako swobodnego tekstu. Zachować format i jednostki.
- Obsłużyć limity znaków, partie, ponawianie, timeout, brak sieci, częściowy błąd i ponowne tłumaczenie zmienionych pól.
- Zapis rejestru tłumaczenia: provider, model/wersja (jeśli dostępna), timestamp, sourceLanguage, targetLanguage, status.
- Nie wykonywać wywołań sieciowych z wątku UI. Użytkownik musi zobaczyć stan tłumaczenia oraz móc edytować i zaakceptować wynik.
- Wrażliwe dane zdrowotne i identyfikacyjne wymagają jawnego potwierdzenia przed wysłaniem do zewnętrznego dostawcy. Preferować tłumaczenie lokalne / prywatny serwer, gdy polityka prywatności tego wymaga.
- PDF powinien zawierać oba zestawy: Original language i Translation, zachowując jednoznaczne parowanie pól. Każda wersja językowa powinna mieć oddzielny nagłówek i oznaczenie statusu tłumaczenia.

## 4. Proponowany model dokumentu

Wersjonowany model danych (np. JSON) o stabilnych kluczach:
- `schemaVersion`, `documentId`, `documentNumber`, `issueDate`, `purpose`, `issuingCountry`, `destinationCountry`.
- `sourceLanguage`, `targetLanguages[]`, `translations[]`.
- `patient`: givenName, familyName, dateOfBirth, sex (opcjonalnie), nationality, address, phone/email (opcjonalnie), identityDocumentType/number/country/expiry, insuranceId (z typem jurysdykcji).
- `consent`: patientAuthorization, scope, recipient, date, signature/consent reference.
- `medicalProvider`: clinicianName, profession, specialty, licenseNumber, issuingAuthority, clinicName, address, contacts, country.
- `clinical`: chiefComplaint, medicalHistory, familyHistory, occupationalHistory, pastIllnesses, injuries, operations, allergies, addictions (opcjonalnie), currentSymptoms, diagnosis[] (display text + ICD code/version + onset date).
- `examination`: date/time, height, weight, BMI (wyliczany), bloodPressure, pulse, respiratoryRate, temperature, oxygenSaturation, generalAppearance, posture, gait, movements, skin, mucousMembranes, lymphNodes, muscleCondition, oedema, dyspnoea, cough.
- `systemsReview`: head, neck, chest, respiratory, cardiovascular, peripheralVessels, abdomen, genitourinary, musculoskeletal, vision, hearing, nervous, mental, endocrine, oncological, other. Każdy wpis: status (normal / abnormal / notExamined / notApplicable), description, date.
- `imagingStudies[]`: modality (X-ray, MRI, CT, Ultrasound, Other), bodyRegion, date, facility, indication, technique/protocol, contrast, findings, impression/conclusion, radiologistName, reportNumber, attachmentIds. Rozróżnić opis badania i opis obrazu / wnioski radiologa.
- `otherTests[]`: laboratory, ECG/Holter/ECHO/Doppler/spirometry/gasometry/other; date, testName, result, unit, referenceRange, provider, attachmentIds.
- `hospitalizations[]`: institution, admission/discharge dates, reason.
- `treatment[]`: medication, dose, frequency, start/end date, procedures, rehabilitation, response.
- `functionalStatus`: mobility, self-care, eating, hygiene, toileting, household, work/study, assistiveDevices, permanent/longTermCare, limitations, scales (instrument, score, date).
- `workCapacity`: occupation, job demands, restrictions, fit/unfit/partiallyFit/undetermined, validFrom/To, recommended adjustments.
- `prognosis`: expected course, permanent/temporary, stabilization/recovery date, planned consultations/tests, followUp.
- `attachments[]`: original reports/images/scans with MIME, size, checksum, encrypted storage reference, consent/retention metadata.
- `signatures`: patient handwritten scan, clinician handwritten scan, signature metadata, seal/stamp scan, signing date/place. Nie utożsamiać obrazu podpisu z podpisem kwalifikowanym.
- `auditTrail[]`: actor, event, timestamp, changed fields, prior/new version.

## 5. Struktura wielostronicowa

1. Document metadata and purpose; issuing institution / recipient.
2. Patient identification and consent / authorization.
3. Medical history and occupational history.
4. Diagnosis and clinical timeline.
5. Physical examination and vital signs.
6. System-by-system examination and deviations.
7. Imaging and diagnostic studies (wiele wpisów).
8. Treatment, medications, hospitalization and supporting records.
9. Functional assessment, assistance needs and work capacity.
10. Prognosis, planned consultations, recommendations and restrictions.
11. Clinician identification, declarations, signature / seal.
12. Patient signature / consent (gdy wymagane).
13+. Attachments index and translated attachments (tylko jeśli wybrane).

Wszystkie strony muszą mieć spójny numer, identyfikator dokumentu, język strony i stopkę; sekcje powinny automatycznie przechodzić na następną stronę bez obcinania danych.

## 6. Załączniki, podpis i eksport

- Dodawanie skanu podpisu odręcznego przez aparat / galerię / wybór pliku; podgląd, przycięcie/obrót, usunięcie i ponowne dodanie.
- Rozdzielić skan podpisu pacjenta od skanu podpisu lekarza; osobne pole pieczęci.
- Możliwość ręcznego wydruku i podpisania dokumentu: eksport/udostępnienie PDF bez podpisu, miejsce na podpis i pieczęć, opcja ponownego zeskanowania podpisanego egzemplarza jako załącznika.
- PDF wielostronicowy: oryginał + wybrane tłumaczenia, indeks załączników, sekcja obrazowania, podpisy, znaki wodne/status tłumaczenia, poprawne fonty Unicode (w tym hebrajski i tekst RTL).
- Android Print Framework (druk systemowy); zapis PDF przez SAF; nazwa pliku bez danych wrażliwych, chyba że użytkownik świadomie ją wybierze.
- Nie deklarować, że ręcznie wklejony obraz podpisu jest podpisem elektronicznym / kwalifikowanym. Opcjonalnie integrować istniejący mechanizm PAdES dopiero po sprawdzeniu jego statusu i testów kryptograficznych.

## 7. Zidentyfikowana architektura repozytorium (stan sprawdzony na `main`)

- `DocumentEditorSchemas.kt`: wersjonowane schematy pól i stabilne klucze, obecnie `DOCUMENT_EDITOR_SCHEMA_VERSION = "2"`; etykiety istniejących pól są polskie.
- `DocumentRouteRegistry.kt`: jawny rejestr tras `GROUP`, `DOCUMENT`, `SPECIALIZED`; test pilnuje tras dla katalogu.
- `DocumentGroupCatalog.kt`: przypisanie kafelków do grup, migracje nazw grup, eksport/import konfiguracji.
- `DocumentGraphicCatalog.kt`: katalog graficznych definicji dokumentów, rodzaj podglądu, tytuł, foto i przypisanie zasobów.
- `DocumentBranding.kt`: cele grafiki i ustawienia marki dokumentu.
- `DocumentOriginalAttachments.kt`: załączniki oryginalne szyfrowane AES/GCM w Android Keystore, limit 25 MB.
- `SignedPdfDocuments.kt`: istniejący magazyn plików PDF szyfrowanych AES/GCM, import PDF, załączanie pliku podpisu, podgląd i archiwizacja.
- `OfficialDocuments.kt`, `FamilyEntityDocuments.kt`, `ChurchDocuments.kt`: odrębne modele i edytory dokumentów specjalnych.
- `DocumentRouteRegistryTest.kt`, `DocumentCatalogTest.kt`, `DocumentGroupCatalogTest.kt`, `DocumentGraphicCatalogTest.kt`: istniejące testy spójności katalogu, tras, pól i grafiki.
- `app/build.gradle.kts`: Compose, AndroidX, ZXing; brak wskazanej biblioteki tłumaczeń/PDF w dependencies na sprawdzonym pliku. Projekt ma workflow `.github/workflows/android-build.yml` uruchamiający JVM unit tests i `assembleDebug`.

## 8. Ocena luk i ryzyk

| Obszar | Stan obecny | Wymagana praca |
|---|---|---|
| Kafelek i routowanie | Mechanizm istnieje | Dodać ID do katalogu, grupy, tras i celów graficznych |
| Dedykowany edytor | Wzorce istnieją | Nowy ekran/sekcje i trwały model dokumentu |
| Język oryginału | Brak sprawdzonej funkcji | RadioButton i wartości BCP-47 |
| Tłumaczenia | Brak sprawdzonego dostawcy | Adapter, prywatność, retry, cache, statusy |
| Zachowanie obu wersji | Brak modelu tłumaczeń | Przechowywać oryginał i tłumaczenie obok siebie |
| Badania obrazowe | Załączniki ogólne istnieją | Dedykowane rekordy RTG/MRI/CT/USG/Other |
| Podpis odręczny | PDF store potrafi przechować oddzielny plik podpisu | Wbudować osobne pola patient/clinician/seal i podpis na wydruku |
| PDF/druk | Istniejący mechanizm Signed PDF | Sprawdzić możliwość generowania PDF, nie tylko importu; dodać wydruk i wielojęzyczny layout |
| Bezpieczeństwo zdrowotne | AES/GCM dla części załączników | Szyfrować także model kliniczny/tłumaczenia, kontrolować eksport i logi |
| Dostępność / RTL | Nieweryfikowane | Layout RTL hebrajskiego, skalowanie tekstu, semantyka formularzy |
| Walidacja medyczna | Brak | Testy pól, jednostek, dat, ICD i przegląd kliniczny |

## 9. Plan implementacji — etapy i kryteria odbioru

### Etap 0 — przygotowanie gałęzi i punktu bazowego
- Odnaleźć lub utworzyć uzgodnioną gałąź `zaswaidczenie` od właściwego commita integracyjnego.
- Sprawdzić różnice względem `main`; nie nadpisywać istniejącej pracy.
- Wynik: gałąź robocza i zapisany SHA bazowy.

### Etap 1 — model domenowy i trwałość
- Dodać nowe pliki modelu z wersjonowaniem, serializacją, migracją i walidacją.
- Zintegrować z bezpiecznym magazynem aplikacji; nie umieszczać danych medycznych w SharedPreferences jako jawnego JSON.
- Zachować istniejące formaty archiwum/vCard i kompatybilność starych wpisów.
- Testy round-trip, migracji, uszkodzonych danych i braku pól.

### Etap 2 — katalog i kafelek
- Dodać `international_medical_certificate` do `documentDisplayOptions`, `DocumentRouteRegistry`, grupy „Służba zdrowotna”, `GraphicDocumentCatalog`, `documentBrandTargets`, schematów i ustawień widoczności.
- Kafelek ma własną ikonę wektorową i tło medyczne; zachować wspólny system wyglądu dokumentów.
- Test: unikalne ID, jawna trasa DOCUMENT, pola edytora, domyślna grupa i zasób ikony/tła.

### Etap 3 — formularz wielostronicowy
- Zaimplementować sekcje z rozdziału 5, powtarzalne listy badań, rozpoznań, hospitalizacji i leków.
- Etykiety pól oraz nazwy techniczne w UI po angielsku.
- RadioButton języka źródłowego i multiselect języków docelowych.
- Walidacja dat, zakresów i jednostek; brak automatycznego uzupełniania nieznanych danych.
- Testy formularza, rotacji ekranu, procesu wznawiania oraz dużej ilości tekstu.

### Etap 4 — obrazowanie i załączniki medyczne
- Dedykowany formularz dla X-ray, MRI, CT, Ultrasound, Other.
- Pola: body region, study date, facility, indication, technique, contrast, findings, impression, radiologist, report number.
- Dodawanie oryginalnych plików i skanów, kontrola MIME/rozmiaru/sum kontrolnych, szyfrowanie.
- Testy wszystkich modalności, wielu badań i błędnych załączników.

### Etap 5 — tłumaczenia
- Provider interface i konfiguracja klucza/endpointu.
- Tłumaczenie automatyczne każdej opisowej wartości i treści kolejnych stron, zachowanie oryginału.
- Wielojęzyczne parowanie pól w PDF, oznaczenie tłumaczenia maszynowego.
- Testy języków, kodów, liczb, dat, nazw własnych, odmowy zgody, offline, limitów i błędów częściowych.
- Osobny test RTL dla Hebrew.
- Brak pozorowania tłumaczenia, jeżeli dostawca jest nieustawiony.

### Etap 6 — PDF, podpis i druk
- Generator wielostronicowego PDF z oryginałem i wszystkimi wybranymi tłumaczeniami.
- Podpis odręczny pacjenta i lekarza, pieczęć, możliwość wydruku pustego pola na podpis.
- Android Print Framework i zapis pliku przez SAF.
- Walidacja renderowania Unicode/RTL, łamania stron, stopki, obrazów i wysokiej rozdzielczości.
- Jasny status: `Unsigned`, `Hand-signed scan attached`, `Digitally signed` (tylko po prawdziwej walidacji podpisu).

### Etap 7 — bezpieczeństwo i zgodność
- Przegląd ochrony danych zdrowotnych, zgody na tłumaczenie zewnętrzne, minimalizacji i retencji.
- Redakcja danych na podglądzie/udostępnianiu, blokowanie przypadkowych logów.
- Test archiwizacji/odtwarzania z zaszyfrowanymi załącznikami.
- Zastrzeżenie: formularz nie zastępuje urzędowego druku, jeśli instytucja wymaga własnego wzoru; nie jest automatycznie uznawany w każdym kraju.

### Etap 8 — testy końcowe i integracja
- `gradle :app:testDebugUnitTest`
- `gradle :app:assembleDebug`
- Testy instrumentalne na Androidzie: kafelek, routing, walidacja, formularz, PDF/druk i podpis.
- Ręczny przegląd przykładowego dokumentu z danymi testowymi, bez danych osobowych.
- Aktualizacja tego audytu o wyniki i SHA; PR pozostaje otwarty do pełnej weryfikacji.

## 10. Testy akceptacyjne obowiązkowe

1. Kafelek widoczny w wybranej grupie i otwiera własny formularz.
2. Wszystkie nazwy pól UI są angielskie; wartości można wpisać w wybranym języku.
3. Dostępne wszystkie języki z listy oraz „Other”; „Belgian” prowadzi do wyboru wariantu NL/FR/DE.
4. Można wybrać więcej niż jeden język docelowy.
5. Oryginał jest zachowany bit-for-bit na poziomie wartości tekstowej; tłumaczenia są osobnymi obiektami.
6. Wszystkie sekcje i pola powtarzalne są zachowane w dokumencie, w tym badania RTG/MRI/CT/USG/inne.
7. Zmiana oryginału oznacza tłumaczenie jako nieaktualne i uruchamia ponowne tłumaczenie po zgodzie.
8. PDF zawiera wszystkie wypełnione pola wersji źródłowej i tłumaczeń oraz wyraźne rozdzielenie języków.
9. Hebrew renderuje się poprawnie od prawej do lewej.
10. Skan podpisu można dodać, zobaczyć, usunąć i wydrukować; wydruk pozostawia miejsce na podpis ręczny.
11. Błędy sieci/translatora nie usuwają oryginału i nie blokują zapisu formularza.
12. Dane kliniczne i załączniki nie wyciekają do logów; brak zewnętrznego tłumaczenia bez świadomej zgody.

## 11. Bieżący status

- [x] Przegląd ośmiu załączników i zebranie wspólnego zakresu formularza.
- [x] Rozpoznanie mechanizmów katalogu, tras, schematów pól, grafiki, załączników i PDF w repozytorium.
- [x] Zidentyfikowanie wymogu gałęzi jako blokady — `zaswaidczenie` nie istnieje w widocznej liście gałęzi.
- [x] Przygotowanie planu i kryteriów odbioru.
- [ ] Implementacja kafelka i edytora.
- [ ] Integracja tłumaczeń.
- [ ] Generator PDF / druk / podpis.
- [ ] Testy, build i PR.

**Decyzja techniczna:** najpierw uzyskać poprawną gałąź bazową, następnie realizować etapy 1–8. Nie wdrażać automatycznego tłumaczenia zewnętrznego bez jawnej zgody użytkownika i widocznego ostrzeżenia o maszynowym charakterze przekładu.


---

## 12. Aktualizacja wykonania — 2026-11-15

### Gałąź robocza
- [x] Utworzono gałąź `zaswaidczenie` od aktualnego `main` (SHA bazowy: `64632d45f796b5e7d77c8d48c675ac83061b5f8d`).
- [x] Zmiany z pierwszego etapu zapisano na tej gałęzi; `main` nie był dalej modyfikowany.
- [ ] Nie scalono innych rozbieżnych gałęzi roboczych — ich historie nie są liniowe względem `main`.

### Wykonane etapy
- [x] Dodano kafelek `international_medical_certificate` do katalogu dokumentów, z angielską nazwą i istniejącym zasobem ikony medycznej.
- [x] Dodano cel przypisania grafiki dokumentu oraz definicję podglądu typu RECORD.
- [x] Przypisano dokument do grupy `Służba zdrowotna`.
- [x] Rejestr tras obejmuje nowy dokument przez istniejące generowanie tras z `documentBrandTargets`; nie dodano osobnej trasy GROUP.
- [x] Dodano angielskojęzyczny schemat pól obejmujący dane dokumentu, pacjenta, lekarza, wywiad, rozpoznanie, parametry badania, ograniczenia funkcjonalne, rokowanie, deklarację oraz referencje skanów podpisu i pieczęci.
- [x] Dodano model domenowy z osobnym przechowywaniem pól źródłowych i tłumaczeń, statusami tłumaczeń, listą języków, modelem badań obrazowych i referencjami podpisów.
- [x] Lista języków modelu obejmuje języki wymagane oraz rozróżnia belgijskie warianty Dutch (Belgium), French (Belgium) i German (Belgium); „Belgian” nie jest traktowany jako osobny język.
- [x] Dodano testy jednostkowe modelu: lista języków, RTL Hebrew, unieważnianie tłumaczenia po zmianie źródła, modalności obrazowania i walidacja kluczy tłumaczeń.
- [x] Rozszerzono testy katalogu, tras i podglądu.

### Kontrola spójności po zapisaniu zmian
Weryfikacja odczytem plików z gałęzi `zaswaidczenie` potwierdziła obecność:
- kafelka i wpisu w katalogu;
- grupy zdrowotnej;
- celu graficznego i definicji RECORD;
- schematu angielskich pól;
- modelu języków i wariantów belgijskich;
- pięciu modalności obrazowania;
- testów tras i katalogu graficznego.

### Korekta oceny — co nadal NIE jest ukończone
Powyższe zmiany stanowią **etap katalogowo-modelowy**, a nie kompletny działający formularz. Nie należy uznawać za wykonane:
- dedykowanego ekranu edycji z radiobuttonami języka źródłowego i wielokrotnym wyborem języków docelowych;
- trwałego, szyfrowanego zapisu całego modelu klinicznego;
- integracji rzeczywistego dostawcy tłumaczeń i automatycznego tłumaczenia stron;
- osobnego formularza badań obrazowych i zarządzania ich załącznikami;
- dodawania skanów podpisów przez aparat/galerię;
- generowania wielostronicowego PDF i integracji z Android Print Framework;
- testów kompilacji i uruchomienia na urządzeniu.

### Wyniki testów i ograniczenia środowiska
- [x] Wykonano kontrolę strukturalną zmian przez ponowny odczyt plików z gałęzi.
- [ ] Nie uzyskano wyniku `gradle :app:testDebugUnitTest` ani `gradle :app:assembleDebug` w tej sesji.
- [ ] GitHub Actions nie zwrócił workflow runów dla gałęzi po zapisaniu commitów; brak wyniku CI nie jest dowodem przejścia testów.
- [ ] Testy instrumentalne i kontrola RTL na urządzeniu pozostają do wykonania.

### Następne prace
1. Zaimplementować dedykowany ekran Compose dla dokumentu, w tym wybór języka źródłowego jako RadioButton oraz wielokrotny wybór języków docelowych.
2. Dodać trwały magazyn danych klinicznych i badań, szyfrowanie oraz migracje schematu.
3. Dodać bezpieczny adapter tłumaczeń; nie wysyłać danych do dostawcy zewnętrznego bez wyraźnej zgody i konfiguracji.
4. Dodać załączniki badań i podpisów przez systemowe selektory plików.
5. Zbudować generator PDF i wydruk; zapewnić Unicode i RTL.
6. Uruchomić testy JVM, kompilację APK i testy urządzeniowe, a następnie zaktualizować status na podstawie rzeczywistych wyników.

**Status całości:** częściowo wykonano — katalog i model domenowy są dodane; formularz, tłumaczenia, PDF, podpis i walidacja kompilacyjna pozostają otwarte.


---

## 13. Aktualizacja po kontroli CI i wdrożeniu pierwszego ekranu — 2026-11-15

### Ustalenia z pierwszego uruchomienia CI
- Workflow GUI i workflow budowania APK zakończyły się błędem na SHA `a3166529bd793614385b995b92041e60c1e381a4`.
- Kompilacja Kotlin aplikacji przeszła; awaria nastąpiła w testach jednostkowych: 110 testów, 2 niepowodzenia.
- `DocumentGraphicCatalogTest.credentialDocumentsUseCredentialPreview` błędnie wymagał obsługi zdjęcia od wszystkich definicji, także dokumentów typu RECORD. Poprawiono asercję: wymóg dotyczy wyłącznie dokumentów typu CREDENTIAL.
- `DocumentGroupCatalogTest.documentEditorFieldKeysAreStableAndUniqueAcrossCatalog` wykrył przekroczenie limitu 64 znaków przez długie klucze pól skanu podpisów. Skrócono klucze do `clinicianSignRef` i `patientSignRef`, zachowując angielskie etykiety pól.
- Kolejne uruchomienie compliance wykazało brak metadanych SPDX w pliku niniejszego audytu; dodano nagłówek CC0-1.0. Pozostał osobny, wcześniej istniejący plik audytu rachunków płatniczych bez metadanych SPDX — to niezależny problem repozytorium, nie zmiana zakresu tego dokumentu.

### Wykonany etap interfejsu
- [x] Dodano dedykowany ekran Compose dla kafelka `International Medical Certificate`; kafelek otwiera ten ekran zamiast ogólnego podglądu legitymacji.
- [x] Dodano wybór jednego języka oryginału przez RadioButton.
- [x] Dodano wielokrotny wybór języków docelowych przez Checkbox, z wariantami belgijskimi NL/FR/DE i opcją „Other” dla języka źródłowego.
- [x] Dodano pola formularza z angielskimi etykietami na podstawie schematu dokumentu.
- [x] Dodano przycisk zapisu i informację, że rzeczywiste tłumaczenie automatyczne nie jest jeszcze aktywne.
- [x] Dodano osobny magazyn szyfrowany AES-GCM z kluczem Android Keystore i plikiem w `noBackupFilesDir`; dane zaświadczenia nie są mieszane z ogólnym archiwum profilu.
- [x] Ekran nie wysyła danych medycznych do żadnego zewnętrznego dostawcy tłumaczeń.

### Zakres nadal otwarty
- [ ] Powtarzalne formularze badań obrazowych (RTG, MRI, CT, USG, inne) i załączniki obrazów/opisów.
- [ ] Import skanów podpisu pacjenta, lekarza i pieczęci przez aparat / selektor plików.
- [ ] Zapis i odtwarzanie pełnego modelu domenowego wraz z tłumaczeniami, listą badań, załącznikami i historią zmian.
- [ ] Rzeczywisty dostawca tłumaczeń, zgoda na przekazanie danych oraz statusy weryfikacji.
- [ ] Wielostronicowy PDF, obsługa Hebrew/RTL i Android Print Framework.
- [ ] Testy instrumentalne oraz walidacja na urządzeniu.

### Weryfikacja zmian bieżącej iteracji
- [x] Ponownie odczytano pliki ekranu, magazynu i punktu nawigacji z gałęzi `zaswaidczenie`.
- [x] Potwierdzono, że ekran korzysta z osobnego magazynu AES-GCM, a nie z ogólnego `DemoIdentityStore`.
- [ ] Testy i build dla najnowszego SHA — oczekują na wynik GitHub Actions; poprzednie uruchomienia po kolejnych commitach zostały anulowane przez nowsze uruchomienia.
- [ ] Testy urządzeniowe i APK — nie uzyskano jeszcze pozytywnego wyniku.

**Status:** katalog, model danych i pierwszy ekran formularza są dodane. Nie uznaje się jeszcze za gotowe tłumaczeń, obrazowania, podpisów, PDF, druku ani wydania APK. PR #69 pozostaje otwarty jako WIP.


---

## 14. Aktualizacja kontroli repozytorium — 2026-11-15

### Dodatkowa korekta bezpieczeństwa danych
- [x] Zmieniono inicjalizację formularza tak, aby odczyt magazynu używał `loadStrict` i zachowywał informację o błędzie odszyfrowania.
- [x] Przy błędzie odczytu formularz wyświetla ostrzeżenie i blokuje zapis. Chroni to istniejący zaszyfrowany plik przed przypadkowym zastąpieniem pustymi danymi po błędzie klucza lub integralności.
- [x] Potwierdzono, że zapis nadal wykorzystuje osobny klucz AES-GCM z Android Keystore.

### Zgodność REUSE
- [x] Kontrola REUSE wykazała brak metadanych SPDX w niniejszym audycie oraz w istniejącym audycie rachunków płatniczych.
- [x] Dodano nagłówki SPDX do obu plików Markdown.
- [x] Workflow `Repository compliance (alfa)` zakończył się powodzeniem dla SHA `3a210afbd5c650ff2506f2f6d856611f2a66b4e3`.

### Wyniki Android CI
- [x] Workflow `Android prototype build` zakończył się powodzeniem dla SHA `3a57c34992d8590641341f5778c7ff3a7695beee`: testy JVM przeszły, a `assembleDebug` i przesłanie artefaktu APK zakończyły się powodzeniem.
- [x] Workflow `Android GUI validation` wykonał testy jednostkowe z wynikiem pozytywnym dla SHA `3a57c34992d8590641341f5778c7ff3a7695beee`.
- [ ] Testy Compose na emulatorze nie mają jeszcze wyniku pozytywnego — wykonanie zostało anulowane przy nowszym uruchomieniu CI.
- [ ] Po korekcie zabezpieczenia odczytu uruchomiono nowe workflow dla SHA `3a210afbd5c650ff2506f2f6d856611f2a66b4e3`; build Android i testy GUI są w toku. Zmiana zabezpieczenia z commitu `5f8d36f` wymaga osobnego potwierdzenia w CI.
- [ ] APK z SHA `3a57c349` jest wynikiem wcześniejszego, pozytywnego buildu; nie należy utożsamiać go z artefaktem najnowszego commitu.

### Ograniczenia funkcjonalne — bez zmian
Formularz nie obsługuje jeszcze rzeczywistego importu obrazów, skanów podpisów i pieczęci, automatycznego tłumaczenia, wielostronicowego PDF ani wydruku. Model domenowy badań obrazowych istnieje, ale interfejs do dodawania i zarządzania badaniami oraz załącznikami nie jest jeszcze ukończony.

**Status iteracji:** formularz bazowy i szyfrowany zapis są zbudowane; kontrola REUSE przeszła; testy JVM i APK przeszły na poprzednim commicie; końcowy wynik testów GUI i ponowny build po ostatniej korekcie nadal oczekiwane. PR #69 pozostaje otwarty jako WIP.


---

## 15. Etap formularza badań obrazowych — 2026-11-15

### Wykonane
- [x] Dodano sekcję `Medical Imaging Studies` do dedykowanego formularza.
- [x] Użytkownik może dodać wiele osobnych badań i wybrać modalność: X-ray, MRI, CT, Ultrasound lub Other.
- [x] Dodano pola: Body Region, Examination Date, Imaging Facility, Clinical Indication, Findings i Impression.
- [x] Dodane badania są serializowane do JSON i zapisywane razem z pozostałymi danymi w szyfrowanym magazynie AES-GCM.
- [x] Lista badań jest odtwarzana po ponownym otwarciu formularza; można usuwać pozycje przed zapisem.
- [x] Dane badań i pól dokumentu nadal pozostają w lokalnym magazynie; brak transmisji do usług zewnętrznych.

### Ograniczenia tej części
- [ ] Nie ma jeszcze selektora plików ani importu obrazów RTG/MRI/CT/USG i opisów w formie załączników.
- [ ] Pola `technique`, `contrast`, `radiologistName`, `reportNumber` i `attachmentIds` są zachowane w modelu/serializacji, ale nie mają jeszcze pełnego interfejsu edycji.
- [ ] Należy dodać walidację dat, limitów rozmiaru oraz obsługę błędnego JSON bez pomijania istniejących danych.
- [ ] Nowy ekran wymaga ponownego potwierdzenia kompilacją i testami Android CI.

### Kontrola repozytorium po etapie
- [x] REUSE compliance: PASS dla SHA `c1d2e5eb7a60ba2c8c5a54b0bd8dfb8266c6402d`.
- [x] Wcześniejszy build APK: PASS dla SHA `3a57c34992d8590641341f5778c7ff3a7695beee`; nie jest to jeszcze dowód kompilacji nowej sekcji obrazowania.
- [ ] Build Android, testy JVM i Compose UI dla nowego formularza badań obrazowych — oczekują na wynik workflow uruchomionego po commicie `2f4f1c9`.

**Status:** formularz podstawowy i wielokrotna lista badań obrazowych są zaimplementowane, ale załączniki, skany podpisów, automatyczne tłumaczenie, PDF/druk i testy końcowe pozostają otwarte. PR #69 pozostaje WIP.


---

## 16. Ochrona integralności danych badań obrazowych — 2026-11-15

- [x] Dekoder listy badań obrazowych zgłasza teraz błąd niepoprawnego JSON zamiast po cichu zwracać pustą listę.
- [x] Błąd odszyfrowania magazynu lub odczytu zapisanej listy badań blokuje przycisk zapisu, chroniąc wcześniejsze dane przed zastąpieniem pustą zawartością.
- [x] REUSE compliance: PASS dla SHA `b08788aba9d4a01c5904895dc3d3a9f65a37f289`.
- [ ] Testy JVM, kompilacja APK i testy Compose dla formularza obrazowania: workflow dla SHA `0bc922e60135ba5ed32fef5efc1a216278579f4d` są uruchomione; końcowego wyniku jeszcze brak.
- [ ] Pozytywny build i testy muszą potwierdzić sekcję obrazowania oraz blokadę zapisu po błędach odczytu.

**Status:** ukończono interfejs listy badań obrazowych i ochronę przed utratą danych po błędzie dekodowania. Załączniki, podpisy, tłumaczenia, PDF i druk pozostają otwarte. PR #69 pozostaje WIP i nie jest scalony.

---

## 17. Korekta kompilacji dekodera i ponowna walidacja — 2026-11-15

### Usterka wykryta w CI
- [x] Dla SHA `86a7f367676bf9817ac3309e87145b16028cb600` workflow `Android prototype build` i `Android GUI validation` zakończyły się błędem kompilacji Kotlin.
- [x] Komunikat kompilatora: `InternationalMedicalCertificateEditorScreen.kt:314:1 Missing return statement`.
- [x] Przyczyna: `decodeMedicalImagingStudies` wykonywała `map { ... }`, ale nie zwracała utworzonej listy.
- [x] Poprawiono funkcję przez jawne `return (0 until array.length()).map { ... }`.
- [x] Poprawkę zapisano w commicie `c463c42323a9dfcc061375a918cc70a9d8ca7a36` na gałęzi `zaswaidczenie`.

### Kontrola po poprawce
- [x] Ponownie odczytano plik źródłowy i potwierdzono obecność instrukcji `return` w dekoderze.
- [x] REUSE compliance: PASS dla SHA `c463c42323a9dfcc061375a918cc70a9d8ca7a36`.
- [ ] Workflow `Android prototype build` dla SHA `c463c42323a9dfcc061375a918cc70a9d8ca7a36`: testy JVM w toku; wynik budowania APK jeszcze niepotwierdzony.
- [ ] Workflow `Android GUI validation` dla SHA `c463c42323a9dfcc061375a918cc70a9d8ca7a36`: testy JVM zakończone i rozpoczęły się testy Compose na emulatorze; końcowy wynik jeszcze niepotwierdzony.
- [ ] Nie uznaje się jeszcze poprawki za zweryfikowaną końcowo, dopóki oba workflow Androida nie zakończą się powodzeniem.

### Stan PR i zakres dalszych prac
- [x] Zaktualizowano opis PR #69 o przyczynę błędu, commit naprawczy i bieżący status walidacji.
- [x] PR #69 pozostaje otwarty, jako Draft/WIP, bez scalania.
- [ ] Import załączników obrazowych i raportów do konkretnego zaświadczenia oraz ich powiązanie z poszczególnymi badaniami.
- [ ] Import skanów podpisu pacjenta, podpisu lekarza i pieczęci.
- [ ] Rozszerzenie pól badania o technique, contrast, radiologistName i reportNumber w interfejsie.
- [ ] Walidacja dat i pól klinicznych oraz testy odtwarzania po ponownym uruchomieniu.
- [ ] Dostawca tłumaczeń z jawną zgodą na przekazanie danych, oznaczeniem tłumaczenia maszynowego i zachowaniem oryginału.
- [ ] Wielostronicowy PDF, Hebrew/RTL, podpisy i Android Print Framework.

**Status:** błąd kompilacji z poprzedniego commitu został poprawiony. Zgodność REUSE przeszła. Wyniki testów Androida dla commitu naprawczego oczekują na zakończenie CI; nie deklaruje się jeszcze gotowości APK ani pełnej funkcjonalności dokumentu.

---

## 18. Rozszerzenie danych raportu obrazowego — 2026-11-15

### Wykonane zmiany
- [x] Dodano do formularza badania obrazowego pola `Technique`, `Contrast`, `Radiologist Name` i `Report Number`.
- [x] Pola są przekazywane do modelu `MedicalImagingStudy`, który już zawierał odpowiednie właściwości.
- [x] Rozszerzono kartę podsumowania badania o prezentację tych danych, jeśli zostały podane.
- [x] Po dodaniu badania pola formularza są czyszczone, a wartości pozostają w dodanym rekordzie.
- [x] Rozszerzono test jednostkowy modelu o sprawdzenie zachowania czterech pól raportu.
- [x] Zmiany kodu i testu zapisano na gałęzi `zaswaidczenie` w commitach `8f5df54d8fce5eb5beca1a8aeed3d0353d298166` i `bcd78729ba78941e6965f32618fd162d3df3a937`.

### Kontrola CI i status
- [x] Dla poprzedniego kodu `f58f3b3531e60bcfd0e76a66739032e6fb61e587`: `assemble-debug` PASS, `Repository compliance` PASS.
- [x] Dla `f58f3b3`: testy JVM zakończone powodzeniem w workflow Android GUI; dalszy etap emulatora został anulowany przy aktualizacji gałęzi.
- [ ] Dla aktualnego commitu `bcd78729ba78941e6965f32618fd162d3df3a937`: wymagane ponowne potwierdzenie testów jednostkowych, kompilacji APK, testów Compose/emulatora i REUSE.
- [ ] Testy interfejsu formularza dla czterech nowych pól nie są uznawane za wykonane wyłącznie na podstawie testu modelu; wymagają testu UI lub kontroli emulatorowej.

### Pozostałe prace
- [ ] Walidacja dat badania i wymaganych pól klinicznych.
- [ ] Import załączników obrazowych i raportów oraz powiązanie plików z rekordami badań.
- [ ] Import skanów podpisu pacjenta, lekarza i pieczęci.
- [ ] Dostawca tłumaczeń z jawną zgodą, bez automatycznego wysyłania danych medycznych.
- [ ] Wielostronicowy PDF, poprawna obsługa Hebrew/RTL, podpisy i Android Print Framework.
- [ ] Testy odtwarzania danych po ponownym uruchomieniu oraz testy odporności magazynu szyfrowanego.

**Status:** rozszerzono formularz i test modelu o brakujące metadane raportu obrazowego. Nie potwierdzono jeszcze testami UI poprawnego działania nowych pól ani końcowego działania na urządzeniu. PR #69 pozostaje otwarty i niescalony.


---

## 19. Walidacja dat badań obrazowych — 2026-11-15

### Wykonane zmiany
- [x] Dodano walidator daty badania w formacie ISO `YYYY-MM-DD`, z kontrolą rzeczywistej daty kalendarzowej (bez akceptowania np. 30 lutego).
- [x] Niepoprawna data wpisana w formularzu jest oznaczana komunikatem błędu i blokuje dodanie rekordu badania.
- [x] Puste pole daty pozostaje dopuszczalne, ponieważ formularz na tym etapie nie wymusza daty jako pola obowiązkowego.
- [x] Dodano testy daty przestępnej, daty prawidłowej, nieistniejących dni/miesięcy, błędnego formatu i dodatkowego tekstu.
- [x] Zmiany zapisano w commitach: `03b536ffa74c60f85148d05d588e7c9dfe5d046b` (formularz/walidator), `c3a72bf8d1559fb47d35ca42fd4cfb4423054c6f` (pierwszy zapis testu) oraz `791c90a14c6f09421d798ca2818cbed2653f2868` (korekta położenia testu wewnątrz klasy JUnit).

### Weryfikacja
- [x] Sprawdzono ręcznie logikę walidatora: parser działa w trybie niepobłażliwym, a pozycja parsowania musi obejmować cały ciąg.
- [x] Przycisk dodania badania jest aktywny przy pustej dacie lub poprawnej dacie ISO; jest nieaktywny dla niepoprawnej daty.
- [ ] Dla commitu `54a8a112042c95f8916f21a4d4c405e19383a9fe` workflow Androida i GUI rozpoczęły się, ale zostały anulowane po kolejnym commicie poprawiającym położenie testu.
- [ ] Dla najnowszego commitu `791c90a14c6f09421d798ca2818cbed2653f2868` należy potwierdzić ponowne uruchomienie i końcowy wynik testów JVM, kompilacji APK oraz Compose/emulatora.

### Kolejne zadania
- [ ] Rozszerzyć testy serializacji/deserializacji wszystkich pól obrazowania (w tym technique, contrast, radiologistName, reportNumber) jako pełny test round-trip, a nie tylko test właściwości modelu.
- [ ] Dodać import plików raportów/obrazów i trwale przechowywać załączniki w prywatnym magazynie aplikacji, z limitem rozmiaru, kontrolą MIME i powiązaniem z rekordem badania.
- [ ] Rozdzielić załączniki źródłowe od skanów podpisów pacjenta/lekarza i pieczęci.
- [ ] Dodać walidację pól wymaganych zależnie od celu zaświadczenia.
- [ ] Zrealizować wielostronicowy PDF, Hebrew/RTL i Android Print Framework.
- [ ] Tłumaczenia pozostają nieaktywne do czasu wdrożenia dostawcy, jawnej zgody na przekazanie danych i rejestru tłumaczeń.

**Status etapu 19:** zmiana kodu i testów została zapisana. Nie uznaje się jej za zweryfikowaną przez CI, dopóki workflow dla najnowszego SHA nie zakończy się powodzeniem. PR #69 pozostaje otwarty, Draft/WIP i niescalony.


---

## 20. Zakończenie walidacji CI i test pełnego odtwarzania badań — 2026-11-15

### Wynik wcześniejszego workflow
- [x] Workflow Android GUI dla SHA `14c9ab7651b8cbe4c932d74baaeb75af01bb3db0` zakończył się statusem `completed / success`.
- [x] Testy jednostkowe, testy Compose na emulatorze i kroki budowania/archiwizacji APK w tym workflow zakończyły się powodzeniem.
- [x] PR #69 pozostaje otwarty, Draft/WIP i niescalony.

### Wykonany następny etap — round-trip JSON
- [x] Udostępniono funkcje kodowania i dekodowania badań obrazowych w zakresie `internal`, aby testy modułu mogły sprawdzać rzeczywistą serializację.
- [x] Dodano test wieloelementowej listy badań, porównujący obiekty przed i po cyklu JSON encode/decode.
- [x] Test obejmuje modalność, identyfikator, lokalizację anatomiczną, datę, placówkę, wskazanie, technikę, kontrast, opis, wnioski, radiologa, numer raportu oraz listę identyfikatorów załączników; sprawdza także puste pola i pustą listę załączników.

### Weryfikacja
- [x] Potwierdzono wynik sukcesu workflow poprzedniego SHA `14c9ab7651b8cbe4c932d74baaeb75af01bb3db0`.
- [ ] Nowy test round-trip wymaga uruchomienia testów JVM, kompilacji APK i emulatora dla nowego commitu; nie należy przypisywać mu wyniku poprzedniego workflow.
- [ ] Następny zakres: import załączników do rekordu badania z trwałym powiązaniem ID, kontrolą MIME/rozmiaru i szyfrowaniem; następnie osobne skany podpisów i pieczęci.

**Status:** walidacja wcześniejszego etapu 19 zakończona powodzeniem. Test round-trip dodany do kodu i oczekuje na CI. PR #69 nadal pozostaje otwarty i niescalony.


---

## 21. Korekta testu round-trip i ponowne uruchomienie walidacji — 2026-11-15

### Korekta wykonania
- [x] Kontrola odczytu wykazała, że wcześniejszy zapis testu round-trip nie umieścił testu w klasie JUnit; nie uznano go za wykonany.
- [x] Poprawiono zapis i ponownie odczytano plik testowy z gałęzi. Test `imagingStudiesPreserveEveryFieldAcrossJsonRoundTrip` jest obecny wewnątrz `InternationalMedicalCertificateTest`.
- [x] Test tworzy dwa badania, wykonuje `encodeMedicalImagingStudies` i `decodeMedicalImagingStudies`, a następnie porównuje pełne obiekty przez `assertEquals`.
- [x] Potwierdzono, że obie funkcje kodera/dekodera mają widoczność `internal`, nie publiczną.

### Status CI
- [x] REUSE compliance zakończyło się powodzeniem dla SHA `a3598a50d81b516485ad90ccff03f41357dcf5c9`.
- [ ] Uruchomienia Android build i GUI dla SHA `a3598a50d81b516485ad90ccff03f41357dcf5c9` zostały anulowane w trakcie testów JVM przez kolejną aktualizację gałęzi; nie są wynikiem pozytywnym ani negatywnym testu.
- [ ] Wymagane jest nowe CI dla bieżącego SHA zawierającego poprawiony test round-trip. Dopiero jego wynik będzie podstawą do oznaczenia testu jako zaliczonego.

### Stan gałęzi
- [x] Test round-trip jest obecny w aktualnym pliku testowym.
- [x] Audyt zawiera rozróżnienie między wynikiem wcześniejszego CI (SHA `14c9ab7`) a oczekującą walidacją nowego testu.
- [ ] Nie uznaje się jeszcze najnowszego testu za zweryfikowany przez CI.

---

## 22. Integracja szyfrowanych załączników z badaniami obrazowymi — 2026-11-15

### Zakres wykonanych prac
- [x] Rozszerzono komponent `DocumentOriginalAttachments` o opcjonalne powiadomienie `onAttachmentIdsChanged`; dotychczasowe wywołania pozostają zgodne dzięki wartości domyślnej.
- [x] Komponent przekazuje aktualną listę identyfikatorów po wczytaniu istniejących załączników, dodaniu pliku, dodaniu zdjęcia i usunięciu załącznika.
- [x] Każde badanie obrazowe otrzymało deterministyczny zakres załączników `imaging_study_<studyId>`, odrębny od innych badań i od dokumentu nadrzędnego.
- [x] W karcie badania osadzono komponent załączników. Zmiana listy ID aktualizuje pole `MedicalImagingStudy.attachmentIds`; istniejący zapis JSON przechowuje to pole.
- [x] Dodano test stabilności identyfikatora zakresu, izolacji dwóch badań oraz odrzucania nieprawidłowego identyfikatora.
- [x] Ponownie odczytano wszystkie trzy zmienione pliki z gałęzi GitHub i potwierdzono obecność zmian.

### Kontrola CI
- [x] Dla poprzedniego SHA `363ddae1cb794a5395c3605e470ebd263231121e` testy JVM, testy Compose na emulatorze, budowa APK oraz REUSE zakończyły się powodzeniem.
- [x] REUSE dla zmian integracji na SHA `e19622264e39b805b4e49fe5d5a8a56e4bdaf846` zakończyło się powodzeniem.
- [ ] Android JVM, Compose i APK dla zmian integracji wymagają zakończenia uruchomienia CI dla najnowszego SHA. Uruchomienia dla pośredniego SHA `029f78ddab542979b9e354e04baaa944aeb31740` zostały anulowane przez kolejną aktualizację gałęzi; nie są zaliczeniem ani błędem testów.
- [ ] Nie uznaje się jeszcze nowej integracji za zweryfikowaną kompilacyjnie ani emulatorowo.

### Uwagi i ryzyka pozostałe
- [ ] Należy zweryfikować zachowanie po restarcie aplikacji i pełnym eksporcie/ imporcie archiwum.
- [ ] Należy obsłużyć sprzątanie załączników po usunięciu całego badania, aby nie pozostawiać osieroconych plików.
- [ ] Należy przetestować uszkodzony lub brakujący plik, błędny MIME, limit 25 MB oraz odtworzenie danych na innej instalacji.
- [ ] Nie dodano automatycznego przesyłania danych medycznych; załączniki nadal korzystają z lokalnego magazynu szyfrowanego kluczem Android Keystore.

**Status:** integracja interfejsu i modelu załączników wykonana i zweryfikowana przez ponowny odczyt źródeł. Walidacja CI dla najnowszego kodu oczekuje na wynik. PR #69 pozostaje otwarty jako Draft i nie został scalony.
---

## 23. Zachowanie identyfikatorów archiwum i sprzątanie załączników badań — 2026-11-15

### Usterki potwierdzone w kodzie
- [x] Eksport archiwum załączników nie zapisywał pierwotnego identyfikatora załącznika.
- [x] Import tworzył nowy identyfikator, mimo że `MedicalImagingStudy.attachmentIds` zachowywało stare identyfikatory. Po odtworzeniu na innej instalacji powiązania badanie–plik mogły być zerwane.
- [x] Usunięcie badania z listy formularza nie usuwało metadanych ani zaszyfrowanych plików z jego zakresu załączników.
- [x] Natychmiastowe usuwanie przy kliknięciu „Remove” byłoby niebezpieczne, ponieważ formularz zapisuje listę badań dopiero przy użyciu „Save certificate”.

### Wykonane poprawki
- [x] Eksport archiwum zapisuje teraz także stabilne pole `id` załącznika.
- [x] Import zachowuje identyfikator z archiwum i ponownie szyfruje dane kluczem Android Keystore bieżącej instalacji.
- [x] Import pozostaje zgodny ze starszym archiwum bez pola `id`: dla takiego wpisu generuje nowy identyfikator; starszy format nie zawiera jednak danych pozwalających odtworzyć wcześniejsze referencje `attachmentIds`.
- [x] Import jest idempotentny dla istniejącego identyfikatora, jeżeli plik, zawartość i metadane są zgodne; konflikt identyfikatora z inną zawartością lub metadanymi zgłasza błąd.
- [x] Dodano usuwanie całego zakresu załączników badania: najpierw zatwierdzane jest usunięcie wpisu z rejestru, a następnie usuwane są odpowiadające mu pliki.
- [x] Sprzątanie uruchamia się dopiero po udanym zapisie nowej listy badań w szyfrowanym magazynie. Niezapisane usunięcie badania nie powoduje utraty jego załączników.
- [x] Dodano test jednostkowy wyboru identyfikatorów badań usuniętych względem poprzednio zapisanej listy.
- [x] Ponownie odczytano zmienione pliki z GitHub i potwierdzono obecność identyfikatora w eksporcie, jego odtwarzanie w imporcie, usuwanie zakresu oraz odroczone sprzątanie po zapisie.

### Weryfikacja CI — stan na moment aktualizacji
- [x] REUSE compliance: PASS dla SHA `5bf6e6f7ffecc095e8d1c6769ebaf19c2caadc1f`.
- [x] Android JVM unit tests: PASS oraz `assembleDebug` i przesłanie APK: PASS dla SHA `4910da5bfc08e766f07a633f9e8d2a8b77a76fb4` — jest to kod sprzed niniejszych poprawek.
- [ ] Testy emulatorowe dla SHA `4910da5bfc08e766f07a633f9e8d2a8b77a76fb4` pozostawały w toku; kolejne commity przerwały tamto uruchomienie.
- [ ] Dla aktualnego SHA `d3cd6f7a865361f03ca2a99af9e3043175e504b0` uruchomiono nowe workflow; build Android jest w kolejce, a REUSE rozpoczęto. Wyników tych workflow nie uznaje się jeszcze za PASS.
- [ ] Testy archiwum na dwóch instalacjach, awarie w połowie importu, uszkodzone pliki, nieprawidłowe MIME i graniczny rozmiar 25 MB nadal wymagają testów integracyjnych/instrumentalnych.

### Ryzyka i dalsze prace
- [ ] Import archiwum nadal wykonuje dodawanie wpisów sekwencyjnie; awaria po części wpisów może pozostawić częściowo odtworzone archiwum. Następny etap powinien wprowadzić przygotowanie i walidację wszystkich wpisów przed zatwierdzeniem importu jako całości.
- [ ] Usuwanie zakresu jest bezpieczne dla obecnego modelu, w którym każdy zakres badania ma osobny identyfikator i załączniki nie są współdzielone. Przed wprowadzeniem współdzielenia plików trzeba dodać kontrolę referencji.
- [ ] Nie potwierdzono jeszcze fizycznego odtworzenia po restarcie urządzenia ani przeniesienia archiwum między dwiema instalacjami Androida.

**Status:** naprawiono zachowanie identyfikatorów przy eksporcie i imporcie oraz dodano odroczone sprzątanie załączników po trwałym usunięciu badania. Zmiany źródłowe potwierdzono odczytem z GitHub; walidacja CI dla końcowego SHA pozostaje w toku. PR #69 nadal jest otwarty, Draft i niescalony.
---

## 24. Walidacja i etapowe zatwierdzanie importu archiwum załączników — 2026-11-15

### Wykryte ryzyko
- [x] Poprzedni import przetwarzał wpisy sekwencyjnie przez `add()`. Błąd przy późniejszym wpisie mógł pozostawić wcześniejsze załączniki zapisane, mimo że całe archiwum nie zostało odtworzone.
- [x] Import nie odrzucał powtórzonych identyfikatorów w obrębie jednego archiwum przed rozpoczęciem zapisu.
- [x] Konieczne było sprawdzanie całego wejścia przed modyfikacją magazynu oraz oddzielenie plików tymczasowych od zatwierdzonego rejestru.

### Wykonane zmiany
- [x] Import najpierw parsuje i waliduje wszystkie wpisy archiwum: wersję, identyfikator dokumentu, identyfikator załącznika, unikalność ID, nazwę, MIME, Base64, niepustą zawartość i limit 25 MB.
- [x] Dodano kontrolę nagłówka PDF i blokadę deklaracji DTD/ENTITY w plikach tekstowych/XML.
- [x] Przed instalacją plików sprawdzane są istniejące metadane, pliki i zawartość; identyczne wpisy są pomijane, a konflikty przerywają import.
- [x] Nowe pliki są najpierw szyfrowane do plików tymczasowych, następnie przenoszone do docelowych nazw, a rejestr metadanych jest zatwierdzany jednym synchronicznym `SharedPreferences.Editor.commit()`.
- [x] Przy błędzie przed zatwierdzeniem rejestru usuwane są pliki zainstalowane w ramach bieżącej próby; pliki tymczasowe są sprzątane w bloku `finally`.
- [x] Zachowano obsługę starszego archiwum bez pola `id` przez wygenerowanie nowego identyfikatora.
- [x] Zmieniony plik `DocumentOriginalAttachments.kt` ponownie odczytano z GitHub; sprawdzono kolejność walidacji, staging, kontrolę konfliktów i obsługę błędów.

### Ograniczenia i testy do wykonania
- [ ] Nie dodano jeszcze testów instrumentalnych importu z rzeczywistym Android Keystore i `SharedPreferences`; wymagane są przypadki: pusty wpis, powtórzony ID, konflikt ID, nieprawidłowy Base64, błędny PDF, nieobsługiwany MIME, rozmiar 25 MB i przekroczenie limitu.
- [ ] Operacja jest odporna na typowe błędy walidacji i zapisu w trakcie jednej próby, ale nie stanowi pełnej transakcji systemu plików odporniej na nagłe odcięcie zasilania pomiędzy przeniesieniem plików a zatwierdzeniem metadanych. Potrzebny jest dziennik transakcji/odtwarzanie po awarii procesu.
- [ ] Kontrola sygnatur obrazów (JPEG/PNG/WebP) oraz ścisłe mapowanie rozszerzeń MIME wymagają osobnego testu i ujednolicenia z walidacją dodawania plików.
- [ ] Testy eksportu/importu na dwóch fizycznych instalacjach i odtworzenia po restarcie nadal nie zostały wykonane.

### CI dla zmiany źródłowej
- [ ] Android prototype build dla SHA `4b83a59614a8d2882956018671ae16c8d262459f`: w toku.
- [ ] Android GUI validation dla SHA `4b83a59614a8d2882956018671ae16c8d262459f`: w toku.
- [ ] REUSE compliance dla SHA `4b83a59614a8d2882956018671ae16c8d262459f`: w toku.
- [x] Poprzedni SHA `cf319727315712a9bff3c7dbf2b6117bed56c959`: Android prototype build zakończony powodzeniem; Android GUI validation anulowano przy aktualizacji gałęzi, więc nie stanowi potwierdzenia testów Compose dla tego SHA.

**Status:** import przebudowano na walidację całego archiwum i etapowe zatwierdzanie danych. Kontrola źródła zakończona; wynik CI dla tej zmiany oczekuje na zakończenie workflow. PR #69 pozostaje otwarty, Draft i niescalony.
---

## 25. Ścisła walidacja MIME i limity archiwum załączników — 2026-11-15

### Wykonane zmiany
- [x] Ujednolicono walidację danych wejściowych dla dodawania i importu załączników przez wspólną funkcję validatePayload.
- [x] Zastąpiono ogólne dopuszczenie dowolnego image/* i text/* ścisłą listą typów: PDF, JPEG, PNG, WebP, text/plain, text/csv i application/xml.
- [x] Dodano kontrolę sygnatur bajtowych PDF (%PDF-), JPEG, PNG i WebP. Plik deklarujący obrazowy MIME, ale zawierający inną zawartość, jest odrzucany.
- [x] Zachowano dotychczasowe formaty tekstowe mapowane przez selektor plików; nadal blokowane są deklaracje DTD/ENTITY w próbce pierwszych 4096 bajtów.
- [x] Dodano limit archiwum: 50 MiB łącznej zawartości, maksymalnie 200 wpisów i kontrolę długości wejściowego Base64 przed dekodowaniem.
- [x] Limit 25 MiB na pojedynczy załącznik pozostaje bez zmian.
- [x] Zmiany zapisano w commicie a8435c1a9c50c5af1ef8ee454108100bbc5636ef.
- [x] Ponownie odczytano źródło z GitHub; potwierdzono obecność wspólnego walidatora, kontroli sygnatur oraz limitów archiwum.

### Weryfikacja CI
- [x] Dla poprzedniego SHA 08f665d3c23acdc56f726123ab2b845c7056c779 workflow Android prototype build zakończył się powodzeniem: JVM unit tests, assembleDebug i przesłanie APK — PASS.
- [x] Dla tego samego poprzedniego SHA REUSE compliance — PASS.
- [ ] Android GUI validation dla poprzedniego SHA pozostawało w toku; testy JVM zakończyły się powodzeniem, testy Compose na emulatorze były uruchomione.
- [ ] Dla nowego SHA a8435c1a9c50c5af1ef8ee454108100bbc5636ef REUSE compliance jest w kolejce, a Android GUI validation oczekuje na wykonanie. Nie ma jeszcze wyniku testów ani kompilacji dla tej zmiany.

### Pozostałe ryzyka i zadania
- [ ] Dodać testy jednostkowe czystej funkcji walidującej MIME i sygnatury: poprawne i błędne nagłówki PDF/JPEG/PNG/WebP, nieobsługiwane typy oraz granice rozmiaru.
- [ ] Dodać testy importu archiwum obejmujące limit 200 wpisów, 50 MiB, wadliwe Base64, konflikt ID, duplikaty oraz rollback.
- [ ] Dodać dziennik transakcji i odzyskiwanie po nagłym przerwaniu procesu pomiędzy instalacją plików a zatwierdzeniem metadanych.
- [ ] Zweryfikować eksport/import na dwóch instalacjach Androida i po restarcie urządzenia.
- [ ] Weryfikacja sygnatur obrazów potwierdza format kontenera, ale nie zastępuje pełnego dekodowania obrazu ani kontroli jego wymiarów/dekompresji.

**Status:** ścisłe mapowanie MIME, kontrola sygnatur i limity archiwum zostały zaimplementowane oraz potwierdzone ponownym odczytem pliku. Nie są jeszcze zaliczone przez CI. PR #69 pozostaje otwarty, Draft/WIP i niescalony.
---

## 26. Testy walidacji załączników — 2026-11-15

### Wykonane prace
- [x] Wydzielono walidator ładunku załącznika do funkcji o widoczności modułowej, aby można było testować go bez Android Keystore i bez operacji na plikach.
- [x] Funkcja jest używana w ścieżce dodawania, eksporcie i imporcie; wszystkie trzy ścieżki korzystają z tej samej kontroli MIME, rozmiaru, sygnatur i deklaracji XML.
- [x] Dodano testy jednostkowe prawidłowych sygnatur PDF/JPEG/PNG/WebP oraz akceptowanych typów tekstowych.
- [x] Dodano testy odrzucania MIME niezgodnego z zawartością, nieobsługiwanych typów, pustych danych, danych ponad 25 MiB i deklaracji DTD/ENTITY.
- [x] W trakcie kontroli źródła wykryto i poprawiono odwołanie do stałej MAX_BYTES, która po wydzieleniu walidatora nie była dostępna poza obiektem magazynu. Poprawka zapisana w commicie 5cdf1fd927f65fa1917c76e2da7026e64f06c3d9.
- [x] Ponownie odczytano plik produkcyjny i testowy z GitHub; potwierdzono użycie walidatora w trzech operacjach oraz obecność trzech nowych testów.

### CI i weryfikacja
- [x] Dla poprzedniego SHA 32552bbf7fc0b74ee3a980a605270691dcc58d4e Android prototype build zakończył się PASS: testy JVM, budowanie APK i przesłanie artefaktu.
- [x] Dla tego SHA REUSE compliance zakończyło się PASS.
- [ ] Android GUI validation dla SHA 32552bbf7fc0b74ee3a980a605270691dcc58d4e: testy JVM PASS, ale Compose UI na emulatorze anulowano; etapy budowania APK pominięto. Nie jest to pełny PASS.
- [ ] Dla aktualnego SHA 5cdf1fd927f65fa1917c76e2da7026e64f06c3d9 nie zwrócono jeszcze uruchomień CI. Nowe testy walidatora nie są zatem potwierdzone wykonaniem.
- [ ] Nie przeprowadzono jeszcze testów instrumentalnych magazynu z Android Keystore i SharedPreferences ani testów odzyskiwania po przerwaniu procesu.

### Ograniczenia i następny etap
- [ ] Funkcja sprawdza sygnatury kontenerów, ale nie dekoduje obrazów w celu sprawdzenia wymiarów, struktury i odporności na dekompresję.
- [ ] Dodać testy archiwum dla duplikatów ID, konfliktów, wadliwego Base64, limitów 50 MiB/200 wpisów oraz rollbacku.
- [ ] Dodać dziennik transakcji i procedurę odzyskiwania po awarii między instalacją plików a zatwierdzeniem metadanych.
- [ ] Po zakończeniu CI wykonać testy eksportu/importu na dwóch instalacjach i po restarcie urządzenia.

**Status:** dodano testowalny walidator i testy jednostkowe. Zidentyfikowaną usterkę kompilacyjną wydzielonej funkcji poprawiono przed końcowym zapisem. Wynik CI dla aktualnego SHA pozostaje niepotwierdzony. PR #69 nadal jest otwarty, Draft i niescalony.
---

## 27. Spójność zapisu i usuwania pojedynczych załączników — 2026-11-15

### Kontrola stanu i CI przed zmianą
- [x] Odczytano PR #69, źródło magazynu załączników, testy jednostkowe i aktualny audyt z gałęzi zaswaidczenie.
- [x] Dla poprzedniego SHA 64855a567b39a129ed3b90e75970fc4235e374a7 wszystkie trzy workflow zakończyły się powodzeniem: REUSE compliance, Android prototype build oraz Android GUI validation.
- [x] W Android prototype build potwierdzono powodzenie testów JVM, budowania APK i archiwizacji APK.
- [x] W Android GUI validation potwierdzono powodzenie testów JVM, testów Compose na emulatorze, budowania APK i archiwizacji raportów.

### Wykryte problemy i wykonane poprawki
- [x] W operacji add() plik szyfrowany był zapisywany przed zatwierdzeniem metadanych. Jeśli zapis SharedPreferences kończył się błędem, pozostawał osierocony plik .enc. Dodano obsługę błędu, która usuwa plik utworzony przez nieudaną próbę i ponownie zgłasza pierwotny wyjątek.
- [x] W operacji remove() plik był usuwany przed zatwierdzeniem zmian metadanych, a zapis wykonywano asynchronicznie przez apply(). Zmieniono kolejność: synchroniczny commit metadanych, a dopiero po jego powodzeniu usunięcie pliku. Błąd zatwierdzenia nie powoduje już usunięcia pliku nadal wskazywanego przez rejestr.
- [x] Zmiany zapisano w commicie 46fc4643e9de09d5d2edb51cb846579884bd1264.
- [x] Ponownie odczytano plik źródłowy z GitHub i potwierdzono obecność obu zabezpieczeń.

### Weryfikacja po zmianie
- [x] PR #69 nadal jest otwarty, Draft i niescalony.
- [ ] Dla SHA 46fc4643e9de09d5d2edb51cb846579884bd1264 nie było jeszcze zarejestrowanych uruchomień CI w chwili aktualizacji audytu. Nowe poprawki nie są jeszcze oznaczone jako zaliczone przez CI.
- [ ] Nie dodano jeszcze testów integracyjnych wymuszających błąd commit() ani testów sprawdzających sprzątanie pliku po nieudanym dodaniu i usunięciu.

### Pozostałe ryzyka i następne prace
- [ ] Operacje dodawania/usuwania i import archiwum nie mają jeszcze wspólnego dziennika transakcji. Nagłe zakończenie procesu w krytycznym momencie może pozostawić plik osierocony albo metadane bez odpowiadającego pliku; potrzebna jest procedura uzgadniania magazynu przy uruchomieniu.
- [ ] Import nadal nie jest w pełni odporny na nagłą awarię pomiędzy instalacją plików a zatwierdzeniem SharedPreferences.
- [ ] Dodać testy archiwum: duplikaty ID, konflikt z innym dokumentem, wadliwe Base64, limity 50 MiB/200 wpisów oraz rollback.
- [ ] Dodać pełne dekodowanie obrazów i limity wymiarów przed przyjęciem plików.
- [ ] Wykonać testy na dwóch instalacjach Androida oraz po restarcie urządzenia.

**Status:** poprawiono obsługę błędów zapisu i kolejność usuwania pojedynczych załączników. Poprawki potwierdzono ponownym odczytem źródła. CI dla nowego SHA oczekuje na uruchomienie/zakończenie. PR #69 pozostaje otwarty, Draft i niescalony.

---

## 28. Ścisła walidacja rejestru metadanych załączników — 2026-11-15

### Kontrola poprzedniego etapu
- [x] Ponownie sprawdzono PR #69 i gałąź `zaswaidczenie`; PR pozostaje otwarty, Draft i niescalony.
- [x] Dla SHA `05cf4fe336c200c43e93d5169f69e626bad398db` wszystkie trzy workflow zakończyły się powodzeniem: REUSE compliance, Android prototype build oraz Android GUI validation.
- [x] W Android GUI validation potwierdzono testy JVM, testy Compose UI na emulatorze, budowanie APK i archiwizację raportów. Publikacja podpisanego APK została pominięta zgodnie z konfiguracją workflow.

### Wykryty problem
- [x] Funkcja `list()` magazynu załączników używała `runCatching(...).getOrDefault(emptyList())`. Uszkodzony JSON metadanych, nieprawidłowy rekord lub błąd odczytu były więc przedstawiane jako pusty rejestr.
- [x] Taki wynik mógł doprowadzić do nadpisania istniejących metadanych podczas kolejnego dodawania załącznika. Błąd integralności był ukrywany zamiast zatrzymać zapis.

### Wykonane poprawki
- [x] Usunięto ciche zamienianie błędu odczytu metadanych na pustą listę. Błąd parsowania jest teraz zgłaszany wywołującemu, a operacja zapisu zostaje przerwana.
- [x] Dodano walidację identyfikatora dokumentu, formatu identyfikatorów załączników, duplikatów ID, niepustej nazwy o maksymalnej długości 180 znaków oraz dozwolonej listy MIME.
- [x] Walidację metadanych wydzielono do funkcji `validateOriginalAttachmentMetadata`, aby można ją było sprawdzać testami jednostkowymi.
- [x] Dodano test poprawnego rekordu oraz testy odrzucania nieprawidłowego ID, powtórzonego ID, niedozwolonego MIME i pustej nazwy.
- [x] Zmiany źródłowe zapisano w commitach `ce1059dd91ff7144f97b4661257eadd417072cfd` i `2e63c6fa878b80d40ccf025bc0859927c0b23161`.
- [x] Testy zapisano w commicie `c7e435f95ad84bbf2a7b11889a8bffacb1c267f5`.
- [x] Ponownie odczytano plik źródłowy i testowy z GitHub; potwierdzono obecność funkcji walidującej, jej użycie w `list()` i testów.

### CI po poprawkach
- [ ] Dla końcowego SHA `c7e435f95ad84bbf2a7b11889a8bffacb1c267f5` nie było jeszcze zarejestrowanych workflow runs w chwili aktualizacji tego audytu. Wyników CI dla nowych zmian nie oznacza się jako PASS.
- [x] Poprzedni SHA `05cf4fe336c200c43e93d5169f69e626bad398db` ma potwierdzone trzy zakończone powodzeniem workflow; jest to walidacja poprzedniej wersji, a nie bieżących poprawek.

### Pozostałe ryzyka i następne prace
- [ ] Dodać testy integracyjne wymuszające błędy `SharedPreferences.commit()` oraz weryfikujące sprzątanie plików po nieudanym dodaniu/usunięciu.
- [ ] Dodać procedurę uzgadniania metadanych i plików po restarcie oraz dziennik transakcji dla importu.
- [ ] Rozszerzyć testy importu o duplikaty i konflikty ID między dokumentami, błędne Base64, limity 50 MiB/200 wpisów oraz wycofanie zmian.
- [ ] Dodać pełne dekodowanie obrazów i ograniczenia wymiarów/dekompresji.
- [ ] Wykonać testy eksportu/importu między dwiema instalacjami Androida oraz po restarcie urządzenia.

**Status:** poprawiono zachowanie magazynu w przypadku uszkodzonych metadanych: błędy nie są już maskowane jako pusty rejestr, a nieprawidłowe rekordy są odrzucane. Kod i testy potwierdzono ponownym odczytem z GitHub. CI dla końcowego SHA oczekuje na uruchomienie. PR #69 pozostaje otwarty, Draft i niescalony.


---

## 29. Uzgadnianie magazynu załączników po uruchomieniu i kontrola importu — 2026-11-15

### Kontrola stanu wejściowego
- [x] Potwierdzono, że PR #69 pozostaje otwarty, Draft i niescalony.
- [x] Dla SHA `e7e6a8e23cda22032c0c35829f5b1189dda3a923` REUSE compliance zakończył się powodzeniem; Android prototype build i Android GUI validation nadal były w toku. Nie przeniesiono tych wyników na późniejsze commity.

### Wykonane poprawki
- [x] Dodano funkcję `OriginalAttachmentStore.reconcile()`, uruchamianą z efektu Compose przy uruchomieniu komponentu magazynu.
- [x] Procedura sprawdza format identyfikatorów dokumentów, poprawność metadanych oraz globalną unikalność identyfikatorów załączników.
- [x] Procedura wykrywa metadane wskazujące na brakujący plik szyfrowany i zgłasza błąd integralności zamiast usuwać wpis lub udawać pusty magazyn.
- [x] Procedura usuwa pliki `.enc` bez odpowiadających im metadanych oraz porzucone pliki tymczasowe `.import`. Ogranicza to skutki awarii po instalacji pliku, ale przed zatwierdzeniem metadanych.
- [x] Dodano test jednostkowy funkcji wyznaczającej osierocone identyfikatory plików.
- [x] Import archiwum waliduje teraz istniejące metadane przed ich wykorzystaniem oraz odrzuca identyfikator załącznika przypisany do więcej niż jednego dokumentu.
- [x] Zmiany źródłowe zapisano w commitach `bc8712381858e6261effdcc865754ec0c8afce4a` i `d3fca6e85a9d2f7c0bd9499259a546e3f699b093`.
- [x] Test odzyskiwania zapisano w commicie `d17473479e9626b2c50c07899f35affff010d5ad`.
- [x] Zmiany źródłowe i testowe ponownie odczytano z GitHub i potwierdzono obecność nowych funkcji.

### Ograniczenia i ryzyka
- [ ] Uzgadnianie jest uruchamiane z komponentu Compose, a nie z centralnego punktu inicjalizacji aplikacji. Należy zweryfikować, czy komponent ten jest uruchamiany we wszystkich ścieżkach dostępu do magazynu.
- [ ] Procedura usuwa osierocone pliki, ale nie stanowi pełnego dziennika transakcji. Nadal możliwa jest awaria w trakcie importu, zanim wszystkie pliki i metadane zostaną zatwierdzone.
- [ ] Brakujące pliki zgłaszają błąd; nie ma jeszcze interfejsu naprawy/odtworzenia z archiwum.
- [ ] Należy dodać testy integracyjne na rzeczywistym `Context`/systemie plików: osierocone pliki, brakujące pliki, powtórzone ID między dokumentami, porzucone pliki tymczasowe oraz odtwarzanie po restarcie.
- [ ] Należy dodać testy archiwum dla wadliwego Base64, limitów 50 MiB/200 wpisów, konfliktów zawartości i wycofania importu.
- [ ] Pełne dekodowanie obrazów, ograniczenia wymiarów i testy dwóch instalacji Androida pozostają do wykonania.

### CI po zmianach
- [ ] Nie oznaczać wcześniejszych przebiegów CI jako walidacji nowych commitów. Wyniki dla końcowego SHA tego etapu należy odczytać po uruchomieniu workflow.
- [ ] Wymagane jest potwierdzenie testów JVM, Compose UI na emulatorze, budowania APK i kontroli REUSE dla dokładnego końcowego SHA.

**Status:** dodano wstępne odzyskiwanie osieroconych plików i kontrolę spójności metadanych przy uruchomieniu komponentu magazynu; import sprawdza istniejące metadane przed scaleniem. To istotne ograniczenie skutków awarii, ale nie pełna transakcyjność. CI dla nowych commitów wymaga potwierdzenia.


---

## 30. Bezpieczne postępowanie po niejednoznacznym wyniku zapisu metadanych importu — 2026-11-15

### Walidacja poprzedniego etapu
- [x] Dla SHA `27173a02e95fc5e84c11156b7f7e30842ba6b8d4` zakończyły się powodzeniem: Repository compliance, Android prototype build oraz Android GUI validation.
- [x] Job Android GUI validation wykonał z powodzeniem testy jednostkowe, testy Compose UI na emulatorze, budowanie debug APK i archiwizację APK oraz raportów.
- [x] Konfiguracja pominęła podpisywanie kluczem stabilnym i publikację APK; nie deklaruje się zatem opublikowanego ani podpisanego wydania.
- [x] PR #69 pozostaje otwarty, Draft i niescalony.

### Wykryte ryzyko
- [x] Import instalował pliki szyfrowane, a następnie wykonywał `SharedPreferences.Editor.commit()`. W przypadku wyniku `false` stan mapy w pamięci procesu może już różnić się od trwałego stanu na dysku.
- [x] Dotychczasowy blok `catch` usuwał wszystkie nowo zainstalowane pliki. Przy niejednoznacznym wyniku commit mogło to pozostawić metadane wskazujące na usunięte pliki.

### Wykonane poprawki
- [x] Dodano rozróżnienie błędów przed próbą zatwierdzenia metadanych i błędów po rozpoczęciu tej próby.
- [x] Jeżeli błąd wystąpi przed próbą `commit()`, nowo zainstalowane pliki są usuwane jak dotychczas.
- [x] Jeżeli `commit()` został już wywołany, pliki pozostają na miejscu. Przy kolejnym uzgadnianiu magazynu pliki bez trwałych referencji w metadanych zostaną rozpoznane jako osierocone i usunięte; jeśli referencje przetrwały, źródła pozostają dostępne.
- [x] Dodano test jednostkowy reguły decyzji o usuwaniu plików po błędzie importu.
- [x] Zmiany zapisano w commitach `48da42cc728c16011ec87706cfb8e2915b64fa4f` oraz `81194783e1348f93761bd21fc03972a849fd59a8`.
- [x] Zmiany źródłowe i testowe ponownie odczytano z GitHub; potwierdzono obecność flagi próby commit, warunkowego sprzątania i testu.

### Ograniczenia
- [ ] Nadal brak pełnego dziennika transakcji i testów awarii procesu na urządzeniu. Obecna poprawka ogranicza ryzyko niespójności po niejednoznacznym `commit()`, ale nie dowodzi atomowości całej operacji.
- [ ] Należy wykonać test integracyjny z wymuszonym wynikiem `commit() == false` oraz odczytem metadanych po ponownym otwarciu magazynu.
- [ ] Pozostają testy wadliwego Base64, limitów archiwum, konfliktów między dokumentami, rollbacku, pełnego dekodowania obrazów i ograniczeń wymiarów.

### CI dla poprzedniego SHA
- [x] Dla SHA `27173a02e95fc5e84c11156b7f7e30842ba6b8d4` wszystkie trzy workflow zakończyły się powodzeniem.
- [ ] Wyniki CI dla commitów sekcji 30 należy potwierdzić osobno; nie przenosić statusu poprzedniego SHA na nowe zmiany.

**Status:** poprawiono sprzątanie importu w przypadku błędu po rozpoczęciu zapisu metadanych. Zmiana ogranicza ryzyko usunięcia plików, do których metadane mogły zostać już zapisane. Testy CI dla nowego końcowego SHA pozostają do potwierdzenia.

---

## 31. Pełna kontrola deklaracji XML w załącznikach — 2026-11-15

### Wykryta luka
- [x] Walidator tekstowych załączników sprawdzał deklaracje `DOCTYPE` i `ENTITY` wyłącznie w pierwszych 4096 bajtach.
- [x] Deklaracja umieszczona dalej w treści mogła ominąć tę kontrolę.

### Wykonana poprawka
- [x] Walidator analizuje teraz całą treść UTF-8 dla typów `text/plain`, `text/csv` i `application/xml`.
- [x] Dodano test regresyjny umieszczający deklarację XML po 4096 znakach.
- [x] Zmianę źródłową zapisano w commicie `b4bc63159896b0c90c965a75ec2e3ef5fe8a1292`.
- [x] Test zapisano w commicie `9aff38277751833619eeea392a554185dfcc93d4`.

### Ograniczenia
- [ ] Jest to kontrola tekstowa deklaracji, a nie pełny parser XML ani walidacja schematu dokumentu.
- [ ] Nadal należy dodać pełne dekodowanie obrazów i limity wymiarów, testy integracyjne magazynu na rzeczywistym Android Context oraz dziennik transakcji importu.

### CI
- [ ] Wyniki workflow dla końcowego SHA tej sekcji wymagają odczytu po zakończeniu uruchomień. Nie przenosi się wyników z wcześniejszych commitów.

**Status:** rozszerzono wykrywanie deklaracji DTD/ENTITY na całą treść załącznika i dodano test regresyjny. Poprawka wymaga potwierdzenia przez CI dla końcowego SHA.

---

## 32. Ścisła walidacja kodowania Base64 przy imporcie — 2026-11-15

### Wykryta luka
- [x] Import używał dekodera Base64 w trybie tolerującym białe znaki i nie sprawdzał kanoniczności zakodowanego ciągu.
- [x] Nieprawidłowe lub niekanoniczne reprezentacje mogły być akceptowane mimo że eksport aplikacji generuje standardowy zapis bez dodatkowych znaków.

### Wykonana poprawka
- [x] Dodano `decodeOriginalAttachmentBase64`, korzystające ze ścisłego dekodera Base64.
- [x] Funkcja sprawdza limit rozmiaru przed dekodowaniem, rozmiar danych po dekodowaniu oraz zgodność z kanonicznym ponownym kodowaniem.
- [x] Import archiwum korzysta teraz z tej funkcji zamiast tolerancyjnego dekodera Androida.
- [x] Dodano testy poprawnego kodowania, błędnych znaków, brakującego/podwójnego paddingu, białych znaków, niekanonicznego kodowania, pustej wartości i przekroczenia limitu.
- [x] Kod zapisano w commicie `8666ed38739d1dd0a3f3206ff0cf7e4e6d755a29`; testy w commicie `3a2c68d1d69075f688380f6bdc7bf729a0ef9471`.

### Ograniczenia
- [ ] Nie zastępuje to testów integracyjnych całego importu na Androidzie ani testów awarii procesu.
- [ ] Nadal wymagane są testy limitów całego archiwum i liczby wpisów, konfliktów ID, pełnego dekodowania obrazów i ograniczeń wymiarów.

### CI
- [ ] CI dla końcowego SHA tej sekcji wymaga potwierdzenia po zakończeniu workflow.

**Status:** import odrzuca teraz niekanoniczne i wadliwe ciągi Base64 oraz weryfikuje rozmiar danych przed i po dekodowaniu. Testy regresyjne dodano; wykonanie CI pozostaje do potwierdzenia.


---

## 33. Walidacja właściciela identyfikatora załącznika przy imporcie — 2026-11-15

### Wykryta luka
- [x] Import sprawdzał powtórzenia identyfikatorów wewnątrz archiwum i duplikaty w istniejących metadanych, ale nie porównywał właściciela identyfikatora z dokumentem wskazanym przez importowany wpis.
- [x] W przypadku niespójności między metadanymi a plikami mogło dojść do próby przypisania identyfikatora istniejącego w innym dokumencie.
- [x] Brak pola `attachments` w archiwum był traktowany jak pusta lista, co mogło ukryć wadliwy format wejściowy.

### Wykonane poprawki
- [x] Dodano walidację, która dopuszcza ponowny import identyfikatora wyłącznie do tego samego dokumentu, a odrzuca próbę przeniesienia go do innego dokumentu.
- [x] Import wymaga jawnej, poprawnej tablicy `attachments`; brak pola lub nieprawidłowy typ jest odrzucany.
- [x] Eksport waliduje strukturę metadanych przed odczytem załączników.
- [x] Dodano test jednostkowy dla ponownego importu do tego samego dokumentu, nowego identyfikatora oraz konfliktu między dokumentami.

### Weryfikacja
- [ ] Ponowny odczyt zmian i kontrola CI dla końcowego SHA — do wykonania po zapisaniu commitów.
- [ ] Testy integracyjne rzeczywistego importu, rollbacku i awarii procesu nadal pozostają wymagane.

**Status:** dodano kontrolę właściciela identyfikatora oraz odrzucanie niekompletnych archiwów. Nie oznacza to jeszcze pełnej transakcyjności importu ani walidacji obrazu przez dekoder.


---

## 34. Ochrona integralności przy dodawaniu pojedynczego załącznika — 2026-11-15

### Kontrola stanu wejściowego
- [x] Kontynuowano prace na gałęzi `zaswaidczenie`, w PR #69; gałąź `main` pozostała bez zmian.
- [x] Przejrzano implementację `OriginalAttachmentStore.add()`, import archiwum, procedurę `reconcile()` oraz testy regresyjne magazynu.
- [x] Potwierdzono wcześniejszą regułę dla importu: po rozpoczęciu `SharedPreferences.commit()` pliki pozostają na miejscu, ponieważ wynik błędu może być niejednoznaczny.

### Wykryta luka
- [x] Metoda `add()` usuwała nowo zapisany plik szyfrowany po każdym wyjątku, również gdy `SharedPreferences.commit()` został już wywołany.
- [x] Wynik `commit() == false` nie daje bezpiecznej podstawy do założenia, że stan mapy preferencji w pamięci procesu i stan trwały są identyczne. Usunięcie pliku po próbie zatwierdzenia mogło więc pozostawić metadane wskazujące na nieistniejący plik.

### Wykonane poprawki
- [x] Dodano osobną, testowalną regułę `shouldDeleteAddedAttachmentAfterFailure(metadataCommitAttempted)`.
- [x] Metoda `add()` oznacza moment rozpoczęcia próby `commit()`.
- [x] Błędy przed próbą zatwierdzenia nadal powodują usunięcie nowo utworzonego pliku.
- [x] Po rozpoczęciu próby zatwierdzenia plik pozostaje na miejscu; procedura `reconcile()` usuwa go później wyłącznie wtedy, gdy nie ma trwałego odwołania w metadanych.
- [x] Zmianę źródłową zapisano w commicie `f9fca22268317987451a29037f2729d3e791ea43`.
- [x] Dodano test regresyjny sprawdzający obie gałęzie decyzji; test zapisano w commicie `602cf19e61f7127aa82ba54872c2f117f4ab31a1`.

### Zakres weryfikacji
- [x] Ponownie odczytano zmieniony plik źródłowy i test z gałęzi `zaswaidczenie`; potwierdzono obecność flagi próby commit, warunkowego sprzątania i testu.
- [ ] Testy JVM, testy Compose na emulatorze, build APK oraz REUSE dla końcowego SHA po tej zmianie wymagają potwierdzenia w GitHub Actions.
- [ ] Test sprawdza czystą funkcję decyzyjną; nie symuluje rzeczywistego `SharedPreferences.commit() == false` ani awarii procesu. Nie należy traktować go jako pełnego testu integracyjnego magazynu.

### Pozostałe ryzyka i następne prace
- [ ] Dodać test integracyjny magazynu z wymuszonym błędem `commit()`, ponownym otwarciem preferencji i kontrolą zgodności pliku z metadanymi.
- [ ] Przenieść uzgadnianie magazynu z efektu pojedynczego komponentu Compose do centralnego punktu inicjalizacji lub zapewnić jego wykonanie przed każdą ścieżką zapisu/odczytu.
- [ ] Dodać testy integracyjne importu/eksportu, awarii procesu, rollbacku, konfliktów identyfikatorów i limitów archiwum.
- [ ] Dodać dekodowanie obrazów z kontrolą rzeczywistego formatu, wymiarów i ryzyka dekompresji; sygnatura pliku nie potwierdza, że obraz jest poprawny ani bezpieczny do renderowania.
- [ ] Zweryfikować bezpieczny, atomowy zapis głównego magazynu `InternationalMedicalCertificateStore.save()`; obecny mechanizm awaryjnego kopiowania do pliku docelowego wymaga osobnego testu przerwania zapisu.

**Status:** poprawiono ryzyko usunięcia pliku po niejednoznacznym wyniku zatwierdzania metadanych w pojedynczym `add()`. Kod i test regresyjny zapisano i ponownie odczytano. Końcowa walidacja CI dla nowego SHA pozostaje wymagana; PR #69 pozostaje otwarty, Draft i niescalony.


---

## 35. Dekodowanie obrazów załączników i limity wymiarów — 2026-11-15

### Wykryta luka
- [x] Walidacja JPEG/PNG/WebP sprawdzała sygnatury, ale nie potwierdzała, że zawartość jest dekodowalnym obrazem.
- [x] Brakowało limitów wymiarów oraz łącznej liczby pikseli, co pozostawiało ryzyko nadmiernej alokacji podczas dekodowania.

### Wykonane poprawki
- [x] Dodano wstępne odczytanie wymiarów przez BitmapFactory.Options.inJustDecodeBounds.
- [x] Odrzucane są obrazy z wymiarem powyżej 12 000 pikseli w dowolnej osi lub ponad 40 mln pikseli łącznie.
- [x] Po kontroli wymiarów wykonywane jest dekodowanie próbkowane do maksymalnie 2048 pikseli na oś, z konfiguracją RGB_565; bitmapa jest zwalniana po walidacji.
- [x] Kontrola obejmuje JPEG, PNG i WebP po sprawdzeniu sygnatury.
- [x] Dodano testy instrumentacyjne Androida: prawidłowy PNG, fałszywa sygnatura PNG i przekroczenie limitu wymiarów.
- [x] Kod źródłowy zapisano w commicie f9ac77d9da2951fd5423ff29e428aacfae500e27, testy instrumentacyjne w commicie 9b662c4751f7087ca2258519143463e21b62d6d7.

### Weryfikacja i ograniczenia
- [x] Ponownie odczytano z GitHub kod, test i audyt; potwierdzono obecność funkcji dekodującej, trzy wywołania dla formatów obrazów oraz trzy testy instrumentacyjne.
- [ ] Testy instrumentacyjne, JVM, Compose UI, build APK i REUSE dla końcowego SHA wymagają potwierdzenia w GitHub Actions.
- [ ] Dekodowanie próbkowane nie jest pełnym testem odporności wszystkich implementacji dekoderów na złośliwe pliki; należy nadal traktować pliki jako niezaufane.
- [ ] Pozostają testy integracyjne rzeczywistego importu/eksportu, rollbacku, awarii procesu i limitów archiwum.

**Status:** rozszerzono kontrolę sygnatur o odczyt wymiarów i rzeczywiste dekodowanie próbkowane. Nie oznacza to jeszcze pełnej walidacji semantycznej plików PDF ani testów awarii całego magazynu.


---

## 36. Atomowy zapis zaszyfrowanego magazynu zaświadczenia — 2026-11-15

### Wykryta luka
- [x] `InternationalMedicalCertificateStore.save()` zapisywał plik tymczasowy, ale przy niepowodzeniu `renameTo()` wykonywał kopiowanie z nadpisaniem pliku docelowego.
- [x] Kopiowanie do istniejącego pliku docelowego mogło go najpierw skrócić, a następnie pozostawić częściową zawartość po przerwaniu procesu lub błędzie wejścia/wyjścia.
- [x] Odczyt nie ograniczał rozmiaru zaszyfrowanego pliku przed wczytaniem go do pamięci.

### Wykonane poprawki
- [x] Dodano `writeMedicalCertificateAtomically()`: zapis do pliku tymczasowego, `FileDescriptor.sync()`, a następnie atomowe przeniesienie w obrębie tego samego katalogu z `ATOMIC_MOVE` i `REPLACE_EXISTING`.
- [x] Usunięto nieatomowe kopiowanie awaryjne. Gdy system plików nie obsługuje atomowej zamiany, operacja kończy się błędem zamiast ryzykować uszkodzenie poprzedniego pliku.
- [x] Plik tymczasowy jest sprzątany po błędzie; istniejący plik docelowy pozostaje nienaruszony, jeśli operacja zamiany się nie powiedzie.
- [x] Dodano limit 9 MiB dla danych jawnych oraz 10 MiB dla pliku zaszyfrowanego; limit pliku jest sprawdzany przed odczytem.
- [x] Dodano testy instrumentacyjne: atomowa zamiana istniejącego pliku, zachowanie celu po nieudanej zamianie, brak pozostałego pliku tymczasowego oraz szyfrowany zapis/odczyt i ponowny zapis wartości.
- [x] Implementację zapisano w commicie `e41686b4254124ae0d29f72f441690ea18faef48`; testy instrumentacyjne w commicie `8b736d2dc27578c1ffd1093742d53b6f4d39b3a0`.

### Weryfikacja i ograniczenia
- [x] Ponownie odczytano implementację i testy z gałęzi `zaswaidczenie`; potwierdzono użycie `ATOMIC_MOVE`, `REPLACE_EXISTING`, synchronizację pliku i testy scenariusza błędu.
- [ ] Testy instrumentacyjne, JVM, Compose UI, build APK i REUSE dla końcowego SHA wymagają potwierdzenia w GitHub Actions.
- [ ] Testy instrumentacyjne nie symulują twardego zaniku zasilania ani awarii systemu plików po zmianie wpisu katalogowego. Nie potwierdzają też trwałości wpisu katalogowego po utracie zasilania; synchronizacja katalogu wymaga osobnej oceny kompatybilności Androida.
- [ ] Należy sprawdzić zachowanie `ATOMIC_MOVE` na docelowych wersjach Androida i systemach plików urządzeń wspieranych przez aplikację.
- [x] Usunięto maskowanie błędów w `load()`: metoda deleguje teraz bezpośrednio do `loadStrict()`, więc uszkodzenie lub brak możliwości odszyfrowania danych jest jawnie zgłaszane zamiast zwracać pustą mapę.
- [x] Zawężono obsługę błędów atomowego zapisu z `Throwable` do `Exception`, aby nie przechwytywać błędów krytycznych procesu/maszyny wirtualnej.
- [x] Dodano test instrumentacyjny potwierdzający, że zmodyfikowany szyfrogram powoduje błąd odczytu, a nie pozornie poprawny pusty rekord.
- [x] Dalsze zmiany zapisano w commitach `1de4bae3a64388b1d441e8cbace2887676c5415b`, `67cde662eb5d39967c128c46b7668a51368963bf` i `4d1621cc7403f85bcd5f72e9e6cb7f7f8b5c0370`.

**Status:** usunięto nieatomowy mechanizm kopiowania na plik docelowy i dodano atomową zamianę z synchronizacją oraz limitami rozmiaru. Dodano testy instrumentacyjne. Ostateczne CI dla nowego SHA pozostaje wymagane; PR #69 nadal otwarty, Draft i niescalony.


### Uzupełnienie kontroli CI — 2026-11-15
- [x] GitHub Actions uruchomił trzy workflow dla pośredniego SHA `0ae9db61e186555de83a9bc986d557ba1984486c`: Android prototype build, Android GUI validation oraz Repository compliance.
- [x] Workflow zgodności REUSE zakończył się powodzeniem.
- [ ] Build Android i walidacja GUI dla tego pośredniego SHA zostały anulowane po kolejnych aktualizacjach gałęzi; nie stanowią wyniku testów końcowej wersji.
- [ ] Dla końcowego SHA `3bec148e53866fbb55a1a97ff6fbdfc1c54545a1` odczyt statusów i listy workflow nie zwrócił uruchomień. Testy nowego kodu pozostają zatem niepotwierdzone.

---

## 37. Naprawa testu JVM wywołującego Android BitmapFactory — 2026-11-15

### Wynik kontroli CI dla SHA `54ec2908a38af3092ba6f202b670a85ff0a02d7a`
- [x] REUSE compliance zakończył się powodzeniem (run `43737706100`).
- [x] Android prototype build oraz Android GUI validation zakończyły się niepowodzeniem na etapie wspólnego zadania testów JVM (runy `43737706087` i `43737706138`).
- [x] Oba logi wskazały ten sam test: `InternationalMedicalCertificateTest.attachmentPayloadValidatorAcceptsSupportedFileSignatures`.
- [x] Kompilacja Kotlin aplikacji zakończyła się poprawnie; zadanie `:app:testDebugUnitTest` przerwało dalsze kroki, dlatego kompilacja APK i testy emulatora zostały pominięte.

### Przyczyna
- [x] Test JVM przekazywał do `validateOriginalAttachmentPayload()` sztuczne nagłówki JPEG/PNG/WebP.
- [x] Walidator celowo wywołuje `android.graphics.BitmapFactory`, aby odczytać rzeczywiste wymiary i zdekodować obraz. W środowisku lokalnych testów JVM Android SDK udostępnia atrapę tej klasy, która zgłasza `RuntimeException` („Method ... not mocked”).
- [x] To błąd umiejscowienia testu i danych testowych, a nie dowód błędu kompilacji ani powód do osłabienia walidatora obrazów.

### Wykonana poprawka
- [x] Test JVM rozdzielono na akceptację typów nieobrazowych (PDF, tekst, CSV, XML) oraz odrzucanie sztucznych, niedekodowalnych danych obrazowych.
- [x] Weryfikacja rzeczywistego dekodowania poprawnych obrazów pozostaje w `OriginalAttachmentImageValidationTest` w zestawie Android instrumented tests, gdzie używany jest prawdziwy `Bitmap`/ `BitmapFactory`.
- [x] Zmianę zapisano w commicie `6b568a9c5314b98e480acc18693baa5cb1d698ef`.

### Status weryfikacji
- [ ] Nowy commit wymaga ponownego uruchomienia i potwierdzenia workflow Android JVM, GUI/emulator oraz REUSE.
- [ ] Do czasu zakończenia nowych workflow nie deklaruje się naprawienia CI ani gotowości APK.

### Kontrola końcowego stanu etapu 37
- [x] Ponownie odczytano zmieniony test JVM i potwierdzono, że nie wywołuje on już dekodera Androida dla pozytywnych przypadków obrazu; prawidłowe dekodowanie jest testowane w klasie instrumentacyjnej.
- [x] PR #69 pozostaje otwarty i Draft; gałąź `zaswaidczenie` jest 99 commitów przed `main` i 0 commitów za nim.
- [x] Końcowy SHA po aktualizacji audytu: `3700c63eb4bbaf7e14778aaac024a5171e6be3f0`.
- [ ] Dla tego SHA GitHub nie zwrócił jeszcze nowych workflow ani statusów. Nie można zatem potwierdzić, że poprawka przeszła testy; wymagane jest uruchomienie CI przez zdarzenie PR/push w repozytorium.

---

## 38. Walidacja domenowa języków i badań obrazowych — 2026-11-15

### Ustalenia
- [x] Wcześniejsza walidacja daty była powiązana głównie z polem formularza. Model domenowy nie kontrolował spójności języków, unikalności identyfikatorów badań ani identyfikatorów załączników.
- [x] Przycisk zapisu nie korzystał z pełnej walidacji modelu domenowego, przez co część błędnych danych mogła zostać utrwalona mimo istnienia metod `validate()`.

### Wykonane prace
- [x] Model odrzuca nieobsługiwany język źródłowy i nieobsługiwane/nieprawidłowe języki docelowe.
- [x] Dodano walidację formatu i unikalności identyfikatorów badań obrazowych.
- [x] Dodano kontrolę rzeczywistej daty badania, a nie tylko jej formatu.
- [x] Dodano kontrolę formatu i unikalności identyfikatorów załączników w obrębie badania.
- [x] Formularz wyświetla błędy walidacji modelu i blokuje zapis, dopóki dane nie są poprawne.
- [x] Dodano testy dla nieobsługiwanych języków, powtórzonych identyfikatorów badań, błędnych dat, nieprawidłowych identyfikatorów załączników oraz poprawnego zestawu danych.
- [x] Zmiany kodu i testów zapisano w commitach `f23b19150ffd4b9343c2b386d517cc89810241e5`, `9301be32d24f816e52f8488494980410701465ba` i `0b492edaa21a37dbfde6c8d40b5105cfcb324932`.

### Weryfikacja CI
- [x] Dla poprzedniego SHA `5416625dbe1bd9500f8af0f28cfa077d21fc6b1b` potwierdzono uruchomienie dwóch workflow Android (build i GUI) oraz powodzenie REUSE.
- [ ] W chwili kontroli oba workflow Android dla SHA `5416625` były nadal w toku; nie stanowią potwierdzenia zmian wprowadzonych w sekcji 38.
- [ ] Nowe testy JVM i pełny build wymagają wyniku CI dla końcowego SHA po tej sekcji.
- [ ] Nadal wymagane są testy instrumentacyjne magazynu i obrazów, testy restartu procesu, integracja eksportu/importu oraz weryfikacja PDF/RTL.

- [x] Dodatkowo zabezpieczono dekodowanie już zapisanych badań: odrzuca ono powtórzone/niebezpieczne identyfikatory, nieprawidłowe daty oraz nieprawidłowe lub powtórzone identyfikatory załączników, zanim rekordy trafią do interfejsu.
- [x] Zastąpiono identyfikator oparty na milisekundowym znaczniku czasu identyfikatorem UUID, eliminując ryzyko kolizji przy szybkim dodawaniu kolejnych badań.
- [x] Dodano testy regresyjne dla uszkodzonych zapisanych rekordów badań.
- [x] Dodatkowe zmiany zapisano w commitach `07e7b3008291a0a1572363565e2d286996b5902a` i `c96f276a91b544c8fcfd5563086c2ada662716ad`.
