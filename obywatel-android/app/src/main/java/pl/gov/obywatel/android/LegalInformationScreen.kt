// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.weight
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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


REGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY W RAMACH ISTNIEJĄCEGO PAŃSTWOWEGO FUNDUSZU CELOWEGO

Status regulaminu i model prawny Celu

Niniejszy regulamin opisuje projekt wyodrębnionego Celu zadaniowego w ramach istniejącego państwowego funduszu celowego (dalej: „Fundusz”). Nie zakłada tworzenia nowego funduszu, nowej osoby prawnej ani nowego rachunku państwowego funduszu celowego. Celem jest finansowanie rehabilitacji, pielęgnacji, higieny i profilaktyki osób długotrwale chorych, osób z niepełnosprawnościami oraz osób w podeszłym wieku.

Projekt Celu może zostać opracowany i przedłożony ministrowi właściwemu dla Funduszu przez osobę fizyczną, osobę prawną albo jednostkę organizacyjną, której przepisy przyznają zdolność prawną. Wniosek powinien zawierać opis potrzeby publicznej, cele, grupę beneficjentów, katalog działań, kryteria przyznawania wsparcia, mierniki rezultatów, budżet, sposób kontroli i rozliczenia oraz propozycję osoby lub podmiotu odpowiedzialnego za realizację. Złożenie projektu nie oznacza jego przyjęcia; o dopuszczalności i trybie jego realizacji rozstrzyga minister lub inny właściwy organ w granicach ustawy regulującej Fundusz, jego zadań i planu finansowego.

Osoby fizyczne, osoby prawne i inne podmioty mogą deklarować lub dokonywać darowizn na wskazany Cel, o ile przepisy regulujące Fundusz dopuszczają przyjmowanie takich darowizn oraz wskazują prawidłowy rachunek i tryb ich przyjmowania. Wpłata musi być kierowana do uprawnionego odbiorcy na oficjalny rachunek, a nie na rachunek prywatny Dyspozytora. Darczyńca może wskazać przeznaczenie darowizny w granicach prawa i warunków jej przyjęcia. Samo wskazanie osoby przez darczyńcę nie przenosi na nią kompetencji do dysponowania rachunkiem państwowego funduszu celowego ani nie wiąże organu publicznego wbrew ustawie.

Zgodnie z art. 29 ust. 3 ustawy o finansach publicznych rachunkiem państwowego funduszu celowego dysponuje minister wskazany w ustawie tworzącej Fundusz albo inny organ wskazany w tej ustawie. Jednocześnie art. 29 ust. 7 przewiduje wyodrębnianie w planie finansowym zadań finansowanych ze środków Funduszu. W konsekwencji Cel może być ujmowany jako zadanie Funduszu, jeżeli mieści się w jego ustawowym zakresie, zostanie zaakceptowany przez właściwy organ i prawidłowo ujęty w planie finansowym. Sam regulamin ani wola darczyńców nie mogą rozszerzyć ustawowych zadań Funduszu.

W regulaminie określenie „Dyspozytor Celu” oznacza obywatela, który z własnej inicjatywy rozpoznaje potrzebę społeczną, określa Cel, ogłasza jego ustanowienie i podejmuje się jego prowadzenia. W znaczeniu organizacyjnym projektu obywatel obejmuje tę funkcję z chwilą przyjęcia odpowiedzialności za Cel; nie musi oczekiwać na przyznanie dotacji ani na formalne powierzenie funkcji przez ministra, aby rozpocząć działania finansowane ze środków własnych. Odrębnie należy określić zakres jego prawnego umocowania do przyjmowania, wykorzystywania i rozliczania środków publicznych. W odniesieniu do środków PFC Dyspozytor wykonuje czynności finansowe i wydaje wiążące dyspozycje wyłącznie w zakresie wynikającym z ustawy, aktu właściwego organu, umowy lub innej skutecznej podstawy prawnej. Sama nazwa funkcji nie przenosi kompetencji ustawowego dysponenta Funduszu.

Projekt zakłada, że Dyspozytor Celu odpowiada za merytoryczne prowadzenie Celu, przygotowanie naborów, ocenę potrzeb, rekomendowanie lub podejmowanie rozstrzygnięć w zakresie powierzonych kompetencji, nadzór nad realizatorami, monitorowanie efektów i rozliczenie zadania. Zakres ten musi zostać skonkretyzowany w akcie właściwego organu i nie może obejmować kompetencji zastrzeżonych ustawowo dla ministra lub dysponenta Funduszu, jeżeli przepisy nie pozwalają na ich przekazanie.

Niniejszy dokument jest projektem regulaminu Celu. Może być stosowany operacyjnie dopiero po zatwierdzeniu przez organ właściwy dla Funduszu, potwierdzeniu dopuszczalności darowizn i zakresu zadania, ujęciu zadania w planie finansowym oraz ustanowieniu Dyspozytora w prawnie skutecznym trybie. Nie stanowi samodzielnej podstawy przyjmowania wpłat ani wydatkowania środków publicznych.

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
1) Funduszu — rozumie się przez to istniejący państwowy fundusz celowy, w ramach którego właściwy organ może zaakceptować i finansować Cel, w granicach ustawy regulującej Fundusz;
2) Ustawie regulującej Fundusz — rozumie się przez to obowiązującą ustawę ustanawiającą i regulującą dany państwowy fundusz celowy, określającą jego dysponenta, zadania, źródła przychodów i zasady gospodarki finansowej;
3) Dysponencie Funduszu — rozumie się przez to ministra albo inny organ wskazany w Ustawie regulującej Fundusz, któremu art. 29 ustawy o finansach publicznych oraz Ustawa tworząca powierzają kompetencję dysponowania rachunkiem Funduszu;
4) Dyspozytorze — rozumie się przez to osobę lub organ, któremu Ustawa tworząca bezpośrednio albo na podstawie wyraźnego upoważnienia powierzy wykonywanie określonych czynności w zakresie rozdysponowania środków; sama nazwa „dyspozytor” nie tworzy kompetencji publicznoprawnej;
5) Dotacji — rozumie się przez to bezzwrotne środki publiczne udzielane na podstawie właściwego przepisu prawa i umowy lub aktu stanowiącego podstawę finansowania, na realizację określonego zadania;
6) Darowiźnie celowej — rozumie się przez to przysporzenie dokonane przez darczyńcę z oznaczeniem przeznaczenia, przyjęte przez podmiot uprawniony na warunkach zgodnych z prawem; darowizna nie jest automatycznie dotacją ani środkiem publicznym;
7) Beneficjencie — rozumie się przez to osobę lub podmiot, któremu zgodnie z Ustawą tworzącą i regulaminem przyznano środki albo na którego rzecz finansowane jest zadanie;
8) Realizatorze — rozumie się przez to podmiot wykonujący zadanie finansowane ze środków programu;
9) Zadaniu — rozumie się przez to wyodrębnione przedsięwzięcie opisane we wniosku, z określonym celem, zakresem rzeczowym, harmonogramem, budżetem i miernikami efektów;
10) Kosztach kwalifikowalnych — rozumie się przez to wydatki niezbędne, racjonalne, udokumentowane i bezpośrednio związane z zadaniem, dopuszczone w ogłoszeniu i zgodne z przepisami.

§ 3. Zasady podstawowe

1. Środki Funduszu mogą być wykorzystywane wyłącznie na cele i w ramach limitów określonych w Ustawie regulującej Fundusz oraz rocznym planie finansowym.
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

1. Podstawą gospodarki finansowej państwowego funduszu celowego jest roczny plan finansowy sporządzany i zmieniany w trybie określonym w ustawie o finansach publicznych i Ustawie regulującej Fundusz.
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

1. Dysponentem Funduszu jest organ wskazany w Ustawie regulującej Fundusz.
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

1. Środki przekazuje się wyłącznie na podstawie właściwej podstawy prawnej, w szczególności decyzji, umowy lub innego aktu przewidzianego w Ustawie regulującej Fundusz.
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

1. Środki niewykorzystane, pobrane nienależnie, w nadmiernej wysokości lub wykorzystane niezgodnie z przeznaczeniem podlegają zwrotowi na zasadach i w trybie określonym w ustawie o finansach publicznych, Ustawie regulującej Fundusz i właściwych przepisach szczególnych.
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

