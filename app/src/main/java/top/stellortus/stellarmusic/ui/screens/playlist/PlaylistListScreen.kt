package top.stellortus.stellarmusic.ui.screens.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import top.stellortus.stellarmusic.network.CreatePlaylistRequest
import top.stellortus.stellarmusic.network.PlaylistApi
import top.stellortus.stellarmusic.network.PlaylistDto
import top.stellortus.stellarmusic.network.UpdatePlaylistRequest
import top.stellortus.stellarmusic.ui.ToastManager

/**
 * 歌单列表页：显示所有人的歌单（不区分 owner），支持新建、重命名、删除。
 * 状态全部用 remember 保存在内存里，不做本地持久化。
 */
@Composable
fun PlaylistListScreen(onOpenPlaylist: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var playlists by remember { mutableStateOf<List<PlaylistDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }

    var renameTarget by remember { mutableStateOf<PlaylistDto?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PlaylistDto?>(null) }

    LaunchedEffect(reloadTick) {
        isLoading = true
        errorMessage = null
        runCatching { PlaylistApi.listPlaylists() }
            .onSuccess { playlists = it }
            .onFailure { errorMessage = it.toPlaylistMessage() }
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("歌单", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = {
                    scope.launch {
                        submitting = true
                        // 不传 name，让服务端生成“歌单N”
                        runCatching { PlaylistApi.createPlaylist(CreatePlaylistRequest()) }
                            .onSuccess { reloadTick++ }
                            .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                        submitting = false
                    }
                },
                enabled = !submitting
            ) {
                Text("新建歌单")
            }
        }

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                OutlinedButton(
                    onClick = { reloadTick++ },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("重试")
                }
            }

            playlists.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("还没有歌单", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onOpen = { onOpenPlaylist(playlist.id) },
                        onRename = {
                            renameTarget = playlist
                            renameText = playlist.name
                        },
                        onDelete = { deleteTarget = playlist }
                    )
                }
            }
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!submitting) renameTarget = null },
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
                                PlaylistApi.updatePlaylist(target.id, UpdatePlaylistRequest(name = name))
                            }
                                .onSuccess {
                                    renameTarget = null
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
                TextButton(onClick = { renameTarget = null }, enabled = !submitting) { Text("取消") }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!submitting) deleteTarget = null },
            title = { Text("删除歌单") },
            text = { Text("确定删除「${target.name}」吗？歌单内的歌曲不会被删除。") },
            confirmButton = {
                TextButton(
                    enabled = !submitting,
                    onClick = {
                        scope.launch {
                            submitting = true
                            runCatching { PlaylistApi.deletePlaylist(target.id) }
                                .onSuccess {
                                    deleteTarget = null
                                    reloadTick++
                                }
                                .onFailure { ToastManager.makeText(context, it.toPlaylistMessage()) }
                            submitting = false
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }, enabled = !submitting) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistDto,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val updatedAt = remember(playlist.updatedAt) { formatDateTime(playlist.updatedAt) }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                playlist.trackCount?.let { append("$it 首歌曲 · ") }
                append("更新于 $updatedAt")
            }
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRename) { Text("重命名") }
                TextButton(onClick = onDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
