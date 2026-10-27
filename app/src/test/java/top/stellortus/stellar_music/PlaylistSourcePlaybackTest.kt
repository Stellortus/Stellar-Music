package top.stellortus.stellar_music

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaylistPlaybackController
import top.stellortus.stellar_music_common.dto.Track

class PlaylistSourcePlaybackTest {
    private fun track(id: Int) = Track(id, "歌曲$id", emptyList(), "$id.mp3", "", "", "", 0L)

    @Test fun currentPlaylistToggleDoesNotReloadRestartOrChangeQueuePosition() = runBlocking {
        val played = mutableListOf<Int>()
        var loads = 0
        var toggles = 0
        val sources = mutableListOf<Int?>()
        val controller = PlaylistPlaybackController(this,
            { loads++; listOf(track(1), track(2), track(3)) }, { played += it.id }, { fail(it) },
            onSourcePlaylistChanged = { sources += it }, onTogglePause = { toggles++ })
        controller.togglePlaylist(42)
        yield()
        controller.playQueueIndex(1)
        controller.togglePlaylist(42)
        controller.togglePlaylist(42, listOf(track(99)))
        controller.onCompleted()
        assertEquals(listOf(1, 2, 3), played)
        assertEquals(1, loads)
        assertEquals(2, toggles)
        assertEquals(listOf<Int?>(42), sources)
        assertEquals(42, controller.currentPlaylistId)
    }

    @Test fun detailSelectionRetainsSourceUntilStandaloneOrAnonymousPlayback() = runBlocking {
        val sources = mutableListOf<Int?>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) },
            onSourcePlaylistChanged = { sources += it })
        controller.playPlaylist(listOf(track(1), track(2)), 1, sourcePlaylistId = 7)
        controller.onCompleted()
        assertEquals(7, controller.currentPlaylistId)
        controller.play(track(1))
        assertNull(controller.currentPlaylistId)
        controller.playPlaylist(listOf(track(1)), sourcePlaylistId = 8)
        controller.playPlaylist(listOf(track(1)))
        assertNull(controller.currentPlaylistId)
        assertEquals(listOf<Int?>(7, null, 8, null), sources)
    }

    @Test fun failedAndEmptyLoadsKeepOriginalPlaylistSource() = runBlocking {
        val errors = mutableListOf<String>()
        val controller = PlaylistPlaybackController(this, { id ->
            if (id == 2) emptyList() else error("离线")
        }, {}, { errors += it })
        controller.playPlaylist(listOf(track(1)), sourcePlaylistId = 1)
        controller.togglePlaylist(2)
        yield()
        assertEquals(1, controller.currentPlaylistId)
        controller.togglePlaylist(3)
        yield()
        assertEquals(1, controller.currentPlaylistId)
        assertEquals(2, errors.size)
    }

    @Test fun togglingOriginalPlaylistCancelsPendingSelectionEvenIfItIgnoresCancellation() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        var toggles = 0
        val controller = PlaylistPlaybackController(this,
            { withContext(NonCancellable) { response.await() } }, { played += it.id }, { fail(it) },
            onTogglePause = { toggles++ })
        controller.playPlaylist(listOf(track(1)), sourcePlaylistId = 1)
        controller.togglePlaylist(2)
        yield()
        controller.togglePlaylist(1)
        response.complete(listOf(track(2)))
        yield()
        assertEquals(1, controller.currentPlaylistId)
        assertEquals(listOf(1), played)
        assertEquals(1, toggles)
    }

    @Test fun newPlaylistSourceWinsEvenWhenPlaylistsContainTheSameSong() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val sources = mutableListOf<Int?>()
        val controller = PlaylistPlaybackController(this,
            { id -> if (id == 1) withContext(NonCancellable) { response.await() } else listOf(track(1)) },
            {}, { fail(it) }, onSourcePlaylistChanged = { sources += it })
        controller.togglePlaylist(1)
        yield()
        controller.togglePlaylist(2)
        yield()
        response.complete(listOf(track(1)))
        yield()
        assertEquals(2, controller.currentPlaylistId)
        assertEquals(listOf<Int?>(2), sources)
    }
}
