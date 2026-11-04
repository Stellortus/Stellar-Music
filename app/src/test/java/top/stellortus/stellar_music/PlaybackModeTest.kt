package top.stellortus.stellar_music

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaybackMode
import top.stellortus.stellar_music.ui.player.PlaylistPlaybackController
import top.stellortus.stellar_music_common.dto.Track

class PlaybackModeTest {
    private fun track(id: Int) = Track(id, "歌曲$id", emptyList(), "$id.mp3", "", "", "", 0L)

    @Test fun cyclingModesWrapsAndContinuesFromAnExplicitSelection() = runBlocking {
        val controller = PlaylistPlaybackController(this, { emptyList() }, { fail("Unexpected playback") }, { fail(it) })
        assertEquals(PlaybackMode.Cyclic, controller.mode)
        repeat(2) {
            listOf(PlaybackMode.Shuffle, PlaybackMode.RepeatOne, PlaybackMode.Cyclic).forEach { expected ->
                controller.cycleMode()
                assertEquals(expected, controller.mode)
            }
        }
        controller.changeMode(PlaybackMode.Shuffle)
        controller.cycleMode()
        assertEquals(PlaybackMode.RepeatOne, controller.mode)
        controller.changeMode(PlaybackMode.Cyclic)
        controller.cycleMode()
        assertEquals(PlaybackMode.Shuffle, controller.mode)
        controller.release()
    }

