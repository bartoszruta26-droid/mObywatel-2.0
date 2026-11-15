// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID

internal enum class DoctorPolishEvidence { DECLARED_LANGUAGE, POLISH_ORIGIN_SURNAME, POLISH_ORIGIN_CONFIRMED }
internal enum class DoctorLicenseVerification { VERIFIED_OFFICIAL_REGISTER, NUMBER_REPORTED_NOT_CHECKED, PENDING }
internal enum class DoctorConsentStatus { USER_REPORTED_ORAL, DIRECTLY_CONFIRMED, PENDING }

internal data class MedicalDoctorRecord(
 val id:String,val name:String,val specialty:String,val country:String,val city:String,val practice:String,val address:String,
 val phone:String,val website:String,val register:String,val license:String,val evidence:DoctorPolishEvidence,
 val polishDeclared:Boolean,val licenseStatus:DoctorLicenseVerification,val consent:DoctorConsentStatus,
 val source:String,val verifiedAt:String,val signerEnabled:Boolean=false
)

internal object MedicalDoctorDirectory {
 private const val PREFS="medical_doctor_directory"
 private const val KEY="records_v1"
 private fun seeds()=listOf(
  MedicalDoctorRecord("doctor-gustavo-polacow-korn","Dr Gustavo Polacow Korn","Otorynolaryngologia","Brazylia","São Paulo",
   "Consultório Médico Dr. Gustavo Polacow Korn","Av. Brig. Faria Lima 1811, cj. 907–908, Jardim Paulistano, São Paulo/SP, CEP 01452-001, Brazylia",
   "+55 11 3812-1157","https://medbook.com.br/gustavokorn","CRM-SP","101458; RQE 45010 (według profilu)",
   DoctorPolishEvidence.POLISH_ORIGIN_SURNAME,false,DoctorLicenseVerification.NUMBER_REPORTED_NOT_CHECKED,DoctorConsentStatus.USER_REPORTED_ORAL,
   "https://www.doctoralia.com.br/perfil/gustavo-polacow-korn | https://bula.com.br/estabelecimento/cons-medico-gustavo-polakow-korn-3666441 | https://crm.com.br/medico/sp/101458-gustavo-polacow-korn/","2026-11-15"),
  MedicalDoctorRecord("doctor-sergio-czerwonko","Dr Sergio Czerwonko","Geriatria; profil wymienia również psychiatrię","Argentyna","Buenos Aires",
   "Consultorio privado","Blanco Encalada 1451, Belgrano, C1428, Buenos Aires, Argentyna","+54 11 4781-2958",
   "https://www.medicoargentina.com/dr/sergio-czerwonko-11040660","Matrícula profesional — Argentyna","77456 (numer z profilu; status do sprawdzenia)",
   DoctorPolishEvidence.POLISH_ORIGIN_SURNAME,false,DoctorLicenseVerification.NUMBER_REPORTED_NOT_CHECKED,DoctorConsentStatus.USER_REPORTED_ORAL,
   "https://www.medicoargentina.com/dr/sergio-czerwonko-11040660 | https://www.doctoraliar.com/perfil/sergio-czerwonko | https://www.argentina.gob.ar/salud/buscador-nacional-de-profesionales-de-la-salud","2026-11-15"),
  MedicalDoctorRecord("doctor-pietra-stanicki","Dr Pietra Stanicki, MD","Podstawowa opieka zdrowotna","Argentyna","Buenos Aires",
   "CARE THERE — wizyty domowe i telemedycyna","Buenos Aires, Argentyna; wizyty domowe i konsultacje wideo","",
   "https://stanicki.doctorsa.com/","Argentyński rejestr REFEPS","Numer indywidualny nieopublikowany w sprawdzonym profilu",
   DoctorPolishEvidence.POLISH_ORIGIN_SURNAME,false,DoctorLicenseVerification.PENDING,DoctorConsentStatus.USER_REPORTED_ORAL,
   "https://stanicki.doctorsa.com/ | https://www.carethere.health/en/faq | https://www.argentina.gob.ar/salud/buscador-nacional-de-profesionales-de-la-salud","2026-11-15")
 )
 private fun encode(rs:List<MedicalDoctorRecord>)=JSONArray().apply { rs.forEach { d->put(JSONObject().apply {
  put("id",d.id);put("name",d.name);put("specialty",d.specialty);put("country",d.country);put("city",d.city)
  put("practice",d.practice);put("address",d.address);put("phone",d.phone);put("website",d.website)
  put("register",d.register);put("license",d.license);put("evidence",d.evidence.name);put("polish",d.polishDeclared)
  put("licenseStatus",d.licenseStatus.name);put("consent",d.consent.name);put("source",d.source)
  put("verifiedAt",d.verifiedAt);put("signerEnabled",false)
 }) } }.toString()
 private fun decode(raw:String):List<MedicalDoctorRecord>{
  val a=JSONArray(raw);return (0 until a.length()).map { i->val o=a.getJSONObject(i)
   MedicalDoctorRecord(o.getString("id"),o.getString("name"),o.optString("specialty"),o.optString("country"),o.optString("city"),
    o.optString("practice"),o.optString("address"),o.optString("phone"),o.optString("website"),o.optString("register"),o.optString("license"),
    DoctorPolishEvidence.valueOf(o.getString("evidence")),o.optBoolean("polish"),DoctorLicenseVerification.valueOf(o.getString("licenseStatus")),
    DoctorConsentStatus.valueOf(o.getString("consent")),o.optString("source"),o.optString("verifiedAt"),false)
  }
 }
 fun list(c:Context):List<MedicalDoctorRecord>{
  val p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);val raw=p.getString(KEY,null)
  if(raw==null){val initial=seeds();check(p.edit().putString(KEY,encode(initial)).commit());return initial}
  return decode(raw)
 }
 fun save(c:Context,d:MedicalDoctorRecord){
  require(d.name.isNotBlank()&&d.practice.isNotBlank()&&d.address.isNotBlank()&&d.source.contains("https://")&&d.verifiedAt.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))){
   "Wymagane: nazwisko, praktyka, adres, źródło i data weryfikacji."
  }
  require(!d.signerEnabled){"Katalog informacyjny nie aktywuje podpisywania."}
  val all=list(c).toMutableList();val i=all.indexOfFirst{it.id==d.id};if(i<0)all.add(d)else all[i]=d
  check(c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY,encode(all)).commit())
 }
 fun delete(c:Context,id:String){check(c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY,encode(list(c).filterNot{it.id==id})).commit())}
}

