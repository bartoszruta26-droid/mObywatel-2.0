// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.util.Xml
import java.io.StringReader
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

/**
 * Parser formatu XML iPKO account-history.
 *
 * accountScopeId jest pseudonimem nadanym lokalnie przez aplikację/użytkownika.
 * Parser celowo nie zapisuje numeru rachunku, nazw, adresów ani pełnego opisu kontrahenta.
 */
object BankStatementXmlParser {
    private val referencePattern = Regex(
        """Referencje\s+własne\s+zleceniodawcy\s*:\s*([A-Za-z0-9_-]+)""",
        RegexOption.IGNORE_CASE
    )
    private val titlePattern = Regex(
        """Tytuł\s*:\s*(.*?)(?=\s+Referencje\s+własne\s+zleceniodawcy\s*:|$)""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    fun parse(xml: String, accountScopeId: String): List<BankStatementEntry> {
        require(accountScopeId.isNotBlank()) { "Lokalny identyfikator rachunku jest wymagany." }
        require(xml.isNotBlank()) { "Plik XML jest pusty." }

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        parser.setInput(StringReader(xml))

        val entries = mutableListOf<BankStatementEntry>()
        var current: Operation? = null
        var rootValidated = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (!rootValidated) {
                        require(parser.name == "account-history") {
                            "Nieobsługiwany dokument XML wyciągu bankowego."
                        }
                        rootValidated = true
                    }
                    when (parser.name) {
                        "operation" -> current = Operation()
                        "order-date" -> current?.orderDate = parser.nextText().trim()
                        "exec-date" -> current?.executionDate = parser.nextText().trim()
                        "type" -> current?.type = parser.nextText().trim()
                        "description" -> current?.description = parser.nextText().trim()
                        "amount" -> {
                            current?.currency = parser.getAttributeValue(null, "curr")?.trim()
                            current?.amount = parser.nextText().trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name == "operation") {
                    val operation = current
                        ?: throw IllegalArgumentException("Nieprawidłowy element operation.")
                    entries += operation.toEntry(accountScopeId)
                    current = null
                }
            }
            event = parser.next()
        }
        require(rootValidated) { "Dokument XML nie zawiera elementu account-history." }
        require(entries.isNotEmpty()) { "Dokument XML nie zawiera operacji do importu." }
        return entries
    }

    private fun Operation.toEntry(accountScopeId: String): BankStatementEntry {
        val dateText = executionDate.ifBlank { orderDate }
        require(dateText.isNotBlank()) { "Operacja XML nie zawiera daty wykonania ani zlecenia." }
        val date = try {
            LocalDate.parse(dateText)
        } catch (error: Exception) {
            throw IllegalArgumentException("Nieprawidłowa data operacji: $dateText", error)
        }
        val currencyCode = currency
            ?: throw IllegalArgumentException("Operacja XML nie zawiera kodu waluty.")
        val amountValue = amount
            ?: throw IllegalArgumentException("Operacja XML nie zawiera kwoty.")
        val decimal = try {
            BigDecimal(amountValue.replace(',', '.'))
        } catch (error: Exception) {
            throw IllegalArgumentException("Nieprawidłowa kwota operacji: $amountValue", error)
        }
        val reference = referencePattern.find(description)?.groupValues?.get(1)
        val safeTitle = titlePattern.find(description)?.groupValues?.get(1)?.trim().orEmpty()
        return BankStatementEntry(
            accountScopeId = accountScopeId.trim(),
            bookingDate = date,
            currencyCode = currencyCode.uppercase(Locale.ROOT),
            signedAmount = decimal,
            operationType = type,
            bankOperationReference = reference,
            description = safeTitle
        ).also { it.validate() }
    }

    private data class Operation(
        var orderDate: String = "",
        var executionDate: String = "",
        var type: String = "",
        var description: String = "",
        var currency: String? = null,
        var amount: String? = null
    )
}
