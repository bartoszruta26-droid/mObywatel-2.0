// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class BrandAsset(val id: String, val owner: String, val kind: String, val label: String, val file: String)
internal data class BrandAssignment(val documentType: String, val kind: String, val assetId: String)

private val documentBrandTargets = listOf(
    "identity" to "Dowód tożsamości",
    "passport" to "Paszport",
    "parliament" to "Legitymacja poselska",
    "training" to "Legitymacja uczestnika kursu",
    "student" to "Legitymacja ucznia / studenta",
    "church" to "Dokumenty i legitymacje kościelne",
    "church_bishop" to "Godność biskupia",
    "church_archbishop" to "Godność arcybiskupia",
    "church_cardinal" to "Godność kardynalska",
    "church_abbot" to "Godność opacka",
    "church_minister" to "Posługa / urząd kościelny",
    "professional" to "Dokumenty kwalifikacji zawodowych",
    "professional_medical" to "Godność / kwalifikacja medyczna",
    "professional_education" to "Godność / kwalifikacja edukacyjna",
    "professional_therapeutic" to "Godność / kwalifikacja terapeutyczna",
    "professional_other" to "Pozostałe godności zawodowe",
    "organization_foundation" to "Fundacja",
    "organization_church" to "Kościół / wspólnota",
    "organization_company" to "Przedsiębiorstwo / osoba prawna",
    "organization_association" to "Stowarzyszenie / organizacja społeczna",
    "family" to "Dokumenty rodowe",
    "entity" to "Dokumenty organizacji i podmiotów",
    "official" to "Pozostałe dokumenty urzędowe"
)

