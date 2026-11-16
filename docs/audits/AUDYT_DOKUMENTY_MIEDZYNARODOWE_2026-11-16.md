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

1. Dokumenty międzynarodowe należy rozdzielić między istniejące grupy aplikacji: **Służba zdrowotna**, **Służba finansowa**, **Służba zawodowa**, **Służba cywilna** i **Służba osobista**. Nie tworzyć nadrzędnej grupy „Dokumenty międzynarodowe” ani nowych podgrup.
2. Nie wolno odtwarzać urzędowego wzoru „na oko”. Jeżeli istnieje ustawowy/konwencyjny wzór, trzeba odwzorować go z właściwego załącznika, łącznie z kolejnością pól, językami, wymiarami, oznaczeniami i regułami podpisu.
3. Aplikacja może tworzyć: (a) dokument roboczy/informacyjny, (b) raport lub zaświadczenie wystawiane przez uprawnionego profesjonalistę, jeśli spełniono warunki prawne, (c) wniosek do organu. Aplikacja nie może udawać, że wydaje EKUZ, S1/S2, A1, ICVP, dokument stanu cywilnego, zaświadczenie o niekaralności, dyplom ani prawo jazdy.
4. Każdy typ otrzyma metadane `legalStatus`, `issuerType`, `verificationAuthority`, `officialTemplateSource`, `canGenerateOfficialDocument`, `visualProfile`, `watermarkPolicy`, `signaturePolicy` i `validityPolicy`.
5. Nie dodawać do wydruku automatycznych określeń „prywatny”, „nieoficjalny”, „demonstracyjny”, „nieformalny” ani podobnych, jeśli nie wymagają ich przepisy lub wzór źródłowy. Wydruk ma być przygotowany do weryfikacji, uzupełnienia, podpisania i opieczętowania przez właściwego wystawcę. Status procesu (np. przygotowany, podpisany, wydany, zweryfikowany) przechowywać w metadanych aplikacji, a nie jako zbędną adnotację na dokumencie.
6. Nazwy urzędów, organów, stanowisk, jednostek, symbole i elementy identyfikacji graficznej muszą odpowiadać rzeczywistemu wystawcy i aktualnemu wzorowi. Można wykorzystać opublikowany przez organ oficjalny wzór wraz z jego nagłówkiem, logotypem i tłem, jeżeli wzór jest przeznaczony do stosowania w danej procedurze. Nie tworzyć odcisków pieczęci, podpisów, hologramów, numerów seryjnych ani znaków wodnych, które wyglądałyby jak już naniesione przez urząd. Na wydruku pozostawić właściwe pola do rzeczywistego podpisu i pieczęci osoby uprawnionej.



## 1A. Docelowe przypisanie do istniejących grup aplikacji

Nie powstaje nowa grupa „Dokumenty międzynarodowe”. Poniższe 43 pozycje są przypisane do pięciu istniejących grup. Dwie pozycje — EKUZ i recepta zagraniczna — już istniały w katalogu i pozostają w swoich dotychczasowych grupach.

| Istniejąca grupa | Dokumenty |
|---|---|
| **Służba zdrowotna** | Międzynarodowa Książeczka Szczepień ICVP; karta informacji medycznej; karta alergii; karta leków; Fit to Travel; Fit to Fly; raport rehabilitacyjny; raport funkcjonalny; opinia funkcjonalna o niepełnosprawności; europejska karta parkingowa (ewidencja); EKUZ; PRC; S1; S2/E112; S3; DA1/E123; recepta transgraniczna; wniosek o refundację kosztów leczenia; recepta zagraniczna; międzynarodowe zaświadczenie lekarskie |
| **Służba finansowa** | A1; U1; U2 |
| **Służba zawodowa** | Europejska Legitymacja Zawodowa EPC; Good Standing; zaświadczenie o statusie zawodowym; uznanie kwalifikacji/specjalizacji; dyplom; świadectwo; suplement do dyplomu |
| **Służba cywilna** | Zagraniczny akt/odpis urodzenia; akt/odpis małżeństwa; akt/odpis zgonu; zaświadczenie o stanie cywilnym; zdolność do zawarcia małżeństwa; miejsce zamieszkania/pobytu; obywatelstwo; pozostawanie przy życiu; niekaralność; rozwód/separacja; adopcja; pochodzenie dziecka |
| **Służba osobista** | Międzynarodowe prawo jazdy; dokument podróży; karta pobytu; ewidencja paszportu/dokumentu tożsamości |

W istniejącym katalogu pozostają również grupy: Służba rodowa, Służba państwowa, Służba kościelna, Służba zbrojna, Służba fundacyjna, Służba gospodarcza, Służba stowarzyszeniowa i Zasoby i dokumenty własne. Nie są zmieniane przez tę klasyfikację.

## 1B. Kontrola kompletności wykazu

