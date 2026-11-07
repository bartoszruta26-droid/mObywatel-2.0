// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pl.gov.obywatel.android.ui.documents.DocumentsScreen
import pl.gov.obywatel.android.ui.home.HomeScreen
import pl.gov.obywatel.android.ui.theme.ObywatelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showDocuments by remember { mutableStateOf(false) }
            BackHandler(enabled = showDocuments) { showDocuments = false }
            ObywatelTheme {
                if (showDocuments) {
                    DocumentsScreen(onBack = { showDocuments = false })
                } else {
                    HomeScreen(onOpenDocuments = { showDocuments = true })
                }
            }
        }
    }
}
