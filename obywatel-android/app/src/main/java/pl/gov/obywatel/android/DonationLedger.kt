// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.math.BigDecimal
import java.math.RoundingMode

internal object DonationLedgerCatalog {
    const val PERSON = "donation_ledger_person"
    const val FOUNDATION = "donation_ledger_foundation"
    const val ASSOCIATION = "donation_ledger_association"
    const val CHURCH = "donation_ledger_church"
    data class Definition(val id: String, val title: String, val entityLabel: String, val group: String)
    val all = listOf(
        Definition(PERSON, "Ewidencja darowizn osoby fizycznej", "Imię i nazwisko obdarowanego", "Służba cywilna"),
        Definition(FOUNDATION, "Ewidencja darowizn fundacji", "Pełna nazwa fundacji", "Służba fundacyjna"),
        Definition(ASSOCIATION, "Ewidencja darowizn stowarzyszenia", "Pełna nazwa stowarzyszenia", "Służba stowarzyszeniowa"),
        Definition(CHURCH, "Ewidencja darowizn Kościoła", "Nazwa Kościoła / jednostki kościelnej", "Służba kościelna")
    ).associateBy { it.id }
    fun find(id: String): Definition? = all[id]
}

private data class DonationLedgerEntry(
    val id: String, val date: String, val donor: String, val donorIdentifier: String,
    val amount: String, val currency: String, val method: String, val purpose: String,
    val evidence: String, val reference: String, val notes: String
)
private object DonationLedgerStore {
    private const val KEY_ALIAS = "donation_ledger_aes"
    private const val FILE_NAME = "donation_ledgers.enc"
    private const val IV_LENGTH = 12
    private const val MAX_BYTES = 8 * 1024 * 1024

    private fun decrypt(context: Context): JSONObject {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return JSONObject()
        require(file.length() in (IV_LENGTH + 1).toLong()..MAX_BYTES.toLong()) { "Plik ewidencji darowizn ma nieprawidłowy rozmiar." }
        val encrypted = file.readBytes()
        require(encrypted.size > IV_LENGTH && encrypted.size <= MAX_BYTES) { "Nieprawidłowy plik ewidencji darowizn." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, encrypted.copyOfRange(0, IV_LENGTH)))
        return JSONObject(String(cipher.doFinal(encrypted.copyOfRange(IV_LENGTH, encrypted.size)), Charsets.UTF_8))
    }

    fun load(context: Context, id: String): Pair<String, List<DonationLedgerEntry>> {
        val root = decrypt(context).optJSONObject(id) ?: return "" to emptyList()
        val array = root.optJSONArray("entries") ?: JSONArray()
        val entries = buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(DonationLedgerEntry(
                    item.optString("id"), item.optString("date"), item.optString("donor"),
                    item.optString("donorIdentifier"), item.optString("amount"), item.optString("currency", "PLN"),
                    item.optString("method"), item.optString("purpose"), item.optString("evidence"),
                    item.optString("reference"), item.optString("notes")
                ))
            }
        }
        return root.optString("entityName") to entries
    }

    fun save(context: Context, id: String, entityName: String, entries: List<DonationLedgerEntry>) {
        require(id in DonationLedgerCatalog.all.keys)
        val root = decrypt(context)
        root.put(id, JSONObject().put("entityName", entityName).put("entries", JSONArray().apply {
            entries.forEach { e -> put(JSONObject().put("id", e.id).put("date", e.date).put("donor", e.donor)
                .put("donorIdentifier", e.donorIdentifier).put("amount", e.amount).put("currency", e.currency)
                .put("method", e.method).put("purpose", e.purpose).put("evidence", e.evidence)
                .put("reference", e.reference).put("notes", e.notes)) }
        }))
        val plaintext = root.toString().toByteArray(Charsets.UTF_8)
        require(plaintext.size <= MAX_BYTES - IV_LENGTH) { "Ewidencja darowizn przekracza dozwolony rozmiar." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(plaintext)
        require(encrypted.size <= MAX_BYTES) { "Zaszyfrowana ewidencja darowizn przekracza dozwolony rozmiar." }
        writeMedicalCertificateAtomically(context.noBackupFilesDir, FILE_NAME, encrypted)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build())
        return generator.generateKey()
    }
}

private fun loadDonationLedger(context: Context, id: String): Pair<String, List<DonationLedgerEntry>> =
    DonationLedgerStore.load(context, id)

private fun donationTotalsAcrossLedgers(context: Context): Map<String, BigDecimal> {
    val totals = sortedMapOf<String, BigDecimal>()
    DonationLedgerCatalog.all.keys.forEach { id ->
        DonationLedgerStore.load(context, id).second.forEach entryLoop@ { entry ->
            val amount = entry.amount.trim().replace("\u00a0", "").replace(" ", "").replace(",", ".")
                .toBigDecimalOrNull() ?: return@entryLoop
            val currency = entry.currency.trim().uppercase().ifBlank { "PLN" }
            totals[currency] = (totals[currency] ?: BigDecimal.ZERO).add(amount)
        }
    }
    return totals
}

private fun saveDonationLedger(context: Context, id: String, entityName: String, entries: List<DonationLedgerEntry>) =
    DonationLedgerStore.save(context, id, entityName, entries)

