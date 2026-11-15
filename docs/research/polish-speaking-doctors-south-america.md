<!-- SPDX-FileCopyrightText: 2026 bartoszruta26-droid -->
<!-- SPDX-License-Identifier: CC0-1.0 -->

# Kwerenda lekarzy w miastach polonijnych Ameryki Południowej

**Data kwerendy:** 2026-11-15  
**Gałąź:** `feature/medical-certificate-management-translation`  
**Cel:** identyfikacja lekarzy spełniających kryterium katalogu: jawna deklaracja języka polskiego albo polsko brzmiące nazwisko / potwierdzone polskie pochodzenie, wraz z publicznie potwierdzoną praktyką. Samo nazwisko nie jest dowodem znajomości języka.

## Wynik

W dostępnych publicznych źródłach nie udało się potwierdzić kilku lekarzy praktykujących w Ameryce Południowej, którzy **jawnie deklarują znajomość języka polskiego**. Nie należy utożsamiać polskiego pochodzenia nazwiska, polskich korzeni ani działalności w mieście polonijnym ze znajomością języka polskiego.

Żaden z trzech profili nie deklaruje języka polskiego. Zgodnie z rozszerzonym kryterium katalogu osoby z polsko brzmiącym nazwiskiem mogą jednak figurować w katalogu informacyjnym, jeżeli praktyka jest potwierdzona publicznym źródłem. Wpis nie oznacza rekomendacji, potwierdzenia polskojęzyczności ani uprawnienia do wystawiania lub podpisywania dokumentów.

## Publiczne tropy — język polski niepotwierdzony

### 1. Dr Gustavo Polacow Korn — São Paulo, Brazylia
- **Dziedzina:** otorynolaryngologia.
- **Identyfikator zawodowy podany w katalogu:** CRM-SP 101458.
- **Praktyka:** Consultório Médico Dr. Gustavo Polacow Korn.
- **Adres praktyki:** Av. Brig. Faria Lima, 1811, cj. 907 e 908, Jardim Paulistano, São Paulo/SP, Brazylia.
- **Informacje o wykształceniu podane w profilu:** doktorat i magisterium w Universidade Federal de São Paulo / Escola Paulista de Medicina; rezydentura z otorynolaryngologii.
- **Język polski:** niepotwierdzony; profil nie deklaruje polskiego.
- **Źródło:** https://medbook.com.br/gustavokorn
- **Status:** dodany do katalogu informacyjnego na podstawie nazwiska o polskim brzmieniu i publicznie potwierdzonej praktyki; język polski niepotwierdzony; aktywny status CRM wymaga sprawdzenia w CFM/CREMESP.

### 2. Dr. Sergio Czerwonko — Buenos Aires, Argentyna
- **Dziedzina:** geriatria; profil katalogowy wymienia również psychiatrię.
- **Praktyka:** Consultorio privado.
- **Adres praktyki:** Blanco Encalada 1451, Belgrano, C1428, Ciudad Autónoma de Buenos Aires, Argentyna.
- **Telefon opublikowany w katalogu:** +54 11 4781-2958.
- **Języki wymienione w profilu:** hiszpański, angielski, portugalski, francuski i hebrajski.
- **Język polski:** niepotwierdzony; nie ma go na opublikowanej liście języków.
- **Źródło:** https://www.medicoargentina.com/dr/sergio-czerwonko-11040660
- **Status:** dodany do katalogu informacyjnego na podstawie nazwiska o polskim brzmieniu i publicznie potwierdzonej praktyki; język polski niepotwierdzony; matrícula i aktualny status wymagają sprawdzenia w REFEPS.

### 3. Dr. Pietra Stanicki, MD — Buenos Aires, Argentyna
- **Dziedzina:** podstawowa opieka zdrowotna (Primary Care).
- **Model świadczeń:** wizyty osobiste i konsultacje wideo; profil opisuje usługę concierge CARE THERE, oferującą konsultacje domowe i zdalne dla ekspatów oraz podróżnych w Brazylii i Argentynie.
- **Lokalizacja podana w profilu:** Buenos Aires, Argentyna.
- **Języki wymienione w profilu:** angielski, portugalski i hiszpański.
- **Język polski:** niepotwierdzony; nie ma go na opublikowanej liście języków.
- **Źródło:** https://stanicki.doctorsa.com/
- **Status:** dodana do katalogu informacyjnego na podstawie nazwiska o polskim brzmieniu i publicznie potwierdzonej praktyki; język polski niepotwierdzony; indywidualny numer licencji wymaga potwierdzenia w REFEPS.

## Potwierdzenie ośrodków polonijnych

- **Buenos Aires:** portal gov.pl wymienia m.in. Unión de los Polacos en la República Argentina / Casa Polaca, polską bibliotekę, szkołę języka i kultury polskiej oraz inne organizacje polonijne. Źródło: https://www.gov.pl/web/argentyna/relacje-dwustronne
- **São Paulo:** miasto ma polsko-brazylijską społeczność, jednak samo istnienie tej społeczności nie potwierdza polskojęzyczności konkretnego lekarza.

## Wymagania utrzymania katalogu

1. Zachować źródło publiczne potwierdzające praktykę, adres i kanał kontaktu.
2. Rozróżniać jawne deklaracje językowe od kryterium nazwiskowego; nie opisywać osób jako polskojęzycznych bez potwierdzenia.
3. Zapisywać status licencji oddzielnie od potwierdzenia istnienia praktyki.
4. Zapisywać zgodę na publikację wraz ze źródłem i datą. W aktualnych rekordach zgoda ustna jest oznaczona jako zgłoszona przez administratora.
5. Prawo do wystawienia konkretnego dokumentu i prawo do jego podpisania wymagają osobnej weryfikacji w odpowiedniej jurysdykcji.

## Rekomendacja implementacyjna

Do czasu potwierdzenia kolejnych osób aplikacja powinna obsługiwać pusty katalog i dodawanie lekarzy przez upoważnionego administratora. Można zachować powyższe rekordy wyłącznie w dokumentacji kwerendy, poza produkcyjną listą wyboru lekarza. Po uzyskaniu zgody i weryfikacji danych można dodać je do katalogu informacyjnego ze źródłem, datą weryfikacji i statusem języka.



## Aktualizacja implementacyjna — 2026-11-15

Trzech lekarzy dodano do lokalnego katalogu informacyjnego w aplikacji. W formularzu zaświadczenia można wybrać lekarza; zapis obejmuje identyfikator i podstawowe dane praktyki. Katalog obsługuje dodawanie, edycję i usuwanie. Wszyscy pozostają wyłączeni z funkcji podpisującego. Status licencji dla dwóch profili argentyńskich oraz aktualny status CRM w Brazylii wymagają sprawdzenia w oficjalnych rejestrach.

Oficjalny rejestr argentyński: https://www.argentina.gob.ar/salud/buscador-nacional-de-profesionales-de-la-salud  
Opis źródła CNES/DATASUS dla profilu Gustavo: https://bula.com.br/medico/gustavo-polacow-korn-crm-sp-101458
