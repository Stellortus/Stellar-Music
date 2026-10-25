package top.stellortus.stellar_music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music.ui.components.StellarIndicatorSize
import top.stellortus.stellar_music.ui.components.StellarLoadingIndicator
import top.stellortus.stellar_music.ui.components.StellarLoadingState
import top.stellortus.stellar_music.ui.components.StellarRetryAction
import top.stellortus.stellar_music.ui.components.StellarSearchField
import top.stellortus.stellar_music.ui.components.StellarText
import top.stellortus.stellar_music.ui.components.StellarTextRole
import top.stellortus.stellar_music.ui.components.StellarTone
import top.stellortus.stellar_music.ui.components.TrackDetails
import top.stellortus.stellar_music.ui.components.TrackItem
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Track

private sealed interface MusicListState {
    data object Loading : MusicListState
    data class Success(val tracks: List<Track>) : MusicListState
    data class Error(val message: String) : MusicListState
}

@UnstableApi
@Composable
fun HomeScreen(player: MusicPlayerState) {
    var state by remember { mutableStateOf<MusicListState>(MusicListState.Loading) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    var addToPlaylistTarget by remember { mutableStateOf<Track?>(null) }
    var searchText by remember { mutableStateOf("") }
    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchState = remember { TrackSearchState(scope) }
    val homeListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    var showingSearch by remember { mutableStateOf(false) }

    var retryTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(retryTick) {
        state = MusicListState.Loading
        currentIndex = 0
        state = runCatching { NetworkService.getMusicList(currentIndex) }
            .fold(
                onSuccess = {
                    currentIndex = it.size
                    endReached = it.isEmpty()
                    MusicListState.Success(it)
                },
                onFailure = {
                    ToastManager.makeText(appContext, it.message ?: "获取音乐列表失败")
                    MusicListState.Error(it.message.orEmpty())
                }
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

    if (showingSearch) {
        TrackSearchScreen(
            state = searchState,
            listState = searchListState,
            onBack = { searchState.cancel(); showingSearch = false },
            currentTrackId = player.currentTrack?.id,
            onTrackClick = { player.play(it) },
            onTrackLongClick = { addToPlaylistTarget = it }
        )
    } else Column(Modifier.fillMaxSize()) {
        StellarSearchField(searchText, { searchText = it }, placeholder = "搜索音乐",
            onSearch = { value ->
                if (value.isNotBlank()) {
                    searchState.edit(value.trim())
                    scope.launch { searchListState.scrollToItem(0) }
                    showingSearch = true
                }
            },
            modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap),
            )
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (val currentState = state) {
                MusicListState.Loading -> StellarLoadingState("正在加载音乐列表…")
                is MusicListState.Success -> TrackList(
                    listState = homeListState,
                    tracks = currentState.tracks,
                    isLoadingMore = isLoadingMore,
                    endReached = endReached,
                    onLoadMore = loadMore,
                    modifier = Modifier.fillMaxSize(),
                    currentTrackId = player.currentTrack?.id,
                    onTrackClick = { player.play(it) },
                    onTrackLongClick = { addToPlaylistTarget = it }
                )
                is MusicListState.Error -> StellarRetryAction(onRetry = { retryTick++ })
            }
        }
    }

    addToPlaylistTarget?.let { track ->
        TrackDetails(
            track = track,
            onDismiss = { addToPlaylistTarget = null }
        )
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    isLoadingMore: Boolean,
    endReached: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    currentTrackId: Int?,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
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

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = StellarTheme.spacing.page)
    ) {
        if (tracks.isEmpty()) {
            item(key = "empty") {
                StellarText("暂无音乐", tone = StellarTone.Secondary,
                    modifier = Modifier.padding(horizontal = StellarTheme.spacing.page, vertical = StellarTheme.spacing.itemGap))
            }
        } else {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                TrackItem(track, onClick = { onTrackClick(track) },
                    modifier = Modifier.padding(horizontal = StellarTheme.spacing.homeTrackMargin),
                    onLongClick = { onTrackLongClick(track) },
                    isCurrent = currentTrackId == track.id,
                    showDivider = index < tracks.lastIndex)
            }
            if (isLoadingMore) {
                item(key = "loading-more") {
                    Column(Modifier.padding(top = StellarTheme.spacing.itemGap)) { LoadingMoreFooter() }
                }
            } else if (endReached) {
                item(key = "end-reached") {
                    Column(Modifier.padding(top = StellarTheme.spacing.itemGap)) { EndReachedFooter() }
                }
            }
        }
    }
}

@Composable
private fun LoadingMoreFooter() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = StellarTheme.spacing.itemGap),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StellarLoadingIndicator(size = StellarIndicatorSize.Small, modifier = Modifier.size(StellarTheme.sizes.indicator))
        StellarText(
            "正在加载更多…",
            role = StellarTextRole.Caption,
            tone = StellarTone.Secondary,
            modifier = Modifier.padding(start = StellarTheme.spacing.gap)
        )
    }
}

@Composable
private fun EndReachedFooter() {
    StellarText(
        "已经到底啦",
        role = StellarTextRole.Caption,
        tone = StellarTone.Secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = StellarTheme.spacing.itemGap)
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "Home Light", widthDp = 360, heightDp = 640)
@androidx.compose.ui.tooling.preview.Preview(name = "Home Dark", widthDp = 360, heightDp = 640, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@androidx.compose.ui.tooling.preview.Preview(name = "Home Large Text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun HomePreview() {
    top.stellortus.stellar_music.ui.theme.StellarMusicTheme {
        Column(Modifier.fillMaxSize()) {
            StellarSearchField("", {}, placeholder = "搜索音乐",
                modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                    top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap))
            TrackList(
                modifier = Modifier.weight(1f),
                tracks = listOf(
                    Track(1, "星河漫游 · 一首很长的歌名用于检查文本截断", listOf("Stellar", "夜航计划"), "music.mp3", "Listener", "", "", 0L),
                    Track(2, "晚风与回声", emptyList(), "echo.mp3", "Listener", "", "", 0L)
                ),
                isLoadingMore = false, endReached = true, onLoadMore = {},
                currentTrackId = 1, onTrackClick = {}, onTrackLongClick = {}
            )
        }
    }
}
