package top.stellortus.stellar_music

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaylistPlaybackController
import top.stellortus.stellar_music.ui.player.RepeatOneOrder
import top.stellortus.stellar_music.ui.player.SequentialOrder
import top.stellortus.stellar_music.ui.player.ShuffleOrder
import top.stellortus.stellar_music_common.dto.Track

class PlaylistPlaybackControllerTest {
    private fun track(id: Int) = Track(id, "歌曲$id", emptyList(), "$id.mp3", "", "", "", 0L)

    @Test
    fun selectedSongAdvancesInOrderAndWrapsUsingASnapshot() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        val tracks = mutableListOf(track(1), track(2), track(3))
        controller.playPlaylist(tracks, 1)
        tracks.clear()
        repeat(3) { controller.onCompleted() }
        assertEquals(listOf(2, 3, 1, 2), played)
    }

    @Test
    fun singleSongLoopsAndStandalonePlaybackClearsQueue() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1)))
        controller.onCompleted()
        controller.play(track(2))
        controller.onCompleted()
        assertEquals(listOf(1, 1, 2, 2), played)
    }

    @Test
    fun emptyAndFailedLoadsPreserveCurrentQueue() = runBlocking {
        val played = mutableListOf<Int>()
        val errors = mutableListOf<String>()
        val controller = PlaylistPlaybackController(this, { error("离线") }, { played += it.id }, { errors += it })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.playPlaylist(emptyList())
        controller.playPlaylist(42)
        yield()
        controller.onCompleted()
        assertEquals(listOf(1, 2), played)
        assertEquals(2, errors.size)
    }

    @Test
    fun lastPlaylistSelectionWinsEvenIfOldRequestIgnoresCancellation() = runBlocking {
        val oldResponse = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this,
            { id -> if (id == 1) withContext(NonCancellable) { oldResponse.await() } else listOf(track(2)) },
            { played += it.id }, { fail(it) })
        controller.playPlaylist(1)
        yield()
        controller.playPlaylist(2)
        yield()
        oldResponse.complete(listOf(track(1)))
        yield()
        assertEquals(listOf(2), played)
    }

    @Test
    fun directSelectionCancelsPendingPlaylistAndPlaybackFailureStopsLoop() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this,
            { withContext(NonCancellable) { response.await() } }, { played += it.id }, { fail(it) })
        controller.playPlaylist(1)
        yield()
        controller.play(track(2))
        response.complete(listOf(track(1)))
        yield()
        controller.onCompleted()
        controller.playPlaylist(listOf(track(3)))
        controller.stopLoop()
        controller.onCompleted()
        assertEquals(listOf(2, 2, 3), played)
    }

    @Test
    fun manualNavigationWrapsAndSynchronizesAllOrdersWithOnePlaybackPerAction() = runBlocking {
        val played = mutableListOf<Int>()
        val selected = mutableListOf<Int>()
        val orders = listOf(SequentialOrder, ShuffleOrder, RepeatOneOrder)
        val controller = PlaylistPlaybackController(this, { emptyList() }, {
            assertTrue(orders.all { order -> order.index == it.id - 1 })
            played += it.id
        }, { fail(it) }, { _, index -> selected += index })
        controller.playPlaylist(listOf(track(1), track(2), track(3)), 1)
        controller.previous()
        controller.previous()
        controller.next()
        controller.playQueueIndex(1)
        controller.playQueueIndex(1)
        controller.onCompleted()
        assertEquals(listOf(2, 1, 3, 1, 2, 3), played)
        assertEquals(listOf(1, 0, 2, 0, 1, 2), selected)
    }

    @Test
    fun bothManualDirectionsCancelPendingLoadsEvenWhenTheyIgnoreCancellation() = runBlocking {
        for (forward in listOf(true, false)) {
            val response = CompletableDeferred<List<Track>>()
            val played = mutableListOf<Int>()
            val controller = PlaylistPlaybackController(this,
                { withContext(NonCancellable) { response.await() } },
                { played += it.id }, { fail(it) })
            controller.playPlaylist(listOf(track(1), track(2), track(3)))
            controller.playPlaylist(42)
            yield()
            if (forward) controller.next() else controller.previous()
            response.complete(listOf(track(99)))
            yield()
            controller.onCompleted()
            assertEquals(if (forward) listOf(1, 2, 3) else listOf(1, 3, 1), played)
            controller.release()
        }
    }

    @Test
    fun explicitPlaybackRestoresCompletionAfterAnErrorStopsTheLoop() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.stopLoop()
        controller.onCompleted()
        controller.next()
        controller.onCompleted()
        controller.stopLoop()
        controller.play(track(3))
        controller.onCompleted()
        assertEquals(listOf(1, 2, 1, 3, 3), played)
    }

    @Test
    fun releaseClearsAllOrdersAndRejectsLateLoadsBeforeAnewSessionStarts() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this,
            { withContext(NonCancellable) { response.await() } }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.playPlaylist(42)
        yield()
        controller.release()
        response.complete(listOf(track(99)))
        yield()
        controller.onCompleted()
        controller.next()
        controller.previous()
        assertEquals(listOf(1), played)
        listOf(SequentialOrder, ShuffleOrder, RepeatOneOrder).forEach {
            assertEquals(-1, it.index)
            assertNull(it.step())
            assertNull(it.next())
            assertNull(it.previous())
        }

        val nextSession = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        nextSession.play(track(3))
        nextSession.onCompleted()
        assertEquals(listOf(1, 3, 3), played)
        nextSession.release()
    }

    @Test
    fun emptyQueueIgnoresCompletionNavigationAndInvalidSelections() = runBlocking {
        val controller = PlaylistPlaybackController(this, { emptyList() }, { fail("Unexpected playback") }, { fail(it) },
            { _, _ -> fail("Unexpected queue update") })
        controller.onCompleted()
        controller.next()
        controller.previous()
        controller.playQueueIndex(0)
        controller.playQueueIndex(-1)
        controller.release()
    }
}
