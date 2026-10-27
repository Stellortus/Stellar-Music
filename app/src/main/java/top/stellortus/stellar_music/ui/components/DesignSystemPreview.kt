package top.stellortus.stellar_music.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.Track

@Preview(name = "Components Light", widthDp = 360, heightDp = 900)
@Preview(name = "Components Dark", widthDp = 360, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Components Large Text", widthDp = 320, heightDp = 900, fontScale = 1.5f)
@Composable
private fun ComponentsPreview() {
    StellarMusicTheme {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(StellarTheme.spacing.page),
            verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)) {
            StellarText("按钮与状态", role = StellarTextRole.ItemTitle)
            StellarButton(onClick = {}) { StellarText("主要操作") }
            StellarButton(onClick = {}, kind = StellarButtonKind.Secondary) { StellarText("次要操作") }
            StellarButton(onClick = {}, kind = StellarButtonKind.Outlined) { StellarText("描边操作") }
            StellarButton(onClick = {}, kind = StellarButtonKind.Text) { StellarText("文字操作") }
            StellarButton(onClick = {}, kind = StellarButtonKind.Danger) { StellarText("删除") }
            StellarButton(onClick = {}, enabled = false) { StellarText("不可用") }
            StellarButton(onClick = {}, loading = true) { StellarText("正在处理…") }
            StellarTextField("星河漫游", {}, "歌单名称", Modifier.fillMaxWidth())
            StellarTextField("不可编辑", {}, "禁用输入框", Modifier.fillMaxWidth(), enabled = false)
            StellarSearchField("", {}, placeholder = "搜索音乐")
            StellarSearchField("星河", {}, placeholder = "搜索歌单")
            StellarSearchField("", {}, enabled = false)
            StellarSearchField("", {}, onClick = {}, placeholder = "搜索歌单")
            PlaylistToolbar({})
            Row { StellarCheckbox(true, {}); StellarCheckbox(false, {}); StellarCheckbox(true, null, enabled = false) }
            Row(horizontalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap)) {
                CoverPlaceholder(CoverPlaceholderSize.Small)
                CoverPlaceholder(CoverPlaceholderSize.Medium)
                CoverPlaceholder(CoverPlaceholderSize.Large)
            }
            FormCard {
                StellarText("菜单操作", role = StellarTextRole.ItemTitle)
                StellarMenuContent(listOf(
                    StellarMenuAction("重命名", R.drawable.ic_edit, {}),
                    StellarMenuAction("删除", R.drawable.ic_delete, {}, StellarMenuActionKind.Danger)))
            }
            TrackItem(Track(1, "一首很长的歌曲名称，用于检查紧凑布局和省略行为", listOf("Stellar"), "music.mp3", "Listener", "", "", 0L),
                onClick = {}, isCurrent = true, showDivider = true)
            StellarEmptyState("还没有歌单")
            StellarRetryAction({})
        }
    }
}

@Preview(name = "Dialog Light", widthDp = 360, heightDp = 640)
@Preview(name = "Dialog Dark", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Dialog Large Text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun DialogPreview() {
    StellarMusicTheme {
        StellarAlertDialog("重命名歌单", onDismissRequest = {},
            text = { StellarTextField("我的歌单", {}, "歌单名称", Modifier.fillMaxWidth()) },
            confirmButton = { StellarButton({}, kind = StellarButtonKind.Secondary) { StellarText("保存") } },
            dismissButton = { StellarButton({}, kind = StellarButtonKind.Text) { StellarText("取消") } })
    }
}

@Preview(name = "Create Playlist Light", widthDp = 360, heightDp = 640)
@Preview(name = "Create Playlist Dark", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Create Playlist Large Text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun CreatePlaylistPreview() {
    StellarMusicTheme {
        CreatePlaylistDialog("留给深夜的声音", {}, false, {}, false, {}, {})
    }
}

@Preview(name = "Search Results Light", widthDp = 360, heightDp = 640)
@Preview(name = "Search Results Dark", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Search Results Large Text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun SearchResultsPreview() {
    StellarMusicTheme {
        Column(Modifier.fillMaxSize().padding(StellarTheme.spacing.page)) {
            StellarSearchField("留给深夜的声音", {}, onClick = {}, contentDescription = "返回歌单列表，编辑搜索内容")
            PlaylistItem(
                Playlist(1, 1, "留给深夜的声音", null, null, true, 0L, 0L, 12),
                {}, {}, {}, {}, showActions = false)
        }
    }
}
