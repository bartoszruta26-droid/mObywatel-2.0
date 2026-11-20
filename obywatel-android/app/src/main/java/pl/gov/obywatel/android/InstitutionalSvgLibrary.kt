// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.util.Xml
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import pl.gov.obywatel.android.network.tor.TorDownloadKind
import pl.gov.obywatel.android.network.tor.TorDownloadServiceProvider
import pl.gov.obywatel.android.network.tor.TorHttpException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private fun validateSvg(bytes:ByteArray) {
    require(bytes.isNotEmpty() && bytes.size <= 12*1024*1024) { "Plik pusty lub przekracza 12 MB." }
    val raw=String(bytes,Charsets.UTF_8)
    require(raw.contains("<svg",true)) { "Serwer nie zwrócił pliku SVG." }
    require(!Regex("<!DOCTYPE|<!ENTITY",RegexOption.IGNORE_CASE).containsMatchIn(raw)) { "SVG zawiera DTD lub deklarację encji." }
    require(!Regex("<(script|foreignObject)\\b",RegexOption.IGNORE_CASE).containsMatchIn(raw)) { "SVG zawiera niedozwolone elementy aktywne." }
    require(!Regex("(href|src)\\s*=\\s*['\"]\\s*(https?:|file:|javascript:|data:)",RegexOption.IGNORE_CASE).containsMatchIn(raw)) { "SVG zawiera zewnętrzne odwołanie." }
    val parser=Xml.newPullParser()
    parser.setInput(java.io.StringReader(raw))
    var root=false
    var event=parser.eventType
    while(event!=XmlPullParser.END_DOCUMENT) {
        if(event==XmlPullParser.START_TAG && !root) { require(parser.name.equals("svg",true)) { "Dokument XML nie ma elementu głównego SVG." }; root=true }
        event=parser.next()
    }
    require(root) { "Brak elementu SVG." }
}


internal data class CommonsSvgCandidate(val title:String,val url:String,val page:String,val license:String,val licenseUrl:String,val artist:String,val restrictions:String,val sha256:String = "")
/** CSV export of search results only; this never downloads SVG payloads. */
internal fun commonsCandidatesCsv(candidates: List<CommonsSvgCandidate>): String {
    fun quote(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
    val header = listOf("filename", "symbol_type", "commons_page", "svg_url", "author", "license", "license_url", "restrictions", "sha256")
    val rows = candidates.distinctBy { it.title }.map { item ->
        val title = item.title.lowercase()
        val type = when {
            listOf("coat of arms", "coat_of_arms", "herb", "crest", "arms").any(title::contains) -> "herb/godło"
            listOf("logo", "emblem", "seal", "insignia", "symbol").any(title::contains) -> "logo/emblemat"
            listOf("flag", "banner").any(title::contains) -> "flaga"
            else -> "symbol"
        }
        listOf(item.title, type, item.page, item.url, item.artist, item.license, item.licenseUrl, item.restrictions, item.sha256)
    }
    return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(",") { quote(it) } } + "\n"
}

/** Import parser for Commons CSV exports and plain URL lists. */
internal fun parseExternalCommonsSvgList(raw: String): List<CommonsSvgCandidate> {
    val lines = raw.removePrefix("\uFEFF").lineSequence().map { it.trim().trimEnd('\r') }.filter { it.isNotBlank() && !it.startsWith("#") }.toList()
    require(lines.isNotEmpty()) { "Zaimportowana lista SVG jest pusta." }
    fun csv(line: String): List<String> {
        val out = mutableListOf<String>(); val cell = StringBuilder(); var quoted = false; var i = 0
        while (i < line.length) { val ch = line[i]; when {
            ch == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { cell.append('"'); i++ }
            ch == '"' -> quoted = !quoted
            ch == ',' && !quoted -> { out += cell.toString(); cell.setLength(0) }
            else -> cell.append(ch)
        }; i++ }
        require(!quoted) { "Nieprawidłowe cudzysłowy w CSV." }; out += cell.toString(); return out
    }
    val first = csv(lines.first()).map { it.trim().lowercase() }
    val isCsvHeader = first.any { it in setOf("filename", "file", "title", "svg_url", "downloadurl", "url") } &&
        (lines.first().contains(",") || first.any { it in setOf("svg_url", "downloadurl", "url") })
    val candidates = if (isCsvHeader) {
        fun idx(vararg names: String) = names.firstNotNullOfOrNull { name -> first.indexOf(name).takeIf { it >= 0 } }
        val urlCol = idx("svg_url", "downloadurl", "url")
        require(urlCol != null) { "Brak kolumny URL w CSV." }
        val fileCol = idx("filename", "file", "title"); val pageCol = idx("commons_page", "sourcepage", "page")
        val authorCol = idx("author", "artist"); val licenseCol = idx("license")
        val licenseUrlCol = idx("license_url", "licenseurl"); val restrictionsCol = idx("restrictions"); val shaCol = idx("sha256")
        lines.drop(1).mapNotNull { line -> val row = csv(line); fun v(col: Int?) = col?.let { row.getOrNull(it).orEmpty().trim() }.orEmpty();
            val url = v(urlCol); if (url.isBlank()) null else CommonsSvgCandidate(v(fileCol).ifBlank { "File:" + url.substringAfterLast('/').substringBefore('?') }, url, v(pageCol), v(licenseCol), v(licenseUrlCol), v(authorCol), v(restrictionsCol), v(shaCol)) }
    } else lines.map { url -> CommonsSvgCandidate("File:" + url.substringAfterLast('/').substringBefore('?'), url, "", "", "", "", "") }
    val checked = candidates.map { item ->
        val uri = java.net.URI(item.url)
        require(uri.scheme.equals("https", true) && uri.host.equals("upload.wikimedia.org", true) && uri.path.endsWith(".svg", true) && uri.userInfo == null) { "Lista zawiera niedozwolony adres SVG." }
        item.copy(title = item.title.trim().let { if (it.startsWith("File:")) it else "File:$it" })
    }
    val unique = distinctCommonsCandidates(checked)
    require(unique.isNotEmpty()) { "Lista nie zawiera prawidłowych, unikatowych plików SVG Wikimedia Commons." }
    return unique
}
/** Ordered Commons search strategies: MIME-filtered first, plain fallback second. */
internal fun commonsSvgSearchExpressions(term: String): List<String> {
    val safeTerm = term.trim()
    require(safeTerm.isNotEmpty()) { "Fraza wyszukiwania SVG nie może być pusta." }
    return listOf("$safeTerm filemime:svg", safeTerm)
}

