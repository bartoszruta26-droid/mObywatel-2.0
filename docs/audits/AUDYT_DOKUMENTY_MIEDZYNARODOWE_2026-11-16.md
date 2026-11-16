SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Audyt katalogu dokumentów międzynarodowych — wymagania prawne, treściowe i graficzne

- Data audytu: 2026-11-16
- Repozytorium: `bartoszruta26-droid/mObywatel-2.0`
- Gałąź audytowa: `audit/international-documents-catalog`
- Baza audytu: `main` — `8d663027d165537c1c28bbde8718000255fd388b`
- Zakres: dokumenty międzynarodowe wskazane do dodania do aplikacji, ich status prawny w Polsce, pola, wystawca, podmiot potwierdzający, wzór i zasady identyfikacji graficznej.
- Charakter: audyt projektowy oparty na źródłach urzędowych. Przed wdrożeniem każdego formularza należy ponownie sprawdzić aktualny akt prawny, załącznik ze wzorem i instrukcję organu wydającego.

## 1. Wnioski wykonawcze

1. Katalog należy dodać jako grupę nadrzędną **Dokumenty międzynarodowe**, z podgrupami: medyczne i zdrowotne; szczepienia i profilaktyka; recepty i farmaceutyczne; ubezpieczenia zdrowotnego; rehabilitacyjne; niepełnosprawności; zawodowe i kwalifikacyjne; edukacyjne; stanu cywilnego; sądowe i urzędowe; ubezpieczeniowe i socjalne; podróży i tożsamości.
2. Nie wolno odtwarzać urzędowego wzoru „na oko”. Jeżeli istnieje ustawowy/konwencyjny wzór, trzeba odwzorować go z właściwego załącznika, łącznie z kolejnością pól, językami, wymiarami, oznaczeniami i regułami podpisu.
3. Aplikacja może tworzyć: (a) dokument roboczy/informacyjny, (b) raport lub zaświadczenie wystawiane przez uprawnionego profesjonalistę, jeśli spełniono warunki prawne, (c) wniosek do organu. Aplikacja nie może udawać, że wydaje EKUZ, S1/S2, A1, ICVP, dokument stanu cywilnego, zaświadczenie o niekaralności, dyplom ani prawo jazdy.
4. Każdy typ otrzyma metadane `legalStatus`, `issuerType`, `verificationAuthority`, `officialTemplateSource`, `canGenerateOfficialDocument`, `visualProfile`, `watermarkPolicy`, `signaturePolicy` i `validityPolicy`.
5. Znak wodny nie może imitować pieczęci państwowej, NFZ, ZUS, ministerstwa, sądu, uczelni, izby zawodowej ani organizacji międzynarodowej. Dla dokumentu nieurzędowego stosować czytelne oznaczenie: **WZÓR / DOKUMENT INFORMACYJNY — NIE JEST DOKUMENTEM URZĘDOWYM**. Dla tłumaczenia maszynowego: **TŁUMACZENIE ROBOCZE — WYMAGA WERYFIKACJI**.
6. W aplikacji nie używać godła RP, flag, logotypów ani pieczęci instytucji jako dekoracji dokumentu. Dopuszczalne są wyłącznie zgodne z prawem oznaczenia źródłowe, w tym wizerunki oficjalnych wzorów w celach informacyjnych, bez sugerowania wydania dokumentu przez organ.

## 2. Klasy statusu i reguły wydawania

| Klasa | Znaczenie | Co może zrobić aplikacja | Oznaczenie |
|---|---|---|---|
| O1 — urzędowy dokument organu | Dokument wydaje właściwa instytucja według obowiązującego wzoru | Wniosek, lista wymaganych danych, kopia użytkownika, instrukcja; bez samodzielnego wystawiania oryginału | „Wydaje: [organ]”; nie generować urzędowej repliki |
| O2 — dokument urzędowy/koordynacyjny wydany przez instytucję zagraniczną | Dokument zagraniczny uznawany w określonym zakresie na podstawie prawa UE/umów | Informacyjna karta danych i kontrola kompletności; nie tworzyć numeru ani potwierdzenia uprawnień | „Wzór informacyjny — dokument wydaje instytucja właściwa” |
| P1 — zaświadczenie profesjonalisty | Wystawia uprawniony lekarz lub inny profesjonalista w granicach kompetencji | Formularz edytowalny, PDF do podpisu, identyfikacja wystawcy i statusu uprawnień | „Do podpisu i potwierdzenia przez wystawcę” |
| I1 — dokument informacyjny pacjenta | Zestawienie danych pacjenta bez samodzielnego skutku urzędowego | Można wygenerować i aktualizować | „Informacja medyczna pacjenta” |
| W1 — wniosek | Dokument inicjujący postępowanie | Wypełnić i wyeksportować; wysłać kanałem organu | „Wniosek — nie potwierdza przyznania prawa” |
| E1 — dokument edukacyjny/zawodowy | Dyplom, suplement, potwierdzenie kwalifikacji lub statusu | Ewidencja, tłumaczenie robocze, lista załączników | „Kopia / zestawienie — nie zastępuje dokumentu wydanego przez instytucję” |

## 3. Specyfikacja dokumentów medycznych

