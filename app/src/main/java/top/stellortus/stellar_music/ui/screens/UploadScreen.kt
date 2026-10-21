package top.stellortus.stellar_music.ui.screens

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music.utils.extensions.toUserMessage
import java.io.File

private sealed interface UploadState {
    data object Idle : UploadState
    data object Uploading : UploadState
    data class Success(val fileName: String) : UploadState
    data class Error(val message: String) : UploadState
}

@Composable
fun UploadScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UploadState>(UploadState.Idle) }

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
                try {
                    // 非 2xx（如 401）会抛出 ApiException，不再误报成功。
                    NetworkService.upload(file)
                    file.name
                } finally {
                    // 无论成功失败都清掉缓存中的临时文件。
                    file.delete()
                }
            }
            state = result.fold(
                onSuccess = { UploadState.Success(it) },
                onFailure = { top.stellortus.stellar_music.ui.ToastManager.makeText(context, it.toUserMessage()); UploadState.Error(it.toUserMessage()) }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        FormCard(
            Modifier.padding(horizontal = StellarTheme.spacing.page).padding(vertical = StellarTheme.spacing.page)
        ) {
            CoverPlaceholder(CoverPlaceholderSize.Large)
            StellarText(
                text = "选择一个音乐文件上传到服务器",
                role = StellarTextRole.BodySmall,
                tone = StellarTone.Secondary
            )
            StellarButton(kind = StellarButtonKind.Primary, loading = state is UploadState.Uploading,
                onClick = { filePicker.launch("audio/*") },
                enabled = state !is UploadState.Uploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                StellarText(if (state is UploadState.Uploading) "上传中…" else "选择音乐文件")
            }
            when (val currentState = state) {
                UploadState.Idle -> Unit
                UploadState.Uploading -> Unit
                is UploadState.Success -> StellarText(
                    text = "上传成功：${currentState.fileName}",
                    tone = StellarTone.Accent,
                    role = StellarTextRole.BodySmall
                )

                is UploadState.Error -> Unit
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
