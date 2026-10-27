package pl.gov.mojedokumenty.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import pl.gov.mojedokumenty.model.DocumentSource
import pl.gov.mojedokumenty.model.DocumentStatus
import pl.gov.mojedokumenty.model.DocumentType
import pl.gov.mojedokumenty.model.GovernmentDocument

/**
 * Lokalna baza dokumentów.
 * - karta „Portfel” pokazuje dokumenty demo + pliki znalezione w `assets/docs/`
 *   + pliki zaimportowane przez użytkownika (SAF / udostępnione intentem),
 * - trwałość importów: SharedPreferences (Room docelowo — zadanie M3 w README).
 * Aplikacja celowo NIE łączy się z serwerami państwowymi.
 */
class DocumentRepository private constructor(private val appContext: Context) {

    private val prefs = appContext.getSharedPreferences("imported_docs", Context.MODE_PRIVATE)

    private val _documents = MutableStateFlow(emptyList<GovernmentDocument>())
    val documents: StateFlow<List<GovernmentDocument>> = _documents.asStateFlow()

    init {
        refresh(appContext)
    }

    /** Przebudowuje listę: demo + assets + importy użytkownika. */
    fun refresh(context: Context) {
        val base = sampleDocuments().toMutableList()
        base += discoverAssetDocuments(context)
        base += importedDocuments()
        _documents.value = base
    }

    fun getById(id: String): GovernmentDocument? =
        _documents.value.firstOrNull { it.id == id }

    /** Import pliku wskazanego przez SAF (persistable URI) lub intent ACTION_VIEW. */
    suspend fun importUserDocument(uri: Uri, persistable: Boolean): Result<GovernmentDocument> =
        withContext(Dispatchers.IO) {
            try {
                if (persistable) {
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    appContext.contentResolver.takePersistableUriPermission(uri, flags)
                }
                val docFile = DocumentFile.fromSingleUri(appContext, uri)
                val name = docFile?.name ?: "dokument_${System.currentTimeMillis()}.pdf"
                val mime = docFile?.type ?: appContext.contentResolver.getType(uri) ?: ""
                val isPdf = mime.contains("pdf") || name.endsWith(".pdf", true)
                val isImage = mime.startsWith("image/") ||
                    name.endsWith(".png", true) || name.endsWith(".jpg", true) || name.endsWith(".jpeg", true)
                if (!isPdf && !isImage) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Obsługiwane są tylko pliki PDF oraz obrazy PNG/JPG.")
                    )
                }

                val existing = _documents.value.firstOrNull { it.fileLocation == uri.toString() }
                if (existing != null) return@withContext Result.success(existing)

                val doc = GovernmentDocument(
                    id = "UF-${uri.hashCode()}",
                    type = guessTypeFromName(name),
                    title = name.substringBeforeLast('.'),
                    serialNumber = "IMPORT·LOKALNY",
                    issuedBy = "Wskazany przez użytkownika",
                    issueDate = "-",
                    validUntil = "-",
                    status = DocumentStatus.AKTYWNY,
                    fileLocation = uri.toString(),
                    source = DocumentSource.USER_FILE
                )
                prefs.edit()
                    .putStringSet(KEY_IMPORTED, HashSet(prefs.getStringSet(KEY_IMPORTED, emptySet())!! + uri.toString()))
                    .apply()
                _documents.value = _documents.value + doc
                Result.success(doc)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    fun removeUserDocument(id: String) {
        val doc = getById(id) ?: return
        if (doc.source != DocumentSource.USER_FILE) return
        prefs.edit()
            .putStringSet(KEY_IMPORTED, HashSet(prefs.getStringSet(KEY_IMPORTED, emptySet())!! - doc.fileLocation!!))
            .apply()
        _documents.value = _documents.value.filterNot { it.id == id }
    }

    private fun importedDocuments(): List<GovernmentDocument> =
        (prefs.getStringSet(KEY_IMPORTED, emptySet()) ?: emptySet()).map { uriStr ->
            val uri = Uri.parse(uriStr)
            val name = DocumentFile.fromSingleUri(appContext, uri)?.name ?: uri.lastPathSegment ?: "dokument"
            GovernmentDocument(
                id = "UF-${uri.hashCode()}",
                type = guessTypeFromName(name),
                title = name.substringBeforeLast('.'),
                serialNumber = "IMPORT·LOKALNY",
                issuedBy = "Wskazany przez użytkownika",
                issueDate = "-",
                validUntil = "-",
                status = DocumentStatus.AKTYWNY,
                fileLocation = uriStr,
                source = DocumentSource.USER_FILE
            )
        }

    /** Skanuje `assets/docs/` — każdy PDF/PNG/JPG automatycznie trafia do portfela. */
    private fun discoverAssetDocuments(context: Context): List<GovernmentDocument> =
        try {
            context.assets.list(ASSET_DIR)?.sorted()?.filter { f ->
                f.lowercase().endsWith(".pdf") || f.lowercase().endsWith(".png") ||
                    f.lowercase().endsWith(".jpg") || f.lowercase().endsWith(".jpeg")
            }?.map { fileName ->
                GovernmentDocument(
                    id = "AS-${fileName.hashCode()}",
                    type = guessTypeFromName(fileName),
                    title = fileName.substringBeforeLast('.') + " (plik w aplikacji)",
                    serialNumber = "PLIK·LOKALNY",
                    issuedBy = "Wgrany do assets/docs",
                    issueDate = "-",
                    validUntil = "-",
                    status = DocumentStatus.AKTYWNY,
                    fileLocation = "$ASSET_DIR/$fileName",
                    source = DocumentSource.ASSET
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

    private fun guessTypeFromName(name: String): DocumentType = when {
        name.contains("dowod", true) || name.contains("osobist", true) -> DocumentType.DOWOD_OSOBISTY
        name.contains("jazd", true) || name.contains("prawo", true) -> DocumentType.PRAWO_JAZDY
        name.contains("rejestr", true) || name.contains("pojazd", true) -> DocumentType.REJSTRACJA_POJAZDU
        name.contains("ubezpieczen", true) || name.contains("oc", true) -> DocumentType.UBEZPIECZENIE
        name.contains("akt", true) || name.contains("urodz", true) || name.contains("malzens", true) ->
            DocumentType.AKT_STANU_CYWILNEGO
        name.contains("legitymacj", true) -> DocumentType.LEGITYMACJA
        else -> DocumentType.INNY
    }

    companion object {
        const val ASSET_DIR = "docs"
        private const val KEY_IMPORTED = "imported_uris"

        @Volatile
        private var instance: DocumentRepository? = null

        fun get(context: Context): DocumentRepository =
            instance ?: synchronized(this) {
                instance ?: DocumentRepository(context.applicationContext).also { instance = it }
            }
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
            fileLocation = "docs/dowod_wzor.pdf",
            source = DocumentSource.ASSET
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
            fileLocation = "docs/prawojazdy_wzor.pdf",
            source = DocumentSource.ASSET
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
            fileLocation = null,
            source = DocumentSource.NONE
        )
    )
}
