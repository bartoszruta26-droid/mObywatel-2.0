package pl.gov.mojedokumenty.model

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

/**
 * Reprezentacja dokumentu w portfelu.
 * Na razie dane lokalne (asset / plik podany przez użytkownika).
 */
data class GovernmentDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val serialNumber: String,
    val issuedBy: String,
    val issueDate: String,      // TODO: zamienić na LocalDate (zadanie w README)
    val validUntil: String,     // TODO: jak wyżej
    val status: DocumentStatus,
    /** Ścieżka do pliku dokumentu: asset ("docs/xxx.pdf") lub content:// URI. */
    val fileUri: Uri? = null,
    /** Kolor akcentu karty — styl „państwowy” (np. bordowy herbowy). */
    val accentColorHex: String = "#7A0026"
)
