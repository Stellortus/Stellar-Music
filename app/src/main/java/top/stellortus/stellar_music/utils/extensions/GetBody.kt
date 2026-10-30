package top.stellortus.stellar_music.utils.extensions

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import top.stellortus.stellar_music.data.AuthPreferences
import top.stellortus.stellar_music.network.ApiException
import top.stellortus.stellar_music_common.dto.MessageResponse

suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T =
    ensureSuccess().body()

suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.value in 200..299) return this
    val message = try {
        body<MessageResponse>().message
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
    if (status.value == 401 && call.request.headers[HttpHeaders.Authorization] == "Bearer ${AuthPreferences.token}") {
        AuthPreferences.sessionExpired.value = true
    }
    throw ApiException(status.value, message ?: "请求失败（HTTP ${status.value}）")
}
