// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context

private const val DOCUMENT_DISPLAY_PREFS = "document_display_preferences"
private const val DOCUMENT_DISPLAY_KEY_PREFIX = "placement_"

private const val DOCUMENT_CARD_STYLE_KEY = "document_card_style"
internal fun loadDocumentCardStyle(context: Context): String =
    context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .getString(DOCUMENT_CARD_STYLE_KEY, "large")?.takeIf { it in setOf("large", "small", "list") } ?: "large"
internal fun saveDocumentCardStyle(context: Context, style: String) {
    require(style in setOf("large", "small", "list")) { "Nieprawidłowy układ kafelków dokumentów." }
    check(context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(DOCUMENT_CARD_STYLE_KEY, style).commit()) {
        "Nie udało się zapisać układu kafelków dokumentów."
    }
}

private const val DOCUMENT_DEFAULT_ICON_KEY = "default_document_icon"
private const val DOCUMENT_ICON_OVERRIDE_PREFIX = "document_icon_"
internal val documentIconChoices = listOf(
    "lib_ic_document" to "Dokument",
    "lib_ic_account" to "Osoba",
    "lib_ic_credit_card" to "Karta",
    "lib_ic_folder" to "Folder",
    "lib_ic_medical_record" to "Dokument medyczny",
    "lib_ic_school" to "Edukacja",
    "lib_ic_building" to "Budynek",
    "lib_ic_wallet" to "Portfel",
    "lib_ic_fingerprint" to "Odcisk palca",
    "lib_ic_flag" to "Flaga",
    "documents_identity_card" to "Dowód tożsamości",
    "documents_passport" to "Paszport",
    "documents_family_record" to "Dokument rodowy",
    "documents_student_legitimacy" to "Legitymacja uczniowska",
    "tile_ic_student_outline" to "Legitymacja ucznia/studenta",
    "tile_ic_disability_outline" to "Orzeczenia i zaświadczenia",
    "tile_ic_church_outline" to "Dokumenty kościelne",
    "tile_ic_civil_outline" to "Dokumenty cywilne",
    "tile_ic_business_outline" to "Dokumenty firmowe i podmiotowe",
    "tile_ic_armed_service_outline" to "Służba zbrojna"
)
internal fun loadDefaultDocumentIcon(context: Context): String =
    context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .getString(DOCUMENT_DEFAULT_ICON_KEY, "automatic")
        ?.takeIf { it == "automatic" || documentIconChoices.any { choice -> choice.first == it } }
        ?: "automatic"
