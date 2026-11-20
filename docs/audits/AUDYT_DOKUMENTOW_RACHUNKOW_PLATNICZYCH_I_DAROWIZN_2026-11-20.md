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

## 3. Pola zależne od rodzaju podmiotu

- Fundacja: KRS, NIP, REGON, cele statutowe, reprezentacja, status OPP/ewentualne uprawnienie do otrzymywania 1,5% PIT, powiązanie darowizny z celem statutowym i uchwała Zarządu.
- Stowarzyszenie: rejestr, statut, organy, składki członkowskie odrębne od darowizn, uchwała/upoważnienie i zgodność celu ze statutem.
- Kościół: status prawny, rejestr/ewidencja, jurysdykcja, typ jednostki, rodzaj ofiary, cel kultu/misyjny/charytatywny, organ finansowy i rozliczenie.
- Przedsiębiorstwo: forma prawna, KRS/CEIDG, NIP, REGON, tytuł prawny przyjęcia darowizny, kwalifikacja podatkowa i jawne rozdzielenie darowizn od zapłat za sprzedaż/usługi.
- Osoba fizyczna: imię i nazwisko, adres, PESEL jako pole opcjonalne, rezydencja podatkowa, relacja stron i grupa podatkowa — jeżeli istotne.

## 4. Ochrona danych i ograniczenia prawne

Formularze są ewidencyjne. Sam zapis nie potwierdza prowadzenia rachunku, prawa do dysponowania środkami, statusu OPP, statusu prawnego jednostki ani skutków podatkowych darowizny. Nie wolno utożsamiać wpłat handlowych z darowiznami ani składek członkowskich z darowiznami. Dane darczyńcy są opcjonalne i powinny być zapisywane tylko w niezbędnym zakresie i przy właściwej podstawie. PESEL osoby fizycznej jest opcjonalny. Nie dodano pól haseł, PIN-ów, kodów autoryzacyjnych ani danych logowania do banku.

## 5. Kontrole regresji wymagane przed scaleniem

- unikalność pięciu identyfikatorów i pól;
- obecność wszystkich pięciu dokumentów w katalogu;
- przypisanie do istniejących grup, bez tworzenia nowych grup;
- trasa każdego dokumentu typu `DOCUMENT`;
- obecność wspólnych pól rachunku i darowizny;
- zachowanie istniejących tras `bank_accounts`, `rod_bank_accounts`, `entity_bank_accounts`;
- zachowanie modułu „Rachunki” i rejestru transakcji finansowych;
- testy JVM oraz Compose UI na emulatorze.

**Status:** implementacja formularzy i testów w toku; nie scalać do `main` przed pozytywną walidacją CI i testami emulatora.
