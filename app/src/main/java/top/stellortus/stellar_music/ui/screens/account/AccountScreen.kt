package top.stellortus.stellar_music.ui.screens.account

import top.stellortus.stellar_music.ui.widgets.FormCard
import top.stellortus.stellar_music.ui.widgets.StellarButton
import top.stellortus.stellar_music.ui.widgets.StellarButtonKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextField
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone

import top.stellortus.stellar_music.ui.theme.StellarTheme

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.stellortus.stellar_music.utils.ToastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import top.stellortus.stellar_music_common.dto.AuthResponse
import top.stellortus.stellar_music.data.AuthPreferences
import top.stellortus.stellar_music.db.manager.TrackManager
import top.stellortus.stellar_music.db.manager.PlaylistManager
import top.stellortus.stellar_music.ui.LocalAuthPreferences
import top.stellortus.stellar_music.network.AuthApi
import top.stellortus.stellar_music.utils.extensions.toUserMessage

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"

@Composable
fun AccountScreen(onLoggedIn: () -> Unit) {
    val navController = rememberNavController()
    // Consume system and IME insets once for both the form and footer.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_LOGIN,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = Modifier.weight(1f),
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onLoggedIn = onLoggedIn,
                    onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) },
                )
            }
            composable(ROUTE_REGISTER) {
                RegisterScreen(
                    onLoggedIn = onLoggedIn,
                    onNavigateToLogin = { navController.popBackStack() },
                )
            }
        }
        VersionFooter()
    }
}

/** 账号页底部共用的版本号，从 PackageManager 读取（构建未开启 BuildConfig 生成）。 */
@Composable
internal fun VersionFooter(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            info.versionName
        }.getOrNull() ?: "未知"
    }
    StellarText(
        text = "当前版本 v$versionName",
        role = StellarTextRole.Caption,
        tone = StellarTone.Secondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = StellarTheme.spacing.itemGap),
    )
}

@Composable
private fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val preferences = LocalAuthPreferences.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),

    ) {
        FormCard(Modifier.padding(horizontal = StellarTheme.spacing.page).padding(vertical = StellarTheme.spacing.page)) {
            StellarTextField(
                value = username,
                onValueChange = { username = it },
                label = "用户名",
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            )
            StellarTextField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                singleLine = true,
                enabled = !loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            StellarButton(kind = StellarButtonKind.Primary, loading = loading,
                onClick = {
                    when {
                        username.isBlank() || password.isBlank() -> ToastManager.makeText("请输入用户名和密码")
                        else -> scope.launch {
                            loading = true

                            runCatching { saveAuth(preferences, AuthApi.login(username.trim(), password)) }
                                .onSuccess {
                                    onLoggedIn()
                                }
                                .onFailure { e ->
                                    if (e is CancellationException) throw e
                                    ToastManager.makeText(e.toUserMessage())
                                }
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StellarText(if (loading) "登录中…" else "登录")
            }
            StellarButton(kind = StellarButtonKind.Text, onClick = onNavigateToRegister, enabled = !loading) {
                StellarText("没有账号？去注册")
            }
        }
    }
}

@Composable
private fun RegisterScreen(
    onLoggedIn: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val preferences = LocalAuthPreferences.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),

    ) {
        FormCard(Modifier.padding(horizontal = StellarTheme.spacing.page).padding(vertical = StellarTheme.spacing.page)) {
            StellarTextField(
                value = username,
                onValueChange = { username = it },
                label = "用户名",
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            )
            StellarTextField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                singleLine = true,
                enabled = !loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            StellarTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "确认密码",
                singleLine = true,
                enabled = !loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            StellarButton(kind = StellarButtonKind.Primary, loading = loading,
                onClick = {
                    when {
                        username.isBlank() -> ToastManager.makeText("请输入用户名")
                        password.isBlank() -> ToastManager.makeText("请输入密码")
                        confirmPassword != password -> ToastManager.makeText("两次输入的密码不一致")
                        else -> scope.launch {
                            loading = true

                            runCatching { saveAuth(preferences, AuthApi.register(username.trim(), password)) }
                                .onSuccess {
                                    onLoggedIn()
                                }
                                .onFailure { e ->
                                    if (e is CancellationException) throw e
                                    ToastManager.makeText(e.toUserMessage())
                                }
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StellarText(if (loading) "注册中…" else "注册")
            }
            StellarButton(kind = StellarButtonKind.Text, onClick = onNavigateToLogin, enabled = !loading) {
                StellarText("已有账号？去登录")
            }
        }
    }
}

/** 登录/注册成功后保存登录态，并由上层回调跳转主页。 */
private suspend fun saveAuth(preferences: AuthPreferences, auth: AuthResponse) {
    preferences.clearSession()
    preferences.apply {
        token = auth.token
        userId = auth.user.id
        username = auth.user.username
    }
    TrackManager.startSession()
    PlaylistManager.startSession()
}
