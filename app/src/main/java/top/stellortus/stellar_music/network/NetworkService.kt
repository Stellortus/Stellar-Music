package top.stellortus.stellar_music.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import top.stellortus.stellar_music.data.AuthPreferences
import top.stellortus.stellar_music.fromId
import top.stellortus.stellar_music.trackList
import top.stellortus.stellar_music.utils.extensions.ensureSuccess
import top.stellortus.stellar_music_common.dto.Track
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

        defaultRequest {
            AuthPreferences.current?.token?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
        }
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

    suspend fun getMusicList(start: Int = 0): List<Track> {
        return client.get(trackList()) {
            parameter("start", start)
        }.ensureSuccess().body()
    }
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
