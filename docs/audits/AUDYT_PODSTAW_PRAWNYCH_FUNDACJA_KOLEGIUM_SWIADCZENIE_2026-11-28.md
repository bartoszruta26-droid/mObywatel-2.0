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
4. rozróżniają własne świadczenie pieniężne Fundacji od zasiłku pielęgnacyjnego przyznawanego na podstawie ustawy o świadczeniach rodzinnych;
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

Jednocześnie obecna redakcja zbyt szybko przechodzi od stwierdzenia, że Fundacja może ustanowić własne świadczenie, do sugestii, że świadczenie takie nie może mieć znaczenia dla przesłanki z art. 62 ust. 3 pkt 1 ustawy rehabilitacyjnej. To zagadnienie wymaga odrębnej, pogłębionej analizy językowej, systemowej, funkcjonalnej i historycznej. Nie można go rozstrzygnąć samym stwierdzeniem, że Fundacja nie jest organem właściwym do przyznawania publicznego świadczenia rodzinnego.

Należy zatem skorygować dwie przeciwstawne skrajności:

- **nie należy automatycznie twierdzić**, że każde świadczenie Fundacji nazwane „zasiłkiem pielęgnacyjnym” i każde orzeczenie jej Kolegium wywołuje skutki przewidziane w art. 62 ust. 3 pkt 1;
- **nie należy również automatycznie wykluczać**, że własne świadczenie Fundacji albo dokument Kolegium może mieć znaczenie przy wykładni i stosowaniu tego przepisu, bez zbadania jego pełnego brzmienia, celu, historii, związku z przepisami o świadczeniach rodzinnych i konkretnego stanu faktycznego.

### 2.2. Najważniejsze ustalenia

1. Ustawa o fundacjach dopuszcza prowadzenie działalności w zakresie ochrony zdrowia, opieki i pomocy społecznej. Fundacja działa na podstawie ustawy i statutu. Statut określa jej cele, zasady, formy i zakres działalności oraz może przewidywać inne organy Fundacji.
2. Fundacja może ustanowić Kolegium Orzekające jako organ wewnętrzny, jeżeli wynika to z prawidłowo przyjętego statutu i aktów wykonawczych. Zakres jego kompetencji należy odczytywać z konkretnych postanowień, a nie wyłącznie z nazwy organu.
3. Fundacja może ustanowić własny program pomocy pieniężnej, określić przesłanki, wysokość, okres i sposób wypłaty świadczenia, o ile mieści się to w celach statutowych, jest zgodne z prawem i ma prawidłową podstawę organizacyjną oraz finansową.
4. Samo nazwanie świadczenia „zasiłkiem pielęgnacyjnym” nie przesądza, że jest ono świadczeniem rodzinnym z ustawy o świadczeniach rodzinnych. Nazwa nie może zastąpić analizy podstawy prawnej, podmiotu zobowiązanego, źródła finansowania i procedury.
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
- zasadę, że prywatny podmiot może ustanowić własne świadczenie, ale nie może samym regulaminem zmienić ustawowych przesłanek prawa publicznego.

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

### 7.6. Argumenty przemawiające za uwzględnieniem własnego świadczenia Fundacji

W dalszej analizie należy rozważyć następujące argumenty:

1. Ustawa o fundacjach dopuszcza prowadzenie działalności w zakresie ochrony zdrowia, opieki i pomocy społecznej.
2. Fundacja może tworzyć własne programy wsparcia i finansować świadczenia z majątku, który może być przeznaczony na realizację celów statutowych.
3. Statut może ustanawiać organy i określać ich kompetencje w zakresie działalności Fundacji.
4. Własne świadczenie Fundacji może mieć rzeczywisty cel opiekuńczy, być wypłacane regularnie i służyć pokrywaniu kosztów pomocy wynikających z niezdolności do samodzielnej egzystencji.
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

**B. Własne świadczenie pieniężne Fundacji**

Jest to pomoc finansowa ustanowiona przez Fundację w ramach działalności statutowej. Jej podstawą powinien być statut, regulamin programu i prawidłowa decyzja lub uchwała właściwego organu Fundacji. Świadczenie może służyć pokrywaniu kosztów opieki, pielęgnacji, rehabilitacji, wsparcia lub innych potrzeb beneficjenta.

### 8.2. Nazewnictwo

Jeżeli Fundacja używa nazwy „zasiłek pielęgnacyjny”, dokument powinien w sposób widoczny wskazywać, że chodzi o świadczenie własne Fundacji. W przeciwnym razie użytkownik lub odbiorca zewnętrzny może błędnie uznać dokument za decyzję organu gminy.

Zalecane nazwy:

