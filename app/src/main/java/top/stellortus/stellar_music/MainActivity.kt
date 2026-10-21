package top.stellortus.stellar_music

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellar_music.data.AuthPreferences
import top.stellortus.stellar_music.network.ApiException
import top.stellortus.stellar_music.network.AuthApi
import top.stellortus.stellar_music.ui.BottomTab.AccountTab
import top.stellortus.stellar_music.ui.BottomTab.HomeTab
import top.stellortus.stellar_music.ui.BottomTab.PlayListTab
import top.stellortus.stellar_music.ui.BottomTab.UploadTab
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music.ui.player.PlayerBar
import top.stellortus.stellar_music.ui.screens.HomeScreen
import top.stellortus.stellar_music.ui.screens.PlayListScreen
import top.stellortus.stellar_music.ui.screens.UploadScreen
import top.stellortus.stellar_music.ui.screens.account.AccountScreen
import top.stellortus.stellar_music.ui.screens.account.ProfileScreen
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthPreferences.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            StellarMusicTheme {
                MusicApp()
            }
        }
    }
}

private enum class AuthState {
    Loading,
    Authenticated,
    Unauthenticated,
    NetworkError
}

@Composable
private fun MusicApp() {
    val context = LocalContext.current
    var authState by remember { mutableStateOf(AuthState.Loading) }
    var verifyAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(verifyAttempt) {
        authState = AuthState.Loading
        val prefs = AuthPreferences.getInstance(context)
        if (!prefs.hasToken) {
            authState = AuthState.Unauthenticated
            return@LaunchedEffect
        }
        runCatching { AuthApi.me() }
            .onSuccess { me ->
                prefs.username = me.username
                prefs.userId = me.id
                authState = AuthState.Authenticated
            }
            .onFailure { e ->
                top.stellortus.stellar_music.ui.ToastManager.makeText(context,
                    if (e is ApiException && e.status == 401) "登录已失效，请重新登录" else e.message ?: "无法连接服务器")
                authState = if (e is ApiException && e.status == 401) {
                    prefs.clear()
                    AuthState.Unauthenticated
                } else {
                    AuthState.NetworkError
                }
            }
    }

    when (authState) {
        AuthState.Loading -> LoadingScreen()
        AuthState.Unauthenticated -> AccountScreen(
            onLoggedIn = { authState = AuthState.Authenticated }
        )
        AuthState.Authenticated -> MainScreen(
            onLogout = { authState = AuthState.Unauthenticated }
        )
        AuthState.NetworkError -> NetworkErrorScreen(
            onRetry = { verifyAttempt++ },
            onGoToLogin = { authState = AuthState.Unauthenticated },
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun MainScreen(onLogout: () -> Unit) {
    var selectedItem by remember { mutableStateOf(HomeTab) }
    val context = LocalContext.current
    val playerState = remember { MusicPlayerState(context) }
    LaunchedEffect(playerState.playbackError) {
        playerState.playbackError?.let {
            top.stellortus.stellar_music.ui.ToastManager.makeText(context, it)
            playerState.clearPlaybackError()
        }
    }
    DisposableEffect(playerState) {
        onDispose { playerState.release() }
    }

    StellarScaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                PlayerBar(playerState)
                StellarNavigationBar(selectedItem) { selectedItem = it }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (selectedItem) {
                HomeTab -> HomeScreen(playerState)
                PlayListTab -> PlayListScreen(playerState)
                UploadTab -> UploadScreen()
                AccountTab -> ProfileScreen(onLogout)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    StellarLoadingState("正在登录…", Modifier.safeDrawingPadding())
}

@Composable
private fun NetworkErrorScreen(onRetry: () -> Unit, onGoToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(StellarTheme.spacing.spacious),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row {
            StellarButton(kind = StellarButtonKind.Outlined, onClick = onGoToLogin) { StellarText("进入登录页") }
            Spacer(modifier = Modifier.width(StellarTheme.spacing.section))
            StellarButton(kind = StellarButtonKind.Primary, onClick = onRetry) { StellarText("重试") }
        }
    }
}