Audyt obejmuje wszystkie rodzaje dokumentów wymienione w pierwotnym zestawieniu: zdrowotne, szczepień, farmaceutyczne, ubezpieczenia zdrowotnego, rehabilitacyjne, niepełnosprawności, zawodowe, edukacyjne, stanu cywilnego, sądowe/urzędowe, ubezpieczeniowe/socjalne oraz podróży/tożsamości. Uwzględniono też dokumenty istniejące wcześniej w aplikacji: międzynarodowe zaświadczenie lekarskie, receptę zagraniczną, EKUZ i kartę szczepień.

Dodatkowe uwagi kompletności:
- S3 pozostaje pozycją rozszerzoną do ewidencji, ale jego zakres i zastosowanie należy każdorazowo potwierdzić według aktualnego formularza i sytuacji osoby uprawnionej.
- E112 jest historycznym oznaczeniem S2; E123 — DA1; E301 — U1; E303 — U2. Nazwy historyczne należy utrzymać jako wyszukiwalne aliasy, nie jako niezależne, równoległe dokumenty.
- „Europejskie prawo jazdy” nie jest odrębnym dokumentem międzynarodowym: chodzi o krajowe prawo jazdy UE, a poza UE może być potrzebne międzynarodowe prawo jazdy według właściwej konwencji.
- Paszport, karta pobytu i dokument podróży są dokumentami organów państwowych; aplikacja może obsługiwać ich ewidencję/original scan, a wnioski tylko zgodnie z formularzami właściwych organów.
- Dla Fit to Fly, Fit to Travel, karty informacji medycznej, alergii, leków i raportów rehabilitacyjnych nie ma jednego uniwersalnego wzoru obowiązującego wszystkie państwa. W takich przypadkach projektować profesjonalny formularz do wypełnienia i podpisu przez właściwego specjalistę, bez dopisywania fikcyjnego statusu urzędowego.

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
- Jeżeli właściwy organ publikuje formularz lub wzór przeznaczony do wypełniania, aplikacja powinna odwzorować go na podstawie aktualnego pliku źródłowego, z zachowaniem układu, kolejności pól, wersji językowych, wymiarów i instrukcji.
- W pierwszej kolejności używać oryginalnych formularzy do pobrania. Jeżeli organ udostępnia wzór w formacie PDF/DOC, preferować wypełnianie tego wzoru zamiast projektowania repliki.
- Nagłówek, nazwa organu, nazwy stanowisk, logotyp, godło, tło i elementy graficzne mogą być przeniesione wyłącznie z właściwego opublikowanego wzoru, w zakresie dozwolonym dla tego formularza.
- Pieczęć i podpis pozostają polami do rzeczywistego naniesienia przez uprawnionego urzędnika. Aplikacja nie generuje ich odcisków, podpisów ani numerów kancelaryjnych.
- Znak wodny instytucji wolno odtworzyć wyłącznie wtedy, gdy stanowi część oficjalnie opublikowanego wzoru i można go legalnie wykorzystać; nie projektować własnej imitacji zabezpieczenia.

### 8.2 Dokumenty przygotowywane do podpisu przez właściwego wystawcę
- Tło: stosować tło z urzędowego wzoru lub neutralne tło dokumentu medycznego/zawodowego, jeżeli nie istnieje wzór obowiązkowy.
- Nagłówek: prawidłowa nazwa dokumentu oraz rzeczywista nazwa i adres właściwego organu/placówki, jeśli dany typ dokumentu wymaga ich w nagłówku.
- Nazwa stanowiska: używać rzeczywistych nazw stanowisk i tytułów zawodowych przewidzianych dla organu/jurysdykcji; nie wpisywać nazwiska ani podpisu konkretnej osoby bez potwierdzenia.
- Pieczęć: pozostawić wyznaczone miejsce na pieczęć organu lub osoby uprawnionej; wymogi co do rodzaju pieczęci, otoku, herbu i rozmiaru pobierać z przepisu lub wzoru.
- Podpis: wyznaczyć pole dla rzeczywistego podpisu osoby uprawnionej oraz — gdy wymagane — daty, stanowiska i identyfikatora zawodowego.
- Znak wodny i logo: używać tylko oryginalnych elementów opublikowanego wzoru lub zatwierdzonego przez instytucję pliku identyfikacji; nie syntetyzować znaków wodnych ani zabezpieczeń.
- Tła tematyczne kafelków: medyczne, administracyjne, zawodowe, edukacyjne lub podróżne zgodnie z istniejącymi profilami graficznymi.
- Status weryfikacji i etap podpisu przechowywać w aplikacji. Nie drukować automatycznych zastrzeżeń, które nie należą do treści danego dokumentu.
- QR: umieszczać tylko wtedy, gdy wynika z urzędowego wzoru albo prowadzi do działającego, właściwego systemu weryfikacji.
- Metadane PDF: nie wpisywać fikcyjnego wystawcy ani urzędowego numeru rejestracyjnego.



