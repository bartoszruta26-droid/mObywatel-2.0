# Archiwum klucza podpisującego Android — instrukcja

**Uwaga: repozytorium jest publiczne.** W tym katalogu znajduje się wyłącznie zaszyfrowany pakiet. Nie umieszczaj tu hasła deszyfrującego, hasła keystore ani jawnego pliku `.p12` / Base64 klucza.

## Pliki

- `mobywatel-android-signing-private.zip.enc.b64` — zaszyfrowane archiwum zakodowane Base64.
- Hasło do odszyfrowania archiwum jest przekazywane właścicielowi repozytorium osobnym kanałem i nie znajduje się w repozytorium.

## Odszyfrowanie (Linux / macOS / Windows z OpenSSL)

1. Pobierz plik `mobywatel-android-signing-private.zip.enc.b64`.
2. Zdekoduj Base64 do pliku binarnego:
   ```sh
   base64 -d mobywatel-android-signing-private.zip.enc.b64 > mobywatel-android-signing-private.zip.enc
   ```
   W PowerShell zamiast tego:
   ```powershell
   [IO.File]::WriteAllBytes("mobywatel-android-signing-private.zip.enc", [Convert]::FromBase64String((Get-Content -Raw "mobywatel-android-signing-private.zip.enc.b64")))
   ```
3. Odszyfruj, podając osobno otrzymane hasło archiwum:
   ```sh
   openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in mobywatel-android-signing-private.zip.enc -out mobywatel-android-signing-private.zip
   ```
   OpenSSL poprosi o hasło. Nie wpisuj go do historii poleceń.
4. Rozpakuj ZIP. Zawiera plik keystore `.p12`, jego wariant Base64, plik hasła keystore oraz instrukcję dodania sekretów GitHub Actions.

## Sekrety GitHub Actions

W ustawieniach repozytorium: **Settings → Secrets and variables → Actions** dodaj:

- `ANDROID_KEYSTORE_BASE64` — cała zawartość `mobywatel-upload-key.p12.base64`
- `ANDROID_KEYSTORE_PASSWORD` — wartość z `ANDROID_KEY_PASSWORD.txt`
- `ANDROID_KEY_ALIAS` — `mobywatel-upload`
- `ANDROID_KEY_PASSWORD` — ta sama wartość co `ANDROID_KEYSTORE_PASSWORD`

**Nie commituj odszyfrowanego archiwum, keystore, pliku Base64 klucza ani żadnego pliku z hasłem.** Po dodaniu sekretów sprawdź podpis APK i fingerprint certyfikatu. Zachowaj bezpieczną kopię zapasową pakietu oraz hasła deszyfrującego poza GitHub.
