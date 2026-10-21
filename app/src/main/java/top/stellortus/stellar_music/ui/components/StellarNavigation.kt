package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import top.stellortus.stellar_music.ui.BottomTab
import top.stellortus.stellar_music.ui.theme.StellarTheme

@Composable
fun StellarScaffold(modifier: Modifier = Modifier, bottomBar: @Composable () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(modifier, bottomBar = bottomBar, containerColor = StellarTheme.colors.pageBackground, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StellarNavigationBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        NavigationBar(containerColor = StellarTheme.colors.pageBackground) {
            BottomTab.entries.forEach { item ->
                NavigationBarItem(
                    selected = selected == item, onClick = { onSelect(item) },
                    icon = {
                        StellarIcon(painterResource(item.iconRes), item.displayName,
                            tone = if (selected == item) StellarTone.Accent else StellarTone.Secondary)
                    },
                    label = { StellarText(item.displayName, role = StellarTextRole.Caption) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = StellarTheme.colors.selectedBackground,
                        selectedTextColor = StellarTheme.colors.textPrimary,
                        unselectedTextColor = StellarTheme.colors.textSecondary)
                )
            }
        }
    }
}
