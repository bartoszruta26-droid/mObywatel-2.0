// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.BitmapFactory
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

private data class ReceiptRecord(val id:String,val name:String,val createdAt:Long, val archiveUri:String? = null)
private object ReceiptStore {
    private fun dir(c:Context)=File(c.noBackupFilesDir,"receipts").apply{mkdirs()}
    private fun index(c:Context)=File(dir(c),"index.json")
    fun persistSelectedFolder(c:Context,name:String,bytes:ByteArray,createdAt:Long):String? {
        val raw=receiptPref(c).getString("folder_tree_uri",null) ?: return null
        val root=DocumentFile.fromTreeUri(c,Uri.parse(raw))
            ?: error("Wybrany katalog rachunków jest niedostępny.")
        val calendar=Calendar.getInstance().apply { timeInMillis=createdAt }
        val year=calendar.get(Calendar.YEAR).toString()
        val month="%02d".format(Locale.ROOT,calendar.get(Calendar.MONTH)+1)
        val yearDir=root.findFile(year)?.takeIf{it.isDirectory} ?: root.createDirectory(year)
            ?: error("Nie można utworzyć katalogu roku $year.")
        val monthDir=yearDir.findFile(month)?.takeIf{it.isDirectory} ?: yearDir.createDirectory(month)
            ?: error("Nie można utworzyć katalogu miesiąca $month.")
        val safe=name.replace(Regex("[^A-Za-z0-9._-]"),"_").take(100).ifBlank{"rachunek.pdf"}
        monthDir.findFile(safe)?.delete()
        val target=monthDir.createFile("application/pdf",safe)
            ?: error("Nie można utworzyć pliku PDF w wybranym katalogu.")
        c.contentResolver.openOutputStream(target.uri,"w")?.use{it.write(bytes)}
            ?: error("Nie można zapisać PDF w wybranym katalogu.")
        return target.uri.toString()
    }
    fun list(c:Context):List<ReceiptRecord> = runCatching {
        val a=JSONArray(index(c).takeIf{it.exists()}?.readText() ?: "[]")
        (0 until a.length()).map { val o=a.getJSONObject(it); ReceiptRecord(o.getString("id"),o.getString("name"),o.getLong("createdAt"),o.optString("archiveUri").takeIf{it.isNotBlank()}) }
    }.getOrDefault(emptyList())
    fun put(c:Context,name:String,bytes:ByteArray,id:String=UUID.randomUUID().toString(),createdAt:Long=System.currentTimeMillis()):ReceiptRecord {
        require(bytes.size>5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Nieprawidłowy plik PDF." }
        require(bytes.size<=50*1024*1024) { "Plik PDF przekracza 50 MB." }
        val temporary=File(dir(c),"$id.pdf.tmp")
        val internal=File(dir(c),"$id.pdf")
        temporary.writeBytes(bytes)
        val archiveUri=try { persistSelectedFolder(c,name,bytes,createdAt) }
            catch (failure:Throwable) { temporary.delete(); throw failure }
        check(temporary.renameTo(internal)) { "Nie udało się zatwierdzić lokalnej kopii rachunku." }
        val record=ReceiptRecord(id,name.take(180),createdAt,archiveUri)
        save(c,(list(c).filterNot{it.id==id}+record))
        return record
    }
    fun bytes(c:Context,id:String)=File(dir(c),"$id.pdf").takeIf{it.isFile}?.readBytes() ?: error("Nie znaleziono pliku PDF.")
    fun delete(c:Context,id:String) { File(dir(c),"$id.pdf").delete(); save(c,list(c).filterNot{it.id==id}) }
    private fun save(c:Context,items:List<ReceiptRecord>) {
        val a=JSONArray(); items.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("createdAt",it.createdAt).put("archiveUri",it.archiveUri ?: "")) }
        index(c).writeText(a.toString())
    }
    fun importPdf(c:Context,uri:Uri):ReceiptRecord {
        val bytes=c.contentResolver.openInputStream(uri)?.use { input ->
            val output=ByteArrayOutputStream()
            val buffer=ByteArray(8192)
            var total=0
            while(true) {
                val count=input.read(buffer)
                if(count<0) break
                total+=count
                require(total<=50*1024*1024) { "PDF przekracza limit 50 MB." }
                output.write(buffer,0,count)
            }
            output.toByteArray()
        } ?: error("Nie można odczytać PDF.")
        require(bytes.size>5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Wybrany plik nie jest poprawnym PDF." }
        val name=c.contentResolver.query(uri,null,null,null,null)?.use{cur-> if(cur.moveToFirst()) { val i=cur.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(i>=0) cur.getString(i) else null } else null} ?: "rachunek.pdf"
        return put(c,name,bytes)
    }
}