### 8.3 Ograniczenia dotyczące wzorów urzędowych i formularzy wielojęzycznych
- Oficjalne formularze wielojęzyczne na podstawie rozporządzenia (UE) 2016/1191 są wydawane przez właściwy organ, opatrzone datą i podpisem oraz — gdy ma zastosowanie — pieczęcią/stemplem. Są załącznikiem tłumaczeniowym, a nie samodzielnym aktem. Aplikacja nie może przedstawiać samodzielnie wypełnionego formularza jako wydanego przez organ.
- Dla EKUZ, PRC, S1, S2, S3, DA1/E123 i refundacji NFZ należy korzystać z bieżących formularzy NFZ oraz instrukcji dla konkretnej procedury. S3 dotyczy kontynuacji leczenia dla emerytowanego pracownika przygranicznego.
- Dla międzynarodowego prawa jazdy należy odwzorować aktualny wzór z załącznika nr 2 lub 3 do rozporządzenia Ministra Infrastruktury z 5 sierpnia 2025 r. — zależnie od konwencji genewskiej albo wiedeńskiej.
- Pieczęć urzędowa w Polsce ma ustawowo określoną formę i jest używana przez podmioty uprawnione. Projekt aplikacji może przewidzieć pole na pieczęć oraz prawidłową nazwę organu, ale nie może wytwarzać odcisku pieczęci ani znaku wodnego udającego zabezpieczenie urzędowe.

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

## 10. Lista pozycji według istniejących grup aplikacji

Poniższa tabela jest docelową strukturą nawigacji. Nie dodaje nowych grup ani podgrup.

| Istniejąca grupa | Dokumenty |
|---|---|
| **Służba zdrowotna** | Międzynarodowe zaświadczenie lekarskie; recepta zagraniczna; recepta transgraniczna; ICVP / Międzynarodowa Książeczka Szczepień; karta informacji medycznej; karta alergii; karta leków; Fit to Travel; Fit to Fly; raport rehabilitacyjny; raport funkcjonalny; opinia funkcjonalna o niepełnosprawności; europejska karta parkingowa; EKUZ; PRC; S1; S2/E112; S3; DA1/E123; wniosek o refundację kosztów leczenia |
| **Służba finansowa** | A1; U1; U2 |
| **Służba zawodowa** | EPC; Good Standing; zaświadczenie o statusie zawodowym; uznanie kwalifikacji/specjalizacji; dyplom; świadectwo; suplement do dyplomu |
| **Służba cywilna** | Zagraniczny akt/odpis urodzenia; akt/odpis małżeństwa; akt/odpis zgonu; zaświadczenie o stanie cywilnym; zdolność do zawarcia małżeństwa; zaświadczenie o miejscu zamieszkania/pobycie; obywatelstwo; pozostawanie przy życiu; zaświadczenie o niekaralności; orzeczenie rozwodowe/separacyjne; dokument adopcyjny; dokument dotyczący pochodzenia dziecka |
| **Służba osobista** | Międzynarodowe prawo jazdy; dokument podróży; karta pobytu; paszport/dokument tożsamości |

Pozycje już obecne w aplikacji (EKUZ, karta szczepień, recepta zagraniczna i międzynarodowe zaświadczenie lekarskie) pozostają w swoich dotychczasowych grupach. Nie tworzyć ich duplikatów — należy rozszerzać istniejące pozycje lub dodać warianty i języki w ramach istniejącego identyfikatora, jeżeli to ten sam typ dokumentu.

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
14. NFZ — aktualne formularze EKUZ, PRC, S1, DA1/E123 i refundacji: https://www.nfz.gov.pl/dla-pacjenta/nasze-zdrowie-w-ue/leczenie-w-krajach-unii-europejskiej-i-efta/dokumenty-do-pobrania/
15. NFZ — zakres stosowania EKUZ, PRC, S1/S072, S2/E112, DA1/E123 i S3: https://www.nfz.gov.pl/dla-pacjenta/nasze-zdrowie-w-ue/krajowy-punkt-kontaktowy/leczenie-w-polsce-wynikajace-z-dyrektywy-transgranicznej/koordynacja-a-opieka-zdrowotna/
16. Portal e-Sprawiedliwość — formularze do dokumentów urzędowych, w tym zasady wydawania formularzy wielojęzycznych przez właściwe organy: https://online-forms.e-justice.europa.eu/public-documents_pl
17. Ustawa o godle, barwach i hymnie RP oraz o pieczęciach państwowych — tekst jednolity Dz.U. 2025 poz. 299: https://eli.gov.pl/eli/DU/2025/299/ogl
18. Rozporządzenie Ministra Infrastruktury z 5 sierpnia 2025 r. w sprawie wzorów dokumentów stwierdzających uprawnienia do kierowania pojazdami, Dz.U. 2025 poz. 1072: https://eli.gov.pl/api/acts/DU/2025/1072/text.pdf


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
- Kryterium: wykorzystywane są wyłącznie autentyczne pliki instytucji lub materiały przekazane / zatwierdzone przez uprawnionego dysponenta; nie wolno tworzyć imitacji podpisów, hologramów ani zabezpieczeń. Pieczęć graficzna może być osadzona wyłącznie wtedy, gdy jej użycie i zakres zastosowania są objęte udokumentowanym upoważnieniem.

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


