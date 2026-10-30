package top.stellortus.stellar_music.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.ui.screens.TrackList
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.StellarSearchField
import top.stellortus.stellar_music_common.dto.Track

@Preview(name = "Home Light", widthDp = 360, heightDp = 640)
@Preview(name = "Home Dark", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Home Large Text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun HomePreview() {
    StellarMusicTheme {
        Column(Modifier.fillMaxSize()) {
            StellarSearchField("", {}, placeholder = "搜索音乐",
                modifier = Modifier.padding(start = StellarTheme.spacing.page, end = StellarTheme.spacing.page,
                    top = StellarTheme.spacing.page, bottom = StellarTheme.spacing.itemGap))
            TrackList(
                modifier = Modifier.weight(1f),
                tracks = listOf(
                    Track(1, "星河漫游 · 一首很长的歌名用于检查文本截断", listOf("Stellar", "夜航计划"), "music.mp3", "Listener", "", "", 0L),
                    Track(2, "晚风与回声", emptyList(), "echo.mp3", "Listener", "", "", 0L)
                ),
                isLoadingMore = false, endReached = true, onLoadMore = {},
                currentTrackId = 1, onTrackClick = {}, onTrackLongClick = {}
            )
        }
    }
}
