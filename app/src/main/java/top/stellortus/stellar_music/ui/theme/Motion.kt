package top.stellortus.stellar_music.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
class StellarMotion {
    val coverRotationDurationMillis = 12000
    val titleScrollSpeed = 30.dp
    val titleScrollPauseMillis = 1000L
}

val LocalStellarMotion = staticCompositionLocalOf { StellarMotion() }