## 10. Integracja audytu i kryteria techniczne generatora PDF

- Audyt został scalony do `main` w PR #77, a następnie włączony do gałęzi implementacyjnej dokumentów międzynarodowych.
- Implementacja musi utrzymywać osobno: autora/posiadacza danych, profesjonalnego wystawcę, organ wydający oryginał, organ weryfikujący, kanał złożenia wniosku oraz status obiegu.
- Statusy aplikacyjne (projekt, gotowy do przeglądu, podpisany przez wystawcę, złożony, wydany przez organ, zweryfikowany, wygasły, odrzucony) są metadanymi procesu. Nie mogą być automatycznie interpretowane jako dowód wydania dokumentu.
- Zasoby instytucjonalne wymagają metadanych pochodzenia: właściciel/dysponent, źródło HTTPS, podstawa prawa/licencji/zgody, numer referencyjny zgody, osoba weryfikująca, wersja i okres obowiązywania.
- Metadane pochodzenia są rejestrem dowodowym, a nie automatycznym stwierdzeniem legalności. Każde użycie musi być porównane z warunkami licencji/zgody i aktualnym wzorem instytucji.
- Generator PDF nie może syntetycznie nanosić pieczęci, odręcznych podpisów ani elementów zabezpieczających (hologramów). Pozostawia odpowiednie pola do rzeczywistego poświadczenia przez uprawnioną osobę.
- Logotypy, godła, flagi, herby i znaki wodne mogą być renderowane wyłącznie po uzupełnieniu i sprawdzeniu wymaganych danych pochodzenia oraz zakresu upoważnienia. Brak danych oznacza pominięcie grafiki, a nie zastępowanie jej imitacją.
- Dla dokumentów wydawanych wyłącznie przez organ PDF aplikacji może służyć jako wniosek, karta danych lub ewidencja. Nie może imitować oryginału ani nadawać numerów, uprawnień lub statusów, których nie wydał organ.
- Wydruki nie otrzymują automatycznych adnotacji o charakterze prywatnym lub demonstracyjnym, chyba że wymaga ich właściwy przepis lub źródłowy wzór. Status procesu pozostaje w metadanych.
- Przed scaleniem wymagane są testy katalogu/grup/tras/schematów, polityki zasobów instytucjonalnych, PDF, importu i eksportu vCard oraz testy Compose UI na emulatorze.


## 14. Uzupełnienie audytu — oświadczenie właściciela projektu i rozszerzenie obsługi znaków instytucjonalnych (2026-11-16)

### 14.1. Oświadczenie przekazane przez właściciela projektu

Właściciel projektu oświadczył w rozmowie z dnia 2026-11-16, że:
- posiada zezwolenie ministra na prowadzenie prac deweloperskich nad aplikacją;
- aplikacja ma zostać włączona do zasobów państwowych;
- w ramach prac chce wykorzystywać znaki graficzne, logotypy, znaki wodne, pieczęcie i inne materiały międzynarodowych oraz państwowych instytucji;
- dokumenty generowane w aplikacji będą następnie przedstawiane właściwym urzędnikom do ręcznego podpisu albo podpisu elektronicznego w odrębnym rządowym narzędziu.

Powyższe zapisano jako oświadczenie i informację przekazaną przez właściciela projektu. Sam wpis audytowy nie stanowi niezależnego potwierdzenia treści zezwolenia ani zakresu praw do każdego znaku konkretnej instytucji. Numer, data, organ wydający i zakres zezwolenia powinny zostać dopisane do rejestru upoważnień po przekazaniu danych dokumentu.

### 14.2. Docelowa polityka wykorzystania materiałów instytucjonalnych

Aplikacja ma technicznie obsługiwać:
- oficjalne logotypy, godła, herby, flagi i znaki wodne;
- autentyczne grafiki pieczęci przekazane lub zatwierdzone przez właściwą instytucję;
- oficjalne tła, układy stron, typografię, oznaczenia i elementy identyfikacji wizualnej;
- warianty językowe oraz wzory dokumentów właściwe dla kraju, instytucji i procedury.

Nie wolno zastępować autentycznych materiałów grafiką odtworzoną na podobieństwo oryginału. Dla każdego zasobu należy zapisać: właściciela/dysponenta, źródło, podstawę użycia, identyfikator zgody lub publikacji, zakres dozwolonych zastosowań, osobę weryfikującą, wersję oraz okres obowiązywania.

### 14.3. Rozróżnienie rodzajów elementów