§ 31. Warunki uruchomienia Celu w ramach istniejącego Funduszu

Przed rozpoczęciem przyjmowania darowizn i finansowania zadań w ramach Celu należy:
1) wskazać konkretny państwowy fundusz celowy oraz obowiązującą ustawę regulującą ten Fundusz;
2) potwierdzić, że ustawowe zadania Funduszu obejmują działania odpowiadające Celowi albo że istnieje prawna możliwość ich objęcia;
3) potwierdzić podstawę prawną i tryb przyjmowania darowizn, w tym darowizn z oznaczeniem Celu, wskazać uprawnionego odbiorcę oraz oficjalny rachunek;
4) przedłożyć ministrowi lub innemu właściwemu organowi projekt Celu wraz z budżetem, kryteriami, wskaźnikami, zasadami kontroli i rozliczenia;
5) uzyskać formalną akceptację Celu przez właściwy organ i ująć zadanie w planie finansowym Funduszu w trybie przewidzianym prawem;
6) określić sposób ewidencjonowania darowizn oraz ich przypisania do Celu, bez tworzenia nieprzewidzianych prawem rachunków lub subfunduszy;
7) ustanowić Dyspozytora Celu w prawnie skutecznym trybie, określając zakres zadań, upoważnień, odpowiedzialności, nadzoru i zastępstwa;
8) określić, które rozstrzygnięcia podejmuje minister lub dysponent Funduszu, a które może podejmować Dyspozytor;
9) ustalić podstawę prawną udzielania dotacji, katalog uprawnionych beneficjentów i realizatorów, procedurę naboru, wzory umów i zasady zwrotu środków;
10) zapewnić obsługę księgową, kontrolę zarządczą, audyt, sprawozdawczość, ochronę danych i dostępność procedur.

§ 32. Wejście w życie

1. Niniejszy dokument opisuje projekt Celu w ramach istniejącego państwowego funduszu celowego i ma charakter projektowy do czasu jego zatwierdzenia przez właściwy organ.
2. Nie stanowi on samodzielnej podstawy przyjmowania darowizn przez Fundusz, udzielania dotacji ani wykonywania kompetencji organu administracji.
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

Projekt należy przed użyciem operacyjnym poddać opinii legislacyjnej i finansowo-księgowej, po wskazaniu konkretnego Funduszu, podstawy przyjmowania darowizn, organu zatwierdzającego Cel oraz prawnego umocowania Dyspozytora.


PODSTAWY PRAWNE WSPARCIA MODELU OBYWATELSKIEGO — ANALIZA PRZEPISÓW

Poniższe przepisy wspierają możliwość zgłoszenia Celu, ubiegania się o środki, realizacji zadania przez podmiot spoza administracji oraz przyjmowania darowizn. Należy odróżnić uprawnienie do ubiegania się o realizację zadania od kompetencji do samodzielnego dysponowania rachunkiem państwowego funduszu celowego.

1. Art. 43 ustawy o finansach publicznych stanowi, że prawo realizacji zadań finansowanych ze środków publicznych przysługuje ogółowi podmiotów, chyba że odrębne ustawy stanowią inaczej. Jest to istotna podstawa obywatelskiego modelu zgłaszania się do realizacji zadań publicznych. Podmiot spoza sektora finansów publicznych może ubiegać się o realizację zadania, o ile przepisy szczególne nie ustanawiają ograniczeń, a wybór i finansowanie następują w odpowiednim trybie.

2. Art. 47 ustawy o finansach publicznych przewiduje, że podmiot wnioskujący o przyznanie środków publicznych na realizację wyodrębnionego zadania przedstawia ofertę wykonania zadania zgodnie z zasadami uczciwej konkurencji, gwarantującą wykonanie efektywne, oszczędne i terminowe. Przepis wspiera konstrukcję wniosku o finansowanie konkretnego Celu wraz z budżetem, harmonogramem i sposobem rozliczenia. Nie ustanawia automatycznego prawa do dotacji ani nie przesądza, że wnioskodawca staje się dysponentem rachunku Funduszu.

3. Art. 29 ust. 2, 3, 5, 6 i 7 ustawy o finansach publicznych określa status państwowego funduszu celowego, wskazuje organ dysponujący rachunkiem oraz przewiduje wyodrębnianie zadań w rocznym planie finansowym. Cel może być ujęty jako zadanie Funduszu, jeżeli mieści się w jego ustawowym zakresie i zostanie prawidłowo uwzględniony w planie finansowym. Brak osobowości prawnej Funduszu nie znosi jednak kompetencji organu wskazanego w art. 29 ust. 3. Sam art. 29 nie przyznaje każdemu obywatelowi prawa do samodzielnego tworzenia subkonta ani dysponowania częścią rachunku.

4. Art. 44 ustawy o finansach publicznych wymaga, aby wydatki publiczne były dokonywane w granicach planu finansowego, zgodnie z przepisami dotyczącymi poszczególnych rodzajów wydatków, w sposób celowy i oszczędny oraz zapewniający najlepsze efekty z danych nakładów. Uzasadnia to budżet Celu, mierniki rezultatów, dokumentowanie wydatków i kontrolę.

5. Art. 63 Konstytucji RP przyznaje każdemu prawo składania petycji, wniosków i skarg w interesie publicznym, własnym lub innej osoby za jej zgodą do organów władzy publicznej. Stanowi podstawę skierowania do ministra propozycji ustanowienia Celu, przedstawienia uzasadnienia i zwrócenia się o rozpatrzenie wniosku. Nie oznacza obowiązku przyjęcia propozycji ani przyznania żądanej kwoty.

6. Art. 60 Konstytucji RP zapewnia obywatelom polskim korzystającym z pełni praw publicznych prawo dostępu do służby publicznej na jednakowych zasadach. Potwierdza zasadę równego dostępu, lecz nie jest samodzielnym tytułem do objęcia konkretnej funkcji dysponenta Funduszu ani do zarządzania rachunkiem państwowym.

7. Art. 11 ustawy o działalności pożytku publicznego i o wolontariacie przewiduje wspieranie lub powierzanie realizacji zadań publicznych organizacjom pozarządowym oraz podmiotom wymienionym w art. 3 ust. 3, prowadzącym działalność statutową w danej dziedzinie. Co do zasady następuje to w trybie konkursu ofert albo w trybach szczególnych przewidzianych ustawą. Przepisy te mogą mieć zastosowanie, jeżeli Wnioskodawca lub realizator spełnia ustawowe warunki.

8. Art. 888 i następne Kodeksu cywilnego regulują umowę darowizny. W relacji prywatnoprawnej darczyńca może przekazać środki na uzgodniony cel, a treść umowy może określać przeznaczenie świadczenia i obowiązki obdarowanego. Powierzenie konkretnej osobie zarządzania darowizną wymaga prawidłowego określenia stron, odbiorcy środków, treści zobowiązania i rachunku. Zasady te nie zmieniają ustawowego dysponenta rachunku państwowego funduszu celowego.

9. Ostateczna możliwość przyjmowania darowizn na rachunek konkretnego Funduszu, przypisywania ich do oznaczonego zadania, tworzenia rachunków pomocniczych, udzielania dotacji i powierzania czynności Dyspozytorowi wymaga sprawdzenia ustawy tworzącej dany Fundusz, przepisów wykonawczych, planu finansowego oraz zasad obsługi rachunku. Ustawa szczególna może przewidywać konkursy, nabory, odrębny rachunek obsługujący dotację i szczegółowe rozliczenia — rozwiązania te obowiązują w granicach tej ustawy.

WNIOSKI DLA PROJEKTU

• Obywatelskie zgłoszenie Celu i wniosek o jego finansowanie można oprzeć przede wszystkim na art. 63 Konstytucji RP oraz art. 43 i 47 ustawy o finansach publicznych.
• Wnioskodawca może wskazać siebie jako proponowanego realizatora i Dyspozytora Celu oraz wnosić o powierzenie konkretnych czynności w dopuszczalnym trybie.
• Konstrukcję odrębnego zadania można oprzeć na art. 29 ust. 5–7 ustawy o finansach publicznych, pod warunkiem zgodności Celu z ustawą regulującą Fundusz i planem finansowym.
• Darowizny prywatne można organizować w reżimie Kodeksu cywilnego, z wyraźnym wskazaniem celu i osoby odpowiedzialnej za ich wykorzystanie, przy prawidłowym określeniu odbiorcy i rachunku.
• Z samych art. 29 ust. 2 ani art. 43 ustawy o finansach publicznych nie wynika bezwarunkowe prawo każdego obywatela do samodzielnego otwarcia subkonta państwowego Funduszu i wydawania zgromadzonych na nim środków. Taki skutek wymaga odpowiedniej podstawy w ustawie regulującej Fundusz lub prawnie skutecznego działania organu uprawnionego do dysponowania rachunkiem.