### 3.1 Międzynarodowa Książeczka Szczepień / ICVP (żółta książeczka)
- Klasa: O1/O2 — oficjalny certyfikat zgodny z Międzynarodowymi Przepisami Zdrowotnymi (IHR).
- Wystawca/wypełniający: uprawniony lekarz lub inny upoważniony pracownik medyczny nadzorujący podanie szczepionki/profilaktyki; dokument zawiera również oficjalną pieczęć ośrodka.
- Potwierdzenie: własnoręczny podpis osoby uprawnionej i oficjalna pieczęć ośrodka; sama pieczęć nie zastępuje podpisu. Szczepionka/profilaktyka musi spełniać wymogi WHO.
- Pola: imię i nazwisko; data urodzenia; płeć; obywatelstwo; dokument tożsamości (jeśli dotyczy); podpis posiadacza; nazwa choroby/warunku; nazwa szczepionki lub profilaktyki; data; podpis i tytuł zawodowy osoby nadzorującej; pieczęć ośrodka; data ważności, jeśli ma zastosowanie.
- Wzór/grafika: dokładny wzór z załącznika do rozporządzenia MZ z 27.09.2023 r. i właściwego wzoru IHR; żółta książeczka jest rozpoznawalnym nośnikiem, ale kolor sam nie nadaje ważności.
- Znak wodny: brak dodatkowego znaku aplikacji na oficjalnym wzorze; wersja demonstracyjna musi być oznaczona „WZÓR — NIEWAŻNY DO UŻYCIA”.
- Aplikacja: nie wystawiać prawdziwego ICVP; udostępnić informację, checklistę i ewentualnie nieoficjalną kartę historii szczepień.

### 3.2 Europejska Karta Ubezpieczenia Zdrowotnego (EKUZ)
- Klasa: O1 — kartę wydaje właściwa instytucja ubezpieczenia zdrowotnego (w Polsce NFZ).
- Potwierdzenie: wydanie przez NFZ; autentyczność weryfikuje instytucja/świadczeniodawca według właściwych procedur.
- Pola modelu ewidencyjnego: imię i nazwisko; identyfikator/PESEL lub odpowiednik; data urodzenia (jeśli przewidziana); numer karty; kod państwa/instytucji; data ważności; dane instytucji wydającej. Nie dopisywać pól, których nie ma na aktualnym wzorze karty.
- Wzór/grafika: wyłącznie aktualny wzór EKUZ i jego specyfikacja instytucjonalna; karta indywidualna dla każdej osoby.
- Znak wodny: nie dodawać do oryginalnej karty; w aplikacji wyłącznie neutralna karta informacyjna z etykietą „Dane EKUZ — nie jest kartą EKUZ”.
- Aplikacja: ewidencja numeru i daty ważności, przypomnienie, instrukcja i link do wniosku NFZ; nie generować karty zastępczej.

### 3.3 Certyfikat tymczasowo zastępujący EKUZ (PRC)
- Klasa: O1 — wydaje właściwa instytucja ubezpieczeniowa/NFZ; w Polsce dostępny również elektronicznie w określonych procedurach.
- Potwierdzenie: NFZ lub właściwa instytucja; dokument ma okres obowiązywania związany z konkretnym uprawnieniem/świadczeniem.
- Pola: dane identyfikacyjne osoby; okres, na który certyfikat jest wydany; dane instytucji; identyfikator/oznaczenie dokumentu i data wydania zgodnie z aktualnym wzorem.
- Wzór: oficjalny PRC NFZ/UE, nie projektować własnego odpowiednika.
- Grafika/znak: oryginalny layout instytucji; aplikacja pokazuje wyłącznie kartę danych i status „oczekuje na wydanie / wydany / wygasł”.

### 3.4 Dokument S1
- Klasa: O1/O2 — dokument koordynacji zabezpieczenia społecznego; w Polsce wydaje NFZ osobie spełniającej warunki.
- Potwierdzenie: NFZ wydaje, a instytucja państwa zamieszkania rejestruje zgodnie z procedurą tego państwa.
- Pola do ewidencji: dane osoby uprawnionej; członkowie rodziny, jeśli obejmuje; państwo i instytucja miejsca zamieszkania; właściwa instytucja ubezpieczeniowa; okres uprawnienia; podstawa/typ uprawnienia; data wydania i numer/referencja zgodnie z dokumentem.
- Wzór: aktualny formularz S1 i wniosek NFZ; nie zastępować go ogólnym zaświadczeniem.
- Grafika: neutralna karta „S1 — ewidencja dokumentu”; nie używać logotypu NFZ jako własnego brandingu.

### 3.5 Dokument S2 / dawny E112 — zgoda na leczenie planowane
- Klasa: O1/O2 — wydaje właściwa instytucja ubezpieczeniowa po wyrażeniu zgody na określone leczenie.
- Potwierdzenie: instytucja właściwa; zakres świadczeń wynika z wydanej zgody.
- Pola ewidencyjne: dane pacjenta; państwo i instytucja leczenia; zakres/rodzaj zatwierdzonego leczenia; okres ważności; warunki zgody; numer dokumentu; data wydania.
- Wzór: oficjalny S2/E112; aplikacja nie może sama zatwierdzić leczenia.
- Grafika: bez pieczęci i symboli organu w generowanym podglądzie; status zgody musi pochodzić z dokumentu źródłowego.

### 3.6 DA1 / E123 — leczenie skutków wypadku przy pracy lub choroby zawodowej
- Klasa: O1/O2.
- Wystawca: właściwa instytucja zabezpieczenia społecznego; w Polsce obieg obejmuje NFZ i instytucję właściwą dla ubezpieczenia wypadkowego.
- Potwierdzenie: właściwa instytucja i ewentualna rejestracja w NFZ.
- Pola ewidencyjne: dane osoby; państwo ubezpieczenia; instytucja właściwa; zdarzenie (wypadek/choroba zawodowa); zakres i okres uprawnienia; numer dokumentu; rejestracja/poświadczenie.
- Wzór: oficjalny DA1/E123 i odpowiednie poświadczenie NFZ; nie tworzyć własnego dokumentu uprawniającego.
- Grafika: osobna ikona „wypadek przy pracy”; dokument źródłowy jako załącznik, status rejestracji oddzielnie.

