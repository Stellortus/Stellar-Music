package top.stellortus.stellar_music.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

val StellarTheme.dialogTitleStyle: TextStyle
    @Composable get() = typography.title.copy(
        brush = colors.accentGradient
    )
