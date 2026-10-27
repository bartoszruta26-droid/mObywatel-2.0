package pl.gov.mojedokumenty.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.gov.mojedokumenty.model.DocumentStatus
import pl.gov.mojedokumenty.model.DocumentType
import pl.gov.mojedokumenty.model.GovernmentDocument

/**
 * Lokalna baza dokumentów (szkielet).
 * Docelowo: Room + import plików PDF z Storage Access Framework (zadania w README).
 * Aplikacja celowo NIE łączy się z serwerami państwowymi.
 */
object DocumentRepository {

    private val _documents = MutableStateFlow(sampleDocuments())
    val documents: StateFlow<List<GovernmentDocument>> = _documents.asStateFlow()

    fun getById(id: String): GovernmentDocument? =
        _documents.value.firstOrNull { it.id == id }

    fun addDocument(document: GovernmentDocument) {
        _documents.value = _documents.value + document
    }

    /** Przykładowe dokumenty demonstracyjne — podmienione przez prawdziwe pliki użytkownika. */
    private fun sampleDocuments(): List<GovernmentDocument> = listOf(
        GovernmentDocument(
            id = "DO-1",
            type = DocumentType.DOWOD_OSOBISTY,
            title = "Dowód osobisty — wzór podglądowy",
            serialNumber = "ABC 123456",
            issuedBy = "Prezydent Miasta (organ wydający)",
            issueDate = "2022-03-15",
            validUntil = "2032-03-15",
            status = DocumentStatus.AKTYWNY,
            fileUri = null // docs/dowod_wzor.pdf — patrz README (assets)
        ),
        GovernmentDocument(
            id = "PJ-1",
            type = DocumentType.PRAWO_JAZDY,
            title = "Prawo jazdy — kategoria B",
            serialNumber = "WXJ 448821",
            issuedBy = "Starosta Powiatowy",
            issueDate = "2021-06-01",
            validUntil = "2041-06-01",
            status = DocumentStatus.AKTYWNY,
            fileUri = null // docs/prawojazdy_wzor.pdf — patrz README (assets)
        ),
        GovernmentDocument(
            id = "RP-1",
            type = DocumentType.REJSTRACJA_POJAZDU,
            title = "Dowód rejestracyjny pojazdu",
            serialNumber = "WAR 4821K",
            issuedBy = "Wydział Komunikacji",
            issueDate = "2020-11-20",
            validUntil = "2025-11-20",
            status = DocumentStatus.DO_ODNOWIENIA,
            fileUri = null
        )
    )
}
