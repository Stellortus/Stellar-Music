package top.stellortus.stellar_music

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistSearchState
import top.stellortus.stellar_music_common.dto.Playlist

class PlaylistSearchStateTest {
    private val playlist = Playlist(1, 1, "星河", null, null, true, 0L, 0L, 1)

    @Test
    fun editingDoesNotRequestUntilExplicitSubmission() = runBlocking {
        var requests = 0
        val state = PlaylistSearchState(this) { requests++; listOf(playlist) }
        state.edit("星")
        state.edit("星河")
        yield()
        assertEquals(0, requests)
        state.submit { fail(it.message) }
        yield()
        assertEquals(1, requests)
    }

    @Test
    fun returningFromResultsCancelsRequestWithoutAnError() = runBlocking {
        val started = CompletableDeferred<Unit>()
        var errors = 0
        val state = PlaylistSearchState(this) {
            started.complete(Unit)
            awaitCancellation()
        }
        state.edit("星河")
        state.submit { errors++ }
        started.await()
        state.cancel()
        yield()
        assertFalse(state.loading)
        assertEquals(0, errors)
        assertEquals("星河", state.query)
    }

    @Test
    fun blankKeywordsDoNotRequestAndSubmissionTrimsKeyword() = runBlocking {
        val keys = mutableListOf<String>()
        val state = PlaylistSearchState(this) { key -> keys.add(key); listOf(playlist) }
        state.edit("  ")
        state.submit { fail(it.message) }
        yield()
        assertTrue(keys.isEmpty())
        assertFalse(state.submitted)
        state.edit("  星河  ")
        state.submit { fail(it.message) }
        yield()
        assertEquals(listOf("星河"), keys)
        assertEquals(listOf(playlist), state.results)
        assertFalse(state.loading)
    }

    @Test
    fun editingDiscardsEvenAnUncancellableOldResponse() = runBlocking {
        val response = CompletableDeferred<List<Playlist>>()
        val state = PlaylistSearchState(this) { withContext(NonCancellable) { response.await() } }
        state.edit("旧关键词")
        state.submit { fail(it.message) }
        yield()
        state.edit("")
        response.complete(listOf(playlist))
        yield()
        assertTrue(state.results.isEmpty())
        assertFalse(state.loading)
        assertFalse(state.submitted)
        assertFalse(state.failed)
    }

    @Test
    fun failedSearchCanBeRetriedWithTheSameKeyword() = runBlocking {
        var attempts = 0
        var errors = 0
        val state = PlaylistSearchState(this) {
            if (attempts++ == 0) error("网络错误")
            listOf(playlist)
        }
        state.edit("星河")
        state.submit { errors++ }
        yield()
        assertTrue(state.failed)
        assertEquals(1, errors)
        state.submit { errors++ }
        yield()
        assertFalse(state.failed)
        assertEquals(listOf(playlist), state.results)
        assertEquals(2, attempts)
    }
}
