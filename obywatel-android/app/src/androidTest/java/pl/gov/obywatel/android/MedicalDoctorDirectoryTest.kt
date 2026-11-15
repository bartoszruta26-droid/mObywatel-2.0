// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicalDoctorDirectoryTest {
    @Test fun seededDirectoryContainsOnlyInformationalNonSigners() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("medical_doctor_directory", 0)
        val previous = prefs.getString("records_v1", null)
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
            if (previous != null) edit.putString("records_v1", previous)
            edit.commit()
        }
    }

    @Test fun directorySupportsUpdatingAndDeletingRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("medical_doctor_directory", 0)
        val previous = prefs.getString("records_v1", null)
        try {
            prefs.edit().clear().commit()
            val original = MedicalDoctorDirectory.list(context).first()
            val updated = original.copy(phone = "+55 11 0000-0000")
            MedicalDoctorDirectory.save(context, updated)
            assertEquals("+55 11 0000-0000", MedicalDoctorDirectory.list(context).first { it.id == original.id }.phone)
            MedicalDoctorDirectory.delete(context, original.id)
            assertFalse(MedicalDoctorDirectory.list(context).any { it.id == original.id })
        } finally {
            val edit = prefs.edit().clear()
            if (previous != null) edit.putString("records_v1", previous)
            edit.commit()
        }
    }
}
