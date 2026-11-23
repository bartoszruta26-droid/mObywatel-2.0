// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeParseException

private data class CertificateStandardProfile(
    val id: String,
    val title: String,
    val reference: String,
    val purpose: String,
    val limitation: String
)

private val certificateProfiles = listOf(
    CertificateStandardProfile("training", "Certyfikat ukończenia szkolenia", "Wzór własny",
        "Potwierdzenie udziału lub ukończenia szkolenia, kursu albo warsztatu.",
        "Wzór nie jest akredytacją, certyfikacją kompetencji ani poświadczeniem zgodności z normą."),
    CertificateStandardProfile("iso9001", "System zarządzania jakością", "ISO 9001",
        "Materiały szkoleniowe i potwierdzenie udziału w szkoleniu dotyczącym systemu zarządzania jakością.",
        "Nie potwierdza certyfikacji systemu zarządzania jakością organizacji."),
    CertificateStandardProfile("iso21001", "Organizacja edukacyjna", "ISO 21001",
        "Materiały szkoleniowe dotyczące systemów zarządzania organizacjami edukacyjnymi.",
        "Nie potwierdza zgodności ani certyfikacji organizacji edukacyjnej."),
    CertificateStandardProfile("iso17024", "Certyfikacja osób", "ISO/IEC 17024",
        "Odniesienie informacyjne dla programów certyfikacji osób.",
        "Ukończenie kursu nie stanowi certyfikacji osoby według ISO/IEC 17024."),
    CertificateStandardProfile("iso7101", "Jakość w ochronie zdrowia", "ISO 7101",
        "Materiały szkoleniowe dotyczące zarządzania jakością w ochronie zdrowia.",
        "Nie potwierdza certyfikacji placówki ani systemu zarządzania."),
    CertificateStandardProfile("iso13485", "Wyroby medyczne", "ISO 13485",
        "Materiały szkoleniowe dotyczące systemów jakości dla wyrobów medycznych.",
        "Nie potwierdza certyfikacji producenta, systemu ani wyrobu medycznego.")
)

private fun safeSvgText(value: String): String = value
    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    .replace("\"", "&quot;").replace("'", "&apos;")

internal fun makeTextSignatureSvg(name: String, color: String): ByteArray {
    val safeColor = SvgSafetyValidator.requireSafeHexColor(color)
    val safeName = safeSvgText(name.trim().take(90))
    require(safeName.isNotBlank()) { "Wpisz tekst podpisu." }
    return """<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="340" viewBox="0 0 1200 340">
<defs>
  <linearGradient id="ink" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="$safeColor"/>
    <stop offset=".52" stop-color="$safeColor" stop-opacity=".88"/>
    <stop offset="1" stop-color="$safeColor"/>
  </linearGradient>
</defs>
<rect width="1200" height="340" fill="none"/>
<path d="M90 250 C190 220 220 285 320 248 S470 226 545 248" fill="none" stroke="$safeColor" stroke-width="2.4" stroke-linecap="round" opacity=".72"/>
<text x="600" y="188" text-anchor="middle"
  font-family="Dancing Script, Segoe Script, Brush Script MT, URW Chancery L, cursive"
  font-size="118" font-style="italic" font-weight="500" letter-spacing="-3"
  fill="url(#ink)">$safeName</text>
<path d="M210 224 C350 300 540 275 700 226 C835 185 945 205 1040 244
         C1082 262 1114 251 1130 226
         M850 263 C940 293 1050 286 1110 267
         M180 230 C130 260 112 280 135 290 C156 299 183 279 194 262"
      fill="none" stroke="$safeColor" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>
<path d="M105 115 C125 87 148 87 166 113 C145 109 132 121 128 141
         M1034 105 C1055 77 1084 78 1102 106 C1080 100 1062 114 1060 136"
      fill="none" stroke="$safeColor" stroke-width="2.2" stroke-linecap="round" opacity=".82"/>
<path d="M260 292 Q600 318 940 288" fill="none" stroke="$safeColor" stroke-width="1.5" stroke-linecap="round" opacity=".55"/>
<g fill="$safeColor" opacity=".8">
  <circle cx="225" cy="104" r="4"/><circle cx="975" cy="100" r="4"/>
  <path d="M190 86 l5 12 12 5 -12 5 -5 12 -5 -12 -12 -5 12 -5z"/>
  <path d="M1010 78 l5 12 12 5 -12 5 -5 12 -5 -12 -12 -5 12 -5z"/>
</g>
</svg>""".toByteArray(Charsets.UTF_8)
}

