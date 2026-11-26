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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp







@Composable
fun LegalInformationScreen(onBack: () -> Unit) {
    var showFundRegulation by rememberSaveable { mutableStateOf(false) }
    var showPfcProjectDocument by rememberSaveable { mutableStateOf(false) }
    var showEntrustmentDocument by rememberSaveable { mutableStateOf(false) }
    var showCompanyFinancingAgreement by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val pfcProjectDocumentText = remember(context, showPfcProjectDocument) {
        if (showPfcProjectDocument) loadPfcProjectDocumentText(context) else ""
    }
    BackHandler(enabled = showFundRegulation || showPfcProjectDocument || showEntrustmentDocument || showCompanyFinancingAgreement) {
        showFundRegulation = false
        showPfcProjectDocument = false
        showEntrustmentDocument = false
        showCompanyFinancingAgreement = false
    }
    BackHandler(enabled = !showFundRegulation && !showPfcProjectDocument && !showEntrustmentDocument && !showCompanyFinancingAgreement) { onBack() }

    val displayedText = when {
        showPfcProjectDocument -> markdownToDisplayText(pfcProjectDocumentText)
        showCompanyFinancingAgreement -> COMPANY_FINANCING_AGREEMENT_TEXT
        showEntrustmentDocument -> ENTRUSTMENT_DOCUMENT_TEXT
        showFundRegulation -> LEGAL_INFORMATION_TEXT.substringAfter("\nREGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY W RAMACH ISTNIEJĄCEGO PAŃSTWOWEGO FUNDUSZU CELOWEGO")
        else -> LEGAL_INFORMATION_TEXT.substringBefore("\nREGULAMIN DOTACJI CELOWYCH — CEL REHABILITACYJNO-OPIEKUŃCZY W RAMACH ISTNIEJĄCEGO PAŃSTWOWEGO FUNDUSZU CELOWEGO")
    }
    val pageTitle = when {
        showPfcProjectDocument -> "Pakiet dokumentów Celu PFC"
        showCompanyFinancingAgreement -> "Umowa odpłatnego współfinansowania Celu"
        showEntrustmentDocument -> "Powierzenie środków Dyspozytorowi Celu"
        showFundRegulation -> "Regulamin dotacji celowych państwowego funduszu celowego"
        else -> "Informacje prawne"
    }
    val sections = if (showPfcProjectDocument) emptyList() else displayedText.trim().split("\n")

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
                    if (showFundRegulation || showPfcProjectDocument || showEntrustmentDocument || showCompanyFinancingAgreement) {
                        showFundRegulation = false
                        showPfcProjectDocument = false
                        showEntrustmentDocument = false
                        showCompanyFinancingAgreement = false
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
            if (!showFundRegulation && !showPfcProjectDocument && !showEntrustmentDocument && !showCompanyFinancingAgreement) {
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
                    modifier = Modifier.fillMaxWidth().clickable { showPfcProjectDocument = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f)) {
                            Text("Pakiet regulaminu i podstaw prawnych Celu PFC", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Regulamin, wyjaśnienia dotyczące Posła i organów państwa oraz wzór powierzenia środków", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.Description, contentDescription = "Otwórz pakiet dokumentów")
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
                    if (showPfcProjectDocument) {
                        Text(displayedText, style = MaterialTheme.typography.bodyMedium)
                    } else {
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
}


/** Renders source Markdown as readable text while preserving the complete content. */
private fun markdownToDisplayText(markdown: String): String =
    markdown.lineSequence()
        .map { line ->
            line.trimEnd()
                .replace(Regex("^#{1,6}\\s+"), "")
                .replace(Regex("^>\\s?"), "")
                .replace(Regex("^[-*]\\s+"), "• ")
                .takeUnless { it.trim() == "---" }
                .orEmpty()
        }
        .joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
