// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp







@Composable
fun LegalInformationScreen(onBack: () -> Unit) {
    var showFundRegulation by rememberSaveable { mutableStateOf(false) }
    var showDetailedPfcModel by rememberSaveable { mutableStateOf(false) }
    var showEntrustmentDocument by rememberSaveable { mutableStateOf(false) }
    var showCompanyFinancingAgreement by rememberSaveable { mutableStateOf(false) }
    var showNonParliamentaryMandate by rememberSaveable { mutableStateOf(false) }
    var showPublicAuthorityPfc by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showFundRegulation || showDetailedPfcModel || showEntrustmentDocument || showCompanyFinancingAgreement || showNonParliamentaryMandate || showPublicAuthorityPfc) {
        showFundRegulation = false
        showDetailedPfcModel = false
        showEntrustmentDocument = false
        showCompanyFinancingAgreement = false
        showNonParliamentaryMandate = false
        showPublicAuthorityPfc = false
    }
    BackHandler(enabled = !showFundRegulation && !showDetailedPfcModel && !showEntrustmentDocument && !showCompanyFinancingAgreement && !showNonParliamentaryMandate && !showPublicAuthorityPfc) { onBack() }

    val displayedText = when {
        showCompanyFinancingAgreement -> markdownToDisplayText(COMPANY_FINANCING_AGREEMENT_TEXT)
        showNonParliamentaryMandate -> NON_PARLIAMENTARY_MANDATE_INTERPRETATION
        showPublicAuthorityPfc -> PUBLIC_AUTHORITY_PFC_INTERPRETATION
        showEntrustmentDocument -> markdownToDisplayText(ENTRUSTMENT_DOCUMENT_TEXT)
        showDetailedPfcModel -> DETAILED_PFC_MODEL_TEXT
        showFundRegulation -> markdownToDisplayText(FUND_REGULATION_MARKDOWN)
        else -> LEGAL_INFORMATION_TEXT
    }
    val pageTitle = when {
        showCompanyFinancingAgreement -> "Umowa odpłatnego współfinansowania Celu"
        showNonParliamentaryMandate -> "Interpretacja prawna mandatu nieparlamentarnego"
        showPublicAuthorityPfc -> "Piastowanie organu władzy publicznej i Cel PFC"
        showEntrustmentDocument -> "Powierzenie środków Dyspozytorowi Celu"
        showDetailedPfcModel -> "Szczegółowy model prawny Celu"
        showFundRegulation -> "Regulamin dotacji celowych państwowego funduszu celowego"
        else -> "Informacje prawne"
    }
    val sections = displayedText.trim().split("\n")

    Surface(
        modifier = Modifier.fillMaxSize().semantics { paneTitle = pageTitle },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    if (showFundRegulation || showDetailedPfcModel || showEntrustmentDocument || showCompanyFinancingAgreement || showNonParliamentaryMandate || showPublicAuthorityPfc) {
                        showFundRegulation = false
                        showDetailedPfcModel = false
                        showEntrustmentDocument = false
                        showCompanyFinancingAgreement = false
                        showNonParliamentaryMandate = false
                        showPublicAuthorityPfc = false
                    } else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    pageTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
            }
            if (!showFundRegulation && !showDetailedPfcModel && !showEntrustmentDocument && !showCompanyFinancingAgreement && !showNonParliamentaryMandate && !showPublicAuthorityPfc) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showNonParliamentaryMandate = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column(Modifier.weight(1f)) {
                            Text("Mandat Posła z Powołania — mandat nieparlamentarny", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Powołanie do służby Narodowi, publiczne ślubowanie i bezpośrednie sprawowanie władzy suwerena", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz interpretację")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showPublicAuthorityPfc = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Column(Modifier.weight(1f)) {
                            Text("Piastowanie organu władzy publicznej i ustanowienie Celu PFC", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Poseł z Powołania jako inicjator Celu, Dyspozytor i wnioskodawca finansowania", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz interpretację")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showFundRegulation = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Regulamin dotacji celowych państwowego funduszu celowego",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Cel rehabilitacyjno-opiekuńczy w ramach istniejącego funduszu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz regulamin")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDetailedPfcModel = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f)) {
                            Text("Szczegółowy model prawny Celu", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Rozwinięcie podstaw prawnych, modelu finansowania i odpowiedzialności Dyspozytora", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz szczegółowy model")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showEntrustmentDocument = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Column(Modifier.weight(1f)) {
                            Text("Dokument powierzenia środków Dyspozytorowi Celu", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Wzór dla Celu Służby Narodowej — rachunek płatniczy i rozliczenie", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz dokument")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showCompanyFinancingAgreement = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Column(Modifier.weight(1f)) {
                            Text("Umowa odpłatnego współfinansowania Celu", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("Wzór współpracy z przedsiębiorcą wraz z trzema załącznikami", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz umowę")
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sections.forEach { line ->
                        when {
                            line.isBlank() -> Spacer(Modifier.height(3.dp))
                            line == line.uppercase() && line.length < 110 ->
                                Text(line, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            line.startsWith("• ") ->
                                Text(line, style = MaterialTheme.typography.bodyMedium)
                            else ->
                                Text(line, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}


/**
 * Converts the source Markdown of legal documents into readable text for the
 * Compose screen. The original Markdown remains intact in its dedicated file.
 */
private fun markdownToDisplayText(markdown: String): String =
    markdown.lineSequence()
        .map { line ->
            line.trimEnd()
                .replace(Regex("^#{1,6}\\s+"), "")
                .replace(Regex("^>\\s?"), "")
                .replace(Regex("^[-*]\\s+"), "• ")
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "\$1")
                .replace(Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)"), "\$1")
                .replace(Regex("`([^`]+)`"), "\$1")
                .replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "\$1 (\$2)")
                .takeUnless { it.trim() == "---" }
                .orEmpty()
        }
        .joinToString("\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
