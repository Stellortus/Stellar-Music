package top.stellortus.stellarmusic.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.stellortus.stellarmusic.data.NameKey.UserName
import top.stellortus.stellarmusic.data.PreferencesName.UserInfo
import top.stellortus.stellarmusic.data.preferences
import top.stellortus.stellarmusic.network.NetworkService
import java.io.File

private sealed interface UploadState {
    data object Idle : UploadState
    data object Uploading : UploadState
    data class Success(val fileName: String) : UploadState
    data class Error(val message: String) : UploadState
}

private sealed interface DeleteState {
    data object Idle : DeleteState
    data object Deleting : DeleteState
    data class Success(val id: Int) : DeleteState
    data class Error(val message: String) : DeleteState
}

@Composable
fun UploadScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UploadState>(UploadState.Idle) }
    var musicId by remember { mutableStateOf("") }
    var deleteState by remember { mutableStateOf<DeleteState>(DeleteState.Idle) }
    val userPreference = preferences(UserInfo)
    val userName = userPreference.read(UserName)

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            state = UploadState.Uploading
            val result = runCatching {
                val file = withContext(Dispatchers.IO) {
                    uri.copyToCache(context)
                }
                NetworkService.upload(file, userName)
                file.delete()
                file.name
            }
            state = result.fold(
                onSuccess = { UploadState.Success(it) },
                onFailure = { UploadState.Error(it.message ?: "上传失败") }
            )
        }
    }
    if (userName.isNullOrEmpty()) {
        Text("请先设置用户名！")
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "上传音乐",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "选择一个音乐文件上传到服务器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { filePicker.launch("audio/*") },
                enabled = state !is UploadState.Uploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state is UploadState.Uploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(if (state is UploadState.Uploading) "上传中…" else "选择音乐文件")
            }
            when (val currentState = state) {
                UploadState.Idle -> Unit
                UploadState.Uploading -> Unit
                is UploadState.Success -> Text(
                    text = "上传成功：${currentState.fileName}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )

                is UploadState.Error -> Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "删除音乐",
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = musicId,
                onValueChange = { value ->
                    if (value.all(Char::isDigit)) musicId = value
                },
                label = { Text("音乐 ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = deleteState !is DeleteState.Deleting,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val id = musicId.toIntOrNull() ?: return@Button
                    scope.launch {
                        deleteState = DeleteState.Deleting
                        deleteState = runCatching {
                            val response = NetworkService.delete(id)
                            check(response.status.value in 200..299) {
                                "删除失败：HTTP ${response.status.value}"
                            }
                            id
                        }.fold(
                            onSuccess = {
                                musicId = ""
                                DeleteState.Success(it)
                            },
                            onFailure = { DeleteState.Error(it.message ?: "删除失败") }
                        )
                    }
                },
                enabled = musicId.toIntOrNull() != null && deleteState !is DeleteState.Deleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (deleteState is DeleteState.Deleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(if (deleteState is DeleteState.Deleting) "删除中…" else "删除")
            }
            when (val currentState = deleteState) {
                DeleteState.Idle -> Unit
                DeleteState.Deleting -> Unit
                is DeleteState.Success -> Text(
                    text = "删除成功：ID ${currentState.id}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )

                is DeleteState.Error -> Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

    }
}

private fun Uri.copyToCache(context: Context): File {
    val originalName = context.contentResolver.query(
        this,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    val safeName = (originalName?.takeIf {
        it.matches(Regex("^[^\\p{P}][\\s\\S]*\$"))
    } ?: "music-${System.currentTimeMillis()}.mp3")
    val target = File(context.cacheDir, safeName)
    context.contentResolver.openInputStream(this).use { input ->
        requireNotNull(input) { "无法读取所选文件" }
        target.outputStream().use { output -> input.copyTo(output) }
    }
    return target
}