ŹRÓDŁA URZĘDOWE

• Ustawa o finansach publicznych, tekst jednolity Dz.U. 2026 poz. 1447: https://eli.gov.pl/api/acts/DU/2026/1447/text.html
• Konstytucja RP, w szczególności art. 60 i 63: https://www.prezydent.pl/prawo/konstytucja-rp/rozdzial-ii-wolnosci-prawa-i-obowiazki-czlowieka-i-obywatela
• Ustawa o działalności pożytku publicznego i o wolontariacie, tekst jednolity Dz.U. 2025 poz. 1338, z późniejszymi zmianami: https://eli.gov.pl/eli/DU/2025/1338/ogl
• Kodeks cywilny, w szczególności art. 888 i następne: https://eli.gov.pl/eli/DU/1964/93/ogl

Stan prawny i teksty źródeł należy ponownie sprawdzić w dniu składania wniosku, z uwzględnieniem późniejszych nowelizacji i ustawy regulującej konkretny Fundusz.


UZUPEŁNIAJĄCA ANALIZA USTAWY O FINANSACH PUBLICZNYCH — DODATKOWE PRZEPISY

Poniższe przepisy uzupełniają podstawy prawne projektu. Należy je odczytywać łącznie z art. 29, art. 43, art. 44 i art. 47 ustawy oraz z ustawą regulującą konkretny państwowy fundusz celowy.

10. Art. 4 ust. 1 pkt 2 — stosowanie ustawy do podmiotów spoza sektora finansów publicznych
Ustawa ma zastosowanie także do innych podmiotów w zakresie, w jakim wykorzystują środki publiczne lub nimi dysponują. Potwierdza to możliwość udziału podmiotu spoza sektora w realizacji zadania finansowanego publicznie, ale oznacza zarazem podleganie właściwym regułom finansowym, rozliczeniowym i kontrolnym. Nie jest to samodzielna podstawa przyznania dotacji ani przekazania uprawnień do rachunku Funduszu.

11. Art. 126 — dotacje z państwowych funduszy celowych na zadania publiczne
Art. 126 zalicza do dotacji środki z państwowych funduszy celowych przeznaczone na podstawie ustawy, odrębnych ustaw lub umów międzynarodowych na finansowanie albo dofinansowanie realizacji zadań publicznych. Jest to bezpośrednia podstawa konstrukcji dotacji pochodzącej z PFC. Konieczne pozostaje wskazanie przepisu pozwalającego danemu Funduszowi finansować konkretny rodzaj zadania i zachowanie właściwego trybu.

12. Art. 127 ust. 1 — katalog celów dotacji celowych
Art. 127 ust. 1 obejmuje między innymi finansowanie lub dofinansowanie ustawowo określonych zadań realizowanych przez jednostki inne niż jednostki samorządu terytorialnego (pkt 1 lit. b), zadań zleconych organizacjom pozarządowym (pkt 1 lit. e) oraz kosztów inwestycji (pkt 1 lit. f). Wspiera to model wykonywania zadania publicznego przez podmiot inny niż administracja, w tym organizację pozarządową, w granicach właściwych przepisów. Nie oznacza, że dowolny cel zdrowotno-opiekuńczy automatycznie staje się zadaniem danego Funduszu.

13. Art. 150 — umowa dotacji celowej
Jeżeli odrębne przepisy lub umowa międzynarodowa nie określają trybu i zasad udzielania lub rozliczania dotacji, art. 150 przewiduje umowę określającą w szczególności opis zadania i cel dotacji, termin wykonania, wysokość środków, termin wykorzystania, sposób rozliczenia, zwrot niewykorzystanej części oraz tryb kontroli. Dla środków PFC trzeba uwzględnić odesłanie do dysponenta środków, o których mowa w art. 127 ust. 2, oraz ustawę regulującą konkretny Fundusz.

14. Art. 151 — zlecenie zadania organizacji pozarządowej wraz z dotacją
Art. 151 ust. 1 stanowi, że dysponent części budżetowej może zlecić organizacji pozarządowej realizację swoich zadań na podstawie umowy, przyznając jednocześnie dotację celową. Umowa określa m.in. zadanie i jego cel, wysokość dotacji, tryb płatności, termin wykorzystania, kontrolę, rozliczenie i zwrot niewykorzystanych środków. Jest to istotna podstawa dla realizacji projektu przez Fundację Wspierania Rodzin „Aria”, o ile Fundacja spełnia definicję organizacji pozarządowej, zadanie należy do zadań właściwego dysponenta, a art. 151 znajduje zastosowanie. Przepis nie daje osobie fizycznej prawa do bezpośredniego dysponowania rachunkiem PFC. Osobista funkcja Wnioskodawcy jako koordynatora lub Dyspozytora wymaga odrębnego, zgodnego z prawem umocowania.

15. Art. 152 — wyodrębniona ewidencja dotacji i rozliczenie
Art. 152 ust. 1 nakłada na jednostki, którym udzielono dotacji na podstawie art. 150 lub art. 151 ust. 1, obowiązek prowadzenia wyodrębnionej ewidencji księgowej środków otrzymanych z dotacji i wydatków z nich finansowanych. Ustępy 2–3 regulują zatwierdzanie rozliczenia rzeczowego i finansowego oraz ustalanie kwoty podlegającej zwrotowi. Użyte w niniejszym regulaminie określenie „subkonto Celu” oznacza wyłącznie subkonto ewidencyjne (konto analityczne albo wyodrębniony segment ewidencji księgowej) prowadzone w ramach ksiąg rachunkowych Funduszu. Służy ono przypisywaniu wpływów, zobowiązań, kosztów i rozliczeń do konkretnego Celu oraz zachowaniu ścieżki audytu. Nie oznacza odrębnego rachunku bankowego ani samodzielnego rachunku płatniczego w rozumieniu przepisów o usługach płatniczych. Subkonto ewidencyjne nie tworzy odrębnego majątku, nie zmienia ustawowego dysponenta Funduszu i nie daje samoistnego prawa do autoryzowania płatności. Jego utworzenie w systemie księgowym, nadanie identyfikatora oraz określenie osób uprawnionych do wprowadzania, zatwierdzania i kontroli zapisów powinno nastąpić w ramach zasad rachunkowości i uprawnień właściwej jednostki.

16. Art. 40 — rachunkowość Funduszu
Art. 40 określa obowiązek prowadzenia rachunkowości przez jednostki sektora finansów publicznych z uwzględnieniem szczególnych zasad ustawy. Wspiera postulat dokumentacji pozwalającej odtworzyć źródło, przeznaczenie i przepływ każdej kwoty, ale nie ustanawia samodzielnego uprawnienia Dyspozytora do rachunku.

17. Art. 52 — limity planu finansowego
Art. 52 rozróżnia prognozowany charakter przychodów i nieprzekraczalny limit wydatków oraz reguluje zmiany planu finansowego. Cel powinien być opisany kwotowo i zadaniowo w granicach właściwego planu; nie wolno zaciągać zobowiązań ponad dopuszczalne limity. Przepis wspiera formalne wyodrębnienie Celu w planie, ale sam nie przyznaje wnioskodawcy prawa do dokonania wpisu.

18. Art. 53 i art. 54 — odpowiedzialność i dyspozycje pieniężne
Art. 53 ust. 1 przypisuje kierownikowi jednostki odpowiedzialność za całość gospodarki finansowej, a ust. 2 pozwala powierzyć określone obowiązki pracownikom jednostki w formie imiennego upoważnienia lub wskazania w regulaminie organizacyjnym. Art. 54 określa zadania głównego księgowego, w tym wykonywanie dyspozycji środkami pieniężnymi i wstępną kontrolę operacji. Przepisy wspierają rozdzielenie prowadzenia merytorycznego Celu, zatwierdzania wydatków, księgowania i autoryzacji płatności. Art. 53 ust. 2 nie jest ogólną podstawą do nadania dowolnej osobie zewnętrznej prawa dysponowania rachunkiem PFC.

