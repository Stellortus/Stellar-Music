package top.stellortus.stellarmusic.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.stellortus.stellarmusic.data.NameKey.UserName
import top.stellortus.stellarmusic.data.PreferencesName.UserInfo
import top.stellortus.stellarmusic.data.preferences
import top.stellortus.stellarmusic.data.rememberPreference

@Composable
fun AccountScreen() {
    val preference = preferences(UserInfo)
    var username by rememberPreference(preference, UserName) { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("用户名") },
            placeholder = { Text("请输入用户名") },
            singleLine = true
        )
    }
}
