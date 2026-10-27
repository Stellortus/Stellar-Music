package top.stellortus.stellar_music.ui.components

import top.stellortus.stellar_music.ui.theme.StellarTheme

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music_common.dto.Playlist

import top.stellortus.stellar_music.ui.screens.playlist.formatDateTime

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit,
    onToggleVisibility: (() -> Unit)? = null,
    actionsEnabled: Boolean = true,
    showActions: Boolean = true,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    controlFeedbackVersion: Long = 0L,
) {
    val updatedAt = remember(playlist.updatedAt) { formatDateTime(playlist.updatedAt) }
    var menuExpanded by remember(playlist.id) { mutableStateOf(false) }
    val onLongClick: (() -> Unit)? = if (showActions && actionsEnabled) {
        { menuExpanded = true }
    } else null
    StellarCard( kind = StellarSurfaceKind.Card,
        modifier = Modifier.fillMaxWidth().padding(vertical = StellarTheme.spacing.gap)) {
        Box {
            Row(
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    interactionSource = null, indication = null,
                    onClick = onOpen, onLongClick = onLongClick,
                    onLongClickLabel = if (onLongClick != null) "歌单操作" else null)
                    .padding(horizontal = StellarTheme.spacing.section, vertical = StellarTheme.spacing.itemGap),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
            ) {
                PlaylistPlayCover(playlist.name, onPlay, onLongClick = onLongClick,
                    isCurrent = isCurrent, isPlaying = isPlaying,
                    controlFeedbackVersion = controlFeedbackVersion)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)) {
                    StellarText(playlist.name, role = StellarTextRole.ItemTitle,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StellarText(if (playlist.public) "公开" else "私有", role = StellarTextRole.Caption, tone = StellarTone.Secondary)
                    StellarText("${playlist.trackCount} 首歌曲 · 更新于 $updatedAt",
                        tone = StellarTone.Secondary,
                        role = StellarTextRole.Caption)
                }
            }
            if (showActions) {
                PlaylistActionsMenu(
                    expanded = menuExpanded && actionsEnabled, onDismiss = { menuExpanded = false },
                    onRename = onRename, onDelete = onDelete,
                    isPublic = playlist.public, onToggleVisibility = onToggleVisibility
                )
            }
        }
    }
}

@Preview(name = "Playlist Light", widthDp = 360)
@Preview(name = "Playlist Dark", widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Playlist Large Text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun PlaylistPreview() {
    top.stellortus.stellar_music.ui.theme.StellarMusicTheme {
        Column {
            StellarSearchField("", {}, placeholder = "搜索歌单",
                modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                    top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap))
            PlaylistToolbar({})
            Column(Modifier.padding(horizontal = StellarTheme.spacing.page)) {
                PlaylistItem(Playlist(1, 1, "留给深夜的声音 · 一份很长的歌单名称", null, null, false, 0L, 0L, 12), {}, {}, {}, {}, onToggleVisibility = {})
                PlaylistItem(Playlist(2, 2, "公开歌单", null, null, true, 0L, 0L, 3), {}, {}, {}, {}, actionsEnabled = false)
            }
        }
    }
}