### 3.7 Recepta transgraniczna / recepta wystawiona w innym państwie UE
- Klasa: P1/O2 — receptę wystawia osoba uprawniona zgodnie z prawem państwa wystawienia; realizacja w Polsce podlega polskim i unijnym regułom.
- Potwierdzenie: dane i uprawnienia osoby wystawiającej oraz wymogi formalne recepty; farmaceuta weryfikuje możliwość realizacji.
- Pola minimalne do modelu: dane pacjenta; data urodzenia; data wystawienia; dane i kwalifikacje/uprawnienia osoby wystawiającej; adres zawodowy i kontakt; podpis (właściwy dla postaci recepty); nazwa substancji czynnej (preferowana INN), postać, moc, ilość, dawkowanie; dane apteki/realizacji tylko w części realizacyjnej. Dodatkowe elementy zgodnie z prawem państwa wystawienia i polskimi wyjątkami.
- Wzór: nie istnieje jeden identyczny wzór graficzny wszystkich państw; zachować wymagane elementy treści i oryginalny dokument.
- Grafika: czysty układ medyczny, bez godła i fikcyjnej pieczęci; oznaczyć język oryginału i tłumaczenie. Podpis lekarza nie może być syntetycznie generowany.
- Aplikacja: oddzielić dokument „recepta zagraniczna” od „recepta transgraniczna”; status zgodności powinien być walidacją danych, nie obietnicą realizacji.

### 3.8 Karta informacji medycznej / Medical Summary
- Klasa: I1/P1 zależnie od tego, czy sporządza ją pacjent, czy lekarz.
- Wystawca: pacjent (samozestawienie) albo lekarz/placówka (raport kliniczny).
- Potwierdzenie: podpis pacjenta dla informacji własnej; podpis i identyfikacja zawodowa lekarza dla raportu lekarskiego.
- Pola: dane pacjenta; język; data aktualizacji; rozpoznania z kodami i wersją klasyfikacji; choroby przewlekłe; przebyte operacje/hospitalizacje; leki (substancja, dawka, droga, częstość); alergie i reakcje; szczepienia; istotne wyniki; urządzenia/implanty; dane kontaktowe lekarza; osoba kontaktowa; zgoda na udostępnienie.
- Wzór: elastyczny, zgodny z interoperacyjnością i minimalizacją danych; nie przedstawiać jako oficjalnego zaświadczenia.
- Grafika: białe tło, granatowo-niebieskie akcenty, czytelne sekcje; znak „Informacja medyczna — nie zastępuje dokumentacji źródłowej”.

### 3.9 Karta alergii / Allergy Card
- Klasa: I1.
- Wystawca: pacjent; dane mogą być potwierdzone przez lekarza.
- Pola: dane pacjenta; alergeny; rodzaj i ciężkość reakcji; data/źródło rozpoznania; leki ratunkowe; plan postępowania; kontakt alarmowy; lekarz/placówka i data weryfikacji.
- Potwierdzenie: lekarz tylko jeśli zweryfikował rozpoznanie; wyraźnie rozróżnić alergię potwierdzoną, podejrzenie i nietolerancję.
- Wzór/grafika: kieszonkowa karta wielojęzyczna; kontrastowe oznaczenie alergii, ale bez symbolu urzędowego; watermark „Dane podane przez pacjenta” lub „Zweryfikowano przez lekarza” zgodnie ze stanem.

### 3.10 Karta leków / Medication Card
- Klasa: I1.
- Wystawca: pacjent lub lekarz/farmaceuta jako zestawienie.
- Pola: nazwa handlowa i substancja czynna; moc; postać; dawka; droga; częstość; wskazanie; data rozpoczęcia/zakończenia; alergie/ostrzeżenia; lekarz prowadzący; data aktualizacji.
- Potwierdzenie: lekarz/farmaceuta może zweryfikować listę; aplikacja nie potwierdza przyjmowania ani zaleceń.
- Wzór/grafika: tabela czytelna w podróży; oznaczenia ostrzegawcze tylko na podstawie danych; watermark „Lista leków — sprawdź aktualność”.

### 3.11 Fit to Travel / Fit to Fly
- Klasa: P1 — zaświadczenie lekarskie; nie ma jednego uniwersalnego urzędowego wzoru uznawanego przez wszystkie linie lotnicze i kraje.
- Wystawca: lekarz uprawniony, po ocenie klinicznej.
- Potwierdzenie: podpis i dane zawodowe lekarza; linia lotnicza/przewoźnik może wymagać własnego formularza i dodatkowej zgody.
- Pola: pacjent i data urodzenia; cel (podróż/lot); data badania; rozpoznania istotne dla podróży; ocena stabilności; ograniczenia i wymagane wsparcie; tlen/urządzenia/transport leków; ryzyko zakaźne, jeśli właściwe; okres ważności opinii; dane lekarza i placówki; podpis.
- Wzór: szablon aplikacji wyłącznie jako wzór opinii; przed użyciem wskazać konieczność sprawdzenia wymagań przewoźnika i kraju.
- Grafika: minimalistyczny układ kliniczny; watermark „Wzór opinii — decyzję o przewozie podejmuje przewoźnik zgodnie z własnymi zasadami”.

