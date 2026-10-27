package top.stellortus.stellar_music.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Track

/** Keeps the queue in the screen's layout, anchored to the actual player surface. */
@Composable
internal fun PlaybackQueueHost(
    expanded: Boolean,
    tracks: List<Track>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (playerAnchorModifier: Modifier) -> Unit,
) {
    var rootTop by remember { mutableFloatStateOf(0f) }
    var anchorTop by remember { mutableFloatStateOf(Float.NaN) }
    BackHandler(enabled = expanded, onBack = onDismiss)
    BoxWithConstraints(modifier.fillMaxSize().onGloballyPositioned { rootTop = it.boundsInWindow().top }) {
        val rootHeight = maxHeight
        val density = LocalDensity.current
        content(Modifier.onGloballyPositioned { anchorTop = it.boundsInWindow().top })
        if (anchorTop.isFinite()) {
            val availableHeight = with(density) { (anchorTop - rootTop).toDp() }
                .coerceIn(0.dp, rootHeight)
            val panelHeight = minOf(rootHeight * 0.65f, availableHeight)
            Box(Modifier.fillMaxWidth().height(availableHeight).clipToBounds()) {
                AnimatedVisibility(expanded, enter = fadeIn(tween(240)), exit = fadeOut(tween(240))) {
                    Box(Modifier.fillMaxSize()
                        .background(StellarTheme.colors.overlayScrim)
                        .semantics { contentDescription = "收起当前播放列表" }
                        .clickable(onClick = onDismiss))
                }
                AnimatedVisibility(
                    visible = expanded,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(tween(240)) { it },
                    exit = slideOutVertically(tween(240)) { it },
                ) {
                    PlaybackQueueSheet(
                        tracks, currentIndex, onSelect, onDismiss,
                        Modifier.fillMaxWidth().padding(horizontal = StellarTheme.spacing.section)
                            .heightIn(max = panelHeight),
                    )
                }
            }
        }
    }
}