internal object DocumentBrandStore {
    private const val ALIAS = "document_brand_assets_aes"
    private const val INDEX = "document-brand-assets.enc"
    private const val ASSIGNMENTS_INDEX = "document-brand-assignments.enc"
    private const val MAX_IMAGE = 12 * 1024 * 1024
    private val kinds = listOf("Logo", "Godło", "Herb", "Flaga", "Pieczęć", "Wzór podpisu odręcznego", "Tło dokumentu", "Znak wodny", "Grafika hologramu / zabezpieczenia")
    fun assetKinds() = kinds
    private fun dir(context: Context) = File(context.noBackupFilesDir, "document-brand-assets").apply { mkdirs() }
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(android.security.keystore.KeyGenParameterSpec.Builder(ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        if (encrypt) { cipher.init(Cipher.ENCRYPT_MODE, key()); return cipher.iv + cipher.doFinal(bytes) }
        require(bytes.size > 28)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }
    private val builtInAssets = listOf(
        BrandAsset("builtin_ciechomscy", "Katalog heraldyczny", "Herb", "Ciechomscy — wąż i jabłko", "builtin:heraldry/ciechomscy-wez-jablko.svg"),
        BrandAsset("builtin_rodu_dab", "Katalog heraldyczny", "Herb", "Herb rodowy — dąb", "builtin:heraldry/herb-rodu-klasyczny.svg"),
        BrandAsset("builtin_rodu_lilia", "Katalog heraldyczny", "Herb", "Herb rodowy — lilia", "builtin:heraldry/herb-rodu-lilia.svg"),
        BrandAsset("builtin_biskupi", "Katalog heraldyczny", "Herb", "Herb biskupi", "builtin:heraldry/herb-biskupi.svg"),
        BrandAsset("builtin_arcybiskupi", "Katalog heraldyczny", "Herb", "Herb arcybiskupi", "builtin:heraldry/herb-arcybiskupi.svg"),
        BrandAsset("builtin_kardynalski", "Katalog heraldyczny", "Herb", "Herb kardynalski", "builtin:heraldry/herb-kardynalski.svg"),
        BrandAsset("builtin_opacki", "Katalog heraldyczny", "Herb", "Herb opacki", "builtin:heraldry/herb-opacki.svg"),
        BrandAsset("builtin_rycerski", "Katalog heraldyczny", "Herb", "Herb rycerski", "builtin:heraldry/herb-rycerski.svg"),
        BrandAsset("builtin_medyczny", "Katalog heraldyczny", "Logo", "Znak medyczny", "builtin:heraldry/herb-medyczny.svg"),
        BrandAsset("builtin_naukowy", "Katalog heraldyczny", "Logo", "Znak naukowy", "builtin:heraldry/herb-naukowy.svg"),
        BrandAsset("builtin_fundacyjny", "Katalog heraldyczny", "Logo", "Znak fundacyjny", "builtin:heraldry/herb-fundacyjny.svg"),
        BrandAsset("builtin_misjonarski", "Katalog heraldyczny", "Logo", "Znak misyjny", "builtin:heraldry/herb-misjonarski.svg"),
        BrandAsset("builtin_samorzadowy", "Katalog heraldyczny", "Godło", "Znak samorządowy", "builtin:heraldry/herb-samorzadowy.svg"),
        BrandAsset("builtin_zaslugi", "Katalog heraldyczny", "Herb", "Znak zasługi", "builtin:heraldry/herb-zaslugi.svg")
    )
    @Synchronized fun list(context: Context): List<BrandAsset> {
        val stored = runCatching {
            val f = File(dir(context), INDEX)
            if (!f.exists()) emptyList() else {
                val a = JSONArray(String(crypt(f.readBytes(), false), Charsets.UTF_8))
                (0 until a.length()).map { i -> a.getJSONObject(i).let { o ->
                    BrandAsset(o.getString("id"), o.getString("owner"), o.getString("kind"), o.getString("label"), o.getString("file"))
                } }
            }
        }.getOrDefault(emptyList())
        return builtInAssets + stored
    }
    private fun save(context: Context, assets: List<BrandAsset>) {
        val a = JSONArray()
        assets.forEach { x -> a.put(JSONObject().apply { put("id",x.id); put("owner",x.owner); put("kind",x.kind); put("label",x.label); put("file",x.file) }) }
        File(dir(context), INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    @Synchronized fun assignments(context: Context): List<BrandAssignment> = runCatching {
        val f = File(dir(context), ASSIGNMENTS_INDEX)
        if (!f.exists()) return emptyList()
        val a = JSONArray(String(crypt(f.readBytes(), false), Charsets.UTF_8))
        (0 until a.length()).map { i -> a.getJSONObject(i).let { o ->
            BrandAssignment(o.getString("documentType"), o.getString("kind"), o.getString("assetId"))
        } }
    }.getOrDefault(emptyList())
    @Synchronized fun assign(context: Context, documentType: String, kind: String, assetId: String) {
        require(documentBrandTargets.any { it.first == documentType }) { "Nieznany rodzaj dokumentu." }
        require(kind in kinds) { "Nieznany rodzaj zasobu." }
        require(list(context).any { it.id == assetId && it.kind == kind }) { "Wybrany zasób nie istnieje lub ma inny rodzaj." }
        val next = assignments(context).filterNot { it.documentType == documentType && it.kind == kind } +
            BrandAssignment(documentType, kind, assetId)
        val a = JSONArray()
        next.forEach { x -> a.put(JSONObject().put("documentType", x.documentType).put("kind", x.kind).put("assetId", x.assetId)) }
        File(dir(context), ASSIGNMENTS_INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    @Synchronized fun unassign(context: Context, documentType: String, kind: String) {
        val a = JSONArray()
        assignments(context).filterNot { it.documentType == documentType && it.kind == kind }.forEach { x ->
            a.put(JSONObject().put("documentType", x.documentType).put("kind", x.kind).put("assetId", x.assetId))
        }
        File(dir(context), ASSIGNMENTS_INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    private fun imageById(context: Context, assetId: String): android.graphics.Bitmap? = runCatching {
        val asset = list(context).firstOrNull { it.id == assetId } ?: return null
        val bytes = if (asset.file.startsWith("builtin:")) {
            context.assets.open(asset.file.removePrefix("builtin:")).use { it.readBytes() }
        } else {
            crypt(File(dir(context), asset.file).readBytes(), false)
        }
        decodeImage(bytes)
    }.getOrNull()
    private fun assignmentKeys(documentType: String, owner: String): List<String> {
        val lower = owner.lowercase(java.util.Locale.ROOT)
        val role = when {
            documentType.startsWith("church_bishop") || documentType == "church_orders" -> "church_bishop"
            documentType.startsWith("church_archbishop") -> "church_archbishop"
            documentType.startsWith("church_cardinal") -> "church_cardinal"
            documentType.startsWith("church_abbot") -> "church_abbot"
            documentType.startsWith("church_minister") || documentType == "church_jurisdiction" -> "church_minister"
            documentType == "training" || documentType == "student" -> "professional_education"
            documentType == "professional" && listOf("lekar", "medycz", "fizjoter", "pielęg", "diagnost").any { lower.contains(it) } -> "professional_medical"
            documentType == "professional" && listOf("nauczyc", "pedagog", "wykładow", "szkol").any { lower.contains(it) } -> "professional_education"
            documentType == "professional" && listOf("terapeut", "masaż", "masaz", "naturop", "zielarz").any { lower.contains(it) } -> "professional_therapeutic"
            documentType == "professional" -> "professional_other"
            documentType == "entity" && listOf("fundac").any { lower.contains(it) } -> "organization_foundation"
            documentType == "entity" && listOf("koś", "kosci", "parafi", "wspólnot").any { lower.contains(it) } -> "organization_church"
            documentType == "entity" && listOf("spół", "spol", "firma", "przedsięb").any { lower.contains(it) } -> "organization_company"
            documentType == "entity" && listOf("stowarz", "organizac").any { lower.contains(it) } -> "organization_association"
            documentType == "family" -> "organization_family"
            else -> ""
        }
        val fallback = when {
            documentType.startsWith("church_") -> "church"
            documentType.startsWith("organization_") -> "entity"
            else -> ""
        }
        val candidates = if (documentType == "professional" || documentType == "entity")
            listOf(role, documentType, fallback)
        else listOf(documentType, role, fallback)
        return candidates.filter { it.isNotBlank() }.distinct()
    }
    fun imageForDocument(context: Context, documentType: String, owner: String, kind: String): android.graphics.Bitmap? {
        val all = assignments(context)
        val assigned = assignmentKeys(documentType, owner).firstNotNullOfOrNull { key ->
            all.lastOrNull { it.documentType == key && it.kind == kind }
        }
        return assigned?.let { imageById(context, it.assetId) } ?: image(context, owner, kind)
    }
    fun preview(context: Context, assetId: String): android.graphics.Bitmap? = imageById(context, assetId)
    private fun decodeImage(bytes: ByteArray): android.graphics.Bitmap {
        BitmapFactory.decodeByteArray(bytes,0,bytes.size)?.let { return it }
        val svg = com.caverock.androidsvg.SVG.getFromString(String(bytes, Charsets.UTF_8))
        val picture = svg.renderToPicture()
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
        val sourceWidth = picture.width.coerceAtLeast(1).toFloat()
        val sourceHeight = picture.height.coerceAtLeast(1).toFloat()
        val scale = minOf(512f / sourceWidth, 512f / sourceHeight)
        val canvas = Canvas(bitmap)
        canvas.translate((512f - sourceWidth * scale) / 2f, (512f - sourceHeight * scale) / 2f)
        canvas.scale(scale, scale)
        canvas.drawPicture(picture)
        return bitmap
    }
    @Synchronized fun import(context: Context, uri: Uri, owner: String, kind: String, label: String) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
            while (true) { val n=input.read(buffer); if(n<0) break; total+=n; require(total<=MAX_IMAGE) { "Plik przekracza limit 12 MB." }; out.write(buffer,0,n) }
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        decodeImage(bytes).recycle()
        val isSvg = String(bytes, 0, minOf(bytes.size, 256), Charsets.UTF_8).contains("<svg", ignoreCase = true)
        val id=UUID.randomUUID().toString(); val file=id+(if (isSvg) ".svg.enc" else ".asset.enc")
        File(dir(context),file).writeBytes(crypt(bytes,true))
        save(context,list(context).filterNot { it.file.startsWith("builtin:") }+BrandAsset(id,owner.trim(),kind,label.trim().ifBlank { kind },file))
    }
    fun image(context: Context, owner: String, kind: String) : android.graphics.Bitmap? = runCatching {
        val asset=list(context).lastOrNull { it.owner.equals(owner,true) && it.kind==kind } ?: return null
        val bytes = if (asset.file.startsWith("builtin:")) {
            context.assets.open(asset.file.removePrefix("builtin:")).use { it.readBytes() }
        } else crypt(File(dir(context),asset.file).readBytes(),false)
        decodeImage(context, bytes)
    }.getOrNull()
    @Synchronized fun delete(context: Context, id: String) {
        val old=list(context)
        val selected=old.firstOrNull { it.id==id } ?: return
        if (selected.file.startsWith("builtin:")) return
        File(dir(context),selected.file).delete()
        save(context,old.filterNot { it.id==id || it.file.startsWith("builtin:") })
    }
}

@Composable
internal fun AssignedDocumentBrandPreview(documentType: String, owner: String, title: String, subtitle: String = "") {
    val context = LocalContext.current
    val brand = remember(documentType, owner) {
        Pair(
            DocumentBrandStore.imageForDocument(context, documentType, owner, "Logo"),
            DocumentBrandStore.imageForDocument(context, documentType, owner, "Herb")
                ?: DocumentBrandStore.imageForDocument(context, documentType, owner, "Godło")
        )
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (brand.first != null) Image(brand.first!!.asImageBitmap(), contentDescription = "Logo przypisane do dokumentu", modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
            if (brand.second != null) Image(brand.second!!.asImageBitmap(), contentDescription = "Herb lub godło przypisane do dokumentu", modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF173B58))
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
                if (brand.first == null && brand.second == null) Text("Brak przypisanego logo, herbu lub godła", style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
            }
        }
    }
}

@Composable
internal fun DocumentBrandAssetsScreen(onBack: () -> Unit) {
    val context= LocalContext.current
    var target by remember { mutableStateOf(documentBrandTargets.first().first) }
    var targetMenuExpanded by remember { mutableStateOf(false) }
    var assignments by remember { mutableStateOf(DocumentBrandStore.assignments(context)) }
    var owner by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(DocumentBrandStore.assetKinds().first()) }
    var label by remember { mutableStateOf("") }
    var assets by remember { mutableStateOf(DocumentBrandStore.list(context)) }
    var status by remember { mutableStateOf("") }
    val picker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) runCatching { DocumentBrandStore.import(context,uri,owner,kind,label) }
            .onSuccess { assets=DocumentBrandStore.list(context); status="Zapisano zasób identyfikacji wizualnej." }
            .onFailure { status="Nie udało się zapisać pliku: "+(it.message ?: "błąd") }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color=Color(0xFF1769AA), modifier=Modifier.clickable(onClick=onBack).padding(vertical=5.dp))
        Text("Zasoby identyfikacji dokumentów", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text("Dodawaj osobne zestawy znaków dla każdego podmiotu, Rodu, Kościoła i zawodu. Możesz przypisywać logo, godło i herb do dokumentów oraz godności. Obsługiwane są SVG, PNG, JPEG i WebP; pliki są przechowywane lokalnie i szyfrowane.", style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(owner,{owner=it},label={Text("Podmiot / właściciel zestawu")},modifier=Modifier.fillMaxWidth())
        Text("Przypisanie do dokumentu",fontWeight=FontWeight.SemiBold)
        Box {
            OutlinedButton(onClick={targetMenuExpanded=true},modifier=Modifier.fillMaxWidth()) {
                Text(documentBrandTargets.firstOrNull { it.first==target }?.second ?: target)
            }
            DropdownMenu(expanded=targetMenuExpanded,onDismissRequest={targetMenuExpanded=false}) {
                documentBrandTargets.forEach { (id,title) ->
                    DropdownMenuItem(text={Text(title)},onClick={target=id;targetMenuExpanded=false})
                }
            }
        }
        Text("Rodzaj zasobu",fontWeight=FontWeight.SemiBold)
        DocumentBrandStore.assetKinds().forEach { k ->
            Row(Modifier.fillMaxWidth().clickable { kind=k }, horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                RadioButton(selected=kind==k,onClick={kind=k})
                Text(k, modifier=Modifier.padding(top=12.dp))
            }
        }
        OutlinedTextField(label,{label=it},label={Text("Opis / nazwa pliku (opcjonalnie)")},modifier=Modifier.fillMaxWidth())
        Button(onClick={picker.launch(arrayOf("image/png","image/jpeg","image/webp","image/svg+xml"))},enabled=owner.isNotBlank(),modifier=Modifier.fillMaxWidth()) { Text("Wgraj wybrany zasób") }
        if(status.isNotBlank()) Text(status,color=Color(0xFF1769AA))
        HorizontalDivider()
        Text("Zapisane zasoby",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        assignments.forEach { assignment ->
            val asset = assets.firstOrNull { it.id == assignment.assetId }
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Przypisanie: ${documentBrandTargets.firstOrNull { it.first==assignment.documentType }?.second ?: assignment.documentType} · ${assignment.kind} · ${asset?.label ?: "brak zasobu"}", style=MaterialTheme.typography.bodySmall, modifier=Modifier.weight(1f))
                TextButton(onClick={ DocumentBrandStore.unassign(context,assignment.documentType,assignment.kind); assignments=DocumentBrandStore.assignments(context) }) { Text("Odłącz") }
            }
        }
        assets.forEach { asset ->
            val assetPreview = remember(asset.id) { DocumentBrandStore.preview(context,asset.id) }
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    if (assetPreview != null) Image(assetPreview.asImageBitmap(), contentDescription=asset.label, modifier=Modifier.size(54.dp), contentScale=ContentScale.Fit)
                    Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(asset.kind,fontWeight=FontWeight.SemiBold)
                    Text(asset.owner+" · "+asset.label,style=MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick={
                            runCatching { DocumentBrandStore.assign(context,target,asset.kind,asset.id) }
                                .onSuccess { assignments=DocumentBrandStore.assignments(context); status="Przypisano ${asset.kind} do wybranego rodzaju dokumentu." }
                                .onFailure { status="Nie udało się przypisać zasobu: ${it.message}" }
                        }) { Text("Przypisz do dokumentu") }
                        if (asset.file.startsWith("builtin:")) {
                            Text("Wbudowany SVG", color=Color(0xFF667788), style=MaterialTheme.typography.labelSmall, modifier=Modifier.padding(top=10.dp))
                        } else TextButton(onClick={
                            DocumentBrandStore.delete(context,asset.id)
                            assets=DocumentBrandStore.list(context)
                            assignments=DocumentBrandStore.assignments(context)
                        }) { Text("Usuń zasób") }
                    }
                    }
                }
            }
        }
        Text("Hologram i znak wodny w eksporcie są elementami graficznymi. Nie stanowią zabezpieczenia antyfałszerskiego ani urzędowego poświadczenia. Wgrywaj wyłącznie znaki, do których używania masz uprawnienie.",style=MaterialTheme.typography.labelSmall,color=Color(0xFF9A5B00))
    }
}

