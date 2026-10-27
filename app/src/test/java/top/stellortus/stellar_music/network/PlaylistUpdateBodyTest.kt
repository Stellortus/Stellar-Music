package top.stellortus.stellar_music.network

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

class PlaylistUpdateBodyTest {
    @Test
    fun visibilityUpdatesAlwaysIncludeExplicitBoolean() {
        for (isPublic in listOf(false, true)) {
            val body = networkJson.encodeToJsonElement(
                UpdatePlaylistRequest.serializer(),
                UpdatePlaylistRequest(public = isPublic),
            ).jsonObject
            assertEquals(JsonPrimitive(isPublic), body["public"])
            assertFalse(body.containsKey("name"))
            assertFalse(body.containsKey("description"))
            assertFalse(body.containsKey("coverPath"))
        }
    }

    @Test
    fun renameIncludesNameAndPreservesEitherVisibility() {
        for (isPublic in listOf(false, true)) {
            val body = networkJson.encodeToJsonElement(
                UpdatePlaylistRequest.serializer(),
                UpdatePlaylistRequest(name = "新的名称", public = isPublic),
            ).jsonObject
            assertEquals(JsonPrimitive("新的名称"), body["name"])
            assertEquals(JsonPrimitive(isPublic), body["public"])
        }
    }
}
