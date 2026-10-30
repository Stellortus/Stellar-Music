package top.stellortus.stellar_music.ui.components

import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import top.stellortus.stellar_music.ui.theme.StellarTheme
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/** A single copy of the title moves between its start and end; it never wraps around. */
@Composable
internal fun PlayerTrackTitle(title: String, trackId: Int) {
    var viewportWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember(title, trackId) { mutableIntStateOf(0) }
    val offset = remember(title, trackId) { Animatable(0f) }
    val overflow = (textWidth - viewportWidth).coerceAtLeast(0)
    val pixelsPerSecond = with(LocalDensity.current) { StellarTheme.motion.titleScrollSpeed.toPx() }
    val pauseMillis = StellarTheme.motion.titleScrollPauseMillis

    LaunchedEffect(offset, overflow, viewportWidth, pixelsPerSecond, pauseMillis) {
        offset.snapTo(0f)
        if (viewportWidth > 0 && overflow > 0) {
            val duration = (overflow / pixelsPerSecond * 1000).roundToInt().coerceAtLeast(1)
            while (isActive) {
                delay(pauseMillis.milliseconds)
                offset.animateTo(overflow.toFloat(), tween(duration, easing = LinearEasing))
                delay(pauseMillis.milliseconds)
                offset.animateTo(0f, tween(duration, easing = LinearEasing))
            }
        }
    }

    Box(Modifier.fillMaxWidth().clipToBounds().onSizeChanged { viewportWidth = it.width }) {
        StellarText(
            title,
            modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true)
                .onSizeChanged { textWidth = it.width }
                .graphicsLayer { translationX = -offset.value },
            role = StellarTextRole.Action,
            maxLines = 1,
            softWrap = false
        )
    }
}