internal fun saveDefaultDocumentIcon(context: Context, value: String) {
    require(value == "automatic" || documentIconChoices.any { it.first == value })
    check(context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(DOCUMENT_DEFAULT_ICON_KEY, value).commit()) {
        "Nie udało się zapisać domyślnej ikony dokumentów."
    }
}
internal fun loadDocumentIconOverrides(context: Context): Map<String, String> {
    val prefs = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
    return documentDisplayOptions.mapNotNull { (id, _, _) ->
        prefs.getString(DOCUMENT_ICON_OVERRIDE_PREFIX + id, null)
            ?.takeIf { selected -> documentIconChoices.any { it.first == selected } }
            ?.let { id to it }
    }.toMap()
}
internal fun saveDocumentIconOverrides(context: Context, values: Map<String, String>) {
    val editor = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE).edit()
    documentDisplayOptions.forEach { (id, _, _) ->
        val value = values[id]
        if (value.isNullOrBlank()) editor.remove(DOCUMENT_ICON_OVERRIDE_PREFIX + id)
        else {
            require(documentIconChoices.any { it.first == value }) { "Nieprawidłowa ikona dokumentu: $id" }
            editor.putString(DOCUMENT_ICON_OVERRIDE_PREFIX + id, value)
        }
    }
    check(editor.commit()) { "Nie udało się zapisać indywidualnych ikon dokumentów." }
}
internal fun resolveDocumentIconResource(
    context: Context,
    documentId: String,
    defaultIcon: String,
    overrides: Map<String, String>
): Int {
    val selected = overrides[documentId] ?: defaultIcon
    if (selected != "automatic") {
        context.resources.getIdentifier(selected, "drawable", context.packageName)
            .takeIf { it != 0 }?.let { return it }
    }
    return documentDisplayOptions.firstOrNull { it.first == documentId }?.third
        ?: R.drawable.lib_ic_document
}
internal fun loadDocumentPlacements(context: Context): Map<String, String> {
    val prefs = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE)
    if (!prefs.getBoolean(DOCUMENT_VISIBILITY_MIGRATION_KEY, false)) {
        // Migracja uzupełnia wyłącznie brakujące wpisy. Nie nadpisuje wyborów
        // użytkownika zapisanych we wcześniejszej wersji aplikacji.
        val editor = prefs.edit()
        documentDisplayOptions.forEach { (id, _, _) ->
            val key = DOCUMENT_DISPLAY_KEY_PREFIX + id
            if (!prefs.contains(key)) {
                val legacy = if (id == "loyalty") prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + "loyalty_cards", null) else null
                editor.putString(key, legacy ?: defaultDocumentPlacements[id] ?: "hidden")
            }
        }
        editor.putBoolean(DOCUMENT_VISIBILITY_MIGRATION_KEY, true)
        check(editor.commit()) { "Nie udało się zastosować domyślnej widoczności dokumentów." }
    }
    if (!prefs.getBoolean(DONATION_LEDGER_VISIBILITY_MIGRATION_KEY, false)) {
        // Ustaw domyślną widoczność czterech ewidencji na ukrytą.
        // Pozostaw jawne, wcześniejsze wybory użytkownika bez zmian.
        val editor = prefs.edit()
        listOf(DonationLedgerCatalog.PERSON, DonationLedgerCatalog.FOUNDATION,
            DonationLedgerCatalog.ASSOCIATION, DonationLedgerCatalog.CHURCH).forEach { id ->
            val key = DOCUMENT_DISPLAY_KEY_PREFIX + id
            if (!prefs.contains(key)) editor.putString(key, "hidden")
        }
        editor.putBoolean(DONATION_LEDGER_VISIBILITY_MIGRATION_KEY, true)
        check(editor.commit()) { "Nie udało się zastosować domyślnej widoczności ewidencji darowizn." }
    }
    if (!prefs.getBoolean(ARMED_SERVICE_VISIBILITY_MIGRATION_KEY, false)) {
        // Dodanie grupy do katalogu nie oznacza zgody na publikację jej kafelków.
        check(prefs.edit().putBoolean(ARMED_SERVICE_VISIBILITY_MIGRATION_KEY, true).commit()) {
            "Nie udało się zapisać migracji widoczności służby zbrojnej."
        }
    }
    if (!prefs.getBoolean(ARMED_SERVICE_DASHBOARD_CLEANUP_MIGRATION_KEY, false)) {
        // Cofnij automatyczne przypisanie do pulpitu z poprzedniej wersji.
        val editor = prefs.edit()
        armedServiceDocumentIds.forEach { id ->
            val key = DOCUMENT_DISPLAY_KEY_PREFIX + id
            if (prefs.getString(key, null) == "dashboard") editor.putString(key, "hidden")
        }
        editor.putBoolean(ARMED_SERVICE_DASHBOARD_CLEANUP_MIGRATION_KEY, true)
        check(editor.commit()) { "Nie udało się uporządkować widoczności służby zbrojnej." }
    }
    return documentDisplayOptions.associate { (id, _, _) ->
        val savedPlacement = prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + id, null)
            ?: if (id == "loyalty") prefs.getString(DOCUMENT_DISPLAY_KEY_PREFIX + "loyalty_cards", null) else null
        id to (savedPlacement ?: defaultDocumentPlacements[id] ?: "hidden")
    }
}
internal fun saveDocumentPlacements(context: Context, placements: Map<String, String>) {
    val editor = context.getSharedPreferences(DOCUMENT_DISPLAY_PREFS, Context.MODE_PRIVATE).edit()
    documentDisplayOptions.forEach { (id, _, _) ->
        val placement = placements[id] ?: "hidden"
        require(placement in setOf("dashboard", "other", "hidden")) { "Nieprawidłowe miejsce wyświetlania dokumentu: $id" }
        editor.putString(DOCUMENT_DISPLAY_KEY_PREFIX + id, placement)
    }
    check(editor.commit()) { "Nie udało się zapisać ustawień widoczności dokumentów." }
}

