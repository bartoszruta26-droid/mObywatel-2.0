// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.BitmapFactory
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.network.tor.DownloadConnectionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.GZIPInputStream
import pl.gov.obywatel.android.network.tor.TorDownloadKind
import pl.gov.obywatel.android.network.tor.TorDownloadServiceProvider

internal data class InstitutionalSymbolCandidate(
    val sourceId: String,
    val repository: String,
    val ref: String,
    val path: String,
    val group: String,
    val rightsStatus: String,
    val sourceUrl: String,
    val downloadUrl: String,
    val license: String,
    val licenseUrl: String,
    val author: String,
    val credit: String
) {
    val displayName: String get() = path.substringAfterLast('/').replace(Regex("(?i)\\.(svg|png)$"), "").replace('_', ' ').replace('-', ' ')
        .replace('_', ' ').replace('-', ' ')
}

internal fun parseInstitutionalSymbolCsv(raw: String): List<InstitutionalSymbolCandidate> {
    fun parseLine(line: String): List<String> {
        val cells = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"') {
                cell.append('"'); i++
            } else if (c == '"') quoted = !quoted
            else if (c == ',' && !quoted) { cells += cell.toString(); cell.setLength(0) }
            else cell.append(c)
            i++
        }
        require(!quoted) { "Nieprawidłowe cudzysłowy w katalogu symboli." }
        cells += cell.toString()
        return cells
    }
    val lines = raw.removePrefix("\uFEFF").lineSequence().filter { it.isNotBlank() && !it.startsWith("#") }.toList()
    require(lines.isNotEmpty()) { "Katalog symboli jest pusty." }
    val header = parseLine(lines.first()).map { it.trim() }
    fun index(name: String) = header.indexOf(name).also { require(it >= 0) { "Brak kolumny $name w katalogu symboli." } }
    val columns = listOf("source_id", "repository", "ref", "path", "group", "rights_status", "source_url", "download_url", "license_short_name", "license_url", "author", "source_credit").associateWith(::index)
    return lines.drop(1).mapNotNull { line ->
        val row = parseLine(line)
        fun value(name: String) = row.getOrNull(columns.getValue(name)).orEmpty()
        val path = value("path")
        val sourceUrl = value("source_url")
        val catalogUrl = value("download_url")
        // GitHub inventory entries carry stable blob URLs rather than raw download URLs.
        // Convert only the exact GitHub blob URL shape; never accept an arbitrary host here.
        val githubRawUrl = Regex("^https://github\\.com/([^/]+/[^/]+)/blob/([^/]+)/(.+)$")
            .matchEntire(sourceUrl)?.let { match ->
                val decodedPath = java.net.URLDecoder.decode(match.groupValues[3], "UTF-8")
                val encodedPath = decodedPath.split("/").joinToString("/") { segment ->
                    java.net.URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
                }
                "https://raw.githubusercontent.com/${match.groupValues[1]}/${match.groupValues[2]}/$encodedPath"
            }.orEmpty()
        val url = catalogUrl.ifBlank { githubRawUrl }
        if (institutionalSymbolFormat(path) == null || url.isBlank()) return@mapNotNull null
        InstitutionalSymbolCandidate(value("source_id"), value("repository"), value("ref"), path,
            value("group"), value("rights_status"), sourceUrl, url,
            value("license_short_name"), value("license_url"), value("author"), value("source_credit"))
    }.distinctBy { it.sourceId + ":" + it.path }
}


internal data class InstitutionalSymbolSourceCollection(
    val id: String, val label: String, val provider: String, val collectionRef: String,
    val sourcePageUrl: String, val discoveryApiUrl: String, val observedSize: String,
    val sourceFormats: String, val rightsPolicy: String, val rightsStatus: String,
    val discoveryMethod: String, val directFilePatternOrNotes: String, val checkedAt: String,
    val refreshIntervalDays: Int, val notes: String, val discoveryState: String,
    val verificationState: String, val downloadState: String, val storageState: String,
    val availabilityState: String
)

