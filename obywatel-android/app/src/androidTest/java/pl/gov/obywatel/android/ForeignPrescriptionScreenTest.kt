// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
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
            doctorContact = "+55 11 3812-1157",
            documentTitle = "FOREIGN PRESCRIPTION / RECEPTA ZAGRANICZNA",
            issuerHeading = "PRESCRIBER / WYSTAWCA RECEPTY",
            signatureCaption = "Prescriber's handwritten signature (sign after printing)",
            stampCaption = "Prescriber's stamp"
        )
        assertTrue(bytes.size > 500)
        assertTrue(String(bytes.copyOfRange(0, 5), Charsets.US_ASCII) == "%PDF-")
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
