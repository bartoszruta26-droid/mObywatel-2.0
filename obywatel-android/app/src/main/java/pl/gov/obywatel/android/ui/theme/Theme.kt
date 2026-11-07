// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightAppColors = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = BlueLight,
    onPrimaryContainer = Navy,
    secondary = Navy,
    onSecondary = White,
    background = Surface,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = BlueLight,
    onSurfaceVariant = Muted,
    outline = Border
)

private val DarkAppColors = darkColorScheme(
    primary = Color(0xFF8FC7FF),
    onPrimary = Color(0xFF003354),
    primaryContainer = Color(0xFF174B70),
    onPrimaryContainer = Color(0xFFD0E8FF),
    secondary = Color(0xFFB7C9DA),
    onSecondary = Color(0xFF233746),
    background = Color(0xFF101820),
    onBackground = Color(0xFFE5EDF4),
    surface = Color(0xFF17232D),
    onSurface = Color(0xFFE5EDF4),
    surfaceVariant = Color(0xFF293945),
    onSurfaceVariant = Color(0xFFB8C7D3),
    // Widoczny obrys kontrolek w ciemnym motywie (kontrast powyżej 3:1).
    outline = Color(0xFF71879A)
)

@Composable
fun ObywatelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkAppColors else LightAppColors,
        content = content
    )
}
