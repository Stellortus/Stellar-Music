package top.stellortus.stellar_music.ui.screens.playlist

import top.stellortus.stellar_music.ui.widgets.StellarEmptyState
import top.stellortus.stellar_music.ui.widgets.StellarLoadingIndicator
import top.stellortus.stellar_music.ui.widgets.StellarRetryAction
import top.stellortus.stellar_music.ui.widgets.StellarSearchField

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.CoroutineScope
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.utils.ToastManager
import top.stellortus.stellar_music.ui.components.*
import top.stellortus.stellar_music.ui.screens.SearchState
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Playlist

/** Owned by the playlist navigation host so detail navigation preserves the search. */
class PlaylistSearchState(
    scope: CoroutineScope,
    search: suspend (String) -> List<Playlist> = PlaylistApi::searchPlaylists,
) : SearchState<Playlist>(scope, search)

@Composable
fun PlaylistSearchScreen(
    state: PlaylistSearchState,
    listState: LazyListState,
    onBack: () -> Unit,
    onOpenPlaylist: (Int) -> Unit,
    onPlayPlaylist: (Int) -> Unit,
    currentPlaylistId: Int? = null,
    isPlaying: Boolean = false,
    controlFeedbackVersion: Long = 0L,
) {
    val submit: () -> Unit = {
        state.submit { ToastManager.makeText(it.toPlaylistMessage()) }
    }
    LaunchedEffect(state) {
        if (!state.submitted) submit()
    }
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().padding(StellarTheme.spacing.page)) {
        StellarSearchField(value = state.query, onValueChange = {}, onClick = onBack,
            contentDescription = "返回歌单列表，编辑搜索内容")
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                state.loading -> StellarLoadingIndicator()
                !state.submitted -> StellarEmptyState("输入歌单名称搜索")
                state.failed -> StellarRetryAction(submit)
                state.results.isEmpty() -> StellarEmptyState("没有找到匹配的歌单")
                else -> LazyColumn(Modifier.fillMaxSize(), state = listState,
                    contentPadding = PaddingValues(top = StellarTheme.spacing.itemGap)) {
                    items(state.results, key = { it.id }) { playlist ->
                        PlaylistItem(playlist, onOpen = { onOpenPlaylist(playlist.id) },
                            onPlay = { onPlayPlaylist(playlist.id) },
                            isCurrent = currentPlaylistId == playlist.id, isPlaying = isPlaying,
                            controlFeedbackVersion = controlFeedbackVersion,
                            onRename = {}, onDelete = {}, showActions = false)
                    }
                }
            }
        }
    }
}
