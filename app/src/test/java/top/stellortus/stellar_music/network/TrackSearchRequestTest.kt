package top.stellortus.stellar_music.network

import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music_common.dto.Track

class TrackSearchRequestTest {
    @Test fun keywordIsEncodedAsQueryParameter() {
        val key = "星河 & night+ /?#="
        val request = trackSearchRequest(key)
        val url = request.url.build()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/track_list/search", url.encodedPath)
        assertEquals(key, Url(url.toString()).parameters["key"])
        assertNull(request.headers["key"])
    }

    @Test fun decodesServerTrackListAndEmptyResults() {
        val response = """[{"id":1,"title":"星河","artists":["Stellar"],"fileName":"music.mp3","uploader":"Listener","coverPath":"","lyricsPath":"","createdAt":0}]"""
        val serializer = ListSerializer(Track.serializer())
        val result = Json.decodeFromString(serializer, response)
        assertEquals(1, result.single().id)
        assertEquals("星河", result.single().title)
        assertEquals(emptyList<Track>(), Json.decodeFromString(serializer, "[]"))
    }
}
