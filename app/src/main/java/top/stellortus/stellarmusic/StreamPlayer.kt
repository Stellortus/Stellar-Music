package top.stellortus.stellarmusic

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import top.stellortus.stellarmusic.network.AuthStreamDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@UnstableApi
class StreamPlayer(
    context: Context,
    private val onPlayingChanged: (Boolean) -> Unit,
    private val onProgressChanged: (positionMs: Long, durationMs: Long) -> Unit,
    private val onError: (String) -> Unit,
    private val onCompleted: () -> Unit,
) {
    private val appContext = context.applicationContext

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var progressJob: Job? = null

    // 使用带 Bearer token 的数据源，否则播放请求会被服务端以 401 拒绝。
    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(DefaultMediaSourceFactory(AuthStreamDataSource.factory))
        .build()
        .apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    onPlayingChanged(isPlaying)
                    if (isPlaying) startProgressLoop() else stopProgressLoop()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlayingChanged(false)
                        stopProgressLoop()
                        onCompleted()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    stopProgressLoop()
                    onPlayingChanged(false)
                    onError("播放失败：${error.errorCodeName}")
                }
            })
        }

    fun play(id: Int) {
        player.setMediaItem(MediaItem.fromUri(fromId(id)))
        player.prepare()
        player.play()
    }

    fun togglePause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0))
    }

    private fun startProgressLoop() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                onProgressChanged(
                    player.currentPosition.coerceAtLeast(0),
                    player.duration.coerceAtLeast(0),
                )
                delay(250.milliseconds)
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        progressJob?.cancel()
        scope.cancel()
        player.release()
    }
}
