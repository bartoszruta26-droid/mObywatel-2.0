// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
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

private data class CertificateStandardProfile(
    val id: String, val title: String, val reference: String,
    val purpose: String, val caution: String
)

private val certificateStandardProfiles = listOf(
    CertificateStandardProfile("training", "Certyfikat ukończenia szkolenia", "Wzór własny",
        "Potwierdzenie udziału lub ukończenia kursu, szkolenia albo warsztatu.",
        "Wzór dokumentu szkoleniowego. Nie jest certyfikatem akredytacji ani certyfikacją kompetencji."),
    CertificateStandardProfile("iso9001", "System zarządzania jakością", "ISO 9001:2026",
        "Odniesienie do systemu zarządzania jakością organizacji.",
        "Wzór szkoleniowy / informacyjny. Nie potwierdza certyfikacji systemu zarządzania jakością."),
    CertificateStandardProfile("iso21001", "Organizacja edukacyjna", "ISO 21001:2025",
        "Odniesienie do systemu zarządzania organizacją edukacyjną.",
        "Wzór szkoleniowy / informacyjny. Nie potwierdza zgodności ani certyfikacji organizacji edukacyjnej."),
    CertificateStandardProfile("iso17024", "Certyfikacja osób", "ISO/IEC 17024:2026",
        "Odniesienie do wymagań dla jednostek certyfikujących osoby i ich programów certyfikacji.",
        "Samo szkolenie nie stanowi certyfikacji osoby wg ISO/IEC 17024. Wymagany jest właściwy program i jednostka certyfikująca."),
    CertificateStandardProfile("iso7101", "Jakość w ochronie zdrowia", "ISO 7101:2023",
        "Odniesienie do systemu zarządzania jakością w organizacji ochrony zdrowia.",
        "Wzór szkoleniowy / informacyjny. Nie potwierdza certyfikacji placówki ani systemu."),
    CertificateStandardProfile("iso13485", "Wyroby medyczne", "ISO 13485:2016",
        "Odniesienie do systemu zarządzania jakością dla wyrobów medycznych.",
        "Wzór szkoleniowy / informacyjny. Nie potwierdza certyfikacji producenta ani wyrobu medycznego.")
)

private fun certificateXmlText(value: String): String = value
    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    .replace("\"", "&quot;").replace("'", "&apos;")

internal fun makeTextSignatureSvg(name: String, color: String): ByteArray {
    val safe = certificateXmlText(name.trim().take(90))
    return """<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="260" viewBox="0 0 1000 260">
<rect width="1000" height="260" fill="none"/>
<text x="500" y="155" text-anchor="middle" font-family="cursive" font-size="108" font-style="italic" fill="$color" stroke="$color" stroke-width="1.2">$safe</text>
<path d="M150 195 Q390 235 650 190 T900 188" fill="none" stroke="$color" stroke-width="3" stroke-linecap="round"/>
</svg>""".toByteArray(Charsets.UTF_8)
}

internal fun makeStampSvg(organization: String, centerText: String, identifier: String, color: String): ByteArray {
    val org = certificateXmlText(organization.trim().take(70))
    val center = certificateXmlText(centerText.trim().take(50))
    val number = certificateXmlText(identifier.trim().take(40))
    return """<svg xmlns="http://www.w3.org/2000/svg" width="640" height="640" viewBox="0 0 640 640">
<rect width="640" height="640" fill="none"/>
<circle cx="320" cy="320" r="278" fill="none" stroke="$color" stroke-width="12"/>
<circle cx="320" cy="320" r="245" fill="none" stroke="$color" stroke-width="4"/>
<circle cx="320" cy="320" r="190" fill="none" stroke="$color" stroke-width="2" stroke-dasharray="8 9"/>
<text x="320" y="205" text-anchor="middle" font-family="serif" font-size="32" font-weight="bold" fill="$color">$org</text>
<text x="320" y="325" text-anchor="middle" font-family="serif" font-size="42" font-weight="bold" fill="$color">$center</text>
<text x="320" y="405" text-anchor="middle" font-family="serif" font-size="25" fill="$color">$number</text>
<text x="320" y="505" text-anchor="middle" font-family="sans-serif" font-size="18" fill="$color">WZÓR GRAFICZNY • NIE PIECZĘĆ URZĘDOWA</text>
</svg>""".toByteArray(Charsets.UTF_8)
}

