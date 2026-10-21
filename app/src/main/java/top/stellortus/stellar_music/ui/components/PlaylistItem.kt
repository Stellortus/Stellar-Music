package top.stellortus.stellar_music.ui.components

import top.stellortus.stellar_music.ui.theme.StellarTheme

import androidx.compose.foundation.clickable
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
    onToggleVisibility: (() -> Unit)? = null,
    actionsEnabled: Boolean = true,
    showActions: Boolean = true
) {
    val updatedAt = remember(playlist.updatedAt) { formatDateTime(playlist.updatedAt) }
    var menuExpanded by remember { mutableStateOf(false) }
    StellarCard( kind = StellarSurfaceKind.Card,
        modifier = Modifier.fillMaxWidth().padding(vertical = StellarTheme.spacing.gap)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
                .padding(horizontal = StellarTheme.spacing.section, vertical = StellarTheme.spacing.itemGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
        ) {
            CoverPlaceholder(CoverPlaceholderSize.Medium)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)) {
                StellarText(playlist.name, role = StellarTextRole.ItemTitle,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                StellarText(if (playlist.public) "公开" else "私有", role = StellarTextRole.Caption, tone = StellarTone.Secondary)
                StellarText("${playlist.trackCount} 首歌曲 · 更新于 $updatedAt",
                    tone = StellarTone.Secondary,
                    role = StellarTextRole.Caption)
            }
            if (showActions) Box {
                StellarButton(kind = StellarButtonKind.Text, enabled = actionsEnabled, onClick = { menuExpanded = true }) { StellarText("更多") }
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
                PlaylistItem(Playlist(1, 1, "留给深夜的声音 · 一份很长的歌单名称", null, null, false, 0L, 0L, 12), {}, {}, {}, onToggleVisibility = {})
                PlaylistItem(Playlist(2, 2, "公开歌单", null, null, true, 0L, 0L, 3), {}, {}, {}, actionsEnabled = false)
            }
        }
    }
}
