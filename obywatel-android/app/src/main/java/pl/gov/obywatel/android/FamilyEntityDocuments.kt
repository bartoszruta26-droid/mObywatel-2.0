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
import androidx.compose.ui.platform.testTag
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

private data class RegistryDoc(val id: String, val title: String, val section: String, val fields: List<String>, val note: String = "")

private val rodBase = listOf("Nazwa Rodu", "Numer / symbol ewidencyjny", "Data sporządzenia", "Miejsce sporządzenia", "Osoba sporządzająca", "Funkcja osoby sporządzającej", "Podstawa / źródła danych", "Osoby, których dotyczy dokument", "Treść / ustalenia", "Podpisy / potwierdzenia", "Znak lub pieczęć Rodu", "Załączniki", "Status", "Uwagi")
private val rodDocs = listOf(
    "Genealogia i stan Rodu" to listOf("Akt rodowy urodzenia","Akt rodowy małżeństwa","Akt rodowy zgonu","Księga Rodowa","Drzewo genealogiczne","Rejestr członków Rodu","Rejestr pokrewieństwa i powinowactwa","Księga zmian stanu rodzinnego","Kronika Rodu"),
    "Ustrój i organizacja Rodu" to listOf("Konstytucja Rodowa","Statut Rodowy","Regulamin Rodu","Akt ustanowienia Rodu","Deklaracja przynależności Rodowej","Akt przyjęcia do Rodu","Akt wystąpienia z Rodu / wykreślenia z ewidencji","Regulamin Rady Rodowej","Regulamin reprezentacji Rodu","Kodeks praw i obowiązków członków Rodu","Zasady sukcesji funkcji i odpowiedzialności"),
    "Władze i decyzje Rodowe" to listOf("Akt powołania Ojca Rodu","Akt powołania Matki Rodu","Akt ustanowienia Potomka","Uchwała Rady Rodowej","Protokół posiedzenia Rady Rodowej","Rejestr decyzji Rodowych","Pełnomocnictwo Rodowe","Upoważnienie do reprezentowania Rodu","Oświadczenie członka Rodu","Akt przekazania funkcji"),
    "Majątek i gospodarka Rodu" to listOf("Rejestr majątku Rodowego","Ewidencja nieruchomości Rodowych","Ewidencja pojazdów Rodowych","Umowa użyczenia rodzinna","Umowa najmu rodzinna","Umowa darowizny","Umowa pożyczki rodzinnej","Rejestr zobowiązań i należności","Ewidencja wspólnych wydatków","Protokół przekazania majątku","Dokumentacja spadkowa i testamentowa","Rejestr posiadanych rachunków bankowych (PLN i waluty obce)"),
    "Dokumenty społeczne, edukacyjne i religijne" to listOf("Legitymacja członka Rodu","Zaświadczenie o przynależności Rodowej","Świadectwo / dyplom","Akt nadania tytułu lub wyróżnienia","Dokument chrztu","Dokument bierzmowania","Dokument ślubu kościelnego","Zaświadczenie o funkcji religijnej","Dokument misji i posługi","Księga zasług i działalności społecznej"),
    "Odpisy Rodowe" to listOf("Odpis rodowy aktu urodzenia","Odpis rodowy aktu małżeństwa","Odpis rodowy aktu zgonu"),
    "Dokumentacja medyczna członków Rodu" to listOf("Dane medyczne małżonka","Dane medyczne ojca","Dane medyczne matki","Dane medyczne dziadka","Dane medyczne babci","Dane medyczne członka Rodu — pierwsza linia","Dane medyczne członka Rodu — druga linia","Dane medyczne innego członka Rodu")
).flatMap { (section, titles) -> titles.mapIndexed { index, title ->
    val medicalFields = listOf("Imię i nazwisko","Data urodzenia","Relacja Rodowa / stopień pokrewieństwa lub powinowactwa","Linia Rodowa (pierwsza / druga / inna)","Dane kontaktowe","Osoba do kontaktu w nagłym przypadku","Telefon alarmowy / ICE","Grupa krwi (jeśli potwierdzona)","Alergie i reakcje niepożądane","Choroby przewlekłe i rozpoznania (z datą i źródłem)","Przebyte choroby, operacje i hospitalizacje","Leki stosowane obecnie (nazwa, dawka, pora, od kiedy)","Leki stosowane okresowo i doraźnie","Wyroby medyczne i implanty","Szczepienia (nazwa, dawka, data)","Niepełnosprawność / ograniczenia funkcjonalne","Ważne wyniki badań (data, jednostka, zakres referencyjny)","Lekarz / placówka prowadząca — dane przekazane dobrowolnie","Zalecenia i plan opieki uzgodniony z pacjentem","Preferencje i potrzeby opiekuńcze","Źródło informacji / dokumentacja źródłowa","Data ostatniej aktualizacji","Osoba wprowadzająca dane","Zgoda / podstawa upoważnienia do przetwarzania danych","Zakres udzielonego upoważnienia i okres jego obowiązywania","Ograniczenia dostępu / osoby upoważnione","Uwagi")
    val odpisFields = listOf("Numer odpisu","Numer aktu źródłowego","Księga Rodowa / tom / strona / pozycja","Imiona i nazwisko osoby, której dotyczy akt","Data i miejsce zdarzenia","Data i miejsce sporządzenia aktu źródłowego","Data wydania odpisu","Miejsce wydania odpisu","Wnioskodawca","Cel wydania odpisu","Zakres odpisu (pełny / skrócony)","Treść odpisu / wierne odwzorowanie wpisu","Klauzula: potwierdzenie zgodności z wpisem w Księdze Rodowej","Imię i nazwisko Starosty Rodowego","Podstawa pełnienia funkcji Starosty Rodowego","Podpis Starosty Rodowego (do złożenia przy wydaniu)","Rodzaj podpisu (własnoręczny / graficzny / elektroniczny)","Pieczęć / znak Rodu","Data i miejsce podpisania","Osoba sporządzająca projekt odpisu","Załączniki i dokumenty porównawcze","Status odpisu (projekt / wydany / anulowany)","Uwagi") + rodBase
    val fields = when {
        title.startsWith("Dane medyczne") -> medicalFields
        title.startsWith("Odpis rodowy aktu") -> odpisFields
        title.contains("urodzenia") -> listOf("Imiona i nazwisko potomka","Data i miejsce urodzenia","Rodzice / opiekunowie","Miejsce w strukturze Rodu","Osoba zgłaszająca","Świadkowie / potwierdzający") + rodBase
        title.contains("małżeństwa") -> listOf("Imiona i nazwiska małżonków","Data i miejsce zawarcia związku","Świadkowie","Rodziny / linie Rodowe") + rodBase
        title.contains("zgonu") -> listOf("Imiona i nazwisko zmarłego","Data i miejsce zgonu","Osoba zgłaszająca","Osoby powiadomione") + rodBase
        title.contains("powołania") || title.contains("ustanowienia") || title.contains("przekazania funkcji") -> listOf("Osoba obejmująca funkcję","Funkcja / urząd","Osoba lub organ powołujący","Data rozpoczęcia","Czas trwania / warunki","Zakres kompetencji","Data zakończenia / przekazania") + rodBase
        title.contains("majątku") || title.contains("nieruchomości") || title.contains("pojazdów") -> listOf("Składnik majątku","Opis / identyfikator","Właściciel / współwłaściciele","Tytuł prawny","Wartość / waluta","Lokalizacja","Data nabycia / zbycia") + rodBase
        title.contains("umowa") -> listOf("Strony umowy","Przedmiot umowy","Data zawarcia","Okres obowiązywania","Wartość / świadczenia","Warunki rozwiązania","Osoby podpisujące") + rodBase
        title.contains("Uchwała") || title.contains("Protokół") || title.contains("Rejestr decyzji") -> listOf("Numer uchwały / protokołu","Organ podejmujący decyzję","Data posiedzenia","Lista obecności","Porządek obrad","Treść decyzji","Wynik głosowania","Data wejścia w życie") + rodBase
        else -> rodBase
    }
    val note = when {
        title.startsWith("Dane medyczne") -> "Zawiera szczególne kategorie danych osobowych dotyczących zdrowia. Wprowadzaj wyłącznie dane niezbędne, zgodnie z prawem i na podstawie właściwej zgody lub innej podstawy prawnej; osoba, której dane dotyczą, powinna znać cel i zakres przetwarzania. Nie wpisuj haseł ani danych dostępowych. Rejestr lokalny nie zastępuje dokumentacji medycznej prowadzonej przez uprawniony podmiot."
        title.startsWith("Odpis rodowy") -> "Prywatny odpis z ewidencji Rodowej. Podpis Starosty Rodowego należy złożyć dopiero po weryfikacji wpisu i umocowania. Formularz nie tworzy podpisu ani pieczęci i nie jest urzędowym odpisem aktu stanu cywilnego."
        else -> "Dokument ewidencji Rodowej. Jego skutki prawne zależą od treści, formy i właściwych przepisów; sam wpis w aplikacji nie zastępuje aktu notarialnego, wpisu urzędowego ani wymaganej formy czynności."
    }
    RegistryDoc(if (title.contains("rachunków bankowych")) "rod_bank_accounts" else "rod_${section.hashCode().toUInt().toString(16)}_${index}", title, section, if (title.contains("rachunków bankowych")) emptyList() else fields.distinct(), note)
} }