// Commons rate limiting: keep every API and file request sequential and deliberately slow.
private const val COMMONS_MIN_REQUEST_INTERVAL_MS = 1000L
private const val COMMONS_API_MAX_ATTEMPTS = 3
private const val COMMONS_DOWNLOAD_MAX_ATTEMPTS = 5
private val commonsApiThrottleLock = Any()
@Volatile private var commonsApiLastRequestAt = 0L

private fun waitForCommonsRequestSlot() {
    synchronized(commonsApiThrottleLock) {
        val waitMs = COMMONS_MIN_REQUEST_INTERVAL_MS - (System.currentTimeMillis() - commonsApiLastRequestAt)
        if (waitMs > 0) Thread.sleep(waitMs)
        commonsApiLastRequestAt = System.currentTimeMillis()
    }
}

private fun retryAfterMillis(value: String?): Long? {
    val header = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    header.toLongOrNull()?.let { seconds ->
        return if (seconds <= 0L) 0L else if (seconds >= 180L) 180000L else seconds * 1000L
    }
    return runCatching {
        val date = java.time.ZonedDateTime.parse(header, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        (date - System.currentTimeMillis()).coerceAtLeast(0L).coerceAtMost(180000L)
    }.getOrNull()
}

private class CommonsHttpException(
    val httpCode: Int,
    val retryDelayMs: Long?,
) : java.io.IOException("Wikimedia Commons: HTTP $httpCode")

internal fun commonsRetryDelayMs(attempt: Int, retryAfterSeconds: Long? = null): Long {
    // Cap before multiplication so an untrusted Retry-After value cannot overflow Long.
    val serverDelay = retryAfterSeconds?.takeIf { it >= 0 }?.let { seconds ->
        if (seconds >= 60L) 60000L else seconds * 1000L
    }
    val exponentialDelay = 2000L * (1L shl (attempt - 1).coerceIn(0, 4))
    return maxOf(serverDelay ?: exponentialDelay, exponentialDelay).coerceIn(2000L, 30000L)
}

private fun commonsRequest(context: Context, params: String): JSONObject {
    var lastFailure: Exception? = null
    val tor = TorDownloadServiceProvider.get(context)
    repeat(COMMONS_API_MAX_ATTEMPTS) { attempt ->
        waitForCommonsRequestSlot()
        try {
            val url = "https://commons.wikimedia.org/w/api.php?$params&maxlag=5"
            appendCommonsDiagnostic(context, "API_REQUEST attempt=${attempt + 1} params=${params.take(900)}")
            val payload = tor.open(url, TorDownloadKind.API).use { response ->
                JSONObject(response.body.bufferedReader(Charsets.UTF_8).use { it.readText() })
            }
            appendCommonsDiagnostic(context, "API_RESPONSE keys=${payload.keys().asSequence().toList()}")
            val apiError = payload.optJSONObject("error")
            if (apiError != null) {
                val errorCode = apiError.optString("code", "unknown")
                val errorInfo = apiError.optString("info", "brak szczegółów")
                appendCommonsDiagnostic(context, "API_ERROR code=$errorCode info=${errorInfo.take(700)}")
                throw java.io.IOException("Wikimedia Commons API [$errorCode]: $errorInfo")
            }
            return payload
        } catch (failure: TorHttpException) {
            val retryAfterMs = retryAfterMillis(failure.retryAfter)
            val retryable = failure.httpCode == 429 || failure.httpCode == 503 || failure.httpCode == 502 || failure.httpCode == 504
            appendCommonsDiagnostic(context, "API_HTTP_ERROR attempt=${attempt + 1} code=${failure.httpCode} retryAfterMs=$retryAfterMs")
            lastFailure = CommonsHttpException(failure.httpCode, retryAfterMs)
            if (!retryable || attempt + 1 >= COMMONS_API_MAX_ATTEMPTS) throw lastFailure!!
            Thread.sleep(maxOf(commonsRetryDelayMs(attempt + 2), retryAfterMs ?: 0L))
        } catch (failure: Exception) {
            appendCommonsDiagnostic(context, "API_EXCEPTION attempt=${attempt + 1} type=${failure.javaClass.simpleName} message=${failure.message ?: "brak"}")
            lastFailure = failure
            if (attempt + 1 >= COMMONS_API_MAX_ATTEMPTS) throw failure
            Thread.sleep(commonsRetryDelayMs(attempt + 2))
        }
    }
    throw lastFailure ?: java.io.IOException("Wikimedia Commons: nie udało się wykonać żądania API przez Tor.")
}

private suspend fun discoverCommonsOpenSvgs(
    context: Context,
    limit: Int = 200,
    onSearchProgress: suspend (titlesFound: Int, term: String) -> Unit = { _, _ -> },
    onBatchReady: suspend (List<CommonsSvgCandidate>) -> Unit = {}
): List<CommonsSvgCandidate> {
    val terms = listOf(
        "coat of arms", "heraldry", "national emblem", "state emblem",
        "national flag", "city flag", "institutional emblem", "official seal",
        "government logo", "herb państwa", "godło państwowe", "herb miasta",
        "herb gminy", "herb powiatu", "herb województwa", "flaga państwowa",
        // Polish public institutions: include official abbreviations and full names.
        "Zakład Ubezpieczeń Społecznych ZUS logo", "ZUS logo Polska",
        "Narodowy Fundusz Zdrowia NFZ logo", "NFZ logo Polska",
        "Kasa Rolniczego Ubezpieczenia Społecznego KRUS logo", "KRUS logo Polska",
        "Państwowy Fundusz Rehabilitacji Osób Niepełnosprawnych PFRON logo",
        "PFRON logo Polska", "Rzecznik Praw Pacjenta logo",
        "Główny Inspektorat Sanitarny GIS logo", "Główny Urząd Statystyczny GUS logo",
        "Narodowy Bank Polski NBP logo", "Państwowa Inspekcja Pracy PIP logo",
        "Urząd Ochrony Konkurencji i Konsumentów UOKiK logo",
        "Urząd Ochrony Danych Osobowych UODO logo",
        "Rzecznik Praw Obywatelskich RPO logo", "Kancelaria Prezesa Rady Ministrów logo",
        "Polish public institution logo", "Polish government agency emblem",
        "United Nations logo", "World Health Organization logo", "UNESCO logo",
        "UNICEF logo", "European Union emblem", "Council of Europe logo", "NATO emblem",
        "international NGO logo", "humanitarian organization logo",
        "human rights organization logo", "environmental organization logo",
        "Amnesty International logo", "Greenpeace logo", "Médecins Sans Frontières logo",
        "Save the Children logo", "Oxfam logo", "World Wide Fund for Nature logo",
        "Human Rights Watch logo", "Transparency International logo",
        "International Rescue Committee logo", "CARE International logo",
        "Caritas Internationalis logo", "World Vision logo", "Plan International logo",
        "Habitat for Humanity logo", "Rotary International logo", "Lions Clubs International logo"
    )
    val seenTitles = linkedSetOf<String>()
    val deliveredTitles = hashSetOf<String>()
    val allCandidates = mutableListOf<CommonsSvgCandidate>()

    suspend fun verifyAndDeliver(batch: List<String>) {
        if (batch.isEmpty() || allCandidates.size >= limit) return
        val encoded = java.net.URLEncoder.encode(batch.joinToString("|"), "UTF-8")
        appendCommonsDiagnostic(context, "VERIFY_BATCH size=${batch.size} first=${batch.first()} last=${batch.last()}")
        val response = try {
            commonsRequest(context, "action=query&prop=imageinfo&titles=$encoded&iiprop=url|mime|extmetadata&format=json")
        } catch (failure: Exception) {
            appendCommonsDiagnostic(context, "VERIFY_BATCH_FAILED type=${failure.javaClass.simpleName} message=${failure.message ?: "brak"}")
            onSearchProgress(seenTitles.size, "Weryfikacja metadanych: partia pominięta (" + (failure.message ?: "błąd API") + ")")
            return
        }
        val pages = response.optJSONObject("query")?.optJSONObject("pages")
            ?: throw java.io.IOException("Wikimedia Commons nie zwróciło sekcji query.pages podczas weryfikacji SVG.")
        val candidates = mutableListOf<CommonsSvgCandidate>()
        val keys = pages.keys()
        var pagesSeen = 0
        var noImageInfo = 0
        var nonSvgMime = 0
        var invalidUrl = 0
        while (keys.hasNext() && allCandidates.size + candidates.size < limit) {
            val page = pages.optJSONObject(keys.next()) ?: continue
            pagesSeen++
            val title = page.optString("title")
            if (!deliveredTitles.add(title)) continue
            val info = page.optJSONArray("imageinfo")?.optJSONObject(0)
            if (info == null) { noImageInfo++; appendCommonsDiagnostic(context, "VERIFY_NO_IMAGEINFO title=$title"); continue }
            if (info.optString("mime") != "image/svg+xml") { nonSvgMime++; appendCommonsDiagnostic(context, "VERIFY_NON_SVG title=$title mime=${info.optString("mime")}"); continue }
            val meta = info.optJSONObject("extmetadata") ?: JSONObject()
            fun value(key: String) = meta.optJSONObject(key)?.optString("value").orEmpty()
                .replace(Regex("<[^>]*>"), " ").replace("&nbsp;", " ").trim()
            // Na tym etapie nie filtrujemy ani nie oceniamy licencji.
            // Zachowujemy metadane Commons w manifeście, ale pobieramy każdy poprawny SVG.
            val license = value("LicenseShortName")
            val url = info.optString("url")
            if (!url.startsWith("https://upload.wikimedia.org/")) { invalidUrl++; appendCommonsDiagnostic(context, "VERIFY_INVALID_URL title=$title url=${url.take(250)}"); continue }
            val pageUrl = "https://commons.wikimedia.org/wiki/" +
                java.net.URLEncoder.encode(title.replace(' ', '_'), "UTF-8")
            candidates.add(CommonsSvgCandidate(
                title, url, pageUrl, license, value("LicenseUrl"), value("Artist"), value("Restrictions")
            ))
        }
        appendCommonsDiagnostic(context, "VERIFY_RESULT pages=$pagesSeen noImageInfo=$noImageInfo nonSvgMime=$nonSvgMime invalidUrl=$invalidUrl candidates=${candidates.size}")
        if (candidates.isNotEmpty()) {
            allCandidates.addAll(candidates)
            onSearchProgress(seenTitles.size, "Zweryfikowano " + allCandidates.size + "; przekazuję partię do pobierania")
            onBatchReady(candidates)
        }
    }

    var pending = mutableListOf<String>()
    for (term in terms) {
        if (allCandidates.size >= limit || seenTitles.size >= limit * 2) break
        // Prefer MIME-property search, then fall back to a plain file-namespace search.
        // The final title-extension check below still guarantees that only SVG files are accepted.
        var results: JSONArray? = null
        var lastSearchFailure: Exception? = null
        for (expression in commonsSvgSearchExpressions(term)) {
            try {
                val query = java.net.URLEncoder.encode(expression, "UTF-8")
                val response = commonsRequest(
                    context,
                    "action=query&list=search&srnamespace=6&srsearch=$query&srlimit=50&format=json"
                )
                val searchArray = response.optJSONObject("query")?.optJSONArray("search")
                    ?: throw java.io.IOException("Wikimedia Commons nie zwróciło listy wyników wyszukiwania.")
                appendCommonsDiagnostic(context, "SEARCH term=$term expression=$expression results=${searchArray.length()}")
                if (searchArray.length() > 0) {
                    results = searchArray
                    break
                }
            } catch (failure: Exception) {
                appendCommonsDiagnostic(context, "SEARCH_FAILED term=$term expression=$expression type=${failure.javaClass.simpleName} message=${failure.message ?: "brak"}")
                lastSearchFailure = failure
            }
        }
        if (results == null && lastSearchFailure != null) {
            onSearchProgress(seenTitles.size, "$term (błąd API: " + (lastSearchFailure.message ?: "nieznany błąd") + ")")
        }
        results?.let { searchResults ->
            for (i in 0 until searchResults.length()) {
                val title = searchResults.optJSONObject(i)?.optString("title").orEmpty()
                if (title.startsWith("File:") && title.endsWith(".svg", true) && seenTitles.add(title)) {
                    pending.add(title)
                    if (pending.size >= 40) {
                        verifyAndDeliver(pending.toList())
                        pending = mutableListOf()
                        if (allCandidates.size >= limit) break
                    }
                }
            }
        }
        appendCommonsDiagnostic(context, "TERM_SUMMARY term=$term uniqueSvgTitles=${seenTitles.size} pending=${pending.size} verified=${allCandidates.size}")
        onSearchProgress(seenTitles.size, term)
        // Verify and dispatch a first small batch while discovery is still running.
        if (pending.size >= 10) {
            verifyAndDeliver(pending.toList())
            pending = mutableListOf()
        }
    }
    if (pending.isNotEmpty() && allCandidates.size < limit) verifyAndDeliver(pending)
    val finalCandidates = allCandidates.distinctBy { it.title }.take(limit)
    appendCommonsDiagnostic(context, "SEARCH_COMPLETE terms=${terms.size} uniqueSvgTitles=${seenTitles.size} candidates=${finalCandidates.size}")
    return finalCandidates
}

private fun downloadCommonsSvg(context: Context, item: CommonsSvgCandidate): String {
    waitForCommonsRequestSlot()
    val tor = TorDownloadServiceProvider.get(context)
    val assetId = commonsAssetId(item.title)
    val tempDirectory = File(context.cacheDir, "tor-svg-downloads").apply { mkdirs() }
    require(tempDirectory.isDirectory) { "Nie można utworzyć katalogu wznowienia pobierania." }
    val partFile = File(tempDirectory, "$assetId.part")
    val metadataFile = File(tempDirectory, "$assetId.json")
    val metadata = runCatching { JSONObject(metadataFile.readText(Charsets.UTF_8)) }.getOrDefault(JSONObject())
    var offset = partFile.takeIf { it.isFile }?.length() ?: 0L
    if (offset > 12L * 1024L * 1024L) { partFile.delete(); offset = 0L }
    val validator = metadata.optString("etag").takeIf { it.isNotBlank() }
        ?: metadata.optString("lastModified").takeIf { it.isNotBlank() }
    if (offset > 0L && validator == null) {
        // Without a stable validator, never append bytes from a possibly changed remote file.
        partFile.delete()
        metadataFile.delete()
        offset = 0L
    }
    var response = try {
        tor.open(item.url, TorDownloadKind.SVG, offset, validator)
    } catch (failure: Exception) {
        if (offset <= 0L) throw failure
        partFile.delete()
        metadataFile.delete()
        offset = 0L
        tor.open(item.url, TorDownloadKind.SVG)
    }
    try {
        val statusCode = response.statusCode
        val contentRange = response.header("Content-Range")
        val append = commonsCanAppendRange(offset, statusCode, contentRange)
        if (offset > 0L && statusCode == 206 && !append) {
            // The server returned a partial response for a different byte offset.
            // Discard the stale part so the next retry starts from byte zero.
            partFile.delete()
            metadataFile.delete()
            throw java.io.IOException("Serwer zwrócił niezgodny Content-Range; część pobrania usunięto.")
        }
        if (statusCode == 206) {
            val expectedStart = if (append) offset else 0L
            require(response.header("Content-Range").orEmpty().startsWith("bytes $expectedStart-")) {
                "Nieprawidłowy początek zakresu SVG."
            }
        }
        val expectedTotal = if (statusCode == 206) {
            response.header("Content-Range")?.substringAfterLast('/')?.toLongOrNull()
                ?: response.contentLength?.let { if (append) offset + it else it }
        } else response.contentLength
        require(expectedTotal == null || expectedTotal <= 12L * 1024L * 1024L) { "Plik SVG przekracza limit 12 MB." }
        metadata.put("etag", response.header("ETag").orEmpty())
            .put("lastModified", response.header("Last-Modified").orEmpty())
            .put("expectedTotal", expectedTotal ?: -1L)
            .put("updatedAt", System.currentTimeMillis())
        metadataFile.writeText(metadata.toString(), Charsets.UTF_8)
        FileOutputStream(partFile, append).use { output ->
            response.body.use { input ->
                val buffer = ByteArray(8192)
                var total = if (append) offset else 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 12L * 1024L * 1024L) { "Plik SVG przekracza limit 12 MB." }
                    output.write(buffer, 0, count)
                }
                output.fd.sync()
            }
        }
    } finally {
        response.close()
    }
    val expectedLength = metadata.optLong("expectedTotal", -1L)
    if (expectedLength > 0L && partFile.length() != expectedLength) {
        throw java.io.IOException("Niepełny transfer SVG: rozmiar częściowy nie odpowiada długości serwera.")
    }
    val bytes = partFile.readBytes()
    validateSvg(bytes)
    val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    if (item.sha256.isNotBlank() && !commonsChecksumMatches(item.sha256, sha)) {
        partFile.delete()
        metadataFile.delete()
        throw java.io.IOException("Suma SHA-256 pobranego SVG jest niezgodna z oczekiwaną sumą.")
    }
    val rights = JSONObject().put("source", "Wikimedia Commons").put("sourcePage", item.page).put("downloadUrl", item.url)
        .put("license", item.license).put("licenseUrl", item.licenseUrl).put("author", item.artist)
        .put("restrictions", item.restrictions).put("sha256", sha)
        .put("verificationStatus", "open-license-metadata-found; trademark-and-use-review-required")
        .put("permittedKinds", JSONArray()).toString()
    val title = item.title.removePrefix("File:")
    val kind = when {
        title.contains("flag", true) -> "Flaga"
        title.contains("coat of arms", true) || title.contains("herald", true) -> "Herb"
        title.contains("seal", true) -> "Pieczęć"
        title.contains("emblem", true) -> "Godło"
        else -> "Logo"
    }
    DocumentBrandStore.importBytes(context, "Wikimedia Commons", kind, title.take(180), bytes, rights = rights, mimeType = "image/svg+xml", assetId = assetId)
    partFile.delete()
    metadataFile.delete()
    return sha
}


