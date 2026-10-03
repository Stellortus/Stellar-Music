package top.stellortus.stellar_music.ui.screens.playlist

import top.stellortus.stellar_music.network.ApiException
import top.stellortus.stellar_music.utils.extensions.toUserMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 歌单接口的错误提示：401 单独提示，其余沿用全局映射（ApiException.message / IOException → 网络错误）。 */
internal fun Throwable.toPlaylistMessage(): String =
    if (this is ApiException && status == 401) "登录已失效，请重新登录" else toUserMessage()

/** 服务端时间戳是毫秒。minSdk 24，用不了 java.time。 */
internal fun formatDateTime(millis: Long): String {
    if (millis <= 0L) return "未知"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
}