internal fun makeStampSvg(organization: String, centerText: String, identifier: String, color: String): ByteArray {
    val safeColor = SvgSafetyValidator.requireSafeHexColor(color)
    val org = safeSvgText(organization.trim().take(70))
    val center = safeSvgText(centerText.trim().take(50))
    val number = safeSvgText(identifier.trim().take(40))
    require(org.isNotBlank()) { "Wpisz nazwę organizacji." }
    return """<svg xmlns="http://www.w3.org/2000/svg" width="720" height="720" viewBox="0 0 720 720">
<defs>
  <path id="topArc" d="M 105,360 A 255,255 0 0 1 615,360"/>
  <path id="bottomArc" d="M 615,360 A 255,255 0 0 1 105,360"/>
  <radialGradient id="sealInk"><stop offset="0" stop-color="$safeColor" stop-opacity=".08"/><stop offset="1" stop-color="$safeColor" stop-opacity="0"/></radialGradient>
</defs>
<rect width="720" height="720" fill="none"/>
<circle cx="360" cy="360" r="315" fill="url(#sealInk)" stroke="$safeColor" stroke-width="3"/>
<circle cx="360" cy="360" r="294" fill="none" stroke="$safeColor" stroke-width="13"/>
<circle cx="360" cy="360" r="275" fill="none" stroke="$safeColor" stroke-width="2.5"/>
<circle cx="360" cy="360" r="238" fill="none" stroke="$safeColor" stroke-width="2" stroke-dasharray="2 9"/>
<circle cx="360" cy="360" r="205" fill="none" stroke="$safeColor" stroke-width="4"/>
<circle cx="360" cy="360" r="184" fill="none" stroke="$safeColor" stroke-width="1.8"/>
<text fill="$safeColor" font-family="serif" font-size="29" font-weight="bold" letter-spacing="2.2">
  <textPath href="#topArc" startOffset="50%" text-anchor="middle">$org</textPath>
</text>
<text fill="$safeColor" font-family="serif" font-size="21" font-weight="bold" letter-spacing="3">
  <textPath href="#bottomArc" startOffset="50%" text-anchor="middle">$number</textPath>
</text>
<g fill="$safeColor">
  <path d="M360 125 l11 25 27 2 -21 17 7 27 -24 -15 -24 15 7 -27 -21 -17 27 -2z"/>
  <path d="M360 524 l8 18 20 2 -15 13 5 20 -18 -11 -18 11 5 -20 -15 -13 20 -2z"/>
  <circle cx="151" cy="360" r="9"/><circle cx="569" cy="360" r="9"/>
  <circle cx="180" cy="180" r="5"/><circle cx="540" cy="180" r="5"/>
  <circle cx="180" cy="540" r="5"/><circle cx="540" cy="540" r="5"/>
</g>
<g fill="none" stroke="$safeColor" stroke-width="3" stroke-linecap="round">
  <path d="M220 260 Q250 220 280 260 Q250 300 220 260 M500 260 Q470 220 440 260 Q470 300 500 260"/>
  <path d="M220 460 Q250 500 280 460 Q250 420 220 460 M500 460 Q470 500 440 460 Q470 420 500 460"/>
  <path d="M260 360 Q310 315 360 360 Q410 405 460 360"/>
</g>
<text x="360" y="338" text-anchor="middle" font-family="serif" font-size="42" font-weight="bold" letter-spacing="1.5" fill="$safeColor">$center</text>
<path d="M245 375 Q360 405 475 375" fill="none" stroke="$safeColor" stroke-width="2.5"/>
<text x="360" y="420" text-anchor="middle" font-family="serif" font-size="18" letter-spacing="2" fill="$safeColor">WZÓR WŁASNY</text>
<text x="360" y="448" text-anchor="middle" font-family="sans-serif" font-size="13" letter-spacing="1.1" fill="$safeColor">NIE JEST PIECZĘCIĄ URZĘDOWĄ</text>
</svg>""".toByteArray(Charsets.UTF_8)
}

