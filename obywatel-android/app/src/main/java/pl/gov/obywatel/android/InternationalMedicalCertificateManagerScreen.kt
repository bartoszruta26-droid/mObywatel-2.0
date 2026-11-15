// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
internal fun InternationalMedicalCertificateManagerScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    var showDoctorDirectory by remember { mutableStateOf(false) }
    if (showDoctorDirectory) { MedicalDoctorDirectoryScreen(onBack = { showDoctorDirectory = false }); return }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf("") }
    val records = remember(refresh) { runCatching { InternationalMedicalCertificateRecords.list(context) } }
    val loadError = records.exceptionOrNull()?.message.orEmpty()
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹  Powrót do dokumentów", color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("International Medical Certificates", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Otwórz zapisane zaświadczenie, aby je wyświetlić lub edytować. Usunięcie wymaga potwierdzenia.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = { showDoctorDirectory = true }, modifier = Modifier.fillMaxWidth()) { Text("Katalog lekarzy") }
        Button(onClick = { onEdit(UUID.randomUUID().toString().replace("-", "")) },
            modifier = Modifier.fillMaxWidth()) { Text("New certificate") }
        if (records.isFailure) {
            Text(loadError.ifBlank { "Nie udało się odczytać zapisanych zaświadczeń." }, color = MaterialTheme.colorScheme.error)
        } else if (records.getOrThrow().isEmpty()) {
            Text("Brak zapisanych zaświadczeń.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records.getOrThrow(), key = { it.first }) { (id, values) ->
                    val title = values["doc_international_medical_certificate_documentNumber"]
                        ?.takeIf { it.isNotBlank() }
                        ?: values["doc_international_medical_certificate_patientName"]?.takeIf { it.isNotBlank() }
                        ?: "Medical certificate"
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(title, fontWeight = FontWeight.SemiBold)
                            Text("ID: $id", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onEdit(id) }, modifier = Modifier.weight(1f)) { Text("Open / Edit") }
                                TextButton(onClick = { deleteId = id }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
        if (actionError.isNotBlank()) Text(actionError, color = MaterialTheme.colorScheme.error)
    }
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete certificate?") },
            text = { Text("This permanently removes the selected encrypted certificate from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    val result = runCatching { InternationalMedicalCertificateRecords.delete(context, id) }
                    if (result.isSuccess) refresh++ else actionError = result.exceptionOrNull()?.message ?: "Delete failed."
                    deleteId = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } }
        )
    }
}
