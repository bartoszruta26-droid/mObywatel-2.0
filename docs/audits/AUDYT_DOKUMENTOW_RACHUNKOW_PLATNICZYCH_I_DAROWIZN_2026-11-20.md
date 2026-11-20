SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0

# Audyt dokumentów „Rachunki płatnicze i darowizny” — 20 listopada 2026 r.

- Repozytorium: `bartoszruta26-droid/mObywatel-2.0`
- Gałąź: `feat/payment-account-donation-documents`
- Zakres: pięć odrębnych formularzy ewidencyjnych w katalogu dokumentów Android.
- Zasada: nie zmieniać istniejącego modułu „Rachunki”, rejestru transakcji walutowych/kruszcowych ani specjalistycznej ewidencji rachunków Rodu i podmiotów.

## 1. Katalog dokumentów i grupy

| Identyfikator | Dokument | Grupa |
|---|---|---|
| `payment_account_foundation` | Rachunek płatniczy i ewidencja darowizn — Fundacja | Służba fundacyjna |
| `payment_account_association` | Rachunek płatniczy i ewidencja darowizn — Stowarzyszenie | Służba stowarzyszeniowa |
| `payment_account_church` | Rachunek płatniczy i ewidencja ofiar oraz darowizn — Kościół | Służba kościelna |
| `payment_account_business` | Rachunek płatniczy i ewidencja darowizn — Przedsiębiorstwo | Służba gospodarcza |
| `payment_account_person` | Rachunek płatniczy i ewidencja darowizn — Osoba fizyczna | Służba osobista |

Każdy dokument ma stabilny identyfikator, własny zestaw pól formularza i trasę typu `DOCUMENT`. Wpisy nie są aliasami grup ani ekranami specjalistycznymi.

## 2. Zakres danych wspólnych

Dane rachunku: posiadacz, adres, bank/dostawca usług płatniczych, państwo, nazwa rachunku, IBAN, BIC/SWIFT, waluta ISO 4217, rodzaj i przeznaczenie rachunku, status, daty otwarcia/zamknięcia, weryfikacja posiadacza, dokument źródłowy, osoby uprawnione i podstawa reprezentacji.

Dane darowizny/wpłaty: podstawa przyjmowania, rodzaj wpływu, cel, kampania/projekt, data, kwota, waluta, sposób przekazania, tytuł przelewu, identyfikator transakcji, dane darczyńcy (opcjonalne), anonimowość, ograniczenia, pokwitowanie, osoba przyjmująca, data wpłaty gotówki na rachunek, status rozliczenia, odniesienie księgowe i dokument potwierdzający.

## 3. Wspólny mechanizm ewidencji kasowej

Wszystkie pięć formularzy otrzymuje ten sam zestaw pól kasowych. Wspólny jest model danych, natomiast wartości są przechowywane w przestrzeni pól konkretnego identyfikatora dokumentu (doc_<documentId>_<fieldKey>). Oznacza to odrębne rejestry dla fundacji, stowarzyszenia, Kościoła, przedsiębiorstwa i osoby fizycznej — bez współdzielenia wpisów ani salda między podmiotami.

Ewidencja rozróżnia:
- **Przyjęcie gotówki** — zdarzenie KP, data i godzina, kwota i waluta, wpłacający (opcjonalnie / anonimowo), osoba przyjmująca, umocowanie oraz numer pokwitowania.
- **Gotówkę pozostającą w kasie** — stan przed i po zdarzeniu, identyfikator kasy, numer i okres raportu kasowego oraz uzgodnienie salda.
- **Przekazanie gotówki** — osoba przekazująca i przyjmująca, data, kwota i dowód przekazania.
- **Wpłatę gotówki do banku** — osobna data, kwota, rachunek docelowy, numer i dowód potwierdzenia bankowego. Samo KP ani przekazanie gotówki nie oznacza wpłaty bankowej.
- **Księgowanie** — odrębna data, numer zapisu i status księgowania. Przyjęcie gotówki nie oznacza automatycznego zaksięgowania darowizny.
- **Kontrolę kasową** — status uzgodnienia, różnica i wyjaśnienie.

Pola formularza nie tworzą integracji bankowej, automatycznego księgowania ani automatycznie wyliczanego salda. Wpisy są ewidencyjne i należą do konkretnego dokumentu. Nie należy prowadzić instytucjonalnego raportu kasowego, jeżeli dany podmiot nie prowadzi kasy w takim trybie; formularz osoby fizycznej może służyć uproszczonej ewidencji otrzymanej i przekazanej gotówki.

## 4. Pola zależne od rodzaju podmiotu

- Fundacja: KRS, NIP, REGON, cele statutowe, reprezentacja, status OPP/ewentualne uprawnienie do otrzymywania 1,5% PIT, powiązanie darowizny z celem statutowym i uchwała Zarządu.
- Stowarzyszenie: rejestr, statut, organy, składki członkowskie odrębne od darowizn, uchwała/upoważnienie i zgodność celu ze statutem.
- Kościół: status prawny, rejestr/ewidencja, jurysdykcja, typ jednostki, rodzaj ofiary, cel kultu/misyjny/charytatywny, organ finansowy i rozliczenie.
- Przedsiębiorstwo: forma prawna, KRS/CEIDG, NIP, REGON, tytuł prawny przyjęcia darowizny, kwalifikacja podatkowa i jawne rozdzielenie darowizn od zapłat za sprzedaż/usługi.
- Osoba fizyczna: imię i nazwisko, adres, PESEL jako pole opcjonalne, rezydencja podatkowa, relacja stron i grupa podatkowa — jeżeli istotne.

## 5. Ochrona danych i ograniczenia prawne

Formularze są ewidencyjne. Sam zapis nie potwierdza prowadzenia rachunku, prawa do dysponowania środkami, statusu OPP, statusu prawnego jednostki ani skutków podatkowych darowizny. Nie wolno utożsamiać wpłat handlowych z darowiznami ani składek członkowskich z darowiznami. Dane darczyńcy są opcjonalne i powinny być zapisywane tylko w niezbędnym zakresie i przy właściwej podstawie. PESEL osoby fizycznej jest opcjonalny. Nie dodano pól haseł, PIN-ów, kodów autoryzacyjnych ani danych logowania do banku.

## 6. Kontrole regresji wymagane przed scaleniem

- unikalność pięciu identyfikatorów i pól;
- obecność wszystkich pięciu dokumentów w katalogu;
- przypisanie do istniejących grup, bez tworzenia nowych grup;
- trasa każdego dokumentu typu `DOCUMENT`;
- obecność wspólnych pól rachunku, darowizny i ewidencji kasowej;
- odrębność przestrzeni pól kasowych dla pięciu identyfikatorów;
- rozdzielenie KP, salda kasy, przekazania gotówki, wpłaty bankowej i księgowania;
- zachowanie istniejących tras `bank_accounts`, `rod_bank_accounts`, `entity_bank_accounts`;
- zachowanie modułu „Rachunki” i rejestru transakcji finansowych;
- testy JVM oraz Compose UI na emulatorze.

**Status:** rozszerzono wspólny schemat formularzy o ewidencję kasową i testy jej rozdzielenia między pięć dokumentów. Nadal wymagane są pozytywne testy JVM, Compose UI na emulatorze oraz przegląd całego PR przed scaleniem do `main`.
