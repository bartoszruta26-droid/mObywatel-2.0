// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

@Composable
fun StudiaOnlineLoginWebView(
    credentialStore: StudiaOnlineCredentialStore,
    onStatus: (String) -> Unit,
    onSessionReady: () -> Unit,
    onClose: () -> Unit
) {
    var loginWebView by remember { mutableStateOf<WebView?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(520.dp),
            factory = { viewContext ->
                WebView(viewContext).apply {
                    loginWebView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                            !isAllowedStudiaOnlineUrl(request.url)

                        override fun onPageFinished(view: WebView, url: String) {
                            if (!isAllowedStudiaOnlineUrl(Uri.parse(url))) {
                                onStatus("Zablokowano nawigację poza zabezpieczoną domenę studia-online.pl.")
                            } else {
                                onStatus("Załadowano stronę serwisu. Samo załadowanie strony nie potwierdza logowania; sprawdź, czy widzisz swój panel studenta.")
                            }
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            if (request.isForMainFrame) {
                                onStatus("Błąd ładowania strony logowania (${error.errorCode}): ${error.description}")
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView,
                            request: WebResourceRequest,
                            errorResponse: WebResourceResponse
                        ) {
                            if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                                onStatus("Serwis zwrócił HTTP ${errorResponse.statusCode} podczas ładowania strony logowania.")
                            }
                        }
                    }
                    loadUrl("https://studia-online.pl/konto")
                }
            },
            update = { view -> loginWebView = view }
        )
        Button(
            onClick = {
                val view = loginWebView
                if (view == null) {
                    onStatus("Okno logowania nie jest jeszcze gotowe.")
                    return@Button
                }
                val credentials = credentialStore.load()
                if (credentials == null) {
                    onStatus("Brak zapisanych danych logowania. Wpisz je na oficjalnej stronie albo zapisz w zaszyfrowanym magazynie.")
                    return@Button
                }
                val usernameJson = JSONObject.quote(credentials.username)
                val passwordJson = try {
                    JSONObject.quote(String(credentials.password))
                } finally {
                    credentials.password.fill(Char.MIN_VALUE)
                }
                val script = """
                    (function() {
                      const pass = document.querySelector('input[type="password"]');
                      const user = document.querySelector(
                        'input[autocomplete="username"], input[name*="login" i], input[placeholder*="login" i]'
                      );
                      if (!pass || !user || !pass.form || pass.form !== user.form) return "LOGIN_FIELDS_NOT_FOUND";
                      const form = pass.form;
                      const target = new URL(form.getAttribute('action') || location.href, location.href);
                      const hostAllowed = target.protocol === "https:" &&
                        (target.hostname === "studia-online.pl" || target.hostname.endsWith(".studia-online.pl"));
                      if (!hostAllowed) return "UNTRUSTED_FORM_ACTION";
                      const setValue = (element, value) => {
                        const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value").set;
                        setter.call(element, value);
                        element.dispatchEvent(new Event("input", { bubbles: true }));
                        element.dispatchEvent(new Event("change", { bubbles: true }));
                      };
                      setValue(user, $usernameJson);
                      setValue(pass, $passwordJson);
                      return "FILLED";
                    })();
                """.trimIndent()
                view.evaluateJavascript(script) { result ->
                    onStatus(
                        when {
                            result?.contains("FILLED") == true ->
                                "Uzupełniono pola formularza. Sprawdź dane i samodzielnie zatwierdź logowanie na stronie."
                            result?.contains("LOGIN_FIELDS_NOT_FOUND") == true ->
                                "Nie rozpoznano pól logowania. Wpisz dane ręcznie w formularzu serwisu."
                            result?.contains("UNTRUSTED_FORM_ACTION") == true ->
                                "Zablokowano formularz kierujący poza zabezpieczoną domenę serwisu."
                            else -> "Nie udało się uzupełnić formularza. Możesz zalogować się ręcznie."
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Uzupełnij formularz zapisanymi danymi") }
        Button(
            onClick = {
                val view = loginWebView
                if (view == null) {
                    onStatus("Okno logowania nie jest jeszcze gotowe.")
                    return@Button
                }
                val script = """
                    (function() {
                      const pass = document.querySelector('input[type="password"]');
                      const user = document.querySelector(
                        'input[autocomplete="username"], input[name*="login" i], input[name*="user" i], input[placeholder*="login" i]'
                      );
                      if (!pass || !user || !pass.form || pass.form !== user.form) return "LOGIN_FIELDS_NOT_FOUND";
                      if (!user.value.trim() || !pass.value) return "LOGIN_FIELDS_EMPTY";
                      const form = pass.form;
                      const target = new URL(form.getAttribute('action') || location.href, location.href);
                      const hostAllowed = target.protocol === "https:" &&
                        (target.hostname === "studia-online.pl" || target.hostname.endsWith(".studia-online.pl"));
                      if (!hostAllowed) return "UNTRUSTED_FORM_ACTION";
                      const submitter = form.querySelector(
                        'button[type="submit"]:not([disabled]), input[type="submit"]:not([disabled]), button:not([type]):not([disabled])'
                      );
                      if (typeof form.requestSubmit === "function") {
                        if (submitter) form.requestSubmit(submitter);
                        else form.requestSubmit();
                      } else if (submitter) {
                        submitter.click();
                      } else {
                        return "SUBMIT_CONTROL_NOT_FOUND";
                      }
                      return "SUBMIT_REQUESTED";
                    })();
                """.trimIndent()
                view.evaluateJavascript(script) { result ->
                    onStatus(
                        when {
                            result?.contains("SUBMIT_REQUESTED") == true ->
                                "Wysłano formularz logowania do studia-online.pl. Poczekaj na odpowiedź serwisu i sprawdź, czy otworzył się panel studenta."
                            result?.contains("LOGIN_FIELDS_EMPTY") == true ->
                                "Wpisz login i hasło w formularzu przed naciśnięciem przycisku Zaloguj."
                            result?.contains("LOGIN_FIELDS_NOT_FOUND") == true ->
                                "Nie rozpoznano formularza logowania. Użyj formularza widocznego na stronie serwisu."
                            result?.contains("UNTRUSTED_FORM_ACTION") == true ->
                                "Zablokowano wysłanie formularza poza zabezpieczoną domenę studia-online.pl."
                            result?.contains("SUBMIT_CONTROL_NOT_FOUND") == true ->
                                "Nie znaleziono przycisku zatwierdzającego formularz logowania."
                            else -> "Nie udało się wysłać formularza. Sprawdź dane i spróbuj ponownie."
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Zaloguj") }
        Button(
            onClick = {
                CookieManager.getInstance().flush()
                val cookies = StudiaOnlineSessionCookies.currentHeader()
                if (cookies.isNullOrBlank()) {
                    onStatus("Brak ciasteczek dostępnych dla ścieżki pobierania. Pozostań w tym oknie i zaloguj się na stronie.")
                } else {
                    val count = cookies.split(';').count { it.contains('=') }
                    onStatus("Odczytano $count ciasteczek dla adresu pobierania. Ich wartości nie są ujawniane. Sprawdź, czy widzisz panel studenta; aktywność sesji potwierdzi odpowiedź serwera. Okno nie zostanie zamknięte.")
                    onSessionReady()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sprawdź ciasteczka sesji") }
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Zamknij okno logowania")
        }
    }
}

internal fun isAllowedStudiaOnlineUrl(uri: Uri): Boolean =
    uri.scheme.equals("https", ignoreCase = true) && isAllowedStudiaOnlineHost(uri.host.orEmpty())

internal fun isAllowedStudiaOnlineHost(host: String): Boolean =
    host.equals("studia-online.pl", ignoreCase = true) ||
        host.endsWith(".studia-online.pl", ignoreCase = true)
