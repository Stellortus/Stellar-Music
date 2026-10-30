package top.stellortus.stellar_music.ui.screens

import top.stellortus.stellar_music.ui.widgets.StellarEmptyState
import top.stellortus.stellar_music.ui.widgets.StellarLoadingIndicator
import top.stellortus.stellar_music.ui.widgets.StellarRetryAction
import top.stellortus.stellar_music.ui.widgets.StellarSearchField

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.utils.ToastManager
import top.stellortus.stellar_music.ui.components.*
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.utils.extensions.toUserMessage
import top.stellortus.stellar_music_common.dto.Track

@Composable
fun TrackSearchScreen(
    state: TrackSearchState,
    listState: LazyListState,
    onBack: () -> Unit,
    currentTrackId: Int?,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit
) {
    val submit: () -> Unit = {
        state.submit { ToastManager.makeText(it.toUserMessage()) }
    }
    LaunchedEffect(state) { if (!state.submitted) submit() }
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize()) {
        StellarSearchField(state.query, {}, placeholder = "搜索音乐", onClick = onBack,
            contentDescription = "返回歌曲列表，编辑搜索内容",
            modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                state.loading -> StellarLoadingIndicator()
                !state.submitted -> StellarEmptyState("输入歌曲名称搜索")
                state.failed -> StellarRetryAction(submit)
                state.results.isEmpty() -> StellarEmptyState("没有找到匹配的歌曲")
                else -> LazyColumn(Modifier.fillMaxSize(), state = listState,
                    contentPadding = PaddingValues(bottom = StellarTheme.spacing.page)) {
                    itemsIndexed(state.results, key = { _, track -> track.id }) { index, track ->
                        TrackItem(track, onClick = { onTrackClick(track) },
                            modifier = Modifier.padding(horizontal = StellarTheme.spacing.homeTrackMargin),
                            onLongClick = { onTrackLongClick(track) },
                            isCurrent = currentTrackId == track.id,
                            showDivider = index < state.results.lastIndex)
                    }
                }
            }
        }
    }
}
