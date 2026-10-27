@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package top.stellortus.stellar_music

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import top.stellortus.stellar_music.data.AuthPreferences

class StreamPlayerIntentTest {
    @Test fun playbackCanBePausedWhileNotActuallyPlaying() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync {
            AuthPreferences.init(context)
            val requests = mutableListOf<Boolean>()
            var actuallyPlaying = false
            val player = StreamPlayer(context,
                onPlayingChanged = { actuallyPlaying = it },
                onProgressChanged = { _, _ -> }, onError = {}, onCompleted = {},
                onPlaybackRequestedChanged = { requests += it })
            try {
                // With no prepared media, playback remains idle, like a buffering transition.
                player.togglePause()
                assertFalse(actuallyPlaying)
                assertEquals(listOf(true), requests)
                player.togglePause()
                assertFalse(actuallyPlaying)
                assertEquals(listOf(true, false), requests)
            } finally {
                player.release()
            }
        }
    }
}
