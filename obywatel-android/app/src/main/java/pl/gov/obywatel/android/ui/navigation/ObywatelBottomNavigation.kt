// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import pl.gov.obywatel.android.R

private data class Tab(
    val label: String,
    val route: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
)

private val tabs = listOf(
    Tab("Dokumenty", "documents", R.drawable.nav_documents, R.drawable.nav_documents_selected),
    Tab("Usługi", "services", R.drawable.nav_services, R.drawable.nav_services_selected),
    Tab("Szukaj", "search", R.drawable.nav_search, R.drawable.nav_search_selected),
    Tab("Skanuj", "verify", R.drawable.nav_scan, R.drawable.nav_scan_selected),
    Tab("Więcej", "more", R.drawable.nav_more, R.drawable.nav_more_selected)
)

@Composable
fun ObywatelBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val selectedRoute = when (currentRoute) {
        "home", "documents", "registry" -> "documents"
        "services" -> "services"
        "search" -> "search"
        "verify" -> "verify"
        else -> "more"
    }
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = androidx.compose.ui.unit.Dp(3f)
    ) {
        tabs.forEach { tab ->
            val selected = selectedRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = {
                    Icon(
                        painter = painterResource(if (selected) tab.selectedIcon else tab.icon),
                        contentDescription = null
                    )
                },
                label = { Text(tab.label) },
                alwaysShowLabel = true
            )
        }
    }
}
