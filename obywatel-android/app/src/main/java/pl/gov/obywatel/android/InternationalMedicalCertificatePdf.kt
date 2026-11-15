// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

internal data class MedicalCertificatePdfField(
    val sourceLabel: String,
    val sourceValue: String,
    val targetLabel: String,
    val targetValue: String
)

/** Creates a printable bilingual PDF; source and target text are never silently substituted. */
internal fun createBilingualMedicalCertificatePdf(
    sourceLanguage: String,
    targetLanguage: String,
    fields: List<MedicalCertificatePdfField>,
    doctorName: String = "",
    doctorPractice: String = "",
    doctorAddress: String = ""
): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 42f
    val contentWidth = pageWidth - margin * 2
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textSize = 17f
    }
    val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textSize = 11f
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f }
    val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8f }
    var pageNumber = 0
    var page: PdfDocument.Page? = null
    var canvas: android.graphics.Canvas? = null
    var y = 0f

    fun startPage() {
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page!!.canvas
        y = margin
        canvas!!.drawText("INTERNATIONAL MEDICAL CERTIFICATE", margin, y, titlePaint)
        y += 22f
        canvas!!.drawText("Original: $sourceLanguage    |    Translation: $targetLanguage", margin, y, mutedPaint)
        y += 24f
    }

    fun finishPage() {
        page?.let(document::finishPage)
        page = null
        canvas = null
    }

    fun drawWrapped(text: String, paint: Paint, indent: Float = 0f) {
        val value = text.ifBlank { "—" }
        var remaining = value
        val maxWidth = contentWidth - indent
        while (remaining.isNotEmpty()) {
            if (y > pageHeight - margin - 24f) {
                finishPage()
                startPage()
            }
            val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var lineEnd = count
            if (count < remaining.length) {
                val lastSpace = remaining.lastIndexOf(' ', count - 1)
                if (lastSpace > 0) lineEnd = lastSpace
            }
            val line = remaining.substring(0, lineEnd).trimEnd()
            canvas!!.drawText(line, margin + indent, y, paint)
            y += paint.textSize + 4f
            remaining = remaining.substring(lineEnd).trimStart()
        }
    }

    try {
        startPage()
        fields.forEach { field ->
            if (y > pageHeight - margin - 90f) {
                finishPage()
                startPage()
            }
            drawWrapped(field.sourceLabel, headingPaint)
            drawWrapped(field.sourceValue, bodyPaint, 8f)
            y += 3f
            drawWrapped(field.targetLabel, headingPaint)
            drawWrapped(field.targetValue, bodyPaint, 8f)
            y += 10f
        }
        if (fields.isEmpty()) drawWrapped("No certificate fields have been completed.", bodyPaint)
        if (y > pageHeight - margin - 170f) {
            finishPage()
            startPage()
        }
        y += 14f
        drawWrapped("ATTENDING PHYSICIAN", headingPaint)
        if (doctorName.isNotBlank()) drawWrapped(doctorName, bodyPaint)
        if (doctorPractice.isNotBlank()) drawWrapped(doctorPractice, bodyPaint)
        if (doctorAddress.isNotBlank()) drawWrapped(doctorAddress, bodyPaint)
        y += 34f
        canvas!!.drawLine(margin, y, margin + 220f, y, bodyPaint)
        canvas!!.drawLine(margin + 250f, y, margin + contentWidth, y, bodyPaint)
        y += 13f
        canvas!!.drawText("Physician's handwritten signature", margin, y, mutedPaint)
        canvas!!.drawText("Stamp", margin + 250f, y, mutedPaint)
        y += 26f
        canvas!!.drawLine(margin, y, margin + 220f, y, bodyPaint)
        canvas!!.drawText("Date and place", margin, y + 13f, mutedPaint)
        finishPage()
        document.writeTo(output)
        return output.toByteArray()
    } finally {
        page?.let { runCatching { document.finishPage(it) } }
        document.close()
        output.close()
    }
}
