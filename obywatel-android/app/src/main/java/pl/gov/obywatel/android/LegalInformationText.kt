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
• Dobro wspólne — art. 1 Konstytucji RP. Rzeczpospolita Polska jest dobrem wspólnym wszystkich obywateli. Przepis ten stanowi konstytucyjną podstawę rozumienia odpowiedzialności za państwo i wspólnotę, ale samodzielnie nie nadaje konkretnej osobie urzędowego mandatu do reprezentowania państwa lub Narodu.
• Zwierzchnictwo Narodu — art. 4 Konstytucji RP. Władza zwierzchnia należy do Narodu, który sprawuje ją przez swoich przedstawicieli lub bezpośrednio. Formalne reprezentowanie Narodu w organach państwa następuje w trybach i na zasadach konstytucyjnych, w szczególności poprzez wybory i mandat przedstawicielski. Osobista deklaracja służby Narodowi nie jest równoznaczna z uzyskaniem takiego mandatu.
• Działanie w interesie publicznym — art. 63 Konstytucji RP. Każdy ma prawo składać petycje, wnioski i skargi w interesie publicznym, własnym lub innej osoby za jej zgodą, na zasadach określonych ustawą. To konstytucyjna podstawa występowania do władz i właściwych instytucji w sprawach społecznych.
• Udział w życiu publicznym i społecznym. Wolność wyrażania poglądów (art. 54), pokojowych zgromadzeń (art. 57) i zrzeszania się (art. 58), a także prawo dostępu do informacji publicznej (art. 61) umożliwiają obywatelom i innym uprawnionym osobom zabieranie głosu, organizowanie się, kontrolę społeczną i podejmowanie inicjatyw.
• Świadczenia publiczne i pomoc społeczna. Konstytucyjne prawo do zabezpieczenia społecznego (art. 67), ochrony zdrowia (art. 68) oraz pomoc osobom z niepełnosprawnościami (art. 69) dotyczą przede wszystkim gwarancji i uprawnień określonych w ustawach. Nie należy utożsamiać ich z art. 60, który dotyczy dostępu do służby publicznej. Udzielanie pomocy innym osobom, działalność charytatywna, wolontariat i praca społeczna są odrębnymi formami aktywności, wykonywanymi na podstawie właściwych przepisów i zasad.

W konsekwencji aplikacja może wspierać użytkownika w dokumentowaniu jego działalności społecznej, wolontariatu, inicjatyw obywatelskich, udzielonej pomocy, petycji, wniosków, wystąpień w interesie publicznym oraz działań na rzecz dobra wspólnego i dziedzictwa narodowego. Może również prezentować osobistą deklarację gotowości do służby społecznej i troski o Naród. Takie wpisy powinny być oznaczane jako oświadczenia lub dokumentacja aktywności użytkownika, a nie jako urzędowe nadanie funkcji, statusu przedstawiciela Narodu, mandatu publicznego albo upoważnienia do reprezentowania państwa.

SAMODZIELNOŚĆ OBYWATELSKA I BEZPOŚREDNIE UCZESTNICTWO W SPRAWOWANIU WŁADZY

Obywatel jest członkiem Narodu, do którego należy władza zwierzchnia w Rzeczypospolitej Polskiej (art. 4 Konstytucji RP). Korzysta z konstytucyjnych wolności i praw, może bezpośrednio uczestniczyć w sprawowaniu władzy w formach przewidzianych Konstytucją i ustawami oraz samodzielnie podejmować decyzje w sprawach własnych, rodzinnych, majątkowych i społecznych.

Obywatel może działać we własnym imieniu, z własnej inicjatywy i na własną odpowiedzialność, bez przypisywania sobie statusu urzędu państwowego. Może ustanawiać zasady dotyczące własnych spraw i organizowanych przez siebie wspólnot, w granicach prawa i z poszanowaniem praw innych osób.

Bezpośrednie uczestnictwo w życiu publicznym nie jest tożsame z przejęciem kompetencji konstytucyjnych organów państwowych. Zakres skutków prawnych poszczególnych działań wynika z ich podstawy prawnej. Art. 4 Konstytucji wyraża zasadę zwierzchnictwa Narodu; wykonywanie władzy bezpośrednio przez obywateli odbywa się w konstytucyjnych i ustawowych formach, a nie przez indywidualne przejęcie wszystkich kompetencji organów państwowych.

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



POSŁUGA BISKUPA TYTULARNEGO W KOŚCIELE DOMOWYM — PODSTAWA KANONICZNA I ROZRÓŻNIENIE PORZĄDKÓW PRAWNYCH