### 3.12 Raport rehabilitacyjny / Rehabilitation Report
- Klasa: P1/I1 zależnie od wystawcy.
- Wystawca: fizjoterapeuta lub lekarz w zakresie swoich kompetencji i przepisów.
- Potwierdzenie: podpis, tytuł zawodowy, numer prawa wykonywania zawodu/uprawnienia, dane podmiotu; nie każdy raport wymaga pieczęci urzędowej.
- Pola: dane pacjenta; rozpoznanie i skierowanie; cel rehabilitacji; ocena początkowa i końcowa; zakres ruchu; siła mięśniowa; ból; funkcja i aktywność; użyte skale z wersją i datą; plan i liczba sesji; tolerancja/reakcje; zalecenia; autor i data.
- Wzór: raport kliniczny oparty na ICF, z możliwością dołączenia wyników; brak jednego międzynarodowego wzoru obowiązującego w Polsce.
- Grafika: neutralny szablon medyczny, wykresy funkcjonalne; watermark „Raport rehabilitacyjny — dokument kliniczny”.

### 3.13 Zaświadczenie o niepełnosprawności / funkcjonowaniu
- Klasa: P1/I1; formalne orzeczenie to odrębny dokument O1.
- Wystawca: orzeczenie wydaje właściwy organ/komisja; lekarz i terapeuta mogą sporządzić opinię medyczną/funkcjonalną w granicach kompetencji.
- Potwierdzenie: komisja/orzecznik lub właściwy organ; podpis terapeuty nie zastępuje orzeczenia.
- Pola opinii: dane pacjenta; rozpoznania; trwałość; ograniczenia czynności; mobilność; samoobsługa; komunikacja; praca/nauka; urządzenia wspomagające; zakres pomocy; skale i wyniki; data i autor.
- Wzór: oddzielić „opinię funkcjonalną” od urzędowego „orzeczenia o niepełnosprawności”.
- Grafika: neutralna, bez symbolu urzędu; wyraźna etykieta typu dokumentu i organu właściwego.

## 4. Dokumenty ubezpieczeniowe i socjalne

### 4.1 A1 — ustawodawstwo właściwe w zakresie zabezpieczenia społecznego
- Klasa: O1/O2.
- Wystawca: właściwa instytucja państwa ubezpieczenia; w Polsce ZUS lub KRUS zależnie od sytuacji.
- Potwierdzenie: instytucja wydająca; weryfikacja autentyczności według kanału elektronicznego/urzędowego.
- Pola ewidencyjne: osoba; pracodawca/płatnik lub działalność; państwa wykonywania pracy; okres; podstawa; instytucja wydająca; numer i data.
- Wzór: oficjalny dokument A1, często elektroniczny; aplikacja tylko przechowuje oryginał i metadane.
- Grafika: karta indeksowa, nie imitacja zaświadczenia ZUS/KRUS.

### 4.2 U1 — okresy ubezpieczenia/zatrudnienia dla bezrobocia
- Klasa: O1/O2.
- Wystawca: właściwa instytucja/urząd zatrudnienia państwa, w którym okresy zostały uzyskane.
- Potwierdzenie: urząd/instytucja wydająca; organ państwa rozpatrującego świadczenie ocenia uprawnienie.
- Pola ewidencyjne: dane osoby; państwa i instytucje; okresy zatrudnienia/ubezpieczenia; pracodawcy; daty; numer; data wydania.
- Wzór: oficjalny U1; nie generować potwierdzonych okresów na podstawie deklaracji użytkownika.

### 4.3 U2 — zachowanie prawa do zasiłku podczas poszukiwania pracy w innym państwie
- Klasa: O1/O2.
- Wystawca: właściwa instytucja/urząd zatrudnienia państwa wypłacającego świadczenie.
- Potwierdzenie: instytucja wydająca i urząd państwa poszukiwania pracy zgodnie z procedurą.
- Pola: dane osoby; okres eksportu świadczenia; państwo docelowe; termin rejestracji; instytucja; numer i data.
- Wzór: oficjalny U2; aplikacja nie może przyznawać ani przedłużać zasiłku.

### 4.4 Dokumenty zwrotu kosztów leczenia / wniosek refundacyjny NFZ
- Klasa: W1/O1.
- Wystawca: wniosek składa uprawniona osoba; rozstrzyga właściwy NFZ.
- Potwierdzenie: NFZ po weryfikacji dokumentów, rachunków i podstawy prawnej.
- Pola: wnioskodawca; pacjent; państwo i placówka; daty świadczeń; rodzaj świadczeń; koszty/waluta; rachunki; dane rachunku bankowego; podstawa; oświadczenia; załączniki.
- Wzór: aktualny formularz NFZ (DOC/PDF) i instrukcja; nie tworzyć zamiennego formularza jako oficjalnego.
- Grafika: formularz administracyjny, bez znaków NFZ w nagłówku aplikacji.

## 5. Dokumenty zawodowe i edukacyjne

### 5.1 Europejska Legitymacja Zawodowa (EPC)
- Klasa: O1 — procedura elektroniczna dla wybranych zawodów, nie uniwersalna karta dla każdego zawodu.
- Wystawca: właściwy organ państwa pochodzenia/przyjmującego w procedurze EPC.
- Potwierdzenie: organ właściwy w systemie IMI; nie sama aplikacja.
- Dane wniosku: tożsamość i obywatelstwo; zawód; państwo pochodzenia i przyjmujące; kwalifikacje/dyplomy; legalne wykonywanie zawodu; dobra reputacja/niekaralność, jeśli wymagana; brak zawieszenia/zakazu; ubezpieczenie; doświadczenie i szkolenia.
- Wzór: elektroniczny wynik/procedura EPC w systemie UE; nie projektować plastikowej legitymacji udającej EPC.
- Grafika: kafelek informacyjny z odnośnikiem do systemu UE; watermark „Zestawienie danych do wniosku EPC”.