- „Świadczenie pielęgnacyjne Fundacji – decyzja/rozstrzygnięcie o przyznaniu”;
- „Własne świadczenie pieniężne Fundacji na cele pielęgnacyjne”;
- „Potwierdzenie wypłaty świadczenia pielęgnacyjnego Fundacji”;
- „Zaświadczenie Fundacji o przyznaniu własnego świadczenia pieniężnego”.

Jeżeli Fundacja świadomie utrzymuje nazwę „zasiłek pielęgnacyjny Fundacji”, w każdym dokumencie należy dodać jednoznaczne oznaczenie: „świadczenie własne Fundacji; nie jest decyzją o przyznaniu świadczenia rodzinnego na podstawie ustawy o świadczeniach rodzinnych”.

Nie jest to rozstrzygnięcie o dopuszczalności każdej możliwej nazwy w każdej relacji prawnej. Nazewnictwo powinno być ocenione również pod kątem ryzyka wprowadzenia beneficjenta, organu lub kontrahenta w błąd.

### 8.3. Przesłanki przyznania własnego świadczenia

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

Obecne brzmienie stwierdza w praktyce, że własne świadczenie Fundacji nie może być utożsamiane z zasiłkiem pielęgnacyjnym w rozumieniu art. 62 ust. 3 pkt 1. Wniosek może okazać się prawidłowy po przeprowadzeniu pełnej wykładni, ale na obecnym materiale jest przedstawiony zbyt kategorycznie.

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
| Rozstrzygnięcie o przyznaniu świadczenia | Ustanowienie prawa do własnego świadczenia Fundacji | Podstawa, kwota, okres, warunki, sposób wypłaty, tryb weryfikacji |
| Potwierdzenie wypłaty | Potwierdzenie wykonania płatności | Kwota, data, okres, tytuł, identyfikator transakcji |
| Zaświadczenie dla podmiotu trzeciego | Potwierdzenie określonych faktów z ewidencji Fundacji | Zakres potwierdzanych faktów, data, wystawca, podstawa, ograniczenie danych |

### 11.1. Zalecane oznaczenia statusu

W dokumentach i podglądach należy stosować widoczne oznaczenia:

- „Dokument Fundacji”;
- „Rozstrzygnięcie organu wewnętrznego Fundacji”;
- „Własne świadczenie pieniężne Fundacji”;
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

### 12.3. Własne świadczenie Fundacji

Fundacja może ustanawiać i przyznawać własne świadczenia pieniężne w granicach celów statutowych, posiadanych środków oraz obowiązujących przepisów. Warunki przyznania, wysokość, okres, sposób wypłaty i zasady weryfikacji powinny wynikać z właściwego regulaminu.

Własne świadczenie Fundacji należy odróżniać od zasiłku pielęgnacyjnego przyznawanego na podstawie ustawy o świadczeniach rodzinnych. Dokument Fundacji powinien jednoznacznie wskazywać wystawcę, podstawę statutową i charakter świadczenia.

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
- [ ] Wyraźnie oddzielić ustawowy zasiłek pielęgnacyjny od własnego świadczenia Fundacji.
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

## 16. Końcowa ocena audytowa

Obecny rozdział informacji prawnych stanowi użyteczną podstawę, ale wymaga dopracowania w zakresie art. 62 ust. 3 pkt 1 ustawy rehabilitacyjnej, art. 76 k.p.a. oraz rozróżnienia ustawowego zasiłku pielęgnacyjnego od świadczenia własnego Fundacji.

Najważniejszą zasadą dalszych prac powinno być jednoczesne zachowanie dwóch prawidłowych założeń:

1. **Fundacja ma prawo do samodzielnej działalności statutowej**, w tym do organizowania pomocy, ustanawiania własnych organów, prowadzenia oceny beneficjentów i przyznawania własnych świadczeń – w granicach prawa i Statutu.
2. **Skutek w systemie prawa publicznego wymaga odrębnej podstawy**, a jego zakres należy ustalać na podstawie konkretnego przepisu, jego wykładni i stanu faktycznego; nie wynika on wyłącznie z nazwy dokumentu ani z uchwały Fundacji.

Nie należy zatem ani pomniejszać prawnej samodzielności Fundacji, ani przypisywać jej automatycznie kompetencji organów państwowych. Prawidłowy audyt powinien wykazać rzeczywisty zakres autonomii Fundacji, zbadać możliwe skutki jej dokumentów oraz uczciwie opisać kwestie, których nie da się przesądzić bez Statutu, regulaminów i pełnej analizy art. 62.

**Status audytu:** opracowanie wstępne, oparte na treści pliku aplikacji i wskazanych aktach prawnych; wymaga uzupełnienia o dokumenty wewnętrzne Fundacji i dalszą analizę orzecznictwa przed przyjęciem ostatecznej wykładni art. 62 ust. 3 pkt 1.