19. Art. 68 i art. 69 — kontrola zarządcza i zarządzanie ryzykiem
Art. 68 definiuje kontrolę zarządczą jako działania służące realizacji celów i zadań zgodnie z prawem, efektywnie, oszczędnie i terminowo; obejmuje m.in. wiarygodność sprawozdań, ochronę zasobów i zarządzanie ryzykiem. Art. 69 określa osoby odpowiedzialne za zapewnienie kontroli zarządczej. Przepisy uzasadniają mierniki rezultatów, kontrolę dokumentów, rozdział ról, rejestr ryzyk, procedury konfliktu interesów i monitorowanie Celu, lecz nie tworzą stanowiska Dyspozytora ani nie przenoszą ustawowej odpowiedzialności ministra lub kierownika jednostki.

20. Art. 33 — jawność gospodarowania środkami publicznymi
Art. 33 ustanawia zasadę jawności gospodarki środkami publicznymi. Wspiera publikowanie informacji o zasadach programu, podstawach prawnych, kwotach, wynikach naborów, sprawozdaniach i efektach Celu, z poszanowaniem ochrony danych i tajemnic prawnie chronionych. Jawność nie daje osobom prywatnym kompetencji do wydawania środków.

DODATKOWE WNIOSKI DLA MODELU
1. Najmocniejszym uzupełnieniem art. 43 i 47 jest zestawienie art. 126, art. 127 ust. 1, art. 150, art. 151 i art. 152: przepisy te opisują konstrukcję dotacji z PFC na zadanie publiczne, możliwość zlecenia zadania uprawnionej organizacji pozarządowej, umowne określenie celu i kontroli oraz obowiązek wyodrębnionej ewidencji.
2. Art. 4 ust. 1 pkt 2 potwierdza, że podmiot spoza sektora, który wykorzystuje środki publiczne lub nimi dysponuje, podlega ustawie w odpowiednim zakresie.
3. Wniosek może proponować Wnioskodawcę jako realizatora, a wskazaną osobę jako koordynatora lub Dyspozytora Celu w granicach kompetencji wyraźnie powierzonych przez właściwy organ i umowę. Należy odróżnić Fundację jako stronę umowy i beneficjenta dotacji od osobistej funkcji Wnioskodawcy.
4. Art. 152 uzasadnia wyodrębnioną ewidencję księgową, ale nie prawo do samodzielnego otwarcia subkonta bankowego.
5. Żaden z tych przepisów samodzielnie nie znosi art. 29 ust. 3 ani nie przenosi na Wnioskodawcę kompetencji ustawowego dysponenta rachunku PFC. Zakres czynności Dyspozytora wymaga formalnego ustanowienia przez organ właściwy.

Urzędowe źródło tekstu jednolitego ustawy o finansach publicznych, Dz.U. z 2026 r. poz. 1447:
https://eli.gov.pl/api/acts/DU/2026/1447/text.pdf
https://eli.gov.pl/api/acts/DU/2026/1447/text.html



UZUPEŁNIAJĄCA ANALIZA — EWIDENCJA CELU, DAROWIZNY I ROZLICZANIE ŚRODKÓW

Korekta terminologiczna dotycząca subkonta
Wnioskodawca posługuje się pojęciem „subkonto” w znaczeniu subkonta ewidencyjnego, czyli wyodrębnionej analitycznej ewidencji księgowej Celu w ramach ksiąg rachunkowych Funduszu. Nie chodzi o otwieranie dodatkowego rachunku bankowego ani o tworzenie odrębnego rachunku płatniczego. W dokumentacji księgowej subkonto powinno pozwalać na oddzielne identyfikowanie wpływów z poszczególnych źródeł, zapisów dotyczących dotacji i darowizn, zobowiązań, wydatków, kosztów, zwrotów, salda oraz dokumentów źródłowych. Powinno być prowadzone jako konto analityczne powiązane z właściwym kontem księgi głównej, zgodnie z zakładowym planem kont i zasadami rachunkowości jednostki prowadzącej księgi Funduszu.

21. Art. 3 — gromadzenie, rozdysponowywanie i zarządzanie środkami
Art. 3 ujmuje finanse publiczne jako proces obejmujący gromadzenie dochodów i przychodów, wydatkowanie, zaciąganie zobowiązań oraz zarządzanie środkami publicznymi. Wspiera to całościowe ujęcie Celu: od przyjęcia i zakwalifikowania wpływu, przez jego ewidencję i przypisanie do zadania, aż po wydatkowanie i rozliczenie. Sam przepis nie ustanawia jednak odrębnej kompetencji do dysponowania środkami Funduszu.

22. Art. 5 ust. 2 pkt 5 — darowizny pieniężne jako dochody publiczne
Przepis zalicza do dochodów publicznych spadki, zapisy i darowizny w postaci pieniężnej na rzecz jednostek sektora finansów publicznych. Jest to ważna podstawa dla rozważenia darowizn pieniężnych na rzecz właściwego podmiotu publicznego, z oznaczeniem celu i ich prawidłowym ujęciem w księgach. Nie oznacza to jednak, że każda darowizna przekazana osobie prywatnej lub dowolnej organizacji staje się dochodem PFC. Trzeba prawidłowo ustalić obdarowanego, tytuł prawny przyjęcia darowizny, możliwość jej przyjęcia przez dany Fundusz oraz zasady jej ujęcia w planie i księgach.

23. Art. 11a — ustawowy przykład wyodrębniania darowizn i przeznaczania ich zgodnie z wolą darczyńcy
Art. 11a przewiduje dla określonych państwowych jednostek budżetowych prowadzących działalność oświatową wydzielony rachunek dochodów, na którym mogą być gromadzone między innymi pieniężne darowizny; przepis wskazuje też, że dochody te mogą być przeznaczane na cele wskazane przez darczyńcę, a decyzję o utworzeniu rachunku podejmuje kierownik jednostki po uzyskaniu wymaganej zgody. Jest to konkretny przykład ustawowego modelu wyodrębnienia środków pochodzących z darowizn i respektowania celu określonego przez darczyńcę. Zakres art. 11a jest jednak ograniczony do wskazanych w nim jednostek i działalności oświatowej; nie stanowi on samodzielnej podstawy do stosowania takiego rachunku w każdym PFC. Dla niniejszego projektu ma znaczenie jako wzorzec konstrukcyjny i argument za wyraźnym uregulowaniem analogicznego mechanizmu w przepisach właściwych dla Funduszu.

24. Art. 39 — klasyfikacja dochodów, wydatków i przychodów
Art. 39 nakazuje klasyfikowanie dochodów publicznych, wydatków publicznych i określonych przychodów według działów, rozdziałów i paragrafów. W połączeniu z art. 29 ust. 7, art. 40 i art. 40a pozwala uzasadnić takie zaprojektowanie ewidencji analitycznej Celu, aby zapisy były zgodne z obowiązującą klasyfikacją i jednocześnie umożliwiały identyfikację konkretnego zadania. Subkonto analityczne nie zastępuje klasyfikacji budżetowej ani kont księgi głównej.

25. Art. 40 ust. 1–4 oraz art. 40a — plany kont, etapy rozliczeń i jednolita ewidencja
Art. 40 ust. 1 nakazuje jednostkom sektora finansów publicznych prowadzić rachunkowość zgodnie z przepisami o rachunkowości i ustawą o finansach publicznych. Ust. 2 pkt 2 wymaga, aby plany kont uwzględniały również etapy rozliczeń poprzedzające płatność dochodów i wydatków, a w zakresie wydatków i kosztów także zaangażowanie środków. Art. 40 ust. 4 stanowi podstawę określenia szczególnych zasad rachunkowości i planów kont. Art. 40a przewiduje wytyczne dotyczące przypisywania paragrafów klasyfikacji do kont księgowych w celu jednolitego ewidencjonowania gospodarowania środkami publicznymi. To przepisy szczególnie istotne dla postulowanego subkonta ewidencyjnego Celu: pozwalają oprzeć jego konstrukcję na planie kont, ewidencji analitycznej i ścieżce audytu, a nie na tworzeniu nowego rachunku bankowego.