private val entityBase = listOf("Pełna nazwa podmiotu","Forma prawna","KRS / CEIDG / inny rejestr","NIP","REGON","Siedziba / adres","Data sporządzenia / wydania","Organ / osoba wystawiająca","Podstawa prawna / statutowa","Osoba wprowadzająca dane","Załączniki / plik źródłowy","Status dokumentu","Uwagi")
private val entityDocs = listOf(
    "Dokumenty założycielskie i rejestrowe" to listOf("Akt założycielski","Statut","Umowa spółki","Odpis aktualny KRS","Odpis pełny KRS","Zaświadczenie o wpisie do CEIDG","Zaświadczenie o nadaniu NIP","Zaświadczenie o nadaniu REGON","Potwierdzenie statusu podatnika VAT","Akt / dokument utworzenia oddziału lub jednostki lokalnej","Zmiana statutu lub umowy","Historia zmian danych rejestrowych"),
    "Dokumenty organów i władz" to listOf("Akt powołania Prezesa","Akt powołania Członka Zarządu","Akt powołania Rady Nadzorczej","Akt odwołania z funkcji","Uchwała Zarządu","Uchwała Rady Nadzorczej","Protokół posiedzenia organu","Rejestr uchwał","Regulamin Zarządu","Regulamin Rady Nadzorczej","Pełnomocnictwo","Prokura","Wzór podpisu / zasady reprezentacji"),
    "Dokumenty właścicielskie i kontrolne" to listOf("Rejestr wspólników","Lista udziałowców","Lista akcjonariuszy","Dokument objęcia udziałów","Dokument zbycia udziałów","Księga akcyjna","Dokumentacja beneficjentów rzeczywistych","Potwierdzenie zgłoszenia CRBR","Struktura właścicielska","Umowa inwestycyjna","Dokument wniesienia wkładu / kapitału","Oświadczenie właściciela"),
    "Finanse i rachunki bankowe" to listOf("Rejestr rachunków bankowych podmiotu (wiele kont i walut)","Potwierdzenie prowadzenia rachunku bankowego","Dokument otwarcia / zamknięcia rachunku")
).flatMap { (section, titles) -> titles.mapIndexed { index, title ->
    val fields = when {
        title.contains("KRS") -> listOf("Numer KRS","Rodzaj odpisu","Data stanu danych","Sąd rejestrowy","Dział / rubryka","Treść wpisów / wzmianki") + entityBase
        title.contains("CEIDG") -> listOf("Imię i nazwisko przedsiębiorcy","Nazwa firmy","NIP / REGON","Numer wpisu","Data rozpoczęcia działalności","Status działalności") + entityBase
        title.contains("powołania") || title.contains("odwołania") -> listOf("Imię i nazwisko osoby","Funkcja","Organ powołujący / odwołujący","Data uchwały","Data rozpoczęcia / zakończenia funkcji","Sposób reprezentacji","Zakres umocowania") + entityBase
        title.contains("Uchwała") || title.contains("Protokół") || title.contains("Regulamin") -> listOf("Numer dokumentu","Organ","Data posiedzenia / przyjęcia","Uczestnicy","Treść / przedmiot","Wynik głosowania","Data obowiązywania") + entityBase
        title.contains("udział") || title.contains("akcjonarius") || title.contains("wspólnik") || title.contains("wkładu") -> listOf("Wspólnik / akcjonariusz","Liczba i rodzaj udziałów / akcji","Wartość nominalna","Data nabycia / objęcia","Data zbycia / umorzenia","Podstawa wpisu") + entityBase
        title.contains("pełnomocnictwo") || title.contains("Prokura") -> listOf("Mocodawca","Pełnomocnik / prokurent","Rodzaj i zakres umocowania","Data udzielenia","Okres obowiązywania","Sposób odwołania") + entityBase
        else -> entityBase
    }
    RegistryDoc(if (title.contains("rachunków bankowych")) "entity_bank_accounts" else "entity_${section.hashCode().toUInt().toString(16)}_${index}", title, section, if (title.contains("rachunków bankowych")) emptyList() else fields.distinct(), "Ewidencja dokumentu podmiotu. Wpisane dane nie potwierdzają samodzielnie aktualnego stanu rejestru; dokument urzędowy należy dołączyć jako źródło.")
} }

