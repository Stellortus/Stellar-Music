package top.stellortus.stellarmusic.ui.screens.account

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import top.stellortus.stellarmusic.data.AuthPreferences
import top.stellortus.stellarmusic.network.AuthApi
import top.stellortus.stellarmusic.network.AuthResponse
import top.stellortus.stellarmusic.network.toUserMessage

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"

@Composable
fun AccountScreen(onLoggedIn: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = Modifier.fillMaxSize(),
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
}

@Composable
private fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("登录", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("用户名") },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("密码") },
            singleLine = true,
            enabled = !loading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                when {
                    username.isBlank() || password.isBlank() -> error = "请输入用户名和密码"
                    else -> scope.launch {
                        loading = true
                        error = null
                        runCatching { AuthApi.login(username.trim(), password) }
                            .onSuccess {
                                saveAuth(context, it)
                                onLoggedIn()
                            }
                            .onFailure { e -> error = e.toUserMessage() }
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(if (loading) "登录中…" else "登录")
        }
        TextButton(onClick = onNavigateToRegister, enabled = !loading) {
            Text("没有账号？去注册")
        }
    }
}

@Composable
private fun RegisterScreen(
    onLoggedIn: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("注册", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("用户名") },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("密码") },
            singleLine = true,
            enabled = !loading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; error = null },
            label = { Text("确认密码") },
            singleLine = true,
            enabled = !loading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                when {
                    username.isBlank() -> error = "请输入用户名"
                    password.isBlank() -> error = "请输入密码"
                    confirmPassword != password -> error = "两次输入的密码不一致"
                    else -> scope.launch {
                        loading = true
                        error = null
                        runCatching { AuthApi.register(username.trim(), password) }
                            .onSuccess {
                                saveAuth(context, it)
                                onLoggedIn()
                            }
                            .onFailure { e -> error = e.toUserMessage() }
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(if (loading) "注册中…" else "注册")
        }
        TextButton(onClick = onNavigateToLogin, enabled = !loading) {
            Text("已有账号？去登录")
        }
    }
}

/** 登录/注册成功后保存登录态，并由上层回调跳转主页。 */
private fun saveAuth(context: Context, auth: AuthResponse) {
    AuthPreferences.getInstance(context).apply {
        token = auth.token
        userId = auth.user.id
        username = auth.user.username
    }
}
