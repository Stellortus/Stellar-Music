package top.stellortus.stellarmusic.ui

import top.stellortus.stellarmusic.R
import top.stellortus.stellarmusic.lang.KeyName

enum class BottomTab(val displayName: KeyName, val iconRes: Int) {
    HomeTab(KeyName.Home, R.drawable.ic_home),
    ListIcon(KeyName.List, R.drawable.ic_list),
    UploadTab(KeyName.Upload, R.drawable.ic_upload),
    AccountIcon(KeyName.Account, R.drawable.ic_account),
}
