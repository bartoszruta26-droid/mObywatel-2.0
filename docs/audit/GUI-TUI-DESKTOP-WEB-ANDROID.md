<!--
SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
SPDX-License-Identifier: CC0-1.0
-->

# Audyt Android GUI — dostępność, nawigacja i grafiki

**Repozytorium:** `bartoszruta26-droid/mObywatel-2.0`  
**Gałąź docelowa:** `main`  
**Zakres:** wyłącznie prototyp Android Jetpack Compose w `obywatel-android/`. TUI, Desktop GUI i Web UI pozostają poza zakresem tej pracy.

## 1. Stan prototypu

Aplikacja zawiera ekran główny, lokalny portfel dokumentów oraz podgląd PDF, DOCX i obrazów. Interfejs jest jawnie oznaczony jako prototyp i nie komunikuje się z usługami administracji publicznej. Import plików korzysta z Android Storage Access Framework.

## 2. Nawigacja i dostępność

- Kafelek „Dokumenty” jest jedyną aktywną usługą demonstracyjną; pozostałe kafle są rzeczywiście wyłączone, a nie pozornie klikalne.
- Karty usług przekazują czytnikom ekranu stan: „Dostępna” albo „W przygotowaniu — funkcja niedostępna”.
- Ekrany mają nazwy paneli, nagłówki i opisy przycisków powrotu, importu, usuwania oraz sterowania stronami podglądu.
- Komunikaty błędów listy i podglądu są ogłaszane przez semantykę Live Region.
- Przyciski nawigacji stron są wyłączane na granicach dokumentu; sterowanie powiększeniem ma etykiety dostępności i przycisk przywracania widoku.

**Weryfikacja automatyczna:** testy Compose sprawdzają nawigację do dokumentów, nieaktywne kafle oraz stan dostępności usług. Testy ekranu dokumentów sprawdzają etykiety przycisków i powrót do ekranu głównego. Automatyczne testy nie zastępują ręcznej kontroli TalkBack, rozmiaru czcionki i kontrastu na urządzeniu.

## 3. Grafiki

W aplikacji wykorzystywane są autorskie ilustracje wektorowe Android Vector Drawable:
- `app/src/main/res/drawable/civic_service_illustration.xml` — ilustracja nagłówka ekranu głównego;
- `app/src/main/res/drawable/empty_documents_illustration.xml` — ilustracja pustego stanu portfela dokumentów.

Są to neutralne ilustracje interfejsowe, a nie oficjalne znaki, dokumenty, hologramy ani zabezpieczenia mObywatela. Grafiki pełnią funkcję dekoracyjną i mają pusty opis alternatywny, ponieważ ich treść jest powtórzona tekstem.

## 4. Ograniczenia i dalsze prace

1. Ręczne testy TalkBack i przełączania fokusu na rzeczywistym urządzeniu.
2. Testy dużej czcionki, powiększenia ekranu, kontrastu i trybu ciemnego.
3. Testy dostępności importu, usuwania, błędów dostawcy SAF oraz podglądu dokumentów.
4. Testy na małych ekranach, tabletach i orientacji poziomej.
5. Dalsza weryfikacja gestów powiększania i przesuwania podglądu.

## 5. Granice zgodności

Projekt jest lokalnym prototypem. Nie należy utożsamiać jego grafik, etykiet ani widoków dokumentów z oficjalnym interfejsem, autentycznością dokumentu lub usługą państwową. Oficjalne informacje o aplikacji publikuje Ministerstwo Cyfryzacji: https://www.gov.pl/web/mobywatel oraz https://www.gov.pl/web/cyfryzacja/informacje-o-aplikacji-mobywatel.
