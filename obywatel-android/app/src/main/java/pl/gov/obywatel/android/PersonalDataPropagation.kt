package pl.gov.obywatel.android

/** Synchronizes profile values into semantically equivalent document fields. */
internal object PersonalDataPropagation {
    private val aliases = mapOf(
        "birthDate" to listOf("disabilityBirthDate"),
        "birthPlace" to listOf("passportBirthPlace"),
        "citizenship" to listOf("passportCitizenship"),
        "sex" to listOf("passportSex"),
        "pesel" to listOf("disabilityPeselOrId")
    )

    fun apply(values: MutableMap<String, String>): Int {
        var changed = 0
        aliases.forEach { (source, targets) ->
            val value = values[source]?.trim().orEmpty()
            if (value.isNotEmpty()) targets.forEach { target ->
                if (values[target] != value) { values[target] = value; changed++ }
            }
        }
        val firstNames = values["firstNames"]?.trim().orEmpty()
        val lastName = values["lastName"]?.trim().orEmpty()
        val fullName = listOf(firstNames, lastName).filter { it.isNotEmpty() }.joinToString(" ")
        if (fullName.isNotEmpty()) listOf("passportHolderNames", "disabilityPersonName").forEach { target ->
            if (values[target] != fullName) { values[target] = fullName; changed++ }
        }
        return changed
    }
}
