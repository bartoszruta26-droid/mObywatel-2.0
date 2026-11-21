// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
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
    val displayName: String get() = path.substringAfterLast('/').removeSuffix(".svg")
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
        if (!path.endsWith(".svg", true) || url.isBlank()) return@mapNotNull null
        InstitutionalSymbolCandidate(value("source_id"), value("repository"), value("ref"), path,
            value("group"), value("rights_status"), sourceUrl, url,
            value("license_short_name"), value("license_url"), value("author"), value("source_credit"))
    }.distinctBy { it.sourceId + ":" + it.path }
}

private fun institutionalSymbolAssetId(item: InstitutionalSymbolCandidate): String =
    "symbol_" + MessageDigest.getInstance("SHA-256")
        .digest((item.sourceId + ":" + item.path).toByteArray(Charsets.UTF_8)).take(16)
        .joinToString("") { "%02x".format(it) }

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
    val uri = java.net.URI(item.downloadUrl)
    val trustedSvgHost = uri.host.equals("upload.wikimedia.org", true) ||
        uri.host.equals("raw.githubusercontent.com", true) ||
        uri.host.equals("cdn.jsdelivr.net", true)
    require(uri.scheme.equals("https", true) && uri.userInfo == null && trustedSvgHost) {
        "Źródło pliku nie znajduje się na dozwolonej domenie Wikimedia Commons, GitHub Raw lub jsDelivr."
    }
    onProgress(InstitutionalSymbolDownloadProgress("Łączenie ze źródłem przez wybraną trasę sieciową. Przy uruchamianiu Tor pierwsze połączenie może potrwać dłużej."))
    val response = TorDownloadServiceProvider.get(context).open(item.downloadUrl, TorDownloadKind.SVG)
    val bytes = response.use {
        require(it.statusCode in 200..299) { "Serwer zwrócił HTTP ${it.statusCode}." }
        val expected = it.contentLength?.takeIf { length -> length > 0L }
        onProgress(InstitutionalSymbolDownloadProgress("Połączono. Pobieranie danych SVG…", 0L, expected))
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
                    onProgress(InstitutionalSymbolDownloadProgress("Pobieranie SVG…", total, expected))
                    lastUpdate = now
                }
            }
            onProgress(InstitutionalSymbolDownloadProgress("Pobieranie zakończone. Odebrano ${formatInstitutionalSymbolBytes(total)}. Weryfikacja pliku…", total, expected ?: total))
        }
        output.toByteArray()
    }
    onProgress(InstitutionalSymbolDownloadProgress("Sprawdzanie bezpieczeństwa i poprawności SVG…", bytes.size.toLong(), bytes.size.toLong()))
    validateInstitutionalSymbolSvg(bytes)
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
        .put("verificationStatus", "catalogued; source metadata retained; usage restrictions require review")
        .put("permittedKinds", JSONArray())
        .toString()
    val kind = when {
        item.group.contains("flag", true) -> "Flaga"
        item.group.contains("arm", true) || item.group.contains("herald", true) -> "Herb"
        item.group.contains("government", true) -> "Znak instytucji publicznej"
        else -> "Logo lub emblemat"
    }
    onProgress(InstitutionalSymbolDownloadProgress("Zapisuję plik i metadane w bibliotece…", bytes.size.toLong(), bytes.size.toLong()))
    DocumentBrandStore.importBytes(context, item.repository, kind, item.displayName.take(180),
        bytes, rights = rights, mimeType = "image/svg+xml", assetId = institutionalSymbolAssetId(item))
    onProgress(InstitutionalSymbolDownloadProgress("Zapisano i zweryfikowano SVG.", bytes.size.toLong(), bytes.size.toLong()))
    return sha
}

private val institutionalSymbolCategoryOrder = listOf("Herby", "Godła", "Flagi", "Logotypy", "Pozostałe")

internal fun institutionalSymbolCategory(item: InstitutionalSymbolCandidate): String {
    val key = "${item.group} ${item.path} ${item.displayName}".lowercase(Locale.ROOT)
    return when {
        listOf("flag", "flaga", "banner").any { key.contains(it) } -> "Flagi"
        listOf("coat of arms", "coat-of-arms", "coat_of_arms", "herald", "herb", "crest", "_arms", "arms_").any { key.contains(it) } -> "Herby"
        listOf("eagle", "godlo", "godło", "emblem").any { key.contains(it) } -> "Godła"
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
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    var busyId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableStateOf<InstitutionalSymbolDownloadProgress?>(null) }
    LaunchedEffect(context) {
        message = "Wczytywanie katalogu SVG…"
        withContext(Dispatchers.IO) {
            context.assets.open("identity-symbols/catalog.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.let { raw ->
            candidates = runCatching { parseInstitutionalSymbolCsv(raw) }.getOrElse {
                message = "Nie udało się wczytać katalogu: ${it.message ?: "błąd danych"}"
                emptyList()
            }
            if (candidates.isNotEmpty()) message = "Katalog SVG gotowy."
        }
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
        Text("Katalog źródeł SVG", style = MaterialTheme.typography.titleMedium)
        Text("Tryb pobierania: ${selectedMode.label}", style = MaterialTheme.typography.labelMedium)
        Text(
            "Lista obejmuje kandydatów z GitHub i Wikimedia Commons. Wybierz grupę lub wpisz nazwę. Pobierany jest tylko wskazany plik; źródło, autor i licencja pozostają zapisane w metadanych.",
            style = MaterialTheme.typography.bodySmall
        )
        Text("Rekordy z adresem SVG: ${candidates.size} · Wyniki: ${matches.size}", style = MaterialTheme.typography.labelMedium)
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
                            Text("Licencja: ${item.license.ifBlank { "metadane do sprawdzenia" }}", style = MaterialTheme.typography.labelSmall)
                            Text("Status praw: ${item.rightsStatus.ifBlank { "do niezależnej weryfikacji" }}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(enabled = busyId == null, onClick = {
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
                                Text(if (busyId == item.sourceId + ":" + item.path) "Pobieranie…" else "Pobierz ten SVG")
                            }
                        }
                    }
                }
            }
        }
    }
}