    @Test fun cyclingModesUpdatesSubsequentCompletionWithoutRestartingPlayback() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.cycleMode()
        assertEquals(listOf(1), played)
        controller.onCompleted()
        assertEquals(listOf(1, 2), played)
        controller.cycleMode()
        assertEquals(listOf(1, 2), played)
        controller.onCompleted()
        assertEquals(listOf(1, 2, 2), played)
        controller.cycleMode()
        controller.onCompleted()
        assertEquals(listOf(1, 2, 2, 1), played)
        controller.release()
    }

    private data class PlaybackSnapshot(
        val positionMs: Long,
        val isPlaying: Boolean,
        val isPlaybackRequested: Boolean,
    )

    @Test fun switchingModesPreservesPlayingPausedAndBufferingStateWithoutCallbacks() = runBlocking {
        val states = listOf(
            PlaybackSnapshot(35000L, isPlaying = true, isPlaybackRequested = true),
            PlaybackSnapshot(35000L, isPlaying = false, isPlaybackRequested = false),
            PlaybackSnapshot(35000L, isPlaying = false, isPlaybackRequested = true),
        )
        for (state in states) {
            var playback: PlaybackSnapshot
            var plays = 0
            var toggles = 0
            var queueChanges = 0
            var sourceChanges = 0
            var queue = emptyList<Track>()
            var selected = -1
            val controller = PlaylistPlaybackController(this, { fail("Unexpected reload"); emptyList() }, {
                plays++
                playback = PlaybackSnapshot(0L, true, true)
            }, { fail(it) }, { tracks, index ->
                queueChanges++
                queue = tracks
                selected = index
            }, { sourceChanges++ }, { toggles++ })
            controller.playPlaylist(listOf(track(1), track(2), track(3)), 1, sourcePlaylistId = 7)
            playback = state
            val originalQueue = queue

            repeat(2) {
                listOf(PlaybackMode.Shuffle, PlaybackMode.RepeatOne, PlaybackMode.Cyclic).forEach { mode ->
                    controller.cycleMode()
                    controller.playQueueIndex(1)
                    assertEquals(mode, controller.mode)
                    assertEquals(state, playback)
                    assertSame(originalQueue, queue)
                    assertEquals(1, selected)
                    assertEquals(7, controller.currentPlaylistId)
                    assertEquals(1, plays)
                    assertEquals(0, toggles)
                    assertEquals(1, queueChanges)
                    assertEquals(1, sourceChanges)
                }
            }
            controller.release()
        }
    }

    @Test fun modeChangesOnlyAffectSubsequentCompletionAndManualNavigation() = runBlocking {
        val played = mutableListOf<Int>()
        var selected = -1
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id },
            { fail(it) }, { _, index -> selected = index })
        controller.playPlaylist(listOf(track(1), track(2), track(3)), 1)
        controller.changeMode(PlaybackMode.RepeatOne)
        assertEquals(listOf(2), played)
        controller.onCompleted()
        assertEquals(listOf(2, 2), played)
        controller.next()
        assertNotEquals(1, selected)
        val manualSelection = selected
        controller.changeMode(PlaybackMode.Cyclic)
        assertEquals(3, played.size)
        assertEquals(manualSelection, selected)
        controller.onCompleted()
        assertEquals((manualSelection + 1) % 3, selected)
        assertEquals(selected + 1, played.last())
        controller.release()
    }

    @Test fun repeatedModeChangesKeepTheSameShuffleAcrossRounds() = runBlocking {
        var selected = -1
        val controller = PlaylistPlaybackController(this, { emptyList() }, {}, { fail(it) },
            { _, index -> selected = index })
        controller.changeMode(PlaybackMode.Shuffle)
        controller.playPlaylist((1..5).map(::track), 2)
        val firstRound = List(5) { selected.also { controller.next() } }
        assertEquals(2, firstRound.first())
        assertEquals((0 until 5).toSet(), firstRound.toSet())
        repeat(3) {
            PlaybackMode.entries.forEach(controller::changeMode)
            controller.changeMode(PlaybackMode.Shuffle)
            assertEquals(firstRound, List(5) { selected.also { controller.next() } })
        }
        controller.release()
    }

    @Test fun changingModeDoesNotCancelPendingPlaylistLoads() = runBlocking {
        val response = CompletableDeferred<List<Track>>()
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { response.await() }, { played += it.id }, { fail(it) })
        controller.playPlaylist(42)
        yield()
        controller.changeMode(PlaybackMode.RepeatOne)
        response.complete(listOf(track(1), track(2)))
        yield()
        assertEquals(listOf(1), played)
        assertEquals(42, controller.currentPlaylistId)
        controller.onCompleted()
        assertEquals(listOf(1, 1), played)
        controller.release()
    }

    @Test fun replacingTheQueueKeepsTheSelectedModeAndStartsAtTheRequestedSong() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        controller.changeMode(PlaybackMode.RepeatOne)
        controller.playPlaylist(listOf(track(1), track(2), track(3)), 1)
        controller.onCompleted()
        controller.playPlaylist(listOf(track(4), track(5)), 1)
        controller.onCompleted()
        assertEquals(PlaybackMode.RepeatOne, controller.mode)
        assertEquals(listOf(2, 2, 5, 5), played)
        controller.release()
    }

    @Test fun changingModeDoesNotRestoreAutomaticPlaybackAfterAnError() = runBlocking {
        val played = mutableListOf<Int>()
        val controller = PlaylistPlaybackController(this, { emptyList() }, { played += it.id }, { fail(it) })
        controller.playPlaylist(listOf(track(1), track(2)))
        controller.stopLoop()
        PlaybackMode.entries.forEach {
            controller.changeMode(it)
            controller.onCompleted()
        }
        assertEquals(listOf(1), played)
        controller.next()
        controller.onCompleted()
        assertEquals(listOf(1, 2, 2), played)
        controller.release()
    }

    @Test fun creatingAndReleasingAnotherControllerDoesNotChangeTheFirstQueue() = runBlocking {
        val firstPlayed = mutableListOf<Int>()
        val first = PlaylistPlaybackController(this, { emptyList() }, { firstPlayed += it.id }, { fail(it) })
        first.playPlaylist(listOf(track(1), track(2), track(3)), 1)
        val secondPlayed = mutableListOf<Int>()
        val second = PlaylistPlaybackController(this, { emptyList() }, { secondPlayed += it.id }, { fail(it) })
        second.playPlaylist(listOf(track(10), track(11)))
        second.changeMode(PlaybackMode.RepeatOne)
        first.onCompleted()
        second.release()
        first.onCompleted()
        first.onCompleted()
        assertEquals(PlaybackMode.Cyclic, first.mode)
        assertEquals(listOf(2, 3, 1, 2), firstPlayed)
        assertEquals(listOf(10), secondPlayed)
        first.release()
    }
}
