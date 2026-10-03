package top.stellortus.stellar_music.ui.player

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellar_music.StreamPlayer
import top.stellortus.stellar_music_common.dto.Track
import kotlin.math.abs

@Stable
@UnstableApi
class MusicPlayerState(context: Context) {

    private val player = StreamPlayer(
        context = context.applicationContext,
        onPlayingChanged = { isPlaying = it },
        onProgressChanged = { position, duration ->
            positionMs = position.toFloat()
            durationMs = duration.toFloat()
            dragPositionMs?.let { pending ->
                if (abs(position - pending) < 1000f) dragPositionMs = null
            }
        },
        onError = { playbackError = it },
        onCompleted = {
            isPlaying = false
            positionMs = 0f
            dragPositionMs = null
        },
    )

    var currentTrack: Track? by mutableStateOf(null)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var positionMs: Float by mutableFloatStateOf(0f)
        private set

    var durationMs: Float by mutableFloatStateOf(0f)
        private set

    /** 拖动进度条时的预览位置；松手 seek 完成前优先显示它。 */
    var dragPositionMs: Float? by mutableStateOf(null)
        private set

    var playbackError: String? by mutableStateOf(null)
        private set

    /** 点击列表里的歌曲：重置进度并开始播放。 */
    fun play(track: Track) {
        currentTrack = track
        positionMs = 0f
        durationMs = 0f
        dragPositionMs = null
        playbackError = null
        player.play(track.id)
    }

    fun togglePause() = player.togglePause()

    /** 拖动期间只更新预览位置，松手才真正 seek，避免拖动被进度回调打断。 */
    fun onSeekPreview(value: Float) {
        dragPositionMs = value
    }

    fun onSeekFinished() {
        dragPositionMs?.let { player.seekTo(it.toLong()) }
    }

    fun release() = player.release()
}
