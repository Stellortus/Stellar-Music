package top.stellortus.stellar_music.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellar_music.R

/**
 * 常驻在底部导航栏上方的播放条：曲名 + 播放/暂停 + 进度条 + 时间。
 * 只在 [MusicPlayerState.currentTrack] 非空时出现，由 MainScreen 渲染，因此切换 tab 不会消失。
 */
@UnstableApi
@Composable
fun PlayerBar(state: MusicPlayerState, modifier: Modifier = Modifier) {
    val track = state.currentTrack ?: return

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { state.togglePause() }) {
                    Icon(
                        painter = painterResource(
                            if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (state.isPlaying) "暂停" else "播放"
                    )
                }
                SeekBar(
                    value = state.dragPositionMs ?: state.positionMs,
                    durationMs = state.durationMs,
                    onValueChange = { state.onSeekPreview(it) },
                    onValueChangeFinished = { state.onSeekFinished() },
                    modifier = Modifier.weight(1f).height(24.dp),
                )
                Text(
                    text = "${formatTime(state.dragPositionMs ?: state.positionMs)} / " +
                        formatTime(state.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            state.playbackError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
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
    val inactiveTrackColor = MaterialTheme.colorScheme.secondary
    val activeTrackColor = MaterialTheme.colorScheme.primary
    val thumbColor = MaterialTheme.colorScheme.primary

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
        val trackHeight = 4.dp.toPx()
        val y = size.height / 2f
        val thumbRadius = minOf(6.dp.toPx(), size.width / 2f)
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
