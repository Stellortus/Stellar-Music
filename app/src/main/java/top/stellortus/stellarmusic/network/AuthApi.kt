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

/** 认证相关 API。baseUrl 复用 [Domain]，协议为 https。 */
object AuthApi {
    private val baseUrl = "https://$Domain"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        attachBearerToken()
    }

    suspend fun register(username: String, password: String): AuthResponse =
        client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username, password))
        }.bodyOrThrow()

    suspend fun login(username: String, password: String): AuthResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.bodyOrThrow()

    suspend fun me(): UserDto =
        client.get("$baseUrl/auth/me").bodyOrThrow()

    suspend fun logout(): MessageResponse =
        client.post("$baseUrl/auth/logout").bodyOrThrow()

    /** 成功（2xx）时解析响应体，否则解析 ErrorResponse 并抛出带 message 的异常。 */
    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T =
        ensureSuccess().body()
}

/**
 * 为 HttpClient 附加登录态：已登录时为每个请求自动带上 Bearer token，未登录则不添加。
 *
 * [AuthApi] 与 [NetworkService] 共用，避免其中一处漏带 token 而被服务端拒绝（401）。
 */
internal fun HttpClientConfig<*>.attachBearerToken() {
    defaultRequest {
        AuthPreferences.current?.token?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

/** 携带 HTTP 状态码的业务异常，便于上层区分 401 等场景。 */
class ApiException(val status: Int, message: String) : Exception(message)

/** 成功（2xx）时返回自身，否则解析服务端 ErrorResponse 并抛出带 message 的 [ApiException]。 */
internal suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.value in 200..299) return this
    val message = try {
        body<ErrorResponse>().message
    } catch (_: Exception) {
        null
    }
    throw ApiException(status.value, message ?: "请求失败（HTTP ${status.value}）")
}

/** 将网络/业务异常转为面向用户的提示文案。 */
fun Throwable.toUserMessage(): String = when {
    this is ApiException -> message ?: "请求失败"
    this is IOException -> "网络错误，请检查网络连接"
    else -> message ?: "操作失败"
}
