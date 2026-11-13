// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private data class ChurchDocType(val id: String, val section: String, val title: String, val fields: List<String>, val note: String)

private val churchCommon = listOf(
    "Rodzaj dokumentu", "Numer / znak dokumentu", "Pełna nazwa Kościoła / jednostki", "Tradycja / jurysdykcja / struktura kościelna",
    "Wystawca / organ", "Osoba podpisująca i jej funkcja", "Adresat / osoba, której dotyczy", "Miejscowość",
    "Data sporządzenia", "Data wejścia w życie", "Okres obowiązywania od", "Okres obowiązywania do",
    "Podstawa kanoniczna / statutowa / organizacyjna", "Zakres treści / kompetencji / rozstrzygnięcia",
    "Podpisy", "Rodzaj pieczęci i oznaczenie", "Dokument źródłowy / załączniki", "Status dokumentu", "Uwagi"
)

private fun churchDoc(id: String, section: String, title: String, specific: List<String> = emptyList(), note: String = "Wpis jest ewidencją dokumentu. Samo zapisanie formularza nie ustanawia urzędu, święceń, jurysdykcji ani upoważnienia kanonicznego.") =
    ChurchDocType(id, section, title, (specific + churchCommon).distinct(), note)

private val churchDocs = listOf(
    // 1. Posługa kościelna i liturgiczna
    churchDoc("church_bishop_appointment","1. Posługa kościelna i liturgiczna","Dekret / akt nominacyjny na urząd kościelny", listOf("Imię i nazwisko nominowanego","Nazwa urzędu / tytułu","Organ nominujący","Data i numer dekretu","Zakres urzędu","Warunki objęcia urzędu")),
    churchDoc("church_bishop_title","1. Posługa kościelna i liturgiczna","Dokument potwierdzający tytuł biskupi", listOf("Imię i nazwisko","Tytuł biskupi","Tytułowa stolica / określenie","Podmiot nadający tytuł","Dokument źródłowy")),
    churchDoc("church_orders","1. Posługa kościelna i liturgiczna","Dokument święceń / konsekracji biskupiej", listOf("Imię i nazwisko","Stopień święceń","Data i miejsce","Szafarz święceń","Współkonsekratorzy","Świadkowie","Numer księgi / wpisu","Dokumentacja sukcesji apostolskiej")),
    churchDoc("church_jurisdiction","1. Posługa kościelna i liturgiczna","Dokument jurysdykcji i zakresu kompetencji", listOf("Osoba / urząd","Rodzaj kompetencji","Terytorium / wspólnota","Zakres uprawnień","Ograniczenia","Organ udzielający","Termin obowiązywania")),
    churchDoc("church_minister_authorization","1. Posługa kościelna i liturgiczna","Akt ustanowienia / upoważnienia szafarza", listOf("Imię i nazwisko szafarza","Rodzaj posługi","Wspólnota / miejsce posługi","Organ ustanawiający","Zakres upoważnienia","Data rozpoczęcia","Termin odnowienia / wygaśnięcia")),
    churchDoc("church_minister_card","1. Posługa kościelna i liturgiczna","Legitymacja szafarza", listOf("Imię i nazwisko","Numer legitymacji","Rodzaj posługi","Wystawca","Data wydania","Ważna do","Fotografia / plik legitymacji")),
    churchDoc("church_formation","1. Posługa kościelna i liturgiczna","Zaświadczenie o formacji liturgicznej", listOf("Uczestnik","Organizator / instytucja","Nazwa formacji","Zakres programu","Liczba godzin","Data ukończenia","Wynik / ocena","Numer zaświadczenia")),
    churchDoc("church_renewal","1. Posługa kościelna i liturgiczna","Dokument odnowienia upoważnienia do posługi", listOf("Osoba","Dotychczasowy dokument","Data odnowienia","Nowy okres obowiązywania","Warunki / zakres")),
    churchDoc("church_liturgy_register","1. Posługa kościelna i liturgiczna","Rejestr celebracji i nabożeństw", listOf("Data i godzina","Rodzaj celebracji / nabożeństwa","Miejsce","Przewodniczący / prowadzący","Posługujący","Liczba uczestników","Uwagi liturgiczne")),
    churchDoc("church_eucharistic_service","1. Posługa kościelna i liturgiczna","Rejestr posługi eucharystycznej", listOf("Data","Miejsce","Rodzaj posługi","Szafarz","Liczba osób / zgłoszeń","Podstawa upoważnienia","Uwagi")),
    churchDoc("church_sick_service","1. Posługa kościelna i liturgiczna","Rejestr posługi wobec chorych", listOf("Data wizyty","Osoba / identyfikator wewnętrzny","Miejsce posługi","Rodzaj posługi","Osoba posługująca","Zgoda / podstawa wizyty","Uwagi ograniczonego dostępu")),
    churchDoc("church_liturgy_approval","1. Posługa kościelna i liturgiczna","Zatwierdzenie tekstu lub formularza liturgicznego", listOf("Nazwa tekstu","Wersja","Autor / opracowujący","Organ zatwierdzający","Data zatwierdzenia","Zakres stosowania","Warunki użycia")),
    churchDoc("church_sacramental_record","1. Posługa kościelna i liturgiczna","Ewidencja dokumentacji sakramentalnej", listOf("Rodzaj sakramentu / czynności","Osoba / identyfikator","Data i miejsce","Osoba udzielająca","Księga i numer wpisu","Adnotacje kanoniczne","Status weryfikacji"), "Moduł ewidencyjny. Księgi sakramentalne i autentyczne zaświadczenia wymagają prowadzenia przez właściwy podmiot i osoby uprawnione; aplikacja nie nadaje wpisom statusu urzędowego."),
    churchDoc("church_canonical_certificate","1. Posługa kościelna i liturgiczna","Zaświadczenie dotyczące stanu kanonicznego wiernego", listOf("Osoba, której dotyczy","Zakres zaświadczenia","Źródłowa księga / wpis","Osoba uprawniona do wydania","Data i numer zaświadczenia"), "Projekt / ewidencja zaświadczenia. Autentyczne zaświadczenie wydaje właściwy organ kościelny zgodnie z prawem i kompetencjami."),
    // 2. Kościół jako osoba prawna
    churchDoc("church_foundation_act","2. Kościół jako osoba prawna","Akt ustanowienia Kościoła / jednostki kościelnej", listOf("Data ustanowienia","Założyciel / ustanawiający","Nazwa i siedziba","Cel i zakres działalności","Struktura początkowa","Sposób reprezentacji")),
    churchDoc("church_constitution","2. Kościół jako osoba prawna","Konstytucja Kościoła", listOf("Wersja dokumentu","Data uchwalenia","Organ uchwalający","Zasady ustrojowe","Organy i kompetencje","Procedura zmiany","Data wejścia w życie")),
    churchDoc("church_statute","2. Kościół jako osoba prawna","Statut Kościoła", listOf("Wersja statutu","Organ przyjmujący","Cele statutowe","Organy","Sposób reprezentacji","Majątek i finansowanie","Zasady zmian statutu")),
    churchDoc("church_organization_rules","2. Kościół jako osoba prawna","Regulamin organizacyjny Kościoła", listOf("Jednostki organizacyjne","Podział kompetencji","Podległość służbowa / kościelna","Obieg dokumentów","Tryb podejmowania decyzji")),
    churchDoc("church_governance_rules","2. Kościół jako osoba prawna","Kodeks praw i obowiązków członków", listOf("Krąg osób objętych","Prawa członków","Obowiązki członków","Tryb przyjęcia / wystąpienia","Tryb rozpatrywania sporów")),
    churchDoc("church_leader_appointment","2. Kościół jako osoba prawna","Akt ustanowienia Zwierzchnika Kościoła", listOf("Imię i nazwisko","Urząd","Organ / tryb ustanowienia","Kadencja / czas pełnienia","Zakres kompetencji","Zasady zastępstwa")),
    churchDoc("church_board_appointment","2. Kościół jako osoba prawna","Akt powołania organu / członka organu", listOf("Nazwa organu","Imię i nazwisko członka","Funkcja","Organ powołujący","Kadencja","Zakres kompetencji","Data objęcia funkcji")),
    churchDoc("church_resolution","2. Kościół jako osoba prawna","Uchwała organu Kościoła", listOf("Numer uchwały","Nazwa organu","Data i miejsce posiedzenia","Lista obecności","Porządek obrad","Treść uchwały","Wynik głosowania","Termin wejścia w życie")),
    churchDoc("church_minutes","2. Kościół jako osoba prawna","Protokół posiedzenia organu Kościoła", listOf("Nazwa organu","Data i miejsce","Przewodniczący","Protokolant","Uczestnicy","Przebieg i ustalenia","Załączniki")),
    churchDoc("church_power_of_attorney","2. Kościół jako osoba prawna","Pełnomocnictwo / upoważnienie do reprezentacji", listOf("Mocodawca","Pełnomocnik","Zakres umocowania","Sposób reprezentacji","Ograniczenia","Data udzielenia","Termin obowiązywania","Odwołanie")),
    churchDoc("church_registry_status","2. Kościół jako osoba prawna","Dokument potwierdzający osobowość prawną / wpis do rejestru", listOf("Rodzaj rejestru","Numer wpisu","Organ prowadzący rejestr","Data wpisu","Zakres danych rejestrowych","Plik urzędowego dokumentu"), "Wprowadź dane zgodne z dokumentem źródłowym. Formularz nie stanowi samodzielnego potwierdzenia osobowości prawnej ani wpisu rejestrowego."),
    churchDoc("church_local_unit","2. Kościół jako osoba prawna","Akt utworzenia jednostki lokalnej Kościoła", listOf("Nazwa jednostki","Rodzaj jednostki","Siedziba / obszar działania","Organ tworzący","Kierownik","Zakres kompetencji","Data rozpoczęcia")),
    churchDoc("church_financial_authority","2. Kościół jako osoba prawna","Upoważnienie do czynności finansowych i majątkowych", listOf("Osoba upoważniona","Rodzaj czynności","Limity kwotowe","Wymagane zgody","Okres obowiązywania","Organ udzielający")),
    churchDoc("church_property_inventory","2. Kościół jako osoba prawna","Inwentarz dóbr kościelnych", listOf("Składnik majątku","Opis i identyfikator","Tytuł prawny","Lokalizacja","Wartość / waluta","Data nabycia","Dokument źródłowy","Osoba odpowiedzialna")),
    churchDoc("church_donation","2. Kościół jako osoba prawna","Umowa darowizny / ofiary", listOf("Darczyńca","Obdarowany","Przedmiot / kwota","Cel darowizny","Data przekazania","Warunki i ograniczenia","Potwierdzenie")),
    churchDoc("church_budget","2. Kościół jako osoba prawna","Budżet i sprawozdanie finansowe Kościoła", listOf("Okres sprawozdawczy","Przychody","Koszty","Aktywa i zobowiązania","Źródła finansowania","Osoba sporządzająca","Organ zatwierdzający","Data zatwierdzenia")),
    // 3. Misje, PDM i PUM
    churchDoc("missionary_identity","3. Misje, Papieskie Dzieła Misyjne i PUM","Legitymacja / zaświadczenie misjonarza", listOf("Imię i nazwisko","Numer legitymacji","Organizacja / struktura","Funkcja misyjna","Data wydania","Ważna do","Wystawca","Plik dokumentu")),
    churchDoc("missionary_membership","3. Misje, Papieskie Dzieła Misyjne i PUM","Dokument członkostwa / współpracy z PDM lub PUM", listOf("Organizacja","Rodzaj członkostwa / współpracy","Numer ewidencyjny","Data przyjęcia","Jednostka / koordynator","Status","Dokument potwierdzający")),
    churchDoc("missionary_call","3. Misje, Papieskie Dzieła Misyjne i PUM","Akt powołania do działalności misyjnej", listOf("Osoba powołana","Rodzaj misji","Podmiot powołujący","Cel i zadania","Obszar działalności","Data rozpoczęcia","Okres powołania")),
    churchDoc("missionary_sending","3. Misje, Papieskie Dzieła Misyjne i PUM","Dokument posłania / mandat misyjny", listOf("Misjonarz","Podmiot posyłający","Podmiot przyjmujący","Kraj / region / wspólnota","Cel i zakres misji","Data posłania","Czas trwania","Warunki i ograniczenia")),
    churchDoc("mission_area","3. Misje, Papieskie Dzieła Misyjne i PUM","Dokument określający obszar i zadania misji", listOf("Nazwa misji","Obszar geograficzny","Wspólnoty objęte działaniem","Cele","Zakres odpowiedzialności","Partnerzy lokalni","Data przeglądu")),
    churchDoc("mission_permission","3. Misje, Papieskie Dzieła Misyjne i PUM","Zgoda / porozumienie na działalność misyjną", listOf("Podmiot udzielający zgody","Misjonarz / organizacja","Miejsce i zakres działalności","Warunki","Okres obowiązywania","Dokument źródłowy")),
    churchDoc("mission_formation","3. Misje, Papieskie Dzieła Misyjne i PUM","Zaświadczenie o formacji misyjnej", listOf("Uczestnik","Organizator","Program formacji","Liczba godzin","Data ukończenia","Wynik","Numer certyfikatu")),
    churchDoc("mission_trip","3. Misje, Papieskie Dzieła Misyjne i PUM","Dokument delegacji i podróży misyjnej", listOf("Osoba delegowana","Organ delegujący","Państwo / miejsce","Cel podróży","Data wyjazdu i powrotu","Koszty i źródło finansowania","Ubezpieczenie","Wizy / zgody")),
    churchDoc("mission_report","3. Misje, Papieskie Dzieła Misyjne i PUM","Sprawozdanie z działalności misyjnej", listOf("Okres sprawozdawczy","Miejsce misji","Wykonane działania","Liczba wydarzeń / uczestników","Osiągnięte rezultaty","Trudności i potrzeby","Załączniki / fotografie","Osoba zatwierdzająca")),
    churchDoc("mission_project","3. Misje, Papieskie Dzieła Misyjne i PUM","Projekt misyjny / wniosek o finansowanie", listOf("Nazwa projektu","Wnioskodawca","Cel i uzasadnienie","Beneficjenci","Harmonogram","Budżet","Źródła finansowania","Wskaźniki rezultatów","Decyzja finansującego")),
    churchDoc("mission_donation_register","3. Misje, Papieskie Dzieła Misyjne i PUM","Ewidencja ofiar i darowizn misyjnych", listOf("Data","Darczyńca / identyfikator","Kwota i waluta","Cel","Sposób przekazania","Potwierdzenie","Sposób rozliczenia")),
    churchDoc("pum_meeting","3. Misje, Papieskie Dzieła Misyjne i PUM","Protokół spotkania formacyjnego PUM", listOf("Data i miejsce","Temat","Prowadzący","Uczestnicy","Przebieg","Wnioski i zadania","Termin kolejnego spotkania")),
    churchDoc("mission_partnership","3. Misje, Papieskie Dzieła Misyjne i PUM","Porozumienie o współpracy misyjnej", listOf("Strony porozumienia","Przedmiot współpracy","Podział zadań","Zasady finansowania","Odpowiedzialność","Czas trwania","Sposób rozwiązania")),
    // 5. Ekumenizm i współpraca międzykościelna
    churchDoc("ecumenical_declaration","5. Ekumenizm i współpraca międzykościelna","Deklaracja ekumeniczna", listOf("Strony deklaracji","Cel deklaracji","Zakres wspólnych zobowiązań","Data przyjęcia","Osoby podpisujące","Sposób publikacji")),
    churchDoc("ecumenical_agreement","5. Ekumenizm i współpraca międzykościelna","Umowa / porozumienie międzykościelne", listOf("Strony porozumienia","Reprezentanci stron","Przedmiot współpracy","Zakres działań","Zasady odpowiedzialności","Finansowanie","Czas obowiązywania","Tryb zakończenia")),
    churchDoc("ecumenical_representative","5. Ekumenizm i współpraca międzykościelna","Akt ustanowienia przedstawiciela ekumenicznego", listOf("Imię i nazwisko","Funkcja","Organ ustanawiający","Zakres reprezentacji","Partnerzy / obszar","Okres ustanowienia")),
    churchDoc("ecumenical_minutes","5. Ekumenizm i współpraca międzykościelna","Protokół spotkania ekumenicznego", listOf("Data i miejsce","Kościoły / wspólnoty uczestniczące","Delegaci","Tematy rozmów","Uzgodnienia","Dalsze działania","Podpisy")),
    churchDoc("ecumenical_joint_statement","5. Ekumenizm i współpraca międzykościelna","Wspólne oświadczenie / deklaracja", listOf("Strony","Treść oświadczenia","Zakres uzgodnienia","Data i miejsce","Podpisy przedstawicieli","Załączniki")),
    churchDoc("ecumenical_charity","5. Ekumenizm i współpraca międzykościelna","Porozumienie o wspólnej inicjatywie charytatywnej", listOf("Organizacje uczestniczące","Cel pomocy","Grupa beneficjentów","Podział obowiązków","Budżet i źródła środków","Zasady ochrony danych","Raportowanie")),
    churchDoc("interfaith_dialogue","5. Ekumenizm i współpraca międzykościelna","Dokument dialogu międzyreligijnego", listOf("Uczestniczące wspólnoty","Temat dialogu","Cel i zakres","Zasady wzajemnego poszanowania","Uzgodnienia","Osoby reprezentujące"))
)