internal fun parseInstitutionalSymbolCollectionsCsv(raw: String): List<InstitutionalSymbolSourceCollection> {
    fun parseLine(line: String): List<String> {
        val cells = mutableListOf<String>(); val cell = StringBuilder(); var quoted = false; var index = 0
        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> { cell.append('"'); index++ }
                ch == '"' -> quoted = !quoted
                ch == ',' && !quoted -> { cells += cell.toString(); cell.setLength(0) }
                else -> cell.append(ch)
            }
            index++
        }
        require(!quoted) { "Nieprawidłowe cudzysłowy w rejestrze kolekcji." }
        cells += cell.toString()
        return cells
    }
    val lines = raw.removePrefix("\uFEFF").lineSequence().filter { it.isNotBlank() && !it.startsWith("#") }.toList()
    require(lines.size >= 2) { "Rejestr kolekcji jest pusty." }
    val header = parseLine(lines.first()).map { it.trim() }
    fun column(name: String) = header.indexOf(name).also { require(it >= 0) { "Brak kolumny $name w rejestrze kolekcji." } }
    val columns = listOf("collection_id","label","provider","collection_ref","source_page_url","discovery_api_url",
        "observed_size","source_formats","rights_policy","rights_status","discovery_method","direct_file_pattern_or_notes",
        "checked_at","refresh_interval_days","notes","discovery_state","verification_state","download_state",
        "storage_state","availability_state").associateWith(::column)
    return lines.drop(1).map { line ->
        val row = parseLine(line)
        fun value(key: String) = row.getOrNull(columns.getValue(key)).orEmpty()
        val sourceUrl = value("source_page_url"); val apiUrl = value("discovery_api_url")
        require(sourceUrl.startsWith("https://") && apiUrl.startsWith("https://")) { "Niebezpieczny lub pusty adres źródła." }
        InstitutionalSymbolSourceCollection(
            value("collection_id"),value("label"),value("provider"),value("collection_ref"),sourceUrl,apiUrl,
            value("observed_size"),value("source_formats"),value("rights_policy"),value("rights_status"),
            value("discovery_method"),value("direct_file_pattern_or_notes"),value("checked_at"),
            value("refresh_interval_days").toIntOrNull() ?: 0,value("notes"),value("discovery_state"),
            value("verification_state"),value("download_state"),value("storage_state"),value("availability_state")
        )
    }.distinctBy { it.id }
}

internal fun institutionalSymbolFormat(path: String): String? = when (path.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
    "svg" -> "SVG"
    "png" -> "PNG"
    else -> null
}

internal fun validateInstitutionalSymbolPng(bytes: ByteArray) {
    val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
    require(bytes.size in 33..(12 * 1024 * 1024)) { "PNG ma nieprawidłowy rozmiar." }
    require(bytes.copyOfRange(0, 8).contentEquals(signature)) { "Nieprawidłowy podpis PNG." }
    require(String(bytes, 12, 4, Charsets.US_ASCII) == "IHDR") { "Brak nagłówka IHDR w PNG." }
    fun readInt(offset: Int): Long = ((bytes[offset].toLong() and 0xff) shl 24) or ((bytes[offset + 1].toLong() and 0xff) shl 16) or ((bytes[offset + 2].toLong() and 0xff) shl 8) or (bytes[offset + 3].toLong() and 0xff)
    val width = readInt(16); val height = readInt(20)
    require(width in 1L..16384L && height in 1L..16384L) { "PNG ma nieprawidłowe lub zbyt duże wymiary." }
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    require(options.outWidth == width.toInt() && options.outHeight == height.toInt()) { "PNG nie przechodzi dekodowania nagłówka obrazu." }
}
private fun institutionalSymbolAssetId(item: InstitutionalSymbolCandidate): String =
    "symbol_" + MessageDigest.getInstance("SHA-256")
        .digest((item.sourceId + ":" + item.path).toByteArray(Charsets.UTF_8)).take(16)
        .joinToString("") { "%02x".format(it) }

internal fun institutionalSymbolCanDownload(item: InstitutionalSymbolCandidate): Boolean {
    val status = item.rightsStatus.trim().lowercase(Locale.ROOT)
    val license = item.license.trim().lowercase(Locale.ROOT)
    return status == "mit" && license == "mit" && item.licenseUrl.startsWith("https://") && !item.group.contains("flag", true)
}

private fun validateInstitutionalSymbolSvg(bytes: ByteArray) = SvgSafetyValidator.validate(bytes)

internal data class InstitutionalSymbolDownloadProgress(
    val stage: String,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long? = null
)

internal fun formatInstitutionalSymbolBytes(bytes: Long): String = when {
    bytes < 1024L -> "${bytes} B"
    bytes < 1024L * 1024L -> String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.ROOT, "%.2f MB", bytes / (1024.0 * 1024.0))
}

