package top.stellortus.stellar_music.ui.screens

import top.stellortus.smooth_kotlin.elseTake
import top.stellortus.smooth_kotlin.ifMeets

internal fun uploadCacheFileName(originalName: String?, defaultName: () -> String): String =
    originalName.orEmpty()
        .ifMeets { it.matches(Regex("^[^\\p{P}][\\s\\S]*\$")) }
        .elseTake(defaultName)