- Pieczęć graficzna: może być umieszczana w PDF, jeżeli konkretny zasób jest autentyczny i jego wykorzystanie w tym rodzaju dokumentu jest wyraźnie dozwolone. W przeciwnym razie pozostaje pole do przyłożenia pieczęci przez urząd.
- Podpis odręczny: aplikacja nie odtwarza podpisu urzędnika. Pozostawia pole do podpisu lub przekazuje dokument do uprawnionego narzędzia podpisu elektronicznego.
- Podpis elektroniczny i pieczęć elektroniczna: są nakładane przez właściwy, uprawniony system lub certyfikat. Sam obraz pieczęci nie jest podpisem elektronicznym ani kwalifikowaną pieczęcią elektroniczną.
- Hologramy i zabezpieczenia antyfałszywkowe: nie są syntetyzowane ani odtwarzane jako imitacje. Można zachować oficjalne oznaczenia opisowe lub wizualizacje informacyjne tylko w granicach zatwierdzonego wzoru i uprawnienia.

### 14.4. Zmiany implementacyjne wynikające z oświadczenia

- Rozszerzono `InstitutionalAssetAuthorization` o jawny zbiór `permittedKinds`, czyli typów zasobów objętych upoważnieniem.
- Polityka renderowania dopuszcza pieczęć tylko wtedy, gdy jej typ znajduje się w zakresie upoważnienia, a dane źródła i okres obowiązywania są kompletne.
- Generator PDF pobiera pieczęć przez ten sam mechanizm autoryzowanych zasobów co logo, herb, flaga i znak wodny; przy braku zatwierdzonego pliku pozostawia pole na pieczęć.
- Podpis odręczny i hologram pozostają wyłączone z automatycznego renderowania.
- Testy jednostkowe obejmują pozytywny przypadek pieczęci z zakresem upoważnienia oraz odmowę dla elementów poza zakresem, podpisu i hologramu.

### 14.5. Kryteria odbioru

1. Każdy zasób instytucjonalny ma kompletny rekord pochodzenia i zakresu użycia.
2. Generator stosuje wyłącznie pliki przypisane do dokumentu i objęte aktualnym upoważnieniem.
3. PDF pozostawia pole podpisu dla urzędnika lub przekazuje plik do zewnętrznego systemu podpisu.
4. Obraz pieczęci nie jest przedstawiany jako kryptograficzna pieczęć elektroniczna.
5. Dla każdego wzoru zachowuje się kraj, instytucję, procedurę, język, wersję i datę obowiązywania.
6. Testy katalogu, grup, PDF, vCard, emulatora i kompilacji APK muszą przejść przed scaleniem PR.



### 14.6. Formularz biblioteki zasobów

- Ekran biblioteki zasobów otrzymał pola do wprowadzania źródła HTTPS, podstawy wykorzystania, numeru zgody / referencji publikacji, osoby weryfikującej, wersji, dat obowiązywania oraz listy dozwolonych typów zasobów.
- Dane są zapisywane w metadanych zasobu i wykorzystywane przez kontrolę renderowania PDF.
- Rozszerzono limit przechowywanych metadanych upoważnienia do 2048 znaków, aby kompletna referencja nie była obcinana przy zapisie lub imporcie.
- Brak kompletnych danych pozostawia zasób w bibliotece, ale blokuje jego automatyczne użycie w wydruku.


## 15. Zasoby graficzne instytucjonalne — pierwsze zasoby wbudowane (2026-11-16)

### 15.1. Dodane pliki

- `obywatel-android/app/src/main/assets/institutions/poland-national-flag.svg` — wektorowy układ flagi państwowej RP, zgodny z proporcją 8:5 i podziałem na dwa równe poziome pasy. Czerwień w pliku SVG jest przybliżeniem sRGB; w razie dostarczenia zatwierdzonego pliku master należy go podmienić.
- `obywatel-android/app/src/main/assets/institutions/european-union-flag.svg` — wektorowa reprodukcja flagi UE z 12 złotymi gwiazdami na niebieskim polu, zgodna z oficjalną geometrią i barwami flagi. Nie jest to zmodyfikowane logo Komisji Europejskiej ani znak programu finansowania.
- Oba zasoby dodano do `DocumentBrandStore.builtInAssets`, z metadanymi źródła, podstawy użycia, referencji, daty, wersji i zakresu `permittedKinds=["Flaga"]`. Są dostępne w bibliotece zasobów aplikacji i mogą być przypisywane do odpowiednich dokumentów.

### 15.2. Źródła i podstawa

1. Polska flaga: Dziennik Ustaw 2025 poz. 299 — tekst jednolity ustawy o godle, barwach i hymnie RP oraz o pieczęciach państwowych, art. 4–6 i załącznik nr 3. Art. 5 ust. 2 przewiduje prawo używania barw RP z zachowaniem szacunku. Źródło: https://dziennikustaw.gov.pl/D2025000029901.pdf
2. Flaga UE: oficjalna strona Unii Europejskiej, sekcja „Download the European flag”, wskazująca oficjalne reprodukcje w tym pliki SVG. Źródło: https://european-union.europa.eu/principles-countries-history/symbols/european-flag_en

### 15.3. Granice biblioteki i następne zasoby