private val certificateResourceKinds = setOf(
    "Certyfikat", "Wzór podpisu odręcznego", "Podpis wektorowy (tekst)",
    "Pieczęć", "Pieczęć graficzna (własna)"
)

@Composable
internal fun CertificateStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var assets by remember { mutableStateOf(DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }) }
    var section by remember { mutableStateOf("generator") }
    var standardId by remember { mutableStateOf("training") }
    var standardMenu by remember { mutableStateOf(false) }
    var importKind by remember { mutableStateOf("Certyfikat") }
    var importKindMenu by remember { mutableStateOf(false) }
    var resourceLabel by remember { mutableStateOf("") }
    var resourceOwner by remember { mutableStateOf("Biblioteka użytkownika") }
    var status by remember { mutableStateOf("") }
    var editAsset by remember { mutableStateOf<BrandAsset?>(null) }
    var deleteAsset by remember { mutableStateOf<BrandAsset?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editOwner by remember { mutableStateOf("") }
    var editRights by remember { mutableStateOf("") }
    var editVersion by remember { mutableStateOf("1") }

    var issuer by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var number by remember { mutableStateOf("") }
    var signatory by remember { mutableStateOf("") }
    var signatureId by remember { mutableStateOf("") }
    var stampId by remember { mutableStateOf("") }
    var signatureMenu by remember { mutableStateOf(false) }
    var stampMenu by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching {
            val label = resourceLabel.trim().ifBlank { uri.lastPathSegment?.substringAfterLast('/') ?: importKind }
            DocumentBrandStore.import(context, uri, resourceOwner.trim().ifBlank { "Biblioteka użytkownika" }, importKind, label)
            assets = DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }
            status = "Dodano zasób: $label"
            resourceLabel = ""
        }.onFailure { status = "Nie udało się dodać zasobu: ${it.message ?: "błąd"}" }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) runCatching {
            renderTrainingCertificatePdf(context, uri, certificateStandardProfiles.first { it.id == standardId },
                issuer, recipient, course, scope, hours, issueDate, number, signatory, signatureId, stampId)
        }.onSuccess { status = "Wygenerowano certyfikat PDF." }
            .onFailure { status = "Nie udało się wygenerować PDF: ${it.message ?: "błąd"}" }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Certyfikaty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Biblioteka wzorów, podpisów graficznych i pieczęci oraz generatory dokumentów.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("generator" to "Generator", "resources" to "Zasoby", "signature" to "TXT → SVG", "stamp" to "Pieczęcie")
                .forEachIndexed { index, item ->
                    SegmentedButton(section == item.first, { section = item.first },
                        SegmentedButtonDefaults.itemShape(index, 4)) { Text(item.second) }
                }
        }
        when (section) {
            "generator" -> {
                Text("Generator certyfikatów", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { standardMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        val selected = certificateStandardProfiles.first { it.id == standardId }
                        Text("${selected.title} · ${selected.reference}")
                    }
                    DropdownMenu(standardMenu, { standardMenu = false }) {
                        certificateStandardProfiles.forEach { profile ->
                            DropdownMenuItem(text = { Text("${profile.title} — ${profile.reference}") },
                                onClick = { standardId = profile.id; standardMenu = false })
                        }
                    }
                }
                val profile = certificateStandardProfiles.first { it.id == standardId }
                Text(profile.purpose, style = MaterialTheme.typography.bodySmall)
                Text(profile.caution, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                OutlinedTextField(issuer, { issuer = it }, label = { Text("Wystawca / organizacja") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(recipient, { recipient = it }, label = { Text("Imię i nazwisko uczestnika / odbiorcy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(course, { course = it }, label = { Text("Nazwa szkolenia / zakres potwierdzenia") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(scope, { scope = it }, label = { Text("Program / uzyskane efekty uczenia się") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(hours, { hours = it }, label = { Text("Liczba godzin (opcjonalnie)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(issueDate, { issueDate = it }, label = { Text("Data wydania (RRRR-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(number, { number = it }, label = { Text("Numer certyfikatu / identyfikator") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(signatory, { signatory = it }, label = { Text("Osoba upoważniona do podpisu — imię i nazwisko") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { signatureMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Podpis graficzny: " + (assets.firstOrNull { it.id == signatureId }?.label ?: "bez grafiki"))
                    }
                    DropdownMenu(signatureMenu, { signatureMenu = false }) {
                        DropdownMenuItem(text = { Text("Bez grafiki podpisu") }, onClick = { signatureId = ""; signatureMenu = false })
                        assets.filter { it.kind in setOf("Wzór podpisu odręcznego", "Podpis wektorowy (tekst)") }.forEach { asset ->
                            DropdownMenuItem(text = { Text(asset.label) }, onClick = { signatureId = asset.id; signatureMenu = false })
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { stampMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pieczęć graficzna: " + (assets.firstOrNull { it.id == stampId }?.label ?: "bez grafiki"))
                    }
                    DropdownMenu(stampMenu, { stampMenu = false }) {
                        DropdownMenuItem(text = { Text("Bez pieczęci") }, onClick = { stampId = ""; stampMenu = false })
                        assets.filter { it.kind in setOf("Pieczęć", "Pieczęć graficzna (własna)") }.forEach { asset ->
                            DropdownMenuItem(text = { Text(asset.label) }, onClick = { stampId = asset.id; stampMenu = false })
                        }
                    }
                }
                Button(onClick = { pdfLauncher.launch("certyfikat-${number.ifBlank { "wzor" }}.pdf") },
                    enabled = issuer.isNotBlank() && recipient.isNotBlank() && course.isNotBlank() && issueDate.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()) { Text("Generuj certyfikat PDF") }
            }
            "resources" -> {
                Text("Biblioteka zasobów", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Zasoby użytkownika są przechowywane lokalnie i szyfrowane. Możesz dodawać gotowe pliki SVG, PNG, JPEG lub WebP.",
                    style = MaterialTheme.typography.bodySmall)
                Box {
                    OutlinedButton(onClick = { importKindMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("Typ zasobu: $importKind") }
                    DropdownMenu(importKindMenu, { importKindMenu = false }) {
                        certificateResourceKinds.forEach { kind ->
                            DropdownMenuItem(text = { Text(kind) }, onClick = { importKind = kind; importKindMenu = false })
                        }
                    }
                }
                OutlinedTextField(resourceLabel, { resourceLabel = it }, label = { Text("Nazwa zasobu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(resourceOwner, { resourceOwner = it }, label = { Text("Właściciel / organizacja") }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { picker.launch(arrayOf("image/svg+xml", "image/png", "image/jpeg", "image/webp", "application/pdf")) },
                    modifier = Modifier.fillMaxWidth()) { Text("Dodaj zasób z pliku") }
                assets.forEach { asset ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            val bitmap = remember(asset.id) { DocumentBrandStore.preview(context, asset.id, asset) }
                            if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = asset.label, modifier = Modifier.size(54.dp))
                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(asset.label, fontWeight = FontWeight.SemiBold)
                                Text("${asset.kind} · ${asset.owner} · v${asset.version}", style = MaterialTheme.typography.bodySmall)
                            }
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
                Text("Generator podpisu wektorowego z tekstu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                var signatureText by remember { mutableStateOf("") }
                var signatureColor by remember { mutableStateOf("#183A63") }
                OutlinedTextField(signatureText, { signatureText = it }, label = { Text("Tekst podpisu / imię i nazwisko") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(signatureColor, { signatureColor = it }, label = { Text("Kolor SVG (np. #183A63)") }, modifier = Modifier.fillMaxWidth())
                Text("Wynik to SVG z wektorowym tekstem, a nie odręczny podpis ani podpis elektroniczny. Krój pisma zależy od fontów programu wyświetlającego.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = {
                    runCatching {
                        require(signatureText.isNotBlank()) { "Wpisz tekst podpisu." }
                        require(Regex("#[0-9A-Fa-f]{6}").matches(signatureColor)) { "Podaj kolor w formacie #RRGGBB." }
                        DocumentBrandStore.importBytes(context, "Podpisy własne", "Podpis wektorowy (tekst)",
                            "Podpis — ${signatureText.take(45)}", makeTextSignatureSvg(signatureText, signatureColor),
                            rights = "Autorska grafika tekstowa wygenerowana lokalnie; nie jest podpisem elektronicznym ani wzorem odręcznego podpisu.")
                        assets = DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }
                        status = "Zapisano podpis tekstowy jako SVG."
                    }.onFailure { status = it.message ?: "Nie udało się wygenerować SVG." }
                }, modifier = Modifier.fillMaxWidth()) { Text("Utwórz podpis SVG i zapisz w bibliotece") }
            }
            "stamp" -> {
                Text("Generator pieczęci graficznej", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                var organization by remember { mutableStateOf("") }
                var centerText by remember { mutableStateOf("PIECZĘĆ") }
                var identifier by remember { mutableStateOf("") }
                var color by remember { mutableStateOf("#244B75") }
                OutlinedTextField(organization, { organization = it }, label = { Text("Nazwa organizacji / podmiotu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(centerText, { centerText = it }, label = { Text("Tekst środkowy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(identifier, { identifier = it }, label = { Text("Numer / identyfikator (opcjonalnie)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(color, { color = it }, label = { Text("Kolor SVG (np. #244B75)") }, modifier = Modifier.fillMaxWidth())
                Text("Generator tworzy autorską pieczęć graficzną organizacji. Nie odtwarza pieczęci państwowych, urzędowych, notarialnych ani cudzych znaków.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = {
                    runCatching {
                        require(organization.isNotBlank()) { "Wpisz nazwę organizacji." }
                        require(Regex("#[0-9A-Fa-f]{6}").matches(color)) { "Podaj kolor w formacie #RRGGBB." }
                        DocumentBrandStore.importBytes(context, "Pieczęcie własne", "Pieczęć graficzna (własna)",
                            "Pieczęć — ${organization.take(45)}", makeStampSvg(organization, centerText, identifier, color),
                            rights = "Autorski wzór graficzny użytkownika; nie jest pieczęcią urzędową ani poświadczeniem.")
                        assets = DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }
                        status = "Zapisano pieczęć graficzną jako SVG."
                    }.onFailure { status = it.message ?: "Nie udało się wygenerować pieczęci." }
                }, modifier = Modifier.fillMaxWidth()) { Text("Utwórz pieczęć SVG i zapisz w bibliotece") }
            }
        }
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
    }

    editAsset?.let { asset ->
        AlertDialog(onDismissRequest = { editAsset = null }, title = { Text("Edycja zasobu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(editLabel, { editLabel = it }, label = { Text("Nazwa") })
                    OutlinedTextField(editOwner, { editOwner = it }, label = { Text("Właściciel / organizacja") })
                    OutlinedTextField(editVersion, { editVersion = it }, label = { Text("Wersja") })
                    OutlinedTextField(editRights, { editRights = it }, label = { Text("Opis praw / źródła") }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        val bytes = DocumentBrandStore.fileBytes(context, asset.id, asset) ?: error("Nie można odczytać pliku zasobu.")
                        DocumentBrandStore.importBytes(context, editOwner, asset.kind, editLabel, bytes,
                            rights = editRights, version = editVersion.toIntOrNull()?.coerceAtLeast(1) ?: asset.version,
                            createdAt = asset.createdAt, mimeType = asset.mimeType, assetId = asset.id)
                    }.onSuccess {
                        assets = DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }
                        status = "Zaktualizowano zasób."; editAsset = null
                    }.onFailure { status = "Nie udało się zapisać zmian: ${it.message}" }
                }) { Text("Zapisz") }
            },
            dismissButton = { TextButton(onClick = { editAsset = null }) { Text("Anuluj") } })
    }
    deleteAsset?.let { asset ->
        AlertDialog(onDismissRequest = { deleteAsset = null }, title = { Text("Usunąć zasób?") },
            text = { Text("Usunięcie jest trwałe. Zasób może być używany w innych dokumentach.") },
            confirmButton = {
                TextButton(onClick = {
                    DocumentBrandStore.delete(context, asset.id)
                    assets = DocumentBrandStore.list(context).filter { it.kind in certificateResourceKinds }
                    deleteAsset = null; status = "Usunięto zasób."
                }) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { deleteAsset = null }) { Text("Anuluj") } })
    }
}

private fun renderTrainingCertificatePdf(
    context: Context, uri: Uri, profile: CertificateStandardProfile,
    issuer: String, recipient: String, course: String, scope: String,
    hours: String, issueDate: String, number: String, signatory: String,
    signatureId: String, stampId: String
) {
    val pdf = PdfDocument()
    try {
        val width = 842; val height = 595
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
        centered("ukończenia szkolenia / potwierdzenie udziału", 184f, 14f, muted)
        centered("niniejszym potwierdza się, że", 232f, 15f, ink)
        centered(recipient, 278f, 28f, navy, true)
        canvas.drawLine(180f, 290f, width - 180f, 290f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; strokeWidth = 1.2f })
        centered("ukończył(a) szkolenie:", 323f, 15f, ink)
        centered(course, 358f, 22f, navy, true)
        if (scope.isNotBlank()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 11f; textAlign = Paint.Align.CENTER }
            val lines = mutableListOf<String>(); var rest = scope.replace('\n', ' ')
            while (rest.isNotBlank() && lines.size < 2) {
                val count = paint.breakText(rest, true, (width - 150f), null).coerceAtLeast(1)
                lines += rest.substring(0, count).trim(); rest = rest.substring(count).trim()
            }
            lines.forEachIndexed { index, line -> canvas.drawText(line, width / 2f, 384f + index * 15f, paint) }
        }
        val meta = listOfNotNull(hours.takeIf { it.isNotBlank() }?.let { "Liczba godzin: $it" },
            "Data wydania: $issueDate", number.takeIf { it.isNotBlank() }?.let { "Nr: $it" }).joinToString("   •   ")
        centered(meta, 428f, 10f, muted)
        centered("Odniesienie: ${profile.reference}", 450f, 9f, muted)
        centered(profile.caution, 466f, 7.5f, muted)
        val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 10f }
        canvas.drawLine(90f, 515f, 310f, 515f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; strokeWidth = 0.8f })
        canvas.drawText("Podpis osoby upoważnionej", 90f, 532f, signPaint)
        canvas.drawText(signatory.take(70), 90f, 548f, signPaint)
        canvas.drawText("Dokument wzorcowy — do zatwierdzenia przez wystawcę", width - 360f, 565f, signPaint)
        signatureId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?.let { drawCertificateBitmapFit(canvas, it, Rect(115, 465, 285, 510)) }
        stampId.takeIf { it.isNotBlank() }?.let { DocumentBrandStore.preview(context, it) }
            ?.let { drawCertificateBitmapFit(canvas, it, Rect(width - 245, 435, width - 95, 555)) }
        pdf.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("Nie można zapisać pliku PDF.")
    } finally { pdf.close() }
}

private fun drawCertificateBitmapFit(canvas: android.graphics.Canvas, bitmap: Bitmap, target: Rect) {
    val scale = minOf(target.width().toFloat() / bitmap.width.coerceAtLeast(1),
        target.height().toFloat() / bitmap.height.coerceAtLeast(1))
    val w = (bitmap.width * scale).toInt(); val h = (bitmap.height * scale).toInt()
    val dst = Rect(target.centerX() - w / 2, target.centerY() - h / 2,
        target.centerX() + w / 2, target.centerY() + h / 2)
    canvas.drawBitmap(bitmap, null, dst, Paint(Paint.ANTI_ALIAS_FLAG))
}
