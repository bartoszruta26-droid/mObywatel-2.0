// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pl.gov.obywatel.android.ui.documents.DocumentsScreen
import pl.gov.obywatel.android.ui.home.HomeScreen
import pl.gov.obywatel.android.ui.identity.IdentityDocumentsScreen
import pl.gov.obywatel.android.ui.services.PublicServicesScreen
import pl.gov.obywatel.android.ui.navigation.ObywatelBottomNavigation
import pl.gov.obywatel.android.ui.registry.RegistryScreen
import pl.gov.obywatel.android.ui.sections.SectionScreen
import pl.gov.obywatel.android.ui.theme.ObywatelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var route by remember { mutableStateOf("home") }
            BackHandler(enabled = route != "home") { route = "home" }
            ObywatelTheme {
                Scaffold(
                    bottomBar = {
                        ObywatelBottomNavigation(
                            currentRoute = route,
                            onNavigate = { route = it }
                        )
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        when (route) {
                            "home" -> HomeScreen(onOpenSection = { route = it })
                            "documents" -> DocumentsScreen(onBack = { route = "home" })
                            "registry" -> RegistryScreen(onBack = { route = "home" })
                            "identity" -> IdentityDocumentsScreen(onBack = { route = "home" })
                            "services" -> PublicServicesScreen(onBack = { route = "home" })
                            else -> SectionScreen(sectionId = route, onBack = { route = "home" })
                        }
                    }
                }
            }
        }
    }
}
