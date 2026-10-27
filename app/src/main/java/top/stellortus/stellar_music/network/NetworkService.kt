package top.stellortus.stellar_music.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import top.stellortus.stellar_music.trackFromId
import top.stellortus.stellar_music.trackList
import top.stellortus.stellar_music.trackSearchUrl
import top.stellortus.stellar_music.upload
import top.stellortus.stellar_music.utils.extensions.bodyOrThrow
import top.stellortus.stellar_music.utils.extensions.ensureSuccess
import top.stellortus.stellar_music.utils.plugins.TokenAuth
import top.stellortus.stellar_music_common.dto.Track
import java.io.File

internal val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

object NetworkService {
    val client: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json(networkJson)
        }
        install(TokenAuth)
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun upload(file: File): HttpResponse {
        return client.submitFormWithBinaryData(
            url = upload(file.name),
            formData = formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "audio/mpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            }
        ).ensureSuccess()
    }

    suspend fun get(id: Int): HttpResponse {
        return client.get(trackFromId(id))
    }

    suspend fun getMusicList(start: Int = 0): List<Track> {
        return client.get(trackList()) {
            parameter("start", start)
        }.ensureSuccess().body()
    }

    suspend fun searchTracks(key: String): List<Track> =
        client.request(trackSearchRequest(key)).bodyOrThrow()
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

internal fun trackSearchRequest(key: String) = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url(trackSearchUrl())
    parameter("key", key)
}
