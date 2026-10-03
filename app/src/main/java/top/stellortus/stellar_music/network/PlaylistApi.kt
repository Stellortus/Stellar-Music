package top.stellortus.stellar_music.network

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import top.stellortus.stellar_music.playlistTrack
import top.stellortus.stellar_music.playlistTracks
import top.stellortus.stellar_music.playlistUrl
import top.stellortus.stellar_music.playlistsUrl
import top.stellortus.stellar_music.utils.extensions.bodyOrThrow
import top.stellortus.stellar_music.utils.extensions.ensureSuccess
import top.stellortus.stellar_music_common.dto.AddTracksRequest
import top.stellortus.stellar_music_common.dto.CreatePlaylistRequest
import top.stellortus.stellar_music_common.dto.PlaylistDetailResponse
import top.stellortus.stellar_music_common.dto.PlaylistResponse
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

object PlaylistApi {
    private val client get() = NetworkService.client

    suspend fun listPlaylists(): List<PlaylistResponse> =
        client.get(playlistsUrl()).bodyOrThrow()

    suspend fun getPlaylist(id: Int): PlaylistDetailResponse =
        client.get(playlistUrl(id)).bodyOrThrow()

    suspend fun createPlaylist(request: CreatePlaylistRequest) =
        client.post(playlistsUrl()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.ensureSuccess()

    suspend fun updatePlaylist(id: Int, request: UpdatePlaylistRequest) =
        client.put(playlistUrl(id)) {
            contentType(ContentType.Application.Json)
            setBody(request)
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