private val certificateAssetKinds = setOf(
    "Certyfikat", "Wzór podpisu odręcznego", "Podpis wektorowy (tekst)",
    "Pieczęć", "Pieczęć graficzna (własna)"
)

@Composable
internal fun CertificateStudioScreen(onBack: () -> Unit, onOpenDigitalCertificates: () -> Unit = {}) {
    val context = LocalContext.current
    var assets by remember { mutableStateOf(DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }) }
    var section by remember { mutableStateOf("generator") }
    var status by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var number by remember { mutableStateOf("") }
    var signatory by remember { mutableStateOf("") }
    var standardId by remember { mutableStateOf("training") }
    var standardMenu by remember { mutableStateOf(false) }
    var importKind by remember { mutableStateOf("Certyfikat") }
    var importMenu by remember { mutableStateOf(false) }
    var resourceName by remember { mutableStateOf("") }
    var resourceOwner by remember { mutableStateOf("Biblioteka użytkownika") }
    var editAsset by remember { mutableStateOf<BrandAsset?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editOwner by remember { mutableStateOf("") }
    var editRights by remember { mutableStateOf("") }
    var editVersion by remember { mutableStateOf("1") }
    var deleteAsset by remember { mutableStateOf<BrandAsset?>(null) }
    var exportAsset by remember { mutableStateOf<BrandAsset?>(null) }
    var signatureId by remember { mutableStateOf("") }
    var stampId by remember { mutableStateOf("") }
    var signatureMenu by remember { mutableStateOf(false) }
    var stampMenu by remember { mutableStateOf(false) }

    var signatureText by remember { mutableStateOf("") }
    var signatureColor by remember { mutableStateOf("#183A63") }
    var stampOrganization by remember { mutableStateOf("") }
    var stampCenter by remember { mutableStateOf("PIECZĘĆ") }
    var stampNumber by remember { mutableStateOf("") }
    var stampColor by remember { mutableStateOf("#244B75") }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching {
            DocumentBrandStore.import(context, uri, resourceOwner.trim().ifBlank { "Biblioteka użytkownika" },
                importKind, resourceName.trim().ifBlank { uri.lastPathSegment?.substringAfterLast('/') ?: importKind })
            assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
            status = "Dodano zasób do biblioteki."
            resourceName = ""
        }.onFailure { status = "Import nie powiódł się: ${it.message ?: "błąd"}" }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        val asset = exportAsset
        if (uri != null && asset != null) runCatching {
            val bytes = DocumentBrandStore.fileBytes(context, asset.id, asset)
                ?: error("Nie można odczytać pliku zasobu.")
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Nie można zapisać wybranego pliku.")
            status = "Wyeksportowano: ${asset.label}"
        }.onFailure { status = "Eksport nie powiódł się: ${it.message ?: "błąd"}" }
        exportAsset = null
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) runCatching {
            val profile = certificateProfiles.first { it.id == standardId }
            renderCertificatePdf(context, uri, profile, issuer, recipient, course, scope,
                hours, issueDate, number, signatory, signatureId, stampId)
            val pdfBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Nie można odczytać wygenerowanego PDF.")
            DocumentBrandStore.importBytes(context, issuer.ifBlank { "Wystawca" }, "Certyfikat",
                "Certyfikat — ${recipient.take(48)} — ${course.take(55)}", pdfBytes,
                rights = "Wygenerowany lokalnie dokument PDF; zatwierdzenie i podpis należą do wystawcy.",
                mimeType = "application/pdf")
            assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
        }.onSuccess { status = "Wygenerowano PDF i zapisano kopię w bibliotece." }
            .onFailure { status = "Nie udało się wygenerować PDF: ${it.message ?: "błąd"}" }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Certyfikaty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Pracownia certyfikatów, baza wzorów, podpisów graficznych i pieczęci.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("generator" to "Generator", "resources" to "Baza", "signature" to "TXT → SVG", "stamp" to "Pieczęć", "x509" to "X.509")
                .forEachIndexed { index, item ->
                    SegmentedButton(section == item.first, { section = item.first },
                        SegmentedButtonDefaults.itemShape(index, 4)) { Text(item.second) }
                }
        }
        when (section) {
            "generator" -> {
                Text("Generator certyfikatu PDF", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { standardMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        val p = certificateProfiles.first { it.id == standardId }
                        Text("${p.title} · ${p.reference}")
                    }
                    DropdownMenu(standardMenu, { standardMenu = false }) {
                        certificateProfiles.forEach { p ->
                            DropdownMenuItem(text = { Text("${p.title} — ${p.reference}") },
                                onClick = { standardId = p.id; standardMenu = false })
                        }
                    }
                }
                val profile = certificateProfiles.first { it.id == standardId }
                Text(profile.purpose, style = MaterialTheme.typography.bodySmall)
                Text(profile.limitation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                OutlinedTextField(issuer, { issuer = it }, label = { Text("Wystawca / organizacja") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(recipient, { recipient = it }, label = { Text("Imię i nazwisko odbiorcy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(course, { course = it }, label = { Text("Nazwa szkolenia / potwierdzenia") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(scope, { scope = it }, label = { Text("Program / efekty uczenia się") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(hours, { hours = it }, label = { Text("Liczba godzin (opcjonalnie)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(issueDate, { issueDate = it }, label = { Text("Data wydania (RRRR-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(number, { number = it }, label = { Text("Numer / identyfikator certyfikatu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(signatory, { signatory = it }, label = { Text("Osoba upoważniona do podpisu") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { signatureMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Podpis graficzny: " + (assets.firstOrNull { it.id == signatureId }?.label ?: "brak"))
                    }
                    DropdownMenu(signatureMenu, { signatureMenu = false }) {
                        DropdownMenuItem(text = { Text("Bez podpisu graficznego") }, onClick = { signatureId = ""; signatureMenu = false })
                        assets.filter { it.kind in setOf("Wzór podpisu odręcznego", "Podpis wektorowy (tekst)") }.forEach { a ->
                            DropdownMenuItem(text = { Text(a.label) }, onClick = { signatureId = a.id; signatureMenu = false })
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { stampMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pieczęć graficzna: " + (assets.firstOrNull { it.id == stampId }?.label ?: "brak"))
                    }
                    DropdownMenu(stampMenu, { stampMenu = false }) {
                        DropdownMenuItem(text = { Text("Bez pieczęci") }, onClick = { stampId = ""; stampMenu = false })
                        assets.filter { it.kind in setOf("Pieczęć", "Pieczęć graficzna (własna)") }.forEach { a ->
                            DropdownMenuItem(text = { Text(a.label) }, onClick = { stampId = a.id; stampMenu = false })
                        }
                    }
                }
                Button(onClick = {
                    val parsedDate = runCatching { LocalDate.parse(issueDate) }.getOrNull()
                    if (parsedDate == null) status = "Data musi mieć format RRRR-MM-DD."
                    else pdfPicker.launch("certyfikat-${number.ifBlank { "wzor" }}.pdf")
                }, enabled = issuer.isNotBlank() && recipient.isNotBlank() && course.isNotBlank() &&
                    runCatching { LocalDate.parse(issueDate) }.isSuccess,
                    modifier = Modifier.fillMaxWidth()) { Text("Generuj certyfikat PDF") }
                OutlinedButton(onClick = onOpenDigitalCertificates, modifier = Modifier.fillMaxWidth()) {
                    Text("Certyfikaty cyfrowe i podpisy elektroniczne")
                }
            }
            "x509" -> X509CertificatePanel()
            "resources" -> {
                Text("Baza zasobów", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Zasoby przechowywane są w istniejącym szyfrowanym magazynie aplikacji. Dołączaj własne wzory PDF i grafiki SVG, PNG, JPEG lub WebP.",
                    style = MaterialTheme.typography.bodySmall)
                Box {
                    OutlinedButton(onClick = { importMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("Rodzaj zasobu: $importKind") }
                    DropdownMenu(importMenu, { importMenu = false }) {
                        certificateAssetKinds.forEach { kind ->
                            DropdownMenuItem(text = { Text(kind) }, onClick = { importKind = kind; importMenu = false })
                        }
                    }
                }
                OutlinedTextField(resourceName, { resourceName = it }, label = { Text("Nazwa zasobu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(resourceOwner, { resourceOwner = it }, label = { Text("Właściciel / organizacja") }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { importPicker.launch(arrayOf("image/svg+xml", "image/png", "image/jpeg", "image/webp", "application/pdf", "application/pkix-cert", "application/x-x509-ca-cert", "application/x-pem-file", "application/xml", "text/xml", "text/plain", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth()) { Text("Dodaj zasób z pliku") }
                assets.forEach { asset ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            val preview = remember(asset.id) { DocumentBrandStore.preview(context, asset.id, asset) }
                            if (preview != null) Image(preview.asImageBitmap(), contentDescription = asset.label, modifier = Modifier.size(52.dp))
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(asset.label, fontWeight = FontWeight.SemiBold)
                                Text("${asset.kind} · ${asset.owner} · v${asset.version}", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = {
                                exportAsset = asset
                                val baseName = asset.label.lowercase()
                                    .replace(Regex("[^a-z0-9ąćęłńóśźż_-]+"), "-")
                                    .trim('-').take(48).ifBlank { "zasob" }
                                val extension = when {
                                    asset.mimeType == "application/pdf" -> ".pdf"
                                    asset.file.contains(".svg") || asset.mimeType == "image/svg+xml" -> ".svg"
                                    asset.mimeType == "image/png" -> ".png"
                                    asset.mimeType == "image/jpeg" -> ".jpg"
                                    asset.mimeType == "image/webp" -> ".webp"
                                    asset.mimeType == "application/pkix-cert" || asset.mimeType == "application/x-x509-ca-cert" -> ".cer"
                                    asset.mimeType == "application/x-pem-file" || asset.mimeType == "text/plain" && asset.kind == "Certyfikat" -> ".pem"
                                    asset.mimeType == "application/xml" || asset.mimeType == "text/xml" -> ".xml"
                                    asset.kind == "Certyfikat" -> ".cer"
                                    else -> ".bin"
                                }
                                exportPicker.launch(baseName + extension)
                            }) { Text("Eksportuj") }
                            TextButton(onClick = {
                                editAsset = asset; editLabel = asset.label; editOwner = asset.owner
                                editRights = asset.rights; editVersion = asset.version.toString()
                            }, enabled = !asset.file.startsWith("builtin:") && !asset.file.startsWith("resource:")) { Text("Edytuj") }
                            TextButton(onClick = { deleteAsset = asset },
                                enabled = !asset.file.startsWith("builtin:") && !asset.file.startsWith("resource:")) { Text("Usuń") }
                        }
                    }
                }
            }
            "signature" -> {
                Text("Generator tekstu do wektorowego SVG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(signatureText, { signatureText = it }, label = { Text("Imię, nazwisko lub tekst") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(signatureColor, { signatureColor = it }, label = { Text("Kolor #RRGGBB") }, modifier = Modifier.fillMaxWidth())
                Text("To wektorowa grafika tekstowa. Nie jest odręcznym podpisem ani podpisem elektronicznym.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { runCatching {
                    val svg = makeTextSignatureSvg(signatureText, signatureColor)
                    DocumentBrandStore.importBytes(context, "Podpisy własne", "Podpis wektorowy (tekst)",
                        "Podpis — ${signatureText.take(45)}", svg,
                        rights = "Autorska grafika tekstowa; nie jest podpisem elektronicznym.")
                    assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
                    status = "Zapisano grafikę SVG podpisu."
                }.onFailure { status = it.message ?: "Nie udało się wygenerować podpisu." } },
                    enabled = signatureText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Generuj i zapisz SVG") }
            }
            "stamp" -> {
                Text("Generator własnej pieczęci graficznej", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(stampOrganization, { stampOrganization = it }, label = { Text("Nazwa organizacji") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(stampCenter, { stampCenter = it }, label = { Text("Tekst środkowy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(stampNumber, { stampNumber = it }, label = { Text("Numer / identyfikator") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(stampColor, { stampColor = it }, label = { Text("Kolor #RRGGBB") }, modifier = Modifier.fillMaxWidth())
                Text("Generator tworzy własny wzór graficzny SVG, nie odtwarza pieczęci urzędowych, państwowych ani notarialnych.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { runCatching {
                    val svg = makeStampSvg(stampOrganization, stampCenter, stampNumber, stampColor)
                    DocumentBrandStore.importBytes(context, "Pieczęcie własne", "Pieczęć graficzna (własna)",
                        "Pieczęć — ${stampOrganization.take(45)}", svg,
                        rights = "Własny wzór graficzny użytkownika; nie jest pieczęcią urzędową ani poświadczeniem.")
                    assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
                    status = "Zapisano własną pieczęć SVG."
                }.onFailure { status = it.message ?: "Nie udało się wygenerować pieczęci." } },
                    enabled = stampOrganization.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Generuj i zapisz SVG") }
            }
        }
        if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }

    editAsset?.let { asset ->
        AlertDialog(onDismissRequest = { editAsset = null }, title = { Text("Edycja zasobu") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(editLabel, { editLabel = it }, label = { Text("Nazwa") })
                OutlinedTextField(editOwner, { editOwner = it }, label = { Text("Właściciel / organizacja") })
                OutlinedTextField(editVersion, { editVersion = it }, label = { Text("Wersja") })
                OutlinedTextField(editRights, { editRights = it }, label = { Text("Źródło / prawa") }, minLines = 2)
            } },
            confirmButton = { TextButton(onClick = { runCatching {
                val bytes = DocumentBrandStore.fileBytes(context, asset.id, asset) ?: error("Nie można odczytać zasobu.")
                DocumentBrandStore.importBytes(context, editOwner, asset.kind, editLabel, bytes,
                    rights = editRights, version = editVersion.toIntOrNull()?.coerceAtLeast(1) ?: asset.version,
                    createdAt = asset.createdAt, mimeType = asset.mimeType, assetId = asset.id)
                assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
                status = "Zaktualizowano zasób."; editAsset = null
            }.onFailure { status = "Błąd edycji: ${it.message ?: "błąd"}" } }) { Text("Zapisz") } },
            dismissButton = { TextButton(onClick = { editAsset = null }) { Text("Anuluj") } })
    }
    deleteAsset?.let { asset ->
        AlertDialog(onDismissRequest = { deleteAsset = null }, title = { Text("Usunąć zasób?") },
            text = { Text("Usunięcie jest trwałe. Zasób może być używany w innych dokumentach.") },
            confirmButton = { TextButton(onClick = {
                DocumentBrandStore.delete(context, asset.id)
                assets = DocumentBrandStore.list(context).filter { it.kind in certificateAssetKinds }
                if (signatureId == asset.id) signatureId = ""
                if (stampId == asset.id) stampId = ""
                deleteAsset = null; status = "Usunięto zasób."
            }) { Text("Usuń") } },
            dismissButton = { TextButton(onClick = { deleteAsset = null }) { Text("Anuluj") } })
    }
}

private fun renderCertificatePdf(
    context: Context, uri: Uri, profile: CertificateStandardProfile,
    issuer: String, recipient: String, course: String, scope: String,
    hours: String, issueDate: String, number: String, signatory: String,
    signatureId: String, stampId: String
) {
    val pdf = PdfDocument()
    try {
        val width = 842
        val height = 595
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
        val canvas = page.canvas
        val navy = android.graphics.Color.rgb(24, 53, 83)
        val gold = android.graphics.Color.rgb(174, 137, 65)
        val ink = android.graphics.Color.rgb(38, 48, 58)
        val muted = android.graphics.Color.rgb(92, 104, 116)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawRect(24f, 24f, width - 24f, height - 24f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.STROKE; strokeWidth = 3f })
        canvas.drawRect(34f, 34f, width - 34f, height - 34f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; style = Paint.Style.STROKE; strokeWidth = 0.8f })
        fun centered(text: String, y: Float, size: Float, color: Int, bold: Boolean = false) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; textSize = size; textAlign = Paint.Align.CENTER
                typeface = Typeface.create(if (bold) "serif" else "sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            }
            canvas.drawText(text.take(130), width / 2f, y, paint)
        }
        centered(issuer.ifBlank { "ORGANIZACJA" }.uppercase(), 82f, 18f, navy, true)
        centered("CERTYFIKAT", 155f, 34f, navy, true)
        centered("potwierdzenie udziału / ukończenia szkolenia", 184f, 14f, muted)
        centered("niniejszym potwierdza się, że", 232f, 15f, ink)
        centered(recipient, 278f, 28f, navy, true)
        canvas.drawLine(180f, 290f, width - 180f, 290f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; strokeWidth = 1.2f })
        centered("ukończył(a) szkolenie:", 323f, 15f, ink)
        centered(course, 358f, 22f, navy, true)
        if (scope.isNotBlank()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 11f; textAlign = Paint.Align.CENTER }
            var rest = scope.replace('\n', ' ')
            val lines = mutableListOf<String>()
            repeat(3) {
                if (rest.isNotBlank()) {
                    val count = paint.breakText(rest, true, (width - 150f), null).coerceAtLeast(1)
                    lines += rest.substring(0, count).trim()
                    rest = rest.substring(count).trim()
                }
            }
            lines.forEachIndexed { index, line -> canvas.drawText(line, width / 2f, 382f + index * 14f, paint) }
        }
        val meta = listOfNotNull(hours.takeIf { it.isNotBlank() }?.let { "Liczba godzin: $it" },
            "Data wydania: $issueDate", number.takeIf { it.isNotBlank() }?.let { "Nr: $it" }).joinToString("   •   ")
        centered(meta, 430f, 10f, muted)
        centered("Odniesienie: ${profile.reference}", 452f, 9f, muted)
        centered(profile.limitation, 468f, 7.5f, muted)
        val signaturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 10f }
        canvas.drawLine(90f, 515f, 310f, 515f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; strokeWidth = 0.8f })
        canvas.drawText("Podpis osoby upoważnionej", 90f, 532f, signaturePaint)
        canvas.drawText(signatory.take(70), 90f, 548f, signaturePaint)
        canvas.drawText("Dokument do zatwierdzenia przez wystawcę", width - 350f, 565f, signaturePaint)
        signatureId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?.let { drawCertificateImage(canvas, it, Rect(115, 465, 285, 510)) }
        stampId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?.let { drawCertificateImage(canvas, it, Rect(width - 245, 435, width - 95, 555)) }
        pdf.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("Nie można zapisać PDF.")
    } finally { pdf.close() }
}

private fun drawCertificateImage(canvas: android.graphics.Canvas, bitmap: Bitmap, target: Rect) {
    val scale = minOf(target.width().toFloat() / bitmap.width.coerceAtLeast(1),
        target.height().toFloat() / bitmap.height.coerceAtLeast(1))
    val width = (bitmap.width * scale).toInt()
    val height = (bitmap.height * scale).toInt()
    val dest = Rect(target.centerX() - width / 2, target.centerY() - height / 2,
        target.centerX() + width / 2, target.centerY() + height / 2)
    canvas.drawBitmap(bitmap, null, dest, Paint(Paint.ANTI_ALIAS_FLAG))
}
