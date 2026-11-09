<!-- SPDX-FileCopyrightText: 2026 bartoszruta26-droid -->
<!-- SPDX-License-Identifier: CC0-1.0 -->

# Kody QR w aplikacji rządowej mObywatel

## Do czego służy funkcja „Kod QR”

W oficjalnej aplikacji mObywatel kod QR jest elementem procesu **kryptograficznego potwierdzania danych dokumentu cyfrowego**. Nie jest zwykłą wizytówką ani samym obrazkiem z danymi zapisanymi w kodzie.

W typowym scenariuszu uczestniczą dwie osoby i dwa urządzenia:

1. Osoba weryfikująca rozpoczyna sprawdzanie dokumentu i wyświetla własny kod QR w aplikacji mObywatel albo w oficjalnym weryfikatorze.
2. Osoba okazująca dokument otwiera właściwy dokument w swojej aplikacji i wybiera funkcję potwierdzenia danych.
3. Osoba okazująca skanuje kod QR osoby weryfikującej. W przewidzianych scenariuszach można też przepisać sześciocyfrowy kod prezentowany pod kodem QR.
4. Aplikacja pokazuje, komu i jaki zakres danych ma zostać przekazany.
5. Właściciel dokumentu świadomie zatwierdza udostępnienie danych.
6. Dane pojawiają się na urządzeniu weryfikującym na ograniczony czas, a następnie znikają zgodnie z opisanym przez administrację mechanizmem weryfikacji.

Ważne: w oficjalnym przepływie kod wyświetlany przez osobę weryfikującą inicjuje sesję sprawdzania. Osoba okazująca dokument skanuje ten kod i zatwierdza przekazanie danych. To nie jest mechanizm polegający na zeskanowaniu statycznego kodu z fotografii dokumentu.

## Główne zastosowania

- **Potwierdzanie danych mDowodu i innych obsługiwanych dokumentów cyfrowych** – weryfikacja kryptograficzna zamiast polegania wyłącznie na wyglądzie ekranu.
- **Sprawdzenie dokumentu przez drugą osobę** – weryfikujący generuje kod QR, a właściciel dokumentu zatwierdza zakres danych, które zostaną udostępnione.
- **Weryfikacja z użyciem oficjalnego weryfikatora internetowego** – w określonych scenariuszach osoba weryfikująca może użyć serwisu [Sprawdź mDowód](https://weryfikator.mobywatel.gov.pl/).
- **Skróty dostępne bezpośrednio na kartach dokumentów** – w oficjalnej aplikacji skrót potwierdzania danych może być dostępny z poziomu dokumentu.

## Czym ten mechanizm różni się od zwykłego kodu QR

Kod QR sam w sobie nie dowodzi autentyczności dokumentu. O autentyczności w oficjalnym procesie decyduje cały mechanizm aplikacji, komunikacja z właściwą usługą, kontrola zakresu danych i potwierdzenie przez właściciela dokumentu.

Zwykły kod QR może zawierać dowolny tekst, adres strony internetowej, dane kontaktowe vCard, dane sieci Wi-Fi lub inne informacje. Po zeskanowaniu taki kod jedynie przekazuje zapisany w nim tekst albo uruchamia obsługiwaną akcję. Nie sprawdza danych w rejestrze państwowym i nie potwierdza tożsamości.

## Generator vCard w obywatelskim prototypie

W niezależnym prototypie obywatelskim funkcja generatora tworzy **zwykły kod QR z wizytówką vCard 3.0**. Może on zawierać nazwę kontaktu, imię i nazwisko, organizację, stanowisko, numer telefonu, adres e-mail, stronę internetową i adres.

Przepływ użycia:

1. Wybierz „Kod QR” w dolnym menu.
2. Wskaż plik kontaktu `.vcf` albo uzupełnij pola ręcznie.
3. Sprawdź, czy zakres danych jest właściwy.
4. Odczytaj wygenerowany kod QR innym urządzeniem.

Kod kontaktowy jest przeznaczony do przekazania danych kontaktowych. Nie jest połączony z usługami Ministerstwa Cyfryzacji, rejestrami państwowymi ani oficjalnym weryfikatorem mObywatela. Nie może być używany do potwierdzania dokumentów, tożsamości, wieku ani uprawnień.

### Ochrona danych

Każdy, kto zeskanuje kod vCard, może odczytać zapisane w nim informacje. Należy umieszczać w nim wyłącznie dane, które użytkownik świadomie chce udostępnić. Nie należy dodawać numeru PESEL, numerów dokumentów, danych logowania ani innych informacji poufnych.

## Źródła oficjalne

1. Ministerstwo Cyfryzacji / Centralny Ośrodek Informatyki, *Jak potwierdzać dokumenty w mObywatelu 2.0* – instrukcja procesu QR i kodu sześciocyfrowego: [PDF](https://info.mobywatel.gov.pl/assets/download/Jak%20potwierdza%C4%87%20dokumenty%20w%20mObywatelu%202.0.pdf).
2. Ministerstwo Cyfryzacji, *Kod QR – skuteczna metoda weryfikacji mDowodu*: [gov.pl](https://www.gov.pl/web/cyfryzacja/kod-qr--skuteczna-metoda-weryfikacji-mdowodu).
3. Ministerstwo Cyfryzacji, *Skróty na dokumentach – więcej wygody w mObywatelu*: [gov.pl](https://www.gov.pl/web/cyfryzacja/skroty-na-dokumentach--wiecej-wygody-w-mobywatelu).
4. Oficjalny serwis weryfikacyjny: [Sprawdź mDowód](https://weryfikator.mobywatel.gov.pl/).

---

**Status opisu:** dokument wyjaśnia oficjalny mechanizm na podstawie materiałów administracji publicznej. Opis generatora vCard dotyczy wyłącznie niezależnego prototypu obywatelskiego.
