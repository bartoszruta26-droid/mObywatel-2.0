// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
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

private data class ReceiptRecord(val id:String,val name:String,val createdAt:Long)
private object ReceiptStore {
    private fun dir(c:Context)=File(c.noBackupFilesDir,"receipts").apply{mkdirs()}
    private fun index(c:Context)=File(dir(c),"index.json")
    fun list(c:Context):List<ReceiptRecord> = runCatching {
        val a=JSONArray(index(c).takeIf{it.exists()}?.readText() ?: "[]")
        (0 until a.length()).map { val o=a.getJSONObject(it); ReceiptRecord(o.getString("id"),o.getString("name"),o.getLong("createdAt")) }
    }.getOrDefault(emptyList())
    fun put(c:Context,name:String,bytes:ByteArray,id:String=UUID.randomUUID().toString()):ReceiptRecord {
        require(bytes.size>5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Nieprawidłowy plik PDF." }
        require(bytes.size<=50*1024*1024) { "Plik PDF przekracza 50 MB." }
        File(dir(c),"$id.pdf").writeBytes(bytes)
        val record=ReceiptRecord(id,name.take(180),System.currentTimeMillis())
        save(c,(list(c).filterNot{it.id==id}+record))
        return record
    }
    fun bytes(c:Context,id:String)=File(dir(c),"$id.pdf").takeIf{it.isFile}?.readBytes() ?: error("Nie znaleziono pliku PDF.")
    fun delete(c:Context,id:String) { File(dir(c),"$id.pdf").delete(); save(c,list(c).filterNot{it.id==id}) }
    private fun save(c:Context,items:List<ReceiptRecord>) {
        val a=JSONArray(); items.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("createdAt",it.createdAt)) }
        index(c).writeText(a.toString())
    }
    fun import(c:Context,uri:Uri):ReceiptRecord {
        val bytes=c.contentResolver.openInputStream(uri)?.use{it.readBytes()} ?: error("Nie można odczytać PDF.")
        val name=c.contentResolver.query(uri,null,null,null,null)?.use{cur-> if(cur.moveToFirst()) { val i=cur.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(i>=0) cur.getString(i) else null } else null} ?: "rachunek.pdf"
        return put(c,name,bytes)
    }
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
    var status by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót do ustawień",Modifier.clickable(onClick=onBack),color=MaterialTheme.colorScheme.primary)
        Text("Ustawienia rachunków",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        OutlinedTextField(title,{title=it},label={Text("Nagłówek rachunku")},modifier=Modifier.fillMaxWidth())
        Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(showPkwiu,{showPkwiu=it}); Text("Pokazuj kolumnę PKWiU") }
        OutlinedTextField(notes,{notes=it},label={Text("Uwagi – treść drukowana na rachunku")},modifier=Modifier.fillMaxWidth(),minLines=7)
        Button(onClick={ receiptPref(c).edit().putString("title",title).putString("notes",notes).putBoolean("show_pkwiu",showPkwiu).apply(); status="Zapisano wzór rachunku." },modifier=Modifier.fillMaxWidth()) { Text("Zapisz wzór") }
        if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary)
    }
}

