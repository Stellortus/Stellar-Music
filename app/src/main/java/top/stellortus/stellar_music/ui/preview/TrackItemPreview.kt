package top.stellortus.stellar_music.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.ui.components.TrackItem
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Track

@Preview(name = "Single Track Light", widthDp = 360)
@Preview(name = "Single Track Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SingleTrackPreview() {
    StellarMusicTheme {
        Column(Modifier.padding(StellarTheme.spacing.page)) {
            TrackItem(
                track = Track(1, "星河漫游", listOf("Stellar"), "music.mp3", "Listener", "", "", 0L),
                onClick = {}, isCurrent = true
            )
        }
    }
}
