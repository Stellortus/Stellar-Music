package top.stellortus.stellar_music.ui.components

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.stellortus.stellar_music.BuildConfig
import top.stellortus.stellar_music.apkFromVersionName
import top.stellortus.stellar_music.network.AppUpdateApi
import top.stellortus.stellar_music.ui.ToastManager
import java.util.UUID

@Composable
fun AppUpdatePrompt() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var versionName by remember { mutableStateOf<String?>(null) }
    var update by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val installedVersion = BuildConfig.VERSION_CODE
            val latestCode = AppUpdateApi.latestVersionCode()
            Log.i("AppUpdate", "Update check: installed=$installedVersion, latest=$latestCode")
            if (latestCode == null) {
                Log.w("AppUpdate", "Update server did not return a valid integer version code")
                return@LaunchedEffect
            }
            if (latestCode > installedVersion) {
                versionName = AppUpdateApi.latestVersionName()
                update = true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AppUpdate", "Update check failed", e)
        }
    }

    if (!update) return
    val availableVersionName = versionName ?: return
    val availableFileName = "Stellar_Music_v${availableVersionName}_release.apk"
    val downloadUrl = apkFromVersionName(availableVersionName)

    StellarAlertDialog(
        title = "发现新版本",
        onDismissRequest = { if (!downloading) update = false },
        text = { StellarText("下载完成后，可从系统下载通知打开安装包。") },
        confirmButton = {
            StellarButton(enabled = !downloading, onClick = {
                downloading = true
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            enqueueUpdate(
                                context.applicationContext,
                                availableFileName,
                                downloadUrl
                            )
                        }
                        ToastManager.makeText(context, "已开始后台下载，请查看系统下载通知")
                        update = false
                    } catch (e: CancellationException) {
                        throw e
                    } finally {
                        downloading = false
                    }
                }
            }) { StellarText("立即更新") }
        },
        dismissButton = {
            StellarButton(
                kind = StellarButtonKind.Text,
                enabled = !downloading,
                onClick = { update = false }) {
                StellarText("暂不更新")
            }
        }
    )
}

private fun enqueueUpdate(context: Context, fileName: String, downloadUrl: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(downloadUrl.toUri())
        .setTitle("Stellar Music 更新")
        .setDescription(fileName)
        .setMimeType("application/vnd.android.package-archive")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            "${UUID.randomUUID()}_$fileName"
        )
    manager.enqueue(request)
}