@Composable
internal fun MedicalDoctorDirectoryScreen(onBack:()->Unit){
 val c=LocalContext.current;var revision by remember{mutableIntStateOf(0)}
 val records=remember(revision){runCatching{MedicalDoctorDirectory.list(c)}}
 var deleting by remember{mutableStateOf<MedicalDoctorRecord?>(null)}
 var editing by remember{mutableStateOf<MedicalDoctorRecord?>(null)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("‹ Powrót",color=MaterialTheme.colorScheme.primary,modifier=Modifier.clickable(onClick=onBack))
  Text("Katalog lekarzy",style=MaterialTheme.typography.headlineSmall)
  Text("Katalog informacyjny. Kryterium: deklarowany język polski albo polsko brzmiące nazwisko / potwierdzone pochodzenie oraz publicznie potwierdzona praktyka. Nazwisko nie potwierdza znajomości języka. Podpisywanie jest wyłączone.",style=MaterialTheme.typography.bodySmall)
  Button(onClick={editing=MedicalDoctorRecord(UUID.randomUUID().toString(),"","","","","","","","","","",DoctorPolishEvidence.POLISH_ORIGIN_SURNAME,false,DoctorLicenseVerification.PENDING,DoctorConsentStatus.PENDING,"","",false)}){Text("Dodaj rekord lekarza")}
  records.onSuccess{list->list.forEach{d->
   Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text(d.name,style=MaterialTheme.typography.titleMedium);Text(d.specialty+" · "+d.city+", "+d.country)
    Text(d.practice);Text(d.address,style=MaterialTheme.typography.bodySmall)
    Text(if(d.polishDeclared)"Język polski: deklarowany" else "Kryterium: nazwisko o polskim brzmieniu; język polski niepotwierdzony")
    Text("Praktyka: potwierdzona źródłem publicznym")
    Text("Licencja: "+when(d.licenseStatus){DoctorLicenseVerification.VERIFIED_OFFICIAL_REGISTER->"zweryfikowana";DoctorLicenseVerification.NUMBER_REPORTED_NOT_CHECKED->"numer podany, status niezweryfikowany";else->"oczekuje na weryfikację"})
    Text("Zgoda: "+if(d.consent==DoctorConsentStatus.USER_REPORTED_ORAL)"ustna — zgłoszona przez administratora" else d.consent.name)
    Text("Podpisujący: wyłączony",color=MaterialTheme.colorScheme.error)
    Text("Źródła: "+d.source,style=MaterialTheme.typography.bodySmall)
    Text("Weryfikacja praktyki: "+d.verifiedAt,style=MaterialTheme.typography.labelSmall)
    Row { TextButton(onClick={editing=d}){Text("Edytuj")}; TextButton(onClick={deleting=d}){Text("Usuń wpis")} }
   }}
  }}.onFailure{Text("Błąd katalogu: "+(it.message?:"nieznany"),color=MaterialTheme.colorScheme.error)}
 }
 editing?.let{d->DoctorDirectoryRecordDialog(d,{editing=null}){updated->runCatching{MedicalDoctorDirectory.save(c,updated)}.onSuccess{editing=null;revision++}.onFailure{editing=null}} }
 deleting?.let{d->AlertDialog(onDismissRequest={deleting=null},title={Text("Usunąć wpis?")},text={Text("Usunąć "+d.name+"?")},
  confirmButton={TextButton(onClick={runCatching{MedicalDoctorDirectory.delete(c,d.id)}.onSuccess{deleting=null;revision++}.onFailure{deleting=null}){Text("Usuń")}},
  dismissButton={TextButton(onClick={deleting=null}){Text("Anuluj")}})}
}

}

