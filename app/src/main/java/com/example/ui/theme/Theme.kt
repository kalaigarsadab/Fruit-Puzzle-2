package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MagicLightColorScheme = lightColorScheme(
    primary = MagicPurple,
    onPrimary = Color.White,
    primaryContainer = MagicPurpleSoft,
    onPrimaryContainer = MagicPurpleDark,
    secondary = BerryPink,
    onSecondary = Color.White,
    secondaryContainer = BerryPinkSoft,
    onSecondaryContainer = BerryPinkDark,
    tertiary = MintGreen,
    onTertiary = Color.White,
    tertiaryContainer = MintGreenSoft,
    onTertiaryContainer = MintGreenDark,
    background = CreamBackground,
    onBackground = DeepCocoa,
    surface = CreamSurface,
    onSurface = DeepCocoa,
    surfaceVariant = CreamWarmCard,
    onSurfaceVariant = SoftCocoa,
    outline = CreamBorder,
    error = AppleRed,
    onError = Color.White
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp)
)

@Composable
fun MagicSortingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MagicLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
