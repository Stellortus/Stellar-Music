package top.stellortus.stellar_music.network

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import top.stellortus.stellar_music.playlistTrack
import top.stellortus.stellar_music.playlistTracks
import top.stellortus.stellar_music.playlistUrl
import top.stellortus.stellar_music.playlistsUrl
import top.stellortus.stellar_music.playlistSearchUrl
import top.stellortus.stellar_music.utils.extensions.bodyOrThrow
import top.stellortus.stellar_music.utils.extensions.ensureSuccess
import top.stellortus.stellar_music_common.dto.AddTracksRequest
import top.stellortus.stellar_music_common.dto.CreatePlaylistRequest
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.PlaylistDetail
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

object PlaylistApi {
    private val client get() = NetworkService.client

    suspend fun listPlaylists(): List<Playlist> =
        client.get(playlistsUrl()).bodyOrThrow()

    suspend fun searchPlaylists(key: String): List<Playlist> =
        client.request(playlistSearchRequest(key)).bodyOrThrow()

    suspend fun getPlaylist(id: Int): PlaylistDetail =
        client.get(playlistUrl(id)).bodyOrThrow()

    suspend fun createPlaylist(request: CreatePlaylistRequest) =
        client.post(playlistsUrl()) {
            contentType(ContentType.Application.Json)
            setBody(playlistCreateBody(request))
        }.ensureSuccess()

    suspend fun updatePlaylist(id: Int, request: UpdatePlaylistRequest) =
        client.put(playlistUrl(id)) {
            contentType(ContentType.Application.Json)
            // Include false as well: default-valued properties can otherwise be omitted.
            setBody(playlistUpdateBody(request))
        }.ensureSuccess()

    suspend fun deletePlaylist(id: Int) =
        client.delete(playlistUrl(id)) {
            contentType(ContentType.Application.Json)
        }.ensureSuccess()

    suspend fun addTracks(id: Int, trackIds: List<Int>) {
        client.post(playlistTracks(id)) {
            contentType(ContentType.Application.Json)
            setBody(AddTracksRequest(trackIds))
        }.ensureSuccess()
    }

    suspend fun removeTrack(id: Int, trackId: Int) {
        client.delete(playlistTrack(id, trackId)) {
            contentType(ContentType.Application.Json)
        }.ensureSuccess()
    }
}

internal fun playlistUpdateBody(request: UpdatePlaylistRequest): JsonObject = buildJsonObject {
    Json.encodeToJsonElement(UpdatePlaylistRequest.serializer(), request).jsonObject.forEach { (key, value) -> put(key, value) }
    put("public", JsonPrimitive(request.public))
}

internal fun playlistCreateBody(request: CreatePlaylistRequest): JsonObject = buildJsonObject {
    Json.encodeToJsonElement(CreatePlaylistRequest.serializer(), request).jsonObject.forEach { (key, value) -> put(key, value) }
    put("public", JsonPrimitive(request.public))
}

internal fun playlistSearchRequest(key: String) = HttpRequestBuilder().apply {
    method = io.ktor.http.HttpMethod.Get
    url(playlistSearchUrl())
    parameter("key", key)
}
