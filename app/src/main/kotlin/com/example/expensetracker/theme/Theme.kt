package com.example.expensetracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLime, 
    onPrimary = Color.Black,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = AccentMint,
    onSecondary = Color.Black,
    tertiary = PrimaryIndigo,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLime, // Keep lime as the signature brand color
    onPrimary = Color.Black,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = PrimaryIndigo,
    onSecondary = Color.White,
    tertiary = AccentMint,
    onTertiary = Color.Black,
    background = BackgroundLight, 
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight, // This will fix the invisible unselected bars
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Use dark by default if system is, or default to dark
    content: @Composable () -> Unit
) {
    // Custom dark-first implementation
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