private fun receiptFileDate(date:String):Long {
    for(pattern in listOf("dd.MM.yyyy","yyyy-MM-dd","dd/MM/yyyy")) {
        val parsed=runCatching { SimpleDateFormat(pattern,Locale("pl")).apply { isLenient=false }.parse(date)?.time }.getOrNull()
        if(parsed!=null) return parsed
    }
    return System.currentTimeMillis()
}

private const val DEFAULT_RECEIPT_NOTES = "Uwagi: Zwolnienie od obowiązku wystawienia faktury - Art. 106b. [Obowiązek wystawienia faktury] [...] ust. 2. Podatnik nie jest obowiązany do wystawienia faktury w odniesieniu do sprzedaży zwolnionej od podatku na podstawie art. 43 ust. 1 [Zwolnienia przedmiotowe], art. 113 ust. 1 [Zwolnienie podmiotowe].\nZwolnienie podmiotowe VAT: art. 113 ust. 1 i 9.\nZwolnienie przedmiotowe VAT: art. 43 ust. 1 usługi medyczne pkt 18a-19a, opieki nad niepełnosprawnymi w miejscu zamieszkania pkt 23; kształcenia zawodowego pkt 29, usługi i towary organizacji filozoficznych i obywatelskich pkt 31, usługi wychowania fizycznego pkt 32, usługi kulturalne pkt 33."

private fun receiptPref(c:Context)=c.getSharedPreferences("receipt_template_settings",Context.MODE_PRIVATE)
private fun receiptNotes(c:Context)=receiptPref(c).getString("notes",DEFAULT_RECEIPT_NOTES) ?: DEFAULT_RECEIPT_NOTES

@Composable
internal fun ReceiptTemplateSettingsScreen(onBack:()->Unit) {
    val c= LocalContext.current
    var notes by remember { mutableStateOf(receiptNotes(c)) }
    var title by remember { mutableStateOf(receiptPref(c).getString("title","RACHUNEK") ?: "RACHUNEK") }
    var showPkwiu by remember { mutableStateOf(receiptPref(c).getBoolean("show_pkwiu",true)) }
    var defaultSigner by remember { mutableStateOf(receiptPref(c).getString("default_signer","") ?: "") }
    var status by remember { mutableStateOf("") }
    val folderPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if(uri!=null) runCatching { c.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION); receiptPref(c).edit().putString("folder_tree_uri",uri.toString()).apply(); status="Wybrano katalog zapisu rachunków." }.onFailure{status="Błąd katalogu: ${it.message}"}
    }
    val selectedFolder=receiptPref(c).getString("folder_tree_uri",null)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót do ustawień",Modifier.clickable(onClick=onBack),color=MaterialTheme.colorScheme.primary)
        Text("Ustawienia rachunków",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        OutlinedTextField(title,{title=it},label={Text("Nagłówek rachunku")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(defaultSigner,{defaultSigner=it},label={Text("Domyślny podpisujący")},modifier=Modifier.fillMaxWidth())
        Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(showPkwiu,{showPkwiu=it}); Text("Pokazuj kolumnę PKWiU") }
        OutlinedTextField(notes,{notes=it},label={Text("Uwagi – treść drukowana na rachunku")},modifier=Modifier.fillMaxWidth(),minLines=7)
        Text("Katalog zapisu PDF",fontWeight=FontWeight.Bold)
        Text(if(selectedFolder.isNullOrBlank()) "Nie wybrano katalogu – pliki pozostają w pamięci aplikacji." else "Wybrany katalog: pliki będą porządkowane w podkatalogach rok/miesiąc.")
        OutlinedButton(onClick={folderPicker.launch(null)},modifier=Modifier.fillMaxWidth()) { Text("Wybierz katalog zapisu rachunków") }
        Button(onClick={ receiptPref(c).edit().putString("title",title).putString("notes",notes).putBoolean("show_pkwiu",showPkwiu).putString("default_signer",defaultSigner).apply(); status="Zapisano wzór rachunku." },modifier=Modifier.fillMaxWidth()) { Text("Zapisz wzór") }
        if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary)
    }
}


