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
    doctorAddress: String = "",
    doctorRegister: String = "",
    doctorContact: String = "",
    documentTitle: String = "INTERNATIONAL MEDICAL CERTIFICATE",
    issuerHeading: String = "ATTENDING PHYSICIAN",
    signatureCaption: String = "Physician's handwritten signature (sign after printing)",
    stampCaption: String = "Physician identification / stamp details",
    patientSignatureCaption: String = "Patient's handwritten signature (if required)",
    dateAndPlaceCaption: String = "Date and place"
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
        canvas!!.drawText(documentTitle, margin, y, titlePaint)
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
        drawWrapped(issuerHeading, headingPaint)
        if (doctorName.isNotBlank()) drawWrapped(doctorName, bodyPaint)
        if (doctorPractice.isNotBlank()) drawWrapped(doctorPractice, bodyPaint)
        if (doctorAddress.isNotBlank()) drawWrapped(doctorAddress, bodyPaint)
        if (doctorRegister.isNotBlank()) drawWrapped(doctorRegister, bodyPaint)
        if (doctorContact.isNotBlank()) drawWrapped(doctorContact, bodyPaint)
        y += 18f

        // Replace the empty stamp placeholder with a prefilled issuer-identification panel.
        // Missing registration data is stated explicitly; the PDF never invents a number.
        val stampLeft = margin + 250f
        val stampWidth = contentWidth - 250f
        val stampTop = y - 8f
        val stampHeight = 88f
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 7.2f }
        val stampHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textSize = 8f
        }
        canvas!!.drawRect(stampLeft, stampTop, stampLeft + stampWidth, stampTop + stampHeight, bodyPaint)
        var stampY = stampTop + 12f
        canvas!!.drawText(stampCaption, stampLeft + 6f, stampY, stampHeadingPaint)
        stampY += 12f
        val stampLines = listOf(
            doctorName.ifBlank { "Name: not provided" },
            doctorRegister.ifBlank { "Professional register / licence number: not provided in source record" },
            doctorPractice,
            doctorAddress,
            doctorContact
        ).filter { it.isNotBlank() }
        stampLines.forEach { value ->
            var remaining = value
            while (remaining.isNotEmpty() && stampY < stampTop + stampHeight - 4f) {
                val count = stampPaint.breakText(remaining, true, stampWidth - 12f, null).coerceAtLeast(1)
                var lineEnd = count
                if (count < remaining.length) {
                    val lastSpace = remaining.lastIndexOf(' ', count - 1)
                    if (lastSpace > 0) lineEnd = lastSpace
                }
                canvas!!.drawText(remaining.substring(0, lineEnd).trimEnd(), stampLeft + 6f, stampY, stampPaint)
                remaining = remaining.substring(lineEnd).trimStart()
                stampY += stampPaint.textSize + 2f
            }
        }

        val signatureY = stampTop + stampHeight - 10f
        canvas!!.drawLine(margin, signatureY, margin + 220f, signatureY, bodyPaint)
        y = stampTop + stampHeight + 13f
        canvas!!.drawText(signatureCaption, margin, y, mutedPaint)
        y += 26f
        canvas!!.drawLine(margin, y, margin + 220f, y, bodyPaint)
        canvas!!.drawLine(stampLeft, y, stampLeft + stampWidth, y, bodyPaint)
        y += 13f
        canvas!!.drawText(patientSignatureCaption, margin, y, mutedPaint)
        canvas!!.drawText(dateAndPlaceCaption, stampLeft, y, mutedPaint)
        finishPage()
        document.writeTo(output)
        return output.toByteArray()
    } finally {
        page?.let { runCatching { document.finishPage(it) } }
        document.close()
        output.close()
    }
}
