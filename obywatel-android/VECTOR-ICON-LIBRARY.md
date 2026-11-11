# Biblioteka ikon wektorowych XML

## Zasoby

Biblioteka zawiera 55 monochromatycznych ikon Android Vector Drawable w katalogu `app/src/main/res/drawable/`. Nazwy mają prefiks `lib_ic_`, aby odróżniać je od ikon używanych bezpośrednio przez interfejs. Zasoby są biblioteką do ponownego wykorzystania, również w edytorze dokumentów; dodanie pliku nie oznacza automatycznego podłączenia go do ekranu.

- `lib_ic_account.xml`
- `lib_ic_home.xml`
- `lib_ic_settings.xml`
- `lib_ic_search.xml`
- `lib_ic_bell.xml`
- `lib_ic_calendar.xml`
- `lib_ic_clock.xml`
- `lib_ic_mail.xml`
- `lib_ic_phone.xml`
- `lib_ic_location.xml`
- `lib_ic_map.xml`
- `lib_ic_document.xml`
- `lib_ic_folder.xml`
- `lib_ic_download.xml`
- `lib_ic_upload.xml`
- `lib_ic_share.xml`
- `lib_ic_edit.xml`
- `lib_ic_delete.xml`
- `lib_ic_print.xml`
- `lib_ic_lock.xml`
- `lib_ic_unlock.xml`
- `lib_ic_shield.xml`
- `lib_ic_check.xml`
- `lib_ic_close.xml`
- `lib_ic_plus.xml`
- `lib_ic_minus.xml`
- `lib_ic_more.xml`
- `lib_ic_help.xml`
- `lib_ic_info.xml`
- `lib_ic_warning.xml`
- `lib_ic_star.xml`
- `lib_ic_heart.xml`
- `lib_ic_visibility.xml`
- `lib_ic_visibility_off.xml`
- `lib_ic_refresh.xml`
- `lib_ic_filter.xml`
- `lib_ic_sort.xml`
- `lib_ic_menu.xml`
- `lib_ic_arrow_back.xml`
- `lib_ic_arrow_forward.xml`
- `lib_ic_cloud.xml`
- `lib_ic_wifi.xml`
- `lib_ic_bluetooth.xml`
- `lib_ic_wallet.xml`
- `lib_ic_credit_card.xml`
- `lib_ic_car.xml`
- `lib_ic_train.xml`
- `lib_ic_medical.xml`
- `lib_ic_school.xml`
- `lib_ic_briefcase.xml`
- `lib_ic_camera.xml`
- `lib_ic_image.xml`
- `lib_ic_music.xml`
- `lib_ic_language.xml`
- `lib_ic_flag.xml`

## Kolory i skalowanie

Każda ikona używa pojedynczego wypełnienia `#FF000000` jako neutralnego koloru bazowego. Nie jest zaszyta w niej paleta marki ani konkretna barwa statusu. Kolor należy nadawać w miejscu użycia, dzięki czemu ten sam plik może mieć różne kolory w różnych kontekstach.

**Widoki Android (XML):**

```xml
<ImageView
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/lib_ic_document"
    app:tint="@color/icon_accent" />
```

**Jetpack Compose:**

```kotlin
Icon(
    painter = painterResource(R.drawable.lib_ic_document),
    contentDescription = "Dokument",
    tint = selectedIconColor
)
```

Kolor można pobierać z motywu, ustawień użytkownika lub palety wybranej w edytorze. Przykładowo: kolor podstawowy, akcentowy, ostrzegawczy, pozytywny, neutralny albo dowolny kolor wybrany przez użytkownika. Dla dostępności należy zachować odpowiedni kontrast i nie kodować statusu wyłącznie barwą.

## Konwencje

- Viewport: 24 × 24; domyślny rozmiar: 24dp.
- Format: Android Vector Drawable XML, bez bitmap i bez zewnętrznych zależności kolorystycznych.
- Kolorowanie: przez `ImageView app:tint` albo parametr `tint` w Compose; nie trzeba tworzyć osobnego pliku dla każdego koloru.
- Zasoby pozostają oddzielnymi plikami, możliwymi do wybrania i przypisania w przyszłym katalogu ikon edytora dokumentów.
- Nie zmieniać nazw ani nie scalać plików w jeden zasób bez aktualizacji katalogu.

## Zakres

Dodano zestaw ogólnego przeznaczenia: profil, nawigacja, dokumenty, komunikacja, ustawienia, bezpieczeństwo, statusy, transport, płatności, multimedia i narzędzia. Są to autorskie, proste piktogramy użytkowe; nie stanowią oficjalnych znaków instytucji ani symboli urzędowych.