private const val COMMONS_DOWNLOAD_LIST_FILE = "commons-svg-download-list.json"
private const val COMMONS_DOWNLOAD_REPORT_FILE = "commons-svg-download-report.json"
private const val COMMONS_DIAGNOSTIC_LOG_FILE = "commons-svg-diagnostic.log"
private val commonsDiagnosticLock = Any()
private fun appendCommonsDiagnostic(context: Context, message: String) {
    synchronized(commonsDiagnosticLock) {
        val stamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.ROOT).format(java.util.Date())
        FileOutputStream(context.getFileStreamPath(COMMONS_DIAGNOSTIC_LOG_FILE), true).use { stream ->
            java.io.OutputStreamWriter(stream, Charsets.UTF_8).buffered().use { writer ->
                writer.append(stamp).append(" ").append(message.replace("\\n", " ").take(2000))
                writer.append('\n')
            }
        }
    }
}
private fun resetCommonsDiagnostic(context: Context) {
    synchronized(commonsDiagnosticLock) {
        context.getFileStreamPath(COMMONS_DIAGNOSTIC_LOG_FILE).writeText("Diagnostyka Wikimedia Commons SVG\\n", Charsets.UTF_8)
    }
}

private fun writeCommonsJson(context: Context, fileName: String, value: String) {
    val atomic = android.util.AtomicFile(context.getFileStreamPath(fileName))
    val output = atomic.startWrite()
    try {
        output.write(value.toByteArray(Charsets.UTF_8))
        atomic.finishWrite(output)
    } catch (failure: Exception) {
        atomic.failWrite(output)
        throw failure
    }
}

