// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicalDoctorDirectoryTest {
    @Test fun seededDirectoryContainsOnlyInformationalNonSigners() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("medical_doctor_directory", 0)
        val previous = prefs.getString("records_v2", null)
        val legacy = prefs.getString("records_v1", null)
        try {
            prefs.edit().clear().commit()
            val doctors = MedicalDoctorDirectory.list(context)
            assertEquals(3, doctors.size)
            assertTrue(doctors.all { it.practice.isNotBlank() && it.address.isNotBlank() && it.source.isNotBlank() })
            assertTrue(doctors.all { !it.signerEnabled })
            assertTrue(doctors.all { !it.polishDeclared })
            assertTrue(doctors.any { it.name.contains("Czerwonko") })
            assertTrue(doctors.any { it.name.contains("Stanicki") })
        } finally {
            val edit = prefs.edit().clear()
            if (previous != null) edit.putString("records_v2", previous)
            if (legacy != null) edit.putString("records_v1", legacy)
            edit.commit()
        }
    }

    @Test fun legacyDirectoryRecordsAreMigratedWithoutLoss() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("medical_doctor_directory", 0)
        val previous = prefs.getString("records_v2", null)
        val legacy = prefs.getString("records_v1", null)
        try {
            val oldRecord = JSONObject().apply {
                put("id", "legacy-doctor")
                put("name", "Dr Legacy")
                put("specialty", "Internal medicine")
                put("country", "Argentina")
                put("city", "Buenos Aires")
                put("practice", "Legacy clinic")
                put("address", "Calle 1")
                put("phone", "")
                put("website", "https://example.org")
                put("register", "")
                put("license", "")
                put("evidence", "POLISH_ORIGIN_SURNAME")
                put("polish", false)
                put("licenseStatus", "PENDING")
                put("consent", "USER_REPORTED_ORAL")
                put("source", "https://example.org")
                put("verifiedAt", "2026-11-15")
                put("signerEnabled", false)
            }
            prefs.edit().clear().putString("records_v1", JSONArray().put(oldRecord).toString()).commit()
            val migrated = MedicalDoctorDirectory.list(context)
            assertEquals(1, migrated.size)
            assertEquals("Dr Legacy", migrated.single().name)
            assertEquals("Legacy clinic", migrated.single().practice)
            assertTrue(prefs.contains("records_v2"))
        } finally {
            val edit = prefs.edit().clear()
            if (previous != null) edit.putString("records_v2", previous)
            if (legacy != null) edit.putString("records_v1", legacy)
            edit.commit()
        }
    }

    @Test fun directorySupportsUpdatingAndDeletingRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("medical_doctor_directory", 0)
        val previous = prefs.getString("records_v2", null)
        val legacy = prefs.getString("records_v1", null)
        try {
            prefs.edit().clear().commit()
            val original = MedicalDoctorDirectory.list(context).first()
            val updated = original.copy(
                phone = "+55 11 0000-0000", register = "", license = "", source = "", verifiedAt = ""
            )
            MedicalDoctorDirectory.save(context, updated)
            val saved = MedicalDoctorDirectory.list(context).first { it.id == original.id }
            assertEquals("+55 11 0000-0000", saved.phone)
            assertTrue(saved.register.isBlank() && saved.license.isBlank())
            MedicalDoctorDirectory.delete(context, original.id)
            assertFalse(MedicalDoctorDirectory.list(context).any { it.id == original.id })
        } finally {
            val edit = prefs.edit().clear()
            if (previous != null) edit.putString("records_v2", previous)
            if (legacy != null) edit.putString("records_v1", legacy)
            edit.commit()
        }
    }
}
