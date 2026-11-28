// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

internal data class OfficialDocType(val id:String, val title:String, val fields:List<String>, val note:String = "")

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

internal val publicServiceDocumentTypes = listOf(
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

internal val generalServiceRecordDocumentTypes = listOf(
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

internal val officialDocTypes = listOf(
    OfficialDocType("birth_extract","Akt urodzenia / odpis aktu urodzenia", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwisko osoby","Data i miejsce urodzenia","Imiona i nazwiska rodziców","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy rodziców / osób potwierdzających","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej, np. przez rodzica. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("marriage_extract","Akt małżeństwa / odpis aktu małżeństwa", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwiska małżonków","Data i miejsce zawarcia małżeństwa","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy małżonków / rodziców / osób potwierdzających","Świadkowie","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("death_extract","Odpis aktu zgonu", listOf("Imiona i nazwisko osoby zmarłej","Data i miejsce zgonu","Urząd stanu cywilnego","Numer aktu / rok","Rodzaj odpisu","Wnioskodawca i podstawa uprawnienia","Cel uzyskania","Sposób odbioru"), "Formularz danych do wniosku do USC. Odpis wydaje kierownik USC, nie starosta."),
    OfficialDocType("company_financing_agreement","Umowa odpłatnego współfinansowania Celu", listOf("Nazwa Projektu / Celu","Nazwa i dane Finansującego","Forma prawna Finansującego","Nazwa i dane Dyspozytora / Realizatora","Podstawa ustanowienia Dyspozytora w Projekcie","Przedmiot i zakres odpłatnych świadczeń","Okres obowiązywania Umowy","Zakres działań w danym kwartale","Wynagrodzenie Dyspozytora – kwota i sposób kalkulacji","Koszty bezpośrednie – kwota i zakres","Koszty organizacyjne – kwota i sposób alokacji","Łączna wartość finansowania kwartalnego","Łączna wartość finansowania za cały okres","Uzasadnienie gospodarcze Finansującego","Grupa i mechanizm związku z działalnością Finansującego","Mierniki wykonania i rezultatu","Terminy raportowania i rozliczeń","Rachunek płatniczy do rozliczeń","Podstawa prawna rozliczeń podatkowych i składkowych","Zasady rozdzielenia środków prywatnych i publicznych","Zasady ochrony danych i poufności","Data zawarcia i miejsce","Osoba reprezentująca Finansującego","Podpis Finansującego","Podpis Dyspozytora / Realizatora"), "Prywatny projekt umowy odpłatnej. Finansujący płaci za rzeczywiście wykonywane i udokumentowane świadczenia Dyspozytora oraz uzgodnione koszty wykonania. Wpis w aplikacji jest roboczym dokumentem ewidencyjnym i nie stanowi sam w sobie podpisanej umowy, faktury ani gwarancji uznania wydatku za koszt podatkowy. Przed podpisaniem należy uzupełnić i dołączyć trzy załączniki: uzasadnienie gospodarcze Finansującego, plan działań i mierników oraz plan finansowania z kwotami."),
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
    OfficialDocType("org_delegate","Dokument delegata organizacji", listOf("Imię i nazwisko","Nazwa organizacji delegującej","Organizacja / wydarzenie / organ, do którego delegowano","Podstawa delegowania","Numer i data uchwały / aktu","Zakres reprezentacji i głosowania","Okres delegowania","Status"), "Potwierdza delegowanie do reprezentowania organizacji w określonej sprawie lub na określonym forum; nie nadaje mandatu publicznego."),
    OfficialDocType("common_rights_self_determination_declaration", "Oświadczenie o samostanowieniu i odpowiedzialności cywilnej", listOf("Osoba składająca oświadczenie","Forma występowania: osoba fizyczna / NGO / biskup tytularny / osoba pełniąca mandat nieparlamentarny / inna","Dane osoby działającej i podstawa reprezentacji — jeśli dotyczy","Opis własnych decyzji, celów i obszaru samostanowienia","Zakres spraw pozostawionych do samodzielnego rozstrzygnięcia","Dobrowolnie przyjęte zobowiązania i zasady odpowiedzialności","Sposób poszanowania praw, wolności i bezpieczeństwa osób trzecich","Zastrzeżenie: oświadczenie nie narusza praw osób trzecich ani powszechnie obowiązującego prawa","Adresat — jeśli dotyczy","Podstawa faktyczna i dokumenty źródłowe","Data, miejsce i sposób złożenia","Podpis / potwierdzenie autorstwa"), "Dokumentuje osobistą deklarację autonomii i odpowiedzialności w granicach prawa. Nie ustanawia suwerenności publicznej, nie znosi jurysdykcji, obowiązków ustawowych ani praw innych osób."),
    OfficialDocType("common_rights_self_limitation", "Deklaracja dobrowolnego samoograniczenia", listOf("Osoba składająca deklarację","Dane kontaktowe — opcjonalnie","Opis dobrowolnego ograniczenia własnych działań, zachowań lub uprawnień faktycznych","Cel samoograniczenia: bezpieczeństwo, porządek, dobro wspólne lub inny","Zakres, czas obowiązywania i terytorium","Warunki rozpoczęcia i zakończenia deklaracji","Sposób monitorowania realizacji i dokumentowania","Osoba wspierająca lub podmiot kontaktowy — za zgodą","Zasady zmiany lub odwołania deklaracji","Oświadczenie o dobrowolności, zrozumieniu treści i braku przymusu","Zastrzeżenie: deklaracja nie ogranicza praw osób trzecich i nie zastępuje orzeczenia, decyzji ani środka przewidzianego ustawą","Data, miejsce i podpis"), "Służy opisaniu dobrowolnych zobowiązań własnych. Nie może stanowić przymusowego środka wobec innych osób, zastępować procedury ustawowej ani prowadzić do samowolnego pozbawienia praw."),
    OfficialDocType("common_rights_third_party_safeguards", "Oświadczenie o poszanowaniu praw osób trzecich i bezpieczeństwa publicznego", listOf("Osoba / podmiot składający oświadczenie","Forma występowania i podstawa reprezentacji","Opis planowanego działania cywilnego","Ocena możliwego wpływu na osoby trzecie","Środki zapobiegające szkodzie, zagrożeniu i naruszeniu prywatności","Zasady dobrowolnego udziału i możliwości rezygnacji","Zasady dostępności i niedyskryminacji","Sposób reagowania na skargę lub zgłoszenie ryzyka","Osoba odpowiedzialna i kanał kontaktowy","Zakres danych przetwarzanych i podstawa ich przetwarzania","Data, miejsce i podpis"), "Dokument planowania odpowiedzialnego działania. Nie jest zezwoleniem na działalność ani certyfikatem bezpieczeństwa; obowiązki wynikające z przepisów pozostają aktualne."),
    OfficialDocType("common_rights_accessibility_needs", "Oświadczenie o szczególnych potrzebach i racjonalnych usprawnieniach", listOf("Osoba, której potrzeby dotyczą","Zakres potrzeb funkcjonalnych i komunikacyjnych ujawnianych dobrowolnie","Orzeczenie lub dokument źródłowy — rodzaj, numer i data, jeżeli osoba chce go wskazać","Stopień niepełnosprawności: znaczny / umiarkowany / lekki / brak danych / nie dotyczy","Szczególne potrzeby dotyczące poruszania się, komunikacji, orientacji, sensoryki, czasu lub wsparcia","Preferowane formy kontaktu i dostępności","Racjonalne usprawnienia wnioskowane w konkretnej sprawie","Zakres zgody na przekazanie informacji o potrzebach i odbiorcy","Termin / okres, którego dotyczy wniosek","Osoba kontaktowa — za zgodą","Data, miejsce i podpis"), "Formularz dobrowolnego zgłoszenia potrzeb w celu uzgodnienia dostępności. Należy ograniczyć dane o zdrowiu do niezbędnego zakresu i określić podstawę ich przetwarzania; samo oświadczenie nie przyznaje ustawowego stopnia niepełnosprawności."),
    OfficialDocType("common_rights_faith_conscience_declaration", "Oświadczenie woli, sumienia i motywacji religijnej", listOf("Osoba składająca oświadczenie","Forma występowania: osoba fizyczna / NGO / biskup tytularny / osoba pełniąca mandat nieparlamentarny / inna","Opis oświadczenia woli i jego zakres","Dobrowolnie wskazane przekonania, wyznawana wiara lub motywacja religijna","Wartości deklarowane: piękno, prawda, sprawiedliwość, pokój i dobro wspólne","Źródło odniesienia religijnego: Bóg / Słowo / tradycja lub wspólnota — opis dobrowolny","Cel cywilnego działania i dobre intencje deklarowane przez składającego","Zobowiązanie do poszanowania godności, praw, wolności i bezpieczeństwa osób trzecich","Zastrzeżenie co do braku zamiaru naruszania prawa lub wyrządzania szkody","Adresat i okoliczności złożenia — jeśli dotyczy","Data, miejsce i podpis"), "Oświadczenie dokumentuje osobiste motywy sumienia i wiary. Przekonania religijne są danymi szczególnej kategorii, dlatego ich ujawnienie jest dobrowolne i powinno być ograniczone do niezbędnego zakresu. Deklaracja religijna nie zastępuje wymaganej prawem formy oświadczenia."),
    OfficialDocType("common_rights_declaration_of_will", "Oświadczenie woli z klauzulą prawdziwości i odpowiedzialności", listOf("Imię i nazwisko składającego","Dane identyfikacyjne ograniczone do niezbędnych","Treść oświadczenia woli","Cel i zamierzony skutek prawny — jeżeli występuje","Adresat oświadczenia","Podstawa prawna i wymagana forma czynności — do weryfikacji","Czy oświadczenie jest składane we własnym imieniu czy przez pełnomocnika","Podstawa i zakres umocowania — jeśli dotyczy","Informacja, czy przepis szczególny wymaga pouczenia o odpowiedzialności karnej","Data, miejsce i podpis","Lista załączników / dokumentów źródłowych"), "Klauzula prawdziwości powinna odróżniać zwykłe oświadczenie prywatne od zeznania lub oświadczenia składanego w ustawowym trybie, w którym ustawa przewiduje odpowiedzialność karną. Nie wolno sugerować, że każde prywatne oświadczenie automatycznie podlega odpowiedzialności z art. 233 Kodeksu karnego. Przed użyciem należy zweryfikować przepis szczególny, pouczenie, cel i wymaganą formę."),
    OfficialDocType("common_rights_nonparliamentary_mandate", "Zaświadczenie ewidencyjne o deklarowanym mandacie nieparlamentarnym", listOf("Imię i nazwisko","Nazwa wspólnoty / organizacji / zgromadzenia, które wskazuje mandat","Rodzaj i opis mandatu nieparlamentarnego","Podmiot lub osoby dokonujące wyboru / powierzenia","Numer, data i treść uchwały / aktu źródłowego","Zakres reprezentacji i jej wyraźne ograniczenia","Okres obowiązywania / kadencja","Terytorium i krąg reprezentowanych osób — jeśli udokumentowany","Sposób odwołania / wygaśnięcia","Dokumenty potwierdzające wybór lub powierzenie","Data wydania i osoba prowadząca ewidencję"), "Dokument ewidencyjny dotyczący mandatu wewnętrznego, społecznego lub organizacyjnego. Nie jest mandatem posła na Sejm RP ani PE, nie nadaje immunitetu, kompetencji organu państwa ani uprawnień publicznych."),
    OfficialDocType("common_rights_civil_service_legitimacy", "Legitymacja uczestnika dobrowolnej służby cywilnej lub społecznej", listOf("Imię i nazwisko","Nazwa inicjatywy / wspólnoty / organizacji","Rodzaj dobrowolnej służby lub działalności","Rola / zadania uzgodnione z organizatorem","Podstawa członkostwa lub porozumienia","Zakres działania i ograniczenia","Osoba koordynująca","Okres uczestnictwa","Zasady bezpieczeństwa i ochrony danych","Kontakt do organizatora","Numer ewidencyjny i status legitymacji"), "Legitymacja prywatna identyfikuje uczestnika dobrowolnej inicjatywy; nie jest legitymacją funkcjonariusza ani dokumentem uprawniającym do stosowania przymusu, kontroli lub wydawania poleceń publicznych."),
    OfficialDocType("common_rights_integrated_declaration_of_will", "Zintegrowane oświadczenie woli i odpowiedzialności cywilnej", listOf(
        "Imię i nazwisko osoby składającej oświadczenie",
        "Forma działania: we własnym imieniu / jako przedstawiciel NGO / w funkcji religijnej lub kościelnej / w ramach mandatu społecznego lub nieparlamentarnego / inna",
        "Dane reprezentowanego podmiotu oraz podstawa i zakres umocowania — jeśli dotyczy",
        "Dane identyfikacyjne niezbędne do celu oświadczenia; bez nadmiarowych numerów i danych",
        "Adresat oświadczenia i cel jego złożenia",
        "Opis własnej woli, decyzji, zamiaru i obszaru samostanowienia",
        "Zakres spraw podejmowanych samodzielnie oraz spraw wymagających uzgodnienia z innymi osobami",
        "Dobrowolne zobowiązania i zasady osobistej odpowiedzialności",
        "Zakres samoograniczenia: zachowania lub działania, których osoba dobrowolnie zobowiązuje się nie podejmować",
        "Czas obowiązywania samoograniczenia oraz warunki zmiany, odwołania lub zakończenia",
        "Motywacja etyczna i religijna — pole dobrowolne",
        "Wskazane wartości: piękno, prawda, sprawiedliwość, pokój, dobro wspólne i poszanowanie godności",
        "Dobrowolnie wskazane źródło odniesienia religijnego: Bóg / Słowo / tradycja / wspólnota / inne",
        "Opis działania cywilnego, społecznego, rodzinnego, edukacyjnego, religijnego lub obywatelskiego, którego dotyczy oświadczenie",
        "Szczególne potrzeby i usprawnienia dostępnościowe — zakres dobrowolnego ujawnienia",
        "Stopień niepełnosprawności — jeśli osoba chce wskazać; dokument źródłowy, numer i data — opcjonalnie",
        "Preferowana forma komunikacji, organizacji spotkań, czasu, wsparcia lub kontaktu",
        "Deklarowany mandat społeczny / nieparlamentarny — rodzaj, podmiot powierzający, dokument źródłowy, data, zakres i ograniczenia; jeśli dotyczy",
        "Nazwa inicjatywy / NGO / wspólnoty / organizacji oraz rola osoby — jeśli dotyczy",
        "Zakres reprezentacji i wyraźne wyłączenie kompetencji, których nie potwierdza dokument źródłowy",
        "Zasady poszanowania praw, wolności, godności, prywatności i bezpieczeństwa osób trzecich",
        "Ocena ryzyka szkody oraz środki zapobiegawcze i zasady reagowania na zagrożenie",
        "Zasady dobrowolnego udziału innych osób, ich zgody i możliwości wycofania się",
        "Zasady pokojowego działania, powstrzymania się od przemocy, gróźb, nękania, przymusu i samowoli",
        "Zasady współpracy, konsultacji, mediacji i rozwiązywania sporów",
        "Treść zasadniczego oświadczenia woli — pełna treść składanej deklaracji",
        "Wskazane podstawy faktyczne, dokumenty źródłowe, załączniki i ich wykaz",
        "Podstawa prawna czynności i wymagana forma — do ustalenia dla konkretnego adresata i celu",
        "Czy oświadczenie jest prywatną deklaracją, oświadczeniem w ramach umowy, czy składane jest w trybie szczególnym przewidzianym ustawą",
        "Czy przepis szczególny przewiduje pouczenie o odpowiedzialności karnej; nazwa przepisu i treść wymaganego pouczenia — jeśli dotyczy",
        "Potwierdzenie, że osoba zapoznała się z treścią, rozumie ją i działa dobrowolnie, bez przymusu",
        "Potwierdzenie, że deklaracja nie ma na celu uchylenia się od obowiązków ustawowych ani naruszenia praw osób trzecich",
        "Zastrzeżenia, zakresy nieobjęte oświadczeniem i pola pozostawione niewypełnione",
        "Data, miejsce, sposób złożenia i okres aktualności",
        "Podpis osoby składającej oraz podpisy osób przyjmujących lub świadków — jeśli są potrzebne",
        "Potwierdzenie autorstwa / rodzaj podpisu / znacznik czasu — jeśli dostępny",
        "Status dokumentu: projekt / podpisany / przekazany adresatowi / odwołany / zastąpiony",
        "Uwagi i historia zmian"
    ), "Zintegrowany formularz prywatnej deklaracji woli, odpowiedzialności i dobrowolnych zobowiązań. Łączy pola dotyczące samostanowienia, samoograniczenia, motywacji etycznej i religijnej, potrzeb dostępnościowych, deklarowanego mandatu społecznego, odpowiedzialności oraz ochrony osób trzecich. Wszystkie dane o wierze, zdrowiu i niepełnosprawności są opcjonalne i należy je ograniczyć do niezbędnego zakresu. Dokument nie nadaje mandatu publicznego, immunitetu, urzędu, kompetencji władczych ani uprawnień do ograniczania praw innych osób. Nie uchyla powszechnie obowiązującego prawa ani obowiązków ustawowych. Zwykłe prywatne oświadczenie nie podlega automatycznie odpowiedzialności z art. 233 Kodeksu karnego; odpowiedzialność ta może wynikać wyłącznie z przesłanek przewidzianych ustawą, w szczególności z właściwego trybu i wymaganego pouczenia. Skutki prawne oświadczenia zależą od jego treści, adresata, celu i wymaganej formy czynności. Przed złożeniem jako czynności wywołującej skutki prawne należy zweryfikować właściwe przepisy i formę.");

    OfficialDocType("common_rights_personal_integrity_certificate", "Zaświadczenie o przyjętych zasadach osobistej odpowiedzialności", listOf("Imię i nazwisko / nazwa podmiotu","Zakres deklarowanych zasad i zobowiązań","Zasady pokojowego i zgodnego z prawem działania","Zasady powstrzymywania się od przemocy, gróźb, nękania i samowoli","Sposób zgłaszania konfliktu interesów i ryzyka szkody","Dobrowolne działania naprawcze / mediacyjne","Osoba lub podmiot potwierdzający przyjęcie deklaracji — jeśli dotyczy","Data i podpis"), "Zaświadcza jedynie o złożeniu lub zarejestrowaniu deklaracji, nie certyfikuje moralności, nie daje szczególnych praw ani nie zastępuje wymaganych zezwoleń."),
) + generalServiceRecordDocumentTypes + publicServiceDocumentTypes

internal fun officialGraphicDocumentTargets(): List<Pair<String, String>> =
    officialDocTypes.map { it.id to it.title }

internal fun officialGraphicDocumentFields(documentId: String): List<Pair<String, String>> =
    officialDocTypes.firstOrNull { it.id == documentId }?.fields.orEmpty()
        .mapIndexed { index, label -> "field_$index" to label }