### 5.2 Zaświadczenie o statusie zawodowym / Good Standing
- Klasa: O1/P1 zależnie od zawodu i organu.
- Wystawca: właściwa izba, regulator lub organ rejestrujący.
- Potwierdzenie: rejestr zawodowy/regulator; weryfikacja aktualności i braku ograniczeń.
- Pola: pełne dane osoby; zawód/specjalność; numer rejestracyjny; data wpisu; status; ograniczenia/zawieszenia; informacja o dobrej reputacji, jeśli organ ją potwierdza; data wydania; podpis i pieczęć/kwalifikowany podpis organu.
- Wzór: wzór właściwej izby/regulatora; brak jednego ogólnoeuropejskiego szablonu.
- Grafika: szablon informacyjny; oficjalny znak izby wyłącznie w oryginalnym załączniku.

### 5.3 Dyplom, świadectwo kwalifikacji i suplement do dyplomu
- Klasa: E1/O1 — wydaje uprawniona szkoła/uczelnia/instytucja.
- Potwierdzenie: uczelnia, szkoła, organ uznający kwalifikacje lub właściwy rejestr.
- Pola ewidencyjne: imię i nazwisko; nazwa instytucji; program/kierunek; poziom; tytuł/kwalifikacja; daty; wynik; numer; podpisy i zabezpieczenia oryginału; suplement — efekty uczenia się, poziom, punkty ECTS, przebieg studiów, skala ocen.
- Wzór: oficjalny wzór instytucji i przepisy krajowe; aplikacja może tworzyć CV kwalifikacji, nie dyplom.
- Grafika: bez odtwarzania godła, hologramu, pieczęci, podpisów ani zabezpieczeń oryginału.

### 5.4 Uznanie specjalizacji/kwalifikacji medycznej
- Klasa: O1 — decyzja lub potwierdzenie właściwego organu.
- Wystawca: organ właściwy dla uznania kwalifikacji w Polsce (zależnie od zawodu i trybu).
- Potwierdzenie: właściwy minister/organ, izba lub instytucja uznająca.
- Pola wniosku: tożsamość; państwo i instytucja kształcenia; dyplom; specjalizacja; program i czas szkolenia; staż; doświadczenie; prawo wykonywania zawodu; dokumenty good standing; tłumaczenia i poświadczenia.
- Wzór: właściwy wniosek i załączniki urzędowe; nie generować decyzji o uznaniu.
- Grafika: formularz wniosku, osobny od dokumentu końcowego.

## 6. Dokumenty stanu cywilnego i urzędowe

### 6.1 Akt/odpis urodzenia, małżeństwa, zgonu
- Klasa: O1 — dokument urzędu stanu cywilnego.
- Wystawca: właściwy USC lub zagraniczny organ rejestracji stanu cywilnego.
- Potwierdzenie: organ wydający; w obrocie UE stosować rozporządzenie 2016/1191 w zakresie objętym jego zakresem.
- Pola ewidencyjne: dane osoby/osób; data i miejsce zdarzenia; rodzice/małżonkowie według rodzaju aktu; numer aktu; urząd/rejestr; data wydania; adnotacje; podpis/pieczęć lub walidacja elektroniczna zgodnie z oryginałem.
- Wzór: aktualny odpis urzędowy; wielojęzyczny standardowy formularz UE jest załącznikiem-pomocą tłumaczeniową, nie samodzielnym aktem.
- Grafika: przechowywać oryginał; aplikacja tworzy kartę indeksową, nie replikę aktu.

### 6.2 Zaświadczenie o zdolności prawnej do zawarcia małżeństwa / stanie cywilnym
- Klasa: O1.
- Wystawca: właściwy USC lub organ państwa pochodzenia.
- Potwierdzenie: organ wydający; przy użyciu zagranicznym weryfikacja zgodnie z przepisami kraju przyjmującego.
- Pola: tożsamość; stan cywilny; dane drugiej osoby, jeśli wymagane; brak przeszkód/zdolność; podstawa; data ważności; organ; podpis i pieczęć.
- Wzór: krajowy wzór zaświadczenia; dla UE możliwy wielojęzyczny formularz standardowy jako pomoc.
- Grafika: oryginał plus karta informacyjna; nie tworzyć urzędowego potwierdzenia.

### 6.3 Zaświadczenie o miejscu zamieszkania/pobycie, obywatelstwie, pozostawaniu przy życiu
- Klasa: O1.
- Wystawca: właściwy organ administracji/USC/notariusz zależnie od rodzaju zaświadczenia.
- Potwierdzenie: organ wydający; dla dokumentów objętych 2016/1191 obowiązują uproszczenia obiegu w UE.
- Pola: osoba; fakt potwierdzany; okres/adres; data; organ; numer; podpis/pieczęć; podstawa wydania.
- Wzór: właściwy krajowy formularz; wielojęzyczny formularz UE tylko w określonych kategoriach i jako załącznik tłumaczeniowy.
- Grafika: bez samodzielnego odtwarzania urzędowego wzoru.

### 6.4 Zaświadczenie o niekaralności
- Klasa: O1.
- Wystawca: Krajowy Rejestr Karny lub właściwy organ państwa obcego.
- Potwierdzenie: organ rejestru; apostille/zwolnienie z apostille zależnie od państwa, rodzaju dokumentu i właściwych przepisów.
- Pola: tożsamość; zakres zapytania; wynik; data; numer; organ; podpis/pieczęć lub walidacja elektroniczna.
- Wzór: urzędowy odpis/informacja; formularz wielojęzyczny UE może być dostępny jako pomoc tłumaczeniowa dla braku wpisu w rejestrze karnym.
- Grafika: wyłącznie indeks/załącznik oryginału.

