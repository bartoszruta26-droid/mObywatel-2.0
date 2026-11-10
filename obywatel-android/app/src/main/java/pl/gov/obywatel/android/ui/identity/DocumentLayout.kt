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
    val locked: Boolean = false
)

internal data class DocumentLayout(
    val pageCount: Int = 1,
    val elements: List<DocumentLayoutElement> = defaults()
) {
    fun element(id: String) = elements.firstOrNull { it.id == id }

    fun update(element: DocumentLayoutElement) = copy(
        elements = elements.map { if (it.id == element.id) element else it }
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
                    .put("visible", e.visible).put("locked", e.locked))
            }
        })

    companion object {
        fun defaults() = listOf(
            DocumentLayoutElement("header", "Nagłówek", 42f, 42f, 511f, 72f, 15f),
            DocumentLayoutElement("title", "Tytuł", 42f, 184f, 511f, 34f, 21f),
            DocumentLayoutElement("recipient", "Adresat", 42f, 292f, 511f, 64f, 10f),
            DocumentLayoutElement("body", "Treść", 42f, 350f, 511f, 285f, 10f),
            DocumentLayoutElement("signature", "Podpis", 42f, 675f, 511f, 100f, 9f),
            DocumentLayoutElement("watermark", "Znak wodny", 105f, 390f, 385f, 80f, 48f),
            DocumentLayoutElement("footer", "Stopka", 42f, 790f, 511f, 30f, 7f)
        )

        fun fromJson(json: JSONObject?): DocumentLayout {
            if (json == null) return DocumentLayout()
            val defaults = defaults()
            val source = json.optJSONArray("elements") ?: JSONArray()
            val parsed = defaults.map { base ->
                val saved = (0 until source.length()).map { source.optJSONObject(it) }
                    .firstOrNull { it?.optString("id") == base.id }
                if (saved == null) base else base.copy(
                    x = saved.optDouble("x", base.x.toDouble()).toFloat().coerceIn(0f, 595f),
                    y = saved.optDouble("y", base.y.toDouble()).toFloat().coerceIn(0f, 842f),
                    width = saved.optDouble("width", base.width.toDouble()).toFloat().coerceIn(1f, 595f),
                    height = saved.optDouble("height", base.height.toDouble()).toFloat().coerceIn(1f, 842f),
                    fontSize = saved.optDouble("fontSize", base.fontSize.toDouble()).toFloat().coerceIn(5f, 72f),
                    visible = saved.optBoolean("visible", base.visible),
                    locked = saved.optBoolean("locked", base.locked)
                )
            }
            return DocumentLayout(json.optInt("pageCount", 1).coerceIn(1, 100), parsed)
        }
    }
}