- Flagi państwowe i flaga UE mogą być wbudowywane jako wzory graficzne zgodne z oficjalną geometrią i źródłami.
- Godło RP należy opierać na wzorze z załącznika nr 1 do ustawy i używać z poszanowaniem ograniczeń podmiotowych z art. 2a oraz ochrony symboli państwowych. Istniejący zasób graficzny wymaga porównania z urzędowym wzorem przed oznaczeniem go jako zgodnego.
- Pieczęcie urzędowe poszczególnych organów nie będą tworzone na podstawie domysłów ani syntetycznych imitacji. Biblioteka ma przyjmować autentyczne pliki źródłowe i pozwalać na ich podmianę po otrzymaniu materiałów; sam obraz pieczęci nie zastępuje podpisu ani pieczęci elektronicznej.
- Logo WHO wymaga wyraźnej pisemnej zgody WHO; logo Komisji Europejskiej wymaga uprzedniego uzyskania zgody i nie może być mylone z samą flagą UE. Wpisanie zezwolenia ministra na prace nad aplikacją nie jest automatycznie zgodą każdej odrębnej organizacji międzynarodowej na używanie jej logo.
- Dla każdego kolejnego zasobu zachowujemy oddzielny zakres użycia. Zasób może być w bibliotece, ale nie może być automatycznie renderowany w dokumencie, jeśli jego upoważnienie nie obejmuje konkretnego rodzaju użycia.

### 15.4. Kontrola techniczna

- Dodano pliki SVG w katalogu assets aplikacji.
- Dodano rekordy wbudowane z `permittedKinds` i metadanymi źródła.
- Do wykonania: test kompilacji i test renderowania SVG na Androidzie, test obecności zasobów w bibliotece oraz kontrola przypisań do dokumentów.


## 16. Dokumenty Stolicy Apostolskiej i Państwa Watykańskiego — katalog międzynarodowy (2026-11-16)

### 16.1. Rozdzielenie podmiotów i jurysdykcji

Stolica Apostolska i Państwo Watykańskie są odrębnymi podmiotami prawa międzynarodowego. Nie wolno łączyć ich dokumentów, organów wydających ani oznaczeń:
- paszport zwykły — Państwo Watykańskie (Governorate);
- paszport dyplomatyczny, służbowy i tymczasowy służbowy — Stolica Apostolska (Sekretariat Stanu);
- zaświadczenie o obywatelstwie — właściwa służba Państwa Watykańskiego;
- dokumenty chrztu, małżeństwa i zgonu — parafia/diecezja prowadząca właściwą księgę;
- zaświadczenie o stanie kanonicznym, dokument święceń i dokument nuncjatury — wyłącznie właściwa władza kościelna.

### 16.2. Dodane pozycje (11)

| Identyfikator | Dokument | Istniejąca grupa |
|---|---|---|
| `vatican_passport_ordinary` | Paszport zwykły Państwa Watykańskiego | Służba osobista |
| `holy_see_passport_diplomatic` | Paszport dyplomatyczny Stolicy Apostolskiej | Służba osobista |
| `holy_see_passport_service` | Paszport służbowy Stolicy Apostolskiej | Służba osobista |
| `holy_see_passport_temporary_service` | Tymczasowy paszport służbowy Stolicy Apostolskiej | Służba osobista |
| `vatican_citizenship_certificate` | Zaświadczenie o obywatelstwie watykańskim | Służba cywilna |
| `ecclesiastical_baptism_certificate` | Kościelny dokument chrztu — parafia / diecezja | Służba kościelna |
| `ecclesiastical_marriage_certificate` | Kościelny dokument małżeństwa — parafia / diecezja | Służba kościelna |
| `ecclesiastical_death_certificate` | Kościelny dokument zgonu — parafia / diecezja | Służba kościelna |
| `canonical_status_certificate` | Zaświadczenie o stanie kanonicznym | Służba kościelna |
| `clerical_ordination_certificate` | Dokument święceń / ordynacji — właściwa diecezja | Służba kościelna |
| `apostolic_nunciature_credential` | Dokument akredytacji / poświadczenia nuncjatury apostolskiej | Służba kościelna |

Wszystkie pozycje otrzymały wspólne pola jurysdykcji, wystawcy, organu wydającego, weryfikacji, procedury, języka źródłowego/tłumaczenia i źródła wzoru. Workflow rozróżnia dokumenty wydawane przez właściwy organ od zaświadczeń profesjonalnych.

### 16.3. Źródła urzędowe

