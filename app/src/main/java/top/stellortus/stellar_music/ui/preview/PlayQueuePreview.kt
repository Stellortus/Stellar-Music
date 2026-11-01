package top.stellortus.stellar_music.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.ui.components.PlayerBarContent
import top.stellortus.stellar_music.ui.player.PlaybackQueueHost
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.widgets.StellarScaffold
import top.stellortus.stellar_music_common.dto.Track

@Preview(name = "Play Queue", widthDp = 360, heightDp = 640)
@Composable
fun PlayQueuePreview() {
    val track = remember {
        Track(1, "星河漫游", listOf("Stellar"), "music.mp3", "Listener", "", "", 0L)
    }
    var expanded by remember { mutableStateOf(true) }

    StellarMusicTheme {
        PlaybackQueueHost(
            expanded = expanded,
            tracks = listOf(track),
            currentIndex = 0,
            onSelect = {},
            onDismiss = { expanded = false },
        ) { playerAnchorModifier ->
            StellarScaffold(
                bottomBar = {
                    PlayerBarContent(
                        track = track,
                        isPlaying = false,
                        positionMs = 45000f,
                        durationMs = 210000f,
                        onTogglePause = {},
                        onSeekPreview = {},
                        onSeekFinished = {},
                        onOpenQueue = { expanded = true },
                        cardAnchorModifier = playerAnchorModifier,
                    )
                },
            ) { padding ->
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding))
            }
        }
    }
}
