package top.stellortus.stellarmusic.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import top.stellortus.stellarmusic.data.AuthPreferences
import java.io.IOException

object AuthApi {
    private const val BASE_URL = "https://$Domain"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        attachBearerToken()
    }

    suspend fun register(username: String, password: String): AuthResponse =
        client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username, password))
        }.bodyOrThrow()

    suspend fun login(username: String, password: String): AuthResponse =
        client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.bodyOrThrow()

    suspend fun me(): UserDto =
        client.get("$BASE_URL/auth/me").bodyOrThrow()

    suspend fun logout(): MessageResponse =
        client.post("$BASE_URL/auth/logout").bodyOrThrow()

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T =
        ensureSuccess().body()
}

internal fun HttpClientConfig<*>.attachBearerToken() {
    defaultRequest {
        AuthPreferences.current?.token?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

class ApiException(val status: Int, message: String) : Exception(message)

internal suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.value in 200..299) return this
    val message = try {
        body<ErrorResponse>().message
    } catch (_: Exception) {
        null
    }
    throw ApiException(status.value, message ?: "请求失败（HTTP ${status.value}）")
}

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> message ?: "请求失败"
    is IOException -> "网络错误，请检查网络连接"
    else -> message ?: "操作失败"
}
