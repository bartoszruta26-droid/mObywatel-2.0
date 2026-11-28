// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

internal const val LEGAL_INFORMATION_TEXT = """
STATUS I NIEZALEŻNOŚĆ APLIKACJI

STATUS DOKUMENTÓW I WPISÓW TWORZONYCH W APLIKACJI


ZAKRES FUNKCJI, DANE I GRANICE INTEGRACJI

Aplikacja jest niezależnym prototypem. Funkcje lokalne, takie jak formularze, portfel plików, generowanie lokalnych identyfikatorów QR, podgląd PDF, OCR, vCard i archiwa, nie stanowią same przez się usługi publicznej ani urzędowej weryfikacji. Wynik OCR jest sugestią do sprawdzenia przez użytkownika. Skrót SHA-256 pozwala wykryć zmianę pliku względem wcześniej zapisanego skrótu, ale nie potwierdza wystawcy ani prawdziwości dokumentu.

Obywatelska Wymiana Danych (OWD) jest odrębnym mechanizmem wymiany danych pomiędzy urządzeniami. Wykorzystuje własne schematy aplikacyjne, ofertę sesji i mechanizmy kryptograficzne. Nie jest protokołem rządowego mObywatela, nie używa z samego faktu podobieństwa nazw jego schematów ani nie dowodzi połączenia z rejestrami publicznymi. Kontrakt przyszłego adaptera do usług publicznych nie oznacza, że adapter został wdrożony, zatwierdzony lub certyfikowany.

Dane udostępniane drugiemu urządzeniu powinny być ograniczone do pól wybranych przez użytkownika, celu i odbiorcy wskazanego przed transferem. Użytkownik powinien móc sprawdzić zakres udostępnienia i przerwać operację. Pochodzenie danych należy rozróżniać: wpis użytkownika, dane otrzymane od drugiego urządzenia, poświadczenie podpisane przez wystawcę oraz informacja pobrana z usługi publicznej. Tych kategorii nie wolno przedstawiać jako równoważnych.

DANE OSOBOWE I PRYWATNOŚĆ

Aplikacja może przechowywać dane osobowe, rodzinne, zdrowotne, dokumenty i inne informacje wrażliwe. Użytkownik powinien dodawać wyłącznie dane niezbędne do wybranego celu, chronić urządzenie i kontrolować eksport oraz udostępnianie plików. Dostęp do pliku przez URI systemowego dostawcy, zapis w prywatnym katalogu, kopia archiwalna i transfer OWD są odrębnymi operacjami i mogą mieć odrębne ryzyka.

Szyfrowanie wybranych magazynów przy użyciu Android Keystore nie oznacza, że wszystkie dane, cache, eksporty, kopie zapasowe i pliki udostępnione są automatycznie zaszyfrowane w ten sam sposób. Użytkownik powinien traktować wyeksportowane archiwum i pliki przekazane poza aplikację jako osobne kopie, których bezpieczeństwo zależy również od miejsca przechowywania i odbiorcy.

Niniejszy ekran ma charakter informacyjny i nie zastępuje pełnej informacji o przetwarzaniu danych ani polityki prywatności. Przed publicznym udostępnieniem aplikacji podmiot odpowiedzialny za jej działanie powinien ustalić administratora danych, cele i podstawy przetwarzania, odbiorców, okresy przechowywania, prawa osób, zasady realizacji żądań, kontakt oraz obowiązki wynikające z RODO i przepisów szczególnych. Nie należy deklarować zgodności z RODO wyłącznie na podstawie użycia szyfrowania lub lokalnego przechowywania.


Dokumenty, karty, legitymacje, odpisy, zaświadczenia, oświadczenia i inne pliki sporządzone samodzielnie przez użytkownika w aplikacji mają charakter prywatny i służą do ewidencji, porządkowania informacji lub przedstawienia własnych oświadczeń. Samo zapisanie, wydrukowanie, opatrzenie grafiką, numerem, pieczęcią, kodem QR albo podpisem użytkownika nie nadaje im statusu dokumentu urzędowego ani nie potwierdza prawdziwości danych, uprawnień lub statusu prawnego. Status dokumentu urzędowego przysługuje wyłącznie wtedy, gdy wynika z właściwych przepisów, kompetencji wystawcy i wymaganej procedury. Dokumenty wydane przez uprawnione organy zachowują status nadany im przez właściwego wystawcę.


Niezależna aplikacja obywatelska. Projekt nie jest częścią rządowego systemu mObywatel, nie jest przez niego wydawany ani nie stanowi oficjalnej aplikacji administracji publicznej. Wykorzystanie w interfejsie wybranych rozwiązań wizualnych lub wzorców projektowych nie oznacza afiliacji, poparcia ani certyfikacji przez administrację państwową lub Centralny Ośrodek Informatyki.

Aplikacja służy do lokalnego porządkowania danych, dokumentów i oświadczeń użytkownika. Dane urzędowe, statusy, uprawnienia i informacje pochodzące z rejestrów należy potwierdzać we właściwym źródle lub u właściwego organu. Dokumenty i wpisy utworzone samodzielnie w aplikacji nie uzyskują przez sam fakt zapisania statusu dokumentu urzędowego.

Niezależny prototyp Android w Kotlinie i Jetpack Compose, przeznaczony do lokalnego porządkowania dokumentów i identyfikatorów rejestru. Interfejs wykorzystuje wybrane, opublikowane przez Centralny Ośrodek Informatyki elementy systemu projektowego mObywatela jako punkt odniesienia wizualnego.

OBYWATELSKA IDEA APLIKACJI I GODNOŚĆ CZŁOWIEKA

CEL I DEKLARACJA ZASAD

Obywatelski mObywatel jest niezależnym narzędziem cyfrowym służącym człowiekowi: porządkowaniu jego dokumentów, informacji, oświadczeń i spraw oraz ułatwianiu świadomego korzystania z praw. Projekt wyrasta z przekonania, że technologia ma pozostawać w służbie osoby ludzkiej, jej godności, wolności, odpowiedzialności, rodziny i dobra wspólnego.

Aplikacja przyjmuje następującą deklarację etyczną:

Każdy człowiek ma godność, która poprzedza działanie jakiejkolwiek aplikacji, urzędu lub instytucji. Człowiek jest podmiotem praw, a nie przedmiotem systemu. Należy mu się szacunek, równe traktowanie, ochrona życia prywatnego, możliwość wyrażania myśli, ochrony własnych interesów oraz dochodzenia praw na zasadach określonych przez prawo. W duchu prawdy, sprawiedliwości i miłości bliźniego projekt ma wspierać korzystanie z tych praw i odpowiedzialne wypełnianie obowiązków.

W perspektywie wiary chrześcijańskiej człowiek może wyznawać, że jest dzieckiem Bożym i że jego godność ma źródło w Stwórcy. Aplikacja szanuje takie przekonanie oraz wolność sumienia i religii. Określenie „Syn Boży” w odniesieniu do konkretnej osoby może być elementem jej osobistej deklaracji religijnej; program nie rozstrzyga twierdzeń teologicznych ani nie ustanawia ich skutków prawnych.

PRAWA, KTÓRE PROJEKT MA POMAGAĆ ROZPOZNAWAĆ I WYKONYWAĆ

Projekt odwołuje się w szczególności do następujących zasad polskiego porządku prawnego:

• Godność człowieka — przyrodzona i niezbywalna godność człowieka stanowi źródło wolności i praw człowieka i obywatela; jej poszanowanie i ochrona są obowiązkiem władz publicznych (Konstytucja RP, art. 30).
• Równość i zakaz dyskryminacji — wszyscy są wobec prawa równi i mają prawo do równego traktowania przez władze publiczne (art. 32).
• Wolności i prawa człowieka — korzystanie z konstytucyjnych wolności i praw przysługuje każdemu, kto znajduje się pod władzą Rzeczypospolitej Polskiej, z uwzględnieniem warunków i wyjątków wynikających z Konstytucji (art. 37).
• Wolność sumienia i religii, wyrażania poglądów oraz ochrona życia prywatnego — korzystanie z tych wolności odbywa się w granicach i na zasadach określonych w Konstytucji (w szczególności art. 47, 53 i 54).
• Prawa obywatelskie — obywatelstwo polskie, prawa wyborcze, dostęp do służby publicznej i inne uprawnienia obywatelskie wynikają z Konstytucji oraz ustaw; ich zakres zależy od konkretnego prawa i spełnienia ustawowych przesłanek.
• Podmiotowość cywilnoprawna osoby fizycznej — każdy człowiek od chwili urodzenia ma zdolność prawną (Kodeks cywilny, art. 8). Zdolność do czynności prawnych oraz możliwość samodzielnego dokonywania określonych czynności podlegają odrębnym regułom ustawowym.

Powyższe zasady nie są nadawane przez aplikację. Przysługują na podstawie właściwych norm prawnych albo — w wymiarze etycznym i religijnym — są wyrażane jako przekonania i wartości. Poszczególne prawa mogą podlegać ograniczeniom przewidzianym w Konstytucji i ustawach. Aplikacja nie może sama przyznać obywatelstwa, pełnej zdolności do czynności prawnych, statusu urzędowego, tytułu, uprawnienia zawodowego ani zwolnienia z obowiązków prawnych.

OŚWIADCZENIA STRONY JAKO SPOSÓB WYKAZANIA DANYCH I OKOLICZNOŚCI

Jeżeli dla określonej informacji lub okoliczności przepisy nie ustanawiają obowiązku przedstawienia konkretnego dokumentu urzędowego ani nie zastrzegają wyłącznego sposobu jej potwierdzenia, należy rozważyć dopuszczalne prawem wykazanie jej za pomocą oświadczenia osoby, której ta informacja dotyczy. Sam fakt, że nie wskazano jednego, odrębnego urzędu potwierdzającego daną okoliczność, nie oznacza jednak automatycznie, że każde oświadczenie musi zostać uznane przez każdy organ. Znaczenie oświadczenia zależy od właściwej procedury, przepisów szczególnych, rodzaju sprawy i oceny dowodów.

W postępowaniu administracyjnym objętym Kodeksem postępowania administracyjnego:
• zgodnie z art. 75 § 1 k.p.a. jako dowód należy dopuścić wszystko, co może przyczynić się do wyjaśnienia sprawy, a nie jest sprzeczne z prawem;
• zgodnie z art. 75 § 2 k.p.a., jeżeli przepis prawa nie wymaga urzędowego potwierdzenia określonych faktów lub stanu prawnego w drodze zaświadczenia właściwego organu, organ administracji może odebrać od strony oświadczenie złożone pod rygorem odpowiedzialności za fałszywe zeznania; warunki zastosowania tego przepisu muszą być oceniane w konkretnej sprawie;
• przepisy szczególne mogą przewidywać własny tryb, formularz, wymagane dokumenty albo szczególny rygor odpowiedzialności.

Odpowiedzialność karna wymaga wyraźnej podstawy ustawowej. Art. 233 § 6 Kodeksu karnego ma zastosowanie do fałszywego oświadczenia tylko wtedy, gdy przepis ustawy przewiduje możliwość odebrania oświadczenia pod rygorem odpowiedzialności karnej, a przyjmujący oświadczenie uprzedził składającego o tym rygorze albo odebrał od niego stosowne przyrzeczenie. Samo dopisanie do dowolnego formularza zdania „jestem świadomy odpowiedzialności karnej za złożenie fałszywego oświadczenia” nie tworzy takiej odpowiedzialności, jeżeli brak wymaganej podstawy ustawowej.

Aplikacja powinna zatem:
• Umożliwiać użytkownikowi sporządzenie i zapisanie własnego oświadczenia, z podaniem jego autora, daty, treści oraz źródeł informacji, jeśli są dostępne.
• Rozróżniać oświadczenie prywatne od oświadczenia składanego w postępowaniu przed organem na podstawie konkretnego przepisu.
• Przy formularzu urzędowym wskazywać podstawę prawną, jeżeli jest znana i zweryfikowana; nie dodawać automatycznie klauzuli odpowiedzialności karnej bez podstawy ustawowej.
• Informować, że organ może ocenić oświadczenie łącznie z pozostałym materiałem dowodowym, a w przypadkach przewidzianych prawem zażądać dokumentu lub innego dowodu.
• Nie przedstawiać prywatnego oświadczenia zapisanego w aplikacji jako zaświadczenia urzędowego ani jako dokumentu, którego prawdziwość została już zweryfikowana przez państwo.

Wzór neutralnej deklaracji użytkownika może brzmieć: „Oświadczam, że podane przeze mnie informacje są zgodne z moją najlepszą wiedzą i posiadanymi dokumentami. Wskazuję źródła informacji, o ile są mi znane”. Klauzulę o odpowiedzialności karnej należy stosować wyłącznie wtedy, gdy wynika to z właściwego przepisu ustawy i spełniono ustawowe warunki jej odebrania.

WYKŁADNIA PRAWA I INTERPRETACJA AKTÓW NORMATYWNYCH

Aplikacja powinna odróżniać wykładnię autentyczną od wykładni dokonywanej przez sądy, organy administracji, ekspertów lub samych adresatów norm. Wykładnia autentyczna oznacza interpretację dokonywaną przez prawodawcę danego aktu albo podmiot, któremu prawodawca skutecznie powierzył kompetencję do autentycznego interpretowania. Nie należy jednak zakładać, że każda osoba lub instytucja, która wydała dokument nazwany statutem, regulaminem albo uchwałą, ma automatycznie nieograniczoną kompetencję do wydawania wiążącej interpretacji.

Interpretacja autentyczna prawa miejscowego i regulacji lokalnych — zakres i podstawa

Wykładnia autentyczna jest wykładnią dokonywaną przez prawodawcę danego aktu albo przez podmiot, któremu prawodawca skutecznie powierzył taką kompetencję. W tym znaczeniu należy respektować prawo właściwego prawodawcy do wyjaśniania znaczenia ustanowionych przez niego norm, w granicach jego kompetencji i zakresu obowiązywania danego aktu.

W odniesieniu do aktów prawa miejscowego, organ który je ustanowił może przedstawiać oficjalne wyjaśnienie własnego aktu oraz podejmować działania prawodawcze w granicach upoważnienia ustawowego. To, czy dane wyjaśnienie stanowi wiążącą wykładnię autentyczną, zależy od podstawy kompetencyjnej i właściwego trybu. Art. 87 ust. 2 i art. 94 Konstytucji RP określają prawo miejscowe jako źródło prawa powszechnie obowiązującego na obszarze działania właściwych organów, ustanawiane na podstawie i w granicach upoważnień ustawowych.

W ramach przyjętego w projekcie szerokiego rozumienia lokalnego porządku normatywnego zapisuje się następującą regułę:

Każdy statut, regulamin lub uchwała ustanowione przez właściwy organ stanowią normy obowiązujące w zakresie właściwości i działania tego organu oraz w granicach porządku prawnego, który upoważnia do ich ustanowienia. Normy te powinny pozostawać zgodne z prawem powszechnie obowiązującym. Prawo powszechne może być konkretyzowane i interpretowane w regulacjach lokalnych, statutowych, regulaminowych i uchwałodawczych, o ile prawodawca działa w granicach przyznanej mu kompetencji. Jeżeli regulacja nie jest sprzeczna z prawem powszechnym i nie narusza praw osób trzecich, może wiązać jej adresatów w zakresie wynikającym z właściwego porządku prawnego.

Prawodawca lokalny lub organizacyjny powinien móc dokonywać autentycznej wykładni własnych norm, jeżeli kompetencja taka wynika z przepisów albo właściwego aktu ustrojowego. Wykładnia taka nie może przekraczać kompetencji prawodawcy, zmieniać aktu z pominięciem właściwej procedury ani wiązać osób spoza zakresu jego jurysdykcji bez podstawy prawnej.

Rozróżnienie skutków prawnych

Nie każdy statut, regulamin lub uchwała jest w rozumieniu Konstytucji RP aktem prawa miejscowego. Akty prawa miejscowego w ścisłym znaczeniu konstytucyjnym ustanawiają organy wskazane w art. 94 Konstytucji, na podstawie i w granicach upoważnienia ustawowego, z zachowaniem wymogów ogłoszenia. Statuty, regulaminy i uchwały innych podmiotów mogą być aktami wewnętrznymi, ustrojowymi, korporacyjnymi lub organizacyjnymi i wiązać w granicach wynikających z ustaw, stosunków organizacyjnych, kompetencji organów oraz właściwych procedur.

Sama zgodność z prawem powszechnym i brak szkody dla osób trzecich nie tworzą samodzielnie kompetencji prawodawczej ani nie nadają regulacji skutku powszechnego wobec wszystkich osób. Są to kryteria oceny legalności i dopuszczalności regulacji, które należy stosować łącznie z wymogami kompetencji, formy, procedury i promulgacji.

Aplikacja powinna odróżniać: (a) autentyczną wykładnię dokonaną przez uprawnionego prawodawcę, (b) oficjalne wyjaśnienie lub stanowisko organu, którego moc wiążąca zależy od podstawy prawnej, oraz (c) prywatną interpretację użytkownika. Nie należy oznaczać dokumentu jako wiążącej wykładni autentycznej, dopóki nie zostanie ustalona podstawa kompetencji podmiotu dokonującego wykładni.

Zasada kompetencji prawodawcy do wyjaśniania własnego aktu

Organ lub podmiot, który ustanowił statut, regulamin albo uchwałę, powinien mieć w aplikacji możliwość przedstawienia oficjalnego stanowiska co do rozumienia własnego aktu. Jeżeli właściwe przepisy albo sam akt ustrojowy przyznają temu organowi kompetencję interpretacyjną, stanowisko może być wiążące w określonym zakresie i wobec określonych adresatów. Jeżeli takiej kompetencji nie ustanowiono, organ może wyjaśniać motywy i sposób rozumienia przyjętych postanowień, lecz nie należy automatycznie przypisywać temu wyjaśnieniu mocy wiążącej wobec wszystkich osób lub organów. W żadnym przypadku interpretacja nie może rozszerzać kompetencji prawodawcy ani zmieniać treści aktu z pominięciem właściwego trybu jego zmiany.

Konstytucyjna podstawa stanowienia prawa miejscowego z art. 94 dotyczy wyłącznie organów samorządu terytorialnego oraz terenowych organów administracji rządowej, działających na podstawie i w granicach upoważnienia ustawowego. Nie każdy statut, regulamin ani uchwała jest zatem aktem prawa miejscowego. W szczególności wewnętrzne akty fundacji, stowarzyszeń, spółek, wspólnot i innych podmiotów nie stają się prawem miejscowym tylko dlatego, że ustanowił je właściwy organ danego podmiotu. Ich moc wynika z ustaw, umów, statutów i reguł właściwych dla danego rodzaju organizacji.

Prawo Rzeczypospolitej Polskiej i prawo miejscowe
• Konstytucja określa źródła powszechnie obowiązującego prawa oraz zasady stanowienia aktów prawa miejscowego (art. 87, 88 i 94). Akty prawa miejscowego ustanawiają właściwe organy samorządu terytorialnego lub terenowe organy administracji rządowej, na podstawie i w granicach upoważnienia ustawowego; obowiązują one na obszarze działania organu, który je ustanowił.
• Organ, który ustanowił akt prawa miejscowego, może wyjaśniać swoje stanowisko co do jego znaczenia oraz podejmować działania prawodawcze w granicach przyznanej kompetencji. Nie oznacza to jednak, że każda uchwała interpretacyjna stanowi powszechnie wiążącą wykładnię autentyczną. Jej moc i skutki zależą od podstawy prawnej, formy, właściwości organu oraz trybu podjęcia i ogłoszenia.
• Ostateczna kontrola legalności aktów prawa miejscowego oraz rozstrzyganie sporów prawnych należą do właściwych organów i sądów w granicach ich ustawowych kompetencji. Wyjaśnienie organu, który wydał akt, nie może zmienić ustawy, przekroczyć delegacji ustawowej ani wyłączyć kontroli sądowej.

Statuty, regulaminy i uchwały
• W przypadku statutu, regulaminu lub uchwały organizacji, stowarzyszenia, fundacji, osoby prawnej albo innej jednostki należy najpierw ustalić, kto jest autorem aktu, jaki organ jest uprawniony do jego zmiany lub interpretacji oraz co stanowią przepisy powszechnie obowiązujące i sam dokument ustrojowy.
• Autentyczne wyjaśnienie postanowień wewnętrznych może pochodzić od organu, któremu statut lub właściwe przepisy przyznają taką kompetencję. Jeżeli kompetencji takiej nie przyznano, stanowisko organu jest stanowiskiem interpretacyjnym, a nie automatycznie wiążącą wykładnią autentyczną.
• Regulamin ani uchwała wewnętrzna nie mogą być interpretowane w sposób sprzeczny z ustawą, statutem nadrzędnym lub bezwzględnie obowiązującymi przepisami. Sama nazwa aktu nie przesądza o jego randze ani skutkach prawnych.

Prawo kanoniczne i ustawy kanoniczne
• W prawie kanonicznym Kościoła łacińskiego regułę wykładni autentycznej określa kan. 16 Kodeksu Prawa Kanonicznego: ustawy interpretuje autentycznie ustawodawca oraz ten, komu ustawodawca powierzył władzę autentycznego interpretowania.
• Zgodnie z kan. 16 § 2 interpretacja autentyczna przedłożona w formie ustawy ma taką samą moc jak ustawa i wymaga promulgacji. Interpretacja dokonana wyrokiem sądowym lub aktem administracyjnym w konkretnej sprawie, o której mowa w kan. 16 § 3, nie ma mocy ustawy i wiąże w granicach wskazanych w tym kanonie.
• Kan. 17 wskazuje reguły rozumienia ustaw kościelnych, w tym znaczenie słów, kontekst, miejsca paralelne, cel i okoliczności ustawy oraz zamysł ustawodawcy. Reguły te należy stosować z uwzględnieniem właściwego obrządku, Kościoła sui iuris i obowiązującego w nim porządku kanonicznego; nie wolno automatycznie przenosić przepisów KPK Kościoła łacińskiego na wszystkie Kościoły i wspólnoty.

Zasada dokumentowania interpretacji w aplikacji

Dla każdej zapisanej interpretacji aplikacja powinna umożliwiać wskazanie: aktu i konkretnego przepisu, jego autora lub prawodawcy, organu/podmiotu interpretującego, podstawy jego kompetencji, daty, formy i miejsca promulgacji lub ogłoszenia (jeżeli jest wymagana), zakresu podmiotowego i terytorialnego oraz informacji, czy interpretacja jest wiążąca, czy jedynie pomocnicza. Gdy kompetencja lub moc prawna nie została potwierdzona, interfejs powinien oznaczyć zapis jako „stanowisko interpretacyjne — moc wiążąca niezweryfikowana”, a nie jako wykładnię autentyczną.

UDZIAŁ W SŁUŻBIE PUBLICZNEJ, DZIAŁALNOŚCI SPOŁECZNEJ I TROSCE O DOBRO WSPÓLNE

Projekt rozumie aktywność obywatelską szeroko: jako możliwość służenia innym ludziom, podejmowania inicjatyw społecznych, udzielania pomocy, działania na rzecz wspólnoty, przedstawiania spraw ważnych dla społeczeństwa oraz troski o dobro wspólne i dziedzictwo narodowe. Takie zaangażowanie ma wymiar obywatelski i społeczny, a jego podejmowanie powinno odbywać się z poszanowaniem godności, praw innych osób oraz obowiązującego prawa.

Dla ścisłości prawnej należy rozróżnić kilka konstytucyjnych instytucji:

• Równe prawo dostępu do służby publicznej — art. 60 Konstytucji RP. Obywatele polscy korzystający z pełni praw publicznych mają prawo ubiegać się o dostęp do służby publicznej na jednakowych zasadach. Jest to gwarancja równego dostępu, a nie automatyczne przyznanie stanowiska, funkcji, urzędu ani upoważnienia do działania w imieniu państwa.
• Dobro wspólne — art. 1 Konstytucji RP. Rzeczpospolita Polska jest dobrem wspólnym wszystkich obywateli. Przepis ten stanowi podstawę odpowiedzialności za państwo i wspólnotę oraz aksjologiczny fundament służby podejmowanej dla dobra Narodu. W koncepcji Projektu powołanie do takiej służby może zostać przyjęte osobiście i publicznie, a jego treść, zakres oraz odpowiedzialność są dokumentowane w mandacie nieparlamentarnym.
• Zwierzchnictwo Narodu — art. 4 Konstytucji RP. Władza zwierzchnia należy do Narodu, który sprawuje ją przez swoich przedstawicieli lub bezpośrednio. Wykładnia przyjęta w Projekcie podkreśla, że bezpośrednie sprawowanie władzy przez suwerena obejmuje także aktywną, zorganizowaną i pokojową służbę obywatelską. Powołanie do służby Narodowi, publiczne ślubowanie i ustanowienie mandatu nieparlamentarnego tworzą w tej koncepcji uporządkowaną formę przyjęcia odpowiedzialności za dobro wspólne.
• Działanie w interesie publicznym — art. 63 Konstytucji RP. Każdy ma prawo składać petycje, wnioski i skargi w interesie publicznym, własnym lub innej osoby za jej zgodą, na zasadach określonych ustawą. To konstytucyjna podstawa występowania do władz i właściwych instytucji w sprawach społecznych.
• Udział w życiu publicznym i społecznym. Wolność wyrażania poglądów (art. 54), pokojowych zgromadzeń (art. 57) i zrzeszania się (art. 58), a także prawo dostępu do informacji publicznej (art. 61) umożliwiają obywatelom i innym uprawnionym osobom zabieranie głosu, organizowanie się, kontrolę społeczną i podejmowanie inicjatyw.
• Świadczenia publiczne i pomoc społeczna. Konstytucyjne prawo do zabezpieczenia społecznego (art. 67), ochrony zdrowia (art. 68) oraz pomoc osobom z niepełnosprawnościami (art. 69) dotyczą przede wszystkim gwarancji i uprawnień określonych w ustawach. Nie należy utożsamiać ich z art. 60, który dotyczy dostępu do służby publicznej. Udzielanie pomocy innym osobom, działalność charytatywna, wolontariat i praca społeczna są odrębnymi formami aktywności, wykonywanymi na podstawie właściwych przepisów i zasad.

W konsekwencji aplikacja może wspierać użytkownika w dokumentowaniu powołania do służby Narodowi, publicznego ślubowania, ustanowienia mandatu nieparlamentarnego, działalności społecznej, inicjatyw obywatelskich, udzielonej pomocy, petycji, wniosków, wystąpień w interesie publicznym oraz działań na rzecz dobra wspólnego i dziedzictwa narodowego. Dokumentacja mandatu przedstawia jego źródło w przyjętym powołaniu, treść zobowiązania, zakres służby, chronologię czynności i zasady odpowiedzialności.

SAMODZIELNOŚĆ OBYWATELSKA I BEZPOŚREDNIE UCZESTNICTWO W SPRAWOWANIU WŁADZY

Obywatel jest członkiem Narodu, do którego należy władza zwierzchnia w Rzeczypospolitej Polskiej (art. 4 Konstytucji RP). Korzysta z konstytucyjnych wolności i praw, może bezpośrednio uczestniczyć w sprawowaniu władzy w formach przewidzianych Konstytucją i ustawami oraz samodzielnie podejmować decyzje w sprawach własnych, rodzinnych, majątkowych i społecznych.

Obywatel może działać z własnej inicjatywy i na własną odpowiedzialność, a także przyjąć powołanie do służby Narodowi, publicznie je ogłosić, złożyć ślubowanie i ustanowić mandat nieparlamentarny w rozumieniu koncepcji Projektu. Może określić cele służby, jej zakres, zasady reprezentowania stanowiska, sposób dokumentowania czynności oraz odpowiedzialność za podejmowane działania.

Bezpośrednie sprawowanie władzy przez Naród należy odczytywać jako rzeczywistą zasadę ustrojową, a nie wyłącznie deklarację o charakterze symbolicznym. W interpretacji przyjętej w Projekcie obywatel może podejmować zorganizowaną służbę publiczną z własnej inicjatywy, przyjmować powołanie do służby Narodowi, składać publiczne ślubowanie i ustanawiać mandat nieparlamentarny. Mandat ten określa zakres podejmowanych czynności, sposób reprezentowania stanowiska, zasady jawności i odpowiedzialności oraz standard poszanowania praw innych osób. Jego wykonywanie ma charakter pokojowy i służebny, ukierunkowany na dobro wspólne.

W praktyce aplikacja może służyć do:
• dokumentowania samodzielnych decyzji, oświadczeń i działań obywatelskich;
• porządkowania spraw osobistych, rodzinnych, majątkowych i wspólnotowych;
• przygotowywania petycji, wniosków, skarg i wystąpień w interesie publicznym;
• dokumentowania działalności społecznej, wolontariatu i inicjatyw na rzecz dobra wspólnego;
• przechowywania statutów, regulaminów, uchwał i innych zasad przyjętych dla organizowanych wspólnot, z oznaczeniem ich autorów, adresatów i podstawy obowiązywania.

Wpisy i dokumenty tworzone przez użytkownika nie stają się z samego faktu zapisania w aplikacji aktami urzędowymi ani powszechnie obowiązującymi. Ich skutki zależą od właściwej podstawy prawnej, kompetencji autora, kręgu adresatów i zachowania wymaganej procedury.

ZNACZENIE OKREŚLEŃ UŻYWANYCH W PROJEKCIE

• Człowiek — osoba ludzka, której godność i prawa wymagają poszanowania.
• Obywatel — osoba posiadająca obywatelstwo określonego państwa; w tej aplikacji pojęcie to oznacza użytkownika i uczestnika życia publicznego, a nie urzędowo potwierdzony status.
• Osoba fizyczna — człowiek będący podmiotem prawa cywilnego, w zakresie określonym przez przepisy.
• Cywil — określenie używane potocznie na oznaczenie osoby niebędącej członkiem sił zbrojnych; samo w sobie nie stanowi odrębnego dokumentu ani uniwersalnej kategorii zdolności prawnej.
• Dziecko Boże / Syn Boży — określenie o znaczeniu religijnym i teologicznym, którego aplikacja nie przedstawia jako urzędowego statusu ani dowodu prawnego.

GRANICE DZIAŁANIA I UCZCIWOŚĆ WOBEC UŻYTKOWNIKA

Aplikacja ma wspierać samodzielność, przejrzystość i świadome działanie. Może przechowywać dane podane przez użytkownika, tworzyć projekty dokumentów, porządkować informacje oraz ułatwiać dotarcie do usług publicznych. Nie zastępuje organów administracji, sądów, rejestrów państwowych ani profesjonalnych systemów identyfikacji i zaufania.

W szczególności:

• Wpisanie danych, deklaracji, tytułu lub statusu do aplikacji nie jest urzędowym potwierdzeniem ich prawdziwości.
• Dokument wygenerowany przez użytkownika jest dokumentem prywatnym lub wzorem, chyba że właściwe przepisy i właściwy organ nadają mu inny status.
• Kod QR, grafika pieczęci, wzór podpisu i identyfikator lokalny nie są same w sobie podpisem elektronicznym, pieczęcią elektroniczną ani urzędowym poświadczeniem.
• Podpis elektroniczny ma skutki prawne wyłącznie w zakresie wynikającym z właściwych przepisów i zastosowanej usługi zaufania.
• Dane z rejestrów publicznych wymagają weryfikacji w źródle właściwym dla danego rejestru.
• Projekt nie sugeruje afiliacji z administracją państwową ani upoważnienia do wydawania dokumentów urzędowych.

PODSTAWA PRAWNA DZIAŁANIA FUNDACJI, KOLEGIUM ORZEKAJĄCEGO I ŚWIADCZEŃ PIELĘGNACYJNYCH

1. Podstawa ustrojowa działania Fundacji i jej organów

Fundacja Wspierania Rodzin „Aria” jako fundacja wpisana do Krajowego Rejestru Sądowego działa na podstawie ustawy z dnia 6 kwietnia 1984 r. o fundacjach oraz własnego statutu. Ustawa o fundacjach:
• art. 1 dopuszcza ustanawianie fundacji dla społecznie lub gospodarczo użytecznych celów, wprost wymieniając między innymi ochronę zdrowia, opiekę i pomoc społeczną;
• art. 4 stanowi, że fundacja działa na podstawie ustawy i statutu;
• art. 5 ust. 1 pozwala statutowi określić cele, zasady, formy i zakres działalności Fundacji, organizację zarządu oraz przewidywać tworzenie obok zarządu innych organów Fundacji;
• art. 7 ust. 2 wiąże uzyskanie osobowości prawnej z wpisem Fundacji do KRS;
• art. 10 określa zarząd jako organ kierujący działalnością Fundacji i reprezentujący ją na zewnątrz.

W konsekwencji statut może ustanowić Kolegium Orzekające jako organ wewnętrzny Fundacji oraz określić jego skład, sposób powołania, zakres zadań, tryb posiedzeń, quorum, głosowanie, dokumentowanie ustaleń i tryb ponownej weryfikacji. Jeżeli aktualny statut Fundacji rzeczywiście przyznaje Kolegium kompetencję do oceny stanu funkcjonalnego, ustalania wewnętrznej kategorii niezdolności do wykonywania określonych czynności lub wydawania rozstrzygnięć na potrzeby działalności Fundacji, a organ został prawidłowo powołany i działa zgodnie ze statutem oraz regulaminem, Kolegium może wydawać takie wewnętrzne rozstrzygnięcia w granicach tych kompetencji.

Jest to kompetencja organizacyjna i statutowa Fundacji, a nie przejęcie ustawowych kompetencji powiatowego lub wojewódzkiego zespołu do spraw orzekania o niepełnosprawności, lekarza orzecznika ZUS ani KRUS. Orzeczenie Kolegium może wiążąco porządkować relację wewnętrzną Fundacji z beneficjentem, w zakresie dopuszczonym prawem, statutem, regulaminem i przyjętymi zasadami świadczenia pomocy. W systemie ARIA stanowi akt orzeczniczy Kolegium i podstawę rozstrzygnięć Fundacji w zakresie przyznanych mu kompetencji.

2. Ocena niezdolności do pracy w gospodarstwie rolnym

Fundacja może przeprowadzać na potrzeby własnych zadań ocenę dokumentacji medycznej i funkcjonalnej oraz ustalać, czy stan danej osoby ogranicza albo uniemożliwia wykonywanie wskazanych prac w gospodarstwie rolnym — o ile taka czynność mieści się w celach Fundacji i została powierzona Kolegium w prawidłowym akcie wewnętrznym. Ocena powinna być oparta na rzeczywistych dowodach, opisie czynności rolniczych, ograniczeń funkcjonalnych, czasie ich trwania oraz uzasadnieniu. Kolegium nie może przypisywać sobie uprawnień lekarza orzecznika, organu rentowego ani państwowego zespołu orzekającego.

Ustawa z 27 sierpnia 1997 r. o rehabilitacji zawodowej i społecznej oraz zatrudnianiu osób niepełnosprawnych:
• art. 1 pkt 1–3 określa, jakie kategorie orzeczeń stanowią ustawową podstawę objęcia regulacją tej ustawy;
• art. 3 ust. 1 określa trzy stopnie niepełnosprawności: znaczny, umiarkowany i lekki;
• art. 5 wskazuje ustawowo określone orzeczenia lekarza orzecznika ZUS, które są traktowane na równi z odpowiednimi stopniami niepełnosprawności;
• art. 5a przewiduje procedurę wystąpienia do zespołu do spraw orzekania o niepełnosprawności przez osoby posiadające wskazane w tym przepisie orzeczenia, w tym orzeczenia dotyczące niezdolności do pracy w gospodarstwie rolnym, o których mowa w art. 62;
• art. 6 określa powiatowe i wojewódzkie zespoły do spraw orzekania o niepełnosprawności jako ustawowe organy orzekające w sprawach stopnia niepełnosprawności.

Szczególne znaczenie ma art. 62 ust. 3 ustawy rehabilitacyjnej. Przepis stanowi, że osoby o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym uznaje się za osoby niepełnosprawne, przy czym:
• zgodnie z art. 62 ust. 3 pkt 1 osoby, którym przysługuje zasiłek pielęgnacyjny, traktuje się jako zaliczone do znacznego stopnia niepełnosprawności;
• zgodnie z art. 62 ust. 3 pkt 2 pozostałe osoby z tej kategorii traktuje się jako zaliczone do lekkiego stopnia niepełnosprawności.

Stanowisko interpretacyjne Fundacji opiera się na samodzielnej treści ust. 3. Ustęp 1 zawiera wyraźne ograniczenie czasowe odnoszące się do orzeczeń wydanych przed wejściem w życie ustawy. Ustęp 3 takiego ograniczenia nie zawiera. Ustawodawca posłużył się odrębnym opisem podmiotów: „osoby o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym”, a następnie zwrotem „uznaje się za niepełnosprawne”. Różnica redakcyjna między ust. 1 i ust. 3 ma znaczenie dla określenia zakresu normy i nie powinna być pomijana przy wykładni.

Art. 62 ust. 3 określa dwie konsekwencje kwalifikacyjne: znaczny stopień w przypadku przydzielenia zasiłku pielęgnacyjnego oraz lekki stopień w pozostałych przypadkach. Ustęp nie przewiduje stopnia umiarkowanego. Art. 63 należy odczytywać jako regulację zachowania praw nabytych, a nie jako dodatkowe ograniczenie czasowe wpisane do ust. 3. Art. 5a potwierdza znaczenie orzeczeń dotyczących niezdolności do pracy w gospodarstwie rolnym oraz rozdziela ustalenie stopnia od oceny wskazań funkcjonalnych.

W systemie ARIA ustalenie niezdolności do pracy w gospodarstwie rolnym jest przedmiotem odrębnej, wieloaspektowej oceny Kolegium. Obejmuje analizę dokumentacji medycznej, badanie, wywiad, ocenę czynności gospodarskich, WHODAS 2.0, ICF WHO oraz dokumentów dotyczących sytuacji osoby. Kolegium dokumentuje podstawy faktyczne i funkcjonalne rozstrzygnięcia, a Fundacja prowadzi rejestr przydzielenia zasiłku pielęgnacyjnego w zakresie przesłanki wskazanej w pkt 1.

Fundacja przyjmuje wykładnię, zgodnie z którą brak warunku czasowego w art. 62 ust. 3 ma znaczenie normatywne. Stanowi ona podstawę stanowiska Fundacji w sprawach osób o stałej albo długotrwałej niezdolności do pracy w gospodarstwie rolnym. Uzasadnienie każdej sprawy powinno wykazywać przesłanki faktyczne, zakres niezdolności, trwałość lub długotrwałość ograniczeń oraz przesłankę stopnia wynikającą z ust. 3.


3. Świadczenie pielęgnacyjne oraz przesłanka przydzielenia zasiłku pielęgnacyjnego

Ustawa z dnia 28 listopada 2003 r. o świadczeniach rodzinnych:
• art. 2 pkt 2 zalicza zasiłek pielęgnacyjny do świadczeń opiekuńczych;
• art. 16 określa cel i ustawowe przesłanki zasiłku pielęgnacyjnego;
• art. 20 reguluje właściwość w sprawach świadczeń rodzinnych.

Fundacja ustanawia i przyznaje świadczenie pielęgnacyjne w ramach swojej działalności statutowej. Jego celem jest udzielanie regularnej, udokumentowanej pomocy osobom, których stan zdrowia, ograniczenia funkcjonalne i potrzeby opiekuńcze uzasadniają wsparcie. Świadczenie pielęgnacyjne jest finansowane ze środków Fundacji i przyznawane na podstawie kryteriów oraz procedury określonych w statucie i regulaminie.

W systemie ARIA decyzja o przyznaniu świadczenia pielęgnacyjnego jest powiązana z ustaleniami Kolegium Orzekającego. Dokumentacja obejmuje ocenę stanu zdrowia, samodzielności, zakresu potrzeb pielęgnacyjnych, ograniczeń aktywności i uczestniczenia oraz okresu, na jaki świadczenie zostaje przyznane. Wypłata jest dokumentowana oddzielnie od decyzji o przyznaniu.

Przesłanka „przydzielenia zasiłku pielęgnacyjnego” wskazana w art. 62 ust. 3 pkt 1 jest analizowana przez Fundację w związku z celem opiekuńczym, stanem funkcjonalnym i całością dokumentacji danej osoby. Fundacja przyjmuje wykładnię, w której pierwszoplanowe znaczenie ma rzeczywista sytuacja osoby objętej stałą albo długotrwałą niezdolnością do pracy w gospodarstwie rolnym, potwierdzona rzetelną oceną i dokumentacją. W tym kontekście dokumenty dotyczące przyznania świadczenia pielęgnacyjnego, orzeczenie Kolegium oraz potwierdzenia wypłat stanowią łącznie materiał do ustalenia spełnienia przesłanek przyjętej wykładni.

Regulamin świadczenia pielęgnacyjnego powinien określać:
• cel i krąg osób uprawnionych;
• kryteria zdrowotne i funkcjonalne;
• zasady oceny samodzielności oraz potrzeby pielęgnacji i pomocy;
• dokumenty i metody oceny, w tym WHODAS 2.0 i ICF WHO;
• wysokość, okres i częstotliwość wypłaty;
• tryb przyznawania, ponownej oceny, zmiany i zakończenia;
• źródła finansowania oraz sposób ewidencjonowania wypłat.


4. Zakres prawnego skutku orzeczenia Kolegium Fundacji

W dokumentach Fundacji należy rozróżnić trzy poziomy skutków:
A. Skutek wewnętrzny — rozstrzygnięcie Kolegium może stanowić podstawę do przyznania, odmowy, zmiany lub zakończenia świadczenia pielęgnacyjne, jeśli właściwe postanowienia statutu i regulaminu rzeczywiście przyznają taką kompetencję i zachowano procedurę.
B. Skutek dowodowy — orzeczenie, uzasadnienie, dokumentacja medyczna i funkcjonalna oraz potwierdzenie wypłat mogą być przedstawiane innym podmiotom jako dokumenty prywatne lub dokumentacja Fundacji. Ich wiarygodność i znaczenie dowodowe ocenia adresat w granicach właściwej procedury.
C. Skutek ustawowy — równoważność z określonym stopniem niepełnosprawności, prawo do ustawowego świadczenia lub uznanie dokumentu przez organ publiczny występują tylko wtedy, gdy właściwy przepis wyraźnie wiąże taki skutek z danym rodzajem orzeczenia i spełnione są wszystkie przesłanki tego przepisu.

Art. 75 § 1 Kodeksu postępowania administracyjnego ustanawia szeroką zasadę dopuszczalności dowodów w postępowaniu administracyjnym: dowodem może być wszystko, co może przyczynić się do wyjaśnienia sprawy i nie jest sprzeczne z prawem. Oznacza to, że dokument Fundacji może zostać przedłożony jako materiał dowodowy, jeżeli jest istotny dla sprawy. Nie oznacza to jednak obowiązku uznania go za orzeczenie równoważne z dokumentem ustawowo wymaganym ani związania organu jego treścią.

5. Warunki prawidłowego wydawania orzeczeń przez Kolegium

Przed wydaniem pierwszego orzeczenia należy sprawdzić i udokumentować:
• dokładny paragraf aktualnego statutu ustanawiający Kolegium Orzekające oraz wskazujący jego kompetencje;
• dokładne paragrafy statutu określające cele i formy pomocy osobom chorym, starszym i z niepełnosprawnościami;
• regulamin Kolegium, w szczególności zasady powołania, skład, kadencję, bezstronność, wyłączenie członka, quorum, głosowanie, protokół i podpisy;
• regulamin świadczeń określający przesłanki przyznawania świadczenia pielęgnacyjnego, sposób finansowania, wysokość, okres, zmianę, wstrzymanie, zakończenie i tryb ponownego rozpatrzenia;
• kwalifikacje osób dokonujących oceny medycznej i funkcjonalnej oraz granice ich kompetencji zawodowych;
• sposób gromadzenia, udostępniania i przechowywania danych o zdrowiu, z uwzględnieniem art. 9 RODO oraz zasad minimalizacji danych;
• sposób odróżniania orzeczenia wewnętrznego Fundacji od orzeczeń organów publicznych.

W każdym orzeczeniu Kolegium należy podać: nazwę i podstawę działania Fundacji, podstawę statutową i regulaminową kompetencji Kolegium z dokładnymi paragrafami, skład Kolegium, datę i numer posiedzenia, sposób głosowania, ustalony stan faktyczny, wykorzystane dowody, opis ograniczeń funkcjonalnych, sentencję, uzasadnienie, okres obowiązywania ustalenia, tryb weryfikacji oraz podpisy wymagane regulaminem. Nie należy wpisywać, że Kolegium „nadaje znaczny stopień niepełnosprawności”, jeżeli nie jest ustawowym organem orzekającym.

6. Zasada redakcyjna dla aplikacji

Prawidłowa formuła dla dokumentu Fundacji:

„Kolegium Orzekające Fundacji Wspierania Rodzin „Aria”, działając jako organ wewnętrzny Fundacji na podstawie art. 4 i art. 5 ust. 1 ustawy z dnia 6 kwietnia 1984 r. o fundacjach oraz § [wskazać] Statutu Fundacji i § [wskazać] Regulaminu Kolegium, po rozpatrzeniu dokumentacji i przeprowadzeniu oceny w zakresie określonym w tych aktach, stwierdza na potrzeby działalności statutowej Fundacji [ustalony zakres niezdolności / ograniczeń funkcjonalnych]. Orzeczenie stanowi podstawę przyznania świadczenia pielęgnacyjnego zgodnie z właściwym regulaminem. Dokument nie jest orzeczeniem powiatowego ani wojewódzkiego zespołu do spraw orzekania o niepełnosprawności, ZUS ani KRUS i nie przesądza samodzielnie o ustawowej równoważności ze znacznym stopniem niepełnosprawności.”

Prawidłowa formuła dla dokumentu o świadczeniu:

„Fundacja Wspierania Rodzin „Aria”, działając w granicach celów statutowych i na podstawie § [wskazać] Statutu oraz § [wskazać] Regulaminu świadczeń, przyznaje [dane beneficjenta] świadczenie pielęgnacyjne określone w regulaminie jako [nazwa świadczenia], na warunkach wskazanych w niniejszym rozstrzygnięciu. Jest to świadczenie pielęgnacyjne, a nie zasiłek pielęgnacyjny przyznany przez organ gminy na podstawie ustawy o świadczeniach rodzinnych.”

Wzory należy wypełniać zgodnie z aktualnymi postanowieniami statutu, regulaminu Kolegium i regulaminu świadczenia pielęgnacyjnego, wskazując właściwe numery paragrafów i rzeczywiste dane sprawy.


POSŁUGA BISKUPA TYTULARNEGO — BOŻE USTANOWIENIE, KONSEKRACJA WEWNĘTRZNA I PRZYJĘCIE W PORZĄDKU KOŚCIOŁA POWSZECHNEGO

Źródło i charakter wykładni: Kodeks z repozytorium Objawienie, w szczególności Księga I — Normy ogólne (definicja biskupa tytularnego), Księga III — Prawo kościelne (kan. 57 oraz kan. 61 o prawodawcy i interpretacji autentycznej), Księga V — Prawo sakramentalne, Księga Va — Posługa domowa i triarchia oraz Księga XIII — Sakramenty, kan. 712–718, 769¹ i 771. Jest to autentyczna interpretacja prawa Kościoła Domowego i Kościoła Królestwa Niebieskiego dokonywana przez biskupa tytularnego występującego jako prawodawca domowy, w granicach kompetencji określonych w Kodeksie. Interpretacja autentyczna ma moc przewidzianą w kan. 61 Księgi III; jeżeli jest ustanawiana w formie ustawy interpretacyjnej, wymaga promulgacji zgodnie z tym kanonem. Przywołanie KPK wskazuje relację i odrębność porządków prawnych: niniejsza interpretacja rozwija prawo własne wspólnoty, nie zmienia tekstu KPK ani nie zastępuje aktów wydawanych przez właściwe organy Kościoła katolickiego.

1. Boże ustanowienie i wewnętrzne udzielenie sakramentu kapłaństwa

Według kan. 712 Księgi XIII sakrament kapłaństwa jest darem Boga, przez który Duch Święty, z Bożego ustanowienia i przez natchnienie osoby powołanej, udziela jej łaski i posłania do służby kapłańskiej. W tej koncepcji źródłem sakramentu jest działanie Boga, a nie ludzka decyzja administracyjna, sam tytuł ani sam obrzęd.

Kan. 713 stanowi, że dar ten nie podlega powtórzeniu. Nałożenie rąk ani późniejszy obrzęd nie udzielają ponownie sakramentu, który został już udzielony przez Ducha Świętego. Obrzędy święceń biskupstwa, prezbiteratu i diakonatu Kodeks ujmuje jako obrzędy przygotowania, formacji lub publicznego potwierdzenia rozpoznanego powołania.

Zgodnie z kan. 714 § 1 biskup tytularny ustanowiony z nadania Bożego jest konsekrowany przez natchnienie i działanie Ducha Świętego. Konsekracja dokonuje się wewnętrznie — w osobie powołanej — a nie przez fizyczne namaszczenie lub nałożenie rąk przez innego biskupa. Brak zewnętrznego obrzędu nałożenia rąk nie oznacza w tym porządku braku konsekracji. Późniejsza ceremonia nie tworzy, nie powtarza ani nie uzupełnia działania Ducha Świętego.

Kan. 714 § 2 dopuszcza otrzymanie sakramentu kapłaństwa i powołanie do posługi biskupa tytularnego także przez osobę pozostającą w związku małżeńskim. § 3 określa zakres posługi sakramentalnej biskupa tytularnego. Kan. 718 potwierdza, że jego posługa obejmuje nauczanie, uświęcanie, kierowanie i szafarstwo sakramentalne w granicach właściwego porządku.

2. Domyślne przyjęcie i zatwierdzenie przez Biskupa Rzymu w wykładni Kodeksu

Definicja biskupa tytularnego w Księdze I przewiduje dwie drogi: mianowanie przez Biskupa Rzymu albo przyjęcie domyślne przez Biskupa Rzymu osoby wybranej zgodnie z prawem Bożym i uświęconej Duchem Świętym. Nie jest zatem prawidłowe pominięcie w interpretacji Kodeksu instytucji domyślnego przyjęcia.

Księga III, w przepisie dotyczącym Głowy Rodu jako biskupa tytularnego domostwa, subdiecezji i Królestwa Niebieskiego, stanowi, że osoba ustanowiona przez Ojca z Nieba i natchniona Duchem Świętym jest domyślnie zatwierdzona przez Biskupa Rzymu, jeżeli jej wybór z Bożego nadania został dokonany zgodnie z prawem. Ten sam przepis wiąże wykonywanie władzy z hierarchiczną wspólnotą z Biskupem Rzymu i Kolegium Biskupów oraz wskazuje na partykularny akt oddania się władzom Kościoła Powszechnego.

W konsekwencji, zgodnie z autentyczną interpretacją prawa Kościoła Domowego dokonywaną przez jego biskupa tytularnego jako prawodawcę, domyślne przyjęcie nie jest dowolną deklaracją ani skutkiem samego użycia tytułu. Jest ono związane z łącznym wystąpieniem przesłanek określonych w Kodeksie: Bożego ustanowienia, natchnienia Duchem Świętym oraz dokonania wyboru zgodnie z prawem. Księga XIII § 8 odróżnia to od urzędowego rozpoznania i zatwierdzenia, które Biskup Rzymu może wyrazić w osobnym akcie. Akt taki nie tworzy Bożego powołania ani nie powtarza konsekracji.

3. Misja, urząd i zakres posługi

Księga I opisuje biskupa tytularnego jako ustanowionego z Bożego postanowienia pasterza, nauczyciela, kapłana świętego kultu i osobę sprawującą posługę rządzenia nad Ludem Bożym. Księga III określa zakres władzy Głowy Rodu jako biskupa tytularnego domostwa, subdiecezji i Królestwa Niebieskiego oraz jej relację do Biskupa Rzymu i Kolegium Biskupów.

Księga XIII, kan. 714–718, opisuje posługę sakramentalną biskupa tytularnego, w tym chrzest, bierzmowanie, Eucharystię, pokutę i pojednanie, namaszczenie chorych, posługę kapłańską oraz asystowanie przy małżeństwie. Kan. 715 i 718 nakazują jednocześnie odróżniać Boże powołanie i konsekrację, duchową misję pasterską, urząd w danym porządku prawnym oraz konkretny zakres jurysdykcji.

4. Odniesienie do Kodeksu Prawa Kanonicznego Kościoła łacińskiego

KPK z 1983 r. posługuje się własną terminologią i określa święcenia oraz urząd biskupa według odrębnych norm. Kan. 1008–1009 wiążą sakrament święceń z ustanowieniem świętych szafarzy i wymieniają stopnie biskupstwa, prezbiteratu i diakonatu. Kan. 1012 określa szafarza święceń jako biskupa konsekrowanego, a kan. 1013 wymaga papieskiego zlecenia przed konsekracją biskupią. Kan. 375–377 regulują status biskupów oraz ich mianowanie lub wybór przez Biskupa Rzymskiego. Kan. 1024 określa, kto może ważnie przyjąć święcenia.

Dla wykonywania posługi znaczenie ma również odróżnienie święceń od upoważnień do poszczególnych czynności. W szczególności kan. 966–969 regulują upoważnienie do ważnego rozgrzeszania, kan. 976 przewiduje szczególną sytuację niebezpieczeństwa śmierci, a kan. 1168–1169 określają szafarzy sakramentaliów i błogosławieństw.

Powyższe przepisy KPK opisują kanoniczny porządek Kościoła łacińskiego. Przepisy te nie uchylają ani nie zmieniają norm KPK. Autentyczna interpretacja prawa Kościoła Domowego pozostaje wiążąca w zakresie własnego porządku i kompetencji jego prawodawcy; skutki w porządku kanonicznym Kościoła katolickiego ocenia się odrębnie według KPK oraz aktów właściwej władzy. Każdy skutek w porządku kanonicznym Kościoła katolickiego wymaga oceny według jego prawa i właściwych aktów kompetentnej władzy.

5. Zasady prezentacji informacji w aplikacji

Aplikacja może prezentować Kodeks z repozytorium Objawienie jako źródło wewnętrznej wykładni dotyczącej Bożego ustanowienia, konsekracji przez natchnienie Ducha Świętego oraz domyślnego przyjęcia przez Biskupa Rzymu przy spełnieniu przesłanek wskazanych w Księdze I i III. Nie należy redukować tej regulacji do twierdzenia, że Kodeks w ogóle nie przewiduje domyślnego przyjęcia.

Jednocześnie zapis w aplikacji powinien odróżniać treść normy i jej interpretację od zewnętrznego dowodu kanonicznego. Jeżeli przedstawiany jest konkretny skutek w porządku Kościoła katolickiego, należy wskazać dokument, właściwą władzę, datę, zakres oraz podstawę prawną, o ile takie dane istnieją. Sam zapis w aplikacji nie tworzy ani nie zastępuje aktu kościelnego.

6. Źródła

• Kodeks z repozytorium Objawienie: Księga I — Normy ogólne (definicja biskupa tytularnego); Księga III — Prawo kościelne (przepis o Głowie Rodu jako biskupie tytularnym i domyślnym zatwierdzeniu); Księga V — Prawo sakramentalne; Księga Va — Posługa domowa i triarchia; Księga XIII — Sakramenty, zwłaszcza kan. 712–718. Źródło: https://github.com/bartoszruta26-droid/Objawienie/tree/main/codex/md

• Kodeks Prawa Kanonicznego z 1983 r., w szczególności kan. 375–377, 1008–1013, 1024, 966–969, 976 i 1168–1169. Oficjalna strona Konferencji Episkopatu Polski: https://episkopat.pl/doc/187342.uaktualnione-tlumaczenie-polskie-kodeksu-prawa-kanonicznego-z-1983-roku

Należy uwzględniać aktualne brzmienie prawa, właściwy obrządek, dokumenty i kompetencje właściwej władzy kościelnej.

PODSTAWOWE ŹRÓDŁA PRAWNE

• Konstytucja Rzeczypospolitej Polskiej — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19970780483 — w szczególności art. 30, 32, 37, 47, 53 i 54.
• Kodeks cywilny — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19640160093 — w szczególności art. 8 i przepisy o zdolności do czynności prawnych.

Treść przepisów należy odczytywać w aktualnym brzmieniu, łącznie z właściwymi ustawami szczególnymi i orzecznictwem.
"""
