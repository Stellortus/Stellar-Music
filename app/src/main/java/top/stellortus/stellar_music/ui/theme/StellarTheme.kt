package top.stellortus.stellar_music.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush

/** The only theme entry point consumed by features and shared components. */
object StellarTheme {
    val colors: StellarColors @Composable get() = StellarColors(MaterialTheme.colorScheme)
    val shapes: StellarSemanticShapes @Composable get() = StellarSemanticShapes(MaterialTheme.shapes)
    val typography: StellarTextStyles @Composable get() = StellarTextStyles(MaterialTheme.typography)
    val spacing: StellarSpacing @Composable get() = LocalStellarSpacing.current
    val sizes: StellarSizes @Composable get() = LocalStellarSizes.current
    val motion: StellarMotion @Composable get() = LocalStellarMotion.current
}

@Immutable
class StellarColors internal constructor(scheme: ColorScheme) {
    val pageBackground = scheme.background
    val cardBackground = scheme.surfaceContainerLow
    val overlayBackground = scheme.surfaceContainerLow
    val insetBackground = scheme.surfaceContainer
    val playerBackground = scheme.surfaceContainerHigh
    val itemBackground = scheme.surface
    val textPrimary = scheme.onSurface
    val textSecondary = scheme.onSurfaceVariant
    val accent = scheme.primary
    val primaryAction = scheme.primary
    val onPrimaryAction = scheme.onPrimary
    val secondaryAction = scheme.secondaryContainer
    val onSecondaryAction = scheme.onSecondaryContainer
    val selectedBackground = scheme.secondaryContainer
    val onSelectedBackground = scheme.onSecondaryContainer
    val dangerBackground = scheme.errorContainer
    val onDangerBackground = scheme.onErrorContainer
    val dangerForeground = scheme.error
    val divider = scheme.outlineVariant
    val outline = scheme.outline
    val disabledBackground = scheme.onSurface.copy(alpha = 0.12f)
    val disabledContent = scheme.onSurface.copy(alpha = 0.38f)
    val progressTrack = scheme.secondary
    val coverForeground = scheme.onPrimaryContainer
    val overlayScrim = scheme.scrim.copy(alpha = 0.32f)
    val accentGradient = Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))
    val coverGradient = Brush.linearGradient(listOf(scheme.primaryContainer, scheme.tertiaryContainer))
}

@Immutable
class StellarSemanticShapes internal constructor(shapes: Shapes) {
    val card = shapes.extraLarge
    val dialog = shapes.extraLarge
    val menu = shapes.extraLarge
    val sheet = shapes.extraLarge
    val button = shapes.large
    val textField = shapes.medium
    val cover = shapes.large
    val item = shapes.large
    val avatar = CircleShape
}

@Immutable
class StellarTextStyles internal constructor(type: Typography) {
    val title = type.titleLarge
    val itemTitle = type.titleMedium
    val body = type.bodyLarge
    val bodySmall = type.bodyMedium
    val action = type.labelLarge
    val caption = type.labelMedium
    val micro = type.labelSmall
    val avatar = type.headlineLarge
}
