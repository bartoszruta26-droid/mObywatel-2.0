// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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

private fun validateInstitutionalSymbolSvg(bytes: ByteArray) {
    require(bytes.isNotEmpty() && bytes.size <= 12 * 1024 * 1024) { "SVG pusty lub przekracza 12 MB." }
    val raw = String(bytes, Charsets.UTF_8)
    require(raw.contains("<svg", true)) { "Źródło nie zwróciło SVG." }
    require(!Regex("<!DOCTYPE|<!ENTITY|<(script|foreignObject)\\b", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
        "SVG zawiera niedozwolone deklaracje lub aktywne elementy."
    }
    require(!Regex("(href|src)\\s*=\\s*['\"]\\s*(https?:|file:|javascript:|data:)", RegexOption.IGNORE_CASE).containsMatchIn(raw)) {
        "SVG zawiera zewnętrzne lub aktywne odwołanie."
    }
    val parser = android.util.Xml.newPullParser()
    parser.setInput(java.io.StringReader(raw))
    var rootSeen = false
    var event = parser.eventType
    while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
        if (event == org.xmlpull.v1.XmlPullParser.START_TAG && !rootSeen) {
            require(parser.name.equals("svg", true)) { "Dokument XML nie ma elementu głównego SVG." }
            rootSeen = true
        }
        event = parser.next()
    }
    require(rootSeen) { "Brak elementu SVG." }
}

private suspend fun downloadInstitutionalSymbol(context: Context, item: InstitutionalSymbolCandidate): String {
    val uri = java.net.URI(item.downloadUrl)
    require(uri.scheme.equals("https", true) && uri.userInfo == null &&
        (uri.host.equals("upload.wikimedia.org", true) || uri.host.equals("raw.githubusercontent.com", true))) {
        "Źródło pliku nie znajduje się na dozwolonej domenie Wikimedia Commons lub raw.githubusercontent.com."
    }
    val response = TorDownloadServiceProvider.get(context).open(item.downloadUrl, TorDownloadKind.SVG)
    val bytes = response.use {
        require(it.statusCode in 200..299) { "Serwer zwrócił HTTP ${it.statusCode}." }
        val output = ByteArrayOutputStream()
        it.body.use { input ->
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= 12 * 1024 * 1024) { "SVG przekracza limit 12 MB." }
                output.write(buffer, 0, count)
            }
        }
        output.toByteArray()
    }
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
    DocumentBrandStore.importBytes(context, item.repository, kind, item.displayName.take(180),
        bytes, rights = rights, mimeType = "image/svg+xml", assetId = institutionalSymbolAssetId(item))
    return sha
}

/** Searchable source catalogue. Only the selected SVG is downloaded. */
@Composable
internal fun InstitutionalSymbolCatalogPanel(context: Context, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    var candidates by remember { mutableStateOf<List<InstitutionalSymbolCandidate>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var busyId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            context.assets.open("identity-symbols/catalog.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.let { raw ->
            candidates = runCatching { parseInstitutionalSymbolCsv(raw) }.getOrElse {
                message = "Nie udało się wczytać katalogu: ${it.message ?: "błąd danych"}"
                emptyList()
            }
        }
    }
    val normalized = query.trim().lowercase(Locale.ROOT)
    val matches = remember(candidates, normalized) {
        if (normalized.isBlank()) emptyList() else candidates.filter { item ->
            listOf(item.displayName, item.path, item.group, item.repository, item.sourceId)
                .any { it.lowercase(Locale.ROOT).contains(normalized) }
        }.take(20)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Katalog nazw i pobieranie pojedynczych symboli", style = MaterialTheme.typography.titleSmall)
        Text("Indeks zawiera źródła z GitHub i Wikimedia Commons. Wyszukiwanie działa lokalnie; pobierany jest wyłącznie wybrany plik SVG.", style = MaterialTheme.typography.bodySmall)
        Text("W katalogu: ${candidates.size} rekordów SVG z dostępnym adresem pobierania.", style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Szukaj nazwy, organizacji, państwa lub herbu") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        if (normalized.isNotBlank() && matches.isEmpty() && candidates.isNotEmpty()) {
            Text("Brak wyników dla tej frazy.", style = MaterialTheme.typography.bodySmall)
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(matches, key = { it.sourceId + ":" + it.path }) { item ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.displayName, style = MaterialTheme.typography.bodyMedium)
                    Text("${item.group} • ${item.repository}", style = MaterialTheme.typography.labelSmall)
                    Text("Licencja: ${item.license.ifBlank { "metadane do sprawdzenia" }}", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = busyId == null, onClick = {
                            busyId = item.sourceId + ":" + item.path
                            message = "Pobieranie: ${item.displayName}"
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { runCatching { downloadInstitutionalSymbol(context, item) } }
                                message = result.fold(
                                    { "Zapisano ${item.displayName}; SHA-256: ${it.take(16)}…" },
                                    { "Błąd pobierania ${item.displayName}: ${it.message ?: "nieznany błąd"}" }
                                )
                                busyId = null
                                if (result.isSuccess) onSaved()
                            }
                        }) { Text(if (busyId == item.sourceId + ":" + item.path) "Pobieranie…" else "Pobierz ten SVG") }
                    }
                }
            }
        }
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
    }
}
