// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
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
private const val DONATION_LEDGER_PREFS = "donation_ledgers_v1"

private fun loadDonationLedger(context: Context, id: String): Pair<String, List<DonationLedgerEntry>> {
    val raw = context.getSharedPreferences(DONATION_LEDGER_PREFS, Context.MODE_PRIVATE).getString(id, null)
        ?: return "" to emptyList()
    val root = JSONObject(raw)
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

private fun saveDonationLedger(context: Context, id: String, entityName: String, entries: List<DonationLedgerEntry>) {
    val root = JSONObject().put("schemaVersion", 1).put("entityName", entityName).put("entries", JSONArray().apply {
        entries.forEach { e -> put(JSONObject().put("id", e.id).put("date", e.date).put("donor", e.donor)
            .put("donorIdentifier", e.donorIdentifier).put("amount", e.amount).put("currency", e.currency)
            .put("method", e.method).put("purpose", e.purpose).put("evidence", e.evidence)
            .put("reference", e.reference).put("notes", e.notes)) }
    })
    check(context.getSharedPreferences(DONATION_LEDGER_PREFS, Context.MODE_PRIVATE).edit()
        .putString(id, root.toString()).commit()) { "Nie udało się zapisać ewidencji darowizn." }
}

/** Edycja wyłącznie w Profil > Ustawienia; Portfel wyświetla ten sam rejestr tylko do odczytu. */
@Composable
internal fun DonationLedgerScreen(documentId: String, readOnly: Boolean, onBack: () -> Unit) {
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
    fun persist() = saveDonationLedger(context, documentId, entityName, entries.toList())
    fun field(key: String, label: String, hint: String = "") {
        OutlinedTextField(value = draft[key].orEmpty(), onValueChange = { draft[key] = it },
            label = { Text(label) }, placeholder = { if (hint.isNotBlank()) Text(hint) },
            modifier = Modifier.fillMaxWidth(), singleLine = key !in setOf("purpose", "evidence", "notes"))
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onBack) { Text("‹ Powrót") }
        Text(definition.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (readOnly) "Podgląd ewidencji. Edycja jest dostępna w Menu główne → Profil → Ustawienia."
            else "Każda otrzymana darowizna stanowi osobny wpis. Zachowuj dowody przekazania i dokumenty źródłowe.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
