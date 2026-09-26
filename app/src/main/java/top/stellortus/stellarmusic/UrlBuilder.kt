package top.stellortus.stellarmusic

import top.stellortus.stellarmusic.network.Domain

fun fromId(id: Int) = "https://$Domain/track/$id"

fun trackList() = "https://$Domain/track_list"