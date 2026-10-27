package top.stellortus.stellar_music.network

import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.playlistSearchUrl
import top.stellortus.stellar_music_common.dto.CreatePlaylistRequest
import top.stellortus.stellar_music_common.dto.Playlist

class PlaylistSearchRequestTest {
    @Test
    fun keywordIsEncodedAsQueryParameterRatherThanHeader() {
        val key = "星河 & night+ /?#="
        val request = playlistSearchRequest(key)
        val url = request.url.build()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals(Url(playlistSearchUrl()).encodedPath, url.encodedPath)
        assertEquals("/playlists/search", url.encodedPath)
        assertEquals(key, Url(url.toString()).parameters["key"])
        assertNull(request.headers["key"])
    }

    @Test
    fun searchResponseIsAPlaylistList() {
        val playlist = Playlist(1, 2, "星河", null, null, true, 0L, 0L, 3)
        val serializer = ListSerializer(Playlist.serializer())
        val response = Json.encodeToString(serializer, listOf(playlist))
        assertEquals(listOf(playlist), Json.decodeFromString(serializer, response))
        assertEquals(emptyList<Playlist>(), Json.decodeFromString(serializer, "[]"))
    }

    @Test
    fun creationAlwaysSendsChosenVisibility() {
        for (isPublic in listOf(false, true)) {
            val body = networkJson.encodeToJsonElement(
                CreatePlaylistRequest.serializer(),
                CreatePlaylistRequest(name = "星河", public = isPublic),
            ).jsonObject
            assertEquals(JsonPrimitive(isPublic), body["public"])
            assertEquals(JsonPrimitive("星河"), body["name"])
            assertFalse(body.containsKey("description"))
            assertFalse(body.containsKey("coverPath"))
        }
    }
}
