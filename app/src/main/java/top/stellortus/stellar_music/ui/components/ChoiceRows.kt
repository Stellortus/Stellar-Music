package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholder
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholderSize
import top.stellortus.stellar_music.ui.widgets.StellarCard
import top.stellortus.stellar_music.ui.widgets.StellarCheckbox
import top.stellortus.stellar_music.ui.widgets.StellarSurfaceKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.Track

@Composable
fun TrackChoiceRow(track: Track, selected: Boolean, alreadyAdded: Boolean, onToggle: () -> Unit) {
    StellarCard(kind = if (selected) StellarSurfaceKind.Selected else StellarSurfaceKind.Inset) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(
                    selected,
                    enabled = !alreadyAdded,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() })
                .padding(vertical = StellarTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StellarCheckbox(selected, onCheckedChange = null, enabled = !alreadyAdded)
            Column(Modifier
                .weight(1f)
                .padding(horizontal = StellarTheme.spacing.gap)) {
                StellarText(
                    track.title,
                    role = StellarTextRole.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StellarText(
                    if (alreadyAdded) "已在歌单中" else track.artists.joinToString(" / ")
                        .ifBlank { "未知歌手" },
                    role = StellarTextRole.Caption,
                    tone = StellarTone.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlaylistChoiceRow(playlist: Playlist, enabled: Boolean, onClick: () -> Unit) {
    StellarCard(kind = StellarSurfaceKind.Inset) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(
                    horizontal = StellarTheme.spacing.itemGap,
                    vertical = StellarTheme.spacing.gap
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
        ) {
            CoverPlaceholder(CoverPlaceholderSize.Small)
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.tiny)
            ) {
                StellarText(
                    playlist.name,
                    role = StellarTextRole.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StellarText(
                    "${playlist.trackCount} 首歌曲",
                    role = StellarTextRole.Caption,
                    tone = StellarTone.Secondary
                )
            }
        }
    }
}
