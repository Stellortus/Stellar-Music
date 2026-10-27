package top.stellortus.stellar_music.ui.screens.playlist

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music_common.dto.PlaylistDetail
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

@OptIn(UnstableApi::class)
@Composable
fun PlaylistDetailScreen(playlistId: Int, player: MusicPlayerState, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTrack by remember { mutableStateOf<top.stellortus.stellar_music_common.dto.Track?>(null) }

    var detail by remember { mutableStateOf<PlaylistDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    LaunchedEffect(playlistId, reloadTick) {
        isLoading = true
        errorMessage = null
        runCatching { PlaylistApi.getPlaylist(playlistId) }
            .onSuccess { detail = it }
            .onFailure {
                errorMessage = it.toPlaylistMessage()
                ToastManager.makeText(context, it.toPlaylistMessage())
            }
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(StellarTheme.spacing.page),
        verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap)
        ) {
            StellarButton(kind = StellarButtonKind.Text, onClick = onBack) { StellarText("返回") }
            StellarText(
                text = "歌单详情",
                role = StellarTextRole.Title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            StellarButton(kind = StellarButtonKind.Text,
                enabled = detail != null && !submitting,
                onClick = {
                    renameText = detail?.name.orEmpty()
                    showRenameDialog = true
                }
            ) {
                StellarText("编辑")
            }
        }

        detail?.let { current ->
            Row(horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.section), verticalAlignment = Alignment.CenterVertically) {
                PlaylistPlayCover(current.name,
                    onPlay = { player.togglePlaylist(playlistId, current.tracks.distinctBy { it.id }) },
                    isCurrent = player.currentPlaylistId == playlistId,
                    isPlaying = player.isPlaybackRequested,
                    controlFeedbackVersion = player.playlistControlFeedbackVersion,
                    size = CoverPlaceholderSize.Large, enabled = !isLoading && errorMessage == null)
                StellarText(current.name, role = StellarTextRole.Title, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            val updatedAt = remember(current.updatedAt) { formatDateTime(current.updatedAt) }
            StellarText(
                text = "${current.tracks.size} 首歌曲 · 更新于 $updatedAt",
                role = StellarTextRole.Caption,
                tone = StellarTone.Secondary
            )
        }

        val current = detail
        val currentError = errorMessage
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StellarLoadingIndicator()
            }

            currentError != null -> Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
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

            current == null || current.tracks.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StellarEmptyState("歌单里还没有歌曲")
            }

            else -> {
                val tracks = current.tracks.distinctBy { it.id }
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                        TrackItem(
                            track = track,
                            onClick = { player.playPlaylist(tracks, index, sourcePlaylistId = playlistId) },
                            onLongClick = { selectedTrack = track },
                            isCurrent = player.currentTrack?.id == track.id,
                            showDivider = index < tracks.lastIndex
                        )
                    }
                }
            }
        }

        StellarButton(kind = StellarButtonKind.Primary,
            onClick = { showAddDialog = true },
            enabled = detail != null && !submitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            StellarText("添加歌曲")
        }
    }

    selectedTrack?.let { track ->
        TrackDetails(track) { selectedTrack = null }
    }
    if (showAddDialog) {
        AddTracksDialog(
            existingTrackIds = detail?.tracks?.map { it.id }?.toSet().orEmpty(),
            onDismiss = { showAddDialog = false },
            onConfirm = { trackIds ->
                scope.launch {
                    submitting = true
                    runCatching { PlaylistApi.addTracks(playlistId, trackIds) }
                        .onSuccess {
                            showAddDialog = false
                            reloadTick++
                        }
                        .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                    submitting = false
                }
            }
        )
    }

    if (showRenameDialog) {
        StellarAlertDialog(
            onDismissRequest = { if (!submitting) showRenameDialog = false },
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
                                // Detail DTO omits public; read the current summary rather than using a default.
                                val summary = PlaylistApi.listPlaylists().find { it.id == playlistId }
                                    ?: error("无法读取歌单公开状态，请稍后重试")
                                PlaylistApi.updatePlaylist(playlistId,
                                    UpdatePlaylistRequest(name = name, public = summary.public)
                                )
                            }
                                .onSuccess {
                                    showRenameDialog = false
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
                StellarButton(kind = StellarButtonKind.Text, onClick = { showRenameDialog = false }, enabled = !submitting) { StellarText("取消") }
            }
        )
    }
}
