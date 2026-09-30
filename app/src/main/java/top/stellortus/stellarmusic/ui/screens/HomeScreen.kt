package top.stellortus.stellarmusic.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellarmusic.R
import top.stellortus.stellarmusic.StreamPlayer
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import top.stellortus.stellarmusic.data.Track
import top.stellortus.stellarmusic.network.NetworkService
import top.stellortus.stellarmusic.ui.ToastManager

private sealed interface MusicListState {
    data object Loading : MusicListState
    data class Success(val tracks: List<Track>) : MusicListState
    data class Error(val message: String) : MusicListState
}

@UnstableApi
@Composable
fun HomeScreen() {
    var state by remember { mutableStateOf<MusicListState>(MusicListState.Loading) }
    var playingId by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableFloatStateOf(0f) }
    var dragPositionMs by remember { mutableStateOf<Float?>(null) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember {
        StreamPlayer(
            context = appContext,
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
            }
        )
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(Unit) {
        state = runCatching { NetworkService.getMusicList(currentIndex) }
            .fold(
                onSuccess = {
                    currentIndex = it.size
                    endReached = it.isEmpty()
                    MusicListState.Success(it)
                },
                onFailure = { MusicListState.Error(it.message ?: "获取音乐列表失败") }
            )
    }

    val loadMore: () -> Unit = remember {
        {
            if (!isLoadingMore && !endReached && state is MusicListState.Success) {
                isLoadingMore = true
                scope.launch {
                    runCatching { NetworkService.getMusicList(currentIndex) }
                        .onSuccess { page ->
                            currentIndex += page.size
                            val existing = (state as? MusicListState.Success)?.tracks.orEmpty()
                            val known = existing.mapTo(mutableSetOf()) { it.id }
                            val fresh = page.filter { known.add(it.id) }
                            if (fresh.isEmpty()) {
                                endReached = true
                            } else {
                                state = MusicListState.Success(existing + fresh)
                            }
                        }
                        .onFailure { ToastManager.makeText(appContext, it.message ?: "加载更多失败") }
                    isLoadingMore = false
                }
            }
        }
    }

    when (val currentState = state) {
        MusicListState.Loading -> LoadingContent()
        is MusicListState.Success -> {
            val currentTrack = currentState.tracks.firstOrNull { it.id == playingId }
            Column(modifier = Modifier.fillMaxSize()) {
                TrackList(
                    tracks = currentState.tracks,
                    isLoadingMore = isLoadingMore,
                    endReached = endReached,
                    onLoadMore = loadMore,
                    modifier = Modifier.weight(1f),
                    onTrackClick = { track ->
                        playingId = track.id
                        positionMs = 0f
                        durationMs = 0f
                        dragPositionMs = null
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
                        dragPositionMs = dragPositionMs,
                        onPlayPause = { player.togglePause() },
                        onValueChange = { dragPositionMs = it },
                        onValueChangeFinished = {
                            dragPositionMs?.let { player.seekTo(it.toLong()) }
                        }
                    )
                }
            }
        }
        is MusicListState.Error -> ToastManager.makeText(currentState.message)
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
    isLoadingMore: Boolean,
    endReached: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    onTrackClick: (Track) -> Unit
) {
    val listState = rememberLazyListState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    // 列表最后一项（曲目或底部提示）进入可视区域即视为已滑到底部，继续下滑就请求下一页；
    // tracks 变化后重启，这样一页填不满屏幕时会继续补下一页
    LaunchedEffect(listState, tracks.size) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@snapshotFlow false
            lastVisible >= layoutInfo.totalItemsCount - 1
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnLoadMore() }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("音乐列表", style = MaterialTheme.typography.headlineMedium)
        if (tracks.isEmpty()) {
            Text("暂无音乐", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tracks, key = { it.id }) { track ->
                    TrackItem(track, onClick = { onTrackClick(track) })
                }
                if (isLoadingMore) {
                    item(key = "loading-more") { LoadingMoreFooter() }
                } else if (endReached) {
                    item(key = "end-reached") { EndReachedFooter() }
                }
            }
        }
    }
}

@Composable
private fun LoadingMoreFooter() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Text(
            "正在加载更多…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun EndReachedFooter() {
    Text(
        "已经到底啦",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    )
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
    dragPositionMs: Float?,
    onPlayPause: () -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }
                SeekBar(
                    value = dragPositionMs ?: positionMs,
                    durationMs = durationMs,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    modifier = Modifier.weight(1f).height(24.dp),

                )
                Text(
                    text = "${formatTime(dragPositionMs ?: positionMs)} / ${formatTime(durationMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SeekBar(
    value: Float,
    durationMs: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxValue = durationMs.coerceAtLeast(1f)
    val fraction = (value / maxValue).coerceIn(0f, 1f)
    val inactiveTrackColor = MaterialTheme.colorScheme.secondary
    val activeTrackColor = MaterialTheme.colorScheme.primary
    val thumbColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .pointerInput(maxValue) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    if (width > 0f) {
                        onValueChange((offset.x / width).coerceIn(0f, 1f) * maxValue)
                        onValueChangeFinished()
                    }
                }
            }
            .pointerInput(maxValue) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            onValueChange((offset.x / width).coerceIn(0f, 1f) * maxValue)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            onValueChange(
                                (change.position.x / width).coerceIn(0f, 1f) * maxValue
                            )
                        }
                    },
                    onDragEnd = { onValueChangeFinished() },
                    onDragCancel = { onValueChangeFinished() },
                )
            }
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..maxValue)
                setProgress { target ->
                    onValueChange(target.coerceIn(0f, maxValue))
                    onValueChangeFinished()
                    true
                }
            }
    ) {
        val trackHeight = 4.dp.toPx()
        val y = size.height / 2f
        val thumbRadius = minOf(6.dp.toPx(), size.width / 2f)
        val thumbX = (fraction * size.width).coerceIn(thumbRadius, size.width - thumbRadius)

        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = activeTrackColor,
            start = Offset(0f, y),
            end = Offset(thumbX, y),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round,
        )
        drawCircle(color = thumbColor, radius = thumbRadius, center = Offset(thumbX, y))
    }
}

private fun formatTime(ms: Float): String {
    val totalSeconds = (ms / 1000f).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
