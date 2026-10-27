package top.stellortus.stellar_music

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaylistPlaybackController
import top.stellortus.stellar_music_common.dto.Track

class PlaybackQueueTest {
    private fun track(id: Int) = Track(id, "歌曲$id", emptyList(), "$id.mp3", "", "", "", 0L)

    @Test fun selectionAndCompletionPublishTheSameSnapshotAndCorrectIndex() = runBlocking {
        val played = mutableListOf<Int>()
        val updates = mutableListOf<Pair<List<Track>, Int>>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id },
            { fail(it) }, { tracks, index -> updates += tracks to index })
        val source = mutableListOf(track(1), track(2), track(3))
        controller.playPlaylist(source, 1)
        source.clear()
        controller.playQueueIndex(0)
        controller.playQueueIndex(0)
        controller.onCompleted()
        assertEquals(listOf(2, 1, 2), played)
        assertEquals(listOf(1, 0, 1), updates.map { it.second })
        assertTrue(updates.all { it.first.map { track -> track.id } == listOf(1, 2, 3) })
    }

    @Test fun standaloneSongIsVisibleWithoutEnablingLooping() = runBlocking {
        val played = mutableListOf<Int>()
        var queue = emptyList<Track>()
        var selected = -1
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id },
            { fail(it) }, { tracks, index -> queue = tracks; selected = index })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.play(track(3))
        controller.onCompleted()
        assertEquals(listOf(1, 3), played)
        assertEquals(listOf(track(3)), queue)
        assertEquals(0, selected)
    }

    @Test fun selectingQueueCancelsPendingLoadEvenWhenSelectingCurrentSong() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this,
            { withContext(NonCancellable) { response.await() } }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.playPlaylist(42)
        yield()
        controller.playQueueIndex(0)
        response.complete(listOf(track(3)))
        yield()
        controller.onCompleted()
        assertEquals(listOf(1, 2), played)
    }

    @Test fun failedAndEmptyLoadsPreservePublishedQueue() = runBlocking {
        val updates = mutableListOf<Pair<List<Track>, Int>>()
        val controller = PlaylistPlaybackController(this, { error("离线") }, {}, {},
            { tracks, index -> updates += tracks to index })
        controller.playPlaylist(listOf(track(1), track(2)), 1)
        controller.playPlaylist(emptyList())
        controller.playPlaylist(42)
        yield()
        assertEquals(listOf(listOf(track(1), track(2)) to 1), updates)
    }
}
