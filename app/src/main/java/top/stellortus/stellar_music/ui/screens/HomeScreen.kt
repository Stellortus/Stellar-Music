package top.stellortus.stellar_music.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music_common.dto.Track

private sealed interface MusicListState {
    data object Loading : MusicListState
    data class Success(val tracks: List<Track>) : MusicListState
    data class Error(val message: String) : MusicListState
}

/** 首页曲库列表。播放器由 MainScreen 持有并通过 [player] 传入，这里只负责列表与翻页。 */
@UnstableApi
@Composable
fun HomeScreen(player: MusicPlayerState) {
    var state by remember { mutableStateOf<MusicListState>(MusicListState.Loading) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()

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
        is MusicListState.Success -> TrackList(
            tracks = currentState.tracks,
            isLoadingMore = isLoadingMore,
            endReached = endReached,
            onLoadMore = loadMore,
            modifier = Modifier.fillMaxSize(),
            onTrackClick = { player.play(it) }
        )
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

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            "音乐列表",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (tracks.isEmpty()) {
            Text(
                "暂无音乐",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    TrackItem(track, onClick = { onTrackClick(track) })
                    if (index < tracks.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            track.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            track.artists.joinToString(" / ").ifBlank { "未知歌手" },
            style = MaterialTheme.typography.bodyMedium,
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
