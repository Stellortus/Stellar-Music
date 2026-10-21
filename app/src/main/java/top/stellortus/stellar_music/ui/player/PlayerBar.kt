package top.stellortus.stellar_music.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellar_music.ui.components.PlayerBarContent

@OptIn(UnstableApi::class)
@Composable
fun PlayerBar(state: MusicPlayerState, modifier: Modifier = Modifier) {
    val track = state.currentTrack ?: return
    PlayerBarContent(track, state.isPlaying, state.dragPositionMs ?: state.positionMs, state.durationMs,
        state::togglePause, state::onSeekPreview, state::onSeekFinished, modifier)
}
