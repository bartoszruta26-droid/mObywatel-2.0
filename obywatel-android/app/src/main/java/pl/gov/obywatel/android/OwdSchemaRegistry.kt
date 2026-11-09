// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import org.json.JSONArray
import org.json.JSONObject

/**
 * Versioned, application-owned OWD schemas.
 *
 * These identifiers are deliberately namespaced under pl.owd and are NOT
 * government/mObywatel schema identifiers. A future approved adapter must map
 * them explicitly to the operator's published schemas.
 */
internal object OwdSchemaRegistry {
    const val SCHEMA_VERSION = 1
    const val DISABILITY_STATUS = "pl.owd.person.disability-status"
    const val CIVIC_MANDATE_STATUS = "pl.owd.civic.mandate-status"
    const val PERSONAL_PROFILE = "pl.owd.person.profile"

    data class FieldDefinition(
        val name: String,
        val required: Boolean = false,
        val maxLength: Int = 512,
        val description: String
    )

    data class Definition(
        val id: String,
        val version: Int,
        val title: String,
        val fields: List<FieldDefinition>
    ) {
        val fieldNames: Set<String> get() = fields.map { it.name }.toSet()
        val requiredFields: Set<String> get() = fields.filter { it.required }.map { it.name }.toSet()
    }

    private val definitions = listOf(
        Definition(
            id = DISABILITY_STATUS,
            version = SCHEMA_VERSION,
            title = "Informacja o statusie niepełnosprawności",
            fields = listOf(
                FieldDefinition("status", true, 80, "Status podany w dokumencie lub deklaracji; nie jest samodzielnym potwierdzeniem."),
                FieldDefinition("degree", false, 80, "Stopień wskazany w źródle informacji."),
                FieldDefinition("documentType", false, 120, "Rodzaj dokumentu, jeśli istnieje."),
                FieldDefinition("documentNumber", false, 120, "Numer dokumentu; przekazywać wyłącznie, gdy jest to niezbędne."),
                FieldDefinition("validFrom", false, 32, "Data początku obowiązywania w formacie ISO-8601."),
                FieldDefinition("validUntil", false, 32, "Data końca obowiązywania w formacie ISO-8601.")
            )
        ),
        Definition(
            id = CIVIC_MANDATE_STATUS,
            version = SCHEMA_VERSION,
            title = "Informacja o statusie mandatu publicznego",
            fields = listOf(
                FieldDefinition("office", true, 120, "Rodzaj mandatu lub pełnionej funkcji."),
                FieldDefinition("jurisdiction", true, 160, "Organ lub jednostka, której dotyczy mandat."),
                FieldDefinition("status", true, 80, "Status opisany przez źródło informacji."),
                FieldDefinition("termStart", false, 32, "Początek kadencji w formacie ISO-8601."),
                FieldDefinition("termEnd", false, 32, "Koniec kadencji w formacie ISO-8601.")
            )
        ),
        Definition(
            id = PERSONAL_PROFILE,
            version = SCHEMA_VERSION,
            title = "Minimalny profil osoby",
            fields = listOf(
                FieldDefinition("givenName", false, 120, "Imię lub imiona."),
                FieldDefinition("familyName", false, 160, "Nazwisko."),
                FieldDefinition("displayName", false, 200, "Nazwa prezentowana.")
            )
        )
    ).associateBy { it.id }

    fun get(schemaId: String): Definition? = definitions[schemaId]

    fun all(): List<Definition> = definitions.values.sortedBy { it.id }