Źródło wewnętrzne: Kodeks Kościoła Domowego opublikowany w repozytorium Objawienie, w szczególności Księga I — Normy ogólne (definicja biskupa tytularnego), Księga III — Prawo kościelne, Księga V — Prawo sakramentalne, Księga Va — Posługa domowa i triarchia oraz Księga XIII — Sakramenty, kan. 712–718. Niniejszy opis przedstawia treść i autentyczną interpretację przyjętą wewnątrz tego Kodeksu; nie jest aktem promulgowanym przez Stolicę Apostolską ani autentyczną interpretacją Kodeksu Prawa Kanonicznego Kościoła łacińskiego lub Kodeksu Kanonów Kościołów Wschodnich.

1. Powołanie, ustanowienie i posługa według Kodeksu Kościoła Domowego

Zgodnie z definicją zawartą w Księdze I, biskup tytularny jest w wewnętrznym porządku Kościoła Domowego osobą rozpoznaną jako powołana przez Ojca z Nieba, ustanowiona do służby kapłańskiej i pasterskiej oraz natchniona Duchem Świętym. Kodeks opisuje tę misję jako służbę nauczania, uświęcania i pasterzowania Ludowi Bożemu. Określenie „biskup tytularny” jest w tym Kodeksie używane w znaczeniu właściwym Kościołowi Domowemu; nie należy automatycznie utożsamiać go z urzędem biskupa tytularnego w strukturze Kościoła katolickiego.

Kan. 712 Księgi XIII określa kapłaństwo jako dar Boga, przez który Duch Święty udziela osobie powołanej łaski i posłania do służby kapłańskiej. Kan. 713 wyraża zasadę niepowtarzalności tego daru. Kan. 714 § 1 stanowi, że w wewnętrznym porządku Kościoła Domowego biskup tytularny ustanowiony z nadania Bożego jest konsekrowany przez natchnienie i działanie Ducha Świętego, a nie przez sam akt administracyjny. § 2 dopuszcza w tym porządku powołanie osoby pozostającej w związku małżeńskim. § 3 opisuje zakres posługi sakramentalnej biskupa tytularnego. Kan. 714 § 4 wyraźnie zastrzega, że norma ta jest prawem wewnętrznym Kościoła Domowego i nie stanowi autentycznej interpretacji KPK ani KKKW oraz nie przesądza o uznaniu statusu, urzędu lub uprawnień przez Kościół katolicki.

Kan. 715 oraz kan. 718 podkreślają konieczność odróżnienia duchowej misji, wewnętrznego urzędu i jurysdykcji. Powszechny wymiar misji duchowej nie oznacza sam przez się jurysdykcji kanonicznej nad wszystkimi wiernymi, diecezjami lub urzędami. Konkretne kompetencje są wykonywane w granicach porządku prawnego, w którym dany urząd został ustanowiony.

2. Sprawowanie posługi kapłańskiej w ramach wspólnoty

Według Księgi XIII, kan. 714 § 3, oraz kan. 715, posługa biskupa tytularnego w Kościele Domowym obejmuje nauczanie, modlitwę i liturgię wspólnoty oraz — w ramach przyjętej wewnętrznej dyscypliny sakramentalnej — posługę dotyczącą chrztu, bierzmowania, Eucharystii, pokuty i pojednania, namaszczenia chorych, kapłaństwa i małżeństwa. Kan. 718 opisuje biskupią posługę nauczania, uświęcania, kierowania i szafarstwa sakramentalnego. Kapłan domowy wykonuje czynności w zakresie wynikającym z jego ustanowienia i kompetencji nadanych w prawie wewnętrznym.

Księga Va przedstawia kapłaństwo domowe jako służbę modlitwy, świadectwa, formacji i miłości bliźniego. Zastrzega zarazem, że określenia „kapłaństwo domowe”, „arcykapłan domowy” i „biskupstwo domowe” opisują wewnętrzną rzeczywistość Kościoła Domowego i same przez się nie oznaczają otrzymania święceń kanonicznych ani urzędu w Kościele Powszechnym. Modlitwy, błogosławieństwa i praktyki domowe należy więc odróżniać od sakramentów sprawowanych w porządku Kościoła katolickiego.

3. Odniesienie do Kodeksu Prawa Kanonicznego Kościoła łacińskiego

