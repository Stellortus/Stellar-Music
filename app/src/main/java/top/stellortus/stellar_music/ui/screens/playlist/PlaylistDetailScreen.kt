package top.stellortus.stellar_music.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music_common.dto.PlaylistDetailResponse
import top.stellortus.stellar_music_common.dto.Track
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

@Composable
fun PlaylistDetailScreen(playlistId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<PlaylistDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    LaunchedEffect(playlistId, reloadTick) {
        isLoading = true
        errorMessage = null
        runCatching { PlaylistApi.getPlaylist(playlistId) }
            .onSuccess { detail = it }
            .onFailure { errorMessage = it.toPlaylistMessage() }
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onBack) { Text("返回") }
            Text(
                text = detail?.name ?: "歌单详情",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                enabled = detail != null && !submitting,
                onClick = {
                    renameText = detail?.name.orEmpty()
                    showRenameDialog = true
                }
            ) {
                Text("编辑")
            }
        }

        detail?.let { current ->
            val updatedAt = remember(current.updatedAt) { formatDateTime(current.updatedAt) }
            Text(
                text = "${current.tracks.size} 首歌曲 · 更新于 $updatedAt",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val current = detail
        val currentError = errorMessage
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            currentError != null -> Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(currentError, color = MaterialTheme.colorScheme.error)
                OutlinedButton(
                    onClick = { reloadTick++ },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("重试")
                }
            }

            current == null || current.tracks.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("歌单里还没有歌曲", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> {
                val tracks = current.tracks.distinctBy { it.id }
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                        TrackRow(
                            track = track,
                            enabled = !submitting,
                            onRemove = {
                                scope.launch {
                                    submitting = true
                                    runCatching { PlaylistApi.removeTrack(playlistId, track.id) }
                                        .onSuccess { reloadTick++ }
                                        .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                                    submitting = false
                                }
                            }
                        )
                        if (index < tracks.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            enabled = detail != null && !submitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加歌曲")
        }
    }

    if (showAddDialog) {
        AddTracksDialog(
            existingTrackIds = detail?.tracks?.map { it.id }?.toSet().orEmpty(),
            onDismiss = { showAddDialog = false },
            onConfirm = { trackIds ->
                scope.launch {
                    submitting = true
                    runCatching { PlaylistApi.addTracks(playlistId, trackIds) }
                        .onSuccess {
                            showAddDialog = false
                            reloadTick++
                        }
                        .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                    submitting = false
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { if (!submitting) showRenameDialog = false },
            title = { Text("重命名歌单") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("歌单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank() && !submitting,
                    onClick = {
                        val name = renameText.trim()
                        scope.launch {
                            submitting = true
                            runCatching {
                                PlaylistApi.updatePlaylist(playlistId,
                                    UpdatePlaylistRequest(name = name)
                                )
                            }
                                .onSuccess {
                                    showRenameDialog = false
                                    reloadTick++
                                }
                                .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                            submitting = false
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }, enabled = !submitting) { Text("取消") }
            }
        )
    }
}

@Composable
private fun TrackRow(track: Track, enabled: Boolean, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artists.joinToString(" / ").ifBlank { "未知歌手" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "上传者：${track.uploader}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onRemove, enabled = enabled) {
            Text("移除", color = MaterialTheme.colorScheme.error)
        }
    }
}