### 6.5 Dokumenty rozwodowe, separacyjne, adopcyjne i dotyczące pochodzenia dziecka
- Klasa: O1.
- Wystawca: sąd, USC lub inny właściwy organ.
- Potwierdzenie: organ wydający; skutki uznania podlegają odrębnym przepisom, nie samemu potwierdzeniu autentyczności.
- Pola: strony; sygnatura; organ; rozstrzygnięcie; data prawomocności/skuteczności; zakres; podpis/pieczęć.
- Wzór: orzeczenie/odpis urzędowy i właściwe zaświadczenia UE; aplikacja nie tworzy rozstrzygnięć.
- Grafika: bez replikowania pieczęci sądowej i podpisów.

## 7. Tożsamość, podróż i dostępność

### 7.1 Międzynarodowe prawo jazdy (Konwencja Wiedeńska lub Genewska)
- Klasa: O1.
- Wystawca: starosta; wydawane na podstawie krajowego prawa jazdy.
- Potwierdzenie: podpis i pieczęć organu wydającego; ważność zależy od właściwej konwencji i krajowego prawa jazdy.
- Pola według wzoru: państwo i oznaczenie PL; numer; konwencja; ważne do; wydane przez; miejscowość; data; numer krajowego prawa jazdy; dane posiadacza i kategorie zgodnie z pełnym załącznikiem; podpis i pieczęć organu.
- Wzór/grafika: obowiązkowy wzór z aktualnego rozporządzenia o wzorach dokumentów stwierdzających uprawnienia do kierowania pojazdami; osobne wzory dla konwencji.
- Znak wodny: nie dodawać; demo oznaczyć „WZÓR — NIEWAŻNY”.
- Aplikacja: wniosek/checklista i przechowywanie skanu, nie wydawanie prawa jazdy.

### 7.2 Paszport, dokument podróży, karta pobytu
- Klasa: O1.
- Wystawca: właściwy organ państwa (w Polsce organ paszportowy/wojewoda/organ właściwy dla dokumentu).
- Potwierdzenie: organ wydający i systemy kontroli dokumentów.
- Pola ewidencyjne: imię i nazwisko; obywatelstwo; data urodzenia; numer; organ; data wydania i ważności; MRZ/identyfikatory wyłącznie jako dane z oryginału, jeśli prawnie i technicznie uzasadnione.
- Wzór: normy ICAO i krajowe zabezpieczenia; zakaz odtwarzania blankietu, MRZ, hologramów i zabezpieczeń.
- Grafika: wyłącznie karta indeksowa dokumentu; nie generować obrazu przypominającego dokument podróży.

### 7.3 Europejska karta parkingowa osoby z niepełnosprawnością
- Klasa: O1.
- Wystawca: właściwy organ krajowy/powiatowy zgodnie z polskimi przepisami.
- Potwierdzenie: uprawniony organ na podstawie właściwego orzeczenia.
- Pola: dane posiadacza; fotografia, jeśli wymaga wzór; data ważności; numer; organ; podpis/pieczęć; symbol i oznaczenie karty zgodne z oficjalnym wzorem.
- Wzór: aktualny zharmonizowany wzór UE i krajowe przepisy; aplikacja nie wydaje karty.
- Grafika: nie kopiować symbolu ani układu tak, by karta wyglądała na oryginał; tylko karta ewidencyjna.

## 8. Zasady wzorów, tła, znaków wodnych i podpisów

### 8.1 Oficjalne dokumenty O1/O2
- Oryginał należy pobrać/pozyskać od właściwego organu i zachować jako załącznik; aplikacja nie przerysowuje oryginału.
- Jeżeli prawo publikuje wzór w załączniku, odwzorowanie do celów informacyjnych musi mieć osobny wariant „podgląd wzoru”, nieaktywny formularz urzędowy.
- Tło: zachować oryginalne tło i zabezpieczenia tylko w kopii oryginału. W aplikacji używać neutralnego tła karty indeksowej.
- Pieczęć: nie generować, nie kopiować do nowego dokumentu i nie udostępniać biblioteki fikcyjnych pieczęci.
- Podpis: brak generowania podpisów odręcznych, pieczęci imiennych, podpisów kwalifikowanych i numerów urzędowych.
- Znak wodny: nie nakładać na oryginał; na kopii roboczej/preview oznaczać wyraźnie „KOPIA / PODGLĄD — NIE JEST ORYGINAŁEM”.

