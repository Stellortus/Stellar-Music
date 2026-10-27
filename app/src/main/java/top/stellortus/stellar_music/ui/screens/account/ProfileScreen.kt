package top.stellortus.stellar_music.ui.screens.account

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.stellortus.stellar_music.ui.ToastManager
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.ui.LocalAuthPreferences
import top.stellortus.stellar_music.network.AuthApi
import top.stellortus.stellar_music.utils.extensions.toUserMessage

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LocalAuthPreferences.current
    val username = prefs.username.orEmpty()
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = StellarTheme.spacing.page).padding(vertical = StellarTheme.spacing.page),
            verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.section)
        ) {
            ProfileIdentity(username, prefs.userId)

            StellarButton(kind = StellarButtonKind.Primary, loading = loading,
                onClick = {
                    scope.launch {
                        loading = true

                        runCatching { AuthApi.logout() }
                            .onSuccess {
                                prefs.clear()
                                onLogout()
                            }
                            .onFailure { e -> ToastManager.makeText(context, e.toUserMessage()) }
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StellarText(if (loading) "退出中…" else "退出登录")
            }
            VersionFooter(Modifier.safeDrawingPadding())
        }
    }
}
