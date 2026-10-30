package top.stellortus.stellar_music.ui.widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.ui.theme.StellarTheme

@Composable
fun StellarScaffold(modifier: Modifier = Modifier, bottomBar: @Composable () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(modifier, bottomBar = bottomBar, containerColor = StellarTheme.colors.pageBackground, content = content)
}
