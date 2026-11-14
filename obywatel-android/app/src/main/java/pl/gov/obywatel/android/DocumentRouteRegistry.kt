// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

/**
 * Single source of truth for document-tile navigation.
 *
 * DOCUMENT keeps the selected document identifier and opens its own preview.
 * GROUP deliberately opens a collection.
 * SPECIALIZED opens an existing feature screen that is not a document preview.
 */
enum class DocumentRouteType { GROUP, DOCUMENT, SPECIALIZED }

data class DocumentRoute(
    val documentId: String,
    val type: DocumentRouteType,
    val destination: String? = null
)

object DocumentRouteRegistry {
    private fun document(id: String, destinationId: String = id) =
        id to DocumentRoute(destinationId, DocumentRouteType.DOCUMENT)

    private fun group(id: String, service: String) =
        id to DocumentRoute(id, DocumentRouteType.GROUP, service)

    private fun specialized(id: String, destination: String) =
        id to DocumentRoute(id, DocumentRouteType.SPECIALIZED, destination)

    /** Every identifier exposed by the document visibility settings is registered. */
    val routes: Map<String, DocumentRoute> = (listOf(
        document("profile"),
        document("identity"),
        document("passport"),
        specialized("registry", "registry_extract"),
        document("family"),
        document("parliament"),
        document("training"),
        document("student"),
        document("disability"),
        group("church", "Dokumenty Kościelne"),
        group("civil", "Formularze dokumentów"),
        group("business", "Dokumenty rejestru podmiotów"),
        specialized("bank_accounts", "bank_accounts"),
        specialized("rod_bank_accounts", "rod_bank_accounts"),
        specialized("entity_bank_accounts", "entity_bank_accounts"),
        specialized("vaccines", "vaccines"),
        specialized("signed_pdf", "signed_pdf"),
        specialized("imported", "imported"),
        specialized("children", "children"),
        specialized("branding", "branding"),
        document("loyalty"),
        document("birth_extract"),
        document("marriage_extract"),
        document("death_extract"),
        document("large_family"),
        document("blood_donor"),
        document("disability_card"),
        document("vehicle_registration"),
        document("insurance_vehicle"),
        document("insurance_professional"),
        document("insurance_private"),
        document("insurance_business"),
        document("criminal_record"),
        document("mission_travel"),
        document("ehic"),
        document("eu_education"),
        document("military_book"),
        document("martial_arts"),
        document("shooting_sport"),
        document("archery"),
        document("tactical_training"),
        document("survival_training"),
        document("security_guard"),
        document("defense_training"),
        document("historical_reenactment"),
        document("reenactment_qualification"),
        document("reenactment_event"),
        document("church_bishop"),
        document("church_archbishop"),
        document("church_cardinal"),
        document("church_abbot"),
        document("church_minister"),
        document("church_missionary"),
        group("professional", "Dokumenty kwalifikacji zawodowych"),
        document("professional_medical"),
        document("professional_education"),
        document("professional_therapeutic"),
        document("professional_other"),
        group("organization_foundation", "Dokumenty rejestru podmiotów"),
        group("organization_church", "Dokumenty Kościelne"),
        group("organization_company", "Dokumenty rejestru podmiotów"),
        group("organization_association", "Dokumenty rejestru podmiotów"),
        group("entity", "Dokumenty rejestru podmiotów"),
        group("official", "Formularze dokumentów"),
        // Compatibility aliases used by older callers.
        document("identity_card", "identity"),
        document("loyalty_cards", "loyalty")
    ) +
        // Keep the route registry synchronized with every schema-backed catalog entry.
        // Explicit GROUP/SPECIALIZED routes above take precedence over generated document routes.
        documentBrandTargets.map { (id, _) -> document(id) } +
        churchGraphicDocumentTargets().map { (id, _) -> document(id) } +
        familyEntityGraphicDocumentTargets().map { (id, _) -> document(id) } +
        officialGraphicDocumentTargets().map { (id, _) -> document(id) }
    ).distinctBy { it.first }.associate { it }

    fun find(documentId: String): DocumentRoute? = routes[documentId]
}
