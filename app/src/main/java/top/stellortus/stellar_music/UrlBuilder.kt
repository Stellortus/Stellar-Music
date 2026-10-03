package top.stellortus.stellar_music

import top.stellortus.stellar_music.network.Domain

fun fromId(id: Int) = "https://$Domain/track/$id"

fun trackList() = "https://$Domain/track_list"

fun authRegisterUrl() = "https://$Domain/auth/register"

fun authLoginUrl() = "https://$Domain/auth/login"

fun authMeUrl() = "https://$Domain/auth/me"

fun authLogoutUrl() = "https://$Domain/auth/logout"

fun playlistsUrl() = "https://$Domain/playlists"

fun playlistUrl(id: Int) = "https://$Domain/playlists/$id"
fun playlistTracks(id: Int) = "https://$Domain/playlists/$id/tracks"

fun playlistTrack(id: Int, trackId: Int) = "https://$Domain/playlists/$id/tracks/$trackId"