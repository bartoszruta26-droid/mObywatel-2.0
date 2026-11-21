// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/**
 * Tworzy wielostronicowy raport A4 bez zewnętrznych bibliotek i bez uprawnień
 * do pamięci współdzielonej. Zwraca kompletne bajty PDF do zapisu przez SAF.
 */
object FinancialReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 40f
    private const val RIGHT = 555f
    private const val BODY_TOP = 78f
    private const val BODY_BOTTOM = 790f
    private const val LINE_HEIGHT = 13f

    private data class ReportLine(val text: String, val heading: Boolean = false)

    fun encode(report: FinancialReport): ByteArray {
        val document = PdfDocument()
        val output = ByteArrayOutputStream()
        try {
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 8.5f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 10f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val lines = buildLines(report)
            val wrapped = lines.flatMap { line ->
                wrap(line.text, if (line.heading) headingPaint else bodyPaint, RIGHT - LEFT)
                    .mapIndexed { index, text -> ReportLine(if (index == 0) text else "  $text", line.heading && index == 0) }
            }
            var cursor = 0
            var pageNumber = 1
            while (cursor < wrapped.size || pageNumber == 1) {
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                val canvas = page.canvas
                canvas.drawColor(android.graphics.Color.WHITE)
                val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textSize = 15f
                    typeface = Typeface.create("sans-serif", Typeface.BOLD)
                }
                canvas.drawText("Raport finansowy — rachunki płatnicze", LEFT, 35f, headerPaint)
                canvas.drawText("Strona $pageNumber", RIGHT - 55f, 815f, bodyPaint)
                var y = BODY_TOP
                while (cursor < wrapped.size && y <= BODY_BOTTOM) {
                    val line = wrapped[cursor]
                    val paint = if (line.heading) headingPaint else bodyPaint
                    if (line.heading && y > BODY_TOP) y += 4f
                    canvas.drawText(line.text, LEFT, y, paint)
                    y += LINE_HEIGHT
                    cursor++
                }
                document.finishPage(page)
                pageNumber++
            }
            document.writeTo(output)
            return output.toByteArray()
        } finally {
            document.close()
            output.close()
        }
    }

    private fun buildLines(report: FinancialReport): List<ReportLine> = buildList {
        add(ReportLine("Okres: ${report.period.fromInclusive} — ${report.period.toExclusive}"))
        add(ReportLine("Zestawienia aktywów", heading = true))
        if (report.assetTotals.isEmpty()) add(ReportLine("Brak transakcji w wybranym okresie."))
        report.assetTotals.forEach { total ->
            add(ReportLine("${total.assetCode} (${total.unit}): przychody ${total.incoming.toPlainString()}, rozchody ${total.outgoing.toPlainString()}, prowizje ${total.fees.toPlainString()}, zmiana netto ${total.balanceChange.toPlainString()}"))
        }
        add(ReportLine("Zestawienia darowizn", heading = true))
        if (report.donationTotals.isEmpty()) add(ReportLine("Brak darowizn w wybranym okresie."))
        report.donationTotals.forEach { total ->
            add(ReportLine("${total.currencyCode}: przyjęte ${total.accepted.toPlainString()}, przekazane ${total.transferred.toPlainString()}, przechowywane ${total.stored.toPlainString()}, gotówka ${total.held.toPlainString()}, wpłacone ${total.deposited.toPlainString()}, wymagające weryfikacji ${total.unsettledReview.toPlainString()}"))
        }
        add(ReportLine("Szczegółowe zdarzenia", heading = true))
        if (report.rows.isEmpty()) add(ReportLine("Brak zdarzeń w wybranym okresie."))
        report.rows.forEach { row ->
            add(ReportLine("${row.occurredAt} | ${row.event} | ${row.assetCode} ${row.amount.toPlainString()} ${row.unit} | ${row.status}"))
            if (row.description.isNotBlank()) {
                val description = row.description.replace(Regex("[\\r\\n\\t]+"), " ").take(4000)
                add(ReportLine("Opis: $description"))
            }
        }
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        var remaining = text
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var end = count
            if (count < remaining.length) {
                val space = remaining.lastIndexOf(' ', count - 1)
                if (space > 0) end = space
            }
            result += remaining.substring(0, end).trimEnd()
            remaining = remaining.substring(end).trimStart()
        }
        return result
    }
}
