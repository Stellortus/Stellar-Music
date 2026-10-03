package top.stellortus.stellar_music.ui.screens.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music_common.dto.Track

/**
 * 添加歌曲多选对话框。歌曲列表复用现有的歌曲列表接口（内部按分页拉全量）。
 * 已经在歌单里的歌曲标记出来并禁用勾选。
 */
@Composable
fun AddTracksDialog(
    existingTrackIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        runCatching { NetworkService.getAllMusicList() }
            .onSuccess { tracks = it.distinctBy { track -> track.id } }
            .onFailure { errorMessage = it.toPlaylistMessage() }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加歌曲") },
        text = {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                errorMessage != null -> Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )

                tracks.isEmpty() -> Text("曲库中还没有歌曲", color = MaterialTheme.colorScheme.onSurfaceVariant)

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tracks, key = { it.id }) { track ->
                        val alreadyAdded = track.id in existingTrackIds
                        val checked = alreadyAdded || track.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyAdded) {
                                    selectedIds = if (track.id in selectedIds) {
                                        selectedIds - track.id
                                    } else {
                                        selectedIds + track.id
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = null,
                                enabled = !alreadyAdded
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    track.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (alreadyAdded) {
                                        "已在歌单中"
                                    } else {
                                        track.artists.joinToString(" / ").ifBlank { "未知歌手" }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty() && !isLoading
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
