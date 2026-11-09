// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.identity

import android.content.Context
import android.content.Intent
import android.print.PrintManager
import android.print.PrintDocumentAdapter
import android.print.PrintAttributes
import android.print.PrintDocumentInfo
import android.print.PageRange
import android.os.CancellationSignal
import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

private data class Org(val id:String,val name:String,val type:String,val registry:String)
private data class BrandAsset(val id:String,val orgId:String,val name:String,val category:String,val file:String,val mime:String,val rights:String)

private object IdentityStore {
    private const val PREF="owd_identity_v1"
    fun orgs(c:Context):List<Org> = runCatching {
        val a=JSONArray(c.getSharedPreferences(PREF,0).getString("orgs","[]"))
        (0 until a.length()).map { val o=a.getJSONObject(it); Org(o.getString("id"),o.getString("name"),o.getString("type"),o.optString("registry")) }
    }.getOrDefault(emptyList())
    fun assets(c:Context):List<BrandAsset> = runCatching {
        val a=JSONArray(c.getSharedPreferences(PREF,0).getString("assets","[]"))
        (0 until a.length()).map { val o=a.getJSONObject(it); BrandAsset(o.getString("id"),o.getString("org"),o.getString("name"),o.getString("category"),o.getString("file"),o.getString("mime"),o.optString("rights")) }
    }.getOrDefault(emptyList())
    fun saveOrgs(c:Context,items:List<Org>) {
        val a=JSONArray(); items.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("type",it.type).put("registry",it.registry)) }
        check(c.getSharedPreferences(PREF,0).edit().putString("orgs",a.toString()).commit())
    }
    fun saveAssets(c:Context,items:List<BrandAsset>) {
        val a=JSONArray(); items.forEach { a.put(JSONObject().put("id",it.id).put("org",it.orgId).put("name",it.name).put("category",it.category).put("file",it.file).put("mime",it.mime).put("rights",it.rights)) }
        check(c.getSharedPreferences(PREF,0).edit().putString("assets",a.toString()).commit())
    }
    fun file(c:Context,a:BrandAsset)=File(File(c.filesDir,"owd-brand-assets"),a.file)
}

private val kinds=listOf("Fundacja","Stowarzyszenie","Podmiot kościelny","Jednostka organizacyjna","Przedsiębiorstwo / spółka","Podmiot rodowy","Działalność osoby fizycznej","Inny")
private val categories=listOf("Logo","Herb","Godło","Flaga","Pieczęć","Wzór podpisu","Hologram / zabezpieczenie","Znak wodny","Tło / papier firmowy","Inne")

