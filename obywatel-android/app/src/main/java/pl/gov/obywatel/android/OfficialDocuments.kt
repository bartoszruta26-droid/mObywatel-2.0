// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class OfficialDocType(val id:String, val title:String, val fields:List<String>, val note:String = "")
private val officialDocTypes = listOf(
    OfficialDocType("birth_extract","Akt urodzenia / odpis aktu urodzenia", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwisko osoby","Data i miejsce urodzenia","Imiona i nazwiska rodziców","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy rodziców / osób potwierdzających","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej, np. przez rodzica. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("marriage_extract","Akt małżeństwa / odpis aktu małżeństwa", listOf("Rodzaj aktu: akt stanu cywilnego USC / akt rodowy","Imiona i nazwiska małżonków","Data i miejsce zawarcia małżeństwa","Osoba sporządzająca / wystawca: kierownik USC / rodzic / przedstawiciel rodu","Nazwa rodu / oznaczenie ewidencji rodowej","Numer aktu / rok / numer wpisu rodowego","Rodzaj odpisu USC: skrócony / zupełny / wielojęzyczny","Data sporządzenia / wydania","Podstawa sporządzenia / źródła danych","Podpisy małżonków / rodziców / osób potwierdzających","Świadkowie","Pieczęć / znak rodowy lub oznaczenie USC","Osoba uprawniona do uzyskania odpisu USC","Cel uzyskania / uwagi"), "Można prowadzić akt stanu cywilnego USC albo odrębny akt rodowy sporządzony w ramach ewidencji rodzinnej. Akt rodowy jest odrębnym wpisem ewidencji rodzinnej; nie jest wpisem w państwowym rejestrze stanu cywilnego ani urzędowym odpisem USC."),
    OfficialDocType("death_extract","Odpis aktu zgonu", listOf("Imiona i nazwisko osoby zmarłej","Data i miejsce zgonu","Urząd stanu cywilnego","Numer aktu / rok","Rodzaj odpisu","Wnioskodawca i podstawa uprawnienia","Cel uzyskania","Sposób odbioru"), "Formularz danych do wniosku do USC. Odpis wydaje kierownik USC, nie starosta."),
    OfficialDocType("large_family","Karta Dużej Rodziny", listOf("Imię i nazwisko","PESEL / data urodzenia","Numer karty","Typ: tradycyjna / elektroniczna","Data wydania","Ważna do","Organ wydający","Status")),
    OfficialDocType("blood_donor","Legitymacja / karta Honorowego Dawcy Krwi", listOf("Imię i nazwisko","Numer legitymacji","Stopień / tytuł","Liczba oddanych litrów (jeśli wpisana)","Data przyznania","Organ / jednostka wydająca","Status")),
    OfficialDocType("disability_card","Legitymacja osoby z niepełnosprawnością", listOf("Imię i nazwisko","Numer legitymacji","Data wydania","Data ważności","Organ wydający","Numer orzeczenia / podstawa","Stopień / wskazanie","Status"), "Legitymacja i orzeczenie to odrębne dokumenty; wpis nie zastępuje dokumentu źródłowego."),
    OfficialDocType("vehicle_registration","Dowód rejestracyjny pojazdu", listOf("Numer rejestracyjny","VIN","Marka i model","Numer dowodu rejestracyjnego","Właściciel / współwłaściciel","Data pierwszej rejestracji","Organ rejestrujący","Termin badania technicznego","Adnotacje / ograniczenia","Status")),
    OfficialDocType("insurance_vehicle","Ubezpieczenie OC pojazdu", listOf("Ubezpieczyciel","Numer polisy","Pojazd / numer rejestracyjny","Okres ochrony od","Okres ochrony do","Data opłacenia składki","Zakres / uwagi","Status")),
    OfficialDocType("insurance_professional","Ubezpieczenie OC zawodowe", listOf("Ubezpieczyciel","Numer polisy","Ubezpieczony / zawód","Zakres czynności objętych ochroną","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Wyłączenia / uwagi","Status")),
    OfficialDocType("insurance_private","Ubezpieczenie OC w życiu prywatnym", listOf("Ubezpieczyciel","Numer polisy","Ubezpieczony / gospodarstwo domowe","Zakres ochrony","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Status")),
    OfficialDocType("insurance_business","Ubezpieczenie OC działalności / firmowe", listOf("Ubezpieczyciel","Numer polisy","Nazwa firmy / NIP","Ubezpieczony podmiot","Zakres działalności i ochrony","Suma gwarancyjna","Okres ochrony od","Okres ochrony do","Wyłączenia / uwagi","Status")),
    OfficialDocType("criminal_record","Zaświadczenie o niekaralności — KRK", listOf("Imię i nazwisko","PESEL / data urodzenia","Numer zapytania / zaświadczenia","Data wydania","Organ / punkt informacyjny KRK","Zakres informacji","Cel uzyskania","Status weryfikacji pliku PDF/XML"), "Można dołączyć oryginalny plik PDF i/lub XML. Sama nazwa pliku nie potwierdza autentyczności ani aktualności zaświadczenia."),
    OfficialDocType("mission_travel","Dokumenty podróży służbowej i misji międzynarodowych", listOf("Organizacja / pracodawca","Podstawa delegowania / nominacji","Imię i nazwisko delegowanego","Funkcja / rola","Cel i zakres misji","Państwo / państwa docelowe","Data rozpoczęcia","Data zakończenia","Numer decyzji / delegacji","Ubezpieczenie i osoba kontaktowa","Wizy / zgody / akredytacje","Status dokumentów")),
    OfficialDocType("ehic","Europejska Karta Ubezpieczenia Zdrowotnego (EKUZ)", listOf("Imię i nazwisko","Numer karty","Numer identyfikacyjny / PESEL","Instytucja wydająca","Data wydania","Data ważności","Status")),
    OfficialDocType("eu_education","Zaświadczenie o zgodności wykształcenia z wymogami UE", listOf("Imię i nazwisko","Kwalifikacja / zawód","Nazwa i kraj instytucji kształcącej","Poziom i czas kształcenia","Numer dyplomu / świadectwa","Organ wydający zaświadczenie","Podstawa prawna / dyrektywa","Państwo docelowe","Data wydania","Numer sprawy / zaświadczenia","Zakres potwierdzenia","Status")),
    OfficialDocType("mayor","Dokument wójta", listOf("Imię i nazwisko","Gmina","Funkcja: wójt","Data wyboru","Data objęcia obowiązków","Kadencja od / do","Organ wyborczy","Numer i data zaświadczenia o wyborze","Podstawa prawna","Status"), "Dokument ewidencyjny. Wybór na wójta potwierdza właściwa komisja wyborcza; ten wpis nie zastępuje urzędowego zaświadczenia."),
    OfficialDocType("village_head","Dokument sołtysa", listOf("Imię i nazwisko","Sołectwo","Gmina","Data wyboru","Kadencja od / do","Organ / zebranie dokonujące wyboru","Numer i data protokołu wyboru","Statut sołectwa","Status"), "Sołtys jest wybierany zgodnie z ustawą o samorządzie gminnym i statutem sołectwa. Forma dokumentowania wyboru zależy od właściwych regulacji lokalnych."),
    OfficialDocType("county_starosta","Dokument starosty", listOf("Imię i nazwisko","Powiat","Funkcja: starosta","Data wyboru przez radę powiatu","Data objęcia obowiązków","Kadencja od / do","Numer i data uchwały","Organ podejmujący uchwałę","Status"), "Dokument ewidencyjny potwierdzający wybór i pełnienie funkcji; nie zastępuje uchwały rady powiatu."),
    OfficialDocType("citizen_record","Dokument obywatela", listOf("Imię i nazwisko","Obywatelstwo","Państwo","Podstawa nabycia / posiadania obywatelstwa","Organ lub dokument źródłowy","Numer dokumentu źródłowego","Data wydania / potwierdzenia","Status"), "Pomocniczy wpis ewidencyjny. Nie jest odrębnym ustawowym dokumentem tożsamości ani dowodem obywatelstwa; należy dołączyć dokument źródłowy."),
    OfficialDocType("ombudsman","Dokument Rzecznika Praw Obywatelskich", listOf("Imię i nazwisko","Funkcja","Data powołania przez Sejm","Zgoda Senatu — data","Kadencja od / do","Numer i data uchwały / aktu","Organ powołujący","Status"), "Dokument dotyczący konstytucyjnego organu ochrony praw i wolności; rodzaj dokumentu źródłowego należy wskazać w polach."),
    OfficialDocType("children_ombudsman","Dokument Rzecznika Praw Dziecka", listOf("Imię i nazwisko","Funkcja","Data powołania przez Sejm","Zgoda Senatu — data","Kadencja od / do","Numer i data uchwały / aktu","Organ powołujący","Status"), "Dokument dotyczący organu ochrony praw dziecka; rodzaj dokumentu źródłowego należy wskazać w polach."),
    OfficialDocType("patient_ombudsman","Dokument Rzecznika Praw Pacjenta", listOf("Imię i nazwisko","Funkcja","Data powołania","Kadencja / okres pełnienia funkcji","Numer i data aktu powołania","Organ powołujący","Status"), "Dokument dotyczący centralnego organu administracji rządowej właściwego w sprawach ochrony praw pacjenta."),
    OfficialDocType("pretrial_mediator","Dokument mediatora przedsądowego", listOf("Imię i nazwisko","Rodzaj mediacji","Podstawa wykonywania mediacji","Organizacja / ośrodek mediacyjny","Numer wpisu / zaświadczenia","Data uzyskania kwalifikacji","Zakres specjalizacji","Okres / status uprawnienia"), "Określenie „mediator przedsądowy” nie oznacza odrębnej państwowej służby ani jednolitej licencji. Wpis służy ewidencji dokumentów potwierdzających kwalifikacje, wpis na listę lub współpracę z ośrodkiem."),
    OfficialDocType("milicja_officer","Archiwalny dokument funkcjonariusza Milicji Obywatelskiej", listOf("Imię i nazwisko","Stopień","Jednostka organizacyjna MO","Stanowisko / funkcja","Numer legitymacji / dokumentu","Data przyjęcia do służby","Data zwolnienia","Okres służby","Archiwalny organ wydający"), "Wyłącznie dokument historyczny dotyczący nieistniejącej już Milicji Obywatelskiej. Nie potwierdza współczesnego statusu funkcjonariusza."),
    OfficialDocType("foreign_service_member","Dokument członka służby zagranicznej", listOf("Imię i nazwisko","Stanowisko / funkcja","Stopień służbowy — jeśli dotyczy","Jednostka organizacyjna MSZ","Placówka zagraniczna","Data nawiązania stosunku pracy / służby","Numer i data aktu","Status"), "Należy rozróżniać członka służby zagranicznej od pracownika placówki, który nie musi mieć statusu członka służby zagranicznej."),
    OfficialDocType("diplomat","Dokument dyplomaty", listOf("Imię i nazwisko","Stopień dyplomatyczny","Stanowisko","Państwo / placówka","Data nadania stopnia","Numer i data aktu","Organ wydający","Okres pełnienia funkcji","Status"), "Wzór ewidencyjny; stopnie i status wynikają z właściwych przepisów o służbie zagranicznej."),
    OfficialDocType("consul","Dokument konsula", listOf("Imię i nazwisko","Funkcja konsularna","Okręg konsularny","Państwo przyjmujące","Data mianowania / powołania","Exequatur — data, jeśli dotyczy","Numer i data dokumentu","Okres pełnienia funkcji","Status"), "Należy odróżnić akt wyznaczenia konsula od zgody państwa przyjmującego na wykonywanie funkcji konsularnych."),
    OfficialDocType("ambassador","Dokument ambasadora", listOf("Imię i nazwisko","Państwo przyjmujące","Nazwa placówki","Data mianowania","Data rozpoczęcia misji","Listy uwierzytelniające — data","Numer i data aktu","Organ wydający","Status"), "Dokument ewidencyjny. Funkcję ambasadora i rozpoczęcie misji potwierdzają właściwe akty oraz procedura akredytacji."),
    OfficialDocType("foreign_mission_head","Dokument kierownika placówki zagranicznej", listOf("Imię i nazwisko","Rodzaj placówki","Nazwa placówki","Państwo siedziby","Funkcja","Data wyznaczenia","Data objęcia obowiązków","Numer i data aktu","Okres pełnienia funkcji","Status"), "Może dotyczyć ambasady, konsulatu lub stałego przedstawicielstwa; nie każdy kierownik placówki ma rangę ambasadora."),
    OfficialDocType("consular_employee","Dokument pracownika konsularnego", listOf("Imię i nazwisko","Placówka konsularna","Stanowisko","Państwo","Status zatrudnienia","Data rozpoczęcia pracy","Numer i data dokumentu","Zakres czynności","Status"), "Dokument ewidencyjny pracownika placówki; sam wpis nie nadaje statusu urzędnika konsularnego."),
    OfficialDocType("inspector","Dokument inspektora", listOf("Imię i nazwisko","Rodzaj inspekcji / inspektoratu","Organ / jednostka zatrudniająca","Stanowisko","Zakres uprawnień kontrolnych","Numer legitymacji / upoważnienia","Data wydania","Okres ważności","Podstawa prawna","Status"), "Pozycja ogólna. Należy wskazać konkretną inspekcję, np. Państwową Inspekcję Pracy, Inspekcję Transportu Drogowego, Inspekcję Sanitarną lub Inspekcję Ochrony Środowiska.")
)

internal fun officialGraphicDocumentTargets(): List<Pair<String, String>> =
    officialDocTypes.map { it.id to it.title }

internal fun officialGraphicDocumentFields(documentId: String): List<Pair<String, String>> =
    officialDocTypes.firstOrNull { it.id == documentId }?.fields.orEmpty()
        .mapIndexed { index, label -> "field_$index" to label }

internal object OfficialDocumentStore {
    private const val ALIAS = "obywatel_official_docs_aes"
    private const val INDEX = "official-documents.enc"
    private const val DIR = "official-document-files"
    private fun key(): SecretKey {
        val ks=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS,null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes:ByteArray, encrypt:Boolean):ByteArray {
        val c=Cipher.getInstance("AES/GCM/NoPadding")
        if(encrypt){ c.init(Cipher.ENCRYPT_MODE,key()); return c.iv+c.doFinal(bytes) }
        require(bytes.size>12); c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12))); return c.doFinal(bytes.copyOfRange(12,bytes.size))
    }
    private fun file(context:Context)=File(context.noBackupFilesDir,INDEX)
    fun load(context:Context):Map<String,Map<String,String>> = runCatching {
        val f=file(context); if(!f.exists()) return emptyMap()
        val o=JSONObject(String(crypt(f.readBytes(),false),Charsets.UTF_8)); val out=mutableMapOf<String,Map<String,String>>()
        o.keys().forEach { id -> val item=o.getJSONObject(id); val m=mutableMapOf<String,String>(); item.keys().forEach { k->m[k]=item.getString(k) }; out[id]=m }
        out
    }.getOrDefault(emptyMap())
    fun save(context:Context, data:Map<String,Map<String,String>>) {
        val root=JSONObject(); data.forEach { (id,values)-> val item=JSONObject(); values.forEach { (k,v)->item.put(k,v) }; root.put(id,item) }
        val target=file(context); val tmp=File(target.parentFile,target.name+".tmp"); tmp.writeBytes(crypt(root.toString().toByteArray(Charsets.UTF_8),true))
        if(!tmp.renameTo(target)){tmp.copyTo(target,true);tmp.delete()}
    }
    fun addAttachment(context:Context, uri:Uri, docId:String):String {
        val bytes=context.contentResolver.openInputStream(uri)?.use { input ->
            val out=java.io.ByteArrayOutputStream(); val buf=ByteArray(8192); var total=0
            while(true){val n=input.read(buf);if(n<0)break;total+=n;require(total<=25*1024*1024){"Plik przekracza limit 25 MB."};out.write(buf,0,n)}
            out.toByteArray()
        } ?: error("Nie można odczytać pliku.")
        require(bytes.isNotEmpty())
        val name=runCatching { context.contentResolver.query(uri,arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),null,null,null)?.use { if(it.moveToFirst()) it.getString(0) else null } }.getOrNull() ?: "zalacznik"
        val lower=name.lowercase()
        require(lower.endsWith(".pdf") || lower.endsWith(".xml")) { "Do tego formularza można dołączyć pliki PDF lub XML." }
        if(lower.endsWith(".pdf")) require(bytes.size>=5 && String(bytes,0,5,Charsets.US_ASCII)=="%PDF-") { "Nieprawidłowy nagłówek PDF." }
        if(lower.endsWith(".xml")) {
            val text=String(bytes,Charsets.UTF_8)
            require(text.trimStart().startsWith("<?xml") || text.trimStart().startsWith("<")) { "Plik XML ma nieprawidłową strukturę początkową." }
            require(!text.contains("<!DOCTYPE",true) && !text.contains("<!ENTITY",true)) { "XML z deklaracją DTD/ENTITY jest odrzucany ze względów bezpieczeństwa." }
        }
        val dir=File(context.noBackupFilesDir,DIR).apply { mkdirs() }; val id=UUID.randomUUID().toString()
        File(dir,"$id.enc").writeBytes(crypt(bytes,true))
        val index=File(dir,"index.enc")
        val current=runCatching { if(index.exists()) JSONArray(String(crypt(index.readBytes(),false))) else JSONArray() }.getOrDefault(JSONArray())
        current.put(JSONObject().put("id",id).put("docId",docId).put("name",name).put("size",bytes.size))
        val tmp=File(dir,"index.tmp"); tmp.writeBytes(crypt(current.toString().toByteArray(Charsets.UTF_8),true)); if(!tmp.renameTo(index)){tmp.copyTo(index,true);tmp.delete()}
        return name
    }
    fun attachments(context:Context, docId:String):List<String> = runCatching {
        val f=File(File(context.noBackupFilesDir,DIR),"index.enc"); if(!f.exists()) return emptyList()
        val a=JSONArray(String(crypt(f.readBytes(),false))); (0 until a.length()).map { a.getJSONObject(it) }.filter { it.optString("docId")==docId }.map { it.optString("name") }
    }.getOrDefault(emptyList())

    fun exportArchive(context: Context): String {
        val root = JSONObject()
        load(context).forEach { (documentId, fields) ->
            val item = JSONObject()
            fields.forEach { (key, value) -> item.put(key, value) }
            root.put(documentId, item)
        }
        return root.toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = JSONObject(raw)
        val restored = linkedMapOf<String, Map<String, String>>()
        root.keys().forEach { documentId ->
            require(documentId.matches(Regex("[A-Za-z0-9_-]{1,96}"))) { "Nieprawidłowy identyfikator dokumentu." }
            val item = root.getJSONObject(documentId)
            val fields = linkedMapOf<String, String>()
            item.keys().forEach { key ->
                require(key.isNotBlank() && key.length <= 256) { "Nieprawidłowa nazwa pola dokumentu." }
                fields[key] = item.getString(key)
            }
            restored[documentId] = fields
        }
        save(context, restored)
    }
}