private data class ReceiptParty(val name:String,val address:String) {
    fun display()=listOf(name,address).filter{it.isNotBlank()}.joinToString("\n")
}
private fun receiptParties(c:Context):List<ReceiptParty> = runCatching {
    val a=JSONArray(receiptPref(c).getString("parties","[]") ?: "[]")
    (0 until a.length()).map { val o=a.getJSONObject(it); ReceiptParty(o.optString("name"),o.optString("address")) }
}.getOrDefault(emptyList())
private fun saveReceiptParties(c:Context,items:List<ReceiptParty>) {
    val a=JSONArray()
    items.forEach { a.put(JSONObject().put("name",it.name).put("address",it.address)) }
    receiptPref(c).edit().putString("parties",a.toString()).apply()
}
private fun receiptMoneyCents(raw:String):Long {
    val currency=Regex("(?i)^\\s*(\\d+)\\s*z[lł]\\s*(\\d{1,2})\\s*groszy?\\s*$").find(raw)
        ?: Regex("(?i)^\\s*(\\d+)\\s*z[lł]\\s*(\\d{1,2})\\s*gr\\s*$").find(raw)
    if(currency!=null) return (currency.groupValues[1].toLongOrNull()?:0L)*100+(currency.groupValues[2].toLongOrNull()?:0L).coerceIn(0,99)
    val s=raw.trim().lowercase(Locale.ROOT).replace("zł","").replace("zl","").replace("gr","").replace(" ","").replace(",",".")
    if(s.isBlank()) return 0L
    return runCatching { java.math.BigDecimal(s).multiply(java.math.BigDecimal(100)).setScale(0,java.math.RoundingMode.HALF_UP).longValueExact() }.getOrDefault(0L).coerceAtLeast(0)
}
private fun receiptMoneyLabel(cents:Long):String = "%d zł %02d gr".format(Locale("pl"),cents/100,cents%100)
private fun receiptNumberWords(n:Long):String {
    val ones=listOf("zero","jeden","dwa","trzy","cztery","pięć","sześć","siedem","osiem","dziewięć")
    val teens=listOf("dziesięć","jedenaście","dwanaście","trzynaście","czternaście","piętnaście","szesnaście","siedemnaście","osiemnaście","dziewiętnaście")
    val tens=listOf("","","dwadzieścia","trzydzieści","czterdzieści","pięćdziesiąt","sześćdziesiąt","siedemdziesiąt","osiemdziesiąt","dziewięćdziesiąt")
    val hundreds=listOf("","sto","dwieście","trzysta","czterysta","pięćset","sześćset","siedemset","osiemset","dziewięćset")
    fun group(v:Int):String {
        val out=mutableListOf<String>(); val h=v/100; val r=v%100
        if(h>0) out.add(hundreds[h])
        if(r in 10..19) out.add(teens[r-10]) else { if(r/10>0) out.add(tens[r/10]); if(r%10>0) out.add(ones[r%10]) }
        return out.joinToString(" ")
    }
    fun plural(v:Int,one:String,few:String,many:String):String = when { v%100 in 12..14 -> many; v%10==1 -> one; v%10 in 2..4 -> few; else -> many }
    val value=n.coerceAtLeast(0).coerceAtMost(999999999L)
    val millions=(value/1000000).toInt(); val thousands=((value/1000)%1000).toInt(); val rest=(value%1000).toInt()
    val chunks=mutableListOf<String>()
    if(millions>0) chunks.add(group(millions)+" "+plural(millions,"milion","miliony","milionów"))
    if(thousands>0) chunks.add(group(thousands)+" "+plural(thousands,"tysiąc","tysiące","tysięcy"))
    if(rest>0 || chunks.isEmpty()) chunks.add(group(rest))
    return chunks.joinToString(" ").trim()
}
private fun receiptAmountInWords(totalCents:Long):String {
    val zl=totalCents/100; val gr=totalCents%100
    return receiptNumberWords(zl)+" "+(if(zl%10==1L && zl%100!=11L) "złoty" else if(zl%10 in 2L..4L && zl%100 !in 12L..14L) "złote" else "złotych")+" "+receiptNumberWords(gr)+" "+(if(gr==1L) "grosz" else if(gr%10 in 2L..4L && gr%100 !in 12L..14L) "grosze" else "groszy")
}

