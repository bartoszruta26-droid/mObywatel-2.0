// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Importer neutralnego CSV. Nie deklaruje zgodności z konkretnym bankiem.
 * Surowy opis nie jest zapisywany; bez referencji zachowujemy wyłącznie jego SHA-256.
 */
object BankStatementCsvParser {
    /** Dekoduje UTF-8 (z BOM lub bez); przy niepoprawnym UTF-8 stosuje Windows-1250. */
    fun parse(bytes: ByteArray, accountScopeId: String): List<BankStatementEntry> {
        require(bytes.isNotEmpty()) { "Plik CSV jest pusty." }
        require(bytes.size <= MAX_CSV_BYTES) { "Plik CSV przekracza limit 10 MB." }
        val payload = if (
            bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) bytes.copyOfRange(3, bytes.size) else bytes
        val utf8 = runCatching {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(payload)).toString()
        }.getOrNull()
        val decoded = utf8 ?: Charset.forName("windows-1250").decode(ByteBuffer.wrap(payload)).toString()
        return parse(decoded, accountScopeId)
    }

    fun parse(csv: String, accountScopeId: String): List<BankStatementEntry> {
        val scope = accountScopeId.trim()
        require(scope.matches(Regex("[A-Za-z0-9._-]{1,64}"))) {
            "Pseudonim rachunku musi mieć 1–64 znaki ASCII, cyfry, kropkę, myślnik lub podkreślenie."
        }
        require(csv.isNotBlank()) { "Plik CSV jest pusty." }
        require(csv.length <= MAX_CSV_CHARACTERS) { "Plik CSV przekracza limit 10 MB." }

        val normalized = csv.removePrefix("\uFEFF")
        val delimiter = detectDelimiter(normalized)
        val records = parseRecords(normalized, delimiter)
            .filterNot { row -> row.all { it.isBlank() } }
        require(records.size >= 2) { "CSV musi zawierać nagłówek i co najmniej jeden zapis." }

        val headers = records.first().map(::normalizeHeader)
        require(headers.isNotEmpty() && headers.all { it.isNotEmpty() }) {
            "Nagłówek CSV zawiera pustą nazwę kolumny."
        }
        require(headers.distinct().size == headers.size) { "Nagłówek CSV zawiera powtórzone kolumny." }

        val indexes = headers.mapIndexed { index, header -> header to index }.toMap()
        val dateIndex = findColumn(indexes, "data", "dataksiegowania", "dataoperacji", "bookingdate", "operationdate")
        val currencyIndex = findColumn(indexes, "waluta", "currency", "currencycode")
        val amountIndex = findColumn(indexes, "kwota", "amount", "signedamount")
        val typeIndex = findColumn(indexes, "typ", "typoperacji", "operacja", "operationtype", "type")
        val referenceIndex = findOptionalColumn(indexes, "referencja", "referencjabankowa", "reference", "bankoperationreference", "externalid")
        val descriptionIndex = findOptionalColumn(indexes, "opis", "tytul", "description", "title")

        require(dateIndex != null && currencyIndex != null && amountIndex != null && typeIndex != null) {
            "Brak wymaganych kolumn CSV: data, waluta, kwota i typ operacji."
        }

        val dataRows = records.drop(1)
        require(dataRows.size <= MAX_OPERATIONS) {
            "CSV zawiera zbyt wiele zapisów (limit: $MAX_OPERATIONS)."
        }
        val entries = dataRows.mapIndexed { rowNumber, row ->
            require(row.size == headers.size) {
                "Wiersz ${rowNumber + 2} ma ${row.size} pól, oczekiwano ${headers.size}."
            }
            fun value(index: Int?): String = index?.let { row[it].trim() }.orEmpty()
            val date = parseDate(value(dateIndex), rowNumber + 2)
            val currency = value(currencyIndex).uppercase(Locale.ROOT)
            val amount = parseAmount(value(amountIndex), rowNumber + 2)
            val type = value(typeIndex)
            require(type.isNotBlank()) { "Brak typu operacji w wierszu ${rowNumber + 2}." }
            val reference = value(referenceIndex).takeIf { it.isNotBlank() }
            val description = value(descriptionIndex)
            val safeDescription = if (reference == null && description.isNotBlank()) {
                "CSV-OP-" + sha256("$scope|$description")
            } else ""
            BankStatementEntry(
                accountScopeId = scope,
                bookingDate = date,
                currencyCode = currency,
                signedAmount = amount,
                operationType = type,
                bankOperationReference = reference,
                description = safeDescription
            ).also {
                try {
                    it.validate()
                } catch (error: IllegalArgumentException) {
                    throw IllegalArgumentException("Nieprawidłowy zapis w wierszu ${rowNumber + 2}: ${error.message}", error)
                }
            }
        }
        require(entries.isNotEmpty()) { "CSV nie zawiera zapisów do importu." }
        return entries
    }

    private fun detectDelimiter(csv: String): Char {
        var quoted = false
        val counts = linkedMapOf(',' to 0, ';' to 0, '\t' to 0)
        for (character in csv) {
            if (character == '"') quoted = !quoted
            else if (!quoted && (character == '\n' || character == '\r')) break
            else if (!quoted && counts.containsKey(character)) counts[character] = counts.getValue(character) + 1
        }
        return counts.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key
            ?: throw IllegalArgumentException("Nie można rozpoznać separatora CSV.")
    }

    private fun parseRecords(csv: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var afterQuote = false
        var index = 0
        while (index < csv.length) {
            val ch = csv[index]
            when {
                quoted && ch == '"' && index + 1 < csv.length && csv[index + 1] == '"' -> {
                    field.append('"'); index++
                }
                ch == '"' && quoted -> { quoted = false; afterQuote = true }
                ch == '"' && field.isEmpty() && !afterQuote -> quoted = true
                ch == '"' && !quoted -> throw IllegalArgumentException("Cudzysłów w niecytowanym polu CSV.")
                quoted -> field.append(ch)
                ch == delimiter -> {
                    row += field.toString(); field.setLength(0); afterQuote = false
                }
                ch == '\n' || ch == '\r' -> {
                    row += field.toString(); field.setLength(0); afterQuote = false
                    records += row; row = mutableListOf()
                    if (ch == '\r' && index + 1 < csv.length && csv[index + 1] == '\n') index++
                }
                afterQuote && ch.isWhitespace() -> Unit
                afterQuote -> throw IllegalArgumentException("Nieprawidłowy znak po cudzysłowie w CSV.")
                else -> { field.append(ch); afterQuote = false }
            }
            index++
        }
        require(!quoted) { "Niezamknięty cudzysłów w CSV." }
        row += field.toString()
        if (row.any { it.isNotBlank() } || records.isEmpty()) records += row
        return records
    }

    private fun normalizeHeader(value: String): String {
        val ascii = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return ascii.filter { it.isLetterOrDigit() }
    }

    private fun findColumn(indexes: Map<String, Int>, vararg aliases: String): Int? =
        findOptionalColumn(indexes, *aliases)

    private fun findOptionalColumn(indexes: Map<String, Int>, vararg aliases: String): Int? =
        aliases.firstNotNullOfOrNull { indexes[it] }

    private fun parseDate(value: String, row: Int): LocalDate {
        require(value.isNotBlank()) { "Brak daty w wierszu $row." }
        val formats = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd.MM.uuuu"),
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
        )
        for (format in formats) {
            try { return LocalDate.parse(value, format) } catch (_: DateTimeParseException) { }
        }
        throw IllegalArgumentException("Nieprawidłowa data w wierszu $row: $value.")
    }

    private fun parseAmount(value: String, row: Int): BigDecimal {
        require(value.isNotBlank()) { "Brak kwoty w wierszu $row." }
        var text = value.replace("\u00A0", "").replace("\u202F", "").replace(" ", "")
        val comma = text.lastIndexOf(',')
        val dot = text.lastIndexOf('.')
        text = when {
            comma >= 0 && dot >= 0 -> {
                val decimalSeparator = if (comma > dot) ',' else '.'
                val groupingSeparator = if (decimalSeparator == ',') '.' else ','
                text.replace(groupingSeparator.toString(), "").replace(decimalSeparator, '.')
            }
            comma >= 0 -> text.replace(',', '.')
            else -> text
        }
        require(text.matches(Regex("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)"))) {
            "Nieprawidłowa kwota w wierszu $row: $value."
        }
        return try { BigDecimal(text) } catch (error: NumberFormatException) {
            throw IllegalArgumentException("Nieprawidłowa kwota w wierszu $row: $value.", error)
        }.also { require(it.compareTo(BigDecimal.ZERO) != 0) { "Kwota w wierszu $row nie może wynosić zero." } }
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private const val MAX_CSV_CHARACTERS = 10 * 1024 * 1024
    private const val MAX_CSV_BYTES = 10 * 1024 * 1024
    private const val MAX_OPERATIONS = 50_000
}