@Composable
internal fun OfficialDocumentsScreen(onBack:()->Unit, readOnly:Boolean=false, initialDocumentId:String?=null, onEditGraphic:(String)->Unit = {}) {
    val context= LocalContext.current
    var records by remember { mutableStateOf(OfficialDocumentStore.load(context)) }
    var selected by remember(initialDocumentId) { mutableStateOf(initialDocumentId) }
    var status by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    val attachmentPicker= rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val id=selected
        if(id!=null && uris.isNotEmpty()) runCatching {
            uris.forEach { OfficialDocumentStore.addAttachment(context,it,id) }
        }.onSuccess { status="Dodano załączniki. Pliki zapisano w zaszyfrowanym archiwum lokalnym."; refresh++ }
            .onFailure { status="Błąd załącznika: ${it.message}" }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.SemiBold, modifier=Modifier.clickable(onClick=onBack).padding(vertical=5.dp))
        Text(if(readOnly) "Dokumenty urzędowe i ubezpieczeniowe" else "Formularze i ewidencja dokumentów", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text("Wprowadzaj dane zgodnie z dokumentem źródłowym. Pliki i wpisy zachowują status nadany im przez właściwego wystawcę.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(selected==null) {
            officialDocTypes.forEach { type ->
                val values=records[type.id].orEmpty()
                val populated=values.values.any { it.isNotBlank() }
                Card(Modifier.fillMaxWidth().clickable { selected=type.id }, colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Description,contentDescription=null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(30.dp))
                        Column(Modifier.weight(1f)) {
                            Text(type.title,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
                            Text(if(populated) "Dane zapisane" else "Brak danych",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            if (!readOnly) TextButton(onClick = { onEditGraphic(type.id) }) { Text("Wygląd") }
                            Text("Otwórz",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        } else {
            val type=officialDocTypes.first { it.id==selected }
            val values=records[type.id].orEmpty()
            val settingsState = rememberLegitimacyPreviewSettings(type.id)
            val local = remember(type.id, records) { mutableStateMapOf<String, String>().apply { putAll(values) } }
            Text(type.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)
            if(type.note.isNotBlank()) Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) {
                Text(type.note,Modifier.padding(12.dp),color=MaterialTheme.colorScheme.onSurface,style=MaterialTheme.typography.bodySmall)
            }
            if (readOnly) {
                val previewSettings = settingsState.value
                val previewFields = type.fields.mapIndexed { index, label ->
                    Triple("field_$index", label.uppercase(), local["f$index"].orEmpty().ifBlank { "—" })
                }
                CredentialCardSides(
                    documentKey = type.id,
                    title = type.title.uppercase(),
                    subtitle = "PODGLĄD EWIDENCYJNY",
                    holderName = local.values.firstOrNull { it.isNotBlank() } ?: "DANE DOKUMENTU",
                    fields = previewFields,
                    settings = previewSettings
                )
                Text("Podgląd wyłącznie do odczytu. Edycja jest dostępna w Więcej → Ustawienia → dokumenty cywilne.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            } else {
                if (!LocalDocumentOcrStageRendered.current) {
                    GeneralDocumentOcrEditor("civil", local, context) { status = it }
                }
                type.fields.forEachIndexed { index, label ->
                    DocumentFieldEditor(
                        documentKey = type.id,
                        fieldKey = "field_$index",
                        label = label,
                        settingsState = settingsState,
                        value = local["f$index"].orEmpty(),
                        onValueChange = { local["f$index"] = it },
                        minLines = if (index >= 6) 2 else 1,
                        maxLines = 4
                    )
                }
                DocumentOriginalAttachments(type.id)
            }
            if (!readOnly) {
                OutlinedButton(onClick = { onEditGraphic(type.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edytuj wygląd graficzny")
                }
                val previewFields = type.fields.mapIndexed { index, label ->
                    Triple("field_$index", label.uppercase(), local["f$index"].orEmpty().ifBlank { "—" })
                }
                Text("Podgląd dokumentu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                CredentialCardSides(
                    documentKey = type.id,
                    title = type.title.uppercase(),
                    subtitle = "PODGLĄD EWIDENCYJNY",
                    holderName = local.values.firstOrNull { it.isNotBlank() } ?: "DANE DOKUMENTU",
                    fields = previewFields,
                    settings = settingsState.value
                )
            }
            if(type.id=="criminal_record") {
                if (!readOnly) OutlinedButton(onClick={attachmentPicker.launch(arrayOf("application/pdf","text/xml","application/xml"))},modifier=Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.FolderOpen,contentDescription=null); Spacer(Modifier.width(8.dp)); Text("Dodaj oryginalny plik PDF lub XML")
                }
                OfficialDocumentStore.attachments(context,type.id).forEach { Text("Załącznik: $it",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall) }
                Text("Plik PDF/XML jest przechowywany jako załącznik. Aplikacja nie weryfikuje podpisu elektronicznego, pieczęci ani statusu KRK.",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
            }
            if(!readOnly) Button(onClick={
                runCatching { OfficialDocumentStore.save(context,records+(type.id to local)); records=OfficialDocumentStore.load(context); status="Zapisano dane lokalnie." }
                    .onFailure { status="Błąd zapisu: ${it.message}" }
            },modifier=Modifier.fillMaxWidth()) { Text("Zapisz zmiany") }
            DocumentPdfExportButton(type.title, local.mapKeys { (key, _) ->
                if (key.startsWith("f")) type.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key
            }, documentType = type.id)
            if(status.isNotBlank()) Text(status,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={selected=null;status=""}) { Text("Wróć do katalogu") }
        }
        Text("Dane są szyfrowane lokalnie w pamięci aplikacji. Aplikacja nie pobiera danych z rejestrów państwowych i nie wystawia dokumentów w imieniu organów.",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.labelSmall)
    }
}
