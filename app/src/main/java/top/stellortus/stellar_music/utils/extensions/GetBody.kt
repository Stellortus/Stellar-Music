package top.stellortus.stellar_music.utils.extensions

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import top.stellortus.stellar_music.network.ApiException
import top.stellortus.stellar_music_common.dto.MessageResponse

suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T =
    ensureSuccess().body()

suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.value in 200..299) return this
    val message = try {
        body<MessageResponse>().message
    } catch (_: Exception) {
        null
    }
    throw ApiException(status.value, message ?: "请求失败（HTTP ${status.value}）")
}