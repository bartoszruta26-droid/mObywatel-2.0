# Kwerenda lekarzy w miastach polonijnych Ameryki Południowej

**Data kwerendy:** 2026-11-15  
**Gałąź:** `feature/medical-certificate-management-translation`  
**Cel:** ustalenie, czy można bezpiecznie dodać do katalogu aplikacji kilku rzeczywistych lekarzy, którzy przyjmują w miastach z aktywną społecznością polonijną i potwierdzają obsługę pacjentów w języku polskim.

## Wynik

W dostępnych publicznych źródłach nie udało się potwierdzić kilku lekarzy praktykujących w Ameryce Południowej, którzy **jawnie deklarują znajomość języka polskiego**. Nie należy utożsamiać polskiego pochodzenia nazwiska, polskich korzeni ani działalności w mieście polonijnym ze znajomością języka polskiego.

Dlatego poniższe rekordy są wyłącznie **tropami do dalszej weryfikacji**. Nie są wpisami katalogu lekarzy polskojęzycznych, nie mogą być prezentowane jako rekomendowani lekarze ani jako osoby uprawnione do wystawiania lub podpisywania dokumentów w aplikacji.

## Publiczne tropy — język polski niepotwierdzony

### 1. Dr Gustavo Polacow Korn — São Paulo, Brazylia
- **Dziedzina:** otorynolaryngologia.
- **Identyfikator zawodowy podany w katalogu:** CRM-SP 101458.
- **Praktyka:** Consultório Médico Dr. Gustavo Polacow Korn.
- **Adres praktyki:** Av. Brig. Faria Lima, 1811, cj. 907 e 908, Jardim Paulistano, São Paulo/SP, Brazylia.
- **Informacje o wykształceniu podane w profilu:** doktorat i magisterium w Universidade Federal de São Paulo / Escola Paulista de Medicina; rezydentura z otorynolaryngologii.
- **Język polski:** niepotwierdzony; profil nie deklaruje polskiego.
- **Źródło:** https://medbook.com.br/gustavokorn
- **Status:** kandydat do weryfikacji, nie dodawać do listy polskojęzycznych.

### 2. Dr. Sergio Czerwonko — Buenos Aires, Argentyna
- **Dziedzina:** geriatria; profil katalogowy wymienia również psychiatrię.
- **Praktyka:** Consultorio privado.
- **Adres praktyki:** Blanco Encalada 1451, Belgrano, C1428, Ciudad Autónoma de Buenos Aires, Argentyna.
- **Telefon opublikowany w katalogu:** +54 11 4781-2958.
- **Języki wymienione w profilu:** hiszpański, angielski, portugalski, francuski i hebrajski.
- **Język polski:** niepotwierdzony; nie ma go na opublikowanej liście języków.
- **Źródło:** https://www.medicoargentina.com/dr/sergio-czerwonko-11040660
- **Status:** trop do weryfikacji, nie dodawać do listy polskojęzycznych.

### 3. Dr. Pietra Stanicki, MD — Buenos Aires, Argentyna
- **Dziedzina:** podstawowa opieka zdrowotna (Primary Care).
- **Model świadczeń:** wizyty osobiste i konsultacje wideo; profil opisuje usługę concierge CARE THERE, oferującą konsultacje domowe i zdalne dla ekspatów oraz podróżnych w Brazylii i Argentynie.
- **Lokalizacja podana w profilu:** Buenos Aires, Argentyna.
- **Języki wymienione w profilu:** angielski, portugalski i hiszpański.
- **Język polski:** niepotwierdzony; nie ma go na opublikowanej liście języków.
- **Źródło:** https://stanicki.doctorsa.com/
- **Status:** trop do weryfikacji, nie dodawać do listy polskojęzycznych.

## Potwierdzenie ośrodków polonijnych

- **Buenos Aires:** portal gov.pl wymienia m.in. Unión de los Polacos en la República Argentina / Casa Polaca, polską bibliotekę, szkołę języka i kultury polskiej oraz inne organizacje polonijne. Źródło: https://www.gov.pl/web/argentyna/relacje-dwustronne
- **São Paulo:** miasto ma polsko-brazylijską społeczność, jednak samo istnienie tej społeczności nie potwierdza polskojęzyczności konkretnego lekarza.

## Wymagania przed dodaniem lekarza do katalogu aplikacji

Dla każdego kandydata wymagane są:
1. publiczne źródło potwierdzające, że dana osoba jest lekarzem i ma aktualne uprawnienia w kraju praktyki;
2. publiczne źródło praktyki lub placówki potwierdzające adres i kanał rejestracji;
3. jawna deklaracja języka polskiego w profilu zawodowym albo pisemne potwierdzenie samego lekarza;
4. potwierdzenie, że lekarz zgadza się na umieszczenie danych w katalogu aplikacji;
5. osobna weryfikacja prawa do wystawiania danego rodzaju dokumentu w odpowiedniej jurysdykcji.

**Nie wolno** ustawiać lekarza jako domyślnego wystawcy ani sugerować jego zgody na podpis. Rekord w katalogu informacyjnym i autoryzacja do podpisywania dokumentów muszą pozostać odrębnymi funkcjami.

## Rekomendacja implementacyjna

Do czasu potwierdzenia kolejnych osób aplikacja powinna obsługiwać pusty katalog i dodawanie lekarzy przez upoważnionego administratora. Można zachować powyższe rekordy wyłącznie w dokumentacji kwerendy, poza produkcyjną listą wyboru lekarza. Po uzyskaniu zgody i weryfikacji danych można dodać je do katalogu informacyjnego ze źródłem, datą weryfikacji i statusem języka.

