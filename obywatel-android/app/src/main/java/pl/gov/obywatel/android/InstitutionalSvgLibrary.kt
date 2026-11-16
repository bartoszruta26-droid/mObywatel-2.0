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

private data class SvgSource(val id:String,val organization:String,val label:String,val kind:String,val page:String,val url:String,val rights:String="Licencja do sprawdzenia na stronie konkretnego pliku.")

private val svgSources = listOf(
SvgSource("vatican_state_coa_2023","Państwo Watykańskie","Herb Państwa Watykańskiego (2023)","Herb","https://commons.wikimedia.org/wiki/File:Coat_of_arms_of_Vatican_City_(2023%E2%80%93present).svg","https://upload.wikimedia.org/wikipedia/commons/3/3a/Coat_of_arms_of_Vatican_City_%282023%E2%80%93present%29.svg","Domena publiczna wskazana na stronie pliku; załącznik B ustawy zasadniczej 2023."),
SvgSource("holy_see_emblem_vatican_va","Stolica Apostolska","Emblemat Stolicy Apostolskiej (Vatican.va)","Godło","https://commons.wikimedia.org/wiki/File:Emblem_of_the_Holy_See_(vatican.va).svg","https://upload.wikimedia.org/wikipedia/commons/d/d4/Emblem_of_the_Holy_See_%28vatican.va%29.svg","CC0 wskazane na stronie pliku; emblemat publikowany przez Vatican.va."),
SvgSource("un_flag","ONZ","Flaga ONZ","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_United_Nations.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_United_Nations.svg"),
SvgSource("who_flag","WHO","Flaga WHO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_WHO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_WHO.svg"),
SvgSource("unesco_flag","UNESCO","Flaga UNESCO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_UNESCO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_UNESCO.svg"),
SvgSource("unicef_flag","UNICEF","Flaga UNICEF","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_UNICEF.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_UNICEF.svg"),
SvgSource("wfp_flag","WFP","Flaga WFP","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_World_Food_Programme.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_World_Food_Programme.svg"),
SvgSource("unido_flag","UNIDO","Flaga UNIDO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_UNIDO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_UNIDO.svg"),
SvgSource("unfccc_flag","UNFCCC","Flaga UNFCCC","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_UNFCCC.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_UNFCCC.svg"),
SvgSource("upu_flag","UPU","Flaga UPU","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_UPU.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_UPU.svg"),
SvgSource("icao_flag","ICAO","Flaga ICAO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_ICAO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_ICAO.svg"),
SvgSource("ilo_flag","ILO","Flaga ILO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_ILO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_ILO.svg"),
SvgSource("imo_flag","IMO","Flaga IMO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_International_Maritime_Organization.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_International_Maritime_Organization.svg"),
SvgSource("itu_flag","ITU","Flaga ITU","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_International_Telecommunication_Union.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_International_Telecommunication_Union.svg"),
SvgSource("nato_flag","NATO","Flaga NATO","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_NATO.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_NATO.svg"),
SvgSource("eu_flag","Unia Europejska","Flaga Unii Europejskiej","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_Europe.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_Europe.svg"),
SvgSource("coe_flag","Rada Europy","Flaga Rady Europy","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Council_of_Europe_(internet_RGB_colours).svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Council_of_Europe_(internet_RGB_colours).svg"),
SvgSource("opec_flag","OPEC","Flaga OPEC","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_OPEC.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_OPEC.svg"),
SvgSource("oas_flag","OAS","Flaga OAS","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Organization_of_American_States.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Organization_of_American_States.svg"),
SvgSource("osce_flag","OBWE","Flaga OBWE","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Organization_for_Security_and_Co-operation_in_Europe.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Organization_for_Security_and_Co-operation_in_Europe.svg"),
SvgSource("asean_flag_1984","ASEAN","Flaga ASEAN — wariant 1984 (historyczny)","Flaga","https://commons.wikimedia.org/wiki/File:ASEAN_Flag_(1984).svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/ASEAN_Flag_(1984).svg"),
SvgSource("arab_league_flag","Liga Państw Arabskich","Flaga Ligi Państw Arabskich","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Arab_League.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Arab_League.svg"),
SvgSource("caricom_flag","CARICOM","Flaga CARICOM","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_CARICOM.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_CARICOM.svg"),
SvgSource("sadc_flag","SADC","Flaga SADC","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_SADC.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_SADC.svg"),
SvgSource("eaeu_flag","Eurazjatycka Unia Gospodarcza","Flaga EAEU","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Eurasian_Economic_Union.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Eurasian_Economic_Union.svg"),
SvgSource("mercosur_flag","Mercosur","Flaga Mercosur","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_Mercosur.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_Mercosur.svg"),
SvgSource("nordic_council_flag","Rada Nordycka","Flaga Rady Nordyckiej","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Nordic_Council.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Nordic_Council.svg"),
SvgSource("oic_emblem","Organizacja Współpracy Islamskiej","Emblemat OIC","Logo","https://commons.wikimedia.org/wiki/File:OIC_Logo_since_2011.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/OIC_Logo_since_2011.svg"),
SvgSource("turkic_states_flag","Organizacja Państw Turkijskich","Flaga Organizacji Państw Turkijskich","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_Organization_of_Turkic_States.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_Organization_of_Turkic_States.svg"),
SvgSource("iaea_flag","IAEA","Flaga IAEA","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_IAEA.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_IAEA.svg"),
SvgSource("icc_flag","Międzynarodowy Trybunał Karny","Flaga MTK","Flaga","https://commons.wikimedia.org/wiki/File:Flag_of_the_International_Criminal_Court.svg","https://commons.wikimedia.org/wiki/Special:Redirect/file/Flag_of_the_International_Criminal_Court.svg")
)

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

