// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class SectionInfo(val title: String, val description: String, val status: String)

private fun sectionInfo(id: String): SectionInfo = when (id) {
    "verify" -> SectionInfo(
        "Weryfikacja QR",
        "Ten ekran jest przygotowany jako punkt wejścia do skanowania identyfikatorów rejestru. Obecna wersja nie uruchamia aparatu ani nie weryfikuje dokumentów zdalnie.",
        "Skanowanie aparatem i zdalny punkt weryfikacji — do wdrożenia."
    )
    "profile" -> SectionInfo("Moje dane",
        "Ustawienia profilu przechowywanego lokalnie na urządzeniu. Nie jest to konto państwowe ani profil zaufany.",
        "Lokalny profil demonstracyjny — bez uwierzytelniania.")
    "security" -> SectionInfo("Bezpieczeństwo",
        "Docelowo: blokada aplikacji, kontrola sesji, eksport danych i zarządzanie prywatnością. Nie zapisuj tutaj haseł ani sekretów.",
        "Ustawienia zabezpieczeń — do wdrożenia.")
    "payments" -> SectionInfo("Płatności",
        "Moduł informacyjny. Aplikacja nie realizuje płatności ani nie łączy się z systemami administracji.",
        "Integracja płatności — niedostępna.")
    "health" -> SectionInfo("Zdrowie",
        "Moduł informacyjny przeznaczony na przyszłe funkcje związane z dokumentacją zdrowotną.",
        "Integracja usług zdrowotnych — niedostępna.")
    else -> SectionInfo("Informacje o aplikacji",
        "Niezależny prototyp Android do lokalnego porządkowania dokumentów i identyfikatorów rejestru.",
        "Prototyp interfejsu.")
}

@Composable
fun SectionScreen(sectionId: String, onBack: () -> Unit) {
    val info = sectionInfo(sectionId)
    Surface(Modifier.fillMaxSize().semantics { paneTitle = info.title },
        color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(info.title, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).semantics { heading() })
            }
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(info.description, style = MaterialTheme.typography.bodyLarge)
                    Text(info.status, style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