26. Art. 41 — sprawozdawczość z wykonania procesów finansowych
Art. 41 przewiduje sporządzanie przez jednostki sektora finansów publicznych sprawozdań z wykonania procesów wskazanych w art. 3. Wspiera to postulat przygotowywania okresowych zestawień wykonania Celu, obejmujących wpływy, zaangażowanie środków, wydatki, koszty, rezultaty i saldo ewidencyjne, w zakresie i formie zgodnej z przepisami sprawozdawczymi właściwymi dla jednostki.

27. Art. 162 pkt 4 — zlecanie zadań, art. 43 i art. 151
Art. 162 pkt 4 stanowi, że w toku wykonywania budżetu państwa zlecanie zadań powinno następować na zasadzie wyboru najkorzystniejszej oferty, a w odniesieniu do organizacji pozarządowych — z odpowiednim zastosowaniem art. 43 i art. 151. Jest to dodatkowe, bezpośrednie powiązanie prawa realizacji zadań finansowanych publicznie z mechanizmem zlecania zadań organizacjom pozarządowym. Wzmacnia argument, że zadanie publiczne może być wykonywane przez podmiot spoza administracji, z zachowaniem właściwego trybu wyboru i umowy.

28. Art. 168 i art. 169 — wykorzystanie dotacji i obowiązek zwrotu
Art. 168 reguluje zwrot niewykorzystanych dotacji z budżetu państwa, a art. 169 — zwrot dotacji wykorzystanych niezgodnie z przeznaczeniem, nienależnie pobranych lub pobranych w nadmiernej wysokości, wraz z odpowiednimi odsetkami. Przepisy te wzmacniają potrzebę prowadzenia subkonta ewidencyjnego, które umożliwia ustalenie kwoty otrzymanej, wykorzystanej, niewykorzystanej i podlegającej zwrotowi. Do dotacji z PFC należy stosować właściwe przepisy dotyczące danego rodzaju dotacji i Funduszu, z uwzględnieniem zakresu zastosowania art. 168–169.

29. Art. 174 i art. 175 — nadzór, kontrola i ocena efektów
Art. 174 określa ogólną kontrolę Ministra Finansów nad finansami publicznymi, natomiast art. 175 reguluje nadzór i kontrolę dysponentów części budżetowych, w tym nad wykorzystaniem dotacji, realizacją zadań finansowanych z budżetu państwa oraz efektywnością i skutecznością realizacji planów w układzie zadaniowym. Przepisy te wspierają jawny system raportowania, mierniki realizacji Celu, dokumentowanie rezultatów i możliwość kontroli. Nie przenoszą jednak ustawowych kompetencji kontrolnych na prywatnego koordynatora.

DOPRECYZOWANIE STATUSU WNIOSKODAWCY I OKREŚLENIA „POSEŁ”
Wnioskodawca używa określenia „Poseł” w przyjętym przez siebie znaczeniu obywatelskiego powołania do służby interesowi publicznemu. W niniejszym projekcie jest to osobiste określenie misji i roli obywatelskiej, a nie twierdzenie o posiadaniu mandatu parlamentarnego ani o wykonywaniu konstytucyjnego urzędu posła na Sejm. Samo obywatelskie powołanie, używanie takiego określenia lub działanie w interesie publicznym nie zastępuje ustawowego umocowania do dysponowania środkami publicznymi. Nie ogranicza natomiast prawa obywatela do zgłaszania inicjatyw, składania wniosków i proponowania sposobu realizacji zadań publicznych na zasadach określonych prawem.

ZAKTUALIZOWANE WNIOSKI DLA MODELU
1. Art. 29 ust. 6–7 uzasadnia wyodrębnienie zadań finansowanych z PFC w rocznym planie finansowym. Art. 39, art. 40 i art. 40a uzasadniają prowadzenie szczegółowej ewidencji analitycznej Celu w ramach ksiąg Funduszu.
2. „Subkonto Celu” w tym projekcie oznacza subkonto ewidencyjne, a nie rachunek bankowy. Jego funkcją jest identyfikacja księgowa wpływów, zobowiązań, kosztów i rozliczeń przypisanych do Celu.
3. Art. 5 ust. 2 pkt 5 i art. 11a pokazują ustawowe uznanie darowizn pieniężnych na rzecz sektora finansów publicznych oraz przykład respektowania celu wskazanego przez darczyńcę w szczególnym modelu rachunku wydzielonego. Możliwość zastosowania tego mechanizmu w konkretnym PFC wymaga jednak sprawdzenia ustawy tworzącej Fundusz i zasad przyjmowania darowizn.
4. Art. 126, art. 127, art. 150–152 oraz art. 162 pkt 4 wspierają konstrukcję zadania publicznego finansowanego dotacją i realizowanego przez uprawniony podmiot, z umową, ewidencją i rozliczeniem.
5. Wnioskodawca może proponować swoją rolę jako inicjatora, koordynatora lub osoby odpowiedzialnej za merytoryczne prowadzenie Celu. Zakres formalnych uprawnień do zatwierdzania zapisów, podejmowania zobowiązań lub autoryzowania płatności musi wynikać z właściwego umocowania i procedur Funduszu.
6. Obywatelskie rozumienie określenia „Poseł” jest w tym dokumencie opisem osobistej misji służby publicznej, a nie deklaracją mandatu parlamentarnego.

Urzędowy tekst jednolity ustawy o finansach publicznych: Dz. U. z 2026 r. poz. 1447, stan prawny uwzględniony w obwieszczeniu z 2 października 2026 r.:
https://eli.gov.pl/api/acts/DU/2026/1447/text.pdf
https://eli.gov.pl/api/acts/DU/2026/1447/text.html

UZUPEŁNIAJĄCA ANALIZA — OSOBA FIZYCZNA JAKO PRYWATNY PODMIOT ZARZĄDZAJĄCY ŚRODKAMI NA CEL PUBLICZNY

30. Art. 4 ust. 1 pkt 2 — podmiot prywatny wykorzystujący lub dysponujący środkami publicznymi
Przepis stanowi, że ustawę o finansach publicznych stosuje się nie tylko do jednostek sektora finansów publicznych, lecz także do „innych podmiotów” w zakresie, w jakim wykorzystują środki publiczne lub nimi dysponują. Jest to istotna podstawa dla modelu, w którym osoba fizyczna pozostaje podmiotem prywatnym, ale na podstawie umowy, decyzji lub innego skutecznego tytułu prawnego wykonuje określone czynności dotyczące środków publicznych. Ustawa obejmuje wówczas tę osobę właściwymi regułami gospodarowania, rozliczania i kontroli. Przepis nie przesądza jednak, że taka osoba staje się przez to jednostką sektora finansów publicznych; przeciwnie, rozróżnia jednostki sektora od innych podmiotów. W projekcie należy zatem odróżnić status „prywatnego podmiotu zarządzającego lub realizatora zadania publicznego” od statusu „jednostki sektora finansów publicznych”.

31. Art. 8 i art. 9 — ustawowe formy jednostek sektora finansów publicznych
Art. 8 stanowi, że jednostki sektora finansów publicznych są tworzone w formach określonych w ustawie, a art. 9 wymienia kategorie podmiotów tworzących ten sektor. W katalogu znajdują się między innymi organy władzy publicznej, jednostki samorządu terytorialnego, jednostki budżetowe, państwowe fundusze celowe, agencje wykonawcze, uczelnie publiczne, publiczne zakłady opieki zdrowotnej i określone państwowe lub samorządowe osoby prawne. Sama osoba fizyczna działająca we własnym imieniu nie została wymieniona jako odrębna kategoria jednostki sektora. Nie wyklucza to jej udziału w realizacji zadań publicznych ani powierzenia jej określonych czynności, ale nie pozwala utożsamić osoby fizycznej z jednostką sektora wyłącznie z powodu publicznego celu działania lub społecznego charakteru wpływów.

