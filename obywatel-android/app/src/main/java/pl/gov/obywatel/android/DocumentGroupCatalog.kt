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

internal fun loadDocumentGroupConfiguration(context: Context): DocumentGroupConfiguration {
    val prefs = context.getSharedPreferences(DOCUMENT_GROUP_PREFS, Context.MODE_PRIVATE)
    val savedGroups = runCatching {
        val array = JSONArray(prefs.getString(DOCUMENT_GROUPS_KEY, "[]"))
        buildList { for (index in 0 until array.length()) add(array.optString(index).trim()) }
    }.getOrDefault(emptyList())
    val groups = (documentVisibilityGroupOrder + savedGroups)
        .filter { it.isNotBlank() }
        .distinct()
    val savedAssignments = runCatching {
        val json = JSONObject(prefs.getString(DOCUMENT_GROUP_ASSIGNMENTS_KEY, "{}"))
        buildMap {
            json.keys().forEach { id -> put(id, json.optString(id)) }
        }
    }.getOrDefault(emptyMap())
    val assignments = documentDisplayOptions.distinctBy { it.first }.associate { (id, _, _) ->
        val defaultGroup = documentVisibilityGroup(id)
        val selected = savedAssignments[id]?.takeIf { it in groups } ?: defaultGroup
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
