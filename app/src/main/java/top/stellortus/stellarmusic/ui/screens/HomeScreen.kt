package top.stellortus.stellarmusic.ui.screens

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.stellortus.stellarmusic.R
import top.stellortus.stellarmusic.data.Track
import top.stellortus.stellarmusic.network.NetworkService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

private sealed interface MusicListState {
    data object Loading : MusicListState
    data class Success(val tracks: List<Track>) : MusicListState
    data class Error(val message: String) : MusicListState
}

@Composable
fun HomeScreen() {
    var state by remember { mutableStateOf<MusicListState>(MusicListState.Loading) }
    var playingId by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableFloatStateOf(0f) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    val appContext = LocalContext.current
    val player = remember {
        StreamingAudioPlayer(
            context = appContext,
            onPlayingChanged = { isPlaying = it },
            onProgressChanged = { position, duration ->
                positionMs = position.toFloat()
                durationMs = duration.toFloat()
            },
            onError = { playbackError = it },
            onCompleted = {
                isPlaying = false
                positionMs = 0f
            }
        )
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(Unit) {
        state = runCatching { NetworkService.getMusicList() }
            .fold(
                onSuccess = { MusicListState.Success(it) },
                onFailure = { MusicListState.Error(it.message ?: "获取音乐列表失败") }
            )
    }

    when (val currentState = state) {
        MusicListState.Loading -> LoadingContent()
        is MusicListState.Success -> {
            val currentTrack = currentState.tracks.firstOrNull { it.id == playingId }
            Column(modifier = Modifier.fillMaxSize()) {
                TrackList(
                    tracks = currentState.tracks,
                    modifier = Modifier.weight(1f),
                    onTrackClick = { track ->
                        playingId = track.id
                        positionMs = 0f
                        durationMs = 0f
                        playbackError = null
                        player.play(track.id)
                    }
                )
                playbackError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
                if (playingId != 0 && currentTrack != null) {
                    PlayerCard(
                        title = currentTrack.title,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        onPlayPause = { player.togglePause() }
                    )
                }
            }
        }
        is MusicListState.Error -> ErrorContent(currentState.message)
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text("正在加载音乐列表…", modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    modifier: Modifier = Modifier,
    onTrackClick: (Track) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("音乐列表", style = MaterialTheme.typography.headlineMedium)
        if (tracks.isEmpty()) {
            Text("暂无音乐", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tracks, key = { it.id }) { track ->
                    TrackItem(track, onClick = { onTrackClick(track) })
                }
            }
        }
    }
}

@Composable
private fun TrackItem(track: Track, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artists.joinToString(" / ").ifBlank { "未知歌手" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("上传者：${track.uploader}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("歌曲id：${track.id}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PlayerCard(
    title: String,
    isPlaying: Boolean,
    positionMs: Float,
    durationMs: Float,
    onPlayPause: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayPause) {
                    // 暂时使用左右箭头作为播放/暂停图标，后续可替换资源。
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }
                Slider(
                    value = positionMs,
                    onValueChange = {},
                    valueRange = 0f..durationMs.coerceAtLeast(1f),
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { Text(message, color = MaterialTheme.colorScheme.error) }
}

private class StreamingAudioPlayer(
    private val context: Context,
    private val onPlayingChanged: (Boolean) -> Unit,
    private val onProgressChanged: (Int, Int) -> Unit,
    private val onError: (String) -> Unit,
    private val onCompleted: () -> Unit
) {
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var downloadJob: Job? = null
    private var progressJob: Job? = null
    private var playToken = 0
    private var isPrepared = false
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    fun play(id: Int) {
        val token = ++playToken
        stopCurrent()
        onPlayingChanged(false)
        downloadJob = scope.launch(Dispatchers.IO) {
            val file = File.createTempFile("stellar-track-", ".mp3", context.cacheDir)
            try {
                val response = NetworkService.get(id)
                if (!response.status.isSuccess()) {
                    throw IOException("服务器返回 HTTP ${response.status.value}")
                }
                val channel = response.bodyAsChannel()
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (isActive) {
                        val count = channel.readAvailable(buffer)
                        if (count == -1) break
                        if (count > 0) output.write(buffer, 0, count)
                    }
                }
                if (!isActive || token != playToken) {
                    file.delete()
                    return@launch
                }
                if (file.length() == 0L) throw IOException("服务器返回了空音频")
                withContext(Dispatchers.Main.immediate) { prepareFile(file, token) }
            } catch (error: Throwable) {
                file.delete()
                if (token == playToken) {
                    onPlayingChanged(false)
                    onError("音频下载失败：${error.message ?: "未知错误"}")
                }
            }
        }
    }

    private fun prepareFile(file: File, token: Int) {
        if (token != playToken) {
            file.delete()
            return
        }
        audioFile = file
        val player = MediaPlayer()
        mediaPlayer = player
        try {
            player.setDataSource(file.absolutePath)
            player.setOnPreparedListener {
                if (token == playToken) {
                    isPrepared = true
                    it.start()
                    onPlayingChanged(true)
                    startProgressUpdates(token)
                }
            }
            player.setOnCompletionListener {
                if (token == playToken) {
                    isPrepared = false
                    onPlayingChanged(false)
                    onCompleted()
                }
            }
            player.setOnErrorListener { failedPlayer, what, extra ->
                if (token == playToken) {
                    isPrepared = false
                    onPlayingChanged(false)
                    onError("音频解码失败（what=$what, extra=$extra），下载文件可能不是有效的 MP3 音频")
                    failedPlayer.reset()
                }
                true
            }
            player.prepareAsync()
        } catch (error: IOException) {
            player.release()
            mediaPlayer = null
            file.delete()
            audioFile = null
            if (token == playToken) {
                onPlayingChanged(false)
                onError("播放器初始化失败：${error.message ?: "未知错误"}")
            }
        }
    }

    fun togglePause() {
        val player = mediaPlayer ?: return
        if (!isPrepared) return
        if (player.isPlaying) {
            player.pause()
            onPlayingChanged(false)
        } else {
            player.start()
            onPlayingChanged(true)
        }
    }

    private fun startProgressUpdates(token: Int) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && token == playToken) {
                mediaPlayer?.let { player ->
                    onProgressChanged(player.currentPosition, player.duration.coerceAtLeast(0))
                }
                delay(250.milliseconds)
            }
        }
    }

    private fun stopCurrent() {
        downloadJob?.cancel()
        progressJob?.cancel()
        isPrepared = false
        mediaPlayer?.release()
        mediaPlayer = null
        audioFile?.delete()
        audioFile = null
    }

    fun release() {
        ++playToken
        stopCurrent()
        scope.cancel()
    }
}

private const val DEFAULT_BUFFER_SIZE = 16 * 1024


