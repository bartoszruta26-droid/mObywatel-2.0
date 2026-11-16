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

/** Creates a printable bilingual PDF with aligned source/translation columns when requested. */
internal fun createBilingualMedicalCertificatePdf(
    sourceLanguage: String,
    targetLanguage: String,
    fields: List<MedicalCertificatePdfField>,
    doctorName: String = "",
    doctorPractice: String = "",
    doctorAddress: String = "",
    doctorRegister: String = "",
    doctorContact: String = "",
    doctorQualifications: String = "",
    doctorSpecialty: String = "",
    sideBySideFields: Boolean = false,
    documentTitle: String = "INTERNATIONAL MEDICAL CERTIFICATE",
    issuerHeading: String = "ATTENDING PHYSICIAN",
    signatureCaption: String = "Physician's handwritten signature (sign after printing)",
    stampCaption: String = "Physician identification / stamp details",
    patientSignatureCaption: String = "Patient's handwritten signature (if required)",
    dateAndPlaceCaption: String = "Date and place",
    pageCountOverride: Int? = null
): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 36f
    val contentWidth = pageWidth - margin * 2
    val polish = targetLanguage.equals("pl", ignoreCase = true)
    val notProvided = if (polish) "nie podano" else "not provided"
    val originalCaption = if (polish) "ORYGINAŁ" else "ORIGINAL"
    val translationCaption = if (polish) "TŁUMACZENIE" else "TRANSLATION"
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textSize = 16f
    }
    val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textSize = 9.2f
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8.5f }
    val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 7.4f }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.7f
    }
    var pageNumber = 0
    var page: PdfDocument.Page? = null
    var canvas: android.graphics.Canvas? = null
    var y = 0f

    fun finishPage() {
        if (pageCountOverride != null && canvas != null) {
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8f
                textAlign = Paint.Align.RIGHT
            }
            canvas!!.drawText(
                if (polish) "Strona $pageNumber z $pageCountOverride" else "Page $pageNumber of $pageCountOverride",
                pageWidth - margin,
                pageHeight - 16f,
                footerPaint
            )
        }
        page?.let(document::finishPage)
        page = null
        canvas = null
    }

    fun startPage() {
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page!!.canvas
        y = margin
        canvas!!.drawText(documentTitle, margin, y, titlePaint)
        y += 19f
        canvas!!.drawText("$originalCaption / ${sourceLanguage.uppercase()}     |     $translationCaption / ${targetLanguage.uppercase()}", margin, y, mutedPaint)
        y += 17f
        // Practice identity and contact information belong in the document header, not in the stamp panel.
        val headerLines = buildList {
            if (doctorPractice.isNotBlank()) add((if (polish) "Placówka: " else "Practice: ") + doctorPractice)
            if (doctorAddress.isNotBlank()) add((if (polish) "Adres: " else "Address: ") + doctorAddress)
            if (doctorContact.isNotBlank()) add((if (polish) "Kontakt: " else "Contact: ") + doctorContact)
        }
        val patientHeaderLines = fields.filter { field ->
            val label = field.sourceLabel.lowercase(java.util.Locale.ROOT)
            label.contains("patient") && (label.contains("name") || label.contains("address"))
        }.map { field ->
            val source = field.sourceValue.ifBlank { "—" }
            val translated = field.targetValue.ifBlank { "—" }
            "${field.sourceLabel}: $source  |  ${field.targetLabel}: $translated"
        }
        (headerLines + patientHeaderLines).forEach { line ->
            var remaining = line
            while (remaining.isNotEmpty()) {
                val count = mutedPaint.breakText(remaining, true, contentWidth, null).coerceAtLeast(1)
                var end = count
                if (count < remaining.length) {
                    val space = remaining.lastIndexOf(' ', count - 1)
                    if (space > 0) end = space
                }
                canvas!!.drawText(remaining.substring(0, end).trimEnd(), margin, y, mutedPaint)
                remaining = remaining.substring(end).trimStart()
                y += 10f
            }
        }
        if (headerLines.isNotEmpty() || patientHeaderLines.isNotEmpty()) {
            y += 4f
            canvas!!.drawLine(margin, y, margin + contentWidth, y, borderPaint)
            y += 12f
        }
    }

    fun wrapLines(text: String, paint: Paint, width: Float): List<String> {
        val value = text.ifBlank { "—" }
        val lines = mutableListOf<String>()
        var remaining = value
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, width, null).coerceAtLeast(1)
            var end = count
            if (count < remaining.length) {
                val space = remaining.lastIndexOf(' ', count - 1)
                if (space > 0) end = space
            }
            lines += remaining.substring(0, end).trimEnd()
            remaining = remaining.substring(end).trimStart()
        }
        return lines
    }

    fun drawWrapped(text: String, paint: Paint, indent: Float = 0f) {
        wrapLines(text, paint, contentWidth - indent).forEach { line ->
            if (y > pageHeight - margin - 22f) {
                finishPage()
                startPage()
            }
            canvas!!.drawText(line, margin + indent, y, paint)
            y += paint.textSize + 3f
        }
    }

    fun drawSideBySideField(field: MedicalCertificatePdfField) {
        val gap = 14f
        val columnWidth = (contentWidth - gap) / 2f
        val left = wrapLines(field.sourceLabel, headingPaint, columnWidth) +
            wrapLines(field.sourceValue, bodyPaint, columnWidth)
        val right = wrapLines(field.targetLabel, headingPaint, columnWidth) +
            wrapLines(field.targetValue, bodyPaint, columnWidth)
        val rowHeight = maxOf(left.size, right.size) * (bodyPaint.textSize + 3f) + 10f
        if (y + rowHeight > pageHeight - margin - 24f) {
            finishPage()
            startPage()
        }
        canvas!!.drawLine(margin + columnWidth + gap / 2f, y - 7f, margin + columnWidth + gap / 2f, y + rowHeight - 4f, borderPaint)
        var leftY = y
        left.forEachIndexed { index, line ->
            val paint = if (index < wrapLines(field.sourceLabel, headingPaint, columnWidth).size) headingPaint else bodyPaint
            canvas!!.drawText(line, margin, leftY, paint)
            leftY += bodyPaint.textSize + 3f
        }
        var rightY = y
        val rightLabelLines = wrapLines(field.targetLabel, headingPaint, columnWidth).size
        right.forEachIndexed { index, line ->
            val paint = if (index < rightLabelLines) headingPaint else bodyPaint
            canvas!!.drawText(line, margin + columnWidth + gap, rightY, paint)
            rightY += bodyPaint.textSize + 3f
        }
        y += rowHeight
        canvas!!.drawLine(margin, y - 3f, margin + contentWidth, y - 3f, borderPaint)
        y += 5f
    }

    var calculatedPageCount = pageCountOverride
    try {
        startPage()
        if (sideBySideFields) {
            fields.forEach(::drawSideBySideField)
        } else {
            fields.forEach { field ->
                val sourceLabelLines = wrapLines(field.sourceLabel, headingPaint, contentWidth)
                drawWrapped(field.sourceLabel, headingPaint)
                drawWrapped(field.sourceValue, bodyPaint, 8f)
                drawWrapped(field.targetLabel, headingPaint)
                drawWrapped(field.targetValue, bodyPaint, 8f)
                y += 7f
            }
        }
        if (fields.isEmpty()) drawWrapped(if (polish) "Nie uzupełniono danych dokumentu." else "No document fields have been completed.", bodyPaint)

        val stampLines = if (polish) listOf(
            "Imię i nazwisko: " + doctorName.ifBlank { notProvided },
            "Tytuł zawodowy: " + doctorQualifications.ifBlank { notProvided },
            "Specjalizacja: " + doctorSpecialty.ifBlank { notProvided },
            "Numer prawa wykonywania zawodu / numer w rejestrze: " + doctorRegister.ifBlank { notProvided }
        ) else listOf(
            "Name: " + doctorName.ifBlank { notProvided },
            "Professional title: " + doctorQualifications.ifBlank { notProvided },
            "Specialty: " + doctorSpecialty.ifBlank { notProvided },
            "Professional licence / register number: " + doctorRegister.ifBlank { notProvided }
        )
        val panelHeight = 78f
        if (y + panelHeight + 85f > pageHeight - margin) {
            finishPage()
            startPage()
        }
        y += 12f
        drawWrapped(if (polish && issuerHeading == "PRESCRIBER / WYSTAWCA RECEPTY") "WYSTAWCA RECEPTY" else issuerHeading, headingPaint)
        y += 4f
        val panelTop = y
        val panelLeft = margin + 220f
        val panelWidth = contentWidth - 220f
        canvas!!.drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderPaint)
        var panelY = panelTop + 12f
        canvas!!.drawText(if (polish && stampCaption == "Prescriber identification / stamp details") "DANE IDENTYFIKACYJNE WYSTAWCY / DANE DO PIECZĘCI" else stampCaption, panelLeft + 6f, panelY, headingPaint)
        panelY += 13f
        stampLines.forEach { line ->
            wrapLines(line, mutedPaint, panelWidth - 12f).forEach { wrapped ->
                if (panelY < panelTop + panelHeight - 3f) {
                    canvas!!.drawText(wrapped, panelLeft + 6f, panelY, mutedPaint)
                    panelY += 9f
                }
            }
        }
        val signatureY = panelTop + panelHeight - 8f
        canvas!!.drawLine(margin, signatureY, margin + 195f, signatureY, bodyPaint)
        y = panelTop + panelHeight + 14f
        canvas!!.drawText(if (polish && signatureCaption.startsWith("Prescriber's handwritten signature")) "Własnoręczny podpis wystawcy (złożyć po wydrukowaniu)" else signatureCaption, margin, y, mutedPaint)
        y += 23f
        canvas!!.drawLine(margin, y, margin + 195f, y, bodyPaint)
        canvas!!.drawLine(panelLeft, y, panelLeft + panelWidth, y, bodyPaint)
        y += 12f
        canvas!!.drawText(if (polish && patientSignatureCaption.startsWith("Patient's handwritten signature")) "Własnoręczny podpis pacjenta (jeżeli wymagany)" else patientSignatureCaption, margin, y, mutedPaint)
        canvas!!.drawText(if (polish && dateAndPlaceCaption == "Date and place") "Data i miejsce" else dateAndPlaceCaption, panelLeft, y, mutedPaint)
        finishPage()
        // Render twice: first determine the page count, then render with footers.
        if (pageCountOverride == null) calculatedPageCount = pageNumber
        else document.writeTo(output)
    } finally {
        page?.let { runCatching { document.finishPage(it) } }
        document.close()
        output.close()
    }
    if (pageCountOverride == null) {
        return createBilingualMedicalCertificatePdf(
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            fields = fields,
            doctorName = doctorName,
            doctorPractice = doctorPractice,
            doctorAddress = doctorAddress,
            doctorRegister = doctorRegister,
            doctorContact = doctorContact,
            doctorQualifications = doctorQualifications,
            doctorSpecialty = doctorSpecialty,
            sideBySideFields = sideBySideFields,
            documentTitle = documentTitle,
            issuerHeading = issuerHeading,
            signatureCaption = signatureCaption,
            stampCaption = stampCaption,
            patientSignatureCaption = patientSignatureCaption,
            dateAndPlaceCaption = dateAndPlaceCaption,
            pageCountOverride = calculatedPageCount
        )
    }
    return output.toByteArray()
}
