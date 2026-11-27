// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal enum class DocumentPresentationMode(val storageValue: String, val label: String) {
    LEGITIMACY("legitimacy", "Legitymacja"),
    CERTIFICATE("certificate", "Zaświadczenie"),
    CSV("csv", "CSV");

    companion object {
        fun fromStorage(value: String?): DocumentPresentationMode =
            entries.firstOrNull { it.storageValue == value } ?: LEGITIMACY
    }
}

private const val DOCUMENT_PRESENTATION_PREFS = "document_presentation_preferences"
private const val DEFAULT_PRESENTATION_MODE_KEY = "default_mode"

internal fun loadDefaultDocumentPresentationMode(context: Context): DocumentPresentationMode =
    DocumentPresentationMode.fromStorage(
        context.getSharedPreferences(DOCUMENT_PRESENTATION_PREFS, Context.MODE_PRIVATE)
            .getString(DEFAULT_PRESENTATION_MODE_KEY, DocumentPresentationMode.LEGITIMACY.storageValue)
    )

internal fun saveDefaultDocumentPresentationMode(context: Context, mode: DocumentPresentationMode) {
    check(
        context.getSharedPreferences(DOCUMENT_PRESENTATION_PREFS, Context.MODE_PRIVATE)
            .edit().putString(DEFAULT_PRESENTATION_MODE_KEY, mode.storageValue).commit()
    ) { "Nie udało się zapisać domyślnej postaci dokumentów." }
}

internal data class CertificateAppearance(
    val heading: String = "ZAŚWIADCZENIE",
    val issuerCaption: String = "WYSTAWCA",
    val accentColor: String = "#245A43",
    val showIssueDate: Boolean = true,
    val showSignature: Boolean = true,
    val showSeal: Boolean = true,
    val footer: String = "Dokument wygenerowany na podstawie danych zapisanych w aplikacji."
)

private const val CERTIFICATE_APPEARANCE_PREFS = "certificate_appearance_preferences"

internal object CertificateAppearanceStore {
    fun load(context: Context): CertificateAppearance {
        val prefs = context.getSharedPreferences(CERTIFICATE_APPEARANCE_PREFS, Context.MODE_PRIVATE)
        val defaults = CertificateAppearance()
        val color = prefs.getString("accentColor", defaults.accentColor).orEmpty()
        return CertificateAppearance(
            heading = prefs.getString("heading", defaults.heading).orEmpty().take(64).ifBlank { defaults.heading },
            issuerCaption = prefs.getString("issuerCaption", defaults.issuerCaption).orEmpty().take(48).ifBlank { defaults.issuerCaption },
            accentColor = color.takeIf { runCatching { android.graphics.Color.parseColor(it) }.isSuccess } ?: defaults.accentColor,
            showIssueDate = prefs.getBoolean("showIssueDate", defaults.showIssueDate),
            showSignature = prefs.getBoolean("showSignature", defaults.showSignature),
            showSeal = prefs.getBoolean("showSeal", defaults.showSeal),
            footer = prefs.getString("footer", defaults.footer).orEmpty().take(180)
        )
    }

    fun save(context: Context, value: CertificateAppearance) {
        require(runCatching { android.graphics.Color.parseColor(value.accentColor) }.isSuccess)
        check(context.getSharedPreferences(CERTIFICATE_APPEARANCE_PREFS, Context.MODE_PRIVATE).edit()
            .putString("heading", value.heading.take(64))
            .putString("issuerCaption", value.issuerCaption.take(48))
            .putString("accentColor", value.accentColor)
            .putBoolean("showIssueDate", value.showIssueDate)
            .putBoolean("showSignature", value.showSignature)
            .putBoolean("showSeal", value.showSeal)
            .putString("footer", value.footer.take(180))
            .commit()) { "Nie udało się zapisać wzoru zaświadczenia." }
    }
}