### 8.2 Dokumenty P1/I1/W1/E1 generowane przez aplikację
- Tło bazowe: białe lub bardzo jasne, wysoki kontrast, bez ornamentów utrudniających skanowanie/OCR.
- Nagłówek: nazwa typu dokumentu, status prawny, język, data sporządzenia i numer lokalny aplikacji (wyraźnie nieurzędowy).
- Stopka: „Wygenerowano w aplikacji; autentyczność i skutki zależą od podpisu/weryfikacji właściwego wystawcy” — tekst dopasować do typu.
- Watermark: zależnie od statusu: „WZÓR”, „DOKUMENT INFORMACYJNY”, „DO PODPISU”, „TŁUMACZENIE ROBOCZE”. Nie nakładać watermarku na treść kliniczną tak, by ją zasłaniał.
- Podpis: osobne pola pacjent / profesjonalista / tłumacz; określić czy jest to podpis odręczny, elektroniczny, czy tylko obraz podpisu. Obraz podpisu nie jest automatycznie podpisem elektronicznym.
- Tło tematyczne kafelka: medyczne dla zdrowotnych; neutralne administracyjne dla urzędowych; edukacyjne dla kwalifikacji; podróżne dla podróży. Nie stosować flag jako tła dokumentu, gdy mogłyby sugerować wydanie przez państwo.
- Flagi: tylko selektor języka/państwa lub informacyjny znacznik jurysdykcji; nie element uwierzytelniający.
- Herb/godło/logo: wyłącznie w oryginalnym pliku organu albo w ramach jawnej informacji o źródle, zgodnie z prawem; nigdy jako dekoracyjny znak aplikacji.
- QR: wyłącznie do lokalnego identyfikatora/bezpiecznego linku weryfikacyjnego, jeśli taki endpoint rzeczywiście istnieje. Nie generować QR udającego rejestr organu.
- Metadane PDF: autor aplikacji/organizacja tylko jako generator; nie wpisywać fikcyjnego organu wydającego.

## 9. Wymagany model katalogowy

Każdy dokument ma mieć stabilny identyfikator i definicję:
- `id`, `groupId`, `displayNamePl`, `displayNameEn`, `aliases[]`, `legalClass`;
- `jurisdictions[]`, `legalBasis[]`, `officialSourceUrls[]`, `officialTemplateUrl`;
- `issuer`, `verifier`, `eligibility`, `validityRules`;
- `requiredFields[]`, `conditionalFields[]`, `repeatableSections[]`;
- `signatureRequirements`, `stampRequirements`, `translationRules`;
- `visualProfile`, `backgroundAssetId`, `watermarkPolicy`, `logoPolicy`;
- `generationMode` = `officialRequest` / `informational` / `professionalCertificate` / `archiveOnly`;
- `canIssueOfficial` (domyślnie false), `requiresHumanVerification`, `lastLegalReviewAt`, `reviewStatus`.
Dane osobowe i medyczne muszą być przechowywane w istniejącym szyfrowanym magazynie, nie w jawnych preferencjach aplikacji. Dokumenty urzędowe przechowywać jako oryginalne pliki, a nie rekonstrukcję pól, jeżeli nie jest to konieczne.

## 10. Grupy i lista pozycji do katalogu

| Grupa | Dokumenty do dodania / obsługi | Tryb |
|---|---|---|
| Medyczne i zdrowotne | Międzynarodowe zaświadczenie lekarskie; karta informacji medycznej; Fit to Travel; Fit to Fly | P1/I1 |
| Szczepienia i profilaktyka | ICVP / Międzynarodowa Książeczka Szczepień; karta historii szczepień | O1/I1 |
| Recepty i farmaceutyczne | Recepta zagraniczna; recepta transgraniczna; karta leków; karta alergii | P1/I1/O2 |
| Ubezpieczenia zdrowotnego | EKUZ; PRC; S1; S2/E112; DA1/E123; wniosek o refundację | O1/O2/W1 |
| Rehabilitacyjne | Raport rehabilitacyjny; raport funkcjonalny | P1/I1 |
| Niepełnosprawności | Opinia funkcjonalna; ewidencja orzeczenia; europejska karta parkingowa (archiwum/checklista) | P1/I1/O1 |
| Zawodowe i kwalifikacyjne | EPC; Good Standing; zaświadczenie o prawie wykonywania zawodu; uznanie specjalizacji | O1/E1 |
| Edukacyjne | Dyplom; świadectwo; suplement do dyplomu; potwierdzenie przebiegu kształcenia | E1/O1 |
| Stanu cywilnego | Odpis urodzenia, małżeństwa, zgonu; zdolność do zawarcia małżeństwa; status cywilny; pozostawanie przy życiu | O1 |
| Sądowe i urzędowe | Zaświadczenie o niekaralności; rozwód/separacja; adopcja; pochodzenie dziecka; obywatelstwo; pobyt/zamieszkanie | O1 |
| Ubezpieczeniowe i socjalne | A1; U1; U2; DA1/E123 (referencja); wnioski o świadczenia | O1/O2/W1 |
| Podróży i tożsamości | Międzynarodowe prawo jazdy; paszport/dokument podróży/karta pobytu (archiwum); dokumenty podróży | O1/archiwum |

Uwaga: pozycje „archiwum/checklista” oznaczają rejestrację i przechowywanie oryginału, a nie generowanie dokumentu przez aplikację.

## 11. Źródła urzędowe i prawne