1. Sekretariat Stanu Stolicy Apostolskiej — oficjalna dokumentacja flagi, herbów i pieczęci Państwa Watykańskiego oraz herbu Stolicy Apostolskiej: https://press.vatican.va/roman_curia/secretariat_state/documents/insigne/sp_ss_scv_stemma-bandiera-sigillo_en.html
2. Państwo Watykańskie — opis flagi i jej historii: https://www.vaticanstate.va/en/state-and-government/general-informations/flag.html
3. Broszura urzędowa „Holy See and Vatican City State Passports” (2025): https://epass.vatican.va/Brochure_2025_WEB_SERV_rev.pdf. Potwierdza rozdzielenie dwóch podmiotów, organów wydających oraz rodzajów paszportów; od 18 grudnia 2025 r. wskazuje nowe kody PP (zwykły) i PO (służbowy), przy zachowaniu PD i PT.
4. Oficjalny wykaz dokumentów papieskich: https://www.vatican.va/offices/papal_docs_list.html
5. Archiwum Stolicy Apostolskiej — oficjalne akty, kodeksy prawa kanonicznego i dokumenty: https://www.vatican.va/archive/index.htm

### 16.4. Zasoby graficzne i ograniczenia

- Zarejestrowano oficjalne źródła flagi, herbu Stolicy Apostolskiej, herbu Państwa Watykańskiego i pieczęci Państwa Watykańskiego.
- Nie dodano syntetycznej pieczęci ani imitacji herbu. Oficjalna strona wskazuje autorstwo/atrybucję niektórych reprodukcji; przed osadzeniem pliku należy zachować wskazane prawa i warunki.
- Dodano do biblioteki wektorową ilustrację flagi Państwa Watykańskiego opartą na oficjalnym opisie: `obywatel-android/app/src/main/assets/institutions/vatican-city-flag.svg`. Jest to autorska, uproszczona ilustracja, nie oficjalny plik master. Metadane ograniczają jej użycie do typu `Flaga`; przed użyciem w dokumencie wystawianym przez organ należy zastąpić ją zatwierdzonym plikiem źródłowym.\n- Nie dodano logo ani pieczęci jako gotowego wbudowanego assetu bez pozyskania właściwego pliku i potwierdzenia zakresu użycia.
- Paszporty są dokumentami wydawanymi przez właściwe organy. Aplikacja może przechowywać dane/oryginał i obsługiwać wniosek, ale nie może generować paszportu ani jego strony danych.

### 16.5. Weryfikacja

- Dodano test instrumentacyjny wykrywający i renderujący flagę watykańską w bibliotece zasobów.\n- Do wykonania: testy JVM katalogu i workflow dla 54 pozycji, testy edytorów i routingu, testy regresji vCard/PDF oraz CI Android.
- Dla dokumentów kościelnych wymagane jest ustalenie właściwej parafii/diecezji/ordynariusza i lokalnych reguł wydawania. Nie istnieje jeden uniwersalny formularz watykański dla wszystkich dokumentów parafialnych.


## 17. Uzupełnienie istniejących schematów dokumentów i naprawa kompilacji (2026-11-16)

### 17.1. Zakres zmian

Kontynuowano uzupełnianie istniejących 54 pozycji katalogu, bez dodawania nowej grupy i bez zmiany identyfikatorów:
- pola wspólne workflow otrzymały polskie etykiety, zachowując dotychczasowe stabilne klucze archiwum;
- formularze czterech rodzajów paszportów Państwa Watykańskiego / Stolicy Apostolskiej zawierają teraz wspólne dane ewidencyjne (posiadacz, wystawca, numer, daty, jurysdykcja, język i źródło wzoru) oraz pola specjalistyczne;
- pole kodu paszportu uwzględnia aktualne kody PP, PD, PO i PT oraz kody poprzednie PV i PS;
- zaświadczenie o obywatelstwie Państwa Watykańskiego otrzymało pola statusu, podstawy i dat nabycia/ustania, właściwej służby i referencji rejestru;
- dokumenty stanu kanonicznego, święceń, nuncjatury i ksiąg sakramentalnych zachowują pola właściwe dla tych dokumentów, uzupełnione wspólnymi polami ewidencyjnymi.

### 17.2. Źródło kodów paszportowych

Oficjalna broszura „Holy See and Vatican City State Passports” wskazuje:
- Państwo Watykańskie wydaje paszporty zwykłe: nowy kod PP, poprzedni PV;
- Stolica Apostolska wydaje paszporty dyplomatyczne: PD;
- Stolica Apostolska wydaje paszporty służbowe: nowy kod PO, poprzedni PS;
- tymczasowy paszport służbowy: PT.
Nowe kody PP i PO są stosowane w dokumentach wydawanych od 18 grudnia 2025 r. Źródło: https://epass.vatican.va/Brochure_2025_WEB_SERV_rev.pdf

### 17.3. Kontrola techniczna

W CI dla poprzedniego commitu wykryto błąd kompilacji Kotlin w pliku DocumentEditorSchemas.kt: gałęzie when zwracały niejednorodny typ listy (Triple i DocumentEditorFieldDefinition). Schemat poprawiono przez jawne łączenie wspólnego zestawu pól ewidencyjnych z polami specyficznymi, z zachowaniem kolejności i unikalności kluczy.

Dodano asercje regresyjne dla:
- kodów nowych i poprzednich paszportów;
- pól obywatelstwa watykańskiego;
- pól stanu kanonicznego;
- pól dokumentu nuncjatury.

