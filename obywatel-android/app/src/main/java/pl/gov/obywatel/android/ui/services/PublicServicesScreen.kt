// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.services

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private data class GovService(val name:String,val category:String,val description:String,val url:String,val access:String,val direct:Boolean,val source:String="gov.pl")
private val govServices=listOf(
    GovService("Katalog usług dla obywatela","Katalogi","Oficjalny katalog spraw i usług administracji publicznej.","https://www.gov.pl/web/gov/uslugi-dla-obywatela","Katalog",false),
    GovService("mObywatel — portal","mObywatel","Portal internetowy mObywatela.","https://mobywatel.gov.pl/","Portal",false,"mobywatel.gov.pl"),
    GovService("Profil Zaufany","Tożsamość","Logowanie i zarządzanie Profilem Zaufanym.","https://pz.gov.pl/","Wymaga logowania",true,"pz.gov.pl"),
    GovService("Pismo ogólne do urzędu","Sprawy urzędowe","Elektroniczne wysłanie pisma do podmiotu publicznego.","https://www.gov.pl/web/gov/wyslij-pismo-ogolne","Wymaga logowania",true),
    GovService("e-Doręczenia","Sprawy urzędowe","Elektroniczna korespondencja z podmiotami publicznymi.","https://www.gov.pl/web/e-doreczenia","Wymaga logowania",true),
    GovService("ePUAP","Sprawy urzędowe","Elektroniczna Platforma Usług Administracji Publicznej.","https://epuap.gov.pl/","Wymaga logowania",true,"epuap.gov.pl"),
    GovService("Dowód osobisty","Dokumenty","Informacje i wnioski dotyczące dowodu osobistego.","https://www.gov.pl/web/gov/uzyskaj-dowod-osobisty","Część online",true),
    GovService("Paszport","Dokumenty","Informacje o uzyskaniu paszportu.","https://www.gov.pl/web/gov/uzyskaj-paszport","Część online",true),
    GovService("Zastrzeż PESEL","Bezpieczeństwo","Zastrzeżenie lub cofnięcie zastrzeżenia PESEL.","https://www.gov.pl/web/gov/zastrzez-swoj-numer-pesel-lub-cofnij-zastrzezenie","Wymaga logowania",true),
    GovService("Punkty karne","Kierowca","Sprawdzenie punktów karnych kierowcy.","https://www.gov.pl/web/gov/sprawdz-swoje-punkty-karne","Wymaga logowania",true),
    GovService("Historia pojazdu","Kierowca","Sprawdzenie danych pojazdu.","https://historiapojazdu.gov.pl/","Usługa online",true,"historiapojazdu.gov.pl"),
    GovService("Internetowe Konto Pacjenta","Zdrowie","Dostęp do e-recept, e-skierowań i dokumentacji.","https://pacjent.gov.pl/internetowe-konto-pacjenta","Wymaga logowania",true,"pacjent.gov.pl"),
    GovService("e-Urząd Skarbowy","Podatki","Elektroniczne usługi Krajowej Administracji Skarbowej.","https://www.podatki.gov.pl/e-urzad-skarbowy/","Wymaga logowania",true,"podatki.gov.pl"),
    GovService("Twój e-PIT","Podatki","Usługa rozliczenia podatku dochodowego.","https://www.podatki.gov.pl/twoj-e-pit/","Wymaga logowania",true,"podatki.gov.pl"),
    GovService("PUE / eZUS","Praca i ubezpieczenia","Elektroniczne usługi Zakładu Ubezpieczeń Społecznych.","https://www.zus.pl/portal/logowanie.npi","Wymaga logowania",true,"zus.pl"),
    GovService("CEIDG","Firma","Wyszukiwarka i obsługa wpisów działalności gospodarczej.","https://ceidg.gov.pl/","Usługa online",true,"ceidg.gov.pl"),
    GovService("Biznes.gov.pl","Firma","Usługi administracyjne dla przedsiębiorców.","https://www.biznes.gov.pl/","Część online",true,"biznes.gov.pl"),
    GovService("KRS — eKRS","Rejestry","Wyszukiwarka i usługi rejestru przedsiębiorców i organizacji.","https://ekrs.ms.gov.pl/","Część online",true,"ms.gov.pl"),
    GovService("Księgi wieczyste","Nieruchomości","Przeglądanie elektronicznych ksiąg wieczystych.","https://ekw.ms.gov.pl/","Usługa online",true,"ms.gov.pl"),
    GovService("Geoportal","Nieruchomości","Państwowe dane przestrzenne i mapy.","https://www.geoportal.gov.pl/","Usługa online",true,"geoportal.gov.pl"),
    GovService("PFRON","Wsparcie społeczne","Informacje i usługi dla osób z niepełnosprawnościami.","https://www.pfron.org.pl/","Portal",false,"pfron.org.pl"),
    GovService("Emp@tia","Wsparcie społeczne","Portal świadczeń i pomocy społecznej.","https://empatia.mpips.gov.pl/","Wymaga logowania",true,"empatia.mpips.gov.pl"),
    GovService("Dane.gov.pl","Rejestry","Otwarty katalog danych publicznych.","https://dane.gov.pl/","Katalog",false,"dane.gov.pl"),
    GovService("Jakość powietrza GIOŚ","Środowisko","Dane pomiarowe i komunikaty o jakości powietrza.","https://powietrze.gios.gov.pl/","Usługa online",true,"gios.gov.pl")
)