@Composable
internal fun DocumentPdfExportButton(title: String, values: Map<String,String>, modifier: Modifier = Modifier, documentType: String = "official", ownerDefault: String = "") {
    val context= LocalContext.current
    var owner by remember(title, ownerDefault) { mutableStateOf(ownerDefault) }
    var status by remember { mutableStateOf("") }
    val create= rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if(uri!=null) runCatching { DocumentPdfRenderer.write(context,uri,title,owner,documentType,values) }
            .onSuccess { status="Utworzono PDF. Plik można teraz podpisać obsługiwanym podpisem elektronicznym." }
            .onFailure { status="Eksport PDF nie powiódł się: "+(it.message ?: "błąd") }
    }
    OutlinedTextField(owner,{owner=it},label={Text("Zestaw identyfikacji podmiotu (nazwa)")},modifier=Modifier.fillMaxWidth())
    Button(onClick={create.launch(title.replace(Regex("[^A-Za-z0-9_-]"),"_").take(50).ifBlank { "dokument" }+".pdf")},modifier=modifier.fillMaxWidth()) { Text("Eksportuj dokument do PDF") }
    if(status.isNotBlank()) Text(status,style=MaterialTheme.typography.bodySmall,color=Color(0xFF1769AA))
}

private object DocumentPdfRenderer {
    fun write(context: Context, uri: Uri, title: String, owner: String, documentType: String, values: Map<String,String>) {
        val pdf=PdfDocument()
        val pageW=595; val pageH=842; val margin=48f
        val body=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(35,48,60); textSize=10.5f; typeface=Typeface.create("sans-serif",Typeface.NORMAL) }
        val heading=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(20,45,78); textSize=19f; typeface=Typeface.create("sans-serif",Typeface.BOLD) }
        val small=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(95,108,120); textSize=8f }
        val line=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(210,220,230); strokeWidth=0.7f }
        val logo=DocumentBrandStore.imageForDocument(context,documentType,owner,"Logo")
        val crest=DocumentBrandStore.imageForDocument(context,documentType,owner,"Herb") ?: DocumentBrandStore.imageForDocument(context,documentType,owner,"Godło")
        val seal=DocumentBrandStore.image(context,owner,"Pieczęć")
        val signature=DocumentBrandStore.image(context,owner,"Wzór podpisu odręcznego")
        val background=DocumentBrandStore.image(context,owner,"Tło dokumentu")
        val watermark=DocumentBrandStore.image(context,owner,"Znak wodny") ?: DocumentBrandStore.image(context,owner,"Grafika hologramu / zabezpieczenia")
        var pageNo=0; var y=0f; var page: PdfDocument.Page?=null
        fun startPage() {
            pageNo++
            page=pdf.startPage(PdfDocument.PageInfo.Builder(pageW,pageH,pageNo).create())
            val canvas=page!!.canvas
            background?.let { canvas.drawBitmap(it,null,Rect(0,0,pageW,pageH),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=42 }) }
            watermark?.let { canvas.save(); canvas.rotate(-28f,pageW/2f,pageH/2f); canvas.drawBitmap(it,null,Rect(95,330,500,735),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=24 }); canvas.restore() }
            logo?.let { canvas.drawBitmap(it,null,Rect(margin.toInt(),28,margin.toInt()+58,86),Paint(Paint.ANTI_ALIAS_FLAG)) }
            crest?.let { canvas.drawBitmap(it,null,Rect(pageW-105,28,pageW-48,86),Paint(Paint.ANTI_ALIAS_FLAG)) }
            canvas.drawText(owner.ifBlank { "Dokument" },margin,105f,small)
            canvas.drawText(title,margin,137f,heading)
            canvas.drawLine(margin,151f,pageW-margin,151f,line)
            y=176f
        }
        fun finishPage() {
            val c=page!!.canvas
            seal?.let { c.drawBitmap(it,null,Rect(pageW-150,pageH-125,pageW-70,pageH-45),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=220 }) }
            signature?.let { c.drawBitmap(it,null,Rect(margin.toInt(),pageH-125,margin.toInt()+130,pageH-72),Paint(Paint.ANTI_ALIAS_FLAG)) }
            c.drawLine(margin,pageH-145f,pageW-margin,pageH-145f,line)
            c.drawText("Miejsce na podpis / podpis elektroniczny · strona "+pageNo,margin,pageH-35f,small)
            pdf.finishPage(page!!)
        }
        startPage()
        val entries=values.filterValues { it.isNotBlank() }.entries.toList()
        entries.forEach { entry ->
            val label=entry.key
            val text=entry.value
            val labelPaint=Paint(body).apply { typeface=Typeface.create("sans-serif",Typeface.BOLD); color=android.graphics.Color.rgb(65,82,98) }
            if(y>pageH-180f) { finishPage(); startPage() }
            page!!.canvas.drawText(label.take(82),margin,y,labelPaint); y+=16f
            var rest=text
            while(rest.isNotEmpty()) {
                val available=(pageW-2*margin).toInt()
                val count=body.breakText(rest,true,available.toFloat(),null).coerceAtLeast(1)
                page!!.canvas.drawText(rest.substring(0,count),margin,y,body)
                rest=rest.substring(count).trimStart(); y+=14f
                if(y>pageH-180f && rest.isNotEmpty()) { finishPage(); startPage() }
            }
            y+=8f; page!!.canvas.drawLine(margin,y,pageW-margin,y,line); y+=14f
        }
        if(entries.isEmpty()) page!!.canvas.drawText("Brak wprowadzonych danych.",margin,y,body)
        finishPage()
        context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("Nie można zapisać wybranego pliku PDF.")
        pdf.close()
    }
}
