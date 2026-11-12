// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class ApplicationAppearance(
    val theme: String = "system",
    val introEmblem: String = "file_00000000532082108d561dcd2385f8f9",
    val welcomeBackground: String = "file_00000000e0e4820a839fa3184993f41b",
    val loginBackground: String = "file_00000000e0e4820a839fa3184993f41b",
    val headerEmblem: String = "file_00000000532082108d561dcd2385f8f9"
)

internal val LocalApplicationAppearance = compositionLocalOf { ApplicationAppearance() }

internal object ApplicationAppearanceStore {
    private const val PREFS = "application_appearance"
    fun load(context: Context): ApplicationAppearance {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ApplicationAppearance(
            theme = p.getString("theme", "system").orEmpty(),
            introEmblem = p.getString("intro_emblem", "file_00000000532082108d561dcd2385f8f9").orEmpty(),
            welcomeBackground = p.getString("welcome_background", "file_00000000e0e4820a839fa3184993f41b").orEmpty(),
            loginBackground = p.getString("login_background", "file_00000000e0e4820a839fa3184993f41b").orEmpty(),
            headerEmblem = p.getString("header_emblem", "file_00000000532082108d561dcd2385f8f9").orEmpty()
        )
    }
    fun save(context: Context, value: ApplicationAppearance) {
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("theme", value.theme)
            .putString("intro_emblem", value.introEmblem)
            .putString("welcome_background", value.welcomeBackground)
            .putString("login_background", value.loginBackground)
            .putString("header_emblem", value.headerEmblem)
            .commit()) { "Nie udało się zapisać ustawień wyglądu aplikacji." }
    }
}

internal fun applicationDrawable(context: Context, name: String, fallback: Int): Int =
    context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 } ?: fallback

private val emblemChoices = listOf(
    "file_00000000532082108d561dcd2385f8f9" to "Godło aplikacji — wzór podstawowy",
    "file_5320_eagle_vector" to "Orzeł wektorowy — bez tła",
    "file_5320_eagle_background_vector" to "Orzeł wektorowy — z tłem",
    "institutions_eagle_emblem_vector" to "Godło instytucjonalne",
    "dashboard_eagle_vector" to "Orzeł pulpitu"
)
private val backgroundChoices = listOf(
    "file_00000000e0e4820a839fa3184993f41b" to "Polska — flaga na maszcie",
    "header_main_banner" to "Baner główny",
    "header_poland_banner" to "Baner Polski",
    "brand_header_illustrative" to "Ilustracja marki",
    "polish_flag_illustration" to "Flaga Polski — ilustracja",
    "file_000000007db0820a9085e7c796d4701a" to "Tło graficzne 2",
    "file_000000007f3c820a942cfb69396276d6" to "Tło graficzne 3",
    "file_00000000818c8210b6fe5da7d5c5f9f1" to "Tło graficzne 4"
)

@Composable
internal fun ApplicationAppearanceSettingsScreen(
    appearance: ApplicationAppearance,
    onChange: (ApplicationAppearance) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack).padding(vertical = 5.dp)) {
            androidx.compose.material3.Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Text("Powrót do ustawień", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Text("Ustawienia aplikacji", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Wybierz motyw kolorystyczny i elementy identyfikacji wizualnej aplikacji. Zmiany są zapisywane na tym urządzeniu.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppearanceSection("Motyw i kolory", Icons.Filled.Palette) {
            listOf(
                "system" to "Systemowy",
                "light" to "Jasny",
                "dark" to "Ciemny",
                "blue" to "Błękitny",
                "green" to "Zielony",
                "violet" to "Fioletowy",
                "amber" to "Bursztynowy",
                "graphite" to "Grafitowy"
            ).forEach { (key, label) ->
                AppearanceChoiceRow(label, appearance.theme == key) { onChange(appearance.copy(theme = key)) }
            }
        }
        AppearanceSection("Godło ekranu początkowego", Icons.Filled.ImageIcon) {
            Text("Znak widoczny podczas uruchamiania aplikacji.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            emblemChoices.forEach { (name, label) ->
                AppearanceImageChoice(context, label, name, appearance.introEmblem == name,
                    R.drawable.file_00000000532082108d561dcd2385f8f9) {
                    onChange(appearance.copy(introEmblem = name))
                }
            }
        }
        AppearanceSection("Tło ekranu powitalnego przed logowaniem", Icons.Filled.ImageIcon) {
            backgroundChoices.forEach { (name, label) ->
                AppearanceImageChoice(context, label, name, appearance.welcomeBackground == name,
                    R.drawable.file_00000000e0e4820a839fa3184993f41b) {
                    onChange(appearance.copy(welcomeBackground = name))
                }
            }
        }
        AppearanceSection("Tło ekranu logowania", Icons.Filled.ImageIcon) {
            backgroundChoices.forEach { (name, label) ->
                AppearanceImageChoice(context, label, name, appearance.loginBackground == name,
                    R.drawable.file_00000000e0e4820a839fa3184993f41b) {
                    onChange(appearance.copy(loginBackground = name))
                }
            }
        }
        AppearanceSection("Godło aplikacji w lewym górnym rogu", Icons.Filled.ColorLens) {
            Text("Znak wyświetlany obok nazwy sekcji na głównym ekranie aplikacji.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            emblemChoices.forEach { (name, label) ->
                AppearanceImageChoice(context, label, name, appearance.headerEmblem == name,
                    R.drawable.file_00000000532082108d561dcd2385f8f9) {
                    onChange(appearance.copy(headerEmblem = name))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AppearanceSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            content()
        }
    }
}

@Composable
private fun AppearanceChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
        .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
        .clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AppearanceImageChoice(
    context: Context,
    label: String,
    resourceName: String,
    selected: Boolean,
    fallback: Int,
    onClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
        .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
        .clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(painterResource(applicationDrawable(context, resourceName, fallback)),
            contentDescription = label, modifier = Modifier.size(62.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            Text(resourceName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}
