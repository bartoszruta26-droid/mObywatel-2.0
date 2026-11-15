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
