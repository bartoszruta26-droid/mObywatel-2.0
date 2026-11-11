// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.identity

import org.json.JSONArray
import org.json.JSONObject

/**
 * Layout coordinates are expressed in PDF points on an A4 page (595 x 842).
 * Element IDs are stable so saved templates remain editable after UI changes.
 */
internal data class DocumentLayoutElement(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val fontSize: Float = 10f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val page: Int = 1,
    val kind: String = "text"
)

internal data class DocumentLayout(
    val pageCount: Int = 1,
    val elements: List<DocumentLayoutElement> = defaults()
) {
    fun element(id: String, page: Int = 1) = elements.firstOrNull { it.id == id && it.page == page } ?: elements.firstOrNull { it.id == id }

    fun update(element: DocumentLayoutElement) = copy(
        elements = elements.map { if (it.id == element.id && it.page == element.page) element else it }
    )

    fun toJson(): JSONObject = JSONObject()
        .put("pageCount", pageCount.coerceIn(1, 100))
        .put("elements", JSONArray().apply {
            elements.forEach { e ->
                put(JSONObject()
                    .put("id", e.id).put("label", e.label)
                    .put("x", e.x).put("y", e.y)
                    .put("width", e.width).put("height", e.height)
                    .put("fontSize", e.fontSize)
                    .put("visible", e.visible).put("locked", e.locked).put("page", e.page).put("kind", e.kind))
            }
        })

    companion object {
        fun forGroup(group: String): DocumentLayout = DocumentLayout()

        fun defaults() = listOf(
            DocumentLayoutElement("header", "Nagłówek", 42f, 42f, 511f, 72f, 15f),
            DocumentLayoutElement("flag", "Flaga", 42f, 42f, 68f, 34f),
            DocumentLayoutElement("emblem", "Herb / godło", 120f, 35f, 54f, 54f),
            DocumentLayoutElement("logo", "Logo", 475f, 38f, 76f, 68f),
            DocumentLayoutElement("seal", "Pieczęć", 390f, 669f, 82f, 82f),
            DocumentLayoutElement("security", "Hologram / zabezpieczenie", 490f, 671f, 54f, 54f),
            DocumentLayoutElement("signatureGraphic", "Grafika podpisu", 54f, 714f, 130f, 28f),
            DocumentLayoutElement("title", "Tytuł", 42f, 184f, 511f, 34f, 21f),
            DocumentLayoutElement("recipient", "Adresat", 42f, 292f, 511f, 64f, 10f),
            DocumentLayoutElement("reference", "Podstawa / informacje uzupełniające", 42f, 535f, 511f, 70f, 9f),
            DocumentLayoutElement("body", "Treść", 42f, 350f, 511f, 285f, 10f),
            DocumentLayoutElement("signature", "Podpis", 42f, 675f, 511f, 100f, 9f),
            DocumentLayoutElement("watermark", "Znak wodny", 105f, 390f, 385f, 80f, 48f),
            DocumentLayoutElement("footer", "Stopka", 42f, 790f, 511f, 30f, 7f)
        )

        fun fromJson(json: JSONObject?): DocumentLayout {
            if (json == null) return DocumentLayout()
            val base = defaults()
            val source = json.optJSONArray("elements") ?: JSONArray()
            val parsed = base.map { element ->
                val saved = (0 until source.length()).mapNotNull { source.optJSONObject(it) }
                    .firstOrNull { it.optString("id") == element.id && it.optInt("page", 1) == 1 }
                if (saved == null) element else element.copy(
                    x = saved.optDouble("x", element.x.toDouble()).toFloat().coerceIn(0f, 595f),
                    y = saved.optDouble("y", element.y.toDouble()).toFloat().coerceIn(0f, 842f),
                    width = saved.optDouble("width", element.width.toDouble()).toFloat().coerceIn(1f, 595f),
                    height = saved.optDouble("height", element.height.toDouble()).toFloat().coerceIn(1f, 842f),
                    fontSize = saved.optDouble("fontSize", element.fontSize.toDouble()).toFloat().coerceIn(5f, 72f),
                    visible = saved.optBoolean("visible", element.visible),
                    locked = saved.optBoolean("locked", element.locked),
                    page = 1
                )
            }.toMutableList()
            for (index in 0 until source.length()) {
                val saved = source.optJSONObject(index) ?: continue
                val id = saved.optString("id")
                val page = saved.optInt("page", 1).coerceIn(1, 100)
                if (parsed.any { it.id == id && it.page == page }) continue
                val template = base.firstOrNull { it.id == id } ?: DocumentLayoutElement(
                    id = id,
                    label = saved.optString("label", "Element dekoracyjny"),
                    x = saved.optDouble("x", 42.0).toFloat(),
                    y = saved.optDouble("y", 42.0).toFloat(),
                    width = saved.optDouble("width", 180.0).toFloat(),
                    height = saved.optDouble("height", 2.0).toFloat(),
                    fontSize = saved.optDouble("fontSize", 18.0).toFloat(),
                    kind = saved.optString("kind", "ornament")
                )
                parsed.add(template.copy(
                    x = saved.optDouble("x", template.x.toDouble()).toFloat().coerceIn(0f, 595f),
                    y = saved.optDouble("y", template.y.toDouble()).toFloat().coerceIn(0f, 842f),
                    width = saved.optDouble("width", template.width.toDouble()).toFloat().coerceIn(1f, 595f),
                    height = saved.optDouble("height", template.height.toDouble()).toFloat().coerceIn(1f, 842f),
                    fontSize = saved.optDouble("fontSize", template.fontSize.toDouble()).toFloat().coerceIn(5f, 72f),
                    visible = saved.optBoolean("visible", template.visible),
                    locked = saved.optBoolean("locked", template.locked),
                    page = page,
                    kind = saved.optString("kind", template.kind)
                ))
            }
            return DocumentLayout(json.optInt("pageCount", 1).coerceIn(1, 100), parsed)
        }
    }
}