private suspend fun downloadInstitutionalSymbol(
    context: Context,
    item: InstitutionalSymbolCandidate,
    onProgress: suspend (InstitutionalSymbolDownloadProgress) -> Unit
): String {
    require(institutionalSymbolCanDownload(item)) {
        "Pobieranie zablokowane: licencja nie została zatwierdzona dla tego pliku. Status: ${item.rightsStatus}; licencja: ${item.license.ifBlank { "brak" }}."
    }
    val uri = java.net.URI(item.downloadUrl)
    val format = institutionalSymbolFormat(item.path)
    require(format != null) { "Obsługiwane są wyłącznie pliki SVG i PNG." }
    val trustedImageHost = uri.host.equals("upload.wikimedia.org", true) ||
        uri.host.equals("raw.githubusercontent.com", true) ||
        uri.host.equals("cdn.jsdelivr.net", true)
    require(uri.scheme.equals("https", true) && uri.userInfo == null && trustedImageHost) {
        "Źródło pliku nie znajduje się na dozwolonej domenie Wikimedia Commons, GitHub Raw lub jsDelivr."
    }
    onProgress(InstitutionalSymbolDownloadProgress("Łączenie ze źródłem przez wybraną trasę sieciową. Przy uruchamianiu Tor pierwsze połączenie może potrwać dłużej."))
    val response = TorDownloadServiceProvider.get(context).open(item.downloadUrl, if (format == "SVG") TorDownloadKind.SVG else TorDownloadKind.IMAGE)
    val bytes = response.use {
        require(it.statusCode in 200..299) { "Serwer zwrócił HTTP ${it.statusCode}." }
        val expected = it.contentLength?.takeIf { length -> length > 0L }
        onProgress(InstitutionalSymbolDownloadProgress("Połączono. Pobieranie danych $format…", 0L, expected))
        val output = ByteArrayOutputStream()
        it.body.use { input ->
            val buffer = ByteArray(8192)
            var total = 0L
            var lastUpdate = System.currentTimeMillis()
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= 12L * 1024L * 1024L) { "SVG przekracza limit 12 MB." }
                output.write(buffer, 0, count)
                val now = System.currentTimeMillis()
                if (now - lastUpdate >= 300L) {
                    onProgress(InstitutionalSymbolDownloadProgress("Pobieranie $format…", total, expected))
                    lastUpdate = now
                }
            }
            onProgress(InstitutionalSymbolDownloadProgress("Pobieranie zakończone. Odebrano ${formatInstitutionalSymbolBytes(total)}. Weryfikacja pliku…", total, expected ?: total))
        }
        output.toByteArray()
    }
    onProgress(InstitutionalSymbolDownloadProgress("Sprawdzanie bezpieczeństwa i poprawności $format…", bytes.size.toLong(), bytes.size.toLong()))
    when (format) {
        "SVG" -> validateInstitutionalSymbolSvg(bytes)
        "PNG" -> validateInstitutionalSymbolPng(bytes)
        else -> error("Nieobsługiwany format grafiki.")
    }
    val mimeType = if (format == "SVG") "image/svg+xml" else "image/png"
    val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    val rights = JSONObject()
        .put("source", item.repository)
        .put("sourceRef", item.ref)
        .put("sourcePath", item.path)
        .put("sourcePage", item.sourceUrl)
        .put("downloadUrl", item.downloadUrl)
        .put("license", item.license)
        .put("licenseUrl", item.licenseUrl)
        .put("author", item.author)
        .put("sourceCredit", item.credit)
        .put("catalogRightsStatus", item.rightsStatus)
        .put("sha256", sha)
        .put("discoveryStatus", "found")
        .put("verificationStatus", "file-format-and-content-validated; MIT-license-metadata-verified; app-use-review-pending")
        .put("verifiedAt", System.currentTimeMillis())
        .put("format", format)
        .put("downloadStatus", "downloaded")
        .put("storageStatus", "saved")
        .put("availabilityStatus", "not_available")
        .put("permittedKinds", JSONArray())
        .toString()
    val kind = when (institutionalSymbolCategory(item)) {
        "Flagi" -> "Flaga"
        "Herby" -> "Herb"
        "Godła" -> "Godło"
        "Symbole" -> "Symbol"
        "Logotypy" -> "Logo"
        else -> "Logo"
    }
    onProgress(InstitutionalSymbolDownloadProgress("Zapisuję plik i metadane w bibliotece…", bytes.size.toLong(), bytes.size.toLong()))
    DocumentBrandStore.importBytes(context, item.repository, kind, item.displayName.take(180),
        bytes, rights = rights, mimeType = mimeType, assetId = institutionalSymbolAssetId(item))
    onProgress(InstitutionalSymbolDownloadProgress("Zapisano i zweryfikowano $format. Dostępność w aplikacji nadal wymaga oceny praw użycia.", bytes.size.toLong(), bytes.size.toLong()))
    return sha
}

