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

internal object DocumentBrandStore {
    private const val ALIAS = "document_brand_assets_aes"
    private const val INDEX = "document-brand-assets.enc"
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
        val pdf=PdfDocument()
        val pageW=595; val pageH=842; val margin=48f
        val body=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(35,48,60); textSize=10.5f; typeface=Typeface.create("sans-serif",Typeface.NORMAL) }
        val heading=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(20,45,78); textSize=19f; typeface=Typeface.create("sans-serif",Typeface.BOLD) }
        val small=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(95,108,120); textSize=8f }
        val line=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(210,220,230); strokeWidth=0.7f }
        val logo=DocumentBrandStore.image(context,owner,"Logo")
        val crest=DocumentBrandStore.image(context,owner,"Herb") ?: DocumentBrandStore.image(context,owner,"Godło")
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
