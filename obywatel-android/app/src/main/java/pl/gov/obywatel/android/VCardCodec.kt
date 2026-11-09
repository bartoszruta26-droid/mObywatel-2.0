// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

/**
 * RFC 6350 vCard 4.0 codec used as the canonical OWD data payload.
 * OWD-specific metadata is carried only in registered-style X- properties;
 * the QR session offer and cryptographic envelope remain outside the vCard.
 */
internal object VCardCodec {
    private val fieldName = Regex("[A-Za-z0-9-]{1,64}")

    data class Card(
        val properties: Map<String, List<String>>,
        val extensions: Map<String, List<String>> = emptyMap()
    ) {
        fun first(name: String): String? = properties[name.uppercase()]?.firstOrNull()
    }

    fun encodeContact(fields: Map<String, String>): String {
        val normalized = fields.mapKeys { it.key.uppercase() }
        val displayName = normalized["FN"].orEmpty().trim()
        val structuredName = normalized["N"].orEmpty().trim()
        require(displayName.isNotBlank()) {
            "vCard 4.0 wymaga właściwości FN."
        }
        val lines = mutableListOf("BEGIN:VCARD", "VERSION:4.0")
        if (displayName.isNotBlank()) lines += "FN:${escape(displayName)}"
        lines += "N:${escapeStructured(structuredName.ifBlank { displayName })}"
        listOf("ORG", "TITLE", "TEL", "EMAIL", "URL", "ADR").forEach { key ->
            normalized[key].orEmpty().trim().takeIf { it.isNotBlank() }?.let {
                lines += "$key:${if (key == "ORG" || key == "ADR") escapeStructured(it) else escape(it)}"
            }
        }
        lines += "END:VCARD"
        return fold(lines).joinToString("\r\n") + "\r\n"
    }

    fun encodeOwdRecord(record: ShareableRecord): String {
        OwdSchemaRegistry.requireValid(record)
        val displayName = record.fields["displayName"]
            ?: record.fields["status"]
            ?: record.fields["office"]
            ?: record.schema
        val lines = mutableListOf(
            "BEGIN:VCARD",
            "VERSION:4.0",
            "FN:${escape(displayName)}",
            "N:${escape(displayName)}",
            "X-OWD-SCHEMA:${escape(record.schema)}",
            "X-OWD-SCHEMA-VERSION:${OwdSchemaRegistry.SCHEMA_VERSION}",
            "X-OWD-PROVENANCE:${record.provenance.name}"
        )
        record.issuerId?.let { lines += "X-OWD-ISSUER:${escape(it)}" }
        record.evidenceReference?.let { lines += "X-OWD-EVIDENCE:${escape(it)}" }
        record.issuedAtEpochSeconds?.let { lines += "X-OWD-ISSUED-AT:$it" }
        record.validUntilEpochSeconds?.let { lines += "X-OWD-VALID-UNTIL:$it" }
        record.fields.toSortedMap().forEach { (name, value) ->
            require(fieldName.matches(name)) { "Nieprawidłowa nazwa pola OWD." }
            lines += "X-OWD-FIELD;X-OWD-NAME=$name:${escape(value)}"
        }
        lines += "END:VCARD"
        return fold(lines).joinToString("\r\n") + "\r\n"
    }

    fun decode(raw: String): Card {
        require(raw.toByteArray(Charsets.UTF_8).size <= 10 * 1024 * 1024) { "Plik vCard przekracza limit 10 MB." }
        val lines = unfold(raw)
        require(lines.size >= 3 && lines.first().equals("BEGIN:VCARD", true) &&
            lines.last().equals("END:VCARD", true)) { "Nieprawidłowa struktura vCard." }
        val parsed = linkedMapOf<String, MutableList<String>>()
        val extensions = linkedMapOf<String, MutableList<String>>()
        var version: String? = null
        lines.drop(1).dropLast(1).forEach { line ->
            val colon = findValueSeparator(line)
            require(colon > 0) { "Nieprawidłowy wiersz vCard." }
            val left = line.substring(0, colon)
            val rawValue = line.substring(colon + 1)
            val tokens = left.split(';')
            val property = tokens.first().substringAfterLast('.').uppercase()
            val value = unescape(rawValue)
            if (property == "VERSION") version = value
            if (property == "X-OWD-FIELD") {
                val name = tokens.drop(1).firstOrNull { it.startsWith("X-OWD-NAME=", true) }
                    ?.substringAfter('=') ?: error("Pole OWD bez nazwy.")
                require(fieldName.matches(name)) { "Nieprawidłowa nazwa pola OWD." }
                extensions.getOrPut("FIELD:$name") { mutableListOf() }.add(value)
            } else if (property.startsWith("X-")) {
                extensions.getOrPut(property) { mutableListOf() }.add(value)
            } else {
                parsed.getOrPut(property) { mutableListOf() }.add(value)
            }
        }
        require(version == "4.0") { "OWD obsługuje vCard 4.0 (RFC 6350)." }
        require(parsed["FN"]?.isNotEmpty() == true) { "Brak wymaganej właściwości FN." }
        return Card(parsed.mapValues { it.value.toList() }, extensions.mapValues { it.value.toList() })
    }


