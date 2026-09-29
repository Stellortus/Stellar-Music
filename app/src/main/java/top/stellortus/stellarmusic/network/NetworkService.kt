package top.stellortus.stellarmusic.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import top.stellortus.stellarmusic.data.Track
import top.stellortus.stellarmusic.fromId
import top.stellortus.stellarmusic.trackList
import java.io.File

object NetworkService {
    val client: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                // 复用连接，提升流式下载效率
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        // 与 AuthApi 一致：已登录时自动附加 Bearer token，否则上传/删除会被服务端拒绝（401）。
        attachBearerToken()
    }

    /** 非 2xx 时抛出 [ApiException]，避免调用方把失败当成成功。 */
    suspend fun upload(file: File): HttpResponse {
        return client.submitFormWithBinaryData(
            url = "https://$Domain/track/${file.name}",
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "audio/mpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            }
        ).ensureSuccess()
    }

    suspend fun get(id: Int): HttpResponse {
        return client.get(fromId(id))
    }

    suspend fun delete(id: Int): HttpResponse {
        return client.delete(fromId(id)).ensureSuccess()
    }

    suspend fun getMusicList(): List<Track> {
        return client.get(trackList()).ensureSuccess().body()
    }
}
