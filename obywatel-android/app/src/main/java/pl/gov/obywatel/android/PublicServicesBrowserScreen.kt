// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.ui.theme.Blue
import pl.gov.obywatel.android.ui.theme.Muted
import pl.gov.obywatel.android.ui.theme.Navy
import pl.gov.obywatel.android.ui.theme.White

private data class PublicServiceLink(
    val title: String,
    val description: String,
    val category: String,
    val url: String,
    val keywords: List<String> = emptyList(),
    val officialLabel: String = "Oficjalny serwis publiczny"
)

private val publicServiceLinks = listOf(
    PublicServiceLink("mObywatel w przeglądarce", "Dokumenty, Twoje sprawy, ePłatności i katalog usług mObywatela.", "mObywatel", "https://mobywatel.gov.pl/", listOf("mdowód", "dokumenty", "sprawy", "aplikacja")),
    PublicServiceLink("Katalog usług dla obywatela", "Wyszukiwarka spraw i usług administracji publicznej.", "Katalog usług", "https://www.gov.pl/web/gov/uslugi-dla-obywatela", listOf("gov", "urząd", "wniosek", "sprawa")),
    PublicServiceLink("Katalog usług mObywatela", "Oficjalny opis usług dostępnych w aplikacji mObywatel.", "mObywatel", "https://info.mobywatel.gov.pl/uslugi", listOf("usługi aplikacji", "funkcje")),
    PublicServiceLink("Pismo ogólne do urzędu", "Przejdź do usługi wysłania pisma do podmiotu publicznego.", "Sprawy urzędowe", "https://www.gov.pl/web/gov/wyslij-pismo-ogolne", listOf("pismo", "wniosek", "korespondencja")),
    PublicServiceLink("Profil Zaufany", "Założenie, potwierdzenie i zarządzanie Profilem Zaufanym.", "Tożsamość i podpis", "https://pz.gov.pl/", listOf("logowanie", "podpis", "tożsamość")),
    PublicServiceLink("Podpisz dokument elektronicznie", "Informacje i dostęp do podpisywania dokumentów online.", "Tożsamość i podpis", "https://www.gov.pl/web/gov/podpisz-dokument-elektronicznie-wykorzystaj-podpis-zaufany", listOf("podpis zaufany", "pdf", "elektroniczny")),
    PublicServiceLink("e-Doręczenia", "Oficjalny portal informacji i obsługi adresu do e-Doręczeń.", "Sprawy urzędowe", "https://www.gov.pl/web/e-doreczenia", listOf("korespondencja", "list", "adres elektroniczny")),
    PublicServiceLink("ePUAP", "Elektroniczna Platforma Usług Administracji Publicznej.", "Sprawy urzędowe", "https://epuap.gov.pl/", listOf("urząd", "wnioski", "skrzynka")),
    PublicServiceLink("Dowód osobisty", "Informacje i usługi dotyczące dowodu osobistego.", "Dokumenty i tożsamość", "https://www.gov.pl/web/gov/uzyskaj-dowod-osobisty", listOf("mdowód", "zgubienie", "wymiana")),
    PublicServiceLink("Paszport", "Informacje o uzyskaniu i wymianie paszportu.", "Dokumenty i tożsamość", "https://www.gov.pl/web/gov/uzyskaj-paszport", listOf("dziecko", "paszport")),
    PublicServiceLink("Zastrzeż PESEL", "Informacje o zastrzeganiu i cofaniu zastrzeżenia numeru PESEL.", "Bezpieczeństwo", "https://www.gov.pl/web/gov/zastrzez-swoj-numer-pesel-lub-cofnij-zastrzezenie", listOf("ochrona danych", "kradzież tożsamości")),
    PublicServiceLink("Sprawdź punkty karne", "Usługa sprawdzania punktów karnych kierowcy.", "Kierowca i pojazdy", "https://www.gov.pl/web/gov/sprawdz-swoje-punkty-karne", listOf("mandaty", "wykroczenia", "prawo jazdy")),
    PublicServiceLink("Historia pojazdu", "Sprawdź dane pojazdu na podstawie numeru rejestracyjnego, VIN i daty pierwszej rejestracji.", "Kierowca i pojazdy", "https://historiapojazdu.gov.pl/", listOf("samochód", "vin", "rejestracja")),
    PublicServiceLink("Zgłoś zbycie lub nabycie pojazdu", "Przejdź do usługi zgłoszenia sprzedaży lub zakupu pojazdu.", "Kierowca i pojazdy", "https://www.gov.pl/web/gov/zglos-zbycie-lub-nabycie-pojazdu", listOf("samochód", "sprzedaż", "kupno")),
    PublicServiceLink("Kierowca i pojazdy", "Zbiór usług dotyczących prawa jazdy i pojazdów.", "Kierowca i pojazdy", "https://www.gov.pl/web/gov/kierowcy-i-pojazdy", listOf("prawo jazdy", "rejestracja")),
    PublicServiceLink("Internetowe Konto Pacjenta", "Dostęp do e-recept, e-skierowań i dokumentacji medycznej.", "Zdrowie", "https://pacjent.gov.pl/internetowe-konto-pacjenta", listOf("IKP", "recepta", "skierowanie", "zdrowie")),
    PublicServiceLink("Moje zdrowie / pacjent.gov.pl", "Oficjalny portal informacyjny systemu ochrony zdrowia.", "Zdrowie", "https://pacjent.gov.pl/", listOf("pacjent", "leczenie")),
    PublicServiceLink("eWUŚ", "Informacje o potwierdzaniu prawa do świadczeń zdrowotnych.", "Zdrowie", "https://www.nfz.gov.pl/", listOf("ubezpieczenie zdrowotne", "NFZ")),
    PublicServiceLink("PUE / eZUS", "Platforma Usług Elektronicznych Zakładu Ubezpieczeń Społecznych.", "Praca i ubezpieczenia", "https://www.zus.pl/portal/logowanie.npi", listOf("emerytura", "zasiłek", "składki", "ubezpieczenie")),
    PublicServiceLink("e-Urząd Skarbowy", "Elektroniczne usługi Krajowej Administracji Skarbowej.", "Podatki i finanse", "https://www.podatki.gov.pl/e-urzad-skarbowy/", listOf("PIT", "CIT", "podatek", "KAS")),
    PublicServiceLink("Twój e-PIT", "Usługa rozliczenia podatku dochodowego.", "Podatki i finanse", "https://www.podatki.gov.pl/twoj-e-pit/", listOf("PIT", "zeznanie roczne")),
    PublicServiceLink("Podatki.gov.pl", "Informacje i usługi podatkowe administracji skarbowej.", "Podatki i finanse", "https://www.podatki.gov.pl/", listOf("VAT", "akcyza", "podatki")),
    PublicServiceLink("CEIDG", "Centralna Ewidencja i Informacja o Działalności Gospodarczej.", "Firma i przedsiębiorca", "https://ceidg.gov.pl/", listOf("firma", "działalność gospodarcza", "wpis")),
    PublicServiceLink("Biznes.gov.pl", "Portal usług i informacji dla przedsiębiorców.", "Firma i przedsiębiorca", "https://www.biznes.gov.pl/", listOf("firma", "przedsiębiorca", "koncesja", "zezwolenie")),
    PublicServiceLink("Krajowy Rejestr Sądowy", "Informacje i wyszukiwarka podmiotów wpisanych do KRS.", "Rejestry publiczne", "https://ekrs.ms.gov.pl/", listOf("fundacja", "stowarzyszenie", "spółka", "odpis")),
    PublicServiceLink("Krajowy Rejestr Karny", "Informacje o uzyskiwaniu zaświadczeń z KRK.", "Rejestry publiczne", "https://www.gov.pl/web/krajowy-rejestr-karny", listOf("niekaralność", "zaświadczenie")),
    PublicServiceLink("Księgi wieczyste", "Przeglądanie ksiąg wieczystych w systemie Ministerstwa Sprawiedliwości.", "Nieruchomości", "https://ekw.ms.gov.pl/", listOf("nieruchomość", "działka", "własność")),
    PublicServiceLink("Geoportal", "Państwowy portal mapowy i dane przestrzenne.", "Nieruchomości", "https://www.geoportal.gov.pl/", listOf("mapa", "działka", "granice")),
    PublicServiceLink("Praca.gov.pl", "Elektroniczne usługi urzędów pracy.", "Praca i ubezpieczenia", "https://www.praca.gov.pl/", listOf("bezrobotny", "oferty pracy", "szkolenia")),
    PublicServiceLink("Zintegrowany Rejestr Kwalifikacji", "Wyszukiwarka kwalifikacji i informacji o ich poziomach.", "Edukacja", "https://kwalifikacje.gov.pl/", listOf("kwalifikacje", "zawód", "kurs")),
    PublicServiceLink("Rekrutacja i studia", "Informacje o usługach i sprawach edukacyjnych.", "Edukacja", "https://www.gov.pl/web/edukacja", listOf("szkoła", "uczeń", "student")),
    PublicServiceLink("PFRON", "Usługi i informacje dotyczące wsparcia osób z niepełnosprawnościami.", "Wsparcie społeczne", "https://www.pfron.org.pl/", listOf("niepełnosprawność", "dofinansowanie", "dostępność")),
    PublicServiceLink("Emp@tia", "Portal usług i informacji w obszarze zabezpieczenia społecznego.", "Wsparcie społeczne", "https://empatia.mpips.gov.pl/", listOf("świadczenia", "pomoc społeczna", "rodzina")),
    PublicServiceLink("Karta Dużej Rodziny", "Informacje i usługi dotyczące Karty Dużej Rodziny.", "Rodzina", "https://www.gov.pl/web/rodzina/karta-duzej-rodziny", listOf("KDR", "rodzice", "dzieci")),
    PublicServiceLink("Gov.pl — sprawy rodzinne", "Informacje i usługi publiczne dotyczące rodziny i małżeństwa.", "Rodzina", "https://www.gov.pl/web/gov/sprawy-rodzinne", listOf("dziecko", "urodzenie", "małżeństwo")),
    PublicServiceLink("Granica i podróże", "Informacje dla podróżnych i osób przekraczających granicę.", "Podróże", "https://www.gov.pl/web/dyplomacja/informacje-dla-podrozujacych", listOf("konsulat", "zagranica", "podróż")),
    PublicServiceLink("Jakość powietrza", "Dane i komunikaty Głównego Inspektoratu Ochrony Środowiska.", "Środowisko", "https://powietrze.gios.gov.pl/", listOf("smog", "GIOŚ", "powietrze")),
    PublicServiceLink("Główny Inspektorat Ochrony Środowiska", "Informacje, rejestry i usługi ochrony środowiska.", "Środowisko", "https://www.gov.pl/web/gios", listOf("środowisko", "odpady")),
    PublicServiceLink("Alert RCB", "Informacje o systemie ostrzegania ludności.", "Bezpieczeństwo", "https://www.gov.pl/web/rcb", listOf("ostrzeżenia", "kryzys", "bezpieczeństwo")),
    PublicServiceLink("Dane publiczne", "Portal otwartych danych i zasobów administracji.", "Rejestry publiczne", "https://dane.gov.pl/", listOf("otwarte dane", "rejestry", "statystyki"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PublicServicesBrowserScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    val prefs = remember { context.getSharedPreferences("public_services_browser", 0) }
    val favorites = remember {
        mutableStateMapOf<String, Boolean>().apply {
            publicServiceLinks.forEach { put(it.title, prefs.getBoolean(it.title, false)) }
        }
    }
    val categories = listOf("Wszystkie", "Ulubione") + publicServiceLinks.map { it.category }.distinct().sorted()
    val normalizedQuery = query.trim().lowercase()
    val filtered = publicServiceLinks.filter { service ->
        val matchesCategory = selectedCategory == "Wszystkie" || selectedCategory == "Ulubione" || service.category == selectedCategory
        val matchesFavorite = selectedCategory != "Ulubione" || favorites[service.title] == true
        val matchesQuery = normalizedQuery.isBlank() ||
            service.title.lowercase().contains(normalizedQuery) ||
            service.description.lowercase().contains(normalizedQuery) ||
            service.category.lowercase().contains(normalizedQuery) ||
            service.keywords.any { it.lowercase().contains(normalizedQuery) }
        matchesCategory && matchesFavorite && matchesQuery
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB)).padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                Text(" Wróć")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Usługi publiczne", color = Navy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Inteligentna przeglądarka", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Public, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Znajdź sprawę urzędową", color = Navy, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Np. dowód, PESEL, recepta, firma, podatek…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Text("Wyszukiwanie obejmuje nazwy, opisy, kategorie i słowa powiązane.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (selectedCategory == "Ulubione") "Ulubione usługi" else "Dostępne usługi", color = Navy, fontWeight = FontWeight.Bold)
            Text("${filtered.size} usług", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        if (filtered.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(36.dp))
                Text("Brak pasujących usług", color = Navy, fontWeight = FontWeight.SemiBold)
                Text("Zmień wyszukiwaną frazę lub kategorię.", color = Muted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.title }) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                            runCatching { context.startActivity(intent) }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Blue, modifier = Modifier.size(25.dp))
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    if (favorites[service.title] == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (favorites[service.title] == true) "Usuń z ulubionych" else "Dodaj do ulubionych",
                                    tint = Blue,
                                    modifier = Modifier.size(22.dp).clickable {
                                        val next = favorites[service.title] != true
                                        favorites[service.title] = next
                                        prefs.edit().putBoolean(service.title, next).apply()
                                    }
                                )
                            }
                            Text(service.title, color = Navy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(service.description, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Text(service.category, color = Blue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Otwórz serwis", color = Blue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.size(4.dp))
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Blue, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
        Text(
            "OWD jest katalogiem odnośników. Logowanie, składanie wniosków i podpisywanie odbywa się w oficjalnym serwisie wskazanym na kafelku.",
            color = Muted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
    }
}
