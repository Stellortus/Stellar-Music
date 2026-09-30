package top.stellortus.stellarmusic

import top.stellortus.stellarmusic.network.Domain

fun fromId(id: Int) = "https://$Domain/track/$id"

fun trackList() = "https://$Domain/track_list"

fun playlistsUrl() = "https://$Domain/playlists"

fun playlistUrl(id: Int) = "https://$Domain/playlists/$id"

fun playlistTracks(id: Int) = "https://$Domain/playlists/$id/tracks"

fun playlistTrack(id: Int, trackId: Int) = "https://$Domain/playlists/$id/tracks/$trackId"