@Composable
fun IdentityDocumentsScreen(onBack:()->Unit) {
    val c= LocalContext.current
    val scope= rememberCoroutineScope()
    val orgs= remember { mutableStateListOf<Org>().apply { addAll(IdentityStore.orgs(c)) } }
    val assets= remember { mutableStateListOf<BrandAsset>().apply { addAll(IdentityStore.assets(c)) } }
    var page by remember { mutableStateOf("Dokumenty") }
    var orgId by remember { mutableStateOf(orgs.firstOrNull()?.id.orEmpty()) }
    var orgName by remember { mutableStateOf("") }
    var orgType by remember { mutableStateOf(kinds.first()) }
    var registry by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var rights by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("Zaświadczenie") }
    var number by remember { mutableStateOf("OWD/2026/0001") }
    var recipient by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("Treść dokumentu…") }
    var signer by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Osoba uprawniona") }
    var watermark by remember { mutableStateOf("WZÓR") }
    var logoId by remember { mutableStateOf("") }
    var sealId by remember { mutableStateOf("") }
    var signatureId by remember { mutableStateOf("") }
    var lastPdf by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val assetPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) scope.launch {
            runCatching {
                val asset=withContext(Dispatchers.IO) {
                    val mime=c.contentResolver.getType(uri) ?: "application/octet-stream"
                    require(mime in listOf("image/png","image/jpeg","image/webp","image/svg+xml","application/pdf")) { "Dozwolone: PNG, SVG, JPG, WEBP, PDF." }
                    val bytes=c.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Nie można odczytać pliku.")
                    require(bytes.isNotEmpty() && bytes.size<=20*1024*1024) { "Plik pusty lub większy niż 20 MB." }
                    val id=UUID.randomUUID().toString()
                    val ext=when(mime){"image/png"->"png";"image/jpeg"->"jpg";"image/webp"->"webp";"image/svg+xml"->"svg";else->"pdf"}
                    val f=id+"."+ext
                    File(File(c.filesDir,"owd-brand-assets").apply{mkdirs()},f).writeBytes(bytes)
                    BrandAsset(id,orgId,assetName.ifBlank{"Zasób "+(assets.size+1)},category,f,mime,rights)
                }
                IdentityStore.saveAssets(c,assets.toList()+asset); assets.add(0,asset)
                assetName=""; rights=""; message="Zasób dodany do biblioteki podmiotu."
            }.onFailure { message=it.message ?: "Błąd importu zasobu." }
        }
    }
    val pdfSaver=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if(uri!=null) scope.launch {
            runCatching {
                val bytes=withContext(Dispatchers.IO) {
                    renderPdf(c,title,number,recipient,body,signer,role,watermark,
                        orgs.firstOrNull{it.id==orgId},
                        assets.firstOrNull{it.id==logoId},assets.firstOrNull{it.id==sealId},assets.firstOrNull{it.id==signatureId})
                }
                withContext(Dispatchers.IO) { c.contentResolver.openOutputStream(uri,"wt")?.use{it.write(bytes)} ?: error("Nie można zapisać PDF.") }
                lastPdf=uri; message="PDF zapisany. Wzór podpisu i grafika pieczęci nie są podpisem elektronicznym."
            }.onFailure { message=it.message ?: "Błąd eksportu PDF." }
        } else message="Anulowano zapis PDF."
    }
    val signedImporter=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) scope.launch {
            runCatching {
                val meta=withContext(Dispatchers.IO) {
                    val bytes=c.contentResolver.openInputStream(uri)?.use{it.readBytes()} ?: error("Nie można odczytać PDF.")
                    require(bytes.size>5 && String(bytes.copyOfRange(0,5),Charsets.US_ASCII)=="%PDF-") {"Niepoprawny PDF."}
                    val hash=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
                    JSONObject().put("sha256",hash).put("importedAt",System.currentTimeMillis())
                        .put("verification","NOT_VERIFIED").put("note","Import pliku po podpisaniu; certyfikat i podpis niezweryfikowane.")
                }
                val p=c.getSharedPreferences("owd_identity_signatures",0)
                val a=JSONArray(p.getString("records","[]")).put(meta)
                check(p.edit().putString("records",a.toString()).commit())
                message="Zapisano SHA-256 i czas importu. Podpis pozostaje NIEZWERYFIKOWANY."
            }.onFailure { message=it.message ?: "Błąd importu podpisanego PDF." }
        }
    }

    Surface(Modifier.fillMaxSize().semantics{paneTitle="Zarządzanie dokumentami i identyfikacją"},color=MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,contentDescription="Wróć")}
                Column(Modifier.weight(1f)){Text("Zarządzanie",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading()});Text("Dokumenty i identyfikacja wizualna",style=MaterialTheme.typography.bodySmall)}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("Dokumenty","Biblioteka","Podmioty","Podpisy").forEach { FilterChip(selected=page==it,onClick={page=it},label={Text(it)}) }
            }
            Spacer(Modifier.height(8.dp))
            when(page) {
                "Podmioty" -> {
                    OutlinedTextField(orgName,{orgName=it},label={Text("Nazwa podmiotu")},modifier=Modifier.fillMaxWidth())
                    SimpleChoice("Rodzaj podmiotu",orgType,kinds){orgType=it}
                    OutlinedTextField(registry,{registry=it},label={Text("KRS / NIP / REGON / identyfikator")},modifier=Modifier.fillMaxWidth())
                    Button(onClick={
                        if(orgName.isNotBlank()){
                            val o=Org(UUID.randomUUID().toString(),orgName.trim(),orgType,registry.trim())
                            orgs.add(o);IdentityStore.saveOrgs(c,orgs.toList());orgId=o.id;orgName="";registry=""
                            message="Podmiot dodany."
                        }
                    },modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Dodaj podmiot")}
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
                        items(orgs,key={it.id}){o->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(o.name,fontWeight=FontWeight.Bold);Text(o.type+" • "+o.registry,style=MaterialTheme.typography.bodySmall);TextButton(onClick={orgId=o.id;page="Biblioteka"}){Text("Biblioteka tego podmiotu")}}}}
                    }
                }
                "Biblioteka" -> {
                    OrgChoice(orgs,orgId){orgId=it}
                    Text("Logotypy, herby, godła, flagi, pieczęcie, podpisy, zabezpieczenia i tła.",style=MaterialTheme.typography.bodySmall)
                    OutlinedTextField(assetName,{assetName=it},label={Text("Nazwa zasobu")},modifier=Modifier.fillMaxWidth())
                    SimpleChoice("Kategoria",category,categories){category=it}
                    OutlinedTextField(rights,{rights=it},label={Text("Prawa / podstawa użycia")},modifier=Modifier.fillMaxWidth())
                    Button(onClick={assetPicker.launch(arrayOf("image/png","image/jpeg","image/webp","image/svg+xml","application/pdf"))},enabled=orgId.isNotBlank(),modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Wgraj zasób")}
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){
                        items(assets.filter{it.orgId==orgId},key={it.id}){a->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){Text(a.name,fontWeight=FontWeight.SemiBold);Text(a.category+" • "+a.mime,style=MaterialTheme.typography.bodySmall);if(a.rights.isNotBlank())Text("Prawa: "+a.rights,style=MaterialTheme.typography.labelSmall)}}}
                    }
                    Text("Pliki przechowywane lokalnie. PNG/JPG/WEBP są osadzane w PDF; SVG i PDF są archiwizowane w bibliotece, ale nie są jeszcze rasteryzowane w eksporcie.",style=MaterialTheme.typography.labelSmall)
                }
                "Dokumenty" -> {
                    OrgChoice(orgs,orgId){orgId=it}
                    OutlinedTextField(title,{title=it},label={Text("Tytuł dokumentu")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(number,{number=it},label={Text("Numer dokumentu / rejestru")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(recipient,{recipient=it},label={Text("Adresat / przedmiot")},modifier=Modifier.fillMaxWidth(),minLines=2)
                    OutlinedTextField(body,{body=it},label={Text("Treść")},modifier=Modifier.fillMaxWidth(),minLines=4)
                    OutlinedTextField(signer,{signer=it},label={Text("Osoba podpisująca")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(role,{role=it},label={Text("Stanowisko / funkcja / tytuł")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(watermark,{watermark=it},label={Text("Znak wodny")},modifier=Modifier.fillMaxWidth())
                    AssetChoice("Logo",assets.filter{it.orgId==orgId&&it.category=="Logo"},logoId){logoId=it}
                    AssetChoice("Pieczęć",assets.filter{it.orgId==orgId&&it.category=="Pieczęć"},sealId){sealId=it}
                    AssetChoice("Wzór podpisu",assets.filter{it.orgId==orgId&&it.category=="Wzór podpisu"},signatureId){signatureId=it}
                    Card { Column(Modifier.padding(12.dp)){Text("Podgląd",fontWeight=FontWeight.Bold);Text(orgs.firstOrNull{it.id==orgId}?.name ?: "Podmiot");Text(title,style=MaterialTheme.typography.titleLarge);Text("Nr "+number);Text(recipient);Text(body);Text(role+" "+signer);Text("Znak wodny: "+watermark)} }
                    Button(onClick={pdfSaver.launch(number.replace('/','-')+".pdf")},enabled=orgId.isNotBlank(),modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.PictureAsPdf,null);Text(" Eksportuj PDF")}
                    OutlinedButton(onClick={
                        val u=lastPdf
                        if(u==null) message="Najpierw wygeneruj PDF."
                        else {
                            val intent=Intent(Intent.ACTION_SEND).apply{type="application/pdf";putExtra(Intent.EXTRA_STREAM,u);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
                            runCatching{c.startActivity(Intent.createChooser(intent,"Przekaż dokument do podpisu"))}
                                .onFailure{message=it.message ?: "Nie można otworzyć aplikacji do podpisu."}
                            message="Przekazanie do wybranej aplikacji. Podpis zostanie złożony poza OWD."
                        }
                    },modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Draw,null);Text(" Przekaż PDF do podpisu")}
                    OutlinedButton(onClick={
                        val u=lastPdf
                        if(u==null) message="Najpierw wygeneruj PDF."
                        else runCatching {
                            val bytes=c.contentResolver.openInputStream(u)?.use{it.readBytes()} ?: error("Nie można odczytać PDF.")
                            val manager=c.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            manager.print(title,PdfBytesPrintAdapter(title,bytes),PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setColorMode(PrintAttributes.COLOR_MODE_COLOR).build())
                            message="Przekazano dokument do systemowej usługi drukowania."
                        }.onFailure{message=it.message ?: "Nie udało się uruchomić drukowania."}
                    },modifier=Modifier.fillMaxWidth()){Text("Drukuj PDF")}
                }
                else -> {
                    Text("Importuj dokument PDF po podpisaniu",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                    Text("Po imporcie zapisujemy SHA-256 i czas importu. Nie weryfikujemy certyfikatu, łańcucha zaufania, znacznika czasu ani kryptograficznej poprawności podpisu.",style=MaterialTheme.typography.bodySmall)
                    Button(onClick={signedImporter.launch(arrayOf("application/pdf"))},modifier=Modifier.fillMaxWidth()){Text("Importuj podpisany PDF")}
                }
            }
            if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp))
        }
    }
}

@Composable private fun SimpleChoice(label:String,value:String,options:List<String>,onPick:(String)->Unit){
    var open by remember{mutableStateOf(false)}
    Box { OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth()){Text(label+": "+value)};DropdownMenu(open,{open=false}){options.forEach{DropdownMenuItem(text={Text(it)},onClick={onPick(it);open=false})}} }
}
@Composable private fun OrgChoice(items:List<Org>,selected:String,onPick:(String)->Unit){
    SimpleChoice("Podmiot",items.firstOrNull{it.id==selected}?.name ?: "Dodaj podmiot",items.map{it.name}){name->items.firstOrNull{it.name==name}?.let{onPick(it.id)}}
}
@Composable private fun AssetChoice(label:String,items:List<BrandAsset>,selected:String,onPick:(String)->Unit){
    SimpleChoice(label,items.firstOrNull{it.id==selected}?.name ?: "Brak",listOf("Brak")+items.map{it.name}){name->onPick(items.firstOrNull{it.name==name}?.id ?: "")}
}

private fun renderPdf(c:Context,title:String,number:String,recipient:String,body:String,signer:String,role:String,watermark:String,org:Org?,logo:BrandAsset?,seal:BrandAsset?,signature:BrandAsset?):ByteArray {
    val pdf=PdfDocument()
    val page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,1).create())
    val canvas=page.canvas
    val p=Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawColor(android.graphics.Color.WHITE)
    p.color=android.graphics.Color.rgb(30,70,120);p.strokeWidth=2f;canvas.drawLine(38f,38f,557f,38f,p)
    p.textSize=18f;p.typeface=Typeface.DEFAULT_BOLD
    canvas.drawText((org?.name ?: "Podmiot").take(50),42f,78f,p)
    logo?.let{drawImage(c,canvas,it,480f,48f,65f,65f)}
    p.color=android.graphics.Color.LTGRAY;p.textSize=54f;p.typeface=Typeface.DEFAULT_BOLD
    canvas.save();canvas.rotate(-30f,295f,420f);canvas.drawText(watermark.take(18),110f,430f,p);canvas.restore()
    p.color=android.graphics.Color.DKGRAY;p.textSize=22f;canvas.drawText(title.take(45),42f,190f,p)
    p.textSize=10f;p.typeface=Typeface.DEFAULT
    canvas.drawText("Numer: "+number,42f,218f,p)
    canvas.drawText("Data: "+java.text.SimpleDateFormat("dd.MM.yyyy",java.util.Locale("pl")).format(java.util.Date()),42f,236f,p)
    canvas.drawText("Identyfikator OWD: "+number,42f,252f,p)
    canvas.drawText("Adresat / przedmiot:",42f,280f,p)
    drawLines(canvas,recipient,42f,302f,510f,p)
    drawLines(canvas,body,42f,355f,510f,p)
    canvas.drawText(role.take(70),42f,680f,p);canvas.drawText(signer.take(70),42f,700f,p)
    seal?.let{drawImage(c,canvas,it,410f,615f,90f,90f)}
    signature?.let{drawImage(c,canvas,it,42f,710f,140f,50f)}
    runCatching {
        val matrix=QRCodeWriter().encode("urn:owd:document:"+number,BarcodeFormat.QR_CODE,96,96)
        val qr=android.graphics.Bitmap.createBitmap(96,96,android.graphics.Bitmap.Config.ARGB_8888)
        for(y in 0 until 96) for(x in 0 until 96) qr.setPixel(x,y,if(matrix[x,y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        canvas.drawBitmap(qr,470f,700f,null);qr.recycle()
    }
    p.textSize=8f;p.color=android.graphics.Color.GRAY
    canvas.drawText("Projekt OWD. QR identyfikuje lokalny dokument; nie potwierdza wystawcy.",42f,814f,p)
    canvas.drawText("Grafika pieczęci i wzór podpisu nie stanowią podpisu elektronicznego.",42f,802f,p)
    pdf.finishPage(page);val out=java.io.ByteArrayOutputStream();pdf.writeTo(out);pdf.close();return out.toByteArray()
}
private fun drawLines(canvas:android.graphics.Canvas,text:String,x:Float,start:Float,width:Float,p:Paint){
    var y=start;var line=""
    text.split(Regex("\\s+")).forEach{word->
        val candidate=if(line.isEmpty())word else line+" "+word
        if(p.measureText(candidate)>width&&line.isNotEmpty()){canvas.drawText(line,x,y,p);y+=18f;line=word}else line=candidate
    }
    if(line.isNotEmpty())canvas.drawText(line,x,y,p)
}
private fun drawImage(c:Context,canvas:android.graphics.Canvas,a:BrandAsset,x:Float,y:Float,w:Float,h:Float){
    if(a.mime !in listOf("image/png","image/jpeg","image/webp"))return
    val b=BitmapFactory.decodeFile(File(File(c.filesDir,"owd-brand-assets"),a.file).absolutePath)?:return
    val s=minOf(w/b.width,h/b.height);val dw=b.width*s;val dh=b.height*s
    canvas.drawBitmap(b,null,android.graphics.RectF(x+(w-dw)/2,y+(h-dh)/2,x+(w+dw)/2,y+(h+dh)/2),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG));b.recycle()
}

private class PdfBytesPrintAdapter(private val jobName:String, private val pdfBytes:ByteArray):PrintDocumentAdapter() {
    override fun onLayout(oldAttributes:PrintAttributes?,newAttributes:PrintAttributes?,cancellationSignal:CancellationSignal,callback:LayoutResultCallback,extras:Bundle?) {
        if(cancellationSignal.isCanceled){callback.onLayoutCancelled();return}
        val info=PrintDocumentInfo.Builder(jobName+".pdf").setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1).build()
        callback.onLayoutFinished(info,true)
    }
    override fun onWrite(pages:Array<out PageRange>,destination:ParcelFileDescriptor,cancellationSignal:CancellationSignal,callback:WriteResultCallback) {
        try {
            if(cancellationSignal.isCanceled){callback.onWriteCancelled();return}
            FileOutputStream(destination.fileDescriptor).use { it.write(pdfBytes);it.flush() }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch(failure:Exception) { callback.onWriteFailed(failure.message ?: "Błąd drukowania PDF") }
        finally { runCatching { destination.close() } }
    }
}
