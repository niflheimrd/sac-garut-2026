package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = OceanDeep,
  onPrimary = Color.White,
  primaryContainer = OceanBlue,
  onPrimaryContainer = Color.White,
  secondary = OceanBlue,
  onSecondary = Color.White,
  secondaryContainer = OceanSurfaceVariant,
  onSecondaryContainer = OceanDeep,
  tertiary = OceanAzure,
  onTertiary = Color.White,
  tertiaryContainer = OceanIce,
  onTertiaryContainer = OceanDeep,
  background = BackgroundLight,
  onBackground = TextDarkPrimary,
  surface = SurfaceWhite,
  onSurface = TextDarkPrimary,
  surfaceVariant = OceanIce,
  onSurfaceVariant = TextDarkSecondary,
  outline = Color(0xFFCAD6E2),
  outlineVariant = Color(0xFFE2EAF1),
)

private val DarkColorScheme = darkColorScheme(
  primary = OceanCyan,
  onPrimary = OceanDeep,
  primaryContainer = OceanBlue,
  onPrimaryContainer = Color.White,
  secondary = OceanAzure,
  onSecondary = Color.White,
  secondaryContainer = OceanDeep,
  onSecondaryContainer = OceanIce,
  tertiary = OceanCyan,
  onTertiary = OceanDeep,
  background = Color(0xFF061425),
  onBackground = Color(0xFFF0F4F8),
  surface = Color(0xFF0B213B),
  onSurface = Color(0xFFF0F4F8),
  surfaceVariant = Color(0xFF132F4C),
  onSurfaceVariant = Color(0xFFB0C4DE),
  outline = Color(0xFF284869),
  outlineVariant = Color(0xFF1B3654),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep brand fidelity with ocean-blue palette by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
