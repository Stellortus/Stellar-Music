package top.stellortus.stellarmusic.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import top.stellortus.stellarmusic.playlistTrack
import top.stellortus.stellarmusic.playlistTracks
import top.stellortus.stellarmusic.playlistUrl
import top.stellortus.stellarmusic.playlistsUrl

/**
 * 歌单接口。复用 NetworkService 的 HttpClient，因此自动带上 Bearer token
 * 与 ContentNegotiation 配置；非 2xx 统一抛 ApiException（message 取自 {"message": "..."}）。
 */
object PlaylistApi {
    private val client get() = NetworkService.client

    suspend fun listPlaylists(): List<PlaylistDto> =
        client.get(playlistsUrl()).ensureSuccess().body()

    suspend fun getPlaylist(id: Int): PlaylistDetailDto =
        client.get(playlistUrl(id)).ensureSuccess().body()

    suspend fun createPlaylist(request: CreatePlaylistRequest): PlaylistDto =
        client.post(playlistsUrl()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.ensureSuccess().body()

    suspend fun updatePlaylist(id: Int, request: UpdatePlaylistRequest): PlaylistDto =
        client.put(playlistUrl(id)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.ensureSuccess().body()

    suspend fun deletePlaylist(id: Int) {
        client.delete(playlistUrl(id)).ensureSuccess()
    }

    suspend fun addTracks(id: Int, trackIds: List<Int>) {
        client.post(playlistTracks(id)) {
            contentType(ContentType.Application.Json)
            setBody(AddTracksRequest(trackIds))
        }.ensureSuccess()
    }

    suspend fun removeTrack(id: Int, trackId: Int) {
        client.delete(playlistTrack(id, trackId)).ensureSuccess()
    }
}
