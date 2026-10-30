package top.stellortus.stellar_music.ui.components

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
import top.stellortus.stellar_music.ui.screens.playlist.formatDateTime
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.StellarCard
import top.stellortus.stellar_music.ui.widgets.StellarSurfaceKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone
import top.stellortus.stellar_music_common.dto.Playlist

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
    StellarCard(
        kind = StellarSurfaceKind.Card,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = StellarTheme.spacing.gap)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = null, indication = null,
                        onClick = onOpen, onLongClick = onLongClick,
                        onLongClickLabel = if (onLongClick != null) "歌单操作" else null
                    )
                    .padding(
                        horizontal = StellarTheme.spacing.section,
                        vertical = StellarTheme.spacing.itemGap
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
            ) {
                PlaylistPlayCover(
                    playlist.name, onPlay, onLongClick = onLongClick,
                    isCurrent = isCurrent, isPlaying = isPlaying,
                    controlFeedbackVersion = controlFeedbackVersion
                )
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)
                ) {
                    StellarText(
                        playlist.name, role = StellarTextRole.ItemTitle,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    StellarText(
                        if (playlist.public) "公开" else "私有",
                        role = StellarTextRole.Caption,
                        tone = StellarTone.Secondary
                    )
                    StellarText(
                        "${playlist.trackCount} 首歌曲 · 更新于 $updatedAt",
                        tone = StellarTone.Secondary,
                        role = StellarTextRole.Caption
                    )
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
