package top.stellortus.stellarmusic.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand colors. Keep feature UI colors here instead of hard-coding them in composables.
val StellarBlue = Color(0xFF315CFF)
val StellarBlueDark = Color(0xFFB8C4FF)
val StellarPurple = Color(0xFF6750A4)
val StellarPurpleDark = Color(0xFFD0BCFF)
val StellarPink = Color(0xFFB3261E)
val StellarPinkDark = Color(0xFFFFB4AB)

val LightColorScheme = lightColorScheme(
    primary = StellarBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE2FF),
    onPrimaryContainer = Color(0xFF00174D),
    inversePrimary = StellarBlueDark,
    secondary = Color(0xFF595D72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDEE1F9),
    onSecondaryContainer = Color(0xFF161A2C),
    tertiary = StellarPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF21005D),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1A1B20),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE2E2EC),
    onSurfaceVariant = Color(0xFF45464F),
    surfaceTint = StellarBlue,
    inverseSurface = Color(0xFF2F3036),
    inverseOnSurface = Color(0xFFF1F0F7),
    error = StellarPink,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF757780),
    outlineVariant = Color(0xFFC5C6D0)
)

val DarkColorScheme = darkColorScheme(
    primary = StellarBlueDark,
    onPrimary = Color(0xFF002A78),
    primaryContainer = Color(0xFF1645C5),
    onPrimaryContainer = Color(0xFFDCE2FF),
    inversePrimary = StellarBlue,
    secondary = Color(0xFFC2C5DD),
    onSecondary = Color(0xFF2B2F42),
    secondaryContainer = Color(0xFF424659),
    onSecondaryContainer = Color(0xFFDEE1F9),
    tertiary = StellarPurpleDark,
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE3E2E9),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE3E2E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceTint = StellarBlueDark,
    inverseSurface = Color(0xFFE3E2E9),
    inverseOnSurface = Color(0xFF2F3036),
    error = StellarPinkDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8F9099),
    outlineVariant = Color(0xFF45464F)
)
