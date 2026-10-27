package top.stellortus.stellar_music.ui

import androidx.compose.runtime.staticCompositionLocalOf
import top.stellortus.stellar_music.data.AuthPreferences

val LocalAuthPreferences = staticCompositionLocalOf<AuthPreferences> {
    error("AuthPreferences is not provided. Add a CompositionLocalProvider at the Compose root.")
}
