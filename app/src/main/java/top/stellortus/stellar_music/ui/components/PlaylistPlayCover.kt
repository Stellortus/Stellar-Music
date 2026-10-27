package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarTheme

@Composable
fun PlaylistPlayCover(
    name: String,
    onPlay: () -> Unit,
    size: CoverPlaceholderSize = CoverPlaceholderSize.Medium,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    controlFeedbackVersion: Long = 0L,
) {
    val controlsAlpha = remember { Animatable(if (isCurrent && isPlaying) 1f else 0f) }
    // Do not replay an old action when a cover enters composition during navigation or scrolling.
    var lastFeedbackVersion by remember { mutableLongStateOf(controlFeedbackVersion) }
    var wasPlaying by remember { mutableStateOf(isPlaying) }
    LaunchedEffect(isCurrent, isPlaying, controlFeedbackVersion) {
        val newAction = controlFeedbackVersion != lastFeedbackVersion
        val becamePaused = wasPlaying && !isPlaying
        wasPlaying = isPlaying
        lastFeedbackVersion = controlFeedbackVersion
        if (!isCurrent) {
            controlsAlpha.snapTo(0f)
        } else if (isPlaying) {
            controlsAlpha.snapTo(1f)
        } else if (newAction || becamePaused) {
            controlsAlpha.snapTo(1f)
            val shownAt = withFrameNanos { it }
            do {
                val elapsed = withFrameNanos { it } - shownAt
            } while (elapsed < 2_000_000_000L)
            controlsAlpha.animateTo(0f, tween(300))
        }
    }
    val actionLabel = when {
        !isCurrent -> "循环播放歌单：$name"
        isPlaying -> "暂停歌单：$name"
        else -> "继续播放歌单：$name"
    }
    Box(
        Modifier.semantics { contentDescription = actionLabel }.combinedClickable(
            interactionSource = null,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClickLabel = actionLabel,
            onLongClickLabel = if (onLongClick != null) "歌单操作" else null,
            onClick = onPlay,
            onLongClick = onLongClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        CoverPlaceholder(size)
        if (isCurrent && controlsAlpha.value > 0f) {
            val iconSize = if (size == CoverPlaceholderSize.Large) {
                StellarTheme.sizes.coverIconMedium
            } else StellarTheme.sizes.coverIconSmall
            Box(
                Modifier.testTag("playlistPlaybackOverlay")
                    .matchParentSize()
                    .clip(StellarTheme.shapes.cover)
                    .graphicsLayer { alpha = controlsAlpha.value }
                    .background(StellarTheme.colors.overlayScrim),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize).testTag("playlistPlaybackIcon"),
                    tint = Color.White,
                )
            }
        }
    }
}
