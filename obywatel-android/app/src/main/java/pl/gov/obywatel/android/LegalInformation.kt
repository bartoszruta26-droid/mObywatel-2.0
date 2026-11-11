// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun LegalInformationScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "‹  Wróć do menu Więcej",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp)
        )
        Text(
            "Informacje prawne",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Aplikacja Obywatelska · wersja prototypowa 0.1.0 · stan informacji: 11 listopada 2026 r.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LegalNoticeCard(
            "1. Charakter i status aplikacji",
            "Aplikacja Obywatelska jest niezależnym projektem obywatelskim rozwijanym jako prototyp na system Android. Nie jest oficjalną aplikacją mObywatel, nie jest wydawana, prowadzona ani zatwierdzona przez Ministerstwo Cyfryzacji, Centralny Ośrodek Informatyki ani inny organ administracji publicznej. Nie należy utożsamiać jej z rządowym systemem teleinformatycznym ani z usługą publiczną."
        )
        LegalNoticeCard(
            "2. Operator i dane kontaktowe",
            "Podmiot odpowiedzialny za publiczne udostępnianie aplikacji, jego pełna nazwa, adres do korespondencji i adres elektroniczny, a także właściwy kontakt w sprawach ochrony danych, muszą zostać wskazane przez operatora przed uruchomieniem usługi dla użytkowników. Dane te nie są obecnie skonfigurowane w tej wersji prototypowej. Niniejszy ekran nie zastępuje obowiązkowego oznaczenia usługodawcy ani informacji o administratorze danych."
        )
        LegalNoticeCard(
            "3. Dane osobowe i prywatność",
            "Aplikacja zawiera moduły do prowadzenia lokalnych ewidencji i dokumentów, w tym danych identyfikacyjnych, rodzinnych, edukacyjnych, zawodowych, organizacyjnych i zdrowotnych. Użytkownik powinien wprowadzać wyłącznie dane, do których przetwarzania ma odpowiednie uprawnienie i podstawę prawną. Dane o zdrowiu są szczególną kategorią danych osobowych w rozumieniu art. 9 RODO i wymagają odrębnej oceny przesłanki legalizującej ich przetwarzanie oraz odpowiednich zabezpieczeń. Szczególnej ostrożności wymagają dane dzieci i innych osób."
        )
        LegalNoticeCard(
            "4. Kto jest administratorem danych?",
            "Rola administratora zależy od konkretnego procesu i od tego, kto faktycznie ustala cele oraz sposoby przetwarzania. Operator aplikacji może być administratorem danych przetwarzanych w związku z jej udostępnianiem, utrzymaniem lub obsługą. W innych sytuacjach może działać jako podmiot przetwarzający albo w ogóle nie otrzymywać danych zapisanych wyłącznie na urządzeniu. Sam fakt wpisania danych do aplikacji nie rozstrzyga automatycznie o roli prawnej użytkownika ani operatora. Przed wydaniem publicznym trzeba sporządzić mapę przepływów danych i przypisać role dla każdego procesu."
        )
        LegalNoticeCard(
            "5. Zakres przechowywania i przekazywania danych",
            "Część funkcji działa lokalnie, a wybrane zasoby identyfikacji dokumentów i indeksy przypisań są zabezpieczane szyfrowaniem AES-GCM. Nie oznacza to, że wszystkie dane aplikacji są szyfrowane w ten sam sposób ani że cały ruch sieciowy został wyłączony. Aplikacja deklaruje uprawnienie INTERNET; przed publicznym wydaniem należy zweryfikować rzeczywisty ruch sieciowy, biblioteki, dzienniki, kopie zapasowe, analitykę i ewentualnych odbiorców danych. W obecnej wersji nie ma aktywnego adaptera do rządowego mObywatela ani potwierdzonego połączenia z rejestrami państwowymi."
        )
        LegalNoticeCard(
            "6. Prawa osób, których dane dotyczą",
            "Jeżeli do danego procesu ma zastosowanie RODO, osoba, której dane dotyczą, może korzystać — z uwzględnieniem wyjątków i warunków określonych w przepisach — z prawa dostępu do danych, sprostowania, usunięcia, ograniczenia przetwarzania, przenoszenia danych, sprzeciwu oraz innych praw przewidzianych w RODO. Może również wnieść skargę do Prezesa Urzędu Ochrony Danych Osobowych. Żądania należy kierować do właściwego administratora wskazanego w pełnej informacji o przetwarzaniu danych. Ten prototyp nie udostępnia jeszcze kompletnego, centralnego formularza realizacji tych praw."
        )
        LegalNoticeCard(
            "7. Dokumenty, legitymacje, herby i pieczęcie",
            "Dokumenty tworzone w aplikacji są prywatnymi formularzami, podglądami lub ewidencjami, chyba że ich status zostanie odrębnie potwierdzony przez właściwego wystawcę i zgodną z prawem procedurę. Wpis, herb, godło, logo, pieczęć, fotografia, podpis graficzny ani kod QR nie potwierdzają samodzielnie tożsamości, urzędu, godności kościelnej, święceń, mandatu, kwalifikacji, członkostwa, upoważnienia ani autentyczności dokumentu. Nie wolno przedstawiać wzoru prywatnego jako dokumentu państwowego lub urzędowego."
        )
        LegalNoticeCard(
            "8. PDF i podpisy elektroniczne",
            "Wygenerowanie pliku PDF nie oznacza jego podpisania, opatrzenia pieczęcią elektroniczną, urzędowego poświadczenia ani nadania mu mocy dokumentu urzędowego. Skutek prawny podpisu zależy od rodzaju podpisu, sposobu jego złożenia, tożsamości podpisującego i przepisów właściwych dla danej czynności. Zgodnie z art. 25 rozporządzenia eIDAS kwalifikowany podpis elektroniczny ma skutek prawny równoważny podpisowi własnoręcznemu. Aplikacja w tej wersji nie świadczy usługi kwalifikowanego podpisu elektronicznego."
        )
        LegalNoticeCard(
            "9. Informacje, usługi publiczne i odpowiedzialność za treść",
            "Katalogi, kafelki i ekrany informacyjne mogą przedstawiać obszary usług publicznych, ale sama ich obecność nie oznacza połączenia z urzędem, rejestrem lub systemem państwowym. Aplikacja nie potwierdza aktualności danych urzędowych i nie zastępuje informacji właściwego organu. Użytkownik powinien sprawdzić wymagania, terminy i skutki prawne w źródle urzędowym właściwym dla sprawy. Aplikacja ma charakter narzędzia organizacyjnego i ewidencyjnego; nie zastępuje porady prawnej, medycznej ani decyzji uprawnionego organu."
        )
        LegalNoticeCard(
            "10. Regulamin i udostępnianie usługi",
            "Jeżeli aplikacja będzie publicznie świadczyć usługi drogą elektroniczną, operator powinien przed rozpoczęciem świadczenia określić i udostępnić regulamin oraz wymagane informacje o usługodawcy, zakresie usług, warunkach technicznych, zasadach zawierania i rozwiązywania umowy oraz trybie reklamacyjnym — stosownie do aktualnych przepisów, w szczególności ustawy o świadczeniu usług drogą elektroniczną. W tej wersji prototypowej nie opublikowano kompletnego regulaminu świadczenia usług."
        )
        LegalNoticeCard(
            "11. Własność intelektualna i znaki",
            "Kod, teksty, grafiki i zasoby mogą podlegać różnym licencjom i prawom. Część piktogramów przeniesionych z zasobów iOS w repozytorium jest oznaczona jako LicenseRef-COI-MIT-PL; należy zachować informacje o pochodzeniu i stosować właściwe warunki licencji. Autorskie ilustracje heraldyczne są koncepcjami graficznymi, a nie potwierdzonymi historycznymi wzorami. Użytkownik importujący plik odpowiada za posiadanie praw lub innej podstawy do jego użycia. Nie wolno naruszać praw autorskich, praw do znaków, dóbr osobistych ani zasad ochrony symboli prawnie chronionych."
        )
        LegalNoticeCard(
            "12. Ograniczenia wersji prototypowej",
            "Prototyp nie przeszedł pełnego audytu prawnego, bezpieczeństwa i ochrony danych dla wdrożenia produkcyjnego. Nie należy umieszczać w nim danych, których ujawnienie lub utrata mogłyby spowodować istotną szkodę, dopóki operator nie opublikuje kompletnej dokumentacji prywatności i nie zakończy audytów. Funkcje mogą być demonstracyjne, lokalne, niepełne lub zmieniać się w kolejnych wersjach."
        )
        LegalNoticeCard(
            "13. Podstawy prawne i źródła urzędowe",
            "Najważniejsze akty i materiały do oceny zgodności projektu: RODO — rozporządzenie (UE) 2016/679, w szczególności art. 5, 6, 9, 12–22, 25, 32 i 35; ustawa z 18 lipca 2002 r. o świadczeniu usług drogą elektroniczną, w szczególności art. 5 i 8; rozporządzenie eIDAS (UE) nr 910/2014, w szczególności art. 25; ustawa z 4 lutego 1994 r. o prawie autorskim i prawach pokrewnych. Zastosowanie przepisów zależy od faktycznego modelu działania, podmiotu operatora, rodzaju danych i sposobu udostępniania aplikacji."
        )
        LegalSourceLink("RODO — EUR-Lex", "https://eur-lex.europa.eu/eli/reg/2016/679/oj?locale=PL") { uriHandler.openUri(it) }
        LegalSourceLink("Ustawa o świadczeniu usług drogą elektroniczną — ELI", "https://eli.gov.pl/api/acts/DU/2024/1513/text.html") { uriHandler.openUri(it) }
        LegalSourceLink("eIDAS — EUR-Lex", "https://eur-lex.europa.eu/eli/reg/2014/910/oj?locale=PL") { uriHandler.openUri(it) }
        LegalSourceLink("Prawo autorskie — ELI", "https://eli.gov.pl/eli/DU/1994/83") { uriHandler.openUri(it) }
        LegalSourceLink("UODO — prawa osób, których dane dotyczą", "https://uodo.gov.pl/pl/493/2254") { uriHandler.openUri(it) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ważne przed publikacją", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Przed udostępnieniem produkcyjnym należy uzupełnić nazwę i dane kontaktowe operatora, wskazać administratora dla każdego procesu, opublikować pełną politykę prywatności i regulamin (jeśli są wymagane), określić okresy retencji, odbiorców i transfery danych, przeprowadzić analizę ryzyka oraz ocenić potrzebę DPIA. Treść tego ekranu ma charakter informacyjny i nie stanowi indywidualnej porady prawnej ani potwierdzenia zgodności aplikacji z prawem.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LegalNoticeCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegalSourceLink(title: String, url: String, onOpen: (String) -> Unit) {
    Text(
        title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { onOpen(url) }.padding(vertical = 5.dp)
    )
}
