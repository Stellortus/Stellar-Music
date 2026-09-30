package top.stellortus.stellarmusic

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellarmusic.data.AuthPreferences
import top.stellortus.stellarmusic.network.ApiException
import top.stellortus.stellarmusic.network.AuthApi
import top.stellortus.stellarmusic.ui.BottomTab
import top.stellortus.stellarmusic.ui.BottomTab.AccountTab
import top.stellortus.stellarmusic.ui.BottomTab.HomeTab
import top.stellortus.stellarmusic.ui.BottomTab.PlayListTab
import top.stellortus.stellarmusic.ui.BottomTab.UploadTab
import top.stellortus.stellarmusic.ui.screens.account.AccountScreen
import top.stellortus.stellarmusic.ui.screens.HomeScreen
import top.stellortus.stellarmusic.ui.screens.PlayListScreen
import top.stellortus.stellarmusic.ui.screens.account.ProfileScreen
import top.stellortus.stellarmusic.ui.screens.UploadScreen
import top.stellortus.stellarmusic.ui.theme.StellarMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 提前初始化登录态存储，供网络层 defaultRequest 读取 token。
        AuthPreferences.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            StellarMusicTheme {
                MusicApp()
            }
        }
    }
}

private sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
    data object NetworkError : AuthState
}

@Composable
private fun MusicApp() {
    val context = LocalContext.current
    var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = { selectedItem = item },
                        icon = {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = "${item.name} icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
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
                HomeTab -> HomeScreen()
                PlayListTab -> PlayListScreen()
                UploadTab -> UploadScreen()
                AccountTab -> ProfileScreen(onLogout)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text("正在登录…", modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun NetworkErrorScreen(onRetry: () -> Unit, onGoToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("无法连接服务器，请检查网络后重试", style = MaterialTheme.typography.bodyLarge)
        Row {
            OutlinedButton(onClick = onGoToLogin) { Text("进入登录页") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onRetry) { Text("重试") }
        }
    }
}
