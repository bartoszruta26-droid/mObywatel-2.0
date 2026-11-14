// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class OfficialDocType(val id:String, val title:String, val fields:List<String>, val note:String = "")
// Dodatkowy katalog służb mundurowych, publicznych, ratowniczych i zawodów ustawowych.
private fun publicServiceDoc(id: String, title: String): OfficialDocType {
    val fields = when {
        id.startsWith("military") -> listOf("Imię i nazwisko", "Stopień wojskowy", "Rodzaj służby wojskowej", "Jednostka / garnizon", "Stanowisko i specjalność wojskowa", "Numer ewidencyjny / legitymacji", "Podstawa powołania / rozkazu", "Data rozpoczęcia i zakończenia służby", "Przydział mobilizacyjny / rezerwa — jeśli dotyczy", "Misja / ćwiczenia / miejsce pełnienia służby", "Dowódca / organ właściwy", "Status służby", "Dokument źródłowy", "Uwagi")
        id.startsWith("police_") || id.startsWith("fire_") || id.startsWith("border_") || id.startsWith("prison_") || id.startsWith("customs_") || id.startsWith("special_") || id.startsWith("protective_") -> listOf("Imię i nazwisko", "Formacja / służba", "Jednostka organizacyjna", "Stopień służbowy", "Stanowisko / komórka / specjalność", "Numer legitymacji służbowej", "Data przyjęcia do służby", "Podstawa mianowania / rozkazu", "Przebieg służby / przydziały", "Zakres zadań i uprawnień", "Przełożony / organ", "Status służbowy", "Dokument źródłowy", "Uwagi")
        id.startsWith("justice_") -> listOf("Imię i nazwisko", "Rodzaj urzędu / funkcji", "Sąd / prokuratura / jednostka", "Wydział / obszar właściwości", "Podstawa powołania / mianowania", "Organ powołujący", "Numer i data aktu", "Data objęcia urzędu", "Kadencja / okres pełnienia funkcji", "Zakres właściwości", "Status urzędu", "Dokument źródłowy", "Uwagi")
        id.startsWith("rescue_") -> listOf("Imię i nazwisko", "Rodzaj ratownictwa / służby", "Jednostka ratownicza", "Stopień / funkcja / poziom kwalifikacji", "Numer uprawnienia / legitymacji", "Podmiot szkolący / certyfikujący", "Data uzyskania kwalifikacji", "Termin ważności / doskonalenia", "Zakres działań ratowniczych", "Obszar / rejon działania", "Status gotowości / członkostwa", "Dokument źródłowy", "Uwagi")
        id.startsWith("diplomatic_") -> listOf("Imię i nazwisko", "Członkostwo w służbie zagranicznej / status", "Stopień dyplomatyczny", "Stanowisko / funkcja", "Placówka zagraniczna", "Państwo przyjmujące", "Okręg konsularny / zakres akredytacji", "Akt mianowania / wyznaczenia", "Data rozpoczęcia misji", "Okres pełnienia funkcji", "Listy uwierzytelniające / exequatur — jeśli dotyczy", "Dokument źródłowy", "Uwagi")
        id.startsWith("education_") -> listOf("Imię i nazwisko", "Rodzaj stanowiska / stopień awansu", "Placówka / uczelnia / instytut", "Przedmiot / dziedzina / dyscyplina", "Podstawa zatrudnienia / powołania", "Akt mianowania / uchwała", "Data uzyskania stopnia / objęcia funkcji", "Wymiar i okres zatrudnienia / kadencja", "Kwalifikacje i dorobek", "Zakres obowiązków / kompetencji", "Status zatrudnienia / funkcji", "Dokument źródłowy", "Uwagi")
        id.startsWith("inspection_") -> listOf("Imię i nazwisko", "Rodzaj inspekcji / organu kontroli", "Właściwy organ / jednostka", "Stanowisko / specjalność", "Numer legitymacji służbowej", "Upoważnienie do kontroli — numer i data", "Podstawa prawna", "Zakres i terytorialna właściwość kontroli", "Data rozpoczęcia pełnienia funkcji", "Okres ważności upoważnienia", "Status", "Dokument źródłowy", "Uwagi")
        id.startsWith("civil_service_") || id.startsWith("civil_tax_") || id.startsWith("civil_government_") || id.startsWith("civil_voivodeship_") -> listOf("Imię i nazwisko", "Status w korpusie służby cywilnej", "Urząd / organ administracji", "Stanowisko / komórka organizacyjna", "Stopień służbowy — jeśli dotyczy", "Podstawa zatrudnienia / mianowania", "Numer i data aktu", "Data rozpoczęcia pracy / służby", "Poziom stanowiska / zakres odpowiedzialności", "Przełożony / dyrektor generalny", "Status zatrudnienia", "Dokument źródłowy", "Uwagi")
        else -> listOf("Imię i nazwisko", "Rodzaj mandatu / urzędu / funkcji publicznej", "Organ / jednostka / wspólnota samorządowa", "Okręg / obszar działania", "Podstawa wyboru / powołania / wyznaczenia", "Organ wyborczy / powołujący", "Numer i data uchwały / aktu / zaświadczenia", "Data objęcia funkcji", "Kadencja / okres pełnienia funkcji", "Zakres kompetencji i odpowiedzialności", "Status mandatu / urzędu", "Dokument źródłowy", "Uwagi")
    }
    return OfficialDocType(id, title, fields, "Wzór ewidencyjny dla funkcji: $title. Status prawny, mandat, urząd, uprawnienia i zakres kompetencji wynikają z właściwego aktu lub dokumentu źródłowego; formularz aplikacji ich samodzielnie nie nadaje ani nie potwierdza.")
}

