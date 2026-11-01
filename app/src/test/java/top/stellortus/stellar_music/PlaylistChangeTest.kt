package top.stellortus.stellar_music

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import top.stellortus.stellar_music.ui.screens.playlist.handlePlaylistChange
import top.stellortus.stellar_music.ui.screens.playlist.renamePlaylistRequest
import top.stellortus.stellar_music.utils.extensions.runCatchingCancellable
import java.io.IOException

class PlaylistChangeTest {
    @Test
    fun successfulChangeClosesTheActionWithoutWarningOrForcedRefresh() {
        val events = mutableListOf<String>()
        Result.success(Unit).handlePlaylistChange(
            onCommitted = { events += "close:$it" },
            onFailure = { events += "warning" },
        )
        assertEquals(listOf("close:false"), events)
    }

    @Test
    fun ordinaryFailureKeepsTheActionOpenForRetry() {
        val error = IOException("offline")
        var closed = false
        var reported: Throwable? = null
        Result.failure<Unit>(error).handlePlaylistChange(
            onCommitted = { closed = true },
            onFailure = { reported = it },
        )
        assertFalse(closed)
        assertSame(error, reported)
    }

    @Test
    fun cancelledChangeDoesNotCloseTheDialogOrReportAnError() = runBlocking {
        val callbacks = mutableListOf<String>()
        val error = runCatching {
            runCatchingCancellable<Unit> { throw CancellationException() }.handlePlaylistChange(
                onCommitted = { callbacks += "close" },
                onFailure = { callbacks += "warning" },
            )
        }.exceptionOrNull()
        assertTrue(error is CancellationException)
        assertTrue(callbacks.isEmpty())
    }

    @Test
    fun bothRenameScreensDeferVisibilityResolutionToTheManager() {
        val request = renamePlaylistRequest("新的名称")
        assertEquals("新的名称", request.name)
        assertNull(request.public)
    }
}
