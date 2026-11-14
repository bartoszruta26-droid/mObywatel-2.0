package pl.gov.obywatel.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class DocumentGroupConfiguration(
    val groups: List<String>,
    val assignments: Map<String, String>
)

private const val DOCUMENT_GROUP_PREFS = "document_group_catalog"
private const val DOCUMENT_GROUPS_KEY = "groups"
private const val DOCUMENT_GROUP_ASSIGNMENTS_KEY = "assignments"
private val legacyDocumentGroupNames = mapOf(
    "Służba wojskowa" to "Służba zbrojna",
    "Dokumenty osobiste i tożsamościowe" to "Służba osobista",
    "Dokumenty kościelne" to "Służba kościelna",
    "Dokumenty rodowe i rodzinne" to "Służba rodowa",
    "Dokumenty finansowe" to "Służba finansowa",
    "Dokumenty publiczne i obywatelskie" to "Służba państwowa",
    "Dokumenty firmowe i podmiotowe" to "Służba gospodarcza",
    "Dokumenty fundacji" to "Służba fundacyjna",
    "Dokumenty przedsiębiorstwa" to "Służba gospodarcza",
    "Dokumenty stowarzyszenia" to "Służba stowarzyszeniowa",
    "Dokumenty zawodowe i edukacyjne" to "Służba zawodowa",
    "Dokumenty cywilne i urzędowe" to "Służba cywilna",
    "Dokumenty zdrowotne i rodzinne" to "Służba zdrowotna",
    "Pliki i pozostałe dokumenty" to "Zasoby i dokumenty własne"
)

internal fun migrateDocumentGroupName(name: String): String =
    legacyDocumentGroupNames[name] ?: name

internal fun migrateDocumentGroupAssignment(documentId: String, group: String): String = when {
    documentId == "organization_foundation" && group == "Służba gospodarcza" -> "Służba fundacyjna"
    documentId == "organization_association" && group == "Służba gospodarcza" -> "Służba stowarzyszeniowa"
    else -> group
}


internal fun loadDocumentGroupConfiguration(context: Context): DocumentGroupConfiguration {
    val prefs = context.getSharedPreferences(DOCUMENT_GROUP_PREFS, Context.MODE_PRIVATE)
    val savedGroups = runCatching {
        val array = JSONArray(prefs.getString(DOCUMENT_GROUPS_KEY, "[]"))
        buildList { for (index in 0 until array.length()) add(array.optString(index).trim()) }
    }.getOrDefault(emptyList())
    val groups = (documentVisibilityGroupOrder + savedGroups.map(::migrateDocumentGroupName))
        .filter { it.isNotBlank() }
        .distinct()
    val savedAssignments = runCatching {
        val json = JSONObject(prefs.getString(DOCUMENT_GROUP_ASSIGNMENTS_KEY, "{}"))
        buildMap {
            json.keys().forEach { id -> put(id, migrateDocumentGroupName(json.optString(id))) }
        }
    }.getOrDefault(emptyMap())
    val assignments = documentDisplayOptions.distinctBy { it.first }.associate { (id, _, _) ->
        val defaultGroup = documentVisibilityGroup(id)
        val migratedAssignment = savedAssignments[id]?.let { migrateDocumentGroupAssignment(id, it) }
        val selected = migratedAssignment?.takeIf { it in groups } ?: defaultGroup
        id to selected
    }
    return DocumentGroupConfiguration(groups, assignments)
}

internal fun saveDocumentGroupConfiguration(
    context: Context,
    configuration: DocumentGroupConfiguration
) {
    val documentIds = documentDisplayOptions.map { it.first }.toSet()
    val groups = configuration.groups.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    require(groups.isNotEmpty()) { "Katalog musi zawierać co najmniej jedną grupę." }
    val assignments = documentIds.associateWith { id ->
        configuration.assignments[id]?.takeIf { it in groups } ?: documentVisibilityGroup(id)
            .takeIf { it in groups } ?: groups.first()
    }
    val prefs = context.getSharedPreferences(DOCUMENT_GROUP_PREFS, Context.MODE_PRIVATE)
    val committed = prefs.edit()
        .putString(DOCUMENT_GROUPS_KEY, JSONArray(groups).toString())
        .putString(DOCUMENT_GROUP_ASSIGNMENTS_KEY, JSONObject(assignments).toString())
        .commit()
    check(committed) { "Nie udało się zapisać grup dokumentów." }
}

internal fun addDocumentGroup(
    configuration: DocumentGroupConfiguration,
    name: String
): DocumentGroupConfiguration {
    val normalized = name.trim()
    require(normalized.isNotBlank()) { "Podaj nazwę nowej grupy." }
    require(configuration.groups.none { it.equals(normalized, ignoreCase = true) }) {
        "Grupa o tej nazwie już istnieje."
    }
    return configuration.copy(groups = configuration.groups + normalized)
}

internal fun assignDocumentToGroup(
    configuration: DocumentGroupConfiguration,
    documentId: String,
    group: String
): DocumentGroupConfiguration {
    require(documentDisplayOptions.any { it.first == documentId }) { "Nieznany dokument: $documentId" }
    require(group in configuration.groups) { "Nieznana grupa: $group" }
    return configuration.copy(assignments = configuration.assignments + (documentId to group))
}