@Composable
internal fun ReceiptDocumentsScreen(onBack:()->Unit) {
    val c=LocalContext.current
    var records by remember { mutableStateOf(ReceiptStore.list(c)) }
    var status by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }; var buyer by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }; var saleDate by remember { mutableStateOf(java.text.SimpleDateFormat("dd.MM.yyyy",java.util.Locale("pl")).format(java.util.Date())) }
    var payment by remember { mutableStateOf("Gotówka") }; var due by remember { mutableStateOf("") }
    var words by remember { mutableStateOf("") }; var signature by remember { mutableStateOf("") }
    var names by remember { mutableStateOf(listOf("","","")) }; var pkwiu by remember { mutableStateOf(listOf("","","")) }
    var units by remember { mutableStateOf(listOf("szt.","","")) }; var quantities by remember { mutableStateOf(listOf("1","","")) }
    var prices by remember { mutableStateOf(listOf("0,00","","")) }
    var replaceId by remember { mutableStateOf<String?>(null) }
    val importPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri-> if(uri!=null) runCatching { val old=replaceId; val b=c.contentResolver.openInputStream(uri)?.use{it.readBytes()} ?: error("Nie można odczytać PDF."); val record=if(old!=null) ReceiptStore.put(c,"Rachunek – plik podmieniony.pdf",b,old) else ReceiptStore.import(c,uri); records=ReceiptStore.list(c); status="Zapisano: ${record.name}" }.onFailure{status="Błąd PDF: ${it.message}"}; replaceId=null }
    val savePicker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri-> if(uri!=null) runCatching { val temp=File(c.cacheDir,"receipt-export.pdf"); temp.writeBytes(pendingPdfBytes); c.contentResolver.openOutputStream(uri)?.use{it.write(pendingPdfBytes)} ?: error("Nie można zapisać PDF."); val rec=ReceiptStore.put(c,"Rachunek ${number.ifBlank{"bez numeru"}}.pdf",pendingPdfBytes); records=ReceiptStore.list(c); status="Wygenerowano i zapisano rachunek PDF: ${rec.name}" }.onFailure{status="Błąd zapisu PDF: ${it.message}"} }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("‹  Powrót",Modifier.clickable(onClick=onBack),color=MaterialTheme.colorScheme.primary)
        Text("Rachunki",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        Text("Wystaw rachunek według układu papierowego, zapisz go jako PDF lub zarządzaj zapisanymi plikami.")
        OutlinedTextField(number,{number=it},label={Text("Rachunek nr")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(saleDate,{saleDate=it},label={Text("Data sprzedaży")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(seller,{seller=it},label={Text("Sprzedawca")},modifier=Modifier.fillMaxWidth(),minLines=2)
        OutlinedTextField(buyer,{buyer=it},label={Text("Nabywca")},modifier=Modifier.fillMaxWidth(),minLines=2)
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
                OutlinedTextField(prices[i],{v->prices=prices.toMutableList().also{it[i]=v}},label={Text("Cena zł")},modifier=Modifier.weight(1f))
            }
        }
        OutlinedTextField(words,{words=it},label={Text("Słownie złotych")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(signature,{signature=it},label={Text("Imię i nazwisko osoby wystawiającej")},modifier=Modifier.fillMaxWidth())
        val total=names.indices.sumOf { i -> (quantities[i].replace(',','.').toDoubleOrNull() ?: 0.0)*(prices[i].replace(',','.').toDoubleOrNull() ?: 0.0) }
        Text("Razem: ${"%.2f".format(java.util.Locale("pl"),total)} zł",fontWeight=FontWeight.Bold)
        Button(onClick={ runCatching { pendingPdfBytes=buildReceiptPdf(c,number,saleDate,seller,buyer,payment,due,names,pkwiu,units,quantities,prices,words,signature,total); savePicker.launch("Rachunek-${number.ifBlank{"nowy"}}.pdf") }.onFailure{status="Nie udało się przygotować PDF: ${it.message}"} },modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.PictureAsPdf,null); Spacer(Modifier.width(8.dp)); Text("Wystaw i zapisz rachunek PDF") }
        Divider()
        Text("Zapisane rachunki PDF",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        OutlinedButton(onClick={replaceId=null;importPicker.launch(arrayOf("application/pdf"))},modifier=Modifier.fillMaxWidth()) { Text("Dodaj istniejący PDF") }
        if(records.isEmpty()) Text("Brak zapisanych rachunków PDF.")
        records.forEach { rec ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Filled.Description,null); Spacer(Modifier.width(8.dp)); Text(rec.name,Modifier.weight(1f),fontWeight=FontWeight.SemiBold) }
                Text(java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",java.util.Locale("pl")).format(java.util.Date(rec.createdAt)),style=MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick={replaceId=rec.id;importPicker.launch(arrayOf("application/pdf"))}) { Text("Podmień PDF") }
                    TextButton(onClick={runCatching { val bytes=ReceiptStore.bytes(c,rec.id); val f=File(c.cacheDir,rec.name); f.writeBytes(bytes); val intent=android.content.Intent(android.content.Intent.ACTION_VIEW).setDataAndType(androidx.core.content.FileProvider.getUriForFile(c,c.packageName+".ocrfiles",f),"application/pdf").addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); c.startActivity(intent) }.onFailure{status="Nie można otworzyć PDF: ${it.message}"}) { Text("Wyświetl") }
                    TextButton(onClick={ReceiptStore.delete(c,rec.id);records=ReceiptStore.list(c);status="Usunięto rachunek PDF.")}) { Icon(Icons.Filled.Delete,null); Text("Usuń") }
                }
            } }
        }
        if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary)
    }
}