@Composable
internal fun InstitutionalSvgLibraryPanel(context:Context,onSaved:()->Unit) {
    val scope= rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
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
        Text("Wybierz pojedynczy plik albo pobierz cały katalog. Pobrane pliki są zapisywane w szyfrowanej bibliotece aplikacji; licencja i aktualność pozostają do potwierdzenia.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(
            enabled = busy == null,
            onClick = {
                busy = "all"
                status = "Rozpoczynam pobieranie ${svgSources.size} pozycji…"
                scope.launch {
                    var downloaded = 0
                    var failed = 0
                    val errors = mutableListOf<String>()
                    svgSources.forEachIndexed { index, source ->
                        status = "Pobieranie ${index + 1}/${svgSources.size}: ${source.label}"
                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                val connection = URL(source.url).openConnection() as HttpURLConnection
                                connection.instanceFollowRedirects = true
                                connection.connectTimeout = 15000
                                connection.readTimeout = 25000
                                connection.setRequestProperty("User-Agent", "mObywatelska-2.0 SVG library")
                                val bytes = try {
                                    require(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
                                    connection.inputStream.use { input ->
                                        val out = ByteArrayOutputStream()
                                        val buffer = ByteArray(8192)
                                        var total = 0
                                        while (true) {
                                            val n = input.read(buffer)
                                            if (n < 0) break
                                            total += n
                                            require(total <= 12 * 1024 * 1024) { "Plik przekracza 12 MB." }
                                            out.write(buffer, 0, n)
                                        }
                                        out.toByteArray()
                                    }
                                } finally {
                                    connection.disconnect()
                                }
                                validateSvg(bytes)
                                val sha = MessageDigest.getInstance("SHA-256").digest(bytes)
                                    .joinToString("") { "%02x".format(it) }
                                val rights = JSONObject()
                                    .put("owner", source.organization)
                                    .put("sourceUrl", source.page)
                                    .put("downloadUrl", source.url)
                                    .put("rightsBasis", source.rights)
                                    .put("sha256", sha)
                                    .put("verificationStatus", "pending")
                                    .put("permittedKinds", JSONArray())
                                    .put("verifiedBy", "Automatyczne pobranie; kontrola prawna wymagana")
                                    .toString()
                                DocumentBrandStore.importBytes(
                                    context, source.organization, source.kind, source.label, bytes,
                                    rights = rights, mimeType = "image/svg+xml", assetId = source.id
                                )
                            }
                        }
                        if (result.isSuccess) downloaded++ else {
                            failed++
                            errors += "${source.label}: ${result.exceptionOrNull()?.message ?: "błąd"}"
                        }
                    }
                    status = "Zakończono katalog: pobrano $downloaded/${svgSources.size}, błędy: $failed. " +
                        if (errors.isEmpty()) "Wszystkie pozycje zostały zapisane." else errors.take(4).joinToString(" | ") +
                            if (errors.size > 4) " | oraz ${errors.size - 4} dalszych błędów." else ""
                    busy = null
                    if (downloaded > 0) onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy == "all") "Pobieranie całego katalogu…" else "Pobierz wszystkie ${svgSources.size} pliki SVG") }
        svgSources.forEach { source ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(source.label,fontWeight=FontWeight.Medium)
                    Text(source.organization+" · "+source.kind,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(enabled=busy==null,onClick={
                    busy=source.id; status=""
                    scope.launch {
                        val result=withContext(Dispatchers.IO) {
                            runCatching {
                                val connection=URL(source.url).openConnection() as HttpURLConnection
                                connection.instanceFollowRedirects=true
                                connection.connectTimeout=15000; connection.readTimeout=25000
                                connection.setRequestProperty("User-Agent","mObywatelska-2.0 SVG library")
                                val bytes=try {
                                    require(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
                                    connection.inputStream.use { input ->
                                        val out=ByteArrayOutputStream(); val buffer=ByteArray(8192); var total=0
                                        while(true) { val n=input.read(buffer); if(n<0) break; total+=n; require(total<=12*1024*1024) { "Plik przekracza 12 MB." }; out.write(buffer,0,n) }
                                        out.toByteArray()
                                    }
                                } finally { connection.disconnect() }
                                validateSvg(bytes)
                                val sha=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                                val rights=JSONObject().put("owner",source.organization).put("sourceUrl",source.page)
                                    .put("downloadUrl",source.url).put("rightsBasis",source.rights).put("sha256",sha)
                                    .put("verificationStatus","pending").put("permittedKinds",JSONArray())
                                    .put("verifiedBy","Automatyczne pobranie; kontrola prawna wymagana").toString()
                                DocumentBrandStore.importBytes(context,source.organization,source.kind,source.label,bytes,
                                    rights=rights,mimeType="image/svg+xml",assetId=source.id)
                            }
                        }
                        status=result.fold({ "Pobrano ${source.label}. Licencja oczekuje na weryfikację." },{ "Nie pobrano ${source.label}: ${it.message ?: "błąd"}" })
                        if(result.isSuccess) onSaved()
                        busy=null
                    }
                }) { Text(if(busy==source.id) "Pobieranie…" else "Pobierz SVG") }
            }
            HorizontalDivider()
        }
        OutlinedButton(enabled=busy==null && DocumentBrandStore.list(context).any { it.file.endsWith(".svg.enc") || (it.file.startsWith("builtin:") && it.file.endsWith(".svg")) },
            onClick={ zipLauncher.launch("institutional-svg-assets.zip") },modifier=Modifier.fillMaxWidth()) {
            Text(if(busy=="zip") "Tworzenie ZIP…" else "Spakuj pobrane SVG do ZIP")
        }
        if(status.isNotBlank()) Text(status,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)
    }
}