private fun readCommonsJson(context: Context, fileName: String): String =
    android.util.AtomicFile(context.getFileStreamPath(fileName)).openRead()
        .bufferedReader(Charsets.UTF_8).use { it.readText() }

internal fun isSvgDownloadDropOff(found: Int, available: Int): Boolean =
    found >= 100 && available.coerceAtLeast(0) * 2 < found

internal fun commonsCanAppendRange(offset: Long, statusCode: Int, contentRange: String?): Boolean =
    offset > 0L && statusCode == 206 && contentRange?.startsWith("bytes $offset-") == true
internal fun commonsChecksumMatches(expectedSha256: String, actualSha256: String?): Boolean =
    expectedSha256.matches(Regex("[a-fA-F0-9]{64}")) && actualSha256?.equals(expectedSha256, ignoreCase = true) == true

internal fun commonsDownloadProgressLabel(completed: Int, total: Int): String {
    val safeTotal = total.coerceAtLeast(0)
    val safeCompleted = completed.coerceIn(0, safeTotal)
    return "Postęp: $safeCompleted/$safeTotal plików SVG"
}

private data class CommonsDownloadSummary(
    val found: Int,
    val downloaded: Int,
    val alreadyPresent: Int,
    val failed: Int,
    val attempts: Int
)

private fun commonsAssetId(title: String): String =
    "commons_" + MessageDigest.getInstance("SHA-256")
        .digest(title.toByteArray(Charsets.UTF_8)).take(16)
        .joinToString("") { "%02x".format(it) }

