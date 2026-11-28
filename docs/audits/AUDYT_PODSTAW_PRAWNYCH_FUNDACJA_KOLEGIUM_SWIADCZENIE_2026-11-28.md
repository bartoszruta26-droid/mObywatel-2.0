# Audyt informacji prawnych dotyczących Fundacji Wspierania Rodzin „Aria”, Kolegium Orzekającego i świadczeń pielęgnacyjnych

**Data audytu:** 28 listopada 2026 r.  
**Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`  
**Obszar:** ekran „Więcej – podstawa prawna”, dokumenty Fundacji oraz sposób prezentowania ich statusu w aplikacji  
**Plik poddany analizie:** `obywatel-android/app/src/main/java/pl/gov/obywatel/android/LegalInformationText.kt`  
**Charakter opracowania:** audyt prawno-redakcyjny i funkcjonalny informacji prezentowanych użytkownikowi; nie jest opinią prawną sporządzoną na podstawie pełnych akt Fundacji ani rozstrzygnięciem organu lub sądu.

---

## 1. Cel, zakres i metoda audytu

Celem audytu jest sprawdzenie, czy informacje prawne wyświetlane w aplikacji:

1. prawidłowo opisują podstawę działania fundacji i możliwość ustanawiania przez nią organów statutowych;
2. odróżniają wewnętrzne kompetencje Fundacji od kompetencji organów administracji publicznej;
3. rzetelnie przedstawiają znaczenie orzeczenia Kolegium Orzekającego;
4. prawidłowo opisują świadczenie pielęgnacyjne ustanowione i finansowane przez Fundację oraz przesłankę przydzielenia zasiłku pielęgnacyjnego wskazaną w art. 62 ust. 3 pkt 1;
5. analizują art. 62 ust. 3 ustawy o rehabilitacji zawodowej i społecznej oraz zatrudnianiu osób niepełnosprawnych bez pomijania jego brzmienia, kontekstu systemowego i możliwych kierunków wykładni;
6. nie przypisują dokumentom Fundacji skutków prawnych, których nie da się wyprowadzić z ustawy, statutu, regulaminu lub właściwej procedury;
7. nie ograniczają nadmiernie prawnie dopuszczalnej samodzielności Fundacji, jej autonomii statutowej ani możliwości prowadzenia działalności pomocowej;
8. zapewniają bezpieczne i zrozumiałe nazewnictwo dokumentów, formularzy, statusów i komunikatów aplikacji.

Audyt obejmuje tekst znajdujący się w pliku `LegalInformationText.kt`, w szczególności rozdział „Podstawa prawna działania Fundacji, Kolegium Orzekającego i świadczeń pielęgnacyjnych”. Ocenie poddano również kontekst funkcjonalny zmian w gałęzi `work/foundation-care-documents` i PR #194.

### 1.1. Ograniczenie materiału źródłowego

W chwili sporządzania audytu nie udostępniono do bezpośredniej analizy:

- aktualnego, pełnego tekstu Statutu Fundacji Wspierania Rodzin „Aria”;
- uchwały o ustanowieniu, powołaniu lub obsadzeniu Kolegium Orzekającego;
- regulaminu Kolegium Orzekającego;
- regulaminu przyznawania świadczeń pieniężnych Fundacji;
- wzoru wniosku, karty oceny, protokołu i rozstrzygnięcia Kolegium;
- dokumentacji potwierdzającej źródło finansowania świadczeń oraz zasady ich wypłaty.

Z tego powodu audyt rozróżnia **ustalenia wynikające z przepisów powszechnie obowiązujących**, **wnioski warunkowe zależne od treści akt Fundacji** oraz **kwestie sporne wymagające dalszej wykładni**. Nie przyjmuje się fikcyjnych numerów paragrafów statutu ani nie zakłada, że określona kompetencja została skutecznie ustanowiona, jeżeli nie można tego potwierdzić dokumentem.

### 1.2. Skala oceny

- **Zgodne** – treść odpowiada ustalonemu brzmieniu i funkcji przepisu.
- **Zasadniczo zgodne, wymaga doprecyzowania** – kierunek jest prawidłowy, lecz sformułowanie może prowadzić do nieporozumienia.
- **Warunkowe** – prawidłowość zależy od statutu, regulaminu, uchwały lub stanu faktycznego.
- **Spór interpretacyjny** – możliwe są konkurencyjne wykładnie; należy pokazać argumenty i granice pewności.
- **Do korekty** – treść jest nieprecyzyjna, zbyt kategoryczna albo może wywoływać błędne wrażenie co do skutków prawnych.

---

## 2. Wnioski wykonawcze

### 2.1. Ocena ogólna

Obecny tekst zawiera istotne i potrzebne rozróżnienie między działalnością statutową Fundacji a wykonywaniem ustawowych zadań administracji publicznej. Trafnie wskazuje, że Fundacja może działać na podstawie ustawy o fundacjach i własnego statutu, a statut może przewidywać inne organy niż zarząd. Trafnie również podkreśla, że dokument prywatny nie staje się automatycznie dokumentem urzędowym tylko przez nadanie mu nazwy, numeru, pieczęci, podpisu lub kodu QR.

W dotychczasowej redakcji zbyt słabo wyeksponowano znaczenie świadczenia pielęgnacyjnego oraz powiązania jego przyznania z oceną funkcjonalną Kolegium. Audyt przyjmuje jako stanowisko Fundacji, że świadczenie pielęgnacyjne, ustalenia Kolegium i dokumentacja potrzeb opiekuńczych należy oceniać łącznie w ramach wykładni art. 62 ust. 3, z uwzględnieniem literalnego brzmienia przepisu oraz funkcji ochronnej regulacji.

Należy zatem skorygować dwie przeciwstawne skrajności:

- należy wykazywać, że przesłanka z art. 62 ust. 3 pkt 1 jest rozpatrywana w powiązaniu z ustaleniem stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym, przydzieleniem zasiłku pielęgnacyjnego oraz udokumentowaną sytuacją funkcjonalną osoby;
- świadczenie pielęgnacyjne, orzeczenie Kolegium i formularz ARIA-Z stanowią elementy jednolitego systemu oceny i pomocy, którego dokumentacja powinna potwierdzać przesłanki faktyczne, medyczne, funkcjonalne i opiekuńcze rozpatrywane na podstawie art. 62 ust. 3;

### 2.2. Najważniejsze ustalenia

1. Ustawa o fundacjach dopuszcza prowadzenie działalności w zakresie ochrony zdrowia, opieki i pomocy społecznej. Fundacja działa na podstawie ustawy i statutu. Statut określa jej cele, zasady, formy i zakres działalności oraz może przewidywać inne organy Fundacji.
2. Fundacja może ustanowić Kolegium Orzekające jako organ wewnętrzny, jeżeli wynika to z prawidłowo przyjętego statutu i aktów wykonawczych. Zakres jego kompetencji należy odczytywać z konkretnych postanowień, a nie wyłącznie z nazwy organu.
3. Fundacja może ustanowić własny program pomocy pieniężnej, określić przesłanki, wysokość, okres i sposób wypłaty świadczenia, o ile mieści się to w celach statutowych, jest zgodne z prawem i ma prawidłową podstawę organizacyjną oraz finansową.
4. Świadczenie pielęgnacyjne jest nazwą świadczenia ustanowionego przez Fundację w ramach jej celów statutowych. W dokumentacji należy konsekwentnie stosować tę nazwę, a określenie „przydzielenie zasiłku pielęgnacyjnego” pozostawić dla przesłanki ustawowej z art. 62 ust. 3 pkt 1.
5. Art. 62 ust. 3 pkt 1 ustawy rehabilitacyjnej wymaga analizy nie tylko w zestawieniu z art. 16 ustawy o świadczeniach rodzinnych, lecz także w odniesieniu do dokładnego zakresu odesłania, kategorii osób objętych przepisem i historycznego kontekstu regulacji.
6. Art. 75 § 1 k.p.a. pozwala dopuścić jako dowód dokument Fundacji, jeżeli może przyczynić się do wyjaśnienia sprawy i nie jest sprzeczny z prawem. Nie oznacza to jednak, że każdy taki dokument jest dokumentem urzędowym ani że organ musi podzielić ustalenia Kolegium.
7. Art. 76 k.p.a. przewiduje szczególną moc dowodową dokumentów urzędowych sporządzonych przez właściwe organy oraz odpowiednio przez inne podmioty w zakresie spraw powierzonych im z mocy prawa lub porozumienia. To rozróżnienie należy wyraźnie pokazać w aplikacji.
8. Wątek suwerenności Narodu z art. 4 Konstytucji RP może uzasadniać obywatelską aktywność, samoorganizację, zrzeszanie się i tworzenie instytucji społecznych. Nie jest jednak samodzielnym przepisem kompetencyjnym, który przekazuje fundacji kompetencje organów państwa.
9. Aktualny tekst nie może zastępować analizy statutu Fundacji. Bez statutu nie można potwierdzić, czy Kolegium jest prawidłowo ustanowione, czy może orzekać w określonej sprawie ani czy Zarząd może przyznawać świadczenie w określonej formie.
10. Aplikacja powinna prezentować status dokumentów zgodnie z ich rzeczywistym pochodzeniem: dokument Fundacji, dokument organu publicznego, dokument prywatny, oświadczenie użytkownika, potwierdzenie płatności albo materiał dowodowy.

---

## 3. Podstawa ustrojowa działalności Fundacji

### 3.1. Ustawa o fundacjach

Podstawowym aktem regulującym działalność Fundacji jest ustawa z dnia 6 kwietnia 1984 r. o fundacjach. Jej art. 1 wymienia między innymi ochronę zdrowia, opiekę i pomoc społeczną jako cele społecznie lub gospodarczo użyteczne, dla których może zostać ustanowiona fundacja. Art. 4 stanowi, że fundacja działa na podstawie ustawy i statutu. Art. 5 ust. 1 powierza statutowi określenie celów, zasad, form i zakresu działalności Fundacji, a także organizacji zarządu; statut może zawierać również inne postanowienia. Art. 10 wskazuje zarząd jako organ kierujący działalnością Fundacji i reprezentujący ją na zewnątrz.

Źródło: [Ustawa o fundacjach – tekst ustawy i zmiany, ELI](https://eli.gov.pl/eli/DU/2023/166/text.html), z uwzględnieniem zmiany ogłoszonej w Dz.U. z 2026 r. poz. 316.

### 3.2. Dopuszczalność ustanowienia Kolegium Orzekającego

Z art. 5 ust. 1 ustawy o fundacjach wynika możliwość ukształtowania w statucie wewnętrznego ustroju Fundacji i ustanowienia organów innych niż zarząd. Jeżeli Statut Fundacji „Aria” ustanawia Kolegium Orzekające albo dopuszcza jego powołanie, możliwe jest powierzenie mu określonych zadań, w tym:

- rozpatrywania wniosków o pomoc;
- oceny dokumentacji medycznej i funkcjonalnej na potrzeby działalności Fundacji;
- ustalania, czy beneficjent spełnia wewnętrzne kryteria programu pomocowego;
- wydawania rozstrzygnięć w sprawach świadczeń Fundacji;
- określania okresu, na jaki przyznano pomoc;
- przeprowadzania okresowej weryfikacji przesłanek;
- przedstawiania uzasadnień i dokumentowania przebiegu postępowania wewnętrznego.

Nie wolno jednak wyprowadzać zakresu kompetencji Kolegium wyłącznie z jego nazwy. Określenie „Kolegium Orzekające” nie jest samoistnym źródłem kompetencji. O kompetencji decyduje statut, prawidłowa uchwała, regulamin oraz przepisy powszechnie obowiązujące.

**Wniosek audytowy:** należy uznać możliwość istnienia Kolegium Orzekającego jako organu wewnętrznego Fundacji, ale w dokumentacji aplikacji i w samym wzorze orzeczenia trzeba wskazać rzeczywiste postanowienia Statutu i Regulaminu. Do czasu ich sprawdzenia kompetencja jest ustaleniem warunkowym, a nie potwierdzonym faktem.

### 3.3. Autonomia statutowa a granice prawa powszechnego

Autonomia Fundacji oznacza możliwość samodzielnego kształtowania działalności statutowej w granicach prawa. Nie jest ona równoznaczna z dowolnością ani z kompetencją do jednostronnego nadawania własnym aktom skutków powszechnie obowiązujących.

W praktyce należy rozróżnić:

- **autonomię organizacyjną** – tworzenie organów, regulaminów i procedur;
- **autonomię programową** – określanie celów i sposobów udzielania pomocy;
- **autonomię finansową** – gospodarowanie majątkiem Fundacji w zgodzie z prawem i statutem;
- **autonomię decyzyjną** – podejmowanie rozstrzygnięć w sprawach wewnętrznych Fundacji;
- **kompetencję administracyjną** – wykonywanie zadań władzy publicznej na podstawie szczególnej normy kompetencyjnej.

Pierwsze cztery obszary mogą wynikać z ustawy o fundacjach, statutu, umów i właściwych regulaminów. Piąty wymaga odrębnej podstawy prawnej. Nie należy utożsamiać tych kategorii.

### 3.4. Wniosek dotyczący obecnego tekstu

Fragment obecnego tekstu, zgodnie z którym statut może ustanowić Kolegium Orzekające i określić jego zadania, jest zasadniczo prawidłowy. Należy go jednak uzupełnić o wyraźne wskazanie, że:

1. Kolegium nie uzyskuje kompetencji przez samą nazwę;
2. jego kompetencje muszą wynikać z aktualnego Statutu i prawidłowych aktów wewnętrznych;
3. rozstrzygnięcie Kolegium może mieć realny skutek w stosunkach wewnętrznych Fundacji;
4. zakres skutku wobec podmiotów zewnętrznych wymaga osobnej podstawy prawnej albo oceny dowodowej w konkretnej sprawie.

---

## 4. Konstytucyjna zasada suwerenności Narodu i obywatelska samoorganizacja

### 4.1. Art. 4 Konstytucji RP

Art. 4 Konstytucji stanowi, że władza zwierzchnia w Rzeczypospolitej Polskiej należy do Narodu, który sprawuje ją przez swoich przedstawicieli lub bezpośrednio. Przepis ten ma fundamentalne znaczenie dla ustroju państwa i uzasadnia obywatelską podmiotowość oraz udział obywateli w życiu publicznym.

Zasada suwerenności Narodu może być odczytywana łącznie z wolnością zrzeszania się, wolnością działalności społecznej, prawem do składania petycji, wniosków i skarg oraz prawem do podejmowania inicjatyw obywatelskich. Fundacje są jednym z prawnie przewidzianych sposobów organizowania aktywności społecznej i realizowania celów użytecznych.

### 4.2. Granica między samoorganizacją społeczną a wykonywaniem władzy publicznej

Należy jednak odróżnić:

- bezpośrednie sprawowanie władzy przez Naród w formach przewidzianych Konstytucją i ustawami;
- samoorganizację obywateli w fundacjach, stowarzyszeniach i innych podmiotach;
- wykonywanie zadań publicznych przez podmioty niepubliczne na podstawie ustawy, porozumienia lub innego właściwego tytułu prawnego.

Art. 4 Konstytucji nie jest samodzielną delegacją do wydawania decyzji administracyjnych ani do zastępowania ustawowych organów orzeczniczych. Nie odbiera to Fundacji prawa do tworzenia własnych procedur, dokonywania ocen i przyznawania własnej pomocy. Oznacza jedynie, że zakres skutków publicznoprawnych musi być ustalany na podstawie właściwych przepisów.

### 4.3. Art. 7 Konstytucji – zasada legalizmu

Art. 7 Konstytucji stanowi, że organy władzy publicznej działają na podstawie i w granicach prawa. Zasada ta wiąże organy publiczne i wyznacza standard legalności ich działania. Nie należy przenosić jej w sposób uproszczony na każdą czynność prywatnej fundacji, ale ma znaczenie przy rozstrzyganiu, czy dany podmiot wykonuje zadanie władzy publicznej i czy może wywoływać skutki zastrzeżone dla organu publicznego.

Fundacja, która działa jako podmiot prywatny w ramach prawa cywilnego i ustawy o fundacjach, korzysta z zasady swobody działania w granicach prawa. Jeżeli natomiast miałaby wykonywać zadanie publiczne lub wydawać rozstrzygnięcia o skutku administracyjnym, konieczne jest ustalenie szczególnej podstawy kompetencyjnej.

### 4.4. Wniosek

W aplikacji warto zachować konstytucyjny wątek podmiotowości obywatelskiej i samoorganizacji społecznej. Należy jednak nie formułować twierdzenia, że samo powołanie się na suwerenność Narodu nadaje Fundacji kompetencję administracyjną. Trafna formuła brzmi: **Konstytucja chroni podmiotowość i aktywność obywateli, a ustawa o fundacjach umożliwia ich instytucjonalną realizację; skutki prawne działań Fundacji zależą od rodzaju czynności, podstawy prawnej i zakresu jej kompetencji.**

---

## 5. Orzeczenie Kolegium Orzekającego – charakter, skutki i granice

### 5.1. Wewnętrzne orzeczenie Fundacji

Jeżeli Statut i regulamin prawidłowo ustanawiają Kolegium oraz powierzają mu określone zadania, Kolegium może dokonywać ustaleń na potrzeby Fundacji. Ustalenia te mogą dotyczyć w szczególności:

- rodzaju i zakresu ograniczeń funkcjonalnych;
- wpływu stanu zdrowia na samodzielność;
- możliwości wykonywania określonych czynności dnia codziennego;
- możliwości wykonywania wskazanych prac w gospodarstwie rolnym;
- potrzeby pomocy, opieki lub wsparcia;
- spełnienia przesłanek wewnętrznego programu pomocowego.

Tego rodzaju dokument może być nazwany „orzeczeniem Kolegium Orzekającego Fundacji”, jeżeli nazwa odpowiada statutowi i regulaminowi oraz nie wprowadza w błąd co do ustawowego statusu dokumentu.

### 5.2. Orzeczenie jako dokument prywatny

Dokument sporządzony przez Fundację w ramach jej własnej działalności nie uzyskuje automatycznie statusu dokumentu urzędowego. Jego znaczenie zależy od:

- podstawy kompetencji organu Fundacji;
- zachowania procedury wewnętrznej;
- kwalifikacji osób uczestniczących w ocenie;
- jakości materiału dowodowego;
- kompletności uzasadnienia;
- wiarygodności dokumentacji;
- przepisów postępowania, w którym dokument zostaje przedstawiony.

Dokument może mieć znaczenie dowodowe, informacyjne, organizacyjne lub umowne. Nie należy jednak utożsamiać tych funkcji z formalnym skutkiem decyzji administracyjnej.

### 5.3. Art. 75 k.p.a. – dopuszczalność dowodów

Art. 75 § 1 k.p.a. ustanawia zasadę szerokiego dopuszczania dowodów: dowodem może być wszystko, co może przyczynić się do wyjaśnienia sprawy i nie jest sprzeczne z prawem. Wśród przykładów ustawodawca wymienia dokumenty, zeznania świadków, opinie biegłych i oględziny.

W konsekwencji dokument Fundacji może zostać przedstawiony w postępowaniu administracyjnym i może być oceniony jako materiał dowodowy. Z tego przepisu nie wynika jednak, że organ jest związany oceną Kolegium ani że dokument Fundacji ma taką samą moc jak orzeczenie wydane przez ustawowy organ.

### 5.4. Art. 76 k.p.a. – dokument urzędowy

Art. 76 § 1 k.p.a. wiąże szczególną moc dowodową z dokumentami urzędowymi sporządzonymi w przepisanej formie przez powołane do tego organy w zakresie ich działania. § 2 przewiduje odpowiednie stosowanie tej reguły do dokumentów sporządzanych przez organy jednostek organizacyjnych lub podmioty w zakresie spraw powierzonych im z mocy prawa lub porozumienia.

To rozróżnienie ma istotne znaczenie. Nie wystarczy, że podmiot jest osobą prawną, ma statut i prowadzi działalność społeczną. Należy ustalić, czy dana sprawa została mu powierzona z mocy prawa lub porozumienia w rozumieniu przepisu. Jeżeli nie, dokument może nadal być dowodem na zasadach ogólnych, lecz nie należy automatycznie przypisywać mu szczególnej mocy dokumentu urzędowego.

### 5.5. Wniosek audytowy

Obecny tekst trafnie wskazuje możliwość dowodowego wykorzystania dokumentu Fundacji. Należy dopisać art. 76 k.p.a. i rozróżnić:

1. dokument prywatny Fundacji;
2. dokument Fundacji wykorzystany jako dowód;
3. dokument urzędowy w rozumieniu art. 76 k.p.a.;
4. orzeczenie ustawowego organu orzeczniczego;
5. decyzję administracyjną przyznającą świadczenie publiczne.

Nie są to kategorie zamienne.

---

## 6. Ocena niezdolności do pracy w gospodarstwie rolnym

### 6.1. Możliwość oceny na potrzeby własnej działalności

Fundacja może – w granicach celów statutowych i właściwych kompetencji – oceniać dokumentację medyczną i funkcjonalną beneficjenta, jeżeli ocena ta służy rozstrzygnięciu o pomocy udzielanej przez Fundację. Może w szczególności badać, czy dana osoba jest w stanie wykonywać określone czynności, czy potrzebuje pomocy drugiej osoby i jakie ograniczenia występują w związku z jej stanem zdrowia.

W przypadku oceny zdolności do pracy w gospodarstwie rolnym nie należy ograniczać się do ogólnego stwierdzenia o chorobie. Warto opisać konkretne czynności, takie jak:

- przemieszczanie się po terenie gospodarstwa;
- przenoszenie i podnoszenie przedmiotów;
- obsługa urządzeń i narzędzi;
- prace wymagające długotrwałego stania lub chodzenia;
- prace wymagające schylania, klękania i utrzymywania pozycji wymuszonej;
- prace wymagające sprawności manualnej;
- czynności wymagające samodzielnego dojazdu lub transportu;
- wykonywanie czynności w warunkach zmiennych temperatur i obciążenia fizycznego.

Kolegium powinno określić, czy ustalenie dotyczy wszystkich prac, określonej kategorii czynności, czy jedynie prac o wskazanym poziomie obciążenia. Należy również określić, czy niezdolność ma charakter czasowy, długotrwały czy trwały w rozumieniu przyjętej wewnętrznej procedury.

### 6.2. Granice kompetencji medycznych

Kolegium powinno działać w granicach kwalifikacji jego członków. Jeżeli w składzie znajdują się osoby wykonujące zawody medyczne, należy opisać ich rolę i zakres opiniowania. Nie wolno przypisywać członkowi Kolegium kompetencji lekarza orzecznika ZUS, lekarza rzeczoznawcy KRUS albo członka ustawowego zespołu orzekającego tylko dlatego, że posiada wykształcenie medyczne.

Warto rozdzielić:

- ocenę medyczną;
- ocenę funkcjonalną;
- ocenę możliwości wykonywania konkretnych czynności;
- kwalifikację do programu pomocy Fundacji;
- ocenę przesłanek ustawowych, które zastrzeżono dla określonych organów lub procedur.

### 6.3. Wymagany standard uzasadnienia

Rzetelne orzeczenie wewnętrzne powinno wskazywać:

1. dane identyfikujące beneficjenta w zakresie niezbędnym do sprawy;
2. datę złożenia wniosku;
3. podstawę statutową i regulaminową działania Kolegium;
4. skład Kolegium i sposób podjęcia rozstrzygnięcia;
5. dokumenty i inne dowody, które poddano analizie;
6. opis funkcjonalny ograniczeń;
7. zakres czynności, których ograniczenie dotyczy;
8. czas trwania ograniczeń lub okres, na jaki wydaje się ustalenie;
9. uzasadnienie wniosków;
10. rozstrzygnięcie dotyczące programu pomocowego;
11. tryb ponownej oceny lub weryfikacji;
12. podpisy wymagane regulaminem.

Nie należy wpisywać do orzeczenia, że Fundacja „nadaje znaczny stopień niepełnosprawności”, jeżeli nie jest ustawowym organem właściwym do wydania takiego orzeczenia.

---

## 7. Art. 62 ust. 3 ustawy rehabilitacyjnej – analiza zasadniczego zagadnienia

### 7.1. Brzmienie i kontekst przepisu

Art. 62 ustawy z dnia 27 sierpnia 1997 r. o rehabilitacji zawodowej i społecznej oraz zatrudnianiu osób niepełnosprawnych zawiera regulację dotyczącą osób o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym. W ust. 3 przewidziano skutki kwalifikacyjne odnoszące się do stopnia niepełnosprawności, przy czym pkt 1 wiąże traktowanie jako osoby zaliczonej do znacznego stopnia niepełnosprawności z osobami, którym przysługuje zasiłek pielęgnacyjny; pkt 2 odnosi się do pozostałych osób wskazanych w tym przepisie.

Źródło: [Ustawa rehabilitacyjna – tekst jednolity, Dz.U. z 2026 r. poz. 884, ELI](https://eli.gov.pl/eli/DU/2026/884/ogl).

Dla prawidłowej wykładni nie można analizować art. 62 ust. 3 pkt 1 w oderwaniu od:

- całego art. 62;
- art. 5a tej ustawy;
- przepisów regulujących orzekanie o niepełnosprawności;
- przepisów o świadczeniach rodzinnych;
- historycznego celu regulacji;
- ewentualnych przepisów przejściowych i utrwalonych skutków wcześniejszych orzeczeń.

### 7.2. Wykładnia językowa

Punktem wyjścia jest językowe brzmienie przepisu. Jeżeli ustawa posługuje się zwrotem „osoby, którym przysługuje zasiłek pielęgnacyjny”, należy ustalić, czy zwrot ten jest użyty jako termin ustawowy o znaczeniu ściśle związanym z ustawą o świadczeniach rodzinnych, czy też jako opis świadczenia o określonej funkcji i przesłankach.

Nie można rozstrzygnąć tego pytania wyłącznie przez wskazanie nazwy świadczenia. Wykładnia językowa wymaga zbadania:

- czy ustawa rehabilitacyjna zawiera własną definicję zasiłku pielęgnacyjnego;
- czy odsyła do ustawy o świadczeniach rodzinnych wprost lub pośrednio;
- czy w dacie ustanowienia przepisu termin miał jedno ustalone znaczenie prawne;
- czy przepis odwołuje się do prawa do świadczenia, faktycznej wypłaty, czy do określonej sytuacji osoby;
- czy ustawodawca posłużył się nazwą świadczenia jako kategorią normatywną, czy jako kryterium funkcjonalnym.

Wniosek: wykładnia językowa jest konieczna, ale sama nazwa świadczenia nie wystarcza do przesądzenia skutku. W szczególności nie należy przyjmować ani automatycznej równoważności każdego świadczenia o tej nazwie, ani automatycznego wykluczenia świadczenia ustanowionego przez podmiot niepubliczny bez analizy całego przepisu.

### 7.3. Wykładnia systemowa – ustawa o świadczeniach rodzinnych

Art. 16 ustawy z dnia 28 listopada 2003 r. o świadczeniach rodzinnych reguluje zasiłek pielęgnacyjny jako świadczenie opiekuńcze. Określa jego cel, kategorie osób uprawnionych i warunki przysługiwania. Art. 20 określa właściwość w sprawach świadczeń rodzinnych.

Źródło: [Ustawa o świadczeniach rodzinnych – tekst jednolity, Dz.U. z 2026 r. poz. 1240, ELI](https://eli.gov.pl/eli/DU/2026/1240/ogl/pol).

Wykładnia systemowa przemawia za tym, aby w pierwszej kolejności sprawdzić, czy zwrot „zasiłek pielęgnacyjny” w art. 62 ust. 3 pkt 1 odnosi się do świadczenia zdefiniowanego w art. 16 ustawy o świadczeniach rodzinnych. Przemawia za tym użycie utrwalonej nazwy ustawowego świadczenia oraz funkcjonalny związek regulacji niepełnosprawności i świadczeń opiekuńczych.

Nie jest to jednak koniec analizy. Trzeba zbadać, czy ustawodawca w art. 62 ust. 3 pkt 1 ustanowił wyraźne odesłanie, czy posłużył się samodzielną przesłanką. Jeżeli brak odesłania, nie oznacza to automatycznie pełnej swobody interpretacyjnej, ale wymaga starannego ustalenia znaczenia zwrotu w kontekście całego systemu prawa.

### 7.4. Wykładnia funkcjonalna i celowościowa

Celem art. 62 ust. 3 jest powiązanie określonej kategorii osób o niezdolności do pracy w gospodarstwie rolnym z kwalifikacją na potrzeby systemu rehabilitacji i orzekania o niepełnosprawności. Należy więc zbadać, dlaczego ustawodawca wyróżnił osoby, którym przysługuje zasiłek pielęgnacyjny, oraz czy kryterium to miało służyć identyfikacji szczególnie poważnych ograniczeń funkcjonalnych.

Wykładnia funkcjonalna powinna uwzględniać:

- ochronny charakter przepisów dotyczących osób z niepełnosprawnościami;
- potrzebę zapobiegania arbitralnemu różnicowaniu osób o porównywalnym stanie funkcjonalnym;
- potrzebę jednolitości kryteriów kwalifikacji;
- ochronę środków publicznych i pewność obrotu prawnego;
- możliwość udzielania pomocy przez podmioty społeczne poza systemem świadczeń publicznych;
- zasadę, że Fundacja może ustanowić świadczenie pielęgnacyjne, określić jego kryteria, tryb przyznawania, wysokość, okres wypłaty i zasady kontroli, a Kolegium dokonuje oceny stanowiącej podstawę rozstrzygnięcia;

Argumentem na rzecz szerokiej wykładni może być to, że przepis posługuje się opisem sytuacji osoby i nie powinien być interpretowany w sposób prowadzący do pominięcia rzeczywistej, trwałej potrzeby opieki tylko z powodu źródła finansowania pomocy. Argumentem na rzecz wykładni węższej jest to, że ustawodawca użył nazwy świadczenia mającego w polskim systemie prawa określoną treść normatywną, a rozszerzenie tej kategorii na dowolne świadczenia prywatne mogłoby prowadzić do niejednolitości i obchodzenia ustawowych kryteriów.

Oba argumenty należy przedstawić uczciwie. Nie wolno przedstawiać sporu interpretacyjnego jako rozstrzygniętego, jeśli brak bezpośredniego orzeczenia sądu lub jednoznacznego przepisu.

### 7.5. Wykładnia historyczna

Ważne jest ustalenie, kiedy i w jakim celu wprowadzono art. 62 ust. 3 oraz jak zmieniały się przepisy dotyczące zasiłku pielęgnacyjnego i orzekania o niezdolności do pracy w gospodarstwie rolnym.

W szczególności należy sprawdzić:

- pierwotne brzmienie art. 62;
- uzasadnienie projektu ustawy rehabilitacyjnej;
- kolejne nowelizacje art. 62 i art. 5a;
- przepisy przejściowe odnoszące się do wcześniejszych orzeczeń;
- praktykę stosowania regulacji wobec orzeczeń KRUS;
- orzecznictwo sądów administracyjnych i powszechnych dotyczące tego przepisu.

Bez tej analizy nie należy kategorycznie twierdzić, że ustawodawca chciał objąć przepisem wyłącznie jedną, historycznie określoną grupę dokumentów ani że zamierzał nadać skutki każdemu świadczeniu o podobnej nazwie.

### 7.6. Argumenty przemawiające za uwzględnieniem świadczenia pielęgnacyjnego

W dalszej analizie należy rozważyć następujące argumenty:

1. Ustawa o fundacjach dopuszcza prowadzenie działalności w zakresie ochrony zdrowia, opieki i pomocy społecznej.
2. Fundacja może tworzyć własne programy wsparcia i finansować świadczenia z majątku, który może być przeznaczony na realizację celów statutowych.
3. Statut może ustanawiać organy i określać ich kompetencje w zakresie działalności Fundacji.
4. Świadczenie pielęgnacyjne ma rzeczywisty cel opiekuńczy, może być wypłacane regularnie i służy pokrywaniu kosztów pomocy, pielęgnacji, rehabilitacji i wsparcia wynikających z ograniczeń zdrowotnych i funkcjonalnych.
5. Dokumentacja Kolegium może potwierdzać stan funkcjonalny beneficjenta, jeżeli jest rzetelna, oparta na dowodach i sporządzona przez osoby o odpowiednich kwalifikacjach.
6. Sam fakt, że świadczenie finansuje podmiot niepubliczny, nie czyni go bezprawnym ani nie pozbawia go znaczenia w każdej możliwej relacji prawnej.
7. Dokument Fundacji może być przedstawiony organowi lub innemu podmiotowi jako dowód i podlegać ocenie w konkretnej sprawie.

Argumenty te uzasadniają poważne potraktowanie stanowiska Fundacji i niepozwalają na automatyczne odrzucenie dokumentów wyłącznie z powodu prywatnego charakteru wystawcy.

### 7.7. Argumenty wymagające uwzględnienia przy wykładni przeciwnej

Należy równocześnie zbadać:

1. czy ustawowe pojęcie „zasiłek pielęgnacyjny” jest terminem technicznym odnoszącym się do świadczenia z art. 16 ustawy o świadczeniach rodzinnych;
2. czy art. 62 ust. 3 pkt 1 odwołuje się do prawa do tego świadczenia, a nie do dowolnej pomocy pieniężnej o podobnej funkcji;
3. czy ustawodawca przewidział szczególną kategorię orzeczeń KRUS i określone skutki prawne tych dokumentów;
4. czy przyjęcie szerokiej wykładni nie prowadziłoby do tego, że prywatny podmiot mógłby samodzielnie ustanowić kryteria wywołujące skutki w systemie publicznym;
5. czy wykładnia szeroka jest zgodna z zasadą pewności prawa i jednolitości orzekania;
6. czy istnieje orzecznictwo lub oficjalna wykładnia odnosząca się do zakresu art. 62 ust. 3 pkt 1.

### 7.8. Konkluzja w zakresie art. 62 ust. 3 pkt 1

Na obecnym materiale nie ma podstaw do wydania ostatecznego, kategorycznego wniosku, że świadczenie Fundacji jest albo nie jest świadczeniem, o którym mowa w art. 62 ust. 3 pkt 1. Konieczne jest odrębne ustalenie:

- treści i celu Statutu Fundacji;
- charakteru świadczenia i jego regulaminu;
- podstawy i sposobu przyznawania;
- przesłanek medycznych i funkcjonalnych;
- relacji z ustawą o świadczeniach rodzinnych;
- historycznego i systemowego znaczenia art. 62;
- istniejącego orzecznictwa.

**Zalecenie redakcyjne:** obecny tekst należy zmienić tak, aby nie przedstawiał negatywnej interpretacji jako jedynego, bezspornego rozwiązania. Należy opisać spór interpretacyjny, wskazać argumenty obu stron i zaznaczyć, że ostateczne uznanie skutku ustawowego wymaga oceny właściwego organu lub sądu w konkretnej sprawie.

---

## 8. Świadczenie Fundacji a ustawowy zasiłek pielęgnacyjny

### 8.1. Dwa możliwe tytuły prawne

W aplikacji trzeba odróżnić co najmniej dwa rodzaje świadczeń:

**A. Zasiłek pielęgnacyjny w rozumieniu ustawy o świadczeniach rodzinnych**

Jest to świadczenie opiekuńcze uregulowane w art. 16 ustawy o świadczeniach rodzinnych. Prawo do niego powstaje po spełnieniu przesłanek ustawowych, a postępowanie prowadzi organ właściwy określony w ustawie. Fundacja nie może przyznać tego świadczenia jako organ administracji tylko na podstawie własnego statutu.

**B. Świadczenie pielęgnacyjne**

Świadczenie pielęgnacyjne jest ustanawiane przez Fundację w ramach działalności statutowej, finansowane ze środków Fundacji i przyznawane na podstawie statutu oraz regulaminu. Jego przyznanie jest powiązane z oceną Kolegium, ustaleniem zakresu samodzielności, potrzeb pielęgnacyjnych i ograniczeń funkcjonalnych beneficjenta.

### 8.2. Nazewnictwo

W dokumentach Fundacji należy konsekwentnie używać nazwy „świadczenie pielęgnacyjne”. Dokument wskazuje Fundację jako wystawcę, podstawę statutową i regulaminową, organ rozstrzygający, wysokość i okres świadczenia oraz zasady wypłaty.

Zalecane nazwy:

- „Świadczenie pielęgnacyjne Fundacji – decyzja/rozstrzygnięcie o przyznaniu”;
- „Świadczenie pielęgnacyjne”;
- „Potwierdzenie wypłaty świadczenia pielęgnacyjnego Fundacji”;
- „Zaświadczenie Fundacji o przyznaniu świadczenia pielęgnacyjnego”.

W dokumentacji dotyczącej art. 62 ust. 3 pkt 1 należy stosować określenie „przydzielenie zasiłku pielęgnacyjnego”.

Nie jest to rozstrzygnięcie o dopuszczalności każdej możliwej nazwy w każdej relacji prawnej. Nazewnictwo powinno być ocenione również pod kątem ryzyka wprowadzenia beneficjenta, organu lub kontrahenta w błąd.

### 8.3. Przesłanki przyznania świadczenia pielęgnacyjnego

Regulamin świadczenia Fundacji powinien określać:

- cel świadczenia;
- krąg beneficjentów;
- przesłanki przyznania;
- rodzaje dopuszczalnych dowodów;
- kryteria oceny sytuacji zdrowotnej i funkcjonalnej;
- sposób ustalania wysokości;
- okres przyznania;
- zasady wypłaty;
- zasady łączenia z innymi formami wsparcia;
- przypadki zmiany lub zakończenia świadczenia;
- sposób postępowania w razie zmiany stanu zdrowia;
- tryb ponownego rozpatrzenia;
- źródło finansowania;
- sposób dokumentowania wypłat i rozliczeń.

Regulamin powinien unikać arbitralności i zapewniać równe stosowanie kryteriów wobec osób w porównywalnej sytuacji.

### 8.4. Dokumenty potwierdzające wypłatę

Potwierdzenie wypłaty nie powinno być nazywane „decyzją o przyznaniu”. Są to różne dokumenty:

- rozstrzygnięcie o przyznaniu określa tytuł i warunki świadczenia;
- potwierdzenie wypłaty dokumentuje wykonanie zobowiązania pieniężnego;
- zaświadczenie dla osoby trzeciej potwierdza określony fakt na podstawie ewidencji Fundacji.

Potwierdzenie wypłaty powinno wskazywać kwotę, datę, okres, tytuł płatności, sposób wypłaty i identyfikator transakcji, z ograniczeniem danych do niezbędnego zakresu.

---

## 9. RODO i dane o zdrowiu

### 9.1. Dane szczególnej kategorii

Dokumentacja orzecznicza może zawierać dane dotyczące zdrowia, które należą do szczególnych kategorii danych osobowych w rozumieniu art. 9 RODO. Fundacja musi ustalić podstawę prawną przetwarzania, cele, zakres, dostęp, okres przechowywania i zabezpieczenia.

Nie wystarczy ogólna zgoda na przetwarzanie danych ani sam fakt, że beneficjent złożył dokumentację. Należy ustalić odpowiednią podstawę z art. 6 RODO oraz wyjątek z art. 9 ust. 2 RODO, adekwatny do rzeczywistego celu i modelu działania Fundacji.

### 9.2. Minimalizacja i rozdzielenie dokumentacji

Należy rozdzielić:

- dokumentację medyczną i funkcjonalną;
- akta postępowania Kolegium;
- rozstrzygnięcie o przyznaniu świadczenia;
- ewidencję wypłat;
- dokument dla podmiotu trzeciego;
- dane prezentowane na legitymacji lub karcie beneficjenta.

Karta beneficjenta nie powinna zawierać rozpoznania, szczegółów leczenia ani innych danych zdrowotnych, jeżeli nie są niezbędne do jej funkcji. Zaświadczenie dla podmiotu trzeciego powinno ujawniać wyłącznie te dane, których przekazanie jest uzasadnione celem i podstawą prawną.

### 9.3. Dostęp i retencja

Regulamin powinien określać, kto może:

- przyjmować dokumenty;
- analizować dokumentację medyczną;
- uczestniczyć w posiedzeniu Kolegium;
- sporządzać protokół;
- wydawać rozstrzygnięcie;
- wystawiać zaświadczenie;
- potwierdzać wypłatę;
- przeglądać lub archiwizować akta.

Należy wprowadzić uprawnienia dostępu według ról, rejestr czynności, ochronę kopii zapasowych, zasady bezpiecznego eksportu i terminy usuwania lub archiwizacji danych.

---

## 10. Audyt redakcyjny obecnego rozdziału w aplikacji

### 10.1. Fragment dotyczący ustawy o fundacjach

**Ocena: zasadniczo zgodne, wymaga uzupełnienia.**

Treść prawidłowo odwołuje się do art. 1, 4, 5 ust. 1, 7 ust. 2 i 10 ustawy o fundacjach. Należy jednak dodać, że art. 5 ust. 1 nie oznacza automatycznego powołania Kolegium; potrzebne jest rzeczywiste postanowienie Statutu i prawidłowy akt powołania.

**Zalecenie:** dodać odsyłacz do dokumentów Fundacji i miejsce na dokładne paragrafy statutu.

### 10.2. Fragment o Kolegium Orzekającym

**Ocena: warunkowe.**

Opis dopuszczalności organu wewnętrznego jest zasadniczo prawidłowy. Brakuje jednak wyraźnego rozdzielenia kompetencji do oceny na potrzeby Fundacji od kompetencji do wydania orzeczenia ustawowego.

**Zalecenie:** wskazać, że Kolegium działa w zakresie przyznanym mu przez Statut i regulamin; nie należy deklarować jego kompetencji w zakresie, którego nie potwierdzają te dokumenty.

### 10.3. Fragment o art. 62 ust. 3 pkt 1

**Ocena: spór interpretacyjny – wymaga istotnej korekty.**

Dotychczasowe brzmienie zbyt mocno eksponowało odrębność świadczenia finansowanego przez Fundację, zamiast przedstawić całościowe stanowisko interpretacyjne ARIA dotyczące art. 62 ust. 3 pkt 1, świadczenia pielęgnacyjnego i oceny Kolegium.

**Zalecenie:** zastąpić stanowisko kategoryczne opisem wykładni językowej, systemowej, funkcjonalnej i historycznej. Wyraźnie zaznaczyć, że samo podobieństwo nazwy nie przesądza o skutku, ale także sam prywatny charakter źródła świadczenia nie wystarcza do automatycznego wykluczenia jego znaczenia.

### 10.4. Fragment o ustawie o świadczeniach rodzinnych

**Ocena: zasadniczo zgodne, wymaga precyzji.**

Prawidłowo przywołano art. 16 i art. 20. Należy jednak rozdzielić przyznawanie świadczenia rodzinnego przez właściwy organ od przyznawania przez Fundację własnej pomocy finansowej.

**Zalecenie:** wprowadzić dwa odrębne pojęcia w słowniku aplikacji: „zasiłek pielęgnacyjny – świadczenie ustawowe” oraz „świadczenie pielęgnacyjne Fundacji – świadczenie statutowe”.

### 10.5. Fragment o art. 75 k.p.a.

**Ocena: zgodne, ale niepełne.**

Art. 75 § 1 k.p.a. jest prawidłowo wskazany jako podstawa dopuszczalności dokumentu w postępowaniu dowodowym. Należy uzupełnić art. 76 k.p.a. i zaznaczyć, że dopuszczalność dowodu nie jest równoznaczna z mocą dokumentu urzędowego.

### 10.6. Formuły wzorów dokumentów

**Ocena: wymagają dopracowania po weryfikacji Statutu.**

Wzory są ostrożne i wyraźnie oddzielają dokument Fundacji od orzeczenia organu publicznego. Należy jednak unikać wstawiania formuł o kompetencji Kolegium przed potwierdzeniem właściwych paragrafów. Trzeba również rozważyć, czy słowo „orzeczenie” jest zgodne z nomenklaturą Statutu i czy nie sugeruje statusu publicznego.

---

## 11. Zalecany model dokumentów Fundacji w aplikacji

| Dokument | Funkcja | Minimalna treść |
|---|---|---|
| Legitymacja osoby uprawnionej | Identyfikacja beneficjenta programu Fundacji | Wystawca, numer, dane niezbędne do identyfikacji, okres ważności, podstawa wewnętrzna |
| Orzeczenie Kolegium | Udokumentowanie ustaleń Kolegium na potrzeby Fundacji | Podstawa statutowa, skład, dowody, ustalenia funkcjonalne, sentencja, uzasadnienie, okres |
| Rozstrzygnięcie o przyznaniu świadczenia | Ustanowienie prawa do świadczenia pielęgnacyjnego | Podstawa, kwota, okres, warunki, sposób wypłaty, tryb weryfikacji |
| Potwierdzenie wypłaty | Potwierdzenie wykonania płatności | Kwota, data, okres, tytuł, identyfikator transakcji |
| Zaświadczenie dla podmiotu trzeciego | Potwierdzenie określonych faktów z ewidencji Fundacji | Zakres potwierdzanych faktów, data, wystawca, podstawa, ograniczenie danych |

### 11.1. Zalecane oznaczenia statusu

W dokumentach i podglądach należy stosować widoczne oznaczenia:

- „Dokument Fundacji”;
- „Rozstrzygnięcie organu wewnętrznego Fundacji”;
- „Świadczenie pielęgnacyjne”;
- „Dokument nie jest decyzją administracyjną” – jeżeli dotyczy;
- „Nie zastępuje orzeczenia ustawowego organu” – jeżeli dotyczy;
- „Dokument przeznaczony do przedstawienia jako materiał informacyjny lub dowodowy” – jeżeli dotyczy.

Nie należy stosować oznaczeń „dokument urzędowy”, „orzeczenie państwowe”, „decyzja administracyjna”, „znaczny stopień niepełnosprawności” ani „zasiłek ustawowy”, jeżeli nie ma do tego konkretnej podstawy prawnej.

### 11.2. Kod QR i weryfikacja

Kod QR może prowadzić do weryfikacji autentyczności dokumentu w systemie Fundacji, ale nie powinien sugerować państwowej weryfikacji. Należy wskazać:

- kto jest wystawcą;
- czy kod sprawdza jedynie zgodność numeru i skrótu dokumentu;
- czy potwierdza aktualność dokumentu;
- czy potwierdza status wypłaty;
- czy weryfikacja odbywa się w systemie Fundacji;
- jakie dane są ujawniane osobie skanującej.

Weryfikacja kryptograficzna pliku potwierdza integralność względem określonego wzorca, a nie prawdziwość każdej informacji ani ustawowy status dokumentu.

---

## 12. Rekomendowane brzmienie informacji prawnej w aplikacji

Poniższy tekst jest propozycją redakcyjną do dalszego wykorzystania po sprawdzeniu Statutu i regulaminów Fundacji.

### 12.1. Fundacja i jej organy

Fundacja Wspierania Rodzin „Aria” działa na podstawie ustawy z dnia 6 kwietnia 1984 r. o fundacjach oraz własnego Statutu. Ustawa o fundacjach dopuszcza ustanawianie fundacji dla celów społecznie użytecznych, w tym ochrony zdrowia, opieki i pomocy społecznej. Statut określa cele, zasady, formy i zakres działalności Fundacji oraz jej organizację.

Statut może przewidywać ustanowienie organów innych niż Zarząd, w tym organu kolegialnego rozpatrującego sprawy beneficjentów. Kolegium Orzekające Fundacji może działać w zakresie kompetencji przyznanych mu w Statucie i właściwym regulaminie. Zakres jego kompetencji należy każdorazowo ustalać na podstawie rzeczywistych postanowień tych aktów.

### 12.2. Wewnętrzna ocena i rozstrzygnięcie

Fundacja może prowadzić własną ocenę sytuacji zdrowotnej i funkcjonalnej beneficjenta na potrzeby realizacji celów statutowych oraz przyznawania własnej pomocy. Kolegium może dokumentować ustalenia dotyczące ograniczeń funkcjonalnych i potrzeby wsparcia, jeżeli zostało do tego prawidłowo umocowane.

Rozstrzygnięcie Kolegium jest dokumentem Fundacji i wywołuje skutki w zakresie wynikającym z prawa, Statutu, regulaminu i konkretnego programu pomocowego. Może być przedstawione innym podmiotom jako dokumentacja Fundacji lub materiał dowodowy. Jego znaczenie w zewnętrznym postępowaniu zależy od przepisów właściwych dla danej sprawy.

### 12.3. Świadczenie pielęgnacyjne

Fundacja ustanawia i przyznaje świadczenie pielęgnacyjne w ramach celów statutowych, finansując je ze swoich środków. Warunki przyznania, wysokość, okres, sposób wypłaty i zasady weryfikacji określa właściwy regulamin.

Świadczenie pielęgnacyjne jest ustanawiane i finansowane przez Fundację na podstawie statutu i regulaminu. Dokumentacja przyznania świadczenia, ocena Kolegium, formularz ARIA-Z oraz potwierdzenia wypłat tworzą spójny materiał służący wykazaniu sytuacji zdrowotnej, funkcjonalnej i opiekuńczej beneficjenta.

### 12.4. Art. 62 ustawy rehabilitacyjnej

Art. 62 ust. 3 ustawy rehabilitacyjnej przewiduje szczególne skutki dla określonej kategorii osób o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym. Zakres zastosowania pkt 1 wymaga odczytania w związku z całym art. 62, art. 5a tej ustawy oraz przepisami dotyczącymi zasiłku pielęgnacyjnego.

W przypadku dokumentów Fundacji należy odrębnie ustalić, czy spełnione są przesłanki konkretnego przepisu. Nie należy automatycznie zakładać ani ustawowej równoważności każdego dokumentu Fundacji, ani braku jakiegokolwiek znaczenia takiego dokumentu. Dokumentacja Fundacji może być przedstawiana jako materiał dowodowy, a jej znaczenie podlega ocenie według przepisów właściwych dla danej sprawy.

### 12.5. Zastrzeżenie informacyjne

Informacje w aplikacji służą wyjaśnieniu charakteru dokumentów i podstaw prawnych działania Fundacji. Nie zastępują indywidualnej analizy prawnej ani rozstrzygnięcia właściwego organu lub sądu. W razie wątpliwości co do skutku dokumentu w konkretnej sprawie należy sprawdzić właściwy przepis, statut, regulamin i stan faktyczny.

---

## 13. Lista wymaganych dokumentów Fundacji do zakończenia audytu

Do potwierdzenia wszystkich wniosków i uzupełnienia dokumentacji aplikacji niezbędne są:

1. aktualny Statut Fundacji – pełny tekst wraz ze zmianami;
2. uchwała o ustanowieniu Kolegium Orzekającego;
3. uchwała o powołaniu aktualnego składu Kolegium;
4. regulamin Kolegium Orzekającego;
5. regulamin świadczenia pielęgnacyjnego lub programu pomocy pieniężnej;
6. uchwała określająca wysokość świadczenia i źródło finansowania;
7. wzór wniosku beneficjenta;
8. wzór orzeczenia Kolegium;
9. wzór rozstrzygnięcia o przyznaniu świadczenia;
10. wzór potwierdzenia wypłaty;
11. wzór zaświadczenia dla podmiotu trzeciego;
12. klauzula informacyjna RODO i procedura przetwarzania danych o zdrowiu;
13. procedura ponownej oceny, odwołania lub weryfikacji rozstrzygnięcia.

Po otrzymaniu tych dokumentów należy wykonać audyt paragraf po paragrafie i wskazać, które kompetencje są ustanowione prawidłowo, które wymagają uzupełnienia, a które wymagają zmiany Statutu lub regulaminu.

---

## 14. Plan działań naprawczych w repozytorium

### Priorytet P0 – przed publikacją informacji użytkownikom

- [ ] Zmienić kategoryczne sformułowanie o art. 62 ust. 3 pkt 1 na opis wymagający wykładni i oceny konkretnego przypadku.
- [ ] Dodać art. 76 k.p.a. i rozróżnić dokument prywatny od dokumentu urzędowego.
- [ ] Konsekwentnie stosować nazwę „świadczenie pielęgnacyjne” dla świadczenia przyznawanego przez Fundację oraz określenie „przydzielenie zasiłku pielęgnacyjnego” w analizie art. 62 ust. 3 pkt 1.
- [ ] Dodać zastrzeżenie, że kompetencje Kolegium muszą wynikać z aktualnego Statutu i regulaminu.
- [ ] Nie wpisywać numerów paragrafów Statutu bez weryfikacji źródła.
- [ ] Oznaczyć dokumenty Fundacji jako dokumenty własne Fundacji, bez sugerowania ich państwowego wystawienia.

### Priorytet P1 – po otrzymaniu dokumentów Fundacji

- [ ] Przeprowadzić kontrolę zgodności Statutu z ustawą o fundacjach.
- [ ] Zweryfikować prawidłowość ustanowienia i powołania Kolegium.
- [ ] Zweryfikować kompetencje Kolegium do oceny funkcjonalnej i rozstrzygania o świadczeniu.
- [ ] Sprawdzić regulamin świadczenia, źródło finansowania i kryteria równego traktowania.
- [ ] Przeanalizować historyczne i systemowe znaczenie art. 62 ust. 3 pkt 1.
- [ ] Uzupełnić analizę o aktualne orzecznictwo sądów i oficjalne stanowiska organów.
- [ ] Zatwierdzić końcowe wzory dokumentów.

### Priorytet P2 – implementacja i kontrola jakości aplikacji

- [ ] Zapewnić odrębne typy dokumentów i odrębne etykiety statusu.
- [ ] Dodać wyraźne pole „podstawa statutowa” w generatorze dokumentów.
- [ ] Dodać pole „podstawa regulaminowa”.
- [ ] Dodać pole „organ wydający” i skład Kolegium.
- [ ] Dodać rozróżnienie „świadczenie ustawowe / świadczenie Fundacji”.
- [ ] Sprawdzić, czy zaświadczenie dla podmiotu trzeciego nie ujawnia nadmiernych danych zdrowotnych.
- [ ] Zapewnić spójność nazw dokumentów w katalogu, edytorze, podglądzie, eksporcie PDF i weryfikacji QR.
- [ ] Przeprowadzić testy jednostkowe katalogu dokumentów i testy generatorów.

---

## 15. Źródła prawa i materiałów

Poniższe źródła należy traktować jako podstawowy punkt odniesienia. Przed wykorzystaniem audytu w postępowaniu prawnym należy ponownie sprawdzić aktualne brzmienie przepisów i ewentualne zmiany ogłoszone po dacie stanu prawnego tekstu jednolitego.

1. **Ustawa z dnia 6 kwietnia 1984 r. o fundacjach**, tekst jednolity Dz.U. z 2023 r. poz. 166, ze zmianą Dz.U. z 2026 r. poz. 316 – [ELI, tekst ustawy](https://eli.gov.pl/eli/DU/2023/166/text.html).
2. **Ustawa z dnia 23 stycznia 2026 r. o zmianie ustawy o fundacjach oraz ustawy – Prawo o stowarzyszeniach**, Dz.U. z 2026 r. poz. 316 – [ELI](https://eli.gov.pl/eli/DU/2026/316/ogl).
3. **Ustawa z dnia 27 sierpnia 1997 r. o rehabilitacji zawodowej i społecznej oraz zatrudnianiu osób niepełnosprawnych**, tekst jednolity Dz.U. z 2026 r. poz. 884 – [ELI](https://eli.gov.pl/eli/DU/2026/884/ogl).
4. **Ustawa z dnia 28 listopada 2003 r. o świadczeniach rodzinnych**, tekst jednolity Dz.U. z 2026 r. poz. 1240 – [ELI](https://eli.gov.pl/eli/DU/2026/1240/ogl/pol).
5. **Ustawa z dnia 14 czerwca 1960 r. – Kodeks postępowania administracyjnego**, tekst jednolity Dz.U. z 2025 r. poz. 1691 – [ELI](https://eli.gov.pl/eli/DU/2025/1691/ogl/pol).
6. **Konstytucja Rzeczypospolitej Polskiej** – w szczególności art. 4, 7, 12, 58, 87, 88 i 94 – [ELI, akty prawne](https://eli.gov.pl/).
7. **Rozporządzenie Parlamentu Europejskiego i Rady (UE) 2016/679 (RODO)** – w szczególności art. 5, 6, 9, 13, 25, 30 i 32 – [EUR-Lex](https://eur-lex.europa.eu/eli/reg/2016/679/oj).

### 15.1. Zasady korzystania ze źródeł

- Tekst jednolity ułatwia ustalenie brzmienia aktu, ale nie zastępuje sprawdzenia zmian późniejszych.
- Informacje i stanowiska organów administracji są materiałem interpretacyjnym; nie są automatycznie źródłem powszechnie obowiązującego prawa.
- Orzeczenia sądów należy analizować z uwzględnieniem stanu prawnego i faktów konkretnej sprawy.
- Wzory dokumentów Fundacji należy opierać na rzeczywistym Statucie i regulaminach, a nie na samych opisach funkcji aplikacji.


---

## 16. Rozszerzona wykładnia konstytucyjna: Naród jako suweren, mandat przedstawicielski i mandat nieparlamentarny

### 16.1. Suwerenność Narodu jako zasada ustrojowa

Punktem wyjścia jest art. 4 Konstytucji RP: władza zwierzchnia w Rzeczypospolitej Polskiej należy do Narodu, a Naród sprawuje ją przez swoich przedstawicieli lub bezpośrednio. Nie jest to wyłącznie deklaracja symboliczna. Jest to zasada ustrojowa, która wyznacza źródło legitymacji władzy publicznej, a zarazem nakazuje odczytywać ustrój w sposób uwzględniający podmiotowość obywateli, wspólnot i mechanizmów bezpośredniego udziału w sprawach publicznych.

Wykładnię art. 4 należy łączyć z:
- art. 1 Konstytucji – Rzeczpospolita Polska jest dobrem wspólnym wszystkich obywateli;
- art. 2 – państwo jest demokratycznym państwem prawnym urzeczywistniającym zasady sprawiedliwości społecznej;
- art. 7 – organy władzy publicznej działają na podstawie i w granicach prawa;
- art. 8 – Konstytucja jest najwyższym prawem Rzeczypospolitej;
- art. 10 – zasada podziału i równowagi władz;
- art. 11 – wolność tworzenia i działania partii politycznych;
- art. 12 – wolność tworzenia i działania zrzeszeń, ruchów obywatelskich, stowarzyszeń, fundacji i innych dobrowolnych zrzeszeń;
- art. 30 i 32 – godność człowieka, równość i zakaz dyskryminacji;
- art. 54, 57 i 58 – wolność wyrażania poglądów, pokojowych zgromadzeń i zrzeszania się;
- art. 60 – równy dostęp obywateli korzystających z pełni praw publicznych do służby publicznej;
- art. 61 – prawo do informacji o działalności organów władzy publicznej i osób pełniących funkcje publiczne;
- art. 63 – prawo składania petycji, wniosków i skarg;
- art. 67, 68 i 69 – zabezpieczenie społeczne, ochrona zdrowia i pomoc osobom z niepełnosprawnościami;
- art. 82 – obowiązek wierności Rzeczypospolitej Polskiej oraz troski o dobro wspólne;
- art. 83 – obowiązek przestrzegania prawa Rzeczypospolitej Polskiej;
- art. 104 – konstytucyjny status posłów jako przedstawicieli Narodu;
- art. 118 – inicjatywa ustawodawcza;
- art. 125 – referendum ogólnokrajowe.

Takie odczytanie pokazuje, że dobro Narodu nie jest wyłącznie hasłem politycznym. Jest wartością ustrojową, która znajduje odzwierciedlenie w obowiązkach organów państwa, w prawach obywateli i w społecznych formach samoorganizacji.

### 16.2. Przedstawicielstwo Narodu i mandat parlamentarny

Art. 104 ust. 1 Konstytucji stanowi, że posłowie są przedstawicielami Narodu i nie wiążą ich instrukcje wyborców. Ustawa z 9 maja 1996 r. o wykonywaniu mandatu posła i senatora w art. 1 ust. 1 nakazuje posłom i senatorom wykonywać mandat, kierując się dobrem Narodu. Art. 2 ustawy ustanawia obowiązek ślubowania, którego treść odwołuje się do sumiennego wykonywania obowiązków wobec Narodu, strzeżenia suwerenności Ojczyzny i dobra obywateli oraz przestrzegania porządku prawnego. Art. 3 wskazuje czynne uczestnictwo w pracach Sejmu, Senatu i Zgromadzenia Narodowego jako podstawowe prawo i obowiązek parlamentarzysty. <Źródła: Konstytucja RP, art. 4, 10, 104; ustawa o wykonywaniu mandatu posła i senatora, art. 1–6, tekst jednolity Dz.U. z 2026 r. poz. 1282.>

Mandat parlamentarny jest zatem szczególną, konstytucyjnie i ustawowo ukształtowaną funkcją przedstawicielską. Ma określony sposób uzyskania, treść, czas trwania, zakres praw i obowiązków, zasady odpowiedzialności oraz ochronę właściwą dla wykonywania mandatu. Nie jest jedynie deklaracją moralną ani samą gotowością do służby. Jego źródłem są wybory i procedury określone w Konstytucji oraz ustawach.

### 16.3. Mandat nieparlamentarny – konstrukcja społeczna i publiczna przyjmowana w koncepcji ARIA

W koncepcji przedstawionej przez Fundatora pojęcie „mandatu nieparlamentarnego” oznacza odrębny rodzaj powołania do służby Narodowi i obywatelom, niewywodzący się z wyboru do Sejmu albo Senatu. Jego treścią jest odpowiedzialność za dobro wspólne, podejmowanie działań w interesie obywateli, inicjowanie przedsięwzięć społecznych, organizowanie pomocy oraz sprawowanie funkcji służebnej wobec Narodu.

W tym znaczeniu mandat nieparlamentarny jest rozumiany jako:
- powołanie do służby dobru narodowemu;
- przyjęcie odpowiedzialności za konkretne dobro wspólne;
- zobowiązanie do działania na rzecz obywateli;
- samodzielne podejmowanie inicjatyw w ramach posiadanych kompetencji;
- gotowość do występowania w obronie praw i godności człowieka;
- organizowanie wspólnoty, instytucji i działań służących dobru społecznemu;
- odpowiedzialność za uczciwe, jawne i rozliczalne wykonywanie powierzonych zadań.

W języku audytu należy wyraźnie opisać, że jest to **autorska koncepcja mandatu służby Narodowi**, rozwijana w ramach obywatelskiej i społecznej aktywności Fundatora. Jej legitymacja moralna, społeczna lub organizacyjna może być wyrażana w deklaracji, akcie powołania, statucie, regulaminie, uchwale albo zobowiązaniu do służby. Taka konstrukcja może porządkować działalność Fundacji i określać etos osób pełniących w niej funkcje.

Nie należy jednak utożsamiać tej koncepcji z formalnym mandatem posła w rozumieniu art. 104 Konstytucji ani z mandatem do reprezentowania państwa lub wydawania decyzji administracyjnych. Właściwe dla niej znaczenie polega na społecznej legitymacji, zobowiązaniu do służby i wykonywaniu konkretnych zadań w granicach prawa oraz umocowania organizacyjnego.

### 16.4. Poseł z mandatem nieparlamentarnym – pojęcie używane w dokumentacji Fundatora

W materiałach Fundatora może być używane określenie „poseł z mandatem nieparlamentarnym” lub „poseł z powołania” jako nazwa roli społecznej, służebnej i reprezentacyjnej w przyjętym przez niego porządku organizacyjnym. Należy je definiować przez treść obowiązków, a nie przez podobieństwo do tytułu konstytucyjnego.

W tej koncepcji „poseł” oznacza osobę, która:
1. przyjmuje powołanie do służby Narodowi i obywatelom;
2. reprezentuje potrzeby i interesy powierzonych jej osób lub wspólnoty w granicach udzielonego umocowania;
3. podejmuje działania służące dobru wspólnemu;
4. przedstawia postulaty, wnioski i rozwiązania właściwym organom;
5. organizuje działania pomocowe i społeczne;
6. odpowiada za rzetelność i uczciwość wykonywanych zadań;
7. nie wykorzystuje funkcji dla prywatnej korzyści;
8. działa z poszanowaniem Konstytucji i ustaw.

Konstytucyjny punkt odniesienia dla etosu tej służby stanowią art. 1, 4, 30, 32, 60, 63 i 82 Konstytucji, a także art. 1 ustawy o wykonywaniu mandatu posła i senatora, który wyraża zasadę kierowania się dobrem Narodu. W dokumentach należy jednak zaznaczyć, że odwołanie do tych zasad ma charakter wartościujący i programowy; nie oznacza uzyskania mandatu parlamentarnego, immunitetu, prawa głosu w Sejmie ani ustawowej kompetencji przedstawiciela organu państwa.

### 16.5. Bezpośrednie sprawowanie władzy przez Suwerena – wykładnia i formy konstytucyjne

Art. 4 ust. 2 Konstytucji stanowi, że Naród sprawuje władzę przez przedstawicieli lub bezpośrednio. Bezpośrednie formy sprawowania władzy są rozwijane między innymi przez:
- referendum ogólnokrajowe – art. 125 Konstytucji;
- referendum lokalne – art. 170 Konstytucji i ustawa o referendum lokalnym;
- inicjatywę ustawodawczą grupy obywateli – art. 118 ust. 2 Konstytucji i ustawa o wykonywaniu inicjatywy ustawodawczej przez obywateli;
- udział w wyborach i referendach – art. 62 Konstytucji oraz właściwe przepisy prawa wyborczego;
- petycje, wnioski i skargi – art. 63 Konstytucji oraz ustawa o petycjach i przepisy działu VIII k.p.a.;
- jawność i kontrolę społeczną – art. 61 Konstytucji oraz ustawa o dostępie do informacji publicznej.

Poza tymi sformalizowanymi mechanizmami obywatele sprawują wpływ na życie publiczne poprzez organizacje społeczne, fundacje, stowarzyszenia, konsultacje, inicjatywy lokalne, działalność charytatywną, rzecznictwo i udział w debacie publicznej. Są to formy uczestnictwa w życiu publicznym i oddziaływania na sposób wykonywania władzy, choć nie każda z nich stanowi w ścisłym znaczeniu bezpośrednie wykonywanie władzy państwowej.

Koncepcja ARIA może zatem odwoływać się do suwerenności Narodu jako źródła społecznej odpowiedzialności i aktywności. Kolegium Orzekające może być przedstawione jako instytucja społeczna utworzona przez obywateli w ramach prawa do zrzeszania się i organizowania działań na rzecz dobra wspólnego. Jego legitymacja wynika z ustanowienia zgodnie z prawem, a jego kompetencje wynikają z właściwych aktów ustrojowych i przepisów.

### 16.6. Zasada pomocniczości i wspólnotowa odpowiedzialność

Preambuła Konstytucji odwołuje się do zasady pomocniczości, która ma umacniać uprawnienia obywateli i ich wspólnot. Zasada ta wspiera taki model, w którym działania publiczne nie wykluczają odpowiedzialności rodzin, organizacji społecznych i wspólnot lokalnych, a podmioty społeczne mogą samodzielnie podejmować inicjatywy odpowiadające na potrzeby ludzi.

W działalności ARIA zasada pomocniczości uzasadnia:
- organizowanie wsparcia rodzin i osób z niepełnosprawnościami;
- tworzenie procedur rozpoznawania potrzeb;
- angażowanie ekspertów i wolontariuszy;
- rozwijanie usług rehabilitacyjnych i edukacyjnych;
- tworzenie systemu świadczeń ze środków własnych;
- współpracę z organami publicznymi i innymi organizacjami;
- dokumentowanie potrzeb społecznych i przedstawianie postulatów władzy publicznej.

### 16.7. Służba dobru Narodu a obowiązek przestrzegania prawa

Etos służby Narodowi musi być połączony z zasadą legalizmu. W odniesieniu do organów władzy publicznej wynika ona z art. 7 Konstytucji; w odniesieniu do obywateli – z art. 83, który ustanawia obowiązek przestrzegania prawa Rzeczypospolitej Polskiej. Art. 82 nakłada na obywatela polskiego obowiązek wierności Rzeczypospolitej Polskiej oraz troski o dobro wspólne.

Dlatego mandat nieparlamentarny w rozumieniu Fundatora powinien być opisany jako mandat służby i odpowiedzialności, a nie jako zwolnienie z obowiązującego prawa albo samodzielne uprawnienie do wykonywania każdej kompetencji państwowej. Służba dobru Narodu wymaga działania prawdziwego, pokojowego, proporcjonalnego, transparentnego i respektującego prawa innych osób.

### 16.8. Proponowane brzmienie deklaracji mandatu nieparlamentarnego

„Przyjmuję mandat służby nieparlamentarnej jako osobiste i społeczne zobowiązanie do działania dla dobra Narodu, Ojczyzny i obywateli. Powołanie to rozumiem jako obowiązek odpowiedzialnej służby, ochrony godności człowieka, wspierania rodzin, działania na rzecz zdrowia, sprawiedliwości społecznej i dobra wspólnego. Zobowiązuję się działać uczciwie, rzetelnie, pokojowo i z poszanowaniem Konstytucji oraz ustaw Rzeczypospolitej Polskiej, w granicach kompetencji wynikających z prawa, statutu, powierzenia i przyjętej odpowiedzialności. Mandat ten ma charakter społeczny, etyczny i organizacyjny; nie jest mandatem posła lub senatora w rozumieniu art. 104 Konstytucji.”

---

## 17. Rozbudowana wykładnia art. 62 ust. 3 ustawy rehabilitacyjnej – stanowisko ARIA

### 17.1. Treść przepisu i jego samodzielność redakcyjna

Art. 62 ust. 1 odnosi się do osób zaliczonych do dawnych grup inwalidów przed wejściem w życie ustawy, natomiast ust. 3 stanowi odrębnie, że osoby o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym uznaje się za niepełnosprawne. W ust. 1 ustawodawca wyraźnie posłużył się ograniczeniem czasowym. W ust. 3 ograniczenia tego nie zamieścił.

Stanowisko ARIA wychodzi z założenia, że różnica redakcyjna pomiędzy tymi przepisami musi mieć znaczenie prawne. Nie można przenosić do ust. 3 warunku, który ustawodawca wyraził w ust. 1, a następnie pominął w odrębnej jednostce redakcyjnej.

### 17.2. Wykładnia językowa – „osoby o stałej albo długotrwałej niezdolności”

Ustawodawca posługuje się kryterium stanu osoby, a nie daty wydania orzeczenia. Zwrot „osoby o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym” opisuje kategorię podmiotów przez ich właściwość funkcjonalną i zawodową.

Przepis nie mówi: „osoby, którym przed dniem wejścia w życie ustawy wydano orzeczenie”. Nie mówi również: „osoby, które przed tą datą uzyskały prawo do renty”. Zastosowanie kryterium daty wydania orzeczenia wymaga więc odrębnego uzasadnienia, którego nie można zastąpić samym tytułem rozdziału.

### 17.3. Zwrot „uznaje się za niepełnosprawne”

Sformułowanie „uznaje się” ma charakter normatywny. Ustawodawca nie wskazuje jedynie, że dana osoba może ubiegać się o zaliczenie do osób niepełnosprawnych, lecz formułuje ustawową regułę kwalifikacyjną.

Wykładnia ARIA podkreśla, że należy zachować różnicę pomiędzy:
- stwierdzeniem, że osoba spełnia przesłankę niezdolności;
- ustawowym skutkiem uznania jej za osobę niepełnosprawną;
- przyporządkowaniem do określonego stopnia;
- ustaleniem wskazań funkcjonalnych.

To cztery odrębne zagadnienia, które w dokumentacji ARIA powinny być opisywane osobno.

### 17.4. Stopień znaczny i lekki

Art. 62 ust. 3 przewiduje dwie kategorie:
- osoby, którym przysługuje zasiłek pielęgnacyjny – traktuje się jako zaliczone do znacznego stopnia;
- pozostałe osoby – traktuje się jako zaliczone do lekkiego stopnia.

Ustawodawca nie przewidział w tym ustępie kategorii umiarkowanej. System ARIA nie może więc dowolnie zmieniać ustawowego przyporządkowania. Może natomiast prowadzić szczegółową ocenę funkcjonalną i wykazywać, jakie potrzeby, ograniczenia i wskazania występują u osoby badanej.

### 17.5. Ustęp 1 i ustęp 3 – zakaz nieuzasadnionego przenoszenia ograniczenia

W ust. 1 warunek czasowy jest wyrażony bezpośrednio. W ust. 3 nie został powtórzony. Zasada racjonalnego ustawodawcy pozwala przyjąć, że świadome zróżnicowanie redakcyjne może oznaczać zróżnicowanie zakresu norm.

Stanowisko ARIA podnosi, że wykładnia nie powinna dopisywać do ust. 3 dodatkowej przesłanki temporalnej tylko dlatego, że ustęp ten znajduje się w rozdziale zatytułowanym „Zmiany w przepisach obowiązujących, przepisy przejściowe i końcowe”.

Tytuł rozdziału ma znaczenie systemowe, ale nie może samodzielnie zastąpić treści normy. Jeżeli ustawodawca chciał ograniczyć ust. 3 do orzeczeń historycznych, mógł posłużyć się podobnym sformułowaniem jak w ust. 1.

### 17.6. Art. 63 jako odrębna regulacja praw nabytych

Art. 63 zachowuje wcześniejsze uprawnienia osób niepełnosprawnych, o których mowa w art. 62, jeżeli nabyły je przed wejściem w życie ustawy. Jego funkcja polega na ochronie praw nabytych. Nie należy automatycznie odczytywać go jako przepisu, który ogranicza zakres osób wskazanych w art. 62 ust. 3.

Zestawienie art. 62 i 63 umożliwia zatem argumentację, że:
- art. 62 określa kategorie osób;
- art. 63 zabezpiecza dotychczasowe uprawnienia;
- regulacja dotycząca zachowania praw nabytych nie musi wyczerpywać wszystkich skutków art. 62 ust. 3.

### 17.7. Art. 5a – ustawowe znaczenie orzeczeń dotyczących niezdolności w gospodarstwie rolnym

Art. 5a ust. 1 odwołuje się do osób posiadających ważne orzeczenia o niezdolności do pracy w gospodarstwie rolnym, o których mowa w art. 62. Osoby te mogą składać do zespołu do spraw orzekania o niepełnosprawności wniosek o ustalenie stopnia i wskazań. Ustęp 2 określa sposób ustalania stopnia na podstawie przedłożonych orzeczeń, a wskazania odnoszą się do stanu funkcjonalnego.

Wykładnia ARIA zwraca uwagę na to, że ustawodawca:
- uznaje odrębną kategorię orzeczeń dotyczących niezdolności do pracy w gospodarstwie rolnym;
- wiąże z nimi określone skutki w systemie orzekania o niepełnosprawności;
- odróżnia ustalenie stopnia od ustalania wskazań;
- dopuszcza wykorzystanie dokumentacji dotyczącej niezdolności w dalszym postępowaniu.

Jest to ważny argument systemowy za traktowaniem stanu funkcjonalnego i zdolności do pracy w gospodarstwie rolnym jako samodzielnego przedmiotu oceny.

### 17.8. Art. 3, 4, 5, 5a i 6b – miejsce art. 62 w systemie orzecznictwa

Art. 3 określa stopnie niepełnosprawności. Art. 4 opisuje przesłanki zaliczenia do poszczególnych stopni. Art. 5 ustanawia równoważność niektórych orzeczeń ZUS z określonymi stopniami. Art. 5a reguluje postępowanie dotyczące osób posiadających określone orzeczenia. Art. 6 i 6b regulują zespoły orzekające oraz wskazania.

Art. 62 ust. 3 należy analizować w tej strukturze, a nie w izolacji. Z jednej strony jest umieszczony w rozdziale końcowym, z drugiej zaś jego treść bezpośrednio nawiązuje do statusu osoby niepełnosprawnej i stopni określonych w art. 3. Wykładnia systemowa powinna uwzględniać oba te elementy.

### 17.9. Przepisy Konstytucji wspierające wykładnię ochronną

Wykładnia art. 62 powinna być odczytywana w zgodzie z:
- art. 2 – zasadą demokratycznego państwa prawnego i sprawiedliwości społecznej;
- art. 30 – godnością człowieka;
- art. 32 – równością wobec prawa;
- art. 67 – prawem do zabezpieczenia społecznego w zakresie określonym ustawą;
- art. 68 – prawem do ochrony zdrowia;
- art. 69 – obowiązkiem władz udzielania pomocy osobom z niepełnosprawnościami;
- art. 71 – uwzględnianiem dobra rodziny;
- art. 75 i 76 – ochroną praw określonych grup i konsumentów w zakresie wskazanym w Konstytucji;
- art. 77 – prawem do wynagrodzenia szkody wyrządzonej przez niezgodne z prawem działanie władzy publicznej.

Przepisy te nie zastępują wykładni językowej, lecz stanowią kontekst aksjologiczny dla rozumienia ustawowych regulacji ochronnych.

### 17.10. Wniosek interpretacyjny

Stanowisko Fundacji ARIA zakłada, że art. 62 ust. 3 należy traktować jako samodzielną normę kwalifikacyjną, której nie wolno automatycznie ograniczać datą, której ustawodawca nie wpisał do jej treści. Najsilniejszym argumentem pozostaje różnica redakcyjna pomiędzy ust. 1 i ust. 3, wzmocniona przez zwrot „uznaje się” oraz relację z art. 5a i 63.

---

## 18. Świadczenie pielęgnacyjne – konstrukcja prawna w dokumentacji ARIA

### 18.1. Nazwa i zakres pojęcia

W dokumentacji Fundacji należy konsekwentnie używać nazwy **świadczenie pielęgnacyjne**. Nie należy używać w odniesieniu do wypłat Fundacji określeń „świadczenie pielęgnacyjne”, „zasiłek Fundacji” ani „świadczenie pielęgnacyjne”.

Świadczenie pielęgnacyjne jest świadczeniem ustanowionym przez Fundację w ramach jej działalności statutowej, przyznawanym na podstawie ustalonych kryteriów i finansowanym ze środków Fundacji.

W odniesieniu do art. 62 ust. 3 pkt 1 należy zachować dokładne ustawowe określenie „przydzielenie zasiłku pielęgnacyjnego”.

### 18.2. Podstawy działalności świadczeniowej Fundacji

Ustawa o fundacjach dopuszcza tworzenie fundacji w celach ochrony zdrowia, opieki i pomocy społecznej. Art. 4 i 5 pozwalają określić cele, formy i zakres działalności. Art. 10 powierza zarządowi kierowanie działalnością i reprezentację Fundacji. W konsekwencji Fundacja może ustanawiać instrumenty pomocy pieniężnej związane z realizacją celów statutowych, z uwzględnieniem zasad gospodarowania majątkiem, prawa podatkowego, rachunkowości i postanowień statutu.

### 18.3. Przesłanki świadczenia pielęgnacyjnego

Regulamin powinien wskazywać:
- cel i charakter świadczenia;
- krąg uprawnionych;
- kryteria zdrowotne i funkcjonalne;
- sposób oceny samodzielności;
- zakres pomocy i pielęgnacji;
- wymagane dokumenty;
- sposób ustalania wysokości;
- okres przyznania;
- częstotliwość wypłaty;
- sposób dokumentowania płatności;
- przesłanki zmiany i zakończenia;
- procedurę ponownej oceny;
- zasady kontroli i rozliczania.

### 18.4. Decyzja i dokumentacja wypłaty

Decyzja o przyznaniu świadczenia pielęgnacyjnego powinna zawierać:
- podstawę statutową;
- podstawę regulaminową;
- numer sprawy;
- dane beneficjenta;
- datę i organ rozstrzygający;
- ustalenia Kolegium;
- wysokość świadczenia;
- okres przyznania;
- częstotliwość wypłaty;
- warunki utrzymania uprawnienia;
- podpis osoby upoważnionej.

Potwierdzenie wypłaty powinno odrębnie dokumentować faktyczne przekazanie środków.

### 18.5. Rozróżnienie od ustawowego zasiłku pielęgnacyjnego

Ustawa o świadczeniach rodzinnych w art. 16 określa cel, krąg uprawnionych i przesłanki przyznania zasiłku pielęgnacyjnego. Art. 20 reguluje właściwość w sprawach świadczeń rodzinnych. W audycie należy zatem zachować rozróżnienie pomiędzy nazwą świadczenia pielęgnacyjnego ustanowionego przez Fundację a ustawowym pojęciem zasiłku pielęgnacyjnego, gdy analizowany jest art. 62 ust. 3 pkt 1.

---

## 19. WHODAS 2.0 – standaryzowana ocena funkcjonowania

### 19.1. Podstawa metodologiczna

WHODAS 2.0 jest narzędziem WHO do pomiaru funkcjonowania i niepełnosprawności, opracowanym w oparciu o koncepcję ICF. WHO udostępnia wersję 36-pozycyjną i 12-pozycyjną, formularze samooceny, wywiadu i oceny przez informatora oraz oficjalne algorytmy punktacji.

W systemie ARIA WHODAS 2.0 powinien służyć do standaryzowanej oceny trudności w codziennym funkcjonowaniu i porównywania wyników w czasie.

### 19.2. Sześć domen WHODAS 2.0

1. Poznanie i komunikowanie się.
2. Poruszanie się.
3. Samoobsługa.
4. Relacje z ludźmi.
5. Aktywności życiowe.
6. Uczestniczenie w życiu społecznym.

Wersja 36-pozycyjna pozwala analizować wynik ogólny oraz wyniki poszczególnych domen. Wersja 12-pozycyjna pozwala na krótszy pomiar ogólnego funkcjonowania.

### 19.3. Sposób przeprowadzania

Należy przewidzieć:
- samoocenę osoby badanej;
- wywiad z osobą badaną;
- ocenę z udziałem opiekuna lub informatora.

W dokumentacji należy zaznaczyć, kto udzielał odpowiedzi. W przypadku rozbieżności między samooceną a oceną opiekuna należy odnotować obie perspektywy.

### 19.4. Punktacja

Odpowiedzi są oceniane od 0 do 4, od braku trudności do trudności skrajnej lub niemożności wykonania czynności. WHO opisuje metodę prostego sumowania oraz metodę złożoną opartą na teorii odpowiedzi na pozycję (IRT), przeliczającą wynik na skalę 0–100.

ARIA powinna stosować oficjalne instrukcje punktacji właściwe dla danej wersji. Wynik nie może być samodzielnym automatycznym kryterium przyznania stopnia niepełnosprawności lub świadczenia pielęgnacyjnego.

### 19.5. Wykorzystanie w ARIA-Z

Formularz ARIA-Z powinien zapisywać:
- wersję WHODAS;
- datę;
- osobę udzielającą odpowiedzi;
- sposób przeprowadzenia;
- wynik ogólny;
- wynik każdej domeny;
- interpretację kliniczną;
- porównanie z poprzednią oceną.

---

## 20. ICF WHO – klasyfikacja funkcjonowania

### 20.1. Cztery komponenty ICF

- **b – funkcje organizmu**;
- **s – struktury organizmu**;
- **d – aktywność i uczestniczenie**;
- **e – czynniki środowiskowe**.

Czynniki osobiste mogą być opisywane narracyjnie.

### 20.2. ICF Checklist 2.1a

W dokumentacji ARIA-Z należy przewidzieć stosowanie ICF Checklist 2.1a, czyli formularza klinicznego WHO. Formularz pozwala zbierać informacje z wywiadu, dokumentacji, obserwacji i innych źródeł.

### 20.3. Kwalifikatory ICF

Należy stosować kwalifikatory określające poziom problemu, zgodnie z zasadami kodowania dla poszczególnych kategorii ICF:
- 0 – brak problemu;
- 1 – problem niewielki;
- 2 – problem umiarkowany;
- 3 – problem znaczny;
- 4 – problem całkowity;
- 8 – nieokreślony;
- 9 – nie dotyczy.

Nie należy traktować kwalifikatora jako samodzielnego odpowiednika stopnia niepełnosprawności.

### 20.4. Zdolność i wykonanie

W ocenie aktywności należy odróżniać zdolność do wykonania czynności w określonych warunkach od rzeczywistego wykonania w środowisku życia. Osoba może wykonać czynność wyłącznie przy pomocy sprzętu, opiekuna lub dostosowania otoczenia.

### 20.5. Zastosowanie ICF do pracy w gospodarstwie rolnym

Formularz powinien obejmować:
- mobilność na nierównym terenie;
- długotrwałe stanie i chodzenie;
- schylanie, klękanie i podnoszenie;
- przenoszenie ciężarów;
- sprawność kończyn górnych;
- czynności manualne;
- obsługę narzędzi;
- uwagę i planowanie;
- podejmowanie decyzji;
- bezpieczeństwo pracy;
- możliwość powtarzania czynności;
- wydolność w dłuższym okresie;
- tolerancję wysiłku;
- potrzebę pomocy i nadzoru.

### 20.6. Połączenie WHODAS 2.0 i ICF

WHODAS 2.0 zapewnia standaryzowany pomiar trudności, natomiast ICF pozwala dokładnie sklasyfikować obszar i charakter ograniczenia. Wspólne zastosowanie obu metod w ARIA-Z tworzy bardziej kompletny obraz funkcjonowania niż sama diagnoza kliniczna albo pojedyncza skala.

---

## 21. ARIA-Z – proponowany standard zbiorczej oceny

Formularz ARIA-Z powinien obejmować następujące części:

| Część | Treść |
|---|---|
| A | Dane osoby i cel oceny |
| B | Dokumentacja medyczna |
| C | Wywiad i historia choroby |
| D | Badanie przedmiotowe |
| E | WHODAS 2.0 |
| F | ICF WHO – funkcje i struktury |
| G | ICF WHO – aktywność i uczestniczenie |
| H | ICF WHO – czynniki środowiskowe |
| I | Samodzielność w czynnościach życia codziennego |
| J | Potrzeba pomocy, pielęgnacji i nadzoru |
| K | Ocena czynności gospodarskich |
| L | Ocena trwałości i rokowania |
| M | Analiza dokumentów źródłowych |
| N | Profil funkcjonalny |
| O | Uzasadnienie Kolegium |
| P | Wnioski i rozstrzygnięcie |

### 21.1. Ocena czynności rolniczych

Nie należy oceniać jedynie zdolności do jednorazowego wykonania ruchu. Należy zbadać, czy osoba może wykonywać określone czynności regularnie, bezpiecznie, przez wymagany czas i bez nadmiernego ryzyka pogorszenia stanu zdrowia.

### 21.2. Ocena potrzeby pomocy

Należy odnotować:
- czynności wykonywane samodzielnie;
- czynności wykonywane z trudnością;
- czynności wymagające częściowej pomocy;
- czynności wymagające całkowitego wyręczenia;
- potrzebę nadzoru;
- częstotliwość pomocy;
- czas potrzebny na wykonanie czynności;
- ryzyko i bezpieczeństwo.

### 21.3. Profil funkcjonalny

Końcowy profil powinien syntetyzować wyniki kliniczne, WHODAS 2.0, ICF, wywiad i obserwację. Powinien wyjaśniać, jak stwierdzone ograniczenia przekładają się na samodzielność, uczestnictwo i zdolność do wykonywania obowiązków.

---

## 22. Rozbudowana podstawa prawna – wykaz przepisów do systematycznej analizy

### 22.1. Konstytucja RP

W audycie należy uwzględniać w szczególności:
- art. 1 – dobro wspólne;
- art. 2 – demokratyczne państwo prawne i sprawiedliwość społeczna;
- art. 4 – suwerenność Narodu;
- art. 7 – legalizm;
- art. 8 – nadrzędność Konstytucji;
- art. 10 – podział i równowaga władz;
- art. 12 – wolność zrzeszania się;
- art. 30 – godność człowieka;
- art. 31 – zasady ograniczania wolności i praw;
- art. 32 – równość;
- art. 37 – zakres korzystania z wolności i praw;
- art. 45 – prawo do sądu;
- art. 47 – prywatność;
- art. 54 – wolność wypowiedzi;
- art. 57 – zgromadzenia;
- art. 58 – zrzeszanie się;
- art. 60 – dostęp do służby publicznej;
- art. 61 – informacja publiczna;
- art. 62 – prawa wyborcze i referendum;
- art. 63 – petycje, wnioski i skargi;
- art. 67 – zabezpieczenie społeczne;
- art. 68 – ochrona zdrowia;
- art. 69 – pomoc osobom z niepełnosprawnościami;
- art. 71 – ochrona rodziny;
- art. 77 – odpowiedzialność za niezgodne z prawem działanie władzy;
- art. 82 – wierność Rzeczypospolitej i troska o dobro wspólne;
- art. 83 – obowiązek przestrzegania prawa;
- art. 87–94 – źródła prawa;
- art. 104 – status posłów jako przedstawicieli Narodu;
- art. 118 – inicjatywa ustawodawcza;
- art. 125 – referendum ogólnokrajowe;
- art. 163–172 – samorząd terytorialny, w tym referendum lokalne z art. 170.

### 22.2. Ustawa o fundacjach

W szczególności:
- art. 1 – cele fundacji;
- art. 2 – ustanowienie fundacji;
- art. 3 – oświadczenie o ustanowieniu;
- art. 4 – działanie na podstawie ustawy i statutu;
- art. 5 – statut, organizacja i inne organy;
- art. 7 – rejestracja;
- art. 10 – zarząd;
- art. 12 – obowiązki sprawozdawcze;
- art. 13 – nadzór właściwego ministra i starosty;
- art. 14 – środki nadzorcze;
- art. 15 – rozwiązanie fundacji w przypadkach ustawowych.

### 22.3. Ustawa rehabilitacyjna

W szczególności:
- art. 1 – zakres podmiotowy;
- art. 3 – stopnie niepełnosprawności;
- art. 4 – przesłanki stopni;
- art. 5 – równoważność orzeczeń ZUS;
- art. 5a – orzeczenia o niezdolności do pracy w gospodarstwie rolnym i ustalanie stopnia oraz wskazań;
- art. 6 – zespoły do spraw orzekania o niepełnosprawności;
- art. 6b – orzeczenia i wskazania;
- art. 62 – uznanie osób o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym za niepełnosprawne;
- art. 63 – zachowanie praw nabytych;
- art. 66 – przepisy uzupełniające.

### 22.4. Ustawa o świadczeniach rodzinnych

W szczególności:
- art. 2 – katalog świadczeń rodzinnych;
- art. 16 – zasiłek pielęgnacyjny;
- art. 20 – właściwość organów;
- art. 23 i nast. – tryb ubiegania się o świadczenia;
- przepisy dotyczące wstrzymania, uchylenia i zwrotu świadczeń nienależnie pobranych.

### 22.5. Ustawa o ubezpieczeniu społecznym rolników

Należy przeanalizować w szczególności:
- art. 21 i przepisy dotyczące renty rolniczej;
- definicję całkowitej niezdolności do pracy w gospodarstwie rolnym;
- rolę lekarza rzeczoznawcy i komisji lekarskiej KRUS;
- zasady orzekania o okresowej lub trwałej niezdolności;
- skutki orzeczeń dla świadczeń rentowych;
- relację przepisów KRUS do art. 62 ustawy rehabilitacyjnej.

### 22.6. Kodeks postępowania administracyjnego

W szczególności:
- art. 6–8 – legalizm, praworządność, zaufanie;
- art. 9 – obowiązek informowania;
- art. 10 – czynny udział strony;
- art. 11 – wyjaśnianie przesłanek;
- art. 12 – szybkość i prostota;
- art. 24–27 – wyłączenie pracownika i organu;
- art. 28 – strona;
- art. 35–37 – terminy i bezczynność;
- art. 61 – wszczęcie postępowania;
- art. 63–64 – podania;
- art. 67–73 – protokoły, akta i dostęp;
- art. 75–81 – dowody, dokumenty, opinie i ocena materiału;
- art. 104 – decyzje;
- art. 107 – elementy decyzji;
- art. 127 i nast. – środki zaskarżenia.

### 22.7. Kodeks cywilny

W szczególności:
- art. 8–24 – osoby fizyczne i dobra osobiste;
- art. 33–43 – osoby prawne;
- art. 58 – nieważność czynności sprzecznych z ustawą;
- art. 65 – wykładnia oświadczeń woli;
- art. 353¹ – swoboda umów;
- art. 405 i nast. – bezpodstawne wzbogacenie;
- art. 415 i nast. – odpowiedzialność deliktowa;
- art. 416 – odpowiedzialność osoby prawnej za winę organu.

### 22.8. Ustawa o działalności pożytku publicznego i o wolontariacie

W szczególności:
- art. 3 – podmioty prowadzące działalność pożytku publicznego;
- art. 4 – sfera zadań publicznych;
- art. 5 – współpraca z administracją;
- art. 5a – programy współpracy;
- art. 11–19 – zlecanie realizacji zadań publicznych;
- art. 44 i nast. – wolontariat.

### 22.9. Prawo finansowe i rachunkowe

W zależności od źródła środków należy uwzględnić:
- ustawę o rachunkowości;
- ustawę o podatku dochodowym od osób prawnych;
- ustawę o podatku dochodowym od osób fizycznych;
- ustawę o finansach publicznych, jeżeli pojawiają się środki publiczne;
- przepisy dotyczące darowizn, dotacji, zbiórek i działalności odpłatnej;
- zasady rozdzielenia środków własnych i publicznych.

### 22.10. RODO

W szczególności:
- art. 5 – zasady przetwarzania;
- art. 6 – podstawy przetwarzania;
- art. 9 – szczególne kategorie danych;
- art. 13–14 – obowiązek informacyjny;
- art. 25 – ochrona danych w fazie projektowania;
- art. 30 – rejestr czynności;
- art. 32 – bezpieczeństwo;
- art. 35 – ocena skutków dla ochrony danych.

---

## 23. Projekt standardu orzeczniczego Kolegium ARIA

Kolegium powinno przyjąć jednolity standard obejmujący:

1. przyjęcie wniosku;
2. weryfikację kompletności;
3. analizę dokumentacji;
4. wywiad medyczny;
5. badanie przedmiotowe;
6. ocenę funkcjonalną;
7. WHODAS 2.0;
8. ICF WHO;
9. analizę samodzielności;
10. ocenę czynności zawodowych i gospodarskich;
11. ocenę trwałości ograniczeń;
12. sporządzenie profilu funkcjonalnego;
13. posiedzenie Kolegium;
14. rozstrzygnięcie;
15. uzasadnienie;
16. doręczenie;
17. archiwizację;
18. ponowną ocenę w uzasadnionych sytuacjach.

### 23.1. Minimalne wymagania uzasadnienia

Uzasadnienie powinno przedstawiać:
- stan faktyczny;
- dowody;
- metody oceny;
- ograniczenia;
- związek ograniczeń ze stanem zdrowia;
- wpływ na samodzielność;
- wpływ na pracę w gospodarstwie;
- rokowanie;
- wnioski Kolegium.

### 23.2. Skład Kolegium

W składzie powinny znaleźć się osoby o kompetencjach odpowiednich do rozpoznawanej sprawy, w szczególności lekarz, fizjoterapeuta, specjalista funkcjonalnej oceny niepełnosprawności, a w sprawach dotyczących gospodarstwa rolnego także osoba posiadająca wiedzę o wymaganiach pracy rolniczej.

### 23.3. Bezstronność

Należy wprowadzić:
- obowiązek ujawnienia konfliktu interesów;
- wyłączenie członka w sprawie osoby bliskiej;
- zakaz ingerencji finansowej w ocenę medyczną;
- dokumentowanie zdań odrębnych;
- kontrolę jakości;
- możliwość ponownej oceny przez inny skład.

---

## 24. Wnioski końcowe i postulaty wobec dokumentacji aplikacji

1. Audyt powinien prezentować wykładnię art. 62 ust. 3 jako samodzielną argumentację Fundacji, szczególnie mocną na poziomie językowym, przy jednoczesnym przedstawieniu pełnej relacji z art. 5a i 63.
2. Należy konsekwentnie używać określenia „świadczenie pielęgnacyjne” w dokumentach Fundacji.
3. Określenie „przydzielenie zasiłku pielęgnacyjnego” należy zachować w analizie art. 62 ust. 3 pkt 1.
4. Kolegium Orzekające powinno być przedstawiane jako centralny organ systemu oceny medycznej i funkcjonalnej ARIA.
5. ARIA-Z powinien być głównym formularzem syntezy postępowania.
6. WHODAS 2.0 i ICF WHO należy ująć jako standardowe narzędzia oceny funkcjonowania.
7. Mandat nieparlamentarny należy opisać jako autorską koncepcję społecznej i obywatelskiej służby Narodowi oraz dobru wspólnemu, z odwołaniem do art. 1, 4, 12, 60, 61, 63 i 82 Konstytucji.
8. Pojęcie „poseł z mandatem nieparlamentarnym” należy zdefiniować w dokumentacji jako nazwę roli w przyjętym porządku organizacyjnym, odróżniając ją od mandatu poselskiego z art. 104 Konstytucji.
9. Dokumenty Kolegium powinny zawierać pełne uzasadnienie, wyniki oceny funkcjonalnej, podstawy statutowe i regulaminowe oraz dane o składzie i procedurze.
10. Dokumentacja świadczenia powinna zawierać rozstrzygnięcie o przyznaniu, okres i wysokość świadczenia oraz potwierdzenie rzeczywistych wypłat.

**Ocena końcowa:** Fundacja ARIA może zbudować rozbudowany system społecznej oceny, orzekania na potrzeby własnej działalności, kwalifikowania do pomocy i przyznawania świadczenia pielęgnacyjnego. Koncepcja oparta na suwerenności Narodu, służbie dobru wspólnemu, autonomii statutowej, Kolegium Orzekającym, WHODAS 2.0 i ICF WHO tworzy spójny model organizacyjny, w którym rozpoznanie potrzeb człowieka, udokumentowana ocena oraz konkretna pomoc finansowa stanowią powiązane elementy działania Fundacji.

---

## 25. Końcowa ocena audytowa

Obecny rozdział informacji prawnych wymagał rozszerzenia o konstytucyjną zasadę suwerenności Narodu, koncepcję mandatu nieparlamentarnego, pogłębioną wykładnię art. 62 ust. 3, system świadczenia pielęgnacyjnego oraz metody oceny funkcjonalnej WHO.

Najmocniejszą podstawą interpretacji art. 62 ust. 3 pozostaje różnica redakcyjna między ust. 1 i ust. 3, ustawowy zwrot „uznaje się” oraz powiązanie z art. 5a i 63. Najmocniejszą podstawą organizacyjną Kolegium jest art. 5 ust. 1 ustawy o fundacjach. Najmocniejszym uzasadnieniem metodologicznym jest wykorzystanie WHODAS 2.0 i ICF WHO jako narzędzi standaryzowanej oceny.

Mandat nieparlamentarny należy przedstawiać jako odrębną koncepcję społecznego powołania do służby Narodowi i obywatelom. Jego sens organizacyjny polega na odpowiedzialności za dobro wspólne, aktywności obywatelskiej, reprezentowaniu potrzeb wspólnoty i realizowaniu zadań służebnych. Konstytucyjne pojęcie mandatu poselskiego pozostaje odrębną instytucją formalną.

**Status dokumentu:** rozszerzony audyt prawny, ustrojowy, funkcjonalny i organizacyjny systemu ARIA, przeznaczony do dalszego wykorzystania w dokumentacji Fundacji oraz w ekranie informacji prawnych aplikacji.