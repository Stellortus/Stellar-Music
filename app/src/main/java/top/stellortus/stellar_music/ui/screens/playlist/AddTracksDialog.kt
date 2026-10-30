package top.stellortus.stellar_music.ui.screens.playlist

import top.stellortus.stellar_music.ui.widgets.StellarAlertDialog
import top.stellortus.stellar_music.ui.widgets.StellarButton
import top.stellortus.stellar_music.ui.widgets.StellarButtonKind
import top.stellortus.stellar_music.ui.widgets.StellarEmptyState
import top.stellortus.stellar_music.ui.widgets.StellarLoadingIndicator
import top.stellortus.stellar_music.ui.widgets.StellarText

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.db.manager.TrackManager
import top.stellortus.stellar_music.utils.ToastManager
import top.stellortus.stellar_music_common.dto.Track

/**
 * 添加歌曲多选对话框。歌曲列表复用现有的歌曲列表接口（内部按分页拉全量）。
 * 已经在歌单里的歌曲标记出来并禁用勾选。
 */
@Composable
fun AddTracksDialog(
    existingTrackIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    var reloadTick by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reloadTick) {
        isLoading = true
        errorMessage = null
        runCatching { TrackManager.getAllMusicList() }
            .onSuccess { tracks = it.distinctBy { track -> track.id } }
            .onFailure { errorMessage = it.toPlaylistMessage(); ToastManager.makeText(it.toPlaylistMessage()) }
        isLoading = false
    }

    StellarAlertDialog(
        onDismissRequest = onDismiss,
        title = "添加歌曲",
        text = {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = StellarTheme.sizes.loadingArea),
                    contentAlignment = Alignment.Center
                ) {
                    StellarLoadingIndicator()
                }

                errorMessage != null -> StellarButton(kind = StellarButtonKind.Text, onClick = { reloadTick++ }) { StellarText("重试") }

                tracks.isEmpty() -> StellarEmptyState("曲库中还没有歌曲")

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = StellarTheme.sizes.dialogHeight),
                    verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)
                ) {
                    items(tracks, key = { it.id }) { track ->
                        val alreadyAdded = track.id in existingTrackIds
                        val checked = alreadyAdded || track.id in selectedIds
                        TrackChoiceRow(track, selected = checked, alreadyAdded = alreadyAdded) {
                            selectedIds = if (track.id in selectedIds) selectedIds - track.id else selectedIds + track.id
                        }
                    }
                }
            }
        },
        confirmButton = {
            StellarButton(kind = StellarButtonKind.Secondary,
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty() && !isLoading
            ) {
                StellarText("添加")
            }
        },
        dismissButton = {
            StellarButton(kind = StellarButtonKind.Text, onClick = onDismiss) { StellarText("取消") }
        }
    )
}
