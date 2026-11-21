// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentAccountDocumentsTest {
    @Test
    fun fivePaymentAccountDocumentsHaveUniqueCatalogEntriesGroupsRoutesAndSchemas() {
        val expected = mapOf(
            "payment_account_foundation" to "Służba fundacyjna",
            "payment_account_association" to "Służba stowarzyszeniowa",
            "payment_account_church" to "Służba kościelna",
            "payment_account_business" to "Służba gospodarcza",
            "payment_account_person" to "Służba osobista"
        )
        assertEquals(5, paymentAccountDocumentTargets().size)
        assertEquals(5, paymentAccountDocumentTargets().map { it.first }.toSet().size)
        expected.forEach { (id, group) ->
            assertTrue(documentDisplayOptions.any { it.first == id })
            assertEquals(group, documentVisibilityGroup(id))
            assertNotNull(DocumentRouteRegistry.find(id))
            val fields = documentEditorFieldDefinitions(id)
            assertTrue(fields.isNotEmpty())
            assertEquals(fields.size, fields.map { it.key }.toSet().size)
            val keys = fields.map { it.key }.toSet()
            assertTrue(keys.containsAll(setOf("accountHolderName", "accountBankName", "accountIban", "accountCurrency")))
            assertTrue(keys.containsAll(setOf("donationPurpose", "donationDate", "donationAmount", "donationCurrency")))
            assertTrue(keys.containsAll(setOf("cashReceiptNumber", "cashBankDepositReference", "cashAccountingReference")))
        }
    }

    @Test
    fun cashLedgerStorageNamespacesAreIndependentPerDocument() {
        val ids = paymentAccountDocumentTargets().map { it.first }
        val keys = paymentAccountDocumentFields(ids.first()).map { it.first }
            .filter { it.startsWith("cash") }
        assertTrue(keys.isNotEmpty())
        val storageKeys = ids.flatMap { id -> keys.map { key -> "doc_${id}_${key}" } }
        assertEquals(storageKeys.size, storageKeys.toSet().size)
        assertFalse(keys.contains("bankPassword"))
        assertFalse(keys.contains("bankPin"))
    }
}