private object FamilyEntityDocumentStore {
    private const val ALIAS = "family_entity_docs_aes"
    private const val FILE = "family-entity-documents.enc"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun crypt(bytes: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        if (encrypt) { cipher.init(Cipher.ENCRYPT_MODE, key()); return cipher.iv + cipher.doFinal(bytes) }
        require(bytes.size > 12); cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12))); return cipher.doFinal(bytes.copyOfRange(12, bytes.size))
    }
    fun load(context: Context): Map<String, Map<String, String>> = runCatching {
        val file = File(context.noBackupFilesDir, FILE); if (!file.exists()) return emptyMap()
        val root = JSONObject(String(crypt(file.readBytes(), false), Charsets.UTF_8))
        buildMap { root.keys().forEach { id -> val obj = root.getJSONObject(id); put(id, buildMap { obj.keys().forEach { k -> put(k, obj.optString(k)) } }) } }
    }.getOrDefault(emptyMap())
    fun save(context: Context, values: Map<String, Map<String, String>>) {
        val root = JSONObject()
        values.forEach { (id, fields) -> val item = JSONObject(); fields.forEach { (k, v) -> item.put(k, v) }; root.put(id, item) }
        val target = File(context.noBackupFilesDir, FILE); val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeBytes(crypt(root.toString().toByteArray(Charsets.UTF_8), true))
        if (!temp.renameTo(target)) { temp.copyTo(target, true); temp.delete() }
    }
}

