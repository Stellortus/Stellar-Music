package top.stellortus.stellar_music.ui.screens.playlist

import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.ui.ToastManager
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.Track

/**
 * 长按歌曲后弹出的"添加到歌单"对话框：列出所有歌单，点一个就把 [track] 加进去。
 */
@Composable
fun AddToPlaylistDialog(track: Track, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var reloadTick by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(reloadTick) {
        isLoading = true
        errorMessage = null
        runCatching { PlaylistApi.listPlaylists() }
            .onSuccess { playlists = it }
            .onFailure { errorMessage = it.toPlaylistMessage(); ToastManager.makeText(context, it.toPlaylistMessage()) }
        isLoading = false
    }

    StellarAlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = "添加到歌单",
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap)) {
                StellarText(
                    text = track.title,
                    role = StellarTextRole.BodySmall,
                    tone = StellarTone.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = StellarTheme.sizes.loadingArea),
                        contentAlignment = Alignment.Center
                    ) {
                        StellarLoadingIndicator()
                    }

                    errorMessage != null -> StellarButton(kind = StellarButtonKind.Text, onClick = { reloadTick++ }) { StellarText("重试") }

                    playlists.isEmpty() -> StellarText(
                        "还没有歌单，先去歌单页创建一个吧",
                        tone = StellarTone.Secondary
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = StellarTheme.sizes.dialogHeight),
                        verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)
                    ) {
                        itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { _, playlist ->
                            PlaylistChoiceRow(playlist, enabled = !submitting) {

                                scope.launch {
                                    submitting = true
                                    runCatching {
                                        PlaylistApi.addTracks(playlist.id, listOf(track.id))
                                    }
                                        .onSuccess {
                                            onDismiss()
                                            ToastManager.makeText(
                                                context,
                                                "已添加到「${playlist.name}」"
                                            )
                                        }
                                        .onFailure {
                                            ToastManager.makeText(
                                                context,
                                                it.toPlaylistMessage()
                                            )
                                        }
                                    submitting = false
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            StellarButton(kind = StellarButtonKind.Text, onClick = onDismiss, enabled = !submitting) { StellarText("取消") }
        }
    )
}
