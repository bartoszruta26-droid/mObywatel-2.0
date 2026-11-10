// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class BrandAsset(val id: String, val owner: String, val kind: String, val label: String, val file: String)

private object DocumentBrandStore {
    private const val ALIAS = "document_brand_assets_aes"
    private const val INDEX = "document-brand-assets.enc"
    private const val MAX_IMAGE = 12 * 1024 * 1024
    private val kinds = listOf("Fotografia", "Flaga", "Godło", "Herb", "Logo", "Pieczęć", "Wzór podpisu odręcznego", "Tło dokumentu", "Znak wodny", "Grafika hologramu / zabezpieczenia")
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
    @Synchronized fun list(context: Context): List<BrandAsset> = runCatching {
        val f = File(dir(context), INDEX); if (!f.exists()) return emptyList()
        val a = JSONArray(String(crypt(f.readBytes(), false), Charsets.UTF_8))
        (0 until a.length()).map { i -> a.getJSONObject(i).let { o -> BrandAsset(o.getString("id"), o.getString("owner"), o.getString("kind"), o.getString("label"), o.getString("file")) } }
    }.getOrDefault(emptyList())
    private fun save(context: Context, assets: List<BrandAsset>) {
        val a = JSONArray()
        assets.forEach { x -> a.put(JSONObject().apply { put("id",x.id); put("owner",x.owner); put("kind",x.kind); put("label",x.label); put("file",x.file) }) }
        File(dir(context), INDEX).writeBytes(crypt(a.toString().toByteArray(Charsets.UTF_8), true))
    }
    @Synchronized fun import(context: Context, uri: Uri, owner: String, kind: String, label: String) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
            while (true) { val n=input.read(buffer); if(n<0) break; total+=n; require(total<=MAX_IMAGE) { "Plik przekracza limit 12 MB." }; out.write(buffer,0,n) }
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        require(BitmapFactory.decodeByteArray(bytes,0,bytes.size) != null) { "Wybierz prawidłowy obraz PNG, JPEG lub WebP." }
        val id=UUID.randomUUID().toString(); val file=id+".asset.enc"
        File(dir(context),file).writeBytes(crypt(bytes,true))
        save(context,list(context)+BrandAsset(id,owner.trim(),kind,label.trim().ifBlank { kind },file))
    }
    fun image(context: Context, owner: String, kind: String) : android.graphics.Bitmap? = runCatching {
        val asset=list(context).lastOrNull { it.owner.equals(owner,true) && it.kind==kind } ?: return null
        val bytes=crypt(File(dir(context),asset.file).readBytes(),false)
        BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }.getOrNull()
    @Synchronized fun delete(context: Context, id: String) {
        val old=list(context); old.firstOrNull { it.id==id }?.let { File(dir(context),it.file).delete() }
        save(context,old.filterNot { it.id==id })
    }
}

private fun defaultDocumentBackground(context: Context, title: String): Bitmap? {
    val normalized = title.lowercase(Locale.ROOT)
    val resourceId = when {
        normalized.contains("szczep") || normalized.contains("vaccin") ->
            R.drawable.bg_document_vaccination_booklet
        normalized.contains("niepełnospraw") || normalized.contains("niepelnospraw") ||
            normalized.contains("orzeczenie") ->
            R.drawable.bg_document_disability_certificate
        normalized.contains("misjon") || normalized.contains("papiesk") ||
            normalized.contains("kościel") || normalized.contains("kosciel") ->
            R.drawable.bg_document_missionary_pdm
        normalized.contains("dowód") || normalized.contains("dowod") ||
            normalized.contains("tożsamo") || normalized.contains("tozsamos") ||
            normalized.contains("cywil") ->
            R.drawable.bg_document_civic_identity
        else -> 0
    }
    return if (resourceId == 0) null else BitmapFactory.decodeResource(context.resources, resourceId)
}