/**
 * One logical Commons asset must have one queue entry, regardless of how many
 * search terms or pagination pages discovered it. Deduplicate by storage identity
 * and canonical upload URL before dispatching work to any Tor route.
 */
internal fun distinctCommonsCandidates(
    candidates: List<CommonsSvgCandidate>
): List<CommonsSvgCandidate> {
    val seenAssetIds = HashSet<String>()
    val seenDownloadUrls = HashSet<String>()
    return candidates.filter { candidate ->
        val assetId = commonsAssetId(candidate.title)
        val canonicalUrl = candidate.url.substringBefore('#').substringBefore('?')
            .trimEnd('/').lowercase(java.util.Locale.ROOT)
        if (canonicalUrl.isBlank()) {
            false
        } else {
            // Record both identities even when one already exists. Avoid short-circuiting:
            // a skipped alias must still reserve its URL against a later differently named alias.
            val newAsset = seenAssetIds.add(assetId)
            val newUrl = seenDownloadUrls.add(canonicalUrl)
            newAsset && newUrl
        }
    }
}

private fun saveCommonsReport(
    context: Context,
    found: Int,
    downloaded: Int,
    alreadyPresent: Int,
    failed: Int,
    attempts: Int,
    current: String,
    errors: List<String>
) {
    val report = JSONObject()
        .put("updatedAt", System.currentTimeMillis())
        .put("found", found)
        .put("downloadedThisRun", downloaded)
        .put("alreadyPresent", alreadyPresent)
        .put("savedInLibrary", DocumentBrandStore.list(context).count { it.id.startsWith("commons_") })
        .put("failed", failed)
        .put("severeDropOff", isSvgDownloadDropOff(found, downloaded + alreadyPresent))
        .put("attempts", attempts)
        .put("current", current)
        .put("errors", JSONArray(errors))
    writeCommonsJson(context, COMMONS_DOWNLOAD_REPORT_FILE, report.toString(2))
}

private fun loadCommonsReportSummary(context: Context): String = runCatching {
    val report = JSONObject(readCommonsJson(context, COMMONS_DOWNLOAD_REPORT_FILE))
    val errors = report.optJSONArray("errors") ?: JSONArray()
    val errorDetails = (0 until minOf(errors.length(), 5)).joinToString(" | ") { errors.optString(it) }
    "Ostatni raport Commons: znaleziono ${report.optInt("found")}, zapisano w tej sesji ${report.optInt("downloadedThisRun")}, już w bibliotece ${report.optInt("alreadyPresent")}, łącznie w bibliotece ${report.optInt("savedInLibrary")}, błędy ${report.optInt("failed")}." +
        (if (report.optBoolean("severeDropOff")) " UWAGA: wykryto duży ubytek — zapisano mniej niż połowę znalezionych pozycji." else "") +
        (if (errorDetails.isNotBlank()) " Przykładowe błędy: $errorDetails." else "") +
        " Aktualizacja: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(report.optLong("updatedAt")))}."
}.getOrDefault("")

private fun readCommonsCandidates(context: Context): List<CommonsSvgCandidate> = runCatching {
    val array = JSONArray(readCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE))
    (0 until array.length()).mapNotNull { index ->
        val item = array.optJSONObject(index) ?: return@mapNotNull null
        CommonsSvgCandidate(
            item.optString("file"), item.optString("downloadUrl"), item.optString("sourcePage"),
            item.optString("license"), item.optString("licenseUrl"), item.optString("author"),
            item.optString("restrictions"), item.optString("sha256")
        )
    }.filter { it.title.startsWith("File:") && it.url.startsWith("https://upload.wikimedia.org/") }
}.getOrDefault(emptyList())

