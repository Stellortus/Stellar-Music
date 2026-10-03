package top.stellortus.stellar_music.utils.extensions

import top.stellortus.stellar_music.network.ApiException
import java.io.IOException


fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> message ?: "请求失败"
    is IOException -> "网络错误，请检查网络连接"
    else -> message ?: "操作失败"
}
