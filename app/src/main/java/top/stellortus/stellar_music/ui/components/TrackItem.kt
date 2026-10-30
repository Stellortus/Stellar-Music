package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholder
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholderSize
import top.stellortus.stellar_music.ui.widgets.StellarCard
import top.stellortus.stellar_music.ui.widgets.StellarDivider
import top.stellortus.stellar_music.ui.widgets.StellarSurfaceKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone
import top.stellortus.stellar_music_common.dto.Track

@Composable
fun TrackItem(
    track: Track, onClick: () -> Unit, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, isCurrent: Boolean = false,
    showDivider: Boolean = false
) {
    Column(modifier.fillMaxWidth()) {
        StellarCard(
            modifier = Modifier.fillMaxWidth(),
            kind = if (isCurrent) StellarSurfaceKind.Selected else StellarSurfaceKind.Item
        ) {
            Row(
                Modifier
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    .padding(
                        horizontal = StellarTheme.spacing.itemGap,
                        vertical = StellarTheme.spacing.trackVerticalPadding
                    ), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)
            ) {
                CoverPlaceholder(CoverPlaceholderSize.Medium)
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)
                ) {
                    StellarText(
                        track.title,
                        role = StellarTextRole.ItemTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StellarText(
                        track.artists.joinToString(" / ").ifBlank { "未知歌手" },
                        role = StellarTextRole.BodySmall,
                        tone = StellarTone.Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (showDivider) {
            StellarDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}