private fun persistCommonsCandidateStatus(
    context: Context, title: String, status: String, attempts: Int, error: String?, sha256: String = ""
) {
    val file = context.getFileStreamPath(COMMONS_DOWNLOAD_LIST_FILE)
    if (!file.exists()) return
    val array = JSONArray(readCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE))
    for (index in 0 until array.length()) {
        val item = array.optJSONObject(index) ?: continue
        if (item.optString("file") == title) {
            item.put("status", status)
                .put("attempts", attempts)
                .put("lastError", error.orEmpty())
                .put("updatedAt", System.currentTimeMillis())
            if (sha256.isNotBlank()) item.put("sha256", sha256)
            break
        }
    }
    writeCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE, array.toString(2))
}

private suspend fun downloadCommonsSvgWithRetry(
    context: Context, item: CommonsSvgCandidate, maxAttempts: Int = COMMONS_DOWNLOAD_MAX_ATTEMPTS
): Triple<Int, String?, String?> {
    var lastError = "Nieznany błąd pobierania."
    repeat(maxAttempts) { index ->
        try {
            val sha256 = downloadCommonsSvg(context, item)
            return Triple(index + 1, null, sha256)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            lastError = failure.message ?: failure.javaClass.simpleName
            if (index + 1 < maxAttempts) {
                val serverDelay = (failure as? CommonsHttpException)?.retryDelayMs ?: 0L
                val backoff = (10000L * (1L shl index.coerceIn(0, 4))).coerceAtMost(180000L)
                kotlinx.coroutines.delay(maxOf(backoff, serverDelay))
            }
        }
    }
    return Triple(maxAttempts, lastError, null)
}

private suspend fun processCommonsCandidates(
    context: Context,
    candidates: List<CommonsSvgCandidate>,
    onProgress: suspend (completed: Int, total: Int, currentTitle: String) -> Unit,
    pauseBetweenDownloadsMs: Long = 1000L
): CommonsDownloadSummary {
    // A unique queue is the single source of work for all Tor routes.
    val uniqueCandidates = distinctCommonsCandidates(candidates)
    var downloaded = 0
    var alreadyPresent = 0
    var failed = 0
    var attempts = 0
    val errors = mutableListOf<String>()
    val knownAssets = DocumentBrandStore.list(context).associateBy { it.id }
    if (uniqueCandidates.isEmpty()) {
        saveCommonsReport(context, 0, 0, 0, 0, 0, "", emptyList())
        return CommonsDownloadSummary(0, 0, 0, 0, 0)
    }
    uniqueCandidates.forEachIndexed { index, candidate ->
        onProgress(index, uniqueCandidates.size, candidate.title)
        val assetId = commonsAssetId(candidate.title)
        val existingAsset = knownAssets[assetId]
        val existingBytes = existingAsset?.let { DocumentBrandStore.fileBytes(context, assetId, it) }
        val actualExistingSha = existingBytes?.let { MessageDigest.getInstance("SHA-256").digest(it).joinToString("") { byte -> "%02x".format(byte) } }
        val recordedSha = candidate.sha256.ifBlank {
            runCatching { JSONObject(existingAsset?.rights.orEmpty()).optString("sha256") }.getOrDefault("")
        }
        if (existingAsset != null && commonsChecksumMatches(recordedSha, actualExistingSha)) {
            alreadyPresent++
            persistCommonsCandidateStatus(context, candidate.title, "saved", 0, null, actualExistingSha.orEmpty())
        } else {
            val result = downloadCommonsSvgWithRetry(context, candidate)
            attempts += result.first
            if (result.second == null && !result.third.isNullOrBlank()) {
                downloaded++
                persistCommonsCandidateStatus(context, candidate.title, "saved", result.first, null, result.third!!)
            } else {
                failed++
                val message = result.second ?: "Pobranie zakończone bez sumy kontrolnej."
                persistCommonsCandidateStatus(context, candidate.title, "failed", result.first, message)
                if (errors.size < 100) errors.add("${candidate.title}: $message")
            }
        }
        saveCommonsReport(context, uniqueCandidates.size, downloaded, alreadyPresent, failed,
            attempts, candidate.title, errors)
        onProgress(index + 1, uniqueCandidates.size, candidate.title)
        if (index < uniqueCandidates.lastIndex && pauseBetweenDownloadsMs > 0L) kotlinx.coroutines.delay(pauseBetweenDownloadsMs.coerceIn(0L, 60000L))
    }
    return CommonsDownloadSummary(uniqueCandidates.size, downloaded, alreadyPresent, failed, attempts)
}