private val publicServiceDocumentTypes = listOf(
    publicServiceDoc("military", "Żołnierz zawodowy"),
    publicServiceDoc("military_dzsw", "Żołnierz Dobrowolnej Zasadniczej Służby Wojskowej"),
    publicServiceDoc("military_wot", "Żołnierz Terytorialnej Służby Wojskowej (WOT)"),
    publicServiceDoc("military_active_reserve", "Żołnierz aktywnej rezerwy"),
    publicServiceDoc("military_passive_reserve", "Żołnierz pasywnej rezerwy"),
    publicServiceDoc("military_candidate", "Żołnierz służby kandydackiej"),
    publicServiceDoc("military_exercises", "Żołnierz rezerwy podczas ćwiczeń wojskowych"),
    publicServiceDoc("military_gendarmerie", "Żołnierz Żandarmerii Wojskowej"),
    publicServiceDoc("military_foreign_service", "Żołnierz zawodowy w służbie poza granicami państwa"),
    publicServiceDoc("police_preventive", "Policjant służby prewencyjnej"),
    publicServiceDoc("police_criminal", "Policjant służby kryminalnej"),
    publicServiceDoc("police_investigative", "Policjant służby śledczej"),
    publicServiceDoc("police_counterterrorism", "Policjant służby kontrterrorystycznej"),
    publicServiceDoc("police_support", "Policjant służby wspomagającej"),
    publicServiceDoc("police_traffic", "Policjant ruchu drogowego"),
    publicServiceDoc("police_water", "Policjant służby wodnej"),
    publicServiceDoc("police_riot", "Policjant oddziału prewencji"),
    publicServiceDoc("police_cybercrime", "Funkcjonariusz Centralnego Biura Zwalczania Cyberprzestępczości"),
    publicServiceDoc("fire_preparatory", "Strażak PSP w służbie przygotowawczej"),
    publicServiceDoc("fire_permanent", "Strażak PSP w służbie stałej"),
    publicServiceDoc("fire_squad_leader", "Dowódca zastępu PSP"),
    publicServiceDoc("fire_shift_commander", "Dowódca zmiany PSP"),
    publicServiceDoc("fire_officer", "Oficer Państwowej Straży Pożarnej"),
    publicServiceDoc("fire_rescue_unit", "Funkcjonariusz jednostki ratowniczo-gaśniczej PSP"),
    publicServiceDoc("fire_headquarters", "Funkcjonariusz komendy PSP"),
    publicServiceDoc("border_guard", "Funkcjonariusz Straży Granicznej"),
    publicServiceDoc("border_service", "Funkcjonariusz służby granicznej"),
    publicServiceDoc("border_traffic_control", "Funkcjonariusz kontroli ruchu granicznego"),
    publicServiceDoc("border_operational", "Funkcjonariusz operacyjno-śledczy SG"),
    publicServiceDoc("border_aviation", "Funkcjonariusz służby lotniczej SG"),
    publicServiceDoc("border_unit", "Funkcjonariusz jednostki organizacyjnej SG"),
    publicServiceDoc("prison_general", "Funkcjonariusz Służby Więziennej"),
    publicServiceDoc("prison_security", "Funkcjonariusz działu ochrony SW"),
    publicServiceDoc("prison_penitentiary", "Funkcjonariusz działu penitencjarnego SW"),
    publicServiceDoc("prison_records", "Funkcjonariusz działu ewidencji SW"),
    publicServiceDoc("prison_health", "Funkcjonariusz służby zdrowia więziennego"),
    publicServiceDoc("prison_escort", "Funkcjonariusz konwoju SW"),
    publicServiceDoc("customs_general", "Funkcjonariusz Służby Celno-Skarbowej"),
    publicServiceDoc("customs_control", "Funkcjonariusz kontroli celno-skarbowej"),
    publicServiceDoc("customs_economic_crime", "Funkcjonariusz zwalczania przestępczości ekonomicznej"),
    publicServiceDoc("customs_operations", "Funkcjonariusz komórki realizacyjnej SCS"),
    publicServiceDoc("customs_border", "Funkcjonariusz kontroli granicznej celnej"),
    publicServiceDoc("special_abw", "Funkcjonariusz Agencji Bezpieczeństwa Wewnętrznego (ABW)"),
    publicServiceDoc("special_aw", "Funkcjonariusz Agencji Wywiadu (AW)"),
    publicServiceDoc("special_skw", "Funkcjonariusz Służby Kontrwywiadu Wojskowego (SKW)"),
    publicServiceDoc("special_sww", "Funkcjonariusz Służby Wywiadu Wojskowego (SWW)"),
    publicServiceDoc("special_cba", "Funkcjonariusz Centralnego Biura Antykorupcyjnego (CBA)"),
    publicServiceDoc("special_sop", "Funkcjonariusz Służby Ochrony Państwa (SOP)"),
    publicServiceDoc("protective_marshal", "Funkcjonariusz Straży Marszałkowskiej"),
    publicServiceDoc("protective_rail", "Funkcjonariusz Straży Ochrony Kolei (SOK)"),
    publicServiceDoc("protective_municipal", "Strażnik Straży Miejskiej / Gminnej"),
    publicServiceDoc("protective_hunting", "Funkcjonariusz Państwowej Straży Łowieckiej"),
    publicServiceDoc("protective_fisheries", "Funkcjonariusz Państwowej Straży Rybackiej"),
    publicServiceDoc("protective_forest", "Strażnik leśny"),
    publicServiceDoc("protective_road", "Funkcjonariusz Inspekcji Transportu Drogowego (ITD)"),
    publicServiceDoc("civil_service_member", "Członek korpusu służby cywilnej"),
    publicServiceDoc("civil_service_nominated", "Urzędnik służby cywilnej mianowany"),
    publicServiceDoc("civil_service_employee", "Pracownik służby cywilnej"),
    publicServiceDoc("civil_tax_nominated", "Urzędnik mianowany w administracji skarbowej"),
    publicServiceDoc("civil_tax_inspector", "Inspektor kontroli skarbowej"),
    publicServiceDoc("civil_government_employee", "Pracownik administracji rządowej"),
    publicServiceDoc("civil_voivodeship_employee", "Pracownik administracji wojewódzkiej"),
    publicServiceDoc("local_mayor", "Burmistrz"),
    publicServiceDoc("local_city_president", "Prezydent miasta"),
    publicServiceDoc("local_marshal", "Marszałek województwa"),
    publicServiceDoc("local_councillor_municipal", "Radny gminy"),
    publicServiceDoc("local_councillor_county", "Radny powiatu"),
    publicServiceDoc("local_councillor_region", "Radny sejmiku województwa"),
    publicServiceDoc("local_county_board", "Członek zarządu powiatu"),
    publicServiceDoc("local_region_board", "Członek zarządu województwa"),
    publicServiceDoc("local_council_chair", "Przewodniczący rady lub sejmiku"),
    publicServiceDoc("constitutional_deputy", "Poseł na Sejm RP"),
    publicServiceDoc("constitutional_senator", "Senator RP"),
    publicServiceDoc("constitutional_meP", "Poseł do Parlamentu Europejskiego"),
    publicServiceDoc("constitutional_president", "Prezydent RP"),
    publicServiceDoc("constitutional_minister", "Członek Rady Ministrów"),
    publicServiceDoc("constitutional_prime_minister", "Prezes Rady Ministrów"),
    publicServiceDoc("constitutional_voivode", "Wojewoda"),
    publicServiceDoc("constitutional_deputy_voivode", "Wicewojewoda"),
    publicServiceDoc("constitutional_rpo", "Rzecznik Praw Obywatelskich"),
    publicServiceDoc("constitutional_nik", "Prezes Najwyższej Izby Kontroli"),
    publicServiceDoc("justice_common_judge", "Sędzia sądu powszechnego"),
    publicServiceDoc("justice_admin_judge", "Sędzia sądu administracyjnego"),
    publicServiceDoc("justice_supreme_judge", "Sędzia Sądu Najwyższego"),
    publicServiceDoc("justice_military_judge", "Sędzia sądu wojskowego"),
    publicServiceDoc("justice_assessor", "Asesor sądowy"),
    publicServiceDoc("justice_prosecutor", "Prokurator"),
    publicServiceDoc("justice_military_prosecutor", "Prokurator wojskowy"),
    publicServiceDoc("justice_prosecutor_assessor", "Asesor prokuratorski"),
    publicServiceDoc("justice_court_clerk", "Referendarz sądowy"),
    publicServiceDoc("justice_probation_officer", "Kurator zawodowy"),
    publicServiceDoc("justice_bailiff", "Komornik sądowy"),
    publicServiceDoc("rescue_paramedic", "Ratownik medyczny systemu PRM"),
    publicServiceDoc("rescue_dispatcher", "Dyspozytor medyczny"),
    publicServiceDoc("rescue_gopr", "Ratownik górski GOPR"),
    publicServiceDoc("rescue_topr", "Ratownik górski TOPR"),
    publicServiceDoc("rescue_water", "Ratownik wodny"),
    publicServiceDoc("rescue_professional", "Członek zawodowej służby ratownictwa"),
    publicServiceDoc("rescue_volunteer_fire", "Członek ochotniczej straży pożarnej"),
    publicServiceDoc("rescue_osp_firefighter", "Strażak ratownik OSP"),
    publicServiceDoc("rescue_civil_defence", "Członek obrony cywilnej"),
    publicServiceDoc("rescue_population_protection", "Osoba wyznaczona do funkcji w ochronie ludności"),
    publicServiceDoc("education_nominated_teacher", "Nauczyciel mianowany"),
    publicServiceDoc("education_diploma_teacher", "Nauczyciel dyplomowany"),
    publicServiceDoc("education_appointed_teacher", "Nauczyciel zatrudniony na podstawie mianowania"),
    publicServiceDoc("education_academic_teacher", "Nauczyciel akademicki"),
    publicServiceDoc("education_university_professor", "Profesor uczelni"),
    publicServiceDoc("education_rector", "Rektor uczelni"),
    publicServiceDoc("education_prorector", "Prorektor"),
    publicServiceDoc("education_science_director", "Dyrektor instytucji naukowej"),
    publicServiceDoc("inspection_auditor", "Biegły rewident wykonujący funkcję publiczną"),
    publicServiceDoc("inspection_labour", "Inspektor Państwowej Inspekcji Pracy"),
    publicServiceDoc("inspection_building", "Inspektor nadzoru budowlanego"),
    publicServiceDoc("inspection_sanitary", "Inspektor sanitarny"),
    publicServiceDoc("inspection_environment", "Inspektor ochrony środowiska"),
    publicServiceDoc("inspection_veterinary", "Inspektor weterynaryjny"),
    publicServiceDoc("inspection_monuments", "Funkcjonariusz Służby Ochrony Zabytków"),
    publicServiceDoc("inspection_state_control", "Pracownik organu kontroli państwowej")
)