32. Art. 43 — powszechne prawo realizacji zadań finansowanych ze środków publicznych
Art. 43 stanowi, że prawo realizacji zadań finansowanych ze środków publicznych przysługuje ogółowi podmiotów, chyba że odrębne ustawy stanowią inaczej. Jest to istotny argument przeciwko założeniu, że zadania publiczne mogą wykonywać wyłącznie organy administracji lub jednostki sektora finansów publicznych. Wniosek obywatela może więc przedstawiać go jako inicjatora, proponowanego realizatora, koordynatora lub osobę odpowiedzialną za wykonanie określonego zakresu zadania. Zakres podmiotów uprawnionych do uzyskania konkretnej dotacji oraz tryb jej przyznania mogą jednak wynikać z przepisów szczególnych, programu, konkursu i umowy. Art. 43 nie daje automatycznego prawa do otrzymania dotacji ani do dysponowania rachunkiem PFC.

33. Art. 61 ust. 1 Konstytucji — jawność także u podmiotów spoza administracji
Konstytucyjne prawo dostępu do informacji obejmuje działalność organów władzy publicznej, a także innych osób i jednostek organizacyjnych w zakresie, w jakim wykonują zadania władzy publicznej i gospodarują mieniem komunalnym lub majątkiem Skarbu Państwa. Przepis ten potwierdza, że wykonywanie zadań publicznych i gospodarowanie majątkiem publicznym może występować również po stronie podmiotów niebędących organami administracji. Wspiera to postulat jawności działań prywatnego realizatora lub zarządzającego zadaniem publicznym. Nie nadaje jednak samodzielnej kompetencji do przyjęcia ani wydatkowania środków.

34. Art. 63 Konstytucji — obywatelska inicjatywa w interesie publicznym
Każdy ma prawo składać do organów władzy publicznej petycje, wnioski i skargi w interesie publicznym, własnym lub innej osoby za jej zgodą. Przepis ten wspiera prawo obywatela do zgłoszenia Celu, przedstawienia propozycji jego organizacji, wskazania potrzeb społecznych i wnioskowania o powierzenie mu określonej roli. Jest podstawą udziału obywatelskiego w inicjowaniu spraw publicznych, a nie samoistnym źródłem kompetencji finansowych.

35. Art. 888, art. 893 i art. 894 Kodeksu cywilnego — darowizna, polecenie i interes społeczny
Art. 888 określa darowiznę jako bezpłatne świadczenie na rzecz obdarowanego kosztem majątku darczyńcy. Art. 893 pozwala darczyńcy nałożyć na obdarowanego obowiązek oznaczonego działania lub zaniechania (polecenie), a art. 894 reguluje żądanie wykonania polecenia; jeżeli polecenie ma na względzie interes społeczny, po śmierci darczyńcy jego wykonania może żądać także właściwy organ państwowy. Przepisy te mogą wspierać konstrukcję darowizny obciążonej poleceniem służącym określonemu interesowi społecznemu. Nie zmieniają jednak same przez się prywatnego obdarowanego w jednostkę sektora finansów publicznych ani darowizny na jego rzecz w dochód publiczny w rozumieniu art. 5 ust. 2 pkt 5 u.f.p. Status środków zależy od tego, kto jest rzeczywistym obdarowanym, jaki jest tytuł prawny wpłaty i czy środki zostały prawnie włączone do gospodarki finansowej właściwej jednostki publicznej.

36. Art. 5 ust. 2 pkt 5 — zakres podmiotowy przepisu i możliwość jego zastosowania
Wykładnia językowa przepisu wskazuje, że warunkiem zaliczenia pieniężnej darowizny do dochodów publicznych jest jej dokonanie „na rzecz jednostki sektora finansów publicznych”. Sam publiczny cel darowizny, obywatelska misja obdarowanego, prowadzenie ewidencji analitycznej lub nazwanie osoby „dysponentem” nie zastępuje tego warunku. Jeżeli darowizna jest prawnie dokonana na rzecz jednostki sektora finansów publicznych, a osoba fizyczna działa wyłącznie jako jej prawidłowo umocowany przedstawiciel, pełnomocnik, koordynator lub osoba wykonująca powierzone czynności, możliwe jest rozdzielenie obdarowanego (jednostki publicznej) od osoby wykonującej zadania zarządcze. To rozdzielenie może stanowić podstawę do dalszego projektowania roli obywatela bez fikcyjnego uznawania go za jednostkę sektora. Wymaga jednak wskazania konkretnej jednostki publicznej jako obdarowanego oraz ustalenia, czy ustawa regulująca dany Fundusz pozwala przyjąć i zaksięgować taką darowiznę.

37. Rozróżnienie trzech modeli prawnych
Dla uniknięcia niejasności regulamin powinien rozróżniać:
a) model publicznego obdarowanego — darowizna jest przekazywana na rzecz jednostki sektora finansów publicznych, a osoba fizyczna wykonuje wyłącznie powierzone czynności w imieniu lub na rzecz tej jednostki; w takim przypadku należy zbadać zastosowanie art. 5 ust. 2 pkt 5 u.f.p.;
b) model prywatnego realizatora — osoba fizyczna lub inny podmiot prywatny otrzymuje środki na podstawie dopuszczalnej prawem umowy lub procedury i realizuje zadanie publiczne; art. 4 ust. 1 pkt 2 u.f.p. obejmuje go zasadami finansowymi w zakresie wykorzystania lub dysponowania środkami publicznymi, lecz sam fakt realizacji celu publicznego nie czyni wpływów dochodem publicznym ani realizatora jednostką sektora;
c) model PFC — środki pozostają środkami Funduszu, rachunek Funduszu pozostaje pod dyspozycją organu wskazanego w art. 29 ust. 3 i ustawie regulującej Fundusz, a osoba fizyczna może pełnić funkcję inicjatora, koordynatora, realizatora albo osobę wykonującą ściśle określone czynności, o ile istnieje odpowiednia podstawa prawna i umocowanie.

38. Wniosek interpretacyjny dla projektu
Najbardziej użyteczna prawnie postać postulatu nie polega na twierdzeniu, że osoba fizyczna już z samej definicji jest jednostką sektora finansów publicznych. Polega na wykazaniu, że prawo rozpoznaje udział podmiotów spoza sektora w realizacji zadań publicznych (art. 43), obejmuje podmioty prywatne wykorzystujące lub dysponujące środkami publicznymi odpowiednimi regułami (art. 4 ust. 1 pkt 2), przewiduje umowne powierzanie zadań i kontrolę ich wykonania (art. 126–152 w granicach zakresu zastosowania poszczególnych przepisów), a Konstytucja obejmuje jawnością także inne osoby i jednostki organizacyjne wykonujące zadania publiczne i gospodarujące majątkiem publicznym (art. 61 ust. 1). Na tej podstawie można wnosić o ustanowienie obywatela prywatnym Dyspozytorem/koordynatorem Celu lub realizatorem określonego zakresu zadania — pod warunkiem wskazania konkretnej podstawy ustawowej, prawidłowego obdarowanego, sposobu przyjęcia środków, granic kompetencji, odpowiedzialności, ewidencji i kontroli. Jest to konstrukcja odmienna od uznania osoby fizycznej za jednostkę sektora finansów publicznych.

Źródła urzędowe:
- Ustawa o finansach publicznych, Dz.U. z 2026 r. poz. 1447, w szczególności art. 4, 5, 8, 9, 29, 43, 126–152: https://eli.gov.pl/api/acts/DU/2026/1447/text.html
- Konstytucja Rzeczypospolitej Polskiej, art. 61 i 63: https://www.prezydent.pl/prawo/konstytucja-rp/rozdzial-ii-wolnosci-prawa-i-obowiazki-czlowieka-i-obywatela
- Kodeks cywilny, Dz.U. z 2026 r. poz. 795, art. 888, 893–895: https://eli.gov.pl/api/acts/DU/2026/795/text/I/D20260795.pdf



UZUPEŁNIENIE — RODZAJE POSŁÓW, MANDATÓW I OBYWATELSKIEGO DECYDOWANIA O CELU

39. Poseł parlamentarny — mandat konstytucyjny
Poseł na Sejm Rzeczypospolitej Polskiej w znaczeniu konstytucyjnym jest osobą, która uzyskała mandat w trybie wyborów do Sejmu określonym w Konstytucji i ustawach. Zgodnie z art. 104 ust. 1 Konstytucji posłowie są przedstawicielami Narodu i nie wiążą ich instrukcje wyborców. Przed rozpoczęciem sprawowania mandatu składają przed Sejmem ślubowanie określone w art. 104 ust. 2. Odmowa złożenia ślubowania oznacza zrzeczenie się mandatu (art. 104 ust. 3). Jest to mandat przedstawicielski o skutkach ustrojowych, w tym prawach i obowiązkach określonych w Konstytucji i ustawach.

