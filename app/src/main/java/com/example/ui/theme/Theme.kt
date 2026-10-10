package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = EditorialBlueContainer,
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = EditorialPurpleContainer,
    tertiary = Color(0xFFFFB77C),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = EditorialAmberContainer,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = CardSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = EditorialBlueContainer,
    onPrimaryContainer = EditorialOnBlueContainer,
    secondary = EditorialPurple,
    onSecondary = Color.White,
    secondaryContainer = EditorialPurpleContainer,
    onSecondaryContainer = EditorialOnPurpleContainer,
    tertiary = EditorialAmber,
    onTertiary = Color.White,
    tertiaryContainer = EditorialAmberContainer,
    onTertiaryContainer = EditorialOnAmberContainer,
    background = EditorialBackground,
    onBackground = EditorialTextDark,
    surface = EditorialCardWhite,
    onSurface = EditorialTextDark,
    surfaceVariant = EditorialSurfaceVariant,
    onSurfaceVariant = EditorialMuted,
    outline = EditorialOutline,
    error = EditorialAlertRed,
    errorContainer = EditorialAlertRedContainer,
    onError = Color.White
)

@Composable
fun TokoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
