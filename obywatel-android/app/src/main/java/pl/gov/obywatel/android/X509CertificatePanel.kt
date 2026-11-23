// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun X509CertificatePanel() {
    val context = LocalContext.current
    var certificates by remember { mutableStateOf<List<X509Certificate>>(emptyList()) }
    var certificateBundleBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedLeaf by remember { mutableStateOf(0) }
    var selectedAnchor by remember { mutableStateOf(-1) }
    var alias by remember { mutableStateOf("owd-signing-key") }
    var subject by remember { mutableStateOf("CN=Użytkownik,O=Organizacja,C=PL") }
    var keyReady by remember { mutableStateOf(false) }
    var issuedCertificateMatches by remember { mutableStateOf<Boolean?>(null) }
    var pendingCsr by remember { mutableStateOf<ByteArray?>(null) }
    var report by remember { mutableStateOf("Zaimportuj certyfikat X.509 w formacie PEM lub DER albo wygeneruj klucz w Android Keystore.") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") } }

    val certificatePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    require(total <= 5 * 1024 * 1024) { "Plik przekracza limit 5 MiB." }
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            } ?: error("Nie można odczytać wybranego pliku.")
            certificateBundleBytes = bytes
            certificates = X509CertificateService.parseCertificates(bytes)
            selectedLeaf = 0
            selectedAnchor = -1
            issuedCertificateMatches = null
            report = "Odczytano certyfikaty X.509: ${certificates.size}. Okres ważności i ścieżkę zaufania należy ocenić osobno."
        }.onFailure { report = "Nie udało się odczytać certyfikatu: ${it.message}" }
    }
    val csrSaver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pkcs10")) { uri ->
        val bytes = pendingCsr
        if (uri != null && bytes != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Nie można zapisać pliku CSR.")
            pendingCsr = null
            report = "Zapisano żądanie PKCS#10 CSR. Przekaż je urzędowi certyfikacji (CA)."
        }.onFailure { report = "Nie udało się zapisać pliku CSR: ${it.message}" }
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Certyfikaty X.509 i klucze", style = MaterialTheme.typography.titleLarge)
        Text(
            "Klucze prywatne są generowane jako nieeksportowalne w Android Keystore. Certyfikaty CA należy weryfikować względem zaufanych kotwic uzyskanych z niezależnego, zaufanego źródła.",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(alias, { alias = it }, label = { Text("Alias klucza") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(subject, { subject = it }, label = { Text("Podmiot CSR (X.500)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            runCatching {
                X509CertificateService.generateSigningKey(alias.trim(), requireUserAuthentication = false)
                keyReady = true
                report = "Utworzono klucz EC P-256 w Android Keystore. Klucz prywatny nie jest eksportowalny. Uwierzytelnianie użytkownika dla każdej operacji nie jest jeszcze włączone."
            }.onFailure { report = "Nie udało się utworzyć klucza: ${it.message}" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Generuj klucz EC P-256 w Android Keystore") }
        OutlinedButton(onClick = {
            runCatching {
                pendingCsr = X509CertificateService.csrPem(
                    X509CertificateService.createCsr(alias.trim(), subject.trim())
                ).toByteArray(Charsets.US_ASCII)
                report = "Utworzono żądanie PKCS#10 CSR podpisane kluczem z Android Keystore."
                csrSaver.launch("owd-${alias.trim()}.csr.pem")
            }.onFailure { report = "Nie udało się utworzyć CSR: ${it.message}" }
        }, enabled = keyReady || alias.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Utwórz i eksportuj CSR")
        }
        OutlinedButton(onClick = { certificatePicker.launch(arrayOf("application/pkix-cert", "application/x-x509-ca-cert", "application/x-pem-file", "application/octet-stream", "text/plain")) },
            modifier = Modifier.fillMaxWidth()) { Text("Importuj certyfikaty X.509 (PEM/DER)") }

        if (certificates.isNotEmpty()) {
            Text("Zaimportowane certyfikaty: ${certificates.size}", style = MaterialTheme.typography.titleMedium)
            certificates.forEachIndexed { index, certificate ->
                val details = remember(certificate) { X509CertificateService.inspect(certificate) }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (index == selectedLeaf) "Certyfikat podmiotu" else "Certyfikat ${index + 1}", style = MaterialTheme.typography.titleSmall)
                        Text(details.subject, style = MaterialTheme.typography.bodySmall)
                        Text("Wystawca: ${details.issuer}", style = MaterialTheme.typography.bodySmall)
                        Text("Numer seryjny: ${details.serialNumberHex}", style = MaterialTheme.typography.bodySmall)
                        Text("Ważny od: ${dateFormat.format(details.notBefore)}", style = MaterialTheme.typography.bodySmall)
                        Text("Ważny do: ${dateFormat.format(details.notAfter)}", style = MaterialTheme.typography.bodySmall)
                        Text("Algorytm: ${details.signatureAlgorithm}; klucz: ${details.publicKeyAlgorithm}", style = MaterialTheme.typography.bodySmall)
                        Text("SHA-256: ${details.sha256Fingerprint}", style = MaterialTheme.typography.bodySmall)
                        Text(if (details.currentlyValid) "W okresie ważności" else "Poza okresem ważności", color = if (details.currentlyValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        if (details.keyUsage.isNotEmpty()) Text("Zastosowanie: ${details.keyUsage.joinToString()}", style = MaterialTheme.typography.bodySmall)
                        if (details.subjectAlternativeNames.isNotEmpty()) Text("SAN: ${details.subjectAlternativeNames.joinToString()}", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedLeaf == index, onCheckedChange = { if (it) selectedLeaf = index })
                            Text("Użyj jako certyfikatu podmiotu")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedAnchor == index, onCheckedChange = { selectedAnchor = if (it) index else -1 })
                            Text("Jawnie oznacz jako kotwicę zaufania")
                        }
                        OutlinedButton(onClick = {
                            runCatching {
                                X509CertificateService.requireIssuedCertificateMatchesKey(alias.trim(), certificate)
                                val orderedChain = listOf(certificate) + certificates.filter { it != certificate }
                                X509CertificateService.installIssuedCertificateChain(alias.trim(), orderedChain)
                                val bundle = certificateBundleBytes ?: error("Zaimportuj ponownie plik certyfikatu CA.")
                                val existing = DocumentBrandStore.list(context).firstOrNull {
                                    it.kind == "Certyfikat" && it.owner == alias.trim() &&
                                        it.label == "Łańcuch X.509 — ${alias.trim()}"
                                }
                                if (existing == null) {
                                    DocumentBrandStore.importBytes(
                                        context, alias.trim(), "Certyfikat",
                                        "Łańcuch X.509 — ${alias.trim()}", bundle,
                                        rights = "Pakiet certyfikatów CA powiązany z aliasem Android Keystore po porównaniu klucza publicznego. Zaufanie i unieważnienie wymagają osobnej walidacji.",
                                        mimeType = "application/octet-stream"
                                    )
                                }
                                issuedCertificateMatches = true
                                report = "Certyfikat CA odpowiada kluczowi publicznemu w Android Keystore. Pakiet certyfikatów zapisano w szyfrowanej bibliotece zasobów pod aliasem ${alias.trim()}."
                            }.onFailure {
                                issuedCertificateMatches = false
                                report = "Certyfikat nie odpowiada kluczowi albo klucz nie jest dostępny: ${it.message}"
                            }
                        }, enabled = keyReady || alias.isNotBlank()) { Text("Powiąż z kluczem Keystore") }
                    }
                }
            }
            Button(onClick = {
                runCatching {
                    require(selectedLeaf in certificates.indices) { "Wybierz certyfikat podmiotu." }
                    require(selectedAnchor in certificates.indices) { "Wskaż zaufaną kotwicę certyfikacji." }
                    val anchor = certificates[selectedAnchor]
                    val intermediates = certificates.filterIndexed { index, _ -> index != selectedLeaf && index != selectedAnchor }
                    val result = X509CertificateService.validatePath(
                        certificates[selectedLeaf], intermediates, setOf(TrustAnchor(anchor, null))
                    )
                    report = buildString {
                        append(result.message)
                        if (result.certificatePathSubjects.isNotEmpty()) {
                            append("\nŚcieżka: ")
                            append(result.certificatePathSubjects.joinToString(" → "))
                        }
                        append("\nKotwica: ")
                        append(result.trustAnchorSubject ?: anchor.subjectX500Principal.name)
                    }
                }.onFailure { report = "Błąd walidacji ścieżki: ${it.message}" }
            }, modifier = Modifier.fillMaxWidth()) { Text("Sprawdź ścieżkę PKIX") }
        }
        if (issuedCertificateMatches == true) Text("Certyfikat powiązany z kluczem.", color = MaterialTheme.colorScheme.primary)
        if (issuedCertificateMatches == false) Text("Nie potwierdzono powiązania certyfikatu z kluczem.", color = MaterialTheme.colorScheme.error)
        Text(report, style = MaterialTheme.typography.bodySmall)
        Text(
            "Zakres obecnej walidacji: odczyt X.509, okres ważności i budowanie ścieżki PKIX do jawnie wskazanej kotwicy. Sprawdzanie CRL/OCSP, unijne listy zaufane, kwalifikacja podpisu oraz walidacja PAdES pozostają zadaniem komponentu DSS po stronie serwera.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
