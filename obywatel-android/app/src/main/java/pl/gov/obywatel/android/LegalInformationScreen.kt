// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val LEGAL_INFORMATION_TEXT = """
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

PODSTAWOWE ŹRÓDŁA PRAWNE

• Konstytucja Rzeczypospolitej Polskiej — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19970780483 — w szczególności art. 30, 32, 37, 47, 53 i 54.
• Kodeks cywilny — https://isap.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU19640160093 — w szczególności art. 8 i przepisy o zdolności do czynności prawnych.

Treść przepisów należy odczytywać w aktualnym brzmieniu, łącznie z właściwymi ustawami szczególnymi i orzecznictwem.
"""

@Composable
fun LegalInformationScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val sections = LEGAL_INFORMATION_TEXT.trim().split("\n")
    Surface(
        modifier = Modifier.fillMaxSize().semantics { paneTitle = "Informacje prawne" },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    "Informacje prawne",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sections.forEach { line ->
                        when {
                            line.isBlank() -> Spacer(Modifier.height(3.dp))
                            line == line.uppercase() && line.length < 110 ->
                                Text(line, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            line.startsWith("• ") ->
                                Text(line, style = MaterialTheme.typography.bodyMedium)
                            else ->
                                Text(line, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }


REGULAMIN DOTACJI CELOWYCH — PROJEKT FUNDUSZU REHABILITACYJNO-OPIEKUŃCZEGO

Status dokumentu i zastrzeżenie prawne

Niniejszy dokument jest projektem zasad przyszłego programu finansowania zadań z zakresu rehabilitacji, pielęgnacji, higieny i profilaktyki zdrowotnej osób przewlekle chorych, osób z niepełnosprawnościami oraz osób w podeszłym wieku. Samo przyjęcie lub opublikowanie niniejszego regulaminu nie tworzy państwowego funduszu celowego, nie ustanawia organu administracji publicznej, nie nadaje dyspozytorowi statusu organu ustawowego i nie upoważnia do dysponowania środkami publicznymi. Używanie nazwy „państwowy fundusz celowy” w odniesieniu do projektu jest dopuszczalne wyłącznie jako opis zamierzonej konstrukcji prawnej, a nie jako stwierdzenie, że fundusz już został utworzony.

Państwowy fundusz celowy może zostać utworzony wyłącznie odrębną ustawą. Ustawa ta musi określić co najmniej zadania funduszu i wskazać ministra albo inny organ uprawniony do dysponowania jego wyodrębnionym rachunkiem. Dopiero ustawa tworząca fundusz może powierzyć dyspozytorowi kompetencje organu państwowego, określić jego umocowanie, odpowiedzialność, nadzór i zakres samodzielności. Regulamin może doprecyzować tryb wykonywania kompetencji ustawowych, ale nie może ich samodzielnie ustanowić ani rozszerzyć.

Do czasu wejścia w życie właściwej ustawy oraz zapewnienia zgodnej z prawem obsługi finansowej dokument ma charakter projektu regulaminu programu i nie jest podstawą przyznawania ani wypłacania dotacji ze środków publicznych. Środki przekazane przez darczyńców mogą być przyjmowane i wydatkowane wyłącznie przez podmiot mający do tego właściwą zdolność prawną, na podstawie prawidłowych umów, z poszanowaniem woli darczyńcy i przepisów o rachunkowości, podatkach, przeciwdziałaniu praniu pieniędzy oraz ochronie danych. Darowizna na rachunek prywatny lub organizacji pozarządowej nie staje się przez to przychodem państwowego funduszu celowego.

ROZDZIAŁ I. POSTANOWIENIA OGÓLNE

§ 1. Przedmiot regulaminu

1. Regulamin określa projektowane zasady planowania, ogłaszania, przyjmowania, oceny, wyboru, finansowania, rozliczania i kontroli zadań służących rehabilitacji, pielęgnacji, utrzymaniu higieny, profilaktyce zdrowotnej i poprawie jakości życia osób:
   1) długotrwale lub przewlekle chorych;
   2) z niepełnosprawnościami;
   3) w podeszłym wieku, w szczególności wymagających wsparcia w codziennym funkcjonowaniu;
   4) zagrożonych utratą samodzielności, wykluczeniem zdrowotnym lub ograniczeniem dostępu do niezbędnych świadczeń i usług opiekuńczych.
2. Regulamin ma służyć ochronie godności, autonomii, bezpieczeństwa i równego traktowania osób objętych pomocą.
3. Pomoc finansowana w ramach programu ma charakter celowy, pomocniczy i uzupełniający wobec świadczeń gwarantowanych oraz innych form wsparcia, z uwzględnieniem przepisów o koordynacji świadczeń i zakazu podwójnego finansowania tych samych wydatków.

§ 2. Definicje

Ilekroć w regulaminie jest mowa o:
1) Funduszu — rozumie się przez to projektowany państwowy fundusz celowy, który może powstać wyłącznie na podstawie odrębnej ustawy; do tego czasu określenie odnosi się do projektu programu;
2) Ustawie tworzącej — rozumie się przez to przyszłą odrębną ustawę ustanawiającą Fundusz, wskazującą jego dysponenta, zadania, źródła przychodów i zasady gospodarki finansowej;
3) Dysponencie Funduszu — rozumie się przez to ministra albo inny organ wskazany w Ustawie tworzącej, któremu art. 29 ustawy o finansach publicznych oraz Ustawa tworząca powierzają kompetencję dysponowania rachunkiem Funduszu;
4) Dyspozytorze — rozumie się przez to osobę lub organ, któremu Ustawa tworząca bezpośrednio albo na podstawie wyraźnego upoważnienia powierzy wykonywanie określonych czynności w zakresie rozdysponowania środków; sama nazwa „dyspozytor” nie tworzy kompetencji publicznoprawnej;
5) Dotacji — rozumie się przez to bezzwrotne środki publiczne udzielane na podstawie właściwego przepisu prawa i umowy lub aktu stanowiącego podstawę finansowania, na realizację określonego zadania;
6) Darowiźnie celowej — rozumie się przez to przysporzenie dokonane przez darczyńcę z oznaczeniem przeznaczenia, przyjęte przez podmiot uprawniony na warunkach zgodnych z prawem; darowizna nie jest automatycznie dotacją ani środkiem publicznym;
7) Beneficjencie — rozumie się przez to osobę lub podmiot, któremu zgodnie z Ustawą tworzącą i regulaminem przyznano środki albo na którego rzecz finansowane jest zadanie;
8) Realizatorze — rozumie się przez to podmiot wykonujący zadanie finansowane ze środków programu;
9) Zadaniu — rozumie się przez to wyodrębnione przedsięwzięcie opisane we wniosku, z określonym celem, zakresem rzeczowym, harmonogramem, budżetem i miernikami efektów;
10) Kosztach kwalifikowalnych — rozumie się przez to wydatki niezbędne, racjonalne, udokumentowane i bezpośrednio związane z zadaniem, dopuszczone w ogłoszeniu i zgodne z przepisami.

§ 3. Zasady podstawowe

1. Środki Funduszu mogą być wykorzystywane wyłącznie na cele i w ramach limitów określonych w Ustawie tworzącej oraz rocznym planie finansowym.
2. Każde rozstrzygnięcie o przyznaniu środków wymaga wskazania podstawy prawnej, dostępności środków, zgodności zadania z celem Funduszu i zachowania właściwej procedury.
3. Gospodarowanie środkami odbywa się z zachowaniem legalności, celowości, oszczędności, efektywności, terminowości, przejrzystości, równego traktowania, bezstronności, rozliczalności i ochrony danych.
4. Nie wolno zaciągać zobowiązań ponad limit planu finansowego ani finansować wydatków bez dostępnych środków.
5. Niedopuszczalne jest finansowanie wydatków niezgodnych z przeznaczeniem dotacji, prywatnych korzyści osób podejmujących decyzje lub podmiotów z nimi powiązanych, a także wydatków podwójnie sfinansowanych.
6. Przyznanie pomocy nie może zależeć od wyznania, światopoglądu, pochodzenia, płci, wieku (poza kryteriami uzasadnionymi celem programu), niepełnosprawności ani innych cech prawnie chronionych, z zastrzeżeniem obiektywnych kryteriów dostępu ustanowionych w ustawie i ogłoszeniu.

ROZDZIAŁ II. CELE I ZAKRES FINANSOWANIA

§ 4. Cele szczegółowe

1. Celem Funduszu jest wspieranie:
   1) rehabilitacji leczniczej i funkcjonalnej, w tym działań zwiększających samodzielność i uczestnictwo społeczne;
   2) pielęgnacji i opieki długoterminowej w środowisku domowym lub placówce;
   3) utrzymania higieny osobistej i higieny otoczenia w zakresie bezpośrednio związanym ze stanem zdrowia i bezpieczeństwem osoby;
   4) profilaktyki zdrowotnej, edukacji zdrowotnej i wczesnego reagowania na pogorszenie stanu zdrowia;
   5) przeciwdziałania odleżynom, upadkom, niedożywieniu, odwodnieniu, zakażeniom i powikłaniom unieruchomienia;
   6) poprawy dostępności sprzętu pomocniczego, środków pielęgnacyjnych i dostosowań środowiska;
   7) wytchnienia i wsparcia opiekunów nieformalnych, o ile pozwala na to Ustawa tworząca;
   8) integracji opieki zdrowotnej, rehabilitacyjnej i społecznej, w granicach kompetencji podmiotów realizujących zadania.
2. Wsparcie nie zastępuje obowiązków podmiotów leczniczych, jednostek pomocy społecznej, płatników publicznych ani innych instytucji, jeżeli obowiązek finansowania wynika z odrębnych przepisów.

§ 5. Przykładowe zadania

1. Finansowanie może obejmować w szczególności:
   1) indywidualne programy rehabilitacji, fizjoterapii i terapii zajęciowej, jeżeli są prowadzone przez osoby o wymaganych kwalifikacjach;
   2) konsultacje i ocenę funkcjonalną, planowanie wsparcia oraz edukację pacjenta i opiekuna;
   3) zakup lub wypożyczenie sprzętu rehabilitacyjnego, ortopedycznego, pomocniczego i ułatwiającego samoobsługę;
   4) środki higieniczne i pielęgnacyjne, w tym produkty do pielęgnacji skóry, profilaktyki odleżyn, inkontynencji i higieny;
   5) dostosowanie łazienki, miejsca snu i przestrzeni domowej do bezpiecznej opieki, w tym uchwyty, poręcze, najazdy i inne uzasadnione rozwiązania;
   6) usługi opiekuńcze i pielęgnacyjne wykonywane zgodnie z prawem i zakresem kwalifikacji personelu;
   7) szkolenia opiekunów w zakresie bezpiecznej pielęgnacji, transferu, profilaktyki powikłań, higieny i reagowania na stany nagłe;
   8) transport do miejsca udzielania świadczeń, jeżeli jest niezbędny i dopuszczony w ogłoszeniu;
   9) działania profilaktyczne i edukacyjne, w tym ocenę ryzyka upadków, niedożywienia i odleżyn;
   10) wsparcie okresowe dla opiekunów osoby wymagającej stałej pomocy.
2. Finansowanie świadczeń zdrowotnych, wyrobów medycznych, usług opiekuńczych i rehabilitacyjnych musi uwzględniać przepisy regulujące dany rodzaj działalności, wymogi zawodowe, sanitarne, jakościowe i bezpieczeństwa.
3. Zakres rzeczowy konkretnego naboru określa jego ogłoszenie.

§ 6. Wydatki niekwalifikowalne

1. Nie finansuje się:
   1) świadczeń lub produktów niezgodnych z prawem, niebezpiecznych albo bez uzasadnienia medycznego lub funkcjonalnego, jeżeli jest ono wymagane;
   2) wydatków poniesionych przed okresem kwalifikowalności, chyba że ogłoszenie i właściwy przepis wyraźnie dopuszczają refundację;
   3) kar, grzywien, odsetek za opóźnienie, kosztów egzekucyjnych i zobowiązań niezwiązanych z zadaniem;
   4) wydatków rozliczonych z innych źródeł;
   5) zakupu aktywów lub usług na warunkach nierynkowych od osób powiązanych, chyba że dopuszcza to prawo i zastosowano procedurę wyłączenia konfliktu interesów;
   6) wydatków luksusowych, reprezentacyjnych i niezwiązanych bezpośrednio z celem zadania;
   7) kosztów działalności politycznej, partyjnej, wyznaniowej lub agitacyjnej;
   8) świadczeń pieniężnych dla osób decyzyjnych lub ich bliskich, poza wynagrodzeniem prawidłowo przewidzianym i dopuszczonym prawem.
2. Ogłoszenie naboru może uszczegółowić katalog wydatków niekwalifikowalnych, nie może jednak rozszerzyć go w sposób sprzeczny z Ustawą tworzącą.

ROZDZIAŁ III. ŹRÓDŁA ŚRODKÓW I GOSPODARKA FINANSOWA

§ 7. Źródła przychodów

1. Źródła przychodów Funduszu określi Ustawa tworząca. Mogą one obejmować darowizny celowe, jeżeli ustawa dopuści ich przyjmowanie i określi podmiot uprawniony do ich przyjęcia, sposób ewidencji oraz zasady wykorzystania.
2. Darowizny przyjmuje się na rachunek wskazany przez uprawniony podmiot, z opisem celu, warunków i ograniczeń. Nie wolno kierować darczyńcy na rachunek prywatny dyspozytora ani rachunek nieuprawnionego pośrednika.
3. Darczyńca otrzymuje informację, czy wpłata jest darowizną na rzecz państwowego funduszu celowego, darowizną na rzecz innego podmiotu, czy wpłatą na projekt społeczny. Informacja musi odpowiadać rzeczywistemu statusowi prawnemu i rachunkowi odbiorcy.
4. Przyjęcie darowizny nie może prowadzić do zastrzeżenia przez darczyńcę wpływu na indywidualne rozstrzygnięcia dotyczące beneficjentów, poza dopuszczalnym wskazaniem celu zgodnym z prawem.
5. Każdą darowiznę ujmuje się w ewidencji w sposób umożliwiający identyfikację kwoty, daty, waluty, rachunku, wskazanego celu i ewentualnych warunków.

§ 8. Plan finansowy i limity

1. Podstawą gospodarki finansowej państwowego funduszu celowego jest roczny plan finansowy sporządzany i zmieniany w trybie określonym w ustawie o finansach publicznych i Ustawie tworzącej.
2. Plan wyodrębnia zadania Funduszu i określa co najmniej stan środków na początek i koniec roku, przychody, dotacje, koszty zadań, wynagrodzenia, należności i zobowiązania oraz plan dochodów i wydatków ujmowanych w terminie zapłaty.
3. Koszty mogą być ponoszone wyłącznie w granicach planowanych przychodów i dostępnych środków z poprzednich okresów, zgodnie z ustawą o finansach publicznych.
4. Regulamin nie upoważnia do zaciągania zobowiązań, dokonywania przesunięć w planie ani uruchamiania środków w sposób odmienny od ustawowego trybu.
5. Limity poszczególnych naborów i zadań podaje się do publicznej wiadomości.

§ 9. Rachunek i ewidencja

1. Państwowy fundusz celowy stanowi wyodrębniony rachunek bankowy w rozumieniu art. 29 ustawy o finansach publicznych.
2. Rachunek prowadzi bank wybrany zgodnie z przepisami właściwymi dla obsługi funduszu.
3. Środki Funduszu nie mogą być mieszane ze środkami prywatnymi, środkami organizacji społecznej ani środkami innego programu.
4. Ewidencja księgowa musi umożliwiać identyfikację źródła, przeznaczenia, beneficjenta, zadania, daty i podstawy każdej operacji.
5. Dostęp do rachunku oraz autoryzacja płatności przysługują wyłącznie osobom upoważnionym zgodnie z ustawą, przepisami wykonawczymi, zasadami rachunkowości i wewnętrzną kontrolą.
6. Niedopuszczalne jest przekazywanie haseł, tokenów, kluczy autoryzacyjnych lub instrumentów płatniczych osobom nieuprawnionym.

ROZDZIAŁ IV. ORGANY, KOMPETENCJE I ODPOWIEDZIALNOŚĆ

§ 10. Dysponent Funduszu

1. Dysponentem Funduszu jest organ wskazany w Ustawie tworzącej.
2. Dysponent wykonuje kompetencje określone w ustawie, w szczególności zapewnia realizację zadań Funduszu, gospodarkę finansową, zgodność wydatków z planem, prowadzenie sprawozdawczości i wykonywanie obowiązków jawności.
3. Dysponent nie może przekazać kompetencji dalej, jeżeli ustawa nie dopuszcza takiego przekazania.
4. Dysponent odpowiada za legalność i prawidłowość czynności w zakresie wynikającym z przepisów prawa.

§ 11. Dyspozytor Funduszu — projektowany model

1. Ustawa tworząca może wskazać Dyspozytora jako organ lub inny podmiot uprawniony do wykonywania zadań w zakresie zarządzania programem i podejmowania rozstrzygnięć dotyczących dotacji.
2. Jeżeli Dyspozytor ma działać w zastępstwie ministra, ustawa musi wyraźnie określić:
   1) czy Dyspozytor jest odrębnym organem administracji publicznej, czy jednostką organizacyjną obsługującą organ;
   2) zakres kompetencji własnych i kompetencji wykonywanych z upoważnienia;
   3) rodzaje decyzji, postanowień, umów i czynności materialno-technicznych, do których jest uprawniony;
   4) zasady podpisywania i wydawania rozstrzygnięć;
   5) tryb zastępstwa ministra, przesłanki jego uruchomienia i granice;
   6) nadzór, kontrolę, odpowiedzialność, tryb odwoławczy i sądową kontrolę rozstrzygnięć;
   7) zasady konfliktu interesów i wyłączenia osoby od sprawy;
   8) sposób obsługi rachunku i autoryzacji płatności.
3. Samo użycie w regulaminie określenia „organ ustawowy” nie nadaje takiego statusu. Status organu, jego właściwość i kompetencje muszą wynikać z ustawy.
4. Jeżeli ustawa powierzy Dyspozytorowi określone czynności, Dyspozytor działa wyłącznie w granicach tego umocowania, z poszanowaniem planu finansowego i zasad kontroli.
5. Dyspozytor nie może samodzielnie ustanawiać nowych zadań publicznych, zmieniać ustawowych kryteriów, zwiększać limitów ani rozporządzać środkami poza planem.
6. W razie nieobecności, przeszkody lub konfliktu interesów osoby wykonującej funkcję Dyspozytora zastępstwo odbywa się wyłącznie w trybie określonym w ustawie lub prawidłowo wydanym upoważnieniu.

§ 12. Kontrola, nadzór i rozdział funkcji

1. Funkcje wnioskodawcy, oceniającego, zatwierdzającego, zlecającego płatność i kontrolującego wykonanie zadania powinny być rozdzielone w sposób ograniczający ryzyko nadużyć.
2. Osoba pozostająca w konflikcie interesów składa oświadczenie i podlega wyłączeniu z czynności dotyczących sprawy.
3. Kontrola obejmuje ocenę formalną, merytoryczną, finansową oraz rzeczową.
4. Kontrola może być prowadzona przed udzieleniem wsparcia, w trakcie realizacji zadania i po jego zakończeniu.
5. Ustalenia kontroli dokumentuje się w protokole, wskazując zakres, dowody, ustalenia, zalecenia i termin ich wykonania.
6. Wyniki kontroli wykorzystuje się do odzyskiwania środków wydanych niezgodnie z przeznaczeniem oraz do poprawy jakości programu.

ROZDZIAŁ V. NABÓR I PRZYZNAWANIE ŚRODKÓW

§ 13. Ogłoszenie naboru

1. Nabór uruchamia się w granicach środków przewidzianych w planie finansowym i zgodnie z Ustawą tworzącą.
2. Ogłoszenie publikuje się w Biuletynie Informacji Publicznej lub innym urzędowym publikatorze określonym w przepisach, a pomocniczo w aplikacji.
3. Ogłoszenie zawiera co najmniej:
   1) podstawę prawną naboru;
   2) cel, zakres i typy zadań;
   3) podmioty uprawnione do ubiegania się o środki;
   4) wysokość puli środków i maksymalną kwotę wsparcia;
   5) zasady wkładu własnego, jeżeli jest wymagany;
   6) kryteria oceny i wagi punktowe;
   7) katalog kosztów kwalifikowalnych i niekwalifikowalnych;
   8) termin, sposób i format składania wniosków;
   9) wykaz wymaganych dokumentów;
   10) sposób ogłoszenia wyników;
   11) warunki zawarcia umowy, przekazania i rozliczenia środków;
   12) tryb zgłaszania zastrzeżeń i wniosków o ponowną ocenę, jeżeli przewiduje go ustawa;
   13) dane kontaktowe i informację o przetwarzaniu danych osobowych.
4. Ogłoszenie nie może zmieniać kryteriów po rozpoczęciu naboru, chyba że zachodzi ustawowa podstawa i wszystkim wnioskodawcom zapewniono równy dostęp do informacji oraz odpowiedni termin.

§ 14. Podmioty uprawnione

1. Krąg uprawnionych wnioskodawców określi Ustawa tworząca i ogłoszenie naboru.
2. W zależności od podstawy ustawowej mogą to być osoby fizyczne wymagające wsparcia, ich opiekunowie lub przedstawiciele ustawowi, podmioty lecznicze, organizacje pozarządowe, jednostki samorządu terytorialnego, podmioty pomocy społecznej, placówki opiekuńcze oraz inni realizatorzy.
3. Jeżeli środki są przyznawane bezpośrednio osobie fizycznej, sposób ustalenia dochodu, sytuacji zdrowotnej, potrzeb i reprezentacji określają przepisy i ogłoszenie z zachowaniem minimalizacji danych.
4. Nie można wymagać od osoby wnioskującej dokumentów, których uzyskanie jest niemożliwe lub nieproporcjonalnie utrudnione, jeżeli cel i przesłanki pomocy można wiarygodnie ustalić w inny zgodny z prawem sposób.
5. W przypadku osób ubezwłasnowolnionych lub małoletnich czynności dokonują osoby uprawnione zgodnie z przepisami, przy uwzględnieniu dobra i w miarę możliwości woli osoby, której pomoc dotyczy.

§ 15. Wniosek

1. Wniosek sporządza się na formularzu określonym w ogłoszeniu i składa w sposób wskazany w ogłoszeniu.
2. Wniosek zawiera co najmniej:
   1) dane identyfikujące wnioskodawcę i — jeżeli to inna osoba — beneficjenta;
   2) opis potrzeby i celu wsparcia;
   3) opis planowanego zadania, zakres rzeczowy i harmonogram;
   4) budżet wraz z uzasadnieniem i kalkulacją kosztów;
   5) wskazanie innych źródeł finansowania;
   6) oświadczenia wymagane przepisami;
   7) zgodę na weryfikację danych w zakresie dopuszczonym prawem.
3. Zakres danych o zdrowiu ogranicza się do informacji niezbędnych do oceny wniosku. W miarę możliwości wykorzystuje się dokumentację lub zaświadczenie w zakresie potwierdzającym kryterium, bez gromadzenia całej dokumentacji medycznej.
4. Wnioskodawca składa oświadczenie o prawdziwości informacji oraz ujawnia istotne źródła finansowania tego samego kosztu.
5. Oświadczenie o odpowiedzialności karnej może być wymagane wyłącznie wtedy, gdy właściwy przepis ustawy przewiduje odebranie oświadczenia pod takim rygorem.

§ 16. Ocena formalna i merytoryczna

1. Ocena obejmuje kolejno:
   1) sprawdzenie terminu i kompletności wniosku;
   2) sprawdzenie uprawnienia wnioskodawcy i beneficjenta;
   3) ocenę zgodności zadania z celem i katalogiem wydatków;
   4) ocenę zasadności, wykonalności, kosztów i trwałości efektów;
   5) ocenę zgodności z zasadami dostępności, bezpieczeństwa i ochrony praw beneficjenta.
2. W przypadku braków formalnych wnioskodawca może zostać wezwany do ich uzupełnienia, jeżeli dopuszcza to Ustawa tworząca i ogłoszenie.
3. Kryteria merytoryczne mogą obejmować:
   1) pilność i skalę niezaspokojonej potrzeby;
   2) ryzyko pogorszenia zdrowia lub utraty samodzielności;
   3) oczekiwany wpływ na funkcjonowanie i bezpieczeństwo;
   4) adekwatność kosztu do rezultatu;
   5) możliwość szybkiego i prawidłowego wykonania;
   6) brak dostępnego finansowania z innych źródeł;
   7) równość dostępu i usunięcie barier.
4. Kryteria, wagi, progi i sposób punktacji publikuje się przed naborem.
5. Oceny dokonują osoby posiadające odpowiednie kompetencje, zobowiązane do bezstronności i poufności.
6. W miarę możliwości w ocenie zadań medycznych uczestniczy osoba posiadająca odpowiednie kwalifikacje, z zachowaniem tajemnicy zawodowej.

§ 17. Rozstrzygnięcie i informacja o wyniku

1. Rozstrzygnięcie podejmuje organ lub osoba właściwa na podstawie Ustawy tworzącej i właściwych przepisów.
2. Rozstrzygnięcie wskazuje podstawę prawną, nazwę zadania, kwotę, zakres finansowania, okres realizacji, warunki i sposób rozliczenia.
3. Informację o przyznanych dotacjach podaje się do publicznej wiadomości w zakresie wymaganym prawem, z poszanowaniem prywatności i ochrony danych.
4. Odmowa wsparcia powinna zawierać przyczynę faktyczną i prawną oraz informację o dostępnych środkach zaskarżenia lub trybie ponownego rozpatrzenia, jeżeli przewidują go przepisy.
5. Samo powiadomienie w aplikacji nie zastępuje doręczenia decyzji lub innego rozstrzygnięcia, jeżeli przepisy wymagają szczególnego sposobu doręczenia.

ROZDZIAŁ VI. UMOWA, PRZEKAZANIE I WYKORZYSTANIE ŚRODKÓW

§ 18. Podstawa przekazania środków

1. Środki przekazuje się wyłącznie na podstawie właściwej podstawy prawnej, w szczególności decyzji, umowy lub innego aktu przewidzianego w Ustawie tworzącej.
2. Umowa określa co najmniej:
   1) strony i podstawę prawną;
   2) cel i opis zadania;
   3) kwotę i harmonogram płatności;
   4) okres realizacji i termin wykorzystania środków;
   5) koszty kwalifikowalne;
   6) obowiązki informacyjne i ewidencyjne;
   7) sposób dokumentowania wydatków;
   8) zasady kontroli i dostępu do dokumentacji;
   9) warunki zmiany zakresu i budżetu;
   10) przesłanki wstrzymania płatności i rozwiązania umowy;
   11) zasady zwrotu środków niewykorzystanych lub wykorzystanych niezgodnie z przeznaczeniem;
   12) zasady przetwarzania danych i poufności;
   13) sposób rozstrzygania sporów.
3. Umowa nie może przyznawać realizatorowi praw szerszych niż wynikające z prawa i rozstrzygnięcia o przyznaniu środków.

§ 19. Zasady wydatkowania

1. Realizator wydatkuje środki celowo, oszczędnie i terminowo, z zachowaniem najlepszych efektów z danych nakładów i optymalnego doboru metod.
2. Zakupy usług, dostaw i robót budowlanych realizuje się z poszanowaniem przepisów o zamówieniach publicznych, jeżeli mają zastosowanie, oraz zasad konkurencyjności i gospodarności.
3. Wydatki dokumentuje się fakturami, rachunkami, dowodami księgowymi lub innymi dokumentami dopuszczonymi prawem.
4. Nie dopuszcza się płatności gotówkowych, jeżeli przepisy, umowa lub standard kontroli wymagają płatności bezgotówkowej.
5. Przekazanie środków osobie trzeciej lub zmiana realizatora wymaga zgody i podstawy przewidzianej w umowie oraz przepisach.

§ 20. Ochrona beneficjenta

1. Realizator zapewnia poszanowanie godności, prywatności, autonomii i praw beneficjenta.
2. Usługi zdrowotne i opiekuńcze wykonują osoby posiadające kwalifikacje wymagane dla danego rodzaju czynności.
3. Beneficjent otrzymuje zrozumiałą informację o zakresie i kosztach wsparcia, sposobie składania skarg oraz podmiocie odpowiedzialnym za wykonanie.
4. Wsparcie nie może być uzależnione od przekazania darowizny, świadczenia pracy na rzecz darczyńcy ani wyrażenia zgody na promocję lub publikację wizerunku.
5. Zgoda na udostępnienie wizerunku, historii choroby lub danych o niepełnosprawności jest odrębna, dobrowolna i możliwa do wycofania w granicach prawa.
6. W sytuacji zagrożenia życia lub zdrowia realizator postępuje zgodnie z właściwymi procedurami ratunkowymi i nie zastępuje pomocy medycznej działaniami finansowanymi przez program.

ROZDZIAŁ VII. ROZLICZENIE, ZWROT I ODPOWIEDZIALNOŚĆ

§ 21. Rozliczenie zadania

1. Realizator składa sprawozdanie w terminie i formie określonych w umowie.
2. Sprawozdanie obejmuje:
   1) opis wykonanych działań i osiągniętych rezultatów;
   2) zestawienie wydatków według kategorii;
   3) wykaz dokumentów księgowych;
   4) informację o środkach niewykorzystanych;
   5) opis odstępstw od harmonogramu i budżetu;
   6) informację o innych źródłach finansowania.
3. Rozliczenie weryfikuje się pod względem formalnym, rachunkowym, merytorycznym i zgodności z umową.
4. Dokumenty przechowuje się przez okres wymagany przepisami i umową, z uwzględnieniem ochrony danych osobowych i tajemnicy zawodowej.

§ 22. Zwrot środków

1. Środki niewykorzystane, pobrane nienależnie, w nadmiernej wysokości lub wykorzystane niezgodnie z przeznaczeniem podlegają zwrotowi na zasadach i w trybie określonym w ustawie o finansach publicznych, Ustawie tworzącej i właściwych przepisach szczególnych.
2. Ustalenie obowiązku zwrotu wymaga wskazania podstawy prawnej, kwoty, terminu i sposobu zapłaty.
3. Nie można w regulaminie ustanawiać własnych sankcji publicznoprawnych ani odsetek w oderwaniu od podstawy ustawowej.
4. W przypadku podejrzenia nadużycia sprawę przekazuje się właściwym organom zgodnie z prawem.

ROZDZIAŁ VIII. JAWNOŚĆ, SPRAWOZDAWCZOŚĆ I KONTROLA

§ 23. Jawność

1. Gospodarka środkami publicznymi jest jawna na zasadach wynikających z ustawy o finansach publicznych.
2. Upublicznia się informacje o naborach, kryteriach, wynikach, kwotach dotacji i zbiorczych efektach programu w zakresie wymaganym prawem.
3. Nie publikuje się danych pozwalających zidentyfikować beneficjenta, jego diagnozę, stan zdrowia lub sytuację rodzinną, chyba że istnieje wyraźna podstawa prawna.
4. Dane statystyczne publikuje się w formie zagregowanej lub zanonimizowanej.

§ 24. Sprawozdawczość

1. Dysponent zapewnia sporządzanie sprawozdań finansowych, budżetowych i merytorycznych w zakresie wynikającym z przepisów.
2. Raport roczny może obejmować liczbę beneficjentów, typy zadań, kwoty udzielonego wsparcia, stopień wykorzystania środków, rezultaty oraz wnioski z kontroli.
3. Mierniki efektów dobiera się do rodzaju zadania, z zachowaniem ostrożności w formułowaniu wniosków o skuteczności medycznej.

§ 25. Audyt i kontrola

1. Fundusz podlega kontroli, audytowi i nadzorowi na zasadach określonych w przepisach prawa.
2. Kontrola obejmuje prawidłowość planowania, przyjmowania darowizn, wyboru beneficjentów, zawierania umów, płatności, realizacji zadań i rozliczeń.
3. System kontroli powinien zapobiegać nadużyciom, korupcji, konfliktom interesów, fikcyjnym usługom, podwójnemu finansowaniu i nieuprawnionemu ujawnianiu danych.
4. Ustalenia kontroli oraz działania naprawcze dokumentuje się i monitoruje.

ROZDZIAŁ IX. OCHRONA DANYCH I DOSTĘPNOŚĆ

§ 26. Ochrona danych osobowych

1. Administratorem danych jest podmiot wskazany zgodnie z rzeczywistą strukturą prawną i przepisami o ochronie danych; regulamin nie ustanawia administratora przez samo nazwanie go.
2. Dane o zdrowiu przetwarza się wyłącznie na właściwej podstawie z art. 6 i art. 9 RODO oraz w zakresie niezbędnym do realizacji zadania.
3. Przed rozpoczęciem naboru przygotowuje się informację o celach, podstawach, odbiorcach, okresach przechowywania i prawach osób.
4. Dostęp do danych przyznaje się według zasady niezbędnej wiedzy i zakresu obowiązków.
5. W miarę potrzeby przeprowadza się ocenę skutków dla ochrony danych, wdraża zabezpieczenia, umowy powierzenia i procedury reagowania na naruszenia.
6. Aplikacja mobilna nie może być jedynym kanałem realizacji prawa do złożenia wniosku lub otrzymania pomocy, jeżeli prowadziłoby to do wykluczenia osób starszych, z niepełnosprawnościami lub bez dostępu do urządzenia.

§ 27. Dostępność

1. Formularze, regulaminy, komunikaty i procedury powinny być dostępne cyfrowo i zrozumiałe.
2. Zapewnia się możliwość złożenia wniosku kanałem alternatywnym, jeżeli wymagają tego przepisy lub zasady równego dostępu.
3. Wnioskodawca może uzyskać pomoc w wypełnieniu wniosku bez utraty samodzielności i kontroli nad treścią oświadczeń.
4. Informacje należy przygotowywać w prostym języku, z możliwością powiększenia tekstu i obsługi technologii asystujących.

ROZDZIAŁ X. SKARGI, WNIOSKI I BEZSTRONNOŚĆ

§ 28. Skargi i sygnały

1. Ogłoszenie określa kanał składania skarg dotyczących procedury, konfliktu interesów, dostępności, naruszeń praw beneficjenta i nieprawidłowości w realizacji zadania.
2. Skargi rozpatruje osoba lub komórka niezaangażowana w czynność, której skarga dotyczy.
3. Skarga w programie nie wyłącza prawa do skorzystania ze środków prawnych przewidzianych w przepisach powszechnie obowiązujących.
4. Zgłoszenia nieprawidłowości są chronione przed odwetem w zakresie wynikającym z właściwych przepisów.

§ 29. Konflikt interesów

1. Osoby uczestniczące w ocenie, rozstrzyganiu, zawieraniu umów, płatnościach i kontroli ujawniają okoliczności mogące podważać ich bezstronność.
2. Osoba, której dotyczy konflikt interesów, nie uczestniczy w rozpatrywaniu sprawy ani w dostępie do dokumentacji wykraczającym poza niezbędny zakres.
3. Rejestr konfliktów interesów i podjętych środków prowadzi się w sposób zapewniający rozliczalność, z ochroną danych.

ROZDZIAŁ XI. PRZEPISY KOŃCOWE I WARUNKI URUCHOMIENIA

§ 30. Zmiany regulaminu

1. Regulamin programu może być zmieniany przez podmiot uprawniony na podstawie właściwej podstawy prawnej.
2. Zmiany nie mogą działać wstecz na niekorzyść beneficjenta ani naruszać praw nabytych, z uwzględnieniem przepisów powszechnie obowiązujących i warunków umów.
3. Zmiany ogłasza się w sposób zapewniający ich dostępność adresatom.

§ 31. Warunki rozpoczęcia działania jako państwowy fundusz celowy

Przed rozpoczęciem działalności Funduszu jako państwowego funduszu celowego należy:
1) uchwalić i ogłosić odrębną ustawę tworzącą Fundusz;
2) wskazać w ustawie ministra albo inny organ, który będzie dysponentem rachunku;
3) jeżeli przewidziano Dyspozytora działającego w zastępstwie ministra — nadać mu w ustawie wyraźną właściwość, kompetencje, tryb działania, odpowiedzialność i podległość;
4) określić ustawowe zadania, źródła przychodów, zasady darowizn celowych i ewidencjonowania wpłat;
5) zapewnić wyodrębniony rachunek bankowy oraz prawidłową obsługę księgową;
6) opracować i uzgodnić roczny plan finansowy w trybie przewidzianym ustawą o finansach publicznych;
7) określić tryb udzielania dotacji i podstawę prawną rozstrzygnięć;
8) zapewnić system kontroli zarządczej, audytu i sprawozdawczości;
9) przygotować wzory umów, formularzy, oświadczeń, zasad kwalifikowalności i zwrotów;
10) przeprowadzić analizę zgodności z przepisami o ochronie danych, zamówieniach publicznych, rachunkowości, pomocy publicznej i świadczeniach zdrowotnych — w zakresie, w jakim mają zastosowanie.

§ 32. Wejście w życie

1. Niniejszy dokument w zakresie projektu programu ma charakter projektowy i informacyjny.
2. Nie stanowi on samodzielnej podstawy tworzenia państwowego funduszu celowego, przyjmowania środków publicznych, udzielania dotacji ani wykonywania kompetencji organu administracji.
3. Postanowienia regulaminu mogą stać się regulaminem wykonawczym państwowego funduszu celowego po wejściu w życie Ustawy tworzącej, w zakresie zgodnym z jej przepisami, po formalnym przyjęciu przez właściwy organ i ogłoszeniu w przewidzianym prawem trybie.
4. W razie sprzeczności regulaminu z ustawą lub innym przepisem powszechnie obowiązującym stosuje się przepis wyższego rzędu.

PODSTAWY PRAWNE I ŹRÓDŁA

1. Konstytucja Rzeczypospolitej Polskiej, w szczególności zasady legalizmu, równości, ochrony godności i praw człowieka, oraz przepisy o źródłach prawa.
2. Ustawa z dnia 27 sierpnia 2009 r. o finansach publicznych, tekst jednolity ogłoszony obwieszczeniem Marszałka Sejmu z dnia 2 października 2026 r. (Dz. U. z 2026 r. poz. 1447), w szczególności art. 29, art. 33–34, art. 43–47 oraz przepisy dotyczące odpowiedzialności za gospodarkę finansową, kontroli i sprawozdawczości.
3. Ustawa z dnia 23 kwietnia 1964 r. — Kodeks cywilny, w szczególności przepisy o darowiźnie (art. 888 i nast.), w zakresie, w jakim mają zastosowanie do przyjmowania darowizn.
4. Rozporządzenie Parlamentu Europejskiego i Rady (UE) 2016/679 (RODO), w szczególności art. 5, 6, 9, 13, 25, 32 i 35.
5. Ustawa z dnia 11 września 2019 r. — Prawo zamówień publicznych, jeżeli dany podmiot i zamówienie podlegają jej przepisom.
6. Ustawa z dnia 29 września 1994 r. o rachunkowości oraz przepisy wykonawcze w zakresie właściwym dla prowadzenia ewidencji Funduszu.
7. Ustawy regulujące świadczenia zdrowotne, rehabilitację społeczną i zawodową, pomoc społeczną, wyroby medyczne i wykonywanie zawodów medycznych — odpowiednio do rodzaju zadania i statusu realizatora.

Tekst jednolity ustawy o finansach publicznych:
https://eli.gov.pl/api/acts/DU/2026/1447/text.html

Projekt należy przed użyciem operacyjnym poddać opinii legislacyjnej i finansowo-księgowej, w szczególności po ustaleniu docelowego modelu prawnego, podmiotu przyjmującego darowizny, źródła środków oraz projektu Ustawy tworzącej Fundusz.

}
