package pl.gov.obywatel.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = BlueLight,
    onPrimaryContainer = Navy,
    secondary = Navy,
    background = Surface,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    outline = Border
)

@Composable
fun ObywatelTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