internal fun churchGraphicDocumentTargets(): List<Pair<String, String>> =
    churchDocs.map { it.id to it.title }

internal fun churchGraphicDocumentFields(documentId: String): List<Pair<String, String>> =
    churchDocs.firstOrNull { it.id == documentId }?.fields.orEmpty()
        .mapIndexed { index, label -> "churchField_$index" to label }

internal object ChurchDocumentStore {
    private const val ALIAS = "church_documents_aes"
    private const val FILE = "church-documents.enc"
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes: ByteArray, encrypt: Boolean): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        if (encrypt) { c.init(Cipher.ENCRYPT_MODE, key()); return c.iv + c.doFinal(bytes) }
        require(bytes.size > 12); c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12))); return c.doFinal(bytes.copyOfRange(12, bytes.size))
    }
    fun load(context: Context): Map<String, Map<String, String>> = runCatching {
        val f = File(context.noBackupFilesDir, FILE); if (!f.exists()) return emptyMap()
        val root = JSONObject(String(crypt(f.readBytes(), false), Charsets.UTF_8))
        buildMap { root.keys().forEach { id -> val item = root.getJSONObject(id); put(id, buildMap { item.keys().forEach { k -> put(k, item.optString(k)) } }) } }
    }.getOrDefault(emptyMap())
    fun save(context: Context, values: Map<String, Map<String, String>>) {
        val root = JSONObject()
        values.forEach { (id, fields) -> val item = JSONObject(); fields.forEach { (k, v) -> item.put(k, v) }; root.put(id, item) }
        val target = File(context.noBackupFilesDir, FILE); val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeBytes(crypt(root.toString().toByteArray(Charsets.UTF_8), true))
        if (!temp.renameTo(target)) { temp.copyTo(target, true); temp.delete() }
    }
    
    fun exportArchive(context: Context): String {
        val root = org.json.JSONObject()
        load(context).forEach { (documentId, fields) ->
            val item = org.json.JSONObject()
            fields.forEach { (key, value) -> item.put(key, value) }
            root.put(documentId, item)
        }
        return root.toString()
    }

    fun importArchive(context: Context, raw: String) {
        val root = org.json.JSONObject(raw)
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
internal fun ChurchDocumentsScreen(onBack: () -> Unit, readOnly: Boolean = false, initialDocumentId: String? = null, onEditGraphic: (String) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var records by remember { mutableStateOf(ChurchDocumentStore.load(context)) }
    var selected by remember(initialDocumentId) { mutableStateOf(initialDocumentId) }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text("Dokumenty Kościelne", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Katalog dokumentów. Podgląd jest oddzielony od formularzy edycyjnych.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (selected == null) {
            churchDocs.groupBy { it.section }.forEach { (section, docs) ->
                Text(section, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                docs.forEach { doc ->
                    val filled = records[doc.id].orEmpty().values.any { it.isNotBlank() }
                    Card(Modifier.fillMaxWidth().clickable { selected = doc.id }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(doc.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(if (filled) "Dane zapisane" else "Brak zapisanych danych", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (!readOnly) TextButton(onClick = { onEditGraphic(doc.id) }) { Text("Wygląd") }
                                Text("Otwórz", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        } else {
            val doc = churchDocs.first { it.id == selected }
            val saved = records[doc.id].orEmpty()
            val local = remember(doc.id) { mutableStateMapOf<String, String>().apply { putAll(saved) } }
            val churchNameIndex = doc.fields.indexOfFirst { it.contains("Pełna nazwa Kościoła", ignoreCase = true) || it.contains("nazwa wspólnoty", ignoreCase = true) }
            val churchOwner = if (churchNameIndex >= 0) local["f$churchNameIndex"].orEmpty() else ""
            // Każdy dokument podrzędny ma własny klucz wyglądu; DocumentBrandStore
            // samodzielnie stosuje przypisania odziedziczone z typu nadrzędnego.
            val brandDocumentType = doc.id
            val previewFields = doc.fields.mapIndexed { index, label ->
                Triple("churchField_$index", label.uppercase(), local["f$index"].orEmpty().ifBlank { "—" })
            }
            Text(doc.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (readOnly) {
                val settings = rememberLegitimacyPreviewSettings(brandDocumentType).value
                CredentialCardSides(
                    documentKey = brandDocumentType,
                    title = doc.title.uppercase(),
                    subtitle = churchOwner.ifBlank { "DOKUMENT KOŚCIELNY" },
                    holderName = churchOwner.ifBlank { "DANE WYSTAWCY" },
                    fields = previewFields,
                    settings = settings
                )
                Text(doc.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Podgląd wyłącznie do odczytu. Edycja jest dostępna w Więcej → Ustawienia → Dokumenty kościelne.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            } else {
                GeneralDocumentOcrEditor("church", local, context) { message = it }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(doc.note, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodySmall)
                }
                doc.fields.forEachIndexed { index, label ->
                    DocumentFieldEditor(
                        documentKey = brandDocumentType,
                        fieldKey = "churchField_$index",
                        label = label,
                        value = local["f$index"].orEmpty(),
                        onValueChange = { local["f$index"] = it },
                        minLines = if (label.contains("Treść") || label.contains("Zakres") || label.contains("Uwagi")) 3 else 1,
                        maxLines = 6
                    )
                }
                DocumentOriginalAttachments(doc.id)
                DocumentPdfExportButton(doc.title, local.mapKeys { (key, _) ->
                    if (key.startsWith("f")) doc.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key
                }, documentType = brandDocumentType, ownerDefault = churchOwner)
                AssignedDocumentBrandPreview(
                    documentType = brandDocumentType,
                    owner = churchOwner,
                    title = "Znak przypisany do dokumentu",
                    subtitle = "Herb, godło lub logo wystawcy"
                )
                OutlinedButton(onClick = { onEditGraphic(doc.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edytuj wygląd graficzny")
                }
                Text("Podgląd dokumentu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                UnifiedCredentialCard(
                    documentKey = brandDocumentType,
                    title = doc.title.uppercase(),
                    subtitle = churchOwner.ifBlank { "DOKUMENT KOŚCIELNY" },
                    holderName = churchOwner.ifBlank { "DANE WYSTAWCY" },
                    fields = previewFields.take(12),
                    settings = rememberLegitimacyPreviewSettings(brandDocumentType).value
                )
                Button(onClick = {
                    runCatching {
                        ChurchDocumentStore.save(context, records + (doc.id to local))
                        records = ChurchDocumentStore.load(context)
                        message = "Zapisano dane lokalnie."
                    }.onFailure { message = "Błąd zapisu: ${it.message}" }
                }, modifier = Modifier.fillMaxWidth()) { Text("Zapisz zmiany") }
                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            DocumentPdfExportButton(doc.title, local.mapKeys { (key, _) ->
                if (key.startsWith("f")) doc.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key
            }, documentType = brandDocumentType, ownerDefault = churchOwner)
            TextButton(onClick = { selected = null; message = "" }) { Text("Wróć do katalogu") }
        }
        Text("Dane są szyfrowane lokalnie. Wpis formularza nie zastępuje dekretu, aktu święceń, autentycznego dokumentu kościelnego ani wpisu w rejestrze państwowym.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}
