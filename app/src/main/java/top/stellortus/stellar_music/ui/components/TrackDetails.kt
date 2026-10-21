package top.stellortus.stellar_music.ui.components

import top.stellortus.stellar_music.ui.theme.StellarTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.ui.screens.playlist.AddToPlaylistDialog
import top.stellortus.stellar_music_common.dto.Track

@Composable
fun TrackDetails(track: Track, onDismiss: () -> Unit) {
    var choosingPlaylist by remember(track.id) { mutableStateOf(false) }
    if (choosingPlaylist) {
        AddToPlaylistDialog(track, onDismiss)
    } else {
        StellarBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(StellarTheme.spacing.page).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.section)) {
                CoverPlaceholder(CoverPlaceholderSize.Large)
                Column(verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.small)) {
                    StellarText(track.title, role = StellarTextRole.Title)
                    StellarText(track.artists.joinToString(" / ").ifBlank { "未知歌手" },
                        role = StellarTextRole.BodySmall, tone = StellarTone.Secondary)
                }
                StellarCard( kind = StellarSurfaceKind.Inset) {
                    Column(Modifier.fillMaxWidth().padding(StellarTheme.spacing.section),
                        verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)) {
                        StellarText("歌曲详情", role = StellarTextRole.ItemTitle)
                        StellarText("歌曲 ID：${track.id}", role = StellarTextRole.BodySmall)
                        track.uploader.takeIf { it.isNotBlank() }?.let { StellarText("上传者：$it", role = StellarTextRole.BodySmall) }
                        track.fileName.takeIf { it.isNotBlank() }?.let { StellarText("文件名：$it", role = StellarTextRole.BodySmall) }
                        if (track.createdAt > 0) StellarText("上传时间：${top.stellortus.stellar_music.ui.screens.playlist.formatDateTime(track.createdAt)}", role = StellarTextRole.BodySmall)
                    }
                }
                StellarButton(kind = StellarButtonKind.Primary, onClick = { choosingPlaylist = true }, modifier = Modifier.fillMaxWidth()) { StellarText("加入歌单") }
            }
        }
    }
}
