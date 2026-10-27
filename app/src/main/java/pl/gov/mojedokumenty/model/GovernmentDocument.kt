package pl.gov.mojedokumenty.model

import android.net.Uri

/** Typ dokumentu — odwzorowanie na wzory urzędowe RP. */
enum class DocumentType(val displayName: String) {
    DOWOD_OSOBISTY("Dowód osobisty"),
    PRAWO_JAZDY("Prawo jazdy"),
    REJSTRACJA_POJAZDU("Dowód rejestracyjny"),
    UBEZPIECZENIE("Ubezpieczenie pojazdu"),
    AKT_STANU_CYWILNEGO("Akt stanu cywilnego"),
    LEGITYMACJA("Legitymacja"),
    INNY("Inny dokument")
}

/** Status dokumentu w portfelu. */
enum class DocumentStatus(val displayName: String) {
    AKTYWNY("Aktywny"),
    WYGAŚNIĘTY("Wygasły"),
    ZASTĘPCZY("Zastępczy (e-dokument)"),
    DO_ODNOWIENIA("Do odnowienia")
}

/** Źródło pliku dokumentu. */
enum class DocumentSource {
    /** Plik z `assets/docs/` (np. „docs/dowod_wzor.pdf”). */
    ASSET,

    /** Plik zaimportowany przez użytkownika (SAF / udostępniony intentem). */
    USER_FILE,

    /** Brak pliku — karta demonstracyjna. */
    NONE
}

/**
 * Reprezentacja dokumentu w portfelu.
 * Dane wyłącznie lokalne — aplikacja celowo NIE łączy się z serwerami państwowymi.
 */
data class GovernmentDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val serialNumber: String,
    val issuedBy: String,
    val issueDate: String,      // TODO: zamienić na LocalDate (zadanie M3 w README)
    val validUntil: String,     // TODO: jak wyżej
    val status: DocumentStatus,
    /** Ścieżka do pliku: asset („docs/xxx.pdf”) albo content:// URI (USER_FILE). */
    val fileLocation: String? = null,
    val source: DocumentSource = DocumentSource.NONE,
    /** Kolor akcentu karty — styl „państwowy” (bordowy herbowy). */
    val accentColorHex: String = "#7A0026"
) {
    val hasFile: Boolean get() = !fileLocation.isNullOrBlank() && source != DocumentSource.NONE

    fun contentUriOrNull(): Uri? =
        if (source == DocumentSource.USER_FILE) fileLocation?.let { Uri.parse(it) } else null
}