@Composable
internal fun FamilyEntityDocumentsScreen(entityMode: Boolean, onBack: () -> Unit, bankAccountsOnly: Boolean = false, readOnly: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bankDocuments = (rodDocs + entityDocs).filter { it.id == "rod_bank_accounts" || it.id == "entity_bank_accounts" }
    val catalog = if (bankAccountsOnly) bankDocuments else (if (entityMode) entityDocs else rodDocs).filterNot { it.id == "rod_bank_accounts" || it.id == "entity_bank_accounts" }
    var records by remember { mutableStateOf(FamilyEntityDocumentStore.load(context)) }
    var selected by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf(readOnly) }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹  Powrót", color = Color(0xFF1769AA), modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Text(if (bankAccountsOnly) "Konta bankowe" else if (entityMode) "Dokumenty rejestru podmiotów" else "Dokumenty Rodowe", modifier = if (bankAccountsOnly) Modifier.testTag("bank-accounts-screen-title") else Modifier, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (bankAccountsOnly) "Osobna ewidencja rachunków prywatnych i podmiotowych. Dane są przechowywane lokalnie w postaci zaszyfrowanej." else "Katalog formularzy z edycją danych, zapisem lokalnym i podglądem dokumentu. Dane są przechowywane lokalnie w postaci zaszyfrowanej.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
        if (selected == null) {
            catalog.groupBy { it.section }.forEach { (section, docs) ->
                Text(section, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF173B58))
                docs.forEach { doc ->
                    val filled = records[doc.id].orEmpty().values.any { it.isNotBlank() }
                    Card(Modifier.fillMaxWidth().clickable { selected = doc.id; preview = readOnly }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) { Text(doc.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF173B58)); Text(if (filled) "Dane zapisane" else "Formularz pusty", style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788)) }
                            Text("Otwórz", color = Color(0xFF1769AA))
                        }
                    }
                }
            }
        } else {
            val doc = catalog.first { it.id == selected }
            val saved = records[doc.id].orEmpty()
            var local by remember(doc.id) { mutableStateOf(saved) }
            var accountCount by remember(doc.id) { mutableIntStateOf((saved["accountCount"]?.toIntOrNull() ?: 1).coerceIn(1, 30)) }
            val isBankRegister = doc.id == "rod_bank_accounts" || doc.id == "entity_bank_accounts"
            val accountFields = listOf("Nazwa własna rachunku", "Bank / instytucja prowadząca", "Waluta (np. PLN, EUR, USD, CHF)", "Numer rachunku / IBAN", "Kod BIC / SWIFT", "Posiadacz / współposiadacze", "Rodzaj rachunku", "Kraj prowadzenia rachunku", "Status rachunku", "Data otwarcia", "Data zamknięcia", "Cel / przeznaczenie", "Uwagi")
            Text(doc.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            DocumentIdentityTemplate(doc.title, local.mapKeys { (key, _) -> if (key.startsWith("f")) doc.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key })
            Text(doc.note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF667788))
            if (isBankRegister) {
                Text("Rejestr obsługuje do 30 rachunków w różnych bankach i walutach. Każdy rachunek jest osobnym wpisem, a numer IBAN i dane właściciela są przechowywane w zaszyfrowanym archiwum lokalnym.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1769AA))
                repeat(accountCount) { accountIndex ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8FB))) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Rachunek bankowy \${accountIndex + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF173B58))
                            accountFields.forEachIndexed { fieldIndex, label ->
                                val key = "account_\${accountIndex}_\${fieldIndex}"
                                val value = local[key].orEmpty()
                                if (preview) {
                                    if (value.isNotBlank()) Column {
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF667788))
                                        Text(value, style = MaterialTheme.typography.bodyLarge)
                                    }
                                } else OutlinedTextField(value = value, onValueChange = { local = local + (key to it) },
                                    label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
                                    singleLine = fieldIndex in listOf(2, 3, 4, 8, 9, 10),
                                    minLines = if (fieldIndex == 12) 2 else 1, maxLines = 4)
                            }
                        }
                    }
                }
                if (!preview) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { if (accountCount < 30) accountCount++ }, modifier = Modifier.weight(1f), enabled = accountCount < 30) { Text("Dodaj rachunek") }
                    OutlinedButton(onClick = { if (accountCount > 1) { accountCount--; local = local.filterKeys { key -> !key.startsWith("account_\${accountCount}_") } } }, modifier = Modifier.weight(1f), enabled = accountCount > 1) { Text("Usuń ostatni") }
                }
            } else doc.fields.forEachIndexed { index, label ->
                val value = local["f$index"].orEmpty()
                if (preview) {
                    if (value.isNotBlank()) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF667788)); Text(value, style = MaterialTheme.typography.bodyLarge) }
                } else OutlinedTextField(value = value, onValueChange = { local = local + ("f$index" to it) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), minLines = if (label.contains("Treść") || label.contains("Uwagi") || label.contains("Podstawa")) 3 else 1, maxLines = 5)
            }
            if (isBankRegister) {
                if (!preview) Text("Dla rachunków walutowych wpisuj kod ISO 4217 waluty (np. EUR, USD, CHF, GBP). Nie przechowuj PIN-ów, haseł, kodów autoryzacyjnych ani danych logowania do bankowości.", color = Color(0xFF9A5B00), style = MaterialTheme.typography.labelSmall)
                if (preview) Text("Liczba zarejestrowanych pozycji: \$accountCount", color = Color(0xFF667788), style = MaterialTheme.typography.labelSmall)
            }
            if (preview) Text("PODGLĄD EWIDENCYJNY — dokument prywatny / roboczy. Nie stanowi samodzielnie urzędowego odpisu, wpisu rejestrowego ani poświadczenia.", color = Color(0xFF9A5B00), style = MaterialTheme.typography.labelSmall)
            if (!readOnly) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { preview = !preview }, modifier = Modifier.weight(1f)) { Text(if (preview) "Edytuj dane" else "Podgląd") }
                    if (!preview) Button(onClick = { runCatching { FamilyEntityDocumentStore.save(context, records + (doc.id to (if (isBankRegister) local + ("accountCount" to accountCount.toString()) else local))); records = FamilyEntityDocumentStore.load(context); message = "Zapisano formularz." }.onFailure { message = "Błąd zapisu: ${it.message}" } }, modifier = Modifier.weight(1f)) { Text("Zapisz") }
                }

            } else {
                Text("Widok tylko do odczytu. Zmiana danych jest dostępna w Więcej → Ustawienia.",
                    style = MaterialTheme.typography.labelSmall, color = Color(0xFF667788))
            }
            if (message.isNotBlank()) Text(message, color = Color(0xFF1769AA), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { selected = null; message = "" }) { Text("Wróć do katalogu") }
            DocumentPdfExportButton(doc.title, local.mapKeys { (key, _) ->
                if (key.startsWith("f")) doc.fields.getOrNull(key.removePrefix("f").toIntOrNull() ?: -1) ?: key else key
            })
        }
    }
}
