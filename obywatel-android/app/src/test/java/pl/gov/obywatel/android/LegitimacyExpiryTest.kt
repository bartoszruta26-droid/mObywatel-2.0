// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals\nimport org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegitimacyExpiryTest {
    @Test
    fun emptyExpiryRemainsValid() {
        assertTrue(isCredentialValidByExpiry(""))
        assertTrue(isCredentialValidByExpiry("   "))
    }

    @Test
    fun nonDateTextRemainsValid() {
        assertTrue(isCredentialValidByExpiry("bezterminowo"))
        assertTrue(isCredentialValidByExpiry("do odwołania"))
        assertTrue(isCredentialValidByExpiry("ważność określona w decyzji"))
    }

    @Test
    fun pastCalendarDateIsInvalid() {
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("d.M.uuuu"))
        assertFalse(isCredentialValidByExpiry(yesterday))
    }

    @Test
    fun todayAndFutureDatesRemainValid() {
        val formatter = DateTimeFormatter.ofPattern("d.M.uuuu")
        assertTrue(isCredentialValidByExpiry(LocalDate.now().format(formatter)))
        assertTrue(isCredentialValidByExpiry(LocalDate.now().plusDays(1).format(formatter)))
    }

    @Test
    fun isoPastDateIsInvalid() {
        assertFalse(isCredentialValidByExpiry(LocalDate.now().minusDays(1).toString()))
    }

    @Test
    fun documentSpecificExpiryIsUsedForIdentityAndProfessionalCredentials() {
        val past = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("d.M.uuuu"))
        assertFalse(credentialValidityForDocument("identity", mapOf("idExpiryDate" to past))!!)
        assertFalse(credentialValidityForDocument("professional_medical", mapOf("professionalValidity" to past))!!)
    }

    @Test
    fun explicitRevocationOverridesFutureExpiry() {
        val future = LocalDate.now().plusDays(30).toString()
        assertFalse(credentialValidityForDocument("parliament", mapOf(
            "mpStatus" to "unieważniona",
            "mpEnd" to future
        ))!!)
    }

    @Test
    fun missingValidityDataIsUnknownRatherThanAssumedValid() {
        assertEquals(null, credentialValidityForDocument("identity", emptyMap()))
    }

    @Test
    fun activeStatusWithoutExpiryRemainsValid() {
        assertTrue(credentialValidityForDocument("passport", mapOf("passportStatus" to "aktywny"))!!)
    }
}
