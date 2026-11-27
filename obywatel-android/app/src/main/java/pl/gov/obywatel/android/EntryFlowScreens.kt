// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Handles only the welcome/login/demo entry flow. The actual navigation and
 * home state remain owned by MainActivity/HomeScreen.
 */
@Composable
internal fun ApplicationEntryFlow(onAppearanceChange: (ApplicationAppearance) -> Unit) {
    var stage by rememberSaveable { mutableIntStateOf(0) }
    when (stage) {
        0 -> EntryWelcomeScreen(onStart = { stage = 1 }, onSkip = { stage = 1 })
        1 -> EntryLoginScreen(onDemo = { stage = 2 }, onBack = { stage = 0 })
        else -> HomeScreen(onAppearanceChange = onAppearanceChange)
    }
}

private val ENTRY_BRAND_MARK_SIZE = 64.dp

@Composable
private fun BrandMark(size: androidx.compose.ui.unit.Dp) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Image(
        painter = painterResource(
            applicationDrawable(
                context,
                appearance.headerEmblem,
                R.drawable.file_00000000532082108d561dcd2385f8f9
            )
        ),
        contentDescription = "Godło aplikacji",
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun EntryBackground() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appearance = LocalApplicationAppearance.current
    Image(
        painter = painterResource(
            applicationDrawable(
                context,
                appearance.loginBackground,
                R.drawable.file_00000000e0e4820a839fa3184993f41b
            )
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(Modifier.fillMaxSize().background(Color(0x55000000)))
}

@Composable
private fun ApplicationBrandTitle(color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            "mObywatelska 2.0",
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
        Text(
            "aplikacja",
            color = color.copy(alpha = 0.92f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EntryFooter(foreground: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            "Więcej o Krajowym Planie\nOdbudowy",
            modifier = Modifier.weight(1f),
            color = foreground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "wersja\n4.93.1\n(6704)",
            color = foreground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EntryWelcomeScreen(onStart: () -> Unit, onSkip: () -> Unit) {
    val foreground = Color(0xFFFFF1D1)
    Box(Modifier.fillMaxSize()) {
        EntryBackground()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BrandMark(size = ENTRY_BRAND_MARK_SIZE)
                ApplicationBrandTitle(foreground)
            }
            Spacer(Modifier.height(34.dp))
            Text(
                "Dzień dobry!",
                color = foreground,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Witaj w aplikacji mObywatelska 2.0",
                color = foreground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text("Rozpocznij", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(18.dp))
            EntryFooter(foreground)
        }
    }
}

@Composable
private fun EntryLoginScreen(onDemo: () -> Unit, onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val cream = Color(0xFFFFF1D1)
    val skyBlue = Color(0xFF9EDCFF)
    Box(Modifier.fillMaxSize()) {
        EntryBackground()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BrandMark(size = ENTRY_BRAND_MARK_SIZE)
                ApplicationBrandTitle(cream)
            }
            Spacer(Modifier.height(38.dp))
            Text(
                "Wpisz hasło",
                color = cream,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Zaloguj się do aplikacji",
                color = cream,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(42.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x665B555C))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Hasło", color = cream, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło",
                                tint = cream
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = cream,
                        focusedBorderColor = cream,
                        unfocusedBorderColor = cream,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Text(
                    "Nie pamiętasz hasła?",
                    color = skyBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onDemo,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text("Przejdź do aplikacji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Wróć")
            }
            Spacer(Modifier.height(18.dp))
            EntryFooter(cream)
        }
    }
}