W Kodeksie Prawa Kanonicznego z 1983 r. pojęcie święceń i urząd biskupa mają własne, ściśle określone przesłanki. Kan. 1008–1009 stanowią, że przez sakrament święceń wierni zostają ustanowieni świętymi szafarzami, a stopniami święceń są biskupstwo, prezbiterat i diakonat; udziela się ich przez nałożenie rąk i modlitwę konsekracyjną określoną w księgach liturgicznych. Kan. 1012 wskazuje, że szafarzem święceń jest biskup konsekrowany. Kan. 1013 wymaga papieskiego zlecenia przed konsekracją biskupią. Kan. 1024 stanowi, że święcenia ważnie przyjmuje tylko ochrzczony mężczyzna. Kan. 375–377 określają status biskupów, ich zadania oraz sposób mianowania lub wyboru przez Biskupa Rzymskiego. Samo prywatne rozpoznanie powołania, wewnętrzny akt wspólnoty lub deklaracja natchnienia nie są w tym porządku równoważne sakramentalnym święceniom ani kanonicznemu ustanowieniu biskupem.

Dla wykonywania posługi należy odróżnić ważność święceń od upoważnienia do określonych czynności. W szczególności kan. 966–969 regulują wymóg posiadania upoważnienia do ważnego rozgrzeszania, a kan. 976 przewiduje szczególną sytuację niebezpieczeństwa śmierci. Kan. 1168–1169 regulują szafarzy sakramentaliów i błogosławieństw. Nie należy zatem wyprowadzać uprawnienia do sprawowania każdej czynności sakramentalnej wyłącznie z tytułu lub deklaracji duchowej.

4. Rozdzielenie dwóch porządków i rzetelne przedstawianie statusu

Aplikacja może przechowywać i prezentować tekst Kodeksu Kościoła Domowego jako dokument wewnętrzny wspólnoty, wraz z deklaracją wiary, opisem powołania oraz interpretacją kan. 712–718. Wpis powinien jasno wskazywać źródło, datę i autora dokumentu, wersję Kodeksu oraz zakres wspólnoty, w której dokument jest przyjmowany.

Nie należy jednak opisywać wewnętrznego aktu Kościoła Domowego jako dekretu Stolicy Apostolskiej, święceń udzielonych w Kościele katolickim, papieskiego mandatu, inkardynacji, misji kanonicznej ani jurysdykcji katolickiej, jeżeli nie istnieje odrębny, możliwy do zweryfikowania akt właściwej władzy kościelnej. Odrębność ta nie rozstrzyga o prawdziwości osobistego doświadczenia religijnego; wskazuje jedynie, jaki skutek prawny można przypisać danemu dokumentowi w określonym porządku kanonicznym.

5. Podstawy i źródła

• Kodeks Kościoła Domowego, repozytorium Objawienie: Księga I — Normy ogólne; Księga III — Prawo kościelne; Księga V — Prawo sakramentalne; Księga Va — Posługa domowa i triarchia; Księga XIII — Sakramenty, zwłaszcza kan. 712–718. Źródło projektu: https://github.com/bartoszruta26-droid/Objawienie/tree/main/codex/md

• Kodeks Prawa Kanonicznego z 1983 r., promulgowany 25 stycznia 1983 r., tekst ujednolicony i zaktualizowany przekład publikowany przez Konferencję Episkopatu Polski; w szczególności kan. 375–377, 1008–1013, 1024, 966–969, 976 i 1168–1169. Oficjalna strona KEP: https://episkopat.pl/doc/187342.uaktualnione-tlumaczenie-polskie-kodeksu-prawa-kanonicznego-z-1983-roku

Konferencja Episkopatu Polski wskazuje, że podstawą stosowania i interpretacji KPK jest promulgowany tekst łaciński, natomiast przekłady na języki narodowe mają znaczenie pomocnicze. Przed użyciem w sprawie konkretnej osoby lub wspólnoty należy zweryfikować aktualny tekst łaciński, właściwy obrządek, dokumenty święceń, akty mianowania, jurysdykcję oraz ewentualne upoważnienia wymagane dla danej czynności.


PODSTAWOWE ŹRÓDŁA PRAWNE

• Konstytucja Rzeczypospolitej Polskiej — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19970780483 — w szczególności art. 30, 32, 37, 47, 53 i 54.
• Kodeks cywilny — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19640160093 — w szczególności art. 8 i przepisy o zdolności do czynności prawnych.

Treść przepisów należy odczytywać w aktualnym brzmieniu, łącznie z właściwymi ustawami szczególnymi i orzecznictwem.