// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Blue, onPrimary = White, primaryContainer = BlueLight, onPrimaryContainer = Navy,
    secondary = Navy, background = Surface, onBackground = Ink, surface = White, onSurface = Ink,
    surfaceVariant = BlueLight, onSurfaceVariant = Muted, outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4E8), onPrimary = Color(0xFF082B4B),
    primaryContainer = Color(0xFF183D5D), onPrimaryContainer = Color(0xFFD8E9FA),
    secondary = Color(0xFFB7CCE3), background = DarkSurface, onBackground = DarkInk,
    surface = DarkSurface, onSurface = DarkInk, surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted, outline = DarkBorder
)

@Composable
fun ObywatelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
