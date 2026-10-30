package top.stellortus.stellar_music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.components.TrackItem
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Track

/** Queue content embedded above the player bar; positioning belongs to the host. */
@Composable
internal fun PlaybackQueueSheet(
    tracks: List<Track>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        if (currentIndex in tracks.indices) listState.scrollToItem(currentIndex)
    }
    val dismissQueue = rememberUpdatedState(onDismiss)
    val swipeThreshold = with(LocalDensity.current) { 48.dp.toPx() }
    Surface(
        modifier = modifier.semantics {
            contentDescription = "当前播放列表面板"
            dismiss { dismissQueue.value(); true }
        },
        shape = StellarTheme.shapes.sheet,
        color = StellarTheme.colors.overlayBackground,
        contentColor = StellarTheme.colors.textPrimary,
        tonalElevation = StellarTheme.sizes.cardElevation,
    ) {
        Column {
            Column(
                Modifier.fillMaxWidth()
                    .semantics { contentDescription = "下滑收起播放列表" }
                    .pointerInput(swipeThreshold) {
                        var downwardDrag = 0f
                        var dismissed = false
                        detectVerticalDragGestures(
                            onDragStart = { downwardDrag = 0f; dismissed = false },
                            onVerticalDrag = { change, amount ->
                                change.consume()
                                downwardDrag += amount
                                if (!dismissed && downwardDrag >= swipeThreshold) {
                                    dismissed = true
                                    dismissQueue.value()
                                }
                            },
                        )
                    }
                    .padding(StellarTheme.spacing.section),
                verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap),
            ) {
                Box(Modifier.fillMaxWidth().height(12.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(32.dp, 4.dp)
                        .background(StellarTheme.colors.progressTrack, StellarTheme.shapes.item))
                }
                StellarText("当前播放列表（${tracks.size}）", role = StellarTextRole.ItemTitle)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                contentPadding = PaddingValues(
                    start = StellarTheme.spacing.section,
                    end = StellarTheme.spacing.section,
                    bottom = StellarTheme.spacing.section,
                ),
                verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap),
            ) {
                itemsIndexed(tracks) { index, track ->
                    TrackItem(
                        track = track,
                        onClick = { onSelect(index) },
                        isCurrent = index == currentIndex,
                        modifier = Modifier.semantics { selected = index == currentIndex },
                    )
                }
            }
        }
    }
}