Wynik CI dla commitu poprzedzającego poprawkę: REUSE compliance — sukces; kompilacja Kotlin i testy JVM — niepowodzenie na wskazanym błędzie typów; testy emulatora i APK nie zostały uruchomione. Nowa poprawka wymaga ponownego uruchomienia całego CI. PR pozostaje draftem i nie może zostać scalony przed pozytywnym wynikiem testów.


## 18. Zasoby heraldyczne Państwa Watykańskiego, Stolicy Apostolskiej i papiestwa (2026-11-16)

### 18.1. Nowe pliki SVG

Dodano do `obywatel-android/app/src/main/assets/institutions/` cztery autorskie ilustracje wektorowe:
- `vatican-city-state-coat-of-arms-2023.svg` — schemat herbu Państwa Watykańskiego oparty na opisie i załączniku B ustawy zasadniczej z 2023 r.;
- `holy-see-emblem-heraldic-reference.svg` — schemat emblematu Stolicy Apostolskiej z tiarą i skrzyżowanymi kluczami;
- `pope-leo-xiv-coat-of-arms-reference.svg` — uproszczona ilustracja herbu papieża Leona XIV na podstawie oficjalnego blazonu z 14 maja 2025 r.;
- `banner-holy-roman-church-historical.svg` — autorska rekonstrukcja historycznej chorągwi Kościoła Rzymskiego, oparta na opisie Governatoratu Państwa Watykańskiego.

Każdy plik ma nagłówek SPDX CC0 dla autorskiej warstwy ilustracyjnej. CC0 nie znosi praw ani ograniczeń dotyczących samych znaków instytucjonalnych i nie jest zgodą na ich urzędowe użycie.

### 18.2. Źródła i identyfikacja

1. Sekretariat Stanu Stolicy Apostolskiej — oficjalny opis flagi, herbu Stolicy Apostolskiej, herbu Państwa Watykańskiego i pieczęci: https://press.vatican.va/roman_curia/secretariat_state/documents/insigne/sp_ss_scv_stemma-bandiera-sigillo_en.html
2. Governatorat Państwa Watykańskiego — opis flagi, historycznych barw oraz chorągwi Kościoła Rzymskiego: https://www.vaticanstate.va/en/state-and-government/general-informations/flag.html
3. Biuro Prasowe Stolicy Apostolskiej — oficjalny blazon i objaśnienie herbu papieża Leona XIV, 14.05.2025: https://press.vatican.va/content/salastampa/en/bollettino/pubblico/2025/05/14/250514b.html
4. Reprodukcja herbu Państwa Watykańskiego 2023 w Wikimedia Commons: https://commons.wikimedia.org/wiki/File:Coat_of_arms_of_Vatican_City_(2023%E2%80%93present).svg — opis wskazuje załącznik B ustawy zasadniczej oraz status domeny publicznej pliku wektorowego autora Goran tek-en.

### 18.3. Zakres użycia i blokady

- Wszystkie cztery ilustracje dodano do `DocumentBrandStore.builtInAssets` z właścicielem/dysponentem, źródłem HTTPS, opisem praw, referencją, weryfikatorem i wersją.
- Wartość `permittedKinds` pozostaje pusta dla wszystkich czterech nowych pozycji. Zasoby są widoczne w bibliotece i mogą służyć do podglądu/referencji, lecz polityka autoryzacji nie pozwala generatorowi PDF użyć ich jako urzędowego herbu, godła ani flagi.
- Oficjalna strona Stolicy Apostolskiej informuje, że jej emblemat został zgłoszony do WIPO w 1996 r. oraz wskazuje autorstwo części reprodukcji. Nie dodano oficjalnego pliku emblematycznego jako zasobu dopuszczonego do dokumentów.
- Ilustracja herbu papieża Leona XIV jest opracowaniem według blazonu, nie oficjalnym plikiem wzorcowym. Jej użycie w dokumentach wymaga odrębnej zgody / potwierdzenia warunków.
- Historyczna chorągiew jest rekonstrukcją ikonograficzną, a nie faksymile archiwalnym ani aktualną flagą państwową.
- W bibliotece pozostaje wcześniejsza flaga Państwa Watykańskiego; razem z nową chorągwią historyczną stanowi zestaw dwóch zasobów flagowych o odmiennym statusie i okresie historycznym.

### 18.4. Testy

Dodano `VaticanHeraldicAssetsTest.kt` w testach instrumentacyjnych:
- sprawdza indeksowanie czterech nowych zasobów;
- sprawdza dostępność bajtów i format SVG;
- sprawdza renderowanie podglądu SVG;
- sprawdza, że zasoby referencyjne nie są renderowane jako znaki instytucjonalne bez jawnego zakresu `permittedKinds`.

Wynik testów i kompilacji dla tego commitu pozostaje do potwierdzenia przez GitHub Actions. Nie oznaczać PR jako gotowego do scalenia, dopóki aktualny CI nie zakończy się powodzeniem.
