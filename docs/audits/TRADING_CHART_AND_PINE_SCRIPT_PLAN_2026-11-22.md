<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Wykres analityczny i przygotowanie do Pine Script — 2026-11-22

## Stan wyjściowy
`main` zawiera repozytorium historycznych kursów NBP z cache'em lokalnym, wykres liniowy kursu średniego waluty i ceny złota, zakresy 7 dni–5 lat oraz osobny wykres dziennych przepływów z importowanych wyciągów. Dane kursowe istnieją jako pojedyncze punkty dzienne.

## Zakres docelowy
Wykres wzorowany funkcjonalnie na TradingView powinien oferować linię, obszar, świece, zakresy czasu, inspekcję punktu, osie i znaczniki, wskaźniki SMA/EMA/RSI, a następnie edytor strategii. Dane NBP są wyłącznie referencyjne: kurs średni i cena złota nie są giełdowymi OHLC ani wolumenem. Wartość dzienna może być reprezentowana jako O=H=L=C, bez sztucznej zmienności lub wolumenu.

## Integracja
Rozszerzyć istniejący `PriceHistoryChartCard`, zachowując `NbpRatesRepository`, cache i metadane źródła. Dla walut używać tabel A/B zgodnie z dostępnością, dla złota `goldPriceHistory`. Nie zmieniać wykresu przepływów bankowych ani semantyki wyceny importów.

## Pine Script
Kotlin/Compose nie wykonuje natywnie Pine Script. Potrzebne są: parser do AST, jawny podzbiór składni, silnik szeregów czasowych, izolowany runtime z limitami CPU/pamięci, edytor, walidator i testy zgodności. Nie deklarować pełnej zgodności, dopóki nie jest zweryfikowana semantyka `series`, `ta.*`, `request.*`, `strategy.*`, rysowanie i zarządzanie stanem.

## Kryteria akceptacji
1. Brak sztucznego OHLCV dla źródeł referencyjnych.
2. Dokładne osie, etykiety, wybór punktu i zakresy czasu.
3. Testy obliczeń i UI na emulatorze.
4. Pine Script jawnie oznaczony jako nieobsługiwany poza zaimplementowanym podzbiorem.
5. CI dla końcowego SHA przed scaleniem i APK.
