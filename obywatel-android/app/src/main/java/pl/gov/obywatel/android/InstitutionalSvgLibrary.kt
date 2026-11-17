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
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
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


private data class CommonsSvgCandidate(val title:String,val url:String,val page:String,val license:String,val licenseUrl:String,val artist:String,val restrictions:String,val sha256:String = "")

// Commons rate limiting: keep every API and file request sequential and deliberately slow.
private const val COMMONS_MIN_REQUEST_INTERVAL_MS = 4000L
private const val COMMONS_API_MAX_ATTEMPTS = 6
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

private fun retryAfterMillis(connection: HttpURLConnection): Long? {
    val value = connection.getHeaderField("Retry-After")?.trim() ?: return null
    value.toLongOrNull()?.let { seconds ->
        return if (seconds <= 0L) 0L else if (seconds >= 180L) 180000L else seconds * 1000L
    }
    return runCatching {
        val date = java.time.ZonedDateTime.parse(value, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
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
    val exponentialDelay = 4000L * (1L shl (attempt - 1).coerceIn(0, 6))
    return maxOf(serverDelay ?: exponentialDelay, exponentialDelay).coerceIn(4000L, 180000L)
}

private fun commonsRequest(params:String):JSONObject {
    var lastFailure: Exception? = null
    repeat(COMMONS_API_MAX_ATTEMPTS) { attempt ->
        // One global queue prevents bursts across API searches and file downloads.
        waitForCommonsRequestSlot()
        val connection=URL("https://commons.wikimedia.org/w/api.php?$params&maxlag=5").openConnection() as HttpURLConnection
        connection.connectTimeout=20000
        connection.readTimeout=30000
        connection.setRequestProperty("User-Agent","mObywatelska-2.0/1.1 (Wikimedia Commons SVG asset catalog)")
        try {
            val code = connection.responseCode
            if (code in 200..299) {
                return JSONObject(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
            }
            val retryAfterMs = retryAfterMillis(connection)
            val retryable = code == 429 || code == 503 || code == 502 || code == 504
            lastFailure = CommonsHttpException(code, retryAfterMs)
            if (!retryable || attempt + 1 >= COMMONS_API_MAX_ATTEMPTS) throw lastFailure!!
            Thread.sleep(maxOf(commonsRetryDelayMs(attempt + 2), retryAfterMs ?: 0L))
        } finally {
            connection.disconnect()
        }
    }
    throw lastFailure ?: java.io.IOException("Wikimedia Commons: nie udało się wykonać żądania API.")
}

private fun discoverCommonsOpenSvgs(limit:Int=200):List<CommonsSvgCandidate> {
    val terms = listOf(
        "coat of arms", "heraldry", "heraldic arms", "blazon", "escutcheon",
        "flag", "national flag", "city flag", "province flag", "municipal flag",
        "emblem", "national emblem", "state emblem", "civic emblem", "institutional emblem",
        "seal", "official seal", "university seal", "government seal",
        "logo", "organization logo", "government logo", "public institution logo",
        "municipal logo", "ministry logo", "police emblem", "fire service emblem",
        "university coat of arms", "hospital logo", "hospital emblem", "church coat of arms",
        "diocesan coat of arms", "province coat of arms", "city coat of arms",
        "municipal coat of arms", "county coat of arms", "country coat of arms",
        "state coat of arms", "royal coat of arms", "regional coat of arms",
        "military insignia", "heraldic crest", "government emblem",
        "herb miasta", "herb gminy", "herb powiatu", "herb województwa",
        "herb państwa", "godło państwowe", "pieczęć urzędowa", "flaga państwowa",
        "flaga miasta", "logo ministerstwa", "logo urzędu", "znak instytucji",
        "insygnia wojskowe", "herb diecezji", "herb uniwersytetu"
    )
    val titles=linkedSetOf<String>()
    for(term in terms) {
        val query=java.net.URLEncoder.encode("$term filemime:svg","UTF-8")
        var offset:String?=null
        var finished=false
        repeat(4) {
            if(finished || titles.size>=limit*5) return@repeat
            val offsetArg=offset?.let { "&sroffset="+java.net.URLEncoder.encode(it,"UTF-8") }.orEmpty()
            val response=commonsRequest("action=query&list=search&srnamespace=6&srsearch=$query&srlimit=50$offsetArg&format=json")
            val results=response.optJSONObject("query")?.optJSONArray("search") ?: JSONArray()
            for(i in 0 until results.length()) {
                val title=results.optJSONObject(i)?.optString("title").orEmpty()
                if(title.startsWith("File:") && title.endsWith(".svg",true)) titles.add(title)
            }
            offset=response.optJSONObject("continue")?.optString("sroffset")
            if(offset.isNullOrBlank()) finished=true
        }
        if(titles.size>=limit*5) break
    }
    val candidates=mutableListOf<CommonsSvgCandidate>()
    for(batch in titles.toList().chunked(40)) {
        if(candidates.size>=limit) break
        val encoded=java.net.URLEncoder.encode(batch.joinToString("|"),"UTF-8")
        val response=commonsRequest("action=query&prop=imageinfo&titles=$encoded&iiprop=url|mime|extmetadata&format=json")
        val pages=response.optJSONObject("query")?.optJSONObject("pages") ?: continue
        val keys=pages.keys()
        while(keys.hasNext() && candidates.size<limit) {
            val page=pages.optJSONObject(keys.next()) ?: continue
            val title=page.optString("title")
            val info=page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
            if(info.optString("mime")!="image/svg+xml") continue
            val meta=info.optJSONObject("extmetadata") ?: JSONObject()
            fun value(key:String)=meta.optJSONObject(key)?.optString("value").orEmpty().replace(Regex("<[^>]*>")," ").replace("&nbsp;"," ").trim()
            val license=value("LicenseShortName")
            val lower=license.lowercase(java.util.Locale.ROOT)
            val attributionLicense =
                (lower.contains("cc by") || lower.contains("cc-by") ||
                    (lower.contains("creative commons") && lower.contains("attribution"))) &&
                    !lower.contains("noncommercial") && !lower.contains("non-commercial") &&
                    !lower.contains("by-nc") && !lower.contains("by nc") &&
                    !lower.contains("by-nd") && !lower.contains("by nd") &&
                    !lower.contains("no derivatives") && !lower.contains("no-derivatives")
            val permissiveOpen = lower.contains("cc0") || lower.contains("public domain") ||
                lower.contains("public-domain") || lower.contains("gfdl") ||
                lower.contains("free art license") || lower.contains("lgpl") || lower.contains("gpl")
            val ccOpen = attributionLicense
            if(!ccOpen && !permissiveOpen) continue
            val url=info.optString("url")
            if(!url.startsWith("https://upload.wikimedia.org/")) continue
            val pageUrl="https://commons.wikimedia.org/wiki/"+java.net.URLEncoder.encode(title.replace(' ','_'),"UTF-8")
            candidates.add(CommonsSvgCandidate(title,url,pageUrl,license,value("LicenseUrl"),value("Artist"),value("Restrictions")))
        }
    }
    return candidates.distinctBy { it.title }.take(limit)
}

private fun downloadCommonsSvg(context:Context,item:CommonsSvgCandidate): String {
    waitForCommonsRequestSlot()
    val connection=URL(item.url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects=true
    connection.connectTimeout=15000
    connection.readTimeout=25000
    connection.setRequestProperty("User-Agent","mObywatelska-2.0/1.0")
    val bytes=try {
        val code = connection.responseCode
        if (code !in 200..299) {
            val retryable = code == 429 || code == 503 || code == 502 || code == 504
            if (retryable) throw CommonsHttpException(code, retryAfterMillis(connection))
            throw java.io.IOException("HTTP $code")
        }
        connection.inputStream.use { input ->
            val output=ByteArrayOutputStream()
            val buffer=ByteArray(8192)
            var total=0
            while(true) { val n=input.read(buffer); if(n<0) break; total+=n; require(total<=12*1024*1024) { "Plik przekracza limit 12 MB." }; output.write(buffer,0,n) }
            output.toByteArray()
        }
    } finally { connection.disconnect() }
    validateSvg(bytes)
    val id="commons_"+MessageDigest.getInstance("SHA-256").digest(item.title.toByteArray(Charsets.UTF_8)).take(16).joinToString("") { "%02x".format(it) }
    val sha=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    val rights=JSONObject().put("source","Wikimedia Commons").put("sourcePage",item.page).put("downloadUrl",item.url)
        .put("license",item.license).put("licenseUrl",item.licenseUrl).put("author",item.artist)
        .put("restrictions",item.restrictions).put("sha256",sha)
        .put("verificationStatus","open-license-metadata-found; trademark-and-use-review-required")
        .put("permittedKinds",JSONArray()).toString()
    val title=item.title.removePrefix("File:")
    val kind=when {
        title.contains("flag",true)->"Flaga"
        title.contains("coat of arms",true)||title.contains("herald",true)->"Herb"
        title.contains("seal",true)->"Pieczęć"
        title.contains("emblem",true)->"Godło"
        else->"Logo"
    }
    DocumentBrandStore.importBytes(context,"Wikimedia Commons",kind,title.take(180),bytes,rights=rights,mimeType="image/svg+xml",assetId=id)
    return sha
}


private const val COMMONS_DOWNLOAD_LIST_FILE = "commons-svg-download-list.json"
private const val COMMONS_DOWNLOAD_REPORT_FILE = "commons-svg-download-report.json"

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
    onProgress: suspend (completed: Int, total: Int, currentTitle: String) -> Unit
): CommonsDownloadSummary {
    var downloaded = 0
    var alreadyPresent = 0
    var failed = 0
    var attempts = 0
    val errors = mutableListOf<String>()
    val knownAssets = DocumentBrandStore.list(context).associateBy { it.id }
    if (candidates.isEmpty()) {
        saveCommonsReport(context, 0, 0, 0, 0, 0, "", emptyList())
        return CommonsDownloadSummary(0, 0, 0, 0, 0)
    }
    candidates.forEachIndexed { index, candidate ->
        onProgress(index, candidates.size, candidate.title)
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
        saveCommonsReport(context, candidates.size, downloaded, alreadyPresent, failed,
            attempts, candidate.title, errors)
        onProgress(index + 1, candidates.size, candidate.title)
    }
    return CommonsDownloadSummary(candidates.size, downloaded, alreadyPresent, failed, attempts)
}

@Composable
internal fun InstitutionalSvgLibraryPanel(context:Context,onSaved:()->Unit) {
    val scope= rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
    var progressCurrent by remember { mutableIntStateOf(0) }
    var progressTotal by remember { mutableIntStateOf(0) }
    var reportSummary by remember { mutableStateOf(loadCommonsReportSummary(context)) }
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
        Text("Wyszukiwanie i pobieranie do 200 plików SVG z Wikimedia Commons. Pobrane grafiki są automatycznie zapisywane w zasobach aplikacji wraz z informacją o źródle, autorstwie, licencji i ograniczeniach.",style=MaterialTheme.typography.bodySmall)
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
        OutlinedButton(
            enabled = busy == null,
            onClick = {
                busy = "commons"
                progressCurrent = 0
                progressTotal = 0
                status = "Wyszukiwanie do 200 otwartych plików SVG w Wikimedia Commons…"
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val candidates = discoverCommonsOpenSvgs(200)
                            val manifest = JSONArray()
                            candidates.forEach { item ->
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
                            processCommonsCandidates(context, candidates) { completed, total, title ->
                                withContext(Dispatchers.Main) {
                                    progressCurrent = completed
                                    progressTotal = total
                                    status = if (completed < total) {
                                        "Pobieranie SVG ${completed + 1}/$total: $title"
                                    } else {
                                        "Zakończono pobieranie SVG $total/$total"
                                    }
                                }
                            }
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
        ) { Text(if (busy == "commons") "Wyszukiwanie i pobieranie…" else "Znajdź i pobierz do 200 otwartych SVG z Commons") }
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
                            processCommonsCandidates(context, candidates) { completed, total, title ->
                                withContext(Dispatchers.Main) {
                                    progressCurrent = completed
                                    progressTotal = total
                                    status = if (completed < total) {
                                        "Wznawianie SVG ${completed + 1}/$total: $title"
                                    } else {
                                        "Zakończono wznowienie SVG $total/$total"
                                    }
                                }
                            }
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
        OutlinedButton(enabled=busy==null && DocumentBrandStore.list(context).any { it.file.endsWith(".svg.enc") || (it.file.startsWith("builtin:") && it.file.endsWith(".svg")) },
            onClick={ zipLauncher.launch("institutional-svg-assets.zip") },modifier=Modifier.fillMaxWidth()) {
            Text(if(busy=="zip") "Tworzenie ZIP…" else "Spakuj pobrane SVG do ZIP")
        }
        if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        if (reportSummary.isNotBlank()) Text(reportSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
