// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun MoreMenuScreen(
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenIdentityDesigner: () -> Unit,
    onOpenImportedDocuments: () -> Unit,
    onOpenPaymentLedger: () -> Unit,
    onOpenLegalInformation: () -> Unit,
    onOpenStudiaOnlineArchive: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Więcej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Informacje, bezpieczeństwo i ustawienia", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Historia działań", "Lista czynności w aplikacji", Icons.Filled.CalendarMonth, onOpenHistory)
        ProfileActionCard("Certyfikaty", "Biblioteka certyfikatów, generator, podpisy i pieczęcie", Icons.Filled.Security, onOpenCertificates)
        ProfileActionCard("Powiadomienia", "Ustawienia powiadomień", Icons.Filled.NotificationsNone, onOpenHelp)
        ProfileActionCard("Projektant dokumentów", "Tworzenie dokumentów, układ, podgląd i eksport PDF", Icons.Filled.Description, onOpenIdentityDesigner)
        ProfileActionCard("Pliki i załączniki", "Import i podgląd plików PDF, DOCX oraz obrazów", Icons.Filled.Description, onOpenImportedDocuments)
        ProfileActionCard("Rachunki płatnicze", "Rejestr transakcji, walut, kruszców i ewidencja darowizn", Icons.Filled.Payments, onOpenPaymentLedger)
        ProfileActionCard(
            "Archiwum materiałów – pobierz moje pliki",
            "Pobierz materiały z studia-online.pl przez Tor, z przerwami i wyborem folderu",
            Icons.Filled.Description,
            onOpenStudiaOnlineArchive
        )
        ProfileActionCard("Pomoc i informacje", "Instrukcje korzystania z aplikacji", Icons.Filled.Info, onOpenHelp)
        ProfileActionCard("Ustawienia", "Dane, wygląd i preferencje", Icons.Filled.Settings, onOpenSettings)
        ProfileActionCard("Informacje prawne", "Podstawy prawne, prawa obywatelskie i zasady działania aplikacji", Icons.Filled.Description, onOpenLegalInformation)
        ProfileActionCard("Regulamin dotacji celowych państwowego funduszu celowego", "Wyodrębniony cel rehabilitacyjno-opiekuńczy w ramach istniejącego funduszu", Icons.Filled.AccountBalance, onOpenLegalInformation)
    }
}

@Composable
internal fun ProfileLandingScreen(
    onOpenDocuments: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCertificates: () -> Unit
) {
    val context = LocalContext.current
    val fields = remember { DemoIdentityStore.load(context) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Więcej", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CivicIllustrationBanner(
            imageRes = R.drawable.header_logo_panel,
            description = "Panel znaku i identyfikacji aplikacji"
        )
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ").ifBlank { "Użytkownik" },
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                    Text("Profil użytkownika", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileActionCard("Dokumenty i dane", "Przeglądaj dane i oświadczenia", Icons.Filled.CreditCard, onOpenDocuments)
        ProfileActionCard("Certyfikaty", "Generowanie certyfikatów oraz zarządzanie wzorami, podpisami i pieczęciami", Icons.Filled.Description, onOpenCertificates)
        ProfileActionCard("Ustawienia", "Otwórz menu zarządzania danymi", Icons.Filled.Settings, onOpenSettings)
        Text(
            "Dane profilu możesz uzupełniać i aktualizować w ustawieniach.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
        )
    }
}
