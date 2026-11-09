<!--
SPDX-FileCopyrightText: 2026 bartoszruta26-droid
SPDX-License-Identifier: CC0-1.0
-->

# Android UI prototype — visual refresh

## Zakres

Niezależny prototyp interfejsu Android w Kotlinie i Jetpack Compose. Odświeżenie obejmuje:
- hierarchię nagłówka i markę demonstracyjną,
- kafle usług z piktogramami Material,
- komunikat jawnie określający status prototypu,
- aktywny stan dolnej nawigacji,
- spójną paletę i promienie kart,
- układ elastyczny oparty na siatce.

## Zasady znaków i wiarygodności

Znak aplikacji w tym prototypie jest autorską ikoną demonstracyjną. Nie jest oficjalnym logo mObywatela. Prototyp nie używa godła RP jako znaku własnej aplikacji, nie odtwarza wzorów dokumentów ani zabezpieczeń i nie sugeruje, że prezentowane dane są urzędowo potwierdzone. Oficjalne godło może być używane wyłącznie w kontekście i na zasadach przewidzianych przepisami. Przed wykorzystaniem jakichkolwiek plików publicznego design systemu należy sprawdzić ich pochodzenie, licencję i warunki użycia.

## Weryfikacja

- Uruchom `./gradlew :app:assembleDebug` w katalogu `obywatel-android`.
- Uruchom testy: `./gradlew :app:testDebugUnitTest`.
- Sprawdź TalkBack, skalowanie czcionek, kontrast oraz zachowanie na małych ekranach.