@Composable
internal fun DocumentIdentityTemplate(
    title: String,
    values: Map<String, String>,
    owner: String = "",
    photoOverride: Bitmap? = null,
    validity: String = values.entries.firstOrNull { it.key.contains("status", true) || it.key.contains("waż", true) }?.value.orEmpty()
) {
    val context = LocalContext.current
    val effectiveOwner = owner.ifBlank { values.entries.firstOrNull { it.key.contains("wystawc", true) || it.key.contains("nazwa", true) || it.key.contains("podmiot", true) || it.key.contains("kościół", true) || it.key.contains("rodu", true) }?.value.orEmpty() }
    val photo = remember(effectiveOwner, photoOverride) { photoOverride ?: DocumentBrandStore.image(context, effectiveOwner, "Fotografia") }
    val flag = remember(effectiveOwner) { DocumentBrandStore.image(context, effectiveOwner, "Flaga") }
    val emblem = remember(effectiveOwner) {
        DocumentBrandStore.image(context, effectiveOwner, "Godło")
            ?: DocumentBrandStore.image(context, effectiveOwner, "Herb")
            ?: DocumentBrandStore.image(context, effectiveOwner, "Logo")
    }
    val background = remember(effectiveOwner, title) { DocumentBrandStore.image(context, effectiveOwner, "Tło dokumentu") ?: defaultDocumentBackground(context, title) }
    val now = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("pl", "PL")).format(Date()) }
    val name = values.entries.firstOrNull { it.key.contains("imię", true) || it.key.contains("nazw", true) || it.key.contains("posiadacz", true) || it.key.contains("osoba", true) }?.value.orEmpty()
    val number = values.entries.firstOrNull { it.key.contains("numer", true) || it.key.contains("znak", true) || it.key.contains("nr ", true) }?.value.orEmpty()
    val expiry = values.entries.firstOrNull { it.key.contains("ważn", true) || it.key.contains("do dnia", true) || it.key.contains("obowiązywania do", true) }?.value.orEmpty()
    val parsedExpiry = runCatching {
        listOf(
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        ).firstNotNullOfOrNull { formatter -> runCatching { java.time.LocalDate.parse(expiry, formatter) }.getOrNull() }
    }.getOrNull()
    val expired = parsedExpiry?.isBefore(java.time.LocalDate.now()) == true
    val state = when {
        validity.contains("nieważ", true) || validity.contains("unvalid", true) || expired -> "Dokument nieważny"
        validity.contains("aktywn", true) || validity.contains("ważny", true) || validity.contains("potwierdz", true) || validity.contains("valid", true) -> "Ważność potwierdzona"
        else -> "Wymaga weryfikacji"
    }
    val stateColor = when (state) {
        "Ważność potwierdzona" -> Color(0xFFE5F4EA)
        "Dokument nieważny" -> Color(0xFFFCE8E6)
        else -> Color(0xFFFFF4D6)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Stan na: $now", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.586f).clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F6F8)).border(1.dp, Color(0xFFD5DDE5), RoundedCornerShape(14.dp))
        ) {
            if (background != null) Image(background.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.16f)
            Row(Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(0.39f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.88f)),
                        contentAlignment = Alignment.Center) {
                        if (photo != null) Image(photo.asImageBitmap(), "Fotografia osoby", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text("MIEJSCE NA\nFOTOGRAFIĘ", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                    }
                    Row(Modifier.fillMaxWidth().weight(0.34f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                            if (flag != null) Image(flag.asImageBitmap(), "Flaga", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            else Text("FLAGA", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                        }
                        Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                            if (emblem != null) Image(emblem.asImageBitmap(), "Godło, herb lub logo wystawcy", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            else Text("GODŁO /\nHERB / LOGO", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Column(Modifier.weight(0.61f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(title.uppercase(), color = Color(0xFF173B58), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                    Text(name.ifBlank { "MIEJSCE NA DANE POSIADACZA" }, color = Color(0xFF173B58), fontWeight = FontWeight.SemiBold, maxLines = 2)
                    values.entries.filter { it.value.isNotBlank() }.take(3).forEach { (key, value) ->
                        Text("$key: $value", color = Color(0xFF34495E), style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("WZÓR DOKUMENTU / EWIDENCJA", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = stateColor)) {
            Text(
                when (state) {
                    "Ważność potwierdzona" -> "✓ Ważność potwierdzona"
                    "Dokument nieważny" -> "✕ Dokument nieważny"
                    else -> "⚠ Wymaga weryfikacji"
                },
                Modifier.padding(12.dp), color = Color(0xFF26384A), fontWeight = FontWeight.Bold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6F9))) {
                Column(Modifier.padding(10.dp)) {
                    Text("Numer dokumentu", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                    Text(number.ifBlank { "Nie podano" }, color = Color(0xFF173B58), fontWeight = FontWeight.SemiBold)
                }
            }
            Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6F9))) {
                Column(Modifier.padding(10.dp)) {
                    Text("Data ważności", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
                    Text(expiry.ifBlank { "Nie podano" }, color = Color(0xFF173B58), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text("Ostatnia aktualizacja: $now", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun DocumentBrandAssetsScreen(onBack: () -> Unit) {
    val context= LocalContext.current
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
        Text("Dodawaj osobne zestawy znaków dla każdego prowadzonego podmiotu, Kościoła, Rodu, firmy lub działalności zawodowej. Pliki są przechowywane lokalnie i szyfrowane.", style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(owner,{owner=it},label={Text("Podmiot / właściciel zestawu")},modifier=Modifier.fillMaxWidth())
        Text("Rodzaj zasobu",fontWeight=FontWeight.SemiBold)
        DocumentBrandStore.assetKinds().forEach { k ->
            Row(Modifier.fillMaxWidth().clickable { kind=k }, horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                RadioButton(selected=kind==k,onClick={kind=k})
                Text(k, modifier=Modifier.padding(top=12.dp))
            }
        }
        OutlinedTextField(label,{label=it},label={Text("Opis / nazwa pliku (opcjonalnie)")},modifier=Modifier.fillMaxWidth())
        Button(onClick={picker.launch(arrayOf("image/png","image/jpeg","image/webp"))},enabled=owner.isNotBlank(),modifier=Modifier.fillMaxWidth()) { Text("Wgraj wybrany zasób") }
        if(status.isNotBlank()) Text(status,color=Color(0xFF1769AA))
        HorizontalDivider()
        Text("Zapisane zasoby",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        assets.forEach { asset ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(asset.kind,fontWeight=FontWeight.SemiBold)
                    Text(asset.owner+" · "+asset.label,style=MaterialTheme.typography.bodySmall)
                    TextButton(onClick={DocumentBrandStore.delete(context,asset.id);assets=DocumentBrandStore.list(context)}) { Text("Usuń zasób") }
                }
            }
        }
        Text("Hologram i znak wodny w eksporcie są elementami graficznymi. Nie stanowią zabezpieczenia antyfałszerskiego ani urzędowego poświadczenia. Wgrywaj wyłącznie znaki, do których używania masz uprawnienie.",style=MaterialTheme.typography.labelSmall,color=Color(0xFF9A5B00))
    }
}

@Composable
internal fun DocumentPdfExportButton(title: String, values: Map<String,String>, modifier: Modifier = Modifier) {
    val context= LocalContext.current
    var owner by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val create= rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if(uri!=null) runCatching { DocumentPdfRenderer.write(context,uri,title,owner,values) }
            .onSuccess { status="Utworzono PDF. Plik można teraz podpisać obsługiwanym podpisem elektronicznym." }
            .onFailure { status="Eksport PDF nie powiódł się: "+(it.message ?: "błąd") }
    }
    OutlinedTextField(owner,{owner=it},label={Text("Zestaw identyfikacji podmiotu (nazwa)")},modifier=Modifier.fillMaxWidth())
    Button(onClick={create.launch(title.replace(Regex("[^A-Za-z0-9_-]"),"_").take(50).ifBlank { "dokument" }+".pdf")},modifier=modifier.fillMaxWidth()) { Text("Eksportuj dokument do PDF") }
    if(status.isNotBlank()) Text(status,style=MaterialTheme.typography.bodySmall,color=Color(0xFF1769AA))
}

private object DocumentPdfRenderer {
    fun write(context: Context, uri: Uri, title: String, owner: String, values: Map<String,String>) {
        val pdf = PdfDocument()
        // Landscape 1.586:1 — stały podział: 39% identyfikacja, 61% dane.
        val pageW = 842; val pageH = 531; val margin = 28f
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(35,48,60); textSize=10.5f }
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(20,45,78); textSize=18f; typeface=Typeface.create("sans-serif",Typeface.BOLD) }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(95,108,120); textSize=8f }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(210,220,230); strokeWidth=0.7f }
        val photo = DocumentBrandStore.image(context,owner,"Fotografia")
        val flag = DocumentBrandStore.image(context,owner,"Flaga")
        val logo = DocumentBrandStore.image(context,owner,"Logo")
        val crest = DocumentBrandStore.image(context,owner,"Herb") ?: DocumentBrandStore.image(context,owner,"Godło")
        val seal = DocumentBrandStore.image(context,owner,"Pieczęć")
        val signature = DocumentBrandStore.image(context,owner,"Wzór podpisu odręcznego")
        val background = DocumentBrandStore.image(context,owner,"Tło dokumentu") ?: defaultDocumentBackground(context,title)
        val watermark = DocumentBrandStore.image(context,owner,"Znak wodny") ?: DocumentBrandStore.image(context,owner,"Grafika hologramu / zabezpieczenia")
        var pageNo = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 115f
        fun drawFit(bitmap: Bitmap?, rect: Rect, alpha: Int = 255) {
            val target = canvas ?: return
            if (bitmap == null) return
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha=alpha; isFilterBitmap=true }
            val scale = minOf(rect.width().toFloat()/bitmap.width, rect.height().toFloat()/bitmap.height)
            val width=(bitmap.width*scale).toInt(); val height=(bitmap.height*scale).toInt()
            val dst=Rect(rect.centerX()-width/2,rect.centerY()-height/2,rect.centerX()+width/2,rect.centerY()+height/2)
            target.drawBitmap(bitmap,Rect(0,0,bitmap.width,bitmap.height),dst,paint)
        }
        fun startPage() {
            pageNo++
            page=pdf.startPage(PdfDocument.PageInfo.Builder(pageW,pageH,pageNo).create())
            canvas=page!!.canvas
            val c=canvas!!
            c.drawColor(android.graphics.Color.WHITE)
            background?.let { c.drawBitmap(it,null,Rect(0,0,pageW,pageH),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=48 }) }
            watermark?.let { c.drawBitmap(it,null,Rect(230,120,610,500),Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha=20 }) }
            val split=(pageW*0.39f).toInt()
            c.drawRoundRect(margin,margin,split-10f,pageH-margin,16f,16f,Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(239,244,248) })
            drawFit(photo,Rect(48,48,split-28,300))
            c.drawRect(48f,310f,(split-28).toFloat(),365f,Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.WHITE })
            drawFit(flag,Rect(50,312,(split-30)/2,363))
            drawFit(crest ?: logo,Rect((split+20)/2,312,split-30,363))
            c.drawText("MIEJSCE NA FOTOGRAFIĘ · FLAGĘ · GODŁO / HERB / LOGO",48f,390f,small)
            c.drawText(owner.ifBlank { "Wystawca / podmiot" },split+12f,50f,small)
            c.drawText(title,split+12f,78f,heading)
            c.drawLine(split+12f,91f,pageW-margin,91f,line)
            c.drawText("Strona $pageNo",pageW-margin-42f,pageH-18f,small)
            y=115f
        }
        fun finishPage() {
            val c=canvas ?: return
            val split=(pageW*0.39f).toInt()
            drawFit(seal,Rect(pageW-130,pageH-80,pageW-70,pageH-25),220)
            drawFit(signature,Rect(split+12,pageH-72,split+150,pageH-28))
            c.drawText("PODGLĄD EWIDENCYJNY · status i autentyczność wymagają odrębnej weryfikacji",48f,pageH-18f,small)
            pdf.finishPage(page!!)
        }
        startPage()
        val entries=values.filterValues { it.isNotBlank() }.entries.toList()
        entries.forEach { entry ->
            val c=canvas!!
            val split=(pageW*0.39f).toInt()
            if(y>pageH-82f) { finishPage(); startPage() }
            c.drawText(entry.key.take(50),split+12f,y,small); y+=13f
            var rest=entry.value
            while(rest.isNotEmpty()) {
                if(y>pageH-72f) { finishPage(); startPage() }
                val available=(pageW-split-margin-24).toInt()
                val count=body.breakText(rest,true,available.toFloat(),null).coerceAtLeast(1)
                canvas!!.drawText(rest.substring(0,count),split+12f,y,body)
                rest=rest.substring(count).trimStart(); y+=14f
            }
            y+=5f; canvas!!.drawLine(split+12f,y,pageW-margin,y,line); y+=10f
        }
        finishPage()
        try {
            context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
                ?: error("Nie można zapisać wybranego pliku PDF.")
        } finally { pdf.close() }
    }
}
