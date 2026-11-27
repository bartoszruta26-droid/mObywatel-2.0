// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.gov.obywatel.android.R

@Composable
internal fun DisabilityDecisionPreview(fields: Map<String, String>) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val settings = rememberLegitimacyPreviewSettings("disability").value
    CredentialTemplatePreview(
        documentKey = "disability",
        settings = settings,
        values = fields
    )
}

@Composable
internal fun SchoolLegitimacyPreview(fields: Map<String, String>, photoBase64: String, student: Boolean) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = if (student) "student" else "training"
    val settingsState = rememberLegitimacyPreviewSettings(documentKey)
    val settings = settingsState.value
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val name = if (student) fields["studentNames"].orEmpty().ifBlank {
        listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ")
    } else listOf(fields["firstNames"], fields["lastName"]).filterNot { it.isNullOrBlank() }.joinToString(" ")
    val institution = if (student) fields["studentUniversity"].orEmpty() else fields["trainingInstitution"].orEmpty()
    val valid = credentialValidityForDocument(documentKey, fields)
    val cardFields = credentialFieldOptions(documentKey).map { (key, label) ->
        Triple(key, label, fields[key].orEmpty().ifBlank { "—" })
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (settings.showCard) {
            CredentialCardSides(
                documentKey = documentKey,
                title = if (student) "LEGITYMACJA UCZNIA / STUDENTA" else "LEGITYMACJA KURSANTA",
                subtitle = institution,
                holderName = name,
                fields = cardFields,
                settings = settings,
                photo = photoBitmap,
                fallbackBackgroundRes = if (student) R.drawable.tlo_legitymacji_niebieskie else R.drawable.tlo_legitymacji_brazowe,
                validity = valid
            )
        }
    }
}

@Composable
internal fun ParliamentaryCredentialPreview(fields: Map<String, String>) {
    if (LocalDocumentScreenMode.current == DocumentScreenMode.EDITOR) return
    val documentKey = "parliament"
    val settingsState = rememberLegitimacyPreviewSettings(documentKey)
    val settings = settingsState.value
    val photoBase64 = fields["photo"].orEmpty()
    val photoBitmap = remember(photoBase64) {
        runCatching {
            if (photoBase64.isBlank()) null else {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 500 || bounds.outHeight / sample > 500) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }.getOrNull()
    }
    val fullName = listOf(fields["firstNames"], fields["lastName"])
        .filterNot { it.isNullOrBlank() }.joinToString(" ")
    val valid = credentialValidityForDocument(documentKey, fields)
    val cardFields = credentialFieldOptions(documentKey).map { (key, label) ->
        Triple(key, label, fields[key].orEmpty().ifBlank { "—" })
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (settings.showCard) {
            CredentialCardSides(
                documentKey = documentKey,
                title = "LEGITYMACJA POSELSKA",
                subtitle = "SEJM RZECZYPOSPOLITEJ POLSKIEJ",
                holderName = fullName,
                fields = cardFields,
                settings = settings,
                photo = photoBitmap,
                fallbackBackgroundRes = R.drawable.tlo_legitymacji_szare,
                validity = valid
            )
        }
    }
}

@Composable
internal fun CivicDocumentStatusTile(fields: Map<String, String>, documentId: String) {
    val documentKey = when (documentId) {
        "1" -> "identity"
        "2" -> "passport"
        "4" -> "parliament"
        "5" -> "training"
        "6" -> "student"
        "7" -> "disability"
        else -> "family"
    }
    CredentialValidityTile(credentialValidityForDocument(documentKey, fields))
}