40. Poseł obywatelski — mandat obywatelski (deklaracyjny)
Na potrzeby niniejszego Regulaminu „Poseł obywatelski” oznacza obywatela, który świadomie przyjmuje odpowiedzialność za służbę Narodowi i dobru wspólnemu oraz publicznie deklaruje gotowość do jej wykonywania. Może on złożyć własną przysięgę obywatelską, wykorzystując w całości tekst ślubowania określony w art. 104 ust. 2 Konstytucji RP. Może wygłosić ten tekst przed budynkiem Sejmu, w innym miejscu publicznym albo podczas uroczystości obywatelskiej. W projekcie traktuje się to jako dobrowolne przyjęcie treści zobowiązania do rzetelnej i sumiennej służby Narodowi, strzeżenia suwerenności i interesów Państwa, działania dla pomyślności Ojczyzny i dobra obywateli oraz przestrzegania Konstytucji i prawa. Jest to obywatelski akt deklaracji i zobowiązania; nie jest ślubowaniem składanym przez parlamentarzystę przed Sejmem w trybie art. 104 i sam przez się nie nadaje mandatu parlamentarnego, immunitetu ani kompetencji organu państwowego. Wykorzystanie tekstu konstytucyjnego jako treści własnej przysięgi nie oznacza, że obywatel twierdzi, iż obejmuje mandat posła na Sejm.

41. Poseł powołany — mandat z powołania
„Poseł powołany” oznacza w niniejszym projekcie obywatela, który rozpoznał w sobie osobiste powołanie do służby Narodowi i dobru wspólnemu oraz przyjął wynikające z niego zobowiązanie. Mandat z powołania wynika z rozpoznania i przyjęcia tej odpowiedzialności; nie wymaga wcześniejszego wygłoszenia publicznej przysięgi ani wpisu do rejestru. Jest to kategoria obywatelska, etyczna i programowa, a nie mandat przedstawicielski w rozumieniu Konstytucji ani samoistne umocowanie do wykonywania władzy publicznej lub dysponowania środkami publicznymi.

42. Rodzaje mandatów i ich wzajemna relacja
W projekcie rozróżnia się:
a) mandat parlamentarny — uzyskany w trybie konstytucyjnym, związany z wyborem do Sejmu i wykonywany po złożeniu ślubowania przed Sejmem;
b) mandat obywatelski (deklaracyjny) — przyjęty przez obywatela poprzez świadomą, dobrowolną deklarację lub przysięgę służby Narodowi, w tym przez wygłoszenie tekstu ślubowania z art. 104 ust. 2 Konstytucji jako własnej przysięgi obywatelskiej;
c) mandat z powołania — wynikający z osobistego rozpoznania i przyjęcia powołania do służby Narodowi, niezależnie od publicznej ceremonii.
Kategorie te opisują różne podstawy służby i nie muszą się wykluczać na płaszczyźnie osobistej. Ta sama osoba może być posłem parlamentarnym, a zarazem deklarować osobiste powołanie do służby; nie powoduje to jednak połączenia ani przeniesienia skutków prawnych poszczególnych mandatów. Mandat obywatelski i mandat z powołania nie zastępują mandatu parlamentarnego.

43. Obywatel jako suweren, współgospodarz dobra wspólnego i inicjator działania
Projekt opiera się na konstytucyjnych zasadach, że Rzeczpospolita Polska jest dobrem wspólnym wszystkich obywateli (art. 1 Konstytucji), władza zwierzchnia należy do Narodu, a Naród sprawuje ją przez przedstawicieli lub bezpośrednio (art. 4). W przyjętej tu koncepcji obywatel nie jest wyłącznie odbiorcą działań administracji. Jest członkiem suwerennego Narodu, współodpowiedzialnym za dobro wspólne oraz uprawnionym do inicjowania i podejmowania zgodnych z prawem działań na jego rzecz. Projekt ujmuje tę pozycję jako obywatelskie współgospodarowanie dobrem wspólnym i współuczestnictwo w życiu publicznym. Nie oznacza to automatycznego nabycia udziału kapitałowego w Skarbie Państwa ani indywidualnego prawa własności do oznaczonych składników majątku publicznego; prawa do konkretnych składników mienia oraz kompetencje organów określają właściwe przepisy.

44. Poseł obywatelski jako Dyspozytor Celu
W modelu organizacyjnym Regulaminu Poseł obywatelski, w szczególności posiadający mandat deklaracyjny, może objąć funkcję Dyspozytora Celu. Obywatelska przysięga potwierdza przyjęcie odpowiedzialności i może stanowić kryterium etyczne lub element procedury rejestrowania i rekomendowania Dyspozytorów. Nie jest jednak źródłem ustawowej kompetencji do autoryzowania płatności z rachunku PFC. Funkcja Dyspozytora w rozumieniu projektu rozpoczyna się z inicjatywy obywatela i obejmuje samodzielne prowadzenie Celu w zakresie organizacyjnym, merytorycznym i społecznym. Formalne uprawnienia do podejmowania czynności w imieniu Funduszu, zaciągania zobowiązań, zatwierdzania wydatków lub wydawania dyspozycji płatniczych wymagają odrębnej podstawy prawnej i właściwego umocowania.

45. Ślubowanie konstytucyjne jako tekst dobrowolnej przysięgi obywatelskiej
Art. 104 ust. 2 Konstytucji określa tekst ślubowania składanego przez posłów przed Sejmem przed rozpoczęciem sprawowania mandatu. Przepis ten reguluje czynność parlamentarzysty i jej skutki w ramach mandatu poselskiego. W projekcie przyjmuje się odrębnie, że obywatel może dobrowolnie wypowiedzieć ten sam tekst jako własną przysięgę służby Narodowi, również publicznie przed budynkiem Sejmu. Samo użycie tekstu nie czyni tej czynności ślubowaniem parlamentarnym ani nie oznacza, że obywatel uzyskuje mandat posła na Sejm. Regulamin uznaje natomiast taką deklarację za możliwą formę przyjęcia mandatu obywatelskiego w znaczeniu projektowym.

46. Ustanowienie Celu z inicjatywy obywatela
Cel może zostać zainicjowany i opisany przez obywatela przed uzyskaniem jakiegokolwiek finansowania. Obywatel rozpoznaje potrzebę społeczną, określa Cel państwowy lub publiczny mieszczący się w zadaniach właściwego Funduszu, wskazuje beneficjentów, działania, rezultaty, budżet, harmonogram i sposób rozliczenia, a następnie ogłasza, że podejmuje się prowadzenia Celu jako jego Dyspozytor w rozumieniu organizacyjnym niniejszego projektu. Uznanie Celu za zadanie finansowane z konkretnego PFC wymaga jednak zgodności z ustawą regulującą Fundusz, decyzji właściwego organu i ujęcia w odpowiednim planie finansowym. Obywatelska inicjatywa i projektowy status Dyspozytora nie są uzależnione od wcześniejszej decyzji o przyznaniu dotacji.

