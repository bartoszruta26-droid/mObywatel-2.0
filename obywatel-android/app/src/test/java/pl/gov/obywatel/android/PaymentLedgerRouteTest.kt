package pl.gov.obywatel.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PaymentLedgerRouteTest {
    @Test
    fun paymentTransactionsRouteIsSpecializedAndVisibleOnDashboard() {
        val route = requireNotNull(DocumentRouteRegistry.find("payment_transactions"))
        assertEquals(DocumentRouteType.SPECIALIZED, route.type)
        assertEquals("payment_transactions", route.destination)
        assertEquals("Służba finansowa", documentVisibilityGroup("payment_transactions"))
        assertEquals("dashboard", defaultDocumentPlacements["payment_transactions"])
    }
}