private val embeddedAllowedHosts = setOf(
    "gov.pl","www.gov.pl","mobywatel.gov.pl","pz.gov.pl","epuap.gov.pl",
    "historiapojazdu.gov.pl","pacjent.gov.pl","www.podatki.gov.pl","www.zus.pl",
    "ceidg.gov.pl","www.biznes.gov.pl","ekrs.ms.gov.pl","ekw.ms.gov.pl",
    "www.geoportal.gov.pl","www.pfron.org.pl","empatia.mpips.gov.pl",
    "dane.gov.pl","powietrze.gios.gov.pl"
)

@Composable
fun PublicServicesScreen(onBack:()->Unit) {
    val context=LocalContext.current
    val scope= rememberCoroutineScope()
    var query by remember{mutableStateOf("")}
    var category by remember{mutableStateOf("Wszystkie")}
    var embeddedUrl by remember{mutableStateOf<String?>(null)}
    var embeddedTitle by remember{mutableStateOf("")}
    var pageTitle by remember{mutableStateOf("")}
    var pageProgress by remember{mutableIntStateOf(0)}
    var canGoBack by remember{mutableStateOf(false)}
    var canGoForward by remember{mutableStateOf(false)}
    var webView by remember{mutableStateOf<WebView?>(null)}
    val status= remember{mutableStateMapOf<String,String>()}
    val checkedAt= remember{mutableStateMapOf<String,String>()}
    val categories=listOf("Wszystkie")+govServices.map{it.category}.distinct().sorted()
    val visible=govServices.filter{(category=="Wszystkie"||it.category==category)&&
        (query.isBlank()||(it.name+" "+it.description+" "+it.category).contains(query,true))}
    fun openInBrowser(service:GovService) {
        embeddedTitle=service.name
        pageTitle=service.name
        embeddedUrl=service.url
    }
    BackHandler(enabled=embeddedUrl!=null) {
        val view=webView
        if(view?.canGoBack()==true) view.goBack() else {
            embeddedUrl=null
            webView=null
        }
    }
    fun check(service:GovService) {
        status[service.url]="Sprawdzanie…"
        scope.launch {
            val result=withContext(Dispatchers.IO) {
                runCatching {
                    val connection=(URL(service.url).openConnection() as HttpURLConnection)
                    connection.instanceFollowRedirects=true
                    connection.requestMethod="HEAD"
                    connection.connectTimeout=6000;connection.readTimeout=6000
                    try {
                        val code=connection.responseCode
                        if(code in 200..399) "Dostępny (HTTP "+code+")"
                        else "Wymaga sprawdzenia (HTTP "+code+")"
                    } finally { connection.disconnect() }
                }.getOrElse{"Nie udało się sprawdzić — "+(it.message ?: "błąd sieci")}
            }
            status[service.url]=result
            checkedAt[service.url]=java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.getDefault()).format(java.util.Date())
        }
    }
    if(embeddedUrl!=null) {
        Column(Modifier.fillMaxSize().semantics{paneTitle="Przeglądarka usług publicznych"}) {
            Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=4.dp)) {
                IconButton(onClick={
                    val view=webView
                    if(view?.canGoBack()==true) view.goBack() else { embeddedUrl=null; webView=null }
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack,contentDescription="Wróć") }
                Column(Modifier.weight(1f)) {
                    Text(embeddedTitle,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,maxLines=1)
                    Text(pageTitle.ifBlank { embeddedUrl!! },style=MaterialTheme.typography.labelSmall,maxLines=1)
                }
                if(pageProgress in 1..99) CircularProgressIndicator(progress={pageProgress/100f},modifier=Modifier.size(22.dp),strokeWidth=2.dp)
                IconButton(onClick={webView?.reload()}){Icon(Icons.Default.Refresh,contentDescription="Odśwież stronę")}
                IconButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(embeddedUrl)))}.onFailure{status[embeddedUrl!!]="Nie można otworzyć strony w systemowej przeglądarce."})}){
                    Icon(Icons.Default.OpenInNew,contentDescription="Otwórz poza aplikacją")
                }
            }
            LinearProgressIndicator(progress={pageProgress/100f},modifier=Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceVariant)
            AndroidView(
                modifier=Modifier.weight(1f).fillMaxWidth(),
                factory={ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled=true
                        settings.domStorageEnabled=true
                        settings.loadsImagesAutomatically=true
                        settings.javaScriptCanOpenWindowsAutomatically=false
                        settings.setSupportMultipleWindows(false)
                        settings.allowFileAccess=false
                        settings.allowContentAccess=false
                        settings.mixedContentMode=android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        webChromeClient=object:WebChromeClient(){
                            override fun onProgressChanged(view:WebView,newProgress:Int){pageProgress=newProgress}
                            override fun onReceivedTitle(view:WebView,title:String?){pageTitle=title.orEmpty()}
                        }
                        webViewClient=object:WebViewClient(){
                            override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean {
                                val uri=request.url
                                val scheme=uri.scheme?.lowercase()
                                val host=uri.host?.lowercase()
                                if(scheme=="https" && host in embeddedAllowedHosts) return false
                                if(scheme=="http"||scheme=="https") {
                                    runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}
                                    return true
                                }
                                return true
                            }
                            override fun onPageFinished(view:WebView,url:String?){
                                canGoBack=view.canGoBack()
                                canGoForward=view.canGoForward()
                            }
                        }
                        webView=this
                        loadUrl(embeddedUrl!!)
                    }
                },
                update={view->
                    if(view.url!=embeddedUrl) view.loadUrl(embeddedUrl!!)
                    canGoBack=view.canGoBack()
                    canGoForward=view.canGoForward()
                },
                onRelease={view->view.stopLoading();view.destroy();if(webView===view)webView=null}
            )
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                TextButton(onClick={if(canGoBack)webView?.goBack()}){Text("Wstecz")}
                TextButton(onClick={if(canGoForward)webView?.goForward()}){Text("Dalej")}
                TextButton(onClick={webView?.reload()}){Text("Odśwież")}
            }
            Text("Przeglądarka jest osadzonym widokiem stron. Logowanie i działanie usług zależą od polityki danego portalu; dla stron blokujących WebView można otworzyć przeglądarkę zewnętrzną.",modifier=Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=4.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Surface(Modifier.fillMaxSize().semantics{paneTitle="Katalog usług publicznych"},color=MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,contentDescription="Wróć")}
                Text("Usługi publiczne",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f).semantics{heading()})
            }
            Text("Wyszukaj usługę i otwórz jej stronę we wbudowanej przeglądarce. Logowanie odbywa się bezpośrednio w portalu, gdy jest przez niego obsługiwane.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Szukaj usługi")})
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                categories.forEach{FilterChip(selected=category==it,onClick={category=it},label={Text(it)})}
            }
            Text(visible.size.toString()+" usług • "+visible.count{it.direct}.toString()+" oznaczonych jako dostępne online",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
            LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                items(visible,key={it.url}) { service ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                Text(service.name,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                                IconButton(onClick={check(service)}){Icon(Icons.Default.Refresh,contentDescription="Sprawdź odnośnik")}
                                IconButton(onClick={openInBrowser(service)}){Icon(Icons.Default.OpenInNew,contentDescription="Otwórz w przeglądarce")}
                            }
                            Text(service.description,style=MaterialTheme.typography.bodySmall)
                            Text(service.category+" • "+service.access,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                            Text(status[service.url] ?: "Stan odnośnika: nie sprawdzono",style=MaterialTheme.typography.labelSmall)
                            if(checkedAt[service.url]!=null) Text("Sprawdzono: "+checkedAt[service.url],style=MaterialTheme.typography.labelSmall)
                            Text("Źródło: "+service.source,style=MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
