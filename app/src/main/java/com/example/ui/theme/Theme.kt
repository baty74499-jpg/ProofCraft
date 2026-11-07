package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CopperLight,
  onPrimary = Color(0xFF381400),
  primaryContainer = CopperDark,
  onPrimaryContainer = Color(0xFFFFDBCF),
  secondary = AmberAccent,
  onSecondary = Color(0xFF3F2E00),
  secondaryContainer = Color(0xFF5B4300),
  onSecondaryContainer = Color(0xFFFFE08A),
  tertiary = WaterAqua,
  onTertiary = Color(0xFF003548),
  background = CharcoalBackground,
  onBackground = TextPrimaryDark,
  surface = CharcoalSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = CharcoalSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  outline = CharcoalBorder
)

private val LightColorScheme = lightColorScheme(
  primary = CopperPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFDBCF),
  onPrimaryContainer = CopperDark,
  secondary = AmberAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFE08A),
  onSecondaryContainer = Color(0xFF281C00),
  tertiary = WaterAquaDark,
  onTertiary = Color.White,
  background = CreamBackground,
  onBackground = TextPrimaryLight,
  surface = CreamSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = CreamSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  outline = CreamBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep artisan copper theme consistent
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
