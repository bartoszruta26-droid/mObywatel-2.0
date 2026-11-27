// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import pl.gov.obywatel.android.DonationAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.obywatel.android.financial.BankStatementEntry
import pl.gov.obywatel.android.financial.BankExchangeOperationProjector
import pl.gov.obywatel.android.financial.ExchangeOperationStatus
import pl.gov.obywatel.android.financial.BankImportPreview
import pl.gov.obywatel.android.financial.PaymentLedgerRepository
import pl.gov.obywatel.android.financial.FinancialReportPeriod
import pl.gov.obywatel.android.financial.FinancialReportSafWriter
import pl.gov.obywatel.android.financial.PaymentAccountScope
import pl.gov.obywatel.android.financial.StoredFinancialTransaction
import pl.gov.obywatel.android.financial.BankStatementXmlParser
import pl.gov.obywatel.android.financial.BankStatementCsvParser
import pl.gov.obywatel.android.financial.PaymentLedgerJsonCodec
import pl.gov.obywatel.android.Asset
import pl.gov.obywatel.android.CashDonation
import pl.gov.obywatel.android.DonationCustodyStatus
import pl.gov.obywatel.android.AssetKind
import pl.gov.obywatel.android.Fee
import pl.gov.obywatel.android.FinancialRecordSource
import pl.gov.obywatel.android.FinancialParseStatus
import pl.gov.obywatel.android.FinancialTransaction
import pl.gov.obywatel.android.FinancialTransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Ekran rejestru odczytuje dane z SQLite przez repozytorium pracujące na IO. */
@Composable
fun PaymentLedgerScreen(onBack: () -> Unit, onOpenDonationLedger: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { PaymentLedgerRepository(context) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<BankStatementEntry>>(emptyList()) }
    var financialTransactions by remember { mutableStateOf<List<StoredFinancialTransaction>>(emptyList()) }
    var assets by remember { mutableStateOf<List<Asset>>(emptyList()) }
    var assetCodeInput by remember { mutableStateOf("") }
    var assetNameInput by remember { mutableStateOf("") }
    var assetUnitInput by remember { mutableStateOf("") }
    var assetPrecisionInput by remember { mutableStateOf("2") }
    var assetPurityInput by remember { mutableStateOf("") }
    var assetKindInput by remember { mutableStateOf(AssetKind.CURRENCY) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var importPreview by remember { mutableStateOf<BankImportPreview?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var reportFromDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1).toString()) }
    var reportToDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var pendingReportPeriod by remember { mutableStateOf<FinancialReportPeriod?>(null) }
    var accountScopeId by remember { mutableStateOf("ipko-account-1") }
    var accountScopes by remember { mutableStateOf<List<PaymentAccountScope>>(emptyList()) }
    var transactionType by remember { mutableStateOf(FinancialTransactionType.EXCHANGE) }
    var executedAtText by remember { mutableStateOf(Instant.now().toString()) }
    var fromAssetText by remember { mutableStateOf("PLN") }
    var fromAmountText by remember { mutableStateOf("") }
    var toAssetText by remember { mutableStateOf("USD") }
    var toAmountText by remember { mutableStateOf("") }
    var feeAssetText by remember { mutableStateOf("") }
    var feeAmountText by remember { mutableStateOf("") }
    var selectedBankEntryIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var donations by remember { mutableStateOf<List<CashDonation>>(emptyList()) }
    var donationAttachments by remember { mutableStateOf<List<DonationAttachment>>(emptyList()) }
    var pendingDonationAttachmentId by remember { mutableStateOf<String?>(null) }
    var donationAmountText by remember { mutableStateOf("") }
    var donationCurrencyText by remember { mutableStateOf("PLN") }
    var donationOrganizationText by remember { mutableStateOf("") }
    var donationPurposeText by remember { mutableStateOf("") }
    var donationDonorText by remember { mutableStateOf("") }
    var donationAnonymous by remember { mutableStateOf(false) }
    var donationCampaignText by remember { mutableStateOf("") }
    var donationCollectedByText by remember { mutableStateOf("") }
    var donationReceiptText by remember { mutableStateOf("") }
    var donationStatus by remember { mutableStateOf(DonationCustodyStatus.HELD) }
    var donationTransferredAmountText by remember { mutableStateOf("") }
    var donationTransferredAtText by remember { mutableStateOf("") }
    var donationNoteText by remember { mutableStateOf("") }
    var editingDonationId by remember { mutableStateOf<String?>(null) }
    var editingTransactionId by remember { mutableStateOf<String?>(null) }
    var expandedExchangeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val exchangeOperations = remember(entries) { BankExchangeOperationProjector.project(entries) }
    val groupedEntryIds = remember(exchangeOperations) {
        exchangeOperations.flatMap { it.legs }.map { it.accountingEntryId() }.toSet()
    }
    val bankEntryOwners = remember(financialTransactions) {
        financialTransactions.flatMap { stored ->
            stored.accountingEntryIds.map { entryId -> entryId to stored.transaction.id }
        }.toMap()
    }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                entries = repository.getEntries()
                financialTransactions = repository.getFinancialTransactions()
                assets = repository.getAssets()
                donations = repository.getCashDonations()
                donationAttachments = repository.getDonationAttachments()
                accountScopes = repository.getAccountScopes()
                if (accountScopes.none { it.accountScopeId == accountScopeId }) {
                    accountScopeId = accountScopes.firstOrNull()?.accountScopeId ?: "ipko-account-1"
                }
            } catch (exception: Exception) {
                error = exception.message ?: "Nie udało się odczytać rejestru."
            } finally {
                loading = false
            }
        }
    }

    fun resetTransactionForm() {
        editingTransactionId = null
        transactionType = FinancialTransactionType.EXCHANGE
        executedAtText = Instant.now().toString()
        fromAssetText = "PLN"
        fromAmountText = ""
        toAssetText = "USD"
        toAmountText = ""
        feeAssetText = ""
        feeAmountText = ""
        selectedBankEntryIds = emptySet()
    }

    fun saveTransactionForm() {
        scope.launch {
            try {
                val fromCode = if (transactionType == FinancialTransactionType.DEPOSIT) null else fromAssetText.trim().uppercase()
                val toCode = if (transactionType == FinancialTransactionType.WITHDRAWAL) null else toAssetText.trim().uppercase()
                val previous = financialTransactions.firstOrNull {
                    it.transaction.id == editingTransactionId
                }?.transaction
                val transaction = FinancialTransaction(
                    id = editingTransactionId ?: UUID.randomUUID().toString(),
                    executedAt = Instant.parse(executedAtText.trim()),
                    type = transactionType,
                    fromAssetCode = fromCode,
                    fromAmount = fromCode?.let { BigDecimal(fromAmountText.trim()) },
                    toAssetCode = toCode,
                    toAmount = toCode?.let { BigDecimal(toAmountText.trim()) },
                    fee = if (feeAmountText.isBlank() && feeAssetText.isBlank()) null else
                        Fee(feeAssetText.trim().uppercase(), BigDecimal(feeAmountText.trim())),
                    source = previous?.source ?: FinancialRecordSource.MANUAL,
                    externalId = previous?.externalId,
                    rawDescription = previous?.rawDescription,
                    parseStatus = previous?.parseStatus ?: FinancialParseStatus.MANUAL
                )
                transaction.validate()
                fun validatePrecision(
                    code: String?,
                    amount: BigDecimal?,
                    unchangedImportedValue: Boolean
                ) {
                    if (code == null || amount == null) return
                    val definition = assets.firstOrNull { it.code == code }
                    // Starsze/importowane rekordy mogą zawierać kody spoza lokalnego słownika.
                    // Pozwalamy zachować wyłącznie niezmienioną parę kod+kwota; każda zmiana
                    // wartości nadal wymaga definicji i walidacji jej precyzji.
                    if (definition == null && unchangedImportedValue) return
                    requireNotNull(definition) {
                        "Aktywo $code nie istnieje w słowniku. Dodaj je przed zapisaniem transakcji."
                    }
                    require(amount.stripTrailingZeros().scale().coerceAtLeast(0) <= definition.decimalPlaces) {
                        "Ilość $code przekracza precyzję słownika (${definition.decimalPlaces} miejsc dziesiętnych)."
                    }
                }
                val importedRecord = previous != null && previous.source != FinancialRecordSource.MANUAL
                validatePrecision(
                    transaction.fromAssetCode,
                    transaction.fromAmount,
                    importedRecord && transaction.fromAssetCode == previous?.fromAssetCode &&
                        transaction.fromAmount == previous?.fromAmount
                )
                validatePrecision(
                    transaction.toAssetCode,
                    transaction.toAmount,
                    importedRecord && transaction.toAssetCode == previous?.toAssetCode &&
                        transaction.toAmount == previous?.toAmount
                )
                transaction.fee?.let { fee ->
                    validatePrecision(
                        fee.assetCode,
                        fee.amount,
                        importedRecord && fee == previous?.fee
                    )
                }
                repository.saveFinancialTransaction(transaction, selectedBankEntryIds)
                message = if (editingTransactionId == null) "Zapisano transakcję." else "Zaktualizowano transakcję."
                error = null
                resetTransactionForm()
                reload()
            } catch (exception: Exception) {
                error = exception.message ?: "Nie udało się zapisać transakcji."
            }
        }
    }

    fun resetDonationForm() {
        editingDonationId = null
        donationAmountText = ""
        donationCurrencyText = "PLN"
        donationOrganizationText = ""
        donationPurposeText = ""
        donationDonorText = ""
        donationAnonymous = false
        donationCampaignText = ""
        donationCollectedByText = ""
        donationReceiptText = ""
        donationStatus = DonationCustodyStatus.HELD
        donationTransferredAmountText = ""
        donationTransferredAtText = ""
        donationNoteText = ""
    }

    fun saveDonationForm() {
        scope.launch {
            try {
                val previous = donations.firstOrNull { it.id == editingDonationId }
                val donation = CashDonation(
                    id = editingDonationId ?: UUID.randomUUID().toString(),
                    receivedAt = previous?.receivedAt ?: Instant.now(),
                    amount = BigDecimal(donationAmountText.trim()),
                    currencyCode = donationCurrencyText.trim().uppercase(),
                    beneficiaryOrganizationName = donationOrganizationText.trim(),
                    purpose = donationPurposeText.trim(),
                    donorName = if (donationAnonymous) null else donationDonorText.trim().ifBlank { null },
                    isAnonymous = donationAnonymous,
                    campaignName = donationCampaignText.trim().ifBlank { null },
                    collectedBy = donationCollectedByText.trim().ifBlank { null },
                    receiptNumber = donationReceiptText.trim().ifBlank { null },
                    custodyStatus = donationStatus,
                    transferredAt = donationTransferredAtText.trim().takeIf { it.isNotBlank() }?.let(Instant::parse),
                    transferredAmount = donationTransferredAmountText.trim().takeIf { it.isNotBlank() }?.let(::BigDecimal),
                    note = donationNoteText.trim().ifBlank { null },
                    source = previous?.source ?: FinancialRecordSource.MANUAL
                )
                donation.validate()
                if (editingDonationId == null) repository.saveCashDonation(donation)
                else repository.updateCashDonation(donation)
                donations = repository.getCashDonations()
                message = if (editingDonationId == null) "Zapisano darowiznę." else "Zaktualizowano darowiznę."
                error = null
                resetDonationForm()
            } catch (exception: Exception) {
                error = exception.message ?: "Nie udało się zapisać darowizny."
            }
        }
    }

    fun editDonation(donation: CashDonation) {
        editingDonationId = donation.id
        donationAmountText = donation.amount.toPlainString()
        donationCurrencyText = donation.currencyCode
        donationOrganizationText = donation.beneficiaryOrganizationName
        donationPurposeText = donation.purpose
        donationDonorText = donation.donorName.orEmpty()
        donationAnonymous = donation.isAnonymous
        donationCampaignText = donation.campaignName.orEmpty()
        donationCollectedByText = donation.collectedBy.orEmpty()
        donationReceiptText = donation.receiptNumber.orEmpty()
        donationStatus = donation.custodyStatus
        donationTransferredAmountText = donation.transferredAmount?.toPlainString().orEmpty()
        donationTransferredAtText = donation.transferredAt?.toString().orEmpty()
        donationNoteText = donation.note.orEmpty()
    }

    fun saveAssetForm() {
        scope.launch {
            try {
                val asset = Asset(
                    code = assetCodeInput.trim().uppercase(),
                    name = assetNameInput.trim(),
                    kind = assetKindInput,
                    unit = assetUnitInput.trim(),
                    decimalPlaces = assetPrecisionInput.trim().toInt(),
                    purity = if (assetPurityInput.isBlank()) null else assetPurityInput.trim().toInt()
                )
                asset.validate()
                repository.saveAsset(asset)
                assets = repository.getAssets()
                message = "Zapisano aktywo ${asset.code}."
                error = null
            } catch (exception: Exception) {
                error = exception.message ?: "Nie udało się zapisać definicji aktywa."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Nie można odczytać wybranego pliku.")
                        input.use { readUtf8Limited(it, MAX_IMPORT_BYTES) }
                    }
                    val preview = repository.previewJson(json)
                    pendingJson = json
                    importPreview = preview
                    message = "Wczytano plik do podglądu."
                    error = null
                } catch (exception: Exception) {
                    pendingJson = null
                    importPreview = null
                    error = exception.message ?: "Nie udało się przygotować importu."
                }
            }
        }
    }
    val xmlImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val selectedAccountScopeId = accountScopeId.trim()
            scope.launch {
                try {
                    // Każdy zakres użyty do importu musi być zarejestrowany lokalnie,
                    // aby po ponownym uruchomieniu nie powstał osierocony alias.
                    val knownScopes = repository.getAccountScopes()
                    if (knownScopes.none { it.accountScopeId == selectedAccountScopeId }) {
                        repository.saveAccountScope(selectedAccountScopeId, selectedAccountScopeId)
                        accountScopes = repository.getAccountScopes()
                    }
                    val archiveJson = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Nie można odczytać wybranego pliku XML.")
                        val xml = input.use { readUtf8Limited(it, MAX_IMPORT_BYTES) }
                        val parsedEntries = BankStatementXmlParser.parse(xml, selectedAccountScopeId)
                        PaymentLedgerJsonCodec.encode(parsedEntries)
                    }
                    val preview = repository.previewJson(archiveJson)
                    pendingJson = archiveJson
                    importPreview = preview
                    message = "XML iPKO odczytano. Sprawdź podgląd przed zatwierdzeniem."
                    error = null
                } catch (exception: Exception) {
                    pendingJson = null
                    importPreview = null
                    error = exception.message ?: "Nie udało się odczytać pliku XML."
                }
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val selectedAccountScopeId = accountScopeId.trim()
            scope.launch {
                try {
                    val knownScopes = repository.getAccountScopes()
                    if (knownScopes.none { it.accountScopeId == selectedAccountScopeId }) {
                        repository.saveAccountScope(selectedAccountScopeId, selectedAccountScopeId)
                        accountScopes = repository.getAccountScopes()
                    }
                    val archiveJson = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Nie można odczytać wybranego pliku CSV.")
                        val bytes = input.use { readBytesLimited(it, MAX_IMPORT_BYTES) }
                        val parsedEntries = BankStatementCsvParser.parse(bytes, selectedAccountScopeId)
                        PaymentLedgerJsonCodec.encode(parsedEntries)
                    }
                    val preview = repository.previewJson(archiveJson)
                    pendingJson = archiveJson
                    importPreview = preview
                    message = "Plik CSV banku odczytano. Sprawdź podgląd przed zatwierdzeniem."
                    error = null
                } catch (exception: Exception) {
                    pendingJson = null
                    importPreview = null
                    error = exception.message ?: "Nie udało się odczytać pliku CSV."
                }
            }
        }
    }

    val donationAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val donationId = pendingDonationAttachmentId
        pendingDonationAttachmentId = null
        if (uri != null && donationId != null) {
            scope.launch {
                try {
                    val attachment = repository.attachDonationDocument(donationId, uri)
                    donationAttachments = repository.getDonationAttachments()
                    message = "Dodano załącznik ${attachment.fileName}; SHA-256: ${attachment.sha256}."
                    error = null
                } catch (exception: Exception) {
                    error = exception.message ?: "Nie udało się dodać załącznika."
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = repository.exportJson()
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: throw IllegalStateException("Nie można zapisać pliku.")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
                    }
                    message = "Eksport JSON został zapisany."
                    error = null
                } catch (exception: Exception) {
                    error = exception.message ?: "Nie udało się zapisać eksportu."
                }
            }
        }
    }
    
    fun selectedReportPeriod(): FinancialReportPeriod? {
        return try {
            val start = LocalDate.parse(reportFromDate.trim())
            val end = LocalDate.parse(reportToDate.trim())
            val period = FinancialReportPeriod.fromInclusiveDates(start, end)
            error = null
            period
        } catch (exception: Exception) {
            error = exception.message
                ?: "Podaj poprawny zakres dat w formacie RRRR-MM-DD."
            null
        }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val period = pendingReportPeriod
                        ?: throw IllegalStateException("Najpierw wybierz poprawny zakres dat raportu.")
                    val pdf = repository.exportFinancialReportPdf(period)
                    withContext(Dispatchers.IO) {
                        FinancialReportSafWriter.write(context.contentResolver, uri, pdf)
                    }
                    message = "Raport PDF został zapisany."
                    error = null
                } catch (exception: Exception) {
                    error = exception.message ?: "Nie udało się zapisać raportu PDF."
                }
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val period = pendingReportPeriod
                        ?: throw IllegalStateException("Najpierw wybierz poprawny zakres dat raportu.")
                    val csv = repository.exportFinancialReportCsv(period)
                    withContext(Dispatchers.IO) {
                        FinancialReportSafWriter.write(
                            context.contentResolver,
                            uri,
                            csv.toByteArray(Charsets.UTF_8)
                        )
                    }
                    message = "Raport CSV został zapisany."
                    error = null
                } catch (exception: Exception) {
                    error = exception.message ?: "Nie udało się zapisać raportu CSV."
                }
            }
        }
    }

    LaunchedEffect(repository) { reload() }
    DisposableEffect(repository) {
        onDispose { repository.close() }
    }

    var expandedFinanceSections by remember { mutableStateOf(setOf("charts")) }

    @Composable
    fun FinanceSection(
        id: String,
        title: String,
        description: String? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val expanded = id in expandedFinanceSections
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Button(onClick = {
                        expandedFinanceSections = if (expanded) expandedFinanceSections - id else expandedFinanceSections + id
                    }) { Text(if (expanded) "Zwiń" else "Rozwiń") }
                }
                if (expanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Rachunki finansowe", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) { Text("Wróć") }
        }
        Text("Lokalny rejestr rachunków, transakcji, wymiany walut i kruszców oraz darowizn.", style = MaterialTheme.typography.bodyLarge)
        FinanceSection("charts", "Wykresy, wycena i przelicznik", "Notowania instrumentów, historia rachunku oraz przeliczanie walut.") {
            pl.gov.obywatel.android.financial.PriceHistoryChartCard(modifier = Modifier.fillMaxWidth())
            pl.gov.obywatel.android.financial.BankStatementNbpChartCard(entries = entries, modifier = Modifier.fillMaxWidth())
            pl.gov.obywatel.android.financial.NbpCurrencyConverterCard(modifier = Modifier.fillMaxWidth())
        }
        FinanceSection("donation-catalog", "Rejestry darowizn", "Wybór ewidencji darowizn i zbiórek.") {
            DonationLedgerCatalog.all.values.forEach { definition ->
                Button(onClick = { onOpenDonationLedger(definition.id) }, modifier = Modifier.fillMaxWidth()) { Text(definition.title) }
            }
        }
        FinanceSection("data-transfer", "Import, eksport i raporty", "Wczytywanie wyciągów, kopie danych oraz raporty CSV i PDF.") {
                OutlinedTextField(
                    value = accountScopeId,
                    onValueChange = { accountScopeId = it },
                    label = { Text("Pseudonim wybranego rachunku") },
                    supportingText = { Text("Alias lokalny, np. ipko-account-1. Nie wpisuj numeru rachunku.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    repository.saveAccountScope(accountScopeId, accountScopeId)
                                    accountScopes = repository.getAccountScopes()
                                    message = "Zapisano pseudonim rachunku."
                                    error = null
                                } catch (exception: Exception) {
                                    error = exception.message ?: "Nie udało się zapisać aliasu."
                                }
                            }
                        },
                        enabled = accountScopeId.isNotBlank()
                    ) { Text("Zapisz alias") }
                }
                if (accountScopes.isNotEmpty()) {
                    Text("Zapisane pseudonimy — wybierz rachunek do importu XML:")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accountScopes, key = { it.accountScopeId }) { scopeItem ->
                            Button(
                                onClick = {
                                    accountScopeId = scopeItem.accountScopeId
                                    message = "Wybrano: ${scopeItem.displayName}"
                                    error = null
                                }
                            ) {
                                Text(
                                    if (scopeItem.accountScopeId == accountScopeId) {
                                        "✓ ${scopeItem.displayName}"
                                    } else {
                                        scopeItem.displayName
                                    }
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }) {
                        Text("Importuj JSON")
                    }
                    Button(onClick = { xmlImportLauncher.launch(arrayOf("application/xml", "text/xml", "application/octet-stream")) }) {
                        Text("Importuj XML iPKO")
                    }
                }
                Button(onClick = { csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "text/*", "application/octet-stream")) }) {
                    Text("Importuj CSV banku")
                }
                Button(onClick = { exportLauncher.launch("rachunki-platnicze.json") }) {
                    Text("Eksportuj JSON")
                }
                Text("Zakres raportu (daty włącznie)", style = MaterialTheme.typography.titleSmall)
                pl.gov.obywatel.android.financial.FinancialReportDateRangePicker(
                    startDate = reportFromDate,
                    endDate = reportToDate,
                    onDatesChanged = { start, end ->
                        reportFromDate = start
                        reportToDate = end
                        error = null
                    }
                )
                Button(onClick = {
                    selectedReportPeriod()?.let { period ->
                        pendingReportPeriod = period
                        csvExportLauncher.launch("raport-rachunki-platnicze.csv")
                    }
                }) {
                    Text("Eksportuj raport CSV")
                }
                Button(onClick = {
                    selectedReportPeriod()?.let { period ->
                        pendingReportPeriod = period
                        pdfExportLauncher.launch("raport-rachunki-platnicze.pdf")
                    }
                }) {
                    Text("Eksportuj raport PDF")
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                importPreview?.let { preview ->
                    Text("Podgląd: nowe ${preview.newEntries.size}, duplikaty ${preview.duplicates.size}, konflikty ${preview.conflicts.size}")
                    preview.conflicts.forEach { conflict ->
                        Text(
                            "Konflikt: ${conflict.incoming.bankOperationReference} — ${conflict.existing.currencyCode} ${conflict.existing.signedAmount} / ${conflict.incoming.currencyCode} ${conflict.incoming.signedAmount}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            val json = pendingJson
                            if (json != null && preview.conflicts.isEmpty()) {
                                scope.launch {
                                    try {
                                        val summary = repository.importJson(json)
                                        message = "Dodano: ${summary.inserted}; duplikaty: ${summary.duplicates}."
                                        pendingJson = null
                                        importPreview = null
                                        reload()
                                    } catch (exception: Exception) {
                                        error = exception.message ?: "Import nie powiódł się."
                                    }
                                }
                            }
                        },
                        enabled = pendingJson != null && preview.conflicts.isEmpty() && !loading
                    ) { Text("Zatwierdź import") }
                    if (preview.conflicts.isNotEmpty()) {
                        Text("Import zablokowany do czasu wyjaśnienia konfliktów.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        FinanceSection("bank-history", "Historia rachunku i wymiany", "Zapisy bankowe, transakcje powiązane oraz operacje wymiany walut.") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historia zapisów bankowych", style = MaterialTheme.typography.titleMedium)
                    Text("Liczba zapisów: ${entries.size}")
                    Button(onClick = { reload() }, enabled = !loading) { Text("Odśwież rejestr") }
                    if (loading) CircularProgressIndicator()
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            if (!loading && entries.isEmpty() && error == null) {
                Text("Rejestr jest pusty. Możesz zaimportować archiwum JSON.")
            }

        }
        FinanceSection("asset-dictionary", "Słownik walut i kruszców", "Definicje aktywów, jednostki, precyzja oraz próby kruszców.") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Słownik walut i kruszców", style = MaterialTheme.typography.titleMedium)
                    Text("Definicje używane w transakcjach. Parametry ilościowe aktywa są chronione po użyciu w historii.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { assetKindInput = AssetKind.CURRENCY }) { Text(if (assetKindInput == AssetKind.CURRENCY) "✓ Waluta" else "Waluta") }
                        Button(onClick = { assetKindInput = AssetKind.PRECIOUS_METAL }) { Text(if (assetKindInput == AssetKind.PRECIOUS_METAL) "✓ Kruszec" else "Kruszec") }
                    }
                    OutlinedTextField(value = assetCodeInput, onValueChange = { assetCodeInput = it }, label = { Text("Kod aktywa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = assetNameInput, onValueChange = { assetNameInput = it }, label = { Text("Nazwa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = assetUnitInput, onValueChange = { assetUnitInput = it }, label = { Text("Jednostka (np. PLN, g)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = assetPrecisionInput, onValueChange = { assetPrecisionInput = it }, label = { Text("Miejsca dziesiętne (0–12)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (assetKindInput == AssetKind.PRECIOUS_METAL) {
                        OutlinedTextField(value = assetPurityInput, onValueChange = { assetPurityInput = it }, label = { Text("Próba (opcjonalnie, 1–1000)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    Button(onClick = { saveAssetForm() }, enabled = !loading && assetCodeInput.isNotBlank() && assetNameInput.isNotBlank()) { Text("Zapisz definicję aktywa") }
                    if (assets.isEmpty()) Text("Słownik jest pusty. Dodaj walutę lub kruszec przed rejestrowaniem transakcji.")
                    assets.forEach { asset ->
                        Text("${asset.code} — ${asset.name} • ${asset.unit} • ${asset.decimalPlaces} miejsc${asset.purity?.let { " • próba $it" }.orEmpty()}")
                    }
                }
            }

        }
        FinanceSection("transactions-form", "Dodawanie i edycja transakcji", "Wprowadzanie operacji wpłat, wypłat i wymiany aktywów.") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (editingTransactionId == null) "Ręczne dodawanie transakcji" else "Edycja transakcji", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FinancialTransactionType.values().toList(), key = { it.name }) { type ->
                            Button(onClick = { transactionType = type }) {
                                Text(if (transactionType == type) "✓ $type" else type.name)
                            }
                        }
                    }
                    OutlinedTextField(value = executedAtText, onValueChange = { executedAtText = it }, label = { Text("Data i czas ISO-8601") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (transactionType != FinancialTransactionType.DEPOSIT) {
                        OutlinedTextField(value = fromAssetText, onValueChange = { fromAssetText = it }, label = { Text("Aktywo oddane") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(assets, key = { it.code }) { asset ->
                                Button(onClick = { fromAssetText = asset.code }) { Text(if (fromAssetText == asset.code) "✓ ${asset.code}" else asset.code) }
                            }
                        }
                        OutlinedTextField(value = fromAmountText, onValueChange = { fromAmountText = it }, label = { Text("Ilość oddana") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    if (transactionType != FinancialTransactionType.WITHDRAWAL) {
                        OutlinedTextField(value = toAssetText, onValueChange = { toAssetText = it }, label = { Text("Aktywo otrzymane") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(assets, key = { it.code }) { asset ->
                                Button(onClick = { toAssetText = asset.code }) { Text(if (toAssetText == asset.code) "✓ ${asset.code}" else asset.code) }
                            }
                        }
                        OutlinedTextField(value = toAmountText, onValueChange = { toAmountText = it }, label = { Text("Ilość otrzymana") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = feeAssetText, onValueChange = { feeAssetText = it }, label = { Text("Aktywo prowizji (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(assets, key = { it.code }) { asset ->
                            Button(onClick = { feeAssetText = asset.code }) { Text(if (feeAssetText == asset.code) "✓ ${asset.code}" else asset.code) }
                        }
                    }
                    OutlinedTextField(value = feeAmountText, onValueChange = { feeAmountText = it }, label = { Text("Kwota prowizji (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Wybierz zapisy bankowe do powiązania")
                    val availableEntries = entries.filter { bankEntry ->
                        val ownerId = bankEntryOwners[bankEntry.accountingEntryId()]
                        ownerId == null || ownerId == editingTransactionId
                    }
                    if (availableEntries.isEmpty()) {
                        Text("Brak wolnych zapisów bankowych zgodnych z zasadą jednego powiązania na zapis.")
                    } else {
                        availableEntries.forEach { bankEntry ->
                            val entryId = bankEntry.accountingEntryId()
                            val selected = entryId in selectedBankEntryIds
                            Button(onClick = {
                                selectedBankEntryIds = if (selected) selectedBankEntryIds - entryId else selectedBankEntryIds + entryId
                            }) {
                                Text((if (selected) "✓ " else "") + "${bankEntry.bookingDate} • ${bankEntry.signedAmount.toPlainString()} ${bankEntry.currencyCode}")
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { saveTransactionForm() }, enabled = !loading) {
                            Text(if (editingTransactionId == null) "Zapisz transakcję" else "Zapisz zmiany")
                        }
                        if (editingTransactionId != null) Button(onClick = { resetTransactionForm() }) { Text("Anuluj") }
                    }
                }
            }

        }
        FinanceSection("ledger-details", "Szczegóły zapisów i transakcji", "Lista transakcji, operacji wymiany oraz pozostałych zapisów księgowych.") {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (financialTransactions.isNotEmpty()) {
                    item {
                        Text("Transakcje finansowe", style = MaterialTheme.typography.titleMedium)
                    }
                    items(financialTransactions, key = { "financial-${it.transaction.id}" }) { stored ->
                        val transaction = stored.transaction
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "${transaction.type} • ${transaction.executedAt}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                transaction.fromAssetCode?.let { code ->
                                    Text("Oddano: ${transaction.fromAmount?.toPlainString()} $code")
                                }
                                transaction.toAssetCode?.let { code ->
                                    Text("Otrzymano: ${transaction.toAmount?.toPlainString()} $code")
                                }
                                transaction.fee?.let { fee ->
                                    Text("Prowizja: ${fee.amount.toPlainString()} ${fee.assetCode}")
                                }
                                Text(
                                    "Zapisy bankowe powiązane: ${stored.accountingEntryIds.size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(onClick = {
                                    editingTransactionId = transaction.id
                                    transactionType = transaction.type
                                    executedAtText = transaction.executedAt.toString()
                                    fromAssetText = transaction.fromAssetCode.orEmpty()
                                    fromAmountText = transaction.fromAmount?.toPlainString().orEmpty()
                                    toAssetText = transaction.toAssetCode.orEmpty()
                                    toAmountText = transaction.toAmount?.toPlainString().orEmpty()
                                    feeAssetText = transaction.fee?.assetCode.orEmpty()
                                    feeAmountText = transaction.fee?.amount?.toPlainString().orEmpty()
                                    selectedBankEntryIds = stored.accountingEntryIds.toSet()
                                }) { Text("Edytuj") }
                            }
                        }
                    }
                }
                if (exchangeOperations.isNotEmpty()) {
                    item {
                        Text("Operacje wymiany walut", style = MaterialTheme.typography.titleMedium)
                    }
                    items(exchangeOperations, key = { it.operationId }) { operation ->
                        val expanded = operation.operationId in expandedExchangeIds
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    when (operation.status) {
                                        ExchangeOperationStatus.COMPLETE -> "Wymiana kompletna"
                                        ExchangeOperationStatus.SINGLE_LEG -> "Wymiana niepełna — jedna noga"
                                        ExchangeOperationStatus.REVIEW_REQUIRED -> "Wymiana wymaga weryfikacji"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (operation.status == ExchangeOperationStatus.REVIEW_REQUIRED) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    "Zapisy: ${operation.legs.size} • Waluty: ${operation.currencies.sorted().joinToString(", ")}"
                                )
                                Button(
                                    onClick = {
                                        expandedExchangeIds = if (expanded) {
                                            expandedExchangeIds - operation.operationId
                                        } else {
                                            expandedExchangeIds + operation.operationId
                                        }
                                    }
                                ) {
                                    Text(if (expanded) "Ukryj zapisy źródłowe" else "Pokaż zapisy źródłowe")
                                }
                                if (expanded) {
                                    operation.legs.forEach { leg ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                "${leg.bookingDate} • ${leg.currencyCode}",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                "${if (leg.signedAmount.signum() > 0) "+" else ""}${leg.signedAmount.toPlainString()} ${leg.currencyCode}",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(leg.operationType, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text("Pozostałe zapisy księgowe", style = MaterialTheme.typography.titleMedium)
                }
                items(entries.filterNot { it.accountingEntryId() in groupedEntryIds }, key = { it.accountingEntryId() }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${entry.bookingDate} • ${entry.currencyCode}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${if (entry.signedAmount.signum() > 0) "+" else ""}${entry.signedAmount.toPlainString()} ${entry.currencyCode}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(entry.operationType, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

        }
        FinanceSection("cash-donations", "Rejestr darowizn gotówkowych", "Przyjmowanie środków, rozliczanie przekazań, pokwitowania i załączniki.") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Darowizny gotówkowe", style = MaterialTheme.typography.titleMedium)
                    Text("Rejestr przyjętych środków z nazwą organizacji-beneficjenta, celem i rozliczeniem przekazania.")
                    OutlinedTextField(value = donationAmountText, onValueChange = { donationAmountText = it }, label = { Text("Kwota przyjęta") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationCurrencyText, onValueChange = { donationCurrencyText = it }, label = { Text("Waluta (kod ISO)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationOrganizationText, onValueChange = { donationOrganizationText = it }, label = { Text("Nazwa organizacji-beneficjenta *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationPurposeText, onValueChange = { donationPurposeText = it }, label = { Text("Cel darowizny *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationDonorText, onValueChange = { donationDonorText = it }, label = { Text("Darczyńca (opcjonalnie)") }, singleLine = true, enabled = !donationAnonymous, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { donationAnonymous = !donationAnonymous; if (donationAnonymous) donationDonorText = "" }) {
                            Text(if (donationAnonymous) "✓ Darczyńca anonimowy" else "Darczyńca anonimowy")
                        }
                    }
                    OutlinedTextField(value = donationCampaignText, onValueChange = { donationCampaignText = it }, label = { Text("Nazwa zbiórki / kampanii (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationCollectedByText, onValueChange = { donationCollectedByText = it }, label = { Text("Osoba przyjmująca (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationReceiptText, onValueChange = { donationReceiptText = it }, label = { Text("Numer pokwitowania (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(DonationCustodyStatus.values().toList(), key = { it.name }) { status ->
                            Button(onClick = { donationStatus = status }) {
                                Text(if (donationStatus == status) "✓ $status" else status.name)
                            }
                        }
                    }
                    OutlinedTextField(value = donationTransferredAmountText, onValueChange = { donationTransferredAmountText = it }, label = { Text("Kwota przekazana organizacji (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationTransferredAtText, onValueChange = { donationTransferredAtText = it }, label = { Text("Data przekazania ISO-8601 (wymagana z kwotą)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = donationNoteText, onValueChange = { donationNoteText = it }, label = { Text("Uwagi (opcjonalnie)") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveDonationForm() },
                            enabled = !loading && donationAmountText.isNotBlank() &&
                                donationCurrencyText.isNotBlank() && donationOrganizationText.isNotBlank() &&
                                donationPurposeText.isNotBlank()
                        ) { Text(if (editingDonationId == null) "Zapisz darowiznę" else "Zapisz zmiany") }
                        if (editingDonationId != null) Button(onClick = { resetDonationForm() }) { Text("Anuluj") }
                    }
                    donations.forEach { donation ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${donation.amount.toPlainString()} ${donation.currencyCode} • ${donation.beneficiaryOrganizationName}", style = MaterialTheme.typography.titleSmall)
                                Text("Cel: ${donation.purpose}")
                                Text("Przyjęto: ${donation.receivedAt} • ${donation.custodyStatus}")
                                donation.transferredAmount?.let { amount ->
                                    Text("Przekazano: ${amount.toPlainString()} ${donation.currencyCode}")
                                }
                                val attached = donationAttachments.filter { it.donationId == donation.id }
                                Text("Pokwitowania i załączniki: ${attached.size}")
                                attached.forEach { attachment ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("${attachment.fileName} • ${attachment.sizeBytes} B • ${attachment.mimeType}", style = MaterialTheme.typography.bodySmall)
                                        Text("SHA-256: ${attachment.sha256}", style = MaterialTheme.typography.labelSmall)
                                        Button(onClick = {
                                            scope.launch {
                                                try {
                                                    val uri = repository.getDonationAttachmentUri(attachment.id)
                                                    val viewIntent = Intent(Intent.ACTION_VIEW)
                                                        .setDataAndType(uri, attachment.mimeType)
                                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    context.startActivity(
                                                        Intent.createChooser(viewIntent, "Otwórz załącznik")
                                                    )
                                                    message = "Przekazano bezpieczny podgląd załącznika do wybranej aplikacji."
                                                    error = null
                                                } catch (exception: Exception) {
                                                    error = exception.message ?: "Nie udało się otworzyć załącznika."
                                                }
                                            }
                                        }) { Text("Otwórz") }
                                        Button(onClick = {
                                            scope.launch {
                                                try {
                                                    repository.deleteDonationAttachment(attachment.id)
                                                    donationAttachments = repository.getDonationAttachments()
                                                    message = "Usunięto załącznik."
                                                    error = null
                                                } catch (exception: Exception) {
                                                    error = exception.message ?: "Nie udało się usunąć załącznika."
                                                }
                                            }
                                        }) { Text("Usuń załącznik") }
                                    }
                                }
                                Button(onClick = {
                                    pendingDonationAttachmentId = donation.id
                                    donationAttachmentLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                                }) { Text("Dodaj pokwitowanie / załącznik") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { editDonation(donation) }) { Text("Edytuj") }
                                    Button(onClick = {
                                        scope.launch {
                                            try {
                                                repository.deleteCashDonationWithAttachments(donation.id)
                                                donations = repository.getCashDonations()
                                                donationAttachments = repository.getDonationAttachments()
                                                message = "Usunięto darowiznę i jej załączniki."
                                                error = null
                                            } catch (exception: Exception) {
                                                error = exception.message ?: "Nie udało się usunąć darowizny."
                                            }
                                        }
                                    }) { Text("Usuń") }
                                }
                            }
                        }
                    }
                }
            }

        }
    }


private const val MAX_IMPORT_BYTES = 10 * 1024 * 1024

/** Odczyt bajtów z limitem; zachowuje kodowanie dla parsera CSV (UTF-8/Windows-1250). */
private fun readBytesLimited(input: InputStream, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Plik przekracza limit 10 MB." }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

/** Odczyt z limitem bajtów — chroni UI i pamięć przed nieograniczonym plikiem wejściowym. */
private fun readUtf8Limited(input: InputStream, maxBytes: Int): String {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Plik przekracza limit 10 MB." }
        output.write(buffer, 0, count)
    }
    return output.toString(Charsets.UTF_8.name())
}
