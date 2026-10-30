package top.stellortus.stellar_music.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.ui.components.PlayerBarContent
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music_common.dto.Track

@Preview(name = "Player Light", widthDp = 360)
@Preview(
    name = "Player Dark",
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Player Large Text",
    widthDp = 320,
    fontScale = 1.5f
)
@Composable
private fun PlayerPreview() {
    StellarMusicTheme {
        PlayerBarContent(
            Track(
                1,
                "星河漫游 · 一首很长的歌名",
                listOf("Stellar"),
                "music.mp3",
                "Listener",
                "",
                "",
                0L
            ),
            true, 45000f, 210000f, {}, {}, {}
        )
    }
}
