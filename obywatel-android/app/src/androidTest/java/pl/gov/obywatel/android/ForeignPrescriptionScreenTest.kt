// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ForeignPrescriptionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetDoctorDirectory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("medical_doctor_directory", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun polishPrescriptionLabelsCoverAllFieldsAndPrescriberSummary() {
        assertTrue(foreignPrescriptionPolishLabel("patientName") == "Pełne imię i nazwisko pacjenta")
        assertTrue(foreignPrescriptionPolishLabel("prescriberName") == "Pełne imię i nazwisko osoby przepisującej")
        assertTrue(foreignPrescriptionPolishLabel("medicineStrength") == "Dawka")
        assertTrue(foreignPrescriptionPolishLabel("medicineDosage") == "Dawkowanie i sposób stosowania")
        assertTrue(foreignPrescriptionPolishLabel("prescriberQualifications") == "Tytuł zawodowy / kwalifikacje")
        assertTrue(foreignPrescriptionPolishLabel("prescriberRegister") == "Numer prawa wykonywania zawodu / numer w rejestrze")
        assertTrue(foreignPrescriptionPolishLabel("prescriberContact") == "Dane kontaktowe wystawcy")
    }

    @Test
    fun editorRendersPrescriptionFieldsAndActions() {
        composeRule.setContent {
            MaterialTheme {
                ForeignPrescriptionEditorScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("Recepta zagraniczna").assertIsDisplayed()
        composeRule.onNodeWithText("Dane lekarza").assertIsDisplayed()
        composeRule.onNodeWithText("Dane pacjenta, produktu leczniczego i wystawcy").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Patient full name").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Patient address").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("International nonproprietary name (INN)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Prescriber's email or phone with international prefix").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Przetłumacz automatycznie").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Zapisz receptę").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Eksportuj PDF dwujęzyczny").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun prescriptionPdfBuildsAsAPrintableBilingualPdf() {
        val bytes = createBilingualMedicalCertificatePdf(
            sourceLanguage = "pt",
            targetLanguage = "pl",
            fields = listOf(
                MedicalCertificatePdfField(
                    sourceLabel = "International nonproprietary name (INN)",
                    sourceValue = "amoxicillin",
                    targetLabel = "Nazwa powszechnie stosowana",
                    targetValue = "amoksycylina"
                )
            ),
            doctorName = "Dr Example",
            doctorPractice = "Example practice",
            doctorAddress = "São Paulo, Brazil",
            doctorRegister = "CRM-SP 101458; RQE 45010",
            doctorContact = "doctor@example.test",
            doctorQualifications = "Medical Doctor",
            doctorSpecialty = "Family Medicine",
            sideBySideFields = true,
            documentTitle = "FOREIGN PRESCRIPTION / RECEPTA ZAGRANICZNA",
            issuerHeading = "PRESCRIBER / WYSTAWCA RECEPTY",
            signatureCaption = "Prescriber's handwritten signature (sign after printing)",
            stampCaption = "Prescriber's stamp"
        )
        assertTrue(bytes.size > 500)
        assertTrue(String(bytes.copyOfRange(0, 5), Charsets.US_ASCII) == "%PDF-")
    }

    @Test
    fun multipageBilingualPdfRepeatsPatientHeaderAndCreatesAllPages() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fields = mutableListOf(
            MedicalCertificatePdfField("Patient full name", "Jan Kowalski", "Pełne imię i nazwisko pacjenta", "Jan Kowalski"),
            MedicalCertificatePdfField("Patient address", "Main Street 1, Warsaw", "Adres pacjenta", "ul. Główna 1, Warszawa")
        )
        repeat(55) { index ->
            fields += MedicalCertificatePdfField(
                "Clinical information $index",
                "Source text for clinical information item $index",
                "Informacja kliniczna $index",
                "Tekst tłumaczenia informacji klinicznej $index"
            )
        }
        val bytes = createBilingualMedicalCertificatePdf(
            sourceLanguage = "en",
            targetLanguage = "pl",
            fields = fields,
            sideBySideFields = true,
            documentTitle = "INTERNATIONAL MEDICAL CERTIFICATE"
        )
        val file = File(context.cacheDir, "multipage-medical-certificate-test.pdf")
        file.writeBytes(bytes)
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(descriptor).use { renderer ->
            assertTrue("Expected more than one PDF page", renderer.pageCount > 1)
        }
        file.delete()
    }

    @Test
    fun doctorPickerUsesTheExistingDirectoryAndPrefillsTheSelectedDoctor() {
        composeRule.setContent {
            MaterialTheme {
                ForeignPrescriptionEditorScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("Wybierz lekarza z katalogu").performClick()
        composeRule.onNodeWithText("Dr Gustavo Polacow Korn — São Paulo").performClick()
        composeRule.onAllNodesWithText("Dr Gustavo Polacow Korn").assertCountEquals(2)
    }
}