@Composable
internal fun InstitutionalSvgLibraryPanel(context:Context,onSaved:()->Unit) {
    val scope= rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
    var progressCurrent by remember { mutableIntStateOf(0) }
    var progressTotal by remember { mutableIntStateOf(0) }
    var reportSummary by remember { mutableStateOf(loadCommonsReportSummary(context)) }
    var pauseBetweenDownloadsMs by remember { mutableStateOf("1000") }
    val externalListLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { busy = "external-import"; scope.launch { val result = withContext(Dispatchers.IO) { runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: error("Nie można odczytać pliku.")
            val candidates = parseExternalCommonsSvgList(raw); val manifest = JSONArray()
            candidates.forEach { item -> manifest.put(JSONObject().put("file", item.title).put("downloadUrl", item.url).put("sourcePage", item.page).put("license", item.license).put("licenseUrl", item.licenseUrl).put("author", item.artist).put("restrictions", item.restrictions).put("sha256", item.sha256).put("source", "Lista zewnętrzna").put("status", "pending").put("attempts", 0)) }
            writeCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE, manifest.toString(2))
            val known = DocumentBrandStore.list(context).associateBy { it.id }
            val alreadySaved = candidates.count { item ->
                val asset = known[commonsAssetId(item.title)]
                val bytes = asset?.let { DocumentBrandStore.fileBytes(context, it.id, it) }
                val actual = bytes?.let { MessageDigest.getInstance("SHA-256").digest(it).joinToString("") { byte -> "%02x".format(byte) } }
                val expected = item.sha256.ifBlank { runCatching { JSONObject(asset?.rights.orEmpty()).optString("sha256") }.getOrDefault("") }
                asset != null && commonsChecksumMatches(expected, actual)
            }
            candidates.size to alreadySaved
        } }; status = result.fold({ "Zaimportowano " + it.first + " plików SVG; już pobrane i zgodne: " + it.second + ". Pozostałe pobierzesz przyciskiem wznowienia." }, { "Błąd importu listy SVG: " + (it.message ?: "nieznany błąd") }); busy = null } }
    }
    val diagnosticLogLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            busy = "diagnostic-export"
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching {
                    val logFile = context.getFileStreamPath(COMMONS_DIAGNOSTIC_LOG_FILE)
                    require(logFile.isFile) { "Brak logu. Najpierw uruchom wyszukiwanie SVG." }
                    context.contentResolver.openOutputStream(uri)?.use { output -> logFile.inputStream().use { it.copyTo(output) } }
                        ?: error("Nie można zapisać pliku logu.")
                    logFile.length()
                } }
                status = result.fold({ "Wyeksportowano log diagnostyczny (${it} B)." }, { "Błąd eksportu logu: ${it.message ?: "nieznany błąd"}" })
                busy = null
            }
        }
    }
    val candidateListLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            busy = "candidate-export"
            progressCurrent = 0
            progressTotal = 0
            status = "Wyszukiwanie plików SVG do eksportu listy…"
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        resetCommonsDiagnostic(context)
                        appendCommonsDiagnostic(context, "CANDIDATE_EXPORT_START limit=200 download=false")
                        val candidates = discoverCommonsOpenSvgs(context, limit = 200, onSearchProgress = { count, term ->
                            withContext(Dispatchers.Main) { status = "Lista SVG: $count wyników — $term" }
                        })
                        val csv = commonsCandidatesCsv(candidates)
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                            output.write(csv.toByteArray(Charsets.UTF_8))
                        } ?: error("Nie można zapisać pliku CSV.")
                        appendCommonsDiagnostic(context, "CANDIDATE_EXPORT_COMPLETE candidates=${candidates.size} downloaded=false")
                        candidates.size
                    }
                }
                status = result.fold({ "Wyeksportowano listę ${it} plików SVG. Nie pobierano samych grafik." }, { "Błąd eksportu listy SVG: ${it.message ?: "nieznany błąd"}" })
                busy = null
            }
        }
    }
    val zipLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if(uri!=null) {
            busy="zip"
            scope.launch {
                val result=withContext(Dispatchers.IO) {
                    runCatching {
                        val files=DocumentBrandStore.list(context).mapNotNull { asset ->
                            if(!(asset.file.endsWith(".svg.enc") || (asset.file.startsWith("builtin:") && asset.file.endsWith(".svg")))) return@mapNotNull null
                            val bytes=DocumentBrandStore.fileBytes(context,asset.id) ?: return@mapNotNull null
                            if(!String(bytes,0,minOf(512,bytes.size),Charsets.UTF_8).contains("<svg",true)) return@mapNotNull null
                            asset to bytes
                        }
                        require(files.isNotEmpty()) { "Biblioteka nie zawiera plików SVG do spakowania." }
                        val manifest=JSONArray()
                        files.forEach { (asset,bytes) ->
                            manifest.put(JSONObject().put("id",asset.id).put("owner",asset.owner).put("kind",asset.kind)
                                .put("label",asset.label).put("rights",asset.rights)
                                .put("sha256",MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }))
                        }
                        val output=context.contentResolver.openOutputStream(uri) ?: error("Nie można utworzyć ZIP.")
                        ZipOutputStream(output).use { zip ->
                            zip.putNextEntry(ZipEntry("manifest.json")); zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8)); zip.closeEntry()
                            val downloadList=context.getFileStreamPath(COMMONS_DOWNLOAD_LIST_FILE)
                            if (downloadList.exists()) {
                                zip.putNextEntry(ZipEntry("download-list.json"))
                                context.openFileInput(COMMONS_DOWNLOAD_LIST_FILE).use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                            val downloadReport=context.getFileStreamPath(COMMONS_DOWNLOAD_REPORT_FILE)
                            if (downloadReport.exists()) {
                                zip.putNextEntry(ZipEntry("download-report.json"))
                                android.util.AtomicFile(downloadReport).openRead().use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                            files.forEach { (asset,bytes) ->
                                val safe=asset.id.replace(Regex("[^A-Za-z0-9_-]"),"_")
                                zip.putNextEntry(ZipEntry("svg/$safe.svg")); zip.write(bytes); zip.closeEntry()
                            }
                            zip.putNextEntry(ZipEntry("README.md"))
                            zip.write(("Eksport zasobów SVG aplikacji\nPliki: "+files.size+"\nMetadane i SHA-256: manifest.json\nKażdy plik wymaga niezależnej kontroli licencji i aktualności przed publikacją.\n").toByteArray(Charsets.UTF_8))
                            zip.closeEntry()
                        }
                        files.size
                    }
                }
                status=result.fold({ "Utworzono ZIP: ${it} plików SVG wraz z manifestem." },{ "Błąd ZIP: ${it.message ?: "nieznany błąd"}" })
                busy=null
            }
        }
    }
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Herby, godła i symbole instytucjonalne",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        Text("Wyszukiwanie plików SVG w Wikimedia Commons (repozytorium mediów Wikipedii): godeł, herbów, flag, emblematów i logo organizacji międzynarodowych, w tym organizacji pozarządowych. Możesz wyeksportować samą listę wyników do CSV bez pobierania grafik albo osobno pobrać pliki. Metadane źródła, autora i licencji są zachowywane.",style=MaterialTheme.typography.bodySmall)
        val savedSvgCount = DocumentBrandStore.list(context).count { it.file.endsWith(".svg.enc") || (it.file.startsWith("builtin:") && it.file.endsWith(".svg")) }
        Text("Pliki SVG zapisane w bibliotece: $savedSvgCount",style=MaterialTheme.typography.labelMedium)
        if (busy == "commons" || busy == "resume") {
            if (progressTotal > 0) {
                Text(commonsDownloadProgressLabel(progressCurrent, progressTotal), style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(
                    progress = (progressCurrent.toFloat() / progressTotal.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Wyszukiwanie plików SVG…", style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        OutlinedTextField(value = pauseBetweenDownloadsMs, onValueChange = { if (it.all(Char::isDigit) && it.length <= 5) pauseBetweenDownloadsMs = it }, label = { Text("Pauza między pobraniami (ms)") }, supportingText = { Text("Zakres 0–60000 ms; domyślnie 1000 ms.") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedButton(enabled = busy == null, onClick = { externalListLauncher.launch(arrayOf("text/csv", "text/plain", "text/*", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Importuj zewnętrzną listę SVG (CSV / TXT)") }
        OutlinedButton(
            enabled = busy == null,
            onClick = { candidateListLauncher.launch("wikimedia-svg-candidates.csv") },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy == "candidate-export") "Przygotowywanie listy…" else "Eksportuj listę znalezionych plików SVG (CSV)") }
        OutlinedButton(
            enabled = busy == null,
            onClick = {
                busy = "commons"
                progressCurrent = 0
                progressTotal = 0
                status = "Wyszukiwanie i pobieranie do 200 plików SVG z Wikimedia Commons…"
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            var totalFound = 0
                            var totalDownloaded = 0
                            var totalAlreadyPresent = 0
                            var totalFailed = 0
                            var totalAttempts = 0
                            var processed = 0
                            resetCommonsDiagnostic(context)
                            appendCommonsDiagnostic(context, "SEARCH_START limit=200 transport=Tor")
                            writeCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE, JSONArray().toString())
                            val candidates = discoverCommonsOpenSvgs(
                                context,
                                200,
                                onSearchProgress = { foundTitles, term ->
                                    withContext(Dispatchers.Main) {
                                        status = "Wyszukiwanie SVG: $foundTitles znalezionych — $term"
                                    }
                                },
                                onBatchReady = { batch ->
                                    val manifest = JSONArray(readCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE))
                                    batch.forEach { item ->
                                        manifest.put(JSONObject()
                                            .put("file", item.title)
                                            .put("downloadUrl", item.url)
                                            .put("sourcePage", item.page)
                                            .put("license", item.license)
                                            .put("licenseUrl", item.licenseUrl)
                                            .put("author", item.artist)
                                            .put("restrictions", item.restrictions)
                                            .put("source", "Wikimedia Commons")
                                            .put("status", "pending")
                                            .put("attempts", 0)
                                            .put("sha256", ""))
                                    }
                                    writeCommonsJson(context, COMMONS_DOWNLOAD_LIST_FILE, manifest.toString(2))
                                    totalFound += batch.size
                                    withContext(Dispatchers.Main) {
                                        progressCurrent = processed
                                        progressTotal = totalFound
                                        status = "Znaleziono $totalFound plików. Rozpoczynam pobieranie przez Tor…"
                                    }
                                    val part = processCommonsCandidates(context, batch, { completed, total, title ->
                                        withContext(Dispatchers.Main) {
                                            progressCurrent = processed + completed
                                            progressTotal = totalFound
                                            status = "Pobieranie SVG: " + (processed + completed) + "/$totalFound — $title"
                                        }
                                    }, pauseBetweenDownloadsMs.toLongOrNull()?.coerceIn(0L, 60000L) ?: 1000L)
                                    processed += part.found
                                    totalDownloaded += part.downloaded
                                    totalAlreadyPresent += part.alreadyPresent
                                    totalFailed += part.failed
                                    totalAttempts += part.attempts
                                    saveCommonsReport(
                                        context, totalFound, totalDownloaded, totalAlreadyPresent,
                                        totalFailed, totalAttempts, batch.lastOrNull()?.title.orEmpty(), emptyList()
                                    )
                                }
                            )
                            appendCommonsDiagnostic(context, "DOWNLOAD_SUMMARY found=$totalFound downloaded=$totalDownloaded alreadyPresent=$totalAlreadyPresent failed=$totalFailed attempts=$totalAttempts")
                            withContext(Dispatchers.Main) {
                                progressCurrent = processed
                                progressTotal = totalFound
                                status = if (totalFound == 0) {
                                    "Wyszukiwanie zakończone: brak plików SVG pasujących do wyszukiwanych haseł."
                                } else {
                                    "Zakończono wyszukiwanie: $totalFound. Pobrano $totalDownloaded; błędy $totalFailed."
                                }
                            }
                            CommonsDownloadSummary(totalFound, totalDownloaded, totalAlreadyPresent, totalFailed, totalAttempts)
                        }
                    }
                    status = result.fold(
                        { "Commons: znaleziono ${it.found}, pobrano ${it.downloaded}, już było ${it.alreadyPresent}, błędy ${it.failed}; prób pobrania ${it.attempts}." },
                        { "Błąd wyszukiwania Commons: ${it.message ?: "nieznany błąd"}" }
                    )
                    reportSummary = loadCommonsReportSummary(context)
                    busy = null
                    if ((result.getOrNull()?.downloaded ?: 0) > 0) onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy == "commons") "Wyszukiwanie i pobieranie…" else "Znajdź i pobierz do 200 plików SVG z Commons") }
        OutlinedButton(
            enabled = busy == null && readCommonsCandidates(context).isNotEmpty(),
            onClick = {
                busy = "resume"
                progressCurrent = 0
                progressTotal = 0
                status = "Wznawianie pobierania niezapisanych grafik…"
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val candidates = readCommonsCandidates(context)
                            processCommonsCandidates(context, candidates, { completed, total, title ->
                                withContext(Dispatchers.Main) {
                                    progressCurrent = completed
                                    progressTotal = total
                                    status = if (completed < total) {
                                        "Wznawianie SVG ${completed + 1}/$total: $title"
                                    } else {
                                        "Zakończono wznowienie SVG $total/$total"
                                    }
                                }
                            }, pauseBetweenDownloadsMs.toLongOrNull()?.coerceIn(0L, 60000L) ?: 1000L)
                        }
                    }
                    status = result.fold(
                        { "Wznowienie: znaleziono ${it.found}, pobrano ${it.downloaded}, już było ${it.alreadyPresent}, błędy ${it.failed}; prób ${it.attempts}." },
                        { "Błąd wznowienia: ${it.message ?: "nieznany błąd"}" }
                    )
                    reportSummary = loadCommonsReportSummary(context)
                    busy = null
                    if ((result.getOrNull()?.downloaded ?: 0) > 0) onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy == "resume") "Wznawianie…" else "Wznów nieudane / brakujące pobrania") }
        OutlinedButton(enabled = busy == null, onClick = { diagnosticLogLauncher.launch("commons-svg-diagnostic.log") }, modifier = Modifier.fillMaxWidth()) {
            Text("Eksportuj log wyszukiwania SVG")
        }
        OutlinedButton(enabled=busy==null && DocumentBrandStore.list(context).any { it.file.endsWith(".svg.enc") || (it.file.startsWith("builtin:") && it.file.endsWith(".svg")) },
            onClick={ zipLauncher.launch("institutional-svg-assets.zip") },modifier=Modifier.fillMaxWidth()) {
            Text(if(busy=="zip") "Tworzenie ZIP…" else "Spakuj pobrane SVG do ZIP")
        }
        if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        if (reportSummary.isNotBlank()) Text(reportSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