    /**
     * Application archive: one standards-compliant vCard 4.0 object with all
     * locally stored key/value entries carried as X-OWD-FIELD properties.
     * This is an application extension profile, not a universal vCard backup.
     */
    fun encodeArchive(fields: Map<String, String>): String {
        require(fields.isNotEmpty()) { "Brak danych do eksportu." }
        val lines = mutableListOf(
            "BEGIN:VCARD",
            "VERSION:4.0",
            "FN:${escape(fields["firstNames"]?.takeIf { it.isNotBlank() }?.let { "$it ${fields["lastName"].orEmpty()}".trim() } ?: "Archiwum danych obywatelskich")}",
            "N:${escapeStructured(fields["lastName"].orEmpty() + ";" + fields["firstNames"].orEmpty())}",
            "X-OWD-ARCHIVE-VERSION:1"
        )
        fields.toSortedMap().forEach { (name, value) ->
            require(fieldName.matches(name)) { "Nieprawidłowa nazwa pola archiwum: $name" }
            lines += "X-OWD-FIELD;X-OWD-NAME=$name:${escape(value)}"
        }
        lines += "END:VCARD"
        val result = fold(lines).joinToString("\\r\\n") + "\\r\\n"
        require(result.toByteArray(Charsets.UTF_8).size <= 10 * 1024 * 1024) { "Archiwum przekracza limit 10 MB." }
        return result
    }

    fun decodeArchive(raw: String): Map<String, String> {
        val card = decode(raw)
        require(card.extensions["X-OWD-ARCHIVE-VERSION"]?.singleOrNull() == "1") {
            "Plik nie jest archiwum danych tej aplikacji."
        }
        val values = card.extensions.filterKeys { it.startsWith("FIELD:") }
            .mapKeys { it.key.removePrefix("FIELD:") }
            .mapValues { (_, entries) -> entries.singleOrNull() ?: error("Pole archiwum ma wiele wartości.") }
        require(values.isNotEmpty()) { "Archiwum nie zawiera danych." }
        return values
    }

    fun decodeOwdRecord(raw: String): ShareableRecord {
        val card = decode(raw)
        val schema = card.extensions["X-OWD-SCHEMA"]?.singleOrNull()
            ?: error("vCard nie zawiera schematu OWD.")
        require(card.extensions["X-OWD-SCHEMA-VERSION"]?.singleOrNull()?.toIntOrNull() == OwdSchemaRegistry.SCHEMA_VERSION) {
            "Nieobsługiwana wersja schematu OWD."
        }
        val provenance = DataProvenance.valueOf(
            card.extensions["X-OWD-PROVENANCE"]?.singleOrNull() ?: error("Brak pochodzenia danych OWD.")
        )
        val fields = card.extensions.filterKeys { it.startsWith("FIELD:") }
            .mapKeys { it.key.removePrefix("FIELD:") }
            .mapValues { (_, values) -> values.singleOrNull() ?: error("Pole OWD ma wielokrotne wartości.") }
        val record = ShareableRecord(
            schema = schema,
            fields = fields,
            provenance = provenance,
            issuerId = card.extensions["X-OWD-ISSUER"]?.singleOrNull(),
            evidenceReference = card.extensions["X-OWD-EVIDENCE"]?.singleOrNull(),
            issuedAtEpochSeconds = card.extensions["X-OWD-ISSUED-AT"]?.singleOrNull()?.toLongOrNull(),
            validUntilEpochSeconds = card.extensions["X-OWD-VALID-UNTIL"]?.singleOrNull()?.toLongOrNull()
        )
        OwdSchemaRegistry.requireValid(record)
        return record
    }

    private fun escapeStructured(value: String): String = value.split(";").joinToString(";") { escape(it) }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;")

    private fun unescape(value: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < value.length) {
            if (value[i] == '\\' && i + 1 < value.length) {
                i++
                out.append(when (value[i]) { 'n', 'N' -> '\n'; else -> value[i] })
            } else out.append(value[i])
            i++
        }
        return out.toString()
    }

    private fun unfold(raw: String): List<String> =
        raw.replace("\r\n", "\n").replace('\r', '\n').split('\n')
            .filter { it.isNotEmpty() }
            .fold(mutableListOf()) { acc, line ->
                if ((line.startsWith(" ") || line.startsWith("\t")) && acc.isNotEmpty()) {
                    acc[acc.lastIndex] = acc.last() + line.substring(1)
                } else acc += line
                acc
            }

    private fun fold(lines: List<String>): List<String> {
        val result = mutableListOf<String>()
        lines.forEach { line ->
            var current = StringBuilder()
            var octets = 0
            line.forEach { char ->
                val count = char.toString().toByteArray(Charsets.UTF_8).size
                if (octets + count > 75) {
                    result += current.toString()
                    current = StringBuilder(" ")
                    octets = 1
                }
                current.append(char)
                octets += count
            }
            result += current.toString()
        }
        return result
    }

    private fun findValueSeparator(line: String): Int {
        var quoted = false
        line.forEachIndexed { index, char ->
            if (char == '"') quoted = !quoted
            if (char == ':' && !quoted) return index
        }
        return -1
    }
}
