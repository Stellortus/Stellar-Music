package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.ui.theme.StellarTheme

@Composable
fun StellarLoadingState(message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(StellarTheme.spacing.page),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        StellarLoadingIndicator()
        StellarText(message, Modifier.padding(top = StellarTheme.spacing.section), tone = StellarTone.Secondary)
    }
}

@Composable
fun StellarEmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(StellarTheme.spacing.section), contentAlignment = Alignment.Center) {
        StellarText(message, tone = StellarTone.Secondary)
    }
}

@Composable
fun StellarRetryAction(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    StellarButton(onRetry, modifier, kind = StellarButtonKind.Outlined) { StellarText("重试") }
}
