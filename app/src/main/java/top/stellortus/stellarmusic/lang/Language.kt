package top.stellortus.stellarmusic.lang

enum class Language(val getLocalName: (KeyName) -> String) {
    zh_CN({
        when (it) {
            KeyName.Home -> "首页"
            KeyName.List -> "歌单"
            KeyName.Upload -> "上传"
            KeyName.Account -> "我的"
        }
    }),
    en_US({
        it.name
    });
}

fun KeyName.localNameOf(language: Language) = language.getLocalName(this)