private fun serviceRecordDoc(id: String, title: String, fields: List<String>): OfficialDocType =
    OfficialDocType(
        id = id,
        title = title,
        fields = fields,
        note = "Wzór ewidencyjny dokumentu służbowego. Treść, skutki prawne, status służby, stanowisko, stopień i uprawnienia wynikają wyłącznie z aktu lub dokumentu wystawionego przez właściwy organ albo pracodawcę. Formularz aplikacji nie zastępuje oryginału."
    )

private val generalServiceRecordDocumentTypes = listOf(
    serviceRecordDoc("service_record_acceptance_decision", "Decyzja o przyjęciu do służby", listOf(
        "Imię i nazwisko osoby przyjmowanej", "Rodzaj służby / formacja", "Organ wydający decyzję", "Numer i data decyzji",
        "Podstawa prawna i tryb naboru", "Jednostka organizacyjna / miejsce pełnienia służby",
        "Data przyjęcia / dzień rozpoczęcia służby", "Warunki lub zastrzeżenia decyzji", "Osoba podpisująca / stanowisko",
        "Data doręczenia", "Status decyzji", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_acceptance_order", "Rozkaz o przyjęciu do służby", listOf(
        "Imię i nazwisko", "Rodzaj służby / formacja", "Jednostka wydająca rozkaz", "Numer i data rozkazu",
        "Podstawa prawna / decyzja poprzedzająca", "Data rozpoczęcia służby", "Stanowisko / przydział",
        "Przełożony / dowódca", "Termin stawiennictwa", "Potwierdzenie wykonania rozkazu", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_personnel_appointment_order", "Rozkaz personalny o mianowaniu do służby", listOf(
        "Imię i nazwisko", "Formacja / rodzaj służby", "Organ personalny wydający rozkaz", "Numer i data rozkazu",
        "Podstawa prawna", "Rodzaj mianowania / status służby", "Data skuteczności mianowania",
        "Stanowisko / jednostka", "Stopień służbowy — jeśli dotyczy", "Data doręczenia", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_position_appointment", "Akt mianowania na stanowisko", listOf(
        "Imię i nazwisko", "Nazwa stanowiska", "Organ / osoba mianująca", "Jednostka organizacyjna",
        "Numer i data aktu", "Podstawa prawna / regulaminowa", "Data objęcia stanowiska",
        "Zakres odpowiedzialności", "Okres powierzenia / kadencja", "Warunki odwołania", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_rank_appointment", "Akt mianowania na stopień służbowy", listOf(
        "Imię i nazwisko", "Formacja / rodzaj służby", "Dotychczasowy stopień", "Nadany stopień",
        "Organ mianujący", "Numer i data aktu", "Podstawa prawna", "Data nadania stopnia",
        "Data wejścia w życie", "Uzasadnienie / wyróżnienie — jeśli wskazano", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_contract", "Umowa lub kontrakt służbowy", listOf(
        "Imię i nazwisko / dane strony", "Formacja / pracodawca / jednostka", "Rodzaj umowy lub kontraktu",
        "Numer i data zawarcia", "Podstawa prawna", "Stanowisko / zakres służby", "Data rozpoczęcia",
        "Czas trwania / okres obowiązywania", "Warunki rozwiązania / wygaśnięcia", "Strony i osoby podpisujące",
        "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_public_appointment", "Akt powołania na stanowisko publiczne", listOf(
        "Imię i nazwisko", "Stanowisko publiczne", "Organ powołujący", "Numer i data aktu",
        "Podstawa prawna", "Data powołania / objęcia stanowiska", "Kadencja / okres pełnienia funkcji",
        "Zakres kompetencji wynikający z aktu", "Warunki odwołania / wygaśnięcia", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_public_election", "Akt wyboru na stanowisko publiczne", listOf(
        "Imię i nazwisko", "Stanowisko / mandat", "Organ wyborczy", "Numer i data uchwały / zaświadczenia",
        "Rodzaj i data wyborów", "Okręg / obszar wyborczy", "Data objęcia funkcji / rozpoczęcia kadencji",
        "Długość kadencji", "Wynik / podstawa stwierdzenia wyboru", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_oath", "Akt ślubowania albo protokół jego złożenia", listOf(
        "Imię i nazwisko składającego ślubowanie", "Stanowisko / funkcja / służba", "Treść roty ślubowania",
        "Data i miejsce złożenia", "Organ / osoba przyjmująca ślubowanie", "Podstawa prawna",
        "Świadkowie / osoby obecne", "Numer i data protokołu", "Podpisy", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_official_id", "Legitymacja służbowa lub funkcjonariusza", listOf(
        "Imię i nazwisko", "Formacja / urząd / pracodawca", "Numer legitymacji", "Stopień / stanowisko",
        "Jednostka organizacyjna", "Data wydania", "Termin ważności", "Organ wydający",
        "Zakres identyfikacji / uprawnień wskazany w dokumencie", "Status dokumentu", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_service_certificate", "Zaświadczenie o pełnieniu służby", listOf(
        "Imię i nazwisko", "Rodzaj służby / formacja", "Jednostka / organ wystawiający",
        "Numer i data zaświadczenia", "Okres potwierdzanego pełnienia służby", "Stanowisko / stopień",
        "Cel wydania", "Podstawa danych ewidencyjnych", "Osoba upoważniona do podpisu",
        "Data wystawienia", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_discharge_certificate", "Świadectwo służby", listOf(
        "Imię i nazwisko", "Rodzaj służby / formacja", "Jednostka wystawiająca",
        "Numer i data świadectwa", "Data rozpoczęcia służby", "Data zakończenia służby",
        "Stanowiska / stopnie w okresie służby", "Sposób i podstawa zwolnienia",
        "Informacje o przebiegu służby", "Data wydania", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_personnel_history", "Karta przebiegu służby albo wyciąg z ewidencji personalnej", listOf(
        "Imię i nazwisko", "Numer ewidencyjny", "Formacja / jednostka prowadząca ewidencję",
        "Numer i data karty / wyciągu", "Chronologia stanowisk i przydziałów", "Chronologia stopni i awansów",
        "Okresy służby", "Przeniesienia / delegowania", "Wyróżnienia / odznaczenia — jeśli ujęto",
        "Organ sporządzający", "Data sporządzenia", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_transfer_order", "Rozkaz o przeniesieniu, delegowaniu lub oddelegowaniu", listOf(
        "Imię i nazwisko", "Formacja / jednostka dotychczasowa", "Jednostka docelowa / miejsce delegowania",
        "Rodzaj czynności: przeniesienie / delegowanie / oddelegowanie", "Numer i data rozkazu",
        "Organ wydający", "Podstawa prawna / służbowa", "Data rozpoczęcia i zakończenia",
        "Stanowisko / zakres zadań", "Warunki powrotu / odwołania", "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_promotion_order", "Rozkaz o awansie albo nadaniu stopnia", listOf(
        "Imię i nazwisko", "Formacja / jednostka", "Dotychczasowy stopień / stanowisko",
        "Nowy stopień / stanowisko", "Rodzaj awansu / wyróżnienia", "Numer i data rozkazu",
        "Organ wydający", "Podstawa prawna", "Data skuteczności awansu", "Uzasadnienie — jeśli wskazano",
        "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_discharge_order", "Decyzja lub rozkaz o zwolnieniu ze służby", listOf(
        "Imię i nazwisko", "Rodzaj służby / formacja", "Organ wydający", "Rodzaj aktu: decyzja / rozkaz",
        "Numer i data aktu", "Podstawa prawna", "Przyczyna / tryb zwolnienia — jeśli wskazano",
        "Data ustania stosunku służbowego", "Sposób doręczenia", "Pouczenie / tryb odwoławczy",
        "Dokument źródłowy", "Uwagi"
    )),
    serviceRecordDoc("service_record_opinion", "Opinia o służbie", listOf(
        "Imię i nazwisko", "Formacja / jednostka", "Okres objęty opinią", "Stanowisko / stopień",
        "Organ / przełożony sporządzający opinię", "Numer i data opinii", "Ocena realizacji zadań",
        "Kwalifikacje / szkolenia / osiągnięcia", "Uwagi / zastrzeżenia", "Data zapoznania się",
        "Dokument źródłowy", "Uwagi dodatkowe"
    )),
    serviceRecordDoc("service_record_periods_certificate", "Zaświadczenie o okresach służby", listOf(
        "Imię i nazwisko", "Rodzaj służby / formacja", "Jednostka / organ wystawiający",
        "Numer i data zaświadczenia", "Wykaz okresów służby od–do", "Przerwy / okresy nieuwzględniane",
        "Łączny staż służby według wystawcy", "Cel zaświadczenia", "Podstawa ewidencyjna",
        "Data wystawienia", "Osoba upoważniona do podpisu", "Dokument źródłowy", "Uwagi"
    ))
)

private val officialDocTypes = listOf(
    OfficialDocType("birth_extract","Akt urodzenia / odpis aktu urodzenia", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwisko osoby","Data i miejsce urodzenia","Imiona i nazwiska rodziców","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy rodziców / osób potwierdzających","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej, np. przez rodzica. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("marriage_extract","Akt małżeństwa / odpis aktu małżeństwa", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwiska małżonków","Data i miejsce zawarcia małżeństwa","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy małżonków / rodziców / osób potwierdzających","Świadkowie","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("death_extract","Odpis aktu zgonu", listOf("Imiona i nazwisko osoby zmarłej","Data i miejsce zgonu","Urząd stanu cywilnego","Numer aktu / rok","Rodzaj odpisu","Wnioskodawca i podstawa uprawnienia","Cel uzyskania","Sposób odbioru"), "Formularz danych do wniosku do USC. Odpis wydaje kierownik USC, nie starosta."),
    OfficialDocType("large_family","Karta Dużej Rodziny", listOf("Imię i nazwisko","PESEL / data urodzenia","Numer karty","Typ: tradycyjna / elektroniczna","Data wydania","Ważna do","Organ wydający","Status")),
    OfficialDocType("blood_donor","Legitymacja / karta Honorowego Dawcy Krwi", listOf("Imię i nazwisko","Numer legitymacji","Stopień / tytuł","Liczba oddanych litrów (jeśli wpisana)","Data przyznania","Organ / jednostka wydająca","Status")),
    OfficialDocType("disability_card","Legitymacja osoby z niepełnosprawnością", listOf("Imię i nazwisko","Numer legitymacji","Data wydania","Data ważności","Organ wydający","Numer orzeczenia / podstawa","Stopień / wskazanie","Status"), "Legitymacja i orzeczenie to odrębne dokumenty; wpis nie zastępuje dokumentu źródłowego."),
    OfficialDocType("vehicle_registration","Dowód rejestracyjny pojazdu", listOf("Numer rejestracyjny","VIN","Marka i model","Numer dowodu rejestracyjnego","Właściciel / współwłaściciel","Data pierwszej rejestracji","Organ rejestrujący","Termin badania technicznego","Adnotacje / ograniczenia","Status")),
    OfficialDocType("insurance_vehicle","Ubezpieczenie OC pojazdu", listOf("Ubezpieczyciel","Numer polisy","Pojazd / numer rejestracyjny","Okres ochrony od","Okres ochrony do","Data opłacenia składki","Zakres / uwagi","Status")),
    OfficialDocType("insurance_professional","Ubezpieczenie OC zawodowe", listOf("Ubezpieczyciel","Numer polisy","Ubezpieczony / zawód","Zakres czynności objętych ochroną","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Wyłączenia / uwagi","Status")),
    OfficialDocType("insurance_private","Ubezpieczenie OC w życiu prywatnym", listOf("Ubezpieczyciel","Numer polisy","Ubezpieczony / gospodarstwo domowe","Zakres ochrony","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Status")),
    OfficialDocType("insurance_business","Ubezpieczenie OC działalności / firmowe", listOf("Ubezpieczyciel","Numer polisy","Nazwa firmy / NIP","Ubezpieczony podmiot","Zakres działalności i ochrony","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Wyłączenia / uwagi","Status")),
    OfficialDocType("criminal_record","Zaświadczenie o niekaralności — KRK", listOf("Imię i nazwisko","PESEL / data urodzenia","Numer zapytania / zaświadczenia","Data wydania","Organ / punkt informacyjny KRK","Zakres informacji","Cel uzyskania","Status weryfikacji pliku PDF/XML"), "Można dołączyć oryginalny plik PDF i/lub XML. Sama nazwa pliku nie potwierdza autentyczności ani aktualności zaświadczenia."),
    OfficialDocType("mission_travel","Dokumenty podróży służbowej i misji międzynarodowych", listOf("Organizacja / pracodawca","Podstawa delegowania / nominacji","Imię i nazwisko delegowanego","Funkcja / rola","Cel i zakres misji","Państwo / państwa docelowe","Data rozpoczęcia","Data zakończenia","Numer decyzji / delegacji","Ubezpieczenie i osoba kontaktowa","Wizy / zgody / akredytacje","Status dokumentów")),
    OfficialDocType("ehic","Europejska Karta Ubezpieczenia Zdrowotnego (EKUZ)", listOf("Imię i nazwisko","Numer karty","Numer identyfikacyjny / PESEL","Instytucja wydająca","Data wydania","Data ważności","Status")),
    OfficialDocType("eu_education","Zaświadczenie o zgodności wykształcenia z wymogami UE", listOf("Imię i nazwisko","Kwalifikacja / zawód","Nazwa i kraj instytucji kształcącej","Poziom i czas kształcenia","Numer dyplomu / świadectwa","Organ wydający zaświadczenie","Podstawa prawna / dyrektywa","Państwo docelowe","Data wydania","Numer sprawy / zaświadczenia","Zakres potwierdzenia","Status")),
    OfficialDocType("mayor","Dokument wójta", listOf("Imię i nazwisko","Gmina","Funkcja: wójt","Data wyboru","Data objęcia obowiązków","Kadencja od / do","Organ wyborczy","Numer i data zaświadczenia o wyborze","Podstawa prawna","Status"), "Dokument ewidencyjny. Wybór na wójta potwierdza właściwa komisja wyborcza; ten wpis nie zastępuje urzędowego zaświadczenia."),
    OfficialDocType("village_head","Dokument sołtysa", listOf("Imię i nazwisko","Sołectwo","Gmina","Data wyboru","Kadencja od / do","Organ / zebranie dokonujące wyboru","Numer i data protokołu wyboru","Statut sołectwa","Status"), "Sołtys jest wybierany zgodnie z ustawą o samorządzie gminnym i statutem sołectwa. Forma dokumentowania wyboru zależy od właściwych regulacji lokalnych."),
    OfficialDocType("county_starosta","Dokument starosty", listOf("Imię i nazwisko","Powiat","Funkcja: starosta","Data wyboru przez radę powiatu","Data objęcia obowiązków","Kadencja od / do","Numer i data uchwały","Organ podejmujący uchwałę","Status"), "Dokument ewidencyjny potwierdzający wybór i pełnienie funkcji; nie zastępuje uchwały rady powiatu."),
    OfficialDocType("citizen_record","Dokument obywatela", listOf("Imię i nazwisko","Obywatelstwo","Państwo","Podstawa nabycia / posiadania obywatelstwa","Organ lub dokument źródłowy","Numer dokumentu źródłowego","Data wydania / potwierdzenia","Status"), "Pomocniczy wpis ewidencyjny. Nie jest odrębnym ustawowym dokumentem tożsamości ani dowodem obywatelstwa; należy dołączyć dokument źródłowy."),
    OfficialDocType("ombudsman","Dokument Rzecznika Praw Obywatelskich", listOf("Imię i nazwisko","Funkcja","Data powołania przez Sejm","Zgoda Senatu — data","Kadencja od / do","Numer i data uchwały / aktu","Organ powołujący","Status"), "Dokument dotyczący konstytucyjnego organu ochrony praw i wolności; rodzaj dokumentu źródłowego należy wskazać w polach."),
    OfficialDocType("children_ombudsman","Dokument Rzecznika Praw Dziecka", listOf("Imię i nazwisko","Funkcja","Data powołania przez Sejm","Zgoda Senatu — data","Kadencja od / do","Numer i data uchwały / aktu","Organ powołujący","Status"), "Dokument dotyczący organu ochrony praw dziecka; rodzaj dokumentu źródłowego należy wskazać w polach."),
    OfficialDocType("patient_ombudsman","Dokument Rzecznika Praw Pacjenta", listOf("Imię i nazwisko","Funkcja","Data powołania","Kadencja / okres pełnienia funkcji","Numer i data aktu powołania","Organ powołujący","Status"), "Dokument dotyczący centralnego organu administracji rządowej właściwego w sprawach ochrony praw pacjenta."),
    OfficialDocType("pretrial_mediator","Dokument mediatora przedsądowego", listOf("Imię i nazwisko","Rodzaj mediacji","Podstawa wykonywania mediacji","Organizacja / ośrodek mediacyjny","Numer wpisu / zaświadczenia","Data uzyskania kwalifikacji","Zakres specjalizacji","Okres / status uprawnienia"), "Określenie „mediator przedsądowy” nie oznacza odrębnej państwowej służby ani jednolitej licencji. Wpis służy ewidencji dokumentów potwierdzających kwalifikacje, wpis na listę lub współpracę z ośrodkiem."),
    OfficialDocType("milicja_officer","Archiwalny dokument funkcjonariusza Milicji Obywatelskiej", listOf("Imię i nazwisko","Stopień","Jednostka organizacyjna MO","Stanowisko / funkcja","Numer legitymacji / dokumentu","Data przyjęcia do służby","Data zwolnienia","Okres służby","Archiwalny organ wydający"), "Wyłącznie dokument historyczny dotyczący nieistniejącej już Milicji Obywatelskiej. Nie potwierdza współczesnego statusu funkcjonariusza."),
    OfficialDocType("foreign_service_member","Dokument członka służby zagranicznej", listOf("Imię i nazwisko","Stanowisko / funkcja","Stopień służbowy — jeśli dotyczy","Jednostka organizacyjna MSZ","Placówka zagraniczna","Data nawiązania stosunku pracy / służby","Numer i data aktu","Status"), "Należy rozróżniać członka służby zagranicznej od pracownika placówki, który nie musi mieć statusu członka służby zagranicznej."),
    OfficialDocType("diplomat","Dokument dyplomaty", listOf("Imię i nazwisko","Stopień dyplomatyczny","Stanowisko","Państwo / placówka","Data nadania stopnia","Numer i data aktu","Organ wydający","Okres pełnienia funkcji","Status"), "Wzór ewidencyjny; stopnie i status wynikają z właściwych przepisów o służbie zagranicznej."),
    OfficialDocType("consul","Dokument konsula", listOf("Imię i nazwisko","Funkcja konsularna","Okręg konsularny","Państwo przyjmujące","Data mianowania / powołania","Exequatur — data, jeśli dotyczy","Numer i data dokumentu","Okres pełnienia funkcji","Status"), "Należy odróżnić akt wyznaczenia konsula od zgody państwa przyjmującego na wykonywanie funkcji konsularnych."),
    OfficialDocType("ambassador","Dokument ambasadora", listOf("Imię i nazwisko","Państwo przyjmujące","Nazwa placówki","Data mianowania","Data rozpoczęcia misji","Listy uwierzytelniające — data","Numer i data aktu","Organ wydający","Status"), "Dokument ewidencyjny. Funkcję ambasadora i rozpoczęcie misji potwierdzają właściwe akty oraz procedura akredytacji."),
    OfficialDocType("foreign_mission_head","Dokument kierownika placówki zagranicznej", listOf("Imię i nazwisko","Rodzaj placówki","Nazwa placówki","Państwo siedziby","Funkcja","Data wyznaczenia","Data objęcia obowiązków","Numer i data aktu","Okres pełnienia funkcji","Status"), "Może dotyczyć ambasady, konsulatu lub stałego przedstawicielstwa; nie każdy kierownik placówki ma rangę ambasadora."),
    OfficialDocType("consular_employee","Dokument pracownika konsularnego", listOf("Imię i nazwisko","Placówka konsularna","Stanowisko","Państwo","Status zatrudnienia","Data rozpoczęcia pracy","Numer i data dokumentu","Zakres czynności","Status"), "Dokument ewidencyjny pracownika placówki; sam wpis nie nadaje statusu urzędnika konsularnego."),
    OfficialDocType("inspector","Dokument inspektora", listOf("Imię i nazwisko","Rodzaj inspekcji / inspektoratu","Organ / jednostka zatrudniająca","Stanowisko","Zakres uprawnień kontrolnych","Numer legitymacji / upoważnienia","Data wydania","Okres ważności","Podstawa prawna","Status"), "Pozycja ogólna. Należy wskazać konkretną inspekcję, np. Państwową Inspekcję Pracy, Inspekcję Transportu Drogowego, Inspekcję Sanitarną lub Inspekcję Ochrony Środowiska."),
    OfficialDocType("org_president","Dokument prezesa organizacji", listOf("Imię i nazwisko","Nazwa organizacji","Forma prawna / rodzaj organizacji","Podstawa pełnienia funkcji: wybór / powołanie / umowa","Numer i data uchwały lub aktu","Kadencja / okres pełnienia funkcji","Zakres reprezentacji","Sposób reprezentacji","Status"), "Dokument wewnętrzny organizacji. Nie potwierdza pełnienia funkcji organu państwowego; umocowanie wynika ze statutu, KRS, uchwały lub właściwej umowy."),
    OfficialDocType("org_general_director","Dokument dyrektora generalnego", listOf("Imię i nazwisko","Nazwa organizacji","Jednostka organizacyjna","Podstawa powierzenia funkcji","Numer i data aktu / uchwały","Okres pełnienia funkcji","Zakres obowiązków i umocowania","Osoba / organ nadzorujący","Status"), "Cywilna funkcja kierownicza w organizacji; zakres kompetencji określa statut, regulamin, uchwała lub umowa."),
    OfficialDocType("org_regional_director","Dokument dyrektora regionalnego", listOf("Imię i nazwisko","Nazwa organizacji","Region / województwo","Siedziba lub obszar działania","Podstawa powierzenia funkcji","Numer i data aktu","Okres pełnienia funkcji","Zakres kompetencji","Status"), "Funkcja regionalna w strukturze organizacji. Nie jest stanowiskiem wojewody ani organu administracji publicznej."),
    OfficialDocType("org_county_director","Dokument dyrektora powiatowego", listOf("Imię i nazwisko","Nazwa organizacji","Powiat / obszar działania","Jednostka organizacyjna","Podstawa powierzenia funkcji","Numer i data aktu","Okres pełnienia funkcji","Zakres kompetencji","Status"), "Funkcja organizacyjna o zasięgu powiatowym; nie jest stanowiskiem starosty ani urzędnika powiatowego."),
    OfficialDocType("org_local_representative","Dokument przedstawiciela lokalnego", listOf("Imię i nazwisko","Nazwa organizacji","Miejscowość / obszar reprezentacji","Podstawa ustanowienia","Numer i data pełnomocnictwa / aktu","Okres reprezentacji","Zakres reprezentacji","Organ / osoba udzielająca umocowania","Status"), "Potwierdza reprezentowanie organizacji w określonym zakresie. Nie nadaje kompetencji organu gminy ani sołtysa."),
    OfficialDocType("org_general_secretary","Dokument sekretarza generalnego", listOf("Imię i nazwisko","Nazwa organizacji","Podstawa wyboru / powołania","Numer i data uchwały / aktu","Okres pełnienia funkcji","Zakres obowiązków","Organ nadzorujący","Status"), "Wewnętrzna funkcja organizacyjna; kompetencje wynikają ze statutu i regulaminów organizacji."),
    OfficialDocType("org_treasurer","Dokument skarbnika organizacji", listOf("Imię i nazwisko","Nazwa organizacji","Podstawa wyboru / powołania","Numer i data uchwały / aktu","Okres pełnienia funkcji","Zakres odpowiedzialności finansowej","Zasady reprezentacji / akceptacji wydatków","Status"), "Potwierdza funkcję w strukturze organizacji. Nie jest powołaniem na skarbnika jednostki samorządu terytorialnego."),
    OfficialDocType("org_members_rights_ombudsman","Dokument rzecznika praw członków", listOf("Imię i nazwisko","Nazwa organizacji","Zakres ochrony praw członków","Podstawa powołania","Numer i data aktu","Okres pełnienia funkcji","Tryb przyjmowania skarg / wniosków","Status"), "Rzecznik wewnętrzny organizacji. Nie jest Rzecznikiem Praw Obywatelskich ani organem administracji publicznej."),
    OfficialDocType("org_patient_ombudsman","Dokument rzecznika praw pacjenta organizacji", listOf("Imię i nazwisko","Nazwa organizacji / podmiotu leczniczego","Zakres działania","Podstawa powołania","Numer i data aktu","Okres pełnienia funkcji","Dane kontaktowe / jednostka","Status"), "Funkcja wewnętrzna lub społeczna. Nie jest ustawowym Rzecznikiem Praw Pacjenta i nie przejmuje jego kompetencji."),
    OfficialDocType("org_family_ombudsman","Dokument rzecznika praw rodziny", listOf("Imię i nazwisko","Nazwa organizacji","Zakres ochrony praw rodziny","Podstawa powołania","Numer i data aktu","Okres pełnienia funkcji","Zakres wsparcia / reprezentacji","Status"), "Funkcja organizacyjna lub społeczna; nie jest ustawowym Rzecznikiem Praw Dziecka ani organem władzy publicznej."),
    OfficialDocType("civil_mediator","Dokument mediatora", listOf("Imię i nazwisko","Rodzaj mediacji","Specjalizacja","Kwalifikacje / szkolenia","Lista mediatorów / ośrodek","Numer i data wpisu / zaświadczenia","Okres / status wpisu","Zakres prowadzonych mediacji"), "Potwierdza kwalifikacje, wpis lub współpracę. Sam dokument organizacji nie nadaje statusu stałego mediatora ani uprawnień zastrzeżonych przepisami."),
    OfficialDocType("civil_arbitrator","Dokument arbitra", listOf("Imię i nazwisko","Nazwa sądu arbitrażowego / organizacji","Dziedzina arbitrażu","Podstawa powołania / wpisu","Numer i data aktu","Okres pełnienia funkcji","Regulamin arbitrażowy","Status"), "Funkcja arbitra prywatnego lub organizacyjnego. Nie jest stanowiskiem sędziego państwowego."),
    OfficialDocType("civil_expert","Dokument eksperta", listOf("Imię i nazwisko","Dziedzina ekspertyzy","Nazwa organizacji / instytucji","Wykształcenie i kwalifikacje","Podstawa uznania / powołania","Numer i data certyfikatu / aktu","Zakres ekspertyz","Okres ważności / status"), "Potwierdza status eksperta w określonej organizacji lub dziedzinie; nie oznacza automatycznie wpisu na listę biegłych sądowych."),
    OfficialDocType("civil_appraiser","Dokument rzeczoznawcy", listOf("Imię i nazwisko","Dziedzina rzeczoznawstwa","Numer uprawnień / wpisu, jeśli dotyczy","Organ / organizacja nadająca status","Podstawa i data nadania","Zakres wyceny / opiniowania","Okres ważności / status"), "Należy wskazać, czy chodzi o rzeczoznawcę z uprawnieniami ustawowymi, czy eksperta organizacyjnego. Wpis wewnętrzny nie zastępuje uprawnień regulowanych."),
    OfficialDocType("civil_auditor","Dokument audytora", listOf("Imię i nazwisko","Rodzaj audytu","Dziedzina / standard","Organizacja","Kwalifikacje / certyfikaty","Podstawa powołania / zatrudnienia","Zakres audytu","Okres / status"), "Dokument potwierdza funkcję audytora w określonym podmiocie; nie nadaje automatycznie uprawnień audytora wewnętrznego w rozumieniu przepisów o finansach publicznych."),
    OfficialDocType("civil_internal_inspector","Dokument inspektora wewnętrznego", listOf("Imię i nazwisko","Nazwa organizacji","Obszar nadzoru wewnętrznego","Podstawa powołania","Numer i data aktu","Zakres czynności","Jednostki objęte nadzorem","Okres pełnienia funkcji","Status"), "Inspektor wewnętrzny organizacji. Nie jest państwowym inspektorem ani organem uprawnionym do kontroli zewnętrznej."),
    OfficialDocType("civil_controller","Dokument kontrolera", listOf("Imię i nazwisko","Nazwa organizacji","Rodzaj kontroli","Zakres przedmiotowy i podmiotowy","Podstawa upoważnienia","Numer i data upoważnienia","Okres ważności","Status"), "Upoważnienie dotyczy kontroli wewnętrznej lub umownej w granicach kompetencji organizacji; nie nadaje uprawnień kontrolera państwowego."),
    OfficialDocType("civil_consultant","Dokument konsultanta", listOf("Imię i nazwisko","Dziedzina konsultacji","Nazwa organizacji / podmiotu","Kwalifikacje i doświadczenie","Podstawa powołania / współpracy","Numer i data aktu / umowy","Zakres konsultacji","Okres współpracy","Status"), "Potwierdza rolę konsultanta organizacji; nie jest równoznaczny z funkcją konsultanta krajowego lub wojewódzkiego ustanawianego na podstawie przepisów."),
    OfficialDocType("civil_social_caretaker","Dokument opiekuna społecznego", listOf("Imię i nazwisko","Nazwa organizacji / inicjatywy","Osoba / grupa objęta wsparciem","Obszar i zakres opieki","Podstawa ustanowienia","Numer i data aktu","Okres pełnienia funkcji","Koordynator / jednostka","Status"), "Funkcja społeczna organizacji. Nie jest ustawową funkcją opiekuna społecznego, kuratora sądowego ani pracownika pomocy społecznej, o ile odrębne przepisy nie stanowią inaczej."),
    OfficialDocType("civil_org_curator","Dokument kuratora organizacyjnego", listOf("Imię i nazwisko","Nazwa organizacji","Osoba / grupa objęta wsparciem","Zakres opieki, mentoringu lub nadzoru organizacyjnego","Podstawa powołania","Numer i data aktu","Okres pełnienia funkcji","Osoba / organ nadzorujący","Status"), "Wewnętrzna funkcja opiekuńcza lub mentorska. Nie jest funkcją kuratora sądowego i nie daje kompetencji do wykonywania orzeczeń sądu."),
    OfficialDocType("civil_rescuer","Dokument ratownika cywilnego", listOf("Imię i nazwisko","Rodzaj ratownictwa","Kwalifikacje i szkolenia","Numer certyfikatu / uprawnienia","Organ / podmiot szkolący","Data uzyskania kwalifikacji","Zakres kompetencji","Termin ważności","Jednostka / zespół","Status"), "Należy wskazać rzeczywiste kwalifikacje i zakres uprawnień, np. KPP lub inne szkolenie. Sama legitymacja organizacji nie nadaje uprawnień ratownika medycznego ani służb ratowniczych."),
    OfficialDocType("org_ambassador","Dokument ambasadora organizacji", listOf("Imię i nazwisko","Nazwa organizacji","Państwo / obszar reprezentacji","Podstawa nominacji","Numer i data aktu","Okres reprezentacji","Zakres reprezentacji i umocowania","Organ udzielający umocowania","Status"), "Ambasador organizacji lub marki. Nie jest ambasadorem RP, przedstawicielem dyplomatycznym państwa ani osobą korzystającą z immunitetu dyplomatycznego."),
    OfficialDocType("org_foreign_representative","Dokument przedstawiciela zagranicznego", listOf("Imię i nazwisko","Nazwa organizacji","Państwo / obszar działania","Placówka / przedstawicielstwo","Podstawa ustanowienia","Numer i data aktu","Zakres reprezentacji","Okres pełnienia funkcji","Status"), "Cywilny przedstawiciel organizacji za granicą; nie jest członkiem służby zagranicznej ani urzędnikiem konsularnym z samego tytułu."),
    OfficialDocType("org_proxy","Dokument pełnomocnika organizacji", listOf("Imię i nazwisko pełnomocnika","Nazwa organizacji / mocodawcy","Rodzaj pełnomocnictwa","Zakres umocowania","Data udzielenia","Okres obowiązywania","Numer / identyfikator dokumentu","Osoba / organ udzielający","Sposób odwołania / status"), "Zakres działania wynika z treści pełnomocnictwa i przepisów Kodeksu cywilnego lub przepisów szczególnych. Legitymacja nie zastępuje pełnomocnictwa."),
    OfficialDocType("org_delegate","Dokument delegata organizacji", listOf("Imię i nazwisko","Nazwa organizacji delegującej","Organizacja / wydarzenie / organ, do którego delegowano","Podstawa delegowania","Numer i data uchwały / aktu","Zakres reprezentacji i głosowania","Okres delegowania","Status"), "Potwierdza delegowanie do reprezentowania organizacji w określonej sprawie lub na określonym forum; nie nadaje mandatu publicznego.")
) + generalServiceRecordDocumentTypes + publicServiceDocumentTypes

internal fun officialGraphicDocumentTargets(): List<Pair<String, String>> =
    officialDocTypes.map { it.id to it.title }

internal fun officialGraphicDocumentFields(documentId: String): List<Pair<String, String>> =
    officialDocTypes.firstOrNull { it.id == documentId }?.fields.orEmpty()
        .mapIndexed { index, label -> "field_$index" to label }

internal object OfficialDocumentStore {
    private const val ALIAS = "obywatel_official_docs_aes"
    private const val INDEX = "official-documents.enc"
    private const val DIR = "official-document-files"
    private fun key(): SecretKey {
        val ks=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS,null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes:ByteArray, encrypt:Boolean):ByteArray {
        val c=Cipher.getInstance("AES/GCM/NoPadding")
        if(encrypt){ c.init(Cipher.ENCRYPT_MODE,key()); return c.iv+c.doFinal(bytes) }
        require(bytes.size>12); c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12))); return c.doFinal(bytes.copyOfRange(12,bytes.size))
    }
    private fun file(context:Context)=File(context.noBackupFilesDir,INDEX)
    fun load(context:Context):Map<String,Map<String,String>> = runCatching {
        val f=file(context); if(!f.exists()) return emptyMap()
        val o=JSONObject(String(crypt(f.readBytes(),false),Charsets.UTF_8)); val out=mutableMapOf<String,Map<String,String>>()
        o.keys().forEach { id -> val item=o.getJSONObject(id); val m=mutableMapOf<String,String>(); item.keys().forEach { k->m[k]=item.getString(k) }; out[id]=m }
        out
    }.getOrDefault(emptyMap())
    fun save(context:Context, data:Map<String,Map<String,String>>) {
        val root=JSONObject(); data.forEach { (id,values)-> val item=JSONObject(); values.forEach { (k,v)->item.put(k,v) }; root.put(id,item) }
        val target=file(context); val tmp=File(target.parentFile,target.name+".tmp"); tmp.writeBytes(crypt(root.toString().toByteArray(Charsets.UTF_8),true))
        if(!tmp.renameTo(target)){tmp.copyTo(target,true);tmp.delete()}
    }
    fun addAttachment(context:Context, uri:Uri, docId:String):String {
        val bytes=context.contentResolver.openInputStream(uri)?.use { input ->
            val out=java.io.ByteArrayOutputStream(); val buf=ByteArray(8192); var total=0
            while(true){val n=input.read(buf);if(n<0)break;total+=n;require(total<=25*1024*1024){"Plik przekracza limit 25 MB."};out.write(buf,0,n)}
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        require(bytes.isNotEmpty())
        val name=runCatching { context.contentResolver.query(uri,arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),null,null,null)?.use { if(it.moveToFirst()) it.getString(0) else null } }.getOrNull() ?: "zalacznik"
        val lower=name.lowercase()
        require(lower.endsWith(".pdf") || lower.endsWith(".xml")) { "Do tego formularza można dołączyć pliki PDF lub XML." }
        if(lower.endsWith(".pdf")) require(bytes.size>=5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Nieprawidłowy nagłówek PDF." }
        if(lower.endsWith(".xml")) {
            val text=String(bytes,Charsets.UTF_8)
            require(text.trimStart().startsWith("<?xml") || text.trimStart().startsWith("<")) { "Plik XML ma nieprawidłową strukturę początkową." }
            require(!text.contains("<!DOCTYPE",true) && !text.contains("<!ENTITY",true)) { "XML z deklaracją DTD/ENTITY jest odrzucany ze względów bezpieczeństwa." }
        }
        val dir=File(context.noBackupFilesDir,DIR).apply { mkdirs() }; val id=UUID.randomUUID().toString()
        File(dir,"$id.enc").writeBytes(crypt(bytes,true))
        val index=File(dir,"index.enc")
        val current=runCatching { if(index.exists()) JSONArray(String(crypt(index.readBytes(),false))) else JSONArray() }.getOrDefault(JSONArray())
        current.put(JSONObject().put("id",id).put("docId",docId).put("name",name).put("size",bytes.size))
        val tmp=File(dir,"index.tmp"); tmp.writeBytes(crypt(current.toString().toByteArray(Charsets.UTF_8),true)); if(!tmp.renameTo(index)){tmp.copyTo(index,true);tmp.delete()}
        return name
    }
    fun attachments(context:Context, docId:String):List<String> = runCatching {
        val f=File(File(context.noBackupFilesDir,DIR),"index.enc"); if(!f.exists()) return emptyList()
        val a=JSONArray(String(crypt(f.readBytes(),false))); (0 until a.length()).map { a.getJSONObject(it) }.filter { it.optString("docId")==docId }.map { it.optString("name") }
    }.getOrDefault(emptyList())

    fun exportArchive(context: Context): String {
        val root = JSONObject()
        load(context).forEach { (documentId, fields) ->
            val item = JSONObject()
            fields.forEach { (key, value) -> item.put(key, value) }
            root.put(documentId, item)
        }
        return root.toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        val restored = linkedMapOf<String, Map<String, String>>()
        root.keys().forEach { documentId ->
            require(documentId.matches(Regex("[A-Za-z0-9_-]{1,96}"))) { "Nieprawidłowy identyfikator dokumentu." }
            val item = root.getJSONObject(documentId)
            val fields = linkedMapOf<String, String>()
            item.keys().forEach { key ->
                require(key.isNotBlank() && key.length <= 256) { "Nieprawidłowa nazwa pola dokumentu." }
                fields[key] = item.getString(key)
            }
            restored[documentId] = fields
        }
        save(context, restored)
    }
}

@Composable
internal fun OfficialDocumentsScreen(onBack:()->Unit, readOnly:Boolean=false, initialDocumentId:String?=null, onEditGraphic:(String)->Unit = {}) {
    val context= LocalContext.current
    var records by remember { mutableStateOf(OfficialDocumentStore.load(context)) }
    var selected by remember(initialDocumentId) { mutableStateOf(initialDocumentId) }
    var status by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    val attachmentPicker= rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val id=selected
        if(id!=null && uris.isNotEmpty()) runCatching {
            uris.forEach { OfficialDocumentStore.addAttachment(context,it,id) }
        }.onSuccess { status="Dodano załączniki. Pliki zapisano w zaszyfrowanym archiwum lokalnym."; refresh++ }
            .onFailure { status="Błąd załącznika: ${it.message}" }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.SemiBold, modifier=Modifier.clickable(onClick=onBack).padding(vertical=5.dp))
        Text(if(readOnly) "Dokumenty urzędowe i ubezpieczeniowe" else "Formularze i ewidencja dokumentów", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text("Wprowadzaj dane zgodnie z dokumentem źródłowym. Pliki i wpisy zachowują status nadany im przez właściwego wystawcę.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(selected==null) {
            officialDocTypes.forEach { type ->
                val values=records[type.id].orEmpty()
                val populated=values.values.any { it.isNotBlank() }
                Card(Modifier.fillMaxWidth().clickable { selected=type.id }, colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Description,contentDescription=null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(30.dp))
                        Column(Modifier.weight(1f)) {
                            Text(type.title,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
                            Text(if(populated) "Dane zapisane" else "Brak danych",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            if (!readOnly) TextButton(onClick = { onEditGraphic(type.id) }) { Text("Wygląd") }
                            Text("Otwórz",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        } else {
            val type=officialDocTypes.first { it.id==selected }
            val values=records[type.id].orEmpty()
            val settingsState = rememberLegitimacyPreviewSettings(type.id)
            val local = remember(type.id, records) { mutableStateMapOf<String, String>().apply { putAll(values) } }
            Text(type.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)
            if(type.note.isNotBlank()) Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) {
                Text(type.note,Modifier.padding(12.dp),color=MaterialTheme.colorScheme.onSurface,style=MaterialTheme.typography.bodySmall)
            }
            if (readOnly) {
                val previewSettings = settingsState.value
                val previewFields = type.fields.mapIndexed { index, label ->
                    Triple("field_$index", label.uppercase(), local["f$index"].orEmpty().ifBlank { "—" })
                }
                CredentialCardSides(
                    documentKey = type.id,
                    title = type.title.uppercase(),
                    subtitle = "PODGLĄD EWIDENCYJNY",
                    holderName = local.values.firstOrNull { it.isNotBlank() } ?: "DANE DOKUMENTU",
                    fields = previewFields,
                    settings = previewSettings
                )
                Text("Podgląd wyłącznie do odczytu. Edycja jest dostępna w Więcej → Ustawienia → dokumenty cywilne.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            } else {
                if (!LocalDocumentOcrStageRendered.current) {
                    GeneralDocumentOcrEditor("civil", local, context) { status = it }
                }
                type.fields.forEachIndexed { index, label ->
                    DocumentFieldEditor(
                        documentKey = type.id,
                        fieldKey = "field_$index",
                        label = label,
                        settingsState = settingsState,
                        value = local["f$index"].orEmpty(),
                        onValueChange = { local["f$index"] = it },
                        minLines = if (index >= 6) 2 else 1,
                        maxLines = 4
                    )
                }
                DocumentOriginalAttachments(type.id)
            }
            if (!readOnly) {
                OutlinedButton(onClick = { onEditGraphic(type.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edytuj wygląd graficzny")
                }
                val previewFields = type.fields.mapIndexed { index, label ->
                    Triple("field_$index", label.uppercase(), local["f$index"].orEmpty().ifBlank { "—" })
                }
                Text("Podgląd dokumentu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                CredentialCardSides(
                    documentKey = type.id,
                    title = type.title.uppercase(),
                    subtitle = "PODGLĄD EWIDENCYJNY",
                    holderName = local.values.firstOrNull { it.isNotBlank() } ?: "DANE DOKUMENTU",
                    fields = previewFields,
                    settings = settingsState.value
                )
            }
            if(type.id=="criminal_record") {
                if (!readOnly) OutlinedButton(onClick={attachmentPicker.launch(arrayOf("application/pdf","text/xml","application/xml"))},modifier=Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.FolderOpen,contentDescription=null); Spacer(Modifier.width(8.dp)); Text("Dodaj oryginalny plik PDF lub XML")
                }
                OfficialDocumentStore.attachments(context,type.id).forEach { Text("Załącznik: $it",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall) }
                Text("Plik PDF/XML jest przechowywany jako załącznik. Aplikacja nie weryfikuje podpisu elektronicznego, pieczęci ani statusu KRK.",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
            }
            if(!readOnly) Button(onClick={
                runCatching { OfficialDocumentStore.save(context,records+(type.id to local)); records=OfficialDocumentStore.load(context); status="Zapisano dane lokalnie." }
                    .onFailure { status="Błąd zapisu: ${it.message}" }
            },modifier=Modifier.fillMaxWidth()) { Text("Zapisz zmiany") }
            DocumentPdfExportButton(type.title, local.mapKeys { (key, _) ->
                if (key.startsWith("f")) type.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key
            }, documentType = type.id)
            if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={selected=null;status=""}) { Text("Wróć do katalogu") }
        }
        Text("Dane są szyfrowane lokalnie w pamięci aplikacji. Aplikacja nie pobiera danych z rejestrów państwowych i nie wystawia dokumentów w imieniu organów.",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelSmall)
    }
}
