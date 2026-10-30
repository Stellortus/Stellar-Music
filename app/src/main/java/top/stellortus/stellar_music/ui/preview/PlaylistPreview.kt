package top.stellortus.stellar_music.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.ui.components.PlaylistItem
import top.stellortus.stellar_music.ui.components.PlaylistToolbar
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.StellarSearchField
import top.stellortus.stellar_music_common.dto.Playlist

@Preview(name = "Playlist Light", widthDp = 360)
@Preview(name = "Playlist Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Playlist Large Text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun PlaylistPreview() {
    StellarMusicTheme {
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