private fun lastReceiptString(c:Context,key:String,default:String=""):String = runCatching {
    JSONObject(receiptPref(c).getString("last_receipt","{}") ?: "{}").optString(key,default)
}.getOrDefault(default)
private fun lastReceiptList(c:Context,key:String,default:List<String>):List<String> = runCatching {
    val a=JSONObject(receiptPref(c).getString("last_receipt","{}") ?: "{}").optJSONArray(key) ?: return@runCatching default
    (0 until a.length()).map { a.optString(it) }.let { if(it.size==default.size) it else default }
}.getOrDefault(default)
@Composable
internal fun ReceiptDocumentsScreen(onBack:()->Unit) {
    val c=LocalContext.current
    var records by remember { mutableStateOf(ReceiptStore.list(c)) }
    var status by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf(lastReceiptString(c,"seller")) }; var buyer by remember { mutableStateOf(lastReceiptString(c,"buyer")) }
    var parties by remember { mutableStateOf(receiptParties(c)) }
    var showPartyManager by remember { mutableStateOf(false) }
    var partyName by remember { mutableStateOf("") }; var partyAddress by remember { mutableStateOf("") }
    var editingParty by remember { mutableStateOf<Int?>(null) }
    var sellerMenu by remember { mutableStateOf(false) }; var buyerMenu by remember { mutableStateOf(false) }
    var number by remember { mutableStateOf(lastReceiptString(c,"number")) }; var saleDate by remember { mutableStateOf(java.text.SimpleDateFormat("dd.MM.yyyy",java.util.Locale("pl")).format(java.util.Date())) }
    var payment by remember { mutableStateOf(lastReceiptString(c,"payment","Gotówka")) }; var due by remember { mutableStateOf(lastReceiptString(c,"due")) }
    var signature by remember { mutableStateOf(receiptPref(c).getString("default_signer","") ?: "") }
    var names by remember { mutableStateOf(lastReceiptList(c,"names",listOf("","",""))) }; var pkwiu by remember { mutableStateOf(lastReceiptList(c,"pkwiu",listOf("","",""))) }
    var units by remember { mutableStateOf(lastReceiptList(c,"units",listOf("szt.","",""))) }; var quantities by remember { mutableStateOf(lastReceiptList(c,"quantities",listOf("1","",""))) }
    var prices by remember { mutableStateOf(lastReceiptList(c,"prices",listOf("0,00","",""))) }
    var replaceId by remember { mutableStateOf<String?>(null) }
    val importPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri-> if(uri!=null) runCatching { val old=replaceId; val b=c.contentResolver.openInputStream(uri)?.use{it.readBytes()} ?: error("Nie można odczytać PDF."); val record=if(old!=null) ReceiptStore.put(c,"Rachunek – plik podmieniony.pdf",b,old) else ReceiptStore.importPdf(c,uri); records=ReceiptStore.list(c); status="Zapisano: ${record.name}" }.onFailure{status="Błąd PDF: ${it.message}"}; replaceId=null }
    val photoPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri ->
        if(uri!=null) runCatching {
            val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
            c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it,null,bounds) }
                ?: error("Nie można odczytać zdjęcia.")
            require(bounds.outWidth>0 && bounds.outHeight>0) { "Wybrany plik nie jest poprawnym obrazem." }
            var sample=1
            while(bounds.outWidth/sample>2400 || bounds.outHeight/sample>2400) sample*=2
            val bitmap=c.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply { inSampleSize=sample })
            } ?: error("Nie można wczytać zdjęcia.")
            val doc=PdfDocument()
            val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create())
            val scale=minOf(555f/bitmap.width,790f/bitmap.height)
            val w=(bitmap.width*scale).toInt(); val h=(bitmap.height*scale).toInt()
            page.canvas.drawBitmap(bitmap,null,android.graphics.RectF((595-w)/2f,(842-h)/2f,(595+w)/2f,(842+h)/2f),Paint(Paint.ANTI_ALIAS_FLAG))
            doc.finishPage(page); val out=ByteArrayOutputStream(); doc.writeTo(out); doc.close(); bitmap.recycle()
            val rec=ReceiptStore.put(c,"Rachunek_reczny_${System.currentTimeMillis()}.pdf",out.toByteArray())
            records=ReceiptStore.list(c); status="Zdjęcie ręcznie wypisanego rachunku zapisano jako PDF: ${rec.name}"
        }.onFailure{status="Błąd zdjęcia: ${it.message}"}
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("‹  Powrót",Modifier.clickable(onClick=onBack),color=MaterialTheme.colorScheme.primary)
        Text("Rachunki",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        Text("Wystaw rachunek według układu papierowego, zapisz go jako PDF lub zarządzaj zapisanymi plikami.")
        OutlinedTextField(number,{number=it},label={Text("Rachunek nr")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(saleDate,{saleDate=it},label={Text("Data sprzedaży")},modifier=Modifier.fillMaxWidth())
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) { OutlinedTextField(seller,{seller=it},label={Text("Sprzedawca")},modifier=Modifier.weight(1f),minLines=2); Box { TextButton(onClick={sellerMenu=true}) { Text("Wybierz") }; DropdownMenu(expanded=sellerMenu,onDismissRequest={sellerMenu=false}) { parties.forEach { party-> DropdownMenuItem(text={Text(party.name)},onClick={seller=party.display();sellerMenu=false}) } } } }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) { OutlinedTextField(buyer,{buyer=it},label={Text("Nabywca")},modifier=Modifier.weight(1f),minLines=2); Box { TextButton(onClick={buyerMenu=true}) { Text("Wybierz") }; DropdownMenu(expanded=buyerMenu,onDismissRequest={buyerMenu=false}) { parties.forEach { party-> DropdownMenuItem(text={Text(party.name)},onClick={buyer=party.display();buyerMenu=false}) } } } }
        OutlinedButton(onClick={showPartyManager=true},modifier=Modifier.fillMaxWidth()) { Text("Zarządzaj podmiotami: dodaj / edytuj / usuń") }
        OutlinedTextField(payment,{payment=it},label={Text("Sposób zapłaty")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(due,{due=it},label={Text("Termin zapłaty")},modifier=Modifier.fillMaxWidth())
        Text("Pozycje rachunku",fontWeight=FontWeight.Bold)
        repeat(3){i->
            Text("Pozycja ${i+1}",fontWeight=FontWeight.SemiBold)
            OutlinedTextField(names[i],{v->names=names.toMutableList().also{it[i]=v}},label={Text("Nazwa towaru lub usługi")},modifier=Modifier.fillMaxWidth())
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(pkwiu[i],{v->pkwiu=pkwiu.toMutableList().also{it[i]=v}},label={Text("PKWiU")},modifier=Modifier.weight(1f))
                OutlinedTextField(units[i],{v->units=units.toMutableList().also{it[i]=v}},label={Text("Jm")},modifier=Modifier.weight(0.7f))
                OutlinedTextField(quantities[i],{v->quantities=quantities.toMutableList().also{it[i]=v}},label={Text("Ilość")},modifier=Modifier.weight(0.8f))
                OutlinedTextField(prices[i],{v->prices=prices.toMutableList().also{it[i]=v}},label={Text("Cena (zł i gr)")},modifier=Modifier.weight(1f))
            }
        }
        OutlinedTextField(signature,{signature=it},label={Text("Imię i nazwisko osoby wystawiającej")},modifier=Modifier.fillMaxWidth())
        val totalCents=names.indices.fold(0L) { acc,i -> val q=runCatching{java.math.BigDecimal(quantities[i].replace(',', '.'))}.getOrDefault(java.math.BigDecimal.ZERO); acc+q.multiply(java.math.BigDecimal(receiptMoneyCents(prices[i]))).setScale(0,java.math.RoundingMode.HALF_UP).toLong() }.coerceAtLeast(0L)
        val total=totalCents/100.0
        val words=receiptAmountInWords(totalCents)
        OutlinedTextField(words,{ },label={Text("Wartość słownie – wyliczana automatycznie")},modifier=Modifier.fillMaxWidth(),readOnly=true)
        Text("Razem: ${receiptMoneyLabel(totalCents)}",fontWeight=FontWeight.Bold)
        Button(onClick={
            runCatching {
                val pdf=buildReceiptPdf(c,number,saleDate,seller,buyer,payment,due,names,pkwiu,units,quantities,prices,words,signature,total)
                val snapshot=JSONObject().put("number",number).put("seller",seller).put("buyer",buyer).put("payment",payment).put("due",due)
                listOf("names" to names,"pkwiu" to pkwiu,"units" to units,"quantities" to quantities,"prices" to prices).forEach { (key,values)-> val a=JSONArray(); values.forEach{a.put(it)}; snapshot.put(key,a) }
                receiptPref(c).edit().putString("last_receipt",snapshot.toString()).apply()
                val rec=ReceiptStore.put(c,"Rachunek ${number.ifBlank{"bez numeru"}}.pdf",pdf,createdAt=receiptFileDate(saleDate))
                records=ReceiptStore.list(c)
                prices=prices.map { receiptMoneyLabel(receiptMoneyCents(it)) }
                status="Wystawiono i zapisano rachunek PDF: ${rec.name}"
            }.onFailure { status="Nie udało się zapisać rachunku PDF: ${it.message}" }
        },modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.PictureAsPdf,null); Spacer(Modifier.width(8.dp)); Text("Wystaw i zapisz rachunek PDF") }
        Divider()
        Text("Zapisane rachunki PDF",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("Każdy zapisany rachunek ma własny kafelek podpisu. Podpis wymaga potwierdzenia przez użytkownika w usłudze rządowej.")
        OutlinedButton(onClick={photoPicker.launch(arrayOf("image/*"))},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.AddPhotoAlternate,null); Spacer(Modifier.width(8.dp)); Text("Dodaj zdjęcie ręcznie wypisanego rachunku") }
        OutlinedButton(onClick={replaceId=null;importPicker.launch(arrayOf("application/pdf"))},modifier=Modifier.fillMaxWidth()) { Text("Dodaj istniejący PDF") }
        if(records.isEmpty()) Text("Brak zapisanych rachunków PDF.")
        records.forEach { rec ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Filled.Description,null); Spacer(Modifier.width(8.dp)); Text(rec.name,Modifier.weight(1f),fontWeight=FontWeight.SemiBold) }
                Text(java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",java.util.Locale("pl")).format(java.util.Date(rec.createdAt)),style=MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                    OutlinedButton(onClick={replaceId=rec.id;importPicker.launch(arrayOf("application/pdf"))}) { Text("Podmień PDF") }
                    TextButton(onClick={
                        runCatching {
                            val bytes=ReceiptStore.bytes(c,rec.id)
                            val f=File(c.cacheDir,rec.name).apply { writeBytes(bytes) }
                            val uri=androidx.core.content.FileProvider.getUriForFile(c,c.packageName+".ocrfiles",f)
                            val intent=Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/pdf").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            c.startActivity(intent)
                        }.onFailure { status="Nie można otworzyć PDF: ${it.message}" }
                    }) { Text("Wyświetl") }
                    TextButton(onClick={
                        runCatching {
                            val file=File(c.cacheDir,rec.name).apply { writeBytes(ReceiptStore.bytes(c,rec.id)) }
                            val uri=androidx.core.content.FileProvider.getUriForFile(c,c.packageName+".ocrfiles",file)
                            val send=Intent(Intent.ACTION_SEND).apply {
                                type="application/pdf"
                                putExtra(Intent.EXTRA_STREAM,uri)
                                clipData=android.content.ClipData.newUri(c.contentResolver,rec.name,uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            c.startActivity(Intent.createChooser(send,"Przekaż rachunek PDF"))
                        }.onFailure{status="Nie można przekazać PDF: ${it.message}"}
                    }) { Icon(Icons.Filled.VerifiedUser,null); Text("Podpisz PDF") }
                }
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick={runCatching {
                        c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.gov.pl/web/gov/podpisz-dokument-elektronicznie-wykorzystaj-podpis-zaufany")))
                    }.onFailure{status="Nie można otworzyć usługi gov.pl: ${it.message}"}}) { Text("Otwórz gov.pl") }
                    TextButton(onClick={replaceId=null;importPicker.launch(arrayOf("application/pdf"))}) { Text("Dodaj podpisany PDF") }
                    TextButton(onClick={ReceiptStore.delete(c,rec.id);records=ReceiptStore.list(c);status="Usunięto rachunek PDF."}) { Icon(Icons.Filled.Delete,null); Text("Usuń") }
                }
            } }
        }
        if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary)
        if(showPartyManager) AlertDialog(
            onDismissRequest={showPartyManager=false},
            title={Text("Podmioty rachunku")},
            text={
                Column(Modifier.heightIn(max=430.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(partyName,{partyName=it},label={Text("Nazwa podmiotu")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(partyAddress,{partyAddress=it},label={Text("Adres i dane identyfikacyjne")},modifier=Modifier.fillMaxWidth(),minLines=2)
                    Button(onClick={
                        val item=ReceiptParty(partyName.trim(),partyAddress.trim())
                        if(item.name.isNotBlank()) {
                            parties=if(editingParty==null) parties+item else parties.toMutableList().also{list-> if(editingParty!! in list.indices) list[editingParty!!]=item}
                            saveReceiptParties(c,parties); partyName=""; partyAddress=""; editingParty=null
                        }
                    },modifier=Modifier.fillMaxWidth()) { Text(if(editingParty==null) "Dodaj podmiot" else "Zapisz zmiany") }
                    parties.forEachIndexed { index,party ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(8.dp)) {
                                Text(party.name,fontWeight=FontWeight.SemiBold)
                                if(party.address.isNotBlank()) Text(party.address)
                                Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick={seller=party.display();showPartyManager=false}) { Text("Ustaw jako wystawiającego") }
                                    TextButton(onClick={buyer=party.display();showPartyManager=false}) { Text("Ustaw jako nabywcę") }
                                }
                                Row {
                                    TextButton(onClick={editingParty=index;partyName=party.name;partyAddress=party.address}) { Text("Edytuj") }
                                    TextButton(onClick={parties=parties.filterIndexed{idx,_->idx!=index};saveReceiptParties(c,parties);if(editingParty==index){editingParty=null;partyName="";partyAddress=""}}) { Text("Usuń") }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton={TextButton(onClick={showPartyManager=false}) { Text("Zamknij") }},
            dismissButton={TextButton(onClick={editingParty=null;partyName="";partyAddress=""}) { Text("Nowy") }}
        )
    }
}

private fun buildReceiptPdf(c:Context,no:String,date:String,seller:String,buyer:String,payment:String,due:String,names:List<String>,pkwiu:List<String>,units:List<String>,qty:List<String>,prices:List<String>,words:String,signature:String,total:Double):ByteArray {
    val doc=PdfDocument(); val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create()); val canvas=page.canvas
    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.BLACK;textSize=10f}
    val bold=Paint(p).apply{typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);textSize=12f}
    val border=Paint(p).apply{style=Paint.Style.STROKE;strokeWidth=0.8f;color=android.graphics.Color.DKGRAY}
    fun line(y:Float,x1:Float=30f,x2:Float=565f){canvas.drawLine(x1,y,x2,y,p)}
    fun txt(s:String,x:Float,y:Float,paint:Paint=p,maxWidth:Float=520f):Float { val tokens=s.replace("\\r","").split(Regex("\\s+")); var row=""; var yy=y; tokens.forEach{w-> val next=if(row.isEmpty())w else "$row $w"; if(row.isNotEmpty() && paint.measureText(next)>maxWidth){canvas.drawText(row,x,yy,paint);yy+=13;row=w}else row=next}; if(row.isNotEmpty())canvas.drawText(row,x,yy,paint); return yy+13f }
    canvas.drawText(receiptPref(c).getString("title","RACHUNEK") ?: "RACHUNEK",250f,42f,bold); canvas.drawText("NR: $no",420f,42f,p)
    canvas.drawText("Data sprzedaży: $date",350f,65f,p)
    canvas.drawRect(30f,85f,285f,205f,border); canvas.drawRect(300f,85f,565f,205f,border)
    txt("Sprzedawca",40f,102f,bold); txt(seller,40f,122f,maxWidth=235f); txt("Nabywca",310f,102f,bold); txt(buyer,310f,122f,maxWidth=245f)
    canvas.drawText("Sposób zapłaty: $payment",310f,225f,p); canvas.drawText("Termin zapłaty: $due",440f,245f,p)
    val top=270f; canvas.drawRect(30f,top,565f,top+35f,border)
    val headers=listOf("Lp","Nazwa towarów lub usług","PKWiU","Jm","Ilość","Cena jedn. zł","Wartość zł")
    val xs=listOf(35f,60f,250f,310f,345f,390f,465f)
    headers.forEachIndexed{i,s->canvas.drawText(s,xs[i],top+22f,p)}
    var y=top+35f
    names.forEachIndexed{i,n-> val rowHeight=maxOf(45f,kotlin.math.ceil(p.measureText(n)/180f).toInt()*13f+22f); canvas.drawRect(30f,y,565f,y+rowHeight,border);canvas.drawText((i+1).toString(),38f,y+25f,p);txt(n,60f,y+18f,maxWidth=180f);val showPkwiu=receiptPref(c).getBoolean("show_pkwiu",true); if(showPkwiu) canvas.drawText(pkwiu[i],250f,y+25f,p);canvas.drawText(units[i],if(showPkwiu) 310f else 285f,y+25f,p);canvas.drawText(qty[i],if(showPkwiu) 345f else 330f,y+25f,p);canvas.drawText(receiptMoneyLabel(receiptMoneyCents(prices[i])),if(showPkwiu) 390f else 380f,y+25f,p);val v=(qty[i].replace(',','.').toDoubleOrNull()?:0.0)*(receiptMoneyCents(prices[i]).toDouble()/100.0);canvas.drawText(receiptMoneyLabel(kotlin.math.round(v*100).toLong()),if(showPkwiu) 465f else 480f,y+25f,p);y+=rowHeight }
    y+=20f; line(y); txt("Słownie: $words",35f,y+18f,maxWidth=350f); canvas.drawText("Razem: ${receiptMoneyLabel(kotlin.math.round(total*100).toLong())}",390f,y+18f,bold)
    y+=65f; canvas.drawText("Do zapłaty: ${receiptMoneyLabel(kotlin.math.round(total*100).toLong())}",80f,y,bold); y+=55f; txt("Imię i nazwisko oraz podpis osoby upoważnionej do wystawienia rachunku: $signature",35f,y)
    y+=35f; val notes=receiptNotes(c); val notesPaint=Paint(p).apply{textSize=7.5f}; var yy=y; notes.split("\n").forEach{row->yy=txt(row,35f,yy,notesPaint,maxWidth=520f)+2f}
    doc.finishPage(page); val out=ByteArrayOutputStream();doc.writeTo(out);doc.close();return out.toByteArray()
}