private val institutionalSymbolCategoryOrder = listOf("Symbole", "Herby", "Godła", "Flagi", "Logotypy", "Pozostałe")

internal fun institutionalSymbolCategory(item: InstitutionalSymbolCandidate): String {
    val key = "${item.group} ${item.path} ${item.displayName}".lowercase(Locale.ROOT)
    return when {
        listOf("flag", "flaga", "banner").any { key.contains(it) } -> "Flagi"
        listOf("coat of arms", "coat-of-arms", "coat_of_arms", "herald", "herb", "crest", "_arms", "arms_").any { key.contains(it) } -> "Herby"
        listOf("eagle", "godlo", "godło", "emblem").any { key.contains(it) } -> "Godła"
        listOf("insignia", "insigne", "badge", "seal", "pieczęć", "pieczec", "medal", "order", "symbol", "insygnia", "odznaka", "oznaka", "monogram").any { key.contains(it) } -> "Symbole"
        listOf("logo", "brand", "organization", "organisation", "ngo", "charit", "mark", "government", "public").any { key.contains(it) } -> "Logotypy"
        else -> "Pozostałe"
    }
}

/** Searchable source catalogue. All candidate groups are visible before a search is entered. */
@Composable
internal fun InstitutionalSymbolCatalogPanel(context: Context, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    val selectedMode = DownloadConnectionSettings.get(context)
    var candidates by remember { mutableStateOf<List<InstitutionalSymbolCandidate>>(emptyList()) }
    var sourceCollections by remember { mutableStateOf<List<InstitutionalSymbolSourceCollection>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    var busyId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableStateOf<InstitutionalSymbolDownloadProgress?>(null) }
    LaunchedEffect(context) {
        message = "Wczytywanie katalogów symboli…"
        val loaded = withContext(Dispatchers.IO) {
            val sources = context.assets.open("identity-symbols/collections.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val local = context.assets.open("identity-symbols/catalog.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val github = context.assets.open("identity-symbols/github-symbol-candidates.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
            Triple(sources, local, github)
        }
        sourceCollections = runCatching { parseInstitutionalSymbolCollectionsCsv(loaded.first) }.getOrElse {
            message = "Nie udało się wczytać rejestru kolekcji: ${it.message ?: "błąd danych"}"; emptyList()
        }
        candidates = runCatching { (parseInstitutionalSymbolCsv(loaded.second) + parseInstitutionalSymbolCsv(loaded.third)).distinctBy { it.sourceId + ":" + it.path } }.getOrElse {
            message = "Nie udało się wczytać katalogu SVG: ${it.message ?: "błąd danych"}"; emptyList()
        }
        if (sourceCollections.isNotEmpty()) message = "Rejestr kolekcji gotowy: ${sourceCollections.size} źródeł."
    }
    val normalized = query.trim().lowercase(Locale.ROOT)
    val matches = remember(candidates, normalized, selectedCategory) {
        candidates.filter { item ->
            (selectedCategory == "Wszystkie" || institutionalSymbolCategory(item) == selectedCategory) &&
                (normalized.isBlank() || listOf(item.displayName, item.path, item.group, item.repository, item.sourceId)
                    .any { it.lowercase(Locale.ROOT).contains(normalized) })
        }
    }
    val groupedMatches = remember(matches) { matches.groupBy(::institutionalSymbolCategory) }
    val categoryCounts = remember(candidates) { candidates.groupingBy(::institutionalSymbolCategory).eachCount() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ikony → Symbole, herby i znaki instytucjonalne", style = MaterialTheme.typography.titleMedium)
        Text("Tryb pobierania: ${selectedMode.label}", style = MaterialTheme.typography.labelMedium)
        Text(
            "Katalog ikon obejmuje kategorię Symbole oraz herby, godła, flagi i logotypy. Obsługiwane są pliki SVG i PNG. Wybierz grupę lub wpisz nazwę. Pobierany jest tylko wskazany plik po kontroli licencji i walidacji zawartości; źródło, autor i licencja pozostają zapisane w metadanych.",
            style = MaterialTheme.typography.bodySmall
        )

        Text("Kolekcje źródłowe do przeszukiwania: ${sourceCollections.size}", style = MaterialTheme.typography.titleSmall)
        Text("Strona kolekcji i adres API są rejestrowane osobno. Każdy plik wymaga własnej kontroli formatu, autora, licencji, aktualności i praw do użycia przed pobraniem lub udostępnieniem.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sourceCollections, key = { it.id }) { source ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(source.label, style = MaterialTheme.typography.bodyMedium)
                    Text("${source.provider} • ${source.observedSize} • ${source.sourceFormats}", style = MaterialTheme.typography.labelSmall)
                    Text("Etapy: ${source.discoveryState} / ${source.verificationState} / ${source.downloadState} / ${source.storageState} / ${source.availabilityState}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Prawa: ${source.rightsStatus} · sprawdzono: ${source.checkedAt} · odświeżanie: co ${source.refreshIntervalDays} dni", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.sourcePageUrl))) }
                            .onFailure { message = "Nie można otworzyć strony źródłowej: ${it.message ?: "błąd systemu"}" }
                    }, enabled = busyId == null) { Text("Otwórz kolekcję źródłową") }
                }
            }
        }
        Text("Kandydaci z katalogu lokalnego", style = MaterialTheme.typography.titleSmall)
        Text("Rekord źródłowy nie oznacza pobrania pliku. Pliki bez zatwierdzonej licencji pozostają pozycjami referencyjnymi.", style = MaterialTheme.typography.bodySmall)

        Text("Rekordy z adresem SVG/PNG: ${candidates.size} · Wyniki: ${matches.size}", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == "Wszystkie",
                onClick = { selectedCategory = "Wszystkie" },
                label = { Text("Wszystkie (${candidates.size})") }
            )
            institutionalSymbolCategoryOrder.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text("$category (${categoryCounts[category] ?: 0})") }
                )
            }
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Szukaj nazwy, organizacji, państwa lub herbu") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        if (busyId != null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            downloadProgress?.let { progress ->
                Text(progress.stage, style = MaterialTheme.typography.bodySmall)
                val total = progress.totalBytes
                Text(
                    if (total != null && total > 0L)
                        "${formatInstitutionalSymbolBytes(progress.downloadedBytes)} / ${formatInstitutionalSymbolBytes(total)}"
                    else formatInstitutionalSymbolBytes(progress.downloadedBytes) + " pobrano",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (message.isNotBlank() && busyId == null) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.startsWith("Pobrano i zapisano")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
            )
        }
        if (matches.isEmpty() && candidates.isNotEmpty()) {
            Text("Brak wyników w wybranej grupie.", style = MaterialTheme.typography.bodySmall)
        }
        val theSvgSourceCount = candidates.count { it.repository == "glincker/thesvg" }
        Text(
            "Źródła katalogu: Wikimedia Commons, GitHub oraz theSVG (glincker/thesvg) · $theSvgSourceCount rekordów theSVG",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            institutionalSymbolCategoryOrder.forEach { category ->
                val groupItems = groupedMatches[category].orEmpty()
                if (groupItems.isNotEmpty()) {
                    item(key = "svg-category-$category") {
                        Text("$category · ${groupItems.size}", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    items(groupItems, key = { it.sourceId + ":" + it.path }) { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(item.displayName, style = MaterialTheme.typography.bodyMedium)
                            Text("${item.group} • ${item.repository}", style = MaterialTheme.typography.labelSmall)
                            Text("Źródło: ${item.repository} · autor: ${item.author.ifBlank { "niepodany w metadanych" }}", style = MaterialTheme.typography.labelSmall)
                            
                            Text("Licencja: ${item.license.ifBlank { "niepodana" }} · status: ${item.rightsStatus}", style = MaterialTheme.typography.labelSmall)
                            Button(enabled = busyId == null && institutionalSymbolCanDownload(item), onClick = {
                                busyId = item.sourceId + ":" + item.path
                                message = ""
                                downloadProgress = InstitutionalSymbolDownloadProgress("Przygotowanie pobierania…")
                                scope.launch {
                                    val result = runCatching {
                                        withContext(Dispatchers.IO) {
                                            downloadInstitutionalSymbol(context, item) { update ->
                                                withContext(Dispatchers.Main.immediate) { downloadProgress = update }
                                            }
                                        }
                                    }
                                    message = result.fold(
                                        { "Pobrano i zapisano ${item.displayName} (${formatInstitutionalSymbolBytes(downloadProgress?.downloadedBytes ?: 0L)}). SHA-256: ${it}" },
                                        { "Nie zapisano ${item.displayName}. ${it.message ?: "Nieznany błąd pobierania."}" }
                                    )
                                    busyId = null
                                    if (result.isSuccess) onSaved()
                                }
                            }) {
                                Text(if (busyId == item.sourceId + ":" + item.path) "Pobieranie…" else if (institutionalSymbolCanDownload(item)) "Pobierz zweryfikowany SVG" else "Wymaga weryfikacji praw")
                            }
                        }
                    }
                }
            }
        }
    }
}
