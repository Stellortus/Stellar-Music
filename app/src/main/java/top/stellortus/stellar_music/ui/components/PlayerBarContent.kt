package top.stellortus.stellar_music.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholder
import top.stellortus.stellar_music.ui.widgets.CoverPlaceholderSize
import top.stellortus.stellar_music.ui.widgets.StellarCard
import top.stellortus.stellar_music.ui.widgets.StellarIcon
import top.stellortus.stellar_music.ui.widgets.StellarIconButton
import top.stellortus.stellar_music.ui.widgets.StellarSurfaceKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone
import top.stellortus.stellar_music_common.dto.Track
import kotlin.math.roundToInt

@Composable
internal fun PlayerBarContent(
    track: Track,
    isPlaying: Boolean,
    positionMs: Float,
    durationMs: Float,
    onTogglePause: () -> Unit,
    onSeekPreview: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenQueue: () -> Unit = {},
    cardAnchorModifier: Modifier = Modifier,
) {
    val openQueue = rememberUpdatedState(onOpenQueue)
    val swipeThreshold = with(LocalDensity.current) { 48.dp.toPx() }
    val coverRotation = remember(track.id) { Animatable(0f) }
    val rotationDuration = StellarTheme.motion.coverRotationDurationMillis
    LaunchedEffect(isPlaying, track.id, rotationDuration) {
        if (isPlaying) {
            while (isActive) {
                val remainingDuration = (rotationDuration * (1f - coverRotation.value / 360f))
                    .roundToInt().coerceAtLeast(1)
                coverRotation.animateTo(360f, tween(remainingDuration, easing = LinearEasing))
                coverRotation.snapTo(0f)
            }
        }
    }
    StellarCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = StellarTheme.spacing.section,
                vertical = StellarTheme.spacing.gap
            )
            .then(cardAnchorModifier),
        kind = StellarSurfaceKind.Player
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = StellarTheme.spacing.section,
                vertical = StellarTheme.spacing.gap
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap)
            ) {
                Box(
                    modifier = Modifier
                        .size(StellarTheme.sizes.touch)
                        .clip(StellarTheme.shapes.item)
                        .semantics {
                            contentDescription = if (isPlaying) "专辑封面，暂停" else "专辑封面，播放"
                        }
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            role = Role.Button,
                            onClickLabel = if (isPlaying) "暂停" else "播放",
                            onClick = onTogglePause
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CoverPlaceholder(
                        CoverPlaceholderSize.Small,
                        Modifier
                            .graphicsLayer { rotationZ = coverRotation.value }
                            .clip(StellarTheme.shapes.avatar)
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .semantics {
                            customActions = listOf(CustomAccessibilityAction("打开当前播放列表") {
                                openQueue.value()
                                true
                            })
                        }
                        .pointerInput(swipeThreshold) {
                            var upwardDrag = 0f
                            var opened = false
                            detectVerticalDragGestures(
                                onDragStart = { upwardDrag = 0f; opened = false },
                                onVerticalDrag = { change, amount ->
                                    change.consume()
                                    upwardDrag -= amount
                                    if (!opened && upwardDrag >= swipeThreshold) {
                                        opened = true
                                        openQueue.value()
                                    }
                                },
                            )
                        }
                ) {
                    PlayerTrackTitle(track.title, track.id)
                    StellarText(
                        track.artists.joinToString(" / ").ifBlank { "未知歌手" },
                        role = StellarTextRole.Micro,
                        tone = StellarTone.Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StellarIconButton(onClick = onTogglePause) {
                    StellarIcon(
                        painter = painterResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }
            }
            Row {
                SeekBar(
                    value = positionMs,
                    durationMs = durationMs,
                    onValueChange = onSeekPreview,
                    onValueChangeFinished = onSeekFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StellarTheme.sizes.seekHeight),
                )

                StellarText(
                    "${formatTime(positionMs)}/${formatTime(durationMs)}",
                    role = StellarTextRole.Micro,
                    tone = StellarTone.Secondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SeekBar(
    value: Float,
    durationMs: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxValue = durationMs.coerceAtLeast(1f)
    val fraction = (value / maxValue).coerceIn(0f, 1f)
    val inactiveTrackColor = StellarTheme.colors.progressTrack
    val activeTrackColor = StellarTheme.colors.accent
    val thumbColor = StellarTheme.colors.accent
    val dimensions = StellarTheme.sizes

    Canvas(
        modifier = modifier
            .pointerInput(maxValue) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    if (width > 0f) {
                        onValueChange((offset.x / width).coerceIn(0f, 1f) * maxValue)
                        onValueChangeFinished()
                    }
                }
            }
            .pointerInput(maxValue) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            onValueChange((offset.x / width).coerceIn(0f, 1f) * maxValue)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            onValueChange(
                                (change.position.x / width).coerceIn(0f, 1f) * maxValue
                            )
                        }
                    },
                    onDragEnd = { onValueChangeFinished() },
                    onDragCancel = { onValueChangeFinished() },
                )
            }
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..maxValue)
                setProgress { target ->
                    onValueChange(target.coerceIn(0f, maxValue))
                    onValueChangeFinished()
                    true
                }
            }
    ) {
        val trackHeight = dimensions.progress.toPx()
        val y = size.height / 2f
        val thumbRadius = minOf(dimensions.thumb.toPx(), size.width / 2f)
        val thumbX = (fraction * size.width).coerceIn(thumbRadius, size.width - thumbRadius)

        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = activeTrackColor,
            start = Offset(0f, y),
            end = Offset(thumbX, y),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round,
        )
        drawCircle(color = thumbColor, radius = thumbRadius, center = Offset(thumbX, y))
    }
}

private fun formatTime(ms: Float): String {
    val totalSeconds = (ms / 1000f).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