    /**
     * Validate schema, field allow-list and provenance metadata before a record
     * can be serialized for OWD transfer or offered to a future adapter.
     */
    fun validate(record: ShareableRecord): List<String> {
        val definition = get(record.schema)
            ?: return listOf("Nieobsługiwany schemat OWD: ${record.schema}")
        val errors = mutableListOf<String>()
        val unknown = record.fields.keys - definition.fieldNames
        if (unknown.isNotEmpty()) errors += "Nieznane pola schematu: ${unknown.sorted().joinToString()}"
        val missing = definition.requiredFields - record.fields.keys
        if (missing.isNotEmpty()) errors += "Brak wymaganych pól: ${missing.sorted().joinToString()}"
        definition.fields.forEach { field ->
            val value = record.fields[field.name] ?: return@forEach
            if (value.isBlank()) errors += "Pole ${field.name} nie może być puste."
            if (value.length > field.maxLength) errors += "Pole ${field.name} przekracza limit ${field.maxLength} znaków."
            if (value.any { it == '\u0000' }) errors += "Pole ${field.name} zawiera niedozwolony znak."
        }
        when (record.provenance) {
            DataProvenance.ISSUER_ATTESTED, DataProvenance.GOVERNMENT_SERVICE -> {
                if (record.issuerId.isNullOrBlank()) errors += "Brak identyfikatora wystawcy dla poświadczenia."
                if (record.evidenceReference.isNullOrBlank()) errors += "Brak referencji dowodu poświadczenia."
                if (record.issuedAtEpochSeconds == null || record.issuedAtEpochSeconds <= 0L) {
                    errors += "Brak prawidłowego czasu wystawienia poświadczenia."
                }
            }
            DataProvenance.LOCAL_USER, DataProvenance.PEER_SHARED -> Unit
        }
        if (record.issuedAtEpochSeconds != null && record.issuedAtEpochSeconds <= 0L) {
            errors += "Nieprawidłowy czas wystawienia."
        }
        if (record.validUntilEpochSeconds != null &&
            (record.issuedAtEpochSeconds == null || record.validUntilEpochSeconds <= record.issuedAtEpochSeconds)) {
            errors += "Termin ważności musi przypadać po czasie wystawienia."
        }
        return errors
    }

    fun requireValid(record: ShareableRecord) {
        val errors = validate(record)
        require(errors.isEmpty()) { errors.joinToString(" ") }
    }

    /** Canonical, versioned JSON envelope used inside encrypted OWD payloads. */
    fun serialize(record: ShareableRecord): String {
        requireValid(record)
        val fields = JSONObject()
        record.fields.toSortedMap().forEach { (key, value) -> fields.put(key, value) }
        val envelope = JSONObject()
            .put("format", "OWD_RECORD")
            .put("schema", record.schema)
            .put("schemaVersion", SCHEMA_VERSION)
            .put("provenance", record.provenance.name)
            .put("fields", fields)
        record.issuerId?.let { envelope.put("issuerId", it) }
        record.evidenceReference?.let { envelope.put("evidenceReference", it) }
        record.issuedAtEpochSeconds?.let { envelope.put("issuedAt", it) }
        record.validUntilEpochSeconds?.let { envelope.put("validUntil", it) }
        return envelope.toString()
    }

    fun deserialize(raw: String): ShareableRecord {
        require(raw.length <= 32 * 1024) { "Rekord OWD przekracza limit rozmiaru." }
        val json = JSONObject(raw)
        require(json.getString("format") == "OWD_RECORD") { "Nieprawidłowy format rekordu OWD." }
        require(json.getInt("schemaVersion") == SCHEMA_VERSION) { "Nieobsługiwana wersja schematu OWD." }
        val fieldsJson = json.getJSONObject("fields")
        val fields = mutableMapOf<String, String>()
        val keys = fieldsJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(fieldsJson.get(key) is String) { "Wartość pola $key musi być tekstem." }
            fields[key] = fieldsJson.getString(key)
        }
        val provenance = try {
            DataProvenance.valueOf(json.getString("provenance"))
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Nieznane pochodzenie danych OWD.")
        }
        val record = ShareableRecord(
            schema = json.getString("schema"),
            fields = fields,
            provenance = provenance,
            issuerId = json.optString("issuerId").takeIf { it.isNotBlank() && it != "null" },
            evidenceReference = json.optString("evidenceReference").takeIf { it.isNotBlank() && it != "null" },
            issuedAtEpochSeconds = if (json.has("issuedAt")) json.getLong("issuedAt") else null,
            validUntilEpochSeconds = if (json.has("validUntil")) json.getLong("validUntil") else null
        )
        requireValid(record)
        return record
    }

    /** Machine-readable schema catalogue for diagnostics and future adapter mapping. */
    fun catalogueJson(): String {
        val schemas = JSONArray()
        all().forEach { definition ->
            val fields = JSONArray()
            definition.fields.forEach { field ->
                fields.put(JSONObject()
                    .put("name", field.name)
                    .put("required", field.required)
                    .put("maxLength", field.maxLength)
                    .put("description", field.description))
            }
            schemas.put(JSONObject()
                .put("id", definition.id)
                .put("version", definition.version)
                .put("title", definition.title)
                .put("fields", fields))
        }
        return JSONObject()
            .put("format", "OWD_SCHEMA_CATALOGUE")
            .put("catalogueVersion", 1)
            .put("schemas", schemas)
            .toString()
    }
}