@Composable
internal fun DefaultDocumentAppearanceSettingsScreen(
    onBack: () -> Unit,
    onOpenLegitimacy: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(loadDefaultDocumentPresentationMode(context)) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do ustawień", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Domyślny wygląd dokumentów", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(
            "Wybierz postać, w jakiej dokumenty będą domyślnie prezentowane. Ustawienie dotyczy podglądu dokumentów; nie zmienia zapisanych danych ani oryginalnych załączników.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Domyślna postać dokumentu", fontWeight = FontWeight.Bold)
                DocumentPresentationMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            selectedMode = mode
                            saveDefaultDocumentPresentationMode(context, mode)
                        }.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMode == mode, onClick = {
                            selectedMode = mode
                            saveDefaultDocumentPresentationMode(context, mode)
                        })
                        Column(Modifier.weight(1f)) {
                            Text(mode.label, fontWeight = FontWeight.SemiBold)
                            Text(
                                when (mode) {
                                    DocumentPresentationMode.LEGITIMACY -> "Karta dokumentu — ustawienie domyślne"
                                    DocumentPresentationMode.CERTIFICATE -> "Dokument pionowy z nagłówkiem, treścią i podpisem"
                                    DocumentPresentationMode.CSV -> "Dane w układzie tabelarycznym"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Text("Ustawienia wzorów", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth().clickable(onClick = onOpenLegitimacy)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Wygląd legitymacji", fontWeight = FontWeight.Bold)
                Text("Orientacja, rozmieszczenie pól, fotografia, grafika, skala i typografia wspólnego wzoru.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Otwórz ustawienia legitymacji  ›", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
        Card(Modifier.fillMaxWidth().clickable(onClick = onOpenCertificate)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Wygląd zaświadczenia", fontWeight = FontWeight.Bold)
                Text("Nagłówek, kolor przewodni, data wystawienia, podpis, pieczęć i stopka.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Otwórz ustawienia zaświadczenia  ›", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(
            "CSV jest formatem tabelarycznym i nie wymaga osobnego wzoru graficznego.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun CertificateAppearanceSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var appearance by remember { mutableStateOf(CertificateAppearanceStore.load(context)) }
    val accentOptions = listOf(
        "#245A43" to "Zielony",
        "#173B63" to "Granatowy",
        "#6A3D70" to "Purpurowy",
        "#7A4B22" to "Brązowy",
        "#343A40" to "Grafitowy"
    )
    fun update(value: CertificateAppearance) {
        appearance = value
        CertificateAppearanceStore.save(context, value)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do domyślnego wyglądu", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Wspólny wygląd zaświadczenia", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(
            "Jeden wzór jest stosowany do dokumentów prezentowanych jako zaświadczenia. Zmiana wyglądu nie modyfikuje treści ani źródłowych danych dokumentu.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = appearance.heading,
            onValueChange = { update(appearance.copy(heading = it.take(64))) },
            label = { Text("Nagłówek dokumentu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = appearance.issuerCaption,
            onValueChange = { update(appearance.copy(issuerCaption = it.take(48))) },
            label = { Text("Podpis wystawcy — etykieta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kolor przewodni", fontWeight = FontWeight.Bold)
                accentOptions.forEach { (hex, label) ->
                    Row(Modifier.fillMaxWidth().clickable { update(appearance.copy(accentColor = hex)) }
                        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = appearance.accentColor == hex,
                            onClick = { update(appearance.copy(accentColor = hex)) })
                        Box(Modifier.padding(end = 9.dp).background(Color(android.graphics.Color.parseColor(hex)),
                            RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(4.dp)).height(22.dp).fillMaxWidth(0.12f))
                        Text(label)
                    }
                }
            }
        }
        CertificateAppearanceSwitch("Pokazuj datę wystawienia", appearance.showIssueDate) {
            update(appearance.copy(showIssueDate = it))
        }
        CertificateAppearanceSwitch("Pokazuj miejsce na podpis", appearance.showSignature) {
            update(appearance.copy(showSignature = it))
        }
        CertificateAppearanceSwitch("Pokazuj miejsce na pieczęć", appearance.showSeal) {
            update(appearance.copy(showSeal = it))
        }
        OutlinedTextField(
            value = appearance.footer,
            onValueChange = { update(appearance.copy(footer = it.take(180))) },
            label = { Text("Stopka dokumentu") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Text("Podgląd wzoru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        DocumentCertificatePreview(
            documentKey = "certificate_template",
            title = "Przykładowy dokument",
            subtitle = "Nazwa instytucji lub wystawcy",
            holderName = "Jan Kowalski",
            fields = listOf(
                Triple("number", "Numer dokumentu", "CERT/2026/001"),
                Triple("purpose", "Przedmiot zaświadczenia", "Potwierdzenie danych i statusu"),
                Triple("scope", "Zakres", "Dane przykładowe prezentowane w aplikacji"),
                Triple("validity", "Okres ważności", "Do 31.12.2026")
            ),
            validity = true,
            appearanceOverride = appearance,
        )
        OutlinedButton(onClick = { update(CertificateAppearance()) }, modifier = Modifier.fillMaxWidth()) {
            Text("Przywróć ustawienia domyślne")
        }
    }
}

@Composable
private fun CertificateAppearanceSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun DocumentCertificatePreview(
    documentKey: String,
    title: String,
    subtitle: String,
    holderName: String,
    fields: List<Triple<String, String, String>>,
    validity: Boolean? = null,
    appearanceOverride: CertificateAppearance? = null,
) {
    val context = LocalContext.current
    val appearance = appearanceOverride ?: remember(documentKey) { CertificateAppearanceStore.load(context) }
    val accent = Color(android.graphics.Color.parseColor(appearance.accentColor))
    val selectedSignatureId = CertificateSignatureStore.selectedId(context)
    val issueDate = fields.firstOrNull { (key, label, _) ->
        key.contains("date", true) || label.contains("data wystawienia", true) || label.contains("data wydania", true)
    }?.third
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 420.dp).padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(5.dp).background(accent, RoundedCornerShape(4.dp)))
            if (subtitle.isNotBlank()) {
                Text(subtitle.uppercase(), Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                    color = Color(0xFF555555), style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Text(appearance.heading, Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                color = accent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                color = Color(0xFF252525), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Niniejszym potwierdza się, że", Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = Color(0xFF555555),
                style = MaterialTheme.typography.bodyMedium)
            Text(holderName.ifBlank { "DANE OSOBY / PODMIOTU" }, Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = Color(0xFF202020),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Box(Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = 0.55f)))
            fields.filter { it.third.isNotBlank() && it.third != "—" }.forEach { (_, label, value) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, color = Color(0xFF666666), style = MaterialTheme.typography.labelSmall)
                    Text(value, color = Color(0xFF202020), style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
            if (validity != null) {
                Text(if (validity) "Status: dane wskazują na ważność dokumentu" else "Status: dokument może być nieważny lub wymaga weryfikacji",
                    color = if (validity) accent else Color(0xFF9B2C2C),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            if (appearance.showIssueDate) {
                Text("Data wystawienia: " + (issueDate?.takeIf { it.isNotBlank() && it != "—" } ?: "……………………"),
                    color = Color(0xFF444444), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                if (appearance.showSeal) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.height(48.dp).aspectRatio(1f).border(1.dp, accent.copy(alpha = 0.65f),
                            RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                            Text("PIECZĘĆ", color = accent, style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center)
                        }
                    }
                } else Spacer(Modifier.weight(1f))
                if (appearance.showSignature) {
                    Column(Modifier.weight(1.3f), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (selectedSignatureId != null) {
                            CertificateSignatureImage(Modifier.fillMaxWidth().height(42.dp))
                        } else {
                            Box(Modifier.fillMaxWidth().height(34.dp), contentAlignment = Alignment.BottomCenter) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF777777)))
                            }
                        }
                        Text(appearance.issuerCaption, color = Color(0xFF555555),
                            style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
            }
            if (appearance.footer.isNotBlank()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Text(appearance.footer, Modifier.fillMaxWidth(), color = Color(0xFF777777),
                    style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun DocumentCsvPreview(title: String, fields: List<Triple<String, String, String>>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) {
                Text("Pole", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text("Wartość", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
            fields.forEach { (_, label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 8.dp)) {
                    Text(label, Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall)
                    Text(value, Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
    }
}
