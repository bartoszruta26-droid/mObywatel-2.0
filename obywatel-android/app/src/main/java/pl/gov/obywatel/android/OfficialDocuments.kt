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