@Composable
private fun DoctorDirectoryRecordDialog(initial:MedicalDoctorRecord,onDismiss:()->Unit,onSave:(MedicalDoctorRecord)->Unit){
 var name by remember(initial.id){mutableStateOf(initial.name)}
 var specialty by remember(initial.id){mutableStateOf(initial.specialty)}
 var country by remember(initial.id){mutableStateOf(initial.country)}
 var city by remember(initial.id){mutableStateOf(initial.city)}
 var practice by remember(initial.id){mutableStateOf(initial.practice)}
 var address by remember(initial.id){mutableStateOf(initial.address)}
 var phone by remember(initial.id){mutableStateOf(initial.phone)}
 var website by remember(initial.id){mutableStateOf(initial.website)}
 var register by remember(initial.id){mutableStateOf(initial.register)}
 var license by remember(initial.id){mutableStateOf(initial.license)}
 var source by remember(initial.id){mutableStateOf(initial.source)}
 var date by remember(initial.id){mutableStateOf(if(initial.verifiedAt.isBlank()) "2026-11-15" else initial.verifiedAt)}
 var polish by remember(initial.id){mutableStateOf(initial.polishDeclared)}
 var evidence by remember(initial.id){mutableStateOf(initial.evidence)}
 var consent by remember(initial.id){mutableStateOf(initial.consent)}
 AlertDialog(onDismissRequest=onDismiss,title={Text("Dane lekarza")},text={
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(5.dp)){
   @Composable fun field(v:String,label:String,change:(String)->Unit){OutlinedTextField(v,change,label={Text(label)},modifier=Modifier.fillMaxWidth())}
   field(name,"Imię i nazwisko"){name=it};field(specialty,"Specjalizacja"){specialty=it}
   field(country,"Kraj"){country=it};field(city,"Miasto"){city=it};field(practice,"Nazwa praktyki"){practice=it}
   field(address,"Adres praktyki"){address=it};field(phone,"Telefon publiczny"){phone=it};field(website,"Strona / rejestracja"){website=it}
   field(register,"Rejestr zawodowy"){register=it};field(license,"Numer licencji"){license=it}
   field(source,"Źródła potwierdzenia praktyki"){source=it};field(date,"Data weryfikacji RRRR-MM-DD"){date=it}
   Row{Checkbox(polish,{polish=it});Text("Deklaruje znajomość języka polskiego")}
   Text("Podstawa polskiego kryterium")
   Row{RadioButton(evidence==DoctorPolishEvidence.POLISH_ORIGIN_SURNAME,{evidence=DoctorPolishEvidence.POLISH_ORIGIN_SURNAME});Text("Polsko brzmiące nazwisko")}
   Row{RadioButton(evidence==DoctorPolishEvidence.POLISH_ORIGIN_CONFIRMED,{evidence=DoctorPolishEvidence.POLISH_ORIGIN_CONFIRMED});Text("Potwierdzone polskie pochodzenie")}
   Text("Zgoda na publikację")
   Row{RadioButton(consent==DoctorConsentStatus.USER_REPORTED_ORAL,{consent=DoctorConsentStatus.USER_REPORTED_ORAL});Text("Ustna — zgłoszona przez administratora")}
   Row{RadioButton(consent==DoctorConsentStatus.DIRECTLY_CONFIRMED,{consent=DoctorConsentStatus.DIRECTLY_CONFIRMED});Text("Potwierdzona bezpośrednio")}
   Row{RadioButton(consent==DoctorConsentStatus.PENDING,{consent=DoctorConsentStatus.PENDING});Text("Oczekuje")}
   Text("Podpisywanie pozostaje wyłączone.",style=MaterialTheme.typography.bodySmall)
  }
 },confirmButton={TextButton(onClick={onSave(initial.copy(name=name.trim(),specialty=specialty.trim(),country=country.trim(),city=city.trim(),
  practice=practice.trim(),address=address.trim(),phone=phone.trim(),website=website.trim(),register=register.trim(),license=license.trim(),
  source=source.trim(),verifiedAt=date.trim(),polishDeclared=polish,evidence=if(polish)DoctorPolishEvidence.DECLARED_LANGUAGE else evidence,consent=consent,
  signerEnabled=false))}){Text("Zapisz")}},dismissButton={TextButton(onClick=onDismiss){Text("Anuluj")}})
}
