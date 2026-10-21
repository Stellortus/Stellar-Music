package top.stellortus.stellar_music.ui.components

import top.stellortus.stellar_music.ui.theme.StellarTheme

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import top.stellortus.stellar_music_common.dto.Track

@Composable
fun TrackItem(track: Track, onClick: () -> Unit, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, isCurrent: Boolean = false,
    showDivider: Boolean = false) {
    Column(modifier.fillMaxWidth()) {
        StellarCard(modifier = Modifier.fillMaxWidth(),
            kind = if (isCurrent) StellarSurfaceKind.Selected else StellarSurfaceKind.Item) {
            Row(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = StellarTheme.spacing.itemGap, vertical = StellarTheme.spacing.trackVerticalPadding), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)) {
                CoverPlaceholder(CoverPlaceholderSize.Medium)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)) {
                    StellarText(track.title, role = StellarTextRole.ItemTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StellarText(track.artists.joinToString(" / ").ifBlank { "未知歌手" }, role = StellarTextRole.BodySmall,
                        tone = StellarTone.Secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (showDivider) {
            StellarDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Single Track Light", widthDp = 360)
@androidx.compose.ui.tooling.preview.Preview(name = "Single Track Dark", widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SingleTrackPreview() {
    top.stellortus.stellar_music.ui.theme.StellarMusicTheme {
        Column(Modifier.padding(StellarTheme.spacing.page)) {
            TrackItem(
                track = Track(1, "星河漫游", listOf("Stellar"), "music.mp3", "Listener", "", "", 0L),
                onClick = {}, isCurrent = true
            )
        }
    }
}
