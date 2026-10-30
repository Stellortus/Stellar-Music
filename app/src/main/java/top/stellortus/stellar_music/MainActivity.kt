package top.stellortus.stellar_music

import top.stellortus.stellar_music.ui.widgets.StellarButton
import top.stellortus.stellar_music.ui.widgets.StellarButtonKind
import top.stellortus.stellar_music.ui.widgets.StellarLoadingState
import top.stellortus.stellar_music.ui.widgets.StellarScaffold
import top.stellortus.stellar_music.ui.widgets.StellarText

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CancellationException
import top.stellortus.stellar_music.data.AuthPreferences
import top.stellortus.stellar_music.db.manager.TrackManager
import top.stellortus.stellar_music.db.manager.PlaylistManager
import top.stellortus.stellar_music.ui.LocalAuthPreferences
import top.stellortus.stellar_music.network.ApiException
import top.stellortus.stellar_music.network.AuthApi
import top.stellortus.stellar_music.ui.BottomTab.AccountTab
import top.stellortus.stellar_music.ui.BottomTab.HomeTab
import top.stellortus.stellar_music.ui.BottomTab.PlayListTab
import top.stellortus.stellar_music.ui.BottomTab.UploadTab
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music.ui.player.PlayerBar
import top.stellortus.stellar_music.ui.player.PlaybackQueueHost
import top.stellortus.stellar_music.ui.screens.HomeScreen
import top.stellortus.stellar_music.ui.screens.PlaylistScreen
import top.stellortus.stellar_music.ui.screens.UploadScreen
import top.stellortus.stellar_music.ui.screens.account.AccountScreen
import top.stellortus.stellar_music.ui.screens.account.ProfileScreen
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.utils.ToastManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastManager.init(applicationContext)
        AuthPreferences.init(applicationContext)
        TrackManager.init(applicationContext)
        PlaylistManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalAuthPreferences provides AuthPreferences) {
                StellarMusicTheme {
                    MusicApp()
                }
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
    val prefs = LocalAuthPreferences.current
    var authState by remember {
        mutableStateOf(
            if (prefs.hasToken && prefs.userId != null && !prefs.sessionExpired.value) AuthState.Authenticated
            else AuthState.Loading
        )
    }
    var verifyAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(verifyAttempt) {
        if (!prefs.hasToken) {
            prefs.clearSession()
            authState = AuthState.Unauthenticated
            return@LaunchedEffect
        }
        if (prefs.userId == null) authState = AuthState.Loading
        val verifyingToken = prefs.token
        try {
            val me = AuthApi.me()
            if (prefs.token != verifyingToken || prefs.sessionExpired.value) return@LaunchedEffect
            if (prefs.userId != null && prefs.userId != me.id) {
                authState = AuthState.Loading
                TrackManager.clear()
                PlaylistManager.clear()
            }
            if (prefs.token == verifyingToken && !prefs.sessionExpired.value) {
                prefs.username = me.username
                prefs.userId = me.id
                TrackManager.startSession()
                PlaylistManager.startSession()
                authState = AuthState.Authenticated
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (prefs.token != verifyingToken) return@LaunchedEffect
            ToastManager.makeText(
                if (error is ApiException && error.status == 401) "登录已失效，请重新登录"
                else error.message ?: "无法连接服务器"
            )
            if (error is ApiException && error.status == 401) {
                prefs.sessionExpired.value = true
            } else if (!prefs.sessionExpired.value) {
                authState = if (prefs.hasToken && prefs.userId != null) AuthState.Authenticated else AuthState.NetworkError
            }
        }
    }

    LaunchedEffect(Unit) {
        prefs.sessionExpired.collect { expired ->
            if (expired) {
                authState = AuthState.Loading
                prefs.clearSession()
                authState = AuthState.Unauthenticated
            }
        }
    }

    when (authState) {
        AuthState.Loading -> LoadingScreen()
        AuthState.Unauthenticated -> AccountScreen(
            onLoggedIn = { authState = AuthState.Authenticated; verifyAttempt++ }
        )
        AuthState.Authenticated -> MainScreen(
            onLogout = { prefs.sessionExpired.value = true }
        )
        AuthState.NetworkError -> NetworkErrorScreen(
            onRetry = { verifyAttempt++ },
            onGoToLogin = { prefs.sessionExpired.value = true },
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun MainScreen(onLogout: () -> Unit) {
    AppUpdatePrompt()
    var selectedItem by remember { mutableStateOf(HomeTab) }
    val context = LocalContext.current
    val playerState = remember { MusicPlayerState(context) }
    var queueVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(playerState.playbackError) {
        playerState.playbackError?.let {
            ToastManager.makeText(it)
            playerState.clearPlaybackError()
        }
    }
    DisposableEffect(playerState) {
        onDispose { playerState.release() }
    }

    PlaybackQueueHost(
        expanded = queueVisible,
        tracks = playerState.queueTracks,
        currentIndex = playerState.currentQueueIndex,
        onSelect = playerState::playQueueIndex,
        onDismiss = { queueVisible = false },
    ) { playerAnchorModifier ->
        StellarScaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column {
                    PlayerBar(playerState, onOpenQueue = { queueVisible = true },
                        queueAnchorModifier = playerAnchorModifier)
                    StellarNavigationBar(selectedItem) { queueVisible = false; selectedItem = it }
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
                    PlayListTab -> PlaylistScreen(playerState)
                    UploadTab -> UploadScreen()
                    AccountTab -> ProfileScreen(onLogout)
                }
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