47. Kolejność ustanowienia, finansowania i prowadzenia Celu
Mechanizm projektu przebiega w następującej kolejności:
1) Obywatel rozpoznaje potrzebę społeczną i określa konkretny Cel służący dobru wspólnemu i dobru Narodu.
2) Obywatel występuje do właściwego ministra z wnioskiem o przyznanie środków na ten Cel, a równocześnie ogłasza wśród innych obywateli potrzebę przekazywania darowizn celowych na jego realizację.
3) Obywatel obejmuje funkcję Dyspozytora Celu w znaczeniu organizacyjnym projektu, przyjmuje odpowiedzialność za jego prowadzenie i podejmuje decyzje dotyczące sposobu realizacji. Może przeznaczyć na Cel własne środki i rozpocząć jego realizację bez oczekiwania na dotację ministra lub wpłaty innych obywateli.
4) Minister oraz poszczególni obywatele niezależnie podejmują decyzje o przyznaniu lub przekazaniu środków, którymi zgodnie z prawem dysponują. Każdy określa warunki wykorzystania środków pochodzących z jego źródła. Minister działa w granicach ustawy regulującej Fundusz, planu finansowego i właściwego trybu dotacyjnego; darczyńcy określają dopuszczalne warunki darowizn; Dyspozytor może wnosić środki własne. Minister może odrębnie powierzyć Dyspozytorowi określone uprawnienia lub funkcję, jeżeli istnieje do tego podstawa prawna. Przyznanie środków ani formalne powierzenie kompetencji nie jest warunkiem obywatelskiego zainicjowania Celu i rozpoczęcia działań z własnych środków.
5) Dyspozytor prowadzi ewidencję, nadzoruje realizację, rozlicza otrzymane środki i odpowiada za zgodność ich wykorzystania z przeznaczeniem, warunkami darowizn, umową dotacji i przepisami prawa.
Kolejność ta oznacza, że Cel i odpowiedzialność obywatela za jego prowadzenie poprzedzają finansowanie. Źródła finansowania mogą pojawiać się w różnym czasie i niezależnie od siebie.

48. Samodzielność decyzyjna Dyspozytora Celu
Istotą funkcji Dyspozytora jest rzeczywiste podejmowanie decyzji dotyczących realizacji Celu, a nie wyłącznie wykonywanie czynności technicznych. W zakresie organizacyjnym i w odniesieniu do środków własnych Dyspozytor samodzielnie określa kolejność działań, priorytety, sposób organizacji, dobór wykonawców i sposób osiągania rezultatów, z poszanowaniem prawa. W odniesieniu do środków publicznych zakres samodzielności musi wynikać z podstawy ich przyznania, ustawy, umowy, decyzji lub aktu umocowania. Jeżeli Dyspozytor otrzyma prawnie skuteczne uprawnienie do decydowania o wykorzystaniu określonej puli, podejmuje decyzje w granicach Celu, zatwierdzonego budżetu i warunków finansowania. Regulamin postuluje, aby akt powierzenia zapewniał Dyspozytorowi realną, a nie wyłącznie pozorną, przestrzeń decyzyjną w ramach tych granic.

49. Odrębność źródeł finansowania i ewidencja Celu
Środki własne Dyspozytora, darowizny przekazywane na rzecz prywatnego realizatora oraz środki publiczne z PFC zachowują odrębny status prawny. Nie wolno ich łączyć na jednym rachunku ani traktować jako jednego rodzaju środków bez podstawy prawnej. Darowizny na rzecz PFC mogą być przyjmowane wyłącznie wtedy, gdy dopuszcza je ustawa regulująca Fundusz i są przekazywane na rachunek właściwego odbiorcy. Darowizna na rzecz prywatnego podmiotu lub osoby nie staje się z tego powodu przychodem PFC ani dochodem publicznym. Subkonto ewidencyjne Celu jest kontem analitycznym w księgach rachunkowych właściwej jednostki, a nie odrębnym rachunkiem bankowym i nie daje samoistnego dostępu do rachunku Funduszu. Ewidencja powinna odrębnie identyfikować źródło, kwotę, datę, tytuł prawny, warunki, wydatki, zobowiązania, zwroty i saldo każdej puli środków.

50. Bezpośrednie obywatelskie decydowanie i art. 4 ust. 2 Konstytucji
W koncepcji Regulaminu bezpośrednie obywatelskie decydowanie o ustanowieniu, prowadzeniu i realizacji Celu, w tym o sposobie wykorzystania środków powierzonych Dyspozytorowi w prawnie określonym zakresie, jest projektowaną formą bezpośredniego udziału obywatela w sprawach publicznych i współdecydowania o dobru wspólnym, odwołującą się do art. 4 ust. 2 Konstytucji RP. Nie jest to wyłącznie konsultowanie działań administracji: projekt zakłada powierzenie obywatelowi rzeczywistej odpowiedzialności i przestrzeni decyzyjnej. Jednocześnie art. 4 ust. 2 nie uchyla art. 7 Konstytucji ani ustawowych zasad gospodarki finansami publicznymi. Aby decyzje Dyspozytora dotyczące środków PFC były wiążące dla Funduszu, muszą zostać osadzone w odpowiedniej podstawie ustawowej i prawidłowym umocowaniu. Wniosek projektu polega zatem na postulowaniu instytucjonalnego mechanizmu bezpośredniego obywatelskiego współdecydowania o wyodrębnionym Celu publicznym.

51. Odpowiedzialność i rozliczalność Dyspozytora
Dyspozytor odpowiada za rzetelność informacji o Celu, zgodność działań z prawem, warunkami finansowania i przyjętymi zobowiązaniami, prawidłową ewidencję, ochronę beneficjentów, zapobieganie konfliktom interesów, jawność informacji podlegających udostępnieniu oraz terminowe rozliczenie środków. W odniesieniu do środków publicznych stosuje się właściwe przepisy o dotacjach, rachunkowości, kontroli i zwrocie środków. W odniesieniu do darowizn prywatnych stosuje się przepisy prawa cywilnego, warunki darowizny i właściwe przepisy podatkowe. Dyspozytor nie może przedstawiać środków prywatnych jako publicznych ani odwrotnie.

52. Zasada rozdzielenia mandatu obywatelskiego i umocowania finansowego
Mandat obywatelski i mandat z powołania określają źródło osobistego zobowiązania do służby Narodowi. Funkcja Dyspozytora określa odpowiedzialność organizacyjną za Cel. Umocowanie finansowe określa natomiast prawne uprawnienia do podejmowania czynności dotyczących środków publicznych. Są to trzy odrębne płaszczyzny. Projekt zakłada, że obywatel może objąć funkcję Dyspozytora przed uzyskaniem finansowania, a następnie ubiegać się o przyznanie środków i formalne rozszerzenie uprawnień decyzyjnych. Nie należy uzależniać obywatelskiej inicjatywy od uprzedniego wskazania Dyspozytora przez ministra.

Źródła urzędowe:
- Konstytucja Rzeczypospolitej Polskiej, art. 1, 4, 7 i 104: https://www.senat.gov.pl/o-senacie/wybrane-akty-prawne/konstytucja/
- Konstytucja Rzeczypospolitej Polskiej, art. 104: https://www.prezydent.pl/prawo/konstytucja-rp/rozdzial-iv-sejm-i-senat
- Ustawa z dnia 9 maja 1996 r. o wykonywaniu mandatu posła i senatora, tekst jednolity Dz.U. z 2026 r. poz. 1282, art. 1–2: https://eli.gov.pl/eli/DU/2026/1282/ogl
- Ustawa o finansach publicznych, tekst jednolity Dz.U. z 2026 r. poz. 1447, w szczególności art. 4, 29, 43–44, 126–152: https://eli.gov.pl/api/acts/DU/2026/1447/text.html
- Kodeks cywilny, art. 888, 893–895: https://eli.gov.pl/api/acts/DU/2026/795/text/I/D20260795.pdf

"""

@Composable
fun LegalInformationScreen(onBack: () -> Unit) {
    var showFundRegulation by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showFundRegulation) { showFundRegulation = false }
    BackHandler(enabled = !showFundRegulation) { onBack() }

    val displayedText = if (showFundRegulation) {
        LEGAL_INFORMATION_TEXT.substringAfter("\nREGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY W RAMACH ISTNIEJĄCEGO PAŃSTWOWEGO FUNDUSZU CELOWEGO")
    } else {
        LEGAL_INFORMATION_TEXT.substringBefore("\nREGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY W RAMACH ISTNIEJĄCEGO PAŃSTWOWEGO FUNDUSZU CELOWEGO")
    }
    val pageTitle = if (showFundRegulation) {
        "Regulamin dotacji celowych państwowego funduszu celowego"
    } else {
        "Informacje prawne"
    }
    val sections = displayedText.trim().split("\n")

    Surface(
        modifier = Modifier.fillMaxSize().semantics { paneTitle = pageTitle },
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
                IconButton(onClick = { if (showFundRegulation) showFundRegulation = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    pageTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
            }
            if (!showFundRegulation) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showFundRegulation = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Regulamin dotacji celowych państwowego funduszu celowego",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Cel rehabilitacyjno-opiekuńczy w ramach istniejącego funduszu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz regulamin")
                    }
                }
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
}