1. NFZ — Dokumenty do pobrania: EKUZ, PRC, S1, formularze i wnioski: https://www.nfz.gov.pl/dla-pacjenta/nasze-zdrowie-w-ue/leczenie-w-krajach-unii-europejskiej-i-efta/dokumenty-do-pobrania/
2. NFZ — dokumenty potwierdzające prawo do świadczeń w UE/EFTA/UK (EKUZ, PRC, S2/E112, S1): https://www.nfz.gov.pl/dla-pacjenta/nasze-zdrowie-w-ue/krajowy-punkt-kontaktowy/ueeog/mozliwosci-leczenia-w-ue/
3. Pacjent.gov.pl — S1 i DA1: https://pacjent.gov.pl/artykul/dokument-s1-lub-da1
4. NFZ — wzory dokumentów dla świadczeniodawców (EKUZ, PRC, S2, E123, DA1, S1): https://www.nfz.gov.pl/dla-swiadczeniodawcy/informacje-dla-swiadczeniodawcow-realizujacych-przepisy-o-koordynacji-/
5. Rozporządzenie MZ z 27.09.2023 r. w sprawie wykazu zalecanych szczepień ochronnych oraz Międzynarodowej Książeczki Szczepień, Dz.U. 2023 poz. 2056: https://eli.gov.pl/api/acts/DU/2023/2056/text.pdf
6. Rozporządzenie (UE) 2016/1191 — dokumenty urzędowe, wielojęzyczne formularze, granice skutku prawnego formularzy: https://eur-lex.europa.eu/eli/reg/2016/1191/oj
7. Europejski portal e-sprawiedliwość — dokumenty urzędowe i wielojęzyczne formularze: https://e-justice.europa.eu/topics/your-rights/public-documents_pl
8. Komisja Europejska — obieg dokumentów urzędowych; uproszczenie apostille nie przesądza o uznaniu skutków materialnych: https://commission.europa.eu/law/cross-border-cases/administrative-cooperation-circulation-public-documents_en
9. Your Europe — dokumenty wymagane do EPC i uznania kwalifikacji: https://europa.eu/youreurope/citizens/work/professional-qualifications/european-professional-card-documents/index_en.htm
10. Gov.pl — międzynarodowe prawo jazdy, wzory konwencji: https://www.gov.pl/web/gov/uzyskaj-miedzynarodowe-prawo-jazdy
11. ELI — aktualny wzór międzynarodowego prawa jazdy, Dz.U. 2025 poz. 1072: https://eli.gov.pl/api/acts/DU/2025/1072/text/O/D20251072.pdf
12. Ustawa o kierujących pojazdami — art. 10, wydanie międzynarodowego prawa jazdy przez starostę: https://api.sejm.gov.pl/eli/acts/DU/2024/1210/text.html
13. Komisja Europejska — transgraniczna opieka zdrowotna i ramy prawne: https://health.ec.europa.eu/cross-border-healthcare/toolbox-cross-border-healthcare_en

## 12. Plan wdrożenia i kryteria odbioru

### Etap A — zamknięcie audytu prawnego dla każdego dokumentu
- Zapisać przy każdym dokumencie dokładny akt prawny/załącznik i aktualny wzór, gdy istnieje.
- Sprawdzić państwa/jurysdykcje, języki, podpis, pieczęć, ważność, tryb elektroniczny i procedurę weryfikacji.
- Oznaczyć brak oficjalnego jednolitego wzoru jako jawny fakt, nie projektować fikcyjnego.
- Kryterium: żaden dokument O1/O2 bez źródła urzędowego i jasnego organu.

### Etap B — katalog i routing
- Dodać grupy i identyfikatory do katalogu, tras, ustawień widoczności, importu/eksportu i vCard tam, gdzie typ dokumentu ma odpowiednik kontaktowy.
- Każdy dokument otrzymuje status prawny i tryb generowania.
- Kryterium: testy unikalności ID, kompletności tras, migracji i eksportu/importu.

### Etap C — edytory i modele
- Edytory tworzyć tylko dla P1/I1/W1; dla O1/O2/E1 preferować wniosek, checklistę, ewidencję i bezpieczne przechowywanie oryginału.
- Powtarzalne sekcje, walidacja, język oryginału/tłumaczenia, zapis wersji i źródła danych.
- Kryterium: dane nie są gubione po zamknięciu i ponownym otwarciu aplikacji; migracje zachowują zgodność.

### Etap D — grafika i PDF
- Przygotować osobne profile graficzne dla: medyczny, szczepienia, farmaceutyczny, administracyjny, zawodowy, edukacyjny i podróżny.
- Dodać tła wektorowe do kafelków, a nie imitacje zabezpieczeń urzędowych.
- PDF-y P1/I1/W1/E1 mają wyraźne statusy, pola podpisu, źródło i numer lokalny.
- Kryterium: brak fałszywych pieczęci, herbów, podpisów, hologramów i znaków wodnych instytucji.

### Etap E — testy i kontrola jakości
- Testy jednostkowe modeli, pól wymaganych, statusów, dat i reguł ważności.
- Testy UI dla każdej grupy i każdego edytora.
- Testy PDF: wielostronicowość, fonty Unicode, języki RTL, łamanie tabel, znaki wodne i stopki.
- Testy importu/eksportu i odtwarzania danych po ponownym uruchomieniu.
- Przegląd prawny/medyczny treści przed oznaczeniem dokumentu jako gotowego.
- Kryterium: CI, testy emulatora i ręczna kontrola reprezentatywnych PDF-ów zakończone powodzeniem.

## 13. Stan audytu i ograniczenia

- Audyt katalogowy jest zapisany jako specyfikacja bazowa, nie jako zatwierdzenie prawne wszystkich możliwych wariantów krajowych.
- Najbardziej precyzyjnie zweryfikowane w tej iteracji: ICVP, EKUZ/PRC, S1, S2/E112, DA1/E123, obieg dokumentów UE 2016/1191, EPC i międzynarodowe prawo jazdy.
- Dla A1/U1/U2, Good Standing, dyplomów, kart parkingowych i poszczególnych dokumentów krajowych trzeba przed implementacją pobrać aktualny wzór i instrukcję właściwego organu dla konkretnej jurysdykcji.
- „Fit to Fly”, „Fit to Travel”, karta informacji medycznej, karta alergii, karta leków i raport rehabilitacyjny nie mają jednego uniwersalnego wzoru urzędowego uznawanego automatycznie w każdym państwie.
- Nie należy scalać do `main` katalogu i edytorów przed wykonaniem etapów A–E oraz testów.