/** Edycja wyłącznie w Profil > Ustawienia; Portfel wyświetla ten sam rejestr tylko do odczytu. */
@Composable
internal fun DonationLedgerScreen(documentId: String, readOnly: Boolean, onBack: () -> Unit, onEdit: (() -> Unit)? = null) {
    val definition = requireNotNull(DonationLedgerCatalog.find(documentId))
    val context = LocalContext.current
    val initial = remember(context, documentId) { loadDonationLedger(context, documentId) }
    var entityName by remember(documentId) { mutableStateOf(initial.first) }
    val entries = remember(documentId) { mutableStateListOf<DonationLedgerEntry>().apply { addAll(initial.second) } }
    val draft = remember(documentId) { mutableStateMapOf<String, String>().apply {
        listOf("date", "donor", "donorIdentifier", "amount", "method", "purpose", "evidence", "reference", "notes")
            .forEach { put(it, "") }
        put("currency", "PLN")
    } }
    var message by remember(documentId) { mutableStateOf("") }
    var aggregateVersion by remember { mutableIntStateOf(0) }
    val aggregateTotals = remember(context, aggregateVersion) { donationTotalsAcrossLedgers(context) }
    fun persist() {
        saveDonationLedger(context, documentId, entityName, entries.toList())
        aggregateVersion++
    }
    @Composable fun field(key: String, label: String, hint: String = "") {
        OutlinedTextField(value = draft[key].orEmpty(), onValueChange = { draft[key] = it },
            label = { Text(label) }, placeholder = { if (hint.isNotBlank()) Text(hint) },
            modifier = Modifier.fillMaxWidth(), singleLine = key !in setOf("purpose", "evidence", "notes"))
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onBack) { Text("‹ Powrót") }
        Text(definition.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Łączna suma darowizn ze wszystkich czterech ewidencji",
                    fontWeight = FontWeight.SemiBold)
                if (aggregateTotals.isEmpty()) Text("Brak zapisanych kwot.")
                aggregateTotals.forEach { (currency, total) ->
                    Text(total.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + currency,
                        style = MaterialTheme.typography.titleMedium)
                }
                Text("Sumy są rozdzielone według waluty; kwoty w różnych walutach nie są dodawane bez przeliczenia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(if (readOnly) "Podgląd ewidencji. Wprowadzanie i zmiana danych są dostępne w Więcej → Rachunki płatnicze → Ewidencja darowizn."
            else "Każda otrzymana darowizna stanowi osobny wpis. Zachowuj dowody przekazania i dokumenty źródłowe.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (readOnly && onEdit != null) {
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Przejdź do wprowadzania danych")
            }
        }
        if (!readOnly) {
            OutlinedTextField(entityName, { entityName = it }, label = { Text(definition.entityLabel) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("Nowy wpis ewidencji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            field("date", "Data otrzymania darowizny", "RRRR-MM-DD")
            field("donor", "Darczyńca — imię i nazwisko / nazwa")
            field("donorIdentifier", "Identyfikator darczyńcy (opcjonalnie)", "NIP / PESEL / inny identyfikator")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(draft["amount"].orEmpty(), { draft["amount"] = it }, label = { Text("Kwota / wartość") },
                    modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(draft["currency"].orEmpty(), { draft["currency"] = it.uppercase() },
                    label = { Text("Waluta") }, modifier = Modifier.weight(0.7f), singleLine = true)
            }
            field("method", "Forma przekazania", "Przelew / gotówka / rzecz / prawo")
            field("purpose", "Cel darowizny / przeznaczenie")
            field("evidence", "Dowód przekazania / dokument źródłowy")
            field("reference", "Numer rachunku / tytuł przelewu / referencja")
            field("notes", "Uwagi i warunki")
            Button(onClick = {
                val required = listOf("date", "donor", "amount", "currency", "method")
                if (entityName.isBlank()) message = "Uzupełnij dane podmiotu prowadzącego ewidencję."
                else if (required.any { draft[it].isNullOrBlank() }) message =
                    "Uzupełnij datę, darczyńcę, wartość, walutę i formę przekazania."
                else {
                    entries.add(DonationLedgerEntry(UUID.randomUUID().toString(), draft["date"].orEmpty(),
                        draft["donor"].orEmpty(), draft["donorIdentifier"].orEmpty(), draft["amount"].orEmpty(),
                        draft["currency"].orEmpty(), draft["method"].orEmpty(), draft["purpose"].orEmpty(),
                        draft["evidence"].orEmpty(), draft["reference"].orEmpty(), draft["notes"].orEmpty()))
                    persist()
                    listOf("date", "donor", "donorIdentifier", "amount", "method", "purpose", "evidence", "reference", "notes")
                        .forEach { draft[it] = "" }
                    draft["currency"] = "PLN"
                    message = "Wpis dodany i zapisany."
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Dodaj wpis do ewidencji") }
            if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Text("Wpisy ewidencji (${entries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (entries.isEmpty()) Text("Ewidencja nie zawiera jeszcze wpisów.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        entries.forEachIndexed { index, entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Wpis ${index + 1} · ${entry.date}", fontWeight = FontWeight.SemiBold)
                    Text("Darczyńca: ${entry.donor}")
                    if (entry.donorIdentifier.isNotBlank()) Text("Identyfikator: ${entry.donorIdentifier}")
                    Text("Wartość: ${entry.amount} ${entry.currency}")
                    Text("Forma: ${entry.method}")
                    if (entry.purpose.isNotBlank()) Text("Cel: ${entry.purpose}")
                    if (entry.evidence.isNotBlank()) Text("Dowód: ${entry.evidence}")
                    if (entry.reference.isNotBlank()) Text("Referencja: ${entry.reference}")
                    if (entry.notes.isNotBlank()) Text("Uwagi: ${entry.notes}")
                    if (!readOnly) TextButton(onClick = { entries.removeAt(index); persist(); message = "Wpis usunięty." }) {
                        Text("Usuń wpis")
                    }
                }
            }
        }
        if (!readOnly) Text("Ewidencja pomocnicza. Nie zastępuje ksiąg rachunkowych, ewidencji wymaganych przepisami podatkowymi ani pokwitowania darowizny.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onBack) { Text("Powrót") }
    }
}
