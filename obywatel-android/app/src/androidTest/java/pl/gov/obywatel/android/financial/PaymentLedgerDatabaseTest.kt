// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentLedgerDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "payment-ledger-test.db"

    @After
    fun cleanup() {
        context.deleteDatabase(databaseName)
    }

    private fun openDatabase() = PaymentLedgerDatabase(context, databaseName)

    private fun entry(amount: String = "749.33", reference: String = "102693731") =
        BankStatementEntry(
            accountScopeId = "test-account",
            bookingDate = LocalDate.parse("2026-11-12"),
            currencyCode = "USD",
            signedAmount = BigDecimal(amount),
            operationType = "WYMIANA W KANTORZE - UZNANIE",
            bankOperationReference = reference
        )

    @Test
    fun importingSameEntryTwicePersistsItOnlyOnce() {
        val db = openDatabase()
        try {
            assertEquals(BatchImportSummary(1, 0), db.importBatch(listOf(entry())))
            assertEquals(BatchImportSummary(0, 1), db.importBatch(listOf(entry("749.330"))))
            assertEquals(1, db.readAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun invalidEntryRollsBackWholeBatch() {
        val db = openDatabase()
        try {
            val invalid = entry().copy(currencyCode = "US")
            assertThrows(IllegalArgumentException::class.java) {
                db.importBatch(listOf(entry(), invalid))
            }
            assertEquals(0, db.readAll().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun recordsSurviveDatabaseReopen() {
        val first = openDatabase()
        first.importBatch(listOf(entry()))
        first.close()

        val second = openDatabase()
        try {
            assertEquals(1, second.readAll().size)
            assertEquals(entry().accountingEntryId(), second.readAll().single().accountingEntryId())
        } finally {
            second.close()
        }
    }
}
