SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Audyt: Międzynarodowe zaświadczenie lekarskie

- Data audytu: 2026-11-15
- Repozytorium: `bartoszruta26-droid/mObywatel-2.0`
- Gałąź docelowa wskazana w zleceniu: `zaswaidczenie`
- Stan gałęzi: **nieodnaleziona**. GitHub zwraca listę 66 gałęzi i nie zawiera refu `zaswaidczenie` ani zbliżonej nazwy. Audyt zapisano na istniejącej gałęzi `main` wyłącznie jako dokument planistyczny. Przed implementacją należy utworzyć / wskazać właściwą gałąź roboczą.
- Zakres tego commitu: audyt wymagań i plan prac; brak zmian aplikacji i brak deklaracji ukończenia funkcji.

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
