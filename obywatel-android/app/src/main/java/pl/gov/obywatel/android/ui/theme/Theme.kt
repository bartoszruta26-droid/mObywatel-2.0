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

private fun palette(primary: Color, primaryContainer: Color, secondary: Color): androidx.compose.material3.ColorScheme =
    lightColorScheme(
        primary = primary, onPrimary = Color.White,
        primaryContainer = primaryContainer, onPrimaryContainer = Color(0xFF172B3D),
        secondary = secondary, onSecondary = Color.White,
        background = Color(0xFFF5F6F8), onBackground = Color(0xFF172B3D),
        surface = Color.White, onSurface = Color(0xFF172B3D),
        surfaceVariant = primaryContainer, onSurfaceVariant = Color(0xFF526273),
        outline = Color(0xFFD8DEE6)
    )

private fun darkPalette(
    primary: Color,
    primaryContainer: Color,
    secondary: Color
): androidx.compose.material3.ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF102030),
    primaryContainer = primaryContainer,
    onPrimaryContainer = Color(0xFFE8EEF5),
    secondary = secondary,
    onSecondary = Color(0xFF15202B),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE7EDF3),
    surface = Color(0xFF171D23),
    onSurface = Color(0xFFE7EDF3),
    surfaceVariant = Color(0xFF28323C),
    onSurfaceVariant = Color(0xFFBEC8D2),
    outline = Color(0xFF56616D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun ObywatelTheme(themeMode: String = "system", content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val colors = when (themeMode) {
        "dark" -> DarkColors
        "light" -> LightColors
        "blue" -> if (systemDark) darkPalette(Color(0xFF8AB4E8), Color(0xFF183D5D), Color(0xFFB7CCE3))
            else palette(Color(0xFF0757A6), Color(0xFFE0EDFC), Color(0xFF123B65))
        "green" -> if (systemDark) darkPalette(Color(0xFF8FD5AE), Color(0xFF1B4B36), Color(0xFFB7E2C8))
            else palette(Color(0xFF236B4A), Color(0xFFE0F1E7), Color(0xFF174A35))
        "violet" -> if (systemDark) darkPalette(Color(0xFFC4A8F4), Color(0xFF3B285D), Color(0xFFD9C9F7))
            else palette(Color(0xFF6543A5), Color(0xFFECE5FA), Color(0xFF493078))
        "amber" -> if (systemDark) darkPalette(Color(0xFFFFD18A), Color(0xFF59401E), Color(0xFFF2D3A1))
            else palette(Color(0xFF8A5410), Color(0xFFFFEFD2), Color(0xFF67400E))
        "graphite" -> if (systemDark) darkPalette(Color(0xFFC2CBD4), Color(0xFF343E48), Color(0xFFD2D9E0))
            else palette(Color(0xFF46515C), Color(0xFFE5E9ED), Color(0xFF303943))
        else -> if (systemDark) DarkColors else LightColors
    }
    MaterialTheme(colorScheme = colors, typography = MaterialTheme.typography, content = content)
}
