package top.stellortus.stellarmusic.ui

import androidx.compose.runtime.Composable
import top.stellortus.stellarmusic.R
import top.stellortus.stellarmusic.lang.KeyName
import top.stellortus.stellarmusic.ui.screens.AccountScreen
import top.stellortus.stellarmusic.ui.screens.HomeScreen
import top.stellortus.stellarmusic.ui.screens.UploadScreen

enum class BottomTab(val displayName: KeyName, val iconRes: Int, val ui: @Composable () -> Unit) {
    HomeTab(KeyName.Home, R.drawable.ic_home, ::HomeScreen),
    ListIcon(KeyName.List, R.drawable.ic_list, {}),
    UploadTab(KeyName.Upload, R.drawable.ic_upload, ::UploadScreen),
    AccountIcon(KeyName.Account, R.drawable.ic_account, ::AccountScreen),
}
