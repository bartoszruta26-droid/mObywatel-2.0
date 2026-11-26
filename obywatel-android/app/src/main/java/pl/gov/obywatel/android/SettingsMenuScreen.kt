// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsMenuScreen(
    documentPlacements: Map<String, String>,
    onManageApplicationAppearance: () -> Unit,
    onManageTranslationLanguages: () -> Unit,
    onManageTorClients: () -> Unit,
    onManageCredentialAppearance: () -> Unit,
    onManageDocumentEditing: () -> Unit,
    onDocumentPlacementsChange: (Map<String, String>) -> Unit,
    onBack: () -> Unit,
    onManageDocumentVisibility: () -> Unit,
    onManageDocumentBrandAssets: () -> Unit,
    onEditDocumentBranding: (String) -> Unit,
    onEditDocuments: (String) -> Unit,
    onEditDisability: () -> Unit,
    onEditRegistry: () -> Unit,
    onEditReceipts: () -> Unit,
    onEditOtherDocuments: (String) -> Unit,
    onExportAll: () -> Unit,
    onImportAll: () -> Unit,
    importStatus: String
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Powrót do ekranu głównego", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Ustawienia", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Zarządzanie danymi", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Edycja tylko w Ustawieniach", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Portfel dokumentów i jego szczegóły są widokami do odczytu. Formularze edycji można otworzyć wyłącznie z tego menu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProfileActionCard(
            "Ustawienia aplikacji",
            "Motyw kolorystyczny, godło oraz tła ekranów uruchamiania i logowania",
            Icons.Filled.Settings,
            onManageApplicationAppearance
        )
        ProfileActionCard(
            "Klienci sieci Tor",
            "Wybór i zarządzanie klientami Tor, w tym wbudowanym klientem oraz Orbotem",
            Icons.Filled.Security,
            onManageTorClients
        )
        ProfileActionCard(
            "Języki i tłumaczenia",
            "Zarządzaj modelami tłumaczeń, automatycznym pobieraniem i użyciem Wi-Fi",
            Icons.Filled.Settings,
            onManageTranslationLanguages
        )
        ProfileActionCard(
            "Wspólny wygląd legitymacji",
            "Jeden układ, rozmieszczenie, skala i typografia dla wszystkich legitymacji",
            Icons.Filled.Badge,
            onManageCredentialAppearance
        )
        ProfileActionCard(
            "Widoczność dokumentów",
            "Wybierz: Dokumenty, Inne dokumenty albo Ukryj",
            Icons.Filled.Security,
            onManageDocumentVisibility
        )
        ProfileActionCard(
            "Zasoby graficzne dokumentów",
            "Dodawanie i przypisywanie teł, herbów, godła, logo, flag oraz fotografii",
            Icons.Filled.Description,
            onManageDocumentBrandAssets
        )
        Text("Kopia danych vCard", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Eksport obejmuje pola profilu i ewidencji, w tym załączone dane tekstowe. Plik może zawierać dane wrażliwe — przechowuj go bezpiecznie.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard("Eksportuj wszystkie dane", "Zapisz jedno archiwum .vcf", Icons.Filled.Description, onExportAll)
        ProfileActionCard("Importuj dane z pliku vCard", "Wczytaj archiwum .vcf i zatwierdź zakres zmian", Icons.Filled.Description, onImportAll)
        if (importStatus.isNotBlank()) Text(importStatus, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        ProfileActionCard(
            "Edytuj dane dokumentów",
            "Otwórz katalog wszystkich dokumentów, uporządkowany w rozwijane grupy. Każdy dokument ma osobny formularz edycji.",
            Icons.Filled.Description,
            onManageDocumentEditing
        )
        ProfileActionCard("Rachunki", "Modyfikacja wzoru papierowego rachunku i treści pola Uwagi", Icons.Filled.Description, onEditReceipts)
        Text("Dane możesz uzupełniać własnym oświadczeniem, wskazując ich źródło, podstawę i zakres odpowiedzialności.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
