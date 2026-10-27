package top.stellortus.stellar_music

import top.stellortus.stellar_music.network.Domain

fun trackFromId(id: Int) = "https://$Domain/track/$id"

fun trackList() = "https://$Domain/track_list"

fun upload(fileName: String) = "https://$Domain/track/$fileName"

fun trackSearchUrl() = "${trackList()}/search"

fun authRegisterUrl() = "https://$Domain/auth/register"

fun authLoginUrl() = "https://$Domain/auth/login"

fun authMeUrl() = "https://$Domain/auth/me"

fun authLogoutUrl() = "https://$Domain/auth/logout"

fun playlistsUrl() = "https://$Domain/playlists"
fun playlistSearchUrl() = "${playlistsUrl()}/search"

fun playlistUrl(id: Int) = "https://$Domain/playlists/$id"
fun playlistTracks(id: Int) = "https://$Domain/playlists/$id/tracks"

fun playlistTrack(id: Int, trackId: Int) = "https://$Domain/playlists/$id/tracks/$trackId"

fun apkFromVersionName(versionName: String) = "https://$Domain/download/$versionName"

fun versionCode() = "https://$Domain/download_api/latest_version_code"

fun latestVersionNameUrl() = "https://$Domain/download_api/latest_version_name"
