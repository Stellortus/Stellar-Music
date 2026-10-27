package top.stellortus.stellar_music.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.ui.LocalAuthPreferences
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music.ui.components.CreatePlaylistDialog
import top.stellortus.stellar_music.ui.components.PlaylistItem
import top.stellortus.stellar_music.ui.components.PlaylistToolbar
import top.stellortus.stellar_music.ui.components.StellarAlertDialog
import top.stellortus.stellar_music.ui.components.StellarButton
import top.stellortus.stellar_music.ui.components.StellarButtonKind
import top.stellortus.stellar_music.ui.components.StellarEmptyState
import top.stellortus.stellar_music.ui.components.StellarLoadingIndicator
import top.stellortus.stellar_music.ui.components.StellarSearchField
import top.stellortus.stellar_music.ui.components.StellarText
import top.stellortus.stellar_music.ui.components.StellarTextField
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.CreatePlaylistRequest
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

/**
 * 歌单列表页：显示所有人的歌单（不区分 owner），支持重命名、删除。
 * 状态全部用 remember 保存在内存里，不做本地持久化。
 */
class PlaylistListState {
    val playlists = mutableStateOf<List<Playlist>>(emptyList())
    var query by mutableStateOf("")
    var loaded = false
}

@Composable
fun PlaylistListScreen(
    pageState: PlaylistListState,
    listState: LazyListState,
    onOpenPlaylist: (Int) -> Unit,
    onPlayPlaylist: (Int) -> Unit,
    onSearch: (String) -> Unit,
    currentPlaylistId: Int? = null,
    isPlaying: Boolean = false,
    controlFeedbackVersion: Long = 0L,
) {
    val context = LocalContext.current
    val userId = LocalAuthPreferences.current.userId
    val scope = rememberCoroutineScope()

    var playlists by pageState.playlists
    var isLoading by remember { mutableStateOf(!pageState.loaded) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var createPublic by remember { mutableStateOf(false) }

    var renameTarget by remember { mutableStateOf<Playlist?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Playlist?>(null) }

    LaunchedEffect(reloadTick) {
        if (reloadTick == 0 && pageState.loaded) return@LaunchedEffect
        isLoading = true
        errorMessage = null
        runCatching { PlaylistApi.listPlaylists() }
            .onSuccess { playlists = it; pageState.loaded = true }
            .onFailure { errorMessage = it.toPlaylistMessage(); ToastManager.makeText(context, it.toPlaylistMessage()) }
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StellarSearchField(pageState.query, { pageState.query = it }, placeholder = "搜索歌单",
            onSearch = { value -> value.trim().takeIf { it.isNotEmpty() }?.let(onSearch) },
            modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap))
        PlaylistToolbar(onCreate = {
            createName = ""
            createPublic = false
            showCreate = true
        }, enabled = !submitting)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StellarLoadingIndicator()
                }

                errorMessage != null -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    StellarButton(kind = StellarButtonKind.Outlined,
                        onClick = { reloadTick++ },
                        modifier = Modifier.padding(top = StellarTheme.spacing.itemGap)
                    ) {
                        StellarText("重试")
                    }
                }

                playlists.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StellarEmptyState("还没有歌单")
                }

                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = StellarTheme.spacing.page, end = StellarTheme.spacing.page, bottom = StellarTheme.spacing.page
                )) {
                    itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { _, playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onOpen = { onOpenPlaylist(playlist.id) },
                            onPlay = { onPlayPlaylist(playlist.id) },
                            isCurrent = currentPlaylistId == playlist.id,
                            isPlaying = isPlaying,
                            controlFeedbackVersion = controlFeedbackVersion,
                            onRename = {
                                renameTarget = playlist
                                renameText = playlist.name
                            },
                            onDelete = { deleteTarget = playlist },
                            actionsEnabled = !submitting && !isLoading,
                            onToggleVisibility = if (playlist.ownerId == userId) {
                                {
                                    if (!submitting) {
                                        submitting = true
                                        scope.launch {
                                            runCatching {
                                                PlaylistApi.updatePlaylist(playlist.id,
                                                    UpdatePlaylistRequest(public = !playlist.public))
                                            }.onSuccess {
                                                playlists = playlists.map {
                                                    if (it.id == playlist.id) it.copy(public = !playlist.public) else it
                                                }
                                                reloadTick++
                                            }.onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                                            submitting = false
                                        }
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    if (showCreate) CreatePlaylistDialog(
        name = createName, onNameChange = { createName = it },
        isPublic = createPublic, onPublicChange = { createPublic = it },
        submitting = submitting, onDismiss = { showCreate = false },
        onCreate = {
            if (!submitting && createName.isNotBlank()) {
                val request = CreatePlaylistRequest(name = createName.trim(), public = createPublic)
                submitting = true
                scope.launch {
                    runCatching { PlaylistApi.createPlaylist(request) }
                        .onSuccess { showCreate = false; reloadTick++ }
                        .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                    submitting = false
                }
            }
        }
    )

    renameTarget?.let { target ->
        StellarAlertDialog(
            onDismissRequest = { if (!submitting) renameTarget = null },
            title = "重命名歌单",
            text = {
                StellarTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = "歌单名称",
                    singleLine = true
                )
            },
            confirmButton = {
                StellarButton(kind = StellarButtonKind.Secondary,
                    enabled = renameText.isNotBlank() && !submitting,
                    onClick = {
                        val name = renameText.trim()
                        scope.launch {
                            submitting = true
                            runCatching {
                                PlaylistApi.updatePlaylist(target.id,
                                    UpdatePlaylistRequest(name = name, public = target.public)
                                )
                            }
                                .onSuccess {
                                    renameTarget = null
                                    reloadTick++
                                }
                                .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                            submitting = false
                        }
                    }
                ) {
                    StellarText("保存")
                }
            },
            dismissButton = {
                StellarButton(kind = StellarButtonKind.Text, onClick = { renameTarget = null }, enabled = !submitting) { StellarText("取消") }
            }
        )
    }

    deleteTarget?.let { target ->
        StellarAlertDialog(
            onDismissRequest = { if (!submitting) deleteTarget = null },
            title = "删除歌单",
            text = { StellarText("确定删除「${target.name}」吗？歌单内的歌曲不会被删除。") },
            confirmButton = {
                StellarButton(kind = StellarButtonKind.Danger,
                    enabled = !submitting,
                    onClick = {
                        scope.launch {
                            submitting = true
                            runCatching { PlaylistApi.deletePlaylist(target.id) }
                                .onSuccess {
                                    deleteTarget = null
                                    reloadTick++
                                }
                                .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                            submitting = false
                        }
                    }
                ) {
                    StellarText("删除")
                }
            },
            dismissButton = {
                StellarButton(kind = StellarButtonKind.Text, onClick = { deleteTarget = null }, enabled = !submitting) { StellarText("取消") }
            }
        )
    }
}
