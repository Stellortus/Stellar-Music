package top.stellortus.stellar_music.ui.player

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import top.stellortus.stellar_music.StreamPlayer
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music_common.dto.Track
import kotlin.math.abs

@Stable
@UnstableApi
class MusicPlayerState(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val playlistPlayback: PlaylistPlaybackController = PlaylistPlaybackController(
        scope = scope,
        loadTracks = { PlaylistApi.getPlaylist(it).tracks },
        onPlay = ::startTrack,
        onError = { playbackError = it },
        onQueueChanged = { tracks, index ->
            queueTracks = tracks
            currentQueueIndex = index
        },
        onSourcePlaylistChanged = {
            currentPlaylistId = it
            if (it != null) playlistControlFeedbackVersion++
        },
        onTogglePause = ::togglePause,
    )

    private val player: StreamPlayer = StreamPlayer(
        context = context.applicationContext,
        onPlayingChanged = { isPlaying = it },
        onPlaybackRequestedChanged = { isPlaybackRequested = it },
        onProgressChanged = { position, duration ->
            positionMs = position.toFloat()
            durationMs = duration.toFloat()
            dragPositionMs?.let { pending ->
                if (abs(position - pending) < 1000f) dragPositionMs = null
            }
        },
        onError = { playlistPlayback.stopLoop(); playbackError = it },
        onCompleted = {
            isPlaying = false
            positionMs = 0f
            dragPositionMs = null
            playlistPlayback.onCompleted()
        },
    )

    var currentTrack: Track? by mutableStateOf(null)
        private set

    var currentPlaylistId: Int? by mutableStateOf(null)
        private set

    /** User playback actions refresh cover feedback; automatic track changes do not. */
    var playlistControlFeedbackVersion: Long by mutableLongStateOf(0L)
        private set

    var queueTracks: List<Track> by mutableStateOf(emptyList())
        private set

    var currentQueueIndex: Int by mutableStateOf(-1)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    /** Remains true during buffering and playlist transitions, until paused or playback fails. */
    var isPlaybackRequested: Boolean by mutableStateOf(false)
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
    fun play(track: Track) = playlistPlayback.play(track)

    fun playPlaylist(playlistId: Int) = playlistPlayback.playPlaylist(playlistId)

    fun playPlaylist(tracks: List<Track>, startIndex: Int = 0, sourcePlaylistId: Int? = null) =
        playlistPlayback.playPlaylist(tracks, startIndex, sourcePlaylistId)

    fun togglePlaylist(playlistId: Int, tracks: List<Track>? = null) =
        playlistPlayback.togglePlaylist(playlistId, tracks)

    fun playQueueIndex(index: Int) = playlistPlayback.playQueueIndex(index)

    private fun startTrack(track: Track) {
        currentTrack = track
        positionMs = 0f
        durationMs = 0f
        dragPositionMs = null
        playbackError = null
        player.play(track.id)
    }

    fun togglePause() {
        player.togglePause()
        if (currentPlaylistId != null) playlistControlFeedbackVersion++
    }

    fun clearPlaybackError() { playbackError = null }

    /** 拖动期间只更新预览位置，松手才真正 seek，避免拖动被进度回调打断。 */
    fun onSeekPreview(value: Float) {
        dragPositionMs = value
    }

    fun onSeekFinished() {
        dragPositionMs?.let { player.seekTo(it.toLong()) }
    }

    fun release() {
        playlistPlayback.cancelRequest()
        scope.cancel()
        player.release()
    }
}
