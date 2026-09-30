package top.stellortus.stellarmusic.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
        attachBearerToken()
    }

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

    suspend fun getMusicList(start: Int = 0): List<Track> {
        return client.get(trackList()) {
            parameter("start", start)
        }.ensureSuccess().body()
    }

    /**
     * 分页拉取直到某一页为空，用于需要完整歌曲列表的场景（如歌单的选择器）。
     * 服务端单次最多返回 10 条，[maxTracks] 只是防止服务端异常时无限循环。
     */
    suspend fun getAllMusicList(maxTracks: Int = 500): List<Track> {
        val all = mutableListOf<Track>()
        var start = 0
        while (all.size < maxTracks) {
            val page = getMusicList(start)
            if (page.isEmpty()) break
            all += page
            start += page.size
        }
        return all.take(maxTracks)
    }
}