private var pendingPdfBytes:ByteArray = byteArrayOf()
private fun buildReceiptPdf(c:Context,no:String,date:String,seller:String,buyer:String,payment:String,due:String,names:List<String>,pkwiu:List<String>,units:List<String>,qty:List<String>,prices:List<String>,words:String,signature:String,total:Double):ByteArray {
    val doc=PdfDocument(); val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create()); val canvas=page.canvas
    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.BLACK;textSize=10f}
    val bold=Paint(p).apply{typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);textSize=12f}
    fun line(y:Float,x1:Float=30f,x2:Float=565f){canvas.drawLine(x1,y,x2,y,p)}
    fun txt(s:String,x:Float,y:Float,paint:Paint=p){ val words=s.split(" "); var row=""; var yy=y; words.forEach{w-> val next=if(row.isEmpty())w else "$row $w"; if(paint.measureText(next)>520){canvas.drawText(row,x,yy,paint);yy+=13;row=w}else row=next}; if(row.isNotEmpty())canvas.drawText(row,x,yy,paint) }
    canvas.drawText(receiptPref(c).getString("title","RACHUNEK") ?: "RACHUNEK",250f,42f,bold); canvas.drawText("NR: $no",420f,42f,p)
    canvas.drawText("Data sprzedaży: $date",350f,65f,p)
    canvas.drawRect(30f,85f,285f,205f,p); canvas.drawRect(300f,85f,565f,205f,p)
    txt("Sprzedawca",40f,102f,bold); txt(seller,40f,122f); txt("Nabywca",310f,102f,bold); txt(buyer,310f,122f)
    canvas.drawText("Sposób zapłaty: $payment",310f,225f,p); canvas.drawText("Termin zapłaty: $due",440f,245f,p)
    val top=270f; canvas.drawRect(30f,top,565f,top+35f,p)
    val headers=listOf("Lp","Nazwa towarów lub usług","PKWiU","Jm","Ilość","Cena jedn. zł","Wartość zł")
    val xs=listOf(35f,60f,250f,310f,345f,390f,465f)
    headers.forEachIndexed{i,s->canvas.drawText(s,xs[i],top+22f,p)}
    var y=top+35f
    names.forEachIndexed{i,n-> canvas.drawRect(30f,y,565f,y+45f,p);canvas.drawText((i+1).toString(),38f,y+25f,p);txt(n,60f,y+18f);if(showPkwiu) canvas.drawText(pkwiu[i],250f,y+25f,p);canvas.drawText(units[i],if(showPkwiu) 310f else 285f,y+25f,p);canvas.drawText(qty[i],if(showPkwiu) 345f else 330f,y+25f,p);canvas.drawText(prices[i],if(showPkwiu) 390f else 380f,y+25f,p);val v=(qty[i].replace(',','.').toDoubleOrNull()?:0.0)*(prices[i].replace(',','.').toDoubleOrNull()?:0.0);canvas.drawText("%.2f".format(java.util.Locale.US,v),if(showPkwiu) 465f else 480f,y+25f,p);y+=45f }
    y+=20f; line(y); txt("Słownie złotych: $words",35f,y+18f); canvas.drawText("Razem: %.2f zł".format(java.util.Locale.US,total),420f,y+18f,bold)
    y+=65f; canvas.drawText("Do zapłaty: %.2f zł".format(java.util.Locale.US,total),80f,y,bold); y+=55f; txt("Imię i nazwisko oraz podpis osoby upoważnionej do wystawienia rachunku: $signature",35f,y)
    y+=35f; val notes=receiptNotes(c); var yy=y; notes.split("\n").forEach{row->txt(row,35f,yy,p);yy+=26f}
    doc.finishPage(page); val out=ByteArrayOutputStream();doc.writeTo(out);doc.close();return out.toByteArray()
}
